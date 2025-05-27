// =============================
// FreshPlan Utility Functions
// =============================

// Debounce utility
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// Format currency
function formatCurrency(amount) {
    return new Intl.NumberFormat('de-DE', {
        style: 'currency',
        currency: 'EUR',
        minimumFractionDigits: 0,
        maximumFractionDigits: 0
    }).format(amount);
}

// Show message
function showMessage(message, type) {
    const messageEl = document.getElementById(type + 'Message');
    if (messageEl) {
        messageEl.textContent = message;
        messageEl.classList.add('show');
        
        setTimeout(() => {
            messageEl.classList.remove('show');
        }, FreshPlan.config.messageDuration);
    }
}

// Get translation
function getTranslation(key) {
    const keys = key.split('.');
    let value = translations[FreshPlan.state.currentLang];
    
    for (const k of keys) {
        if (value && value[k]) {
            value = value[k];
        } else {
            return key; // Return key if translation not found
        }
    }
    
    return value;
}

// Switch tab
function switchTab(tabName) {
    // Update active tab
    document.querySelectorAll('.nav-tab').forEach(tab => {
        tab.classList.remove('active');
    });
    document.querySelector(`[data-tab="${tabName}"]`).classList.add('active');

    // Update content
    document.querySelectorAll('.tab-content').forEach(content => {
        content.classList.remove('active');
    });
    document.getElementById(tabName).classList.add('active');

    // Update progress
    if (tabName === 'profile') {
        generateCustomerProfile();
    }

    // Mark tab as visited
    FreshPlan.state.tabProgress[tabName] = true;
    updateProgress();
}

// Update progress
function updateProgress() {
    const totalTabs = Object.keys(FreshPlan.state.tabProgress).length;
    const completedTabs = Object.values(FreshPlan.state.tabProgress).filter(Boolean).length;
    const progressPercent = (completedTabs / totalTabs) * 100;

    const progressBar = document.getElementById('progressBar');
    if (progressBar) {
        progressBar.style.width = progressPercent + '%';
    }

    // Update tab indicators
    Object.keys(FreshPlan.state.tabProgress).forEach(tab => {
        const tabElement = document.querySelector(`[data-tab="${tab}"]`);
        if (tabElement) {
            if (tab === 'customer' && FreshPlan.state.customerData.companyName) {
                tabElement.classList.add('completed');
            } else if (tab === 'offer' && FreshPlan.state.generatedPdf) {
                tabElement.classList.add('completed');
            }
        }
    });
}

// Validate email
function validateEmail(field) {
    const email = field.value;
    const errorEl = document.getElementById(field.id + 'Error');
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    if (!email) {
        showFieldError(field, errorEl, getTranslation('messages.requiredField'));
        return false;
    } else if (!emailRegex.test(email)) {
        showFieldError(field, errorEl, getTranslation('messages.invalidEmail'));
        return false;
    } else {
        clearFieldError(field, errorEl);
        return true;
    }
}

// Validate phone
function validatePhone(field) {
    const phone = field.value;
    const errorEl = document.getElementById(field.id + 'Error');
    const phoneRegex = /^[\d\s\-\+\(\)]+$/;

    if (!phone) {
        showFieldError(field, errorEl, getTranslation('messages.requiredField'));
        return false;
    } else if (!phoneRegex.test(phone) || phone.length < 6) {
        showFieldError(field, errorEl, getTranslation('messages.invalidPhone'));
        return false;
    } else {
        clearFieldError(field, errorEl);
        return true;
    }
}

// Validate ZIP
function validateZip(field) {
    const zip = field.value;
    const errorEl = document.getElementById(field.id + 'Error');

    if (!zip) {
        showFieldError(field, errorEl, getTranslation('messages.requiredField'));
        return false;
    } else if (!/^\d{5}$/.test(zip)) {
        showFieldError(field, errorEl, getTranslation('messages.invalidZip'));
        return false;
    } else {
        clearFieldError(field, errorEl);
        return true;
    }
}

