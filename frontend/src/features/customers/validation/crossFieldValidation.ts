/**
 * Cross-Field Validation
 * 
 * Validierungen, die mehrere Felder gleichzeitig betrachten.
 * Implementiert Geschäftsregeln und Abhängigkeiten zwischen Feldern.
 * 
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/04-validation.md
 */

// CustomerData und LocationData sind jetzt dynamische Field-basierte Daten
// Wir verwenden Record<string, any> für die Field Values

export interface ValidationResult {
  isValid: boolean;
  errors?: Record<string, string>;
  warnings?: Record<string, string>;
}

/**
 * Customer Cross-Field Validators
 */
export const customerCrossFieldValidators = {
  /**
   * Validate complete address
   * If one address field is filled, all must be filled
   */
  validateAddress(data: Record<string, any>): ValidationResult {
    const { street, postalCode, city } = data;
    const hasAnyAddress = !!(street || postalCode || city);
    const hasCompleteAddress = !!(street && postalCode && city);
    
    if (hasAnyAddress && !hasCompleteAddress) {
      const errors: Record<string, string> = {};
      if (!street) errors.street = 'Straße fehlt für vollständige Adresse';
      if (!postalCode) errors.postalCode = 'PLZ fehlt für vollständige Adresse';
      if (!city) errors.city = 'Ort fehlt für vollständige Adresse';
      
      return {
        isValid: false,
        errors
      };
    }
    
    return { isValid: true };
  },
  
  /**
   * Validate hotel-specific requirements
   */
  validateHotelRequirements(data: any): ValidationResult {
    if (data.industry !== 'hotel') return { isValid: true };
    
    const errors: Record<string, string> = {};
    const warnings: Record<string, string> = {};
    
    // Star rating is required for hotels
    if (!data.starRating) {
      errors.starRating = 'Sterne-Kategorie ist für Hotels erforderlich';
    }
    
    // Room count is required
    if (!data.roomCount || data.roomCount < 1) {
      errors.roomCount = 'Anzahl Zimmer ist erforderlich';
    }
    
    // Business rule: 5-star hotels should have certain amenities
    if (data.starRating === '5') {
      if (!data.restaurantSeats || data.restaurantSeats < 50) {
        warnings.restaurantSeats = '5-Sterne Hotels haben üblicherweise ein Restaurant mit mindestens 50 Plätzen';
      }
      if (data.roomCount < 50) {
        warnings.roomCount = '5-Sterne Hotels haben üblicherweise mindestens 50 Zimmer';
      }
    }
    
    return {
      isValid: Object.keys(errors).length === 0,
      errors: Object.keys(errors).length > 0 ? errors : undefined,
      warnings: Object.keys(warnings).length > 0 ? warnings : undefined
    };
  },
  
  /**
   * Validate hospital-specific requirements
   */
  validateHospitalRequirements(data: any): ValidationResult {
    if (data.industry !== 'krankenhaus') return { isValid: true };
    
    const errors: Record<string, string> = {};
    
    if (!data.hospitalType) {
      errors.hospitalType = 'Krankenhaustyp ist erforderlich';
    }
    
    if (!data.bedCount || data.bedCount < 1) {
      errors.bedCount = 'Anzahl Betten ist erforderlich';
    }
    
    if (!data.departments || data.departments.length === 0) {
      errors.departments = 'Mindestens eine Abteilung muss angegeben werden';
    }
    
    // University hospitals should have more beds
    if (data.hospitalType === 'university' && data.bedCount < 300) {
      errors.bedCount = 'Universitätskliniken haben üblicherweise mindestens 300 Betten';
    }
    
    return {
      isValid: Object.keys(errors).length === 0,
      errors: Object.keys(errors).length > 0 ? errors : undefined
    };
  },
  
  /**
   * Validate chain customer requirements
   */
  validateChainCustomer(data: Partial<CustomerData>, locationCount: number): ValidationResult {
    if (data.chainCustomer !== 'ja') return { isValid: true };
    
    const warnings: Record<string, string> = {};
    
    // Chain customers should have multiple locations
    if (locationCount < 2) {
      warnings.locations = 'Ketten-Kunden haben üblicherweise mehrere Standorte';
    }
    
    return {
      isValid: true,
      warnings: Object.keys(warnings).length > 0 ? warnings : undefined
    };
  },
  
  /**
   * Validate contact information completeness
   */
  validateContactInfo(data: Partial<CustomerData>): ValidationResult {
    const { email, phone, contactPerson } = data as any;
    const warnings: Record<string, string> = {};
    
    // If contact person is provided, email or phone should be provided
    if (contactPerson && !email && !phone) {
      warnings.contact = 'Kontaktperson ohne Kontaktmöglichkeit (E-Mail oder Telefon)';
    }
    
    // At least one contact method should be provided
    if (!email && !phone) {
      warnings.contact = 'Keine Kontaktmöglichkeit angegeben (E-Mail oder Telefon empfohlen)';
    }
    
    return {
      isValid: true,
      warnings: Object.keys(warnings).length > 0 ? warnings : undefined
    };
  }
};

