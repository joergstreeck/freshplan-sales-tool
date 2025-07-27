# 🛠️ Sprint 2 - Tag 2: Task Engine & Backend API

**Sprint:** Sprint 2 - Customer UI Integration & Task Preview  
**Tag:** Tag 2 von 3.5  
**Dauer:** 8 Stunden  
**Fokus:** Task Domain Model, Task Engine, Backend API, Integration  

---

## 📍 Navigation

### Sprint 2 Dokumente:
- **← Zurück:** [Tag 1 Implementation](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/DAY1_IMPLEMENTATION.md)
- **→ Weiter:** [Tag 3 Implementation](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/DAY3_IMPLEMENTATION.md)
- **↑ Übersicht:** [Sprint 2 Overview](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/README.md)
- **📚 Quick Ref:** [Quick Reference](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/QUICK_REFERENCE.md)

### FC-005 Struktur:
- **Hauptübersicht:** [FC-005 README](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/README.md)
- **Backend Docs:** [02-BACKEND](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/02-BACKEND/README.md)
- **Integration:** [04-INTEGRATION](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/04-INTEGRATION/README.md)

### Übergeordnete Docs:
- **Master Plan:** [CRM V5](/Users/joergstreeck/freshplan-sales-tool/docs/CRM_COMPLETE_MASTER_PLAN_V5.md)
- **Feature Roadmap:** [Complete Roadmap](/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-12_COMPLETE_FEATURE_ROADMAP.md)

---

## 🎯 Tag 2 Ziele

1. **Task Domain Model definieren** (1h)
2. **Task Engine implementieren** (2h)
3. **3 Core Rules umsetzen** (2h)
4. **Backend API erstellen** (2h)
5. **Frontend Integration** (1h)

---

## 2.1 Task Domain Model (1h)

### TypeScript Types:
```typescript
// frontend/src/types/task.types.ts
export interface Task {
  id: string;
  title: string;
  description?: string;
  type: 'call' | 'email' | 'meeting' | 'follow-up' | 'other';
  priority: 'low' | 'medium' | 'high';
  status: 'pending' | 'in-progress' | 'completed' | 'cancelled';
  dueDate: string; // ISO 8601
  createdAt: string;
  updatedAt: string;
  completedAt?: string;
  
  // Relations
  customerId: string;
  customerName: string;
  assignedTo: string;
  assignedToName: string;
  createdBy: string;
  
  // Metadata
  source: 'manual' | 'rule' | 'system';
  ruleId?: string;
  tags?: string[];
  
  // UI Hints
  isNew?: boolean; // Created < 24h
  isOverdue?: boolean;
  daysUntilDue?: number;
}

export interface TaskRule {
  id: string;
  name: string;
  description: string;
  trigger: 'customer-created' | 'quote-sent' | 'customer-inactive' | 'contract-expiring';
  condition: (context: TaskContext) => boolean;
  generateTask: (context: TaskContext) => Omit<Task, 'id' | 'createdAt' | 'updatedAt'>;
  priority: number; // For ordering
  enabled: boolean;
}

export interface TaskContext {
  customer: Customer;
  user: User;
  trigger: string;
  metadata?: Record<string, any>;
}

export interface TaskEvent {
  type: string;
  context: TaskContext;
  timestamp: string;
}
```

### Backend Entity:
```java
// backend/src/main/java/de/freshplan/api/domain/task/entity/Task.java
package de.freshplan.api.domain.task.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tasks")
public class Task extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;
    
    @Column(nullable = false)
    public String title;
    
    @Column(length = 1000)
    public String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public TaskType type = TaskType.OTHER;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public TaskPriority priority = TaskPriority.MEDIUM;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public TaskStatus status = TaskStatus.PENDING;
    
    @Column(name = "due_date", nullable = false)
    public LocalDateTime dueDate;
    
    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    public LocalDateTime updatedAt;
    
    @Column(name = "completed_at")
    public LocalDateTime completedAt;
    
    // Relations
    @Column(name = "customer_id", nullable = false)
    public UUID customerId;
    
    @Column(name = "customer_name", nullable = false)
    public String customerName;
    
    @Column(name = "assigned_to", nullable = false)
    public UUID assignedTo;
    
    @Column(name = "assigned_to_name", nullable = false)
    public String assignedToName;
    
    @Column(name = "created_by", nullable = false)
    public UUID createdBy;
    
    // Metadata
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public TaskSource source = TaskSource.MANUAL;
    
    @Column(name = "rule_id")
    public String ruleId;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum TaskType {
        CALL, EMAIL, MEETING, FOLLOW_UP, OTHER
    }
    
    public enum TaskPriority {
        LOW, MEDIUM, HIGH
    }
    
    public enum TaskStatus {
        PENDING, IN_PROGRESS, COMPLETED, CANCELLED
    }
    
    public enum TaskSource {
        MANUAL, RULE, SYSTEM
    }
}
```

