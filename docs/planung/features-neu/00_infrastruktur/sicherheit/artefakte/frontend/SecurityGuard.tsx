import React from 'react';
import { useSecurityContext } from './SecurityProvider';

/**
 * SecurityGuard Component für FreshFoodz B2B CRM
 *
 * Ziel: Deklarative Security-Gates für React-Components
 * - Conditional Rendering basierend auf Security-Claims
 * - Performance-optimiert mit React.memo
 * - Flexible Permission-Checks für verschiedene Use-Cases
 */

interface SecurityGuardProps {
  children: React.ReactNode;
  fallback?: React.ReactNode;

  // Territory-based Guards
  requireTerritory?: 'DE' | 'CH' | string;
  allowTerritories?: string[];

  // Lead-based Guards
  requireLeadOwnership?: string; // leadId
  requireLeadAccess?: string; // leadId (owner OR collaborator)
  requireLeadEdit?: string; // leadId

  // Role-based Guards
  requireContactRole?: 'GF' | 'BUYER' | 'CHEF' | string;
  requireAnyContactRole?: string[];
  requireAllContactRoles?: string[];

  // Scope-based Guards
  requireScope?: string;
  requireAnyScope?: string[];
  requireAllScopes?: string[];

  // Content Category Guards
  requireCategoryAccess?: 'GENERAL' | 'COMMERCIAL' | 'PRODUCT';

  // Advanced Guards
  requireOverride?: string; // operation name
  requireAdmin?: boolean;

  // Custom Guard Function
  customGuard?: (claims: any) => boolean;

  // UI Behavior
  showLoadingSpinner?: boolean;
  logAccessDenied?: boolean;
}

export const SecurityGuard: React.FC<SecurityGuardProps> = React.memo(({
  children,
  fallback = null,
  requireTerritory,
  allowTerritories,
  requireLeadOwnership,
  requireLeadAccess,
  requireLeadEdit,
  requireContactRole,
  requireAnyContactRole,
  requireAllContactRoles,
  requireScope,
  requireAnyScope,
  requireAllScopes,
  requireCategoryAccess,
  requireOverride,
  requireAdmin,
  customGuard,
  showLoadingSpinner = false,
  logAccessDenied = false,
}) => {
  const security = useSecurityContext();

  // Loading State
  if (security.isLoading && showLoadingSpinner) {
    return <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-freshfoodz-primary" />;
  }

  if (security.isLoading) {
    return null; // Hide während Loading
  }

  if (!security.claims) {
    logAccessDenied && console.warn('SecurityGuard: No security claims available');
    return <>{fallback}</>;
  }

  // Territory Guards
  if (requireTerritory && !security.hasTerritory(requireTerritory)) {
    logAccessDenied && console.warn(`SecurityGuard: Territory ${requireTerritory} required, user has ${security.claims.territory}`);
    return <>{fallback}</>;
  }

  if (allowTerritories && !allowTerritories.some(t => security.hasTerritory(t))) {
    logAccessDenied && console.warn(`SecurityGuard: Territories ${allowTerritories.join(',')} required, user has ${security.claims.territory}`);
    return <>{fallback}</>;
  }

  // Lead Guards
  if (requireLeadOwnership && !security.isLeadOwner(requireLeadOwnership)) {
    logAccessDenied && console.warn(`SecurityGuard: Lead ownership required for ${requireLeadOwnership}`);
    return <>{fallback}</>;
  }

  if (requireLeadAccess && !security.canViewLead(requireLeadAccess)) {
    logAccessDenied && console.warn(`SecurityGuard: Lead access required for ${requireLeadAccess}`);
    return <>{fallback}</>;
  }

  if (requireLeadEdit && !security.canEditLead(requireLeadEdit)) {
    logAccessDenied && console.warn(`SecurityGuard: Lead edit permission required for ${requireLeadEdit}`);
    return <>{fallback}</>;
  }

  // Contact Role Guards
  if (requireContactRole && !security.hasContactRole(requireContactRole)) {
    logAccessDenied && console.warn(`SecurityGuard: Contact role ${requireContactRole} required`);
    return <>{fallback}</>;
  }

  if (requireAnyContactRole && !requireAnyContactRole.some(role => security.hasContactRole(role))) {
    logAccessDenied && console.warn(`SecurityGuard: Any contact role ${requireAnyContactRole.join(',')} required`);
    return <>{fallback}</>;
  }

  if (requireAllContactRoles && !requireAllContactRoles.every(role => security.hasContactRole(role))) {
    logAccessDenied && console.warn(`SecurityGuard: All contact roles ${requireAllContactRoles.join(',')} required`);
    return <>{fallback}</>;
  }

  // Scope Guards
  if (requireScope && !security.hasScope(requireScope)) {
    logAccessDenied && console.warn(`SecurityGuard: Scope ${requireScope} required`);
    return <>{fallback}</>;
  }

  if (requireAnyScope && !requireAnyScope.some(scope => security.hasScope(scope))) {
    logAccessDenied && console.warn(`SecurityGuard: Any scope ${requireAnyScope.join(',')} required`);
    return <>{fallback}</>;
  }

  if (requireAllScopes && !requireAllScopes.every(scope => security.hasScope(scope))) {
    logAccessDenied && console.warn(`SecurityGuard: All scopes ${requireAllScopes.join(',')} required`);
    return <>{fallback}</>;
  }

  // Category Guards
  if (requireCategoryAccess && !security.canViewCategory(requireCategoryAccess)) {
    logAccessDenied && console.warn(`SecurityGuard: Category access ${requireCategoryAccess} required`);
    return <>{fallback}</>;
  }

  // Advanced Guards
  if (requireOverride && !security.canOverride(requireOverride)) {
    logAccessDenied && console.warn(`SecurityGuard: Override permission ${requireOverride} required`);
    return <>{fallback}</>;
  }

  if (requireAdmin && !security.hasScope('admin:all')) {
    logAccessDenied && console.warn('SecurityGuard: Admin permission required');
    return <>{fallback}</>;
  }

  // Custom Guard
  if (customGuard && !customGuard(security.claims)) {
    logAccessDenied && console.warn('SecurityGuard: Custom guard condition failed');
    return <>{fallback}</>;
  }

  // All guards passed
  return <>{children}</>;
});

