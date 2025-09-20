# 🆘 In-App Help System - Proaktive Unterstützung statt Frustration

**Phase:** 0 - Critical Foundation  
**Priorität:** 🔴 KRITISCH - Ohne Hilfe = Keine Adoption  
**Status:** 📋 GEPLANT  
**Letzte Aktualisierung:** 08.08.2025  
**Claude-Ready:** ✅ Vollständig navigierbar

## 🧭 NAVIGATION FÜR CLAUDE

**← Zurück:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/COST_MANAGEMENT_EXTERNAL_SERVICES.md`  
**→ Nächster:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/FEATURE_ADOPTION_TRACKING.md`  
**↑ Parent:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/README.md`  
**⚠️ Wichtig für:**
- Alle Intelligence Features
- Complex UI Components
- Mobile Features

## ⚠️ WICHTIG: MUI Grid v2 Syntax

**Help-Komponenten müssen MUI v7 Grid v2 Syntax verwenden:**
```typescript
// ✅ RICHTIG - Grid v2 für Help Layouts
import { Grid } from '@mui/material';

<Grid container spacing={2}>
  <Grid size={{ xs: 12, md: 8 }}>
    <HelpContent />
  </Grid>
  <Grid size={{ xs: 12, md: 4 }}>
    <HelpSidebar />
  </Grid>
</Grid>

// ❌ FALSCH - Alte Grid v1 Syntax
<Grid item xs={12} md={8}>  // item prop existiert nicht mehr!
```

**Siehe:** [MUI Grid Migration Guide](/Users/joergstreeck/freshplan-sales-tool/docs/guides/DEBUG_COOKBOOK.md#mui-grid-v2)

## ⚡ Quick Implementation Guide für Claude

```bash
# SOFORT STARTEN - In-App Help implementieren:
cd /Users/joergstreeck/freshplan-sales-tool

# 1. Help Service & Detection
mkdir -p frontend/src/features/help/services
touch frontend/src/features/help/services/StruggleDetectionService.ts
touch frontend/src/features/help/services/HelpContentService.ts
touch frontend/src/features/help/services/OnboardingService.ts

# 2. Help Components
mkdir -p frontend/src/features/help/components
touch frontend/src/features/help/components/ContextualTooltip.tsx
touch frontend/src/features/help/components/FeatureSpotlight.tsx
touch frontend/src/features/help/components/InteractiveTour.tsx
touch frontend/src/features/help/components/HelpFloatingButton.tsx

# 3. Help Store
mkdir -p frontend/src/features/help/stores
touch frontend/src/features/help/stores/helpStore.ts
touch frontend/src/features/help/types/help.types.ts

# 4. Tests
mkdir -p frontend/src/features/help/__tests__
touch frontend/src/features/help/__tests__/StruggleDetection.test.ts
```

## 🎯 Das Problem: Komplexe Features überfordern User

**Frustrations-Quellen:**
- 🌡️ "Was bedeutet Warmth Score 73?"
- 📊 "Warum ist der Kontakt rot?"
- 💡 "Was soll ich mit den Suggestions machen?"
- 📱 "Wie funktioniert Swipe auf Desktop?"
- 🔄 "Wo sind meine Offline-Änderungen?"

**Typische User-Reaktionen:**
- Hovern mehrfach über unklare Elemente
- Klicken wild herum
- Verlassen Formulare unausgefüllt
- Rufen Support an für Basis-Fragen

## 💡 DIE LÖSUNG: Proaktive Frustrations-Erkennung

### 1. Struggle Detection Service

**Datei:** `frontend/src/features/help/services/StruggleDetectionService.ts`

```typescript
// CLAUDE: Intelligente Erkennung von User-Frustration
// Pfad: frontend/src/features/help/services/StruggleDetectionService.ts

import { EventEmitter } from 'events';

