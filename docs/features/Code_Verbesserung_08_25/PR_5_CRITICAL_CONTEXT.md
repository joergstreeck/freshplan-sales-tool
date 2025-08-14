# 🚨 KRITISCHER KONTEXT für PR #5 - MUSS GELESEN WERDEN!

**Erstellt:** 13.08.2025  
**Zweck:** Essenzielle Informationen für sicheres Refactoring  
**Status:** 🔴 SENSIBLE PRODUKTION - HÖCHSTE VORSICHT

---

## 📑 Navigation (Lesereihenfolge)

**Du bist hier:** Dokument 1 von 7  
**⬅️ Start:** [`README.md`](/Users/joergstreeck/freshplan-sales-tool/docs/features/Code_Verbesserung_08_25/README.md)  
**➡️ Weiter:** [`CODE_QUALITY_START_HERE.md`](/Users/joergstreeck/freshplan-sales-tool/docs/features/Code_Verbesserung_08_25/CODE_QUALITY_START_HERE.md)  
**🎯 Ziel:** [`PR_5_BACKEND_SERVICES_REFACTORING.md`](/Users/joergstreeck/freshplan-sales-tool/docs/features/Code_Verbesserung_08_25/PR_5_BACKEND_SERVICES_REFACTORING.md)

---

## ⚠️ KRITISCHE WARNUNGEN - ZUERST LESEN!

### 🔴 Was NIEMALS verändert werden darf:

1. **Datenbank-Schema der Haupttabellen**
   - `customers` Tabelle ist PRODUCTION - keine Spalten löschen/ändern!
   - `opportunities` Tabelle hat aktive Trigger
   - `audit_entries` wird für Compliance benötigt
   - Nur NEUE Tabellen/Views hinzufügen, niemals bestehende ändern

2. **API-Endpunkte müssen identisch bleiben**
   ```
   GET /api/customers/{id}          - MUSS gleiche Response liefern
   POST /api/customers              - MUSS gleiche Felder akzeptieren
   PUT /api/customers/{id}          - MUSS gleiche Updates erlauben
   DELETE /api/customers/{id}       - MUSS Soft-Delete bleiben
   ```

3. **Business-kritische Logik**
   - Customer Number Generation (Format: KD-YYYY-NNNNN) MUSS erhalten bleiben
   - Risk Score Berechnung darf sich NICHT ändern
   - Soft-Delete Mechanismus MUSS funktionieren
   - Parent-Child Hierarchie MUSS intakt bleiben

---

## 📊 Aktuelle Produktions-Statistiken

**Stand: 14.08.2025 - Nach Phase 4**
- **69 Test-Kunden** im System (CustomerDataInitializer)
- **25 Kontakte** verknüpft
- **31 Opportunities** in verschiedenen Stages
- **Frontend** erwartet diese Datenstruktur!
- **Phase 1-4 CQRS Migration:** ✅ CustomerService, OpportunityService, AuditService, CustomerTimelineService erfolgreich migriert

---

## 🔍 WAS GENAU MACHEN WIR?

### Ziel des Refactorings:
Wir trennen **gemischte Read/Write-Operationen** in separate Services nach dem **CQRS Pattern**, OHNE die externe API zu brechen.

### Was sich NICHT ändert (Facade Pattern):
```java
// ALT: Bleibt als Facade bestehen!
@Path("/api/customers")
public class CustomerResource {
    @Inject CustomerCommandService commandService;
    @Inject CustomerQueryService queryService;
    
    @GET
    @Path("/{id}")
    public CustomerResponse getCustomer(@PathParam("id") UUID id) {
        // Delegiert intern an queryService
        return queryService.findById(id)
            .orElseThrow(() -> new NotFoundException());
    }
    
    @POST
    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        // Delegiert intern an commandService
        return commandService.createCustomer(request, getUser());
    }
}
```

