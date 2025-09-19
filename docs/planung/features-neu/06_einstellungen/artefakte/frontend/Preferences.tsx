import React from 'react';
import { useSettings } from '../../src/context/SettingsContext';
import { Paper, Typography, FormControlLabel, Switch } from '@mui/material';

export default function PreferencesPage(){
  const { settings } = useSettings();
  const channels = settings['notifications.channels'] || {};
  return (
    <Paper sx={{p:2}}>
      <Typography variant="h6">Präferenzen</Typography>
      <FormControlLabel control={<Switch checked={!!channels.email} />} label="E‑Mail Benachrichtigungen" />
      <FormControlLabel control={<Switch checked={!!channels.phone} />} label="Telefon Benachrichtigungen" />
      <FormControlLabel control={<Switch checked={!!channels.sms} />} label="SMS Benachrichtigungen" />
    </Paper>
  );
}
