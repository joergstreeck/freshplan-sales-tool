# 🚀 FC-030 ONE-TAP ACTIONS - TECHNISCHES KONZEPT

**Feature Code:** FC-030  
**Feature-Typ:** 🎨 FRONTEND + Backend  
**Geschätzter Aufwand:** 3 Tage  
**Priorität:** HIGH - Produktivitäts-Booster  
**Abhängigkeiten:** -  
**Claude-optimiert:** 15-Min Context Chunks ⚡

---

## 📋 EXECUTIVE SUMMARY (2 Min Lesezeit)

### 🎯 Problem
Verkäufer verlieren täglich 2-3 Stunden durch wiederkehrende Klick-Orgien für Standard-Aktionen.

### 💡 Lösung
Smart Context Actions + One-Tap Shortcuts für 90% der Daily Tasks.

### 📈 Business Impact
- **Zeitersparnis:** 2-3h/Tag → 15-20 Min für Standard-Tasks
- **User Experience:** Intuitive Schnellaktionen
- **Adoption:** Höhere Tool-Nutzung durch Effizienz

---

## 🧭 NAVIGATION & VERWEISE

### 📋 Zurück zum Überblick:
- **[📊 Master Plan V5](/docs/CRM_COMPLETE_MASTER_PLAN_V5.md)** - Vollständige Feature-Roadmap
- **[🗺️ Feature Overview](/docs/features/MASTER/FEATURE_OVERVIEW.md)** - Alle 40 Features im Überblick

### 🔗 Dependencies (Required):
- **Keine direkten Dependencies** - Eigenständiges Feature

### ⚡ Direkt integriert in:
- **[📊 M3 Sales Cockpit](/docs/features/ACTIVE/05_ui_foundation/M3_TECH_CONCEPT.md)** - Context Actions in Cards
- **[👥 M5 Customer Refactor](/docs/features/PLANNED/12_customer_refactor_m5/M5_TECH_CONCEPT.md)** - Quick Customer Actions
- **[📊 M4 Opportunity Pipeline](/docs/features/ACTIVE/02_opportunity_pipeline/M4_TECH_CONCEPT.md)** - Deal Quick Actions

### 🚀 Ermöglicht folgende Features:
- **[⚡ FC-020 Quick Wins](/docs/features/PLANNED/20_quick_wins/FC-020_TECH_CONCEPT.md)** - Command Palette Integration
- **[📱 FC-018 Mobile PWA](/docs/features/PLANNED/18_mobile_pwa/FC-018_TECH_CONCEPT.md)** - Mobile One-Tap Actions
- **[🎯 FC-031 Smart Templates](/docs/features/PLANNED/31_smart_templates/FC-031_TECH_CONCEPT.md)** - Template Quick Actions

### 🎨 UI Integration:
- **[🧭 M1 Navigation](/docs/features/ACTIVE/05_ui_foundation/M1_TECH_CONCEPT.md)** - Global Quick Actions
- **[⚙️ M7 Settings](/docs/features/ACTIVE/05_ui_foundation/M7_SETTINGS_TECH_CONCEPT.md)** - Quick Action Konfiguration

### 🔧 Technische Details:
- **[IMPLEMENTATION_BACKEND.md](./IMPLEMENTATION_BACKEND.md)** *(geplant)* - Backend-Services
- **[IMPLEMENTATION_FRONTEND.md](./IMPLEMENTATION_FRONTEND.md)** *(geplant)* - React Components
- **[DECISION_LOG.md](./DECISION_LOG.md)** *(geplant)* - Architektur-Entscheidungen

---

## 🏗️ ARCHITEKTUR OVERVIEW (5 Min)

### 🎨 Frontend-Architektur
```typescript
components/
├── QuickActions/
│   ├── ContextActionBar.tsx      # Kontextuelle Actions
│   ├── FloatingActionButton.tsx  # FAB für häufige Actions
│   ├── QuickActionMenu.tsx       # Dropdown für mehr Actions
│   └── types/
│       └── QuickActionTypes.ts   # Action Definitions
└── hooks/
    ├── useQuickActions.ts        # Action State Management
    ├── useContextActions.ts      # Context-basierte Actions
    └── useActionShortcuts.ts     # Keyboard Shortcuts
```

