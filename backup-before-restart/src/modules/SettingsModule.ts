/**
 * Settings Module - TypeScript version
 * Handles application settings and integrations
 */

import Module from '../core/Module';
import { useStore } from '../store';
import { validateEmail } from '../utils/validation';
import type { SettingsState, SalespersonInfo } from '../types';

interface IntegrationTestResult {
  success: boolean;
  message: string;
  details?: any;
}

export default class SettingsModule extends Module {
  private form: HTMLFormElement | null = null;
  private importInput: HTMLInputElement | null = null;

  constructor() {
    super('settings');
  }

  async setup(): Promise<void> {
    // Get form element
    this.form = this.dom.$('#settingsForm');
    
    // Build settings UI
    this.buildSettingsUI();
    
    // Load current settings
    this.loadSettings();
  }

  bindEvents(): void {
    if (!this.form) return;

    // Form submission
    this.on(this.form, 'submit', (e: Event) => {
      e.preventDefault();
      this.saveSettings();
    });

    // Test connections
    this.on('#testMondayBtn', 'click', () => this.testMondayConnection());
    this.on('#testEmailBtn', 'click', () => this.testEmailConnection());
    this.on('#testXentralBtn', 'click', () => this.testXentralConnection());
    
    // Import/Export
    this.on('#exportSettingsBtn', 'click', () => this.exportSettings());
    this.on('#importSettingsBtn', 'click', () => this.triggerImport());
    this.on('#importSettingsFile', 'change', (e: Event) => this.importSettings(e));

    // Auto-update offer tab salesperson
    this.on('#defaultSalespersonName, #defaultSalespersonEmail, #defaultSalespersonPhone', 'input', () => {
      this.updateOfferSalesperson();
    });

    // Integration toggle handlers
    this.on('#enableMonday', 'change', (e: Event) => {
      this.toggleIntegration('monday', (e.target as HTMLInputElement).checked);
    });
    
    this.on('#enableEmail', 'change', (e: Event) => {
      this.toggleIntegration('email', (e.target as HTMLInputElement).checked);
    });
    
    this.on('#enableXentral', 'change', (e: Event) => {
      this.toggleIntegration('xentral', (e.target as HTMLInputElement).checked);
    });
  }

  subscribeToState(): void {
    // React to settings changes from other sources
    useStore.subscribe(
      (state) => state.settings,
      () => {
        if (!this.form?.contains(document.activeElement)) {
          this.loadSettings();
        }
      }
    );
  }

