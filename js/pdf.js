// =============================
// FreshPlan PDF Generation
// =============================

// Generate offer
function generateOffer() {
    // Validate required fields
    if (!FreshPlan.state.customerData.companyName) {
        showMessage(getTranslation('messages.fillCustomerFirst'), 'error');
        switchTab('customer');
        return;
    }
    
    const salespersonName = document.getElementById('salespersonName').value;
    const salespersonEmail = document.getElementById('salespersonEmail').value;
    
    if (!salespersonName || !salespersonEmail) {
        showMessage(getTranslation('messages.fillSalespersonInfo'), 'error');
        return;
    }
    
    showMessage(getTranslation('messages.pdfGenerating'), 'success');
    
    // Generate PDF
    generatePDF();
}

// Generate PDF
function generatePDF() {
    const { jsPDF } = window.jspdf;
    const doc = new jsPDF();
    
    // Set fonts
    doc.setFont('helvetica');
    
    // Page 1: Cover Letter
    addCoverLetter(doc);
    
    // Page 2: Partnership Agreement
    if (document.getElementById('docPartnership').checked) {
        doc.addPage();
        addPartnershipAgreement(doc);
    }
    
    // Page 3: Discount System
    if (document.getElementById('docRabatt').checked) {
        doc.addPage();
        addDiscountSystem(doc);
    }
    
    // Page 4: Calculation Examples
    if (document.getElementById('docCalc').checked) {
        doc.addPage();
        addCalculationExamples(doc);
    }
    
    // Save the PDF
    const pdfBlob = doc.output('blob');
    const pdfUrl = URL.createObjectURL(pdfBlob);
    
    // Store generated PDF
    FreshPlan.state.generatedPdf = {
        blob: pdfBlob,
        url: pdfUrl,
        filename: `FreshPlan_Angebot_${FreshPlan.state.customerData.companyName.replace(/[^a-z0-9]/gi, '_')}_${new Date().toISOString().split('T')[0]}.pdf`
    };
    
    // Show preview
    showPdfPreview();
    
    // Update progress
    FreshPlan.state.tabProgress.offer = true;
    updateProgress();
}

// Add cover letter
function addCoverLetter(doc) {
    const contractStart = document.getElementById('contractStart').value;
    const salespersonName = document.getElementById('salespersonName').value;
    const salespersonEmail = document.getElementById('salespersonEmail').value;
    const salespersonPhone = document.getElementById('salespersonPhone').value || '';
    const additionalNotes = document.getElementById('additionalNotes').value;
    
    // Header
    doc.setFontSize(24);
    doc.setTextColor(127, 176, 105);
    doc.text('FreshPlan', 20, 30);
    doc.setFontSize(14);
    doc.setTextColor(0, 0, 0);
    doc.text('Ihre Partnerschaft für Erfolg', 20, 40);
    
    // Date
    doc.setFontSize(10);
    doc.text(new Date().toLocaleDateString('de-DE'), 170, 30);
    
    // Address
    doc.setFontSize(11);
    doc.text(FreshPlan.state.customerData.companyName, 20, 60);
    doc.text(FreshPlan.state.customerData.contactName, 20, 66);
    doc.text(FreshPlan.state.customerData.street, 20, 72);
    doc.text(`${FreshPlan.state.customerData.zipCode} ${FreshPlan.state.customerData.city}`, 20, 78);
    
    // Subject
    doc.setFontSize(14);
    doc.setFont('helvetica', 'bold');
    doc.text('Ihr individuelles FreshPlan-Angebot', 20, 100);
    
    // Salutation
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(11);
    const salutation = `Sehr geehrte/r ${FreshPlan.state.customerData.contactName},`;
    doc.text(salutation, 20, 115);
    
    // Body text
    const bodyText = [
        'vielen Dank für Ihr Interesse an unserer FreshPlan-Partnerschaft.',
        '',
        `Basierend auf Ihrem geplanten Jahresvolumen von ${formatCurrency(FreshPlan.state.customerData.targetRevenue)}`,
        `können Sie mit unserem transparenten Rabattsystem bis zu ${calculateCustomerDiscount()}% sparen.`,
        '',
        'Die Vorteile Ihrer FreshPlan-Partnerschaft:',
        '• Garantierte Preisstabilität für 12 Monate',
        '• Keine Mindestabnahmeverpflichtung',
        '• Flexible Bestellmöglichkeiten',
        '• Persönliche Betreuung',
        '',
        'Vertragsbeginn: ' + new Date(contractStart).toLocaleDateString('de-DE'),
        'Laufzeit: 12 Monate mit automatischer Verlängerung'
    ];
    
    let yPosition = 125;
    bodyText.forEach(line => {
        doc.text(line, 20, yPosition);
        yPosition += 6;
    });
    
    // Additional notes if any
    if (additionalNotes) {
        yPosition += 6;
        doc.text('Zusätzliche Vereinbarungen:', 20, yPosition);
        yPosition += 6;
        const notes = doc.splitTextToSize(additionalNotes, 170);
        notes.forEach(line => {
            doc.text(line, 20, yPosition);
            yPosition += 6;
        });
    }
    
    // Signature
    yPosition = 250;
    doc.text('Mit freundlichen Grüßen', 20, yPosition);
    doc.text(salespersonName, 20, yPosition + 10);
    if (salespersonPhone) {
        doc.text(`Tel: ${salespersonPhone}`, 20, yPosition + 16);
    }
    doc.text(`E-Mail: ${salespersonEmail}`, 20, yPosition + 22);
}

