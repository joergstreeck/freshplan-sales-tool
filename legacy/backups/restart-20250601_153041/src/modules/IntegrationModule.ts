/**
 * Integration Module
 * Handles external API integrations UI
 */

import Module from '../core/Module';
import { useStore } from '../store';
import { IntegrationService } from '../services';
import type { CustomerData, CalculatorCalculation } from '../types';

export default class IntegrationModule extends Module {
  private integrationService: IntegrationService;
  private isProcessing: boolean = false;

  constructor() {
    super('integration');
    this.integrationService = new IntegrationService();
  }

  async setup(): Promise<void> {
    // Create integration UI if needed
    this.createIntegrationUI();
  }

  bindEvents(): void {
    // Test connections
    this.on('#test-monday', 'click', () => this.testMondayConnection());
    this.on('#test-email', 'click', () => this.testEmailConnection());
    this.on('#test-xentral', 'click', () => this.testXentralConnection());
    
    // Create lead button
    this.on('#create-lead-all', 'click', () => this.createLeadInAllSystems());
    
    // Individual create buttons
    this.on('#create-monday-lead', 'click', () => this.createMondayLead());
    this.on('#send-email', 'click', () => this.sendEmail());
    this.on('#create-xentral-customer', 'click', () => this.createXentralCustomer());
  }

  subscribeToState(): void {
    // Subscribe to settings changes
    useStore.subscribe(
      (state) => state.settings.integrations,
      () => this.updateIntegrationStatus()
    );
  }

  /**
   * Create integration UI elements
   */
  private createIntegrationUI(): void {
    const container = this.dom.$('#integration-controls');
    if (!container) return;

    container.innerHTML = `
      <div class="integration-status">
        <h3>Integration Status</h3>
        <div class="status-grid">
          <div class="status-item" id="monday-status">
            <span class="service-name">Monday.com</span>
            <span class="status-indicator" data-status="unconfigured"></span>
            <button id="test-monday" class="btn-sm">Test</button>
          </div>
          <div class="status-item" id="email-status">
            <span class="service-name">Email</span>
            <span class="status-indicator" data-status="unconfigured"></span>
            <button id="test-email" class="btn-sm">Test</button>
          </div>
          <div class="status-item" id="xentral-status">
            <span class="service-name">Xentral</span>
            <span class="status-indicator" data-status="unconfigured"></span>
            <button id="test-xentral" class="btn-sm">Test</button>
          </div>
        </div>
      </div>
      
      <div class="integration-actions">
        <button id="create-lead-all" class="btn-primary" disabled>
          Create Lead in All Systems
        </button>
        <div class="individual-actions">
          <button id="create-monday-lead" class="btn-secondary" disabled>
            Create Monday Lead
          </button>
          <button id="send-email" class="btn-secondary" disabled>
            Send Email
          </button>
          <button id="create-xentral-customer" class="btn-secondary" disabled>
            Create Xentral Customer
          </button>
        </div>
      </div>
      
      <div id="integration-results" class="integration-results hidden"></div>
    `;
    
    this.updateIntegrationStatus();
  }

  /**
   * Update integration status indicators
   */
  private updateIntegrationStatus(): void {
    const integrations = useStore.getState().settings.integrations;
    
    // Monday status
    const mondayConfigured = !!(integrations.monday.token && integrations.monday.boardId);
    this.updateStatusIndicator('monday-status', mondayConfigured);
    
    // Email status  
    const emailConfigured = !!(
      integrations.email.smtpServer && 
      integrations.email.smtpEmail && 
      integrations.email.smtpPassword
    );
    this.updateStatusIndicator('email-status', emailConfigured);
    
    // Xentral status
    const xentralConfigured = !!(integrations.xentral.url && integrations.xentral.key);
    this.updateStatusIndicator('xentral-status', xentralConfigured);
    
    // Update button states
    const anyConfigured = mondayConfigured || emailConfigured || xentralConfigured;
    const createAllBtn = this.dom.$('#create-lead-all') as HTMLButtonElement;
    if (createAllBtn) createAllBtn.disabled = !anyConfigured || this.isProcessing;
    
    const mondayBtn = this.dom.$('#create-monday-lead') as HTMLButtonElement;
    if (mondayBtn) mondayBtn.disabled = !mondayConfigured || this.isProcessing;
    
    const emailBtn = this.dom.$('#send-email') as HTMLButtonElement;
    if (emailBtn) emailBtn.disabled = !emailConfigured || this.isProcessing;
    
    const xentralBtn = this.dom.$('#create-xentral-customer') as HTMLButtonElement;
    if (xentralBtn) xentralBtn.disabled = !xentralConfigured || this.isProcessing;
  }

