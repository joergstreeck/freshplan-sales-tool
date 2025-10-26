# 🎯 SPEC: Customer Detail View - FINALE VERSION

**📅 Erstellt:** 2025-10-26
**📝 Status:** ✅ FINAL - VERBINDLICH
**👤 Autor:** Jörg Streeck + Strategic Consultant
**🚨 KRITISCH:** Alle vorherigen SPEC_D11 Versionen sind VERALTET und UNGÜLTIG!

---

## 🚨 ARCHITEKTUR-ÜBERSICHT (was wirklich gebaut werden muss)

**Ich schreibe dir JETZT die finale SPEC. Hier direkt im Chat, damit du sie Claude Code geben kannst.**

---

# 📋 FINALE SPEC: Customer Detail View - Korrekte Architektur

**Datum:** 2025-10-26  
**Sprint:** 2.1.7.2 - D11 Customer Detail View (NEUSTART)  
**Status:** 🔴 ALTE SPECS UNGÜLTIG - Dies ist die einzige korrekte Spezifikation

---

## 🎯 MISSION STATEMENT

**Ein CRM-Kundenverwaltung mit:**
1. Kompakter Übersicht für tägliche Arbeit (80% Use Case)
2. Detail-Ansicht für Datenpflege (20% Use Case)
3. Strukturierten Adressen (Straße, PLZ, Ort - kein Freitext!)
4. Kontaktverwaltung (CHEF/BUYER Multi-Contact-B2B)
5. Lead→Customer Konversion ohne Datenverlust

---

## 🚀 NAVIGATION-FLOW (exakt so!)

```
┌─────────────────────────────────────────────────┐
│ Kundenliste (CustomersPageV2)                   │
│ ├─ Button [+ Neuer Kunde] → Wizard (bleibt)    │
│ └─ Click auf Tabellenzeile                      │
└─────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────┐
│ Kompakte Übersicht (CustomerCompactView)        │
│ ├─ Erscheint rechts (wie Cockpit-Arbeitsbereich)│
│ ├─ Zeigt: Name, Status, Umsatz, Standorte      │
│ ├─ Quick Actions: [E-Mail][Anrufen][Aktivität] │
│ └─ Button: [🔍 Alle Details anzeigen]           │
└─────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────┐
│ Detail-Ansicht (CustomerDetailModal)            │
│ ├─ Vollbild-Modal/Drawer                       │
│ ├─ Eigener [← Zurück] Button oben links        │
│ ├─ Tab "Firma" (3 Cards)                       │
│ ├─ Tab "Geschäft" (4 Cards)                    │
│ └─ Tab "Verlauf" (Kontakte + Timeline)         │
└─────────────────────────────────────────────────┘
```

---

## 📊 DATENSTRUKTUREN (Backend Entities)

### 1. Customer Entity - Adressfelder

```java
@Entity
@Table(name = "customers")
public class Customer {
    // ... existing fields ...
    
    // HAUPTADRESSE (= Rechnungsadresse, strukturiert!)
    // Gleiche Feldnamen wie Lead für 1:1 Konversion!
    @Column(name = "street")
    private String street;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "city")
    private String city;

    @Column(name = "country_code")
    private String countryCode; // "DE", "CH", "AT"
    
    // LIEFERADRESSEN (JSONB Array mit Struktur!)
    @Type(JsonBinaryType.class)
    @Column(name = "delivery_addresses", columnDefinition = "jsonb")
    private List<DeliveryAddress> deliveryAddresses;
    
    // STANDORT-STATISTIK
    @Column(name = "locations_de")
    private Integer locationsDE;
    
    @Column(name = "locations_ch")
    private Integer locationsCH;
    
    @Column(name = "locations_at")
    private Integer locationsAT;
    
    @Column(name = "expansion_planned")
    @Enumerated(EnumType.STRING)
    private ExpansionPlanned expansionPlanned;
}
```

### 2. DeliveryAddress (JSONB Struktur)

```java
public class DeliveryAddress {
    private String locationName;      // "Filiale München"
    private String street;            // "Hauptstraße 123"
    private String postalCode;        // "80331"
    private String city;              // "München"
    private String countryCode;       // "DE", "CH", "AT" (String, NICHT Country enum!)
    private Boolean isActive;         // true/false
    private String notes;             // Optional
}
```

