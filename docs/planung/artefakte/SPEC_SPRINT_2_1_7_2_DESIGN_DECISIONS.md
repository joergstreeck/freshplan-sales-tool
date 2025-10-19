# Sprint 2.1.7.2 - Design Decisions

**Sprint-ID:** 2.1.7.2
**Created:** 2025-10-19
**Status:** 📋 FINAL
**Owner:** Claude + Jörg (User Validation)

---

## 📋 ÜBERSICHT

Dieses Dokument konsolidiert **alle Architektur- und Design-Entscheidungen** für Sprint 2.1.7.2.

**Hauptentscheidungen:**
1. Sales-Rep Mapping Strategie (Email-basiert vs User-Tabelle-Erweiterung)
2. Mock vs Real Development (Hybrid-Ansatz mit Feature-Flag)
3. Churn-Alarm Konfiguration (Pro Kunde vs Global)
4. Zahlungsverhalten-Ampel (Schwellwerte)
5. Status-Architektur Integration (Sprint 2.1.7.4)
6. Webhook vs Polling (Real-Time vs Batch)
7. Xentral-Verknüpfung (Optional vs Mandatory)

---

## 1️⃣ Sales-Rep Mapping Strategie ⭐

### **Problem**
Wie verknüpfen wir FreshPlan-User mit Xentral Sales-Reps?

**Optionen:**
1. **Option A: User-Tabelle erweitern** mit `xentral_sales_rep_id`
2. **Option B: Mapping-Tabelle** (user_id ↔ xentral_sales_rep_id)
3. **Option C: Hardcoded Mapping** in application.properties

### **Lösung**
**Option A: User-Tabelle erweitern** ✅

**User-Entscheidung (2025-10-18):**
> "User-Tabelle erweitern mit `xentral_sales_rep_id` + Auto-Sync-Job"

**Implementierung:**
```sql
-- Migration V10031
ALTER TABLE users
ADD COLUMN xentral_sales_rep_id VARCHAR(50) DEFAULT NULL;

COMMENT ON COLUMN users.xentral_sales_rep_id IS 'Xentral Sales Rep ID for API filtering';
```

**Auto-Sync-Job:**
```java
@Scheduled(cron = "0 0 2 * * ?") // Daily 2:00 AM
public void syncSalesRepIds() {
    List<XentralSalesRepDTO> salesReps = xentralApiService.getAllSalesReps();

    for (XentralSalesRepDTO salesRep : salesReps) {
        // Match by Email
        Optional<User> user = userRepository.findByEmail(salesRep.email());
        if (user.isPresent()) {
            user.get().setXentralSalesRepId(salesRep.id());
        }
    }
}
```

### **Begründung**
- ✅ **Einfach:** Keine zusätzliche Tabelle nötig
- ✅ **Performant:** 1:1 Relationship (User ↔ Sales-Rep)
- ✅ **Wartbar:** Admin kann in `/admin/users` manuell nachpflegen
- ✅ **Auto-Sync:** Email-basiertes Matching täglich

### **Alternativen (verworfen)**
❌ **Mapping-Tabelle:** Over-Engineering für 1:1 Relationship
❌ **Hardcoded:** Nicht wartbar, nicht skalierbar

---

## 2️⃣ Mock vs Real Development - Hybrid-Ansatz 🔧

### **Problem**
Xentral-API ist noch nicht fertig getestet - wie bauen wir trotzdem?

**Optionen:**
1. **Option A: NUR Mock** - Warten auf IT-Response (Blocker!)
2. **Option B: NUR Real API** - Kann nicht ohne IT starten
3. **Option C: Hybrid-Ansatz** - Foundation mit Mocks, später Switch

### **Lösung**
**Option C: Hybrid-Ansatz mit Feature-Flag** ✅

**User-Entscheidung (2025-10-18):**
> "Mock-Enabled Development mit Feature-Flag - später Switch auf echte API"

