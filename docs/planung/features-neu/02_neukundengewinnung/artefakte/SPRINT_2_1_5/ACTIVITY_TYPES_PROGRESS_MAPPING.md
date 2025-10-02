# Activity-Types Progress-Mapping - Sprint 2.1.5

**Dokumenttyp:** Business Rules Specification
**Status:** Definitive (02.10.2025)
**Owner:** team/leads-backend
**Sprint:** 2.1.5 Backend Phase 1 (COMPLETE)

---

## 📋 Übersicht

Dieses Dokument definiert verbindlich, welche Activity-Types als "Progress" im Sinne der **60-Tage-Aktivitätsregel** (§3.3 Partnervertrag) gelten.

**Business-Kontext:**
Partner müssen innerhalb von 60 Tagen nach Lead-Registrierung **substanzielle Aktivitäten** nachweisen, um den Lead-Schutz zu behalten. Nicht alle Activity-Logs zählen als Progress – nur echte Vertriebsaktivitäten.

---

## 🎯 Vertragliche Grundlage

### §3.3 Partnervertrag - Lead-Protection Rules

```
"Der Partner verliert den Schutz auf einen Lead, wenn er innerhalb von
60 Tagen keine substantiellen Vertriebsaktivitäten durchführt.

Als substantielle Aktivitäten gelten:
- Qualifizierte Gespräche mit Entscheidungsträgern
- Physische Meetings vor Ort
- Produktdemonstrationen
- ROI-Präsentationen
- Versand von Sample-Boxen

NICHT als substantielle Aktivitäten gelten:
- Reine Email-Kommunikation ohne Response
- Automatische Follow-ups
- Interne Notizen
- Unqualifizierte Anrufe (Sekretariat, Abwesenheit)"
```

---

## 🔢 Activity-Types Mapping

### ✅ countsAsProgress = TRUE (5 Types)

| Activity Type | Beschreibung | Business-Begründung |
|--------------|-------------|---------------------|
| **QUALIFIED_CALL** | Echtes Gespräch mit Entscheider (Küchenchef, Einkaufsleiter) | Direkter Kontakt mit Decision-Maker, qualifiziertes Interesse |
| **MEETING** | Physisches Treffen vor Ort (Restaurant/Hotel/Küche) | Höchste Commitment-Stufe, zeigt ernsthaftes Interesse beider Seiten |
| **DEMO** | Produktdemonstration (Verkostung, Koch-Session) | Aktive Produktpräsentation, zeigt Kaufabsicht |
| **ROI_PRESENTATION** | Business-Value-Präsentation mit Zahlen | Verhandlungsphase, konkrete Geschäftsentwicklung |
| **SAMPLE_SENT** | Sample-Box versendet (physisches Produkt) | Tangible Action, Kunde kann Produkt testen |

### ❌ countsAsProgress = FALSE (5 Types)

| Activity Type | Beschreibung | Business-Begründung |
|--------------|-------------|---------------------|
| **NOTE** | Interne Notiz ohne Kundenkontakt | Rein administrativ, keine Außenwirkung |
| **FOLLOW_UP** | Automatisches Follow-up via System | Zu low-touch, kein aktives Engagement |
| **EMAIL** | Standard-Email (nicht qualifiziert) | Zu passiv, oft keine Response |
| **CALL** | Unqualifizierter Anruf (Sekretariat, Voicemail, kein Decision-Maker) | Kein echtes Gespräch mit Entscheider |
| **SAMPLE_FEEDBACK** | Passive Feedback-Dokumentation | Nur Logging, keine aktive Vertriebshandlung |

---

## 🏗️ Technische Implementierung

### 1. Backend Enum

