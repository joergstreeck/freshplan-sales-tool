package de.freshplan.domain.profile.service.query;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import de.freshplan.domain.profile.entity.Profile;
import de.freshplan.domain.profile.repository.ProfileRepository;
import de.freshplan.domain.profile.service.dto.ProfileResponse;
import de.freshplan.domain.profile.service.exception.ProfileNotFoundException;
import de.freshplan.domain.profile.service.mapper.ProfileMapper;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ProfileQueryService.
 * Tests all read operations for profiles with CQRS pattern.
 * 
 * IMPORTANT: Verifies that NO write operations occur in query service!
 */
@QuarkusTest
class ProfileQueryServiceTest {

  @Inject ProfileQueryService queryService;

  @InjectMock ProfileRepository profileRepository;

  @InjectMock ProfileMapper profileMapper;

  private Profile testProfile;
  private ProfileResponse testResponse;
  private UUID testId;

  @BeforeEach
  void setUp() {
    testId = UUID.randomUUID();
    
    // Setup test profile
    testProfile = new Profile();
    testProfile.setId(testId);
    testProfile.setCustomerId("CUST-001");
    testProfile.setCompanyInfo("Test Company");
    testProfile.setContactInfo("test@example.com");
    testProfile.setFinancialInfo("Revenue: 1M EUR");
    testProfile.setNotes("Test notes");
    testProfile.setCreatedBy("system");
    testProfile.setUpdatedBy("system");
    testProfile.setCreatedAt(LocalDateTime.now());
    testProfile.setUpdatedAt(LocalDateTime.now());
    
    // Setup test response using builder
    testResponse = ProfileResponse.builder()
        .id(testId)
        .customerId("CUST-001")
        .notes("Test notes")
        .build();
  }

  @Test
  void getProfile_withValidId_shouldReturnProfile() {
    // Given
    when(profileRepository.findByIdOptional(testId)).thenReturn(Optional.of(testProfile));
    when(profileMapper.toResponse(testProfile)).thenReturn(testResponse);
    
    // When
    ProfileResponse result = queryService.getProfile(testId);
    
    // Then
    assertNotNull(result);
    assertEquals(testId, result.getId());
    assertEquals("CUST-001", result.getCustomerId());
    
    // Verify NO write operations
    verifyNoWriteOperations();
  }

  @Test
  void getProfile_withNullId_shouldThrowException() {
    // When & Then
    assertThrows(
        IllegalArgumentException.class,
        () -> queryService.getProfile(null),
        "Profile ID cannot be null");
    
    verifyNoWriteOperations();
  }

  @Test
  void getProfile_withNonExistentId_shouldThrowException() {
    // Given
    when(profileRepository.findByIdOptional(testId)).thenReturn(Optional.empty());
    
    // When & Then
    assertThrows(
        ProfileNotFoundException.class,
        () -> queryService.getProfile(testId));
    
    verifyNoWriteOperations();
  }

  @Test
  void getProfileByCustomerId_withValidId_shouldReturnProfile() {
    // Given
    when(profileRepository.findByCustomerId("CUST-001")).thenReturn(Optional.of(testProfile));
    when(profileMapper.toResponse(testProfile)).thenReturn(testResponse);
    
    // When
    ProfileResponse result = queryService.getProfileByCustomerId("CUST-001");
    
    // Then
    assertNotNull(result);
    assertEquals("CUST-001", result.getCustomerId());
    
    verifyNoWriteOperations();
  }

  @Test
  void getProfileByCustomerId_withNullId_shouldThrowException() {
    // When & Then
    assertThrows(
        IllegalArgumentException.class,
        () -> queryService.getProfileByCustomerId(null),
        "Customer ID cannot be null or empty");
    
    verifyNoWriteOperations();
  }

  @Test
  void getProfileByCustomerId_withEmptyId_shouldThrowException() {
    // When & Then
    assertThrows(
        IllegalArgumentException.class,
        () -> queryService.getProfileByCustomerId("  "),
        "Customer ID cannot be null or empty");
    
    verifyNoWriteOperations();
  }

  @Test
  void getProfileByCustomerId_withNonExistentId_shouldThrowException() {
    // Given
    when(profileRepository.findByCustomerId("CUST-999")).thenReturn(Optional.empty());
    
    // When & Then
    assertThrows(
        ProfileNotFoundException.class,
        () -> queryService.getProfileByCustomerId("CUST-999"));
    
    verifyNoWriteOperations();
  }