### 🔧 Backend-Services
```java
com.freshplan.quickactions/
├── service/
│   ├── QuickActionService.java      # Action Orchestration
│   ├── ActionTemplateService.java   # Vordefinierte Actions
│   └── ActionHistoryService.java    # Usage Analytics
├── dto/
│   ├── QuickActionRequest.java      # Action Parameters
│   ├── QuickActionResponse.java     # Action Results
│   └── ActionContextDto.java       # Context Information
└── entity/
    ├── QuickActionTemplate.java    # Konfigurierbare Actions
    └── ActionHistory.java          # Usage Tracking
```

---

## 🎯 PHASE 1: SMART CONTEXT ACTIONS (1 Tag)

### 🔥 Context-Sensitive Action Bar

#### Customer Card Context Actions
```typescript
// CustomerContextActions.tsx
interface CustomerContextActions {
  customer: Customer;
  onActionComplete: (action: QuickAction) => void;
}

const CustomerContextActions: React.FC<CustomerContextActions> = ({
  customer,
  onActionComplete
}) => {
  const { executeAction, isLoading } = useQuickActions();
  
  const contextActions = useMemo(() => [
    {
      id: 'call-customer',
      icon: <PhoneIcon />,
      label: 'Anrufen',
      shortcut: 'C',
      action: () => executeAction('call', { 
        phone: customer.phone,
        customerId: customer.id 
      })
    },
    {
      id: 'email-customer', 
      icon: <EmailIcon />,
      label: 'E-Mail',
      shortcut: 'E',
      action: () => executeAction('email', {
        email: customer.email,
        customerId: customer.id,
        template: 'follow-up'
      })
    },
    {
      id: 'create-opportunity',
      icon: <AddBusinessIcon />,
      label: 'Opportunity',
      shortcut: 'O',
      action: () => executeAction('create-opportunity', {
        customerId: customer.id,
        template: 'standard'
      })
    },
    {
      id: 'schedule-meeting',
      icon: <EventIcon />,
      label: 'Termin',
      shortcut: 'T',
      action: () => executeAction('schedule-meeting', {
        customerId: customer.id,
        duration: 60
      })
    }
  ], [customer, executeAction]);

  return (
    <Box sx={{ display: 'flex', gap: 1, p: 1 }}>
      {contextActions.map(action => (
        <Tooltip key={action.id} title={`${action.label} (${action.shortcut})`}>
          <IconButton
            size="small"
            onClick={action.action}
            disabled={isLoading}
            sx={{
              bgcolor: 'primary.main',
              color: 'white',
              '&:hover': { bgcolor: 'primary.dark' }
            }}
          >
            {action.icon}
          </IconButton>
        </Tooltip>
      ))}
    </Box>
  );
};
```

#### Opportunity Context Actions
```typescript
// OpportunityContextActions.tsx
const OpportunityContextActions: React.FC<{
  opportunity: Opportunity;
}> = ({ opportunity }) => {
  const getStageActions = (stage: OpportunityStage) => {
    switch (stage) {
      case 'qualified':
        return [
          {
            id: 'credit-check',
            icon: <SecurityIcon />,
            label: 'Bonitätsprüfung',
            shortcut: 'B',
            priority: 'high'
          },
          {
            id: 'create-proposal',
            icon: <DescriptionIcon />,
            label: 'Angebot erstellen',
            shortcut: 'A',
            priority: 'medium'
          }
        ];
      case 'proposal':
        return [
          {
            id: 'send-proposal',
            icon: <SendIcon />,
            label: 'Angebot senden',
            shortcut: 'S',
            priority: 'high'
          },
          {
            id: 'schedule-followup',
            icon: <ScheduleIcon />,
            label: 'Follow-up planen',
            shortcut: 'F',
            priority: 'medium'
          }
        ];
      case 'negotiation':
        return [
          {
            id: 'update-proposal',
            icon: <EditIcon />,
            label: 'Angebot anpassen',
            shortcut: 'U',
            priority: 'high'
          },
          {
            id: 'escalate-discount',
            icon: <TrendingUpIcon />,
            label: 'Rabatt eskalieren',
            shortcut: 'R',
            priority: 'medium'
          }
        ];
      default:
        return [];
    }
  };

  const stageActions = getStageActions(opportunity.stage);
  
  return (
    <ActionBar actions={stageActions} context={opportunity} />
  );
};
```

