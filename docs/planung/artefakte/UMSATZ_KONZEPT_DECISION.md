# 💰 Umsatz-Konzept Entscheidung - FreshPlan Sales Tool

**Sprint:** 2.1.7.4 - Customer Status Architecture
**Datum:** 2025-10-20
**Status:** ✅ FINAL
**Owner:** Jörg + Claude
**Grund:** Lead-Kunden-Harmonisierung + Klarheit über Umsatzfelder

---

## 🎯 PROBLEM

**Verwirrung bei Umsatzfeldern:**
Im System existieren **4 verschiedene Umsatz-Felder** in 3 Entities:

| Entity | Feld | Quelle | Zweck | Status |
|--------|------|--------|-------|--------|
| **Lead** | `estimatedVolume` | Manuell (Vertriebler) | Qualifizierung | ✅ Aktiv |
| **Customer** | `estimatedVolume` | ❌ **NICHT kopiert!** | Sollte aus Lead kommen | ⚠️ Leer! |
| **Customer** | `expectedAnnualVolume` | Manuell | Prognose | ✅ Aktiv |
| **Customer** | `actualAnnualVolume` | 🔮 Xentral (später) | ECHTE ERP-Daten | 🔜 Sprint 2.1.7.2 |
| **Opportunity** | `expectedValue` | OpportunityMultiplier | DIESER Deal | ✅ Aktiv |

**Fragen:**
1. Warum hat Customer ZWEI Volumen-Felder? (`estimatedVolume` + `expectedAnnualVolume`)
2. Woher kommt `expectedAnnualVolume` wenn Lead nur `estimatedVolume` hat?
3. Wie hängt `Opportunity.expectedValue` mit Kundenvolumen zusammen?

---

## 💡 LÖSUNG: 3-Phasen-Modell

### **Phase 1: LEAD (Qualifizierung)**

```
┌─────────────────────────────────────┐
│ Lead.estimatedVolume                │
│ ────────────────────────────────    │
│ Quelle:  Vertriebler-Schätzung      │
│ Zweck:   Lead Scoring               │
│ Basis:   Pain Points, Deal Size     │
│ Beispiel: 80.000€ - 120.000€        │
└─────────────────────────────────────┘
```

**Wer füllt aus:** Vertriebler (während Lead-Qualifizierung)

**Basis der Schätzung:**
- BusinessType (RESTAURANT = niedriger, HOTEL = höher)
- KitchenSize (KLEIN/MITTEL/GROSS/SEHR_GROSS)
- EmployeeCount
- DealSize Enum (wenn vorhanden)

**Accuracy:** ±30% (grobe Schätzung)

---

### **Phase 2: CUSTOMER (Opportunity WON)**

```
┌─────────────────────────────────────┐
│ Customer.expectedAnnualVolume       │
│ ────────────────────────────────    │
│ Quelle:  Lead.estimatedVolume       │
│          (automatisch kopiert)      │
│ Zweck:   Opportunity-Kalkulation    │
│ Basis:   Lead-Schätzung             │
│ Beispiel: 100.000€                  │
└─────────────────────────────────────┘
              ↓ (Sprint 2.1.7.2)
┌─────────────────────────────────────┐
│ Customer.actualAnnualVolume         │
│ ────────────────────────────────    │
│ Quelle:  Xentral Legacy API         │
│          (SUM Rechnungen 12 Monate) │
│ Zweck:   ECHTE Daten für Prognose   │
│ Basis:   ERP-Rechnungen             │
│ Beispiel: 87.450€ (REAL!)           │
└─────────────────────────────────────┘
```

**Automatischer Flow (Sprint 2.1.7.4):**
```java
// LeadConvertService.convertToCustomer()
customer.setExpectedAnnualVolume(lead.estimatedVolume); // ✅ Automatisch!
customer.setActualAnnualVolume(null); // Noch NULL (erst nach Sprint 2.1.7.2)
```

**Xentral Sync (Sprint 2.1.7.2 - automatisch):**
```java
// Nightly Job: Xentral Invoice Sync
BigDecimal actualVolume = xentralApi.getInvoices(customerId, last12Months)
    .stream()
    .map(Invoice::getTotalAmount)
    .reduce(BigDecimal.ZERO, BigDecimal::add);

customer.setActualAnnualVolume(actualVolume); // ✅ Überschreibt expectedAnnualVolume!
```

**Wichtig:**
- `expectedAnnualVolume` = Initialer Wert aus Lead (kann manuell angepasst werden)
- `actualAnnualVolume` = Überschreibt expected sobald Xentral-Daten verfügbar

---

### **Phase 3: OPPORTUNITY (Verkaufschance)**

