/**
 * Fokus-Liste - Spalte 2 des Sales Cockpit (MUI Version)
 *
 * Zeigt die gefilterte und priorisierte Kundenliste
 * Nutzt die FilterBar aus FC-001
 */

import {
  Box,
  Typography,
  CircularProgress,
  Alert,
  AlertTitle,
  Button,
  Card,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Chip,
  IconButton,
} from '@mui/material';
import { useFocusListStore } from '../../customer/store/focusListStore';
import { useCustomerSearch } from '../../customer/hooks/useCustomerSearch';
import { FilterBar } from '../../customer/components/FilterBar';
import { CustomerCard } from '../../customer/components/CustomerCard';
import { SmartSortSelector } from '../../customer/components/SmartSortSelector';
import RefreshIcon from '@mui/icons-material/Refresh';
import MoreVertIcon from '@mui/icons-material/MoreVert';
import ChevronLeftIcon from '@mui/icons-material/ChevronLeft';
import ChevronRightIcon from '@mui/icons-material/ChevronRight';
import { format } from 'date-fns';
import { de } from 'date-fns/locale';
import { FixedSizeList } from 'react-window';
import { LazyComponent as _LazyComponent } from '../../../components/common/LazyComponent';
import React from 'react';

interface FocusListColumnMUIProps {
  onCustomerSelect?: (customerId: string) => void;
}

// Virtual Row Component für react-window
const VirtualRow = React.memo(
  ({ index, style, data }: { index: number; style: React.CSSProperties; data: unknown }) => {
    const { customers, columns, selectedId, onSelect, renderCell } = data;
    const customer = customers[index];

    return (
      <Box
        style={style}
        sx={{
          display: 'flex',
          alignItems: 'center',
          borderBottom: '1px solid rgba(224, 224, 224, 1)',
          cursor: 'pointer',
          backgroundColor: selectedId === customer.id ? 'action.selected' : 'transparent',
          '&:hover': {
            backgroundColor: 'action.hover',
          },
        }}
        onClick={() => onSelect(customer.id)}
      >
        <Table size="small">
          <TableBody>
            <TableRow>
              {columns.map((column: { id: string; align?: string; minWidth?: number }) => (
                <TableCell
                  key={`${customer.id}-${column.id}`}
                  align={column.align || 'left'}
                  style={{ minWidth: column.minWidth }}
                >
                  {renderCell(customer, column.id)}
                </TableCell>
              ))}
            </TableRow>
          </TableBody>
        </Table>
      </Box>
    );
  }
);

VirtualRow.displayName = 'VirtualRow';

