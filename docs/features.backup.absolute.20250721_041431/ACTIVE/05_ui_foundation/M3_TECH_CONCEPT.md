# M3 Sales Cockpit - Tech Concept

**Feature-Code:** M3  
**Feature-Name:** Sales Cockpit  
**Feature-Typ:** 🎨 FRONTEND ENHANCEMENT  
**Erstellt:** 2025-07-20  
**Status:** Tech Concept (Enhancement-Mode)  
**Priorität:** HIGH - Core Dashboard  
**Geschätzter Aufwand:** 4-5 Tage (Enhancement)  

---

## 🧠 CLAUDE WORKING SECTION (15-Min Context Chunk)

### 📍 Kontext laden für produktive Arbeit

**Was ist M3?** Sales Cockpit Enhancement - Transformation des bestehenden 3-Spalten-Dashboards zu einem KI-powered Command Center

**Warum Enhancement statt Neuentwicklung?** SalesCockpitV2.tsx existiert bereits (60% fertig) - wir haben ein exzellentes 3-Spalten-Layout mit ResizablePanels!

**Aktueller Zustand:** ✅ **LIVE URL:** http://localhost:5173/cockpit
- ✅ **3-Spalten-Layout:** MyDayColumn + FocusColumn + WinColumn perfekt implementiert
- ✅ **ResizablePanels:** Nutzer kann Spalten anpassen - excellent UX!
- ✅ **Material-UI Integration:** Konsistente Komponenten und Theming
- ❌ **KI-Intelligence:** Keine Smart Insights oder Opportunity Predictions
- ❌ **Real-Time Updates:** Statische Daten statt Live-Updates
- ❌ **Performance Metrics:** Keine Sales-Performance Visualization

**Abhängigkeiten verstehen:**
- **Benötigt:** FC-008 Security (✅ VERFÜGBAR) - User-Context für personalisierte Daten
- **Erweitert:** M1 Navigation (✅ VERFÜGBAR) - Dashboard-Navigation und Integration
- **Integriert mit:** FC-002 Smart Insights, FC-034 Instant Insights - KI-Features

**Technischer Kern - KI-Enhanced Cockpit:**
```typescript
export const SalesCockpitV2Enhanced: React.FC = () => {
    const { user } = useAuth();
    const { insights } = useSmartInsights();
    const { metrics } = useRealTimeMetrics();
    
    return (
        <Box sx={{ height: '100vh', display: 'flex', flexDirection: 'column' }}>
            {/* Enhanced Header mit AI Insights */}
            <CockpitHeader user={user} insights={insights} />
            
            {/* Bestehende 3-Spalten mit KI-Enhancement */}
            <ResizablePanelGroup direction="horizontal" style={{ flex: 1 }}>
                <ResizablePanel defaultSize={30}>
                    <MyDayColumnEnhanced />
                </ResizablePanel>
                <ResizableHandle />
                <ResizablePanel defaultSize={40}>
                    <FocusColumnEnhanced />
                </ResizablePanel>
                <ResizableHandle />
                <ResizablePanel defaultSize={30}>
                    <WinColumnEnhanced />
                </ResizablePanel>
            </ResizablePanelGroup>
        </Box>
    );
};
```

**Business Value - Command Center Evolution:**
- **Bestehend:** Statisches 3-Spalten Dashboard (60% fertig)
- **Enhanced:** KI-powered Sales Command Center mit Predictive Analytics
- **ROI:** 3-5 Entwicklertage (~€5.000) für 40% mehr Sales Efficiency

---

## 🎯 ÜBERSICHT

### Business Value
**Problem:** Bestehender Sales Cockpit ist statisch - Verkäufer müssen manuell priorisieren und verpassen Opportunities

**Lösung:** KI-enhanced Command Center mit predictive insights, real-time updates und intelligent prioritization

**ROI:** 
- **Kosten:** 4-5 Entwicklertage (~€5.000) - Enhancement bestehender Exzellenz
- **Sales Efficiency:** +40% durch intelligent prioritization und AI insights
- **Opportunity Detection:** +25% mehr qualifizierte Leads durch predictive analytics
- **Time Saving:** 2-3 Stunden pro Verkäufer/Tag durch automatisierte Priorisierung
- **ROI-Ratio:** 12:1 (Break-even nach 2 Wochen)

### Kernfunktionen (Enhancement)
1. **KI-Enhanced MyDay Column** - Intelligent Task Prioritization basierend auf Success Probability
2. **Smart Focus Column** - Predictive Opportunity Scoring mit Next Best Actions
3. **Enhanced Win Column** - Real-time Pipeline Visualization mit Forecasting
4. **AI Insights Header** - Personalized Daily Briefing mit Action Recommendations
5. **Performance Metrics Dashboard** - Live Sales KPIs mit Trend Analysis
6. **Predictive Alerts** - Proactive Notifications für At-Risk Opportunities

---

## 🏗️ ARCHITEKTUR

### Enhancement Strategy - Build on Existing Excellence
```
BESTEHEND (60% fertig - EXZELLENT!):    →    ENHANCED (100% mit KI):
SalesCockpitV2.tsx                           SalesCockpitV2Enhanced.tsx
├── 3-Spalten ResizablePanels ✅              ├── AI-Enhanced 3-Spalten System
├── MyDayColumnMUI.tsx ✅                     ├── MyDayColumnEnhanced (KI-Priorität)
├── FocusListColumnMUI.tsx ✅                 ├── FocusColumnEnhanced (Predictive)
├── WinColumnMUI.tsx ✅                       ├── WinColumnEnhanced (Real-time)
└── Material-UI Theming ✅                    └── AI Insights Integration
```

