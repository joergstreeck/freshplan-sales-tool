# DSGVO Technical Specification

**Sprint:** 2.1.8
**Stand:** 2025-12-04
**Status:** IMPLEMENTATION READY

---

## 1. Übersicht

Implementierung der gesetzlich vorgeschriebenen DSGVO-Features:
- **Art. 15** - Auskunftsrecht (PDF-Export)
- **Art. 17** - Löschrecht (Soft-Delete + PII-Anonymisierung)
- **Art. 7 Abs. 3** - Einwilligungswiderruf (Kontakt-Sperre)

### Rechtsrahmen

| Artikel | Frist | Bußgeld (max) | Umsetzung |
|---------|-------|---------------|-----------|
| Art. 15 | 1 Monat | 20 Mio EUR | PDF-Export |
| Art. 17 | unverzüglich | 20 Mio EUR | Soft-Delete + Anonymisierung |
| Art. 7.3 | sofort | 20 Mio EUR | Kontakt-Sperre |

---

## 2. Datenbankschema (Migration)

### Neue Felder in `leads` Tabelle

```sql
-- Migration: V{NEXT}_add_gdpr_fields.sql
ALTER TABLE leads ADD COLUMN IF NOT EXISTS consent_revoked_at TIMESTAMP;
ALTER TABLE leads ADD COLUMN IF NOT EXISTS consent_revoked_by UUID REFERENCES users(id);
ALTER TABLE leads ADD COLUMN IF NOT EXISTS contact_blocked BOOLEAN DEFAULT FALSE;
ALTER TABLE leads ADD COLUMN IF NOT EXISTS gdpr_deleted BOOLEAN DEFAULT FALSE;
ALTER TABLE leads ADD COLUMN IF NOT EXISTS gdpr_deleted_at TIMESTAMP;
ALTER TABLE leads ADD COLUMN IF NOT EXISTS gdpr_deleted_by UUID REFERENCES users(id);
ALTER TABLE leads ADD COLUMN IF NOT EXISTS gdpr_deletion_reason VARCHAR(500);
```

### Neue Tabelle: `gdpr_data_requests`

```sql
CREATE TABLE gdpr_data_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_type VARCHAR(50) NOT NULL, -- 'LEAD', 'CUSTOMER'
    entity_id UUID NOT NULL,
    requested_by UUID NOT NULL REFERENCES users(id),
    requested_at TIMESTAMP NOT NULL DEFAULT NOW(),
    pdf_generated BOOLEAN DEFAULT FALSE,
    pdf_generated_at TIMESTAMP,
    pdf_file_path VARCHAR(500),
    CONSTRAINT fk_requested_by FOREIGN KEY (requested_by) REFERENCES users(id)
);

CREATE INDEX idx_gdpr_data_requests_entity ON gdpr_data_requests(entity_type, entity_id);
CREATE INDEX idx_gdpr_data_requests_requested_at ON gdpr_data_requests(requested_at);
```

### Neue Tabelle: `gdpr_deletion_logs`

```sql
CREATE TABLE gdpr_deletion_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_type VARCHAR(50) NOT NULL, -- 'LEAD', 'CUSTOMER'
    entity_id UUID NOT NULL,
    deleted_by UUID NOT NULL REFERENCES users(id),
    deleted_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deletion_reason VARCHAR(500) NOT NULL,
    original_data_hash VARCHAR(64), -- SHA-256 für Audit-Nachweis
    CONSTRAINT fk_deleted_by FOREIGN KEY (deleted_by) REFERENCES users(id)
);

CREATE INDEX idx_gdpr_deletion_logs_entity ON gdpr_deletion_logs(entity_type, entity_id);
CREATE INDEX idx_gdpr_deletion_logs_deleted_at ON gdpr_deletion_logs(deleted_at);
```

---

## 3. Backend Services

### 3.1 GdprService.java

