import React, { useState } from 'react';
import { Box, TextField, Button } from '@mui/material';
import { sendReply } from '../../hooks/useCommunication';
import type { ThreadItem } from '../../types/communication';

export default function ReplyComposer({ thread, onError, onSent }:{ thread: ThreadItem, onError:(m:string)=>void, onSent:()=>void }){
  const [text,setText]=useState('');
  const [busy,setBusy]=useState(false);
  async function send(){
    setBusy(true); onError('');
    try{ await sendReply(thread.id, thread.etag, text); setText(''); onSent(); }
    catch(e:any){ onError(e.message); }
    finally{ setBusy(false); }
  }
  return (
    <Box sx={{ display:'flex', gap:1, mt:1 }}>
      <TextField value={text} onChange={(e)=>setText(e.target.value)} multiline minRows={3} fullWidth placeholder="Antwort schreiben…" />
      <Button variant="contained" onClick={send} disabled={busy || !text.trim()}>Senden</Button>
    </Box>
  );
}
