package de.freshplan.domain.opportunity.service.query;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import de.freshplan.domain.opportunity.entity.Opportunity;
import de.freshplan.domain.opportunity.entity.OpportunityStage;
import de.freshplan.domain.opportunity.repository.OpportunityRepository;
import de.freshplan.domain.opportunity.service.dto.OpportunityResponse;
import de.freshplan.domain.opportunity.service.dto.PipelineOverviewResponse;
import de.freshplan.domain.opportunity.service.exception.OpportunityNotFoundException;
import de.freshplan.domain.opportunity.service.mapper.OpportunityMapper;
import de.freshplan.domain.user.entity.User;
import de.freshplan.domain.user.repository.UserRepository;
import de.freshplan.test.builders.OpportunityTestDataFactory;
import de.freshplan.test.builders.UserTestDataFactory;
import io.quarkus.panache.common.Page;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
/**
 * Unit Tests für OpportunityQueryService
 *
 * <p>Diese Tests stellen sicher, dass der OpportunityQueryService nur lesende Operationen
 * durchführt und keine Transaktionen verwendet.
 */
@QuarkusTest
@Tag("core")class OpportunityQueryServiceTest {

  @Inject OpportunityQueryService queryService;

  @InjectMock OpportunityRepository opportunityRepository;

  @InjectMock UserRepository userRepository;

  @InjectMock OpportunityMapper opportunityMapper;

  private User testUser;
  private Opportunity testOpportunity1;
  private Opportunity testOpportunity2;
  private OpportunityResponse testResponse1;
  private OpportunityResponse testResponse2;
  private UUID testUserId;

  @BeforeEach
  void setUp() {
    // Test-User Setup
    testUserId = UUID.randomUUID();
    testUser =
        UserTestDataFactory.builder()
            .withUsername("testuser")
            .withFirstName("Test")
            .withLastName("User")
            .withEmail("test@example.com")
            .build();
    // User ID cannot be set directly - it's auto-generated

    // Test-Opportunities Setup
    testOpportunity1 =
        OpportunityTestDataFactory.builder()
            .withName("Opportunity 1")
            .inStage(OpportunityStage.NEW_LEAD)
            .assignedTo(testUser)
            .withExpectedValue(new BigDecimal("10000"))
            .build();
    testOpportunity1.setId(UUID.randomUUID());

    testOpportunity2 =
        OpportunityTestDataFactory.builder()
            .withName("Opportunity 2")
            .inStage(OpportunityStage.PROPOSAL)
            .assignedTo(testUser)
            .withExpectedValue(new BigDecimal("20000"))
            .build();
    testOpportunity2.setId(UUID.randomUUID());

    // Test-Responses Setup
    testResponse1 =
        OpportunityResponse.builder()
            .id(testOpportunity1.getId())
            .name("Opportunity 1")
            .stage(OpportunityStage.NEW_LEAD)
            .expectedValue(new BigDecimal("10000"))
            .build();

    testResponse2 =
        OpportunityResponse.builder()
            .id(testOpportunity2.getId())
            .name("Opportunity 2")
            .stage(OpportunityStage.PROPOSAL)
            .expectedValue(new BigDecimal("20000"))
            .build();
  }

  // =====================================
  // BASIC QUERY TESTS
  // =====================================

  @Test
  void findAllOpportunities_withPagination_shouldReturnList() {
    // Given
    Page page = Page.of(0, 20);
    List<Opportunity> opportunities = Arrays.asList(testOpportunity1, testOpportunity2);

    when(opportunityRepository.findAllActive(page)).thenReturn(opportunities);
    when(opportunityMapper.toResponse(testOpportunity1)).thenReturn(testResponse1);
    when(opportunityMapper.toResponse(testOpportunity2)).thenReturn(testResponse2);

    // When
    List<OpportunityResponse> responses = queryService.findAllOpportunities(page);

    // Then
    assertThat(responses).hasSize(2);
    assertThat(responses).containsExactly(testResponse1, testResponse2);

    // Verify NO persist was called (read-only operation)
    verify(opportunityRepository, never()).persist(any(Opportunity.class));
  }

  @Test
  void findById_withExistingId_shouldReturnOpportunity() {
    // Given
    UUID id = testOpportunity1.getId();
    when(opportunityRepository.findByIdOptional(id)).thenReturn(Optional.of(testOpportunity1));
    when(opportunityMapper.toResponse(testOpportunity1)).thenReturn(testResponse1);

    // When
    OpportunityResponse response = queryService.findById(id);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getId()).isEqualTo(id);
    assertThat(response.getName()).isEqualTo("Opportunity 1");
  }

  @Test
  void findById_withNonExistentId_shouldThrowException() {
    // Given
    UUID nonExistentId = UUID.randomUUID();
    when(opportunityRepository.findByIdOptional(nonExistentId)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> queryService.findById(nonExistentId))
        .isInstanceOf(OpportunityNotFoundException.class);
  }

  @Test
  void findAll_withoutPagination_shouldReturnAllOpportunities() {
    // Given
    List<Opportunity> allOpportunities = Arrays.asList(testOpportunity1, testOpportunity2);
    when(opportunityRepository.listAll()).thenReturn(allOpportunities);
    when(opportunityMapper.toResponse(testOpportunity1)).thenReturn(testResponse1);
    when(opportunityMapper.toResponse(testOpportunity2)).thenReturn(testResponse2);

    // When
    List<OpportunityResponse> responses = queryService.findAll();

    // Then
    assertThat(responses).hasSize(2);
    assertThat(responses).containsExactly(testResponse1, testResponse2);
  }

  // =====================================
  // FILTERED QUERY TESTS
  // =====================================

  @Test
  void findByAssignedTo_withValidUserId_shouldReturnUserOpportunities() {
    // Given
    when(userRepository.findByIdOptional(testUserId)).thenReturn(Optional.of(testUser));
    when(opportunityRepository.findByAssignedTo(testUser))
        .thenReturn(Arrays.asList(testOpportunity1, testOpportunity2));
    when(opportunityMapper.toResponse(testOpportunity1)).thenReturn(testResponse1);
    when(opportunityMapper.toResponse(testOpportunity2)).thenReturn(testResponse2);

    // When
    List<OpportunityResponse> responses = queryService.findByAssignedTo(testUserId);

    // Then
    assertThat(responses).hasSize(2);
    assertThat(responses).containsExactly(testResponse1, testResponse2);
  }

  @Test
  void findByAssignedTo_withNonExistentUserId_shouldThrowException() {
    // Given
    UUID nonExistentUserId = UUID.randomUUID();
    when(userRepository.findByIdOptional(nonExistentUserId)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> queryService.findByAssignedTo(nonExistentUserId))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("User not found");
  }

  @Test
  void findByStage_shouldReturnOpportunitiesInStage() {
    // Given
    OpportunityStage stage = OpportunityStage.PROPOSAL;
    when(opportunityRepository.findByStage(stage)).thenReturn(Arrays.asList(testOpportunity2));
    when(opportunityMapper.toResponse(testOpportunity2)).thenReturn(testResponse2);

    // When
    List<OpportunityResponse> responses = queryService.findByStage(stage);

    // Then
    assertThat(responses).hasSize(1);
    assertThat(responses.get(0).getStage()).isEqualTo(OpportunityStage.PROPOSAL);
  }

  // =====================================
  // ANALYTICS TESTS
  // =====================================

  @Test
  void getPipelineOverview_shouldReturnAggregatedData() {
    // Given
    // Mock repository pipeline overview
    Object[] stageStats = new Object[] {OpportunityStage.NEW_LEAD, 5L, new BigDecimal("50000")};
    List<Object[]> stageStatsList = new java.util.ArrayList<>();
    stageStatsList.add(stageStats);

    when(opportunityRepository.getPipelineOverview()).thenReturn(stageStatsList);
    when(opportunityRepository.calculateForecast()).thenReturn(new BigDecimal("100000"));
    when(opportunityRepository.getConversionRate()).thenReturn(25.0);
    when(opportunityRepository.findHighPriorityOpportunities(5))
        .thenReturn(Arrays.asList(testOpportunity1));
    when(opportunityRepository.findOverdueOpportunities())
        .thenReturn(Arrays.asList(testOpportunity2));
    when(opportunityMapper.toResponse(testOpportunity1)).thenReturn(testResponse1);
    when(opportunityMapper.toResponse(testOpportunity2)).thenReturn(testResponse2);

    // When
    PipelineOverviewResponse response = queryService.getPipelineOverview();

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getTotalForecast()).isEqualTo(new BigDecimal("100000"));
    assertThat(response.getConversionRate()).isEqualTo(25.0);
    assertThat(response.getStageStatistics()).hasSize(1);

    PipelineOverviewResponse.StageStatistic stat = response.getStageStatistics().get(0);
    assertThat(stat.getStage()).isEqualTo(OpportunityStage.NEW_LEAD);
    assertThat(stat.getCount()).isEqualTo(5L);
    assertThat(stat.getTotalValue()).isEqualTo(new BigDecimal("50000"));

    assertThat(response.getHighPriorityOpportunities()).hasSize(1);
    assertThat(response.getOverdueOpportunities()).hasSize(1);
  }

  @Test
  void calculateForecast_shouldReturnForecastValue() {
    // Given
    BigDecimal expectedForecast = new BigDecimal("75000");
    when(opportunityRepository.calculateForecast()).thenReturn(expectedForecast);

    // When
    BigDecimal forecast = queryService.calculateForecast();

    // Then
    assertThat(forecast).isEqualTo(expectedForecast);
  }

  // =====================================
  // VERIFY NO WRITE OPERATIONS
  // =====================================

  @Test
  void allQueryMethods_shouldNeverCallPersist() {
    // Given
    Page page = Page.of(0, 10);
    UUID id = UUID.randomUUID();

    when(opportunityRepository.findAllActive(any())).thenReturn(Arrays.asList());
    when(opportunityRepository.findByIdOptional(any())).thenReturn(Optional.empty());
    when(opportunityRepository.listAll()).thenReturn(Arrays.asList());
    when(opportunityRepository.findByStage(any())).thenReturn(Arrays.asList());
    when(opportunityRepository.getPipelineOverview()).thenReturn(Arrays.asList());
    when(opportunityRepository.calculateForecast()).thenReturn(BigDecimal.ZERO);
    when(opportunityRepository.getConversionRate()).thenReturn(0.0);
    when(opportunityRepository.findHighPriorityOpportunities(anyInt())).thenReturn(Arrays.asList());
    when(opportunityRepository.findOverdueOpportunities()).thenReturn(Arrays.asList());

    // When - Call all query methods
    try {
      queryService.findAllOpportunities(page);
    } catch (Exception e) {
      /* ignore */
    }

    try {
      queryService.findById(id);
    } catch (Exception e) {
      /* ignore */
    }

    queryService.findAll();
    queryService.findByStage(OpportunityStage.NEW_LEAD);
    queryService.getPipelineOverview();
    queryService.calculateForecast();

    // Then - Verify no write operations
    verify(opportunityRepository, never()).persist(any(Opportunity.class));
    verify(opportunityRepository, never()).delete(any(Opportunity.class));
    verify(opportunityRepository, never()).persistAndFlush(any());
    verify(opportunityRepository, never()).deleteById(any());
  }
}
