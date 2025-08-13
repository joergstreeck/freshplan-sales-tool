/**
 * KanbanBoardDndKit - Refactored Version
 * 
 * This component has been refactored into smaller, more manageable components.
 * The original 1053-line file has been split into:
 * 
 * - kanban/KanbanBoardDndKit.tsx (main board component - 408 lines)
 * - kanban/OpportunityCard.tsx (card component - 271 lines)
 * - kanban/KanbanColumn.tsx (column component - 110 lines)
 * - kanban/mockData.ts (mock data - 89 lines)
 * 
 * This is a re-export for backward compatibility.
 */

export { KanbanBoardDndKit, OpportunityCard } from './kanban';

// Also re-export the old OpportunityCard for backward compatibility
export { OpportunityCard as default } from './kanban/OpportunityCard';