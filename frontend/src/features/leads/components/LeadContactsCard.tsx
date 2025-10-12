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
} from '@mui/material';
import {
  Edit as EditIcon,
  Delete as DeleteIcon,
  StarBorder as StarBorderIcon,
  Phone as PhoneIcon,
  Email as EmailIcon,
  Add as AddIcon,
} from '@mui/icons-material';
import type { LeadContactDTO } from '../types';

interface LeadContactsCardProps {
  contacts: LeadContactDTO[];
  onAdd?: () => void;
  onEdit?: (contact: LeadContactDTO) => void;
  onDelete?: (contactId: string) => void;
  onSetPrimary?: (contactId: string) => void;
  readonly?: boolean;
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
 * Design: FreshFoodz CI (#94C456, #004F7B)
 */
export function LeadContactsCard({
  contacts,
  onAdd,
  onEdit,
  onDelete,
  onSetPrimary,
  readonly = false,
}: LeadContactsCardProps) {
  const primaryContact = contacts.find(c => c.primary);
  const secondaryContacts = contacts.filter(c => !c.primary);

  const getWarmthColor = (score?: number) => {
    if (!score) return '#9E9E9E'; // Gray
    if (score >= 70) return '#94C456'; // FreshFoodz Green
    if (score >= 40) return '#FF9800'; // Orange
    return '#F44336'; // Red
  };

  const renderContactItem = (contact: LeadContactDTO, isPrimary: boolean) => (
    <ListItem
      key={contact.id}
      sx={{
        borderLeft: isPrimary ? '4px solid #94C456' : '4px solid transparent',
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
                sx={{ bgcolor: '#94C456', color: 'white', fontWeight: 600 }}
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
            {contact.position && (
              <Typography variant="body2" color="text.secondary">
                {contact.position}
                {contact.decisionLevel && ` • ${contact.decisionLevel}`}
              </Typography>
            )}
            <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
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
      <CardContent>
        {contacts.length === 0 ? (
          <Box
            sx={{
              textAlign: 'center',
              py: 4,
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
        )}
      </CardContent>
    </Card>
  );
}
