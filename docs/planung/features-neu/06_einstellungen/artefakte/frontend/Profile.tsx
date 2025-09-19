import React from 'react';
import { useSettings } from '../../src/context/SettingsContext';
import { Paper, Typography } from '@mui/material';

export default function ProfilePage(){
  const { settings } = useSettings();
  return (
    <Paper sx={{p:2}}>
      <Typography variant="h6">Profil</Typography>
      <pre>{JSON.stringify({ locale: settings['locale'] }, null, 2)}</pre>
    </Paper>
  );
}
