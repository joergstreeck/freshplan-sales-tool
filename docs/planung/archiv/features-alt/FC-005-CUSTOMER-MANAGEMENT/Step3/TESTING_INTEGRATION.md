# 🧪 Testing Integration - Vollständige Test-Coverage für Step3

**Phase:** 3 - Testing & Quality  
**Tag:** 5 der Woche 1  
**Status:** 📋 GEPLANT  
**Letzte Aktualisierung:** 08.08.2025  
**Claude-Ready:** ✅ Vollständig navigierbar

## 🧭 NAVIGATION FÜR CLAUDE

**← Zurück:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/SMART_SUGGESTIONS.md`  
**→ Abschluss:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/README.md`  
**↑ Parent:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/README.md`  
**⚠️ Voraussetzungen:**
- ALLE Step3 Features müssen implementiert sein
- `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/SMART_CONTACT_CARDS.md` ✅
- `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/RELATIONSHIP_INTELLIGENCE.md` ✅
- `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/CONTACT_TIMELINE.md` ✅

## ⚡ Quick Test Setup für Claude

```bash
# SOFORT STARTEN - Test-Environment vorbereiten:
cd /Users/joergstreeck/freshplan-sales-tool

# 1. Backend Tests vorbereiten
cd backend
./mvnw test-compile

# 2. Frontend Tests vorbereiten
cd ../frontend
npm install --save-dev @testing-library/react @testing-library/jest-dom
npm install --save-dev @testing-library/user-event vitest-canvas-mock
npm install --save-dev msw@latest

# 3. E2E Tests mit Playwright
npm install --save-dev @playwright/test
npx playwright install

# 4. Test-Struktur erstellen
mkdir -p frontend/src/features/customers/components/contacts/__tests__
mkdir -p frontend/src/features/customers/services/__tests__
mkdir -p frontend/src/features/customers/stores/__tests__
mkdir -p frontend/e2e/step3

# 5. Coverage Report Setup
npm install --save-dev @vitest/coverage-v8
```

## 📦 TEST STRUKTUR ÜBERSICHT

```
tests/
├── backend/                                 # Java Tests
│   ├── unit/                               # Unit Tests
│   │   ├── ContactServiceTest.java
│   │   ├── WarmthCalculatorTest.java
│   │   └── SuggestionEngineTest.java
│   └── integration/                        # Integration Tests
│       ├── ContactResourceIT.java
│       ├── ContactTimelineResourceIT.java
│       └── CustomerSearchResourceIT.java
├── frontend/                                # TypeScript Tests
│   ├── unit/                               # Component Tests
│   │   ├── SmartContactCard.test.tsx
│   │   ├── ContactTimeline.test.tsx
│   │   └── SuggestionCard.test.tsx
│   ├── integration/                        # Store/Service Tests
│   │   ├── contactStore.test.ts
│   │   ├── warmthService.test.ts
│   │   └── suggestionEngine.test.ts
│   └── e2e/                                # End-to-End Tests
│       ├── contact-management.spec.ts
│       ├── relationship-tracking.spec.ts
│       └── suggestion-workflow.spec.ts
└── performance/                            # Performance Tests
    ├── contact-loading.perf.ts
    └── timeline-render.perf.ts
```

## 🧪 1. BACKEND UNIT TESTS

### ContactService Test

**Datei:** `backend/src/test/java/de/freshplan/services/ContactServiceTest.java`

```java
// CLAUDE: Erstelle diesen Test
// Pfad: backend/src/test/java/de/freshplan/services/ContactServiceTest.java

package de.freshplan.services;

import de.freshplan.domain.contact.entity.Contact;
import de.freshplan.domain.contact.repository.ContactRepository;
import de.freshplan.domain.customer.entity.Customer;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.inject.Inject;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@QuarkusTest
class ContactServiceTest {
    
    @Inject
    ContactService contactService;
    
