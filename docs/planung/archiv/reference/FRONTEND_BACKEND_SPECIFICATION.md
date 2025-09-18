# Frontend-Backend Spezifikation für FreshPlan Sales Tool

**📅 Aktuelles Datum: <!-- AUTO_DATE --> (System: 08.06.2025)**

## 🎨 Freshfoodz Corporate Identity Compliance

**KRITISCH: Alle Frontend-Komponenten MÜSSEN Freshfoodz CI-Standards einhalten!**

### Verbindliche Design-Tokens:

```css
/* Freshfoodz Farben - NICHT ändern! */
:root {
  --freshfoodz-primary: #94C456;    /* Primärgrün für Buttons, Links */
  --freshfoodz-secondary: #004F7B;   /* Dunkelblau für Headlines, Navigation */
  --freshfoodz-white: #FFFFFF;       /* Hintergründe, Cards */
  --freshfoodz-black: #000000;       /* Haupttext, Icons */
}

/* Freshfoodz Typografie - NICHT ändern! */
.freshfoodz-headline {
  font-family: 'Antonio', sans-serif;
  font-weight: bold;
}

.freshfoodz-body {
  font-family: 'Poppins', sans-serif;
  font-weight: 400; /* Regular */
}

.freshfoodz-body-medium {
  font-family: 'Poppins', sans-serif;
  font-weight: 500; /* Medium */
}
```

### Frontend-Komponenten CI-Compliance:

#### Buttons:
```typescript
// RICHTIG - Freshfoodz CI konform
const PrimaryButton = styled.button`
  background-color: var(--freshfoodz-primary);
  color: var(--freshfoodz-white);
  font-family: 'Poppins', sans-serif;
  font-weight: 500;
`;

const SecondaryButton = styled.button`
  background-color: transparent;
  color: var(--freshfoodz-secondary);
  border: 2px solid var(--freshfoodz-secondary);
  font-family: 'Poppins', sans-serif;
  font-weight: 500;
`;
```

#### Navigation & Headlines:
```typescript
// RICHTIG - Freshfoodz CI konform
const Navigation = styled.nav`
  background-color: var(--freshfoodz-secondary);
  color: var(--freshfoodz-white);
`;

const PageTitle = styled.h1`
  font-family: 'Antonio', sans-serif;
  font-weight: bold;
  color: var(--freshfoodz-secondary);
`;

const SectionHeading = styled.h2`
  font-family: 'Antonio', sans-serif;
  font-weight: bold;
  color: var(--freshfoodz-black);
`;
```

#### Links & Actions:
```typescript
// RICHTIG - Freshfoodz CI konform
const ActionLink = styled.a`
  color: var(--freshfoodz-primary);
  font-family: 'Poppins', sans-serif;
  font-weight: 500;
  
  &:hover {
    color: var(--freshfoodz-secondary);
  }
`;
```

**Referenz**: `/docs/FRESH-FOODZ_CI.md` für vollständige CI-Vorgaben

## 1. Backend-Berechnungen und Business-Logik

### 1.1 Rabattrechner (Calculator Module)

#### API Endpoint: `POST /api/calculator/calculate`

**Request:**
```json
{
  "orderValue": 15000,      // in Euro, min: 1000, max: 100000
  "leadTime": 14,           // in Tagen, min: 1, max: 30
  "isPickup": false,        // boolean
  "isChainCustomer": false  // boolean (aus Kundendaten)
}
```

**Backend-Berechnungslogik:**

1. **Basisrabatt-Berechnung:**
   ```
   - ab 5.000€: 3%
   - ab 15.000€: 6%
   - ab 30.000€: 8%
   - ab 50.000€: 9%
   - ab 75.000€: 10%
   ```

2. **Frühbucherrabatt-Berechnung:**
   ```
   - ab 10 Tage Vorlaufzeit: 1%
   - ab 15 Tage Vorlaufzeit: 2%
   - ab 30 Tage Vorlaufzeit: 3%
   ```

3. **Abholungsrabatt:**
   ```
   - Wenn isPickup = true UND orderValue >= 5000: 2%
   - Sonst: 0%
   ```

4. **Maximaler Gesamtrabatt: 15%**
   ```
   totalDiscount = MIN(baseDiscount + earlyDiscount + pickupDiscount, 15)
   ```

5. **Ersparnis und Endpreis:**
   ```
   savingsAmount = orderValue * (totalDiscount / 100)
   finalPrice = orderValue - savingsAmount
   ```

