import React, { useCallback, useMemo, CSSProperties } from 'react';
import { FixedSizeList as List } from 'react-window';
import InfiniteLoader from 'react-window-infinite-loader';
import {
  Box,
  Table as _Table,
  TableBody as _TableBody,
  TableCell as _TableCell,
  TableContainer as _TableContainer,
  TableHead as _TableHead,
  TableRow as _TableRow,
  Paper,
  Chip,
  IconButton,
  Typography,
  Skeleton,
  useTheme,
} from '@mui/material';
import {
  Edit as EditIcon,
  Visibility as ViewIcon,
  Delete as DeleteIcon,
} from '@mui/icons-material';
import type { Customer } from '../../../types/customer.types';
import type { ColumnConfig } from '../types/filter.types';
import { formatCurrency, formatDate } from '../../../utils/formatters';

interface VirtualizedCustomerTableProps {
  customers: Customer[];
  columns?: ColumnConfig[];
  onRowClick?: (customer: Customer) => void;
  onEdit?: (customer: Customer) => void;
  onDelete?: (customer: Customer) => void;
  loading?: boolean;
  hasMore?: boolean;
  loadMore?: () => void;
  height?: number;
  rowHeight?: number;
}

export function VirtualizedCustomerTable({
  customers,
  columns = [],
  onRowClick,
  onEdit,
  onDelete,
  loading = false,
  hasMore = false,
  loadMore,
  height = 600,
  rowHeight = 72,
}: VirtualizedCustomerTableProps) {
  const theme = useTheme();
  
  // Calculate visible columns
  const visibleColumns = useMemo(() => 
    columns.filter(col => col.visible),
    [columns]
  );

  // Item count for infinite loading
  const itemCount = hasMore ? customers.length + 1 : customers.length;
  
  // Check if item is loaded
  const isItemLoaded = useCallback((index: number) => 
    !hasMore || index < customers.length,
    [hasMore, customers.length]
  );

  // Load more items
  const loadMoreItems = useCallback(() => {
    if (loadMore && !loading) {
      return loadMore();
    }
    return Promise.resolve();
  }, [loadMore, loading]);

  // Render a single row
  const Row = ({ index, style }: { index: number; style: CSSProperties }) => {
    // Loading row
    if (!isItemLoaded(index)) {
      return (
        <div style={style}>
          <Box sx={{ p: 2, display: 'flex', gap: 2, alignItems: 'center' }}>
            <Skeleton variant="circular" width={40} height={40} />
            <Skeleton variant="text" width="30%" />
            <Skeleton variant="text" width="20%" />
            <Skeleton variant="text" width="15%" />
          </Box>
        </div>
      );
    }

    const customer = customers[index];
    if (!customer) return null;

    // Calculate risk color
    const getRiskColor = (score: number | undefined) => {
      if (!score) return 'default';
      if (score >= 70) return 'error';
      if (score >= 40) return 'warning';
      return 'success';
    };

    // Calculate status color
    const getStatusColor = (status: string) => {
      switch (status) {
        case 'ACTIVE': return 'success';
        case 'INACTIVE': return 'default';
        case 'DRAFT': return 'warning';
        default: return 'default';
      }
    };

    return (
      <div 
        style={{
          ...style,
          borderBottom: `1px solid ${theme.palette.divider}`,
          cursor: onRowClick ? 'pointer' : 'default',
        }}
        onClick={() => onRowClick?.(customer)}
      >
        <Box
          sx={{
            display: 'flex',
            alignItems: 'center',
            px: 2,
            py: 1,
            gap: 2,
            height: rowHeight,
            '&:hover': onRowClick ? {
              backgroundColor: theme.palette.action.hover,
            } : undefined,
          }}
        >
          {/* Render visible columns */}
          {visibleColumns.map((column) => {
            const value = customer[column.id as keyof Customer];
            
            // Special rendering for certain columns
            switch (column.id) {
              case 'status':
                return (
                  <Box key={column.id} sx={{ flex: 1, minWidth: 100 }}>
                    <Chip
                      label={value as string}
                      size="small"
                      color={getStatusColor(value as string) as any}
                    />
                  </Box>
                );
                
              case 'riskScore':
                return (
                  <Box key={column.id} sx={{ flex: 1, minWidth: 100 }}>
                    {value !== undefined && (
                      <Chip
                        label={`${value}%`}
                        size="small"
                        color={getRiskColor(value as number) as any}
                        variant="outlined"
                      />
                    )}
                  </Box>
                );
                
              case 'expectedAnnualVolume':
                return (
                  <Box key={column.id} sx={{ flex: 1, minWidth: 120 }}>
                    <Typography variant="body2">
                      {formatCurrency(value as number)}
                    </Typography>
                  </Box>
                );
                
              case 'lastContactDate':
              case 'createdAt':
                return (
                  <Box key={column.id} sx={{ flex: 1, minWidth: 120 }}>
                    <Typography variant="body2" color="text.secondary">
                      {value ? formatDate(value as string) : '-'}
                    </Typography>
                  </Box>
                );
                
              case 'actions':
                return (
                  <Box 
                    key={column.id} 
                    sx={{ flex: 0, minWidth: 120, display: 'flex', gap: 1 }}
                    onClick={(e) => e.stopPropagation()}
                  >
                    {onEdit && (
                      <IconButton size="small" onClick={() => onEdit(customer)}>
                        <EditIcon fontSize="small" />
                      </IconButton>
                    )}
                    <IconButton size="small" onClick={() => onRowClick?.(customer)}>
                      <ViewIcon fontSize="small" />
                    </IconButton>
                    {onDelete && (
                      <IconButton 
                        size="small" 
                        color="error"
                        onClick={() => onDelete(customer)}
                      >
                        <DeleteIcon fontSize="small" />
                      </IconButton>
                    )}
                  </Box>
                );
                
              default:
                return (
                  <Box key={column.id} sx={{ flex: 1, minWidth: 120 }}>
                    <Typography variant="body2" noWrap>
                      {value?.toString() || '-'}
                    </Typography>
                  </Box>
                );
            }
          })}
        </Box>
      </div>
    );
  };

  // Header component (not virtualized)
  const Header = () => (
    <Box
      sx={{
        display: 'flex',
        alignItems: 'center',
        px: 2,
        py: 1.5,
        gap: 2,
        borderBottom: `2px solid ${theme.palette.divider}`,
        backgroundColor: theme.palette.grey[50],
        fontWeight: 600,
      }}
    >
      {visibleColumns.map((column) => (
        <Box
          key={column.id}
          sx={{
            flex: column.id === 'actions' ? 0 : 1,
            minWidth: column.id === 'actions' ? 120 : 
                     column.id === 'expectedAnnualVolume' ? 120 : 
                     column.id === 'lastContactDate' ? 120 : 100,
          }}
        >
          <Typography variant="subtitle2" fontWeight={600}>
            {column.label}
          </Typography>
        </Box>
      ))}
    </Box>
  );

  return (
    <Paper sx={{ height: height + 48, display: 'flex', flexDirection: 'column' }}>
      {/* Fixed Header */}
      <Header />
      
      {/* Virtualized List */}
      <Box sx={{ flex: 1 }}>
        {customers.length > 0 ? (
          <InfiniteLoader
            isItemLoaded={isItemLoaded}
            itemCount={itemCount}
            loadMoreItems={loadMoreItems}
          >
            {({ onItemsRendered, ref }) => (
              <List
                ref={ref}
                height={height}
                itemCount={itemCount}
                itemSize={rowHeight}
                onItemsRendered={onItemsRendered}
                width="100%"
              >
                {Row}
              </List>
            )}
          </InfiniteLoader>
        ) : (
          <Box
            sx={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              height: '100%',
              color: 'text.secondary',
            }}
          >
            <Typography>Keine Kunden gefunden</Typography>
          </Box>
        )}
      </Box>
    </Paper>
  );
}