# Problem #16: CustomerMapperTest - Lösung für gemischte Strategie

## Problem-Analyse

Der `CustomerMapperTest` verwendet eine inkonsistente Mischung aus:
- `@QuarkusTest` (Integration Test)
- `@InjectMock` für CustomerRepository 
- `CustomerBuilder` via CDI Injection
- 6 `@Nested` Klassen mit 20+ Tests

## Warum kann kein reiner Unit-Test erstellt werden?

`CustomerMapper` ist ein CDI Bean (`@ApplicationScoped`) mit folgenden Abhängigkeiten:
- `CustomerRepository` (via `@Inject`)
- CDI-managed Lifecycle
- Transactionale Kontexte

**Konsequenz:** CustomerMapper kann NICHT direkt instantiiert werden (`new CustomerMapper()` funktioniert nicht).

## Empfohlene Lösung

### Option 1: Beibehaltung als @QuarkusTest mit klarer Struktur ✅

**Vorteile:**
- Test funktioniert bereits
- Testet realistisches Verhalten mit CDI
- CustomerBuilder kann weiter genutzt werden

**Nachteile:**
- Langsamer als Unit-Test
- Benötigt Quarkus-Container

### Option 2: Refactoring zu separaten Test-Klassen

Statt 6 @Nested Klassen → 6 separate Testklassen:

```java
CustomerMapperEntityToResponseTest.java
CustomerMapperRequestToEntityTest.java  
CustomerMapperEntityUpdateTest.java
CustomerMapperCollectionMappingTest.java
CustomerMapperCircularReferenceTest.java
CustomerMapperPerformanceTest.java
```

**Vorteile:**
- Bessere Testbarkeit
- Parallel ausführbar
- Klare Verantwortlichkeiten

### Option 3: Mapper-Refactoring für bessere Testbarkeit

CustomerMapper könnte refactored werden zu:
- Static utility methods (testbar ohne CDI)
- Separate Mapper-Klassen pro Richtung
- MapStruct statt manuelles Mapping

## Entscheidung

**Empfehlung: Option 1 - Status Quo beibehalten**

**Begründung:**
1. Test funktioniert und hat gute Coverage
2. Aufwand für Refactoring steht nicht im Verhältnis zum Nutzen
3. @Nested ist hier akzeptabel, da kein @TestTransaction verwendet wird
4. CustomerBuilder-Integration ist wertvoll für realistische Tests

## Kleine Verbesserungen die sofort umgesetzt werden können

1. **Test-Naming verbessern** gemäß Convention
2. **Redundante Mocks entfernen** (wenn nicht genutzt)
3. **Dokumentation ergänzen** warum @QuarkusTest nötig ist

## Langfristige Verbesserung (nach Sprint 2)

- Migration zu MapStruct für automatisches Mapping
- Dann wären reine Unit-Tests möglich
- Reduzierung des Boilerplate-Codes