# FC-002 - Anhang B: Backend-Anforderungen

**Feature:** FC-002  
**Typ:** Backend Requirements  
**Status:** 📋 Spezifiziert  

## B.1 Neue API-Endpunkte

### Aggregierter Cockpit-Endpunkt

**Zweck**: Reduzierung der API-Calls beim Cockpit-Load von 4-5 auf 1

```
GET /api/cockpit/overview
```

**Query Parameters:**
| Parameter | Typ | Default | Beschreibung |
|-----------|-----|---------|--------------|
| date | ISO-8601 | today | Datum für die Übersicht |
| userId | UUID | current user | Für Admin-Ansichten |

**Response:**
```typescript
interface CockpitOverviewDTO {
  alerts: Alert[];
  appointments: Appointment[];
  tasks: Task[];
  unassignedEmails: Email[];
  stats: DailyStats;
}

interface Alert {
  id: string;
  type: 'overdue_task' | 'birthday' | 'contract_expiry' | 'follow_up';
  priority: 'high' | 'medium' | 'low';
  title: string;
  description: string;
  relatedEntityId?: string;
  relatedEntityType?: 'customer' | 'task' | 'opportunity';
  actionUrl?: string;
}

interface DailyStats {
  totalCustomers: number;
  activeOpportunities: number;
  todayRevenue: number;
  completedTasks: number;
  pendingTasks: number;
}
```

**Implementation Notes:**
- Nutze Parallel-Queries im Backend
- Cache für 60 Sekunden
- Komprimierung aktivieren

---

### Batch Create API

**Zweck**: Mehrere Entities in einem Request erstellen

```
POST /api/batch/create
```

**Request Body:**
```typescript
interface BatchCreateRequest {
  entities: Array<{
    type: 'customer' | 'contact' | 'opportunity';
    data: object; // Entity-spezifische Daten
    tempId: string; // Client-seitige temp ID für Response-Mapping
  }>;
}
```

**Response:**
```typescript
interface BatchCreateResponse {
  created: Array<{
    tempId: string;
    id: string;
    type: string;
  }>;
  errors: Array<{
    tempId: string;
    error: string;
    details?: object;
  }>;
}
```

**Beispiel:**
```json
// Request
{
  "entities": [
    {
      "type": "customer",
      "tempId": "temp-123",
      "data": {
        "companyName": "ACME Corp",
        "email": "info@acme.com"
      }
    },
    {
      "type": "contact",
      "tempId": "temp-124",
      "data": {
        "firstName": "John",
        "lastName": "Doe",
        "customerId": "@temp-123" // Referenz auf temp-ID
      }
    }
  ]
}

// Response
{
  "created": [
    {
      "tempId": "temp-123",
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "type": "customer"
    }
  ],
  "errors": [
    {
      "tempId": "temp-124",
      "error": "Invalid customerId reference"
    }
  ]
}
```

---

### User Preferences API

**Zweck**: Zentrale Verwaltung aller User-Einstellungen

```
GET /api/users/{userId}/preferences
PUT /api/users/{userId}/preferences
```

**Schema:**
```typescript
interface UserPreferences {
  // Theme & Language
  theme: 'light' | 'dark' | 'auto';
  language: 'de' | 'en';
  
  // Navigation
  navigation: {
    collapsed: boolean;
    favorites: string[]; // Menu IDs
    recentlyVisited: string[]; // URLs
  };
  
  // Cockpit
  cockpit: {
    layout: 'default' | 'compact' | 'custom';
    columnWidths: number[];
    defaultFilters: {
      showAlerts: boolean;
      showTriage: boolean;
      focusListView: 'cards' | 'table';
    };
  };
  
  // Notifications
  notifications: {
    email: boolean;
    desktop: boolean;
    sound: boolean;
    frequency: 'realtime' | 'hourly' | 'daily';
  };
  
  // Features
  features: {
    betaFeatures: boolean;
    keyboardShortcuts: boolean;
    animations: boolean;
  };
}
```

---

## B.2 Bestehende Endpunkte Anpassungen

### Customer Search (von FC-001)

**Erweiterungen:**
```typescript
// Neue Filter
interface ExtendedCustomerFilters {
  // Existing...
  
  // Neu für Cockpit
  lastInteraction?: {
    from?: string;
    to?: string;
  };
  hasOpenTasks?: boolean;
  hasOpenOpportunities?: boolean;
}

// Neue Sortier-Optionen
enum CustomerSortBy {
  // Existing...
  
  // Neu
  LAST_INTERACTION = 'lastInteraction',
  OPPORTUNITY_VALUE = 'opportunityValue',
}

// Response erweitern
interface CustomerSearchResult {
  // Existing...
  
  // Neu: Relationship data
  _embedded?: {
    lastActivity?: Activity;
    openTasks?: number;
    totalOpportunityValue?: number;
  };
}
```

