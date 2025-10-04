---
module: "02_neukundengewinnung"
domain: "shared"
doc_type: "contract"
status: "approved"
sprint: "2.1.5"
owner: "team/leads-backend"
updated: "2025-10-03"
---

# Activity Types Progress Mapping - Verbindliche Business Rules

**📍 Navigation:** Home → Planung → 02 Neukundengewinnung → Artefakte → Sprint 2.1.5 → Activity Types Progress Mapping

## Zweck

Definiert **verbindlich** welche Activity-Types als "belegbarer Fortschritt" (§2(8)(b) Handelsvertretervertrag) gelten.

**Business-Kontext:**
- Vertrag fordert "belegbaren Fortschritt je 60 Tage"
- **NUR qualifizierte Vertriebsaktivitäten** zählen als Progress
- **Administrative/Dokumentative** Aktivitäten zählen NICHT

---

## Verbindliche Mapping-Tabelle

| Activity Type | counts_as_progress | Begründung | Beispiel |
|--------------|-------------------|------------|----------|
| **QUALIFIED_CALL** | ✅ TRUE | Qualifiziertes Telefonat mit Entscheider | "Gespräch mit Einkaufsleiter, Interesse an Bio-Produkten bekundet" |
| **MEETING** | ✅ TRUE | Persönliches Treffen beim Kunden | "Vor-Ort-Termin in der Küche, Bedarf analysiert" |
| **DEMO** | ✅ TRUE | Produktdemo/Präsentation | "Live-Cooking-Demo mit Convenience-Produkten" |
| **ROI_PRESENTATION** | ✅ TRUE | ROI-Kalkulation präsentiert | "Cost-Benefit-Analyse für Hauptgericht-Linie vorgestellt" |
| **SAMPLE_SENT** | ✅ TRUE | Produktproben versendet | "10kg Testpaket Bio-Convenience versendet, Feedback bis 15.10." |
| **NOTE** | ❌ FALSE | Interne Notiz | "Kunde hat neue Website gelauncht" |
| **FOLLOW_UP** | ❌ FALSE | Follow-Up geplant | "Reminder: Kunde in 2 Wochen wieder anrufen" |
| **EMAIL** | ❌ FALSE | Standard-Email versendet | "Produktkatalog per Email verschickt" |
| **CALL** | ❌ FALSE | Unqualifizierter Anruf | "Anruf - Ansprechpartner nicht erreicht" |
| **SAMPLE_FEEDBACK** | ❌ FALSE | Feedback zu Samples erhalten | "Kunde meldet: Samples waren gut, braucht aber noch Zeit" |
| **FIRST_CONTACT_DOCUMENTED** | ❌ FALSE | Erstkontakt dokumentiert (Pre-Claim) | "Messe-Gespräch am Stand 12, allgemeines Interesse" |
| **EMAIL_RECEIVED** | ❌ FALSE | Eingehende Email erfasst (Quick-Capture) | "Email von Kunde: Anfrage zu Lieferkonditionen" |
| **LEAD_ASSIGNED** | ❌ FALSE | Lead zugewiesen (Audit) | "Lead automatisch zugewiesen an Partner Nord via Geo+Workload" |

---

## Business Rules (Detailliert)

### Rule 1: Qualifizierte Vertriebsaktivitäten (counts_as_progress = TRUE)

**Definition:** Aktivitäten die **aktiven Vertriebsfortschritt** zeigen und den Lead dem Abschluss näher bringen.

**Kriterien (mindestens 1 muss erfüllt sein):**
1. **Direkter Kundenkontakt** mit **Entscheider** (qualifiziertes Telefonat, Meeting)
2. **Produktpräsentation** (Demo, ROI-Kalkulation)
3. **Konkrete Geschäftsanbahnung** (Samples versendet, Angebot erstellt)

**Wirkung:**
- `leads.progress_deadline` wird auf `activity_date + 60 Tage` gesetzt (via Trigger V257)
- `leads.last_activity_at` wird aktualisiert
- Lead-Schutz bleibt aktiv (60-Tage-Uhr startet neu)

---

### Rule 2: Administrative/Dokumentative Aktivitäten (counts_as_progress = FALSE)

**Definition:** Aktivitäten die zwar **dokumentiert** werden müssen, aber **keinen Vertriebsfortschritt** darstellen.

**Beispiele:**
- **Interne Notizen** (NOTE) - "Kunde hat neue Küche eingebaut"
- **Follow-Up-Planung** (FOLLOW_UP) - "Reminder in 2 Wochen"
- **Standard-Email** (EMAIL) - "Katalog versendet"
- **Unqualifizierte Calls** (CALL) - "Anruf - Ansprechpartner nicht erreicht"
- **Feedback ohne Aktion** (SAMPLE_FEEDBACK) - "Kunde sagt: Samples gut, braucht noch Zeit"

