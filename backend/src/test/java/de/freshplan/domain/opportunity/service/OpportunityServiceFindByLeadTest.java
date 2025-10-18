package de.freshplan.domain.opportunity.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.freshplan.domain.opportunity.entity.Opportunity;
import de.freshplan.domain.opportunity.entity.OpportunityStage;
import de.freshplan.domain.opportunity.entity.OpportunityType;
import de.freshplan.domain.opportunity.repository.OpportunityRepository;
import de.freshplan.domain.opportunity.service.dto.OpportunityResponse;
import de.freshplan.domain.opportunity.service.mapper.OpportunityMapper;
import de.freshplan.modules.leads.domain.Lead;
import de.freshplan.test.builders.OpportunityTestDataFactory;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Enterprise Tests für OpportunityService.findByLeadId()
 * Sprint 2.1.7.1 - Lead → Opportunity Traceability
 *
 * @description Unit Tests für das Abrufen aller Opportunities eines Leads
 * @since 2025-10-18
 */
@QuarkusTest
@Tag("integration")
@TestSecurity(user = "testuser", roles = {"admin", "manager", "sales"})
@DisplayName("OpportunityService.findByLeadId() - Sprint 2.1.7.1 Enterprise Tests")
public class OpportunityServiceFindByLeadTest {

  @Inject OpportunityService opportunityService;

  @InjectMock OpportunityRepository opportunityRepository;

  @Inject OpportunityMapper opportunityMapper;

  private Lead testLead;
  private Opportunity opportunity1;
  private Opportunity opportunity2;
  private Opportunity opportunity3;

  @BeforeEach
  void setUp() {
    reset(opportunityRepository);

    // Create test lead (mock - not persisted)
    testLead = new Lead();
    testLead.id = 123L;
    testLead.companyName = "Test Catering GmbH";
    testLead.city = "Berlin";
    testLead.postalCode = "10115";
    testLead.countryCode = "DE";
    testLead.createdAt = LocalDateTime.now();

    // Create opportunities using TestDataFactory
    opportunity1 = OpportunityTestDataFactory.builder()
        .withName("Opportunity 1 - Neugeschäft")
        .inStage(OpportunityStage.QUALIFICATION)
        .withOpportunityType(OpportunityType.NEUGESCHAEFT)
        .withExpectedValue(new BigDecimal("25000"))
        .withExpectedCloseDate(LocalDate.now().plusDays(30))
        .withProbability(60)
        .build();
    opportunity1.setLead(testLead); // Set lead manually

    opportunity2 = OpportunityTestDataFactory.builder()
        .withName("Opportunity 2 - Sortimentserweiterung")
        .inStage(OpportunityStage.PROPOSAL)
        .withOpportunityType(OpportunityType.SORTIMENTSERWEITERUNG)
        .withExpectedValue(new BigDecimal("50000"))
        .withExpectedCloseDate(LocalDate.now().plusDays(60))
        .withProbability(80)
        .build();
    opportunity2.setLead(testLead);

    opportunity3 = OpportunityTestDataFactory.builder()
        .withName("Opportunity 3 - Neuer Standort")
        .inStage(OpportunityStage.CLOSED_WON)
        .withOpportunityType(OpportunityType.NEUER_STANDORT)
        .withExpectedValue(new BigDecimal("100000"))
        .withExpectedCloseDate(LocalDate.now().plusDays(90))
        .withProbability(100)
        .build();
    opportunity3.setLead(testLead);
  }

