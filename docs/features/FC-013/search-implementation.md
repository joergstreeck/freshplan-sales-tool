# FC-013: Search Implementation für Activity & Notes System

**Parent:** [/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-24_TECH_CONCEPT_FC-013-activity-notes-system.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-24_TECH_CONCEPT_FC-013-activity-notes-system.md)  
**Fokus:** Volltext-Suche und erweiterte Filter

## 🔍 Such-Architektur

### PostgreSQL Full-Text Search
```sql
-- Search Index für Activities
CREATE INDEX idx_activity_search ON activities 
USING gin(to_tsvector('german', title || ' ' || COALESCE(content, '')));

-- Search Function
CREATE FUNCTION search_activities(search_term TEXT)
RETURNS TABLE(activity_id UUID, rank REAL) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        id,
        ts_rank(to_tsvector('german', title || ' ' || content), 
                plainto_tsquery('german', search_term)) as rank
    FROM activities
    WHERE to_tsvector('german', title || ' ' || content) @@ 
          plainto_tsquery('german', search_term)
    ORDER BY rank DESC;
END;
$$ LANGUAGE plpgsql;
```

### Search Repository
```java
@ApplicationScoped
public class ActivitySearchRepository {
    
    @PersistenceContext
    EntityManager em;
    
    public List<Activity> searchActivities(
        String searchTerm,
        String entityType,
        UUID entityId,
        int limit
    ) {
        return em.createNativeQuery(
            """
            SELECT a.* FROM activities a
            JOIN search_activities(:searchTerm) s ON a.id = s.activity_id
            WHERE (:entityType IS NULL OR a.entity_type = :entityType)
            AND (:entityId IS NULL OR a.entity_id = :entityId)
            ORDER BY s.rank DESC
            LIMIT :limit
            """, Activity.class)
            .setParameter("searchTerm", searchTerm)
            .setParameter("entityType", entityType)
            .setParameter("entityId", entityId)
            .setParameter("limit", limit)
            .getResultList();
    }
}
```

## 🎯 Filter-System

### Filter Criteria
```java
public class ActivityFilterCriteria {
    // Basis-Filter
    private List<ActivityType> types;
    private List<String> users;
    private LocalDateTime dateFrom;
    private LocalDateTime dateTo;
    
    // Entity-Filter
    private String entityType;
    private List<UUID> entityIds;
    
    // Status-Filter
    private List<ActivityStatus> statuses;
    private Boolean hasReminder;
    private Boolean isOverdue;
    
    // Volltext
    private String searchText;
    
    // Sortierung
    private SortBy sortBy = SortBy.CREATED_AT_DESC;
    private int page = 0;
    private int size = 20;
}
```

### Query Builder
```java
@ApplicationScoped
public class ActivityQueryBuilder {
    
    public TypedQuery<Activity> buildQuery(
        ActivityFilterCriteria criteria
    ) {
        var cb = em.getCriteriaBuilder();
        var query = cb.createQuery(Activity.class);
        var root = query.from(Activity.class);
        
        var predicates = new ArrayList<Predicate>();
        
        // Type Filter
        if (criteria.getTypes() != null && !criteria.getTypes().isEmpty()) {
            predicates.add(root.get("type").in(criteria.getTypes()));
        }
        
        // Date Range
        if (criteria.getDateFrom() != null) {
            predicates.add(cb.greaterThanOrEqualTo(
                root.get("createdAt"), criteria.getDateFrom()
            ));
        }
        
        // User Filter
        if (criteria.getUsers() != null && !criteria.getUsers().isEmpty()) {
            predicates.add(root.get("createdBy").get("id").in(criteria.getUsers()));
        }
        
        // Overdue Filter
        if (Boolean.TRUE.equals(criteria.getIsOverdue())) {
            predicates.add(cb.and(
                cb.lessThan(root.get("dueDate"), LocalDateTime.now()),
                cb.equal(root.get("status"), ActivityStatus.PENDING)
            ));
        }
        
        query.where(predicates.toArray(new Predicate[0]));
        
        // Sortierung
        query.orderBy(getSortOrder(cb, root, criteria.getSortBy()));
        
        return em.createQuery(query);
    }
}
```

## 🚀 Quick Filters (Frontend Integration)

### Vordefinierte Filter
```typescript
export const QUICK_FILTERS = {
  MY_ACTIVITIES: {
    label: "Meine Aktivitäten",
    filter: { users: [currentUser.id] }
  },
  OVERDUE_TASKS: {
    label: "Überfällige Aufgaben",
    filter: { 
      types: ['TASK_CREATED'],
      isOverdue: true,
      statuses: ['PENDING']
    }
  },
  TODAY: {
    label: "Heute",
    filter: {
      dateFrom: startOfDay(new Date()),
      dateTo: endOfDay(new Date())
    }
  },
  CUSTOMER_COMMUNICATION: {
    label: "Kundenkommunikation",
    filter: {
      types: ['EMAIL_SENT', 'EMAIL_RECEIVED', 'PHONE_CALL', 'MEETING']
    }
  },
  PENDING_ACTIONS: {
    label: "Offene Aktionen",
    filter: {
      types: ['TASK_CREATED', 'FOLLOW_UP_SCHEDULED'],
      statuses: ['PENDING']
    }
  }
};
```

### Saved Searches
```java
@Entity
public class SavedSearch {
    @Id
    private UUID id;
    
    private String name;
    private UUID userId;
    
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private ActivityFilterCriteria criteria;
    
    private boolean isShared;
    private boolean isPinned;
}
```

## 🎨 Search UI Components

### Search Bar mit Autocomplete
```typescript
export const ActivitySearchBar: React.FC = () => {
  const [suggestions, setSuggestions] = useState<SearchSuggestion[]>([]);
  
  const handleSearch = debounce(async (term: string) => {
    if (term.length < 2) return;
    
    const results = await activityApi.searchSuggestions(term);
    setSuggestions(results);
  }, 300);
  
  return (
    <Autocomplete
      options={suggestions}
      renderOption={(props, option) => (
        <Box {...props}>
          <Typography variant="body2">{option.text}</Typography>
          <Typography variant="caption" color="text.secondary">
            {option.type} • {option.count} Treffer
          </Typography>
        </Box>
      )}
    />
  );
};
```

## 📊 Performance Optimierung

### Caching Strategy
```java
@ApplicationScoped
public class ActivitySearchCache {
    
    @CacheResult(cacheName = "activity-search")
    public List<Activity> searchWithCache(
        String searchTerm,
        String entityType,
        UUID entityId
    ) {
        return searchRepository.searchActivities(
            searchTerm, entityType, entityId, 50
        );
    }
    
    @CacheInvalidate(cacheName = "activity-search")
    public void invalidateSearch(String entityType, UUID entityId) {
        // Cache wird automatisch geleert
    }
}
```

### Index-Wartung
```sql
-- Regelmäßige Index-Wartung
CREATE OR REPLACE FUNCTION maintain_search_index()
RETURNS void AS $$
BEGIN
    REINDEX INDEX CONCURRENTLY idx_activity_search;
    ANALYZE activities;
END;
$$ LANGUAGE plpgsql;
```

## 🔄 Integration mit FC-010

FC-010 Pipeline Scalability definiert bereits Filter für Opportunities. Diese werden erweitert:

```typescript
// Erweiterung der FC-010 Filter
export interface ExtendedOpportunityFilters extends OpportunityFilters {
  // Neue Activity-basierte Filter
  hasRecentActivity?: boolean;
  activityTypes?: ActivityType[];
  lastActivityDays?: number;
  hasPendingTasks?: boolean;
}
```