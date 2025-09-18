# 🧭 NEXT STEP NAVIGATION

**Zweck:** Immer nur EINEN klaren nächsten Schritt für Claude
**Update:** Bei jeder Unterbrechung oder Fortschritt

---

## 🎯 JETZT GERADE:

**INFRASTRUKTUR-PLANUNGSEXTRAKTION ABGESCHLOSSEN**

**Stand 18.09.2025 13:51:**
- ✅ **Dokumentations-Bereinigung:** /docs komplett aufgeräumt, 138 Backup-Files archiviert
- ✅ **8 Infrastructure Plans extrahiert:** Nach PLANUNGSMETHODIK aus grundlagen/ → infrastruktur/
- ✅ **PLANUNGSMETHODIK umgesetzt:** Atomare Pläne, Executive Summaries, 4-Phasen-Struktur
- ✅ **Master Index erstellt:** Zentrale Koordination aller Infrastructure-Pläne
- ✅ **Cross-References aktualisiert:** Scripts + Dokumentation nutzen neue Struktur
- ✅ **Claude-Optimization:** Automatische Dokumentation in `/docs/planung/claude-work/`
- 🔄 **Feature-Branch offen:** `feature/routing-improvements`
- ✅ **ABGESCHLOSSEN:** CRM_COMPLETE_MASTER_PLAN_V5.md kompakt refactoriert nach PLANUNGSMETHODIK
- ⚡ **NÄCHSTER SCHRITT:** Phase 3 UMBAU - Tests zu TestDataBuilder migrieren

**System-Status:**
- ✅ **Migrationen:** V10000-V10005 vollständig implementiert
- ✅ **CI-Guards:** ci.build Flag überall korrekt
- ✅ **Dokumentation:** Vollständig aktualisiert
- ✅ **TestDataBuilder:** Vollständig implementiert mit allen Builders!

### 🚨 NÄCHSTER SCHRITT FÜR NEUEN CLAUDE:

**PHASE 3: UMBAU - Tests zu TestDataBuilder migrieren**

```bash
# 1. Status prüfen:
# TestDataBuilder EXISTIERT bereits in:
ls -la backend/src/main/java/de/freshplan/test/
ls -la backend/src/main/java/de/freshplan/test/builders/

# 2. Betroffene Tests identifizieren:
grep -r "new Customer()" backend/src/test/java --include="*.java" | wc -l
# Ca. 233 Stellen müssen migriert werden

# 3. Migration starten mit:
# - Für Unit-Tests: testDataBuilder.customer().build()
# - Für Integration-Tests: testDataBuilder.customer().persist()
```

**✅ Bereits implementierte Features:**
- `build()` - Nur Objekt-Erstellung (für Unit-Tests) ✅
- `persist()` - Mit DB-Persistierung (für Integration-Tests) ✅
- Automatische is_test_data=true Markierung ✅
- Unique IDs mit Timestamp + Counter + Thread-ID ✅
- Vordefinierte Szenarien (asPremium(), asRisk(), etc.) ✅
- PermissionHelperPg für race-safe Permission-Erstellung ✅

### ⚠️ KRITISCHE HINWEISE:
- **Nächste Migration:** V225 (V224 ist die höchste)
- **PermissionHelperPg** bereits vorbereitet in src/main/java/de/freshplan/test/helpers/
- **233 Test-Stellen** müssen später migriert werden
- **@ApplicationScoped** für CDI-Injection verwenden

### 📊 TODO-Status:
1. **testdata-1** (✅ completed): TestDataBuilder mit build()/persist() implementiert
2. **testdata-2** (✅ completed): PermissionHelperPg fertiggestellt und getestet  
3. **testdata-3** (🔄 pending): Tests zu TestDataBuilder migrieren (233 Stellen)
4. **testdata-4** (🔄 pending): CI Pipeline grün bekommen
5. **testdata-5** (🔄 pending): Performance-Verbesserung validieren (>30%)

---

**Navigation:**
⬅️ Letzte Übergabe: [`2025-09-17_HANDOVER_19-14.md`](/docs/claude-work/daily-work/2025-09-17/)
📚 Hauptdoku: [`TESTKUNDEN_STRATEGIE/`](/backend/docs/TESTKUNDEN_STRATEGIE/)