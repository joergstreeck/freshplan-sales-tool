/**
 * Unified Activity Timeline Component
 *
 * Sprint 2.1.7.2: D8 Unified Communication System
 *
 * Features:
 * - Zeigt Activities für Lead ODER Customer
 * - Bei Customer: Zeigt auch Lead-History! (via originalLeadId)
 * - Vertikale Timeline (chronologisch absteigend)
 * - Activity-Type Icons (CALL, EMAIL, MEETING, NOTE)
 * - "Als Lead erfasst" Badge für Lead-Activities
 *
 * Code-Reuse:
 * - Component funktioniert für BEIDE Entities (Lead + Customer)
 * - Keine Duplikation!
 *
 * Design System Compliance:
 * - ✅ Nur MUI Theme colors (primary.main, secondary.main)
 * - ✅ Typography variants (Antonio Bold für h6, Poppins für body)
 * - ✅ Deutsche UI-Sprache
 */

import React, { useState, useEffect } from 'react';
import {
  Timeline,
  TimelineItem,
  TimelineSeparator,
  TimelineConnector,
  TimelineContent,
  TimelineDot,
  TimelineOppositeContent,
} from '@mui/lab';
import {
  Box,
  Typography,
  Paper,
  CircularProgress,
  Alert,
  Chip,
  IconButton,
} from '@mui/material';
import {
  Phone as PhoneIcon,
  Email as EmailIcon,
  Event as MeetingIcon,
  Notes as NoteIcon,
  Settings as SystemIcon,
  Edit as EditIcon,
  LocalShipping as SampleIcon,
  ShoppingCart as OrderIcon,
  SwapHoriz as StatusChangeIcon,
  Pause as PauseIcon,
  PlayArrow as PlayIcon,
} from '@mui/icons-material';
import { format } from 'date-fns';
import { de } from 'date-fns/locale';

// ============================================================================
// TYPES
// ============================================================================

export interface Activity {
  id: string;
  entityType: 'LEAD' | 'CUSTOMER';
  entityId: string;
  activityType: string;
  activityDate: string;
  description?: string;
  summary?: string;
  outcome?: string;
  userId: string;
  performedBy?: string;
  isMeaningfulContact?: boolean;
  resetsTimer?: boolean;
  countsAsProgress?: boolean;
  createdAt: string;
}

export interface ActivityTimelineProps {
  /** Entity type: 'lead' or 'customer' */
  entityType: 'lead' | 'customer';
  /** Entity ID as string (Lead ID or Customer UUID) */
  entityId: string;
  /** Optional callback when activity should be edited */
  onEdit?: (activity: Activity) => void;
}

// ============================================================================
// COMPONENT
// ============================================================================

