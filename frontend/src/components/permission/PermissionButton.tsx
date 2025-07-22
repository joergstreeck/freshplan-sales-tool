import React from 'react';
import { Button, IconButton, Fab } from '@mui/material';
import type { ButtonProps, IconButtonProps, FabProps } from '@mui/material';
import { usePermissions } from '../../contexts/PermissionContext';

/**
 * Permission-aware Button components from FC-009 CLAUDE_TECH.
 * 
 * These buttons automatically hide themselves if the user lacks the required permission.
 */

interface PermissionButtonProps extends ButtonProps {
    /** The permission code required to show this button */
    permission: string;
}

export const PermissionButton: React.FC<PermissionButtonProps> = ({
    permission,
    children,
    ...props
}) => {
    const { hasPermission, isLoading } = usePermissions();

    // Don't show button while loading permissions
    if (isLoading) return null;
    
    // Don't render if user lacks permission
    if (!hasPermission(permission)) return null;

    return <Button {...props}>{children}</Button>;
};

/**
 * Permission-aware IconButton.
 */
interface PermissionIconButtonProps extends IconButtonProps {
    /** The permission code required to show this button */
    permission: string;
}

export const PermissionIconButton: React.FC<PermissionIconButtonProps> = ({
    permission,
    children,
    ...props
}) => {
    const { hasPermission, isLoading } = usePermissions();

    if (isLoading || !hasPermission(permission)) return null;

    return <IconButton {...props}>{children}</IconButton>;
};

/**
 * Permission-aware Floating Action Button.
 */
interface PermissionFabProps extends FabProps {
    /** The permission code required to show this FAB */
    permission: string;
}

export const PermissionFab: React.FC<PermissionFabProps> = ({
    permission,
    children,
    ...props
}) => {
    const { hasPermission, isLoading } = usePermissions();

    if (isLoading || !hasPermission(permission)) return null;

    return <Fab {...props}>{children}</Fab>;
};

/**
 * Multi-permission button - shows if user has ANY of the permissions.
 */
interface MultiPermissionButtonProps extends ButtonProps {
    /** Array of permission codes - user needs ANY of these */
    permissions: string[];
    /** Require ALL permissions instead of ANY */
    requireAll?: boolean;
}

export const MultiPermissionButton: React.FC<MultiPermissionButtonProps> = ({
    permissions,
    requireAll = false,
    children,
    ...props
}) => {
    const { hasAnyPermission, hasAllPermissions, isLoading } = usePermissions();

    if (isLoading) return null;

    const hasRequiredPermissions = requireAll 
        ? hasAllPermissions(permissions)
        : hasAnyPermission(permissions);

    if (!hasRequiredPermissions) return null;

    return <Button {...props}>{children}</Button>;
};