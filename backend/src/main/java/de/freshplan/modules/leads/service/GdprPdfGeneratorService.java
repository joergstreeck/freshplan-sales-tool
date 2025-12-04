package de.freshplan.modules.leads.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import de.freshplan.modules.leads.domain.Lead;
import de.freshplan.modules.leads.domain.LeadActivity;
import jakarta.enterprise.context.ApplicationScoped;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.jboss.logging.Logger;

/**
 * PDF-Generator für DSGVO-konforme Datenexporte (Art. 15).
 *
 * <p>Generiert PDF-Dokumente mit allen personenbezogenen Daten eines Leads/Customers gemäß DSGVO
 * Art. 15 (Auskunftsrecht der betroffenen Person).
 *
 * <p><strong>Verwendete Bibliothek:</strong> OpenPDF (LGPL-Lizenz, Fork von iText)
 *
 * <p><strong>PDF-Struktur:</strong>
 *
 * <ol>
 *   <li>Header mit DSGVO-Hinweis
 *   <li>Stammdaten (Firma, Kontakt, Adresse)
 *   <li>Aktivitäten/Kontakthistorie
 *   <li>Einwilligungsstatus (Consent)
 *   <li>Schutz-Status (Protection)
 *   <li>Footer mit Datum und Verantwortlichem
 * </ol>
 *
 * @since Sprint 2.1.8
 */
@ApplicationScoped
public class GdprPdfGeneratorService {

  private static final Logger LOG = Logger.getLogger(GdprPdfGeneratorService.class);

  // FreshFoodz CI Colors
  private static final Color PRIMARY_GREEN = new Color(148, 196, 86); // #94C456
  private static final Color PRIMARY_BLUE = new Color(0, 79, 123); // #004F7B
  private static final Color LIGHT_GRAY = new Color(245, 245, 245);