**Response:**
```json
{
  "baseDiscount": 6,
  "baseDiscountThreshold": "15.000€",
  "earlyDiscount": 1,
  "earlyDiscountThreshold": "10 Tage",
  "pickupDiscount": 0,
  "pickupEligible": false,
  "pickupMinOrderValue": 5000,
  "totalDiscount": 7,
  "maxDiscountReached": false,
  "savingsAmount": 1050,
  "finalPrice": 13950,
  "nextThreshold": {
    "type": "base",
    "currentValue": 15000,
    "nextValue": 30000,
    "additionalDiscount": 2,
    "amountNeeded": 15000
  }
}
```

### 1.2 Kundendaten-Verwaltung (Customer Module)

#### API Endpoint: `POST /api/customers`

**Request:**
```json
{
  "companyName": "Hotel Beispiel GmbH",
  "legalForm": "gmbh",
  "customerType": "neukunde",
  "industry": "hotel",
  "isChainCustomer": true,
  "customerNumber": "K-2024-001",
  "address": {
    "street": "Musterstraße 123",
    "postalCode": "12345",
    "city": "Berlin"
  },
  "contactPerson": {
    "name": "Max Mustermann",
    "position": "Einkaufsleiter",
    "phone": "+49 30 12345678",
    "email": "max@beispiel.de"
  },
  "businessData": {
    "expectedAnnualVolume": 500000,
    "paymentMethod": "rechnung"
  },
  "notes": "Wichtiger Neukunde",
  "customField1": "",
  "customField2": ""
}
```

**Backend-Validierungen:**
- E-Mail-Format validieren
- Telefonnummer-Format prüfen
- PLZ muss 5-stellig sein (Deutschland)
- Pflichtfelder prüfen
- Eindeutigkeit von customerNumber prüfen

**Response:**
```json
{
  "id": "uuid-12345",
  "customerNumber": "K-2024-001",
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T10:30:00Z",
  "status": "active",
  "creditCheckRequired": true,  // true wenn customerType="neukunde" UND paymentMethod="rechnung"
  "showLocationTabs": true,     // true wenn isChainCustomer=true
  ...request data...
}
```

### 1.3 Dynamische Tab-Sichtbarkeit

#### API Endpoint: `GET /api/ui/navigation/{customerId}`

**Backend-Logik für Tab-Sichtbarkeit:**
```
1. "Rabattrechner": IMMER sichtbar
2. "Kundendaten": IMMER sichtbar
3. "Standorte": NUR wenn customer.isChainCustomer = true
4. "Standort-Details": NUR wenn customer.isChainCustomer = true UND detailedLocations = true
5. "Bonitätsprüfung": IMMER sichtbar
6. "Profil": IMMER sichtbar
7. "Angebot": NUR wenn customer.status = "active"
8. "Einstellungen": IMMER sichtbar
```

**Response:**
```json
{
  "tabs": [
    {"id": "calculator", "visible": true, "enabled": true, "label": "Rabattrechner"},
    {"id": "customer", "visible": true, "enabled": true, "label": "Kundendaten"},
    {"id": "locations", "visible": true, "enabled": true, "label": "Standorte"},
    {"id": "locationdetails", "visible": false, "enabled": false, "label": "Standort-Details"},
    {"id": "creditcheck", "visible": true, "enabled": true, "label": "Bonitätsprüfung"},
    {"id": "profile", "visible": true, "enabled": true, "label": "Profil"},
    {"id": "offer", "visible": true, "enabled": false, "label": "Angebot", "tooltip": "Kunde muss aktiv sein"},
    {"id": "settings", "visible": true, "enabled": true, "label": "Einstellungen"}
  ]
}
```

### 1.4 Standortverwaltung (Locations Module)

#### Abhängige Felder basierend auf Branche:

**Backend-Logik für branchenspezifische Felder:**
```javascript
switch(customer.industry) {
  case 'hotel':
    return {
      fields: [
        {name: 'rooms', label: 'Anzahl Zimmer', type: 'number', required: true},
        {name: 'stars', label: 'Sterne-Kategorie', type: 'select', options: ['3','4','5'], required: true},
        {name: 'breakfastGuests', label: 'Ø Frühstücksgäste/Tag', type: 'number', required: true}
      ]
    };
  case 'krankenhaus':
    return {
      fields: [
        {name: 'beds', label: 'Anzahl Betten', type: 'number', required: true},
        {name: 'stations', label: 'Anzahl Stationen', type: 'number', required: true},
        {name: 'mealsPerDay', label: 'Mahlzeiten/Tag', type: 'number', required: true}
      ]
    };
  // ... weitere Branchen
}
```

### 1.5 Bonitätsprüfung (Credit Check Module)

