# Contributing to FreshPlan Sales Tool

Vielen Dank für Ihr Interesse, zu diesem Projekt beizutragen! 🎉

## Verhaltenskodex

Dieses Projekt und alle Beteiligten verpflichten sich zu einem offenen und einladenden Umfeld. Wir erwarten von allen Mitwirkenden respektvollen Umgang miteinander.

## Wie kann ich beitragen?

### 🐛 Bugs melden

Bevor Sie einen Bug melden:
- Überprüfen Sie die [Issues](https://github.com/IhrUsername/freshplan-sales-tool/issues), ob der Bug bereits gemeldet wurde
- Falls nicht, erstellen Sie ein neues Issue mit:
  - Einer klaren Beschreibung
  - Schritten zur Reproduktion
  - Erwartetes vs. tatsächliches Verhalten
  - Screenshots (falls relevant)
  - Browser und Version

### 💡 Features vorschlagen

Wir freuen uns über neue Ideen! Erstellen Sie ein Issue mit:
- Klarer Beschreibung des Features
- Use Case / Anwendungsfall
- Mögliche Implementierungsansätze
- Mockups oder Wireframes (optional)

### 🔧 Code beitragen

1. **Fork & Clone**
   ```bash
   git clone https://github.com/IhrUsername/freshplan-sales-tool.git
   cd freshplan-sales-tool
   ```

2. **Branch erstellen**
   ```bash
   git checkout -b feature/mein-feature
   # oder
   git checkout -b fix/bug-beschreibung
   ```

3. **Änderungen vornehmen**
   - Folgen Sie dem bestehenden Code-Stil
   - Kommentieren Sie komplexe Logik
   - Testen Sie Ihre Änderungen in verschiedenen Browsern

4. **Commit**
   ```bash
   git add .
   git commit -m "feat: Kurze Beschreibung der Änderung"
   ```
   
   Commit-Konventionen:
   - `feat:` Neues Feature
   - `fix:` Bugfix
   - `docs:` Dokumentationsänderungen
   - `style:` Code-Formatierung
   - `refactor:` Code-Refactoring
   - `test:` Tests hinzufügen/ändern
   - `chore:` Wartungsarbeiten

5. **Push & Pull Request**
   ```bash
   git push origin feature/mein-feature
   ```
   - Erstellen Sie einen Pull Request
   - Beschreiben Sie Ihre Änderungen
   - Referenzieren Sie relevante Issues

## Code-Standards

### JavaScript
- ES6+ Syntax verwenden
- Funktionen dokumentieren mit JSDoc
- Keine globalen Variablen (außer notwendige)
- Sinnvolle Variablennamen

```javascript
/**
 * Berechnet den Rabatt basierend auf dem Bestellwert
 * @param {number} orderValue - Bestellwert in Euro
 * @returns {number} Rabatt in Prozent
 */
function calculateDiscount(orderValue) {
    // Implementierung
}
```

### CSS
- BEM-Namenskonvention wo sinnvoll
- CSS-Variablen für Farben und Wiederverwendbares
- Mobile-first Ansatz

### HTML
- Semantisches HTML5
- Barrierefreiheit beachten (ARIA-Labels)
- Valides Markup

## Testing

Testen Sie Ihre Änderungen:
- [ ] Chrome (aktuellste Version)
- [ ] Firefox (aktuellste Version)
- [ ] Safari (falls verfügbar)
- [ ] Edge (aktuellste Version)
- [ ] Mobile Browser (Chrome/Safari)

Prüfen Sie:
- [ ] Alle Features funktionieren wie erwartet
- [ ] Keine JavaScript-Fehler in der Konsole
- [ ] Responsive Design intakt
- [ ] PDF-Generierung funktioniert

## Dokumentation

- Aktualisieren Sie die README.md bei neuen Features
- Dokumentieren Sie komplexe Funktionen
- Fügen Sie Beispiele hinzu wo hilfreich

## Review-Prozess

1. Alle Pull Requests werden reviewed
2. Mindestens ein Maintainer muss zustimmen
3. Alle Tests müssen bestehen
4. Keine Konflikte mit main Branch

## Fragen?

- Erstellen Sie ein Issue mit dem Label "question"
- Oder kontaktieren Sie: support@freshfoodz.de

## Anerkennung

Alle Beitragenden werden in der README.md aufgeführt!

---

Vielen Dank für Ihre Unterstützung! 🙏