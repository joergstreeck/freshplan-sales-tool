---
Title: Database Migration Strategy
Purpose: Nachvollziehbare, risikoarme Migrationen von Planung bis Produktion.
Audience: Backend, DevOps, DBA
Last Updated: 2025-09-20
Status: Final
---

# 🗄️ Database Migration Strategy

## 30-Second Summary
- Planungsphase: dynamische Nummern via get-next-migration.sh; V225 reserviert → unkritisch.
- Produktion: strikte Nummern-Vergabe, Review-Gates, Zero-Downtime-Patterns.

## Numbering & Workflow

### Planung
- **Dateien aus Templates erzeugen:** `$(./scripts/get-next-migration.sh)__<name>.sql`
- **Rebase vor Merge** auf main.

### Produktion
- **Fortlaufend**, CI prüft Lücken/Mehrfachnummern.
- **Changelog generiert**.

## Zero-Downtime Patterns

### Expand → Migrate → Contract
- **Neue Spalten nullable** + Backfill-Job → Dual-Write → Altspalten entfernen.

### Indizes
- **CONCURRENTLY**; lange Locks vermeiden.

### Views
- **MV REFRESH CONCURRENTLY**; Versionen wechseln per Synonym/VIEW-Swap.

### Feature Flags
- **Schema hinter Flag aktivieren**; Rollback Pfade bereit halten.

## Rollback
- **Nur additive Änderungen** ohne Downtime.
- **Destruktiv erst nach „contract"-Schritt** + Datensicherung.

## Integration Points
- **SLOs:** (Rebuild-Fenster)
- **Events:** (Versionierung)
- **Settings:** (Feature-Flags)

## Troubleshooting

### Lock-Waits
Zeitfenster, CONCURRENTLY, Statement Timeouts, Queue entlasten.

### Abweichende Umgebungen
Drift-Check per schemadiff; preflight in Staging inkl. k6.