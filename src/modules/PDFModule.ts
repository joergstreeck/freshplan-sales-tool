/**
 * PDF Module - TypeScript version
 * Handles PDF generation for offers and documents
 */

import Module from '../core/Module';
import { useStore } from '../store';
import { formatCurrency, formatDate } from '../utils/formatting';
import type { CustomerData, ProfileData } from '../types';

// Type definitions for jsPDF
declare global {
  interface Window {
    jspdf: {
      jsPDF: any;
    };
  }
}

interface SalespersonInfo {
  name: string;
  email: string;
  phone: string;
}

interface OfferCalculation {
  annualVolume: number;
  annualSavings: number;
  monthlyVolume: number;
  monthlySavings: number;
  discount: number;
}

interface CalculationExample {
  title: string;
  orderValue: number;
  leadTime: number;
  pickup: boolean;
  result: {
    base: number;
    early: number;
    pickup: number;
    total: number;
  };
}

export default class PDFModule extends Module {
  private currentPDF: any = null;
  private pdfFilename: string | null = null;

  constructor() {
    super('pdf');
  }

  async setup(): Promise<void> {
    // Check if jsPDF is available
    if (typeof window.jspdf === 'undefined') {
      console.warn('jsPDF not loaded. PDF generation will not work.');
      this.setModuleState('available', false);
      return;
    }
    
    this.setModuleState('available', true);
    
    // Initialize offer form values
    this.initializeOfferForm();
  }

  bindEvents(): void {
    // Generate PDF button
    this.on('#generatePdfBtn', 'click', () => {
      this.generateOffer();
    });

    // Modal actions
    this.on('#closePdfModalBtn', 'click', () => {
      this.closePdfModal();
    });

    this.on('#downloadPdfBtn', 'click', () => {
      this.downloadPDF();
    });

    this.on('#sendPdfBtn', 'click', () => {
      this.sendOffer();
    });

    // Document checkboxes
    this.on('#docPartnership, #docRabatt, #docCalc', 'change', () => {
      this.updatePreview();
    });

    // Offer form changes
    this.on('#offerDiscount, #additionalNotes', 'change', () => {
      this.updatePreview();
    });
  }

  subscribeToState(): void {
    // React to settings changes
    useStore.subscribe(
      (state) => state.settings.salesperson,
      (salesperson) => this.updateSalespersonInfo(salesperson)
    );

    // React to navigation to offer tab
    this.events.on('module:tabs:switched', (tab) => {
      if (tab === 'offer') {
        this.prepareOfferView();
      }
    });
  }

  /**
   * Initialize offer form with defaults
   */
  private initializeOfferForm(): void {
    // Load settings for salesperson info
    const settings = useStore.getState().settings;
    if (settings.salesperson) {
      this.updateSalespersonInfo(settings.salesperson);
    }

    // Set default discount
    const defaultDiscount = settings.defaults?.discount || 15;
    this.dom.val('#offerDiscount', defaultDiscount);
  }

  /**
   * Update salesperson info in offer form
   */
  private updateSalespersonInfo(salesperson: SalespersonInfo): void {
    if (!salesperson) return;

    this.dom.val('#salespersonName', salesperson.name);
    this.dom.val('#salespersonEmail', salesperson.email);
    this.dom.val('#salespersonPhone', salesperson.phone);
  }

  /**
   * Prepare offer view
   */
  private prepareOfferView(): void {
    const customerData = this.getCustomerData();
    
    if (!customerData || !customerData.companyName) {
      this.dom.html('#offerPreview', `
        <div class="empty-state">
          <p>Keine Kundendaten vorhanden</p>
          <button class="btn btn-primary" onclick="FreshPlan.app.getModule('tabs').navigateTo('customer')">
            Kundendaten eingeben
          </button>
        </div>
      `);
      return;
    }

    this.updatePreview();
  }

