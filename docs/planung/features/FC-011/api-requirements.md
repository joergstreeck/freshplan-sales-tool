# FC-011: API Requirements für Pipeline-Cockpit Integration

**Parent:** [/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-24_TECH_CONCEPT_FC-011-pipeline-cockpit-integration.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-24_TECH_CONCEPT_FC-011-pipeline-cockpit-integration.md)  
**Fokus:** Backend API Erweiterungen und Datenstrukturen

## 🔌 Neue API Endpoints

### 1. Customer Context Loading

```yaml
# Lädt Kunde mit Opportunity-Kontext
GET /api/customers/{customerId}/with-context
Query Parameters:
  - opportunityId: string (optional) # Für opportunity-spezifischen Kontext
  - includeActivities: boolean (default: true)
  - includeDocuments: boolean (default: true)
  - includeContracts: boolean (default: true)
  - activityLimit: number (default: 20)

Response:
  {
    "customer": {
      "id": "uuid",
      "name": "Müller GmbH",
      "customerNumber": "K-2024-001",
      "type": "existing",
      "segment": "enterprise",
      "primaryContact": { ... },
      "addresses": [ ... ]
    },
    "context": {
      "activeOpportunity": {
        "id": "uuid",
        "name": "Großauftrag Q4",
        "value": 50000,
        "stage": "NEGOTIATION",
        "probability": 75,
        "expectedCloseDate": "2025-08-15"
      },
      "openOpportunities": 3,
      "totalPipelineValue": 125000,
      "lastActivity": {
        "type": "email",
        "date": "2025-07-23T14:30:00Z",
        "summary": "Angebot versendet"
      }
    },
    "activities": [ ... ],
    "documents": [ ... ],
    "contracts": [ ... ]
  }
```

### 2. Batch Preloading

```yaml
# Batch-Abruf für Preloading
POST /api/customers/batch-summary
Body:
  {
    "customerIds": ["uuid1", "uuid2", "uuid3"],
    "fields": ["id", "name", "segment", "primaryContact", "lastActivity"]
  }

Response:
  {
    "customers": [
      {
        "id": "uuid1",
        "name": "Müller GmbH",
        "segment": "enterprise",
        "primaryContact": { "name": "Max Müller", "email": "..." },
        "lastActivity": "2025-07-23T14:30:00Z"
      },
      ...
    ]
  }
```

### 3. Quick Actions API

```yaml
# Execute Quick Action
POST /api/opportunities/{opportunityId}/quick-action
Body:
  {
    "action": "MARK_WON" | "MARK_LOST" | "START_RENEWAL" | "REACTIVATE",
    "reason": "string (optional)",
    "notes": "string (optional)",
    "nextSteps": "string (optional)"
  }

Response:
  {
    "success": true,
    "opportunity": { ... updated opportunity ... },
    "sideEffects": [
      {
        "type": "EMAIL_QUEUED",
        "details": "Renewal reminder scheduled for customer"
      },
      {
        "type": "TASK_CREATED",
        "details": "Follow-up task created for account manager"
      }
    ]
  }
```

### 4. Pipeline Performance Stats

```yaml
# Real-time Pipeline Metrics
GET /api/pipeline/performance-stats
Query Parameters:
  - userId: string (optional)
  - timeframe: "today" | "week" | "month" | "quarter"

Response:
  {
    "metrics": {
      "clickToLoadTime": {
        "p50": 380,
        "p95": 820,
        "p99": 1200
      },
      "preloadHitRate": 0.65,
      "averageContextSwitchTime": 195,
      "mostAccessedCustomers": [
        { "customerId": "uuid", "accessCount": 45 },
        ...
      ]
    },
    "suggestions": [
      "Consider preloading top 5 customers at session start",
      "Enable compression for activity data"
    ]
  }
```

## 🗄️ Datenbank-Erweiterungen

### 1. Activity Tracking Table

```sql
-- Track user interactions for smart preloading
-- Erweitert das Audit Trail System aus FC-012
CREATE TABLE opportunity_access_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    opportunity_id UUID NOT NULL REFERENCES opportunities(id),
    customer_id UUID NOT NULL REFERENCES customers(id),
    access_type VARCHAR(50) NOT NULL, -- 'click', 'hover', 'detail_view'
    access_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    load_time_ms INTEGER,
    was_preloaded BOOLEAN DEFAULT FALSE,
    
    -- Indexes for performance queries
    INDEX idx_access_user_time (user_id, access_timestamp DESC),
    INDEX idx_access_customer (customer_id, access_timestamp DESC)
);
```

### 2. User Preferences

