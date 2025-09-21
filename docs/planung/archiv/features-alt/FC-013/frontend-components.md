# FC-013: Frontend Components für Activity & Notes System

**Parent:** [/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-24_TECH_CONCEPT_FC-013-activity-notes-system.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-24_TECH_CONCEPT_FC-013-activity-notes-system.md)  
**Fokus:** React UI Components für Activities

## 🎨 Activity Components

### 1. Activity Timeline Component
```typescript
// frontend/src/features/activity/components/ActivityTimeline.tsx
interface ActivityTimelineProps {
  entityType: 'opportunity' | 'customer' | 'contract';
  entityId: string;
  showQuickActions?: boolean;
  maxHeight?: number;
}

export const ActivityTimeline: React.FC<ActivityTimelineProps> = ({
  entityType,
  entityId,
  showQuickActions = true,
  maxHeight = 600
}) => {
  const { data, fetchNextPage, hasNextPage, isFetchingNextPage } = 
    useInfiniteQuery({
      queryKey: ['activities', entityType, entityId],
      queryFn: ({ pageParam = 0 }) => 
        activityApi.getActivities(entityType, entityId, pageParam),
      getNextPageParam: (lastPage) => 
        lastPage.hasMore ? lastPage.page + 1 : undefined,
    });

  const activities = data?.pages.flatMap(page => page.items) ?? [];

  return (
    <Box sx={{ height: maxHeight, display: 'flex', flexDirection: 'column' }}>
      {showQuickActions && (
        <QuickActionBar 
          entityType={entityType} 
          entityId={entityId} 
        />
      )}
      
      <Timeline sx={{ flex: 1, overflowY: 'auto', p: 2 }}>
        {activities.map((activity, index) => (
          <TimelineItem key={activity.id}>
            <TimelineOppositeContent>
              <Typography variant="caption" color="text.secondary">
                {formatRelativeTime(activity.createdAt)}
              </Typography>
            </TimelineOppositeContent>
            
            <TimelineSeparator>
              <TimelineDot 
                color={getActivityColor(activity.type)}
                variant={activity.status === 'COMPLETED' ? 'filled' : 'outlined'}
              >
                {getActivityIcon(activity.type)}
              </TimelineDot>
              {index < activities.length - 1 && <TimelineConnector />}
            </TimelineSeparator>
            
            <TimelineContent>
              <ActivityCard activity={activity} />
            </TimelineContent>
          </TimelineItem>
        ))}
        
        {hasNextPage && (
          <Box sx={{ textAlign: 'center', py: 2 }}>
            <Button 
              onClick={() => fetchNextPage()}
              disabled={isFetchingNextPage}
            >
              {isFetchingNextPage ? 'Lädt...' : 'Mehr laden'}
            </Button>
          </Box>
        )}
      </Timeline>
    </Box>
  );
};
```