---

## 2.2 Task Engine Implementation (2h)

```typescript
// frontend/src/services/taskEngine.ts
import { TaskRule, TaskEvent, Task, TaskContext } from '../types/task.types';
import { taskApi } from './api/taskApi';
import { 
  welcomeCustomerRule,
  requestQuoteRule, 
  inactivityWarningRule 
} from './taskRules';

class TaskEngine {
  private rules: Map<string, TaskRule> = new Map();
  private enabled: boolean = true;
  
  constructor() {
    this.registerDefaultRules();
  }
  
  private registerDefaultRules() {
    // Die 3 Core Rules für Sprint 2
    this.registerRule(welcomeCustomerRule);
    this.registerRule(requestQuoteRule);
    this.registerRule(inactivityWarningRule);
  }
  
  registerRule(rule: TaskRule) {
    if (!rule.enabled) return;
    this.rules.set(rule.id, rule);
    console.log(`[TaskEngine] Rule registered: ${rule.name}`);
  }
  
  async processEvent(event: TaskEvent): Promise<Task[]> {
    if (!this.enabled) return [];
    
    const tasks: Task[] = [];
    const applicableRules = this.findApplicableRules(event);
    
    for (const rule of applicableRules) {
      try {
        if (rule.condition(event.context)) {
          const taskData = rule.generateTask(event.context);
          const task = await this.createTask({
            ...taskData,
            source: 'rule',
            ruleId: rule.id
          });
          tasks.push(task);
          
          console.log(`[TaskEngine] Task created by rule ${rule.name}:`, task);
        }
      } catch (error) {
        console.error(`[TaskEngine] Error in rule ${rule.name}:`, error);
      }
    }
    
    return tasks;
  }
  
  private findApplicableRules(event: TaskEvent): TaskRule[] {
    return Array.from(this.rules.values())
      .filter(rule => rule.trigger === event.type)
      .sort((a, b) => b.priority - a.priority);
  }
  
  private async createTask(taskData: Partial<Task>): Promise<Task> {
    // Call backend API
    const response = await taskApi.createTask(taskData);
    
    // Emit custom event for UI updates
    window.dispatchEvent(new CustomEvent('freshplan:task-created', {
      detail: response
    }));
    
    return response;
  }
  
  // Dev helpers
  getRules(): TaskRule[] {
    return Array.from(this.rules.values());
  }
  
  setEnabled(enabled: boolean) {
    this.enabled = enabled;
  }
}

export const taskEngine = new TaskEngine();

// Dev mode: Expose to window
if (import.meta.env.DEV) {
  (window as any).taskEngine = taskEngine;
}
```

---

## 2.3 Die 3 Core Rules (2h)

### Rule 1: Welcome Customer (2 Tage)
```typescript
// frontend/src/services/taskRules/welcomeCustomerRule.ts
import { TaskRule } from '../../types/task.types';
import { addDays } from 'date-fns';

export const welcomeCustomerRule: TaskRule = {
  id: 'welcome-customer',
  name: 'Willkommensanruf für neue Kunden',
  description: 'Erstellt automatisch eine Aufgabe für einen Willkommensanruf 2 Tage nach Kundenanlage',
  trigger: 'customer-created',
  priority: 100,
  enabled: true,
  
  condition: (context) => {
    // Immer wenn ein neuer Kunde angelegt wird
    return true;
  },
  
  generateTask: (context) => ({
    title: `🎉 Neukunde ${context.customer.name} begrüßen`,
    description: `Willkommensanruf bei ${context.customer.name} durchführen:
    
• Sich für das Vertrauen bedanken
• Ansprechpartner und Prozesse klären
• Erste Bedarfe erfragen
• Nächste Schritte besprechen

