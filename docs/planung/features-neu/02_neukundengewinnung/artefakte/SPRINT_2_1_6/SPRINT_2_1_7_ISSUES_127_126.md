# 📋 Sprint 2.1.7: Issue #127 & #126 Integration

**Erstellt am:** 2025-10-06 23:05
**Status:** Planned for Sprint 2.1.7 Track 2
**Owner:** team/leads-backend

---

## 🎯 Executive Summary

**Beide Issues sind verschoben auf Sprint 2.1.7 Track 2 (Test Infrastructure Overhaul)**

| Issue | Titel | Effort | Track | Priorität |
|-------|-------|--------|-------|-----------|
| **#127** | Clock Injection für deterministische Tests | 4-6h | Track 2 | 🟡 MITTEL |
| **#126** | ActivityOutcome als Enum + DB CHECK Constraint | 2h | Track 2 | 🔵 NIEDRIG |

**Gesamt-Effort:** ~6-8h (beide zusammen)

---

## 🔍 ISSUE #127: CLOCK INJECTION STANDARD

### **Status**
- ✅ **Teilweise implementiert:** 2/5 Services haben Clock (LeadMaintenanceService, FollowUpAutomationService)
- ❌ **Fehlend:** 3/5 Services (LeadProtectionService, LeadService, LeadConvertService)
- ✅ **Nicht kritisch:** Production + Tests funktionieren stabil

### **Warum Sprint 2.1.7?**
- ✅ Passt perfekt zu **Track 2: Test Infrastructure Overhaul**
- ✅ Großes Refactoring (4-6h) → nicht für Sprint 2.1.6 Phase 4 geeignet
- ✅ Separater PR (saubere Lösung)
- ✅ **Beispiel-Implementation vorhanden:** LeadMaintenanceService (Sprint 2.1.6 Phase 3)

### **Implementation Plan**

#### **1. ClockProvider erstellen (0.5h)**
```java
package de.freshplan.infrastructure.time;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import java.time.Clock;

@ApplicationScoped
public class ClockProvider {

    @Produces
    @ApplicationScoped
    public Clock provideClock() {
        return Clock.systemDefaultZone();
    }
}
```

---

#### **2. LeadProtectionService refactoren (1h)**

**Betroffene Methoden:**
- `getRemainingProtectionDays(Lead lead)` - Line 106
- `needsProgressWarning(Lead lead)` - Line 128
- `calculateProgress(Lead lead)` - Line 395

**VORHER:**
```java
@ApplicationScoped
public class LeadProtectionService {

    public int getRemainingProtectionDays(Lead lead) {
        LocalDateTime now = LocalDateTime.now(); // ❌ Non-deterministic
        // ...
    }
}
```

**NACHHER:**
```java
@ApplicationScoped
public class LeadProtectionService {

    @Inject Clock clock; // ✅ Injected

    public int getRemainingProtectionDays(Lead lead) {
        LocalDateTime now = LocalDateTime.now(clock); // ✅ Deterministic
        // ...
    }
}
```

**Tests:**
```java
@ExtendWith(MockitoExtension.class)
class LeadProtectionServiceTest {

    @Mock private Clock clock;
    @Mock private UserLeadSettingsService settingsService;
    @InjectMocks private LeadProtectionService protectionService;

    @Test
    void shouldDetectProgressWarningAt53Days() {
        // Given - Fixed time: 2025-01-15
        LocalDateTime fixedTime = LocalDateTime.of(2025, 1, 15, 10, 0);
        when(clock.instant()).thenReturn(fixedTime.atZone(ZoneId.systemDefault()).toInstant());
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        Lead lead = new Lead();
        lead.progressDeadline = fixedTime.plusDays(5);

        // When
        boolean needsWarning = protectionService.needsProgressWarning(lead);

        // Then
        assertThat(needsWarning).isTrue();
    }
}
```

---

#### **3. LeadService refactoren (2h)**

