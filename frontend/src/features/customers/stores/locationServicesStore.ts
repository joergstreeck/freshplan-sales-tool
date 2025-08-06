/**
 * Location Services Store für Step 4
 * 
 * Separater Store für die Verwaltung von standortspezifischen Services.
 * Ermöglicht Bulk-Actions und Progress-Tracking.
 */

import { create } from 'zustand';
import { devtools, persist } from 'zustand/middleware';
import { immer } from 'zustand/middleware/immer';
import type { CustomerLocation } from '../types/customer.types';

export interface LocationServiceData {
  locationId: string;
  // Hotel Services
  offersBreakfast?: boolean;
  breakfastWarm?: boolean;
  breakfastGuestsPerDay?: number;
  offersLunch?: boolean;
  offersDinner?: boolean;
  offersRoomService?: boolean;
  offersEvents?: boolean;
  eventCapacity?: number;
  roomCount?: number;
  averageOccupancy?: number;
  
  // Krankenhaus Services
  mealSystem?: 'COOK_AND_SERVE' | 'COOK_AND_CHILL' | 'FROZEN';
  bedsCount?: number;
  mealsPerDay?: number;
  offersVegetarian?: boolean;
  offersVegan?: boolean;
  offersHalal?: boolean;
  offersKosher?: boolean;
  
  // Betriebsrestaurant Services
  operatingDays?: number;
  lunchGuests?: number;
  subsidized?: boolean;
}

interface LocationServicesState {
  // State
  locationServices: Record<string, LocationServiceData>;
  currentLocationIndex: number;
  completedLocationIds: string[];
  applyToAll: boolean;
  
  // Actions
  saveLocationServices: (locationId: string, services: LocationServiceData) => void;
  copyLocationServices: (fromId: string, toIds: string[]) => void;
  setCurrentLocationIndex: (index: number) => void;
  markLocationCompleted: (locationId: string) => void;
  setApplyToAll: (value: boolean) => void;
  applyToAllRemaining: (locations: CustomerLocation[], currentServices: LocationServiceData) => void;
  clearAllServices: () => void;
  
  // Getters
  getLocationServices: (locationId: string) => LocationServiceData | undefined;
  isLocationCompleted: (locationId: string) => boolean;
  getProgress: (totalLocations: number) => number;
}

export const useLocationServicesStore = create<LocationServicesState>()(
  devtools(
    persist(
      immer((set, get) => ({
        // Initial State
        locationServices: {},
        currentLocationIndex: 0,
        completedLocationIds: [],
        applyToAll: false,
        
        // Save services for a specific location
        saveLocationServices: (locationId: string, services: LocationServiceData) => {
          set((state) => {
            state.locationServices[locationId] = {
              ...services,
              locationId
            };
            
            // Mark as completed if not already
            if (!state.completedLocationIds.includes(locationId)) {
              state.completedLocationIds.push(locationId);
            }
          });
        },
        
        // Copy services from one location to multiple others
        copyLocationServices: (fromId: string, toIds: string[]) => {
          const sourceServices = get().locationServices[fromId];
          if (!sourceServices) return;
          
          set((state) => {
            toIds.forEach(toId => {
              state.locationServices[toId] = {
                ...sourceServices,
                locationId: toId
              };
              
              if (!state.completedLocationIds.includes(toId)) {
                state.completedLocationIds.push(toId);
              }
            });
          });
        },
        
        // Navigation
        setCurrentLocationIndex: (index: number) => {
          set((state) => {
            state.currentLocationIndex = index;
          });
        },
        
        // Mark location as completed
        markLocationCompleted: (locationId: string) => {
          set((state) => {
            if (!state.completedLocationIds.includes(locationId)) {
              state.completedLocationIds.push(locationId);
            }
          });
        },
        
        // Apply to all toggle
        setApplyToAll: (value: boolean) => {
          set((state) => {
            state.applyToAll = value;
          });
        },
        
        // Apply current services to all remaining locations
        applyToAllRemaining: (locations: CustomerLocation[], currentServices: LocationServiceData) => {
          const completedIds = get().completedLocationIds;
          const remainingLocationIds = locations
            .map(l => l.id)
            .filter(id => !completedIds.includes(id));
          
          get().copyLocationServices(currentServices.locationId, remainingLocationIds);
        },
        
        // Clear all services (for reset)
        clearAllServices: () => {
          set((state) => {
            state.locationServices = {};
            state.completedLocationIds = [];
            state.currentLocationIndex = 0;
            state.applyToAll = false;
          });
        },
        
        // Getters
        getLocationServices: (locationId: string) => {
          return get().locationServices[locationId];
        },
        
        isLocationCompleted: (locationId: string) => {
          return get().completedLocationIds.includes(locationId);
        },
        
        getProgress: (totalLocations: number) => {
          const completed = get().completedLocationIds.length;
          return totalLocations > 0 ? (completed / totalLocations) * 100 : 0;
        }
      })),
      {
        name: 'location-services-storage',
        partialize: (state) => ({
          locationServices: state.locationServices,
          completedLocationIds: state.completedLocationIds,
          currentLocationIndex: state.currentLocationIndex
        })
      }
    )
  )
);