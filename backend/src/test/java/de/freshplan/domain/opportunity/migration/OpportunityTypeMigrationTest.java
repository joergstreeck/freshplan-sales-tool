package de.freshplan.domain.opportunity.migration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import de.freshplan.domain.opportunity.entity.Opportunity;
import de.freshplan.domain.opportunity.entity.OpportunityStage;
import de.freshplan.domain.opportunity.entity.OpportunityType;
import de.freshplan.test.builders.OpportunityTestDataFactory;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Integration Tests für Migration V10030 - OpportunityType
 *
 * <p>Sprint 2.1.7.1: OpportunityType Backend Quick Win
 *
 * <p>Testet: - DB DEFAULT Value - CHECK Constraint - Column NOT NULL - JPA @PrePersist Default
 */
@QuarkusTest
@Tag("integration")
@DisplayName("Migration V10030 - OpportunityType Integration Tests")
class OpportunityTypeMigrationTest {

  @Inject EntityManager entityManager;

  @Inject DataSource dataSource;

  @Test
  @Transactional
  @DisplayName("Database should have opportunity_type column with DEFAULT")
  void opportunityTypeColumn_shouldExistWithDefault() throws SQLException {
    // Verify column exists and has DEFAULT constraint
    var connection = dataSource.getConnection();
    var stmt =
        connection.prepareStatement(
            "SELECT column_default FROM information_schema.columns "
                + "WHERE table_name = 'opportunities' AND column_name = 'opportunity_type'");

    var rs = stmt.executeQuery();

    assertThat(rs.next()).isTrue();
    String defaultValue = rs.getString("column_default");
    assertThat(defaultValue).contains("NEUGESCHAEFT"); // DB DEFAULT set

    rs.close();
    stmt.close();
    connection.close();
  }

  @Test
  @Transactional
  @DisplayName("Database should have CHECK constraint for opportunity_type")
  void opportunityTypeColumn_shouldHaveCheckConstraint() throws SQLException {
    // Verify CHECK constraint exists
    var connection = dataSource.getConnection();
    var stmt =
        connection.prepareStatement(
            "SELECT constraint_name FROM information_schema.table_constraints "
                + "WHERE table_name = 'opportunities' AND constraint_type = 'CHECK' "
                + "AND constraint_name = 'chk_opportunity_type'");

    var rs = stmt.executeQuery();

    assertThat(rs.next())
        .as("CHECK constraint 'chk_opportunity_type' should exist")
        .isTrue();

    rs.close();
    stmt.close();
    connection.close();
  }

  @Test
  @Transactional
  @DisplayName("Database should reject invalid opportunity_type values")
  void opportunityTypeColumn_shouldRejectInvalidValues() {
    // Try to persist Opportunity with invalid opportunityType via native SQL
    assertThatThrownBy(
            () -> {
              entityManager
                  .createNativeQuery(
                      "INSERT INTO opportunities (id, name, stage, opportunity_type, assigned_to, probability, created_at, stage_changed_at, updated_at) "
                          + "VALUES (gen_random_uuid(), 'Test', 'NEW_LEAD', 'INVALID_TYPE', null, 10, now(), now(), now())")
                  .executeUpdate();
              entityManager.flush();
            })
        .as("Invalid opportunity_type should be rejected by CHECK constraint")
        .hasMessageContaining("chk_opportunity_type");
  }

  @Test
  @Transactional
  @DisplayName("JPA @PrePersist should set default OpportunityType if null")
  void jpaPrePersist_shouldSetDefaultOpportunityType() {
    // Create Opportunity without setting opportunityType
    Opportunity opportunity =
        OpportunityTestDataFactory.builder()
            .withName("Test Opportunity without Type")
            .inStage(OpportunityStage.NEW_LEAD)
            .build();

    // Manually set opportunityType to null to test @PrePersist hook
    opportunity.setOpportunityType(null);

    // Persist assignedTo user first (to avoid TransientObjectException)
    if (opportunity.getAssignedTo() != null) {
      entityManager.persist(opportunity.getAssignedTo());
    }

    entityManager.persist(opportunity);
    entityManager.flush();

    // Reload from DB
    entityManager.clear();
    Opportunity reloaded = entityManager.find(Opportunity.class, opportunity.getId());

    assertThat(reloaded.getOpportunityType())
        .as("@PrePersist should have set default NEUGESCHAEFT")
        .isEqualTo(OpportunityType.NEUGESCHAEFT);
  }

  @Test
  @Transactional
  @DisplayName("OpportunityTestDataFactory should create with default OpportunityType")
  void testDataFactory_shouldCreateWithDefaultType() {
    // TestDataFactory creates with NEUGESCHAEFT default
    Opportunity opportunity = OpportunityTestDataFactory.builder().build();

    // Persist assignedTo user first (to avoid TransientObjectException)
    if (opportunity.getAssignedTo() != null) {
      entityManager.persist(opportunity.getAssignedTo());
    }

    entityManager.persist(opportunity);
    entityManager.flush();

    assertThat(opportunity.getOpportunityType()).isEqualTo(OpportunityType.NEUGESCHAEFT);
  }

  @Test
  @Transactional
  @DisplayName("Should persist all 4 OpportunityType values")
  void shouldPersistAllOpportunityTypes() {
    // Test all 4 Freshfoodz types can be persisted
    for (OpportunityType type : OpportunityType.values()) {
      Opportunity opp =
          OpportunityTestDataFactory.builder()
              .withName("Test " + type.name())
              .withOpportunityType(type)
              .build();

      // Persist assignedTo user first (to avoid TransientObjectException)
      if (opp.getAssignedTo() != null) {
        entityManager.persist(opp.getAssignedTo());
      }

      entityManager.persist(opp);
      entityManager.flush();

      assertThat(opp.getId()).isNotNull();
      assertThat(opp.getOpportunityType()).isEqualTo(type);
    }
  }

  @Test
  @Transactional
  @DisplayName("Database should have index on opportunity_type")
  void opportunityTypeColumn_shouldHaveIndex() throws SQLException {
    // Verify B-Tree index exists (Performance requirement from V10030)
    var connection = dataSource.getConnection();
    var stmt =
        connection.prepareStatement(
            "SELECT indexname FROM pg_indexes "
                + "WHERE tablename = 'opportunities' AND indexname = 'idx_opportunities_opportunity_type'");

    var rs = stmt.executeQuery();

    assertThat(rs.next()).as("Index idx_opportunities_opportunity_type should exist").isTrue();

    rs.close();
    stmt.close();
    connection.close();
  }

  @Test
  @Transactional
  @DisplayName("OpportunityType column should be NOT NULL")
  void opportunityTypeColumn_shouldBeNotNull() throws SQLException {
    // Verify NOT NULL constraint
    var connection = dataSource.getConnection();
    var stmt =
        connection.prepareStatement(
            "SELECT is_nullable FROM information_schema.columns "
                + "WHERE table_name = 'opportunities' AND column_name = 'opportunity_type'");

    var rs = stmt.executeQuery();

    assertThat(rs.next()).isTrue();
    String nullable = rs.getString("is_nullable");
    assertThat(nullable).isEqualTo("NO"); // NOT NULL constraint

    rs.close();
    stmt.close();
    connection.close();
  }
}
