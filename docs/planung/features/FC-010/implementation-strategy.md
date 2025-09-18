# FC-010: Implementierungs-Strategie für Pipeline-Skalierung

**Parent:** [/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-010-pipeline-scalability-ux.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-010-pipeline-scalability-ux.md)  
**Fokus:** Technische Umsetzung und Architektur

## 🏗️ Architektur-Übersicht

### State Management für Filter & Views

```typescript
// Global Filter State (Zustand)
interface PipelineFilterState {
  // Quick Filters
  quickFilters: {
    myOpportunities: boolean;
    highValue: boolean;
    closingSoon: boolean;
    renewalDue: boolean;
    needsAction: boolean;
  };
  
  // Advanced Filters
  advancedFilters: {
    salesRep?: string[];
    dateRange?: { start: Date; end: Date };
    customerType?: ('new' | 'existing')[];
    contractStatus?: ContractStatus[];
    valueRange?: { min: number; max: number };
  };
  
  // View Settings
  viewSettings: {
    displayMode: 'compact' | 'standard' | 'detailed';
    showClosedStages: boolean;
    sortStrategy: Record<OpportunityStage, SortField>;
    activePreset?: string;
  };
}

// Zustand Store
export const usePipelineStore = create<PipelineFilterState>((set) => ({
  quickFilters: {
    myOpportunities: false,
    highValue: false,
    closingSoon: false,
    renewalDue: false,
    needsAction: false,
  },
  // ... rest of state
}));
```

### WIP-Limit Implementation

```typescript
// Configuration
const WIP_LIMITS: Record<OpportunityStage, number | null> = {
  [OpportunityStage.LEAD]: 20,
  [OpportunityStage.QUALIFIED]: 15,
  [OpportunityStage.PROPOSAL]: 10,
  [OpportunityStage.NEGOTIATION]: 7,
  [OpportunityStage.RENEWAL]: 10,
  [OpportunityStage.CLOSED_WON]: null,
  [OpportunityStage.CLOSED_LOST]: null,
};

// Hook for WIP monitoring
export const useWIPMonitoring = (stage: OpportunityStage, count: number) => {
  const limit = WIP_LIMITS[stage];
  
  return {
    isOverLimit: limit ? count > limit : false,
    limit,
    count,
    percentage: limit ? (count / limit) * 100 : 0,
    severity: limit && count > limit * 1.5 ? 'error' : 'warning'
  };
};
```

### Performance Optimization

```typescript
// Virtual Scrolling for Large Lists
import { useVirtualizer } from '@tanstack/react-virtual';

const VirtualizedOpportunityList: React.FC<{
  opportunities: Opportunity[];
  displayMode: DisplayMode;
}> = ({ opportunities, displayMode }) => {
  const parentRef = useRef<HTMLDivElement>(null);
  
  const rowVirtualizer = useVirtualizer({
    count: opportunities.length,
    getScrollElement: () => parentRef.current,
    estimateSize: () => {
      switch (displayMode) {
        case 'compact': return 80;
        case 'standard': return 150;
        case 'detailed': return 250;
      }
    },
    overscan: 5,
  });
  
  return (
    <div ref={parentRef} style={{ height: '600px', overflow: 'auto' }}>
      <div style={{ height: `${rowVirtualizer.getTotalSize()}px` }}>
        {rowVirtualizer.getVirtualItems().map((virtualItem) => (
          <div
            key={virtualItem.key}
            style={{
              position: 'absolute',
              top: 0,
              left: 0,
              width: '100%',
              height: `${virtualItem.size}px`,
              transform: `translateY(${virtualItem.start}px)`,
            }}
          >
            <OpportunityCard
              opportunity={opportunities[virtualItem.index]}
              displayMode={displayMode}
            />
          </div>
        ))}
      </div>
    </div>
  );
};
```

### Filter Engine

```typescript
class OpportunityFilterEngine {
  private filters: PipelineFilterState;
  
  constructor(filters: PipelineFilterState) {
    this.filters = filters;
  }
  
  apply(opportunities: Opportunity[]): Opportunity[] {
    let filtered = [...opportunities];
    
    // Quick Filters
    if (this.filters.quickFilters.myOpportunities) {
      filtered = filtered.filter(o => o.assignedTo === currentUser.id);
    }
    
    if (this.filters.quickFilters.highValue) {
      filtered = filtered.filter(o => o.expectedValue >= 50000);
    }
    
    if (this.filters.quickFilters.closingSoon) {
      const weekFromNow = addDays(new Date(), 7);
      filtered = filtered.filter(o => 
        o.expectedCloseDate && isBefore(o.expectedCloseDate, weekFromNow)
      );
    }
    
    if (this.filters.quickFilters.renewalDue) {
      filtered = filtered.filter(o => 
        o.contractMonitoring?.daysUntilExpiry < 90
      );
    }
    
    if (this.filters.quickFilters.needsAction) {
      filtered = filtered.filter(o => this.needsAction(o));
    }
    
    // Advanced Filters
    if (this.filters.advancedFilters.salesRep?.length) {
      filtered = filtered.filter(o => 
        this.filters.advancedFilters.salesRep!.includes(o.assignedTo)
      );
    }
    
    // ... more advanced filters
    
    return filtered;
  }
  
  private needsAction(opportunity: Opportunity): boolean {
    // Complex logic for "needs action"
    const daysSinceLastActivity = differenceInDays(
      new Date(),
      opportunity.lastActivityDate
    );
    
    return daysSinceLastActivity > 7 || 
           opportunity.stage === OpportunityStage.RENEWAL ||
           opportunity.hasOpenTasks;
  }
}
```

