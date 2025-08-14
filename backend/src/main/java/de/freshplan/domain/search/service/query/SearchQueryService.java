package de.freshplan.domain.search.service.query;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerContact;
import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.domain.customer.repository.ContactRepository;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.search.service.dto.ContactSearchDto;
import de.freshplan.domain.search.service.dto.CustomerSearchDto;
import de.freshplan.domain.search.service.dto.QueryType;
import de.freshplan.domain.search.service.dto.SearchResult;
import de.freshplan.domain.search.service.dto.SearchResults;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;

/**
 * CQRS Query Service for universal search functionality across multiple entities.
 * 
 * This service handles all read-only search operations with intelligent query analysis,
 * parallel search, and relevance scoring. Part of the CQRS migration from SearchService.
 * 
 * Key Features:
 * - Query type detection (EMAIL, PHONE, CUSTOMER_NUMBER, TEXT)
 * - Multi-entity search (customers and contacts)
 * - Relevance scoring algorithms
 * - Performance-optimized quick search for autocomplete
 * 
 * @since Phase 10 CQRS Migration
 */
@ApplicationScoped
public class SearchQueryService {

  private static final Logger LOG = Logger.getLogger(SearchQueryService.class);

  // Regex patterns for query type detection
  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
  private static final Pattern PHONE_PATTERN = Pattern.compile("^[\\d\\s+\\-()]+$");
  private static final Pattern CUSTOMER_NUMBER_PATTERN = Pattern.compile("^[A-Z0-9\\-]+$");

  @Inject CustomerRepository customerRepository;

  @Inject ContactRepository contactRepository;

  /**
   * Performs universal search across customers and contacts.
   * 
   * Analyzes the query to determine its type (email, phone, customer number, or text)
   * and applies appropriate search strategies for optimal performance and relevance.
   *
   * @param query The search query
   * @param includeContacts Whether to include contacts in search results
   * @param includeInactive Whether to include inactive customers
   * @param limit Maximum results per entity type
   * @return Combined search results with relevance scoring
   */
  public SearchResults universalSearch(
      String query, boolean includeContacts, boolean includeInactive, int limit) {

    long startTime = System.currentTimeMillis();

    // Analyze query type for optimal search strategy
    QueryType queryType = detectQueryType(query);
    LOG.debugf("Query type detected: %s for query: %s", queryType, query);

    // Prepare search results
    List<SearchResult> customerResults = new ArrayList<>();
    List<SearchResult> contactResults = new ArrayList<>();

    // Search customers using type-specific strategy
    customerResults = searchCustomers(query, queryType, includeInactive, limit);

    // Search contacts if requested
    if (includeContacts) {
      contactResults = searchContacts(query, queryType, limit);
    }

    // Calculate execution time
    long executionTime = System.currentTimeMillis() - startTime;
    LOG.debugf("Universal search completed in %dms: %d customers, %d contacts found", 
        executionTime, customerResults.size(), contactResults.size());

    // Build and return results
    return SearchResults.builder()
        .customers(customerResults)
        .contacts(contactResults)
        .totalCount(customerResults.size() + contactResults.size())
        .executionTime(executionTime)
        .build();
  }

  /**
   * Quick search for autocomplete functionality. 
   * 
   * Returns minimal data for performance, optimized for fast response times.
   * Only searches customer names and numbers for simplicity.
   *
   * @param query The search query
   * @param limit Maximum number of results
   * @return Lightweight search results for autocomplete
   */
  public SearchResults quickSearch(String query, int limit) {
    long startTime = System.currentTimeMillis();
    LOG.debugf("Quick search starting for query: %s", query);

    // Quick search only in customer names and numbers
    List<Customer> customers = customerRepository.quickSearch(query, limit);

    List<SearchResult> customerResults =
        customers.stream()
            .map(
                customer -> {
                  // Create DTO to avoid lazy loading issues
                  CustomerSearchDto dto = new CustomerSearchDto();
                  dto.setId(customer.getId().toString());
                  dto.setCompanyName(customer.getCompanyName());
                  dto.setCustomerNumber(customer.getCustomerNumber());
                  dto.setStatus(
                      customer.getStatus() != null ? customer.getStatus().toString() : null);

                  return SearchResult.builder()
                      .type("customer")
                      .id(customer.getId().toString())
                      .data(dto)
                      .relevanceScore(100) // Quick search doesn't calculate detailed scores
                      .matchedFields(List.of("companyName", "customerNumber"))
                      .build();
                })
            .collect(Collectors.toList());

    long executionTime = System.currentTimeMillis() - startTime;
    LOG.debugf("Quick search completed in %dms: %d results", executionTime, customerResults.size());

    return SearchResults.builder()
        .customers(customerResults)
        .contacts(new ArrayList<>()) // Quick search doesn't include contacts
        .totalCount(customerResults.size())
        .executionTime(executionTime)
        .build();
  }

