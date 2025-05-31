/**
 * Translation System - TypeScript version
 * Multi-language support for FreshPlan Sales Tool
 */

import type { Language } from '../types';

export interface TranslationStructure {
  nav: {
    demo: string;
    customer: string;
    profile: string;
    offer: string;
    settings: string;
  };
  demo: {
    title: string;
    parameters: string;
    orderValue: string;
    leadTime: string;
    pickup: string;
    pickupInfo: string;
    chain: string;
    chainInfo: string;
    yourSavings: string;
    totalDiscount: string;
    savingsEuro: string;
    toPay: string;
    scenarioTitle: string;
    spontaneous: string;
    planned: string;
    optimal: string;
    days: string;
    delivery: string;
    savings: string;
    baseDiscount: string;
    earlyBooking: string;
    combiTitle: string;
    combiText: string;
    example: string;
    priceStability: string;
    priceStabilityText: string;
    priceStabilityDetail: string;
    kombi: string;
    kombiSpecial: string;
    chainBundle: string;
    chainBundleInfo: string;
    chainSpecial: string;
    scenarioLoaded: string;
  };
  customer: {
    title: string;
    single: string;
    singleDesc: string;
    chain: string;
    chainDesc: string;
    company: string;
    contact: string;
    email: string;
    phone: string;
    address: string;
    street: string;
    postalCode: string;
    city: string;
    industry: string;
    selectIndustry: string;
    locations: string;
    chainName: string;
    numberOfLocations: string;
    avgOrderPerLocation: string;
    totalVolume: string;
    next: string;
    save: string;
    hotel: string;
    altenheim: string;
    krankenhaus: string;
    betriebsrestaurant: string;
    restaurant: string;
    kette: string;
    hotelSpecific: string;
    rooms: string;
    occupancyRate: string;
    breakfastPrice: string;
    altenheimSpecific: string;
    residents: string;
    krankenhausSpecific: string;
    beds: string;
    staffMeals: string;
    betriebsrestaurantSpecific: string;
    employees: string;
    utilizationRate: string;
    restaurantSpecific: string;
    seats: string;
    turnsPerDay: string;
    operatingDays: string;
    type: string;
    mealsPerDay: string;
    saved: string;
    enterData: string;
    basicInfo: string;
    chainDetails: string;
    vendingInterest: string;
    vendingMachineLocations: string;
    expectedTransactions: string;
    contractDetails: string;
    desiredContractStart: string;
    contractDuration: string;
    contractEnd: string;
    months12: string;
    months24: string;
    months36: string;
  };
  profile: {
    title: string;
    customerInfo: string;
    potentialAnalysis: string;
    recommendedProducts: string;
    advantages: string;
    priceStability: string;
    personalService: string;
    flexibleDelivery: string;
    qualityProducts: string;
    estimatedVolume: string;
    potentialSavings: string;
    recommendedContract: string;
    months: string;
    argument1Title: string;
    argument1Desc: string;
    argument2Title: string;
    argument2Desc: string;
    argument3Title: string;
    argument3Desc: string;
    argument4Title: string;
    argument4Desc: string;
    decisionCriteria: string;
    painPoints: string;
    benefits: string;
    approach: string;
    growthPotential: string;
    growthNote: string;
    vendingPotential: string;
    additionalRevenue: string;
    vendingMachines: string;
    transactionsPerDay: string;
    avgTransaction: string;
    scheduleDemo: string;
    scheduleDemoDesc: string;
    createOffer: string;
    createOfferDesc: string;
    sendInfo: string;
    sendInfoDesc: string;
    noData: string;
    pleaseEnterCustomerData: string;
    enterData: string;
    contractValue: string;
    salesStrategy: string;
    keyArguments: string;
    nextSteps: string;
  };
  offer: {
    title: string;
    contractDetails: string;
    contractStart: string;
    contractDuration: string;
    discount: string;
    specialConditions: string;
    notes: string;
    generatePdf: string;
    send: string;
    preview: string;
    pdfGenerated: string;
    offerSent: string;
    noCustomerData: string;
    fillCustomerFirst: string;
    fillSalespersonInfo: string;
    generating: string;
    annualSavings: string;
    noPdfGenerated: string;
    pdfDownloaded: string;
    salespersonName: string;
    salespersonEmail: string;
    salespersonPhone: string;
    includeDocuments: string;
    partnershipAgreement: string;
    discountSystemOverview: string;
    calculationExamples: string;
    pdfPreview: string;
    download: string;
    sendViaEmail: string;
  };
  settings: {
    title: string;
    salesperson: string;
    name: string;
    email: string;
    phone: string;
    company: string;
    monday: string;
    apiToken: string;
    boardId: string;
    testConnection: string;
    save: string;
    saved: string;
    connectionSuccess: string;
    connectionError: string;
    missingToken: string;
    mobile: string;
    defaultValues: string;
    defaultDiscount: string;
    defaultContractDuration: string;
    integrations: string;
    emailIntegration: string;
    smtpServer: string;
    password: string;
    xentralIntegration: string;
    apiUrl: string;
    apiKey: string;
    exportSettings: string;
    importSettings: string;
  };
  common: {
    loading: string;
    error: string;
    success: string;
    cancel: string;
    confirm: string;
    yes: string;
    no: string;
    back: string;
    next: string;
    save: string;
    delete: string;
    edit: string;
    close: string;
    download: string;
    upload: string;
    print: string;
    reset: string;
    search: string;
    filter: string;
    sort: string;
    required: string;
    optional: string;
    tagline: string;
  };
  validation: {
    required: string;
    email: string;
    phone: string;
    postalCode: string;
    number: string;
    min: string;
    max: string;
    minLength: string;
    maxLength: string;
    pattern: string;
    formInvalid: string;
  };
  app: {
    title: string;
  };
}

