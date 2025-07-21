# FC-010: Customer Import - Testing Strategy

**Fokus:** Test-Coverage, Edge Cases und Performance Tests

## 🧪 Unit Tests

### Backend Validation Tests

```java
@QuarkusTest
class ValidationServiceTest {
    
    @Inject
    ValidationService validationService;
    
    @Test
    void testEmailValidation() {
        // Valid emails
        assertThat(validationService.validateEmail("test@example.com"))
            .isTrue();
        assertThat(validationService.validateEmail("user.name+tag@company.co.uk"))
            .isTrue();
            
        // Invalid emails
        assertThat(validationService.validateEmail("invalid.email"))
            .isFalse();
        assertThat(validationService.validateEmail("@example.com"))
            .isFalse();
    }
    
    @Test
    void testPhoneNumberNormalization() {
        // Different formats should normalize to same
        String normalized1 = validationService.normalizePhone("+49 89 123456");
        String normalized2 = validationService.normalizePhone("089-123456");
        String normalized3 = validationService.normalizePhone("089/123456");
        
        assertThat(normalized1).isEqualTo(normalized2);
        assertThat(normalized2).isEqualTo(normalized3);
    }
    
    @Test
    void testDuplicateDetection() {
        Customer existing = createTestCustomer("Test GmbH", "test@example.com");
        
        // Exact match
        assertThat(validationService.isDuplicate(
            "Test GmbH", "test@example.com", null
        )).isTrue();
        
        // Similar name
        assertThat(validationService.isDuplicate(
            "Test GmbH & Co", "test@example.com", null
        )).isTrue();
        
        // Different email, same name
        assertThat(validationService.isDuplicate(
            "Test GmbH", "other@example.com", null
        )).isFalse();
    }
}
```

### Frontend Component Tests

```typescript
// FileUpload.test.tsx
describe('FileUpload Component', () => {
  it('should accept CSV files', async () => {
    const onFileSelect = jest.fn();
    const { getByText } = render(
      <FileUpload onFileSelect={onFileSelect} />
    );
    
    const file = new File(['test,data'], 'test.csv', {
      type: 'text/csv'
    });
    
    fireEvent.drop(getByText(/Datei hier ablegen/), {
      dataTransfer: { files: [file] }
    });
    
    await waitFor(() => {
      expect(getByText('test.csv')).toBeInTheDocument();
    });
  });
  
  it('should reject unsupported file types', async () => {
    const { getByText } = render(<FileUpload onFileSelect={jest.fn()} />);
    
    const file = new File(['test'], 'test.pdf', {
      type: 'application/pdf'
    });
    
    fireEvent.drop(getByText(/Datei hier ablegen/), {
      dataTransfer: { files: [file] }
    });
    
    await waitFor(() => {
      expect(getByText(/Unsupported file type/)).toBeInTheDocument();
    });
  });
  
  it('should show file preview', async () => {
    const csvContent = 'name,email,phone\nTest GmbH,test@example.com,089123456';
    const file = new File([csvContent], 'customers.csv', {
      type: 'text/csv'
    });
    
    const { getByText, getByTestId } = render(
      <FileUpload onFileSelect={jest.fn()} />
    );
    
    fireEvent.drop(getByText(/Datei hier ablegen/), {
      dataTransfer: { files: [file] }
    });
    
    await waitFor(() => {
      expect(getByText('Test GmbH')).toBeInTheDocument();
      expect(getByText('test@example.com')).toBeInTheDocument();
    });
  });
});
```

## 🔄 Integration Tests

### Import Flow E2E Test

```typescript
// import-flow.e2e.test.ts
describe('Customer Import E2E', () => {
  beforeEach(() => {
    cy.login('admin', 'password');
    cy.visit('/import');
  });
  
  it('should complete full import flow', () => {
    // Step 1: Upload file
    cy.get('[data-testid="file-dropzone"]').attachFile('valid-customers.csv');
    cy.contains('valid-customers.csv').should('be.visible');
    cy.contains('100 Zeilen erkannt').should('be.visible');
    cy.get('[data-testid="next-button"]').click();
    
    // Step 2: Field mapping
    cy.contains('Felder zuordnen').should('be.visible');
    cy.contains('5 Felder automatisch zugeordnet').should('be.visible');
    
    // Manual mapping for unmapped field
    cy.get('[data-testid="field-mapping-company"]')
      .select('Spalte A - Firmenname');
    
    cy.get('[data-testid="next-button"]').click();
    
    // Step 3: Validation
    cy.contains('Validierung läuft...').should('be.visible');
    cy.contains('95 gültige Zeilen', { timeout: 10000 }).should('be.visible');
    cy.contains('3 Warnungen').should('be.visible');
    cy.contains('2 Fehler').should('be.visible');
    
    // Fix errors
    cy.get('[data-testid="fix-errors-button"]').click();
    cy.get('[data-testid="error-row-45"]').within(() => {
      cy.get('input[name="email"]').clear().type('fixed@example.com');
    });
    
    cy.get('[data-testid="revalidate-button"]').click();
    cy.contains('97 gültige Zeilen').should('be.visible');
    
    cy.get('[data-testid="start-import-button"]').click();
    
    // Step 4: Import progress
    cy.contains('Import läuft...').should('be.visible');
    cy.get('[data-testid="progress-bar"]').should('be.visible');
    
    // Wait for completion
    cy.contains('Import erfolgreich abgeschlossen!', { 
      timeout: 30000 
    }).should('be.visible');
    
    cy.contains('97 Kunden importiert').should('be.visible');
    cy.get('[data-testid="view-customers-button"]').click();
    
    // Verify in customer list
    cy.url().should('include', '/customers');
    cy.contains('97 neue Kunden').should('be.visible');
  });
});
```

