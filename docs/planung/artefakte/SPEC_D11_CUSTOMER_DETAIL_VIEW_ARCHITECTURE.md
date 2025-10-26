# 🎯 SPEC: Customer Detail View Architecture (D11 Sub-Trigger)

**📅 Erstellt:** 2025-10-26
**🎯 Sprint:** 2.1.7.2 - D11 Server-Driven Customer Cards
**🔄 Status:** ✅ APPROVED (Architektur-Entscheidung)
**👤 Autor:** Jörg Streeck + Claude Code

---

## 🎯 PROBLEM: Vertikale Kartenstapelung ist schlechtes UX Design

### User Feedback
> "jetzt hast du in Phase 1 einen neuen Tab Profil (Server-Driven) eingerichtet. Und dann hast du alle 7 neuen Karten dort untereinander geballert. Das kann doch aber nicht best Practice sein bei UI Design"

### Root Cause
- Phase 1 Implementation: Alle 7 Customer Cards untereinander gestapelt
- Komponente: `ServerDrivenCustomerCards.tsx` (Zeile 87-89)
- Problem: Zu viel Information auf einmal → Overwhelm → schlechte UX
- Konsequenz: Nutzer muss scrollen, um wichtige Infos zu finden

---

## ✅ LÖSUNG: Zwei-View Architektur mit Tabs

### Architektur-Prinzip
```
80% Use Case: Kompakte Übersicht (View A)
20% Use Case: Deep Dive mit Tabs (View B)
```

### Vorteile
1. ✅ **Cognitive Load reduziert** - Nutzer sieht zuerst nur wichtigste Infos
2. ✅ **Progressive Disclosure** - Details nur auf Anforderung
3. ✅ **Logische Gruppierung** - 7 Karten in 3 Tabs organisiert
4. ✅ **Multi-Location Support** - Summary in Compact View, Details in Tab
5. ✅ **Mobile-Friendly** - Kompakte View passt auf Smartphone
6. ✅ **Schnellere Ladezeiten** - Lazy Loading für Detail-View

---

## 📋 VIEW A: Kompakte Kunden-Übersicht (Standard)

### Wann sichtbar
- User klickt im Cockpit auf Kunde (Arbeitsbereich rechts)
- User klickt in Kundenmanagement auf Kunde
- **Default View:** `/customers/:id`

### Was wird gezeigt

```
┌─────────────────────────────────────────┐
│ NH Hotels GmbH                          │
│ AKTIV • Jahresumsatz: €180.000         │
├─────────────────────────────────────────┤
│ 📍 3 Standorte (München, Berlin, Hamburg)│
│ Risiko-Score: 25% 🟢                    │
│ Letzter Kontakt: vor 5 Tagen            │
│ Letzte Bestellung: vor 8 Tagen          │
│                                         │
│ 🎯 Nächste Schritte:                    │
│ • Cross-Sell: Desserts vorschlagen      │
│ • Feedback-Termin vereinbaren           │
│                                         │
│ 📞 Hauptansprechpartner:                │
│ Klaus Schmidt (Küchenchef München)      │
│ stefan.mueller@nh-hotels.de             │
│                                         │
│ [E-Mail] [Anrufen] [Aktivität]         │
│                                         │
│ [🔍 Alle Details anzeigen] ← Button!   │
└─────────────────────────────────────────┘
```

### Komponenten-Architektur

**Component:** `CustomerCompactView.tsx`

**Props:**
```typescript
interface CustomerCompactViewProps {
  customerId: string;
  onShowDetails: () => void; // Callback für [Alle Details] Button
}
```

