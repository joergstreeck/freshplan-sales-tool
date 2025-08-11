import React, { useState } from 'react';
import {
  Grid,
  Box,
  Paper,
  Typography,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Avatar,
  Chip,
  IconButton,
  TablePagination,
  TextField,
  InputAdornment,
  Tooltip,
} from '@mui/material';
import {
  Search as SearchIcon,
  Visibility as ViewIcon,
  Block as BlockIcon,
  CheckCircle as ActiveIcon,
  Cancel as InactiveIcon,
} from '@mui/icons-material';
import { useQuery } from '@tanstack/react-query';
import { auditApi } from '../services/auditApi';

interface UserActivityPanelProps {
  _dateRange: {
    from: Date;
    to: Date;
  };
}

interface UserActivity {
  userId: string;
  userName: string;
  userRole: string;
  department?: string;
  totalActions: number;
  lastActivity: string;
  riskScore: number;
  status: 'active' | 'inactive' | 'blocked';
  criticalActions: number;
}

export const UserActivityPanel: React.FC<UserActivityPanelProps> = ({ dateRange }) => {
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [searchTerm, setSearchTerm] = useState('');

  // Mock data - würde normalerweise von der API kommen
  const mockUsers: UserActivity[] = [
    {
      userId: '1',
      userName: 'Max Mustermann',
      userRole: 'admin',
      department: 'IT',
      totalActions: 1234,
      lastActivity: new Date().toISOString(),
      riskScore: 15,
      status: 'active',
      criticalActions: 2,
    },
    {
      userId: '2',
      userName: 'Anna Schmidt',
      userRole: 'sales',
      department: 'Vertrieb',
      totalActions: 567,
      lastActivity: new Date(Date.now() - 3600000).toISOString(),
      riskScore: 5,
      status: 'active',
      criticalActions: 0,
    },
    {
      userId: '3',
      userName: 'Peter Weber',
      userRole: 'manager',
      department: 'Management',
      totalActions: 890,
      lastActivity: new Date(Date.now() - 7200000).toISOString(),
      riskScore: 25,
      status: 'active',
      criticalActions: 5,
    },
  ];

  const filteredUsers = mockUsers.filter(
    user =>
      user.userName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      user.userRole.toLowerCase().includes(searchTerm.toLowerCase()) ||
      user.department?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const handleChangePage = (_: unknown, newPage: number) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement>) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  const getRiskColor = (score: number) => {
    if (score <= 10) return 'success';
    if (score <= 50) return 'warning';
    return 'error';
  };

  const getRoleColor = (role: string) => {
    switch (role) {
      case 'admin':
        return 'error';
      case 'manager':
        return 'warning';
      case 'sales':
        return 'primary';
      default:
        return 'default';
    }
  };

  return (
    <Box>
      <Grid container spacing={3}>
        {/* Search and Stats */}
        <Grid size={{ xs: 12 }}>
          <Paper sx={{ p: 3 }}>
            <Grid container spacing={2} alignItems="center">
              <Grid size={{ xs: 12, md: 6 }}>
                <TextField
                  fullWidth
                  placeholder="Benutzer suchen..."
                  value={searchTerm}
                  onChange={e => setSearchTerm(e.target.value)}
                  InputProps={{
                    startAdornment: (
                      <InputAdornment position="start">
                        <SearchIcon />
                      </InputAdornment>
                    ),
                  }}
                  sx={{
                    '& .MuiOutlinedInput-root': {
                      '&:hover fieldset': {
                        borderColor: '#94C456',
                      },
                      '&.Mui-focused fieldset': {
                        borderColor: '#94C456',
                      },
                    },
                  }}
                />
              </Grid>
              <Grid size={{ xs: 12, md: 6 }}>
                <Box sx={{ display: 'flex', gap: 2, justifyContent: 'flex-end' }}>
                  <Box sx={{ textAlign: 'center' }}>
                    <Typography variant="h4" sx={{ fontFamily: 'Antonio, sans-serif' }}>
                      {filteredUsers.length}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      Aktive Benutzer
                    </Typography>
                  </Box>
                  <Box sx={{ textAlign: 'center' }}>
                    <Typography
                      variant="h4"
                      sx={{ fontFamily: 'Antonio, sans-serif', color: '#ff9800' }}
                    >
                      {filteredUsers.filter(u => u.riskScore > 50).length}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      Hohes Risiko
                    </Typography>
                  </Box>
                  <Box sx={{ textAlign: 'center' }}>
                    <Typography
                      variant="h4"
                      sx={{ fontFamily: 'Antonio, sans-serif', color: '#f44336' }}
                    >
                      {filteredUsers.reduce((sum, u) => sum + u.criticalActions, 0)}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      Kritische Aktionen
                    </Typography>
                  </Box>
                </Box>
              </Grid>
            </Grid>
          </Paper>
        </Grid>

        {/* User Table */}
        <Grid size={{ xs: 12 }}>
          <Paper>
            <TableContainer>
              <Table sx={{ minWidth: 650 }}>
                <TableHead>
                  <TableRow sx={{ bgcolor: '#f5f5f5' }}>
                    <TableCell>Benutzer</TableCell>
                    <TableCell>Rolle</TableCell>
                    <TableCell>Abteilung</TableCell>
                    <TableCell align="right">Aktionen</TableCell>
                    <TableCell align="right">Kritisch</TableCell>
                    <TableCell>Risiko</TableCell>
                    <TableCell>Letzte Aktivität</TableCell>
                    <TableCell>Status</TableCell>
                    <TableCell align="center">Aktionen</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {filteredUsers
                    .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
                    .map(user => (
                      <TableRow key={user.userId} sx={{ '&:hover': { bgcolor: 'action.hover' } }}>
                        <TableCell>
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                            <Avatar sx={{ bgcolor: '#004F7B', width: 32, height: 32 }}>
                              {user.userName.charAt(0)}
                            </Avatar>
                            <Typography variant="body2" fontWeight={500}>
                              {user.userName}
                            </Typography>
                          </Box>
                        </TableCell>
                        <TableCell>
                          <Chip
                            label={user.userRole}
                            size="small"
                            color={getRoleColor(user.userRole) as unknown}
                            variant="outlined"
                          />
                        </TableCell>
                        <TableCell>{user.department}</TableCell>
                        <TableCell align="right">
                          <Typography variant="body2" fontWeight={500}>
                            {user.totalActions.toLocaleString('de-DE')}
                          </Typography>
                        </TableCell>
                        <TableCell align="right">
                          {user.criticalActions > 0 && (
                            <Chip
                              label={user.criticalActions}
                              size="small"
                              color="error"
                              sx={{ fontWeight: 'bold' }}
                            />
                          )}
                        </TableCell>
                        <TableCell>
                          <Chip
                            label={`${user.riskScore}%`}
                            size="small"
                            color={getRiskColor(user.riskScore) as unknown}
                          />
                        </TableCell>
                        <TableCell>
                          <Typography variant="caption" color="text.secondary">
                            {new Date(user.lastActivity).toLocaleString('de-DE', {
                              day: '2-digit',
                              month: '2-digit',
                              hour: '2-digit',
                              minute: '2-digit',
                            })}
                          </Typography>
                        </TableCell>
                        <TableCell>
                          {user.status === 'active' ? (
                            <Chip
                              icon={<ActiveIcon />}
                              label="Aktiv"
                              size="small"
                              color="success"
                              variant="outlined"
                            />
                          ) : user.status === 'blocked' ? (
                            <Chip
                              icon={<InactiveIcon />}
                              label="Blockiert"
                              size="small"
                              color="error"
                              variant="outlined"
                            />
                          ) : (
                            <Chip label="Inaktiv" size="small" variant="outlined" />
                          )}
                        </TableCell>
                        <TableCell align="center">
                          <Box>
                            <Tooltip title="Details anzeigen">
                              <IconButton size="small" sx={{ color: '#004F7B' }}>
                                <ViewIcon fontSize="small" />
                              </IconButton>
                            </Tooltip>
                            {user.status === 'active' && (
                              <Tooltip title="Benutzer blockieren">
                                <IconButton size="small" sx={{ color: '#f44336' }}>
                                  <BlockIcon fontSize="small" />
                                </IconButton>
                              </Tooltip>
                            )}
                          </Box>
                        </TableCell>
                      </TableRow>
                    ))}

                  {filteredUsers.length === 0 && (
                    <TableRow>
                      <TableCell colSpan={9} align="center" sx={{ py: 5 }}>
                        <Typography color="text.secondary">Keine Benutzer gefunden</Typography>
                      </TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </TableContainer>

            <TablePagination
              component="div"
              count={filteredUsers.length}
              page={page}
              onPageChange={handleChangePage}
              rowsPerPage={rowsPerPage}
              onRowsPerPageChange={handleChangeRowsPerPage}
              labelRowsPerPage="Einträge pro Seite:"
              labelDisplayedRows={({ from, to, count }) => `${from}-${to} von ${count}`}
            />
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
};
