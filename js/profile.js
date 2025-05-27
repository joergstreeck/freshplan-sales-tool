// =============================
// FreshPlan Customer Profile
// =============================

// Generate customer profile
function generateCustomerProfile() {
    const savedData = FreshPlan.storage.getItem(FreshPlan.config.storageKeys.customerData);
    
    if (!savedData) {
        document.getElementById('profileOverview').style.display = 'none';
        document.getElementById('profileEmpty').style.display = 'block';
        return;
    }
    
    FreshPlan.state.customerData = JSON.parse(savedData);
    
    document.getElementById('profileOverview').style.display = 'block';
    document.getElementById('profileEmpty').style.display = 'none';
    
    // Generate profile sections
    generateBasicInfo();
    generatePotentialAnalysis();
    generateSalesStrategy();
    generateKeyArguments();
    generateNextSteps();
}

// Generate basic info
function generateBasicInfo() {
    const container = document.getElementById('profileBasicInfo');
    const typeText = FreshPlan.state.customerData.customerType === 'chain' ? 'Kette/Gruppe' : 'Einzelstandort';
    const industryText = getIndustryName(FreshPlan.state.customerData.industry);
    
    container.innerHTML = `
        <div class="profile-info-item"><strong>Firma:</strong> ${FreshPlan.state.customerData.companyName}</div>
        <div class="profile-info-item"><strong>Typ:</strong> ${typeText}</div>
        <div class="profile-info-item"><strong>Branche:</strong> ${industryText}</div>
        <div class="profile-info-item"><strong>Kontakt:</strong> ${FreshPlan.state.customerData.contactName}</div>
        <div class="profile-info-item"><strong>Position:</strong> ${FreshPlan.state.customerData.contactPosition || 'N/A'}</div>
        <div class="profile-info-item"><strong>E-Mail:</strong> ${FreshPlan.state.customerData.contactEmail}</div>
        <div class="profile-info-item"><strong>Telefon:</strong> ${FreshPlan.state.customerData.contactPhone}</div>
        <div class="profile-info-item"><strong>Standort:</strong> ${FreshPlan.state.customerData.zipCode} ${FreshPlan.state.customerData.city}</div>
    `;
}

// Generate potential analysis
function generatePotentialAnalysis() {
    const container = document.getElementById('profilePotential');
    const targetRevenue = FreshPlan.state.customerData.targetRevenue || 0;
    const discount = calculateCustomerDiscount();
    const savings = targetRevenue * (discount / 100);
    
    container.innerHTML = `
        <div class="potential-metrics">
            <div class="metric-item">
                <span>${getTranslation('profile.targetVolume')}:</span>
                <span class="metric-value">${formatCurrency(targetRevenue)}</span>
            </div>
            <div class="metric-item">
                <span>${getTranslation('profile.savingsPotential')}:</span>
                <span class="metric-value">${formatCurrency(savings)}</span>
            </div>
            <div class="metric-item">
                <span>${getTranslation('profile.contractValue')}:</span>
                <span class="metric-value">${formatCurrency(targetRevenue - savings)}</span>
            </div>
        </div>
    `;
    
    // Add vending potential if interested
    if (FreshPlan.state.customerData.vendingInterest) {
        const vendingConfig = FreshPlan.config.vending;
        const vendingRevenue = FreshPlan.state.customerData.vendingLocations * 
                              FreshPlan.state.customerData.vendingDaily * 
                              vendingConfig.avgPrice * 
                              vendingConfig.operatingDays;
        container.innerHTML += `
            <div class="info-box" style="margin-top: 1rem;">
                <h4>Vending-Potenzial</h4>
                <p>Zusätzliches Umsatzpotenzial: ${formatCurrency(vendingRevenue)}/Jahr</p>
            </div>
        `;
    }
}

// Generate sales strategy
function generateSalesStrategy() {
    const container = document.getElementById('profileStrategy');
    const strategy = getIndustryStrategy(FreshPlan.state.customerData.industry);
    
    container.innerHTML = `
        <div class="strategy-grid">
            <div class="strategy-section">
                <h4>${getTranslation('profile.decisionCriteria')}</h4>
                <ul>${strategy.criteria.map(c => `<li>${c}</li>`).join('')}</ul>
            </div>
            <div class="strategy-section">
                <h4>${getTranslation('profile.painPoints')}</h4>
                <ul>${strategy.painPoints.map(p => `<li>${p}</li>`).join('')}</ul>
            </div>
            <div class="strategy-section">
                <h4>${getTranslation('profile.opportunities')}</h4>
                <ul>${strategy.opportunities.map(o => `<li>${o}</li>`).join('')}</ul>
            </div>
            <div class="strategy-section">
                <h4>${getTranslation('profile.competitiveAdvantage')}</h4>
                <ul>${strategy.advantages.map(a => `<li>${a}</li>`).join('')}</ul>
            </div>
        </div>
    `;
}

// Generate key arguments
function generateKeyArguments() {
    const container = document.getElementById('profileArguments');
    const arguments = getKeyArguments();
    
    container.innerHTML = `
        <div class="arguments-list">
            ${arguments.map((arg, index) => `
                <div class="argument-item">
                    <div class="argument-number">${index + 1}</div>
                    <div>
                        <strong>${arg.title}</strong>
                        <p>${arg.description}</p>
                    </div>
                </div>
            `).join('')}
        </div>
    `;
}

