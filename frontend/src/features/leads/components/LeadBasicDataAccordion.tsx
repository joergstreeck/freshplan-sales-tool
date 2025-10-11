import {
  Accordion,
  AccordionSummary,
  AccordionDetails,
  Typography,
  Box,
  IconButton
} from '@mui/material';
import Grid from '@mui/material/Grid';
import {
  ExpandMore as ExpandMoreIcon,
  Edit as EditIcon,
  Business as BusinessIcon
} from '@mui/icons-material';
import type { Lead } from '../types';

interface LeadBasicDataAccordionProps {
  lead: Lead;
  expanded: boolean;
  onChange: (event: React.SyntheticEvent, isExpanded: boolean) => void;
  onEdit?: () => void;
}

export function LeadBasicDataAccordion({ lead, expanded, onChange, onEdit }: LeadBasicDataAccordionProps) {
  // Preview Info für Header
  const previewParts = [
    lead.companyName || 'Kein Name',
    lead.city || 'Kein Ort',
    lead.source || ''
  ].filter(Boolean);

  const previewText = previewParts.join(' • ');

  return (
    <Accordion
      expanded={expanded}
      onChange={onChange}
      sx={{ mb: 2, border: '1px solid', borderColor: 'divider' }}
    >
      <AccordionSummary expandIcon={<ExpandMoreIcon />}>
        <Box sx={{ display: 'flex', alignItems: 'center', width: '100%', gap: 1 }}>
          <BusinessIcon color="primary" />
          <Box sx={{ flexGrow: 1 }}>
            <Typography variant="h6">Stammdaten</Typography>
            {!expanded && (
              <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
                {previewText}
              </Typography>
            )}
          </Box>
          {onEdit && (
            <IconButton
              size="small"
              component="div"
              onClick={(e) => {
                e.stopPropagation();
                onEdit();
              }}
              sx={{ cursor: 'pointer' }}
            >
              <EditIcon fontSize="small" />
            </IconButton>
          )}
        </Box>
      </AccordionSummary>

      <AccordionDetails>
        <Grid container spacing={2}>
          {/* Firmenname */}
          <Grid size={{ xs: 12, sm: 6 }}>
            <Typography variant="caption" color="text.secondary">
              Firmenname
            </Typography>
            <Typography variant="body1">{lead.companyName || '—'}</Typography>
          </Grid>

          {/* Website */}
          <Grid size={{ xs: 12, sm: 6 }}>
            <Typography variant="caption" color="text.secondary">
              Website
            </Typography>
            <Typography variant="body1">{lead.website || '—'}</Typography>
          </Grid>

          {/* Straße */}
          <Grid size={{ xs: 12, sm: 6 }}>
            <Typography variant="caption" color="text.secondary">
              Straße
            </Typography>
            <Typography variant="body1">{lead.street || '—'}</Typography>
          </Grid>

          {/* PLZ */}
          <Grid size={{ xs: 12, sm: 3 }}>
            <Typography variant="caption" color="text.secondary">
              PLZ
            </Typography>
            <Typography variant="body1">{lead.postalCode || '—'}</Typography>
          </Grid>

          {/* Stadt */}
          <Grid size={{ xs: 12, sm: 3 }}>
            <Typography variant="caption" color="text.secondary">
              Stadt
            </Typography>
            <Typography variant="body1">{lead.city || '—'}</Typography>
          </Grid>

          {/* Quelle */}
          <Grid size={{ xs: 12, sm: 6 }}>
            <Typography variant="caption" color="text.secondary">
              Quelle
            </Typography>
            <Typography variant="body1">{lead.source || '—'}</Typography>
          </Grid>

          {/* Erstellt von */}
          <Grid size={{ xs: 12, sm: 6 }}>
            <Typography variant="caption" color="text.secondary">
              Erstellt von
            </Typography>
            <Typography variant="body1">
              {lead.createdBy || '—'}
            </Typography>
          </Grid>

          {/* Erstellt am */}
          <Grid size={{ xs: 12, sm: 6 }}>
            <Typography variant="caption" color="text.secondary">
              Erstellt am
            </Typography>
            <Typography variant="body1">
              {lead.createdAt ? new Date(lead.createdAt).toLocaleDateString('de-DE') : '—'}
            </Typography>
          </Grid>
        </Grid>
      </AccordionDetails>
    </Accordion>
  );
}
