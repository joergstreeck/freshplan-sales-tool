import React from 'react';
import { useUsers, useCreateUser, useReplaceClaims, User } from '../hooks/useAdminUsers';
import { Paper, Typography, TextField, Button, Stack, Dialog, DialogTitle, DialogContent, DialogActions, Chip } from '@mui/material';

export default function UserManagementPage(){
  const [q, setQ] = React.useState('');
  const [open, setOpen] = React.useState(false);
  const [email, setEmail] = React.useState('');
  const [name, setName] = React.useState('');
  const [roles, setRoles] = React.useState<string[]>(['rep']);
  const list = useUsers({ q, limit: 50 });
  const create = useCreateUser();
  const replace = useReplaceClaims();

  return (
    <Paper sx={{p:2}}>
      <Stack direction="row" spacing={2} alignItems="center" sx={{mb:2}}>
        <Typography variant="h6">User Management</Typography>
        <TextField size="small" placeholder="Suche…" value={q} onChange={e=>setQ(e.target.value)} />
        <Button variant="contained" onClick={()=>setOpen(true)}>Neu</Button>
      </Stack>

      <div>
        {(list.data||[]).map((u:User)=> (
          <Stack key={u.id} direction="row" spacing={2} sx={{py:1, borderBottom:'1px solid var(--mui-palette-divider)'}}>
            <Typography sx={{width:260}}>{u.email}</Typography>
            <Typography sx={{width:200}}>{u.name}</Typography>
            <Stack direction="row" spacing={1}>{(u.roles||[]).map(r=><Chip key={r} label={r}/> )}</Stack>
          </Stack>
        ))}
      </div>

      <Dialog open={open} onClose={()=>setOpen(false)}>
        <DialogTitle>Neuer Benutzer</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{mt:1}}>
            <TextField label="E-Mail" value={email} onChange={e=>setEmail(e.target.value)} />
            <TextField label="Name" value={name} onChange={e=>setName(e.target.value)} />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={()=>setOpen(false)}>Abbrechen</Button>
          <Button onClick={async ()=>{ await create.mutateAsync({email,name,roles}); setOpen(false); }}>Anlegen</Button>
        </DialogActions>
      </Dialog>
    </Paper>
  );
}
