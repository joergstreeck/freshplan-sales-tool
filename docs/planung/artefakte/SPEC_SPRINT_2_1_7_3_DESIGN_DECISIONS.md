# Sprint 2.1.7.3 - Design Decisions

**Sprint-ID:** 2.1.7.3
**Created:** 2025-10-19
**Status:** ✅ FINAL
**Owner:** Claude + Jörg (User Validation)

---

## 📋 ÜBERSICHT

Dieses Dokument konsolidiert **alle Architektur- und Design-Entscheidungen** für Sprint 2.1.7.3.

**Hauptentscheidungen:**
1. Business-Type-Matrix für Intelligente Schätzung
2. 3-Tier Fallback für Base Volume (Xentral → Lead → Manual)
3. Settings-System (Simplified Architecture - OHNE Territory-Overrides)
4. Admin Route (/admin/settings/opportunities)
5. Audit-Log (JA - created_by, updated_by)
6. OpportunityType als VARCHAR (harmonisiert)

---

## 1️⃣ Business-Type-Matrix für Intelligente Schätzung ⭐

### **Problem**
Wie schätzen wir `expectedValue` für neue Opportunities aus bestehenden Kunden?

### **Lösung**
Business-Type-Matrix mit konfigurierbaren Multiplikatoren.

### **Architektur**
```typescript
// Formel
expectedValue = baseVolume × multiplier[businessType][opportunityType]

// Beispiel: Restaurant mit Sortimentserweiterung
baseVolume = 50.000€ (actualAnnualVolume aus Xentral)
multiplier = 0.25 (RESTAURANT × SORTIMENTSERWEITERUNG)
expectedValue = 12.500€
```

### **Multiplier-Matrix (ALLE 9 BusinessTypes × 4 OpportunityTypes = 36 Einträge)**

| BusinessType | NEUGESCHAEFT | SORTIMENTSERWEITERUNG | NEUER_STANDORT | VERLAENGERUNG |
|--------------|--------------|------------------------|----------------|---------------|
| **RESTAURANT**   | 100%     | **25%**                | 80%            | 95%           |
| **HOTEL**        | 100%     | **65%**                | 90%            | 90%           |
| **CATERING**     | 100%     | **50%**                | 75%            | 85%           |
| **KANTINE**      | 100%     | **30%**                | 60%            | 95%           |
| **BILDUNG**      | 100%     | **20%**                | 50%            | 90%           |
| **GESUNDHEIT**   | 100%     | **35%**                | 70%            | 95%           |
| **GROSSHANDEL**  | 100%     | **40%**                | 55%            | 80%           |
| **LEH**          | 100%     | **45%**                | 60%            | 85%           |
| **SONSTIGES**    | 100%     | **15%**                | 40%            | 70%           |

### **Begründung Branchenlogik**

**Gastro/Hotellerie (Haupt-Zielgruppe):**
- **Hotel (65%):** Höchstes Potential (große Küchen, mehrere Standorte, hohe Budgets)
- **Catering (50%):** Mittleres Potential (Event-basiert, saisonales Wachstum)
- **Restaurant (25%):** Konservativ (Einzelbetriebe, kleinere Budgets)

**Institutionell:**
- **Gesundheit (35%):** Krankenhäuser, Pflegeheime, hohe Standards
- **Kantine (30%):** Betriebsküchen, stabile Verträge
- **Bildung (20%):** Schulen/Unis, enge Budgets, lange Entscheidungswege

**B2B-Handel:**
- **LEH (45%):** Lebensmitteleinzelhandel (größere Ketten, mittleres Wachstum)
- **Großhandel (40%):** B2B-Intermediäre (hohe Volumen, aber weniger Sortimentswachstum)

**Sonstiges (15%):**
- Konservativ (unbekannte Branche, niedrigstes Risiko)

