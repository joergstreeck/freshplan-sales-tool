package de.freshplan.api.exception.mapper;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.logging.Logger;

/**
 * Master Exception Mapper für alle IllegalArgumentException Fälle
 *
 * <p>Zentrale Behandlung aller IllegalArgumentException mit spezifischer Behandlung für
 * verschiedene Fehlertypen: - Enum parsing errors - UUID parsing errors - Parameter validation
 * errors - Search parameter errors - Date format errors
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@Provider
public class IllegalArgumentMasterExceptionMapper
    implements ExceptionMapper<IllegalArgumentException> {

  private static final Logger logger =
      Logger.getLogger(IllegalArgumentMasterExceptionMapper.class.getName());

  @Override
  public Response toResponse(IllegalArgumentException exception) {
    String message = exception.getMessage();

    if (message == null) {
      return createGenericErrorResponse("Unknown parameter error");
    }

    logger.info("Handling IllegalArgumentException: " + message);

    // 1. Enum parsing errors
    if (message.contains("No enum constant")) {
      return handleEnumParsingError(message);
    }

    // 2. Empty enum errors
    if (message.contains("enum constant") && message.endsWith(".")) {
      return handleEmptyEnumError(message);
    }

    // 3. UUID parsing errors
    if (message.contains("Invalid UUID string")) {
      return handleUuidParsingError(message);
    }

    // 4. Page index validation errors
    if (message.contains("Page index must be >= 0")) {
      return handlePageIndexError(message);
    }

    // 5. Search strategy validation errors
    if (message.contains("Invalid strategy:") && message.contains("Valid strategies are:")) {
      return handleSearchStrategyError(message);
    }

    // 6. Parameter type mismatch errors
    if (message.contains("Parameter value") && message.contains("did not match expected type")) {
      return handleParameterTypeMismatchError(message);
    }

    // 7. Date format errors in search context
    if (message.contains("Unsupported date format:")) {
      return handleDateFormatError(message);
    }

    // 8. Generic IllegalArgumentException - log for analysis and return user-friendly error
    logger.warning("Unhandled IllegalArgumentException: " + message);
    logger.severe("Stack trace: " + getStackTraceAsString(exception));
    return createGenericErrorResponse(message);
  }

  private String getStackTraceAsString(Exception e) {
    java.io.StringWriter sw = new java.io.StringWriter();
    e.printStackTrace(new java.io.PrintWriter(sw));
    return sw.toString();
  }

  private Response handleEnumParsingError(String message) {
    String errorDetail = extractEnumErrorDetail(message);
    return Response.status(Response.Status.BAD_REQUEST)
        .entity(
            new ErrorResponse(
                "INVALID_ENUM_VALUE",
                "Invalid enum value provided: " + errorDetail,
                "Please check the API documentation for valid enum values"))
        .build();
  }

  private Response handleEmptyEnumError(String message) {
    return Response.status(Response.Status.BAD_REQUEST)
        .entity(
            new ErrorResponse(
                "EMPTY_ENUM_VALUE",
                "Empty or null enum value provided",
                "Enum values cannot be empty. Please provide a valid value."))
        .build();
  }

  private Response handleUuidParsingError(String message) {
    String invalidUuid = extractInvalidUuid(message);
    return Response.status(Response.Status.BAD_REQUEST)
        .entity(
            new ErrorResponse(
                "INVALID_UUID_FORMAT",
                "Invalid UUID format: " + invalidUuid,
                "UUID must be in format: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx (36 characters)"))
        .build();
  }

  private Response handlePageIndexError(String message) {
    return Response.status(Response.Status.BAD_REQUEST)
        .entity(
            new ErrorResponse(
                "INVALID_PAGE_INDEX",
                "Page index must be greater than or equal to 0",
                "Use page=0 for the first page, page=1 for the second page, etc."))
        .build();
  }

  private Response handleSearchStrategyError(String message) {
    String validStrategies = extractValidStrategies(message);
    return Response.status(Response.Status.BAD_REQUEST)
        .entity(
            new ErrorResponse(
                "INVALID_SEARCH_STRATEGY",
                "Invalid search strategy provided",
                "Valid strategies are: " + validStrategies))
        .build();
  }

  private Response handleParameterTypeMismatchError(String message) {
    return Response.status(Response.Status.BAD_REQUEST)
        .entity(
            new ErrorResponse(
                "PARAMETER_TYPE_MISMATCH",
                "Parameter type mismatch in request",
                "Check that numeric fields receive numbers and date fields receive valid dates"))
        .build();
  }

  private Response handleDateFormatError(String message) {
    String invalidDate = extractDateFromMessage(message);
    return Response.status(Response.Status.BAD_REQUEST)
        .entity(
            new ErrorResponse(
                "UNSUPPORTED_DATE_FORMAT",
                "Unsupported date format: " + invalidDate,
                "Use format YYYY-MM-DD for dates or YYYY-MM-DDTHH:mm:ss for datetimes"))
        .build();
  }

  private Response createGenericErrorResponse(String message) {
    return Response.status(Response.Status.BAD_REQUEST)
        .entity(
            new ErrorResponse(
                "INVALID_PARAMETER",
                "Invalid parameter provided",
                "Please check your request parameters and try again"))
        .build();
  }

  // Helper methods for extracting details from error messages
  private String extractEnumErrorDetail(String message) {
    try {
      if (message.contains("No enum constant ")) {
        String[] parts = message.split("No enum constant ");
        if (parts.length > 1) {
          String enumPart = parts[1];
          int lastDot = enumPart.lastIndexOf('.');
          if (lastDot > 0) {
            String enumClass = enumPart.substring(0, lastDot);
            String invalidValue = enumPart.substring(lastDot + 1);
            String simpleClassName = enumClass.substring(enumClass.lastIndexOf('.') + 1);
            return String.format("%s='%s'", simpleClassName, invalidValue);
          }
        }
      }
      return message;
    } catch (Exception e) {
      return message;
    }
  }

  private String extractInvalidUuid(String message) {
    try {
      if (message.contains("Invalid UUID string: ")) {
        String[] parts = message.split("Invalid UUID string: ");
        if (parts.length > 1) {
          return "'" + parts[1] + "'";
        }
      }
      return "provided value";
    } catch (Exception e) {
      return "provided value";
    }
  }

  private String extractValidStrategies(String message) {
    try {
      if (message.contains("Valid strategies are: ")) {
        String[] parts = message.split("Valid strategies are: ");
        if (parts.length > 1) {
          return parts[1];
        }
      }
      return "SALES_PRIORITY, RISK_MITIGATION, ENGAGEMENT_FOCUS, REVENUE_POTENTIAL, CONTACT_FREQUENCY";
    } catch (Exception e) {
      return "see API documentation";
    }
  }

  private String extractDateFromMessage(String message) {
    try {
      if (message.contains("Unsupported date format: ")) {
        String[] parts = message.split("Unsupported date format: ");
        if (parts.length > 1) {
          return "'" + parts[1] + "'";
        }
      }
      return "provided value";
    } catch (Exception e) {
      return "provided value";
    }
  }

  public static class ErrorResponse {
    public final String code;
    public final String message;
    public final String hint;

    public ErrorResponse(String code, String message, String hint) {
      this.code = code;
      this.message = message;
      this.hint = hint;
    }
  }
}