**Benötigte Daten (Backend):**
```typescript
interface CustomerCompactData {
  // Stammdaten
  id: string;
  companyName: string;
  status: CustomerStatus; // ENUM: ACTIVE, INACTIVE, CHURN_RISK
  annualRevenue: number;

  // Multi-Location Summary
  locationCount: number;
  topLocations: string[]; // Top 3 Cities (z.B. ["München", "Berlin", "Hamburg"])

  // Health & Risk
  healthScore: number; // 0-100
  healthStatus: 'GREEN' | 'YELLOW' | 'RED';
  lastContactDate: string; // ISO 8601
  lastOrderDate: string; // ISO 8601

  // Next Steps (aus Activities)
  nextSteps: {
    icon: string;
    description: string;
  }[];

  // Haupt-Ansprechpartner
  primaryContact: {
    name: string;
    role: string;
    email: string;
    phone?: string;
  };
}
```

**Neuer Backend Endpoint (optional):**
```
GET /api/customers/{id}/compact-view
```

**Alternative:** Frontend filtert existierende `/api/customers/{id}` Daten (langsamer, aber weniger Backend-Arbeit)

### Multi-Location Handling in Compact View

**WICHTIG:** Nur Summary anzeigen - KEINE Details!

```typescript
// Beispiel-Code
<Box display="flex" alignItems="center" gap={1}>
  <PlaceIcon sx={{ color: 'primary.main' }} />
  <Typography variant="body1">
    {customer.locationCount} Standorte
  </Typography>
  {customer.topLocations.length > 0 && (
    <Typography variant="body2" color="text.secondary">
      ({customer.topLocations.slice(0, 3).join(', ')}
      {customer.locationCount > 3 && ` +${customer.locationCount - 3} weitere`})
    </Typography>
  )}
</Box>
```

**Kein Dropdown!** → Für Details muss User auf [Alle Details] klicken

---

## 📋 VIEW B: Detail-Ansicht mit Tabs (Deep Dive)

### Wann sichtbar
- User klickt auf [Alle Details anzeigen] Button
- **Navigation:** `/customers/:id/details` (Option A - neue Seite)
- Öffnet sich als separate Seite mit Browser-History

### Navigation-Entscheidung

**Option A (GEWÄHLT): Neue Seite**
```
/customers → Kundenliste
/customers/:id → Kompakte View
/customers/:id/details → Tab-View
```

**Vorteile:**
- ✅ Browser-Back funktioniert intuitiv
- ✅ URLs sind teilbar (z.B. für Support-Tickets)
- ✅ Einfacher zu implementieren
- ✅ Bessere Navigation in Browser-History
- ✅ Separate Seite = weniger State Management

**~~Option B (VERWORFEN): Modal/Overlay~~**
```
/customers/:id → Kompakte View
[Alle Details] → Overlay öffnet sich
```
- ❌ Browser-Back funktioniert nicht (muss custom implementiert werden)
- ❌ URLs nicht teilbar
- ❌ Komplexer State Management
- ✅ Schnellerer Wechsel (kein Page Reload)

### Tab-Struktur

```
[ Firma ] [ Geschäft ] [ Verlauf ]
    ↓
Aktueller Tab zeigt 2-3 Cards in Grid-Layout
```

---

## 🏢 TAB 1: "Firma"

### Enthält 3 Cards
1. **Unternehmensprofil** (Server-Driven)
2. **Standorte** (Server-Driven) ← HIER Multi-Location!
3. **Klassifikation** (Server-Driven)

### Layout
```typescript
// Desktop: 2 Spalten
<Grid container spacing={2}>
  <Grid size={{ xs: 12, md: 6 }}>
    <DynamicCustomerCard schema={companyProfileSchema} />
  </Grid>
  <Grid size={{ xs: 12, md: 6 }}>
    <DynamicCustomerCard schema={locationsSchema} />
  </Grid>
  <Grid size={{ xs: 12, md: 6 }}>
    <DynamicCustomerCard schema={classificationSchema} />
  </Grid>
</Grid>

// Mobile: 1 Spalte (xs: 12 → volle Breite)
```

### Card 1: Unternehmensprofil

**Schema-ID:** `company_profile`

