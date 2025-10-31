import { useState, useEffect } from 'react';
import {
  Timeline,
  TimelineItem,
  TimelineSeparator,
  TimelineConnector,
  TimelineContent,
  TimelineDot,
  TimelineOppositeContent,
} from '@mui/lab';
import { Box, Typography, Paper, CircularProgress } from '@mui/material';
import {
  Email,
  Phone,
  Event,
  LocalShipping,
  ShoppingCart,
  Note,
  SwapHoriz,
} from '@mui/icons-material';
import { format } from 'date-fns';
import { de } from 'date-fns/locale';

interface CustomerActivity {
  id: number;
  activityType: string;
  activityDate: string;
  description?: string;
  userId: string;
}

interface CustomerActivityTimelineProps {
  customerId: string;
}

/**
 * Customer Activity Timeline
 *
 * Zeigt chronologische Aktivitätshistorie eines Kunden.
 */
export default function CustomerActivityTimeline({ customerId }: CustomerActivityTimelineProps) {
  const [activities, setActivities] = useState<CustomerActivity[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchActivities = async () => {
      try {
        setLoading(true);
        const response = await fetch(`/api/customers/${customerId}/timeline`, {
          credentials: 'include',
        });

        if (!response.ok) {
          throw new Error('Fehler beim Laden der Aktivitäten');
        }

        const result = await response.json();
        setActivities(result.events || result.data || []);
        setError(null);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Unbekannter Fehler');
      } finally {
        setLoading(false);
      }
    };

    fetchActivities();
  }, [customerId]);

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
      default:
        return <Note />;
    }
  };

  const getActivityColor = (
    type: string
  ): 'primary' | 'secondary' | 'success' | 'error' | 'info' | 'warning' | 'grey' => {
    switch (type) {
      case 'EMAIL':
      case 'CALL':
      case 'MEETING':
        return 'primary';
      case 'ORDER':
        return 'success';
      case 'STATUS_CHANGE':
        return 'info';
      default:
        return 'grey';
    }
  };

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

  if (activities.length === 0) {
    return (
      <Box p={2} textAlign="center">
        <Typography variant="body2" color="text.secondary">
          Noch keine Aktivitäten erfasst
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
              {format(new Date(activity.activityDate), 'dd. MMM yyyy', { locale: de })}
            </Typography>
            <Typography variant="caption" display="block">
              {format(new Date(activity.activityDate), 'HH:mm', { locale: de })} Uhr
            </Typography>
          </TimelineOppositeContent>
          <TimelineSeparator>
            <TimelineDot color={getActivityColor(activity.activityType)}>
              {getActivityIcon(activity.activityType)}
            </TimelineDot>
            {index < activities.length - 1 && <TimelineConnector />}
          </TimelineSeparator>
          <TimelineContent>
            <Paper elevation={1} sx={{ p: 1.5 }}>
              <Typography variant="subtitle2">{activity.activityType}</Typography>
              {activity.description && (
                <Typography variant="body2" color="text.secondary">
                  {activity.description}
                </Typography>
              )}
              <Typography variant="caption" color="text.secondary" display="block" sx={{ mt: 0.5 }}>
                Benutzer: {activity.userId}
              </Typography>
            </Paper>
          </TimelineContent>
        </TimelineItem>
      ))}
    </Timeline>
  );
}
