---
module: "02_neukundengewinnung"
domain: "shared"
doc_type: "technical_concept"
status: "ready_for_implementation"
sprint: "2.1.6"
phase: "4"
owner: "team/leads-backend"
created: "2025-10-07"
updated: "2025-10-07"
---

# Sprint 2.1.6 Phase 4 - Job Monitoring & Performance Optimization

**📍 Navigation:** Home → Planung → 02 Neukundengewinnung → Artefakte → Sprint 2.1.6 → Phase 4

## 🎯 Executive Summary

**Mission:** Performance-Optimierung und Monitoring-Grundlagen für Nightly Jobs

**Context:** Phase 3 Gap Analysis identifizierte 2 implementierbare Verbesserungen + 2 deferierte Features für Modul 00/05.

**Timeline:** 0.5 Tage (1.5h Implementation)
**Impact:** 98% COMPLETE Status für Phase 3, Vorbereitung für Prometheus-Integration

---

## 📋 Scope Definition

### ✅ IN SCOPE (Phase 4 - ~1.5h)

#### **1. Batch-Processing LIMIT 100** (~30min)

**Problem:** Aktuell verarbeiten Jobs unbegrenzt viele Leads pro Run.

**Risk:** Bei >1000 Leads kann es zu Memory-Problemen kommen.

**Solution:** Pagination mit `LIMIT 100` pro Batch.

**Implementation:**
```java
// VORHER (LeadMaintenanceService.java)
List<Lead> leads = Lead.find(
    "progressDeadline < ?1 AND progressWarningSentAt IS NULL AND status = ?2",
    threshold, LeadStatus.ACTIVE
).list();

// NACHHER
private static final int BATCH_SIZE = 100;

List<Lead> leads = Lead.find(
    "progressDeadline < ?1 AND progressWarningSentAt IS NULL AND status = ?2",
    threshold, LeadStatus.ACTIVE
).page(0, BATCH_SIZE).list(); // ✅ Max 100 Leads/Run
```

**Affected Jobs:**
- Job 1: Progress Warning Check
- Job 2: Protection Expiry Check
- Job 3: DSGVO Pseudonymization
- Job 4: Import Jobs Archival

**Tests:** Bestehende Integration-Tests bleiben grün (kleine Testdaten-Mengen).

---

#### **2. Basic Job Metrics (Structured Logging)** (~1h)

**Problem:** Aktuell nur einfache LOG-Statements, keine strukturierten Metriken.

**Solution:** Structured Logging mit Job-Statistiken (Vorbereitung für Prometheus).

**Implementation:**
```java
// LeadMaintenanceService.java

public int checkProgressWarnings() {
    long startTime = System.currentTimeMillis();
    int processedCount = 0;
    int emailsSent = 0;

    try {
        // ... existing logic ...
        processedCount = leads.size();
        emailsSent = outboxEmails.size();

        return processedCount;
    } finally {
        long duration = System.currentTimeMillis() - startTime;
        logJobMetrics("progress_warning_check", processedCount, emailsSent, duration);
    }
}

private void logJobMetrics(String jobName, int processed, int actions, long durationMs) {
    LOG.info(
        "Job Metrics: {} | Processed: {} | Actions: {} | Duration: {}ms",
        jobName, processed, actions, durationMs
    );
}
```

**Metrics Logged:**
- Job Name (z.B. `progress_warning_check`)
- Processed Count (z.B. `15 leads`)
- Actions Taken (z.B. `15 emails queued`)
- Duration (z.B. `234ms`)

**Output Example:**
```
[INFO] Job Metrics: progress_warning_check | Processed: 15 | Actions: 15 | Duration: 234ms
[INFO] Job Metrics: protection_expiry_check | Processed: 5 | Actions: 5 | Duration: 123ms
[INFO] Job Metrics: dsgvo_pseudonymization | Processed: 2 | Actions: 2 | Duration: 67ms
[INFO] Job Metrics: import_jobs_archival | Processed: 10 | Actions: 10 | Duration: 45ms
```

**Benefit:**
- ✅ Einfaches Parsing für Log-Aggregation (Splunk, ELK)
- ✅ Vorbereitung für Prometheus (gleiche Metric-Namen)
- ✅ Debugging bei Performance-Problemen

