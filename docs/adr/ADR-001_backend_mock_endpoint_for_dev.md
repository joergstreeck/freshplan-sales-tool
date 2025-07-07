# ADR-001: Backend Mock-Endpunkte für Entwicklungsumgebung

**Status:** Akzeptiert  
**Datum:** 2025-01-07  
**Entscheider:** Entwicklungsteam  

## Kontext

Für die Entwicklung des Sales Cockpit Dashboards benötigen wir Testdaten, um die 
Frontend-Komponenten entwickeln und testen zu können. Es stehen zwei Optionen zur 
Verfügung:

1. Mock-User und Test-Daten in der Datenbank anlegen
2. Dedizierte Mock-Endpunkte im Backend implementieren

## Entscheidung

Wir implementieren **dedizierte Mock-Endpunkte** im Backend unter dem Pfad 
`/api/dev/*`, die nur in der Entwicklungsumgebung verfügbar sind.

### Implementierte Mock-Endpunkte:
- `GET /api/dev/sales-cockpit/dashboard/{userId}` - Mock Dashboard-Daten
- `GET /api/dev/sales-cockpit/activities/{userId}` - Mock Activities  
- `GET /api/dev/customers` - Mock Kundenliste

## Begründung

### Architektur-Qualität: ⭐⭐⭐⭐ (4 Sterne)

**Vorteile:**
1. **Saubere Trennung Dev/Prod**: Mock-Daten existieren nur im Code, nicht in der DB
2. **Zentrale Kontrolle**: Backend kontrolliert Mock-Daten-Struktur
3. **Keine Datenbank-Verschmutzung**: Keine Test-User in Production-DB
4. **Versionierbar**: Mock-Daten sind im Git und können versioniert werden
5. **Deterministisch**: Immer gleiche Testdaten für konsistente Tests
6. **Performance**: Keine DB-Queries für Mock-Daten
7. **Sicherheit**: Mock-Endpunkte können per Profile komplett deaktiviert werden

**Nachteile:**
1. **Initialer Aufwand**: Mock-Endpunkte müssen implementiert werden
2. **Wartung**: Bei API-Änderungen müssen Mock-Endpunkte angepasst werden
3. **Realitätsnähe**: Mock-Daten könnten von echten Daten abweichen

## Implementierungsdetails

### 1. Profile-basierte Aktivierung
```java
@Profile("dev | test")
@Path("/api/dev")
public class MockDataResource {
    // Mock-Endpunkte nur in dev/test Profilen verfügbar
}
```

### 2. Strukturierte Mock-Daten
```java
@GET
@Path("/sales-cockpit/dashboard/{userId}")
@Produces(MediaType.APPLICATION_JSON)
public Response getMockDashboard(@PathParam("userId") String userId) {
    // Deterministisch basierend auf userId
    // Verschiedene Szenarien für verschiedene User-IDs
}
```

### 3. Szenario-basierte Daten
- User "mock-user-1": Erfolgreicher Sales-Mitarbeiter
- User "mock-user-2": Neuer Mitarbeiter mit wenig Daten
- User "mock-user-3": Manager mit Team-Übersicht
- User "mock-user-error": Provoziert Fehlerszenarien

## Konsequenzen

### Positive Konsequenzen:
- Frontend-Entwicklung kann unabhängig vom Backend-Status arbeiten
- Keine Vermischung von Test- und Produktionsdaten
- Einfache Integration in CI/CD-Pipeline
- Klare Dokumentation der API-Struktur durch Mock-Implementierung

### Negative Konsequenzen:
- Zusätzlicher Code, der gewartet werden muss
- Risiko der Divergenz zwischen Mock und echter API

### Mitigationsstrategien:
1. **Automatisierte Tests**: Mock-Endpunkte gegen API-Contract testen
2. **Shared Types**: Gemeinsame DTOs für Mock und echte Endpunkte
3. **Code-Review**: Mock-Änderungen immer mit API-Änderungen reviewen

## Alternativen

### Alternative 1: Mock-User in Datenbank
- ❌ Vermischung von Test- und Produktionsdaten
- ❌ Schwierig zu unterscheiden welche Daten "echt" sind
- ❌ Migration/Cleanup-Probleme
- ✅ Näher an der Realität

### Alternative 2: Frontend-only Mocks
- ❌ Duplizierung der Mock-Logik
- ❌ Keine zentrale Kontrolle
- ✅ Kein Backend-Code nötig

## Referenzen

- [Backend Start Guide](../technical/BACKEND_START_GUIDE.md)
- [API Contract](../technical/API_CONTRACT.md)
- [CRM Complete Master Plan](../CRM_COMPLETE_MASTER_PLAN.md)