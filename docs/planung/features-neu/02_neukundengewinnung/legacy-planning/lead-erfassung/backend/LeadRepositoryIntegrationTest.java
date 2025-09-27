package de.freshplan.leads.repo;

import de.freshplan.leads.domain.LeadEntity;
import de.freshplan.leads.domain.LeadStatus;
import de.freshplan.security.ScopeContext;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Integration Tests for LeadRepository with ABAC Security
 *
 * Tests territory-scoped data access, CRUD operations, and performance
 * characteristics of the Lead Repository implementation.
 *
 * @see ../../../grundlagen/TESTING_STANDARDS.md - BDD Testing Patterns
 * @see ../../../grundlagen/SECURITY_GUIDELINES.md - ABAC Testing
 *
 * @author Lead Management Team
 * @version 2.0
 * @since 2025-09-19
 */
@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LeadRepositoryIntegrationTest {

    @Inject
    LeadRepositoryImpl repository;

    @Inject
    EntityManager em;

    @Inject
    ScopeContext scopeContext;

    private static final String TERRITORY_NORTH = "DE_NORTH";
    private static final String TERRITORY_SOUTH = "DE_SOUTH";
    private static final String TERRITORY_EAST = "DE_EAST";

    private List<UUID> testLeadIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        // Clear any existing test data
        cleanupTestData();

        // Set default security context
        scopeContext.setTerritories(Arrays.asList(TERRITORY_NORTH, TERRITORY_SOUTH));
        scopeContext.setChainId("EDEKA");
        scopeContext.setUserId("test-user");
        scopeContext.setTenant("freshfoodz");
    }

    @AfterEach
    void tearDown() {
        cleanupTestData();
        testLeadIds.clear();
    }

    @Transactional
    void cleanupTestData() {
        em.createQuery("DELETE FROM LeadEntity l WHERE l.email LIKE :pattern")
          .setParameter("pattern", "test-%@example.com")
          .executeUpdate();
    }

    // ========== CRUD Operations Tests ==========

    @Test
    @DisplayName("Given valid lead data, when persisting, then lead should be saved with territory")
    @Transactional
    void testPersistLead_withValidData_shouldSaveWithTerritory() {
        // Given
        LeadEntity lead = createTestLead("Test Company", "test-persist@example.com");

        // When
        repository.persist(lead);
        em.flush();

        // Then
        assertThat(lead.getId()).isNotNull();
        assertThat(lead.getTerritory()).isEqualTo(TERRITORY_NORTH);
        assertThat(lead.getCreatedAt()).isNotNull();
        assertThat(lead.getUpdatedAt()).isNotNull();

        testLeadIds.add(lead.getId());
    }

    @Test
    @DisplayName("Given existing lead, when finding by ID, then should return lead if territory matches")
    @Transactional
    void testFindById_withValidTerritory_shouldReturnLead() {
        // Given
        LeadEntity lead = createAndPersistLead("Find Test", "test-find@example.com", TERRITORY_NORTH);

        // When
        Optional<LeadEntity> found = repository.findById(lead.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getCompanyName()).isEqualTo("Find Test");
        assertThat(found.get().getTerritory()).isEqualTo(TERRITORY_NORTH);
    }

    @Test
    @DisplayName("Given lead in different territory, when finding by ID, then should return empty")
    @Transactional
    void testFindById_withDifferentTerritory_shouldReturnEmpty() {
        // Given
        LeadEntity lead = createAndPersistLead("Other Territory", "test-other@example.com", TERRITORY_EAST);

        // When
        Optional<LeadEntity> found = repository.findById(lead.getId());

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Given existing lead, when updating, then should save changes with updated timestamp")
    @Transactional
    void testUpdateLead_withValidAccess_shouldUpdateSuccessfully() {
        // Given
        LeadEntity lead = createAndPersistLead("Update Test", "test-update@example.com", TERRITORY_NORTH);
        lead.setCompanyName("Updated Company");
        lead.setStatus(LeadStatus.QUALIFIED);

        // When
        LeadEntity updated = repository.update(lead);
        em.flush();

        // Then
        assertThat(updated.getCompanyName()).isEqualTo("Updated Company");
        assertThat(updated.getStatus()).isEqualTo(LeadStatus.QUALIFIED);
        assertThat(updated.getUpdatedAt()).isAfter(updated.getCreatedAt());
    }

    @Test
    @DisplayName("Given lead without access, when updating, then should throw SecurityException")
    @Transactional
    void testUpdateLead_withoutAccess_shouldThrowException() {
        // Given
        LeadEntity lead = createAndPersistLead("No Access", "test-noaccess@example.com", TERRITORY_EAST);

        // When/Then
        assertThatThrownBy(() -> repository.update(lead))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("access denied");
    }

    @Test
    @DisplayName("Given existing lead, when deleting with access, then should remove from database")
    @Transactional
    void testDeleteLead_withValidAccess_shouldDelete() {
        // Given
        LeadEntity lead = createAndPersistLead("Delete Test", "test-delete@example.com", TERRITORY_NORTH);
        UUID leadId = lead.getId();

        // When
        repository.delete(leadId);
        em.flush();

        // Then
        LeadEntity deleted = em.find(LeadEntity.class, leadId);
        assertThat(deleted).isNull();
    }

    // ========== Search and Pagination Tests ==========

    @Test
    @DisplayName("Given multiple leads, when searching with pagination, then should return correct page")
    @Transactional
    void testFindPage_withPagination_shouldReturnCorrectResults() {
        // Given
        createAndPersistLead("Company A", "test-a@example.com", TERRITORY_NORTH);
        createAndPersistLead("Company B", "test-b@example.com", TERRITORY_NORTH);
        LeadEntity leadC = createAndPersistLead("Company C", "test-c@example.com", TERRITORY_NORTH);
        createAndPersistLead("Company D", "test-d@example.com", TERRITORY_SOUTH);

        // When - First page
        List<LeadEntity> page1 = repository.findPage(null, null, null, null, null, 2);

        // Then
        assertThat(page1).hasSize(2);

        // When - Second page with cursor
        UUID cursor = page1.get(1).getId();
        List<LeadEntity> page2 = repository.findPage(null, null, null, null, cursor, 2);

        // Then
        assertThat(page2).hasSizeGreaterThanOrEqualTo(1);
        assertThat(page2.get(0).getId()).isGreaterThan(cursor);
    }

    @Test
    @DisplayName("Given leads with different statuses, when filtering by status, then should return matching leads")
    @Transactional
    void testFindPage_withStatusFilter_shouldFilterCorrectly() {
        // Given
        createAndPersistLead("New Lead", "test-new@example.com", TERRITORY_NORTH, LeadStatus.NEW);
        createAndPersistLead("Qualified Lead", "test-qual@example.com", TERRITORY_NORTH, LeadStatus.QUALIFIED);
        createAndPersistLead("Another New", "test-new2@example.com", TERRITORY_NORTH, LeadStatus.NEW);

        // When
        List<LeadEntity> newLeads = repository.findPage(null, LeadStatus.NEW, null, null, null, 10);
        List<LeadEntity> qualifiedLeads = repository.findPage(null, LeadStatus.QUALIFIED, null, null, null, 10);

        // Then
        assertThat(newLeads).hasSizeGreaterThanOrEqualTo(2);
        assertThat(newLeads).allMatch(l -> l.getStatus() == LeadStatus.NEW);
        assertThat(qualifiedLeads).hasSizeGreaterThanOrEqualTo(1);
        assertThat(qualifiedLeads).allMatch(l -> l.getStatus() == LeadStatus.QUALIFIED);
    }

    @Test
    @DisplayName("Given leads with search term, when searching, then should return matching results")
    @Transactional
    void testFindPage_withSearchQuery_shouldReturnMatches() {
        // Given
        createAndPersistLead("FreshFood GmbH", "fresh@example.com", TERRITORY_NORTH);
        createAndPersistLead("Restaurant Milano", "milano@example.com", TERRITORY_NORTH);
        createAndPersistLead("Fresh Delights", "delights@example.com", TERRITORY_NORTH);

        // When
        List<LeadEntity> results = repository.findPage("fresh", null, null, null, null, 10);

        // Then
        assertThat(results).hasSizeGreaterThanOrEqualTo(2);
        assertThat(results).anyMatch(l -> l.getCompanyName().contains("FreshFood"));
        assertThat(results).anyMatch(l -> l.getCompanyName().contains("Fresh Delights"));
    }

    @Test
    @DisplayName("Given leads in multiple territories, when filtering by territory, then should return only matching")
    @Transactional
    void testFindPage_withTerritoryFilter_shouldFilterCorrectly() {
        // Given
        createAndPersistLead("North Company", "test-n@example.com", TERRITORY_NORTH);
        createAndPersistLead("South Company", "test-s@example.com", TERRITORY_SOUTH);
        createAndPersistLead("Another North", "test-n2@example.com", TERRITORY_NORTH);

        // When
        List<LeadEntity> northLeads = repository.findPage(null, null, null, TERRITORY_NORTH, null, 10);

        // Then
        assertThat(northLeads).hasSizeGreaterThanOrEqualTo(2);
        assertThat(northLeads).allMatch(l -> TERRITORY_NORTH.equals(l.getTerritory()));
    }

    // ========== Aggregate and Statistics Tests ==========

    @Test
    @DisplayName("Given leads with different statuses, when counting by status, then should return correct count")
    @Transactional
    void testCountByStatus_shouldReturnCorrectCount() {
        // Given
        createAndPersistLead("Lead 1", "test-1@example.com", TERRITORY_NORTH, LeadStatus.NEW);
        createAndPersistLead("Lead 2", "test-2@example.com", TERRITORY_NORTH, LeadStatus.NEW);
        createAndPersistLead("Lead 3", "test-3@example.com", TERRITORY_SOUTH, LeadStatus.QUALIFIED);

        // When
        long newCount = repository.countByStatus(LeadStatus.NEW);
        long qualifiedCount = repository.countByStatus(LeadStatus.QUALIFIED);

        // Then
        assertThat(newCount).isGreaterThanOrEqualTo(2);
        assertThat(qualifiedCount).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Given leads in date range, when finding by dates, then should return matching leads")
    @Transactional
    void testFindByDateRange_shouldReturnLeadsInRange() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusDays(1);
        LocalDateTime tomorrow = now.plusDays(1);

        createAndPersistLead("Today Lead", "test-today@example.com", TERRITORY_NORTH);

        // When
        List<LeadEntity> results = repository.findByDateRange(yesterday, tomorrow, 100);

        // Then
        assertThat(results).isNotEmpty();
        assertThat(results).anyMatch(l -> l.getCompanyName().equals("Today Lead"));
    }

    @Test
    @DisplayName("Given leads in territories, when getting stats, then should return grouped counts")
    @Transactional
    void testGetStatsByTerritory_shouldReturnTerritoryStats() {
        // Given
        createAndPersistLead("North 1", "test-n1@example.com", TERRITORY_NORTH);
        createAndPersistLead("North 2", "test-n2@example.com", TERRITORY_NORTH);
        createAndPersistLead("South 1", "test-s1@example.com", TERRITORY_SOUTH);

        // When
        Map<String, Long> stats = repository.getStatsByTerritory();

        // Then
        assertThat(stats).containsKey(TERRITORY_NORTH);
        assertThat(stats).containsKey(TERRITORY_SOUTH);
        assertThat(stats.get(TERRITORY_NORTH)).isGreaterThanOrEqualTo(2);
        assertThat(stats.get(TERRITORY_SOUTH)).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Given multiple leads, when bulk updating status, then should update all accessible")
    @Transactional
    void testBulkUpdateStatus_shouldUpdateAccessibleLeads() {
        // Given
        LeadEntity lead1 = createAndPersistLead("Bulk 1", "test-bulk1@example.com", TERRITORY_NORTH, LeadStatus.NEW);
        LeadEntity lead2 = createAndPersistLead("Bulk 2", "test-bulk2@example.com", TERRITORY_NORTH, LeadStatus.NEW);
        LeadEntity lead3 = createAndPersistLead("Bulk 3", "test-bulk3@example.com", TERRITORY_EAST, LeadStatus.NEW);

        List<UUID> ids = Arrays.asList(lead1.getId(), lead2.getId(), lead3.getId());

        // When
        int updated = repository.bulkUpdateStatus(ids, LeadStatus.QUALIFIED);
        em.flush();

        // Then
        assertThat(updated).isEqualTo(2); // Only NORTH territory leads updated

        LeadEntity updated1 = em.find(LeadEntity.class, lead1.getId());
        LeadEntity updated2 = em.find(LeadEntity.class, lead2.getId());
        LeadEntity notUpdated = em.find(LeadEntity.class, lead3.getId());

        assertThat(updated1.getStatus()).isEqualTo(LeadStatus.QUALIFIED);
        assertThat(updated2.getStatus()).isEqualTo(LeadStatus.QUALIFIED);
        assertThat(notUpdated.getStatus()).isEqualTo(LeadStatus.NEW);
    }

    // ========== Performance Tests ==========

    @Test
    @DisplayName("Given large dataset, when paginating, then should perform within SLO")
    @Transactional
    void testFindPage_withLargeDataset_shouldPerformWithinSLO() {
        // Given - Create 100 test leads
        for (int i = 0; i < 100; i++) {
            createAndPersistLead("Company " + i, "test-perf-" + i + "@example.com",
                                i % 2 == 0 ? TERRITORY_NORTH : TERRITORY_SOUTH);
        }
        em.flush();

        // When - Measure query time
        long startTime = System.currentTimeMillis();
        List<LeadEntity> results = repository.findPage("Company", null, null, null, null, 20);
        long queryTime = System.currentTimeMillis() - startTime;

        // Then - Should complete within 200ms (P95 SLO)
        assertThat(queryTime).isLessThan(200);
        assertThat(results).hasSize(20);
    }

    // ========== Helper Methods ==========

    private LeadEntity createTestLead(String companyName, String email) {
        LeadEntity lead = new LeadEntity();
        lead.setCompanyName(companyName);
        lead.setContactName("Test Contact");
        lead.setEmail(email);
        lead.setPhone("+49 123 456789");
        lead.setStatus(LeadStatus.NEW);
        lead.setSource("TEST");
        lead.setBusinessType("RESTAURANT");
        lead.setMonthlyVolume(5000.0);
        lead.setNotes("Test lead for integration testing");
        return lead;
    }

    @Transactional
    private LeadEntity createAndPersistLead(String companyName, String email, String territory) {
        return createAndPersistLead(companyName, email, territory, LeadStatus.NEW);
    }

    @Transactional
    private LeadEntity createAndPersistLead(String companyName, String email, String territory, LeadStatus status) {
        LeadEntity lead = createTestLead(companyName, email);
        lead.setTerritory(territory);
        lead.setStatus(status);
        em.persist(lead);
        em.flush();
        testLeadIds.add(lead.getId());
        return lead;
    }
}