---

## 🎯 PHASE 2: FLOATING ACTION BUTTON (FAB) (1 Tag)

### 🎨 Smart FAB mit Adaptive Actions

#### Intelligente FAB Implementation
```typescript
// FloatingActionButton.tsx
const SmartFAB: React.FC = () => {
  const location = useLocation();
  const { currentCustomer, currentOpportunity } = useContext(AppContext);
  const { frequentActions } = useActionAnalytics();
  
  const getContextualActions = () => {
    const route = location.pathname;
    
    if (route.includes('/customers') && currentCustomer) {
      return [
        {
          icon: <PhoneIcon />,
          label: 'Anrufen',
          action: () => callCustomer(currentCustomer.id),
          frequency: frequentActions.call || 0
        },
        {
          icon: <AddBusinessIcon />,
          label: 'Neue Opportunity',
          action: () => createOpportunity(currentCustomer.id),
          frequency: frequentActions.opportunity || 0
        }
      ];
    }
    
    if (route.includes('/opportunities') && currentOpportunity) {
      return [
        {
          icon: <DescriptionIcon />,
          label: 'Angebot erstellen',
          action: () => createProposal(currentOpportunity.id),
          frequency: frequentActions.proposal || 0
        },
        {
          icon: <PersonAddIcon />,
          label: 'Kontakt hinzufügen',
          action: () => addContact(currentOpportunity.customerId),
          frequency: frequentActions.contact || 0
        }
      ];
    }
    
    // Default actions für Dashboard
    return [
      {
        icon: <PersonAddIcon />,
        label: 'Neuer Kunde',
        action: () => createCustomer(),
        frequency: frequentActions.customer || 0
      },
      {
        icon: <BusinessIcon />,
        label: 'Neue Opportunity',
        action: () => createOpportunity(),
        frequency: frequentActions.opportunity || 0
      }
    ];
  };

  const [isOpen, setIsOpen] = useState(false);
  const contextualActions = getContextualActions()
    .sort((a, b) => b.frequency - a.frequency) // Häufigste zuerst
    .slice(0, 4); // Max 4 Actions

  return (
    <Box sx={{ position: 'fixed', bottom: 24, right: 24, zIndex: 1000 }}>
      <Zoom in={true}>
        <Fab
          color="primary"
          size="large"
          onClick={() => setIsOpen(!isOpen)}
          sx={{
            boxShadow: 4,
            '&:hover': { transform: 'scale(1.1)' }
          }}
        >
          <AddIcon sx={{ transform: isOpen ? 'rotate(45deg)' : 'rotate(0deg)' }} />
        </Fab>
      </Zoom>
      
      <Collapse in={isOpen}>
        <Box sx={{ mt: 2, display: 'flex', flexDirection: 'column', gap: 1 }}>
          {contextualActions.map((action, index) => (
            <Zoom 
              key={action.label}
              in={isOpen}
              style={{ transitionDelay: `${index * 100}ms` }}
            >
              <Fab
                size="medium"
                color="secondary"
                onClick={() => {
                  action.action();
                  setIsOpen(false);
                }}
                sx={{ 
                  boxShadow: 2,
                  bgcolor: 'success.main',
                  '&:hover': { bgcolor: 'success.dark' }
                }}
              >
                {action.icon}
              </Fab>
            </Zoom>
          ))}
        </Box>
      </Collapse>
    </Box>
  );
};
```

