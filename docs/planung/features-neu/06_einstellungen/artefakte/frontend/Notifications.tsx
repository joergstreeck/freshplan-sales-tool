import React from 'react';
import { useSettings } from '../../src/context/SettingsContext';
import { Paper, Typography, Chip } from '@mui/material';

export default function NotificationsPage(){
  const { settings } = useSettings();
  const followups = settings['sla.sample.followups'] || [];
  return (
    <Paper sx={{p:2}}>
      <Typography variant="h6">Benachrichtigungen & SLA</Typography>
      <Typography variant="body2">Follow‑ups nach Sample‑Lieferung:</Typography>
      <div>{followups.map((f:string)=> <Chip key={f} label={f} sx={{mr:1, mt:1}} />)}</div>
    </Paper>
  );
}