```
┌─────────────────────────────────────┐
│ Opportunity.expectedValue           │
│ ────────────────────────────────    │
│ Formel:  baseVolume × multiplier    │
│ Quelle:  OpportunityMultiplier      │
│ Basis:   Customer Jahresumsatz      │
│ Beispiel: 100k€ × 0.65 = 65.000€    │
└─────────────────────────────────────┘
```

**Automatische Berechnung (bereits implementiert in Sprint 2.1.7.3):**

```java
/**
 * OpportunityMultiplier Formel:
 * expectedValue = baseVolume × multiplier
 *
 * baseVolume Priorität:
 * 1. customer.actualAnnualVolume (BEST - Xentral ECHTE Daten)
 * 2. customer.expectedAnnualVolume (OK - Lead-Schätzung)
 * 3. 0 (FALLBACK - manuell eingeben)
 */

// Beispiel: HOTEL + SORTIMENTSERWEITERUNG
BigDecimal baseVolume = customer.getActualAnnualVolume() != null
    ? customer.getActualAnnualVolume()      // BEST: 87.450€ (Xentral)
    : customer.getExpectedAnnualVolume();   // OK: 100.000€ (Lead-Schätzung)

BigDecimal multiplier = OpportunityMultiplier.getMultiplierValue(
    "HOTEL",                    // BusinessType
    "SORTIMENTSERWEITERUNG",    // OpportunityType
    BigDecimal.valueOf(0.65)    // Default 65%
); // Returns: 0.65

BigDecimal expectedValue = baseVolume.multiply(multiplier);
// Result: 87.450€ × 0.65 = 56.842€ (wenn Xentral-Daten verfügbar)
// OR: 100.000€ × 0.65 = 65.000€ (wenn nur Lead-Schätzung)
```

**OpportunityType Multipliers (Beispiel-Matrix):**

| BusinessType | NEUGESCHAEFT | SORTIMENTSERWEITERUNG | NEUER_STANDORT | VERLAENGERUNG |
|--------------|--------------|------------------------|----------------|----------------|
| **RESTAURANT** | 1.00 (100%) | 0.15 (15%) | 0.40 (40%) | 0.75 (75%) |
| **HOTEL** | 1.00 (100%) | 0.65 (65%) | 0.90 (90%) | 0.85 (85%) |
| **CATERING** | 1.00 (100%) | 0.35 (35%) | 0.60 (60%) | 0.70 (70%) |
| **KANTINE** | 1.00 (100%) | 0.25 (25%) | 0.50 (50%) | 0.90 (90%) |

**Warum unterschiedlich?**
- **RESTAURANT:** Kleine Betriebe, Sortimentserweiterung = klein (15%)
- **HOTEL:** Große Betriebe, Sortimentserweiterung = signifikant (65%)
- **NEUER_STANDORT:** Hotel-Expansion = fast voller Umsatz (90%)
- **VERLAENGERUNG:** Verlängerung = meist 70-90% des Vorjahres

---

## 🔄 COMPLETE DATA FLOW

```
┌──────────────────────────────────────────────────────────────┐
│ 1. LEAD QUALIFIZIERUNG                                       │
│    Vertriebler schätzt: estimatedVolume = 100.000€          │
└──────────────────────────────────────────────────────────────┘
                       ↓
┌──────────────────────────────────────────────────────────────┐
│ 2. OPPORTUNITY WON → AUTO-CONVERT                           │
│    Lead → Customer (PROSPECT)                                │
│    estimatedVolume → expectedAnnualVolume (automatisch!)     │
└──────────────────────────────────────────────────────────────┘
                       ↓
┌──────────────────────────────────────────────────────────────┐
│ 3. ERSTE BESTELLUNG GELIEFERT                                │
│    Customer: PROSPECT → AKTIV                                │
│    expectedAnnualVolume = 100.000€ (aus Lead)               │
│    actualAnnualVolume = NULL (noch keine Rechnungen)        │
└──────────────────────────────────────────────────────────────┘
                       ↓
┌──────────────────────────────────────────────────────────────┐
│ 4. XENTRAL SYNC (Sprint 2.1.7.2 - Nightly Job)             │
│    SUM(invoices_last_12_months) = 87.450€                   │
│    actualAnnualVolume = 87.450€ (überschreibt expected!)    │
└──────────────────────────────────────────────────────────────┘
                       ↓
┌──────────────────────────────────────────────────────────────┐
│ 5. NEUE OPPORTUNITY ERSTELLEN                                │
│    OpportunityType: SORTIMENTSERWEITERUNG                    │
│    baseVolume = 87.450€ (actualAnnualVolume BEVORZUGT!)     │
│    multiplier = 0.65 (OpportunityMultiplier Matrix)         │
│    expectedValue = 56.842€ (automatisch berechnet!)         │
└──────────────────────────────────────────────────────────────┘
```

