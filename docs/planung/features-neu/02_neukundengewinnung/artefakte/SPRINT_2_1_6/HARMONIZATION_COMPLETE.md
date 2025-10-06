# BusinessType Harmonization - Complete Documentation

**Sprint:** 2.1.6 Phase 2
**Date:** 2025-10-06
**Status:** ✅ COMPLETE

---

## 📋 Overview

Complete harmonization of business type classification across Lead and Customer entities. Eliminates all frontend hardcoding and establishes a Single Source of Truth pattern.

---

## ✅ What Was Accomplished

### 1. Backend Harmonization

#### Migration V263: Lead BusinessType
- ✅ Added `leads.business_type` column (VARCHAR(30))
- ✅ CHECK constraint for 9 valid values
- ✅ Lead.industry marked @Deprecated
- ✅ Index created for query performance

#### Migration V264: Customer BusinessType
- ✅ Added `customers.business_type` column (VARCHAR(30))
- ✅ Data migration: Industry → BusinessType (all 9 values mapped)
- ✅ CHECK constraint matching Lead values
- ✅ Customer.industry marked @Deprecated
- ✅ Auto-sync between industry ↔ businessType in setters

**Industry → BusinessType Mapping:**
```
HOTEL           → HOTEL
RESTAURANT      → RESTAURANT
CATERING        → CATERING
KANTINE         → KANTINE
GESUNDHEITSWESEN → GESUNDHEIT
BILDUNG         → BILDUNG
EINZELHANDEL    → LEH (Lebensmitteleinzelhandel)
VERANSTALTUNG   → SONSTIGES
SONSTIGE        → SONSTIGES
```

### 2. Shared BusinessType Enum

**Location:** `/backend/src/main/java/de/freshplan/domain/shared/BusinessType.java`

**9 Unified Values:**
1. RESTAURANT - Restaurant und Gastronomie
2. HOTEL - Hotel und Beherbergung
3. CATERING - Catering und Event-Catering
4. KANTINE - Kantine/Betriebsgastronomie
5. GROSSHANDEL - Großhandel
6. LEH - Lebensmitteleinzelhandel
7. BILDUNG - Bildungseinrichtungen
8. GESUNDHEIT - Gesundheitswesen
9. SONSTIGES - Sonstiges

**Features:**
- ✅ Display names for UI (German)
- ✅ `fromString()` converter (case-insensitive)
- ✅ `fromLegacyIndustry()` migration helper
- ✅ `toLegacyIndustry()` backward compatibility

### 3. Frontend Harmonization

#### EnumResource Endpoints
```
GET /api/enums/business-types  → 9 values (RESTAURANT, HOTEL, ...)
GET /api/enums/lead-sources     → 6 values (MESSE, EMPFEHLUNG, ...)
GET /api/enums/kitchen-sizes    → 3 values (small, medium, large)
```

#### React Hooks (Single Source of Truth)
- ✅ `useBusinessTypes()` - Fetch business types from backend
- ✅ `useLeadSources()` - Fetch lead sources from backend
- ✅ `useKitchenSizes()` - Fetch kitchen sizes from backend
- All use React Query with 5min cache (gcTime: 10min)

#### Lead Forms Updated
- ✅ LeadWizard.tsx: All 3 dropdowns load dynamically
- ✅ No hardcoded BusinessType values
- ✅ No hardcoded LeadSource values
- ✅ No hardcoded KitchenSize values
- ✅ Loading states with CircularProgress

#### Customer Types Updated
- ✅ CustomerListItem: Added `businessType` field
- ✅ CustomerSearchCriteria: Added `businessType` filter
- ✅ GetFieldDefinitionsRequest: Added `businessType` parameter
- ✅ All `industry` fields marked @deprecated
- ✅ Backward compatibility maintained

---

## 🌐 API Contract

### Enum Endpoints

**Pattern:** `GET /api/enums/{enum-name}`

**Available Endpoints:**
- `GET /api/enums/business-types` → BusinessType values
- `GET /api/enums/lead-sources` → LeadSource values
- `GET /api/enums/kitchen-sizes` → KitchenSize values

**Response Format:**
```json
[
  {
    "value": "RESTAURANT",
    "label": "Restaurant und Gastronomie"
  },
  {
    "value": "HOTEL",
    "label": "Hotel und Beherbergung"
  }
]
```

**Sorting:** Backend controls order via Enum ordinal (RESTAURANT first, SONSTIGES last)

**Caching:** Frontend uses React Query (staleTime: 5min, gcTime: 10min)

---

## 📐 Value Normalization Policy

**Casing Rules:**

| Enum Type | Casing | Rationale | Example Values |
|-----------|--------|-----------|----------------|
| **BusinessType** | UPPERCASE | German B2B terms, technical identifiers | `RESTAURANT`, `HOTEL`, `KANTINE` |
| **LeadSource** | UPPERCASE | German acquisition channels, business domain | `MESSE`, `EMPFEHLUNG`, `KALTAKQUISE` |
| **KitchenSize** | lowercase | International standard sizes, technical descriptors | `small`, `medium`, `large` |

**Implementation:**
- Backend enums use the defined casing
- Frontend receives exact values from API (no transformation)
- Database CHECK constraints match enum casing
- Case-insensitive matching used in legacy compatibility layers only

---

## 🏗️ Architecture

### Single Source of Truth Pattern

