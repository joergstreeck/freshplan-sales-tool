# 📊 PERFORMANCE GOALS & DATABASE OPTIMIZATION

**Navigation:**
- **Parent:** [Performance Overview](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/07-PERFORMANCE/README.md)
- **Next:** [Caching & API](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/07-PERFORMANCE/02-caching-api.md)
- **Related:** [Database Schema](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/02-BACKEND/04-database.md)

## Performance-Ziele

### SLA Targets

| Operation | Target P50 | Target P95 | Target P99 | Max |
|-----------|------------|------------|------------|-----|
| Field Definitions laden | 50ms | 100ms | 200ms | 500ms |
| Customer Draft erstellen | 100ms | 200ms | 500ms | 1s |
| Customer suchen (einfach) | 50ms | 150ms | 300ms | 500ms |
| Customer suchen (komplex) | 100ms | 300ms | 500ms | 1s |
| Field Values laden | 50ms | 100ms | 200ms | 500ms |
| Bulk Operations (100 items) | 500ms | 1s | 2s | 5s |

### Skalierungsziele

- **Customers:** 100.000+ aktive Kunden
- **Field Values:** 50+ Felder pro Kunde (5M+ Einträge)
- **Locations:** Durchschnitt 5 pro Kettenkunde
- **Concurrent Users:** 1.000+ gleichzeitige Nutzer
- **API Requests:** 10.000 req/min Peak

## Database Optimierung

### Index-Strategie

```sql
-- Primary Indexes (automatisch durch PKs)
-- Zusätzliche Performance-Indexes

-- Field Values: Häufigste Zugriffsmuster
CREATE INDEX idx_field_values_entity_type_id 
    ON field_values(entity_id, entity_type) 
    INCLUDE (field_definition_id, value);

CREATE INDEX idx_field_values_definition_value 
    ON field_values(field_definition_id, value) 
    WHERE entity_type = 'CUSTOMER';

-- Customer Search Optimization
CREATE INDEX idx_customer_status_created 
    ON customers(status, created_at DESC) 
    WHERE status != 'DELETED';

-- GIN Index für JSONB Suche
CREATE INDEX idx_field_values_jsonb 
    ON field_values USING gin(value jsonb_path_ops);

-- Partial Indexes für häufige Queries
CREATE INDEX idx_active_customers 
    ON customers(id) 
    WHERE status = 'ACTIVE';

CREATE INDEX idx_draft_customers_user 
    ON customers(created_by, created_at DESC) 
    WHERE status = 'DRAFT';

-- Location Performance
CREATE INDEX idx_locations_customer_type 
    ON locations(customer_id, location_type);

-- Composite Index für Sortierung
CREATE INDEX idx_customer_company_name 
    ON field_values((value->>'companyName')) 
    WHERE field_definition_id = 'companyName' 
    AND entity_type = 'CUSTOMER';
```

### Query Optimization

```java
@ApplicationScoped
public class OptimizedCustomerRepository {
    
    @Inject
    EntityManager em;
    
    // Optimierte Suche mit Pagination
    public Page<CustomerSearchResult> searchCustomers(
        SearchCriteria criteria,
        Pageable pageable
    ) {
        // Build dynamic query with selective joins
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<CustomerSearchResult> query = 
            cb.createQuery(CustomerSearchResult.class);
        
        Root<Customer> customer = query.from(Customer.class);
        
        // Selective field loading
        query.select(cb.construct(
            CustomerSearchResult.class,
            customer.get("id"),
            customer.get("status"),
            // Subquery for company name
            cb.literal("") // Placeholder, filled by second query
        ));
        
        // Apply filters
        List<Predicate> predicates = buildPredicates(criteria, cb, customer);
        query.where(predicates.toArray(new Predicate[0]));
        
        // Count query for pagination
        Long total = executeCountQuery(predicates);
        
        // Execute main query
        List<CustomerSearchResult> results = em.createQuery(query)
            .setFirstResult(pageable.getOffset())
            .setMaxResults(pageable.getPageSize())
            .getResultList();
        
        // Batch load field values
        enrichWithFieldValues(results);
        
        return new PageImpl<>(results, pageable, total);
    }
    
    // Batch loading für N+1 Prevention
    private void enrichWithFieldValues(List<CustomerSearchResult> results) {
        if (results.isEmpty()) return;
        
        Set<UUID> customerIds = results.stream()
            .map(CustomerSearchResult::getId)
            .collect(Collectors.toSet());
        
        // Single query for all field values
        Map<UUID, Map<String, Object>> fieldValueMap = em.createQuery(
            "SELECT fv.entityId, fv.fieldDefinitionId, fv.value " +
            "FROM FieldValue fv " +
            "WHERE fv.entityId IN :ids " +
            "AND fv.entityType = :type " +
            "AND fv.fieldDefinitionId IN :fields",
            Object[].class
        )
        .setParameter("ids", customerIds)
        .setParameter("type", EntityType.CUSTOMER)
        .setParameter("fields", List.of("companyName", "industry", "city"))
        .getResultStream()
        .collect(Collectors.groupingBy(
            row -> (UUID) row[0],
            Collectors.toMap(
                row -> (String) row[1],
                row -> row[2]
            )
        ));
        
        // Enrich results
        results.forEach(result -> {
            Map<String, Object> values = fieldValueMap.get(result.getId());
            if (values != null) {
                result.setCompanyName((String) values.get("companyName"));
                result.setIndustry((String) values.get("industry"));
                result.setCity((String) values.get("city"));
            }
        });
    }
}
```

### Database Partitioning

```sql
-- Partitionierung für große Tabellen
-- Field Values nach Entity Type
CREATE TABLE field_values (
    id UUID PRIMARY KEY,
    field_definition_id VARCHAR(100) NOT NULL,
    entity_id UUID NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    value JSONB,
    updated_at TIMESTAMP NOT NULL
) PARTITION BY LIST (entity_type);

CREATE TABLE field_values_customer PARTITION OF field_values
    FOR VALUES IN ('CUSTOMER');

CREATE TABLE field_values_location PARTITION OF field_values
    FOR VALUES IN ('LOCATION');

CREATE TABLE field_values_detailed_location PARTITION OF field_values
    FOR VALUES IN ('DETAILED_LOCATION');

-- Time-based partitioning for audit logs
CREATE TABLE customer_data_access_log (
    id UUID PRIMARY KEY,
    accessed_at TIMESTAMP NOT NULL,
    -- other columns
) PARTITION BY RANGE (accessed_at);

CREATE TABLE customer_data_access_log_2025_q1 
    PARTITION OF customer_data_access_log
    FOR VALUES FROM ('2025-01-01') TO ('2025-04-01');
```

## Best Practices

### 1. Query Patterns
- Verwende immer Pagination für Listen
- Lade nur benötigte Felder (Field Projection)
- Nutze Batch-Loading für N+1 Prevention

### 2. Index Management
- Regelmäßige ANALYZE für Statistiken
- Monitor Index-Usage mit pg_stat_user_indexes
- Unused Indexes entfernen

### 3. Connection Pooling
- Min: 10 Connections
- Max: 50 Connections pro Service
- Timeout: 5 Sekunden

---

**Next:** [Caching & API →](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/07-PERFORMANCE/02-caching-api.md)