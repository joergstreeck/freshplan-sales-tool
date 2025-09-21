# 📋 Handelsvertretervertrag - Lead-Requirements für CRM-System

**📊 Dokument-Typ:** Business Requirements Analysis
**🎯 Zweck:** Systemrelevante Lead-Logik aus Handelsvertretervertrag extrahieren
**📅 Datum:** 2025-09-18
**🔗 Basis:** Handelsvertretervertrag.pdf (§2(8) Lead-Registrierung und Schutz)

---

## 🎯 **Executive Summary**

**Kritische Erkenntnis:** Der Handelsvertretervertrag definiert ein präzises Lead-Schutzsystem mit 6-Monats-Zyklen und 60-Tage-Aktivitätschecks, das direkt in unser CRM implementiert werden muss.

**System-Impact:**
- Lead-Entity benötigt Schutz-Status und Aktivitäts-Tracking
- Automatisierte Fristen-Überwachung erforderlich
- Integration mit E-Mail-System für Aktivitäts-Dokumentation
- Provisions-relevante Status-Transitions

---

## 📋 **Lead-Definition (§2(8)a)**

### **Grundlegende Lead-Struktur:**
```yaml
Lead-Registrierung erfordert:
  - Firma (Unternehmensname)
  - Ort (Standort/Adresse)
  - Zentraler Kontakt ODER dokumentierter Erstkontakt

Schutz-Zeitraum:
  - 6 Monate ab Registrierung
  - Automatische Verlängerung möglich (§2(8)e)
```

**System-Requirements:**
- Lead-Entity mit Pflichtfeldern: `company`, `location`, `centralContact`
- Alternative: `documentedFirstContact` wenn zentraler Kontakt nicht verfügbar
- Schutz-Status: `PROTECTED` (6 Monate), `EXPIRED`, `EXTENDED`

---

## ⏰ **Aktivitäts-Standard (§2(8)b-c)**

### **60-Tage-Aktivitätscheck:**
```yaml
Aktivitäts-Definition:
  - "Mindestens ein belegbarer Fortschritt je 60 Tage"

Belegbare Aktivitäten:
  - Qualifiziertes Gespräch
  - Kundenreaktion (E-Mail, Anruf)
  - Terminierte Folgeaktivität

Dokumentations-Anforderungen:
  - "Proaktives Monatsreporting ist nicht erforderlich"
  - "Kurzes Status-Update erfolgt auf Anforderung"
```

**System-Requirements:**
- Aktivitäts-Tracking mit Aktivitäts-Typen: `QUALIFIED_CALL`, `CUSTOMER_REACTION`, `SCHEDULED_FOLLOWUP`
- Automatischer 60-Tage-Timer pro Lead
- Integration mit E-Mail-System für automatische Aktivitäts-Erkennung

---

## 🚨 **Fristen-Management (§2(8)c)**

### **Erinnerungs- und Nachfrist-System:**
```yaml
Erinnerungsprozess:
  1. Nach 60 Tagen ohne Aktivität
  2. Freshfoodz fordert "kurzes Update" in Textform
  3. Nachfrist: 10 Kalendertage
  4. Bei fehlender Abhilfe: Lead-Schutz erlischt

Stop-the-Clock (§2(8)d):
  - Verzögerungen durch Freshfoodz-Zuarbeiten
  - Beispiele: Preisfreigaben, Muster, Unterlagen
  - Kundenseitige Sperrfristen
  - Schutzzeitraum verlängert sich entsprechend
```

**System-Requirements:**
- Automatische Reminder-Engine (60-Tage-Check)
- Nachfrist-Timer (10 Kalendertage)
- Stop-the-Clock-Funktion für Freshfoodz-bedingte Verzögerungen
- Status-Updates via E-Mail oder System-Interface

---

## 🔄 **Lead-Status-Lifecycle**

### **Lead-Zustandsmaschine:**
```yaml
Lead-Status-Flow:
  REGISTERED → ACTIVE → REMINDER_SENT → GRACE_PERIOD → EXPIRED/PROTECTED

Status-Definitionen:
  - REGISTERED: Lead eingegeben, 6-Monats-Schutz aktiv
  - ACTIVE: Regelmäßige Aktivität (< 60 Tage seit letzter Aktivität)
  - REMINDER_SENT: 60-Tage-Grenze erreicht, Update angefordert
  - GRACE_PERIOD: 10-Tage-Nachfrist läuft
  - EXPIRED: Schutz erloschen, Lead freigegeben
  - EXTENDED: Schutz verlängert (§2(8)e)
  - STOP_CLOCK: Fristen pausiert (Freshfoodz-Verzögerung)
```

**System-Requirements:**
- State Machine für Lead-Status
- Automatische Status-Transitions basierend auf Aktivität
- Manual Override für Stop-the-Clock-Szenarien

---

## 💰 **Provisions-Integration (§2(1-4))**

### **Lead-to-Customer-Konvertierung:**
```yaml
Provisions-relevante Events:
  - Schriftliche Kundenvereinbarung (Einzel- oder Rahmenvertrag)
  - Erster vereinnahmter Umsatz
  - Provisionsberechnung: 7% erstes Jahr, 2% Folgejahre

Neukunden-Definition:
  - "Kein Warenbezug bei Freshfoodz in letzten 12 Monaten"

Kettenkunden-Besonderheit:
  - Provisionsanspruch je Standort
  - Erst ab erstem Umsatz ODER nachweislichem Onboarding
```

**System-Requirements:**
- Lead-to-Customer-Konvertierung-Workflow
- Integration mit Umsatz-Tracking
- Automatische Provisions-Berechnung
- 12-Monats-Prüfung für Neukunden-Status

---

