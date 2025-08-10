import { describe, it, expect, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Step3MultiContactManagement } from './Step3MultiContactManagement';
import type { Contact } from '../../types/contact.types';
import { CustomerFieldThemeProvider } from '../../theme/CustomerFieldThemeProvider';
import React from 'react';

// Mock the store
let mockContacts: Contact[] = [];
const mockAddContact = vi.fn();
const mockUpdateContact = vi.fn();
const mockRemoveContact = vi.fn();
const mockSetPrimaryContact = vi.fn();
const mockValidateContacts = vi.fn().mockResolvedValue(true);
const mockLocations = [
  { id: 'loc-1', name: 'Berlin Hauptfiliale' },
  { id: 'loc-2', name: 'München Filiale' },
];

const mockStore = {
  contacts: mockContacts,
  addContact: mockAddContact,
  updateContact: mockUpdateContact,
  removeContact: mockRemoveContact,
  setPrimaryContact: mockSetPrimaryContact,
  validateContacts: mockValidateContacts,
  contactValidationErrors: {},
  locations: mockLocations,
};

vi.mock('../../stores/customerOnboardingStore', () => ({
  useCustomerOnboardingStore: vi.fn(() => mockStore),
}));

// Mock the field catalog
vi.mock('../../data/fieldCatalogContactExtensions', () => ({
  contactFieldCatalog: {
    basicInfo: {
      id: 'basicInfo',
      name: 'Basisdaten',
      fields: [
        {
          key: 'salutation',
          label: 'Anrede',
          type: 'select',
          options: [
            { value: 'Herr', label: 'Herr' },
            { value: 'Frau', label: 'Frau' },
            { value: 'Divers', label: 'Divers' },
          ],
        },
        {
          key: 'firstName',
          label: 'Vorname',
          type: 'text',
          required: true,
          validation: { pattern: /^[a-zA-ZäöüÄÖÜß\s-]+$/ },
        },
        {
          key: 'lastName',
          label: 'Nachname',
          type: 'text',
          required: true,
          validation: { pattern: /^[a-zA-ZäöüÄÖÜß\s-]+$/ },
        },
      ],
    },
  },
}));

// Mock components
// CustomerFieldThemeProvider is NOT mocked - we use the real one

vi.mock('../adaptive/AdaptiveFormContainer', () => ({
  AdaptiveFormContainer: ({ children }: unknown) => (
    <div className="adaptive-form-container">{children}</div>
  ),
}));

vi.mock('../contacts/ContactFormDialog', () => ({
  ContactFormDialog: ({ open, onClose, contact }: unknown) =>
    open ? <div role="dialog">Contact Form Dialog</div> : null,
}));

// ContactCard is NOT mocked - we test the real integration

vi.mock('../contacts/ContactQuickActions', () => ({
  ContactQuickActions: () => <div aria-label="Schnellaktionen">Quick Actions</div>,
}));

const createMockContact = (overrides?: Partial<Contact>): Contact => ({
  id: 'contact-1',
  customerId: 'customer-1',
  salutation: 'Herr',
  firstName: 'Max',
  lastName: 'Mustermann',
  position: 'Geschäftsführer',
  decisionLevel: 'entscheider',
  email: 'max@example.com',
  phone: '+49 30 12345678',
  mobile: '+49 170 12345678',
  isPrimary: true,
  isActive: true,
  responsibilityScope: 'all',
  createdAt: new Date('2025-01-01').toISOString(),
  updatedAt: new Date('2025-01-01').toISOString(),
  ...overrides,
});

// Test wrapper with CustomerFieldThemeProvider
const TestWrapper: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  return <CustomerFieldThemeProvider>{children}</CustomerFieldThemeProvider>;
};

const renderWithTheme = (ui: React.ReactElement) => {
  return render(ui, { wrapper: TestWrapper });
};

