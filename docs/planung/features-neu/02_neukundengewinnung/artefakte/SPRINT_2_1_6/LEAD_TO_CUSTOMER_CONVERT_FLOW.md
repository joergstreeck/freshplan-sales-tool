# Lead → Customer Convert Flow - Sprint 2.1.6

**Dokumenttyp:** Business Process & Technical Specification
**Status:** Planned (Sprint 2.1.6)
**Owner:** team/leads-backend + team/customer-management
**Sprint:** 2.1.6 (12-18.10.2025)

---

## 📋 Übersicht

Dieses Dokument spezifiziert den **automatischen Convert-Flow** von qualifizierten Leads zu Kunden im FreshFoodz CRM.

**Business-Kontext:**
Wenn ein Lead den Status QUALIFIED → CONVERTED erreicht, soll automatisch ein Kunde-Datensatz angelegt werden. **ZERO Doppeleingabe** - alle Lead-Daten werden 1:1 übernommen.

**Kritische Anforderungen:**
1. **Automatische Trigger** bei Status QUALIFIED → CONVERTED
2. **Alle Daten übernehmen** (Company, Contact, Territory, Notes, History)
3. **Duplikaten-Check** gegen existierende Kunden
4. **Historie vollständig erhalten** (Lead-Activities → Customer-Activities)
5. **Lead-ID Verknüpfung** in `customer.original_lead_id`
6. **Navigation nach Convert** zu `/customer-management/customers/{id}`

---

## 🎯 Business-Kontext

### Lead-Lifecycle

```
STAGE 0 (Vormerkung)
    ↓
STAGE 1 (Lead-Registrierung)
    ↓
STAGE 2 (Qualifiziert) ← Hier erfolgt der Convert
    ↓
STATUS: QUALIFIED → CONVERTED
    ↓
✨ AUTOMATISCHE KUNDE-ANLAGE ✨
    ↓
Kunde in Modul 03 Kundenmanagement
```

### Conversion-Kriterien

**Ein Lead ist conversion-ready wenn:**
1. **Stage = 2** (Qualifiziert)
2. **Mindestens 1 Progress-Activity** in letzten 60 Tagen
3. **Estimated Volume definiert** (ROI-Berechnung durchgeführt)
4. **Contact-Daten vollständig** (Name, Email/Phone)
5. **Consent erteilt** (DSGVO-Konform)

---

## 🏗️ Technische Implementierung

### 1. Automatischer Trigger

**Option A: Status-Update Endpoint (Explizit)**
```java
@PUT
@Path("/{id}/status")
@RolesAllowed({"partner", "manager", "admin"})
@Transactional
public Response updateLeadStatus(
    @PathParam("id") Long leadId,
    @Valid LeadStatusUpdateRequest request
) {
    Lead lead = leadRepository.findById(leadId);

    // Alten Status speichern
    LeadStatus oldStatus = lead.getStatus();

    // Neuen Status setzen
    lead.setStatus(request.status());

    // Conversion-Trigger
    if (oldStatus == LeadStatus.QUALIFIED &&
        request.status() == LeadStatus.CONVERTED) {

        // Automatische Kunde-Anlage
        Customer customer = leadConversionService.convertToCustomer(lead);

        auditService.log("lead_converted_to_customer", lead.getId(), Map.of(
            "customer_id", customer.getId()
        ));

        return Response.ok(Map.of(
            "lead_id", lead.getId(),
            "customer_id", customer.getId(),
            "redirect_url", "/customer-management/customers/" + customer.getId()
        )).build();
    }

    return Response.ok(lead).build();
}
```

