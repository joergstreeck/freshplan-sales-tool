# 🎨 Theme Architecture - Konsistente UI/UX über alle Steps

**Phase:** 1 - Foundation  
**Tag:** 0 (Grundlage für alle Tage)  
**Status:** 🔴 KRITISCH - MUSS beachtet werden  

## 🧭 Navigation

**→ Nächster:** [Backend Intelligence](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/BACKEND_INTELLIGENCE.md)  
**↑ Übergeordnet:** [Step 3 Main Guide](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/README.md)  

## 🚨 KRITISCHE ARCHITEKTUR-ENTSCHEIDUNG

> **Step 3 MUSS die gleiche adaptive Theme-Architektur verwenden wie Step 1 und Step 2, um konsistente UI/UX zu gewährleisten.**

Diese Entscheidung ist NICHT OPTIONAL und wurde in [STEP3_MULTI_CONTACT_ARCHITECTURE.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/STEP3_MULTI_CONTACT_ARCHITECTURE.md) festgelegt.

## 🎨 Pflicht-Komponenten aus Step 1 & 2

### 1. CustomerFieldThemeProvider (Wrapper)

```typescript
// MUSS als äußerster Wrapper verwendet werden
import { CustomerFieldThemeProvider } from '../../theme';

export const Step3ContactManagement: React.FC = () => {
  return (
    <CustomerFieldThemeProvider mode="anpassungsfähig">
      {/* Alle Step 3 Komponenten */}
    </CustomerFieldThemeProvider>
  );
};
```

### 2. AdaptiveFormContainer (Layout)

```typescript
// MUSS für konsistentes Layout verwendet werden
import { AdaptiveFormContainer } from '../adaptive/AdaptiveFormContainer';

// In jeder Contact Card:
<ContactCard>
  <AdaptiveFormContainer>
    {/* Felder werden automatisch responsive angeordnet */}
  </AdaptiveFormContainer>
</ContactCard>
```

### 3. DynamicFieldRenderer (Felder)

```typescript
// MUSS für alle Formularfelder verwendet werden
import { DynamicFieldRenderer } from '../fields/DynamicFieldRenderer';

<DynamicFieldRenderer
  fields={contactFields}
  values={contact}
  errors={validationErrors}
  onChange={handleFieldChange}
  onBlur={handleFieldBlur}
/>
```

## 📦 Komponenten-Hierarchie für Step 3

```
Step3ContactManagement/
├── index.tsx                      # Hauptkomponente mit Theme Provider
├── components/
│   ├── ContactCard.tsx           # Einzelne Kontakt-Card
│   ├── ContactForm.tsx           # Modal/Inline Form
│   ├── LocationAssignment.tsx    # Filial-Zuordnung
│   ├── ResponsibilitySelector.tsx # NEU: Zuständigkeitsbereich
│   └── RelationshipFields.tsx    # Beziehungsebene-Felder
└── hooks/
    └── useContactTheme.ts        # Theme-spezifische Hooks

Alle nutzen:
- CustomerFieldThemeProvider (Wrapper)
- AdaptiveFormContainer (Layout)
- DynamicFieldRenderer (Felder)
```

## 🔧 Field Catalog Integration

### Contact-spezifische Felder erweitern

```typescript
// fieldCatalogExtensions.ts
export const contactFieldExtensions = {
  // Strukturierte Namensfelder (aus V2)
  contactSalutation: {
    key: "contactSalutation",
    label: "Anrede",
    fieldType: "select" as const,
    required: true,
    category: "contact",
    sizeHint: "klein" as const,
    options: [
      { value: "Herr", label: "Herr" },
      { value: "Frau", label: "Frau" },
      { value: "Divers", label: "Divers" }
    ]
  },
  contactTitle: {
    key: "contactTitle",
    label: "Titel",
    fieldType: "text" as const,
    category: "contact",
    sizeHint: "klein" as const,
    placeholder: "Dr., Prof., etc."
  },
  contactFirstName: {
    key: "contactFirstName",
    label: "Vorname",
    fieldType: "text" as const,
    required: true,
    category: "contact",
    sizeHint: "mittel" as const
  },
  contactLastName: {
    key: "contactLastName",
    label: "Nachname",
    fieldType: "text" as const,
    required: true,
    category: "contact",
    sizeHint: "mittel" as const
  },
  contactBirthday: {
    key: "contactBirthday",
    label: "Geburtstag",
    fieldType: "date" as const,
    category: "relationship",
    sizeHint: "klein" as const,
    helpText: "Für persönliche Glückwünsche"
  }
};
```

## 🎯 Adaptive Theme Features nutzen

### 1. Responsive Field Widths

