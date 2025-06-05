/**
 * Customer Service - Pure business logic for customer management
 * No DOM dependencies, easily testable
 */

import type { 
  CustomerData, 
  CustomerType, 
  Industry,
  ValidationResult,
  ValidationError,
  ValidationRule
} from '../types';

export interface CustomerValidationRules {
  companyName: ValidationRule;
  contactEmail: ValidationRule;
  contactPhone: ValidationRule;
  zipCode: ValidationRule;
  [key: string]: ValidationRule;
}

export interface IndustryRequirements {
  requiredFields: string[];
  optionalFields: string[];
  validationRules?: Partial<CustomerValidationRules>;
}

export class CustomerService {
  private validationRules: CustomerValidationRules;
  private industryRequirements: Map<Industry, IndustryRequirements>;

  constructor() {
    // Default validation rules
    this.validationRules = {
      companyName: {
        required: true,
        min: 2,
        max: 100,
        pattern: /^[a-zA-ZäöüÄÖÜß\s&.-]+$/
      },
      contactEmail: {
        required: true,
        pattern: /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      },
      contactPhone: {
        required: true,
        pattern: /^[\d\s\-\+\(\)]+$/,
        min: 10
      },
      zipCode: {
        required: true,
        pattern: /^\d{5}$/
      },
      street: {
        required: true,
        min: 3,
        max: 100
      },
      city: {
        required: true,
        min: 2,
        max: 50
      },
      contactName: {
        required: true,
        min: 2,
        max: 100
      }
    };

    // Industry-specific requirements
    this.industryRequirements = new Map([
      ['hotel', {
        requiredFields: ['rooms', 'occupancyRate', 'breakfastPrice'],
        optionalFields: ['avgOrderPerLocation'],
        validationRules: {
          rooms: {
            required: true,
            min: 1,
            max: 2000,
            custom: (value) => Number.isInteger(value) || 'Rooms must be a whole number'
          },
          occupancyRate: {
            required: true,
            min: 0,
            max: 100,
            custom: (value) => value % 1 === 0 || value % 0.5 === 0 || 'Occupancy rate must be in 0.5% increments'
          },
          breakfastPrice: {
            required: true,
            min: 5,
            max: 50
          }
        }
      }],
      ['krankenhaus', {
        requiredFields: ['beds', 'staffMeals'],
        optionalFields: ['avgOrderPerLocation'],
        validationRules: {
          beds: {
            required: true,
            min: 10,
            max: 5000,
            custom: (value) => Number.isInteger(value) || 'Beds must be a whole number'
          },
          staffMeals: {
            required: true,
            min: 0,
            max: 10000,
            custom: (value) => Number.isInteger(value) || 'Staff meals must be a whole number'
          }
        }
      }],
      ['altenheim', {
        requiredFields: ['residents', 'mealsPerDay'],
        optionalFields: ['avgOrderPerLocation'],
        validationRules: {
          residents: {
            required: true,
            min: 5,
            max: 500,
            custom: (value) => Number.isInteger(value) || 'Residents must be a whole number'
          },
          mealsPerDay: {
            required: true,
            min: 1,
            max: 5,
            custom: (value) => Number.isInteger(value) || 'Meals per day must be a whole number'
          }
        }
      }],
      ['betriebsrestaurant', {
        requiredFields: ['employees', 'utilizationRate'],
        optionalFields: ['avgOrderPerLocation'],
        validationRules: {
          employees: {
            required: true,
            min: 10,
            max: 10000,
            custom: (value) => Number.isInteger(value) || 'Employees must be a whole number'
          },
          utilizationRate: {
            required: true,
            min: 5,
            max: 100,
            custom: (value) => value > 0 || 'Utilization rate must be positive'
          }
        }
      }],
      ['restaurant', {
        requiredFields: ['seats', 'turnsPerDay', 'operatingDays'],
        optionalFields: ['avgOrderPerLocation'],
        validationRules: {
          seats: {
            required: true,
            min: 10,
            max: 1000,
            custom: (value) => Number.isInteger(value) || 'Seats must be a whole number'
          },
          turnsPerDay: {
            required: true,
            min: 0.5,
            max: 5,
            custom: (value) => value > 0 || 'Turns per day must be positive'
          },
          operatingDays: {
            required: true,
            min: 200,
            max: 365,
            custom: (value) => Number.isInteger(value) || 'Operating days must be a whole number'
          }
        }
      }],
      ['kette', {
        requiredFields: ['totalLocations', 'avgOrderPerLocation'],
        optionalFields: ['vendingInterest', 'vendingLocations', 'vendingDaily'],
        validationRules: {
          totalLocations: {
            required: true,
            min: 2,
            max: 1000,
            custom: (value) => Number.isInteger(value) || 'Total locations must be a whole number'
          },
          avgOrderPerLocation: {
            required: true,
            min: 1000,
            max: 100000
          }
        }
      }]
    ]);
  }

