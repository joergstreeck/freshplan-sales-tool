---
Title: Settings Registry Reference (Module 06)
Purpose: Vollständige Quelle für konfigurierbare Policies, inkl. Scopes, Defaults und Merge-Strategien.
Audience: Entwickler, Admins, Configuration-Owner
Last Updated: 2025-09-20
Status: Final
---

# ⚙️ Settings Registry Reference (Module 06)

## 30-Second Summary
- Keys sind strikt typisiert (scalar, list, object), validiert per JSON Schema.
- Scopes: global, tenant, org, user – Merge-Strategien klar definiert.
- B2B-Food Besonderheiten wie Saisonfenster und Währungen sind abbildbar.

## Key Families (Auszug, final)

### Lead Protection Settings
- **lead.protection.baseMonths**
  - scope: global; type: scalar; merge: replace; default: {months:6}
  - schema: int 1..24
- **lead.protection.progressDays**
  - scope: global; type: scalar; merge: replace; default: {days:60}
- **lead.protection.graceDays**
  - scope: global; type: scalar; merge: replace; default: {days:10}
- **lead.protection.mode**
  - scope: global; type: scalar; merge: replace; default: {mode:"GREATEST"}
- **lead.protection.reminder.channels**
  - scope: tenant/org; type: list; merge: append; default: ["EMAIL","INAPP"]
- **lead.protection.notify.roles**
  - scope: tenant/org; type: list; merge: append; default: ["manager"]

### AI Settings
- **ai.budget.monthly.cap**
  - scope: tenant/org; type: scalar; merge: replace; default: 1000
- **ai.routing.confidence.threshold**
  - scope: user/org/tenant; type: scalar; merge: replace; default: 0.7
- **ai.routing.demotion.cooldown**
  - scope: org/tenant; type: scalar; merge: replace; default: "PT30M"
- **ai.cache.ttl**
  - scope: org/tenant; type: scalar; merge: replace; default: "PT8H"

### Credit Settings
- **credit.cache.ttl**
  - scope: org/tenant; type: scalar; merge: replace; default: "PT8H"
- **credit.peak.slo.p95.ms**
  - scope: global; type: scalar; merge: replace; default: 500
- **credit.batch.window.ms**
  - scope: org/tenant; type: scalar; merge: replace; default: 50

### Help Settings
- **help.space.enduser / help.space.salesops**
  - scope: org/tenant; type: object; merge: merge; default: {}
  - enthält CAR-Parameter pro Space (cooldowns, nudge-budget)

## Validation Rules
- Alle Keys besitzen JSON Schema; Writes werden serverseitig validiert.
- Merge-Strategien beachten (replace, merge, append).

## Integration Points
- **Security:** [SECURITY_MODEL_FINAL.md](../../sicherheit/artefakte/SECURITY_MODEL_FINAL.md)
- **SLOs/Events/Ops:** Querverweise beachten.

## Troubleshooting

### Setting greift nicht
Scope prüfen (user/org/tenant/global), Cache-Invalidation (LISTEN/NOTIFY), JSON Schema-Fehler.