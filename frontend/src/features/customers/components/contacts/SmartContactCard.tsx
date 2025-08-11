/**
 * SmartContactCard Component
 * 
 * Enhanced contact card with relationship intelligence, visual warmth indicators,
 * and quick actions for efficient contact management.
 * 
 * Part of FC-005 Contact Management UI - PR 3.
 * 
 * @see /docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/SMART_CONTACT_CARDS.md
 */

import React, { useState } from 'react';
import {
  Card,
  CardContent,
  CardActions,
  Typography,
  Box,
  Chip,
  IconButton,
  Menu,
  MenuItem,
  Avatar,
  Tooltip,
  Stack,
  useTheme,
  alpha,
  Divider,
} from '@mui/material';
import {
  MoreVert as MoreVertIcon,
  Star as StarIcon,
  StarBorder as StarBorderIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  LocationOn as LocationIcon,
  Phone as PhoneIcon,
  Email as EmailIcon,
  WhatsApp as WhatsAppIcon,
  Cake as CakeIcon,
  Business as BusinessIcon,
  Timeline as TimelineIcon,
  PersonAdd as PersonAddIcon,
  Groups as GroupsIcon,
} from '@mui/icons-material';

import type { Contact } from '../../types/contact.types';
import { getContactFullName, DECISION_LEVELS as _DECISION_LEVELS } from '../../types/contact.types';
import { WarmthIndicator, type RelationshipWarmth } from './WarmthIndicator';
import { ContactQuickActions } from './ContactQuickActions';
import { MiniAuditTimeline } from '../../../audit/components/MiniAuditTimeline';
import { useAuth } from '../../../../contexts/AuthContext';
import { LazyComponent } from '../../../../components/common/LazyComponent';

export interface SmartContactCardProps {
  contact: Contact;
  warmth?: RelationshipWarmth;
  onEdit: (contact: Contact) => void;
  onDelete: (contactId: string) => void;
  onSetPrimary: (contactId: string) => void;
  onAssignLocation?: (contactId: string) => void;
  onViewTimeline?: (contactId: string) => void;
  onQuickAction?: (action: string, contactId: string) => void;
  isHovered?: boolean;
  showQuickActions?: boolean;
  showAuditTrail?: boolean;
  customerId?: string;
}

/**
 * Smart Contact Card with enhanced visual feedback and relationship intelligence
 */
