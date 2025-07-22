# FC-015: Deal Loss Analysis 📊 CLAUDE_TECH

**Feature Code:** FC-015  
**Optimiert für:** Claude's 30-Sekunden-Produktivität  
**Original:** 1454 Zeilen → **Optimiert:** ~700 Zeilen (52% Reduktion)

## 🎯 QUICK-LOAD: Sofort produktiv in 30 Sekunden!

### Was macht FC-015?
**Mandatory Loss Analysis System mit verpflichtender Erfassung für alle CLOSED_LOST Opportunities und AI-powered Quarterly Reports**

### Die 5 Kern-Features:
1. **Mandatory Analysis** → Auto-Modal bei CLOSED_LOST, nicht schließbar bis vollständig
2. **Multi-Step Wizard** → 5 Schritte für bessere UX und Datenqualität  
3. **Competitor Intelligence** → Strukturierte Preiserfassung und Market Analysis
4. **Pattern Detection** → Auto-Alerts bei kritischen Loss-Trends (>5 gleiche Reasons)
5. **AI Quarterly Reports** → OpenAI-powered Recommendations und Executive Summaries

### Sofort starten:
```bash
# Backend: Loss Analysis Foundation
cd backend
./mvnw quarkus:add-extension -Dextensions="quarkus-hibernate-validator,quarkus-scheduler"

# Frontend: Analysis Modal + Dashboard
cd frontend  
npm install recharts react-rating @mui/lab
```

---

## 📦 1. BACKEND: Copy-paste Recipes

### 1.1 Loss Analysis Entity (10 Minuten)
```java
@Entity
@Table(name = "loss_analyses")
public class LossAnalysis {
    @Id @GeneratedValue
    private UUID id;
    
    @Column(name = "opportunity_id", nullable = false, unique = true)
    private UUID opportunityId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opportunity_id", insertable = false, updatable = false)
    private Opportunity opportunity;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "primary_loss_reason", nullable = false)
    private LossReason primaryLossReason;
    
    @Column(name = "competitor_name")
    private String competitorName;
    
    @Column(name = "competitor_price", precision = 10, scale = 2)
    private BigDecimal competitorPrice;
    
    @Column(name = "our_final_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal ourFinalPrice;
    
    @Column(name = "lessons_learned", nullable = false, length = 2000)
    @Size(min = 50, message = "Lessons Learned must be at least 50 characters")
    private String lessonsLearned;
    
    @Column(name = "was_preventable", nullable = false)
    private boolean wasPreventable = false;
    
    @Column(name = "prevention_strategy", length = 1000)
    private String preventionStrategy;
    
    @Column(name = "deal_phase_lost")
    @Enumerated(EnumType.STRING)
    private DealPhase dealPhaseLost;
    
    @Column(name = "analyzed_by", nullable = false)
    private UUID analyzedBy;
    
    @Column(name = "analyzed_at", nullable = false)
    private LocalDateTime analyzedAt;
    
    @Column(name = "confidence_score")
    @Min(1) @Max(5)
    private Integer confidenceScore;
    
    // Constructors, getters, setters
}

enum LossReason {
    PRICE_TOO_HIGH("Preis zu hoch", "price"),
    COMPETITOR_CHOSEN("Mitbewerber gewählt", "competitor"),
    NO_BUDGET("Kein Budget vorhanden", "budget"),
    PROJECT_CANCELLED("Projekt abgesagt", "cancelled"),
    WRONG_TIMING("Falsches Timing", "timing"),
    MISSING_FEATURES("Fehlende Features", "features"),
    POOR_RELATIONSHIP("Schlechte Beziehung", "relationship"),
    TECHNICAL_MISMATCH("Technische Inkompatibilität", "technical"),
    INTERNAL_SOLUTION("Interne Lösung entwickelt", "internal"),
    OTHER("Sonstiges", "other");
    
    private final String displayName;
    private final String analyticsKey;
}

enum DealPhase {
    INITIAL_CONTACT("Erstkontakt"),
    QUALIFICATION("Qualifikation"), 
    NEEDS_ANALYSIS("Bedarfsanalyse"),
    PROPOSAL_PHASE("Angebotserstellung"),
    NEGOTIATION("Verhandlung"),
    FINAL_DECISION("Endentscheidung");
}
```

