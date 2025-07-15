package de.freshplan.domain.customer.service;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerLifecycleStage;
import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.domain.customer.entity.Industry;
import de.freshplan.domain.customer.service.dto.CustomerSearchRequest;
import de.freshplan.domain.customer.service.dto.FilterCriteria;
import de.freshplan.domain.customer.service.dto.FilterOperator;
import de.freshplan.domain.customer.service.dto.LogicalOperator;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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

    // Apply sorting - Multi-Sort takes precedence over single sort
    if (request.getMultiSort() != null && !request.getMultiSort().isEmpty()) {
      Sort multiSort = buildMultiSort(request.getMultiSort());
      query = Customer.find(queryString, multiSort, params);
    } else if (request.getSort() != null) {
      Sort sort =
          request.getSort().isAscending()
              ? Sort.by(request.getSort().getField()).ascending()
              : Sort.by(request.getSort().getField()).descending();
      query = Customer.find(queryString, sort, params);
    }

    return query;
  }

  /**
   * Builds a multi-sort Sort object from a list of sort criteria. Supports complex sorting
   * strategies for sales-oriented customer prioritization.
   */
  private Sort buildMultiSort(
      List<de.freshplan.domain.customer.service.dto.SortCriteria> sortCriteria) {
    if (sortCriteria.isEmpty()) {
      return Sort.by("companyName").ascending(); // Default fallback
    }

    // Start with the first sort criterion
    de.freshplan.domain.customer.service.dto.SortCriteria first = sortCriteria.get(0);
    String firstField = mapSortField(first.getField());
    Sort sort =
        first.isAscending() ? Sort.by(firstField).ascending() : Sort.by(firstField).descending();

    // Chain additional sort criteria
    for (int i = 1; i < sortCriteria.size(); i++) {
      de.freshplan.domain.customer.service.dto.SortCriteria criteria = sortCriteria.get(i);
      String field = mapSortField(criteria.getField());

      sort =
          criteria.isAscending()
              ? sort.and(field, Sort.Direction.Ascending)
              : sort.and(field, Sort.Direction.Descending);
    }

    return sort;
  }

  /**
   * Maps frontend sort field names to database field names. Handles special cases for computed
   * fields and relationships.
   */
  private String mapSortField(String frontendField) {
    switch (frontendField) {
      case "companyName":
        return "companyName";
      case "riskScore":
        return "riskScore";
      case "expectedAnnualVolume":
        return "expectedAnnualVolume";
      case "lastContactDate":
        return "lastContactDate";
      case "nextFollowUpDate":
        return "nextFollowUpDate";
      case "createdAt":
        return "createdAt";
      case "updatedAt":
        return "updatedAt";
      case "status":
        return "status";
      case "atRisk":
        return "atRisk";
      case "industry":
        return "industry";
      case "customerNumber":
        return "customerNumber";
      default:
        LOG.warnf("Unknown sort field: %s, falling back to companyName", frontendField);
        return "companyName";
    }
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

    if ("industry".equals(field) && value instanceof String) {
      return Industry.valueOf((String) value);
    }

    if ("lifecycleStage".equals(field) && value instanceof String) {
      return CustomerLifecycleStage.valueOf((String) value);
    }

    // Handle DateTime conversions for date/time fields
    if (isDateTimeField(field) && value instanceof String) {
      return convertStringToLocalDateTime((String) value);
    }

    return value;
  }

  /** Checks if a field is a DateTime field that needs conversion. */
  private boolean isDateTimeField(String field) {
    return "createdAt".equals(field)
        || "updatedAt".equals(field)
        || "lastContactDate".equals(field)
        || "nextFollowUpDate".equals(field);
  }

  /** Converts a string to LocalDateTime. Supports various date formats. */
  private LocalDateTime convertStringToLocalDateTime(String dateString) {
    if (dateString == null || dateString.trim().isEmpty()) {
      return null;
    }

    try {
      // Try ISO date format first (2024-01-01T00:00:00)
      if (dateString.contains("T")) {
        return LocalDateTime.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
      }

      // Try date-only format (2024-01-01) - add midnight time
      if (dateString.matches("\\d{4}-\\d{2}-\\d{2}")) {
        return LocalDateTime.parse(dateString + "T00:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME);
      }

      // Try other common formats if needed
      throw new IllegalArgumentException("Unsupported date format: " + dateString);

    } catch (DateTimeParseException e) {
      LOG.errorf("Failed to parse date string '%s': %s", dateString, e.getMessage());
      throw new IllegalArgumentException(
          "Invalid date format: "
              + dateString
              + ". Expected format: YYYY-MM-DD or YYYY-MM-DDTHH:MM:SS",
          e);
    }
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

    // For industry field, convert strings to enums
    if ("industry".equals(field) && list.get(0) instanceof String) {
      return list.stream().map(item -> Industry.valueOf((String) item)).toList();
    }

    // For lifecycleStage field, convert strings to enums
    if ("lifecycleStage".equals(field) && list.get(0) instanceof String) {
      return list.stream().map(item -> CustomerLifecycleStage.valueOf((String) item)).toList();
    }

    return list;
  }
}