### 3. Contact Entity (NEU!)

```java
@Entity
@Table(name = "customer_contacts")
public class Contact {
    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;
    
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;
    
    @Column(name = "first_name", nullable = false)
    private String firstName;
    
    @Column(name = "last_name", nullable = false)
    private String lastName;
    
    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private ContactRole role; // CHEF, BUYER, MANAGER, OTHER
    
    @Column(name = "email")
    private String email;
    
    @Column(name = "phone")
    private String phone;
    
    @Column(name = "mobile")
    private String mobile;
    
    @Column(name = "location_id")
    private UUID locationId; // Optional: Zuordnung zu Lieferadresse
    
    @Column(name = "is_primary")
    private Boolean isPrimary; // Hauptansprechpartner
    
    @Column(name = "notes")
    private String notes;
    
    @Column(name = "created_at")
    private Instant createdAt;
    
    @Column(name = "created_by")
    private String createdBy;
}
```

---

## 🔌 BACKEND ENDPOINTS

### CustomerSchemaResource.java (7 Cards)

**Endpoint:** `GET /api/customers/{id}/profile`

**Card 1: company_profile**
```json
{
  "cardId": "company_profile",
  "title": "Unternehmensprofil",
  "order": 1,
  "fields": [
    {"fieldId": "customerNumber", "label": "Kundennummer", "type": "text", "readOnly": true},
    {"fieldId": "companyName", "label": "Firmenname", "type": "text", "required": true},
    {"fieldId": "tradingName", "label": "Handelsname", "type": "text"},
    {"fieldId": "customerType", "label": "Kundentyp", "type": "select", "options": ["UNTERNEHMEN", "EINZELPERSON"]},
    {"fieldId": "businessType", "label": "Branche", "type": "select", "options": ["RESTAURANT", "HOTEL", "CATERING", "KANTINE", "GROSSHANDEL", "LEH", "BILDUNG", "GESUNDHEIT", "SONSTIGES"]},
    {"fieldId": "legalForm", "label": "Rechtsform", "type": "select"},
    {"fieldId": "status", "label": "Status", "type": "select", "enumSource": "/api/enums/customer-status", "options": ["LEAD", "PROSPECT", "AKTIV", "RISIKO", "INAKTIV", "ARCHIVIERT"]},
    {"fieldId": "originalLeadId", "label": "Original Lead ID", "type": "text", "readOnly": true}
  ]
}
```

**Card 2: locations**
```json
{
  "cardId": "locations",
  "title": "Standorte",
  "order": 2,
  "fields": [
    {
      "fieldId": "mainAddress",
      "label": "Hauptadresse (= Rechnungsadresse)",
      "type": "group",
      "fields": [
        {"fieldId": "street", "label": "Straße", "type": "text"},
        {"fieldId": "postalCode", "label": "PLZ", "type": "text"},
        {"fieldId": "city", "label": "Ort", "type": "text"},
        {"fieldId": "countryCode", "label": "Land", "type": "select", "options": ["DE", "CH", "AT"]}
      ]
    },
    {
      "fieldId": "deliveryAddresses",
      "label": "Lieferadressen",
      "type": "array",
      "itemSchema": {
        "fields": [
          {"fieldId": "locationName", "label": "Standort-Name", "type": "text"},
          {"fieldId": "street", "label": "Straße", "type": "text"},
          {"fieldId": "postalCode", "label": "PLZ", "type": "text"},
          {"fieldId": "city", "label": "Ort", "type": "text"},
          {"fieldId": "countryCode", "label": "Land", "type": "select", "options": ["DE", "CH", "AT"]},
          {"fieldId": "isActive", "label": "Aktiv", "type": "boolean"}
        ]
      }
    },
    {"fieldId": "locationsDE", "label": "Standorte Deutschland", "type": "number"},
    {"fieldId": "locationsCH", "label": "Standorte Schweiz", "type": "number"},
    {"fieldId": "locationsAT", "label": "Standorte Österreich", "type": "number"},
    {"fieldId": "expansionPlanned", "label": "Expansion geplant?", "type": "select", "options": ["YES", "NO", "UNKNOWN"]}
  ]
}
```

**Card 3-7:** Wie bisher (classification, business_data, contracts, pain_points, products)

---

### CustomerContactResource.java (NEU!)