### Component Architecture - Enhanced Sales Cockpit
```typescript
// Enhanced Sales Cockpit (builds on excellent existing V2)
interface SalesCockpitProps {
    user: User;
}

export const SalesCockpitV2Enhanced: React.FC<SalesCockpitProps> = ({ user }) => {
    const { insights, isLoading: insightsLoading } = useSmartInsights(user.id);
    const { metrics, isLoading: metricsLoading } = useRealTimeMetrics(user.id);
    const { opportunities } = useOpportunityPipeline(user.id);
    const { tasks } = useMyDayTasks(user.id);
    
    if (insightsLoading || metricsLoading) {
        return <CockpitSkeleton />;
    }
    
    return (
        <Box sx={{ height: '100vh', display: 'flex', flexDirection: 'column' }}>
            {/* Enhanced AI Insights Header */}
            <AICockpitHeader 
                user={user} 
                insights={insights} 
                metrics={metrics}
                sx={{ height: '120px', mb: 2 }}
            />
            
            {/* Bestehende 3-Spalten-Exzellenz mit KI-Enhancement */}
            <Box sx={{ flex: 1, px: 2 }}>
                <ResizablePanelGroup direction="horizontal" style={{ height: '100%' }}>
                    <ResizablePanel defaultSize={30} minSize={25}>
                        <MyDayColumnEnhanced 
                            tasks={tasks}
                            insights={insights}
                            user={user}
                        />
                    </ResizablePanel>
                    
                    <ResizableHandle withHandle />
                    
                    <ResizablePanel defaultSize={40} minSize={35}>
                        <FocusColumnEnhanced 
                            opportunities={opportunities}
                            insights={insights}
                            user={user}
                        />
                    </ResizablePanel>
                    
                    <ResizableHandle withHandle />
                    
                    <ResizablePanel defaultSize={30} minSize={25}>
                        <WinColumnEnhanced 
                            pipeline={opportunities}
                            metrics={metrics}
                            user={user}
                        />
                    </ResizablePanel>
                </ResizablePanelGroup>
            </Box>
        </Box>
    );
};

// AI Insights Header - New Command Center Feature
interface AICockpitHeaderProps {
    user: User;
    insights: SmartInsights;
    metrics: RealTimeMetrics;
    sx?: SxProps;
}

export const AICockpitHeader: React.FC<AICockpitHeaderProps> = ({ user, insights, metrics, sx }) => {
    const [alertsExpanded, setAlertsExpanded] = useState(false);
    
    return (
        <Paper sx={{ p: 2, background: 'linear-gradient(135deg, #94C456 0%, #004F7B 100%)', ...sx }}>
            <Grid container spacing={2} alignItems="center">
                {/* Personal Welcome & AI Brief */}
                <Grid item xs={12} md={4}>
                    <Box sx={{ color: 'white' }}>
                        <Typography variant="h5" sx={{ fontWeight: 'bold', fontFamily: 'Antonio' }}>
                            Good Morning, {user.firstName}! 👋
                        </Typography>
                        <Typography variant="body1" sx={{ opacity: 0.9 }}>
                            {insights.dailyBrief}
                        </Typography>
                        <Chip 
                            label={`${insights.priorityActions} Priority Actions`}
                            size="small"
                            sx={{ mt: 1, bgcolor: 'rgba(255,255,255,0.2)', color: 'white' }}
                        />
                    </Box>
                </Grid>
                
                {/* Live Performance Metrics */}
                <Grid item xs={12} md={4}>
                    <Box sx={{ textAlign: 'center' }}>
                        <Grid container spacing={2}>
                            <Grid item xs={4}>
                                <Typography variant="h4" sx={{ color: 'white', fontWeight: 'bold' }}>
                                    {metrics.todayRevenue}
                                </Typography>
                                <Typography variant="caption" sx={{ color: 'rgba(255,255,255,0.8)' }}>
                                    Today's Revenue
                                </Typography>
                            </Grid>
                            <Grid item xs={4}>
                                <Typography variant="h4" sx={{ color: 'white', fontWeight: 'bold' }}>
                                    {metrics.pipelineValue}
                                </Typography>
                                <Typography variant="caption" sx={{ color: 'rgba(255,255,255,0.8)' }}>
                                    Pipeline Value
                                </Typography>
                            </Grid>
                            <Grid item xs={4}>
                                <Typography variant="h4" sx={{ color: 'white', fontWeight: 'bold' }}>
                                    {metrics.winRate}%
                                </Typography>
                                <Typography variant="caption" sx={{ color: 'rgba(255,255,255,0.8)' }}>
                                    Win Rate
                                </Typography>
                            </Grid>
                        </Grid>
                        
                        {/* Trend Indicators */}
                        <Box sx={{ mt: 1, display: 'flex', justifyContent: 'center', gap: 1 }}>
                            <TrendIndicator trend={metrics.revenueTrend} />
                            <TrendIndicator trend={metrics.pipelineTrend} />
                            <TrendIndicator trend={metrics.winRateTrend} />
                        </Box>
                    </Box>
                </Grid>
                
                {/* Smart Alerts & Actions */}
                <Grid item xs={12} md={4}>
                    <Box sx={{ textAlign: 'right' }}>
                        <IconButton 
                            sx={{ color: 'white', mb: 1 }}
                            onClick={() => setAlertsExpanded(!alertsExpanded)}
                        >
                            <Badge badgeContent={insights.alerts.length} color="error">
                                <NotificationsIcon />
                            </Badge>
                        </IconButton>
                        
                        <Typography variant="body2" sx={{ color: 'rgba(255,255,255,0.9)' }}>
                            {insights.nextBestAction}
                        </Typography>
                        
                        <Button
                            variant="contained"
                            size="small"
                            sx={{ 
                                mt: 1, 
                                bgcolor: 'rgba(255,255,255,0.2)', 
                                '&:hover': { bgcolor: 'rgba(255,255,255,0.3)' }
                            }}
                            onClick={insights.executeNextAction}
                        >
                            Take Action
                        </Button>
                        
                        {/* Expandable Alerts */}
                        <Collapse in={alertsExpanded}>
                            <Paper sx={{ mt: 1, p: 1, maxHeight: 200, overflowY: 'auto' }}>
                                {insights.alerts.map((alert, index) => (
                                    <Alert 
                                        key={index} 
                                        severity={alert.severity} 
                                        size="small"
                                        sx={{ mb: 0.5 }}
                                    >
                                        {alert.message}
                                    </Alert>
                                ))}
                            </Paper>
                        </Collapse>
                    </Box>
                </Grid>
            </Grid>
        </Paper>
    );
};

// Enhanced MyDay Column (builds on excellent existing MyDayColumnMUI.tsx)
interface MyDayColumnEnhancedProps {
    tasks: Task[];
    insights: SmartInsights;
    user: User;
}

export const MyDayColumnEnhanced: React.FC<MyDayColumnEnhancedProps> = ({ tasks, insights, user }) => {
    const [filter, setFilter] = useState<'all' | 'priority' | 'ai-suggested'>('ai-suggested');
    
    // AI-Enhanced Task Prioritization
    const prioritizedTasks = useMemo(() => {
        return tasks
            .map(task => ({
                ...task,
                aiScore: insights.calculateTaskPriority(task),
                predictedOutcome: insights.predictTaskOutcome(task),
                timeToComplete: insights.estimateTimeToComplete(task)
            }))
            .sort((a, b) => b.aiScore - a.aiScore);
    }, [tasks, insights]);
    
    const filteredTasks = useMemo(() => {
        switch (filter) {
            case 'priority':
                return prioritizedTasks.filter(task => task.priority === 'high');
            case 'ai-suggested':
                return prioritizedTasks.filter(task => task.aiScore > 0.7);
            default:
                return prioritizedTasks;
        }
    }, [prioritizedTasks, filter]);
    
    return (
        <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
            <CardHeader
                title={
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <TodayIcon color="primary" />
                        <Typography variant="h6" sx={{ fontFamily: 'Antonio' }}>
                            My Day
                        </Typography>
                        <Chip 
                            label={`${filteredTasks.length} tasks`}
                            size="small"
                            color="primary"
                            variant="outlined"
                        />
                    </Box>
                }
                action={
                    <ToggleButtonGroup
                        value={filter}
                        exclusive
                        onChange={(_, newFilter) => newFilter && setFilter(newFilter)}
                        size="small"
                    >
                        <ToggleButton value="ai-suggested">
                            <AutoAwesomeIcon fontSize="small" />
                        </ToggleButton>
                        <ToggleButton value="priority">
                            <PriorityHighIcon fontSize="small" />
                        </ToggleButton>
                        <ToggleButton value="all">
                            <ListIcon fontSize="small" />
                        </ToggleButton>
                    </ToggleButtonGroup>
                }
                sx={{ pb: 1 }}
            />
            
            <CardContent sx={{ flex: 1, overflowY: 'auto', pt: 0 }}>
                {filteredTasks.map((task) => (
                    <EnhancedTaskCard 
                        key={task.id}
                        task={task}
                        insights={insights}
                        onComplete={() => handleTaskComplete(task)}
                        sx={{ mb: 1 }}
                    />
                ))}
                
                {filteredTasks.length === 0 && (
                    <Box sx={{ textAlign: 'center', py: 4 }}>
                        <CheckCircleIcon sx={{ fontSize: 48, color: 'success.main', mb: 2 }} />
                        <Typography variant="h6" color="success.main">
                            All Done! 🎉
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                            {filter === 'ai-suggested' 
                                ? 'No AI-suggested tasks remaining'
                                : 'No tasks for today'
                            }
                        </Typography>
                    </Box>
                )}
            </CardContent>
        </Card>
    );
};

// Enhanced Task Card with AI Insights
interface EnhancedTaskCardProps {
    task: Task & {
        aiScore: number;
        predictedOutcome: string;
        timeToComplete: number;
    };
    insights: SmartInsights;
    onComplete: () => void;
    sx?: SxProps;
}

export const EnhancedTaskCard: React.FC<EnhancedTaskCardProps> = ({ task, insights, onComplete, sx }) => {
    const [expanded, setExpanded] = useState(false);
    
    const getPriorityColor = (score: number) => {
        if (score > 0.8) return 'error';
        if (score > 0.6) return 'warning';
        return 'success';
    };
    
    return (
        <Card 
            sx={{ 
                border: task.aiScore > 0.8 ? '2px solid #f44336' : '1px solid #e0e0e0',
                '&:hover': { 
                    boxShadow: 3,
                    transform: 'translateY(-2px)',
                    transition: 'all 0.2s ease-in-out'
                },
                ...sx 
            }}
        >
            <CardContent sx={{ pb: 1 }}>
                <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 1 }}>
                    <Checkbox
                        checked={task.completed}
                        onChange={onComplete}
                        color="primary"
                    />
                    
                    <Box sx={{ flex: 1 }}>
                        <Typography 
                            variant="body1" 
                            sx={{ 
                                fontWeight: task.aiScore > 0.7 ? 'bold' : 'normal',
                                textDecoration: task.completed ? 'line-through' : 'none'
                            }}
                        >
                            {task.title}
                        </Typography>
                        
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 0.5 }}>
                            <Chip 
                                label={`${Math.round(task.aiScore * 100)}% Priority`}
                                size="small"
                                color={getPriorityColor(task.aiScore)}
                                variant="outlined"
                            />
                            
                            <Chip 
                                label={`${task.timeToComplete}min`}
                                size="small"
                                variant="outlined"
                                icon={<AccessTimeIcon />}
                            />
                            
                            {task.aiScore > 0.8 && (
                                <Chip 
                                    label="🔥 Hot"
                                    size="small"
                                    color="error"
                                />
                            )}
                        </Box>
                        
                        {task.predictedOutcome && (
                            <Typography variant="caption" color="text.secondary" sx={{ mt: 0.5, display: 'block' }}>
                                💡 {task.predictedOutcome}
                            </Typography>
                        )}
                    </Box>
                    
                    <IconButton
                        size="small"
                        onClick={() => setExpanded(!expanded)}
                    >
                        {expanded ? <ExpandLessIcon /> : <ExpandMoreIcon />}
                    </IconButton>
                </Box>
                
                <Collapse in={expanded}>
                    <Box sx={{ mt: 2, pt: 2, borderTop: '1px solid #e0e0e0' }}>
                        <Typography variant="body2" color="text.secondary">
                            {task.description}
                        </Typography>
                        
                        {task.relatedOpportunity && (
                            <Box sx={{ mt: 1 }}>
                                <Typography variant="caption" color="primary">
                                    Related: {task.relatedOpportunity.name}
                                </Typography>
                            </Box>
                        )}
                        
                        <Box sx={{ mt: 1, display: 'flex', gap: 1 }}>
                            <Button size="small" variant="contained" onClick={onComplete}>
                                Complete
                            </Button>
                            <Button size="small" variant="outlined">
                                Reschedule
                            </Button>
                            <Button size="small" variant="text">
                                Details
                            </Button>
                        </Box>
                    </Box>
                </Collapse>
            </CardContent>
        </Card>
    );
};

// Enhanced Focus Column (builds on excellent existing FocusListColumnMUI.tsx)
interface FocusColumnEnhancedProps {
    opportunities: Opportunity[];
    insights: SmartInsights;
    user: User;
}

export const FocusColumnEnhanced: React.FC<FocusColumnEnhancedProps> = ({ opportunities, insights, user }) => {
    const [sortBy, setSortBy] = useState<'ai-score' | 'value' | 'close-date'>('ai-score');
    
    // AI-Enhanced Opportunity Scoring
    const scoredOpportunities = useMemo(() => {
        return opportunities
            .map(opp => ({
                ...opp,
                aiScore: insights.calculateOpportunityScore(opp),
                winProbability: insights.predictWinProbability(opp),
                nextBestAction: insights.getNextBestAction(opp),
                riskFactors: insights.identifyRisks(opp)
            }))
            .sort((a, b) => {
                switch (sortBy) {
                    case 'ai-score':
                        return b.aiScore - a.aiScore;
                    case 'value':
                        return b.value - a.value;
                    case 'close-date':
                        return new Date(a.closeDate).getTime() - new Date(b.closeDate).getTime();
                    default:
                        return b.aiScore - a.aiScore;
                }
            });
    }, [opportunities, insights, sortBy]);
    
    const highPriorityOpps = scoredOpportunities.filter(opp => opp.aiScore > 0.7);
    
    return (
        <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
            <CardHeader
                title={
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <TargetIcon color="primary" />
                        <Typography variant="h6" sx={{ fontFamily: 'Antonio' }}>
                            Focus Pipeline
                        </Typography>
                        <Chip 
                            label={`${highPriorityOpps.length} hot deals`}
                            size="small"
                            color="error"
                            variant="outlined"
                        />
                    </Box>
                }
                action={
                    <FormControl size="small" sx={{ minWidth: 120 }}>
                        <Select
                            value={sortBy}
                            onChange={(e) => setSortBy(e.target.value as any)}
                            displayEmpty
                        >
                            <MenuItem value="ai-score">🤖 AI Score</MenuItem>
                            <MenuItem value="value">💰 Value</MenuItem>
                            <MenuItem value="close-date">📅 Close Date</MenuItem>
                        </Select>
                    </FormControl>
                }
                sx={{ pb: 1 }}
            />
            
            <CardContent sx={{ flex: 1, overflowY: 'auto', pt: 0 }}>
                {scoredOpportunities.map((opportunity) => (
                    <EnhancedOpportunityCard 
                        key={opportunity.id}
                        opportunity={opportunity}
                        insights={insights}
                        sx={{ mb: 2 }}
                    />
                ))}
            </CardContent>
        </Card>
    );
};

// Enhanced Opportunity Card with AI Insights
interface EnhancedOpportunityCardProps {
    opportunity: Opportunity & {
        aiScore: number;
        winProbability: number;
        nextBestAction: string;
        riskFactors: string[];
    };
    insights: SmartInsights;
    sx?: SxProps;
}

export const EnhancedOpportunityCard: React.FC<EnhancedOpportunityCardProps> = ({ opportunity, insights, sx }) => {
    const navigate = useNavigate();
    
    const getScoreColor = (score: number) => {
        if (score > 0.8) return '#f44336'; // Red - Hot
        if (score > 0.6) return '#ff9800'; // Orange - Warm  
        if (score > 0.4) return '#2196f3'; // Blue - Cool
        return '#9e9e9e'; // Grey - Cold
    };
    
    return (
        <Card 
            sx={{ 
                border: `2px solid ${getScoreColor(opportunity.aiScore)}`,
                position: 'relative',
                '&:hover': { 
                    boxShadow: 4,
                    transform: 'translateY(-4px)',
                    transition: 'all 0.3s ease-in-out'
                },
                ...sx 
            }}
        >
            {/* AI Score Badge */}
            <Box
                sx={{
                    position: 'absolute',
                    top: -8,
                    right: 8,
                    bgcolor: getScoreColor(opportunity.aiScore),
                    color: 'white',
                    borderRadius: '50%',
                    width: 32,
                    height: 32,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    fontSize: '0.75rem',
                    fontWeight: 'bold',
                    zIndex: 1
                }}
            >
                {Math.round(opportunity.aiScore * 100)}
            </Box>
            
            <CardContent>
                <Typography variant="h6" sx={{ fontWeight: 'bold', pr: 4 }}>
                    {opportunity.name}
                </Typography>
                
                <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                    {opportunity.customer.name}
                </Typography>
                
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                    <Chip 
                        label={formatCurrency(opportunity.value)}
                        color="primary"
                        variant="filled"
                    />
                    <Chip 
                        label={`${opportunity.winProbability}% Win`}
                        color={opportunity.winProbability > 70 ? 'success' : opportunity.winProbability > 40 ? 'warning' : 'error'}
                        variant="outlined"
                    />
                </Box>
                
                {/* Next Best Action */}
                <Alert 
                    severity="info" 
                    sx={{ mb: 1, '& .MuiAlert-message': { fontSize: '0.875rem' } }}
                    icon={<AutoAwesomeIcon />}
                >
                    <Typography variant="body2" sx={{ fontWeight: 'medium' }}>
                        💡 {opportunity.nextBestAction}
                    </Typography>
                </Alert>
                
                {/* Risk Factors */}
                {opportunity.riskFactors.length > 0 && (
                    <Alert severity="warning" sx={{ mb: 1 }}>
                        <Typography variant="caption">
                            ⚠️ Risks: {opportunity.riskFactors.join(', ')}
                        </Typography>
                    </Alert>
                )}
                
                {/* Progress Bar */}
                <Box sx={{ mb: 2 }}>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.5 }}>
                        <Typography variant="caption" color="text.secondary">
                            Stage: {opportunity.stage}
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                            Close: {format(new Date(opportunity.closeDate), 'MMM dd')}
                        </Typography>
                    </Box>
                    <LinearProgress 
                        variant="determinate" 
                        value={opportunity.winProbability} 
                        sx={{ height: 6, borderRadius: 3 }}
                        color={opportunity.winProbability > 70 ? 'success' : opportunity.winProbability > 40 ? 'warning' : 'error'}
                    />
                </Box>
                
                {/* Action Buttons */}
                <Box sx={{ display: 'flex', gap: 1 }}>
                    <Button 
                        size="small" 
                        variant="contained"
                        onClick={() => navigate(`/opportunities/${opportunity.id}`)}
                        sx={{ flex: 1 }}
                    >
                        Open
                    </Button>
                    <Button 
                        size="small" 
                        variant="outlined"
                        onClick={() => insights.executeNextAction(opportunity)}
                    >
                        {opportunity.nextBestAction.split(' ')[0]}
                    </Button>
                </Box>
            </CardContent>
        </Card>
    );
};
```

