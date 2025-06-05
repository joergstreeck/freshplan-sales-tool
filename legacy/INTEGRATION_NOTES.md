# Integration Notes für Entwickler

## 🚨 Wichtiger Hinweis zur Bonitätsprüfung

Die aktuelle Bonitätsprüfungs-Funktionalität ist **NUR eine Demo-Implementation** und darf **NICHT im Produktivbetrieb** verwendet werden!

### Aktueller Stand

#### Demo-Code Location
- **Datei**: `freshplan-complete.html`
- **Funktion**: `startCreditCheck()` (Zeilen ~1150-1170)
- **Logik**: 
  ```javascript
  // DEMO ONLY - Muss ersetzt werden!
  if (numericVolume < 5000) → Abgelehnt
  if (numericVolume > 50000) → Prüfung erforderlich
  else → Automatisch genehmigt
  ```

#### Fehlende Integration
- ❌ Keine echte Warenkreditversicherer-API
- ❌ Keine Datenbankanbindung
- ❌ Keine echte Bonitätsprüfung
- ❌ Keine Dokumentenverarbeitung

### Erforderliche Schritte vor Produktivbetrieb

#### 1. Allianz Trade API Integration
```javascript
// Beispiel-Integration
class AllianzTradeService {
    async checkCredit(customerData) {
        const apiData = {
            companyName: customerData.companyName,
            registrationNumber: customerData.registrationNumber,
            vatId: customerData.vatId,
            requestedLimit: customerData.creditLimit,
            // Weitere Felder gemäß API-Dokumentation
        };
        
        return await allianzTradeAPI.gradeCheck(apiData);
    }
}
```

#### 2. Datenbank-Integration
- Customer-Daten persistent speichern
- Credit-Check Historie aufbauen
- Audit-Trail implementieren

#### 3. Geschäftsleitung-Freigabe
- Dashboard-System implementieren (siehe DASHBOARD_ARCHITECTURE.md)
- Workflow-Engine für Freigaben
- E-Mail-Benachrichtigungen

#### 4. Sicherheit
- API-Keys sicher verwalten
- Verschlüsselte Datenübertragung
- Zugriffsrechte implementieren

### Test-Empfehlungen

```javascript
// Test-Flag für Entwicklung
const IS_DEMO_MODE = process.env.NODE_ENV !== 'production';

if (IS_DEMO_MODE) {
    console.warn('⚠️ Bonitätsprüfung läuft im DEMO-Modus!');
    // Demo-Logik
} else {
    // Echte API-Calls
}
```

### Kontakt für API-Zugänge
- Allianz Trade API: [Dokumentation anfordern]
- Technischer Support: [Kontakt einfügen]

---

**WICHTIG**: Vor Go-Live muss die Geschäftsleitung die Integration abnehmen!