### 2. Activity Card Component
```typescript
// frontend/src/features/activity/components/ActivityCard.tsx
export const ActivityCard: React.FC<{ activity: Activity }> = ({ activity }) => {
  const [expanded, setExpanded] = useState(false);
  const { user } = useAuth();
  const queryClient = useQueryClient();
  
  const { mutate: completeTask } = useMutation({
    mutationFn: (data: { activityId: string; note: string }) => 
      activityApi.completeTask(data.activityId, data.note),
    onSuccess: () => {
      queryClient.invalidateQueries(['activities']);
      toast.success('Aufgabe abgeschlossen');
    }
  });

  const isTask = ['TASK_CREATED', 'REMINDER_SET'].includes(activity.type);
  const canComplete = isTask && 
    activity.status === 'PENDING' && 
    activity.assignedTo?.id === user.id;

  return (
    <Card 
      sx={{ 
        mb: 1,
        backgroundColor: activity.status === 'COMPLETED' ? 'grey.50' : 'white'
      }}
    >
      <CardContent>
        <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
          <Box sx={{ flex: 1 }}>
            <Typography variant="subtitle2" component="div">
              {activity.title}
              {activity.status === 'COMPLETED' && (
                <Chip 
                  label="Erledigt" 
                  size="small" 
                  color="success" 
                  sx={{ ml: 1 }} 
                />
              )}
            </Typography>
            
            <Typography variant="caption" color="text.secondary">
              {activity.createdBy.name} • {formatDateTime(activity.createdAt)}
            </Typography>
            
            {activity.dueDate && (
              <Box sx={{ mt: 0.5 }}>
                <Chip
                  icon={<AccessTimeIcon />}
                  label={`Fällig: ${formatDate(activity.dueDate)}`}
                  size="small"
                  color={isPastDue(activity.dueDate) ? 'error' : 'default'}
                />
              </Box>
            )}
          </Box>
          
          <Box>
            {canComplete && (
              <IconButton 
                color="success"
                onClick={() => handleComplete(activity)}
                size="small"
              >
                <CheckCircleIcon />
              </IconButton>
            )}
            
            <IconButton 
              onClick={() => setExpanded(!expanded)}
              size="small"
            >
              {expanded ? <ExpandLessIcon /> : <ExpandMoreIcon />}
            </IconButton>
          </Box>
        </Box>
        
        <Collapse in={expanded}>
          <Box sx={{ mt: 2 }}>
            {activity.content && (
              <Typography 
                variant="body2" 
                sx={{ whiteSpace: 'pre-wrap', mb: 2 }}
              >
                {activity.content}
              </Typography>
            )}
            
            {activity.metadata && (
              <ActivityMetadata metadata={activity.metadata} />
            )}
            
            {activity.attachments?.length > 0 && (
              <AttachmentList attachments={activity.attachments} />
            )}
          </Box>
        </Collapse>
      </CardContent>
    </Card>
  );
};
```

### 3. Quick Action Bar
```typescript
// frontend/src/features/activity/components/QuickActionBar.tsx
const QUICK_ACTIONS = [
  {
    key: 'CUSTOMER_INFORMED',
    label: 'Kunde informiert',
    icon: <InfoIcon />,
    color: 'info' as const
  },
  {
    key: 'CONTRACT_SENT',
    label: 'Vertrag versendet',
    icon: <SendIcon />,
    color: 'success' as const
  },
  {
    key: 'FOLLOW_UP_NEEDED',
    label: 'Follow-up',
    icon: <EventIcon />,
    color: 'warning' as const
  },
  {
    key: 'ADD_NOTE',
    label: 'Notiz',
    icon: <NoteAddIcon />,
    color: 'default' as const
  }
];

export const QuickActionBar: React.FC<{
  entityType: string;
  entityId: string;
}> = ({ entityType, entityId }) => {
  const [selectedAction, setSelectedAction] = useState<string | null>(null);
  
  const { mutate: createActivity } = useMutation({
    mutationFn: activityApi.createActivity,
    onSuccess: () => {
      queryClient.invalidateQueries(['activities', entityType, entityId]);
      toast.success('Aktivität hinzugefügt');
    }
  });

  const handleQuickAction = async (actionKey: string) => {
    if (actionKey === 'ADD_NOTE') {
      setSelectedAction('ADD_NOTE');
      return;
    }
    
    if (actionKey === 'FOLLOW_UP_NEEDED') {
      setSelectedAction('FOLLOW_UP_NEEDED');
      return;
    }
    
    // Simple checkbox actions
    const variables = await getActionVariables(actionKey);
    
    createActivity({
      entityType,
      entityId,
      templateKey: actionKey,
      variables
    });
  };

  return (
    <Box sx={{ p: 1, borderBottom: 1, borderColor: 'divider' }}>
      <Stack direction="row" spacing={1}>
        {QUICK_ACTIONS.map(action => (
          <Button
            key={action.key}
            size="small"
            variant="outlined"
            color={action.color}
            startIcon={action.icon}
            onClick={() => handleQuickAction(action.key)}
          >
            {action.label}
          </Button>
        ))}
      </Stack>
      
      <AddNoteDialog
        open={selectedAction === 'ADD_NOTE'}
        onClose={() => setSelectedAction(null)}
        onSubmit={(note) => {
          createActivity({
            entityType,
            entityId,
            type: 'NOTE',
            title: 'Notiz',
            content: note
          });
          setSelectedAction(null);
        }}
      />
      
      <CreateTaskDialog
        open={selectedAction === 'FOLLOW_UP_NEEDED'}
        onClose={() => setSelectedAction(null)}
        onSubmit={(task) => {
          createActivity({
            entityType,
            entityId,
            type: 'TASK_CREATED',
            ...task
          });
          setSelectedAction(null);
        }}
      />
    </Box>
  );
};
```