### 📊 Usage Analytics für Adaptive Actions
```java
// ActionAnalyticsService.java
@ApplicationScoped
public class ActionAnalyticsService {
    
    @Inject
    ActionHistoryRepository historyRepository;
    
    public ActionFrequencyDto getFrequentActions(String userId, int days) {
        var endDate = LocalDate.now();
        var startDate = endDate.minusDays(days);
        
        var actions = historyRepository.findByUserAndDateRange(
            userId, startDate, endDate
        );
        
        var frequency = actions.stream()
            .collect(groupingBy(
                ActionHistory::getActionType,
                counting()
            ));
        
        return ActionFrequencyDto.builder()
            .call(frequency.getOrDefault("call", 0L))
            .email(frequency.getOrDefault("email", 0L))
            .opportunity(frequency.getOrDefault("opportunity", 0L))
            .proposal(frequency.getOrDefault("proposal", 0L))
            .customer(frequency.getOrDefault("customer", 0L))
            .contact(frequency.getOrDefault("contact", 0L))
            .build();
    }
    
    @Transactional
    public void trackAction(String userId, String actionType, 
                           String context, Map<String, Object> metadata) {
        var history = ActionHistory.builder()
            .userId(userId)
            .actionType(actionType)
            .context(context)
            .metadata(metadata)
            .timestamp(LocalDateTime.now())
            .build();
            
        historyRepository.persist(history);
    }
}
```

---

## 🎯 PHASE 3: KEYBOARD SHORTCUTS & QUICK MENU (1 Tag)

### ⌨️ Global Keyboard Shortcuts
```typescript
// useActionShortcuts.ts
const useActionShortcuts = () => {
  const { executeAction } = useQuickActions();
  const { currentCustomer, currentOpportunity } = useContext(AppContext);
  
  useEffect(() => {
    const handleKeyDown = (event: KeyboardEvent) => {
      // Ctrl/Cmd + Shift + Key für Global Actions
      if ((event.ctrlKey || event.metaKey) && event.shiftKey) {
        switch (event.key.toLowerCase()) {
          case 'c':
            event.preventDefault();
            if (currentCustomer) {
              executeAction('call', { customerId: currentCustomer.id });
            }
            break;
            
          case 'e':
            event.preventDefault();
            if (currentCustomer) {
              executeAction('email', { customerId: currentCustomer.id });
            }
            break;
            
          case 'o':
            event.preventDefault();
            executeAction('create-opportunity', {
              customerId: currentCustomer?.id
            });
            break;
            
          case 'n':
            event.preventDefault();
            executeAction('create-customer');
            break;
            
          case 'p':
            event.preventDefault();
            if (currentOpportunity) {
              executeAction('create-proposal', {
                opportunityId: currentOpportunity.id
              });
            }
            break;
            
          case '/':
            event.preventDefault();
            executeAction('open-command-palette');
            break;
        }
      }
    };
    
    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [currentCustomer, currentOpportunity, executeAction]);
};
```

