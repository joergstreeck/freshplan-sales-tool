/**
 * Profile Module - TypeScript version
 * Generates customer profiles with analysis and recommendations
 */

import Module from '../core/Module';
import { useStore } from '../store';
import { formatCurrency, formatPercent } from '../utils/formatting';
import type { 
  CustomerData, 
  Industry, 
  ProfileData,
  VolumeCalculation,
  SavingsCalculation,
  VendingPotential,
  Recommendation,
  SalesStrategy
} from '../types';

interface IndustryStrategy {
  approach: string;
  keyPoints: string[];
  painPoints: string[];
}

interface NextStep {
  action: string;
  title: string;
  description: string;
  icon: string;
}

export default class ProfileModule extends Module {
  private profileData: ProfileData | null = null;

  constructor() {
    super('profile');
  }

  async setup(): Promise<void> {
    // Check if customer data exists
    this.checkCustomerData();
  }

  bindEvents(): void {
    // Generate profile button (if exists)
    this.on('#generateProfileBtn', 'click', () => {
      this.generateProfile();
    });

    // Next step actions
    this.on(document, 'click', '.next-step-btn', (e: Event) => {
      const button = e.target as HTMLElement;
      const action = button.dataset.action;
      if (action) {
        this.handleNextStep(action);
      }
    });
  }

  subscribeToState(): void {
    // React to customer data changes
    useStore.subscribe(
      (state) => state.customer.data,
      (data) => {
        if (data) {
          this.generateProfile(data);
        }
      }
    );

    // React to tab changes
    this.events.on('module:tabs:switched', (tab) => {
      if (tab === 'profile') {
        this.checkCustomerData();
      }
    });
  }

  /**
   * Check if customer data exists
   */
  private checkCustomerData(): void {
    const customerData = useStore.getState().customer.data;
    
    const emptyState = this.dom.$('#profileEmpty');
    const profileContent = this.dom.$('#profileOverview');

    if (!customerData || !customerData.companyName) {
      this.dom.show(emptyState);
      this.dom.hide(profileContent);
    } else {
      this.dom.hide(emptyState);
      this.dom.show(profileContent);
      this.generateProfile(customerData);
    }
  }

  /**
   * Generate customer profile
   */
  private generateProfile(customerData?: CustomerData | null): void {
    if (!customerData) {
      customerData = useStore.getState().customer.data;
    }

    if (!customerData || !customerData.companyName) {
      this.showError('Keine Kundendaten vorhanden');
      return;
    }

    // Generate profile data
    this.profileData = this.analyzeCustomer(customerData);
    
    // Update UI
    this.renderProfile();
    
    // Save profile
    useStore.getState().setProfileData(this.profileData);
    
    // Emit event
    this.emit('generated', this.profileData);
  }

  /**
   * Analyze customer data
   */
  private analyzeCustomer(data: CustomerData): ProfileData {
    const industry = data.industry || 'restaurant';
    
    // Calculate potential volume
    const volume = this.calculateVolume(data, industry);
    
    // Calculate potential savings
    const savings = this.calculateSavings(volume);
    
    // Generate recommendations
    const recommendations = this.generateRecommendations(data, industry, volume);
    
    // Generate strategy
    const strategy = this.generateStrategy(data, industry);
    
    return {
      customerData: data,
      analysis: {
        estimatedVolume: volume.annual,
        monthlyVolume: volume.monthly,
        potentialSavings: savings,
        growthPotential: volume.growth,
        vendingPotential: this.calculateVendingPotential(data)
      },
      recommendations,
      strategy,
      generatedAt: new Date().toISOString()
    };
  }

