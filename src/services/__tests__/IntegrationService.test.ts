/**
 * Integration Service Tests with MSW
 */

import { describe, it, expect, beforeEach, vi } from 'vitest';
import { IntegrationService } from '../IntegrationService';
import { useStore } from '@/store';
import type { CustomerData, CalculatorCalculation } from '@/types';

// Dynamic import for test-only modules
const getMockHelpers = async () => {
  const { server, resetHandlers, useHandlers } = await import('@/mocks/__tests__/server');
  const { errorHandlers, delayHandlers } = await import('@/mocks/handlers');
  return { server, resetHandlers, useHandlers, errorHandlers, delayHandlers };
};

describe('IntegrationService', () => {
  let service: IntegrationService;
  let testCustomer: CustomerData;
  let testCalculation: CalculatorCalculation;

  beforeEach(() => {
    service = new IntegrationService();
    
    // Reset store
    useStore.getState().reset();
    
    // Set up test data
    testCustomer = {
      companyName: 'Test GmbH',
      contactName: 'Max Mustermann',
      contactEmail: 'test@example.com',
      contactPhone: '+49 123 456789',
      customerType: 'single',
      industry: 'retail',
      address: {
        street: 'Test Street 123',
        zip: '12345',
        city: 'Test City',
        country: 'DE'
      }
    };
    
    testCalculation = {
      orderValue: 20000,
      leadTime: 30,
      pickup: true,
      chain: false,
      baseDiscount: 8,
      earlyDiscount: 3,
      pickupDiscount: 2,
      chainDiscount: 0,
      totalDiscount: 13,
      discountAmount: 2600,
      finalPrice: 17400,
      savingsAmount: 2600
    };
  });

  describe('Monday.com Integration', () => {
    beforeEach(() => {
      // Configure Monday settings
      useStore.getState().updateIntegration('monday', {
        token: 'test-token',
        boardId: 'test-board-id'
      });
    });

    it('should create a lead successfully', async () => {
      const result = await service.createMondayLead(testCustomer, testCalculation);
      
      expect(result.success).toBe(true);
      expect(result.data).toMatchObject({
        id: '123456',
        name: 'Test Customer'
      });
    });

    it('should handle authentication errors', async () => {
      useHandlers(...errorHandlers.monday);
      
      const result = await service.createMondayLead(testCustomer, testCalculation);
      
      expect(result.success).toBe(false);
      expect(result.error).toContain('Invalid API token');
    });

    it('should handle missing configuration', async () => {
      useStore.getState().updateIntegration('monday', {
        token: '',
        boardId: ''
      });
      
      const result = await service.createMondayLead(testCustomer, testCalculation);
      
      expect(result.success).toBe(false);
      expect(result.error).toBe('Monday.com integration not configured');
    });

    it('should get boards list', async () => {
      const result = await service.getMondayBoards();
      
      expect(result.success).toBe(true);
      expect(result.data).toHaveLength(1);
      expect(result.data[0]).toMatchObject({
        id: 'test-board-id',
        name: 'Test Board'
      });
    });

    it('should handle network delays gracefully', async () => {
      useHandlers(...delayHandlers.monday);
      
      const startTime = Date.now();
      const result = await service.createMondayLead(testCustomer, testCalculation);
      const duration = Date.now() - startTime;
      
      expect(result.success).toBe(true);
      expect(duration).toBeGreaterThanOrEqual(2000);
    });
  });

  describe('Email Integration', () => {
    beforeEach(() => {
      // Configure email settings
      useStore.getState().updateIntegration('email', {
        smtpServer: 'smtp.test.com',
        smtpEmail: 'sender@test.com',
        smtpPassword: 'test-password'
      });
    });

    it('should send email successfully', async () => {
      const message = {
        to: testCustomer.contactEmail!,
        subject: 'Test Email',
        body: 'This is a test email'
      };
      
      const result = await service.sendEmail(message);
      
      expect(result.success).toBe(true);
      expect(result.data).toMatchObject({
        success: true,
        recipient: 'test@example.com'
      });
    });

    it('should handle SMTP errors', async () => {
      useHandlers(...errorHandlers.email);
      
      const message = {
        to: testCustomer.contactEmail!,
        subject: 'Test Email',
        body: 'This is a test email'
      };
      
      const result = await service.sendEmail(message);
      
      expect(result.success).toBe(false);
      expect(result.error).toContain('SMTP connection failed');
    });

    it('should validate email settings', async () => {
      const result = await service.validateEmailSettings();
      
      expect(result.success).toBe(true);
      expect(result.data.message).toBe('SMTP configuration is valid');
    });

    it('should handle missing email configuration', async () => {
      useStore.getState().updateIntegration('email', {
        smtpServer: '',
        smtpEmail: '',
        smtpPassword: ''
      });
      
      const message = {
        to: 'test@example.com',
        subject: 'Test',
        body: 'Test'
      };
      
      const result = await service.sendEmail(message);
      
      expect(result.success).toBe(false);
      expect(result.error).toBe('Email integration not configured');
    });
  });

  describe('Xentral Integration', () => {
    beforeEach(() => {
      // Configure Xentral settings
      useStore.getState().updateIntegration('xentral', {
        url: 'https://test.xentral.com',
        key: 'test-api-key'
      });
    });

    it('should create customer successfully', async () => {
      const result = await service.createXentralCustomer(testCustomer);
      
      expect(result.success).toBe(true);
      expect(result.data).toMatchObject({
        id: 12345,
        kundennummer: 'CUST-12345',
        name: 'Test GmbH'
      });
    });

    it('should create offer successfully', async () => {
      const customerId = 12345;
      const result = await service.createXentralOffer(customerId, testCalculation);
      
      expect(result.success).toBe(true);
      expect(result.data).toMatchObject({
        id: 67890,
        belegnr: 'OFFER-67890',
        kunde: customerId
      });
    });

    it('should get customer details', async () => {
      const customerId = 12345;
      const result = await service.getXentralCustomer(customerId);
      
      expect(result.success).toBe(true);
      expect(result.data).toMatchObject({
        id: customerId,
        kundennummer: `CUST-${customerId}`
      });
    });

    it('should handle API key errors', async () => {
      useHandlers(...errorHandlers.xentral);
      
      const result = await service.createXentralCustomer(testCustomer);
      
      expect(result.success).toBe(false);
      expect(result.error).toContain('Invalid API key');
    });

    it('should handle missing Xentral configuration', async () => {
      useStore.getState().updateIntegration('xentral', {
        url: '',
        key: ''
      });
      
      const result = await service.createXentralCustomer(testCustomer);
      
      expect(result.success).toBe(false);
      expect(result.error).toBe('Xentral integration not configured');
    });
  });

  describe('Combined Workflow', () => {
    beforeEach(() => {
      // Configure all integrations
      useStore.getState().updateIntegration('monday', {
        token: 'test-token',
        boardId: 'test-board-id'
      });
      
      useStore.getState().updateIntegration('email', {
        smtpServer: 'smtp.test.com',
        smtpEmail: 'sender@test.com',
        smtpPassword: 'test-password'
      });
      
      useStore.getState().updateIntegration('xentral', {
        url: 'https://test.xentral.com',
        key: 'test-api-key'
      });
      
      useStore.getState().updateSalesperson({
        name: 'John Doe',
        email: 'john@example.com',
        phone: '+49 123 456789'
      });
    });

    it('should create lead in all systems', async () => {
      const results = await service.createLeadInAllSystems(
        testCustomer,
        testCalculation
      );
      
      expect(results.monday?.success).toBe(true);
      expect(results.email?.success).toBe(true);
      expect(results.xentral?.success).toBe(true);
    });

    it('should handle partial failures gracefully', async () => {
      // Make Monday fail
      useHandlers(...errorHandlers.monday);
      
      const results = await service.createLeadInAllSystems(
        testCustomer,
        testCalculation
      );
      
      expect(results.monday?.success).toBe(false);
      expect(results.email?.success).toBe(true);
      expect(results.xentral?.success).toBe(true);
    });

    it('should skip unconfigured integrations', async () => {
      // Remove Monday configuration
      useStore.getState().updateIntegration('monday', {
        token: '',
        boardId: ''
      });
      
      const results = await service.createLeadInAllSystems(
        testCustomer,
        testCalculation
      );
      
      expect(results.monday).toBeUndefined();
      expect(results.email?.success).toBe(true);
      expect(results.xentral?.success).toBe(true);
    });

    it('should include PDF attachment when provided', async () => {
      const pdfData = 'base64-encoded-pdf-data';
      
      const results = await service.createLeadInAllSystems(
        testCustomer,
        testCalculation,
        pdfData
      );
      
      expect(results.email?.success).toBe(true);
    });

    it('should handle concurrent API calls efficiently', async () => {
      const startTime = Date.now();
      
      const results = await service.createLeadInAllSystems(
        testCustomer,
        testCalculation
      );
      
      const duration = Date.now() - startTime;
      
      // All calls should happen in parallel, not sequentially
      expect(duration).toBeLessThan(1000);
      
      expect(results.monday?.success).toBe(true);
      expect(results.email?.success).toBe(true);
      expect(results.xentral?.success).toBe(true);
    });
  });

  describe('Error Handling', () => {
    it('should include timestamps in all responses', async () => {
      const result = await service.createMondayLead(testCustomer, testCalculation);
      
      expect(result.timestamp).toBeDefined();
      expect(new Date(result.timestamp).toString()).not.toBe('Invalid Date');
    });

    it('should handle network timeouts', async () => {
      // Configure Monday settings first
      useStore.getState().updateIntegration('monday', {
        token: 'test-token',
        boardId: 'test-board-id'
      });
      
      // Mock fetch to timeout
      const originalFetch = global.fetch;
      global.fetch = vi.fn().mockRejectedValue(new Error('Network timeout'));
      
      const result = await service.createMondayLead(testCustomer, testCalculation);
      
      expect(result.success).toBe(false);
      expect(result.error).toContain('Network timeout');
      
      global.fetch = originalFetch;
    });

    it('should handle invalid JSON responses', async () => {
      // Configure Monday settings first
      useStore.getState().updateIntegration('monday', {
        token: 'test-token',
        boardId: 'test-board-id'
      });
      
      // Mock invalid response
      const originalFetch = global.fetch;
      global.fetch = vi.fn().mockResolvedValue({
        json: () => Promise.reject(new Error('Invalid JSON'))
      });
      
      const result = await service.createMondayLead(testCustomer, testCalculation);
      
      expect(result.success).toBe(false);
      expect(result.error).toContain('Invalid JSON');
      
      global.fetch = originalFetch;
    });
  });
});