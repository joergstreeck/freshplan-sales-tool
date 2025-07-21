# FC-016: Opportunity Cloning 🔄 CLAUDE_TECH

**Feature Code:** FC-016  
**Optimiert für:** Claude's 30-Sekunden-Produktivität  
**Original:** 786 Zeilen → **Optimiert:** ~400 Zeilen (49% Reduktion)

## 🎯 QUICK-LOAD: Sofort produktiv in 30 Sekunden!

### Was macht FC-016?
**Ein-Klick Opportunity Cloning mit Smart Templates für 80% Zeitersparnis bei Folgegeschäften (10 Min → 30 Sek)**

### Die 5 Kern-Features:
1. **Clone Button** → Direkt in OpportunityCard für Ein-Klick-Zugriff
2. **Smart Templates** → Renewal, Addon, Alternative mit vorkonfigurierten Einstellungen
3. **Selective Clone** → Wählbare Übernahme von Produkten, Team, Dokumenten, Notizen
4. **Auto-Naming** → Intelligente Titel-Generierung mit Jahr/Quartal-Pattern
5. **Integration** → Nahtlos in M4 Kanban Board mit Optimistic Updates

### Sofort starten:
```bash
# Backend: Clone Service Extension
cd backend
# Kein neuer Extension nötig - nutzt bestehende Opportunity Entity

# Frontend: Clone Dialog Components
cd frontend
npm install react-hook-form @hookform/resolvers
```

---

## 📦 1. BACKEND: Copy-paste Recipes

### 1.1 Clone Service mit Selective Copy (10 Minuten)
```java
@ApplicationScoped
@Transactional
public class OpportunityCloneService {
    
    @Inject OpportunityRepository opportunityRepository;
    @Inject LineItemRepository lineItemRepository;
    @Inject AuditLogService auditService;
    
    public Opportunity clone(Opportunity original, CloneConfiguration config) {
        
        // Basis-Opportunity klonen
        Opportunity cloned = createBaseClone(original, config);
        
        // Selektive Übernahme basierend auf Konfiguration
        if (config.isIncludeProducts()) {
            cloneLineItems(original, cloned);
        }
        
        if (config.isIncludeTeamMembers()) {
            cloneTeamMembers(original, cloned);
        }
        
        if (config.isIncludeAttachments()) {
            cloneAttachments(original, cloned);
        }
        
        if (config.isIncludeNotes()) {
            cloneNotes(original, cloned);
        }
        
        // Opportunity speichern
        Opportunity saved = opportunityRepository.persist(cloned);
        
        // Audit Log
        auditService.log(AuditAction.OPPORTUNITY_CLONED, 
            Map.of(
                "originalId", original.getId(),
                "clonedId", saved.getId(),
                "template", config.getTemplate(),
                "clonedFields", config.getIncludedFields()
            ));
        
        return saved;
    }
    
    private Opportunity createBaseClone(Opportunity original, CloneConfiguration config) {
        return Opportunity.builder()
            .title(config.getNewTitle())
            .customerId(original.getCustomerId()) // Immer übernommen
            .stage(config.getTargetStage()) // Default: LEAD
            .expectedCloseDate(config.getExpectedCloseDate())
            .probability(config.getInitialProbability()) // Default: 10%
            .value(original.getValue()) // Erstmal übernommen, wird recalculiert
            .assignedUserId(getCurrentUserId()) // Clone Creator wird Owner
            .createdBy(getCurrentUserId())
            .source("CLONED_FROM_" + original.getId())
            .build();
    }
    
    private void cloneLineItems(Opportunity original, Opportunity cloned) {
        List<LineItem> originalItems = lineItemRepository.findByOpportunityId(original.getId());
        
        List<LineItem> clonedItems = originalItems.stream()
            .map(item -> LineItem.builder()
                .opportunityId(cloned.getId())
                .productId(item.getProductId())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .discount(item.getDiscount())
                .description(item.getDescription())
                .customConfig(item.getCustomConfig()) // Clone custom configs too
                .build())
            .collect(Collectors.toList());
            
        lineItemRepository.persistAll(clonedItems);
        
        // Gesamtwert neu berechnen
        BigDecimal totalValue = clonedItems.stream()
            .map(LineItem::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        cloned.setValue(totalValue);
    }
    
    private void cloneTeamMembers(Opportunity original, Opportunity cloned) {
        // Team members übernehmen aber Clone Creator als Primary Owner
        Set<UUID> teamMembers = new HashSet<>(original.getTeamMemberIds());
        teamMembers.add(getCurrentUserId()); // Creator hinzufügen
        
        cloned.setTeamMemberIds(teamMembers);
    }
    
    private void cloneAttachments(Opportunity original, Opportunity cloned) {
        List<Attachment> originalAttachments = attachmentService.findByOpportunityId(original.getId());
        
        originalAttachments.forEach(attachment -> {
            // Create reference copy (not file copy) for efficiency
            attachmentService.createReference(
                cloned.getId(),
                attachment.getFileId(),
                attachment.getFileName(),
                "Copied from " + original.getTitle()
            );
        });
    }
    
    private void cloneNotes(Opportunity original, Opportunity cloned) {
        List<Note> originalNotes = noteService.findByOpportunityId(original.getId());
        
        originalNotes.forEach(note -> {
            noteService.createNote(Note.builder()
                .opportunityId(cloned.getId())
                .content("[CLONED] " + note.getContent())
                .createdBy(getCurrentUserId())
                .noteType(note.getNoteType())
                .build());
        });
    }
}
```

