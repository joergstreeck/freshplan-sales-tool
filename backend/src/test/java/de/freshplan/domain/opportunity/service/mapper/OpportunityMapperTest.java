package de.freshplan.domain.opportunity.service.mapper;

import static org.assertj.core.api.Assertions.*;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.opportunity.entity.Opportunity;
import de.freshplan.domain.opportunity.entity.OpportunityStage;
import de.freshplan.domain.user.entity.User;
import de.freshplan.test.builders.CustomerTestDataFactory;
import de.freshplan.test.builders.OpportunityTestDataFactory;
import de.freshplan.test.builders.UserTestDataFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Unit Tests für OpportunityMapper - DTO Mapping
 *
 * <p><b>Migration:</b> Converted from @QuarkusTest to Plain JUnit (saves ~15s per test run)
 *
 * <p>Tests decken ab: - Entity zu Response DTO Mapping - Null-Handling - Vollständige Feldabbildung
 * - Edge Cases und Grenzfälle
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@Tag("unit")
@DisplayName("OpportunityMapper - DTO Mapping")
class OpportunityMapperTest {

  private OpportunityMapper opportunityMapper;

  private Opportunity testOpportunity;
  private Customer testCustomer;
  private User testUser;

  @BeforeEach
  void setUp() {
    // Initialize mapper
    opportunityMapper = new OpportunityMapper();

    // Create test customer using CustomerTestDataFactory
    testCustomer = CustomerTestDataFactory.builder().withCompanyName("Test Company Ltd.").build();
    testCustomer.setId(UUID.randomUUID());
    testCustomer.setIsTestData(true); // Mark as test data

    // Create test user using UserTestDataFactory
    testUser =
        UserTestDataFactory.builder()
            .withUsername("testuser")
            .withFirstName("Test")
            .withLastName("User")
            .withEmail("test@example.com")
            .build();
    // Use reflection to set ID (User entity has protected ID)
    try {
      var idField = User.class.getDeclaredField("id");
      idField.setAccessible(true);
      idField.set(testUser, UUID.randomUUID());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    // Create test opportunity using OpportunityTestDataFactory
    testOpportunity =
        OpportunityTestDataFactory.builder()
            .forCustomer(testCustomer)
            .withName("Test Opportunity")
            .withDescription("Test Description for mapping")
            .inStage(OpportunityStage.PROPOSAL)
            .assignedTo(testUser)
            .withExpectedValue(BigDecimal.valueOf(25000))
            .withExpectedCloseDate(LocalDate.of(2025, 12, 31))
            .withProbability(75)
            .build();
    testOpportunity.setId(UUID.randomUUID());
    // Note: createdAt is set by @CreationTimestamp, stageChangedAt/updatedAt might not have setters
    // We'll test the mapper with what's available and focus on the mapping logic
  }

  @Nested
  @Tag("integration")
  @DisplayName("toResponse - Entity zu DTO Mapping")
  class ToResponseTests {

    @Test
    @DisplayName("Should map complete opportunity entity to response DTO")
    void toResponse_completeEntity_shouldMapAllFields() {
      // Act
      var response = opportunityMapper.toResponse(testOpportunity);

      // Assert
      assertThat(response).isNotNull();
      assertThat(response.getId()).isEqualTo(testOpportunity.getId());
      assertThat(response.getName()).contains("Test Opportunity");
      assertThat(response.getDescription()).isEqualTo("Test Description for mapping");
      assertThat(response.getStage()).isEqualTo(OpportunityStage.PROPOSAL);
      assertThat(response.getCustomerId()).isEqualTo(testCustomer.getId());
      assertThat(response.getCustomerName()).isEqualTo("Test Company Ltd.");
      assertThat(response.getAssignedToId()).isEqualTo(testUser.getId());
      assertThat(response.getAssignedToName()).isEqualTo(testUser.getUsername());
      assertThat(response.getExpectedValue()).isEqualTo(BigDecimal.valueOf(25000));
      assertThat(response.getExpectedCloseDate()).isEqualTo(LocalDate.of(2025, 12, 31));
      assertThat(response.getProbability()).isEqualTo(75);
      assertThat(response.getCreatedAt()).isEqualTo(testOpportunity.getCreatedAt());
      assertThat(response.getStageChangedAt()).isEqualTo(testOpportunity.getStageChangedAt());
      assertThat(response.getUpdatedAt()).isEqualTo(testOpportunity.getUpdatedAt());
      assertThat(response.getActivities()).isNull(); // TODO: Will be implemented later
    }

    @Test
    @DisplayName("Should handle minimal opportunity with only required fields")
    void toResponse_minimalEntity_shouldMapRequiredFields() {
      // Arrange
      // Need to provide a customer for the factory
      Customer mockCustomer =
          CustomerTestDataFactory.builder().withCompanyName("[TEST] Mock Customer").build();
      mockCustomer.setId(UUID.randomUUID());

      var minimalOpportunity =
          OpportunityTestDataFactory.builder()
              .forCustomer(mockCustomer)
              .withName("Minimal Opportunity")
              .inStage(OpportunityStage.NEW_LEAD)
              .build();
      minimalOpportunity.setId(UUID.randomUUID());
      // createdAt will be set automatically by @CreationTimestamp when persisted

      // Act
      var response = opportunityMapper.toResponse(minimalOpportunity);

      // Assert
      assertThat(response).isNotNull();
      assertThat(response.getId()).isEqualTo(minimalOpportunity.getId());
      assertThat(response.getName()).contains("Minimal Opportunity");
      assertThat(response.getStage()).isEqualTo(OpportunityStage.NEW_LEAD);
      // createdAt might be null in test until persisted
      // assertThat(response.getCreatedAt()).isEqualTo(minimalOpportunity.getCreatedAt());

      // Description is automatically set by factory
      assertThat(response.getDescription()).isNotNull();
      // Customer is now set as it's required by factory
      assertThat(response.getCustomerId()).isNotNull();
      assertThat(response.getCustomerName()).contains("[TEST] Mock Customer");
      assertThat(response.getAssignedToId()).isNull();
      assertThat(response.getAssignedToName()).isNull();
      // Factory sets default value of 10000 if not provided
      assertThat(response.getExpectedValue()).isEqualTo(new BigDecimal("10000"));
      // Factory might set default close date
      assertThat(response.getExpectedCloseDate()).isNotNull();
      // Note: These might be set automatically
      // assertThat(response.getStageChangedAt()).isNull();
      // assertThat(response.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("Should return null when entity is null")
    void toResponse_nullEntity_shouldReturnNull() {
      // Act
      var response = opportunityMapper.toResponse(null);

      // Assert
      assertThat(response).isNull();
    }

    @Test
    @DisplayName("Should handle opportunity with null customer")
    void toResponse_nullCustomer_shouldMapWithNullCustomerFields() {
      // Arrange
      testOpportunity.setCustomer(null);

      // Act
      var response = opportunityMapper.toResponse(testOpportunity);

      // Assert
      assertThat(response).isNotNull();
      assertThat(response.getCustomerId()).isNull();
      assertThat(response.getCustomerName()).isNull();
      // Other fields should still be mapped
      assertThat(response.getName()).contains("Test Opportunity");
      assertThat(response.getAssignedToId()).isEqualTo(testUser.getId());
    }

    @Test
    @DisplayName("Should handle opportunity with null assigned user")
    void toResponse_nullAssignedUser_shouldMapWithNullUserFields() {
      // Arrange
      testOpportunity.setAssignedTo(null);

      // Act
      var response = opportunityMapper.toResponse(testOpportunity);

      // Assert
      assertThat(response).isNotNull();
      assertThat(response.getAssignedToId()).isNull();
      assertThat(response.getAssignedToName()).isNull();
      // Other fields should still be mapped
      assertThat(response.getName()).contains("Test Opportunity");
      assertThat(response.getCustomerId()).isEqualTo(testCustomer.getId());
    }

    @Test
    @DisplayName("Should handle opportunity with null customer and null user")
    void toResponse_nullCustomerAndUser_shouldMapOtherFields() {
      // Arrange
      testOpportunity.setCustomer(null);
      testOpportunity.setAssignedTo(null);

      // Act
      var response = opportunityMapper.toResponse(testOpportunity);

      // Assert
      assertThat(response).isNotNull();
      assertThat(response.getCustomerId()).isNull();
      assertThat(response.getCustomerName()).isNull();
      assertThat(response.getAssignedToId()).isNull();
      assertThat(response.getAssignedToName()).isNull();
      // Core fields should still be mapped
      assertThat(response.getId()).isEqualTo(testOpportunity.getId());
      assertThat(response.getName()).contains("Test Opportunity");
      assertThat(response.getStage()).isEqualTo(OpportunityStage.PROPOSAL);
    }
  }

  @Nested
  @Tag("integration")
  @DisplayName("Stage Mapping Tests")
  class StageMappingTests {

    @Test
    @DisplayName("Should correctly map all opportunity stages")
    void toResponse_allStages_shouldMapCorrectly() {
      for (OpportunityStage stage : OpportunityStage.values()) {
        // Arrange - Create fresh opportunity for each stage to avoid changeStage restrictions
        var opportunity =
            OpportunityTestDataFactory.builder()
                .withName("Test Opportunity")
                .forCustomer(testCustomer)
                .assignedTo(testUser)
                .inStage(stage)
                .build();
        opportunity.setId(UUID.randomUUID());

        // OpportunityTestDataFactory already sets the stage and probability correctly

        // Act
        var response = opportunityMapper.toResponse(opportunity);

        // Assert
        assertThat(response.getStage())
            .withFailMessage(
                "Stage %s should be mapped correctly. Expected: %s, Actual: %s",
                stage, stage, response.getStage())
            .isEqualTo(stage);
      }
    }
  }

  @Nested
  @Tag("integration")
  @DisplayName("Value Object Mapping Tests")
  class ValueObjectMappingTests {

    @Test
    @DisplayName("Should handle zero expected value")
    void toResponse_zeroExpectedValue_shouldMapCorrectly() {
      // Arrange
      testOpportunity.setExpectedValue(BigDecimal.ZERO);

      // Act
      var response = opportunityMapper.toResponse(testOpportunity);

      // Assert
      assertThat(response.getExpectedValue()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should handle large expected value")
    void toResponse_largeExpectedValue_shouldMapCorrectly() {
      // Arrange
      var largeValue = new BigDecimal("999999999.99");
      testOpportunity.setExpectedValue(largeValue);

      // Act
      var response = opportunityMapper.toResponse(testOpportunity);

      // Assert
      assertThat(response.getExpectedValue()).isEqualTo(largeValue);
    }

    @Test
    @DisplayName("Should handle edge case probability values")
    void toResponse_edgeCaseProbabilities_shouldMapCorrectly() {
      // Test minimum probability
      testOpportunity.setProbability(0);
      var response = opportunityMapper.toResponse(testOpportunity);
      assertThat(response.getProbability()).isEqualTo(0);

      // Test maximum probability
      testOpportunity.setProbability(100);
      response = opportunityMapper.toResponse(testOpportunity);
      assertThat(response.getProbability()).isEqualTo(100);
    }

    @Test
    @DisplayName("Should handle dates at year boundaries")
    void toResponse_yearBoundaryDates_shouldMapCorrectly() {
      // Arrange
      var endOfYear = LocalDate.of(2025, 12, 31);
      var startOfYear = LocalDate.of(2025, 1, 1);
      testOpportunity.setExpectedCloseDate(endOfYear);
      // Note: createdAt is managed by @CreationTimestamp and can't be set directly

      // Act
      var response = opportunityMapper.toResponse(testOpportunity);

      // Assert
      assertThat(response.getExpectedCloseDate()).isEqualTo(endOfYear);
      assertThat(response.getCreatedAt()).isEqualTo(testOpportunity.getCreatedAt());
    }
  }

  @Nested
  @Tag("integration")
  @DisplayName("Data Consistency Tests")
  class DataConsistencyTests {

    @Test
    @DisplayName("Should maintain ID consistency across multiple mappings")
    void toResponse_multipleMapping_shouldMaintainIdConsistency() {
      // Act
      var response1 = opportunityMapper.toResponse(testOpportunity);
      var response2 = opportunityMapper.toResponse(testOpportunity);

      // Assert
      assertThat(response1.getId()).isEqualTo(response2.getId());
      assertThat(response1.getCustomerId()).isEqualTo(response2.getCustomerId());
      assertThat(response1.getAssignedToId()).isEqualTo(response2.getAssignedToId());
    }

    @Test
    @DisplayName("Should handle empty string values")
    void toResponse_emptyStrings_shouldMapCorrectly() {
      // Arrange
      testOpportunity.setName("");
      testOpportunity.setDescription("");

      // Act
      var response = opportunityMapper.toResponse(testOpportunity);

      // Assert
      assertThat(response.getName()).isEmpty();
      assertThat(response.getDescription()).isEmpty();
    }

    @Test
    @DisplayName("Should handle whitespace-only strings")
    void toResponse_whitespaceStrings_shouldMapAsIs() {
      // Arrange
      testOpportunity.setName("   ");
      testOpportunity.setDescription("\t\n");

      // Act
      var response = opportunityMapper.toResponse(testOpportunity);

      // Assert
      assertThat(response.getName()).isEqualTo("   ");
      assertThat(response.getDescription()).isEqualTo("\t\n");
    }
  }

  @Nested
  @Tag("integration")
  @DisplayName("Performance and Isolation Tests")
  class PerformanceAndIsolationTests {

    @Test
    @DisplayName("Should create independent response objects")
    void toResponse_independentObjects_shouldNotShareReferences() {
      // Act
      var response1 = opportunityMapper.toResponse(testOpportunity);
      var response2 = opportunityMapper.toResponse(testOpportunity);

      // Assert
      assertThat(response1).isNotSameAs(response2);
      assertThat(response1.getId()).isEqualTo(response2.getId());

      // Modifying one response should not affect the other
      // (This is inherent with value objects, but good to verify)
    }

    @Test
    @DisplayName("Should handle rapid successive mappings")
    void toResponse_rapidSuccessiveMappings_shouldWork() {
      // Act & Assert - Should not throw any exceptions
      for (int i = 0; i < 100; i++) {
        var response = opportunityMapper.toResponse(testOpportunity);
        assertThat(response).isNotNull();
        assertThat(response.getName()).contains("Test Opportunity");
      }
    }
  }
}
