import React from 'react';
import { Paper, Box, LinearProgress, Typography } from '@mui/material';

export default function ChannelMixChart({ mix }: { mix: { DIRECT: number; PARTNER: number } }) {
  const total = Math.max(mix.DIRECT + mix.PARTNER, 1);
  const directPct = Math.round((mix.DIRECT / total) * 100);
  const partnerPct = 100 - directPct;
  return (
    <Paper elevation={0} sx={{ p: 2 }}>
      <Typography variant="overline" color="text.secondary">Channel Mix</Typography>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mt: 1 }}>
        <Box sx={{ flex: 1 }}><LinearProgress variant="determinate" value={directPct} /></Box>
        <Typography>Direct {directPct}% / Partner {partnerPct}%</Typography>
      </Box>
    </Paper>
  );
}