/**
 * Location Cross-Field Validators
 */
export const locationCrossFieldValidators = {
  /**
   * Validate location address
   */
  validateAddress(data: Partial<LocationData>): ValidationResult {
    // Same logic as customer address
    return customerCrossFieldValidators.validateAddress(data);
  },
  
  /**
   * Validate main location requirements
   */
  validateMainLocation(data: Partial<LocationData>, isFirst: boolean): ValidationResult {
    if (data.locationType !== 'hauptstandort') return { isValid: true };
    
    const warnings: Record<string, string> = {};
    
    // First location should typically be main location
    if (!isFirst) {
      warnings.locationType = 'Der Hauptstandort ist üblicherweise der erste Standort';
    }
    
    // Main location should have complete address
    const addressResult = this.validateAddress(data);
    if (!addressResult.isValid) {
      return {
        isValid: false,
        errors: {
          ...addressResult.errors,
          address: 'Hauptstandort sollte eine vollständige Adresse haben'
        }
      };
    }
    
    return {
      isValid: true,
      warnings: Object.keys(warnings).length > 0 ? warnings : undefined
    };
  },
  
  /**
   * Validate location capacity based on industry
   */
  validateCapacity(data: any, industry: string): ValidationResult {
    const warnings: Record<string, string> = {};
    
    switch (industry) {
      case 'hotel':
        if (data.locationType === 'hauptstandort' && (!data.roomCount || data.roomCount < 10)) {
          warnings.roomCount = 'Hotels haben üblicherweise mindestens 10 Zimmer';
        }
        break;
        
      case 'krankenhaus':
        if (!data.bedCount || data.bedCount < 20) {
          warnings.bedCount = 'Krankenhausstandorte haben üblicherweise mindestens 20 Betten';
        }
        break;
        
      case 'restaurant':
        if (!data.seatCount || data.seatCount < 20) {
          warnings.seatCount = 'Restaurants haben üblicherweise mindestens 20 Sitzplätze';
        }
        break;
    }
    
    return {
      isValid: true,
      warnings: Object.keys(warnings).length > 0 ? warnings : undefined
    };
  }
};

/**
 * Run all cross-field validations for customer
 */
export function validateCustomerCrossFields(
  data: Partial<CustomerData>,
  locationCount: number = 0
): ValidationResult {
  const results: ValidationResult[] = [
    customerCrossFieldValidators.validateAddress(data),
    customerCrossFieldValidators.validateHotelRequirements(data),
    customerCrossFieldValidators.validateHospitalRequirements(data),
    customerCrossFieldValidators.validateChainCustomer(data, locationCount),
    customerCrossFieldValidators.validateContactInfo(data)
  ];
  
  return mergeValidationResults(results);
}

/**
 * Run all cross-field validations for location
 */
export function validateLocationCrossFields(
  data: Partial<LocationData>,
  industry: string,
  isFirst: boolean = false
): ValidationResult {
  const results: ValidationResult[] = [
    locationCrossFieldValidators.validateAddress(data),
    locationCrossFieldValidators.validateMainLocation(data, isFirst),
    locationCrossFieldValidators.validateCapacity(data, industry)
  ];
  
  return mergeValidationResults(results);
}

/**
 * Merge multiple validation results
 */
function mergeValidationResults(results: ValidationResult[]): ValidationResult {
  const errors: Record<string, string> = {};
  const warnings: Record<string, string> = {};
  let isValid = true;
  
  results.forEach(result => {
    if (!result.isValid) {
      isValid = false;
    }
    
    if (result.errors) {
      Object.assign(errors, result.errors);
    }
    
    if (result.warnings) {
      Object.assign(warnings, result.warnings);
    }
  });
  
  return {
    isValid,
    errors: Object.keys(errors).length > 0 ? errors : undefined,
    warnings: Object.keys(warnings).length > 0 ? warnings : undefined
  };
}