import React, { useState, useCallback as _useCallback } from 'react';
import {
  Box,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Chip,
  IconButton,
  Tooltip,
  Typography,
  CircularProgress,
  Button,
  TablePagination,
} from '@mui/material';
import {
  Visibility as VisibilityIcon,
  Download as DownloadIcon,
  Security as SecurityIcon,
  Warning as WarningIcon,
} from '@mui/icons-material';
import { format } from 'date-fns';
import { de } from 'date-fns/locale';
import type { AuditLog, AuditFilters } from '../types';
import { AuditDetailModal } from './AuditDetailModal';
import { UniversalExportButton } from '@/components/export/UniversalExportButton';

interface AuditTrailTableProps {
  filters?: AuditFilters;
}

export const AuditTrailTable: React.FC<AuditTrailTableProps> = ({ filters }) => {
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(25);
  const [selectedAudit, setSelectedAudit] = useState<string | null>(null);

  const { data, isLoading, error } = useQuery({
    queryKey: ['auditTrail', filters, page, rowsPerPage],
    queryFn: () =>
      auditApi.getAuditLogs({
        ...filters,
        page,
        pageSize: rowsPerPage,
      }),
  });

  const handleChangePage = (_: unknown, newPage: number) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement>) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  const getActionColor = (action: string | undefined) => {
    if (!action) return 'default';
    const upperAction = action.toUpperCase();
    if (upperAction.includes('DELETE') || upperAction.includes('FAILURE') || upperAction.includes('DENIED')) return 'error';
    if (upperAction.includes('CREATE') || upperAction.includes('SUCCESS')) return 'success';
    if (upperAction.includes('UPDATE') || upperAction.includes('CHANGE')) return 'primary';
    if (upperAction.includes('PERMISSION') || upperAction.includes('SECURITY')) return 'warning';
    if (upperAction.includes('LOGIN') || upperAction.includes('LOGOUT')) return 'info';
    return 'default';
  };

  const getEntityTypeIcon = (entityType: string | undefined) => {
    if (!entityType) return '📁';
    switch (entityType.toUpperCase()) {
      case 'USER':
        return '👤';
      case 'CUSTOMER':
        return '🏢';
      case 'OPPORTUNITY':
        return '💼';
      case 'CONTRACT':
        return '📄';
      case 'SYSTEM':
        return '⚙️';
      case 'PERMISSION':
        return '🔐';
      case 'DATA_OPERATION':
      case 'EXPORT':
        return '💾';
      case 'ACTIVITY':
        return '📝';
      case 'CALCULATION':
        return '🧮';
      case 'GENERAL':
      default:
        return '📁';
    }
  };

  const formatDateTime = (dateStr: string) => {
    try {
      return format(new Date(dateStr), 'dd.MM.yyyy HH:mm:ss', { locale: de });
    } catch {
      return dateStr;
    }
  };

  if (isLoading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight={400}>
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    return (
      <Box p={3}>
        <Typography color="error">
          Fehler beim Laden der Audit-Daten: {(error as Error).message}
        </Typography>
      </Box>
    );
  }

  const auditLogs = data || [];

  return (
    <Box>
      <TableContainer component={Paper}>
        <Table sx={{ minWidth: 650 }} size="medium">
          <TableHead>
            <TableRow sx={{ backgroundColor: 'action.hover' }}>
              <TableCell width="180">Zeitstempel</TableCell>
              <TableCell width="150">Benutzer</TableCell>
              <TableCell width="140">Aktion</TableCell>
              <TableCell width="120">Objekt</TableCell>
              <TableCell>Details</TableCell>
              <TableCell width="100">Grund</TableCell>
              <TableCell width="80" align="center">
                Aktionen
              </TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {auditLogs.map(log => (
              <TableRow
                key={log.id}
                hover
                sx={{
                  '&:hover': { backgroundColor: 'action.hover' },
                  ...(log.isCritical && {
                    backgroundColor: 'error.light',
                    '&:hover': { backgroundColor: 'error.light' },
                  }),
                }}
              >
                <TableCell>
                  <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>
                    {formatDateTime(log.occurredAt)}
                  </Typography>
                </TableCell>

                <TableCell>
                  <Box>
                    <Typography variant="body2" fontWeight="medium">
                      {log.userName}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      {log.userRole}
                    </Typography>
                  </Box>
                </TableCell>

                <TableCell>
                  <Chip
                    label={log.eventType || log.action || 'UNKNOWN'}
                    size="small"
                    color={getActionColor(log.eventType || log.action) as unknown}
                    icon={log.isCritical || log.failure ? <WarningIcon /> : undefined}
                  />
                </TableCell>

                <TableCell>
                  <Box display="flex" alignItems="center" gap={0.5}>
                    <Typography variant="body2">{getEntityTypeIcon(log.entityType)}</Typography>
                    <Box>
                      <Typography variant="body2">{log.entityType}</Typography>
                      {log.entityName && (
                        <Typography variant="caption" color="text.secondary">
                          {log.entityName}
                        </Typography>
                      )}
                    </Box>
                  </Box>
                </TableCell>

                <TableCell>
                  {log.changedFields && (
                    <Typography variant="body2" noWrap sx={{ maxWidth: 300 }}>
                      Geändert: {log.changedFields}
                    </Typography>
                  )}
                  {log.comment && (
                    <Typography
                      variant="caption"
                      color="text.secondary"
                      noWrap
                      sx={{ maxWidth: 300 }}
                    >
                      {log.comment}
                    </Typography>
                  )}
                </TableCell>

                <TableCell>
                  {log.reason && (
                    <Tooltip title={log.reason}>
                      <Typography variant="body2" noWrap sx={{ maxWidth: 150 }}>
                        {log.reason}
                      </Typography>
                    </Tooltip>
                  )}
                </TableCell>

                <TableCell align="center">
                  <Tooltip title="Details anzeigen">
                    <IconButton
                      size="small"
                      onClick={() => setSelectedAudit(log.id)}
                      color="primary"
                    >
                      <VisibilityIcon fontSize="small" />
                    </IconButton>
                  </Tooltip>
                  {log.isDsgvoRelevant && (
                    <Tooltip title="DSGVO-relevant">
                      <SecurityIcon fontSize="small" color="warning" />
                    </Tooltip>
                  )}
                </TableCell>
              </TableRow>
            ))}

            {auditLogs.length === 0 && (
              <TableRow>
                <TableCell colSpan={7} align="center" sx={{ py: 5 }}>
                  <Typography color="text.secondary">Keine Audit-Einträge gefunden</Typography>
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>

      <TablePagination
        component="div"
        count={-1} // Unknown total count for now
        page={page}
        onPageChange={handleChangePage}
        rowsPerPage={rowsPerPage}
        onRowsPerPageChange={handleChangeRowsPerPage}
        rowsPerPageOptions={[10, 25, 50, 100]}
        labelRowsPerPage="Einträge pro Seite:"
        labelDisplayedRows={({ from, to }) => `${from}-${to}`}
      />

      {/* Universal Export Button */}
      <Box sx={{ mt: 2, display: 'flex', justifyContent: 'flex-end' }}>
        <UniversalExportButton
          entity="audit"
          queryParams={filters ? {
            ...(filters.dateRange?.from && { from: filters.dateRange.from.toISOString().split('T')[0] }),
            ...(filters.dateRange?.to && { to: filters.dateRange.to.toISOString().split('T')[0] }),
            ...(filters.entityType && { entityType: filters.entityType }),
            ...(filters.entityId && { entityId: filters.entityId }),
            ...(filters.searchText && { searchText: filters.searchText }),
          } : {}}
          buttonLabel="Exportieren"
        />
      </Box>

      {selectedAudit && (
        <AuditDetailModal
          auditId={selectedAudit}
          open={true}
          onClose={() => setSelectedAudit(null)}
        />
      )}
    </Box>
  );
};