**Implementierung:**
```java
@ApplicationScoped
public class XentralApiService {

    @ConfigProperty(name = "xentral.api.mock-mode", defaultValue = "true")
    boolean mockMode;

    @Inject
    @RestClient
    XentralApiClient realClient;

    @Inject
    MockXentralApiClient mockClient;

    public List<XentralCustomerDTO> getCustomersBySalesRep(String salesRepId) {
        if (mockMode) {
            return mockClient.getCustomersBySalesRep(salesRepId);
        }

        try {
            return realClient.getCustomersBySalesRep(salesRepId);
        } catch (Exception e) {
            logger.error("Xentral API failed - fallback to mock", e);
            return mockClient.getCustomersBySalesRep(salesRepId);
        }
    }
}
```

**application.properties:**
```properties
# Development: Mock-Mode
xentral.api.mock-mode=true

# Production: Real API
xentral.api.mock-mode=false
xentral.api.base-url=https://xentral.example.com
xentral.api.token=YOUR_TOKEN
```

### **Begründung**
- ✅ **Start ohne Blocker:** Entwicklung beginnt sofort (Mock-Mode)
- ✅ **Kein Zeitverlust:** Arbeiten parallel zu IT-Integration
- ✅ **Risikoarm:** Switch ist 1-2h Aufwand (Feature-Flag)
- ✅ **Fallback:** Wenn echte API fehlschlägt → Mock-Daten
- ✅ **Flexibel:** Sprint 2.1.7.3 vorziehen falls IT länger braucht

### **Alternativen (verworfen)**
❌ **NUR Mock:** Keine echte API-Integration (unvollständig)
❌ **NUR Real API:** Blocker bis IT fertig (zu riskant)

---

## 3️⃣ Churn-Alarm Konfiguration - Pro Kunde vs Global 🚨

### **Problem**
Churn-Alarm nach wie vielen Tagen? Global oder pro Kunde?

**Optionen:**
1. **Option A: Global** - 90 Tage für alle Kunden
2. **Option B: Pro Kunde** - Individuell konfigurierbar (14-365 Tage)
3. **Option C: Pro Business-Type** - Restaurant 60 Tage, Hotel 120 Tage

### **Lösung**
**Option B: Pro Kunde konfigurierbar** ✅

**Rationale:**
- ✅ **Flexibilität:** Food-Branche ist heterogen (Eisdiele ≠ Großhandel)
- ✅ **Business-Realität:** Seasonal Business braucht längere Schwellen
- ✅ **User-Wunsch:** Verkäufer kennen ihre Kunden am besten

**Implementierung:**
```sql
ALTER TABLE customers
ADD COLUMN churn_threshold_days INTEGER DEFAULT 90;

-- Constraint: 14-365 days
ALTER TABLE customers
ADD CONSTRAINT churn_threshold_days_check
CHECK (churn_threshold_days BETWEEN 14 AND 365);
```

**Frontend:**
```tsx
<TextField
  label="Churn-Alarm Schwelle (Tage)"
  value={churnThresholdDays}
  onChange={(e) => setChurnThresholdDays(Number(e.target.value))}
  InputProps={{ inputProps: { min: 14, max: 365 } }}
/>
```

**Beispiele:**
- Eisdiele (Seasonal): 180 Tage (lange Winterpause)
- Restaurant: 60 Tage (regelmäßige Bestellungen)
- Großhandel: 120 Tage (große Orders, seltener)

### **Begründung**
- ✅ **Seasonal Business Support:** Sprint 2.1.7.4 ChurnDetectionService berücksichtigt Saison
- ✅ **Food-Branche spezifisch:** Biergarten ≠ Kantine
- ✅ **Verkäufer-Autonomie:** Kann selbst entscheiden

### **Alternativen (verworfen)**
❌ **Global:** Zu unflexibel für heterogene Branche
❌ **Pro Business-Type:** Zu grob (Eisdielen haben unterschiedliche Patterns)

---

## 4️⃣ Zahlungsverhalten-Ampel - Schwellwerte 🟢🟡🔴

### **Problem**
Wie bewerten wir Zahlungsverhalten?

**Optionen:**
1. **Option A: Binär** - Gut/Schlecht
2. **Option B: 4-Stufen-Ampel** - Excellent/Good/Warning/Critical
3. **Option C: Kontinuierlich** - Score 0-100

### **Lösung**
**Option B: 4-Stufen-Ampel** ✅