---

## 🔄 ABHÄNGIGKEITEN

### Benötigt diese Features:
- **FC-008 Security Foundation** - User Context für personalisierte Dashboard-Daten ✅ VERFÜGBAR
- **M1 Navigation System** - Dashboard-Navigation und Layout-Integration ✅ VERFÜGBAR
- **Bestehende SalesCockpitV2.tsx** - 60% fertige Basis-Implementation ✅ VORHANDEN

### Ermöglicht diese Features:
- **FC-002 Smart Customer Insights** - KI-Features für intelligent prioritization
- **FC-034 Instant Insights** - Real-time Analytics Integration
- **M4 Opportunity Pipeline** - Enhanced Pipeline Visualization
- **All Sales Features** - Zentrales Command Center für Sales-Aktivitäten

### Integriert mit:
- **FC-037 Advanced Reporting** - Performance Metrics Dashboard Integration
- **FC-040 Performance Monitoring** - Real-time System Health in Cockpit
- **M2 Quick Create Actions** - Quick Actions direkt vom Dashboard

---

## 🧪 TESTING-STRATEGIE

### Component Tests - Enhanced Cockpit
```typescript
describe('SalesCockpitV2Enhanced', () => {
    it('should render AI insights header with user data', () => {
        const mockUser = { id: '1', firstName: 'John', lastName: 'Doe' };
        const mockInsights = {
            dailyBrief: 'Focus on high-priority deals today',
            priorityActions: 3,
            alerts: []
        };
        
        render(<SalesCockpitV2Enhanced user={mockUser} />);
        
        expect(screen.getByText('Good Morning, John!')).toBeInTheDocument();
        expect(screen.getByText('Focus on high-priority deals today')).toBeInTheDocument();
        expect(screen.getByText('3 Priority Actions')).toBeInTheDocument();
    });
    
    it('should maintain existing ResizablePanels functionality', () => {
        render(<SalesCockpitV2Enhanced user={mockUser} />);
        
        // Check that all three columns are present
        expect(screen.getByText('My Day')).toBeInTheDocument();
        expect(screen.getByText('Focus Pipeline')).toBeInTheDocument();
        expect(screen.getByText('Win Column')).toBeInTheDocument();
        
        // Verify ResizablePanels are rendered
        expect(document.querySelectorAll('[data-panel-group]')).toHaveLength(1);
        expect(document.querySelectorAll('[data-panel]')).toHaveLength(3);
    });
});

describe('MyDayColumnEnhanced', () => {
    it('should prioritize tasks based on AI score', () => {
        const mockTasks = [
            { id: '1', title: 'Low priority task', aiScore: 0.3 },
            { id: '2', title: 'High priority task', aiScore: 0.9 },
            { id: '3', title: 'Medium priority task', aiScore: 0.6 }
        ];
        
        render(<MyDayColumnEnhanced tasks={mockTasks} insights={mockInsights} user={mockUser} />);
        
        const taskElements = screen.getAllByText(/priority task/);
        
        // High priority task should be first
        expect(taskElements[0]).toHaveTextContent('High priority task');
        expect(taskElements[1]).toHaveTextContent('Medium priority task');
        expect(taskElements[2]).toHaveTextContent('Low priority task');
    });
    
    it('should filter tasks based on AI suggestions', () => {
        const mockTasks = [
            { id: '1', title: 'AI suggested', aiScore: 0.8 },
            { id: '2', title: 'Not AI suggested', aiScore: 0.5 }
        ];
        
        render(<MyDayColumnEnhanced tasks={mockTasks} insights={mockInsights} user={mockUser} />);
        
        // Default filter should be 'ai-suggested'
        expect(screen.getByText('AI suggested')).toBeInTheDocument();
        expect(screen.queryByText('Not AI suggested')).not.toBeInTheDocument();
    });
});

describe('FocusColumnEnhanced', () => {
    it('should display opportunities with AI scoring', () => {
        const mockOpportunities = [
            {
                id: '1',
                name: 'Hot Deal',
                aiScore: 0.9,
                winProbability: 85,
                nextBestAction: 'Send proposal',
                value: 50000
            }
        ];
        
        render(<FocusColumnEnhanced opportunities={mockOpportunities} insights={mockInsights} user={mockUser} />);
        
        expect(screen.getByText('Hot Deal')).toBeInTheDocument();
        expect(screen.getByText('90')).toBeInTheDocument(); // AI Score badge
        expect(screen.getByText('85% Win')).toBeInTheDocument();
        expect(screen.getByText('💡 Send proposal')).toBeInTheDocument();
    });
    
    it('should sort opportunities by different criteria', () => {
        const mockOpportunities = [
            { id: '1', name: 'Low Score', aiScore: 0.3, value: 100000 },
            { id: '2', name: 'High Score', aiScore: 0.9, value: 50000 }
        ];
        
        render(<FocusColumnEnhanced opportunities={mockOpportunities} insights={mockInsights} user={mockUser} />);
        
        // Default sort by AI score - High Score should be first
        let oppElements = screen.getAllByText(/Score/);
        expect(oppElements[0]).toHaveTextContent('High Score');
        
        // Change to sort by value
        fireEvent.change(screen.getByDisplayValue('🤖 AI Score'), { target: { value: 'value' } });
        
        // Now Low Score (higher value) should be first
        oppElements = screen.getAllByText(/Score/);
        expect(oppElements[0]).toHaveTextContent('Low Score');
    });
});
```