**Felder (Server-Driven):**
- Firmenname (TEXT)
- Handelsname (TEXT)
- Kundentyp (ENUM: `/api/enums/customer-types`)
- Branche (TEXT)
- Rechtsform (ENUM: `/api/enums/legal-forms`)
- Status (ENUM: ACTIVE, INACTIVE, CHURN_RISK)

**Backend-Schema:**
```json
{
  "cardId": "company_profile",
  "title": "Unternehmensprofil",
  "order": 1,
  "fields": [
    {
      "key": "companyName",
      "label": "Firmenname",
      "type": "TEXT",
      "required": true,
      "editable": false
    },
    {
      "key": "tradingName",
      "label": "Handelsname",
      "type": "TEXT",
      "required": false,
      "editable": true
    },
    {
      "key": "customerType",
      "label": "Kundentyp",
      "type": "ENUM",
      "enumSource": "/api/enums/customer-types",
      "required": true,
      "editable": true
    }
    // ... weitere Felder
  ]
}
```

---

### Card 2: Standorte (Multi-Location Details)

**Schema-ID:** `locations`

**WICHTIG:** HIER werden die Multi-Location Details gezeigt!

**Felder (Server-Driven):**
- Rechnungsadresse (TEXT - Read-only)
- Lieferadressen (LIST - Read-only, später editierbar)
- Standorte Deutschland (NUMBER - Read-only)
- Standorte Schweiz (NUMBER - Read-only)
- Standorte Österreich (NUMBER - Read-only)
- Expansion geplant? (ENUM: `/api/enums/expansion-plan`)

**UI-Mockup:**

```typescript
<Card>
  <CardHeader title="Standorte" />
  <CardContent>
    {/* Rechnungsadresse */}
    <Typography variant="h6">Rechnungsadresse</Typography>
    <Typography variant="body2" color="text.secondary">
      {customer.billingAddress.street}, {customer.billingAddress.zip} {customer.billingAddress.city}
    </Typography>

    {/* Lieferadressen (Liste) */}
    <Typography variant="h6" sx={{ mt: 2 }}>Lieferadressen</Typography>
    <List>
      {customer.deliveryAddresses.map((addr, idx) => (
        <ListItem key={addr.id}>
          <ListItemIcon>
            <LocalShippingIcon />
          </ListItemIcon>
          <ListItemText
            primary={addr.city}
            secondary={`${addr.street}, ${addr.zip} ${addr.city}`}
          />
        </ListItem>
      ))}
    </List>

    {/* Standort-Statistik (Grid) */}
    <Typography variant="h6" sx={{ mt: 2 }}>Standort-Übersicht</Typography>
    <Grid container spacing={2} sx={{ mt: 1 }}>
      <Grid size={{ xs: 12, sm: 4 }}>
        <TextField
          label="Standorte Deutschland"
          value={customer.locationsDE}
          disabled
          fullWidth
        />
      </Grid>
      <Grid size={{ xs: 12, sm: 4 }}>
        <TextField
          label="Standorte Schweiz"
          value={customer.locationsCH}
          disabled
          fullWidth
        />
      </Grid>
      <Grid size={{ xs: 12, sm: 4 }}>
        <TextField
          label="Standorte Österreich"
          value={customer.locationsAT}
          disabled
          fullWidth
        />
      </Grid>
    </Grid>

    {/* Expansion geplant (Dropdown) */}
    <FormControl fullWidth sx={{ mt: 2 }}>
      <InputLabel>Expansion geplant?</InputLabel>
      <Select value={customer.expansionPlanned}>
        <MenuItem value="yes">Ja, Expansion geplant</MenuItem>
        <MenuItem value="no">Nein</MenuItem>
        <MenuItem value="unsure">Unklar</MenuItem>
      </Select>
    </FormControl>

    {/* Bearbeiten Button */}
    <Button
      variant="outlined"
      sx={{ mt: 2 }}
      onClick={() => openEditDialog()}
    >
      Standorte bearbeiten
    </Button>
  </CardContent>
</Card>
```

