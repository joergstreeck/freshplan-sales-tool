# FC-003: E-Mail Integration - Implementation Guide

## 📋 Prerequisites

- [ ] SMTP-Server Entscheidung getroffen
- [ ] S3 Bucket für Attachments konfiguriert
- [ ] Customer Timeline (M5) implementiert
- [ ] Activity System (M4) verfügbar

## 🏗️ Backend Implementation

### Step 1: Email Entity & Repository

```java
// backend/src/main/java/de/freshplan/domain/email/entity/Email.java
@Entity
@Table(name = "emails")
public class Email extends PanacheEntityBase {
    @Id
    @GeneratedValue
    public UUID id;
    
    @ManyToOne
    @JoinColumn(name = "customer_id")
    public Customer customer;
    
    @Column(name = "message_id", unique = true)
    public String messageId;
    
    @Column(name = "in_reply_to")
    public String inReplyTo;
    
    @Column(name = "subject")
    public String subject;
    
    @Column(name = "from_address")
    public String fromAddress;
    
    @Column(name = "to_addresses", columnDefinition = "TEXT")
    public String toAddresses;
    
    @Column(name = "body_text", columnDefinition = "TEXT")
    public String bodyText;
    
    @Column(name = "body_html", columnDefinition = "TEXT")
    public String bodyHtml;
    
    @Column(name = "received_at")
    public LocalDateTime receivedAt;
    
    @OneToMany(mappedBy = "email", cascade = CascadeType.ALL)
    public List<EmailAttachment> attachments;
}
```

### Step 2: SMTP Receiver Service

```java
// backend/src/main/java/de/freshplan/infrastructure/email/SmtpReceiverService.java
@ApplicationScoped
@Startup
public class SmtpReceiverService {
    
    @Inject
    EmailIngestionService emailIngestionService;
    
    @ConfigProperty(name = "freshplan.smtp.port", defaultValue = "2525")
    int smtpPort;
    
    private SMTPServer smtpServer;
    
    void onStart(@Observes StartupEvent event) {
        smtpServer = new SMTPServer(new SimpleMessageListenerAdapter() {
            @Override
            public void deliver(String from, String recipient, InputStream data) {
                try {
                    MimeMessage message = new MimeMessage(null, data);
                    emailIngestionService.processIncomingEmail(
                        convertToIncomingEmail(message, recipient)
                    );
                } catch (Exception e) {
                    Log.error("Failed to process email", e);
                }
            }
        });
        
        smtpServer.setPort(smtpPort);
        smtpServer.start();
        Log.info("SMTP Server started on port " + smtpPort);
    }
}
```

### Step 3: Email Ingestion Service

```java
// backend/src/main/java/de/freshplan/domain/email/service/EmailIngestionService.java
@ApplicationScoped
@Transactional
public class EmailIngestionService {
    
    @Inject
    EmailRepository emailRepository;
    
    @Inject
    CustomerRepository customerRepository;
    
    @Inject
    TriageInboxService triageInboxService;
    
    @Inject
    Event<EmailReceivedEvent> emailReceivedEvent;
    
    private static final Pattern CUSTOMER_EMAIL_PATTERN = 
        Pattern.compile("kunde-(\\d+)@crm\\.freshplan\\.de");
    
    public void processIncomingEmail(IncomingEmail email) {
        // Check for duplicate
        if (emailRepository.findByMessageId(email.getMessageId()).isPresent()) {
            Log.info("Duplicate email ignored: " + email.getMessageId());
            return;
        }
        
        // Extract customer ID
        Optional<Long> customerId = extractCustomerId(email.getTo());
        
        Email entity = new Email();
        entity.messageId = email.getMessageId();
        entity.subject = email.getSubject();
        entity.fromAddress = email.getFrom();
        entity.toAddresses = String.join(",", email.getTo());
        entity.bodyText = email.getBodyText();
        entity.bodyHtml = email.getBodyHtml();
        entity.receivedAt = LocalDateTime.now();
        entity.inReplyTo = email.getInReplyTo();
        
        if (customerId.isPresent()) {
            // Assign to customer
            entity.customer = customerRepository.findById(customerId.get());
            emailRepository.persist(entity);
            
            // Fire event for timeline
            emailReceivedEvent.fire(new EmailReceivedEvent(
                entity.id,
                customerId.get(),
                entity.subject
            ));
            
            Log.info("Email assigned to customer " + customerId.get());
        } else {
            // Add to triage inbox
            emailRepository.persist(entity);
            triageInboxService.addUnassignedEmail(entity);
            
            Log.info("Email added to triage inbox");
        }
    }
    
    private Optional<Long> extractCustomerId(List<String> recipients) {
        return recipients.stream()
            .map(CUSTOMER_EMAIL_PATTERN::matcher)
            .filter(Matcher::matches)
            .map(m -> Long.parseLong(m.group(1)))
            .findFirst();
    }
}
```

### Step 4: REST Endpoints

```java
// backend/src/main/java/de/freshplan/api/resources/EmailResource.java
@Path("/api/emails")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class EmailResource {
    
    @Inject
    EmailService emailService;
    
    @GET
    @Path("/customer/{customerId}")
    public Response getCustomerEmails(
            @PathParam("customerId") Long customerId,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        
        Page<EmailDto> emails = emailService.getCustomerEmails(customerId, page, size);
        return Response.ok(emails).build();
    }
    
    @POST
    @Path("/{emailId}/assign")
    public Response assignEmail(
            @PathParam("emailId") UUID emailId,
            AssignEmailRequest request) {
        
        emailService.assignToCustomer(emailId, request.customerId);
        return Response.noContent().build();
    }
}
```

