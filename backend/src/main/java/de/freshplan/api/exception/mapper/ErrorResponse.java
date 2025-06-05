package de.freshplan.api.exception.mapper;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Standard error response structure for REST API.
 * 
 * This class provides a consistent error response format across
 * all API endpoints, making it easier for clients to handle errors.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ErrorResponse {
    
    @JsonProperty("errorId")
    private final String errorId;
    
    @JsonProperty("timestamp")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private final Instant timestamp;
    
    @JsonProperty("status")
    private final int status;
    
    @JsonProperty("error")
    private final String error;
    
    @JsonProperty("message")
    private final String message;
    
    @JsonProperty("path")
    private final String path;
    
    @JsonProperty("violations")
    private final List<FieldError> violations;
    
    /**
     * Creates a new error response.
     * 
     * @param status HTTP status code
     * @param error error type
     * @param message error message
     * @param path request path
     * @param violations field validation errors
     */
    public ErrorResponse(int status, String error, String message, String path, List<FieldError> violations) {
        this.errorId = UUID.randomUUID().toString();
        this.timestamp = Instant.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.violations = violations;
    }
    
    // Getters
    public String getErrorId() {
        return errorId;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public int getStatus() {
        return status;
    }
    
    public String getError() {
        return error;
    }
    
    public String getMessage() {
        return message;
    }
    
    public String getPath() {
        return path;
    }
    
    public List<FieldError> getViolations() {
        return violations;
    }
    
    /**
     * Represents a field validation error.
     */
    public static class FieldError {
        
        @JsonProperty("field")
        private final String field;
        
        @JsonProperty("message")
        private final String message;
        
        public FieldError(String field, String message) {
            this.field = field;
            this.message = message;
        }
        
        public String getField() {
            return field;
        }
        
        public String getMessage() {
            return message;
        }
    }
    
    /**
     * Builder for ErrorResponse.
     */
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private int status;
        private String error;
        private String message;
        private String path;
        private List<FieldError> violations;
        
        public Builder status(int status) {
            this.status = status;
            return this;
        }
        
        public Builder error(String error) {
            this.error = error;
            return this;
        }
        
        public Builder message(String message) {
            this.message = message;
            return this;
        }
        
        public Builder path(String path) {
            this.path = path;
            return this;
        }
        
        public Builder violations(List<FieldError> violations) {
            this.violations = violations;
            return this;
        }
        
        public ErrorResponse build() {
            return new ErrorResponse(status, error, message, path, violations);
        }
    }
}