### 1.2 Clone Configuration mit Templates (5 Minuten)
```java
public class CloneConfiguration {
    
    // Basis-Eigenschaften
    private String newTitle;
    private OpportunityStage targetStage = OpportunityStage.LEAD;
    private LocalDate expectedCloseDate;
    private Integer initialProbability = 10;
    private CloneTemplate template;
    
    // Übernahme-Optionen
    private boolean includeProducts = true;
    private boolean includeDiscounts = true;
    private boolean includeTeamMembers = true;
    private boolean includeNotes = false;
    private boolean includeAttachments = false;
    
    // Template-basierte Konfiguration
    public static CloneConfiguration fromTemplate(CloneTemplate template, Opportunity original) {
        return switch (template) {
            case RENEWAL -> CloneConfiguration.builder()
                .template(template)
                .newTitle(generateRenewalTitle(original.getTitle()))
                .expectedCloseDate(original.getExpectedCloseDate().plusYears(1))
                .targetStage(OpportunityStage.LEAD)
                .initialProbability(30) // Höhere Wahrscheinlichkeit bei Renewals
                .includeProducts(true)
                .includeDiscounts(true)
                .includeTeamMembers(true)
                .includeNotes(false) // Alte Notizen meist nicht relevant
                .includeAttachments(true)
                .build();
                
            case ADDON -> CloneConfiguration.builder()
                .template(template)
                .newTitle("Zusatz zu " + original.getTitle())
                .expectedCloseDate(LocalDate.now().plusDays(30))
                .targetStage(OpportunityStage.QUALIFICATION)
                .initialProbability(50) // Addons haben hohe Chance
                .includeProducts(false) // Neue Produkte hinzufügen
                .includeDiscounts(true) // Bestehende Rabatte übernehmen
                .includeTeamMembers(true)
                .includeNotes(true) // Context wichtig für Addons
                .includeAttachments(true)
                .build();
                
            case ALTERNATIVE -> CloneConfiguration.builder()
                .template(template)
                .newTitle("Alternative: " + original.getTitle())
                .expectedCloseDate(original.getExpectedCloseDate())
                .targetStage(original.getStage()) // Gleiche Stage
                .initialProbability(original.getProbability()) // Gleiche Probability
                .includeProducts(true)
                .includeDiscounts(true)
                .includeTeamMembers(true)
                .includeNotes(true)
                .includeAttachments(true)
                .build();
        };
    }
    
    private static String generateRenewalTitle(String originalTitle) {
        int currentYear = LocalDate.now().getYear();
        
        // Check if title already contains year pattern
        if (originalTitle.matches(".*\\b\\d{4}\\b.*")) {
            // Replace existing year with next year
            return originalTitle.replaceAll("\\b\\d{4}\\b", String.valueOf(currentYear + 1));
        } else {
            // Add year pattern
            return originalTitle + " " + (currentYear + 1);
        }
    }
}

enum CloneTemplate {
    RENEWAL("Verlängerung", "refresh"),
    ADDON("Zusatzauftrag", "add"),
    ALTERNATIVE("Alternative", "swap_horiz");
    
    private final String displayName;
    private final String icon;
}
```