**Option B: Dedizierter Convert-Endpoint (Expliziter)**
```java
@POST
@Path("/{id}/convert-to-customer")
@RolesAllowed({"partner", "manager", "admin"})
@Transactional
public Response convertToCustomer(@PathParam("id") Long leadId) {
    Lead lead = leadRepository.findById(leadId);

    // Validierung: Nur QUALIFIED Leads konvertieren
    if (lead.getStatus() != LeadStatus.QUALIFIED) {
        throw new BadRequestException("Only QUALIFIED leads can be converted");
    }

    // Duplikaten-Check
    Optional<Customer> existingCustomer = customerRepository
        .findByOriginalLeadId(leadId);

    if (existingCustomer.isPresent()) {
        throw new ConflictException("Lead already converted to customer: " +
                                   existingCustomer.get().getId());
    }

    // Conversion durchführen
    Customer customer = leadConversionService.convertToCustomer(lead);

    // Lead-Status aktualisieren
    lead.setStatus(LeadStatus.CONVERTED);
    lead.setConvertedAt(Instant.now());
    lead.setConvertedToCustomerId(customer.getId());

    return Response.created(
        URI.create("/api/customers/" + customer.getId())
    ).entity(customer).build();
}
```

**Empfehlung:** Option B (Dedizierter Endpoint) für bessere Explizitheit

### 2. Daten-Mapping

**LeadConversionService.java:**
```java
@ApplicationScoped
public class LeadConversionService {

    @Inject
    CustomerRepository customerRepository;

    @Inject
    LeadActivityRepository leadActivityRepository;

    @Inject
    CustomerActivityRepository customerActivityRepository;

    @Transactional
    public Customer convertToCustomer(Lead lead) {
        // 1. Duplikaten-Check
        checkForDuplicates(lead);

        // 2. Customer-Entität erstellen
        Customer customer = new Customer();

        // Company-Daten übernehmen
        customer.setCompanyName(lead.getCompanyName());
        customer.setBusinessType(lead.getBusinessType());
        customer.setCity(lead.getCity());
        customer.setPostalCode(lead.getPostalCode());
        customer.setStreet(lead.getStreet());
        customer.setTerritory(lead.getTerritory());

        // Contact-Daten übernehmen
        if (lead.getContact() != null) {
            CustomerContact contact = new CustomerContact();
            contact.setFirstName(lead.getContact().getFirstName());
            contact.setLastName(lead.getContact().getLastName());
            contact.setEmail(lead.getContact().getEmail());
            contact.setEmailNormalized(lead.getContact().getEmailNormalized());
            contact.setPhone(lead.getContact().getPhone());
            contact.setPhoneNormalized(lead.getContact().getPhoneNormalized());
            contact.setRole(lead.getContact().getRole());
            contact.setIsPrimary(true);

            customer.setContacts(List.of(contact));
        }

        // Business-Daten übernehmen
        customer.setEstimatedVolume(lead.getEstimatedVolume());
        customer.setVatId(lead.getVatId());
        customer.setNotes(lead.getNotes());

        // Ownership übernehmen
        customer.setOwnerUserId(lead.getOwnerUserId());
        customer.setOwnerTeamId(lead.getOwnerTeamId());

        // Lead-Verknüpfung
        customer.setOriginalLeadId(lead.getId());
        customer.setConvertedFrom("lead");
        customer.setConvertedAt(Instant.now());

        // Lifecycle-Status
        customer.setCustomerStatus(CustomerStatus.ACTIVE);
        customer.setOnboardingStatus(OnboardingStatus.PENDING);

        customerRepository.persist(customer);

        // 3. Activities migrieren
        migrateActivities(lead, customer);

        // 4. Notes/Tags migrieren (falls vorhanden)
        migrateNotesAndTags(lead, customer);

        return customer;
    }

    private void migrateActivities(Lead lead, Customer customer) {
        List<LeadActivity> leadActivities = leadActivityRepository
            .findByLeadId(lead.getId());

        for (LeadActivity leadActivity : leadActivities) {
            CustomerActivity customerActivity = new CustomerActivity();

            // Activity-Daten übernehmen
            customerActivity.setCustomerId(customer.getId());
            customerActivity.setActivityType(
                mapToCustomerActivityType(leadActivity.getActivityType())
            );
            customerActivity.setActivityDate(leadActivity.getActivityDate());
            customerActivity.setSummary(leadActivity.getSummary());
            customerActivity.setOutcome(leadActivity.getOutcome());
            customerActivity.setNextAction(leadActivity.getNextAction());
            customerActivity.setNextActionDate(leadActivity.getNextActionDate());
            customerActivity.setPerformedBy(leadActivity.getPerformedBy());

            // Metadata: Ursprung markieren
            customerActivity.setMigratedFromLeadId(lead.getId());
            customerActivity.setMigratedFromLeadActivityId(leadActivity.getId());

            customerActivityRepository.persist(customerActivity);
        }
    }

    private ActivityType mapToCustomerActivityType(LeadActivityType leadType) {
        // Mapping-Logik (meist 1:1, außer lead-spezifische Types)
        return switch (leadType) {
            case QUALIFIED_CALL -> ActivityType.CALL;
            case MEETING -> ActivityType.MEETING;
            case DEMO -> ActivityType.DEMO;
            case SAMPLE_SENT -> ActivityType.SAMPLE_DELIVERY;
            case NOTE -> ActivityType.NOTE;
            // ... weitere Mappings
        };
    }

    private void checkForDuplicates(Lead lead) {
        // Email-Check
        if (lead.getContact() != null && lead.getContact().getEmail() != null) {
            Optional<Customer> emailMatch = customerRepository
                .findByContactEmail(lead.getContact().getEmailNormalized());

            if (emailMatch.isPresent()) {
                throw new DuplicateCustomerException(
                    "Customer with email already exists: " + emailMatch.get().getId()
                );
            }
        }

        // VAT ID Check (falls vorhanden)
        if (lead.getVatId() != null) {
            Optional<Customer> vatMatch = customerRepository
                .findByVatId(lead.getVatId());

            if (vatMatch.isPresent()) {
                throw new DuplicateCustomerException(
                    "Customer with VAT ID already exists: " + vatMatch.get().getId()
                );
            }
        }

        // Fuzzy Company + City (Warning, kein Hard-Block)
        List<Customer> fuzzyMatches = customerRepository
            .findFuzzyCandidates(lead.getCompanyName(), lead.getCity());

        if (!fuzzyMatches.isEmpty()) {
            log.warn("Potential duplicate customers found for lead {}: {}",
                    lead.getId(),
                    fuzzyMatches.stream().map(Customer::getId).toList());
            // In Sprint 2.1.6: Review-UI implementieren
        }
    }
}
```

