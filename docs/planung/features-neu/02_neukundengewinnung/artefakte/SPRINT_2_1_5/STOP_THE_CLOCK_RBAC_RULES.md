# Stop-the-Clock RBAC Rules - Sprint 2.1.5/2.1.6

**Dokumenttyp:** Security & Business Rules Specification
**Status:** Backend Rules Definitive (02.10.2025), UI Implementation Sprint 2.1.6
**Owner:** team/leads-backend
**Sprint:** 2.1.5 (Backend-only) → 2.1.6 (UI)

---

## 📋 Übersicht

Dieses Dokument definiert die **Role-Based Access Control (RBAC)** Regeln für den **Stop-the-Clock Mechanismus** im Lead-Protection-System.

**Business-Kontext:**
In Ausnahmefällen (z.B. FreshFoodz-Verzögerung, Kunde im Urlaub) kann die 60-Tage-Progress-Frist **pausiert** werden. Diese Funktion ist hochsensibel und darf nur von autorisierten Rollen genutzt werden.

**Vertragliche Grundlage:**
§3.3.2 Partnervertrag - "Bei nachweisbaren Verzögerungen seitens FreshFoodz kann eine Grace Period von bis zu 10 Tagen gewährt werden."

---

## 🔒 RBAC-Policy

### 1. Berechtigte Rollen

| Role | Pausieren | Resumen | Grund ändern | Audit einsehen | Begründung |
|------|-----------|---------|--------------|----------------|------------|
| **MANAGER** | ✅ JA | ✅ JA | ✅ JA | ✅ JA | Führungsebene, Vertriebsverantwortung |
| **ADMIN** | ✅ JA | ✅ JA | ✅ JA | ✅ JA | Technische & Compliance-Verantwortung |
| **PARTNER** | ❌ NEIN | ❌ NEIN | ❌ NEIN | ⚠️ Nur eigene | Regulärer Vertriebsmitarbeiter |
| **VIEWER** | ❌ NEIN | ❌ NEIN | ❌ NEIN | ⚠️ Nur eigene | Read-only Zugriff |

### 2. Backend Annotation

**LeadProtectionResource.java:**
```java
@Path("/leads/{id}/stop-clock")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LeadProtectionResource {

    @POST
    @RolesAllowed({"manager", "admin"})
    @Transactional
    public Response pauseClock(
        @PathParam("id") Long leadId,
        StopClockRequest request
    ) {
        // Validierung + Business-Logik
        leadProtectionService.pauseClock(leadId, request, securityContext);

        // Audit-Log
        auditService.log("lead_protection_clock_paused", leadId, request);

        return Response.ok().build();
    }

    @DELETE
    @RolesAllowed({"manager", "admin"})
    @Transactional
    public Response resumeClock(@PathParam("id") Long leadId) {
        leadProtectionService.resumeClock(leadId, securityContext);
        auditService.log("lead_protection_clock_resumed", leadId);
        return Response.ok().build();
    }

    @GET
    @Path("/history")
    @RolesAllowed({"manager", "admin", "partner"}) // Partner sieht nur eigene
    public Response getStopClockHistory(@PathParam("id") Long leadId) {
        // RLS-Validierung: Partner sehen nur eigene Leads
        List<StopClockEvent> history = leadProtectionService
            .getStopClockHistory(leadId, securityContext);
        return Response.ok(history).build();
    }
}
```

---

## 📝 Erlaubte Gründe

### 1. Definierte Grund-Kategorien

```java
public enum StopClockReason {
    FRESHFOODZ_DELAY(
        "FreshFoodz Verzögerung",
        "Verzögerung bei Lieferung, Vertragsabwicklung oder Support seitens FreshFoodz",
        10, // Max. Pausendauer in Tagen (vertragliche Grace Period)
        true // Automatische Genehmigung
    ),

    CUSTOMER_VACATION(
        "Kunde im Urlaub",
        "Entscheider ist nachweislich abwesend (Urlaub, Krankheit, Betriebsferien)",
        30, // Max. Pausendauer
        false // Manuelle Genehmigung erforderlich
    ),

    SEASONAL_BREAK(
        "Saisonale Pause",
        "Geschäftspause während Nebensaison (z.B. Ski-Resort im Sommer)",
        60, // Max. Pausendauer
        false // Manuelle Genehmigung
    ),

    OTHER(
        "Andere",
        "Sonstige begründete Ausnahmesituation (Freitext-Begründung PFLICHT)",
        14, // Max. Pausendauer (Default)
        false // Manuelle Genehmigung
    );

    private final String displayName;
    private final String description;
    private final int maxPauseDays;
    private final boolean autoApprove;

    // Getter, Constructor...
}
```