export enum StrugglePattern {
  HOVERING = 'hovering',           // 3+ mal über Element
  CLICKING_AROUND = 'clicking',    // 5+ Klicks in 10s
  FORM_ABANDONMENT = 'abandonment',// Form nach 30s verlassen
  REPEATED_ACTION = 'repeated',    // Gleiche Aktion 3x
  SCROLL_CONFUSION = 'scrolling',  // Hektisches Scrollen
  IDLE_CONFUSION = 'idle',         // 20s keine Aktion
  ERROR_LOOP = 'error_loop'        // Gleicher Fehler 2x
}

export interface StruggleEvent {
  pattern: StrugglePattern;
  element?: HTMLElement;
  context: {
    page: string;
    feature: string;
    timestamp: Date;
    sessionDuration: number;
  };
  severity: 'low' | 'medium' | 'high';
  suggestedHelp: string;
}

export class StruggleDetectionService extends EventEmitter {
  private hoverCount = new Map<string, number>();
  private clickBuffer: { timestamp: Date; target: string }[] = [];
  private formStartTime = new Map<string, Date>();
  private actionHistory: string[] = [];
  private lastActivity = new Date();
  private errorCount = new Map<string, number>();
  
  constructor() {
    super();
    this.initializeListeners();
  }
  
  private initializeListeners() {
    // Hover Detection
    document.addEventListener('mouseover', (e) => {
      const target = e.target as HTMLElement;
      const identifier = this.getElementIdentifier(target);
      
      // Track hovering over help-eligible elements
      if (this.isHelpEligible(target)) {
        const count = (this.hoverCount.get(identifier) || 0) + 1;
        this.hoverCount.set(identifier, count);
        
        if (count === 3) {
          this.detectStruggle(StrugglePattern.HOVERING, target, 'medium');
          this.hoverCount.delete(identifier);
        }
        
        // Reset after 5 seconds
        setTimeout(() => {
          const current = this.hoverCount.get(identifier);
          if (current && current > 0) {
            this.hoverCount.set(identifier, current - 1);
          }
        }, 5000);
      }
    });
    
    // Click Pattern Detection
    document.addEventListener('click', (e) => {
      const now = new Date();
      const target = this.getElementIdentifier(e.target as HTMLElement);
      
      // Add to buffer
      this.clickBuffer.push({ timestamp: now, target });
      
      // Keep only last 10 seconds
      this.clickBuffer = this.clickBuffer.filter(
        click => now.getTime() - click.timestamp.getTime() < 10000
      );
      
      // Detect rapid clicking
      if (this.clickBuffer.length >= 5) {
        const uniqueTargets = new Set(this.clickBuffer.map(c => c.target));
        if (uniqueTargets.size >= 3) {
          this.detectStruggle(StrugglePattern.CLICKING_AROUND, null, 'high');
          this.clickBuffer = [];
        }
      }
      
      // Track repeated actions
      this.actionHistory.push(target);
      if (this.actionHistory.length > 10) {
        this.actionHistory.shift();
      }
      
      // Check for repeated actions
      const last3 = this.actionHistory.slice(-3);
      if (last3.length === 3 && last3.every(a => a === last3[0])) {
        this.detectStruggle(StrugglePattern.REPEATED_ACTION, e.target as HTMLElement, 'medium');
        this.actionHistory = [];
      }
      
      this.lastActivity = now;
    });
    
    // Form Abandonment Detection
    document.addEventListener('focusin', (e) => {
      const form = (e.target as HTMLElement).closest('form');
      if (form) {
        const formId = this.getElementIdentifier(form);
        if (!this.formStartTime.has(formId)) {
          this.formStartTime.set(formId, new Date());
        }
      }
    });
    
    document.addEventListener('focusout', (e) => {
      const form = (e.target as HTMLElement).closest('form');
      if (form && !form.contains(e.relatedTarget as Node)) {
        const formId = this.getElementIdentifier(form);
        const startTime = this.formStartTime.get(formId);
        
        if (startTime) {
          const duration = new Date().getTime() - startTime.getTime();
          
          // Form abandoned after 30 seconds without submission
          if (duration > 30000 && !this.isFormComplete(form)) {
            this.detectStruggle(StrugglePattern.FORM_ABANDONMENT, form, 'high');
            this.formStartTime.delete(formId);
          }
        }
      }
    });
    
    // Idle Confusion Detection
    setInterval(() => {
      const idleTime = new Date().getTime() - this.lastActivity.getTime();
      
      // 20 seconds of inactivity on complex page
      if (idleTime > 20000 && idleTime < 25000) {
        if (this.isComplexPage()) {
          this.detectStruggle(StrugglePattern.IDLE_CONFUSION, null, 'low');
        }
      }
    }, 5000);
    
    // Error Loop Detection
    window.addEventListener('error', (e) => {
      const errorKey = e.message || 'unknown';
      const count = (this.errorCount.get(errorKey) || 0) + 1;
      this.errorCount.set(errorKey, count);
      
      if (count >= 2) {
        this.detectStruggle(StrugglePattern.ERROR_LOOP, null, 'high');
        this.errorCount.delete(errorKey);
      }
      
      // Clear after 30 seconds
      setTimeout(() => {
        this.errorCount.delete(errorKey);
      }, 30000);
    });
  }
  
