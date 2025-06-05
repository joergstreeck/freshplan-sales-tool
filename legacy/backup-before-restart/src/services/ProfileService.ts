/**
 * Profile Service - Pure business logic for customer profile generation
 * No DOM dependencies, easily testable
 */

import type {
  CustomerData,
  ProfileData,
  ProfileAnalysis,
  Recommendation,
  SalesStrategy,
  VendingPotential,
  SavingsCalculation,
  Industry
} from '../types';

export interface ProfileGenerationOptions {
  includeVending?: boolean;
  discountPercentage?: number;
  growthRate?: number;
}

export interface IndustryProfile {
  painPoints: string[];
  opportunities: string[];
  avgOrderValue: number;
  seasonality: string;
  keyMetrics: string[];
}

export class ProfileService {
  private industryProfiles: Map<Industry, IndustryProfile>;
  private defaultDiscountPercentage: number;

  constructor(defaultDiscount: number = 10) {
    this.defaultDiscountPercentage = defaultDiscount;
    
    // Industry-specific profiles
    this.industryProfiles = new Map([
      ['hotel', {
        painPoints: [
          'Fluctuating occupancy rates affect ordering',
          'Storage limitations for perishables',
          'Complex breakfast buffet requirements',
          'Seasonal demand variations'
        ],
        opportunities: [
          'Predictable breakfast patterns',
          'Potential for banquet/event supplies',
          'Mini-bar restocking opportunities',
          'Staff meal programs'
        ],
        avgOrderValue: 15000,
        seasonality: 'High season summer/winter, low season spring/fall',
        keyMetrics: ['occupancy rate', 'breakfast participation', 'average stay duration']
      }],
      ['krankenhaus', {
        painPoints: [
          'Strict dietary requirements',
          'Complex meal planning for patients',
          '24/7 operation demands',
          'Budget constraints'
        ],
        opportunities: [
          'Stable, predictable demand',
          'Large volume requirements',
          'Staff cafeteria supplies',
          'Special dietary product needs'
        ],
        avgOrderValue: 25000,
        seasonality: 'Stable throughout the year',
        keyMetrics: ['bed occupancy', 'average patient stay', 'staff count']
      }],
      ['altenheim', {
        painPoints: [
          'Special dietary needs for elderly',
          'Limited kitchen staff',
          'Budget pressures',
          'Texture-modified food requirements'
        ],
        opportunities: [
          'Consistent meal patterns',
          'Long-term stable contracts',
          'Specialized product offerings',
          'Holiday/event catering'
        ],
        avgOrderValue: 12000,
        seasonality: 'Very stable, slight increase during holidays',
        keyMetrics: ['resident count', 'care levels', 'meal participation rate']
      }],
      ['betriebsrestaurant', {
        painPoints: [
          'Price-sensitive employees',
          'Limited menu variety expectations',
          'Peak hour capacity issues',
          'Subsidization requirements'
        ],
        opportunities: [
          'Predictable weekday patterns',
          'Bulk ordering advantages',
          'Healthy/sustainable food trends',
          'Catering for meetings/events'
        ],
        avgOrderValue: 8000,
        seasonality: 'Lower in vacation periods (July/August, December)',
        keyMetrics: ['employee count', 'utilization rate', 'average ticket size']
      }],
      ['restaurant', {
        painPoints: [
          'High competition pressure',
          'Volatile customer demand',
          'Fresh ingredient requirements',
          'Storage limitations'
        ],
        opportunities: [
          'Menu optimization support',
          'Seasonal specialties',
          'Premium product offerings',
          'Delivery/takeout supplies'
        ],
        avgOrderValue: 10000,
        seasonality: 'Higher in summer/December, lower in January/February',
        keyMetrics: ['seat capacity', 'table turns', 'average check size']
      }],
      ['kette', {
        painPoints: [
          'Standardization requirements',
          'Multiple delivery locations',
          'Central vs. local purchasing decisions',
          'Quality consistency demands'
        ],
        opportunities: [
          'Volume bundling benefits',
          'Centralized ordering systems',
          'Private label opportunities',
          'National contract potential'
        ],
        avgOrderValue: 50000,
        seasonality: 'Varies by chain type',
        keyMetrics: ['location count', 'standardization level', 'growth rate']
      }]
    ]);
  }