  /**
   * Calculate volume based on industry
   */
  private calculateVolume(data: CustomerData, industry: Industry): VolumeCalculation {
    let monthlyVolume = 0;
    
    switch (industry) {
      case 'hotel':
        const rooms = data.rooms || 100;
        const occupancy = data.occupancyRate || 75;
        const breakfastPrice = data.breakfastPrice || 8;
        
        // Calculate based on breakfast and restaurant
        const dailyBreakfasts = rooms * (occupancy / 100) * 0.7; // 70% take breakfast
        const breakfastRevenue = dailyBreakfasts * breakfastPrice * 30;
        const restaurantRevenue = rooms * 0.3 * 35 * 30; // 30% use restaurant
        
        monthlyVolume = (breakfastRevenue + restaurantRevenue) * 0.4; // 40% food cost
        break;
        
      case 'altenheim':
        const residents = data.residents || 80;
        const mealsPerDay = data.mealsPerDay || 3;
        const mealCost = 4.5;
        
        monthlyVolume = residents * mealsPerDay * mealCost * 30;
        break;
        
      case 'krankenhaus':
        const beds = data.beds || 200;
        const staffMeals = data.staffMeals || 50;
        const patientMealCost = 12;
        const staffMealCost = 8;
        
        const dailyPatientMeals = beds * 0.85 * 3; // 85% occupancy, 3 meals
        const dailyStaffMeals = staffMeals;
        
        monthlyVolume = (dailyPatientMeals * patientMealCost + dailyStaffMeals * staffMealCost) * 30;
        break;
        
      case 'betriebsrestaurant':
        const employees = data.employees || 500;
        const utilization = data.utilizationRate || 60;
        const avgMealPrice = 7;
        
        const dailyMeals = employees * (utilization / 100);
        monthlyVolume = dailyMeals * avgMealPrice * 22; // 22 working days
        break;
        
      case 'restaurant':
        const seats = data.seats || 80;
        const turnsPerDay = data.turnsPerDay || 2;
        const operatingDays = data.operatingDays || 300;
        const avgTicket = 25;
        const foodCostRatio = 0.35;
        
        const monthlyGuests = seats * turnsPerDay * (operatingDays / 12);
        monthlyVolume = monthlyGuests * avgTicket * foodCostRatio;
        break;
        
      case 'kette':
        const locations = data.totalLocations || 1;
        const avgOrder = data.avgOrderPerLocation || 10000;
        monthlyVolume = locations * avgOrder;
        break;
        
      default:
        // Fallback calculation
        monthlyVolume = 15000;
    }
    
    // Add growth potential
    const annualVolume = monthlyVolume * 12;
    const growthRate = 0.05; // 5% annual growth
    const growthPotential = annualVolume * growthRate;
    
    return {
      monthly: Math.round(monthlyVolume),
      annual: Math.round(annualVolume),
      growth: Math.round(growthPotential)
    };
  }

  /**
   * Calculate potential savings
   */
  private calculateSavings(volume: VolumeCalculation): SavingsCalculation {
    // Assume average discount of 10-15%
    const avgDiscount = 0.125;
    const annualSavings = volume.annual * avgDiscount;
    
    return {
      annual: Math.round(annualSavings),
      monthly: Math.round(annualSavings / 12),
      percentage: avgDiscount * 100
    };
  }

  /**
   * Calculate vending potential
   */
  private calculateVendingPotential(data: CustomerData): VendingPotential | null {
    if (!data.vendingInterest) {
      return null;
    }
    
    const locations = data.vendingLocations || 1;
    const dailyTransactions = data.vendingDaily || 50;
    const avgTransaction = 3.5;
    const margin = 0.25;
    
    const dailyRevenue = locations * dailyTransactions * avgTransaction;
    const monthlyRevenue = dailyRevenue * 30;
    const monthlyProfit = monthlyRevenue * margin;
    
    return {
      locations,
      dailyTransactions: dailyTransactions * locations,
      monthlyRevenue: Math.round(monthlyRevenue),
      monthlyProfit: Math.round(monthlyProfit)
    };
  }

