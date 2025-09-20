import React from 'react';
import { Paper, Typography, TextField, Button, Stack } from '@mui/material';

export default function DsgvoWorkflowPage(){
  const [subjectId, setSubjectId] = React.useState('');
  const [justification, setJustification] = React.useState('');
  const queue = async (type:'export'|'delete')=>{
    const r = await fetch(`/api/admin/ops/dsar/${type}`, { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify({ subjectId, justification }) });
    if(!r.ok) alert('Fehler'); else alert('Queued');
  };
  return (
    <Paper sx={{p:2}}>
      <Typography variant="h6">DSGVO Workflows</Typography>
      <Stack spacing={2} sx={{mt:2, maxWidth:560}}>
        <TextField label="Subject ID (UUID)" value={subjectId} onChange={e=>setSubjectId(e.target.value)} />
        <TextField label="Begründung" value={justification} onChange={e=>setJustification(e.target.value)} />
        <Stack direction="row" spacing={2}>
          <Button variant="contained" onClick={()=>queue('export')}>Export anfordern</Button>
          <Button variant="outlined" onClick={()=>queue('delete')}>Löschung anfordern</Button>
        </Stack>
      </Stack>
    </Paper>
  );
}
