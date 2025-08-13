# 📚 Code Verbesserung August 2025 - Dokumentation

## 📑 Navigation

**Du bist hier:** Startpunkt der Dokumentation  
**➡️ Als Nächstes:** [`PR_5_CRITICAL_CONTEXT.md`](/Users/joergstreeck/freshplan-sales-tool/docs/features/Code_Verbesserung_08_25/PR_5_CRITICAL_CONTEXT.md) (⚠️ PFLICHTLEKTÜRE!)  
**🗺️ Übersicht:** [`NAVIGATION_MAP.md`](/Users/joergstreeck/freshplan-sales-tool/docs/features/Code_Verbesserung_08_25/NAVIGATION_MAP.md)

---

## 🎯 Übersicht

Dieser Ordner enthält die komplette Dokumentation für die **Code Quality Initiative** im August 2025, speziell für die **Backend Service Refactoring** mit CQRS Pattern.

## 🚀 SCHNELLSTART für neuen Claude

### ⚡ Option A: QUICK START (Empfohlen!)
**[`PR_5_QUICK_START.md`](PR_5_QUICK_START.md)** - **NUR 3 DOKUMENTE, 10 MINUTEN!**  
*Spring direkt zu den essentiellen Informationen für PR #5*

### 📚 Option B: Vollständige Dokumentation (falls mehr Kontext gewünscht)

