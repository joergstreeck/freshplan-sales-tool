# FreshPlan Legacy Calculator - Detaillierte Analyse für 1:1 Migration

## 1. HTML-Struktur

### 1.1 Haupt-Container
```html
<div id="calculator" class="tab-panel active">
    <div class="demonstrator-container">
        <!-- Zwei-Spalten-Layout -->
        <div class="calculator-section">...</div>
        <div class="info-section">...</div>
    </div>
</div>
```

### 1.2 Calculator Section (Linke Spalte)
```html
<div class="calculator-section">
    <h2 class="section-title" data-i18n="calculatorTitle">FreshPlan Rabattrechner</h2>
    
    <!-- Order Value Slider -->
    <div class="slider-group">
        <div class="slider-label">
            <span data-i18n="orderValue">Bestellwert</span>
            <span class="slider-value" id="orderValueDisplay">€15.000</span>
        </div>
        <input type="range" id="orderValue" class="slider" 
               min="1000" max="100000" step="1000" value="15000" 
               oninput="updateCalculator()">
    </div>
    
    <!-- Lead Time Slider -->
    <div class="slider-group">
        <div class="slider-label">
            <span data-i18n="leadTime">Vorlaufzeit</span>
            <span class="slider-value" id="leadTimeDisplay">14 Tage</span>
        </div>
        <input type="range" id="leadTime" class="slider" 
               min="1" max="30" step="1" value="14" 
               oninput="updateCalculator()">
    </div>
    
    <!-- Pickup Checkbox -->
    <div class="checkbox-label">
        <input type="checkbox" id="pickupToggle" class="checkbox" onchange="updateCalculator()">
        <span data-i18n="pickup">Abholung (Mindestbestellwert: 5.000€ netto)</span>
    </div>
    
    <!-- Results Container -->
    <div class="results-container">
        <!-- Result Grid -->
        <div class="result-grid">
            <div class="result-item">
                <span class="result-label" data-i18n="baseDiscount">Basisrabatt</span>
                <span class="result-value" id="baseDiscount">6%</span>
            </div>
            <div class="result-item">
                <span class="result-label" data-i18n="earlyDiscount">Frühbucher</span>
                <span class="result-value" id="earlyDiscount">1%</span>
            </div>
            <div class="result-item">
                <span class="result-label" data-i18n="pickupDiscount">Abholung</span>
                <span class="result-value" id="pickupDiscount">0%</span>
            </div>
        </div>
        
        <!-- Total Discount -->
        <div class="total-discount">
            <span data-i18n="totalDiscount">Gesamtrabatt</span>
            <span class="total-value" id="totalDiscount">7%</span>
        </div>
        
        <!-- Savings Display -->
        <div class="savings-display">
            <div class="savings-item">
                <span data-i18n="savings">Ersparnis</span>
                <span class="savings-value" id="discountAmount">€1.050</span>
            </div>
            <div class="savings-item highlight">
                <span data-i18n="finalPrice">Endpreis</span>
                <span class="savings-value" id="finalPrice">€13.950</span>
            </div>
        </div>
    </div>
</div>
```

### 1.3 Info Section (Rechte Spalte)
```html
<div class="info-section">
    <!-- Discount System Info Card -->
    <div class="info-card">
        <div class="info-header">
            <h3 data-i18n="discountSystem">FreshPlan-Rabattsystem</h3>
        </div>
        <div class="info-content" style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;">
            <!-- Basisrabatt-Tabelle und Frühbucher-Tabelle -->
        </div>
        <div style="text-align: center; margin-top: 0.75rem; padding-top: 0.75rem; border-top: 2px solid var(--primary-green);">
            <strong style="color: var(--primary-green); font-size: 0.9rem;" data-i18n="maxTotalDiscount">
                Maximaler Gesamtrabatt: 15%
            </strong>
        </div>
    </div>
    
    <!-- Chain Customer Info Card -->
    <div class="info-card">
        <div class="info-header">
            <h3 data-i18n="chainCustomerRegulation">Kettenkundenregelung</h3>
        </div>
        <!-- Kettenkundenregelung Details -->
    </div>
    
    <!-- Example Scenarios Card -->
    <div class="info-card" style="background: rgba(148, 196, 86, 0.1); border: 1px solid rgba(148, 196, 86, 0.3);">
        <div class="info-header">
            <h3 style="color: var(--primary-green);" data-i18n="exampleScenarios">Beispielszenarien</h3>
        </div>
        <div class="examples-container" style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 1rem; margin-top: 1rem;">
            <!-- 3 Beispiel-Szenarien -->
        </div>
    </div>
</div>
```

## 2. CSS-Klassen und Styling

### 2.1 Layout-Klassen
- `.demonstrator-container`: Grid mit 2 Spalten (1fr 1fr), Gap: 2rem
- `.calculator-section`: Weiße Box mit Padding 2rem, Border-Radius 12px, Box-Shadow
- `.info-section`: Flex-Column mit Gap 1.5rem
- `.info-card`: Weiße Box mit Padding 1.5rem, Border-Radius 12px, Box-Shadow

### 2.2 Slider-Styling
```css
.slider {
    width: 100%;
    height: 6px;
    border-radius: 3px;
    background: #e0e0e0;
    outline: none;
    -webkit-appearance: none;
}

.slider::-webkit-slider-thumb {
    width: 20px;
    height: 20px;
    border-radius: 50%;
    background: var(--primary-green);
    cursor: pointer;
    border: 2px solid white;
    box-shadow: 0 2px 8px rgba(148, 196, 86, 0.3);
}
```

### 2.3 Wichtige Farben
- Primary Green: `#94C456`
- Primary Blue: `#004F7B`
- Dark Green: `#7FB040`
- Text Dark: `#333`
- Text Light: `#666`
- Border Light: `#E0E0E0`
- Background Light: `#F8F9FA`