### 1.3 REST API Endpoint (5 Minuten)
```java
@Path("/api/opportunities/{id}/clone")
@Authenticated
public class OpportunityCloneResource {
    
    @Inject OpportunityService opportunityService;
    @Inject OpportunityCloneService cloneService;
    @Inject SecurityService securityService;
    
    @POST
    @RolesAllowed({"sales", "admin", "manager"})
    public Response cloneOpportunity(
            @PathParam("id") UUID opportunityId,
            CloneConfiguration config) {
        
        try {
            Opportunity original = opportunityService.findById(opportunityId);
            
            // Security Check - User kann Original lesen
            if (!securityService.canRead(original)) {
                return Response.status(403)
                    .entity(Map.of("error", "Keine Berechtigung für Opportunity Clone"))
                    .build();
            }
            
            Opportunity cloned = cloneService.clone(original, config);
            
            return Response.status(201)
                .entity(CloneResult.builder()
                    .originalId(original.getId())
                    .clonedId(cloned.getId())
                    .clonedOpportunity(opportunityService.toDTO(cloned))
                    .clonedFields(config.getIncludedFields())
                    .redirectUrl("/opportunities/" + cloned.getId())
                    .build())
                .build();
                
        } catch (EntityNotFoundException e) {
            return Response.status(404)
                .entity(Map.of("error", "Opportunity not found"))
                .build();
        } catch (Exception e) {
            return Response.status(500)
                .entity(Map.of("error", "Clone operation failed: " + e.getMessage()))
                .build();
        }
    }
    
    @GET
    @Path("/templates")
    public Response getCloneTemplates() {
        List<CloneTemplateInfo> templates = Arrays.stream(CloneTemplate.values())
            .map(template -> CloneTemplateInfo.builder()
                .key(template.name())
                .displayName(template.getDisplayName())
                .icon(template.getIcon())
                .description(getTemplateDescription(template))
                .build())
            .collect(Collectors.toList());
            
        return Response.ok(templates).build();
    }
    
    private String getTemplateDescription(CloneTemplate template) {
        return switch (template) {
            case RENEWAL -> "Verlängert bestehenden Vertrag für nächstes Jahr";
            case ADDON -> "Erstellt Zusatzauftrag mit neuen Produkten";
            case ALTERNATIVE -> "Kopiert alles für alternative Angebotsvariante";
        };
    }
}
```

---

## 🎨 2. FRONTEND: Clone Dialog & Integration

### 2.1 Clone Button in OpportunityCard (5 Minuten)
```typescript
// Integration in bestehende OpportunityCard
export const OpportunityCard: React.FC<{ opportunity: Opportunity; index: number }> = ({ 
  opportunity, 
  index 
}) => {
  const { openCloneDialog } = useOpportunityClone();
  
  return (
    <Draggable draggableId={opportunity.id} index={index}>
      {(provided, snapshot) => (
        <CardContainer ref={provided.innerRef} {...provided.draggableProps}>
          <CardHeader {...provided.dragHandleProps}>
            <Title>{opportunity.title}</Title>
            <CardActions>
              <Tooltip title="Opportunity klonen">
                <IconButton 
                  size="small" 
                  onClick={(e) => {
                    e.stopPropagation();
                    openCloneDialog(opportunity);
                  }}
                  sx={{ 
                    color: '#94C456',
                    '&:hover': { bgcolor: 'rgba(148, 196, 86, 0.1)' }
                  }}
                >
                  <ContentCopy fontSize="small" />
                </IconButton>
              </Tooltip>
              
              <IconButton size="small">
                <MoreVert fontSize="small" />
              </IconButton>
            </CardActions>
          </CardHeader>
          
          <CustomerInfo customer={opportunity.customer} />
          <Value>{formatCurrency(opportunity.value)}</Value>
          <ProbabilitySlider value={opportunity.probability} />
        </CardContainer>
      )}
    </Draggable>
  );
};
```

