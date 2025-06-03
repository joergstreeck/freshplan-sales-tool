# CustomerModuleV2 - Detaillierte Test-Checkliste

## 1. UI-Interaktionen

### 1.1 Kundentyp-Auswahl (Einzelstandort/Kette)
- [ ] **Test 1.1.1**: Klick auf "Einzelstandort" - Radiobutton wird ausgewählt
- [ ] **Test 1.1.2**: Klick auf "Kette/Gruppe" - Radiobutton wird ausgewählt
- [ ] **Test 1.1.3**: Bei Auswahl "Kette/Gruppe" - Ketten-Details Bereich wird sichtbar
- [ ] **Test 1.1.4**: Bei Wechsel zurück zu "Einzelstandort" - Ketten-Details Bereich wird ausgeblendet
- [ ] **Test 1.1.5**: Feld "Kettenkunde" wird nur bei Auswahl "Kette/Gruppe" angezeigt

### 1.2 Neukunden-Warnung
- [ ] **Test 1.2.1**: Auswahl "Neukunde" + "Rechnung" - gelbe Warnung erscheint sofort
- [ ] **Test 1.2.2**: Wechsel zu "Bestandskunde" - Warnung verschwindet
- [ ] **Test 1.2.3**: Wechsel zu "Vorkasse/Bar" - Warnung verschwindet
- [ ] **Test 1.2.4**: Warnung zeigt korrekten Text über Bonitätsprüfung

### 1.3 Formular-Eingaben
- [ ] **Test 1.3.1**: Texteingabe in alle Textfelder möglich
- [ ] **Test 1.3.2**: Auswahl in allen Dropdown-Menüs funktioniert
- [ ] **Test 1.3.3**: Zahleneingabe in Zahlenfelder (Jahresvolumen, PLZ)
- [ ] **Test 1.3.4**: Checkbox "Interesse an Vending-Automaten" anklickbar
- [ ] **Test 1.3.5**: Bei aktivierter Vending-Checkbox - Vending-Felder werden sichtbar
- [ ] **Test 1.3.6**: Bei deaktivierter Vending-Checkbox - Vending-Felder werden ausgeblendet

### 1.4 Buttons
- [ ] **Test 1.4.1**: "Speichern"-Button klickbar und löst Speicherung aus
- [ ] **Test 1.4.2**: "Zurücksetzen"-Button zeigt Bestätigungsdialog
- [ ] **Test 1.4.3**: Nach Bestätigung werden alle Felder geleert
- [ ] **Test 1.4.4**: Bei Ablehnung bleiben Daten erhalten

### 1.5 Bonitätsprüfung (nur für Neukunden mit Rechnung)
- [ ] **Test 1.5.1**: "Bonitätsprüfung anfordern"-Button nur sichtbar bei Neukunde + Rechnung
- [ ] **Test 1.5.2**: Button löst Prüfung aus (Demo-Modus)
- [ ] **Test 1.5.3**: "Freigabe durch Geschäftsleitung"-Button nur sichtbar bei Neukunde + Rechnung
- [ ] **Test 1.5.4**: Button löst Anfrage aus (Demo-Modus)

### 1.6 Auto-Vervollständigung
- [ ] **Test 1.6.1**: Keine Auto-Vervollständigung aktiv (Browser-Standard)
- [ ] **Test 1.6.2**: Tab-Navigation zwischen Feldern funktioniert

## 2. Validierungsszenarien

### 2.1 Pflichtfeld-Validierung
- [ ] **Test 2.1.1**: Leeres Pflichtfeld "Firma/Organisation" - Fehler bei Speichern
- [ ] **Test 2.1.2**: Leeres Pflichtfeld "Rechtsform" - Fehler bei Speichern
- [ ] **Test 2.1.3**: Leeres Pflichtfeld "Kundentyp" - Fehler bei Speichern
- [ ] **Test 2.1.4**: Leeres Pflichtfeld "Branche" - Fehler bei Speichern
- [ ] **Test 2.1.5**: Leeres Pflichtfeld "Ansprechpartner" - Fehler bei Speichern
- [ ] **Test 2.1.6**: Leeres Pflichtfeld "E-Mail" - Fehler bei Speichern
- [ ] **Test 2.1.7**: Leeres Pflichtfeld "Telefon" - Fehler bei Speichern
- [ ] **Test 2.1.8**: Leeres Pflichtfeld "Straße & Hausnummer" - Fehler bei Speichern
- [ ] **Test 2.1.9**: Leeres Pflichtfeld "PLZ" - Fehler bei Speichern
- [ ] **Test 2.1.10**: Leeres Pflichtfeld "Ort" - Fehler bei Speichern
- [ ] **Test 2.1.11**: Leeres Pflichtfeld "Erwartetes Jahresvolumen" - Fehler bei Speichern
- [ ] **Test 2.1.12**: Leeres Pflichtfeld "Zahlungsart" - Fehler bei Speichern

