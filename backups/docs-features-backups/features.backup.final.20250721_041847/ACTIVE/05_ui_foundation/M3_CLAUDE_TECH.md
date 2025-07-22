# M3 Sales Cockpit - CLAUDE TECH 🤖

## 🧠 QUICK-LOAD (30 Sekunden bis zur Produktivität)

**Feature:** KI-Enhanced Sales Command Center mit 3-Spalten Layout  
**Stack:** React + ResizablePanels + MUI + Real-time Hooks + AI Integration  
**Status:** 🟡 60% FERTIG - Enhancement bestehender Exzellenz  
**Dependencies:** FC-008 Security (✅) | M1 Navigation (✅) | FC-002 Smart Insights  

**Jump to:** [📚 Recipes](#-implementation-recipes) | [🧪 Tests](#-test-patterns) | [🔌 Integration](#-integration-cookbook) | [🎯 AI Features](#-ai-enhancement-recipes)

**Core Purpose in 1 Line:** `AI Insights → Smart Prioritization → Real-time Metrics → Action Recommendations`

---

## 🍳 IMPLEMENTATION RECIPES

### Recipe 1: Enhanced Sales Cockpit in 5 Minuten
```typescript
// 1. Enhanced Sales Cockpit (builds on excellent V2) - copy-paste ready
export const SalesCockpitV2Enhanced: React.FC = () => {
    const { user } = useAuth();
    const { insights, isLoading: insightsLoading } = useSmartInsights(user.id);
    const { metrics, isLoading: metricsLoading } = useRealTimeMetrics(user.id);
    const { opportunities } = useOpportunityPipeline(user.id);
    const { tasks } = useMyDayTasks(user.id);
    
    if (insightsLoading || metricsLoading) {
        return <CockpitSkeleton />;
    }
    
    return (
        <Box sx={{ height: '100vh', display: 'flex', flexDirection: 'column' }}>
            {/* AI Insights Header */}
            <AICockpitHeader 
                user={user} 
                insights={insights} 
                metrics={metrics}
                sx={{ height: '120px', mb: 2 }}
            />
            
            {/* Existing 3-Column Excellence with AI Enhancement */}
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

// 2. Smart Insights Hook
export const useSmartInsights = (userId: string) => {
    return useQuery({
        queryKey: ['smart-insights', userId],
        queryFn: async () => {
            const response = await apiClient.get(`/api/insights/user/${userId}`);
            return response.data;
        },
        refetchInterval: 60000, // Update every minute
        staleTime: 30000
    });
};

// 3. Real-time Metrics Hook
export const useRealTimeMetrics = (userId: string) => {
    const queryClient = useQueryClient();
    
    // Initial data fetch
    const query = useQuery({
        queryKey: ['metrics', userId],
        queryFn: () => apiClient.get(`/api/metrics/user/${userId}`).then(r => r.data)
    });
    
    // WebSocket for real-time updates
    useEffect(() => {
        const ws = new WebSocket(`ws://localhost:8080/metrics/${userId}`);
        
        ws.onmessage = (event) => {
            const metrics = JSON.parse(event.data);
            queryClient.setQueryData(['metrics', userId], metrics);
        };
        
        return () => ws.close();
    }, [userId, queryClient]);
    
    return query;
};
```

### Recipe 2: AI Insights Header Component
```typescript
// 4. AI Cockpit Header with Performance Metrics (copy-paste ready)
export const AICockpitHeader: React.FC<{
    user: User;
    insights: SmartInsights;
    metrics: RealTimeMetrics;
    sx?: SxProps;
}> = ({ user, insights, metrics, sx }) => {
    const [alertsExpanded, setAlertsExpanded] = useState(false);
    
    return (
        <Paper sx={{ 
            p: 2, 
            background: 'linear-gradient(135deg, #94C456 0%, #004F7B 100%)', 
            ...sx 
        }}>
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
                                <MetricDisplay 
                                    value={metrics.todayRevenue}
                                    label="Today's Revenue"
                                    trend={metrics.revenueTrend}
                                />
                            </Grid>
                            <Grid item xs={4}>
                                <MetricDisplay 
                                    value={metrics.pipelineValue}
                                    label="Pipeline Value"
                                    trend={metrics.pipelineTrend}
                                />
                            </Grid>
                            <Grid item xs={4}>
                                <MetricDisplay 
                                    value={`${metrics.winRate}%`}
                                    label="Win Rate"
                                    trend={metrics.winRateTrend}
                                />
                            </Grid>
                        </Grid>
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
                    </Box>
                </Grid>
            </Grid>
            
            {/* Expandable Alerts */}
            <Collapse in={alertsExpanded}>
                <Paper sx={{ mt: 2, p: 1, maxHeight: 200, overflowY: 'auto' }}>
                    {insights.alerts.map((alert, index) => (
                        <Alert 
                            key={index} 
                            severity={alert.severity} 
                            sx={{ mb: 0.5 }}
                        >
                            {alert.message}
                        </Alert>
                    ))}
                </Paper>
            </Collapse>
        </Paper>
    );
};
```

### Recipe 3: Enhanced Column Components
```typescript
// 5. Enhanced MyDay Column with AI Prioritization (copy-paste ready)
export const MyDayColumnEnhanced: React.FC<{
    tasks: Task[];
    insights: SmartInsights;
    user: User;
}> = ({ tasks, insights, user }) => {
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
                title="My Day"
                subheader={`${filteredTasks.length} tasks`}
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
                        <ToggleButton value="all">All</ToggleButton>
                    </ToggleButtonGroup>
                }
            />
            
            <CardContent sx={{ flex: 1, overflowY: 'auto', pt: 0 }}>
                {filteredTasks.map((task) => (
                    <TaskCard 
                        key={task.id}
                        task={task}
                        onComplete={() => handleTaskComplete(task)}
                        sx={{ mb: 1 }}
                    />
                ))}
            </CardContent>
        </Card>
    );
};