**Wirkung:**
- `leads.progress_deadline` bleibt **unverändert** (Trigger V257 fired nicht)
- Lead-Schutz-Uhr läuft weiter (ACHTUNG: Lead kann ablaufen!)
- Activity wird trotzdem erfasst (für Kontext/Audit)

---

### Rule 3: System-Aktivitäten (counts_as_progress = FALSE)

**Definition:** Automatisch erzeugte Activities für Audit/Prozess-Dokumentation.

**Beispiele:**
- **FIRST_CONTACT_DOCUMENTED** - Erstkontakt bei Pre-Claim Stage 0
- **EMAIL_RECEIVED** - Quick-Capture aus eingehender Email
- **LEAD_ASSIGNED** - Automatische Zuweisung (Geo+Workload)

**Wirkung:**
- Rein dokumentativ
- Keine Auswirkung auf Progress-Deadline
- Wichtig für Audit-Trail

---

## Implementation

### Backend (Enum + Mapping)

```java
// LeadActivityType.java
public enum LeadActivityType {
    // ✅ Progress Activities (counts_as_progress = TRUE)
    QUALIFIED_CALL("Qualifiziertes Telefonat", true),
    MEETING("Persönliches Treffen", true),
    DEMO("Produktdemo", true),
    ROI_PRESENTATION("ROI-Präsentation", true),
    SAMPLE_SENT("Produktproben versendet", true),

    // ❌ Non-Progress Activities (counts_as_progress = FALSE)
    NOTE("Interne Notiz", false),
    FOLLOW_UP("Follow-Up geplant", false),
    EMAIL("Email versendet", false),
    CALL("Anruf", false),
    SAMPLE_FEEDBACK("Sample-Feedback", false),

    // ❌ System Activities (counts_as_progress = FALSE)
    FIRST_CONTACT_DOCUMENTED("Erstkontakt dokumentiert", false),
    EMAIL_RECEIVED("Email erhalten", false),
    LEAD_ASSIGNED("Lead zugewiesen", false);

    private final String displayName;
    private final boolean countsAsProgress;

    LeadActivityType(String displayName, boolean countsAsProgress) {
        this.displayName = displayName;
        this.countsAsProgress = countsAsProgress;
    }

    public boolean countsAsProgress() {
        return countsAsProgress;
    }

    public String getDisplayName() {
        return displayName;
    }
}
```

### LeadActivity.java

```java
@Entity
@Table(name = "lead_activities")
public class LeadActivity {
    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;

    @Column(name = "lead_id", nullable = false)
    private UUID leadId;

    @Column(name = "activity_type", length = 50, nullable = false)
    private String activityType;  // Stored as String (VARCHAR)

    @Column(name = "activity_date", nullable = false)
    private Instant activityDate;

    @Column(name = "counts_as_progress", nullable = false)
    private Boolean countsAsProgress = false;  // ← DEFAULT FALSE (konservativ!)

    @Column(name = "summary", length = 500)
    private String summary;

    @Column(name = "outcome", length = 50)
    private String outcome;

    @Column(name = "performed_by", length = 50)
    private String performedBy;

    // Getters/Setters

    @PrePersist
    @PreUpdate
    public void setCountsAsProgressFromType() {
        // Auto-set countsAsProgress basierend auf activityType
        if (activityType != null) {
            try {
                LeadActivityType type = LeadActivityType.valueOf(activityType);
                this.countsAsProgress = type.countsAsProgress();
            } catch (IllegalArgumentException e) {
                // Unknown type → DEFAULT FALSE (konservativ!)
                this.countsAsProgress = false;
            }
        }
    }
}
```

---

### Frontend (Dropdown + Icon-Mapping)

