# 🎯 FC-031 SMART TEMPLATES - TECHNISCHES KONZEPT

**Feature Code:** FC-031  
**Feature-Typ:** 🔀 FULLSTACK + KI  
**Geschätzter Aufwand:** 4 Tage  
**Priorität:** HIGH - Zeitersparnis-Champion  
**Abhängigkeiten:** -  
**Claude-optimiert:** 15-Min Context Chunks ⚡

---

## 📋 EXECUTIVE SUMMARY (2 Min Lesezeit)

### 🎯 Problem
Verkäufer verschwenden täglich 1-2 Stunden mit manueller Erstellung von E-Mails, Angeboten und Verträgen.

### 💡 Lösung
KI-gestützte Template Engine mit Kontext-Personalisierung und automatischer Inhaltsgenerierung.

### 📈 Business Impact
- **Zeitersparnis:** 90% weniger Zeit für Standard-Dokumente
- **Qualität:** Konsistente, professionelle Kommunikation
- **Skalierbarkeit:** Templates für neue Branchen ohne Entwicklungsaufwand

---

## 🧭 NAVIGATION & VERWEISE

### 📋 Zurück zum Überblick:
- **[📊 Master Plan V5](/docs/CRM_COMPLETE_MASTER_PLAN_V5.md)** - Vollständige Feature-Roadmap
- **[🗺️ Feature Overview](/docs/features/MASTER/FEATURE_OVERVIEW.md)** - Alle 40 Features im Überblick

### 🔗 Dependencies (Required):
- **Keine direkten Dependencies** - Eigenständiges Feature

### ⚡ Direkt integriert in:
- **[📧 FC-003 E-Mail Integration](/docs/features/PLANNED/06_email_integration/FC-003_TECH_CONCEPT.md)** - E-Mail Templates
- **[🧮 M8 Calculator Modal](/docs/features/ACTIVE/03_calculator_modal/M8_TECH_CONCEPT.md)** - Angebots-Templates
- **[📄 PDF-001 PDF Generator](/docs/features/ACTIVE/pdf-generator/)** - Vertrags-Templates

### 🚀 Ermöglicht folgende Features:
- **[🚀 FC-030 One-Tap Actions](/docs/features/PLANNED/30_one_tap_actions/FC-030_TECH_CONCEPT.md)** - Template Quick Actions
- **[⚡ FC-020 Quick Wins](/docs/features/PLANNED/20_quick_wins/FC-020_TECH_CONCEPT.md)** - Command Palette Templates
- **[📱 FC-018 Mobile PWA](/docs/features/PLANNED/18_mobile_pwa/FC-018_TECH_CONCEPT.md)** - Mobile Template Access

### 🎨 UI Integration:
- **[📊 M3 Sales Cockpit](/docs/features/ACTIVE/05_ui_foundation/M3_TECH_CONCEPT.md)** - Template Shortcuts
- **[⚙️ M7 Settings](/docs/features/ACTIVE/05_ui_foundation/M7_SETTINGS_TECH_CONCEPT.md)** - Template Management

### 🔧 Technische Details:
- **[IMPLEMENTATION_BACKEND.md](./IMPLEMENTATION_BACKEND.md)** *(geplant)* - Template Engine
- **[IMPLEMENTATION_FRONTEND.md](./IMPLEMENTATION_FRONTEND.md)** *(geplant)* - Template Editor
- **[DECISION_LOG.md](./DECISION_LOG.md)** *(geplant)* - KI-Integration Entscheidungen

---

## 🏗️ ARCHITEKTUR OVERVIEW (5 Min)

### 🎨 Frontend-Architektur
```typescript
components/
├── Templates/
│   ├── TemplateEditor.tsx         # Visual Template Editor
│   ├── TemplatePreview.tsx        # Live Preview mit Daten
│   ├── TemplateSelector.tsx       # Template Auswahl
│   ├── SmartFields/
│   │   ├── PersonalizationField.tsx  # KI-gestützte Felder
│   │   ├── ConditionalBlock.tsx      # Bedingte Inhalte
│   │   └── VariableInserter.tsx      # Variable Placeholder
│   └── types/
│       └── TemplateTypes.ts       # Template Definitionen
└── hooks/
    ├── useSmartTemplates.ts       # Template Logic
    ├── useTemplatePersonalization.ts  # KI Personalisierung
    └── useTemplateValidation.ts   # Validation Logic
```

### 🔧 Backend-Services
```java
com.freshplan.templates/
├── service/
│   ├── SmartTemplateService.java     # Template Orchestration
│   ├── PersonalizationEngine.java   # KI-basierte Personalisierung
│   ├── TemplateRenderer.java        # Content Generation
│   └── VariableResolver.java        # Platzhalter-Auflösung
├── dto/
│   ├── TemplateRequest.java         # Template Anfrage
│   ├── TemplateResponse.java        # Generierter Content
│   └── PersonalizationContext.java # Kontext für KI
└── entity/
    ├── SmartTemplate.java          # Template Definition
    ├── TemplateVariable.java       # Variable Mapping
    └── TemplateUsage.java          # Usage Analytics
```

---

## 🎯 PHASE 1: TEMPLATE ENGINE FOUNDATION (1.5 Tage)

