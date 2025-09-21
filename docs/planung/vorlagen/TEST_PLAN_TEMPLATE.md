# Test Plan: [Feature Name]

**Feature-Code:** FC-XXX
**Test Plan Version:** v1.0
**Erstellt:** [YYYY-MM-DD]
**Test Lead:** [Name]
**Status:** 🟡 Entwurf / 🔵 In Ausführung / 🟠 Review / 🟢 Abgeschlossen

## 📋 Test Overview

### Feature Description
**Feature:** [Feature Name]
**Beschreibung:** [Was wird getestet?]
**Business Impact:** [Welche Business-Kritikalität hat das Feature?]
**Test Scope:** [Was ist im/außerhalb des Test-Scopes?]

### Test Objectives
- [ ] **Funktionalität:** Alle Features funktionieren wie spezifiziert
- [ ] **Performance:** Response-Zeiten unter definierten Limits
- [ ] **Security:** Sicherheitsanforderungen erfüllt
- [ ] **Usability:** User Experience entspricht Standards
- [ ] **Compatibility:** Browser/Device-Kompatibilität gegeben
- [ ] **Reliability:** System ist stabil unter Last

### Test Environment
- **Frontend:** React Development Server (localhost:5173)
- **Backend:** Quarkus Development Mode (localhost:8080)
- **Database:** PostgreSQL Test Instance
- **Authentication:** Keycloak Test Realm
- **Browser:** Chrome (latest), Firefox (latest), Safari (latest)
- **Devices:** Desktop (1920x1080), Tablet (768x1024), Mobile (375x667)

## 🧪 Test Strategy

### Test Pyramid
```
        🔺 E2E Tests (10%)
       Critical User Journeys

     🔹 Integration Tests (20%)
    API Contracts & Module Integration

  🔸 Unit Tests (70%)
 Business Logic & Component Testing
```

### Test Types & Coverage
| Test Type | Coverage Target | Tools | Responsibility |
|-----------|----------------|-------|----------------|
| **Unit Tests** | >80% | Vitest + React Testing Library | Developer |
| **Integration Tests** | 100% API Endpoints | RestAssured + TestContainers | Developer |
| **Component Tests** | 100% UI Components | Storybook + Chromatic | Developer |
| **E2E Tests** | Critical Paths | Playwright | QA + Developer |
| **Performance Tests** | Key Transactions | k6 + Artillery | QA |
| **Security Tests** | OWASP Top 10 | ZAP + Manual | Security Team |
| **Accessibility Tests** | WCAG 2.1 AA | axe-core + Manual | QA |

## 🎯 Test Cases

### Functional Test Cases

#### FC-XXX-TC-001: [Basic Functionality]
**Priority:** High
**Type:** Functional
**Automation:** Yes

**Preconditions:**
- User is authenticated
- Database contains test data
- Feature flag is enabled

**Test Steps:**
1. Navigate to [Feature URL]
2. Verify page loads correctly
3. Interact with main functionality
4. Verify expected outcome

**Expected Result:**
- Page loads within 2 seconds
- All UI elements are visible
- Functionality works as designed
- No console errors

**Test Data:**
```json
{
  "user": {
    "role": "admin",
    "permissions": ["read", "write"]
  },
  "testData": {
    "validInput": "test value",
    "invalidInput": ""
  }
}
```

#### FC-XXX-TC-002: [Error Handling]
**Priority:** High
**Type:** Negative
**Automation:** Yes

**Test Steps:**
1. Navigate to feature
2. Enter invalid data
3. Submit form/trigger action
4. Verify error handling

**Expected Result:**
- Clear error messages displayed
- No application crash
- User can recover from error
- Error logged appropriately

#### FC-XXX-TC-003: [Permission-Based Access]
**Priority:** High
**Type:** Security
**Automation:** Yes

**Test Steps:**
1. Login with different user roles
2. Attempt to access feature
3. Verify access control

**Expected Result:**
- Authorized users can access
- Unauthorized users see 403/redirect
- No data leakage occurs

### Performance Test Cases

#### FC-XXX-PC-001: [Load Performance]
**Priority:** High
**Type:** Performance
**Load:** 100 concurrent users

**Metrics:**
- Response Time: < 200ms P95
- Throughput: > 1000 requests/minute
- Error Rate: < 0.1%
- Resource Usage: < 80% CPU/Memory

