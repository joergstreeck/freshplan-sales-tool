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
  Avatar,
  Chip,
  CircularProgress,
  Alert,
  Tooltip,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Button,
} from '@mui/material';
import {
  ExpandMore as ExpandMoreIcon,
  Phone as PhoneIcon,
  Email as EmailIcon,
  Event as EventIcon,
  Description as DescriptionIcon,
  PersonAdd as PersonAddIcon,
  LocalShipping as LocalShippingIcon,
  Assignment as AssignmentIcon,
  CheckCircle as CheckCircleIcon,
  Cancel as CancelIcon,
  Pause as PauseIcon,
  PlayArrow as PlayArrowIcon,
  Star as StarIcon,
  Sync as SyncIcon,
  Handshake as HandshakeIcon,
  PushPin as PushPinIcon,
} from '@mui/icons-material';
import { formatDistanceToNow, format } from 'date-fns';
import { de } from 'date-fns/locale';

interface Activity {
  id: number;
  activityType: string;
  activityDate: string;
  description?: string;
  userId: string;
  isMeaningfulContact?: boolean;
  outcome?: string; // Sprint 2.1.7 Issue #126: ActivityOutcome
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

// Activity Type Icons & Colors (Material-UI Icons)
function getActivityTypeInfo(type: string): { icon: React.ReactElement; color: string } {
  const typeMap: Record<string, { icon: React.ReactElement; color: string }> = {
    // Lead lifecycle events
    LEAD_CREATED: { icon: <StarIcon />, color: '#94C456' },
    CREATED: { icon: <StarIcon />, color: '#94C456' },
    LEAD_ASSIGNED: { icon: <PersonAddIcon />, color: '#2196F3' },

    // Communication activities
    CALL: { icon: <PhoneIcon />, color: '#2196F3' },
    EMAIL: { icon: <EmailIcon />, color: '#00BCD4' },
    MEETING: { icon: <EventIcon />, color: '#9C27B0' },
    MEETING_SCHEDULED: { icon: <EventIcon />, color: '#9C27B0' },

    // Contact & Documentation
    FIRST_CONTACT_DOCUMENTED: { icon: <HandshakeIcon />, color: '#4CAF50' },
    CONTACT_ADDED: { icon: <PersonAddIcon />, color: '#00BCD4' },
    NOTE: { icon: <DescriptionIcon />, color: '#607D8B' },
    NOTE_ADDED: { icon: <DescriptionIcon />, color: '#607D8B' },

    // Sales activities
    SAMPLE_SENT: { icon: <LocalShippingIcon />, color: '#FF9800' },
    PROPOSAL_SENT: { icon: <AssignmentIcon />, color: '#9C27B0' },

    // Status changes
    STATUS_CHANGED: { icon: <SyncIcon />, color: '#FF9800' },
    QUALIFIED: { icon: <CheckCircleIcon />, color: '#4CAF50' },
    DISQUALIFIED: { icon: <CancelIcon />, color: '#F44336' },

    // Clock management
    CLOCK_STOPPED: { icon: <PauseIcon />, color: '#FF9800' },
    CLOCK_RESUMED: { icon: <PlayArrowIcon />, color: '#4CAF50' },
  };

  return typeMap[type] || { icon: <PushPinIcon />, color: '#9E9E9E' };
}

// Sprint 2.1.7 Issue #126: ActivityOutcome Badge Info
function getOutcomeInfo(outcome: string): { label: string; color: string; icon: string } {
  const outcomeMap: Record<string, { label: string; color: string; icon: string }> = {
    SUCCESSFUL: { label: 'Erfolgreich', color: '#4CAF50', icon: '✅' },
    UNSUCCESSFUL: { label: 'Nicht erfolgreich', color: '#F44336', icon: '❌' },
    NO_ANSWER: { label: 'Keine Antwort', color: '#FF9800', icon: '📵' },
    CALLBACK_REQUESTED: { label: 'Rückruf gewünscht', color: '#2196F3', icon: '🔄' },
    INFO_SENT: { label: 'Info versendet', color: '#00BCD4', icon: '📧' },
    QUALIFIED: { label: 'Qualifiziert', color: '#94C456', icon: '⭐' },
    DISQUALIFIED: { label: 'Disqualifiziert', color: '#9E9E9E', icon: '⛔' },
  };

  return (
    outcomeMap[outcome] || { label: outcome, color: '#757575', icon: '❓' }
  );
}

export function LeadActivityTimelineGrouped({ leadId }: LeadActivityTimelineGroupedProps) {
  // State: Activities, Loading, Error
  const [activities, setActivities] = useState<Activity[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // State: Welche Gruppe ist ausgeklappt (default: last7Days)
  const [expandedGroup, setExpandedGroup] = useState<string>('last7Days');

  // State: Filter
  const [typeFilter, setTypeFilter] = useState<string>('ALL');
  const [outcomeFilter, setOutcomeFilter] = useState<string>('ALL');

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

  // Gefilterte Aktivitäten
  const filteredActivities = useMemo(() => {
    return activities.filter(activity => {
      // Filter nach Activity Type
      if (typeFilter !== 'ALL' && activity.activityType !== typeFilter) {
        return false;
      }
      // Filter nach Outcome
      if (outcomeFilter !== 'ALL' && activity.outcome !== outcomeFilter) {
        return false;
      }
      return true;
    });
  }, [activities, typeFilter, outcomeFilter]);

  // Gruppierte Aktivitäten (auf Basis der gefilterten Liste)
  const groupedActivities = useMemo(() => {
    return groupActivitiesByTimeRange(filteredActivities);
  }, [filteredActivities]);

  const handleGroupChange =
    (group: string) => (event: React.SyntheticEvent, isExpanded: boolean) => {
      setExpandedGroup(isExpanded ? group : '');
    };

  // Render Activity Item
  const renderActivity = (activity: Activity) => {
    const { icon, color } = getActivityTypeInfo(activity.activityType);
    const activityDate = new Date(activity.activityDate);

    return (
      <ListItem key={activity.id} sx={{ py: 1.5, alignItems: 'flex-start', flexDirection: 'column' }}>
        <Box sx={{ display: 'flex', width: '100%', alignItems: 'flex-start' }}>
          <ListItemAvatar>
            <Avatar sx={{ bgcolor: color, width: 32, height: 32 }}>
              {icon}
            </Avatar>
          </ListItemAvatar>
          <Box sx={{ flexGrow: 1 }}>
            <Typography variant="body2" sx={{ fontWeight: 500 }}>
              {activity.description}
            </Typography>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 0.5, flexWrap: 'wrap' }}>
              <Tooltip
                title={format(activityDate, 'dd.MM.yyyy HH:mm', { locale: de })}
                arrow
                placement="top"
              >
                <Typography
                  variant="caption"
                  color="text.secondary"
                  sx={{ cursor: 'help', textDecoration: 'underline dotted' }}
                >
                  {formatDistanceToNow(activityDate, {
                    addSuffix: true,
                    locale: de,
                  })}
                </Typography>
              </Tooltip>
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
              {/* Sprint 2.1.7 Issue #126: Display ActivityOutcome Badge */}
              {activity.outcome && (
                <>
                  <Typography variant="caption" color="text.secondary">
                    •
                  </Typography>
                  <Chip
                    icon={
                      <Box component="span" sx={{ fontSize: '1rem', ml: 0.5 }}>
                        {getOutcomeInfo(activity.outcome).icon}
                      </Box>
                    }
                    label={getOutcomeInfo(activity.outcome).label}
                    size="small"
                    sx={{
                      height: 24,
                      fontSize: '0.75rem',
                      bgcolor: getOutcomeInfo(activity.outcome).color,
                      color: 'white',
                      fontWeight: 600,
                      '& .MuiChip-label': {
                        px: 1,
                      },
                      '& .MuiChip-icon': {
                        color: 'white',
                        fontSize: '1rem',
                      },
                    }}
                  />
                </>
              )}
            </Box>
          </Box>
        </Box>
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

  // Handler: Filter zurücksetzen
  const handleResetFilters = () => {
    setTypeFilter('ALL');
    setOutcomeFilter('ALL');
  };

  return (
    <Box>
      {/* Filter-UI */}
      <Box sx={{ mb: 2, display: 'flex', gap: 2, flexWrap: 'wrap', alignItems: 'center' }}>
        {/* Activity Type Filter */}
        <FormControl sx={{ minWidth: 200 }} size="small">
          <InputLabel>Aktivitätstyp</InputLabel>
          <Select
            value={typeFilter}
            label="Aktivitätstyp"
            onChange={(e) => setTypeFilter(e.target.value)}
          >
            <MenuItem value="ALL">Alle anzeigen</MenuItem>
            <MenuItem value="CALL">📞 Anrufe</MenuItem>
            <MenuItem value="EMAIL">📧 E-Mails</MenuItem>
            <MenuItem value="MEETING">📅 Meetings</MenuItem>
            <MenuItem value="NOTE">📝 Notizen</MenuItem>
            <MenuItem value="FIRST_CONTACT_DOCUMENTED">🤝 Erstkontakt</MenuItem>
            <MenuItem value="SAMPLE_SENT">📦 Muster versendet</MenuItem>
            <MenuItem value="PROPOSAL_SENT">📋 Angebot versendet</MenuItem>
          </Select>
        </FormControl>

        {/* Outcome Filter */}
        <FormControl sx={{ minWidth: 200 }} size="small">
          <InputLabel>Ergebnis</InputLabel>
          <Select
            value={outcomeFilter}
            label="Ergebnis"
            onChange={(e) => setOutcomeFilter(e.target.value)}
          >
            <MenuItem value="ALL">Alle anzeigen</MenuItem>
            <MenuItem value="SUCCESSFUL">✅ Erfolgreich</MenuItem>
            <MenuItem value="UNSUCCESSFUL">❌ Nicht erfolgreich</MenuItem>
            <MenuItem value="NO_ANSWER">📵 Keine Antwort</MenuItem>
            <MenuItem value="CALLBACK_REQUESTED">🔄 Rückruf gewünscht</MenuItem>
            <MenuItem value="INFO_SENT">📧 Info versendet</MenuItem>
            <MenuItem value="QUALIFIED">⭐ Qualifiziert</MenuItem>
            <MenuItem value="DISQUALIFIED">⛔ Disqualifiziert</MenuItem>
          </Select>
        </FormControl>

        {/* Reset Button (conditional) */}
        {(typeFilter !== 'ALL' || outcomeFilter !== 'ALL') && (
          <Button
            variant="outlined"
            size="small"
            onClick={handleResetFilters}
            sx={{ height: 40 }}
          >
            Filter zurücksetzen
          </Button>
        )}

        {/* Count Badge */}
        <Chip
          label={`${filteredActivities.length} von ${activities.length}`}
          size="small"
          color={filteredActivities.length < activities.length ? 'primary' : 'default'}
          sx={{ ml: 'auto' }}
        />
      </Box>

      {/* Timeline Groups */}
      {renderGroup('last7Days', 'Letzte 7 Tage', groupedActivities.last7Days)}
      {renderGroup('last30Days', 'Letzte 30 Tage', groupedActivities.last30Days)}
      {renderGroup('older', 'Älter', groupedActivities.older)}
    </Box>
  );
}