  /**
   * Generate a complete customer profile
   */
  generateProfile(
    customerData: CustomerData, 
    monthlyVolume: number,
    options: ProfileGenerationOptions = {}
  ): ProfileData {
    const analysis = this.analyzeCustomer(customerData, monthlyVolume, options);
    const recommendations = this.generateRecommendations(customerData, analysis);
    const strategy = this.generateSalesStrategy(customerData, analysis);

    const industryProfile = this.industryProfiles.get(customerData.industry || '');
    const industryConfig = industryProfile ? {
      name: customerData.industry || '',
      avgPrices: {
        standard: industryProfile.avgOrderValue
      }
    } : undefined;

    return {
      customerData,
      industry: industryConfig,
      analysis,
      recommendations,
      strategy,
      generatedAt: new Date().toISOString()
    };
  }

  /**
   * Analyze customer potential
   */
  private analyzeCustomer(
    customerData: CustomerData,
    monthlyVolume: number,
    options: ProfileGenerationOptions
  ): ProfileAnalysis {
    const estimatedVolume = monthlyVolume || this.estimateVolume(customerData);
    const discountPercentage = options.discountPercentage || this.defaultDiscountPercentage;
    const growthRate = options.growthRate || this.estimateGrowthPotential(customerData);

    const potentialSavings = this.calculateSavings(estimatedVolume, discountPercentage);
    const vendingPotential = options.includeVending ? this.calculateVendingPotential(customerData) : null;

    return {
      estimatedVolume: estimatedVolume * 12, // Annual volume
      monthlyVolume: estimatedVolume,
      potentialSavings,
      growthPotential: growthRate,
      vendingPotential
    };
  }

  /**
   * Estimate monthly volume if not provided
   */
  private estimateVolume(customerData: CustomerData): number {
    const industryProfile = this.industryProfiles.get(customerData.industry || '');
    const baseVolume = industryProfile?.avgOrderValue || 10000;

    // Adjust based on customer specifics
    let multiplier = 1;

    switch (customerData.industry) {
      case 'hotel':
        if (customerData.rooms) {
          multiplier = customerData.rooms / 100; // Base assumption: 100 rooms
        }
        break;

      case 'krankenhaus':
        if (customerData.beds) {
          multiplier = customerData.beds / 200; // Base assumption: 200 beds
        }
        break;

      case 'altenheim':
        if (customerData.residents) {
          multiplier = customerData.residents / 80; // Base assumption: 80 residents
        }
        break;

      case 'betriebsrestaurant':
        if (customerData.employees) {
          multiplier = customerData.employees / 500; // Base assumption: 500 employees
        }
        break;

      case 'restaurant':
        if (customerData.seats) {
          multiplier = customerData.seats / 60; // Base assumption: 60 seats
        }
        break;

      case 'kette':
        if (customerData.totalLocations && customerData.avgOrderPerLocation) {
          return customerData.totalLocations * customerData.avgOrderPerLocation;
        }
        break;
    }

    return Math.round(baseVolume * multiplier);
  }

  /**
   * Calculate potential savings
   */
  private calculateSavings(monthlyVolume: number, discountPercentage: number): SavingsCalculation {
    const monthly = Math.round(monthlyVolume * (discountPercentage / 100));
    const annual = monthly * 12;

    return {
      annual,
      monthly,
      percentage: discountPercentage
    };
  }

  /**
   * Estimate growth potential
   */
  private estimateGrowthPotential(customerData: CustomerData): number {
    let growthRate = 5; // Base growth rate

    // Industry-specific adjustments
    switch (customerData.industry) {
      case 'hotel':
        if (customerData.occupancyRate && customerData.occupancyRate < 70) {
          growthRate += 5; // Growth potential if occupancy is low
        }
        break;

      case 'kette':
        if (customerData.totalLocations && customerData.totalLocations > 10) {
          growthRate += 10; // Chains have higher growth potential
        }
        break;

      case 'restaurant':
        if (customerData.turnsPerDay && customerData.turnsPerDay < 2) {
          growthRate += 3; // Growth potential if turns are low
        }
        break;
    }

    // Customer type adjustment
    if (customerData.customerType === 'chain') {
      growthRate += 5;
    }

    return Math.min(growthRate, 25); // Cap at 25%
  }