#### FC-XXX-PC-002: [Stress Testing]
**Priority:** Medium
**Type:** Stress
**Load:** 500 concurrent users for 10 minutes

**Acceptance Criteria:**
- No memory leaks
- Graceful degradation
- Recovery after load reduction

### Security Test Cases

#### FC-XXX-SC-001: [Input Validation]
**Priority:** High
**Type:** Security

**Test Vectors:**
- SQL Injection: `'; DROP TABLE users; --`
- XSS: `<script>alert('xss')</script>`
- Path Traversal: `../../../etc/passwd`
- Large Payload: 1MB+ request

**Expected Result:**
- All malicious inputs rejected
- Proper error messages
- No system compromise

#### FC-XXX-SC-002: [Authentication]
**Priority:** Critical
**Type:** Security

**Test Cases:**
- Expired token handling
- Token manipulation
- Session fixation
- Brute force protection

### Accessibility Test Cases

#### FC-XXX-AC-001: [Keyboard Navigation]
**Priority:** High
**Type:** Accessibility

**Test Steps:**
1. Navigate using only keyboard
2. Tab through all interactive elements
3. Use keyboard shortcuts
4. Verify focus indicators

**Expected Result:**
- All functionality accessible via keyboard
- Logical tab order
- Clear focus indicators
- Keyboard shortcuts work

#### FC-XXX-AC-002: [Screen Reader Compatibility]
**Priority:** High
**Type:** Accessibility

**Tools:** NVDA, JAWS, VoiceOver
**Test Steps:**
1. Navigate with screen reader
2. Verify content is announced correctly
3. Test form interactions
4. Verify error announcements

## 🔧 Test Automation

### Unit Test Examples
```typescript
// Frontend Unit Test
describe('[ComponentName]', () => {
  it('should render with correct props', () => {
    const props = { variant: 'primary', size: 'large' };
    render(<ComponentName {...props} />);

    expect(screen.getByRole('button')).toHaveClass('primary');
    expect(screen.getByRole('button')).toHaveClass('large');
  });

  it('should handle click events', () => {
    const handleClick = vi.fn();
    render(<ComponentName onClick={handleClick} />);

    fireEvent.click(screen.getByRole('button'));
    expect(handleClick).toHaveBeenCalledTimes(1);
  });

  it('should show loading state', () => {
    render(<ComponentName loading />);
    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });
});

// Backend Unit Test
@QuarkusTest
class ServiceTest {
    @Inject
    UserService userService;

    @Test
    void shouldCreateUser() {
        var request = new CreateUserRequest("john.doe", "john@example.com");
        var user = userService.createUser(request);

        assertThat(user.getUsername()).isEqualTo("john.doe");
        assertThat(user.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void shouldThrowExceptionForDuplicateEmail() {
        var request = new CreateUserRequest("jane.doe", "existing@example.com");

        assertThatThrownBy(() -> userService.createUser(request))
            .isInstanceOf(EmailAlreadyExistsException.class);
    }
}
```

