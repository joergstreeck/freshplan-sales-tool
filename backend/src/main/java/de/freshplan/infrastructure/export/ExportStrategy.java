package de.freshplan.infrastructure.export;

import java.util.List;
import java.util.Map;

/**
 * Strategy interface for different export formats.
 * Part of the Universal Export Framework.
 * 
 * Each implementation handles a specific export format (CSV, Excel, JSON, HTML).
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
public interface ExportStrategy {
    
    /**
     * Export data in the specific format.
     * 
     * @param data List of objects to export (can be entities, DTOs, Maps)
     * @param config Export configuration with field definitions and options
     * @return Export result containing the exported data
     */
    ExportResult export(List<?> data, ExportConfig config);
    
    /**
     * Get the supported export format.
     * 
     * @return The export format this strategy handles
     */
    ExportFormat getFormat();
    
    /**
     * Check if this strategy can handle the given format.
     * 
     * @param format The format to check
     * @return true if this strategy can handle the format
     */
    default boolean supports(ExportFormat format) {
        return getFormat() == format;
    }
    
    /**
     * Extract field value from an object.
     * Supports nested properties with dot notation (e.g., "customer.name").
     * 
     * @param obj The object to extract value from
     * @param fieldKey The field key (can use dot notation)
     * @return The extracted value or null
     */
    default Object extractFieldValue(Object obj, String fieldKey) {
        if (obj == null || fieldKey == null) {
            return null;
        }
        
        // Handle Map objects
        if (obj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) obj;
            if (!fieldKey.contains(".")) {
                return map.get(fieldKey);
            }
            // Handle nested maps
            String[] parts = fieldKey.split("\\.", 2);
            Object nested = map.get(parts[0]);
            return extractFieldValue(nested, parts[1]);
        }
        
        // Handle regular objects via reflection
        try {
            if (!fieldKey.contains(".")) {
                // Simple field
                var field = obj.getClass().getDeclaredField(fieldKey);
                field.setAccessible(true);
                return field.get(obj);
            }
            
            // Nested field
            String[] parts = fieldKey.split("\\.", 2);
            var field = obj.getClass().getDeclaredField(parts[0]);
            field.setAccessible(true);
            Object nested = field.get(obj);
            return extractFieldValue(nested, parts[1]);
            
        } catch (NoSuchFieldException e) {
            // Try getter method
            try {
                String getterName = "get" + 
                    fieldKey.substring(0, 1).toUpperCase() + 
                    fieldKey.substring(1);
                var method = obj.getClass().getMethod(getterName);
                return method.invoke(obj);
            } catch (Exception ex) {
                return null;
            }
        } catch (IllegalAccessException e) {
            return null;
        }
    }
    
    /**
     * Format a field value according to its type and format specification.
     * 
     * @param value The raw value
     * @param fieldConfig The field configuration
     * @return Formatted string representation
     */
    default String formatFieldValue(Object value, ExportConfig.FieldConfig fieldConfig) {
        if (value == null) {
            return fieldConfig.getDefaultValue();
        }
        
        // Format based on type
        return switch (fieldConfig.getType()) {
            case DATE -> formatDate(value, fieldConfig.getFormat());
            case DATETIME -> formatDateTime(value, fieldConfig.getFormat());
            case CURRENCY -> formatCurrency(value, fieldConfig.getFormat());
            case PERCENTAGE -> formatPercentage(value);
            case BOOLEAN -> formatBoolean(value);
            case NUMBER -> formatNumber(value, fieldConfig.getFormat());
            default -> value.toString();
        };
    }
    
    // Default formatting methods (can be overridden)
    default String formatDate(Object value, String pattern) {
        // Implementation would use DateTimeFormatter
        return value.toString();
    }
    
    default String formatDateTime(Object value, String pattern) {
        // Implementation would use DateTimeFormatter
        return value.toString();
    }
    
    default String formatCurrency(Object value, String currency) {
        // Implementation would use NumberFormat
        return value.toString();
    }
    
    default String formatPercentage(Object value) {
        // Implementation would format as percentage
        return value.toString() + "%";
    }
    
    default String formatBoolean(Object value) {
        return Boolean.TRUE.equals(value) ? "Ja" : "Nein";
    }
    
    default String formatNumber(Object value, String pattern) {
        // Implementation would use DecimalFormat
        return value.toString();
    }
}