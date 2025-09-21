import React, { createContext, useContext, useEffect, useState } from 'react';
import { useAuth } from '@/hooks/useAuth';

/**
 * SecurityProvider für FreshFoodz B2B CRM
 *
 * Ziel: Frontend-Security-Context für Territory + Multi-Contact + Lead-Protection
 * - Integriert mit Backend SessionSettingsFilter über Keycloak Claims
 * - Cached Security-Claims für Performance (session-persistent)
 * - Reactive Security-State für UI-Component-Visibility
 */

interface SecurityClaims {
  userId: string;
  orgId: string;
  territory: 'DE' | 'CH';
  scopes: string[];
  contactRoles: ('GF' | 'BUYER' | 'CHEF')[];
  roles: string[];
}

interface SecurityContextType {
  claims: SecurityClaims | null;
  isLoading: boolean;

  // Territory Security
  hasTerritory: (territory: string) => boolean;
  canAccessTerritory: (territory: string) => boolean;

  // Lead Protection
  isLeadOwner: (leadId: string) => boolean;
  isLeadCollaborator: (leadId: string) => boolean;
  canEditLead: (leadId: string) => boolean;
  canViewLead: (leadId: string) => boolean;

  // Multi-Contact Security
  hasContactRole: (role: string) => boolean;
  canViewCategory: (category: 'GENERAL' | 'COMMERCIAL' | 'PRODUCT') => boolean;

  // Scope-based Permissions
  hasScope: (scope: string) => boolean;
  canOverride: (operation: string) => boolean;

  // UI Helpers
  shouldShowComponent: (componentType: string, context?: any) => boolean;
  getVisibilityLevel: () => 'OWNER_ONLY' | 'COLLABORATORS' | 'ACCOUNT_TEAM' | 'ORG_READ';
}

const SecurityContext = createContext<SecurityContextType | null>(null);

export const useSecurityContext = () => {
  const context = useContext(SecurityContext);
  if (!context) {
    throw new Error('useSecurityContext must be used within SecurityProvider');
  }
  return context;
};

interface SecurityProviderProps {
  children: React.ReactNode;
}