**ActivityType.java:**
```java
public enum ActivityType {
    // countsAsProgress = TRUE
    QUALIFIED_CALL(true, "Qualifiziertes Gespräch mit Entscheider"),
    MEETING(true, "Physisches Treffen vor Ort"),
    DEMO(true, "Produktdemonstration"),
    ROI_PRESENTATION(true, "ROI-Präsentation"),
    SAMPLE_SENT(true, "Sample-Box versendet"),

    // countsAsProgress = FALSE
    NOTE(false, "Interne Notiz"),
    FOLLOW_UP(false, "Automatisches Follow-up"),
    EMAIL(false, "Standard-Email"),
    CALL(false, "Unqualifizierter Anruf"),
    SAMPLE_FEEDBACK(false, "Sample-Feedback");

    private final boolean countsAsProgress;
    private final String displayName;

    ActivityType(boolean countsAsProgress, String displayName) {
        this.countsAsProgress = countsAsProgress;
        this.displayName = displayName;
    }

    public boolean countsAsProgress() {
        return countsAsProgress;
    }

    public String getDisplayName() {
        return displayName;
    }
}
```

### 2. Database Schema

**V256 Migration (bereits deployed):**
```sql
-- lead_activities Tabelle erweitert
ALTER TABLE lead_activities
ADD COLUMN counts_as_progress BOOLEAN DEFAULT FALSE;

-- Update existing data (einmalig):
UPDATE lead_activities
SET counts_as_progress = true
WHERE activity_type IN (
    'QUALIFIED_CALL',
    'MEETING',
    'DEMO',
    'ROI_PRESENTATION',
    'SAMPLE_SENT'
);

-- Index für Performance:
CREATE INDEX idx_lead_activities_progress
ON lead_activities(lead_id, counts_as_progress, activity_date)
WHERE counts_as_progress = true;
```

### 3. Progress Deadline Calculation

**LeadProtectionService.java:**
```java
public Instant calculateProgressDeadline(Lead lead) {
    // Find latest progress-counting activity
    Instant lastProgressActivity = activityRepository
        .findLatestProgressActivity(lead.getId())
        .map(LeadActivity::getActivityDate)
        .orElse(lead.getRegisteredAt());

    // Add 60 days
    return lastProgressActivity.plus(60, ChronoUnit.DAYS);
}

public boolean hasRecentProgress(Long leadId) {
    Instant deadline = Instant.now().minus(60, ChronoUnit.DAYS);

    return activityRepository.existsByLeadIdAndCountsAsProgressAndActivityDateAfter(
        leadId,
        true,
        deadline
    );
}
```

### 4. Activity Repository Query

**LeadActivityRepository.java:**
```java
@Query("""
    SELECT a FROM LeadActivity a
    WHERE a.lead.id = :leadId
    AND a.countsAsProgress = true
    ORDER BY a.activityDate DESC
    LIMIT 1
""")
Optional<LeadActivity> findLatestProgressActivity(@Param("leadId") Long leadId);

boolean existsByLeadIdAndCountsAsProgressAndActivityDateAfter(
    Long leadId,
    boolean countsAsProgress,
    Instant after
);
```

---

## 🎨 Frontend UI Integration

### 1. Activity Timeline Filtering

**ActivityTimeline.tsx:**
```tsx
const progressActivities = activities.filter(
  (activity) => activity.countsAsProgress
);

const nonProgressActivities = activities.filter(
  (activity) => !activity.countsAsProgress
);

return (
  <Box>
    {/* Progress Activities hervorgehoben */}
    <Typography variant="h6" color="success.main">
      Substantielle Aktivitäten ({progressActivities.length})
    </Typography>
    <Timeline>
      {progressActivities.map((activity) => (
        <TimelineItem key={activity.id}>
          <TimelineDot color="success" />
          <TimelineContent>{activity.summary}</TimelineContent>
        </TimelineItem>
      ))}
    </Timeline>

    {/* Non-Progress Activities gedämpft */}
    <Typography variant="h6" color="text.secondary">
      Weitere Aktivitäten ({nonProgressActivities.length})
    </Typography>
    <Timeline>
      {nonProgressActivities.map((activity) => (
        <TimelineItem key={activity.id}>
          <TimelineDot color="grey" />
          <TimelineContent>{activity.summary}</TimelineContent>
        </TimelineItem>
      ))}
    </Timeline>
  </Box>
);
```

### 2. Activity Type Selector

