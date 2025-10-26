import React, { useState } from 'react';
import { useTheme } from '@mui/material/styles';
import { getSecureBoolean, setSecureBoolean } from '@/utils/secureStorage';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Typography,
  IconButton,
  Tooltip,
  Fab,
  Box,
  Chip,
} from '@mui/material';
import { Keyboard as KeyboardIcon, Close as CloseIcon } from '@mui/icons-material';
import { KEYBOARD_SHORTCUTS } from '@/hooks/useKeyboardNavigation';

export const KeyboardShortcutsHelp: React.FC = () => {
  const theme = useTheme();
  const [open, setOpen] = useState(false);
  const [hasSeenHelp, setHasSeenHelp] = useState(() => {
    return getSecureBoolean('hasSeenKeyboardHelp', false);
  });

  const handleOpen = () => {
    setOpen(true);
    setHasSeenHelp(true);
    setSecureBoolean('hasSeenKeyboardHelp', true);
  };

  const handleClose = () => {
    setOpen(false);
  };

  // Keyboard shortcut to open help (?)
  React.useEffect(() => {
    const handleKeyPress = (e: KeyboardEvent) => {
      if (e.key === '?' && e.shiftKey) {
        e.preventDefault();
        handleOpen();
      }
    };

    window.addEventListener('keydown', handleKeyPress);
    return () => window.removeEventListener('keydown', handleKeyPress);
  }, []);

  return (
    <>
      {/* Floating Help Button - Top Right */}
      <Tooltip
        title={
          <Box>
            <Typography variant="body2">Tastaturkürzel anzeigen</Typography>
            <Typography variant="caption" sx={{ opacity: 0.8 }}>
              Drücke Shift + ? für Hilfe
            </Typography>
          </Box>
        }
        placement="left"
      >
        <Fab
          size="medium"
          onClick={handleOpen}
          sx={{
            position: 'fixed',
            top: 80, // Unter dem Header positioniert
            right: 32,
            zIndex: 1200, // Über dem Content aber unter Modals
            backgroundColor: hasSeenHelp ? theme.palette.primary.main : theme.palette.success.main,
            color: theme.palette.common.white,
            '&:hover': {
              backgroundColor: hasSeenHelp
                ? theme.palette.primary.dark
                : theme.palette.success.dark,
            },
            animation: hasSeenHelp ? 'none' : 'pulse 2s infinite',
            '@keyframes pulse': {
              '0%': {
                transform: 'scale(1)',
                opacity: 1,
              },
              '70%': {
                transform: 'scale(1.1)',
                opacity: 0.7,
              },
              '100%': {
                transform: 'scale(1)',
                opacity: 1,
              },
            },
          }}
        >
          <KeyboardIcon />
        </Fab>
      </Tooltip>

      {/* Keyboard Shortcuts Dialog */}
      <Dialog
        open={open}
        onClose={handleClose}
        maxWidth="md"
        fullWidth
        PaperProps={{
          sx: {
            borderRadius: 2,
            border: `2px solid ${theme.palette.success.main}`,
          },
        }}
      >
        <DialogTitle
          sx={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            backgroundColor: theme.palette.primary.main,
            color: theme.palette.common.white,
          }}
        >
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <KeyboardIcon />
            <Typography variant="h6">Tastaturkürzel</Typography>
          </Box>
          <IconButton onClick={handleClose} sx={{ color: 'white' }}>
            <CloseIcon />
          </IconButton>
        </DialogTitle>

        <DialogContent sx={{ p: 0 }}>
          <TableContainer component={Paper} elevation={0}>
            <Table>
              <TableHead>
                <TableRow sx={{ backgroundColor: theme.palette.grey[100] }}>
                  <TableCell sx={{ fontWeight: 600, color: theme.palette.primary.main }}>
                    Tastenkombination
                  </TableCell>
                  <TableCell sx={{ fontWeight: 600, color: theme.palette.primary.main }}>
                    Aktion
                  </TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {Object.entries(KEYBOARD_SHORTCUTS).map(([key, action]) => (
                  <TableRow
                    key={key}
                    sx={{
                      '&:hover': { backgroundColor: theme.palette.action.hover },
                    }}
                  >
                    <TableCell>
                      <Box sx={{ display: 'flex', gap: 0.5 }}>
                        {key.split(' ').map((part, index) => {
                          if (part === '+') {
                            return (
                              <Typography key={index} variant="body2" sx={{ px: 0.5 }}>
                                +
                              </Typography>
                            );
                          }
                          return (
                            <Chip
                              key={index}
                              label={part}
                              size="small"
                              sx={{
                                height: 24,
                                backgroundColor: theme.palette.grey[200],
                                border: `1px solid ${theme.palette.divider}`,
                                borderRadius: 1,
                                fontFamily: '"Courier New", Courier, monospace',
                                fontSize: '0.875rem',
                              }}
                            />
                          );
                        })}
                      </Box>
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2">{action}</Typography>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>

          {/* Additional Tips */}
          <Box
            sx={{
              p: 2,
              backgroundColor: theme.palette.grey[50],
              borderTop: `1px solid ${theme.palette.divider}`,
            }}
          >
            <Typography variant="subtitle2" sx={{ mb: 1, color: theme.palette.primary.main }}>
              💡 Tipps:
            </Typography>
            <Typography variant="body2" sx={{ mb: 0.5 }}>
              • Die Shortcuts funktionieren nur, wenn kein Eingabefeld aktiv ist
            </Typography>
            <Typography variant="body2" sx={{ mb: 0.5 }}>
              • Nutze <strong>Tab</strong> und <strong>Shift+Tab</strong> für die
              Standard-Navigation
            </Typography>
            <Typography variant="body2">
              • Diese Hilfe öffnet sich mit <strong>Shift + ?</strong>
            </Typography>
          </Box>
        </DialogContent>

        <DialogActions sx={{ p: 2, backgroundColor: theme.palette.grey[100] }}>
          <Button
            onClick={handleClose}
            variant="contained"
            sx={{
              backgroundColor: theme.palette.success.main,
              '&:hover': { backgroundColor: theme.palette.success.dark },
            }}
          >
            Verstanden
          </Button>
        </DialogActions>
      </Dialog>
    </>
  );
};
