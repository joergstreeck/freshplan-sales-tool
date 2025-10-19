import { Box, LinearProgress, Typography, Tooltip, useTheme } from '@mui/material';
import { TrendingUp, TrendingDown, TrendingFlat } from '@mui/icons-material';

interface LeadScoreIndicatorProps {
  score?: number;
  size?: 'small' | 'medium' | 'large';
  showLabel?: boolean;
  showTrend?: boolean;
}

/**
 * Lead Score Indicator (Sprint 2.1.6 Phase 4 - ADR-006 Phase 2)
 *
 * Visualisiert Lead-Qualität 0-100 Punkte:
 * - 0-39: Niedrig (rot)
 * - 40-69: Mittel (gelb)
 * - 70-100: Hoch (grün - FreshFoodz CI)
 *
 * Berechnung (Backend):
 * - Umsatzpotenzial: 25%
 * - Engagement: 25%
 * - Fit: 25%
 * - Dringlichkeit: 25%
 */
export default function LeadScoreIndicator({
  score,
  size = 'medium',
  showLabel = true,
  showTrend = false,
}: LeadScoreIndicatorProps) {
  const theme = useTheme();

  if (score === undefined || score === null) {
    return (
      <Box display="flex" alignItems="center" gap={1}>
        <Typography variant="body2" color="text.secondary">
          Noch nicht bewertet
        </Typography>
      </Box>
    );
  }

  // Score-basierte Farbe
  const getScoreColor = (value: number): string => {
    if (value < 40) return theme.palette.error.main; // Rot
    if (value < 70) return theme.palette.warning.main; // Orange
    return theme.palette.primary.main; // FreshFoodz Grün
  };

  // Score-basiertes Label
  const getScoreLabel = (value: number): string => {
    if (value < 40) return 'Niedrig';
    if (value < 70) return 'Mittel';
    return 'Hoch';
  };

  // Trend-Icon (optional, für spätere Erweiterung)
  const getTrendIcon = () => {
    // Placeholder: könnte später mit historischen Daten verglichen werden
    if (score >= 70) return <TrendingUp fontSize="small" sx={{ color: 'primary.main' }} />;
    if (score < 40) return <TrendingDown fontSize="small" sx={{ color: 'error.main' }} />;
    return <TrendingFlat fontSize="small" sx={{ color: 'warning.main' }} />;
  };

  const scoreColor = getScoreColor(score);
  const scoreLabel = getScoreLabel(score);

  const progressHeight = size === 'small' ? 6 : size === 'large' ? 12 : 8;
  const fontSize = size === 'small' ? '0.75rem' : size === 'large' ? '1rem' : '0.875rem';

  return (
    <Tooltip
      title={
        <Box>
          <Typography variant="caption" display="block">
            <strong>Lead-Score: {score}/100</strong>
          </Typography>
          <Typography variant="caption" display="block">
            Qualität: {scoreLabel}
          </Typography>
          <Typography variant="caption" display="block" sx={{ mt: 0.5 }}>
            Berechnet aus: Umsatzpotenzial (25%), Engagement (25%), Fit (25%), Dringlichkeit (25%)
          </Typography>
        </Box>
      }
      arrow
    >
      <Box display="flex" alignItems="center" gap={1}>
        {showLabel && (
          <Typography
            variant="body2"
            sx={{
              fontSize,
              fontWeight: 600,
              color: scoreColor,
              minWidth: size === 'small' ? '40px' : '60px',
            }}
          >
            {score}/100
          </Typography>
        )}

        <Box flex={1} minWidth={size === 'small' ? '60px' : '100px'}>
          <LinearProgress
            variant="determinate"
            value={score}
            sx={{
              height: progressHeight,
              borderRadius: 1,
              backgroundColor: 'grey.300',
              '& .MuiLinearProgress-bar': {
                backgroundColor: scoreColor,
                borderRadius: 1,
              },
            }}
          />
        </Box>

        {showTrend && getTrendIcon()}
      </Box>
    </Tooltip>
  );
}
