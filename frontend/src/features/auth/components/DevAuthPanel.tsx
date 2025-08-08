/**
 * FC-005 Development Auth Panel
 *
 * Provides a development-only authentication panel for testing different user roles.
 * This component should only be rendered when auth bypass is enabled.
 *
 * @see /Users/joergstreeck/freshplan-sales-tool/frontend/src/features/auth/contexts/DevAuthContext.tsx
 * @see /Users/joergstreeck/freshplan-sales-tool/frontend/src/config/featureFlags.ts
 */

import React from 'react';
import {
  Box,
  Paper,
  Typography,
  Button,
  Stack,
  Chip,
  Alert,
  Divider,
  IconButton,
  Collapse,
} from '@mui/material';
import {
  AdminPanelSettings as AdminIcon,
  SupervisorAccount as ManagerIcon,
  Person as SalesIcon,
  Logout as LogoutIcon,
  ExpandMore as ExpandMoreIcon,
  Warning as WarningIcon,
} from '@mui/icons-material';
import { useDevAuth } from '../contexts/DevAuthContext';

/**
 * Props for DevAuthPanel
 */
interface DevAuthPanelProps {
  /** Whether to show the panel in a compact mode */
  compact?: boolean;
}

/**
 * Development Authentication Panel
 *
 * @remarks
 * - Shows current auth status
 * - Allows quick role switching
 * - Displays permissions and roles
 * - Shows sunset date warning
 */
export const DevAuthPanel: React.FC<DevAuthPanelProps> = ({ compact = false }) => {
  const { isAuthenticated, user, login, logout } = useDevAuth();
  const [expanded, setExpanded] = React.useState(!compact);

  const roleIcons = {
    admin: <AdminIcon />,
    manager: <ManagerIcon />,
    sales: <SalesIcon />,
  };

  const handleRoleSwitch = async (role: string) => {
    if (user?.username === role) return;
    await login(role);
  };

  if (compact && !expanded) {
    return (
      <Paper
        elevation={3}
        sx={{
          position: 'fixed',
          bottom: 16,
          right: 16,
          p: 1,
          backgroundColor: 'warning.light',
          cursor: 'pointer',
        }}
        onClick={() => setExpanded(true)}
      >
        <Stack direction="row" spacing={1} alignItems="center">
          <WarningIcon color="warning" />
          <Typography variant="caption">Dev Auth</Typography>
          <ExpandMoreIcon />
        </Stack>
      </Paper>
    );
  }

  return (
    <Paper
      elevation={3}
      sx={{
        position: compact ? 'fixed' : 'relative',
        bottom: compact ? 16 : 'auto',
        right: compact ? 16 : 'auto',
        maxWidth: 400,
        p: 2,
        backgroundColor: 'background.paper',
        border: '2px solid',
        borderColor: 'warning.main',
      }}
    >
      {compact && (
        <IconButton
          size="small"
          onClick={() => setExpanded(false)}
          sx={{ position: 'absolute', top: 4, right: 4 }}
        >
          <ExpandMoreIcon sx={{ transform: 'rotate(180deg)' }} />
        </IconButton>
      )}

      <Stack spacing={2}>
        <Box>
          <Typography variant="h6" color="warning.main" gutterBottom>
            🚧 Development Auth Mode
          </Typography>
          <Alert severity="warning" sx={{ py: 0.5 }}>
            <Typography variant="caption">This auth bypass expires on 2025-08-27</Typography>
          </Alert>
        </Box>

        <Divider />

        {isAuthenticated && user ? (
          <>
            <Box>
              <Typography variant="subtitle2" gutterBottom>
                Current User
              </Typography>
              <Typography variant="body2">
                {user.firstName} {user.lastName}
              </Typography>
              <Typography variant="caption" color="text.secondary">
                {user.email}
              </Typography>
            </Box>

            <Box>
              <Typography variant="subtitle2" gutterBottom>
                Roles
              </Typography>
              <Stack direction="row" spacing={0.5} flexWrap="wrap">
                {user.roles.map(role => (
                  <Chip
                    key={role}
                    label={role}
                    size="small"
                    icon={roleIcons[role as keyof typeof roleIcons]}
                    color="primary"
                    variant="outlined"
                  />
                ))}
              </Stack>
            </Box>

            <Box>
              <Typography variant="subtitle2" gutterBottom>
                Permissions
              </Typography>
              <Typography variant="caption" color="text.secondary">
                {user.permissions.join(', ')}
              </Typography>
            </Box>

            <Button
              fullWidth
              variant="outlined"
              color="error"
              startIcon={<LogoutIcon />}
              onClick={logout}
              size="small"
            >
              Logout
            </Button>
          </>
        ) : (
          <Box>
            <Typography variant="body2" gutterBottom>
              Not authenticated
            </Typography>
          </Box>
        )}

        <Divider />

        <Box>
          <Typography variant="subtitle2" gutterBottom>
            Quick Login As:
          </Typography>
          <Stack spacing={1}>
            <Button
              fullWidth
              variant={user?.username === 'admin' ? 'contained' : 'outlined'}
              startIcon={<AdminIcon />}
              onClick={() => handleRoleSwitch('admin')}
              size="small"
            >
              Admin (All Permissions)
            </Button>
            <Button
              fullWidth
              variant={user?.username === 'manager' ? 'contained' : 'outlined'}
              startIcon={<ManagerIcon />}
              onClick={() => handleRoleSwitch('manager')}
              size="small"
            >
              Manager (Read/Write)
            </Button>
            <Button
              fullWidth
              variant={user?.username === 'sales' ? 'contained' : 'outlined'}
              startIcon={<SalesIcon />}
              onClick={() => handleRoleSwitch('sales')}
              size="small"
            >
              Sales (Limited)
            </Button>
          </Stack>
        </Box>

        <Alert severity="info" sx={{ py: 0.5 }}>
          <Typography variant="caption">Use VITE_AUTH_BYPASS=false to disable</Typography>
        </Alert>
      </Stack>
    </Paper>
  );
};
