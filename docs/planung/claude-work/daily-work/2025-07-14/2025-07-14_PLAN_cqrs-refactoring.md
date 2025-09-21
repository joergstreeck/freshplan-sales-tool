# Plan für CQRS-Refactoring

**Datum:** 14.07.2025 13:45
**Erstellt von:** Claude & Jörg
**Status:** 📋 PLANUNG

## 1. Ausgangslage

### ✅ Aktueller Stand (Commit 2af6b7f):
- Backend kompiliert erfolgreich
- API funktioniert (Kunden werden geladen)
- Alle Enterprise Standards bis auf CQRS implementiert
- CustomerService.java: ~475 Zeilen (monolithisch)

### 🎯 Ziel:
- CQRS-Pattern sauber implementieren
- Dabei Funktionalität erhalten
- Balance zwischen Enterprise-Standards und Pragmatismus

## 2. Warum CQRS?

### Vorteile:
- **Trennung von Verantwortlichkeiten**: Lesen vs. Schreiben
- **Bessere Skalierbarkeit**: Read/Write können unabhängig optimiert werden
- **Klarere Struktur**: Explizite Commands und Queries
- **Testbarkeit**: Kleinere, fokussierte Services

### Nachteile:
- **Komplexität**: Mehr Klassen und Interfaces
- **Overhead**: Für kleine Anwendungen möglicherweise zu viel
- **Lernkurve**: Team muss Pattern verstehen

## 3. Kompromiss-Vorschlag

### Option 1: "CQRS Light" (EMPFOHLEN)
```
CustomerService (Facade - behält public API)
├── CustomerQueryService (alle Lesezugriffe)
└── CustomerCommandService (alle Schreibzugriffe)
```

**Vorteile:**
- Backward-kompatibel (keine API-Änderungen)
- Schrittweise Migration möglich
- Pragmatischer Ansatz

### Option 2: "Full CQRS"
```
Commands/
├── CreateCustomerCommand
├── UpdateCustomerCommand
└── DeleteCustomerCommand

Queries/
├── GetCustomerQuery
├── ListCustomersQuery
└── SearchCustomersQuery

Handlers/
├── CreateCustomerCommandHandler
├── GetCustomerQueryHandler
└── etc.
```

**Vorteile:**
- Vollständige Trennung
- Maximale Flexibilität

**Nachteile:**
- Viel Boilerplate
- Breaking Changes in API

## 4. Implementierungsplan (Option 1)

### Phase 1: Vorbereitung
1. **Branch erstellen**: `feature/cqrs-light`
2. **Tests sicherstellen**: Alle Tests müssen grün sein
3. **Backup-Punkt**: Tag setzen vor Start

### Phase 2: Service aufteilen
1. **CustomerQueryService erstellen**
   - Alle `get*`, `find*`, `search*` Methoden
   - Read-only Operationen
   - ~200 Zeilen

2. **CustomerCommandService erstellen**
   - `create*`, `update*`, `delete*` Methoden
   - Alle schreibenden Operationen
   - ~150 Zeilen

3. **CustomerService als Facade**
   - Behält alle public Methoden
   - Delegiert an Query/Command Services
   - ~100 Zeilen

### Phase 3: Schrittweise Migration
```java
// Alt (in CustomerService)
public CustomerResponse getCustomer(UUID id) {
    // 20 Zeilen Logik
}

// Neu (in CustomerService)
public CustomerResponse getCustomer(UUID id) {
    return queryService.getCustomer(id);
}

// Logik verschoben nach CustomerQueryService
public CustomerResponse getCustomer(UUID id) {
    // 20 Zeilen Logik (von CustomerService)
}
```

### Phase 4: Testing
1. **Unit Tests**: Für jeden Service
2. **Integration Tests**: End-to-End
3. **Manuelle Tests**: UI durchklicken

## 5. Kritische Punkte

### Was kann schiefgehen:
1. **Zirkuläre Dependencies**: Command braucht Query-Daten
2. **Transaction-Grenzen**: Müssen korrekt bleiben
3. **Merge-Konflikte**: Bei Copy-Paste Vorsicht!

### Sicherheitsmaßnahmen:
1. **Nach jedem Schritt kompilieren**
2. **Tests laufen lassen**
3. **Git commits klein halten**
4. **Bei Problemen: sofort stoppen**

## 6. Zeitschätzung

- Phase 1: 15 Minuten
- Phase 2: 45-60 Minuten
- Phase 3: 30-45 Minuten  
- Phase 4: 30 Minuten
- **Gesamt**: 2-3 Stunden

## 7. Entscheidungsfragen

1. **CQRS Light oder Full CQRS?**
2. **Jetzt starten oder erst andere Probleme lösen?**
3. **Akzeptanzkriterien definieren?**

## 8. Alternative: Kein CQRS

Falls wir entscheiden, CQRS nicht zu implementieren:
- CustomerService bleibt wie er ist
- Fokus auf andere Verbesserungen
- Enterprise Standards 9/10 ist auch OK

---

**Nächster Schritt:** Gemeinsame Entscheidung treffen