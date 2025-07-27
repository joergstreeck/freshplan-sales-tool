/**
 * FC-005 Playwright Global Teardown
 * 
 * ZWECK: Cleanup nach E2E Tests (Test-Daten löschen, Logs sammeln)
 * PHILOSOPHIE: Saubere Test-Umgebung hinterlassen
 */

import { FullConfig } from '@playwright/test';
import fs from 'fs';
import path from 'path';

async function globalTeardown(config: FullConfig) {
  console.log('🧹 FC-005 E2E Tests - Global Teardown startet...');
  
  const baseURL = config.projects[0].use.baseURL || 'http://localhost:5173';
  
  // Cleanup test data
  try {
    await cleanupTestData(baseURL);
    console.log('✅ Test-Daten bereinigt');
  } catch (error) {
    console.warn('⚠️ Test-Daten Cleanup fehlgeschlagen:', error);
  }
  
  // Clean up auth state file
  try {
    const authStatePath = './e2e-auth-state.json';
    if (fs.existsSync(authStatePath)) {
      fs.unlinkSync(authStatePath);
      console.log('✅ Auth State bereinigt');
    }
  } catch (error) {
    console.warn('⚠️ Auth State Cleanup fehlgeschlagen:', error);
  }
  
  // Generate test summary
  try {
    await generateTestSummary();
    console.log('✅ Test Summary generiert');
  } catch (error) {
    console.warn('⚠️ Test Summary Generation fehlgeschlagen:', error);
  }
  
  console.log('✅ FC-005 E2E Tests - Global Teardown abgeschlossen');
}

async function cleanupTestData(baseURL: string) {
  // Delete test customers created during E2E tests
  const testCustomerNames = [
    'E2E Test Hotel GmbH',
    'E2E Chain Restaurant AG',
    'E2E Draft Test',
    'E2E Happy Path Hotel',
    'E2E Chain Customer Test'
  ];
  
  for (const companyName of testCustomerNames) {
    try {
      // In a real implementation, we would have an API to search and delete
      // For now, we just make a cleanup request
      await fetch(`${baseURL}/api/customers/cleanup`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ 
          action: 'delete-test-customers',
          filter: { companyName: companyName }
        })
      });
    } catch (error) {
      // Ignore individual cleanup failures
    }
  }
  
  // Clean up any draft data
  try {
    await fetch(`${baseURL}/api/customers/drafts/cleanup`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ 
        action: 'delete-e2e-drafts'
      })
    });
  } catch (error) {
    // Ignore draft cleanup failures
  }
}

async function generateTestSummary() {
  const resultsPath = '../../../../../test-results/results.json';
  
  if (!fs.existsSync(resultsPath)) {
    console.log('ℹ️ Keine Test-Ergebnisse gefunden für Summary');
    return;
  }
  
  try {
    const results = JSON.parse(fs.readFileSync(resultsPath, 'utf8'));
    
    const summary = {
      timestamp: new Date().toISOString(),
      total: results.stats?.total || 0,
      passed: results.stats?.passed || 0,
      failed: results.stats?.failed || 0,
      skipped: results.stats?.skipped || 0,
      duration: results.stats?.duration || 0,
      
      // Categorize by test type
      categories: {
        happyPath: 0,
        chainCustomer: 0,
        draftRecovery: 0,
        validation: 0,
        errorHandling: 0
      },
      
      // Browser-specific results
      browsers: {} as Record<string, any>
    };
    
    // Analyze test results
    if (results.suites) {
      results.suites.forEach((suite: any) => {
        if (suite.specs) {
          suite.specs.forEach((spec: any) => {
            const title = spec.title?.toLowerCase() || '';
            
            if (title.includes('happy path')) summary.categories.happyPath++;
            if (title.includes('chain customer')) summary.categories.chainCustomer++;
            if (title.includes('draft recovery')) summary.categories.draftRecovery++;
            if (title.includes('validation')) summary.categories.validation++;
            if (title.includes('error')) summary.categories.errorHandling++;
            
            // Track browser results
            if (spec.tests) {
              spec.tests.forEach((test: any) => {
                const project = test.projectName || 'unknown';
                if (!summary.browsers[project]) {
                  summary.browsers[project] = { passed: 0, failed: 0 };
                }
                
                if (test.status === 'passed') {
                  summary.browsers[project].passed++;
                } else {
                  summary.browsers[project].failed++;
                }
              });
            }
          });
        }
      });
    }
    
    // Write summary
    const summaryPath = '../../../../../test-results/e2e-summary.json';
    fs.writeFileSync(summaryPath, JSON.stringify(summary, null, 2));
    
    // Write human-readable summary
    const readableSummary = `
# FC-005 E2E Test Summary

**Datum:** ${new Date().toLocaleString('de-DE')}

## Gesamt-Ergebnisse
- **Gesamt Tests:** ${summary.total}
- **Erfolgreich:** ${summary.passed} ✅
- **Fehlgeschlagen:** ${summary.failed} ❌
- **Übersprungen:** ${summary.skipped} ⏭️
- **Dauer:** ${Math.round(summary.duration / 1000)}s

## Test-Kategorien
- **Happy Path:** ${summary.categories.happyPath} Tests
- **Chain Customer:** ${summary.categories.chainCustomer} Tests  
- **Draft Recovery:** ${summary.categories.draftRecovery} Tests
- **Validation:** ${summary.categories.validation} Tests
- **Error Handling:** ${summary.categories.errorHandling} Tests

## Browser-Kompatibilität
${Object.entries(summary.browsers).map(([browser, stats]: [string, any]) => 
  `- **${browser}:** ${stats.passed} ✅ / ${stats.failed} ❌`
).join('\n')}

## Status
${summary.failed === 0 ? '🎉 **ALLE TESTS ERFOLGREICH**' : `⚠️ **${summary.failed} TESTS FEHLGESCHLAGEN**`}
`;
    
    fs.writeFileSync('../../../../../test-results/e2e-summary.md', readableSummary);
    
  } catch (error) {
    console.warn('Fehler bei Test Summary Generation:', error);
  }
}

export default globalTeardown;