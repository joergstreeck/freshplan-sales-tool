/**
 * Customer Onboarding Store Extensions
 *
 * Erweitert den Store um Funktionalität für die neue Step 2 Struktur
 * mit standortbasierter Angebotserfassung.
 */

import type { WritableDraft } from 'immer/dist/types/types-external';

// Service-Daten pro Standort
export interface LocationServiceData {
  // Hotel-spezifisch
  offersBreakfast?: 'ja' | 'nein';
  breakfastWarm?: 'ja' | 'nein';
  breakfastGuestsPerDay?: number;
  offersLunch?: 'ja' | 'nein';
  offersDinner?: 'ja' | 'nein';
  offersRoomService?: 'ja' | 'nein';
  offersEvents?: 'ja' | 'nein';
  eventCapacity?: number;
  roomCount?: number;
  averageOccupancy?: number;

  // Andere Branchen können hier ergänzt werden
  [key: string]: unknown;
}

// Erweiterung des Store-States
export interface CustomerOnboardingStateExtensions {
  // Neue Step 2 Felder
  expectedAnnualRevenue?: number;
  selectedLocationId: string | 'all';
  applyToAllLocations: boolean;
  locationServices: Map<string, LocationServiceData>;
  completedLocationIds: string[];
}

// Erweiterung der Store-Actions
export interface CustomerOnboardingActionsExtensions {
  // Umsatzerwartung
  setExpectedRevenue: (amount: number) => void;

  // Standort-Auswahl
  setSelectedLocation: (locationId: string | 'all') => void;
  setApplyToAll: (value: boolean) => void;

  // Service-Daten
  saveLocationServices: (data: LocationServiceData) => void;
  getLocationServices: (locationId: string) => LocationServiceData;
  markLocationCompleted: (locationId: string) => void;

  // Helper
  getAllLocationsWithServices: () => Array<{ locationId: string; services: LocationServiceData }>;
  hasAllLocationsCompleted: () => boolean;
}

/**
 * Store-Erweiterungen implementieren
 *
 * Diese Funktionen können in den bestehenden Store integriert werden.
 */
export const createStoreExtensions = (set: unknown, get: unknown) => ({
  // State
  expectedAnnualRevenue: undefined,
  selectedLocationId: 'all',
  applyToAllLocations: false,
  locationServices: new Map<string, LocationServiceData>(),
  completedLocationIds: [],

  // Actions
  setExpectedRevenue: (amount: number) => {
    set((state: WritableDraft<unknown>) => {
      state.customerData.expectedAnnualRevenue = amount;
      state.isDirty = true;
    });
  },

  setSelectedLocation: (locationId: string | 'all') => {
    set((state: WritableDraft<unknown>) => {
      state.selectedLocationId = locationId;
    });
  },

  setApplyToAll: (value: boolean) => {
    set((state: WritableDraft<unknown>) => {
      state.applyToAllLocations = value;
    });
  },

  saveLocationServices: (data: LocationServiceData) => {
    set((state: WritableDraft<unknown>) => {
      const { selectedLocationId, applyToAllLocations, locations } = get();

      if (selectedLocationId === 'all' || applyToAllLocations) {
        // Speichere für alle Standorte
        locations.forEach((loc: unknown) => {
          state.locationServices.set(loc.id, { ...data });
        });
        state.locationServices.set('all', { ...data });
      } else {
        // Speichere nur für ausgewählten Standort
        state.locationServices.set(selectedLocationId, { ...data });
      }

      // Markiere als abgeschlossen
      if (
        selectedLocationId !== 'all' &&
        !state.completedLocationIds.includes(selectedLocationId)
      ) {
        state.completedLocationIds.push(selectedLocationId);
      }

      state.isDirty = true;
    });
  },

  getLocationServices: (locationId: string): LocationServiceData => {
    const { locationServices, applyToAllLocations } = get();

    if (applyToAllLocations && locationServices.has('all')) {
      return locationServices.get('all') || {};
    }

    return locationServices.get(locationId) || {};
  },

  markLocationCompleted: (locationId: string) => {
    set((state: WritableDraft<unknown>) => {
      if (!state.completedLocationIds.includes(locationId)) {
        state.completedLocationIds.push(locationId);
      }
    });
  },

  getAllLocationsWithServices: () => {
    const { locations, locationServices } = get();
    return locations.map((loc: unknown) => ({
      locationId: loc.id,
      services: locationServices.get(loc.id) || {},
    }));
  },

  hasAllLocationsCompleted: () => {
    const { locations, completedLocationIds } = get();
    return locations.every((loc: unknown) =>
      completedLocationIds.includes((loc as { id: string }).id)
    );
  },
});

/**
 * Helper um die Erweiterungen in den bestehenden Store zu integrieren
 *
 * Beispiel:
 * ```typescript
 * export const useCustomerOnboardingStore = create<CustomerOnboardingState & CustomerOnboardingStateExtensions>()(
 *   persist(
 *     immer((set, get) => ({
 *       // ... existing state ...
 *       ...createStoreExtensions(set, get),
 *       // ... existing actions ...
 *     }))
 *   )
 * );
 * ```
 */
