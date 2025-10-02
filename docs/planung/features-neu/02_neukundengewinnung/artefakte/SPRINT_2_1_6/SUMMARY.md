---
module: "02_neukundengewinnung"
domain: "shared"
doc_type: "konzept"
status: "planned"
sprint: "2.1.6"
owner: "team/leads-backend"
updated: "2025-10-02"
---

# Sprint 2.1.6 – Artefakte Summary

**📍 Navigation:** Home → Planung → 02 Neukundengewinnung → Artefakte → Sprint 2.1.6

## Übersicht

Sprint 2.1.6 implementiert **Lead-Transfer zwischen Partnern**, **Bestandsleads-Migration**, **Lead → Kunde Convert Flow**, **Stop-the-Clock UI** und **erweiterte Fuzzy-Matching**-Funktionalität.

## Sprint-Status

**Status:** 📅 PLANNED (12-18.10.2025)
**Abhängigkeiten:** Sprint 2.1.5 (Backend Phase 1 COMPLETE, Frontend Phase 2 muss abgeschlossen sein)

## Features & Artefakte

### 1. Bestandsleads-Migrations-API
**Verschoben aus 2.1.5, NEU in Modul 08 Administration**

📄 **[BESTANDSLEADS_MIGRATION_API_SPEC.md](./BESTANDSLEADS_MIGRATION_API_SPEC.md)**

#### API-Endpoint
- `POST /api/admin/migration/leads/import` (Admin-only, Modul 08)
- **Dry-Run Mode PFLICHT** (dryRun=true vor echtem Import)
- Batch-Import mit Validierung (max. 1000 Leads/Batch)
- Historische Datumsfelder korrekt übernehmen (registeredAt, activities)
- `countsAsProgress` explizit setzen (NICHT automatisch berechnen!)

#### Business-Regeln
- Duplikaten-Check + Warning-Report
- Audit-Log für alle Import-Vorgänge (importId, source, user, timestamp)
- Re-Import-Fähigkeit bei Fehlern (nur fehlerhafte Datensätze)
- Batch-Transactional (ganzer Batch oder nichts)

---

### 2. Lead → Kunde Convert Flow
**Verschoben aus 2.1.5**

📄 **[LEAD_TO_CUSTOMER_CONVERT_FLOW.md](./LEAD_TO_CUSTOMER_CONVERT_FLOW.md)**

#### Automatischer Trigger
- Bei Status QUALIFIED → CONVERTED
- `POST /api/leads/{id}/convert-to-customer` (Dedizierter Endpoint)
- Alle Lead-Daten werden 1:1 übernommen (**ZERO Doppeleingabe**)

#### Daten-Mapping
- Company-Daten → Customer (companyName, city, territory, etc.)
- Contact-Daten → CustomerContact (mit isPrimary=true)
- Business-Daten → Customer (estimatedVolume, vatId, notes)
- Ownership → Customer (ownerUserId, ownerTeamId)
- Lead-Verknüpfung: `customer.original_lead_id`

---

### 3. Stop-the-Clock UI
**Backend-only in 2.1.5, UI jetzt in 2.1.6**

📄 **[STOP_THE_CLOCK_RBAC_RULES.md](../SPRINT_2_1_5/STOP_THE_CLOCK_RBAC_RULES.md)** (Backend-Regeln bereits definiert)

#### Frontend Components
- `StopTheClockDialog.tsx` - Pause/Resume Dialog (Manager + Admin only)
- `LeadProtectionBadge.tsx` - Erweitert um Pause/Resume Buttons

---

## Definition of Done (Sprint)

- [ ] **V258 lead_transfers Migration deployed**
- [ ] **Transfer-Request-Flow End-to-End funktioniert**
- [ ] **Bestandsleads-Migrations-API funktioniert (Dry-Run + Production)**
- [ ] **Lead → Kunde Convert Flow End-to-End funktioniert**
- [ ] **Stop-the-Clock UI funktioniert (Manager-only, RBAC enforced)**
- [ ] **Fuzzy-Matching mit konfigurierbaren Schwellen**
- [ ] **Nightly Jobs deployed (Warning, Expiry, Pseudonymisierung)**
- [ ] **Backdating Endpoint funktioniert mit Audit-Log**
- [ ] **Integration Tests grün**
- [ ] **Dokumentation: SUMMARY, CHANGELOG, QA_CHECKLIST**

---

## Links

- [BESTANDSLEADS_MIGRATION_API_SPEC.md](./BESTANDSLEADS_MIGRATION_API_SPEC.md)
- [LEAD_TO_CUSTOMER_CONVERT_FLOW.md](./LEAD_TO_CUSTOMER_CONVERT_FLOW.md)
- [STOP_THE_CLOCK_RBAC_RULES.md](../SPRINT_2_1_5/STOP_THE_CLOCK_RBAC_RULES.md)
- [TRIGGER_SPRINT_2_1_6.md](../../../TRIGGER_SPRINT_2_1_6.md)
- [SPRINT_MAP.md](../../SPRINT_MAP.md)

---

**Dokument-Owner:** Jörg Streeck + Claude Code
**Letzte Änderung:** 2025-10-02
**Version:** 1.0 (Planned for Sprint 2.1.6)