  /**
   * Validate customer data
   */
  validateCustomer(data: Partial<CustomerData>): ValidationResult {
    const errors: ValidationError[] = [];

    // Basic field validation
    const basicFields = ['companyName', 'contactName', 'contactEmail', 'contactPhone', 'street', 'zipCode', 'city'];
    
    for (const field of basicFields) {
      const rule = this.validationRules[field];
      if (rule) {
        const validationError = this.validateField(field, data[field as keyof CustomerData], rule);
        if (validationError) {
          errors.push(validationError);
        }
      }
    }

    // Industry-specific validation
    if (data.industry && (data.industry as string) !== '') {
      const industryReqs = this.industryRequirements.get(data.industry as Industry);
      if (industryReqs) {
        // Validate required industry fields
        for (const field of industryReqs.requiredFields) {
          const value = data[field as keyof CustomerData];
          const rule = industryReqs.validationRules?.[field] || {};
          
          if (rule.required !== false && (value === undefined || value === null || value === '')) {
            errors.push({
              field,
              message: `${this.getFieldLabel(field)} is required for ${data.industry}`,
              rule: 'required'
            });
          } else if (value !== undefined && value !== null) {
            const validationError = this.validateField(field, value, rule);
            if (validationError) {
              errors.push(validationError);
            }
          }
        }
      }
    }

    // Chain-specific validation
    if (data.customerType === 'chain') {
      const chainReqs = this.industryRequirements.get('kette');
      if (chainReqs) {
        for (const field of chainReqs.requiredFields) {
          const value = data[field as keyof CustomerData];
          const rule = chainReqs.validationRules?.[field] || {};
          
          if (rule.required !== false && (value === undefined || value === null || value === '')) {
            errors.push({
              field,
              message: `${this.getFieldLabel(field)} is required for chain customers`,
              rule: 'required'
            });
          } else if (value !== undefined && value !== null) {
            const validationError = this.validateField(field, value, rule);
            if (validationError) {
              errors.push(validationError);
            }
          }
        }
      }
    }

    return {
      isValid: errors.length === 0,
      errors
    };
  }

  /**
   * Validate a single field
   */
  private validateField(field: string, value: any, rule: ValidationRule): ValidationError | null {
    const label = this.getFieldLabel(field);

    // Required check
    if (rule.required && (value === undefined || value === null || value === '')) {
      return {
        field,
        message: `${label} is required`,
        rule: 'required'
      };
    }

    // Skip other validations if value is empty and not required
    if (!rule.required && (value === undefined || value === null || value === '')) {
      return null;
    }

    // Pattern check
    if (rule.pattern && typeof value === 'string' && !rule.pattern.test(value)) {
      return {
        field,
        message: `${label} has invalid format`,
        rule: 'pattern'
      };
    }

    // Min length/value check
    if (rule.min !== undefined) {
      if (typeof value === 'string' && value.length < rule.min) {
        return {
          field,
          message: `${label} must be at least ${rule.min} characters`,
          rule: 'min'
        };
      } else if (typeof value === 'number' && value < rule.min) {
        return {
          field,
          message: `${label} must be at least ${rule.min}`,
          rule: 'min'
        };
      }
    }

    // Max length/value check
    if (rule.max !== undefined) {
      if (typeof value === 'string' && value.length > rule.max) {
        return {
          field,
          message: `${label} must be at most ${rule.max} characters`,
          rule: 'max'
        };
      } else if (typeof value === 'number' && value > rule.max) {
        return {
          field,
          message: `${label} must be at most ${rule.max}`,
          rule: 'max'
        };
      }
    }

    // Custom validation
    if (rule.custom) {
      const result = rule.custom(value);
      if (result !== true) {
        return {
          field,
          message: typeof result === 'string' ? result : `${label} is invalid`,
          rule: 'custom'
        };
      }
    }

    return null;
  }