```java
@Path("/api/customers/{customerId}/contacts")
public class CustomerContactResource {
    
    @GET
    public List<ContactDTO> listContacts(@PathParam("customerId") UUID customerId) {
        // Returns all contacts for customer
    }
    
    @POST
    public ContactDTO createContact(
        @PathParam("customerId") UUID customerId,
        @Valid ContactDTO contact
    ) {
        // Creates new contact
    }
    
    @PUT
    @Path("/{contactId}")
    public ContactDTO updateContact(
        @PathParam("customerId") UUID customerId,
        @PathParam("contactId") UUID contactId,
        @Valid ContactDTO contact
    ) {
        // Updates existing contact
    }
    
    @DELETE
    @Path("/{contactId}")
    public void deleteContact(
        @PathParam("customerId") UUID customerId,
        @PathParam("contactId") UUID contactId
    ) {
        // Deletes contact
    }
}
```

---

### CustomerSummaryResource.java (NEU!)

```java
@Path("/api/customers/{id}/summary")
public class CustomerSummaryResource {
    
    @GET
    public CustomerSummaryDTO getSummary(@PathParam("id") UUID customerId) {
        return new CustomerSummaryDTO(
            companyName,
            status,
            expectedAnnualVolume,
            locationCount,
            locationNames, // Top 3
            primaryContactName,
            primaryContactEmail,
            riskScore,
            lastContactDate,
            nextSteps // aus Activities
        );
    }
}
```

---

### CustomerResource.changeCustomerStatus() - 3-Tier Role-Based Authorization

**Sprint 2.1.7.2 D11:** Customer Status Update mit rollenbasierter Zugriffssteuerung

**Endpoint:** `PUT /api/customers/{id}/status`

**Request Body:**
```json
{
  "newStatus": "AKTIV"
}
```

**Authorization Rules (3-Tier):**

| Rolle | Erlaubte Status-Änderungen | Einschränkungen |
|-------|---------------------------|----------------|
| **SALES** (sales) | AKTIV ↔ RISIKO | Kann NUR zwischen AKTIV und RISIKO wechseln |
| **MANAGER** (manager) | AKTIV, RISIKO, INAKTIV | Kann zusätzlich INAKTIV setzen, ABER NICHT ARCHIVIERT |
| **ADMIN** (admin) | Alle Status | Keine Einschränkungen, kann auch ARCHIVIERT setzen |

**Business Rules:**
- System setzt Status automatisch (z.B. Lead→AKTIV bei Konversion, Churn→RISIKO)
- Benutzer können Status manuell überschreiben (basierend auf ihrer Rolle)
- Bei unzureichenden Berechtigungen: HTTP 403 mit benutzerfreundlicher Fehlermeldung

**Error Responses:**

```json
{
  "message": "Sales users can only change between AKTIV and RISIKO. Contact your manager to set other statuses.",
  "errorCode": "INSUFFICIENT_PERMISSIONS"
}
```

```json
{
  "message": "Only ADMIN can set ARCHIVIERT status. Contact your administrator.",
  "errorCode": "INSUFFICIENT_PERMISSIONS"
}
```

**Implementation:**
- Datei: `CustomerResource.java` (Zeile 393-494)
- Enum Endpoint: `/api/enums/customer-status` (siehe `EnumResource.java`)
- Validierung: Role-based checks BEFORE calling service layer

---

## 🎨 FRONTEND COMPONENTS

### 1. CustomerCompactView.tsx (NEU!)

