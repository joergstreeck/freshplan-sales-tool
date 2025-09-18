# Slider-Erweiterung und klickbare Beispielszenarien

**Datum:** 2025-06-28
**Typ:** IMPL (Implementation)
**Status:** Implementiert

## Zusammenfassung

1. Vorlaufzeit-Slider wurde von 30 auf 50 Tage erweitert
2. Beispielszenarien sind jetzt klickbare Presets, die die Slider-Werte setzen

## Änderungen

### 1. Vorlaufzeit-Slider erweitert
- Maximum von 30 auf 50 Tage erhöht
- Ermöglicht Nutzern, den vollen Frühbucherrabatt (3% ab 30 Tage) besser zu nutzen

### 2. Klickbare Beispielszenarien
Die drei Szenario-Cards sind jetzt klickbare Presets:

**Hotelkette (Klick setzt):**
- Bestellwert: 35.000€
- Vorlaufzeit: 21 Tage
- Abholung: ✓
- Zeigt: 12% Rabatt

**Klinikgruppe (Klick setzt):**
- Bestellwert: 65.000€
- Vorlaufzeit: 30 Tage
- Abholung: ✗
- Zeigt: 12% Rabatt

**Restaurant (Klick setzt):**
- Bestellwert: 8.500€
- Vorlaufzeit: 14 Tage
- Abholung: ✓
- Zeigt: 6% Rabatt

## Technische Details

- `applyScenario()` Handler setzt die State-Werte
- Hover-Effekt zeigt Klickbarkeit (Border, Shadow, Transform)
- Cursor: pointer für bessere UX
- Die Rabattberechnung erfolgt automatisch über die existierende API-Integration

## Test

1. Öffne http://localhost:5173/legacy-tool
2. Prüfe den Vorlaufzeit-Slider - sollte bis 50 Tage gehen
3. Die drei Beispielszenarien sollten ihre Rabatte dynamisch berechnen
4. Browser-Konsole sollte keine Fehler zeigen