    @InjectMock
    ContactRepository contactRepository;
    
    private Customer testCustomer;
    private Contact testContact;
    
    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setId(UUID.randomUUID());
        testCustomer.setName("Test GmbH");
        
        testContact = new Contact();
        testContact.setId(UUID.randomUUID());
        testContact.setFirstName("Max");
        testContact.setLastName("Mustermann");
        testContact.setCustomer(testCustomer);
    }
    
    @Test
    void shouldCreateFirstContactAsPrimary() {
        // Given
        when(contactRepository.findByCustomerId(testCustomer.getId()))
            .thenReturn(List.of());
        
        // When
        Contact created = contactService.createContact(testCustomer.getId(), testContact);
        
        // Then
        ArgumentCaptor<Contact> captor = ArgumentCaptor.forClass(Contact.class);
        verify(contactRepository).persist(captor.capture());
        
        Contact persisted = captor.getValue();
        assertThat(persisted.isPrimary()).isTrue();
        assertThat(persisted.getFirstName()).isEqualTo("Max");
    }
    
    @Test
    void shouldNotCreateSecondContactAsPrimary() {
        // Given - bereits ein Kontakt vorhanden
        Contact existingContact = new Contact();
        existingContact.setPrimary(true);
        when(contactRepository.findByCustomerId(testCustomer.getId()))
            .thenReturn(List.of(existingContact));
        
        // When
        Contact created = contactService.createContact(testCustomer.getId(), testContact);
        
        // Then
        ArgumentCaptor<Contact> captor = ArgumentCaptor.forClass(Contact.class);
        verify(contactRepository).persist(captor.capture());
        
        Contact persisted = captor.getValue();
        assertThat(persisted.isPrimary()).isFalse();
    }
    
    @Test
    void shouldChangePrimaryContact() {
        // Given
        Contact primary = createContact(true);
        Contact secondary = createContact(false);
        
        when(contactRepository.findByCustomerId(testCustomer.getId()))
            .thenReturn(List.of(primary, secondary));
        when(contactRepository.findById(secondary.getId()))
            .thenReturn(secondary);
        
        // When
        contactService.setPrimaryContact(testCustomer.getId(), secondary.getId());
        
        // Then
        assertThat(primary.isPrimary()).isFalse();
        assertThat(secondary.isPrimary()).isTrue();
        verify(contactRepository, times(2)).persist(any());
    }
    
    @Test
    void shouldValidateEmailFormat() {
        // Given
        testContact.setEmail("invalid-email");
        
        // When/Then
        assertThatThrownBy(() -> 
            contactService.createContact(testCustomer.getId(), testContact)
        )
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("email");
    }
    
    @Test
    void shouldCalculateCompleteness() {
        // Given
        Contact incomplete = new Contact();
        incomplete.setFirstName("Test");
        incomplete.setLastName("User");
        // Keine weiteren Daten
        
        Contact complete = new Contact();
        complete.setFirstName("Max");
        complete.setLastName("Mustermann");
        complete.setEmail("max@example.com");
        complete.setPhone("+49 123 456789");
        complete.setPosition("CEO");
        complete.setDecisionLevel("EXECUTIVE");
        
        // When
        int incompleteness = contactService.calculateCompleteness(incomplete);
        int completeness = contactService.calculateCompleteness(complete);
        
        // Then
        assertThat(incompleteness).isLessThan(50);
        assertThat(completeness).isGreaterThan(80);
    }
    
    private Contact createContact(boolean primary) {
        Contact contact = new Contact();
        contact.setId(UUID.randomUUID());
        contact.setPrimary(primary);
        contact.setCustomer(testCustomer);
        return contact;
    }
}
```

### WarmthCalculator Test

**Datei:** `backend/src/test/java/de/freshplan/services/intelligence/WarmthCalculatorTest.java`

```java
// CLAUDE: Test für Warmth-Berechnung
// Pfad: backend/src/test/java/de/freshplan/services/intelligence/WarmthCalculatorTest.java