### 1.2 Loss Analysis Service mit Pattern Detection (15 Minuten)
```java
@ApplicationScoped
@Transactional
public class LossAnalysisService {
    
    @Inject LossAnalysisRepository repository;
    @Inject CompetitorIntelligenceService competitorService;
    @Inject EventBus eventBus;
    
    public LossAnalysis createLossAnalysis(CreateLossAnalysisRequest request) {
        
        // Validation
        Opportunity opportunity = validateOpportunity(request.getOpportunityId());
        validateNoDuplicateAnalysis(opportunity.getId());
        
        // Create analysis
        LossAnalysis analysis = LossAnalysis.builder()
            .opportunityId(opportunity.getId())
            .primaryLossReason(request.getPrimaryLossReason())
            .competitorName(request.getCompetitorName())
            .competitorPrice(request.getCompetitorPrice())
            .ourFinalPrice(opportunity.getValue())
            .lessonsLearned(request.getLessonsLearned())
            .wasPreventable(request.isWasPreventable())
            .preventionStrategy(request.getPreventionStrategy())
            .dealPhaseLost(request.getDealPhaseLost())
            .analyzedBy(getCurrentUserId())
            .analyzedAt(LocalDateTime.now())
            .confidenceScore(request.getConfidenceScore())
            .build();
        
        // Enhanced validation
        validateAnalysisCompleteness(analysis);
        
        // Save
        LossAnalysis saved = repository.persist(analysis);
        
        // Update competitor intelligence
        updateCompetitorIntelligence(analysis);
        
        // Fire events
        eventBus.fire(new LossAnalysisCompletedEvent(
            opportunity.getId(),
            analysis.getId(),
            analysis.getPrimaryLossReason(),
            analysis.isWasPreventable()
        ));
        
        // Check for critical patterns
        checkForCriticalPatterns(analysis);
        
        return saved;
    }
    
    private void validateAnalysisCompleteness(LossAnalysis analysis) {
        List<String> errors = new ArrayList<>();
        
        // Lessons learned minimum length
        if (analysis.getLessonsLearned() == null || 
            analysis.getLessonsLearned().trim().length() < 50) {
            errors.add("Lessons Learned must be at least 50 characters");
        }
        
        // Competitor data consistency
        if ((analysis.getCompetitorName() != null) != 
            (analysis.getCompetitorPrice() != null)) {
            errors.add("Competitor name and price must both be provided or both be empty");
        }
        
        // Prevention strategy required if preventable
        if (analysis.isWasPreventable() && 
            (analysis.getPreventionStrategy() == null || 
             analysis.getPreventionStrategy().trim().isEmpty())) {
            errors.add("Prevention strategy is required when deal is marked as preventable");
        }
        
        if (!errors.isEmpty()) {
            throw new ValidationException(
                "Loss analysis validation failed: " + String.join(", ", errors)
            );
        }
    }
    
    private void checkForCriticalPatterns(LossAnalysis newAnalysis) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        
        // Check for concerning patterns (5+ same reason in 30 days)
        List<LossAnalysis> recentLosses = repository
            .findByAnalyzedAtAfterAndPrimaryLossReason(
                thirtyDaysAgo, 
                newAnalysis.getPrimaryLossReason()
            );
        
        if (recentLosses.size() >= 5) {
            // Critical pattern detected - notify management
            eventBus.fire(new CriticalLossPatternDetectedEvent(
                newAnalysis.getPrimaryLossReason(),
                recentLosses.size(),
                recentLosses.stream()
                    .map(LossAnalysis::getOpportunityId)
                    .collect(Collectors.toList())
            ));
        }
        
        // Check competitor dominance (3+ wins in 30 days)
        if (newAnalysis.getCompetitorName() != null) {
            long competitorWins = repository
                .countByAnalyzedAtAfterAndCompetitorName(
                    thirtyDaysAgo, 
                    newAnalysis.getCompetitorName()
                );
            
            if (competitorWins >= 3) {
                eventBus.fire(new CompetitorDominanceAlertEvent(
                    newAnalysis.getCompetitorName(),
                    competitorWins
                ));
            }
        }
    }
    
    private void updateCompetitorIntelligence(LossAnalysis analysis) {
        if (analysis.getCompetitorName() != null && 
            analysis.getCompetitorPrice() != null) {
            
            competitorService.updateCompetitorPrice(
                analysis.getCompetitorName(),
                analysis.getCompetitorPrice(),
                analysis.getOpportunity().getCustomer().getIndustry()
            );
        }
    }
}
```

