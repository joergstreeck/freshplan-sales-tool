/**
 * Contact API Service
 *
 * Handles all API communication for contact management.
 * Implements CRUD operations matching the backend ContactResource.
 *
 * @see /Users/joergstreeck/freshplan-sales-tool/backend/src/main/java/de/freshplan/api/resources/ContactResource.java
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/README.md
 */

import { apiClient } from './api-client';
import type { Contact, CreateContactDTO, UpdateContactDTO } from '../types/contact.types';
import type { ContactInteractionDTO } from '../types/intelligence.types';

/**
 * Contact API Service
 */
export const contactApi = {
  /**
   * Get all contacts for a customer
   */
  getContacts: async (customerId: string): Promise<Contact[]> => {
    const response = await apiClient.get(`/customers/${customerId}/contacts`);
    return response.data;
  },

  /**
   * Get a single contact
   */
  getContact: async (customerId: string, contactId: string): Promise<Contact> => {
    const response = await apiClient.get(`/customers/${customerId}/contacts/${contactId}`);
    return response.data;
  },

  /**
   * Create a new contact
   */
  createContact: async (customerId: string, contact: CreateContactDTO): Promise<Contact> => {
    const response = await apiClient.post(`/customers/${customerId}/contacts`, contact);
    return response.data;
  },

  /**
   * Update an existing contact
   */
  updateContact: async (
    customerId: string,
    contactId: string,
    updates: UpdateContactDTO
  ): Promise<Contact> => {
    const response = await apiClient.put(`/customers/${customerId}/contacts/${contactId}`, updates);
    return response.data;
  },

  /**
   * Soft delete a contact
   * Contact remains in database with isActive = false
   */
  deleteContact: async (customerId: string, contactId: string, reason?: string): Promise<void> => {
    await apiClient.delete(`/customers/${customerId}/contacts/${contactId}`, {
      params: { reason },
    });
  },

  /**
   * Set a contact as primary
   * Backend ensures only one primary contact per customer
   */
  setPrimaryContact: async (customerId: string, contactId: string): Promise<Contact> => {
    const response = await apiClient.put(
      `/customers/${customerId}/contacts/${contactId}/set-primary`
    );
    return response.data;
  },

  /**
   * Bulk create contacts
   * Useful for import scenarios
   */
  createContacts: async (customerId: string, contacts: CreateContactDTO[]): Promise<Contact[]> => {
    const response = await apiClient.post(`/customers/${customerId}/contacts/bulk`, contacts);
    return response.data;
  },

  /**
   * Get contacts by location
   * Returns contacts assigned to a specific location
   */
  getContactsByLocation: async (customerId: string, locationId: string): Promise<Contact[]> => {
    const response = await apiClient.get(
      `/customers/${customerId}/locations/${locationId}/contacts`
    );
    return response.data;
  },

  /**
   * Assign contact to locations
   * Updates the location assignments for a contact
   */
  assignContactToLocations: async (
    customerId: string,
    contactId: string,
    locationIds: string[]
  ): Promise<Contact> => {
    const response = await apiClient.put(
      `/customers/${customerId}/contacts/${contactId}/locations`,
      { locationIds }
    );
    return response.data;
  },

  /**
   * Search contacts
   * Searches across all customer contacts
   */
  searchContacts: async (customerId: string, query: string): Promise<Contact[]> => {
    const response = await apiClient.get(`/customers/${customerId}/contacts/search`, {
      params: { q: query },
    });
    return response.data;
  },

  /**
   * Get contact activity timeline
   * Returns all interactions with a contact
   */
  getContactTimeline: async (
    customerId: string,
    contactId: string
  ): Promise<ContactInteractionDTO[]> => {
    const response = await apiClient.get(`/customers/${customerId}/contacts/${contactId}/timeline`);
    return response.data;
  },

  /**
   * Export contacts
   * Returns contacts in specified format (CSV, Excel, etc.)
   */
  exportContacts: async (
    customerId: string,
    format: 'csv' | 'excel' | 'json' = 'csv'
  ): Promise<Blob> => {
    const response = await apiClient.get(`/customers/${customerId}/contacts/export`, {
      params: { format },
      responseType: 'blob',
    });
    return response.data;
  },
};

/**
 * Contact validation helpers
 */
export const contactValidation = {
  /**
   * Validate email format
   */
  isValidEmail: (email: string): boolean => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  },

  /**
   * Validate phone number format (German)
   */
  isValidPhone: (phone: string): boolean => {
    // Remove all non-numeric characters for validation
    const cleanPhone = phone.replace(/[\s\-()+/]/g, '');
    // German phone numbers: landline (3-5 area + 3-8 number) or mobile (15-17 digits with country)
    return /^\d{7,17}$/.test(cleanPhone);
  },

  /**
   * Format phone number for display
   */
  formatPhone: (phone: string): string => {
    const clean = phone.replace(/\D/g, '');

    // German mobile number
    if (
      clean.startsWith('49') &&
      (clean.startsWith('4915') || clean.startsWith('4916') || clean.startsWith('4917'))
    ) {
      return clean.replace(/^(\d{2})(\d{3})(\d{4})(\d+)/, '+$1 $2 $3 $4');
    }

    // German landline
    if (clean.startsWith('49')) {
      return clean.replace(/^(\d{2})(\d{2,5})(\d+)/, '+$1 $2 $3');
    }

    // Local format
    if (clean.startsWith('0')) {
      return clean.replace(/^(\d{3,5})(\d+)/, '$1 $2');
    }

    return phone;
  },

  /**
   * Check if contact has minimum required data
   */
  hasMinimumData: (contact: Partial<CreateContactDTO>): boolean => {
    return !!(contact.firstName && contact.lastName);
  },

  /**
   * Check if contact has any contact method
   */
  hasContactMethod: (contact: Partial<CreateContactDTO>): boolean => {
    return !!(contact.email || contact.phone || contact.mobile);
  },
};

/**
 * Contact role helpers
 */
export const contactRoleHelpers = {
  /**
   * Get display name for role
   */
  getRoleDisplayName: (role: string): string => {
    const roleMap: Record<string, string> = {
      geschaeftsfuehrung: 'Geschäftsführung',
      einkauf: 'Einkauf',
      kueche: 'Küche',
      verwaltung: 'Verwaltung',
      qualitaet: 'Qualitätsmanagement',
      buchhaltung: 'Buchhaltung',
      marketing: 'Marketing',
      personal: 'Personal',
      technik: 'Technik',
      sonstiges: 'Sonstiges',
    };
    return roleMap[role] || role;
  },

  /**
   * Get icon for role
   */
  getRoleIcon: (role: string): string => {
    const iconMap: Record<string, string> = {
      geschaeftsfuehrung: '👔',
      einkauf: '🛒',
      kueche: '👨‍🍳',
      verwaltung: '📋',
      qualitaet: '✅',
      buchhaltung: '💰',
      marketing: '📢',
      personal: '👥',
      technik: '🔧',
      sonstiges: '📌',
    };
    return iconMap[role] || '📌';
  },
};
