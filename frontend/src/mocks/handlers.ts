import { http, HttpResponse } from 'msw';

// Sprint 2.1.6: Lead API uses real backend - no mocks needed
// MSW is configured with onUnhandledRequest: 'bypass' to allow real API calls

// Mock customer data
const mockCustomers = [
  {
    id: '1',
    customerNumber: 'K-2025-001',
    companyName: 'FreshFood GmbH',
    tradingName: 'FreshFood Berlin',
    customerType: 'EXISTING',
    status: 'ACTIVE',
    industry: 'GASTRONOMY',
    expectedAnnualVolume: 150000,
    lastContactDate: '2025-08-01',
    riskScore: 25,
    atRisk: false,
  },
  {
    id: '2',
    customerNumber: 'K-2025-002',
    companyName: 'Restaurant Zur goldenen Gans',
    tradingName: null,
    customerType: 'NEW',
    status: 'ACTIVE',
    industry: 'RESTAURANT',
    expectedAnnualVolume: 85000,
    lastContactDate: '2025-07-28',
    riskScore: 15,
    atRisk: false,
  },
  {
    id: '3',
    customerNumber: 'K-2025-003',
    companyName: 'Kantine Plus AG',
    tradingName: 'Kantine Plus',
    customerType: 'EXISTING',
    status: 'AT_RISK',
    industry: 'CANTEEN',
    expectedAnnualVolume: 220000,
    lastContactDate: '2025-06-15',
    riskScore: 75,
    atRisk: true,
  },
  {
    id: '4',
    customerNumber: 'K-2025-004',
    companyName: 'Hotel Vier Jahreszeiten',
    tradingName: null,
    customerType: 'EXISTING',
    status: 'ACTIVE',
    industry: 'HOTEL',
    expectedAnnualVolume: 180000,
    lastContactDate: '2025-07-30',
    riskScore: 35,
    atRisk: false,
  },
  {
    id: '5',
    customerNumber: 'K-2025-005',
    companyName: 'Café am Markt',
    tradingName: 'Marktcafé',
    customerType: 'NEW',
    status: 'INACTIVE',
    industry: 'CAFE_BAR',
    expectedAnnualVolume: 45000,
    lastContactDate: '2025-05-20',
    riskScore: 90,
    atRisk: true,
  },
];

