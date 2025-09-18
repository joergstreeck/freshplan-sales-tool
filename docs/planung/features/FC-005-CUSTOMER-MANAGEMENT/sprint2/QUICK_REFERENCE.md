# 🚀 Sprint 2 Quick Reference - Das Wichtigste auf einen Blick

**Sprint 2:** Customer UI Integration & Task Preview MVP  
**Dauer:** 3.5 Tage  
**Philosophie:** "Software die mitdenkt"  

---

## ⚠️ WICHTIG: TypeScript Import Types

**Alle Code-Beispiele verwenden `import type` für TypeScript Types!**
```typescript
import type { Task, Customer } from './types';  // ✅ RICHTIG
import { Task, Customer } from './types';       // ❌ FALSCH
```
[Details im TypeScript Import Type Guide](/Users/joergstreeck/freshplan-sales-tool/docs/guides/TYPESCRIPT_IMPORT_TYPE_GUIDE.md)

---

## 🎯 Die 5 Kern-Features

### 1. Customer UI Integration
```typescript
// Wo: CustomersPage.tsx
// Was: Empty State Hero + CustomerOnboardingWizard
// Warum: Erste Kundenanlage = Start der Beziehung
```

### 2. Field Theme System ✅
```typescript
// Status: Implementiert, ABER Fix erforderlich!
// Problem: UI-Elemente fehlen, Größen nicht korrekt
// Lösung: ADAPTIVE_THEME_FIX_PLAN.md
```

### 3. Task Preview MVP  
```typescript
// 3 Rules: Welcome (2d), Quote (7d), Inactivity (60d)
// Automatisch: Nach Customer.create → Task generiert
// Feedback: Toast mit "Aufgabe anzeigen" Action
```

### 4. Quick Wins
```typescript
// Shortcuts: Ctrl+N (Kunde), Ctrl+T (Task), Ctrl+K (Command)
// Empty States: Motivierend statt frustrierend
// Mobile: Swipe right = done, left = snooze
```

### 5. Wizard-Struktur Final
```typescript
// 3 Schritte: Kundendaten → Standorte → Details
// Mit Adaptive Theme für intelligente Feldanordnung
// Details: WIZARD_STRUCTURE_FINAL.md
```

---

## 📁 File Locations

### Frontend Changes:
```
frontend/src/
├── pages/
│   ├── CustomersPageV2.tsx                   # Main Page with MainLayoutV2 ✅
│   └── __tests__/CustomersPageV2.test.tsx    # Page Tests (8 tests)
├── features/customers/components/wizard/
│   ├── CustomerOnboardingWizard.tsx          # Wizard Component
│   ├── CustomerOnboardingWizardModal.tsx     # NEW: Modal Wrapper ✅
│   └── __tests__/
│       └── CustomerOnboardingWizardModal.test.tsx  # Modal Tests (15 tests)
├── components/
│   ├── layout/MainLayoutV2.tsx               # MUST USE for all pages!
│   ├── common/EmptyStateHero.tsx             # Smart Empty States
│   ├── notifications/ActionToast.tsx         # Toast with Actions
│   ├── mobile/SwipeableTaskCard.tsx         # Mobile Swipe
│   ├── tasks/TaskBadge.tsx                  # NEW/OVERDUE Badges
│   └── dashboard/CockpitTeaser.tsx          # Preview Widget
├── hooks/useKeyboardShortcuts.tsx            # Productivity Boost
├── services/
│   ├── taskEngine.ts                        # Core Task Engine
│   └── taskRules/                           # Rule Implementations
└── types/task.types.ts                      # Task Domain Model
```

### Backend Changes:
```
backend/src/main/java/de/freshplan/api/
└── domain/task/
    ├── entity/Task.java                     # JPA Entity
    ├── service/TaskService.java             # Business Logic
    ├── repository/TaskRepository.java       # Data Access
    └── api/TaskResource.java                # REST Endpoints
```

---

## 🚨 KRITISCH: Architecture Fix Changes

### Was wurde geändert:
1. **MainLayoutV2 ist Pflicht** für alle Seiten
2. **Wizard als Modal** statt separate Route
3. **Event-basierte Navigation** für globale Actions
4. **Tests hinzugefügt** (23 Tests total)

### Geänderte Dateien:
- `CustomersPageV2.tsx` - Nutzt jetzt MainLayoutV2
- `CustomerOnboardingWizardModal.tsx` - NEW: Modal Wrapper
- `/customers/new` Route - ENTFERNT
- `navigation.config.ts` - Action statt Path

---

### Field Theme System ✅
- **Datei:** `/frontend/src/features/customers/theme/fieldTheme.ts`
- **5 Größen:** compact, small, medium, large, full
- **Calculator:** `/frontend/src/features/customers/utils/fieldSizeCalculator.ts`
- **Debug:** `window.debugFieldTheme()` in Browser Console