### 2. Validierungsregeln

**LeadProtectionService.java:**
```java
public void pauseClock(Long leadId, StopClockRequest request, SecurityContext ctx) {
    // 1. RBAC-Check (bereits durch @RolesAllowed, aber double-check)
    if (!hasRole(ctx, "manager", "admin")) {
        throw new ForbiddenException("Insufficient privileges for stop-clock");
    }

    // 2. Grund-Validierung
    if (request.reason() == null) {
        throw new BadRequestException("Reason is required");
    }

    // 3. Freitext PFLICHT bei OTHER
    if (request.reason() == StopClockReason.OTHER &&
        (request.reasonText() == null || request.reasonText().isBlank())) {
        throw new BadRequestException("Reason text required for OTHER category");
    }

    // 4. Max. Pausendauer prüfen
    if (request.pauseDays() > request.reason().getMaxPauseDays()) {
        throw new BadRequestException(
            "Pause duration exceeds maximum for reason: " +
            request.reason().getMaxPauseDays() + " days"
        );
    }

    // 5. Aktive Pause bereits vorhanden?
    if (lead.getStopClockPausedAt() != null) {
        throw new ConflictException("Clock is already paused");
    }

    // 6. Lead-Status PROTECTED?
    if (lead.getProtectionStatus() != ProtectionStatus.PROTECTED) {
        throw new BadRequestException("Clock can only be paused for PROTECTED leads");
    }

    // 7. Business-Logik
    lead.setStopClockPausedAt(Instant.now());
    lead.setStopClockReason(request.reason());
    lead.setStopClockReasonText(request.reasonText());
    lead.setStopClockResumeScheduledAt(
        Instant.now().plus(request.pauseDays(), ChronoUnit.DAYS)
    );

    leadRepository.persist(lead);
}
```

---

## 🎨 UI Implementation (Sprint 2.1.6)

### 1. StopTheClockDialog Component

**StopTheClockDialog.tsx:**
```tsx
interface StopTheClockDialogProps {
  open: boolean;
  leadId: number;
  onClose: () => void;
}

export function StopTheClockDialog({ open, leadId, onClose }: Props) {
  const [reason, setReason] = useState<StopClockReason | null>(null);
  const [reasonText, setReasonText] = useState('');
  const [pauseDays, setPauseDays] = useState(10);

  const handleSubmit = async () => {
    await pauseClock(leadId, {
      reason,
      reasonText: reason === 'OTHER' ? reasonText : null,
      pauseDays
    });
    onClose();
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>60-Tage-Frist pausieren</DialogTitle>
      <DialogContent>
        <Alert severity="warning" sx={{ mb: 2 }}>
          Diese Funktion ist nur für <strong>Ausnahmesituationen</strong> gedacht
          und wird im Audit-Log protokolliert.
        </Alert>

        {/* Grund-Auswahl */}
        <FormControl fullWidth sx={{ mb: 2 }}>
          <InputLabel>Grund *</InputLabel>
          <Select
            value={reason}
            onChange={(e) => setReason(e.target.value as StopClockReason)}
            required
          >
            <MenuItem value="FRESHFOODZ_DELAY">
              FreshFoodz Verzögerung (max. 10 Tage)
            </MenuItem>
            <MenuItem value="CUSTOMER_VACATION">
              Kunde im Urlaub (max. 30 Tage)
            </MenuItem>
            <MenuItem value="SEASONAL_BREAK">
              Saisonale Pause (max. 60 Tage)
            </MenuItem>
            <MenuItem value="OTHER">Andere (max. 14 Tage)</MenuItem>
          </Select>
        </FormControl>

        {/* Freitext bei OTHER */}
        {reason === 'OTHER' && (
          <TextField
            fullWidth
            label="Begründung (Pflicht)"
            multiline
            rows={3}
            value={reasonText}
            onChange={(e) => setReasonText(e.target.value)}
            required
            helperText="Bitte konkrete Begründung für die Ausnahmesituation angeben"
            sx={{ mb: 2 }}
          />
        )}

        {/* Pausendauer */}
        <TextField
          fullWidth
          type="number"
          label="Pausendauer (Tage)"
          value={pauseDays}
          onChange={(e) => setPauseDays(parseInt(e.target.value))}
          inputProps={{
            min: 1,
            max: getMaxPauseDays(reason)
          }}
          helperText={`Max. ${getMaxPauseDays(reason)} Tage für gewählten Grund`}
        />

        {/* Info: Vertragliche Grundlage */}
        {reason === 'FRESHFOODZ_DELAY' && (
          <Alert severity="info" sx={{ mt: 2 }}>
            §3.3.2 Partnervertrag: Vertragliche Grace Period bei FreshFoodz-Verzögerung
          </Alert>
        )}
      </DialogContent>

      <DialogActions>
        <Button onClick={onClose}>Abbrechen</Button>
        <Button
          onClick={handleSubmit}
          variant="contained"
          color="warning"
          disabled={!reason || (reason === 'OTHER' && !reasonText)}
        >
          Frist pausieren
        </Button>
      </DialogActions>
    </Dialog>
  );
}
```

