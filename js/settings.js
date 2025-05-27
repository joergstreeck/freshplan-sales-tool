// =============================
// FreshPlan Settings Management
// =============================

// Load settings
function loadSettings() {
    const settings = FreshPlan.storage.getItem(FreshPlan.config.storageKeys.settings);
    if (settings) {
        const data = JSON.parse(settings);
        
        // Apply settings to fields
        if (data.defaultSalesperson) {
            Object.keys(data.defaultSalesperson).forEach(key => {
                const field = document.getElementById(key);
                if (field) field.value = data.defaultSalesperson[key];
            });
        }
        
        if (data.monday) {
            if (data.monday.token) document.getElementById('mondayToken').value = data.monday.token;
            if (data.monday.boardId) document.getElementById('mondayBoardId').value = data.monday.boardId;
        }
        
        if (data.email) {
            if (data.email.server) document.getElementById('smtpServer').value = data.email.server;
            if (data.email.email) document.getElementById('smtpEmail').value = data.email.email;
            if (data.email.password) document.getElementById('smtpPassword').value = data.email.password;
        }
        
        if (data.xentral) {
            if (data.xentral.url) document.getElementById('xentralUrl').value = data.xentral.url;
            if (data.xentral.key) document.getElementById('xentralKey').value = data.xentral.key;
        }
    }
}

// Load default salesperson
function loadDefaultSalesperson() {
    const settings = FreshPlan.storage.getItem(FreshPlan.config.storageKeys.settings);
    if (settings) {
        const data = JSON.parse(settings);
        if (data.defaultSalesperson) {
            const nameField = document.getElementById('salespersonName');
            const emailField = document.getElementById('salespersonEmail');
            const phoneField = document.getElementById('salespersonPhone');
            const mobileField = document.getElementById('salespersonMobile');
            
            if (nameField && !nameField.value && data.defaultSalesperson.defaultSalespersonName) {
                nameField.value = data.defaultSalesperson.defaultSalespersonName;
            }
            if (emailField && !emailField.value && data.defaultSalesperson.defaultSalespersonEmail) {
                emailField.value = data.defaultSalesperson.defaultSalespersonEmail;
            }
            if (phoneField && !phoneField.value && data.defaultSalesperson.defaultSalespersonPhone) {
                phoneField.value = data.defaultSalesperson.defaultSalespersonPhone;
            }
            if (mobileField && !mobileField.value && data.defaultSalesperson.defaultSalespersonMobile) {
                mobileField.value = data.defaultSalesperson.defaultSalespersonMobile;
            }
        }
    }
}

// Save settings
function saveSettings() {
    const settings = {
        defaultSalesperson: {
            defaultSalespersonName: document.getElementById('defaultSalespersonName').value,
            defaultSalespersonEmail: document.getElementById('defaultSalespersonEmail').value,
            defaultSalespersonPhone: document.getElementById('defaultSalespersonPhone').value,
            defaultSalespersonMobile: document.getElementById('defaultSalespersonMobile').value
        },
        monday: {
            token: document.getElementById('mondayToken').value,
            boardId: document.getElementById('mondayBoardId').value
        },
        email: {
            server: document.getElementById('smtpServer').value,
            email: document.getElementById('smtpEmail').value,
            password: document.getElementById('smtpPassword').value
        },
        xentral: {
            url: document.getElementById('xentralUrl').value,
            key: document.getElementById('xentralKey').value
        }
    };
    
    FreshPlan.storage.setItem(FreshPlan.config.storageKeys.settings, JSON.stringify(settings));
    showMessage(getTranslation('messages.settingsSaved'), 'success');
}

// Test Monday.com connection
function testMondayConnection() {
    const token = document.getElementById('mondayToken').value;
    
    if (!token) {
        showMessage(getTranslation('messages.missingAPIToken'), 'error');
        return;
    }
    
    // Simulate API test
    showMessage('Teste Verbindung...', 'success');
    
    setTimeout(() => {
        // In a real implementation, this would make an actual API call
        showMessage(getTranslation('messages.connectionSuccess'), 'success');
    }, 1000);
}

// Sync price lists
function syncPriceLists() {
    const url = document.getElementById('xentralUrl').value;
    const key = document.getElementById('xentralKey').value;
    
    if (!url || !key) {
        showMessage(getTranslation('messages.missingXentralData'), 'error');
        return;
    }
    
    showMessage(getTranslation('messages.syncInProgress'), 'success');
    
    setTimeout(() => {
        // In a real implementation, this would sync with Xentral
        showMessage(getTranslation('messages.syncSuccess'), 'success');
    }, 2000);
}

// Export functions
window.loadSettings = loadSettings;
window.loadDefaultSalesperson = loadDefaultSalesperson;
window.saveSettings = saveSettings;
window.testMondayConnection = testMondayConnection;
window.syncPriceLists = syncPriceLists;