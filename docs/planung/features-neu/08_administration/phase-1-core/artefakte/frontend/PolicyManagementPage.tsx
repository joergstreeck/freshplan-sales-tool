import React from 'react';
import { usePolicies, usePolicyChange } from '../hooks/useAdminPolicies';
import { Paper, Typography, TextField, Button, Stack, MenuItem } from '@mui/material';

export default function PolicyManagementPage(){
  const pol = usePolicies();
  const change = usePolicyChange();
  const [key, setKey] = React.useState('outbox.rate.max');
  const [value, setValue] = React.useState('100');
  const [risk, setRisk] = React.useState<'TIER1'|'TIER2'|'TIER3'>('TIER2');
  const [just, setJust] = React.useState('');
  const [em, setEm] = React.useState(false);

  const submit = async ()=>{
    let v:any = value;
    try { v = JSON.parse(value); } catch {}
    await change.mutateAsync({ key, value: v, riskTier: risk, justification: just, emergency: em });
    alert('Policy Change requested');
  };

  return (
    <Paper sx={{p:2}}>
      <Typography variant="h6">Policies & Approvals</Typography>
      <Stack spacing={2} sx={{mt:2, maxWidth:560}}>
        <TextField label="Key" value={key} onChange={e=>setKey(e.target.value)} />
        <TextField label="Value (JSON/String)" value={value} onChange={e=>setValue(e.target.value)} />
        <TextField select label="Risk Tier" value={risk} onChange={e=>setRisk(e.target.value as any)}>
          <MenuItem value="TIER1">TIER1</MenuItem>
          <MenuItem value="TIER2">TIER2</MenuItem>
          <MenuItem value="TIER3">TIER3</MenuItem>
        </TextField>
        <TextField label="Begründung" value={just} onChange={e=>setJust(e.target.value)} />
        <Button variant="contained" onClick={submit}>Änderung anstoßen</Button>
      </Stack>
    </Paper>
  );
}
