import { useState, useMemo, useEffect } from 'react';
import {
  Accordion,
  AccordionSummary,
  AccordionDetails,
  Typography,
  Box,
  List,
  ListItem,
  ListItemAvatar,
  ListItemText,
  Avatar,
  Chip,
  CircularProgress,
  Alert,
} from '@mui/material';
import { ExpandMore as ExpandMoreIcon } from '@mui/icons-material';
import { formatDistanceToNow } from 'date-fns';
import { de } from 'date-fns/locale';

interface Activity {
  id: number;
  activityType: string;
  activityDate: string;
  description?: string;
  userId: string;
  isMeaningfulContact?: boolean;
}

interface LeadActivityTimelineGroupedProps {
  leadId: number;
}

// Gruppierungs-Logik
function groupActivitiesByTimeRange(activities: Activity[]) {
  const now = new Date();
  const sevenDaysAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
  const thirtyDaysAgo = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000);

  const groups = {
    last7Days: [] as Activity[],
    last30Days: [] as Activity[],
    older: [] as Activity[],
  };

  activities.forEach(activity => {
    const activityDate = new Date(activity.activityDate);

    if (activityDate >= sevenDaysAgo) {
      groups.last7Days.push(activity);
    } else if (activityDate >= thirtyDaysAgo) {
      groups.last30Days.push(activity);
    } else {
      groups.older.push(activity);
    }
  });

  return groups;
}

// Activity Type Icons & Colors
function getActivityTypeInfo(type: string): { icon: string; color: string } {
  const typeMap: Record<string, { icon: string; color: string }> = {
    LEAD_CREATED: { icon: '✨', color: '#94C456' },
    FIRST_CONTACT_DOCUMENTED: { icon: '📞', color: '#2196F3' },
    STATUS_CHANGED: { icon: '🔄', color: '#FF9800' },
    QUALIFIED: { icon: '✅', color: '#4CAF50' },
    MEETING_SCHEDULED: { icon: '📅', color: '#9C27B0' },
    CONTACT_ADDED: { icon: '👤', color: '#00BCD4' },
    NOTE_ADDED: { icon: '📝', color: '#607D8B' },
  };

  return typeMap[type] || { icon: '📌', color: '#9E9E9E' };
}

export function LeadActivityTimelineGrouped({ leadId }: LeadActivityTimelineGroupedProps) {
  // State: Activities, Loading, Error
  const [activities, setActivities] = useState<Activity[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // State: Welche Gruppe ist ausgeklappt (default: last7Days)
  const [expandedGroup, setExpandedGroup] = useState<string>('last7Days');

  // Fetch Activities from API
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

  // Gruppierte Aktivitäten
  const groupedActivities = useMemo(() => {
    return groupActivitiesByTimeRange(activities);
  }, [activities]);

  const handleGroupChange =
    (group: string) => (event: React.SyntheticEvent, isExpanded: boolean) => {
      setExpandedGroup(isExpanded ? group : '');
    };

  // Render Activity Item
  const renderActivity = (activity: Activity) => {
    const { icon, color } = getActivityTypeInfo(activity.activityType);

    return (
      <ListItem key={activity.id} sx={{ py: 1.5, alignItems: 'flex-start' }}>
        <ListItemAvatar>
          <Avatar sx={{ bgcolor: color, width: 32, height: 32, fontSize: '1rem' }}>{icon}</Avatar>
        </ListItemAvatar>
        <ListItemText
          primary={
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <Typography variant="body2" sx={{ fontWeight: 500 }}>
                {activity.description}
              </Typography>
            </Box>
          }
          secondary={
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 0.5 }}>
              <Typography variant="caption" color="text.secondary">
                {formatDistanceToNow(new Date(activity.activityDate), {
                  addSuffix: true,
                  locale: de,
                })}
              </Typography>
              {activity.userId && (
                <>
                  <Typography variant="caption" color="text.secondary">
                    •
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    {activity.userId}
                  </Typography>
                </>
              )}
            </Box>
          }
        />
      </ListItem>
    );
  };

  // Render Group Accordion
  const renderGroup = (key: string, title: string, activities: Activity[]) => {
    if (activities.length === 0) return null;

    // Preview: Letzte Aktivität
    const lastActivity = activities[0];
    const previewText = lastActivity
      ? `Letzte: ${lastActivity.description.substring(0, 40)}${lastActivity.description.length > 40 ? '...' : ''}`
      : '';

    return (
      <Accordion
        key={key}
        expanded={expandedGroup === key}
        onChange={handleGroupChange(key)}
        sx={{ mb: 1, boxShadow: 'none', border: '1px solid', borderColor: 'divider' }}
      >
        <AccordionSummary expandIcon={<ExpandMoreIcon />}>
          <Box sx={{ flexGrow: 1 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <Typography variant="subtitle2">{title}</Typography>
              <Chip label={activities.length} size="small" sx={{ height: 20 }} />
            </Box>
            {expandedGroup !== key && (
              <Typography
                variant="caption"
                color="text.secondary"
                sx={{ mt: 0.5, display: 'block' }}
              >
                {previewText}
              </Typography>
            )}
          </Box>
        </AccordionSummary>

        <AccordionDetails sx={{ pt: 0 }}>
          <List sx={{ width: '100%', p: 0 }}>{activities.map(renderActivity)}</List>
        </AccordionDetails>
      </Accordion>
    );
  };

  // Loading State
  if (loading) {
    return (
      <Box display="flex" justifyContent="center" p={3}>
        <CircularProgress />
      </Box>
    );
  }

  // Error State
  if (error) {
    return (
      <Alert severity="error" sx={{ m: 2 }}>
        {error}
      </Alert>
    );
  }

  // Empty State
  if (activities.length === 0) {
    return (
      <Typography variant="body2" color="text.secondary" textAlign="center" sx={{ py: 3 }}>
        Noch keine Aktivitäten vorhanden
      </Typography>
    );
  }

  return (
    <Box>
      {renderGroup('last7Days', 'Letzte 7 Tage', groupedActivities.last7Days, true)}
      {renderGroup('last30Days', 'Letzte 30 Tage', groupedActivities.last30Days, false)}
      {renderGroup('older', 'Älter', groupedActivities.older, false)}
    </Box>
  );
}
