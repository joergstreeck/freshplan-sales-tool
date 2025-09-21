import React from 'react';
import { Chip, Box, Typography } from '@mui/material';

type Customer = { id: string; name: string; territory: string; type: 'HOTEL'|'RESTAURANT'|'BETRIEBSGASTRONOMIE'|'CATERING' };

const typeColor: Record<Customer['type'], 'primary'|'secondary'> = {
  HOTEL: 'secondary',
  RESTAURANT: 'primary',
  BETRIEBSGASTRONOMIE: 'secondary',
  CATERING: 'primary'
};

export default function CustomerList({ items }: { items: Customer[] }) {
  return (
    <Box>
      <Typography variant="h2" sx={{ mb: 2 }}>Alle Kunden</Typography>
      {items.map(c => (
        <Box key={c.id} sx={{ p: 1, mb: 1, borderRadius: '10px', boxShadow: 'var(--shadow-sm)' }}>
          <Typography variant="h6">{c.name}</Typography>
          <Chip label={c.type} color={typeColor[c.type]} sx={{ mr: 1 }} />
          <Chip label={c.territory} variant="outlined" />
        </Box>
      ))}
    </Box>
  );
}