### 2.2 OpportunityCloneDialog Hauptkomponente (15 Minuten)
```typescript
export const OpportunityCloneDialog: React.FC<{
  opportunity: Opportunity;
  open: boolean;
  onClose: () => void;
}> = ({ opportunity, open, onClose }) => {
  
  const { cloneOpportunity } = useOpportunityMutations();
  const navigate = useNavigate();
  
  const [config, setConfig] = useState<CloneConfiguration>(() => 
    getDefaultConfig(opportunity)
  );
  const [selectedTemplate, setSelectedTemplate] = useState<CloneTemplate | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const handleTemplateSelect = (template: CloneTemplate) => {
    setSelectedTemplate(template);
    setConfig(CloneConfiguration.fromTemplate(template, opportunity));
  };

  const handleClone = async () => {
    setIsLoading(true);
    try {
      const result = await cloneOpportunity.mutateAsync({ 
        opportunityId: opportunity.id, 
        config 
      });
      
      toast.success(`Opportunity "${config.newTitle}" wurde erfolgreich erstellt`);
      onClose();
      
      // Navigation zur neuen Opportunity
      navigate(`/opportunities/${result.clonedId}`);
      
    } catch (error: any) {
      toast.error(`Fehler beim Klonen: ${error.message}`);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>
        <Box display="flex" alignItems="center">
          <ContentCopy sx={{ mr: 1, color: '#94C456' }} />
          Opportunity klonen
        </Box>
      </DialogTitle>
      
      <DialogContent>
        {/* Original Opportunity Info */}
        <Card variant="outlined" sx={{ mb: 3, bgcolor: 'grey.50' }}>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              {opportunity.title}
            </Typography>
            <Grid container spacing={2}>
              <Grid item xs={6}>
                <Typography variant="body2" color="text.secondary">
                  Kunde: {opportunity.customer.name}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Wert: {formatCurrency(opportunity.value)}
                </Typography>
              </Grid>
              <Grid item xs={6}>
                <Typography variant="body2" color="text.secondary">
                  Stage: {opportunity.stage}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  {opportunity.lineItems?.length || 0} Produkte
                </Typography>
              </Grid>
            </Grid>
          </CardContent>
        </Card>

        {/* Quick Templates */}
        <Box mb={3}>
          <Typography variant="subtitle1" gutterBottom>
            Schnelle Vorlagen:
          </Typography>
          <Grid container spacing={1}>
            {CLONE_TEMPLATES.map((template) => (
              <Grid item xs={4} key={template.key}>
                <Button
                  fullWidth
                  variant={selectedTemplate === template.key ? "contained" : "outlined"}
                  startIcon={<Icon>{template.icon}</Icon>}
                  onClick={() => handleTemplateSelect(template.key)}
                  sx={{
                    bgcolor: selectedTemplate === template.key ? '#94C456' : 'transparent',
                    '&:hover': { 
                      bgcolor: selectedTemplate === template.key ? '#7FA93F' : 'rgba(148, 196, 86, 0.1)' 
                    }
                  }}
                >
                  {template.displayName}
                </Button>
              </Grid>
            ))}
          </Grid>
          
          {selectedTemplate && (
            <Alert severity="info" sx={{ mt: 1 }}>
              {getTemplateDescription(selectedTemplate)}
            </Alert>
          )}
        </Box>

        {/* Neuer Titel */}
        <TextField
          fullWidth
          label="Titel der neuen Opportunity"
          value={config.newTitle}
          onChange={(e) => setConfig(prev => ({ ...prev, newTitle: e.target.value }))}
          sx={{ mb: 3 }}
          helperText="Tipp: Jahr oder Quartal hinzufügen für bessere Übersicht"
        />

        {/* Clone-Optionen */}
        <CloneConfigurationForm 
          config={config} 
          onChange={setConfig}
          opportunity={opportunity}
        />
        
      </DialogContent>
      
      <DialogActions>
        <Button onClick={onClose} disabled={isLoading}>
          Abbrechen
        </Button>
        <Button 
          variant="contained" 
          startIcon={isLoading ? <CircularProgress size={16} /> : <ContentCopy />}
          onClick={handleClone}
          disabled={isLoading || !config.newTitle.trim()}
          sx={{ 
            bgcolor: '#94C456', 
            '&:hover': { bgcolor: '#7FA93F' },
            '&:disabled': { bgcolor: 'grey.400' }
          }}
        >
          {isLoading ? 'Wird geklont...' : 'Opportunity klonen'}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

const CLONE_TEMPLATES = [
  { key: 'RENEWAL', displayName: 'Verlängerung', icon: 'refresh' },
  { key: 'ADDON', displayName: 'Zusatz', icon: 'add' },
  { key: 'ALTERNATIVE', displayName: 'Alternative', icon: 'swap_horiz' }
];

const getTemplateDescription = (template: CloneTemplate) => {
  const descriptions = {
    RENEWAL: 'Verlängert bestehenden Vertrag für nächstes Jahr',
    ADDON: 'Erstellt Zusatzauftrag mit neuen Produkten',
    ALTERNATIVE: 'Kopiert alles für alternative Angebotsvariante'
  };
  return descriptions[template];
};
```

