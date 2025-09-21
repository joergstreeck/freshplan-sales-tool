import React, { useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Chip,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  Button,
  Alert,
  IconButton,
  Tooltip,
  Grid,
  Paper,
  Divider
} from '@mui/material';
import {
  ExpandMore as ExpandMoreIcon,
  PlayArrow as PlayIcon,
  ContentCopy as CopyIcon,
  Warning as WarningIcon,
  CheckCircle as CheckIcon,
  Timer as TimerIcon,
  Pause as PauseIcon
} from '@mui/icons-material';

interface UserLeadOperationsPanelProps {
  response: CARResponse;
  onActionExecute?: (action: string) => void;
  onCopyToClipboard?: (content: string) => void;
}

/**
 * User-Lead-Operations Panel für CAR-Strategy Help-System
 * Zeigt External AI Operations-Runbook als interaktive Guided Operation
 */
export const UserLeadOperationsPanel: React.FC<UserLeadOperationsPanelProps> = ({
  response,
  onActionExecute,
  onCopyToClipboard
}) => {
  const [expandedSections, setExpandedSections] = useState<string[]>(['overview']);
  const [executedActions, setExecutedActions] = useState<Set<string>>(new Set());

  const handleSectionToggle = (section: string) => {
    setExpandedSections(prev =>
      prev.includes(section)
        ? prev.filter(s => s !== section)
        : [...prev, section]
    );
  };

  const handleActionExecute = (action: string) => {
    setExecutedActions(prev => new Set(prev).add(action));
    onActionExecute?.(action);
  };

  const handleCopySQL = (sql: string) => {
    onCopyToClipboard?.(sql);
  };

  const getStateIcon = (state: string) => {
    switch (state) {
      case 'PROTECTED': return <CheckIcon color="success" />;
      case 'REMINDER_DUE': return <WarningIcon color="warning" />;
      case 'GRACE': return <TimerIcon color="error" />;
      case 'EXPIRED': return <PauseIcon color="disabled" />;
      default: return <TimerIcon />;
    }
  };

  const getStateColor = (state: string) => {
    switch (state) {
      case 'PROTECTED': return 'success';
      case 'REMINDER_DUE': return 'warning';
      case 'GRACE': return 'error';
      case 'EXPIRED': return 'default';
      default: return 'primary';
    }
  };

  return (
    <Box sx={{ maxWidth: 1200, mx: 'auto', p: 2 }}>
      {/* Header mit Quick Summary */}
      <Card sx={{ mb: 3, background: 'linear-gradient(135deg, #1976d2 0%, #42a5f5 100%)', color: 'white' }}>
        <CardContent>
          <Typography variant="h5" gutterBottom sx={{ fontWeight: 'bold' }}>
            {response.title}
          </Typography>
          <Typography variant="body1" sx={{ mb: 2, opacity: 0.9 }}>
            {response.quickSummary}
          </Typography>
          <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
            <Chip
              label="6M+60T+10T Protection"
              size="small"
              sx={{ backgroundColor: 'rgba(255,255,255,0.2)', color: 'white' }}
            />
            <Chip
              label="Stop-Clock Management"
              size="small"
              sx={{ backgroundColor: 'rgba(255,255,255,0.2)', color: 'white' }}
            />
            <Chip
              label="User-basiert"
              size="small"
              sx={{ backgroundColor: 'rgba(255,255,255,0.2)', color: 'white' }}
            />
          </Box>
        </CardContent>
      </Card>

      {/* Business Context */}
      {response.businessContext && (
        <Alert severity="info" sx={{ mb: 3 }}>
          <Typography variant="body2">
            {response.businessContext}
          </Typography>
        </Alert>
      )}

      {/* State Machine Visualization */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <TimerIcon color="primary" />
            Lead-Protection State Machine
          </Typography>
          <Grid container spacing={2}>
            {[
              { state: 'PROTECTED', description: 'Aktiver Schutz, Timer läuft' },
              { state: 'REMINDER_DUE', description: '≥60T Inaktivität, Reminder wird gesendet' },
              { state: 'GRACE', description: '10T Frist nach Reminder' },
              { state: 'EXPIRED', description: 'Schutz erloschen' }
            ].map(({ state, description }) => (
              <Grid item xs={12} sm={6} md={3} key={state}>
                <Paper
                  sx={{
                    p: 2,
                    textAlign: 'center',
                    backgroundColor: getStateColor(state) === 'success' ? '#e8f5e8' :
                                   getStateColor(state) === 'warning' ? '#fff3e0' :
                                   getStateColor(state) === 'error' ? '#ffebee' : '#f5f5f5'
                  }}
                >
                  {getStateIcon(state)}
                  <Typography variant="subtitle2" sx={{ fontWeight: 'bold', mt: 1 }}>
                    {state}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    {description}
                  </Typography>
                </Paper>
              </Grid>
            ))}
          </Grid>
        </CardContent>
      </Card>

      {/* Action Steps */}
      <Accordion
        expanded={expandedSections.includes('actions')}
        onChange={() => handleSectionToggle('actions')}
        sx={{ mb: 2 }}
      >
        <AccordionSummary expandIcon={<ExpandMoreIcon />}>
          <Typography variant="h6" sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <PlayIcon color="primary" />
            Operations-Aktionen
          </Typography>
        </AccordionSummary>
        <AccordionDetails>
          <Box sx={{ space: 2 }}>
            {response.actionSteps?.map((step, index) => {
              const isSQLCommand = step.includes('SELECT') || step.includes('INSERT') || step.includes('UPDATE');
              const actionKey = `action-${index}`;
              const isExecuted = executedActions.has(actionKey);

              if (step.trim() === '') {
                return <Box key={index} sx={{ height: 8 }} />;
              }

              return (
                <Paper
                  key={index}
                  sx={{
                    p: 2,
                    mb: 2,
                    backgroundColor: isSQLCommand ? '#f8f9fa' : 'transparent',
                    border: isSQLCommand ? '1px solid #e9ecef' : 'none'
                  }}
                >
                  <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 1 }}>
                    <Typography
                      variant="body2"
                      sx={{
                        flex: 1,
                        fontFamily: isSQLCommand ? 'monospace' : 'inherit',
                        whiteSpace: 'pre-wrap',
                        color: isExecuted ? 'text.secondary' : 'text.primary'
                      }}
                    >
                      {step}
                    </Typography>

                    {isSQLCommand && (
                      <Box sx={{ display: 'flex', gap: 1 }}>
                        <Tooltip title="SQL kopieren">
                          <IconButton
                            size="small"
                            onClick={() => handleCopySQL(step)}
                            sx={{ opacity: 0.7 }}
                          >
                            <CopyIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="Aktion ausführen">
                          <IconButton
                            size="small"
                            onClick={() => handleActionExecute(actionKey)}
                            color={isExecuted ? 'success' : 'primary'}
                            sx={{ opacity: isExecuted ? 0.5 : 1 }}
                          >
                            {isExecuted ? <CheckIcon fontSize="small" /> : <PlayIcon fontSize="small" />}
                          </IconButton>
                        </Tooltip>
                      </Box>
                    )}
                  </Box>
                </Paper>
              );
            })}
          </Box>
        </AccordionDetails>
      </Accordion>

      {/* Troubleshooting */}
      {response.troubleshooting && Object.keys(response.troubleshooting).length > 0 && (
        <Accordion
          expanded={expandedSections.includes('troubleshooting')}
          onChange={() => handleSectionToggle('troubleshooting')}
          sx={{ mb: 2 }}
        >
          <AccordionSummary expandIcon={<ExpandMoreIcon />}>
            <Typography variant="h6" sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <WarningIcon color="warning" />
              Troubleshooting
            </Typography>
          </AccordionSummary>
          <AccordionDetails>
            {Object.entries(response.troubleshooting).map(([problem, solution], index) => (
              <Alert key={index} severity="warning" sx={{ mb: 1 }}>
                <Typography variant="subtitle2" sx={{ fontWeight: 'bold' }}>
                  {problem}
                </Typography>
                <Typography variant="body2" sx={{ mt: 0.5, fontFamily: 'monospace' }}>
                  {solution}
                </Typography>
              </Alert>
            ))}
          </AccordionDetails>
        </Accordion>
      )}

      {/* KPIs */}
      {response.kpis && response.kpis.length > 0 && (
        <Accordion
          expanded={expandedSections.includes('kpis')}
          onChange={() => handleSectionToggle('kpis')}
          sx={{ mb: 2 }}
        >
          <AccordionSummary expandIcon={<ExpandMoreIcon />}>
            <Typography variant="h6">
              📊 Operations-KPIs
            </Typography>
          </AccordionSummary>
          <AccordionDetails>
            <Grid container spacing={2}>
              {response.kpis.map((kpi, index) => (
                <Grid item xs={12} sm={6} key={index}>
                  <Paper sx={{ p: 2, backgroundColor: '#f8f9fa' }}>
                    <Typography variant="body2" sx={{ fontWeight: 'bold' }}>
                      {kpi}
                    </Typography>
                  </Paper>
                </Grid>
              ))}
            </Grid>
          </AccordionDetails>
        </Accordion>
      )}

      {/* Business Rules */}
      {response.businessRules && response.businessRules.length > 0 && (
        <Accordion
          expanded={expandedSections.includes('rules')}
          onChange={() => handleSectionToggle('rules')}
          sx={{ mb: 2 }}
        >
          <AccordionSummary expandIcon={<ExpandMoreIcon />}>
            <Typography variant="h6">
              📋 Business-Regeln
            </Typography>
          </AccordionSummary>
          <AccordionDetails>
            {response.businessRules.map((rule, index) => (
              <Typography key={index} variant="body2" sx={{ mb: 1, pl: 2 }}>
                • {rule}
              </Typography>
            ))}
          </AccordionDetails>
        </Accordion>
      )}

      {/* Escalation */}
      {response.escalation && (
        <Alert severity="error" sx={{ mb: 3 }}>
          <Typography variant="subtitle2" sx={{ fontWeight: 'bold' }}>
            Eskalation:
          </Typography>
          <Typography variant="body2" sx={{ whiteSpace: 'pre-wrap' }}>
            {response.escalation}
          </Typography>
        </Alert>
      )}

      {/* Related Documentation */}
      {response.relatedDocumentation && response.relatedDocumentation.length > 0 && (
        <Card>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              📚 Zugehörige Dokumentation
            </Typography>
            {response.relatedDocumentation.map((doc, index) => (
              <Typography key={index} variant="body2" sx={{ mb: 0.5, fontFamily: 'monospace' }}>
                {doc}
              </Typography>
            ))}
          </CardContent>
        </Card>
      )}
    </Box>
  );
};

export default UserLeadOperationsPanel;