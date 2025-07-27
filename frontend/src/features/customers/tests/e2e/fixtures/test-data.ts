/**
 * FC-005 E2E Test Data Fixtures
 * 
 * ZWECK: Wiederverwendbare Test-Daten für E2E Tests
 * PHILOSOPHIE: Realistische Daten die die field-basierte Architektur testen
 */

export const testCustomers = {
  singleLocation: {
    companyName: 'E2E Happy Path Hotel',
    industry: 'hotel',
    chainCustomer: 'nein',
    hotelStars: '4',
    email: 'kontakt@e2e-hotel.de',
    phone: '+49 30 12345678',
    website: 'www.e2e-hotel.de'
  },
  
  chainCustomer: {
    companyName: 'E2E Chain Customer Test',
    industry: 'hotel', 
    chainCustomer: 'ja',
    hotelStars: '5',
    email: 'zentrale@e2e-chain.de',
    phone: '+49 89 87654321',
    website: 'www.e2e-chain.de'
  },
  
  officeCustomer: {
    companyName: 'E2E Büro & Verwaltung GmbH',
    industry: 'office',
    chainCustomer: 'nein',
    email: 'info@e2e-buero.de',
    phone: '+49 40 11111111',
    employees: '250'
  },
  
  healthcareCustomer: {
    companyName: 'E2E Gesundheitszentrum',
    industry: 'healthcare',
    chainCustomer: 'nein',
    email: 'verwaltung@e2e-gesundheit.de',
    phone: '+49 221 22222222',
    bedCount: '150'
  }
};

export const testLocations = [
  {
    name: 'Berlin Mitte',
    street: 'Unter den Linden 1',
    postalCode: '10117',
    city: 'Berlin',
    contactPerson: 'Anna Schmidt',
    phone: '+49 30 111111',
    email: 'anna.schmidt@e2e-chain.de'
  },
  {
    name: 'München Zentrum', 
    street: 'Marienplatz 8',
    postalCode: '80331',
    city: 'München',
    contactPerson: 'Max Müller',
    phone: '+49 89 222222',
    email: 'max.mueller@e2e-chain.de'
  },
  {
    name: 'Hamburg Hafen',
    street: 'Speicherstadt 15',
    postalCode: '20457', 
    city: 'Hamburg',
    contactPerson: 'Lisa Weber',
    phone: '+49 40 333333',
    email: 'lisa.weber@e2e-chain.de'
  }
];

export const testDetailedLocations = [
  {
    locationName: 'Berlin Mitte',
    details: [
      {
        name: 'Hauptrestaurant',
        category: 'restaurant',
        floor: 'EG',
        capacity: '120',
        operatingHours: '06:00-22:00',
        responsiblePerson: 'Chef Koch',
        internalPhone: '101',
        specialRequirements: 'Vegetarische und vegane Optionen'
      },
      {
        name: 'Tagungsraum Alpha',
        category: 'meeting',
        floor: '1. OG',
        capacity: '25',
        operatingHours: '08:00-18:00',
        responsiblePerson: 'Event Manager',
        internalPhone: '201',
        specialRequirements: 'Beamer und Flipchart vorhanden'
      }
    ]
  },
  {
    locationName: 'München Zentrum',
    details: [
      {
        name: 'Brasserie',
        category: 'restaurant', 
        floor: 'EG',
        capacity: '80',
        operatingHours: '07:00-23:00',
        responsiblePerson: 'Sous Chef',
        internalPhone: '102',
        specialRequirements: 'Bayerische Küche'
      },
      {
        name: 'Wellness-Bereich',
        category: 'wellness',
        floor: 'UG',
        capacity: '30',
        operatingHours: '06:00-22:00',
        responsiblePerson: 'Wellness Manager',
        internalPhone: '301',
        specialRequirements: 'Sauna und Pool'
      }
    ]
  }
];

export const validationTestCases = {
  invalidCompanyName: {
    value: '',
    expectedError: 'Firmenname ist erforderlich'
  },
  invalidEmail: {
    value: 'invalid-email',
    expectedError: 'Ungültige E-Mail-Adresse'
  },
  invalidPostalCode: {
    value: '1234', // Zu kurz für deutsche PLZ
    expectedError: 'PLZ muss 5 Ziffern haben'
  },
  invalidPhone: {
    value: '123', // Zu kurz
    expectedError: 'Ungültige Telefonnummer'
  }
};

export const performanceTestData = {
  largeLocationList: Array.from({ length: 50 }, (_, index) => ({
    name: `Performance Test Location ${index + 1}`,
    street: `Test Straße ${index + 1}`,
    postalCode: `${10000 + index}`.padStart(5, '0'),
    city: `Test Stadt ${index + 1}`,
    contactPerson: `Test Person ${index + 1}`,
    phone: `+49 30 ${(1000000 + index).toString()}`,
    email: `test${index + 1}@performance-test.de`
  })),
  
  complexCustomerData: {
    companyName: 'Performance Test Enterprise GmbH & Co. KG',
    industry: 'hotel',
    chainCustomer: 'ja',
    hotelStars: '5',
    // Complex nested data to test performance
    customFields: {
      certifications: [
        'ISO 9001:2015',
        'ISO 14001:2015', 
        'HACCP',
        'Bio-Zertifikat',
        'Halal-Zertifikat'
      ],
      amenities: {
        restaurant: {
          types: ['Fine Dining', 'Casual', 'Bar'],
          cuisines: ['International', 'Regional', 'Vegetarian', 'Vegan'],
          capacity: 200,
          openingHours: {
            monday: '06:00-22:00',
            tuesday: '06:00-22:00',
            wednesday: '06:00-22:00',
            thursday: '06:00-22:00',
            friday: '06:00-23:00',
            saturday: '06:00-23:00',
            sunday: '06:00-22:00'
          }
        },
        wellness: {
          facilities: ['Sauna', 'Steam Bath', 'Pool', 'Gym', 'Spa'],
          treatments: ['Massage', 'Facial', 'Body Wrap', 'Manicure'],
          operatingHours: '06:00-22:00'
        }
      },
      policies: {
        cancellation: '24 hours',
        checkIn: '15:00',
        checkOut: '11:00',
        smoking: 'designated areas only',
        pets: 'allowed with fee'
      }
    }
  }
};

export const accessibilityTestData = {
  // Screen reader friendly labels
  ariaLabels: {
    companyName: 'Firmenname eingeben',
    industry: 'Branche auswählen', 
    chainCustomer: 'Kettenkunde ja oder nein',
    addLocation: 'Neuen Standort hinzufügen',
    removeLocation: 'Standort entfernen',
    nextStep: 'Zum nächsten Schritt',
    previousStep: 'Zum vorherigen Schritt',
    saveCustomer: 'Kunden speichern'
  },
  
  // Keyboard navigation test sequences
  keyboardNavigation: [
    'Tab', 'Tab', 'Tab', // Navigate through form fields
    'Enter', // Select dropdown option
    'Tab', 'Space', // Navigate to and activate button
    'Shift+Tab', // Navigate backwards
    'Escape' // Close modal/dropdown
  ]
};

export default {
  testCustomers,
  testLocations,
  testDetailedLocations,
  validationTestCases,
  performanceTestData,
  accessibilityTestData
};