```typescript
interface CustomerCompactViewProps {
  customerId: string;
  onShowDetails: () => void;
}

export const CustomerCompactView: React.FC<CustomerCompactViewProps> = ({
  customerId,
  onShowDetails
}) => {
  const { data: summary } = useCustomerSummary(customerId);
  
  return (
    <Box sx={{ p: 3 }}>
      {/* Header */}
      <Typography variant="h4">{summary.companyName}</Typography>
      <Chip label={summary.status} color="primary" />
      <Typography variant="body2">
        Jahresumsatz: {formatCurrency(summary.expectedAnnualVolume)}
      </Typography>
      
      {/* Standorte-Summary */}
      <Box sx={{ mt: 2 }}>
        <PlaceIcon />
        <Typography>
          {summary.locationCount} Standorte: {summary.locationNames.join(', ')}
        </Typography>
      </Box>
      
      {/* Risiko & Kontakt */}
      <Typography>Risiko-Score: {summary.riskScore}%</Typography>
      <Typography>Letzter Kontakt: {formatDate(summary.lastContactDate)}</Typography>
      
      {/* Nächste Schritte */}
      <Box sx={{ mt: 2 }}>
        <Typography variant="h6">🎯 Nächste Schritte:</Typography>
        {summary.nextSteps.map(step => (
          <Typography key={step}>• {step}</Typography>
        ))}
      </Box>
      
      {/* Hauptansprechpartner */}
      <Box sx={{ mt: 2 }}>
        <Typography variant="h6">📞 Hauptansprechpartner:</Typography>
        <Typography>{summary.primaryContactName}</Typography>
        <Typography>{summary.primaryContactEmail}</Typography>
      </Box>
      
      {/* Quick Actions */}
      <Stack direction="row" spacing={2} sx={{ mt: 3 }}>
        <Button variant="contained">E-Mail</Button>
        <Button variant="contained">Anrufen</Button>
        <Button variant="contained">Aktivität</Button>
      </Stack>
      
      {/* Details Button */}
      <Button
        variant="outlined"
        fullWidth
        sx={{ mt: 3 }}
        onClick={onShowDetails}
      >
        🔍 Alle Details anzeigen
      </Button>
    </Box>
  );
};
```

---

### 2. CustomerDetailModal.tsx (NEU!)

```typescript
interface CustomerDetailModalProps {
  customerId: string;
  open: boolean;
  onClose: () => void;
}

export const CustomerDetailModal: React.FC<CustomerDetailModalProps> = ({
  customerId,
  open,
  onClose
}) => {
  const [activeTab, setActiveTab] = useState(0);
  
  return (
    <Drawer
      open={open}
      onClose={onClose}
      anchor="right"
      sx={{ width: '100%', maxWidth: '100vw' }}
      PaperProps={{ sx: { width: '100%' } }}
    >
      {/* Header mit Zurück-Button */}
      <Box sx={{ p: 2, display: 'flex', alignItems: 'center' }}>
        <IconButton onClick={onClose}>
          <ArrowBackIcon />
        </IconButton>
        <Typography variant="h5" sx={{ ml: 2 }}>
          Kundendetails
        </Typography>
      </Box>
      
      {/* Tabs */}
      <Tabs value={activeTab} onChange={(e, v) => setActiveTab(v)}>
        <Tab label="Firma" />
        <Tab label="Geschäft" />
        <Tab label="Verlauf" />
      </Tabs>
      
      {/* Tab Content */}
      <Box sx={{ p: 3 }}>
        {activeTab === 0 && <CustomerDetailTabFirma customerId={customerId} />}
        {activeTab === 1 && <CustomerDetailTabGeschaeft customerId={customerId} />}
        {activeTab === 2 && <CustomerDetailTabVerlauf customerId={customerId} />}
      </Box>
    </Drawer>
  );
};
```

---

### 3. CustomerDetailTabVerlauf.tsx (NEU!)

```typescript
export const CustomerDetailTabVerlauf: React.FC<{ customerId: string }> = ({
  customerId
}) => {
  const { data: contacts } = useCustomerContacts(customerId);
  const [contactDialogOpen, setContactDialogOpen] = useState(false);
  
  return (
    <Box>
      {/* Section 1: Ansprechpartner */}
      <Paper sx={{ p: 3, mb: 3 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
          <Typography variant="h6">Ansprechpartner</Typography>
          <Button
            variant="contained"
            onClick={() => setContactDialogOpen(true)}
          >
            + Neuer Kontakt
          </Button>
        </Box>
        
        <List>
          {contacts?.map(contact => (
            <ListItem key={contact.id}>
              <ListItemText
                primary={`${contact.firstName} ${contact.lastName}`}
                secondary={
                  <>
                    <Chip label={contact.role} size="small" sx={{ mr: 1 }} />
                    {contact.email} • {contact.phone}
                  </>
                }
              />
              <IconButton onClick={() => handleEditContact(contact)}>
                <EditIcon />
              </IconButton>
            </ListItem>
          ))}
        </List>
      </Paper>
      
      {/* Section 2: Timeline (Platzhalter) */}
      <Paper sx={{ p: 3 }}>
        <Typography variant="h6">Timeline</Typography>
        <Typography color="text.secondary" sx={{ mt: 2 }}>
          Kommunikation und Aktivitäten werden in Sprint 2.2.x implementiert.
        </Typography>
      </Paper>
      
      {/* Contact Dialog */}
      <ContactEditDialog
        open={contactDialogOpen}
        onClose={() => setContactDialogOpen(false)}
        customerId={customerId}
      />
    </Box>
  );
};
```