  private detectStruggle(
    pattern: StrugglePattern, 
    element: HTMLElement | null,
    severity: 'low' | 'medium' | 'high'
  ) {
    const event: StruggleEvent = {
      pattern,
      element: element || undefined,
      context: {
        page: window.location.pathname,
        feature: this.detectFeature(element),
        timestamp: new Date(),
        sessionDuration: this.getSessionDuration()
      },
      severity,
      suggestedHelp: this.getSuggestedHelp(pattern, element)
    };
    
    this.emit('struggle-detected', event);
    
    // Log for analytics
    console.log('[Struggle Detection]', {
      pattern,
      severity,
      feature: event.context.feature,
      suggestion: event.suggestedHelp
    });
  }
  
  private isHelpEligible(element: HTMLElement): boolean {
    // Elements that commonly need help
    const helpAttributes = [
      'data-warmth-score',
      'data-suggestion',
      'data-timeline',
      'data-intelligence',
      'data-complex-feature'
    ];
    
    return helpAttributes.some(attr => element.hasAttribute(attr)) ||
           element.classList.contains('help-eligible') ||
           element.closest('[data-help-topic]') !== null;
  }
  
  private isFormComplete(form: HTMLElement): boolean {
    const requiredFields = form.querySelectorAll('[required]');
    return Array.from(requiredFields).every(field => {
      return (field as HTMLInputElement).value.trim() !== '';
    });
  }
  
  private isComplexPage(): boolean {
    const complexFeatures = [
      'warmth-indicator',
      'timeline',
      'suggestions',
      'contact-intelligence'
    ];
    
    return complexFeatures.some(feature => 
      document.querySelector(`[data-feature="${feature}"]`) !== null
    );
  }
  
  private getElementIdentifier(element: HTMLElement): string {
    return element.id || 
           element.getAttribute('data-testid') || 
           element.className ||
           element.tagName;
  }
  
  private detectFeature(element: HTMLElement | null): string {
    if (!element) return 'general';
    
    const featureMap = {
      'warmth': 'Relationship Intelligence',
      'suggestion': 'Smart Suggestions',
      'timeline': 'Contact Timeline',
      'contact-card': 'Contact Management',
      'budget': 'Cost Management'
    };
    
    for (const [key, feature] of Object.entries(featureMap)) {
      if (element.className.includes(key) || 
          element.id.includes(key) ||
          element.closest(`[data-feature="${key}"]`)) {
        return feature;
      }
    }
    
    return 'general';
  }
  
