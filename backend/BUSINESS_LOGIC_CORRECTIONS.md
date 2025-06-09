# Business Logic Corrections - FreshPlan 2.0

## Referenz: freshplan_summary.md

Die korrekte Business-Logik laut offizieller Dokumentation:

### Basisrabatt
- unter 5.000 EUR: 0%
- 5.000 - 14.999 EUR: 3%
- 15.000 - 29.999 EUR: 6%
- 30.000 - 49.999 EUR: 8%
- 50.000 - 74.999 EUR: 9%
- ab 75.000 EUR: 10%

### Frühbucherrabatt
- 10 - 14 Tage: +1%
- 15 - 29 Tage: +2%
- 30 - 44 Tage: +3%

### Abholrabatt
- +2% bei Abholung (nur ab 5.000 EUR)

### Maximum
- Gesamtrabatt maximal 15%

## Gefundene Fehler

### 1. Altes Backend (im Archiv)
**Datei:** `/archive/backend-old-20250608/src/main/java/de/freshplan/domain/calculator/service/CalculatorService.java`

**Fehler:**
- Zeile 17: `new DiscountRule(50000, 10)` - FALSCH! 50k sollte 9% sein, nicht 10%
- Zeile 20: `new DiscountRule(10000, 4)` - FALSCH! Diese Stufe existiert nicht
- Zeile 27: `new EarlyBookingRule(21, 2)` - FALSCH! Sollte 15 sein
- Zeile 28: `new EarlyBookingRule(14, 1)` - FALSCH! Sollte 10 sein
- Fehlende 75.000 EUR Schwelle für 10%

### 2. Frontend Dateien
**Status:** ✅ KORREKT
- `/frontend/src/mocks/calculatorHandlers.ts` - Korrekte Implementierung
- Alle Frontend-Calculator-Komponenten verwenden die korrekten Werte

### 3. Neues Backend
**Status:** ✅ KORREKT
- `/backend/src/main/java/de/freshplan/calculator/CalculatorService.java` - Korrekte Implementierung
- `/backend/src/main/java/de/freshplan/api/CalculatorResource.java` - Korrekte Rules im Endpoint

## Empfehlung

Das neue Backend ist bereits korrekt implementiert. Das alte Backend im Archiv enthält falsche Business-Logik und sollte NICHT als Referenz verwendet werden.

## Offene Punkte

1. **Kettenkunden-Logik** - Noch nicht implementiert
2. **2-Abruf-Option** - Noch nicht implementiert
3. **Maximale Vorlaufzeit** - Dokumentation sagt "30-44 Tage"