---

## 🔍 Duplikaten-Check

### 1. Hard Duplicate (Blockiert Conversion)

| Check | Regel | Error Message |
|-------|-------|---------------|
| **Email Exact Match** | Normalisierte Email bereits bei existierendem Kunden | "Customer with email {email} already exists (ID: {id})" |
| **VAT ID Match** | VAT ID bereits bei existierendem Kunden | "Customer with VAT ID {vat} already exists (ID: {id})" |
| **Phone Exact Match** | Normalisierte Phone bereits bei existierendem Kunden | "Customer with phone {phone} already exists (ID: {id})" |

### 2. Soft Duplicate (Warning, kein Block)

| Check | Regel | Aktion |
|-------|-------|--------|
| **Company + City Fuzzy** | Ähnlicher Company-Name in gleicher Stadt | Log-Warning + Audit-Event "potential_duplicate_customer" |
| **Company + PostalCode** | Exakt gleicher Company-Name + PLZ | Log-Warning + Audit-Event |

**Soft Duplicate Handling (Sprint 2.1.6):**
- Conversion wird NICHT blockiert
- Audit-Log Event: `potential_duplicate_customer_created`
- Manager-Dashboard: "Review Duplicates" Widget
- Manuelle Review-UI: Merge/Unmerge/Mark-as-Different

---

## 🎨 Frontend UI Flow

### 1. Lead-Detail Page - Convert Button