  private getSuggestedHelp(pattern: StrugglePattern, element: HTMLElement | null): string {
    const suggestions: Record<StrugglePattern, string> = {
      [StrugglePattern.HOVERING]: '💡 Möchten Sie mehr über dieses Feature erfahren?',
      [StrugglePattern.CLICKING_AROUND]: '🤔 Suchen Sie etwas Bestimmtes? Ich kann helfen!',
      [StrugglePattern.FORM_ABANDONMENT]: '📝 Brauchen Sie Hilfe beim Ausfüllen?',
      [StrugglePattern.REPEATED_ACTION]: '🔄 Das scheint nicht zu funktionieren. Soll ich helfen?',
      [StrugglePattern.SCROLL_CONFUSION]: '📍 Verloren? Hier ist eine Übersicht der Seite.',
      [StrugglePattern.IDLE_CONFUSION]: '💭 Brauchen Sie eine kurze Einführung?',
      [StrugglePattern.ERROR_LOOP]: '⚠️ Es gibt ein Problem. Soll ich eine Alternative zeigen?'
    };
    
    return suggestions[pattern];
  }
  
  private getSessionDuration(): number {
    // Get from session storage or calculate
    const sessionStart = parseInt(sessionStorage.getItem('sessionStart') || '0');
    if (!sessionStart) {
      const now = Date.now();
      sessionStorage.setItem('sessionStart', now.toString());
      return 0;
    }
    return Date.now() - sessionStart;
  }
}

// Singleton instance
export const struggleDetection = new StruggleDetectionService();
```

### 2. Contextual Help Component

**Datei:** `frontend/src/features/help/components/ContextualTooltip.tsx`

```typescript
// CLAUDE: Intelligente kontextuelle Hilfe-Tooltips
// Pfad: frontend/src/features/help/components/ContextualTooltip.tsx

import React, { useState, useEffect, useRef } from 'react';
import {
  Tooltip,
  Box,
  Typography,
  IconButton,
  Button,
  Fade,
  Paper,
  Chip,
  LinearProgress
} from '@mui/material';
import {
  HelpOutline as HelpIcon,
  Close as CloseIcon,
  PlayArrow as PlayIcon,
  NavigateNext as NextIcon,
  Check as CheckIcon,
  Lightbulb as TipIcon
} from '@mui/icons-material';
import { helpService } from '../services/HelpContentService';
import { useHelpStore } from '../stores/helpStore';

interface ContextualTooltipProps {
  topic: string;
  children: React.ReactElement;
  forceShow?: boolean;
  intensity?: 'subtle' | 'moderate' | 'prominent';
}