  /**
   * Build settings UI
   */
  private buildSettingsUI(): void {
    const container = this.dom.$('#settings');
    if (!container) return;

    container.innerHTML = `
      <form id="settingsForm" class="settings-form">
        <div class="form-header">
          <h2 data-i18n="settings.title">Einstellungen</h2>
          <div class="form-actions">
            <button type="button" id="exportSettingsBtn" class="btn btn-secondary" data-i18n="settings.export">
              Exportieren
            </button>
            <button type="button" id="importSettingsBtn" class="btn btn-secondary" data-i18n="settings.import">
              Importieren
            </button>
            <input type="file" id="importSettingsFile" accept=".json" style="display: none;">
          </div>
        </div>

        <!-- Salesperson Information -->
        <div class="form-section">
          <h3 data-i18n="settings.salesperson">Verkäufer-Informationen</h3>
          <div class="form-row">
            <div class="form-group">
              <label for="defaultSalespersonName" data-i18n="settings.name">Name</label>
              <input type="text" id="defaultSalespersonName" required>
            </div>
          </div>
          
          <div class="form-row">
            <div class="form-group">
              <label for="defaultSalespersonEmail" data-i18n="settings.email">E-Mail</label>
              <input type="email" id="defaultSalespersonEmail" required>
            </div>
            <div class="form-group">
              <label for="defaultSalespersonPhone" data-i18n="settings.phone">Telefon</label>
              <input type="tel" id="defaultSalespersonPhone" required>
            </div>
          </div>
          
          <div class="form-row">
            <div class="form-group">
              <label for="defaultSalespersonMobile" data-i18n="settings.mobile">Mobil</label>
              <input type="tel" id="defaultSalespersonMobile">
            </div>
          </div>
        </div>

        <!-- Default Values -->
        <div class="form-section">
          <h3 data-i18n="settings.defaults">Standardwerte</h3>
          <div class="form-row">
            <div class="form-group">
              <label for="defaultDiscount" data-i18n="settings.defaultDiscount">Standard-Rabatt (%)</label>
              <input type="number" id="defaultDiscount" min="0" max="100" step="1" value="15">
            </div>
            <div class="form-group">
              <label for="defaultContractDuration" data-i18n="settings.contractDuration">Vertragslaufzeit (Monate)</label>
              <select id="defaultContractDuration">
                <option value="12">12 Monate</option>
                <option value="24" selected>24 Monate</option>
                <option value="36">36 Monate</option>
              </select>
            </div>
          </div>
        </div>

        <!-- Monday.com Integration -->
        <div class="form-section">
          <div class="section-header">
            <h3>Monday.com Integration</h3>
            <label class="toggle">
              <input type="checkbox" id="enableMonday">
              <span class="toggle-slider"></span>
            </label>
          </div>
          
          <div id="mondaySettings" class="integration-settings" style="display: none;">
            <div class="form-row">
              <div class="form-group">
                <label for="mondayToken" data-i18n="settings.apiToken">API Token</label>
                <input type="password" id="mondayToken" placeholder="Ihr Monday.com API Token">
              </div>
            </div>
            
            <div class="form-row">
              <div class="form-group">
                <label for="mondayBoardId" data-i18n="settings.boardId">Board ID</label>
                <input type="text" id="mondayBoardId" placeholder="Board ID für Leads">
              </div>
              <div class="form-group">
                <button type="button" id="testMondayBtn" class="btn btn-secondary" data-i18n="settings.testConnection">
                  Verbindung testen
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Email Integration -->
        <div class="form-section">
          <div class="section-header">
            <h3>E-Mail Integration</h3>
            <label class="toggle">
              <input type="checkbox" id="enableEmail">
              <span class="toggle-slider"></span>
            </label>
          </div>
          
          <div id="emailSettings" class="integration-settings" style="display: none;">
            <div class="form-row">
              <div class="form-group">
                <label for="smtpServer" data-i18n="settings.smtpServer">SMTP Server</label>
                <input type="text" id="smtpServer" placeholder="smtp.example.com">
              </div>
              <div class="form-group">
                <label for="smtpPort" data-i18n="settings.smtpPort">Port</label>
                <input type="number" id="smtpPort" value="587" min="1" max="65535">
              </div>
            </div>
            
            <div class="form-row">
              <div class="form-group">
                <label for="smtpEmail" data-i18n="settings.smtpEmail">E-Mail</label>
                <input type="email" id="smtpEmail" placeholder="sender@example.com">
              </div>
              <div class="form-group">
                <label for="smtpPassword" data-i18n="settings.smtpPassword">Passwort</label>
                <input type="password" id="smtpPassword">
              </div>
            </div>
            
            <div class="form-row">
              <div class="form-group">
                <button type="button" id="testEmailBtn" class="btn btn-secondary" data-i18n="settings.testConnection">
                  Verbindung testen
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Xentral Integration -->
        <div class="form-section">
          <div class="section-header">
            <h3>Xentral Integration</h3>
            <label class="toggle">
              <input type="checkbox" id="enableXentral">
              <span class="toggle-slider"></span>
            </label>
          </div>
          
          <div id="xentralSettings" class="integration-settings" style="display: none;">
            <div class="form-row">
              <div class="form-group">
                <label for="xentralUrl" data-i18n="settings.xentralUrl">Xentral URL</label>
                <input type="url" id="xentralUrl" placeholder="https://ihr-xentral.de">
              </div>
            </div>
            
            <div class="form-row">
              <div class="form-group">
                <label for="xentralKey" data-i18n="settings.xentralKey">API Key</label>
                <input type="password" id="xentralKey" placeholder="Ihr Xentral API Key">
              </div>
              <div class="form-group">
                <button type="button" id="testXentralBtn" class="btn btn-secondary" data-i18n="settings.testConnection">
                  Verbindung testen
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Form Actions -->
        <div class="form-actions">
          <button type="submit" class="btn btn-primary" data-i18n="settings.save">
            Einstellungen speichern
          </button>
        </div>
      </form>
    `;

    // Store import input reference
    this.importInput = this.dom.$('#importSettingsFile');
    this.form = this.dom.$('#settingsForm');
  }

