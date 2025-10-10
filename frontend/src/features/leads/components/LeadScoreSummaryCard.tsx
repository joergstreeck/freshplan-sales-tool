import { Card, CardContent, Box, Typography, LinearProgress, Grid, Chip } from '@mui/material';

interface LeadScoreSummaryCardProps {
  leadScore?: number;
  painScore?: number;
  revenueScore?: number;
  fitScore?: number;
  engagementScore?: number;
}

function getScoreColor(score: number): 'success' | 'warning' | 'error' {
  if (score >= 70) return 'success';
  if (score >= 40) return 'warning';
  return 'error';
}

function getScoreIcon(score: number): string {
  if (score >= 70) return '✅';
  if (score >= 40) return '⚠️';
  return '❌';
}

function MiniScoreChip({ icon, label, score }: { icon: string; label: string; score: number }) {
  return (
    <Box textAlign="center">
      <Typography variant="caption" display="block" color="text.secondary">
        {icon} {label}
      </Typography>
      <Chip
        label={score}
        color={getScoreColor(score)}
        size="small"
        sx={{ fontWeight: 'bold', mt: 0.5 }}
      />
    </Box>
  );
}

export function LeadScoreSummaryCard({
  leadScore = 0,
  painScore = 0,
  revenueScore = 0,
  fitScore = 0,
  engagementScore = 0,
}: LeadScoreSummaryCardProps) {
  return (
    <Card sx={{ mb: 3, bgcolor: 'primary.light' }}>
      <CardContent>
        {/* Gesamt-Score */}
        <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
          <Typography variant="h5">Lead Score</Typography>
          <Chip
            label={`${leadScore}/100 ${getScoreIcon(leadScore)}`}
            color={getScoreColor(leadScore)}
            size="large"
            sx={{ fontWeight: 'bold' }}
          />
        </Box>

        <LinearProgress
          variant="determinate"
          value={leadScore}
          sx={{ height: 12, borderRadius: 6, mb: 3 }}
          color={getScoreColor(leadScore)}
        />

        {/* Mini-Scores Grid */}
        <Grid container spacing={2}>
          <Grid item xs={3}>
            <MiniScoreChip icon="💰" label="Umsatz (25%)" score={revenueScore} />
          </Grid>
          <Grid item xs={3}>
            <MiniScoreChip icon="🎯" label="Fit (25%)" score={fitScore} />
          </Grid>
          <Grid item xs={3}>
            <MiniScoreChip icon="⚠️" label="Pain (25%)" score={painScore} />
          </Grid>
          <Grid item xs={3}>
            <MiniScoreChip icon="🤝" label="Engagement (25%)" score={engagementScore} />
          </Grid>
        </Grid>
      </CardContent>
    </Card>
  );
}
