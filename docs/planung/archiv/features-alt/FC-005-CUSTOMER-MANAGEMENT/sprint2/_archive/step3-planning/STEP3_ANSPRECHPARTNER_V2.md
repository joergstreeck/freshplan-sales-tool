# 👥 Step 3: Ansprechpartner V2

**Status:** 🔄 Erweitert um Standort-Zuordnung  
**Component:** Step3AnsprechpartnerV2.tsx  
**Theme:** AdaptiveFormContainer

---

## 📍 Navigation
**← Zurück:** [Step 2 V3](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/wizard/STEP2_HERAUSFORDERUNGEN_POTENZIAL_V3.md)  
**→ Weiter:** [Step 4](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/wizard/STEP4_ANGEBOT_SERVICES.md)  
**⬆️ Wizard:** [Wizard Structure V3](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/WIZARD_STRUCTURE_V3.md)

---

## 🆕 Neue Features in V2

### Standort-Zuordnung für Ansprechpartner:
- Zentrale Ansprechpartner (für alle Standorte)
- Regionale Manager (für bestimmte Regionen)
- Lokale Ansprechpartner (einzelne Standorte)

### Verbesserte Struktur:
- Getrennte Namensfelder (Vor-/Nachname)
- Strukturierte Anrede mit Titel
- Klarere Rollen-Definition

---

## 🖼️ UI Design

```
┌─────────────────────────────────────────────┐
│ Schritt 3: Ansprechpartner                   │
├─────────────────────────────────────────────┤
│                                              │
│ 👤 Hauptansprechpartner                      │
│                                              │
│ ┌─── AdaptiveFormContainer ─────────────┐   │
│ │ [Herr ▼] [Dr.    ] [Max      ] [Muster]│   │
│ │  Anrede   Titel    Vorname     Nachname│   │
│ │  klein    klein    mittel      mittel  │   │
│ │                                        │   │
│ │ [Position/Funktion                    ]│   │
│ │ [Geschäftsführer                     ]│   │
│ │  └─ groß                              │   │
│ │                                        │   │
│ │ [E-Mail                ] [Telefon     ]│   │
│ │ [max@hotel.de         ] [+49 30 12345]│   │
│ │  └─ mittel               └─ mittel    │   │
│ │                                        │   │
│ │ 📍 Zuständigkeitsbereich:             │   │
│ │ ○ Für alle Standorte                  │   │
│ │ ● Für bestimmte Standorte:           │   │
│ │   [✓] Berlin  [✓] München  [ ] Hamburg│   │
│ └────────────────────────────────────────┘   │
│                                              │
│ ➕ Weiteren Ansprechpartner hinzufügen       │
└─────────────────────────────────────────────┘
```

---

## 📊 Datenmodell V2

```typescript
interface ContactV2 {
  id: string;
  // Strukturierte Namen
  salutation: 'Herr' | 'Frau' | 'Divers';
  title?: string; // Dr., Prof., etc.
  firstName: string;
  lastName: string;
  
  // Kontaktdaten
  position: string;
  email: string;
  phone?: string;
  mobile?: string;
  
  // NEU: Zuständigkeit
  responsibilityScope: 'all' | 'specific';
  assignedLocationIds?: string[]; // Bei 'specific'
  
  // Rollen
  isPrimary: boolean;
  roles: ContactRole[];
}

type ContactRole = 
  | 'decision_maker'
  | 'technical_contact'
  | 'billing_contact'
  | 'operations_contact';
```

---

## 🎨 Component Implementation

```typescript
export const Step3AnsprechpartnerV2: React.FC = () => {
  const {
    contacts,
    locations,
    addContact,
    updateContact,
    removeContact,
    setContactResponsibility
  } = useCustomerOnboardingStore();
  
  const handleResponsibilityChange = (
    contactId: string,
    scope: 'all' | 'specific',
    locationIds?: string[]
  ) => {
    setContactResponsibility(contactId, scope, locationIds);
  };
  
  return (
    <Box>
      <Typography variant="h5">
        Schritt 3: Ansprechpartner
      </Typography>
      
      {contacts.map((contact, index) => (
        <ContactCard key={contact.id}>
          <AdaptiveFormContainer>
            {/* Name Fields */}
            <Box sx={{ display: 'flex', gap: 2 }}>
              <FormControl size="small" sx={{ minWidth: 100 }}>
                <InputLabel>Anrede</InputLabel>
                <Select value={contact.salutation}>
                  <MenuItem value="Herr">Herr</MenuItem>
                  <MenuItem value="Frau">Frau</MenuItem>
                  <MenuItem value="Divers">Divers</MenuItem>
                </Select>
              </FormControl>
              
              <TextField
                label="Titel"
                value={contact.title}
                size="small"
                sx={{ width: 100 }}
              />
              
              <TextField
                label="Vorname"
                value={contact.firstName}
                required
                sx={{ flex: 1 }}
              />
              
              <TextField
                label="Nachname"
                value={contact.lastName}
                required
                sx={{ flex: 1 }}
              />
            </Box>
            
            {/* Zuständigkeit NEU */}
            <ResponsibilitySelector
              scope={contact.responsibilityScope}
              selectedLocationIds={contact.assignedLocationIds}
              locations={locations}
              onChange={(scope, ids) => 
                handleResponsibilityChange(contact.id, scope, ids)
              }
            />
          </AdaptiveFormContainer>
        </ContactCard>
      ))}
      
      <Button onClick={addContact} startIcon={<AddIcon />}>
        Weiteren Ansprechpartner hinzufügen
      </Button>
    </Box>
  );
};
```