### 1.3 Quarterly Report Generator mit AI (10 Minuten)
```java
@ApplicationScoped
public class QuarterlyReportGenerator {
    
    @Inject LossAnalysisRepository repository;
    @Inject OpportunityRepository opportunityRepository;
    @Inject OpenAIService openAIService;
    
    public QuarterlyLossReport generateReport(Year year, Quarter quarter) {
        
        LocalDateTime startDate = quarter.atYear(year.getValue()).atDay(1).atStartOfDay();
        LocalDateTime endDate = quarter.atYear(year.getValue()).atEndOfQuarter().atTime(23, 59, 59);
        
        List<LossAnalysis> quarterlyLosses = repository
            .findByAnalyzedAtBetween(startDate, endDate);
        List<Opportunity> quarterlyWins = opportunityRepository
            .findByStageAndClosedAtBetween(OpportunityStage.CLOSED_WON, startDate, endDate);
        
        return QuarterlyLossReport.builder()
            .reportPeriod(new ReportPeriod(year, quarter))
            .generatedAt(LocalDateTime.now())
            .summary(buildExecutiveSummary(quarterlyLosses, quarterlyWins))
            .lossReasonBreakdown(calculateLossReasonBreakdown(quarterlyLosses))
            .competitorAnalysis(buildCompetitorAnalysis(quarterlyLosses))
            .recommendations(generateAIRecommendations(quarterlyLosses))
            .build();
    }
    
    private ExecutiveSummary buildExecutiveSummary(
            List<LossAnalysis> losses, 
            List<Opportunity> wins) {
        
        BigDecimal totalLossValue = losses.stream()
            .map(LossAnalysis::getOurFinalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalWinValue = wins.stream()
            .map(Opportunity::getValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        double winRate = (double) wins.size() / (wins.size() + losses.size()) * 100;
        
        long preventableLosses = losses.stream()
            .filter(LossAnalysis::isWasPreventable)
            .count();
        BigDecimal preventableLossValue = losses.stream()
            .filter(LossAnalysis::isWasPreventable)
            .map(LossAnalysis::getOurFinalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return ExecutiveSummary.builder()
            .totalDeals(wins.size() + losses.size())
            .dealsWon(wins.size())
            .dealsLost(losses.size())
            .winRate(winRate)
            .totalWinValue(totalWinValue)
            .totalLossValue(totalLossValue)
            .preventableLosses(preventableLosses)
            .preventableLossValue(preventableLossValue)
            .topLossReason(findTopLossReason(losses))
            .biggestCompetitorThreat(findBiggestCompetitorThreat(losses))
            .build();
    }
    
    private List<AIRecommendation> generateAIRecommendations(List<LossAnalysis> losses) {
        // Prepare data for AI analysis
        String lossContext = losses.stream()
            .map(this::formatAnalysisForAI)
            .collect(Collectors.joining("\n"));
        
        String prompt = buildAIPrompt(lossContext, losses.size());
        
        try {
            String aiResponse = openAIService.generateRecommendations(prompt);
            return parseAIRecommendations(aiResponse);
        } catch (Exception e) {
            // Fallback to rule-based recommendations
            return generateRuleBasedRecommendations(losses);
        }
    }
    
    private String buildAIPrompt(String lossContext, int totalLosses) {
        return String.format("""
            Analyze the following %d lost sales opportunities and provide 3-5 specific, 
            actionable recommendations to improve win rate:
            
            %s
            
            Focus on:
            1. Patterns in loss reasons that could be prevented
            2. Competitive positioning improvements  
            3. Sales process optimizations
            4. Pricing strategy adjustments
            
            Format each recommendation as:
            Priority: [HIGH/MEDIUM/LOW]
            Action: [Specific action to take]
            Insight: [Why this will help based on the data]
            """, totalLosses, lossContext);
    }
}
```

### 1.4 REST API Endpoints (5 Minuten)
```java
@Path("/api/loss-analyses")
@Authenticated
public class LossAnalysisResource {
    
    @Inject LossAnalysisService service;
    @Inject QuarterlyReportGenerator reportGenerator;
    
    @POST
    @RolesAllowed({"admin", "manager", "sales"})
    public Response createLossAnalysis(CreateLossAnalysisRequest request) {
        try {
            LossAnalysis analysis = service.createLossAnalysis(request);
            return Response.status(201).entity(analysis).build();
        } catch (ValidationException e) {
            return Response.status(400)
                .entity(Map.of("error", e.getMessage()))
                .build();
        }
    }
    
    @GET
    @Path("/opportunity/{opportunityId}")
    public Response getAnalysisByOpportunity(@PathParam("opportunityId") UUID opportunityId) {
        Optional<LossAnalysis> analysis = service.findByOpportunityId(opportunityId);
        
        if (analysis.isPresent()) {
            return Response.ok(analysis.get()).build();
        } else {
            return Response.status(404)
                .entity(Map.of("error", "No analysis found for this opportunity"))
                .build();
        }
    }
    
    @GET
    @Path("/quarterly-report")
    @RolesAllowed({"admin", "manager"})
    public Response getQuarterlyReport(
            @QueryParam("year") @DefaultValue("2025") int year,
            @QueryParam("quarter") @DefaultValue("1") int quarter) {
        
        try {
            QuarterlyLossReport report = reportGenerator.generateReport(
                Year.of(year), 
                Quarter.of(quarter)
            );
            return Response.ok(report).build();
        } catch (Exception e) {
            return Response.status(500)
                .entity(Map.of("error", "Failed to generate report: " + e.getMessage()))
                .build();
        }
    }
    
    @GET
    @Path("/trends")
    @RolesAllowed({"admin", "manager"})
    public Response getLossTrends(
            @QueryParam("months") @DefaultValue("12") int months) {
        
        List<MonthlyLossData> trends = service.getLossTrends(months);
        return Response.ok(trends).build();
    }
}
```