Kontakt: ${context.customer.phone || 'Keine Telefonnummer hinterlegt'}
E-Mail: ${context.customer.email || 'Keine E-Mail hinterlegt'}`,
    type: 'call',
    priority: 'high',
    status: 'pending',
    dueDate: addDays(new Date(), 2).toISOString(),
    customerId: context.customer.id,
    customerName: context.customer.name,
    assignedTo: context.user.id,
    assignedToName: context.user.name,
    createdBy: 'system',
    source: 'rule',
    tags: ['willkommen', 'neukunde']
  })
};
```

### Rule 2: Request Quote (7 Tage)
```typescript
// frontend/src/services/taskRules/requestQuoteRule.ts
import { TaskRule } from '../../types/task.types';
import { addDays } from 'date-fns';

export const requestQuoteRule: TaskRule = {
  id: 'request-quote',
  name: 'Angebot nachfassen',
  description: 'Erinnert nach 7 Tagen an das Erstellen eines Angebots für neue Kunden',
  trigger: 'customer-created',
  priority: 90,
  enabled: true,
  
  condition: (context) => {
    // Nur für Kunden mit Potenzial (später: basierend auf Kundenkategorie)
    return !context.customer.tags?.includes('test');
  },
  
  generateTask: (context) => ({
    title: `💰 Angebot für ${context.customer.name} erstellen`,
    description: `Zeit ein erstes Angebot zu erstellen!

Der Kunde wurde vor einer Woche angelegt. Folgende Schritte empfehlen wir:

1. Bedarfsanalyse durchführen/vervollständigen
2. Passende Produkte auswählen
3. Individuelles Angebot erstellen
4. Angebot versenden und Termin vereinbaren

💡 Tipp: Nutzen Sie den Angebots-Konfigurator für schnelle Erstellung.`,
    type: 'follow-up',
    priority: 'medium',
    status: 'pending',
    dueDate: addDays(new Date(), 7).toISOString(),
    customerId: context.customer.id,
    customerName: context.customer.name,
    assignedTo: context.user.id,
    assignedToName: context.user.name,
    createdBy: 'system',
    source: 'rule',
    tags: ['angebot', 'opportunity']
  })
};
```

### Rule 3: Inactivity Warning (60 Tage)
```typescript
// frontend/src/services/taskRules/inactivityWarningRule.ts
import { TaskRule } from '../../types/task.types';
import { addDays } from 'date-fns';

export const inactivityWarningRule: TaskRule = {
  id: 'inactivity-warning',
  name: 'Inaktivitäts-Warnung',
  description: 'Warnt bei Kunden ohne Aktivität seit 60 Tagen',
  trigger: 'customer-inactive', // Wird täglich vom Backend getriggert
  priority: 80,
  enabled: true,
  
  condition: (context) => {
    // Backend prüft bereits die 60-Tage-Regel
    return true;
  },
  
  generateTask: (context) => ({
    title: `⚠️ ${context.customer.name} reaktivieren`,
    description: `Dieser Kunde hatte seit 60 Tagen keinen Kontakt mehr!

Letzte Aktivität: ${context.metadata?.lastActivity || 'Unbekannt'}

Empfohlene Aktionen:
• Anrufen und nach aktuellem Stand fragen
• Neue Produkte oder Services vorstellen
• Nach Feedback zur bisherigen Zusammenarbeit fragen
• Termin für persönliches Gespräch vereinbaren

⚡ Schnell-Aktionen:
- E-Mail mit News senden
- WhatsApp-Nachricht schicken
- Kleine Aufmerksamkeit zusenden`,
    type: 'call',
    priority: 'high',
    status: 'pending',
    dueDate: addDays(new Date(), 1).toISOString(), // Morgen!
    customerId: context.customer.id,
    customerName: context.customer.name,
    assignedTo: context.user.id,
    assignedToName: context.user.name,
    createdBy: 'system',
    source: 'rule',
    tags: ['reaktivierung', 'inaktiv', 'wichtig']
  })
};
```

---

## 2.4 Backend API Implementation (2h)

