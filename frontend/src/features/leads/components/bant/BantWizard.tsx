/**
 * BANT Wizard Component
 *
 * Sprint 2.1.8 Phase 4: BANT Qualification System
 *
 * 4-Step Lead Qualification Wizard:
 * - Budget: Hat der Lead Budget?
 * - Authority: Ist der Ansprechpartner der Entscheider?
 * - Need: Besteht ein konkreter Bedarf?
 * - Timeline: Wann soll die Entscheidung fallen?
 *
 * @module BantWizard
 * @since Sprint 2.1.8
 */

import { useState } from 'react';
import {
  Box,
  Stepper,
  Step,
  StepLabel,
  Button,
  Typography,
  FormControl,
  FormControlLabel,
  RadioGroup,
  Radio,
  TextField,
  InputAdornment,
  Paper,
  Chip,
  Alert,
  LinearProgress,
  Stack,
} from '@mui/material';
import {
  AttachMoney as BudgetIcon,
  PersonSearch as AuthorityIcon,
  Psychology as NeedIcon,
  Schedule as TimelineIcon,
  CheckCircle as CompleteIcon,
} from '@mui/icons-material';
import { useTheme } from '@mui/material/styles';

// BANT Data Types
export interface BantData {
  // Budget
  bantBudgetStatus: string;
  bantBudgetAmount?: number;
  bantBudgetTimeline?: string;
  // Authority
  bantAuthorityLevel: string;
  bantAuthorityDecisionProcess?: string;
  // Need
  bantNeedLevel: string;
  bantNeedPainPoints?: string;
  bantNeedCurrentSolution?: string;
  // Timeline
  bantTimelineStatus: string;
  bantTimelineTargetDate?: string;
  bantTimelineUrgency?: string;
}

interface BantWizardProps {
  initialData?: Partial<BantData>;
  onComplete: (data: BantData) => void;
  onCancel?: () => void;
  readOnly?: boolean;
}

// BANT Score Calculation
function calculateBantScore(data: Partial<BantData>): number {
  let score = 0;

  // Budget Score (0-25)
  switch (data.bantBudgetStatus) {
    case 'CONFIRMED':
      score += 25;
      break;
    case 'PENDING':
      score += 15;
      break;
    case 'REJECTED':
      score += 5;
      break;
    case 'NO_BUDGET':
      score += 0;
      break;
    default:
      score += 0;
  }

  // Authority Score (0-25)
  switch (data.bantAuthorityLevel) {
    case 'DECISION_MAKER':
      score += 25;
      break;
    case 'CHAMPION':
      score += 20;
      break;
    case 'INFLUENCER':
      score += 15;
      break;
    case 'USER':
      score += 5;
      break;
    case 'BLOCKER':
      score += 0;
      break;
    default:
      score += 0;
  }

  // Need Score (0-25)
  switch (data.bantNeedLevel) {
    case 'CRITICAL':
      score += 25;
      break;
    case 'HIGH':
      score += 20;
      break;
    case 'MEDIUM':
      score += 15;
      break;
    case 'LOW':
      score += 5;
      break;
    case 'NONE':
      score += 0;
      break;
    default:
      score += 0;
  }

  // Timeline Score (0-25)
  switch (data.bantTimelineStatus) {
    case 'IMMEDIATE':
      score += 25;
      break;
    case 'QUARTER':
      score += 20;
      break;
    case 'HALF_YEAR':
      score += 15;
      break;
    case 'YEAR':
      score += 10;
      break;
    case 'FUTURE':
      score += 5;
      break;
    case 'NO_TIMELINE':
      score += 0;
      break;
    default:
      score += 0;
  }

  return score;
}

// Get score color based on value
function getScoreColor(score: number): 'success' | 'warning' | 'error' | 'info' {
  if (score >= 75) return 'success';
  if (score >= 50) return 'warning';
  if (score >= 25) return 'info';
  return 'error';
}