  /**
   * Searches for customers based on query and detected type.
   * 
   * Uses different search strategies based on query type:
   * - EMAIL: Search in contact email fields
   * - PHONE: Search in phone fields
   * - CUSTOMER_NUMBER: Exact or prefix match on customer number
   * - TEXT: Full text search across multiple fields
   */
  private List<SearchResult> searchCustomers(
      String query, QueryType queryType, boolean includeInactive, int limit) {

    List<Customer> customers;
    List<String> matchedFields = new ArrayList<>();

    // Apply type-specific search strategy
    switch (queryType) {
      case EMAIL:
        // Search in contact email field if available
        customers = customerRepository.findByContactEmail(query, limit);
        matchedFields.add("contactEmail");
        break;

      case PHONE:
        // Search in phone fields
        customers = customerRepository.findByPhone(query, limit);
        matchedFields.add("phone");
        break;

      case CUSTOMER_NUMBER:
        // Exact or prefix match on customer number
        customers = customerRepository.findByCustomerNumberLike(query + "%", limit);
        matchedFields.add("customerNumber");
        break;

      case TEXT:
      default:
        // Full text search in multiple fields
        customers = customerRepository.searchFullText(query, limit);
        matchedFields.addAll(List.of("companyName", "tradingName", "customerNumber"));
        break;
    }

    // Filter out inactive customers if requested
    if (!includeInactive) {
      customers =
          customers.stream()
              .filter(c -> c.getStatus() != CustomerStatus.INAKTIV)
              .collect(Collectors.toList());
    }

    // Convert to search results with relevance scoring
    return customers.stream()
        .map(
            customer -> {
              int score = calculateRelevanceScore(customer, query, queryType);
              
              // Create DTO to avoid lazy loading issues
              CustomerSearchDto dto = new CustomerSearchDto();
              dto.setId(customer.getId().toString());
              dto.setCompanyName(customer.getCompanyName());
              dto.setCustomerNumber(customer.getCustomerNumber());
              dto.setStatus(customer.getStatus() != null ? customer.getStatus().toString() : null);

              return SearchResult.builder()
                  .type("customer")
                  .id(customer.getId().toString())
                  .data(dto)
                  .relevanceScore(score)
                  .matchedFields(matchedFields)
                  .build();
            })
        .sorted(Comparator.comparingInt(SearchResult::getRelevanceScore).reversed())
        .limit(limit)
        .collect(Collectors.toList());
  }

  /**
   * Searches for contacts based on query and type.
   * 
   * Uses extended search methods optimized for contact-specific fields.
   */
  private List<SearchResult> searchContacts(String query, QueryType queryType, int limit) {

    List<CustomerContact> contacts;
    List<String> matchedFields = new ArrayList<>();

    // Apply type-specific contact search strategy
    switch (queryType) {
      case EMAIL:
        // Exact email search
        contacts = contactRepository.findByEmail(query, limit);
        matchedFields.add("email");
        break;

      case PHONE:
        // Search in phone AND mobile fields with normalization
        contacts = contactRepository.findByPhoneOrMobile(query, limit);
        matchedFields.addAll(List.of("phone", "mobile"));
        break;

      case TEXT:
      case CUSTOMER_NUMBER:
      default:
        // Full text search in ALL contact fields
        contacts = contactRepository.searchContactsFullText(query, limit);
        matchedFields.addAll(
            List.of(
                "firstName",
                "lastName",
                "email",
                "phone",
                "mobile",
                "position",
                "department",
                "notes"));
        break;
    }

    // Convert to search results with contact-specific relevance scoring
    return contacts.stream()
        .map(
            contact -> {
              int score = calculateContactRelevanceScore(contact, query, queryType);
              
              // Create DTO to avoid lazy loading issues
              ContactSearchDto dto = new ContactSearchDto();
              dto.setId(contact.getId().toString());
              dto.setFirstName(contact.getFirstName());
              dto.setLastName(contact.getLastName());
              dto.setEmail(contact.getEmail());
              dto.setPhone(contact.getPhone());
              dto.setPosition(contact.getPosition());
              dto.setCustomerId(contact.getCustomer().getId().toString());
              dto.setCustomerName(contact.getCustomer().getCompanyName());
              dto.setIsPrimary(contact.getIsPrimary());

              return SearchResult.builder()
                  .type("contact")
                  .id(contact.getId().toString())
                  .data(dto)
                  .relevanceScore(score)
                  .matchedFields(matchedFields)
                  .build();
            })
        .sorted(Comparator.comparingInt(SearchResult::getRelevanceScore).reversed())
        .limit(limit)
        .collect(Collectors.toList());
  }

