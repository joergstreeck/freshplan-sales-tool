import React, { useState } from 'react';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import {
  Box,
  Drawer,
  AppBar,
  Toolbar,
  Typography,
  IconButton,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Divider,
  useTheme,
  useMediaQuery,
  Badge,
  Avatar,
  Menu,
  MenuItem
} from '@mui/material';
import {
  Menu as MenuIcon,
  Security as SecurityIcon,
  People as PeopleIcon,
  Settings as SettingsIcon,
  Assessment as AssessmentIcon,
  ChevronLeft as ChevronLeftIcon,
  Notifications as NotificationsIcon,
  AccountCircle as AccountIcon,
  Logout as LogoutIcon,
  Dashboard as DashboardIcon
} from '@mui/icons-material';
import { useAuth } from '@/hooks/useAuth';

const drawerWidth = 260;

interface MenuItem {
  text: string;
  icon: React.ReactNode;
  path: string;
  roles: string[];
  badge?: number;
}

const menuItems: MenuItem[] = [
  { 
    text: 'Übersicht', 
    icon: <DashboardIcon />, 
    path: '/admin',
    roles: ['admin', 'auditor']
  },
  { 
    text: 'Audit Dashboard', 
    icon: <SecurityIcon />, 
    path: '/admin/audit',
    roles: ['admin', 'auditor']
  },
  { 
    text: 'Benutzerverwaltung', 
    icon: <PeopleIcon />, 
    path: '/admin/users',
    roles: ['admin']
  },
  { 
    text: 'Reports', 
    icon: <AssessmentIcon />, 
    path: '/admin/reports',
    roles: ['admin', 'auditor', 'manager']
  },
  { 
    text: 'Einstellungen', 
    icon: <SettingsIcon />, 
    path: '/admin/settings',
    roles: ['admin']
  }
];

