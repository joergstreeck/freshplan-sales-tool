import React, { useState } from 'react';
import { TextField, Button, Typography, Box, Paper } from '@mui/material';

type Item = { sku: string; rating?: number; comment?: string };
export default function FeedbackFormPublic({ token }: { token: string }) {
  const [contactName, setContactName] = useState('');
  const [role, setRole] = useState<'CHEF'|'BUYER'|'GM'|'OTHER'>('CHEF');
  const [items, setItems] = useState<Item[]>([{ sku: '' }] as Item[]);
  const [ok, setOk] = useState(false);

  const submit = async () => {
    const res = await fetch(`/api/feedback/${encodeURIComponent(token)}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        contact: { name: contactName, role },
        items
      })
    });
    setOk(res.status === 201);
  };

  return (
    <Box sx={{ p: 2, display: 'flex', justifyContent: 'center' }}>
      <Paper sx={{ p: 3, maxWidth: 640, width: '100%' }}>
        <Typography variant="h6" gutterBottom>Cook&Fresh® Produkt-Feedback</Typography>
        <TextField fullWidth label="Ihr Name" value={contactName} onChange={e=>setContactName(e.target.value)} sx={{ mb: 2 }}/>
        <TextField fullWidth label="Rolle (CHEF/BUYER/GM/OTHER)" value={role} onChange={e=>setRole(e.target.value as any)} sx={{ mb: 2 }}/>
        {items.map((it, i)=> (
          <Box key={i} sx={{ display: 'flex', gap: 1, mb: 1 }}>
            <TextField label="SKU" value={it.sku} onChange={e=>{
              const v=[...items]; v[i].sku=e.target.value; setItems(v);
            }}/>
            <TextField type="number" label="Rating 1-5" value={it.rating||''} onChange={e=>{
              const v=[...items]; v[i].rating=Number(e.target.value); setItems(v);
            }}/>
            <TextField label="Kommentar" value={it.comment||''} onChange={e=>{
              const v=[...items]; v[i].comment=e.target.value; setItems(v);
            }} sx={{ flex:1 }}/>
          </Box>
        ))}
        <Box sx={{ display:'flex', gap:1, mt:1 }}>
          <Button onClick={()=>setItems(prev=>[...prev, { sku: '' }])}>+ Produkt</Button>
          <Button variant="contained" onClick={submit}>Absenden</Button>
        </Box>
        {ok && <Typography color="success.main" sx={{ mt:2 }}>Vielen Dank! Ihr Feedback wurde gespeichert.</Typography>}
      </Paper>
    </Box>
  );
}