### 🎯 Quick Action Menu (Right-Click Context)
```typescript
// QuickActionMenu.tsx
const QuickActionMenu: React.FC<{
  context: 'customer' | 'opportunity' | 'global';
  contextId?: string;
  anchorEl: HTMLElement | null;
  onClose: () => void;
}> = ({ context, contextId, anchorEl, onClose }) => {
  const { executeAction } = useQuickActions();
  
  const getMenuItems = () => {
    switch (context) {
      case 'customer':
        return [
          {
            icon: <PhoneIcon />,
            text: 'Anrufen',
            shortcut: 'Ctrl+Shift+C',
            action: () => executeAction('call', { customerId: contextId })
          },
          {
            icon: <EmailIcon />,
            text: 'E-Mail senden',
            shortcut: 'Ctrl+Shift+E',
            action: () => executeAction('email', { customerId: contextId })
          },
          { divider: true },
          {
            icon: <AddBusinessIcon />,
            text: 'Neue Opportunity',
            shortcut: 'Ctrl+Shift+O',
            action: () => executeAction('create-opportunity', { customerId: contextId })
          },
          {
            icon: <PersonAddIcon />,
            text: 'Kontakt hinzufügen',
            action: () => executeAction('add-contact', { customerId: contextId })
          },
          { divider: true },
          {
            icon: <CalculateIcon />,
            text: 'Kalkulation erstellen',
            action: () => executeAction('create-calculation', { customerId: contextId })
          },
          {
            icon: <EventIcon />,
            text: 'Termin planen',
            shortcut: 'Ctrl+Shift+T',
            action: () => executeAction('schedule-meeting', { customerId: contextId })
          }
        ];
        
      case 'opportunity':
        return [
          {
            icon: <DescriptionIcon />,
            text: 'Angebot erstellen',
            shortcut: 'Ctrl+Shift+P',
            action: () => executeAction('create-proposal', { opportunityId: contextId })
          },
          {
            icon: <SecurityIcon />,
            text: 'Bonitätsprüfung',
            action: () => executeAction('credit-check', { opportunityId: contextId })
          },
          { divider: true },
          {
            icon: <SendIcon />,
            text: 'Angebot senden',
            action: () => executeAction('send-proposal', { opportunityId: contextId })
          },
          {
            icon: <ScheduleIcon />,
            text: 'Follow-up planen',
            action: () => executeAction('schedule-followup', { opportunityId: contextId })
          }
        ];
        
      default:
        return [
          {
            icon: <PersonAddIcon />,
            text: 'Neuer Kunde',
            shortcut: 'Ctrl+Shift+N',
            action: () => executeAction('create-customer')
          },
          {
            icon: <BusinessIcon />,
            text: 'Neue Opportunity',
            shortcut: 'Ctrl+Shift+O',
            action: () => executeAction('create-opportunity')
          }
        ];
    }
  };

  return (
    <Menu
      anchorEl={anchorEl}
      open={Boolean(anchorEl)}
      onClose={onClose}
      PaperProps={{
        sx: { 
          minWidth: 200,
          boxShadow: 3,
          border: '1px solid',
          borderColor: 'divider'
        }
      }}
    >
      {getMenuItems().map((item, index) => 
        item.divider ? (
          <Divider key={index} />
        ) : (
          <MenuItem
            key={index}
            onClick={() => {
              item.action();
              onClose();
            }}
            sx={{ gap: 2 }}
          >
            {item.icon}
            <Box sx={{ flexGrow: 1 }}>
              <Typography variant="body2">{item.text}</Typography>
              {item.shortcut && (
                <Typography variant="caption" color="text.secondary">
                  {item.shortcut}
                </Typography>
              )}
            </Box>
          </MenuItem>
        )
      )}
    </Menu>
  );
};
```

---

## 🔧 BACKEND ACTION ORCHESTRATION

