# FC-031 CLAUDE_TECH: Smart Templates - KI-Powered Document Generation

**CLAUDE TECH** | **Original:** 1422 Zeilen → **Optimiert:** 520 Zeilen (63% Reduktion!)  
**Feature-Typ:** 🔀 FULLSTACK | **Priorität:** HOCH | **Geschätzter Aufwand:** 4 Tage

## ⚡ QUICK-LOAD (30 Sekunden bis produktiv!)

**KI-gesteuerte Template-Engine mit Personalisierung, Visual Editor und automatischer Content-Generation**

### 🎯 Das macht es:
- **Smart Generation**: OpenAI GPT-4 Integration für automatische Template-Erstellung + Personalisierung
- **Visual Editor**: Drag & Drop Template Builder + Real-time Preview + Variable Mapping
- **Context-Aware**: CRM-Daten Integration + Dynamic Content + Intelligent Suggestions
- **Multi-Format**: PDF, DOCX, HTML Export + Email Integration + Print-optimized Layouts

### 🚀 ROI:
- **90% Zeitersparnis** bei Dokumentenerstellung durch automatische Content-Generation
- **40% höhere Response-Rate** durch personalisierte, datengetriebene Templates
- **70% weniger Fehler** durch automatische Datenvalidierung und Template-Konsistenz
- **Break-even nach 2 Monaten** durch drastisch reduzierte Dokumentations-Zeiten

### 🏗️ Template Flow:
```
Template Selection → AI Content Generation → Visual Customization → Data Binding → Multi-Format Export → Delivery Tracking
```

---

## 📋 COPY-PASTE READY RECIPES

### 🔧 Backend Starter Kit

#### 1. Core Template Engine:
```java
@ApplicationScoped
public class SmartTemplateEngine {
    
    @Inject
    OpenAIService openAIService;
    
    @Inject
    TemplateRepository templateRepository;
    
    @Inject
    CustomerService customerService;
    
    @Inject
    DocumentGenerationService documentService;
    
    public CompletionStage<GeneratedDocument> generateDocument(
            UUID templateId, 
            UUID customerId, 
            GenerationRequest request) {
        
        return CompletableFuture
            .supplyAsync(() -> loadTemplateAndContext(templateId, customerId))
            .thenCompose(context -> generateAIContent(context, request))
            .thenCompose(enrichedContext -> processTemplateVariables(enrichedContext))
            .thenCompose(processedContext -> renderDocument(processedContext))
            .thenApply(document -> enhanceWithMetadata(document));
    }
    
    private TemplateContext loadTemplateAndContext(UUID templateId, UUID customerId) {
        Template template = templateRepository.findById(templateId)
            .orElseThrow(() -> new TemplateNotFoundException(templateId));
            
        Customer customer = customerService.findById(customerId)
            .orElseThrow(() -> new CustomerNotFoundException(customerId));
            
        // Load historical interactions, preferences, etc.
        CustomerInsights insights = customerService.getInsights(customerId);
        
        return TemplateContext.builder()
            .template(template)
            .customer(customer)
            .insights(insights)
            .generationTimestamp(Instant.now())
            .build();
    }
    
    private CompletionStage<TemplateContext> generateAIContent(
            TemplateContext context, 
            GenerationRequest request) {
        
        String prompt = buildPersonalizationPrompt(context, request);
        
        return openAIService.generateCompletion(OpenAIRequest.builder()
            .model("gpt-4")
            .prompt(prompt)
            .maxTokens(2000)
            .temperature(0.7)
            .build())
            .thenApply(aiResponse -> context.withAIContent(aiResponse.getContent()));
    }
    
    private String buildPersonalizationPrompt(TemplateContext context, GenerationRequest request) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Erstelle personalisierten Content für folgenden Kontext:\n\n");
        
        // Customer context
        prompt.append("KUNDE:\n");
        prompt.append("- Name: ").append(context.getCustomer().getName()).append("\n");
        prompt.append("- Branche: ").append(context.getCustomer().getIndustry()).append("\n");
        prompt.append("- Größe: ").append(context.getCustomer().getSize()).append("\n");
        
        // Historical insights
        if (context.getInsights().hasInteractions()) {
            prompt.append("\nVORHERIGE INTERAKTIONEN:\n");
            context.getInsights().getRecentInteractions()
                .forEach(interaction -> prompt.append("- ").append(interaction.getSummary()).append("\n"));
        }
        
        // Template context
        prompt.append("\nTEMPLATE-TYP: ").append(context.getTemplate().getType()).append("\n");
        prompt.append("ZIEL: ").append(request.getObjective()).append("\n");
        
        // Specific instructions
        prompt.append("\nANWEISUNGEN:\n");
        prompt.append("- Verwende einen professionellen, aber persönlichen Ton\n");
        prompt.append("- Beziehe die Kundenhistorie subtil ein\n");
        prompt.append("- Fokussiere auf den spezifischen Mehrwert für diesen Kunden\n");
        prompt.append("- Halte die Länge angemessen (max 200 Wörter)\n");
        
        return prompt.toString();
    }
    
    private CompletionStage<ProcessedTemplate> processTemplateVariables(TemplateContext context) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Object> variables = new HashMap<>();
            
            // Customer variables
            variables.put("customer.name", context.getCustomer().getName());
            variables.put("customer.company", context.getCustomer().getCompanyName());
            variables.put("customer.industry", context.getCustomer().getIndustry());
            
            // AI-generated content
            variables.put("ai.personalizedContent", context.getAIContent());
            variables.put("ai.suggestions", context.getAISuggestions());
            
            // Dynamic calculations
            variables.put("calc.estimatedValue", calculateEstimatedValue(context));
            variables.put("calc.timeframe", calculateTimeframe(context));
            
            // Date/time variables
            variables.put("date.today", LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            variables.put("date.nextWeek", LocalDate.now().plusWeeks(1).format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            
            return ProcessedTemplate.builder()
                .templateContent(context.getTemplate().getContent())
                .variables(variables)
                .metadata(context.getTemplate().getMetadata())
                .build();
        });
    }
}
```