  /**
   * Generate recommendations
   */
  private generateRecommendations(
    data: CustomerData, 
    industry: Industry, 
    volume: VolumeCalculation
  ): Recommendation[] {
    const recommendations: Recommendation[] = [];
    
    // Volume-based recommendations
    if (volume.annual > 100000) {
      recommendations.push({
        type: 'premium',
        title: 'Premium-Partnerschaft',
        description: 'Bei Ihrem Volumen profitieren Sie von maximalen Rabatten und persönlicher Betreuung.'
      });
    }
    
    // Industry-specific recommendations
    const industryRecs: Partial<Record<Industry, Recommendation[]>> = {
      hotel: [
        {
          type: 'breakfast',
          title: 'Frühstücks-Sortiment',
          description: 'Optimiertes Sortiment für Hotel-Frühstück mit Convenience-Produkten'
        },
        {
          type: 'seasonal',
          title: 'Saisonale Angebote',
          description: 'Spezielle Konditionen für Saisonware und Event-Catering'
        }
      ],
      krankenhaus: [
        {
          type: 'special',
          title: 'Diätetische Kost',
          description: 'Spezialsortiment für besondere Ernährungsanforderungen'
        },
        {
          type: 'hygiene',
          title: 'Hygiene-Standards',
          description: 'HACCP-konforme Lieferung und Dokumentation'
        }
      ],
      betriebsrestaurant: [
        {
          type: 'variety',
          title: 'Abwechslungsreiches Sortiment',
          description: 'Vielfältiges Angebot für tägliche Menüvariation'
        },
        {
          type: 'healthy',
          title: 'Gesunde Alternativen',
          description: 'Bio- und Vollwertkost für gesundheitsbewusste Mitarbeiter'
        }
      ]
    };
    
    if (industryRecs[industry]) {
      recommendations.push(...industryRecs[industry]!);
    }
    
    // Chain recommendations
    if (data.customerType === 'chain') {
      recommendations.push({
        type: 'chain',
        title: 'Kettenbündelung',
        description: 'Nutzen Sie Wochenbündelung für höhere Rabattstufen'
      });
    }
    
    // Vending recommendation
    if (data.vendingInterest) {
      recommendations.push({
        type: 'vending',
        title: 'Vending-Lösung',
        description: 'Zusätzlicher Umsatz durch Automaten-Standorte'
      });
    }
    
    return recommendations;
  }

  /**
   * Generate sales strategy
   */
  private generateStrategy(_data: CustomerData, industry: Industry): SalesStrategy {
    const strategies: Partial<Record<Industry, IndustryStrategy>> = {
      hotel: {
        approach: 'Fokus auf Convenience und Qualität',
        keyPoints: [
          'Zeitersparnis durch vorverarbeitete Produkte',
          'Konstante Qualität für Gästezufriedenheit',
          'Flexible Lieferzeiten für Saisongeschäft'
        ],
        painPoints: [
          'Schwankende Auslastung',
          'Hohe Qualitätsansprüche der Gäste',
          'Personalmangel in der Küche'
        ]
      },
      krankenhaus: {
        approach: 'Zuverlässigkeit und Spezialprodukte',
        keyPoints: [
          'Garantierte Liefersicherheit',
          'Diätetische Spezialkost',
          'Hygiene-Dokumentation'
        ],
        painPoints: [
          'Strenge Hygienevorgaben',
          'Spezielle Ernährungsanforderungen',
          'Kostendruck im Gesundheitswesen'
        ]
      },
      betriebsrestaurant: {
        approach: 'Vielfalt und Wirtschaftlichkeit',
        keyPoints: [
          'Abwechslungsreiches Sortiment',
          'Attraktive Großgebinde',
          'Menüplanung-Support'
        ],
        painPoints: [
          'Tägliche Abwechslung gefordert',
          'Preisdruck durch Subventionen',
          'Verschiedene Ernährungsstile'
        ]
      }
    };
    
    const strategy = strategies[industry] || {
      approach: 'Individuelle Beratung',
      keyPoints: [
        'Maßgeschneidertes Sortiment',
        'Flexible Konditionen',
        'Persönlicher Service'
      ],
      painPoints: [
        'Optimierung der Warenwirtschaft',
        'Kostenkontrolle',
        'Qualitätssicherung'
      ]
    };
    
    return {
      approach: strategy.approach,
      keyPoints: strategy.keyPoints,
      painPoints: strategy.painPoints
    };
  }

