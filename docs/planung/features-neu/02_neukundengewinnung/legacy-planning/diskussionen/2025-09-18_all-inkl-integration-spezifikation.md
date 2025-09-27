# 📧 all.inkl Integration - Detaillierte Spezifikation

**📊 Dokument-Typ:** Technical Integration Specification
**🎯 Zweck:** Konkrete all.inkl-Integration für E-Mail-Posteingang definieren
**📅 Datum:** 2025-09-18
**🔗 Basis:** all.inkl KAS API + Standard E-Mail-Protokolle

---

## 🎯 **INTEGRATION-ÜBERSICHT**

### **Zwei-Stufen-Ansatz:**
1. **Standard E-Mail-Protokolle:** IMAP/SMTP für E-Mail-Handling
2. **KAS API (optional):** Für Account-Management und Automatisierung

---

## 📧 **STUFE 1: Standard E-Mail-Integration (Kern-Feature)**

### **Server-Konfiguration:**
```yaml
IMAP-Server:
  Host: "w1234567.kasserver.com"  # Servername aus KAS
  Port: 993 (SSL/TLS)
  Encryption: SSL/TLS (required)

SMTP-Server:
  Host: "w1234567.kasserver.com"  # Gleicher Server
  Port: 465 (SSL) oder 587 (STARTTLS)
  Encryption: SSL/TLS (required)

Authentication:
  Username: "vollständige E-Mail-Adresse"
  Password: "E-Mail-Passwort aus KAS"
```

### **Technische Implementation:**
```java
// E-Mail-Account-Konfiguration für all.inkl
@Entity
public class AllInklEmailAccount {
    private String serverName;           // z.B. "w1234567.kasserver.com"
    private String emailAddress;         // vollständige E-Mail-Adresse
    private String passwordSecret;       // Vault-Referenz für Passwort

    // Standard IMAP/SMTP-Konfiguration
    private int imapPort = 993;
    private int smtpPort = 465;
    private boolean sslRequired = true;
}
```

### **E-Mail-Polling-Strategie:**
```yaml
IMAP-Integration:
  Method: IMAP IDLE (Push-ähnlich) + Fallback-Polling
  Interval: 30 Sekunden bei IDLE-Failure
  Folders: INBOX, Sent, wichtige Custom-Folders

Performance:
  Concurrent-Connections: Max 2-3 pro Account
  Batch-Size: 50 E-Mails pro Fetch
  Delta-Sync: Nur neue/geänderte Messages
```

---

## 🔧 **STUFE 2: KAS API-Integration (Management/Automation)**

### **API-Zugang:**
```yaml
KAS API:
  Type: SOAP-based API
  Endpoint: "https://kas.all-inkl.com/soap/kas.php"
  Authentication: KAS-Login + KAS-Passwort (nicht E-Mail-Passwort!)
  Documentation: https://kas.all-inkl.com/schnittstelle/dokumentation/
```

### **Automatisierung-Möglichkeiten:**
```yaml
Account-Management via KAS API:
  ✅ E-Mail-Konten automatisch anlegen
  ✅ Passwörter programmatisch ändern
  ✅ Quota-Überwachung
  ✅ Weiterleitungen konfigurieren
  ✅ Autoresponder setup

Domain-Management:
  ✅ DNS-Records verwalten
  ✅ Subdomain-Setup für E-Mail
  ✅ SSL-Zertifikat-Status prüfen
```

### **Java-Integration (KAS API):**
```java
// KAS API Client für Account-Management
@Service
public class AllInklKasApiService {

    private final KasApiClient kasClient;

    public AllInklEmailAccountInfo createEmailAccount(String emailAddress, String password) {
        // KAS API Call für E-Mail-Account-Erstellung
        KasEmailAccount account = kasClient.createEmailAccount(
            emailAddress,
            password,
            quotaMB: 1000
        );
        return mapToAccountInfo(account);
    }

    public void updateEmailPassword(String emailAddress, String newPassword) {
        kasClient.updateEmailPassword(emailAddress, newPassword);
    }

    public EmailQuotaInfo getQuotaInfo(String emailAddress) {
        return kasClient.getEmailQuota(emailAddress);
    }
}
```

---

## 📋 **PRAKTISCHE IMPLEMENTATION**

### **Phase 1: Basic E-Mail-Integration (Woche 1-3)**
```yaml
Setup:
  1. KAS-Account einrichten + E-Mail-Postfach anlegen
  2. Server-Name notieren (w1234567.kasserver.com)
  3. IMAP/SMTP-Credentials im Vault speichern

Code:
  1. Standard Java Mail API für IMAP/SMTP
  2. Connection-Pool für IMAP-Verbindungen
  3. E-Mail-Parser für RFC822-Messages
  4. Threading-Logic über Message-ID/In-Reply-To
```

### **Phase 2: KAS API-Integration (Woche 4-6)**
```yaml
Setup:
  1. KAS API-Credentials (Login/Passwort) beschaffen
  2. SOAP-Client-Library integrieren
  3. API-Wrapper-Service implementieren

Features:
  1. Automatische Account-Erstellung
  2. Quota-Monitoring
  3. Passwort-Management
  4. Health-Checks
```