  @Test
  @DisplayName("Findet alle Opportunities für einen Lead")
  void testFindByLeadId_MultipleOpportunities() {
    // Given: Lead has 3 opportunities
    List<Opportunity> opportunities = Arrays.asList(opportunity1, opportunity2, opportunity3);

    // Mock repository
    @SuppressWarnings("unchecked")
    PanacheQuery<Opportunity> mockQuery = mock(PanacheQuery.class);
    when(opportunityRepository.find("lead.id", testLead.id)).thenReturn(mockQuery);
    when(mockQuery.list()).thenReturn(opportunities);

    // When
    List<OpportunityResponse> result = opportunityService.findByLeadId(testLead.id);

    // Then
    assertThat(result).isNotNull();
    assertThat(result).hasSize(3);

    // Verify first opportunity
    OpportunityResponse opp1 = result.get(0);
    assertThat(opp1.getName()).isEqualTo("Opportunity 1 - Neugeschäft");
    assertThat(opp1.getStage()).isEqualTo(OpportunityStage.QUALIFICATION);
    assertThat(opp1.getOpportunityType()).isEqualTo(OpportunityType.NEUGESCHAEFT);
    assertThat(opp1.getLeadId()).isEqualTo(123L);

    // Verify second opportunity
    OpportunityResponse opp2 = result.get(1);
    assertThat(opp2.getName()).isEqualTo("Opportunity 2 - Sortimentserweiterung");
    assertThat(opp2.getStage()).isEqualTo(OpportunityStage.PROPOSAL);
    assertThat(opp2.getOpportunityType()).isEqualTo(OpportunityType.SORTIMENTSERWEITERUNG);

    // Verify third opportunity
    OpportunityResponse opp3 = result.get(2);
    assertThat(opp3.getName()).isEqualTo("Opportunity 3 - Neuer Standort");
    assertThat(opp3.getStage()).isEqualTo(OpportunityStage.CLOSED_WON);
    assertThat(opp3.getOpportunityType()).isEqualTo(OpportunityType.NEUER_STANDORT);

    // Verify repository was called correctly
    verify(opportunityRepository).find("lead.id", testLead.id);
    verify(mockQuery).list();
  }

  @Test
  @DisplayName("Gibt leere Liste zurück wenn Lead keine Opportunities hat")
  void testFindByLeadId_EmptyList() {
    // Given: Lead has no opportunities
    @SuppressWarnings("unchecked")
    PanacheQuery<Opportunity> mockQuery = mock(PanacheQuery.class);
    when(opportunityRepository.find("lead.id", testLead.id)).thenReturn(mockQuery);
    when(mockQuery.list()).thenReturn(Collections.emptyList());

    // When
    List<OpportunityResponse> result = opportunityService.findByLeadId(testLead.id);

    // Then
    assertThat(result).isNotNull();
    assertThat(result).isEmpty();

    verify(opportunityRepository).find("lead.id", testLead.id);
  }

