import { Box, List, ListItem, ListItemIcon, ListItemText, Alert } from '@mui/material';
import type { Lead } from '../types';

interface FitScoreDisplayProps {
  lead: Lead;
}

const IDEAL_SEGMENTS = ['RESTAURANT', 'CATERING', 'HOTEL', 'KANTINE'];
const TOP_CITIES = [
  'Berlin',
  'München',
  'Hamburg',
  'Köln',
  'Frankfurt',
  'Stuttgart',
  'Düsseldorf',
  'Dortmund',
  'Essen',
  'Leipzig',
];
const HIGH_QUALITY_SOURCES = ['EMPFEHLUNG', 'PARTNER', 'MESSE'];

function isIdealSegment(type?: string): boolean {
  return type ? IDEAL_SEGMENTS.includes(type) : false;
}

function isTopCity(city?: string): boolean {
  return city ? TOP_CITIES.includes(city) : false;
}

function isHighQualitySource(source?: string): boolean {
  return source ? HIGH_QUALITY_SOURCES.includes(source) : false;
}

function getSourceQualityLabel(source?: string): string {
  if (!source) return 'Nicht angegeben';
  if (HIGH_QUALITY_SOURCES.includes(source)) return `${source} (Hohe Qualität ✅)`;
  if (source === 'MESSE') return 'MESSE (Mittel ⚠️)';
  if (source === 'WEB_FORMULAR') return 'WEB_FORMULAR (Niedrig ⚠️)';
  return `${source} (Sonstige)`;
}

export function FitScoreDisplay({ lead }: FitScoreDisplayProps) {
  return (
    <Box sx={{ p: 2 }}>
      <Alert severity="info" sx={{ mb: 2 }}>
        Score: {lead.fitScore || 0}/100{' '}
        {lead.fitScore && lead.fitScore >= 70
          ? '✅'
          : lead.fitScore && lead.fitScore >= 40
            ? '⚠️'
            : '❌'}
        <Box component="span" display="block" sx={{ fontSize: '0.85em', mt: 0.5 }}>
          Automatisch berechnet aus Stammdaten
        </Box>
      </Alert>

      <List>
        <ListItem>
          <ListItemIcon sx={{ minWidth: 40 }}>
            {isIdealSegment(lead.businessType) ? '✅' : '⚠️'}
          </ListItemIcon>
          <ListItemText
            primary="Geschäftstyp"
            secondary={`${lead.businessType || 'Nicht angegeben'} ${isIdealSegment(lead.businessType) ? '(Ideal)' : ''}`}
          />
        </ListItem>

        <ListItem>
          <ListItemIcon sx={{ minWidth: 40 }}>{isTopCity(lead.city) ? '✅' : '⚠️'}</ListItemIcon>
          <ListItemText
            primary="Standort"
            secondary={`${lead.city || 'Nicht angegeben'} ${isTopCity(lead.city) ? '(Top 10)' : ''}`}
          />
        </ListItem>

        <ListItem>
          <ListItemIcon sx={{ minWidth: 40 }}>
            {isHighQualitySource(lead.source) ? '✅' : '⚠️'}
          </ListItemIcon>
          <ListItemText primary="Quelle" secondary={getSourceQualityLabel(lead.source)} />
        </ListItem>
      </List>

      <Box sx={{ mt: 2, color: 'text.secondary', fontSize: '0.875rem' }}>
        💡 Tipp: Stammdaten ändern, um Fit Score zu verbessern
      </Box>
    </Box>
  );
}