```typescript
// AdaptiveFormContainer kümmert sich automatisch um:
// - Mobile: Felder untereinander (100% Breite)
// - Tablet: 2 Spalten
// - Desktop: Basierend auf sizeHint

// sizeHint Mapping:
// - "klein": 25% (Desktop), 50% (Tablet), 100% (Mobile)
// - "mittel": 50% (Desktop), 100% (Tablet), 100% (Mobile)
// - "groß": 100% (alle Geräte)
```

### 2. Konsistente Spacing & Colors

```typescript
// Theme spacing verwenden (NICHT hardcoded!)
<Box sx={{ mb: theme => theme.spacing(2) }}>
  {/* Konsistent mit Step 1 & 2 */}
</Box>

// Theme colors verwenden
<Card sx={{ 
  borderColor: theme => theme.palette.divider,
  backgroundColor: theme => theme.palette.background.paper
}}>
```

### 3. Field Validation Display

```typescript
// DynamicFieldRenderer zeigt automatisch Fehler im Theme-Style
<DynamicFieldRenderer
  fields={fields}
  errors={validationErrors}
  // Fehler werden mit:
  // - Roter Border
  // - Helper Text in Rot
  // - Konsistenter Animation
  // angezeigt - genau wie in Step 1 & 2
/>
```

## 📱 Mobile Responsiveness

### Contact Cards Mobile Layout

```typescript
// ContactCard responsive design
const ContactCard = styled(Card)(({ theme }) => ({
  marginBottom: theme.spacing(2),
  // Mobile: Reduzierter Schatten und Padding
  [theme.breakpoints.down('sm')]: {
    boxShadow: theme.shadows[1],
    padding: theme.spacing(2),
  },
  // Desktop: Voller Schatten und mehr Padding
  [theme.breakpoints.up('md')]: {
    boxShadow: theme.shadows[2],
    padding: theme.spacing(3),
  }
}));
```

### Touch-Optimierte Actions

```typescript
// Mobile-First Button Sizing
<IconButton
  sx={{
    // Größere Touch-Targets auf Mobile
    padding: { xs: 2, md: 1 },
    '& .MuiSvgIcon-root': {
      fontSize: { xs: '1.5rem', md: '1.25rem' }
    }
  }}
>
  <DeleteIcon />
</IconButton>
```

## 🔄 State Management Integration

```typescript
// Store MUSS erweitert werden für Theme-Konsistenz
interface CustomerOnboardingState {
  // Existing
  customerData: CustomerData;
  currentStep: number;
  
  // NEW für Multi-Contact mit Theme
  contacts: Contact[];
  primaryContactId?: string;
  contactValidationErrors: Record<string, FieldError>;
  
  // Theme-aware Actions
  addContact: (contact: Contact) => void;
  updateContact: (id: string, updates: Partial<Contact>) => void;
  removeContact: (id: string) => void;
  setPrimaryContact: (id: string) => void;
  validateContactField: (contactId: string, fieldKey: string, value: any) => void;
}
```

## ✅ Implementation Checkliste

### Theme-Konsistenz (PFLICHT!)
- [ ] CustomerFieldThemeProvider als äußerster Wrapper
- [ ] AdaptiveFormContainer für JEDE Card/Section
- [ ] DynamicFieldRenderer für ALLE Felder
- [ ] Theme spacing/colors verwenden (keine hardcoded Werte)
- [ ] Field Catalog erweitert mit Contact-Feldern

### Responsive Design
- [ ] Mobile-first Card Layout implementiert
- [ ] Touch-optimierte Actions (min. 44x44px)
- [ ] Breakpoint-basierte Layouts
- [ ] Performance bei vielen Kontakten getestet

### Field Integration
- [ ] fieldCatalogExtensions definiert
- [ ] Alle Contact-Felder im Catalog
- [ ] Validation Rules definiert
- [ ] Error Display konsistent mit Step 1 & 2

## 🚨 Häufige Fehler vermeiden

### ❌ FALSCH:
```typescript
// Eigene Form-Komponenten
<TextField value={contact.name} />

// Hardcoded Spacing
<Box sx={{ marginBottom: 16 }}>

// Eigenes Layout
<Grid container spacing={2}>
```

### ✅ RICHTIG:
```typescript
// Theme-Komponenten verwenden
<DynamicFieldRenderer fields={nameFields} />

// Theme Spacing
<Box sx={{ mb: 2 }}>

// AdaptiveFormContainer
<AdaptiveFormContainer>
```

## 📚 Referenz-Implementierungen

**MUSS angeschaut werden:**
- [Step 1 Implementation](/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/components/steps/Step1BasisFilialstruktur.tsx)
- [Step 2 Implementation](/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/components/steps/Step2HerausforderungenPotenzialV3.tsx)
- [Theme Documentation](/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/theme/README.md)
- [Field Catalog](/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/data/fieldCatalog.json)

---

**Nächster Schritt:** [→ Backend Intelligence](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/BACKEND_INTELLIGENCE.md)

**Konsistenz = Vertrauen = Bessere UX! 🎨✨**