  /**
   * Load settings into form
   */
  private loadSettings(): void {
    const settings = useStore.getState().settings;

    // Salesperson info
    this.setFieldValue('defaultSalespersonName', settings.salesperson.name);
    this.setFieldValue('defaultSalespersonEmail', settings.salesperson.email);
    this.setFieldValue('defaultSalespersonPhone', settings.salesperson.phone);
    this.setFieldValue('defaultSalespersonMobile', settings.salesperson.mobile);

    // Default values
    this.setFieldValue('defaultDiscount', settings.defaults.discount);
    this.setFieldValue('defaultContractDuration', settings.defaults.contractDuration);

    // Monday integration
    const mondayEnabled = !!settings.integrations.monday.token;
    this.setFieldValue('enableMonday', mondayEnabled, 'checkbox');
    this.toggleIntegration('monday', mondayEnabled);
    this.setFieldValue('mondayToken', settings.integrations.monday.token);
    this.setFieldValue('mondayBoardId', settings.integrations.monday.boardId);

    // Email integration
    const emailEnabled = !!settings.integrations.email.smtpServer;
    this.setFieldValue('enableEmail', emailEnabled, 'checkbox');
    this.toggleIntegration('email', emailEnabled);
    this.setFieldValue('smtpServer', settings.integrations.email.smtpServer);
    this.setFieldValue('smtpEmail', settings.integrations.email.smtpEmail);
    this.setFieldValue('smtpPassword', settings.integrations.email.smtpPassword);

    // Xentral integration
    const xentralEnabled = !!settings.integrations.xentral.url;
    this.setFieldValue('enableXentral', xentralEnabled, 'checkbox');
    this.toggleIntegration('xentral', xentralEnabled);
    this.setFieldValue('xentralUrl', settings.integrations.xentral.url);
    this.setFieldValue('xentralKey', settings.integrations.xentral.key);
  }

  /**
   * Set field value safely
   */
  private setFieldValue(fieldId: string, value: any, type: string = 'text'): void {
    const field = this.dom.$(`#${fieldId}`) as HTMLInputElement;
    if (field) {
      if (type === 'checkbox') {
        field.checked = Boolean(value);
      } else {
        field.value = String(value || '');
      }
    }
  }

  /**
   * Get form data
   */
  private getFormData(): SettingsState {
    return {
      salesperson: {
        name: (this.dom.$('#defaultSalespersonName') as HTMLInputElement)?.value || '',
        email: (this.dom.$('#defaultSalespersonEmail') as HTMLInputElement)?.value || '',
        phone: (this.dom.$('#defaultSalespersonPhone') as HTMLInputElement)?.value || '',
        mobile: (this.dom.$('#defaultSalespersonMobile') as HTMLInputElement)?.value || ''
      },
      defaults: {
        discount: parseInt((this.dom.$('#defaultDiscount') as HTMLInputElement)?.value || '15'),
        contractDuration: parseInt((this.dom.$('#defaultContractDuration') as HTMLSelectElement)?.value || '24')
      },
      integrations: {
        monday: {
          token: (this.dom.$('#mondayToken') as HTMLInputElement)?.value || '',
          boardId: (this.dom.$('#mondayBoardId') as HTMLInputElement)?.value || ''
        },
        email: {
          smtpServer: (this.dom.$('#smtpServer') as HTMLInputElement)?.value || '',
          smtpEmail: (this.dom.$('#smtpEmail') as HTMLInputElement)?.value || '',
          smtpPassword: (this.dom.$('#smtpPassword') as HTMLInputElement)?.value || ''
        },
        xentral: {
          url: (this.dom.$('#xentralUrl') as HTMLInputElement)?.value || '',
          key: (this.dom.$('#xentralKey') as HTMLInputElement)?.value || ''
        }
      }
    };
  }

