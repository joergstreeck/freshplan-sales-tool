/**
 * Main entry point for FreshPlan Sales Tool
 */

import app from './FreshPlanApp';

// Initialize application when DOM is ready
app.init().catch(error => {
  console.error('Failed to initialize FreshPlan app:', error);
});

// Export for use in other scripts
export default app;