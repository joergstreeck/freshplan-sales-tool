/**
 * FreshPlan Calculator Module
 */

export class Calculator {
    constructor() {
        this.orderValue = 15000;
        this.leadTime = 14;
        this.isPickup = false;
        this.isChain = false;
        
        this.discountRates = {
            base: [
                { min: 0, max: 4999, rate: 0 },
                { min: 5000, max: 9999, rate: 3 },
                { min: 10000, max: 19999, rate: 6 },
                { min: 20000, max: 49999, rate: 9 },
                { min: 50000, max: Infinity, rate: 12 }
            ],
            leadTime: [
                { min: 0, max: 6, rate: 0 },
                { min: 7, max: 13, rate: 1 },
                { min: 14, max: 29, rate: 2 },
                { min: 30, max: Infinity, rate: 3 }
            ],
            pickup: 2,
            chain: 3
        };
        
        this.scenarios = {
            spontan: { orderValue: 8000, leadTime: 3, pickup: false, chain: false },
            geplant: { orderValue: 25000, leadTime: 14, pickup: false, chain: false },
            optimal: { orderValue: 50000, leadTime: 30, pickup: true, chain: false }
        };
    }
    
    initialize() {
        this.setupEventListeners();
        this.loadSavedData();
        this.updateCalculation();
    }
    
    setupEventListeners() {
        // Order value slider
        const orderValueSlider = document.getElementById('orderValue');
        const orderValueDisplay = document.getElementById('orderValueDisplay');
        
        if (orderValueSlider) {
            orderValueSlider.addEventListener('input', (e) => {
                this.orderValue = parseInt(e.target.value);
                orderValueDisplay.textContent = this.formatCurrency(this.orderValue);
                this.updateCalculation();
            });
        }
        
        // Lead time slider
        const leadTimeSlider = document.getElementById('leadTime');
        const leadTimeDisplay = document.getElementById('leadTimeDisplay');
        
        if (leadTimeSlider) {
            leadTimeSlider.addEventListener('input', (e) => {
                this.leadTime = parseInt(e.target.value);
                leadTimeDisplay.textContent = `${this.leadTime} Tage`;
                this.updateCalculation();
            });
        }
        
        // Pickup toggle
        const pickupToggle = document.getElementById('pickupToggle');
        if (pickupToggle) {
            pickupToggle.addEventListener('change', (e) => {
                this.isPickup = e.target.checked;
                this.updateCalculation();
            });
        }
        
        // Chain toggle
        const chainToggle = document.getElementById('chainToggle');
        if (chainToggle) {
            chainToggle.addEventListener('change', (e) => {
                this.isChain = e.target.checked;
                this.updateCalculation();
            });
        }
        
        // Scenario cards
        const scenarioCards = document.querySelectorAll('.scenario-card');
        scenarioCards.forEach(card => {
            card.addEventListener('click', () => {
                const scenario = card.dataset.scenario;
                this.loadScenario(scenario);
            });
        });
    }
    
    loadSavedData() {
        const savedData = localStorage.getItem('calculatorData');
        if (savedData) {
            const data = JSON.parse(savedData);
            this.orderValue = data.orderValue || 15000;
            this.leadTime = data.leadTime || 14;
            this.isPickup = data.isPickup || false;
            this.isChain = data.isChain || false;
            
            // Update UI
            this.updateUI();
        }
    }
    
    updateUI() {
        // Update sliders
        const orderValueSlider = document.getElementById('orderValue');
        const orderValueDisplay = document.getElementById('orderValueDisplay');
        if (orderValueSlider) {
            orderValueSlider.value = this.orderValue;
            orderValueDisplay.textContent = this.formatCurrency(this.orderValue);
        }
        
        const leadTimeSlider = document.getElementById('leadTime');
        const leadTimeDisplay = document.getElementById('leadTimeDisplay');
        if (leadTimeSlider) {
            leadTimeSlider.value = this.leadTime;
            leadTimeDisplay.textContent = `${this.leadTime} Tage`;
        }
        
        // Update toggles
        const pickupToggle = document.getElementById('pickupToggle');
        if (pickupToggle) pickupToggle.checked = this.isPickup;
        
        const chainToggle = document.getElementById('chainToggle');
        if (chainToggle) chainToggle.checked = this.isChain;
    }
    