package de.freshplan.services.intelligence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class WarmthCalculatorTest {
    
    private final WarmthCalculator calculator = new WarmthCalculator();
    
    @Test
    void shouldCalculateHotScore() {
        // Given
        WarmthFactors factors = WarmthFactors.builder()
            .daysSinceLastContact(2)
            .interactionFrequency(12) // pro Monat
            .responseRate(0.9)
            .positiveInteractions(8)
            .negativeInteractions(0)
            .dealValue(50000)
            .build();
        
        // When
        int score = calculator.calculateWarmthScore(factors);
        
        // Then
        assertThat(score).isGreaterThan(80);
        assertThat(calculator.getTemperature(score)).isEqualTo("HOT");
    }
    
    @Test
    void shouldCalculateColdScore() {
        // Given
        WarmthFactors factors = WarmthFactors.builder()
            .daysSinceLastContact(120)
            .interactionFrequency(0)
            .responseRate(0.1)
            .positiveInteractions(0)
            .negativeInteractions(3)
            .dealValue(0)
            .build();
        
        // When
        int score = calculator.calculateWarmthScore(factors);
        
        // Then
        assertThat(score).isLessThan(30);
        assertThat(calculator.getTemperature(score)).isEqualTo("COLD");
    }
    
    @ParameterizedTest
    @CsvSource({
        "0, 100",   // Heute = 100%
        "7, 90",    // 1 Woche = 90%
        "30, 70",   // 1 Monat = 70%
        "60, 40",   // 2 Monate = 40%
        "90, 20",   // 3 Monate = 20%
        "180, 0"    // 6 Monate = 0%
    })
    void shouldCalculateRecencyScore(int daysSince, int expectedScore) {
        int score = calculator.calculateRecencyScore(daysSince);
        assertThat(score).isEqualTo(expectedScore);
    }
    
    @Test
    void shouldDetectTrend() {
        // Given - abnehmende Interaktionen
        LocalDateTime now = LocalDateTime.now();
        List<ContactInteraction> interactions = List.of(
            createInteraction(now.minusDays(90), "positive"),
            createInteraction(now.minusDays(60), "neutral"),
            createInteraction(now.minusDays(30), "negative")
        );
        
        // When
        String trend = calculator.analyzeTrend(interactions);
        
        // Then
        assertThat(trend).isEqualTo("DECLINING");
    }
}
```

## 🎨 2. FRONTEND COMPONENT TESTS

### SmartContactCard Test

**Datei:** `frontend/src/features/customers/components/contacts/__tests__/SmartContactCard.test.tsx`

```typescript
// CLAUDE: Component Test für SmartContactCard
// Pfad: frontend/src/features/customers/components/contacts/__tests__/SmartContactCard.test.tsx

import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { SmartContactCard } from '../SmartContactCard';
import { createMockContact } from '../../__mocks__/contactMocks';
import { vi } from 'vitest';