### **Warum diese Entscheidung?**
- ✅ **Datenbasiert statt Bauchgefühl** - Verkäufer müssen nicht raten
- ✅ **Business-Type-spezifisch** - Hotel ≠ Restaurant (unterschiedliche Wachstumspotenziale)
- ✅ **OpportunityType-spezifisch** - Sortimentserweiterung ≠ Neuer Standort
- ✅ **Konfigurierbar** - Settings in Datenbank (nicht hardcoded)

### **Alternativen (verworfen)**
- ❌ **One-Size-Fits-All Multiplier (15%):** Zu ungenau, ignoriert Branchenunterschiede
- ❌ **Hardcoded Matrix:** Nicht wartbar, Änderungen erfordern Deployment
- ❌ **AI-basierte Schätzung:** Zu komplex, braucht Trainingsdaten (später möglich)

---

## 2️⃣ 3-Tier Fallback für Base Volume 🎯

### **Problem**
Woher kommt `baseVolume` für die Multiplier-Berechnung?

### **Lösung**
Priorisierte Fallback-Strategie mit 3 Stufen.

### **Implementierung**
```typescript
function getBaseVolume(customer: Customer): number {
  // TIER 1: Xentral Actual Revenue (BESTE Quelle!)
  if (customer.actualAnnualVolume && customer.actualAnnualVolume > 0) {
    return customer.actualAnnualVolume; // ← Echte Umsatzdaten (Sprint 2.1.7.2)
  }

  // TIER 2: Lead Estimation (OK für neue Kunden)
  if (customer.expectedAnnualVolume && customer.expectedAnnualVolume > 0) {
    return customer.expectedAnnualVolume; // ← Schätzung aus Lead-Erfassung
  }

  // TIER 3: Manual Entry (Fallback)
  return 0; // ← Verkäufer muss manuell schätzen
}
```

### **Warum das brillant ist** ⭐⭐⭐⭐⭐
- ✅ **Xentral-Daten sind präziseste Quelle** (echte Umsätze statt Schätzungen)
- ✅ **Territoriale Unterschiede automatisch enthalten** (Hamburg-Restaurant ≠ München-Restaurant)
- ✅ **Eliminiert komplexe Territory-Override-Logik**
- ✅ **Nur beim Lead-Erstkontakt "Bauchgefühl"** - danach immer datenbasiert
- ✅ **Zukunftssicher** - je länger Kunde dabei, desto genauer die Schätzungen

### **User-Einsicht (2025-10-19)** 💡
> "wie wäre es denn wenn wir die echten Umsatzzahlen aus Xentral zur weiteren Schätzung heranziehen.
> Die sind doch am genauesten. So haben wir nur beim Erstkontakt eine Schätzung aus dem bauch heraus.
> Darin wären dann auch automatisch die territorialen Unterschiede enthalten."

→ **Validiert!** Diese Strategie ist optimal. ✅

### **Alternativen (verworfen)**
- ❌ **Nur Lead-Schätzung:** Veraltet nach 6 Monaten, keine echten Umsatzdaten
- ❌ **Territory-spezifische Multiplier:** Zu komplex, Xentral-Daten lösen das bereits

---

## 3️⃣ Settings-System (Simplified Architecture) 🛠️

### **User-Entscheidung (2025-10-19)**
> "settings ohne Territory-Overrides. Alles andere machen wir in Sprint 2.1.7.3"

### **Scope**

**✅ IN SCOPE (Sprint 2.1.7.3):**
- Database Settings Table: `opportunity_multipliers`
- Backend READ-API: `GET /api/settings/opportunity-multipliers`
- Frontend Integration: Dialog lädt Multipliers beim Öffnen
- **Audit-Log: JA** (created_at, created_by, updated_at, updated_by)

**❌ OUT OF SCOPE:**
- Admin-UI → Modul 08 (Admin-Dashboard)
- Territory-Overrides → NICHT nötig (Xentral-Daten enthalten bereits territoriale Unterschiede!)
- Trigger für Audit-Log → Später (bei Admin-UI)

### **Warum KEIN Territory-Override?**

