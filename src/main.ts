/**
 * Main entry point for FreshPlan Sales Tool
 * Phase 1b - With JavaScript functionality
 * Phase 2 - With modular architecture
 */

// Import CSS
import './styles/original-imported-styles.css';

// Import legacy JavaScript
import { initLegacyScript } from './legacy-script';

// Import FreshPlan App for Phase 2
import FreshPlanApp from './FreshPlanApp';

// Check if we're in Phase 2 mode
const urlParams = new URLSearchParams(window.location.search);
const isPhase2 = urlParams.get('phase2') === 'true';

// Wait for DOM to be ready
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', async () => {
    if (isPhase2) {
      console.log('Phase 2: Initializing modular architecture...');
      // Initialize FreshPlan App first
      await FreshPlanApp.init();
    }
    
    console.log('Phase 1b: Initializing JavaScript functionality...');
    initLegacyScript();
  });
} else {
  // DOM already loaded
  (async () => {
    if (isPhase2) {
      console.log('Phase 2: Initializing modular architecture...');
      // Initialize FreshPlan App first
      await FreshPlanApp.init();
    }
    
    console.log('Phase 1b: Initializing JavaScript functionality...');
    initLegacyScript();
  })();
}