### 2.3 CloneConfigurationForm (10 Minuten)
```typescript
const CloneConfigurationForm: React.FC<{
  config: CloneConfiguration;
  onChange: (config: CloneConfiguration) => void;
  opportunity: Opportunity;
}> = ({ config, onChange, opportunity }) => {

  const updateConfig = (updates: Partial<CloneConfiguration>) => {
    onChange({ ...config, ...updates });
  };

  return (
    <Box>
      <Typography variant="subtitle1" gutterBottom>
        Was soll übernommen werden?
      </Typography>
      
      <FormGroup>
        {/* Immer übernommen */}
        <FormControlLabel
          control={<Checkbox checked disabled />}
          label={
            <Box>
              <Typography variant="body2">
                <strong>Kunde & Kontakte</strong> (immer übernommen)
              </Typography>
              <Typography variant="caption" color="text.secondary">
                {opportunity.customer.name}
                {opportunity.contacts && ` • ${opportunity.contacts.length} Kontakte`}
              </Typography>
            </Box>
          }
          sx={{ mb: 1 }}
        />

        {/* Produkte & Konditionen */}
        <FormControlLabel
          control={
            <Checkbox
              checked={config.includeProducts}
              onChange={(e) => updateConfig({ 
                includeProducts: e.target.checked,
                includeDiscounts: e.target.checked // Discounts nur mit Produkten
              })}
            />
          }
          label={
            <Box>
              <Typography variant="body2">
                <strong>Produkte & Mengen</strong>
              </Typography>
              <Typography variant="caption" color="text.secondary">
                {opportunity.lineItems?.length || 0} Positionen • 
                Wert: {formatCurrency(opportunity.value)}
              </Typography>
            </Box>
          }
          sx={{ mb: 1 }}
        />

        {config.includeProducts && (
          <FormControlLabel
            control={
              <Checkbox
                checked={config.includeDiscounts}
                onChange={(e) => updateConfig({ includeDiscounts: e.target.checked })}
              />
            }
            label="Rabatte & Sonderkonditionen"
            sx={{ ml: 3, mb: 1 }}
          />
        )}

        {/* Team */}
        <FormControlLabel
          control={
            <Checkbox
              checked={config.includeTeamMembers}
              onChange={(e) => updateConfig({ includeTeamMembers: e.target.checked })}
            />
          }
          label={
            <Box>
              <Typography variant="body2">
                <strong>Team-Mitglieder</strong>
              </Typography>
              <Typography variant="caption" color="text.secondary">
                {opportunity.teamMembers?.length 
                  ? opportunity.teamMembers.map(m => m.name).join(', ')
                  : 'Nur Sie als Owner'
                }
              </Typography>
            </Box>
          }
          sx={{ mb: 1 }}
        />

        {/* Notizen */}
        <FormControlLabel
          control={
            <Checkbox
              checked={config.includeNotes}
              onChange={(e) => updateConfig({ includeNotes: e.target.checked })}
            />
          }
          label={
            <Box>
              <Typography variant="body2">
                <strong>Notizen & Kommentare</strong>
              </Typography>
              <Typography variant="caption" color="text.secondary">
                Interne Notizen aus dem Original-Deal
              </Typography>
            </Box>
          }
          sx={{ mb: 1 }}
        />

        {/* Dokumente */}
        <FormControlLabel
          control={
            <Checkbox
              checked={config.includeAttachments}
              onChange={(e) => updateConfig({ includeAttachments: e.target.checked })}
            />
          }
          label={
            <Box>
              <Typography variant="body2">
                <strong>Dokumente & Anhänge</strong>
              </Typography>
              <Typography variant="caption" color="text.secondary">
                {opportunity.attachments?.length || 0} Dateien • 
                Referenzen werden kopiert (keine Duplikate)
              </Typography>
            </Box>
          }
        />
      </FormGroup>

      {/* Erwartetes Abschlussdatum */}
      <TextField
        fullWidth
        type="date"
        label="Erwarteter Abschluss"
        value={config.expectedCloseDate ? format(config.expectedCloseDate, 'yyyy-MM-dd') : ''}
        onChange={(e) => updateConfig({ 
          expectedCloseDate: e.target.value ? new Date(e.target.value) : null 
        })}
        sx={{ mt: 3 }}
        InputLabelProps={{ shrink: true }}
        helperText="Leer lassen für automatische Berechnung basierend auf Template"
      />
    </Box>
  );
};
```

