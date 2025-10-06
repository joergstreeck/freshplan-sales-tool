---
module: "02_neukundengewinnung"
doc_type: "konzept"
status: "active"
owner: "joergstreeck"
updated: "2025-10-06"
title: "Lead/Customer Field Harmonization - Best Practice Proposal"
sprint: "2.1.6 Phase 2 Follow-up"
priority: "high"
---

# Lead/Customer Field Harmonization - Best Practice Proposal

**Sprint:** 2.1.6 Phase 2 Follow-up
**Datum:** 2025-10-06
**Status:** Proposal
**Priority:** 🔴 HIGH (Code Hygiene & Wartbarkeit)

---

## 🎯 Ziel

**Single Source of Truth für Business-Klassifizierung:**
- Einheitliche Feldnamen zwischen Lead und Customer
- Keine hardcodierten Enum-Werte im Frontend
- Ein Enum für beide Entities (BusinessType statt Industry)
- Dynamisches Laden aller Dropdown-Werte vom Backend

---

## ❌ Aktuelles Problem

### Inkonsistente Feldnamen & Typen

**Lead Entity (2 Felder für dasselbe Konzept!):**
```java
public String industry;           // Zeile 84 - String (frei wählbar)
public String businessType;       // Zeile 88 - String (CHECK constraint auf 9 Werte seit V263)
```

**Customer Entity:**
```java
@Enumerated(EnumType.STRING)
private Industry industry;        // Zeile 55 - Industry Enum (9 alte Werte)
```

### Probleme

1. **Unterschiedliche Feldnamen:**
   - Lead: `businessType` (seit V263)
   - Customer: `industry`
   - Verwirrend beim Mapping Lead → Customer

2. **2 verschiedene Enums:**
   - Lead: Nutzt `BusinessType` (9 Werte: RESTAURANT, HOTEL, CATERING, KANTINE, GROSSHANDEL, LEH, BILDUNG, GESUNDHEIT, SONSTIGES)
   - Customer: Nutzt `Industry` (9 alte Werte: HOTEL, RESTAURANT, CATERING, KANTINE, GESUNDHEITSWESEN, BILDUNG, VERANSTALTUNG, EINZELHANDEL, SONSTIGE)

3. **Hardcoded Frontend-Werte:**
   - ✅ BusinessType: Jetzt dynamisch (V263)
   - ❌ LeadSource: 6 hardcodierte Werte (Backend-API existiert!)
   - ❌ KitchenSize: 3 hardcodierte Werte
   - ❌ FirstContactChannel: Nicht als Dropdown genutzt

4. **Lead hat 2 Felder:**
   - `industry` (String, frei) UND `businessType` (String, constraint)
   - Welches ist die Single Source of Truth?

---

## ✅ Best Practice Lösung

### Phase 1: Feldnamen-Harmonisierung (SOFORT)

**Ziel:** Gleiche Feldnamen = gleiche Bedeutung

#### Lead Entity
```java
// DEPRECATED: Feld entfernen (wird nicht genutzt)
@Deprecated
@Size(max = 50)
public String industry; // → LÖSCHEN in nächster Migration

// BEHALTEN: Umbenennen für Konsistenz
@Size(max = 100)
@Column(name = "business_type")
public String businessType; // → Bleibt als String mit CHECK constraint
```

**Vorgehen:**
1. Migration V264: `ALTER TABLE leads DROP COLUMN industry;` (wenn nicht genutzt)
2. Lead.java: `industry` Feld als `@Deprecated` markieren
3. Mapper prüfen: Nutzt irgendwo `lead.industry`? → Umstellen auf `businessType`

#### Customer Entity (SPÄTER - Breaking Change)
```java
// Option A: Parallel-Feld während Migration
@Enumerated(EnumType.STRING)
@Column(name = "industry") // Bleibt für Backward Compatibility
private Industry industry;

@Enumerated(EnumType.STRING)
@Column(name = "business_type") // NEU: Harmonisiert mit Lead
private BusinessType businessType;

// Option B: Migration mit Umbenennung
// 1. Migration: ADD COLUMN business_type
// 2. Migration: Daten kopieren: UPDATE customers SET business_type = industry
// 3. Migration: DROP COLUMN industry (nach Deprecation Phase)
```

---

### Phase 2: Frontend Hardcoding eliminieren (SOFORT - Quick Win!)

#### 2.1 LeadSource dynamisch laden

