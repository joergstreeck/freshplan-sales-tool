/**
 * FreshPlan PDF Generator Module
 */

export class PDFGenerator {
    constructor() {
        this.jsPDF = window.jspdf?.jsPDF;
    }
    
    async generatePDF() {
        if (!this.jsPDF) {
            window.FreshPlan.showError('PDF-Bibliothek wird geladen...');
            return;
        }
        
        try {
            const doc = new this.jsPDF();
            const pageWidth = doc.internal.pageSize.getWidth();
            const pageHeight = doc.internal.pageSize.getHeight();
            const margin = 20;
            let yPosition = margin;
            
            // Add header
            this.addHeader(doc, yPosition);
            yPosition += 40;
            
            // Add calculator results
            if (window.appState.calculatorData?.totalDiscount > 0) {
                yPosition = this.addCalculatorSection(doc, yPosition);
            }
            
            // Add customer data
            if (window.appState.customerData?.companyName) {
                yPosition = this.addCustomerSection(doc, yPosition);
            }
            
            // Add profile data
            if (window.appState.profileData?.completed) {
                yPosition = this.addProfileSection(doc, yPosition);
            }
            
            // Add footer
            this.addFooter(doc);
            
            // Save PDF
            const fileName = `FreshPlan_${this.formatDate(new Date())}.pdf`;
            doc.save(fileName);
            
            window.FreshPlan.showNotification('PDF wurde erstellt', 'success');
        } catch (error) {
            console.error('PDF generation error:', error);
            window.FreshPlan.showError('Fehler beim Erstellen des PDFs');
        }
    }
    
    addHeader(doc, y) {
        const pageWidth = doc.internal.pageSize.getWidth();
        
        // Logo placeholder
        doc.setFillColor(148, 196, 86);
        doc.rect(20, y, 40, 20, 'F');
        
        // Title
        doc.setFontSize(24);
        doc.setTextColor(148, 196, 86);
        doc.text('FreshPlan', pageWidth / 2, y + 10, { align: 'center' });
        
        // Subtitle
        doc.setFontSize(12);
        doc.setTextColor(100, 100, 100);
        doc.text('Sales Tool - Angebot', pageWidth / 2, y + 20, { align: 'center' });
        
        // Date
        doc.setFontSize(10);
        doc.text(this.formatDate(new Date()), pageWidth - 20, y + 10, { align: 'right' });
    }
    
    addCalculatorSection(doc, y) {
        const data = window.appState.calculatorData;
        const pageWidth = doc.internal.pageSize.getWidth();
        const margin = 20;
        
        // Section title
        doc.setFontSize(16);
        doc.setTextColor(0, 79, 123);
        doc.text('Rabattberechnung', margin, y);
        y += 10;
        
        // Draw line
        doc.setDrawColor(200, 200, 200);
        doc.line(margin, y, pageWidth - margin, y);
        y += 10;
        
        // Order details
        doc.setFontSize(10);
        doc.setTextColor(0, 0, 0);
        
        const details = [
            ['Bestellwert:', this.formatCurrency(data.orderValue)],
            ['Vorlaufzeit:', `${data.leadTime} Tage`],
            ['Abholung:', data.isPickup ? 'Ja' : 'Nein'],
            ['Kettenkunde:', data.isChain ? 'Ja' : 'Nein']
        ];
        
        details.forEach(([label, value]) => {
            doc.text(label, margin, y);
            doc.text(value, margin + 60, y);
            y += 8;
        });
        
        y += 5;
        
        // Discount breakdown
        doc.setFontSize(12);
        doc.setTextColor(0, 79, 123);
        doc.text('Rabattaufschlüsselung:', margin, y);
        y += 8;
        
        doc.setFontSize(10);
        doc.setTextColor(0, 0, 0);
        
        const discounts = [
            ['Basisrabatt:', `${data.baseDiscount}%`],
            ['Frühbucherrabatt:', `${data.earlyDiscount}%`],
            ['Abholrabatt:', `${data.pickupDiscount}%`],
            ['Kettenrabatt:', `${data.chainDiscount}%`]
        ];
        
        discounts.forEach(([label, value]) => {
            doc.text(label, margin + 10, y);
            doc.text(value, margin + 70, y, { align: 'right' });
            y += 8;
        });
        
        // Total
        doc.setDrawColor(148, 196, 86);
        doc.line(margin + 10, y, margin + 70, y);
        y += 8;
        
        doc.setFontSize(12);
        doc.setTextColor(148, 196, 86);
        doc.text('Gesamtrabatt:', margin + 10, y);
        doc.text(`${data.totalDiscount}%`, margin + 70, y, { align: 'right' });
        y += 10;
        
        // Final price
        doc.setFontSize(14);
        doc.setTextColor(0, 0, 0);
        doc.text('Endpreis:', margin, y);
        doc.setTextColor(148, 196, 86);
        doc.text(this.formatCurrency(data.finalPrice), margin + 70, y, { align: 'right' });
        
        return y + 20;
    }
    
