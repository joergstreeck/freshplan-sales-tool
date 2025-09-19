import React from 'react';
import { Box, Typography, Grid, Paper } from '@mui/material';

export default function OpportunityDashboard({ kpis }: { kpis: { roiPipeline:number; demoToClosePct:number; seasonalOpen:number } }) {
  return (
    <Box>
      <Typography variant="h2" sx={{ mb: 2 }}>Verkaufschancen</Typography>
      <Grid container spacing={2}>
        <Grid item xs={12} md={4}><Paper sx={{ p:2 }}><Typography>ROI-Pipeline</Typography><Typography variant="h4">{kpis.roiPipeline.toFixed(0)} €</Typography></Paper></Grid>
        <Grid item xs={12} md={4}><Paper sx={{ p:2 }}><Typography>Demo-to-Close</Typography><Typography variant="h4">{kpis.demoToClosePct.toFixed(1)}%</Typography></Paper></Grid>
        <Grid item xs={12} md={4}><Paper sx={{ p:2 }}><Typography>Seasonal Window Open</Typography><Typography variant="h4">{kpis.seasonalOpen}</Typography></Paper></Grid>
      </Grid>
    </Box>
  );
}