  /**
   * Update offer preview
   */
  private updatePreview(): void {
    const customerData = this.getCustomerData();
    if (!customerData || !customerData.companyName) return;

    const discount = parseFloat(String(this.dom.val('#offerDiscount') || 0));
    const calculation = this.calculateOffer(customerData, discount);

    const docPartnership = (this.dom.$('#docPartnership') as HTMLInputElement)?.checked;
    const docRabatt = (this.dom.$('#docRabatt') as HTMLInputElement)?.checked;
    const docCalc = (this.dom.$('#docCalc') as HTMLInputElement)?.checked;

    const preview = `
      <div class="offer-preview">
        <h4>Angebot für ${customerData.companyName}</h4>
        <div class="preview-section">
          <strong>Vertragskonditionen:</strong>
          <ul>
            <li>Rabatt: ${discount}%</li>
            <li>Geschätztes Jahresvolumen: ${formatCurrency(calculation.annualVolume)}</li>
            <li>Jährliche Ersparnis: ${formatCurrency(calculation.annualSavings)}</li>
            <li>Vertragslaufzeit: ${customerData.contractDuration || 24} Monate</li>
          </ul>
        </div>
        <div class="preview-section">
          <strong>Eingeschlossene Dokumente:</strong>
          <ul>
            ${docPartnership ? '<li>Partnerschaftsvereinbarung</li>' : ''}
            ${docRabatt ? '<li>Rabattsystem-Übersicht</li>' : ''}
            ${docCalc ? '<li>Berechnungsbeispiele</li>' : ''}
          </ul>
        </div>
      </div>
    `;

    this.dom.html('#offerPreview', preview);
  }

  /**
   * Calculate offer details
   */
  private calculateOffer(customerData: CustomerData, discount: number): OfferCalculation {
    // Get volume from profile or estimate
    let annualVolume = 0;
    
    const profileData = useStore.getState().profile.data;
    if (profileData?.analysis?.estimatedVolume) {
      annualVolume = profileData.analysis.estimatedVolume;
    } else {
      // Simple estimation
      if (customerData.customerType === 'chain') {
        const locations = customerData.totalLocations || 1;
        const avgOrder = customerData.avgOrderPerLocation || 10000;
        annualVolume = locations * avgOrder * 12;
      } else {
        annualVolume = 180000; // Default
      }
    }

    const annualSavings = annualVolume * (discount / 100);

    return {
      annualVolume,
      annualSavings,
      monthlyVolume: annualVolume / 12,
      monthlySavings: annualSavings / 12,
      discount
    };
  }

  /**
   * Generate offer PDF
   */
  async generateOffer(): Promise<void> {
    const customerData = this.getCustomerData();
    
    if (!customerData || !customerData.companyName) {
      this.showError('Bitte füllen Sie zuerst die Kundendaten aus');
      return;
    }

    // Validate salesperson info
    const salesperson: SalespersonInfo = {
      name: String(this.dom.val('#salespersonName') || ''),
      email: String(this.dom.val('#salespersonEmail') || ''),
      phone: String(this.dom.val('#salespersonPhone') || '')
    };

    if (!salesperson.name || !salesperson.email) {
      this.showError('Bitte füllen Sie die Verkäufer-Informationen aus');
      return;
    }

    this.setLoading(true);

    try {
      // Create PDF
      const { jsPDF } = window.jspdf;
      const doc = new jsPDF();
      
      // Set up PDF
      this.currentPDF = doc;
      this.pdfFilename = `FreshPlan_Angebot_${customerData.companyName.replace(/\s+/g, '_')}_${Date.now()}.pdf`;

      const coverLetterEnabled = (this.dom.$('#docPartnership') as HTMLInputElement)?.checked;
      const partnershipEnabled = (this.dom.$('#docPartnership') as HTMLInputElement)?.checked;
      const discountEnabled = (this.dom.$('#docRabatt') as HTMLInputElement)?.checked;
      const calculationEnabled = (this.dom.$('#docCalc') as HTMLInputElement)?.checked;

      // Generate cover letter
      if (coverLetterEnabled) {
        this.generateCoverLetter(doc, customerData, salesperson);
      }

      // Generate partnership agreement
      if (partnershipEnabled) {
        if (doc.internal.getNumberOfPages() > 1) doc.addPage();
        this.generatePartnershipAgreement(doc, customerData);
      }

      // Generate discount overview
      if (discountEnabled) {
        doc.addPage();
        this.generateDiscountOverview(doc);
      }

      // Generate calculation examples
      if (calculationEnabled) {
        doc.addPage();
        this.generateCalculationExamples(doc, customerData);
      }

      // Show preview
      this.showPdfModal();
      
      this.showSuccess('PDF wurde erfolgreich generiert');
      this.emit('generated', { filename: this.pdfFilename });

    } catch (error) {
      console.error('PDF generation error:', error);
      this.showError('Fehler beim Generieren des PDFs');
    } finally {
      this.setLoading(false);
    }
  }