```sql
-- Store user-specific UI preferences
CREATE TABLE user_pipeline_preferences (
    user_id UUID PRIMARY KEY REFERENCES users(id),
    default_view_mode VARCHAR(20) DEFAULT 'standard', -- 'compact', 'standard', 'detailed'
    split_view_ratio DECIMAL(3,2) DEFAULT 0.30,
    favorite_filters JSONB DEFAULT '[]',
    quick_action_preferences JSONB DEFAULT '{}',
    keyboard_shortcuts_enabled BOOLEAN DEFAULT TRUE,
    preload_strategy VARCHAR(20) DEFAULT 'smart', -- 'aggressive', 'smart', 'minimal'
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 🔄 WebSocket Events

### Real-time Updates

```typescript
// WebSocket Event Types
interface PipelineWebSocketEvents {
  // Opportunity Updates
  'opportunity:updated': {
    opportunityId: string;
    changes: Partial<Opportunity>;
    updatedBy: string;
  };
  
  // Customer Context Changes
  'customer:activity': {
    customerId: string;
    activity: Activity;
    affectedOpportunities: string[];
  };
  
  // Collaborative Features
  'user:viewing': {
    userId: string;
    opportunityId: string;
    action: 'start' | 'stop';
  };
  
  // Performance Hints
  'performance:suggestion': {
    type: 'preload' | 'cache_clear' | 'layout_change';
    suggestion: string;
    data: any;
  };
}

// WebSocket Connection
const usePipelineWebSocket = () => {
  useEffect(() => {
    const ws = new WebSocket('wss://api.freshplan.de/pipeline');
    
    ws.on('opportunity:updated', (data) => {
      // Update local cache
      queryClient.setQueryData(
        ['opportunity', data.opportunityId],
        (old) => ({ ...old, ...data.changes })
      );
      
      // Show notification if relevant
      if (isMyOpportunity(data.opportunityId)) {
        toast.info(`Opportunity ${data.changes.name} wurde aktualisiert`);
      }
    });
    
    return () => ws.close();
  }, []);
};
```

## 🔐 Security & Permissions

### Context-based Access Control

```java
// Backend Permission Check
@GET
@Path("/customers/{customerId}/with-context")
@RolesAllowed({"user", "admin"})
@Auditable(eventType = AuditEventType.CUSTOMER_DATA_ACCESSED, entityType = "customer")
public Response getCustomerWithContext(
    @PathParam("customerId") UUID customerId,
    @QueryParam("opportunityId") UUID opportunityId,
    @Context SecurityContext ctx
) {
    // Check customer access
    if (!customerService.hasAccess(ctx.getUserPrincipal(), customerId)) {
        return Response.status(403).build();
    }
    
    // Check opportunity access if provided
    if (opportunityId != null) {
        Opportunity opp = opportunityService.findById(opportunityId);
        if (!opp.getCustomerId().equals(customerId) || 
            !opportunityService.hasAccess(ctx.getUserPrincipal(), opp)) {
            return Response.status(403).build();
        }
    }
    
    // Load and return data
    return Response.ok(
        customerService.loadWithContext(customerId, opportunityId)
    ).build();
}
```

## 📈 Monitoring & Analytics

### Performance Tracking Endpoints

```yaml
# Log performance metrics
POST /api/analytics/performance
Body:
  {
    "metric": "cockpit_load_time",
    "value": 450,
    "metadata": {
      "customerId": "uuid",
      "wasPreloaded": true,
      "dataSize": 2048
    }
  }

# Note: Diese Metriken werden auch im Audit Trail erfasst

# Get aggregated metrics
GET /api/analytics/pipeline-performance/summary
Response:
  {
    "period": "2025-07-24",
    "metrics": {
      "averageLoadTime": 385,
      "preloadSuccessRate": 0.72,
      "mostCommonActions": [
        { "action": "load_customer", "count": 1250 },
        { "action": "quick_win", "count": 45 }
      ]
    }
  }
```

## 🔗 Integration mit anderen Features

### M4 Opportunity Pipeline
- Erweitert `OpportunityResource` mit Context-Loading
- Nutzt bestehende Stage-Change Logic
- Teilt Transaction-Boundaries

### M5 Customer Management  
- Erweitert `CustomerResource` mit Batch-Endpoints
- Optimiert `CustomerService` für Context-Queries
- Cached Customer-Data gemeinsam

### FC-003 Email Integration
- Stellt Customer-Context für Email-Templates bereit
- Quick-Action "Email" nutzt vorgeladene Daten
- Tracked Email-Activities in Access-Log

### FC-009 Contract Renewal
- Contract-Data in Customer-Context enthalten
- Renewal Quick-Action integriert
- Zeigt Renewal-Warnings in Pipeline