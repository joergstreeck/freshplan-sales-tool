import React, { ReactNode } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { Box, Typography, Paper, Button } from '@mui/material';
import { Lock as LockIcon, ArrowBack as ArrowBackIcon } from '@mui/icons-material';
import { usePermissions } from '../../contexts/PermissionContext';
import { useAuth } from '../../contexts/AuthContext';

/**
 * Permission-aware routing components for FC-009.
 * 
 * Protects routes based on required permissions.
 */

interface PermissionRouteProps {
    /** The permission code required to access this route */
    permission: string;
    /** The content to render if permission is granted */
    children: ReactNode;
    /** Optional custom access denied component */
    accessDeniedComponent?: ReactNode;
    /** Redirect to this path if permission denied (instead of showing access denied) */
    redirectTo?: string;
}

export const PermissionRoute: React.FC<PermissionRouteProps> = ({
    permission,
    children,
    accessDeniedComponent,
    redirectTo
}) => {
    const { hasPermission, isLoading } = usePermissions();
    const { isAuthenticated } = useAuth();
    const location = useLocation();

    // Redirect to login if not authenticated
    if (!isAuthenticated) {
        return <Navigate to="/login" state={{ from: location }} replace />;
    }

    // Show loading while checking permissions
    if (isLoading) {
        return (
            <Box display="flex" justifyContent="center" alignItems="center" minHeight="200px">
                <Typography>Loading...</Typography>
            </Box>
        );
    }

    // Check permission
    if (!hasPermission(permission)) {
        if (redirectTo) {
            return <Navigate to={redirectTo} replace />;
        }

        return accessDeniedComponent || <AccessDeniedDefault permission={permission} />;
    }

    return <>{children}</>;
};

/**
 * Multi-permission route - requires ANY of the permissions.
 */
interface MultiPermissionRouteProps {
    /** Array of permission codes - user needs ANY of these */
    permissions: string[];
    /** Require ALL permissions instead of ANY */
    requireAll?: boolean;
    /** The content to render if permission is granted */
    children: ReactNode;
    /** Optional custom access denied component */
    accessDeniedComponent?: ReactNode;
    /** Redirect to this path if permission denied */
    redirectTo?: string;
}

export const MultiPermissionRoute: React.FC<MultiPermissionRouteProps> = ({
    permissions,
    requireAll = false,
    children,
    accessDeniedComponent,
    redirectTo
}) => {
    const { hasAnyPermission, hasAllPermissions, isLoading } = usePermissions();
    const { isAuthenticated } = useAuth();
    const location = useLocation();

    if (!isAuthenticated) {
        return <Navigate to="/login" state={{ from: location }} replace />;
    }

    if (isLoading) {
        return (
            <Box display="flex" justifyContent="center" alignItems="center" minHeight="200px">
                <Typography>Loading...</Typography>
            </Box>
        );
    }

    const hasRequiredPermissions = requireAll 
        ? hasAllPermissions(permissions)
        : hasAnyPermission(permissions);

    if (!hasRequiredPermissions) {
        if (redirectTo) {
            return <Navigate to={redirectTo} replace />;
        }

        return accessDeniedComponent || <AccessDeniedDefault permission={permissions.join(', ')} />;
    }

    return <>{children}</>;
};

/**
 * Default access denied component.
 */
interface AccessDeniedDefaultProps {
    permission: string;
}

const AccessDeniedDefault: React.FC<AccessDeniedDefaultProps> = ({ permission }) => {
    const location = useLocation();

    return (
        <Box
            display="flex"
            justifyContent="center"
            alignItems="center"
            minHeight="calc(100vh - 200px)"
            p={3}
        >
            <Paper
                elevation={3}
                sx={{
                    p: 4,
                    textAlign: 'center',
                    maxWidth: 500,
                    width: '100%'
                }}
            >
                <LockIcon 
                    sx={{ 
                        fontSize: 64, 
                        color: 'error.main', 
                        mb: 2 
                    }} 
                />
                
                <Typography variant="h5" gutterBottom color="error">
                    Zugriff verweigert
                </Typography>
                
                <Typography variant="body1" color="text.secondary" paragraph>
                    Sie haben keine Berechtigung, auf diese Seite zuzugreifen.
                </Typography>
                
                <Typography variant="body2" color="text.secondary" paragraph>
                    Benötigte Berechtigung: <code>{permission}</code>
                </Typography>
                
                <Button
                    variant="contained"
                    startIcon={<ArrowBackIcon />}
                    onClick={() => window.history.back()}
                    sx={{ mt: 2 }}
                >
                    Zurück
                </Button>
            </Paper>
        </Box>
    );
};