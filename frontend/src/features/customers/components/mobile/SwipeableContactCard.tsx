/**
 * SwipeableContactCard Component
 *
 * Touch-optimized contact card with swipe gestures for quick actions.
 * Part of FC-005 Contact Management UI - Mobile Actions.
 *
 * @see /docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/MOBILE_CONTACT_ACTIONS.md
 */

import React, { useState, useEffect } from 'react';
import { useSwipeable } from 'react-swipeable';
import { Box, Card, CardContent, Typography, Fade, IconButton, Zoom } from '@mui/material';
import { Phone as PhoneIcon, Email as EmailIcon, Close as CloseIcon } from '@mui/icons-material';

import { SmartContactCard } from '../contacts/SmartContactCard';
import { MobileActionBar } from './MobileActionBar';
import { actionSuggestionService } from './ActionSuggestionService';
import type { Contact } from '../../types/contact.types';
import type { RelationshipWarmth } from '../contacts/WarmthIndicator';
import type {
  QuickAction,
  SwipeActions,
  ContactIntelligence,
} from '../../types/mobileActions.types';

interface SwipeableContactCardProps {
  contact: Contact;
  warmth?: RelationshipWarmth;
  intelligence?: ContactIntelligence;
  onAction: (action: QuickAction, contactId: string) => void;
  onEdit?: (contact: Contact) => void;
  onDelete?: (contactId: string) => void;
  onSetPrimary?: (contactId: string) => void;
  showInstructions?: boolean;
}

/**
 * Swipeable contact card for mobile devices
 */
export const SwipeableContactCard: React.FC<SwipeableContactCardProps> = ({
  contact,
  warmth,
  intelligence,
  onAction,
  onEdit,
  onDelete,
  onSetPrimary,
  showInstructions = true,
}) => {
  const [swipeActions, setSwipeActions] = useState<SwipeActions>();
  const [swipeProgress, setSwipeProgress] = useState(0);
  const [swipeDirection, setSwipeDirection] = useState<'left' | 'right' | null>(null);
  const [showSwipeInstructions, setShowSwipeInstructions] = useState(false);

  useEffect(() => {
    const actions = actionSuggestionService.getSwipeActions(contact, intelligence);
    setSwipeActions(actions);

    // Show instructions on first use
    if (showInstructions && !localStorage.getItem('swipe-instructions-seen')) {
      setShowSwipeInstructions(true);
    }
  }, [contact, intelligence, showInstructions]);

  const handlers = useSwipeable({
    onSwiping: eventData => {
      const progress = Math.min(Math.abs(eventData.deltaX) / 100, 1);
      setSwipeProgress(progress);
      setSwipeDirection(eventData.deltaX < 0 ? 'left' : 'right');
    },
    onSwipedLeft: () => {
      if (swipeActions?.left && swipeActions.left.enabled) {
        // Haptic feedback
        if ('vibrate' in navigator) {
          navigator.vibrate(50);
        }
        onAction(swipeActions.left, contact.id);
        resetSwipe();
      }
    },
    onSwipedRight: () => {
      if (swipeActions?.right && swipeActions.right.enabled) {
        // Haptic feedback
        if ('vibrate' in navigator) {
          navigator.vibrate(50);
        }
        onAction(swipeActions.right, contact.id);
        resetSwipe();
      }
    },
    onTouchEndOrOnMouseUp: () => {
      if (swipeProgress < 0.5) {
        resetSwipe();
      }
    },
    preventScrollOnSwipe: true,
    trackMouse: false, // Touch only for mobile
    delta: 10,
    swipeDuration: 500,
    velocity: 0.5,
  });

  const resetSwipe = () => {
    setSwipeProgress(0);
    setSwipeDirection(null);
  };

  const dismissInstructions = () => {
    setShowSwipeInstructions(false);
    localStorage.setItem('swipe-instructions-seen', 'true');
  };

  return (
    <Box
      {...handlers}
      sx={{
        position: 'relative',
        touchAction: 'pan-y',
        userSelect: 'none',
        overflow: 'hidden',
      }}
    >
      {/* Swipe Action Indicators */}
      {swipeDirection === 'left' && swipeActions?.left && (
        <SwipeIndicator direction="left" action={swipeActions.left} progress={swipeProgress} />
      )}
      {swipeDirection === 'right' && swipeActions?.right && (
        <SwipeIndicator direction="right" action={swipeActions.right} progress={swipeProgress} />
      )}

      {/* Main Card with Transform */}
      <Box
        sx={{
          transform: swipeDirection
            ? `translateX(${
                swipeDirection === 'right' ? swipeProgress * 50 : -swipeProgress * 50
              }px) rotate(${swipeDirection === 'right' ? swipeProgress * 2 : -swipeProgress * 2}deg)`
            : 'translateX(0)',
          transition: swipeProgress === 0 ? 'all 0.3s ease' : 'none',
          opacity: 1 - swipeProgress * 0.2,
          transformOrigin: 'center bottom',
        }}
      >
        <SmartContactCard
          contact={contact}
          warmth={warmth}
          onEdit={onEdit || (() => {})}
          onDelete={onDelete || (() => {})}
          onSetPrimary={onSetPrimary || (() => {})}
          onQuickAction={(action, contactId) => onAction({ ...action } as QuickAction, contactId)}
          showQuickActions={!actionSuggestionService.isMobileDevice()}
        />

        {/* Mobile Action Bar for non-swipe actions */}
        {actionSuggestionService.isMobileDevice() && (
          <MobileActionBar
            contact={contact}
            suggestions={actionSuggestionService.getSuggestedActions(contact, intelligence)}
            onAction={action => onAction(action, contact.id)}
          />
        )}
      </Box>

      {/* Swipe Instructions Overlay */}
      {showSwipeInstructions && <SwipeInstructions onDismiss={dismissInstructions} />}
    </Box>
  );
};

