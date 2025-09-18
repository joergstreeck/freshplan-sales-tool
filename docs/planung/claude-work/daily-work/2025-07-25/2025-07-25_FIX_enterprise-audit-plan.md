# Enterprise-Standard Fix Plan für FC-012 Audit Trail

## 🎯 Ziel
Alle Lombok-Abhängigkeiten entfernen und dabei Enterprise-Standard beibehalten.

## 📋 Probleme und Lösungen

### 1. AuditInterceptor.java
**Probleme:**
- Annotation `@Auditable` erwartet keine Parameter
- Fehlender Map-Import
- Falsche Map-Deklaration

**Lösungen:**
```java
// Zeile 25: Annotation ohne Parameter
@Auditable

// Zeile 13: Import hinzufügen
import java.util.Map;

// Zeile 187: Map-Deklaration entfernen
// (war nur ein Workaround)
```

### 2. AuditService.java
**Probleme:**
- Logger-Methoden im falschen Format
- AuditEvent als innere Klasse fehlt
- AuditConfiguration fehlt
- SecurityUtils, HttpServerRequest Injection

**Lösungen:**
```java
// Logger-Methoden anpassen:
log.info() → log.infof()
log.debug() → log.debugf()
log.error() → log.errorf()
log.warn() → log.warnf()

// AuditEvent als normale innere Klasse
public static class AuditEvent {
    private final AuditEntry entry;
    
    public AuditEvent(AuditEntry entry) {
        this.entry = entry;
    }
    
    public AuditEntry getEntry() {
        return entry;
    }
}
```

### 3. AuditEntry.java
**Probleme:**
- Fehlendes userComment Feld
- toBuilder() Methode fehlt

**Lösungen:**
- userComment zu Entity hinzufügen
- toBuilder() implementieren
- Builder erweitern

### 4. AuditResource.java
**Probleme:**
- AuditContext Import fehlt
- DATA_EXPORT EventType fehlt
- Map.of() Verwendung

**Lösungen:**
- Import ergänzen
- EventType in Enum hinzufügen
- Map.of() korrekt verwenden

### 5. AuditEventType.java
**Probleme:**
- Fehlende Event-Typen

**Lösungen:**
- GDPR_REQUEST hinzufügen
- DATA_EXPORT_STARTED/COMPLETED hinzufügen

## 🔧 Implementierungsreihenfolge

1. **AuditEventType** - Fehlende Enums ergänzen
2. **AuditEntry** - userComment & toBuilder
3. **AuditInterceptor** - Imports & Annotation
4. **AuditService** - Logger & innere Klassen
5. **AuditResource** - Imports & Fixes
6. **Tests** - Anpassungen nach Refactoring

## ✅ Validierung

Nach jedem Schritt:
```bash
cd backend
./mvnw compile
```

Nach allen Fixes:
```bash
./mvnw test -Dtest="*Audit*"
```

## 🎯 Erwartetes Ergebnis

- Keine Lombok-Abhängigkeiten mehr
- Alle Tests grün
- Code auf Enterprise-Standard
- Vollständig funktionsfähige Audit-Trail