### 2.4 Spezielle Effekte
- **Hover auf Example Items**: 
  - Transform: translateY(-2px)
  - Box-Shadow: 0 4px 12px rgba(148, 196, 86, 0.3)
  - Border-Color: var(--primary-green)
  - Background: rgba(148, 196, 86, 0.05)

- **Total Discount Box**:
  - Gradient: linear-gradient(135deg, #94C456, #7FB069)
  - Text: Weiß mit Text-Shadow
  - Font-Size: 2rem für den Wert

## 3. JavaScript-Logik

### 3.1 Rabatt-Berechnung
```javascript
// Basisrabatt-Stufen
const baseDiscountRates = {
    5000: 3,    // ab 5.000€: 3%
    15000: 6,   // ab 15.000€: 6%
    30000: 8,   // ab 30.000€: 8%
    50000: 9,   // ab 50.000€: 9%
    75000: 10   // ab 75.000€: 10%
};

// Frühbucher-Rabatt
const earlyBookingRates = {
    10: 1,  // ab 10 Tage: 1%
    15: 2,  // ab 15 Tage: 2%
    30: 3   // ab 30 Tage: 3%
};

// Zusätzliche Rabatte
const pickupDiscount = 2; // 2% für Abholung
const maxTotalDiscount = 15; // Maximaler Gesamtrabatt
```

### 3.2 Update-Funktionen
```javascript
function updateCalculator() {
    // 1. Werte aus Slidern lesen
    const orderValue = document.getElementById('orderValue').value;
    const leadTime = document.getElementById('leadTime').value;
    const isPickup = document.getElementById('pickupToggle').checked;
    
    // 2. Display-Werte aktualisieren
    document.getElementById('orderValueDisplay').textContent = formatCurrency(orderValue);
    document.getElementById('leadTimeDisplay').textContent = `${leadTime} Tage`;
    
    // 3. Rabatte berechnen
    const baseDiscount = calculateBaseDiscount(orderValue);
    const earlyDiscount = calculateEarlyDiscount(leadTime);
    const pickupDiscountValue = isPickup ? 2 : 0;
    
    // 4. Gesamtrabatt (max 15%)
    const totalDiscount = Math.min(baseDiscount + earlyDiscount + pickupDiscountValue, 15);
    
    // 5. Beträge berechnen
    const discountAmount = orderValue * (totalDiscount / 100);
    const finalPrice = orderValue - discountAmount;
    
    // 6. UI aktualisieren
    updateUI(baseDiscount, earlyDiscount, pickupDiscountValue, totalDiscount, discountAmount, finalPrice);
}
```

### 3.3 Beispiel-Szenarien
```javascript
const scenarios = {
    hotel: {
        orderValue: 35000,
        leadTime: 21,
        pickup: true,
        expectedDiscount: 12  // 8% + 2% + 2%
    },
    klinik: {
        orderValue: 65000,
        leadTime: 30,
        pickup: false,
        expectedDiscount: 12  // 9% + 3%
    },
    restaurant: {
        orderValue: 8500,
        leadTime: 14,
        pickup: true,
        expectedDiscount: 6   // 3% + 1% + 2%
    }
};

function loadExample(type) {
    const scenario = scenarios[type];
    document.getElementById('orderValue').value = scenario.orderValue;
    document.getElementById('leadTime').value = scenario.leadTime;
    document.getElementById('pickupToggle').checked = scenario.pickup;
    updateCalculator();
}
```

## 4. Visuelle Details

### 4.1 Typography
- Überschriften: Font-Weight 700, Farbe Primary Blue
- Labels: Font-Weight 500, Farbe Text Dark
- Werte: Font-Weight 700, verschiedene Größen
- Kleine Texte: Font-Size 0.875rem (14px)

### 4.2 Spacing
- Standard Padding: 2rem (32px) für Container
- Kleineres Padding: 1rem (16px) für Items
- Gap zwischen Elementen: 1rem oder 1.5rem
- Margin-Bottom für Sections: 2rem

### 4.3 Borders & Shadows
- Standard Border: 1px solid rgba(0, 0, 0, 0.06)
- Hover Border: rgba(148, 196, 86, 0.3)
- Box-Shadow: 0 2px 12px rgba(0, 0, 0, 0.06)
- Hover Shadow: 0 4px 12px rgba(148, 196, 86, 0.3)

### 4.4 Responsive Design
- Bei < 1024px: Einspaltig statt zweispaltig
- Info-Section kommt nach Calculator-Section
- Beispiel-Container: 1 Spalte statt 3

## 5. Animationen und Übergänge

### 5.1 Standard-Transition
```css
transition: all 0.3s ease;
```

### 5.2 Hover-Effekte
- Buttons: Transform translateY(-1px)
- Cards: Transform translateY(-2px)
- Slider Thumb: Transform scale(1.15)

## 6. Wichtige Funktionalitäten

### 6.1 Echtzeit-Updates
- Alle Änderungen werden sofort berechnet (oninput/onchange)
- Keine Submit-Buttons nötig

### 6.2 Formatierung
- Währung: Deutsch (€), ohne Dezimalstellen
- Prozente: Mit %-Zeichen
- Tage: Mit "Tage" Suffix

### 6.3 Validierung
- Abholung nur ab 5.000€ Bestellwert
- Maximaler Gesamtrabatt: 15%

## 7. Internationalisierung
Alle Texte verwenden `data-i18n` Attribute für Übersetzungen.

## 8. Barrierefreiheit
- Semantische HTML-Elemente
- Labels für alle Inputs
- Kontrastreiche Farben
- Fokus-Indikatoren

Diese Dokumentation enthält alle Details für eine perfekte 1:1-Migration des Calculators.