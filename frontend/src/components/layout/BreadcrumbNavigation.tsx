import React, { useMemo } from 'react';
import { Breadcrumbs, Link, Typography, Box, Chip } from '@mui/material';
import { NavigateNext as NavigateNextIcon, Home as HomeIcon } from '@mui/icons-material';
import { useLocation, useNavigate } from 'react-router-dom';
import { navigationConfig } from '@/config/navigation.config';

interface BreadcrumbItem {
  label: string;
  path: string;
  icon?: React.ReactNode;
}

export const BreadcrumbNavigation: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();

  const breadcrumbs = useMemo((): BreadcrumbItem[] => {
    const pathSegments = location.pathname.split('/').filter(Boolean);
    const items: BreadcrumbItem[] = [];

    // Always add home/cockpit as first item
    items.push({
      label: 'Cockpit',
      path: '/cockpit',
      icon: <HomeIcon sx={{ fontSize: 20, mr: 0.5 }} />,
    });

    // Build breadcrumb trail from navigation config
    let currentPath = '';
    pathSegments.forEach((segment, index) => {
      currentPath += `/${segment}`;

      // Find matching navigation item
      const navItem = navigationConfig.find(item =>
        item.path === currentPath ||
        item.subItems?.some(sub => sub.path === currentPath)
      );

      if (navItem) {
        // Check if it's a main item or sub-item
        if (navItem.path === currentPath) {
          items.push({
            label: navItem.label,
            path: currentPath,
          });
        } else {
          // It's a sub-item
          const subItem = navItem.subItems?.find(sub => sub.path === currentPath);
          if (subItem) {
            // Add parent if not already added
            if (!items.some(item => item.path === navItem.path)) {
              items.push({
                label: navItem.label,
                path: navItem.path,
              });
            }
            items.push({
              label: subItem.label,
              path: currentPath,
            });
          }
        }
      } else {
        // For paths not in navigation (like /customers/123), create readable label
        const label = segment
          .split('-')
          .map(word => word.charAt(0).toUpperCase() + word.slice(1))
          .join(' ');

        items.push({
          label: label,
          path: currentPath,
        });
      }
    });

    return items;
  }, [location.pathname]);

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
        separator={<NavigateNextIcon fontSize="small" sx={{ color: '#94C456' }} />}
        sx={{
          '& .MuiBreadcrumbs-ol': {
            flexWrap: 'nowrap',
            overflowX: 'auto',
            '&::-webkit-scrollbar': {
              height: 4,
            },
            '&::-webkit-scrollbar-thumb': {
              backgroundColor: '#94C456',
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
                  color: '#004F7B',
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
                  color: '#94C456',
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