### 2. LeadProtectionBadge Integration

**LeadProtectionBadge.tsx (erweitert):**
```tsx
export function LeadProtectionBadge({ lead }: Props) {
  const [dialogOpen, setDialogOpen] = useState(false);
  const hasStopClockPermission = useHasRole(['manager', 'admin']);

  // Prüfen ob Clock aktuell pausiert
  const isPaused = lead.stopClockPausedAt != null;

  return (
    <Box>
      <Tooltip title={getTooltipText(lead)}>
        <Chip
          icon={isPaused ? <PauseIcon /> : <ShieldIcon />}
          label={isPaused ? 'Pausiert' : getStatusLabel(lead)}
          color={isPaused ? 'warning' : getStatusColor(lead)}
        />
      </Tooltip>

      {/* Stop-the-Clock Buttons (nur für Manager + Admin) */}
      {hasStopClockPermission && (
        <Box sx={{ mt: 1 }}>
          {!isPaused ? (
            <Button
              size="small"
              startIcon={<PauseIcon />}
              onClick={() => setDialogOpen(true)}
              disabled={lead.protectionStatus !== 'PROTECTED'}
            >
              Frist pausieren
            </Button>
          ) : (
            <Button
              size="small"
              startIcon={<PlayArrowIcon />}
              onClick={() => resumeClock(lead.id)}
              color="success"
            >
              Frist fortsetzen
            </Button>
          )}
        </Box>
      )}

      {/* Pause-Info anzeigen */}
      {isPaused && (
        <Alert severity="warning" sx={{ mt: 1 }}>
          <Typography variant="body2">
            <strong>Pausiert seit:</strong>{' '}
            {formatDate(lead.stopClockPausedAt)}
          </Typography>
          <Typography variant="body2">
            <strong>Grund:</strong> {lead.stopClockReason}
          </Typography>
          {lead.stopClockReasonText && (
            <Typography variant="body2">
              <strong>Details:</strong> {lead.stopClockReasonText}
            </Typography>
          )}
          <Typography variant="body2">
            <strong>Automatische Fortsetzung:</strong>{' '}
            {formatDate(lead.stopClockResumeScheduledAt)}
          </Typography>
        </Alert>
      )}

      {/* Dialog */}
      <StopTheClockDialog
        open={dialogOpen}
        leadId={lead.id}
        onClose={() => setDialogOpen(false)}
      />
    </Box>
  );
}
```

---

## 📊 Audit-Log Format

### 1. Clock Paused Event

```json
{
  "event_type": "lead_protection_clock_paused",
  "timestamp": "2025-10-05T14:30:00Z",
  "user_id": "manager-abc-123",
  "user_role": "manager",
  "lead_id": 12345,
  "data": {
    "reason": "FRESHFOODZ_DELAY",
    "reason_text": "Lieferverzögerung aufgrund Logistikproblem",
    "pause_days": 10,
    "paused_at": "2025-10-05T14:30:00Z",
    "resume_scheduled_at": "2025-10-15T14:30:00Z",
    "progress_deadline_before": "2025-11-20T00:00:00Z",
    "progress_deadline_after": "2025-11-30T00:00:00Z"
  },
  "ip_address": "192.168.1.100",
  "user_agent": "Mozilla/5.0..."
}
```

### 2. Clock Resumed Event

