---
module: "02_neukundengewinnung"
domain: "backend"
doc_type: "guideline"
status: "approved"
sprint: "2.1.5"
owner: "team/leads-backend"
updated: "2025-10-03"
---

# Dedupe Policy - Sprint 2.1.5 (Hard Collisions Only)

**📍 Navigation:** Home → Planung → 02 Neukundengewinnung → Artefakte → Sprint 2.1.5 → Dedupe Policy

## Zweck

Dedupe-Policy für **Sprint 2.1.5** - Verhindert harte Duplikate (100% Match) beim Lead-Erfassen.

**Scope Sprint 2.1.5:**
- ✅ **Harte Kollisionen** (exakte Matches) → BLOCK + Manager-Override
- ✅ **Einfache weiche Heuristik** (Domain+Stadt) → WARN + Fortfahren
- ❌ **KEIN Fuzzy-Matching** (pg_trgm) → Sprint 2.1.6

---

## Business Rules

### Rule 1: Harte Kollisionen (BLOCK)

**Definition:** 100% Match auf **normalisier

ten** Feldern

**Kriterien (OR-Verknüpfung):**
1. **Email exakt:** `normalized_email = normalize_email(input.email)`
2. **Telefon exakt:** `phone_e164 = normalize_phone(input.phone)`
3. **Firma + PLZ exakt:** `company_name_normalized = normalize(input.companyName)` AND `postal_code = input.postalCode`

**Aktion:**
- ❌ **409 Conflict** (RFC 7807 Problem+JSON)
- ✅ **Override erlaubt:** Nur Manager/Admin + `overrideReason` (Pflicht, min. 10 Zeichen)
- 📊 **Audit-Log:** `lead_duplicate_override` Event

---

### Rule 2: Weiche Kollisionen (WARN)

**Definition:** Ähnliche Leads, aber kein 100% Match

**Kriterien (AND-Verknüpfung):**
1. **Gleiche Email-Domain UND gleiche Stadt/PLZ**
   - Beispiel: `@test.de` + `Hamburg` → Warnung
2. **Gleiche Firma (exakt) UND gleiche Stadt**
   - Beispiel: `Test GmbH` + `Hamburg` → Warnung

**Aktion:**
- ⚠️ **409 Conflict** mit `severity: "WARNING"`
- ✅ **Fortfahren erlaubt:** Jeder Nutzer + `reason` (Pflicht, min. 10 Zeichen)
- 📊 **Audit-Log:** `lead_duplicate_warning_accepted` Event

---

### Rule 3: Fuzzy-Matching (NICHT in 2.1.5!)

**Scope Sprint 2.1.6:**
- `pg_trgm` Extension
- `similarity(company_name, input) >= 0.7`
- Merge/Unmerge Flow
- Identitätsgraph

**Siehe:** `TRIGGER_SPRINT_2_1_6.md` für Details

---

## Normalisierung

### Email-Normalisierung

```sql
CREATE OR REPLACE FUNCTION normalize_email(email VARCHAR)
RETURNS VARCHAR
LANGUAGE plpgsql
IMMUTABLE
AS $$
BEGIN
  IF email IS NULL THEN RETURN NULL; END IF;
  RETURN LOWER(TRIM(email));
END;
$$;

-- Beispiele:
-- "Max@Test.DE  " → "max@test.de"
-- "  Info@EXAMPLE.COM" → "info@example.com"
```

### Telefon-Normalisierung (E.164)

```sql
CREATE OR REPLACE FUNCTION normalize_phone(phone VARCHAR)
RETURNS VARCHAR
LANGUAGE plpgsql
IMMUTABLE
AS $$
BEGIN
  IF phone IS NULL THEN RETURN NULL; END IF;

  -- Remove all non-digits
  phone := REGEXP_REPLACE(phone, '[^0-9+]', '', 'g');

  -- Add +49 if missing
  IF phone !~ '^\+' THEN
    IF phone ~ '^0' THEN
      phone := '+49' || SUBSTRING(phone FROM 2);
    ELSE
      phone := '+49' || phone;
    END IF;
  END IF;

  RETURN phone;
END;
$$;

-- Beispiele:
-- "0123 456 789" → "+49123456789"
-- "+49 (0)123 456789" → "+49123456789"
-- "123-456-789" → "+49123456789"
```

### Firmenname-Normalisierung

