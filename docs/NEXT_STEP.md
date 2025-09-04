# 🧭 NEXT STEP NAVIGATION

**Zweck:** Immer nur EINEN klaren nächsten Schritt für Claude
**Update:** Bei jeder Unterbrechung oder Fortschritt

---

## 🎯 JETZT GERADE:

**PHASE 0, 1 & 2 ABGESCHLOSSEN - BEREIT FÜR PHASE 3 (UMBAU)**

**Stand 17.08.2025 19:00:**
- ✅ **Phase 0 komplett:** Alle Migrationen V10000-V10005 implementiert
- ✅ **Phase 1 komplett:** Alle alten Initializers bereits entfernt
- ✅ **Phase 2 komplett:** TestDataBuilder mit allen Builders implementiert!
- ✅ **CI-Pipeline verbessert:** Smoke-Tests an zwei Stellen
- ✅ **Team-Feedback adressiert:** Alle kritischen Punkte behoben
- ⚡ **NÄCHSTER SCHRITT:** Phase 3 - Tests migrieren zu TestDataBuilder!

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
⬅️ Letzte Übergabe: [`2025-08-17_HANDOVER_18-21.md`](/docs/claude-work/daily-work/2025-08-17/)  
📚 Hauptdoku: [`TESTKUNDEN_STRATEGIE/`](/backend/docs/TESTKUNDEN_STRATEGIE/)