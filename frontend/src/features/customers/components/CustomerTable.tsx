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
  IconButton,
  Collapse,
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
import type { Lead } from '../../leads/types';
import { leadStatusLabels, leadStatusColors } from '../../leads/types';
import LeadScoreIndicator from '../../leads/LeadScoreIndicator';
import StopTheClockDialog from '../../leads/StopTheClockDialog';
import LeadActivityTimeline from '../../leads/LeadActivityTimeline';
import { Pause, PlayArrow, Timeline as TimelineIcon } from '@mui/icons-material';

interface CustomerTableProps {
  customers: CustomerResponse[];
  onRowClick?: (customer: CustomerResponse) => void;
  highlightNew?: boolean;
  columns?: ColumnConfig[];
  context?: 'customers' | 'leads'; // Sprint 2.1.6 Phase 4: Context for status labels
}

export function CustomerTable({
  customers,
  onRowClick,
  highlightNew,
  columns,
  context = 'customers',
}: CustomerTableProps) {
  const [page, setPage] = React.useState(0);
  const [rowsPerPage, setRowsPerPage] = React.useState(25);
  const [stopClockDialogOpen, setStopClockDialogOpen] = React.useState(false);
  const [selectedLead, setSelectedLead] = React.useState<Lead | null>(null);
  const [timelineLeadId, setTimelineLeadId] = React.useState<number | null>(null);

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

  // Sprint 2.1.5: Pre-Claim Status Detection (registeredAt === null/undefined)
  // Intersection type for Lead-specific fields
  const isPreClaim = (customer: CustomerResponse & { registeredAt?: string }) => {
    return !customer.registeredAt && customer.createdAt;
  };

  // Sprint 2.1.5: Pre-Claim Badge - Tage bis Ablauf (10 Tage ab createdAt)
  const getPreClaimDaysRemaining = (customer: CustomerResponse) => {
    if (!customer.createdAt) return null;
    const createdDate = new Date(customer.createdAt);
    const now = new Date();
    const daysSinceCreation = Math.floor(
      (now.getTime() - createdDate.getTime()) / (1000 * 60 * 60 * 24)
    );
    const daysRemaining = 10 - daysSinceCreation;
    return daysRemaining > 0 ? daysRemaining : 0;
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
              <React.Fragment key={customer.id}>
                <TableRow
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
                            {/* Sprint 2.1.5: Pre-Claim Badge */}
                            {isPreClaim(customer) &&
                              (() => {
                                const daysRemaining = getPreClaimDaysRemaining(customer);
                                const isUrgent = daysRemaining !== null && daysRemaining < 3;
                                return (
                                  <Chip
                                    label={`⏳ Pre-Claim (${daysRemaining}T)`}
                                    size="small"
                                    sx={{
                                      bgcolor: isUrgent ? '#FF6B6B' : '#FFA726',
                                      color: 'white',
                                      fontSize: '0.7rem',
                                      height: 20,
                                    }}
                                  />
                                );
                              })()}
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
                      case 'businessType':
                        // Sprint 2.1.6: Harmonized businessType field (replaces industry for leads)
                        return (customer as Lead).businessType || '-';
                      case 'leadScore':
                        // Sprint 2.1.6 Phase 4: Lead scoring visualization
                        return (
                          <LeadScoreIndicator
                            score={(customer as Lead).leadScore}
                            size="small"
                            showLabel={false}
                          />
                        );
                      case 'stage': {
                        // Sprint 2.1.6 Phase 4: Lead stage with German labels (DESIGN_SYSTEM.md)
                        const stageLabels: Record<number, string> = {
                          0: 'Vormerkung',
                          1: 'Registrierung',
                          2: 'Qualifizierung',
                        };
                        const stageColors: Record<number, string> = {
                          0: '#2196F3', // Blue
                          1: '#FF9800', // Orange
                          2: '#4CAF50', // Green
                        };
                        const stageValue = (customer as Lead).stage;
                        return (
                          <Chip
                            label={stageLabels[stageValue] || stageValue?.toString()}
                            size="small"
                            sx={{
                              bgcolor: stageColors[stageValue] || '#9E9E9E',
                              color: 'white',
                            }}
                          />
                        );
                      }
                      case 'status': {
                        // Sprint 2.1.6 Phase 4: Context-aware status labels
                        const statusLabels = context === 'leads' ? leadStatusLabels : customerStatusLabels;
                        const statusColors = context === 'leads' ? leadStatusColors : customerStatusColors;
                        const statusValue = customer.status as keyof typeof statusLabels;
                        return (
                          <Chip
                            label={statusLabels[statusValue] || customer.status}
                            size="small"
                            sx={{
                              bgcolor: statusColors[statusValue] || '#9E9E9E',
                              color: 'white',
                            }}
                          />
                        );
                      }
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
                      case 'actions':
                        // Sprint 2.1.6 Phase 4: Lead actions (Stop-the-Clock, Timeline)
                        if (context === 'leads') {
                          const lead = customer as Lead;
                          return (
                            <Box sx={{ display: 'flex', gap: 1, justifyContent: 'flex-end' }}>
                              <IconButton
                                size="small"
                                onClick={(e) => {
                                  e.stopPropagation();
                                  setSelectedLead(lead);
                                  setStopClockDialogOpen(true);
                                }}
                                color={lead.clockStoppedAt ? 'success' : 'warning'}
                                title={lead.clockStoppedAt ? 'Schutzfrist fortsetzen' : 'Schutzfrist pausieren'}
                              >
                                {lead.clockStoppedAt ? <PlayArrow /> : <Pause />}
                              </IconButton>
                              <IconButton
                                size="small"
                                onClick={(e) => {
                                  e.stopPropagation();
                                  setTimelineLeadId(timelineLeadId === Number(lead.id) ? null : Number(lead.id));
                                }}
                                color={timelineLeadId === Number(lead.id) ? 'primary' : 'default'}
                                title="Aktivitäten anzeigen"
                              >
                                <TimelineIcon />
                              </IconButton>
                            </Box>
                          );
                        }
                        return '-';
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

              {/* Sprint 2.1.6 Phase 4: Lead Activity Timeline Expansion Row */}
              {context === 'leads' && timelineLeadId === Number(customer.id) && (
                <TableRow>
                  <TableCell colSpan={visibleColumns.length} sx={{ py: 0, bgcolor: 'grey.50' }}>
                    <Collapse in={timelineLeadId === Number(customer.id)} timeout="auto" unmountOnExit>
                      <Box sx={{ py: 2, px: 3 }}>
                        <LeadActivityTimeline leadId={Number(customer.id)} />
                      </Box>
                    </Collapse>
                  </TableCell>
                </TableRow>
              )}
            </React.Fragment>
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

      {/* Sprint 2.1.6 Phase 4: Stop-the-Clock Dialog (Leads only) */}
      {context === 'leads' && (
        <StopTheClockDialog
          open={stopClockDialogOpen}
          lead={selectedLead}
          onClose={() => setStopClockDialogOpen(false)}
          onSuccess={() => {
            setStopClockDialogOpen(false);
            setSelectedLead(null);
            // Trigger parent refresh would go here
          }}
        />
      )}
    </Paper>
  );
}