export const ContextualTooltip: React.FC<ContextualTooltipProps> = ({
  topic,
  children,
  forceShow = false,
  intensity = 'subtle'
}) => {
  const [open, setOpen] = useState(false);
  const [content, setContent] = useState<HelpContent | null>(null);
  const [showCount, setShowCount] = useState(0);
  const [dismissed, setDismissed] = useState(false);
  const { markTopicViewed, getTopicViewCount } = useHelpStore();
  const timeoutRef = useRef<NodeJS.Timeout>();
  
  useEffect(() => {
    loadContent();
    const viewCount = getTopicViewCount(topic);
    setShowCount(viewCount);
    
    // Auto-show based on struggle detection
    const handleStruggle = (event: CustomEvent) => {
      if (event.detail.suggestedHelp === topic && !dismissed) {
        setOpen(true);
      }
    };
    
    window.addEventListener('struggle-detected', handleStruggle as EventListener);
    return () => {
      window.removeEventListener('struggle-detected', handleStruggle as EventListener);
    };
  }, [topic]);
  
  const loadContent = async () => {
    const helpContent = await helpService.getHelpContent(topic);
    setContent(helpContent);
  };
  
  const handleOpen = () => {
    if (dismissed && showCount >= 3) return; // Respect user's choice after 3 times
    
    setOpen(true);
    markTopicViewed(topic);
    setShowCount(prev => prev + 1);
    
    // Auto-close for subtle hints
    if (intensity === 'subtle' && showCount > 0) {
      timeoutRef.current = setTimeout(() => {
        setOpen(false);
      }, 5000);
    }
  };
  
  const handleClose = () => {
    setOpen(false);
    if (timeoutRef.current) {
      clearTimeout(timeoutRef.current);
    }
  };
  
  const handleDismiss = () => {
    setDismissed(true);
    handleClose();
    helpService.dismissTopic(topic);
  };
  
  const getTooltipContent = () => {
    if (!content) return null;
    
    // Adaptive intensity based on view count
    const currentIntensity = showCount === 0 ? 'prominent' : 
                           showCount === 1 ? 'moderate' : 
                           'subtle';
    
    switch (currentIntensity) {
      case 'subtle':
        return (
          <Box sx={{ maxWidth: 200 }}>
            <Typography variant="caption">
              {content.quickTip}
            </Typography>
          </Box>
        );
        
      case 'moderate':
        return (
          <Paper sx={{ p: 2, maxWidth: 300 }}>
            <Box display="flex" alignItems="center" mb={1}>
              <TipIcon sx={{ mr: 1, color: 'warning.main' }} />
              <Typography variant="subtitle2" fontWeight="bold">
                {content.title}
              </Typography>
            </Box>
            <Typography variant="body2" paragraph>
              {content.description}
            </Typography>
            {content.example && (
              <Box sx={{ bgcolor: 'grey.100', p: 1, borderRadius: 1, mb: 1 }}>
                <Typography variant="caption" component="pre">
                  {content.example}
                </Typography>
              </Box>
            )}
            <Box display="flex" gap={1}>
              <Button size="small" startIcon={<PlayIcon />} onClick={startTour}>
                Tour starten
              </Button>
              <Button size="small" variant="text" onClick={handleDismiss}>
                Verstanden
              </Button>
            </Box>
          </Paper>
        );
        
      case 'prominent':
        return (
          <Paper sx={{ p: 3, maxWidth: 400 }}>
            <Box display="flex" justifyContent="space-between" alignItems="start" mb={2}>
              <Box>
                <Typography variant="h6" gutterBottom>
                  {content.title}
                </Typography>
                <Chip label="Neu für Sie" color="primary" size="small" />
              </Box>
              <IconButton size="small" onClick={handleClose}>
                <CloseIcon />
              </IconButton>
            </Box>
            
            <Typography variant="body2" paragraph>
              {content.description}
            </Typography>
            
            {content.steps && (
              <Box mb={2}>
                <Typography variant="subtitle2" gutterBottom>
                  So funktioniert's:
                </Typography>
                <Box component="ol" sx={{ pl: 2, m: 0 }}>
                  {content.steps.map((step, index) => (
                    <Typography key={index} component="li" variant="body2" sx={{ mb: 0.5 }}>
                      {step}
                    </Typography>
                  ))}
                </Box>
              </Box>
            )}
            
            {content.video && (
              <Box 
                sx={{ 
                  bgcolor: 'black', 
                  borderRadius: 1, 
                  height: 150, 
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  mb: 2,
                  cursor: 'pointer'
                }}
                onClick={() => playVideo(content.video)}
              >
                <PlayIcon sx={{ fontSize: 48, color: 'white' }} />
              </Box>
            )}
            
            <Box display="flex" gap={1}>
              <Button 
                variant="contained" 
                startIcon={<PlayIcon />}
                onClick={startInteractiveTour}
                fullWidth
              >
                Interaktive Tour (2 Min)
              </Button>
            </Box>
            
            <Box display="flex" justifyContent="space-between" mt={2}>
              <Button size="small" variant="text" onClick={handleDismiss}>
                Nicht mehr zeigen
              </Button>
              <Button size="small" variant="text" onClick={() => setOpen(false)}>
                Später
              </Button>
            </Box>
          </Paper>
        );
    }
  };
  
  const startTour = () => {
    helpService.startTour(topic);
    handleClose();
  };
  
  const startInteractiveTour = () => {
    helpService.startInteractiveTour(topic);
    handleClose();
  };
  
  const playVideo = (videoUrl: string) => {
    helpService.playVideo(videoUrl);
  };
  
  // Don't show if user has dismissed it 3+ times
  if (dismissed && showCount >= 3 && !forceShow) {
    return children;
  }
  
  return (
    <Tooltip
      title={getTooltipContent()}
      open={open || forceShow}
      onOpen={handleOpen}
      onClose={handleClose}
      arrow
      placement="top"
      TransitionComponent={Fade}
      TransitionProps={{ timeout: 600 }}
      PopperProps={{
        sx: {
          '& .MuiTooltip-tooltip': {
            bgcolor: 'transparent',
            maxWidth: 'none',
            p: 0
          }
        }
      }}
    >
      {React.cloneElement(children, {
        'data-help-topic': topic,
        className: `${children.props.className || ''} help-eligible`
      })}
    </Tooltip>
  );
};
```

### 3. Feature Spotlight

**Datei:** `frontend/src/features/help/components/FeatureSpotlight.tsx`

```typescript
// CLAUDE: Spotlight für neue Features
// Pfad: frontend/src/features/help/components/FeatureSpotlight.tsx