    updateCalculation() {
        const result = this.calculate();
        
        // Update displays
        document.getElementById('baseDiscount').textContent = `${result.baseDiscount}%`;
        document.getElementById('earlyDiscount').textContent = `${result.earlyDiscount}%`;
        document.getElementById('pickupDiscount').textContent = `${result.pickupDiscount}%`;
        document.getElementById('chainDiscount').textContent = `${result.chainDiscount}%`;
        document.getElementById('totalDiscount').textContent = `${result.totalDiscount}%`;
        document.getElementById('discountAmount').textContent = this.formatCurrency(result.discountAmount);
        document.getElementById('finalPrice').textContent = this.formatCurrency(result.finalPrice);
        
        // Save to app state
        window.appState.calculatorData = result;
        
        // Save to localStorage
        this.saveData();
        
        // Update progress
        window.FreshPlan.updateProgressBar();
    }
    
    calculate() {
        // Calculate base discount
        let baseDiscount = 0;
        for (const range of this.discountRates.base) {
            if (this.orderValue >= range.min && this.orderValue <= range.max) {
                baseDiscount = range.rate;
                break;
            }
        }
        
        // Calculate early booking discount
        let earlyDiscount = 0;
        for (const range of this.discountRates.leadTime) {
            if (this.leadTime >= range.min && this.leadTime <= range.max) {
                earlyDiscount = range.rate;
                break;
            }
        }
        
        // Calculate additional discounts
        const pickupDiscount = this.isPickup ? this.discountRates.pickup : 0;
        const chainDiscount = this.isChain ? this.discountRates.chain : 0;
        
        // Calculate total discount (max 20%)
        const totalDiscount = Math.min(20, baseDiscount + earlyDiscount + pickupDiscount + chainDiscount);
        
        // Calculate amounts
        const discountAmount = this.orderValue * (totalDiscount / 100);
        const finalPrice = this.orderValue - discountAmount;
        
        return {
            orderValue: this.orderValue,
            leadTime: this.leadTime,
            isPickup: this.isPickup,
            isChain: this.isChain,
            baseDiscount,
            earlyDiscount,
            pickupDiscount,
            chainDiscount,
            totalDiscount,
            discountAmount,
            finalPrice
        };
    }
    
    loadScenario(scenarioName) {
        const scenario = this.scenarios[scenarioName];
        if (!scenario) return;
        
        this.orderValue = scenario.orderValue;
        this.leadTime = scenario.leadTime;
        this.isPickup = scenario.pickup;
        this.isChain = scenario.chain;
        
        this.updateUI();
        this.updateCalculation();
        
        // Highlight active scenario
        document.querySelectorAll('.scenario-card').forEach(card => {
            card.classList.remove('active');
        });
        document.querySelector(`[data-scenario="${scenarioName}"]`)?.classList.add('active');
    }
    
    saveData() {
        const data = {
            orderValue: this.orderValue,
            leadTime: this.leadTime,
            isPickup: this.isPickup,
            isChain: this.isChain,
            timestamp: new Date().toISOString()
        };
        
        localStorage.setItem('calculatorData', JSON.stringify(data));
    }
    
    formatCurrency(amount) {
        return new Intl.NumberFormat('de-DE', {
            style: 'currency',
            currency: 'EUR',
            minimumFractionDigits: 0,
            maximumFractionDigits: 0
        }).format(amount);
    }
    
    updateDisplay() {
        // Called on window resize
        this.updateCalculation();
    }
}