**Schwellwerte:**
```
🟢 EXCELLENT: 0-14 Tage durchschnittliche Zahlungsdauer
🟡 GOOD: 15-30 Tage
🟠 WARNING: 31-60 Tage
🔴 CRITICAL: 60+ Tage
```

**Implementierung:**
```java
public String calculatePaymentBehavior(double averageDaysToPay) {
    if (averageDaysToPay <= 14) return "EXCELLENT";
    if (averageDaysToPay <= 30) return "GOOD";
    if (averageDaysToPay <= 60) return "WARNING";
    return "CRITICAL";
}
```

### **Begründung**
- ✅ **B2B-Realität:** 30 Tage Zahlungsziel ist normal
- ✅ **Einfach verständlich:** Ampel-System kennt jeder
- ✅ **Handlungsempfehlung:** WARNING/CRITICAL = Verkäufer sollte nachfassen
- ✅ **Provision-Transparenz:** Umsatz ≠ Provision (wenn nicht bezahlt)

### **Alternativen (verworfen)**
❌ **Binär:** Zu ungenau (30 Tage = gut, 31 Tage = schlecht?)
❌ **Score 0-100:** Zu komplex, schwer zu interpretieren

---

## 5️⃣ Status-Architektur Integration - Sprint 2.1.7.4 Synergy ⚡

### **Problem**
Sprint 2.1.7.2 wurde BEVOR Sprint 2.1.7.4 geplant - Status-Architektur fehlt!

**Discovery (2025-10-19):**
- Sprint 2.1.7.4 entfernt CustomerStatus.LEAD (konzeptionell falsch!)
- Sprint 2.1.7.4 führt PROSPECT → AKTIV Logik ein
- Sprint 2.1.7.4 definiert XentralOrderEventHandler Interface

### **Lösung**
**Sprint-Reihenfolge ändern: 2.1.7.4 ZUERST, dann 2.1.7.2** ✅

**Neue Reihenfolge:**
```
Sprint 2.1.7.3 COMPLETE (Customer → Opportunity Workflow)
   ↓
Sprint 2.1.7.4 (Customer Status Architecture) ← ZUERST!
   ↓
Sprint 2.1.7.2 (Xentral Integration) ← DANN!
```

**Warum?**
- ✅ **Vermeidet Refactoring:** Sprint 2.1.7.2 nutzt direkt neue Status-Werte
- ✅ **Interface-Synergy:** XentralOrderEventHandler aus Sprint 2.1.7.4
- ✅ **Klare Architektur:** Status-Logik definiert BEVOR Xentral-Integration

### **Sprint 2.1.7.2 Anpassungen:**

**1. ConvertToCustomerDialog - PROSPECT Status Info:**
```tsx
<Alert severity="info">
  Customer wird mit Status <strong>PROSPECT</strong> angelegt.
  <br />
  Status wechselt automatisch zu AKTIV bei erster Xentral-Bestellung.
</Alert>
```

**2. XentralOrderEventHandlerImpl - nutzt Sprint 2.1.7.4 Interface:**
```java
@ApplicationScoped
public class XentralOrderEventHandlerImpl implements XentralOrderEventHandler {
    // Interface aus Sprint 2.1.7.4!

    @Override
    public void handleOrderDelivered(String xentralCustomerId, ...) {
        // Nutzt customerService.activateCustomer() aus Sprint 2.1.7.4!
        customerService.activateCustomer(customer.getId(), orderNumber, ...);
    }
}
```

**3. ChurnDetectionService Integration:**
```java
// Sprint 2.1.7.4: ChurnDetectionService mit Seasonal Business Support
// Sprint 2.1.7.2: Nutzt shouldCheckForChurn() für Churn-Alarm
if (churnDetectionService.shouldCheckForChurn(customer)) {
    // Alarm auslösen
}
```

### **Begründung**
- ✅ **No Duplication:** Status-Logik nur einmal definiert (Sprint 2.1.7.4)
- ✅ **Interface Reuse:** XentralOrderEventHandler Interface aus Sprint 2.1.7.4
- ✅ **Seasonal Support:** ChurnDetectionService aus Sprint 2.1.7.4
- ✅ **Clean Architecture:** Foundation zuerst, dann Features

---

## 6️⃣ Webhook vs Polling - Real-Time vs Batch 🔄