  /**
   * Get human-readable field label
   */
  private getFieldLabel(field: string): string {
    const labels: Record<string, string> = {
      companyName: 'Company name',
      contactName: 'Contact name',
      contactEmail: 'Email',
      contactPhone: 'Phone',
      street: 'Street',
      zipCode: 'ZIP code',
      city: 'City',
      rooms: 'Number of rooms',
      occupancyRate: 'Occupancy rate',
      breakfastPrice: 'Breakfast price',
      beds: 'Number of beds',
      staffMeals: 'Staff meals',
      residents: 'Number of residents',
      mealsPerDay: 'Meals per day',
      employees: 'Number of employees',
      utilizationRate: 'Utilization rate',
      seats: 'Number of seats',
      turnsPerDay: 'Turns per day',
      operatingDays: 'Operating days per year',
      totalLocations: 'Total locations',
      avgOrderPerLocation: 'Average order per location'
    };

    return labels[field] || field;
  }

  /**
   * Get required fields for industry
   */
  getRequiredFields(industry: Industry, customerType: CustomerType): string[] {
    const basicFields = ['companyName', 'contactName', 'contactEmail', 'contactPhone', 'street', 'zipCode', 'city'];
    const industryFields = industry ? this.industryRequirements.get(industry)?.requiredFields || [] : [];
    const chainFields = customerType === 'chain' ? this.industryRequirements.get('kette')?.requiredFields || [] : [];

    return [...new Set([...basicFields, ...industryFields, ...chainFields])];
  }

  /**
   * Check if customer data is complete
   */
  isCustomerComplete(data: Partial<CustomerData>): boolean {
    if (!data.industry || !data.customerType) {
      return false;
    }

    // Map customerType string to CustomerType enum if needed
    const customerType = (data.customerType === 'single' || data.customerType === 'chain') 
      ? data.customerType as CustomerType 
      : 'single' as CustomerType;
    const requiredFields = this.getRequiredFields(data.industry, customerType);
    
    return requiredFields.every(field => {
      const value = data[field as keyof CustomerData];
      return value !== undefined && value !== null && value !== '';
    });
  }

  /**
   * Calculate estimated monthly volume based on industry
   */
  calculateMonthlyVolume(data: CustomerData): number {
    switch (data.industry) {
      case 'hotel':
        if (data.rooms && data.occupancyRate && data.breakfastPrice) {
          const dailyGuests = data.rooms * (data.occupancyRate / 100);
          const breakfastRevenue = dailyGuests * data.breakfastPrice * 30;
          const otherMeals = dailyGuests * 0.3 * 25 * 30; // 30% have other meals
          return Math.round(breakfastRevenue + otherMeals);
        }
        break;

      case 'krankenhaus':
        if (data.beds && data.staffMeals) {
          const patientMeals = data.beds * 0.85 * 3 * 12 * 30; // 85% occupancy, 3 meals, €12/meal
          const staffMealRevenue = data.staffMeals * 8 * 22; // €8/meal, 22 working days
          return Math.round(patientMeals + staffMealRevenue);
        }
        break;

      case 'altenheim':
        if (data.residents && data.mealsPerDay) {
          return Math.round(data.residents * data.mealsPerDay * 8 * 30); // €8/meal
        }
        break;

      case 'betriebsrestaurant':
        if (data.employees && data.utilizationRate) {
          const dailyMeals = data.employees * (data.utilizationRate / 100);
          return Math.round(dailyMeals * 7 * 22); // €7/meal, 22 working days
        }
        break;

      case 'restaurant':
        if (data.seats && data.turnsPerDay && data.operatingDays) {
          const monthlyDays = data.operatingDays / 12;
          const dailyRevenue = data.seats * data.turnsPerDay * 0.7 * 20; // 70% capacity, €20 average
          return Math.round(dailyRevenue * monthlyDays);
        }
        break;

      case 'kette':
        if (data.totalLocations && data.avgOrderPerLocation) {
          return Math.round(data.totalLocations * data.avgOrderPerLocation);
        }
        break;
    }

    return 0;
  }