### 4. Activity Filter & Search
```typescript
// frontend/src/features/activity/components/ActivityFilter.tsx
export const ActivityFilter: React.FC<{
  onFilterChange: (filters: ActivityFilters) => void;
}> = ({ onFilterChange }) => {
  const [filters, setFilters] = useState<ActivityFilters>({
    types: [],
    users: [],
    dateRange: null,
    status: 'all',
    searchText: ''
  });

  const debouncedSearch = useDebouncedCallback((text: string) => {
    updateFilter('searchText', text);
  }, 300);

  return (
    <Paper sx={{ p: 2, mb: 2 }}>
      <Grid container spacing={2} alignItems="center">
        <Grid item xs={12} md={4}>
          <TextField
            fullWidth
            size="small"
            placeholder="Suche in Aktivitäten..."
            InputProps={{
              startAdornment: <SearchIcon sx={{ mr: 1 }} />
            }}
            onChange={(e) => debouncedSearch(e.target.value)}
          />
        </Grid>
        
        <Grid item xs={12} md={2}>
          <FormControl fullWidth size="small">
            <InputLabel>Status</InputLabel>
            <Select
              value={filters.status}
              onChange={(e) => updateFilter('status', e.target.value)}
            >
              <MenuItem value="all">Alle</MenuItem>
              <MenuItem value="pending">Offen</MenuItem>
              <MenuItem value="completed">Erledigt</MenuItem>
              <MenuItem value="overdue">Überfällig</MenuItem>
            </Select>
          </FormControl>
        </Grid>
        
        <Grid item xs={12} md={3}>
          <Autocomplete
            multiple
            size="small"
            options={ACTIVITY_TYPES}
            value={filters.types}
            onChange={(_, value) => updateFilter('types', value)}
            renderInput={(params) => (
              <TextField {...params} label="Aktivitätstypen" />
            )}
          />
        </Grid>
        
        <Grid item xs={12} md={3}>
          <DateRangePicker
            value={filters.dateRange}
            onChange={(range) => updateFilter('dateRange', range)}
          />
        </Grid>
      </Grid>
    </Paper>
  );
};
```

### 5. Task Dashboard Widget
```typescript
// frontend/src/features/activity/components/TaskDashboard.tsx
export const TaskDashboard: React.FC = () => {
  const { user } = useAuth();
  const { data: tasks } = useQuery({
    queryKey: ['myTasks', user.id],
    queryFn: () => activityApi.getMyTasks()
  });

  const groupedTasks = useMemo(() => {
    if (!tasks) return {};
    
    return {
      overdue: tasks.filter(t => isPastDue(t.dueDate)),
      today: tasks.filter(t => isToday(t.dueDate)),
      upcoming: tasks.filter(t => isFuture(t.dueDate) && !isPastDue(t.dueDate))
    };
  }, [tasks]);

  return (
    <Card>
      <CardHeader 
        title="Meine Aufgaben" 
        avatar={<TaskIcon />}
        action={
          <IconButton size="small">
            <RefreshIcon />
          </IconButton>
        }
      />
      <CardContent>
        <List>
          {groupedTasks.overdue?.length > 0 && (
            <>
              <ListSubheader sx={{ color: 'error.main' }}>
                Überfällig ({groupedTasks.overdue.length})
              </ListSubheader>
              {groupedTasks.overdue.map(task => (
                <TaskListItem key={task.id} task={task} highlight="error" />
              ))}
            </>
          )}
          
          {groupedTasks.today?.length > 0 && (
            <>
              <ListSubheader sx={{ color: 'warning.main' }}>
                Heute ({groupedTasks.today.length})
              </ListSubheader>
              {groupedTasks.today.map(task => (
                <TaskListItem key={task.id} task={task} highlight="warning" />
              ))}
            </>
          )}
          
          {groupedTasks.upcoming?.length > 0 && (
            <>
              <ListSubheader>
                Demnächst ({groupedTasks.upcoming.length})
              </ListSubheader>
              {groupedTasks.upcoming.slice(0, 5).map(task => (
                <TaskListItem key={task.id} task={task} />
              ))}
            </>
          )}
        </List>
      </CardContent>
    </Card>
  );
};
```

