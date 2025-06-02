/**
 * Integration Service
 * Handles external API integrations with flexible store support
 */

import type { CustomerData, CalculatorCalculation } from '@/types';
import { useStore } from '@/store';
import type { StoreState } from '@/store/createStore';

export interface IntegrationResult {
  success: boolean;
  data?: any;
  error?: string;
  timestamp: string;
}

export interface MondayItem {
  id: string;
  name: string;
  created_at: string;
  column_values: any[];
}

export interface EmailMessage {
  to: string;
  subject: string;
  body: string;
  attachments?: any[];
}

export interface XentralCustomer {
  id: number;
  kundennummer: string;
  name: string;
  email: string;
  strasse?: string;
  plz?: string;
  ort?: string;
}

export class IntegrationService {
  private getStore: () => StoreState;

  constructor(store?: () => StoreState) {
    // Allow injection of store getter for testing
    this.getStore = store || (() => useStore.getState());
  }

  /**
   * Monday.com Integration
   */
  async createMondayLead(
    customer: CustomerData,
    calculation: CalculatorCalculation
  ): Promise<IntegrationResult> {
    const settings = this.getStore().settings.integrations.monday;
    
    if (!settings.token || !settings.boardId) {
      return {
        success: false,
        error: 'Monday.com integration not configured',
        timestamp: new Date().toISOString()
      };
    }

    try {
      const response = await fetch('https://api.monday.com/v2', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': settings.token,
          'API-Version': '2023-10'
        },
        body: JSON.stringify({
          query: `
            mutation ($boardId: ID!, $itemName: String!, $columnValues: JSON!) {
              create_item (
                board_id: $boardId,
                item_name: $itemName,
                column_values: $columnValues
              ) {
                id
                name
                created_at
                column_values {
                  id
                  value
                }
              }
            }
          `,
          variables: {
            boardId: settings.boardId,
            itemName: customer.companyName,
            columnValues: JSON.stringify({
              email: customer.contactEmail,
              phone: customer.contactPhone,
              discount: String(calculation.totalDiscount),
              value: String(calculation.orderValue),
              status: 'new'
            })
          }
        })
      });

      const result = await response.json();
      
      if (result.errors) {
        throw new Error(result.errors[0].message);
      }