**Backend-Schema:**
```json
{
  "cardId": "locations",
  "title": "Standorte",
  "order": 2,
  "fields": [
    {
      "key": "billingAddress",
      "label": "Rechnungsadresse",
      "type": "ADDRESS",
      "required": true,
      "editable": false
    },
    {
      "key": "deliveryAddresses",
      "label": "Lieferadressen",
      "type": "ADDRESS_LIST",
      "required": false,
      "editable": true
    },
    {
      "key": "locationsDE",
      "label": "Standorte Deutschland",
      "type": "NUMBER",
      "required": false,
      "editable": false
    },
    {
      "key": "locationsCH",
      "label": "Standorte Schweiz",
      "type": "NUMBER",
      "required": false,
      "editable": false
    },
    {
      "key": "locationsAT",
      "label": "Standorte Österreich",
      "type": "NUMBER",
      "required": false,
      "editable": false
    },
    {
      "key": "expansionPlanned",
      "label": "Expansion geplant?",
      "type": "ENUM",
      "enumSource": "/api/enums/expansion-plan",
      "required": false,
      "editable": true
    }
  ]
}
```

**Neuer Backend Endpoint:**
```
GET /api/enums/expansion-plan
```

**ENUM-Werte:**
```java
public enum ExpansionPlan {
    YES("yes", "Ja, Expansion geplant"),
    NO("no", "Nein"),
    UNSURE("unsure", "Unklar");

    private final String value;
    private final String label;
}
```

---

### Card 3: Klassifikation

**Schema-ID:** `classification`

**Felder (Server-Driven):**
- Küchengröße (ENUM)
- Anzahl Mitarbeiter (NUMBER)
- Anzahl Filialen (NUMBER)
- Filialunternehmen? (BOOLEAN)

**Backend-Schema:**
```json
{
  "cardId": "classification",
  "title": "Klassifikation",
  "order": 3,
  "fields": [
    {
      "key": "kitchenSize",
      "label": "Küchengröße",
      "type": "ENUM",
      "enumSource": "/api/enums/kitchen-size",
      "required": false,
      "editable": true
    },
    {
      "key": "employeeCount",
      "label": "Anzahl Mitarbeiter",
      "type": "NUMBER",
      "required": false,
      "editable": true
    },
    {
      "key": "branchCount",
      "label": "Anzahl Filialen",
      "type": "NUMBER",
      "required": false,
      "editable": true
    },
    {
      "key": "isChainCompany",
      "label": "Filialunternehmen?",
      "type": "BOOLEAN",
      "required": false,
      "editable": true
    }
  ]
}
```

---

## 💰 TAB 2: "Geschäft"

### Enthält 4 Cards
1. **Geschäftsdaten & Performance** (Server-Driven)
2. **Vertragsbedingungen** (Server-Driven)
3. **Bedürfnisse & Lösungen** (Server-Driven)
4. **Produktportfolio & Services** (Server-Driven)

### Layout
```typescript
// Desktop: 2 Spalten Grid
<Grid container spacing={2}>
  <Grid size={{ xs: 12, md: 6 }}>
    <DynamicCustomerCard schema={businessDataSchema} />
  </Grid>
  <Grid size={{ xs: 12, md: 6 }}>
    <DynamicCustomerCard schema={contractsSchema} />
  </Grid>
  <Grid size={{ xs: 12, md: 6 }}>
    <DynamicCustomerCard schema={painPointsSchema} />
  </Grid>
  <Grid size={{ xs: 12, md: 6 }}>
    <DynamicCustomerCard schema={productsSchema} />
  </Grid>
</Grid>

// Mobile: 1 Spalte
```

### Card 1: Geschäftsdaten & Performance

**Schema-ID:** `business_data`