## 🔗 **Integration-Requirements**

### **E-Mail-System-Integration (mit 08_administration/integration):**
```yaml
Automatische Aktivitäts-Erkennung:
  - E-Mail-Parsing für Kunden-Reaktionen
  - Termin-Erkennung in E-Mail-Content
  - Gespräch-Notizen via E-Mail-Interface

E-Mail-basierte Reminder:
  - Automatische 60-Tage-Erinnerungen
  - 10-Tage-Nachfrist-Notification
  - Status-Update-Requests via E-Mail
```

### **Customer-Entity-Integration:**
```yaml
Lead-Customer-Pipeline:
  - Lead-Entity mit Customer-Referenz
  - Automatische Customer-Creation bei Vertragsabschluss
  - Historische Lead-Daten in Customer-Timeline

Status-Synchronisation:
  - Lead.CONVERTED → Customer.ACTIVE
  - Lead-Protection-History in Customer-Entity
```

---

## 📊 **Daten-Modell-Requirements**

### **Lead-Entity-Struktur:**
```java
@Entity
public class Lead {
    private UUID id;

    // Pflichtfelder aus Vertrag
    private String company;              // Firma
    private String location;             // Ort
    private String centralContact;       // Zentraler Kontakt
    private String documentedFirstContact; // Alternative

    // Schutz-System
    private LeadStatus status;           // ACTIVE, REMINDER_SENT, etc.
    private LocalDateTime registeredAt;  // Registrierungsdatum
    private LocalDateTime protectedUntil; // 6-Monats-Schutz
    private LocalDateTime lastActivity;  // Letzte Aktivität

    // Fristen-Management
    private LocalDateTime reminderSentAt; // 60-Tage-Reminder
    private LocalDateTime gracePeriodUntil; // 10-Tage-Nachfrist
    private boolean stopClockActive;     // Fristen pausiert
    private String stopClockReason;      // Grund für Pause

    // Aktivitäts-Tracking
    private List<LeadActivity> activities;

    // Customer-Integration
    private UUID customerId;             // Bei Konvertierung
    private LocalDateTime convertedAt;   // Vertragsabschluss
}
```

### **LeadActivity-Entity:**
```java
@Entity
public class LeadActivity {
    private UUID id;
    private UUID leadId;

    private ActivityType type;           // QUALIFIED_CALL, CUSTOMER_REACTION, etc.
    private LocalDateTime activityDate;
    private String description;
    private String documentationSource;  // EMAIL, MANUAL, SYSTEM

    // E-Mail-Integration
    private UUID emailId;                // Referenz zu E-Mail-Entity
    private boolean autoDetected;        // Automatisch erkannt
}
```

---

## 🔄 **Business-Logic-Requirements**

### **Automatisierungs-Rules:**
```yaml
Daily Batch-Jobs:
  - Lead-Status-Check (60-Tage-Aktivität)
  - Schutz-Ablauf-Check (6-Monats-Grenze)
  - Reminder-Versendung
  - Grace-Period-Überwachung

E-Mail-Integration-Rules:
  - Eingehende E-Mails → Lead-Aktivität-Check
  - Termin-Keywords → SCHEDULED_FOLLOWUP-Aktivität
  - Kunden-Antworten → CUSTOMER_REACTION-Aktivität

Business-Validation:
  - Duplicate-Lead-Prevention (Firma + Ort)
  - Neukunden-Check (12-Monats-Regel)
  - Provisions-Berechtigungs-Prüfung
```

---

## 🚀 **Implementation-Roadmap**

### **Phase 1: Core Lead-Entity (Woche 1-2)**
- Lead-Entity + Repository implementation
- Basic Lead-Status-Management
- Lead-Registrierung-UI

### **Phase 2: Fristen-Management (Woche 3-4)**
- 60-Tage-Aktivitäts-Check-Engine
- Reminder-System implementation
- Stop-the-Clock-Funktionalität

### **Phase 3: E-Mail-Integration (Woche 5-6)**
- Integration mit 08_administration/integration/email-service
- Automatische Aktivitäts-Erkennung
- E-Mail-basierte Status-Updates

### **Phase 4: Customer-Integration (Woche 7-8)**
- Lead-to-Customer-Konvertierung
- Provisions-Calculation-Engine
- Customer-Timeline-Integration

---

## 🎯 **Success-Kriterien**

### **Funktional:**
- ✅ 100% Lead-Schutz-Compliance mit Vertrag
- ✅ Automatische 60-Tage-Checks
- ✅ E-Mail-basierte Aktivitäts-Erkennung
- ✅ Seamless Lead-to-Customer-Pipeline

### **Non-Funktional:**
- ✅ <1s Response-Zeit für Lead-Status-Checks
- ✅ 99.9% Uptime für Fristen-Management
- ✅ DSGVO-compliant Data-Processing (§2(8)g-i)

---

## 📋 **Offene Fragen für Technical Concept:**

1. **E-Mail-Parsing-Complexity:** Wie sophisticated soll automatische Aktivitäts-Erkennung werden?
2. **Stop-the-Clock-Triggers:** Welche System-Events lösen automatisch Stop-the-Clock aus?
3. **Duplicate-Detection:** Wie handhaben wir ähnliche Firmennamen oder Standort-Variationen?
4. **Provisions-Integration:** Integration mit bestehender Customer-Entity oder separate Provisions-Module?

---

**🔗 Diese Requirements bilden die Basis für:**
- `lead-erfassung/technical-concept.md`
- `email-posteingang/technical-concept.md` (Aktivitäts-Integration)
- Integration mit 08_administration/integration/email-service

**📝 Nächster Schritt:** Update der Ausbau-Strategie-Diskussion mit diesen konkreten Business-Requirements!