  /**
   * Format customer data for display
   */
  formatCustomerData(data: CustomerData): Record<string, string> {
    const formatted: Record<string, string> = {
      'Company': data.companyName,
      'Type': data.customerType === 'chain' ? 'Chain' : 'Single Location',
      'Industry': this.getIndustryLabel(data.industry),
      'Contact': data.contactName,
      'Email': data.contactEmail,
      'Phone': data.contactPhone,
      'Address': `${data.street}, ${data.zipCode} ${data.city}`
    };

    // Add industry-specific data
    switch (data.industry) {
      case 'hotel':
        if (data.rooms) formatted['Rooms'] = data.rooms.toString();
        if (data.occupancyRate) formatted['Occupancy'] = `${data.occupancyRate}%`;
        if (data.breakfastPrice) formatted['Breakfast Price'] = `€${data.breakfastPrice}`;
        break;

      case 'krankenhaus':
        if (data.beds) formatted['Beds'] = data.beds.toString();
        if (data.staffMeals) formatted['Staff Meals/Day'] = data.staffMeals.toString();
        break;

      case 'altenheim':
        if (data.residents) formatted['Residents'] = data.residents.toString();
        if (data.mealsPerDay) formatted['Meals/Day'] = data.mealsPerDay.toString();
        break;

      case 'betriebsrestaurant':
        if (data.employees) formatted['Employees'] = data.employees.toString();
        if (data.utilizationRate) formatted['Utilization'] = `${data.utilizationRate}%`;
        break;

      case 'restaurant':
        if (data.seats) formatted['Seats'] = data.seats.toString();
        if (data.turnsPerDay) formatted['Turns/Day'] = data.turnsPerDay.toString();
        if (data.operatingDays) formatted['Operating Days'] = data.operatingDays.toString();
        break;
    }

    // Add chain-specific data
    if (data.customerType === 'chain') {
      if (data.totalLocations) formatted['Locations'] = data.totalLocations.toString();
      if (data.avgOrderPerLocation) formatted['Avg Order/Location'] = `€${data.avgOrderPerLocation}`;
    }

    return formatted;
  }

  /**
   * Get industry label
   */
  private getIndustryLabel(industry: Industry): string {
    const labels: Record<Industry, string> = {
      restaurant: 'Restaurant',
      hotel: 'Hotel',
      krankenhaus: 'Hospital',
      altenheim: 'Nursing Home',
      betriebsrestaurant: 'Company Restaurant',
      kette: 'Chain',
      '': 'Not specified'
    };

    return labels[industry] || industry;
  }

  /**
   * Export customer data as JSON
   */
  exportCustomer(data: CustomerData): string {
    return JSON.stringify({
      customer: data,
      monthlyVolume: this.calculateMonthlyVolume(data),
      timestamp: new Date().toISOString()
    }, null, 2);
  }

  /**
   * Import customer data from JSON
   */
  importCustomer(json: string): CustomerData | null {
    try {
      const parsed = JSON.parse(json);
      if (parsed.customer) {
        return parsed.customer as CustomerData;
      }
      return null;
    } catch (error) {
      console.error('Failed to import customer:', error);
      return null;
    }
  }
}