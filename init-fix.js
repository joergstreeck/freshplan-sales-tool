/**
 * Initialization Fix
 * Ensures all modules are properly loaded and initialized
 */

console.log('🔧 Initialization fix loading...');

// Wait for DOM
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', initApp);
} else {
  initApp();
}

async function initApp() {
  console.log('🚀 Starting app initialization...');
  
  try {
    // Import all modules
    const [
      { default: state },
      { initializeTabs },
      { initializeCalculator },
      { initializeCustomer },
      { initializeSettings },
      { initializeProfile },
      { initializePDF, closePdfModal, downloadPDF, sendOffer },
      { initializeLanguage, updateLanguage },
      { initializeValidation },
      { initializeAutosave }
    ] = await Promise.all([
      import('./js/state.js'),
      import('./js/modules/tabs.js'),
      import('./js/modules/calculator.js'),
      import('./js/modules/customer.js'),
      import('./js/modules/settings.js'),
      import('./js/modules/profile.js'),
      import('./js/modules/pdf.js'),
      import('./js/utils/i18n.js'),
      import('./js/utils/validation.js'),
      import('./js/utils/storage.js')
    ]);
    
    console.log('✅ All modules imported');
    
    // Initialize state
    state.load();
    console.log('✅ State loaded');
    
    // Initialize all modules
    initializeTabs();
    console.log('✅ Tabs initialized');
    
    initializeCalculator();
    console.log('✅ Calculator initialized');
    
    initializeCustomer();
    console.log('✅ Customer initialized');
    
    initializeSettings();
    console.log('✅ Settings initialized');
    
    initializeProfile();
    console.log('✅ Profile initialized');
    
    initializePDF();
    console.log('✅ PDF initialized');
    
    initializeLanguage();
    console.log('✅ Language initialized');
    
    initializeValidation();
    console.log('✅ Validation initialized');
    
    initializeAutosave();
    console.log('✅ Autosave initialized');
    
    // Update language
    updateLanguage();
    
    // Fix modal button listeners
    const closeModalBtn = document.getElementById('closePdfModalBtn');
    if (closeModalBtn) {
      closeModalBtn.addEventListener('click', closePdfModal);
      console.log('✅ Modal close button connected');
    }
    
    // Ensure PDF modal functions are available globally
    window.FreshPlan = window.FreshPlan || {};
    window.FreshPlan.closePdfModal = closePdfModal;
    window.FreshPlan.downloadPdf = downloadPDF;
    window.FreshPlan.sendOffer = sendOffer;
    
    console.log('✅ App initialization complete!');
    console.log('🎉 FreshPlan Sales Tool is ready!');
    
  } catch (error) {
    console.error('❌ Initialization error:', error);
    console.error(error.stack);
  }
}

// Also log any unhandled errors
window.addEventListener('error', (event) => {
  console.error('❌ Runtime error:', event.error);
});

window.addEventListener('unhandledrejection', (event) => {
  console.error('❌ Unhandled promise rejection:', event.reason);
});