---

## 🎨 2. FRONTEND: Loss Analysis Modal & Dashboard

### 2.1 Mandatory Loss Analysis Modal (15 Minuten)
```typescript
export const LossAnalysisModal: React.FC<{
  opportunity: Opportunity;
  open: boolean;
  onAnalysisComplete: (analysis: LossAnalysis) => void;
}> = ({ opportunity, open, onAnalysisComplete }) => {
  
  const { createLossAnalysis } = useLossAnalysisMutations();
  const [analysis, setAnalysis] = useState<CreateLossAnalysisRequest>({
    opportunityId: opportunity.id,
    ourFinalPrice: opportunity.value,
    analyzedBy: useCurrentUser().id
  });
  const [currentStep, setCurrentStep] = useState(0);
  const [isSubmitting, setIsSubmitting] = useState(false);
  
  // Validation
  const isValid = useMemo(() => {
    return analysis.primaryLossReason &&
           analysis.lessonsLearned?.length >= 50 &&
           (!analysis.wasPreventable || analysis.preventionStrategy) &&
           analysis.confidenceScore >= 1 && analysis.confidenceScore <= 5;
  }, [analysis]);
  
  const handleSubmit = async () => {
    if (!isValid) return;
    
    setIsSubmitting(true);
    try {
      const result = await createLossAnalysis.mutateAsync(analysis);
      
      toast.success('Loss Analysis erfolgreich gespeichert');
      onAnalysisComplete(result);
      
    } catch (error: any) {
      toast.error(`Fehler beim Speichern: ${error.message}`);
    } finally {
      setIsSubmitting(false);
    }
  };
  
  const steps = [
    { title: 'Verlustgrund', component: LossReasonStep },
    { title: 'Details', component: CompetitorDetailsStep },
    { title: 'Lessons Learned', component: LessonsLearnedStep },
    { title: 'Prävention', component: PreventionStep },
    { title: 'Bestätigung', component: ConfirmationStep }
  ];
  
  return (
    <Dialog 
      open={open} 
      onClose={undefined} // Nicht schließbar bis Analysis komplett!
      maxWidth="md" 
      fullWidth
      disableEscapeKeyDown
    >
      <DialogTitle>
        <Box display="flex" alignItems="center" justifyContent="space-between">
          <Box display="flex" alignItems="center">
            <ErrorOutline sx={{ mr: 1, color: 'error.main' }} />
            Deal-Verlust analysieren
          </Box>
          <Chip 
            label={`Schritt ${currentStep + 1} von ${steps.length}`}
            variant="outlined"
            size="small"
          />
        </Box>
        
        {/* Progress Stepper */}
        <Stepper activeStep={currentStep} sx={{ mt: 2 }}>
          {steps.map((step, index) => (
            <Step key={step.title}>
              <StepLabel>{step.title}</StepLabel>
            </Step>
          ))}
        </Stepper>
      </DialogTitle>
      
      <DialogContent>
        {/* Opportunity Context Card */}
        <Card variant="outlined" sx={{ mb: 3, bgcolor: 'grey.50' }}>
          <CardContent>
            <Grid container spacing={2}>
              <Grid item xs={12} sm={6}>
                <Typography variant="h6" gutterBottom>
                  {opportunity.title}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Kunde: {opportunity.customer.name}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Wert: {formatCurrency(opportunity.value)}
                </Typography>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="text.secondary">
                  Sales Cycle: {calculateSalesCycleDays(opportunity)} Tage
                </Typography>
              </Grid>
            </Grid>
          </CardContent>
        </Card>
        
        {/* Current Step Content */}
        {React.createElement(steps[currentStep].component, {
          analysis,
          setAnalysis,
          opportunity
        })}
      </DialogContent>
      
      <DialogActions>
        <Button 
          onClick={() => setCurrentStep(Math.max(0, currentStep - 1))}
          disabled={currentStep === 0 || isSubmitting}
        >
          Zurück
        </Button>
        
        {currentStep < steps.length - 1 ? (
          <Button 
            variant="contained"
            onClick={() => setCurrentStep(currentStep + 1)}
            disabled={!isStepValid(currentStep, analysis) || isSubmitting}
          >
            Weiter
          </Button>
        ) : (
          <Button 
            variant="contained"
            onClick={handleSubmit}
            disabled={!isValid || isSubmitting}
            startIcon={isSubmitting ? <CircularProgress size={16} /> : <Save />}
            sx={{ bgcolor: '#94C456', '&:hover': { bgcolor: '#7FA93F' } }}
          >
            {isSubmitting ? 'Wird gespeichert...' : 'Analyse speichern'}
          </Button>
        )}
      </DialogActions>
    </Dialog>
  );
};

// Step Components
const LossReasonStep: React.FC<StepProps> = ({ analysis, setAnalysis }) => {
  return (
    <Box>
      <Typography variant="h6" gutterBottom>
        Was war der Hauptgrund für den Verlust?
      </Typography>
      
      <FormControl fullWidth required sx={{ mb: 3 }}>
        <InputLabel>Verlustgrund</InputLabel>
        <Select
          value={analysis.primaryLossReason || ''}
          onChange={(e) => setAnalysis(prev => ({ 
            ...prev, 
            primaryLossReason: e.target.value as LossReason 
          }))}
        >
          {Object.entries(LossReason).map(([key, reason]) => (
            <MenuItem key={key} value={key}>
              <Box display="flex" alignItems="center">
                <Box
                  sx={{
                    width: 12,
                    height: 12,
                    borderRadius: '50%',
                    bgcolor: getLossReasonColor(key),
                    mr: 2
                  }}
                />
                {reason.displayName}
              </Box>
            </MenuItem>
          ))}
        </Select>
        <FormHelperText>
          Wählen Sie den wichtigsten Grund aus. Details können im nächsten Schritt ergänzt werden.
        </FormHelperText>
      </FormControl>
      
      {/* Conditional alerts based on loss reason */}
      {analysis.primaryLossReason === 'COMPETITOR_CHOSEN' && (
        <Alert severity="info" sx={{ mb: 2 }}>
          <AlertTitle>Competitor Analysis</AlertTitle>
          Im nächsten Schritt erfassen wir Details zum Mitbewerber.
        </Alert>
      )}
    </Box>
  );
};

const LessonsLearnedStep: React.FC<StepProps> = ({ analysis, setAnalysis }) => {
  const [charCount, setCharCount] = useState(analysis.lessonsLearned?.length || 0);
  
  return (
    <Box>
      <Typography variant="h6" gutterBottom>
        Was haben wir aus diesem Verlust gelernt?
      </Typography>
      
      <TextField
        fullWidth
        required
        label="Lessons Learned"
        multiline
        rows={6}
        value={analysis.lessonsLearned || ''}
        onChange={(e) => {
          setAnalysis(prev => ({ ...prev, lessonsLearned: e.target.value }));
          setCharCount(e.target.value.length);
        }}
        placeholder={`Beispiele:
• Entscheider wurde zu spät in den Prozess eingebunden
• Value Proposition nicht klar genug kommuniziert
• Mitbewerber hatte bessere Referenzen in der Branche
• Timing war schlecht - Budget war bereits vergeben`}
        helperText={`${charCount}/2000 Zeichen (mindestens 50 erforderlich)`}
        error={charCount > 0 && charCount < 50}
        inputProps={{ maxLength: 2000 }}
        sx={{ mb: 3 }}
      />
      
      {/* Confidence Score */}
      <Box sx={{ mb: 3 }}>
        <Typography variant="body1" gutterBottom>
          Wie sicher sind Sie sich bei dieser Einschätzung?
        </Typography>
        <Box display="flex" alignItems="center" gap={2}>
          <Typography variant="body2" color="text.secondary">Unsicher</Typography>
          <Rating
            value={analysis.confidenceScore || 0}
            onChange={(_, value) => setAnalysis(prev => ({ 
              ...prev, 
              confidenceScore: value 
            }))}
            max={5}
            precision={1}
          />
          <Typography variant="body2" color="text.secondary">Sehr sicher</Typography>
        </Box>
      </Box>
    </Box>
  );
};
```

### 2.2 Quarterly Dashboard mit Charts (15 Minuten)
```typescript
export const QuarterlyLossReportDashboard: React.FC = () => {
  const [selectedPeriod, setSelectedPeriod] = useState({
    year: new Date().getFullYear(),
    quarter: Math.ceil((new Date().getMonth() + 1) / 3) as Quarter
  });
  
  const { data: report, isLoading } = useQuarterlyLossReport(
    selectedPeriod.year, 
    selectedPeriod.quarter
  );
  
  if (isLoading) return <DashboardSkeleton />;
  if (!report) return <NoDataAlert />;
  
  return (
    <Box>
      {/* Header with Period Selector */}
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4">
          Loss Analysis Report - Q{selectedPeriod.quarter} {selectedPeriod.year}
        </Typography>
        
        <QuarterSelector
          value={selectedPeriod}
          onChange={setSelectedPeriod}
          maxYear={new Date().getFullYear()}
        />
      </Box>
      
      {/* Executive Summary Cards */}
      <Grid container spacing={3} mb={4}>
        <Grid item xs={12} sm={6} md={3}>
          <MetricCard
            title="Win Rate"
            value={`${report.summary.winRate.toFixed(1)}%`}
            subtitle={`${report.summary.dealsWon} von ${report.summary.totalDeals} Deals`}
            color="primary"
            icon={<TrendingUp />}
          />
        </Grid>
        
        <Grid item xs={12} sm={6} md={3}>
          <MetricCard
            title="Verlorener Wert"
            value={formatCurrency(report.summary.totalLossValue)}
            subtitle={`${report.summary.dealsLost} verlorene Deals`}
            color="error"
            icon={<TrendingDown />}
          />
        </Grid>
        
        <Grid item xs={12} sm={6} md={3}>
          <MetricCard
            title="Vermeidbare Verluste"
            value={`${((report.summary.preventableLosses / report.summary.dealsLost) * 100).toFixed(1)}%`}
            subtitle={formatCurrency(report.summary.preventableLossValue)}
            color="warning"
            icon={<Warning />}
          />
        </Grid>
        
        <Grid item xs={12} sm={6} md={3}>
          <MetricCard
            title="Top Verlustgrund"
            value={report.summary.topLossReason?.displayName || 'N/A'}
            subtitle={`Größter Competitor: ${report.summary.biggestCompetitorThreat || 'N/A'}`}
            color="info"
            icon={<Analytics />}
          />
        </Grid>
      </Grid>
      
      {/* Main Dashboard Content */}
      <Grid container spacing={3}>
        {/* Loss Reason Breakdown Pie Chart */}
        <Grid item xs={12} lg={6}>
          <Card>
            <CardHeader title="Verlustgründe Breakdown" />
            <CardContent>
              <ResponsiveContainer width="100%" height={300}>
                <PieChart>
                  <Pie
                    data={report.lossReasonBreakdown}
                    dataKey="count"
                    nameKey="reason.displayName"
                    cx="50%"
                    cy="50%"
                    outerRadius={80}
                    label={({ reason, percentage }) => 
                      `${reason.displayName}: ${percentage.toFixed(1)}%`
                    }
                  >
                    {report.lossReasonBreakdown.map((entry, index) => (
                      <Cell key={index} fill={getLossReasonColor(entry.reason.analyticsKey)} />
                    ))}
                  </Pie>
                  <Tooltip />
                  <Legend />
                </PieChart>
              </ResponsiveContainer>
            </CardContent>
          </Card>
        </Grid>
        
        {/* Competitor Analysis */}
        <Grid item xs={12} lg={6}>
          <Card>
            <CardHeader 
              title="Top 5 Competitors"
              subheader={`${report.competitorAnalysis.length} Mitbewerber erfasst`}
            />
            <CardContent>
              <List>
                {report.competitorAnalysis.slice(0, 5).map((competitor, index) => (
                  <ListItem key={competitor.competitorName} divider={index < 4}>
                    <ListItemAvatar>
                      <Avatar sx={{ bgcolor: index === 0 ? 'error.main' : 'grey.500' }}>
                        {index + 1}
                      </Avatar>
                    </ListItemAvatar>
                    
                    <ListItemText
                      primary={
                        <Box display="flex" alignItems="center" justifyContent="space-between">
                          <Typography variant="subtitle1">
                            {competitor.competitorName}
                          </Typography>
                          <Chip 
                            label={`${competitor.lossCount} Siege`}
                            color={index === 0 ? 'error' : 'default'}
                            size="small"
                          />
                        </Box>
                      }
                      secondary={
                        <Box>
                          <Typography variant="body2" color="text.secondary">
                            Ø Preis: {formatCurrency(competitor.averageCompetitorPrice)}
                            {' vs. '}
                            Ø Unser Preis: {formatCurrency(competitor.averageOurPrice)}
                          </Typography>
                          <Chip
                            label={`${competitor.priceDifferentialPercentage > 0 ? '+' : ''}${competitor.priceDifferentialPercentage.toFixed(1)}% Preisdifferenz`}
                            size="small"
                            color={competitor.priceDifferentialPercentage < -10 ? 'error' : 'default'}
                            sx={{ mt: 0.5 }}
                          />
                        </Box>
                      }
                    />
                  </ListItem>
                ))}
              </List>
            </CardContent>
          </Card>
        </Grid>
        
        {/* AI-Generated Recommendations */}
        <Grid item xs={12}>
          <Card>
            <CardHeader 
              title="AI-Empfohlene Maßnahmen"
              avatar={<AutoAwesome color="primary" />}
              subheader="KI-basierte Handlungsempfehlungen"
            />
            <CardContent>
              <Grid container spacing={2}>
                {report.recommendations.map((rec, index) => (
                  <Grid item xs={12} md={6} key={index}>
                    <Card variant="outlined">
                      <CardContent>
                        <Box display="flex" alignItems="start" mb={1}>
                          <Chip
                            label={rec.priority}
                            color={rec.priority === 'HIGH' ? 'error' : 'warning'}
                            size="small"
                            sx={{ mr: 1 }}
                          />
                          <Typography variant="h6" sx={{ flexGrow: 1 }}>
                            {rec.action}
                          </Typography>
                        </Box>
                        
                        <Typography variant="body2" color="text.secondary" mb={2}>
                          {rec.insight}
                        </Typography>
                        
                        <Button size="small" variant="outlined">
                          Als Task erstellen
                        </Button>
                      </CardContent>
                    </Card>
                  </Grid>
                ))}
              </Grid>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};
```