**Before (Hardcoded):**
```typescript
// ❌ Frontend hardcoding - BAD
<MenuItem value="RESTAURANT">Restaurant</MenuItem>
<MenuItem value="HOTEL">Hotel</MenuItem>
```

**After (Dynamic):**
```typescript
// ✅ Single Source of Truth - GOOD
const { data: businessTypes } = useBusinessTypes();
businessTypes?.map(type => (
  <MenuItem key={type.value} value={type.value}>
    {type.label}
  </MenuItem>
))
```

### Backward Compatibility

**Auto-Sync in Entity Setters:**
```java
// Customer.java
public void setBusinessType(BusinessType businessType) {
    this.businessType = businessType;
    // Auto-sync to legacy field
    if (businessType != null) {
        this.industry = businessType.toLegacyIndustry();
    }
}

public void setIndustry(Industry industry) {
    this.industry = industry;
    // Auto-sync to new field
    if (industry != null) {
        this.businessType = BusinessType.fromLegacyIndustry(industry);
    }
}
```

**Benefits:**
- ✅ Old code using `industry` continues to work
- ✅ New code using `businessType` is automatically synced
- ✅ No breaking changes for existing APIs
- ✅ Gradual migration path

---

## 🧪 Testing

### Backend Tests
- ✅ Backend compiles successfully
- ✅ Migration V264 executes cleanly
- ✅ BusinessType CHECK constraints enforced

### Frontend Tests
- ✅ TypeScript compiles without errors
- ✅ Vite dev server runs without errors
- ✅ All enum endpoints accessible (business-types, lead-sources, kitchen-sizes)

### Integration Tests
- ✅ Lead form loads businessType dropdown dynamically
- ✅ Customer types support both industry and businessType
- ✅ Auto-sync between fields verified

---

## 📊 Field-Based Architecture Compatibility

### Customer Management System (FULLY HARMONIZED)

The Customer module uses a **Field-Based Architecture** with dynamic enum loading.

**Final State (Best Practice Complete):**
- ✅ Field Catalog: `businessType` field with `fieldType: "enum"`
- ✅ EnumField component created (uses `useBusinessTypes()` hook)
- ✅ DynamicFieldRenderer: Case for `enum` fields added
- ✅ CustomerDataStep: Uses `businessType` instead of `industry`
- ✅ Conditional fields updated: `HOTEL`, `KANTINE` (uppercase values)
- ✅ Both Lead and Customer forms use IDENTICAL pattern

**Implementation:**
```typescript
// Lead Form (LeadWizard.tsx)
const { data: businessTypes } = useBusinessTypes();
businessTypes?.map(type => <MenuItem value={type.value}>{type.label}</MenuItem>)

// Customer Form (EnumField.tsx → DynamicFieldRenderer)
const { data: businessTypes } = useBusinessTypes();
businessTypes?.map(type => <MenuItem value={type.value}>{type.label}</MenuItem>)
```

**Result:** Single Source of Truth achieved across entire application!

---

## 📝 Documentation Updates

- ✅ MIGRATIONS.md: V264 entry added
- ✅ Lead.java: @Deprecated annotations with migration references
- ✅ Customer.java: @Deprecated annotations with migration references
- ✅ BusinessType.java: Comprehensive Javadoc
- ✅ Frontend hooks: JSDoc with Sprint references

---

## 🚀 Next Steps (Future Sprints)

### Phase 3: Cleanup Deprecated Fields (Breaking Change)
- Migration V265: DROP COLUMN leads.industry
- Migration V266: DROP COLUMN customers.industry
- Remove @Deprecated annotations from entities

### Phase 4: Remove Legacy Enum (Breaking Change)
- Remove Industry.java enum completely
- Remove conversion methods from BusinessType.java (fromLegacyIndustry, toLegacyIndustry)
- Update all tests

---

## ✅ Success Criteria - ALL MET

- [x] Lead.businessType with CHECK constraint (V263)
- [x] Customer.businessType with data migration (V264)
- [x] Industry → BusinessType mapping (100% data migrated)
- [x] Frontend hardcoding eliminated (ALL forms)
- [x] Frontend types updated (Customer + Lead types)
- [x] Single Source of Truth: GET /api/enums/business-types
- [x] Backward compatibility maintained (auto-sync)
- [x] Lead forms use useBusinessTypes() hook
- [x] Customer forms use useBusinessTypes() hook (via EnumField)
- [x] Field Catalog migrated to businessType
- [x] DynamicFieldRenderer supports enum fields
- [x] TypeScript compiles without errors
- [x] Backend compiles without errors
- [x] Migrations execute successfully
- [x] Documentation complete
- [x] Best Practice: 100% achieved

---

## 🎯 Impact

**Before:**
- 🔴 3 different hardcoded dropdown implementations (Lead, Customer, Forms)
- 🔴 Inconsistent values between Lead and Customer
- 🔴 No Single Source of Truth
- 🔴 Manual updates needed for new business types

**After:**
- 🟢 1 unified BusinessType enum (9 values)
- 🟢 1 backend endpoint (/api/enums/business-types)
- 🟢 Dynamic frontend dropdowns
- 🟢 Add new types in ONE place (BusinessType.java)
- 🟢 Automatic propagation to all forms
- 🟢 Full backward compatibility

---

**Generated:** 2025-10-06
**Sprint:** 2.1.6 Phase 2
**Author:** @joergstreeck
**Status:** ✅ PRODUCTION READY
