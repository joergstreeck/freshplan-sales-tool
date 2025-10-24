import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
  Button,
  TextField,
  InputAdornment,
  Chip,
  IconButton,
  Tooltip,
  Alert,
  CircularProgress,
  Stack,
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import ClearIcon from '@mui/icons-material/Clear';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import PersonAddIcon from '@mui/icons-material/PersonAdd';
import BlockIcon from '@mui/icons-material/Block';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import SyncIcon from '@mui/icons-material/Sync';
import { useUsers, useDeleteUser, useToggleUserStatus } from './userQueries';
import { useUserStore } from './userStore';
import type { User, UserFilter } from './userSchemas';

export const UserTableMUI = () => {
  const navigate = useNavigate();
  const [searchInput, setSearchInput] = useState('');
  const { activeFilters, setActiveFilters, openEditModal, openCreateModal } = useUserStore();

  // Merge search term into filters
  const filters: UserFilter = {
    ...activeFilters,
    search: searchInput.trim() || undefined,
  };

  const { data: users = [], isLoading, error } = useUsers(filters);
  const deleteUser = useDeleteUser();
  const toggleUserStatus = useToggleUserStatus();

  const handleSearch = () => {
    setActiveFilters({
      ...activeFilters,
      search: searchInput.trim() || undefined,
    });
  };

  const handleClearSearch = () => {
    setSearchInput('');
    setActiveFilters({
      ...activeFilters,
      search: undefined,
    });
  };

  const handleDeleteUser = async (user: User) => {
    if (window.confirm(`Benutzer "${user.username}" wirklich löschen?`)) {
      try {
        await deleteUser.mutateAsync(user.id);
      } catch (_error) {
        void _error;
      }
    }
  };

  const handleToggleStatus = async (user: User) => {
    try {
      await toggleUserStatus.mutateAsync({
        id: user.id,
        enabled: !user.enabled,
      });
    } catch (_error) {
      void _error;
    }
  };

  const roleColors: Record<string, 'primary' | 'secondary' | 'success' | 'warning'> = {
    admin: 'warning',
    manager: 'primary',
    sales: 'success',
    user: 'secondary',
  };

  const roleLabels: Record<string, string> = {
    admin: 'Admin',
    manager: 'Manager',
    sales: 'Vertrieb',
    user: 'Benutzer',
  };

  if (error) {
    return (
      <Alert severity="error">
        Fehler beim Laden der Benutzer:{' '}
        {error instanceof Error ? error.message : 'Unbekannter Fehler'}
      </Alert>
    );
  }

  return (
    <Box>
      {/* Header Section */}
      <Paper sx={{ p: 3, mb: 3 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
          <Box>
            <Typography
              variant="h4"
              sx={{ fontFamily: theme => theme.typography.h4.fontFamily, mb: 0.5 }}
            >
              Benutzerverwaltung
            </Typography>
            <Typography variant="body2" color="text.secondary">
              {users.length} {users.length === 1 ? 'Benutzer' : 'Benutzer'} im System
            </Typography>
          </Box>
          <Stack direction="row" spacing={2}>
            <Button
              variant="outlined"
              startIcon={<SyncIcon />}
              onClick={() => navigate('/admin/users/sales-rep-mapping')}
              sx={{
                borderColor: 'secondary.main',
                color: 'secondary.main',
                '&:hover': {
                  borderColor: 'secondary.dark',
                  backgroundColor: 'secondary.light',
                },
              }}
            >
              Xentral Sales-Rep Mapping
            </Button>
            <Button
              variant="contained"
              startIcon={<PersonAddIcon />}
              onClick={openCreateModal}
              sx={{
                backgroundColor: 'primary.main',
                color: 'white',
                '&:hover': {
                backgroundColor: 'primary.dark',
              },
            }}
          >
            Neuer Benutzer
          </Button>
          </Stack>
        </Box>

        {/* Search Bar */}
        <Box sx={{ display: 'flex', gap: 1, maxWidth: 500 }}>
          <TextField
            fullWidth
            size="small"
            placeholder="Benutzer suchen (Name, E-Mail, Benutzername)..."
            value={searchInput}
            onChange={e => setSearchInput(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && handleSearch()}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon />
                </InputAdornment>
              ),
            }}
          />
          <Button
            variant="outlined"
            onClick={handleSearch}
            sx={{
              borderColor: 'secondary.main',
              color: 'secondary.main',
              '&:hover': {
                borderColor: 'secondary.dark',
                backgroundColor: 'rgba(0, 79, 123, 0.04)',
              },
            }}
          >
            Suchen
          </Button>
          {(searchInput || Object.keys(activeFilters).length > 0) && (
            <IconButton onClick={handleClearSearch} color="default">
              <ClearIcon />
            </IconButton>
          )}
        </Box>
      </Paper>

      {/* Table Section */}
      <Paper>
        {isLoading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
            <CircularProgress sx={{ color: 'primary.main' }} />
          </Box>
        ) : users.length === 0 ? (
          <Box sx={{ textAlign: 'center', p: 4 }}>
            <Typography color="text.secondary">
              {searchInput || Object.keys(activeFilters).length > 0
                ? 'Keine Benutzer gefunden. Versuchen Sie eine andere Suche.'
                : 'Noch keine Benutzer vorhanden.'}
            </Typography>
          </Box>
        ) : (
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>
                    <Typography variant="subtitle2" fontWeight="bold">
                      Benutzer
                    </Typography>
                  </TableCell>
                  <TableCell>
                    <Typography variant="subtitle2" fontWeight="bold">
                      E-Mail
                    </Typography>
                  </TableCell>
                  <TableCell>
                    <Typography variant="subtitle2" fontWeight="bold">
                      Rollen
                    </Typography>
                  </TableCell>
                  <TableCell align="center">
                    <Typography variant="subtitle2" fontWeight="bold">
                      Status
                    </Typography>
                  </TableCell>
                  <TableCell align="right">
                    <Typography variant="subtitle2" fontWeight="bold">
                      Aktionen
                    </Typography>
                  </TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {users.map(user => (
                  <TableRow
                    key={user.id}
                    sx={{
                      '&:hover': {
                        backgroundColor: 'rgba(0, 0, 0, 0.02)',
                      },
                    }}
                  >
                    <TableCell>
                      <Box>
                        <Typography variant="body1" fontWeight="medium">
                          {user.firstName} {user.lastName}
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                          @{user.username}
                        </Typography>
                      </Box>
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2">{user.email}</Typography>
                    </TableCell>
                    <TableCell>
                      <Stack direction="row" spacing={0.5} flexWrap="wrap" useFlexGap>
                        {user.roles.map(role => (
                          <Chip
                            key={role}
                            label={roleLabels[role] || role}
                            size="small"
                            color={roleColors[role] || 'default'}
                            variant="outlined"
                          />
                        ))}
                      </Stack>
                    </TableCell>
                    <TableCell align="center">
                      {user.enabled ? (
                        <Chip
                          icon={<CheckCircleIcon />}
                          label="Aktiv"
                          size="small"
                          color="success"
                          variant="filled"
                        />
                      ) : (
                        <Chip
                          icon={<BlockIcon />}
                          label="Inaktiv"
                          size="small"
                          color="error"
                          variant="filled"
                        />
                      )}
                    </TableCell>
                    <TableCell align="right">
                      <Stack direction="row" spacing={1} justifyContent="flex-end">
                        <Tooltip title="Bearbeiten">
                          <IconButton
                            size="small"
                            onClick={() => openEditModal(user.id)}
                            sx={{ color: 'secondary.main' }}
                          >
                            <EditIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title={user.enabled ? 'Deaktivieren' : 'Aktivieren'}>
                          <IconButton
                            size="small"
                            onClick={() => handleToggleStatus(user)}
                            disabled={toggleUserStatus.isPending}
                            color={user.enabled ? 'error' : 'success'}
                          >
                            {user.enabled ? (
                              <BlockIcon fontSize="small" />
                            ) : (
                              <CheckCircleIcon fontSize="small" />
                            )}
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="Löschen">
                          <IconButton
                            size="small"
                            onClick={() => handleDeleteUser(user)}
                            disabled={deleteUser.isPending}
                            color="error"
                          >
                            <DeleteIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                      </Stack>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        )}
      </Paper>
    </Box>
  );
};