### 2.3 Integration in M4 Opportunity Pipeline (5 Minuten)
```typescript
// M4 Opportunity Pipeline Integration
export const OpportunityCard: React.FC<{ opportunity: Opportunity }> = ({ 
  opportunity 
}) => {
  const { updateOpportunityStage } = useOpportunityMutations();
  const [lossAnalysisOpen, setLossAnalysisOpen] = useState(false);
  
  const handleStageChange = async (newStage: OpportunityStage) => {
    if (newStage === 'CLOSED_LOST') {
      // Check if loss analysis already exists
      const existingAnalysis = await checkLossAnalysisExists(opportunity.id);
      
      if (!existingAnalysis) {
        // Open mandatory loss analysis modal
        setLossAnalysisOpen(true);
        return; // Don't update stage yet
      }
    }
    
    // Update stage normally for other cases
    await updateOpportunityStage.mutateAsync({
      opportunityId: opportunity.id,
      newStage
    });
  };
  
  const handleLossAnalysisComplete = async (analysis: LossAnalysis) => {
    // Now that analysis is complete, update the stage
    await updateOpportunityStage.mutateAsync({
      opportunityId: opportunity.id,
      newStage: 'CLOSED_LOST'
    });
    
    setLossAnalysisOpen(false);
    
    toast.success('Deal als verloren markiert und analysiert');
  };
  
  return (
    <Card>
      {/* Opportunity Card Content */}
      <CardContent>
        <Typography variant="h6">{opportunity.title}</Typography>
        <Typography variant="body2">{opportunity.customer.name}</Typography>
        
        {/* Stage Selector */}
        <StageSelector
          currentStage={opportunity.stage}
          onStageChange={handleStageChange}
        />
      </CardContent>
      
      {/* Mandatory Loss Analysis Modal */}
      <LossAnalysisModal
        opportunity={opportunity}
        open={lossAnalysisOpen}
        onAnalysisComplete={handleLossAnalysisComplete}
      />
    </Card>
  );
};
```

