# 🔧 FIX: AuditService RequestScoped Context Error

**Datum:** 2025-07-26 01:12  
**Problem:** OpportunityRenewalResourceTest Failures - HTTP 500 statt 201  
**Status:** 🔄 FIX IMPLEMENTIERT - CI VERIFIKATION AUSSTEHEND

## 🎯 Problem-Identifikation

**Symptome:**
- CI Integration Tests: 8 Failures in `OpportunityRenewalResourceTest`
- Alle Tests erwarten Status `201` (Created), bekommen aber `500` (Internal Server Error)
- Nicht UserResourceIT wie ursprünglich vermutet!

**Root Cause:**
```
ERROR [de.fr.do.au.se.AuditService] Critical: Failed to log audit event: 
jakarta.enterprise.context.ContextNotActiveException: RequestScoped context was not active
```

**Fehler-Ort:** `AuditService.logSync()` - RequestScoped SecurityIdentity nicht verfügbar in Async-Context

## 🔧 Lösung

**Fix:** `@ActivateRequestContext` Annotation hinzugefügt zu `logSync()` Methode

```java
// Vorher:
@Transactional(Transactional.TxType.REQUIRES_NEW)  
public UUID logSync(AuditContext context) {

// Nachher:
@Transactional(Transactional.TxType.REQUIRES_NEW)
@jakarta.enterprise.context.control.ActivateRequestContext
public UUID logSync(AuditContext context) {
```

**Datei:** `./src/main/java/de/freshplan/domain/audit/service/AuditService.java:118`

## 🔄 Verifikation

**Lokaler Test:**
```bash
./mvnw test -Dtest=OpportunityRenewalResourceTest
# Ergebnis: ERFOLGREICH - alle Tests laufen lokal durch
```

**CI Verifikation:** ⏳ AUSSTEHEND
- **Erwartung:** OpportunityRenewalResourceTest: 8 Failures → 0 Failures
- **Commit:** dbfbbce - gepusht um 01:13
- **Status:** Warten auf CI-Pipeline Ergebnis

**❗ WICHTIG:** Status auf ✅ GELÖST erst ändern wenn CI grün ist!

## 📋 Nächste Schritte

1. ✅ **Commit & Push:** Fix zur CI pushen (abgeschlossen - dbfbbce)
2. ⏳ **CI Monitoring:** Grüne Pipeline bestätigen - NÄCHSTER CLAUDE MUSS DIES VERIFIZIEREN
3. 📋 **Debug-Datei entfernen:** UserResourceITDebug.java löschen (nicht mehr benötigt)
4. 📋 **Dokumentation aktualisieren:** Status auf ✅ GELÖST ändern wenn CI grün

## 🧠 Learnings für nächsten Claude

**Wichtige Erkenntnis:** 
- Das ursprüngliche Problem waren NICHT die UserResourceIT Tests
- Die UserResourceIT Assertion-Fixes waren korrekt, aber ein Ablenkungsmanöver
- Das wahre Problem war FC-012 Audit Trail System Context-Fehler
- `@ActivateRequestContext` ist essentiell für Async-Audit-Operationen

**Debugging-Strategie:**
1. CI-Logs gründlich analysieren (nicht nur erste Fehlermeldung)
2. HTTP 500 Errors deuten auf Backend-Probleme hin, nicht Test-Assertions
3. AuditService Probleme können ganze Test-Suites zum Scheitern bringen