// Add partnership agreement
function addPartnershipAgreement(doc) {
    doc.setFontSize(18);
    doc.setFont('helvetica', 'bold');
    doc.text('FreshPlan-Partnerschaftsvereinbarung', 20, 30);
    
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(11);
    
    const agreementText = [
        'zwischen',
        '',
        'Freshfoodz GmbH',
        'Musterstraße 123',
        '12345 Musterstadt',
        '',
        'und',
        '',
        FreshPlan.state.customerData.companyName,
        FreshPlan.state.customerData.street,
        `${FreshPlan.state.customerData.zipCode} ${FreshPlan.state.customerData.city}`,
        '',
        '§1 Vertragsgegenstand',
        'Diese Vereinbarung regelt die Konditionen für den Bezug von Lebensmitteln',
        'und Convenience-Produkten im Rahmen des FreshPlan-Programms.',
        '',
        '§2 Zielvolumen',
        `Das unverbindliche Zielvolumen beträgt ${formatCurrency(FreshPlan.state.customerData.targetRevenue)} netto pro Jahr.`,
        'Es besteht keine Mindestabnahmeverpflichtung.',
        '',
        '§3 Rabattvereinbarung',
        'Es gelten die Rabattstaffeln gemäß Anlage 1.',
        'Die Rabatte werden bei jeder Bestellung automatisch berücksichtigt.',
        '',
        '§4 Preisstabilität',
        'Die vereinbarten Preise bleiben für 12 Monate stabil.',
        'Preisanpassungen erfolgen nur bei Rohstoffpreissteigerungen >5%.',
        '',
        '§5 Laufzeit',
        'Die Vereinbarung beginnt am ' + new Date(document.getElementById('contractStart').value).toLocaleDateString('de-DE'),
        'und läuft 12 Monate. Sie verlängert sich automatisch um weitere 12 Monate,',
        'sofern nicht 3 Monate vor Ablauf gekündigt wird.'
    ];
    
    let yPosition = 50;
    agreementText.forEach(line => {
        if (line.startsWith('§')) {
            doc.setFont('helvetica', 'bold');
        } else {
            doc.setFont('helvetica', 'normal');
        }
        doc.text(line, 20, yPosition);
        yPosition += 6;
    });
    
    // Signature fields
    yPosition = 240;
    doc.line(20, yPosition, 80, yPosition);
    doc.line(110, yPosition, 170, yPosition);
    doc.text('Ort, Datum', 20, yPosition + 5);
    doc.text('Ort, Datum', 110, yPosition + 5);
    
    doc.line(20, yPosition + 20, 80, yPosition + 20);
    doc.line(110, yPosition + 20, 170, yPosition + 20);
    doc.text('Freshfoodz GmbH', 20, yPosition + 25);
    doc.text(FreshPlan.state.customerData.companyName, 110, yPosition + 25);
}

