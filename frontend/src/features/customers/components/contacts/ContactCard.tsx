/**
 * ContactCard Component
 *
 * Smart contact card with relationship intelligence features.
 * Part of Step 3 Multi-Contact Management.
 *
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/SMART_CONTACT_CARDS.md
 */

import React from 'react';
import {
  Card,
  CardContent,
  CardActions,
  Typography,
  Box,
  Chip,
  IconButton,
  Tooltip,
  Stack,
  useTheme,
  alpha,
} from '@mui/material';
import {
  Star as StarIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Phone as PhoneIcon,
  Email as EmailIcon,
  Business as BusinessIcon,
  Cake as CakeIcon,
  LocationOn as LocationIcon,
  TrendingUp as TrendingUpIcon,
} from '@mui/icons-material';

import type { Contact } from '../../types/contact.types';
import { getContactFullName, getContactDisplayName } from '../../types/contact.types';

interface ContactCardProps {
  contact: Contact;
  onEdit: (contact: Contact) => void;
  onDelete: (contactId: string) => void;
  onSetPrimary: (contactId: string) => void;
  isExpanded?: boolean;
  showRelationshipData?: boolean;
}

/**
 * Smart Contact Card Component
 *
 * Displays contact information with visual indicators
 * for relationship warmth and importance.
 */
export const ContactCard: React.FC<ContactCardProps> = ({
  contact,
  onEdit,
  onDelete,
  onSetPrimary,
  isExpanded = false,
  showRelationshipData = true,
}) => {
  const theme = useTheme();

  // Calculate days until birthday
  const getDaysUntilBirthday = (): number | null => {
    if (!contact.birthday) return null;

    const today = new Date();
    const birthday = new Date(contact.birthday);
    birthday.setFullYear(today.getFullYear());

    if (birthday < today) {
      birthday.setFullYear(today.getFullYear() + 1);
    }

    const diffTime = birthday.getTime() - today.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

    return diffDays;
  };

  const daysUntilBirthday = getDaysUntilBirthday();
  const birthdayIsSoon = daysUntilBirthday !== null && daysUntilBirthday <= 30;

  return (
    <Card
      elevation={contact.isPrimary ? 3 : 1}
      sx={{
        position: 'relative',
        border: contact.isPrimary ? `2px solid ${theme.palette.primary.main}` : undefined,
        backgroundColor: contact.isPrimary ? alpha(theme.palette.primary.main, 0.05) : undefined,
        transition: 'all 0.2s ease-in-out',
        '&:hover': {
          transform: 'translateY(-2px)',
          boxShadow: theme.shadows[4],
        },
      }}
    >
      {/* Primary Badge */}
      {contact.isPrimary && (
        <Box
          sx={{
            position: 'absolute',
            top: 0,
            right: 0,
            backgroundColor: theme.palette.primary.main,
            color: theme.palette.primary.contrastText,
            px: 2,
            py: 0.5,
            borderBottomLeftRadius: theme.shape.borderRadius,
            display: 'flex',
            alignItems: 'center',
            gap: 0.5,
          }}
        >
          <StarIcon fontSize="small" />
          <Typography variant="caption" fontWeight="bold">
            HAUPT
          </Typography>
        </Box>
      )}

      <CardContent>
        {/* Header */}
        <Box sx={{ mb: 2 }}>
          <Typography variant="h6" component="h3">
            {getContactFullName(contact)}
          </Typography>
          {contact.position && (
            <Typography variant="body2" color="text.secondary">
              {contact.position}
            </Typography>
          )}
          {contact.decisionLevel && (
            <Chip
              label={contact.decisionLevel}
              size="small"
              color={contact.decisionLevel === 'entscheider' ? 'primary' : 'default'}
              sx={{ mt: 0.5 }}
            />
          )}
        </Box>

        {/* Contact Info */}
        <Stack spacing={1}>
          {contact.email && (
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <EmailIcon fontSize="small" color="action" />
              <Typography variant="body2" sx={{ wordBreak: 'break-word' }}>
                {contact.email}
              </Typography>
            </Box>
          )}

          {contact.phone && (
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <PhoneIcon fontSize="small" color="action" />
              <Typography variant="body2">{contact.phone}</Typography>
            </Box>
          )}

          {contact.mobile && (
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <PhoneIcon fontSize="small" color="action" />
              <Typography variant="body2">{contact.mobile} (Mobil)</Typography>
            </Box>
          )}
        </Stack>

        {/* Location Responsibility */}
        {contact.responsibilityScope && (
          <Box sx={{ mt: 2, display: 'flex', alignItems: 'center', gap: 1 }}>
            <LocationIcon fontSize="small" color="action" />
            <Typography variant="body2" color="text.secondary">
              {contact.responsibilityScope === 'all'
                ? 'Alle Standorte'
                : `${contact.assignedLocationIds?.length || 0} ausgewählte Standorte`}
            </Typography>
          </Box>
        )}

        {/* Roles */}
        {contact.roles && contact.roles.length > 0 && (
          <Box sx={{ mt: 2 }}>
            <Stack direction="row" spacing={0.5} flexWrap="wrap">
              {contact.roles.map(role => (
                <Chip
                  key={role}
                  label={role}
                  size="small"
                  variant="outlined"
                  icon={<BusinessIcon fontSize="small" />}
                />
              ))}
            </Stack>
          </Box>
        )}

        {/* Relationship Data (when expanded) */}
        {isExpanded && showRelationshipData && (
          <Box sx={{ mt: 2, pt: 2, borderTop: 1, borderColor: 'divider' }}>
            {birthdayIsSoon && (
              <Chip
                icon={<CakeIcon />}
                label={`Geburtstag in ${daysUntilBirthday} Tagen`}
                size="small"
                color="secondary"
                sx={{ mb: 1 }}
              />
            )}

            {contact.hobbies && contact.hobbies.length > 0 && (
              <Box sx={{ mb: 1 }}>
                <Typography variant="caption" color="text.secondary">
                  Interessen:
                </Typography>
                <Stack direction="row" spacing={0.5} flexWrap="wrap" sx={{ mt: 0.5 }}>
                  {contact.hobbies.map(hobby => (
                    <Chip
                      key={hobby}
                      label={hobby}
                      size="small"
                      variant="outlined"
                      color="secondary"
                    />
                  ))}
                </Stack>
              </Box>
            )}

            {contact.personalNotes && (
              <Box>
                <Typography variant="caption" color="text.secondary">
                  Notizen:
                </Typography>
                <Typography variant="body2" sx={{ mt: 0.5, fontStyle: 'italic' }}>
                  {contact.personalNotes}
                </Typography>
              </Box>
            )}
          </Box>
        )}
      </CardContent>

      <CardActions sx={{ justifyContent: 'space-between', px: 2, pb: 2 }}>
        <Box>
          {!contact.isPrimary && (
            <Tooltip title="Als Hauptansprechpartner festlegen">
              <IconButton size="small" onClick={() => onSetPrimary(contact.id)}>
                <StarIcon />
              </IconButton>
            </Tooltip>
          )}
        </Box>

        <Box>
          <Tooltip title="Bearbeiten">
            <IconButton size="small" onClick={() => onEdit(contact)}>
              <EditIcon />
            </IconButton>
          </Tooltip>
          <Tooltip title="Löschen">
            <IconButton
              size="small"
              onClick={() => onDelete(contact.id)}
              disabled={contact.isPrimary}
            >
              <DeleteIcon />
            </IconButton>
          </Tooltip>
        </Box>
      </CardActions>
    </Card>
  );
};