#### 1️⃣ **ESSENTIELL** (Muss gelesen werden)
1. **[`PR_5_CRITICAL_CONTEXT.md`](PR_5_CRITICAL_CONTEXT.md)** - ⚠️ Was darf NICHT kaputt gehen
2. **[`PR_5_BACKEND_SERVICES_REFACTORING.md`](PR_5_BACKEND_SERVICES_REFACTORING.md)** - Der konkrete 5-Tages-Plan
3. **[`TEST_STRATEGY_PER_PR.md#pr-5`](TEST_STRATEGY_PER_PR.md#pr-5-backend-service-refactoring-cqrs)** - Welche Tests du brauchst

#### 2️⃣ **OPTIONAL** (Nur bei Bedarf)
4. **[`CODE_QUALITY_PR_ROADMAP.md`](CODE_QUALITY_PR_ROADMAP.md)** - Wo ist PR #5 im Gesamtplan?
5. **[`ENTERPRISE_CODE_REVIEW_2025.md`](ENTERPRISE_CODE_REVIEW_2025.md)** - Historischer Kontext
6. **[`CODE_QUALITY_START_HERE.md`](CODE_QUALITY_START_HERE.md)** - Generelle Initiative

---

## 📊 Aktueller Status

### PR #5: Backend Services CQRS Refactoring
- **Status:** 🚧 IN IMPLEMENTIERUNG (Phase 1)
- **Branch:** `feature/refactor-large-services` ✅ erstellt
- **Umfang:** 3 Services, 1.628 Zeilen Code
- **Geschätzt:** 3-5 Tage
- **Risiko:** 🔴 HOCH (Kern-Services)
- **Fortschritt:** Phase 0 ✅ | Phase 1 🚧 | Phase 2-4 ⏳

### 📝 Live-Dokumentation:
- **[`PR_5_IMPLEMENTATION_LOG.md`](PR_5_IMPLEMENTATION_LOG.md)** - Aktueller Fortschritt & Metriken

### Betroffene Services:
1. **CustomerService** (716 Zeilen) - HERZSTÜCK der Anwendung
2. **OpportunityService** (451 Zeilen) - Sales Pipeline
3. **AuditService** (461 Zeilen) - Compliance-kritisch

---

## ⚠️ Kritische Informationen

### Was du WISSEN MUSST:
- **69 Test-Kunden** sind im System (müssen weiter funktionieren)
- **Frontend** ist eng gekoppelt mit der API (darf nicht brechen)
- **Audit-Trail** ist Compliance-relevant (muss vollständig bleiben)
- **Migration V219** ist die nächste freie Nummer

### Was du NIEMALS tun darfst:
- ❌ API-Contract ändern
- ❌ Bestehende DB-Tabellen modifizieren
- ❌ Customer Number Format ändern
- ❌ Ohne Tests committen
- ❌ Direkt auf main pushen

---

## 🛠️ Technische Details

### CQRS-Architektur:
```
CustomerService (716 Zeilen)
    ↓ split into ↓
CustomerCommandService (Write)  +  CustomerQueryService (Read)
    ↓ events ↓                        ↓ views ↓
    Event Store                   Materialized Views
```

### Vorteile:
- ✅ Bessere Performance (optimierte Reads)
- ✅ Klarere Verantwortlichkeiten
- ✅ Einfachere Tests
- ✅ Unabhängige Skalierung

### Risiken:
- ⚠️ Breaking Changes möglich
- ⚠️ Eventual Consistency
- ⚠️ Komplexere Architektur
- ⚠️ Migration erforderlich

---

## 📝 Checkliste für Start

### Vor dem Coding:
- [ ] Alle 6 Dokumente in diesem Ordner gelesen
- [ ] `PR_5_CRITICAL_CONTEXT.md` verstanden
- [ ] Backup der Datenbank erstellt
- [ ] Baseline-Tests dokumentiert
- [ ] Branch von main erstellt (nicht von feature!)

### Während dem Coding:
- [ ] Feature Flag für schrittweise Migration nutzen
- [ ] Parallele Implementierung (alte + neue Services)
- [ ] Nach jedem Schritt testen
- [ ] Performance messen und vergleichen
- [ ] Rollback-Plan bereit halten

### Nach dem Coding:
- [ ] Alle Tests grün
- [ ] Frontend funktioniert unverändert
- [ ] Performance gleich oder besser
- [ ] Dokumentation aktualisiert
- [ ] Team-Review durchgeführt

---

## 🔗 Externe Referenzen

### Projekt-Dokumente:
- [`/docs/CLAUDE.md`](/Users/joergstreeck/freshplan-sales-tool/docs/CLAUDE.md) - Allgemeine Arbeitsrichtlinien
- [`/docs/NEXT_STEP.md`](/Users/joergstreeck/freshplan-sales-tool/docs/NEXT_STEP.md) - Aktueller Projektstatus

### Source Code:
- [`CustomerService.java`](/Users/joergstreeck/freshplan-sales-tool/backend/src/main/java/de/freshplan/domain/customer/service/CustomerService.java)
- [`OpportunityService.java`](/Users/joergstreeck/freshplan-sales-tool/backend/src/main/java/de/freshplan/domain/opportunity/service/OpportunityService.java)
- [`AuditService.java`](/Users/joergstreeck/freshplan-sales-tool/backend/src/main/java/de/freshplan/domain/audit/service/AuditService.java)

### Tests:
- [`CustomerServiceTest.java`](/Users/joergstreeck/freshplan-sales-tool/backend/src/test/java/de/freshplan/domain/customer/service/CustomerServiceTest.java)
- [`CustomerMapperTest.java`](/Users/joergstreeck/freshplan-sales-tool/backend/src/test/java/de/freshplan/domain/customer/service/mapper/CustomerMapperTest.java)

---

## 💡 Tips für Erfolg

1. **Kleine Schritte:** Nicht alles auf einmal ändern
2. **Testen, testen, testen:** Nach jeder Änderung
3. **Performance messen:** Vorher/Nachher Vergleich
4. **Rollback bereit:** Immer einen Weg zurück haben
5. **Kommunizieren:** Bei Unsicherheit nachfragen

---

## 🚨 Notfall-Prozedur

Falls etwas schiefgeht:

```bash
# 1. STOP - Keine weiteren Änderungen!

# 2. Status prüfen
git status
git diff

# 3. Tests laufen
./mvnw test

# 4. Bei Fehler: Rollback
git reset --hard HEAD
psql -U freshplan < backup_[timestamp].sql

# 5. Services neu starten
./scripts/stop-backend.sh
./scripts/start-backend.sh

# 6. Verifizieren
curl http://localhost:8080/api/customers
```

---

**WICHTIG:** Diese Dokumentation ist VOLLSTÄNDIG und enthält ALLE Informationen für PR #5.

**Bei Fragen:** Erst diese Dokumente lesen, dann nachfragen!

---

**Letzte Aktualisierung:** 13.08.2025  
**Autor:** Claude  
**Status:** ✅ BEREIT FÜR NEUEN CLAUDE