---

### 4. ContactEditDialog.tsx (NEU!)

```typescript
export const ContactEditDialog: React.FC<{
  open: boolean;
  onClose: () => void;
  customerId: string;
  contact?: Contact;
}> = ({ open, onClose, customerId, contact }) => {
  const [formData, setFormData] = useState({
    firstName: contact?.firstName || '',
    lastName: contact?.lastName || '',
    role: contact?.role || 'CHEF',
    email: contact?.email || '',
    phone: contact?.phone || '',
    mobile: contact?.mobile || '',
    isPrimary: contact?.isPrimary || false,
    notes: contact?.notes || ''
  });
  
  const handleSave = async () => {
    if (contact) {
      await updateContact(customerId, contact.id, formData);
    } else {
      await createContact(customerId, formData);
    }
    onClose();
  };
  
  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>
        {contact ? 'Kontakt bearbeiten' : 'Neuer Kontakt'}
      </DialogTitle>
      <DialogContent>
        <Grid container spacing={2} sx={{ mt: 1 }}>
          <Grid size={{ xs: 6 }}>
            <TextField
              label="Vorname"
              value={formData.firstName}
              onChange={e => setFormData({...formData, firstName: e.target.value})}
              required
              fullWidth
            />
          </Grid>
          <Grid size={{ xs: 6 }}>
            <TextField
              label="Nachname"
              value={formData.lastName}
              onChange={e => setFormData({...formData, lastName: e.target.value})}
              required
              fullWidth
            />
          </Grid>
          <Grid size={{ xs: 12 }}>
            <TextField
              select
              label="Rolle"
              value={formData.role}
              onChange={e => setFormData({...formData, role: e.target.value})}
              fullWidth
            >
              <MenuItem value="CHEF">Küchenchef (CHEF)</MenuItem>
              <MenuItem value="BUYER">Einkäufer (BUYER)</MenuItem>
              <MenuItem value="MANAGER">Manager</MenuItem>
              <MenuItem value="OTHER">Sonstiges</MenuItem>
            </TextField>
          </Grid>
          <Grid size={{ xs: 12 }}>
            <TextField
              label="E-Mail"
              type="email"
              value={formData.email}
              onChange={e => setFormData({...formData, email: e.target.value})}
              fullWidth
            />
          </Grid>
          <Grid size={{ xs: 6 }}>
            <TextField
              label="Telefon"
              value={formData.phone}
              onChange={e => setFormData({...formData, phone: e.target.value})}
              fullWidth
            />
          </Grid>
          <Grid size={{ xs: 6 }}>
            <TextField
              label="Mobil"
              value={formData.mobile}
              onChange={e => setFormData({...formData, mobile: e.target.value})}
              fullWidth
            />
          </Grid>
          <Grid size={{ xs: 12 }}>
            <FormControlLabel
              control={
                <Checkbox
                  checked={formData.isPrimary}
                  onChange={e => setFormData({...formData, isPrimary: e.target.checked})}
                />
              }
              label="Hauptansprechpartner"
            />
          </Grid>
          <Grid size={{ xs: 12 }}>
            <TextField
              label="Notizen"
              value={formData.notes}
              onChange={e => setFormData({...formData, notes: e.target.value})}
              multiline
              rows={3}
              fullWidth
            />
          </Grid>
        </Grid>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Abbrechen</Button>
        <Button onClick={handleSave} variant="contained">Speichern</Button>
      </DialogActions>
    </Dialog>
  );
};
```

---

### 5. CustomersPageV2.tsx (ÄNDERN!)