---

## 📋 DESIGN DECISIONS

### **Decision 1: estimatedVolume → expectedAnnualVolume kopieren**

**Entscheidung:** ✅ JA - Automatisch bei Lead→Customer Conversion

**Rationale:**
- Lead-Schätzung ist wertvoll (Vertriebler kennt den Kunden)
- Ohne Kopieren: Datenverlust
- Mit Kopieren: Basis für Opportunity-Kalkulation sofort verfügbar

**Implementation (Sprint 2.1.7.4):**
```java
// LeadConvertService.convertToCustomer()
if (lead.estimatedVolume != null) {
    customer.setExpectedAnnualVolume(lead.estimatedVolume);
}
```

---

### **Decision 2: Customer.estimatedVolume Feld entfernen?**

**Entscheidung:** ❌ NEIN - Feld bleibt, aber wird NICHT genutzt

**Rationale:**
- **Backend-Parity:** Feld existiert (V10032), Entfernen = Breaking Change
- **Frontend:** Nicht anzeigen (verwirrt User)
- **Später:** Vielleicht anderer Use-Case (z.B. Abweichung Schätzung vs. Real)

**Action:**
- Feld bleibt in DB
- Wird NICHT kopiert (expectedAnnualVolume ist das neue Feld)
- Kann später genutzt werden für "Schätzungs-Accuracy" Analyse

---

### **Decision 3: actualAnnualVolume überschreibt expectedAnnualVolume?**

**Entscheidung:** ⚠️ NEIN - Beide Felder parallel

**Rationale:**
- `expectedAnnualVolume` = Vertriebler-Prognose (manuell anpassbar)
- `actualAnnualVolume` = Xentral-Realität (read-only)
- **Use Case:** Vertriebler sieht Abweichung:
  - Expected: 100k€ (optimistische Schätzung)
  - Actual: 50k€ (Realität ist niedriger!)
  - → Nachjustierung von Opportunities nötig

**Best Practice:**
```java
// Opportunity Calculation Priority:
BigDecimal baseVolume = customer.getActualAnnualVolume() != null
    ? customer.getActualAnnualVolume()      // PREFER: Real data
    : customer.getExpectedAnnualVolume();   // FALLBACK: Estimate
```

---

### **Decision 4: OpportunityMultiplier automatisch nutzen?**

**Entscheidung:** ✅ JA - Automatische Vorschlag, manuell überschreibbar

**Rationale:**
- Vertriebler bekommt **intelligenten Vorschlag** (basierend auf Matrix)
- Kann manuell anpassen (jeder Deal ist anders)
- **UX:** "Vorgeschlagener Wert: 56.842€ (basierend auf Jahresumsatz 87.450€ × 0.65)"

**Implementation (bereits in Sprint 2.1.7.3):**
- OpportunityMultiplier Entity ✅
- OpportunityMultiplierService ✅
- Admin UI für Matrix-Konfiguration ✅

**TODO (Sprint 2.1.7.4 - validieren!):**
- Prüfen ob OpportunityService automatisch berechnet
- Falls NEIN: Aktivieren in diesem Sprint

---

## 🎯 SPRINT 2.1.7.4 ACTIONS

### **1. LeadConvertService erweitern**

```java
// VORHER (aktuell):
customer.setCompanyName(lead.companyName);
customer.setStatus(CustomerStatus.AKTIV); // ❌ FALSCH!
// ... nur 3 Felder kopiert

// NACHHER (Sprint 2.1.7.4):
customer.setCompanyName(lead.companyName);
customer.setStatus(CustomerStatus.PROSPECT); // ✅ KORREKT!

// Umsatz-Felder
if (lead.estimatedVolume != null) {
    customer.setExpectedAnnualVolume(lead.estimatedVolume); // ✅ NEU!
}

// Business-Felder (100% Parity)
customer.setKitchenSize(lead.kitchenSize);
customer.setEmployeeCount(lead.employeeCount);
customer.setBranchCount(lead.branchCount);
customer.setIsChain(lead.isChain);
customer.setBusinessType(lead.businessType);

// Pain Scoring (8 Boolean Felder)
customer.setPainStaffShortage(lead.painStaffShortage);
customer.setPainHighCosts(lead.painHighCosts);
customer.setPainFoodWaste(lead.painFoodWaste);
customer.setPainQualityInconsistency(lead.painQualityInconsistency);
customer.setPainTimePressure(lead.painTimePressure);
customer.setPainSupplierQuality(lead.painSupplierQuality);
customer.setPainUnreliableDelivery(lead.painUnreliableDelivery);
customer.setPainPoorService(lead.painPoorService);
customer.setPainNotes(lead.painNotes);
```

