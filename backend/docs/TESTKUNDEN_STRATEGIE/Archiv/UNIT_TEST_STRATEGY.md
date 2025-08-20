# Unit-Test Strategie für Entities mit new()

## Problem

Viele Unit-Tests erstellen Entities direkt mit `new Customer()` statt den CustomerBuilder zu verwenden.

## Analyse

### Wann ist `new Entity()` akzeptabel?

1. **Reine Unit-Tests** (ohne @QuarkusTest)
2. **Mapper-Tests** die nur Transformation testen
3. **Validator-Tests** die nur Validation Logic testen
4. **Helper/Utility Tests** ohne DB-Interaktion

### Wann MUSS CustomerBuilder verwendet werden?

1. **Integration Tests** mit @QuarkusTest
2. **Repository Tests** mit DB-Interaktion
3. **Service Tests** die realistische Daten brauchen
4. **Tests mit @TestTransaction**

## Best Practice Guidelines

### Für Unit-Tests (OHNE @QuarkusTest)

```java
// ✅ OK für reine Unit-Tests
@Test
class CustomerValidatorTest {
    @Test
    void validateCustomer_withInvalidData_shouldThrowException() {
        // Direct instantiation ist OK für Unit-Tests
        Customer customer = new Customer();
        customer.setCompanyName(null);
        
        // Test validation logic
        assertThatThrownBy(() -> validator.validate(customer))
            .isInstanceOf(ValidationException.class);
    }
}
```

### Für Integration-Tests (MIT @QuarkusTest)

```java
// ✅ RICHTIG - CustomerBuilder verwenden
@QuarkusTest
class CustomerServiceTest {
    @Inject CustomerBuilder customerBuilder;
    
    @Test
    void createCustomer_withValidData_shouldPersist() {
        // Builder für realistische Test-Daten
        Customer customer = customerBuilder
            .withCompanyName("[TEST] Hotel GmbH")
            .withStatus(CustomerStatus.AKTIV)
            .build();
            
        // Test service
        service.create(customer);
    }
}
```

## Migration Strategy

### Phase 1: Identifikation (DONE)
- Unit-Tests die `new Customer()` verwenden identifiziert
- Keine kritischen Fehler gefunden

### Phase 2: Priorisierung
- **HIGH**: Tests die in CI fehlschlagen → Sofort migrieren
- **MEDIUM**: Integration Tests mit new() → Bei nächster Änderung migrieren  
- **LOW**: Reine Unit-Tests → Können bleiben wie sie sind

### Phase 3: Schrittweise Migration
- Nur bei Bedarf (Test-Fehler, neue Features)
- Opportunistisch beim Refactoring
- KEINE Massen-Migration (zu riskant)

## Entscheidung

**Status: KEINE SOFORTIGE ACTION NÖTIG**

**Begründung:**
1. Tests funktionieren aktuell
2. `new Customer()` in Unit-Tests ist technisch korrekt
3. Migration nur bei konkretem Bedarf
4. Fokus auf wichtigere Probleme

## Empfehlungen

### Für neue Tests:
- **Integration Tests**: IMMER CustomerBuilder
- **Unit Tests**: new() ist OK, Builder ist besser
- **Dokumentation**: Kommentar warum new() verwendet wird

### Für bestehende Tests:
- **Keine proaktive Migration**
- **Bei Test-Fehlern**: Auf Builder umstellen
- **Bei Feature-Änderungen**: Opportunistisch migrieren

## Test-Kategorisierung

### Tests die new Customer() verwenden (OK für Unit-Tests):
- CustomerMapperTest (hybrid, funktioniert)
- CustomerValidatorTest (falls vorhanden)
- DTO Converter Tests
- Utility Tests

### Tests die CustomerBuilder verwenden MÜSSEN:
- CustomerRepositoryTest ✅
- CustomerServiceIntegrationTest ✅
- CustomerResourceIT ✅
- Alle Tests mit @TestTransaction ✅