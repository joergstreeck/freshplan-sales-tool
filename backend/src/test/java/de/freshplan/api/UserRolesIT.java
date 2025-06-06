package de.freshplan.api;

import de.freshplan.domain.user.entity.User;
import de.freshplan.domain.user.repository.UserRepository;
import de.freshplan.domain.user.service.dto.UpdateUserRolesRequest;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for user roles endpoint.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
@QuarkusTest
class UserRolesIT {
    
    @Inject
    UserRepository userRepository;
    
    private User testUser;
    
    @BeforeEach
    @Transactional
    void setUp() {
        // Clean up and create test user
        userRepository.deleteAll();
        
        testUser = new User("john.doe", "John", "Doe", "john.doe@example.com");
        userRepository.persist(testUser);
        userRepository.flush();
    }
    
    @Test
    @TestSecurity(user = "admin", roles = {"admin"})
    void updateUserRoles_withValidRoles_shouldUpdateSuccessfully() {
        UpdateUserRolesRequest request = new UpdateUserRolesRequest();
        request.setRoles(List.of("admin", "manager"));
        
        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .put("/api/users/{id}/roles", testUser.getId())
            .then()
            .statusCode(200)
            .body("id", equalTo(testUser.getId().toString()))
            .body("roles", hasSize(2))
            .body("roles", containsInAnyOrder("admin", "manager"));
    }
    
    @Test
    @TestSecurity(user = "admin", roles = {"admin"})
    void updateUserRoles_withInvalidRole_shouldReturn400() {
        UpdateUserRolesRequest request = new UpdateUserRolesRequest();
        request.setRoles(List.of("admin", "invalid_role"));
        
        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .put("/api/users/{id}/roles", testUser.getId())
            .then()
            .statusCode(400)
            .body("error", equalTo("INVALID_ROLE"))
            .body("message", containsString("Invalid role: 'invalid_role'"));
    }
    
    @Test
    @TestSecurity(user = "admin", roles = {"admin"})
    void updateUserRoles_withEmptyRoles_shouldReturn400() {
        UpdateUserRolesRequest request = new UpdateUserRolesRequest();
        request.setRoles(List.of());
        
        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .put("/api/users/{id}/roles", testUser.getId())
            .then()
            .statusCode(400);
    }
    
    @Test
    @TestSecurity(user = "admin", roles = {"admin"})
    void updateUserRoles_withNonExistentUser_shouldReturn404() {
        UpdateUserRolesRequest request = new UpdateUserRolesRequest();
        request.setRoles(List.of("admin"));
        
        UUID nonExistentId = UUID.randomUUID();
        
        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .put("/api/users/{id}/roles", nonExistentId)
            .then()
            .statusCode(404)
            .body("error", equalTo("USER_NOT_FOUND"));
    }
    
    @Test
    @TestSecurity(user = "manager", roles = {"manager"})
    void updateUserRoles_asNonAdmin_shouldReturn403() {
        UpdateUserRolesRequest request = new UpdateUserRolesRequest();
        request.setRoles(List.of("admin"));
        
        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .put("/api/users/{id}/roles", testUser.getId())
            .then()
            .statusCode(403);
    }
    
    @Test
    @TestSecurity(user = "admin", roles = {"admin"})
    void updateUserRoles_withMixedCase_shouldNormalize() {
        UpdateUserRolesRequest request = new UpdateUserRolesRequest();
        request.setRoles(List.of("ADMIN", "Manager", "SALES"));
        
        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .put("/api/users/{id}/roles", testUser.getId())
            .then()
            .statusCode(200)
            .body("roles", hasSize(3))
            .body("roles", containsInAnyOrder("admin", "manager", "sales"));
    }
    
    @Test
    void updateUserRoles_withoutAuthentication_shouldReturn401() {
        UpdateUserRolesRequest request = new UpdateUserRolesRequest();
        request.setRoles(List.of("admin"));
        
        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .put("/api/users/{id}/roles", testUser.getId())
            .then()
            .statusCode(401);
    }
}