# 🎯 Nächster Schritt für PR #5 CQRS Refactoring

**Stand:** 13.08.2025 21:00  
**Aktueller Fortschritt:** 57% von Phase 1 (4 von 7 Command-Methoden fertig)

## ✅ Was wurde zuletzt gemacht

### restoreCustomer() Methode implementiert:
- ✅ Exakte Kopie aus CustomerService (Zeilen 263-303)
- ✅ Nutzt `findByIdOptional()` um auch gelöschte Kunden zu finden
- ✅ Business Rule: IllegalStateException wenn Kunde nicht gelöscht ist
- ✅ Timeline Event "CUSTOMER_RESTORED" mit ImportanceLevel.HIGH
- ✅ Integration Tests geschrieben und erfolgreich

## 🔥 Als nächstes: addChildCustomer()

### Original-Methode in CustomerService.java (Zeilen 372-412):
```java
@Transactional
public CustomerResponse addChildCustomer(UUID parentId, UUID childId, String updatedBy) {
    // 1. Beide Kunden laden (nur aktive)
    // 2. Business Rule: Child darf noch keinen Parent haben
    // 3. Business Rule: Keine zirkulären Referenzen (isDescendant check)
    // 4. Parent setzen und speichern
    // KEIN Timeline Event in dieser Methode!
}
```

### Wichtige Details:
- **Zwei CustomerNotFoundException** mit unterschiedlichen Messages ("Parent customer not found" vs "Child customer not found")
- **isDescendant()** Helper-Methode wird benötigt (Zeilen 622-630)
- **KEIN Timeline Event** wird erstellt (anders als bei anderen Methoden!)
- Nutzt `findByIdActive()` für beide Kunden

### Zu implementieren in CustomerCommandService:
1. Die Methode als exakte Kopie implementieren
2. Die Helper-Methode `isDescendant()` ebenfalls kopieren
3. Integration Test schreiben der beide Services vergleicht

## 📋 Verbleibende Aufgaben

### Phase 1 - CustomerCommandService (noch 3 Methoden):
- [ ] `addChildCustomer(UUID parentId, UUID childId, String updatedBy)`
- [ ] `updateAllRiskScores()` - Wartungsoperation, durchläuft alle Kunden
- [ ] `mergeCustomers(UUID targetId, UUID sourceId, String mergedBy)` - Komplexeste Methode

### Phase 1 - CustomerQueryService (8 Methoden):
- [ ] `getCustomer(UUID)`
- [ ] `getAllCustomers(int page, int size)`
- [ ] `getCustomersByStatus(CustomerStatus, int page, int size)`
- [ ] `getCustomersByIndustry(Industry, int page, int size)`
- [ ] `getCustomerHierarchy(UUID)`
- [ ] `getCustomersAtRisk(int minRiskScore, int page, int size)`
- [ ] `getOverdueFollowUps(int page, int size)`
- [ ] `getDashboardData()`

### Phase 1 - CustomerResource:
- [ ] Feature Flag `features.cqrs.enabled` einbauen
- [ ] Facade Pattern für nahtloses Switching

## ⚠️ Wichtige Erkenntnisse

1. **KEINE Domain Events** - nur Timeline Events direkt in DB
2. **Timeline Events brauchen Category** - NOT NULL constraint!
3. **findByIdOptional()** für gelöschte Kunden, **findByIdActive()** für normale Operationen
4. **Nicht alle Methoden erstellen Timeline Events** - addChildCustomer() tut es nicht!

## 🔧 Test-Befehle

```bash
# Spezifische Tests ausführen:
MAVEN_OPTS="-Dmaven.multiModuleProjectDirectory=$PWD" ./mvnw test -Dtest=CustomerCommandServiceIntegrationTest

# Alle Tests:
MAVEN_OPTS="-Dmaven.multiModuleProjectDirectory=$PWD" ./mvnw test
```

## 📍 Dateien

- **Hauptdatei:** `/backend/src/main/java/de/freshplan/domain/customer/service/command/CustomerCommandService.java`
- **Test-Datei:** `/backend/src/test/java/de/freshplan/domain/customer/service/command/CustomerCommandServiceIntegrationTest.java`
- **Original:** `/backend/src/main/java/de/freshplan/domain/customer/service/CustomerService.java`

---

**Für den nächsten Claude:** Setze mit `addChildCustomer()` fort. Denke daran, auch die `isDescendant()` Helper-Methode zu kopieren!