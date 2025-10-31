package de.freshplan.modules.xentral.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import de.freshplan.modules.xentral.dto.ConnectionTestResponse;
import de.freshplan.modules.xentral.dto.XentralEmployeeDTO;
import de.freshplan.modules.xentral.dto.XentralSettingsDTO;
import de.freshplan.modules.xentral.entity.XentralSettings;
import de.freshplan.modules.xentral.repository.XentralSettingsRepository;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit Tests for XentralSettingsService.
 *
 * <p>Sprint 2.1.7.2 - Enterprise-Grade Test Coverage (Service Layer)
 *
 * <p><b>Test Strategy:</b> Unit tests with mocked dependencies (Repository, ApiService)
 *
 * <p><b>Bug Detection Focus:</b>
 *
 * <ul>
 *   <li>UPSERT Logic: Ensure createOrUpdate works correctly
 *   <li>Validation: Ensure invalid DTOs are rejected
 *   <li>Connection Test: Ensure API errors are handled gracefully
 *   <li>DTO Mapping: Ensure fromEntity() works correctly
 * </ul>
 *
 * @author FreshPlan Team
 * @since Sprint 2.1.7.2
 */
@QuarkusTest
class XentralSettingsServiceTest {

  @Inject XentralSettingsService service;

  @InjectMock XentralSettingsRepository repository;

  @InjectMock XentralApiService xentralApiService;

  private static final String VALID_URL = "https://test.xentral.biz";
  private static final String VALID_TOKEN = "test-token-12345";

  @BeforeEach
  void resetMocks() {
    // Reset mocks before each test
    reset(repository, xentralApiService);
  }

  // ============================================================================
  // getSettings() - Returns Optional<DTO> from DB
  // ============================================================================

  @Test
  @DisplayName("getSettings() - DB has data → Optional.of(DTO)")
  void testGetSettings_DbHasData_ReturnsOptionalWithDto() {
    // GIVEN: DB has settings
    XentralSettings entity = createMockEntity();
    when(repository.getSingleton()).thenReturn(Optional.of(entity));

    // WHEN
    Optional<XentralSettingsDTO> result = service.getSettings();

    // THEN
    assertTrue(result.isPresent(), "Should return Optional with DTO");
    assertEquals(VALID_URL, result.get().apiUrl());
    assertEquals(VALID_TOKEN, result.get().apiToken());
    assertTrue(result.get().mockMode());

    verify(repository, times(1)).getSingleton();
  }

  @Test
  @DisplayName("getSettings() - DB empty → Optional.empty()")
  void testGetSettings_DbEmpty_ReturnsEmptyOptional() {
    // GIVEN: DB is empty
    when(repository.getSingleton()).thenReturn(Optional.empty());

    // WHEN
    Optional<XentralSettingsDTO> result = service.getSettings();

    // THEN
    assertFalse(result.isPresent(), "Should return empty Optional when DB is empty");
    verify(repository, times(1)).getSingleton();
  }

  // ============================================================================
  // updateSettings() - UPSERT Logic
  // ============================================================================

  @Test
  @DisplayName("updateSettings() - Valid DTO → Success")
  void testUpdateSettings_ValidDto_ReturnsUpdatedDto() {
    // GIVEN: Valid DTO
    XentralSettingsDTO dto = new XentralSettingsDTO(VALID_URL, VALID_TOKEN, true);
    XentralSettings entity = createMockEntity();
    when(repository.createOrUpdate(anyString(), anyString(), anyBoolean())).thenReturn(entity);

    // WHEN
    XentralSettingsDTO result = service.updateSettings(dto);

    // THEN
    assertNotNull(result);
    assertEquals(VALID_URL, result.apiUrl());
    assertEquals(VALID_TOKEN, result.apiToken());
    assertTrue(result.mockMode());

    verify(repository, times(1)).createOrUpdate(VALID_URL, VALID_TOKEN, true);
  }

  @Test
  @DisplayName("updateSettings() - NULL apiUrl → IllegalArgumentException")
  void testUpdateSettings_NullApiUrl_ThrowsException() {
    // GIVEN: Invalid DTO (null apiUrl)
    XentralSettingsDTO dto = new XentralSettingsDTO(null, VALID_TOKEN, true);

    // WHEN + THEN
    assertThrows(
        IllegalArgumentException.class,
        () -> service.updateSettings(dto),
        "Should throw IllegalArgumentException for null apiUrl");

    verify(repository, never()).createOrUpdate(any(), any(), any());
  }

  @Test
  @DisplayName("updateSettings() - EMPTY apiUrl → IllegalArgumentException")
  void testUpdateSettings_EmptyApiUrl_ThrowsException() {
    // GIVEN: Invalid DTO (empty apiUrl)
    XentralSettingsDTO dto = new XentralSettingsDTO("", VALID_TOKEN, true);

    // WHEN + THEN
    assertThrows(
        IllegalArgumentException.class,
        () -> service.updateSettings(dto),
        "Should throw IllegalArgumentException for empty apiUrl");

    verify(repository, never()).createOrUpdate(any(), any(), any());
  }