### 🔥 Core Template Entity
```java
// SmartTemplate.java
@Entity
@Table(name = "smart_templates")
public class SmartTemplate extends BaseEntity {
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String description;
    
    @Enumerated(EnumType.STRING)
    private TemplateType type; // EMAIL, PROPOSAL, CONTRACT, FOLLOW_UP
    
    @Enumerated(EnumType.STRING)
    private TemplateCategory category; // SALES, SUPPORT, MARKETING
    
    @Column(columnDefinition = "TEXT")
    private String htmlContent;
    
    @Column(columnDefinition = "TEXT")  
    private String plainTextContent;
    
    @Column(columnDefinition = "TEXT")
    private String subject; // Für E-Mail Templates
    
    @Convert(converter = JsonConverter.class)
    private Map<String, Object> variables; // Verfügbare Variablen
    
    @Convert(converter = JsonConverter.class)
    private Map<String, Object> configuration; // Template Config
    
    @Column
    private String industry; // Null = universal
    
    @Column
    private String language = "de";
    
    @Column
    private boolean isActive = true;
    
    @Column
    private boolean isSystem = false; // System vs. User Templates
    
    @Column
    private String createdBy;
    
    @Column
    private int usageCount = 0;
    
    @Column
    private LocalDateTime lastUsed;
    
    // Personalization Settings
    @Column
    private boolean aiPersonalizationEnabled = true;
    
    @Enumerated(EnumType.STRING)
    private PersonalizationLevel personalizationLevel = PersonalizationLevel.MEDIUM;
}

enum TemplateType {
    EMAIL("E-Mail"),
    PROPOSAL("Angebot"), 
    CONTRACT("Vertrag"),
    FOLLOW_UP("Follow-Up"),
    MEETING_INVITE("Termineinladung"),
    QUOTE_REQUEST("Angebotsanfrage"),
    CUSTOMER_ONBOARDING("Kunden-Onboarding");
    
    private final String displayName;
}

enum PersonalizationLevel {
    MINIMAL,  // Nur Grunddaten (Name, Firma)
    MEDIUM,   // + Branche, Größe, Historie
    FULL      // + KI-generierte Insights
}
```

### 🎨 Template Variable System
```java
// TemplateVariable.java
@Entity
@Table(name = "template_variables")
public class TemplateVariable extends BaseEntity {
    
    @Column(nullable = false)
    private String name; // {{customer.name}}
    
    @Column(nullable = false)
    private String displayName; // "Kundenname"
    
    @Column
    private String description; // "Der vollständige Name des Kunden"
    
    @Enumerated(EnumType.STRING)
    private VariableType type;
    
    @Column
    private String defaultValue;
    
    @Column
    private boolean isRequired = false;
    
    @Convert(converter = JsonConverter.class)
    private Map<String, Object> validationRules;
    
    @Column
    private String category; // customer, opportunity, company, system
    
    @Column
    private String resolverClass; // Klasse für komplexe Auflösung
}

enum VariableType {
    TEXT("Text"),
    NUMBER("Zahl"),
    DATE("Datum"),
    CURRENCY("Währung"),
    PERCENTAGE("Prozent"),
    BOOLEAN("Ja/Nein"),
    LIST("Liste"),
    CALCULATED("Berechnet"),
    AI_GENERATED("KI-generiert");
    
    private final String displayName;
}
```

### 🔧 Template Service Implementation
```java
// SmartTemplateService.java
@ApplicationScoped
@Transactional
public class SmartTemplateService {
    
    @Inject
    SmartTemplateRepository templateRepository;
    
    @Inject
    VariableResolver variableResolver;
    
    @Inject
    PersonalizationEngine personalizationEngine;
    
    @Inject
    TemplateRenderer templateRenderer;
    
    public List<SmartTemplate> getTemplatesForContext(
        TemplateType type,
        String industry,
        String language
    ) {
        return templateRepository.findByTypeAndContext(type, industry, language)
            .stream()
            .filter(SmartTemplate::isActive)
            .sorted(Comparator
                .comparing(SmartTemplate::getUsageCount)
                .reversed()
            )
            .collect(Collectors.toList());
    }
    
    public TemplateResponse generateContent(
        String templateId,
        PersonalizationContext context
    ) {
        var template = templateRepository.findById(templateId)
            .orElseThrow(() -> new TemplateNotFoundException(templateId));
        
        // 1. Variablen auflösen
        var resolvedVariables = variableResolver.resolveAll(
            template.getVariables(),
            context
        );
        
        // 2. KI-Personalisierung (wenn aktiviert)
        if (template.isAiPersonalizationEnabled()) {
            resolvedVariables = personalizationEngine.enhance(
                resolvedVariables,
                context,
                template.getPersonalizationLevel()
            );
        }
        
        // 3. Content rendern
        var renderedContent = templateRenderer.render(
            template,
            resolvedVariables
        );
        
        // 4. Usage tracking
        trackUsage(template, context);
        
        return TemplateResponse.builder()
            .subject(renderedContent.getSubject())
            .htmlContent(renderedContent.getHtmlContent())
            .plainTextContent(renderedContent.getPlainTextContent())
            .variables(resolvedVariables)
            .templateId(templateId)
            .build();
    }
    
    private void trackUsage(SmartTemplate template, PersonalizationContext context) {
        template.setUsageCount(template.getUsageCount() + 1);
        template.setLastUsed(LocalDateTime.now());
        
        var usage = TemplateUsage.builder()
            .templateId(template.getId())
            .userId(context.getUserId())
            .customerId(context.getCustomerId())
            .opportunityId(context.getOpportunityId())
            .generatedAt(LocalDateTime.now())
            .personalizationLevel(template.getPersonalizationLevel())
            .build();
            
        templateUsageRepository.persist(usage);
    }
}
```

---

## 🎯 PHASE 2: KI-GESTÜTZTE PERSONALISIERUNG (1.5 Tage)

