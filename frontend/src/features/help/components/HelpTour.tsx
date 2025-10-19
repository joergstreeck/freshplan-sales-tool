import React, { useEffect, useState } from 'react';
import Joyride, { STATUS } from 'react-joyride';
import type { CallBackProps, Step } from 'react-joyride';
import { Box, Typography, Avatar } from '@mui/material';
import { Assistant as AssistantIcon } from '@mui/icons-material';
import { useHelpStore } from '../stores/helpStore';
import type { HelpContent } from '../types/help.types';

interface TourStep extends Step {
  helpContent?: HelpContent;
}

export const HelpTour: React.FC = () => {
  const { currentHelp, tourActive, tourStep, nextTourStep, previousTourStep, endTour, trackView } =
    useHelpStore();

  const [steps, setSteps] = useState<TourStep[]>([]);

  useEffect(() => {
    if (currentHelp && tourActive) {
      const tourContent = currentHelp.helpContents.filter(h => h.helpType === 'TOUR');

      const tourSteps: TourStep[] = tourContent.map((content, index) => {
        // Parse target from content (e.g., "[target: .warmth-indicator]")
        const targetMatch = content.shortContent.match(/\[target:\s*([^\]]+)\]/);
        const target = targetMatch ? targetMatch[1] : '.help-tour-step-' + index;

        // Remove target marker from content
        const cleanContent = content.shortContent.replace(/\[target:[^\]]+\]/, '').trim();

        return {
          target,
          content: (
            <Box>
              <Box display="flex" alignItems="center" mb={2}>
                <Avatar sx={{ bgcolor: 'primary.main', mr: 1, width: 32, height: 32 }}>
                  <AssistantIcon fontSize="small" />
                </Avatar>
                <Typography variant="h6">{content.title}</Typography>
              </Box>

              <Typography variant="body2" paragraph>
                {cleanContent}
              </Typography>

              {content.mediumContent && (
                <Typography variant="body2" color="text.secondary">
                  {content.mediumContent}
                </Typography>
              )}

              {/* Show example if available */}
              {content.detailedContent && (
                <Box
                  sx={{
                    mt: 2,
                    p: 1.5,
                    bgcolor: 'grey.100',
                    borderRadius: 1,
                    border: '1px solid',
                    borderColor: 'divider',
                  }}
                >
                  <Typography variant="caption" color="text.secondary">
                    Beispiel:
                  </Typography>
                  <Typography variant="body2">{content.detailedContent}</Typography>
                </Box>
              )}
            </Box>
          ),
          placement: 'bottom' as const,
          spotlightClicks: true,
          disableBeacon: index === 0,
          helpContent: content,
        };
      });

      setSteps(tourSteps);

      // Track view for first step
      if (tourContent.length > 0) {
        trackView(tourContent[0].id);
      }
    }
  }, [currentHelp, tourActive, trackView]);

  const handleJoyrideCallback = (data: CallBackProps) => {
    const { status, type, index, action } = data;

    if (type === 'step:after' && action === 'next') {
      // Track view for next step
      if (steps[index + 1]?.helpContent) {
        trackView(steps[index + 1].helpContent.id);
      }
      nextTourStep();
    } else if (type === 'step:after' && action === 'prev') {
      previousTourStep();
    } else if (status === STATUS.FINISHED || status === STATUS.SKIPPED) {
      endTour();
    }
  };

  if (!tourActive || steps.length === 0) return null;

  return (
    <Joyride
      steps={steps}
      run={tourActive}
      stepIndex={tourStep}
      continuous
      showProgress
      showSkipButton
      disableOverlay
      disableScrolling
      callback={handleJoyrideCallback}
      styles={{
        options: {
          primaryColor: theme.palette.info.main,
          zIndex: 10000,
          width: 400,
        },
        tooltip: {
          borderRadius: 8,
          padding: 16,
        },
        tooltipContainer: {
          textAlign: 'left',
        },
        buttonNext: {
          borderRadius: 4,
        },
        buttonBack: {
          borderRadius: 4,
          marginRight: 8,
        },
        buttonSkip: {
          borderRadius: 4,
        },
      }}
      locale={{
        back: 'Zurück',
        close: 'Schließen',
        last: 'Fertig',
        next: 'Weiter',
        skip: 'Überspringen',
      }}
      floaterProps={{
        disableAnimation: false,
        styles: {
          floater: {
            filter: 'drop-shadow(0 0 10px rgba(0, 0, 0, 0.3))',
          },
        },
      }}
    />
  );
};
