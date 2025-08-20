# TestDataBuilder Migration - Verbleibende Tests

**Stand: 18.08.2025**  
**Status: 30-40% abgeschlossen**  
**Verbleibende Tests: 34 Dateien**

## 📊 Übersicht

### Migration abgeschlossen ✅
- **30 Tests** verwenden bereits TestDataBuilder
- **17 kritische Probleme** wurden gelöst
- **Builder-Pattern** erfolgreich etabliert

### Migration ausstehend ❌
- **34 Test-Dateien** verwenden noch direkte Konstruktoren
- Geschätzt **500+ Stellen** im Code müssen angepasst werden

---

## 🔴 KRITISCH: Tests mit direkten Konstruktoren (Höchste Priorität)

Diese Tests sollten zuerst migriert werden, da sie die meisten direkten Konstruktor-Aufrufe haben:

### Customer-Tests (direkte `new Customer()` Aufrufe)

1. **CustomerCommandServiceTest.java**
   - Verwendet: `new Customer()` mindestens 2x
   - Migration: CustomerBuilder verwenden
   - Priorität: HOCH (Command Service ist kritisch)

2. **CustomerRepositoryTest.java.disabled**
   - Status: Deaktiviert
   - Migration: Reaktivieren und mit CustomerBuilder migrieren
   - Priorität: MITTEL

3. **TimelineCommandServiceTest.java**
   - Verwendet: `new Customer()` für Test-Setup
   - Migration: CustomerBuilder verwenden
   - Priorität: HOCH

4. **ContactQueryServiceTest.java**
   - Verwendet: Mock Customer Objekte
   - Migration: CustomerBuilder für Test-Daten
   - Priorität: MITTEL

5. **SearchQueryServiceTest.java**
   - Verwendet: `new Customer()` für Such-Tests
   - Migration: CustomerBuilder mit verschiedenen Szenarien
   - Priorität: MITTEL

6. **HtmlExportQueryServiceTest.java**
   - Verwendet: Test-Customer für Export
   - Migration: CustomerBuilder verwenden
   - Priorität: NIEDRIG

7. **SalesCockpitQueryServiceTest.java**
   - Verwendet: Customer für Dashboard-Tests
   - Migration: CustomerBuilder mit realistischen Daten
   - Priorität: HOCH

### Opportunity-Tests (direkte `new Opportunity()` Aufrufe)

8. **OpportunityCommandServiceTest.java**
   - Verwendet: `new Opportunity()` mehrfach
   - Migration: OpportunityBuilder verwenden
   - Priorität: HOCH

9. **OpportunityQueryServiceTest.java**
   - Verwendet: Mock Opportunities
   - Migration: OpportunityBuilder verwenden
   - Priorität: HOCH

10. **OpportunityEntityStageTest.java**
    - Verwendet: `new Opportunity()` für Entity-Tests
    - Migration: OpportunityBuilder.build() (ohne persist)
    - Priorität: MITTEL

11. **OpportunityStageTest.java**
    - Verwendet: Direkte Opportunity-Erstellung
    - Migration: OpportunityBuilder für Stage-Tests
    - Priorität: NIEDRIG

12. **OpportunityMapperTest.java**
    - Verwendet: `new Opportunity()` für Mapper-Tests
    - Migration: OpportunityBuilder.build()
    - Priorität: MITTEL

### User-Tests (direkte `new User()` Aufrufe)

13. **UserCommandServiceTest.java**
    - Verwendet: `new User()` mehrfach
    - Migration: UserBuilder verwenden
    - Priorität: HOCH

14. **UserQueryServiceTest.java**
    - Verwendet: Mock User Objekte
    - Migration: UserBuilder verwenden
    - Priorität: HOCH

15. **UserServiceTest.java**
    - Verwendet: `new User()` in Helper-Methoden
    - Migration: UserBuilder verwenden
    - Priorität: HOCH

16. **UserServiceRolesTest.java**
    - Verwendet: User für Rollen-Tests
    - Migration: UserBuilder mit verschiedenen Rollen
    - Priorität: MITTEL

17. **UserMapperTest.java**
    - Verwendet: `new User()` für Mapper-Tests
    - Migration: UserBuilder.build()
    - Priorität: MITTEL

18. **UserEntityTest.java**
    - Verwendet: Direkte User-Erstellung
    - Migration: UserBuilder für Entity-Tests
    - Priorität: NIEDRIG

19. **UserRepositoryTest.java**
    - Verwendet: `new User()` für Repository-Tests
    - Migration: UserBuilder.persist()
    - Priorität: HOCH

20. **UserResourceIT.java**
    - Verwendet: Test-User für API-Tests
    - Migration: UserBuilder für Integration-Tests
    - Priorität: HOCH

21. **UserRolesIT.java**
    - Verwendet: User mit verschiedenen Rollen
    - Migration: UserBuilder mit Rollen-Szenarien
    - Priorität: MITTEL

---

## 🟡 MITTEL: Tests mit gemischter Verwendung

Diese Tests verwenden teilweise Builder, aber auch noch direkte Konstruktoren:

22. **OpportunityServiceMockTest.java**
    - Status: Verwendet CustomerBuilder, aber `new Opportunity()`
    - Migration: Vollständig auf Builder umstellen
    - Priorität: MITTEL