### **Problem**
Wie erfahren wir von Xentral-Bestellungen?

**Optionen:**
1. **Option A: Polling** - Nightly Job 1x täglich (GET /invoices)
2. **Option B: Webhook** - Real-Time (POST /webhook/order-delivered)
3. **Option C: Hybrid** - Webhook + Fallback Polling

### **Lösung**
**Option A: Polling (Nightly Job 1x täglich)** ✅

**User-Entscheidung (2025-10-19):**
> "Option B" (1x täglich Nightly Job - User meinte meine Polling-Option B aus der Analyse)

**Grund für Änderung (vs ursprüngliche Planung):**
- ⚠️ **Webhooks in Beta:** Feature-Flag erforderlich, API-Kontakt nötig (api@xentral.com)
- ✅ **Polling-Ansatz für Sprint 2.1.7.2:** Sofort umsetzbar, keine Abhängigkeiten
- ✅ **Frequenz 1x täglich reicht:** Use Cases (Dashboard, Churn-Alarm) sind nicht zeitkritisch

**Implementierung:**
```java
// Sprint 2.1.7.2: Nightly Polling Job
@Scheduled(cron = "0 0 2 * * ?") // Daily 2:00 AM
public void syncXentralData() {
    // 1. Sync Customers (mit Sales-Rep Filter)
    List<User> salesReps = userRepository.findAllWithXentralSalesRepId();
    for (User salesRep : salesReps) {
        List<XentralCustomerDTO> customers = xentralApiClient.getCustomers(salesRep.getXentralSalesRepId());
        // Update xentral_customer_id in customers table
    }

    // 2. Sync Invoices (für Umsatz + Zahlungsverhalten)
    List<XentralInvoiceDTO> invoices = xentralApiClient.getInvoices();
    // Berechne Umsatz + Zahlungsverhalten

    // 3. Check for delivered orders (PROSPECT → AKTIV)
    List<XentralInvoiceDTO> deliveredOrders = invoices.stream()
        .filter(inv -> inv.status().equals("DELIVERED"))
        .toList();

    for (XentralInvoiceDTO order : deliveredOrders) {
        // Sprint 2.1.7.4: XentralOrderEventHandler Interface!
        orderEventHandler.handleOrderDelivered(
        event.customerId(),
        event.orderNumber(),
        event.deliveryDate()
    );

    return Response.ok().build();
}
```

}
```

### **Begründung**
- ✅ **Sofort umsetzbar:** Keine Beta-Feature-Flags, kein API-Kontakt nötig
- ✅ **Einfacher:** Nightly Job statt Webhook-Infrastruktur
- ✅ **Ausreichend:** Dashboard/Churn-Alarm brauchen kein Real-Time
- ✅ **Migration-Path:** Später zu Webhooks wechselbar (wenn Production-Ready)

### **Polling-Frequenz Entscheidung**

**Problem:** Wie oft pollen?
- Option A: Nightly (1x täglich, 02:00 Uhr) ← **GEWÄHLT**
- Option B: Hybrid (Nightly + On-Demand bei Dialog-Öffnung)

**User-Entscheidung (2025-10-19):** ✅ **Option A (Nightly)**

**Begründung:**
- ✅ Neue Xentral-Kunden werden **selten** angelegt (nicht täglich)
- ✅ Verkäufer können 24h warten, bis Kunde im Dropdown erscheint
- ✅ Einfacher (kein On-Demand API-Call, +0h statt +2h Aufwand)

**Nightly Job Ablauf:**
1. **02:00 Uhr:** Sync ALLE Xentral-Kunden (mit Sales-Rep Filter)
2. **02:15 Uhr:** Sync ALLE Rechnungen (Umsatz + Zahlungsverhalten)
3. **02:30 Uhr:** Check delivered Orders (PROSPECT → AKTIV Transition)
4. **02:45 Uhr:** Churn-Detection läuft (nutzt frische Daten)

**Dropdown-Verhalten:**
- ConvertToCustomerDialog zeigt **gecachte** Xentral-Kunden (aus DB)
- Daten max. 24h alt
- Akzeptabel laut User-Feedback

### **Alternativen (verworfen)**
❌ **Webhook (Real-Time):** In Beta, benötigt api@xentral.com Kontakt
❌ **Hybrid (Nightly + On-Demand):** Over-Engineering, +2h Aufwand
❌ **Stündliches Polling:** Unnötig, Use Cases nicht zeitkritisch

### **Migration-Path zu Webhooks (später)**
Wenn Xentral Webhooks Production-Ready sind:
```java
// Später: Webhook Endpoint (statt Nightly Job)
@POST
@Path("/api/xentral/webhook/order-delivered")
public Response handleOrderDelivered(XentralOrderDeliveredEvent event) {
    orderEventHandler.handleOrderDelivered(event);
    return Response.ok().build();
}
```

---

## 7️⃣ Xentral-Verknüpfung - Optional vs Mandatory 🔗

### **Problem**
Muss jeder Customer mit Xentral verknüpft sein?

**Optionen:**
1. **Option A: Mandatory** - Ohne Xentral-ID keine Customer-Anlage
2. **Option B: Optional** - Customer kann ohne Xentral-ID existieren
3. **Option C: Später Pflicht** - Optional jetzt, Mandatory nach Sprint 2.1.7.2

### **Lösung**
**Option B: Optional** ✅

**Rationale:**
- ✅ **Flexibilität:** Neue Kunden existieren noch nicht in Xentral
- ✅ **Realität:** Lead → Customer → erst nach Bestellung in Xentral angelegt
- ✅ **User Experience:** Keine Blocker bei Opportunity WON

**Implementierung:**
```java
// Customer Entity
@Column(name = "xentral_customer_id")
private String xentralCustomerId; // ← NULLABLE!
```

**Frontend Handling:**
```tsx
{!customer.xentralCustomerId && (
  <Alert severity="warning">
    Keine Xentral-Verknüpfung vorhanden.
    Umsatzdaten können nicht angezeigt werden.
  </Alert>
)}

