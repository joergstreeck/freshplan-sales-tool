# API Contract Update - Vorschlag

## Neue Endpoints für API_CONTRACT.md

### Calculator API
```typescript
// Calculate pricing
POST /api/calculator/calculate
Headers: { Authorization: 'Bearer ${token}' }
Request: {
  basePrice: number;
  customerType: 'new' | 'existing';
  isChainCustomer: boolean;
  chainType?: string;
  numberOfLocations: number;
  servicesPerLocation: {
    breakfast: number;
    lunch: number;
    vendingMachines: number;
  };
}
Response: {
  totalPrice: number;
  discount: number;
  discountPercentage: number;
  pricePerLocation: number;
  savings: number;
}
```

### Customer API
```typescript
// Create customer
POST /api/customers
Headers: { Authorization: 'Bearer ${token}' }
Request: {
  companyName: string;
  contactPerson: string;
  email: string;
  phone: string;
  address: {
    street: string;
    zip: string;
    city: string;
  };
  customerType: 'new' | 'existing';
  isChainCustomer: boolean;
}
Response: {
  id: string;
  ...customerData;
  createdAt: string;
}

// Get customer
GET /api/customers/{id}
Headers: { Authorization: 'Bearer ${token}' }
Response: { ...customerData }

// Update customer
PUT /api/customers/{id}
Headers: { Authorization: 'Bearer ${token}' }
Request: { ...customerData }
Response: { ...updatedCustomerData }

// List customers
GET /api/customers
Headers: { Authorization: 'Bearer ${token}' }
Query: { page?: number; size?: number; search?: string }
Response: {
  content: Customer[];
  totalElements: number;
  totalPages: number;
}
```

### Locations API
```typescript
// Add location to customer
POST /api/customers/{customerId}/locations
Headers: { Authorization: 'Bearer ${token}' }
Request: {
  name: string;
  address: {
    street: string;
    zip: string;
    city: string;
  };
  services: {
    breakfast: number;
    lunch: number;
    vendingMachines: number;
  };
}
Response: {
  id: string;
  ...locationData;
}

// Get customer locations
GET /api/customers/{customerId}/locations
Headers: { Authorization: 'Bearer ${token}' }
Response: Location[]
```

### Credit Check API
```typescript
// Initiate credit check
POST /api/creditcheck/initiate
Headers: { Authorization: 'Bearer ${token}' }
Request: {
  customerId: string;
  requestedAmount: number;
}
Response: {
  checkId: string;
  status: 'pending' | 'approved' | 'rejected';
  creditLimit?: number;
  validUntil?: string;
}

// Get credit check status
GET /api/creditcheck/{checkId}
Headers: { Authorization: 'Bearer ${token}' }
Response: {
  status: 'pending' | 'approved' | 'rejected';
  creditLimit?: number;
  reason?: string;
}
```

### UI Configuration API
```typescript
// Get navigation state for customer
GET /api/ui/navigation/{customerId}
Headers: { Authorization: 'Bearer ${token}' }
Response: {
  completedSteps: string[];
  currentStep: string;
  availableSteps: string[];
}
```

### Translations API
```typescript
// Get translations for locale
GET /api/translations/{locale}
Headers: { Authorization: 'Bearer ${token}' }
Response: {
  [key: string]: string;
}
```

## Security Requirements

Alle Endpoints (außer `/api/ping`) erfordern:
- Gültiges Bearer Token von Keycloak
- Passende Rolle für die Operation:
  - `viewer`: Nur GET Operationen
  - `sales`: Alle Customer/Calculator Operationen
  - `manager`: Zusätzlich Credit Check Operationen
  - `admin`: Vollzugriff inkl. User Management