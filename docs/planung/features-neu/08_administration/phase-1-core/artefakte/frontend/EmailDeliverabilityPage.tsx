import React from 'react';
import { useSmtp, useUpdateSmtp, useOutbox } from '../hooks/useAdminOperations';
import { Paper, Typography, TextField, Button, Stack, Slider, Alert } from '@mui/material';

export default function EmailDeliverabilityPage(){
  const smtp = useSmtp();
  const upd = useUpdateSmtp();
  const outbox = useOutbox();
  const [host, setHost] = React.useState('');
  const [port, setPort] = React.useState(587);
  const [username, setUsername] = React.useState('');
  const [passwordRef, setPasswordRef] = React.useState('secret:smtp/password');
  const [tlsMode, setTlsMode] = React.useState<'STARTTLS'|'TLS'|'NONE'>('STARTTLS');
  const [truststoreRef, setTruststoreRef] = React.useState<string>('secret:truststore');

  React.useEffect(()=>{
    if (smtp.data) {
      setHost(smtp.data.host||''); setPort(smtp.data.port||587); setUsername(smtp.data.username||'');
      setTlsMode(smtp.data.tlsMode||'STARTTLS'); setTruststoreRef(smtp.data.truststoreRef||'secret:truststore');
    }
  }, [smtp.data]);

  const onSave = async ()=>{
    await upd.mutateAsync({ host, port, username, passwordRef, tlsMode, truststoreRef });
  };

  return (
    <Paper sx={{p:2}}>
      <Typography variant="h6">E‑Mail Deliverability</Typography>
      <Stack spacing={2} sx={{mt:2, maxWidth:560}}>
        <TextField label="Host" value={host} onChange={e=>setHost(e.target.value)} />
        <TextField label="Port" type="number" value={port} onChange={e=>setPort(Number(e.target.value))} />
        <TextField label="Username" value={username} onChange={e=>setUsername(e.target.value)} />
        <TextField label="Password Ref" value={passwordRef} onChange={e=>setPasswordRef(e.target.value)} />
        <TextField label="TLS Mode" value={tlsMode} onChange={e=>setTlsMode(e.target.value as any)} />
        <TextField label="Truststore Ref" value={truststoreRef} onChange={e=>setTruststoreRef(e.target.value)} />
        <Button variant="contained" onClick={onSave}>Speichern</Button>
      </Stack>

      <Typography variant="subtitle1" sx={{mt:4}}>Outbox</Typography>
      {outbox.query.data && (
        <Stack spacing={2} sx={{mt:1, maxWidth:560}}>
          <Alert severity={outbox.query.data.paused ? 'warning' : 'success'}>
            {outbox.query.data.paused ? 'Outbox pausiert' : 'Outbox aktiv'} – Backlog: {outbox.query.data.backlog}
          </Alert>
          <Stack direction="row" spacing={2}>
            <Button onClick={outbox.pause} variant="outlined">Pause</Button>
            <Button onClick={outbox.resume} variant="outlined">Resume</Button>
          </Stack>
          <Typography>Rate per minute</Typography>
          <Slider min={0} max={500} value={outbox.query.data.ratePerMin} onChangeCommitted={(_, v)=> outbox.rate(v as number)} />
        </Stack>
      )}
    </Paper>
  );
}