### Integration Tests - AI Features
```typescript
describe('AI Integration', () => {
    it('should fetch and display smart insights', async () => {
        const mockInsights = {
            dailyBrief: 'Focus on closing Q4 deals',
            priorityActions: 5,
            alerts: [
                { severity: 'warning', message: 'Deal ABC Corp at risk' }
            ]
        };
        
        jest.mocked(useSmartInsights).mockReturnValue({
            insights: mockInsights,
            isLoading: false
        });
        
        render(<SalesCockpitV2Enhanced user={mockUser} />);
        
        expect(screen.getByText('Focus on closing Q4 deals')).toBeInTheDocument();
        expect(screen.getByText('5 Priority Actions')).toBeInTheDocument();
        
        // Click alerts to expand
        fireEvent.click(screen.getByRole('button', { name: /notifications/i }));
        
        await waitFor(() => {
            expect(screen.getByText('Deal ABC Corp at risk')).toBeInTheDocument();
        });
    });
    
    it('should handle real-time metrics updates', async () => {
        const mockMetrics = {
            todayRevenue: '€15,000',
            pipelineValue: '€500,000',
            winRate: 75,
            revenueTrend: 'up',
            pipelineTrend: 'up',
            winRateTrend: 'stable'
        };
        
        jest.mocked(useRealTimeMetrics).mockReturnValue({
            metrics: mockMetrics,
            isLoading: false
        });
        
        render(<SalesCockpitV2Enhanced user={mockUser} />);
        
        expect(screen.getByText('€15,000')).toBeInTheDocument();
        expect(screen.getByText('€500,000')).toBeInTheDocument();
        expect(screen.getByText('75%')).toBeInTheDocument();
    });
});
```