### Was sich intern ändert:
- **CustomerService** (716 Zeilen) → **CustomerCommandService** + **CustomerQueryService**
- Neue Event-basierte Kommunikation zwischen Services
- Read-optimierte Views für bessere Performance

---

## 🛠️ SCHRITT-FÜR-SCHRITT VORGEHEN (SICHER!)

### Phase 0: Vorbereitung (KRITISCH!)
```bash
# 1. BACKUP erstellen
pg_dump -U freshplan -h localhost freshplan > backup_before_pr5_$(date +%Y%m%d_%H%M%S).sql

# 2. Branch von MAIN erstellen (nicht von feature branch!)
git checkout main
git pull
git checkout -b feature/refactor-large-services

# 3. Baseline-Tests laufen lassen und dokumentieren
cd backend
./mvnw test > baseline_tests_$(date +%Y%m%d_%H%M%S).log
# ALLE müssen grün sein!

# 4. Performance-Baseline messen
curl -w "@curl-format.txt" -o /dev/null -s "http://localhost:8080/api/customers"
# Response time dokumentieren!
```

### Phase 1: Parallele Implementierung (SAFE)
1. **NEUE Services neben alten erstellen**
   - CustomerCommandService.java (NEU)
   - CustomerQueryService.java (NEU)
   - CustomerService.java bleibt UNVERÄNDERT

2. **Feature Flag für schrittweise Migration**
```java
@ConfigProperty(name = "features.cqrs.enabled", defaultValue = "false")
boolean cqrsEnabled;

public CustomerResponse createCustomer(CreateCustomerRequest request) {
    if (cqrsEnabled) {
        return commandService.createCustomer(request);
    }
    return oldService.createCustomer(request); // Fallback
}
```

### Phase 2: Paralleles Testen
```java
@Test
public void testParallelExecution() {
    // Beide Implementierungen müssen identische Ergebnisse liefern
    CustomerResponse oldResult = oldService.createCustomer(request);
    CustomerResponse newResult = commandService.createCustomer(request);
    
    assertThat(oldResult).isEqualToComparingFieldByField(newResult);
}
```

---

## 🧪 KRITISCHE TEST-CHECKLISTE

### Vor JEDEM Commit:
- [ ] Alle 69 Test-Kunden werden noch korrekt geladen
- [ ] Frontend funktioniert ohne Änderungen
- [ ] API-Responses sind identisch (Feldnamen, Typen)
- [ ] Performance nicht schlechter als vorher
- [ ] Keine neuen Fehler in den Logs

### Regression Tests (MUSS laufen):
```bash
# 1. Backend Unit Tests
./mvnw test

# 2. Integration Tests  
./mvnw test -Dtest=*IntegrationTest

# 3. Frontend Tests
cd ../frontend
npm test

# 4. E2E Test mit Frontend
npm run test:e2e
```

---

## 🔐 ROLLBACK-PLAN

Falls IRGENDWAS schiefgeht:

```bash
# 1. Code zurücksetzen
git reset --hard HEAD~1

# 2. Database zurücksetzen (falls Migration applied)
psql -U freshplan -h localhost freshplan < backup_before_pr5_[timestamp].sql

# 3. Services neu starten
./scripts/stop-backend.sh
./scripts/start-backend.sh

# 4. Verifizieren
curl http://localhost:8080/api/customers
```

---

## 📋 DEPENDENCIES & SIDE EFFECTS

### Diese Services nutzen CustomerService:
1. **OpportunityService** - für Customer-Validierung
2. **AuditService** - logged alle Customer-Änderungen
3. **ReportService** - aggregiert Customer-Daten
4. **NotificationService** - sendet Emails bei Customer-Events

### Diese Frontend-Komponenten nutzen die API:
1. **CustomerList.tsx** - erwartet `expectedAnnualVolume` Feld
2. **CustomerDetail.tsx** - nutzt alle Felder
3. **IntelligentFilterBar.tsx** - filtert nach `riskScore`, `status`, `industry`
4. **KanbanBoard.tsx** - grouped by `status`