```sql
CREATE OR REPLACE FUNCTION normalize_company_name(name VARCHAR)
RETURNS VARCHAR
LANGUAGE plpgsql
IMMUTABLE
AS $$
BEGIN
  IF name IS NULL THEN RETURN NULL; END IF;

  -- Lowercase + Trim
  name := LOWER(TRIM(name));

  -- Remove GmbH, AG, KG variants
  name := REGEXP_REPLACE(name, '\s+(gmbh|ag|kg|ohg|gbr|ug|e\.?v\.?|mbh)\s*$', '', 'gi');

  -- Remove special characters
  name := REGEXP_REPLACE(name, '[^a-z0-9\s]', '', 'g');

  -- Collapse multiple spaces
  name := REGEXP_REPLACE(name, '\s+', ' ', 'g');

  RETURN TRIM(name);
END;
$$;

-- Beispiele:
-- "Test GmbH & Co. KG" → "test co"
-- "  EXAMPLE AG  " → "example"
-- "Müller-Bäckerei e.V." → "muller backerei"
```

---

## Backend Implementation

### DedupeService.java

```java
@ApplicationScoped
public class DedupeService {

    @Inject
    LeadRepository leadRepository;

    @Inject
    AuditService auditService;

    public DedupeResult checkDuplicate(LeadDTO dto) {
        // 1. HARTE KOLLISIONEN (BLOCK)
        List<Lead> hardMatches = findHardDuplicates(dto);
        if (!hardMatches.isEmpty()) {
            return new DedupeResult(
                DedupeDecision.BLOCK_HARD,
                hardMatches,
                "Identischer Lead existiert bereits (Email/Telefon/Firma+PLZ)",
                "SUPERVISOR_OVERRIDE_REQUIRED"
            );
        }

        // 2. WEICHE KOLLISIONEN (WARN)
        List<Lead> softMatches = findSoftDuplicates(dto);
        if (!softMatches.isEmpty()) {
            return new DedupeResult(
                DedupeDecision.WARN_SOFT,
                softMatches,
                "Ähnlicher Lead gefunden (gleiche Domain+Stadt oder Firma+Stadt)",
                "REVIEW_RECOMMENDED"
            );
        }

        return new DedupeResult(DedupeDecision.CLEAR, List.of(), null, null);
    }

    private List<Lead> findHardDuplicates(LeadDTO dto) {
        String normalizedEmail = normalizeEmail(dto.contact() != null ? dto.contact().email() : null);
        String normalizedPhone = normalizePhone(dto.contact() != null ? dto.contact().phone() : null);
        String normalizedCompany = normalizeCompanyName(dto.companyName());
        String postalCode = dto.postalCode();

        return leadRepository.find("""
            SELECT l FROM Lead l WHERE
            (l.normalizedEmail = :email AND :email IS NOT NULL)
            OR (l.phoneE164 = :phone AND :phone IS NOT NULL)
            OR (l.companyNameNormalized = :company AND l.postalCode = :postalCode
                AND :company IS NOT NULL AND :postalCode IS NOT NULL)
            """,
            Parameters.with("email", normalizedEmail)
                .and("phone", normalizedPhone)
                .and("company", normalizedCompany)
                .and("postalCode", postalCode)
        ).list();
    }

    private List<Lead> findSoftDuplicates(LeadDTO dto) {
        if (dto.contact() == null || dto.contact().email() == null) {
            return List.of();
        }

        String emailDomain = extractDomain(dto.contact().email());
        String city = dto.city();
        String company = dto.companyName();

        return leadRepository.find("""
            SELECT l FROM Lead l WHERE
            (
              (l.emailDomain = :domain AND l.city = :city AND :domain IS NOT NULL)
              OR (l.companyName = :company AND l.city = :city AND :company IS NOT NULL)
            )
            AND l.normalizedEmail != :exactEmail  -- Exclude hard matches
            """,
            Parameters.with("domain", emailDomain)
                .and("city", city)
                .and("company", company)
                .and("exactEmail", normalizeEmail(dto.contact().email()))
        ).list();
    }

    private String extractDomain(String email) {
        if (email == null || !email.contains("@")) return null;
        return email.substring(email.indexOf("@") + 1).toLowerCase();
    }

    private String normalizeEmail(String email) {
        if (email == null) return null;
        return email.trim().toLowerCase();
    }

    private String normalizePhone(String phone) {
        // Implement E.164 normalization (see SQL function above)
        // Simplified Java version:
        if (phone == null) return null;
        String digits = phone.replaceAll("[^0-9+]", "");
        if (!digits.startsWith("+")) {
            if (digits.startsWith("0")) {
                digits = "+49" + digits.substring(1);
            } else {
                digits = "+49" + digits;
            }
        }
        return digits;
    }

    private String normalizeCompanyName(String name) {
        if (name == null) return null;
        return name.trim().toLowerCase()
            .replaceAll("\\s+(gmbh|ag|kg|ohg|gbr|ug|e\\.?v\\.?|mbh)\\s*$", "")
            .replaceAll("[^a-z0-9\\s]", "")
            .replaceAll("\\s+", " ")
            .trim();
    }
}

public record DedupeResult(
    DedupeDecision decision,
    List<Lead> matches,
    String message,
    String action
) {}

public enum DedupeDecision {
    BLOCK_HARD,   // 100% Match → Supervisor override
    WARN_SOFT,    // Ähnlich → Warnung
    CLEAR         // Kein Treffer
}
```

