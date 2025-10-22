# Sprint 2.1.7.4 - Design Decisions

**Sprint-ID:** 2.1.7.4
**Created:** 2025-10-19
**Status:** 📋 FINAL
**Owner:** Claude + Jörg (User Validation)

---

## 📋 ÜBERSICHT

Dieses Dokument konsolidiert **alle Architektur- und Design-Entscheidungen** für Sprint 2.1.7.4.

**Hauptentscheidungen:**
1. CustomerStatus.LEAD entfernen (PROSPECT statt LEAD)
2. Wann wird Customer AKTIV? (Erste Bestellung vs. Bezahlte Rechnung)
3. Auto-Conversion bei Opportunity WON
4. Manual vs Automatic Activation
5. Seasonal Business Support (Food-Branche kritisch!)
6. PROSPECT Lifecycle - Warnungen statt Auto-Archivierung
7. Seasonal Pattern Implementation Strategy
8. **LeadConvertService Komplett-Fix** (100% Datenübernahme)
9. **Umsatz-Konzept Harmonisierung** (estimatedVolume → expectedAnnualVolume)
10. **Multi-Location Management Architektur** (DEFERRED → Sprint 2.1.7.7)

---

## 1️⃣ CustomerStatus.LEAD entfernen ⭐

### **Problem**
CustomerStatus Enum hat einen `LEAD` Wert - aber das ist konzeptionell falsch!

**Aktueller Stand (vor Sprint 2.1.7.4):**
```java
public enum CustomerStatus {
  LEAD,        // ❌ WRONG! Leads sollten in "leads" Tabelle sein
  PROSPECT,
  AKTIV,
  // ...
}
```

**Frage:** Ist es logisch, dass ein Kunde den Status "LEAD" hat?

### **Lösung**
**LEAD entfernen** - Leads gehören in die `leads` Tabelle, nicht in `customers`!

**Neue Architektur:**
```
LEADS TABELLE:
- Status: REGISTERED → ACTIVE → QUALIFIED → CONVERTED

CUSTOMERS TABELLE:
- Status: PROSPECT → AKTIV → RISIKO → INAKTIV → ARCHIVIERT
```

**Begründung:**
- **Konzeptionelle Klarheit:** Lead ≠ Customer
- **Datenmodell korrekt:** Leads und Customers sind verschiedene Entities
- **B2B CRM Best Practice:** PROSPECT = "qualifizierter Lead, aber noch kein Kunde"

### **Migration Strategy**
```sql
-- Alle bestehenden LEAD-Kunden zu PROSPECT migrieren
UPDATE customers SET status = 'PROSPECT' WHERE status = 'LEAD';

-- CHECK Constraint aktualisieren
ALTER TABLE customers
ADD CONSTRAINT customer_status_check
CHECK (status IN ('PROSPECT', 'AKTIV', 'RISIKO', 'INAKTIV', 'ARCHIVIERT'));
```

### **Alternativen (verworfen)**
❌ **LEAD beibehalten:** Konzeptionell falsch, verwirrend
❌ **Alle LEAD → AKTIV migrieren:** Zu aggressiv - noch keine Bestellung!
❌ **Neue Tabelle "prospects":** Over-Engineering - PROSPECT als Status reicht

---

## 2️⃣ Wann wird Customer AKTIV? 🎯

### **Problem**
Lead wird zu Customer konvertiert - aber ab wann ist er **wirklich** ein Kunde?

**Optionen:**
1. **Opportunity WON:** Sofort bei gewonnener Opportunity
2. **Erste Bestellung erstellt:** Order in Xentral angelegt
3. **Erste Bestellung geliefert:** Ware beim Kunden angekommen
4. **Erste Rechnung bezahlt:** Payment received

### **Lösung**
**Option 3: Erste Bestellung geliefert** ✅

**User-Entscheidung (2025-10-19):**
> "AKTIV bei erster **gelieferter Bestellung** (nicht bei Rechnung!)"

**Begründung:**
- ✅ **Pragmatisch:** Ware geliefert = Kunde ist real (Stornos sind selten)
- ✅ **Verkäufer-Moral:** Sofort sichtbar statt 60 Tage warten
- ✅ **B2B-Realität:** Zahlungsziel 30-60 Tage ist normal - zu lange für Status-Update
- ✅ **Business-Validierung:** Delivery = echter Umsatz (noch nicht bezahlt, aber committed)

### **Alternativen (verworfen)**
❌ **Opportunity WON:** Zu früh - Deal könnte noch platzen
❌ **Erste Rechnung bezahlt:** Zu spät - B2B hat oft 30-60 Tage Zahlungsziel

### **Implementierung**
```
Lead → Opportunity WON → Customer (PROSPECT)
                           ↓
                    Erste Bestellung geliefert
                           ↓
                    Customer (AKTIV)
```