  /**
   * Update status indicator UI
   */
  private updateStatusIndicator(elementId: string, isConfigured: boolean): void {
    const statusItem = this.dom.$(`#${elementId}`);
    if (!statusItem) return;
    
    const indicator = statusItem.querySelector('.status-indicator');
    if (indicator) {
      indicator.setAttribute('data-status', isConfigured ? 'configured' : 'unconfigured');
    }
  }

  /**
   * Test Monday.com connection
   */
  private async testMondayConnection(): Promise<void> {
    this.setProcessing(true);
    this.showInfo('Testing Monday.com connection...');
    
    const result = await this.integrationService.getMondayBoards();
    
    if (result.success) {
      this.showSuccess(`Monday.com connected! Found ${result.data.length} boards.`);
      this.updateStatusIndicator('monday-status', true);
    } else {
      this.showError(`Monday.com test failed: ${result.error}`);
    }
    
    this.setProcessing(false);
  }

  /**
   * Test email connection
   */
  private async testEmailConnection(): Promise<void> {
    this.setProcessing(true);
    this.showInfo('Testing email configuration...');
    
    const result = await this.integrationService.validateEmailSettings();
    
    if (result.success) {
      this.showSuccess('Email configuration is valid!');
      this.updateStatusIndicator('email-status', true);
    } else {
      this.showError(`Email test failed: ${result.error}`);
    }
    
    this.setProcessing(false);
  }

  /**
   * Test Xentral connection
   */
  private async testXentralConnection(): Promise<void> {
    this.setProcessing(true);
    this.showInfo('Testing Xentral connection...');
    
    // Try to get a non-existent customer to test auth
    const result = await this.integrationService.getXentralCustomer(1);
    
    if (result.success || result.error?.includes('not found')) {
      this.showSuccess('Xentral connected successfully!');
      this.updateStatusIndicator('xentral-status', true);
    } else {
      this.showError(`Xentral test failed: ${result.error}`);
    }
    
    this.setProcessing(false);
  }

  /**
   * Create lead in all configured systems
   */
  private async createLeadInAllSystems(): Promise<void> {
    const customer = this.getCustomerData();
    const calculation = this.getCalculation();
    
    if (!customer || !calculation) {
      this.showError('Please complete customer data and calculation first');
      return;
    }
    
    this.setProcessing(true);
    this.showInfo('Creating lead in all systems...');
    
    // Get PDF if available
    const pdfData = useStore.getState().pdf.currentPDF;
    
    const results = await this.integrationService.createLeadInAllSystems(
      customer,
      calculation,
      pdfData
    );
    
    this.displayResults(results);
    this.setProcessing(false);
  }

  /**
   * Create Monday.com lead only
   */
  private async createMondayLead(): Promise<void> {
    const customer = this.getCustomerData();
    const calculation = this.getCalculation();
    
    if (!customer || !calculation) {
      this.showError('Please complete customer data and calculation first');
      return;
    }
    
    this.setProcessing(true);
    this.showInfo('Creating Monday.com lead...');
    
    const result = await this.integrationService.createMondayLead(customer, calculation);
    
    if (result.success) {
      this.showSuccess(`Monday.com lead created! ID: ${result.data.id}`);
    } else {
      this.showError(`Failed to create Monday lead: ${result.error}`);
    }
    
    this.setProcessing(false);
  }