```json
{
  "event_type": "lead_protection_clock_resumed",
  "timestamp": "2025-10-15T14:30:00Z",
  "user_id": "manager-abc-123",
  "user_role": "manager",
  "lead_id": 12345,
  "data": {
    "paused_duration_days": 10,
    "originally_paused_at": "2025-10-05T14:30:00Z",
    "reason_was": "FRESHFOODZ_DELAY",
    "resume_type": "automatic" // or "manual"
  }
}
```

### 3. Abuse Detection

**Monitoring-Regel:**
```sql
-- Partner mit verdächtig vielen Stop-Clock Events
SELECT
  user_id,
  COUNT(*) as total_pauses,
  COUNT(DISTINCT lead_id) as unique_leads,
  AVG(EXTRACT(EPOCH FROM (data->>'resume_scheduled_at')::timestamptz -
              (data->>'paused_at')::timestamptz) / 86400) as avg_pause_days
FROM audit_log
WHERE event_type = 'lead_protection_clock_paused'
  AND timestamp >= NOW() - INTERVAL '90 days'
GROUP BY user_id
HAVING COUNT(*) > 20  -- Threshold: >20 Pauses in 90 Tagen
ORDER BY total_pauses DESC;
```

---

## 🔄 Automatische Fortsetzung

### 1. Scheduled Job

**StopClockScheduler.java:**
```java
@ApplicationScoped
public class StopClockScheduler {

    @Scheduled(cron = "0 0 * * * ?")  // Jede volle Stunde
    @Transactional
    void resumeExpiredPauses() {
        List<Lead> expiredPauses = leadRepository.find(
            "stopClockPausedAt IS NOT NULL AND stopClockResumeScheduledAt <= ?1",
            Instant.now()
        ).list();

        for (Lead lead : expiredPauses) {
            log.info("Auto-resuming clock for lead {}", lead.getId());

            lead.setStopClockPausedAt(null);
            lead.setStopClockReason(null);
            lead.setStopClockReasonText(null);
            lead.setStopClockResumeScheduledAt(null);

            auditService.log("lead_protection_clock_resumed", lead.getId(), Map.of(
                "resume_type", "automatic"
            ));

            // Recalculate Progress Deadline
            protectionService.recalculateProgressDeadline(lead);
        }

        log.info("Auto-resumed {} expired clock pauses", expiredPauses.size());
    }
}
```

---

## 🧪 Test-Szenarien

### 1. RBAC Enforcement

```java
@Test
void partnerCannotPauseClock() {
    var partner = mockSecurityContext("partner");

    assertThrows(ForbiddenException.class, () -> {
        protectionResource.pauseClock(leadId, new StopClockRequest(
            StopClockReason.OTHER, "test", 10
        ));
    });
}

@Test
void managerCanPauseClock() {
    var manager = mockSecurityContext("manager");

    Response response = protectionResource.pauseClock(leadId, new StopClockRequest(
        StopClockReason.FRESHFOODZ_DELAY, null, 10
    ));

    assertEquals(200, response.getStatus());
}
```

### 2. Validation Rules

```java
@Test
void shouldRejectOtherReasonWithoutText() {
    var manager = mockSecurityContext("manager");

    assertThrows(BadRequestException.class, () -> {
        protectionService.pauseClock(leadId, new StopClockRequest(
            StopClockReason.OTHER,
            null,  // reasonText FEHLT
            10
        ), manager);
    });
}

@Test
void shouldRejectExcessivePauseDuration() {
    var manager = mockSecurityContext("manager");

    assertThrows(BadRequestException.class, () -> {
        protectionService.pauseClock(leadId, new StopClockRequest(
            StopClockReason.FRESHFOODZ_DELAY,
            null,
            15  // Max. 10 Tage erlaubt
        ), manager);
    });
}
```

---

## 📚 Referenzen

- **Partnervertrag:** §3.3.2 Grace Period bei FreshFoodz-Verzögerung
- **TRIGGER_SPRINT_2_1_5.md:** Zeile 186-195 (Stop-the-Clock Rules)
- **Lead.java Entity:** `backend/src/main/java/de/freshplan/modules/leads/domain/Lead.java`
- **LeadProtectionService.java:** `backend/src/main/java/de/freshplan/modules/leads/service/LeadProtectionService.java`
- **Security Context:** Modul 00 ABAC/RLS Foundation

---

**Dokument-Owner:** Jörg Streeck + Claude Code
**Letzte Änderung:** 2025-10-02
**Version:** 1.0 (Backend Rules Definitive, UI Sprint 2.1.6)