---

## 🎯 ResponsibilitySelector Component

```typescript
const ResponsibilitySelector: React.FC<{
  scope: 'all' | 'specific';
  selectedLocationIds?: string[];
  locations: CustomerLocation[];
  onChange: (scope: 'all' | 'specific', locationIds?: string[]) => void;
}> = ({ scope, selectedLocationIds = [], locations, onChange }) => {
  return (
    <Box sx={{ mt: 2 }}>
      <Typography variant="subtitle2" gutterBottom>
        📍 Zuständigkeitsbereich:
      </Typography>
      
      <RadioGroup
        value={scope}
        onChange={(e) => onChange(e.target.value as 'all' | 'specific')}
      >
        <FormControlLabel
          value="all"
          control={<Radio />}
          label="Für alle Standorte"
        />
        <FormControlLabel
          value="specific"
          control={<Radio />}
          label="Für bestimmte Standorte:"
        />
      </RadioGroup>
      
      {scope === 'specific' && (
        <Box sx={{ ml: 4, mt: 1 }}>
          <LocationCheckboxList
            locations={locations}
            selectedIds={selectedLocationIds}
            onChange={(ids) => onChange('specific', ids)}
          />
        </Box>
      )}
    </Box>
  );
};
```

---

## 🏢 Einzelbetrieb-Handling

```typescript
// Bei Einzelbetrieb automatisch "alle Standorte"
const isSingleLocation = locations.length === 1;

if (isSingleLocation) {
  // Zuständigkeits-Selector nicht anzeigen
  // Automatisch scope: 'all' setzen
  contact.responsibilityScope = 'all';
  contact.assignedLocationIds = [locations[0].id];
}
```

---

## 📋 Field Catalog für Ansprechpartner

```json
{
  "contacts": {
    "salutation": {
      "key": "salutation",
      "label": "Anrede",
      "fieldType": "select",
      "required": true,
      "options": ["Herr", "Frau", "Divers"],
      "sizeHint": "klein"
    },
    "title": {
      "key": "title",
      "label": "Titel",
      "fieldType": "text",
      "placeholder": "Dr., Prof., etc.",
      "sizeHint": "klein"
    },
    "firstName": {
      "key": "firstName",
      "label": "Vorname",
      "fieldType": "text",
      "required": true,
      "sizeHint": "mittel"
    },
    "lastName": {
      "key": "lastName",
      "label": "Nachname",
      "fieldType": "text",
      "required": true,
      "sizeHint": "mittel"
    },
    "position": {
      "key": "position",
      "label": "Position/Funktion",
      "fieldType": "text",
      "required": true,
      "placeholder": "z.B. Geschäftsführer, Einkaufsleiter",
      "sizeHint": "groß"
    }
  }
}
```

---

## 🔄 Migration von V1 zu V2

### Store-Erweiterung:
```typescript
// Neue Action
setContactResponsibility: (
  contactId: string,
  scope: 'all' | 'specific',
  locationIds?: string[]
) => {
  set((state) => {
    const contact = state.contacts.find(c => c.id === contactId);
    if (contact) {
      contact.responsibilityScope = scope;
      contact.assignedLocationIds = scope === 'specific' ? locationIds : undefined;
    }
  });
}
```

### Backend-Anpassung:
```java
// ContactEntity erweitern
@Column(name = "responsibility_scope")
@Enumerated(EnumType.STRING)
private ResponsibilityScope responsibilityScope = ResponsibilityScope.ALL;

@ManyToMany
@JoinTable(name = "contact_location_assignments")
private Set<CustomerLocation> assignedLocations = new HashSet<>();
```

---

## ✅ Best Practices

### Validierung:
- Bei "specific": Mindestens 1 Standort muss ausgewählt sein
- E-Mail-Format prüfen
- Telefonnummern normalisieren

### UX-Optimierungen:
- Auto-Complete für Titel (Dr., Prof., Dipl.-Ing.)
- Intelligente Defaults (Hauptansprechpartner = alle Standorte)
- Visuelle Gruppierung bei vielen Kontakten

---

## 🔗 Weiterführende Links

**Implementation:**
- [Contact Management V2](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/CONTACT_MANAGEMENT_V2.md)
- [Responsibility Assignment](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/RESPONSIBILITY_ASSIGNMENT.md)

**Nächster Step:**
- [Step 4: Angebot & Leistungen](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/wizard/STEP4_ANGEBOT_SERVICES.md)