describe('SmartContactCard', () => {
  const mockHandlers = {
    onEdit: vi.fn(),
    onDelete: vi.fn(),
    onSetPrimary: vi.fn(),
    onAssignLocation: vi.fn(),
    onQuickAction: vi.fn()
  };
  
  beforeEach(() => {
    vi.clearAllMocks();
  });
  
  it('should render contact information', () => {
    const contact = createMockContact({
      firstName: 'Max',
      lastName: 'Mustermann',
      position: 'CEO',
      email: 'max@example.com'
    });
    
    render(<SmartContactCard contact={contact} {...mockHandlers} />);
    
    expect(screen.getByText('Max Mustermann')).toBeInTheDocument();
    expect(screen.getByText('CEO')).toBeInTheDocument();
    expect(screen.getByText('max@example.com')).toBeInTheDocument();
  });
  
  it('should show primary badge for primary contact', () => {
    const contact = createMockContact({ isPrimary: true });
    
    render(<SmartContactCard contact={contact} {...mockHandlers} />);
    
    expect(screen.getByText('Hauptkontakt')).toBeInTheDocument();
    expect(screen.getByText('Hauptkontakt')).toHaveClass('MuiChip-colorPrimary');
  });
  
  it('should display warmth indicator', () => {
    const contact = createMockContact();
    const warmth = {
      score: 85,
      temperature: 'HOT' as const,
      trendDirection: 'IMPROVING' as const
    };
    
    render(
      <SmartContactCard 
        contact={contact} 
        warmth={warmth}
        {...mockHandlers} 
      />
    );
    
    expect(screen.getByText('85')).toBeInTheDocument();
    expect(screen.getByTestId('warmth-indicator')).toHaveStyle({
      backgroundColor: '#FF6B6B' // HOT color
    });
  });
  
  it('should handle quick actions', async () => {
    const contact = createMockContact({
      phone: '+49 123 456789',
      email: 'test@example.com'
    });
    
    render(<SmartContactCard contact={contact} {...mockHandlers} />);
    
    // Click phone action
    const phoneButton = screen.getByTestId('quick-action-phone');
    await userEvent.click(phoneButton);
    
    expect(mockHandlers.onQuickAction).toHaveBeenCalledWith({
      type: 'call',
      contactId: contact.id
    });
  });
  
  it('should open menu on more button click', async () => {
    const contact = createMockContact();
    
    render(<SmartContactCard contact={contact} {...mockHandlers} />);
    
    const moreButton = screen.getByLabelText('more actions');
    await userEvent.click(moreButton);
    
    expect(screen.getByText('Bearbeiten')).toBeInTheDocument();
    expect(screen.getByText('Als Hauptkontakt')).toBeInTheDocument();
    expect(screen.getByText('Filiale zuordnen')).toBeInTheDocument();
    expect(screen.getByText('Löschen')).toBeInTheDocument();
  });
  
  it('should handle delete with confirmation', async () => {
    const contact = createMockContact();
    
    render(<SmartContactCard contact={contact} {...mockHandlers} />);
    
    // Open menu
    await userEvent.click(screen.getByLabelText('more actions'));
    
    // Click delete
    await userEvent.click(screen.getByText('Löschen'));
    
    // Confirm dialog should appear
    expect(screen.getByText('Kontakt löschen?')).toBeInTheDocument();
    
    // Confirm deletion
    await userEvent.click(screen.getByText('Löschen', { selector: 'button' }));
    
    expect(mockHandlers.onDelete).toHaveBeenCalledWith(contact.id);
  });
  
  it('should display data freshness indicator', () => {
    const contact = createMockContact({
      lastUpdated: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000) // 7 days ago
    });
    
    render(<SmartContactCard contact={contact} {...mockHandlers} />);
    
    expect(screen.getByText('Vor 7 Tagen aktualisiert')).toBeInTheDocument();
    expect(screen.getByTestId('freshness-warning')).toBeInTheDocument();
  });
});
```

### ContactTimeline Test

**Datei:** `frontend/src/features/customers/components/timeline/__tests__/ContactTimeline.test.tsx`

```typescript
// CLAUDE: Test für Contact Timeline
// Pfad: frontend/src/features/customers/components/timeline/__tests__/ContactTimeline.test.tsx

import { render, screen, fireEvent, within } from '@testing-library/react';
import { ContactTimeline } from '../ContactTimeline';
import { createMockTimelineEvents } from '../../__mocks__/timelineMocks';
import { vi } from 'vitest';

