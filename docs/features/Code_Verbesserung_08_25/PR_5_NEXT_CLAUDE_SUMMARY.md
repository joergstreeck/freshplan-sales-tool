# 📋 PR #5 Status für nächsten Claude - CustomerCommandService

**Stand:** 13.08.2025 21:35  
**Branch:** `feature/refactor-large-services`  
**Aktueller Fokus:** CustomerCommandService (86% fertig)

---

## 🎯 Was wurde heute erreicht?

### CustomerCommandService.java
**Pfad:** `/backend/src/main/java/de/freshplan/domain/customer/service/command/CustomerCommandService.java`

✅ **6 von 7 Command-Methoden implementiert:**
1. ✅ `createCustomer()` - Mit Timeline Events
2. ✅ `updateCustomer()` - Exakte Kopie
3. ✅ `deleteCustomer()` - Soft-Delete mit Business Rules
4. ✅ `restoreCustomer()` - Für gelöschte Kunden
5. ✅ `addChildCustomer()` - Mit Bug aus Original (dokumentiert)
6. ✅ `updateAllRiskScores()` - Mit Problemen aus Original (dokumentiert)
7. ⏳ `mergeCustomers()` - NOCH ZU IMPLEMENTIEREN

### Integration Tests
**Pfad:** `/backend/src/test/java/de/freshplan/domain/customer/service/command/CustomerCommandServiceIntegrationTest.java`

✅ **Alle Tests laufen GRÜN und beweisen:**
- Beide Services (alt und neu) verhalten sich 100% identisch
- Timeline Events funktionieren korrekt mit Category
- Soft-Delete Business Rules werden eingehalten
- Bekannte Bugs wurden dokumentiert und in Tests erfasst

---

## ⚠️ WICHTIGE ERKENNTNISSE

### 1. KEINE Domain Events!
CustomerService nutzt **Timeline Events** (direkt in DB), NICHT Domain Events mit Event Bus!

### 2. Dokumentierte Probleme (Technical Debt)

#### In `addChildCustomer()`:
- ❌ Erstellt KEIN Timeline Event (inkonsistent)
- ❌ Bug: isDescendant() Check ist invertiert (zirkuläre Hierarchien möglich)

#### In `updateAllRiskScores()`:
- ❌ Limitiert auf 1000 Kunden (Page.ofSize(1000))
- ❌ Erstellt KEINE Timeline Events
- ❌ Keine Fehlerbehandlung für einzelne Kunden
- ❌ Keine Möglichkeit für Teil-Updates

**WICHTIG:** Alle Probleme wurden ABSICHTLICH übernommen für 100% Kompatibilität!

---

## 📝 Was fehlt noch?

### Phase 1 - CustomerService Split:
- [ ] `mergeCustomers()` Methode implementieren (letzte Command-Methode)
- [ ] CustomerQueryService mit 8 Query-Methoden erstellen
- [ ] CustomerResource als Facade mit Feature Flag
- [ ] Performance-Vergleich durchführen

### Weitere Phasen:
- [ ] Phase 2: OpportunityService splitten
- [ ] Phase 3: AuditService zu Event Store
- [ ] Phase 4: Integration & Tests

---

## 🚀 Nächste Schritte für neuen Claude

### 1. Repository-Status prüfen:
```bash
git status
git log --oneline -5
```

### 2. Tests laufen lassen:
```bash
cd backend
./mvnw test -Dtest=CustomerCommandServiceIntegrationTest -q
```

### 3. Mit `mergeCustomers()` fortfahren:
- Original-Methode ist in `CustomerService.java` Zeilen 483-536
- Exakte Kopie erstellen (mit allen Eigenheiten)
- Integration Tests schreiben
- Dokumentieren wenn Probleme gefunden werden

### 4. Dann CustomerQueryService beginnen:
- 8 Query-Methoden aus CustomerService extrahieren
- Read-optimierte Implementierung

---

## 📚 Wichtige Dokumente

1. **Hauptplan:** `PR_5_BACKEND_SERVICES_REFACTORING.md`
2. **Kritischer Kontext:** `PR_5_CRITICAL_CONTEXT.md`
3. **Implementation Log:** `PR_5_IMPLEMENTATION_LOG.md`
4. **Test-Strategie:** `TEST_STRATEGY_PER_PR.md`

---

## ⚡ Quick Commands

```bash
# Backend starten
cd backend
./mvnw quarkus:dev

# Nur CustomerCommandService Tests
./mvnw test -Dtest=CustomerCommandServiceIntegrationTest

# Alle Tests
./mvnw test

# Code formatieren
./mvnw spotless:apply
```

---

## 🔑 Schlüssel-Prinzip

**EXAKTE KOPIE** - Alle Methoden müssen sich IDENTISCH zum Original verhalten!
- Auch mit Bugs
- Auch mit Limitierungen
- Auch mit fehlenden Features

Das garantiert sichere Migration via Feature Flag.

---

**Viel Erfolg!** 🚀