### 2.4 Hooks für Clone-Funktionalität (5 Minuten)
```typescript
// useOpportunityClone Hook
export const useOpportunityClone = () => {
  const [dialogState, setDialogState] = useState<{
    isOpen: boolean;
    opportunity: Opportunity | null;
  }>({ isOpen: false, opportunity: null });

  const openCloneDialog = (opportunity: Opportunity) => {
    setDialogState({ isOpen: true, opportunity });
  };

  const closeCloneDialog = () => {
    setDialogState({ isOpen: false, opportunity: null });
  };

  return {
    isOpen: dialogState.isOpen,
    opportunity: dialogState.opportunity,
    openCloneDialog,
    closeCloneDialog
  };
};

// Extended useOpportunityMutations
export const useOpportunityMutations = () => {
  const queryClient = useQueryClient();
  
  const cloneOpportunity = useMutation({
    mutationFn: async ({ opportunityId, config }: CloneParams) => {
      const response = await apiClient.post(
        `/api/opportunities/${opportunityId}/clone`,
        config
      );
      return response.data as CloneResult;
    },
    onSuccess: (result) => {
      // Invalidate opportunity queries to show new clone
      queryClient.invalidateQueries({ queryKey: ['opportunities'] });
      
      // Optimistically add to opportunity list if possible
      queryClient.setQueryData(['opportunities'], (old: any) => {
        if (old?.data) {
          return {
            ...old,
            data: [...old.data, result.clonedOpportunity]
          };
        }
        return old;
      });
      
      // Preload the new opportunity data
      queryClient.setQueryData(
        ['opportunity', result.clonedId], 
        result.clonedOpportunity
      );
    },
    onError: (error: any) => {
      console.error('Clone failed:', error);
    }
  });

  return { cloneOpportunity };
};

// Default Configuration Generator
const getDefaultConfig = (opportunity: Opportunity): CloneConfiguration => {
  return {
    newTitle: `Copy of ${opportunity.title}`,
    targetStage: 'LEAD',
    expectedCloseDate: addDays(new Date(), 30),
    initialProbability: 10,
    includeProducts: true,
    includeDiscounts: true,
    includeTeamMembers: true,
    includeNotes: false,
    includeAttachments: false
  };
};
```