```typescript
// activityTypesConfig.ts
export interface ActivityTypeConfig {
  value: string;
  label: string;
  icon: React.ReactNode;
  countsAsProgress: boolean;
  color: 'success' | 'default' | 'info';
}

export const ACTIVITY_TYPES: ActivityTypeConfig[] = [
  // ✅ Progress Activities
  {
    value: 'QUALIFIED_CALL',
    label: 'Qualifiziertes Telefonat',
    icon: <Phone />,
    countsAsProgress: true,
    color: 'success',
  },
  {
    value: 'MEETING',
    label: 'Persönliches Treffen',
    icon: <Groups />,
    countsAsProgress: true,
    color: 'success',
  },
  {
    value: 'DEMO',
    label: 'Produktdemo',
    icon: <Slideshow />,
    countsAsProgress: true,
    color: 'success',
  },
  {
    value: 'ROI_PRESENTATION',
    label: 'ROI-Präsentation',
    icon: <Analytics />,
    countsAsProgress: true,
    color: 'success',
  },
  {
    value: 'SAMPLE_SENT',
    label: 'Produktproben versendet',
    icon: <LocalShipping />,
    countsAsProgress: true,
    color: 'success',
  },

  // ❌ Non-Progress Activities
  {
    value: 'NOTE',
    label: 'Interne Notiz',
    icon: <Note />,
    countsAsProgress: false,
    color: 'default',
  },
  {
    value: 'FOLLOW_UP',
    label: 'Follow-Up geplant',
    icon: <Schedule />,
    countsAsProgress: false,
    color: 'default',
  },
  {
    value: 'EMAIL',
    label: 'Email versendet',
    icon: <Email />,
    countsAsProgress: false,
    color: 'default',
  },
  {
    value: 'CALL',
    label: 'Anruf',
    icon: <PhoneMissed />,
    countsAsProgress: false,
    color: 'default',
  },
  {
    value: 'SAMPLE_FEEDBACK',
    label: 'Sample-Feedback',
    icon: <Feedback />,
    countsAsProgress: false,
    color: 'default',
  },

  // ❌ System Activities (nicht im Dropdown - nur für Display)
  {
    value: 'FIRST_CONTACT_DOCUMENTED',
    label: 'Erstkontakt dokumentiert',
    icon: <ContactPage />,
    countsAsProgress: false,
    color: 'info',
  },
  {
    value: 'EMAIL_RECEIVED',
    label: 'Email erhalten',
    icon: <Inbox />,
    countsAsProgress: false,
    color: 'info',
  },
  {
    value: 'LEAD_ASSIGNED',
    label: 'Lead zugewiesen',
    icon: <Assignment />,
    countsAsProgress: false,
    color: 'info',
  },
];

export const getActivityTypeConfig = (type: string): ActivityTypeConfig | undefined => {
  return ACTIVITY_TYPES.find(t => t.value === type);
};

export const getUserSelectableActivityTypes = (): ActivityTypeConfig[] => {
  return ACTIVITY_TYPES.filter(t =>
    !['FIRST_CONTACT_DOCUMENTED', 'EMAIL_RECEIVED', 'LEAD_ASSIGNED'].includes(t.value)
  );
};
```

---

### Activity Timeline UI

```typescript
// ActivityTimeline.tsx
const ActivityTimeline = ({ activities }: { activities: LeadActivity[] }) => {
  return (
    <Timeline>
      {activities.map(activity => {
        const config = getActivityTypeConfig(activity.activityType);
        const isProgress = activity.countsAsProgress;

        return (
          <TimelineItem key={activity.id}>
            <TimelineSeparator>
              <TimelineDot color={isProgress ? 'success' : 'grey'}>
                {config?.icon}
              </TimelineDot>
              <TimelineConnector />
            </TimelineSeparator>
            <TimelineContent>
              <Typography variant="h6">
                {config?.label}
                {isProgress && <Chip size="small" label="Progress" color="success" sx={{ ml: 1 }} />}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                {formatDate(activity.activityDate)} • {activity.performedBy}
              </Typography>
              <Typography variant="body2">{activity.summary}</Typography>
              {activity.outcome && (
                <Typography variant="caption" color="primary">
                  Ergebnis: {activity.outcome}
                </Typography>
              )}
            </TimelineContent>
          </TimelineItem>
        );
      })}
    </Timeline>
  );
};
```

---

## Tests

### Unit Tests (Business Logic)

```java
@Test
void activityType_qualifiedCall_shouldCountAsProgress() {
    // Given
    LeadActivityType type = LeadActivityType.QUALIFIED_CALL;

    // Then
    assertThat(type.countsAsProgress()).isTrue();
    assertThat(type.getDisplayName()).isEqualTo("Qualifiziertes Telefonat");
}

@Test
void activityType_note_shouldNotCountAsProgress() {
    // Given
    LeadActivityType type = LeadActivityType.NOTE;

    // Then
    assertThat(type.countsAsProgress()).isFalse();
}

@Test
void leadActivity_setCountsAsProgressFromType_shouldAutoSet() {
    // Given
    LeadActivity activity = new LeadActivity();
    activity.setActivityType("QUALIFIED_CALL");

    // When
    activity.setCountsAsProgressFromType();

    // Then
    assertThat(activity.getCountsAsProgress()).isTrue();
}

@Test
void leadActivity_unknownType_shouldDefaultToFalse() {
    // Given
    LeadActivity activity = new LeadActivity();
    activity.setActivityType("UNKNOWN_TYPE");

    // When
    activity.setCountsAsProgressFromType();

    // Then
    assertThat(activity.getCountsAsProgress()).isFalse();  // Konservativ!
}
```

---

