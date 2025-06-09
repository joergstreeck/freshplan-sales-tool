package de.freshplan.api.exception.mapper;

import de.freshplan.domain.user.service.exception.UserNotFoundException;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for UserNotFoundExceptionMapper.
 * 
 * Tests the exception mapping to proper HTTP responses.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
@QuarkusTest
class UserNotFoundExceptionMapperTest {
    
    @Inject
    UserNotFoundExceptionMapper mapper;
    
    private UriInfo mockUriInfo;
    
    @BeforeEach
    void setUp() {
        mockUriInfo = Mockito.mock(UriInfo.class);
        mapper.uriInfo = mockUriInfo;
    }
    
    @Test
    void testToResponse_ShouldReturn404WithErrorResponse() {
        // Given
        UUID userId = UUID.randomUUID();
        String message = "User not found with ID: " + userId;
        UserNotFoundException exception = 
                new UserNotFoundException(message);
        
        when(mockUriInfo.getPath())
                .thenReturn("/api/users/" + userId);
        
        // When
        Response response = mapper.toResponse(exception);
        
        // Then
        assertThat(response.getStatus()).isEqualTo(404);
        
        ErrorResponse errorResponse = 
                (ErrorResponse) response.getEntity();
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getStatus()).isEqualTo(404);
        assertThat(errorResponse.getError()).isEqualTo("Not Found");
        assertThat(errorResponse.getMessage()).isEqualTo(message);
        assertThat(errorResponse.getPath())
                .isEqualTo("/api/users/" + userId);
        assertThat(errorResponse.getErrorId()).isNotNull();
        assertThat(errorResponse.getTimestamp()).isNotNull();
    }
}