# 🎯 System-Entscheidungen basierend auf KI-Empfehlungen

**📊 Dokument-Typ:** Strategic Decision Documentation
**🎯 Zweck:** KI-Empfehlungen würdigen und Jörgs Zusatzanforderungen integrieren
**📅 Datum:** 2025-09-18
**🔗 Basis:** KI-Architekturdiskussion + Jörgs Ergänzungen

---

## ✅ **ZUSTIMMUNG ZU KI-EMPFEHLUNGEN**

### **1. Lead-Entity als separate Entity - JA ✅**
**Begründung bestätigt:** Handelsvertretervertrag erzwingt eigene Zustandslogik + Fristen + Stop-the-Clock. Separate Entity macht das auditierbar und testbar.

**Entscheidung:** `Lead` als eigene Entity mit State-Machine implementieren.

### **2. Handelsvertretervertrag-Compliance sofort - JA ✅**
**6-Monats-Schutz + 60-Tage-Check + Nachfrist + Stop-the-Clock** sind kernkritisch und vertragsrelevant.

**Entscheidung:** Vollständige Vertrags-Compliance in V1, keine Kompromisse.

### **3. E-Mail-Integration phasenweise - JA ✅**
**Phase 1:** Vorhandene `/08_administration/integration/email-service/` nutzen
**Phase 3:** Buy-Option evaluieren (SendGrid/Mailgun für Outbound-Campaigns)

**Entscheidung:** MVP mit bestehender Integration, Provider-Entscheidung später.

### **4. Routing-Minimalumfang für V1 - JA ✅**
**Felder + Badge + manueller Eskalations-CTA** reichen für V1.

**Entscheidung:** Automatisierung in V2/V3, V1 fokussiert auf Core-Funktionalität.

### **5. Campaign-Scope Basic-Engine - JA ✅**
**Queue + Template + Schedule** ohne Journeys/A/B-Testing.

**Entscheidung:** Attribution minimal (utm), Advanced-Features später.

### **6. Cockpit-Smart-Updates selektiv - JA ✅**
**Echtzeit:** `lead.created`/`mention.created`
**30s Intervall:** KPIs/Listen

**Entscheidung:** Passt zu Cockpit-Performance-Paradigma.

---

## 🆕 **JÖRGS ZUSATZANFORDERUNGEN INTEGRIERT**

### **A) Benutzerspezifische Provisionen/Lead-Zeiträume**

**System-Impact:**
```typescript
// Erweiterte Lead-Entity für benutzerspezifische Konfiguration
interface Lead {
  // ... bestehende Felder

  // Benutzerspezifische Überschreibungen
  customProtectionPeriod?: Duration;     // Überschreibt Standard 6 Monate
  customActivityCheckDays?: number;      // Überschreibt Standard 60 Tage
  customGracePeriodDays?: number;        // Überschreibt Standard 10 Tage

  assignedUserId: string;                // Referenz für benutzerspezifische Einstellungen
}

// Neue User-Settings-Entity
interface UserLeadSettings {
  userId: string;

  // Lead-Zeiträume pro Benutzer
  defaultProtectionMonths: number;       // Standard: 6, konfigurierbar
  defaultActivityCheckDays: number;      // Standard: 60, konfigurierbar
  defaultGracePeriodDays: number;        // Standard: 10, konfigurierbar

  // Provisions-Settings pro Benutzer
  firstYearCommissionRate: number;       // Standard: 7%, konfigurierbar
  followupYearCommissionRate: number;    // Standard: 2%, konfigurierbar
  monthlyAdvanceAmount: number;          // Standard: 2000€, konfigurierbar
}
```

**Business-Logic-Anpassungen:**
- Lead-State-Machine berücksichtigt benutzerspezifische Zeiträume
- Provisions-Berechnung nutzt User-Settings statt Konstanten
- Admin-UI für Lead-Settings-Management

### **B) all.inkl als Mail-Provider**

**Provider-Spezifikation:**
```yaml
Mail-Provider: all.inkl
Zugang: SMTP/IMAP (Standard)
Integration-Typ: Build-basiert über bestehende email-service-Struktur

Technische Details:
  SMTP-Server: all.inkl SMTP-Konfiguration
  IMAP-Access: Für E-Mail-Empfang und Threading
  Authentication: Standard SMTP-Auth
  TLS/SSL: Ja (erforderlich)

Integration mit /08_administration/integration/email-service/:
  - SMTP-Konfiguration für all.inkl
  - IMAP-Polling für Inbound-E-Mails
  - Bestehende Parser-Struktur nutzen
```

**Kein Provider-Lock-in:** all.inkl nutzt Standard-Protokolle, Migration zu anderen Providern jederzeit möglich.

---

## 🎯 **AKTUALISIERTE SYSTEM-ENTSCHEIDUNGEN**