**User-Argument (korrekt!):**
> "Territory nicht nötig, weil auf Xentral Daten zugegriffen wird.
> Das sind echte Umsätze und beinhalten ja quasi die territorialen unterschiede."

**Logik:**
```
Xentral actualAnnualVolume = ECHTER territorialer Umsatz
├─ Hotel München: 500.000€/Jahr (Bayern-Preise)
├─ Hotel Berlin: 350.000€/Jahr (Berlin-Preise)
└─ Restaurant Landshut: 80.000€/Jahr (ländlich)

→ Multiplier × actualAnnualVolume berücksichtigt Territory automatisch!
→ Kein separates Territory-Override nötig! ✅
```

### **Database Schema**
```sql
CREATE TABLE opportunity_multipliers (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  business_type VARCHAR(50) NOT NULL,      -- RESTAURANT, HOTEL, CATERING, ...
  opportunity_type VARCHAR(50) NOT NULL,   -- NEUGESCHAEFT, SORTIMENTSERWEITERUNG, ...
  multiplier NUMERIC(5,2) NOT NULL,        -- 0.25, 0.65, 0.50

  -- Audit-Log (JA!)
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_by UUID REFERENCES users(id),
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by UUID REFERENCES users(id),

  UNIQUE(business_type, opportunity_type)
);
```

### **Änderungen (temporär via SQL)**
```sql
-- Beispiel: RESTAURANT SORTIMENTSERWEITERUNG von 25% auf 30% erhöhen
UPDATE opportunity_multipliers
SET multiplier = 0.30,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = '...'  -- User-ID
WHERE business_type = 'RESTAURANT'
  AND opportunity_type = 'SORTIMENTSERWEITERUNG';
```

### **Alternativen (verworfen)**
- ❌ **Hardcoded Multipliers:** Nicht wartbar, Deployment bei jeder Änderung
- ❌ **Territory-Overrides jetzt:** Over-Engineering, Xentral löst das bereits
- ❌ **Enum-based Storage:** Weniger flexibel, VARCHAR erlaubt einfachere Extension

---

## 4️⃣ Admin Route Empfehlung 🗺️

### **Entscheidung**
Route: `/admin/settings/opportunities`

### **Bestehende Struktur**
```
/admin
  /system (SystemDashboard - technische Settings)
  /integrations (IntegrationsDashboard - Xentral, APIs)
  /settings (Placeholder - Business Settings!) ← HIER!
  /users (UserManagement)
  /audit (AuditLog)
```

### **Begründung**
- ✅ **Business Settings** (nicht System/Technical) - Multiplier = Business-Regel
- ✅ **Konsistent** mit /admin/settings Kategorie
- ✅ **Erweiterbar** - später: /admin/settings/territories, /admin/settings/products

### **Alternative (verworfen)**
- ❌ `/admin/system/opportunities` - Würde suggerieren dass es Technical-Config ist

### **Implementierung in Sprint 2.1.7.3?**
→ **NEIN!** Nur Backend READ-API. Admin-UI kommt in Modul 08.

---

## 5️⃣ OpportunityType als VARCHAR (Harmonisiert) ✅

### **User-Entscheidung**
> "Das sollte schon alles harmonisiert sein, wir verwenden VARCHAR"

### **Bedeutung**
- ✅ `CreateOpportunityForCustomerRequest.opportunityType` ist **String** (nicht Enum!)
- ✅ Backend konvertiert String → Enum intern
- ✅ Frontend sendet String-Values ("NEUGESCHAEFT", "SORTIMENTSERWEITERUNG", ...)
- ✅ Keine separaten Enum-Typen in DTO (Harmonisierung mit Lead-Conversion-Flow)

### **Bereits umgesetzt in Sprint 2.1.7.1**
```java
// CreateOpportunityForCustomerRequest.java
public class CreateOpportunityForCustomerRequest {
  private String opportunityType;  // ← VARCHAR, not OpportunityType Enum!
}

// OpportunityService.java
OpportunityType type = OpportunityType.valueOf(request.getOpportunityType());
```