**Workflow:**
1. Opportunity WON → Auto-Convert Lead → Customer (status: PROSPECT)
2. Vertriebler bekommt Benachrichtigung: "Erste Bestellung geliefert?"
3. Manuelle Aktivierung: Button "Als AKTIV markieren"
4. Später (Sprint 2.1.7.2): Automatische Aktivierung via Xentral Webhook

---

## 3️⃣ Auto-Conversion bei Opportunity WON ⚡

### **Problem**
Vertriebler müssen manuell Lead → Customer konvertieren nach WON - nervt!

### **Lösung**
**Automatische Conversion** bei Opportunity WON (wenn leadId vorhanden)

**Workflow:**
```
Opportunity Stage: QUALIFIED → PROPOSAL → NEGOTIATION → CLOSED_WON
                                                            ↓
                                                 (leadId != null?)
                                                            ↓
                                                         YES!
                                                            ↓
                        Auto-Convert: Lead → Customer (PROSPECT)
                                                            ↓
                        Opportunity-Link: leadId → customerId
```

**Vorteile:**
- ✅ **Keine manuelle Arbeit:** Conversion passiert automatisch
- ✅ **Keine verlorenen Leads:** Jeder WON-Lead wird konvertiert
- ✅ **Audit-Trail:** System protokolliert "Auto-converted from Opportunity WON"

### **Alternativen (verworfen)**
❌ **Manuelle Conversion:** Zu viel Arbeit, fehleranfällig
❌ **Batch-Job nachts:** Zu langsam - Vertriebler will sofort sehen
❌ **Conversion beim ersten Contact:** Zu früh - noch kein Deal!

### **Edge Cases**
- **Lead nicht gefunden:** Log warning, continue (Opportunity bleibt WON)
- **Lead bereits konvertiert:** Skip conversion (Opportunity-Link aktualisieren)
- **Opportunity hat schon Customer:** Skip conversion (nichts zu tun)

---

## 4️⃣ Manual vs Automatic Activation 🔧

### **Problem**
Wann wird Customer PROSPECT → AKTIV?

### **Lösung**
**Hybrid-Ansatz** (Sprint 2.1.7.4: Manual, Sprint 2.1.7.2: Automatic)

**User-Entscheidung (2025-10-19):**
> "Manual Activation jetzt, Xentral Webhook später"

**Phase 1 (Sprint 2.1.7.4): Manual Activation**
```tsx
<Button onClick={activateCustomer}>
  Erste Bestellung geliefert → AKTIV markieren
</Button>
```

**Workflow:**
1. Vertriebler sieht PROSPECT Alert auf CustomerDetailPage
2. Klick auf "AKTIV markieren" Button
3. Dialog: "Bestellnummer?" (optional, für Audit-Trail)
4. API PUT /api/customers/{id}/activate
5. Status: PROSPECT → AKTIV
6. Audit-Log: "Activated by [User] - Order: [Number]"

**Phase 2 (Sprint 2.1.7.2): Automatic Activation**
```java
// Xentral Webhook: Order Delivered Event
@POST
@Path("/webhook/xentral/order-delivered")
public Response handleOrderDelivered(XentralOrderEvent event) {
  // 1. Find Customer by xentralCustomerId
  // 2. If status == PROSPECT → Activate!
  // 3. Audit-Log: "Activated automatically via Xentral"
}
```

### **Begründung**
- ✅ **Pragmatisch:** Manual Activation sofort verfügbar (Sprint 2.1.7.4)
- ✅ **Ausbaufähig:** Xentral Integration später (Sprint 2.1.7.2)
- ✅ **Fallback:** Wenn Xentral-Webhook ausfällt → Manual Button bleibt

### **Alternativen (verworfen)**
❌ **Nur Automatic:** Zu riskant ohne Xentral-Integration fertig
❌ **Nur Manual:** Zu viel manuelle Arbeit langfristig

---

## 5️⃣ Seasonal Business Support 🍦❄️

### **Problem**
Food-Branche hat massive Saisonbetriebe:
- **Eisdielen:** März-Oktober (7 Monate aktiv, 5 Monate Pause)
- **Ski-Hütten:** Dezember-März (4 Monate aktiv, 8 Monate Pause)
- **Biergärten:** April-September (6 Monate aktiv)
- **Weihnachtsmarkt-Stände:** November-Dezember (2 Monate aktiv!)

**Ohne Seasonal Support:**
```
Eisdiele:
- Oktober: Letzte Bestellung → AKTIV ✅
- Januar: 90 Tage keine Bestellung → RISIKO ❌ (FALSCH!)
- März: 150 Tage keine Bestellung → INAKTIV ❌ (TOTAL FALSCH!)

Dashboard:
- Churn-Rate: 40% ❌ (alle Eisdielen im Winter!)
- Risiko-Kunden: 50+ ❌ (alle Saisonbetriebe!)
```