### **1. Lead-Entity-Design (FINAL)**
```java
@Entity
public class Lead {
    // Vertrags-Pflichtfelder
    private String company;
    private String location;
    private String centralContact;

    // State-Machine
    private LeadStatus status;
    private LocalDateTime protectedUntil;
    private LocalDateTime lastActivityAt;
    private LocalDateTime nextCheckDueAt;

    // Benutzerspezifische Überschreibungen (NEU)
    private Integer customProtectionDays;
    private Integer customActivityCheckDays;
    private Integer customGracePeriodDays;

    // Zuordnung
    private String assignedUserId;

    // Channel-Management
    private ChannelType channelType;
    private String territory;
    private ConflictStatus conflictStatus;
}
```

### **2. User-Settings-System (NEU)**
```java
@Entity
public class UserLeadSettings {
    private String userId;

    // Lead-Zeiträume
    private int defaultProtectionMonths = 6;
    private int defaultActivityCheckDays = 60;
    private int defaultGracePeriodDays = 10;

    // Provisions-Konfiguration
    private BigDecimal firstYearCommissionRate = new BigDecimal("0.07");
    private BigDecimal followupYearCommissionRate = new BigDecimal("0.02");
    private BigDecimal monthlyAdvanceAmount = new BigDecimal("2000.00");
}
```

### **3. E-Mail-Provider-Integration (FINAL)**
```yaml
Phase 1 (V1): all.inkl via bestehende email-service-Struktur
  - SMTP/IMAP-Integration
  - Standard-Protokolle, kein Lock-in
  - Inbound-Parsing für Lead-Aktivitäten

Phase 3 (V3): Outbound-Campaign-Provider evaluieren
  - SendGrid/Mailgun für Marketing-Campaigns
  - all.inkl bleibt für Inbound + Basic-Outbound
```

---

## 🚀 **AKTUALISIERTE 10-TAGE-ROADMAP**

### **Tag 1-2: Foundation + User-Settings**
- Lead-Entity + State-Machine implementation
- **UserLeadSettings-Entity** (NEU)
- **Admin-UI für Lead-Settings** (NEU)

### **Tag 3-4: E-Mail-Integration mit all.inkl**
- **all.inkl SMTP/IMAP-Konfiguration** in email-service
- Inbound-Parser + Matching-Heuristik
- Triage-UI Skeleton

### **Tag 5-6: Benutzerspezifische Business-Logic**
- **Lead-Zeiträume pro User** in State-Machine
- **Provisions-Berechnung pro User**
- Scheduler-Jobs mit User-Settings

### **Tag 7-8: Routing + Conflict-Management**
- Channel-Type + Territory-Fields
- Conflict-Status + Manual-Escalation-CTA
- Filter/Badges in Fokusliste

### **Tag 9-10: Integration + Testing**
- Lead→Customer-Konvertierung
- **Benutzerspezifische Provisions-Berechnung** (NEU)
- UAT mit all.inkl-Integration

---

## 📋 **ANTWORTEN AUF KI-FRAGEN**

### **1. Lead-Entity separat?** ✅ **JA - bestätigt**

### **2. Handelsvertretervertrag-Compliance sofort?** ✅ **JA - V1 kernkritisch**

### **3. E-Mail-Provider MVP mit bestehender Integration?** ✅ **JA - all.inkl via email-service**

### **4. Routing-Minimalumfang V1?** ✅ **JA - Felder + Badge + CTA reichen**

### **5. Campaign-Scope Basic-Engine?** ✅ **JA - ohne Journeys/A/B**

### **6. Cockpit-Smart-Updates selektiv?** ✅ **JA - Echtzeit nur für kritische Events**

---

## 🆕 **ZUSÄTZLICHE ANFORDERUNGEN FÜR API-CONTRACTS**

### **User-Settings-APIs (NEU):**
```typescript
// User-Lead-Settings-Management
GET /users/{userId}/lead-settings     // Aktuelle Einstellungen
PUT /users/{userId}/lead-settings     // Einstellungen ändern

// Lead-Creation mit User-Settings
POST /leads                           // Berücksichtigt User-Settings automatisch

// Provisions-Calculation mit User-Settings
GET /leads/{leadId}/commission-preview // Berechnung mit User-Rates
POST /leads/{leadId}/convert           // Conversion mit korrekten Rates
```

### **all.inkl E-Mail-Integration:**
```typescript
// E-Mail-Provider-spezifische Endpoints
POST /email/all-inkl/configure        // SMTP/IMAP-Setup
GET /email/all-inkl/status            // Connection-Health
POST /email/inbound                   // Parser für all.inkl-Mails
```

---

## 🎯 **NÄCHSTE SCHRITTE**

### **Sofort benötigt:**
1. **API-Contracts** für alle Endpoints inkl. User-Settings
2. **Database-Schema** für Lead + UserLeadSettings
3. **all.inkl Integration-Guide** mit SMTP/IMAP-Details

### **Integration-Punkte:**
- **08_administration/integration/email-service/** für all.inkl
- **03_kundenmanagement** für Lead→Customer-Konvertierung
- **01_mein-cockpit** für Smart-Updates + Badges

---

**📝 Die KI-Empfehlungen sind excellent und bilden eine solide Basis. Mit den Zusatzanforderungen (User-Settings + all.inkl) haben wir ein vollständiges System-Design!**

**🚀 Ready für API-Contracts + Technical Concepts!**