**Keine Änderungen nötig!** ✅

---

## 6️⃣ Scope-Update: 16h → 30-31h 📊

### **Ursprünglicher Scope**
- 16h (2 Arbeitstage)
- Nur Customer → Opportunity Flow + Historie

### **Erweiterter Scope (mit Business-Type-Matrix + Settings)**
- **30-31h** (3-4 Arbeitstage)

### **Neue Deliverables**
- ✅ Migration V10031: `opportunity_multipliers` Tabelle (1h)
- ✅ Backend: OpportunityMultiplier Entity + Service (2h)
- ✅ Backend: GET /api/settings/opportunity-multipliers (1h)
- ✅ Frontend: Dialog lädt Multipliers + berechnet expectedValue (2h)
- ✅ Testing: Business-Type-Matrix Logic (2h)
- ✅ Testing: 3-Tier Fallback Strategie (1h)

**Total Overhead:** +14h (16h → 30h)

### **Business Value Upgrade**
- ✅ **Datengetriebene Schätzung** statt Bauchgefühl
- ✅ **Xentral-Integration vorbereitet** (Tier 1 Fallback ready)
- ✅ **Zukunftssicher** (Settings anpassbar ohne Code-Änderung)
- ✅ **Territoriale Unterschiede automatisch** (in Xentral-Daten enthalten)

---

## 7️⃣ Audit-Log für Multiplier-Änderungen ✅

### **User-Klarstellung (2025-10-19)**
> "Audit ja. Territory nicht nötig, weil auf Xentral Daten zugegriffen wird."

### **Entscheidung**
✅ **Audit-Log JA** - created_at, created_by, updated_at, updated_by

### **Begründung**
- ✅ **Nachvollziehbarkeit:** Wer hat welche Multiplier wann geändert?
- ✅ **Compliance:** Enterprise-Standard für Settings-Änderungen
- ✅ **Debugging:** Bei falschen Schätzungen: "Wann wurde der Multiplier geändert?"

### **Implementierung**
```sql
-- In opportunity_multipliers Table:
created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
created_by UUID REFERENCES users(id),
updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_by UUID REFERENCES users(id)
```

### **Trigger (SPÄTER in Modul 08)**
```sql
-- Wird in Admin-UI Sprint implementiert:
CREATE TRIGGER trg_opportunity_multipliers_audit
  AFTER UPDATE ON opportunity_multipliers
  FOR EACH ROW
  EXECUTE FUNCTION log_setting_change();
```

---

## 📝 ZUSAMMENFASSUNG

**Was ändert sich (VORHER → NACHHER)?**

| Aspekt | VORHER (Original TRIGGER) | NACHHER (mit Entscheidungen) |
|--------|---------------------------|------------------------------|
| **Schätzung** | Manuell eingeben | Business-Type-Matrix (36 Multipliers) |
| **Base Volume** | expectedAnnualVolume (statisch) | 3-Tier Fallback (Xentral > Lead > Manual) |
| **Territoriale Unterschiede** | Nicht berücksichtigt | Automatisch in Xentral-Daten |
| **Settings** | Keine | Database-driven (konfigurierbar) |
| **Audit** | Nein | Ja (created_by, updated_by) |
| **Territory-Overrides** | - | NICHT nötig (Xentral löst das!) |
| **Aufwand** | 16h | 30-31h |

**Business Value Upgrade:**
- ✅ **Datengetriebene Schätzung** statt Bauchgefühl
- ✅ **Xentral-Integration vorbereitet** (Tier 1 Fallback ready)
- ✅ **Zukunftssicher** (Settings anpassbar ohne Code-Änderung)
- ✅ **ALLE 9 BusinessTypes unterstützt**

---

**Letzte Aktualisierung:** 2025-10-19
**Status:** ✅ FINAL (User-Validiert)