SecurityGuard.displayName = 'SecurityGuard';

// Specialized Guards für häufige Use-Cases

export const TerritoryGuard: React.FC<{
  territory: 'DE' | 'CH';
  children: React.ReactNode;
  fallback?: React.ReactNode;
}> = ({ territory, children, fallback }) => (
  <SecurityGuard requireTerritory={territory} fallback={fallback}>
    {children}
  </SecurityGuard>
);

export const LeadOwnerGuard: React.FC<{
  leadId: string;
  children: React.ReactNode;
  fallback?: React.ReactNode;
}> = ({ leadId, children, fallback }) => (
  <SecurityGuard requireLeadOwnership={leadId} fallback={fallback}>
    {children}
  </SecurityGuard>
);

export const ContactRoleGuard: React.FC<{
  role: 'GF' | 'BUYER' | 'CHEF';
  children: React.ReactNode;
  fallback?: React.ReactNode;
}> = ({ role, children, fallback }) => (
  <SecurityGuard requireContactRole={role} fallback={fallback}>
    {children}
  </SecurityGuard>
);

export const CommercialContentGuard: React.FC<{
  children: React.ReactNode;
  fallback?: React.ReactNode;
}> = ({ children, fallback }) => (
  <SecurityGuard requireCategoryAccess="COMMERCIAL" fallback={fallback}>
    {children}
  </SecurityGuard>
);

export const ProductContentGuard: React.FC<{
  children: React.ReactNode;
  fallback?: React.ReactNode;
}> = ({ children, fallback }) => (
  <SecurityGuard requireCategoryAccess="PRODUCT" fallback={fallback}>
    {children}
  </SecurityGuard>
);

export const AdminGuard: React.FC<{
  children: React.ReactNode;
  fallback?: React.ReactNode;
}> = ({ children, fallback }) => (
  <SecurityGuard requireAdmin fallback={fallback}>
    {children}
  </SecurityGuard>
);

// Convenience Wrapper für Lead-spezifische Actions
export const LeadActionGuard: React.FC<{
  leadId: string;
  action: 'view' | 'edit' | 'delete';
  children: React.ReactNode;
  fallback?: React.ReactNode;
}> = ({ leadId, action, children, fallback }) => {
  const props = {
    view: { requireLeadAccess: leadId },
    edit: { requireLeadEdit: leadId },
    delete: { requireLeadOwnership: leadId, requireScope: 'lead:delete' },
  }[action];

  return (
    <SecurityGuard {...props} fallback={fallback}>
      {children}
    </SecurityGuard>
  );
};

// Multi-Condition Guard für komplexe Szenarien
export const MultiConditionGuard: React.FC<{
  conditions: {
    territory?: string[];
    contactRoles?: string[];
    scopes?: string[];
    leadAccess?: string;
    custom?: (claims: any) => boolean;
  };
  operator?: 'AND' | 'OR'; // Default: AND
  children: React.ReactNode;
  fallback?: React.ReactNode;
}> = ({ conditions, operator = 'AND', children, fallback }) => {
  return (
    <SecurityGuard
      allowTerritories={conditions.territory}
      requireAnyContactRole={conditions.contactRoles}
      requireAnyScope={conditions.scopes}
      requireLeadAccess={conditions.leadAccess}
      customGuard={conditions.custom}
      fallback={fallback}
    >
      {children}
    </SecurityGuard>
  );
};