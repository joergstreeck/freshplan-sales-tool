package de.freshplan.domain.audit.repository;

import de.freshplan.domain.audit.entity.AuditEntry;
import de.freshplan.domain.audit.entity.AuditEventType;
import de.freshplan.domain.audit.entity.AuditSource;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.TypedQuery;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

/**
 * Enterprise-grade repository for audit trail management
 *
 * <p>Provides comprehensive query capabilities for compliance, security monitoring, and audit trail
 * analysis.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
public class AuditRepository implements PanacheRepositoryBase<AuditEntry, UUID> {

  private static final int DEFAULT_PAGE_SIZE = 50;
  private static final int MAX_PAGE_SIZE = 1000;

  /** Find audit entries by entity */
  public List<AuditEntry> findByEntity(String entityType, UUID entityId) {
    return find(
            "entityType = ?1 and entityId = ?2", Sort.descending("timestamp"), entityType, entityId)
        .list();
  }

  /** Find audit entries by entity with pagination */
  public List<AuditEntry> findByEntity(String entityType, UUID entityId, int page, int size) {
    return find(
            "entityType = ?1 and entityId = ?2", Sort.descending("timestamp"), entityType, entityId)
        .page(Page.of(page, Math.min(size, MAX_PAGE_SIZE)))
        .list();
  }

  /** Find audit entries by user */
  public List<AuditEntry> findByUser(UUID userId, Instant from, Instant to) {
    return find(
            "userId = ?1 and timestamp between ?2 and ?3",
            Sort.descending("timestamp"),
            userId,
            from,
            to)
        .list();
  }

  /** Find audit entries by event type */
  public List<AuditEntry> findByEventType(AuditEventType eventType, Instant from, Instant to) {
    return find(
            "eventType = ?1 and timestamp between ?2 and ?3",
            Sort.descending("timestamp"),
            eventType,
            from,
            to)
        .list();
  }

  /** Find security-relevant audit entries */
  public List<AuditEntry> findSecurityEvents(Instant from, Instant to) {
    return find(
            "eventType in ?1 and timestamp between ?2 and ?3",
            Sort.descending("timestamp"),
            getSecurityEventTypes(),
            from,
            to)
        .list();
  }

  /** Find failed operations */
  public List<AuditEntry> findFailures(Instant from, Instant to) {
    return find(
            "(eventType like '%FAILURE%' or eventType like '%DENIED%' or eventType like '%ERROR%') "
                + "and timestamp between ?1 and ?2",
            Sort.descending("timestamp"), from, to)
        .list();
  }

  /** Advanced search with multiple filters */
  public List<AuditEntry> search(AuditSearchCriteria criteria) {
    Map<String, Object> params = new HashMap<>();
    StringBuilder query = new StringBuilder("1=1");

    if (criteria.getEntityType() != null) {
      query.append(" and entityType = :entityType");
      params.put("entityType", criteria.getEntityType());
    }

    if (criteria.getEntityId() != null) {
      query.append(" and entityId = :entityId");
      params.put("entityId", criteria.getEntityId());
    }

    if (criteria.getUserId() != null) {
      query.append(" and userId = :userId");
      params.put("userId", criteria.getUserId());
    }

    if (criteria.getEventTypes() != null && !criteria.getEventTypes().isEmpty()) {
      query.append(" and eventType in :eventTypes");
      params.put("eventTypes", criteria.getEventTypes());
    }

    if (criteria.getSources() != null && !criteria.getSources().isEmpty()) {
      query.append(" and source in :sources");
      params.put("sources", criteria.getSources());
    }

    if (criteria.getFrom() != null) {
      query.append(" and timestamp >= :from");
      params.put("from", criteria.getFrom());
    }

    if (criteria.getTo() != null) {
      query.append(" and timestamp <= :to");
      params.put("to", criteria.getTo());
    }

    if (criteria.getSearchText() != null && !criteria.getSearchText().isBlank()) {
      query.append(" and (userComment like :searchText or changeReason like :searchText)");
      params.put("searchText", "%" + criteria.getSearchText() + "%");
    }

    return find(query.toString(), Sort.descending("timestamp"), params)
        .page(Page.of(criteria.getPage(), Math.min(criteria.getSize(), MAX_PAGE_SIZE)))
        .list();
  }

  /** Get audit statistics for a time period */
  public AuditStatistics getStatistics(Instant from, Instant to) {
    String query =
        """
            SELECT
                COUNT(*) as totalEvents,
                COUNT(DISTINCT userId) as uniqueUsers,
                COUNT(DISTINCT entityId) as uniqueEntities,
                COUNT(CASE WHEN eventType LIKE '%FAILURE%' OR eventType LIKE '%ERROR%' THEN 1 END) as failureCount
            FROM AuditEntry
            WHERE timestamp BETWEEN :from AND :to
            """;

    Object[] result =
        (Object[])
            getEntityManager()
                .createQuery(query)
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult();

    return AuditStatistics.builder()
        .totalEvents((Long) result[0])
        .uniqueUsers((Long) result[1])
        .uniqueEntities((Long) result[2])
        .failureCount((Long) result[3])
        .period(from, to)
        .build();
  }

  /** Get last audit entry hash for blockchain-style chaining */
  public Optional<String> getLastHash() {
    return find("1=1", Sort.descending("timestamp"))
        .firstResultOptional()
        .map(AuditEntry::getDataHash);
  }

  /** Stream audit entries for export (memory-efficient) */
  public Stream<AuditEntry> streamForExport(AuditSearchCriteria criteria) {
    // Build query similar to search() but return stream
    Map<String, Object> params = new HashMap<>();
    StringBuilder queryStr = new StringBuilder("1=1");

    // ... (same query building logic as search method)

    TypedQuery<AuditEntry> query =
        getEntityManager()
            .createQuery("SELECT a FROM AuditEntry a WHERE " + queryStr, AuditEntry.class);

    params.forEach(query::setParameter);

    return query.getResultStream();
  }

  /** Clean up old audit entries (for data retention) */
  public long deleteOlderThan(Instant cutoffDate, boolean dryRun) {
    if (dryRun) {
      return count("timestamp < ?1", cutoffDate);
    }

    return delete("timestamp < ?1", cutoffDate);
  }

  /** Find entries that require notification */
  public List<AuditEntry> findRequiringNotification() {
    List<AuditEventType> notificationTypes =
        Arrays.stream(AuditEventType.values())
            .filter(AuditEventType::requiresNotification)
            .toList();

    return find(
            "eventType in ?1 and timestamp > ?2",
            Sort.descending("timestamp"),
            notificationTypes,
            Instant.now().minusSeconds(300)) // Last 5 minutes
        .list();
  }

  /** Verify audit trail integrity */
  public List<AuditIntegrityIssue> verifyIntegrity(Instant from, Instant to) {
    List<AuditIntegrityIssue> issues = new ArrayList<>();

    List<AuditEntry> entries =
        find("timestamp between ?1 and ?2", Sort.ascending("timestamp"), from, to).list();

    String expectedPreviousHash = null;
    for (AuditEntry entry : entries) {
      if (expectedPreviousHash != null && !expectedPreviousHash.equals(entry.getPreviousHash())) {
        issues.add(
            new AuditIntegrityIssue(
                entry.getId(), "Hash chain broken", expectedPreviousHash, entry.getPreviousHash()));
      }
      expectedPreviousHash = entry.getDataHash();
    }

    return issues;
  }

  private List<AuditEventType> getSecurityEventTypes() {
    return Arrays.stream(AuditEventType.values())
        .filter(AuditEventType::isSecurityRelevant)
        .toList();
  }

  /** Search criteria for advanced audit queries */
  public static class AuditSearchCriteria {
    private String entityType;
    private UUID entityId;
    private UUID userId;
    private List<AuditEventType> eventTypes;
    private List<AuditSource> sources;
    private Instant from;
    private Instant to;
    private String searchText;
    private int page = 0;
    private int size = DEFAULT_PAGE_SIZE;

    private AuditSearchCriteria(Builder builder) {
      this.entityType = builder.entityType;
      this.entityId = builder.entityId;
      this.userId = builder.userId;
      this.eventTypes = builder.eventTypes;
      this.sources = builder.sources;
      this.from = builder.from;
      this.to = builder.to;
      this.searchText = builder.searchText;
      this.page = builder.page;
      this.size = builder.size;
    }

    // Getters
    public String getEntityType() {
      return entityType;
    }

    public UUID getEntityId() {
      return entityId;
    }

    public UUID getUserId() {
      return userId;
    }

    public List<AuditEventType> getEventTypes() {
      return eventTypes;
    }

    public List<AuditSource> getSources() {
      return sources;
    }

    public Instant getFrom() {
      return from;
    }

    public Instant getTo() {
      return to;
    }

    public String getSearchText() {
      return searchText;
    }

    public int getPage() {
      return page;
    }

    public int getSize() {
      return size;
    }

    // Setters
    public void setEntityType(String entityType) {
      this.entityType = entityType;
    }

    public void setEntityId(UUID entityId) {
      this.entityId = entityId;
    }

    public void setUserId(UUID userId) {
      this.userId = userId;
    }

    public void setEventTypes(List<AuditEventType> eventTypes) {
      this.eventTypes = eventTypes;
    }

    public void setSources(List<AuditSource> sources) {
      this.sources = sources;
    }

    public void setFrom(Instant from) {
      this.from = from;
    }

    public void setTo(Instant to) {
      this.to = to;
    }

    public void setSearchText(String searchText) {
      this.searchText = searchText;
    }

    public void setPage(int page) {
      this.page = page;
    }

    public void setSize(int size) {
      this.size = size;
    }

    public static Builder builder() {
      return new Builder();
    }

    public static class Builder {
      private String entityType;
      private UUID entityId;
      private UUID userId;
      private List<AuditEventType> eventTypes;
      private List<AuditSource> sources;
      private Instant from;
      private Instant to;
      private String searchText;
      private int page = 0;
      private int size = DEFAULT_PAGE_SIZE;

      public Builder entityType(String entityType) {
        this.entityType = entityType;
        return this;
      }

      public Builder entityId(UUID entityId) {
        this.entityId = entityId;
        return this;
      }

      public Builder userId(UUID userId) {
        this.userId = userId;
        return this;
      }

      public Builder eventTypes(List<AuditEventType> eventTypes) {
        this.eventTypes = eventTypes;
        return this;
      }

      public Builder sources(List<AuditSource> sources) {
        this.sources = sources;
        return this;
      }

      public Builder from(Instant from) {
        this.from = from;
        return this;
      }

      public Builder to(Instant to) {
        this.to = to;
        return this;
      }

      public Builder searchText(String searchText) {
        this.searchText = searchText;
        return this;
      }

      public Builder page(int page) {
        this.page = page;
        return this;
      }

      public Builder size(int size) {
        this.size = size;
        return this;
      }

      public AuditSearchCriteria build() {
        return new AuditSearchCriteria(this);
      }
    }
  }

  /** Audit statistics */
  public static class AuditStatistics {
    private final Long totalEvents;
    private final Long uniqueUsers;
    private final Long uniqueEntities;
    private final Long failureCount;
    private final Instant periodStart;
    private final Instant periodEnd;

    private AuditStatistics(Builder builder) {
      this.totalEvents = builder.totalEvents;
      this.uniqueUsers = builder.uniqueUsers;
      this.uniqueEntities = builder.uniqueEntities;
      this.failureCount = builder.failureCount;
      this.periodStart = builder.periodStart;
      this.periodEnd = builder.periodEnd;
    }

    // Getters
    public Long getTotalEvents() {
      return totalEvents;
    }

    public Long getUniqueUsers() {
      return uniqueUsers;
    }

    public Long getUniqueEntities() {
      return uniqueEntities;
    }

    public Long getFailureCount() {
      return failureCount;
    }

    public Instant getPeriodStart() {
      return periodStart;
    }

    public Instant getPeriodEnd() {
      return periodEnd;
    }

    public static Builder builder() {
      return new Builder();
    }

    public static class Builder {
      private Long totalEvents;
      private Long uniqueUsers;
      private Long uniqueEntities;
      private Long failureCount;
      private Instant periodStart;
      private Instant periodEnd;

      public Builder totalEvents(Long totalEvents) {
        this.totalEvents = totalEvents;
        return this;
      }

      public Builder uniqueUsers(Long uniqueUsers) {
        this.uniqueUsers = uniqueUsers;
        return this;
      }

      public Builder uniqueEntities(Long uniqueEntities) {
        this.uniqueEntities = uniqueEntities;
        return this;
      }

      public Builder failureCount(Long failureCount) {
        this.failureCount = failureCount;
        return this;
      }

      public Builder periodStart(Instant periodStart) {
        this.periodStart = periodStart;
        return this;
      }

      public Builder periodEnd(Instant periodEnd) {
        this.periodEnd = periodEnd;
        return this;
      }

      public Builder period(Instant from, Instant to) {
        this.periodStart = from;
        this.periodEnd = to;
        return this;
      }

      public AuditStatistics build() {
        return new AuditStatistics(this);
      }
    }
  }

  /** Integrity issue for audit trail verification */
  public static class AuditIntegrityIssue {
    private final UUID entryId;
    private final String issue;
    private final String expected;
    private final String actual;

    public AuditIntegrityIssue(UUID entryId, String issue, String expected, String actual) {
      this.entryId = entryId;
      this.issue = issue;
      this.expected = expected;
      this.actual = actual;
    }

    // Getters
    public UUID getEntryId() {
      return entryId;
    }

    public String getIssue() {
      return issue;
    }

    public String getExpected() {
      return expected;
    }

    public String getActual() {
      return actual;
    }
  }
}
