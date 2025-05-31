/**
 * Fix Navigation Issues
 */

// 1. Fix TypeScript errors
const fixTypeScriptErrors = () => {
  console.log('Fixing TypeScript errors...');
  
  // The main issue is in FreshPlanApp.ts - already fixed
  // Check for other issues
};

// 2. Check module initialization
const checkModuleInit = async () => {
  console.log('Checking module initialization...');
  
  try {
    // Import the app
    const { default: app } = await import('./src/FreshPlanApp');
    
    // Check if initialized
    console.log('App initialized:', app.isInitialized());
    
    // Check TabNavigationModule
    const tabModule = app.getModule('tabs');
    console.log('TabNavigationModule exists:', !!tabModule);
    
    if (tabModule) {
      console.log('Current tab:', tabModule.getCurrentTab());
      console.log('Tab module initialized:', tabModule.isInitialized());
    }
    
  } catch (error) {
    console.error('Error checking modules:', error);
  }
};

// 3. Test event binding
const testEventBinding = () => {
  console.log('Testing event binding...');
  
  // Wait for DOM
  document.addEventListener('DOMContentLoaded', () => {
    const navButtons = document.querySelectorAll('.nav-tab');
    console.log('Found nav buttons:', navButtons.length);
    
    navButtons.forEach((button, index) => {
      console.log(`Button ${index}:`, {
        text: button.textContent?.trim(),
        dataTab: (button as HTMLElement).dataset.tab,
        hasClickHandler: !!(button as any).onclick || button.hasAttribute('onclick')
      });
      
      // Test click
      button.addEventListener('click', (e) => {
        console.log('Test click handler fired for:', (e.currentTarget as HTMLElement).dataset.tab);
      });
    });
  });
};

// 4. Check CSS loading
const checkCSSLoading = () => {
  console.log('Checking CSS loading...');
  
  const styleSheets = Array.from(document.styleSheets);
  console.log('Loaded stylesheets:', styleSheets.length);
  
  styleSheets.forEach((sheet, index) => {
    try {
      console.log(`Sheet ${index}:`, {
        href: sheet.href,
        rules: sheet.cssRules?.length || 0
      });
    } catch (e) {
      console.log(`Sheet ${index}: Cannot access (CORS or loading error)`);
    }
  });
};

// Run all checks
export const runDiagnostics = async () => {
  console.log('=== Running Navigation Diagnostics ===');
  
  fixTypeScriptErrors();
  await checkModuleInit();
  testEventBinding();
  checkCSSLoading();
  
  console.log('=== Diagnostics Complete ===');
};

// Auto-run if loaded directly
if (import.meta.url === new URL(window.location.href).href) {
  runDiagnostics();
}