  @Test
  void getAllProfiles_shouldReturnAllProfiles() {
    // Given
    Profile profile2 = new Profile();
    profile2.setId(UUID.randomUUID());
    profile2.setCustomerId("CUST-002");
    
    ProfileResponse response2 = ProfileResponse.builder()
        .customerId("CUST-002")
        .build();
    
    List<Profile> profiles = Arrays.asList(testProfile, profile2);
    when(profileRepository.listAll()).thenReturn(profiles);
    when(profileMapper.toResponse(testProfile)).thenReturn(testResponse);
    when(profileMapper.toResponse(profile2)).thenReturn(response2);
    
    // When
    List<ProfileResponse> result = queryService.getAllProfiles();
    
    // Then
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals("CUST-001", result.get(0).getCustomerId());
    assertEquals("CUST-002", result.get(1).getCustomerId());
    
    verifyNoWriteOperations();
  }

  @Test
  void getAllProfiles_withEmptyDatabase_shouldReturnEmptyList() {
    // Given
    when(profileRepository.listAll()).thenReturn(Arrays.asList());
    
    // When
    List<ProfileResponse> result = queryService.getAllProfiles();
    
    // Then
    assertNotNull(result);
    assertTrue(result.isEmpty());
    
    verifyNoWriteOperations();
  }

  @Test
  void profileExists_withExistingCustomer_shouldReturnTrue() {
    // Given
    when(profileRepository.existsByCustomerId("CUST-001")).thenReturn(true);
    
    // When
    boolean result = queryService.profileExists("CUST-001");
    
    // Then
    assertTrue(result);
    
    verifyNoWriteOperations();
  }

  @Test
  void profileExists_withNonExistingCustomer_shouldReturnFalse() {
    // Given
    when(profileRepository.existsByCustomerId("CUST-999")).thenReturn(false);
    
    // When
    boolean result = queryService.profileExists("CUST-999");
    
    // Then
    assertFalse(result);
    
    verifyNoWriteOperations();
  }

  @Test
  void profileExists_withNullCustomerId_shouldThrowException() {
    // When & Then
    assertThrows(
        IllegalArgumentException.class,
        () -> queryService.profileExists(null),
        "Customer ID cannot be null or empty");
    
    verifyNoWriteOperations();
  }

  @Test
  void exportProfileAsHtml_withValidId_shouldReturnHtml() {
    // Given
    when(profileRepository.findByIdOptional(testId)).thenReturn(Optional.of(testProfile));
    
    // When
    String html = queryService.exportProfileAsHtml(testId);
    
    // Then
    assertNotNull(html);
    assertTrue(html.contains("<!DOCTYPE html>"));
    assertTrue(html.contains("CUST-001"));
    assertTrue(html.contains("Test Company"));
    assertTrue(html.contains("#004F7B")); // FreshPlan CI color
    assertTrue(html.contains("#94C456")); // FreshPlan CI color
    
    verifyNoWriteOperations();
  }

  @Test
  void exportProfileAsHtml_withNullId_shouldThrowException() {
    // When & Then
    assertThrows(
        IllegalArgumentException.class,
        () -> queryService.exportProfileAsHtml(null),
        "Profile ID cannot be null");
    
    verifyNoWriteOperations();
  }

  @Test
  void exportProfileAsHtml_withNonExistentId_shouldThrowException() {
    // Given
    when(profileRepository.findByIdOptional(testId)).thenReturn(Optional.empty());
    
    // When & Then
    assertThrows(
        ProfileNotFoundException.class,
        () -> queryService.exportProfileAsHtml(testId));
    
    verifyNoWriteOperations();
  }

  @Test
  void exportProfileAsHtml_shouldEscapeHtmlSpecialCharacters() {
    // Given
    testProfile.setCompanyInfo("Test & Company <script>alert('xss')</script>");
    testProfile.setNotes("Notes with \"quotes\" and 'apostrophes'");
    when(profileRepository.findByIdOptional(testId)).thenReturn(Optional.of(testProfile));
    
    // When
    String html = queryService.exportProfileAsHtml(testId);
    
    // Then
    assertNotNull(html);
    // Verify XSS protection
    assertTrue(html.contains("Test &amp; Company &lt;script&gt;alert(&#39;xss&#39;)&lt;/script&gt;"));
    assertTrue(html.contains("Notes with &quot;quotes&quot; and &#39;apostrophes&#39;"));
    assertFalse(html.contains("<script>alert"));
    
    verifyNoWriteOperations();
  }

  /**
   * Helper method to verify that NO write operations occur in QueryService.
   * This is critical for CQRS compliance!
   */
  private void verifyNoWriteOperations() {
    verify(profileRepository, never()).persist(any(Profile.class));
    verify(profileRepository, never()).delete(any(Profile.class));
    verify(profileRepository, never()).deleteAll();
    verify(profileRepository, never()).flush();
  }
}