**Backend:** API existiert bereits! ✅
```java
GET /api/enums/lead-sources → [{value: "MESSE", label: "Messe"}, ...]
```

**Frontend:** Hook erstellen
```typescript
// File: useLeadSources.ts
export function useLeadSources() {
  return useQuery({
    queryKey: ['enums', 'leadSources'],
    queryFn: () => fetch('/api/enums/lead-sources').then(r => r.json()),
    staleTime: 5 * 60 * 1000,
  });
}

// LeadWizard.tsx (Lines 369-377): Ersetzen
const { data: sources, isLoading: isLoadingSources } = useLeadSources();

{sources?.map(s => (
  <MenuItem key={s.value} value={s.value}>{s.label}</MenuItem>
))}
```

**Aufwand:** 10 Minuten
**Impact:** Eliminiert 6 hardcodierte Werte

#### 2.2 KitchenSize dynamisch laden (Optional)

**Backend:** API fehlt noch
```java
// EnumResource.java
@GET
@Path("/kitchen-sizes")
public List<EnumValue> getKitchenSizes() {
  return List.of(
    new EnumValue("SMALL", "Klein"),
    new EnumValue("MEDIUM", "Mittel"),
    new EnumValue("LARGE", "Groß")
  );
}
```

**Frontend:**
```typescript
export function useKitchenSizes() {
  return useQuery({
    queryKey: ['enums', 'kitchenSizes'],
    queryFn: () => fetch('/api/enums/kitchen-sizes').then(r => r.json()),
    staleTime: 5 * 60 * 1000,
  });
}
```

**Aufwand:** 15 Minuten
**Impact:** Eliminiert 3 hardcodierte Werte

#### 2.3 FirstContactChannel API (Falls benötigt)

**Backend:** API fehlt
```java
@GET
@Path("/first-contact-channels")
public List<EnumValue> getFirstContactChannels() {
  return List.of(
    new EnumValue("MESSE", "Messestand/Event"),
    new EnumValue("PHONE", "Telefonat"),
    new EnumValue("EMAIL", "E-Mail"),
    new EnumValue("REFERRAL", "Empfehlung/Vorstellung"),
    new EnumValue("OTHER", "Sonstige")
  );
}
```

---

### Phase 3: BusinessType als Single Source of Truth (SPÄTER)

**Ziel:** Customer nutzt BusinessType statt Industry

#### Migration Strategy (Breaking Change!)

**Option A: Gradual Migration (EMPFOHLEN)**

1. **V264: Add business_type to customers**
   ```sql
   ALTER TABLE customers ADD COLUMN business_type VARCHAR(50);

   -- Daten konvertieren mit BusinessType.fromLegacyIndustry()
   UPDATE customers SET business_type =
     CASE industry
       WHEN 'HOTEL' THEN 'HOTEL'
       WHEN 'RESTAURANT' THEN 'RESTAURANT'
       WHEN 'CATERING' THEN 'CATERING'
       WHEN 'KANTINE' THEN 'KANTINE'
       WHEN 'GESUNDHEITSWESEN' THEN 'GESUNDHEIT'
       WHEN 'BILDUNG' THEN 'BILDUNG'
       WHEN 'VERANSTALTUNG' THEN 'SONSTIGES'
       WHEN 'EINZELHANDEL' THEN 'LEH'
       WHEN 'SONSTIGE' THEN 'SONSTIGES'
     END;
   ```

2. **Code Update:**
   ```java
   // Customer.java - Beide Felder parallel
   @Deprecated
   @Enumerated(EnumType.STRING)
   private Industry industry; // Legacy, später löschen

   @Enumerated(EnumType.STRING)
   @Column(name = "business_type")
   private BusinessType businessType; // Neue Single Source of Truth
   ```

3. **Deprecation Phase (1-2 Sprints):**
   - Alle Mapper/Services auf `businessType` umstellen
   - `industry` getter/setter als `@Deprecated` markieren
   - Tests anpassen

4. **V265: Drop industry column (nach Deprecation)**
   ```sql
   ALTER TABLE customers DROP COLUMN industry;
   ```

**Option B: Direct Migration (Riskanter)**
```sql
-- V264: Rename + Convert in one step
ALTER TABLE customers RENAME COLUMN industry TO business_type;
UPDATE customers SET business_type = /* conversion logic */;
ALTER TABLE customers ADD CONSTRAINT chk_business_type CHECK (business_type IN (...));
```

---

## 📋 Implementierungs-Roadmap

