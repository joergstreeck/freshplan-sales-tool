/**
 * Generic DataTable Component
 *
 * Config-driven, reusable table component extracted from CustomerTable.
 * Supports pagination, sorting, row actions, custom rendering.
 * Extracted during Migration M1 (Sprint 2.1.7.7)
 *
 * @module DataTable
 * @since Sprint 2.1.7.7 (Migration M1)
 */

import React from 'react';
import {
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Box,
  Typography,
  TablePagination,
  IconButton,
  CircularProgress,
  TableSortLabel,
} from '@mui/material';
import { Edit as EditIcon, Delete as DeleteIcon } from '@mui/icons-material';
import type { DataTableColumn, DataTableProps } from './DataTableTypes';

/**
 * Generic DataTable Component
 *
 * @example
 * ```tsx
 * <DataTable<Customer>
 *   data={customers}
 *   columns={columns}
 *   getRowId={(row) => row.id}
 *   onRowClick={(row) => navigate(`/customers/${row.id}`)}
 *   pagination
 * />
 * ```
 */
export function DataTable<T>({
  data,
  columns,
  getRowId,
  onRowClick,
  onEdit,
  onDelete,
  showActions = false,
  customActions,
  pagination = false,
  paginationConfig,
  onPaginationChange,
  sortConfig,
  onSortChange,
  highlightNew = false,
  loading = false,
  emptyMessage = 'Keine Daten vorhanden',
  height,
  rowHeight = 53,
}: DataTableProps<T>) {
  // Internal pagination state (uncontrolled)
  const [internalPage, setInternalPage] = React.useState(0);
  const [internalRowsPerPage, setInternalRowsPerPage] = React.useState(25);

  // Use controlled values if provided, otherwise use internal state
  const currentPage = paginationConfig?.page ?? internalPage;
  const currentRowsPerPage = paginationConfig?.rowsPerPage ?? internalRowsPerPage;

  // Filter visible columns
  const visibleColumns = columns.filter(col => col.visible);

  // Sort columns by order property
  const sortedColumns = [...visibleColumns].sort((a, b) => {
    const orderA = a.order ?? 999;
    const orderB = b.order ?? 999;
    return orderA - orderB;
  });

  // Pagination handlers
  const handleChangePage = (event: unknown, newPage: number) => {
    if (onPaginationChange) {
      onPaginationChange(newPage, currentRowsPerPage);
    } else {
      setInternalPage(newPage);
    }
  };

  const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement>) => {
    const newRowsPerPage = parseInt(event.target.value, 10);
    if (onPaginationChange) {
      onPaginationChange(0, newRowsPerPage);
    } else {
      setInternalRowsPerPage(newRowsPerPage);
      setInternalPage(0);
    }
  };

  // Sort handler
  const handleSort = (field: string) => {
    if (!onSortChange) return;

    const newDirection =
      sortConfig?.field === field && sortConfig?.direction === 'asc' ? 'desc' : 'asc';

    onSortChange({ field, direction: newDirection });
  };

  // Paginate data (client-side)
  const paginatedData = pagination
    ? data.slice(currentPage * currentRowsPerPage, currentPage * currentRowsPerPage + currentRowsPerPage)
    : data;

  // Render cell content
  const renderCellContent = (row: T, column: DataTableColumn<T>) => {
    // Use custom render function if provided
    if (column.render) {
      return column.render(row);
    }

    // Otherwise, use field accessor
    if (column.field) {
      const value = row[column.field];
      return value !== null && value !== undefined ? String(value) : '-';
    }

    return '-';
  };

  // Loading state
  if (loading) {
    return (
      <Paper sx={{ width: '100%', overflow: 'hidden' }}>
        <Box
          sx={{
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            minHeight: 400,
          }}
        >
          <CircularProgress />
        </Box>
      </Paper>
    );
  }

  // Empty state
  if (data.length === 0) {
    return (
      <Paper sx={{ width: '100%', overflow: 'hidden' }}>
        <Box
          sx={{
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            minHeight: 400,
          }}
        >
          <Typography variant="body1" color="text.secondary">
            {emptyMessage}
          </Typography>
        </Box>
      </Paper>
    );
  }

  return (
    <Paper sx={{ width: '100%', overflow: 'hidden' }}>
      <TableContainer sx={{ maxHeight: height || 'calc(100vh - 300px)' }}>
        <Table stickyHeader>
          <TableHead>
            <TableRow>
              {sortedColumns.map(column => (
                <TableCell
                  key={column.id}
                  align={column.align || 'left'}
                  sx={{ width: column.width }}
                >
                  {column.renderHeader ? (
                    column.renderHeader()
                  ) : column.sortable ? (
                    <TableSortLabel
                      active={sortConfig?.field === column.field}
                      direction={
                        sortConfig?.field === column.field ? sortConfig.direction : 'asc'
                      }
                      onClick={() => column.field && handleSort(String(column.field))}
                    >
                      {column.label}
                    </TableSortLabel>
                  ) : (
                    column.label
                  )}
                </TableCell>
              ))}
              {showActions && (
                <TableCell align="right" sx={{ width: customActions ? 180 : 120 }}>
                  Aktionen
                </TableCell>
              )}
            </TableRow>
          </TableHead>
          <TableBody>
            {paginatedData.map(row => {
              const rowId = getRowId(row);
              const rowWithCreatedAt = row as T & { createdAt?: string };
              const isNew =
                highlightNew &&
                rowWithCreatedAt.createdAt &&
                new Date(rowWithCreatedAt.createdAt) >
                  new Date(Date.now() - 24 * 60 * 60 * 1000);

              return (
                <TableRow
                  key={rowId}
                  hover
                  onClick={() => onRowClick?.(row)}
                  sx={{
                    cursor: onRowClick ? 'pointer' : 'default',
                    bgcolor: isNew ? 'rgba(148, 196, 86, 0.08)' : undefined,
                    '&:hover': {
                      bgcolor: isNew ? 'rgba(148, 196, 86, 0.15)' : 'action.hover',
                    },
                    height: rowHeight,
                  }}
                >
                  {sortedColumns.map(column => (
                    <TableCell key={column.id} align={column.align || 'left'}>
                      {renderCellContent(row, column)}
                    </TableCell>
                  ))}
                  {showActions && (
                    <TableCell align="right">
                      <Box sx={{ display: 'flex', gap: 0.5, justifyContent: 'flex-end' }}>
                        {customActions && customActions(row)}
                        {onEdit && (
                          <IconButton
                            size="small"
                            onClick={e => {
                              e.stopPropagation();
                              onEdit(row);
                            }}
                            title="Bearbeiten"
                            sx={{ color: 'primary.main' }}
                          >
                            <EditIcon fontSize="small" />
                          </IconButton>
                        )}
                        {onDelete && (
                          <IconButton
                            size="small"
                            onClick={e => {
                              e.stopPropagation();
                              onDelete(row);
                            }}
                            title="Löschen"
                            sx={{ color: 'error.main' }}
                          >
                            <DeleteIcon fontSize="small" />
                          </IconButton>
                        )}
                      </Box>
                    </TableCell>
                  )}
                </TableRow>
              );
            })}
          </TableBody>
        </Table>
      </TableContainer>

      {pagination && (
        <TablePagination
          rowsPerPageOptions={paginationConfig?.rowsPerPageOptions || [10, 25, 50, 100]}
          component="div"
          count={paginationConfig?.totalRows ?? data.length}
          rowsPerPage={currentRowsPerPage}
          page={currentPage}
          onPageChange={handleChangePage}
          onRowsPerPageChange={handleChangeRowsPerPage}
          labelRowsPerPage="Zeilen pro Seite:"
          labelDisplayedRows={({ from, to, count }) =>
            `${from}–${to} von ${count !== -1 ? count : `mehr als ${to}`}`
          }
        />
      )}
    </Paper>
  );
}