### **Lösung**
**Seasonal Business Flag + Active Months** ✅

**User-Entscheidung (2025-10-19):**
> "Seasonal Business IN Sprint 2.1.7.4 - kritisch für Food-Branche!"

**Datenbankschema:**
```sql
ALTER TABLE customers
ADD COLUMN is_seasonal_business BOOLEAN DEFAULT FALSE,
ADD COLUMN seasonal_months INTEGER[] DEFAULT NULL,  -- [1-12]
ADD COLUMN seasonal_pattern VARCHAR(50) DEFAULT NULL; -- 'SUMMER', 'WINTER', etc.
```

**Business-Logik:**
```java
public boolean shouldCheckForChurn(Customer customer) {
  if (customer.isSeasonalBusiness()) {
    int currentMonth = LocalDate.now().getMonthValue();
    if (!customer.getSeasonalMonths().contains(currentMonth)) {
      return false; // Outside season = expected inactivity
    }
  }
  return true;
}
```

**Beispiel:**
```
Eisdiele (seasonal_months = [3,4,5,6,7,8,9,10]):
- Januar (Monat 1): shouldCheckForChurn() = FALSE ✅ (kein Alarm!)
- Juli (Monat 7): shouldCheckForChurn() = TRUE ✅ (normales Monitoring)
```

### **Begründung**
- ✅ **Business-kritisch:** 20-30% der Food-Kunden sind saisonal
- ✅ **Falsche Alarme vermeiden:** Churn-Rate korrekt (nicht 40% zu hoch)
- ✅ **Dashboard-Genauigkeit:** Vertriebsleiter sieht reale Risiko-Kunden
- ✅ **Einfache Implementierung:** 2h Aufwand = akzeptabel

### **Alternativen (verworfen)**
❌ **Ignorieren:** 40% falsche Churn-Rate = Dashboard nutzlos
❌ **Manuell filtern:** Zu viel Arbeit für Vertriebsleiter
❌ **Später implementieren:** Zu spät - Dashboard ist schon falsch

---

## 6️⃣ PROSPECT Lifecycle - Warnungen statt Auto-Archivierung ⚠️

### **Problem**
PROSPECT für immer? Was wenn Kunde nie bestellt?

**Scenario:**
```
Lead → Opportunity WON → Customer (PROSPECT)
                            ↓
                      (90 Tage vergehen...)
                            ↓
                      Kunde bestellt NIE!
                            ↓
                      Was tun? 🤔
```

**Optionen:**
1. **Auto-Archivierung:** Nach 90 Tagen → ARCHIVIERT
2. **Warnungen + Manual Review:** 60/90 Tage Warnungen, dann manuelle Entscheidung
3. **Nichts tun:** PROSPECT bleibt für immer

### **Lösung**
**Option 2: Warnungen + Manual Review** ✅

**User-Entscheidung (2025-10-19):**
> "NICHT automatisch archivieren! Warnungen statt Auto-Action."

**Rationale:**
- ✅ **B2B Food hat langen Vorlauf:** 3-6 Monate Bauarbeiten, Budget-Freeze normal
- ✅ **Falsche Auto-Archive gefährlich:** Kunde könnte nach 4 Monaten bestellen!
- ✅ **Warnungen genügen:** Vertriebsleiter sieht "Review Required" und entscheidet

**Workflow:**
```
PROSPECT (0-60 Tage): Normal ✅
    ↓
PROSPECT (60-90 Tage): ⚠️ Warnung an Owner (Tag: "WARNED_60_DAYS")
    ↓
PROSPECT (90+ Tage): 🚨 Review Required (Tag: "REVIEW_REQUIRED")
    ↓
Manager-Entscheidung: Weiterverfol gen oder Archivieren
```

**Implementierung:**
- **Sprint 2.1.7.4:** Keine PROSPECT Lifecycle (zu viel Scope)
- **Sprint 2.1.7.6:** ProspectLifecycleService (Nightly Job)

### **Alternativen (verworfen)**
❌ **Auto-Archivierung nach 90 Tagen:** Zu aggressiv für B2B Food
❌ **Nichts tun:** PROSPECT stapeln sich - unübersichtlich

---

## 7️⃣ Seasonal Pattern Implementation Strategy 📅

### **Problem**
Wie implementieren wir Seasonal Patterns? All-in-one oder schrittweise?

**Optionen:**
1. **All-in-one (Sprint 2.1.7.4):** DB + Basic Logic + Advanced UI
2. **Schrittweise (Sprint 2.1.7.4 + 2.1.7.6):** DB jetzt, Advanced UI später

### **Lösung**
**Schrittweise Implementation** ✅

**User-Entscheidung (2025-10-19):**
> "Seasonal Pattern: Start mit Monats-Arrays (Sprint 2.1.7.4), Advanced UI später (Sprint 2.1.7.6)"