## 🚀 Performance Tests

### Large File Import Test

```java
@Test
@Timeout(value = 5, unit = TimeUnit.MINUTES)
void testLargeFileImport() {
    // Generate test file with 10,000 rows
    File largeFile = generateTestFile(10000);
    
    ImportConfiguration config = createDefaultConfig();
    
    long startTime = System.currentTimeMillis();
    
    ImportResult result = importService.importFile(
        largeFile,
        config.getId()
    );
    
    long duration = System.currentTimeMillis() - startTime;
    
    // Assertions
    assertThat(result.getSuccessCount()).isGreaterThan(9900);
    assertThat(duration).isLessThan(60000); // < 1 minute
    
    // Performance metrics
    double rowsPerSecond = 10000.0 / (duration / 1000.0);
    assertThat(rowsPerSecond).isGreaterThan(150); // > 150 rows/sec
}

@Test
void testMemoryUsage() {
    Runtime runtime = Runtime.getRuntime();
    long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
    
    // Import large file
    File largeFile = generateTestFile(50000);
    importService.importFile(largeFile, configId);
    
    long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
    long memoryUsed = memoryAfter - memoryBefore;
    
    // Should not use more than 200MB for 50k rows
    assertThat(memoryUsed).isLessThan(200 * 1024 * 1024);
}
```

## 🔍 Edge Case Tests

### Special Characters & Encoding

```typescript
describe('Special Characters Handling', () => {
  const testCases = [
    { name: 'Umlauts', data: 'Müller GmbH,ö.ä.ü@example.com' },
    { name: 'Special chars', data: 'Test & Co. KG,info@test-co.de' },
    { name: 'Unicode', data: '株式会社テスト,test@例え.jp' },
    { name: 'Quotes', data: '"Test, Inc.",test@example.com' }
  ];
  
  testCases.forEach(({ name, data }) => {
    it(`should handle ${name} correctly`, async () => {
      const file = new File([data], 'test.csv');
      const result = await parseCSV(file);
      
      expect(result.errors).toHaveLength(0);
      expect(result.rows[0]).toBeDefined();
    });
  });
});
```

### Error Recovery Tests

```java
@Test
void testPartialImportRecovery() {
    // Simulate failure after 50 rows
    ImportJob job = startImportWithFailureAt(50);
    
    assertThat(job.getStatus()).isEqualTo("FAILED");
    assertThat(job.getProcessedRows()).isEqualTo(50);
    assertThat(job.getSuccessfulRows()).isEqualTo(49);
    
    // Resume import
    ImportJob resumedJob = importService.resumeImport(job.getId());
    
    assertThat(resumedJob.getStatus()).isEqualTo("COMPLETED");
    assertThat(resumedJob.getProcessedRows()).isEqualTo(100);
    assertThat(resumedJob.getSuccessfulRows()).isEqualTo(99);
}
```

## 📊 Test Coverage Report

```bash
# Backend Coverage
./mvnw test jacoco:report

# Expected coverage:
- Service Layer: > 90%
- Validation Logic: > 95%
- Import Processor: > 85%
- Error Handling: > 80%

# Frontend Coverage
npm run test:coverage

# Expected coverage:
- Components: > 85%
- Hooks: > 90%
- Utils: > 95%
- Pages: > 80%
```

## 🔧 Test Data Generators

```typescript
// test-utils/generators.ts
export const generateTestCSV = (rows: number): string => {
  const headers = ['name', 'email', 'phone', 'address', 'city'];
  const data = [headers.join(',')];
  
  for (let i = 0; i < rows; i++) {
    data.push([
      faker.company.name(),
      faker.internet.email(),
      faker.phone.number(),
      faker.address.streetAddress(),
      faker.address.city()
    ].join(','));
  }
  
  return data.join('\n');
};

export const generateInvalidData = (): string[] => {
  return [
    'Missing,Fields',
    'Invalid Email,not-an-email,123456',
    'Duplicate,duplicate@example.com,123456',
    'Special Chars,test@example.com,+++49(0)89/123-456',
    '"Quoted, Company",test@example.com,123456'
  ];
};
```