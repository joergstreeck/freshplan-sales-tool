# 💡 Kontextsensitive In-App-Hilfe

**Erstellt:** 01.08.2025  
**Status:** 🆕 Konzept  
**Priorität:** HIGH - Kritisch für Feature-Adoption  

## 🧭 Navigation

**← Zurück:** [Cost Management External Services](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/COST_MANAGEMENT_EXTERNAL_SERVICES.md)  
**→ Nächstes:** [Feature Adoption Tracking](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/FEATURE_ADOPTION_TRACKING.md)  
**↗ Verbunden:** [Relationship Intelligence](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/RELATIONSHIP_INTELLIGENCE.md)  

## 🎯 Problem: Magische Features ohne Erklärung

**Die Herausforderung:**
- "Was bedeutet Warmth Score 73?" 🤔
- "Warum schlägt das System DAS vor?"
- Support-Tickets für selbsterklärende Features
- Niedrige Adoption durch Unsicherheit

## 💡 Lösung: Intelligente, kontextuelle Hilfe

### 1. Smart Tooltips mit Erklärungen

```typescript
export const WarmthScoreTooltip: React.FC<{contact: Contact}> = ({contact}) => {
  const explanation = useWarmthExplanation(contact);
  
  return (
    <Tooltip
      title={
        <Box>
          <Typography variant="subtitle2">
            Beziehungswärme: {contact.warmthScore}/100
          </Typography>
          <Divider sx={{my: 1}} />
          
          {/* Faktoren-Breakdown */}
          <Typography variant="body2">
            Berechnet aus:
          </Typography>
          <List dense>
            {explanation.factors.map(factor => (
              <ListItem key={factor.name}>
                <ListItemIcon>
                  {factor.positive ? <AddIcon color="success" /> : <RemoveIcon color="error" />}
                </ListItemIcon>
                <ListItemText 
                  primary={factor.description}
                  secondary={`${factor.impact > 0 ? '+' : ''}${factor.impact} Punkte`}
                />
              </ListItem>
            ))}
          </List>
          
          {/* Confidence Indicator */}
          <Alert severity={explanation.confidence > 70 ? "success" : "info"} sx={{mt: 1}}>
            Zuverlässigkeit: {explanation.confidence}%
            {explanation.confidence < 70 && (
              <Typography variant="caption" display="block">
                Mehr Interaktionen erhöhen die Genauigkeit
              </Typography>
            )}
          </Alert>
          
          {/* Action Link */}
          <Button size="small" onClick={() => openDetailedHelp('warmth-score')}>
            Mehr erfahren
          </Button>
        </Box>
      }
      placement="right"
      arrow
      interactive
    >
      <HelpOutlineIcon fontSize="small" sx={{cursor: 'pointer', ml: 0.5}} />
    </Tooltip>
  );
};
```

### 2. Progressive Disclosure

```typescript
// Stufe 1: Inline Hint
export const InlineHint: React.FC<{feature: string}> = ({feature}) => {
  const hint = useFeatureHint(feature);
  
  if (!hint.shouldShow) return null;
  
  return (
    <Fade in timeout={1000}>
      <Alert 
        severity="info" 
        icon={<LightbulbIcon />}
        action={
          <IconButton size="small" onClick={() => dismissHint(feature)}>
            <CloseIcon fontSize="small" />
          </IconButton>
        }
      >
        {hint.message}
        {hint.hasMore && (
          <Link onClick={() => expandHelp(feature)} sx={{ml: 1}}>
            Details
          </Link>
        )}
      </Alert>
    </Fade>
  );
};

// Stufe 2: Expandierte Hilfe
export const ExpandedHelp: React.FC<{feature: string}> = ({feature}) => {
  const help = useExpandedHelp(feature);
  
  return (
    <Drawer anchor="right" open={help.isOpen} onClose={help.close}>
      <Box sx={{width: 400, p: 3}}>
        <Typography variant="h6" gutterBottom>
          {help.title}
        </Typography>
        
        {/* Video Tutorial */}
        {help.videoUrl && (
          <Card sx={{mb: 2}}>
            <CardMedia
              component="video"
              src={help.videoUrl}
              controls
              height="200"
            />
          </Card>
        )}
        
        {/* Step-by-Step Guide */}
        <Stepper activeStep={help.currentStep} orientation="vertical">
          {help.steps.map((step, index) => (
            <Step key={index}>
              <StepLabel>{step.title}</StepLabel>
              <StepContent>
                <Typography>{step.description}</Typography>
                {step.image && (
                  <img src={step.image} alt={step.title} style={{maxWidth: '100%'}} />
                )}
              </StepContent>
            </Step>
          ))}
        </Stepper>
        
        {/* FAQ Section */}
        <Accordion>
          <AccordionSummary expandIcon={<ExpandMoreIcon />}>
            <Typography>Häufige Fragen</Typography>
          </AccordionSummary>
          <AccordionDetails>
            {help.faqs.map(faq => (
              <Box key={faq.id} mb={2}>
                <Typography variant="subtitle2">{faq.question}</Typography>
                <Typography variant="body2" color="textSecondary">
                  {faq.answer}
                </Typography>
              </Box>
            ))}
          </AccordionDetails>
        </Accordion>
      </Box>
    </Drawer>
  );
};
```