import React, { useState, useEffect } from 'react';
import { 
  Box, 
  Paper, 
  Typography, 
  IconButton,
  Button,
  Fade,
  Backdrop
} from '@mui/material';
import { 
  Close as CloseIcon,
  ArrowForward as NextIcon,
  ArrowBack as PrevIcon
} from '@mui/icons-material';

interface SpotlightStep {
  target: string; // CSS selector
  title: string;
  content: string;
  placement: 'top' | 'bottom' | 'left' | 'right';
  action?: {
    label: string;
    onClick: () => void;
  };
}

interface FeatureSpotlightProps {
  steps: SpotlightStep[];
  onComplete: () => void;
  onSkip: () => void;
}

export const FeatureSpotlight: React.FC<FeatureSpotlightProps> = ({
  steps,
  onComplete,
  onSkip
}) => {
  const [currentStep, setCurrentStep] = useState(0);
  const [position, setPosition] = useState({ top: 0, left: 0 });
  const [targetRect, setTargetRect] = useState<DOMRect | null>(null);
  
  useEffect(() => {
    highlightCurrentTarget();
  }, [currentStep]);
  
  const highlightCurrentTarget = () => {
    const target = document.querySelector(steps[currentStep].target);
    if (target) {
      const rect = target.getBoundingClientRect();
      setTargetRect(rect);
      
      // Calculate tooltip position
      const placement = steps[currentStep].placement;
      let top = 0, left = 0;
      
      switch (placement) {
        case 'top':
          top = rect.top - 150;
          left = rect.left + rect.width / 2 - 150;
          break;
        case 'bottom':
          top = rect.bottom + 20;
          left = rect.left + rect.width / 2 - 150;
          break;
        case 'left':
          top = rect.top + rect.height / 2 - 75;
          left = rect.left - 320;
          break;
        case 'right':
          top = rect.top + rect.height / 2 - 75;
          left = rect.right + 20;
          break;
      }
      
      setPosition({ top, left });
      
      // Scroll into view
      target.scrollIntoView({ behavior: 'smooth', block: 'center' });
      
      // Add highlight class
      target.classList.add('feature-spotlight-target');
    }
  };
  
  const handleNext = () => {
    // Remove highlight from current
    const current = document.querySelector(steps[currentStep].target);
    current?.classList.remove('feature-spotlight-target');
    
    if (currentStep < steps.length - 1) {
      setCurrentStep(currentStep + 1);
    } else {
      onComplete();
    }
  };
  
  const handlePrev = () => {
    if (currentStep > 0) {
      const current = document.querySelector(steps[currentStep].target);
      current?.classList.remove('feature-spotlight-target');
      setCurrentStep(currentStep - 1);
    }
  };
  
  const handleSkip = () => {
    const current = document.querySelector(steps[currentStep].target);
    current?.classList.remove('feature-spotlight-target');
    onSkip();
  };
  
  return (
    <>
      <Backdrop
        open={true}
        sx={{
          zIndex: 9998,
          backgroundColor: 'rgba(0, 0, 0, 0.7)',
          // Create hole for target element
          clipPath: targetRect ? 
            `polygon(
              0 0,
              100% 0,
              100% 100%,
              0 100%,
              0 ${targetRect.top - 10}px,
              ${targetRect.left - 10}px ${targetRect.top - 10}px,
              ${targetRect.left - 10}px ${targetRect.bottom + 10}px,
              ${targetRect.right + 10}px ${targetRect.bottom + 10}px,
              ${targetRect.right + 10}px ${targetRect.top - 10}px,
              0 ${targetRect.top - 10}px
            )` : 'none'
        }}
        onClick={handleSkip}
      />
      
      <Fade in={true}>
        <Paper
          sx={{
            position: 'fixed',
            top: position.top,
            left: position.left,
            zIndex: 9999,
            p: 3,
            width: 300,
            boxShadow: 6
          }}
        >
          <Box display="flex" justifyContent="space-between" alignItems="start" mb={2}>
            <Typography variant="h6">
              {steps[currentStep].title}
            </Typography>
            <IconButton size="small" onClick={handleSkip}>
              <CloseIcon />
            </IconButton>
          </Box>
          
          <Typography variant="body2" paragraph>
            {steps[currentStep].content}
          </Typography>
          
          {steps[currentStep].action && (
            <Button
              variant="outlined"
              size="small"
              onClick={steps[currentStep].action!.onClick}
              sx={{ mb: 2 }}
            >
              {steps[currentStep].action.label}
            </Button>
          )}
          
          <Box display="flex" justifyContent="space-between" alignItems="center">
            <Typography variant="caption" color="text.secondary">
              Schritt {currentStep + 1} von {steps.length}
            </Typography>
            
            <Box display="flex" gap={1}>
              <IconButton 
                size="small" 
                onClick={handlePrev}
                disabled={currentStep === 0}
              >
                <PrevIcon />
              </IconButton>
              <Button
                variant="contained"
                size="small"
                onClick={handleNext}
                endIcon={currentStep < steps.length - 1 ? <NextIcon /> : <CheckIcon />}
              >
                {currentStep < steps.length - 1 ? 'Weiter' : 'Fertig'}
              </Button>
            </Box>
          </Box>
        </Paper>
      </Fade>
      
      <style>
        {`
          .feature-spotlight-target {
            position: relative;
            z-index: 9997;
            box-shadow: 0 0 0 4px rgba(33, 150, 243, 0.5);
            animation: pulse 2s infinite;
          }
          
          @keyframes pulse {
            0% {
              box-shadow: 0 0 0 4px rgba(33, 150, 243, 0.5);
            }
            50% {
              box-shadow: 0 0 0 8px rgba(33, 150, 243, 0.2);
            }
            100% {
              box-shadow: 0 0 0 4px rgba(33, 150, 243, 0.5);
            }
          }
        `}
      </style>
    </>
  );
};
```

### 4. Help Store

**Datei:** `frontend/src/features/help/stores/helpStore.ts`

```typescript
// CLAUDE: Zustand Store für Help System
// Pfad: frontend/src/features/help/stores/helpStore.ts

