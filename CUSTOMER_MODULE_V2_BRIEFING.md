# CustomerModuleV2 - Technisches Briefing für KI-Partner

## Übersicht

Wir haben gerade ein großes Refactoring von `CustomerModule` (alt) zu `CustomerModuleV2` (neu) durchgeführt. Das Ziel war, von einem monolithischen Ansatz zu einer sauberen, mehrschichtigen Architektur zu wechseln.

## Architektur-Vergleich

### Alt: CustomerModule (Phase 1)
```
CustomerModule.ts
├── UI-Logik
├── Business-Logik  
├── Validierung
├── LocalStorage-Zugriff
└── Event-Handling
```
**Problem:** Alles in einer Datei vermischt, schwer testbar, nicht erweiterbar.

### Neu: CustomerModuleV2 (Phase 2)
```
CustomerModuleV2.ts (nur UI)
    ↓
CustomerServiceV2.ts (Business Logic)
    ↓
ICustomerRepository (Interface)
    ↓
LocalStorageCustomerRepository.ts (Implementierung)
```

## Die neue Architektur im Detail

### 1. Domain Layer (Interfaces)
**ICustomerRepository** - Definiert Vertrag für Datenzugriff:
- `saveCustomer(customer: CustomerData): Promise<void>`
- `getCustomerById(id: string): Promise<CustomerData | null>`
- `getAllCustomers(): Promise<CustomerData[]>`
- `deleteAllCustomers(): Promise<void>`
- `getLegacyData(): Promise<any | null>` (für Backward-Compatibility)

**ICustomerValidator** - Definiert Validierungs-Interface:
- `validate(data: CustomerData): Promise<ValidationResult>`
- `validateRequiredFields(data: CustomerData): ValidationResult`

### 2. Infrastructure Layer
**LocalStorageCustomerRepository** - Konkrete Implementierung:
```javascript
// Beispiel-Implementierung
async saveCustomer(customer) {
    const existingData = JSON.parse(localStorage.getItem('freshplanData') || '{}');
    existingData.customer = customer;
    localStorage.setItem('freshplanData', JSON.stringify(existingData));
}
```

### 3. Service Layer
**CustomerServiceV2** - Business Logic:
- Validierung von Kundendaten
- Geschäftsregeln (z.B. Neukunde + Rechnung = Warnung)
- Event-Emission bei wichtigen Aktionen
- Orchestrierung zwischen Repository und UI

### 4. UI Layer
**CustomerModuleV2** - Nur UI-Interaktion:
- Form-Handling
- Event-Listener für Buttons
- Anzeige von Validierungsfehlern
- Delegiert alle Logik an Service

## Vorteile des neuen Ansatzes

1. **Testbarkeit**: Jede Schicht kann isoliert getestet werden
2. **Erweiterbarkeit**: Einfacher Wechsel von LocalStorage zu API
3. **Wartbarkeit**: Klare Trennung der Verantwortlichkeiten
4. **Zukunftssicher**: Vorbereitet für kommende Integrationen

## Aktuelle Probleme

### 🔴 Hauptproblem: Integration funktioniert nicht

**Symptom:** 
- Module wird geladen mit `?phase2=true`
- Aber Save/Clear Buttons reagieren nicht
- Formular-Events werden nicht gebunden

**Root Cause:**
```javascript
// In CustomerModuleV2
bindEvents() {
    // Diese Methode wird aufgerufen, aber...
    this.on(saveBtn, 'click', async () => {
        // Dieser Handler wird nie ausgeführt!
    });
}
```

### 🟡 Sekundäre Probleme:

1. **Legacy-Interferenz**
   - `legacy-script.ts` ist noch aktiv
   - Überschreibt möglicherweise Event-Handler
   - Konkurriert um DOM-Kontrolle

2. **Timing-Issues**
   - Module initialisiert bevor DOM ready
   - Race Conditions zwischen Modulen
   - Tab-Switch Events kommen nicht an

3. **Module Base Class Komplexität**
   ```javascript
   class Module {
       on(target, event, handler) {
           // Komplexe Event-Binding Logik
           // Funktioniert in Tests, aber nicht in App
       }
   }
   ```

## Test-Ergebnisse

### ✅ Was funktioniert:
- **19/19 Unit Tests** bestanden
- Service Layer arbeitet perfekt
- Repository Pattern implementiert
- Validierung funktioniert isoliert

### ❌ Was nicht funktioniert:
- **14/22 E2E Tests** fehlgeschlagen
- UI-Events werden nicht gefeuert
- Integration mit App broken
- Browser-Kompatibilität fraglich

## Code-Beispiele (vereinfacht)

### CustomerModuleV2 (UI Layer)
```javascript
class CustomerModuleV2 extends Module {
    async setup() {
        // Repository und Service initialisieren
        const repository = new LocalStorageCustomerRepository();
        this.customerService = new CustomerServiceV2(repository, null, this.events);
        
        // Gespeicherte Daten laden
        const savedData = await this.customerService.loadInitialCustomerData();
        if (savedData) {
            await this.populateForm(savedData);
        }
    }
    
    bindEvents() {
        // Save Button
        const saveBtn = this.dom.$('.header-btn-save');
        this.on(saveBtn, 'click', async () => {
            const formData = this.collectFormData();
            try {
                await this.customerService.saveCurrentCustomerData(formData);
                alert('✓ Daten gespeichert!');
            } catch (error) {
                alert(`Fehler: ${error.message}`);
            }
        });
    }
}
```

### CustomerServiceV2 (Business Logic)
```javascript
class CustomerServiceV2 {
    async saveCurrentCustomerData(data) {
        // 1. Validierung
        const validation = await this.validator.validate(data);
        if (!validation.isValid) {
            throw new CustomerValidationError('Validierung fehlgeschlagen', validation.errors);
        }
        
        // 2. Geschäftsregeln prüfen
        if (data.customerStatus === 'neukunde' && data.paymentMethod === 'rechnung') {
            this.eventBus.emit('customer:creditCheckRequired', {
                customer: data,
                message: 'Bonitätsprüfung erforderlich'
            });
        }
        
        // 3. Speichern
        await this.repository.saveCustomer(data);
        
        // 4. Event aussenden
        this.eventBus.emit('customer:saved', { customer: data });
    }
}
```

## Lösungsansätze

### Kurzfristig (Quick Fix):
1. Legacy-Script deaktivieren wenn `phase2=true`
2. DOM-Ready sicherstellen vor Module-Init
3. Event-Binding debuggen

### Mittelfristig (Proper Fix):
1. Module-System überarbeiten
2. Event-Bus standardisieren  
3. Saubere Migration von Legacy

### Langfristig (Ideal):
1. Framework einführen (Vue/React)
2. Oder: Web Components nutzen
3. Vollständige Trennung von Alt und Neu

## Fragen an dich als KI-Partner

1. **Architektur:** Ist das Repository Pattern hier over-engineered oder genau richtig?

2. **Integration:** Wie würdest du das Event-Binding Problem lösen?

3. **Migration:** Schrittweise oder Big-Bang? Was empfiehlst du?

4. **Testing:** Wie können wir Integration besser testen?

5. **Alternative:** Sollten wir ein leichtgewichtiges Framework einführen?

## Deine Aufgabe

Bitte analysiere:
1. Die Architektur-Entscheidung (gut/schlecht?)
2. Das Integration-Problem (Lösungsvorschläge?)
3. Die Test-Strategie (was fehlt?)
4. Migration-Strategie (wie vorgehen?)

Wir brauchen deinen frischen Blick und kreative Lösungsansätze!