### 3. Interaktive Feature Tours

```typescript
export const FeatureTour: React.FC = () => {
  const tour = useFeatureTour();
  
  const steps: TourStep[] = [
    {
      target: '.warmth-indicator',
      content: (
        <Box>
          <Typography variant="h6">Beziehungswärme</Typography>
          <Typography variant="body2">
            Diese Anzeige zeigt, wie "warm" die Kundenbeziehung ist.
            Rot = Handlungsbedarf, Grün = Alles gut!
          </Typography>
          <WarmthDemo />
        </Box>
      ),
      placement: 'bottom',
      spotlightClicks: true
    },
    {
      target: '.smart-suggestions',
      content: (
        <Box>
          <Typography variant="h6">Intelligente Vorschläge</Typography>
          <Typography variant="body2">
            Basierend auf der Beziehungshistorie empfehlen wir die nächsten Schritte.
          </Typography>
          <Chip label="Beispiel: Geburtstagsglückwunsch senden" />
        </Box>
      )
    },
    {
      target: '.contact-timeline',
      content: 'Hier sehen Sie alle Interaktionen chronologisch sortiert.'
    }
  ];
  
  return (
    <Joyride
      steps={steps}
      run={tour.isActive}
      continuous
      showProgress
      showSkipButton
      styles={{
        options: {
          primaryColor: '#1976d2',
          zIndex: 10000
        }
      }}
      locale={{
        back: 'Zurück',
        close: 'Schließen',
        last: 'Fertig',
        next: 'Weiter',
        skip: 'Überspringen'
      }}
    />
  );
};
```

### 4. Contextual Help Triggers

```java
@Service
public class HelpTriggerService {
    
    @EventListener
    public void onUserAction(UserActionEvent event) {
        HelpContext context = analyzeContext(event);
        
        // Erste Nutzung eines Features
        if (isFirstTimeUsage(event.getUserId(), event.getFeature())) {
            triggerHelp(HelpType.FIRST_TIME_TOUR, event.getFeature());
        }
        
        // Fehlerhafte Nutzung
        if (detectMisuse(event)) {
            triggerHelp(HelpType.CORRECTION_HINT, event.getFeature());
        }
        
        // Erweiterte Features verfügbar
        if (hasAdvancedFeatures(event) && !hasUsedAdvanced(event.getUserId())) {
            triggerHelp(HelpType.ADVANCED_FEATURES, event.getFeature());
        }
        
        // NEU: Proaktive Hilfe bei Frustration
        if (detectUserStruggle(event)) {
            triggerProactiveHelp(event);
        }
    }
    
    private boolean detectUserStruggle(UserActionEvent event) {
        UserBehaviorPattern pattern = behaviorAnalyzer.analyze(
            event.getUserId(), 
            event.getSessionId(),
            Duration.ofMinutes(5)
        );
        
        return pattern.hasAnyOf(
            REPEATED_FAILED_ATTEMPTS,    // 3+ mal gleiche Aktion ohne Erfolg
            RAPID_NAVIGATION_CHANGES,     // Hektisches Hin- und Her-Klicken
            LONG_IDLE_AFTER_START,       // Start einer Aktion, dann lange Pause
            ABANDONED_WORKFLOW_PATTERN   // Mehrfach begonnene, nie beendete Workflows
        );
    }
    
    private void triggerProactiveHelp(UserActionEvent event) {
        ProactiveHelpContext context = ProactiveHelpContext.builder()
            .userId(event.getUserId())
            .feature(event.getFeature())
            .struggleType(identifyStruggleType(event))
            .attemptCount(getAttemptCount(event))
            .timeSpent(getTimeSpentStruggling(event))
            .build();
            
        helpEventPublisher.publish(new ProactiveHelpEvent(context));
    }
}
```

