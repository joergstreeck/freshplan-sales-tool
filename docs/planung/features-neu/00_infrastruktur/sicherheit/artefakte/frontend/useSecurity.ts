import { useMemo } from 'react';
import { useSecurityContext } from './SecurityProvider';

/**
 * Enhanced Security Hooks für FreshFoodz B2B CRM
 *
 * Ziel: Performance-optimierte React-Hooks für Security-Operations
 * - Memoized Security-Checks für Re-Render-Optimierung
 * - Composable Security-Logic für Complex-Components
 * - Type-safe Security-Operations
 */

// Lead-spezifische Security Hook
export const useLeadPermissions = (leadId?: string) => {
  const security = useSecurityContext();

  return useMemo(() => {
    if (!leadId || !security.claims) {
      return {
        canView: false,
        canEdit: false,
        canDelete: false,
        canAssignCollaborators: false,
        isOwner: false,
        isCollaborator: false,
        accessLevel: 'NONE' as const,
      };
    }

    const isOwner = security.isLeadOwner(leadId);
    const isCollaborator = security.isLeadCollaborator(leadId);
    const canView = security.canViewLead(leadId);
    const canEdit = security.canEditLead(leadId);

    return {
      canView,
      canEdit,
      canDelete: isOwner && security.hasScope('lead:delete'),
      canAssignCollaborators: isOwner || security.hasScope('lead:override'),
      isOwner,
      isCollaborator,
      accessLevel: isOwner ? 'OWNER' : isCollaborator ? 'COLLABORATOR' : canView ? 'VIEWER' : 'NONE' as const,
    };
  }, [leadId, security.claims, security.isLeadOwner, security.isLeadCollaborator, security.canViewLead, security.canEditLead, security.hasScope]);
};

// Territory-spezifische Security Hook
export const useTerritoryPermissions = () => {
  const security = useSecurityContext();

  return useMemo(() => {
    if (!security.claims) {
      return {
        currentTerritory: null,
        availableTerritories: [],
        canSwitchTerritory: false,
        canAccessDE: false,
        canAccessCH: false,
        isGlobalAccess: false,
      };
    }

    const currentTerritory = security.claims.territory;
    const canSwitchTerritory = security.hasScope('territory:switch');
    const isGlobalAccess = security.hasScope('territory:all') || security.hasScope('admin:all');

    return {
      currentTerritory,
      availableTerritories: isGlobalAccess ? ['DE', 'CH'] : [currentTerritory],
      canSwitchTerritory,
      canAccessDE: currentTerritory === 'DE' || isGlobalAccess,
      canAccessCH: currentTerritory === 'CH' || isGlobalAccess,
      isGlobalAccess,
    };
  }, [security.claims, security.hasScope]);
};

// Multi-Contact Security Hook
export const useContactRolePermissions = () => {
  const security = useSecurityContext();

  return useMemo(() => {
    if (!security.claims) {
      return {
        roles: [],
        isGF: false,
        isBuyer: false,
        isChef: false,
        canViewCommercialContent: false,
        canViewProductContent: false,
        canViewGeneralContent: true,
        contentVisibilityLevel: 'NONE' as const,
      };
    }

    const roles = security.claims.contactRoles;
    const isGF = roles.includes('GF');
    const isBuyer = roles.includes('BUYER');
    const isChef = roles.includes('CHEF');

    // GF sieht alles, BUYER sieht Commercial + General, CHEF sieht Product + General
    const canViewCommercialContent = isGF || isBuyer;
    const canViewProductContent = isGF || isChef;

    const contentVisibilityLevel = isGF ? 'ALL' :
                                  (isBuyer && isChef) ? 'COMMERCIAL_AND_PRODUCT' :
                                  isBuyer ? 'COMMERCIAL' :
                                  isChef ? 'PRODUCT' : 'GENERAL' as const;

    return {
      roles,
      isGF,
      isBuyer,
      isChef,
      canViewCommercialContent,
      canViewProductContent,
      canViewGeneralContent: true, // Alle können General Content sehen
      contentVisibilityLevel,
    };
  }, [security.claims]);
};

// Note/Activity Visibility Hook
export const useContentVisibility = (category: 'GENERAL' | 'COMMERCIAL' | 'PRODUCT') => {
  const security = useSecurityContext();
  const contactRoles = useContactRolePermissions();

  return useMemo(() => {
    if (!security.claims) return false;

    switch (category) {
      case 'GENERAL':
        return true; // Alle können General Content sehen
      case 'COMMERCIAL':
        return contactRoles.canViewCommercialContent;
      case 'PRODUCT':
        return contactRoles.canViewProductContent;
      default:
        return false;
    }
  }, [category, security.claims, contactRoles.canViewCommercialContent, contactRoles.canViewProductContent]);
};

