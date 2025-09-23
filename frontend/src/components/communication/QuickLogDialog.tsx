import React, { useState } from 'react';
import { Dialog, DialogTitle, DialogContent, DialogActions, TextField, Button, MenuItem } from '@mui/material';

interface QuickLogPayload {
  type: 'CALL' | 'MEETING';
  summary: string;
  occurredAt: string;
}

export default function QuickLogDialog({ open, onClose, onSubmit }:{ open:boolean; onClose:()=>void; onSubmit:(payload:QuickLogPayload)=>Promise<void> }){
  const [type,setType]=useState<'CALL'|'MEETING'>('CALL');
  const [summary,setSummary]=useState('');
  const [occurredAt,setOccurredAt]=useState(new Date().toISOString());
  async function save(){ await onSubmit({ type, summary, occurredAt }); onClose(); }
  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>Quick Log</DialogTitle>
      <DialogContent sx={{ display:'grid', gap:2, mt:1 }}>
        <TextField select label="Typ" value={type} onChange={(e)=>setType(e.target.value as 'CALL' | 'MEETING')}>
          <MenuItem value="CALL">Anruf</MenuItem>
          <MenuItem value="MEETING">Meeting</MenuItem>
        </TextField>
        <TextField label="Zeitpunkt" value={occurredAt} onChange={(e)=>setOccurredAt(e.target.value)} />
        <TextField label="Notizen" value={summary} onChange={(e)=>setSummary(e.target.value)} multiline minRows={3} />
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Abbrechen</Button>
        <Button variant="contained" onClick={save} disabled={!summary.trim()}>Speichern</Button>
      </DialogActions>
    </Dialog>
  );
}