## 🤝 Proaktive Hilfe bei erkannten Problemen (NEU)

### Intelligente Frustrations-Erkennung

```typescript
export const ProactiveHelpSystem: React.FC = () => {
  const struggleDetection = useStruggleDetection();
  const [showHelp, setShowHelp] = useState(false);
  const [helpAccepted, setHelpAccepted] = useState<boolean | null>(null);
  
  useEffect(() => {
    if (struggleDetection.detected && !helpAccepted) {
      // Warte kurz, um nicht zu aufdringlich zu sein
      const timer = setTimeout(() => setShowHelp(true), 2000);
      return () => clearTimeout(timer);
    }
  }, [struggleDetection.detected]);
  
  if (!showHelp) return null;
  
  return (
    <Slide direction="up" in={showHelp}>
      <Paper
        sx={{
          position: 'fixed',
          bottom: 80,
          right: 20,
          p: 2,
          maxWidth: 350,
          boxShadow: 3,
          borderLeft: 4,
          borderColor: 'primary.main'
        }}
      >
        <Box display="flex" alignItems="flex-start">
          <Avatar sx={{bgcolor: 'primary.main', mr: 2}}>
            <AssistantIcon />
          </Avatar>
          
          <Box flex={1}>
            <Typography variant="subtitle1" gutterBottom>
              Kann ich Ihnen helfen? 🤔
            </Typography>
            
            <Typography variant="body2" color="textSecondary" gutterBottom>
              {getStruggleMessage(struggleDetection.type)}
            </Typography>
            
            {/* Spezifische Hilfe-Optionen */}
            <Stack spacing={1} sx={{mt: 2}}>
              {struggleDetection.suggestions.map((suggestion, idx) => (
                <Button
                  key={idx}
                  size="small"
                  variant={idx === 0 ? "contained" : "outlined"}
                  startIcon={suggestion.icon}
                  onClick={() => {
                    suggestion.action();
                    trackHelpAccepted(suggestion.type);
                    setHelpAccepted(true);
                  }}
                  fullWidth
                >
                  {suggestion.label}
                </Button>
              ))}
              
              <Button
                size="small"
                variant="text"
                onClick={() => {
                  setShowHelp(false);
                  setHelpAccepted(false);
                  trackHelpDismissed(struggleDetection.type);
                }}
              >
                Nein danke, ich schaffe das
              </Button>
            </Stack>
          </Box>
          
          <IconButton 
            size="small" 
            onClick={() => setShowHelp(false)}
            sx={{ml: 1}}
          >
            <CloseIcon fontSize="small" />
          </IconButton>
        </Box>
      </Paper>
    </Slide>
  );
};

// Struggle-spezifische Nachrichten
const getStruggleMessage = (struggleType: StruggleType): string => {
  const messages = {
    REPEATED_FAILED_ATTEMPTS: 
      "Ich sehe, dass Sie mehrmals versucht haben, diese Aktion durchzuführen. " +
      "Lassen Sie mich Ihnen zeigen, wie es funktioniert.",
    
    RAPID_NAVIGATION_CHANGES:
      "Sie suchen etwas Bestimmtes? " +
      "Ich kann Ihnen helfen, schneller ans Ziel zu kommen.",
    
    LONG_IDLE_AFTER_START:
      "Nicht sicher, wie es weitergeht? " +
      "Hier ist eine Schritt-für-Schritt Anleitung.",
    
    ABANDONED_WORKFLOW_PATTERN:
      "Diesen Prozess haben Sie schon öfter begonnen. " +
      "Soll ich Ihnen die Abkürzung zeigen?",
      
    COMPLEX_FORM_STRUGGLE:
      "Dieses Formular hat viele Felder. " +
      "Möchten Sie nur die wichtigsten ausfüllen?"
  };
  
  return messages[struggleType] || 
    "Scheint, als könnten Sie etwas Unterstützung gebrauchen.";
};

// Struggle Detection Hook
export const useStruggleDetection = () => {
  const [struggles, setStruggles] = useState<UserStruggle[]>([]);
  const { trackEvent } = useAnalytics();
  
  useEffect(() => {
    // Track User-Aktionen
    const unsubscribe = eventBus.subscribe('user.action', (event) => {
      analyzeUserBehavior(event);
    });
    
    return unsubscribe;
  }, []);
  
  const analyzeUserBehavior = (event: UserActionEvent) => {
    const recentActions = getRecentActions(5); // Letzte 5 Minuten
    
    // Pattern Detection
    if (detectRepeatedFailure(recentActions)) {
      triggerHelp('REPEATED_FAILED_ATTEMPTS', {
        action: event.action,
        attempts: countAttempts(recentActions, event.action),
        suggestions: [
          {
            label: "Video-Tutorial ansehen",
            icon: <PlayCircleIcon />,
            action: () => showVideoTutorial(event.feature)
          },
          {
            label: "Schritt-für-Schritt Anleitung",
            icon: <ListAltIcon />,
            action: () => showStepByStep(event.feature)
          },
          {
            label: "Support kontaktieren",
            icon: <ContactSupportIcon />,
            action: () => openSupportChat()
          }
        ]
      });
    }
    
    if (detectNavigationConfusion(recentActions)) {
      triggerHelp('RAPID_NAVIGATION_CHANGES', {
        visitedPages: getUniquePages(recentActions),
        suggestions: [
          {
            label: "Zur Suche",
            icon: <SearchIcon />,
            action: () => focusGlobalSearch()
          },
          {
            label: "Sitemap anzeigen",
            icon: <AccountTreeIcon />,
            action: () => showSitemap()
          },
          {
            label: "Guided Tour starten",
            icon: <ExploreIcon />,
            action: () => startGuidedTour()
          }
        ]
      });
    }
  };
};
```