### 🎯 QuickActionService Implementation
```java
// QuickActionService.java
@ApplicationScoped
@Transactional
public class QuickActionService {
    
    @Inject
    CustomerService customerService;
    
    @Inject
    OpportunityService opportunityService;
    
    @Inject
    NotificationService notificationService;
    
    @Inject
    ActionAnalyticsService analyticsService;
    
    public QuickActionResponse executeAction(
        QuickActionRequest request, 
        String userId
    ) {
        var actionType = request.getActionType();
        var context = request.getContext();
        
        try {
            var result = switch (actionType) {
                case "call" -> handleCallAction(request, userId);
                case "email" -> handleEmailAction(request, userId);
                case "create-opportunity" -> handleCreateOpportunity(request, userId);
                case "create-customer" -> handleCreateCustomer(request, userId);
                case "create-proposal" -> handleCreateProposal(request, userId);
                case "schedule-meeting" -> handleScheduleMeeting(request, userId);
                case "credit-check" -> handleCreditCheck(request, userId);
                default -> throw new UnsupportedOperationException(
                    "Action not supported: " + actionType
                );
            };
            
            // Analytics tracking
            analyticsService.trackAction(
                userId, 
                actionType, 
                context.toString(),
                request.getMetadata()
            );
            
            return QuickActionResponse.success(result);
            
        } catch (Exception e) {
            log.error("Quick action failed", e);
            return QuickActionResponse.error(e.getMessage());
        }
    }
    
    private Object handleCallAction(QuickActionRequest request, String userId) {
        var customerId = request.getRequiredParam("customerId", String.class);
        var customer = customerService.findById(customerId);
        
        if (customer.getPhone() == null) {
            throw new IllegalStateException("Kunde hat keine Telefonnummer");
        }
        
        // Integration mit Telefonie-System (Mock)
        var callId = initiateCRMCall(customer.getPhone(), userId);
        
        // Aktivität vermerken
        var activity = ActivityEntry.builder()
            .customerId(customerId)
            .userId(userId)
            .type("call_initiated")
            .description("Anruf gestartet")
            .metadata(Map.of("phone", customer.getPhone(), "callId", callId))
            .timestamp(LocalDateTime.now())
            .build();
            
        activityService.recordActivity(activity);
        
        return Map.of(
            "callId", callId,
            "phone", customer.getPhone(),
            "customerName", customer.getName()
        );
    }
    
    private Object handleEmailAction(QuickActionRequest request, String userId) {
        var customerId = request.getRequiredParam("customerId", String.class);
        var template = request.getOptionalParam("template", String.class, "follow-up");
        
        var customer = customerService.findById(customerId);
        
        if (customer.getEmail() == null) {
            throw new IllegalStateException("Kunde hat keine E-Mail-Adresse");
        }
        
        // E-Mail-Composer mit Template öffnen
        var emailTemplate = emailTemplateService.getTemplate(template);
        var personalizedContent = emailTemplate.personalize(customer);
        
        return Map.of(
            "email", customer.getEmail(),
            "subject", personalizedContent.getSubject(),
            "body", personalizedContent.getBody(),
            "template", template
        );
    }
    
    private Object handleCreateOpportunity(QuickActionRequest request, String userId) {
        var customerId = request.getOptionalParam("customerId", String.class);
        var template = request.getOptionalParam("template", String.class, "standard");
        
        var opportunity = Opportunity.builder()
            .customerId(customerId)
            .ownerId(userId)
            .stage(OpportunityStage.LEAD)
            .source("quick_action")
            .priority(Priority.MEDIUM)
            .createdAt(LocalDateTime.now())
            .build();
            
        if (template.equals("standard") && customerId != null) {
            var customer = customerService.findById(customerId);
            opportunity.setEstimatedValue(
                calculateEstimatedValue(customer)
            );
        }
        
        var saved = opportunityService.create(opportunity);
        
        return Map.of(
            "opportunityId", saved.getId(),
            "stage", saved.getStage(),
            "customerId", saved.getCustomerId()
        );
    }
    
    // Utility methods
    private String initiateCRMCall(String phone, String userId) {
        // Integration mit CTI-System
        return "call_" + System.currentTimeMillis();
    }
    
    private BigDecimal calculateEstimatedValue(Customer customer) {
        // Intelligente Schätzung basierend auf Kundendaten
        return customer.getAverageOrderValue()
            .multiply(BigDecimal.valueOf(1.2)); // 20% Aufschlag
    }
}
```

---

## 🎨 UI INTEGRATION PATTERNS