---

### ❌ OUT OF SCOPE (Deferred)

#### **3. Prometheus-Metrics** → **Modul 00 (Betrieb)**

**Warum deferred:**
- Braucht `quarkus-micrometer-registry-prometheus` Dependency
- Braucht Prometheus-Endpoint-Config (`/metrics`)
- Braucht Prometheus-Server-Setup (Infrastruktur)
- Besser mit allen anderen App-Metrics zusammen implementieren

**Geplant für:** Modul 00 (Betrieb) - Q1 2026

**Metrics (später):**
```java
@Inject MeterRegistry registry;

Counter jobCounter = registry.counter("job.executions", "job", "progress_warning");
Timer jobTimer = registry.timer("job.duration", "job", "progress_warning");
```

---

#### **4. Slack-Alerting** → **Modul 05 (Kommunikation)**

**Warum deferred:**
- Braucht Slack-Webhook-Integration (HTTP-Client)
- Braucht Config-Management (Webhook-URLs, Channel-IDs)
- Braucht Error-Handling + Retry-Logik
- Besser mit Email-System zusammen implementieren (gleicher Alert-Mechanismus)

**Geplant für:** Modul 05 (Kommunikation) - Q1 2026

**Implementation (später):**
```java
@Inject SlackAlertService slackAlertService;

if (failureCount > 10) {
    slackAlertService.sendAlert(
        "🚨 Job Failures",
        "Progress Warning Check failed 10+ times"
    );
}
```

---

## 🔄 Implementation Plan

### **Step 1: Batch-Processing LIMIT 100** (30min)

#### **1.1 LeadMaintenanceService.java** (4 Änderungen)

```java
public class LeadMaintenanceService {

    private static final int BATCH_SIZE = 100; // ✅ NEU

    public int checkProgressWarnings() {
        // ...
        List<Lead> leads = Lead.find(
            "progressDeadline < ?1 AND progressWarningSentAt IS NULL AND status = ?2",
            threshold, LeadStatus.ACTIVE
        ).page(0, BATCH_SIZE).list(); // ✅ ÄNDERUNG
        // ...
    }

    public int checkProtectionExpiry() {
        // ...
        List<Lead> leads = Lead.find(
            "progressWarningSentAt IS NOT NULL AND ...",
            gracePeriodThreshold, LeadStatus.ACTIVE
        ).page(0, BATCH_SIZE).list(); // ✅ ÄNDERUNG
        // ...
    }

    public int pseudonymizeLeads() {
        // ...
        List<Lead> leads = Lead.find(
            "status = ?1 AND ownerUserId IS NULL AND ...",
            LeadStatus.EXPIRED, pseudonymizationThreshold
        ).page(0, BATCH_SIZE).list(); // ✅ ÄNDERUNG
        // ...
    }

    public int archiveImportJobs() {
        // ...
        List<ImportJob> jobs = ImportJob.find(
            "status = ?1 AND ttlExpiresAt < ?2",
            ImportStatus.COMPLETED, now
        ).page(0, BATCH_SIZE).list(); // ✅ ÄNDERUNG
        // ...
    }
}
```

#### **1.2 Tests:** Keine Änderung nötig (Testdaten < 100)

---

### **Step 2: Structured Job Logging** (1h)

#### **2.1 LeadMaintenanceService.java** (Metric-Logging)

