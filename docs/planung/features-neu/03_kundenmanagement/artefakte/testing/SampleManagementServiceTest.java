package de.freshplan.customer;

import de.freshplan.security.ScopeContext;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * BDD Tests für SampleManagementService
 * Foundation Standards: Given-When-Then Pattern, 80%+ Coverage
 */
@QuarkusTest
class SampleManagementServiceTest {

    @Inject
    SampleManagementService sampleService;

    @InjectMock
    ScopeContext mockScope;

    @BeforeEach
    void setUp() {
        // Default: User hat Territory-Zugriff
        when(mockScope.getTerritories()).thenReturn(List.of("BER"));
    }

    @Test
    void requestSample_withValidData_shouldCreateSample() {
        // Given
        String customerId = UUID.randomUUID().toString();
        Map<String, Object> validRequest = Map.of(
            "deliveryDate", "2025-10-15",
            "notes", "Sample für Testküche"
        );

        // When & Then
        assertDoesNotThrow(() -> sampleService.requestSample(customerId, validRequest));
    }

    @Test
    void requestSample_withNoTerritories_shouldThrowForbidden() {
        // Given
        when(mockScope.getTerritories()).thenReturn(Collections.emptyList());
        String customerId = UUID.randomUUID().toString();
        Map<String, Object> request = Map.of("deliveryDate", "2025-10-15");

        // When & Then
        ForbiddenException exception = assertThrows(
            ForbiddenException.class,
            () -> sampleService.requestSample(customerId, request)
        );
        assertEquals("No authorized territories for user", exception.getMessage());
    }

    @Test
    void requestSample_withInvalidCustomerId_shouldThrowBadRequest() {
        // Given
        String invalidCustomerId = "not-a-uuid";
        Map<String, Object> request = Map.of("deliveryDate", "2025-10-15");

        // When & Then
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> sampleService.requestSample(invalidCustomerId, request)
        );
        assertEquals("Invalid customer ID format", exception.getMessage());
    }

    @Test
    void requestSample_withMissingDeliveryDate_shouldThrowBadRequest() {
        // Given
        String customerId = UUID.randomUUID().toString();
        Map<String, Object> requestWithoutDate = Map.of("notes", "Test");

        // When & Then
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> sampleService.requestSample(customerId, requestWithoutDate)
        );
        assertEquals("Delivery date required", exception.getMessage());
    }

    @Test
    void requestSample_withNullCustomerId_shouldThrowBadRequest() {
        // Given
        String nullCustomerId = null;
        Map<String, Object> request = Map.of("deliveryDate", "2025-10-15");

        // When & Then
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> sampleService.requestSample(nullCustomerId, request)
        );
        assertEquals("Customer ID required", exception.getMessage());
    }

    @Test
    void listByCustomer_withValidCustomer_shouldReturnSamples() {
        // Given
        String customerId = UUID.randomUUID().toString();

        // When
        Map<String, Object> result = sampleService.listByCustomer(customerId);

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("items"));
        assertInstanceOf(List.class, result.get("items"));
    }

    @Test
    void listByCustomer_withNoTerritories_shouldThrowForbidden() {
        // Given
        when(mockScope.getTerritories()).thenReturn(Collections.emptyList());
        String customerId = UUID.randomUUID().toString();

        // When & Then
        ForbiddenException exception = assertThrows(
            ForbiddenException.class,
            () -> sampleService.listByCustomer(customerId)
        );
        assertEquals("No authorized territories for user", exception.getMessage());
    }

    @Test
    void listByCustomer_withInvalidCustomerId_shouldThrowBadRequest() {
        // Given
        String invalidCustomerId = "invalid-uuid-format";

        // When & Then
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> sampleService.listByCustomer(invalidCustomerId)
        );
        assertEquals("Invalid customer ID format", exception.getMessage());
    }
}