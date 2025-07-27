/**
 * @fileoverview Test Utilities für FC-005 Customer Management
 * @module tests/setup/testUtils
 * 
 * Wiederverwendbare Test-Utilities und Render-Funktionen
 */

import React from 'react'
import { render, RenderOptions } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { BrowserRouter } from 'react-router-dom'
import { ToastContainer } from 'react-toastify'
import type { Customer, FieldDefinition, Location } from '../../types'

// =============================================================================
// Test Providers
// =============================================================================

/**
 * Create test query client with defaults
 */
export const createTestQueryClient = () => new QueryClient({
  defaultOptions: {
    queries: {
      retry: false,
      cacheTime: 0,
      staleTime: 0,
    },
    mutations: {
      retry: false,
    }
  },
  logger: {
    log: () => {},
    warn: () => {},
    error: () => {},
  }
})

/**
 * All providers needed for testing
 */
interface AllProvidersProps {
  children: React.ReactNode
  queryClient?: QueryClient
}

export const AllProviders: React.FC<AllProvidersProps> = ({ 
  children, 
  queryClient = createTestQueryClient() 
}) => {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        {children}
        <ToastContainer />
      </BrowserRouter>
    </QueryClientProvider>
  )
}

/**
 * Custom render with all providers
 */
export const renderWithProviders = (
  ui: React.ReactElement,
  options?: Omit<RenderOptions, 'wrapper'>
) => {
  const queryClient = createTestQueryClient()
  
  return {
    ...render(ui, { 
      wrapper: ({ children }) => (
        <AllProviders queryClient={queryClient}>
          {children}
        </AllProviders>
      ),
      ...options 
    }),
    queryClient
  }
}

// =============================================================================
// Test Data Factories
// =============================================================================

/**
 * Factory for creating test customers
 */
export const customerFactory = {
  build: (overrides: Partial<Customer> = {}): Customer => ({
    id: `customer-${Math.random().toString(36).substr(2, 9)}`,
    customerNumber: `C${Math.floor(Math.random() * 100000)}`,
    name: 'Test Customer GmbH',
    industry: 'hotel',
    fieldValues: {
      email: 'test@example.com',
      phone: '+49 89 123456',
      street: 'Teststraße 123',
      postal_code: '80331',
      city: 'München'
    },
    locations: [],
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
    ...overrides
  }),
  
  buildList: (count: number, overrides: Partial<Customer> = {}): Customer[] => {
    return Array.from({ length: count }, (_, i) => 
      customerFactory.build({
        ...overrides,
        name: `Test Customer ${i + 1}`,
        customerNumber: `C${10000 + i}`
      })
    )
  }
}

/**
 * Factory for creating test field definitions
 */
export const fieldDefinitionFactory = {
  build: (overrides: Partial<FieldDefinition> = {}): FieldDefinition => ({
    id: `field-${Math.random().toString(36).substr(2, 9)}`,
    name: 'test_field',
    label: 'Test Field',
    type: 'text',
    required: false,
    visible: true,
    editable: true,
    category: 'basic',
    industry: 'all',
    ...overrides
  }),
  
  buildSet: (): FieldDefinition[] => [
    fieldDefinitionFactory.build({
      id: 'customer_number',
      name: 'customer_number',
      label: 'Kundennummer',
      type: 'text',
      required: true,
      editable: false
    }),
    fieldDefinitionFactory.build({
      id: 'name',
      name: 'name',
      label: 'Firmenname',
      type: 'text',
      required: true
    }),
    fieldDefinitionFactory.build({
      id: 'email',
      name: 'email',
      label: 'E-Mail',
      type: 'email',
      required: true,
      category: 'contact'
    }),
    fieldDefinitionFactory.build({
      id: 'industry',
      name: 'industry',
      label: 'Branche',
      type: 'select',
      required: true,
      options: [
        { value: 'hotel', label: 'Hotel' },
        { value: 'hospital', label: 'Krankenhaus' }
      ]
    })
  ]
}

/**
 * Factory for creating test locations
 */
export const locationFactory = {
  build: (overrides: Partial<Location> = {}): Location => ({
    id: `location-${Math.random().toString(36).substr(2, 9)}`,
    name: 'Test Standort',
    type: 'main',
    street: 'Teststraße 1',
    postalCode: '80331',
    city: 'München',
    fieldValues: {},
    detailedLocations: [],
    ...overrides
  }),
  
  buildWithDetails: (overrides: Partial<Location> = {}): Location => ({
    ...locationFactory.build(overrides),
    detailedLocations: [
      {
        id: `detail-${Math.random().toString(36).substr(2, 9)}`,
        name: 'Restaurant',
        category: 'restaurant',
        floor: 'EG',
        capacity: 100,
        operationalHours: '11:00-23:00',
        specialRequirements: [],
        internalContact: 'Max Mustermann'
      },
      {
        id: `detail-${Math.random().toString(36).substr(2, 9)}`,
        name: 'Bar',
        category: 'bar',
        floor: '1.OG',
        capacity: 50,
        operationalHours: '17:00-02:00',
        specialRequirements: ['Cocktailbar'],
        internalContact: 'Maria Musterfrau'
      }
    ]
  })
}

// =============================================================================
// Test Helpers
// =============================================================================

/**
 * Wait for element to be removed
 */
export const waitForElementToBeRemoved = async (
  getElement: () => HTMLElement | null,
  timeout = 3000
) => {
  const start = Date.now()
  
  while (getElement() && Date.now() - start < timeout) {
    await new Promise(resolve => setTimeout(resolve, 50))
  }
  
  if (getElement()) {
    throw new Error(`Element was not removed within ${timeout}ms`)
  }
}

/**
 * Fill form field helper
 */
export const fillFormField = async (
  element: HTMLElement,
  fieldName: string,
  value: string
) => {
  const field = element.querySelector(`[name="${fieldName}"]`) as HTMLInputElement
  if (!field) throw new Error(`Field ${fieldName} not found`)
  
  const { fireEvent } = await import('@testing-library/react')
  fireEvent.change(field, { target: { value } })
  fireEvent.blur(field)
}

/**
 * Select option helper
 */
export const selectOption = async (
  element: HTMLElement,
  fieldName: string,
  optionValue: string
) => {
  const { fireEvent } = await import('@testing-library/react')
  const select = element.querySelector(`[name="${fieldName}"]`) as HTMLSelectElement
  if (!select) throw new Error(`Select ${fieldName} not found`)
  
  fireEvent.change(select, { target: { value: optionValue } })
}

/**
 * Submit form helper
 */
export const submitForm = async (form: HTMLFormElement) => {
  const { fireEvent, waitFor } = await import('@testing-library/react')
  
  fireEvent.submit(form)
  
  await waitFor(() => {
    // Wait for form submission to complete
  })
}

// =============================================================================
// Mock Implementations
// =============================================================================

/**
 * Mock successful API response
 */
export const mockApiSuccess = <T,>(data: T, delay = 100) => {
  return new Promise<T>((resolve) => {
    setTimeout(() => resolve(data), delay)
  })
}

/**
 * Mock API error
 */
export const mockApiError = (message = 'API Error', delay = 100) => {
  return new Promise((_, reject) => {
    setTimeout(() => reject(new Error(message)), delay)
  })
}

/**
 * Mock auto-save behavior
 */
export const mockAutoSave = () => {
  let savedData: any = null
  
  return {
    save: (data: any) => {
      savedData = data
      return mockApiSuccess({ success: true })
    },
    getSavedData: () => savedData,
    reset: () => { savedData = null }
  }
}