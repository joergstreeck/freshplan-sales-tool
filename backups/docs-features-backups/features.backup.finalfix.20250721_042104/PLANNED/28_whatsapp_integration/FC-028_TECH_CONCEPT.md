# FC-028: WhatsApp Business Integration - Technisches Konzept

**Status:** 📋 PLANNED  
**Claude Tech:** [FC-028_CLAUDE_TECH.md](/docs/features/PLANNED/28_whatsapp_integration/FC-028_CLAUDE_TECH.md)

## Navigation
- **Zurück:** [FC-027 Magic Moments](/docs/features/PLANNED/27_magic_moments/FC-027_TECH_CONCEPT.md)
- **Weiter:** [FC-029 Voice First](/docs/features/PLANNED/29_voice_first/FC-029_TECH_CONCEPT.md)
- **Übersicht:** [Master Plan V5](/docs/CRM_COMPLETE_MASTER_PLAN_V5.md)

## Übersicht
Integration der WhatsApp Business API für direkte Kundenkommunikation aus dem CRM heraus. Ermöglicht das Senden von Nachrichten, Empfangen von Antworten und automatische Synchronisation mit der Activity Timeline.

## Feature-Beschreibung

### Kernfunktionalitäten
1. **Message Sending**
   - Direkte Nachrichten aus Customer Card
   - Template-basierte Nachrichten
   - Bulk-Messaging für Kampagnen
   - Rich Media Support (Bilder, PDFs)

2. **Message Receiving**
   - Webhook für eingehende Nachrichten
   - Automatische Zuordnung zu Kunden
   - Notification System
   - Conversation Threading

3. **Template Management**
   - Vordefinierte Message Templates
   - Variablen-Unterstützung
   - Multi-Language Support
   - Approval Workflow

4. **Activity Integration**
   - Automatische Activity-Erstellung
   - Conversation History
   - Analytics & Reporting
   - DSGVO-konforme Archivierung

## Technische Architektur

### Frontend-Komponenten
```
components/
├── whatsapp/
│   ├── WhatsAppButton.tsx
│   ├── MessageComposer.tsx
│   ├── ConversationView.tsx
│   ├── TemplateSelector.tsx
│   └── MessageStatus.tsx
├── modals/
│   └── WhatsAppChatModal.tsx
└── providers/
    └── WhatsAppProvider.tsx
```

### Backend-Services
```
services/
├── WhatsAppService
│   ├── MessageSender
│   ├── WebhookHandler
│   ├── TemplateManager
│   └── MediaHandler
├── integration/
│   ├── MetaAPIClient
│   └── WebhookVerifier
└── storage/
    └── MessageArchiver
```

### Datenmodell
```sql
-- WhatsApp Messages
CREATE TABLE whatsapp_messages (
    id UUID PRIMARY KEY,
    customer_id UUID REFERENCES customers(id),
    phone_number VARCHAR(20),
    direction VARCHAR(10), -- 'inbound' | 'outbound'
    message_id VARCHAR(100), -- WhatsApp Message ID
    conversation_id VARCHAR(100),
    content TEXT,
    media_url TEXT,
    media_type VARCHAR(50),
    template_id UUID REFERENCES whatsapp_templates(id),
    status VARCHAR(20), -- 'sent', 'delivered', 'read', 'failed'
    error_message TEXT,
    created_at TIMESTAMP,
    delivered_at TIMESTAMP,
    read_at TIMESTAMP
);

-- WhatsApp Templates
CREATE TABLE whatsapp_templates (
    id UUID PRIMARY KEY,
    name VARCHAR(100),
    category VARCHAR(50),
    language VARCHAR(10),
    header_text TEXT,
    body_text TEXT,
    footer_text TEXT,
    variables JSONB,
    buttons JSONB,
    approval_status VARCHAR(20),
    meta_template_id VARCHAR(100),
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMP
);

-- WhatsApp Opt-ins
CREATE TABLE whatsapp_optins (
    customer_id UUID PRIMARY KEY REFERENCES customers(id),
    phone_number VARCHAR(20),
    opted_in BOOLEAN DEFAULT FALSE,
    opted_in_at TIMESTAMP,
    opted_out_at TIMESTAMP,
    consent_method VARCHAR(50),
    consent_text TEXT
);
```

## Implementation Details

### Message Sender
```java
@ApplicationScoped
public class WhatsAppService {
    
    @ConfigProperty(name = "whatsapp.api.token")
    String apiToken;
    
    @ConfigProperty(name = "whatsapp.phone.id")
    String phoneId;
    
    private final WebClient client;
    
    public Uni<MessageResponse> sendTextMessage(
        String recipientPhone,
        String message,
        UUID customerId
    ) {
        // Validate opt-in status
        return validateOptIn(recipientPhone)
            .flatMap(valid -> {
                if (!valid) {
                    throw new OptInRequiredException();
                }
                
                var payload = Json.createObjectBuilder()
                    .add("messaging_product", "whatsapp")
                    .add("to", recipientPhone)
                    .add("type", "text")
                    .add("text", Json.createObjectBuilder()
                        .add("body", message))
                    .build();
                
                return sendToAPI(payload);
            })
            .flatMap(response -> saveMessage(response, customerId));
    }
    
    public Uni<MessageResponse> sendTemplate(
        String recipientPhone,
        UUID templateId,
        Map<String, String> variables
    ) {
        // Template-based messaging
    }
}
```

