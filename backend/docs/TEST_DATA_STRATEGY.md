# Test Data Strategy

## Übersicht

Unsere Test-Daten-Strategie basiert auf einer **hybriden Lösung**, die das Beste aus beiden Welten kombiniert:

1. **Minimale SQL-Migration** für absolut notwendige Basis-Daten
2. **Java-basierter TestDataService** für umfangreiche, typsichere Test-Daten

## Warum diese Lösung?

### ❌ Problem mit reinen SQL-Migrationen:
- Keine Typ-Sicherheit (Enum-Werte können falsch sein)
- Schwer wartbar bei vielen Test-Daten
- Keine Flexibilität zur Laufzeit
- Versionskonflikte bei paralleler Entwicklung

### ✅ Vorteile unserer Lösung:
- **Type-safe**: Compiler prüft alle Enum-Werte
- **Wartbar**: Änderungen in einer Java-Klasse
- **Flexibel**: Test-Daten können zur Laufzeit erstellt werden
- **Idempotent**: Prüft vor Erstellung, ob Daten existieren
- **Umgebungs-spezifisch**: Läuft nur in dev/test

## Architektur

```
┌─────────────────────────────────────┐
│         V219 Migration              │
│    (5 Basis-Test-Kunden)           │
│  - Minimal für Integration Tests    │
└─────────────────────────────────────┘
                 ↓
┌─────────────────────────────────────┐
│       TestDataService.java          │
│   (58+ umfangreiche Test-Daten)    │
│  - Type-safe                        │
│  - Erweiterbar                      │
│  - Profile-basiert                  │
└─────────────────────────────────────┘
                 ↓
┌─────────────────────────────────────┐
│      TestDataResource REST API      │
│  POST /api/test-data/init          │
│  DELETE /api/test-data             │
│  GET /api/test-data/status         │
└─────────────────────────────────────┘
```

## Verwendung

### 1. Automatische Initialisierung

Test-Daten werden automatisch beim Start erstellt:
- **Profile**: `dev` oder `test`
- **Bedingung**: Weniger als 58 Test-Kunden vorhanden

### 2. Manuelle Initialisierung

```bash
# Test-Daten erstellen
curl -X POST http://localhost:8080/api/test-data/init

# Test-Daten löschen (nur im test-Profile)
curl -X DELETE http://localhost:8080/api/test-data

# Status abfragen
curl http://localhost:8080/api/test-data/status
```

### 3. Konfiguration

```properties
# application.properties
freshplan.test-data.enabled=true
freshplan.test-data.min-customers=58
```

## Neue Test-Daten hinzufügen

### Schritt 1: TestDataService erweitern

```java
// In TestDataService.java
private List<Customer> createTestCustomers() {
    // ...
    
    // Neues Test-Szenario hinzufügen
    customers.add(createTestCustomer(
        "TEST-099", 
        "[TEST] Neues Szenario",
        Industry.NEUE_BRANCHE,
        CustomerStatus.NEUER_STATUS,
        999999.00,
        50
    ));
    
    return customers;
}
```

### Schritt 2: Tests schreiben

```java
@Test
void testNewScenario() {
    // Test mit neuem Test-Kunden
    Customer testCustomer = customerRepository
        .find("customerNumber", "TEST-099")
        .firstResult();
        
    assertThat(testCustomer).isNotNull();
    // ...
}
```

## Best Practices

### ✅ DO:
- Test-Daten im Java-Code pflegen (TestDataService)
- Neue Szenarien als Methoden hinzufügen
- Test-Daten mit `[TEST]` Präfix kennzeichnen
- `is_test_data=true` Flag setzen

### ❌ DON'T:
- Neue Flyway-Migrationen für Test-Daten erstellen
- Test-Daten in Produktion erstellen
- Hard-coded Test-Daten in Tests
- Test-Daten ohne Präfix erstellen

## Migration von alten Test-Daten

Falls alte Test-Daten-Migrationen existieren:

1. **Backup erstellen** der existierenden Test-Daten
2. **TestDataService erweitern** mit den Daten aus der Migration
3. **Migration löschen** oder auskommentieren
4. **Tests ausführen** um sicherzustellen, dass alle Daten vorhanden sind

## Troubleshooting

### Problem: Test-Daten werden nicht erstellt
```bash
# Logs prüfen
grep "TestDataService" logs/quarkus.log

# Manuell initialisieren
curl -X POST http://localhost:8080/api/test-data/init
```

### Problem: Falsche Enum-Werte
```java
// In TestDataService.java
// IDE zeigt sofort Fehler bei falschen Enum-Werten
customer.setIndustry(Industry.FALSCH); // Compile-Error!
```

### Problem: Zu viele/wenige Test-Daten
```properties
# application-test.properties anpassen
freshplan.test-data.min-customers=100
```

## Fazit

Diese Lösung bietet:
- **Robustheit**: Type-safe und compiler-geprüft
- **Wartbarkeit**: Zentrale Stelle für alle Test-Daten
- **Flexibilität**: Einfach erweiterbar
- **Sicherheit**: Keine Test-Daten in Produktion

Dies ist die **Best Practice** für Test-Daten-Management in modernen Java-Anwendungen.