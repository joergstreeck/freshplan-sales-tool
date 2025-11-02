import { useMemo } from 'react';
import {
  Card,
  CardHeader,
  CardContent,
  List,
  ListItem,
  ListItemText,
  ListItemSecondaryAction,
  IconButton,
  Chip,
  Typography,
  Box,
  Button,
  Stack,
  Divider,
  useTheme,
} from '@mui/material';
import {
  Edit as EditIcon,
  Delete as DeleteIcon,
  StarBorder as StarBorderIcon,
  Phone as PhoneIcon,
  Email as EmailIcon,
  Smartphone as SmartphoneIcon,
  Schedule as ScheduleIcon,
  Add as AddIcon,
} from '@mui/icons-material';
import { formatDistanceToNow } from 'date-fns';
import { de } from 'date-fns/locale';
import type { LeadContactDTO } from '../types';
import { useEnumOptions } from '../../../hooks/useEnumOptions';

interface LeadContactsCardProps {
  contacts: LeadContactDTO[];
  onAdd?: () => void;
  onEdit?: (contact: LeadContactDTO) => void;
  onDelete?: (contactId: string) => void;
  onSetPrimary?: (contactId: string) => void;
  readonly?: boolean;
  embedded?: boolean; // If true, renders without Card wrapper (for use in Accordions)
}

/**
 * LeadContactsCard - Sprint 2.1.6 Phase 5+ (ADR-007 100% Parity)
 *
 * Displays and manages multiple contacts for a lead.
 * Features:
 * - List all contacts with primary flag
 * - CRM Intelligence indicators (warmth score, data quality)
 * - Add/Edit/Delete contacts
 * - Set primary contact
 *
 * Design: FreshFoodz CI
 */
