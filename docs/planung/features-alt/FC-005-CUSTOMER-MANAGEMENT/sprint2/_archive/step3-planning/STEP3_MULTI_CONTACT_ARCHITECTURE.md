# 🏗️ Step 3 Multi-Contact Architecture

**Dokument:** Architektur-Entscheidungen für Step 3 Implementation  
**Sprint:** Sprint 2 - Contact Management  
**Erstellt:** 31.07.2025  
**Status:** 📋 GEPLANT  

## 📌 Kernentscheidung: Konsistenz mit Step 1 & 2

Step 3 **MUSS** die gleiche adaptive Theme-Architektur verwenden wie Step 1 und Step 2, um konsistente UI/UX zu gewährleisten.

## 🎨 Theme-Architektur Wiederverwendung

### Aus Step 1 & 2 übernehmen:

```typescript
// Step3AnsprechpartnerV5.tsx
import { CustomerFieldThemeProvider } from '../../theme';
import { AdaptiveFormContainer } from '../adaptive/AdaptiveFormContainer';
import { DynamicFieldRenderer } from '../fields/DynamicFieldRenderer';

export const Step3AnsprechpartnerV5: React.FC = () => {
  return (
    <CustomerFieldThemeProvider mode="anpassungsfähig">
      <Box>
        <Typography variant="h5" component="h2" gutterBottom>
          Schritt 3: Ansprechpartner
        </Typography>
        
        {/* Multi-Contact Cards */}
        <Box sx={{ mb: 3 }}>
          {contacts.map(contact => (
            <ContactCard key={contact.id}>
              <AdaptiveFormContainer>
                <DynamicFieldRenderer
                  fields={contactFields}
                  values={contact}
                  errors={validationErrors}
                  onChange={handleFieldChange}
                  onBlur={handleFieldBlur}
                />
              </AdaptiveFormContainer>
            </ContactCard>
          ))}
        </Box>
        
        {/* Add Contact Button */}
        <Button 
          variant="contained" 
          startIcon={<AddIcon />}
          onClick={handleAddContact}
        >
          Neuer Kontakt
        </Button>
      </Box>
    </CustomerFieldThemeProvider>
  );
};
```

## 📦 Komponenten-Hierarchie

```
Step3AnsprechpartnerV5/
├── index.tsx                    # Hauptkomponente mit Theme Provider
├── ContactCard.tsx              # Einzelne Kontakt-Card
├── ContactForm.tsx              # Modal/Inline Form
├── LocationAssignment.tsx       # Filial-Zuordnung Dropdown
└── RelationshipFields.tsx       # Beziehungsebene-Felder

Alle nutzen:
- CustomerFieldThemeProvider (Wrapper)
- AdaptiveFormContainer (Layout)
- DynamicFieldRenderer (Felder)
```

## 🔧 Field Catalog Integration

### Contact-spezifische Felder erweitern

```json
// fieldCatalogExtensions.json
{
  "contactFields": [
    {
      "key": "contactSalutation",
      "label": "Anrede",
      "fieldType": "select",
      "required": true,
      "category": "contact",
      "sizeHint": "kompakt",
      "options": [
        { "value": "Herr", "label": "Herr" },
        { "value": "Frau", "label": "Frau" },
        { "value": "Divers", "label": "Divers" }
      ]
    },
    {
      "key": "contactBirthday",
      "label": "Geburtstag",
      "fieldType": "date",
      "category": "relationship",
      "sizeHint": "klein",
      "helpText": "Für persönliche Glückwünsche"
    }
  ]
}
```

## 🎯 Adaptive Theme Features nutzen

### 1. Responsive Field Widths
```typescript
// Nutze sizeHint aus fieldCatalog
const getFieldWidth = (field: FieldDefinition) => {
  // AdaptiveFormContainer kümmert sich um responsive Breakpoints
  return field.sizeHint || 'mittel';
};
```

### 2. Konsistente Spacing
```typescript
// Theme spacing verwenden
<Box sx={{ mb: theme => theme.spacing(2) }}>
  {/* Konsistent mit Step 1 & 2 */}
</Box>
```

### 3. Field Validation
```typescript
// DynamicFieldRenderer handles validation display
<DynamicFieldRenderer
  fields={fields}
  errors={validationErrors}
  // Zeigt automatisch Fehler im Theme-Style
/>
```

## 📱 Mobile Responsiveness

### Contact Cards Mobile Layout
```typescript
// ContactCard responsive design
<Card sx={{
  mb: 2,
  // Mobile: Full width
  // Desktop: Mit Schatten und Spacing
  boxShadow: { xs: 0, md: 1 },
  p: { xs: 2, md: 3 }
}}>
  <AdaptiveFormContainer>
    {/* Fields stack on mobile, grid on desktop */}
  </AdaptiveFormContainer>
</Card>
```

## 🔄 State Management Integration

```typescript
// Erweitere customerOnboardingStore
interface CustomerOnboardingState {
  // Existing
  customerData: CustomerData;
  
  // NEW für Multi-Contact
  contacts: Contact[];
  primaryContactId?: string;
  
  // Actions
  addContact: (contact: Contact) => void;
  updateContact: (id: string, updates: Partial<Contact>) => void;
  removeContact: (id: string) => void;
  setPrimaryContact: (id: string) => void;
}
```

## ✅ Checkliste für Step 3 Implementation

### Theme-Konsistenz
- [ ] CustomerFieldThemeProvider als Wrapper
- [ ] AdaptiveFormContainer für jede Card
- [ ] DynamicFieldRenderer für alle Felder
- [ ] Theme spacing/colors verwenden

### Multi-Contact Features
- [ ] Contact Array im Store
- [ ] Primary Contact Markierung
- [ ] Add/Edit/Delete Funktionen
- [ ] Location Assignment

### Responsive Design
- [ ] Mobile-first Card Layout
- [ ] Touch-optimierte Actions
- [ ] Swipe-Gesten (optional)

### Field Integration
- [ ] fieldCatalogExtensions erweitern
- [ ] Relationship Fields definieren
- [ ] Validation Rules

## 🚨 Wichtige Hinweise

1. **KEINE neue Theme-Logik** - Nutze existierende Komponenten
2. **Konsistenz ist KEY** - User erwartet gleiche UX wie Step 1 & 2
3. **Mobile-First** - Viele Kontakte müssen auf Mobile funktionieren
4. **Performance** - Lazy Loading bei vielen Kontakten

## 📚 Referenz-Dokumente

- [Step 1 Implementation](/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/components/steps/Step1BasisFilialstruktur.tsx)
- [Step 2 Implementation](/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/components/steps/Step2HerausforderungenPotenzialV3.tsx)
- [Theme Documentation](/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/theme/README.md)
- [Field Catalog](/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/data/fieldCatalog.json)

---

**Navigation:**
- [← Sprint 2 Master Plan](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/SPRINT2_MASTER_PLAN.md)
- [→ Contact Management Vision](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/CONTACT_MANAGEMENT_VISION.md)