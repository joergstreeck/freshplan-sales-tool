/**
 * FreshPlan Settings Manager Module
 */

export class SettingsManager {
    constructor() {
        this.settings = {
            language: 'de',
            currency: 'EUR',
            salesperson: {
                name: '',
                email: '',
                phone: ''
            }
        };
    }
    
    initialize() {
        this.setupForm();
        this.loadSettings();
    }
    
    setupForm() {
        const settingsPanel = document.getElementById('settings');
        if (!settingsPanel) return;
        
        settingsPanel.innerHTML = `
            <div class="settings-container">
                <h2 class="section-title" data-i18n="settings.title">Einstellungen</h2>
                
                <form id="settingsForm" class="settings-form">
                    <div class="form-section">
                        <h3 class="form-section-title" data-i18n="settings.general">Allgemein</h3>
                        
                        <div class="form-row">
                            <div class="form-group">
                                <label for="settingsLanguage" data-i18n="settings.language">Sprache</label>
                                <select id="settingsLanguage" name="language">
                                    <option value="de">Deutsch</option>
                                    <option value="en">English</option>
                                </select>
                            </div>
                            
                            <div class="form-group">
                                <label for="currency" data-i18n="settings.currency">Währung</label>
                                <select id="currency" name="currency">
                                    <option value="EUR">EUR (€)</option>
                                    <option value="CHF">CHF</option>
                                </select>
                            </div>
                        </div>
                    </div>
                    
                    <div class="form-section">
                        <h3 class="form-section-title" data-i18n="settings.salesPerson">Verkäufer</h3>
                        
                        <div class="form-row">
                            <div class="form-group">
                                <label for="salespersonName" data-i18n="settings.name">Name</label>
                                <input type="text" id="salespersonName" name="salespersonName">
                            </div>
                            
                            <div class="form-group">
                                <label for="salespersonEmail" data-i18n="settings.email">E-Mail</label>
                                <input type="email" id="salespersonEmail" name="salespersonEmail">
                            </div>
                        </div>
                        
                        <div class="form-row">
                            <div class="form-group">
                                <label for="salespersonPhone" data-i18n="settings.phone">Telefon</label>
                                <input type="tel" id="salespersonPhone" name="salespersonPhone">
                            </div>
                        </div>
                    </div>
                    
                    <div class="form-section">
                        <h3 class="form-section-title" data-i18n="settings.exportImport">Export/Import</h3>
                        
                        <div class="export-import-actions">
                            <button type="button" class="btn btn-secondary" id="exportDataBtn">
                                <span data-i18n="settings.exportData">Daten exportieren</span>
                            </button>
                            
                            <div class="import-container">
                                <input type="file" id="importFile" accept=".json" style="display: none;">
                                <button type="button" class="btn btn-secondary" id="importDataBtn">
                                    <span data-i18n="settings.importData">Daten importieren</span>
                                </button>
                            </div>
                        </div>
                        
                        <div class="form-help">
                            <div class="form-help-icon">i</div>
                            <div>
                                Exportieren Sie alle Ihre Daten als JSON-Datei oder importieren Sie zuvor exportierte Daten.
                            </div>
                        </div>
                    </div>
                    
                    <div class="form-actions">
                        <button type="button" class="btn btn-secondary" id="settingsResetBtn">
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
    }
    
    setupEventListeners() {
        const form = document.getElementById('settingsForm');
        if (!form) return;
        
        // Form submission
        form.addEventListener('submit', (e) => {
            e.preventDefault();
            this.saveSettings();
        });
        
        // Language change
        document.getElementById('settingsLanguage')?.addEventListener('change', (e) => {
            this.updateLanguage(e.target.value);
        });
        
        // Export button
        document.getElementById('exportDataBtn')?.addEventListener('click', () => {
            this.exportData();
        });
        
        // Import button
        document.getElementById('importDataBtn')?.addEventListener('click', () => {
            document.getElementById('importFile')?.click();
        });
        
        // Import file change
        document.getElementById('importFile')?.addEventListener('change', (e) => {
            const file = e.target.files[0];
            if (file) {
                this.importData(file);
            }
        });
        
        // Reset button
        document.getElementById('settingsResetBtn')?.addEventListener('click', () => {
            this.resetSettings();
        });
    }
    
    updateLanguage(lang) {
        this.settings.language = lang;
        window.appState.language = lang;
        
        // Update main language selector
        const mainLangSelect = document.getElementById('languageSelect');
        if (mainLangSelect) {
            mainLangSelect.value = lang;
        }
        
        // Update page language
        import('./utils.js').then(module => {
            module.updatePageLanguage(lang);
        });
    }
    
    saveSettings() {
        const form = document.getElementById('settingsForm');
        if (!form) return;
        
        // Collect form data
        const formData = new FormData(form);
        
        this.settings.language = formData.get('language');
        this.settings.currency = formData.get('currency');
        this.settings.salesperson = {
            name: formData.get('salespersonName'),
            email: formData.get('salespersonEmail'),
            phone: formData.get('salespersonPhone')
        };
        
        // Save to app state and localStorage
        window.appState.settings = this.settings;
        localStorage.setItem('settings', JSON.stringify(this.settings));
        
        window.FreshPlan.showNotification('Einstellungen gespeichert', 'success');
    }
    
    loadSettings() {
        const savedSettings = localStorage.getItem('settings');
        if (savedSettings) {
            this.settings = JSON.parse(savedSettings);
            
            // Update form
            const form = document.getElementById('settingsForm');
            if (form) {
                form.elements['language'].value = this.settings.language;
                form.elements['currency'].value = this.settings.currency;
                form.elements['salespersonName'].value = this.settings.salesperson.name || '';
                form.elements['salespersonEmail'].value = this.settings.salesperson.email || '';
                form.elements['salespersonPhone'].value = this.settings.salesperson.phone || '';
            }
        }
    }
    
    resetSettings() {
        if (confirm('Möchten Sie wirklich alle Einstellungen zurücksetzen?')) {
            this.settings = {
                language: 'de',
                currency: 'EUR',
                salesperson: {
                    name: '',
                    email: '',
                    phone: ''
                }
            };
            
            document.getElementById('settingsForm')?.reset();
            
            // Clear saved settings
            localStorage.removeItem('settings');
            window.appState.settings = {};
            
            // Reset language
            this.updateLanguage('de');
        }
    }
    
    exportData() {
        import('./utils.js').then(module => {
            module.exportData();
            window.FreshPlan.showNotification('Daten wurden exportiert', 'success');
        });
    }
    
    importData(file) {
        import('./utils.js').then(module => {
            module.importData(file)
                .then(data => {
                    window.FreshPlan.showNotification('Daten wurden importiert', 'success');
                    
                    // Reload all modules
                    window.calculator?.loadSavedData();
                    window.customerManager?.loadSavedData();
                    window.profileManager?.loadSavedData();
                    this.loadSettings();
                    
                    // Update UI
                    window.FreshPlan.updateProgressBar();
                })
                .catch(error => {
                    window.FreshPlan.showError('Fehler beim Importieren der Daten');
                    console.error('Import error:', error);
                });
        });
    }
}