  // Fonts
  private static final Font TITLE_FONT = new Font(Font.HELVETICA, 18, Font.BOLD, PRIMARY_BLUE);
  private static final Font SUBTITLE_FONT = new Font(Font.HELVETICA, 14, Font.BOLD, PRIMARY_BLUE);
  private static final Font SECTION_FONT = new Font(Font.HELVETICA, 12, Font.BOLD, Color.BLACK);
  private static final Font LABEL_FONT = new Font(Font.HELVETICA, 10, Font.BOLD, Color.DARK_GRAY);
  private static final Font VALUE_FONT = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);
  private static final Font FOOTER_FONT = new Font(Font.HELVETICA, 8, Font.ITALIC, Color.GRAY);

  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
  private static final DateTimeFormatter DATE_ONLY_FORMATTER =
      DateTimeFormatter.ofPattern("dd.MM.yyyy");

  /**
   * Generiert einen DSGVO-konformen Datenexport für einen Lead.
   *
   * @param lead Der Lead für den der Export erstellt werden soll
   * @return PDF als Byte-Array
   */
  public byte[] generateLeadDataExport(Lead lead) {
    LOG.infof("Generating GDPR Data Export PDF for Lead %d", lead.id);

    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      Document document = new Document(PageSize.A4, 50, 50, 50, 50);
      PdfWriter.getInstance(document, baos);
      document.open();

      // Header
      addHeader(document);

      // Entity Info
      addEntityInfo(document, lead);

      // Section 1: Stammdaten
      addStammdatenSection(document, lead);

      // Section 2: Kontaktdaten
      addKontaktdatenSection(document, lead);

      // Section 3: Aktivitäten
      addAktivitaetenSection(document, lead);

      // Section 4: Einwilligungsstatus
      addConsentSection(document, lead);

      // Section 5: Schutz-Status
      addProtectionSection(document, lead);

      // Footer
      addFooter(document);

      document.close();

      LOG.infof("GDPR Data Export PDF generated successfully for Lead %d", lead.id);
      return baos.toByteArray();

    } catch (Exception e) {
      LOG.errorf(e, "Failed to generate GDPR Data Export PDF for Lead %d", lead.id);
      throw new RuntimeException("PDF-Generierung fehlgeschlagen: " + e.getMessage(), e);
    }
  }

  private void addHeader(Document document) throws DocumentException {
    // Title
    Paragraph title = new Paragraph("Datenauskunft gemäß Art. 15 DSGVO", TITLE_FONT);
    title.setAlignment(Element.ALIGN_CENTER);
    title.setSpacingAfter(10);
    document.add(title);

    // Subtitle
    Paragraph subtitle =
        new Paragraph("Auskunft über die Verarbeitung personenbezogener Daten", SUBTITLE_FONT);
    subtitle.setAlignment(Element.ALIGN_CENTER);
    subtitle.setSpacingAfter(5);
    document.add(subtitle);

    // Legal Notice
    Paragraph legalNotice =
        new Paragraph(
            "Dieses Dokument enthält alle über Sie gespeicherten personenbezogenen Daten "
                + "gemäß Art. 15 der EU-Datenschutz-Grundverordnung (DSGVO).",
            VALUE_FONT);
    legalNotice.setAlignment(Element.ALIGN_CENTER);
    legalNotice.setSpacingAfter(20);
    document.add(legalNotice);

    // Horizontal Line
    document.add(new Paragraph(" "));
  }

  private void addEntityInfo(Document document, Lead lead) throws DocumentException {
    PdfPTable table = new PdfPTable(2);
    table.setWidthPercentage(100);
    table.setSpacingAfter(20);

    addTableRow(table, "Dokument-ID:", "GDPR-LEAD-" + lead.id + "-" + System.currentTimeMillis());
    addTableRow(table, "Erstellt am:", LocalDateTime.now().format(DATE_FORMATTER) + " Uhr");
    addTableRow(table, "Entitätstyp:", "Lead (Interessent)");
    addTableRow(table, "Interne ID:", String.valueOf(lead.id));

    document.add(table);
  }

  private void addStammdatenSection(Document document, Lead lead) throws DocumentException {
    addSectionHeader(document, "1. Stammdaten");

    PdfPTable table = new PdfPTable(2);
    table.setWidthPercentage(100);
    table.setSpacingAfter(15);

    addTableRow(table, "Firmenname:", nullSafe(lead.companyName));
    addTableRow(table, "Branche:", lead.businessType != null ? lead.businessType.name() : "-");
    addTableRow(table, "Küchengröße:", lead.kitchenSize != null ? lead.kitchenSize.name() : "-");
    addTableRow(
        table,
        "Mitarbeiteranzahl:",
        lead.employeeCount != null ? String.valueOf(lead.employeeCount) : "-");
    addTableRow(
        table,
        "Geschätztes Volumen:",
        lead.estimatedVolume != null ? lead.estimatedVolume + " €" : "-");
    addTableRow(
        table,
        "Anzahl Filialen:",
        lead.branchCount != null ? String.valueOf(lead.branchCount) : "1");
    addTableRow(table, "Kettenbetrieb:", Boolean.TRUE.equals(lead.isChain) ? "Ja" : "Nein");

    document.add(table);
  }

  private void addKontaktdatenSection(Document document, Lead lead) throws DocumentException {
    addSectionHeader(document, "2. Kontaktdaten");

    PdfPTable table = new PdfPTable(2);
    table.setWidthPercentage(100);
    table.setSpacingAfter(15);

    addTableRow(table, "Ansprechpartner:", nullSafe(lead.contactPerson));
    addTableRow(table, "E-Mail:", nullSafe(lead.email));
    addTableRow(table, "Telefon:", nullSafe(lead.phone));
    addTableRow(table, "Website:", nullSafe(lead.website));
    addTableRow(table, "Straße:", nullSafe(lead.street));
    addTableRow(table, "PLZ:", nullSafe(lead.postalCode));
    addTableRow(table, "Stadt:", nullSafe(lead.city));
    addTableRow(table, "Land:", nullSafe(lead.countryCode));

    document.add(table);
  }

  private void addAktivitaetenSection(Document document, Lead lead) throws DocumentException {
    addSectionHeader(document, "3. Aktivitäten / Kontakthistorie");

    if (lead.activities == null || lead.activities.isEmpty()) {
      document.add(new Paragraph("Keine Aktivitäten vorhanden.", VALUE_FONT));
      document.add(new Paragraph(" "));
      return;
    }

    PdfPTable table = new PdfPTable(4);
    table.setWidthPercentage(100);
    table.setWidths(new float[] {20, 20, 30, 30});
    table.setSpacingAfter(15);

    // Header
    addTableHeaderCell(table, "Datum");
    addTableHeaderCell(table, "Typ");
    addTableHeaderCell(table, "Beschreibung");
    addTableHeaderCell(table, "Erstellt von");

    // Aktivitäten (max. 50)
    int count = 0;
    for (LeadActivity activity : lead.activities) {
      if (count >= 50) {
        break;
      }
      addTableCell(
          table,
          activity.activityDate != null ? activity.activityDate.format(DATE_ONLY_FORMATTER) : "-");
      addTableCell(table, activity.activityType != null ? activity.activityType.name() : "-");
      addTableCell(table, truncate(activity.description, 100));
      addTableCell(table, nullSafe(activity.userId));
      count++;
    }

    document.add(table);

    if (lead.activities.size() > 50) {
      document.add(
          new Paragraph(
              "... und " + (lead.activities.size() - 50) + " weitere Aktivitäten", FOOTER_FONT));
    }
  }

  private void addConsentSection(Document document, Lead lead) throws DocumentException {
    addSectionHeader(document, "4. Einwilligungsstatus");

    PdfPTable table = new PdfPTable(2);
    table.setWidthPercentage(100);
    table.setSpacingAfter(15);

    // Consent Status
    String consentStatus;
    if (lead.consentRevokedAt != null) {
      consentStatus = "Widerrufen am " + lead.consentRevokedAt.format(DATE_FORMATTER);
    } else {
      consentStatus = "Aktiv";
    }
    addTableRow(table, "Status:", consentStatus);

    if (lead.consentRevokedAt != null) {
      addTableRow(table, "Widerrufen am:", lead.consentRevokedAt.format(DATE_FORMATTER));
      addTableRow(table, "Widerrufen durch:", nullSafe(lead.consentRevokedBy));
    }

    addTableRow(
        table, "Kontakt gesperrt:", Boolean.TRUE.equals(lead.contactBlocked) ? "Ja" : "Nein");

    document.add(table);
  }

  private void addProtectionSection(Document document, Lead lead) throws DocumentException {
    addSectionHeader(document, "5. Schutz-Status & Verarbeitung");

    PdfPTable table = new PdfPTable(2);
    table.setWidthPercentage(100);
    table.setSpacingAfter(15);

    addTableRow(table, "Status:", lead.status != null ? lead.status.name() : "-");
    addTableRow(
        table,
        "Registriert am:",
        lead.registeredAt != null ? lead.registeredAt.format(DATE_FORMATTER) : "-");
    addTableRow(
        table,
        "Schutz-Start:",
        lead.protectionStartAt != null ? lead.protectionStartAt.format(DATE_FORMATTER) : "-");
    addTableRow(
        table,
        "Schutz-Dauer:",
        lead.protectionMonths != null ? lead.protectionMonths + " Monate" : "-");
    addTableRow(table, "Quelle:", lead.source != null ? lead.source.name() : "-");
    addTableRow(table, "Kampagne:", nullSafe(lead.sourceCampaign));
    addTableRow(
        table,
        "Erstellt am:",
        lead.createdAt != null ? lead.createdAt.format(DATE_FORMATTER) : "-");
    addTableRow(table, "Erstellt von:", nullSafe(lead.createdBy));
    addTableRow(
        table,
        "Zuletzt geändert:",
        lead.updatedAt != null ? lead.updatedAt.format(DATE_FORMATTER) : "-");

    // DSGVO-Status
    if (Boolean.TRUE.equals(lead.gdprDeleted)) {
      addTableRow(table, "DSGVO-Löschung:", "Ja");
      addTableRow(
          table,
          "Gelöscht am:",
          lead.gdprDeletedAt != null ? lead.gdprDeletedAt.format(DATE_FORMATTER) : "-");
      addTableRow(table, "Löschgrund:", nullSafe(lead.gdprDeletionReason));
    }

    if (lead.pseudonymizedAt != null) {
      addTableRow(table, "Pseudonymisiert am:", lead.pseudonymizedAt.format(DATE_FORMATTER));
    }

    document.add(table);
  }

  private void addFooter(Document document) throws DocumentException {
    document.add(new Paragraph(" "));
    document.add(new Paragraph(" "));

    // Separator
    Paragraph separator = new Paragraph("─".repeat(80), VALUE_FONT);
    separator.setAlignment(Element.ALIGN_CENTER);
    document.add(separator);

    // Footer Text
    Paragraph footer =
        new Paragraph(
            "FreshFoodz GmbH | Verantwortlicher für die Datenverarbeitung\n"
                + "Dieses Dokument wurde automatisch erstellt am "
                + LocalDateTime.now().format(DATE_FORMATTER)
                + " Uhr\n"
                + "Bei Fragen wenden Sie sich an: datenschutz@freshfoodz.de",
            FOOTER_FONT);
    footer.setAlignment(Element.ALIGN_CENTER);
    footer.setSpacingBefore(10);
    document.add(footer);
  }

  // ============================================================================
  // Helper Methods
  // ============================================================================

  private void addSectionHeader(Document document, String title) throws DocumentException {
    Paragraph header = new Paragraph(title, SECTION_FONT);
    header.setSpacingBefore(15);
    header.setSpacingAfter(10);
    document.add(header);
  }

  private void addTableRow(PdfPTable table, String label, String value) {
    PdfPCell labelCell = new PdfPCell(new Phrase(label, LABEL_FONT));
    labelCell.setBorder(Rectangle.NO_BORDER);
    labelCell.setBackgroundColor(LIGHT_GRAY);
    labelCell.setPadding(5);
    table.addCell(labelCell);

    PdfPCell valueCell = new PdfPCell(new Phrase(value, VALUE_FONT));
    valueCell.setBorder(Rectangle.NO_BORDER);
    valueCell.setPadding(5);
    table.addCell(valueCell);
  }

  private void addTableHeaderCell(PdfPTable table, String text) {
    PdfPCell cell = new PdfPCell(new Phrase(text, LABEL_FONT));
    cell.setBackgroundColor(PRIMARY_BLUE);
    cell.getPhrase().getFont().setColor(Color.WHITE);
    cell.setPadding(5);
    table.addCell(cell);
  }

  private void addTableCell(PdfPTable table, String text) {
    PdfPCell cell = new PdfPCell(new Phrase(text, VALUE_FONT));
    cell.setPadding(3);
    table.addCell(cell);
  }

  private String nullSafe(String value) {
    return value != null && !value.isBlank() ? value : "-";
  }

  private String truncate(String text, int maxLength) {
    if (text == null) {
      return "-";
    }
    if (text.length() <= maxLength) {
      return text;
    }
    return text.substring(0, maxLength - 3) + "...";
  }
}