### Task Resource:
```java
// backend/src/main/java/de/freshplan/api/domain/task/api/TaskResource.java
package de.freshplan.api.domain.task.api;

import de.freshplan.api.domain.task.service.TaskService;
import de.freshplan.api.domain.task.service.dto.*;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.UUID;

@Path("/api/tasks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TaskResource {
    
    @Inject
    TaskService taskService;
    
    @POST
    public Response createTask(@Valid CreateTaskRequest request) {
        TaskResponse task = taskService.createTask(request);
        return Response.status(Response.Status.CREATED).entity(task).build();
    }
    
    @GET
    public Response getTasks(
            @QueryParam("customerId") UUID customerId,
            @QueryParam("assignedTo") UUID assignedTo,
            @QueryParam("status") String status,
            @QueryParam("overdue") Boolean overdue,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        
        TaskFilterRequest filter = TaskFilterRequest.builder()
                .customerId(customerId)
                .assignedTo(assignedTo)
                .status(status)
                .overdue(overdue)
                .page(page)
                .size(size)
                .build();
                
        return Response.ok(taskService.getTasks(filter)).build();
    }
    
    @GET
    @Path("/{id}")
    public Response getTask(@PathParam("id") UUID id) {
        return taskService.findById(id)
                .map(Response::ok)
                .orElse(Response.status(Response.Status.NOT_FOUND))
                .build();
    }
    
    @PUT
    @Path("/{id}")
    public Response updateTask(@PathParam("id") UUID id, @Valid UpdateTaskRequest request) {
        return taskService.updateTask(id, request)
                .map(Response::ok)
                .orElse(Response.status(Response.Status.NOT_FOUND))
                .build();
    }
    
    @PUT
    @Path("/{id}/complete")
    public Response completeTask(@PathParam("id") UUID id) {
        return taskService.completeTask(id)
                .map(Response::ok)
                .orElse(Response.status(Response.Status.NOT_FOUND))
                .build();
    }
    
    @GET
    @Path("/stats")
    public Response getTaskStats(@QueryParam("userId") UUID userId) {
        return Response.ok(taskService.getTaskStats(userId)).build();
    }
}
```

### Task Service:
```java
// backend/src/main/java/de/freshplan/api/domain/task/service/TaskService.java
package de.freshplan.api.domain.task.service;

import de.freshplan.api.domain.task.entity.Task;
import de.freshplan.api.domain.task.repository.TaskRepository;
import de.freshplan.api.domain.task.service.dto.*;
import de.freshplan.api.domain.task.service.mapper.TaskMapper;
import de.freshplan.api.domain.customer.service.CustomerService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@ApplicationScoped
@Transactional
public class TaskService {
    
    @Inject
    TaskRepository taskRepository;
    
    @Inject
    TaskMapper taskMapper;
    
    @Inject
    CustomerService customerService;
    
    public TaskResponse createTask(CreateTaskRequest request) {
        // Validate customer exists
        var customer = customerService.findById(request.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        
        Task task = taskMapper.toEntity(request);
        task.customerName = customer.getName();
        task.persist();
        
        return taskMapper.toResponse(task);
    }
    
    public Page<TaskResponse> getTasks(TaskFilterRequest filter) {
        return taskRepository.findByFilter(filter)
                .map(taskMapper::toResponse);
    }
    
    public Optional<TaskResponse> findById(UUID id) {
        return Task.findByIdOptional(id)
                .map(task -> taskMapper.toResponse((Task) task));
    }
    
    public Optional<TaskResponse> updateTask(UUID id, UpdateTaskRequest request) {
        return Task.findByIdOptional(id)
                .map(entity -> {
                    Task task = (Task) entity;
                    taskMapper.updateEntity(task, request);
                    return taskMapper.toResponse(task);
                });
    }
    
    public Optional<TaskResponse> completeTask(UUID id) {
        return Task.findByIdOptional(id)
                .map(entity -> {
                    Task task = (Task) entity;
                    task.status = Task.TaskStatus.COMPLETED;
                    task.completedAt = LocalDateTime.now();
                    return taskMapper.toResponse(task);
                });
    }
    
    public TaskStatsResponse getTaskStats(UUID userId) {
        var stats = new TaskStatsResponse();
        stats.setTotal(taskRepository.countByAssignedTo(userId));
        stats.setPending(taskRepository.countByAssignedToAndStatus(userId, Task.TaskStatus.PENDING));
        stats.setOverdue(taskRepository.countOverdueByAssignedTo(userId));
        stats.setCompletedToday(taskRepository.countCompletedTodayByAssignedTo(userId));
        return stats;
    }
}
```

