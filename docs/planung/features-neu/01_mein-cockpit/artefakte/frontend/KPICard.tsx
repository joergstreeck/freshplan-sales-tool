import React from 'react';
import { Paper, Box, Typography } from '@mui/material';

export default function KPICard({ title, value, suffix }: { title: string; value: string|number; suffix?: string }) {
  return (
    <Paper elevation={0} sx={{ p: 2 }}>
      <Typography variant="overline" color="text.secondary">{title}</Typography>
      <Box sx={{ display: 'flex', alignItems: 'baseline', gap: 1 }}>
        <Typography variant="h4" data-testid="kpi-value">{value}</Typography>
        {suffix && <Typography variant="body2" color="text.secondary">{suffix}</Typography>}
      </Box>
    </Paper>
  );
}