23. **SalesCockpitServiceIntegrationTest.java**
    - Status: Gemischte Verwendung
    - Migration: Konsequent Builder verwenden
    - Priorität: MITTEL

24. **CustomerContactTest.java**
    - Status: Entity-Test mit direkten Konstruktoren
    - Migration: ContactBuilder erstellen und verwenden
    - Priorität: NIEDRIG

25. **SearchServiceTest.java**
    - Status: Teilweise Builder
    - Migration: Vollständig migrieren
    - Priorität: MITTEL

---

## 🟢 NIEDRIG: Legacy und Test-Utilities

Diese können später migriert werden:

26. **TestDataBuilder.java** (alt)
    - Status: Legacy Test-Builder
    - Migration: Durch neue Builder ersetzen
    - Priorität: NIEDRIG

27. **TestFixtures.java**
    - Status: Alte Test-Fixtures
    - Migration: Durch Builder-basierte Fixtures ersetzen
    - Priorität: NIEDRIG

28. **TestDataInitializer.java**
    - Status: Initialisiert Test-Daten beim Start
    - Migration: Builder verwenden
    - Priorität: NIEDRIG

29. **TestDataDisciplineRules.java.disabled**
    - Status: Deaktiviert
    - Migration: Eventuell löschen oder reaktivieren
    - Priorität: SEHR NIEDRIG

30. **UserServiceCQRSIntegrationTest.java**
    - Status: CQRS-Test mit direkten Konstruktoren
    - Migration: Builder verwenden
    - Priorität: MITTEL

31. **UserRepoSaveLoadIT.java** (Greenpath)
    - Status: Greenpath Test
    - Migration: UserBuilder verwenden
    - Priorität: NIEDRIG

---

## 📋 Migration Checkliste pro Test

Bei der Migration jedes Tests sollten folgende Schritte durchgeführt werden:

### 1. Analyse
- [ ] Alle `new Entity()` Aufrufe identifizieren
- [ ] Prüfen ob Mock oder echte DB-Interaktion benötigt wird
- [ ] Entscheiden: `.build()` (ohne DB) oder `.persist()` (mit DB)

### 2. Migration
- [ ] Builder injizieren: `@Inject CustomerBuilder customerBuilder;`
- [ ] Direkte Konstruktoren ersetzen
- [ ] Test-Isolation sicherstellen (testRunId verwenden)
- [ ] @TestTransaction für automatisches Rollback

### 3. Verbesserung
- [ ] Mock-Tests zu Integration-Tests umschreiben (wo sinnvoll)
- [ ] @InjectMock entfernen und echte DB verwenden
- [ ] Realistische Test-Szenarien mit Builder-Methoden

### 4. Validierung
- [ ] Test läuft erfolgreich
- [ ] Keine direkten Konstruktoren mehr
- [ ] Test-Daten werden korrekt markiert (isTestData)
- [ ] Cleanup funktioniert (wenn nötig)

---

## 🎯 Migrations-Strategie

### Phase 4: High-Priority Migration (1-2 Tage)
- Alle Command/Query Services (10 Tests)
- Alle Repository Tests (5 Tests)
- Geschätzte Zeit: 2-3 Stunden

### Phase 5: Medium-Priority Migration (2-3 Tage)
- Alle Mapper Tests (3 Tests)
- Alle Integration Tests (8 Tests)
- Geschätzte Zeit: 3-4 Stunden

### Phase 6: Low-Priority Migration (1 Woche)
- Entity Tests (5 Tests)
- Utility Classes (3 Tests)
- Legacy Code Cleanup
- Geschätzte Zeit: 2-3 Stunden

### Phase 7: Final Cleanup
- Alte TestDataBuilder löschen
- TestFixtures modernisieren
- Dokumentation aktualisieren
- Code Review

---

## 📈 Fortschritt-Tracking

```
Gesamt: 34 Tests zu migrieren
✅ Abgeschlossen: 0/34 (0%)
🔄 In Arbeit: 0/34
❌ Ausstehend: 34/34

Prioritäten:
🔴 HOCH: 12 Tests
🟡 MITTEL: 15 Tests  
🟢 NIEDRIG: 7 Tests
```

---

## 🚀 Nächste Schritte

1. **Sofort**: Diese Dokumentation im Team besprechen
2. **Diese Woche**: Phase 4 starten (High-Priority Tests)
3. **Nächste Woche**: Phase 5 und 6 durchführen
4. **Ziel**: Bis Ende August 2025 vollständig migriert

---

## 💡 Tipps für die Migration

1. **Batch-Migration**: Ähnliche Tests zusammen migrieren
2. **Pair Programming**: Zu zweit geht's schneller
3. **Automatisierung**: Regex-Replace für einfache Fälle
4. **Test-First**: Erst Test laufen lassen, dann migrieren
5. **Commit oft**: Kleine, atomare Commits

---

## 📝 Notizen

- Die Migration ist technisch einfach, aber zeitaufwändig
- Viele Tests können von Mock zu Integration migriert werden
- Dies ist eine gute Gelegenheit, Test-Qualität zu verbessern
- Builder-Pattern macht Tests lesbarer und wartbarer

**Letzte Aktualisierung**: 18.08.2025  
**Autor**: Claude  
**Review**: Ausstehend