### E2E Tests - Enhanced Cockpit Flow
```typescript
describe('Enhanced Cockpit E2E', () => {
    it('should provide complete enhanced cockpit experience', async () => {
        await page.goto('/cockpit');
        
        // Verify AI insights header
        await expect(page.locator('text=Good Morning')).toBeVisible();
        await expect(page.locator('[data-testid=performance-metrics]')).toBeVisible();
        
        // Test column functionality
        await expect(page.locator('[data-testid=my-day-column]')).toBeVisible();
        await expect(page.locator('[data-testid=focus-column]')).toBeVisible();
        await expect(page.locator('[data-testid=win-column]')).toBeVisible();
        
        // Test AI task prioritization
        await page.click('[data-testid=ai-suggested-filter]');
        await expect(page.locator('.task-card[data-ai-score]')).toBeVisible();
        
        // Test opportunity interaction
        const firstOpp = page.locator('.opportunity-card').first();
        await firstOpp.click();
        
        await expect(page.locator('[data-testid=opportunity-details]')).toBeVisible();
    });
    
    it('should support responsive design', async () => {
        await page.goto('/cockpit');
        
        // Test desktop layout
        await expect(page.locator('[data-panel-group]')).toBeVisible();
        
        // Test mobile layout
        await page.setViewportSize({ width: 768, height: 1024 });
        await expect(page.locator('[data-testid=mobile-cockpit]')).toBeVisible();
        
        // Test tablet layout
        await page.setViewportSize({ width: 1024, height: 768 });
        await expect(page.locator('[data-panel-group]')).toBeVisible();
    });
});
```