#### API Endpoint: `POST /api/creditcheck/initiate`

**Backend-Logik:**
```
1. Prüfung nur erforderlich wenn:
   - customerType = "neukunde" UND
   - paymentMethod = "rechnung"

2. Automatische Limits basierend auf Unternehmensgröße:
   - small (bis 50 MA): max 10.000€ Kreditlimit
   - medium (50-250 MA): max 50.000€ Kreditlimit
   - large (>250 MA): max 100.000€ Kreditlimit

3. Status-Workflow:
   - "not_required" → keine Prüfung nötig
   - "pending" → Prüfung angefordert
   - "in_progress" → externe Prüfung läuft
   - "approved" → Kreditlimit genehmigt
   - "rejected" → abgelehnt
   - "manual_review" → manuelle Prüfung erforderlich
```

### 1.6 Übersetzungen (i18n)

#### API Endpoint: `GET /api/translations/{locale}`

**Backend liefert komplette Übersetzungsdateien:**
```json
{
  "locale": "de",
  "translations": {
    "common": {
      "save": "Speichern",
      "cancel": "Abbrechen",
      "delete": "Löschen",
      "edit": "Bearbeiten"
    },
    "calculator": {
      "title": "FreshPlan Rabattrechner",
      "orderValue": "Bestellwert",
      "leadTime": "Vorlaufzeit",
      "pickup": "Abholung (Mindestbestellwert: 5.000€ netto)",
      "baseDiscount": "Basisrabatt",
      "earlyDiscount": "Frühbucher",
      "pickupDiscount": "Abholung",
      "totalDiscount": "Gesamtrabatt",
      "savings": "Ersparnis",
      "finalPrice": "Endpreis"
    },
    // ... weitere Übersetzungen
  }
}
```

## 2. Frontend State Management

### 2.1 Globaler Application State

```typescript
interface AppState {
  // User & Auth
  currentUser: {
    id: string;
    name: string;
    role: 'admin' | 'manager' | 'sales';
    locale: 'de' | 'en';
  };
  
  // Current Customer Context
  currentCustomer: {
    id: string | null;
    data: Customer | null;
    isDirty: boolean;  // unsaved changes
  };
  
  // UI State
  ui: {
    activeTab: string;
    loading: boolean;
    errors: Error[];
    notifications: Notification[];
  };
  
  // Calculator State (temporary, nicht persistent)
  calculator: {
    orderValue: number;
    leadTime: number;
    isPickup: boolean;
    results: CalculatorResults | null;
  };
  
  // Navigation State
  navigation: {
    tabs: Tab[];
    breadcrumbs: string[];
  };
  
  // i18n State
  i18n: {
    locale: 'de' | 'en';
    translations: Record<string, any>;
  };
}
```

### 2.2 Lokaler Component State

```typescript
// Calculator Component
const [orderValue, setOrderValue] = useState(15000);
const [leadTime, setLeadTime] = useState(14);
const [isPickup, setIsPickup] = useState(false);
const [isCalculating, setIsCalculating] = useState(false);

// Customer Form Component  
const [formData, setFormData] = useState<CustomerFormData>({});
const [validationErrors, setValidationErrors] = useState<ValidationError[]>([]);
const [isSaving, setIsSaving] = useState(false);

// Locations Component
const [locations, setLocations] = useState<Location[]>([]);
const [selectedLocation, setSelectedLocation] = useState<string | null>(null);
const [isDetailedMode, setIsDetailedMode] = useState(false);
```

## 3. Frontend-Backend Kommunikationsfluss

### 3.1 Initial App Load

```typescript
// 1. Auth Check
GET /api/auth/me
→ currentUser

// 2. Load Translations
GET /api/translations/{locale}
→ translations

// 3. Load UI Config
GET /api/ui/config
→ navigation tabs, features

// 4. If customerId in URL
GET /api/customers/{id}
→ currentCustomer
```

### 3.2 Calculator Flow

```typescript
// User ändert Slider
onChange → setState(local) → debounce(300ms) → API Call

// API Call
POST /api/calculator/calculate
Body: {orderValue, leadTime, isPickup, isChainCustomer}
→ Response: {discounts, savings, finalPrice}
→ Update UI
```

### 3.3 Customer Save Flow

```typescript
// 1. Frontend Validierung
validateForm() → if errors → show errors → stop

// 2. API Call
POST/PUT /api/customers
→ Response: saved customer

// 3. Update Navigation wenn nötig
if (chainCustomerChanged) {
  GET /api/ui/navigation/{customerId}
  → Update visible tabs
}

// 4. Show success notification
```

### 3.4 Dynamic Field Loading