  /**
   * Detects the type of query based on pattern matching.
   * 
   * This intelligent detection allows for optimized search strategies
   * based on the nature of the user's query.
   */
  private QueryType detectQueryType(String query) {
    if (query == null || query.trim().isEmpty()) {
      return QueryType.TEXT;
    }

    String trimmed = query.trim();

    // Email pattern detection
    if (EMAIL_PATTERN.matcher(trimmed).matches()) {
      return QueryType.EMAIL;
    }

    // Phone pattern detection (minimum 5 digits)
    if (PHONE_PATTERN.matcher(trimmed).matches() && trimmed.length() >= 5) {
      return QueryType.PHONE;
    }

    // Customer number pattern detection (max 20 chars)
    if (CUSTOMER_NUMBER_PATTERN.matcher(trimmed).matches() && trimmed.length() <= 20) {
      return QueryType.CUSTOMER_NUMBER;
    }

    // Default to text search
    return QueryType.TEXT;
  }

  /**
   * Calculates relevance score for customer results.
   * 
   * Higher score indicates more relevant results. Factors include:
   * - Exact matches vs partial matches
   * - Customer status (active customers prioritized)
   * - Recent activity (recent contact dates boosted)
   */
  private int calculateRelevanceScore(Customer customer, String query, QueryType queryType) {
    int score = 0;
    String lowerQuery = query.toLowerCase();

    // Exact customer number match gets highest priority
    if (customer.getCustomerNumber() != null
        && customer.getCustomerNumber().equalsIgnoreCase(query)) {
      score += 100;
    }

    // Company name matching with different weights
    if (customer.getCompanyName() != null) {
      String lowerName = customer.getCompanyName().toLowerCase();
      if (lowerName.equals(lowerQuery)) {
        score += 90;  // Exact match
      } else if (lowerName.startsWith(lowerQuery)) {
        score += 70;  // Prefix match
      } else if (lowerName.contains(lowerQuery)) {
        score += 50;  // Contains match
      }
    }

    // Status bonus (active customers are more relevant)
    if (customer.getStatus() == CustomerStatus.AKTIV) {
      score += 20;
    }

    // Recent activity bonus
    if (customer.getLastContactDate() != null) {
      long daysSinceContact =
          java.time.Duration.between(customer.getLastContactDate(), LocalDateTime.now()).toDays();
      if (daysSinceContact < 30) {
        score += 10;  // Recent activity bonus
      }
    }

    return score;
  }

  /**
   * Calculates relevance score for contact results.
   * 
   * Factors include email/name matches and primary contact status.
   */
  private int calculateContactRelevanceScore(
      CustomerContact contact, String query, QueryType queryType) {
    int score = 0;
    String lowerQuery = query.toLowerCase();

    // Email exact match for email queries
    if (queryType == QueryType.EMAIL
        && contact.getEmail() != null
        && contact.getEmail().equalsIgnoreCase(query)) {
      score += 100;
    }

    // Full name matching
    String fullName = (contact.getFirstName() + " " + contact.getLastName()).toLowerCase();
    if (fullName.equals(lowerQuery)) {
      score += 90;   // Exact full name match
    } else if (fullName.startsWith(lowerQuery)) {
      score += 70;   // Name starts with query
    } else if (fullName.contains(lowerQuery)) {
      score += 50;   // Name contains query
    }

    // Primary contact bonus (primary contacts are more important)
    if (contact.getIsPrimary()) {
      score += 30;
    }

    return score;
  }
}