### Adaptive Hilfe-Intensität

```typescript
// Hilfe passt sich an User-Reaktion an
export const AdaptiveHelpIntensity = {
  // Initial: Dezent
  firstOffer: {
    delay: 3000,
    position: 'bottom-right',
    size: 'small',
    dismissable: true,
    persistent: false
  },
  
  // Wenn ignoriert: Etwas prominenter
  secondOffer: {
    delay: 5000,
    position: 'center',
    size: 'medium',
    animation: 'pulse',
    dismissable: true,
    persistent: true
  },
  
  // Bei fortgesetztem Struggle: Direkter
  thirdOffer: {
    delay: 1000,
    position: 'center',
    size: 'large',
    modal: true,
    dismissable: true,
    offerLiveHelp: true
  },
  
  // Nach Ablehnung: Respektvolle Distanz
  afterDismissal: {
    cooldownPeriod: 3600000, // 1 Stunde
    reducedThreshold: true,
    subtleIndicatorOnly: true
  }
};
```

### 5. Smart Help Content Management

```typescript
interface HelpContent {
  id: string;
  feature: string;
  triggers: HelpTrigger[];
  content: {
    short: string;
    medium: string;
    detailed: string;
    video?: string;
    interactive?: boolean;
  };
  targeting: {
    userLevel: 'beginner' | 'intermediate' | 'expert';
    role: string[];
    previousInteractions: string[];
  };
  analytics: {
    views: number;
    helpful: number;
    notHelpful: number;
    avgTimeSpent: number;
  };
}

// Dynamisches Help Loading
export const useContextualHelp = (feature: string) => {
  const user = useCurrentUser();
  const context = useFeatureContext();
  
  const help = useQuery({
    queryKey: ['help', feature, user.level, context],
    queryFn: async () => {
      const response = await api.getHelpContent({
        feature,
        userLevel: user.level,
        context: context,
        preferredFormat: user.preferences.helpFormat
      });
      
      // Track Help Request
      analytics.track('help_requested', {
        feature,
        trigger: context.trigger,
        userLevel: user.level
      });
      
      return response;
    }
  });
  
  return help;
};
```

### 6. Feedback Loop

