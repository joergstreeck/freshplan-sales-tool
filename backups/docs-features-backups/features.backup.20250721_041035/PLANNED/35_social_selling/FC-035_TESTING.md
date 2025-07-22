# 🧪 FC-035 TESTING STRATEGY

**Companion zu:** FC-035_OVERVIEW.md  
**Zweck:** Test-Strategie, Szenarien & Beispiele  

---

## 🎯 TEST COVERAGE ZIELE

- **Unit Tests:** ≥ 80% Coverage
- **Integration Tests:** Alle API Endpoints
- **E2E Tests:** Critical User Journeys
- **Performance:** Response < 200ms

---

## 🧪 UNIT TESTS

### Frontend Component Tests
```typescript
// SocialProfileCard.test.tsx
describe('SocialProfileCard', () => {
  it('should display LinkedIn profile when available', () => {
    const mockCustomer = {
      id: '123',
      profiles: {
        linkedin: {
          name: 'Restaurant Hamburg',
          url: 'https://linkedin.com/company/restaurant-hamburg'
        }
      }
    };
    
    render(<SocialProfileCard customer={mockCustomer} />);
    
    expect(screen.getByText('Restaurant Hamburg')).toBeInTheDocument();
    expect(screen.getByRole('link')).toHaveAttribute('href', 
      'https://linkedin.com/company/restaurant-hamburg'
    );
  });
  
  it('should trigger sync on button click', async () => {
    const mockSync = jest.fn();
    render(<SocialProfileCard customer={mockCustomer} onSync={mockSync} />);
    
    await userEvent.click(screen.getByLabelText('Sync profiles'));
    
    expect(mockSync).toHaveBeenCalledWith(mockCustomer.id);
  });
});
```

### Hook Tests
```typescript
// useEngagementOpportunities.test.ts
describe('useEngagementOpportunities', () => {
  it('should fetch and sort opportunities by priority', async () => {
    const { result } = renderHook(() => useEngagementOpportunities());
    
    await waitFor(() => {
      expect(result.current.opportunities).toHaveLength(3);
      expect(result.current.opportunities[0].priority).toBe('high');
    });
  });
});
```

---

## 🔗 INTEGRATION TESTS

### API Endpoint Tests
```java
@QuarkusTest
public class SocialSellingResourceTest {
  
  @Test
  public void testGetSocialProfiles() {
    given()
      .when()
      .get("/api/customers/123/social-profiles")
      .then()
      .statusCode(200)
      .body("profiles.size()", is(2))
      .body("profiles[0].platform", is("linkedin"));
  }
  
  @Test
  public void testEngagementOpportunities() {
    given()
      .when()
      .get("/api/engagement-opportunities")
      .then()
      .statusCode(200)
      .body("size()", greaterThan(0))
      .body("[0].trigger", notNullValue());
  }
}
```

### Service Integration Tests
```java
@QuarkusTest
public class LinkedInServiceTest {
  
  @Inject
  LinkedInService linkedInService;
  
  @Test
  public void testSyncCompanyFeed() {
    // Mock LinkedIn API
    mockLinkedInAPI.stubFor(
      get(urlEqualTo("/v2/organizations/123/updates"))
      .willReturn(aResponse()
        .withStatus(200)
        .withBody(readResource("linkedin-updates.json"))
      )
    );
    
    List<Post> posts = linkedInService
      .getCompanyFeed("123")
      .toCompletableFuture()
      .join();
    
    assertThat(posts).hasSize(5);
    assertThat(posts.get(0).getType()).isEqualTo("company_update");
  }
}
```

---

## 🎭 E2E TEST SCENARIOS

### Scenario 1: Social Profile Setup
```typescript
test('User adds LinkedIn profile to customer', async ({ page }) => {
  // Navigate to customer
  await page.goto('/customers/123');
  
  // Open social profiles section
  await page.click('text=Social Media Profile');
  
  // Add LinkedIn URL
  await page.click('button:has-text("Add Profile")');
  await page.selectOption('select[name="platform"]', 'linkedin');
  await page.fill('input[name="profileUrl"]', 
    'https://linkedin.com/company/test-restaurant'
  );
  await page.click('button:has-text("Save")');
  
  // Verify profile appears
  await expect(page.locator('text=test-restaurant')).toBeVisible();
});
```

### Scenario 2: Engagement Workflow
```typescript
test('User engages with job change alert', async ({ page }) => {
  // Check dashboard
  await page.goto('/dashboard');
  
  // Find engagement alert
  const alert = page.locator('text=Neue Position');
  await expect(alert).toBeVisible();
  
  // Click congratulate
  await page.click('button:has-text("Gratulieren")');
  
  // Verify comment dialog
  await expect(page.locator('text=Kommentar verfassen')).toBeVisible();
  
  // Send congratulation
  await page.fill('textarea', 'Herzlichen Glückwunsch!');
  await page.click('button:has-text("Senden")');
  
  // Verify success
  await expect(page.locator('text=Erfolgreich gesendet')).toBeVisible();
});
```

---

## 🏃 PERFORMANCE TESTS

### Load Test Scenarios
```javascript
// k6 load test
import http from 'k6/http';
import { check } from 'k6';

export let options = {
  stages: [
    { duration: '2m', target: 100 },
    { duration: '5m', target: 100 },
    { duration: '2m', target: 0 },
  ],
};

export default function() {
  // Test engagement opportunities endpoint
  let response = http.get('http://localhost:8080/api/engagement-opportunities');
  
  check(response, {
    'status is 200': (r) => r.status === 200,
    'response time < 200ms': (r) => r.timings.duration < 200,
  });
}
```

---

## 🐛 EDGE CASES & ERROR HANDLING

### Test Cases
1. **API Rate Limiting**
   - LinkedIn API returns 429
   - Implement exponential backoff
   
2. **Invalid Profile URLs**
   - Malformed URLs
   - Private profiles
   - Deleted profiles

3. **Sync Failures**
   - Network timeout
   - Invalid credentials
   - API changes

### Error Handling Tests
```typescript
it('should handle LinkedIn API errors gracefully', async () => {
  // Mock API error
  mockLinkedInAPI.rejectGetCompanyFeed(new Error('Rate limited'));
  
  render(<SocialProfileCard customer={mockCustomer} />);
  
  await waitFor(() => {
    expect(screen.getByText('Sync fehlgeschlagen')).toBeInTheDocument();
    expect(screen.getByText('Später erneut versuchen')).toBeInTheDocument();
  });
});
```

---

## 📊 TEST DATA

### Mock Social Profiles
```json
{
  "testProfiles": [
    {
      "platform": "linkedin",
      "profileUrl": "https://linkedin.com/company/test-restaurant",
      "profileData": {
        "name": "Test Restaurant GmbH",
        "industry": "Gastgewerbe",
        "size": "11-50"
      }
    }
  ]
}
```

### Mock Engagement Opportunities
```json
{
  "opportunities": [
    {
      "trigger": "Max Mustermann wurde zum Geschäftsführer befördert",
      "triggerType": "job_change",
      "suggestedAction": "Gratulieren",
      "priority": "high"
    }
  ]
}
```