#### 2. Visual Template Builder:
```java
@ApplicationScoped
public class TemplateBuilderService {
    
    @Inject
    TemplateRepository templateRepository;
    
    @Inject
    ComponentLibraryService componentLibrary;
    
    public TemplateBuilder createBuilder() {
        return new TemplateBuilder(componentLibrary.getAvailableComponents());
    }
    
    public Template saveTemplate(TemplateDefinition definition) {
        // Validate template structure
        validateTemplateStructure(definition);
        
        // Generate template metadata
        TemplateMetadata metadata = generateMetadata(definition);
        
        // Create template entity
        Template template = Template.builder()
            .name(definition.getName())
            .type(definition.getType())
            .content(definition.getContent())
            .variables(extractVariables(definition))
            .metadata(metadata)
            .version(1)
            .status(TemplateStatus.DRAFT)
            .createdAt(Instant.now())
            .build();
            
        return templateRepository.save(template);
    }
    
    private void validateTemplateStructure(TemplateDefinition definition) {
        // Check for required fields
        if (definition.getName() == null || definition.getName().isBlank()) {
            throw new ValidationException("Template name is required");
        }
        
        // Validate variable syntax
        validateVariableSyntax(definition.getContent());
        
        // Check component compatibility
        validateComponentUsage(definition.getComponents());
    }
    
    private List<TemplateVariable> extractVariables(TemplateDefinition definition) {
        List<TemplateVariable> variables = new ArrayList<>();
        
        // Extract variables from content using regex
        Pattern variablePattern = Pattern.compile("\\{\\{([^}]+)\\}\\}");
        Matcher matcher = variablePattern.matcher(definition.getContent());
        
        while (matcher.find()) {
            String variableName = matcher.group(1).trim();
            VariableType type = inferVariableType(variableName);
            
            variables.add(TemplateVariable.builder()
                .name(variableName)
                .type(type)
                .required(isRequiredVariable(variableName))
                .defaultValue(getDefaultValue(variableName, type))
                .build());
        }
        
        return variables;
    }
    
    private VariableType inferVariableType(String variableName) {
        // Smart type inference based on variable name patterns
        if (variableName.startsWith("date.")) return VariableType.DATE;
        if (variableName.startsWith("calc.")) return VariableType.CALCULATED;
        if (variableName.startsWith("ai.")) return VariableType.AI_GENERATED;
        if (variableName.contains("amount") || variableName.contains("price")) return VariableType.CURRENCY;
        if (variableName.contains("count") || variableName.contains("number")) return VariableType.NUMBER;
        
        return VariableType.TEXT;
    }
}
```