export const AdminLayout: React.FC = () => {
  const theme = useTheme();
  const location = useLocation();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const [drawerOpen, setDrawerOpen] = useState(!isMobile);
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const navigate = useNavigate();
  const { hasRole, username, email, logout } = useAuth();
  
  // Helper function to check if user has any of the allowed roles
  const hasAnyRole = (roles: string[]) => {
    return roles.length === 0 || roles.some(role => hasRole(role));
  };
  
  const handleDrawerToggle = () => {
    setDrawerOpen(!drawerOpen);
  };
  
  const handleUserMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };
  
  const handleUserMenuClose = () => {
    setAnchorEl(null);
  };
  
  const handleLogout = () => {
    handleUserMenuClose();
    logout();
    navigate('/');
  };
  
  const filteredMenuItems = menuItems.filter(item => 
    hasAnyRole(item.roles)
  );
  
  return (
    <Box sx={{ display: 'flex' }}>
      <AppBar
        position="fixed"
        sx={{
          width: { md: `calc(100% - ${drawerOpen ? drawerWidth : 0}px)` },
          ml: { md: `${drawerOpen ? drawerWidth : 0}px` },
          bgcolor: '#004F7B',
          transition: theme.transitions.create(['margin', 'width'], {
            easing: theme.transitions.easing.sharp,
            duration: theme.transitions.duration.leavingScreen,
          }),
        }}
      >
        <Toolbar>
          <IconButton
            color="inherit"
            aria-label="open drawer"
            onClick={handleDrawerToggle}
            edge="start"
            sx={{ mr: 2 }}
          >
            {drawerOpen ? <ChevronLeftIcon /> : <MenuIcon />}
          </IconButton>
          
          <Typography 
            variant="h6" 
            noWrap 
            sx={{ 
              fontFamily: 'Antonio, sans-serif',
              fontWeight: 'bold',
              letterSpacing: '0.5px',
              flexGrow: 1
            }}
          >
            FreshPlan Admin Center
          </Typography>
          
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            <IconButton color="inherit">
              <Badge badgeContent={4} color="error">
                <NotificationsIcon />
              </Badge>
            </IconButton>
            
            <Box sx={{ display: 'flex', alignItems: 'center' }}>
              <Typography 
                variant="body2" 
                sx={{ 
                  mr: 1, 
                  display: { xs: 'none', sm: 'block' },
                  fontFamily: 'Poppins, sans-serif'
                }}
              >
                {username || email || 'Admin'}
              </Typography>
              <IconButton
                onClick={handleUserMenuOpen}
                size="small"
                sx={{ ml: 1 }}
                color="inherit"
              >
                <Avatar sx={{ width: 32, height: 32, bgcolor: '#94C456' }}>
                  {username?.charAt(0) || email?.charAt(0) || 'A'}
                </Avatar>
              </IconButton>
            </Box>
          </Box>
        </Toolbar>
      </AppBar>
      
      <Menu
        anchorEl={anchorEl}
        open={Boolean(anchorEl)}
        onClose={handleUserMenuClose}
        anchorOrigin={{
          vertical: 'bottom',
          horizontal: 'right',
        }}
        transformOrigin={{
          vertical: 'top',
          horizontal: 'right',
        }}
      >
        <MenuItem onClick={() => { handleUserMenuClose(); navigate('/profile'); }}>
          <ListItemIcon>
            <AccountIcon fontSize="small" />
          </ListItemIcon>
          Profil
        </MenuItem>
        <Divider />
        <MenuItem onClick={handleLogout}>
          <ListItemIcon>
            <LogoutIcon fontSize="small" />
          </ListItemIcon>
          Abmelden
        </MenuItem>
      </Menu>
      
      <Drawer
        sx={{
          width: drawerOpen ? drawerWidth : 0,
          flexShrink: 0,
          '& .MuiDrawer-paper': {
            width: drawerWidth,
            boxSizing: 'border-box',
            bgcolor: '#f8f9fa',
            borderRight: '1px solid #e0e0e0',
            transition: theme.transitions.create('width', {
              easing: theme.transitions.easing.sharp,
              duration: theme.transitions.duration.enteringScreen,
            }),
          },
        }}
        variant={isMobile ? 'temporary' : 'persistent'}
        anchor="left"
        open={drawerOpen}
        onClose={handleDrawerToggle}
      >
        <Toolbar />
        <Box sx={{ overflow: 'auto' }}>
          <List>
            {filteredMenuItems.map((item) => {
              const isActive = location.pathname === item.path;
              return (
                <ListItem key={item.text} disablePadding>
                  <ListItemButton
                    onClick={() => {
                      navigate(item.path);
                      if (isMobile) {
                        setDrawerOpen(false);
                      }
                    }}
                    selected={isActive}
                    sx={{
                      '&:hover': {
                        bgcolor: 'rgba(148, 196, 86, 0.1)',
                      },
                      '&.Mui-selected': {
                        bgcolor: 'rgba(148, 196, 86, 0.15)',
                        borderLeft: '4px solid #94C456',
                        '&:hover': {
                          bgcolor: 'rgba(148, 196, 86, 0.2)',
                        }
                      }
                    }}
                  >
                    <ListItemIcon sx={{ 
                      color: isActive ? '#94C456' : '#004F7B',
                      minWidth: 40
                    }}>
                      {item.badge ? (
                        <Badge badgeContent={item.badge} color="error">
                          {item.icon}
                        </Badge>
                      ) : (
                        item.icon
                      )}
                    </ListItemIcon>
                    <ListItemText 
                      primary={item.text}
                      primaryTypographyProps={{
                        fontFamily: 'Poppins, sans-serif',
                        fontSize: '14px',
                        fontWeight: isActive ? 600 : 500,
                        color: isActive ? '#004F7B' : 'text.primary'
                      }}
                    />
                  </ListItemButton>
                </ListItem>
              );
            })}
          </List>
        </Box>
      </Drawer>
      
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          bgcolor: '#f5f5f5',
          p: 3,
          width: { md: `calc(100% - ${drawerOpen ? drawerWidth : 0}px)` },
          ml: { md: drawerOpen ? `${drawerWidth}px` : 0 },
          transition: theme.transitions.create(['margin', 'width'], {
            easing: theme.transitions.easing.sharp,
            duration: theme.transitions.duration.leavingScreen,
          }),
          minHeight: '100vh',
          mt: 8
        }}
      >
        <Outlet />
      </Box>
    </Box>
  );
};