  /**
   * Calculate vending potential
   */
  private calculateVendingPotential(customerData: CustomerData): VendingPotential | null {
    if (!customerData.vendingInterest) {
      return null;
    }

    const locations = customerData.vendingLocations || 1;
    const dailyTransactions = customerData.vendingDaily || this.estimateVendingTransactions(customerData);
    const avgTransaction = 2.5; // €2.50 average
    const profitMargin = 0.35; // 35% margin

    const monthlyRevenue = Math.round(locations * dailyTransactions * avgTransaction * 30);
    const monthlyProfit = Math.round(monthlyRevenue * profitMargin);

    return {
      locations,
      dailyTransactions,
      monthlyRevenue,
      monthlyProfit
    };
  }

  /**
   * Estimate vending transactions based on customer type
   */
  private estimateVendingTransactions(customerData: CustomerData): number {
    switch (customerData.industry) {
      case 'hotel':
        return 50; // Hotels have moderate vending usage
      case 'krankenhaus':
        return 100; // Hospitals have high vending usage (visitors + staff)
      case 'betriebsrestaurant':
        return customerData.employees ? Math.round(customerData.employees * 0.2) : 50;
      case 'altenheim':
        return 30; // Lower vending usage
      default:
        return 40;
    }
  }

  /**
   * Generate recommendations
   */
  private generateRecommendations(
    customerData: CustomerData,
    analysis: ProfileAnalysis
  ): Recommendation[] {
    const recommendations: Recommendation[] = [];

    // Volume-based recommendations
    if (analysis.monthlyVolume < 5000) {
      recommendations.push({
        type: 'growth',
        title: 'Bundle Orders for Better Discounts',
        description: 'Consider consolidating orders to reach higher discount tiers. Current volume qualifies for minimal discounts.'
      });
    }

    if (analysis.monthlyVolume > 30000) {
      recommendations.push({
        type: 'efficiency',
        title: 'Optimize Delivery Schedule',
        description: 'With your high volume, consider scheduled deliveries to reduce costs and improve planning.'
      });
    }

    // Industry-specific recommendations
    const industryProfile = this.industryProfiles.get(customerData.industry || '');
    if (industryProfile) {
      if (customerData.industry === 'hotel' && customerData.breakfastPrice && customerData.breakfastPrice < 15) {
        recommendations.push({
          type: 'revenue',
          title: 'Premium Breakfast Options',
          description: 'Enhance breakfast offerings with premium products to justify higher pricing and improve margins.'
        });
      }

      if (customerData.industry === 'krankenhaus') {
        recommendations.push({
          type: 'product',
          title: 'Special Dietary Solutions',
          description: 'Explore our specialized dietary product lines for patients with specific nutritional requirements.'
        });
      }

      if (customerData.industry === 'betriebsrestaurant' && customerData.utilizationRate && customerData.utilizationRate < 50) {
        recommendations.push({
          type: 'marketing',
          title: 'Increase Cafeteria Utilization',
          description: 'Implement promotional programs and menu variety to boost employee participation rates.'
        });
      }
    }

    // Chain-specific recommendations
    if (customerData.customerType === 'chain') {
      recommendations.push({
        type: 'systems',
        title: 'Centralized Ordering Platform',
        description: 'Implement our digital ordering system for better control and visibility across all locations.'
      });

      if (customerData.totalLocations && customerData.totalLocations > 5) {
        recommendations.push({
          type: 'contract',
          title: 'National Framework Agreement',
          description: 'Qualify for national contract terms with standardized pricing across all locations.'
        });
      }
    }

    // Vending recommendations
    if (analysis.vendingPotential) {
      recommendations.push({
        type: 'vending',
        title: 'Vending Machine Opportunity',
        description: `Potential additional revenue of €${analysis.vendingPotential.monthlyRevenue}/month through vending solutions.`
      });
    }

    // Growth recommendations
    if (analysis.growthPotential > 10) {
      recommendations.push({
        type: 'growth',
        title: 'Growth Partnership Program',
        description: 'Your high growth potential qualifies you for our partnership program with additional benefits.'
      });
    }

    return recommendations;
  }

