import { test, expect } from '@playwright/test';

test.describe('Debug CustomerModuleV2 Initialization', () => {
  test('Check if CustomerModuleV2 is loaded and initialized', async ({ page }) => {
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
    await page.waitForTimeout(3000); // Give more time for initialization
    
    // Check for CustomerModuleV2 logs
    const moduleV2Logs = consoleLogs.filter(log => 
      log.includes('CustomerModuleV2') || 
      log.includes('Customer form detected') ||
      log.includes('DOM Observer')
    );
    
    console.log('\n=== CustomerModuleV2 Related Logs ===');
    moduleV2Logs.forEach(log => console.log(log));
    
    // Navigate to customer tab
    await page.click('[data-tab="customer"]');
    await page.waitForTimeout(1000);
    
    // Check if module is registered in FreshPlan
    const moduleInfo = await page.evaluate(() => {
      const win = window as any;
      if (win.FreshPlan) {
        const customerModule = win.FreshPlan.getModule('customer');
        return {
          hasModule: !!customerModule,
          moduleName: customerModule?.constructor?.name || 'unknown',
          isV2: customerModule?.constructor?.name === 'CustomerModuleV2'
        };
      }
      return { hasModule: false, moduleName: 'N/A', isV2: false };
    });
    
    console.log('\nModule Info:', moduleInfo);
    
    // Check if buttons have event listeners
    const buttonInfo = await page.evaluate(() => {
      const saveBtn = document.querySelector('.header-btn-save');
      const clearBtn = document.querySelector('.header-btn-clear');
      
      // Try to get event listeners (this is limited but can give hints)
      const hasListeners = (element: Element | null) => {
        if (!element) return false;
        // Clone and click to see if default is prevented
        const clone = element.cloneNode(true) as HTMLElement;
        let prevented = false;
        clone.addEventListener('click', (e) => {
          if (e.defaultPrevented) prevented = true;
        });
        document.body.appendChild(clone);
        clone.click();
        document.body.removeChild(clone);
        return prevented;
      };
      
      return {
        saveExists: !!saveBtn,
        clearExists: !!clearBtn,
        saveHasListeners: hasListeners(saveBtn),
        clearHasListeners: hasListeners(clearBtn)
      };
    });
    
    console.log('\nButton Info:', buttonInfo);
    
    // Check form elements
    const formInfo = await page.evaluate(() => {
      const form = document.getElementById('customerForm');
      const companyName = document.getElementById('companyName');
      const customerStatus = document.getElementById('customerStatus');
      
      return {
        formExists: !!form,
        formVisible: form ? window.getComputedStyle(form).display !== 'none' : false,
        companyNameExists: !!companyName,
        customerStatusExists: !!customerStatus
      };
    });
    
    console.log('\nForm Info:', formInfo);
    
    // Take screenshot
    await page.screenshot({ path: 'debug-module-init.png', fullPage: true });
    
    // Assertions
    expect(consoleLogs.some(log => log.includes('Legacy script disabled'))).toBeTruthy();
    expect(formInfo.formExists).toBeTruthy();
    expect(formInfo.formVisible).toBeTruthy();
  });
});