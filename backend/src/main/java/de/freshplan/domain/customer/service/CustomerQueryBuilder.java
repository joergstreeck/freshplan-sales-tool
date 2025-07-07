package de.freshplan.domain.customer.service;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.domain.customer.service.dto.CustomerSearchRequest;
import de.freshplan.domain.customer.service.dto.FilterCriteria;
import de.freshplan.domain.customer.service.dto.FilterOperator;
import de.freshplan.domain.customer.service.dto.LogicalOperator;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jboss.logging.Logger;

/**
 * Service for building dynamic Panache queries from search requests. Handles complex filter
 * combinations and sorting.
 */
@ApplicationScoped
public class CustomerQueryBuilder {

  private static final Logger LOG = Logger.getLogger(CustomerQueryBuilder.class);

  /**
   * Builds a PanacheQuery from the given search request.
   *
   * @param request the search request containing filters and sorting
   * @return a configured PanacheQuery
   */
  public PanacheQuery<Customer> buildQuery(CustomerSearchRequest request) {
    Map<String, Object> params = new HashMap<>();
    List<String> conditions = new ArrayList<>();

    // Add global search condition
    if (request.getGlobalSearch() != null && !request.getGlobalSearch().trim().isEmpty()) {
      conditions.add(buildGlobalSearchCondition(request.getGlobalSearch(), params));
    }

    // Add filter conditions
    if (request.getFilters() != null && !request.getFilters().isEmpty()) {
      buildFilterConditions(request.getFilters(), conditions, params);
    }

    // Build final query string
    String queryString = conditions.isEmpty() ? "1=1" : String.join(" ", conditions);

    LOG.debugf("Built query: %s with params: %s", queryString, params);

    // Create query with sorting
    PanacheQuery<Customer> query = Customer.find(queryString, params);

    // Apply sorting
    if (request.getSort() != null) {
      Sort sort =
          request.getSort().isAscending()
              ? Sort.by(request.getSort().getField()).ascending()
              : Sort.by(request.getSort().getField()).descending();
      query = Customer.find(queryString, sort, params);
    }

    return query;
  }

  private String buildGlobalSearchCondition(String searchTerm, Map<String, Object> params) {
    String paramName = "globalSearch";
    params.put(paramName, "%" + searchTerm.toLowerCase() + "%");

    return "(LOWER(companyName) LIKE :"
        + paramName
        + " OR LOWER(customerNumber) LIKE :"
        + paramName
        + " OR LOWER(tradingName) LIKE :"
        + paramName
        + ")";
  }

  private void buildFilterConditions(
      List<FilterCriteria> filters, List<String> conditions, Map<String, Object> params) {

    for (int i = 0; i < filters.size(); i++) {
      FilterCriteria filter = filters.get(i);
      String condition = buildCondition(filter, i, params);

      if (i == 0 && conditions.isEmpty()) {
        // First condition without AND/OR
        conditions.add(condition);
      } else if (filter.getCombineWith() == LogicalOperator.AND) {
        conditions.add("AND " + condition);
      } else {
        conditions.add("OR " + condition);
      }
    }
  }

  private String buildCondition(FilterCriteria filter, int index, Map<String, Object> params) {
    String field = mapFieldName(filter.getField());
    String paramName = "param" + index;
    FilterOperator operator = filter.getOperator();
    
    // Validate BETWEEN operator early
    if (operator == FilterOperator.BETWEEN) {
      if (!(filter.getValue() instanceof List)) {
        throw new IllegalArgumentException("BETWEEN operator requires a list value");
      }
      List<?> values = (List<?>) filter.getValue();
      if (values.size() != 2) {
        throw new IllegalArgumentException("BETWEEN operator requires exactly two values");
      }
    }
    
    Object value = convertValue(field, filter.getValue());

    switch (operator) {
      case EQUALS:
        params.put(paramName, value);
        return field + " = :" + paramName;

      case NOT_EQUALS:
        params.put(paramName, value);
        return field + " != :" + paramName;

      case GREATER_THAN:
        params.put(paramName, value);
        return field + " > :" + paramName;

      case GREATER_THAN_OR_EQUALS:
        params.put(paramName, value);
        return field + " >= :" + paramName;

      case LESS_THAN:
        params.put(paramName, value);
        return field + " < :" + paramName;

      case LESS_THAN_OR_EQUALS:
        params.put(paramName, value);
        return field + " <= :" + paramName;

      case CONTAINS:
        params.put(paramName, "%" + filter.getValue().toString().toLowerCase() + "%");
        return "LOWER(" + field + ") LIKE :" + paramName;

      case STARTS_WITH:
        params.put(paramName, filter.getValue().toString().toLowerCase() + "%");
        return "LOWER(" + field + ") LIKE :" + paramName;

      case ENDS_WITH:
        params.put(paramName, "%" + filter.getValue().toString().toLowerCase());
        return "LOWER(" + field + ") LIKE :" + paramName;

      case IN:
        params.put(paramName, convertListValues(field, filter.getValue()));
        return field + " IN :" + paramName;

      case NOT_IN:
        params.put(paramName, convertListValues(field, filter.getValue()));
        return field + " NOT IN :" + paramName;

      case IS_NULL:
        return field + " IS NULL";

      case IS_NOT_NULL:
        return field + " IS NOT NULL";

      case BETWEEN:
        // Expecting a list with two values [min, max]
        if (filter.getValue() instanceof List) {
          List<?> values = (List<?>) filter.getValue();
          if (values.size() == 2) {
            params.put(paramName + "Min", convertValue(field, values.get(0)));
            params.put(paramName + "Max", convertValue(field, values.get(1)));
            return field + " BETWEEN :" + paramName + "Min AND :" + paramName + "Max";
          }
        }
        throw new IllegalArgumentException("BETWEEN operator requires a list with two values");

      default:
        throw new UnsupportedOperationException("Operator not implemented: " + operator);
    }
  }

  /**
   * Maps DTO field names to entity field names. This allows the API to use different field names
   * than the database.
   */
  private String mapFieldName(String dtoField) {
    // Add any field mappings here if needed
    // For now, we use the same names
    return dtoField;
  }

  /**
   * Converts a value to the appropriate type based on the field name. Handles enum conversions and
   * other type mappings.
   */
  private Object convertValue(String field, Object value) {
    if (value == null) {
      return null;
    }

    // Handle enum conversions
    if ("status".equals(field) && value instanceof String) {
      return CustomerStatus.valueOf((String) value);
    }

    // Add other field-specific conversions here as needed

    return value;
  }

  /** Converts list values for IN/NOT_IN operators. */
  @SuppressWarnings("unchecked")
  private Object convertListValues(String field, Object value) {
    if (!(value instanceof List)) {
      return value;
    }

    List<?> list = (List<?>) value;
    if (list.isEmpty()) {
      return list;
    }

    // For status field, convert strings to enums
    if ("status".equals(field) && list.get(0) instanceof String) {
      return list.stream().map(item -> CustomerStatus.valueOf((String) item)).toList();
    }

    return list;
  }
}
