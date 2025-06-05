# 🧪 FreshPlan Sales Tool - Test Checkliste

## 1. 📊 Rabatt-Demo Tab

### Grundfunktionen
- [ ] **Bestellwert-Slider** bewegen
  - Prüfen: Wert-Anzeige aktualisiert sich
  - Prüfen: Rabatt wird neu berechnet
  
- [ ] **Vorlaufzeit-Slider** bewegen
  - Prüfen: Tage-Anzeige aktualisiert sich
  - Prüfen: Frühbucherrabatt ändert sich

- [ ] **Selbstabholung** Checkbox
  - Prüfen: 3% Rabatt wird hinzugefügt/entfernt
  
- [ ] **Kettenkunde** Checkbox
  - Prüfen: 5% Rabatt wird hinzugefügt/entfernt

### Berechnungen überprüfen
- [ ] Bestellwert: €15.000, Vorlaufzeit: 7 Tage
  - Erwarteter Rabatt: ~5% (Basis)
  
- [ ] Bestellwert: €50.000, Vorlaufzeit: 14 Tage, Abholung: Ja
  - Erwarteter Rabatt: ~15% (10% Basis + 2% Frühbucher + 3% Abholung)

### Szenarien
- [ ] Szenario-Karten werden angezeigt
- [ ] Klick auf Szenario lädt die Werte

## 2. 👥 Kundendaten Tab

### Formular-Eingaben
- [ ] **Grunddaten** ausfüllen
  - Firma/Organisation
  - Ansprechpartner
  - E-Mail (Validierung testen)
  - Telefon

- [ ] **Branche** auswählen
  - Prüfen: Branchenspezifische Felder erscheinen
  - Hotel: Zimmer, Auslastung, Frühstückspreis
  - Krankenhaus: Betten, Mitarbeiter-Essen

- [ ] **Einzelstandort vs. Kette** umschalten
  - Prüfen: Ketten-Details Sektion erscheint/verschwindet

- [ ] **Vending-Interesse** aktivieren
  - Prüfen: Zusätzliche Felder erscheinen

### Speichern
- [ ] Formular speichern
- [ ] Seite neu laden - Daten sollten erhalten bleiben
- [ ] Browser schließen und öffnen - Daten noch da?

## 3. 📋 Kundenprofil Tab

- [ ] **Ohne Kundendaten**: "Keine Kundendaten vorhanden" Meldung
- [ ] **Mit Kundendaten**: Profil wird generiert
  - Kundeninformationen korrekt?
  - Potenzialanalyse sinnvoll?
  - Empfohlene Produkte passend zur Branche?
  - Schlüsselargumente vorhanden?

## 4. 📄 Angebot Tab

### PDF-Generierung
- [ ] **Ohne Kundendaten**: Fehlermeldung erscheint
- [ ] **Mit Kundendaten**:
  - [ ] Rabatt einstellen
  - [ ] Verkäufer-Infos eingeben
  - [ ] Dokumente auswählen
  - [ ] "PDF erstellen" klicken
  - [ ] PDF-Vorschau erscheint?

### PDF-Modal
- [ ] Download-Button funktioniert
- [ ] E-Mail-Button öffnet Mail-Client
- [ ] Schließen-Button (X) funktioniert

## 5. ⚙️ Einstellungen Tab

### Verkäufer-Informationen
- [ ] Daten eingeben und speichern
- [ ] Werden diese im Angebot-Tab übernommen?

### Standard-Werte
- [ ] Standard-Rabatt ändern
- [ ] Standard-Vertragslaufzeit ändern
- [ ] Prüfen: Werden neue Defaults verwendet?

### Import/Export
- [ ] "Einstellungen exportieren" - JSON wird heruntergeladen
- [ ] "Einstellungen importieren" - JSON kann geladen werden

## 6. 🌐 Allgemeine Funktionen

### Sprach-Umschaltung
- [ ] Auf "EN" klicken
  - Alle Texte wechseln zu Englisch?
  - Navigation übersetzt?
  - Formulare übersetzt?
  
- [ ] Zurück auf "DE"
  - Alles wieder Deutsch?

### Tab-Navigation
- [ ] Alle Tabs anklickbar
- [ ] Aktiver Tab hervorgehoben
- [ ] URL ändert sich (z.B. ?tab=customer)
- [ ] Browser Zurück/Vor funktioniert

### Progress Bar
- [ ] Fortschrittsbalken sichtbar
- [ ] Füllt sich beim Durchgehen der Tabs

### Responsive Design
- [ ] Browserfenster verkleinern
- [ ] Mobile Ansicht testen (F12 → Device Mode)

## 7. 🐛 Fehlerbehandlung

### Validierung
- [ ] Ungültige E-Mail eingeben → Fehlermeldung
- [ ] Pflichtfelder leer lassen → Kann nicht gespeichert werden
- [ ] Negative Zahlen eingeben → Werden verhindert

### Browser-Kompatibilität
- [ ] Chrome/Edge
- [ ] Firefox
- [ ] Safari (falls Mac)

## 8. 💾 Datenpersistenz

- [ ] Alle Tabs durchgehen und Daten eingeben
- [ ] Browser komplett schließen
- [ ] Wieder öffnen - alle Daten noch vorhanden?
- [ ] LocalStorage in DevTools prüfen (F12 → Application → Local Storage)

## 📝 Notizen

Probleme gefunden:
- 

Verbesserungsvorschläge:
- 

---

✅ = Funktioniert einwandfrei
⚠️ = Funktioniert mit Einschränkungen
❌ = Funktioniert nicht