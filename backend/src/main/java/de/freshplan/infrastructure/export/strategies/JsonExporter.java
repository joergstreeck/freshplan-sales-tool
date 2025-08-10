package de.freshplan.infrastructure.export.strategies;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.freshplan.infrastructure.export.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON export strategy using Jackson.
 * Creates professional JSON exports with metadata and formatting.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
public class JsonExporter implements ExportStrategy {
    
    private static final Logger log = Logger.getLogger(JsonExporter.class);
    
    @Inject
    ObjectMapper objectMapper;
    
    @Override
    public ExportFormat getFormat() {
        return ExportFormat.JSON;
    }
    
    @Override
    public ExportResult export(List<?> data, ExportConfig config) {
        log.infof("Exporting %d records to JSON using Jackson", data.size());
        
        try {
            // Configure ObjectMapper for pretty printing
            ObjectMapper mapper = objectMapper != null ? objectMapper : new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            
            // Create export container with metadata
            Map<String, Object> exportContainer = new HashMap<>();
            
            // Add metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("title", config.getTitle());
            metadata.put("generatedAt", LocalDateTime.now().format(
                DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            metadata.put("generatedBy", config.getGeneratedBy());
            metadata.put("recordCount", data.size());
            
            if (config.getSubtitle() != null) {
                metadata.put("subtitle", config.getSubtitle());
            }
            
            // Add field definitions if specified
            if (config.getFields() != null && !config.getFields().isEmpty()) {
                metadata.put("fields", config.getFields());
            }
            
            exportContainer.put("metadata", metadata);
            
            // Add data
            if (config.getFields() != null && !config.getFields().isEmpty()) {
                // Filter data based on visible fields
                List<Map<String, Object>> filteredData = data.stream()
                    .map(record -> filterRecord(record, config.getVisibleFields()))
                    .toList();
                exportContainer.put("data", filteredData);
            } else {
                // Export all data as-is
                exportContainer.put("data", data);
            }
            
            // Add pagination info if present
            if (config.getFormatOptions().containsKey("page")) {
                Map<String, Object> pagination = new HashMap<>();
                pagination.put("page", config.getFormatOptions().get("page"));
                pagination.put("size", config.getFormatOptions().get("size"));
                pagination.put("totalRecords", data.size());
                exportContainer.put("pagination", pagination);
            }
            
            // Convert to JSON string
            String jsonString = mapper.writeValueAsString(exportContainer);
            
            String filename = generateFilename(config);
            
            return ExportResult.builder()
                .format(ExportFormat.JSON)
                .filename(filename)
                .recordCount(data.size())
                .withStringData(jsonString)
                .addMetadata("library", "Jackson")
                .addMetadata("prettyPrint", true)
                .build();
            
        } catch (Exception e) {
            log.error("JSON export failed", e);
            throw new RuntimeException("Failed to generate JSON", e);
        }
    }
    
    /**
     * Filter record to include only visible fields
     */
    private Map<String, Object> filterRecord(Object record, List<ExportConfig.FieldConfig> fields) {
        Map<String, Object> filtered = new HashMap<>();
        
        for (ExportConfig.FieldConfig field : fields) {
            Object value = extractFieldValue(record, field.getKey());
            
            // Format value based on field type
            if (value != null) {
                switch (field.getType()) {
                    case DATE:
                    case DATETIME:
                        if (value instanceof LocalDateTime) {
                            String format = field.getFormat() != null ? 
                                field.getFormat() : "yyyy-MM-dd'T'HH:mm:ss";
                            value = ((LocalDateTime) value).format(
                                DateTimeFormatter.ofPattern(format));
                        }
                        break;
                    case CURRENCY:
                        if (value instanceof Number) {
                            value = String.format("%.2f", ((Number) value).doubleValue());
                        }
                        break;
                    case BOOLEAN:
                        // Keep as boolean for JSON
                        break;
                    default:
                        // Keep as-is or convert to string
                        break;
                }
            } else if (field.getDefaultValue() != null) {
                value = field.getDefaultValue();
            }
            
            filtered.put(field.getKey(), value);
        }
        
        return filtered;
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