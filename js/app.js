// =============================
// FreshPlan Main Application
// =============================

// Load translations first
document.write('<script src="js/translations.js"><\/script>');

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    try {
        initializeTabs();
        initializeDiscountCalculator();
        initializeCustomerForm();
        initializeLanguageSwitcher();
        initializeValidation();
        initializeAutosave();

        // Set default contract start date to today
        const contractStartEl = document.getElementById('contractStart');
        if (contractStartEl) {
            try {
                const today = new Date();
                const dateStr = today.toISOString().split('T')[0];
                contractStartEl.value = dateStr;
                updateContractEnd();
            } catch (e) {
                console.warn('Could not set default date:', e);
            }
        }

        // Load saved settings
        loadSettings();

        // Load default salesperson info
        loadDefaultSalesperson();

        // Update initial language
        updateLanguage();

        // Update progress
        updateProgress();

        // Mark demo as visited after viewing with error handling
        setTimeout(() => {
            try {
                FreshPlan.state.tabProgress.demonstrator = true;
                updateProgress();
            } catch (e) {
                console.warn('Progress update error:', e);
            }
        }, 2000);
    } catch (error) {
        console.error('Initialization error:', error);
        showMessage('Fehler beim Laden. Bitte Seite neu laden.', 'error');
    }
});

// Tab Navigation
function initializeTabs() {
    const tabs = document.querySelectorAll('.nav-tab');
    tabs.forEach(tab => {
        tab.addEventListener('click', function() {
            const tabName = this.getAttribute('data-tab');
            switchTab(tabName);
        });
    });
}

// Language Switching
function initializeLanguageSwitcher() {
    const langButtons = document.querySelectorAll('.lang-btn');
    langButtons.forEach(btn => {
        btn.addEventListener('click', function() {
            langButtons.forEach(b => b.classList.remove('active'));
            this.classList.add('active');
            FreshPlan.state.currentLang = this.getAttribute('data-lang');
            updateLanguage();
        });
    });
}

// Update language
function updateLanguage() {
    // Update all elements with data-lang-key
    document.querySelectorAll('[data-lang-key]').forEach(element => {
        const key = element.getAttribute('data-lang-key');
        const translation = getTranslation(key);
        if (translation) {
            element.textContent = translation;
        }
    });
    
    // Update placeholders and other attributes as needed
    updatePlaceholders();
    
    // Update scenarios if on demo tab
    if (document.getElementById('demonstrator').classList.contains('active')) {
        updateScenarios();
    }
}

// Update placeholders
function updatePlaceholders() {
    // Update specific placeholders based on language
    const placeholders = {
        'location-name-': FreshPlan.state.currentLang === 'de' ? 'z.B. Hotel Berlin' : 'e.g. Hotel Berlin'
    };
    
    // Apply placeholders
    Object.keys(placeholders).forEach(id => {
        const elements = document.querySelectorAll(`[id^="${id}"]`);
        elements.forEach(el => {
            if (el) el.placeholder = placeholders[id];
        });
    });
}

// Load autosaved data on page load
window.addEventListener('load', function() {
    setTimeout(() => {
        try {
            loadAutosavedData();
        } catch (e) {
            console.warn('Could not load autosaved data:', e);
        }
    }, 500);
});

// Expose necessary functions globally (for inline event handlers)
window.switchTab = switchTab;
window.toggleChainDetails = toggleChainDetails;
window.calculatePotential = calculatePotential;
window.generateOffer = generateOffer;
window.sendOffer = sendOffer;
window.printOffer = printOffer;
window.saveSettings = saveSettings;
window.testMondayConnection = testMondayConnection;
window.syncPriceLists = syncPriceLists;
window.closePdfModal = closePdfModal;
window.downloadPdf = downloadPdf;
window.closeCalcModal = closeCalcModal;
window.applyCalculation = applyCalculation;
window.handleDocumentUpload = handleDocumentUpload;
window.resetDocuments = resetDocuments;

// Log successful initialization
console.log('FreshPlan Sales Tool initialized successfully!');
console.log('Version: 1.0.0 (Refactored)');
console.log('Environment: Claude.ai compatible (in-memory storage)');