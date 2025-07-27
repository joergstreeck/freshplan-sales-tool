/**
 * @fileoverview MSW Mock Server für FC-005 Customer Management
 * @module tests/setup/mockServer
 * 
 * Mock Service Worker Setup für API Mocking in Tests
 * Simuliert das komplette Backend für Customer Management
 */

import { setupServer } from 'msw/node'
import { rest } from 'msw'
import type { 
  Customer, 
  CustomerDraftResponse,
  FieldDefinition,
  Location 
} from '../../types'

// =============================================================================
// Test Data
// =============================================================================

const mockFieldDefinitions: FieldDefinition[] = [
  {
    id: 'customer_number',
    name: 'customer_number',
    label: 'Kundennummer',
    type: 'text',
    required: true,
    visible: true,
    editable: false,
    category: 'basic',
    industry: 'all',
    validation: {
      pattern: '^[0-9]{5,10}$',
      message: 'Kundennummer muss 5-10 Ziffern haben'
    }
  },
  {
    id: 'name',
    name: 'name',
    label: 'Firmenname',
    type: 'text',
    required: true,
    visible: true,
    editable: true,
    category: 'basic',
    industry: 'all',
    validation: {
      minLength: 2,
      maxLength: 100
    }
  },
  {
    id: 'email',
    name: 'email',
    label: 'E-Mail',
    type: 'email',
    required: true,
    visible: true,
    editable: true,
    category: 'contact',
    industry: 'all'
  },
  {
    id: 'room_count',
    name: 'room_count',
    label: 'Anzahl Zimmer',
    type: 'number',
    required: true,
    visible: true,
    editable: true,
    category: 'details',
    industry: 'hotel',
    validation: {
      min: 1,
      max: 1000
    }
  }
]

const mockDrafts = new Map<string, any>()
const mockCustomers = new Map<string, Customer>()

// =============================================================================
// Request Handlers
// =============================================================================