### **Konfiguration in /08_administration/integration/email-service/**
```java
// Integration in bestehende E-Mail-Service-Struktur
@Component
public class AllInklEmailProvider implements EmailProvider {

    @Override
    public EmailConnection connect(EmailAccountConfig config) {
        return new AllInklImapConnection(
            config.getServerName(),
            config.getCredentials(),
            993, // IMAP SSL Port
            true  // SSL required
        );
    }

    @Override
    public void sendEmail(EmailMessage message, EmailAccountConfig config) {
        AllInklSmtpSender sender = new AllInklSmtpSender(
            config.getServerName(),
            465  // SMTP SSL Port
        );
        sender.send(message);
    }
}
```

---

## 🚨 **RATE-LIMITS & BESCHRÄNKUNGEN**

### **E-Mail-Limits (geschätzt - zu klären):**
```yaml
IMAP:
  Concurrent-Connections: ~5-10 pro Account
  Request-Rate: Keine offizielle Begrenzung
  Empfehlung: Max 3 parallele Connections

SMTP:
  Sending-Rate: ~100-500 E-Mails/Stunde (zu verifizieren)
  Size-Limit: 50MB pro E-Mail (inkl. Attachments)
  Empfehlung: Queue-basiertes Sending mit Delays
```

### **KAS API-Limits:**
```yaml
API-Calls:
  Rate-Limit: ~1000 Calls/Tag (zu verifizieren)
  Concurrent: Max 3-5 parallele Requests
  Retry-Strategy: Exponential Backoff bei 429/500
```

---

## 🔐 **SECURITY & CREDENTIALS**

### **Credential-Management:**
```yaml
E-Mail-Credentials:
  Storage: HashiCorp Vault oder Kubernetes Secrets
  Format: "vault://all-inkl/email/{account-id}"
  Rotation: Manuell via KAS-Interface oder API

KAS API-Credentials:
  Storage: Separates Vault-Secret
  Format: "vault://all-inkl/kas/api-credentials"
  Scope: Read/Write für E-Mail-Management
```

### **SSL/TLS-Konfiguration:**
```java
// IMAP SSL-Konfiguration
Properties imapProps = new Properties();
imapProps.put("mail.store.protocol", "imaps");
imapProps.put("mail.imaps.host", serverName);
imapProps.put("mail.imaps.port", "993");
imapProps.put("mail.imaps.ssl.enable", "true");
imapProps.put("mail.imaps.ssl.trust", "*");  // Für all.inkl-Zertifikate
```

---

## 📊 **MONITORING & HEALTH-CHECKS**

### **IMAP/SMTP-Monitoring:**
```yaml
Health-Checks:
  - IMAP-Connection-Test (alle 5 Minuten)
  - SMTP-Send-Test (alle 15 Minuten)
  - Quota-Überwachung (täglich)
  - SSL-Zertifikat-Gültigkeit (wöchentlich)

Alerts:
  - Connection-Failures > 3 in 15 Minuten
  - Quota > 80% voll
  - Send-Failures > 10% in 1 Stunde
```

### **KAS API-Monitoring:**
```yaml
API-Health:
  - API-Endpoint-Verfügbarkeit
  - Response-Time-Monitoring
  - Error-Rate-Tracking
  - Credential-Validity-Check
```

---

## 🧪 **TESTING-STRATEGIE**

### **E-Mail-Integration-Tests:**
```java
@Test
public void testAllInklImapConnection() {
    // Test IMAP-Verbindung mit echten Credentials
    AllInklEmailAccount account = createTestAccount();

    assertThat(imapService.connect(account))
        .isNotNull()
        .satisfies(connection -> {
            assertThat(connection.getFolders()).contains("INBOX");
            assertThat(connection.getMessageCount("INBOX")).isGreaterThanOrEqualTo(0);
        });
}

@Test
public void testEmailThreading() {
    // Test Thread-Erkennung mit all.inkl-E-Mails
    List<EmailMessage> testMessages = loadTestEmailsFromAllInkl();

    EmailThreadBuilder threadBuilder = new EmailThreadBuilder();
    List<EmailThread> threads = threadBuilder.buildThreads(testMessages);

    assertThat(threads).hasSize(expectedThreadCount);
}
```

---

## 📋 **OFFENE PUNKTE ZU KLÄREN**

### **1. Rate-Limits verifizieren:**
- Echte SMTP-Sending-Limits testen
- IMAP-Connection-Limits validieren
- KAS API-Quotas erfragen

### **2. SSL-Zertifikate:**
- all.inkl-Zertifikat-Chain für SSL-Trust
- Zertifikat-Rotation-Handling

### **3. Account-Setup:**
- Optimale Postfach-Konfiguration
- Folder-Struktur für verschiedene Use-Cases
- Autoresponder/Weiterleitung-Best-Practices

---

## 🚀 **INTEGRATION IN BESTEHENDE ARCHITEKTUR**

### **Einbindung in /08_administration/integration/email-service/:**
```yaml
Directory-Structure:
/08_administration/integration/email-service/
├── providers/
│   ├── AllInklEmailProvider.java
│   ├── AllInklImapConnection.java
│   ├── AllInklSmtpSender.java
│   └── AllInklKasApiService.java
├── config/
│   └── AllInklEmailConfig.java
└── tests/
    └── AllInklIntegrationTest.java
```

### **Event-Integration:**
```yaml
E-Mail-Events für Neukundengewinnung:
  - email.received (von all.inkl IMAP)
  - email.sent (via all.inkl SMTP)
  - email.thread.updated
  - email.account.quota.warning
```

---

**📝 Mit diesen Details haben wir eine vollständige all.inkl-Integration-Roadmap! 🚀**

**Nächster Schritt:** KAS API-Credentials beschaffen und erste IMAP-Connection-Tests durchführen.