## 🎨 Frontend Implementation

### Step 1: Email Timeline Component

```typescript
// frontend/src/features/email/components/EmailTimeline.tsx
import React from 'react';
import { useCustomerEmails } from '../hooks/useCustomerEmails';
import { Box, Card, Typography, Chip } from '@mui/material';
import { EmailOutlined } from '@mui/icons-material';

interface EmailTimelineProps {
  customerId: number;
}

export const EmailTimeline: React.FC<EmailTimelineProps> = ({ customerId }) => {
  const { data: emails, isLoading } = useCustomerEmails(customerId);
  
  if (isLoading) return <EmailTimelineSkeleton />;
  
  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
      {emails?.map((email) => (
        <Card key={email.id} sx={{ p: 2 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
            <EmailOutlined fontSize="small" />
            <Typography variant="subtitle2">
              {email.fromAddress}
            </Typography>
            <Chip 
              label={formatDate(email.receivedAt)} 
              size="small" 
              variant="outlined" 
            />
          </Box>
          
          <Typography variant="h6" gutterBottom>
            {email.subject}
          </Typography>
          
          <Typography 
            variant="body2" 
            color="text.secondary"
            sx={{ 
              maxHeight: 100, 
              overflow: 'hidden',
              textOverflow: 'ellipsis'
            }}
          >
            {email.bodyText}
          </Typography>
          
          {email.attachments?.length > 0 && (
            <Box sx={{ mt: 1 }}>
              <Chip 
                icon={<AttachFileIcon />}
                label={`${email.attachments.length} Anhänge`}
                size="small"
              />
            </Box>
          )}
        </Card>
      ))}
    </Box>
  );
};
```

### Step 2: Triage Inbox Extension

```typescript
// frontend/src/features/cockpit/components/TriageInbox.tsx
import React, { useState } from 'react';
import { useTriageEmails } from '../hooks/useTriageEmails';
import { 
  List, ListItem, ListItemText, ListItemSecondaryAction,
  IconButton, Dialog, TextField, Autocomplete
} from '@mui/material';
import { AssignmentInd } from '@mui/icons-material';

export const TriageInbox: React.FC = () => {
  const { data: emails, isLoading } = useTriageEmails();
  const [assignDialog, setAssignDialog] = useState<{
    open: boolean;
    emailId?: string;
  }>({ open: false });
  
  const handleAssign = (emailId: string) => {
    setAssignDialog({ open: true, emailId });
  };
  
  return (
    <>
      <List>
        {emails?.map((email) => (
          <ListItem key={email.id}>
            <ListItemText
              primary={email.subject}
              secondary={`Von: ${email.fromAddress} • ${formatDate(email.receivedAt)}`}
            />
            <ListItemSecondaryAction>
              <IconButton 
                edge="end" 
                onClick={() => handleAssign(email.id)}
                title="Kunde zuordnen"
              >
                <AssignmentInd />
              </IconButton>
            </ListItemSecondaryAction>
          </ListItem>
        ))}
      </List>
      
      <AssignEmailDialog
        open={assignDialog.open}
        emailId={assignDialog.emailId}
        onClose={() => setAssignDialog({ open: false })}
      />
    </>
  );
};
```

## 🧪 Testing

### Backend Tests

```java
@QuarkusTest
@TestProfile(TestProfile.class)
class EmailIngestionServiceTest {
    
    @Inject
    EmailIngestionService service;
    
    @Test
    @Transactional
    void shouldAssignEmailToCustomer() {
        // Given
        var email = new IncomingEmail();
        email.setTo(List.of("kunde-123@crm.freshplan.de"));
        email.setSubject("Test Email");
        email.setMessageId("<test@example.com>");
        
        // When
        service.processIncomingEmail(email);
        
        // Then
        var saved = emailRepository.findByMessageId("<test@example.com>");
        assertThat(saved).isPresent();
        assertThat(saved.get().customer.id).isEqualTo(123L);
    }
}
```

## 🚀 Deployment

### Environment Variables

```bash
# SMTP Configuration
FRESHPLAN_SMTP_PORT=2525
FRESHPLAN_SMTP_HOST=0.0.0.0

# S3 Configuration
AWS_S3_BUCKET=freshplan-email-attachments
AWS_REGION=eu-central-1
```

### Database Migration

```sql
-- V1.0__create_email_tables.sql
CREATE TABLE emails (
    id UUID PRIMARY KEY,
    customer_id BIGINT REFERENCES customers(id),
    message_id VARCHAR(255) UNIQUE NOT NULL,
    in_reply_to VARCHAR(255),
    subject VARCHAR(500),
    from_address VARCHAR(255),
    to_addresses TEXT,
    body_text TEXT,
    body_html TEXT,
    received_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_emails_customer ON emails(customer_id);
CREATE INDEX idx_emails_message_id ON emails(message_id);
CREATE INDEX idx_emails_received_at ON emails(received_at DESC);
```

## 📊 Monitoring

- SMTP Server Health Check
- Email Processing Queue Size
- Failed Assignment Rate
- Average Processing Time

## 🔐 Security

- Validate sender domains
- Sanitize HTML content
- Virus scan attachments
- Rate limiting per sender