import React, { useState } from 'react';
import { Box, Typography, Divider, Alert } from '@mui/material';
import ReplyComposer from './ReplyComposer';
import type { ThreadItem } from '../../types/communication';

export default function ThreadDetail({ thread }: { thread: ThreadItem }){
  const [error, setError] = useState<string|null>(null);
  return (
    <Box>
      <Typography variant="h5">{thread.subject}</Typography>
      {error && <Alert severity="error">{error}</Alert>}
      <Divider sx={{my:2}}/>
      {/* Messages list would be fetched & rendered here (omitted for brevity) */}
      <ReplyComposer thread={thread} onError={setError} onSent={()=>{ /* refresh messages */ }}/>
    </Box>
  );
}