```java
@ApplicationScoped
public class GdprService {

    @Inject LeadRepository leadRepository;
    @Inject GdprDataRequestRepository dataRequestRepository;
    @Inject GdprDeletionLogRepository deletionLogRepository;
    @Inject PdfGeneratorService pdfService;
    @Inject AuditLogService auditService;

    // Art. 15 - Auskunftsrecht
    @Transactional
    public byte[] generateDataExport(UUID entityId, EntityType type, UUID requestedBy) {
        // 1. Log the request
        var request = new GdprDataRequest();
        request.setEntityType(type);
        request.setEntityId(entityId);
        request.setRequestedBy(requestedBy);
        dataRequestRepository.persist(request);

        // 2. Generate PDF
        byte[] pdf = pdfService.generateGdprExport(entityId, type);

        // 3. Update request
        request.setPdfGenerated(true);
        request.setPdfGeneratedAt(Instant.now());

        // 4. Audit log
        auditService.log(AuditAction.GDPR_DATA_EXPORT, entityId, requestedBy);

        return pdf;
    }

    // Art. 17 - Löschrecht (Soft-Delete + PII-Anonymisierung)
    @Transactional
    public void gdprDelete(UUID entityId, EntityType type, UUID deletedBy, String reason) {
        Lead lead = leadRepository.findById(entityId);

        // 1. Prüfe Abhängigkeiten
        if (lead.getOpportunities() != null && !lead.getOpportunities().isEmpty()) {
            throw new GdprDeletionBlockedException(
                "Lead hat offene Opportunities. Bitte zuerst abschließen oder löschen."
            );
        }

        // 2. Hash für Audit-Nachweis
        String dataHash = HashUtils.sha256(lead.getCompanyName() + lead.getEmail());

        // 3. PII anonymisieren (NICHT löschen - Audit-Trail behalten)
        lead.setCompanyName("DSGVO-GELÖSCHT-" + lead.getId().toString().substring(0, 8));
        lead.setEmail(null);
        lead.setPhone(null);
        lead.setContactPerson(null);
        lead.setStreet(null);
        lead.setCity(null);
        lead.setPostalCode(null);
        lead.setNotes(null);
        lead.setGdprDeleted(true);
        lead.setGdprDeletedAt(Instant.now());
        lead.setGdprDeletedBy(deletedBy);
        lead.setGdprDeletionReason(reason);

        // 4. Deletion Log
        var log = new GdprDeletionLog();
        log.setEntityType(type);
        log.setEntityId(entityId);
        log.setDeletedBy(deletedBy);
        log.setDeletionReason(reason);
        log.setOriginalDataHash(dataHash);
        deletionLogRepository.persist(log);

        // 5. Audit
        auditService.log(AuditAction.GDPR_DELETION, entityId, deletedBy);
    }

    // Art. 7.3 - Einwilligungswiderruf
    @Transactional
    public void revokeConsent(UUID leadId, UUID revokedBy) {
        Lead lead = leadRepository.findById(leadId);

        lead.setConsentRevokedAt(Instant.now());
        lead.setConsentRevokedBy(revokedBy);
        lead.setContactBlocked(true);

        auditService.log(AuditAction.GDPR_CONSENT_REVOKED, leadId, revokedBy);
    }
}
```

### 3.2 PdfGeneratorService.java (Apache PDFBox)

```java
@ApplicationScoped
public class PdfGeneratorService {

    public byte[] generateGdprExport(UUID entityId, EntityType type) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                // Header
                content.setFont(PDType1Font.HELVETICA_BOLD, 18);
                content.beginText();
                content.newLineAtOffset(50, 750);
                content.showText("Datenauskunft gemäß Art. 15 DSGVO");
                content.endText();

                // Sections: Stammdaten, Aktivitäten, Consent, etc.
                addSection(content, "1. Stammdaten", getStammdaten(entityId, type));
                addSection(content, "2. Aktivitäten", getAktivitaeten(entityId));
                addSection(content, "3. Einwilligungsstatus", getConsentStatus(entityId));

                // Footer
                addFooter(content, "FreshFoodz GmbH | Erstellt am: " + LocalDate.now());
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        }
    }
}
```

### 3.3 GdprResource.java (REST Endpoints)

```java
@Path("/api/gdpr")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public class GdprResource {

    @Inject GdprService gdprService;
    @Inject SecurityIdentity identity;

    // Art. 15 - Datenexport anfordern
    @POST
    @Path("/leads/{id}/data-request")
    @RolesAllowed({"ADMIN", "MANAGER"})
    public Response requestDataExport(@PathParam("id") UUID leadId) {
        byte[] pdf = gdprService.generateDataExport(
            leadId,
            EntityType.LEAD,
            getCurrentUserId()
        );

        return Response.ok(pdf)
            .header("Content-Disposition", "attachment; filename=dsgvo-auskunft-" + leadId + ".pdf")
            .header("Content-Type", "application/pdf")
            .build();
    }

    // Art. 17 - DSGVO-Löschung
    @DELETE
    @Path("/leads/{id}")
    @RolesAllowed({"ADMIN", "MANAGER"})
    public Response gdprDelete(
            @PathParam("id") UUID leadId,
            @QueryParam("reason") @NotBlank String reason) {

        gdprService.gdprDelete(leadId, EntityType.LEAD, getCurrentUserId(), reason);
        return Response.noContent().build();
    }

    // Art. 7.3 - Einwilligung widerrufen
    @POST
    @Path("/leads/{id}/revoke-consent")
    public Response revokeConsent(@PathParam("id") UUID leadId) {
        gdprService.revokeConsent(leadId, getCurrentUserId());
        return Response.noContent().build();
    }
}
```