  @Test
  @DisplayName("updateSettings() - Invalid URL (no protocol) → IllegalArgumentException")
  void testUpdateSettings_InvalidUrl_ThrowsException() {
    // GIVEN: Invalid DTO (URL without protocol)
    XentralSettingsDTO dto = new XentralSettingsDTO("test.xentral.biz", VALID_TOKEN, true);

    // WHEN + THEN
    assertThrows(
        IllegalArgumentException.class,
        () -> service.updateSettings(dto),
        "Should throw IllegalArgumentException for URL without protocol");

    verify(repository, never()).createOrUpdate(any(), any(), any());
  }

  @Test
  @DisplayName("updateSettings() - NULL apiToken → IllegalArgumentException")
  void testUpdateSettings_NullApiToken_ThrowsException() {
    // GIVEN: Invalid DTO (null apiToken)
    XentralSettingsDTO dto = new XentralSettingsDTO(VALID_URL, null, true);

    // WHEN + THEN
    assertThrows(
        IllegalArgumentException.class,
        () -> service.updateSettings(dto),
        "Should throw IllegalArgumentException for null apiToken");

    verify(repository, never()).createOrUpdate(any(), any(), any());
  }

  @Test
  @DisplayName("updateSettings() - EMPTY apiToken → IllegalArgumentException")
  void testUpdateSettings_EmptyApiToken_ThrowsException() {
    // GIVEN: Invalid DTO (empty apiToken)
    XentralSettingsDTO dto = new XentralSettingsDTO(VALID_URL, "", true);

    // WHEN + THEN
    assertThrows(
        IllegalArgumentException.class,
        () -> service.updateSettings(dto),
        "Should throw IllegalArgumentException for empty apiToken");

    verify(repository, never()).createOrUpdate(any(), any(), any());
  }

  @Test
  @DisplayName("updateSettings() - NULL mockMode → IllegalArgumentException")
  void testUpdateSettings_NullMockMode_ThrowsException() {
    // GIVEN: Invalid DTO (null mockMode)
    XentralSettingsDTO dto = new XentralSettingsDTO(VALID_URL, VALID_TOKEN, null);

    // WHEN + THEN
    assertThrows(
        IllegalArgumentException.class,
        () -> service.updateSettings(dto),
        "Should throw IllegalArgumentException for null mockMode");

    verify(repository, never()).createOrUpdate(any(), any(), any());
  }

  // ============================================================================
  // testConnection() - API Connection Test
  // ============================================================================

  @Test
  @DisplayName("testConnection() - API success → SUCCESS response")
  void testConnection_ApiSuccess_ReturnsSuccessResponse() {
    // GIVEN: API returns sales reps
    List<XentralEmployeeDTO> mockSalesReps =
        List.of(
            new XentralEmployeeDTO("EMP-001", "Max", "Mustermann", "max@test.de", "sales"),
            new XentralEmployeeDTO("EMP-002", "Anna", "Schmidt", "anna@test.de", "sales"));
    when(xentralApiService.getAllSalesReps()).thenReturn(mockSalesReps);

    // WHEN
    ConnectionTestResponse response = service.testConnection();

    // THEN
    assertEquals("success", response.status());
    assertTrue(
        response.message().contains("Verbindung erfolgreich"),
        "Message should contain 'Verbindung erfolgreich'");
    assertTrue(response.message().contains("2"), "Message should contain employee count");

    verify(xentralApiService, times(1)).getAllSalesReps();
  }

  @Test
  @DisplayName("testConnection() - API error → ERROR response")
  void testConnection_ApiError_ReturnsErrorResponse() {
    // GIVEN: API throws exception
    when(xentralApiService.getAllSalesReps()).thenThrow(new RuntimeException("Connection timeout"));

    // WHEN
    ConnectionTestResponse response = service.testConnection();

    // THEN
    assertEquals("error", response.status());
    assertTrue(
        response.message().contains("Verbindung fehlgeschlagen"),
        "Message should contain 'Verbindung fehlgeschlagen'");
    assertTrue(
        response.message().contains("Connection timeout"), "Message should contain error details");

    verify(xentralApiService, times(1)).getAllSalesReps();
  }

  // ============================================================================
  // Helper Methods
  // ============================================================================

  private XentralSettings createMockEntity() {
    XentralSettings entity = new XentralSettings();
    entity.setId(java.util.UUID.randomUUID());
    entity.setApiUrl(VALID_URL);
    entity.setApiToken(VALID_TOKEN);
    entity.setMockMode(true);
    return entity;
  }
}
