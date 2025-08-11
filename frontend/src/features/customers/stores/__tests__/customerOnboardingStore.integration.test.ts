import { describe, it, expect, vi } from 'vitest';
import { renderHook } from '@testing-library/react';
import { useCustomerOnboardingStore } from '../customerOnboardingStore';

// Mock the API
vi.mock('../../services/contactApi', () => ({
  contactApi: {
    createContact: vi.fn(),
    updateContact: vi.fn(),
    deleteContact: vi.fn(),
    getContacts: vi.fn(),
    setPrimaryContact: vi.fn(),
    searchContacts: vi.fn(),
    bulkUpdateContacts: vi.fn(),
  },
}));

describe.skip('Customer Onboarding Store - Contact Integration', () => {
  beforeEach(() => {
    // Reset store state
    act(() => {
      useCustomerOnboardingStore.setState({
        contacts: [],
        primaryContactId: undefined,
        contactValidationErrors: {},
      });
    });
    vi.clearAllMocks();
  });

  describe.skip('Contact CRUD Operations', () => {
    it('should add first contact as primary', () => {
      const { result } = renderHook(() => useCustomerOnboardingStore());

      const newContact: CreateContactDTO = {
        firstName: 'Max',
        lastName: 'Mustermann',
        email: 'max@example.com',
      };

      act(() => {
        result.current.addContact(newContact);
      });

      expect(result.current.contacts).toHaveLength(1);
      expect(result.current.contacts[0]).toMatchObject({
        ...newContact,
        isPrimary: true,
        isActive: true,
        responsibilityScope: 'all',
      });
      expect(result.current.primaryContactId).toBe(result.current.contacts[0].id);
    });

    it('should add subsequent contacts as non-primary', () => {
      const { result } = renderHook(() => useCustomerOnboardingStore());

      // Add first contact
      act(() => {
        result.current.addContact({
          firstName: 'Max',
          lastName: 'Mustermann',
          email: 'max@example.com',
        });
      });

      // Add second contact
      act(() => {
        result.current.addContact({
          firstName: 'Maria',
          lastName: 'Musterfrau',
          email: 'maria@example.com',
        });
      });

      expect(result.current.contacts).toHaveLength(2);
      expect(result.current.contacts[0].isPrimary).toBe(true);
      expect(result.current.contacts[1].isPrimary).toBe(false);
    });

    it('should update contact correctly', () => {
      const { result } = renderHook(() => useCustomerOnboardingStore());

      // Add contact
      act(() => {
        result.current.addContact({
          firstName: 'Max',
          lastName: 'Mustermann',
          email: 'max@example.com',
        });
      });

      const contactId = result.current.contacts[0].id;

      // Update contact
      act(() => {
        result.current.updateContact(contactId, {
          position: 'CEO',
          phone: '+49 30 12345678',
        });
      });

      expect(result.current.contacts[0]).toMatchObject({
        firstName: 'Max',
        lastName: 'Mustermann',
        email: 'max@example.com',
        position: 'CEO',
        phone: '+49 30 12345678',
      });
    });

    it('should remove contact and handle primary reassignment', () => {
      const { result } = renderHook(() => useCustomerOnboardingStore());

      // Add multiple contacts
      act(() => {
        result.current.addContact({ firstName: 'Max', lastName: 'Mustermann' });
        result.current.addContact({ firstName: 'Maria', lastName: 'Musterfrau' });
        result.current.addContact({ firstName: 'Peter', lastName: 'Schmidt' });
      });

      const primaryId = result.current.contacts[0].id;

      // Remove primary contact
      act(() => {
        result.current.removeContact(primaryId);
      });

      expect(result.current.contacts).toHaveLength(2);
      // Next contact should become primary
      expect(result.current.contacts[0].isPrimary).toBe(true);
      expect(result.current.primaryContactId).toBe(result.current.contacts[0].id);
    });

    it('should handle removing last contact', () => {
      const { result } = renderHook(() => useCustomerOnboardingStore());

      // Add single contact
      act(() => {
        result.current.addContact({ firstName: 'Max', lastName: 'Mustermann' });
      });

      const contactId = result.current.contacts[0].id;

      // Remove last contact
      act(() => {
        result.current.removeContact(contactId);
      });

      expect(result.current.contacts).toHaveLength(0);
      expect(result.current.primaryContactId).toBeUndefined();
    });
  });

  describe.skip('Primary Contact Management', () => {
    it('should change primary contact correctly', () => {
      const { result } = renderHook(() => useCustomerOnboardingStore());

      // Add multiple contacts
      act(() => {
        result.current.addContact({ firstName: 'Max', lastName: 'Mustermann' });
        result.current.addContact({ firstName: 'Maria', lastName: 'Musterfrau' });
        result.current.addContact({ firstName: 'Peter', lastName: 'Schmidt' });
      });

      const newPrimaryId = result.current.contacts[2].id;

      // Set new primary
      act(() => {
        result.current.setPrimaryContact(newPrimaryId);
      });

      expect(result.current.contacts[0].isPrimary).toBe(false);
      expect(result.current.contacts[1].isPrimary).toBe(false);
      expect(result.current.contacts[2].isPrimary).toBe(true);
      expect(result.current.primaryContactId).toBe(newPrimaryId);
    });
  });

  describe.skip('Contact Validation', () => {
    it('should validate required fields', async () => {
      const { result } = renderHook(() => useCustomerOnboardingStore());

      // Add contact with missing required fields
      act(() => {
        result.current.addContact({
          firstName: '',
          lastName: '',
        });
      });

      const contactId = result.current.contacts[0].id;

      // Validate specific field
      act(() => {
        result.current.validateContactField(contactId, 'firstName', '');
      });

      expect(result.current.contactValidationErrors[contactId]).toBeDefined();
      expect(result.current.contactValidationErrors[contactId].firstName).toBe(
        'Vorname ist erforderlich'
      );
    });

    it('should validate email format', () => {
      const { result } = renderHook(() => useCustomerOnboardingStore());

      // Add contact
      act(() => {
        result.current.addContact({
          firstName: 'Max',
          lastName: 'Mustermann',
          email: 'invalid-email',
        });
      });

      const contactId = result.current.contacts[0].id;

      // Validate email
      act(() => {
        result.current.validateContactField(contactId, 'email', 'invalid-email');
      });

      expect(result.current.contactValidationErrors[contactId]?.email).toBe(
        'Ungültige E-Mail-Adresse'
      );
    });

    it('should clear validation errors on valid input', () => {
      const { result } = renderHook(() => useCustomerOnboardingStore());

      // Add contact with invalid email
      act(() => {
        result.current.addContact({
          firstName: 'Max',
          lastName: 'Mustermann',
          email: 'invalid',
        });
      });

      const contactId = result.current.contacts[0].id;

      // First validate with invalid email
      act(() => {
        result.current.validateContactField(contactId, 'email', 'invalid');
      });

      expect(result.current.contactValidationErrors[contactId]?.email).toBeDefined();

      // Then validate with valid email
      act(() => {
        result.current.validateContactField(contactId, 'email', 'max@example.com');
      });

      expect(result.current.contactValidationErrors[contactId]?.email).toBeUndefined();
    });

    it('should validate all contacts', async () => {
      const { result } = renderHook(() => useCustomerOnboardingStore());

      // Add valid and invalid contacts
      act(() => {
        result.current.addContact({
          firstName: 'Max',
          lastName: 'Mustermann',
          email: 'max@example.com',
        });
        result.current.addContact({
          firstName: '',
          lastName: 'Musterfrau',
          email: 'invalid-email',
        });
      });

      // Validate all contacts
      let isValid: boolean;
      await act(async () => {
        isValid = await result.current.validateContacts();
      });

      expect(isValid!).toBe(false);
      expect(Object.keys(result.current.contactValidationErrors)).toHaveLength(1);
    });
  });

  describe.skip('Contact Retrieval', () => {
    it('should get contact by id', () => {
      const { result } = renderHook(() => useCustomerOnboardingStore());

      // Add contacts
      act(() => {
        result.current.addContact({ firstName: 'Max', lastName: 'Mustermann' });
        result.current.addContact({ firstName: 'Maria', lastName: 'Musterfrau' });
      });

      const contactId = result.current.contacts[1].id;

      // Get contact
      let contact: Contact | undefined;
      act(() => {
        contact = result.current.getContact(contactId);
      });

      expect(contact).toBeDefined();
      expect(contact?.firstName).toBe('Maria');
    });

    it('should return undefined for non-existent contact', () => {
      const { result } = renderHook(() => useCustomerOnboardingStore());

      let contact: Contact | undefined;
      act(() => {
        contact = result.current.getContact('non-existent-id');
      });

      expect(contact).toBeUndefined();
    });
  });

  describe.skip('Location Assignment', () => {
    it('should handle location assignments correctly', () => {
      const { result } = renderHook(() => useCustomerOnboardingStore());

      // Add contact with specific location responsibility
      act(() => {
        result.current.addContact({
          firstName: 'Max',
          lastName: 'Mustermann',
          responsibilityScope: 'specific',
          assignedLocationIds: ['loc-1', 'loc-2'],
        });
      });

      expect(result.current.contacts[0]).toMatchObject({
        responsibilityScope: 'specific',
        assignedLocationIds: ['loc-1', 'loc-2'],
      });

      // Update location assignments
      act(() => {
        result.current.updateContact(result.current.contacts[0].id, {
          assignedLocationIds: ['loc-1', 'loc-3', 'loc-4'],
        });
      });

      expect(result.current.contacts[0].assignedLocationIds).toEqual(['loc-1', 'loc-3', 'loc-4']);
    });
  });

  describe.skip('Bulk Operations', () => {
    it('should handle bulk status updates', () => {
      const { result } = renderHook(() => useCustomerOnboardingStore());

      // Add multiple contacts
      act(() => {
        result.current.addContact({ firstName: 'Max', lastName: 'Mustermann' });
        result.current.addContact({ firstName: 'Maria', lastName: 'Musterfrau' });
        result.current.addContact({ firstName: 'Peter', lastName: 'Schmidt' });
      });

      const contactIds = result.current.contacts.map(c => c.id);

      // Bulk update to inactive
      act(() => {
        contactIds.forEach(id => {
          result.current.updateContact(id, { isActive: false });
        });
      });

      expect(result.current.contacts.every(c => !c.isActive)).toBe(true);
    });
  });

  describe.skip('State Persistence', () => {
    it('should maintain contact state across re-renders', () => {
      const { result, rerender } = renderHook(() => useCustomerOnboardingStore());

      // Add contacts
      act(() => {
        result.current.addContact({ firstName: 'Max', lastName: 'Mustermann' });
        result.current.addContact({ firstName: 'Maria', lastName: 'Musterfrau' });
      });

      const initialContacts = result.current.contacts;

      // Re-render
      rerender();

      expect(result.current.contacts).toEqual(initialContacts);
      expect(result.current.contacts).toHaveLength(2);
    });
  });

  describe.skip('Edge Cases', () => {
    it('should handle empty contact creation gracefully', () => {
      const { result } = renderHook(() => useCustomerOnboardingStore());

      // Add contact without any data
      act(() => {
        result.current.addContact();
      });

      expect(result.current.contacts).toHaveLength(1);
      expect(result.current.contacts[0]).toMatchObject({
        firstName: '',
        lastName: '',
        isPrimary: true,
        isActive: true,
        responsibilityScope: 'all',
      });
    });

    it('should handle concurrent updates correctly', () => {
      const { result } = renderHook(() => useCustomerOnboardingStore());

      // Add contact
      act(() => {
        result.current.addContact({ firstName: 'Max', lastName: 'Mustermann' });
      });

      const contactId = result.current.contacts[0].id;

      // Simulate concurrent updates
      act(() => {
        result.current.updateContact(contactId, { position: 'CEO' });
        result.current.updateContact(contactId, { phone: '+49 30 12345678' });
        result.current.updateContact(contactId, { email: 'max@example.com' });
      });

      // All updates should be applied
      expect(result.current.contacts[0]).toMatchObject({
        position: 'CEO',
        phone: '+49 30 12345678',
        email: 'max@example.com',
      });
    });
  });
});