**ActivityCreateDialog.tsx:**
```tsx
<FormControl fullWidth>
  <InputLabel>Aktivitätstyp</InputLabel>
  <Select
    value={activityType}
    onChange={(e) => setActivityType(e.target.value)}
  >
    <ListSubheader>✅ Zählt als Progress</ListSubheader>
    <MenuItem value="QUALIFIED_CALL">
      <Chip label="Progress" color="success" size="small" sx={{ mr: 1 }} />
      Qualifiziertes Gespräch
    </MenuItem>
    <MenuItem value="MEETING">
      <Chip label="Progress" color="success" size="small" sx={{ mr: 1 }} />
      Meeting vor Ort
    </MenuItem>
    <MenuItem value="DEMO">
      <Chip label="Progress" color="success" size="small" sx={{ mr: 1 }} />
      Produktdemonstration
    </MenuItem>
    <MenuItem value="ROI_PRESENTATION">
      <Chip label="Progress" color="success" size="small" sx={{ mr: 1 }} />
      ROI-Präsentation
    </MenuItem>
    <MenuItem value="SAMPLE_SENT">
      <Chip label="Progress" color="success" size="small" sx={{ mr: 1 }} />
      Sample-Box versendet
    </MenuItem>

    <ListSubheader>❌ Zählt nicht als Progress</ListSubheader>
    <MenuItem value="NOTE">Interne Notiz</MenuItem>
    <MenuItem value="FOLLOW_UP">Follow-up</MenuItem>
    <MenuItem value="EMAIL">Email</MenuItem>
    <MenuItem value="CALL">Anruf (unqualifiziert)</MenuItem>
    <MenuItem value="SAMPLE_FEEDBACK">Sample-Feedback</MenuItem>
  </Select>
</FormControl>

{/* Hinweis bei Non-Progress Aktivität */}
{activityType && !progressTypes.includes(activityType) && (
  <Alert severity="info" sx={{ mt: 2 }}>
    Diese Aktivität zählt <strong>nicht</strong> als substanzielle
    Vertriebsaktivität im Sinne der 60-Tage-Regel.
  </Alert>
)}
```

---

## 📊 Business-Metriken

### 1. Lead-Health-Score

```java
public enum LeadHealthStatus {
    HEALTHY,   // Progress in letzten 30 Tagen
    WARNING,   // Progress 30-53 Tage her
    CRITICAL,  // Progress 54-60 Tage her
    EXPIRED    // Kein Progress seit >60 Tagen
}

public LeadHealthStatus calculateHealthStatus(Lead lead) {
    Instant lastProgress = findLatestProgressActivity(lead.getId())
        .map(LeadActivity::getActivityDate)
        .orElse(lead.getRegisteredAt());

    long daysSinceProgress = ChronoUnit.DAYS.between(lastProgress, Instant.now());

    if (daysSinceProgress <= 30) return HEALTHY;
    if (daysSinceProgress <= 53) return WARNING;
    if (daysSinceProgress <= 60) return CRITICAL;
    return EXPIRED;
}
```

### 2. Partner Performance Dashboard

**Metriken:**
- **Progress-Rate:** Anteil Leads mit mindestens 1 Progress-Activity in 60 Tagen
- **Average Progress-Frequency:** Durchschnittliche Anzahl Progress-Activities pro Lead
- **Preferred Activity-Type:** Top 3 meistgenutzte Progress-Types

```sql
-- Query für Dashboard Widget:
SELECT
  activity_type,
  COUNT(*) as total_count,
  COUNT(DISTINCT lead_id) as unique_leads
FROM lead_activities
WHERE counts_as_progress = true
  AND owner_user_id = :userId
  AND activity_date >= NOW() - INTERVAL '90 days'
GROUP BY activity_type
ORDER BY total_count DESC;
```

---

## 🧪 Test-Szenarien

### 1. Progress Deadline Calculation

