# Frontend-Backend-Verbindung für Rabattberechnung

**Datum:** 2025-06-28
**Typ:** IMPL (Implementation)
**Status:** Implementiert - Test erforderlich

## Zusammenfassung

Die CalculatorLayout-Komponente wurde so angepasst, dass sie die Backend-API für die Rabattberechnung verwendet.

## Änderungen

### 1. CalculatorLayout.tsx angepasst
- Import des `useCalculateDiscount` Hooks hinzugefügt
- State für `pickup` Checkbox hinzugefügt
- `useEffect` Hook für automatische API-Aufrufe bei Änderungen
- Dynamische Anzeige der API-Ergebnisse statt statischer Werte

### 2. CORS-Konfiguration korrigiert
- Port 5174 entfernt (war falsch dokumentiert)
- Nur Port 5173 für Frontend konfiguriert

## Test-Anleitung

### Voraussetzungen:
1. Backend läuft auf http://localhost:8080
2. Frontend läuft auf http://localhost:5173

### Test-Schritte:
1. Öffne http://localhost:5173/login-bypass
2. Klicke auf "Create Development Token"
3. Navigiere zu http://localhost:5173/legacy-tool
4. Verwende die Slider und Checkbox:
   - Bestellwert ändern → Rabatte sollten sich automatisch anpassen
   - Vorlaufzeit ändern → Frühbucherrabatt sollte sich ändern
   - Abholung aktivieren → 2% Abholrabatt (nur ab 5.000€)

### Erwartete Ergebnisse:
- Bei 15.000€ und 14 Tagen: 6% Basis + 1% Frühbucher = 7% Gesamt
- Bei 30.000€ und 30 Tagen: 8% Basis + 3% Frühbucher = 11% Gesamt
- Bei Abholung ab 5.000€: zusätzlich 2%
- Maximaler Gesamtrabatt: 15%

## Offene Punkte

- Browser-Konsole auf Fehler prüfen
- Network-Tab überprüfen ob API-Calls gemacht werden
- Ladeindikator könnte während API-Calls angezeigt werden
- Error-Handling für fehlgeschlagene API-Calls könnte verbessert werden

## API-Test Kommando

```bash
curl -X POST http://localhost:8080/api/calculator/calculate \
  -H "Content-Type: application/json" \
  -H "Origin: http://localhost:5173" \
  -d '{"orderValue": 15000, "leadTime": 14, "pickup": false, "chain": false}'
```

Erwartete Antwort:
```json
{
  "orderValue": 15000.0,
  "leadTime": 14,
  "pickup": false,
  "chain": false,
  "baseDiscount": 6.0,
  "earlyDiscount": 1.0,
  "pickupDiscount": 0.0,
  "chainDiscount": 0.0,
  "totalDiscount": 7.0,
  "discountAmount": 1050.0,
  "savingsAmount": 1050.0,
  "finalPrice": 13950.0
}
```