---

## 🎯 ERFOLGS-KRITERIEN

### Das Refactoring ist NUR erfolgreich wenn:
1. ✅ KEINE Breaking Changes in der API
2. ✅ ALLE Tests bleiben grün
3. ✅ Performance verbessert sich (oder bleibt gleich)
4. ✅ Frontend funktioniert OHNE Anpassungen
5. ✅ Rollback jederzeit möglich
6. ✅ Audit-Trail bleibt vollständig

---

## 🚦 GO/NO-GO Entscheidungspunkte

### Nach Phase 1 (Parallel Implementation):
- **GO wenn:** Alle Tests grün, keine API-Änderungen
- **NO-GO wenn:** Tests fehlschlagen, API-Contract verletzt

### Nach Phase 2 (Testing):
- **GO wenn:** Performance besser/gleich, keine Fehler
- **NO-GO wenn:** Performance-Regression, Fehler in Logs

### Vor Merge:
- **GO wenn:** 5 Tage stabil im Test, Team-Review positiv
- **NO-GO wenn:** Irgendwelche Zweifel oder offene Issues

---

## 📞 NOTFALL-KONTAKTE

Falls du nicht weiterkommst oder unsicher bist:

1. **Check diese Dokumente:**
   - [`PR_5_BACKEND_SERVICES_REFACTORING.md`](/Users/joergstreeck/freshplan-sales-tool/docs/features/Code_Verbesserung_08_25/PR_5_BACKEND_SERVICES_REFACTORING.md) - Detaillierter Plan
   - [`TEST_STRATEGY_PER_PR.md`](/Users/joergstreeck/freshplan-sales-tool/docs/features/Code_Verbesserung_08_25/TEST_STRATEGY_PER_PR.md) - Test-Anforderungen
   - `/docs/CLAUDE.md` - Allgemeine Arbeitsrichtlinien

2. **Prüfe diese Systeme:**
   - PostgreSQL läuft? `pg_isready -h localhost`
   - Backend läuft? `curl http://localhost:8080/q/health`
   - Frontend läuft? `curl http://localhost:5173`

3. **Im Zweifel:** STOPPEN und nachfragen!

---

## ⚡ QUICK REFERENCE - Wichtigste Befehle

```bash
# Status prüfen
git status
git diff

# Tests laufen
./mvnw test
./mvnw test -Dtest=CustomerServiceTest

# Services prüfen
curl http://localhost:8080/api/customers | jq
curl http://localhost:8080/api/customers/[uuid] | jq

# Logs prüfen
tail -f /tmp/quarkus.log
tail -f /tmp/vite.log

# Database prüfen
psql -U freshplan -h localhost freshplan
\dt                          # Tabellen anzeigen
SELECT COUNT(*) FROM customers;
SELECT COUNT(*) FROM audit_entries;
```

---

## 🔴 ABSOLUTE NO-GOs

1. **NIEMALS** direkt in PROD testen
2. **NIEMALS** Schema ohne Migration ändern
3. **NIEMALS** API-Contract brechen
4. **NIEMALS** ohne Tests committen
5. **NIEMALS** Force-Push auf shared branches
6. **NIEMALS** Audit-Einträge löschen
7. **NIEMALS** Customer Numbers ändern

---

**WICHTIG:** Dieses Dokument ist PFLICHTLEKTÜRE vor Beginn der Arbeit an PR #5!

**Bei Unsicherheit:** Lieber einmal zu viel fragen als einmal zu wenig!

---

**Letzte Warnung:** Der CustomerService ist das HERZSTÜCK der Anwendung. Ein Fehler hier betrifft ALLE anderen Module!

**Autor:** Claude  
**Kritikalität:** 🔴 HÖCHSTE STUFE  
**Gültig bis:** PR #5 abgeschlossen