// Scope-basierte Permissions Hook
export const useScopePermissions = () => {
  const security = useSecurityContext();

  return useMemo(() => {
    if (!security.claims) {
      return {
        scopes: [],
        hasScope: () => false,
        canCreateLeads: false,
        canEditAnyLead: false,
        canDeleteLeads: false,
        canManageUsers: false,
        canViewReports: false,
        canExportData: false,
        isAdmin: false,
      };
    }

    const hasScope = (scope: string) => security.hasScope(scope);

    return {
      scopes: security.claims.scopes,
      hasScope,
      canCreateLeads: hasScope('lead:create'),
      canEditAnyLead: hasScope('lead:override'),
      canDeleteLeads: hasScope('lead:delete'),
      canManageUsers: hasScope('user:manage'),
      canViewReports: hasScope('reports:view'),
      canExportData: hasScope('data:export'),
      isAdmin: hasScope('admin:all'),
    };
  }, [security.claims, security.hasScope]);
};

// UI-Visibility Hook für Components
export const useUIPermissions = () => {
  const security = useSecurityContext();
  const scopePerms = useScopePermissions();
  const territoryPerms = useTerritoryPermissions();

  return useMemo(() => ({
    // Navigation Elements
    showAdminPanel: scopePerms.isAdmin,
    showReportsSection: scopePerms.canViewReports,
    showUserManagement: scopePerms.canManageUsers,
    showTerritorySwitch: territoryPerms.canSwitchTerritory,

    // Lead-related UI
    showCreateLeadButton: scopePerms.canCreateLeads,
    showBulkActions: scopePerms.canEditAnyLead || scopePerms.isAdmin,
    showExportButton: scopePerms.canExportData,

    // Content-related UI
    shouldShowComponent: (componentType: string, context?: any) =>
      security.shouldShowComponent(componentType, context),

    // Debug Info (nur in Development)
    debugInfo: process.env.NODE_ENV === 'development' ? {
      userId: security.claims?.userId,
      territory: security.claims?.territory,
      roles: security.claims?.contactRoles,
      scopes: security.claims?.scopes,
    } : undefined,
  }), [security, scopePerms, territoryPerms]);
};

// Compliance & Audit Hook
export const useSecurityAudit = () => {
  const security = useSecurityContext();

  return useMemo(() => ({
    // Audit-Trail Information
    getUserId: () => security.claims?.userId,
    getOrgId: () => security.claims?.orgId,
    getTerritory: () => security.claims?.territory,

    // GDPR-relevante Funktionen
    requestDataDeletion: async () => {
      // Implementation für GDPR Data Deletion Request
      // Wird über Backend-API abgewickelt
      if (!security.claims?.userId) return false;

      try {
        const response = await fetch('/api/gdpr/data-deletion-request', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${await getAccessToken()}`, // Aus useAuth Hook
          },
          body: JSON.stringify({
            userId: security.claims.userId,
            requestType: 'FULL_DELETION',
            reason: 'User requested data deletion'
          })
        });
        return response.ok;
      } catch (error) {
        console.error('GDPR data deletion request failed:', error);
        return false;
      }
    },

    // Security Event Logging
    logSecurityEvent: async (event: string, details?: any) => {
      if (!security.claims) return;

      try {
        await fetch('/api/security/audit-log', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${await getAccessToken()}`,
          },
          body: JSON.stringify({
            event,
            details,
            timestamp: new Date().toISOString(),
          })
        });
      } catch (error) {
        console.error('Security event logging failed:', error);
      }
    },
  }), [security.claims]);
};

// Performance-optimierte Kombinationen für häufige Use-Cases
export const useLeadCardPermissions = (leadId: string) => {
  const leadPerms = useLeadPermissions(leadId);
  const contactPerms = useContactRolePermissions();

  return useMemo(() => ({
    ...leadPerms,
    showCommercialNotes: contactPerms.canViewCommercialContent,
    showProductNotes: contactPerms.canViewProductContent,
    showEditButton: leadPerms.canEdit,
    showDeleteButton: leadPerms.canDelete,
    showCollaboratorList: leadPerms.accessLevel !== 'NONE',
  }), [leadPerms, contactPerms]);
};

// Helper für dynamische Permission-Checks in Lists
export const useBulkPermissions = (leadIds: string[]) => {
  const security = useSecurityContext();

  return useMemo(() => {
    const ownedLeads = leadIds.filter(id => security.isLeadOwner(id));
    const accessibleLeads = leadIds.filter(id => security.canViewLead(id));
    const editableLeads = leadIds.filter(id => security.canEditLead(id));

    return {
      ownedCount: ownedLeads.length,
      accessibleCount: accessibleLeads.length,
      editableCount: editableLeads.length,
      canBulkEdit: editableLeads.length > 0,
      canBulkDelete: ownedLeads.length > 0 && security.hasScope('lead:delete'),
      allOwned: ownedLeads.length === leadIds.length,
      allAccessible: accessibleLeads.length === leadIds.length,
    };
  }, [leadIds, security]);
};

// Helper um Access Token zu erhalten (würde normalerweise aus useAuth kommen)
const getAccessToken = async (): Promise<string> => {
  // Placeholder - in echter Implementation aus useAuth Hook
  return localStorage.getItem('access_token') || '';
};