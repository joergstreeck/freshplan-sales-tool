import { Box, Stepper, Step, StepLabel, Typography, Chip } from '@mui/material';
import { CheckCircle, RadioButtonUnchecked, Cancel } from '@mui/icons-material';

interface LeadStatusWorkflowProps {
  currentStatus: string;
  orientation?: 'horizontal' | 'vertical';
  size?: 'small' | 'medium';
}

/**
 * Lead Status Workflow (Sprint 2.1.6 Phase 4)
 *
 * Visualisiert Lead-Lebenszyklus mit deutschen Bezeichnungen.
 * "Prospect" → "Interessent" (Design System: Sprache des Vertriebsmitarbeiters)
 *
 * Status-Übergänge:
 * 1. REGISTERED → Initial erfasst
 * 2. LEAD → Qualifiziert, kontaktiert
 * 3. PROSPECT (Interessent) → Aktives Interesse, in Verhandlung
 * 4. ACTIVE → Kunde gewonnen
 * 5. EXPIRED → Lead abgelaufen
 * 6. CONVERTED → In Kundenstamm übernommen
 */
export default function LeadStatusWorkflow({
  currentStatus,
  orientation = 'horizontal',
  size = 'medium',
}: LeadStatusWorkflowProps) {
  // Workflow-Schritte mit deutschen Labels
  const workflowSteps = [
    { status: 'REGISTERED', label: 'Registriert', description: 'Initial erfasst' },
    { status: 'LEAD', label: 'Lead', description: 'Qualifiziert' },
    { status: 'PROSPECT', label: 'Interessent', description: 'In Verhandlung' },
    { status: 'ACTIVE', label: 'Aktiv', description: 'Kunde gewonnen' },
  ];

  // Terminal-Status (außerhalb des normalen Workflows)
  const terminalStatuses = ['EXPIRED', 'CONVERTED'];

  const getCurrentStepIndex = () => {
    const index = workflowSteps.findIndex((step) => step.status === currentStatus);
    return index === -1 ? 0 : index;
  };

  const isTerminalStatus = terminalStatuses.includes(currentStatus);
  const currentStepIndex = getCurrentStepIndex();

  const getStatusColor = (status: string): 'success' | 'error' | 'warning' | 'info' => {
    if (status === 'EXPIRED') return 'error';
    if (status === 'CONVERTED') return 'success';
    if (status === 'ACTIVE') return 'success';
    if (status === 'PROSPECT') return 'info';
    return 'warning';
  };

  const getTerminalStatusLabel = (status: string): string => {
    if (status === 'EXPIRED') return 'Abgelaufen';
    if (status === 'CONVERTED') return 'Konvertiert';
    return status;
  };

  if (isTerminalStatus) {
    return (
      <Box textAlign="center" py={2}>
        <Chip
          icon={currentStatus === 'EXPIRED' ? <Cancel /> : <CheckCircle />}
          label={getTerminalStatusLabel(currentStatus)}
          color={getStatusColor(currentStatus)}
          size={size}
          sx={{ fontSize: size === 'small' ? '0.75rem' : '0.875rem' }}
        />
        <Typography variant="caption" display="block" mt={1} color="text.secondary">
          {currentStatus === 'EXPIRED'
            ? 'Lead-Schutz abgelaufen'
            : 'Erfolgreich in Kundenstamm übernommen'}
        </Typography>
      </Box>
    );
  }

  return (
    <Box>
      <Stepper activeStep={currentStepIndex} orientation={orientation}>
        {workflowSteps.map((step, index) => (
          <Step key={step.status} completed={index < currentStepIndex}>
            <StepLabel
              icon={
                index < currentStepIndex ? (
                  <CheckCircle color="success" fontSize={size} />
                ) : index === currentStepIndex ? (
                  <RadioButtonUnchecked color="primary" fontSize={size} />
                ) : (
                  <RadioButtonUnchecked color="disabled" fontSize={size} />
                )
              }
            >
              <Typography variant={size === 'small' ? 'caption' : 'body2'} fontWeight={500}>
                {step.label}
              </Typography>
              {orientation === 'vertical' && (
                <Typography variant="caption" color="text.secondary" display="block">
                  {step.description}
                </Typography>
              )}
            </StepLabel>
          </Step>
        ))}
      </Stepper>

      {orientation === 'horizontal' && (
        <Typography
          variant="caption"
          color="text.secondary"
          display="block"
          textAlign="center"
          mt={1}
        >
          {workflowSteps[currentStepIndex]?.description}
        </Typography>
      )}
    </Box>
  );
}
