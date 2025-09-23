# ADR-0001: CQRS Light statt Full-CQRS

**Status:** Akzeptiert
**Datum:** 21.09.2025
**Autor:** Development Team

## Kontext

Für das FreshPlan Sales Tool benötigen wir eine skalierbare Architektur, die gleichzeitig kosteneffizient für 5-50 interne Nutzer ist. Die Entscheidung zwischen Full-CQRS (mit separaten Datenbanken) und CQRS Light (eine Datenbank) steht an.

## Entscheidung

Wir implementieren **CQRS Light** mit:
- Einer PostgreSQL-Datenbank
- Getrennten Command- und Query-Services
- PostgreSQL LISTEN/NOTIFY für Event-Kommunikation
- Keine separaten Event-Stores oder Event-Bus-Systeme

## Begründung

### Pro CQRS Light:
- **Kosteneffizient:** Eine Datenbank-Instanz statt mehrerer
- **Performance ausreichend:** <200ms p95 ist für interne Tools optimal
- **Einfache Wartung:** Weniger Infrastruktur-Komplexität
- **Bewährte Technologie:** PostgreSQL-Expertise bereits vorhanden
- **Budget-Constraints:** Minimale Infrastruktur-Kosten

### Contra Full-CQRS:
- **Over-Engineering:** Für 5-50 Nutzer zu komplex
- **Höhere Kosten:** Mehrere DB-Instanzen + Event-Bus
- **Mehr Wartungsaufwand:** Komplexere Fehlersuche
- **YAGNI:** Advanced Features werden nicht benötigt

## Konsequenzen

### Positive:
- Schnelle Implementation möglich
- Niedrige Betriebskosten
- Einfaches Monitoring und Debugging
- Performance-Ziele (<200ms p95) erreichbar

### Negative:
- Bei >100 Nutzern eventuell Migration zu Full-CQRS nötig
- Weniger Flexibilität bei komplexen Query-Patterns
- Event-System limitiert auf PostgreSQL LISTEN/NOTIFY

### Mitigationen:
- Architektur so gestalten, dass spätere Migration möglich ist
- Performance-Monitoring implementieren
- Bei Skalierung >100 Nutzer: Evaluation Full-CQRS

## Alternativen

1. **Full-CQRS:** Abgelehnt wegen Over-Engineering
2. **Traditionelles CRUD:** Abgelehnt wegen Performance-Anforderungen
3. **Event Sourcing:** Abgelehnt wegen Komplexität

## Compliance

- **Performance SLO:** `api_request_p95_ms < 200` erfüllt
- **Budget-Constraint:** Minimale Infrastruktur-Kosten
- **Team-Expertise:** PostgreSQL-Know-how verfügbar