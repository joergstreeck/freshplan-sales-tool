/**
 * Quick fix for tab navigation
 */

console.log('🔧 Applying tab navigation fix...');

// Wait for DOM
document.addEventListener('DOMContentLoaded', () => {
  console.log('DOM loaded, fixing tabs...');
  
  // Get all tab buttons
  const tabButtons = document.querySelectorAll('.nav-tab');
  const tabContents = document.querySelectorAll('.tab-content');
  
  console.log(`Found ${tabButtons.length} tab buttons and ${tabContents.length} tab contents`);
  
  // Function to switch tabs
  function switchToTab(tabName) {
    console.log(`Switching to tab: ${tabName}`);
    
    // Hide all tab contents
    tabContents.forEach(content => {
      content.classList.remove('active');
      content.setAttribute('aria-hidden', 'true');
    });
    
    // Remove active from all buttons
    tabButtons.forEach(btn => {
      btn.classList.remove('active');
      btn.setAttribute('aria-selected', 'false');
    });
    
    // Show selected tab
    const selectedContent = document.getElementById(tabName);
    const selectedButton = document.querySelector(`[data-tab="${tabName}"]`);
    
    if (selectedContent && selectedButton) {
      selectedContent.classList.add('active');
      selectedContent.setAttribute('aria-hidden', 'false');
      selectedButton.classList.add('active');
      selectedButton.setAttribute('aria-selected', 'true');
      
      // Update URL
      const url = new URL(window.location);
      url.searchParams.set('tab', tabName);
      window.history.pushState({ tab: tabName }, '', url);
      
      console.log(`✅ Switched to ${tabName}`);
    } else {
      console.error(`❌ Could not find tab: ${tabName}`);
    }
  }
  
  // Add click handlers
  tabButtons.forEach(button => {
    button.addEventListener('click', (e) => {
      e.preventDefault();
      const tabName = button.getAttribute('data-tab');
      switchToTab(tabName);
    });
  });
  
  // Check URL for initial tab
  const urlParams = new URLSearchParams(window.location.search);
  const initialTab = urlParams.get('tab') || 'demonstrator';
  if (initialTab !== 'demonstrator') {
    switchToTab(initialTab);
  }
  
  // Make function available globally
  window.switchToTab = switchToTab;
  
  console.log('✅ Tab navigation fix applied');
});

// Also fix calculator buttons
window.addEventListener('load', () => {
  console.log('🔧 Fixing calculator functionality...');
  
  // Fix sliders
  const orderValueSlider = document.getElementById('orderValue');
  const leadTimeSlider = document.getElementById('leadTime');
  const pickupToggle = document.getElementById('pickupToggle');
  const chainToggle = document.getElementById('chainToggle');
  
  if (orderValueSlider) {
    // Update display function
    function updateCalculator() {
      const orderValue = parseInt(orderValueSlider.value);
      const leadTime = parseInt(leadTimeSlider.value);
      const pickup = pickupToggle.checked;
      const chain = chainToggle.checked;
      
      // Update displays
      document.getElementById('orderValueDisplay').textContent = `€${orderValue.toLocaleString('de-DE')}`;
      document.getElementById('leadTimeDisplay').textContent = `${leadTime} Tage`;
      
      // Call global calculator if available
      if (typeof updateDiscountCalculation === 'function') {
        updateDiscountCalculation();
      } else {
        console.warn('updateDiscountCalculation not available yet');
      }
    }
    
    // Add event listeners
    orderValueSlider.addEventListener('input', updateCalculator);
    leadTimeSlider.addEventListener('input', updateCalculator);
    pickupToggle.addEventListener('change', updateCalculator);
    chainToggle.addEventListener('change', updateCalculator);
    
    console.log('✅ Calculator event listeners added');
  }
});