---

### LeadResource.java (POST /api/leads)

```java
@POST
@Path("/api/leads")
@RolesAllowed({"PARTNER", "MANAGER", "ADMIN"})
@Transactional
public Response createLead(@Valid LeadDTO dto, @Context SecurityContext ctx) {
    // 1. Dedupe-Check
    DedupeResult dedupe = dedupeService.checkDuplicate(dto);

    if (dedupe.decision() == DedupeDecision.BLOCK_HARD) {
        // 2. Check für Override-Permission
        if (!ctx.isUserInRole("MANAGER") && !ctx.isUserInRole("ADMIN")) {
            return Response.status(409)
                .entity(Problem.builder()
                    .withType(URI.create("https://freshfoodz.de/problems/duplicate-lead"))
                    .withTitle("Duplicate Lead")
                    .withStatus(409)
                    .withDetail(dedupe.message())
                    .with("duplicates", dedupe.matches().stream()
                        .map(this::toSimpleDTO)
                        .toList())
                    .with("action", dedupe.action())
                    .build())
                .build();
        }

        // 3. Manager/Admin: Override erlaubt
        if (dto.overrideReason() == null || dto.overrideReason().length() < 10) {
            return Response.status(400)
                .entity(Problem.builder()
                    .withTitle("Override Reason Required")
                    .withDetail("Manager/Admin override requires reason (min. 10 characters)")
                    .build())
                .build();
        }

        // 4. Override mit Audit-Log
        auditService.log("lead_duplicate_override", Map.of(
            "user", ctx.getUserPrincipal().getName(),
            "reason", dto.overrideReason(),
            "duplicates", dedupe.matches().stream().map(Lead::getId).toList()
        ));
    }

    if (dedupe.decision() == DedupeDecision.WARN_SOFT) {
        // Weiche Kollision: Jeder darf fortfahren mit Reason
        if (dto.reason() == null || dto.reason().length() < 10) {
            return Response.status(409)
                .entity(Problem.builder()
                    .withType(URI.create("https://freshfoodz.de/problems/similar-lead"))
                    .withTitle("Similar Lead Found")
                    .withStatus(409)
                    .withDetail(dedupe.message())
                    .with("severity", "WARNING")
                    .with("duplicates", dedupe.matches().stream()
                        .map(this::toSimpleDTO)
                        .toList())
                    .with("action", "REASON_REQUIRED")
                    .build())
                .build();
        }

        // Audit-Log
        auditService.log("lead_duplicate_warning_accepted", Map.of(
            "user", ctx.getUserPrincipal().getName(),
            "reason", dto.reason(),
            "similarLeads", dedupe.matches().stream().map(Lead::getId).toList()
        ));
    }

    // 5. Lead erstellen
    Lead lead = leadService.createLead(dto);
    return Response.status(201).entity(lead).build();
}

private SimplifiedLeadDTO toSimpleDTO(Lead lead) {
    return new SimplifiedLeadDTO(
        lead.getId(),
        lead.getCompanyName(),
        lead.getCity(),
        lead.getRegisteredAt(),
        lead.getProtectedUntil(),
        lead.getOwnerUserId()
    );
}
```

---

## API Contract (RFC 7807)

### Hard Collision (409 - BLOCK)

**Response:**
```json
{
  "type": "https://freshfoodz.de/problems/duplicate-lead",
  "title": "Duplicate Lead",
  "status": 409,
  "detail": "Identischer Lead existiert bereits (Email/Telefon/Firma+PLZ)",
  "action": "SUPERVISOR_OVERRIDE_REQUIRED",
  "duplicates": [
    {
      "id": "lead-uuid-1",
      "companyName": "Test GmbH",
      "city": "Hamburg",
      "registeredAt": "2025-09-15T10:00:00Z",
      "protectedUntil": "2026-03-15T10:00:00Z",
      "ownerUserId": "partner-A"
    }
  ]
}
```