#### 3. Multi-Format Document Generator:
```java
@ApplicationScoped
public class DocumentGenerationService {
    
    @Inject
    PDFGenerator pdfGenerator;
    
    @Inject
    WordGenerator wordGenerator;
    
    @Inject
    HTMLGenerator htmlGenerator;
    
    @Inject
    EmailTemplateGenerator emailGenerator;
    
    public CompletionStage<GeneratedDocument> generateDocument(
            ProcessedTemplate template, 
            DocumentFormat format,
            GenerationOptions options) {
        
        return switch (format) {
            case PDF -> generatePDF(template, options);
            case DOCX -> generateWord(template, options);
            case HTML -> generateHTML(template, options);
            case EMAIL -> generateEmail(template, options);
        };
    }
    
    private CompletionStage<GeneratedDocument> generatePDF(
            ProcessedTemplate template, 
            GenerationOptions options) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Create PDF with iText 7
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                PdfWriter writer = new PdfWriter(outputStream);
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf);
                
                // Apply custom styling
                applyPDFStyling(document, options.getStyling());
                
                // Process template content
                String processedContent = processTemplateContent(template);
                
                // Parse and add content
                addContentToPDF(document, processedContent, template.getVariables());
                
                document.close();
                
                return GeneratedDocument.builder()
                    .format(DocumentFormat.PDF)
                    .content(outputStream.toByteArray())
                    .filename(generateFilename(template, "pdf"))
                    .mimeType("application/pdf")
                    .generatedAt(Instant.now())
                    .build();
                    
            } catch (Exception e) {
                throw new DocumentGenerationException("Failed to generate PDF", e);
            }
        });
    }
    
    private void applyPDFStyling(Document document, StylingOptions styling) {
        // Company branding
        if (styling.includeLogo()) {
            addCompanyLogo(document, styling.getLogoPath());
        }
        
        // Color scheme
        DeviceRgb primaryColor = new DeviceRgb(
            styling.getPrimaryColor().getRed(),
            styling.getPrimaryColor().getGreen(),
            styling.getPrimaryColor().getBlue()
        );
        
        // Fonts
        PdfFont headerFont = PdfFontFactory.createFont(
            styling.getHeaderFontPath(), 
            PdfEncodings.IDENTITY_H
        );
        
        // Apply to document...
    }
    
    private CompletionStage<GeneratedDocument> generateEmail(
            ProcessedTemplate template, 
            GenerationOptions options) {
        
        return CompletableFuture.supplyAsync(() -> {
            // Generate HTML email with responsive design
            String emailHTML = generateResponsiveEmailHTML(template);
            
            // Generate plain text fallback
            String emailText = generatePlainTextVersion(template);
            
            return GeneratedDocument.builder()
                .format(DocumentFormat.EMAIL)
                .content(emailHTML.getBytes(StandardCharsets.UTF_8))
                .plainTextContent(emailText)
                .filename(generateFilename(template, "html"))
                .mimeType("text/html")
                .generatedAt(Instant.now())
                .build();
        });
    }
}
```