---

## 🚀 IMPLEMENTATION GUIDE

### Phase 1: AI Insights Header (1.5 Tage)
1. **AICockpitHeader Component** - Enhanced Header mit Performance Metrics
2. **Smart Insights Integration** - FC-002 API Integration für Daily Briefing
3. **Real-Time Metrics** - Live Performance Dashboard

### Phase 2: Enhanced Columns (2 Tage)
1. **MyDayColumnEnhanced** - AI Task Prioritization + Smart Filtering
2. **FocusColumnEnhanced** - Predictive Opportunity Scoring
3. **WinColumnEnhanced** - Real-time Pipeline Visualization

### Phase 3: AI Features Integration (1 Tag)
1. **Smart Insights Hooks** - useSmartInsights, useRealTimeMetrics
2. **Predictive Analytics** - AI Scoring Algorithms Integration
3. **Next Best Actions** - Action Recommendation Engine

### Phase 4: Testing & Polish (0.5 Tage)
1. **Component Tests** - Jest + React Testing Library
2. **E2E Tests** - Playwright Enhanced Cockpit Flows
3. **Performance Optimization** - Lazy Loading, Memoization

---

## 📊 SUCCESS CRITERIA

### Funktionale Anforderungen
- ✅ KI-enhanced 3-Spalten-Layout basierend auf exzellenter V2 Basis
- ✅ AI Task Prioritization mit Smart Filtering
- ✅ Predictive Opportunity Scoring mit Next Best Actions
- ✅ Real-time Performance Metrics Dashboard
- ✅ Responsive Design für alle Geräte
- ✅ Integration mit bestehender Navigation und Quick Actions

