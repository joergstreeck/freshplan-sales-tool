import { create } from 'zustand';

interface AuthState {
  userPermissions: string[];
  setPermissions: (permissions: string[]) => void;
}

export const useAuthStore = create<AuthState>()(set => ({
  // Default permissions für Navigation - später aus Keycloak laden
  userPermissions: [
    'cockpit.view',
    'customers.create',
    'customers.view',
    'reports.view',
    'settings.view',
    'admin.view', // Admin-Bereich Zugriff
    'auditor.view', // Audit Dashboard Zugriff
  ],

  setPermissions: permissions => set({ userPermissions: permissions }),
}));