### 🎨 Frontend Template Builder:

#### 1. Visual Template Editor:
```typescript
export const TemplateBuilder: React.FC = () => {
  const [template, setTemplate] = useState<TemplateDefinition>(createEmptyTemplate());
  const [selectedComponent, setSelectedComponent] = useState<string | null>(null);
  const [previewMode, setPreviewMode] = useState<'edit' | 'preview'>('edit');

  const { data: availableComponents } = useQuery({
    queryKey: ['template-components'],
    queryFn: () => templateApi.getAvailableComponents()
  });

  const { mutate: saveTemplate } = useMutation({
    mutationFn: templateApi.saveTemplate,
    onSuccess: () => {
      toast.success('Template erfolgreich gespeichert');
    }
  });

  const handleComponentDrop = useCallback((component: TemplateComponent, position: number) => {
    setTemplate(prev => ({
      ...prev,
      components: [
        ...prev.components.slice(0, position),
        { ...component, id: generateId() },
        ...prev.components.slice(position)
      ]
    }));
  }, []);

  const handleVariableAdd = useCallback((variableName: string, type: VariableType) => {
    setTemplate(prev => ({
      ...prev,
      variables: [
        ...prev.variables,
        {
          name: variableName,
          type,
          required: false,
          defaultValue: getDefaultValueForType(type)
        }
      ]
    }));
  }, []);

  return (
    <Container maxWidth="xl" sx={{ py: 3 }}>
      <Box sx={{ display: 'flex', gap: 3 }}>
        {/* Component Library */}
        <Paper sx={{ width: 300, p: 2 }}>
          <Typography 
            variant="h6" 
            fontFamily="Antonio Bold"
            gutterBottom
            sx={{ color: '#004F7B' }}
          >
            Komponenten
          </Typography>
          
          <Stack spacing={1}>
            {availableComponents?.map((component) => (
              <DraggableComponent
                key={component.type}
                component={component}
                onDragEnd={(draggedComponent) => {
                  // Handle drop on main canvas
                }}
              />
            ))}
          </Stack>

          <Divider sx={{ my: 2 }} />

          <Typography variant="h6" gutterBottom>
            Variablen
          </Typography>
          
          <VariableManager
            variables={template.variables}
            onAddVariable={handleVariableAdd}
            onUpdateVariable={(index, variable) => {
              setTemplate(prev => ({
                ...prev,
                variables: prev.variables.map((v, i) => i === index ? variable : v)
              }));
            }}
          />
        </Paper>

        {/* Main Editor */}
        <Box sx={{ flexGrow: 1 }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
            <TextField
              label="Template Name"
              value={template.name}
              onChange={(e) => setTemplate(prev => ({ ...prev, name: e.target.value }))}
              sx={{ width: 300 }}
            />
            
            <Box sx={{ display: 'flex', gap: 1 }}>
              <ToggleButtonGroup
                value={previewMode}
                exclusive
                onChange={(_, mode) => mode && setPreviewMode(mode)}
              >
                <ToggleButton value="edit">
                  <EditIcon />
                  Bearbeiten
                </ToggleButton>
                <ToggleButton value="preview">
                  <PreviewIcon />
                  Vorschau
                </ToggleButton>
              </ToggleButtonGroup>
              
              <Button
                variant="contained"
                startIcon={<SaveIcon />}
                onClick={() => saveTemplate(template)}
                sx={{ bgcolor: '#94C456' }}
              >
                Speichern
              </Button>
            </Box>
          </Box>

          {previewMode === 'edit' ? (
            <TemplateCanvas
              template={template}
              onComponentAdd={handleComponentDrop}
              onComponentUpdate={(componentId, updates) => {
                setTemplate(prev => ({
                  ...prev,
                  components: prev.components.map(c => 
                    c.id === componentId ? { ...c, ...updates } : c
                  )
                }));
              }}
              onComponentDelete={(componentId) => {
                setTemplate(prev => ({
                  ...prev,
                  components: prev.components.filter(c => c.id !== componentId)
                }));
              }}
              selectedComponent={selectedComponent}
              onComponentSelect={setSelectedComponent}
            />
          ) : (
            <TemplatePreview
              template={template}
              sampleData={generateSampleData(template.variables)}
            />
          )}
        </Box>

        {/* Properties Panel */}
        {selectedComponent && (
          <Paper sx={{ width: 300, p: 2 }}>
            <Typography variant="h6" gutterBottom>
              Eigenschaften
            </Typography>
            
            <ComponentPropertiesEditor
              component={template.components.find(c => c.id === selectedComponent)}
              onUpdate={(updates) => {
                setTemplate(prev => ({
                  ...prev,
                  components: prev.components.map(c => 
                    c.id === selectedComponent ? { ...c, ...updates } : c
                  )
                }));
              }}
              availableVariables={template.variables}
            />
          </Paper>
        )}
      </Box>
    </Container>
  );
};
```

