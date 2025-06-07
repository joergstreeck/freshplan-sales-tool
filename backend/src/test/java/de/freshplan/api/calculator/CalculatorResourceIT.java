package de.freshplan.api.calculator;

import de.freshplan.api.calculator.dto.DiscountCalculationRequest;
import de.freshplan.api.calculator.dto.DiscountCalculationResponse;
import de.freshplan.domain.calculator.model.DiscountTier;
import de.freshplan.domain.calculator.model.LeadTimeDiscount;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for Calculator REST API.
 * 
 * Tests the complete API functionality including validation,
 * calculation logic, and response formatting.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
@QuarkusTest
class CalculatorResourceIT {
    
    private static final String CALCULATOR_ENDPOINT = "/api/v1/calculator/discount";
    
    @Test
    void calculateDiscount_withBasicOrder_shouldReturnCorrectDiscount() {
        // Given - Order of €8,000 (should get 3% volume discount)
        DiscountCalculationRequest request = new DiscountCalculationRequest(
            new BigDecimal("8000.00"), 5, false
        );
        
        // When/Then
        DiscountCalculationResponse response = given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post(CALCULATOR_ENDPOINT)
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
        .extract()
            .as(DiscountCalculationResponse.class);
        
        // Verify response
        assertThat(response.getOrderValueNet()).isEqualTo(new BigDecimal("8000.00"));
        assertThat(response.getVolumeDiscountTier()).isEqualTo(DiscountTier.TIER_1);
        assertThat(response.getVolumeDiscountRate()).isEqualTo(new BigDecimal("0.03"));
        assertThat(response.getVolumeDiscountAmount()).isEqualTo(new BigDecimal("240.00"));
        assertThat(response.getLeadTimeDiscountRate()).isEqualTo(new BigDecimal("0.00"));
        assertThat(response.getSelfPickupDiscountRate()).isEqualTo(new BigDecimal("0.00"));
        assertThat(response.getTotalDiscountRate()).isEqualTo(new BigDecimal("0.03"));
        assertThat(response.getFinalNetValue()).isEqualTo(new BigDecimal("7760.00"));
        assertThat(response.getCalculatedAt()).isNotNull();
    }
    
    @Test
    void calculateDiscount_withLargeOrderAndBenefits_shouldApplyMultipleDiscounts() {
        // Given - Order of €60,000, 20 days lead time, self pickup
        DiscountCalculationRequest request = new DiscountCalculationRequest(
            new BigDecimal("60000.00"), 20, true
        );
        
        // When/Then
        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post(CALCULATOR_ENDPOINT)
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("orderValueNet", equalTo(60000.00f))
            .body("volumeDiscountTier", equalTo("TIER_4"))
            .body("leadTimeDiscountCategory", equalTo("MEDIUM_LEAD"))
            .body("hasSelfPickupDiscount", equalTo(true))
            .body("volumeDiscountRate", equalTo(0.09f))
            .body("leadTimeDiscountRate", equalTo(0.02f))
            .body("selfPickupDiscountRate", equalTo(0.02f))
            .body("totalDiscountRate", equalTo(0.13f))
            .body("volumeDiscountAmount", equalTo(5400.00f))
            .body("leadTimeDiscountAmount", equalTo(1200.00f))
            .body("selfPickupDiscountAmount", equalTo(1200.00f))
            .body("totalDiscountAmount", equalTo(7800.00f))
            .body("finalNetValue", equalTo(52200.00f))
            .body("calculatedAt", notNullValue());
    }
    
    @Test
    void calculateDiscount_withMaximumDiscountScenario_shouldCapAt15Percent() {
        // Given - Large order with all possible discounts (would exceed 15%)
        DiscountCalculationRequest request = new DiscountCalculationRequest(
            new BigDecimal("80000.00"), 35, true
        );
        
        // When/Then
        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post(CALCULATOR_ENDPOINT)
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("orderValueNet", equalTo(80000.00f))
            .body("volumeDiscountRate", equalTo(0.10f))
            .body("leadTimeDiscountRate", equalTo(0.03f))
            .body("selfPickupDiscountRate", equalTo(0.02f))
            .body("totalDiscountRate", equalTo(0.15f)) // Capped at 15%
            .body("totalDiscountAmount", equalTo(12000.00f)) // 80000 * 0.15
            .body("finalNetValue", equalTo(68000.00f));
    }
    