**LeadDetailPage.tsx:**
```tsx
export function LeadDetailPage({ leadId }: Props) {
  const { lead } = useLead(leadId);
  const navigate = useNavigate();
  const [convertDialogOpen, setConvertDialogOpen] = useState(false);

  const canConvert = lead.status === 'QUALIFIED' && lead.stage === 2;

  const handleConvert = async () => {
    try {
      const result = await convertLeadToCustomer(leadId);

      // Success-Message
      showNotification({
        severity: 'success',
        message: 'Lead erfolgreich als Kunde angelegt'
      });

      // Navigation zu Customer-Detail
      navigate(`/customer-management/customers/${result.customer_id}`);

    } catch (error) {
      if (error.type === 'DUPLICATE_CUSTOMER') {
        // Duplicate-Error spezifisch behandeln
        showNotification({
          severity: 'error',
          message: `Kunde mit dieser Email existiert bereits (ID: ${error.existingCustomerId})`
        });
      } else {
        showNotification({
          severity: 'error',
          message: 'Fehler beim Konvertieren: ' + error.message
        });
      }
    }
  };

  return (
    <Box>
      {/* Lead-Details */}
      <LeadDetails lead={lead} />

      {/* Convert-Button (nur für QUALIFIED Leads) */}
      {canConvert && (
        <Box sx={{ mt: 3 }}>
          <Alert severity="info" sx={{ mb: 2 }}>
            Dieser Lead ist <strong>qualifiziert</strong> und bereit für die Konvertierung.
            Alle Daten werden automatisch in einen Kunden-Datensatz übernommen.
          </Alert>

          <Button
            variant="contained"
            color="success"
            size="large"
            startIcon={<CheckCircleIcon />}
            onClick={() => setConvertDialogOpen(true)}
          >
            Als Kunde anlegen
          </Button>
        </Box>
      )}

      {/* Confirmation Dialog */}
      <ConvertToCustomerDialog
        open={convertDialogOpen}
        lead={lead}
        onConfirm={handleConvert}
        onCancel={() => setConvertDialogOpen(false)}
      />
    </Box>
  );
}
```

### 2. Conversion Confirmation Dialog

**ConvertToCustomerDialog.tsx:**
```tsx
interface Props {
  open: boolean;
  lead: Lead;
  onConfirm: () => void;
  onCancel: () => void;
}

export function ConvertToCustomerDialog({ open, lead, onConfirm, onCancel }: Props) {
  return (
    <Dialog open={open} onClose={onCancel} maxWidth="md" fullWidth>
      <DialogTitle>Lead als Kunde anlegen</DialogTitle>
      <DialogContent>
        <Alert severity="success" sx={{ mb: 2 }}>
          Der Lead wird in einen <strong>aktiven Kunden</strong> konvertiert.
          Dieser Vorgang kann nicht rückgängig gemacht werden.
        </Alert>

        {/* Daten-Übersicht */}
        <Typography variant="h6" sx={{ mt: 2, mb: 1 }}>
          Folgende Daten werden übernommen:
        </Typography>

        <List dense>
          <ListItem>
            <ListItemIcon><BusinessIcon /></ListItemIcon>
            <ListItemText
              primary="Firmendaten"
              secondary={`${lead.companyName}, ${lead.city}`}
            />
          </ListItem>

          {lead.contact && (
            <ListItem>
              <ListItemIcon><PersonIcon /></ListItemIcon>
              <ListItemText
                primary="Kontaktdaten"
                secondary={`${lead.contact.firstName} ${lead.contact.lastName} (${lead.contact.email})`}
              />
            </ListItem>
          )}

          <ListItem>
            <ListItemIcon><TerritoryIcon /></ListItemIcon>
            <ListItemText
              primary="Territory"
              secondary={lead.territory}
            />
          </ListItem>

          {lead.estimatedVolume && (
            <ListItem>
              <ListItemIcon><EuroIcon /></ListItemIcon>
              <ListItemText
                primary="Geschätztes Volumen"
                secondary={formatCurrency(lead.estimatedVolume)}
              />
            </ListItem>
          )}

          <ListItem>
            <ListItemIcon><TimelineIcon /></ListItemIcon>
            <ListItemText
              primary="Activity-Historie"
              secondary={`${lead.activitiesCount} Aktivitäten werden übernommen`}
            />
          </ListItem>
        </List>

        {/* Duplikaten-Warnung (falls vorhanden) */}
        {lead.potentialDuplicates && lead.potentialDuplicates.length > 0 && (
          <Alert severity="warning" sx={{ mt: 2 }}>
            <Typography variant="body2" fontWeight="bold">
              Mögliche Duplikate gefunden:
            </Typography>
            {lead.potentialDuplicates.map((dup) => (
              <Typography key={dup.id} variant="body2">
                • {dup.companyName} ({dup.city}) - Ähnlichkeit: {dup.score}%
              </Typography>
            ))}
            <Typography variant="body2" sx={{ mt: 1 }}>
              Bitte nach Konvertierung manuell prüfen.
            </Typography>
          </Alert>
        )}
      </DialogContent>

      <DialogActions>
        <Button onClick={onCancel}>Abbrechen</Button>
        <Button
          onClick={onConfirm}
          variant="contained"
          color="success"
          startIcon={<CheckCircleIcon />}
        >
          Jetzt konvertieren
        </Button>
      </DialogActions>
    </Dialog>
  );
}
```

