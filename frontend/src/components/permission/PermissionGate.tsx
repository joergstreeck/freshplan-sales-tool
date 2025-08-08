/**
 * Permission-based Content Gating Component
 * 
 * @module components/permission/PermissionGate
 * @description Enterprise-grade permission gating für conditional rendering basierend auf
 *              User-Permissions. Implementiert RBAC (Role-Based Access Control) mit
 *              graceful loading states und flexible fallback options.
 * 
 * @example
 * ```tsx
 * // Einfache Permission-Prüfung
 * <PermissionGate permission="customers:write">
 *   <Button>Kunde bearbeiten</Button>
 * </PermissionGate>
 * 
 * // Mit Custom Fallback
 * <PermissionGate 
 *   permission="admin:access" 
 *   fallback={<Alert>Keine Berechtigung</Alert>}
 * >
 *   <AdminPanel />
 * </PermissionGate>
 * 
 * // Multiple Permissions (OR-Logik)
 * <MultiPermissionGate permissions={["manager:view", "admin:view"]}>
 *   <Dashboard />
 * </MultiPermissionGate>
 * ```
 * 
 * @since 2.0.0
 * @see {@link usePermissions} - Hook für Permission-Logik
 * @see {@link PermissionButton} - Button mit Permission-Check
 */

import React, { ReactNode } from 'react';
import { Skeleton } from '@mui/material';
import { usePermissions } from '../../contexts/PermissionContext';

/**
 * Props für PermissionGate Component
 * 
 * @interface PermissionGateProps
 */
interface PermissionGateProps {
  /** The permission code to check (e.g., "customers:read") */
  permission: string;
  /** Content to render if permission is granted */
  children: ReactNode;
  /** Optional fallback content if permission is denied */
  fallback?: ReactNode;
  /** Type of loading skeleton to show */
  loadingSkeleton?: 'text' | 'rectangular' | 'circular';
  /** Height for loading skeleton */
  loadingHeight?: number;
}

export const PermissionGate: React.FC<PermissionGateProps> = ({
  permission,
  children,
  fallback = null,
  loadingSkeleton = 'rectangular',
  loadingHeight = 40,
}) => {
  const { hasPermission, isLoading } = usePermissions();

  if (isLoading) {
    return (
      <Skeleton variant={loadingSkeleton} height={loadingHeight} sx={{ bgcolor: 'grey.100' }} />
    );
  }

  return hasPermission(permission) ? <>{children}</> : <>{fallback}</>;
};

/**
 * Multiple Permission Gate Component
 * 
 * @description Prüft mehrere Permissions mit OR-Logik.
 *              Rendert Content wenn MINDESTENS EINE der angegebenen
 *              Permissions vorhanden ist.
 * 
 * @interface MultiPermissionGateProps
 */
interface MultiPermissionGateProps {
  /** Array of permission codes - user needs ANY of these */
  permissions: string[];
  /** Content to render if any permission is granted */
  children: ReactNode;
  /** Optional fallback content if no permissions */
  fallback?: ReactNode;
  /** Require ALL permissions instead of ANY */
  requireAll?: boolean;
}

export const MultiPermissionGate: React.FC<MultiPermissionGateProps> = ({
  permissions,
  children,
  fallback = null,
  requireAll = false,
}) => {
  const { hasAnyPermission, hasAllPermissions, isLoading } = usePermissions();

  if (isLoading) {
    return <Skeleton variant="rectangular" height={40} />;
  }

  const hasRequiredPermissions = requireAll
    ? hasAllPermissions(permissions)
    : hasAnyPermission(permissions);

  return hasRequiredPermissions ? <>{children}</> : <>{fallback}</>;
};
