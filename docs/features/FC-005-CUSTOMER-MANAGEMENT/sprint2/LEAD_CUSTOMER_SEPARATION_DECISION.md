# 🎯 Sprint 2 Entscheidung: Lead-Kunde-Trennung

**Datum:** 27.07.2025  
**Sprint:** Sprint 2 - Customer UI Integration  
**Status:** ✅ Entschieden & Dokumentiert

---

## 📋 Navigation
**Parent:** [Sprint 2 Overview](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/README.md)  
**Previous:** [Sidebar Discussion](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/SIDEBAR_LEAD_DISCUSSION.md)  
**Next:** [Sidebar Navigation Config](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/SIDEBAR_NAVIGATION_CONFIG.md)  
**Related:** [FC-020 Lead Management](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-020-LEAD-MANAGEMENT_TECH_CONCEPT.md) | [Key Decisions Summary](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/SPRINT2_KEY_DECISIONS_SUMMARY.md)

---

## 🎯 Kernentscheidung

**Lead Management (FC-020) wird NICHT Teil von FC-005**, sondern als eigenständiges Feature in Phase 2 implementiert.

## 📊 Begründung

### 1. **CRM Best Practices**
- Salesforce, HubSpot, Pipedrive trennen Lead- und Kundenverwaltung
- Unterschiedliche Prozesse, UI/UX, Datenmodelle
- Bounded Contexts im DDD-Sinne

### 2. **Architektur-Vorteile**
```typescript
// Klare Trennung der Domänen
import type { Customer } from '@/features/customers/types';
import type { Lead } from '@/features/leads/types';  // Phase 2

// Keine Vermischung der Logik!
```

### 3. **Wartbarkeit**
- FC-005 bleibt fokussiert auf field-basierte Kundenverwaltung
- FC-020 kann unabhängig entwickelt werden
- Keine Regressions-Risiken

## ✅ Was wir in Sprint 2 vorbereiten

### 1. **CustomerOnboardingWizard - Zukunftssicher**
```typescript
// ⚠️ WICHTIG: TypeScript Import Type verwenden!
import type { CustomerFormData } from '../types/customer.types';

interface CustomerOnboardingWizardProps {
  // Für spätere Lead-Konvertierung vorbereitet
  initialData?: Partial<CustomerFormData>;
  source?: 'direct' | 'lead_conversion' | 'import';
  onComplete: (data: CustomerFormData) => void;
}

export const CustomerOnboardingWizard: React.FC<CustomerOnboardingWizardProps> = ({
  initialData,
  source = 'direct',
  onComplete
}) => {
  // Sprint 2: initialData ist immer undefined
  // Sprint 2: source ist immer 'direct'
  // Aber: Die Props sind da für Phase 2!
};
```

### 2. **Sidebar Navigation - Platzhalter**
```typescript
// ⚠️ TypeScript Import Type!
import type { NavigationItem } from '@/types/navigation';

export const navigationConfig: NavigationItem[] = [
  {
    title: 'Neukundengewinnung',
    items: [
      { label: 'E-Mail Posteingang', path: '/inbox' },
      { 
        label: 'Lead-Erfassung', 
        path: '/leads', 
        disabled: true,  // Sprint 2: disabled
        tooltip: 'Verfügbar in Phase 2'
      },
      { label: 'Kampagnen', path: '/campaigns' }
    ]
  },
  {
    title: 'Kundenmanagement',
    items: [
      { label: 'Alle Kunden', path: '/customers' },
      { label: 'Neuer Kunde', path: '/customers/new' }, // Sprint 2: Direkt
      { label: 'Verkaufschancen', path: '/opportunities' },
      { label: 'Aktivitäten', path: '/activities' }
    ]
  }
];
```

### 3. **Customer Entity - Erweiterbar**
```typescript
// customer.types.ts
export interface Customer {
  // Bestehende Felder...
  id: string;
  name: string;
  email: string;
  // ...

  // Für Phase 2 vorbereitet (optional)
  leadSource?: string;
  leadConvertedAt?: Date;
  originalLeadId?: string;
}
```

## 🚫 Was wir in Sprint 2 NICHT implementieren

- ❌ Lead-Datenmodell
- ❌ Lead-Konvertierungs-UI
- ❌ BANT-Qualifizierung
- ❌ Lead-spezifische API Endpoints

## 🔄 Integration in Phase 2

```typescript
// Phase 2: Lead Conversion Service
// services/leadConversion.service.ts
import type { Lead } from '@/features/leads/types';
import type { Customer } from '@/features/customers/types';

export class LeadConversionService {
  async convertToCustomer(lead: Lead): Promise<void> {
    // 1. Map Lead → CustomerFormData
    const initialData = this.mapLeadToCustomer(lead);
    
    // 2. Open Wizard mit vorbefüllten Daten
    // Der Wizard aus Sprint 2 ist bereit dafür!
    openCustomerWizard({
      initialData,
      source: 'lead_conversion'
    });
  }
}
```

## 📋 Sprint 2 Fokus bleibt klar

1. **CustomerOnboardingWizard** für direkte Kundenanlage
2. **"Neuer Kunde"** DOPPELT platziert:
   - Als Menüpunkt in Sidebar unter "3. Kundenmanagement → 3.2 Neuer Kunde"
   - Als Quick-Action Button im Header der Kundenliste [+ Neuer Kunde]
3. **Keine Lead-Logik** - nur Vorbereitung der Props (initialData, source)
4. **TypeScript Import Types** konsequent verwenden!

## 🎯 Vorteile dieser Strategie

✅ **Sprint 2 bleibt schlank** - keine Feature Creep  
✅ **Code ist vorbereitet** - initialData Props ready  
✅ **Architektur sauber** - Bounded Contexts getrennt  
✅ **Team-Kommunikation klar** - "Leads kommen in Phase 2"  

## 📚 Referenzen

- [FC-020 Lead Management Konzept](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-020-LEAD-MANAGEMENT_TECH_CONCEPT.md)
- [TypeScript Import Type Guide](/Users/joergstreeck/freshplan-sales-tool/docs/guides/TYPESCRIPT_IMPORT_TYPE_GUIDE.md)
- [Sprint 2 Overview](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/README.md)

---

**Entscheidung getroffen von:** Jörg & Claude  
**Status:** Verbindlich für Sprint 2