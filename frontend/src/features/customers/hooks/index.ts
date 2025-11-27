/**
 * Hooks Export
 *
 * Central export for all custom hooks.
 *
 * Sprint 2.1.7.x Migration Note:
 * - useFieldDefinitions and useFieldDefinitionsApi have been REMOVED
 * - Replaced by useCustomerSchema (Server-Driven UI)
 * - See /src/hooks/useCustomerSchema.ts
 *
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/README.md
 */

// Auto-save hooks
export { useAutoSave } from './useAutoSave';
export { useAutoSaveApi } from './useAutoSaveApi';
