import React from 'react';
import {
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Chip,
  Box,
  Typography,
  TablePagination,
} from '@mui/material';
import { format } from 'date-fns';
import { de } from 'date-fns/locale';
import type { CustomerResponse } from '../../customer/types/customer.types';
import {
  customerTypeLabels,
  customerStatusLabels,
  industryLabels,
  customerStatusColors,
} from '../../customer/types/customer.types';
import type { ColumnConfig } from '../types/filter.types';

interface CustomerTableProps {
  customers: CustomerResponse[];
  onRowClick?: (customer: CustomerResponse) => void;
  highlightNew?: boolean;
  columns?: ColumnConfig[];
}

export function CustomerTable({
  customers,
  onRowClick,
  highlightNew,
  columns,
}: CustomerTableProps) {
  const [page, setPage] = React.useState(0);
  const [rowsPerPage, setRowsPerPage] = React.useState(25);

  // Default columns if none provided
  const defaultColumns: ColumnConfig[] = [
    { id: 'customerNumber', label: 'Kundennr.', visible: true },
    { id: 'companyName', label: 'Firma', visible: true },
    { id: 'customerType', label: 'Typ', visible: true },
    { id: 'industry', label: 'Branche', visible: true },
    { id: 'status', label: 'Status', visible: true },
    { id: 'expectedAnnualVolume', label: 'Jahresumsatz', visible: true },
    { id: 'lastContactDate', label: 'Letzter Kontakt', visible: true },
    { id: 'riskScore', label: 'Risiko', visible: true },
  ];

  const activeColumns = columns || defaultColumns;
  const visibleColumns = activeColumns.filter(col => col.visible);

  const handleChangePage = (event: unknown, newPage: number) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement>) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  // Format currency
  const formatCurrency = (amount?: number) => {
    if (!amount) return '-';
    return new Intl.NumberFormat('de-DE', {
      style: 'currency',
      currency: 'EUR',
    }).format(amount);
  };

  // Format date
  const formatDate = (dateString?: string) => {
    if (!dateString) return '-';
    try {
      return format(new Date(dateString), 'dd.MM.yyyy', { locale: de });
    } catch {
      return '-';
    }
  };

  // Check if customer is new (created within last 24h)
  const isNewCustomer = (customer: CustomerResponse) => {
    if (!highlightNew || !customer.createdAt) return false;
    const createdDate = new Date(customer.createdAt);
    const dayAgo = new Date();
    dayAgo.setDate(dayAgo.getDate() - 1);
    return createdDate > dayAgo;
  };

  const paginatedCustomers = customers.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage);

  return (
    <Paper sx={{ width: '100%', overflow: 'hidden' }}>
      <TableContainer sx={{ maxHeight: 'calc(100vh - 300px)' }}>
        <Table stickyHeader>
          <TableHead>
            <TableRow>
              {visibleColumns.map(column => (
                <TableCell
                  key={column.id}
                  align={column.id === 'expectedAnnualVolume' ? 'right' : 'left'}
                >
                  {column.label}
                </TableCell>
              ))}
            </TableRow>
          </TableHead>
          <TableBody>
            {paginatedCustomers.map(customer => (
              <TableRow
                key={customer.id}
                hover
                onClick={() => onRowClick?.(customer)}
                sx={{
                  cursor: onRowClick ? 'pointer' : 'default',
                  bgcolor: isNewCustomer(customer) ? 'rgba(148, 196, 86, 0.08)' : undefined,
                  '&:hover': {
                    bgcolor: isNewCustomer(customer) ? 'rgba(148, 196, 86, 0.15)' : 'action.hover',
                  },
                }}
              >
                {visibleColumns.map(column => {
                  const renderCellContent = () => {
                    switch (column.id) {
                      case 'customerNumber':
                        return (
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                            {customer.customerNumber}
                            {isNewCustomer(customer) && (
                              <Chip
                                label="NEU"
                                size="small"
                                sx={{
                                  bgcolor: '#94C456',
                                  color: 'white',
                                  fontSize: '0.7rem',
                                  height: 20,
                                }}
                              />
                            )}
                          </Box>
                        );
                      case 'companyName':
                        return (
                          <Box>
                            <Typography variant="body2" fontWeight="medium">
                              {customer.companyName}
                            </Typography>
                            {customer.tradingName && (
                              <Typography variant="caption" color="text.secondary">
                                {customer.tradingName}
                              </Typography>
                            )}
                          </Box>
                        );
                      case 'customerType':
                        return customerTypeLabels[customer.customerType];
                      case 'industry':
                        return customer.industry ? industryLabels[customer.industry] : '-';
                      case 'status':
                        return (
                          <Chip
                            label={customerStatusLabels[customer.status]}
                            size="small"
                            sx={{
                              bgcolor: customerStatusColors[customer.status],
                              color: 'white',
                            }}
                          />
                        );
                      case 'expectedAnnualVolume':
                        return formatCurrency(customer.expectedAnnualVolume);
                      case 'lastContactDate':
                        return formatDate(customer.lastContactDate);
                      case 'contactCount':
                        return (
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                            <Typography variant="body2">{customer.contactCount || 0}</Typography>
                            {customer.contactCount > 0 && (
                              <Typography variant="caption" color="text.secondary">
                                Kontakt{customer.contactCount > 1 ? 'e' : ''}
                              </Typography>
                            )}
                          </Box>
                        );
                      case 'createdAt':
                        return formatDate(customer.createdAt);
                      case 'revenue':
                        return formatCurrency(customer.revenue);
                      case 'location':
                        return customer.city || '-';
                      case 'tags':
                        return customer.tags?.length > 0 ? (
                          <Box sx={{ display: 'flex', gap: 0.5, flexWrap: 'wrap' }}>
                            {customer.tags.slice(0, 2).map((tag, index) => (
                              <Chip key={index} label={tag} size="small" variant="outlined" />
                            ))}
                            {customer.tags.length > 2 && (
                              <Typography variant="caption" color="text.secondary">
                                +{customer.tags.length - 2}
                              </Typography>
                            )}
                          </Box>
                        ) : (
                          '-'
                        );
                      case 'riskScore':
                        return (
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                            <Box
                              sx={{
                                width: 60,
                                height: 8,
                                bgcolor: 'grey.200',
                                borderRadius: 1,
                                overflow: 'hidden',
                              }}
                            >
                              <Box
                                sx={{
                                  width: `${customer.riskScore}%`,
                                  height: '100%',
                                  bgcolor:
                                    customer.riskScore > 70
                                      ? 'error.main'
                                      : customer.riskScore > 40
                                        ? 'warning.main'
                                        : 'success.main',
                                  transition: 'width 0.3s ease',
                                }}
                              />
                            </Box>
                            <Typography variant="caption" color="text.secondary">
                              {customer.riskScore}%
                            </Typography>
                          </Box>
                        );
                      default:
                        return '-';
                    }
                  };

                  return (
                    <TableCell
                      key={column.id}
                      align={column.id === 'expectedAnnualVolume' ? 'right' : 'left'}
                    >
                      {renderCellContent()}
                    </TableCell>
                  );
                })}
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
      <TablePagination
        rowsPerPageOptions={[10, 25, 50, 100]}
        component="div"
        count={customers.length}
        rowsPerPage={rowsPerPage}
        page={page}
        onPageChange={handleChangePage}
        onRowsPerPageChange={handleChangeRowsPerPage}
        labelRowsPerPage="Zeilen pro Seite:"
        labelDisplayedRows={({ from, to, count }) =>
          `${from}–${to} von ${count !== -1 ? count : `mehr als ${to}`}`
        }
      />
    </Paper>
  );
}
