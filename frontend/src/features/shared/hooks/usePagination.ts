/**
 * usePagination Hook
 *
 * Generic hook for managing pagination state (client-side or server-side).
 * Extracted during Migration M1 (Sprint 2.1.7.7)
 *
 * @module usePagination
 * @since Sprint 2.1.7.7 (Migration M1)
 */

import { useState, useCallback, useMemo } from 'react';

export interface UsePaginationOptions {
  /** Initial page (0-indexed) */
  initialPage?: number;

  /** Initial rows per page */
  initialRowsPerPage?: number;

  /** Available rows per page options */
  rowsPerPageOptions?: number[];

  /** Total number of rows (for server-side pagination) */
  totalRows?: number;
}

export interface UsePaginationReturn<T> {
  /** Current page (0-indexed) */
  page: number;

  /** Current rows per page */
  rowsPerPage: number;

  /** Total rows */
  totalRows: number;

  /** Available rows per page options */
  rowsPerPageOptions: number[];

  /** Paginated data (client-side only) */
  paginatedData: T[];

  /** Change page handler */
  handlePageChange: (event: unknown, newPage: number) => void;

  /** Change rows per page handler */
  handleRowsPerPageChange: (event: React.ChangeEvent<HTMLInputElement>) => void;

  /** Reset pagination to first page */
  resetPage: () => void;
}

/**
 * usePagination Hook - Client-side
 *
 * @example
 * ```tsx
 * const {
 *   page,
 *   rowsPerPage,
 *   paginatedData,
 *   handlePageChange,
 *   handleRowsPerPageChange,
 * } = usePagination(customers, { initialRowsPerPage: 25 });
 * ```
 */
export function usePagination<T>(
  data: T[],
  options: UsePaginationOptions = {}
): UsePaginationReturn<T> {
  const {
    initialPage = 0,
    initialRowsPerPage = 25,
    rowsPerPageOptions = [10, 25, 50, 100],
    totalRows: providedTotalRows,
  } = options;

  const [page, setPage] = useState(initialPage);
  const [rowsPerPage, setRowsPerPage] = useState(initialRowsPerPage);

  const totalRows = providedTotalRows ?? data.length;

  const handlePageChange = useCallback((event: unknown, newPage: number) => {
    setPage(newPage);
  }, []);

  const handleRowsPerPageChange = useCallback((event: React.ChangeEvent<HTMLInputElement>) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0); // Reset to first page when changing rows per page
  }, []);

  const resetPage = useCallback(() => {
    setPage(0);
  }, []);

  const paginatedData = useMemo(() => {
    const startIndex = page * rowsPerPage;
    const endIndex = startIndex + rowsPerPage;
    return data.slice(startIndex, endIndex);
  }, [data, page, rowsPerPage]);

  return {
    page,
    rowsPerPage,
    totalRows,
    rowsPerPageOptions,
    paginatedData,
    handlePageChange,
    handleRowsPerPageChange,
    resetPage,
  };
}

/**
 * usePaginationServer Hook - Server-side pagination
 *
 * @example
 * ```tsx
 * const {
 *   page,
 *   rowsPerPage,
 *   handlePageChange,
 *   handleRowsPerPageChange,
 * } = usePaginationServer({
 *   totalRows: 1000,
 *   onPageChange: (page, rowsPerPage) => {
 *     fetchCustomers({ page, limit: rowsPerPage });
 *   },
 * });
 * ```
 */
export function usePaginationServer(options: {
  initialPage?: number;
  initialRowsPerPage?: number;
  rowsPerPageOptions?: number[];
  totalRows: number;
  onPageChange: (page: number, rowsPerPage: number) => void;
}) {
  const {
    initialPage = 0,
    initialRowsPerPage = 25,
    rowsPerPageOptions = [10, 25, 50, 100],
    totalRows,
    onPageChange,
  } = options;

  const [page, setPage] = useState(initialPage);
  const [rowsPerPage, setRowsPerPage] = useState(initialRowsPerPage);

  const handlePageChange = useCallback(
    (event: unknown, newPage: number) => {
      setPage(newPage);
      onPageChange(newPage, rowsPerPage);
    },
    [onPageChange, rowsPerPage]
  );

  const handleRowsPerPageChange = useCallback(
    (event: React.ChangeEvent<HTMLInputElement>) => {
      const newRowsPerPage = parseInt(event.target.value, 10);
      setRowsPerPage(newRowsPerPage);
      setPage(0); // Reset to first page
      onPageChange(0, newRowsPerPage);
    },
    [onPageChange]
  );

  const resetPage = useCallback(() => {
    setPage(0);
    onPageChange(0, rowsPerPage);
  }, [onPageChange, rowsPerPage]);

  return {
    page,
    rowsPerPage,
    totalRows,
    rowsPerPageOptions,
    handlePageChange,
    handleRowsPerPageChange,
    resetPage,
  };
}
