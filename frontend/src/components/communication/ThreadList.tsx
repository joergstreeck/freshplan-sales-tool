import React from 'react';
import { Box, Paper, Typography, Chip } from '@mui/material';
import { useThreads } from '../../hooks/useCommunication';
import type { ThreadItem } from '../../types/communication';

export default function ThreadList({ customerId }: { customerId?: string }) {
  const { data, loading, error } = useThreads({ customerId });
  if (error) return <Paper sx={{ p: 2 }}>Fehler: {error}</Paper>;
  if (loading) return <Paper sx={{ p: 2 }}>Lade Threads…</Paper>;
  return (
    <Box>
      {data.items.map((t: ThreadItem) => (
        <Paper key={t.id} sx={{ p: 2, mb: 1, borderRadius: '10px', boxShadow: 'var(--shadow-sm)' }}>
          <Typography variant="h6">{t.subject}</Typography>
          <Typography variant="body2" color="text.secondary">
            {new Date(t.lastMessageAt).toLocaleString()} • {t.territory}
          </Typography>
          <Chip
            size="small"
            label={t.channel}
            color={t.channel === 'EMAIL' ? 'primary' : 'secondary'}
            sx={{ ml: 1 }}
          />
          {t.unreadCount > 0 && <Chip size="small" label={`${t.unreadCount} neu`} sx={{ ml: 1 }} />}
        </Paper>
      ))}
    </Box>
  );
}
