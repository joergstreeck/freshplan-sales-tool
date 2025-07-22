# M4 Opportunity Pipeline - CLAUDE TECH 🤖

## 🧠 QUICK-LOAD (30 Sekunden bis zur Produktivität)

**Feature:** Kanban Board für Sales Pipeline mit 5 Stages + Drag & Drop + Bonitätsprüfung  
**Stack:** React DnD + Quarkus REST + PostgreSQL + React Query  
**Status:** 📋 Ready to Implement (nach FC-008 Security)  
**Dependencies:** FC-008 Security, FC-009 Permissions | Integriert: FC-011 Bonitätsprüfung, M8 Calculator  

**Jump to:** [📚 Recipes](#-implementation-recipes) | [🧪 Tests](#-test-patterns) | [🔌 Integration](#-integration-cookbook) | [🎯 Drag & Drop](#-drag--drop-patterns)

**Core Purpose in 1 Line:** `Opportunity Cards → Drag between Stages → Validate Transitions → Update Backend → Real-time Sync`

---

## 🍳 IMPLEMENTATION RECIPES

### Recipe 1: Kanban Board in 5 Minuten
```typescript
// 1. Opportunity Board Component (copy-paste ready)
export const OpportunityBoard: React.FC = () => {
    const { data: opportunities, isLoading } = useOpportunities();
    const moveOpportunity = useMoveOpportunity();
    
    const stages: Stage[] = ['LEAD', 'QUALIFIED', 'PROPOSAL', 'NEGOTIATION', 'CLOSED'];
    
    const handleDragEnd = (result: DropResult) => {
        if (!result.destination) return;
        
        const { draggableId, source, destination } = result;
        
        if (source.droppableId !== destination.droppableId) {
            moveOpportunity.mutate({
                opportunityId: draggableId,
                fromStage: source.droppableId as Stage,
                toStage: destination.droppableId as Stage
            });
        }
    };
    
    if (isLoading) return <BoardSkeleton />;
    
    return (
        <DragDropContext onDragEnd={handleDragEnd}>
            <div className="flex gap-4 h-full overflow-x-auto p-4">
                {stages.map(stage => (
                    <KanbanColumn
                        key={stage}
                        stage={stage}
                        opportunities={opportunities?.filter(o => o.stage === stage) || []}
                    />
                ))}
            </div>
        </DragDropContext>
    );
};

// 2. Kanban Column Component
export const KanbanColumn: React.FC<{
    stage: Stage;
    opportunities: Opportunity[];
}> = ({ stage, opportunities }) => {
    const stageConfig = getStageConfig(stage);
    
    return (
        <div className="flex-1 min-w-[300px] bg-gray-50 rounded-lg">
            <div className="p-4 border-b bg-white rounded-t-lg">
                <div className="flex items-center justify-between">
                    <h3 className="font-semibold flex items-center gap-2">
                        {stageConfig.icon}
                        {stageConfig.label}
                    </h3>
                    <Badge variant="secondary">{opportunities.length}</Badge>
                </div>
                <div className="text-sm text-gray-500 mt-1">
                    €{opportunities.reduce((sum, o) => sum + o.value, 0).toLocaleString()}
                </div>
            </div>
            
            <Droppable droppableId={stage}>
                {(provided, snapshot) => (
                    <div
                        ref={provided.innerRef}
                        {...provided.droppableProps}
                        className={cn(
                            "p-2 min-h-[200px] space-y-2",
                            snapshot.isDraggingOver && "bg-blue-50"
                        )}
                    >
                        {opportunities.map((opp, index) => (
                            <OpportunityCard
                                key={opp.id}
                                opportunity={opp}
                                index={index}
                            />
                        ))}
                        {provided.placeholder}
                    </div>
                )}
            </Droppable>
        </div>
    );
};
```

### Recipe 2: Backend Stage Management
```java
// 3. Opportunity Service (copy-paste ready)
@ApplicationScoped
@Transactional
public class OpportunityService {
    @Inject OpportunityRepository repository;
    @Inject BonitatService bonitatService;
    @Inject StageHistoryService historyService;
    @Inject SecurityContext securityContext;
    
    public OpportunityResponse moveToStage(UUID opportunityId, Stage newStage, String reason) {
        Opportunity opp = repository.findById(opportunityId)
            .orElseThrow(() -> new NotFoundException("Opportunity not found"));
        
        // Check permissions
        if (!canModifyOpportunity(opp)) {
            throw new ForbiddenException("No permission to modify this opportunity");
        }
        
        Stage oldStage = opp.getStage();
        
        // Validate transition
        if (!isValidTransition(oldStage, newStage)) {
            throw new BadRequestException(
                String.format("Invalid transition from %s to %s", oldStage, newStage)
            );
        }
        
        // Special logic for QUALIFIED → PROPOSAL
        if (oldStage == Stage.QUALIFIED && newStage == Stage.PROPOSAL) {
            BonitatResult result = bonitatService.checkCustomerCredit(opp.getCustomer().getId());
            if (!result.isApproved()) {
                throw new BusinessException("Bonitätsprüfung fehlgeschlagen: " + result.getReason());
            }
        }
        
        // Update stage
        opp.setStage(newStage);
        opp.setUpdatedBy(securityContext.getCurrentUser());
        opp.setUpdatedAt(LocalDateTime.now());
        
        // Auto-update probability
        opp.setProbability(getDefaultProbability(newStage));
        
        // Log history
        historyService.logStageChange(opp, oldStage, newStage, reason);
        
        // Fire event for real-time updates
        eventBus.publish(new OpportunityStageChangedEvent(opp, oldStage, newStage));
        
        return OpportunityMapper.toResponse(repository.save(opp));
    }
    
    private boolean isValidTransition(Stage from, Stage to) {
        // Allow forward progression
        if (from.ordinal() < to.ordinal()) return true;
        
        // Allow backward to QUALIFIED from PROPOSAL/NEGOTIATION
        if (to == Stage.QUALIFIED && 
            (from == Stage.PROPOSAL || from == Stage.NEGOTIATION)) return true;
        
        // Allow reopening closed deals
        if (from == Stage.CLOSED && to == Stage.NEGOTIATION) return true;
        
        return false;
    }
    
    private int getDefaultProbability(Stage stage) {
        return switch (stage) {
            case LEAD -> 10;
            case QUALIFIED -> 25;
            case PROPOSAL -> 50;
            case NEGOTIATION -> 75;
            case CLOSED -> 100;
        };
    }
}
```

### Recipe 3: Opportunity Card with Actions
```typescript
// 4. Draggable Opportunity Card (copy-paste ready)
export const OpportunityCard: React.FC<{
    opportunity: Opportunity;
    index: number;
}> = ({ opportunity, index }) => {
    const [showActions, setShowActions] = useState(false);
    const { openCalculator } = useCalculatorModal();
    const deleteOpportunity = useDeleteOpportunity();
    
    return (
        <Draggable draggableId={opportunity.id} index={index}>
            {(provided, snapshot) => (
                <Card
                    ref={provided.innerRef}
                    {...provided.draggableProps}
                    {...provided.dragHandleProps}
                    className={cn(
                        "cursor-move hover:shadow-md transition-shadow",
                        snapshot.isDragging && "shadow-lg rotate-2"
                    )}
                    onMouseEnter={() => setShowActions(true)}
                    onMouseLeave={() => setShowActions(false)}
                >
                    <CardContent className="p-3">
                        <div className="flex justify-between items-start mb-2">
                            <h4 className="font-medium text-sm line-clamp-2">
                                {opportunity.title}
                            </h4>
                            {showActions && (
                                <DropdownMenu>
                                    <DropdownMenuTrigger asChild>
                                        <Button variant="ghost" size="sm">
                                            <MoreVertical className="h-4 w-4" />
                                        </Button>
                                    </DropdownMenuTrigger>
                                    <DropdownMenuContent>
                                        <DropdownMenuItem onClick={() => openCalculator(opportunity)}>
                                            <Calculator className="mr-2 h-4 w-4" />
                                            Kalkulieren
                                        </DropdownMenuItem>
                                        <DropdownMenuItem onClick={() => deleteOpportunity.mutate(opportunity.id)}>
                                            <Trash className="mr-2 h-4 w-4" />
                                            Löschen
                                        </DropdownMenuItem>
                                    </DropdownMenuContent>
                                </DropdownMenu>
                            )}
                        </div>
                        
                        <div className="space-y-1 text-sm">
                            <div className="flex items-center gap-2 text-gray-600">
                                <Building className="h-3 w-3" />
                                {opportunity.customer.name}
                            </div>
                            <div className="flex items-center gap-2">
                                <Euro className="h-3 w-3" />
                                <span className="font-semibold">
                                    {opportunity.value.toLocaleString()}
                                </span>
                            </div>
                            <div className="flex items-center gap-2 text-gray-500">
                                <Calendar className="h-3 w-3" />
                                {formatDate(opportunity.expectedCloseDate)}
                            </div>
                        </div>
                        
                        {opportunity.probability && (
                            <Progress 
                                value={opportunity.probability} 
                                className="mt-2 h-1"
                            />
                        )}
                    </CardContent>
                </Card>
            )}
        </Draggable>
    );
};
```

---

## 🧪 TEST PATTERNS

### Pattern 1: Drag & Drop Test
```typescript
describe('OpportunityBoard', () => {
    it('should move opportunity between stages', async () => {
        const { getByText, getAllByTestId } = render(<OpportunityBoard />);
        
        // Wait for opportunities to load
        await waitFor(() => {
            expect(getByText('Test Opportunity')).toBeInTheDocument();
        });
        
        // Find opportunity card
        const opportunityCard = getByText('Test Opportunity').closest('[data-rbd-draggable-id]');
        
        // Simulate drag from LEAD to QUALIFIED
        fireEvent.dragStart(opportunityCard!);
        const qualifiedColumn = getAllByTestId('droppable-QUALIFIED')[0];
        fireEvent.drop(qualifiedColumn);
        
        // Verify API call
        await waitFor(() => {
            expect(mockMoveOpportunity).toHaveBeenCalledWith({
                opportunityId: 'test-id',
                fromStage: 'LEAD',
                toStage: 'QUALIFIED'
            });
        });
    });
});
```

### Pattern 2: Stage Validation Test
```java
@QuarkusTest
class OpportunityServiceTest {
    @Inject OpportunityService service;
    
    @Test
    @TestSecurity(user = "testuser", roles = "sales")
    void testInvalidStageTransition() {
        // Create opportunity in LEAD stage
        Opportunity opp = createTestOpportunity(Stage.LEAD);
        
        // Try to jump directly to NEGOTIATION (invalid)
        assertThrows(BadRequestException.class, () -> {
            service.moveToStage(opp.getId(), Stage.NEGOTIATION, "Test");
        });
    }
    
    @Test
    @TestSecurity(user = "testuser", roles = "sales")
    void testBonitatCheckRequired() {
        // Create opportunity in QUALIFIED stage
        Opportunity opp = createTestOpportunity(Stage.QUALIFIED);
        
        // Mock failed Bonität check
        when(bonitatService.checkCustomerCredit(any()))
            .thenReturn(BonitatResult.failed("Credit limit exceeded"));
        
        // Try to move to PROPOSAL
        BusinessException ex = assertThrows(BusinessException.class, () -> {
            service.moveToStage(opp.getId(), Stage.PROPOSAL, "Test");
        });
        
        assertTrue(ex.getMessage().contains("Bonitätsprüfung fehlgeschlagen"));
    }
}
```

---

## 🔌 INTEGRATION COOKBOOK

### Mit Bonitätsprüfung (FC-011)
```typescript
// Stage transition with credit check
const useStageTransition = () => {
    const [showBonitatDialog, setShowBonitatDialog] = useState(false);
    const [pendingTransition, setPendingTransition] = useState<any>(null);
    
    const moveOpportunity = useMutation({
        mutationFn: async (data: MoveStageRequest) => {
            // Check if credit check needed
            if (data.fromStage === 'QUALIFIED' && data.toStage === 'PROPOSAL') {
                const creditCheck = await checkCustomerCredit(data.customerId);
                if (!creditCheck.approved) {
                    throw new Error(`Bonitätsprüfung fehlgeschlagen: ${creditCheck.reason}`);
                }
            }
            
            return opportunityApi.moveToStage(data);
        },
        onError: (error: any) => {
            if (error.message.includes('Bonitätsprüfung')) {
                setShowBonitatDialog(true);
                setPendingTransition(data);
            } else {
                toast.error(error.message);
            }
        }
    });
    
    return { moveOpportunity, showBonitatDialog, pendingTransition };
};
```

### Mit Calculator Modal (M8)
```typescript
// Auto-open calculator in PROPOSAL stage
export const OpportunityActions: React.FC<{ opportunity: Opportunity }> = ({ opportunity }) => {
    const { openCalculator } = useCalculatorModal();
    
    useEffect(() => {
        // Auto-open calculator when entering PROPOSAL stage
        if (opportunity.stage === 'PROPOSAL' && !opportunity.proposalCreated) {
            openCalculator({
                customerId: opportunity.customer.id,
                opportunityId: opportunity.id,
                prefilledValue: opportunity.value
            });
        }
    }, [opportunity.stage]);
    
    return (
        <Button 
            onClick={() => openCalculator({ opportunityId: opportunity.id })}
            variant="outline"
            size="sm"
        >
            <Calculator className="mr-2 h-4 w-4" />
            Angebot erstellen
        </Button>
    );
};
```

---

## 🎯 DRAG & DROP PATTERNS

### Touch Support
```typescript
// Enable touch support for mobile
export const TouchEnabledBoard: React.FC = () => {
    useEffect(() => {
        // Enable touch support
        const enableTouchSupport = () => {
            if ('ontouchstart' in window) {
                document.addEventListener('touchmove', (e) => e.preventDefault(), { passive: false });
            }
        };
        
        enableTouchSupport();
    }, []);
    
    return (
        <DragDropContext 
            onDragEnd={handleDragEnd}
            enableDefaultSensors={false}
            sensors={[MouseSensor, TouchSensor]}
        >
            {/* Board content */}
        </DragDropContext>
    );
};
```

### Optimistic Updates
```typescript
// Optimistic UI updates for smooth UX
const useMoveOpportunity = () => {
    const queryClient = useQueryClient();
    
    return useMutation({
        mutationFn: opportunityApi.moveToStage,
        onMutate: async ({ opportunityId, toStage }) => {
            // Cancel outgoing refetches
            await queryClient.cancelQueries(['opportunities']);
            
            // Snapshot previous value
            const previousOpportunities = queryClient.getQueryData(['opportunities']);
            
            // Optimistically update
            queryClient.setQueryData(['opportunities'], (old: any) => {
                return old.map((opp: Opportunity) => 
                    opp.id === opportunityId 
                        ? { ...opp, stage: toStage }
                        : opp
                );
            });
            
            return { previousOpportunities };
        },
        onError: (err, variables, context) => {
            // Rollback on error
            queryClient.setQueryData(['opportunities'], context?.previousOpportunities);
            toast.error('Fehler beim Verschieben');
        },
        onSettled: () => {
            queryClient.invalidateQueries(['opportunities']);
        }
    });
};
```

---

## 📚 DEEP KNOWLEDGE (Bei Bedarf expandieren)

<details>
<summary>🏗️ Stage Configuration</summary>

### Complete Stage Configuration
```typescript
const STAGE_CONFIG: Record<Stage, StageConfig> = {
    LEAD: {
        label: 'Lead',
        icon: <Sparkles className="h-4 w-4" />,
        color: 'gray',
        description: 'Neue, unqualifizierte Opportunities',
        actions: ['qualify', 'delete'],
        requiredFields: ['title', 'customer', 'value']
    },
    QUALIFIED: {
        label: 'Qualifiziert',
        icon: <CheckCircle className="h-4 w-4" />,
        color: 'blue',
        description: 'Interesse bestätigt, Budget vorhanden',
        actions: ['createProposal', 'assignUser', 'delete'],
        requiredFields: ['expectedCloseDate', 'probability']
    },
    PROPOSAL: {
        label: 'Angebot',
        icon: <FileText className="h-4 w-4" />,
        color: 'yellow',
        description: 'Angebot erstellt und versendet',
        actions: ['editProposal', 'sendReminder', 'negotiate'],
        requiredFields: ['proposalNumber', 'proposalDate']
    },
    NEGOTIATION: {
        label: 'Verhandlung',
        icon: <MessageSquare className="h-4 w-4" />,
        color: 'orange',
        description: 'In aktiver Verhandlung',
        actions: ['updateValue', 'close', 'backToProposal'],
        requiredFields: ['nextAction', 'nextActionDate']
    },
    CLOSED: {
        label: 'Abgeschlossen',
        icon: <Trophy className="h-4 w-4" />,
        color: 'green',
        description: 'Deal gewonnen oder verloren',
        actions: ['reopen', 'archive'],
        requiredFields: ['closedDate', 'closedReason']
    }
};
```

</details>

<details>
<summary>⚡ Performance Optimization</summary>

### Virtual Scrolling for Large Pipelines
```typescript
import { FixedSizeList } from 'react-window';

export const VirtualizedColumn: React.FC<{ opportunities: Opportunity[] }> = ({ opportunities }) => {
    const Row = ({ index, style }: { index: number; style: React.CSSProperties }) => (
        <div style={style}>
            <OpportunityCard opportunity={opportunities[index]} index={index} />
        </div>
    );
    
    return (
        <FixedSizeList
            height={600}
            itemCount={opportunities.length}
            itemSize={120}
            width="100%"
        >
            {Row}
        </FixedSizeList>
    );
};
```

### Database Query Optimization
```java
// Optimized query with eager loading
@Query("""
    SELECT DISTINCT o FROM Opportunity o
    LEFT JOIN FETCH o.customer c
    LEFT JOIN FETCH o.assignedUser u
    LEFT JOIN FETCH o.attachments a
    WHERE o.stage != 'CLOSED'
    OR o.closedDate > :thirtyDaysAgo
    ORDER BY o.stage, o.probability DESC, o.value DESC
""")
List<Opportunity> findActiveOpportunities(@Param("thirtyDaysAgo") LocalDate date);
```

</details>

---

**🎯 Nächster Schritt:** Backend Endpoints implementieren und mit Frontend Kanban Board verbinden