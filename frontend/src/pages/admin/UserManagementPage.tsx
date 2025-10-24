/**
 * UserManagementPage - Benutzerverwaltung mit Xentral Sales-Rep Mapping
 * Sprint 2.1.7.2 D6 - Sales-Rep Mapping Auto-Sync
 *
 * @description Admin-UI für User-Verwaltung mit Xentral Sales-Rep ID Anzeige
 * @since 2025-10-24
 */

import { useState, useEffect } from 'react';
import {
  Box,
  Paper,
  Typography,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Button,
  CircularProgress,
  Alert,
  Chip,
} from '@mui/material';
import {
  People as PeopleIcon,
  Sync as SyncIcon,
  CheckCircle as CheckCircleIcon,
  Cancel as CancelIcon,
} from '@mui/icons-material';
import { MainLayoutV2 } from '@/components/layout/MainLayoutV2';
import { httpClient } from '@/lib/apiClient';
import toast from 'react-hot-toast';

/**
 * User DTO (simplified from backend)
 * Backend: UserResponse.java
 */
interface User {
  id: string;
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  roles: string[];
  xentralSalesRepId?: string;
  enabled: boolean;
}

/**
 * Job Trigger Response
 * Backend: AdminJobsResource.JobTriggerResponse
 */
interface JobTriggerResponse {
  status: 'success' | 'error';
  message: string;
}

export default function UserManagementPage() {
  // State
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [syncing, setSyncing] = useState(false);

  // Load Users
  useEffect(() => {
    loadUsers();
  }, []);

  const loadUsers = async () => {
    setLoading(true);
    try {
      const response = await httpClient.get<User[]>('/api/users');
      setUsers(response.data);
    } catch (error) {
      console.error('Failed to load users:', error);
      toast.error('Fehler beim Laden der Benutzer');
    } finally {
      setLoading(false);
    }
  };

  /**
   * Trigger manual Sales-Rep Sync Job
   * POST /api/admin/jobs/sync-sales-reps
   */
  const handleSyncSalesReps = async () => {
    setSyncing(true);
    try {
      const response = await httpClient.post<JobTriggerResponse>(
        '/api/admin/jobs/sync-sales-reps'
      );

      if (response.data.status === 'success') {
        toast.success('Sales-Rep Sync erfolgreich! Seite wird neu geladen...');

        // Reload users after sync
        setTimeout(() => {
          loadUsers();
        }, 1500);
      } else {
        toast.error(`Sync fehlgeschlagen: ${response.data.message}`);
      }
    } catch (error: any) {
      console.error('Sales-Rep sync failed:', error);
      toast.error(error.response?.data?.message || 'Fehler beim Sync. Siehe Server-Logs.');
    } finally {
      setSyncing(false);
    }
  };

  if (loading) {
    return (
      <MainLayoutV2 maxWidth="lg">
        <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: 400 }}>
          <CircularProgress />
        </Box>
      </MainLayoutV2>
    );
  }

  return (
    <MainLayoutV2 maxWidth="lg">
      <Box>
        {/* Header */}
        <Box sx={{ mb: 4 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }}>
            <Box sx={{ display: 'flex', alignItems: 'center' }}>
              <PeopleIcon sx={{ fontSize: 40, color: 'secondary.main', mr: 2 }} />
              <Typography variant="h4" component="h1">
                Benutzerverwaltung
              </Typography>
            </Box>

            {/* Manual Sync Button */}
            <Button
              variant="contained"
              startIcon={syncing ? <CircularProgress size={20} /> : <SyncIcon />}
              onClick={handleSyncSalesReps}
              disabled={syncing}
            >
              {syncing ? 'Synchronisiere...' : 'Sales-Rep IDs synchronisieren'}
            </Button>
          </Box>

          <Typography variant="body1" color="text.secondary">
            Übersicht aller Benutzer mit Xentral Sales-Rep Zuordnung
          </Typography>
        </Box>

        {/* Info Alert */}
        <Alert severity="info" sx={{ mb: 3 }}>
          <Typography variant="body2">
            <strong>Sales-Rep Sync:</strong> Täglich um 2:00 Uhr werden Xentral Employee IDs automatisch
            per Email-Matching synchronisiert. Sie können den Sync auch manuell anstoßen.
          </Typography>
        </Alert>

        {/* Users Table */}
        <Paper>
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>
                    <strong>Name</strong>
                  </TableCell>
                  <TableCell>
                    <strong>Email</strong>
                  </TableCell>
                  <TableCell>
                    <strong>Rollen</strong>
                  </TableCell>
                  <TableCell>
                    <strong>Xentral Sales-Rep ID</strong>
                  </TableCell>
                  <TableCell>
                    <strong>Status</strong>
                  </TableCell>
                </TableRow>
              </TableHead>

              <TableBody>
                {users.map(user => (
                  <TableRow key={user.id}>
                    {/* Name */}
                    <TableCell>
                      {user.firstName} {user.lastName}
                      <Typography variant="caption" color="text.secondary" sx={{ display: 'block' }}>
                        {user.username}
                      </Typography>
                    </TableCell>

                    {/* Email */}
                    <TableCell>{user.email}</TableCell>

                    {/* Roles */}
                    <TableCell>
                      <Box sx={{ display: 'flex', gap: 0.5, flexWrap: 'wrap' }}>
                        {user.roles.map(role => (
                          <Chip
                            key={role}
                            label={role}
                            size="small"
                            color={role === 'ADMIN' ? 'secondary' : 'default'}
                          />
                        ))}
                      </Box>
                    </TableCell>

                    {/* Xentral Sales-Rep ID */}
                    <TableCell>
                      {user.xentralSalesRepId ? (
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                          <CheckCircleIcon sx={{ color: 'success.main', fontSize: 18 }} />
                          <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>
                            {user.xentralSalesRepId}
                          </Typography>
                        </Box>
                      ) : (
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                          <CancelIcon sx={{ color: 'text.disabled', fontSize: 18 }} />
                          <Typography variant="body2" color="text.secondary">
                            Nicht zugeordnet
                          </Typography>
                        </Box>
                      )}
                    </TableCell>

                    {/* Status */}
                    <TableCell>
                      {user.enabled ? (
                        <Chip label="Aktiv" color="success" size="small" />
                      ) : (
                        <Chip label="Deaktiviert" size="small" />
                      )}
                    </TableCell>
                  </TableRow>
                ))}

                {users.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={5} align="center">
                      <Typography variant="body2" color="text.secondary" sx={{ py: 4 }}>
                        Keine Benutzer vorhanden
                      </Typography>
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </Paper>

        {/* Statistics */}
        <Box sx={{ mt: 3, display: 'flex', gap: 2 }}>
          <Paper sx={{ p: 2, flex: 1 }}>
            <Typography variant="caption" color="text.secondary">
              Gesamt-Benutzer
            </Typography>
            <Typography variant="h5">{users.length}</Typography>
          </Paper>

          <Paper sx={{ p: 2, flex: 1 }}>
            <Typography variant="caption" color="text.secondary">
              Mit Xentral-ID
            </Typography>
            <Typography variant="h5">
              {users.filter(u => u.xentralSalesRepId).length}
            </Typography>
          </Paper>

          <Paper sx={{ p: 2, flex: 1 }}>
            <Typography variant="caption" color="text.secondary">
              Ohne Xentral-ID
            </Typography>
            <Typography variant="h5" sx={{ color: 'warning.main' }}>
              {users.filter(u => !u.xentralSalesRepId).length}
            </Typography>
          </Paper>
        </Box>
      </Box>
    </MainLayoutV2>
  );
}
