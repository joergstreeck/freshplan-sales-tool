import React from 'react'; import { Box, CircularProgress } from '@mui/material';
export default function LoadingState(){ return <Box sx={{ p:2, display:'flex', justifyContent:'center' }}><CircularProgress/></Box>; }