#### 2. AI Content Generator Component:
```typescript
export const AIContentGenerator: React.FC<{
  onContentGenerated: (content: string) => void;
  customerContext?: Customer;
  templateType: TemplateType;
}> = ({ onContentGenerated, customerContext, templateType }) => {
  const [prompt, setPrompt] = useState('');
  const [isGenerating, setIsGenerating] = useState(false);
  const [generatedOptions, setGeneratedOptions] = useState<string[]>([]);

  const { mutate: generateContent } = useMutation({
    mutationFn: async (request: AIGenerationRequest) => {
      setIsGenerating(true);
      return templateApi.generateAIContent(request);
    },
    onSuccess: (response) => {
      setGeneratedOptions(response.options);
      setIsGenerating(false);
    },
    onError: () => {
      setIsGenerating(false);
      toast.error('Content-Generierung fehlgeschlagen');
    }
  });

  const handleGenerate = () => {
    if (!prompt.trim()) return;

    generateContent({
      prompt,
      templateType,
      customerContext: customerContext ? {
        name: customerContext.name,
        industry: customerContext.industry,
        size: customerContext.size,
        recentInteractions: customerContext.recentInteractions?.slice(0, 5)
      } : undefined,
      options: {
        tone: 'professional',
        length: 'medium',
        language: 'de'
      }
    });
  };

  return (
    <Card sx={{ p: 3 }}>
      <Typography 
        variant="h6" 
        fontFamily="Antonio Bold"
        gutterBottom
        sx={{ color: '#004F7B' }}
      >
        🤖 KI Content-Generator
      </Typography>

      <Stack spacing={2}>
        <TextField
          label="Beschreiben Sie den gewünschten Content"
          multiline
          rows={3}
          value={prompt}
          onChange={(e) => setPrompt(e.target.value)}
          placeholder="z.B. 'Erstelle eine persönliche Begrüßung für einen Kunden aus der Automobilindustrie, der bereits Interesse an unseren Services gezeigt hat'"
          fullWidth
        />

        {customerContext && (
          <Alert icon={<PersonIcon />} severity="info">
            <AlertTitle>Kundenkontext erkannt</AlertTitle>
            Der Content wird personalisiert für <strong>{customerContext.name}</strong> 
            ({customerContext.industry}) generiert.
          </Alert>
        )}

        <Button
          variant="contained"
          onClick={handleGenerate}
          disabled={!prompt.trim() || isGenerating}
          startIcon={isGenerating ? <CircularProgress size={16} /> : <AutoAwesomeIcon />}
          sx={{ bgcolor: '#94C456', alignSelf: 'flex-start' }}
        >
          {isGenerating ? 'Generiert...' : 'Content generieren'}
        </Button>

        {generatedOptions.length > 0 && (
          <Box>
            <Typography variant="subtitle2" gutterBottom>
              Generierte Optionen:
            </Typography>
            
            <Stack spacing={1}>
              {generatedOptions.map((option, index) => (
                <Paper 
                  key={index}
                  sx={{ 
                    p: 2, 
                    cursor: 'pointer',
                    border: '1px solid transparent',
                    '&:hover': {
                      borderColor: '#94C456',
                      bgcolor: 'rgba(148, 196, 86, 0.05)'
                    }
                  }}
                  onClick={() => onContentGenerated(option)}
                >
                  <Typography variant="body2">
                    {option}
                  </Typography>
                  
                  <Box sx={{ display: 'flex', justifyContent: 'flex-end', mt: 1 }}>
                    <Button 
                      size="small" 
                      startIcon={<CheckIcon />}
                      sx={{ color: '#94C456' }}
                    >
                      Verwenden
                    </Button>
                  </Box>
                </Paper>
              ))}
            </Stack>
          </Box>
        )}
      </Stack>
    </Card>
  );
};
```