export function BantWizard({
  initialData,
  onComplete,
  onCancel,
  readOnly = false,
}: BantWizardProps) {
  const theme = useTheme();
  const steps = ['Budget', 'Authority', 'Need', 'Timeline'];
  const [activeStep, setActiveStep] = useState(0);
  const [data, setData] = useState<Partial<BantData>>(
    initialData || {
      bantBudgetStatus: 'UNKNOWN',
      bantAuthorityLevel: 'UNKNOWN',
      bantNeedLevel: 'UNKNOWN',
      bantTimelineStatus: 'UNKNOWN',
    }
  );

  const handleNext = () => {
    if (activeStep < steps.length - 1) {
      setActiveStep(prev => prev + 1);
    } else {
      onComplete(data as BantData);
    }
  };

  const handleBack = () => {
    setActiveStep(prev => prev - 1);
  };

  const updateField = (field: keyof BantData, value: string | number) => {
    setData(prev => ({ ...prev, [field]: value }));
  };

  const bantScore = calculateBantScore(data);
  const scoreColor = getScoreColor(bantScore);

  const renderStepContent = () => {
    switch (activeStep) {
      case 0: // Budget
        return (
          <Box>
            <Stack direction="row" spacing={1} alignItems="center" mb={2}>
              <BudgetIcon color="primary" />
              <Typography variant="h6">Budget</Typography>
            </Stack>
            <Typography variant="body2" color="text.secondary" mb={3}>
              Hat der Lead Budget für die Lösung genehmigt oder in Aussicht?
            </Typography>

            <FormControl component="fieldset" fullWidth>
              <RadioGroup
                value={data.bantBudgetStatus || 'UNKNOWN'}
                onChange={e => updateField('bantBudgetStatus', e.target.value)}
              >
                <FormControlLabel
                  value="CONFIRMED"
                  control={<Radio disabled={readOnly} />}
                  label="Budget bestätigt"
                />
                <FormControlLabel
                  value="PENDING"
                  control={<Radio disabled={readOnly} />}
                  label="Budget in Klärung"
                />
                <FormControlLabel
                  value="REJECTED"
                  control={<Radio disabled={readOnly} />}
                  label="Budget abgelehnt"
                />
                <FormControlLabel
                  value="NO_BUDGET"
                  control={<Radio disabled={readOnly} />}
                  label="Kein Budget vorhanden"
                />
                <FormControlLabel
                  value="UNKNOWN"
                  control={<Radio disabled={readOnly} />}
                  label="Noch nicht besprochen"
                />
              </RadioGroup>
            </FormControl>

            {data.bantBudgetStatus === 'CONFIRMED' && (
              <TextField
                fullWidth
                label="Budget-Betrag"
                type="number"
                value={data.bantBudgetAmount || ''}
                onChange={e => updateField('bantBudgetAmount', parseFloat(e.target.value))}
                InputProps={{
                  startAdornment: <InputAdornment position="start">€</InputAdornment>,
                }}
                sx={{ mt: 2 }}
                disabled={readOnly}
              />
            )}
          </Box>
        );

      case 1: // Authority
        return (
          <Box>
            <Stack direction="row" spacing={1} alignItems="center" mb={2}>
              <AuthorityIcon color="primary" />
              <Typography variant="h6">Authority</Typography>
            </Stack>
            <Typography variant="body2" color="text.secondary" mb={3}>
              Welche Rolle spielt der Ansprechpartner im Entscheidungsprozess?
            </Typography>

            <FormControl component="fieldset" fullWidth>
              <RadioGroup
                value={data.bantAuthorityLevel || 'UNKNOWN'}
                onChange={e => updateField('bantAuthorityLevel', e.target.value)}
              >
                <FormControlLabel
                  value="DECISION_MAKER"
                  control={<Radio disabled={readOnly} />}
                  label="Entscheider (GF, Inhaber)"
                />
                <FormControlLabel
                  value="CHAMPION"
                  control={<Radio disabled={readOnly} />}
                  label="Champion (aktiver Unterstützer)"
                />
                <FormControlLabel
                  value="INFLUENCER"
                  control={<Radio disabled={readOnly} />}
                  label="Influencer (kann empfehlen)"
                />
                <FormControlLabel
                  value="USER"
                  control={<Radio disabled={readOnly} />}
                  label="Anwender (kein Einfluss)"
                />
                <FormControlLabel
                  value="BLOCKER"
                  control={<Radio disabled={readOnly} />}
                  label="Blocker (gegen Änderung)"
                />
                <FormControlLabel
                  value="UNKNOWN"
                  control={<Radio disabled={readOnly} />}
                  label="Noch nicht ermittelt"
                />
              </RadioGroup>
            </FormControl>

            <TextField
              fullWidth
              label="Entscheidungsprozess (optional)"
              placeholder="Wer ist noch involviert? Wie wird entschieden?"
              multiline
              rows={2}
              value={data.bantAuthorityDecisionProcess || ''}
              onChange={e => updateField('bantAuthorityDecisionProcess', e.target.value)}
              sx={{ mt: 2 }}
              disabled={readOnly}
            />
          </Box>
        );

      case 2: // Need
        return (
          <Box>
            <Stack direction="row" spacing={1} alignItems="center" mb={2}>
              <NeedIcon color="primary" />
              <Typography variant="h6">Need</Typography>
            </Stack>
            <Typography variant="body2" color="text.secondary" mb={3}>
              Wie dringend ist der Bedarf des Leads?
            </Typography>

            <FormControl component="fieldset" fullWidth>
              <RadioGroup
                value={data.bantNeedLevel || 'UNKNOWN'}
                onChange={e => updateField('bantNeedLevel', e.target.value)}
              >
                <FormControlLabel
                  value="CRITICAL"
                  control={<Radio disabled={readOnly} />}
                  label="Kritisch (Problem muss sofort gelöst werden)"
                />
                <FormControlLabel
                  value="HIGH"
                  control={<Radio disabled={readOnly} />}
                  label="Hoch (klarer Handlungsbedarf)"
                />
                <FormControlLabel
                  value="MEDIUM"
                  control={<Radio disabled={readOnly} />}
                  label="Mittel (Interesse vorhanden)"
                />
                <FormControlLabel
                  value="LOW"
                  control={<Radio disabled={readOnly} />}
                  label="Niedrig (nur informiert)"
                />
                <FormControlLabel
                  value="NONE"
                  control={<Radio disabled={readOnly} />}
                  label="Kein Bedarf erkennbar"
                />
                <FormControlLabel
                  value="UNKNOWN"
                  control={<Radio disabled={readOnly} />}
                  label="Noch nicht ermittelt"
                />
              </RadioGroup>
            </FormControl>

            <TextField
              fullWidth
              label="Pain Points (optional)"
              placeholder="Was sind die konkreten Probleme?"
              multiline
              rows={2}
              value={data.bantNeedPainPoints || ''}
              onChange={e => updateField('bantNeedPainPoints', e.target.value)}
              sx={{ mt: 2 }}
              disabled={readOnly}
            />

            <TextField
              fullWidth
              label="Aktuelle Lösung (optional)"
              placeholder="Welche Lösung nutzt der Lead aktuell?"
              value={data.bantNeedCurrentSolution || ''}
              onChange={e => updateField('bantNeedCurrentSolution', e.target.value)}
              sx={{ mt: 2 }}
              disabled={readOnly}
            />
          </Box>
        );

      case 3: // Timeline
        return (
          <Box>
            <Stack direction="row" spacing={1} alignItems="center" mb={2}>
              <TimelineIcon color="primary" />
              <Typography variant="h6">Timeline</Typography>
            </Stack>
            <Typography variant="body2" color="text.secondary" mb={3}>
              Wann plant der Lead eine Entscheidung zu treffen?
            </Typography>

            <FormControl component="fieldset" fullWidth>
              <RadioGroup
                value={data.bantTimelineStatus || 'UNKNOWN'}
                onChange={e => updateField('bantTimelineStatus', e.target.value)}
              >
                <FormControlLabel
                  value="IMMEDIATE"
                  control={<Radio disabled={readOnly} />}
                  label="Sofort (innerhalb 1 Monat)"
                />
                <FormControlLabel
                  value="QUARTER"
                  control={<Radio disabled={readOnly} />}
                  label="Dieses Quartal (1-3 Monate)"
                />
                <FormControlLabel
                  value="HALF_YEAR"
                  control={<Radio disabled={readOnly} />}
                  label="Halbjahr (3-6 Monate)"
                />
                <FormControlLabel
                  value="YEAR"
                  control={<Radio disabled={readOnly} />}
                  label="Dieses Jahr (6-12 Monate)"
                />
                <FormControlLabel
                  value="FUTURE"
                  control={<Radio disabled={readOnly} />}
                  label="Später (>12 Monate)"
                />
                <FormControlLabel
                  value="NO_TIMELINE"
                  control={<Radio disabled={readOnly} />}
                  label="Kein Zeitrahmen bekannt"
                />
                <FormControlLabel
                  value="UNKNOWN"
                  control={<Radio disabled={readOnly} />}
                  label="Noch nicht besprochen"
                />
              </RadioGroup>
            </FormControl>

            <TextField
              fullWidth
              label="Zieldatum (optional)"
              type="date"
              value={data.bantTimelineTargetDate || ''}
              onChange={e => updateField('bantTimelineTargetDate', e.target.value)}
              InputLabelProps={{ shrink: true }}
              sx={{ mt: 2 }}
              disabled={readOnly}
            />
          </Box>
        );

      default:
        return null;
    }
  };

  return (
    <Paper sx={{ p: 3 }}>
      {/* Header with Score */}
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h5">BANT-Qualifizierung</Typography>
        <Chip
          icon={<CompleteIcon />}
          label={`Score: ${bantScore}/100`}
          color={scoreColor}
          variant="filled"
        />
      </Box>

      {/* Progress Bar */}
      <Box mb={3}>
        <LinearProgress
          variant="determinate"
          value={bantScore}
          color={scoreColor}
          sx={{ height: 8, borderRadius: 1 }}
        />
      </Box>

      {/* Stepper */}
      <Stepper activeStep={activeStep} alternativeLabel sx={{ mb: 4 }}>
        {steps.map((label, index) => {
          const icons = [
            <BudgetIcon key="b" />,
            <AuthorityIcon key="a" />,
            <NeedIcon key="n" />,
            <TimelineIcon key="t" />,
          ];
          return (
            <Step key={label}>
              <StepLabel
                StepIconComponent={() => (
                  <Box
                    sx={{
                      width: 40,
                      height: 40,
                      borderRadius: '50%',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      backgroundColor:
                        index <= activeStep
                          ? theme.palette.primary.main
                          : theme.palette.action.disabledBackground,
                      color: index <= activeStep ? 'white' : theme.palette.action.disabled,
                    }}
                  >
                    {icons[index]}
                  </Box>
                )}
              >
                {label}
              </StepLabel>
            </Step>
          );
        })}
      </Stepper>

      {/* Step Content */}
      <Box sx={{ minHeight: 300 }}>{renderStepContent()}</Box>

      {/* Navigation Buttons */}
      <Box display="flex" justifyContent="space-between" mt={4}>
        <Button onClick={onCancel} color="inherit" disabled={readOnly}>
          Abbrechen
        </Button>
        <Box>
          <Button onClick={handleBack} disabled={activeStep === 0 || readOnly} sx={{ mr: 1 }}>
            Zurück
          </Button>
          <Button variant="contained" onClick={handleNext} disabled={readOnly}>
            {activeStep === steps.length - 1 ? 'Speichern' : 'Weiter'}
          </Button>
        </Box>
      </Box>

      {/* Score Summary */}
      {activeStep === steps.length - 1 && (
        <Alert severity={scoreColor} sx={{ mt: 3 }}>
          <Typography variant="body2">
            {bantScore >= 75 && 'Hot Lead! Hohe Abschlusswahrscheinlichkeit.'}
            {bantScore >= 50 && bantScore < 75 && 'Warmer Lead. Weiter qualifizieren.'}
            {bantScore >= 25 && bantScore < 50 && 'Lead braucht Nurturing.'}
            {bantScore < 25 && 'Kalter Lead. Prüfen ob Potenzial vorhanden.'}
          </Typography>
        </Alert>
      )}
    </Paper>
  );
}

export default BantWizard;
