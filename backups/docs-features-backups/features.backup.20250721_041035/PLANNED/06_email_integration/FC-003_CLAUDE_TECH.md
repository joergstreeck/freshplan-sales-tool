# FC-003 E-Mail Integration - CLAUDE TECH 🤖

## 🧠 QUICK-LOAD (30 Sekunden bis zur Produktivität)

**Feature:** BCC-to-CRM E-Mail Integration mit automatischer Kundenzuordnung  
**Stack:** Quarkus + Apache James SMTP + PostgreSQL + React  
**Status:** 🟡 READY TO START - Alle Dependencies verfügbar  
**Dependencies:** FC-008 Security (✅) | FC-014 Timeline (pending) | M4 Pipeline (✅)  

**Jump to:** [📚 Recipes](#-implementation-recipes) | [🧪 Tests](#-test-patterns) | [🔌 Integration](#-integration-cookbook) | [📊 Triage](#-triage-inbox)

**Core Purpose in 1 Line:** `BCC an kunde-{id}@crm.freshplan.de → Automatisch in Timeline → Triage für Unbekannte`

---

## 🍳 IMPLEMENTATION RECIPES

### Recipe 1: SMTP Server in 5 Minuten

```java
// 1. Add to pom.xml
<dependency>
    <groupId>org.apache.james</groupId>
    <artifactId>apache-james-protocols-smtp</artifactId>
    <version>3.8.0</version>
</dependency>

// 2. SmtpReceiverService.java - Copy-paste ready
@ApplicationScoped
@Startup
public class SmtpReceiverService {
    
    @ConfigProperty(name = "email.smtp.port", defaultValue = "2525")
    int smtpPort;
    
    @Inject EmailProcessingService processingService;
    
    private SMTPServer smtpServer;
    
    void onStart(@Observes StartupEvent ev) {
        smtpServer = SMTPServer.port(smtpPort)
            .hostName("0.0.0.0")
            .messageHandlerFactory(ctx -> new MessageHandler() {
                String from;
                List<String> recipients = new ArrayList<>();
                
                @Override
                public void from(String from) { this.from = from; }
                
                @Override
                public void recipient(String recipient) throws RejectException {
                    if (!recipient.endsWith("@crm.freshplan.de")) {
                        throw new RejectException("Invalid domain");
                    }
                    recipients.add(recipient);
                }
                
                @Override
                public void data(InputStream data) throws IOException {
                    processingService.processAsync(IncomingEmail.builder()
                        .from(from)
                        .recipients(recipients)
                        .rawData(data)
                        .build());
                }
            })
            .build();
            
        smtpServer.start();
        Log.info("SMTP Server started on port " + smtpPort);
    }
}

// 3. Docker-compose addition
services:
  app:
    ports:
      - "2525:2525"  # SMTP Port
    environment:
      - EMAIL_SMTP_PORT=2525
```

### Recipe 2: Email Processing Pipeline

```java
// EmailProcessingService.java - Core Logic
@ApplicationScoped
public class EmailProcessingService {
    
    @Inject CustomerRepository customerRepo;
    @Inject EmailActivityService activityService;
    @Inject TriageInboxService triageService;
    
    // Pattern: kunde-{uuid}@crm.freshplan.de or {number}@crm.freshplan.de
    private static final Pattern UUID_PATTERN = 
        Pattern.compile("kunde-([a-f0-9-]{36})@crm\\.freshplan\\.de");
    private static final Pattern NUMBER_PATTERN = 
        Pattern.compile("([0-9]+)@crm\\.freshplan\\.de");
    
    @Async
    public CompletionStage<Void> processAsync(IncomingEmail email) {
        return CompletableFuture.runAsync(() -> {
            try {
                var match = parseCustomerFromRecipients(email);
                
                if (match.isPresent()) {
                    processCustomerEmail(match.get(), email);
                } else {
                    processUnmatchedEmail(email);
                }
            } catch (Exception e) {
                Log.error("Email processing failed", e);
                triageService.addToDeadLetter(email, e.getMessage());
            }
        });
    }
    
    private Optional<Customer> parseCustomerFromRecipients(IncomingEmail email) {
        for (String recipient : email.getRecipients()) {
            // Try UUID: kunde-123e4567-e89b-12d3-a456-426614174000@crm.freshplan.de
            var uuidMatcher = UUID_PATTERN.matcher(recipient.toLowerCase());
            if (uuidMatcher.matches()) {
                var customerId = UUID.fromString(uuidMatcher.group(1));
                var customer = customerRepo.findById(customerId);
                if (customer.isPresent()) return customer;
            }
            
            // Try Number: 12345@crm.freshplan.de
            var numberMatcher = NUMBER_PATTERN.matcher(recipient.toLowerCase());
            if (numberMatcher.matches()) {
                var customerNumber = numberMatcher.group(1);
                return customerRepo.findByCustomerNumber(customerNumber);
            }
        }
        return Optional.empty();
    }
    
    private void processCustomerEmail(Customer customer, IncomingEmail email) {
        // Parse email content
        var content = parseEmailContent(email);
        
        // Create activity
        var activity = activityService.createEmailActivity(
            customer.getId(),
            email.getFrom(),
            content.getSubject(),
            content.getPlainText(),
            ActivityDirection.INCOMING
        );
        
        Log.info("Email assigned to customer {} as activity {}", 
                 customer.getId(), activity.getId());
    }
}
```

### Recipe 3: Database Schema (Copy-paste for Flyway)

```sql
-- V8.0__email_integration.sql
CREATE TABLE email_threads (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES customers(id),
    root_message_id VARCHAR(500),
    subject VARCHAR(500),
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_activity TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    message_count INT DEFAULT 1,
    participants TEXT[]
);

CREATE TABLE emails (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    activity_id UUID NOT NULL REFERENCES activities(id),
    thread_id UUID REFERENCES email_threads(id),
    message_id VARCHAR(500) UNIQUE,
    in_reply_to VARCHAR(500),
    from_address VARCHAR(255) NOT NULL,
    to_addresses TEXT[],
    subject VARCHAR(500),
    plain_text_body TEXT,
    html_body TEXT,
    has_attachments BOOLEAN DEFAULT FALSE
);

CREATE TABLE triage_emails (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    from_address VARCHAR(255),
    subject VARCHAR(500),
    body TEXT,
    received_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'PENDING',
    reason VARCHAR(255),
    suggested_customer_id UUID REFERENCES customers(id),
    assigned_to_customer_id UUID REFERENCES customers(id),
    assigned_by_user_id UUID REFERENCES users(id),
    assigned_at TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_email_thread_customer ON email_threads(customer_id);
CREATE INDEX idx_email_activity ON emails(activity_id);
CREATE INDEX idx_triage_status ON triage_emails(status) WHERE status = 'PENDING';
```

### Recipe 4: Frontend Email Timeline

```typescript
// EmailTimeline.tsx - Drop into Customer Detail
export const EmailTimeline: React.FC<{customerId: string}> = ({customerId}) => {
    const { data: threads } = useEmailThreads(customerId);
    const [expanded, setExpanded] = useState<Set<string>>(new Set());
    
    return (
        <Box sx={{ maxHeight: 600, overflow: 'auto' }}>
            <Box sx={{ p: 2, borderBottom: 1, borderColor: 'divider' }}>
                <Typography variant="h6">E-Mail Verlauf</Typography>
                <Typography variant="caption" color="text.secondary">
                    BCC an: kunde-{customerId}@crm.freshplan.de
                </Typography>
            </Box>
            
            <List>
                {threads?.map(thread => (
                    <React.Fragment key={thread.id}>
                        <ListItem 
                            button 
                            onClick={() => toggleExpanded(thread.id)}
                        >
                            <ListItemIcon>
                                {expanded.has(thread.id) 
                                    ? <ExpandLess /> 
                                    : <ExpandMore />
                                }
                            </ListItemIcon>
                            <ListItemText
                                primary={thread.subject}
                                secondary={`${thread.messageCount} E-Mails · ${
                                    formatRelativeTime(thread.lastActivity)
                                }`}
                            />
                        </ListItem>
                        
                        <Collapse in={expanded.has(thread.id)}>
                            <EmailThreadMessages threadId={thread.id} />
                        </Collapse>
                    </React.Fragment>
                ))}
            </List>
            
            {!threads?.length && (
                <Box sx={{ p: 4, textAlign: 'center' }}>
                    <EmailIcon sx={{ fontSize: 48, color: 'text.disabled' }} />
                    <Typography color="text.secondary">
                        Noch keine E-Mails
                    </Typography>
                </Box>
            )}
        </Box>
    );
    
    function toggleExpanded(threadId: string) {
        setExpanded(prev => {
            const next = new Set(prev);
            if (next.has(threadId)) {
                next.delete(threadId);
            } else {
                next.add(threadId);
            }
            return next;
        });
    }
};

// Hook for email data
export const useEmailThreads = (customerId: string) => {
    return useQuery({
        queryKey: ['email-threads', customerId],
        queryFn: () => apiClient.get(`/api/customers/${customerId}/email-threads`)
            .then(res => res.data)
    });
};
```

---

## 🧪 TEST PATTERNS

### Pattern 1: SMTP Server Test
```java
@QuarkusTest
class SmtpReceiverTest {
    
    @TestHTTPResource("/")
    URL baseUrl;
    
    @Test
    void testEmailReception() throws Exception {
        // Send test email
        var session = Session.getDefaultInstance(new Properties());
        var message = new MimeMessage(session);
        message.setFrom("test@example.com");
        message.addRecipient(TO, new InternetAddress("kunde-123@crm.freshplan.de"));
        message.setSubject("Test Email");
        message.setText("Test content");
        
        Transport.send(message, "localhost", 2525);
        
        // Verify processing
        await().atMost(5, SECONDS).until(() -> {
            var activities = given()
                .when().get("/api/customers/123/activities")
                .then().extract().jsonPath()
                .getList(".", Activity.class);
            
            return activities.stream()
                .anyMatch(a -> a.getSubject().equals("Test Email"));
        });
    }
}
```

### Pattern 2: Customer Matching Test
```java
@Test
void testCustomerPatternMatching() {
    var service = new EmailProcessingService();
    
    // UUID pattern
    assertThat(service.extractCustomerId("kunde-550e8400-e29b-41d4-a716-446655440000@crm.freshplan.de"))
        .isEqualTo("550e8400-e29b-41d4-a716-446655440000");
    
    // Number pattern
    assertThat(service.extractCustomerNumber("12345@crm.freshplan.de"))
        .isEqualTo("12345");
    
    // Invalid patterns
    assertThat(service.extractCustomerId("invalid@crm.freshplan.de"))
        .isEmpty();
}
```

---

## 🔌 INTEGRATION COOKBOOK

### Mit M4 Opportunity Pipeline
```java
// Auto-create opportunity from important emails
@Observes
void onEmailReceived(@EmailReceived EmailReceivedEvent event) {
    var email = emailRepo.findById(event.getEmailId());
    
    // Check for opportunity keywords
    if (containsOpportunityKeywords(email.getSubject())) {
        var opportunity = Opportunity.builder()
            .customerId(event.getCustomerId())
            .title("Anfrage: " + email.getSubject())
            .source("EMAIL")
            .stage("LEAD")
            .build();
            
        opportunityService.create(opportunity);
    }
}
```

### Mit FC-014 Activity Timeline
```java
// Email activities automatically appear in timeline
public class EmailActivity extends Activity {
    
    @Override
    public ActivityType getType() {
        return ActivityType.EMAIL;
    }
    
    @Override
    public String getIcon() {
        return direction == INCOMING ? "inbox" : "send";
    }
    
    @Override
    public String getPreview() {
        return StringUtils.abbreviate(plainTextBody, 200);
    }
}
```

---

## 📊 TRIAGE INBOX

### Recipe 5: Triage Inbox Component
```typescript
// TriageInbox.tsx - Standalone page for unmatched emails
export const TriageInbox: React.FC = () => {
    const { data: emails } = useTriageEmails();
    const [selected, setSelected] = useState<Set<string>>(new Set());
    const { assignToCustomer } = useEmailService();
    
    const handleAssign = async (customerId: string) => {
        await Promise.all(
            Array.from(selected).map(emailId => 
                assignToCustomer(emailId, customerId)
            )
        );
        setSelected(new Set());
    };
    
    return (
        <Paper sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
            <Box sx={{ p: 2, borderBottom: 1, borderColor: 'divider' }}>
                <Typography variant="h6">
                    Triage Inbox
                    <Chip 
                        label={emails?.length || 0} 
                        color="warning" 
                        size="small" 
                        sx={{ ml: 1 }} 
                    />
                </Typography>
            </Box>
            
            <List sx={{ flexGrow: 1, overflow: 'auto' }}>
                {emails?.map(email => (
                    <TriageEmailItem
                        key={email.id}
                        email={email}
                        selected={selected.has(email.id)}
                        onToggleSelect={() => toggleSelect(email.id)}
                    />
                ))}
            </List>
            
            {selected.size > 0 && (
                <Box sx={{ p: 2, borderTop: 1, borderColor: 'divider' }}>
                    <CustomerSearchAssign
                        onAssign={handleAssign}
                        emailCount={selected.size}
                    />
                </Box>
            )}
        </Paper>
    );
};
```

### Recipe 6: Quick Customer Assignment
```typescript
// CustomerSearchAssign.tsx
export const CustomerSearchAssign: React.FC<Props> = ({ onAssign, emailCount }) => {
    const [search, setSearch] = useState('');
    const { data: customers } = useCustomerSearch(search);
    
    return (
        <Box>
            <TextField
                fullWidth
                placeholder="Kunde suchen..."
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                sx={{ mb: 2 }}
            />
            
            {customers?.map(customer => (
                <Box 
                    key={customer.id}
                    sx={{ 
                        display: 'flex', 
                        justifyContent: 'space-between',
                        p: 1,
                        '&:hover': { bgcolor: 'action.hover' }
                    }}
                >
                    <Box>
                        <Typography>{customer.name}</Typography>
                        <Typography variant="caption" color="text.secondary">
                            {customer.email}
                        </Typography>
                    </Box>
                    <Button
                        size="small"
                        onClick={() => onAssign(customer.id)}
                    >
                        {emailCount} zuordnen
                    </Button>
                </Box>
            ))}
        </Box>
    );
};
```

---

## 📚 DEEP KNOWLEDGE (Bei Bedarf expandieren)

<details>
<summary>🔧 Production Setup</summary>

### DNS Configuration
```bash
# MX Record for crm.freshplan.de
crm.freshplan.de.  300  IN  MX  10  mail.freshplan.de.

# SPF Record
freshplan.de.  300  IN  TXT  "v=spf1 mx a ip4:YOUR_IP -all"

# DKIM (optional but recommended)
default._domainkey.freshplan.de.  300  IN  TXT  "v=DKIM1; k=rsa; p=MIGf..."
```

### Docker Production Setup
```yaml
version: '3.8'
services:
  smtp:
    image: apache/james:latest
    ports:
      - "25:25"
    volumes:
      - ./james-config:/opt/james/conf
    environment:
      - JAMES_DOMAIN=crm.freshplan.de
```

</details>

<details>
<summary>📊 Performance Tuning</summary>

### Async Processing Configuration
```properties
# application.properties
email.processing.thread-pool-size=10
email.processing.queue-size=1000
email.processing.timeout-seconds=30

# Circuit breaker for resilience
email.processing.circuit-breaker.failure-threshold=5
email.processing.circuit-breaker.delay=60000
```

### Database Optimization
```sql
-- Partial index for faster triage queries
CREATE INDEX idx_triage_pending_recent 
ON triage_emails(received_at DESC) 
WHERE status = 'PENDING';

-- Full-text search
CREATE INDEX idx_email_fts 
ON emails USING gin(to_tsvector('german', subject || ' ' || plain_text_body));
```

</details>

<details>
<summary>🔒 Security Considerations</summary>

### Anti-Spam Measures
```java
@ApplicationScoped
public class SpamFilter {
    
    public boolean isSpam(IncomingEmail email) {
        // Rate limiting
        if (getRecentEmailCount(email.getFrom()) > 50) {
            return true;
        }
        
        // Blacklist check
        if (isBlacklisted(email.getFrom())) {
            return true;
        }
        
        // SPF check
        if (!spfValidator.validate(email)) {
            return true;
        }
        
        return false;
    }
}
```

</details>

---

**🎯 Nächster Schritt:** SMTP Server Setup mit Docker und erste Email-Verarbeitung testen