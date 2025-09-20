# 🚀 PR #5 QUICK START - NUR DAS WICHTIGSTE!

**Zweck:** Schneller Einstieg für PR #5 ohne Umwege  
**Zeitbedarf:** 10 Minuten Lesezeit  
**Aufgabe:** Backend Services mit CQRS Pattern refactoren

---

## ⚡ SOFORT-EINSTIEG (3 Dokumente, 10 Min)

### 1️⃣ **KRITISCHE WARNUNGEN** (3 Min)
**📄 [`PR_5_CRITICAL_CONTEXT.md`](PR_5_CRITICAL_CONTEXT.md)**  
**Was:** Was du NIEMALS ändern darfst  
**Warum:** Damit nichts kaputt geht  
**Wichtigste Punkte:**
- API muss identisch bleiben
- DB-Schema nur erweitern, nie ändern
- 69 Test-Kunden müssen funktionieren
- Frontend darf nicht brechen

### 2️⃣ **DER PLAN** (5 Min)
**📄 [`PR_5_BACKEND_SERVICES_REFACTORING.md`](PR_5_BACKEND_SERVICES_REFACTORING.md)**  
**Was:** Detaillierter 5-Tages-Plan mit Code  
**Warum:** Das ist deine Arbeitsanleitung  
**Wichtigste Punkte:**
- CustomerService → CustomerCommandService + CustomerQueryService
- Migration V219 vorbereitet
- Feature Flag für schrittweise Migration
- Konkrete Code-Beispiele vorhanden

### 3️⃣ **DIE TESTS** (2 Min)
**📄 [`TEST_STRATEGY_PER_PR.md#pr-5`](TEST_STRATEGY_PER_PR.md#pr-5-backend-service-refactoring-cqrs)**  
**Was:** Welche Tests du schreiben musst  
**Warum:** Qualitätssicherung  
**Wichtigste Punkte:**
- 90% Coverage Ziel
- Performance < 200ms für Queries
- Event-Tests wichtig
- Backward Compatibility testen

---

## 🎯 DEINE AUFGABE IN 1 MINUTE

### Was:
**3 Services aufteilen** nach CQRS Pattern:
- CustomerService (716 Zeilen)
- OpportunityService (451 Zeilen)  
- AuditService (461 Zeilen)

### Wie:
1. **Tag 1-2:** CustomerService splitten
2. **Tag 3:** OpportunityService splitten
3. **Tag 4:** AuditService zu Event Store
4. **Tag 5:** Integration & Tests

### Ergebnis:
- 6 fokussierte Services statt 3 große
- Bessere Performance durch Read-Views
- Event-Driven Architecture vorbereitet

---

## ✅ QUICK CHECKLIST zum Start

```bash
# 1. Branch erstellen
git checkout main
git pull
git checkout -b feature/refactor-large-services

# 2. Backup machen
pg_dump -U freshplan freshplan > backup_$(date +%Y%m%d).sql

# 3. Tests laufen lassen (Baseline)
cd backend
./mvnw test

# 4. Mit Phase 1 starten (CustomerCommandService)
```

---

## 🔴 STOPP-SIGNALE

Wenn du EINES davon siehst, STOPPE sofort:

1. ❌ API-Response Format ändert sich
2. ❌ Weniger als 69 Test-Kunden laden
3. ❌ Frontend zeigt Fehler
4. ❌ Tests werden rot
5. ❌ Performance wird schlechter

**Bei Problemen:** Rollback mit dem Backup!

---

## 📞 Falls du nicht weiterkommst:

1. **Unsicher?** → Lies [`PR_5_CRITICAL_CONTEXT.md`](PR_5_CRITICAL_CONTEXT.md)
2. **Plan unklar?** → Lies [`PR_5_BACKEND_SERVICES_REFACTORING.md`](PR_5_BACKEND_SERVICES_REFACTORING.md)
3. **Tests unklar?** → Lies [`TEST_STRATEGY_PER_PR.md#pr-5`](TEST_STRATEGY_PER_PR.md)

---

## 🏁 LOS GEHT'S!

**Du hast:**
- ✅ Die Warnungen (was nicht kaputt gehen darf)
- ✅ Den Plan (5 Tage, klare Schritte)
- ✅ Die Tests (was geprüft werden muss)

**Starte mit:** CustomerCommandService.java erstellen!

---

## 📚 Weitere Dokumente (NUR bei Bedarf)

Falls du mehr Kontext brauchst:
- [`CODE_QUALITY_PR_ROADMAP.md`](CODE_QUALITY_PR_ROADMAP.md) - Gesamtübersicht aller PRs
- [`ENTERPRISE_CODE_REVIEW_2025.md`](ENTERPRISE_CODE_REVIEW_2025.md) - Warum wir refactoren
- [`README.md`](README.md) - Ordner-Übersicht

**Aber:** Diese sind OPTIONAL. Die 3 oben reichen für die Arbeit!

---

**🎯 Fokus:** Lies nur die 3 essentiellen Dokumente und leg los!