**Betroffene Stellen:** 11 Stellen mit `LocalDateTime.now()`
- Line 36: `LocalDateTime.now().minusDays(60)`
- Line 51: `lead.reminderSentAt = LocalDateTime.now()`
- Line 75: `LocalDateTime.now().minusDays(10)`
- Line 89: `lead.gracePeriodStartAt = LocalDateTime.now()`
- Line 110: `LocalDateTime.now().minusMonths(6)`
- Line 121: `lead.expiredAt = LocalDateTime.now()`
- Line 132: `LocalDateTime.now().minusDays(10)`
- Line 143: `lead.expiredAt = LocalDateTime.now()`
- Line 163: `lead.lastActivityAt = LocalDateTime.now()`
- Line 177: `LocalDateTime.now().plusDays(daysBeforeExpiry)`
- Line 178: `LocalDateTime.now().plusMonths(6)`

**Refactoring:**
```java
@ApplicationScoped
public class LeadService {

    @Inject Clock clock; // ✅ NEU

    public List<Lead> getLeadsNeedingReminder() {
        LocalDateTime cutoffDate = LocalDateTime.now(clock).minusDays(60); // ✅
        // ...
    }
}
```

---

#### **4. LeadConvertService refactoren (1h)**

**Betroffene Stellen:** 8 Stellen (alle für Timestamps)
- Line 79: `customer.setCreatedAt(LocalDateTime.now())`
- Line 81: `customer.setUpdatedAt(LocalDateTime.now())`
- Line 100: `mainLocation.setCreatedAt(LocalDateTime.now())`
- Line 102: `mainLocation.setUpdatedAt(LocalDateTime.now())`
- Line 116: `address.setCreatedAt(LocalDateTime.now())`
- Line 118: `address.setUpdatedAt(LocalDateTime.now())`
- Line 151: `contact.setCreatedAt(LocalDateTime.now())`
- Line 153: `contact.setUpdatedAt(LocalDateTime.now())`

**Refactoring:**
```java
@ApplicationScoped
public class LeadConvertService {

    @Inject Clock clock; // ✅ NEU

    public Customer convertLead(Long leadId, String userId) {
        LocalDateTime now = LocalDateTime.now(clock); // ✅ Einmal

        customer.setCreatedAt(now);
        customer.setUpdatedAt(now);
        // ...
    }
}
```

---

#### **5. ADR erstellen (0.5h)**

**ADR-007: Clock Injection Standard**
```markdown
# ADR-007: Clock Injection Standard

**Status:** Accepted
**Date:** 2025-10-19 (Sprint 2.1.7)
**Context:** Deterministische Zeit-Tests für alle Services

**Decision:**
Alle Services nutzen `@Inject Clock` statt `LocalDateTime.now()`.

**Consequences:**
- ✅ 100% deterministische Tests
- ✅ Konsistenz über alle Services
- ✅ Testbare Zeit-Logik

**Implementation:**
- ClockProvider (@Produces Clock Bean)
- Production: Clock.systemDefaultZone()
- Tests: Clock.fixed()

**Beispiel:** LeadMaintenanceService.java (Sprint 2.1.6 Phase 3)
```

---

### **Akzeptanzkriterien**
- [ ] ClockProvider als CDI Bean erstellt
- [ ] LeadProtectionService nutzt injected Clock (3 Methoden)
- [ ] LeadService nutzt injected Clock (11 Stellen)
- [ ] LeadConvertService nutzt injected Clock (8 Stellen)
- [ ] Alle Tests auf Clock.fixed() umgestellt
- [ ] ADR-007 erstellt
- [ ] Alle Tests grün (Unit + Integration)
- [ ] 100% Konsistenz zwischen allen Services

---

## 🔍 ISSUE #126: ACTIVITYOUTCOME ENUM + VALIDATION

### **Status**
- ❌ **Nicht implementiert:** outcome ist String ohne Validierung
- ✅ **Field wird nicht verwendet:** 0 read, 0 write (kein Production-Impact)
- 🎯 **Sprint 2.1.7 braucht Enum:** Follow-Up-Dashboard nutzt outcome

