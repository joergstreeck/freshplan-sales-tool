package de.freshplan.api.exception.mapper;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.time.format.DateTimeParseException;
import java.util.logging.Logger;

/**
 * Exception Mapper für Date-Format Fehler
 * 
 * Behandelt DateTimeParseException und IllegalArgumentException die durch 
 * ungültige Datumsformate entstehen, wie z.B. "Unsupported date format: invalid-date"
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
@Provider
public class DateFormatExceptionMapper implements ExceptionMapper<DateTimeParseException> {

    private static final Logger logger = Logger.getLogger(DateFormatExceptionMapper.class.getName());

    @Override
    public Response toResponse(DateTimeParseException exception) {
        String message = exception.getMessage();
        logger.warning("Date parsing error: " + message);
        
        // Extract the invalid date value if possible
        String invalidDate = extractInvalidDate(exception);
        
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse(
                        "INVALID_DATE_FORMAT",
                        "Invalid date format: " + invalidDate,
                        "Expected format: YYYY-MM-DD (ISO 8601) or YYYY-MM-DDTHH:mm:ss for datetime"
                ))
                .build();
    }
    
    private String extractInvalidDate(DateTimeParseException exception) {
        try {
            String parsedString = exception.getParsedString();
            return parsedString != null ? "'" + parsedString + "'" : "provided value";
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