export const ActivityTimeline: React.FC<ActivityTimelineProps> = ({
  entityType,
  entityId,
  onEdit,
}) => {
  const [activities, setActivities] = useState<Activity[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // ============================================================================
  // DATA FETCHING
  // ============================================================================

  useEffect(() => {
    const fetchActivities = async () => {
      try {
        setLoading(true);

        // For customer: Backend returns unified timeline including Lead history!
        // For lead: Backend returns only Lead activities
        const endpoint =
          entityType === 'customer'
            ? `/api/activities/customer/${entityId}`
            : `/api/activities/lead/${entityId}`;

        const response = await fetch(endpoint, {
          credentials: 'include',
        });

        if (!response.ok) {
          throw new Error('Fehler beim Laden der Aktivitäten');
        }

        const result = await response.json();
        setActivities(result || []);
        setError(null);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Unbekannter Fehler');
      } finally {
        setLoading(false);
      }
    };

    fetchActivities();
  }, [entityType, entityId]);

  // ============================================================================
  // ACTIVITY ICON MAPPING
  // ============================================================================

  const getActivityIcon = (type: string) => {
    switch (type) {
      case 'CALL':
      case 'QUALIFIED_CALL':
        return <PhoneIcon />;
      case 'EMAIL':
      case 'EMAIL_RECEIVED':
        return <EmailIcon />;
      case 'MEETING':
      case 'DEMO':
      case 'ROI_PRESENTATION':
        return <MeetingIcon />;
      case 'SAMPLE_SENT':
      case 'SAMPLE_FEEDBACK':
        return <SampleIcon />;
      case 'ORDER':
        return <OrderIcon />;
      case 'NOTE':
      case 'FOLLOW_UP':
        return <NoteIcon />;
      case 'STATUS_CHANGE':
      case 'FIRST_CONTACT_DOCUMENTED':
      case 'LEAD_ASSIGNED':
        return <StatusChangeIcon />;
      case 'CLOCK_STOPPED':
      case 'GRACE_PERIOD_STARTED':
        return <PauseIcon />;
      case 'CLOCK_RESUMED':
      case 'REACTIVATED':
        return <PlayIcon />;
      case 'CREATED':
      case 'DELETED':
      case 'EXPIRED':
      case 'REMINDER_SENT':
        return <SystemIcon />;
      default:
        return <NoteIcon />;
    }
  };

  // ============================================================================
  // ACTIVITY COLOR MAPPING (MUI Theme Colors Only!)
  // ============================================================================

  const getActivityColor = (activity: Activity): 'primary' | 'secondary' | 'success' | 'error' | 'warning' | 'info' | 'default' => {
    // Meaningful Contact → Success
    if (activity.isMeaningfulContact) return 'success';

    // Activity Type Colors
    switch (activity.activityType) {
      case 'CALL':
      case 'QUALIFIED_CALL':
        return 'primary';
      case 'EMAIL':
      case 'EMAIL_RECEIVED':
        return 'info';
      case 'MEETING':
      case 'DEMO':
      case 'ROI_PRESENTATION':
        return 'success';
      case 'SAMPLE_SENT':
      case 'ORDER':
        return 'success';
      case 'CLOCK_STOPPED':
      case 'GRACE_PERIOD_STARTED':
        return 'warning';
      case 'CLOCK_RESUMED':
      case 'REACTIVATED':
        return 'info';
      case 'DELETED':
      case 'EXPIRED':
        return 'error';
      case 'CREATED':
      case 'REMINDER_SENT':
      case 'STATUS_CHANGE':
      case 'FIRST_CONTACT_DOCUMENTED':
      case 'LEAD_ASSIGNED':
        return 'secondary';
      default:
        return 'default';
    }
  };

  // ============================================================================
  // ACTIVITY LABEL MAPPING (German)
  // ============================================================================

  const getActivityLabel = (type: string): string => {
    const labels: Record<string, string> = {
      QUALIFIED_CALL: 'Qualifizierter Anruf',
      MEETING: 'Termin',
      DEMO: 'Produktdemo',
      ROI_PRESENTATION: 'ROI-Präsentation',
      SAMPLE_SENT: 'Muster versendet',
      NOTE: 'Notiz',
      FOLLOW_UP: 'Follow-up',
      EMAIL: 'E-Mail',
      CALL: 'Anruf',
      SAMPLE_FEEDBACK: 'Muster-Feedback',
      FIRST_CONTACT_DOCUMENTED: 'Erstkontakt dokumentiert',
      EMAIL_RECEIVED: 'E-Mail erhalten',
      LEAD_ASSIGNED: 'Lead zugewiesen',
      ORDER: 'Bestellung',
      STATUS_CHANGE: 'Statusänderung',
      CREATED: 'Erstellt',
      DELETED: 'Gelöscht',
      REMINDER_SENT: 'Erinnerung versendet',
      GRACE_PERIOD_STARTED: 'Schutzfrist pausiert',
      EXPIRED: 'Abgelaufen',
      REACTIVATED: 'Reaktiviert',
      CLOCK_STOPPED: 'Schutzfrist gestoppt',
      CLOCK_RESUMED: 'Schutzfrist fortgesetzt',
    };
    return labels[type] || type;
  };

  // ============================================================================
  // OUTCOME LABEL MAPPING (German)
  // ============================================================================

  const getOutcomeLabel = (outcome: string): string => {
    const labels: Record<string, string> = {
      SUCCESSFUL: 'Erfolgreich',
      UNSUCCESSFUL: 'Nicht erfolgreich',
      NO_ANSWER: 'Nicht erreicht',
      CALLBACK_REQUESTED: 'Rückruf gewünscht',
      INFO_SENT: 'Informationen gesendet',
      QUALIFIED: 'Qualifiziert',
      DISQUALIFIED: 'Disqualifiziert',
    };
    return labels[outcome] || outcome;
  };

  // ============================================================================
  // RENDER: LOADING STATE
  // ============================================================================

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" p={3}>
        <CircularProgress />
      </Box>
    );
  }

  // ============================================================================
  // RENDER: ERROR STATE
  // ============================================================================

  if (error) {
    return (
      <Alert severity="error" sx={{ m: 2 }}>
        {error}
      </Alert>
    );
  }

  // ============================================================================
  // RENDER: EMPTY STATE
  // ============================================================================

  if (activities.length === 0) {
    return (
      <Alert severity="info" sx={{ m: 2 }}>
        Noch keine Aktivitäten vorhanden.
      </Alert>
    );
  }

  // ============================================================================
  // RENDER: TIMELINE
  // ============================================================================

  return (
    <Timeline position="right">
      {activities.map((activity, index) => (
        <TimelineItem key={activity.id}>
          {/* Date/Time on the left */}
          <TimelineOppositeContent color="text.secondary" sx={{ flex: 0.3 }}>
            <Typography variant="caption">
              {format(new Date(activity.activityDate), 'dd. MMM yyyy', {
                locale: de,
              })}
            </Typography>
            <Typography variant="caption" display="block">
              {format(new Date(activity.activityDate), 'HH:mm', { locale: de })} Uhr
            </Typography>
          </TimelineOppositeContent>

          {/* Icon + Connector */}
          <TimelineSeparator>
            <TimelineDot color={getActivityColor(activity)}>
              {getActivityIcon(activity.activityType)}
            </TimelineDot>
            {index < activities.length - 1 && <TimelineConnector />}
          </TimelineSeparator>

          {/* Content */}
          <TimelineContent>
            <Paper elevation={1} sx={{ p: 2, mb: 2 }}>
              {/* Header: Activity Type + Badges + Edit Button */}
              <Box
                sx={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  mb: 1,
                }}
              >
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, flexWrap: 'wrap' }}>
                  {/* Activity Type Chip */}
                  <Chip
                    label={getActivityLabel(activity.activityType)}
                    size="small"
                    color={getActivityColor(activity)}
                  />

                  {/* "Als Lead erfasst" Badge (nur bei Customer-Ansicht!) */}
                  {entityType === 'customer' && activity.entityType === 'LEAD' && (
                    <Chip
                      label="Als Lead erfasst"
                      size="small"
                      variant="outlined"
                      color="secondary"
                    />
                  )}

                  {/* Meaningful Contact Badge */}
                  {activity.isMeaningfulContact && (
                    <Chip
                      label="Meaningful Contact"
                      size="small"
                      color="success"
                      variant="outlined"
                    />
                  )}

                  {/* Outcome Chip */}
                  {activity.outcome && (
                    <Chip
                      label={getOutcomeLabel(activity.outcome)}
                      size="small"
                      variant="outlined"
                    />
                  )}
                </Box>

                {/* Edit Button */}
                {onEdit && (
                  <IconButton size="small" onClick={() => onEdit(activity)}>
                    <EditIcon fontSize="small" />
                  </IconButton>
                )}
              </Box>

              {/* Summary */}
              {activity.summary && (
                <Typography variant="subtitle2" fontWeight="bold" gutterBottom>
                  {activity.summary}
                </Typography>
              )}

              {/* Description */}
              {activity.description && (
                <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                  {activity.description}
                </Typography>
              )}

              {/* Footer: User */}
              <Typography variant="caption" color="text.secondary" sx={{ mt: 1, display: 'block' }}>
                Erfasst von: {activity.performedBy || activity.userId}
              </Typography>
            </Paper>
          </TimelineContent>
        </TimelineItem>
      ))}
    </Timeline>
  );
};

export default ActivityTimeline;
