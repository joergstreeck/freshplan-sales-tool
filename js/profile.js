/**
 * FreshPlan Profile Manager Module
 */

export class ProfileManager {
    constructor() {
        this.profileData = {
            customerPotential: '',
            currentSituation: '',
            goals: '',
            arguments: [],
            strategy: '',
            nextSteps: '',
            completed: false
        };
    }
    
    initialize() {
        this.setupForm();
        this.loadSavedData();
    }
    
    setupForm() {
        const profilePanel = document.getElementById('profile');
        if (!profilePanel) return;
        
        profilePanel.innerHTML = `
            <div class="profile-container">
                <h2 class="section-title" data-i18n="profile.title">Kundenprofil</h2>
                
                <form id="profileForm" class="profile-form">
                    <div class="form-section">
                        <h3 class="form-section-title" data-i18n="profile.customerPotential">Kundenpotenzial</h3>
                        <div class="form-group">
                            <textarea id="customerPotential" name="customerPotential" rows="4" 
                                placeholder="Beschreiben Sie das Potenzial dieses Kunden..."></textarea>
                        </div>
                    </div>
                    
                    <div class="form-section">
                        <h3 class="form-section-title" data-i18n="profile.currentSituation">Aktuelle Situation</h3>
                        <div class="form-group">
                            <textarea id="currentSituation" name="currentSituation" rows="4"
                                placeholder="Wie ist die aktuelle Situation des Kunden?"></textarea>
                        </div>
                    </div>
                    
                    <div class="form-section">
                        <h3 class="form-section-title" data-i18n="profile.goals">Ziele & Bedürfnisse</h3>
                        <div class="form-group">
                            <textarea id="goals" name="goals" rows="4"
                                placeholder="Welche Ziele und Bedürfnisse hat der Kunde?"></textarea>
                        </div>
                    </div>
                    
                    <div class="form-section">
                        <h3 class="form-section-title" data-i18n="profile.arguments">Verkaufsargumente</h3>
                        <div class="arguments-list" id="argumentsList">
                            <!-- Arguments will be rendered here -->
                        </div>
                        <button type="button" class="btn btn-secondary" id="addArgumentBtn">
                            <span data-i18n="common.add">+ Argument hinzufügen</span>
                        </button>
                    </div>
                    
                    <div class="form-section">
                        <h3 class="form-section-title" data-i18n="profile.strategy">Strategie</h3>
                        <div class="form-group">
                            <textarea id="strategy" name="strategy" rows="4"
                                placeholder="Welche Verkaufsstrategie verfolgen Sie?"></textarea>
                        </div>
                    </div>
                    
                    <div class="form-section">
                        <h3 class="form-section-title" data-i18n="profile.nextSteps">Nächste Schritte</h3>
                        <div class="form-group">
                            <textarea id="nextSteps" name="nextSteps" rows="4"
                                placeholder="Was sind die nächsten konkreten Schritte?"></textarea>
                        </div>
                    </div>
                    
                    <div class="form-actions">
                        <button type="button" class="btn btn-secondary" id="profileResetBtn">
                            <span data-i18n="common.reset">Zurücksetzen</span>
                        </button>
                        <button type="submit" class="btn btn-primary">
                            <span data-i18n="common.save">Speichern</span>
                        </button>
                    </div>
                </form>
            </div>
        `;
        
        this.setupEventListeners();
        this.renderArguments();
    }
    
    setupEventListeners() {
        const form = document.getElementById('profileForm');
        if (!form) return;
        
        // Form submission
        form.addEventListener('submit', (e) => {
            e.preventDefault();
            this.saveData();
        });
        
        // Add argument button
        document.getElementById('addArgumentBtn')?.addEventListener('click', () => {
            this.addArgument();
        });
        
        // Reset button
        document.getElementById('profileResetBtn')?.addEventListener('click', () => {
            this.resetForm();
        });
        
        // Auto-save on input
        form.querySelectorAll('textarea').forEach(textarea => {
            textarea.addEventListener('input', () => {
                this.autoSave();
            });
        });
    }
    
    addArgument() {
        const argument = {
            id: Date.now(),
            text: ''
        };
        
        this.profileData.arguments.push(argument);
        this.renderArguments();
    }
    
    renderArguments() {
        const argumentsList = document.getElementById('argumentsList');
        if (!argumentsList) return;
        
        argumentsList.innerHTML = this.profileData.arguments.map((arg, index) => `
            <div class="argument-item" data-id="${arg.id}">
                <div class="argument-number">${index + 1}</div>
                <div class="argument-content">
                    <input type="text" value="${arg.text}" 
                           placeholder="Verkaufsargument eingeben..."
                           onchange="window.profileManager.updateArgument(${arg.id}, this.value)">
                </div>
                <button type="button" class="btn btn-sm btn-secondary" 
                        onclick="window.profileManager.removeArgument(${arg.id})">
                    ×
                </button>
            </div>
        `).join('');
    }
    
    updateArgument(id, text) {
        const argument = this.profileData.arguments.find(a => a.id === id);
        if (argument) {
            argument.text = text;
            this.autoSave();
        }
    }
    
    removeArgument(id) {
        this.profileData.arguments = this.profileData.arguments.filter(a => a.id !== id);
        this.renderArguments();
        this.autoSave();
    }
    
    autoSave() {
        // Debounced auto-save
        if (this.autoSaveTimeout) {
            clearTimeout(this.autoSaveTimeout);
        }
        
        this.autoSaveTimeout = setTimeout(() => {
            this.saveData(true);
        }, 1000);
    }
    
    saveData(isAutoSave = false) {
        const form = document.getElementById('profileForm');
        if (!form) return;
        
        // Collect form data
        const formData = new FormData(form);
        
        for (const [key, value] of formData.entries()) {
            if (this.profileData.hasOwnProperty(key)) {
                this.profileData[key] = value;
            }
        }
        
        // Check if profile is completed
        this.profileData.completed = !!(
            this.profileData.customerPotential &&
            this.profileData.currentSituation &&
            this.profileData.goals &&
            this.profileData.arguments.length > 0 &&
            this.profileData.strategy &&
            this.profileData.nextSteps
        );
        
        // Save to app state and localStorage
        window.appState.profileData = this.profileData;
        localStorage.setItem('profileData', JSON.stringify(this.profileData));
        
        if (!isAutoSave) {
            window.FreshPlan.showNotification('Profil gespeichert', 'success');
        }
        
        // Update progress
        window.FreshPlan.updateProgressBar();
    }
    
    loadSavedData() {
        const savedData = localStorage.getItem('profileData');
        if (savedData) {
            this.profileData = JSON.parse(savedData);
            
            // Update form
            const form = document.getElementById('profileForm');
            if (form) {
                for (const [key, value] of Object.entries(this.profileData)) {
                    if (key !== 'arguments' && key !== 'completed') {
                        const field = form.elements[key];
                        if (field) {
                            field.value = value;
                        }
                    }
                }
            }
            
            // Render arguments
            this.renderArguments();
        }
    }
    
    resetForm() {
        if (confirm('Möchten Sie wirklich alle Profilangaben zurücksetzen?')) {
            this.profileData = {
                customerPotential: '',
                currentSituation: '',
                goals: '',
                arguments: [],
                strategy: '',
                nextSteps: '',
                completed: false
            };
            
            document.getElementById('profileForm')?.reset();
            this.renderArguments();
            
            // Clear saved data
            localStorage.removeItem('profileData');
            window.appState.profileData = {};
            
            window.FreshPlan.updateProgressBar();
        }
    }
}