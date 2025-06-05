import { test, expect } from '@playwright/test';

test.describe('Debug onclick removal and event binding', () => {
  test('Check if onclick attributes are removed and events are bound', async ({ page }) => {
    // Enable debug logging and capture console
    const consoleLogs: string[] = [];
    page.on('console', msg => {
      const text = msg.text();
      consoleLogs.push(text);
      console.log('[Browser]', text);
    });
    
    await page.addInitScript(() => {
      localStorage.setItem('FP_DEBUG_EVENTS', 'true');
    });
    
    // Navigate with phase2 flag
    await page.goto('/?phase2=true');
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(2000);
    
    // Check onclick removal logs
    const onclickLogs = consoleLogs.filter(log => log.includes('Removing onclick'));
    console.log('\n=== onclick Removal Logs ===');
    onclickLogs.forEach(log => console.log(log));
    
    // Navigate to customer tab
    await page.click('[data-tab="customer"]');
    await page.waitForTimeout(500);
    
    // Check button attributes
    const buttonInfo = await page.evaluate(() => {
      const saveBtn = document.querySelector('.header-btn-save') as HTMLElement;
      const clearBtn = document.querySelector('.header-btn-clear') as HTMLElement;
      
      return {
        save: {
          exists: !!saveBtn,
          hasOnclick: saveBtn?.hasAttribute('onclick'),
          onclickValue: saveBtn?.getAttribute('onclick'),
          innerHTML: saveBtn?.innerHTML
        },
        clear: {
          exists: !!clearBtn,
          hasOnclick: clearBtn?.hasAttribute('onclick'),
          onclickValue: clearBtn?.getAttribute('onclick'),
          innerHTML: clearBtn?.innerHTML
        }
      };
    });
    
    console.log('\nButton Attributes:', JSON.stringify(buttonInfo, null, 2));
    
    // Try clicking save button and check for console errors
    let clickError = null;
    page.on('pageerror', error => {
      clickError = error.message;
    });
    
    await page.click('.header-btn-save');
    await page.waitForTimeout(500);
    
    if (clickError) {
      console.log('\nClick Error:', clickError);
    }
    
    // Check if any events were fired
    const eventsFired = await page.evaluate(() => {
      const events: string[] = [];
      
      // Set up listeners
      const captureEvent = (name: string) => {
        window.addEventListener(name, () => events.push(name));
      };
      
      captureEvent('customer:saved');
      captureEvent('customer:cleared');
      captureEvent('customer:creditCheckRequired');
      
      // Return a promise that resolves after a delay
      return new Promise<string[]>(resolve => {
        setTimeout(() => resolve(events), 100);
      });
    });
    
    console.log('\nEvents fired:', eventsFired);
    
    // Check validation requirements
    const validationInfo = await page.evaluate(() => {
      const requiredFields = [
        'companyName', 'legalForm', 'customerType', 'industry',
        'street', 'postalCode', 'city', 'contactName',
        'contactPhone', 'contactEmail', 'paymentMethod'
      ];
      
      const fieldStatus = requiredFields.map(id => {
        const element = document.getElementById(id);
        return {
          id,
          exists: !!element,
          type: element?.tagName,
          value: (element as any)?.value || ''
        };
      });
      
      return fieldStatus;
    });
    
    console.log('\nRequired Fields:', JSON.stringify(validationInfo, null, 2));
    
    // Assertions
    expect(buttonInfo.save.hasOnclick).toBeFalsy();
    expect(buttonInfo.clear.hasOnclick).toBeFalsy();
  });
});