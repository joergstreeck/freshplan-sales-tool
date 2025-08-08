/**
 * FC-005 Customer Configuration Index
 *
 * Zentrale Export-Datei für alle Konfigurationen des Customer-Moduls.
 * Ermöglicht einfachen Import aller Konfigurationen mit einem Statement.
 *
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/03-location-management.md
 */

// Location Categories Configuration
export {
  categoryIcons,
  categoryLabels,
  getCategoryIcon,
  getCategoryLabel,
  availableCategories,
} from './locationCategories';

// Industry Templates Configuration
export {
  industryTemplates,
  getIndustryTemplates,
  hasIndustryTemplates,
  getIndustriesWithTemplates,
  type LocationTemplate,
} from './industryTemplates';