### **Warum Sprint 2.1.7?**
- ✅ **Field aktuell ungenutzt** (kein Druck)
- ✅ **Follow-Up-Dashboard** (Sprint 2.1.7) nutzt outcome → Enum sollte davor da sein
- ✅ **Saubere Lösung:** Enum + Feature zusammen implementieren
- ✅ **Kleiner Aufwand:** 2h (passt gut zu Track 2)

### **Implementation Plan**

#### **1. Enum erstellen (0.5h)**
```java
package de.freshplan.modules.leads.domain;

/**
 * Activity Outcome Enum (Sprint 2.1.7)
 *
 * <p>Ergebnis einer Lead-Aktivität (Follow-Up-Dashboard).
 *
 * <p>Definiert in V256 Migration Comment.
 */
public enum ActivityOutcome {
    /** Positives Interesse (Lead ist engagiert) */
    POSITIVE_INTEREST,

    /** Weitere Informationen nötig (Lead braucht mehr Details) */
    NEEDS_MORE_INFO,

    /** Nicht interessiert (Lead hat abgelehnt) */
    NOT_INTERESTED,

    /** Callback vereinbart (Follow-Up geplant) */
    CALLBACK_SCHEDULED,

    /** Demo vereinbart (nächste Stufe) */
    DEMO_SCHEDULED,

    /** Closed Won (Lead wurde Customer) */
    CLOSED_WON,

    /** Closed Lost (Lead verloren) */
    CLOSED_LOST
}
```

---

#### **2. Entity anpassen (0.5h)**

**VORHER:**
```java
@Entity
@Table(name = "lead_activities")
public class LeadActivity extends PanacheEntityBase {

    @Size(max = 50)
    @Column(name = "outcome")
    public String outcome; // ❌ String
}
```

**NACHHER:**
```java
@Entity
@Table(name = "lead_activities")
public class LeadActivity extends PanacheEntityBase {

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", length = 50)
    public ActivityOutcome outcome; // ✅ Enum
}
```

---

#### **3. Migration erstellen (0.5h)**

**V269__add_activity_outcome_constraint.sql**
```sql
-- Migration V269: Add outcome CHECK constraint (Sprint 2.1.7)
-- Validates ActivityOutcome Enum values at DB-Level
-- Related: Issue #126

ALTER TABLE lead_activities
  ADD CONSTRAINT lead_activities_outcome_chk
  CHECK (outcome IN (
    'POSITIVE_INTEREST',
    'NEEDS_MORE_INFO',
    'NOT_INTERESTED',
    'CALLBACK_SCHEDULED',
    'DEMO_SCHEDULED',
    'CLOSED_WON',
    'CLOSED_LOST'
  ) OR outcome IS NULL);

COMMENT ON CONSTRAINT lead_activities_outcome_chk
  ON lead_activities IS
  'Sprint 2.1.7: Validates ActivityOutcome Enum values. See V256 for original field definition.';
```

---

#### **4. Tests schreiben (0.5h)**

```java
@QuarkusTest
class ActivityOutcomeValidationIT {

    @Test
    @DisplayName("Should accept valid outcome")
    void shouldAcceptValidOutcome() {
        LeadActivity activity = new LeadActivity();
        activity.outcome = ActivityOutcome.POSITIVE_INTEREST; // ✅ Valid
        activity.persist();

        assertThat(activity.id).isNotNull();
    }

    @Test
    @DisplayName("Should reject invalid outcome at DB level")
    void shouldRejectInvalidOutcomeAtDBLevel() {
        // Bypass Entity validation by using native SQL
        assertThatThrownBy(() -> {
            em.createNativeQuery(
                "INSERT INTO lead_activities (outcome) VALUES ('invalid_outcome')"
            ).executeUpdate();
        }).isInstanceOf(PersistenceException.class)
          .hasMessageContaining("lead_activities_outcome_chk");
    }

    @Test
    @DisplayName("Should allow NULL outcome")
    void shouldAllowNullOutcome() {
        LeadActivity activity = new LeadActivity();
        activity.outcome = null; // ✅ NULL is allowed
        activity.persist();

        assertThat(activity.id).isNotNull();
    }

    @Test
    @DisplayName("Should serialize enum to JSON correctly")
    void shouldSerializeEnumToJSON() {
        LeadActivity activity = new LeadActivity();
        activity.outcome = ActivityOutcome.DEMO_SCHEDULED;

        String json = JsonbBuilder.create().toJson(activity);

        assertThat(json).contains("\"outcome\":\"DEMO_SCHEDULED\"");
    }
}
```