---

## 4. Frontend Components

### 4.1 GdprActionsMenu.tsx

```typescript
// Dropdown-Menü für DSGVO-Aktionen in LeadDetailPage
interface GdprActionsMenuProps {
  leadId: string;
  consentGivenAt?: string;
  consentRevokedAt?: string;
  gdprDeleted?: boolean;
}

export function GdprActionsMenu({ leadId, consentGivenAt, consentRevokedAt, gdprDeleted }: GdprActionsMenuProps) {
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const { hasRole } = useAuth();
  const isManagerOrAdmin = hasRole('MANAGER') || hasRole('ADMIN');

  return (
    <>
      <IconButton onClick={(e) => setAnchorEl(e.currentTarget)}>
        <MoreVertIcon />
      </IconButton>
      <Menu anchorEl={anchorEl} open={Boolean(anchorEl)} onClose={() => setAnchorEl(null)}>
        {/* Art. 15 - Datenexport */}
        {isManagerOrAdmin && (
          <MenuItem onClick={() => handleDataExport(leadId)}>
            <ListItemIcon><DownloadIcon /></ListItemIcon>
            <ListItemText>Datenauskunft (Art. 15)</ListItemText>
          </MenuItem>
        )}

        {/* Art. 7.3 - Einwilligung widerrufen */}
        {consentGivenAt && !consentRevokedAt && (
          <MenuItem onClick={() => handleRevokeConsent(leadId)}>
            <ListItemIcon><BlockIcon /></ListItemIcon>
            <ListItemText>Einwilligung widerrufen</ListItemText>
          </MenuItem>
        )}

        {/* Art. 17 - DSGVO-Löschung */}
        {isManagerOrAdmin && !gdprDeleted && (
          <MenuItem onClick={() => setShowDeleteDialog(true)}>
            <ListItemIcon><DeleteForeverIcon color="error" /></ListItemIcon>
            <ListItemText sx={{ color: 'error.main' }}>DSGVO-Löschung (Art. 17)</ListItemText>
          </MenuItem>
        )}
      </Menu>
    </>
  );
}
```

### 4.2 GdprDeleteDialog.tsx

```typescript
// Confirmation Dialog für DSGVO-Löschung
interface GdprDeleteDialogProps {
  open: boolean;
  onClose: () => void;
  onConfirm: (reason: string) => void;
  entityName: string;
}

const DELETION_REASONS = [
  { value: 'ART_17_REQUEST', label: 'Betroffener hat Löschung beantragt (Art. 17)' },
  { value: 'DATA_NOT_REQUIRED', label: 'Daten nicht mehr erforderlich (Art. 17 Abs. 1a)' },
  { value: 'CONSENT_WITHDRAWN', label: 'Einwilligung widerrufen (Art. 17 Abs. 1b)' },
  { value: 'OTHER', label: 'Anderer Grund' },
];

export function GdprDeleteDialog({ open, onClose, onConfirm, entityName }: GdprDeleteDialogProps) {
  const [reason, setReason] = useState('');
  const [customReason, setCustomReason] = useState('');

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle sx={{ color: 'error.main' }}>
        ⚠️ DSGVO-Löschung (Art. 17)
      </DialogTitle>
      <DialogContent>
        <Alert severity="warning" sx={{ mb: 2 }}>
          Diese Aktion ist <strong>unwiderruflich</strong>. Alle personenbezogenen Daten
          werden anonymisiert. Der Audit-Trail bleibt für Compliance-Zwecke erhalten.
        </Alert>

        <Typography variant="body1" gutterBottom>
          Lead: <strong>{entityName}</strong>
        </Typography>

        <FormControl fullWidth sx={{ mt: 2 }}>
          <InputLabel>Löschgrund *</InputLabel>
          <Select value={reason} onChange={(e) => setReason(e.target.value)}>
            {DELETION_REASONS.map((r) => (
              <MenuItem key={r.value} value={r.value}>{r.label}</MenuItem>
            ))}
          </Select>
        </FormControl>

        {reason === 'OTHER' && (
          <TextField
            fullWidth
            multiline
            rows={2}
            label="Begründung (min. 10 Zeichen)"
            value={customReason}
            onChange={(e) => setCustomReason(e.target.value)}
            sx={{ mt: 2 }}
          />
        )}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Abbrechen</Button>
        <Button
          color="error"
          variant="contained"
          onClick={() => onConfirm(reason === 'OTHER' ? customReason : reason)}
          disabled={!reason || (reason === 'OTHER' && customReason.length < 10)}
        >
          Unwiderruflich löschen
        </Button>
      </DialogActions>
    </Dialog>
  );
}
```

