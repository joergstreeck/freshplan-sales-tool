/**
 * Main entry point for FreshPlan Sales Tool
 * Phase 1b - With JavaScript functionality
 */

// Import CSS
import './styles/original-imported-styles.css';

// Import legacy JavaScript
import { initLegacyScript } from './legacy-script';

// Wait for DOM to be ready
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', () => {
    console.log('Phase 1b: Initializing JavaScript functionality...');
    initLegacyScript();
  });
} else {
  // DOM already loaded
  console.log('Phase 1b: Initializing JavaScript functionality...');
  initLegacyScript();
}