  /**
   * Save settings
   */
  private saveSettings(): void {
    const settings = this.getFormData();

    // Validate email
    if (settings.salesperson.email && !validateEmail(settings.salesperson.email)) {
      this.showError('Bitte geben Sie eine gültige E-Mail-Adresse ein');
      return;
    }

    // Update store
    const store = useStore.getState();
    store.updateSettings(settings);

    this.showSuccess('Einstellungen gespeichert');
    this.emit('saved', settings);
  }

  /**
   * Toggle integration visibility
   */
  private toggleIntegration(integration: 'monday' | 'email' | 'xentral', enabled: boolean): void {
    const settingsDiv = this.dom.$(`#${integration}Settings`);
    if (settingsDiv) {
      (settingsDiv as HTMLElement).style.display = enabled ? 'block' : 'none';
    }
  }

  /**
   * Update offer tab salesperson in real-time
   */
  private updateOfferSalesperson(): void {
    const salesperson: Partial<SalespersonInfo> = {
      name: (this.dom.$('#defaultSalespersonName') as HTMLInputElement)?.value || '',
      email: (this.dom.$('#defaultSalespersonEmail') as HTMLInputElement)?.value || '',
      phone: (this.dom.$('#defaultSalespersonPhone') as HTMLInputElement)?.value || ''
    };

    // Update offer tab fields if they exist
    this.dom.val('#salespersonName', salesperson.name || '');
    this.dom.val('#salespersonEmail', salesperson.email || '');
    this.dom.val('#salespersonPhone', salesperson.phone || '');
  }

  /**
   * Test Monday.com connection
   */
  private async testMondayConnection(): Promise<void> {
    const token = (this.dom.$('#mondayToken') as HTMLInputElement)?.value;
    const boardId = (this.dom.$('#mondayBoardId') as HTMLInputElement)?.value;

    if (!token) {
      this.showError('Bitte geben Sie einen API Token ein');
      return;
    }

    this.setLoading(true);

    try {
      // In a real implementation, this would make an API call
      const result = await this.mockTestConnection('monday', { token, boardId });
      
      if (result.success) {
        this.showSuccess('Monday.com Verbindung erfolgreich');
      } else {
        this.showError(result.message);
      }
    } catch (error) {
      this.showError('Verbindungsfehler');
    } finally {
      this.setLoading(false);
    }
  }

  /**
   * Test Email connection
   */
  private async testEmailConnection(): Promise<void> {
    const server = (this.dom.$('#smtpServer') as HTMLInputElement)?.value;
    const email = (this.dom.$('#smtpEmail') as HTMLInputElement)?.value;
    const password = (this.dom.$('#smtpPassword') as HTMLInputElement)?.value;

    if (!server || !email || !password) {
      this.showError('Bitte füllen Sie alle E-Mail-Felder aus');
      return;
    }

    this.setLoading(true);

    try {
      const result = await this.mockTestConnection('email', { server, email, password });
      
      if (result.success) {
        this.showSuccess('E-Mail Verbindung erfolgreich');
      } else {
        this.showError(result.message);
      }
    } catch (error) {
      this.showError('Verbindungsfehler');
    } finally {
      this.setLoading(false);
    }
  }