// Add discount system
function addDiscountSystem(doc) {
    doc.setFontSize(18);
    doc.setFont('helvetica', 'bold');
    doc.text('Anlage 1: Rabattsystem', 20, 30);
    
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(12);
    doc.text('Transparente Konditionen für Ihren Erfolg', 20, 40);
    
    // Discount table
    const tableData = [
        ['Bestellwert (netto)', 'Basisrabatt'],
        ['ab 5.000 €', '3%'],
        ['ab 15.000 €', '6%'],
        ['ab 30.000 €', '8%'],
        ['ab 50.000 €', '9%'],
        ['ab 75.000 €', '10%']
    ];
    
    doc.autoTable({
        head: [tableData[0]],
        body: tableData.slice(1),
        startY: 50,
        theme: 'grid',
        headStyles: { fillColor: [127, 176, 105] },
        margin: { left: 20 }
    });
    
    // Additional discounts
    const additionalY = doc.lastAutoTable.finalY + 20;
    doc.setFont('helvetica', 'bold');
    doc.text('Zusätzliche Rabatte:', 20, additionalY);
    
    doc.setFont('helvetica', 'normal');
    const additionalDiscounts = [
        'Frühbucherrabatt:',
        '• 10-14 Tage Vorlauf: +1%',
        '• 15-29 Tage Vorlauf: +2%',
        '• 30-44 Tage Vorlauf: +3%',
        '',
        'Selbstabholung:',
        '• Bei Bestellwert ab 5.000€: +2%',
        '',
        'Maximal möglicher Gesamtrabatt: 15%'
    ];
    
    let yPos = additionalY + 10;
    additionalDiscounts.forEach(line => {
        doc.text(line, 20, yPos);
        yPos += 7;
    });
    
    // Special conditions
    doc.setFont('helvetica', 'bold');
    doc.text('Besondere Konditionen:', 20, yPos + 10);
    
    doc.setFont('helvetica', 'normal');
    const specialConditions = [
        '• Kombi-Bestellung: Ab 30.000€ Aufteilung auf 2 Liefertermine möglich',
        '• Kettenkunden: Wöchentliche Sammelbestellungen für höhere Rabattstufen',
        '• Keine Mindestbestellmenge pro Auftrag',
        '• Kostenlose Lieferung ab 250€ Bestellwert'
    ];
    
    yPos += 20;
    specialConditions.forEach(line => {
        doc.text(line, 20, yPos);
        yPos += 7;
    });
}

