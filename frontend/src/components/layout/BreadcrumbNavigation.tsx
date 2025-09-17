import React, { useMemo } from 'react';
import { Breadcrumbs, Link, Typography, Box } from '@mui/material';
import { useTheme } from '@mui/material/styles';
import { NavigateNext as NavigateNextIcon, Home as HomeIcon } from '@mui/icons-material';
import { useLocation, useNavigate } from 'react-router-dom';
import { navigationConfig } from '@/config/navigation.config';
import { createNavigationPathMap, formatPathSegment } from '@/utils/navigationHelpers';

interface BreadcrumbItem {
  label: string;
  path: string;
  icon?: React.ReactNode;
}

/**
 * BreadcrumbNavigation component displays the current navigation path.
 * Optimized with O(1) path lookups using a pre-computed path map.
 */
export const BreadcrumbNavigation: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const theme = useTheme();

  // Pre-compute path map for O(1) lookups
  const pathMap = useMemo(
    () => createNavigationPathMap(navigationConfig),
    []
  );

  const breadcrumbs = useMemo((): BreadcrumbItem[] => {
    const pathSegments = location.pathname.split('/').filter(Boolean);
    const items: BreadcrumbItem[] = [];

    // Always add home/cockpit as first item
    items.push({
      label: 'Cockpit',
      path: '/cockpit',
      icon: <HomeIcon sx={{ fontSize: 20, mr: 0.5 }} />,
    });

    // Build breadcrumb trail with O(1) lookups
    let currentPath = '';
    pathSegments.forEach((segment) => {
      currentPath += `/${segment}`;

      // O(1) lookup from pre-computed map
      const navEntry = pathMap.get(currentPath);

      if (navEntry) {
        // Add parent if it's a sub-item and not already added
        if (navEntry.parent && !items.some(item => item.path === navEntry.parent!.path)) {
          items.push({
            label: navEntry.parent.label,
            path: navEntry.parent.path,
          });
        }

        items.push({
          label: navEntry.item.label,
          path: currentPath,
        });
      } else {
        // For dynamic paths (like /customers/123), create readable label
        items.push({
          label: formatPathSegment(segment),
          path: currentPath,
        });
      }
    });

    return items;
  }, [location.pathname, pathMap]);

  // Immer anzeigen, auch auf der Startseite (für bessere Sichtbarkeit)
  // if (breadcrumbs.length <= 1) {
  //   return null; // Don't show breadcrumbs on home page
  // }

  return (
    <Box
      sx={{
        px: 3,
        py: 1.5,
        borderBottom: '1px solid',
        borderColor: 'divider',
        backgroundColor: 'background.paper',
      }}
    >
      <Breadcrumbs
        separator={<NavigateNextIcon fontSize="small" sx={{ color: theme.palette.success.main }} />}
        sx={{
          '& .MuiBreadcrumbs-ol': {
            flexWrap: 'nowrap',
            overflowX: 'auto',
            '&::-webkit-scrollbar': {
              height: 4,
            },
            '&::-webkit-scrollbar-thumb': {
              backgroundColor: theme.palette.success.main,
              borderRadius: 2,
            },
          },
        }}
      >
        {breadcrumbs.map((crumb, index) => {
          const isLast = index === breadcrumbs.length - 1;

          if (isLast) {
            return (
              <Typography
                key={crumb.path}
                sx={{
                  display: 'flex',
                  alignItems: 'center',
                  color: theme.palette.primary.main,
                  fontWeight: 500,
                  fontSize: '0.875rem',
                }}
              >
                {crumb.icon}
                {crumb.label}
              </Typography>
            );
          }

          return (
            <Link
              key={crumb.path}
              component="button"
              variant="body2"
              onClick={() => navigate(crumb.path)}
              sx={{
                display: 'flex',
                alignItems: 'center',
                color: 'text.secondary',
                textDecoration: 'none',
                fontSize: '0.875rem',
                '&:hover': {
                  textDecoration: 'underline',
                  color: theme.palette.success.main,
                },
              }}
            >
              {crumb.icon}
              {crumb.label}
            </Link>
          );
        })}
      </Breadcrumbs>
    </Box>
  );
};