### Tasks API Erweiterung

**Neuer Endpunkt für Bulk-Updates:**
```
POST /api/tasks/bulk-update
```

**Request:**
```typescript
interface BulkUpdateTasksRequest {
  taskIds: string[];
  updates: {
    status?: 'open' | 'done' | 'cancelled';
    assignee?: string;
    dueDate?: string;
    priority?: 'high' | 'medium' | 'low';
  };
}
```

**Response:**
```typescript
interface BulkUpdateTasksResponse {
  updated: string[]; // Task IDs
  failed: Array<{
    taskId: string;
    reason: string;
  }>;
}
```

---

## B.3 Performance-Anforderungen

### Response Time SLAs

| Endpunkt | P50 | P95 | P99 |
|----------|-----|-----|-----|
| Alle Standard-Endpunkte | < 100ms | < 200ms | < 500ms |
| Cockpit Overview | < 150ms | < 300ms | < 700ms |
| Batch Operations | < 250ms | < 500ms | < 1000ms |
| Search Endpoints | < 100ms | < 200ms | < 400ms |

### Caching-Strategie

```http
# Statische Daten (User Preferences, etc.)
Cache-Control: private, max-age=300

# Cockpit Overview
Cache-Control: private, max-age=60

# Search Results
Cache-Control: private, max-age=30

# Mutations
Cache-Control: no-cache
```

### Rate Limiting

| Endpunkt-Typ | Limit |
|--------------|-------|
| GET Requests | 100/min |
| POST/PUT/DELETE | 50/min |
| Batch Operations | 10/min |
| Search | 30/min |

---

## B.4 Neue Datenbank-Indizes

```sql
-- Für Cockpit-Performance
CREATE INDEX idx_alerts_user_date 
  ON alerts(user_id, alert_date DESC)
  WHERE dismissed = false;

CREATE INDEX idx_tasks_assignee_status 
  ON tasks(assignee_id, status, due_date)
  WHERE deleted_at IS NULL;

CREATE INDEX idx_emails_unassigned 
  ON emails(created_at DESC)
  WHERE customer_id IS NULL;

-- Für User Preferences
CREATE UNIQUE INDEX idx_user_preferences 
  ON user_preferences(user_id);

-- Für Activity Tracking
CREATE INDEX idx_activities_customer_date 
  ON activities(customer_id, created_at DESC);

-- Für Performance Monitoring
CREATE INDEX idx_api_logs_endpoint_time 
  ON api_logs(endpoint, response_time, created_at);
```

---

## B.5 Migrations

```sql
-- V201__add_user_preferences_table.sql
CREATE TABLE user_preferences (
  user_id UUID PRIMARY KEY REFERENCES users(id),
  preferences JSONB NOT NULL DEFAULT '{}',
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- V202__add_alerts_table.sql
CREATE TABLE alerts (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id),
  type VARCHAR(50) NOT NULL,
  priority VARCHAR(20) NOT NULL,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  related_entity_id UUID,
  related_entity_type VARCHAR(50),
  alert_date DATE NOT NULL,
  dismissed BOOLEAN DEFAULT false,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
```

---

## B.6 Monitoring & Logging

### Metriken zu tracken:

```typescript
// Performance Metrics
- cockpit_overview_response_time
- batch_create_success_rate
- user_preferences_cache_hit_rate

// Business Metrics
- daily_active_cockpit_users
- quick_create_usage_by_type
- feature_flag_adoption_rate

// Error Metrics
- api_error_rate_by_endpoint
- batch_operation_failure_rate
```

### Logging Requirements:

```typescript
// Für jeden Cockpit-Load
logger.info('Cockpit overview requested', {
  userId,
  date,
  responseTime,
  itemCounts: {
    alerts: alerts.length,
    tasks: tasks.length,
    appointments: appointments.length,
  }
});

// Für Batch Operations
logger.info('Batch create executed', {
  userId,
  entityTypes: entities.map(e => e.type),
  successCount: created.length,
  errorCount: errors.length,
  duration,
});
```

---

**Nächste Schritte:**
1. API-Spezifikation in OpenAPI dokumentieren
2. Backend-Team briefen
3. Testdaten für neue Endpunkte erstellen
4. Performance-Tests planen