---

### **Akzeptanzkriterien**
- [ ] Enum ActivityOutcome erstellt (7 Werte)
- [ ] LeadActivity.outcome nutzt Enum (@Enumerated)
- [ ] Migration V269 mit CHECK Constraint erstellt
- [ ] Tests für ungültige Outcomes (DB-Level)
- [ ] Tests für NULL-Werte
- [ ] Tests für JSON Serialization
- [ ] Alle Tests grün

---

## 📊 ZUSAMMENFASSUNG

### **Sprint 2.1.7 Track 2 Integration**

| User Story | Issue | Effort | Deliverables |
|------------|-------|--------|--------------|
| **5. Clock Injection Standard** | #127 | 4-6h | ClockProvider + 3 Services refactored + ADR-007 |
| **6. ActivityOutcome Enum** | #126 | 2h | Enum + Migration V269 + Tests |
| **7. CRM Szenario-Builder** | - | 12-16h | ScenarioBuilder + Lead-Journey Fixtures |
| **8. Lead-Journey Fixtures** | - | 6-8h | TestDataFactories + Fixtures |
| **9. Faker-Integration** | - | 4-6h | RealisticDataGenerator |
| **10. Test-Pattern Library** | - | 4-6h | TESTING_PATTERNS.md + Examples |

**Track 2 Gesamt-Effort:** ~32-44h (4-5.5 Tage)

---

### **Dokumentation erstellt**

✅ **GitHub Issues kommentiert:**
- [Issue #127 Comment](https://github.com/joergstreeck/freshplan-sales-tool/issues/127#issuecomment-3374271637)
- [Issue #126 Comment](https://github.com/joergstreeck/freshplan-sales-tool/issues/126#issuecomment-3374273530)

✅ **Sprint-Planung aktualisiert:**
- [TRIGGER_SPRINT_2_1_7.md](TRIGGER_SPRINT_2_1_7.md) - User Stories 5 & 6 hinzugefügt
- Track 2 Kern-Deliverables erweitert (6 Deliverables)
- Gesamt-Effort aktualisiert (~32-44h)

✅ **Analyse-Dokumente:**
- [ISSUE_127_126_DEEP_ANALYSIS.md](ISSUE_127_126_DEEP_ANALYSIS.md) - Detaillierte Code-Analyse
- [TECHNICAL_DEBT_ANALYSIS.md](TECHNICAL_DEBT_ANALYSIS.md) - Vollständige Debt-Übersicht

---

## ✅ NÄCHSTE SCHRITTE

### **Für Sprint 2.1.6 Abschluss:**
- ✅ **Beide Issues NICHT BLOCKIEREND**
- ✅ **Sprint 2.1.6 Phase 3 kann abgeschlossen werden**
- 📋 Fokus auf PR #134 (Outbox-Pattern + Issue #134)

### **Für Sprint 2.1.7 Start (19.10.2025):**
1. **Track 2 User Stories 5 & 6 implementieren** (6-8h)
2. **ClockProvider + 3 Services refactoren**
3. **ActivityOutcome Enum + Migration V269**
4. **ADR-007 erstellen**
5. **Dann weiter mit CRM Szenario-Builder** (User Story 7)

---

**Status:** ✅ **DOKUMENTIERT & GEPLANT**
**Timeline:** Sprint 2.1.7 (Start 19.10.2025)
**Owner:** team/leads-backend