### 4.3 ContactBlockedBadge.tsx

```typescript
// Badge für gesperrte Kontakte
export function ContactBlockedBadge({ lead }: { lead: Lead }) {
  if (!lead.contactBlocked) return null;

  return (
    <Tooltip title={`Einwilligung widerrufen am ${formatDate(lead.consentRevokedAt)}. Keine Kontaktaufnahme erlaubt (DSGVO Art. 7 Abs. 3)`}>
      <Chip
        icon={<BlockIcon />}
        label="Kontakt gesperrt"
        color="error"
        size="small"
        variant="outlined"
      />
    </Tooltip>
  );
}
```

---

## 5. RBAC-Matrix

| Aktion | SALES | MANAGER | ADMIN |
|--------|-------|---------|-------|
| Datenexport anfordern (Art. 15) | ❌ | ✅ | ✅ |
| DSGVO-Löschung (Art. 17) | ❌ | ✅ | ✅ |
| Einwilligung widerrufen (Art. 7.3) | ✅ | ✅ | ✅ |
| Gesperrte Kontakte sehen | ✅ | ✅ | ✅ |
| DSGVO-Logs einsehen | ❌ | ✅ | ✅ |

---

## 6. Testplan

### Unit Tests

```java
@QuarkusTest
class GdprServiceTest {

    @Test
    void shouldGenerateDataExport() { /* ... */ }

    @Test
    void shouldSoftDeleteWithPiiAnonymization() { /* ... */ }

    @Test
    void shouldBlockDeletionWithOpenOpportunities() { /* ... */ }

    @Test
    void shouldRevokeConsentAndBlockContact() { /* ... */ }

    @Test
    void shouldCreateAuditLogForAllActions() { /* ... */ }
}
```

### Integration Tests

```java
@QuarkusTest
class GdprResourceIT {

    @Test
    void art15_shouldGeneratePdfExport() { /* ... */ }

    @Test
    void art17_shouldAnonymizePiiData() { /* ... */ }

    @Test
    void art7_shouldBlockContactAfterRevocation() { /* ... */ }

    @Test
    void shouldReturn403ForSalesRole() { /* ... */ }
}
```

---

## 7. Admin-UI Route

Neue Route: `/admin/dsgvo`

```typescript
// AdminDsgvoPage.tsx
export function AdminDsgvoPage() {
  return (
    <MainLayoutV2>
      <Typography variant="h4">DSGVO-Verwaltung</Typography>

      <Tabs>
        <Tab label="Datenauskunfts-Anfragen" />
        <Tab label="Löschprotokoll" />
        <Tab label="Gesperrte Kontakte" />
      </Tabs>

      {/* Tab 1: Offene Art. 15 Anfragen */}
      <DataRequestsTable />

      {/* Tab 2: Art. 17 Löschungen */}
      <DeletionLogsTable />

      {/* Tab 3: Art. 7.3 Sperrliste */}
      <BlockedContactsTable />
    </MainLayoutV2>
  );
}
```

---

## 8. Checkliste

- [ ] Migration erstellen (GDPR-Felder + Tabellen)
- [ ] GdprService implementieren
- [ ] PdfGeneratorService implementieren (Apache PDFBox)
- [ ] GdprResource implementieren
- [ ] Frontend: GdprActionsMenu
- [ ] Frontend: GdprDeleteDialog
- [ ] Frontend: ContactBlockedBadge
- [ ] Frontend: AdminDsgvoPage
- [ ] Unit Tests (≥80% Coverage)
- [ ] Integration Tests
- [ ] Route `/admin/dsgvo` hinzufügen