  /**
   * Generate cover letter page
   */
  private generateCoverLetter(doc: any, customerData: CustomerData, salesperson: SalespersonInfo): void {
    const discount = parseFloat(String(this.dom.val('#offerDiscount') || 0));
    const notes = String(this.dom.val('#additionalNotes') || '');
    const calculation = this.calculateOffer(customerData, discount);

    // Header
    doc.setFontSize(20);
    doc.setTextColor(0, 79, 123); // Freshfoodz blue
    doc.text('FreshPlan Angebot', 20, 30);

    // Date
    doc.setFontSize(10);
    doc.setTextColor(100);
    doc.text(formatDate(new Date()), 190, 30, { align: 'right' });

    // Customer address
    doc.setFontSize(12);
    doc.setTextColor(0);
    doc.text(customerData.companyName, 20, 60);
    doc.text(customerData.contactName, 20, 67);
    doc.text(customerData.street, 20, 74);
    doc.text(`${customerData.zipCode} ${customerData.city}`, 20, 81);

    // Greeting
    doc.text(`Sehr geehrte/r ${customerData.contactName},`, 20, 100);

    // Content
    doc.setFontSize(11);
    const content = `
vielen Dank für Ihr Interesse an FreshPlan, unserem innovativen Rabattsystem für 
Großkunden. Basierend auf unserer Analyse Ihrer Bedarfssituation freuen wir uns, 
Ihnen folgendes Angebot unterbreiten zu können:

Ihre Vorteile mit FreshPlan:
• Garantierter Rabatt von ${discount}% auf unser gesamtes Sortiment
• Jährliche Ersparnis von ca. ${formatCurrency(calculation.annualSavings)}
• 12 Monate Preisstabilität während der Vertragslaufzeit
• Persönlicher Ansprechpartner für alle Ihre Anliegen
• Flexible Bestellmöglichkeiten mit attraktiven Zusatzrabatten

${notes ? `\n${notes}\n` : ''}

Gerne bespreche ich mit Ihnen die Details persönlich und zeige Ihnen, wie Sie 
mit FreshPlan Ihre Warenwirtschaft optimieren können.

Mit freundlichen Grüßen
    `.trim();

    const lines = doc.splitTextToSize(content, 170);
    doc.text(lines, 20, 115);

    // Signature
    const yPos = 115 + lines.length * 5 + 20;
    doc.text(salesperson.name, 20, yPos);
    doc.setFontSize(10);
    doc.text(salesperson.email, 20, yPos + 5);
    doc.text(salesperson.phone, 20, yPos + 10);

    // Footer
    this.addFooter(doc);
  }