      return {
        success: true,
        data: result.data.create_item,
        timestamp: new Date().toISOString()
      };
    } catch (error) {
      return {
        success: false,
        error: error instanceof Error ? error.message : 'Unknown error occurred',
        timestamp: new Date().toISOString()
      };
    }
  }

  async getMondayBoards(): Promise<IntegrationResult> {
    const settings = this.getStore().settings.integrations.monday;
    
    if (!settings.token) {
      return {
        success: false,
        error: 'Monday.com token not configured',
        timestamp: new Date().toISOString()
      };
    }

    try {
      const response = await fetch('https://api.monday.com/v2', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': settings.token,
          'API-Version': '2023-10'
        },
        body: JSON.stringify({
          query: `
            query {
              boards {
                id
                name
                columns {
                  id
                  title
                }
              }
            }
          `
        })
      });

      const result = await response.json();
      
      if (result.errors) {
        throw new Error(result.errors[0].message);
      }

      return {
        success: true,
        data: result.data.boards,
        timestamp: new Date().toISOString()
      };
    } catch (error) {
      return {
        success: false,
        error: error instanceof Error ? error.message : 'Unknown error occurred',
        timestamp: new Date().toISOString()
      };
    }
  }

  /**
   * Email Integration
   */
  async sendEmail(message: EmailMessage): Promise<IntegrationResult> {
    const settings = this.getStore().settings.integrations.email;
    
    if (!settings.smtpServer || !settings.smtpEmail || !settings.smtpPassword) {
      return {
        success: false,
        error: 'Email integration not configured',
        timestamp: new Date().toISOString()
      };
    }

    try {
      const response = await fetch('/api/email/send', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          ...message,
          from: settings.smtpEmail,
          smtp: {
            server: settings.smtpServer,
            email: settings.smtpEmail,
            password: settings.smtpPassword
          }
        })
      });

      const result = await response.json();
      
      if (!result.success) {
        throw new Error(result.error || 'Failed to send email');
      }

      return {
        success: true,
        data: result,
        timestamp: new Date().toISOString()
      };
    } catch (error) {
      return {
        success: false,
        error: error instanceof Error ? error.message : 'Unknown error occurred',
        timestamp: new Date().toISOString()
      };
    }
  }

  async validateEmailSettings(): Promise<IntegrationResult> {
    const settings = this.getStore().settings.integrations.email;
    
    try {
      const response = await fetch('/api/email/validate', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          smtpServer: settings.smtpServer,
          smtpEmail: settings.smtpEmail,
          smtpPassword: settings.smtpPassword
        })
      });

      const result = await response.json();
      
      return {
        success: result.success,
        data: result,
        error: result.error,
        timestamp: new Date().toISOString()
      };
    } catch (error) {
      return {
        success: false,
        error: error instanceof Error ? error.message : 'Unknown error occurred',
        timestamp: new Date().toISOString()
      };
    }
  }

  /**
   * Xentral Integration
   */
  async createXentralCustomer(customer: CustomerData): Promise<IntegrationResult> {
    const settings = this.getStore().settings.integrations.xentral;
    
    if (!settings.url || !settings.key) {
      return {
        success: false,
        error: 'Xentral integration not configured',
        timestamp: new Date().toISOString()
      };
    }

    try {
      const response = await fetch(`${settings.url}/api/v1/adressen`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${settings.key}`
        },
        body: JSON.stringify({
          name: customer.companyName,
          email: customer.contactEmail,
          telefon: customer.contactPhone,
          strasse: customer.address?.street || customer.street,
          plz: customer.address?.zip || customer.zipCode,
          ort: customer.address?.city || customer.city,
          land: customer.address?.country || 'DE',
          typ: 'kunde'
        })
      });

      const result = await response.json();
      
      if (!result.success) {
        throw new Error(result.error || 'Failed to create customer');
      }

      return {
        success: true,
        data: result.data,
        timestamp: new Date().toISOString()
      };
    } catch (error) {
      return {
        success: false,
        error: error instanceof Error ? error.message : 'Unknown error occurred',
        timestamp: new Date().toISOString()
      };
    }
  }

  async createXentralOffer(
    customerId: number,
    calculation: CalculatorCalculation
  ): Promise<IntegrationResult> {
    const settings = this.getStore().settings.integrations.xentral;
    
    if (!settings.url || !settings.key) {
      return {
        success: false,
        error: 'Xentral integration not configured',
        timestamp: new Date().toISOString()
      };
    }

    try {
      const response = await fetch(`${settings.url}/api/v1/belege/angebote`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${settings.key}`
        },
        body: JSON.stringify({
          adresse: customerId,
          datum: new Date().toISOString().split('T')[0],
          status: 'angelegt',
          summe: calculation.finalPrice,
          rabatt: calculation.totalDiscount,
          positionen: [{
            artikel: 'FRESHPLAN',
            bezeichnung: 'FreshPlan Service',
            menge: 1,
            preis: calculation.orderValue,
            rabatt: calculation.totalDiscount
          }]
        })
      });

      const result = await response.json();
      
      if (!result.success) {
        throw new Error(result.error || 'Failed to create offer');
      }

      return {
        success: true,
        data: result.data,
        timestamp: new Date().toISOString()
      };
    } catch (error) {
      return {
        success: false,
        error: error instanceof Error ? error.message : 'Unknown error occurred',
        timestamp: new Date().toISOString()
      };
    }
  }

  async getXentralCustomer(id: number): Promise<IntegrationResult> {
    const settings = this.getStore().settings.integrations.xentral;
    
    if (!settings.url || !settings.key) {
      return {
        success: false,
        error: 'Xentral integration not configured',
        timestamp: new Date().toISOString()
      };
    }

    try {
      const response = await fetch(`${settings.url}/api/v1/adressen/${id}`, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${settings.key}`
        }
      });

      const result = await response.json();
      
      if (!result.success) {
        throw new Error(result.error || 'Customer not found');
      }

      return {
        success: true,
        data: result.data,
        timestamp: new Date().toISOString()
      };
    } catch (error) {
      return {
        success: false,
        error: error instanceof Error ? error.message : 'Unknown error occurred',
        timestamp: new Date().toISOString()
      };
    }
  }

  /**
   * Combined workflow
   */
  async createLeadInAllSystems(
    customer: CustomerData,
    calculation: CalculatorCalculation,
    pdfData?: any
  ): Promise<{
    monday?: IntegrationResult;
    email?: IntegrationResult;
    xentral?: IntegrationResult;
  }> {
    const results: any = {};

    // Create Monday.com lead
    const mondaySettings = this.getStore().settings.integrations.monday;
    if (mondaySettings.token && mondaySettings.boardId) {
      results.monday = await this.createMondayLead(customer, calculation);
    }

    // Create Xentral customer and offer
    const xentralSettings = this.getStore().settings.integrations.xentral;
    if (xentralSettings.url && xentralSettings.key) {
      const customerResult = await this.createXentralCustomer(customer);
      if (customerResult.success && customerResult.data?.id) {
        results.xentral = await this.createXentralOffer(
          customerResult.data.id,
          calculation
        );
      } else {
        results.xentral = customerResult;
      }
    }

    // Send email with PDF
    const emailSettings = this.getStore().settings.integrations.email;
    if (emailSettings.smtpServer && customer.contactEmail) {
      const salesperson = this.getStore().settings.salesperson;
      
      results.email = await this.sendEmail({
        to: customer.contactEmail,
        subject: `FreshPlan Angebot für ${customer.companyName}`,
        body: `
          Sehr geehrte Damen und Herren,

          vielen Dank für Ihr Interesse an FreshPlan.
          
          Anbei finden Sie Ihr persönliches Angebot mit einem Rabatt von ${calculation.totalDiscount}%.
          
          Ihre Ersparnis: €${calculation.savingsAmount.toLocaleString('de-DE')}
          Endpreis: €${calculation.finalPrice.toLocaleString('de-DE')}
          
          Bei Fragen stehe ich Ihnen gerne zur Verfügung.
          
          Mit freundlichen Grüßen,
          ${salesperson.name}
          ${salesperson.email}
          ${salesperson.phone || salesperson.mobile || ''}
        `.trim(),
        attachments: pdfData ? [{
          filename: 'FreshPlan_Angebot.pdf',
          content: pdfData,
          encoding: 'base64'
        }] : undefined
      });
    }

    return results;
  }
}