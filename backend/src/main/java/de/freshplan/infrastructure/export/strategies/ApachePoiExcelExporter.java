package de.freshplan.infrastructure.export.strategies;

import de.freshplan.infrastructure.export.*;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jboss.logging.Logger;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

/**
 * Excel export strategy using Apache POI.
 * Creates professional Excel files with formatting and multiple sheets.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
public class ApachePoiExcelExporter implements ExportStrategy {
    
    private static final Logger log = Logger.getLogger(ApachePoiExcelExporter.class);
    
    @Override
    public ExportFormat getFormat() {
        return ExportFormat.EXCEL;
    }
    
    @Override
    public ExportResult export(List<?> data, ExportConfig config) {
        log.infof("Exporting %d records to Excel using Apache POI", data.size());
        
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            // Create main data sheet
            Sheet sheet = workbook.createSheet(
                config.getTitle() != null ? config.getTitle() : "Data"
            );
            
            // Create cell styles - handle null styles gracefully
            CellStyle headerStyle = createHeaderStyle(workbook, 
                config.getStyles() != null ? config.getStyles() : ExportConfig.ExportStyles.defaultStyles());
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            List<ExportConfig.FieldConfig> fields = config.getVisibleFields();
            
            for (int i = 0; i < fields.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(fields.get(i).getLabel());
                cell.setCellStyle(headerStyle);
            }
            
            // Create data rows
            int rowNum = 1;
            for (Object record : data) {
                Row row = sheet.createRow(rowNum++);
                
                for (int i = 0; i < fields.size(); i++) {
                    ExportConfig.FieldConfig field = fields.get(i);
                    Cell cell = row.createCell(i);
                    
                    try {
                        Object value = extractFieldValue(record, field.getKey());
                        setCellValue(cell, value, field, dateStyle, currencyStyle);
                    } catch (Exception e) {
                        log.warnf("Failed to extract/set value for field %s: %s", 
                            field.getKey(), e.getMessage());
                        cell.setCellValue(field.getDefaultValue() != null ? 
                            field.getDefaultValue() : "");
                    }
                }
            }
            
            // Auto-size columns
            for (int i = 0; i < fields.size(); i++) {
                sheet.autoSizeColumn(i);
                // Add some extra space, but respect Excel's max width
                int width = sheet.getColumnWidth(i);
                int newWidth = Math.min(width + 512, 255 * 256); // Excel max is 255 characters * 256 units per character
                sheet.setColumnWidth(i, newWidth);
            }
            
            // Add freeze pane for header
            sheet.createFreezePane(0, 1);
            
            // Add summary sheet if configured
            if (config.getFormatOptions().getOrDefault("includeSummary", false).equals(true)) {
                createSummarySheet(workbook, data, config);
            }
            
            // Write to output stream
            workbook.write(baos);
            
            String filename = generateFilename(config);
            
            return ExportResult.builder()
                .format(ExportFormat.EXCEL)
                .filename(filename)
                .recordCount(data.size())
                .withByteData(baos.toByteArray())
                .addMetadata("library", "Apache POI")
                .addMetadata("sheets", workbook.getNumberOfSheets())
                .build();
            
        } catch (Exception e) {
            log.error("Excel export failed", e);
            throw new RuntimeException("Failed to generate Excel", e);
        }
    }
    
    private CellStyle createHeaderStyle(Workbook workbook, ExportConfig.ExportStyles styles) {
        CellStyle style = workbook.createCellStyle();
        
        // Background color - FreshPlan green
        style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // Font
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        
        // Borders
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        return style;
    }
    
    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        CreationHelper helper = workbook.getCreationHelper();
        style.setDataFormat(helper.createDataFormat().getFormat("dd.mm.yyyy"));
        return style;
    }
    
    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        CreationHelper helper = workbook.getCreationHelper();
        style.setDataFormat(helper.createDataFormat().getFormat("#,##0.00 €"));
        return style;
    }
    
    private void setCellValue(Cell cell, Object value, ExportConfig.FieldConfig field,
                              CellStyle dateStyle, CellStyle currencyStyle) {
        if (value == null) {
            cell.setCellValue(field.getDefaultValue());
            return;
        }
        
        switch (field.getType()) {
            case NUMBER:
                if (value instanceof Number) {
                    cell.setCellValue(((Number) value).doubleValue());
                } else {
                    cell.setCellValue(value.toString());
                }
                break;
                
            case CURRENCY:
                if (value instanceof Number) {
                    cell.setCellValue(((Number) value).doubleValue());
                    cell.setCellStyle(currencyStyle);
                } else {
                    cell.setCellValue(value.toString());
                }
                break;
                
            case DATE:
            case DATETIME:
                if (value instanceof LocalDateTime) {
                    // Convert LocalDateTime to Date for POI
                    cell.setCellValue(value.toString());
                    cell.setCellStyle(dateStyle);
                } else if (value instanceof Date) {
                    cell.setCellValue((Date) value);
                    cell.setCellStyle(dateStyle);
                } else {
                    cell.setCellValue(value.toString());
                }
                break;
                
            case BOOLEAN:
                if (value instanceof Boolean) {
                    cell.setCellValue((Boolean) value ? "Ja" : "Nein");
                } else {
                    cell.setCellValue(value.toString());
                }
                break;
                
            default:
                cell.setCellValue(formatFieldValue(value, field));
        }
    }
    
    private void createSummarySheet(Workbook workbook, List<?> data, ExportConfig config) {
        Sheet sheet = workbook.createSheet("Zusammenfassung");
        
        int rowNum = 0;
        
        // Title
        Row titleRow = sheet.createRow(rowNum++);
        titleRow.createCell(0).setCellValue("Export Zusammenfassung");
        
        rowNum++; // Empty row
        
        // Statistics
        Row countRow = sheet.createRow(rowNum++);
        countRow.createCell(0).setCellValue("Anzahl Datensätze:");
        countRow.createCell(1).setCellValue(data.size());
        
        Row dateRow = sheet.createRow(rowNum++);
        dateRow.createCell(0).setCellValue("Erstellt am:");
        dateRow.createCell(1).setCellValue(
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))
        );
        
        if (config.getGeneratedBy() != null) {
            Row userRow = sheet.createRow(rowNum++);
            userRow.createCell(0).setCellValue("Erstellt von:");
            userRow.createCell(1).setCellValue(config.getGeneratedBy());
        }
        
        // Auto-size columns
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }
    
    private String generateFilename(ExportConfig config) {
        String base = config.getTitle() != null ? 
            config.getTitle().toLowerCase()
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "") : 
            "export";
        
        String timestamp = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        
        return base + "_" + timestamp;
    }
}