  /**
   * Generate sales strategy
   */
  private generateSalesStrategy(
    customerData: CustomerData,
    analysis: ProfileAnalysis
  ): SalesStrategy {
    const industryProfile = this.industryProfiles.get(customerData.industry || '');
    const isHighValue = analysis.monthlyVolume > 25000;
    const isChain = customerData.customerType === 'chain';

    // Determine approach
    let approach = 'Standard Sales Approach';
    if (isChain) {
      approach = 'Strategic Account Management';
    } else if (isHighValue) {
      approach = 'Key Account Approach';
    } else if (analysis.growthPotential > 15) {
      approach = 'Development Account Approach';
    }

    // Key selling points
    const keyPoints: string[] = [
      `Potential savings of €${analysis.potentialSavings.annual} per year`,
      'Reliable delivery network with 99.5% on-time performance',
      'Digital ordering platform for efficiency'
    ];

    if (isChain) {
      keyPoints.push('Centralized billing and reporting');
      keyPoints.push('Dedicated account management team');
    }

    if (analysis.monthlyVolume > 15000) {
      keyPoints.push('Volume-based pricing advantages');
      keyPoints.push('Priority customer support');
    }

    if (industryProfile) {
      keyPoints.push(`Specialized solutions for ${customerData.industry} sector`);
    }

    // Pain points to address
    const painPoints = industryProfile?.painPoints || [
      'Rising food costs',
      'Supply chain reliability',
      'Administrative complexity',
      'Quality consistency'
    ];

    return {
      approach,
      keyPoints,
      painPoints
    };
  }

  /**
   * Get profile summary
   */
  getProfileSummary(profile: ProfileData): string {
    const { analysis, customerData } = profile;
    const monthlyVolume = analysis.monthlyVolume.toLocaleString('de-DE');
    const annualSavings = analysis.potentialSavings.annual.toLocaleString('de-DE');
    
    let summary = `${customerData.companyName} is a ${customerData.customerType} ${customerData.industry} customer `;
    summary += `with an estimated monthly volume of €${monthlyVolume}. `;
    summary += `They could save €${annualSavings} annually with our solution. `;
    
    if (analysis.growthPotential > 10) {
      summary += `High growth potential of ${analysis.growthPotential}% identified. `;
    }
    
    if (analysis.vendingPotential) {
      const vendingRevenue = analysis.vendingPotential.monthlyRevenue.toLocaleString('de-DE');
      summary += `Additional vending opportunity worth €${vendingRevenue}/month. `;
    }

    return summary;
  }

  /**
   * Export profile as JSON
   */
  exportProfile(profile: ProfileData): string {
    return JSON.stringify(profile, null, 2);
  }

  /**
   * Generate profile report
   */
  generateReport(profile: ProfileData): string {
    const { customerData, analysis, recommendations, strategy } = profile;
    
    let report = `# Customer Profile Report\n\n`;
    report += `## Customer Information\n`;
    report += `- Company: ${customerData.companyName}\n`;
    report += `- Type: ${customerData.customerType}\n`;
    report += `- Industry: ${customerData.industry}\n`;
    report += `- Contact: ${customerData.contactName}\n\n`;
    
    report += `## Financial Analysis\n`;
    report += `- Monthly Volume: €${analysis.monthlyVolume.toLocaleString('de-DE')}\n`;
    report += `- Annual Volume: €${analysis.estimatedVolume.toLocaleString('de-DE')}\n`;
    report += `- Potential Annual Savings: €${analysis.potentialSavings.annual.toLocaleString('de-DE')}\n`;
    report += `- Growth Potential: ${analysis.growthPotential}%\n\n`;
    
    if (analysis.vendingPotential) {
      report += `## Vending Opportunity\n`;
      report += `- Locations: ${analysis.vendingPotential.locations}\n`;
      report += `- Monthly Revenue: €${analysis.vendingPotential.monthlyRevenue.toLocaleString('de-DE')}\n`;
      report += `- Monthly Profit: €${analysis.vendingPotential.monthlyProfit.toLocaleString('de-DE')}\n\n`;
    }
    
    report += `## Sales Strategy\n`;
    report += `### Approach: ${strategy.approach}\n\n`;
    report += `### Key Selling Points\n`;
    strategy.keyPoints.forEach(point => {
      report += `- ${point}\n`;
    });
    report += `\n`;
    
    report += `### Pain Points to Address\n`;
    strategy.painPoints.forEach(pain => {
      report += `- ${pain}\n`;
    });
    report += `\n`;
    
    report += `## Recommendations\n`;
    recommendations.forEach(rec => {
      report += `### ${rec.title}\n`;
      report += `${rec.description}\n\n`;
    });
    
    report += `\n---\nGenerated: ${new Date().toLocaleString('de-DE')}`;
    
    return report;
  }
}