### Adaptive Layout Evolution 🆕
- **Konzept:** `/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/prototypes/ADAPTIVE_LAYOUT_EVOLUTION.md`
- **Implementation:** `/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/prototypes/ADAPTIVE_LAYOUT_IMPLEMENTATION_GUIDE.md`
- **Rollout:** `/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/prototypes/ADAPTIVE_LAYOUT_ROLLOUT_GUIDE.md`
- **Status:** Ready for Team Review

---

## 🔑 Key Code Snippets

### Customer Created → Task Generated:
```typescript
const handleCustomerCreated = async (customer) => {
  // 1. Close Wizard
  setWizardOpen(false);
  
  // 2. Generate Task (if enabled)
  let taskId;
  if (featureFlags.taskPreview) {
    const tasks = await taskEngine.processEvent({
      type: 'customer-created',
      context: { customer, user }
    });
    taskId = tasks[0]?.id;
  }
  
  // 3. Show Success with Action
  toast.success(
    <ActionToast
      message={`Kunde "${customer.name}" angelegt!`}
      action={taskId ? {
        label: "Aufgabe anzeigen",
        onClick: () => navigate(`/tasks/${taskId}`)
      } : undefined}
    />
  );
  
  // 4. Navigate to Detail
  navigate(`/customers/${customer.id}?highlight=new`);
};
```

### Task Rule Example:
```typescript
export const welcomeCustomerRule: TaskRule = {
  id: 'welcome-customer',
  trigger: 'customer-created',
  condition: () => true, // Always
  generateTask: (context) => ({
    title: `🎉 Neukunde ${context.customer.name} begrüßen`,
    dueDate: addDays(new Date(), 2).toISOString(),
    priority: 'high',
    type: 'call'
  })
};
```

### Empty State Magic:
```typescript
{customers.length === 0 ? (
  <EmptyStateHero 
    title="Noch keine Kunden"
    description="Legen Sie Ihren ersten Kunden an!"
    action={{
      label: "✨ Ersten Kunden anlegen",
      onClick: () => setWizardOpen(true)
    }}
  />
) : (
  <CustomerTable customers={customers} />
)}
```

---

## 🧪 Test Commands

```bash
# Unit Tests
cd frontend && npm test CustomersPage
cd backend && ./mvnw test -Dtest=TaskServiceTest

# E2E Test
npx playwright test customer-onboarding

# Full Test Suite
npm run test:all
```

---

## 🎨 UI/UX Decisions

| Feature | Decision | Reason |
|---------|----------|---------|
| Button | Hybrid (Table + Empty) | Discoverable always |
| Wizard | Modal/Drawer | Device adaptive |
| Tasks | Auto-generate | "System thinks" |
| Badges | NEW (24h), OVERDUE | Visual priority |
| Mobile | Swipe gestures | Natural interaction |

---

## 📊 Performance Targets

- Task Creation: < 2 seconds ⚡
- Customer List: < 200ms render
- Mobile Swipe: 60 FPS
- Bundle Size: < 250KB gzipped

---

## 🚨 Common Issues & Solutions

### "Task not created"
```typescript
// Check Feature Flag
console.log(featureFlags.taskPreview); // Should be true

// Check Task Engine
window.taskEngine.getRules(); // Dev only
```

### "Empty State not showing"
```typescript
// Check data loading
console.log(customers); // Should be []
console.log(isLoading); // Should be false
```

### "Keyboard shortcuts not working"
```typescript
// Check provider wrapped
<KeyboardShortcutsProvider>
  <App />
</KeyboardShortcutsProvider>
```

---

## 🔗 Related Documentation

### Sprint 2 Core Docs:
- **Philosophy:** [PHILOSOPHY_AND_APPROACH.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/PHILOSOPHY_AND_APPROACH.md)
- **Sprint Overview:** [Sprint 2 README](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/README.md)

### Daily Implementation Guides:
- **Tag 1:** [DAY1_IMPLEMENTATION.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/DAY1_IMPLEMENTATION.md) - CustomersPage & Quick Wins
- **Tag 2:** [DAY2_IMPLEMENTATION.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/DAY2_IMPLEMENTATION.md) - Task Engine & Backend
- **Tag 3:** [DAY3_IMPLEMENTATION.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/DAY3_IMPLEMENTATION.md) - Mobile & Audit UI
- **Tag 3.5:** [DAY3_5_FINAL.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/DAY3_5_FINAL.md) - Polish & Demo

### FC-005 Base:
- **FC-005 README:** [/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/README.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/README.md)

---

## ✅ Sprint 2 Checklist

**Day 1:**
- [ ] CustomersPage refactoring
- [ ] EmptyStateHero component
- [ ] Keyboard shortcuts
- [ ] ActionToast implementation

**Day 2:**
- [ ] Task domain model
- [ ] 3 Task rules
- [ ] Task Engine service
- [ ] Backend API

**Day 3:**
- [ ] Swipeable cards
- [ ] Cockpit teaser
- [ ] Audit UI
- [ ] Performance optimization

**Day 3.5:**
- [ ] E2E tests
- [ ] Documentation
- [ ] Code review
- [ ] Sprint demo prep

---

**Remember:** Every line of code should make the system feel like it's working FOR the user, not against them! 🎯