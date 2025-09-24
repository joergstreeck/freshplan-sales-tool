/**
 * HeaderV2 - Freshfoodz CI-konformer Header
 *
 * Bietet:
 * - User-Menü mit Logout
 * - Globale Suche
 * - Benachrichtigungen (später)
 * - Mobile-optimierte Navigation
 */

import React, { useState } from 'react';
import {
  AppBar,
  Toolbar,
  Typography,
  IconButton,
  Avatar,
  Menu,
  MenuItem,
  Box,
  TextField,
  InputAdornment,
  useMediaQuery,
  useTheme,
  Divider,
  ListItemIcon,
  ListItemText,
  Tooltip,
  Badge,
} from '@mui/material';
import {
  Menu as MenuIcon,
  Search as SearchIcon,
  Clear as ClearIcon,
  Person as PersonIcon,
  Logout as LogoutIcon,
  Settings as SettingsIcon,
  Notifications as NotificationsIcon,
  KeyboardArrowDown as ArrowDownIcon,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '@/hooks/useAuth';
import { useNavigationStore } from '@/store/navigationStore';
import { Logo } from '../common/Logo';

const DRAWER_WIDTH = 320;
const DRAWER_WIDTH_COLLAPSED = 64;

interface HeaderV2Props {
  onMenuClick?: () => void;
  showMenuIcon?: boolean;
}

export const HeaderV2: React.FC<HeaderV2Props> = ({ onMenuClick, showMenuIcon = true }) => {
  const theme = useTheme();
  const navigate = useNavigate();
  const { user, logout } = useAuth();
  const { toggleSidebar, isCollapsed } = useNavigationStore();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));

  // State
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [searchValue, setSearchValue] = useState('');
  const [isSearchFocused, setIsSearchFocused] = useState(false);

  const handleMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
  };

  const handleLogout = async () => {
    handleMenuClose();
    await logout();
    navigate('/login');
  };

  const handleProfileClick = () => {
    handleMenuClose();
    navigate('/profile');
  };

  const handleSettingsClick = () => {
    handleMenuClose();
    navigate('/settings');
  };

  const handleSearchClear = () => {
    setSearchValue('');
  };

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchValue.trim()) {
      // TODO: Implement global search
    }
  };

  // User display name
  const userDisplayName =
    user?.firstName && user?.lastName
      ? `${user.firstName} ${user.lastName}`
      : user?.username || user?.email?.split('@')[0] || 'Gast';

  // User initials for avatar
  const userInitials =
    user?.firstName && user?.lastName
      ? `${user.firstName[0]}${user.lastName[0]}`.toUpperCase()
      : user?.email?.[0]?.toUpperCase() || user?.username?.[0]?.toUpperCase() || '?';

  return (
    <AppBar
      position="fixed"
      sx={{
        backgroundColor: '#FFFFFF',
        color: '#000000',
        borderBottom: `2px solid ${theme.palette.primary.main}`,
        boxShadow: '0 2px 4px rgba(0,0,0,0.08)',
        zIndex: theme.zIndex.appBar,
        left: { xs: 0, md: isCollapsed ? DRAWER_WIDTH_COLLAPSED : DRAWER_WIDTH },
        width: {
          xs: '100%',
          md: `calc(100% - ${isCollapsed ? DRAWER_WIDTH_COLLAPSED : DRAWER_WIDTH}px)`,
        },
      }}
    >
      <Toolbar
        sx={{
          minHeight: 64,
          px: { xs: 2, sm: 3 },
          justifyContent: 'space-between',
        }}
      >
        {/* Left Section - Mobile Menu Only */}
        <Box
          sx={{
            display: 'flex',
            alignItems: 'center',
            flexShrink: 0,
          }}
        >
          {/* Mobile Menu Toggle */}
          {showMenuIcon && isMobile && (
            <IconButton
              edge="start"
              onClick={onMenuClick || toggleSidebar}
              sx={{
                color: '#004F7B',
                display: { md: 'none' },
              }}
            >
              <MenuIcon />
            </IconButton>
          )}

          {/* Spacer on Desktop to keep content right-aligned */}
          {!isMobile && <Box sx={{ width: 1 }} />}
        </Box>

        {/* Right Section - Logo, Search, Notifications, User */}
        <Box
          sx={{
            display: 'flex',
            alignItems: 'center',
            gap: 2,
            flexGrow: 1,
            justifyContent: 'flex-end',
          }}
        >
          {/* Logo - Now in right section */}
          <Box sx={{ mr: 'auto', display: 'flex', alignItems: 'center' }}>
            {/* Desktop: Volles Logo */}
            <Box sx={{ display: { xs: 'none', sm: 'block' } }}>
              <Logo variant="full" height={40} onClick={() => navigate('/cockpit')} />
            </Box>
            {/* Mobile: Icon Logo */}
            <Box sx={{ display: { xs: 'block', sm: 'none' } }}>
              <Logo variant="icon" height={32} onClick={() => navigate('/cockpit')} />
            </Box>
          </Box>

          {/* Search Bar - Desktop */}
          {!isMobile && (
            <Box
              component="form"
              onSubmit={handleSearchSubmit}
              sx={{
                width: '100%',
                maxWidth: 400,
              }}
            >
              <TextField
                fullWidth
                size="small"
                placeholder="Suche nach Kunden, Aufträgen oder Produkten..."
                value={searchValue}
                onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                  setSearchValue(e.target.value)
                }
                onFocus={() => setIsSearchFocused(true)}
                onBlur={() => setIsSearchFocused(false)}
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <SearchIcon sx={{ color: '#94C456' }} />
                    </InputAdornment>
                  ),
                  endAdornment: searchValue && (
                    <InputAdornment position="end">
                      <IconButton size="small" onClick={handleSearchClear} edge="end">
                        <ClearIcon fontSize="small" />
                      </IconButton>
                    </InputAdornment>
                  ),
                  sx: {
                    backgroundColor: isSearchFocused ? '#FAFAFA' : '#F5F5F5',
                    borderRadius: 2,
                    '& .MuiOutlinedInput-notchedOutline': {
                      borderColor: isSearchFocused ? '#94C456' : 'transparent',
                    },
                    '&:hover .MuiOutlinedInput-notchedOutline': {
                      borderColor: '#94C456',
                    },
                    '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
                      borderColor: '#94C456',
                      borderWidth: 2,
                    },
                  },
                }}
              />
            </Box>
          )}

          {/* Notifications */}
          <Tooltip title="Benachrichtigungen">
            <IconButton
              sx={{
                color: '#004F7B',
                flexShrink: 0,
              }}
            >
              <Badge badgeContent={3} color="error">
                <NotificationsIcon />
              </Badge>
            </IconButton>
          </Tooltip>

          {/* User Menu */}
          <Box sx={{ display: 'flex', alignItems: 'center', flexShrink: 0 }}>
            <IconButton
              onClick={handleMenuOpen}
              sx={{
                p: 0.5,
                borderRadius: 2,
                '&:hover': {
                  backgroundColor: 'rgba(148, 196, 86, 0.08)',
                },
              }}
            >
              <Avatar
                sx={{
                  width: 36,
                  height: 36,
                  bgcolor: '#94C456',
                  fontSize: '0.875rem',
                  fontWeight: 600,
                  mr: 1,
                }}
              >
                {userInitials}
              </Avatar>
              {!isMobile && (
                <>
                  <Box sx={{ textAlign: 'left', mx: 1 }}>
                    <Typography
                      variant="body2"
                      sx={{
                        fontWeight: 500,
                        color: '#004F7B',
                        lineHeight: 1.2,
                      }}
                    >
                      {userDisplayName}
                    </Typography>
                    <Typography
                      variant="caption"
                      sx={{
                        color: 'text.secondary',
                        textTransform: 'capitalize',
                      }}
                    >
                      {user?.roles?.[0] || 'Vertrieb'}
                    </Typography>
                  </Box>
                  <ArrowDownIcon sx={{ color: '#004F7B' }} />
                </>
              )}
            </IconButton>

            <Menu
              anchorEl={anchorEl}
              open={Boolean(anchorEl)}
              onClose={handleMenuClose}
              anchorOrigin={{
                vertical: 'bottom',
                horizontal: 'right',
              }}
              transformOrigin={{
                vertical: 'top',
                horizontal: 'right',
              }}
              PaperProps={{
                sx: {
                  mt: 1,
                  minWidth: 200,
                  boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
                },
              }}
            >
              <Box sx={{ px: 2, py: 1.5 }}>
                <Typography variant="body2" fontWeight={500}>
                  {userDisplayName}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  {user?.email || 'user@example.com'}
                </Typography>
              </Box>

              <Divider />

              <MenuItem onClick={handleProfileClick}>
                <ListItemIcon>
                  <PersonIcon fontSize="small" />
                </ListItemIcon>
                <ListItemText>Mein Profil</ListItemText>
              </MenuItem>

              <MenuItem onClick={handleSettingsClick}>
                <ListItemIcon>
                  <SettingsIcon fontSize="small" />
                </ListItemIcon>
                <ListItemText>Einstellungen</ListItemText>
              </MenuItem>

              <Divider />

              <MenuItem onClick={handleLogout}>
                <ListItemIcon>
                  <LogoutIcon fontSize="small" sx={{ color: 'error.main' }} />
                </ListItemIcon>
                <ListItemText>
                  <Typography color="error">Abmelden</Typography>
                </ListItemText>
              </MenuItem>
            </Menu>
          </Box>
        </Box>
      </Toolbar>

      {/* Mobile Search Bar */}
      {isMobile && (
        <Toolbar sx={{ py: 1 }}>
          <Box component="form" onSubmit={handleSearchSubmit} sx={{ width: '100%' }}>
            <TextField
              fullWidth
              size="small"
              placeholder="Suchen..."
              value={searchValue}
              onChange={(e: React.ChangeEvent<HTMLInputElement>) => setSearchValue(e.target.value)}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon sx={{ color: '#94C456' }} />
                  </InputAdornment>
                ),
                endAdornment: searchValue && (
                  <InputAdornment position="end">
                    <IconButton size="small" onClick={handleSearchClear} edge="end">
                      <ClearIcon fontSize="small" />
                    </IconButton>
                  </InputAdornment>
                ),
                sx: {
                  backgroundColor: '#F5F5F5',
                  borderRadius: 2,
                  '& .MuiOutlinedInput-notchedOutline': {
                    borderColor: 'transparent',
                  },
                  '&:hover .MuiOutlinedInput-notchedOutline': {
                    borderColor: '#94C456',
                  },
                  '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
                    borderColor: '#94C456',
                    borderWidth: 2,
                  },
                },
              }}
            />
          </Box>
        </Toolbar>
      )}
    </AppBar>
  );
};

export default HeaderV2;