```typescript
// VORHER:
const handleRowClick = (customer: Customer) => {
  navigate(`/customers/${customer.id}`); // ❌ FALSCH
};

// NACHHER:
const [selectedCustomerId, setSelectedCustomerId] = useState<string | null>(null);
const [detailModalOpen, setDetailModalOpen] = useState(false);

const handleRowClick = (customer: Customer) => {
  setSelectedCustomerId(customer.id);
  // Zeige CustomerCompactView im rechten Bereich (wie Cockpit)
};

const handleShowDetails = () => {
  setDetailModalOpen(true);
};

// Im JSX:
<Box display="flex">
  <Box flex={1}>
    <VirtualizedCustomerTable onRowClick={handleRowClick} />
  </Box>
  
  {selectedCustomerId && (
    <Box width={400} sx={{ borderLeft: 1, borderColor: 'divider' }}>
      <CustomerCompactView
        customerId={selectedCustomerId}
        onShowDetails={handleShowDetails}
      />
    </Box>
  )}
</Box>

<CustomerDetailModal
  customerId={selectedCustomerId}
  open={detailModalOpen}
  onClose={() => setDetailModalOpen(false)}
/>
```

---

## 🔄 LEAD → CUSTOMER CONVERSION

### OpportunityService.convertToCustomer() - ANPASSEN!

```java
public Customer convertToCustomer(Lead lead, Opportunity opportunity) {
    Customer customer = new Customer();
    
    // Stammdaten
    customer.setCompanyName(lead.getCompanyName());
    customer.setBusinessType(lead.getBusinessType());
    customer.setExpectedAnnualVolume(lead.getEstimatedVolume());
    customer.setOriginalLeadId(lead.getId());
    
    // HAUPTADRESSE (strukturiert! - gleiche Feldnamen wie Lead für 1:1 Konversion)
    customer.setStreet(lead.getStreet());
    customer.setPostalCode(lead.getPostalCode());
    customer.setCity(lead.getCity());
    customer.setCountryCode(lead.getCountryCode());

    // LIEFERADRESSEN (Initial: gleich wie Rechnungsadresse)
    DeliveryAddress initialDelivery = new DeliveryAddress();
    initialDelivery.setLocationName("Hauptstandort");
    initialDelivery.setStreet(lead.getStreet());
    initialDelivery.setPostalCode(lead.getPostalCode());
    initialDelivery.setCity(lead.getCity());
    initialDelivery.setCountryCode(lead.getCountryCode());
    initialDelivery.setIsActive(true);
    customer.setDeliveryAddresses(List.of(initialDelivery));
    
    // Pain Points (alle 8!)
    customer.setPainStaffShortage(lead.getPainStaffShortage());
    customer.setPainHighCosts(lead.getPainHighCosts());
    customer.setPainFoodWaste(lead.getPainFoodWaste());
    customer.setPainQualityInconsistency(lead.getPainQualityInconsistency());
    customer.setPainTimePressure(lead.getPainTimePressure());
    customer.setPainSupplierQuality(lead.getPainSupplierQuality());
    customer.setPainUnreliableDelivery(lead.getPainUnreliableDelivery());
    customer.setPainPoorService(lead.getPainPoorService());
    customer.setPainNotes(lead.getPainNotes());
    
    // Speichern
    customer = customerRepository.save(customer);
    
    // KONTAKT erstellen (Primary!)
    Contact primaryContact = new Contact();
    primaryContact.setCustomer(customer);
    primaryContact.setFirstName(lead.getContactFirstName());
    primaryContact.setLastName(lead.getContactLastName());
    primaryContact.setEmail(lead.getEmail());
    primaryContact.setPhone(lead.getPhone());
    primaryContact.setRole(ContactRole.CHEF); // oder aus Lead-Feld
    primaryContact.setIsPrimary(true);
    contactRepository.save(primaryContact);
    
    return customer;
}
```

---

## ✅ ACCEPTANCE CRITERIA

### Funktional
- [ ] Kundenliste → Click auf Kunde → Kompakte Übersicht erscheint rechts
- [ ] Kompakte Übersicht zeigt: Name, Status, Umsatz, Standorte-Summary, Kontakte-Summary
- [ ] Button "Alle Details anzeigen" → Vollbild-Modal öffnet sich
- [ ] Modal hat [← Zurück] Button (schließt Modal)
- [ ] Modal hat 3 Tabs: Firma, Geschäft, Verlauf
- [ ] Tab "Firma" → Card "Standorte" hat strukturierte Adressfelder (Straße, PLZ, Ort, Land)
- [ ] Tab "Verlauf" → Section "Ansprechpartner" zeigt alle Kontakte
- [ ] Button [+ Neuer Kontakt] → Dialog öffnet sich
- [ ] Kontakt-Dialog hat: Vorname, Nachname, Rolle (CHEF/BUYER), E-Mail, Telefon, isPrimary
- [ ] Lead → Customer Conversion kopiert Adresse strukturiert
- [ ] Lead → Customer Conversion erstellt Primary Contact