  /**
   * Render profile UI
   */
  private renderProfile(): void {
    if (!this.profileData) return;
    
    const { customerData, analysis, recommendations, strategy } = this.profileData;
    
    // Basic info
    this.renderBasicInfo(customerData);
    
    // Potential analysis
    this.renderPotentialAnalysis(analysis);
    
    // Sales strategy
    this.renderStrategy(strategy);
    
    // Key arguments
    this.renderKeyArguments();
    
    // Recommended products
    this.renderRecommendations(recommendations);
    
    // Next steps
    this.renderNextSteps();
  }

  /**
   * Render basic customer info
   */
  private renderBasicInfo(data: CustomerData): void {
    const container = this.dom.$('#profileBasicInfo');
    if (!container) return;
    
    const info = [
      { label: 'Firma', value: data.companyName },
      { label: 'Branche', value: this.translate(`customer.${data.industry}`) },
      { label: 'Typ', value: data.customerType === 'chain' ? 'Kette/Gruppe' : 'Einzelstandort' },
      { label: 'Ansprechpartner', value: data.contactName },
      { label: 'E-Mail', value: data.contactEmail },
      { label: 'Telefon', value: data.contactPhone }
    ];
    
    if (data.customerType === 'chain') {
      info.push({ label: 'Standorte', value: String(data.totalLocations) });
    }
    
    container.innerHTML = info
      .filter(item => item.value)
      .map(item => `
        <div class="info-item">
          <span class="info-label">${item.label}:</span>
          <span class="info-value">${item.value}</span>
        </div>
      `).join('');
  }

  /**
   * Render potential analysis
   */
  private renderPotentialAnalysis(analysis: ProfileData['analysis']): void {
    const container = this.dom.$('#profilePotential');
    if (!container) return;
    
    let html = `
      <div class="potential-grid">
        <div class="potential-item">
          <h4>Geschätztes Volumen</h4>
          <div class="potential-value">${formatCurrency(analysis.estimatedVolume)}</div>
          <div class="potential-detail">ca. ${formatCurrency(analysis.monthlyVolume)}/Monat</div>
        </div>
        <div class="potential-item">
          <h4>Einsparpotenzial</h4>
          <div class="potential-value">${formatCurrency(analysis.potentialSavings.annual)}</div>
          <div class="potential-detail">bei ${formatPercent(analysis.potentialSavings.percentage / 100)} Rabatt</div>
        </div>
        <div class="potential-item">
          <h4>Wachstumspotenzial</h4>
          <div class="potential-value">+${formatCurrency(analysis.growthPotential)}</div>
          <div class="potential-detail">bei 5% jährlichem Wachstum</div>
        </div>
    `;
    
    if (analysis.vendingPotential) {
      html += `
        <div class="potential-item">
          <h4>Vending-Potenzial</h4>
          <div class="potential-value">+${formatCurrency(analysis.vendingPotential.monthlyRevenue)}/Monat</div>
          <div class="potential-detail">${analysis.vendingPotential.locations} Automaten</div>
        </div>
      `;
    }
    
    html += '</div>';
    container.innerHTML = html;
  }

  /**
   * Render sales strategy
   */
  private renderStrategy(strategy: SalesStrategy): void {
    const container = this.dom.$('#profileStrategy');
    if (!container) return;
    
    container.innerHTML = `
      <div class="strategy-section">
        <h4>Verkaufsansatz</h4>
        <p>${strategy.approach}</p>
      </div>
      <div class="strategy-section">
        <h4>Kundenschmerzpunkte</h4>
        <ul>
          ${strategy.painPoints.map(point => `<li>${point}</li>`).join('')}
        </ul>
      </div>
      <div class="strategy-section">
        <h4>Unsere Lösungen</h4>
        <ul>
          ${strategy.keyPoints.map(point => `<li>${point}</li>`).join('')}
        </ul>
      </div>
    `;
  }