---

## 📊 Audit-Log Format

### Lead Converted Event

```json
{
  "event_type": "lead_converted_to_customer",
  "timestamp": "2025-10-15T14:30:00Z",
  "user_id": "partner-abc-123",
  "user_role": "partner",
  "lead_id": 12345,
  "customer_id": 67890,
  "data": {
    "lead_company_name": "Restaurant Musterküche",
    "lead_stage": 2,
    "lead_status": "QUALIFIED",
    "lead_registered_at": "2024-06-15T10:00:00Z",
    "customer_status": "ACTIVE",
    "activities_migrated": 12,
    "notes_migrated": true,
    "estimated_volume": 5000.00,
    "potential_duplicates_warned": 0,
    "conversion_duration_ms": 234
  }
}
```

### Activity Migration Event

```json
{
  "event_type": "lead_activities_migrated_to_customer",
  "timestamp": "2025-10-15T14:30:01Z",
  "lead_id": 12345,
  "customer_id": 67890,
  "data": {
    "total_activities": 12,
    "migrated_activity_ids": [101, 102, 103, ...],
    "activity_types": {
      "QUALIFIED_CALL": 3,
      "MEETING": 2,
      "DEMO": 1,
      "SAMPLE_SENT": 1,
      "NOTE": 5
    }
  }
}
```

---

## 🔄 Rollback-Strategie

### Manuelle Rollback (Admin-only)

**Use-Case:** Versehentliche Konvertierung, Duplikat übersehen

**Endpoint:**
```java
@DELETE
@Path("/customers/{customerId}/revert-to-lead")
@RolesAllowed({"admin"})
@Transactional
public Response revertToLead(@PathParam("customerId") Long customerId) {
    Customer customer = customerRepository.findById(customerId);

    // Validierung: Nur frisch konvertierte Kunden (< 24h)
    if (customer.getConvertedAt().isBefore(Instant.now().minus(24, ChronoUnit.HOURS))) {
        throw new BadRequestException("Cannot revert customer older than 24 hours");
    }

    // Validierung: Kunde darf keine Orders haben
    if (orderRepository.existsByCustomerId(customerId)) {
        throw new BadRequestException("Cannot revert customer with existing orders");
    }

    // Lead-Status zurücksetzen
    Lead lead = leadRepository.findById(customer.getOriginalLeadId());
    lead.setStatus(LeadStatus.QUALIFIED);
    lead.setConvertedAt(null);
    lead.setConvertedToCustomerId(null);

    // Customer löschen
    customerRepository.delete(customer);

    // Audit-Log
    auditService.log("customer_reverted_to_lead", customerId, Map.of(
        "lead_id", lead.getId()
    ));

    return Response.ok(lead).build();
}
```

