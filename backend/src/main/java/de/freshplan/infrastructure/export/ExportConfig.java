package de.freshplan.infrastructure.export;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration for export operations.
 * Defines what fields to export, how to format them, and export options.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
public class ExportConfig {
    
    // Basic configuration
    private final String title;
    private final String subtitle;
    private final LocalDateTime generatedAt;
    private final String generatedBy;
    
    // Field definitions
    private final List<FieldConfig> fields;
    
    // Format-specific options
    private final Map<String, Object> formatOptions;
    
    // Styling (for HTML/PDF)
    private final ExportStyles styles;
    
    // Filter criteria (optional)
    private final Map<String, Object> filters;
    
    private ExportConfig(Builder builder) {
        this.title = builder.title;
        this.subtitle = builder.subtitle;
        this.generatedAt = builder.generatedAt != null ? 
            builder.generatedAt : LocalDateTime.now();
        this.generatedBy = builder.generatedBy;
        this.fields = new ArrayList<>(builder.fields);
        this.formatOptions = new HashMap<>(builder.formatOptions);
        this.styles = builder.styles != null ? 
            builder.styles : ExportStyles.defaultStyles();
        this.filters = new HashMap<>(builder.filters);
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    // Getters
    public String getTitle() {
        return title;
    }
    
    public String getSubtitle() {
        return subtitle;
    }
    
    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }
    
    public String getGeneratedBy() {
        return generatedBy;
    }
    
    public List<FieldConfig> getFields() {
        return new ArrayList<>(fields);
    }
    
    public List<FieldConfig> getVisibleFields() {
        return fields.stream()
            .filter(FieldConfig::isVisible)
            .toList();
    }
    
    public Map<String, Object> getFormatOptions() {
        return new HashMap<>(formatOptions);
    }
    
    public ExportStyles getStyles() {
        return styles;
    }
    
    public Map<String, Object> getFilters() {
        return new HashMap<>(filters);
    }
    
    /**
     * Field configuration for export
     */
    public static class FieldConfig {
        private final String key;           // Field name in the object
        private final String label;         // Column header
        private final FieldType type;       // Data type
        private final String format;        // Format pattern (e.g., "dd.MM.yyyy")
        private final Integer width;        // Column width (for Excel/HTML)
        private final boolean visible;      // Show/hide field
        private final String defaultValue;  // Value when null
        private final boolean sortable;     // Can be sorted
        private final String alignment;     // left, center, right
        
        private FieldConfig(Builder builder) {
            this.key = builder.key;
            this.label = builder.label != null ? builder.label : builder.key;
            this.type = builder.type != null ? builder.type : FieldType.STRING;
            this.format = builder.format;
            this.width = builder.width;
            this.visible = builder.visible;
            this.defaultValue = builder.defaultValue != null ? 
                builder.defaultValue : "";
            this.sortable = builder.sortable;
            this.alignment = builder.alignment != null ? 
                builder.alignment : getDefaultAlignment(this.type);
        }
        
        private static String getDefaultAlignment(FieldType type) {
            return switch (type) {
                case NUMBER, CURRENCY, PERCENTAGE -> "right";
                case DATE, DATETIME, BOOLEAN -> "center";
                default -> "left";
            };
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        // Getters
        public String getKey() { return key; }
        public String getLabel() { return label; }
        public FieldType getType() { return type; }
        public String getFormat() { return format; }
        public Integer getWidth() { return width; }
        public boolean isVisible() { return visible; }
        public String getDefaultValue() { return defaultValue; }
        public boolean isSortable() { return sortable; }
        public String getAlignment() { return alignment; }
        
        public static class Builder {
            private String key;
            private String label;
            private FieldType type;
            private String format;
            private Integer width;
            private boolean visible = true;
            private String defaultValue;
            private boolean sortable = false;
            private String alignment;
            
            public Builder key(String key) {
                this.key = key;
                return this;
            }
            
            public Builder label(String label) {
                this.label = label;
                return this;
            }
            
            public Builder type(FieldType type) {
                this.type = type;
                return this;
            }
            
            public Builder format(String format) {
                this.format = format;
                return this;
            }
            
            public Builder width(Integer width) {
                this.width = width;
                return this;
            }
            
            public Builder visible(boolean visible) {
                this.visible = visible;
                return this;
            }
            
            public Builder defaultValue(String defaultValue) {
                this.defaultValue = defaultValue;
                return this;
            }
            
            public Builder sortable(boolean sortable) {
                this.sortable = sortable;
                return this;
            }
            
            public Builder alignment(String alignment) {
                this.alignment = alignment;
                return this;
            }
            
            public FieldConfig build() {
                if (key == null || key.isBlank()) {
                    throw new IllegalStateException("Field key is required");
                }
                return new FieldConfig(this);
            }
        }
    }
    