  /**
   * Render key arguments
   */
  private renderKeyArguments(): void {
    const container = this.dom.$('#profileArguments');
    if (!container) return;
    
    const salesArguments = [
      {
        title: 'Kostenersparnis',
        description: 'Bis zu 15% Rabatt auf das gesamte Sortiment',
        icon: '💰'
      },
      {
        title: 'Wachstum',
        description: 'Gemeinsames Wachstum durch strategische Partnerschaft',
        icon: '📈'
      },
      {
        title: 'Effizienz',
        description: 'Zeitersparnis durch optimierte Bestellprozesse',
        icon: '🔄'
      },
      {
        title: 'Service',
        description: 'Persönlicher Ansprechpartner für alle Belange',
        icon: '👤'
      }
    ];
    
    container.innerHTML = `
      <div class="arguments-grid">
        ${salesArguments.map(arg => `
          <div class="argument-card">
            <div class="argument-icon">${arg.icon}</div>
            <h4>${arg.title}</h4>
            <p>${arg.description}</p>
          </div>
        `).join('')}
      </div>
    `;
  }

  /**
   * Render product recommendations
   */
  private renderRecommendations(recommendations: Recommendation[]): void {
    const container = this.dom.$('#profileProducts');
    if (!container) return;
    
    container.innerHTML = `
      <div class="recommendations-grid">
        ${recommendations.map(rec => `
          <div class="recommendation-card">
            <h4>${rec.title}</h4>
            <p>${rec.description}</p>
          </div>
        `).join('')}
      </div>
    `;
  }

  /**
   * Render next steps
   */
  private renderNextSteps(): void {
    const container = this.dom.$('#profileNextSteps');
    if (!container) return;
    
    const steps: NextStep[] = [
      {
        action: 'schedule-demo',
        title: 'Demo vereinbaren',
        description: 'Zeigen Sie dem Kunden unser System',
        icon: '📅'
      },
      {
        action: 'create-offer',
        title: 'Angebot erstellen',
        description: 'Erstellen Sie ein individuelles Angebot',
        icon: '📄'
      },
      {
        action: 'send-info',
        title: 'Info senden',
        description: 'Senden Sie weitere Informationen',
        icon: '📧'
      }
    ];
    
    container.innerHTML = `
      <div class="next-steps-grid">
        ${steps.map(step => `
          <div class="next-step-card">
            <div class="step-icon">${step.icon}</div>
            <h4>${step.title}</h4>
            <p>${step.description}</p>
            <button class="btn btn-primary next-step-btn" data-action="${step.action}">
              ${step.title}
            </button>
          </div>
        `).join('')}
      </div>
    `;
  }

  /**
   * Handle next step action
   */
  private handleNextStep(action: string): void {
    switch (action) {
      case 'create-offer':
        // Switch to offer tab
        this.events.emit('app:navigate', 'offer');
        break;
        
      case 'schedule-demo':
        // Open calendar or contact form
        this.showSuccess('Demo-Termin Funktion kommt bald');
        break;
        
      case 'send-info':
        // Prepare info email
        this.prepareInfoEmail();
        break;
    }
  }

  /**
   * Prepare info email
   */
  private prepareInfoEmail(): void {
    if (!this.profileData) return;
    
    const { customerData } = this.profileData;
    const subject = `FreshPlan Informationen für ${customerData.companyName}`;
    const body = `Sehr geehrte/r ${customerData.contactName},\n\nvielen Dank für Ihr Interesse an FreshPlan...`;
    
    window.location.href = `mailto:${customerData.contactEmail}?subject=${encodeURIComponent(subject)}&body=${encodeURIComponent(body)}`;
  }

  /**
   * Translate key with fallback
   */
  private translate(key: string): string {
    // This would normally use the i18n module
    const translations: Record<string, string> = {
      'customer.hotel': 'Hotel',
      'customer.altenheim': 'Altenheim',
      'customer.krankenhaus': 'Krankenhaus',
      'customer.betriebsrestaurant': 'Betriebsrestaurant',
      'customer.restaurant': 'Restaurant',
      'customer.kette': 'Systemgastronomie'
    };
    
    return translations[key] || key;
  }

  /**
   * Public API
   */
  
  getProfileData(): ProfileData | null {
    return this.profileData;
  }

  regenerateProfile(): void {
    this.generateProfile();
  }

  hasProfile(): boolean {
    return this.profileData !== null;
  }

  /**
   * Cleanup module resources
   */
  cleanup(): void {
    // Module cleanup is handled by base class
  }
}