**Sprint 2.1.7.4 (Basic):**
- ✅ DB Schema (is_seasonal_business, seasonal_months[], seasonal_pattern)
- ✅ Backend Logic (ChurnDetectionService.shouldCheckForChurn())
- ✅ Entity + DTO (Customer.java, CustomerResponse.java)
- ✅ Dashboard Metrics (seasonalActive, seasonalPaused)
- ✅ Simple Frontend Indicator (CustomerDetailPage Alert)

**Sprint 2.1.7.6 (Advanced):**
- 🔜 MonthPicker Component (Custom Month Selection)
- 🔜 Pattern Templates (SUMMER, WINTER, CHRISTMAS, etc.)
- 🔜 Edit Customer Dialog (Seasonal Business Toggle)
- 🔜 Preview (Active/Inactive Months)

**Begründung:**
- ✅ **Sprint 2.1.7.4 Focus:** Status Architecture (PROSPECT/AKTIV)
- ✅ **Vermeidet Scope Creep:** 12h statt 18h
- ✅ **Foundation zuerst:** DB + Logic muss funktionieren vor UI
- ✅ **Sprint 2.1.7.6 synergy:** PROSPECT Lifecycle + Seasonal Patterns UI

### **Alternativen (verworfen)**
❌ **All-in-one (Sprint 2.1.7.4):** Zu viel Scope (18h statt 12h)
❌ **Nur DB (Sprint 2.1.7.4):** Zu wenig - Dashboard wäre falsch
❌ **Gar nicht (Sprint 2.1.7.4):** Food-Branche zu wichtig!

---

## 📊 ENTSCHEIDUNGS-MATRIX

| Entscheidung | Gewählt | Begründung | Alternative (verworfen) |
|--------------|---------|------------|-------------------------|
| **LEAD entfernen** | JA | Konzeptionell falsch | LEAD beibehalten |
| **AKTIV Trigger** | Erste Bestellung geliefert | Pragmatisch (B2B 30-60 Tage Zahlungsziel) | Bezahlte Rechnung |
| **Auto-Conversion WON** | JA | Keine manuelle Arbeit | Manuell konvertieren |
| **Manual Activation** | JA (Phase 1) | Sofort verfügbar | Nur Xentral Webhook |
| **Seasonal Business** | JA (Sprint 2.1.7.4) | Food-Branche kritisch (40% false alarms) | Ignorieren, später |
| **PROSPECT Lifecycle** | Sprint 2.1.7.6 | Scope-Vermeidung | Sprint 2.1.7.4 |
| **Auto-Archivierung** | NEIN | B2B Food hat langen Vorlauf (3-6 Monate) | Auto-Archive nach 90 Tagen |
| **Seasonal Pattern UI** | Sprint 2.1.7.6 | Foundation zuerst (DB + Logic) | All-in-one Sprint 2.1.7.4 |

---

## 🎯 USER-VALIDIERTE ENTSCHEIDUNGEN

### **Session 2025-10-19 - Lead → Customer Conversion Diskussion**

**Kontext:** User fragte: "Ist es denn logisch, dass ein Kunde den Status 'Lead' hat?"

**Ergebnis:**
1. ✅ **CustomerStatus.LEAD entfernen** - konzeptionell falsch
2. ✅ **AKTIV bei erster Bestellung** (nicht Rechnung)
3. ✅ **Seasonal Business IN Sprint 2.1.7.4** - kritisch für Food-Branche
4. ✅ **PROSPECT Lifecycle später (Sprint 2.1.7.6)** - Warnungen statt Auto-Archivierung
5. ✅ **Seasonal Pattern: Start mit Monats-Arrays** - Advanced UI in Sprint 2.1.7.6

**User-Quotes:**
> "AKTIV bei erster **gelieferter Bestellung** (nicht bei Rechnung!)"

> "Seasonal Business IN Sprint 2.1.7.4 - kritisch für Food-Branche!"

> "NICHT automatisch archivieren! Warnungen statt Auto-Action."

> "Seasonal Pattern: Start mit Monats-Arrays (Sprint 2.1.7.4), Advanced UI später (Sprint 2.1.7.6)"

---

## 8️⃣ LeadConvertService Komplett-Fix (100% Datenübernahme) ⭐

### **Problem**
**Aktueller Code kopiert nur 3 Felder bei Lead→Customer Conversion!**

```java
// LeadConvertService.convertToCustomer() - AKTUELL (Zeile 78-86):
Customer customer = new Customer();
customer.setCustomerNumber(customerNumber);
customer.setCompanyName(lead.companyName);
customer.setStatus(CustomerStatus.AKTIV); // ❌ Auch falsch!
customer.setOriginalLeadId(leadId);
// ... Location/Address/Contact werden erstellt
// ❌ ABER: 15+ Felder NICHT kopiert!
```