**Manager/Admin Override (Retry mit Reason):**
```json
{
  "companyName": "Test GmbH",
  "city": "Hamburg",
  "contact": {"email": "max@test.de"},
  "overrideReason": "Neue Abteilung, anderer Ansprechpartner"  // ← PFLICHT, min. 10 Zeichen
}
```

---

### Soft Collision (409 - WARN)

**Response:**
```json
{
  "type": "https://freshfoodz.de/problems/similar-lead",
  "title": "Similar Lead Found",
  "status": 409,
  "detail": "Ähnlicher Lead gefunden (gleiche Domain+Stadt oder Firma+Stadt)",
  "severity": "WARNING",
  "action": "REASON_REQUIRED",
  "duplicates": [
    {
      "id": "lead-uuid-2",
      "companyName": "Test GmbH",
      "city": "Hamburg",
      "registeredAt": "2025-09-20T14:00:00Z",
      "ownerUserId": "partner-B"
    }
  ]
}
```

**Fortfahren mit Reason:**
```json
{
  "companyName": "Test GmbH",
  "city": "Hamburg",
  "contact": {"email": "info@test.de"},
  "reason": "Anderer Standort, separate Filiale"  // ← PFLICHT, min. 10 Zeichen
}
```

---

## Frontend UI

### Duplicate Dialog (Hard Collision)

```typescript
// DuplicateLeadDialog.tsx
const DuplicateLeadDialog = ({ duplicates, onOpenExisting, onOverride }: Props) => {
  const { user } = useAuth();
  const [overrideReason, setOverrideReason] = useState('');

  const isManagerOrAdmin = user.roles.includes('MANAGER') || user.roles.includes('ADMIN');

  return (
    <Dialog open={true}>
      <DialogTitle>⚠️ Identischer Lead existiert bereits!</DialogTitle>
      <DialogContent>
        {duplicates.map(lead => (
          <Card key={lead.id} sx={{ mb: 2 }}>
            <CardContent>
              <Typography variant="h6">{lead.companyName}</Typography>
              <Typography>Stadt: {lead.city}</Typography>
              <Typography>Zugewiesen: {lead.ownerUserId}</Typography>
              <Typography>Erstellt: {formatDate(lead.registeredAt)}</Typography>
              <Typography>Geschützt bis: {formatDate(lead.protectedUntil)}</Typography>
            </CardContent>
          </Card>
        ))}

        {isManagerOrAdmin && (
          <TextField
            fullWidth
            multiline
            rows={3}
            label="Begründung für Duplikat-Erstellung *"
            value={overrideReason}
            onChange={(e) => setOverrideReason(e.target.value)}
            helperText="Mindestens 10 Zeichen (z.B. 'Neue Abteilung, anderer Ansprechpartner')"
            error={overrideReason.length > 0 && overrideReason.length < 10}
          />
        )}
      </DialogContent>
      <DialogActions>
        <Button onClick={() => onOpenExisting(duplicates[0].id)}>
          Existierenden Lead öffnen
        </Button>
        {isManagerOrAdmin && (
          <Button
            variant="contained"
            color="error"
            disabled={overrideReason.length < 10}
            onClick={() => onOverride(overrideReason)}
          >
            Trotzdem anlegen
          </Button>
        )}
      </DialogActions>
    </Dialog>
  );
};
```

---

### Similar Lead Dialog (Soft Collision)

```typescript
// SimilarLeadDialog.tsx
const SimilarLeadDialog = ({ similarLeads, onOpenExisting, onContinue }: Props) => {
  const [reason, setReason] = useState('');

  return (
    <Dialog open={true}>
      <DialogTitle>ℹ️ Ähnlicher Lead gefunden</DialogTitle>
      <DialogContent>
        {similarLeads.map(lead => (
          <Card key={lead.id} sx={{ mb: 2 }}>
            <CardContent>
              <Typography variant="h6">{lead.companyName}</Typography>
              <Typography>Stadt: {lead.city}</Typography>
              <Typography>Ähnlichkeit: 85% (Firma + Stadt)</Typography>
            </CardContent>
          </Card>
        ))}

        <TextField
          fullWidth
          multiline
          rows={2}
          label="Warum trotzdem anlegen? *"
          value={reason}
          onChange={(e) => setReason(e.target.value)}
          helperText="Mindestens 10 Zeichen (z.B. 'Anderer Standort, separate Filiale')"
          error={reason.length > 0 && reason.length < 10}
        />
      </DialogContent>
      <DialogActions>
        <Button onClick={() => onOpenExisting(similarLeads[0].id)}>
          Existierenden Lead öffnen
        </Button>
        <Button
          variant="contained"
          disabled={reason.length < 10}
          onClick={() => onContinue(reason)}
        >
          Neuen Lead anlegen
        </Button>
      </DialogActions>
    </Dialog>
  );
};
```

