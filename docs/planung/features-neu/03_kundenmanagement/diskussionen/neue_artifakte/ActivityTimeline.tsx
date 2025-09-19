import React from 'react';
import { Timeline, TimelineItem, TimelineSeparator, TimelineConnector, TimelineContent, TimelineDot } from '@mui/lab';

type Event = { id: string; kind: string; at: string; note?: string; sampleStatus?: string };

export default function ActivityTimeline({ events }: { events: Event[] }) {
  return (
    <Timeline>
      {events.map((e, idx) => (
        <TimelineItem key={e.id}>
          <TimelineSeparator>
            <TimelineDot color={e.sampleStatus ? 'primary' : 'secondary'} />
            {idx < events.length - 1 && <TimelineConnector />}
          </TimelineSeparator>
          <TimelineContent>{e.kind} – {new Date(e.at).toLocaleString()} {e.note ? `– ${e.note}` : ''}</TimelineContent>
        </TimelineItem>
      ))}
    </Timeline>
  );
}