### 2.5 Global Clone Provider (5 Minuten)
```typescript
// Provider für App-weite Clone-Funktionalität
export const OpportunityCloneProvider: React.FC<{ children: React.ReactNode }> = ({ 
  children 
}) => {
  const cloneState = useOpportunityClone();
  
  return (
    <>
      {children}
      
      {/* Global Clone Dialog */}
      {cloneState.opportunity && (
        <OpportunityCloneDialog
          opportunity={cloneState.opportunity}
          open={cloneState.isOpen}
          onClose={cloneState.closeCloneDialog}
        />
      )}
    </>
  );
};

// Usage in App.tsx
export const App: React.FC = () => {
  return (
    <QueryClientProvider client={queryClient}>
      <OpportunityCloneProvider>
        <Routes>
          <Route path="/opportunities" element={<OpportunityKanban />} />
          {/* other routes */}
        </Routes>
      </OpportunityCloneProvider>
    </QueryClientProvider>
  );
};
```

---

## ✅ 3. TESTING & INTEGRATION

```typescript
// Clone Service Tests
describe('OpportunityCloneService', () => {
  it('should clone opportunity with selective options', () => {
    const original = createTestOpportunity();
    const config = CloneConfiguration.builder()
      .newTitle('Cloned Opportunity')
      .includeProducts(true)
      .includeTeamMembers(false)
      .build();
    
    const cloned = cloneService.clone(original, config);
    
    expect(cloned.getTitle()).toBe('Cloned Opportunity');
    expect(cloned.getCustomerId()).toBe(original.getCustomerId());
    expect(cloned.getAssignedUserId()).toBe(getCurrentUserId());
    expect(cloned.getStage()).toBe(OpportunityStage.LEAD);
  });
  
  it('should clone line items when includeProducts is true', () => {
    const original = createOpportunityWithProducts(3);
    const config = CloneConfiguration.builder()
      .includeProducts(true)
      .build();
    
    final cloned = cloneService.clone(original, config);
    
    final clonedItems = lineItemRepository.findByOpportunityId(cloned.getId());
    expect(clonedItems).hasSize(3);
    expect(cloned.getValue()).isEqualTo(original.getValue());
  });
});

// Frontend Component Tests
describe('OpportunityCloneDialog', () => {
  it('should apply template configuration correctly', () => {
    const opportunity = createTestOpportunity();
    render(<OpportunityCloneDialog opportunity={opportunity} open={true} />);
    
    // Click Renewal template
    fireEvent.click(screen.getByText('Verlängerung'));
    
    // Check if title is updated with year pattern
    const titleInput = screen.getByLabelText('Titel der neuen Opportunity');
    expect(titleInput.value).toMatch(/2026/); // Next year
    
    // Check if products are included
    expect(screen.getByLabelText('Produkte & Mengen')).toBeChecked();
  });
  
  it('should prevent cloning without title', () => {
    const opportunity = createTestOpportunity();
    render(<OpportunityCloneDialog opportunity={opportunity} open={true} />);
    
    // Clear title
    const titleInput = screen.getByLabelText('Titel der neuen Opportunity');
    fireEvent.change(titleInput, { target: { value: '' } });
    
    // Clone button should be disabled
    expect(screen.getByText('Opportunity klonen')).toBeDisabled();
  });
});
```

---

## 🎯 IMPLEMENTATION PRIORITIES

1. **Phase 1 (1 Tag)**: Backend Clone Service + Basic Frontend Dialog
2. **Phase 2 (1 Tag)**: Smart Templates + Clone Configuration Form  
3. **Phase 3 (0.5 Tag)**: Testing + Kanban Integration

**Geschätzter Aufwand:** 2-3 Entwicklungstage