---

## 📊 IMPLEMENTIERUNGSPLAN

### Phase 1: Template Engine Core (1 Tag)
1. **Template Repository**: CRUD Operations + Version Management
2. **Variable System**: Type Inference + Validation + Default Values
3. **Basic Rendering**: Text Substitution + Conditional Logic

### Phase 2: AI Integration (1 Tag)
1. **OpenAI Service**: GPT-4 Integration + Prompt Engineering
2. **Context Builder**: Customer Data + Historical Insights
3. **Content Generation**: Personalization + Multiple Options

### Phase 3: Visual Builder (1 Tag)
1. **Drag & Drop Editor**: Component Library + Canvas
2. **Property Editor**: Dynamic Forms + Validation
3. **Live Preview**: Real-time Rendering + Sample Data

### Phase 4: Multi-Format Export (1 Tag)
1. **PDF Generation**: iText 7 + Custom Styling
2. **Word Export**: Apache POI + Template Processing
3. **Email Templates**: Responsive HTML + Plain Text Fallback

---

## 🎯 BUSINESS VALUE

### ROI Metriken:
- **90% Zeitersparnis** bei Dokumentenerstellung durch automatische Content-Generation
- **40% höhere Response-Rate** durch personalisierte, datengetriebene Templates
- **70% weniger Fehler** durch automatische Datenvalidierung und Template-Konsistenz
- **Break-even nach 2 Monaten** durch drastisch reduzierte Dokumentations-Zeiten

### Technical Benefits:
- **Personalization at Scale**: KI-gesteuerte Anpassung an jeden Kunden
- **Visual Template Creation**: Drag & Drop Interface für Non-Technical Users
- **Multi-Format Support**: Ein Template → Alle Ausgabeformate
- **Data Integration**: Nahtlose CRM-Anbindung + Dynamic Content

---

## 🔗 INTEGRATION POINTS

### Dependencies:
- **FC-034 Instant Insights**: KI-Service für Content-Generation (Recommended)
- **FC-008 Security Foundation**: User Context + Permission Management (Required)

### Enables:
- **Sales Process Automation**: Schnellere Angebotserstellung + Personalisierte Kommunikation
- **FC-029 Voice First**: Voice-to-Template Commands + Audio Content Generation
- **FC-037 Advanced Reporting**: Template-basierte Report Generation

---

## ⚠️ WICHTIGE ENTSCHEIDUNGEN

1. **OpenAI GPT-4 für Content-Generation**: Beste Qualität für deutsche Business-Texte
2. **iText 7 für PDF**: Enterprise-Grade PDF-Generation mit Corporate Branding
3. **Visual Editor statt Code**: Drag & Drop Interface für Business Users
4. **Multi-Tenant Template Library**: Shared Templates + Custom Tenant Templates

---

**Status:** Ready for Implementation | **Phase 1:** Template Engine + AI Integration | **Next:** Visual Builder Development