### 2.2 Format-Validierung
- [ ] **Test 2.2.1**: E-Mail ohne @ - Fehlermeldung "gültige E-Mail-Adresse"
- [ ] **Test 2.2.2**: E-Mail mit @ aber ohne Domain - Fehlermeldung
- [ ] **Test 2.2.3**: Gültige E-Mail (test@example.com) - keine Fehlermeldung
- [ ] **Test 2.2.4**: PLZ mit weniger als 5 Ziffern - Fehlermeldung
- [ ] **Test 2.2.5**: PLZ mit mehr als 5 Ziffern - Fehlermeldung
- [ ] **Test 2.2.6**: PLZ mit Buchstaben - Fehlermeldung
- [ ] **Test 2.2.7**: PLZ mit genau 5 Ziffern (12345) - keine Fehlermeldung
- [ ] **Test 2.2.8**: Telefonnummer mit Buchstaben - Fehlermeldung
- [ ] **Test 2.2.9**: Telefonnummer mit Sonderzeichen (+49 123 456789) - erlaubt
- [ ] **Test 2.2.10**: Jahresvolumen negative Zahl - nicht möglich einzugeben

### 2.3 Echtzeit-Validierung
- [ ] **Test 2.3.1**: Fehlermeldung erscheint sofort beim Verlassen eines fehlerhaften Feldes
- [ ] **Test 2.3.2**: Rote Umrandung bei fehlerhaften Feldern
- [ ] **Test 2.3.3**: Fehlermeldung verschwindet bei Korrektur der Eingabe
- [ ] **Test 2.3.4**: Fokus springt zum ersten fehlerhaften Feld bei Speichern

## 3. Daten-Persistenz

### 3.1 LocalStorage Speicherung
- [ ] **Test 3.1.1**: Eingaben werden nach 1 Sekunde automatisch gespeichert
- [ ] **Test 3.1.2**: Browser-Neustart - alle Daten sind noch vorhanden
- [ ] **Test 3.1.3**: Neuer Tab öffnen - Daten sind synchronisiert
- [ ] **Test 3.1.4**: LocalStorage Eintrag "freshplan-state" enthält customer.data

### 3.2 Datenwiederherstellung
- [ ] **Test 3.2.1**: Nach Seitenneuladung sind alle Felder wieder gefüllt
- [ ] **Test 3.2.2**: Kundentyp (Einzelstandort/Kette) wird korrekt wiederhergestellt
- [ ] **Test 3.2.3**: Branche wird korrekt wiederhergestellt
- [ ] **Test 3.2.4**: Vending-Checkbox-Status wird wiederhergestellt
- [ ] **Test 3.2.5**: Neukunden-Warnung erscheint wieder bei entsprechenden Daten

### 3.3 Daten löschen
- [ ] **Test 3.3.1**: "Zurücksetzen" löscht LocalStorage-Daten
- [ ] **Test 3.3.2**: Nach Löschen und Neuladung sind alle Felder leer
- [ ] **Test 3.3.3**: Bonitätsprüfungs-Status wird zurückgesetzt

## 4. Edge Cases und Fehlerfälle

### 4.1 Große Dateneingaben
- [ ] **Test 4.1.1**: Sehr lange Firmennamen (>100 Zeichen) - werden akzeptiert
- [ ] **Test 4.1.2**: Sehr große Jahresvolumen (>1.000.000) - werden korrekt angezeigt
- [ ] **Test 4.1.3**: Notizfeld mit sehr viel Text (>1000 Zeichen) - wird gespeichert