### Bulk Actions Implementation

```typescript
interface BulkActionContext {
  selectedIds: Set<string>;
  isSelecting: boolean;
  toggleSelection: (id: string) => void;
  selectAll: () => void;
  clearSelection: () => void;
  executeAction: (action: BulkAction) => Promise<void>;
}

const useBulkActions = (): BulkActionContext => {
  const [selectedIds, setSelectedIds] = useState<Set<string>>(new Set());
  const { mutateAsync: bulkUpdate } = useBulkUpdateOpportunities();
  
  const executeAction = async (action: BulkAction) => {
    switch (action.type) {
      case 'assign':
        await bulkUpdate({
          ids: Array.from(selectedIds),
          update: { assignedTo: action.userId }
        });
        break;
        
      case 'move':
        await bulkUpdate({
          ids: Array.from(selectedIds),
          update: { stage: action.stage }
        });
        break;
        
      case 'tag':
        await bulkUpdate({
          ids: Array.from(selectedIds),
          update: { tags: action.tags }
        });
        break;
    }
    
    clearSelection();
  };
  
  return {
    selectedIds,
    isSelecting: selectedIds.size > 0,
    toggleSelection,
    selectAll,
    clearSelection,
    executeAction
  };
};
```

### Backend API Extensions

```yaml
# New Endpoints for Scalability

# Filtered Pipeline with Pagination
GET /api/opportunities/pipeline/filtered
Query Params:
  - filters: JSON encoded filter state
  - page: number
  - pageSize: number (default: 50)
  - includeStats: boolean

# Bulk Operations
POST /api/opportunities/bulk
Body:
  - ids: string[]
  - operation: 'update' | 'delete' | 'archive'
  - data: any

# Pipeline Statistics
GET /api/opportunities/pipeline/stats
Response:
  - stageDistribution: Record<Stage, number>
  - wipStatus: Record<Stage, {count, limit, percentage}>
  - userPerformance: Record<UserId, Stats>
```

### Caching Strategy

```typescript
// React Query Configuration for Optimal Caching
export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 5 * 60 * 1000, // 5 minutes
      cacheTime: 10 * 60 * 1000, // 10 minutes
      refetchOnWindowFocus: false,
      refetchInterval: 30 * 1000, // 30 seconds for pipeline
    },
  },
});

// Optimistic Updates for Drag & Drop
const moveOpportunity = useMutation({
  mutationFn: (data: MoveData) => api.moveOpportunity(data),
  onMutate: async (newData) => {
    // Cancel outgoing refetches
    await queryClient.cancelQueries({ queryKey: ['pipeline'] });
    
    // Snapshot previous value
    const previousPipeline = queryClient.getQueryData(['pipeline']);
    
    // Optimistically update
    queryClient.setQueryData(['pipeline'], (old) => {
      // Move opportunity in cache
    });
    
    return { previousPipeline };
  },
  onError: (err, newData, context) => {
    // Rollback on error
    queryClient.setQueryData(['pipeline'], context.previousPipeline);
  },
});
```

## 🚀 Deployment Strategy

### Feature Flags

```typescript
const FEATURE_FLAGS = {
  'pipeline.wip-limits': {
    enabled: true,
    rollout: 100, // percentage
  },
  'pipeline.virtual-scrolling': {
    enabled: true,
    minItems: 50, // only activate for 50+ items
  },
  'pipeline.bulk-actions': {
    enabled: false, // Phase 3
    allowedRoles: ['admin', 'manager'],
  },
};
```

### Migration Plan

1. **Phase 1**: Deploy filter system without breaking changes
2. **Phase 2**: Enable WIP limits as warnings only
3. **Phase 3**: Activate virtual scrolling based on performance metrics
4. **Phase 4**: Roll out bulk actions to power users first

## 🔗 Verwandte Dokumente

- **UI Spezifikationen:** [./ui-ux-specifications.md](./ui-ux-specifications.md)
- **Performance Tests:** [./performance-optimization.md](./performance-optimization.md)
- **Backend API:** [/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-12_TECH_CONCEPT_M4-opportunity-pipeline.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-12_TECH_CONCEPT_M4-opportunity-pipeline.md)