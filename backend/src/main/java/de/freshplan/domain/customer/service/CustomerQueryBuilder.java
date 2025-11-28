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
import java.util.function.Function;
import org.jboss.logging.Logger;

/**
 * Service for building dynamic Panache queries from search requests. Handles complex filter
 * combinations and sorting.
 */
@SuppressWarnings("PMD.CyclomaticComplexity") // Query builder with many filter operations
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

  // PMD Complexity Refactoring (Issue #146) - Operator to SQL template mapping
  private static final Map<FilterOperator, String> COMPARISON_OPERATORS =
      Map.of(
          FilterOperator.EQUALS, " = :",
          FilterOperator.NOT_EQUALS, " != :",
          FilterOperator.GREATER_THAN, " > :",
          FilterOperator.GREATER_THAN_OR_EQUALS, " >= :",
          FilterOperator.LESS_THAN, " < :",
          FilterOperator.LESS_THAN_OR_EQUALS, " <= :");

  private static final Map<FilterOperator, String> LIKE_PATTERNS =
      Map.of(
          FilterOperator.CONTAINS, "%%%s%%",
          FilterOperator.STARTS_WITH, "%s%%",
          FilterOperator.ENDS_WITH, "%%%s");

  private String buildCondition(FilterCriteria filter, int index, Map<String, Object> params) {
    String field = mapFieldName(filter.getField());
    String paramName = "param" + index;
    FilterOperator operator = filter.getOperator();

    // PMD Complexity Refactoring (Issue #146) - Extracted to helper methods
    validateBetweenOperator(operator, filter.getValue());

    Object value = convertValue(field, filter.getValue());

    // Handle comparison operators (=, !=, >, >=, <, <=)
    if (COMPARISON_OPERATORS.containsKey(operator)) {
      return buildComparisonCondition(field, paramName, operator, value, params);
    }

    // Handle LIKE operators (CONTAINS, STARTS_WITH, ENDS_WITH)
    if (LIKE_PATTERNS.containsKey(operator)) {
      return buildLikeCondition(field, paramName, operator, filter.getValue(), params);
    }

    // Handle remaining operators
    return buildSpecialCondition(field, paramName, operator, filter, params);
  }

  // ============================================================================
  // PMD Complexity Refactoring (Issue #146) - Helper methods for buildCondition()
  // ============================================================================

  private void validateBetweenOperator(FilterOperator operator, Object value) {
    if (operator == FilterOperator.BETWEEN) {
      if (!(value instanceof List)) {
        throw new IllegalArgumentException("BETWEEN operator requires a list value");
      }
      List<?> values = (List<?>) value;
      if (values.size() != 2) {
        throw new IllegalArgumentException("BETWEEN operator requires exactly two values");
      }
    }
  }

  private String buildComparisonCondition(
      String field,
      String paramName,
      FilterOperator operator,
      Object value,
      Map<String, Object> params) {
    params.put(paramName, value);
    return field + COMPARISON_OPERATORS.get(operator) + paramName;
  }

  private String buildLikeCondition(
      String field,
      String paramName,
      FilterOperator operator,
      Object rawValue,
      Map<String, Object> params) {
    String pattern = LIKE_PATTERNS.get(operator);
    params.put(paramName, String.format(pattern, rawValue.toString().toLowerCase()));
    return "LOWER(" + field + ") LIKE :" + paramName;
  }

  private String buildSpecialCondition(
      String field,
      String paramName,
      FilterOperator operator,
      FilterCriteria filter,
      Map<String, Object> params) {
    return switch (operator) {
      case IN -> {
        params.put(paramName, convertListValues(field, filter.getValue()));
        yield field + " IN :" + paramName;
      }
      case NOT_IN -> {
        params.put(paramName, convertListValues(field, filter.getValue()));
        yield field + " NOT IN :" + paramName;
      }
      case IS_NULL -> field + " IS NULL";
      case IS_NOT_NULL -> field + " IS NOT NULL";
      case BETWEEN -> buildBetweenCondition(field, paramName, filter.getValue(), params);
      default -> throw new UnsupportedOperationException("Operator not implemented: " + operator);
    };
  }

  private String buildBetweenCondition(
      String field, String paramName, Object value, Map<String, Object> params) {
    List<?> values = (List<?>) value;
    params.put(paramName + "Min", convertValue(field, values.get(0)));
    params.put(paramName + "Max", convertValue(field, values.get(1)));
    return field + " BETWEEN :" + paramName + "Min AND :" + paramName + "Max";
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

    // Handle enum conversions using generic helper
    if (value instanceof String stringValue) {
      Object enumValue = convertStringToEnum(field, stringValue);
      if (enumValue != null) {
        return enumValue;
      }
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

    // Convert string lists to enums using generic helper
    if (list.get(0) instanceof String) {
      Function<String, Object> enumConverter = getEnumConverter(field);
      if (enumConverter != null) {
        return list.stream().map(item -> enumConverter.apply((String) item)).toList();
      }
    }

    return list;
  }

  /**
   * Converts a string value to the appropriate enum type based on field name. Returns null if the
   * field doesn't require enum conversion.
   */
  private Object convertStringToEnum(String field, String value) {
    return switch (field) {
      case "status" -> CustomerStatus.valueOf(value);
      case "industry" -> Industry.valueOf(value);
      case "lifecycleStage" -> CustomerLifecycleStage.valueOf(value);
      default -> null;
    };
  }

  /**
   * Returns a function that converts strings to the appropriate enum type for the given field.
   * Returns null if the field doesn't require enum conversion.
   */
  private Function<String, Object> getEnumConverter(String field) {
    return switch (field) {
      case "status" -> CustomerStatus::valueOf;
      case "industry" -> Industry::valueOf;
      case "lifecycleStage" -> CustomerLifecycleStage::valueOf;
      default -> null;
    };
  }
}