---

## 2.5 Frontend API Integration (1h)

```typescript
// frontend/src/services/api/taskApi.ts
import { apiClient } from './client';
import { Task } from '../../types/task.types';

interface TaskFilter {
  customerId?: string;
  assignedTo?: string;
  status?: string;
  overdue?: boolean;
  page?: number;
  size?: number;
}

interface TaskStats {
  total: number;
  pending: number;
  overdue: number;
  completedToday: number;
}

export const taskApi = {
  async createTask(data: Partial<Task>): Promise<Task> {
    const response = await apiClient.post('/api/tasks', data);
    return response.data;
  },
  
  async getTasks(filter: TaskFilter = {}): Promise<{
    content: Task[];
    totalElements: number;
    totalPages: number;
  }> {
    const response = await apiClient.get('/api/tasks', { params: filter });
    return response.data;
  },
  
  async getTask(id: string): Promise<Task> {
    const response = await apiClient.get(`/api/tasks/${id}`);
    return response.data;
  },
  
  async updateTask(id: string, data: Partial<Task>): Promise<Task> {
    const response = await apiClient.put(`/api/tasks/${id}`, data);
    return response.data;
  },
  
  async completeTask(id: string): Promise<Task> {
    const response = await apiClient.put(`/api/tasks/${id}/complete`);
    return response.data;
  },
  
  async getStats(userId?: string): Promise<TaskStats> {
    const response = await apiClient.get('/api/tasks/stats', {
      params: userId ? { userId } : undefined
    });
    return response.data;
  }
};
```

### React Query Hook:
```typescript
// frontend/src/features/tasks/hooks/useTasks.ts
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { taskApi } from '../../../services/api/taskApi';
import { useAuth } from '../../../contexts/AuthContext';

export function useTasks(filter = {}) {
  const { user } = useAuth();
  
  return useQuery({
    queryKey: ['tasks', filter],
    queryFn: () => taskApi.getTasks({
      ...filter,
      assignedTo: user.id // Immer nur eigene Tasks
    })
  });
}

export function useTaskStats() {
  const { user } = useAuth();
  
  return useQuery({
    queryKey: ['task-stats', user.id],
    queryFn: () => taskApi.getStats(user.id),
    refetchInterval: 60000 // Refresh every minute
  });
}

export function useCompleteTask() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: (taskId: string) => taskApi.completeTask(taskId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tasks'] });
      queryClient.invalidateQueries({ queryKey: ['task-stats'] });
    }
  });
}
```

---

## ✅ Tag 2 Checklist

- [ ] Task Domain Model (Entity + Types) erstellt
- [ ] Task Engine mit Event Processing
- [ ] 3 Core Rules implementiert und getestet
- [ ] Backend API (POST, GET, PUT /api/tasks)
- [ ] Frontend API Client + React Query Hooks
- [ ] Integration Test: Customer anlegen → Task erstellt
- [ ] Performance: Task Creation < 2 Sekunden

---

## 🧪 Integration Test

```typescript
// frontend/src/features/customers/__tests__/taskIntegration.test.tsx
it('creates welcome task when customer is created', async () => {
  const { result } = renderHook(() => useCreateCustomer(), { wrapper });
  
  await act(async () => {
    await result.current.mutate({
      name: 'Test Customer',
      email: 'test@example.com'
    });
  });
  
  // Wait for task creation
  await waitFor(() => {
    const tasks = queryClient.getQueryData(['tasks']);
    expect(tasks).toHaveLength(1);
    expect(tasks[0].title).toContain('Test Customer begrüßen');
    expect(tasks[0].dueDate).toBeTruthy();
  });
});
```

---

## 🎯 Nächste Schritte

Nach erfolgreichem Abschluss von Tag 2:
1. Backend Tests laufen lassen: `cd backend && ./mvnw test`
2. Frontend Integration testen: `npm test -- taskIntegration`
3. Commit: `git commit -m "feat(sprint2): implement Day 2 - Task Engine & API"`
4. Weiter mit [Tag 3: Mobile & Polish](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/DAY3_IMPLEMENTATION.md)

---

**Remember:** Das System soll proaktiv mitdenken und dem User Arbeit abnehmen! 🤖