    addCustomerSection(doc, y) {
        const data = window.appState.customerData;
        const pageWidth = doc.internal.pageSize.getWidth();
        const margin = 20;
        
        // Check if we need a new page
        if (y > 220) {
            doc.addPage();
            y = margin;
        }
        
        // Section title
        doc.setFontSize(16);
        doc.setTextColor(0, 79, 123);
        doc.text('Kundendaten', margin, y);
        y += 10;
        
        // Draw line
        doc.setDrawColor(200, 200, 200);
        doc.line(margin, y, pageWidth - margin, y);
        y += 10;
        
        // Customer details
        doc.setFontSize(10);
        doc.setTextColor(0, 0, 0);
        
        const customerInfo = [
            ['Firma:', data.companyName],
            ['Ansprechpartner:', data.contactPerson],
            ['E-Mail:', data.email],
            ['Telefon:', data.phone || '-'],
            ['Adresse:', `${data.address}, ${data.postalCode} ${data.city}`]
        ];
        
        customerInfo.forEach(([label, value]) => {
            doc.text(label, margin, y);
            doc.text(value, margin + 40, y);
            y += 8;
        });
        
        return y + 10;
    }
    
    addProfileSection(doc, y) {
        const data = window.appState.profileData;
        const pageWidth = doc.internal.pageSize.getWidth();
        const margin = 20;
        
        // Check if we need a new page
        if (y > 200) {
            doc.addPage();
            y = margin;
        }
        
        // Section title
        doc.setFontSize(16);
        doc.setTextColor(0, 79, 123);
        doc.text('Kundenprofil', margin, y);
        y += 10;
        
        // Draw line
        doc.setDrawColor(200, 200, 200);
        doc.line(margin, y, pageWidth - margin, y);
        y += 10;
        
        // Add profile sections
        doc.setFontSize(10);
        doc.setTextColor(0, 0, 0);
        
        if (data.customerPotential) {
            doc.setFontSize(12);
            doc.text('Kundenpotenzial:', margin, y);
            y += 8;
            doc.setFontSize(10);
            const lines = doc.splitTextToSize(data.customerPotential, pageWidth - margin * 2);
            doc.text(lines, margin, y);
            y += lines.length * 5 + 5;
        }
        
        if (data.strategy) {
            doc.setFontSize(12);
            doc.text('Strategie:', margin, y);
            y += 8;
            doc.setFontSize(10);
            const lines = doc.splitTextToSize(data.strategy, pageWidth - margin * 2);
            doc.text(lines, margin, y);
            y += lines.length * 5 + 5;
        }
        
        return y + 10;
    }
    
    addFooter(doc) {
        const pageHeight = doc.internal.pageSize.getHeight();
        const pageWidth = doc.internal.pageSize.getWidth();
        const margin = 20;
        
        // Add footer to all pages
        const pageCount = doc.internal.getNumberOfPages();
        
        for (let i = 1; i <= pageCount; i++) {
            doc.setPage(i);
            
            // Footer line
            doc.setDrawColor(200, 200, 200);
            doc.line(margin, pageHeight - 30, pageWidth - margin, pageHeight - 30);
            
            // Footer text
            doc.setFontSize(8);
            doc.setTextColor(150, 150, 150);
            doc.text('FreshFoodz GmbH', margin, pageHeight - 20);
            doc.text(`Seite ${i} von ${pageCount}`, pageWidth / 2, pageHeight - 20, { align: 'center' });
            
            // Salesperson info
            const settings = window.appState.settings;
            if (settings?.salesperson?.name) {
                doc.text(settings.salesperson.name, pageWidth - margin, pageHeight - 20, { align: 'right' });
            }
        }
    }
    
    formatCurrency(amount) {
        return new Intl.NumberFormat('de-DE', {
            style: 'currency',
            currency: 'EUR'
        }).format(amount);
    }
    
    formatDate(date) {
        return new Intl.DateTimeFormat('de-DE', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit'
        }).format(date);
    }
}