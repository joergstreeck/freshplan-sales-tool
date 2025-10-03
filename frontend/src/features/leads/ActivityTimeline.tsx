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
  Chip,
  Alert,
  ToggleButtonGroup,
  ToggleButton,
} from '@mui/material';
import {
  CheckCircle as ProgressIcon,
  RadioButtonUnchecked as NonProgressIcon,
  Warning as WarningIcon,
} from '@mui/icons-material';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import type { LeadActivity } from './types';

interface ActivityTimelineProps {
  activities: LeadActivity[];
  progressDeadline?: string; // ISO 8601 timestamp
  variant?: 'full' | 'compact';
}

type FilterMode = 'all' | 'progress' | 'non-progress';

export default function ActivityTimeline({
  activities,
  progressDeadline,
  variant = 'full',
}: ActivityTimelineProps) {
  const { t } = useTranslation('leads');
  const [filterMode, setFilterMode] = useState<FilterMode>('all');

  // Split activities by countsAsProgress
  const progressActivities = activities.filter((a) => a.countsAsProgress);
  const nonProgressActivities = activities.filter((a) => !a.countsAsProgress);

  // Filter logic
  const filteredActivities =
    filterMode === 'progress'
      ? progressActivities
      : filterMode === 'non-progress'
        ? nonProgressActivities
        : activities;

  // Sort by date (newest first)
  const sortedActivities = [...filteredActivities].sort(
    (a, b) => new Date(b.activityDate).getTime() - new Date(a.activityDate).getTime()
  );

  // Find latest progress activity
  const latestProgressActivity = progressActivities.sort(
    (a, b) => new Date(b.activityDate).getTime() - new Date(a.activityDate).getTime()
  )[0];

  // Calculate days since last progress
  const daysSinceLastProgress = latestProgressActivity
    ? Math.floor(
        (new Date().getTime() - new Date(latestProgressActivity.activityDate).getTime()) /
          (1000 * 60 * 60 * 24)
      )
    : null;

  // Warning if progressDeadline < 7 days
  const showWarning = progressDeadline
    ? new Date(progressDeadline).getTime() - new Date().getTime() < 7 * 24 * 60 * 60 * 1000
    : false;

  // Get activity type label (i18n)
  const getActivityTypeLabel = (type: string): string => {
    return t(`activityTypes.${type}`, { defaultValue: type });
  };

  return (
    <Box>
      {/* Filter Controls */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h6" component="h3">
          {t('timeline.title')}
        </Typography>

        <ToggleButtonGroup
          value={filterMode}
          exclusive
          onChange={(_, newMode) => newMode && setFilterMode(newMode)}
          size="small"
          aria-label="Aktivitäten-Filter"
        >
          <ToggleButton value="all" aria-label="Alle Aktivitäten">
            {t('timeline.filterAll')} ({activities.length})
          </ToggleButton>
          <ToggleButton value="progress" aria-label="Nur Progress-Aktivitäten">
            {t('timeline.filterProgress')} ({progressActivities.length})
          </ToggleButton>
          <ToggleButton value="non-progress" aria-label="Sonstige Aktivitäten">
            {t('timeline.filterOther')} ({nonProgressActivities.length})
          </ToggleButton>
        </ToggleButtonGroup>
      </Box>

      {/* Warning Badge if progressDeadline < 7 days */}
      {showWarning && progressDeadline && (
        <Alert severity="warning" icon={<WarningIcon />} sx={{ mb: 2 }}>
          <strong>{t('timeline.warning.label')}</strong> {t('timeline.warning.deadline')}{' '}
          {new Date(progressDeadline).toLocaleDateString('de-DE', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
          })}
          . {t('timeline.warning.action')}
        </Alert>
      )}

      {/* Progress Summary */}
      {variant === 'full' && daysSinceLastProgress !== null && (
        <Box sx={{ mb: 2, p: 2, bgcolor: 'grey.50', borderRadius: 1 }}>
          <Typography variant="body2" color="text.secondary">
            <strong>{t('timeline.summary.lastProgress')}</strong>{' '}
            {latestProgressActivity
              ? `${t('timeline.summary.daysAgo', { count: daysSinceLastProgress })} (${getActivityTypeLabel(latestProgressActivity.activityType)})`
              : t('timeline.summary.noProgress')}
          </Typography>
          {progressDeadline && (
            <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
              <strong>{t('timeline.summary.progressDeadline')}</strong>{' '}
              {new Date(progressDeadline).toLocaleDateString('de-DE', {
                day: '2-digit',
                month: '2-digit',
                year: 'numeric',
              })}
            </Typography>
          )}
        </Box>
      )}

      {/* Timeline */}
      {sortedActivities.length === 0 ? (
        <Box textAlign="center" py={4}>
          <Typography variant="body2" color="text.secondary">
            {t('timeline.empty')}
          </Typography>
        </Box>
      ) : (
        <Timeline position={variant === 'compact' ? 'right' : 'alternate'}>
          {sortedActivities.map((activity, index) => {
            const isProgressActivity = activity.countsAsProgress;
            const isLatestProgress = latestProgressActivity?.id === activity.id;

            return (
              <TimelineItem key={activity.id}>
                {variant === 'full' && (
                  <TimelineOppositeContent color="text.secondary" sx={{ flex: 0.3 }}>
                    <Typography variant="caption">
                      {new Date(activity.activityDate).toLocaleDateString('de-DE', {
                        day: '2-digit',
                        month: 'short',
                        year: 'numeric',
                      })}
                    </Typography>
                    <Typography variant="caption" display="block">
                      {new Date(activity.activityDate).toLocaleTimeString('de-DE', {
                        hour: '2-digit',
                        minute: '2-digit',
                      })}
                    </Typography>
                  </TimelineOppositeContent>
                )}

                <TimelineSeparator>
                  <TimelineDot
                    color={isProgressActivity ? 'success' : 'grey'}
                    variant={isLatestProgress ? 'filled' : 'outlined'}
                  >
                    {isProgressActivity ? (
                      <ProgressIcon fontSize="small" />
                    ) : (
                      <NonProgressIcon fontSize="small" />
                    )}
                  </TimelineDot>
                  {index < sortedActivities.length - 1 && <TimelineConnector />}
                </TimelineSeparator>

                <TimelineContent>
                  <Box
                    sx={{
                      p: 1.5,
                      bgcolor: isProgressActivity ? 'success.50' : 'grey.50',
                      borderRadius: 1,
                      border: isLatestProgress ? '2px solid' : '1px solid',
                      borderColor: isLatestProgress ? 'success.main' : 'divider',
                    }}
                  >
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 0.5 }}>
                      <Typography variant="subtitle2" component="div">
                        {getActivityTypeLabel(activity.activityType)}
                      </Typography>
                      {isProgressActivity && (
                        <Chip label={t('timeline.labels.progress')} color="success" size="small" />
                      )}
                      {isLatestProgress && (
                        <Chip label={t('timeline.labels.latest')} color="primary" size="small" variant="outlined" />
                      )}
                    </Box>

                    {activity.summary && (
                      <Typography variant="body2" sx={{ mb: 0.5 }}>
                        {activity.summary}
                      </Typography>
                    )}

                    {activity.outcome && (
                      <Typography variant="caption" color="text.secondary" display="block">
                        <strong>{t('timeline.labels.outcome')}</strong> {activity.outcome}
                      </Typography>
                    )}

                    {activity.nextAction && (
                      <Typography variant="caption" color="text.secondary" display="block">
                        <strong>{t('timeline.labels.nextAction')}</strong> {activity.nextAction}
                        {activity.nextActionDate && ` (${t('timeline.labels.until')} ${new Date(activity.nextActionDate).toLocaleDateString('de-DE')})`}
                      </Typography>
                    )}

                    {variant === 'compact' && (
                      <Typography variant="caption" color="text.secondary" display="block" sx={{ mt: 0.5 }}>
                        {new Date(activity.activityDate).toLocaleDateString('de-DE', {
                          day: '2-digit',
                          month: '2-digit',
                          year: 'numeric',
                        })}
                      </Typography>
                    )}
                  </Box>
                </TimelineContent>
              </TimelineItem>
            );
          })}
        </Timeline>
      )}
    </Box>
  );
}
