import {
  Accordion,
  AccordionSummary,
  AccordionDetails,
  Box,
  Typography,
  Chip,
} from '@mui/material';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';

interface ScoreAccordionProps {
  title: string;
  icon: string;
  score?: number;
  weight?: number;
  expanded: boolean;
  onChange: () => void;
  children: React.ReactNode;
}

function getScoreColor(score: number): 'success' | 'warning' | 'error' {
  if (score >= 70) return 'success';
  if (score >= 40) return 'warning';
  return 'error';
}

function getScoreIcon(score: number): string {
  if (score >= 70) return '✅';
  if (score >= 40) return '⚠️';
  return '❌';
}

export function ScoreAccordion({
  title,
  icon,
  score,
  weight,
  expanded,
  onChange,
  children,
}: ScoreAccordionProps) {
  return (
    <Accordion expanded={expanded} onChange={onChange} sx={{ mb: 1 }}>
      <AccordionSummary expandIcon={<ExpandMoreIcon />}>
        <Box display="flex" alignItems="center" width="100%">
          <Typography variant="h6" sx={{ flexGrow: 1 }}>
            {icon} {title}
            {weight && (
              <Typography component="span" variant="caption" sx={{ ml: 1, color: 'text.secondary' }}>
                ({weight}%)
              </Typography>
            )}
          </Typography>
          {score !== undefined && (
            <Chip
              label={`${score}/100 ${getScoreIcon(score)}`}
              color={getScoreColor(score)}
              size="small"
              sx={{ mr: 2 }}
            />
          )}
        </Box>
      </AccordionSummary>

      <AccordionDetails>{children}</AccordionDetails>
    </Accordion>
  );
}