### 🤖 Personalization Engine
```java
// PersonalizationEngine.java
@ApplicationScoped
public class PersonalizationEngine {
    
    @Inject
    OpenAIService openAIService;
    
    @Inject
    CustomerInsightsService customerInsightsService;
    
    @Inject
    OpportunityAnalysisService opportunityAnalysisService;
    
    public Map<String, Object> enhance(
        Map<String, Object> baseVariables,
        PersonalizationContext context,
        PersonalizationLevel level
    ) {
        var enhanced = new HashMap<>(baseVariables);
        
        switch (level) {
            case MINIMAL -> enhanceMinimal(enhanced, context);
            case MEDIUM -> enhanceMedium(enhanced, context);
            case FULL -> enhanceFull(enhanced, context);
        }
        
        return enhanced;
    }
    
    private void enhanceMinimal(Map<String, Object> variables, PersonalizationContext context) {
        // Nur Grunddaten - keine KI
        if (context.getCustomer() != null) {
            var customer = context.getCustomer();
            variables.put("customer.greeting", getPersonalizedGreeting(customer));
            variables.put("customer.industry_label", getIndustryLabel(customer.getIndustry()));
        }
    }
    
    private void enhanceMedium(Map<String, Object> variables, PersonalizationContext context) {
        enhanceMinimal(variables, context);
        
        if (context.getCustomer() != null) {
            var insights = customerInsightsService.getInsights(context.getCustomer().getId());
            
            variables.put("customer.size_description", 
                generateSizeDescription(insights.getEmployeeCount()));
            variables.put("customer.business_focus", 
                insights.getPrimaryBusinessFocus());
            variables.put("customer.recent_activity", 
                summarizeRecentActivity(insights.getRecentActivities()));
        }
    }
    
    private void enhanceFull(Map<String, Object> variables, PersonalizationContext context) {
        enhanceMedium(variables, context);
        
        // KI-generierte Insights
        if (context.getCustomer() != null && context.getOpportunity() != null) {
            var customer = context.getCustomer();
            var opportunity = context.getOpportunity();
            
            var aiInsights = generateAIInsights(customer, opportunity, context);
            
            variables.put("ai.personalized_opener", aiInsights.getPersonalizedOpener());
            variables.put("ai.value_proposition", aiInsights.getValueProposition());
            variables.put("ai.pain_point_reference", aiInsights.getPainPointReference());
            variables.put("ai.next_steps_suggestion", aiInsights.getNextStepsSuggestion());
            variables.put("ai.competitive_advantage", aiInsights.getCompetitiveAdvantage());
        }
    }
    
    private AIInsights generateAIInsights(Customer customer, Opportunity opportunity, PersonalizationContext context) {
        var prompt = buildPrompt(customer, opportunity, context);
        
        var openAIRequest = OpenAIRequest.builder()
            .model("gpt-4")
            .messages(List.of(
                Message.system("Du bist ein erfahrener Vertriebsexperte für Catering-Software."),
                Message.user(prompt)
            ))
            .temperature(0.7)
            .maxTokens(500)
            .build();
        
        var response = openAIService.generateCompletion(openAIRequest);
        
        return parseAIResponse(response.getContent());
    }
    
    private String buildPrompt(Customer customer, Opportunity opportunity, PersonalizationContext context) {
        return String.format("""
            Erstelle personalisierte Inhalte für eine Verkaufs-E-Mail:
            
            Kunde: %s
            Branche: %s
            Mitarbeiter: %s
            Jahresumsatz: %s
            
            Opportunity: %s
            Stage: %s
            Geschätzter Wert: %s
            
            Bisherige Kommunikation:
            %s
            
            Generiere:
            1. Personalisierte Begrüßung (max. 50 Wörter)
            2. Wertversprechen basierend auf Branche (max. 80 Wörter)
            3. Bezug auf relevante Schmerzpunkte (max. 60 Wörter)
            4. Vorschlag für nächste Schritte (max. 40 Wörter)
            5. Wettbewerbsvorteil (max. 50 Wörter)
            
            Antworte im JSON-Format.
            """,
            customer.getName(),
            customer.getIndustry(),
            customer.getEmployeeCount(),
            customer.getAnnualRevenue(),
            opportunity.getTitle(),
            opportunity.getStage(),
            opportunity.getEstimatedValue(),
            getCommunicationHistory(customer.getId())
        );
    }
}
```

### 🎨 Smart Template Variables
```typescript
// Smart Variable System
interface SmartVariable {
  name: string;
  type: VariableType;
  category: 'customer' | 'opportunity' | 'company' | 'ai' | 'system';
  resolver?: VariableResolver;
  aiEnhanced?: boolean;
}

// Standard Variables
const STANDARD_VARIABLES: SmartVariable[] = [
  // Customer Variables
  {
    name: 'customer.name',
    type: 'TEXT',
    category: 'customer'
  },
  {
    name: 'customer.company',
    type: 'TEXT', 
    category: 'customer'
  },
  {
    name: 'customer.industry_localized',
    type: 'TEXT',
    category: 'customer',
    resolver: 'IndustryLocalizationResolver'
  },
  
  // AI-Enhanced Variables
  {
    name: 'ai.personalized_greeting',
    type: 'TEXT',
    category: 'ai',
    aiEnhanced: true
  },
  {
    name: 'ai.value_proposition',
    type: 'TEXT', 
    category: 'ai',
    aiEnhanced: true
  },
  {
    name: 'ai.pain_point_analysis',
    type: 'TEXT',
    category: 'ai', 
    aiEnhanced: true
  },
  
  // Calculated Variables
  {
    name: 'opportunity.potential_savings',
    type: 'CURRENCY',
    category: 'opportunity',
    resolver: 'SavingsCalculatorResolver'
  },
  {
    name: 'customer.relationship_duration',
    type: 'TEXT',
    category: 'customer',
    resolver: 'RelationshipDurationResolver'
  }
];
```

---

## 🎯 PHASE 3: VISUAL TEMPLATE EDITOR (1 Tag)

