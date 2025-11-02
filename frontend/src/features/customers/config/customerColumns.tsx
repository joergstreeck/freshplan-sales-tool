/**
 * Customer Table Column Configuration
 *
 * Defines columns for Customer DataTable with custom rendering.
 * Extracted during Migration M2 (Sprint 2.1.7.7)
 *
 * @module customerColumns
 * @since Sprint 2.1.7.7 (Migration M2)
 */

import React from 'react';
import { Box, Typography, Chip, useTheme } from '@mui/material';
import type { CustomerResponse } from '../../customer/types/customer.types';
import {
  customerTypeLabels,
  customerStatusLabels,
  industryLabels,
  getCustomerStatusColor,
  CustomerStatus,
} from '../../customer/types/customer.types';
import { formatCurrency, formatDate } from '../../shared/utils/dataFormatters';
import type { DataTableColumn } from '../../shared/components/data-table';

/**
 * Status Chip Component - avoids React Hook error in render functions
 */
function CustomerStatusChip({ status }: { status: CustomerStatus }) {
  const theme = useTheme();
  const statusColor = getCustomerStatusColor(status, theme);
  return (
    <Chip
      label={customerStatusLabels[status] || status}
      size="small"
      sx={{
        bgcolor: statusColor,
        color: 'white',
      }}
    />
  );
}

/**
 * Get Customer Table Columns
 *
 * @returns DataTableColumn configuration for Customer table
 */
export function getCustomerTableColumns(): DataTableColumn<CustomerResponse>[] {
  return [
    {
      id: 'customerNumber',
      label: 'Kundennr.',
      field: 'customerNumber',
      visible: true,
      order: 0,
      width: 120,
      render: (customer: CustomerResponse) => {
        const isNew =
          customer.createdAt &&
          new Date(customer.createdAt) > new Date(Date.now() - 24 * 60 * 60 * 1000);

        return (
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            {customer.customerNumber}
            {isNew && (
              <Chip
                label="NEU"
                size="small"
                sx={{
                  bgcolor: 'primary.main',
                  color: 'white',
                  fontSize: '0.7rem',
                  height: 20,
                }}
              />
            )}
          </Box>
        );
      },
    },
    {
      id: 'companyName',
      label: 'Firma',
      field: 'companyName',
      visible: true,
      order: 1,
      render: (customer: CustomerResponse) => (
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
      ),
    },
    {
      id: 'customerType',
      label: 'Typ',
      field: 'customerType',
      visible: true,
      order: 2,
      width: 150,
      render: (customer: CustomerResponse) => customerTypeLabels[customer.customerType],
    },
    {
      id: 'industry',
      label: 'Branche',
      field: 'industry',
      visible: true,
      order: 3,
      width: 150,
      render: (customer: CustomerResponse) =>
        customer.industry ? industryLabels[customer.industry] : '-',
    },
    {
      id: 'status',
      label: 'Status',
      field: 'status',
      visible: true,
      order: 4,
      width: 120,
      render: (customer: CustomerResponse) => <CustomerStatusChip status={customer.status} />,
    },
    {
      id: 'expectedAnnualVolume',
      label: 'Jahresumsatz',
      field: 'expectedAnnualVolume',
      visible: true,
      order: 5,
      width: 150,
      align: 'right',
      render: (customer: CustomerResponse) => formatCurrency(customer.expectedAnnualVolume),
    },
    {
      id: 'lastContactDate',
      label: 'Letzter Kontakt',
      field: 'lastContactDate',
      visible: true,
      order: 6,
      width: 150,
      render: (customer: CustomerResponse) => formatDate(customer.lastContactDate),
    },
    {
      id: 'riskScore',
      label: 'Risiko',
      field: 'riskScore',
      visible: true,
      order: 7,
      width: 120,
      render: (customer: CustomerResponse) => (
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
      ),
    },
    {
      id: 'contactCount',
      label: 'Kontakte',
      field: 'contactsCount',
      visible: false,
      order: 8,
      width: 100,
      render: (customer: CustomerResponse) => (
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <Typography variant="body2">{customer.contactsCount || 0}</Typography>
          {customer.contactsCount && customer.contactsCount > 0 && (
            <Typography variant="caption" color="text.secondary">
              Kontakt{customer.contactsCount > 1 ? 'e' : ''}
            </Typography>
          )}
        </Box>
      ),
    },
    {
      id: 'revenue',
      label: 'Umsatz',
      field: 'revenue',
      visible: false,
      order: 9,
      width: 150,
      align: 'right',
      render: (customer: CustomerResponse) => formatCurrency(customer.revenue),
    },
    {
      id: 'location',
      label: 'Standort',
      field: 'city',
      visible: false,
      order: 10,
      width: 150,
      render: (customer: CustomerResponse) => customer.city || '-',
    },
    {
      id: 'createdAt',
      label: 'Erstellt am',
      field: 'createdAt',
      visible: false,
      order: 11,
      width: 150,
      render: (customer: CustomerResponse) => formatDate(customer.createdAt),
    },
  ];
}
