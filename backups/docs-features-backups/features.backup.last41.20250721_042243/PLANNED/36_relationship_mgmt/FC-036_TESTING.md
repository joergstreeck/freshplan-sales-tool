# 🧪 FC-036 TESTING STRATEGY

**Companion zu:** FC-036_OVERVIEW.md  
**Zweck:** Test-Szenarien für Beziehungsmanagement  

---

## 🎯 TEST COVERAGE ZIELE

- **Unit Tests:** ≥ 85% Coverage (Privacy-kritisch!)
- **Integration Tests:** IndexedDB & Encryption
- **E2E Tests:** Complete User Journeys
- **Privacy Tests:** Datenschutz-Compliance

---

## 🧪 UNIT TESTS

### Component Tests
```typescript
// PersonalDetailsCard.test.tsx
describe('PersonalDetailsCard', () => {
  it('should only show data to owner', async () => {
    const mockContact = {
      id: '123',
      personalDetails: {
        userId: 'user-456',
        familyStatus: 'married',
        children: [{ name: 'Lisa', age: 8 }]
      }
    };
    
    // Mock different user
    mockCurrentUser({ id: 'user-789' });
    
    render(<PersonalDetailsCard contact={mockContact} />);
    
    expect(screen.queryByText('Lisa')).not.toBeInTheDocument();
    expect(screen.getByText('Keine persönlichen Details')).toBeInTheDocument();
  });
  
  it('should encrypt sensitive data before saving', async () => {
    const encryptSpy = jest.spyOn(crypto, 'encrypt');
    
    const { getByRole } = render(<PersonalDetailsCard contact={mockContact} />);
    
    await userEvent.type(getByRole('textbox', { name: /spouse/i }), 'Jane Doe');
    await userEvent.click(getByRole('button', { name: /save/i }));
    
    expect(encryptSpy).toHaveBeenCalledWith(
      expect.objectContaining({ spouse: { name: 'Jane Doe' } })
    );
  });
});
```

### Hook Tests
```typescript
// usePersonalDetails.test.ts
describe('usePersonalDetails', () => {
  it('should load data from IndexedDB', async () => {
    // Mock IndexedDB
    const mockData = {
      contactId: '123',
      hobbies: ['Golf', 'Cooking']
    };
    
    await db.personalDetails.add(mockData);
    
    const { result } = renderHook(() => usePersonalDetails('123'));
    
    await waitFor(() => {
      expect(result.current.details).toEqual(mockData);
    });
  });
  
  it('should handle encryption errors gracefully', async () => {
    jest.spyOn(crypto, 'decrypt').mockRejectedValue(new Error('Invalid key'));
    
    const { result } = renderHook(() => usePersonalDetails('123'));
    
    await waitFor(() => {
      expect(result.current.error).toBe('Daten konnten nicht entschlüsselt werden');
      expect(result.current.details).toBeNull();
    });
  });
});
```

---

## 🔐 PRIVACY & SECURITY TESTS

### Data Isolation Tests
```typescript
describe('Data Privacy', () => {
  it('should isolate data between users', async () => {
    // User A data
    await loginAs('userA');
    await db.personalDetails.add({
      contactId: 'contact-1',
      userId: 'userA',
      notes: 'Private note from A'
    });
    
    // Switch to User B
    await loginAs('userB');
    const userBData = await db.personalDetails
      .where('contactId').equals('contact-1')
      .toArray();
    
    expect(userBData).toHaveLength(0);
  });
  
  it('should encrypt data at rest', async () => {
    const rawData = { familyStatus: 'married', children: ['Max', 'Lisa'] };
    const stored = await storePersonalDetails('contact-1', rawData);
    
    // Check raw IndexedDB
    const dbData = await getRawIndexedDBData('personalDetails', stored.id);
    
    expect(dbData.data).not.toContain('married');
    expect(dbData.data).not.toContain('Max');
    expect(dbData.data).toMatch(/^[A-Za-z0-9+/=]+$/); // Base64 encrypted
  });
});
```

### GDPR Compliance Tests
```typescript
describe('GDPR Compliance', () => {
  it('should export all personal data on request', async () => {
    // Add test data
    await addTestPersonalData();
    
    const exportData = await exportPersonalData('user-123');
    
    expect(exportData).toHaveProperty('personalDetails');
    expect(exportData).toHaveProperty('conversationNotes');
    expect(exportData).toHaveProperty('giftHistory');
    expect(exportData.format).toBe('encrypted');
  });
  
  it('should delete all data on request', async () => {
    await addTestPersonalData();
    
    await deleteAllPersonalData('user-123');
    
    const remaining = await db.personalDetails
      .where('userId').equals('user-123')
      .count();
    
    expect(remaining).toBe(0);
  });
});
```