    @Test
    void calculateDiscount_withSmallOrder_shouldReturnNoDiscount() {
        // Given - Order of €3,000 (below discount threshold)
        DiscountCalculationRequest request = new DiscountCalculationRequest(
            new BigDecimal("3000.00"), 5, false
        );
        
        // When/Then
        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post(CALCULATOR_ENDPOINT)
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("volumeDiscountTier", equalTo("TIER_0"))
            .body("volumeDiscountRate", equalTo(0.00f))
            .body("totalDiscountRate", equalTo(0.00f))
            .body("finalNetValue", equalTo(3000.00f));
    }
    
    @Test
    void calculateDiscount_withNullOrderValue_shouldReturnBadRequest() {
        // Given - Request with null order value
        String invalidRequest = """
            {
                "orderValueNet": null,
                "leadTimeDays": 5,
                "selfPickup": false
            }
            """;
        
        // When/Then
        given()
            .contentType(ContentType.JSON)
            .body(invalidRequest)
        .when()
            .post(CALCULATOR_ENDPOINT)
        .then()
            .statusCode(400);
    }
    
    @Test
    void calculateDiscount_withNegativeOrderValue_shouldReturnBadRequest() {
        // Given - Request with negative order value
        DiscountCalculationRequest request = new DiscountCalculationRequest(
            new BigDecimal("-1000.00"), 5, false
        );
        
        // When/Then
        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post(CALCULATOR_ENDPOINT)
        .then()
            .statusCode(400);
    }
    
    @Test
    void calculateDiscount_withOrderBelowMinimum_shouldReturnBadRequest() {
        // Given - Order below €100 minimum
        DiscountCalculationRequest request = new DiscountCalculationRequest(
            new BigDecimal("50.00"), 5, false
        );
        
        // When/Then
        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post(CALCULATOR_ENDPOINT)
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("code", equalTo("INVALID_REQUEST"))
            .body("message", containsString("Minimum order value"));
    }
    
    @Test
    void calculateDiscount_withOrderAboveMaximum_shouldReturnBadRequest() {
        // Given - Order above €100,000 maximum
        DiscountCalculationRequest request = new DiscountCalculationRequest(
            new BigDecimal("150000.00"), 5, false
        );
        
        // When/Then
        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post(CALCULATOR_ENDPOINT)
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("code", equalTo("INVALID_REQUEST"))
            .body("message", containsString("exceeds maximum"));
    }
    
    @Test
    void calculateDiscount_withNegativeLeadTime_shouldReturnBadRequest() {
        // Given - Request with negative lead time
        DiscountCalculationRequest request = new DiscountCalculationRequest(
            new BigDecimal("5000.00"), -1, false
        );
        
        // When/Then
        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post(CALCULATOR_ENDPOINT)
        .then()
            .statusCode(400);
    }
    
    @Test
    void calculateDiscount_withNullLeadTime_shouldReturnBadRequest() {
        // Given - Request with null lead time
        String invalidRequest = """
            {
                "orderValueNet": 5000.00,
                "leadTimeDays": null,
                "selfPickup": false
            }
            """;
        
        // When/Then
        given()
            .contentType(ContentType.JSON)
            .body(invalidRequest)
        .when()
            .post(CALCULATOR_ENDPOINT)
        .then()
            .statusCode(400);
    }
    
    @Test
    void calculateDiscount_withNullSelfPickup_shouldReturnBadRequest() {
        // Given - Request with null self pickup flag
        String invalidRequest = """
            {
                "orderValueNet": 5000.00,
                "leadTimeDays": 5,
                "selfPickup": null
            }
            """;
        
        // When/Then
        given()
            .contentType(ContentType.JSON)
            .body(invalidRequest)
        .when()
            .post(CALCULATOR_ENDPOINT)
        .then()
            .statusCode(400);
    }
    
    @Test
    void calculateDiscount_withMalformedJson_shouldReturnBadRequest() {
        // Given - Malformed JSON
        String malformedJson = "{ invalid json }";
        
        // When/Then
        given()
            .contentType(ContentType.JSON)
            .body(malformedJson)
        .when()
            .post(CALCULATOR_ENDPOINT)
        .then()
            .statusCode(400);
    }
}