describe('ContactTimeline', () => {
  it('should render timeline events', () => {
    const events = createMockTimelineEvents(5);
    
    render(<ContactTimeline contactId="123" events={events} />);
    
    expect(screen.getAllByTestId('timeline-event')).toHaveLength(5);
  });
  
  it('should filter events by category', async () => {
    const events = [
      ...createMockTimelineEvents(3, { category: 'communication' }),
      ...createMockTimelineEvents(2, { category: 'business' }),
      ...createMockTimelineEvents(1, { category: 'system' })
    ];
    
    render(<ContactTimeline contactId="123" events={events} />);
    
    // Initially all events visible
    expect(screen.getAllByTestId('timeline-event')).toHaveLength(6);
    
    // Filter by communication
    await userEvent.click(screen.getByText('Kommunikation'));
    
    expect(screen.getAllByTestId('timeline-event')).toHaveLength(3);
  });
  
  it('should group events by day', async () => {
    const today = new Date();
    const yesterday = new Date(today);
    yesterday.setDate(yesterday.getDate() - 1);
    
    const events = [
      createMockTimelineEvent({ timestamp: today }),
      createMockTimelineEvent({ timestamp: today }),
      createMockTimelineEvent({ timestamp: yesterday }),
    ];
    
    render(<ContactTimeline contactId="123" events={events} groupBy="day" />);
    
    expect(screen.getByText('Heute')).toBeInTheDocument();
    expect(screen.getByText('Gestern')).toBeInTheDocument();
  });
  
  it('should load more events on scroll', async () => {
    const loadMore = vi.fn();
    const events = createMockTimelineEvents(10);
    
    render(
      <ContactTimeline 
        contactId="123" 
        events={events}
        hasMore={true}
        onLoadMore={loadMore}
      />
    );
    
    // Scroll to bottom
    const container = screen.getByTestId('timeline-container');
    fireEvent.scroll(container, { 
      target: { scrollTop: container.scrollHeight } 
    });
    
    await waitFor(() => {
      expect(loadMore).toHaveBeenCalled();
    });
  });
});
```

## 🚀 3. E2E TESTS MIT PLAYWRIGHT

### Complete Contact Management Flow

**Datei:** `frontend/e2e/step3/contact-management.spec.ts`

```typescript
// CLAUDE: E2E Test für kompletten Contact Management Flow
// Pfad: frontend/e2e/step3/contact-management.spec.ts

import { test, expect } from '@playwright/test';
import { loginAsUser, navigateToCustomer } from '../helpers';

