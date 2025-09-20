import React from 'react';
import { postFollowUp } from '../../lib/helpApi';
import { Paper, Typography, TextField, Button, Stack, Alert, Chip } from '@mui/material';

export default function FollowUpWizard(){
  const [accountId, setAccountId] = React.useState('');
  const [note, setNote] = React.useState('');
  const [offs, setOffs] = React.useState<string[]>(['P3D','P7D']);
  const [result, setResult] = React.useState<any>(null);
  const [error, setError] = React.useState<string>('');
  const toggle = (v:string)=> setOffs(s=> s.includes(v) ? s.filter(x=>x!==v) : [...s, v]);
  const save = async ()=>{
    setError(''); setResult(null);
    try{
      const res = await postFollowUp({ accountId, followupOffsets: offs, note });
      setResult(res);
    }catch(e:any){ setError(e.message || 'Fehler'); }
  };
  return (
    <Paper sx={{p:2}}>
      <Typography variant="h6">Follow‑Up planen</Typography>
      {error && <Alert severity="error" sx={{mb:1}}>{error}</Alert>}
      <Stack spacing={2}>
        <TextField label="Kunden-ID (UUID)" value={accountId} onChange={e=>setAccountId(e.target.value)} size="small" />
        <Stack direction="row" spacing={1}>
          {['P3D','P7D','P14D'].map(v=>(<Chip key={v} label={v} color={offs.includes(v)?'primary':'default'} onClick={()=>toggle(v)} />))}
        </Stack>
        <TextField label="Notiz" value={note} onChange={e=>setNote(e.target.value)} size="small" multiline minRows={2} />
        <Button variant="contained" onClick={save}>Plan erstellen</Button>
        {result && <pre>{JSON.stringify(result, null, 2)}</pre>}
      </Stack>
    </Paper>
  );
}