### Sofort (Phase 2.1.6 Nachbesserung)

| Task | Aufwand | Impact | Files |
|------|---------|--------|-------|
| **1. LeadSource dynamisch** | 10min | 🟢 HIGH | useLeadSources.ts, LeadWizard.tsx |
| **2. KitchenSize API + Hook** | 15min | 🟡 MEDIUM | EnumResource.java, useKitchenSizes.ts, LeadWizard.tsx |
| **3. Lead.industry deprecaten** | 5min | 🟢 HIGH | Lead.java, Mapper prüfen |

**Gesamt:** 30 Minuten → Alle Frontend-Hardcodings eliminiert ✅

### Später (Sprint 2.1.7 oder 2.2)

| Task | Aufwand | Impact | Risk |
|------|---------|--------|------|
| **4. Customer.businessType Migration** | 2-3h | 🔴 BREAKING | 🟡 MEDIUM |
| **5. Industry Enum deprecaten** | 1h | 🔴 BREAKING | 🟡 MEDIUM |
| **6. DROP industry column** | 30min | 🔴 BREAKING | 🟡 MEDIUM |

**Gesamt:** 3-5 Stunden → Vollständige Harmonisierung

---

## ✅ Vorteile der Best Practice Lösung

### Code Hygiene
- ✅ **NO Hardcoding:** Alle Werte vom Backend
- ✅ **Konsistenz:** Lead + Customer nutzen gleiche Feldnamen
- ✅ **Wartbarkeit:** Neue Werte nur im Backend hinzufügen
- ✅ **Single Source of Truth:** Ein Enum (BusinessType) für beide Entities

### Flexibilität
- ✅ Dropdown-Werte ändern ohne Frontend-Deployment
- ✅ Backend-API kann Werte pro Tenant/Konfiguration liefern
- ✅ Einfaches A/B Testing neuer Kategorien

### Debugging
- ✅ Keine Inkonsistenzen zwischen Frontend/Backend möglich
- ✅ API-Logs zeigen alle genutzten Werte
- ✅ Klare Mapping-Logik Lead → Customer

---

## 🚀 Empfehlung

**SOFORT umsetzen (30 Minuten):**
1. ✅ LeadSource dynamisch laden (Backend-API existiert!)
2. ✅ KitchenSize API erstellen + dynamisch laden
3. ✅ Lead.industry als @Deprecated markieren

**SPÄTER (Sprint 2.1.7):**
4. Customer.businessType Migration (Gradual Approach)
5. Industry Enum deprecaten
6. Vollständige Harmonisierung abschließen

**Begründung:**
- Phase 1 (30min) eliminiert ALLE Frontend-Hardcodings → Sofortiger Gewinn
- Phase 2 (3-5h) ist Breaking Change → Braucht eigenen Sprint für Testing

---

## 📊 Migration Impact Analysis

### Lead Entity
- ✅ **Kein Breaking Change:** businessType bleibt String mit CHECK constraint
- ✅ **industry Feld:** Löschen wenn ungenutzt (prüfen!)

### Customer Entity
- ⚠️ **Breaking Change:** industry → businessType (Type-Änderung!)
- ⚠️ **Rollback:** Möglich mit backup + conversion logic
- ⚠️ **Testing:** Integration Tests für alle Customer Services erforderlich

### Frontend
- ✅ **Kein Breaking Change:** Dropdowns nutzen API statt Hardcoding
- ✅ **Rollback:** API gibt alte Werte zurück → Frontend funktioniert

---

## 🔗 Referenzen

- **BusinessType Enum:** `/backend/src/main/java/de/freshplan/domain/shared/BusinessType.java`
- **Industry Enum (Legacy):** `/backend/src/main/java/de/freshplan/domain/customer/entity/Industry.java`
- **EnumResource API:** `/backend/src/main/java/de/freshplan/api/resources/EnumResource.java`
- **Migration V263:** `/backend/src/main/resources/db/migration/V263__add_business_type_constraint.sql`
- **Lead Entity:** `/backend/src/main/java/de/freshplan/modules/leads/domain/Lead.java`
- **Customer Entity:** `/backend/src/main/java/de/freshplan/domain/customer/entity/Customer.java`

---

**Nächste Schritte:**
1. Review dieses Proposals
2. Entscheidung: Sofort Phase 1 (30min) umsetzen?
3. Planning: Phase 2 in Sprint 2.1.7 einplanen?