### Integration Tests (Trigger V257)

```java
@Test
@Tag("integration")
void trigger_progressActivity_shouldUpdateProgressDeadline() {
    // Given
    Lead lead = createLead("Test GmbH", "Hamburg");
    lead.setRegisteredAt(Instant.now());
    lead.setProgressDeadline(Instant.now().plus(Duration.ofDays(60)));
    leadRepository.persist(lead);

    Instant oldDeadline = lead.getProgressDeadline();

    // When: Insert QUALIFIED_CALL Activity (counts_as_progress = TRUE)
    LeadActivity activity = new LeadActivity();
    activity.setLeadId(lead.getId());
    activity.setActivityType("QUALIFIED_CALL");
    activity.setActivityDate(Instant.now());
    activity.setCountsAsProgress(true);
    activity.setSummary("Gespräch mit Einkaufsleiter");
    activityRepository.persist(activity);

    // Trigger fires automatically

    // Then: Progress deadline updated
    Lead updated = leadRepository.findById(lead.getId());
    assertThat(updated.getProgressDeadline()).isAfter(oldDeadline);
    assertThat(updated.getProgressDeadline()).isCloseTo(
        activity.getActivityDate().plus(Duration.ofDays(60)),
        within(1, ChronoUnit.SECONDS)
    );
}

@Test
@Tag("integration")
void trigger_nonProgressActivity_shouldNotUpdateProgressDeadline() {
    // Given
    Lead lead = createLead("Test GmbH", "Hamburg");
    lead.setRegisteredAt(Instant.now());
    lead.setProgressDeadline(Instant.now().plus(Duration.ofDays(60)));
    leadRepository.persist(lead);

    Instant oldDeadline = lead.getProgressDeadline();

    // When: Insert NOTE Activity (counts_as_progress = FALSE)
    LeadActivity activity = new LeadActivity();
    activity.setLeadId(lead.getId());
    activity.setActivityType("NOTE");
    activity.setActivityDate(Instant.now());
    activity.setCountsAsProgress(false);
    activity.setSummary("Interne Notiz");
    activityRepository.persist(activity);

    // Trigger DOES NOT fire (V257 checks counts_as_progress = TRUE)

    // Then: Progress deadline UNCHANGED
    Lead updated = leadRepository.findById(lead.getId());
    assertThat(updated.getProgressDeadline()).isEqualTo(oldDeadline);
}
```

---

## Audit & Metriken

### Audit-Events

```java
// Bei Progress-Activity:
auditService.log("lead_progress_activity", Map.of(
    "leadId", activity.getLeadId(),
    "activityType", activity.getActivityType(),
    "countsAsProgress", activity.getCountsAsProgress(),
    "newProgressDeadline", lead.getProgressDeadline()
));

// Bei Non-Progress-Activity:
auditService.log("lead_non_progress_activity", Map.of(
    "leadId", activity.getLeadId(),
    "activityType", activity.getActivityType(),
    "note", "Does not reset progress deadline"
));
```

### Metriken

```java
@Counted(name = "lead_activity_progress_total", description = "Anzahl Progress-Activities")
public void recordProgressActivity(String activityType) {
    // Auto-increment bei counts_as_progress = TRUE
}

@Counted(name = "lead_activity_non_progress_total", description = "Anzahl Non-Progress-Activities")
public void recordNonProgressActivity(String activityType) {
    // Auto-increment bei counts_as_progress = FALSE
}

@Gauge(name = "lead_activity_types", description = "Verteilung Activity-Types")
public Map<String, Long> getActivityTypeDistribution() {
    return activityRepository.findAll().stream()
        .collect(Collectors.groupingBy(
            LeadActivity::getActivityType,
            Collectors.counting()
        ));
}
```

---

## Änderungshistorie

| Datum | Änderung | Begründung |
|-------|----------|------------|
| 2025-10-01 | Initial: 5 Progress-Types, 5 Non-Progress-Types | ADR-004 Decision |
| 2025-10-03 | +3 System-Types (FIRST_CONTACT_DOCUMENTED, EMAIL_RECEIVED, LEAD_ASSIGNED) | Pre-Claim + Quick-Capture + Assignment Audit |

---

## Referenzen

- **Handelsvertretervertrag:** §2(8)(b) - 60-Tage-Aktivitätsstandard
- **V256:** `lead_activities.counts_as_progress` DEFAULT FALSE
- **V257:** Trigger `update_progress_on_activity()` - NUR bei counts_as_progress=TRUE
- **ADR-004:** Inline-First Architecture

---

**Letzte Aktualisierung:** 2025-10-03
**Autor:** Claude Code (Sprint 2.1.5 Activity Types Mapping)
**Status:** ✅ Production-Ready (Verbindliche Business Rules)