**UI-Button (Admin-Dashboard):**
```tsx
<Button
  variant="outlined"
  color="error"
  onClick={() => revertCustomerToLead(customerId)}
  disabled={!canRevert(customer)}
>
  Zurück zu Lead konvertieren
</Button>
```

---

## 🧪 Test-Szenarien

### 1. Happy Path - Successful Conversion

```java
@Test
void shouldConvertQualifiedLeadToCustomer() {
    // Given: Qualified Lead mit vollständigen Daten
    Lead lead = createQualifiedLead();
    createActivities(lead, 5);

    // When: Convert durchführen
    Customer customer = conversionService.convertToCustomer(lead);

    // Then: Validierungen
    assertNotNull(customer.getId());
    assertEquals(lead.getCompanyName(), customer.getCompanyName());
    assertEquals(lead.getContact().getEmail(), customer.getContacts().get(0).getEmail());
    assertEquals(lead.getId(), customer.getOriginalLeadId());
    assertEquals(CustomerStatus.ACTIVE, customer.getCustomerStatus());

    // Activities migriert?
    assertEquals(5, customerActivityRepository.countByCustomerId(customer.getId()));
}
```

### 2. Duplicate Detection - Email Exists

```java
@Test
void shouldRejectConversionWhenEmailDuplicateExists() {
    // Given: Existierender Kunde mit Email
    createCustomer("existing@example.com");

    // Given: Lead mit gleicher Email
    Lead lead = createQualifiedLead("existing@example.com");

    // When/Then: Conversion blockiert
    assertThrows(DuplicateCustomerException.class, () -> {
        conversionService.convertToCustomer(lead);
    });
}
```

### 3. Status Validation

```java
@Test
void shouldRejectConversionForNonQualifiedLead() {
    // Given: Lead mit Status NEW
    Lead lead = createLead(LeadStatus.NEW);

    // When/Then: Conversion blockiert
    assertThrows(BadRequestException.class, () -> {
        conversionResource.convertToCustomer(lead.getId());
    });
}
```

---

## 📚 Business-Metriken

### Conversion-Rate Dashboard

```sql
-- Lead → Customer Conversion-Rate (last 90 days)
SELECT
  DATE_TRUNC('week', converted_at) as week,
  COUNT(*) as total_conversions,
  AVG(EXTRACT(EPOCH FROM (converted_at - registered_at)) / 86400) as avg_days_to_convert,
  AVG(estimated_volume) as avg_customer_value
FROM customers
WHERE converted_from = 'lead'
  AND converted_at >= NOW() - INTERVAL '90 days'
GROUP BY week
ORDER BY week DESC;
```

### Partner Performance

```sql
-- Top Converter (Partner mit höchster Conversion-Rate)
SELECT
  owner_user_id,
  COUNT(DISTINCT original_lead_id) as converted_leads,
  SUM(estimated_volume) as total_value,
  AVG(estimated_volume) as avg_deal_size
FROM customers
WHERE converted_from = 'lead'
  AND converted_at >= NOW() - INTERVAL '90 days'
GROUP BY owner_user_id
ORDER BY converted_leads DESC
LIMIT 10;
```

---

## 📚 Referenzen

- **TRIGGER_SPRINT_2_1_6.md:** Zeile 71-80 (Lead → Kunde Convert Flow)
- **Modul 02 Neukundengewinnung:** Lead-Management Backend
- **Modul 03 Kundenmanagement:** Customer-Management Backend
- **Lead.java:** `backend/src/main/java/de/freshplan/modules/leads/domain/Lead.java`
- **Customer.java:** `backend/src/main/java/de/freshplan/modules/customers/domain/Customer.java`

---

**Dokument-Owner:** Jörg Streeck + Claude Code
**Letzte Änderung:** 2025-10-02
**Version:** 1.0 (Planned for Sprint 2.1.6)