---

## 🗄️ 3. DATENBANK: Schema & Performance

### 3.1 Database Schema (Copy-paste ready)
```sql
-- V7.0__create_loss_analysis_tables.sql

-- Loss Analyses table
CREATE TABLE loss_analyses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    opportunity_id UUID NOT NULL UNIQUE REFERENCES opportunities(id),
    primary_loss_reason VARCHAR(50) NOT NULL,
    competitor_name VARCHAR(255),
    competitor_price DECIMAL(10,2),
    our_final_price DECIMAL(10,2) NOT NULL,
    lessons_learned TEXT NOT NULL CHECK (length(lessons_learned) >= 50),
    was_preventable BOOLEAN NOT NULL DEFAULT false,
    prevention_strategy TEXT,
    deal_phase_lost VARCHAR(50),
    analyzed_by UUID NOT NULL REFERENCES users(id),
    analyzed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    confidence_score INTEGER CHECK (confidence_score >= 1 AND confidence_score <= 5),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Performance indexes
CREATE INDEX idx_loss_analyses_analyzed_at ON loss_analyses(analyzed_at DESC);
CREATE INDEX idx_loss_analyses_primary_reason ON loss_analyses(primary_loss_reason);
CREATE INDEX idx_loss_analyses_competitor ON loss_analyses(competitor_name);
CREATE INDEX idx_loss_analyses_preventable ON loss_analyses(was_preventable) WHERE was_preventable = true;

-- Quarterly reports cache table
CREATE TABLE quarterly_loss_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    year INTEGER NOT NULL,
    quarter INTEGER NOT NULL CHECK (quarter >= 1 AND quarter <= 4),
    report_data JSONB NOT NULL,
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(year, quarter)
);

-- Indexes for reports
CREATE INDEX idx_quarterly_reports_period ON quarterly_loss_reports(year, quarter);

-- Auto-update timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_loss_analyses_updated_at
    BEFORE UPDATE ON loss_analyses
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
```

