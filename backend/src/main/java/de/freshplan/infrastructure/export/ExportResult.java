package de.freshplan.infrastructure.export;

import jakarta.ws.rs.core.StreamingOutput;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Result container for export operations.
 * Can hold different types of export data (bytes, string, stream).
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
public class ExportResult {
    
    private final ExportFormat format;
    private final String filename;
    private final LocalDateTime generatedAt;
    private final long recordCount;
    private final Map<String, Object> metadata;
    
    // One of these will be set based on format
    private byte[] byteData;
    private String stringData;
    private StreamingOutput streamData;
    
    private ExportResult(Builder builder) {
        this.format = builder.format;
        this.filename = builder.filename;
        this.generatedAt = builder.generatedAt != null ? 
            builder.generatedAt : LocalDateTime.now();
        this.recordCount = builder.recordCount;
        this.metadata = builder.metadata != null ? 
            new HashMap<>(builder.metadata) : new HashMap<>();
        this.byteData = builder.byteData;
        this.stringData = builder.stringData;
        this.streamData = builder.streamData;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    // Getters
    public ExportFormat getFormat() {
        return format;
    }
    
    public String getFilename() {
        return filename;
    }
    
    public String getFullFilename() {
        return filename + format.getExtension();
    }
    
    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }
    
    public long getRecordCount() {
        return recordCount;
    }
    
    public Map<String, Object> getMetadata() {
        return new HashMap<>(metadata);
    }
    
    public byte[] getByteData() {
        return byteData;
    }
    
    public String getStringData() {
        return stringData;
    }
    
    public StreamingOutput getStreamData() {
        return streamData;
    }
    
    /**
     * Check if result has data
     */
    public boolean hasData() {
        return byteData != null || stringData != null || streamData != null;
    }
    
    /**
     * Get the appropriate content type for HTTP response
     */
    public String getContentType() {
        return format.getContentType();
    }
    
    /**
     * Get Content-Disposition header value
     */
    public String getContentDisposition() {
        return String.format("attachment; filename=\"%s\"", getFullFilename());
    }
    
    /**
     * Builder for ExportResult
     */
    public static class Builder {
        private ExportFormat format;
        private String filename;
        private LocalDateTime generatedAt;
        private long recordCount;
        private Map<String, Object> metadata;
        private byte[] byteData;
        private String stringData;
        private StreamingOutput streamData;
        
        public Builder format(ExportFormat format) {
            this.format = format;
            return this;
        }
        
        public Builder filename(String filename) {
            this.filename = filename;
            return this;
        }
        
        public Builder generatedAt(LocalDateTime generatedAt) {
            this.generatedAt = generatedAt;
            return this;
        }
        
        public Builder recordCount(long recordCount) {
            this.recordCount = recordCount;
            return this;
        }
        
        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }
        
        public Builder addMetadata(String key, Object value) {
            if (this.metadata == null) {
                this.metadata = new HashMap<>();
            }
            this.metadata.put(key, value);
            return this;
        }
        
        public Builder withByteData(byte[] data) {
            this.byteData = data;
            return this;
        }
        
        public Builder withStringData(String data) {
            this.stringData = data;
            return this;
        }
        
        public Builder withStreamData(StreamingOutput data) {
            this.streamData = data;
            return this;
        }
        
        public ExportResult build() {
            if (format == null) {
                throw new IllegalStateException("Format is required");
            }
            if (filename == null || filename.isBlank()) {
                throw new IllegalStateException("Filename is required");
            }
            if (byteData == null && stringData == null && streamData == null) {
                throw new IllegalStateException("Export data is required");
            }
            
            return new ExportResult(this);
        }
    }
}