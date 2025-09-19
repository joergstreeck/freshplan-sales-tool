import React from 'react';
import { Chip } from '@mui/material';
import type { Channel } from '../../types/cockpit';

export default function ChannelBadge({ channel }: { channel: Channel }) {
  const color = channel === 'DIRECT' ? 'primary' : 'secondary';
  return <Chip label={channel} color={color} size="small" />;
}