**Felder (Server-Driven):**
- Umsatz geschätzt (CURRENCY)
- Umsatz Ist (CURRENCY)
- Umsatz Xentral (CURRENCY - Read-only)
- Erwarteter Jahresumsatz (CURRENCY)
- Umsatz letzte 30 Tage (CURRENCY - Read-only)
- Umsatz letzte 90 Tage (CURRENCY - Read-only)
- Umsatz letzte 365 Tage (CURRENCY - Read-only)
- YoY Growth (PERCENTAGE - Read-only)

**Backend berechnet:**
- YoY Growth: `(revenue_current_year - revenue_last_year) / revenue_last_year * 100`
- 30/90/365 Tage Umsätze aus Xentral-Orders

---

### Card 2: Vertragsbedingungen

**Schema-ID:** `contracts`

**Felder (Server-Driven):**
- Zahlungsziel (ENUM: `/api/enums/payment-terms`)
- Kreditlimit (CURRENCY)
- Lieferbedingung (ENUM: `/api/enums/delivery-conditions`)
- Finanzierungsart (ENUM)

---

### Card 3: Bedürfnisse & Lösungen

**Schema-ID:** `pain_points`

**Felder (Server-Driven):**
- Pain Points (MULTI_ENUM - Checkboxen)
- Details zu Pain Points (TEXTAREA)

**Pain Points aus Lead:**
```
[ ] Hohe Lebensmittelkosten
[ ] Zeitaufwendige Bestellprozesse
[ ] Mangelnde Produktverfügbarkeit
[ ] Unzureichende Lieferantenqualität
[ ] Schwierigkeiten bei der Menüplanung
```

---

### Card 4: Produktportfolio & Services

**Schema-ID:** `products`

**Felder (Server-Driven):**
- Aktive Produkte (LIST - Read-only)
- Service-Level (ENUM)
- Cross-Sell Opportunities (LIST - Read-only)

**Cross-Sell wird automatisch berechnet:**
```java
// Backend Service
List<CrossSellOpportunity> calculateCrossSell(Customer customer) {
  // Beispiel: Kunde kauft Fleisch → empfehle Gewürze
  // Kunde kauft Desserts nicht → Cross-Sell Opportunity!
}
```

---

## 📈 TAB 3: "Verlauf" (Phase 2 - SPÄTER)

### Enthält 2 Sections
1. **Kontakte & Stakeholder** (Server-Driven)
2. **Aktivitäten & Timeline** (Server-Driven)

### Layout
```typescript
// 1-Spalten Layout (Timeline = volle Breite)
<Box>
  <ContactsSection customerId={customerId} />
  <TimelineSection customerId={customerId} />
</Box>
```

### Section 1: Kontakte & Stakeholder

**Neuer Backend Endpoint:**
```
GET /api/customers/{id}/contacts
```

**Liefert:**
```json
{
  "contacts": [
    {
      "id": "uuid",
      "name": "Klaus Schmidt",
      "role": "CHEF", // ENUM: CHEF, BUYER, DECISION_MAKER
      "email": "klaus.schmidt@nh-hotels.de",
      "phone": "+49 89 123456",
      "isPrimary": true
    }
  ]
}
```

---

### Section 2: Aktivitäten & Timeline

**Neuer Backend Endpoint:**
```
GET /api/customers/{id}/timeline
```

**Liefert:**
```json
{
  "activities": [
    {
      "type": "ORDER", // ENUM: ORDER, MEETING, CALL, EMAIL, NOTE
      "timestamp": "2025-10-20T14:30:00Z",
      "title": "Bestellung #12345",
      "details": "Fleisch-Sortiment, €2.400",
      "icon": "ShoppingCartIcon",
      "color": "success"
    },
    {
      "type": "MEETING",
      "timestamp": "2025-10-15T10:00:00Z",
      "title": "Quartalsgespräch",
      "details": "Feedback zu Desserts",
      "icon": "EventIcon",
      "color": "primary"
    }
  ],
  "leadHistory": {
    "convertedAt": "2025-09-01T12:00:00Z",
    "originalQuality": "PREMIUM",
    "conversionReason": "Positive Verkostung"
  }
}
```