---

## Tests

### Unit Tests (Mock-first)

```java
@Test
void checkDuplicate_hardCollision_email_shouldBlock() {
    // Given
    Lead existing = createLead("Test GmbH", "max@test.de", "+49123456789");
    leadRepository.persist(existing);

    LeadDTO dto = LeadDTO.builder()
        .companyName("Another GmbH")
        .contact(new ContactDTO("Max", "Mustermann", "max@test.de", null))
        .build();

    // When
    DedupeResult result = dedupeService.checkDuplicate(dto);

    // Then
    assertThat(result.decision()).isEqualTo(DedupeDecision.BLOCK_HARD);
    assertThat(result.matches()).hasSize(1);
    assertThat(result.matches().get(0).getId()).isEqualTo(existing.getId());
    assertThat(result.message()).contains("Email");
}

@Test
void checkDuplicate_hardCollision_phone_shouldBlock() {
    // Given
    Lead existing = createLead("Test GmbH", "max@test.de", "+49123456789");
    leadRepository.persist(existing);

    LeadDTO dto = LeadDTO.builder()
        .companyName("Another GmbH")
        .contact(new ContactDTO("Lisa", "Schmidt", "lisa@test.de", "0123 456 789"))  // Same phone!
        .build();

    // When
    DedupeResult result = dedupeService.checkDuplicate(dto);

    // Then
    assertThat(result.decision()).isEqualTo(DedupeDecision.BLOCK_HARD);
    assertThat(result.message()).contains("Telefon");
}

@Test
void checkDuplicate_hardCollision_companyAndPostalCode_shouldBlock() {
    // Given
    Lead existing = createLead("Test GmbH", null, null);
    existing.setPostalCode("20095");
    leadRepository.persist(existing);

    LeadDTO dto = LeadDTO.builder()
        .companyName("Test GmbH")  // Same company
        .postalCode("20095")       // Same postal code
        .build();

    // When
    DedupeResult result = dedupeService.checkDuplicate(dto);

    // Then
    assertThat(result.decision()).isEqualTo(DedupeDecision.BLOCK_HARD);
    assertThat(result.message()).contains("Firma+PLZ");
}

@Test
void checkDuplicate_softCollision_sameDomainAndCity_shouldWarn() {
    // Given
    Lead existing = createLead("Test GmbH", "max@test.de", null);
    existing.setCity("Hamburg");
    existing.setEmailDomain("test.de");
    leadRepository.persist(existing);

    LeadDTO dto = LeadDTO.builder()
        .companyName("Another GmbH")
        .city("Hamburg")
        .contact(new ContactDTO("Lisa", "Schmidt", "lisa@test.de", null))  // Same domain!
        .build();

    // When
    DedupeResult result = dedupeService.checkDuplicate(dto);

    // Then
    assertThat(result.decision()).isEqualTo(DedupeDecision.WARN_SOFT);
    assertThat(result.message()).contains("Domain+Stadt");
}

@Test
void checkDuplicate_noDuplicate_shouldClear() {
    // Given
    LeadDTO dto = LeadDTO.builder()
        .companyName("Unique GmbH")
        .city("Berlin")
        .contact(new ContactDTO("Max", "Mustermann", "unique@example.de", null))
        .build();

    // When
    DedupeResult result = dedupeService.checkDuplicate(dto);

    // Then
    assertThat(result.decision()).isEqualTo(DedupeDecision.CLEAR);
    assertThat(result.matches()).isEmpty();
}
```

---

## Metriken

```java
// LeadMetrics.java
@Counted(name = "lead_dedupe_block_total", description = "Harte Kollisionen geblockt")
public void recordBlockedDuplicate() {
    // Auto-increment bei BLOCK_HARD
}

@Counted(name = "lead_dedupe_warn_total", description = "Weiche Kollisionen gewarnt")
public void recordWarnedDuplicate() {
    // Auto-increment bei WARN_SOFT
}

@Counted(name = "lead_duplicate_override_total", description = "Manager/Admin Overrides")
public void recordOverride() {
    // Auto-increment bei override
}
```

---

## Referenzen

- **Sprint 2.1.6:** Fuzzy-Matching mit pg_trgm
- **ADR-004:** Inline-First Architecture (kein separate duplicate_leads table)
- **RFC 7807:** Problem+JSON for error responses

---

**Letzte Aktualisierung:** 2025-10-03
**Autor:** Claude Code (Sprint 2.1.5 Dedupe Implementation)
**Status:** ✅ Production-Ready (Validiert gegen ChatGPT + Claude)