import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface HelpPreferences {
  showTooltips: boolean;
  tooltipIntensity: 'subtle' | 'moderate' | 'prominent';
  showOnboarding: boolean;
  dismissedTopics: string[];
  viewedTopics: Record<string, number>; // topic -> view count
  completedTours: string[];
  lastHelpRequest: Date | null;
}

interface HelpStore extends HelpPreferences {
  // Actions
  setTooltipIntensity: (intensity: 'subtle' | 'moderate' | 'prominent') => void;
  dismissTopic: (topic: string) => void;
  markTopicViewed: (topic: string) => void;
  completeTour: (tourId: string) => void;
  getTopicViewCount: (topic: string) => number;
  shouldShowHelp: (topic: string) => boolean;
  resetHelp: () => void;
}

export const useHelpStore = create<HelpStore>()(
  persist(
    (set, get) => ({
      // Initial state
      showTooltips: true,
      tooltipIntensity: 'moderate',
      showOnboarding: true,
      dismissedTopics: [],
      viewedTopics: {},
      completedTours: [],
      lastHelpRequest: null,
      
      // Actions
      setTooltipIntensity: (intensity) => set({ tooltipIntensity: intensity }),
      
      dismissTopic: (topic) => set((state) => ({
        dismissedTopics: [...state.dismissedTopics, topic]
      })),
      
      markTopicViewed: (topic) => set((state) => ({
        viewedTopics: {
          ...state.viewedTopics,
          [topic]: (state.viewedTopics[topic] || 0) + 1
        }
      })),
      
      completeTour: (tourId) => set((state) => ({
        completedTours: [...state.completedTours, tourId]
      })),
      
      getTopicViewCount: (topic) => {
        return get().viewedTopics[topic] || 0;
      },
      
      shouldShowHelp: (topic) => {
        const state = get();
        
        // Never show if globally disabled
        if (!state.showTooltips) return false;
        
        // Never show if dismissed
        if (state.dismissedTopics.includes(topic)) return false;
        
        // Reduce frequency after multiple views
        const viewCount = state.viewedTopics[topic] || 0;
        if (viewCount > 5) return false;
        
        return true;
      },
      
      resetHelp: () => set({
        dismissedTopics: [],
        viewedTopics: {},
        completedTours: [],
        showOnboarding: true
      })
    }),
    {
      name: 'help-preferences'
    }
  )
);
```

## 📋 IMPLEMENTIERUNGS-CHECKLISTE

### Phase 1: Detection (45 Min)
- [ ] StruggleDetectionService implementieren
- [ ] Event-System einrichten
- [ ] Pattern-Erkennung testen

### Phase 2: Help Content (30 Min)
- [ ] Help Content Service
- [ ] Content für alle Features
- [ ] Mehrsprachigkeit vorbereiten

### Phase 3: UI Components (45 Min)
- [ ] ContextualTooltip Component
- [ ] FeatureSpotlight
- [ ] InteractiveTour

### Phase 4: Integration (30 Min)
- [ ] Help Store einbinden
- [ ] Preferences UI
- [ ] Analytics Integration

### Phase 5: Testing (30 Min)
- [ ] Struggle Detection Tests
- [ ] Component Tests
- [ ] E2E Tour Tests

## 🔗 INTEGRATION POINTS

1. **WarmthIndicator** - Tooltip für Score-Erklärung
2. **SmartSuggestions** - Tour für erste Nutzung
3. **ContactTimeline** - Interaktive Demo
4. **BudgetDashboard** - Onboarding für Cost Management

## ⚠️ HÄUFIGE FEHLER VERMEIDEN

1. **Zu aufdringliche Hilfe**
   → Lösung: Adaptive Intensität basierend auf Nutzung

2. **Generische Hilfe-Texte**
   → Lösung: Kontextspezifische, actionable Tipps

3. **Keine Escape-Möglichkeit**
   → Lösung: Immer Skip/Dismiss Option anbieten

---

**Status:** BEREIT FÜR IMPLEMENTIERUNG  
**Geschätzte Zeit:** 180 Minuten  
**Nächstes Dokument:** [→ Feature Adoption Tracking](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/FEATURE_ADOPTION_TRACKING.md)  
**Parent:** [↑ Critical Success Factors](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/CRITICAL_SUCCESS_FACTORS.md)

**Proaktive Hilfe = Glückliche User! 🆘✨**