### 🔥 Universeller Action Hook
```typescript
// useQuickActions.ts
interface QuickActionContext {
  customer?: Customer;
  opportunity?: Opportunity;
  location: string;
}

export const useQuickActions = () => {
  const [isLoading, setIsLoading] = useState(false);
  const { user } = useAuth();
  const { showNotification } = useNotification();
  
  const executeAction = async (
    actionType: string,
    params: Record<string, any> = {},
    context?: QuickActionContext
  ) => {
    setIsLoading(true);
    
    try {
      const response = await fetch('/api/quick-actions/execute', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${user.token}`
        },
        body: JSON.stringify({
          actionType,
          params,
          context: context || getCurrentContext(),
          metadata: {
            timestamp: new Date().toISOString(),
            userAgent: navigator.userAgent
          }
        })
      });
      
      const result = await response.json();
      
      if (result.success) {
        showNotification({
          type: 'success',
          message: `${actionType} erfolgreich ausgeführt`,
          duration: 3000
        });
        
        // Event für andere Komponenten
        window.dispatchEvent(new CustomEvent('quickActionComplete', {
          detail: { actionType, result: result.data }
        }));
        
        return result.data;
      } else {
        throw new Error(result.error);
      }
    } catch (error) {
      showNotification({
        type: 'error',
        message: `Fehler bei ${actionType}: ${error.message}`,
        duration: 5000
      });
      throw error;
    } finally {
      setIsLoading(false);
    }
  };
  
  const getCurrentContext = (): QuickActionContext => {
    const location = window.location.pathname;
    
    // Context aus URL und State extrahieren
    return {
      location,
      customer: getCurrentCustomer(),
      opportunity: getCurrentOpportunity()
    };
  };
  
  return {
    executeAction,
    isLoading
  };
};
```

---

## 📊 TESTING STRATEGY

### 🧪 Component Tests
```typescript
// __tests__/QuickActions.test.tsx
describe('Quick Actions', () => {
  test('Customer context actions sind verfügbar', () => {
    const customer = mockCustomer();
    
    render(
      <CustomerContextActions 
        customer={customer}
        onActionComplete={jest.fn()}
      />
    );
    
    expect(screen.getByLabelText('Anrufen (C)')).toBeInTheDocument();
    expect(screen.getByLabelText('E-Mail (E)')).toBeInTheDocument();
    expect(screen.getByLabelText('Opportunity (O)')).toBeInTheDocument();
  });
  
  test('FAB zeigt kontextuelle Actions', () => {
    const { rerender } = render(
      <MemoryRouter initialEntries={['/customers/123']}>
        <SmartFAB />
      </MemoryRouter>
    );
    
    // Auf Kunden-Seite
    expect(screen.getByLabelText('Anrufen')).toBeInTheDocument();
    
    // Auf Opportunity-Seite
    rerender(
      <MemoryRouter initialEntries={['/opportunities/456']}>
        <SmartFAB />
      </MemoryRouter>
    );
    
    expect(screen.getByLabelText('Angebot erstellen')).toBeInTheDocument();
  });
  
  test('Keyboard shortcuts funktionieren', () => {
    const mockExecuteAction = jest.fn();
    
    render(
      <TestComponent useQuickActions={() => ({ 
        executeAction: mockExecuteAction,
        isLoading: false 
      })} />
    );
    
    // Ctrl+Shift+C für Call
    fireEvent.keyDown(document, {
      key: 'c',
      ctrlKey: true,
      shiftKey: true
    });
    
    expect(mockExecuteAction).toHaveBeenCalledWith('call', expect.any(Object));
  });
});
```

### 🔧 Backend Tests
```java
// QuickActionServiceTest.java
@QuarkusTest
class QuickActionServiceTest {
    
    @Inject
    QuickActionService quickActionService;
    
    @Test
    void shouldExecuteCallAction() {
        var request = QuickActionRequest.builder()
            .actionType("call")
            .params(Map.of("customerId", "cust_123"))
            .build();
            
        var response = quickActionService.executeAction(request, "user_456");
        
        assertTrue(response.isSuccess());
        assertThat(response.getData())
            .containsKeys("callId", "phone", "customerName");
    }
    
    @Test
    void shouldCreateOpportunityWithEstimatedValue() {
        var request = QuickActionRequest.builder()
            .actionType("create-opportunity")
            .params(Map.of(
                "customerId", "cust_123",
                "template", "standard"
            ))
            .build();
            
        var response = quickActionService.executeAction(request, "user_456");
        
        assertTrue(response.isSuccess());
        var opportunityId = (String) response.getData().get("opportunityId");
        assertNotNull(opportunityId);
    }
    
    @Test
    void shouldTrackActionAnalytics() {
        var request = QuickActionRequest.builder()
            .actionType("email")
            .params(Map.of("customerId", "cust_123"))
            .build();
            
        quickActionService.executeAction(request, "user_456");
        
        verify(analyticsService).trackAction(
            eq("user_456"),
            eq("email"),
            any(),
            any()
        );
    }
}
```

---

## 🚀 DEPLOYMENT & ROLLOUT

### 📦 Feature Flag Configuration
```typescript
// featureFlags.ts
export const QUICK_ACTIONS_FLAGS = {
  CONTEXT_ACTIONS: 'quick_actions.context_actions',
  FLOATING_ACTION_BUTTON: 'quick_actions.fab',
  KEYBOARD_SHORTCUTS: 'quick_actions.shortcuts',
  ANALYTICS_TRACKING: 'quick_actions.analytics'
} as const;