```typescript
// When industry changes
onChange(industry) → 
GET /api/locations/fields?industry={industry}
→ Dynamic fields config
→ Render additional fields
```

## 4. Datenfluss-Diagramm

```
Frontend                          Backend                    Database
--------                          -------                    --------
                                    
[Calculator UI]                                             
    |                                                       
    ├─ onChange ──────────→ [POST /api/calculate] ────→ [Business Logic]
    |                              |                              |
    |                              ├─ Validate                    |
    |                              ├─ Calculate Discounts         |
    |                              └─ Return Results              |
    |                                     |                       |
    └─ Update UI ←─────────────── [Response] ←─────────────────┘

[Customer Form]
    |
    ├─ onSubmit ──────────→ [POST /api/customers] ────→ [Validate & Save] ──→ [DB]
    |                              |                              |
    |                              ├─ Business Rules             |
    |                              ├─ Set Flags                  |
    |                              └─ Return Customer            |
    |                                     |                       |
    ├─ Update State ←─────── [Response] ←──────────────────────┘
    |
    └─ if chainCustomer ────→ [GET /api/ui/navigation] ──→ [Tab Logic]
                                      |
                                      └─ Return Tab Config
```

## 5. Validierungsregeln (Backend)

### 5.1 Customer Validation

```typescript
interface ValidationRules {
  companyName: {
    required: true,
    minLength: 3,
    maxLength: 100
  },
  email: {
    required: true,
    pattern: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
    unique: true  // Check in DB
  },
  phone: {
    required: true,
    pattern: /^\+?[0-9\s\-\(\)]+$/,
    minLength: 10
  },
  postalCode: {
    required: true,
    pattern: /^[0-9]{5}$/  // German PLZ
  },
  expectedAnnualVolume: {
    required: true,
    min: 0,
    max: 10000000
  }
}
```

### 5.2 Calculator Validation

```typescript
interface CalculatorValidation {
  orderValue: {
    required: true,
    min: 1000,
    max: 100000,
    step: 1000
  },
  leadTime: {
    required: true,
    min: 1,
    max: 30,
    step: 1
  }
}
```

## 6. Error Handling

### Backend Error Response Format:
```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed",
    "details": [
      {
        "field": "email",
        "message": "Email is already in use",
        "code": "DUPLICATE_EMAIL"
      }
    ],
    "timestamp": "2024-01-15T10:30:00Z",
    "traceId": "abc123"
  }
}
```

### Frontend Error Display:
```typescript
// Global errors → Toast/Notification
// Field errors → Inline under field
// Network errors → Retry mechanism
// Auth errors → Redirect to login
```

## 7. Performance Optimierungen

### Backend:
- Calculator Results cachen (TTL: 5 Minuten)
- Kunde-Tab-Config cachen (TTL: 30 Minuten)
- Übersetzungen cachen (TTL: 24 Stunden)

### Frontend:
- Debounce Calculator API calls (300ms)
- React Query für API State Management
- Optimistic Updates für bessere UX
- Lazy Loading für Tabs

## 8. Sicherheit

### Backend Security:
- Input Validation auf allen Endpoints
- SQL Injection Prevention (Prepared Statements)
- XSS Prevention (Output Encoding)
- CSRF Protection
- Rate Limiting (100 requests/minute)
- Role-based Access Control

### Frontend Security:
- Keine Business Logic im Frontend
- Keine sensiblen Daten im Local Storage
- API Keys nur im Backend
- Content Security Policy Headers

### Frontend CI-Compliance Prüfung:
- [ ] Alle Farben verwenden Freshfoodz Palette (#94C456, #004F7B, #FFFFFF, #000000)
- [ ] Alle Headlines verwenden Antonio Bold
- [ ] Alle Texte verwenden Poppins Regular/Medium
- [ ] Logo-Regeln werden eingehalten (neutrale Hintergründe, Schutzzone)
- [ ] Design-Tokens sind in CSS-Variablen definiert
- [ ] Accessibility-Standards sind erfüllt (WCAG 2.1 AA Kontraste)

## 9. Monitoring & Logging

### Backend Logging:
```
- Alle API Requests (ohne sensitive Daten)
- Alle Errors mit Stack Traces
- Performance Metriken
- Business Events (Kunde angelegt, Angebot erstellt)
```

### Frontend Logging:
```
- User Actions (anonymisiert)
- JavaScript Errors
- Performance Metrics
- API Response Times
```

Diese Spezifikation dient als Grundlage für die Backend-Implementierung und stellt sicher, dass Frontend und Backend optimal zusammenarbeiten.