// Validate required field
function validateRequired(field) {
    const errorEl = document.getElementById(field.id + 'Error');
    
    if (!field.value.trim()) {
        showFieldError(field, errorEl, getTranslation('messages.requiredField'));
        return false;
    } else {
        clearFieldError(field, errorEl);
        return true;
    }
}

// Show field error
function showFieldError(field, errorEl, message) {
    field.classList.add('error');
    if (errorEl) {
        errorEl.textContent = message;
        errorEl.classList.add('show');
    }
}

// Clear field error
function clearFieldError(field, errorEl) {
    field.classList.remove('error');
    if (errorEl) {
        errorEl.classList.remove('show');
    }
}

// Get industry name
function getIndustryName(industry) {
    return FreshPlan.config.industries[industry]?.name || industry;
}

// Update contract end date
function updateContractEnd() {
    const startDate = document.getElementById('contractStart').value;
    if (startDate) {
        const endDate = new Date(startDate);
        endDate.setFullYear(endDate.getFullYear() + 1);
        endDate.setDate(endDate.getDate() - 1);
        document.getElementById('contractEnd').value = endDate.toISOString().split('T')[0];
    }
}

// Close PDF modal
function closePdfModal() {
    document.getElementById('pdfModal').style.display = 'none';
}

// Download PDF
function downloadPdf() {
    const link = document.createElement('a');
    link.href = FreshPlan.state.generatedPdf.url;
    link.download = FreshPlan.state.generatedPdf.filename;
    link.click();
}

// Close calculation modal
function closeCalcModal() {
    document.getElementById('calcModal').style.display = 'none';
}

// Apply calculation
function applyCalculation() {
    document.getElementById('targetRevenue').value = FreshPlan.state.calculatedRevenue;
    closeCalcModal();
    showMessage('Kalkulierter Umsatz wurde übernommen!', 'success');
}

// Toggle chain details
function toggleChainDetails() {
    const detail = document.getElementById('chainLocationsDetail');
    detail.style.display = detail.style.display === 'none' ? 'block' : 'none';
    
    if (detail.style.display === 'block') {
        updateChainDetails();
    }
}

// Toggle location detail
function toggleLocationDetail(index) {
    const detail = document.getElementById(`location-detail-${index}`);
    detail.style.display = detail.style.display === 'none' ? 'block' : 'none';
}

// Handle document upload
function handleDocumentUpload(type, input) {
    const file = input.files[0];
    if (file) {
        // Store file reference
        FreshPlan.state.uploadedDocuments[type] = file;
        
        // Show success indicator
        document.getElementById(type + 'Status').style.display = 'inline';
        
        showMessage(`${file.name} wurde hochgeladen`, 'success');
    }
}

// Reset documents
function resetDocuments() {
    FreshPlan.state.uploadedDocuments = {
        partnership: null,
        rabatt: null,
        agb: null
    };
    
    // Hide all status indicators
    ['partnership', 'rabatt', 'agb'].forEach(type => {
        document.getElementById(type + 'Status').style.display = 'none';
    });
    
    // Clear file inputs
    document.getElementById('uploadPartnership').value = '';
    document.getElementById('uploadRabatt').value = '';
    document.getElementById('uploadAGB').value = '';
    
    showMessage('Standard-Dokumente werden verwendet', 'success');
}

// Export functions
window.debounce = debounce;
window.formatCurrency = formatCurrency;
window.showMessage = showMessage;
window.getTranslation = getTranslation;
window.switchTab = switchTab;
window.updateProgress = updateProgress;
window.validateEmail = validateEmail;
window.validatePhone = validatePhone;
window.validateZip = validateZip;
window.validateRequired = validateRequired;
window.showFieldError = showFieldError;
window.clearFieldError = clearFieldError;
window.getIndustryName = getIndustryName;
window.updateContractEnd = updateContractEnd;
window.closePdfModal = closePdfModal;
window.downloadPdf = downloadPdf;
window.closeCalcModal = closeCalcModal;
window.applyCalculation = applyCalculation;
window.toggleChainDetails = toggleChainDetails;
window.toggleLocationDetail = toggleLocationDetail;
window.handleDocumentUpload = handleDocumentUpload;
window.resetDocuments = resetDocuments;