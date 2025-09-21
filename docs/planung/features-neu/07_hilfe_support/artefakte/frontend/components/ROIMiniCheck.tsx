import React from 'react';
import { postRoiQuick } from '../../lib/helpApi';
import { Paper, Typography, TextField, Button, Stack, Alert } from '@mui/material';

export default function ROIMiniCheck(){
  const [accountId, setAccountId] = React.useState('');
  const [hours, setHours] = React.useState<number>(1);
  const [rate, setRate] = React.useState<number>(25);
  const [days, setDays] = React.useState<number>(22);
  const [waste, setWaste] = React.useState<number>(0);
  const [result, setResult] = React.useState<any>(null);
  const [error, setError] = React.useState<string>('');
  const calc = async ()=>{
    setError(''); setResult(null);
    try{
      const res = await postRoiQuick({ accountId, hoursSavedPerDay: hours, hourlyRate: rate, workingDaysPerMonth: days, wasteReductionPerMonth: waste });
      setResult(res);
    }catch(e:any){ setError(e.message || 'Fehler'); }
  };
  return (
    <Paper sx={{p:2}}>
      <Typography variant="h6">ROI Quick‑Check</Typography>
      {error && <Alert severity="error" sx={{mb:1}}>{error}</Alert>}
      <Stack spacing={2}>
        <TextField label="Kunden-ID (UUID)" value={accountId} onChange={e=>setAccountId(e.target.value)} size="small" />
        <TextField label="Stundenersparnis/Tag" type="number" value={hours} onChange={e=>setHours(Number(e.target.value))} size="small" />
        <TextField label="Stundensatz (€)" type="number" value={rate} onChange={e=>setRate(Number(e.target.value))} size="small" />
        <TextField label="Arbeitstage/Monat" type="number" value={days} onChange={e=>setDays(Number(e.target.value))} size="small" />
        <TextField label="Waste‑Reduktion/Monat (€)" type="number" value={waste} onChange={e=>setWaste(Number(e.target.value))} size="small" />
        <Button variant="contained" onClick={calc}>Berechnen</Button>
        {result && <pre>{JSON.stringify(result, null, 2)}</pre>}
      </Stack>
    </Paper>
  );
}
