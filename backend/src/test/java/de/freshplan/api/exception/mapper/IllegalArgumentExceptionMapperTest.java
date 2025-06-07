package de.freshplan.api.exception.mapper;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for IllegalArgumentExceptionMapper.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
class IllegalArgumentExceptionMapperTest {
    
    private IllegalArgumentExceptionMapper mapper;
    
    @BeforeEach
    void setUp() {
        mapper = new IllegalArgumentExceptionMapper();
    }
    
    @Test
    void toResponse_withException_shouldReturnBadRequest() {
        // Given
        IllegalArgumentException exception = 
                new IllegalArgumentException("Invalid input provided");
        
        // When
        Response response = mapper.toResponse(exception);
        
        // Then
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getEntity()).isInstanceOf(ErrorResponse.class);
        
        ErrorResponse errorResponse = (ErrorResponse) response.getEntity();
        assertThat(errorResponse.getStatus()).isEqualTo(400);
        assertThat(errorResponse.getError()).isEqualTo("INVALID_ARGUMENT");
        assertThat(errorResponse.getMessage()).isEqualTo("Invalid input provided");
        assertThat(errorResponse.getErrorId()).isNotNull();
        assertThat(errorResponse.getTimestamp()).isNotNull();
    }
    
    @Test
    void toResponse_withNullMessage_shouldHandleGracefully() {
        // Given
        IllegalArgumentException exception = 
                new IllegalArgumentException((String) null);
        
        // When
        Response response = mapper.toResponse(exception);
        
        // Then
        assertThat(response.getStatus()).isEqualTo(400);
        ErrorResponse errorResponse = (ErrorResponse) response.getEntity();
        assertThat(errorResponse.getMessage()).isNull();
    }
}