---

## 🎭 E2E TEST SCENARIOS

### Scenario 1: Birthday Reminder Flow
```typescript
test('Complete birthday reminder workflow', async ({ page }) => {
  // Add birthday
  await page.goto('/customers/123/relationship');
  await page.click('button:has-text("Details bearbeiten")');
  
  await page.fill('input[name="birthday"]', '15.03.1980');
  await page.click('button:has-text("Speichern")');
  
  // Fast forward to 14 days before
  await page.evaluate(() => {
    window.mockDate('2025-03-01');
  });
  
  // Check dashboard
  await page.goto('/dashboard');
  
  const reminder = page.locator('text=Geburtstag in 14 Tagen');
  await expect(reminder).toBeVisible();
  
  // Set reminder
  await page.click('button[aria-label="Erinnerung setzen"]');
  await page.selectOption('select', '7-days-before');
  await page.click('button:has-text("Erstellen")');
  
  // Verify
  await expect(page.locator('text=Erinnerung gesetzt')).toBeVisible();
});
```

### Scenario 2: Relationship Score
```typescript
test('Relationship score updates with interactions', async ({ page }) => {
  await page.goto('/customers/123/relationship');
  
  // Initial score
  const score = await page.locator('[data-testid="relationship-score"]').textContent();
  expect(score).toBe('Schwach');
  
  // Add personal details
  await page.click('button:has-text("Details hinzufügen")');
  await page.fill('input[name="spouse"]', 'Maria');
  await page.fill('textarea[name="hobbies"]', 'Golf, Kochen');
  await page.click('button:has-text("Speichern")');
  
  // Log meeting
  await page.click('button:has-text("Treffen protokollieren")');
  await page.fill('textarea', 'Gutes Gespräch über neue Produkte');
  await page.click('button:has-text("Speichern")');
  
  // Score should improve
  await page.reload();
  const newScore = await page.locator('[data-testid="relationship-score"]').textContent();
  expect(newScore).toBe('Mittel');
});
```

---

## 🏃 PERFORMANCE TESTS

### IndexedDB Performance
```javascript
describe('IndexedDB Performance', () => {
  it('should handle 1000+ personal records efficiently', async () => {
    const startTime = performance.now();
    
    // Add 1000 records
    const records = Array.from({ length: 1000 }, (_, i) => ({
      contactId: `contact-${i}`,
      userId: 'test-user',
      hobbies: ['Hobby1', 'Hobby2'],
      familyStatus: 'married'
    }));
    
    await db.personalDetails.bulkAdd(records);
    
    const endTime = performance.now();
    expect(endTime - startTime).toBeLessThan(1000); // < 1 second
    
    // Query performance
    const queryStart = performance.now();
    const results = await db.personalDetails
      .where('userId')
      .equals('test-user')
      .limit(50)
      .toArray();
    
    const queryEnd = performance.now();
    expect(queryEnd - queryStart).toBeLessThan(50); // < 50ms
  });
});
```

---

## 🐛 EDGE CASES

### Test Edge Cases
```typescript
describe('Edge Cases', () => {
  it('should handle corrupt encrypted data', async () => {
    // Corrupt the data
    await db.personalDetails.add({
      contactId: '123',
      data: 'INVALID_ENCRYPTED_DATA'
    });
    
    const { details, error } = await loadPersonalDetails('123');
    
    expect(details).toBeNull();
    expect(error).toBe('Daten beschädigt');
  });
  
  it('should handle birthday on Feb 29', () => {
    const leapYearBirthday = new Date('2000-02-29');
    const reminder = calculateBirthdayReminder(leapYearBirthday, new Date('2025-02-15'));
    
    expect(reminder.date).toBe('28.02.2025');
    expect(reminder.note).toContain('Schaltjahr');
  });
  
  it('should limit notes length', async () => {
    const longNote = 'x'.repeat(10000);
    
    const result = await saveConversationNote({
      content: longNote,
      contactId: '123'
    });
    
    expect(result.content.length).toBe(5000); // Max length
    expect(result.truncated).toBe(true);
  });
});
```

---

## 📊 TEST DATA

### Mock Personal Details
```json
{
  "testContacts": [
    {
      "id": "test-1",
      "name": "Max Mustermann",
      "personalDetails": {
        "birthday": "1980-03-15",
        "familyStatus": "married",
        "spouse": { "name": "Maria", "birthday": "1982-07-20" },
        "children": [
          { "name": "Lisa", "age": 8 },
          { "name": "Tom", "age": 5 }
        ],
        "hobbies": ["Golf", "Kochen"],
        "favoriteFood": ["Italienisch", "Sushi"]
      }
    }
  ]
}
```