### Integration Test Examples
```typescript
// API Integration Test
describe('User API', () => {
  it('should create user via API', async () => {
    const userData = {
      username: 'testuser',
      email: 'test@example.com'
    };

    const response = await fetch('/api/users', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${getTestToken()}`
      },
      body: JSON.stringify(userData)
    });

    expect(response.status).toBe(201);
    const user = await response.json();
    expect(user.username).toBe('testuser');
  });

  it('should return 400 for invalid data', async () => {
    const invalidData = { username: '' }; // Missing required fields

    const response = await fetch('/api/users', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${getTestToken()}`
      },
      body: JSON.stringify(invalidData)
    });

    expect(response.status).toBe(400);
    const error = await response.json();
    expect(error.message).toContain('validation failed');
  });
});
```

### E2E Test Examples
```typescript
// Playwright E2E Test
test.describe('[Feature Name]', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
    await page.fill('[data-testid="username"]', 'testuser');
    await page.fill('[data-testid="password"]', 'password');
    await page.click('[data-testid="login-button"]');
    await page.waitForURL('/dashboard');
  });

  test('should create new record', async ({ page }) => {
    // Navigate to feature
    await page.click('[data-testid="feature-menu"]');
    await page.click('[data-testid="create-button"]');

    // Fill form
    await page.fill('[data-testid="name-input"]', 'Test Record');
    await page.fill('[data-testid="description-input"]', 'Test Description');

    // Submit
    await page.click('[data-testid="submit-button"]');

    // Verify success
    await expect(page.locator('[data-testid="success-message"]')).toBeVisible();
    await expect(page.locator('[data-testid="record-list"]')).toContainText('Test Record');
  });

  test('should handle form validation', async ({ page }) => {
    await page.click('[data-testid="feature-menu"]');
    await page.click('[data-testid="create-button"]');

    // Submit empty form
    await page.click('[data-testid="submit-button"]');

    // Verify validation errors
    await expect(page.locator('[data-testid="name-error"]')).toBeVisible();
    await expect(page.locator('[data-testid="name-error"]')).toContainText('Name is required');
  });
});
```

## 📊 Performance Testing

### Load Test Configuration
```javascript
// k6 Load Test
import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
  stages: [
    { duration: '2m', target: 20 }, // Ramp-up
    { duration: '5m', target: 20 }, // Stay at 20 users
    { duration: '2m', target: 50 }, // Ramp to 50 users
    { duration: '5m', target: 50 }, // Stay at 50 users
    { duration: '2m', target: 0 },  // Ramp-down
  ],
  thresholds: {
    http_req_duration: ['p(95)<200'], // 95% of requests under 200ms
    http_req_failed: ['rate<0.01'],   // Error rate under 1%
  },
};

export default function () {
  const response = http.get('https://api.freshplan.de/api/v1/users', {
    headers: { Authorization: `Bearer ${getToken()}` },
  });

  check(response, {
    'status is 200': (r) => r.status === 200,
    'response time < 200ms': (r) => r.timings.duration < 200,
  });

  sleep(1);
}
```

### Performance Monitoring
```yaml
# Performance Metrics to Monitor
response_time:
  p50: < 100ms
  p95: < 200ms
  p99: < 500ms

throughput:
  target: > 1000 rps
  max: 5000 rps

error_rate:
  target: < 0.1%
  max: 1%

resource_usage:
  cpu: < 80%
  memory: < 80%
  disk: < 70%
```

## 🔒 Security Testing

### Security Test Cases
```yaml
authentication:
  - test_case: "Invalid token handling"
    steps:
      - Send request with expired token
      - Send request with malformed token
      - Send request without token
    expected: 401 Unauthorized

authorization:
  - test_case: "Role-based access control"
    steps:
      - Access admin endpoint as regular user
      - Access user data as different user
    expected: 403 Forbidden

input_validation:
  - test_case: "SQL injection prevention"
    payload: "'; DROP TABLE users; --"
    expected: Input rejected, no SQL execution

  - test_case: "XSS prevention"
    payload: "<script>alert('xss')</script>"
    expected: Script tags escaped/filtered
```

### Penetration Testing
```bash
# OWASP ZAP Automated Scan
zap-baseline.py -t https://api.freshplan.de \
  -J zap-report.json \
  -r zap-report.html

# Manual Security Tests
- Session management
- CSRF protection
- CORS configuration
- HTTP security headers
- Data encryption
```

## 📱 Browser & Device Testing

### Browser Compatibility Matrix
| Browser | Version | Desktop | Mobile | Status |
|---------|---------|---------|--------|--------|
| Chrome | Latest | ✅ | ✅ | Pass |
| Firefox | Latest | ✅ | ✅ | Pass |
| Safari | Latest | ✅ | ✅ | Pass |
| Edge | Latest | ✅ | ❌ | N/A |

### Device Testing
| Device Type | Screen Size | Test Scenarios |
|-------------|-------------|----------------|
| Desktop | 1920x1080 | Full feature set |
| Laptop | 1366x768 | Responsive layout |
| Tablet | 768x1024 | Touch interactions |
| Mobile | 375x667 | Mobile-optimized UI |

## 📈 Test Metrics & Reporting

### Coverage Metrics
```yaml
unit_tests:
  target: > 80%
  current: 85%
  trend: ↗️ +2%

integration_tests:
  target: 100% API endpoints
  current: 95%
  missing: [DELETE /api/users/{id}]

e2e_tests:
  target: Critical user journeys
  current: 80%
  coverage: [login, create, read, update]
```

### Test Execution Report
```markdown
## Test Execution Summary
**Date:** 2023-01-01
**Environment:** Test
**Build:** v2.1.0-rc.1

### Results
- **Total Tests:** 245
- **Passed:** 238 (97%)
- **Failed:** 5 (2%)
- **Skipped:** 2 (1%)

### Failed Tests
1. FC-XXX-TC-015: Permission error handling
2. FC-XXX-PC-001: Load performance under stress
3. FC-XXX-AC-002: Screen reader navigation

### Risk Assessment
- **High:** Performance degradation under load
- **Medium:** Accessibility issues in edge cases
- **Low:** Minor UI inconsistencies
```

## 🚨 Test Environment Setup

### Local Development
```bash
# Setup test environment
npm run test:setup

# Run all tests
npm run test:all

# Run specific test suite
npm run test:unit
npm run test:integration
npm run test:e2e

# Run with coverage
npm run test:coverage
```

### CI/CD Pipeline Integration
```yaml
# GitHub Actions Test Pipeline
name: Test Pipeline
on: [push, pull_request]

jobs:
  unit-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-node@v3
      - run: npm ci
      - run: npm run test:unit
      - run: npm run test:coverage

  integration-tests:
    runs-on: ubuntu-latest
    services:
      postgres:
        image: postgres:13
        env:
          POSTGRES_PASSWORD: test
    steps:
      - run: npm run test:integration

  e2e-tests:
    runs-on: ubuntu-latest
    steps:
      - run: npx playwright install
      - run: npm run test:e2e
```

## 📝 Test Data Management

### Test Data Strategy
```yaml
test_data:
  approach: "Fresh data per test"
  cleanup: "Automatic after each test"
  isolation: "Each test uses unique data"

data_sources:
  - fixtures: "Static test data files"
  - factories: "Generated test data"
  - mocks: "Simulated external services"
```

### Test Data Examples
```typescript
// Test Data Factory
export const createTestUser = (overrides = {}) => ({
  id: generateUUID(),
  username: 'testuser_' + Date.now(),
  email: 'test_' + Date.now() + '@example.com',
  role: 'user',
  createdAt: new Date().toISOString(),
  ...overrides,
});

// Test Fixtures
export const testUsers = [
  {
    id: '123e4567-e89b-12d3-a456-426614174000',
    username: 'admin_user',
    email: 'admin@freshplan.de',
    role: 'admin',
  },
  {
    id: '987fcdeb-51a2-43d1-9f12-456789abcdef',
    username: 'regular_user',
    email: 'user@freshplan.de',
    role: 'user',
  },
];
```

## ✅ Definition of Done

### Test Completion Criteria
- [ ] All test cases executed
- [ ] >80% unit test coverage achieved
- [ ] All integration tests passing
- [ ] Critical E2E scenarios validated
- [ ] Performance targets met
- [ ] Security vulnerabilities addressed
- [ ] Accessibility requirements fulfilled
- [ ] Browser compatibility confirmed
- [ ] Test report generated
- [ ] Bugs documented and triaged

### Quality Gates
- [ ] **Code Coverage:** >80% for new code
- [ ] **Performance:** P95 < 200ms for API calls
- [ ] **Security:** No high/critical vulnerabilities
- [ ] **Accessibility:** WCAG 2.1 AA compliance
- [ ] **Browser Support:** Latest Chrome, Firefox, Safari
- [ ] **Mobile Support:** Responsive design working

## 📚 Test Resources

### Documentation
- [Testing Standards](../grundlagen/TESTING_GUIDE.md)
- [API Documentation](../vorlagen/API_SPEC_TEMPLATE.md)
- [Component Specifications](../vorlagen/COMPONENT_SPEC_TEMPLATE.md)

### Tools & Utilities
- **Unit Testing:** Vitest, React Testing Library
- **Integration Testing:** RestAssured, TestContainers
- **E2E Testing:** Playwright
- **Performance Testing:** k6, Artillery
- **Security Testing:** OWASP ZAP
- **Accessibility Testing:** axe-core, Pa11y

---

**📋 Template verwendet:** TEST_PLAN_TEMPLATE.md v1.0
**📅 Letzte Aktualisierung:** [YYYY-MM-DD]
**👨‍💻 Test Lead:** [Name]

**🎯 Dieser Test Plan sichert die Qualität und Zuverlässigkeit von Feature FC-XXX**