---

### **2. OpportunityService validieren**

**Prüfen:** Nutzt OpportunityService bereits OpportunityMultiplier?

**Falls NEIN:** Aktivieren!

```java
// OpportunityService.createOpportunity()
if (request.getExpectedValue() == null && customer.getActualAnnualVolume() != null) {
    // Auto-calculate using OpportunityMultiplier
    BigDecimal baseVolume = customer.getActualAnnualVolume() != null
        ? customer.getActualAnnualVolume()
        : customer.getExpectedAnnualVolume();

    if (baseVolume != null && request.getOpportunityType() != null) {
        BigDecimal multiplier = OpportunityMultiplier.getMultiplierValue(
            customer.getBusinessType().toString(),
            request.getOpportunityType().toString(),
            BigDecimal.ONE // Fallback: 100%
        );

        opportunity.setExpectedValue(baseVolume.multiply(multiplier));
        opportunity.setDescription(
            "Vorgeschlagener Wert: " + formatCurrency(opportunity.getExpectedValue()) +
            " (basierend auf Jahresumsatz " + formatCurrency(baseVolume) +
            " × " + multiplier + ")"
        );
    }
}
```

---

## 📊 USER STORIES

### **User Story 1: Vertriebler schätzt Lead-Volumen**

```
Als Vertriebler
möchte ich beim Lead das geschätzte Jahresvolumen eingeben
damit ich später bei der Opportunity realistische Werte habe

Akzeptanzkriterien:
- Lead.estimatedVolume Feld verfügbar ✅
- Wird automatisch zu Customer.expectedAnnualVolume kopiert ✅
- Keine Datenverluste bei Conversion ✅
```

---

### **User Story 2: Opportunity mit intelligentem Vorschlag**

```
Als Vertriebler
möchte ich beim Erstellen einer Opportunity einen intelligenten Wert-Vorschlag
basierend auf dem Kundenvolumen und Opportunity-Typ
damit ich nicht raten muss

Akzeptanzkriterien:
- System schlägt expectedValue vor (baseVolume × multiplier) ✅
- Ich kann den Wert manuell anpassen ✅
- Ich sehe die Berechnungsgrundlage ("basierend auf...") ✅
```

---

### **User Story 3: Xentral-Sync aktualisiert Volumen**

```
Als Vertriebsleiter
möchte ich sehen was der Kunde WIRKLICH umsetzt (nicht nur Schätzung)
damit ich realistische Prognosen machen kann

Akzeptanzkriterien:
- actualAnnualVolume wird aus Xentral gezogen (Sprint 2.1.7.2) 🔜
- expectedAnnualVolume bleibt als Referenz (Vergleich) ✅
- Dashboard zeigt Abweichung (Optional - Sprint 2.1.7.6) 🔜
```

---

## 🔗 RELATED DOCUMENTATION

**Sprint Dokumentation:**
- TRIGGER_SPRINT_2_1_7_4.md
- SPEC_SPRINT_2_1_7_4_TECHNICAL.md
- SPEC_SPRINT_2_1_7_4_DESIGN_DECISIONS.md

**Related Sprints:**
- Sprint 2.1.7.2: Xentral Integration (actualAnnualVolume Sync)
- Sprint 2.1.7.3: OpportunityMultiplier Matrix (already implemented)

**Xentral API:**
- XENTRAL_API_INFO.md (Legacy API Rechnungs-Sync)

---

## ✅ CHECKLIST - Sprint 2.1.7.4

**LeadConvertService:**
- [ ] Status AKTIV → PROSPECT ändern
- [ ] estimatedVolume → expectedAnnualVolume kopieren
- [ ] kitchen_size, employee_count, branch_count, is_chain kopieren
- [ ] businessType kopieren
- [ ] Pain Scoring (8 Felder) kopieren
- [ ] Tests: Feldübernahme validieren (8 neue Tests)

**OpportunityService:**
- [ ] Validieren: Nutzt bereits OpportunityMultiplier?
- [ ] Falls NEIN: Aktivieren (Auto-Calculation)
- [ ] Frontend: Vorschlag anzeigen ("basierend auf...")

**Dokumentation:**
- [x] UMSATZ_KONZEPT_DECISION.md (dieses Dokument)
- [ ] DESIGN_DECISIONS erweitern
- [ ] Code-Kommentare aktualisieren

---

**Status:** ✅ FINAL - Ready für Implementation
**Letzte Aktualisierung:** 2025-10-20 (Initial Creation)
**Owner:** Jörg + Claude
