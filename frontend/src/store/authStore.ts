import { create } from 'zustand';

interface AuthState {
  userPermissions: string[];
  setPermissions: (permissions: string[]) => void;
}

export const useAuthStore = create<AuthState>()((set) => ({
  // Default permissions für Navigation - später aus Keycloak laden
  userPermissions: [
    'cockpit.view',
    'customers.create',
    'customers.view',
    'opportunities.view',  // M4 Opportunity Pipeline Permission
    'activities.view',     // Für Activities Sub-Item
    'reports.view',
    'settings.view',
  ],
  
  setPermissions: (permissions) => set({ userPermissions: permissions }),
}));