export const SecurityProvider: React.FC<SecurityProviderProps> = ({ children }) => {
  const { user, isAuthenticated, getAccessToken } = useAuth();
  const [claims, setClaims] = useState<SecurityClaims | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [leadOwnerships, setLeadOwnerships] = useState<Set<string>>(new Set());
  const [leadCollaborations, setLeadCollaborations] = useState<Set<string>>(new Set());

  // Extract claims from Keycloak token
  useEffect(() => {
    if (!isAuthenticated || !user) {
      setClaims(null);
      setIsLoading(false);
      return;
    }

    const extractClaims = async () => {
      try {
        const token = await getAccessToken();
        if (!token) return;

        // Decode JWT payload (simplified - in production use jwt-decode library)
        const payload = JSON.parse(atob(token.split('.')[1]));

        const securityClaims: SecurityClaims = {
          userId: payload.sub || user.id,
          orgId: payload.org_id || 'freshfoodz', // Default org
          territory: payload.territory || 'DE', // Default territory
          scopes: Array.isArray(payload.scopes) ? payload.scopes : [],
          contactRoles: Array.isArray(payload.contact_roles) ? payload.contact_roles : ['GF'],
          roles: Array.isArray(payload.realm_access?.roles) ? payload.realm_access.roles : []
        };

        setClaims(securityClaims);

        // Cache in sessionStorage for performance
        sessionStorage.setItem('freshplan_security_claims', JSON.stringify(securityClaims));

      } catch (error) {
        console.error('Failed to extract security claims:', error);
        // Fallback to cached claims
        const cached = sessionStorage.getItem('freshplan_security_claims');
        if (cached) {
          setClaims(JSON.parse(cached));
        }
      } finally {
        setIsLoading(false);
      }
    };

    extractClaims();
  }, [isAuthenticated, user, getAccessToken]);

  // Load user's lead ownerships and collaborations
  useEffect(() => {
    if (!claims?.userId) return;

    const loadLeadAccess = async () => {
      try {
        const [ownershipsRes, collaborationsRes] = await Promise.all([
          fetch(`/api/security/lead-ownerships`, {
            headers: { Authorization: `Bearer ${await getAccessToken()}` }
          }),
          fetch(`/api/security/lead-collaborations`, {
            headers: { Authorization: `Bearer ${await getAccessToken()}` }
          })
        ]);

        if (ownershipsRes.ok) {
          const ownerships = await ownershipsRes.json();
          setLeadOwnerships(new Set(ownerships.map((o: any) => o.leadId)));
        }

        if (collaborationsRes.ok) {
          const collaborations = await collaborationsRes.json();
          setLeadCollaborations(new Set(collaborations.map((c: any) => c.leadId)));
        }
      } catch (error) {
        console.error('Failed to load lead access:', error);
      }
    };

    loadLeadAccess();
  }, [claims?.userId, getAccessToken]);

  const securityContextValue: SecurityContextType = {
    claims,
    isLoading,

    // Territory Security
    hasTerritory: (territory: string) => claims?.territory === territory,
    canAccessTerritory: (territory: string) => claims?.territory === territory,

    // Lead Protection
    isLeadOwner: (leadId: string) => leadOwnerships.has(leadId),
    isLeadCollaborator: (leadId: string) => leadCollaborations.has(leadId),
    canEditLead: (leadId: string) =>
      leadOwnerships.has(leadId) || claims?.scopes.includes('lead:override') || false,
    canViewLead: (leadId: string) =>
      leadOwnerships.has(leadId) || leadCollaborations.has(leadId) ||
      claims?.scopes.includes('lead:read') || false,

    // Multi-Contact Security
    hasContactRole: (role: string) => claims?.contactRoles.includes(role as any) || false,
    canViewCategory: (category: 'GENERAL' | 'COMMERCIAL' | 'PRODUCT') => {
      if (!claims) return false;
      if (category === 'GENERAL') return true;
      if (category === 'COMMERCIAL') return claims.contactRoles.includes('BUYER') || claims.contactRoles.includes('GF');
      if (category === 'PRODUCT') return claims.contactRoles.includes('CHEF') || claims.contactRoles.includes('GF');
      return false;
    },

    // Scope-based Permissions
    hasScope: (scope: string) => claims?.scopes.includes(scope) || false,
    canOverride: (operation: string) =>
      claims?.scopes.includes(`${operation}:override`) ||
      claims?.scopes.includes('admin:all') || false,

    // UI Helpers
    shouldShowComponent: (componentType: string, context?: any) => {
      if (!claims) return false;

      switch (componentType) {
        case 'lead-edit-button':
          return context?.leadId ?
            leadOwnerships.has(context.leadId) || claims.scopes.includes('lead:override') : false;
        case 'territory-switcher':
          return claims.scopes.includes('territory:switch');
        case 'admin-panel':
          return claims.roles.includes('admin') || claims.scopes.includes('admin:all');
        case 'commercial-notes':
          return claims.contactRoles.includes('BUYER') || claims.contactRoles.includes('GF');
        case 'product-notes':
          return claims.contactRoles.includes('CHEF') || claims.contactRoles.includes('GF');
        default:
          return true;
      }
    },

    getVisibilityLevel: () => {
      if (!claims) return 'ORG_READ';
      if (claims.roles.includes('admin')) return 'ORG_READ';
      if (claims.scopes.includes('team:all')) return 'ACCOUNT_TEAM';
      return 'COLLABORATORS';
    }
  };

  return (
    <SecurityContext.Provider value={securityContextValue}>
      {children}
    </SecurityContext.Provider>
  );
};

// Hook für einfache Security-Checks in Components
export const useLeadSecurity = (leadId?: string) => {
  const security = useSecurityContext();

  return {
    canView: leadId ? security.canViewLead(leadId) : false,
    canEdit: leadId ? security.canEditLead(leadId) : false,
    isOwner: leadId ? security.isLeadOwner(leadId) : false,
    isCollaborator: leadId ? security.isLeadCollaborator(leadId) : false,
  };
};

// Hook für Multi-Contact Security
export const useContactSecurity = () => {
  const security = useSecurityContext();

  return {
    isGF: security.hasContactRole('GF'),
    isBuyer: security.hasContactRole('BUYER'),
    isChef: security.hasContactRole('CHEF'),
    canViewCommercial: security.canViewCategory('COMMERCIAL'),
    canViewProduct: security.canViewCategory('PRODUCT'),
  };
};

// Hook für Territory Security
export const useTerritorySecurity = () => {
  const security = useSecurityContext();

  return {
    currentTerritory: security.claims?.territory,
    isDE: security.hasTerritory('DE'),
    isCH: security.hasTerritory('CH'),
    canAccessDE: security.canAccessTerritory('DE'),
    canAccessCH: security.canAccessTerritory('CH'),
  };
};