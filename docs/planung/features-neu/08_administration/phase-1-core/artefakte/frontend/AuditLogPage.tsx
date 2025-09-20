import React from 'react';
import { useAudit, exportAudit } from '../hooks/useAdminAudit';
import { Paper, Typography, TextField, Button, Stack } from '@mui/material';

export default function AuditLogPage(){
  const [q, setQ] = React.useState('');
  const list = useAudit({ q });
  const onExport = async ()=>{
    const text = await exportAudit();
    const blob = new Blob([text], { type:'application/jsonl' });
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = 'admin_audit.jsonl';
    a.click();
  };
  return (
    <Paper sx={{p:2}}>
      <Stack direction="row" spacing={2} alignItems="center" sx={{mb:2}}>
        <Typography variant="h6">Audit Log</Typography>
        <TextField size="small" placeholder="Suche…" value={q} onChange={e=>setQ(e.target.value)} />
        <Button variant="outlined" onClick={onExport}>Export</Button>
      </Stack>
      <div>
        {(list.data||[]).map(ev=> (
          <Stack key={ev.id} direction="row" spacing={2} sx={{py:1, borderBottom:'1px solid var(--mui-palette-divider)'}}>
            <Typography sx={{width:160}}>{new Date(ev.createdAt).toLocaleString()}</Typography>
            <Typography sx={{width:160}}>{ev.riskTier}</Typography>
            <Typography sx={{width:200}}>{ev.action}</Typography>
            <Typography sx={{width:240}}>{ev.resourceType} {ev.resourceId}</Typography>
          </Stack>
        ))}
      </div>
    </Paper>
  );
}
