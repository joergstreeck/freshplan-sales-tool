package de.freshplan.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import de.freshplan.domain.profile.service.ProfileService;
import de.freshplan.domain.profile.service.dto.CreateProfileRequest;
import de.freshplan.domain.profile.service.dto.ProfileResponse;
import de.freshplan.domain.profile.service.dto.UpdateProfileRequest;
import de.freshplan.domain.profile.service.exception.DuplicateProfileException;
import de.freshplan.domain.profile.service.exception.ProfileNotFoundException;
import io.quarkus.test.junit.mockito.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestSecurity(authorizationEnabled = false)
class ProfileResourceTest {

  @InjectMock ProfileService profileService;

  private ProfileResponse testProfileResponse;
  private CreateProfileRequest createRequest;
  private UUID profileId;

  @BeforeEach
  void setUp() {
    profileId = UUID.randomUUID();

    testProfileResponse =
        ProfileResponse.builder()
            .id(profileId)
            .customerId("CUST-001")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

    createRequest = CreateProfileRequest.builder().customerId("CUST-001").build();
  }

  @Test
  void createProfile_shouldReturn201() {
    // Given
    when(profileService.createProfile(any(CreateProfileRequest.class)))
        .thenReturn(testProfileResponse);

    // When/Then
    given()
        .contentType(ContentType.JSON)
        .body(createRequest)
        .when()
        .post("/api/profiles")
        .then()
        .statusCode(201)
        .header("Location", containsString("/api/profiles/" + profileId))
        .body("id", equalTo(profileId.toString()))
        .body("customerId", equalTo("CUST-001"));

    verify(profileService).createProfile(any(CreateProfileRequest.class));
  }

  @Test
  void createProfile_withDuplicateCustomerId_shouldReturn409() {
    // Given
    when(profileService.createProfile(any(CreateProfileRequest.class)))
        .thenThrow(new DuplicateProfileException("CUST-001"));

    // When/Then
    given()
        .contentType(ContentType.JSON)
        .body(createRequest)
        .when()
        .post("/api/profiles")
        .then()
        .statusCode(409)
        .body("error", equalTo("Duplicate Profile"))
        .body("message", containsString("CUST-001"));
  }

  @Test
  void getProfile_withExistingId_shouldReturn200() {
    // Given
    when(profileService.getProfile(profileId)).thenReturn(testProfileResponse);

    // When/Then
    given()
        .when()
        .get("/api/profiles/" + profileId)
        .then()
        .statusCode(200)
        .body("id", equalTo(profileId.toString()))
        .body("customerId", equalTo("CUST-001"));

    verify(profileService).getProfile(profileId);
  }

  @Test
  void getProfile_withNonExistingId_shouldReturn404() {
    // Given
    when(profileService.getProfile(profileId)).thenThrow(new ProfileNotFoundException(profileId));

    // When/Then
    given()
        .when()
        .get("/api/profiles/" + profileId)
        .then()
        .statusCode(404)
        .body("error", equalTo("Profile Not Found"))
        .body("message", containsString(profileId.toString()));
  }

  @Test
  void getProfileByCustomerId_withExistingCustomerId_shouldReturn200() {
    // Given
    when(profileService.getProfileByCustomerId("CUST-001")).thenReturn(testProfileResponse);

    // When/Then
    given()
        .when()
        .get("/api/profiles/customer/CUST-001")
        .then()
        .statusCode(200)
        .body("customerId", equalTo("CUST-001"));

    verify(profileService).getProfileByCustomerId("CUST-001");
  }

  @Test
  void updateProfile_withExistingProfile_shouldReturn200() {
    // Given
    UpdateProfileRequest updateRequest = UpdateProfileRequest.builder().build();
    when(profileService.updateProfile(eq(profileId), any(UpdateProfileRequest.class)))
        .thenReturn(testProfileResponse);

    // When/Then
    given()
        .contentType(ContentType.JSON)
        .body(updateRequest)
        .when()
        .put("/api/profiles/" + profileId)
        .then()
        .statusCode(200)
        .body("id", equalTo(profileId.toString()));

    verify(profileService).updateProfile(eq(profileId), any(UpdateProfileRequest.class));
  }

  @Test
  void deleteProfile_withExistingProfile_shouldReturn204() {
    // Given
    doNothing().when(profileService).deleteProfile(profileId);

    // When/Then
    given().when().delete("/api/profiles/" + profileId).then().statusCode(204);

    verify(profileService).deleteProfile(profileId);
  }

  @Test
  void getAllProfiles_shouldReturn200WithList() {
    // Given
    when(profileService.getAllProfiles()).thenReturn(List.of(testProfileResponse));

    // When/Then
    given()
        .when()
        .get("/api/profiles")
        .then()
        .statusCode(200)
        .body("$", hasSize(1))
        .body("[0].customerId", equalTo("CUST-001"));

    verify(profileService).getAllProfiles();
  }

  @Test
  void checkProfileExists_withExistingCustomerId_shouldReturn200() {
    // Given
    when(profileService.profileExists("CUST-001")).thenReturn(true);

    // When/Then
    given().when().head("/api/profiles/customer/CUST-001").then().statusCode(200);

    verify(profileService).profileExists("CUST-001");
  }

  @Test
  void checkProfileExists_withNonExistingCustomerId_shouldReturn404() {
    // Given
    when(profileService.profileExists("CUST-999")).thenReturn(false);

    // When/Then
    given().when().head("/api/profiles/customer/CUST-999").then().statusCode(404);

    verify(profileService).profileExists("CUST-999");
  }

  @Test
  void exportProfilePdf_withExistingProfile_shouldReturnPdf() {
    // Given
    byte[] pdfContent = "PDF-CONTENT".getBytes();
    when(profileService.exportProfileAsPdf(profileId)).thenReturn(pdfContent);

    // When/Then
    given()
        .when()
        .get("/api/profiles/" + profileId + "/export/pdf")
        .then()
        .statusCode(200)
        .contentType("application/pdf")
        .header(
            "Content-Disposition",
            containsString("attachment; filename=\"profile-" + profileId + ".pdf\""));

    verify(profileService).exportProfileAsPdf(profileId);
  }
}