**Was FEHLT:**
- `kitchenSize` ❌ (vorhanden seit V10032)
- `employeeCount` ❌
- `estimatedVolume` ❌ **KRITISCH!**
- `branchCount`, `isChain` ❌
- `businessType` ❌
- **Pain Scoring (8 Felder)** ❌
- `painStaffShortage`, `painHighCosts`, `painFoodWaste`, etc.

**Impact:**
- Vertriebler verliert alle Lead-Qualifikationsdaten
- Opportunity-Kalkulation hat keine Basis (kein Volumen!)
- Pain Points müssen neu eingegeben werden

---

### **Lösung**
**100% Datenübernahme bei Lead→Customer Conversion** ✅

**Alle Felder kopieren:**

```java
// NACHHER (Sprint 2.1.7.4):
Customer customer = new Customer();
customer.setCustomerNumber(customerNumber);
customer.setCompanyName(lead.companyName);
customer.setStatus(CustomerStatus.PROSPECT); // ✅ KORREKT!
customer.setOriginalLeadId(leadId);

// Umsatz-Felder (KRITISCH!)
if (lead.estimatedVolume != null) {
    customer.setExpectedAnnualVolume(lead.estimatedVolume); // ✅ NEU!
}

// Business-Felder (Lead Parity - V10032)
customer.setKitchenSize(lead.kitchenSize);
customer.setEmployeeCount(lead.employeeCount);
customer.setBranchCount(lead.branchCount);
customer.setIsChain(lead.isChain);
customer.setBusinessType(lead.businessType);

// Pain Scoring (8 Boolean Felder - Sprint 2.1.6 Harmonization)
customer.setPainStaffShortage(lead.painStaffShortage);
customer.setPainHighCosts(lead.painHighCosts);
customer.setPainFoodWaste(lead.painFoodWaste);
customer.setPainQualityInconsistency(lead.painQualityInconsistency);
customer.setPainTimePressure(lead.painTimePressure);
customer.setPainSupplierQuality(lead.painSupplierQuality);
customer.setPainUnreliableDelivery(lead.painUnreliableDelivery);
customer.setPainPoorService(lead.painPoorService);
customer.setPainNotes(lead.painNotes);

// ... (rest bleibt wie vorher: Location, Address, Contact)
```

---

### **Begründung**
- ✅ **Keine Datenverluste:** Vertriebler-Arbeit wird nicht verschwendet
- ✅ **Opportunity-Basis:** expectedAnnualVolume ist sofort verfügbar
- ✅ **Pain Points erhalten:** Für Retention/Upsell wichtig
- ✅ **100% Parity:** Lead und Customer haben gleiche Datenqualität

### **Alternativen (verworfen)**
❌ **Nur Status-Fix (ohne Felder):** Datenverlust bleibt
❌ **Separater Sprint:** Zu spät - Lead→Customer Conversions passieren JETZT
❌ **Manuell nachpflegen:** Zu viel Arbeit, fehleranfällig

---

## 9️⃣ Umsatz-Konzept Harmonisierung (estimatedVolume → expectedAnnualVolume) 💰

### **Problem**
**3 Umsatzfelder = Verwirrung!**

| Feld | Wo? | Quelle | Zweck | Problem |
|------|-----|--------|-------|---------|
| `estimatedVolume` | Lead | Manuell | Qualifizierung | ✅ OK |
| `estimatedVolume` | Customer | ❌ NICHT kopiert | Sollte aus Lead kommen | ⚠️ Leer! |
| `expectedAnnualVolume` | Customer | Manuell | Prognose | ❓ Woher? |
| `actualAnnualVolume` | Customer | Xentral | ERP-Daten | 🔜 Sprint 2.1.7.2 |

**Fragen:**
1. Warum hat Customer ZWEI Volumen-Felder?
2. Wird `estimatedVolume` aus Lead übernommen? **NEIN!** ❌
3. Wie hängt `Opportunity.expectedValue` damit zusammen?

---

### **Lösung**
**3-Phasen-Modell mit klarem Data Flow** ✅

**Phase 1: LEAD**
```
Lead.estimatedVolume = 100.000€ (Vertriebler-Schätzung)
```

**Phase 2: CUSTOMER (Opportunity WON)**
```
Customer.expectedAnnualVolume = Lead.estimatedVolume (automatisch kopiert!)
Customer.actualAnnualVolume = NULL (noch keine Xentral-Daten)
```

**Phase 3: CUSTOMER (nach Sprint 2.1.7.2 - Xentral Sync)**
```
Customer.actualAnnualVolume = 87.450€ (Xentral - ECHTE Rechnungen!)
Customer.expectedAnnualVolume = 100.000€ (bleibt als Referenz)
```

