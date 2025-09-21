import React from 'react';
import { useEffect, useState } from 'react';
import { getMenu, HelpArticle } from '../../lib/helpApi';
import { Paper, Typography, TextField, List, ListItemButton, ListItemText, Stack } from '@mui/material';

export default function HelpHubPage(){
  const [items, setItems] = useState<HelpArticle[]>([]);
  const [q, setQ] = useState('');
  useEffect(()=>{ getMenu({}).then(setItems).catch(()=>{}); },[]);
  const filtered = q ? items.filter(a=> (a.title||'').toLowerCase().includes(q.toLowerCase()) || (a.keywords||[]).some(k=>k.toLowerCase().includes(q.toLowerCase()))) : items;
  return (
    <Paper sx={{p:2}}>
      <Stack direction="row" spacing={2} alignItems="center" sx={{mb:2}}>
        <Typography variant="h6">Hilfe & Support</Typography>
        <TextField size="small" placeholder="Suchen…" value={q} onChange={e=>setQ(e.target.value)} />
      </Stack>
      <List>
        {filtered.map(a=> (
          <ListItemButton key={a.id} component="a" href={a.cta?.href || '#'}>
            <ListItemText primary={a.title} secondary={a.summary} />
          </ListItemButton>
        ))}
      </List>
    </Paper>
  );
}
