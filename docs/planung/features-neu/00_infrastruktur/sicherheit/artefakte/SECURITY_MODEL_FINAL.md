---
Title: Security Model (ABAC/RLS Final)
Purpose: Einheitliche, verlässliche Quelle für Zugriffskontrolle im FreshPlan CRM.
Audience: Entwickler, Security, Compliance
Last Updated: 2025-09-20
Status: Final
---

# 🔐 Security Model (ABAC/RLS Final)

## 30-Second Summary
- Zugriff basiert auf Lead-Ownership + Zeitfenstern, nicht auf Geografie.
- Schutzlogik: 6 Monate Basis + 60 Tage Aktivität + 10 Tage Nachfrist; Stop-the-Clock via Holds.
- Durchsetzung in der DB via RLS + View v_lead_protection; Applikation prüft zusätzlich ABAC-Guards.
- Kollaboration über Collaborator-Rollen; kritische Aktionen bleiben Owner-gebunden.

## Core Reference

### Ownership-Regeln
- Ein Lead hat genau einen aktuellen Owner (lead_assignment.is_current=true).
- Collaborators: VIEW, ASSIST, NEGOTIATE – keine kritischen Aktionen (Credit/Order) ohne Owner.

### Schutzfenster
- **Basisdauer:** 6 Monate ab Assignment.
- **Progress:** jede belegbare Aktivität verschiebt 60 Tage.
- **Grace:** 10 Tage Nachfrist nach Progress-Fenster.
- **Holds:** FreshFoodz-bedingte Verzögerungen pausieren die Uhr.

### Belegbare Aktivitäten
- QUALIFIED_CALL, CUSTOMER_REACTION, SCHEDULED_FOLLOWUP, SAMPLE_FEEDBACK, ROI_PRESENTATION.

### RLS Prinzip
- Lesen/Schreiben erlaubt, wenn Owner aktiv (ACTIVE/GRACE) oder Collaborator (lesen / eingeschränkt schreiben).
- Auditor/Manager dürfen lesen.
- Policies: fail-closed.

## Implementation Examples

### RLS/ABAC Queries (Auszug)
- **View:** v_lead_protection berechnet status, reminder_due_at, grace_until, valid_until.
- **RLS Lesen (vereinfacht):** Zugriff erlaubt, wenn p.status in (ACTIVE, GRACE) und p.owner_user_id = current_user.
- **Write-Guard (Service):** leadProtectionService.ensureOwnerActive(leadId, userId) → 403 bei Verletzung.

### Collaborator-API (Beispiel)
```bash
POST /api/leads/{id}/collaborators { userId, role }
DELETE /api/leads/{id}/collaborators/{userId}
```

## Policy Examples

### Kritische Aktionen (CreditCheck, OrderSubmit)
Nur Owner; Collaborators erlauben vorbereitende Schritte (Angebot, Notizen).

### Ownership-Challenge
Nicht-Owner mit belegbarer Aktivität erzeugt Challenge → Team-Lead entscheidet.

## Integration Points
- **Settings:** [SETTINGS_REGISTRY_COMPLETE.md](../../governance/artefakte/SETTINGS_REGISTRY_COMPLETE.md) (lead.protection.*, ai.*, credit.*)
- **Events:** [EVENT_CATALOG.md](../../integration/artefakte/EVENT_CATALOG.md) (lead.protection.reminder, lead.protection.expired)
- **Operations:** [OPERATIONS_RUNBOOK.md](../../operations/artefakte/OPERATIONS_RUNBOOK.md) (Ownership-Konflikte, Expiry-Wellen)

## Troubleshooting

### Schutz läuft trotz Activity ab
Prüfe Activity.kind, Zeitzone, RLS-Fenster und aktive Holds.

### Collaborator sieht keinen Lead
Rolle prüfen (VIEW/ASSIST/NEGOTIATE) und RLS anhand v_lead_protection nachvollziehen.

### Owner kann nicht schreiben
status=EXPIRED? → Re-Assign oder Team-Lead-Entscheidung nötig.