export function FocusListColumnMUI({ onCustomerSelect }: FocusListColumnMUIProps = {}) {
  const {
    searchCriteria,
    viewMode,
    selectedCustomerId,
    setSelectedCustomer,
    visibleTableColumns,
    setPage,
    setPageSize,
  } = useFocusListStore();
  // useAuth(); // userId wird aktuell nicht genutzt

  // Customer Search Query mit den Filtern aus dem Store
  const {
    data: searchResults,
    isLoading,
    isError,
    error,
    refetch,
  } = useCustomerSearch(searchCriteria);

  // Set selected customer in cockpit store when clicked
  const handleCustomerSelect = (customerId: string) => {
    setSelectedCustomer(customerId);
    // Callback an parent component
    if (onCustomerSelect) {
      onCustomerSelect(customerId);
    } else { void 0;
    }
  };

  // Helper für formatierte Währungsanzeige
  const formatCurrency = (value: number | null | undefined) => {
    if (!value) return '-';
    return new Intl.NumberFormat('de-DE', {
      style: 'currency',
      currency: 'EUR',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(value);
  };

  // Render Zellinhalt basierend auf Spalten-ID
  const renderCellContent = (
    customer: {
      companyName: string;
      customerNumber: string;
      status?: string;
      lastContact?: string;
      location?: string;
    },
    columnId: string
  ) => {
    switch (columnId) {
      case 'companyName':
        return (
          <Box>
            <Typography variant="body2" fontWeight="medium">
              {customer.companyName}
            </Typography>
            <Typography variant="caption" color="text.secondary">
              #{customer.customerNumber}
            </Typography>
          </Box>
        );

      case 'customerNumber':
        return <Typography variant="body2">#{customer.customerNumber}</Typography>;

      case 'status':
        return (
          <Chip
            label={customer.status === 'AKTIV' ? 'Aktiv' : customer.status}
            size="small"
            color={customer.status === 'AKTIV' ? 'success' : 'default'}
            sx={{
              backgroundColor: customer.status === 'AKTIV' ? '#94C456' : undefined,
              color: customer.status === 'AKTIV' ? '#fff' : undefined,
            }}
          />
        );

      case 'riskScore':
        return (
          <Box
            sx={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: 0.5,
            }}
          >
            <Box
              sx={{
                width: 8,
                height: 8,
                borderRadius: '50%',
                backgroundColor:
                  customer.riskScore > 70
                    ? '#f44336'
                    : customer.riskScore > 40
                      ? '#ff9800'
                      : '#4caf50',
              }}
            />
            <Typography variant="body2">{customer.riskScore}%</Typography>
          </Box>
        );

      case 'industry':
        return (
          <Typography variant="body2" color="text.secondary">
            {customer.industry || '-'}
          </Typography>
        );

      case 'expectedAnnualVolume':
        return (
          <Typography variant="body2">{formatCurrency(customer.expectedAnnualVolume)}</Typography>
        );

      case 'lastContactDate':
        return (
          <Typography variant="body2" color="text.secondary">
            {customer.lastContactDate
              ? format(new Date(customer.lastContactDate), 'dd.MM.yyyy', { locale: de })
              : '-'}
          </Typography>
        );

      case 'assignedTo':
        return (
          <Typography variant="body2" color="text.secondary">
            {customer.assignedTo || '-'}
          </Typography>
        );

      case 'actions':
        return (
          <IconButton
            size="small"
            onClick={e => {
              e.stopPropagation();
              // TODO: Aktionen-Menü öffnen
            }}
          >
            <MoreVertIcon fontSize="small" />
          </IconButton>
        );

      default:
        return <Typography variant="body2">-</Typography>;
    }
  };

  return (
    <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      {/* Kompakter Header - alles in einer Zeile */}
      <Box
        sx={{
          p: 1.5,
          borderBottom: 1,
          borderColor: 'divider',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          gap: 2,
          minHeight: 64,
        }}
      >
        {/* Links: Titel + Sortierung */}
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, flex: 1 }}>
          <Typography variant="h6" sx={{ color: 'primary.main', whiteSpace: 'nowrap' }}>
            Arbeitsliste
          </Typography>
          <SmartSortSelector variant="compact" showDescription={false} />
        </Box>

        {/* Rechts: Refresh Button */}
        <IconButton size="small" onClick={() => refetch()}>
          <RefreshIcon />
        </IconButton>
      </Box>

      {/* Filter Bar - kompakter */}
      <Box sx={{ px: 1.5, py: 1, borderBottom: 1, borderColor: 'divider' }}>
        <FilterBar />
      </Box>

      {/* Content */}
      <Box sx={{ flex: 1, overflow: 'auto', p: 2 }}>
        {/* Loading State */}
        {isLoading && (
          <Box
            sx={{
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              justifyContent: 'center',
              height: '100%',
            }}
          >
            <CircularProgress size={40} />
            <Typography sx={{ mt: 2 }} color="text.secondary">
              Lade Kundendaten...
            </Typography>
          </Box>
        )}

        {/* Error State */}
        {isError && (
          <Alert
            severity="error"
            action={
              <Button
                color="inherit"
                size="small"
                onClick={() => refetch()}
                startIcon={<RefreshIcon />}
              >
                Erneut versuchen
              </Button>
            }
          >
            <AlertTitle>Fehler beim Laden</AlertTitle>
            {error?.message || 'Die Kundendaten konnten nicht geladen werden.'}
          </Alert>
        )}

        {/* Results */}
        {!isLoading && !isError && searchResults && (
          <>
            {/* Results Info */}
            {searchResults.totalElements > 0 && (
              <Box sx={{ mb: 2 }}>
                <Typography variant="body2" color="text.secondary">
                  Zeige {searchResults.content.length} von {searchResults.totalElements} Kunden
                </Typography>
              </Box>
            )}

            {/* Customer List/Grid */}
            {viewMode === 'cards' ? (
              <Box
                sx={{
                  display: 'grid',
                  gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))',
                  gap: 2,
                }}
              >
                {searchResults.content.map(customer => (
                  <CustomerCard
                    key={customer.id}
                    customer={customer}
                    selected={selectedCustomerId === customer.id}
                    onClick={() => handleCustomerSelect(customer.id)}
                  />
                ))}
              </Box>
            ) : // Tabellenansicht mit Virtual Scrolling für große Listen
            searchResults.content.length > 20 ? (
              // Virtual Scrolling für > 20 Einträge
              <Box>
                {/* Table Header */}
                <Table size="small" stickyHeader>
                  <TableHead>
                    <TableRow>
                      {visibleTableColumns.map(column => (
                        <TableCell
                          key={column.id}
                          align={column.align || 'left'}
                          style={{ minWidth: column.minWidth }}
                          sx={{ fontWeight: 600 }}
                        >
                          {column.label}
                        </TableCell>
                      ))}
                    </TableRow>
                  </TableHead>
                </Table>

                {/* Virtual List Body */}
                <FixedSizeList
                  height={400}
                  itemCount={searchResults.content.length}
                  itemSize={52}
                  width="100%"
                  itemData={{
                    customers: searchResults.content,
                    columns: visibleTableColumns,
                    selectedId: selectedCustomerId,
                    onSelect: handleCustomerSelect,
                    renderCell: renderCellContent,
                  }}
                >
                  {VirtualRow}
                </FixedSizeList>
              </Box>
            ) : (
              // Normale Tabelle für <= 20 Einträge
              <TableContainer>
                <Table size="small" stickyHeader>
                  <TableHead>
                    <TableRow>
                      {visibleTableColumns.map(column => (
                        <TableCell
                          key={column.id}
                          align={column.align || 'left'}
                          style={{ minWidth: column.minWidth }}
                          sx={{ fontWeight: 600 }}
                        >
                          {column.label}
                        </TableCell>
                      ))}
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {searchResults.content.map(customer => (
                      <TableRow
                        key={customer.id}
                        hover
                        selected={selectedCustomerId === customer.id}
                        onClick={() => handleCustomerSelect(customer.id)}
                        sx={{
                          cursor: 'pointer',
                          '&.Mui-selected': {
                            backgroundColor: 'action.selected',
                          },
                        }}
                      >
                        {visibleTableColumns.map(column => (
                          <TableCell
                            key={`${customer.id}-${column.id}`}
                            align={column.align || 'left'}
                          >
                            {renderCellContent(customer, column.id)}
                          </TableCell>
                        ))}
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            )}

            {/* Pagination Controls - Responsive Layout */}
            {searchResults.totalPages > 1 && (
              <Box
                sx={{
                  mt: 3,
                  display: 'flex',
                  flexDirection: { xs: 'column', sm: 'column', md: 'row' },
                  justifyContent: 'space-between',
                  alignItems: { xs: 'stretch', sm: 'stretch', md: 'center' },
                  gap: 2,
                }}
              >
                {/* Page Navigation */}
                <Box
                  sx={{ display: 'flex', alignItems: 'center', gap: 1, justifyContent: 'center' }}
                >
                  <IconButton
                    size="small"
                    disabled={searchResults.first}
                    onClick={() => setPage(searchResults.page - 1)}
                    sx={{
                      border: 1,
                      borderColor: 'divider',
                      '&:hover': { borderColor: 'primary.main' },
                    }}
                  >
                    <ChevronLeftIcon />
                  </IconButton>

                  <Typography variant="body2" sx={{ mx: 1, whiteSpace: 'nowrap' }}>
                    {searchResults.page + 1} / {searchResults.totalPages}
                  </Typography>

                  <IconButton
                    size="small"
                    disabled={searchResults.last}
                    onClick={() => setPage(searchResults.page + 1)}
                    sx={{
                      border: 1,
                      borderColor: 'divider',
                      '&:hover': { borderColor: 'primary.main' },
                    }}
                  >
                    <ChevronRightIcon />
                  </IconButton>
                </Box>

                {/* Page Size Selector - Kompakt */}
                <Box
                  sx={{ display: 'flex', alignItems: 'center', gap: 0.5, justifyContent: 'center' }}
                >
                  <Typography variant="caption" color="text.secondary" sx={{ mr: 1 }}>
                    pro Seite:
                  </Typography>
                  {[10, 20, 50].map(size => (
                    <IconButton
                      key={size}
                      size="small"
                      onClick={() => setPageSize(size)}
                      sx={{
                        minWidth: 32,
                        height: 32,
                        fontSize: '0.875rem',
                        color: searchResults.size === size ? 'primary.main' : 'text.secondary',
                        backgroundColor:
                          searchResults.size === size ? 'action.selected' : 'transparent',
                        '&:hover': {
                          backgroundColor:
                            searchResults.size === size ? 'action.selected' : 'action.hover',
                        },
                      }}
                    >
                      {size}
                    </IconButton>
                  ))}
                </Box>
              </Box>
            )}

            {/* Empty State */}
            {searchResults.totalElements === 0 && (
              <Box
                sx={{
                  textAlign: 'center',
                  py: 4,
                }}
              >
                <Typography color="text.secondary" gutterBottom>
                  Keine Kunden gefunden
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Versuchen Sie, Ihre Filterkriterien anzupassen
                </Typography>
              </Box>
            )}
          </>
        )}
      </Box>
    </Card>
  );
}