### 4.2 Spezielle Zeichen
- [ ] **Test 4.2.1**: Firmennamen mit Umlauten (ÄÖÜ) - werden korrekt gespeichert
- [ ] **Test 4.2.2**: Firmennamen mit Sonderzeichen (&, -, ') - werden akzeptiert
- [ ] **Test 4.2.3**: E-Mail mit Bindestrich/Unterstrich - wird validiert

### 4.3 Browser-Kompatibilität
- [ ] **Test 4.3.1**: Chrome - alle Funktionen arbeiten
- [ ] **Test 4.3.2**: Firefox - alle Funktionen arbeiten
- [ ] **Test 4.3.3**: Safari - alle Funktionen arbeiten
- [ ] **Test 4.3.4**: Edge - alle Funktionen arbeiten

### 4.4 Fehlerzustände
- [ ] **Test 4.4.1**: LocalStorage voll - Fehlermeldung wird angezeigt
- [ ] **Test 4.4.2**: JavaScript deaktiviert - Basis-HTML funktioniert noch
- [ ] **Test 4.4.3**: Netzwerkfehler bei Bonitätsprüfung - Fehlermeldung erscheint

### 4.5 Grenzwerte
- [ ] **Test 4.5.1**: PLZ "00000" - wird akzeptiert
- [ ] **Test 4.5.2**: PLZ "99999" - wird akzeptiert
- [ ] **Test 4.5.3**: Jahresvolumen "0" - wird akzeptiert
- [ ] **Test 4.5.4**: Anzahl Standorte bei Kette minimum "2"
- [ ] **Test 4.5.5**: Vertragslaufzeit alle Optionen wählbar (12/24/36 Monate)

## 5. Integration mit anderen Modulen

### 5.1 Tab-Navigation
- [ ] **Test 5.1.1**: Nach erfolgreichem Speichern kann zu anderen Tabs gewechselt werden
- [ ] **Test 5.1.2**: Bei unvollständigen Pflichtfeldern - Warnung beim Tab-Wechsel
- [ ] **Test 5.1.3**: Daten bleiben beim Tab-Wechsel erhalten

### 5.2 Rabattrechner-Integration
- [ ] **Test 5.2.1**: Kundentyp "Kette" aktiviert Kettenrabatt im Rechner
- [ ] **Test 5.2.2**: Wechsel Einzelstandort/Kette aktualisiert Rechner

### 5.3 Profil-Tab
- [ ] **Test 5.3.1**: Gespeicherte Kundendaten erscheinen im Profil-Tab
- [ ] **Test 5.3.2**: Branchenspezifische Empfehlungen basieren auf ausgewählter Branche

### 5.4 Standorte-Tab (bei Ketten)
- [ ] **Test 5.4.1**: Bei Kundentyp "Kette" wird Standorte-Tab relevant
- [ ] **Test 5.4.2**: Anzahl Standorte aus Kundendaten wird übernommen

### 5.5 Angebots-Tab
- [ ] **Test 5.5.1**: Kundendaten werden für Angebotserstellung verwendet
- [ ] **Test 5.5.2**: Zahlungsart wird ins Angebot übernommen

### 5.6 Event-System
- [ ] **Test 5.6.1**: "customer:saved" Event wird bei Speichern ausgelöst
- [ ] **Test 5.6.2**: "customer:cleared" Event wird bei Löschen ausgelöst
- [ ] **Test 5.6.3**: "app:reset" Event löscht auch Kundendaten

### 5.7 Sprachumschaltung
- [ ] **Test 5.7.1**: Alle Labels wechseln bei Sprachumschaltung
- [ ] **Test 5.7.2**: Eingegebene Daten bleiben bei Sprachwechsel erhalten
- [ ] **Test 5.7.3**: Validierungsmeldungen in korrekter Sprache

## 6. Performance und Benutzerfreundlichkeit

### 6.1 Ladezeiten
- [ ] **Test 6.1.1**: Formular lädt in < 1 Sekunde
- [ ] **Test 6.1.2**: Autosave verzögert nicht die Eingabe
- [ ] **Test 6.1.3**: Validierung erfolgt ohne spürbare Verzögerung

### 6.2 Responsive Design
- [ ] **Test 6.2.1**: Desktop (>1200px) - zweispaltige Darstellung
- [ ] **Test 6.2.2**: Tablet (768-1200px) - angepasstes Layout
- [ ] **Test 6.2.3**: Mobile (<768px) - einspaltige Darstellung
- [ ] **Test 6.2.4**: Touch-Eingaben funktionieren auf mobilen Geräten

### 6.3 Barrierefreiheit
- [ ] **Test 6.3.1**: Alle Felder haben Labels
- [ ] **Test 6.3.2**: Pflichtfelder sind mit * gekennzeichnet
- [ ] **Test 6.3.3**: Tab-Reihenfolge ist logisch
- [ ] **Test 6.3.4**: Fehlermeldungen sind für Screenreader zugänglich

### 6.4 Benutzerführung
- [ ] **Test 6.4.1**: Logische Gruppierung der Felder in Sektionen
- [ ] **Test 6.4.2**: Klare Überschriften für jeden Bereich
- [ ] **Test 6.4.3**: Hilfreiche Platzhalter-Texte wo angebracht
- [ ] **Test 6.4.4**: Eindeutige Fehlermeldungen mit Lösungshinweisen

## Test-Durchführung

### Vorbereitung
1. Browser-Cache und LocalStorage leeren
2. Browser-Konsole öffnen für Fehlerüberwachung
3. Unterschiedliche Browser bereithalten

### Dokumentation
- [ ] Jeden gefundenen Fehler mit Screenshot dokumentieren
- [ ] Browser und Version notieren
- [ ] Exakte Schritte zur Reproduktion aufschreiben
- [ ] Erwartetes vs. tatsächliches Verhalten beschreiben

### Prioritäten
- **Kritisch**: Datenverlust, Absturz, Pflichtfeld-Validierung
- **Hoch**: Falsche Validierung, UI-Fehler, Integration
- **Mittel**: Darstellungsfehler, Performance
- **Niedrig**: Schönheitsfehler, nice-to-have Features