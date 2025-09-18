# 📋 PLAN: Systematisches CI Integration Test Debugging

**Datum:** 2025-07-26 00:50
**Zweck:** Strukturierter Ansatz für CI-Fehler-Behebung
**Status:** READY FOR EXECUTION

## 🎯 Zielsetzung
**HAUPTZIEL:** CI Integration Tests dauerhaft GRÜN bekommen
**NEBENEFFEKT:** Robuste Test-Isolation für zukünftige Entwicklung

## 📊 Aktuelle Situation
- ✅ FC-012 Audit Trail System: FUNKTIONIERT
- ✅ Backend CI: GRÜN
- ❌ Integration Tests: ROT (UserNotFoundException)
- 🔄 Mehrere Race-Condition-Fixes fehlgeschlagen

## 🔬 PHASE 1: Erweiterte Diagnose (15 Min)

### 1.1 Aktuelle CI-Logs detailliert analysieren
```bash
# Neueste failed run ID holen
gh run list --branch feature/m4-renewal-stage-implementation --limit 1

# Vollständige Logs mit Fokus auf Test-Reihenfolge
gh run view [RUN_ID] --log-failed > ci-debug-full.log

# Pattern-Analyse
grep -A 10 -B 10 "UserNotFoundException\|testDeleteUser\|testUpdateUser" ci-debug-full.log
```

### 1.2 Test-Execution-Order verstehen
- Welche Tests laufen parallel?
- Welche Tests teilen sich DB-State?
- Wann genau tritt UserNotFoundException auf?

## 🧪 PHASE 2: Erweiterte Debug-Instrumentierung (20 Min)

### 2.1 UserResourceITDebug erweitern
```java
@TestMethodOrder(OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserResourceITDebug {
    
    @Order(1)
    @Test
    void debug_DatabaseState_Before() {
        // Log: Anzahl Users, alle User-IDs
        // Log: Thread-Info, Timing
    }
    
    @Order(2) 
    @Test
    void debug_CreateAndDelete_Sequence() {
        // Step-by-step mit DB-State-Checks
    }
    
    @Order(3)
    @Test  
    void debug_ParallelExecution_Test() {
        // Parallel execution simulation
    }
}
```

### 2.2 Transaction-Boundary-Debugging
```java
@BeforeEach
void debugSetUp() {
    System.out.println("=== THREAD: " + Thread.currentThread().getId());
    System.out.println("=== TRANSACTION STATUS: " + transactionStatus);
    System.out.println("=== DB USER COUNT: " + userRepository.count());
    
    // Log alle existierenden User-IDs
    userRepository.listAll().forEach(u -> 
        System.out.println("=== EXISTING USER: " + u.getId() + " - " + u.getUsername()));
}
```

## 🔧 PHASE 3: Alternative Lösungsansätze (30 Min)

### 3.1 Test-Isolation-Strategien testen
```java
// Option A: Sequential Execution
@Execution(ExecutionMode.SAME_THREAD)

// Option B: Per-Class Lifecycle  
@TestInstance(TestInstance.Lifecycle.PER_CLASS)

// Option C: Database per Thread
@TestProfile("test-isolated")
```

### 3.2 Transactional Test Boundaries
```java
@TestTransaction // Rollback nach jedem Test
@Transactional(Transactional.TxType.REQUIRES_NEW) // Neue Transaktion
```

## 📈 PHASE 4: Implementierung & Validierung (30 Min)

### 4.1 Schrittweise Implementation des besten Ansatzes
1. Einen Ansatz implementieren
2. CI-Test durchführen  
3. Logs analysieren
4. Bei Erfolg: Alle Tests aktivieren
5. Bei Misserfolg: Nächsten Ansatz versuchen

### 4.2 Erfolgs-Validierung
- 3x hintereinander grüne CI-Läufe
- Keine Race Conditions mehr
- Robuste Test-Isolation bestätigt

## 🚨 NOTFALL-PLAN
Falls systematisches Debugging nicht hilft:
1. **UserResourceIT komplett disablen** (`@Disabled`)
2. **Separate Test-Klasse** nur für kritische User-Tests
3. **Mock-basierte Tests** statt Integration Tests
4. **Issue für Architectural Review** erstellen

## 📝 DOKUMENTATIONS-PFLICHT
- Jeden Schritt in Debug-Markdown dokumentieren
- CI-Logs als Artifacts speichern  
- Erfolgreiche Lösung in CLAUDE.md eintragen
- TODO-Liste kontinuierlich aktualisieren

---
**⏰ ZEITBUDGET: Max. 1.5 Stunden**
**🎯 ERFOLGSKRITERIUM: CI Integration Tests GRÜN**