```java
@Test
void shouldCalculateProgressDeadline() {
    Lead lead = createLead(Instant.parse("2025-10-01T00:00:00Z"));

    // Erste Progress-Activity nach 10 Tagen
    createActivity(lead, QUALIFIED_CALL, Instant.parse("2025-10-11T10:00:00Z"));

    Instant deadline = protectionService.calculateProgressDeadline(lead);

    // Erwarte: 2025-10-11 + 60 Tage = 2025-12-10
    assertEquals(Instant.parse("2025-12-10T10:00:00Z"), deadline);
}
```

### 2. Mixed Activities - Only Progress Counts

```java
@Test
void shouldOnlyCountProgressActivities() {
    Lead lead = createLead(Instant.parse("2025-10-01T00:00:00Z"));

    // Non-Progress Activities (zählen NICHT)
    createActivity(lead, NOTE, Instant.parse("2025-10-05T10:00:00Z"));
    createActivity(lead, EMAIL, Instant.parse("2025-10-10T10:00:00Z"));

    // Progress Activity (zählt!)
    createActivity(lead, MEETING, Instant.parse("2025-10-15T10:00:00Z"));

    Instant deadline = protectionService.calculateProgressDeadline(lead);

    // Erwarte: 2025-10-15 (MEETING) + 60 Tage
    assertEquals(Instant.parse("2025-12-14T10:00:00Z"), deadline);
}
```

### 3. Warning System

```java
@Test
void shouldTriggerWarningAt53Days() {
    Lead lead = createLead(Instant.parse("2025-08-01T00:00:00Z"));
    createActivity(lead, QUALIFIED_CALL, Instant.parse("2025-08-01T00:00:00Z"));

    // 53 Tage später (Warning-Schwelle)
    Instant warningDate = Instant.parse("2025-09-23T00:00:00Z");

    LeadHealthStatus status = protectionService.calculateHealthStatus(lead, warningDate);

    assertEquals(LeadHealthStatus.WARNING, status);
}
```

---

## 📚 Business Rules Summary

### Klare Entscheidungsmatrix

| Szenario | Activity Type | Zählt als Progress? | Begründung |
|----------|--------------|---------------------|------------|
| Telefonat mit Küchenchef, 15min Gespräch über Bedarf | QUALIFIED_CALL | ✅ JA | Entscheider erreicht |
| Anruf, nur Sekretariat erreicht | CALL | ❌ NEIN | Kein Decision-Maker |
| Meeting vor Ort, Produktvorstellung | MEETING | ✅ JA | Highest Engagement |
| Email mit Produktinfos verschickt | EMAIL | ❌ NEIN | Zu passiv |
| Sample-Box verschickt (DHL-Tracking) | SAMPLE_SENT | ✅ JA | Tangible Action |
| Notiz: "Kunde will im Dezember entscheiden" | NOTE | ❌ NEIN | Nur Logging |
| Demo-Termin, Koch-Session mit Produkt | DEMO | ✅ JA | Aktive Präsentation |
| Automatisches Follow-up Email (T+7) | FOLLOW_UP | ❌ NEIN | System-generiert |
| ROI-Calculation präsentiert | ROI_PRESENTATION | ✅ JA | Verhandlungsphase |
| Sample-Feedback dokumentiert | SAMPLE_FEEDBACK | ❌ NEIN | Passives Logging |

---

## 🔄 Änderungshistorie

| Datum | Änderung | Begründung |
|-------|----------|------------|
| 2025-10-02 | Initial Definition | Sprint 2.1.5 Planning |
| - | - | Future Changes hier dokumentieren |

---

## 📚 Referenzen

- **Partnervertrag:** §3.3 Lead-Protection Rules
- **TRIGGER_SPRINT_2_1_5.md:** Zeile 170-184 (Activity-Types Progress-Mapping)
- **V256 Migration:** `backend/src/main/resources/db/migration/V256__lead_activities_progress_tracking.sql`
- **ActivityType.java:** `backend/src/main/java/de/freshplan/modules/leads/domain/ActivityType.java`
- **LeadProtectionService.java:** `backend/src/main/java/de/freshplan/modules/leads/service/LeadProtectionService.java`

---

**Dokument-Owner:** Jörg Streeck + Claude Code
**Letzte Änderung:** 2025-10-02
**Version:** 1.0 (Definitive)
