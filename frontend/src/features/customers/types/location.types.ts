/**
 * Location Entity Types
 * 
 * Types for managing customer locations (Standorte) and detailed locations (Ausgabestellen).
 * Used when chainCustomer = 'ja'.
 * 
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/01-TECH-CONCEPT/03-data-model.md
 */

/**
 * Location type enumeration
 */
export enum LocationType {
  /** Delivery address */
  LIEFERADRESSE = 'LIEFERADRESSE',
  /** Billing address */
  RECHNUNGSADRESSE = 'RECHNUNGSADRESSE',
  /** Combined delivery and billing */
  KOMBINIERT = 'KOMBINIERT'
}

/**
 * Core location entity
 * Represents a branch/site of a chain customer
 */
export interface Location {
  /** UUID primary key */
  id: string;
  /** Parent customer UUID */
  customerId: string;
  /** Type of location */
  locationType: LocationType;
  /** Sort order for display */
  sortOrder: number;
  /** Audit timestamps */
  createdAt: string;
  updatedAt: string;
}

/**
 * Location with resolved field values
 * Used for API responses and UI display
 */
export interface LocationWithFields extends Location {
  /** Resolved field values */
  fields: Record<string, any>;
  /** Number of detailed locations */
  detailedLocationCount?: number;
}

/**
 * Detailed location (Ausgabestelle)
 * Represents individual serving points within a location
 */
export interface DetailedLocation {
  /** UUID primary key */
  id: string;
  /** Parent location UUID */
  locationId: string;
  /** Name of the serving point */
  name: string;
  /** Address fields */
  street?: string;
  postalCode?: string;
  city?: string;
  /** Additional notes */
  notes?: string;
  /** Active status */
  isActive: boolean;
  /** Audit timestamps */
  createdAt: string;
  updatedAt: string;
}

/**
 * Location form data for wizard
 */
export interface LocationFormData {
  /** Location name from fields */
  locationName: string;
  /** Address from fields */
  street: string;
  postalCode: string;
  city: string;
  /** Type of location */
  locationType: LocationType;
  /** Industry-specific fields */
  [key: string]: any;
}

/**
 * Detailed location form data
 */
export interface DetailedLocationFormData {
  /** Serving point name */
  name: string;
  /** Optional different address */
  street?: string;
  postalCode?: string;
  city?: string;
  /** Additional notes */
  notes?: string;
}

/**
 * Location statistics by industry
 */
export interface LocationStatistics {
  /** Total number of locations */
  totalLocations: number;
  /** Total number of detailed locations */
  totalDetailedLocations: number;
  /** Industry-specific counts */
  industrySpecific?: {
    /** For hotels */
    totalRooms?: number;
    smallHotels?: number;
    mediumHotels?: number;
    largeHotels?: number;
    /** For hospitals */
    totalBeds?: number;
    /** For restaurants */
    totalSeats?: number;
    /** For senior residences */
    totalResidents?: number;
  };
}

/**
 * Batch operation for detailed locations
 */
export interface DetailedLocationBatch {
  /** Parent location ID */
  locationId: string;
  /** List of detailed locations to create/update */
  detailedLocations: DetailedLocationFormData[];
  /** Replace all existing detailed locations */
  replaceExisting?: boolean;
}