// Gradual Rollout
const useQuickActionsFeature = () => {
  const { isEnabled } = useFeatureFlags();
  
  return {
    contextActions: isEnabled(QUICK_ACTIONS_FLAGS.CONTEXT_ACTIONS),
    floatingActionButton: isEnabled(QUICK_ACTIONS_FLAGS.FLOATING_ACTION_BUTTON),
    keyboardShortcuts: isEnabled(QUICK_ACTIONS_FLAGS.KEYBOARD_SHORTCUTS),
    analyticsTracking: isEnabled(QUICK_ACTIONS_FLAGS.ANALYTICS_TRACKING)
  };
};
```

### 📊 Success Metrics
```typescript
// Analytics Dashboard für Quick Actions
interface QuickActionMetrics {
  totalActions: number;
  actionsByType: Record<string, number>;
  averageTimeToAction: number; // ms
  userAdoptionRate: number; // %
  keyboardShortcutUsage: number; // %
  contextAccuracy: number; // % richtige Context Actions
}

// Tracking Implementation
const trackQuickActionUsage = () => {
  // Zeit bis zur Action
  const actionStartTime = performance.now();
  
  return (actionType: string) => {
    const duration = performance.now() - actionStartTime;
    
    analytics.track('quick_action_executed', {
      actionType,
      duration,
      context: getCurrentContext(),
      timestamp: new Date().toISOString()
    });
  };
};
```

---

## 🎯 SUCCESS CRITERIA & KPIs

### 📈 Quantitative Ziele
- **Zeitersparnis:** 80% Reduktion für Standard-Tasks (15 Min → 3 Min)
- **Adoption Rate:** 90% der aktiven User nutzen Quick Actions
- **Context Accuracy:** 95% korrekte Context Actions
- **Keyboard Shortcut Usage:** 60% der Power Users

### 🎨 Qualitative Ziele
- **Intuitive Bedienung:** Actions sind selbsterklärend
- **Kontextuelle Relevanz:** Actions passen zur aktuellen Situation
- **Performance:** <200ms Action-Response-Zeit
- **Accessibility:** Vollständige Keyboard-Navigation

### 🔄 Continuous Improvement
- **A/B Testing:** Verschiedene Action-Layouts
- **Usage Analytics:** Welche Actions werden am meisten genutzt
- **User Feedback:** Quarterly UX Reviews
- **Performance Monitoring:** Action-Response-Zeiten

---

## 💡 FUTURE ENHANCEMENTS

### 🤖 KI-basierte Action Suggestions
```typescript
// Zukünftige Erweiterung: ML-basierte Action Prediction
interface ActionPrediction {
  actionType: string;
  confidence: number;
  reasoning: string;
  suggestedParams: Record<string, any>;
}

const useActionPredictions = () => {
  // ML-Model analysiert User-Verhalten und schlägt Actions vor
  const predictions = useMemo(() => 
    mlService.predictNextActions(userContext), 
    [userContext]
  );
  
  return predictions;
};
```

### 🎯 Voice-Activated Actions
```typescript
// Integration mit FC-029 Voice-First Interface
const useVoiceActions = () => {
  const { executeAction } = useQuickActions();
  
  useEffect(() => {
    voiceService.registerCommands([
      {
        command: 'call customer',
        action: () => executeAction('call', getCurrentCustomerContext())
      },
      {
        command: 'create opportunity',
        action: () => executeAction('create-opportunity')
      }
    ]);
  }, []);
};
```

---

**🚀 FC-030 One-Tap Actions Tech Concept - KOMPLETT**

**Nächster Schritt:** Implementation Backend Services + Frontend Components  
**Geschätzter Aufwand:** 3 Tage  
**ROI:** Massive Produktivitätssteigerung durch 80% Zeitersparnis bei Standard-Tasks