### Webhook Handler
```java
@Path("/webhooks/whatsapp")
public class WhatsAppWebhookResource {
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response handleWebhook(
        @HeaderParam("X-Hub-Signature-256") String signature,
        JsonObject payload
    ) {
        // Verify webhook signature
        if (!verifySignature(signature, payload)) {
            return Response.status(401).build();
        }
        
        // Process different event types
        var entry = payload.getJsonArray("entry");
        for (var e : entry) {
            processEntry(e.asJsonObject());
        }
        
        return Response.ok().build();
    }
    
    private void processEntry(JsonObject entry) {
        var changes = entry.getJsonArray("changes");
        for (var change : changes) {
            var value = change.asJsonObject().getJsonObject("value");
            
            if (value.containsKey("messages")) {
                processIncomingMessage(value);
            } else if (value.containsKey("statuses")) {
                processStatusUpdate(value);
            }
        }
    }
}
```

### Template Manager
```java
@ApplicationScoped
public class TemplateManager {
    
    public Uni<Template> createTemplate(CreateTemplateRequest request) {
        // Validate template format
        validateTemplate(request);
        
        // Submit to Meta for approval
        return submitToMeta(request)
            .flatMap(metaResponse -> saveTemplate(request, metaResponse));
    }
    
    public Uni<List<Variable>> extractVariables(String templateText) {
        // Extract {{variables}} from template
        Pattern pattern = Pattern.compile("\\{\\{(\\w+)\\}\\}");
        Matcher matcher = pattern.matcher(templateText);
        
        List<Variable> variables = new ArrayList<>();
        while (matcher.find()) {
            variables.add(new Variable(matcher.group(1)));
        }
        
        return Uni.createFrom().item(variables);
    }
}
```

## API Endpoints

```java
// WhatsApp API Endpoints
POST   /api/whatsapp/send          // Send message
POST   /api/whatsapp/send-template // Send template message
GET    /api/whatsapp/conversations/:customerId
GET    /api/whatsapp/templates
POST   /api/whatsapp/templates
PUT    /api/whatsapp/opt-in/:customerId
DELETE /api/whatsapp/opt-in/:customerId

// Webhook
POST   /webhooks/whatsapp         // Meta webhook endpoint
GET    /webhooks/whatsapp         // Webhook verification
```

## Security Considerations

1. **Authentication**:
   - Bearer Token für Meta API
   - Webhook Signature Verification
   - Rate Limiting pro Phone Number

2. **Data Privacy**:
   - End-to-End Encryption
   - Message Retention Policy (90 Tage)
   - DSGVO-konforme Opt-in/Opt-out
   - Keine Speicherung von Media-Content

3. **Compliance**:
   - WhatsApp Business Policy
   - Template Pre-Approval
   - 24-Hour Window für Antworten
   - No Promotional Content ohne Opt-in

## Performance Optimierung

1. **Message Queue**:
   - Async Message Processing
   - Retry Logic für Failed Messages
   - Bulk Send Optimization

2. **Caching**:
   - Template Cache (1 Stunde)
   - Customer Phone Mapping
   - Opt-in Status Cache

3. **Rate Limiting**:
   - 1000 Messages/Sekunde (Tier 1)
   - Automatic Backoff
   - Priority Queue für wichtige Messages

## Feature Flags

```java
@ConfigProperty(name = "feature.whatsapp.enabled", defaultValue = "false")
boolean whatsappEnabled;

@ConfigProperty(name = "feature.whatsapp.templates", defaultValue = "true")
boolean templatesEnabled;

@ConfigProperty(name = "feature.whatsapp.media", defaultValue = "false")
boolean mediaEnabled;
```

## Testing-Strategie

### Unit Tests
- Message Formatter
- Template Variable Resolution
- Webhook Signature Verification

### Integration Tests
- Meta API Mock
- Webhook Processing
- Activity Creation

### E2E Tests
- Complete Message Flow
- Template Management
- Conversation View

## Rollout-Plan

1. **Phase 1**: Basic Text Messages (0.5 Tage)
2. **Phase 2**: Template Support (0.5 Tage)
3. **Phase 3**: Media Messages (optional)
4. **Phase 4**: Advanced Features (optional)

## Meta Business Platform Setup

1. **Business Verification**:
   - Business Manager Account
   - Business Verification (2-3 Tage)
   - WhatsApp Business Account

2. **API Access**:
   - App Review für whatsapp_business_messaging
   - Webhook Setup
   - Test Phone Numbers

3. **Production Access**:
   - Display Name Approval
   - Business Use Case Verification
   - Rate Limit Increase Request

## Kosten

- **API Kosten**: ~0.03-0.08€ pro Nachricht (je nach Land)
- **Conversation Charges**: 
  - User-Initiated: 0.00€ (24h Window)
  - Business-Initiated: 0.05-0.15€
- **Monatliche Basis**: 0€ (nur Usage-based)

## Offene Fragen

1. Welche Message Templates initial?
2. Media Support (Bilder/PDFs) nötig?
3. Gruppen-Nachrichten Support?
4. Integration mit bestehendem Notification System?
5. Archivierungs-Strategie für Compliance?

## Referenzen

- [WhatsApp Business API Docs](https://developers.facebook.com/docs/whatsapp)
- [Meta Graph API](https://developers.facebook.com/docs/graph-api)
- [WhatsApp Pricing](https://developers.facebook.com/docs/whatsapp/pricing)
- [Business Policy](https://www.whatsapp.com/legal/business-policy)

## Verwandte Features

- [FC-014 Activity Timeline](/docs/features/PLANNED/16_activity_timeline/FC-014_TECH_CONCEPT.md) - Für Message-History
- [FC-012 Team Communication](/docs/features/PLANNED/14_team_communication/FC-012_TECH_CONCEPT.md) - Für interne Notifications
- [FC-031 Smart Templates](/docs/features/PLANNED/31_smart_templates/FC-031_TECH_CONCEPT.md) - Für Message-Templates