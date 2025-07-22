import React from 'react';
import {
    Box,
    Typography,
    Grid,
    Card,
    CardContent,
    Chip,
    List,
    ListItem,
    ListItemText,
    ListItemIcon,
    Divider,
    Alert,
    CircularProgress
} from '@mui/material';
import {
    Security as SecurityIcon,
    Person as PersonIcon,
    AdminPanelSettings as AdminIcon,
    Visibility as ViewIcon,
    Edit as EditIcon,
    Delete as DeleteIcon,
    Add as AddIcon
} from '@mui/icons-material';

import { usePermissions, usePermission } from '../contexts/PermissionContext';
import { useAuth } from '../contexts/AuthContext';
import { PermissionGate, MultiPermissionGate } from '../components/permission/PermissionGate';
import { PermissionButton, PermissionIconButton, MultiPermissionButton } from '../components/permission/PermissionButton';

/**
 * Demo page showcasing FC-009 Advanced Permissions System
 */
export const PermissionDemoPage: React.FC = () => {
    const { user } = useAuth();
    const { permissions, isLoading, refreshPermissions } = usePermissions();

    // Test individual permission hooks
    const customerReadPerm = usePermission('customers:read');
    const adminAccessPerm = usePermission('admin:access');

    // Simulate permission tests
    const testPermissions = [
        'customers:read',
        'customers:write', 
        'customers:delete',
        'users:read',
        'users:write',
        'admin:access',
        'admin:permissions',
        'reports:read',
        'reports:export'
    ];

    if (isLoading) {
        return (
            <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
                <CircularProgress />
                <Typography sx={{ ml: 2 }}>Loading permissions...</Typography>
            </Box>
        );
    }

    return (
        <Box p={3}>
            <Box display="flex" alignItems="center" mb={2}>
                <SecurityIcon sx={{ mr: 1, fontSize: 32 }} />
                <Typography variant="h4">
                    🔒 FC-009 Permission System Demo
                </Typography>
            </Box>
            
            <Alert severity="info" sx={{ mb: 3 }}>
                Diese Seite demonstriert das FC-009 Advanced Permissions System mit RBAC, 
                ressourcenbasierten Berechtigungen und React-Komponenten.
            </Alert>

            <Grid container spacing={3}>
                {/* User Info Card */}
                <Grid item xs={12} md={4}>
                    <Card>
                        <CardContent>
                            <Box display="flex" alignItems="center" mb={2}>
                                <PersonIcon sx={{ mr: 1 }} />
                                <Typography variant="h6">Aktueller Benutzer</Typography>
                            </Box>
                            <Typography variant="body2" color="text.secondary" gutterBottom>
                                Benutzername: {user?.username || 'Unbekannt'}
                            </Typography>
                            <Typography variant="body2" color="text.secondary" gutterBottom>
                                E-Mail: {user?.email || 'Unbekannt'}
                            </Typography>
                            <Typography variant="body2" color="text.secondary">
                                Rollen: {user?.roles?.join(', ') || 'Keine'}
                            </Typography>
                        </CardContent>
                    </Card>
                </Grid>

                {/* Permissions List Card */}
                <Grid item xs={12} md={8}>
                    <Card>
                        <CardContent>
                            <Box display="flex" alignItems="center" justifyContent="space-between" mb={2}>
                                <Box display="flex" alignItems="center">
                                    <SecurityIcon sx={{ mr: 1 }} />
                                    <Typography variant="h6">
                                        Benutzerberechtigungen ({permissions.size})
                                    </Typography>
                                </Box>
                                <PermissionButton
                                    permission="admin:permissions"
                                    variant="outlined"
                                    size="small"
                                    onClick={() => refreshPermissions()}
                                >
                                    Aktualisieren
                                </PermissionButton>
                            </Box>
                            
                            {permissions.size === 0 ? (
                                <Typography color="text.secondary">
                                    Keine Berechtigungen geladen
                                </Typography>
                            ) : (
                                <Box display="flex" flexWrap="wrap" gap={1}>
                                    {Array.from(permissions).map(permission => (
                                        <Chip
                                            key={permission}
                                            label={permission}
                                            size="small"
                                            variant="outlined"
                                            color={permission.includes('admin') ? 'error' : 
                                                   permission.includes('write') || permission.includes('delete') ? 'warning' : 'primary'}
                                        />
                                    ))}
                                </Box>
                            )}
                        </CardContent>
                    </Card>
                </Grid>

                {/* Permission Gates Demo */}
                <Grid item xs={12} md={6}>
                    <Card>
                        <CardContent>
                            <Typography variant="h6" gutterBottom>
                                🚪 Permission Gates Demo
                            </Typography>
                            
                            <List>
                                <PermissionGate permission="customers:read">
                                    <ListItem>
                                        <ListItemIcon><ViewIcon color="primary" /></ListItemIcon>
                                        <ListItemText 
                                            primary="Customer Read Access"
                                            secondary="You can view customers" 
                                        />
                                    </ListItem>
                                </PermissionGate>

                                <PermissionGate 
                                    permission="customers:write"
                                    fallback={
                                        <ListItem>
                                            <ListItemIcon><EditIcon color="disabled" /></ListItemIcon>
                                            <ListItemText 
                                                primary="Kunden-Schreibrechte"
                                                secondary="Zugriff verweigert - unzureichende Berechtigungen"
                                                primaryTypographyProps={{ color: 'text.disabled' }}
                                                secondaryTypographyProps={{ color: 'error.main' }}
                                            />
                                        </ListItem>
                                    }
                                >
                                    <ListItem>
                                        <ListItemIcon><EditIcon color="success" /></ListItemIcon>
                                        <ListItemText 
                                            primary="Kunden-Schreibrechte"
                                            secondary="Sie können Kunden bearbeiten" 
                                        />
                                    </ListItem>
                                </PermissionGate>

                                <MultiPermissionGate 
                                    permissions={['admin:access', 'admin:permissions']}
                                    fallback={
                                        <ListItem>
                                            <ListItemIcon><AdminIcon color="disabled" /></ListItemIcon>
                                            <ListItemText 
                                                primary="Admin-Zugriff"
                                                secondary="Admin-Zugriff verweigert"
                                                primaryTypographyProps={{ color: 'text.disabled' }}
                                                secondaryTypographyProps={{ color: 'error.main' }}
                                            />
                                        </ListItem>
                                    }
                                >
                                    <ListItem>
                                        <ListItemIcon><AdminIcon color="error" /></ListItemIcon>
                                        <ListItemText 
                                            primary="Admin-Zugriff"
                                            secondary="Sie haben Admin-Berechtigungen" 
                                        />
                                    </ListItem>
                                </MultiPermissionGate>
                            </List>
                        </CardContent>
                    </Card>
                </Grid>

                {/* Permission Buttons Demo */}
                <Grid item xs={12} md={6}>
                    <Card>
                        <CardContent>
                            <Typography variant="h6" gutterBottom>
                                🔘 Berechtigungs-Buttons Demo
                            </Typography>
                            
                            <Box display="flex" flexDirection="column" gap={2}>
                                <Box display="flex" gap={1} flexWrap="wrap">
                                    <PermissionButton 
                                        permission="customers:write"
                                        variant="contained"
                                        startIcon={<AddIcon />}
                                    >
                                        Kunde erstellen
                                    </PermissionButton>

                                    <PermissionButton 
                                        permission="customers:delete"
                                        variant="outlined"
                                        color="error"
                                        startIcon={<DeleteIcon />}
                                    >
                                        Kunde löschen
                                    </PermissionButton>

                                    <MultiPermissionButton
                                        permissions={['reports:read', 'reports:export']}
                                        variant="contained"
                                        color="secondary"
                                    >
                                        Bericht exportieren
                                    </MultiPermissionButton>
                                </Box>

                                <Box display="flex" gap={1}>
                                    <PermissionIconButton permission="customers:read" color="primary">
                                        <ViewIcon />
                                    </PermissionIconButton>

                                    <PermissionIconButton permission="customers:write" color="success">
                                        <EditIcon />
                                    </PermissionIconButton>

                                    <PermissionIconButton permission="customers:delete" color="error">
                                        <DeleteIcon />
                                    </PermissionIconButton>
                                </Box>

                                <Alert severity="info" variant="outlined">
                                    Buttons werden automatisch ausgeblendet, wenn dem Benutzer die erforderlichen Berechtigungen fehlen
                                </Alert>
                            </Box>
                        </CardContent>
                    </Card>
                </Grid>

                {/* Permission Test Results */}
                <Grid item xs={12}>
                    <Card>
                        <CardContent>
                            <Typography variant="h6" gutterBottom>
                                🧪 Berechtigungs-Testergebnisse
                            </Typography>
                            
                            <Grid container spacing={1}>
                                {testPermissions.map(permission => (
                                    <Grid item key={permission}>
                                        <PermissionGate
                                            permission={permission}
                                            fallback={
                                                <Chip
                                                    label={permission}
                                                    color="default"
                                                    variant="outlined"
                                                    size="small"
                                                />
                                            }
                                        >
                                            <Chip
                                                label={permission}
                                                color="success"
                                                size="small"
                                            />
                                        </PermissionGate>
                                    </Grid>
                                ))}
                            </Grid>

                            <Divider sx={{ my: 2 }} />

                            <Typography variant="body2" color="text.secondary">
                                Grüne Chips: Berechtigungen erteilt | Graue Chips: Berechtigungen verweigert
                            </Typography>
                        </CardContent>
                    </Card>
                </Grid>

                {/* Hook Usage Demo */}
                <Grid item xs={12}>
                    <Card>
                        <CardContent>
                            <Typography variant="h6" gutterBottom>
                                ⚡ Berechtigungs-Hooks Demo
                            </Typography>
                            
                            <List>
                                <ListItem>
                                    <ListItemText
                                        primary="usePermission('customers:read')"
                                        secondary={`Ergebnis: ${customerReadPerm.hasPermission} | Lade: ${customerReadPerm.isLoading}`}
                                    />
                                </ListItem>
                                <ListItem>
                                    <ListItemText
                                        primary="usePermission('admin:access')"
                                        secondary={`Ergebnis: ${adminAccessPerm.hasPermission} | Lade: ${adminAccessPerm.isLoading}`}
                                    />
                                </ListItem>
                            </List>
                        </CardContent>
                    </Card>
                </Grid>
            </Grid>
        </Box>
    );
};