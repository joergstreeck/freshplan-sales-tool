/**
 * ListSkeleton - Generic Loading Skeleton for List Pages
 *
 * Shared component for Customer and Lead list pages.
 * Moved from features/customers/ to features/shared/ for Clean Architecture.
 *
 * @since Sprint 2.1.7.7 - Architecture Cleanup
 */

import { Box, Skeleton, Paper } from '@mui/material';

export function ListSkeleton() {
  return (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      {/* Header Skeleton */}
      <Box
        sx={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          p: 3,
          borderBottom: 1,
          borderColor: 'divider',
          bgcolor: 'background.paper',
        }}
      >
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <Skeleton variant="text" width={150} height={40} />
          <Skeleton variant="circular" width={40} height={24} />
        </Box>
        <Skeleton variant="rectangular" width={150} height={36} />
      </Box>

      {/* Table Skeleton */}
      <Box sx={{ flex: 1, p: 3 }}>
        <Paper sx={{ p: 2 }}>
          {/* Table Header */}
          <Box
            sx={{ display: 'flex', gap: 2, mb: 2, pb: 2, borderBottom: 1, borderColor: 'divider' }}
          >
            {[100, 200, 100, 150, 100, 150, 150, 100].map((width, index) => (
              <Skeleton key={index} variant="text" width={width} height={20} />
            ))}
          </Box>

          {/* Table Rows */}
          {Array.from({ length: 10 }).map((_, index) => (
            <Box key={index} sx={{ display: 'flex', gap: 2, py: 1.5 }}>
              {[100, 200, 100, 150, 100, 150, 150, 100].map((width, idx) => (
                <Skeleton key={idx} variant="text" width={width} height={20} />
              ))}
            </Box>
          ))}
        </Paper>

        {/* Pagination Skeleton */}
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mt: 2 }}>
          <Skeleton variant="text" width={200} height={20} />
          <Box sx={{ display: 'flex', gap: 1 }}>
            {Array.from({ length: 5 }).map((_, index) => (
              <Skeleton key={index} variant="rectangular" width={80} height={32} />
            ))}
          </Box>
        </Box>
      </Box>
    </Box>
  );
}