// Add calculation examples
function addCalculationExamples(doc) {
    doc.setFontSize(18);
    doc.setFont('helvetica', 'bold');
    doc.text('Ihre individuelle Kalkulation', 20, 30);
    
    const revenue = FreshPlan.state.customerData.targetRevenue || 0;
    const avgOrderValue = revenue / 52; // Weekly orders
    const discount = calculateCustomerDiscount();
    
    // Scenario 1: Regular order
    doc.setFontSize(14);
    doc.text('Szenario 1: Wöchentliche Regelbestellung', 20, 50);
    
    doc.setFontSize(11);
    doc.setFont('helvetica', 'normal');
    const scenario1 = [
        `Durchschnittlicher Bestellwert: ${formatCurrency(avgOrderValue)}`,
        `Basisrabatt: ${discount}%`,
        `Ihre Ersparnis pro Bestellung: ${formatCurrency(avgOrderValue * discount / 100)}`,
        `Jahresersparnis: ${formatCurrency(revenue * discount / 100)}`
    ];
    
    let yPos = 60;
    scenario1.forEach(line => {
        doc.text(line, 25, yPos);
        yPos += 7;
    });
    
    // Scenario 2: Optimized order
    doc.setFont('helvetica', 'bold');
    doc.setFontSize(14);
    doc.text('Szenario 2: Optimierte Bestellung mit Vorlauf', 20, yPos + 10);
    
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(11);
    const optimizedDiscount = Math.min(discount + 3, 15); // With early booking
    const scenario2 = [
        `Bestellung mit 30 Tagen Vorlauf`,
        `Basisrabatt: ${discount}%`,
        `Frühbucherbonus: +3%`,
        `Gesamtrabatt: ${optimizedDiscount}%`,
        `Zusätzliche Jahresersparnis: ${formatCurrency(revenue * 3 / 100)}`
    ];
    
    yPos += 20;
    scenario2.forEach(line => {
        doc.text(line, 25, yPos);
        yPos += 7;
    });
    
    // Summary box
    const summaryY = yPos + 20;
    doc.setFillColor(127, 176, 105, 0.1);
    doc.rect(20, summaryY, 170, 40, 'F');
    
    doc.setFont('helvetica', 'bold');
    doc.text('Ihr Einsparpotenzial auf einen Blick:', 30, summaryY + 10);
    
    doc.setFontSize(16);
    doc.text(`Bis zu ${formatCurrency(revenue * optimizedDiscount / 100)} pro Jahr!`, 30, summaryY + 25);
    
    // Call to action
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(11);
    doc.text('Starten Sie jetzt und sichern Sie sich Ihre Vorteile!', 30, summaryY + 35);
}

// Show PDF preview
function showPdfPreview() {
    const modal = document.getElementById('pdfModal');
    const frame = document.getElementById('pdfFrame');
    
    frame.src = FreshPlan.state.generatedPdf.url;
    modal.style.display = 'block';
    
    showMessage(getTranslation('messages.offerCreated'), 'success');
}

// Send offer
function sendOffer() {
    if (!FreshPlan.state.generatedPdf) {
        showMessage('Bitte erst PDF generieren!', 'error');
        return;
    }
    
    if (!FreshPlan.state.customerData.contactEmail) {
        showMessage(getTranslation('messages.noEmailAddress'), 'error');
        return;
    }
    
    // Since we can't actually send emails from the browser,
    // we'll open the default email client
    const subject = encodeURIComponent(`Ihr FreshPlan-Angebot - ${FreshPlan.state.customerData.companyName}`);
    const body = encodeURIComponent(`Sehr geehrte/r ${FreshPlan.state.customerData.contactName},\n\nanbei erhalten Sie Ihr individuelles FreshPlan-Angebot.\n\nBei Fragen stehe ich Ihnen gerne zur Verfügung.\n\nMit freundlichen Grüßen\n${document.getElementById('salespersonName').value}`);
    
    window.location.href = `mailto:${FreshPlan.state.customerData.contactEmail}?subject=${subject}&body=${body}`;
    
    showMessage(getTranslation('messages.emailOpened'), 'success');
}

// Print offer
function printOffer() {
    if (!FreshPlan.state.generatedPdf) {
        showMessage('Bitte erst PDF generieren!', 'error');
        return;
    }
    
    // Open PDF in new window for printing
    const printWindow = window.open(FreshPlan.state.generatedPdf.url, '_blank');
    if (printWindow) {
        printWindow.onload = function() {
            printWindow.print();
        };
    }
}

// Export functions
window.generateOffer = generateOffer;
window.sendOffer = sendOffer;
window.printOffer = printOffer;