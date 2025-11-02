import { useState, useEffect, useMemo } from 'react';
import {
  Timeline,
  TimelineItem,
  TimelineSeparator,
  TimelineConnector,
  TimelineContent,
  TimelineDot,
  TimelineOppositeContent,
} from '@mui/lab';
import { Box, Typography, Paper, CircularProgress, Alert, Chip } from '@mui/material';
import {
  Email,
  Phone,
  Event,
  LocalShipping,
  ShoppingCart,
  Note,
  SwapHoriz,
  Pause,
  PlayArrow,
} from '@mui/icons-material';
import { format } from 'date-fns';
import { de } from 'date-fns/locale';
import { useEnumOptions } from '../../hooks/useEnumOptions';

interface LeadActivity {
  id: number;
  activityType: string;
  activityDate: string;
  description?: string;
  userId: string;
  isMeaningfulContact?: boolean;
}

interface LeadActivityTimelineProps {
  leadId: number;
}

/**
 * Lead Activity Timeline (Sprint 2.1.6 Phase 4)
 *
 * Zeigt chronologische Aktivitätshistorie eines Leads.
 * Nutzt existierende lead_activities Tabelle (V229).
 *
 * Aktivitätstypen:
 * - EMAIL, CALL, MEETING: Meaningful Contact (resettet Timer)
 * - SAMPLE_SENT, ORDER, NOTE, STATUS_CHANGE: Tracking
 * - CLOCK_STOPPED, CLOCK_RESUMED: Stop-the-Clock
 */
export default function LeadActivityTimeline({ leadId }: LeadActivityTimelineProps) {
  // Sprint 2.1.7.7 - Enum-Rendering-Parity Migration BATCH 4
  const { data: activityTypeOptions } = useEnumOptions('/api/enums/activity-types');

  // Create fast lookup map (O(1) statt O(n) mit .find())
  const activityTypeLabels = useMemo(() => {
    if (!activityTypeOptions) return {};
    return activityTypeOptions.reduce(
      (acc, item) => {
        acc[item.value] = item.label;
        return acc;
      },
      {} as Record<string, string>,
    );
  }, [activityTypeOptions]);

  const [activities, setActivities] = useState<LeadActivity[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchActivities = async () => {
      try {
        setLoading(true);
        const response = await fetch(`/api/leads/${leadId}/activities`, {
          credentials: 'include',
        });

        if (!response.ok) {
          throw new Error('Fehler beim Laden der Aktivitäten');
        }

        const result = await response.json();
        // Backend returns paginated response with {data: [], page, size, total}
        setActivities(result.data || []);
        setError(null);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Unbekannter Fehler');
      } finally {
        setLoading(false);
      }
    };

    fetchActivities();
  }, [leadId]);

  const getActivityIcon = (type: string) => {
    switch (type) {
      case 'EMAIL':
        return <Email />;
      case 'CALL':
        return <Phone />;
      case 'MEETING':
        return <Event />;
      case 'SAMPLE_SENT':
        return <LocalShipping />;
      case 'ORDER':
        return <ShoppingCart />;
      case 'NOTE':
        return <Note />;
      case 'STATUS_CHANGE':
        return <SwapHoriz />;
      case 'CLOCK_STOPPED':
        return <Pause />;
      case 'CLOCK_RESUMED':
        return <PlayArrow />;
      default:
        return <Note />;
    }
  };

  const getActivityColor = (type: string, isMeaningful?: boolean) => {
    if (isMeaningful) return 'success';
    if (type === 'CLOCK_STOPPED') return 'warning';
    if (type === 'CLOCK_RESUMED') return 'info';
    if (type === 'ORDER') return 'success';
    return 'grey';
  };

  const getActivityLabel = (type: string): string => {
    return activityTypeLabels[type] || type;
  };

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" p={3}>
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    return (
      <Alert severity="error" sx={{ m: 2 }}>
        {error}
      </Alert>
    );
  }

  if (activities.length === 0) {
    return (
      <Box p={3} textAlign="center">
        <Typography variant="body2" color="text.secondary">
          Noch keine Aktivitäten vorhanden
        </Typography>
      </Box>
    );
  }

  return (
    <Timeline position="right">
      {activities.map((activity, index) => (
        <TimelineItem key={activity.id}>
          <TimelineOppositeContent color="text.secondary" sx={{ flex: 0.3 }}>
            <Typography variant="caption">
              {format(new Date(activity.activityDate), 'dd. MMM yyyy', {
                locale: de,
              })}
            </Typography>
            <Typography variant="caption" display="block">
              {format(new Date(activity.activityDate), 'HH:mm', { locale: de })}
            </Typography>
          </TimelineOppositeContent>

          <TimelineSeparator>
            <TimelineDot
              color={getActivityColor(activity.activityType, activity.isMeaningfulContact)}
            >
              {getActivityIcon(activity.activityType)}
            </TimelineDot>
            {index < activities.length - 1 && <TimelineConnector />}
          </TimelineSeparator>

          <TimelineContent>
            <Paper elevation={1} sx={{ p: 2, mb: 2 }}>
              <Box display="flex" alignItems="center" gap={1} mb={0.5}>
                <Typography variant="subtitle2" component="span">
                  {getActivityLabel(activity.activityType)}
                </Typography>
                {activity.isMeaningfulContact && (
                  <Chip
                    label="Meaningful Contact"
                    size="small"
                    color="success"
                    variant="outlined"
                  />
                )}
              </Box>

              {activity.description && (
                <Typography variant="body2" color="text.secondary">
                  {activity.description}
                </Typography>
              )}

              <Typography variant="caption" color="text.secondary" display="block" mt={1}>
                von {activity.userId}
              </Typography>
            </Paper>
          </TimelineContent>
        </TimelineItem>
      ))}
    </Timeline>
  );
}
