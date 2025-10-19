import { useState } from 'react';
import {
  Box,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Button,
  IconButton,
  Chip,
  Typography,
  Stack,
  InputAdornment,
  Alert,
  Skeleton,
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import ClearIcon from '@mui/icons-material/Clear';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import AddIcon from '@mui/icons-material/Add';
import { useUsers, useDeleteUser, useToggleUserStatus } from '../userQueries';
import { useUserStore } from '../userStore';
import type { User, UserFilter } from '../userSchemas';

export const UserTableMUI = () => {
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
      await toggleUserStatus.mutateAsync({ userId: user.id, active: !user.active });
    } catch (_error) {
      void _error;
    }
  };

  const getRoleChipColor = (role: string): 'error' | 'warning' | 'info' | 'success' => {
    switch (role) {
      case 'admin':
        return 'error';
      case 'manager':
        return 'warning';
      case 'sales':
        return 'info';
      default:
        return 'success';
    }
  };

  const getRoleLabel = (role: string): string => {
    switch (role) {
      case 'admin':
        return 'Administrator';
      case 'manager':
        return 'Manager';
      case 'sales':
        return 'Vertrieb';
      default:
        return role;
    }
  };

  if (error) {
    return (
      <Alert severity="error" sx={{ mt: 2 }}>
        Fehler beim Laden der Benutzer: {error.message}
      </Alert>
    );
  }

  return (
    <Paper sx={{ width: '100%', overflow: 'hidden' }}>
      {/* Header */}
      <Box sx={{ p: 3, borderBottom: 1, borderColor: 'divider' }}>
        <Stack direction="row" justifyContent="space-between" alignItems="center" spacing={2}>
          <Typography variant="h6" component="h2">
            Benutzerverwaltung
          </Typography>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={openCreateModal}
            sx={{
              bgcolor: 'primary.main',
              '&:hover': { bgcolor: 'primary.dark' },
            }}
          >
            Neuer Benutzer
          </Button>
        </Stack>

        {/* Search */}
        <Box sx={{ mt: 3 }}>
          <TextField
            fullWidth
            placeholder="Nach Benutzername oder E-Mail suchen..."
            value={searchInput}
            onChange={(e: React.ChangeEvent<HTMLInputElement>) => setSearchInput(e.target.value)}
            onKeyPress={e => e.key === 'Enter' && handleSearch()}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon />
                </InputAdornment>
              ),
              endAdornment: searchInput && (
                <InputAdornment position="end">
                  <IconButton size="small" onClick={handleClearSearch}>
                    <ClearIcon />
                  </IconButton>
                </InputAdornment>
              ),
            }}
            size="small"
          />
        </Box>
      </Box>

      {/* Table */}
      <TableContainer sx={{ maxHeight: 'calc(100vh - 400px)' }}>
        <Table stickyHeader>
          <TableHead>
            <TableRow>
              <TableCell>Benutzername</TableCell>
              <TableCell>Name</TableCell>
              <TableCell>E-Mail</TableCell>
              <TableCell>Rolle</TableCell>
              <TableCell align="center">Status</TableCell>
              <TableCell align="right">Aktionen</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {isLoading ? (
              // Loading skeletons
              [...Array(5)].map((_, index) => (
                <TableRow key={index}>
                  <TableCell>
                    <Skeleton />
                  </TableCell>
                  <TableCell>
                    <Skeleton />
                  </TableCell>
                  <TableCell>
                    <Skeleton />
                  </TableCell>
                  <TableCell>
                    <Skeleton />
                  </TableCell>
                  <TableCell>
                    <Skeleton />
                  </TableCell>
                  <TableCell>
                    <Skeleton />
                  </TableCell>
                </TableRow>
              ))
            ) : users.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} align="center">
                  <Typography color="text.secondary" sx={{ py: 4 }}>
                    Keine Benutzer gefunden
                  </Typography>
                </TableCell>
              </TableRow>
            ) : (
              users.map(user => (
                <TableRow key={user.id} hover>
                  <TableCell>{user.username}</TableCell>
                  <TableCell>
                    {user.firstName} {user.lastName}
                  </TableCell>
                  <TableCell>{user.email}</TableCell>
                  <TableCell>
                    {user.roles.map(role => (
                      <Chip
                        key={role}
                        label={getRoleLabel(role)}
                        size="small"
                        color={getRoleChipColor(role)}
                        sx={{ mr: 0.5 }}
                      />
                    ))}
                  </TableCell>
                  <TableCell align="center">
                    <Chip
                      label={user.active ? 'Aktiv' : 'Inaktiv'}
                      size="small"
                      color={user.active ? 'success' : 'default'}
                      onClick={() => handleToggleStatus(user)}
                      sx={{ cursor: 'pointer' }}
                    />
                  </TableCell>
                  <TableCell align="right">
                    <IconButton size="small" onClick={() => openEditModal(user.id)} color="primary">
                      <EditIcon />
                    </IconButton>
                    <IconButton
                      size="small"
                      onClick={() => handleDeleteUser(user)}
                      color="error"
                      disabled={user.username === 'admin'}
                    >
                      <DeleteIcon />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Footer with count */}
      {!isLoading && users.length > 0 && (
        <Box sx={{ p: 2, borderTop: 1, borderColor: 'divider' }}>
          <Typography variant="body2" color="text.secondary">
            {users.length} Benutzer gefunden
          </Typography>
        </Box>
      )}
    </Paper>
  );
};