export const handlers = [
  // =========================================================================
  // Field Definitions
  // =========================================================================
  
  rest.get('/api/field-definitions', (req, res, ctx) => {
    const industry = req.url.searchParams.get('industry')
    
    const fields = industry 
      ? mockFieldDefinitions.filter(f => f.industry === industry || f.industry === 'all')
      : mockFieldDefinitions
    
    return res(
      ctx.status(200),
      ctx.json(fields),
      ctx.delay(50)
    )
  }),

  rest.get('/api/field-definitions/:id', (req, res, ctx) => {
    const { id } = req.params
    const field = mockFieldDefinitions.find(f => f.id === id)
    
    if (!field) {
      return res(ctx.status(404))
    }
    
    return res(
      ctx.status(200),
      ctx.json(field)
    )
  }),

  // =========================================================================
  // Customer Drafts
  // =========================================================================
  
  rest.post('/api/customers/drafts', (req, res, ctx) => {
    const draftId = `draft-${Date.now()}`
    const draft: CustomerDraftResponse = {
      draftId,
      expiresAt: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString(),
      data: {}
    }
    
    mockDrafts.set(draftId, draft)
    
    return res(
      ctx.status(201),
      ctx.json(draft),
      ctx.delay(100)
    )
  }),

  rest.put('/api/customers/drafts/:draftId', async (req, res, ctx) => {
    const { draftId } = req.params
    const data = await req.json()
    
    if (!mockDrafts.has(draftId as string)) {
      return res(ctx.status(404))
    }
    
    const draft = mockDrafts.get(draftId as string)
    draft.data = { ...draft.data, ...data }
    mockDrafts.set(draftId as string, draft)
    
    return res(
      ctx.status(200),
      ctx.json(draft)
    )
  }),

  rest.post('/api/customers/drafts/:draftId/finalize', async (req, res, ctx) => {
    const { draftId } = req.params
    const draft = mockDrafts.get(draftId as string)
    
    if (!draft) {
      return res(ctx.status(404))
    }
    
    const customer: Customer = {
      id: `customer-${Date.now()}`,
      customerNumber: draft.data.customerNumber || `C${Date.now()}`,
      name: draft.data.name || 'New Customer',
      industry: draft.data.industry || 'hotel',
      fieldValues: draft.data.fieldValues || {},
      locations: draft.data.locations || [],
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    }
    
    mockCustomers.set(customer.id, customer)
    mockDrafts.delete(draftId as string)
    
    return res(
      ctx.status(201),
      ctx.json(customer),
      ctx.delay(150)
    )
  }),

  // =========================================================================
  // Customers
  // =========================================================================
  
  rest.get('/api/customers', (req, res, ctx) => {
    const page = parseInt(req.url.searchParams.get('page') || '0')
    const size = parseInt(req.url.searchParams.get('size') || '20')
    const search = req.url.searchParams.get('search')
    
    let customers = Array.from(mockCustomers.values())
    
    if (search) {
      customers = customers.filter(c => 
        c.name.toLowerCase().includes(search.toLowerCase()) ||
        c.customerNumber.includes(search)
      )
    }
    
    const start = page * size
    const end = start + size
    const paginatedCustomers = customers.slice(start, end)
    
    return res(
      ctx.status(200),
      ctx.json({
        content: paginatedCustomers,
        totalElements: customers.length,
        totalPages: Math.ceil(customers.length / size),
        number: page,
        size: size
      })
    )
  }),

  rest.get('/api/customers/:id', (req, res, ctx) => {
    const { id } = req.params
    const customer = mockCustomers.get(id as string)
    
    if (!customer) {
      return res(ctx.status(404))
    }
    
    return res(
      ctx.status(200),
      ctx.json(customer)
    )
  }),

  rest.put('/api/customers/:id', async (req, res, ctx) => {
    const { id } = req.params
    const updates = await req.json()
    const customer = mockCustomers.get(id as string)
    
    if (!customer) {
      return res(ctx.status(404))
    }
    
    const updatedCustomer = {
      ...customer,
      ...updates,
      updatedAt: new Date().toISOString()
    }
    
    mockCustomers.set(id as string, updatedCustomer)
    
    return res(
      ctx.status(200),
      ctx.json(updatedCustomer)
    )
  }),

  rest.delete('/api/customers/:id', (req, res, ctx) => {
    const { id } = req.params
    
    if (!mockCustomers.has(id as string)) {
      return res(ctx.status(404))
    }
    
    mockCustomers.delete(id as string)
    
    return res(ctx.status(204))
  }),

  // =========================================================================
  // Locations
  // =========================================================================
  
  rest.get('/api/locations/industries', (req, res, ctx) => {
    return res(
      ctx.status(200),
      ctx.json([
        { code: 'hotel', label: 'Hotel / Gastronomie' },
        { code: 'hospital', label: 'Krankenhaus' },
        { code: 'senior_residence', label: 'Seniorenresidenz' },
        { code: 'restaurant', label: 'Restaurant' },
        { code: 'company_restaurant', label: 'Betriebsrestaurant' }
      ])
    )
  }),

  rest.get('/api/locations/categories', (req, res, ctx) => {
    const industry = req.url.searchParams.get('industry')
    
    const categories = {
      hotel: ['restaurant', 'bar', 'banquet', 'pool', 'spa'],
      hospital: ['cafeteria', 'station', 'kiosk', 'staff'],
      senior_residence: ['dining', 'residential', 'therapy'],
      restaurant: ['main', 'outdoor', 'bar', 'private'],
      company_restaurant: ['canteen', 'coffee', 'meeting']
    }
    
    return res(
      ctx.status(200),
      ctx.json(categories[industry as keyof typeof categories] || [])
    )
  })
]

// =============================================================================
// Server Setup
// =============================================================================

export const server = setupServer(...handlers)

// =============================================================================
// Test Utilities
// =============================================================================

/**
 * Reset all mock data
 */
export const resetMockData = () => {
  mockDrafts.clear()
  mockCustomers.clear()
}

/**
 * Add test customer
 */
export const addTestCustomer = (customer: Customer) => {
  mockCustomers.set(customer.id, customer)
}

/**
 * Get all mock customers
 */
export const getMockCustomers = () => Array.from(mockCustomers.values())

/**
 * Get mock draft
 */
export const getMockDraft = (draftId: string) => mockDrafts.get(draftId)