### Technisch
- [ ] Backend: Customer Entity hat street, postalCode, city, countryCode (gleiche Feldnamen wie Lead!)
- [ ] Backend: Contact Entity existiert mit CHEF/BUYER Rolle
- [ ] Backend: GET /api/customers/{id}/summary liefert Daten für kompakte Übersicht
- [ ] Backend: GET /api/customers/{id}/contacts liefert alle Kontakte
- [ ] Backend: POST /api/customers/{id}/contacts erstellt neuen Kontakt
- [ ] Frontend: CustomerCompactView.tsx existiert
- [ ] Frontend: CustomerDetailModal.tsx existiert
- [ ] Frontend: CustomerDetailTabVerlauf.tsx existiert
- [ ] Frontend: ContactEditDialog.tsx existiert
- [ ] CustomersPageV2 zeigt kompakte Übersicht rechts (nicht Navigate!)

### Performance
- [ ] Kompakte Übersicht lädt < 300ms
- [ ] Modal öffnet < 200ms
- [ ] Tab-Wechsel < 100ms

---

## 🚨 KRITISCHE FEHLER VERMEIDEN

**❌ NICHT machen:**
1. Adressen als Freitext (nur "billingAddress" String)
2. Kontakte in Tab "Verlauf" auf "später" verschieben
3. Navigate zu `/customers/:id` (stattdessen: Kompakte Übersicht rechts!)
4. Lead-Conversion ohne strukturierte Adresse
5. Lead-Conversion ohne Contact erstellen

**✅ IMMER machen:**
1. Adressen strukturiert (Straße, PLZ, Ort, Land als separate Felder)
2. Kontakte JETZT implementieren (nicht später!)
3. Kompakte Übersicht ZUERST zeigen (dann Details-Modal)
4. Lead→Customer: Adresse + Contact kopieren
5. Bei Unklarheit: CRM_AI_CONTEXT_SCHNELL.md lesen!

---

## 📋 IMPLEMENTIERUNGS-REIHENFOLGE

**Phase 1: Backend Customer & Contact (3h)**
1. Customer Entity: Adressfelder hinzufügen (street, postalCode, city, countryCode - EXAKT wie Lead!)
2. Contact Entity prüfen (existiert bereits!)
3. CustomerContactResource.java prüfen (existiert bereits!)
4. CustomerSummaryResource.java erstellen
5. OpportunityService.convertToCustomer() anpassen

**Phase 2: Backend Server-Driven Cards (2h)**
6. CustomerSchemaResource: Card "locations" strukturiert umbauen
7. Alle anderen Cards prüfen (pain_points, business_data, etc.)

**Phase 3: Frontend Components (4h)**
8. CustomerCompactView.tsx erstellen
9. CustomerDetailModal.tsx erstellen
10. CustomerDetailTabVerlauf.tsx erstellen
11. ContactEditDialog.tsx erstellen
12. CustomersPageV2.tsx anpassen (Kompakte Übersicht rechts zeigen)

**Phase 4: Testing (1h)**
13. Lead erstellen → Customer konvertieren → Adresse prüfen
14. Contact prüfen (von Lead kopiert)
15. Neuen Kontakt anlegen
16. Standort hinzufügen
17. Modal öffnen/schließen

**Gesamt: ~10 Stunden**

---

## 📝 DONE CRITERIA

**D11 ist COMPLETE wenn:**
1. ✅ Kompakte Übersicht erscheint rechts bei Click auf Kunde
2. ✅ Button "Alle Details" öffnet Vollbild-Modal mit Tabs
3. ✅ Adressen sind strukturiert (Straße, PLZ, Ort, Land)
4. ✅ Kontakte können angelegt/bearbeitet werden (CHEF/BUYER)
5. ✅ Lead→Customer Conversion kopiert Adresse + Contact
6. ✅ Tests ≥80% Coverage
7. ✅ Browser-Test erfolgreich (Kunde KD-DEV-123)

---

**🤖 Ende der SPEC - Dies ist die einzige gültige Spezifikation für D11**