export function LeadContactsCard({
  contacts,
  onAdd,
  onEdit,
  onDelete,
  onSetPrimary,
  readonly = false,
  embedded = false,
}: LeadContactsCardProps) {
  const theme = useTheme();

  // Server-Driven Enum: DecisionLevel Labels (Sprint 2.1.7.7 - Enum-Rendering-Parity)
  const { data: decisionLevelOptions } = useEnumOptions('/api/enums/decision-levels');

  // Create fast lookup map (O(1) statt O(n) mit .find())
  const decisionLevelLabels = useMemo(() => {
    if (!decisionLevelOptions) return {};
    return decisionLevelOptions.reduce(
      (acc, item) => {
        acc[item.value] = item.label;
        return acc;
      },
      {} as Record<string, string>,
    );
  }, [decisionLevelOptions]);

  const primaryContact = contacts.find(c => c.primary);
  const secondaryContacts = contacts.filter(c => !c.primary);

  const getWarmthColor = (score?: number) => {
    if (!score) return theme.palette.grey[500]; // Gray
    if (score >= 70) return theme.palette.primary.main; // FreshFoodz Green
    if (score >= 40) return theme.palette.warning.main; // Orange
    return theme.palette.error.main; // Red
  };

  const renderContactItem = (contact: LeadContactDTO, isPrimary: boolean) => (
    <ListItem
      key={contact.id}
      sx={{
        borderLeft: isPrimary ? `4px solid ${theme.palette.primary.main}` : '4px solid transparent',
        bgcolor: isPrimary ? 'rgba(148, 196, 86, 0.05)' : 'transparent',
        mb: 1,
        borderRadius: 1,
        '&:hover': { bgcolor: 'action.hover' },
      }}
    >
      <ListItemText
        primary={
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Typography variant="subtitle1" fontWeight={isPrimary ? 600 : 400}>
              {contact.displayName ||
                contact.fullName ||
                `${contact.firstName} ${contact.lastName}`}
            </Typography>
            {isPrimary && (
              <Chip
                label="Hauptkontakt"
                size="small"
                sx={{ bgcolor: 'primary.main', color: 'white', fontWeight: 600 }}
              />
            )}
            {contact.warmthScore && (
              <Chip
                label={`${contact.warmthScore}% Wärme`}
                size="small"
                sx={{ bgcolor: getWarmthColor(contact.warmthScore), color: 'white' }}
              />
            )}
          </Box>
        }
        secondary={
          <Stack spacing={0.5} sx={{ mt: 0.5 }}>
            {(contact.position || contact.decisionLevel) && (
              <Typography variant="body2" color="text.secondary">
                {contact.position || (decisionLevelLabels[contact.decisionLevel] || contact.decisionLevel)}
              </Typography>
            )}
            <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', alignItems: 'center' }}>
              {contact.email && (
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                  <EmailIcon sx={{ fontSize: 16, color: 'text.secondary' }} />
                  <Typography variant="body2" color="text.secondary">
                    {contact.email}
                  </Typography>
                </Box>
              )}
              {contact.phone && (
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                  <PhoneIcon sx={{ fontSize: 16, color: 'text.secondary' }} />
                  <Typography variant="body2" color="text.secondary">
                    {contact.phone}
                  </Typography>
                </Box>
              )}
              {contact.mobile && (
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                  <SmartphoneIcon sx={{ fontSize: 16, color: 'text.secondary' }} />
                  <Typography variant="body2" color="text.secondary">
                    {contact.mobile}
                  </Typography>
                </Box>
              )}
              {contact.lastInteractionDate && (
                <Chip
                  icon={<ScheduleIcon sx={{ fontSize: 14 }} />}
                  label={`Letzter Kontakt: ${formatDistanceToNow(new Date(contact.lastInteractionDate), { addSuffix: true, locale: de })}`}
                  size="small"
                  variant="outlined"
                  sx={{
                    height: 24,
                    fontSize: '0.75rem',
                    borderColor: 'text.secondary',
                    color: 'text.secondary',
                  }}
                />
              )}
            </Box>
            {contact.dataQualityScore && contact.dataQualityScore < 70 && (
              <Typography variant="caption" color="warning.main">
                ⚠️ Datenqualität: {contact.dataQualityScore}%
                {contact.dataQualityRecommendations && ` - ${contact.dataQualityRecommendations}`}
              </Typography>
            )}
          </Stack>
        }
      />
      {!readonly && (
        <ListItemSecondaryAction>
          <Stack direction="row" spacing={0.5}>
            {!isPrimary && onSetPrimary && (
              <IconButton
                edge="end"
                size="small"
                onClick={() => onSetPrimary(contact.id)}
                title="Als Hauptkontakt setzen"
              >
                <StarBorderIcon />
              </IconButton>
            )}
            {onEdit && (
              <IconButton edge="end" size="small" onClick={() => onEdit(contact)}>
                <EditIcon />
              </IconButton>
            )}
            {onDelete && !isPrimary && (
              <IconButton
                edge="end"
                size="small"
                onClick={() => onDelete(contact.id)}
                color="error"
              >
                <DeleteIcon />
              </IconButton>
            )}
          </Stack>
        </ListItemSecondaryAction>
      )}
    </ListItem>
  );

  // Contact List Content (reused in both embedded and standalone modes)
  const contactListContent = contacts.length === 0 ? (
    <Box
      sx={{
        textAlign: 'center',
        py: embedded ? 2 : 4,
        color: 'text.secondary',
      }}
    >
      <Typography variant="body2">Noch keine Kontakte erfasst</Typography>
      {!readonly && onAdd && (
        <Button startIcon={<AddIcon />} onClick={onAdd} sx={{ mt: 2 }}>
          Ersten Kontakt hinzufügen
        </Button>
      )}
    </Box>
  ) : (
    <List sx={{ py: 0 }}>
      {primaryContact && (
        <>
          {renderContactItem(primaryContact, true)}
          {secondaryContacts.length > 0 && <Divider sx={{ my: 2 }} />}
        </>
      )}
      {secondaryContacts.map(contact => renderContactItem(contact, false))}
    </List>
  );

  // Embedded Mode: Render without Card wrapper (for use in Accordions)
  if (embedded) {
    return <Box sx={{ width: '100%' }}>{contactListContent}</Box>;
  }

  // Standalone Mode: Render with Card wrapper
  return (
    <Card>
      <CardHeader
        title={
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Typography variant="h6">Kontakte</Typography>
            <Chip label={contacts.length} size="small" />
          </Box>
        }
        action={
          !readonly &&
          onAdd && (
            <Button startIcon={<AddIcon />} onClick={onAdd} size="small" variant="outlined">
              Kontakt hinzufügen
            </Button>
          )
        }
      />
      <Divider />
      <CardContent>{contactListContent}</CardContent>
    </Card>
  );
}
