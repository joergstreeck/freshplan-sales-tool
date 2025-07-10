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
  Stack,
  Card,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Chip,
  IconButton
} from '@mui/material';
import { useFocusListStore } from '../../customer/store/focusListStore';
import { useAuth } from '../../../hooks/useAuth';
import { useCustomerSearch } from '../../customer/hooks/useCustomerSearch';
import { FilterBar } from '../../customer/components/FilterBar';
import { CustomerCard } from '../../customer/components/CustomerCard';
import { TableColumnSettings } from '../../customer/components/TableColumnSettings';
import RefreshIcon from '@mui/icons-material/Refresh';
import PhoneIcon from '@mui/icons-material/Phone';
import EmailIcon from '@mui/icons-material/Email';
import MoreVertIcon from '@mui/icons-material/MoreVert';
import { format } from 'date-fns';
import { de } from 'date-fns/locale';

interface FocusListColumnMUIProps {
  onCustomerSelect?: (customerId: string) => void;
}

export function FocusListColumnMUI({ onCustomerSelect }: FocusListColumnMUIProps = {}) {
  const { 
    searchCriteria, 
    viewMode, 
    selectedCustomerId, 
    setSelectedCustomer,
    visibleTableColumns 
  } = useFocusListStore();
  // useAuth(); // userId wird aktuell nicht genutzt
  
  // Customer Search Query mit den Filtern aus dem Store
  const { 
    data: searchResults, 
    isLoading, 
    isError, 
    error,
    refetch
  } = useCustomerSearch(searchCriteria);

  // Set selected customer in cockpit store when clicked
  const handleCustomerSelect = (customerId: string) => {
    console.log('FocusListColumnMUI - handleCustomerSelect called with:', customerId);
    setSelectedCustomer(customerId);
    // Callback an parent component
    if (onCustomerSelect) {
      console.log('FocusListColumnMUI - calling onCustomerSelect');
      onCustomerSelect(customerId);
    } else {
      console.error('FocusListColumnMUI - onCustomerSelect callback is not defined!');
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
  const renderCellContent = (customer: any, columnId: string) => {
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
        return (
          <Typography variant="body2">
            #{customer.customerNumber}
          </Typography>
        );
      
      case 'status':
        return (
          <Chip 
            label={customer.status === 'AKTIV' ? 'Aktiv' : customer.status}
            size="small"
            color={customer.status === 'AKTIV' ? 'success' : 'default'}
            sx={{ 
              backgroundColor: customer.status === 'AKTIV' ? '#94C456' : undefined,
              color: customer.status === 'AKTIV' ? '#fff' : undefined
            }}
          />
        );
      
      case 'riskScore':
        return (
          <Box sx={{ 
            display: 'flex', 
            alignItems: 'center',
            justifyContent: 'center',
            gap: 0.5
          }}>
            <Box
              sx={{
                width: 8,
                height: 8,
                borderRadius: '50%',
                backgroundColor: 
                  customer.riskScore > 70 ? '#f44336' :
                  customer.riskScore > 40 ? '#ff9800' :
                  '#4caf50'
              }}
            />
            <Typography variant="body2">
              {customer.riskScore}%
            </Typography>
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
          <Typography variant="body2">
            {formatCurrency(customer.expectedAnnualVolume)}
          </Typography>
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
            onClick={(e) => {
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
      {/* Header */}
      <Box sx={{ 
        p: 2, 
        borderBottom: 1, 
        borderColor: 'divider'
      }}>
        <Typography variant="h6" sx={{ color: 'primary.main' }}>
          Arbeitsliste
        </Typography>
      </Box>

      {/* Filter Bar */}
      <Box sx={{ p: 2, borderBottom: 1, borderColor: 'divider' }}>
        <FilterBar />
      </Box>

      {/* Content */}
      <Box sx={{ flex: 1, overflow: 'auto', p: 2 }}>
        {/* Loading State */}
        {isLoading && (
          <Box sx={{ 
            display: 'flex', 
            flexDirection: 'column', 
            alignItems: 'center', 
            justifyContent: 'center',
            height: '100%'
          }}>
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
              <Box sx={{ 
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))',
                gap: 2
              }}>
                {searchResults.content.map(customer => (
                  <CustomerCard 
                    key={customer.id}
                    customer={customer}
                    selected={selectedCustomerId === customer.id}
                    onClick={() => handleCustomerSelect(customer.id)}
                  />
                ))}
              </Box>
            ) : (
              // Tabellenansicht
              <TableContainer>
                <Table size="small" stickyHeader>
                  <TableHead>
                    <TableRow>
                      {visibleTableColumns.map((column) => (
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
                          }
                        }}
                      >
                        {visibleTableColumns.map((column) => (
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

            {/* Pagination Info */}
            {searchResults.totalPages > 1 && (
              <Box sx={{ mt: 3, textAlign: 'center' }}>
                <Typography variant="caption" color="text.secondary">
                  Seite {searchResults.number + 1} von {searchResults.totalPages}
                </Typography>
              </Box>
            )}

            {/* Empty State */}
            {searchResults.totalElements === 0 && (
              <Box sx={{ 
                textAlign: 'center', 
                py: 4 
              }}>
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