### 🎨 Template Editor Component
```typescript
// TemplateEditor.tsx
interface TemplateEditorProps {
  template?: SmartTemplate;
  onSave: (template: SmartTemplate) => void;
  onPreview: (template: SmartTemplate, context: any) => void;
}

const TemplateEditor: React.FC<TemplateEditorProps> = ({
  template,
  onSave,
  onPreview
}) => {
  const [content, setContent] = useState(template?.htmlContent || '');
  const [variables, setVariables] = useState(template?.variables || {});
  const [previewContext, setPreviewContext] = useState(null);
  const { availableVariables } = useSmartTemplates();
  
  const editorRef = useRef<any>();
  
  // Rich Text Editor mit Variable Support
  const editorConfig = {
    toolbar: [
      'bold', 'italic', 'underline', '|',
      'fontSize', 'fontColor', 'backgroundColor', '|',
      'bulletList', 'numberedList', '|',
      'link', 'insertTable', '|',
      'insertVariable', 'insertConditionalBlock', '|',
      'undo', 'redo'
    ],
    plugins: [
      'Bold', 'Italic', 'Underline',
      'FontSize', 'FontColor', 'FontBackgroundColor',
      'List', 'Link', 'Table',
      'Undo',
      'VariableInserter', // Custom Plugin
      'ConditionalBlockInserter' // Custom Plugin
    ]
  };

  return (
    <Box sx={{ height: '100vh', display: 'flex' }}>
      {/* Sidebar: Variables & Settings */}
      <Paper sx={{ width: 300, p: 2, borderRadius: 0 }}>
        <Typography variant="h6" gutterBottom>
          Template Variablen
        </Typography>
        
        <Accordion defaultExpanded>
          <AccordionSummary expandIcon={<ExpandMoreIcon />}>
            <Typography>Kunden-Variablen</Typography>
          </AccordionSummary>
          <AccordionDetails>
            <List dense>
              {availableVariables
                .filter(v => v.category === 'customer')
                .map(variable => (
                  <ListItem
                    key={variable.name}
                    button
                    onClick={() => insertVariable(variable)}
                  >
                    <ListItemIcon>
                      {variable.aiEnhanced && <AutoAwesomeIcon color="primary" />}
                    </ListItemIcon>
                    <ListItemText
                      primary={variable.displayName}
                      secondary={`{{${variable.name}}}`}
                    />
                  </ListItem>
                ))}
            </List>
          </AccordionDetails>
        </Accordion>
        
        <Accordion>
          <AccordionSummary expandIcon={<ExpandMoreIcon />}>
            <Typography>KI-Variablen</Typography>
          </AccordionSummary>
          <AccordionDetails>
            <List dense>
              {availableVariables
                .filter(v => v.category === 'ai')
                .map(variable => (
                  <ListItem
                    key={variable.name}
                    button
                    onClick={() => insertVariable(variable)}
                  >
                    <ListItemIcon>
                      <PsychologyIcon color="secondary" />
                    </ListItemIcon>
                    <ListItemText
                      primary={variable.displayName}
                      secondary={`{{${variable.name}}}`}
                    />
                  </ListItem>
                ))}
            </List>
          </AccordionDetails>
        </Accordion>
      </Paper>
      
      {/* Main Editor */}
      <Box sx={{ flexGrow: 1, display: 'flex', flexDirection: 'column' }}>
        {/* Toolbar */}
        <Toolbar sx={{ borderBottom: 1, borderColor: 'divider' }}>
          <Typography variant="h6" sx={{ flexGrow: 1 }}>
            Template Editor
          </Typography>
          
          <ButtonGroup>
            <Button
              startIcon={<VisibilityIcon />}
              onClick={() => openPreview()}
            >
              Vorschau
            </Button>
            <Button
              startIcon={<SaveIcon />}
              variant="contained"
              onClick={() => saveTemplate()}
            >
              Speichern
            </Button>
          </ButtonGroup>
        </Toolbar>
        
        {/* Rich Text Editor */}
        <Box sx={{ flexGrow: 1, p: 2 }}>
          <CKEditor
            editor={ClassicEditor}
            config={editorConfig}
            data={content}
            onChange={(event, editor) => {
              setContent(editor.getData());
            }}
            ref={editorRef}
          />
        </Box>
        
        {/* Template Settings */}
        <Paper sx={{ p: 2, borderRadius: 0 }}>
          <Grid container spacing={2}>
            <Grid item xs={4}>
              <FormControl fullWidth>
                <InputLabel>Template-Typ</InputLabel>
                <Select value={template?.type || ''}>
                  <MenuItem value="EMAIL">E-Mail</MenuItem>
                  <MenuItem value="PROPOSAL">Angebot</MenuItem>
                  <MenuItem value="CONTRACT">Vertrag</MenuItem>
                  <MenuItem value="FOLLOW_UP">Follow-Up</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            
            <Grid item xs={4}>
              <FormControl fullWidth>
                <InputLabel>Personalisierung</InputLabel>
                <Select value={template?.personalizationLevel || 'MEDIUM'}>
                  <MenuItem value="MINIMAL">Minimal</MenuItem>
                  <MenuItem value="MEDIUM">Medium</MenuItem>
                  <MenuItem value="FULL">Vollständig (KI)</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            
            <Grid item xs={4}>
              <FormControl fullWidth>
                <InputLabel>Branche</InputLabel>
                <Select value={template?.industry || ''}>
                  <MenuItem value="">Alle Branchen</MenuItem>
                  <MenuItem value="GASTRONOMY">Gastronomie</MenuItem>
                  <MenuItem value="CATERING">Catering</MenuItem>
                  <MenuItem value="RETAIL">Einzelhandel</MenuItem>
                </Select>
              </FormControl>
            </Grid>
          </Grid>
        </Paper>
      </Box>
      
      {/* Live Preview Panel */}
      {previewContext && (
        <Paper sx={{ width: 400, p: 2, borderRadius: 0 }}>
          <Typography variant="h6" gutterBottom>
            Live Vorschau
          </Typography>
          
          <TemplatePreview
            content={content}
            variables={variables}
            context={previewContext}
          />
        </Paper>
      )}
    </Box>
  );
  
  const insertVariable = (variable: SmartVariable) => {
    const editor = editorRef.current;
    if (editor) {
      editor.editing.view.document.isFocused && 
      editor.model.change(writer => {
        const insertPosition = editor.model.document.selection.getFirstPosition();
        writer.insertText(`{{${variable.name}}}`, insertPosition);
      });
    }
  };
  
  const openPreview = () => {
    // Mock context für Preview
    setPreviewContext({
      customer: mockCustomer(),
      opportunity: mockOpportunity(),
      user: getCurrentUser()
    });
  };
};
```