**UI-Component:**
```typescript
<Timeline>
  {activities.map(activity => (
    <TimelineItem key={activity.id}>
      <TimelineSeparator>
        <TimelineDot color={activity.color}>
          <ActivityIcon type={activity.type} />
        </TimelineDot>
        <TimelineConnector />
      </TimelineSeparator>
      <TimelineContent>
        <Typography variant="h6">{activity.title}</Typography>
        <Typography variant="body2" color="text.secondary">
          {formatDate(activity.timestamp)}
        </Typography>
        <Typography variant="body1">{activity.details}</Typography>
      </TimelineContent>
    </TimelineItem>
  ))}
</Timeline>
```

---

## 🏗️ KOMPONENTEN-STRUKTUR (Frontend)

### Neue Files

```
/frontend/src/features/customers/components/detail/
├── CustomerCompactView.tsx        ← NEU! Standard-View
├── CustomerDetailView.tsx         ← NEU! Tab-Container
├── tabs/
│   ├── CustomerDetailTabFirma.tsx     ← NEU! Tab 1
│   ├── CustomerDetailTabGeschaeft.tsx ← NEU! Tab 2
│   └── CustomerDetailTabVerlauf.tsx   ← NEU! Tab 3 (später)
└── ServerDrivenCustomerCards.tsx  ← BEHALTEN (wird in Tabs verwendet)
```

### Zu löschende Files

```
/frontend/src/features/customers/components/
└── CustomerProfileTab.tsx ← LÖSCHEN (alter Ansatz - alle 7 Cards untereinander)
```

---

## 🔄 ROUTING-ÄNDERUNG

### ALT (Phase 1)
```typescript
// CustomerDetailPage.tsx
/customers/:id → Zeigt alle 7 Cards untereinander (CustomerProfileTab)
```

### NEU (Phase 2)
```typescript
// CustomerDetailPage.tsx
/customers/:id → CustomerCompactView (default)
  └─ Button [Alle Details anzeigen] → Navigate to /customers/:id/details

// CustomerDetailViewPage.tsx (NEU)
/customers/:id/details → CustomerDetailView (Tab-Container)
  ├─ Tab "Firma" → CustomerDetailTabFirma
  ├─ Tab "Geschäft" → CustomerDetailTabGeschaeft
  └─ Tab "Verlauf" → CustomerDetailTabVerlauf (später)
```

### React Router Config

```typescript
// App.tsx oder routes.tsx
<Route path="/customers/:id" element={<CustomerDetailPage />} />
<Route path="/customers/:id/details" element={<CustomerDetailViewPage />} />
```

---

## 📊 IMPLEMENTIERUNGSPLAN

### Phase 1: Kompakte View (JETZT - 3h)

**Tasks:**
1. Erstelle `CustomerCompactView.tsx`
   - Zeige: Name, Status, Umsatz, Risiko, Kontakte
   - Multi-Location: Nur Summary ("3 Standorte")
   - Button: [Alle Details anzeigen]

2. Update `CustomerDetailPage.tsx`
   - Default: Zeige CustomerCompactView
   - Button-Click: Navigate to `/customers/:id/details`

3. Neuer Backend Endpoint (optional)
   - `GET /api/customers/{id}/compact-view`
   - Alternative: Frontend filtert existierende Daten

**Acceptance Criteria:**
- [ ] CustomerCompactView zeigt alle Kern-Infos
- [ ] Multi-Location Summary funktioniert (z.B. "3 Standorte: München, Berlin, Hamburg")
- [ ] Button [Alle Details] navigiert zu `/customers/:id/details`

---

### Phase 2: Tab-Structure (JETZT - 4h)

**Tasks:**
1. Erstelle `CustomerDetailView.tsx` (Tab-Container)
   - MUI Tabs Component
   - 3 Tabs: Firma, Geschäft, Verlauf
   - Tab-State Management

