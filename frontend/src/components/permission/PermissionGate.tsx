import React, { ReactNode } from 'react';
import { Skeleton } from '@mui/material';
import { usePermissions } from '../../contexts/PermissionContext';

/**
 * PermissionGate component from FC-009 CLAUDE_TECH recipes.
 *
 * Conditionally renders children based on permission checks.
 * Shows loading skeleton while permissions are being loaded.
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
 * Multiple permission gate - requires ANY of the permissions.
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
