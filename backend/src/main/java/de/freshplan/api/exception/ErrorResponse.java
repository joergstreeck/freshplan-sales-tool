package de.freshplan.api.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Standardized error response for all API errors. Follows RFC 7807 Problem Details for HTTP APIs.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ErrorResponse {
  private final String type;
  private final String title;
  private final int status;
  private final String detail;
  private final String instance;
  private final LocalDateTime timestamp;
  private final Map<String, List<String>> violations;
  private final String traceId;
  private final String error; // Legacy field for backward compatibility
  private final String message; // Legacy field for backward compatibility

  private ErrorResponse(Builder builder) {
    this.type = Objects.requireNonNull(builder.type, "type must not be null");
    this.title = Objects.requireNonNull(builder.title, "title must not be null");
    this.status = builder.status;
    this.detail = builder.detail;
    this.instance = builder.instance;
    this.timestamp = builder.timestamp != null ? builder.timestamp : LocalDateTime.now();
    this.violations = builder.violations;
    this.traceId = builder.traceId;
    this.error =
        builder.error != null
            ? builder.error
            : builder.type; // Use type as error for backward compatibility
    this.message =
        builder.message != null
            ? builder.message
            : builder.detail; // Use detail as message for backward compatibility
  }

  // Factory methods for common error types

  public static ErrorResponse notFound(String resource, String id) {
    return builder()
        .type("RESOURCE_NOT_FOUND")
        .title("Resource Not Found")
        .status(404)
        .detail(String.format("%s with ID %s not found", resource, id))
        .timestamp(LocalDateTime.now())
        .build();
  }

  public static ErrorResponse validation(Map<String, List<String>> violations) {
    return builder()
        .type("VALIDATION_ERROR")
        .title("Validation Failed")
        .status(400)
        .detail("The request contains invalid fields")
        .violations(violations)
        .timestamp(LocalDateTime.now())
        .build();
  }

  public static ErrorResponse badRequest(String detail) {
    return builder()
        .type("BAD_REQUEST")
        .title("Bad Request")
        .status(400)
        .detail(detail)
        .timestamp(LocalDateTime.now())
        .build();
  }

  public static ErrorResponse unauthorized(String detail) {
    return builder()
        .type("UNAUTHORIZED")
        .title("Unauthorized")
        .status(401)
        .detail(detail != null ? detail : "Authentication required")
        .timestamp(LocalDateTime.now())
        .build();
  }

  public static ErrorResponse forbidden(String detail) {
    return builder()
        .type("FORBIDDEN")
        .title("Forbidden")
        .status(403)
        .detail(detail != null ? detail : "Insufficient permissions")
        .timestamp(LocalDateTime.now())
        .build();
  }

  public static ErrorResponse conflict(String detail) {
    return builder()
        .type("CONFLICT")
        .title("Conflict")
        .status(409)
        .detail(detail)
        .timestamp(LocalDateTime.now())
        .build();
  }

  public static ErrorResponse internalError(String detail, String traceId) {
    return builder()
        .type("INTERNAL_ERROR")
        .title("Internal Server Error")
        .status(500)
        .detail(detail != null ? detail : "An unexpected error occurred")
        .timestamp(LocalDateTime.now())
        .traceId(traceId)
        .build();
  }

  // Getters

  public String getType() {
    return type;
  }

  public String getTitle() {
    return title;
  }

  public int getStatus() {
    return status;
  }

  public String getDetail() {
    return detail;
  }

  public String getInstance() {
    return instance;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public Map<String, List<String>> getViolations() {
    return violations;
  }

  public String getTraceId() {
    return traceId;
  }

  public String getError() {
    return error;
  }

  public String getMessage() {
    return message;
  }

  // Builder

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String type;
    private String title;
    private int status;
    private String detail;
    private String instance;
    private LocalDateTime timestamp;
    private Map<String, List<String>> violations;
    private String traceId;
    private String error;
    private String message;

    public Builder type(String type) {
      this.type = type;
      return this;
    }

    public Builder title(String title) {
      this.title = title;
      return this;
    }

    public Builder status(int status) {
      this.status = status;
      return this;
    }

    public Builder detail(String detail) {
      this.detail = detail;
      return this;
    }

    public Builder instance(String instance) {
      this.instance = instance;
      return this;
    }

    public Builder timestamp(LocalDateTime timestamp) {
      this.timestamp = timestamp;
      return this;
    }

    public Builder violations(Map<String, List<String>> violations) {
      this.violations = violations;
      return this;
    }

    public Builder traceId(String traceId) {
      this.traceId = traceId;
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

    public ErrorResponse build() {
      return new ErrorResponse(this);
    }
  }
}