  @Test
  @DisplayName("Findet nur eine Opportunity für einen Lead")
  void testFindByLeadId_SingleOpportunity() {
    // Given: Lead has exactly 1 opportunity
    @SuppressWarnings("unchecked")
    PanacheQuery<Opportunity> mockQuery = mock(PanacheQuery.class);
    when(opportunityRepository.find("lead.id", testLead.id)).thenReturn(mockQuery);
    when(mockQuery.list()).thenReturn(Collections.singletonList(opportunity1));

    // When
    List<OpportunityResponse> result = opportunityService.findByLeadId(testLead.id);

    // Then
    assertThat(result).isNotNull();
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getName()).isEqualTo("Opportunity 1 - Neugeschäft");
    assertThat(result.get(0).getLeadId()).isEqualTo(123L);
  }

  @Test
  @DisplayName("Mapped alle Felder korrekt zu OpportunityResponse DTOs")
  void testFindByLeadId_DTOMapping() {
    // Given
    @SuppressWarnings("unchecked")
    PanacheQuery<Opportunity> mockQuery = mock(PanacheQuery.class);
    when(opportunityRepository.find("lead.id", testLead.id)).thenReturn(mockQuery);
    when(mockQuery.list()).thenReturn(Collections.singletonList(opportunity1));

    // When
    List<OpportunityResponse> result = opportunityService.findByLeadId(testLead.id);

    // Then
    assertThat(result).hasSize(1);
    OpportunityResponse response = result.get(0);

    // Verify all important fields are mapped (ID can be null in mocks)
    assertThat(response.getName()).isEqualTo(opportunity1.getName());
    assertThat(response.getStage()).isEqualTo(opportunity1.getStage());
    assertThat(response.getOpportunityType()).isEqualTo(opportunity1.getOpportunityType());
    assertThat(response.getExpectedValue()).isEqualByComparingTo(opportunity1.getExpectedValue());
    assertThat(response.getExpectedCloseDate()).isEqualTo(opportunity1.getExpectedCloseDate());
    assertThat(response.getLeadId()).isEqualTo(testLead.id);
    assertThat(response.getLeadCompanyName()).isEqualTo(testLead.companyName);
  }

  @Test
  @DisplayName("Funktioniert mit verschiedenen OpportunityTypes")
  void testFindByLeadId_DifferentOpportunityTypes() {
    // Given: Opportunities with all different types
    Opportunity opp1 = OpportunityTestDataFactory.builder()
        .withOpportunityType(OpportunityType.NEUGESCHAEFT)
        .build();
    opp1.setLead(testLead);

    Opportunity opp2 = OpportunityTestDataFactory.builder()
        .withOpportunityType(OpportunityType.SORTIMENTSERWEITERUNG)
        .build();
    opp2.setLead(testLead);

    Opportunity opp3 = OpportunityTestDataFactory.builder()
        .withOpportunityType(OpportunityType.NEUER_STANDORT)
        .build();
    opp3.setLead(testLead);

    Opportunity opp4 = OpportunityTestDataFactory.builder()
        .withOpportunityType(OpportunityType.VERLAENGERUNG)
        .build();
    opp4.setLead(testLead);

    List<Opportunity> opportunities = Arrays.asList(opp1, opp2, opp3, opp4);

    @SuppressWarnings("unchecked")
    PanacheQuery<Opportunity> mockQuery = mock(PanacheQuery.class);
    when(opportunityRepository.find("lead.id", testLead.id)).thenReturn(mockQuery);
    when(mockQuery.list()).thenReturn(opportunities);

    // When
    List<OpportunityResponse> result = opportunityService.findByLeadId(testLead.id);

    // Then
    assertThat(result).hasSize(4);
    assertThat(result).extracting(OpportunityResponse::getOpportunityType)
        .containsExactly(
            OpportunityType.NEUGESCHAEFT,
            OpportunityType.SORTIMENTSERWEITERUNG,
            OpportunityType.NEUER_STANDORT,
            OpportunityType.VERLAENGERUNG
        );
  }

  @Test
  @DisplayName("Funktioniert mit verschiedenen OpportunityStages")
  void testFindByLeadId_DifferentStages() {
    // Given: Opportunities in different stages
    Opportunity opp1 = OpportunityTestDataFactory.builder()
        .inStage(OpportunityStage.NEW_LEAD)
        .build();
    opp1.setLead(testLead);

    Opportunity opp2 = OpportunityTestDataFactory.builder()
        .inStage(OpportunityStage.QUALIFICATION)
        .build();
    opp2.setLead(testLead);

    Opportunity opp3 = OpportunityTestDataFactory.builder()
        .inStage(OpportunityStage.CLOSED_WON)
        .build();
    opp3.setLead(testLead);

    Opportunity opp4 = OpportunityTestDataFactory.builder()
        .inStage(OpportunityStage.CLOSED_LOST)
        .build();
    opp4.setLead(testLead);

    List<Opportunity> opportunities = Arrays.asList(opp1, opp2, opp3, opp4);

    @SuppressWarnings("unchecked")
    PanacheQuery<Opportunity> mockQuery = mock(PanacheQuery.class);
    when(opportunityRepository.find("lead.id", testLead.id)).thenReturn(mockQuery);
    when(mockQuery.list()).thenReturn(opportunities);

    // When
    List<OpportunityResponse> result = opportunityService.findByLeadId(testLead.id);

    // Then
    assertThat(result).hasSize(4);
    assertThat(result).extracting(OpportunityResponse::getStage)
        .containsExactly(
            OpportunityStage.NEW_LEAD,
            OpportunityStage.QUALIFICATION,
            OpportunityStage.CLOSED_WON,
            OpportunityStage.CLOSED_LOST
        );
  }
}