  /**
   * Test Xentral connection
   */
  private async testXentralConnection(): Promise<void> {
    const url = (this.dom.$('#xentralUrl') as HTMLInputElement)?.value;
    const key = (this.dom.$('#xentralKey') as HTMLInputElement)?.value;

    if (!url || !key) {
      this.showError('Bitte füllen Sie alle Xentral-Felder aus');
      return;
    }

    this.setLoading(true);

    try {
      const result = await this.mockTestConnection('xentral', { url, key });
      
      if (result.success) {
        this.showSuccess('Xentral Verbindung erfolgreich');
      } else {
        this.showError(result.message);
      }
    } catch (error) {
      this.showError('Verbindungsfehler');
    } finally {
      this.setLoading(false);
    }
  }

  /**
   * Mock connection test (replace with real API calls)
   */
  private async mockTestConnection(
    type: string, 
    config: any
  ): Promise<IntegrationTestResult> {
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    // Mock validation
    if (type === 'monday' && config.token.length > 10) {
      return { success: true, message: 'Connected' };
    }
    
    if (type === 'email' && config.server.includes('.')) {
      return { success: true, message: 'Connected' };
    }
    
    if (type === 'xentral' && config.url.startsWith('http')) {
      return { success: true, message: 'Connected' };
    }
    
    return { success: false, message: 'Ungültige Zugangsdaten' };
  }

  /**
   * Export settings to JSON
   */
  private exportSettings(): void {
    const settings = useStore.getState().settings;
    const exportData = {
      ...settings,
      exported: new Date().toISOString(),
      version: '3.0.0'
    };

    // Remove sensitive data
    if (exportData.integrations?.email?.smtpPassword) {
      exportData.integrations.email.smtpPassword = '***';
    }

    const json = JSON.stringify(exportData, null, 2);
    const blob = new Blob([json], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    
    const a = this.dom.create('a', {
      href: url,
      download: `freshplan-settings-${Date.now()}.json`
    });
    
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);

    this.showSuccess('Einstellungen exportiert');
  }

  /**
   * Trigger import file dialog
   */
  private triggerImport(): void {
    this.importInput?.click();
  }

  /**
   * Import settings from JSON
   */
  private async importSettings(event: Event): Promise<void> {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    
    if (!file) return;

    try {
      const text = await file.text();
      const imported = JSON.parse(text);

      // Validate structure
      if (!imported.salesperson || !imported.defaults || !imported.integrations) {
        throw new Error('Ungültiges Einstellungsformat');
      }

      // Merge with current settings (preserve passwords if masked)
      const current = useStore.getState().settings;
      const merged: SettingsState = {
        ...imported,
        integrations: {
          ...imported.integrations,
          email: {
            ...imported.integrations.email,
            smtpPassword: imported.integrations.email.smtpPassword === '***' 
              ? current.integrations.email.smtpPassword 
              : imported.integrations.email.smtpPassword
          }
        }
      };

      // Update store
      useStore.getState().updateSettings(merged);
      
      // Reload form
      this.loadSettings();
      
      this.showSuccess('Einstellungen importiert');
      
      // Clear file input
      input.value = '';
      
    } catch (error) {
      console.error('Import error:', error);
      this.showError('Fehler beim Importieren der Einstellungen');
    }
  }

  /**
   * Public API
   */
  
  getSetting(path: string): any {
    const settings = useStore.getState().settings;
    const keys = path.split('.');
    let value: any = settings;
    
    for (const key of keys) {
      if (value && typeof value === 'object' && key in value) {
        value = value[key];
      } else {
        return undefined;
      }
    }
    
    return value;
  }

  getSettings(): SettingsState {
    return useStore.getState().settings;
  }

  isIntegrationEnabled(integration: 'monday' | 'email' | 'xentral'): boolean {
    const settings = useStore.getState().settings;
    
    switch (integration) {
      case 'monday':
        return !!settings.integrations.monday.token;
      case 'email':
        return !!settings.integrations.email.smtpServer;
      case 'xentral':
        return !!settings.integrations.xentral.url;
      default:
        return false;
    }
  }

  /**
   * Cleanup module resources
   */
  cleanup(): void {
    // Module cleanup is handled by base class
  }
}