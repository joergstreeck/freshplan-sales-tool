import React, { useState, useEffect } from 'react';
import {
  Paper,
  Box,
  Typography,
  Button,
  Stack,
  Avatar,
  IconButton,
  Slide,
  Fade,
} from '@mui/material';
import {
  Assistant as AssistantIcon,
  Close as CloseIcon,
  PlayCircleOutline as PlayCircleIcon,
  Search as SearchIcon,
  AccountTree as AccountTreeIcon,
  Explore as ExploreIcon,
  ContactSupport as ContactSupportIcon,
  ListAlt as ListAltIcon,
} from '@mui/icons-material';
import { CustomerFieldThemeProvider } from '../../customers/theme/CustomerFieldThemeProvider';
import { useHelpStore } from '../stores/helpStore';
import type { StruggleType, HelpSuggestion } from '../types/help.types';

const STRUGGLE_MESSAGES: Record<StruggleType, string> = {
  REPEATED_FAILED_ATTEMPTS:
    'Ich sehe, dass Sie mehrmals versucht haben, diese Aktion durchzuführen. Lassen Sie mich Ihnen zeigen, wie es funktioniert.',

  RAPID_NAVIGATION_CHANGES:
    'Sie suchen etwas Bestimmtes? Ich kann Ihnen helfen, schneller ans Ziel zu kommen.',

  LONG_IDLE_AFTER_START:
    'Nicht sicher, wie es weitergeht? Hier ist eine Schritt-für-Schritt Anleitung.',

  ABANDONED_WORKFLOW_PATTERN:
    'Diesen Prozess haben Sie schon öfter begonnen. Soll ich Ihnen die Abkürzung zeigen?',

  COMPLEX_FORM_STRUGGLE:
    'Dieses Formular hat viele Felder. Möchten Sie nur die wichtigsten ausfüllen?',
};

export const ProactiveHelp: React.FC = () => {
  const { struggles, clearStruggles, startTour } = useHelpStore();

  const [showHelp, setShowHelp] = useState(false);
  const [helpAccepted, setHelpAccepted] = useState<boolean | null>(null);
  const [dismissed, setDismissed] = useState<Set<string>>(new Set());

  // Get the most recent struggle
  const currentStruggle = struggles[struggles.length - 1];

  useEffect(() => {
    if (currentStruggle && !dismissed.has(currentStruggle.type) && !helpAccepted) {
      // Wait a bit to not be too intrusive
      const timer = setTimeout(() => setShowHelp(true), 2000);
      return () => clearTimeout(timer);
    }
  }, [currentStruggle, dismissed, helpAccepted]);

  if (!currentStruggle || !showHelp) return null;

  const getSuggestions = (): HelpSuggestion[] => {
    const suggestions: HelpSuggestion[] = [];

    switch (currentStruggle.type) {
      case 'REPEATED_FAILED_ATTEMPTS':
        suggestions.push(
          {
            type: 'video',
            label: 'Video-Tutorial ansehen',
            icon: <PlayCircleIcon />,
            action: () => {
              // TODO: Open video tutorial
              console.log('Open video tutorial');
            },
          },
          {
            type: 'tour',
            label: 'Schritt-für-Schritt Anleitung',
            icon: <ListAltIcon />,
            action: () => startTour(currentStruggle.feature),
          },
          {
            type: 'support',
            label: 'Support kontaktieren',
            icon: <ContactSupportIcon />,
            action: () => {
              // TODO: Open support chat
              console.log('Open support chat');
            },
          }
        );
        break;

      case 'RAPID_NAVIGATION_CHANGES':
        suggestions.push(
          {
            type: 'tour',
            label: 'Zur Suche',
            icon: <SearchIcon />,
            action: () => {
              // TODO: Focus global search
              document.querySelector<HTMLInputElement>('[data-testid="global-search"]')?.focus();
            },
          },
          {
            type: 'tour',
            label: 'Sitemap anzeigen',
            icon: <AccountTreeIcon />,
            action: () => {
              // TODO: Show sitemap
              console.log('Show sitemap');
            },
          },
          {
            type: 'tour',
            label: 'Guided Tour starten',
            icon: <ExploreIcon />,
            action: () => startTour('navigation'),
          }
        );
        break;

      default:
        suggestions.push({
          type: 'tour',
          label: 'Hilfe anzeigen',
          icon: <PlayCircleIcon />,
          action: () => startTour(currentStruggle.feature),
        });
    }

    return suggestions;
  };

  const handleAcceptHelp = (suggestion: HelpSuggestion) => {
    suggestion.action();
    setHelpAccepted(true);
    setShowHelp(false);
    clearStruggles();
  };

  const handleDismiss = () => {
    setDismissed(prev => new Set(prev).add(currentStruggle.type));
    setShowHelp(false);
    setHelpAccepted(false);
  };

  const suggestions = getSuggestions();

  return (
    <CustomerFieldThemeProvider mode="anpassungsfähig">
      <Slide direction="up" in={showHelp} mountOnEnter unmountOnExit>
        <Paper
          sx={{
            position: 'fixed',
            bottom: 80,
            right: 20,
            p: 2,
            maxWidth: 350,
            boxShadow: 3,
            borderLeft: 4,
            borderColor: 'primary.main',
            zIndex: 1300,
          }}
        >
          <Box display="flex" alignItems="flex-start">
            <Avatar sx={{ bgcolor: 'primary.main', mr: 2 }}>
              <AssistantIcon />
            </Avatar>

            <Box flex={1}>
              <Typography variant="subtitle1" gutterBottom fontWeight="bold">
                Kann ich Ihnen helfen? 🤔
              </Typography>

              <Typography variant="body2" color="text.secondary" gutterBottom>
                {STRUGGLE_MESSAGES[currentStruggle.type]}
              </Typography>

              {/* Specific help options */}
              <Stack spacing={1} sx={{ mt: 2 }}>
                {suggestions.map((suggestion, idx) => (
                  <Button
                    key={idx}
                    size="small"
                    variant={idx === 0 ? 'contained' : 'outlined'}
                    startIcon={suggestion.icon}
                    onClick={() => handleAcceptHelp(suggestion)}
                    fullWidth
                  >
                    {suggestion.label}
                  </Button>
                ))}

                <Button size="small" variant="text" onClick={handleDismiss}>
                  Nein danke, ich schaffe das
                </Button>
              </Stack>

              {/* Show context if high severity */}
              {currentStruggle.severity === 'high' && (
                <Fade in timeout={1000}>
                  <Typography
                    variant="caption"
                    color="text.secondary"
                    sx={{ mt: 1, display: 'block' }}
                  >
                    {currentStruggle.attemptCount &&
                      `${currentStruggle.attemptCount} Versuche erkannt`}
                  </Typography>
                </Fade>
              )}
            </Box>

            <IconButton size="small" onClick={handleDismiss} sx={{ ml: 1 }}>
              <CloseIcon fontSize="small" />
            </IconButton>
          </Box>
        </Paper>
      </Slide>
    </CustomerFieldThemeProvider>
  );
};