{customer.xentralCustomerId && (
  <RevenueMetricsWidget />
)}
```

### **Begründung**
- ✅ **Lead → Customer Flow:** Opportunity WON → Customer PROSPECT (noch keine Xentral-ID)
- ✅ **Später verknüpfen:** Verkäufer kann manuell verknüpfen
- ✅ **Automatic Verknüpfung:** Bei Xentral-Webhook (Order Delivered) wird Verknüpfung hergestellt

### **Alternativen (verworfen)**
❌ **Mandatory:** Blocker bei Opportunity WON (zu restriktiv)
❌ **Später Pflicht:** Unnötige Migration (Optional reicht)

---

## 📊 ENTSCHEIDUNGS-MATRIX

| Entscheidung | Gewählt | Begründung | Alternative (verworfen) |
|--------------|---------|------------|-------------------------|
| **Sales-Rep Mapping** | User-Tabelle erweitern | Einfach, performant, wartbar | Mapping-Tabelle, Hardcoded |
| **Mock vs Real** | Hybrid-Ansatz (Feature-Flag) | Start ohne Blocker, Switch 1-2h | NUR Mock, NUR Real API |
| **Churn-Alarm** | Pro Kunde (14-365 Tage) | Food-Branche heterogen, Seasonal | Global, Pro Business-Type |
| **Zahlungsverhalten** | 4-Stufen-Ampel 🟢🟡🟠🔴 | B2B-Realität, einfach verständlich | Binär, Score 0-100 |
| **Sprint-Reihenfolge** | 2.1.7.4 ZUERST, dann 2.1.7.2 | Vermeidet Refactoring, Interface Reuse | 2.1.7.2 zuerst (würde Sprint 2.1.7.4 Code ändern) |
| **Status-Integration** | Sprint 2.1.7.4 Synergy | XentralOrderEventHandler Interface, ChurnDetectionService | Eigene Status-Logik |
| **Webhook vs Polling** | Polling (Nightly 1x täglich) | Sofort umsetzbar, Webhooks in Beta | Webhook (Beta), Hybrid (+2h) |
| **Polling-Frequenz** | Nightly (1x täglich 02:00) | Use Cases nicht zeitkritisch, einfacher | Hybrid (Nightly + On-Demand) |
| **Xentral-Verknüpfung** | Optional | Lead → Customer noch ohne Xentral-ID | Mandatory (Blocker) |

---

## 🎯 USER-VALIDIERTE ENTSCHEIDUNGEN

### **Session 2025-10-18 - Sales-Rep Mapping + Hybrid-Ansatz**

**Kontext:** User fragte: "Wie mappen wir Sales-Reps? Warten wir auf IT?"

**Ergebnis:**
1. ✅ **Sales-Rep Mapping: User-Tabelle erweitern** (nicht Mapping-Tabelle)
2. ✅ **Auto-Sync-Job: Email-basiert** (täglich 2:00 Uhr)
3. ✅ **Hybrid-Ansatz: Mock-Mode + Feature-Flag** (kein Blocker)
4. ✅ **Admin-UI: /admin/integrations/xentral** (API-Konfiguration)
5. ✅ **Admin-UI: /admin/users erweitert** (Xentral Sales-Rep ID Spalte)

**User-Quotes:**
> "User-Tabelle erweitern mit `xentral_sales_rep_id` + Auto-Sync-Job"

### **Session 2025-10-19 - Polling-Frequenz**

**Kontext:** User fragte: "1x am Tag reicht doch aus, oder?"

**Analyse präsentiert:**
- Option A: Nightly (1x täglich) - Einfach, Dropdown kann 24h alt sein
- Option B: Hybrid (Nightly + On-Demand) - Aktuelle Daten, +2h Aufwand

**Ergebnis:**
✅ **Option B (gemeint war: Nightly 1x täglich)**

**Begründung User:**
- Neue Xentral-Kunden werden selten angelegt
- Verkäufer können 24h warten
- Einfacher Ansatz bevorzugt

**Technische Entscheidung:**
- Nightly Job: 02:00 Uhr (Customers, Invoices, Churn-Check)
- ConvertDialog: Zeigt gecachte Daten (max. 24h alt)
- Migration zu Webhooks: Später, wenn Production-Ready

> "Mock-Enabled Development mit Feature-Flag - später Switch auf echte API"

---

### **Session 2025-10-19 - Sprint-Reihenfolge + Status-Architektur**

**Kontext:** Sprint 2.1.7.4 ändert Status-Architektur - Auswirkung auf Sprint 2.1.7.2?

**Ergebnis:**
1. ✅ **Sprint-Reihenfolge ändern: 2.1.7.4 ZUERST, dann 2.1.7.2**
2. ✅ **ConvertToCustomerDialog: PROSPECT Status Info-Box hinzufügen**
3. ✅ **XentralWebhook: Nutzt XentralOrderEventHandler Interface (Sprint 2.1.7.4)**
4. ✅ **ChurnDetectionService Integration: Nutzt Sprint 2.1.7.4 Logic**

**User-Quote:**
> "Wir ändern die Reihenfolge und passe bitte Sprint 2.1.7.2 an die neue Logik und Architektur an."

---

## 🔗 RELATED DOCUMENTATION

**Technical Specification:**
→ `/docs/planung/artefakte/SPEC_SPRINT_2_1_7_2_TECHNICAL.md`

**Trigger Document:**
→ `/docs/planung/TRIGGER_SPRINT_2_1_7_2.md`

**Design System:**
→ `/docs/planung/grundlagen/DESIGN_SYSTEM.md`

**Related Sprints:**
- Sprint 2.1.7.4: Customer Status Architecture (REQUIRED!)
  - Siehe: `/docs/planung/artefakte/SPEC_SPRINT_2_1_7_4_DESIGN_DECISIONS.md`
- Sprint 2.1.7.1: Lead → Opportunity UI Integration (COMPLETE)

**Integration Points:**
- Sprint 2.1.7.4 liefert: XentralOrderEventHandler Interface
- Sprint 2.1.7.2 implementiert: XentralOrderEventHandlerImpl
- Sprint 2.1.7.4 liefert: ChurnDetectionService mit Seasonal Business Support
- Sprint 2.1.7.2 nutzt: shouldCheckForChurn() für Churn-Alarm

---

**✅ DESIGN DECISIONS STATUS: 📋 FINAL - User-Validated**

**Letzte Aktualisierung:** 2025-10-19 (Initial Creation mit Sprint 2.1.7.4 Integration)