**OpportunityMultiplier Nutzung:**
```java
// Opportunity.expectedValue Berechnung (automatisch!):
BigDecimal baseVolume = customer.actualAnnualVolume != null
    ? customer.actualAnnualVolume      // PREFER: Xentral Real Data
    : customer.expectedAnnualVolume;   // FALLBACK: Lead Estimate

BigDecimal multiplier = OpportunityMultiplier.getMultiplierValue(
    "HOTEL", "SORTIMENTSERWEITERUNG", BigDecimal.valueOf(0.65)
); // Returns: 0.65

opportunity.setExpectedValue(baseVolume.multiply(multiplier));
// Result: 87.450€ × 0.65 = 56.842€ (intelligenter Vorschlag!)
```

---

### **Entscheidungen**

**Decision 1: estimatedVolume → expectedAnnualVolume kopieren?**
- ✅ **JA** - Automatisch bei Lead→Customer Conversion
- Rationale: Lead-Schätzung ist wertvoll (Vertriebler kennt den Kunden)

**Decision 2: Customer.estimatedVolume Feld entfernen?**
- ❌ **NEIN** - Feld bleibt, aber wird NICHT genutzt
- Rationale: Backend-Parity (V10032 hat es angelegt), später evtl. Use-Case

**Decision 3: actualAnnualVolume überschreibt expectedAnnualVolume?**
- ❌ **NEIN** - Beide Felder parallel
- Rationale: Vertriebler sieht Abweichung (Expected: 100k€, Actual: 50k€)

**Decision 4: OpportunityMultiplier automatisch nutzen?**
- ✅ **JA** - Automatischer Vorschlag, manuell überschreibbar
- Rationale: Intelligente Hilfe für Vertriebler

---

### **Begründung**
- ✅ **Klarheit:** Jedes Feld hat klaren Zweck
- ✅ **Automation:** OpportunityMultiplier schlägt Wert vor
- ✅ **Flexibilität:** Vertriebler kann anpassen
- ✅ **Xentral-Ready:** actualAnnualVolume ersetzt expected (Sprint 2.1.7.2)

### **Alternativen (verworfen)**
❌ **Nur expectedAnnualVolume nutzen:** Verlust von Xentral-Realität
❌ **Keine Harmonisierung:** Verwirrung bleibt
❌ **Separates Dokument:** Wird nicht gelesen

**Artefakt:**
→ Siehe `/docs/planung/artefakte/UMSATZ_KONZEPT_DECISION.md` (vollständige Doku)

---

## 🔗 EXTERNAL ANALYSIS

**Source:** Externe KI-Diskussion (2025-10-19)

**Option C (Hybrid)** wurde gewählt:
- Manual Activation jetzt (Sprint 2.1.7.4)
- Xentral Webhook später (Sprint 2.1.7.2)
- Seasonal Business Support (Sprint 2.1.7.4)
- PROSPECT Lifecycle Warnings (Sprint 2.1.7.6)

**Kritische Würdigung:**
- ✅ **Pragmatisch:** Sofort einsetzbar
- ✅ **Ausbaufähig:** Xentral Integration nachholbar
- ✅ **Business-fokussiert:** Food-Branche Saisonalität berücksichtigt

---

## 🔟 Multi-Location Management Architektur (DEFERRED → Sprint 2.1.7.7)

### **Problem**

**User-Anforderung (2025-10-21):**
> "Wir haben oft Filialbetriebe, besonders Hotelketten"

**Kritische Fragen:**
1. **Customer-Anlage:** Wie legt Vertriebler Filialbetriebe nutzerfreundlich an?
2. **Lead-Conversion:** Ist der konvertierte Lead Hauptbetrieb oder Filiale?
3. **Xentral-Umsatz:** Wie ordnen wir Umsatz pro Filiale zu?
4. **Opportunities:** Pro Filiale oder pro Kette?

**Xentral-Realität:**
- ❌ **KEINE Filial-ID** in Xentral
- ✅ **Gleiche Kundennummer** für alle Filialen (z.B. `56037`)
- ✅ **Unterscheidung:** Nur über **Lieferadresse** (String-Matching!)

### **Status Quo - Was existiert bereits?**

#### ✅ **SEHR GUTE Basis-Infrastruktur!**

**1. Customer-Hierarchie (Customer.java:131-142):**
```java
// Parent-Child Relationship
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "parent_customer_id")
private Customer parentCustomer;  // ← Zentrale/Hauptbetrieb

@OneToMany(mappedBy = "parentCustomer", cascade = CascadeType.ALL)
private List<Customer> childCustomers = new ArrayList<>();  // ← Filialen

@Enumerated(EnumType.STRING)
@Column(name = "hierarchy_type", length = 20)
private CustomerHierarchyType hierarchyType = CustomerHierarchyType.STANDALONE;
```