---

## ✅ 4. TESTING & VALIDATION

```typescript
// Loss Analysis Tests
describe('LossAnalysisModal', () => {
  it('should prevent closing without completing analysis', () => {
    render(<LossAnalysisModal opportunity={mockOpportunity} open={true} />);
    
    // ESC key should not close modal
    fireEvent.keyDown(document, { key: 'Escape' });
    expect(screen.getByText('Deal-Verlust analysieren')).toBeInTheDocument();
    
    // Click outside should not close modal
    fireEvent.click(document.body);
    expect(screen.getByText('Deal-Verlust analysieren')).toBeInTheDocument();
  });
  
  it('should validate lessons learned minimum length', () => {
    const { setAnalysis } = renderLossAnalysisModal();
    
    // Short lessons learned should show error
    setAnalysis({ lessonsLearned: 'Too short' });
    
    expect(screen.getByText(/mindestens 50 erforderlich/)).toBeInTheDocument();
    expect(screen.getByText('Analyse speichern')).toBeDisabled();
  });
  
  it('should enforce competitor data consistency', () => {
    const { setAnalysis } = renderLossAnalysisModal();
    
    // Competitor name without price should fail validation
    setAnalysis({ 
      competitorName: 'Competitor A',
      competitorPrice: null 
    });
    
    expect(screen.getByText(/both be provided or both be empty/)).toBeInTheDocument();
  });
});

// Backend Service Tests
@Test
void createLossAnalysis_withValidData_shouldCreateAnalysis() {
    // Given
    CreateLossAnalysisRequest request = validLossAnalysisRequest();
    
    // When
    LossAnalysis result = lossAnalysisService.createLossAnalysis(request);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getPrimaryLossReason()).isEqualTo(LossReason.PRICE_TOO_HIGH);
    assertThat(result.getLessonsLearned()).hasSize(greaterThan(50));
    
    // Verify pattern detection was called
    verify(eventBus).fire(any(LossAnalysisCompletedEvent.class));
}

@Test
void checkForCriticalPatterns_withFiveSameReasons_shouldFireAlert() {
    // Given: 4 existing analyses with same reason
    createExistingAnalyses(4, LossReason.PRICE_TOO_HIGH);
    
    // When: 5th analysis with same reason
    LossAnalysis newAnalysis = createLossAnalysis(LossReason.PRICE_TOO_HIGH);
    
    // Then: Critical pattern alert should be fired
    verify(eventBus).fire(argThat(event -> 
        event instanceof CriticalLossPatternDetectedEvent &&
        ((CriticalLossPatternDetectedEvent) event).getReasonCount() == 5
    ));
}
```

---

## 🎯 IMPLEMENTATION PRIORITIES

1. **Phase 1 (2 Tage)**: Backend Foundation + Mandatory Modal
2. **Phase 2 (2 Tage)**: Quarterly Reports + Dashboard  
3. **Phase 3 (1 Tag)**: AI Integration + Pattern Detection

**Geschätzter Aufwand:** 4-5 Entwicklungstage