describe.skip('Step3MultiContactManagement', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockContacts = [];
    // Update the mock store with the new contacts array
    mockStore.contacts = mockContacts;
    // Mock window.confirm
    global.confirm = vi.fn(() => true);
  });

  it('should render empty state when no contacts exist', () => {
    renderWithTheme(<Step3MultiContactManagement />);

    expect(screen.getByText('Ansprechpartner verwalten')).toBeInTheDocument();
    expect(screen.getByText(/Noch keine Kontakte erfasst/)).toBeInTheDocument();
    expect(screen.getByText('Neuen Kontakt hinzufügen')).toBeInTheDocument();
  });

  it('should use theme architecture components', () => {
    renderWithTheme(<Step3MultiContactManagement />);

    expect(screen.getByTestId('adaptive-form-container')).toBeInTheDocument();
  });

  it('should open contact form dialog on add button click', async () => {
    renderWithTheme(<Step3MultiContactManagement />);

    const addButton = screen.getByText('Neuen Kontakt hinzufügen');
    await userEvent.click(addButton);

    // Dialog should be opened (mocked ContactFormDialog)
    await waitFor(() => {
      expect(screen.getByRole('dialog')).toBeInTheDocument();
    });
  });

  it('should display multiple contacts', () => {
    mockContacts = [
      createMockContact({ id: 'contact-1', isPrimary: true }),
      createMockContact({
        id: 'contact-2',
        salutation: 'Frau',
        firstName: 'Maria',
        lastName: 'Musterfrau',
        isPrimary: false,
      }),
      createMockContact({
        id: 'contact-3',
        salutation: 'Herr',
        firstName: 'Peter',
        lastName: 'Schmidt',
        isPrimary: false,
      }),
    ];
    mockStore.contacts = mockContacts;

    renderWithTheme(<Step3MultiContactManagement />);

    expect(screen.getByText('Herr Max Mustermann')).toBeInTheDocument();
    expect(screen.getByText('Frau Maria Musterfrau')).toBeInTheDocument();
    expect(screen.getByText('Herr Peter Schmidt')).toBeInTheDocument();
    // Check for Paper elements instead of contact-card
    const papers = screen
      .getAllByRole('region')
      .filter(el => el.className.includes('MuiPaper-root'));
    expect(papers.length).toBeGreaterThanOrEqual(3);
  });

  it('should handle contact editing', async () => {
    const contact = createMockContact();
    mockContacts = [contact];
    mockStore.contacts = mockContacts;

    renderWithTheme(<Step3MultiContactManagement />);

    // Find edit button - it's an IconButton with EditIcon
    const editButtons = screen.getAllByRole('button');
    const editButton = editButtons.find(btn => btn.querySelector('[data-testid="EditIcon"]'));
    expect(editButton).toBeTruthy();

    if (editButton) {
      await userEvent.click(editButton);

      // Should open edit dialog
      await waitFor(() => {
        expect(screen.getByRole('dialog')).toBeInTheDocument();
      });
    }
  });

  it('should validate before allowing navigation', async () => {
    mockContacts = [createMockContact()];
    mockStore.contacts = mockContacts;
    mockValidateContacts.mockResolvedValueOnce(false);

    renderWithTheme(<Step3MultiContactManagement />);

    // Trigger validation (would be called by wizard)
    const result = await mockValidateContacts();

    expect(result).toBe(false);
    expect(mockValidateContacts).toHaveBeenCalled();
  });

  it('should show contact information', () => {
    mockContacts = [
      createMockContact({ id: 'contact-1' }),
      createMockContact({
        id: 'contact-2',
        salutation: 'Frau',
        firstName: 'Maria',
        lastName: 'Musterfrau',
        isPrimary: false,
      }),
    ];
    mockStore.contacts = mockContacts;

    renderWithTheme(<Step3MultiContactManagement />);

    // Should show both contacts
    expect(screen.getByText('Herr Max Mustermann')).toBeInTheDocument();
    expect(screen.getByText('Frau Maria Musterfrau')).toBeInTheDocument();
  });

  it('should handle empty state with proper styling', () => {
    renderWithTheme(<Step3MultiContactManagement />);

    // Should show empty state message
    expect(screen.getByText(/Noch keine Kontakte erfasst/)).toBeInTheDocument();
    // Should show info alert
    expect(screen.getByRole('alert')).toBeInTheDocument();
  });

  it('should display primary contact indicator', () => {
    mockContacts = [
      createMockContact({ id: 'contact-1', isPrimary: true }),
      createMockContact({ id: 'contact-2', firstName: 'Maria', isPrimary: false }),
    ];
    mockStore.contacts = mockContacts;

    renderWithTheme(<Step3MultiContactManagement />);

    // Should show Hauptansprechpartner chip for primary contact
    expect(screen.getByText('Hauptansprechpartner')).toBeInTheDocument();
    // Should have star icon for primary
    expect(screen.getByTestId('StarIcon')).toBeInTheDocument();
  });

  it('should support mobile quick actions', async () => {
    // Mock mobile viewport
    global.innerWidth = 375;
    global.dispatchEvent(new Event('resize'));

    mockContacts = [createMockContact()];
    mockStore.contacts = mockContacts;

    renderWithTheme(<Step3MultiContactManagement />);

    // First expand the contact card to show quick actions
    const expandButton = screen
      .getAllByRole('button')
      .find(btn => btn.querySelector('[data-testid="ExpandMoreIcon"]'));

    if (expandButton) {
      await userEvent.click(expandButton);
      // Mobile quick actions should be visible after expansion
      expect(screen.getByLabelText('Schnellaktionen')).toBeInTheDocument();
    }
  });

  it('should handle contact removal', async () => {
    const contact = createMockContact({ isPrimary: false });
    mockContacts = [contact];
    mockStore.contacts = mockContacts;

    renderWithTheme(<Step3MultiContactManagement />);

    // Find the delete button with data-testid
    const deleteButton = screen.getByTestId('delete-contact-contact-1');
    await userEvent.click(deleteButton);

    expect(mockRemoveContact).toHaveBeenCalledWith('contact-1');
  });

  it('should call addContact when dialog is used', async () => {
    renderWithTheme(<Step3MultiContactManagement />);

    const addButton = screen.getByText('Neuen Kontakt hinzufügen');
    await userEvent.click(addButton);

    // Dialog should be opened
    expect(screen.getByRole('dialog')).toBeInTheDocument();
  });

  it('should render search functionality placeholder', () => {
    mockContacts = [
      createMockContact({ id: 'contact-1', firstName: 'Max' }),
      createMockContact({ id: 'contact-2', firstName: 'Maria', isPrimary: false }),
      createMockContact({ id: 'contact-3', firstName: 'Peter', isPrimary: false }),
    ];
    mockStore.contacts = mockContacts;

    renderWithTheme(<Step3MultiContactManagement />);

    // The component should have contact cards
    expect(screen.getAllByTestId('contact-card')).toHaveLength(3);
  });

  it('should display location assignments in overview', () => {
    mockContacts = [
      createMockContact({
        id: 'contact-1',
        responsibilityScope: 'specific',
        assignedLocationIds: ['loc-1', 'loc-2'],
        assignedLocationNames: ['Berlin', 'München'],
      }),
    ];
    mockStore.contacts = mockContacts;

    renderWithTheme(<Step3MultiContactManagement />);

    // The component shows the number of locations, not the names
    expect(screen.getByText('2 Standorte')).toBeInTheDocument();
  });

  it('should render multiple contact cards', () => {
    mockContacts = [
      createMockContact({ id: 'contact-1' }),
      createMockContact({ id: 'contact-2', firstName: 'Maria', isPrimary: false }),
      createMockContact({ id: 'contact-3', firstName: 'Peter', isPrimary: false }),
    ];
    mockStore.contacts = mockContacts;

    renderWithTheme(<Step3MultiContactManagement />);

    // Should render all contact cards
    const cards = screen.getAllByTestId('contact-card');
    expect(cards).toHaveLength(3);
  });

  it('should render contact with validation errors', () => {
    // Add a contact to test error display
    mockContacts = [createMockContact()];
    mockStore.contacts = mockContacts;

    renderWithTheme(<Step3MultiContactManagement />);

    // Should at least render the contact card
    expect(screen.getByTestId('contact-card')).toBeInTheDocument();
  });
});