export const SmartContactCard: React.FC<SmartContactCardProps> = ({
  contact,
  warmth,
  onEdit,
  onDelete,
  onSetPrimary,
  onAssignLocation,
  onViewTimeline,
  onQuickAction,
  showQuickActions = true,
  showAuditTrail = true,
  customerId: _customerId,
}) => {
  const theme = useTheme();
  const { user } = useAuth();
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [isCardHovered, setIsCardHovered] = useState(false);
  
  // Check if user can view audit trail
  // In development with authBypass, always allow audit viewing
  const isDevelopment = import.meta.env.DEV;
  const canViewAudit = showAuditTrail && (isDevelopment || user?.roles?.some(role => 
    ['admin', 'manager', 'auditor'].includes(role)
  ));

  // Helper function - must be defined before use
  const isBirthdayUpcoming = (birthday?: string): boolean => {
    if (!birthday) return false;
    const today = new Date();
    const birthdayDate = new Date(birthday);
    const thisYearBirthday = new Date(
      today.getFullYear(),
      birthdayDate.getMonth(),
      birthdayDate.getDate()
    );
    
    const daysUntilBirthday = Math.ceil(
      (thisYearBirthday.getTime() - today.getTime()) / (1000 * 60 * 60 * 24)
    );
    
    return daysUntilBirthday >= 0 && daysUntilBirthday <= 7;
  };

  // Visual state calculation
  const getCardVisualState = () => {
    const state = {
      borderWidth: 1,
      borderColor: theme.palette.divider,
      elevation: 1,
      glowEffect: false,
      backgroundColor: theme.palette.background.paper,
    };

    // Primary contact highlighting
    if (contact.isPrimary) {
      state.borderWidth = 2;
      state.borderColor = theme.palette.primary.main;
      state.backgroundColor = alpha(theme.palette.primary.main, 0.02);
      state.glowEffect = true;
    }

    // Warmth-based border color
    if (warmth) {
      const warmthColors = {
        HOT: '#FF4444',
        WARM: '#FF8800',
        COOLING: '#FFBB00',
        COLD: '#666666',
      };
      if (!contact.isPrimary) {
        state.borderColor = warmthColors[warmth.temperature];
      }
    }

    // Birthday highlighting
    if (isBirthdayUpcoming(contact.birthday)) {
      state.glowEffect = true;
    }

    return state;
  };

  const visualState = getCardVisualState();

  // Helper functions
  const getInitials = () => {
    return `${contact.firstName[0]}${contact.lastName[0]}`.toUpperCase();
  };

  const getAvatarColor = () => {
    const colors = ['#94C456', '#004F7B', '#FF8800', '#2196F3', '#9C27B0'];
    const index = (contact.firstName.charCodeAt(0) + contact.lastName.charCodeAt(0)) % colors.length;
    return colors[index];
  };

  const getDecisionLevelConfig = () => {
    const configs = {
      entscheider: { label: 'Entscheider', color: 'error' as const, icon: <StarIcon /> },
      mitentscheider: { label: 'Mitentscheider', color: 'warning' as const, icon: <GroupsIcon /> },
      einflussnehmer: { label: 'Einflussnehmer', color: 'info' as const, icon: <BusinessIcon /> },
      nutzer: { label: 'Nutzer', color: 'success' as const, icon: <PersonAddIcon /> },
      gatekeeper: { label: 'Gatekeeper', color: 'default' as const, icon: <BusinessIcon /> },
    };
    return configs[contact.decisionLevel || 'nutzer'];
  };


  const getDaysUntilBirthday = (birthday?: string): number => {
    if (!birthday) return -1;
    const today = new Date();
    const birthdayDate = new Date(birthday);
    birthdayDate.setFullYear(today.getFullYear());
    
    if (birthdayDate < today) {
      birthdayDate.setFullYear(today.getFullYear() + 1);
    }
    
    return Math.floor((birthdayDate.getTime() - today.getTime()) / (1000 * 60 * 60 * 24));
  };

  const decisionLevelConfig = getDecisionLevelConfig();

  return (
    <Card
      onMouseEnter={() => setIsCardHovered(true)}
      onMouseLeave={() => setIsCardHovered(false)}
      elevation={visualState.elevation}
      sx={{
        position: 'relative',
        border: `${visualState.borderWidth}px solid`,
        borderColor: visualState.borderColor,
        backgroundColor: visualState.backgroundColor,
        transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
        cursor: 'pointer',
        overflow: 'visible',
        height: '100%',
        display: 'flex',
        flexDirection: 'column',
        ...(isCardHovered && {
          transform: 'translateY(-4px)',
          boxShadow: theme.shadows[8],
        }),
        ...(visualState.glowEffect && {
          '&::before': {
            content: '""',
            position: 'absolute',
            top: -2,
            left: -2,
            right: -2,
            bottom: -2,
            background: `linear-gradient(45deg, ${visualState.borderColor}, transparent)`,
            borderRadius: 'inherit',
            opacity: 0.2,
            animation: 'pulse 2s infinite',
            pointerEvents: 'none',
          },
        }),
        '@keyframes pulse': {
          '0%': { opacity: 0.2 },
          '50%': { opacity: 0.4 },
          '100%': { opacity: 0.2 },
        },
      }}
    >
      {/* Status Badges - Top Right */}
      <Box sx={{ position: 'absolute', top: 8, right: 8, display: 'flex', gap: 0.5, zIndex: 1 }}>
        {contact.isPrimary && (
          <Tooltip title="Hauptansprechpartner">
            <Chip
              icon={<StarIcon />}
              label="HAUPT"
              color="primary"
              size="small"
              sx={{ fontWeight: 'bold', fontSize: '0.7rem' }}
            />
          </Tooltip>
        )}
        {isBirthdayUpcoming(contact.birthday) && (
          <Tooltip title={`Geburtstag in ${getDaysUntilBirthday(contact.birthday)} Tagen`}>
            <Chip
              icon={<CakeIcon />}
              label={`${getDaysUntilBirthday(contact.birthday)}T`}
              color="secondary"
              size="small"
              sx={{ fontWeight: 'bold', fontSize: '0.7rem' }}
            />
          </Tooltip>
        )}
      </Box>

      <CardContent sx={{ pb: 1, flex: 1 }}>
        {/* Header Section with Avatar */}
        <Box display="flex" alignItems="flex-start" gap={2}>
          <Avatar
            sx={{
              bgcolor: getAvatarColor(),
              width: 56,
              height: 56,
              fontSize: '1.25rem',
              fontWeight: 'bold',
              border: contact.isPrimary ? `2px solid ${theme.palette.primary.main}` : 'none',
            }}
          >
            {getInitials()}
          </Avatar>

          <Box flex={1}>
            <Typography
              variant="h6"
              component="div"
              sx={{
                fontWeight: contact.isPrimary ? 'bold' : 'medium',
                fontSize: '1rem',
                lineHeight: 1.2,
                mb: 0.5,
              }}
            >
              {getContactFullName(contact)}
            </Typography>

            {contact.position && (
              <Typography color="text.secondary" variant="body2" sx={{ mb: 1 }}>
                {contact.position}
              </Typography>
            )}

            {/* Decision Level Chip */}
            {decisionLevelConfig && (
              <Chip
                icon={decisionLevelConfig.icon}
                label={decisionLevelConfig.label}
                size="small"
                color={decisionLevelConfig.color}
                sx={{ fontWeight: 'medium', mb: 1 }}
              />
            )}
          </Box>

          {/* More Menu Button */}
          <IconButton
            size="small"
            onClick={(e) => {
              e.stopPropagation();
              setAnchorEl(e.currentTarget);
            }}
          >
            <MoreVertIcon />
          </IconButton>
        </Box>

        {/* Contact Information */}
        <Stack spacing={0.5} sx={{ mt: 2 }}>
          {contact.email && (
            <Box display="flex" alignItems="center" gap={1}>
              <EmailIcon fontSize="small" sx={{ color: 'text.secondary' }} />
              <Typography variant="body2" noWrap sx={{ fontSize: '0.875rem' }}>
                {contact.email}
              </Typography>
            </Box>
          )}

          {contact.phone && (
            <Box display="flex" alignItems="center" gap={1}>
              <PhoneIcon fontSize="small" sx={{ color: 'text.secondary' }} />
              <Typography variant="body2" sx={{ fontSize: '0.875rem' }}>
                {contact.phone}
              </Typography>
            </Box>
          )}

          {contact.mobile && (
            <Box display="flex" alignItems="center" gap={1}>
              <WhatsAppIcon fontSize="small" sx={{ color: 'text.secondary' }} />
              <Typography variant="body2" sx={{ fontSize: '0.875rem' }}>
                {contact.mobile}
              </Typography>
            </Box>
          )}
        </Stack>

        {/* Location Responsibility */}
        {contact.responsibilityScope && (
          <Box sx={{ mt: 2, display: 'flex', alignItems: 'center', gap: 1 }}>
            <LocationIcon fontSize="small" sx={{ color: 'text.secondary' }} />
            <Typography variant="body2" color="text.secondary" sx={{ fontSize: '0.875rem' }}>
              {contact.responsibilityScope === 'all'
                ? 'Alle Standorte'
                : `${contact.assignedLocationIds?.length || 0} Standorte`}
            </Typography>
          </Box>
        )}

        {/* Warmth Indicator */}
        {warmth && (
          <Box sx={{ mt: 2 }}>
            <WarmthIndicator
              warmth={warmth}
              size="small"
              showDetails={isCardHovered}
              variant="bar"
            />
          </Box>
        )}

        {/* Relationship Data (when expanded) */}
        {isCardHovered && (contact.hobbies?.length || contact.personalNotes) && (
          <Box sx={{ mt: 2, pt: 1, borderTop: 1, borderColor: 'divider' }}>
            {contact.hobbies && contact.hobbies.length > 0 && (
              <Stack direction="row" spacing={0.5} flexWrap="wrap" sx={{ mb: 1 }}>
                {contact.hobbies.slice(0, 3).map((hobby) => (
                  <Chip
                    key={hobby}
                    label={hobby}
                    size="small"
                    variant="outlined"
                    sx={{ fontSize: '0.7rem' }}
                  />
                ))}
                {contact.hobbies.length > 3 && (
                  <Chip
                    label={`+${contact.hobbies.length - 3}`}
                    size="small"
                    variant="outlined"
                    sx={{ fontSize: '0.7rem' }}
                  />
                )}
              </Stack>
            )}
          </Box>
        )}
      </CardContent>

      {/* Audit Timeline Integration with Lazy Loading */}
      {canViewAudit && contact.id && (
        <LazyComponent
          minHeight={60}
          threshold={0.2}
          rootMargin="50px"
          placeholder={
            <Typography variant="caption" color="text.secondary" sx={{ p: 1 }}>
              Lade Änderungshistorie...
            </Typography>
          }
        >
          <MiniAuditTimeline
            entityType="CONTACT"
            entityId={contact.id}
            maxEntries={5}
            showDetails={false}
            compact={true}
            onShowMore={onViewTimeline ? () => onViewTimeline(contact.id) : undefined}
          />
        </LazyComponent>
      )}

      {/* Quick Actions (visible on hover) */}
      {isCardHovered && showQuickActions && onQuickAction && (
        <CardActions sx={{ justifyContent: 'center', py: 1, borderTop: 1, borderColor: 'divider' }}>
          <ContactQuickActions
            contact={contact}
            onAction={(action) => onQuickAction(action, contact.id)}
            variant="compact"
          />
        </CardActions>
      )}

      {/* Context Menu */}
      <Menu
        anchorEl={anchorEl}
        open={Boolean(anchorEl)}
        onClose={() => setAnchorEl(null)}
        PaperProps={{
          sx: { minWidth: 200 },
        }}
      >
        <MenuItem
          onClick={() => {
            onEdit(contact);
            setAnchorEl(null);
          }}
        >
          <EditIcon fontSize="small" sx={{ mr: 1 }} />
          Bearbeiten
        </MenuItem>

        {!contact.isPrimary && (
          <MenuItem
            onClick={() => {
              onSetPrimary(contact.id);
              setAnchorEl(null);
            }}
          >
            <StarBorderIcon fontSize="small" sx={{ mr: 1 }} />
            Als Hauptkontakt
          </MenuItem>
        )}

        {onAssignLocation && (
          <MenuItem
            onClick={() => {
              onAssignLocation(contact.id);
              setAnchorEl(null);
            }}
          >
            <LocationIcon fontSize="small" sx={{ mr: 1 }} />
            Standort zuordnen
          </MenuItem>
        )}

        {onViewTimeline && (
          <MenuItem
            onClick={() => {
              onViewTimeline(contact.id);
              setAnchorEl(null);
            }}
          >
            <TimelineIcon fontSize="small" sx={{ mr: 1 }} />
            Aktivitätsverlauf
          </MenuItem>
        )}

        <Divider />

        <MenuItem
          onClick={() => {
            if (window.confirm(`Möchten Sie ${getContactFullName(contact)} wirklich löschen?`)) {
              onDelete(contact.id);
            }
            setAnchorEl(null);
          }}
          sx={{ color: 'error.main' }}
          disabled={contact.isPrimary}
        >
          <DeleteIcon fontSize="small" sx={{ mr: 1 }} />
          Löschen
        </MenuItem>
      </Menu>
    </Card>
  );
};