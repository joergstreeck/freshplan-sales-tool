import React from 'react';
import { Box, Chip } from '@mui/material';
export default function SegmentFilterBar({ territories, channels }:{ territories:string[]; channels:string[] }){
  return (
    <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', my: 1 }}>
      {territories.map(t => <Chip key={t} label={t} variant="outlined" />)}
      {channels.map(c => <Chip key={c} label={c} color={c==='DIRECT'?'primary':'secondary'} />)}
    </Box>
  );
}
