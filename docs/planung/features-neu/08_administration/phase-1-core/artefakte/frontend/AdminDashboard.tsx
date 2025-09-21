import React from 'react';
import { Paper, Typography, Grid, Card, CardContent, Button } from '@mui/material';

export default function AdminDashboard(){
  const cards = [
    { title: 'Users', href: '/admin/users', desc: 'User Management & Claims' },
    { title: 'Audit', href: '/admin/audit', desc: 'Events, Search & Export' },
    { title: 'Email Deliverability', href: '/admin/ops/email', desc: 'SMTP, Rate Limits, Bounce' },
    { title: 'DSGVO', href: '/admin/ops/dsgvo', desc: 'Export/Delete Requests' },
    { title: 'Policies', href: '/admin/security', desc: 'ABAC & Approvals' }
  ];
  return (
    <Paper sx={{p:2}}>
      <Typography variant="h6" sx={{mb:2}}>Administration</Typography>
      <Grid container spacing={2}>
        {cards.map(c=> (
          <Grid key={c.title} item xs={12} md={4}>
            <Card><CardContent>
              <Typography variant="subtitle1">{c.title}</Typography>
              <Typography variant="body2" sx={{mb:1}}>{c.desc}</Typography>
              <Button href={c.href} variant="contained">Öffnen</Button>
            </CardContent></Card>
          </Grid>
        ))}
      </Grid>
    </Paper>
  );
}
