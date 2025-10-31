import { Box, Typography, Stack, Chip, CircularProgress, LinearProgress } from '@mui/material';
import { alpha } from '@mui/material/styles';
import {
  Phone as PhoneIcon,
  Email as EmailIcon,
  Person as PersonIcon,
  SmartphoneOutlined as MobileIcon,
  WorkOutline as WorkIcon,
  AccessTime as TimeIcon,
  TrendingUp as TrendingIcon,
  StickyNote2Outlined as NoteIcon,
} from '@mui/icons-material';
import type { LeadContactDTO } from './types';
import { useEffect, useState } from 'react';
import { getLeadById } from './api';

interface LeadContactsListProps {
  leadId: number;
}

/**
 * LeadContactsList - Compact contacts display for Lead list view
 *
 * Similar to LeadActivityTimeline, this component displays contacts
 * in an expandable inline panel below the lead entry.
 *
 * Features:
 * - Loads contacts from lead detail endpoint
 * - Highlights primary contact
 * - Shows email and phone info
 * - Compact, read-only view
 *
 * Design: FreshFoodz CI (matching LeadActivityTimeline style)
 */
export default function LeadContactsList({ leadId }: LeadContactsListProps) {
  const [contacts, setContacts] = useState<LeadContactDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const loadContacts = async () => {
      try {
        setLoading(true);
        const lead = await getLeadById(leadId.toString());
        setContacts(lead.contacts || []);
        setError(null);
      } catch (err) {
        setError('Fehler beim Laden der Kontakte');
        console.error('Failed to load contacts:', err);
      } finally {
        setLoading(false);
      }
    };

    loadContacts();
  }, [leadId]);

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" p={2}>
        <CircularProgress size={24} />
      </Box>
    );
  }

  if (error) {
    return (
      <Typography variant="body2" color="error" p={2}>
        {error}
      </Typography>
    );
  }

  if (contacts.length === 0) {
    return (
      <Box p={2} textAlign="center">
        <Typography variant="body2" color="text.secondary">
          Noch keine Kontakte erfasst
        </Typography>
      </Box>
    );
  }

  // Helper: Format relative time
  const formatRelativeTime = (dateString?: string) => {
    if (!dateString) return null;
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

    if (diffDays === 0) return 'Heute';
    if (diffDays === 1) return 'Gestern';
    if (diffDays < 7) return `vor ${diffDays} Tagen`;
    if (diffDays < 30) return `vor ${Math.floor(diffDays / 7)} Wochen`;
    if (diffDays < 365) return `vor ${Math.floor(diffDays / 30)} Monaten`;
    return `vor ${Math.floor(diffDays / 365)} Jahren`;
  };

  // Helper: Decision Level Labels
  const decisionLevelLabels: Record<string, { label: string; color: string }> = {
    executive: { label: 'Führungsebene', color: 'error.main' },
    manager: { label: 'Management', color: 'warning.main' },
    operational: { label: 'Operative Ebene', color: 'info.main' },
    influencer: { label: 'Influencer', color: 'secondary.main' },
  };

  // Helper: Warmth Score Color
  const getWarmthColor = (score?: number) => {
    if (!score) return 'grey.500';
    if (score >= 70) return 'success.main';
    if (score >= 40) return 'warning.main';
    return 'error.main';
  };

  // Sort contacts: primary first
  const sortedContacts = [...contacts].sort((a, b) => {
    if (a.primary && !b.primary) return -1;
    if (!a.primary && b.primary) return 1;
    return 0;
  });

  return (
    <Stack spacing={1.5}>
      {sortedContacts.map(contact => (
        <Box
          key={contact.id}
          sx={{
            p: 1,
            borderRadius: 1,
            borderLeft: contact.primary ? '3px solid' : '3px solid transparent',
            borderColor: contact.primary ? 'primary.main' : 'transparent',
            bgcolor: contact.primary
              ? theme => alpha(theme.palette.primary.main, 0.05)
              : 'action.hover',
            display: 'grid',
            gridTemplateColumns: '1fr 1fr auto',
            gap: 2,
            alignItems: 'start',
          }}
        >
          {/* Column 1: Name, Position, Badges */}
          <Box>
            <Box display="flex" alignItems="center" gap={0.5} mb={0.5}>
              <PersonIcon sx={{ fontSize: 16, color: 'text.secondary' }} />
              <Typography variant="subtitle2" fontWeight={contact.primary ? 600 : 500}>
                {contact.displayName ||
                  contact.fullName ||
                  `${contact.firstName} ${contact.lastName}`}
              </Typography>
            </Box>
            {contact.position && (
              <Box display="flex" alignItems="center" gap={0.5} mb={0.5}>
                <WorkIcon sx={{ fontSize: 12, color: 'text.secondary' }} />
                <Typography variant="caption" color="text.secondary">
                  {contact.position}
                </Typography>
              </Box>
            )}
            <Box display="flex" gap={0.5} flexWrap="wrap">
              {contact.primary && (
                <Chip
                  label="Haupt"
                  size="small"
                  sx={{ bgcolor: 'primary.main', color: 'white', height: 18, fontSize: '0.65rem' }}
                />
              )}
              {contact.isDecisionMaker && (
                <Chip
                  label="Entscheider"
                  size="small"
                  sx={{ bgcolor: 'error.main', color: 'white', height: 18, fontSize: '0.65rem' }}
                />
              )}
              {contact.decisionLevel && decisionLevelLabels[contact.decisionLevel] && (
                <Chip
                  label={decisionLevelLabels[contact.decisionLevel].label}
                  size="small"
                  sx={{
                    bgcolor: decisionLevelLabels[contact.decisionLevel].color,
                    color: 'white',
                    height: 18,
                    fontSize: '0.65rem',
                  }}
                />
              )}
            </Box>
          </Box>

          {/* Column 2: Contact Info */}
          <Stack spacing={0.5}>
            {contact.email && (
              <Box display="flex" alignItems="center" gap={0.5}>
                <EmailIcon sx={{ fontSize: 12, color: 'text.secondary' }} />
                <Typography variant="caption" color="text.secondary">
                  {contact.email}
                </Typography>
              </Box>
            )}
            {contact.phone && (
              <Box display="flex" alignItems="center" gap={0.5}>
                <PhoneIcon sx={{ fontSize: 12, color: 'text.secondary' }} />
                <Typography variant="caption" color="text.secondary">
                  {contact.phone}
                </Typography>
              </Box>
            )}
            {contact.mobile && (
              <Box display="flex" alignItems="center" gap={0.5}>
                <MobileIcon sx={{ fontSize: 12, color: 'text.secondary' }} />
                <Typography variant="caption" color="text.secondary">
                  {contact.mobile}
                </Typography>
              </Box>
            )}
            {contact.personalNotes && (
              <Box display="flex" gap={0.5} mt={0.5}>
                <NoteIcon sx={{ fontSize: 12, color: 'warning.main', flexShrink: 0, mt: 0.2 }} />
                <Typography
                  variant="caption"
                  color="text.secondary"
                  sx={{
                    fontStyle: 'italic',
                    overflow: 'hidden',
                    textOverflow: 'ellipsis',
                    display: '-webkit-box',
                    WebkitLineClamp: 2,
                    WebkitBoxOrient: 'vertical',
                  }}
                >
                  {contact.personalNotes}
                </Typography>
              </Box>
            )}
          </Stack>

          {/* Column 3: Sales Intelligence */}
          <Stack spacing={0.5} alignItems="flex-end">
            {contact.warmthScore !== undefined && (
              <Box display="flex" alignItems="center" gap={0.5}>
                <TrendingIcon sx={{ fontSize: 12, color: getWarmthColor(contact.warmthScore) }} />
                <Box sx={{ width: 50 }}>
                  <LinearProgress
                    variant="determinate"
                    value={contact.warmthScore}
                    sx={{
                      height: 4,
                      borderRadius: 1,
                      bgcolor: 'grey.200',
                      '& .MuiLinearProgress-bar': {
                        bgcolor: getWarmthColor(contact.warmthScore),
                      },
                    }}
                  />
                </Box>
                <Typography variant="caption" fontWeight={600} sx={{ minWidth: 30 }}>
                  {contact.warmthScore}%
                </Typography>
              </Box>
            )}
            {contact.lastInteractionDate && (
              <Box display="flex" alignItems="center" gap={0.5}>
                <TimeIcon sx={{ fontSize: 12, color: 'text.secondary' }} />
                <Typography variant="caption" color="text.secondary">
                  {formatRelativeTime(contact.lastInteractionDate)}
                </Typography>
              </Box>
            )}
            {contact.interactionCount !== undefined && contact.interactionCount > 0 && (
              <Typography variant="caption" color="text.secondary">
                {contact.interactionCount}x Kontakte
              </Typography>
            )}
          </Stack>
        </Box>
      ))}
    </Stack>
  );
}