// Generate next steps
function generateNextSteps() {
    const container = document.getElementById('profileNextSteps');
    
    container.innerHTML = `
        <div class="steps-timeline">
            <div class="step-item active">
                <div class="step-marker">1</div>
                <div class="step-content">
                    <strong>Erstkontakt</strong><br>
                    <small>Bedarfsanalyse</small>
                </div>
            </div>
            <div class="step-item">
                <div class="step-marker">2</div>
                <div class="step-content">
                    <strong>Demo</strong><br>
                    <small>Rabatt-Demonstrator</small>
                </div>
            </div>
            <div class="step-item">
                <div class="step-marker">3</div>
                <div class="step-content">
                    <strong>Angebot</strong><br>
                    <small>Individualisiert</small>
                </div>
            </div>
            <div class="step-item">
                <div class="step-marker">4</div>
                <div class="step-content">
                    <strong>Verhandlung</strong><br>
                    <small>Konditionen</small>
                </div>
            </div>
            <div class="step-item">
                <div class="step-marker">5</div>
                <div class="step-content">
                    <strong>Abschluss</strong><br>
                    <small>Vertragsunterzeichnung</small>
                </div>
            </div>
        </div>
        
        <div class="info-box" style="margin-top: 2rem;">
            <h4>Empfohlene nächste Aktion</h4>
            <p>${getRecommendedAction()}</p>
        </div>
    `;
}

// Get industry strategy
function getIndustryStrategy(industry) {
    const strategies = {
        hotel: {
            criteria: ['Qualität & Frische', 'Flexibilität bei Bestellmengen', 'Zuverlässige Lieferung'],
            painPoints: ['Schwankende Auslastung', 'Hohe Personalkosten', 'Food Waste'],
            opportunities: ['Cook&Fresh für Room Service', 'Flexible Kombi-Bestellungen', 'Frühstücks-Komplettlösungen'],
            advantages: ['12 Monate Preisstabilität', 'Keine Mindestabnahme', 'Rabatte ab 5.000€']
        },
        altenheim: {
            criteria: ['Konsistenzanpassung', 'Diätformen-Vielfalt', 'Einfache Zubereitung'],
            painPoints: ['Fachkräftemangel Küche', 'Steigende Lebensmittelkosten', 'Hygiene-Anforderungen'],
            opportunities: ['Komplette Menülinien', 'Konsistenz-angepasste Produkte', 'Notfall-Vorrat'],
            advantages: ['MDK-konforme Produkte', 'Transparente Allergenkennzeichnung', 'Schulungsangebote']
        },
        krankenhaus: {
            criteria: ['Hygiene & Sicherheit', 'Kosteneffizienz', 'Patientenzufriedenheit'],
            painPoints: ['Kostendruck', 'Unterschiedliche Kostformen', 'Personal-Engpässe'],
            opportunities: ['Wahlleistung Premium-Menüs', 'Outsourcing Wochenend-Küche', 'Mitarbeiter-Catering'],
            advantages: ['HACCP-zertifiziert', 'Individuelle Portionierung', 'Notfall-Lieferservice']
        },
        betriebsrestaurant: {
            criteria: ['Attraktive Preise', 'Abwechslung', 'Moderne Gerichte'],
            painPoints: ['Mitarbeiterzufriedenheit', 'Subventionsdruck', 'Trend-Anforderungen'],
            opportunities: ['Vending als Ergänzung', 'Aktionswochen', 'Gesundheitsprogramme'],
            advantages: ['Mengenrabatte optimal nutzen', 'Trend-Produkte im Portfolio', 'Marketing-Unterstützung']
        },
        restaurant: {
            criteria: ['Konstante Qualität', 'Faire Preise', 'Innovations-Partner'],
            painPoints: ['Personalmangel', 'Schwankende Gästezahlen', 'Margendruck'],
            opportunities: ['Sous-Vide Premiumprodukte', 'Saisonale Spezialitäten', 'Convenience ohne Kompromisse'],
            advantages: ['Kalkulationssicherheit', 'Kleine Verpackungseinheiten', 'Inspiration & Rezepte']
        }
    };
    
    return strategies[industry] || strategies.restaurant;
}

// Get key arguments
function getKeyArguments() {
    return [
        {
            title: 'Garantierte Preisstabilität',
            description: '12 Monate keine Preiserhöhung - volle Planungssicherheit für Ihr Budget'
        },
        {
            title: 'Keine Mindestabnahme',
            description: 'Bestellen Sie nur was Sie brauchen - das Zielvolumen ist unverbindlich'
        },
        {
            title: 'Transparentes Rabattsystem',
            description: 'Je mehr Sie bestellen, desto mehr sparen Sie - bis zu 13% Gesamtrabatt möglich'
        },
        {
            title: 'Flexibilität bei Großbestellungen',
            description: 'Kombi-Lieferungen ab 30.000€ - teilen Sie auf 2 Termine auf'
        },
        {
            title: 'Persönlicher Ansprechpartner',
            description: 'Ihr dedizierter Betreuer kennt Ihre Bedürfnisse und ist immer erreichbar'
        }
    ];
}

// Get recommended action
function getRecommendedAction() {
    const revenue = FreshPlan.state.customerData.targetRevenue || 0;
    const isChain = FreshPlan.state.customerData.customerType === 'chain';
    
    if (revenue >= 50000) {
        return 'Vereinbaren Sie einen Vor-Ort-Termin zur Bestandsaufnahme und Großkunden-Beratung.';
    } else if (isChain) {
        return 'Präsentieren Sie die Vorteile der Kettenbündelung mit zentraler Abrechnung.';
    } else if (revenue >= 15000) {
        return 'Führen Sie eine Live-Demo des Rabatt-Systems durch und zeigen Sie das Einsparpotenzial.';
    } else {
        return 'Starten Sie mit einer Testbestellung und bauen Sie das Vertrauen schrittweise auf.';
    }
}

// Export functions
window.generateCustomerProfile = generateCustomerProfile;