  /**
   * Send email only
   */
  private async sendEmail(): Promise<void> {
    const customer = this.getCustomerData();
    const calculation = this.getCalculation();
    
    if (!customer || !calculation || !customer.contactEmail) {
      this.showError('Please complete customer data with email first');
      return;
    }
    
    this.setProcessing(true);
    this.showInfo('Sending email...');
    
    const salesperson = useStore.getState().settings.salesperson;
    const pdfData = useStore.getState().pdf.currentPDF;
    
    const result = await this.integrationService.sendEmail({
      to: customer.contactEmail,
      subject: `FreshPlan Offer for ${customer.companyName}`,
      body: this.generateEmailBody(customer, calculation, salesperson),
      attachments: pdfData ? [{
        filename: 'FreshPlan_Offer.pdf',
        content: pdfData,
        encoding: 'base64'
      }] : undefined
    });
    
    if (result.success) {
      this.showSuccess('Email sent successfully!');
    } else {
      this.showError(`Failed to send email: ${result.error}`);
    }
    
    this.setProcessing(false);
  }

  /**
   * Create Xentral customer only
   */
  private async createXentralCustomer(): Promise<void> {
    const customer = this.getCustomerData();
    
    if (!customer) {
      this.showError('Please complete customer data first');
      return;
    }
    
    this.setProcessing(true);
    this.showInfo('Creating Xentral customer...');
    
    const result = await this.integrationService.createXentralCustomer(customer);
    
    if (result.success) {
      this.showSuccess(`Xentral customer created! ID: ${result.data.kundennummer}`);
    } else {
      this.showError(`Failed to create Xentral customer: ${result.error}`);
    }
    
    this.setProcessing(false);
  }

  /**
   * Display integration results
   */
  private displayResults(results: any): void {
    const resultsContainer = this.dom.$('#integration-results');
    if (!resultsContainer) return;
    
    let html = '<h4>Integration Results</h4><ul>';
    
    if (results.monday) {
      const status = results.monday.success ? '✅' : '❌';
      const message = results.monday.success 
        ? `Lead created (ID: ${results.monday.data?.id})`
        : results.monday.error;
      html += `<li>${status} Monday.com: ${message}</li>`;
    }
    
    if (results.email) {
      const status = results.email.success ? '✅' : '❌';
      const message = results.email.success 
        ? 'Email sent successfully'
        : results.email.error;
      html += `<li>${status} Email: ${message}</li>`;
    }
    
    if (results.xentral) {
      const status = results.xentral.success ? '✅' : '❌';
      const message = results.xentral.success 
        ? `Customer/Offer created`
        : results.xentral.error;
      html += `<li>${status} Xentral: ${message}</li>`;
    }
    
    html += '</ul>';
    
    resultsContainer.innerHTML = html;
    this.dom.show(resultsContainer);
  }

  /**
   * Helper methods
   */
  
  private getCustomerData(): CustomerData | null {
    return useStore.getState().customer.data;
  }
  
  private getCalculation(): CalculatorCalculation | null {
    return useStore.getState().calculator.calculation;
  }
  
  private setProcessing(processing: boolean): void {
    this.isProcessing = processing;
    this.updateIntegrationStatus();
  }
  
  private generateEmailBody(
    customer: CustomerData,
    calculation: CalculatorCalculation,
    salesperson: any
  ): string {
    return `
Dear ${customer.contactName || 'Sir/Madam'},

Thank you for your interest in FreshPlan.

Please find attached your personalized offer with a discount of ${calculation.totalDiscount}%.

Your savings: €${calculation.savingsAmount.toLocaleString('de-DE')}
Final price: €${calculation.finalPrice.toLocaleString('de-DE')}

If you have any questions, please don't hesitate to contact me.

Best regards,
${salesperson.name}
${salesperson.email}
${salesperson.phone || salesperson.mobile || ''}
    `.trim();
  }

  /**
   * Public API
   */
  
  async testAllConnections(): Promise<void> {
    await this.testMondayConnection();
    await this.testEmailConnection();
    await this.testXentralConnection();
  }
  
  getIntegrationStatus(): {
    monday: boolean;
    email: boolean;
    xentral: boolean;
  } {
    const integrations = useStore.getState().settings.integrations;
    
    return {
      monday: !!(integrations.monday.token && integrations.monday.boardId),
      email: !!(integrations.email.smtpServer && integrations.email.smtpEmail),
      xentral: !!(integrations.xentral.url && integrations.xentral.key)
    };
  }

  /**
   * Cleanup
   */
  cleanup(): void {
    // Module cleanup handled by base class
  }
}