/**
 * FC-005 Location Configuration Tests
 * 
 * Tests für die externalisierten Location-Konfigurationen.
 * Stellt sicher, dass alle Kategorien und Templates korrekt exportiert werden.
 * 
 * @see /Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/config/locationCategories.ts
 * @see /Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/config/industryTemplates.ts
 */

import { describe, it, expect } from 'vitest';
import {
  categoryIcons,
  categoryLabels,
  getCategoryIcon,
  getCategoryLabel,
  industryTemplates,
  getIndustryTemplates,
  hasIndustryTemplates
} from '../../../config';

describe('🔧 CR-003: Location Configuration', () => {
  
  describe('Category Configuration', () => {
    it('should export all category icon functions', () => {
      expect(categoryIcons).toBeDefined();
      expect(categoryIcons.restaurant).toBeDefined();
      expect(typeof categoryIcons.restaurant).toBe('function');
      expect(categoryIcons.cafeteria).toBeDefined();
      expect(categoryIcons.kiosk).toBeDefined();
      expect(categoryIcons.station).toBeDefined();
      expect(categoryIcons.kitchen).toBeDefined();
      expect(categoryIcons.storage).toBeDefined();
      expect(categoryIcons.other).toBeDefined();
    });

    it('should export all category labels', () => {
      expect(categoryLabels).toBeDefined();
      expect(categoryLabels.restaurant).toBe('Restaurant');
      expect(categoryLabels.cafeteria).toBe('Cafeteria');
      expect(categoryLabels.kiosk).toBe('Kiosk');
      expect(categoryLabels.station).toBe('Station/Abteilung');
      expect(categoryLabels.kitchen).toBe('Küche');
      expect(categoryLabels.storage).toBe('Lager');
      expect(categoryLabels.other).toBe('Sonstiges');
    });

    it('should get correct icon for category', () => {
      const icon = getCategoryIcon('restaurant');
      expect(icon).toBeDefined();
      expect(icon.type).toBeDefined(); // React element
    });

    it('should get correct label for category', () => {
      expect(getCategoryLabel('restaurant')).toBe('Restaurant');
      expect(getCategoryLabel('station')).toBe('Station/Abteilung');
    });

    it('should handle unknown category gracefully', () => {
      const unknownIcon = getCategoryIcon('unknown' as any);
      expect(unknownIcon).toBeDefined();
      // Should return the 'other' icon
      
      const unknownLabel = getCategoryLabel('unknown' as any);
      expect(unknownLabel).toBe('Sonstiges');
    });
  });

  describe('Industry Templates', () => {
    it('should export industry templates', () => {
      expect(industryTemplates).toBeDefined();
      expect(industryTemplates.hotel).toBeDefined();
      expect(industryTemplates.krankenhaus).toBeDefined();
      expect(industryTemplates.seniorenresidenz).toBeDefined();
      expect(industryTemplates.restaurant).toBeDefined();
      expect(industryTemplates.betriebsrestaurant).toBeDefined();
    });

    it('should get templates for specific industry', () => {
      const hotelTemplates = getIndustryTemplates('hotel');
      expect(hotelTemplates).toBeDefined();
      expect(hotelTemplates.length).toBeGreaterThan(0);
      expect(hotelTemplates[0]).toHaveProperty('name');
      expect(hotelTemplates[0]).toHaveProperty('category');
    });

    it('should return empty array for unknown industry', () => {
      const unknownTemplates = getIndustryTemplates('unknown' as any);
      expect(unknownTemplates).toEqual([]);
    });

    it('should check if industry has templates', () => {
      expect(hasIndustryTemplates('hotel')).toBe(true);
      expect(hasIndustryTemplates('krankenhaus')).toBe(true);
      expect(hasIndustryTemplates('unknown' as any)).toBe(false);
    });

    it('should have correct structure for templates', () => {
      const hotelTemplates = getIndustryTemplates('hotel');
      const firstTemplate = hotelTemplates[0];
      
      expect(firstTemplate).toHaveProperty('name', 'Restaurant Haupthaus');
      expect(firstTemplate).toHaveProperty('category', 'restaurant');
      expect(firstTemplate).toHaveProperty('description');
      expect(firstTemplate).toHaveProperty('capacity');
    });
  });

  describe('Configuration Completeness', () => {
    it('should have icon functions for all categories', () => {
      Object.keys(categoryLabels).forEach(category => {
        const iconFunc = categoryIcons[category as keyof typeof categoryIcons];
        expect(iconFunc).toBeDefined();
        expect(typeof iconFunc).toBe('function');
      });
    });

    it('should have valid categories in all templates', () => {
      Object.values(industryTemplates).forEach(templates => {
        templates.forEach(template => {
          expect(categoryLabels[template.category]).toBeDefined();
        });
      });
    });
  });
});