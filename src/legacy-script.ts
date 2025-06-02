/**
 * Legacy JavaScript code from reference-original.html
 * Phase 1b - Direct port of original functionality
 */

import { LocationDetail, createLocationDetailCard } from './templates/locationDetailsTemplates';
import { getIndustryLocationFields } from './templates/industryLocationTemplates';

// Make functions available globally for inline onclick handlers
declare global {
  interface Window {
    updateCalculator: () => void;
    changeLanguage: (lang: string) => void;
    handleClearForm: () => void;
    handleSaveForm: () => void;
    loadExample: (type: string) => void;
    formatCurrency: (input: HTMLInputElement) => void;
    toggleLocationDetailsTab: (show: boolean) => void;
    addLocationDetail: () => void;
    startCreditCheck: () => void;
    requestManagementApproval: () => void;
    updateTotalLocations: () => void;
  }
}

export function initLegacyScript(): void {
  // Translations
  const translations: Record<string, Record<string, string>> = {
    de: {
      // Header
      clearForm: 'Formular leeren',
      save: 'Speichern',
      
      // Navigation
      calculator: 'Rabattrechner',
      customer: 'Kundendaten',
      locations: 'Standorte',
      locationDetails: 'Standort-Details',
      creditcheck: 'Bonitätsprüfung',
      profile: 'Profil',
      offer: 'Angebot',
      settings: 'Einstellungen',
      
      // Calculator
      calculatorTitle: 'FreshPlan Rabattrechner',
      orderValue: 'Bestellwert',
      leadTime: 'Vorlaufzeit',
      days: 'Tage',
      pickup: 'Abholung (Mindestbestellwert: 5.000€ netto)',
      baseDiscount: 'Basisrabatt',
      earlyDiscount: 'Frühbucher',
      pickupDiscount: 'Abholung',
      totalDiscount: 'Gesamtrabatt',
      savings: 'Ersparnis',
      finalPrice: 'Endpreis',
      
      // Customer
      customerTitle: 'Kundendaten erfassen',
      basicData: 'Grunddaten',
      companyName: 'Firmenname*',
      legalForm: 'Rechtsform*',
      customerType: 'Kundentyp*',
      industry: 'Branche*',
      chainCustomer: 'Kettenkunde',
      customerNumberInternal: 'Kundennummer (intern)',
      addressData: 'Adressdaten',
      streetAndNumber: 'Straße und Hausnummer*',
      postalCode: 'PLZ*',
      city: 'Ort*',
      contactPerson: 'Ansprechpartner',
      contactName: 'Name*',
      contactPosition: 'Position',
      contactPhone: 'Telefon*',
      contactEmail: 'E-Mail*',
      businessData: 'Geschäftsdaten',
      expectedAnnualVolume: 'Erwartetes Jahresvolumen (€)*',
      paymentMethod: 'Zahlungsart*',
      additionalInfo: 'Zusatzinformationen',
      notes: 'Notizen',
      customField1: 'Freifeld 1',
      customField2: 'Freifeld 2',
      
      // Common form elements
      pleaseSelect: 'Bitte wählen',
      yes: 'Ja',
      no: 'Nein',
      
      // Legal forms
      legalFormGmbH: 'GmbH',
      legalFormAG: 'AG',
      legalFormGbR: 'GbR',
      legalFormEinzel: 'Einzelunternehmen',
      legalFormKG: 'KG',
      legalFormOHG: 'OHG',
      legalFormUG: 'UG',
      
      // Customer types
      newCustomer: 'Neukunde',
      existingCustomer: 'Bestandskunde',
      
      // Industries
      industryHotel: 'Hotel',
      industryHospital: 'Klinik',
      industryNursingHome: 'Seniorenresidenz',
      industryCafeteria: 'Betriebsrestaurant',
      industryRestaurant: 'Restaurant',
      
      // Payment methods
      paymentPrepayment: 'Vorkasse',
      paymentCash: 'Barzahlung',
      paymentInvoice: 'Rechnung (30 Tage netto)',
      
      // Placeholders
      volumePlaceholder: 'z.B. 500.000',
      notesPlaceholder: 'Interne Notizen zum Kunden...',
      
      // Discount system
      discountSystem: 'FreshPlan-Rabattsystem',
      from: 'ab',
      from10days: 'ab 10 Tage',
      from15days: 'ab 15 Tage',
      from30days: 'ab 30 Tage',
      earlyBookingDiscount: 'Frühbucherrabatt',
      maxTotalDiscount: 'Maximaler Gesamtrabatt: 15%',
      
      // Chain customer
      chainCustomerRegulation: 'Kettenkundenregelung',
      chainCustomerDesc: 'Für Unternehmen mit mehreren Standorten (z.B. Hotel- oder Klinikgruppen):',
      optionA: 'Option A:',
      optionADesc: 'Bestellungen verschiedener Standorte innerhalb einer Woche werden zusammengerechnet',
      optionB: 'Option B:',
      optionBDesc: 'Zentrale Bestellung mit Mehrfachauslieferung',
      
      // Example scenarios
      exampleScenarios: 'Beispielszenarien',
      hotelChain: 'Hotelkette',
      clinicGroup: 'Klinikgruppe',
      restaurant: 'Restaurant',
      '21days': '21 Tage',
      '30days': '30 Tage',
      '14days': '14 Tage',
      delivery: 'Lieferung',
      discount: 'Rabatt',
      
      // Language
      languageDE: 'Deutsch',
      languageEN: 'English',
      tagline: 'So einfach, schnell und lecker!',
      
      // Locations
      locationsTitle: 'Standortverwaltung',
      locationOverview: 'Kettenübersicht',
      totalLocations: 'Gesamtanzahl Standorte',
      managementType: 'Verwaltungstyp',
      centralManagement: 'Zentrale Verwaltung',
      decentralManagement: 'Dezentrale Verwaltung',
      detailedCapture: 'Detaillierte Erfassung',
      captureDetailsLabel: 'Standorte detailliert erfassen',
      captureDetailsDesc: 'Erfassen Sie jeden Standort einzeln mit Adresse, Ansprechpartner und spezifischen Eigenschaften.',
      vendingConcept: 'Vending-Konzept',
      vendingInterestLabel: 'Interesse an Freshfoodz-Vending?',
      vendingLocations: 'Anzahl Standorte mit Vending',
      vendingType: 'Automatentyp',
      vendingSnack: 'Snack-Automat',
      vendingFresh: 'Frische-Automat',
      vendingCombi: 'Kombi-Automat',
      
      // Location details
      locationDetailsTitle: 'Detaillierte Standorterfassung',
      totalLocationsDetail: 'Erfasste Standorte:',
      addLocation: '+ Standort hinzufügen',
      syncWarningTitle: '⚠️ Abweichung erkannt',
      syncWarningText: 'Die Anzahl und Kategorien der erfassten Standorte stimmen nicht mit den Angaben auf der Haupt-Standortseite überein.',
      noLocationsYet: 'Noch keine Standorte erfasst.',
      clickAddLocation: 'Klicken Sie auf den Button oben um zu beginnen.',
      
      // Location details form fields (from de.json)
      'locationDetails.locationTitle': 'Standort',
      'locationDetails.locationName': 'Standortname*',
      'locationDetails.category': 'Kategorie',
      'locationDetails.mainLocation': 'Hauptstandort',
      'locationDetails.branch': 'Filiale',
      'locationDetails.externalOffice': 'Außenstelle',
      'locationDetails.streetAndNumber': 'Straße und Hausnummer*',
      'locationDetails.postalCode': 'PLZ*',
      'locationDetails.city': 'Ort*',
      'locationDetails.contactName': 'Ansprechpartner Name',
      'locationDetails.contactPhone': 'Telefon',
      'locationDetails.contactEmail': 'E-Mail',
      'common.remove': 'Entfernen',
      'common.pleaseSelect': 'Bitte wählen',
      
      // Credit check
      creditCheckTitle: 'Bonitätsprüfung',
      creditCheckDescription: 'Die Bonitätsprüfung ist nur für <strong>Neukunden</strong> relevant, die auf Rechnung zahlen möchten.',
      customerName: 'Kundenname',
      creditLimitLabel: 'Kreditlimit/Offene Posten (€)',
      registrationNumber: 'Handelsregisternummer*',
      vatId: 'USt-IdNr.*',
      companySize: 'Unternehmensgröße*',
      paymentHistory: 'Bisherige Zahlungserfahrungen',
      companySizeSmall: 'Klein (bis 50 Mitarbeiter)',
      companySizeMedium: 'Mittel (50-250 Mitarbeiter)',
      companySizeLarge: 'Groß (über 250 Mitarbeiter)',
      paymentHistoryNoInfo: 'Keine Angabe',
      paymentHistoryNew: 'Neukunde - keine Erfahrungen',
      paymentHistoryGood: 'Gut - pünktliche Zahlung',
      paymentHistoryMedium: 'Mittel - gelegentliche Verzögerungen',
      paymentHistoryBad: 'Schlecht - häufige Mahnungen',
      checkStatus: 'Prüfungsstatus',
      status: 'Status',
      notChecked: 'Nicht geprüft',
      creditLimit: 'Kreditlimit',
      rating: 'Bewertung',
      startCreditCheck: 'Bonitätsprüfung starten',
      requestManagement: 'Anfrage an Geschäftsleitung',
      newCustomers: 'Neukunden',
      
      // Profile & Offer
      profileTitle: 'Kundenprofil',
      profileDescription: 'Hier werden die Kundenprofile angezeigt und verwaltet.',
      offerTitle: 'Angebot erstellen',
      offerDescription: 'Hier können Sie PDF-Angebote für Ihre Kunden erstellen.',
      
      // Settings
      settingsTitle: 'Einstellungen',
      settingsDescription: 'Hier können Sie Ihre persönlichen Daten und Standardwerte anpassen.',
      
      // Validation messages
      fillRequired: 'Bitte füllen Sie alle Pflichtfelder aus:',
      saveSuccess: 'Daten wurden erfolgreich gespeichert!',
      nothingToSave: 'Aktuell gibt es auf dieser Seite nichts zu speichern.',
      confirmClear: 'Möchten Sie wirklich alle Daten löschen?',
      clearOnlyCustomer: 'Die Funktion "Formular leeren" ist nur im Kundendaten-Tab verfügbar.',
      confirmRemoveLocation: 'Möchten Sie diesen Standort wirklich entfernen?',
      
      // Industry-specific location fields
      'locations.services': 'Services',
      
      // Hotel
      'locations.hotel.categorization': 'Hotel-Kategorisierung',
      'locations.hotel.small': 'Klein (bis 50 Zimmer)',
      'locations.hotel.medium': 'Mittel (51-150 Zimmer)',
      'locations.hotel.large': 'Groß (über 150 Zimmer)',
      'locations.hotel.unit': 'Hotels',
      'locations.hotel.breakfast': 'Frühstücksservice',
      'locations.hotel.restaurant': 'Restaurant à la carte',
      'locations.hotel.roomService': 'Room Service',
      'locations.hotel.banquet': 'Bankett/Veranstaltungen',
      'locations.hotel.ofPrefix': 'von',
      
      // Clinic
      'locations.clinic.categorization': 'Klinik-Kategorisierung',
      'locations.clinic.small': 'Klein (bis 150 Betten)',
      'locations.clinic.medium': 'Mittel (151-400 Betten)',
      'locations.clinic.large': 'Groß (über 400 Betten)',
      'locations.clinic.unit': 'Kliniken',
      'locations.clinic.privatePatientShare': 'Anteil Privatpatienten',
      'locations.clinic.patientCatering': 'Patientenverpflegung',
      'locations.clinic.staffCatering': 'Mitarbeiterverpflegung',
      'locations.clinic.ofPrefix': 'von',
      
      // Senior Residence
      'locations.senior.categorization': 'Seniorenresidenz-Kategorisierung',
      'locations.senior.small': 'Klein (bis 50 Bewohner)',
      'locations.senior.medium': 'Mittel (51-150 Bewohner)',
      'locations.senior.large': 'Groß (über 150 Bewohner)',
      'locations.senior.unit': 'Residenzen',
      'locations.senior.careFocus': 'Pflegeschwerpunkt',
      'locations.senior.careMixed': 'Gemischt',
      'locations.senior.careAssisted': 'Betreutes Wohnen',
      'locations.senior.careNursing': 'Vollpflege',
      'locations.senior.catering': 'Verpflegung',
      'locations.senior.fullCatering': 'Vollverpflegung',
      'locations.senior.partialCatering': 'Teilverpflegung',
      'locations.senior.specialDiet': 'Sonderkostform',
      'locations.senior.ofPrefix': 'von',
      
      // Restaurant
      'locations.restaurant.categorization': 'Restaurant-Kategorisierung',
      'locations.restaurant.small': 'Klein (bis 50 Sitzplätze)',
      'locations.restaurant.medium': 'Mittel (51-150 Sitzplätze)',
      'locations.restaurant.large': 'Groß (über 150 Sitzplätze)',
      'locations.restaurant.unit': 'Restaurants',
      'locations.restaurant.alaCarte': 'À la carte',
      'locations.restaurant.banquet': 'Bankett/Veranstaltungen',
      'locations.restaurant.ofPrefix': 'von',
      
      // Cafeteria
      'locations.cafeteria.categorization': 'Betriebsrestaurant-Kategorisierung',
      'locations.cafeteria.small': 'Klein (bis 200 MA)',
      'locations.cafeteria.medium': 'Mittel (201-500 MA)',
      'locations.cafeteria.large': 'Groß (über 500 MA)',
      'locations.cafeteria.unit': 'Standorte',
      'locations.cafeteria.serviceScope': 'Serviceumfang',
      'locations.cafeteria.breakfast': 'Frühstück',
      'locations.cafeteria.lunch': 'Mittagessen',
      'locations.cafeteria.dinner': 'Abendessen',
      'locations.cafeteria.ofPrefix': 'von',
      
      // Common nested keys for i18nModule compatibility
      'common.addLocation': '+ Standort hinzufügen',
      'common.clearForm': 'Formular leeren',
      'common.save': 'Speichern'
    },
    en: {
      // Header
      clearForm: 'Clear Form',
      save: 'Save',
      
      // Navigation
      calculator: 'Discount Calculator',
      customer: 'Customer Data',
      locations: 'Locations',
      locationDetails: 'Location Details',
      creditcheck: 'Credit Check',
      profile: 'Profile',
      offer: 'Offer',
      settings: 'Settings',
      
      // Calculator
      calculatorTitle: 'FreshPlan Discount Calculator',
      orderValue: 'Order Value',
      leadTime: 'Lead Time',
      days: 'Days',
      pickup: 'Pickup (Minimum order: €5,000 net)',
      baseDiscount: 'Base Discount',
      earlyDiscount: 'Early Booking',
      pickupDiscount: 'Pickup',
      totalDiscount: 'Total Discount',
      savings: 'Savings',
      finalPrice: 'Final Price',
      
      // Customer
      customerTitle: 'Enter Customer Data',
      basicData: 'Basic Data',
      companyName: 'Company Name*',
      legalForm: 'Legal Form*',
      customerType: 'Customer Type*',
      industry: 'Industry*',
      chainCustomer: 'Chain Customer',
      customerNumberInternal: 'Customer Number (internal)',
      addressData: 'Address Data',
      streetAndNumber: 'Street and Number*',
      postalCode: 'Postal Code*',
      city: 'City*',
      contactPerson: 'Contact Person',
      contactName: 'Name*',
      contactPosition: 'Position',
      contactPhone: 'Phone*',
      contactEmail: 'Email*',
      businessData: 'Business Data',
      expectedAnnualVolume: 'Expected Annual Volume (€)*',
      paymentMethod: 'Payment Method*',
      additionalInfo: 'Additional Information',
      notes: 'Notes',
      customField1: 'Custom Field 1',
      customField2: 'Custom Field 2',
      
      // Common form elements
      pleaseSelect: 'Please select',
      yes: 'Yes',
      no: 'No',
      
      // Legal forms
      legalFormGmbH: 'GmbH (Ltd.)',
      legalFormAG: 'AG (Inc.)',
      legalFormGbR: 'GbR (Partnership)',
      legalFormEinzel: 'Sole Proprietorship',
      legalFormKG: 'KG (Limited Partnership)',
      legalFormOHG: 'OHG (General Partnership)',
      legalFormUG: 'UG (Mini-GmbH)',
      
      // Customer types
      newCustomer: 'New Customer',
      existingCustomer: 'Existing Customer',
      
      // Industries
      industryHotel: 'Hotel',
      industryHospital: 'Hospital',
      industryNursingHome: 'Nursing Home',
      industryCafeteria: 'Company Restaurant',
      industryRestaurant: 'Restaurant',
      
      // Payment methods
      paymentPrepayment: 'Prepayment',
      paymentCash: 'Cash Payment',
      paymentInvoice: 'Invoice (30 days net)',
      
      // Placeholders
      volumePlaceholder: 'e.g. 500,000',
      notesPlaceholder: 'Internal notes about the customer...',
      
      // Discount system
      discountSystem: 'FreshPlan Discount System',
      from: 'from',
      from10days: 'from 10 days',
      from15days: 'from 15 days',
      from30days: 'from 30 days',
      earlyBookingDiscount: 'Early Booking Discount',
      maxTotalDiscount: 'Maximum Total Discount: 15%',
      
      // Chain customer
      chainCustomerRegulation: 'Chain Customer Regulation',
      chainCustomerDesc: 'For companies with multiple locations (e.g. hotel or hospital groups):',
      optionA: 'Option A:',
      optionADesc: 'Orders from different locations within a week are combined',
      optionB: 'Option B:',
      optionBDesc: 'Central order with multiple deliveries',
      
      // Example scenarios
      exampleScenarios: 'Example Scenarios',
      hotelChain: 'Hotel Chain',
      clinicGroup: 'Hospital Group',
      restaurant: 'Restaurant',
      '21days': '21 Days',
      '30days': '30 Days',
      '14days': '14 Days',
      delivery: 'Delivery',
      discount: 'Discount',
      
      // Language
      languageDE: 'German',
      languageEN: 'English',
      tagline: 'So simple, fast and delicious!',
      
      // Locations
      locationsTitle: 'Location Management',
      locationOverview: 'Chain Overview',
      totalLocations: 'Total Number of Locations',
      managementType: 'Management Type',
      centralManagement: 'Central Management',
      decentralManagement: 'Decentralized Management',
      detailedCapture: 'Detailed Capture',
      captureDetailsLabel: 'Capture locations in detail',
      captureDetailsDesc: 'Capture each location individually with address, contact person and specific properties.',
      vendingConcept: 'Vending Concept',
      vendingInterestLabel: 'Interest in Freshfoodz Vending?',
      vendingLocations: 'Number of Locations with Vending',
      vendingType: 'Vending Machine Type',
      vendingSnack: 'Snack Machine',
      vendingFresh: 'Fresh Machine',
      vendingCombi: 'Combination Machine',
      
      // Location details
      locationDetailsTitle: 'Detailed Location Capture',
      totalLocationsDetail: 'Captured Locations:',
      addLocation: '+ Add Location',
      syncWarningTitle: '⚠️ Discrepancy Detected',
      syncWarningText: 'The number and categories of captured locations do not match the information on the main location page.',
      noLocationsYet: 'No locations captured yet.',
      clickAddLocation: 'Click the button above to begin.',
      
      // Location details form fields (from de.json)
      'locationDetails.locationTitle': 'Location',
      'locationDetails.locationName': 'Location Name*',
      'locationDetails.category': 'Category',
      'locationDetails.mainLocation': 'Main Location',
      'locationDetails.branch': 'Branch',
      'locationDetails.externalOffice': 'External Office',
      'locationDetails.streetAndNumber': 'Street and Number*',
      'locationDetails.postalCode': 'Postal Code*',
      'locationDetails.city': 'City*',
      'locationDetails.contactName': 'Contact Name',
      'locationDetails.contactPhone': 'Phone',
      'locationDetails.contactEmail': 'Email',
      'common.remove': 'Remove',
      'common.pleaseSelect': 'Please select',
      
      // Credit check
      creditCheckTitle: 'Credit Check',
      creditCheckDescription: 'Credit check is only relevant for <strong>new customers</strong> who want to pay by invoice.',
      customerName: 'Customer Name',
      creditLimitLabel: 'Credit Limit/Open Items (€)',
      registrationNumber: 'Trade Register Number*',
      vatId: 'VAT ID*',
      companySize: 'Company Size*',
      paymentHistory: 'Previous Payment Experience',
      companySizeSmall: 'Small (up to 50 employees)',
      companySizeMedium: 'Medium (50-250 employees)',
      companySizeLarge: 'Large (over 250 employees)',
      paymentHistoryNoInfo: 'No information',
      paymentHistoryNew: 'New customer - no experience',
      paymentHistoryGood: 'Good - timely payment',
      paymentHistoryMedium: 'Medium - occasional delays',
      paymentHistoryBad: 'Poor - frequent reminders',
      checkStatus: 'Check Status',
      status: 'Status',
      notChecked: 'Not checked',
      creditLimit: 'Credit Limit',
      rating: 'Rating',
      startCreditCheck: 'Start Credit Check',
      requestManagement: 'Request to Management',
      newCustomers: 'New Customers',
      
      // Profile & Offer
      profileTitle: 'Customer Profile',
      profileDescription: 'Customer profiles are displayed and managed here.',
      offerTitle: 'Create Offer',
      offerDescription: 'Here you can create PDF offers for your customers.',
      
      // Settings
      settingsTitle: 'Settings',
      settingsDescription: 'Here you can adjust your personal data and default values.',
      
      // Validation messages
      fillRequired: 'Please fill in all required fields:',
      saveSuccess: 'Data saved successfully!',
      nothingToSave: 'Nothing to save on this page.',
      confirmClear: 'Do you really want to delete all data?',
      clearOnlyCustomer: 'The "Clear Form" function is only available in the Customer Data tab.',
      confirmRemoveLocation: 'Do you really want to remove this location?',
      
      // Industry-specific location fields
      'locations.services': 'Services',
      
      // Hotel
      'locations.hotel.categorization': 'Hotel Categorization',
      'locations.hotel.small': 'Small (up to 50 rooms)',
      'locations.hotel.medium': 'Medium (51-150 rooms)',
      'locations.hotel.large': 'Large (over 150 rooms)',
      'locations.hotel.unit': 'Hotels',
      'locations.hotel.breakfast': 'Breakfast Service',
      'locations.hotel.restaurant': 'Restaurant à la carte',
      'locations.hotel.roomService': 'Room Service',
      'locations.hotel.banquet': 'Banquet/Events',
      'locations.hotel.ofPrefix': 'of',
      
      // Clinic
      'locations.clinic.categorization': 'Clinic Categorization',
      'locations.clinic.small': 'Small (up to 150 beds)',
      'locations.clinic.medium': 'Medium (151-400 beds)',
      'locations.clinic.large': 'Large (over 400 beds)',
      'locations.clinic.unit': 'Clinics',
      'locations.clinic.privatePatientShare': 'Private Patient Share',
      'locations.clinic.patientCatering': 'Patient Catering',
      'locations.clinic.staffCatering': 'Staff Catering',
      'locations.clinic.ofPrefix': 'of',
      
      // Senior Residence
      'locations.senior.categorization': 'Senior Residence Categorization',
      'locations.senior.small': 'Small (up to 50 residents)',
      'locations.senior.medium': 'Medium (51-150 residents)',
      'locations.senior.large': 'Large (over 150 residents)',
      'locations.senior.unit': 'Residences',
      'locations.senior.careFocus': 'Care Focus',
      'locations.senior.careMixed': 'Mixed',
      'locations.senior.careAssisted': 'Assisted Living',
      'locations.senior.careNursing': 'Full Care',
      'locations.senior.catering': 'Catering',
      'locations.senior.fullCatering': 'Full Catering',
      'locations.senior.partialCatering': 'Partial Catering',
      'locations.senior.specialDiet': 'Special Diet',
      'locations.senior.ofPrefix': 'of',
      
      // Restaurant
      'locations.restaurant.categorization': 'Restaurant Categorization',
      'locations.restaurant.small': 'Small (up to 50 seats)',
      'locations.restaurant.medium': 'Medium (51-150 seats)',
      'locations.restaurant.large': 'Large (over 150 seats)',
      'locations.restaurant.unit': 'Restaurants',
      'locations.restaurant.alaCarte': 'À la carte',
      'locations.restaurant.banquet': 'Banquet/Events',
      'locations.restaurant.ofPrefix': 'of',
      
      // Cafeteria
      'locations.cafeteria.categorization': 'Cafeteria Categorization',
      'locations.cafeteria.small': 'Small (up to 200 employees)',
      'locations.cafeteria.medium': 'Medium (201-500 employees)',
      'locations.cafeteria.large': 'Large (over 500 employees)',
      'locations.cafeteria.unit': 'Locations',
      'locations.cafeteria.serviceScope': 'Service Scope',
      'locations.cafeteria.breakfast': 'Breakfast',
      'locations.cafeteria.lunch': 'Lunch',
      'locations.cafeteria.dinner': 'Dinner',
      'locations.cafeteria.ofPrefix': 'of',
      
      // Common nested keys for i18nModule compatibility
      'common.addLocation': '+ Add Location',
      'common.clearForm': 'Clear Form',
      'common.save': 'Save'
    }
  };
  
  // Initialize current language
  let currentLanguage = 'de';
  
  // Initialize tab functionality
  function initTabs() {
    const tabs = document.querySelectorAll('.nav-tab');
    const panels = document.querySelectorAll('.tab-panel');
    
    tabs.forEach(tab => {
      tab.addEventListener('click', () => {
        const targetTab = tab.getAttribute('data-tab');
        
        // Update active states
        tabs.forEach(t => t.classList.remove('active'));
        panels.forEach(p => p.classList.remove('active'));
        
        tab.classList.add('active');
        const targetPanel = document.getElementById(targetTab!);
        if (targetPanel) {
          targetPanel.classList.add('active');
        }
        
        // Update URL hash
        window.location.hash = targetTab!;
      });
    });
    
    // Handle initial hash
    if (window.location.hash) {
      const hashTab = window.location.hash.substring(1);
      const hashButton = document.querySelector(`.nav-tab[data-tab="${hashTab}"]`);
      if (hashButton) {
        (hashButton as HTMLElement).click();
      }
    }
  }
  
  // Calculator functionality
  function updateCalculator() {
    const orderValueSlider = document.getElementById('orderValue') as HTMLInputElement;
    const leadTimeSlider = document.getElementById('leadTime') as HTMLInputElement;
    const pickupCheckbox = document.getElementById('pickupToggle') as HTMLInputElement;
    
    const orderValue = parseInt(orderValueSlider.value);
    const leadTime = parseInt(leadTimeSlider.value);
    const isPickup = pickupCheckbox.checked;
    
    // Update display values
    document.getElementById('orderValueDisplay')!.textContent = `€${orderValue.toLocaleString('de-DE')}`;
    document.getElementById('leadTimeDisplay')!.textContent = `${leadTime} ${translations[currentLanguage]['days'] || 'Tage'}`;
    
    // Calculate base discount
    let baseDiscount = 0;
    if (orderValue >= 75000) baseDiscount = 10;
    else if (orderValue >= 50000) baseDiscount = 9;
    else if (orderValue >= 30000) baseDiscount = 8;
    else if (orderValue >= 15000) baseDiscount = 6;
    else if (orderValue >= 5000) baseDiscount = 3;
    
    // Calculate early booking discount
    let earlyDiscount = 0;
    if (leadTime >= 30) earlyDiscount = 3;
    else if (leadTime >= 15) earlyDiscount = 2;
    else if (leadTime >= 10) earlyDiscount = 1;
    
    // Calculate pickup discount (only if order value >= 5000)
    let pickupDiscount = 0;
    if (isPickup && orderValue >= 5000) {
      pickupDiscount = 2;
    }
    
    // Calculate total discount (max 15%)
    let totalDiscount = Math.min(baseDiscount + earlyDiscount + pickupDiscount, 15);
    
    // Update display
    document.getElementById('baseDiscount')!.textContent = `${baseDiscount}%`;
    document.getElementById('earlyDiscount')!.textContent = `${earlyDiscount}%`;
    document.getElementById('pickupDiscount')!.textContent = `${pickupDiscount}%`;
    document.getElementById('totalDiscount')!.textContent = `${totalDiscount}%`;
    
    // Calculate savings and final price
    const discountAmount = Math.round(orderValue * totalDiscount / 100);
    const finalPrice = orderValue - discountAmount;
    
    document.getElementById('discountAmount')!.textContent = `€${discountAmount.toLocaleString('de-DE')}`;
    document.getElementById('finalPrice')!.textContent = `€${finalPrice.toLocaleString('de-DE')}`;
  }
  
  // Load example scenarios
  function loadExample(type: string) {
    const orderValueSlider = document.getElementById('orderValue') as HTMLInputElement;
    const leadTimeSlider = document.getElementById('leadTime') as HTMLInputElement;
    const pickupCheckbox = document.getElementById('pickupToggle') as HTMLInputElement;
    
    switch(type) {
      case 'hotel':
        orderValueSlider.value = '35000';
        leadTimeSlider.value = '21';
        pickupCheckbox.checked = true;
        break;
      case 'klinik':
        orderValueSlider.value = '65000';
        leadTimeSlider.value = '30';
        pickupCheckbox.checked = false;
        break;
      case 'restaurant':
        orderValueSlider.value = '8500';
        leadTimeSlider.value = '14';
        pickupCheckbox.checked = true;
        break;
    }
    
    updateCalculator();
  }
  
  // Helper function to translate elements within a container
  function translateElements(container: Element) {
    container.querySelectorAll('[data-i18n]').forEach(element => {
      const key = element.getAttribute('data-i18n');
      if (key) {
        // Support nested keys like "locationDetails.locationName"
        const keys = key.split('.');
        let value: any = translations[currentLanguage];
        
        for (const k of keys) {
          if (value && typeof value === 'object' && k in value) {
            value = value[k];
          } else {
            value = translations[currentLanguage][key]; // Fallback to direct key
            break;
          }
        }
        
        if (typeof value === 'string') {
          element.innerHTML = value;
        }
      }
    });
  }
  
  // Language change functionality
  function changeLanguage(lang: string) {
    currentLanguage = lang;
    document.querySelectorAll('[data-i18n]').forEach(element => {
      const key = element.getAttribute('data-i18n');
      if (key) {
        // Support nested keys like "common.addLocation"
        const keys = key.split('.');
        let value: any = translations[lang];
        
        for (const k of keys) {
          if (value && typeof value === 'object' && k in value) {
            value = value[k];
          } else {
            value = translations[lang][key]; // Fallback to direct key
            break;
          }
        }
        
        if (typeof value === 'string') {
          element.innerHTML = value;
        }
      }
    });
    
    // Update placeholders
    document.querySelectorAll('[data-i18n-placeholder]').forEach(element => {
      const key = element.getAttribute('data-i18n-placeholder');
      if (key && translations[lang] && translations[lang][key]) {
        (element as HTMLInputElement).placeholder = translations[lang][key];
      }
    });
    
    // Update calculator display
    updateCalculator();
  }
  
  // Clear form functionality
  function handleClearForm() {
    const activePanel = document.querySelector('.tab-panel.active');
    
    if (activePanel && activePanel.id === 'customer') {
      if (confirm(translations[currentLanguage]['confirmClear'])) {
        const form = document.getElementById('customerForm') as HTMLFormElement;
        form.reset();
        localStorage.removeItem('freshplanData');
        
        // Remove any error styling
        document.querySelectorAll('#customerForm [required]').forEach(field => {
          (field as HTMLInputElement).style.borderColor = '';
        });
        
        // Reset calculator to defaults
        (document.getElementById('orderValue') as HTMLInputElement).value = '15000';
        (document.getElementById('leadTime') as HTMLInputElement).value = '14';
        (document.getElementById('pickupToggle') as HTMLInputElement).checked = false;
        updateCalculator();
        
        // Reset locations data
        locationDetailsList = [];
        renderLocationDetails();
        
        // Hide locations tab if visible
        const locationsTab = document.querySelector('.nav-tab[data-tab="locations"]') as HTMLElement;
        if (locationsTab) {
          locationsTab.style.display = 'none';
        }
        
        // Hide location details tab if visible
        toggleLocationDetailsTab(false);
      }
    } else {
      alert(translations[currentLanguage]['clearOnlyCustomer']);
    }
  }
  
  // Save form functionality
  function handleSaveForm() {
    const activePanel = document.querySelector('.tab-panel.active');
    
    // Only save if we're on customer form
    if (activePanel && activePanel.id === 'customer') {
      const form = document.getElementById('customerForm') as HTMLFormElement;
      const requiredFields = form.querySelectorAll('[required]');
      let allFieldsFilled = true;
      let emptyFields: string[] = [];
      
      requiredFields.forEach(field => {
        const inputField = field as HTMLInputElement;
        if (!inputField.value || inputField.value.trim() === '') {
          allFieldsFilled = false;
          inputField.style.borderColor = 'var(--danger)';
          const label = form.querySelector(`label[for="${inputField.id}"]`);
          emptyFields.push(label ? label.textContent!.replace('*', '').trim() : inputField.id);
        } else {
          inputField.style.borderColor = '';
        }
      });
      
      if (!allFieldsFilled) {
        alert(translations[currentLanguage]['fillRequired'] + '\n\n' + emptyFields.join('\n'));
        return;
      }
      
      // Check for Neukunde + Rechnung combination
      const customerType = (document.getElementById('customerType') as HTMLInputElement).value;
      const paymentMethod = (document.getElementById('paymentMethod') as HTMLInputElement).value;
      
      if (customerType === 'neukunde' && paymentMethod === 'rechnung') {
        if (!confirm(currentLanguage === 'de' 
          ? 'Hinweis: Für Neukunden ist Zahlung auf Rechnung erst nach Bonitätsprüfung möglich. Möchten Sie fortfahren?' 
          : 'Note: For new customers, payment by invoice is only possible after credit check. Do you want to continue?')) {
          return;
        }
      }
      
      // Collect and save data
      const formData: any = {};
      
      // Collect calculator data
      formData.calculator = {
        orderValue: (document.getElementById('orderValue') as HTMLInputElement).value,
        leadTime: (document.getElementById('leadTime') as HTMLInputElement).value,
        pickup: (document.getElementById('pickupToggle') as HTMLInputElement).checked
      };
      
      // Collect customer data
      const customerFields = ['companyName', 'legalForm', 'customerType', 'industry', 'chainCustomer', 
                             'customerNumber', 'street', 'postalCode', 'city', 'contactName', 
                             'contactPosition', 'contactPhone', 'contactEmail', 'expectedVolume', 
                             'paymentMethod', 'notes', 'customField1', 'customField2'];
      
      formData.customer = {};
      customerFields.forEach(field => {
        const element = document.getElementById(field);
        if (element) {
          formData.customer[field] = (element as HTMLInputElement).value;
        }
      });
      
      // Collect locations data if chain customer
      if ((document.getElementById('chainCustomer') as HTMLSelectElement)?.value === 'ja') {
        formData.locations = {
          totalLocations: (document.getElementById('totalLocations') as HTMLInputElement)?.value || '0',
          managementType: (document.getElementById('locationsManagementType') as HTMLSelectElement)?.value || 'zentral',
          detailedLocations: (document.getElementById('detailedLocations') as HTMLInputElement)?.checked || false
        };
        
        // Collect industry-specific location data
        const industry = (document.getElementById('industry') as HTMLSelectElement)?.value;
        switch(industry) {
          case 'hotel':
            formData.locations.smallHotels = (document.getElementById('smallHotels') as HTMLInputElement)?.value || '0';
            formData.locations.mediumHotels = (document.getElementById('mediumHotels') as HTMLInputElement)?.value || '0';
            formData.locations.largeHotels = (document.getElementById('largeHotels') as HTMLInputElement)?.value || '0';
            formData.locations.hotelBreakfast = (document.getElementById('hotelBreakfast') as HTMLInputElement)?.value || '0';
            formData.locations.hotelRestaurant = (document.getElementById('hotelRestaurant') as HTMLInputElement)?.value || '0';
            formData.locations.hotelRoomService = (document.getElementById('hotelRoomService') as HTMLInputElement)?.value || '0';
            formData.locations.hotelBanquet = (document.getElementById('hotelBanquet') as HTMLInputElement)?.value || '0';
            break;
          case 'krankenhaus':
            formData.locations.smallClinics = (document.getElementById('smallClinics') as HTMLInputElement)?.value || '0';
            formData.locations.mediumClinics = (document.getElementById('mediumClinics') as HTMLInputElement)?.value || '0';
            formData.locations.largeClinics = (document.getElementById('largeClinics') as HTMLInputElement)?.value || '0';
            formData.locations.privatePatientShare = (document.getElementById('privatePatientShare') as HTMLInputElement)?.value || '15';
            formData.locations.clinicPremiumMeals = (document.getElementById('clinicPremiumMeals') as HTMLInputElement)?.value || '0';
            formData.locations.clinicStaffCatering = (document.getElementById('clinicStaffCatering') as HTMLInputElement)?.value || '0';
            break;
          // Add other industries as needed
        }
        
        // Save location details list
        formData.locationDetailsList = locationDetailsList;
      }
      
      // Save to localStorage
      localStorage.setItem('freshplanData', JSON.stringify(formData));
      
      alert(translations[currentLanguage]['saveSuccess']);
    } else {
      alert(translations[currentLanguage]['nothingToSave']);
    }
  }
  
  // Load saved data
  function loadSavedData() {
    const savedData = localStorage.getItem('freshplanData');
    if (savedData) {
      try {
        const data = JSON.parse(savedData);
        
        // Load calculator data
        if (data.calculator) {
          if (data.calculator.orderValue) {
            (document.getElementById('orderValue') as HTMLInputElement).value = data.calculator.orderValue;
          }
          if (data.calculator.leadTime) {
            (document.getElementById('leadTime') as HTMLInputElement).value = data.calculator.leadTime;
          }
          if (data.calculator.pickup !== undefined) {
            (document.getElementById('pickupToggle') as HTMLInputElement).checked = data.calculator.pickup;
          }
          updateCalculator();
        }
        
        // Load customer data
        if (data.customer) {
          Object.keys(data.customer).forEach(field => {
            const element = document.getElementById(field);
            if (element) {
              (element as HTMLInputElement).value = data.customer[field];
            }
          });
          
          // If chain customer, show locations tab
          if (data.customer.chainCustomer === 'ja') {
            const locationsTab = document.querySelector('.nav-tab[data-tab="locations"]') as HTMLElement;
            if (locationsTab) {
              locationsTab.style.display = 'block';
            }
          }
          
          // Trigger industry field update
          if (data.customer.industry) {
            updateLocationIndustryFields();
          }
        }
        
        // Load locations data
        if (data.locations) {
          // Basic locations fields
          if (data.locations.totalLocations) {
            const totalField = document.getElementById('totalLocations') as HTMLInputElement;
            if (totalField) totalField.value = data.locations.totalLocations;
          }
          if (data.locations.managementType) {
            const mgmtField = document.getElementById('locationsManagementType') as HTMLSelectElement;
            if (mgmtField) mgmtField.value = data.locations.managementType;
          }
          if (data.locations.detailedLocations !== undefined) {
            const detailsCheckbox = document.getElementById('detailedLocations') as HTMLInputElement;
            if (detailsCheckbox) {
              detailsCheckbox.checked = data.locations.detailedLocations;
              toggleLocationDetailsTab(data.locations.detailedLocations);
            }
          }
          
          // Load industry-specific fields
          setTimeout(() => {
            Object.keys(data.locations).forEach(key => {
              if (key !== 'totalLocations' && key !== 'managementType' && key !== 'detailedLocations') {
                const element = document.getElementById(key) as HTMLInputElement;
                if (element) {
                  element.value = data.locations[key];
                }
              }
            });
            updateTotalLocations();
          }, 100);
        }
        
        // Load location details list
        if (data.locationDetailsList) {
          locationDetailsList = data.locationDetailsList;
          renderLocationDetails();
        }
      } catch (e) {
        console.error('Error loading saved data:', e);
      }
    }
  }
  
  // Format currency input
  function formatCurrency(input: HTMLInputElement) {
    let value = input.value.replace(/[^0-9]/g, '');
    if (value) {
      const number = parseInt(value);
      input.value = number.toLocaleString('de-DE');
    }
  }
  
  // Chain customer functionality
  function toggleLocationDetailsTab(show: boolean) {
    const locationDetailsTab = document.querySelector('.nav-tab[data-tab="locationdetails"]') as HTMLElement;
    if (locationDetailsTab) {
      locationDetailsTab.style.display = show ? 'block' : 'none';
    }
  }
  
  // Initialize chain customer toggle
  function initChainCustomer() {
    const chainCustomerSelect = document.getElementById('chainCustomer') as HTMLSelectElement;
    const locationsTab = document.querySelector('.nav-tab[data-tab="locations"]') as HTMLElement;
    
    if (chainCustomerSelect) {
      chainCustomerSelect.addEventListener('change', () => {
        const isChain = chainCustomerSelect.value === 'ja';
        if (locationsTab) {
          locationsTab.style.display = isChain ? 'block' : 'none';
        }
      });
    }
    
    // Also update location fields when industry changes
    const industrySelect = document.getElementById('industry') as HTMLSelectElement;
    if (industrySelect) {
      industrySelect.addEventListener('change', updateLocationIndustryFields);
      // Initialize on load
      updateLocationIndustryFields();
    }
  }
  
  // Vending interest toggle
  function initVendingInterest() {
    const vendingCheckbox = document.getElementById('vendingInterest') as HTMLInputElement;
    const vendingDetails = document.getElementById('vendingDetails');
    
    if (vendingCheckbox && vendingDetails) {
      vendingCheckbox.addEventListener('change', () => {
        vendingDetails.style.display = vendingCheckbox.checked ? 'block' : 'none';
      });
    }
  }
  
  // Location details functionality
  let locationDetailsList: LocationDetail[] = [];
  
  function addLocationDetail() {
    const newLocation = {
      id: Date.now(),
      name: '',
      street: '',
      postalCode: '',
      city: '',
      contactName: '',
      contactPhone: '',
      contactEmail: '',
      category: ''
    };
    
    locationDetailsList.push(newLocation);
    renderLocationDetails();
  }
  
  function removeLocationDetail(id: number) {
    if (confirm(translations[currentLanguage]['confirmRemoveLocation'])) {
      locationDetailsList = locationDetailsList.filter(loc => loc.id !== id);
      renderLocationDetails();
      checkLocationSync();
    }
  }
  
  function renderLocationDetails() {
    const container = document.getElementById('locationDetailsList');
    const noLocationsMessage = document.getElementById('noLocationsMessage');
    const detailLocationCount = document.getElementById('detailLocationCount');
    
    if (!container) return;
    
    if (locationDetailsList.length === 0) {
      if (noLocationsMessage) noLocationsMessage.style.display = 'block';
      container.innerHTML = '';
    } else {
      if (noLocationsMessage) noLocationsMessage.style.display = 'none';
      
      // Use template functions to generate HTML with i18n support
      container.innerHTML = locationDetailsList
        .map((location, index) => createLocationDetailCard(location, index + 1))
        .join('');
      
      // Apply translations to newly added elements
      translateElements(container);
      
      // Add event listeners for input changes
      container.querySelectorAll('input, select').forEach(input => {
        input.addEventListener('change', (e) => {
          const target = e.target as HTMLInputElement;
          const card = target.closest('.location-detail-card');
          const locationId = parseInt(card!.getAttribute('data-location-id')!);
          const field = target.getAttribute('data-field')!;
          const location = locationDetailsList.find(loc => loc.id === locationId);
          if (location) {
            (location as any)[field] = target.value;
          }
        });
      });
    }
    
    // Update count
    if (detailLocationCount) {
      detailLocationCount.textContent = locationDetailsList.length.toString();
    }
    
    // Check sync after render
    checkLocationSync();
  }
  
  // Check synchronization between locations and location details
  function checkLocationSync() {
    const totalLocationsField = document.getElementById('totalLocations') as HTMLInputElement;
    const syncWarning = document.getElementById('syncWarning');
    const syncWarningDetails = document.getElementById('syncWarningDetails');
    const detailLocationTotal = document.getElementById('detailLocationTotal');
    
    if (!totalLocationsField || !syncWarning) return;
    
    const expectedTotal = parseInt(totalLocationsField.value || '0');
    const actualTotal = locationDetailsList.length;
    
    // Update total display
    if (detailLocationTotal) {
      detailLocationTotal.textContent = expectedTotal.toString();
    }
    
    // Show warning if mismatch
    if (expectedTotal > 0 && actualTotal !== expectedTotal) {
      syncWarning.style.display = 'block';
      if (syncWarningDetails) {
        syncWarningDetails.innerHTML = currentLanguage === 'de'
          ? `Sie haben ${actualTotal} Standorte erfasst, aber ${expectedTotal} Standorte insgesamt angegeben.`
          : `You have captured ${actualTotal} locations, but specified ${expectedTotal} locations in total.`;
      }
    } else {
      syncWarning.style.display = 'none';
    }
  }
  
  // Credit check functionality
  function startCreditCheck() {
    alert(currentLanguage === 'de' 
      ? 'Bonitätsprüfung wird gestartet...\n(In der Demo-Version nicht verfügbar)' 
      : 'Credit check is starting...\n(Not available in demo version)');
  }
  
  function requestManagementApproval() {
    alert(currentLanguage === 'de' 
      ? 'Anfrage an Geschäftsleitung wird gesendet...\n(In der Demo-Version nicht verfügbar)' 
      : 'Request to management is being sent...\n(Not available in demo version)');
  }
  
  // Function to update industry-specific fields in locations tab
  function updateLocationIndustryFields() {
    const industry = (document.getElementById('industry') as HTMLSelectElement)?.value;
    const section = document.getElementById('industrySpecificSection');
    if (!section) return;
    
    // Use template function to get HTML with i18n support
    const html = getIndustryLocationFields(industry);
    section.innerHTML = html;
    
    // Apply translations to newly added elements
    translateElements(section);
  }
  
  // Legacy implementation kept for reference but not used
  /* function updateLocationIndustryFieldsLegacy() {
    const industry = (document.getElementById('industry') as HTMLSelectElement)?.value;
    const section = document.getElementById('industrySpecificSection');
    if (!section) return;
    
    let html = '';
    
    switch(industry) {
      case 'hotel':
        html = `
          <h3 class="form-section-title">Hotel-Kategorisierung</h3>
          <div class="form-row">
            <div class="form-group">
              <label>Klein (bis 50 Zimmer)</label>
              <input type="number" id="smallHotels" name="smallHotels" min="0" value="0" oninput="updateTotalLocations()"> <span>Hotels</span>
            </div>
            <div class="form-group">
              <label>Mittel (51-150 Zimmer)</label>
              <input type="number" id="mediumHotels" name="mediumHotels" min="0" value="0" oninput="updateTotalLocations()"> <span>Hotels</span>
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>Groß (über 150 Zimmer)</label>
              <input type="number" id="largeHotels" name="largeHotels" min="0" value="0" oninput="updateTotalLocations()"> <span>Hotels</span>
            </div>
            <div class="form-group">
              <!-- Platzhalter -->
            </div>
          </div>
          <h4 style="margin-top: 1.5rem; margin-bottom: 1rem; color: var(--primary-blue);">Services</h4>
          <div class="form-row">
            <div class="form-group">
              <label>Frühstücksservice</label>
              <input type="number" id="hotelBreakfast" name="hotelBreakfast" min="0" max="999"> <span>von <span id="totalHotelsCount">0</span> Hotels</span>
            </div>
            <div class="form-group">
              <label>Restaurant à la carte</label>
              <input type="number" id="hotelRestaurant" name="hotelRestaurant" min="0" max="999"> <span>von <span id="totalHotelsCount2">0</span> Hotels</span>
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>Room Service</label>
              <input type="number" id="hotelRoomService" name="hotelRoomService" min="0" max="999"> <span>von <span id="totalHotelsCount3">0</span> Hotels</span>
            </div>
            <div class="form-group">
              <label>Bankett/Veranstaltungen</label>
              <input type="number" id="hotelBanquet" name="hotelBanquet" min="0" max="999"> <span>von <span id="totalHotelsCount4">0</span> Hotels</span>
            </div>
          </div>
        `;
        break;
        
      case 'krankenhaus':
        html = `
          <h3 class="form-section-title">Klinik-Kategorisierung</h3>
          <div class="form-row">
            <div class="form-group">
              <label>Klein (bis 150 Betten)</label>
              <input type="number" id="smallClinics" name="smallClinics" min="0" value="0" oninput="updateTotalLocations()"> <span>Kliniken</span>
            </div>
            <div class="form-group">
              <label>Mittel (151-400 Betten)</label>
              <input type="number" id="mediumClinics" name="mediumClinics" min="0" value="0" oninput="updateTotalLocations()"> <span>Kliniken</span>
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>Groß (über 400 Betten)</label>
              <input type="number" id="largeClinics" name="largeClinics" min="0" value="0" oninput="updateTotalLocations()"> <span>Kliniken</span>
            </div>
            <div class="form-group">
              <label>Anteil Privatpatienten</label>
              <input type="range" id="privatePatientShare" name="privatePatientShare" min="0" max="100" value="15" class="slider" oninput="document.getElementById('privatePatientDisplay').textContent = this.value + '%'">
              <span id="privatePatientDisplay">15%</span>
            </div>
          </div>
          <h4 style="margin-top: 1.5rem; margin-bottom: 1rem; color: var(--primary-blue);">Services</h4>
          <div class="form-row">
            <div class="form-group">
              <label>Patientenverpflegung</label>
              <input type="number" id="clinicPremiumMeals" name="clinicPremiumMeals" min="0" max="999"> <span>von <span id="totalClinicsCount">0</span> Kliniken</span>
            </div>
            <div class="form-group">
              <label>Mitarbeiterverpflegung</label>
              <input type="number" id="clinicStaffCatering" name="clinicStaffCatering" min="0" max="999"> <span>von <span id="totalClinicsCount2">0</span> Kliniken</span>
            </div>
          </div>
        `;
        break;
        
      case 'seniorenresidenz':
        html = `
          <h3 class="form-section-title">Seniorenresidenz-Kategorisierung</h3>
          <div class="form-row">
            <div class="form-group">
              <label>Klein (bis 50 Bewohner)</label>
              <input type="number" id="smallSeniorResidences" name="smallSeniorResidences" min="0" value="0" oninput="updateTotalLocations()"> <span>Residenzen</span>
            </div>
            <div class="form-group">
              <label>Mittel (51-150 Bewohner)</label>
              <input type="number" id="mediumSeniorResidences" name="mediumSeniorResidences" min="0" value="0" oninput="updateTotalLocations()"> <span>Residenzen</span>
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>Groß (über 150 Bewohner)</label>
              <input type="number" id="largeSeniorResidences" name="largeSeniorResidences" min="0" value="0" oninput="updateTotalLocations()"> <span>Residenzen</span>
            </div>
            <div class="form-group">
              <label>Pflegeschwerpunkt</label>
              <select id="careLevel" name="careLevel">
                <option value="mixed">Gemischt</option>
                <option value="assisted">Betreutes Wohnen</option>
                <option value="nursing">Vollpflege</option>
              </select>
            </div>
          </div>
          <h4 style="margin-top: 1.5rem; margin-bottom: 1rem; color: var(--primary-blue);">Verpflegung</h4>
          <div class="form-row">
            <div class="form-group">
              <label>Vollverpflegung</label>
              <input type="number" id="seniorFullCatering" name="seniorFullCatering" min="0" max="999"> <span>von <span id="totalSeniorResidencesCount">0</span> Residenzen</span>
            </div>
            <div class="form-group">
              <label>Teilverpflegung</label>
              <input type="number" id="seniorPartialCatering" name="seniorPartialCatering" min="0" max="999"> <span>von <span id="totalSeniorResidencesCount2">0</span> Residenzen</span>
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>Sonderkostform</label>
              <input type="number" id="seniorSpecialDiet" name="seniorSpecialDiet" min="0" max="999"> <span>von <span id="totalSeniorResidencesCount3">0</span> Residenzen</span>
            </div>
            <div class="form-group">
              <!-- Platzhalter -->
            </div>
          </div>
        `;
        break;
        
      case 'restaurant':
        html = `
          <h3 class="form-section-title">Restaurant-Kategorisierung</h3>
          <div class="form-row">
            <div class="form-group">
              <label>Klein (bis 50 Sitzplätze)</label>
              <input type="number" id="smallRestaurants" name="smallRestaurants" min="0" value="0" oninput="updateTotalLocations()"> <span>Restaurants</span>
            </div>
            <div class="form-group">
              <label>Mittel (51-150 Sitzplätze)</label>
              <input type="number" id="mediumRestaurants" name="mediumRestaurants" min="0" value="0" oninput="updateTotalLocations()"> <span>Restaurants</span>
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>Groß (über 150 Sitzplätze)</label>
              <input type="number" id="largeRestaurants" name="largeRestaurants" min="0" value="0" oninput="updateTotalLocations()"> <span>Restaurants</span>
            </div>
            <div class="form-group">
              <!-- Platzhalter -->
            </div>
          </div>
          <h4 style="margin-top: 1.5rem; margin-bottom: 1rem; color: var(--primary-blue);">Services</h4>
          <div class="form-row">
            <div class="form-group">
              <label>À la carte</label>
              <input type="number" id="restaurantAlaCarte" name="restaurantAlaCarte" min="0" max="999"> <span>von <span id="totalRestaurantsCount">0</span> Restaurants</span>
            </div>
            <div class="form-group">
              <label>Bankett/Veranstaltungen</label>
              <input type="number" id="restaurantBanquet" name="restaurantBanquet" min="0" max="999"> <span>von <span id="totalRestaurantsCount2">0</span> Restaurants</span>
            </div>
          </div>
        `;
        break;
        
      case 'betriebsrestaurant':
        html = `
          <h3 class="form-section-title">Betriebsrestaurant-Kategorisierung</h3>
          <div class="form-row">
            <div class="form-group">
              <label>Klein (bis 200 MA)</label>
              <input type="number" id="smallCafeterias" name="smallCafeterias" min="0" value="0" oninput="updateTotalLocations()"> <span>Standorte</span>
            </div>
            <div class="form-group">
              <label>Mittel (201-500 MA)</label>
              <input type="number" id="mediumCafeterias" name="mediumCafeterias" min="0" value="0" oninput="updateTotalLocations()"> <span>Standorte</span>
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>Groß (über 500 MA)</label>
              <input type="number" id="largeCafeterias" name="largeCafeterias" min="0" value="0" oninput="updateTotalLocations()"> <span>Standorte</span>
            </div>
            <div class="form-group">
              <!-- Platzhalter -->
            </div>
          </div>
          <h4 style="margin-top: 1.5rem; margin-bottom: 1rem; color: var(--primary-blue);">Serviceumfang</h4>
          <div class="form-row">
            <div class="form-group">
              <label>Frühstück</label>
              <input type="number" id="cafeteriaBreakfast" name="cafeteriaBreakfast" min="0" max="999"> <span>von <span id="totalCafeteriasCount">0</span> Standorten</span>
            </div>
            <div class="form-group">
              <label>Mittagessen</label>
              <input type="number" id="cafeteriaLunch" name="cafeteriaLunch" min="0" max="999"> <span>von <span id="totalCafeteriasCount2">0</span> Standorten</span>
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>Abendessen</label>
              <input type="number" id="cafeteriaDinner" name="cafeteriaDinner" min="0" max="999"> <span>von <span id="totalCafeteriasCount3">0</span> Standorten</span>
            </div>
            <div class="form-group">
              <!-- Platzhalter -->
            </div>
          </div>
        `;
        break;
        
      default:
        html = '';
    }
    
    section.innerHTML = html;
  } */
  
  // Function to update total locations count
  function updateTotalLocations() {
    const industry = (document.getElementById('industry') as HTMLSelectElement)?.value;
    let total = 0;
    
    switch(industry) {
      case 'hotel':
        total = parseInt((document.getElementById('smallHotels') as HTMLInputElement)?.value || '0') +
                parseInt((document.getElementById('mediumHotels') as HTMLInputElement)?.value || '0') +
                parseInt((document.getElementById('largeHotels') as HTMLInputElement)?.value || '0');
        // Update all hotel count displays
        ['totalHotelsCount', 'totalHotelsCount2', 'totalHotelsCount3', 'totalHotelsCount4'].forEach(id => {
          const element = document.getElementById(id);
          if (element) element.textContent = total.toString();
        });
        break;
        
      case 'krankenhaus':
        total = parseInt((document.getElementById('smallClinics') as HTMLInputElement)?.value || '0') +
                parseInt((document.getElementById('mediumClinics') as HTMLInputElement)?.value || '0') +
                parseInt((document.getElementById('largeClinics') as HTMLInputElement)?.value || '0');
        // Update all clinic count displays
        ['totalClinicsCount', 'totalClinicsCount2'].forEach(id => {
          const element = document.getElementById(id);
          if (element) element.textContent = total.toString();
        });
        break;
        
      case 'seniorenresidenz':
        total = parseInt((document.getElementById('smallSeniorResidences') as HTMLInputElement)?.value || '0') +
                parseInt((document.getElementById('mediumSeniorResidences') as HTMLInputElement)?.value || '0') +
                parseInt((document.getElementById('largeSeniorResidences') as HTMLInputElement)?.value || '0');
        // Update all senior residence count displays
        ['totalSeniorResidencesCount', 'totalSeniorResidencesCount2', 'totalSeniorResidencesCount3'].forEach(id => {
          const element = document.getElementById(id);
          if (element) element.textContent = total.toString();
        });
        break;
        
      case 'restaurant':
        total = parseInt((document.getElementById('smallRestaurants') as HTMLInputElement)?.value || '0') +
                parseInt((document.getElementById('mediumRestaurants') as HTMLInputElement)?.value || '0') +
                parseInt((document.getElementById('largeRestaurants') as HTMLInputElement)?.value || '0');
        // Update all restaurant count displays
        ['totalRestaurantsCount', 'totalRestaurantsCount2'].forEach(id => {
          const element = document.getElementById(id);
          if (element) element.textContent = total.toString();
        });
        break;
        
      case 'betriebsrestaurant':
        total = parseInt((document.getElementById('smallCafeterias') as HTMLInputElement)?.value || '0') +
                parseInt((document.getElementById('mediumCafeterias') as HTMLInputElement)?.value || '0') +
                parseInt((document.getElementById('largeCafeterias') as HTMLInputElement)?.value || '0');
        // Update all cafeteria count displays
        ['totalCafeteriasCount', 'totalCafeteriasCount2', 'totalCafeteriasCount3'].forEach(id => {
          const element = document.getElementById(id);
          if (element) element.textContent = total.toString();
        });
        break;
    }
    
    // Update total locations field
    const totalField = document.getElementById('totalLocations') as HTMLInputElement;
    if (totalField) {
      totalField.value = total.toString();
    }
    
    // Check sync when total changes
    checkLocationSync();
  }
  
  // Make functions globally available
  window.updateCalculator = updateCalculator;
  window.changeLanguage = changeLanguage;
  window.handleClearForm = handleClearForm;
  window.handleSaveForm = handleSaveForm;
  window.loadExample = loadExample;
  window.formatCurrency = formatCurrency;
  window.toggleLocationDetailsTab = toggleLocationDetailsTab;
  window.addLocationDetail = addLocationDetail;
  window.startCreditCheck = startCreditCheck;
  window.requestManagementApproval = requestManagementApproval;
  window.updateTotalLocations = updateTotalLocations;
  
  // Also expose removeLocationDetail for the dynamic HTML
  (window as any).removeLocationDetail = removeLocationDetail;
  
  // Initialize Neukunde + Rechnung warning
  function initPaymentWarning() {
    const customerTypeSelect = document.getElementById('customerType') as HTMLSelectElement;
    const paymentMethodSelect = document.getElementById('paymentMethod') as HTMLSelectElement;
    
    function checkNeukundeRechnung() {
      if (customerTypeSelect && paymentMethodSelect) {
        if (customerTypeSelect.value === 'neukunde' && paymentMethodSelect.value === 'rechnung') {
          alert(currentLanguage === 'de' 
            ? 'Hinweis: Für Neukunden ist Zahlung auf Rechnung erst nach Bonitätsprüfung möglich.' 
            : 'Note: For new customers, payment by invoice is only possible after credit check.');
        }
      }
    }
    
    if (customerTypeSelect) {
      customerTypeSelect.addEventListener('change', checkNeukundeRechnung);
    }
    
    if (paymentMethodSelect) {
      paymentMethodSelect.addEventListener('change', checkNeukundeRechnung);
    }
  }
  
  // Initialize everything when DOM is ready
  initTabs();
  updateCalculator();
  loadSavedData();
  initChainCustomer();
  initVendingInterest();
  initPaymentWarning();
  renderLocationDetails();
}