test.describe('Contact Management E2E', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsUser(page, 'sales@freshplan.de', 'password');
    await navigateToCustomer(page, 'Test GmbH');
  });
  
  test('should manage multiple contacts lifecycle', async ({ page }) => {
    // Navigate to contacts tab
    await page.click('text=Ansprechpartner');
    
    // Add first contact (automatically primary)
    await page.click('text=Ansprechpartner hinzufügen');
    await page.fill('[name="firstName"]', 'Max');
    await page.fill('[name="lastName"]', 'Mustermann');
    await page.fill('[name="email"]', 'max@test.com');
    await page.fill('[name="phone"]', '+49 123 456789');
    await page.selectOption('[name="decisionLevel"]', 'executive');
    await page.click('text=Speichern');
    
    // Verify primary badge
    await expect(page.locator('text=Hauptkontakt').first()).toBeVisible();
    
    // Add second contact
    await page.click('text=Ansprechpartner hinzufügen');
    await page.fill('[name="firstName"]', 'Maria');
    await page.fill('[name="lastName"]', 'Musterfrau');
    await page.fill('[name="email"]', 'maria@test.com');
    await page.selectOption('[name="decisionLevel"]', 'operational');
    
    // Add relationship data
    await page.click('text=Beziehungsebene');
    await page.check('text=Golf');
    await page.check('text=Wein');
    await page.fill('[name="personalNotes"]', 'Mag italienisches Essen');
    await page.click('text=Speichern');
    
    // Verify two contacts exist
    await expect(page.locator('[data-testid="contact-card"]')).toHaveCount(2);
    
    // Change primary contact
    await page.locator('[data-testid="contact-card"]').nth(1).hover();
    await page.locator('[data-testid="contact-card"]').nth(1)
      .locator('[aria-label="more"]').click();
    await page.click('text=Als Hauptkontakt festlegen');
    
    // Verify primary changed
    await expect(
      page.locator('[data-testid="contact-card"]').nth(1)
        .locator('text=Hauptkontakt')
    ).toBeVisible();
    
    // Test quick actions
    await page.locator('[data-testid="quick-action-email"]').first().click();
    // Should open email client (can't test actual opening)
    
    // Test timeline
    await page.click('text=Timeline');
    await expect(page.locator('text=Kontakt hinzugefügt')).toBeVisible();
    await expect(page.locator('text=Als Hauptkontakt festgelegt')).toBeVisible();
    
    // Test warmth indicator
    await expect(page.locator('[data-testid="warmth-score"]')).toBeVisible();
    
    // Delete contact
    await page.locator('[data-testid="contact-card"]').nth(1).hover();
    await page.locator('[data-testid="contact-card"]').nth(1)
      .locator('[aria-label="more"]').click();
    await page.click('text=Löschen');
    
    // Confirm deletion
    await page.click('button:has-text("Löschen")');
    
    // Verify only one contact remains
    await expect(page.locator('[data-testid="contact-card"]')).toHaveCount(1);
  });
  
  test('should track relationship warmth', async ({ page }) => {
    // Create contact
    await createTestContact(page);
    
    // Log interaction
    await page.click('text=Interaktion erfassen');
    await page.selectOption('[name="type"]', 'phone_outgoing');
    await page.fill('[name="notes"]', 'Positives Gespräch über neue Produkte');
    await page.selectOption('[name="sentiment"]', 'positive');
    await page.click('text=Speichern');
    
    // Check warmth increased
    const warmthScore = await page.locator('[data-testid="warmth-score"]').textContent();
    expect(parseInt(warmthScore || '0')).toBeGreaterThan(50);
    
    // Check timeline
    await page.click('text=Timeline');
    await expect(page.locator('text=Ausgehender Anruf')).toBeVisible();
    await expect(page.locator('[data-testid="sentiment-positive"]')).toBeVisible();
  });
  
  test('should show smart suggestions', async ({ page }) => {
    // Navigate to dashboard
    await page.goto('/dashboard');
    
    // Check birthday reminder
    const today = new Date();
    await expect(page.locator('text=Geburtstage')).toBeVisible();
    
    // Check cooling relationships
    await expect(page.locator('text=Abkühlende Beziehungen')).toBeVisible();
    
    // Act on suggestion
    await page.locator('[data-testid="suggestion-card"]').first()
      .locator('text=Anrufen').click();
    
    // Verify action was tracked
    await page.goto('/customers/test-gmbh/contacts');
    await page.click('text=Timeline');
    await expect(page.locator('text=Suggestion befolgt')).toBeVisible();
  });
});
```

## 📊 4. PERFORMANCE TESTS

**Datei:** `frontend/src/features/customers/__tests__/performance/contact-loading.perf.ts`

```typescript
// CLAUDE: Performance Test für Contact Loading
// Pfad: frontend/src/features/customers/__tests__/performance/contact-loading.perf.ts

import { measurePerformance } from '@/test-utils/performance';