/**
 * Swipe indicator component
 */
const SwipeIndicator: React.FC<{
  direction: 'left' | 'right';
  action: QuickAction;
  progress: number;
}> = ({ direction, action, progress }) => {
  return (
    <Zoom in={progress > 0.1}>
      <Box
        sx={{
          position: 'absolute',
          top: '50%',
          [direction]: 16,
          transform: 'translateY(-50%)',
          backgroundColor: action.color,
          color: 'white',
          borderRadius: '50%',
          width: 56 + progress * 20,
          height: 56 + progress * 20,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          opacity: Math.min(progress * 2, 1),
          zIndex: 10,
          transition: 'all 0.2s ease',
          boxShadow: `0 4px ${8 + progress * 8}px rgba(0,0,0,0.2)`,
        }}
      >
        <Box
          sx={{
            transform: `scale(${1 + progress * 0.2})`,
            transition: 'transform 0.2s ease',
          }}
        >
          {action.icon}
        </Box>
      </Box>
    </Zoom>
  );
};

/**
 * Swipe instructions overlay
 */
const SwipeInstructions: React.FC<{
  onDismiss: () => void;
}> = ({ onDismiss }) => {
  return (
    <Fade in={true}>
      <Box
        onClick={onDismiss}
        sx={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(0, 0, 0, 0.8)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 1000,
          padding: 3,
        }}
      >
        <Card
          sx={{
            maxWidth: 350,
            textAlign: 'center',
            position: 'relative',
            p: 2,
          }}
        >
          <IconButton
            onClick={onDismiss}
            sx={{
              position: 'absolute',
              top: 8,
              right: 8,
            }}
          >
            <CloseIcon />
          </IconButton>

          <CardContent>
            <Typography variant="h5" gutterBottom sx={{ fontWeight: 'bold' }}>
              Swipe für Schnellaktionen
            </Typography>

            <Box sx={{ my: 3 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                <EmailIcon sx={{ fontSize: 40, color: 'info.main', mr: 2 }} />
                <Typography variant="body1">👈 Nach links für E-Mail</Typography>
              </Box>

              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                <Typography variant="body1" sx={{ mr: 2 }}>
                  Nach rechts für Anruf 👉
                </Typography>
                <PhoneIcon sx={{ fontSize: 40, color: 'success.main' }} />
              </Box>
            </Box>

            <Typography variant="caption" color="text.secondary">
              Diese Meldung wird nur einmal angezeigt
            </Typography>
          </CardContent>
        </Card>
      </Box>
    </Fade>
  );
};
