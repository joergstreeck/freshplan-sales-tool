// =============================
// FreshPlan Discount Calculator
// =============================

// Initialize discount calculator
function initializeDiscountCalculator() {
    const orderValueSlider = document.getElementById('orderValue');
    const leadTimeSlider = document.getElementById('leadTime');
    const pickupToggle = document.getElementById('pickupToggle');
    const chainToggle = document.getElementById('chainToggle');

    // Debounced update function
    const debouncedUpdate = debounce(updateDiscountCalculation, FreshPlan.config.debounce.calculation);

    // Add event listeners
    orderValueSlider.addEventListener('input', debouncedUpdate);
    leadTimeSlider.addEventListener('input', debouncedUpdate);
    pickupToggle.addEventListener('change', updateDiscountCalculation);
    chainToggle.addEventListener('change', updateDiscountCalculation);

    // Initial calculation
    updateDiscountCalculation();
    updateScenarios();
}

// Update discount calculation
function updateDiscountCalculation() {
    const orderValue = parseInt(document.getElementById('orderValue').value);
    const leadTime = parseInt(document.getElementById('leadTime').value);
    const pickup = document.getElementById('pickupToggle').checked;
    const chain = document.getElementById('chainToggle').checked;

    // Update displays
    document.getElementById('orderValueDisplay').textContent = formatCurrency(orderValue);
    document.getElementById('leadTimeDisplay').textContent = leadTime + ' ' + getTranslation('demo.days');

    // Calculate base discount
    let baseDiscount = 0;
    const discountRules = FreshPlan.config.discountRules.base;
    for (const rule of discountRules) {
        if (orderValue >= rule.min) {
            baseDiscount = rule.discount;
            break;
        }
    }

    // Calculate early booking discount
    let earlyDiscount = 0;
    const earlyBookingRules = FreshPlan.config.discountRules.earlyBooking;
    for (const rule of earlyBookingRules) {
        if (leadTime >= rule.min && leadTime <= rule.max) {
            earlyDiscount = rule.discount;
            break;
        }
    }

    // Calculate pickup discount
    const pickupConfig = FreshPlan.config.discountRules.pickup;
    let pickupDiscount = (pickup && orderValue >= pickupConfig.minOrderValue) ? pickupConfig.discount : 0;

    // Update individual discount displays
    document.getElementById('rabattStufe').textContent = 
        `${getTranslation('demo.baseDiscount')}: ${baseDiscount}%`;
    document.getElementById('frühbucherBonus').textContent = 
        `${getTranslation('demo.earlyBooking')}: ${earlyDiscount}%`;

    // Update pickup bonus display with validation hint
    if (pickup && orderValue < pickupConfig.minOrderValue) {
        document.getElementById('pickupBonus').textContent = `+0% (min. ${formatCurrency(pickupConfig.minOrderValue)})`;
        document.getElementById('pickupBonus').style.color = 'var(--danger)';
    } else {
        document.getElementById('pickupBonus').textContent = pickup ? `+${pickupConfig.discount}%` : '+0%';
        document.getElementById('pickupBonus').style.color = 'inherit';
    }

    // Calculate total discount
    let totalDiscount = Math.min(
        baseDiscount + earlyDiscount + pickupDiscount,
        FreshPlan.config.discountRules.maxTotalDiscount
    );

    // Calculate savings
    const savings = orderValue * (totalDiscount / 100);
    const finalPrice = orderValue - savings;

    // Update results
    document.getElementById('totalDiscount').textContent = totalDiscount + '%';
    document.getElementById('totalSavings').textContent = formatCurrency(savings);
    document.getElementById('finalPrice').textContent = formatCurrency(finalPrice);

    // Store calculation
    FreshPlan.state.discountCalculation = {
        orderValue,
        leadTime,
        pickup,
        chain,
        baseDiscount,
        earlyDiscount,
        pickupDiscount,
        totalDiscount,
        savings,
        finalPrice
    };
}

// Update scenarios
function updateScenarios() {
    const scenarioGrid = document.getElementById('scenarioGrid');
    const scenarios = [
        {
            title: getTranslation('demo.spontaneous'),
            orderValue: 15000,
            leadTime: 2,
            pickup: false,
            discount: 6,
            savings: 900
        },
        {
            title: getTranslation('demo.planned'),
            orderValue: 15000,
            leadTime: 16,
            pickup: true,
            discount: 10,
            savings: 1500
        },
        {
            title: getTranslation('demo.optimal'),
            orderValue: 30000,
            leadTime: 30,
            pickup: true,
            discount: 13,
            savings: 3900
        },
        {
            title: getTranslation('demo.kombi'),
            orderValue: 40000,
            leadTime: 20,
            pickup: false,
            discount: 10,
            savings: 4000,
            special: getTranslation('demo.kombiSpecial')
        },
        {
            title: getTranslation('demo.chainBundle'),
            orderValue: 60000,
            leadTime: 15,
            pickup: false,
            discount: 11,
            savings: 6600,
            special: getTranslation('demo.chainSpecial')
        }
    ];

    scenarioGrid.innerHTML = scenarios.map(scenario => `
        <div class="scenario-card">
            <div class="scenario-title">${scenario.title}</div>
            <div class="scenario-details">
                <div>${getTranslation('demo.orderValue')}: ${formatCurrency(scenario.orderValue)}</div>
                <div>${getTranslation('demo.leadTime')}: ${scenario.leadTime} ${getTranslation('demo.days')}</div>
                <div>${scenario.pickup ? getTranslation('demo.pickup') : getTranslation('demo.delivery')}</div>
                ${scenario.special ? `<div style="color: var(--primary-green); font-weight: bold; margin-top: 0.5rem;">${scenario.special}</div>` : ''}
            </div>
            <div class="scenario-savings">${scenario.discount}% = ${formatCurrency(scenario.savings)} ${getTranslation('demo.savings')}</div>
        </div>
    `).join('');
}

// Calculate customer discount (for profile/offer)
function calculateCustomerDiscount() {
    const revenue = FreshPlan.state.customerData.targetRevenue || 0;
    let discount = 0;
    
    const discountRules = FreshPlan.config.discountRules.base;
    for (const rule of discountRules) {
        if (revenue >= rule.min) {
            discount = rule.discount;
            break;
        }
    }
    
    // Add potential bonuses
    if (FreshPlan.state.customerData.customerType === 'chain') {
        discount += 1;
    }
    
    return Math.min(discount, FreshPlan.config.discountRules.maxTotalDiscount);
}

// Export functions
window.initializeDiscountCalculator = initializeDiscountCalculator;
window.updateDiscountCalculation = updateDiscountCalculation;
window.updateScenarios = updateScenarios;
window.calculateCustomerDiscount = calculateCustomerDiscount;