```typescript
export const HelpFeedback: React.FC<{helpId: string}> = ({helpId}) => {
  const [feedback, setFeedback] = useState<'helpful' | 'not-helpful' | null>(null);
  
  return (
    <Box sx={{mt: 2, p: 2, bgcolor: 'grey.100', borderRadius: 1}}>
      <Typography variant="body2" gutterBottom>
        War diese Erklärung hilfreich?
      </Typography>
      
      <ButtonGroup size="small">
        <Button
          variant={feedback === 'helpful' ? 'contained' : 'outlined'}
          onClick={() => {
            setFeedback('helpful');
            api.submitHelpFeedback(helpId, 'helpful');
          }}
          startIcon={<ThumbUpIcon />}
        >
          Ja
        </Button>
        <Button
          variant={feedback === 'not-helpful' ? 'contained' : 'outlined'}
          onClick={() => {
            setFeedback('not-helpful');
            setShowDetailedFeedback(true);
          }}
          startIcon={<ThumbDownIcon />}
        >
          Nein
        </Button>
      </ButtonGroup>
      
      {feedback === 'not-helpful' && (
        <TextField
          fullWidth
          multiline
          rows={2}
          placeholder="Was war unklar?"
          sx={{mt: 1}}
          onBlur={(e) => {
            api.submitHelpFeedback(helpId, 'not-helpful', e.target.value);
          }}
        />
      )}
    </Box>
  );
};
```

### 7. Help Analytics Dashboard

```typescript
export const HelpAnalyticsDashboard: React.FC = () => {
  const analytics = useHelpAnalytics();
  
  return (
    <Grid container spacing={3}>
      {/* Most Requested Help Topics */}
      <Grid item xs={12} md={6}>
        <Card>
          <CardContent>
            <Typography variant="h6">Top Hilfe-Themen</Typography>
            <List>
              {analytics.topTopics.map(topic => (
                <ListItem key={topic.id}>
                  <ListItemText
                    primary={topic.title}
                    secondary={`${topic.requests} Anfragen`}
                  />
                  <Chip 
                    label={`${topic.helpfulRate}% hilfreich`}
                    color={topic.helpfulRate > 70 ? 'success' : 'warning'}
                  />
                </ListItem>
              ))}
            </List>
          </CardContent>
        </Card>
      </Grid>
      
      {/* Feature Adoption Correlation */}
      <Grid item xs={12} md={6}>
        <Card>
          <CardContent>
            <Typography variant="h6">Hilfe → Adoption</Typography>
            <Typography variant="body2" color="textSecondary" gutterBottom>
              Features mit Hilfe vs. ohne Hilfe
            </Typography>
            <AdoptionChart 
              withHelp={analytics.adoptionWithHelp}
              withoutHelp={analytics.adoptionWithoutHelp}
            />
          </CardContent>
        </Card>
      </Grid>
    </Grid>
  );
};
```

## 📱 Mobile-optimierte Hilfe

```typescript
// Swipe-Up Help Sheet
export const MobileHelpSheet: React.FC = () => {
  const [open, setOpen] = useState(false);
  
  return (
    <SwipeableDrawer
      anchor="bottom"
      open={open}
      onClose={() => setOpen(false)}
      onOpen={() => setOpen(true)}
      swipeAreaWidth={56}
      disableSwipeToOpen={false}
      ModalProps={{ keepMounted: true }}
    >
      <Box sx={{height: 20, backgroundColor: 'primary.main'}}>
        <Puller />
      </Box>
      <Box sx={{p: 2}}>
        <Typography variant="h6">Schnellhilfe</Typography>
        <QuickHelpContent />
      </Box>
    </SwipeableDrawer>
  );
};
```

## 🎯 Erfolgsmetriken

```typescript
interface HelpSystemMetrics {
  reduction: {
    supportTickets: number; // % Reduktion
    onboardingTime: number; // Tage
    featureDiscoveryTime: number; // Minuten
  };
  
  engagement: {
    helpRequestsPerUser: number;
    helpCompletionRate: number;
    feedbackRate: number;
    helpfulnessScore: number;
  };
  
  impact: {
    featureAdoptionRate: number;
    userSatisfaction: number;
    taskCompletionRate: number;
  };
}
```

## 🚀 Implementierungs-Checkliste

- [ ] Tooltip Component Library
- [ ] Help Content CMS
- [ ] Feature Tour System
- [ ] Feedback Collection
- [ ] Analytics Integration
- [ ] Mobile Help UI
- [ ] A/B Testing für Help-Varianten

## 🔗 Verwandte Dokumente

- [Relationship Intelligence](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/RELATIONSHIP_INTELLIGENCE.md) - Hauptziel der Hilfe
- [Smart Suggestions](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/SMART_SUGGESTIONS.md) - Erklärt Vorschläge
- [Feature Adoption Tracking](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/FEATURE_ADOPTION_TRACKING.md) - Misst Erfolg

---

**Nächster Schritt:** [→ Feature Adoption Tracking](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/FEATURE_ADOPTION_TRACKING.md)