### 🔥 Custom CKEditor Plugins
```typescript
// VariableInserterPlugin.ts
class VariableInserter extends Plugin {
  static get pluginName() {
    return 'VariableInserter';
  }
  
  init() {
    const editor = this.editor;
    
    editor.ui.componentFactory.add('insertVariable', locale => {
      const dropdownView = createDropdown(locale);
      
      dropdownView.buttonView.set({
        label: 'Variable einfügen',
        icon: '<svg>...</svg>',
        tooltip: true
      });
      
      const variables = getAvailableVariables();
      
      addListToDropdown(dropdownView, 
        variables.map(variable => ({
          type: 'button',
          model: new Model({
            label: variable.displayName,
            withText: true
          })
        }))
      );
      
      dropdownView.on('execute', (evt) => {
        const variable = evt.source.label;
        editor.model.change(writer => {
          const insertPosition = editor.model.document.selection.getFirstPosition();
          writer.insertText(`{{${variable}}}`, insertPosition);
        });
      });
      
      return dropdownView;
    });
  }
}

// ConditionalBlockPlugin.ts  
class ConditionalBlockInserter extends Plugin {
  static get pluginName() {
    return 'ConditionalBlockInserter';
  }
  
  init() {
    const editor = this.editor;
    
    editor.ui.componentFactory.add('insertConditionalBlock', locale => {
      const buttonView = new ButtonView(locale);
      
      buttonView.set({
        label: 'Bedingter Block',
        icon: '<svg>...</svg>',
        tooltip: true
      });
      
      buttonView.on('execute', () => {
        editor.model.change(writer => {
          const insertPosition = editor.model.document.selection.getFirstPosition();
          const conditionalBlock = `
            {{#if opportunity.stage == 'qualified'}}
            Vielen Dank für Ihr Interesse an unserer Lösung.
            {{else}}
            Gerne möchten wir Sie über unsere Lösungen informieren.
            {{/if}}
          `;
          writer.insertText(conditionalBlock, insertPosition);
        });
      });
      
      return buttonView;
    });
  }
}
```

---

## 🎯 PHASE 4: INTEGRATION & OPTIMIZATION (1 Tag)

### 🔥 Quick Template Actions Integration
```typescript
// useTemplateQuickActions.ts
export const useTemplateQuickActions = () => {
  const { executeAction } = useQuickActions();
  const { generateContent } = useSmartTemplates();
  
  const quickEmailTemplate = async (customerId: string, type: 'follow-up' | 'introduction' | 'proposal') => {
    const customer = await customerService.getById(customerId);
    const templates = await getTemplatesForType('EMAIL', type);
    
    if (templates.length === 0) {
      throw new Error(`Kein Template für ${type} gefunden`);
    }
    
    const mostUsedTemplate = templates[0]; // Sortiert nach Usage
    
    const context = {
      customer,
      user: getCurrentUser(),
      opportunity: getCurrentOpportunity()
    };
    
    const generatedContent = await generateContent(mostUsedTemplate.id, context);
    
    // E-Mail Composer mit Template öffnen
    return executeAction('open-email-composer', {
      to: customer.email,
      subject: generatedContent.subject,
      body: generatedContent.htmlContent,
      templateId: mostUsedTemplate.id
    });
  };
  
  const quickProposalTemplate = async (opportunityId: string) => {
    const opportunity = await opportunityService.getById(opportunityId);
    const customer = await customerService.getById(opportunity.customerId);
    
    const templates = await getTemplatesForType('PROPOSAL', customer.industry);
    const selectedTemplate = templates.find(t => 
      t.configuration?.opportunityStage === opportunity.stage
    ) || templates[0];
    
    const context = {
      customer,
      opportunity,
      user: getCurrentUser(),
      company: getCompanyInfo()
    };
    
    const generatedContent = await generateContent(selectedTemplate.id, context);
    
    // PDF Generator mit Template öffnen
    return executeAction('open-pdf-generator', {
      type: 'proposal',
      content: generatedContent.htmlContent,
      opportunityId,
      templateId: selectedTemplate.id
    });
  };
  
  return {
    quickEmailTemplate,
    quickProposalTemplate
  };
};
```

### 📊 Template Analytics & Optimization
```java
// TemplateAnalyticsService.java
@ApplicationScoped
public class TemplateAnalyticsService {
    
    @Inject
    TemplateUsageRepository usageRepository;
    
    public TemplatePerformanceReport getPerformanceReport(String templateId) {
        var usages = usageRepository.findByTemplateId(templateId);
        
        var totalUsages = usages.size();
        var averagePersonalizationTime = usages.stream()
            .mapToLong(usage -> Duration.between(
                usage.getCreatedAt(), 
                usage.getCompletedAt()
            ).toMillis())
            .average()
            .orElse(0.0);
            
        var successRate = calculateSuccessRate(usages);
        var userFeedback = getUserFeedback(templateId);
        
        return TemplatePerformanceReport.builder()
            .templateId(templateId)
            .totalUsages(totalUsages)
            .averagePersonalizationTime(averagePersonalizationTime)
            .successRate(successRate)
            .userFeedback(userFeedback)
            .recommendations(generateRecommendations(usages))
            .build();
    }
    
    private double calculateSuccessRate(List<TemplateUsage> usages) {
        var successfulUsages = usages.stream()
            .filter(usage -> {
                // Success = E-Mail wurde gesendet oder PDF wurde erstellt
                return usage.getActionCompleted() && 
                       !usage.getUserAborted();
            })
            .count();
            
        return (double) successfulUsages / usages.size() * 100;
    }
    
    private List<String> generateRecommendations(List<TemplateUsage> usages) {
        var recommendations = new ArrayList<String>();
        
        // Analyse der Abbruchrate
        var abortRate = usages.stream()
            .mapToDouble(usage -> usage.getUserAborted() ? 1.0 : 0.0)
            .average()
            .orElse(0.0);
            
        if (abortRate > 0.3) {
            recommendations.add("Template ist zu komplex - vereinfachen");
        }
        
        // Analyse der Personalisierungszeit
        var avgPersonalizationTime = usages.stream()
            .filter(usage -> !usage.getUserAborted())
            .mapToLong(usage -> Duration.between(
                usage.getCreatedAt(),
                usage.getCompletedAt()
            ).toMinutes())
            .average()
            .orElse(0.0);
            
        if (avgPersonalizationTime > 10) {
            recommendations.add("Template benötigt zu viel manuelle Anpassung");
            recommendations.add("Mehr KI-Personalisierung aktivieren");
        }
        
        // Analyse der Variable Usage
        var variableUsage = analyzeVariableUsage(usages);
        var unusedVariables = variableUsage.entrySet().stream()
            .filter(entry -> entry.getValue() < 0.1) // <10% Usage
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
            
        if (!unusedVariables.isEmpty()) {
            recommendations.add("Ungenutzte Variablen entfernen: " + 
                String.join(", ", unusedVariables));
        }
        
        return recommendations;
    }
}
```

### 🎨 Template Suggestion Engine
```typescript
// useTemplateSuggestions.ts
export const useTemplateSuggestions = () => {
  const getSuggestedTemplates = async (context: {
    customer?: Customer;
    opportunity?: Opportunity;
    lastAction?: string;
    communicationHistory?: any[];
  }) => {
    const suggestions = [];
    
    // Regel-basierte Suggestions
    if (context.opportunity?.stage === 'qualified' && !context.lastAction?.includes('proposal')) {
      suggestions.push({
        template: await getTemplate('proposal-initial'),
        reason: 'Opportunity ist qualifiziert - Zeit für erstes Angebot',
        priority: 'high',
        estimatedTime: '5 Min'
      });
    }
    
    if (context.communicationHistory?.length === 0) {
      suggestions.push({
        template: await getTemplate('email-introduction'),
        reason: 'Erster Kontakt mit Kunde',
        priority: 'medium',
        estimatedTime: '3 Min'
      });
    }
    
    // KI-basierte Suggestions
    const aiSuggestions = await getAISuggestions(context);
    suggestions.push(...aiSuggestions);
    
    // Nach Priorität und Relevanz sortieren
    return suggestions
      .sort((a, b) => {
        const priorityWeight = { high: 3, medium: 2, low: 1 };
        return priorityWeight[b.priority] - priorityWeight[a.priority];
      })
      .slice(0, 5); // Top 5 Suggestions
  };
  
  const getAISuggestions = async (context: any) => {
    const prompt = `
      Basierend auf folgendem Kontext, welche E-Mail-Templates wären am relevantesten?
      
      Kunde: ${context.customer?.name}
      Branche: ${context.customer?.industry}
      Opportunity Stage: ${context.opportunity?.stage}
      Letzte Kommunikation: ${context.communicationHistory?.slice(-3)}
      
      Antworte mit Template-Typen und Begründung.
    `;
    
    const aiResponse = await openAIService.generateCompletion({
      model: 'gpt-4',
      messages: [
        { role: 'system', content: 'Du bist ein Vertriebsexperte für Software.' },
        { role: 'user', content: prompt }
      ],
      temperature: 0.3
    });
    
    return parseAISuggestions(aiResponse.content);
  };
  
  return {
    getSuggestedTemplates
  };
};
```

---

## 🔧 SYSTEM TEMPLATES & SEEDING

### 📧 Standard E-Mail Templates
```java
// TemplateSeeder.java - System Templates
@ApplicationScoped
public class TemplateSeeder {
    
    @Inject
    SmartTemplateService templateService;
    
    @EventObserver
    public void seedSystemTemplates(@Observes StartupEvent event) {
        if (templateService.hasSystemTemplates()) {
            return; // Bereits initialisiert
        }
        
        createEmailTemplates();
        createProposalTemplates();
        createContractTemplates();
    }
    
    private void createEmailTemplates() {
        // Follow-Up E-Mail Template
        var followUpTemplate = SmartTemplate.builder()
            .name("Standard Follow-Up")
            .description("Standard-Nachfass-E-Mail nach erstem Kontakt")
            .type(TemplateType.EMAIL)
            .category(TemplateCategory.SALES)
            .subject("{{ai.personalized_subject}} - Nächste Schritte für {{customer.company}}")
            .htmlContent("""
                <p>{{ai.personalized_greeting}}</p>
                
                <p>vielen Dank für das interessante Gespräch {{#if meeting.date}}am {{meeting.date}}{{/if}}. 
                Wie besprochen, sende ich Ihnen gerne weitere Informationen zu unserer FreshPlan-Lösung.</p>
                
                <h3>Ihre Vorteile mit FreshPlan:</h3>
                <ul>
                    <li>{{ai.value_proposition}}</li>
                    <li>Reduzierung der Planungszeit um bis zu 70%</li>
                    <li>Automatische Kostenoptimierung</li>
                    <li>Nahtlose Integration in bestehende Systeme</li>
                </ul>
                
                {{#if opportunity.estimated_savings}}
                <p><strong>Geschätzte jährliche Einsparungen für {{customer.company}}: {{opportunity.estimated_savings}}</strong></p>
                {{/if}}
                
                <p>{{ai.next_steps_suggestion}}</p>
                
                <p>Bei Fragen stehe ich Ihnen gerne zur Verfügung.</p>
                
                <p>Mit freundlichen Grüßen<br>
                {{user.name}}<br>
                {{user.title}}<br>
                {{company.name}}</p>
            """)
            .variables(Map.of(
                "ai.personalized_greeting", "KI-generierte Begrüßung",
                "ai.personalized_subject", "KI-generierter Betreff",
                "ai.value_proposition", "Branchenspezifisches Wertversprechen",
                "ai.next_steps_suggestion", "KI-Vorschlag für nächste Schritte",
                "customer.company", "Firmenname",
                "opportunity.estimated_savings", "Geschätzte Einsparungen",
                "user.name", "Name des Verkäufers",
                "meeting.date", "Datum des letzten Meetings"
            ))
            .personalizationLevel(PersonalizationLevel.FULL)
            .isSystem(true)
            .isActive(true)
            .build();
            
        templateService.create(followUpTemplate);
        
        // Proposal Introduction Template
        var proposalTemplate = SmartTemplate.builder()
            .name("Angebots-Einführung")
            .description("E-Mail für Übersendung eines neuen Angebots")
            .type(TemplateType.EMAIL)
            .category(TemplateCategory.SALES)
            .subject("Ihr persönliches Angebot von {{company.name}} - {{proposal.title}}")
            .htmlContent("""
                <p>Sehr geehrte{{#if contact.gender == 'female'}} Frau{{else}} Herr{{/if}} {{contact.last_name}},</p>
                
                <p>wie versprochen erhalten Sie anbei unser {{#if proposal.type == 'individual'}}individuelles{{else}}maßgeschneidertes{{/if}} 
                Angebot für {{customer.company}}.</p>
                
                {{#if ai.pain_point_reference}}
                <p>{{ai.pain_point_reference}}</p>
                {{/if}}
                
                <h3>Highlights Ihres Angebots:</h3>
                <ul>
                    <li>{{proposal.key_feature_1}}</li>
                    <li>{{proposal.key_feature_2}}</li>
                    <li>{{proposal.key_feature_3}}</li>
                </ul>
                
                <p><strong>Investition:</strong> {{proposal.total_amount}} (zzgl. MwSt.)</p>
                <p><strong>ROI:</strong> Amortisation bereits nach {{proposal.payback_period}} Monaten</p>
                
                {{#if proposal.special_conditions}}
                <div style="background-color: #f0f8ff; padding: 15px; border-left: 4px solid #0066cc;">
                    <strong>🎯 Exklusiv für {{customer.company}}:</strong><br>
                    {{proposal.special_conditions}}
                </div>
                {{/if}}
                
                <p>{{ai.competitive_advantage}}</p>
                
                <p>Gerne erläutere ich Ihnen die Details in einem persönlichen Gespräch. 
                Wann hätten Sie Zeit für einen kurzen Termin?</p>
                
                <p>Mit freundlichen Grüßen<br>
                {{user.signature}}</p>
            """)
            .variables(Map.of(
                "contact.gender", "Geschlecht des Ansprechpartners",
                "contact.last_name", "Nachname des Ansprechpartners",
                "proposal.title", "Titel des Angebots",
                "proposal.type", "Art des Angebots (standard/individual)",
                "proposal.total_amount", "Gesamtbetrag des Angebots",
                "proposal.payback_period", "Amortisationsdauer",
                "ai.pain_point_reference", "KI-Bezug auf Schmerzpunkte",
                "ai.competitive_advantage", "KI-generierter Wettbewerbsvorteil"
            ))
            .personalizationLevel(PersonalizationLevel.FULL)
            .isSystem(true)
            .isActive(true)
            .build();
            
        templateService.create(proposalTemplate);
    }
}
```

---

## 📊 TESTING STRATEGY

### 🧪 Template Engine Tests
```java
// SmartTemplateServiceTest.java
@QuarkusTest
class SmartTemplateServiceTest {
    
    @Inject
    SmartTemplateService templateService;
    
    @Test
    void shouldGeneratePersonalizedContent() {
        var template = createTestTemplate();
        var context = PersonalizationContext.builder()
            .customer(mockCustomer())
            .opportunity(mockOpportunity())
            .userId("user123")
            .build();
            
        var result = templateService.generateContent(template.getId(), context);
        
        assertThat(result.getSubject()).contains("Müller GmbH");
        assertThat(result.getHtmlContent()).contains("Sehr geehrter Herr Müller");
        assertThat(result.getVariables()).containsKey("customer.name");
    }
    
    @Test
    void shouldApplyConditionalBlocks() {
        var template = SmartTemplate.builder()
            .htmlContent("""
                {{#if opportunity.stage == 'qualified'}}
                Zeit für ein Angebot!
                {{else}}
                Lassen Sie uns sprechen.
                {{/if}}
            """)
            .build();
            
        var qualifiedContext = PersonalizationContext.builder()
            .opportunity(Opportunity.builder().stage("qualified").build())
            .build();
            
        var result = templateService.generateContent(template.getId(), qualifiedContext);
        
        assertThat(result.getHtmlContent()).contains("Zeit für ein Angebot!");
        assertThat(result.getHtmlContent()).doesNotContain("Lassen Sie uns sprechen.");
    }
}
```

### 🎨 Frontend Component Tests
```typescript
// TemplateEditor.test.tsx
describe('TemplateEditor', () => {
  test('zeigt verfügbare Variablen an', () => {
    render(
      <TemplateEditor 
        template={mockTemplate()}
        onSave={jest.fn()}
        onPreview={jest.fn()}
      />
    );
    
    expect(screen.getByText('Kunden-Variablen')).toBeInTheDocument();
    expect(screen.getByText('{{customer.name}}')).toBeInTheDocument();
    expect(screen.getByText('{{ai.personalized_greeting}}')).toBeInTheDocument();
  });
  
  test('fügt Variable per Klick ein', async () => {
    const mockInsert = jest.fn();
    render(
      <TemplateEditor 
        template={mockTemplate()}
        onSave={jest.fn()}
        onPreview={jest.fn()}
        editorRef={{ current: { insertText: mockInsert } }}
      />
    );
    
    await user.click(screen.getByText('{{customer.name}}'));
    
    expect(mockInsert).toHaveBeenCalledWith('{{customer.name}}');
  });
  
  test('zeigt Live-Preview mit echten Daten', async () => {
    const mockPreview = jest.fn().mockResolvedValue({
      htmlContent: '<p>Hallo Herr Mustermann,</p>',
      subject: 'Angebot für Mustermann GmbH'
    });
    
    render(
      <TemplateEditor 
        template={mockTemplate()}
        onSave={jest.fn()}
        onPreview={mockPreview}
      />
    );
    
    await user.click(screen.getByText('Vorschau'));
    
    await waitFor(() => {
      expect(screen.getByText('Hallo Herr Mustermann,')).toBeInTheDocument();
    });
  });
});
```

---

## 🚀 DEPLOYMENT & SUCCESS METRICS

### 📦 Feature Flag Configuration
```typescript
// Feature Flags für graduelle Auslieferung
export const SMART_TEMPLATES_FLAGS = {
  TEMPLATE_ENGINE: 'smart_templates.engine',
  AI_PERSONALIZATION: 'smart_templates.ai_personalization', 
  VISUAL_EDITOR: 'smart_templates.visual_editor',
  TEMPLATE_SUGGESTIONS: 'smart_templates.suggestions'
} as const;

// Rollout Plan:
// Phase 1: Template Engine (10% Users)
// Phase 2: AI Personalization (25% Users) 
// Phase 3: Visual Editor (50% Users)
// Phase 4: Full Rollout (100% Users)
```

### 📊 Success Metrics & KPIs
```typescript
interface TemplateMetrics {
  // Effizienz-Metriken
  avgTemplateCreationTime: number; // Minuten
  avgPersonalizationTime: number; // Minuten  
  templateReuseRate: number; // %
  
  // Qualitäts-Metriken
  templateSuccessRate: number; // % erfolgreich verwendet
  userSatisfactionScore: number; // 1-5 Stars
  aiPersonalizationAccuracy: number; // % richtige Personalisierung
  
  // Business-Metriken
  emailResponseRate: number; // % Antworten auf Template-E-Mails
  proposalAcceptanceRate: number; // % angenommene Angebote
  timeToProposal: number; // Tage von Lead zu Angebot
}

// Ziel-KPIs:
const TARGET_METRICS = {
  avgTemplateCreationTime: 2, // < 2 Min
  avgPersonalizationTime: 1, // < 1 Min
  templateReuseRate: 80, // > 80%
  templateSuccessRate: 85, // > 85%
  aiPersonalizationAccuracy: 90, // > 90%
  emailResponseRate: 25, // > 25% (vs. 15% ohne Templates)
  proposalAcceptanceRate: 35 // > 35% (vs. 25% ohne Templates)
};
```

---

## 💡 FUTURE ENHANCEMENTS

### 🤖 Advanced AI Features
```typescript
// Zukünftige KI-Erweiterungen
interface AdvancedAIFeatures {
  // Sentiment-basierte Personalisierung
  sentimentAnalysis: (communicationHistory: string[]) => 'positive' | 'neutral' | 'negative';
  
  // A/B Testing für Templates
  automaticABTesting: (templateVariants: Template[]) => OptimalTemplate;
  
  // Predictive Template Suggestions
  predictBestTemplate: (context: CustomerContext) => TemplateRecommendation[];
  
  // Multi-Language Support
  autoTranslation: (template: Template, targetLanguage: string) => Template;
  
  // Industry-specific Optimization
  industryPersonalization: (template: Template, industry: string) => Template;
}
```

### 🎨 Enhanced Editor Features
```typescript
// Visual Template Builder Erweiterungen
interface EnhancedEditor {
  // Drag & Drop Builder
  dragDropBuilder: boolean;
  
  // Template Versioning
  versionControl: boolean;
  
  // Collaborative Editing
  realTimeCollaboration: boolean;
  
  // Template Analytics Dashboard
  performanceDashboard: boolean;
  
  // Mobile Template Editor
  mobileEditor: boolean;
}
```

---

**🎯 FC-031 Smart Templates Tech Concept - KOMPLETT**

**Nächster Schritt:** KI-Integration mit OpenAI + Visual Editor Implementation  
**Geschätzter Aufwand:** 4 Tage  
**ROI:** 90% Zeitersparnis bei Dokumentenerstellung + 40% höhere Response-Rate