  /**
   * Generate partnership agreement
   */
  private generatePartnershipAgreement(doc: any, customerData: CustomerData): void {
    doc.setFontSize(16);
    doc.setTextColor(0, 79, 123);
    doc.text('FreshPlan-Partnerschaftsvereinbarung', 105, 30, { align: 'center' });

    doc.setFontSize(10);
    doc.setTextColor(0);
    
    const contractStart = customerData.contractStart 
      ? new Date(customerData.contractStart) 
      : new Date();
    
    const contractEnd = customerData.contractEnd 
      ? new Date(customerData.contractEnd) 
      : new Date(Date.now() + 365 * 24 * 60 * 60 * 1000);
    
    const agreementText = `
zwischen

Freshfoodz GmbH
Plauener Str. 163-165
13053 Berlin
(im Folgenden "Freshfoodz")

und

${customerData.companyName}
${customerData.street}
${customerData.zipCode} ${customerData.city}
(im Folgenden "Kunde")

§1 Zielsetzung und Grundlage
Diese Vereinbarung bildet die Grundlage für eine planbare, partnerschaftliche 
Zusammenarbeit zwischen Freshfoodz und dem Kunden.

§2 Laufzeit und Zielvolumen
Die Vereinbarung beginnt am ${formatDate(contractStart)} 
und endet am ${formatDate(contractEnd)} 
(Laufzeit: ${customerData.contractDuration || 24} Monate).

§3 Teilnahme am FreshPlan-Rabattsystem
Der Kunde erhält Zugriff auf das FreshPlan-Rabattsystem in der bei 
Vertragsabschluss gültigen Fassung.

§4 Preisstabilität
Die Preise bleiben für die Dauer der Laufzeit stabil. Eine Anpassung ist nur 
möglich, wenn der Verbraucherpreisindex um mehr als 5% steigt.
    `.trim();

    const lines = doc.splitTextToSize(agreementText, 170);
    doc.text(lines, 20, 50);

    this.addFooter(doc);
  }

  /**
   * Generate discount overview
   */
  private generateDiscountOverview(doc: any): void {
    doc.setFontSize(16);
    doc.setTextColor(0, 79, 123);
    doc.text('FreshPlan Rabattsystem', 105, 30, { align: 'center' });

    // Base discounts table
    doc.setFontSize(12);
    doc.setTextColor(0);
    doc.text('1. Basisrabatt je Einzelbestellung (netto)', 20, 50);

    // Table data
    const baseDiscounts = [
      ['unter 5.000 EUR', '0%'],
      ['5.000 - 14.999 EUR', '3%'],
      ['15.000 - 29.999 EUR', '6%'],
      ['30.000 - 49.999 EUR', '8%'],
      ['50.000 - 74.999 EUR', '9%'],
      ['ab 75.000 EUR', '10%']
    ];

    doc.setFontSize(10);
    let y = 60;
    baseDiscounts.forEach(row => {
      doc.text(row[0], 30, y);
      doc.text(row[1], 150, y, { align: 'right' });
      y += 7;
    });

    // Early booking
    y += 10;
    doc.setFontSize(12);
    doc.text('2. Frühbucherrabatt (zusätzlich)', 20, y);
    
    const earlyDiscounts = [
      ['10 - 14 Tage Vorlauf', '+1%'],
      ['15 - 29 Tage Vorlauf', '+2%'],
      ['30 - 44 Tage Vorlauf', '+3%']
    ];

    doc.setFontSize(10);
    y += 10;
    earlyDiscounts.forEach(row => {
      doc.text(row[0], 30, y);
      doc.text(row[1], 150, y, { align: 'right' });
      y += 7;
    });

    // Pickup discount
    y += 10;
    doc.setFontSize(12);
    doc.text('3. Abholrabatt (zusätzlich)', 20, y);
    doc.setFontSize(10);
    doc.text('+2% bei Abholung (ab 5.000 EUR Bestellwert)', 30, y + 10);

    this.addFooter(doc);
  }

  /**
   * Generate calculation examples
   */
  private generateCalculationExamples(doc: any, _customerData: CustomerData): void {
    doc.setFontSize(16);
    doc.setTextColor(0, 79, 123);
    doc.text('Berechnungsbeispiele', 105, 30, { align: 'center' });

    const examples: CalculationExample[] = [
      {
        title: 'Spontanbestellung',
        orderValue: 8000,
        leadTime: 3,
        pickup: false,
        result: { base: 3, early: 0, pickup: 0, total: 3 }
      },
      {
        title: 'Geplante Bestellung',
        orderValue: 25000,
        leadTime: 14,
        pickup: false,
        result: { base: 6, early: 1, pickup: 0, total: 7 }
      },
      {
        title: 'Optimale Bestellung',
        orderValue: 50000,
        leadTime: 30,
        pickup: true,
        result: { base: 9, early: 3, pickup: 2, total: 14 }
      }
    ];

    let y = 50;
    
    examples.forEach(example => {
      doc.setFontSize(12);
      doc.setTextColor(0, 79, 123);
      doc.text(example.title, 20, y);
      
      doc.setFontSize(10);
      doc.setTextColor(0);
      doc.text(`Bestellwert: ${formatCurrency(example.orderValue)}`, 30, y + 8);
      doc.text(`Vorlaufzeit: ${example.leadTime} Tage`, 30, y + 15);
      doc.text(`Abholung: ${example.pickup ? 'Ja' : 'Nein'}`, 30, y + 22);
      
      doc.setTextColor(148, 196, 86); // Green
      doc.text(`Gesamtrabatt: ${example.result.total}%`, 30, y + 30);
      doc.text(`Ersparnis: ${formatCurrency(example.orderValue * example.result.total / 100)}`, 30, y + 37);
      
      y += 50;
    });

    this.addFooter(doc);
  }