// 6. Task Card with AI Score
const TaskCard: React.FC<{
    task: EnhancedTask;
    onComplete: () => void;
    sx?: SxProps;
}> = ({ task, onComplete, sx }) => (
    <Card 
        sx={{ 
            border: task.aiScore > 0.8 ? '2px solid #f44336' : '1px solid #e0e0e0',
            ...sx 
        }}
    >
        <CardContent sx={{ pb: 1 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <Checkbox checked={task.completed} onChange={onComplete} />
                <Box sx={{ flex: 1 }}>
                    <Typography variant="body1">{task.title}</Typography>
                    <Box sx={{ display: 'flex', gap: 1, mt: 0.5 }}>
                        <Chip 
                            label={`${Math.round(task.aiScore * 100)}%`}
                            size="small"
                            color={task.aiScore > 0.8 ? 'error' : 'default'}
                        />
                        <Chip 
                            label={`${task.timeToComplete}min`}
                            size="small"
                            icon={<AccessTimeIcon />}
                        />
                    </Box>
                </Box>
            </Box>
        </CardContent>
    </Card>
);
```

---

## 🧪 TEST PATTERNS

### Pattern 1: Enhanced Cockpit Tests
```typescript
describe('SalesCockpitV2Enhanced', () => {
    it('should render AI insights header', () => {
        const mockInsights = {
            dailyBrief: 'Focus on high-priority deals',
            priorityActions: 3,
            alerts: []
        };
        
        render(<SalesCockpitV2Enhanced />);
        
        expect(screen.getByText(/Good Morning/)).toBeInTheDocument();
        expect(screen.getByText('Focus on high-priority deals')).toBeInTheDocument();
    });
    
    it('should maintain ResizablePanels functionality', () => {
        render(<SalesCockpitV2Enhanced />);
        
        expect(screen.getByText('My Day')).toBeInTheDocument();
        expect(screen.getByText('Focus Pipeline')).toBeInTheDocument();
        expect(document.querySelectorAll('[data-panel]')).toHaveLength(3);
    });
});
```

### Pattern 2: AI Integration Tests
```typescript
describe('AI Features', () => {
    it('should prioritize tasks by AI score', () => {
        const tasks = [
            { id: '1', title: 'Low priority', aiScore: 0.3 },
            { id: '2', title: 'High priority', aiScore: 0.9 }
        ];
        
        render(<MyDayColumnEnhanced tasks={tasks} insights={mockInsights} />);
        
        const taskElements = screen.getAllByText(/priority/);
        expect(taskElements[0]).toHaveTextContent('High priority');
    });
});
```

---

## 🔌 INTEGRATION COOKBOOK

### Mit bestehender V2 Implementation
```typescript
// Gradual migration from V2 to Enhanced
export const SalesCockpit: React.FC = () => {
    const { featureFlags } = useFeatureFlags();
    
    if (featureFlags.aiEnhancedCockpit) {
        return <SalesCockpitV2Enhanced />;
    }
    
    return <SalesCockpitV2 />; // Existing implementation
};
```

### Mit Navigation (M1)
```typescript
// Update navigation to highlight active cockpit
export const navigationItems = [
    {
        path: '/cockpit',
        label: 'Sales Command Center',
        icon: <DashboardIcon />,
        badge: insights?.priorityActions // Show action count
    }
];
```

### Mit Quick Actions (M2)
```typescript
// Add quick actions to cockpit header
<AICockpitHeader>
    <QuickActionButtons 
        actions={['create-opportunity', 'add-task', 'send-email']}
        context="cockpit"
    />
</AICockpitHeader>
```

---

## 🎯 AI ENHANCEMENT RECIPES

### Recipe 4: Opportunity Scoring
```typescript
// Enhanced Opportunity Card with AI (copy-paste ready)
export const OpportunityCard: React.FC<{
    opportunity: EnhancedOpportunity;
}> = ({ opportunity }) => {
    const scoreColor = opportunity.aiScore > 0.8 ? '#f44336' : 
                      opportunity.aiScore > 0.6 ? '#ff9800' : '#2196f3';
    
    return (
        <Card sx={{ border: `2px solid ${scoreColor}` }}>
            <Box sx={{
                position: 'absolute',
                top: -8,
                right: 8,
                bgcolor: scoreColor,
                color: 'white',
                borderRadius: '50%',
                width: 32,
                height: 32,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center'
            }}>
                {Math.round(opportunity.aiScore * 100)}
            </Box>
            
            <CardContent>
                <Typography variant="h6">{opportunity.name}</Typography>
                <Typography variant="body2">{opportunity.customer.name}</Typography>
                
                <Box sx={{ mt: 1 }}>
                    <Chip label={formatCurrency(opportunity.value)} />
                    <Chip label={`${opportunity.winProbability}% Win`} />
                </Box>
                
                <Alert severity="info" sx={{ mt: 1 }}>
                    💡 {opportunity.nextBestAction}
                </Alert>
            </CardContent>
        </Card>
    );
};
```

### Recipe 5: Real-time Updates
```typescript
// WebSocket integration for live updates
export const useRealTimeUpdates = (cockpitId: string) => {
    const queryClient = useQueryClient();
    
    useEffect(() => {
        const ws = new WebSocket(`ws://localhost:8080/cockpit/${cockpitId}`);
        
        ws.onmessage = (event) => {
            const update = JSON.parse(event.data);
            
            switch (update.type) {
                case 'TASK_UPDATE':
                    queryClient.invalidateQueries(['tasks']);
                    break;
                case 'OPPORTUNITY_UPDATE':
                    queryClient.invalidateQueries(['opportunities']);
                    break;
                case 'METRICS_UPDATE':
                    queryClient.setQueryData(['metrics'], update.data);
                    break;
            }
        };
        
        return () => ws.close();
    }, [cockpitId, queryClient]);
};
```

---

## 📚 DEEP KNOWLEDGE (Bei Bedarf expandieren)

<details>
<summary>🎨 Advanced UI Features</summary>

### Drag & Drop Task Management
```typescript
export const DraggableTaskList: React.FC = () => {
    const [tasks, setTasks] = useState<Task[]>([]);
    
    const onDragEnd = (result: DropResult) => {
        if (!result.destination) return;
        
        const items = Array.from(tasks);
        const [reorderedItem] = items.splice(result.source.index, 1);
        items.splice(result.destination.index, 0, reorderedItem);
        
        setTasks(items);
        updateTaskPriorities(items);
    };
    
    return (
        <DragDropContext onDragEnd={onDragEnd}>
            <Droppable droppableId="tasks">
                {(provided) => (
                    <div {...provided.droppableProps} ref={provided.innerRef}>
                        {tasks.map((task, index) => (
                            <Draggable key={task.id} draggableId={task.id} index={index}>
                                {(provided) => (
                                    <div
                                        ref={provided.innerRef}
                                        {...provided.draggableProps}
                                        {...provided.dragHandleProps}
                                    >
                                        <TaskCard task={task} />
                                    </div>
                                )}
                            </Draggable>
                        ))}
                        {provided.placeholder}
                    </div>
                )}
            </Droppable>
        </DragDropContext>
    );
};
```

</details>

<details>
<summary>📊 Performance Optimization</summary>

### Virtual Scrolling for Large Lists
```typescript
import { FixedSizeList } from 'react-window';

export const VirtualizedTaskList: React.FC<{ tasks: Task[] }> = ({ tasks }) => {
    const Row = ({ index, style }) => (
        <div style={style}>
            <TaskCard task={tasks[index]} />
        </div>
    );
    
    return (
        <FixedSizeList
            height={600}
            itemCount={tasks.length}
            itemSize={100}
            width="100%"
        >
            {Row}
        </FixedSizeList>
    );
};
```

### Memoization for Expensive Calculations
```typescript
const expensiveInsights = useMemo(() => {
    return calculateComplexInsights(opportunities, tasks, metrics);
}, [opportunities, tasks, metrics]);
```

</details>

---

**🎯 Nächster Schritt:** AICockpitHeader Component implementieren und mit bestehender V2 integrieren