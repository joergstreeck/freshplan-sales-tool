import React from 'react';
import { Box, Grid, Typography } from '@mui/material';
import KPICard from './KPICard';
import ChannelMixChart from './ChannelMixChart';
import ROICalculator from './ROICalculator';
import ChannelBadge from './ChannelBadge';
import { useCockpitSummary, useCockpitFilters } from '../../hooks/useCockpitData';

export default function MultiChannelDashboard() {
  const { data: filters } = useCockpitFilters();
  const { data: summary, loading, error } = useCockpitSummary({ range: '30d' });

  if (error) return <div>Fehler: {error}</div>;
  if (loading || !summary) return <div>Lade Cockpit…</div>;

  return (
    <Box>
      <Typography variant="h2" sx={{ mb: 2 }}>Multi-Channel Sales Cockpit</Typography>
      <Grid container spacing={2}>
        <Grid item xs={12} md={3}><KPICard title="Sample Success" value={`${summary.sampleSuccessRatePct.toFixed(1)} %`} /></Grid>
        <Grid item xs={12} md={3}><KPICard title="ROI-Pipeline" value={`${Math.round(summary.roiPipelineValue)} €`} /></Grid>
        <Grid item xs={12} md={3}><KPICard title="Partner Share" value={`${summary.partnerSharePct.toFixed(1)} %`} /></Grid>
        <Grid item xs={12} md={3}><KPICard title="At Risk" value={summary.atRiskCustomers} /></Grid>
        <Grid item xs={12} md={6}><ChannelMixChart mix={summary.channelMix} /></Grid>
        <Grid item xs={12} md={6}><ROICalculator /></Grid>
      </Grid>
      <Box sx={{ mt: 2, display: 'flex', gap: 1 }}>
        <ChannelBadge channel="DIRECT" />
        <ChannelBadge channel="PARTNER" />
      </Box>
    </Box>
  );
}