export const translations: Record<Language, TranslationStructure> = {
  de: {
    app: {
      title: 'FreshPlan Sales Tool'
    },
    nav: {
      demo: 'Rabatt-Demo',
      customer: 'Kundendaten',
      profile: 'Kundenprofil',
      offer: 'Angebot',
      settings: 'Einstellungen'
    },
    demo: {
      title: 'Interaktiver Rabatt-Demonstrator',
      parameters: 'Bestellparameter',
      orderValue: 'Bestellwert (netto)',
      leadTime: 'Vorlaufzeit (Tage)',
      pickup: 'Selbstabholung an der Rampe',
      pickupInfo: 'Sie sparen 2% bei Abholung (ab 5.000€)',
      chain: 'Mehrere Standorte (Filialgeschäft)',
      chainInfo: 'Ermöglicht Wochenbündelung für höhere Rabattstufen',
      yourSavings: 'Ihre Ersparnis',
      totalDiscount: 'Gesamtrabatt:',
      savingsEuro: 'Ersparnis in Euro:',
      toPay: 'Zu zahlen:',
      scenarioTitle: 'Szenario-Vergleich',
      spontaneous: 'Spontanbestellung',
      planned: 'Geplante Bestellung',
      optimal: 'Optimale Planung',
      days: 'Tage',
      delivery: 'Lieferung',
      savings: 'Ersparnis',
      baseDiscount: 'Rabattstufe',
      earlyBooking: 'Frühbucherrabatt',
      combiTitle: 'Kombi-Bestellung (ab 30.000€)',
      combiText: 'Großbestellungen können flexibel auf 2 Liefertermine innerhalb von 4 Wochen aufgeteilt werden. Der Gesamtrabatt gilt für beide Lieferungen!',
      example: 'Beispiel',
      priceStability: '12 Monate Preisstabilität',
      priceStabilityText: 'Ihre vereinbarten Preise bleiben während der gesamten Vertragslaufzeit stabil. Schutz vor Inflation inklusive!',
      priceStabilityDetail: 'Preisanpassung nur bei Indexsteigerung >5% (dann nur der Anteil darüber)',
      kombi: 'Kombi-Bestellung',
      kombiSpecial: '2 Lieferungen',
      chainBundle: 'Kettenbündelung',
      chainBundleInfo: '(kein Rabatt, nur Bündelung)',
      chainSpecial: 'Wochenbündelung',
      scenarioLoaded: 'Szenario geladen'
    },
    customer: {
      title: 'Kundendaten erfassen',
      single: 'Einzelstandort',
      singleDesc: 'Ein Standort',
      chain: 'Kette/Gruppe',
      chainDesc: 'Mehrere Standorte',
      company: 'Firma/Organisation',
      contact: 'Ansprechpartner',
      email: 'E-Mail',
      phone: 'Telefon',
      address: 'Adresse',
      street: 'Straße & Hausnummer',
      postalCode: 'PLZ',
      city: 'Ort',
      industry: 'Branche',
      selectIndustry: 'Bitte wählen',
      locations: 'Standorte',
      chainName: 'Kettename',
      numberOfLocations: 'Anzahl Standorte',
      avgOrderPerLocation: 'Ø Bestellwert pro Standort',
      totalVolume: 'Gesamtvolumen',
      next: 'Weiter',
      save: 'Speichern',
      hotel: 'Hotel',
      altenheim: 'Alten-/Pflegeheim',
      krankenhaus: 'Krankenhaus/Klinik',
      betriebsrestaurant: 'Betriebsrestaurant',
      restaurant: 'Restaurant',
      kette: 'Kette',
      hotelSpecific: 'Hotel Details',
      rooms: 'Zimmer',
      occupancyRate: 'Auslastung (%)',
      breakfastPrice: 'Frühstückspreis (€)',
      altenheimSpecific: 'Heim Details',
      residents: 'Bewohner',
      krankenhausSpecific: 'Krankenhaus Details',
      beds: 'Betten',
      staffMeals: 'Mitarbeiter-Essen/Tag',
      betriebsrestaurantSpecific: 'Betriebsrestaurant Details',
      employees: 'Mitarbeiter',
      utilizationRate: 'Nutzungsrate (%)',
      restaurantSpecific: 'Restaurant Details',
      seats: 'Sitzplätze',
      turnsPerDay: 'Umschlag/Tag',
      operatingDays: 'Betriebstage/Jahr',
      type: 'Typ',
      mealsPerDay: 'Mahlzeiten/Tag',
      saved: 'Kundendaten wurden gespeichert',
      enterData: 'Kundendaten erfassen',
      basicInfo: 'Grunddaten',
      chainDetails: 'Ketten-Details',
      vendingInterest: 'Interesse an Vending-Automaten',
      vendingMachineLocations: 'Anzahl Automaten-Standorte',
      expectedTransactions: 'Erwartete Transaktionen/Tag',
      contractDetails: 'Vertragsdetails',
      desiredContractStart: 'Gewünschter Vertragsbeginn',
      contractDuration: 'Vertragslaufzeit',
      contractEnd: 'Vertragsende',
      months12: '12 Monate',
      months24: '24 Monate',
      months36: '36 Monate'
    },
    profile: {
      title: 'Kundenprofil',
      customerInfo: 'Kundeninformationen',
      potentialAnalysis: 'Potenzialanalyse',
      recommendedProducts: 'Empfohlene Produkte',
      advantages: 'Ihre Vorteile',
      priceStability: 'Preisstabilität für 12 Monate',
      personalService: 'Persönlicher Ansprechpartner',
      flexibleDelivery: 'Flexible Lieferzeiten',
      qualityProducts: 'Premiumqualität aus regionaler Produktion',
      estimatedVolume: 'Geschätztes Jahresvolumen',
      potentialSavings: 'Mögliche Ersparnis',
      recommendedContract: 'Empfohlene Vertragslaufzeit',
      months: 'Monate',
      argument1Title: 'Maximale Ersparnis',
      argument1Desc: 'Profitieren Sie von attraktiven Rabatten auf unser gesamtes Sortiment',
      argument2Title: 'Preisstabilität',
      argument2Desc: 'Keine Preiserhöhungen während der Vertragslaufzeit',
      argument3Title: 'Flexibilität',
      argument3Desc: 'Bestellen Sie nur was Sie brauchen, wann Sie es brauchen',
      argument4Title: 'Persönlicher Service',
      argument4Desc: 'Ihr dedizierter Ansprechpartner für alle Fragen',
      decisionCriteria: 'Entscheidungskriterien',
      painPoints: 'Herausforderungen',
      benefits: 'Ihre Vorteile',
      approach: 'Empfohlene Vorgehensweise',
      growthPotential: 'Wachstumspotenzial',
      growthNote: 'Basierend auf durchschnittlichem Branchenwachstum',
      vendingPotential: 'Vending-Potenzial',
      additionalRevenue: 'Zusätzlicher Umsatz',
      vendingMachines: 'Automaten',
      transactionsPerDay: 'Transaktionen/Tag',
      avgTransaction: 'Ø Transaktion',
      scheduleDemo: 'Demo vereinbaren',
      scheduleDemoDesc: 'Zeigen Sie die Vorteile live',
      createOffer: 'Angebot erstellen',
      createOfferDesc: 'Individuelles PDF generieren',
      sendInfo: 'Info senden',
      sendInfoDesc: 'Zusätzliche Unterlagen versenden',
      noData: 'Keine Kundendaten vorhanden',
      pleaseEnterCustomerData: 'Bitte erfassen Sie zuerst die Kundendaten',
      enterData: 'Daten erfassen',
      contractValue: 'Vertragswert',
      salesStrategy: 'Verkaufsstrategie',
      keyArguments: 'Schlüsselargumente',
      nextSteps: 'Nächste Schritte'
    },
    offer: {
      title: 'Angebot erstellen',
      contractDetails: 'Vertragsdetails',
      contractStart: 'Vertragsbeginn',
      contractDuration: 'Laufzeit',
      discount: 'Vereinbarter Rabatt (%)',
      specialConditions: 'Sonderkonditionen',
      notes: 'Anmerkungen',
      generatePdf: 'PDF erstellen',
      send: 'Angebot senden',
      preview: 'Vorschau',
      pdfGenerated: 'PDF wurde erstellt',
      offerSent: 'Angebot wurde versendet',
      noCustomerData: 'Bitte erfassen Sie zuerst Kundendaten',
      fillCustomerFirst: 'Bitte füllen Sie zuerst die Kundendaten aus',
      fillSalespersonInfo: 'Bitte geben Sie Ihre Verkäuferinformationen ein',
      generating: 'PDF wird generiert...',
      annualSavings: 'Jährliche Ersparnis',
      noPdfGenerated: 'Kein PDF vorhanden. Bitte generieren Sie zuerst ein Angebot.',
      pdfDownloaded: 'PDF wurde heruntergeladen',
      salespersonName: 'Verkäufer Name',
      salespersonEmail: 'Verkäufer E-Mail',
      salespersonPhone: 'Verkäufer Telefon',
      includeDocuments: 'Dokumente einschließen:',
      partnershipAgreement: 'Partnerschaftsvereinbarung',
      discountSystemOverview: 'Rabattsystem-Übersicht',
      calculationExamples: 'Berechnungsbeispiele',
      pdfPreview: 'PDF Vorschau',
      download: 'Download',
      sendViaEmail: 'Per E-Mail senden'
    },
    settings: {
      title: 'Einstellungen',
      salesperson: 'Verkäufer-Informationen',
      name: 'Name',
      email: 'E-Mail',
      phone: 'Telefon',
      company: 'Firma',
      monday: 'Monday.com Integration',
      apiToken: 'API Token',
      boardId: 'Board ID',
      testConnection: 'Verbindung testen',
      save: 'Speichern',
      saved: 'Einstellungen gespeichert',
      connectionSuccess: 'Verbindung erfolgreich',
      connectionError: 'Verbindungsfehler',
      missingToken: 'Bitte geben Sie einen API Token ein',
      mobile: 'Mobil',
      defaultValues: 'Standardwerte',
      defaultDiscount: 'Standard-Rabatt (%)',
      defaultContractDuration: 'Standard-Vertragslaufzeit',
      integrations: 'Integrationen',
      emailIntegration: 'E-Mail Integration',
      smtpServer: 'SMTP Server',
      password: 'Passwort',
      xentralIntegration: 'Xentral Integration',
      apiUrl: 'API URL',
      apiKey: 'API Key',
      exportSettings: 'Einstellungen exportieren',
      importSettings: 'Einstellungen importieren'
    },
    common: {
      loading: 'Laden...',
      error: 'Fehler',
      success: 'Erfolg',
      cancel: 'Abbrechen',
      confirm: 'Bestätigen',
      yes: 'Ja',
      no: 'Nein',
      back: 'Zurück',
      next: 'Weiter',
      save: 'Speichern',
      delete: 'Löschen',
      edit: 'Bearbeiten',
      close: 'Schließen',
      download: 'Herunterladen',
      upload: 'Hochladen',
      print: 'Drucken',
      reset: 'Zurücksetzen',
      search: 'Suchen',
      filter: 'Filtern',
      sort: 'Sortieren',
      required: 'Pflichtfeld',
      optional: 'Optional',
      tagline: 'So einfach, schnell und lecker!'
    },
    validation: {
      required: 'Dieses Feld ist erforderlich',
      email: 'Bitte geben Sie eine gültige E-Mail-Adresse ein',
      phone: 'Bitte geben Sie eine gültige Telefonnummer ein',
      postalCode: 'Bitte geben Sie eine gültige Postleitzahl ein',
      number: 'Bitte geben Sie eine gültige Zahl ein',
      min: 'Der Wert muss mindestens {min} sein',
      max: 'Der Wert darf höchstens {max} sein',
      minLength: 'Mindestens {minLength} Zeichen erforderlich',
      maxLength: 'Maximal {maxLength} Zeichen erlaubt',
      pattern: 'Das Format ist ungültig',
      formInvalid: 'Bitte überprüfen Sie Ihre Eingaben'
    }
  },
  en: {
    app: {
      title: 'FreshPlan Sales Tool'
    },
    nav: {
      demo: 'Discount Demo',
      customer: 'Customer Data',
      profile: 'Customer Profile',
      offer: 'Offer',
      settings: 'Settings'
    },
    demo: {
      title: 'Interactive Discount Demonstrator',
      parameters: 'Order Parameters',
      orderValue: 'Order Value (net)',
      leadTime: 'Lead Time (days)',
      pickup: 'Self Pickup at Ramp',
      pickupInfo: 'Save 2% on pickup (from €5,000)',
      chain: 'Multiple Locations (Branch Business)',
      chainInfo: 'Enables weekly bundling for higher discount levels',
      yourSavings: 'Your Savings',
      totalDiscount: 'Total Discount:',
      savingsEuro: 'Savings in Euro:',
      toPay: 'To Pay:',
      scenarioTitle: 'Scenario Comparison',
      spontaneous: 'Spontaneous Order',
      planned: 'Planned Order',
      optimal: 'Optimal Planning',
      days: 'days',
      delivery: 'Delivery',
      savings: 'Savings',
      baseDiscount: 'Discount Level',
      earlyBooking: 'Early Booking Discount',
      combiTitle: 'Combo Order (from €30,000)',
      combiText: 'Large orders can be flexibly split into 2 delivery dates within 4 weeks. The total discount applies to both deliveries!',
      example: 'Example',
      priceStability: '12 Months Price Stability',
      priceStabilityText: 'Your agreed prices remain stable throughout the entire contract period. Protection against inflation included!',
      priceStabilityDetail: 'Price adjustment only for index increase >5% (then only the portion above)',
      kombi: 'Combo Order',
      kombiSpecial: '2 Deliveries',
      chainBundle: 'Chain Bundling',
      chainBundleInfo: '(no discount, bundling only)',
      chainSpecial: 'Weekly Bundling',
      scenarioLoaded: 'Scenario loaded'
    },
    customer: {
      title: 'Capture Customer Data',
      single: 'Single Location',
      singleDesc: 'One location',
      chain: 'Chain/Group',
      chainDesc: 'Multiple locations',
      company: 'Company/Organization',
      contact: 'Contact Person',
      email: 'Email',
      phone: 'Phone',
      address: 'Address',
      street: 'Street & Number',
      postalCode: 'Postal Code',
      city: 'City',
      industry: 'Industry',
      selectIndustry: 'Please select',
      locations: 'Locations',
      chainName: 'Chain Name',
      numberOfLocations: 'Number of Locations',
      avgOrderPerLocation: 'Avg. Order per Location',
      totalVolume: 'Total Volume',
      next: 'Next',
      save: 'Save',
      hotel: 'Hotel',
      altenheim: 'Nursing Home',
      krankenhaus: 'Hospital/Clinic',
      betriebsrestaurant: 'Company Restaurant',
      restaurant: 'Restaurant',
      kette: 'Chain',
      hotelSpecific: 'Hotel Details',
      rooms: 'Rooms',
      occupancyRate: 'Occupancy Rate (%)',
      breakfastPrice: 'Breakfast Price (€)',
      altenheimSpecific: 'Home Details',
      residents: 'Residents',
      krankenhausSpecific: 'Hospital Details',
      beds: 'Beds',
      staffMeals: 'Staff Meals/Day',
      betriebsrestaurantSpecific: 'Company Restaurant Details',
      employees: 'Employees',
      utilizationRate: 'Utilization Rate (%)',
      restaurantSpecific: 'Restaurant Details',
      seats: 'Seats',
      turnsPerDay: 'Turns/Day',
      operatingDays: 'Operating Days/Year',
      type: 'Type',
      mealsPerDay: 'Meals/Day',
      saved: 'Customer data has been saved',
      basicInfo: 'Basic Information',
      chainDetails: 'Chain Details',
      vendingInterest: 'Interest in Vending Machines',
      vendingMachineLocations: 'Number of Machine Locations',
      expectedTransactions: 'Expected Transactions/Day',
      contractDetails: 'Contract Details',
      desiredContractStart: 'Desired Contract Start',
      contractDuration: 'Contract Duration',
      contractEnd: 'Contract End',
      months12: '12 Months',
      months24: '24 Months',
      months36: '36 Months',
      enterData: 'Enter Data'
    },
    profile: {
      title: 'Customer Profile',
      customerInfo: 'Customer Information',
      potentialAnalysis: 'Potential Analysis',
      recommendedProducts: 'Recommended Products',
      advantages: 'Your Advantages',
      priceStability: 'Price stability for 12 months',
      personalService: 'Personal contact person',
      flexibleDelivery: 'Flexible delivery times',
      qualityProducts: 'Premium quality from regional production',
      estimatedVolume: 'Estimated Annual Volume',
      potentialSavings: 'Potential Savings',
      recommendedContract: 'Recommended Contract Duration',
      months: 'months',
      argument1Title: 'Maximum Savings',
      argument1Desc: 'Benefit from attractive discounts on our entire range',
      argument2Title: 'Price Stability',
      argument2Desc: 'No price increases during the contract period',
      argument3Title: 'Flexibility',
      argument3Desc: 'Order only what you need, when you need it',
      argument4Title: 'Personal Service',
      argument4Desc: 'Your dedicated contact for all questions',
      decisionCriteria: 'Decision Criteria',
      painPoints: 'Challenges',
      benefits: 'Your Benefits',
      approach: 'Recommended Approach',
      growthPotential: 'Growth Potential',
      growthNote: 'Based on average industry growth',
      vendingPotential: 'Vending Potential',
      additionalRevenue: 'Additional Revenue',
      vendingMachines: 'Vending Machines',
      transactionsPerDay: 'Transactions/Day',
      avgTransaction: 'Avg Transaction',
      scheduleDemo: 'Schedule Demo',
      scheduleDemoDesc: 'Show the benefits live',
      createOffer: 'Create Offer',
      createOfferDesc: 'Generate individual PDF',
      sendInfo: 'Send Info',
      sendInfoDesc: 'Send additional documents',
      noData: 'No customer data available',
      pleaseEnterCustomerData: 'Please enter customer data first',
      enterData: 'Enter data',
      contractValue: 'Contract Value',
      salesStrategy: 'Sales Strategy',
      keyArguments: 'Key Arguments',
      nextSteps: 'Next Steps'
    },
    offer: {
      title: 'Create Offer',
      contractDetails: 'Contract Details',
      contractStart: 'Contract Start',
      contractDuration: 'Duration',
      discount: 'Agreed Discount (%)',
      specialConditions: 'Special Conditions',
      notes: 'Notes',
      generatePdf: 'Generate PDF',
      send: 'Send Offer',
      preview: 'Preview',
      pdfGenerated: 'PDF has been created',
      offerSent: 'Offer has been sent',
      noCustomerData: 'Please enter customer data first',
      fillCustomerFirst: 'Please fill in customer data first',
      fillSalespersonInfo: 'Please enter your salesperson information',
      generating: 'Generating PDF...',
      annualSavings: 'Annual Savings',
      noPdfGenerated: 'No PDF available. Please generate an offer first.',
      pdfDownloaded: 'PDF has been downloaded',
      salespersonName: 'Salesperson Name',
      salespersonEmail: 'Salesperson Email',
      salespersonPhone: 'Salesperson Phone',
      includeDocuments: 'Include Documents:',
      partnershipAgreement: 'Partnership Agreement',
      discountSystemOverview: 'Discount System Overview',
      calculationExamples: 'Calculation Examples',
      pdfPreview: 'PDF Preview',
      download: 'Download',
      sendViaEmail: 'Send via Email'
    },
    settings: {
      title: 'Settings',
      salesperson: 'Salesperson Information',
      name: 'Name',
      email: 'Email',
      phone: 'Phone',
      company: 'Company',
      monday: 'Monday.com Integration',
      apiToken: 'API Token',
      boardId: 'Board ID',
      testConnection: 'Test Connection',
      save: 'Save',
      saved: 'Settings saved',
      connectionSuccess: 'Connection successful',
      connectionError: 'Connection error',
      missingToken: 'Please enter an API token',
      mobile: 'Mobile',
      defaultValues: 'Default Values',
      defaultDiscount: 'Default Discount (%)',
      defaultContractDuration: 'Default Contract Duration',
      integrations: 'Integrations',
      emailIntegration: 'Email Integration',
      smtpServer: 'SMTP Server',
      password: 'Password',
      xentralIntegration: 'Xentral Integration',
      apiUrl: 'API URL',
      apiKey: 'API Key',
      exportSettings: 'Export Settings',
      importSettings: 'Import Settings'
    },
    common: {
      loading: 'Loading...',
      error: 'Error',
      success: 'Success',
      cancel: 'Cancel',
      confirm: 'Confirm',
      yes: 'Yes',
      no: 'No',
      back: 'Back',
      next: 'Next',
      save: 'Save',
      delete: 'Delete',
      edit: 'Edit',
      close: 'Close',
      download: 'Download',
      upload: 'Upload',
      print: 'Print',
      reset: 'Reset',
      search: 'Search',
      filter: 'Filter',
      sort: 'Sort',
      required: 'Required',
      optional: 'Optional',
      tagline: 'So easy, fast and delicious!'
    },
    validation: {
      required: 'This field is required',
      email: 'Please enter a valid email address',
      phone: 'Please enter a valid phone number',
      postalCode: 'Please enter a valid postal code',
      number: 'Please enter a valid number',
      min: 'Value must be at least {min}',
      max: 'Value must be at most {max}',
      minLength: 'At least {minLength} characters required',
      maxLength: 'Maximum {maxLength} characters allowed',
      pattern: 'Invalid format',
      formInvalid: 'Please check your inputs'
    }
  }
};

// Helper function for nested translations
export function getTranslation(key: string, lang: Language = 'de'): string {
  const keys = key.split('.');
  let value: any = translations[lang];
  
  for (const k of keys) {
    value = value?.[k];
    if (!value) break;
  }
  
  // Fallback to German if not found
  if (!value && lang !== 'de') {
    value = keys.reduce((obj: any, k) => obj?.[k], translations.de as any);
  }
  
  return value || key;
}