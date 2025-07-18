# 💬 FC-028 WHATSAPP BUSINESS INTEGRATION (KOMPAKT)

**Erstellt:** 18.07.2025  
**Status:** 📋 READY TO START  
**Feature-Typ:** 🔀 FULLSTACK  
**Priorität:** HIGH - Kundenkommunikation  
**Geschätzt:** 1 Tag  

---

## 🧠 WAS WIR BAUEN

**Problem:** Kunden antworten auf WhatsApp, nicht E-Mail  
**Lösung:** Native WhatsApp Business Integration  
**Value:** Dort sein, wo Kunden sind  

> **Business Case:** 80% schnellere Antwortzeiten, 90% Öffnungsrate

### 🎯 Core Features:
- **Direct Messaging:** Aus Customer Card
- **Template Messages:** Angebote, Termine
- **Media Sharing:** PDFs, Bilder
- **Status Tracking:** Gelesen/Zugestellt

---

## 🚀 SOFORT LOSLEGEN (15 MIN)

### 1. **WhatsApp Business API Setup:**
```bash
# Meta Business Account needed
# https://business.facebook.com/
# WhatsApp Business API Access
```

### 2. **Backend Service:**
```java
@ApplicationScoped
public class WhatsAppService {
    
    @ConfigProperty(name = "whatsapp.api.token")
    String apiToken;
    
    public void sendMessage(String to, String message) {
        // WhatsApp Cloud API
    }
}
```

### 3. **Frontend Component:**
```typescript
<WhatsAppButton
  customer={customer}
  onClick={() => openWhatsAppDialog()}
/>
```

---

## 💬 MESSAGE TEMPLATES

### Angebots-Template:
```typescript
const templates = {
  offer: `Hallo {name} 👋

wie besprochen hier Ihr Angebot:
📎 {filename}

✅ Highlights:
{highlights}

Haben Sie noch Fragen?

Beste Grüße
{salesperson}`,

  appointment: `Hallo {name} 👋

Terminbestätigung:
📅 {date}
🕐 {time}
📍 {location}

Bis dann! 😊`,

  followUp: `Hallo {name} 👋

wollte nur kurz nachfragen, ob Sie 
unser Angebot erhalten haben?

Gerne bin ich für Fragen da!`
};
```

---

## 🎨 UI INTEGRATION

### Customer Card Enhancement:
```typescript
<CustomerActions>
  <IconButton onClick={call}>
    <PhoneIcon />
  </IconButton>
  
  <IconButton onClick={email}>
    <EmailIcon />
  </IconButton>
  
  <IconButton onClick={whatsapp} color="success">
    <WhatsAppIcon />
  </IconButton>
</CustomerActions>
```

### WhatsApp Dialog:
```
┌─────────────────────────────────────┐
│ 💬 WhatsApp an Müller GmbH         │
├─────────────────────────────────────┤
│                                     │
│ Vorlage: [Angebot ▼]               │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ Hallo Herr Müller 👋            │ │
│ │                                 │ │
│ │ wie besprochen hier Ihr        │ │
│ │ Angebot:                       │ │
│ │ 📎 Angebot_Müller_2025.pdf     │ │
│ │                                 │ │
│ │ ✅ Highlights:                  │ │
│ │ • Kombi-Dämpfer 20% Rabatt     │ │
│ │ • Kostenlose Schulung          │ │
│ │                                 │ │
│ └─────────────────────────────────┘ │
│                                     │
│ [📎 Datei anhängen]                 │
│                                     │
│              [Abbrechen] [📤 Senden]│
└─────────────────────────────────────┘
```

---

## 🔧 BACKEND IMPLEMENTATION

### WhatsApp Cloud API:
```java
@Path("/api/whatsapp")
@Authenticated
public class WhatsAppResource {
    
    @POST
    @Path("/send")
    public Response sendMessage(
        WhatsAppMessageDTO message
    ) {
        // 1. Validate phone number
        String formattedPhone = formatPhone(
            message.to()
        );
        
        // 2. Send via API
        var response = whatsAppClient
            .messages()
            .send(MessageRequest.builder()
                .to(formattedPhone)
                .type("text")
                .text(TextObject.builder()
                    .body(message.text())
                    .build())
                .build());
        
        // 3. Log activity
        activityService.log(
            message.customerId(),
            ActivityType.WHATSAPP_SENT,
            message.text()
        );
        
        return Response.ok(response).build();
    }
}
```

### Media Messages:
```java
public void sendDocument(
    String to, 
    String documentUrl,
    String caption
) {
    whatsAppClient.messages().send(
        MessageRequest.builder()
            .to(to)
            .type("document")
            .document(MediaObject.builder()
                .link(documentUrl)
                .caption(caption)
                .filename("Angebot.pdf")
                .build())
            .build()
    );
}
```

---

## 📊 ACTIVITY TRACKING

```typescript
// Automatisch in Timeline
interface WhatsAppActivity {
  type: 'WHATSAPP_SENT' | 'WHATSAPP_RECEIVED';
  timestamp: Date;
  message: string;
  status: 'sent' | 'delivered' | 'read';
  media?: {
    type: 'document' | 'image';
    filename: string;
  };
}

// Timeline zeigt WhatsApp mit Logo
<TimelineItem icon={<WhatsAppIcon />}>
  WhatsApp gesendet: "Angebot verschickt"
  ✓✓ Gelesen um 14:32
</TimelineItem>
```

---

## 🔒 COMPLIANCE

### Opt-In Management:
```java
// DSGVO-konform
@Entity
public class WhatsAppConsent {
    UUID customerId;
    boolean consentGiven;
    LocalDateTime consentDate;
    String consentText;
}

// Check before sending
if (!hasWhatsAppConsent(customerId)) {
    throw new NoConsentException();
}
```

---

## 📞 NÄCHSTE SCHRITTE

1. **Meta Business Account** erstellen
2. **WhatsApp API Access** beantragen
3. **Message Templates** definieren
4. **Backend Service** implementieren
5. **UI Components** bauen
6. **Consent Management** DSGVO

**WICHTIG:** Business Verification dauert 2-3 Tage!