import React from 'react';
import { useSettings } from '../../src/context/SettingsContext';
import { Paper, Typography, ToggleButtonGroup, ToggleButton } from '@mui/material';

export default function AppearancePage(){
  const { mode } = useSettings();
  const [m, setM] = React.useState(mode);
  return (
    <Paper sx={{p:2}}>
      <Typography variant="h6">Appearance</Typography>
      <ToggleButtonGroup value={m} exclusive onChange={(_,v)=> v && setM(v)}>
        <ToggleButton value="light">Light</ToggleButton>
        <ToggleButton value="dark">Dark</ToggleButton>
      </ToggleButtonGroup>
    </Paper>
  );
}