  /**
   * Add footer to PDF page
   */
  private addFooter(doc: any): void {
    const pageHeight = doc.internal.pageSize.height;
    doc.setFontSize(8);
    doc.setTextColor(150);
    doc.text('Freshfoodz GmbH | Plauener Str. 163-165 | 13053 Berlin', 105, pageHeight - 20, { align: 'center' });
    doc.text(`Seite ${doc.internal.getCurrentPageInfo().pageNumber}`, 105, pageHeight - 15, { align: 'center' });
  }

  /**
   * Show PDF modal
   */
  private showPdfModal(): void {
    const modal = this.dom.$('#pdfModal');
    if (!modal) return;

    // Update filename
    this.dom.text('#pdfFilename', this.pdfFilename || '');

    // Generate preview (in real app, would show PDF preview)
    const preview = this.dom.$('#pdfPreview');
    if (preview) {
      preview.innerHTML = `
        <div class="pdf-preview-placeholder">
          <p>PDF wurde erfolgreich generiert!</p>
          <p>Dateiname: ${this.pdfFilename}</p>
          <p>Seiten: ${this.currentPDF.internal.getNumberOfPages()}</p>
        </div>
      `;
    }

    // Show modal
    (modal as HTMLElement).style.display = 'block';
  }

  /**
   * Close PDF modal
   */
  private closePdfModal(): void {
    const modal = this.dom.$('#pdfModal');
    if (modal) {
      (modal as HTMLElement).style.display = 'none';
    }
  }

  /**
   * Download current PDF
   */
  private downloadPDF(): void {
    if (!this.currentPDF) {
      this.showError('Kein PDF generiert');
      return;
    }

    this.currentPDF.save(this.pdfFilename);
    this.showSuccess('PDF heruntergeladen');
    this.emit('downloaded', { filename: this.pdfFilename });
  }

  /**
   * Send offer via email
   */
  private sendOffer(): void {
    const customerData = this.getCustomerData();
    if (!customerData || !customerData.contactEmail) {
      this.showError('Keine Kundendaten vorhanden');
      return;
    }

    const subject = `FreshPlan Angebot für ${customerData.companyName}`;
    const body = `Sehr geehrte/r ${customerData.contactName},\n\nanbei erhalten Sie unser FreshPlan Angebot.\n\nMit freundlichen Grüßen`;

    window.location.href = `mailto:${customerData.contactEmail}?subject=${encodeURIComponent(subject)}&body=${encodeURIComponent(body)}`;
    
    this.emit('sent', { email: customerData.contactEmail });
  }

  /**
   * Get customer data
   */
  private getCustomerData(): CustomerData | null {
    return useStore.getState().customer.data;
  }

  /**
   * Generate profile PDF
   */
  async generateProfilePDF(_profileData: ProfileData): Promise<void> {
    // This would generate a PDF version of the customer profile
    // Implementation similar to generateOffer but with profile content
    this.showSuccess('Profil-PDF Export wird implementiert');
  }

  /**
   * Public API
   */
  
  isPDFAvailable(): boolean {
    return this.getModuleState('available') === true;
  }

  getCurrentPDFFilename(): string | null {
    return this.pdfFilename;
  }

  hasPDF(): boolean {
    return this.currentPDF !== null;
  }

  /**
   * Cleanup module resources
   */
  cleanup(): void {
    // Module cleanup is handled by base class
  }
}