**CustomerHierarchyType Enum:**
- `HEADQUARTER` - Zentrale/Hauptsitz
- `FILIALE` - Filiale/Zweigstelle
- `ABTEILUNG` - Abteilung
- `FRANCHISE` - Franchise-Nehmer
- `STANDALONE` - Einzelbetrieb

**2. CustomerLocation Support (Customer.java:300-304):**
```java
@OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
private List<CustomerLocation> locations = new ArrayList<>();
```

**CustomerLocation.java Features:**
- `locationName`, `locationCode`, `category`
- `isMainLocation` Flag
- `isShippingLocation` Flag
- **Multiple Adressen pro Location** (CustomerAddress)
- Business-Methoden: `getPrimaryShippingAddress()`, etc.

**3. Chain Structure Fields (Customer.java:175-192):**
```java
@Column(name = "total_locations_eu")
private Integer totalLocationsEU = 0;

@Column(name = "locations_germany")
private Integer locationsGermany = 0;
// ... Austria, Switzerland, Rest EU
```

**4. Lead Parity (Customer.java:110-120):**
```java
@Column(name = "branch_count")
private Integer branchCount = 1;

@Column(name = "is_chain")
private Boolean isChain = false;
```

### **Lücken - Was fehlt?**

#### ❌ **1. Opportunity → Location Verknüpfung**

```java
// Opportunity.java:52-53 (AKTUELL)
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "customer_id")
private Customer customer;  // ← NUR Customer, KEINE Location!
```

**Impact:**
- Opportunity kann nicht einer **spezifischen Filiale** zugeordnet werden
- Upselling/Cross-Selling pro Filiale schwer zu tracken

#### ❌ **2. Lead → Location Konzept**

```java
// Lead.java (AKTUELL)
@Column(name = "branch_count")
public Integer branchCount = 1;  // ← Nur Zahl, keine Locations

@Column(name = "is_chain")
public Boolean isChain = false;  // ← Boolean Flag, keine Details
```

**Impact:**
- Lead kann nicht "Filiale XY von Kette Z" sein
- Lead→Customer Conversion: Kann nicht spezifische Filiale zuordnen

#### ❌ **3. Xentral Filial-Mapping UNGEKLÄRT**

**Problem:**
- Xentral: Gleiche Kundennummer (`56037`) für alle Filialen
- Unterscheidung NUR über Lieferadresse-String
- Rechnung/Lieferschein → Welche Filiale? (Address-Matching fragil!)

### **Best Practice - 3 Architektur-Modelle**

#### **MODELL A: Separate Customers pro Filiale**

```
Hauptbetrieb: Customer #1 (hierarchyType = HEADQUARTER)
  ├─ Filiale Berlin:  Customer #2 (hierarchyType = FILIALE, parentCustomer = #1)
  ├─ Filiale München: Customer #3 (hierarchyType = FILIALE, parentCustomer = #1)
  └─ Filiale Hamburg: Customer #4 (hierarchyType = FILIALE, parentCustomer = #1)
```

**✅ Vorteile:**
- Separate Opportunities pro Filiale möglich
- Separate Umsatz-Tracking (Xentral-Sync einfach)
- Separate Contacts pro Filiale (Filialleiter)
- Lead→Customer Conversion klar (Lead = spezifische Filiale)

**❌ Nachteile:**
- Viele Customer-Einträge (100 Filialen = 101 Customers)
- Gesamt-Umsatz = Aggregation über alle Children
- Dashboard-Filter "Nur Hauptbetriebe" nötig

#### **MODELL B: Locations innerhalb Customer**

```
Customer #1 (Bäckerei Müller GmbH)
  ├─ Location "Zentrale Hamburg" (isMainLocation = true)
  ├─ Location "Filiale Berlin"
  ├─ Location "Filiale München"
  └─ Location "Filiale Köln"
```

**✅ Vorteile:**
- 1 Customer-Eintrag (übersichtlich)
- Gesamt-Umsatz einfach (Customer-Level)
- Dashboard-KPIs einfach (Anzahl Customers = Anzahl Unternehmen)

**❌ Nachteile:**
- ⚠️ **Opportunity OHNE Location-Link** (Migration nötig!)
- Umsatz-Tracking pro Filiale komplex (Address-Matching)
- Lead→Customer: Lead wird zu Haupt-Customer (nicht Filiale)

#### **MODELL C: HYBRID (EMPFOHLEN)**

**Phase 1 (Sprint 2.1.7.4 - AKTUELL):**
```java
// LeadConvertService - MINIMAL-SUPPORT
Customer customer = new Customer();
customer.setCompanyName(lead.companyName);
customer.setStatus(CustomerStatus.PROSPECT);

// Branch-Infos als Metadaten
customer.setBranchCount(lead.branchCount);
customer.setIsChain(lead.isChain);

// Hierarchy Type bestimmen
if (lead.isChain != null && lead.isChain) {
    customer.setHierarchyType(CustomerHierarchyType.HEADQUARTER);
} else {
    customer.setHierarchyType(CustomerHierarchyType.STANDALONE);
}

// ❌ KEIN automatisches Filial-Splitting
// ❌ KEIN Opportunity→Location Link
```

