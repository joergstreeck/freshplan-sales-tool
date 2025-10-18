/**
 * Mock Data for Opportunity Management
 *
 * TODO: Replace with real data from Auth Context and API in Sprint 2.1.7.2
 *
 * @author FreshPlan Team
 * @since 2.0.0 (Sprint 2.1.7.1)
 */

/**
 * Mock Current User
 * TODO: Replace with data from Auth Context
 */
export const MOCK_CURRENT_USER = {
  id: 'current-user-123',
  name: 'Max Manager',
  role: 'MANAGER', // or 'USER' for sales rep
} as const;

/**
 * Mock Team Members for User Filter Dropdown
 * TODO: Replace with data from API (/api/users or /api/teams)
 */
export const MOCK_TEAM_MEMBERS = [
  { id: 'current-user-123', name: 'Max Manager' },
  { id: 'user-1', name: 'Anna Schmidt' },
  { id: 'user-2', name: 'Peter Meier' },
  { id: 'user-3', name: 'Sarah Wagner' },
] as const;