## 🎯 Integration in Opportunity Card

### Enhanced Opportunity Card
```typescript
// Extension of existing OpportunityCard
export const OpportunityCardWithActivities: React.FC<{
  opportunity: Opportunity;
}> = ({ opportunity }) => {
  const [showActivities, setShowActivities] = useState(false);
  const { data: recentActivity } = useQuery({
    queryKey: ['recentActivity', opportunity.id],
    queryFn: () => activityApi.getMostRecentActivity('opportunity', opportunity.id)
  });

  return (
    <Card>
      <CardContent>
        {/* Existing opportunity content */}
        
        {/* Activity indicators */}
        <Box sx={{ mt: 2, display: 'flex', gap: 1 }}>
          {recentActivity?.type === 'CONTRACT_SENT' && (
            <Chip 
              icon={<CheckIcon />} 
              label="Vertrag versendet" 
              size="small" 
              color="success" 
            />
          )}
          
          {recentActivity?.type === 'CUSTOMER_INFORMED' && (
            <Chip 
              icon={<InfoIcon />} 
              label="Kunde informiert" 
              size="small" 
              color="info" 
            />
          )}
          
          {opportunity.daysSinceLastActivity > 14 && (
            <Chip 
              icon={<WarningIcon />} 
              label={`${opportunity.daysSinceLastActivity} Tage inaktiv`} 
              size="small" 
              color="warning" 
            />
          )}
        </Box>
        
        <Box sx={{ mt: 1 }}>
          <Button 
            size="small" 
            startIcon={<HistoryIcon />}
            onClick={() => setShowActivities(!showActivities)}
          >
            Aktivitäten ({opportunity.activityCount})
          </Button>
        </Box>
      </CardContent>
      
      <Collapse in={showActivities}>
        <Divider />
        <Box sx={{ maxHeight: 300, overflow: 'auto' }}>
          <ActivityTimeline
            entityType="opportunity"
            entityId={opportunity.id}
            showQuickActions={false}
            maxHeight={300}
          />
        </Box>
      </Collapse>
    </Card>
  );
};
```

## 🔔 Notification Components

### Task Reminder Toast
```typescript
export const useTaskReminders = () => {
  const { user } = useAuth();
  
  useEffect(() => {
    const ws = new WebSocket(`wss://api.freshplan.de/reminders/${user.id}`);
    
    ws.onmessage = (event) => {
      const reminder = JSON.parse(event.data);
      
      toast.warning(
        <Box>
          <Typography variant="subtitle2">
            Erinnerung: {reminder.task.title}
          </Typography>
          <Typography variant="caption">
            Fällig: {formatRelativeTime(reminder.task.dueDate)}
          </Typography>
          <Box sx={{ mt: 1 }}>
            <Button 
              size="small" 
              onClick={() => navigateToTask(reminder.task)}
            >
              Zur Aufgabe
            </Button>
          </Box>
        </Box>,
        { autoClose: false }
      );
    };
    
    return () => ws.close();
  }, [user.id]);
};
```