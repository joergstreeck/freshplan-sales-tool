import React, { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Typography,
  Box,
  Stepper,
  Step,
  StepLabel,
  StepContent,
  IconButton,
  Chip,
  Stack,
  Alert,
  ButtonGroup,
  TextField,
} from '@mui/material';
import {
  Close as CloseIcon,
  ThumbUp as ThumbUpIcon,
  ThumbDown as ThumbDownIcon,
  PlayCircleOutline as PlayCircleIcon,
} from '@mui/icons-material';
import { CustomerFieldThemeProvider } from '../../customers/theme/CustomerFieldThemeProvider';
import { useHelpStore } from '../stores/helpStore';

export const HelpModal: React.FC = () => {
  const { modalOpen, modalContent, closeModal, submitFeedback, startTour } = useHelpStore();

  const [feedback, setFeedback] = useState<'helpful' | 'not-helpful' | null>(null);
  const [feedbackComment, setFeedbackComment] = useState('');
  const [activeStep, setActiveStep] = useState(0);

  if (!modalContent) return null;

  const handleFeedback = async (helpful: boolean) => {
    setFeedback(helpful ? 'helpful' : 'not-helpful');

    if (modalContent) {
      await submitFeedback(modalContent.id, helpful, helpful ? undefined : feedbackComment);
    }
  };

  const handleClose = () => {
    closeModal();
    setFeedback(null);
    setFeedbackComment('');
    setActiveStep(0);
  };

  // Parse detailed content as steps if it contains step markers
  const parseSteps = (content?: string): Array<{ label: string; description: string }> => {
    if (!content) return [];

    const stepRegex = /(\d+)\.\s*([^:]+):\s*([^0-9]+)/g;
    const steps: Array<{ label: string; description: string }> = [];
    let match;

    while ((match = stepRegex.exec(content)) !== null) {
      steps.push({
        label: match[2].trim(),
        description: match[3].trim(),
      });
    }

    return steps;
  };

  const steps = parseSteps(modalContent.detailedContent);
  const hasSteps = steps.length > 0;

  return (
    <CustomerFieldThemeProvider mode="anpassungsfähig">
      <Dialog
        open={modalOpen}
        onClose={handleClose}
        maxWidth="md"
        fullWidth
        PaperProps={{
          sx: { minHeight: '400px' },
        }}
      >
        <DialogTitle>
          <Box display="flex" alignItems="center" justifyContent="space-between">
            <Box>
              <Typography variant="h6">{modalContent.title}</Typography>
              <Stack direction="row" spacing={1} sx={{ mt: 0.5 }}>
                <Chip label={modalContent.helpType} size="small" variant="outlined" />
                <Chip
                  label={modalContent.targetUserLevel}
                  size="small"
                  color="primary"
                  variant="outlined"
                />
                {modalContent.viewCount > 0 && (
                  <Chip
                    label={`${modalContent.viewCount} Aufrufe`}
                    size="small"
                    variant="outlined"
                  />
                )}
              </Stack>
            </Box>
            <IconButton edge="end" color="inherit" onClick={handleClose} aria-label="close">
              <CloseIcon />
            </IconButton>
          </Box>
        </DialogTitle>

        <DialogContent dividers>
          {/* Short Content */}
          <Typography variant="body1" paragraph>
            {modalContent.shortContent}
          </Typography>

          {/* Medium Content */}
          {modalContent.mediumContent && (
            <Alert severity="info" sx={{ mb: 2 }}>
              {modalContent.mediumContent}
            </Alert>
          )}

          {/* Video if available */}
          {modalContent.videoUrl && (
            <Box sx={{ mb: 3, position: 'relative', paddingTop: '56.25%' }}>
              <video
                src={modalContent.videoUrl}
                controls
                style={{
                  position: 'absolute',
                  top: 0,
                  left: 0,
                  width: '100%',
                  height: '100%',
                }}
              />
            </Box>
          )}

          {/* Detailed Content as Steps or Plain Text */}
          {hasSteps ? (
            <Stepper activeStep={activeStep} orientation="vertical">
              {steps.map((step, index) => (
                <Step key={index}>
                  <StepLabel>{step.label}</StepLabel>
                  <StepContent>
                    <Typography variant="body2" color="text.secondary">
                      {step.description}
                    </Typography>
                    <Box sx={{ mb: 2, mt: 1 }}>
                      <Button
                        variant="contained"
                        onClick={() => setActiveStep(index + 1)}
                        sx={{ mt: 1, mr: 1 }}
                        size="small"
                        disabled={index === steps.length - 1}
                      >
                        Weiter
                      </Button>
                      <Button
                        disabled={index === 0}
                        onClick={() => setActiveStep(index - 1)}
                        sx={{ mt: 1, mr: 1 }}
                        size="small"
                      >
                        Zurück
                      </Button>
                    </Box>
                  </StepContent>
                </Step>
              ))}
            </Stepper>
          ) : modalContent.detailedContent ? (
            <Typography variant="body2" color="text.secondary" paragraph>
              {modalContent.detailedContent}
            </Typography>
          ) : null}

          {/* Feedback Section */}
          <Box sx={{ mt: 3, p: 2, bgcolor: 'grey.100', borderRadius: 1 }}>
            <Typography variant="body2" gutterBottom>
              War diese Erklärung hilfreich?
            </Typography>

            <ButtonGroup size="small">
              <Button
                variant={feedback === 'helpful' ? 'contained' : 'outlined'}
                onClick={() => handleFeedback(true)}
                startIcon={<ThumbUpIcon />}
                disabled={feedback !== null}
              >
                Ja ({modalContent.helpfulCount})
              </Button>
              <Button
                variant={feedback === 'not-helpful' ? 'contained' : 'outlined'}
                onClick={() => setFeedback('not-helpful')}
                startIcon={<ThumbDownIcon />}
                disabled={feedback !== null}
              >
                Nein ({modalContent.notHelpfulCount})
              </Button>
            </ButtonGroup>

            {feedback === 'not-helpful' && (
              <TextField
                fullWidth
                multiline
                rows={2}
                placeholder="Was war unklar? (optional)"
                value={feedbackComment}
                onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) => setFeedbackComment(e.target.value)}
                sx={{ mt: 1 }}
                onBlur={() => handleFeedback(false)}
              />
            )}

            {feedback === 'helpful' && (
              <Alert severity="success" sx={{ mt: 1 }}>
                Danke für Ihr Feedback!
              </Alert>
            )}
          </Box>
        </DialogContent>

        <DialogActions>
          {modalContent.helpType === 'TOUR' && (
            <Button
              onClick={() => {
                startTour(modalContent.feature);
                handleClose();
              }}
              startIcon={<PlayCircleIcon />}
            >
              Interaktive Tour starten
            </Button>
          )}
          <Button onClick={handleClose}>Schließen</Button>
        </DialogActions>
      </Dialog>
    </CustomerFieldThemeProvider>
  );
};
