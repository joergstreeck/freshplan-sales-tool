# FreshPlan Sales Tool - Migrations-Plan V2

## 🎯 Ziel
Schrittweise Migration der Original-App (reference-original.html) zur TypeScript/Vite-Architektur mit 100% Feature-Parität.

## 📋 Migrations-Strategie: "Feature-für-Feature"

### Phase 1: Basis-Korrekturen (1-2 Tage)
#### 1.1 Kundendaten-Modul erweitern
- [ ] Rechtsform-Dropdown hinzufügen
- [ ] Kundentyp (Neukunde/Bestandskunde) hinzufügen
- [ ] Kundennummer-Feld hinzufügen
- [ ] Jahresvolumen-Feld hinzufügen
- [ ] Zahlungsart-Dropdown hinzufügen
- [ ] Notizen-Textarea hinzufügen
- [ ] Freifeld 1 & 2 hinzufügen

#### 1.2 State-Management anpassen
- [ ] CustomerState erweitern für neue Felder
- [ ] Validierungsregeln hinzufügen
- [ ] LocalStorage-Synchronisation

### Phase 2: Bonitätsprüfung (2-3 Tage)
#### 2.1 Neues Modul erstellen
- [ ] CreditCheckModule.ts erstellen
- [ ] CreditCheckState in AppState integrieren
- [ ] Tab in Navigation hinzufügen
- [ ] Tab-Visibility-Logik (nur bei Neukunde + Rechnung)

#### 2.2 Bonitätsprüfungs-Features
- [ ] Automatische Datenübernahme
- [ ] Kreditlimit-Berechnung
- [ ] Handelsregister-Felder
- [ ] USt-IdNr. mit Validierung
- [ ] Unternehmensgröße-Auswahl
- [ ] Zahlungserfahrungen-Erfassung
- [ ] Status-Anzeige
- [ ] Anfrage-Button

### Phase 3: Standort-Details Tab (2 Tage)
#### 3.1 LocationDetailsModule erstellen
- [ ] Tab in Navigation hinzufügen
- [ ] Tab-Visibility bei Detailerfassung
- [ ] Standort-Liste mit Bearbeitung
- [ ] Detailformular pro Standort

#### 3.2 Standort-Features
- [ ] Branchenspezifische Felder
- [ ] Vending-Interesse pro Standort
- [ ] Synchronisierungs-Checks
- [ ] Validierung und Speicherung

### Phase 4: Standorte-Modul vervollständigen (1 Tag)
- [ ] Verwaltungstyp (zentral/dezentral)
- [ ] Größenkategorien (Klein/Mittel/Groß)
- [ ] Service-Optionen pro Branche
- [ ] Vending-Konzept vollständig

### Phase 5: UI/UX-Verbesserungen (1-2 Tage)
#### 5.1 Header optimieren
- [ ] Logo einbinden
- [ ] "Formular leeren"-Button
- [ ] Globaler Speichern-Button

#### 5.2 Auto-Save implementieren
- [ ] Bei Tab-Wechsel
- [ ] Bei vollständigen Pflichtfeldern
- [ ] Warnung bei ungespeicherten Änderungen

### Phase 6: Testing & Bugfixing (2 Tage)
- [ ] Feature-by-Feature Test gegen Original
- [ ] Datenfluss-Tests
- [ ] Validierungs-Tests
- [ ] Browser-Kompatibilität

## 📊 Geschätzte Gesamtdauer: 10-13 Tage

## 🚀 Sofort-Maßnahmen (Tag 1)

### Schritt 1: CustomerService erweitern
```typescript
// Neue Felder für CustomerData
interface CustomerData {
  // Existing...
  legalForm?: 'gmbh' | 'ag' | 'gbr' | 'einzelunternehmen' | 'other';
  customerType?: 'new' | 'existing';
  customerNumber?: string;
  annualVolume?: number;
  paymentMethod?: 'prepayment' | 'cash' | 'invoice';
  notes?: string;
  customField1?: string;
  customField2?: string;
}
```

### Schritt 2: Translations erweitern
```typescript
// Fehlende Übersetzungen hinzufügen
customer: {
  // ...
  legalForm: 'Rechtsform',
  selectLegalForm: 'Bitte wählen',
  gmbh: 'GmbH',
  ag: 'AG',
  gbr: 'GbR',
  einzelunternehmen: 'Einzelunternehmen',
  customerType: 'Kundentyp',
  newCustomer: 'Neukunde',
  existingCustomer: 'Bestandskunde',
  customerNumber: 'Kundennummer (intern)',
  annualVolume: 'Erwartetes Jahresvolumen',
  paymentMethod: 'Zahlungsart',
  prepayment: 'Vorkasse',
  cash: 'Bar bei Lieferung',
  invoice: 'Rechnung',
  notes: 'Notizen',
  customField1: 'Freifeld 1',
  customField2: 'Freifeld 2'
}
```

## 🧪 Test-Kriterien
Jedes Feature muss gegen das Original getestet werden:
1. Visuelle Übereinstimmung
2. Funktionale Übereinstimmung
3. Datenfluss-Korrektheit
4. Validierungs-Verhalten
5. State-Persistenz

## 📝 Dokumentation
- [ ] Jedes neue Modul dokumentieren
- [ ] API-Änderungen dokumentieren
- [ ] Test-Protokolle führen

## ⚠️ Wichtige Hinweise
1. **Keine** neuen Features während der Migration
2. **Exakte** Replikation der Original-Funktionalität
3. **Kontinuierliche** Tests gegen Original
4. **Kleine** Commits mit klaren Beschreibungen
5. **Regelmäßige** Standalone-Builds zum Testen