2. Erstelle `CustomerDetailTabFirma.tsx`
   - Verwendet `ServerDrivenCustomerCards`
   - Zeigt nur Cards: `company_profile`, `locations`, `classification`
   - Grid Layout: 2 Spalten Desktop, 1 Spalte Mobile

3. Erstelle `CustomerDetailTabGeschaeft.tsx`
   - Verwendet `ServerDrivenCustomerCards`
   - Zeigt Cards: `business_data`, `contracts`, `pain_points`, `products`
   - Grid Layout: 2 Spalten Desktop, 1 Spalte Mobile

4. Update Routing
   - Neue Route: `/customers/:id/details`
   - Route-Component: `CustomerDetailViewPage.tsx`

**Acceptance Criteria:**
- [ ] Tab-Navigation funktioniert
- [ ] Cards werden korrekt in Tabs gruppiert
- [ ] Grid-Layout responsive (2 Spalten Desktop, 1 Spalte Mobile)
- [ ] URL-Navigation funktioniert (`/customers/:id/details?tab=firma`)

---

### Phase 3: Multi-Location Details (JETZT - 3h)

**Tasks:**
1. In Tab "Firma" → Card "Standorte"
   - Rechnungsadresse (Text)
   - Lieferadressen (Liste)
   - Standorte DE/CH/AT (Read-only Fields)
   - Expansion geplant (Dropdown)

2. Backend: Neuer ENUM Endpoint
   - `GET /api/enums/expansion-plan`
   - ENUM: `ExpansionPlan.java`

3. Backend: Schema Update
   - `locations` Card-Schema erweitern
   - Felder: `billingAddress`, `deliveryAddresses`, `locationsDE`, `locationsCH`, `locationsAT`, `expansionPlanned`

4. Bearbeiten-Dialog für Standorte (später)
   - Wizard-Style (Phase 4)
   - Erstmal: Inline-Edit oder Modal

**Acceptance Criteria:**
- [ ] Standorte-Card zeigt alle Multi-Location Details
- [ ] Rechnungsadresse korrekt formatiert
- [ ] Lieferadressen als Liste dargestellt
- [ ] Expansion-Dropdown funktioniert (ExpansionPlan ENUM)
- [ ] Standort-Statistik (DE/CH/AT) read-only angezeigt

---

### Phase 4: Tab "Verlauf" (SPÄTER - 4h)

**Tasks:**
1. Erstelle `CustomerDetailTabVerlauf.tsx`
   - Section 1: Kontakte (separater Endpoint)
   - Section 2: Timeline (separater Endpoint)

2. Backend: Neuer Endpoint
   - `GET /api/customers/{id}/contacts`
   - Liefert: Liste von Ansprechpartnern (CHEF, BUYER)

3. Backend: Neuer Endpoint
   - `GET /api/customers/{id}/timeline`
   - Liefert: Aktivitäten (Orders, Meetings, Calls)

4. Timeline Component
   - MUI Timeline verwenden
   - Icons für Activity-Types
   - Lead-Historie einbinden

**Acceptance Criteria:**
- [ ] Kontakte werden korrekt angezeigt
- [ ] Timeline zeigt alle Aktivitäten chronologisch
- [ ] Lead-Konvertierungs-Historie sichtbar
- [ ] Icons für Activity-Types korrekt gemappt

---

## 📋 BACKEND-ÄNDERUNGEN (Minimal!)

### Neue Endpoints

```java
// CustomerResource.java (erweitern)

@GET
@Path("/{id}/compact-view")
@Produces(MediaType.APPLICATION_JSON)
public CustomerCompactData getCompactView(@PathParam("id") UUID customerId) {
    // Liefert: Name, Status, Umsatz, Health Score, Locations Summary, Next Steps
}

@GET
@Path("/{id}/contacts")
@Produces(MediaType.APPLICATION_JSON)
public List<ContactResponse> getContacts(@PathParam("id") UUID customerId) {
    // Liefert: Ansprechpartner (CHEF, BUYER)
}

@GET
@Path("/{id}/timeline")
@Produces(MediaType.APPLICATION_JSON)
public TimelineResponse getTimeline(@PathParam("id") UUID customerId) {
    // Liefert: Orders, Meetings, Calls, Lead-Historie
}
```