describe('Contact Loading Performance', () => {
  it('should load 100 contacts in under 500ms', async () => {
    const result = await measurePerformance(async () => {
      const contacts = await contactApi.getContacts('customer-id');
      return contacts;
    });
    
    expect(result.duration).toBeLessThan(500);
    expect(result.memoryUsed).toBeLessThan(10 * 1024 * 1024); // 10MB
  });
  
  it('should render 50 contact cards in under 200ms', async () => {
    const contacts = createMockContacts(50);
    
    const result = await measurePerformance(() => {
      render(<ContactListView contacts={contacts} />);
    });
    
    expect(result.duration).toBeLessThan(200);
    expect(result.fps).toBeGreaterThan(30);
  });
  
  it('should handle timeline with 1000 events', async () => {
    const events = createMockTimelineEvents(1000);
    
    const result = await measurePerformance(() => {
      render(<ContactTimeline events={events} />);
    });
    
    // Mit virtualisierung sollte es schnell sein
    expect(result.duration).toBeLessThan(300);
    expect(result.renderCount).toBeLessThan(50); // Nur sichtbare Items
  });
});
```

## 📋 TEST COVERAGE REPORT

```bash
# Führe alle Tests aus und generiere Coverage Report
npm run test:coverage

# Backend Coverage
./mvnw test jacoco:report

# E2E Tests
npm run test:e2e

# Performance Tests
npm run test:perf
```

### Erwartete Coverage:

| Komponente | Unit | Integration | E2E | Gesamt |
|------------|------|-------------|-----|--------|
| SmartContactCard | 95% | 90% | ✅ | 92% |
| ContactTimeline | 92% | 88% | ✅ | 90% |
| RelationshipIntelligence | 90% | 85% | ✅ | 88% |
| SuggestionEngine | 94% | 89% | ✅ | 91% |
| **Gesamt** | **93%** | **88%** | **✅** | **90%** |

## 🐛 COMMON ISSUES & FIXES

### Issue 1: Flaky Timeline Tests
```typescript
// Problem: Timeline events kommen in falscher Reihenfolge
// Fix: Sortierung explizit testen
await waitFor(() => {
  const events = screen.getAllByTestId('timeline-event');
  const timestamps = events.map(e => e.getAttribute('data-timestamp'));
  expect(timestamps).toEqual([...timestamps].sort().reverse());
});
```

### Issue 2: Warmth Score Calculation
```typescript
// Problem: Score wird nicht korrekt berechnet
// Fix: Mock Date für konsistente Tests
vi.useFakeTimers();
vi.setSystemTime(new Date('2024-01-15'));
// ... test
vi.useRealTimers();
```

### Issue 3: Primary Contact Constraint
```sql
-- Problem: Duplicate primary contacts
-- Fix: Database constraint
ALTER TABLE contacts 
ADD CONSTRAINT unique_primary_per_customer 
UNIQUE (customer_id, is_primary) 
WHERE is_primary = true;
```

## ✅ FINAL CHECKLIST

### Unit Tests
- [ ] Alle Services getestet
- [ ] Alle Components getestet  
- [ ] Alle Stores getestet
- [ ] Edge Cases abgedeckt
- [ ] Error Handling getestet

### Integration Tests
- [ ] API Endpoints getestet
- [ ] Database Operations getestet
- [ ] Store/Service Integration
- [ ] Navigation Flows

### E2E Tests
- [ ] Complete User Journey
- [ ] Multi-Contact Management
- [ ] Warmth Tracking
- [ ] Timeline Functionality
- [ ] Smart Suggestions

### Performance
- [ ] Load Time < 500ms
- [ ] Render Time < 200ms
- [ ] Memory Usage < 50MB
- [ ] No Memory Leaks

### Quality Gates
- [ ] Coverage > 85%
- [ ] No Console Errors
- [ ] Accessibility Score > 90
- [ ] Lighthouse Score > 85

## 🚀 NÄCHSTE SCHRITTE

1. **Code Review** durchführen
2. **Merge Request** erstellen
3. **CI/CD Pipeline** grün
4. **Deploy to Staging**
5. **UAT mit Stakeholdern**

---

**Status:** BEREIT FÜR IMPLEMENTIERUNG  
**Geschätzte Zeit:** 180 Minuten (3 Stunden)  
**Abschluss:** [→ Step3 Summary](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/README.md)  
**Parent:** [↑ Step3 Übersicht](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/README.md)

**Testing = Qualität & Vertrauen! 🧪✨**