    /**
     * Field types for export
     */
    public enum FieldType {
        STRING,
        NUMBER,
        DATE,
        DATETIME,
        BOOLEAN,
        CURRENCY,
        PERCENTAGE,
        EMAIL,
        PHONE,
        URL,
        ENUM,
        JSON
    }
    
    /**
     * Export styling configuration
     */
    public static class ExportStyles {
        private final String primaryColor;
        private final String secondaryColor;
        private final String fontFamily;
        private final String logoUrl;
        private final boolean includeHeader;
        private final boolean includeFooter;
        private final Map<String, String> customCss;
        
        private ExportStyles(Builder builder) {
            this.primaryColor = builder.primaryColor;
            this.secondaryColor = builder.secondaryColor;
            this.fontFamily = builder.fontFamily;
            this.logoUrl = builder.logoUrl;
            this.includeHeader = builder.includeHeader;
            this.includeFooter = builder.includeFooter;
            this.customCss = new HashMap<>(builder.customCss);
        }
        
        public static ExportStyles defaultStyles() {
            return builder()
                .primaryColor("#004F7B")      // FreshPlan dark blue
                .secondaryColor("#94C456")    // FreshPlan green
                .fontFamily("'Segoe UI', Tahoma, Geneva, Verdana, sans-serif")
                .includeHeader(true)
                .includeFooter(true)
                .build();
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        // Getters
        public String getPrimaryColor() { return primaryColor; }
        public String getSecondaryColor() { return secondaryColor; }
        public String getFontFamily() { return fontFamily; }
        public String getLogoUrl() { return logoUrl; }
        public boolean isIncludeHeader() { return includeHeader; }
        public boolean isIncludeFooter() { return includeFooter; }
        public Map<String, String> getCustomCss() { return new HashMap<>(customCss); }
        
        public static class Builder {
            private String primaryColor = "#004F7B";
            private String secondaryColor = "#94C456";
            private String fontFamily = "'Segoe UI', sans-serif";
            private String logoUrl;
            private boolean includeHeader = true;
            private boolean includeFooter = true;
            private Map<String, String> customCss = new HashMap<>();
            
            public Builder primaryColor(String color) {
                this.primaryColor = color;
                return this;
            }
            
            public Builder secondaryColor(String color) {
                this.secondaryColor = color;
                return this;
            }
            
            public Builder fontFamily(String fontFamily) {
                this.fontFamily = fontFamily;
                return this;
            }
            
            public Builder logoUrl(String logoUrl) {
                this.logoUrl = logoUrl;
                return this;
            }
            
            public Builder includeHeader(boolean include) {
                this.includeHeader = include;
                return this;
            }
            
            public Builder includeFooter(boolean include) {
                this.includeFooter = include;
                return this;
            }
            
            public Builder addCustomCss(String selector, String rules) {
                this.customCss.put(selector, rules);
                return this;
            }
            
            public ExportStyles build() {
                return new ExportStyles(this);
            }
        }
    }
    
    /**
     * Builder for ExportConfig
     */
    public static class Builder {
        private String title;
        private String subtitle;
        private LocalDateTime generatedAt;
        private String generatedBy;
        private List<FieldConfig> fields = new ArrayList<>();
        private Map<String, Object> formatOptions = new HashMap<>();
        private ExportStyles styles;
        private Map<String, Object> filters = new HashMap<>();
        
        public Builder title(String title) {
            this.title = title;
            return this;
        }
        
        public Builder subtitle(String subtitle) {
            this.subtitle = subtitle;
            return this;
        }
        
        public Builder generatedAt(LocalDateTime generatedAt) {
            this.generatedAt = generatedAt;
            return this;
        }
        
        public Builder generatedBy(String generatedBy) {
            this.generatedBy = generatedBy;
            return this;
        }
        
        public Builder fields(List<FieldConfig> fields) {
            this.fields = new ArrayList<>(fields);
            return this;
        }
        
        public Builder addField(FieldConfig field) {
            this.fields.add(field);
            return this;
        }
        
        public Builder formatOptions(Map<String, Object> options) {
            this.formatOptions = new HashMap<>(options);
            return this;
        }
        
        public Builder addFormatOption(String key, Object value) {
            this.formatOptions.put(key, value);
            return this;
        }
        
        public Builder styles(ExportStyles styles) {
            this.styles = styles;
            return this;
        }
        
        public Builder filters(Map<String, Object> filters) {
            this.filters = new HashMap<>(filters);
            return this;
        }
        
        public Builder addFilter(String key, Object value) {
            this.filters.put(key, value);
            return this;
        }
        
        public ExportConfig build() {
            if (fields.isEmpty()) {
                throw new IllegalStateException("At least one field is required");
            }
            return new ExportConfig(this);
        }
    }
}