```java
public class LeadMaintenanceService {

    public int checkProgressWarnings() {
        long startTime = System.currentTimeMillis();
        int processedCount = 0;
        int emailsSent = 0;

        try {
            // ... existing logic ...
            processedCount = leads.size();
            emailsSent = leads.size(); // 1 Email pro Lead

            return processedCount;
        } finally {
            logJobMetrics("progress_warning_check", processedCount, emailsSent,
                System.currentTimeMillis() - startTime);
        }
    }

    public int checkProtectionExpiry() {
        long startTime = System.currentTimeMillis();
        int processedCount = 0;
        int statusChanges = 0;

        try {
            // ... existing logic ...
            processedCount = leads.size();
            statusChanges = leads.size(); // ACTIVE → EXPIRED

            return processedCount;
        } finally {
            logJobMetrics("protection_expiry_check", processedCount, statusChanges,
                System.currentTimeMillis() - startTime);
        }
    }

    public int pseudonymizeLeads() {
        long startTime = System.currentTimeMillis();
        int processedCount = 0;

        try {
            // ... existing logic ...
            processedCount = leads.size();

            return processedCount;
        } finally {
            logJobMetrics("dsgvo_pseudonymization", processedCount, processedCount,
                System.currentTimeMillis() - startTime);
        }
    }

    public int archiveImportJobs() {
        long startTime = System.currentTimeMillis();
        int processedCount = 0;

        try {
            // ... existing logic ...
            processedCount = jobs.size();

            return processedCount;
        } finally {
            logJobMetrics("import_jobs_archival", processedCount, processedCount,
                System.currentTimeMillis() - startTime);
        }
    }

    private void logJobMetrics(String jobName, int processed, int actions, long durationMs) {
        LOG.info(
            "Job Metrics: {} | Processed: {} | Actions: {} | Duration: {}ms",
            jobName, processed, actions, durationMs
        );
    }
}
```

#### **2.2 LeadMaintenanceScheduler.java** (Enhanced Logging)

```java
@Scheduled(cron = "0 0 2 * * ?") // 02:00 UTC
void checkProgressWarnings() {
    LOG.info("Starting scheduled Progress Warning Check");
    long startTime = System.currentTimeMillis();

    try {
        int count = maintenanceService.checkProgressWarnings();
        LOG.info("Scheduled Progress Warning Check completed: {} warnings issued", count);
    } catch (Exception e) {
        LOG.error("Scheduled Progress Warning Check failed", e);
        // TODO: Increment failure counter (Phase 4 - später für Slack-Alerting)
    } finally {
        long duration = System.currentTimeMillis() - startTime;
        LOG.info("Job Duration: progress_warning_check_scheduler | {}ms", duration);
    }
}
```

---

## ✅ Acceptance Criteria

### **Functional Requirements:**
- [ ] Alle 4 Jobs nutzen `BATCH_SIZE = 100` (LIMIT)
- [ ] Structured Metrics-Logging in allen Jobs
- [ ] Log-Format: `Job Metrics: {name} | Processed: {n} | Actions: {n} | Duration: {ms}ms`
- [ ] Scheduler-Logging mit Start/End/Duration

### **Non-Functional Requirements:**
- [ ] Keine Breaking Changes (bestehende Tests bleiben grün)
- [ ] Keine neuen Dependencies
- [ ] Log-Level: INFO (kein DEBUG/TRACE-Spam)
- [ ] Performance-Impact < 5ms (Logging-Overhead)

### **Quality Gates:**
- [ ] Spotless: Code formatiert
- [ ] Tests: Alle Tests grün
- [ ] Logs: Structured Metrics in allen Jobs
- [ ] Documentation: PHASE_4_COMPLETE.md erstellt

---

## 📊 Expected Metrics Output

### **Normal Operation (Logs):**
```
[INFO] 2025-10-07 02:00:00 Starting scheduled Progress Warning Check
[INFO] 2025-10-07 02:00:00 Job Metrics: progress_warning_check | Processed: 15 | Actions: 15 | Duration: 234ms
[INFO] 2025-10-07 02:00:00 Scheduled Progress Warning Check completed: 15 warnings issued
[INFO] 2025-10-07 02:00:00 Job Duration: progress_warning_check_scheduler | 245ms

[INFO] 2025-10-07 02:00:01 Starting scheduled Protection Expiry Check
[INFO] 2025-10-07 02:00:01 Job Metrics: protection_expiry_check | Processed: 5 | Actions: 5 | Duration: 123ms
[INFO] 2025-10-07 02:00:01 Scheduled Protection Expiry Check completed: 5 leads expired
[INFO] 2025-10-07 02:00:01 Job Duration: protection_expiry_check_scheduler | 134ms

[INFO] 2025-10-07 03:00:00 Starting scheduled DSGVO Pseudonymization
[INFO] 2025-10-07 03:00:00 Job Metrics: dsgvo_pseudonymization | Processed: 2 | Actions: 2 | Duration: 67ms
[INFO] 2025-10-07 03:00:00 Scheduled DSGVO Pseudonymization completed: 2 leads pseudonymized
[INFO] 2025-10-07 03:00:00 Job Duration: dsgvo_pseudonymization_scheduler | 78ms

[INFO] 2025-10-07 04:00:00 Starting scheduled Import Jobs Archival
[INFO] 2025-10-07 04:00:00 Job Metrics: import_jobs_archival | Processed: 10 | Actions: 10 | Duration: 45ms
[INFO] 2025-10-07 04:00:00 Scheduled Import Jobs Archival completed: 10 jobs archived
[INFO] 2025-10-07 04:00:00 Job Duration: import_jobs_archival_scheduler | 56ms
```

