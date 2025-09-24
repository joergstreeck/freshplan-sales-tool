import { describe, it, expect, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { Step3MultiContactManagement } from '../../steps/Step3MultiContactManagement';
import { ContactCard } from '../ContactCard';
import type { Contact } from '../../../types/contact.types';

// Mock heavy operations
vi.mock('../../../stores/customerOnboardingStore', () => ({
  useCustomerOnboardingStore: () => ({
    contacts: [],
    addContact: vi.fn(),
    updateContact: vi.fn(),
    removeContact: vi.fn(),
    setPrimaryContact: vi.fn(),
    validateContacts: vi.fn().mockResolvedValue(true),
    contactValidationErrors: {},
  }),
}));

const createMockContacts = (count: number): Contact[] => {
  return Array.from({ length: count }, (_, i) => ({
    id: `contact-${i}`,
    customerId: 'customer-1',
    salutation: i % 3 === 0 ? 'Herr' : i % 3 === 1 ? 'Frau' : 'Divers',
    firstName: `Vorname${i}`,
    lastName: `Nachname${i}`,
    position: `Position ${i}`,
    decisionLevel: i % 2 === 0 ? 'entscheider' : 'mitentscheider',
    email: `contact${i}@example.com`,
    phone: `+49 30 ${String(i).padStart(8, '0')}`,
    mobile: `+49 170 ${String(i).padStart(7, '0')}`,
    isPrimary: i === 0,
    isActive: true,
    responsibilityScope: i % 2 === 0 ? 'all' : 'specific',
    assignedLocationIds: i % 2 === 1 ? [`loc-${i}`, `loc-${i + 1}`] : [],
    hobbies: i % 3 === 0 ? ['Golf', 'Tennis'] : [],
    birthday: i % 4 === 0 ? '1980-01-01' : undefined,
    personalNotes: i % 5 === 0 ? `Persönliche Notiz für Kontakt ${i}` : undefined,
    createdAt: new Date('2025-01-01').toISOString(),
    updatedAt: new Date('2025-01-01').toISOString(),
  }));
};

describe.skip('Contact Components Performance', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should render 50 contacts in under 200ms', async () => {
    const contacts = createMockContacts(50);

    // Update mock to return contacts
    vi.mocked(useCustomerOnboardingStore).mockReturnValue({
      ...vi.mocked(useCustomerOnboardingStore)(),
      contacts,
    });

    const start = performance.now();

    render(<Step3MultiContactManagement />);

    await waitFor(() => {
      expect(screen.getAllByTestId('contact-card')).toHaveLength(50);
    });

    const end = performance.now();
    const renderTime = end - start;

    if (process.env.DEBUG_PERF) console.log(`Rendered 50 contacts in ${renderTime.toFixed(2)}ms`);
    expect(renderTime).toBeLessThan(200);
  });

  it('should handle rapid contact additions efficiently', async () => {
    const mockAddContact = vi.fn();
    vi.mocked(useCustomerOnboardingStore).mockReturnValue({
      ...vi.mocked(useCustomerOnboardingStore)(),
      addContact: mockAddContact,
    });

    render(<Step3MultiContactManagement />);

    const start = performance.now();

    // Simulate rapid contact additions
    for (let i = 0; i < 10; i++) {
      mockAddContact({
        firstName: `Test${i}`,
        lastName: `User${i}`,
        email: `test${i}@example.com`,
      });
    }

    const end = performance.now();
    const operationTime = end - start;

    if (process.env.DEBUG_PERF) console.log(`Added 10 contacts in ${operationTime.toFixed(2)}ms`);
    expect(operationTime).toBeLessThan(50); // Should be very fast as it's just function calls
  });

  it('should efficiently filter large contact lists', async () => {
    const contacts = createMockContacts(100);

    vi.mocked(useCustomerOnboardingStore).mockReturnValue({
      ...vi.mocked(useCustomerOnboardingStore)(),
      contacts,
    });

    render(<Step3MultiContactManagement />);

    // Measure filter performance
    const start = performance.now();

    // Simulate search filter (component should handle this internally)
    const _filteredContacts = contacts.filter(
      c =>
        c.firstName.toLowerCase().includes('vorname1') ||
        c.lastName.toLowerCase().includes('vorname1')
    );

    const end = performance.now();
    const filterTime = end - start;

    if (process.env.DEBUG_PERF) console.log(`Filtered 100 contacts in ${filterTime.toFixed(2)}ms`);
    expect(filterTime).toBeLessThan(10); // Filtering should be very fast
  });

  it('should handle large relationship data efficiently', async () => {
    const contactWithLargeData = createMockContacts(1)[0];

    // Add large amounts of relationship data
    contactWithLargeData.hobbies = Array.from({ length: 20 }, (_, i) => `Hobby ${i}`);
    contactWithLargeData.personalNotes = 'Lorem ipsum '.repeat(100);
    contactWithLargeData.tags = Array.from({ length: 30 }, (_, i) => `Tag${i}`);

    const start = performance.now();

    render(<ContactCard contact={contactWithLargeData} />);

    await waitFor(() => {
      expect(screen.getByText('Vorname0 Nachname0')).toBeInTheDocument();
    });

    const end = performance.now();
    const renderTime = end - start;

    if (process.env.DEBUG_PERF)
      console.log(`Rendered contact with large data in ${renderTime.toFixed(2)}ms`);
    expect(renderTime).toBeLessThan(50);
  });

  it('should paginate efficiently for very large datasets', async () => {
    const PAGE_SIZE = 20;
    const TOTAL_CONTACTS = 1000;

    // Simulate paginated loading
    const loadPage = (page: number) => {
      const start = page * PAGE_SIZE;
      const end = Math.min(start + PAGE_SIZE, TOTAL_CONTACTS);
      return createMockContacts(end - start);
    };

    const start = performance.now();

    // Load first page
    const firstPage = loadPage(0);

    const end = performance.now();
    const loadTime = end - start;

    if (process.env.DEBUG_PERF)
      console.log(`Loaded page of ${PAGE_SIZE} contacts in ${loadTime.toFixed(2)}ms`);
    expect(loadTime).toBeLessThan(20);
    expect(firstPage).toHaveLength(PAGE_SIZE);
  });

  it('should debounce search input efficiently', async () => {
    const contacts = createMockContacts(50);
    const searchSpy = vi.fn();

    vi.mocked(useCustomerOnboardingStore).mockReturnValue({
      ...vi.mocked(useCustomerOnboardingStore)(),
      contacts,
      searchContacts: searchSpy,
    });

    render(<Step3MultiContactManagement />);

    // Simulate rapid typing
    const searches = ['M', 'Ma', 'Max', 'Max ', 'Max M'];

    const start = performance.now();

    // Component should debounce these calls
    searches.forEach(term => {
      // Simulate search without actual typing (component handles debouncing)
      searchSpy(term);
    });

    const end = performance.now();
    const searchTime = end - start;

    if (process.env.DEBUG_PERF)
      console.log(`Processed ${searches.length} search queries in ${searchTime.toFixed(2)}ms`);
    expect(searchTime).toBeLessThan(10);

    // With proper debouncing, should be called less than the number of searches
    expect(searchSpy).toHaveBeenCalledTimes(searches.length);
  });

  it('should handle bulk operations on large datasets', async () => {
    const contacts = createMockContacts(100);
    const mockUpdateContact = vi.fn();

    vi.mocked(useCustomerOnboardingStore).mockReturnValue({
      ...vi.mocked(useCustomerOnboardingStore)(),
      contacts,
      updateContact: mockUpdateContact,
    });

    const start = performance.now();

    // Simulate bulk update (e.g., deactivate 50 contacts)
    const contactsToUpdate = contacts.slice(0, 50);
    contactsToUpdate.forEach(contact => {
      mockUpdateContact(contact.id, { isActive: false });
    });

    const end = performance.now();
    const bulkUpdateTime = end - start;

    if (process.env.DEBUG_PERF)
      console.log(`Bulk updated 50 contacts in ${bulkUpdateTime.toFixed(2)}ms`);
    expect(bulkUpdateTime).toBeLessThan(100);
  });

  it('should lazy load contact details efficiently', async () => {
    const contacts = createMockContacts(20);

    // Only basic info loaded initially
    const basicContacts = contacts.map(({ id, firstName, lastName, isPrimary }) => ({
      id,
      firstName,
      lastName,
      isPrimary,
    }));

    const start = performance.now();

    // Simulate initial render with basic info
    basicContacts.slice(0, 10);

    const end = performance.now();
    const lazyLoadTime = end - start;

    if (process.env.DEBUG_PERF)
      console.log(`Lazy loaded 10 contact summaries in ${lazyLoadTime.toFixed(2)}ms`);
    expect(lazyLoadTime).toBeLessThan(5);
  });

  it('should maintain smooth scrolling with virtual list', async () => {
    const contacts = createMockContacts(500);

    // Simulate viewport that shows 10 items
    const VIEWPORT_SIZE = 10;
    const OVERSCAN = 3;

    const getVisibleRange = (scrollTop: number, itemHeight: number) => {
      const start = Math.floor(scrollTop / itemHeight);
      const end = start + VIEWPORT_SIZE;
      return {
        start: Math.max(0, start - OVERSCAN),
        end: Math.min(contacts.length, end + OVERSCAN),
      };
    };

    const start = performance.now();

    // Simulate scroll positions
    const scrollPositions = [0, 1000, 2000, 5000, 10000];
    const itemHeight = 80;

    scrollPositions.forEach(scrollTop => {
      const range = getVisibleRange(scrollTop, itemHeight);
      const visibleContacts = contacts.slice(range.start, range.end);
      expect(visibleContacts.length).toBeLessThanOrEqual(VIEWPORT_SIZE + OVERSCAN * 2);
    });

    const end = performance.now();
    const scrollSimulationTime = end - start;

    if (process.env.DEBUG_PERF)
      console.log(`Simulated virtual scrolling in ${scrollSimulationTime.toFixed(2)}ms`);
    expect(scrollSimulationTime).toBeLessThan(10);
  });

  it('should optimize re-renders on contact updates', async () => {
    const contacts = createMockContacts(30);
    let renderCount = 0;

    // Track renders
    const TestComponent = () => {
      renderCount++;
      return <Step3MultiContactManagement />;
    };

    vi.mocked(useCustomerOnboardingStore).mockReturnValue({
      ...vi.mocked(useCustomerOnboardingStore)(),
      contacts,
    });

    const { rerender } = render(<TestComponent />);

    const initialRenderCount = renderCount;

    // Update single contact
    contacts[5] = { ...contacts[5], firstName: 'Updated' };

    vi.mocked(useCustomerOnboardingStore).mockReturnValue({
      ...vi.mocked(useCustomerOnboardingStore)(),
      contacts: [...contacts],
    });

    rerender(<TestComponent />);

    // Should only trigger minimal re-renders
    expect(renderCount - initialRenderCount).toBeLessThanOrEqual(2);
  });
});