export const handlers = [
  // Sales Cockpit API endpoints
  http.get('http://localhost:8080/api/sales-cockpit/dashboard/dev', () => {
    return HttpResponse.json({
      totalLeads: 3,
      totalCustomers: 5,
      monthlyRevenue: 25000,
      conversionRate: 15.2,
    });
  }),

  // Customer API endpoints
  http.get('http://localhost:8080/api/customers', ({ request }) => {
    const url = new URL(request.url);
    const page = parseInt(url.searchParams.get('page') || '0');
    const size = parseInt(url.searchParams.get('size') || '20');
    const sort = url.searchParams.get('sort') || 'companyName';

    // Simple sorting
    const sortedCustomers = [...mockCustomers].sort((a, b) => {
      if (sort === 'customerNumber') {
        return a.customerNumber.localeCompare(b.customerNumber);
      } else if (sort === 'status') {
        return a.status.localeCompare(b.status);
      } else if (sort === 'lastContactDate') {
        return new Date(b.lastContactDate).getTime() - new Date(a.lastContactDate).getTime();
      }
      return a.companyName.localeCompare(b.companyName);
    });

    // Pagination
    const start = page * size;
    const end = start + size;
    const paginatedCustomers = sortedCustomers.slice(start, end);

    return HttpResponse.json({
      content: paginatedCustomers,
      page: page,
      size: size,
      totalElements: mockCustomers.length,
      totalPages: Math.ceil(mockCustomers.length / size),
      first: page === 0,
      last: page === Math.ceil(mockCustomers.length / size) - 1,
    });
  }),

  // Customer search endpoint
  http.post('http://localhost:8080/api/customers/search', () => {
    return HttpResponse.json({
      content: mockCustomers.slice(0, 3), // Return first 3 customers
      page: 0,
      size: 50,
      totalElements: mockCustomers.length,
      totalPages: 1,
      first: true,
      last: true,
    });
  }),

  // Sprint 2.1.6: Lead API endpoints use real backend
  // No mock handlers needed - MSW configured with 'bypass' for unhandled requests

  // Sprint 2.1.6.1 Phase 1: BusinessType Enum Endpoint
  http.get('http://localhost:8080/api/enums/business-types', () => {
    return HttpResponse.json([
      { value: 'RESTAURANT', label: 'Restaurant' },
      { value: 'HOTEL', label: 'Hotel' },
      { value: 'CATERING', label: 'Catering' },
      { value: 'KANTINE', label: 'Kantine' },
      { value: 'GROSSHANDEL', label: 'Großhandel' },
      { value: 'LEH', label: 'LEH' },
      { value: 'BILDUNG', label: 'Bildung' },
      { value: 'GESUNDHEIT', label: 'Gesundheit' },
      { value: 'SONSTIGES', label: 'Sonstiges' },
    ]);
  }),

  // Sprint 2.1.7.2: KitchenSize Enum
  http.get('http://localhost:8080/api/enums/kitchen-sizes', () => {
    return HttpResponse.json([
      { value: 'KLEIN', label: 'Klein (<5 Mitarbeiter)' },
      { value: 'MITTEL', label: 'Mittel (5-20 Mitarbeiter)' },
      { value: 'GROSS', label: 'Groß (20-50 Mitarbeiter)' },
      { value: 'SEHR_GROSS', label: 'Sehr groß (>50 Mitarbeiter)' },
    ]);
  }),

  // Sprint 2.1.7.2: LeadSource Enum
  http.get('http://localhost:8080/api/enums/lead-sources', () => {
    return HttpResponse.json([
      { value: 'MESSE', label: 'Messe' },
      { value: 'EMPFEHLUNG', label: 'Empfehlung' },
      { value: 'TELEFON', label: 'Telefon (Kaltakquise)' },
      { value: 'WEB_FORMULAR', label: 'Web-Formular' },
      { value: 'PARTNER', label: 'Partner' },
      { value: 'SONSTIGE', label: 'Sonstige' },
    ]);
  }),

  // Sprint 2.1.7.2: Lead Schema Endpoint (Progressive Profiling + Edit Dialog)
  http.get('http://localhost:8080/api/leads/schema', () => {
    return HttpResponse.json([
      // Card 1: Progressive Profiling (LeadWizard - 3 stages)
      {
        cardId: 'lead_progressive_profiling',
        title: 'Lead erfassen',
        subtitle: 'Progressive Profiling: Schrittweise Lead-Qualifizierung',
        icon: '🎯',
        order: 1,
        sections: [
          // ========== STAGE 0: PRE-CLAIM (MESSE, EMPFEHLUNG) ==========
          {
            sectionId: 'stage_0_pre_claim',
            title: 'Basis-Informationen (Pre-Claim)',
            subtitle: 'Pflichtfelder für Lead-Schutz (10 Tage)',
            collapsible: false,
            defaultCollapsed: false,
            fields: [
              {
                fieldKey: 'companyName',
                label: 'Firmenname',
                type: 'TEXT',
                required: true,
                gridCols: 12,
                placeholder: 'z.B. Hotel Adlon Kempinski Berlin',
                helpText: 'Der offizielle Firmenname des Leads',
              },
              {
                fieldKey: 'source',
                label: 'Quelle',
                type: 'ENUM',
                enumSource: '/api/enums/lead-sources',
                required: true,
                gridCols: 6,
                helpText: 'Woher kommt der Lead? (MESSE/EMPFEHLUNG = Pre-Claim 10 Tage)',
              },
              {
                fieldKey: 'website',
                label: 'Website',
                type: 'TEXT',
                gridCols: 6,
                placeholder: 'https://www.example.com',
                helpText: 'Unternehmens-Website (optional)',
                showDividerAfter: true,
              },
              {
                fieldKey: 'phone',
                label: 'Telefon',
                type: 'TEXT',
                gridCols: 6,
                placeholder: '+49 30 12345678',
                helpText: 'Haupttelefonnummer (optional)',
              },
              {
                fieldKey: 'email',
                label: 'E-Mail',
                type: 'TEXT',
                gridCols: 6,
                placeholder: 'info@example.com',
                helpText: 'Haupt-E-Mail-Adresse (optional)',
                showDividerAfter: true,
              },
              {
                fieldKey: 'street',
                label: 'Straße',
                type: 'TEXT',
                gridCols: 12,
                placeholder: 'Unter den Linden 77',
                helpText: 'Straße und Hausnummer (optional)',
              },
              {
                fieldKey: 'postalCode',
                label: 'PLZ',
                type: 'TEXT',
                gridCols: 4,
                placeholder: '10117',
                helpText: 'Postleitzahl (optional)',
              },
              {
                fieldKey: 'city',
                label: 'Stadt',
                type: 'TEXT',
                gridCols: 8,
                placeholder: 'Berlin',
                helpText: 'Stadt (optional)',
                showDividerAfter: true,
              },
              {
                fieldKey: 'notes',
                label: 'Notizen / Quelle (optional)',
                type: 'TEXTAREA',
                gridCols: 12,
                placeholder: 'Z.B. Empfehlung von Herrn Schulz, Partner-Liste Nr. 47, Stand A-12 auf der INTERNORGA...',
                helpText: 'Hintergrund-Informationen ohne Einfluss auf Lead-Schutz',
              },
            ],
          },
          // ========== STAGE 1: VOLLSCHUTZ (6 MONATE) ==========
          {
            sectionId: 'stage_1_vollschutz',
            title: 'Erweiterte Informationen (Vollschutz)',
            subtitle: 'Business Type, Budget, Mitarbeiter, Filialstruktur',
            collapsible: false,
            defaultCollapsed: false,
            fields: [
              {
                fieldKey: 'businessType',
                label: 'Branche',
                type: 'ENUM',
                enumSource: '/api/enums/business-types',
                gridCols: 6,
                helpText: 'Branche des Unternehmens',
              },
              {
                fieldKey: 'kitchenSize',
                label: 'Küchengröße',
                type: 'ENUM',
                enumSource: '/api/enums/kitchen-sizes',
                gridCols: 6,
                helpText: 'Größe der Küche / Produktionskapazität',
              },
              {
                fieldKey: 'employeeCount',
                label: 'Mitarbeiteranzahl',
                type: 'NUMBER',
                gridCols: 6,
                placeholder: 'z.B. 50',
                helpText: 'Anzahl Mitarbeiter gesamt',
              },
              {
                fieldKey: 'estimatedVolume',
                label: 'Geschätztes Jahresvolumen (€)',
                type: 'CURRENCY',
                gridCols: 6,
                placeholder: 'z.B. 100000',
                helpText: 'Geschätztes jährliches Einkaufsvolumen Lebensmittel/Getränke',
                showDividerAfter: true,
              },
              {
                fieldKey: 'branchCount',
                label: 'Anzahl Filialen/Standorte',
                type: 'NUMBER',
                gridCols: 6,
                placeholder: 'z.B. 1',
                helpText: 'Anzahl Filialen/Standorte (1 = Einzelstandort)',
              },
              {
                fieldKey: 'isChain',
                label: 'Kettenbetrieb',
                type: 'BOOLEAN',
                gridCols: 6,
                helpText: 'Ist dies ein Kettenbetrieb? (mehrere Standorte mit zentraler Verwaltung)',
              },
            ],
          },
          // ========== STAGE 2: NURTURING & QUALIFIKATION ==========
          {
            sectionId: 'stage_2_nurturing',
            title: 'Nurturing & Qualifikation',
            subtitle: 'Pain Points, Beziehungsstatus, Entscheider-Zugang',
            collapsible: false,
            defaultCollapsed: false,
            fields: [
              {
                fieldKey: 'painStaffShortage',
                label: 'Personalmangel',
                type: 'BOOLEAN',
                gridCols: 6,
                helpText: 'Leidet der Betrieb unter Personalmangel?',
              },
              {
                fieldKey: 'painHighCosts',
                label: 'Hohe Kosten',
                type: 'BOOLEAN',
                gridCols: 6,
                helpText: 'Sind die Einkaufskosten zu hoch?',
              },
              {
                fieldKey: 'painFoodWaste',
                label: 'Lebensmittelverschwendung',
                type: 'BOOLEAN',
                gridCols: 6,
                helpText: 'Gibt es Probleme mit Lebensmittelverschwendung?',
              },
              {
                fieldKey: 'painQualityInconsistency',
                label: 'Qualitätsschwankungen',
                type: 'BOOLEAN',
                gridCols: 6,
                helpText: 'Gibt es Qualitätsschwankungen bei Produkten?',
              },
              {
                fieldKey: 'painTimePressure',
                label: 'Zeitdruck',
                type: 'BOOLEAN',
                gridCols: 6,
                helpText: 'Besteht Zeitdruck in der Küche/Produktion?',
              },
              {
                fieldKey: 'painSupplierQuality',
                label: 'Lieferanten-Qualität',
                type: 'BOOLEAN',
                gridCols: 6,
                helpText: 'Gibt es Probleme mit der Qualität des aktuellen Lieferanten?',
              },
              {
                fieldKey: 'painUnreliableDelivery',
                label: 'Unzuverlässige Lieferung',
                type: 'BOOLEAN',
                gridCols: 6,
                helpText: 'Ist die Lieferung des aktuellen Lieferanten unzuverlässig?',
              },
              {
                fieldKey: 'painPoorService',
                label: 'Schlechter Service',
                type: 'BOOLEAN',
                gridCols: 6,
                helpText: 'Ist der Service des aktuellen Lieferanten schlecht?',
              },
              {
                fieldKey: 'painNotes',
                label: 'Notizen zu Schmerzpunkten',
                type: 'TEXTAREA',
                gridCols: 12,
                placeholder: 'Weitere Schmerzpunkte oder Details...',
                helpText: 'Freitext für zusätzliche Pain Points',
                showDividerAfter: true,
              },
              {
                fieldKey: 'relationshipStatus',
                label: 'Beziehungsstatus',
                type: 'ENUM',
                enumSource: '/api/enums/relationship-status',
                gridCols: 6,
                helpText: 'Wie ist der aktuelle Beziehungsstatus?',
              },
              {
                fieldKey: 'decisionMakerAccess',
                label: 'Entscheider-Zugang',
                type: 'ENUM',
                enumSource: '/api/enums/decision-maker-access',
                gridCols: 6,
                helpText: 'Haben wir Zugang zum Entscheider?',
              },
              {
                fieldKey: 'competitorInUse',
                label: 'Aktueller Wettbewerber',
                type: 'TEXT',
                gridCols: 6,
                placeholder: 'z.B. Metro, Transgourmet',
                helpText: 'Welcher Wettbewerber wird aktuell genutzt?',
              },
              {
                fieldKey: 'internalChampionName',
                label: 'Interner Champion',
                type: 'TEXT',
                gridCols: 6,
                placeholder: 'z.B. Max Mustermann',
                helpText: 'Name des internen Champions (falls vorhanden)',
              },
            ],
          },
        ],
      },
      // Card 2: Edit Dialog (not needed for LeadWizard tests, but included for completeness)
      {
        cardId: 'lead_edit',
        title: 'Lead bearbeiten',
        subtitle: 'Lead-Informationen aktualisieren',
        icon: '✏️',
        order: 2,
        sections: [
          {
            sectionId: 'edit_basic',
            title: 'Basis-Informationen',
            subtitle: 'Firmenname, Quelle, Website',
            collapsible: false,
            defaultCollapsed: false,
            fields: [
              {
                fieldKey: 'companyName',
                label: 'Firmenname',
                type: 'TEXT',
                required: true,
                gridCols: 12,
                placeholder: 'z.B. Hotel Adlon Kempinski Berlin',
                helpText: 'Der offizielle Firmenname des Leads',
              },
              {
                fieldKey: 'source',
                label: 'Quelle',
                type: 'ENUM',
                enumSource: '/api/enums/lead-sources',
                required: true,
                gridCols: 6,
                helpText: 'MESSE/TELEFON erfordern Erstkontakt-Dokumentation. Andere Quellen: 10-Tage Pre-Claim.',
              },
              {
                fieldKey: 'website',
                label: 'Website',
                type: 'TEXT',
                gridCols: 6,
                placeholder: 'https://www.example.com',
                helpText: 'Unternehmens-Website (optional)',
              },
            ],
          },
          {
            sectionId: 'edit_address',
            title: 'Adresse',
            subtitle: 'Straße, PLZ, Stadt',
            collapsible: false,
            defaultCollapsed: false,
            fields: [
              {
                fieldKey: 'street',
                label: 'Straße',
                type: 'TEXT',
                gridCols: 12,
                placeholder: 'Unter den Linden 77',
                helpText: 'Straße und Hausnummer (optional)',
              },
              {
                fieldKey: 'postalCode',
                label: 'PLZ',
                type: 'TEXT',
                gridCols: 4,
                placeholder: '10117',
                helpText: 'Postleitzahl (optional)',
              },
              {
                fieldKey: 'city',
                label: 'Stadt',
                type: 'TEXT',
                gridCols: 8,
                placeholder: 'Berlin',
                helpText: 'Stadt (optional)',
              },
            ],
          },
          {
            sectionId: 'edit_extended',
            title: 'Erweiterte Informationen',
            subtitle: 'Weitere Lead-Details über separate Dialoge verfügbar',
            collapsible: true,
            defaultCollapsed: true,
            fields: [],
          },
        ],
      },
    ]);
  }),

  // Sprint 2.1.7.2: Relationship Status Enum (for Engagement Score)
  http.get('http://localhost:8080/api/enums/relationship-status', () => {
    return HttpResponse.json([
      { value: 'COLD', label: 'Kein Kontakt' },
      { value: 'CONTACTED', label: 'Erstkontakt hergestellt' },
      { value: 'ENGAGED_SKEPTICAL', label: 'Mehrere Touchpoints, skeptisch' },
      { value: 'ENGAGED_POSITIVE', label: 'Mehrere Touchpoints, positiv' },
      { value: 'TRUSTED', label: 'Vertrauensbasis vorhanden' },
      { value: 'ADVOCATE', label: 'Kämpft aktiv für uns' },
    ]);
  }),

  // Sprint 2.1.7.2: Decision Maker Access Enum (for Engagement Score)
  http.get('http://localhost:8080/api/enums/decision-maker-access', () => {
    return HttpResponse.json([
      { value: 'UNKNOWN', label: 'Entscheider noch nicht identifiziert' },
      { value: 'BLOCKED', label: 'Entscheider bekannt, aber Zugang blockiert' },
      { value: 'INDIRECT', label: 'Zugang über Dritte (Assistent, Mitarbeiter, Partner)' },
      { value: 'DIRECT', label: 'Direkter Kontakt zum Entscheider' },
      { value: 'IS_DECISION_MAKER', label: 'Unser Kontakt IST der Entscheider' },
    ]);
  }),

  // Sprint 2.1.7.2: Urgency Level Enum (for Pain Score)
  http.get('http://localhost:8080/api/enums/urgency-levels', () => {
    return HttpResponse.json([
      { value: 'NORMAL', label: 'Normal (6+ Monate)' },
      { value: 'MEDIUM', label: 'Mittel (1-3 Monaten)' },
      { value: 'HIGH', label: 'Hoch (nächsten Monat)' },
      { value: 'EMERGENCY', label: 'Notfall (sofort)' },
    ]);
  }),

  // Sprint 2.1.7.2: Score Schema Endpoint (Pain, Revenue, Engagement)
  http.get('http://localhost:8080/api/scores/schema', () => {
    return HttpResponse.json([
      // Pain Score Schema
      {
        cardId: 'pain_score',
        title: 'Pain Score',
        subtitle: 'Schmerzpunkte des Kunden bewerten',
        icon: '⚠️',
        order: 1,
        sections: [
          {
            sectionId: 'pain_points',
            title: 'Schmerzpunkte',
            subtitle: 'Herausforderungen des Kunden identifizieren',
            collapsible: false,
            defaultCollapsed: false,
            fields: [
              {
                fieldKey: 'painStaffShortage',
                label: 'Personalmangel / Fachkräftemangel',
                type: 'BOOLEAN',
                gridCols: 6,
                helpText: 'Leidet der Betrieb unter Personalmangel?',
              },
              {
                fieldKey: 'painHighCosts',
                label: 'Hoher Kostendruck',
                type: 'BOOLEAN',
                gridCols: 6,
                helpText: 'Sind die Einkaufskosten zu hoch?',
              },
              {
                fieldKey: 'painFoodWaste',
                label: 'Food Waste / Überproduktion',
                type: 'BOOLEAN',
                gridCols: 6,
                helpText: 'Gibt es Probleme mit Lebensmittelverschwendung?',
              },
              {
                fieldKey: 'painQualityInconsistency',
                label: 'Interne Qualitätsinkonsistenz',
                type: 'BOOLEAN',
                gridCols: 6,
                helpText: 'Gibt es Qualitätsschwankungen bei Produkten?',
              },
              {
                fieldKey: 'painTimePressure',
                label: 'Zeitdruck / Effizienz',
                type: 'BOOLEAN',
                gridCols: 6,
                helpText: 'Besteht Zeitdruck in der Küche/Produktion?',
              },
              {
                fieldKey: 'painSupplierQuality',
                label: 'Qualitätsprobleme beim Lieferanten',
                type: 'BOOLEAN',
                gridCols: 6,
                helpText: 'Gibt es Probleme mit der Qualität des aktuellen Lieferanten?',
              },
              {
                fieldKey: 'painUnreliableDelivery',
                label: 'Unzuverlässige Lieferzeiten',
                type: 'BOOLEAN',
                gridCols: 6,
                helpText: 'Ist die Lieferung des aktuellen Lieferanten unzuverlässig?',
              },
              {
                fieldKey: 'painPoorService',
                label: 'Schlechter Service/Support',
                type: 'BOOLEAN',
                gridCols: 6,
                helpText: 'Ist der Service des aktuellen Lieferanten schlecht?',
              },
              {
                fieldKey: 'urgencyLevel',
                label: 'Dringlichkeitsstufe',
                type: 'ENUM',
                enumSource: '/api/enums/urgency-levels',
                gridCols: 6,
                helpText: 'Wie dringlich ist die Lösung dieser Probleme?',
              },
              {
                fieldKey: 'painNotes',
                label: 'Weitere Details zu Pain-Faktoren (optional)',
                type: 'TEXTAREA',
                gridCols: 12,
                placeholder: 'Beschreiben Sie konkrete Probleme, Auswirkungen oder besondere Umstände...',
                helpText: 'Freitext für zusätzliche Pain Points',
              },
            ],
          },
        ],
      },
      // Revenue Score Schema
      {
        cardId: 'revenue_score',
        title: 'Revenue Score',
        subtitle: 'Umsatzpotenzial des Kunden bewerten',
        icon: '💰',
        order: 2,
        sections: [
          {
            sectionId: 'revenue_potential',
            title: 'Umsatzpotenzial',
            subtitle: 'Bewerten Sie das geschätzte Umsatzpotenzial',
            collapsible: false,
            defaultCollapsed: false,
            fields: [
              {
                fieldKey: 'estimatedVolume',
                label: 'Geschätztes Jahresvolumen (€)',
                type: 'CURRENCY',
                gridCols: 6,
                placeholder: 'z.B. 100000',
                helpText: 'Geschätztes jährliches Einkaufsvolumen Lebensmittel/Getränke',
              },
              {
                fieldKey: 'budgetConfirmed',
                label: 'Budget freigegeben / bestätigt',
                type: 'BOOLEAN',
                gridCols: 12,
                helpText: 'Hat der Kunde Budget für Einkäufe bestätigt?',
              },
            ],
          },
        ],
      },
      // Engagement Score Schema
      {
        cardId: 'engagement_score',
        title: 'Engagement Score',
        subtitle: 'Engagement-Level des Kunden bewerten',
        icon: '🤝',
        order: 3,
        sections: [
          {
            sectionId: 'engagement_metrics',
            title: 'Beziehungsebene',
            subtitle: 'Bewerten Sie die Beziehung zum Kunden',
            collapsible: false,
            defaultCollapsed: false,
            fields: [
              {
                fieldKey: 'relationshipStatus',
                label: 'Beziehungsqualität',
                type: 'ENUM',
                enumSource: '/api/enums/relationship-status',
                gridCols: 6,
                helpText: 'Wie ist der aktuelle Beziehungsstatus?',
              },
              {
                fieldKey: 'decisionMakerAccess',
                label: 'Entscheider-Zugang',
                type: 'ENUM',
                enumSource: '/api/enums/decision-maker-access',
                gridCols: 6,
                helpText: 'Haben wir Zugang zum Entscheider?',
              },
              {
                fieldKey: 'internalChampionName',
                label: 'Fürsprecher im Unternehmen',
                type: 'TEXT',
                gridCols: 6,
                placeholder: 'Name des Fürsprechers',
                helpText: '+30 Punkte wenn vorhanden',
              },
              {
                fieldKey: 'competitorInUse',
                label: 'Aktueller Wettbewerber',
                type: 'TEXT',
                gridCols: 6,
                placeholder: 'z.B. Metro, CHEFS CULINAR',
                helpText: 'Welcher Lieferant wird aktuell genutzt?',
              },
              {
                fieldKey: 'engagementNotes',
                label: 'Notizen zum Engagement (optional)',
                type: 'TEXTAREA',
                gridCols: 12,
                placeholder: 'Weitere Informationen zum Engagement...',
                helpText: 'Freitext für zusätzliche Details',
              },
            ],
          },
        ],
      },
    ]);
  }),
];