**Phase 2 (Sprint 2.1.7.7 - SPÄTER):**
- "Filialen verwalten" UI (Customer-DetailPage)
- "Neue Filiale anlegen" (Separate Customer ODER Location)
- Opportunity→Location Link (Migration + Field)
- Xentral-Filial-Mapping (Address-Matching-Service)

### **Lösung für Sprint 2.1.7.4**

**✅ MINIMAL-SUPPORT (EMPFOHLEN):**

**Was implementieren:**
1. ✅ Lead→Customer Conversion mit `hierarchyType`
2. ✅ `branchCount`, `isChain` kopieren (Metadaten)
3. ✅ `totalLocationsEU`, `locationsGermany` kopieren
4. ❌ **KEIN** automatisches Filial-Splitting
5. ❌ **KEIN** Opportunity→Location Link

**User-Workflow (Interim):**
```
1. Lead erfassen: "Bäckerei Müller GmbH" (branchCount = 10, isChain = true)
2. Opportunity WON → Auto-Convert
3. Customer erstellt: hierarchyType = HEADQUARTER, branchCount = 10
4. SPÄTER: Vertriebler kann manuell Filialen anlegen (Sprint 2.1.7.7)
```

**Offene Punkte (für Sprint 2.1.7.7):**
1. **Filial-Anlage UI:** Wie legt User Filialen an? (Separate Customer oder Location?)
2. **Opportunity→Filiale:** Migration `opportunity.location_id` hinzufügen?
3. **Xentral-Sync:** Address-Matching-Service (Lieferadresse → Location)
4. **Umsatz-Tracking:** Pro Filiale oder aggregiert?

### **Empfehlung: Sprint 2.1.7.7 anlegen**

**Sprint 2.1.7.7: Multi-Location Management & Xentral Filial-Sync**

**Scope:**
- Filial-Anlage UI (Customer-DetailPage)
- Entscheidung: Separate Customer vs. Location
- Opportunity→Location Link (Migration + UI)
- Xentral Address-Matching-Service
- Umsatz-Tracking pro Filiale

**Effort:** 12-15h (2 Tage)

**Dependencies:**
- Sprint 2.1.7.4 COMPLETE (hierarchyType Foundation)
- Sprint 2.1.7.2 COMPLETE (Xentral Integration)

---

## 🛠️ TECHNICAL DEBT

### **Temporäre Lösungen (werden in Folge-Sprints ersetzt)**

1. **Manual Activation Button (Sprint 2.1.7.4)**
   - Temporär: Vertriebler klickt Button
   - Ziel: Xentral Webhook (Sprint 2.1.7.2)
   - Begründung: Sofort einsetzbar, kein Blocker

2. **Seasonal Business Basic UI (Sprint 2.1.7.4)**
   - Temporär: Simple Indicator (Alert)
   - Ziel: Advanced UI mit MonthPicker (Sprint 2.1.7.6)
   - Begründung: Foundation zuerst, UI später

3. **XentralOrderEventHandler Mock (Sprint 2.1.7.4)**
   - Temporär: Mock Implementation (logs only)
   - Ziel: Real Webhook (Sprint 2.1.7.2)
   - Begründung: Interface definieren, Implementierung später

### **Keine Technical Debt**

Diese Features bleiben wie implementiert:
- CustomerStatus Enum (LEAD entfernt)
- Auto-Conversion bei Opportunity WON
- ChurnDetectionService (shouldCheckForChurn)
- Dashboard Metrics (PROSPECT, seasonalActive, seasonalPaused)

---

## 🔗 RELATED DOCUMENTATION

**Technical Specification:**
→ `/docs/planung/artefakte/SPEC_SPRINT_2_1_7_4_TECHNICAL.md`

**Trigger Document:**
→ `/docs/planung/TRIGGER_SPRINT_2_1_7_4.md`

**Design System:**
→ `/docs/planung/grundlagen/DESIGN_SYSTEM.md`

**Related Sprints:**
- Sprint 2.1.7.3: Customer → Opportunity Workflow (COMPLETE)
- Sprint 2.1.7.6: Customer Lifecycle Management (PROSPECT Lifecycle, Advanced Seasonal Patterns)
- Sprint 2.1.7.2: Xentral Integration (Automatic Activation via Webhook)

---

**✅ DESIGN DECISIONS STATUS: 📋 FINAL - User-Validated**

**Letzte Aktualisierung:** 2025-10-21 (Multi-Location Management Architektur hinzugefügt - DEFERRED zu Sprint 2.1.7.7)
