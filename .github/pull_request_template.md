## 🎯 Ziel
<!-- PFLICHT: Was wird implementiert? Welches Feature/Sub-Feature? -->

## ⚠️ Risiko
<!-- PFLICHT: Welche Risiken? Mitigation-Strategien? -->

## 🔄 Migrations-Schritte + Rollback
<!-- PFLICHT: SQL-Änderungen? Migration VXX? Rollback-Plan <5min? -->
- [ ] Keine SQL-Änderungen
- [ ] Migration V___ erstellt
- [ ] Rollback getestet und dokumentiert

## ⚡ Performance-Nachweis
<!-- PFLICHT: k6-Results? Bundle-Size? Query-Plans? -->
- [ ] k6-smoke: P95 < 200ms
- [ ] Bundle-size: < +20KB
- [ ] EXPLAIN ANALYZE für neue Queries durchgeführt

## 🔒 Security-Checks
<!-- PFLICHT: ABAC/RLS? ZAP-Results? Input-Validation? -->
- [ ] ABAC/RLS-Tests: grün
- [ ] ZAP-baseline: clean
- [ ] Input-Validation: implementiert
- [ ] Keine hardcoded Secrets

## 📚 SoT-Referenzen
<!-- PFLICHT: Technical-Concepts? Artefakte? CLAUDE.md Regeln? -->
- [ ] Technical-Concept: [Link zum relevanten Dokument]
- [ ] Artefakte genutzt: [Liste mit Änderungen]
- [ ] CLAUDE.md Regeln: befolgt
- [ ] Master Plan V5: aligned

---

### PR Checklist
- [ ] Tests schreiben und alle bestehen
- [ ] Code follows style guidelines (Spotless applied)
- [ ] Self-review durchgeführt
- [ ] Kommentare für komplexe Logik hinzugefügt
- [ ] Dokumentation aktualisiert
- [ ] Keine Breaking Changes ohne Diskussion
- [ ] Feature Flag für neue Features (wenn applicable)