### Neue ENUMs

```java
// ExpansionPlan.java (NEU)
public enum ExpansionPlan {
    YES("yes", "Ja, Expansion geplant"),
    NO("no", "Nein"),
    UNSURE("unsure", "Unklar");
}

// ContactRole.java (NEU)
public enum ContactRole {
    CHEF("chef", "Küchenchef"),
    BUYER("buyer", "Einkäufer"),
    DECISION_MAKER("decision_maker", "Entscheider");
}

// ActivityType.java (NEU)
public enum ActivityType {
    ORDER("order", "Bestellung"),
    MEETING("meeting", "Meeting"),
    CALL("call", "Anruf"),
    EMAIL("email", "E-Mail"),
    NOTE("note", "Notiz");
}
```

### Neue Enum-Resources

```java
// EnumResource.java (erweitern)

@GET
@Path("/expansion-plan")
@Produces(MediaType.APPLICATION_JSON)
public List<EnumValue> getExpansionPlan() {
    return Arrays.stream(ExpansionPlan.values())
        .map(e -> new EnumValue(e.getValue(), e.getLabel()))
        .collect(Collectors.toList());
}
```

---

## ✅ ACCEPTANCE CRITERIA (Gesamt)

### Funktional
- [ ] Kompakte Kunden-Übersicht als Default-View
- [ ] Button [Alle Details anzeigen] öffnet Tab-View
- [ ] Tab-View mit 3 Tabs (Firma, Geschäft, Verlauf)
- [ ] Tab "Firma" zeigt 3 Cards in 2-Spalten Grid
- [ ] Tab "Geschäft" zeigt 4 Cards in 2-Spalten Grid
- [ ] Tab "Verlauf" zeigt Kontakte + Timeline (Phase 4)
- [ ] Multi-Location: Summary in Compact View, Details in Tab "Firma"
- [ ] Expansion-Dropdown funktioniert (ExpansionPlan ENUM)
- [ ] Navigation `/customers/:id` → `/customers/:id/details` funktioniert
- [ ] Browser-Back funktioniert korrekt

### Technisch
- [ ] Routing mit React Router korrekt implementiert
- [ ] MUI Grid v7 API verwendet (`size={{ xs: 12, md: 6 }}`)
- [ ] Design System eingehalten (keine hardcoded colors/fonts)
- [ ] Server-Driven Architektur beibehalten (Backend definiert Schema)
- [ ] Test Coverage ≥80% für neue Components
- [ ] Keine ESLint/TS Errors

### Performance
- [ ] Compact View lädt < 200ms
- [ ] Tab-Wechsel < 100ms
- [ ] Lazy Loading für Tab-Content (nur aktiver Tab lädt Daten)

---

## 🎯 DONE CRITERIA

**D11 Customer Detail View Redesign ist COMPLETE wenn:**

1. ✅ CustomerCompactView als Default implementiert
2. ✅ CustomerDetailView mit Tabs funktionsfähig
3. ✅ Tab "Firma" mit Multi-Location Details
4. ✅ Tab "Geschäft" mit allen 4 Cards
5. ✅ Routing `/customers/:id` → `/customers/:id/details` funktioniert
6. ✅ Browser-Back funktioniert
7. ✅ Tests ≥80% Coverage
8. ✅ Design System Compliance (Pre-Commit Hook grün)
9. ✅ PR merged to main
10. ✅ Customer KD-DEV-123 (Super-Customer C1) zeigt neue Views korrekt

---

**🤖 Erstellt mit Claude Code - Architektur-Spezifikation V1.0**
