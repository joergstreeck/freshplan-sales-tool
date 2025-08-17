# Migration Learnings - Phase 3

## 🎯 Stand: 4 von 27 Low-Risk Tests migriert

### ✅ Erfolgreich migrierte Tests

1. **ContactRepositoryTest** - Helper-Methoden mit build()
2. **CustomerRepositoryTest** - 2 Helper-Methoden, Prefix-Override nötig
3. **ContactPerformanceTest** - setUp() Methode
4. **SearchCQRSIntegrationTest** - Inline-Loop und Helper-Methode

### 📚 Wichtige Erkenntnisse

#### 1. CustomerBuilder Verhalten
- **Problem**: CustomerBuilder fügt automatisch `[TEST-xxx]` Prefix zum Company Name hinzu
- **Lösung**: Nach `build()` muss `setCompanyName()` aufgerufen werden um Prefix zu entfernen
```java
Customer customer = customerBuilder
    .withCompanyName(name)
    .build();
customer.setCompanyName(name); // Override to remove prefix
```

#### 2. Helper-Methoden Pattern
- Tests nutzen oft `createTestCustomer()` Helper-Methoden
- Diese verwenden `build()` statt `persist()` - Tests machen Persistierung selbst
- Helper-Methoden überschreiben oft generierte Werte (customerNumber, companyName)

#### 3. CustomerBuilder API
- `withType()` nicht `withCustomerType()`
- `withFinancingType()` nicht `withPrimaryFinancing()`
- Wichtig: Dokumentation der verfügbaren Methoden prüfen

#### 4. ID-Längen Problem
- **Problem**: Auto-generierte IDs waren zu lang für varchar(20) Constraint
- **Lösung**: TestDataUtils.uniqueId() angepasst für kürzere IDs

#### 5. Injection Pattern
- CustomerBuilder direkt injizieren (nicht TestDataBuilder wegen CDI)
- Injection nach anderen `@Inject` Statements platzieren
```java
@Inject CustomerRepository repository;
@Inject CustomerBuilder customerBuilder;
```

### ⚠️ Probleme bei automatisierter Migration

1. **Komplexe Test-Strukturen**: Tests haben sehr unterschiedliche Patterns
2. **Helper-Methoden mit Parametern**: Benötigen individuelle Anpassung
3. **Transaction-Annotationen**: `@TestTransaction` auf Helper-Methoden kann Probleme verursachen
4. **Verschiedene Builder-Methoden**: Nicht alle Fields haben intuitive Methoden-Namen

### 🎯 Empfohlene Strategie

1. **Manuelle Migration** für besseres Verständnis der Patterns
2. **Schrittweise Verbesserung** des Migrations-Scripts
3. **Fokus auf Qualität** statt Geschwindigkeit
4. **Dokumentation** aller Learnings für spätere Automatisierung

### 🔄 Nächste Schritte

1. Weitere manuelle Migrationen für Pattern-Erkennung
2. Script-Verbesserung basierend auf neuen Erkenntnissen
3. Batch-Migration nur für wirklich einfache Tests
4. Medium- und High-Risk Tests definitiv manuell

### 📊 Migrations-Statistik

- **Erfolgreich**: 4/27 (14.8%)
- **Geschätzte Zeit pro Test**: ~5-10 Minuten
- **Komplexität**: Höher als erwartet
- **Automatisierbarkeit**: Teilweise möglich, aber limitiert

### 🚨 Wichtige Hinweise

- OpportunityRepositoryTest hat andere Probleme (Test-Setup)
- Nicht alle Test-Fehler sind auf Customer-Migration zurückzuführen
- Manche Tests benötigen tiefgreifendere Anpassungen