### Performance-Anforderungen
- ✅ Cockpit Load Time < 2s
- ✅ AI Insights Calculation < 500ms
- ✅ Real-time Updates < 1s
- ✅ Column Resize < 100ms
- ✅ Task/Opportunity Filtering < 200ms

### UX-Anforderungen
- ✅ Intuitive AI-enhanced Interface
- ✅ Clear Visual Hierarchy mit Smart Prioritization
- ✅ Smooth Animations für enhanced user experience
- ✅ Consistent Freshfoodz CI (#94C456, #004F7B)
- ✅ Accessibility (Screen Reader, Keyboard Navigation)

---

## 🔗 NAVIGATION ZU ALLEN 40 FEATURES

### Core Sales Features
- [FC-001 Customer Acquisition](/docs/features/PLANNED/01_customer_acquisition/FC-001_TECH_CONCEPT.md) | [FC-002 Smart Customer Insights](/docs/features/PLANNED/02_smart_insights/FC-002_TECH_CONCEPT.md) | [M4 Opportunity Pipeline](/docs/features/ACTIVE/02_opportunity_pipeline/M4_TECH_CONCEPT.md) | [FC-004 Verkäuferschutz](/docs/features/PLANNED/07_verkaeuferschutz/FC-004_TECH_CONCEPT.md)
- [FC-013 Duplicate Detection](/docs/features/PLANNED/15_duplicate_detection/FC-013_TECH_CONCEPT.md) | [FC-014 Activity Timeline](/docs/features/PLANNED/16_activity_timeline/FC-014_TECH_CONCEPT.md) | [FC-015 Deal Loss Analysis](/docs/features/PLANNED/17_deal_loss_analysis/FC-015_TECH_CONCEPT.md)
- [FC-016 Opportunity Cloning](/docs/features/PLANNED/18_opportunity_cloning/FC-016_TECH_CONCEPT.md) | [FC-019 Advanced Sales Metrics](/docs/features/PLANNED/19_advanced_metrics/FC-019_TECH_CONCEPT.md)

### Security & Foundation Features  
- [FC-008 Security Foundation](/docs/features/ACTIVE/01_security_foundation/FC-008_TECH_CONCEPT.md) | [FC-009 Advanced Permissions](/docs/features/ACTIVE/04_permissions_system/FC-009_TECH_CONCEPT.md) | [FC-025 DSGVO Compliance](/docs/features/PLANNED/25_dsgvo_compliance/FC-025_TECH_CONCEPT.md)
- [FC-023 Event Sourcing](/docs/features/PLANNED/23_event_sourcing/FC-023_TECH_CONCEPT.md) | [FC-038 Multi-Tenant Architecture](/docs/features/PLANNED/38_multitenant/FC-038_TECH_CONCEPT.md) | [FC-039 API Gateway](/docs/features/PLANNED/39_api_gateway/FC-039_TECH_CONCEPT.md)

### Mobile & Field Service Features
- [FC-006 Mobile App](/docs/features/PLANNED/09_mobile_app/FC-006_TECH_CONCEPT.md) | [FC-018 Mobile PWA](/docs/features/PLANNED/09_mobile_app/FC-018_MOBILE_FIELD_SALES.md) | [FC-022 Mobile Light](/docs/features/PLANNED/22_mobile_light/FC-022_TECH_CONCEPT.md)
- [FC-029 Voice-First Interface](/docs/features/PLANNED/29_voice_first/FC-029_TECH_CONCEPT.md) | [FC-030 One-Tap Actions](/docs/features/PLANNED/30_one_tap_actions/FC-030_TECH_CONCEPT.md) | [FC-032 Offline-First](/docs/features/PLANNED/32_offline_first/FC-032_TECH_CONCEPT.md)

### Communication Features
- [FC-003 E-Mail Integration](/docs/features/PLANNED/06_email_integration/FC-003_TECH_CONCEPT.md) | [FC-012 Team Communication](/docs/features/PLANNED/14_team_communication/FC-012_TECH_CONCEPT.md) | [FC-028 WhatsApp Business](/docs/features/PLANNED/28_whatsapp_integration/FC-028_TECH_CONCEPT.md)
- [FC-035 Social Selling Helper](/docs/features/PLANNED/35_social_selling/FC-035_TECH_CONCEPT.md)

### Analytics & Intelligence Features
- [M6 Analytics Module](/docs/features/PLANNED/13_analytics_m6/M6_TECH_CONCEPT.md) | [FC-007 Chef-Dashboard](/docs/features/PLANNED/10_chef_dashboard/FC-007_TECH_CONCEPT.md) | [FC-026 Analytics Platform](/docs/features/PLANNED/26_analytics_platform/FC-026_TECH_CONCEPT.md)
- [FC-034 Instant Insights](/docs/features/PLANNED/34_instant_insights/FC-034_TECH_CONCEPT.md) | [FC-037 Advanced Reporting](/docs/features/PLANNED/37_advanced_reporting/FC-037_TECH_CONCEPT.md) | [FC-040 Performance Monitoring](/docs/features/PLANNED/40_performance_monitoring/FC-040_TECH_CONCEPT.md)

### Integration & Infrastructure Features
- [FC-005 Xentral Integration](/docs/features/PLANNED/08_xentral_integration/FC-005_TECH_CONCEPT.md) | [FC-010 Customer Import](/docs/features/PLANNED/11_customer_import/FC-010_TECH_CONCEPT.md) | [FC-021 Integration Hub](/docs/features/PLANNED/21_integration_hub/FC-021_TECH_CONCEPT.md)
- [FC-024 File Management](/docs/features/PLANNED/24_file_management/FC-024_TECH_CONCEPT.md) | [M5 Customer Refactor](/docs/features/PLANNED/12_customer_refactor_m5/M5_TECH_CONCEPT.md)

### UI & Productivity Features
- [M1 Navigation](/docs/features/ACTIVE/05_ui_foundation/M1_TECH_CONCEPT.md) | [M2 Quick Create](/docs/features/ACTIVE/05_ui_foundation/M2_TECH_CONCEPT.md) | [M7 Settings](/docs/features/ACTIVE/05_ui_foundation/M7_SETTINGS_TECH_CONCEPT.md)
- [FC-020 Quick Wins](/docs/features/PLANNED/20_quick_wins/FC-020_TECH_CONCEPT.md) | [FC-031 Smart Templates](/docs/features/PLANNED/31_smart_templates/FC-031_TECH_CONCEPT.md) | [FC-033 Visual Customer Cards](/docs/features/PLANNED/33_visual_cards/FC-033_TECH_CONCEPT.md)

### Advanced Features
- [FC-011 Bonitätsprüfung](/docs/features/ACTIVE/02_opportunity_pipeline/integrations/FC-011_TECH_CONCEPT.md) | [FC-017 Sales Gamification](/docs/features/PLANNED/99_sales_gamification/FC-017_TECH_CONCEPT.md) | [FC-027 Magic Moments](/docs/features/PLANNED/27_magic_moments/FC-027_TECH_CONCEPT.md)
- [FC-036 Beziehungsmanagement](/docs/features/PLANNED/36_relationship_mgmt/FC-036_TECH_CONCEPT.md) | [M8 Calculator Modal](/docs/features/ACTIVE/03_calculator_modal/M8_TECH_CONCEPT.md)

---

**🎛️ M3 Sales Cockpit - Ready for AI Enhancement!**  
**⏱️ Geschätzter Aufwand:** 4-5 Tage | **Basis:** 60% exzellent fertig | **Enhancement:** KI + Real-Time + Predictive Analytics