### **Future Prometheus-Integration (Modul 00):**
```
# HELP job_executions_total Total number of job executions
# TYPE job_executions_total counter
job_executions_total{job="progress_warning_check"} 365
job_executions_total{job="protection_expiry_check"} 365

# HELP job_duration_seconds Job execution duration
# TYPE job_duration_seconds histogram
job_duration_seconds_bucket{job="progress_warning_check",le="0.1"} 300
job_duration_seconds_bucket{job="progress_warning_check",le="0.5"} 365
job_duration_seconds_bucket{job="progress_warning_check",le="1.0"} 365
```

---

## 🔗 Related Documents

### **Phase 3 Artifacts:**
- [AUTOMATED_JOBS_SPECIFICATION.md](AUTOMATED_JOBS_SPECIFICATION.md) - Original Spec
- [PHASE_3_GAP_ANALYSIS.md](../../../../backend/src/test/java/de/freshplan/modules/leads/service/PHASE_3_GAP_ANALYSIS.md) - Gap Analysis (identifizierte Phase 4)

### **Master Planning:**
- [TRIGGER_SPRINT_2_1_6.md](../../../TRIGGER_SPRINT_2_1_6.md) - Sprint 2.1.6 Trigger
- [CRM_COMPLETE_MASTER_PLAN_V5.md](../../../CRM_COMPLETE_MASTER_PLAN_V5.md) - Master Plan V5

### **Future Work:**
- **Modul 00 (Betrieb):** Prometheus-Metrics, Grafana-Dashboards
- **Modul 05 (Kommunikation):** Slack-Alerting, Email-Alerting

---

## 📋 Implementation Checklist

### **Pre-Implementation:**
- [ ] Phase 3 PR #134 merged
- [ ] Branch: `feature/mod02-sprint-2.1.6-phase-4` erstellt

### **Implementation:**
- [ ] `BATCH_SIZE = 100` in LeadMaintenanceService.java
- [ ] 4x `.page(0, BATCH_SIZE).list()` Änderungen
- [ ] `logJobMetrics()` Methode hinzufügen
- [ ] Job-Metrics in allen 4 Jobs
- [ ] Scheduler-Logging erweitern
- [ ] Spotless ausführen

### **Testing:**
- [ ] Integration-Tests grün (7 Tests)
- [ ] Manual Test: Logs prüfen
- [ ] Performance-Check: <5ms Overhead

### **Documentation:**
- [ ] PHASE_4_COMPLETE.md erstellen
- [ ] PHASE_3_GAP_ANALYSIS.md aktualisieren (Phase 4 Done)
- [ ] Master Plan V5 aktualisieren (98% COMPLETE)

### **Git:**
- [ ] Commit: "feat(leads): Sprint 2.1.6 Phase 4 - Job Monitoring & Performance"
- [ ] PR erstellen (nach User-Freigabe)

---

## ✅ Success Criteria

**Phase 4 ist COMPLETE wenn:**
- ✅ Alle Jobs nutzen Batch-Processing (LIMIT 100)
- ✅ Structured Metrics-Logging in allen Jobs
- ✅ Logs sind parsebar (JSON-ready Format)
- ✅ Alle Tests grün
- ✅ Dokumentation aktualisiert

**Phase 3 + 4 Combined Status: 98% COMPLETE**
- ✅ 4/4 Jobs implementiert
- ✅ Batch-Processing implementiert
- ✅ Structured Logging implementiert
- ⏸️ Prometheus-Metrics (Modul 00)
- ⏸️ Slack-Alerting (Modul 05)

---

**Status:** ✅ **READY FOR IMPLEMENTATION**
**Owner:** team/leads-backend
**Timeline:** 0.5 Tage (1.5h)
