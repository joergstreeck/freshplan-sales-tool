import React, { useState, useEffect } from 'react';
import {
  Tooltip,
  Box,
  Typography,
  IconButton,
  Button,
  Fade,
  Paper,
  Divider,
  Alert
} from '@mui/material';
import {
  HelpOutline as HelpOutlineIcon,
  Close as CloseIcon,
  PlayCircleOutline as PlayCircleIcon,
  QuizOutlined as QuizIcon
} from '@mui/icons-material';
import { CustomerFieldThemeProvider } from '../../customers/theme/CustomerFieldThemeProvider';
import { useHelpStore } from '../stores/helpStore';

interface HelpTooltipProps {
  feature: string;
  children?: React.ReactNode;
  placement?: 'top' | 'bottom' | 'left' | 'right';
  forceOpen?: boolean;
}

export const HelpTooltip: React.FC<HelpTooltipProps> = ({
  feature,
  children,
  placement = 'right',
  forceOpen = false
}) => {
  const { 
    currentHelp, 
    tooltipOpen, 
    openTooltip, 
    closeTooltip, 
    loadHelpContent,
    trackView,
    openModal,
    startTour,
    loading,
    error
  } = useHelpStore();
  
  const [hasLoaded, setHasLoaded] = useState(false);
  const isOpen = tooltipOpen[feature] || forceOpen;
  
  // Find tooltip content for this feature
  const tooltipContent = currentHelp?.helpContents.find(
    h => h.feature === feature && h.helpType === 'TOOLTIP'
  );
  
  // Load help content when tooltip opens
  useEffect(() => {
    if (isOpen && !hasLoaded) {
      loadHelpContent(feature);
      setHasLoaded(true);
    }
  }, [isOpen, feature, hasLoaded, loadHelpContent]);
  
  // Track view when content is shown
  useEffect(() => {
    if (isOpen && tooltipContent) {
      trackView(tooltipContent.id);
    }
  }, [isOpen, tooltipContent, trackView]);
  
  const handleToggle = () => {
    if (isOpen) {
      closeTooltip(feature);
    } else {
      openTooltip(feature);
    }
  };
  
  const handleOpenModal = () => {
    if (tooltipContent) {
      openModal(tooltipContent);
      closeTooltip(feature);
    }
  };
  
  const handleStartTour = () => {
    startTour(feature);
    closeTooltip(feature);
  };
  
  const tooltipContentElement = (
    <CustomerFieldThemeProvider mode="anpassungsfähig">
      <Paper sx={{ p: 2, maxWidth: 350 }}>
        <Box display="flex" justifyContent="space-between" alignItems="start" mb={1}>
          <Typography variant="subtitle2" fontWeight="bold">
            {tooltipContent?.title || `Hilfe: ${feature}`}
          </Typography>
          <IconButton 
            size="small" 
            onClick={() => closeTooltip(feature)} 
            sx={{ ml: 1, mt: -0.5 }}
          >
            <CloseIcon fontSize="small" />
          </IconButton>
        </Box>
        
        {loading ? (
          <Typography variant="body2" color="text.secondary">
            Lade Hilfe...
          </Typography>
        ) : error ? (
          <Alert severity="error" sx={{ py: 0.5 }}>
            Hilfe konnte nicht geladen werden
          </Alert>
        ) : tooltipContent ? (
          <>
            <Typography variant="body2" color="text.secondary" paragraph>
              {tooltipContent.shortContent}
            </Typography>
            
            {tooltipContent.mediumContent && (
              <>
                <Divider sx={{ my: 1 }} />
                <Typography variant="body2" color="text.secondary" paragraph>
                  {tooltipContent.mediumContent}
                </Typography>
              </>
            )}
            
            <Box sx={{ mt: 2, display: 'flex', gap: 1 }}>
              {tooltipContent.detailedContent && (
                <Button 
                  size="small"
                  variant="outlined"
                  onClick={handleOpenModal}
                  startIcon={<QuizIcon />}
                >
                  Mehr Details
                </Button>
              )}
              
              {currentHelp?.helpContents.some(h => h.helpType === 'TOUR') && (
                <Button 
                  size="small"
                  variant="contained"
                  onClick={handleStartTour}
                  startIcon={<PlayCircleIcon />}
                >
                  Tour starten
                </Button>
              )}
            </Box>
            
            {/* Confidence Indicator */}
            {currentHelp?.context && (
              <Box sx={{ mt: 2 }}>
                <Typography variant="caption" color="text.secondary">
                  Angepasst für: {currentHelp.context.userLevel}
                  {currentHelp.context.isFirstTime && ' (Erste Nutzung)'}
                </Typography>
              </Box>
            )}
          </>
        ) : (
          <Typography variant="body2" color="text.secondary">
            Keine Hilfe für dieses Feature verfügbar.
          </Typography>
        )}
      </Paper>
    </CustomerFieldThemeProvider>
  );
  
  return (
    <Tooltip
      title={tooltipContentElement}
      placement={placement}
      arrow
      open={isOpen}
      onClose={() => closeTooltip(feature)}
      disableFocusListener
      disableHoverListener
      disableTouchListener
      TransitionComponent={Fade}
      TransitionProps={{ timeout: 300 }}
      PopperProps={{
        disablePortal: true,
      }}
    >
      <Box display="inline-flex" alignItems="center">
        {children}
        <IconButton
          size="small"
          onClick={handleToggle}
          sx={{ 
            ml: children ? 0.5 : 0,
            color: 'action.active',
            '&:hover': {
              color: 'primary.main'
            }
          }}
          aria-label={`Hilfe für ${feature}`}
        >
          <HelpOutlineIcon fontSize="small" />
        </IconButton>
      </Box>
    </Tooltip>
  );
};