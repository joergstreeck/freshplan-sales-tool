package de.freshplan.domain.search.service;

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
import de.freshplan.domain.search.service.query.SearchQueryService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/**
 * CQRS Facade for universal search functionality across multiple entities.
 *
 * <p>This service acts as a facade that routes search requests to either the new CQRS-based
 * SearchQueryService or the legacy implementation, controlled by feature flag.
 *
 * <p>Since SearchService is read-only (no write operations), only a QueryService is needed.
 *
 * @since Phase 10 CQRS Migration
 */
@ApplicationScoped
@Transactional
public class SearchService {

  private static final Logger LOG = Logger.getLogger(SearchService.class);

  // Feature flag for CQRS migration
  @ConfigProperty(name = "features.cqrs.enabled", defaultValue = "false")
  boolean cqrsEnabled;

  // CQRS Services
  @Inject SearchQueryService queryService;

  // Regex patterns for query type detection
  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
  private static final Pattern PHONE_PATTERN = Pattern.compile("^[\\d\\s+\\-()]+$");
  private static final Pattern CUSTOMER_NUMBER_PATTERN = Pattern.compile("^[A-Z0-9\\-]+$");

  @Inject CustomerRepository customerRepository;

  @Inject ContactRepository contactRepository;

  /**
   * Performs universal search across customers and contacts OR leads and lead contacts.
   *
   * <p>Routes to CQRS SearchQueryService if enabled, otherwise uses legacy implementation.
   *
   * @param query The search query
   * @param includeContacts Whether to include contacts
   * @param includeInactive Whether to include inactive customers/leads
   * @param limit Maximum results per entity type
   * @param context Search context: "leads" for leads, otherwise customers (default: "customers")
   * @return Combined search results
   */
  public SearchResults universalSearch(
      String query, boolean includeContacts, boolean includeInactive, int limit, String context) {

    if (cqrsEnabled) {
      LOG.debugf("CQRS enabled - delegating universalSearch to SearchQueryService");
      // NOTE: CQRS SearchQueryService currently only supports customer search (no context routing yet)
      return queryService.universalSearch(query, includeContacts, includeInactive, limit);
    }

    // Legacy implementation (preserved for fallback)
    LOG.debugf("CQRS disabled - using legacy universalSearch implementation");
    return legacyUniversalSearch(query, includeContacts, includeInactive, limit, context);
  }

  /**
   * Quick search for autocomplete functionality. Returns minimal data for performance.
   *
   * <p>Routes to CQRS SearchQueryService if enabled, otherwise uses legacy implementation.
   *
   * @param query The search query
   * @param limit Maximum results
   * @return Lightweight search results
   */
  public SearchResults quickSearch(String query, int limit) {
    if (cqrsEnabled) {
      LOG.debugf("CQRS enabled - delegating quickSearch to SearchQueryService");
      return queryService.quickSearch(query, limit);
    }

    // Legacy implementation (preserved for fallback)
    LOG.debugf("CQRS disabled - using legacy quickSearch implementation");
    return legacyQuickSearch(query, limit);
  }

  // =================== LEGACY IMPLEMENTATIONS ===================
  // Preserved for fallback when CQRS is disabled

  /** Legacy implementation of universalSearch (preserved for fallback). */
  private SearchResults legacyUniversalSearch(
      String query, boolean includeContacts, boolean includeInactive, int limit, String context) {

    long startTime = System.currentTimeMillis();

    // Analyze query type
    QueryType queryType = detectQueryType(query);
    LOG.debugf(
        "Query type detected: %s for query: %s in context: %s", queryType, query, context);

    // Prepare search results
    List<SearchResult> entityResults = new ArrayList<>();
    List<SearchResult> contactResults = new ArrayList<>();

    // Context-based routing: "leads" → search leads, otherwise → search customers
    if ("leads".equals(context)) {
      // Search in Leads
      entityResults = searchLeads(query, queryType, includeInactive, limit);

      // Search in Lead Contacts if requested
      if (includeContacts) {
        contactResults = searchLeadContacts(query, queryType, limit);
      }
    } else {
      // Search in Customers (default)
      entityResults = searchCustomers(query, queryType, includeInactive, limit);

      // Search in Customer Contacts if requested
      if (includeContacts) {
        contactResults = searchContacts(query, queryType, limit);
      }
    }

    // Calculate execution time
    long executionTime = System.currentTimeMillis() - startTime;

    // Build and return results
    // Note: Field name stays "customers" for backward compatibility
    return SearchResults.builder()
        .customers(entityResults)
        .contacts(contactResults)
        .totalCount(entityResults.size() + contactResults.size())
        .executionTime(executionTime)
        .build();
  }

  /** Legacy implementation of quickSearch (preserved for fallback). */
  private SearchResults legacyQuickSearch(String query, int limit) {
    long startTime = System.currentTimeMillis();

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

    return SearchResults.builder()
        .customers(customerResults)
        .contacts(new ArrayList<>())
        .totalCount(customerResults.size())
        .executionTime(executionTime)
        .build();
  }

  /** Legacy helper: Searches for customers based on query and type. */
  private List<SearchResult> searchCustomers(
      String query, QueryType queryType, boolean includeInactive, int limit) {

    List<Customer> customers;
    List<String> matchedFields = new ArrayList<>();

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

    // Filter out inactive if needed
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
              // Customer entity doesn't have email/phone directly

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

  /** Legacy helper: Searches for contacts based on query and type. */
  private List<SearchResult> searchContacts(String query, QueryType queryType, int limit) {

    List<CustomerContact> contacts;
    List<String> matchedFields = new ArrayList<>();

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

    // Convert to search results
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

  /** Legacy helper: Detects the type of query based on pattern matching. */
  private QueryType detectQueryType(String query) {
    if (query == null || query.trim().isEmpty()) {
      return QueryType.TEXT;
    }

    String trimmed = query.trim();

    if (EMAIL_PATTERN.matcher(trimmed).matches()) {
      return QueryType.EMAIL;
    }

    if (PHONE_PATTERN.matcher(trimmed).matches() && trimmed.length() >= 5) {
      return QueryType.PHONE;
    }

    if (CUSTOMER_NUMBER_PATTERN.matcher(trimmed).matches() && trimmed.length() <= 20) {
      return QueryType.CUSTOMER_NUMBER;
    }

    return QueryType.TEXT;
  }

  /** Legacy helper: Calculates relevance score for customer results. */
  private int calculateRelevanceScore(Customer customer, String query, QueryType queryType) {
    int score = 0;
    String lowerQuery = query.toLowerCase();

    // Exact match bonus
    if (customer.getCustomerNumber() != null
        && customer.getCustomerNumber().equalsIgnoreCase(query)) {
      score += 100;
    }

    // Company name match
    if (customer.getCompanyName() != null) {
      String lowerName = customer.getCompanyName().toLowerCase();
      if (lowerName.equals(lowerQuery)) {
        score += 90;
      } else if (lowerName.startsWith(lowerQuery)) {
        score += 70;
      } else if (lowerName.contains(lowerQuery)) {
        score += 50;
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
        score += 10;
      }
    }

    return score;
  }

  /** Legacy helper: Calculates relevance score for contact results. */
  private int calculateContactRelevanceScore(
      CustomerContact contact, String query, QueryType queryType) {
    int score = 0;
    String lowerQuery = query.toLowerCase();

    // Email exact match
    if (queryType == QueryType.EMAIL
        && contact.getEmail() != null
        && contact.getEmail().equalsIgnoreCase(query)) {
      score += 100;
    }

    // Name matching
    String fullName = (contact.getFirstName() + " " + contact.getLastName()).toLowerCase();
    if (fullName.equals(lowerQuery)) {
      score += 90;
    } else if (fullName.startsWith(lowerQuery)) {
      score += 70;
    } else if (fullName.contains(lowerQuery)) {
      score += 50;
    }

    // Primary contact bonus
    if (contact.getIsPrimary()) {
      score += 30;
    }

    return score;
  }

  // =================== LEAD SEARCH METHODS ===================
  // New methods for Lead search (parallel to Customer search)

  /** Searches for leads based on query and type. */
  private List<SearchResult> searchLeads(
      String query, QueryType queryType, boolean includeInactive, int limit) {

    List<de.freshplan.modules.leads.domain.Lead> leads;
    List<String> matchedFields = new ArrayList<>();

    switch (queryType) {
      case EMAIL:
        // Search in lead email field (Lead has direct email field!)
        leads =
            de.freshplan.modules.leads.domain.Lead.find(
                    "lower(email) = ?1", query.toLowerCase())
                .page(0, limit)
                .list();
        matchedFields.add("email");
        break;

      case PHONE:
        // Search in phone field
        leads =
            de.freshplan.modules.leads.domain.Lead.find(
                    "phone LIKE ?1 OR phoneE164 LIKE ?1", "%" + query + "%")
                .page(0, limit)
                .list();
        matchedFields.add("phone");
        break;

      case CUSTOMER_NUMBER:
        // Leads don't have customer numbers - fall through to TEXT search
      case TEXT:
      default:
        // Full text search in multiple fields
        String searchPattern = "%" + query.toLowerCase() + "%";
        leads =
            de.freshplan.modules.leads.domain.Lead.find(
                    "lower(companyName) LIKE ?1 OR lower(contactPerson) LIKE ?1",
                    searchPattern)
                .page(0, limit)
                .list();
        matchedFields.addAll(List.of("companyName", "contactPerson"));
        break;
    }

    // Filter out inactive if needed
    if (!includeInactive) {
      leads =
          leads.stream()
              .filter(
                  l ->
                      l.status
                              != de.freshplan.modules.leads.domain.LeadStatus.EXPIRED
                          && l.status
                              != de.freshplan.modules.leads.domain.LeadStatus.DELETED
                          && l.status
                              != de.freshplan.modules.leads.domain.LeadStatus.LOST)
              .collect(Collectors.toList());
    }

    // Convert to search results with relevance scoring
    return leads.stream()
        .map(
            lead -> {
              int score = calculateLeadRelevanceScore(lead, query, queryType);
              // Create DTO to avoid lazy loading issues
              de.freshplan.domain.search.service.dto.LeadSearchDto dto =
                  new de.freshplan.domain.search.service.dto.LeadSearchDto();
              dto.setId(lead.id.toString());
              dto.setCompanyName(lead.companyName);
              dto.setStatus(lead.status != null ? lead.status.toString() : null);
              dto.setStage(lead.stage != null ? lead.stage.toString() : null);
              dto.setEmail(lead.email);
              dto.setPhone(lead.phone);
              // Contact count - lazy loaded, use contacts.size() carefully
              dto.setContactCount(lead.contacts != null ? lead.contacts.size() : 0);

              return SearchResult.builder()
                  .type("lead")
                  .id(lead.id.toString())
                  .data(dto)
                  .relevanceScore(score)
                  .matchedFields(matchedFields)
                  .build();
            })
        .sorted(Comparator.comparingInt(SearchResult::getRelevanceScore).reversed())
        .limit(limit)
        .collect(Collectors.toList());
  }

  /** Searches for lead contacts based on query and type. */
  private List<SearchResult> searchLeadContacts(String query, QueryType queryType, int limit) {

    List<de.freshplan.modules.leads.domain.LeadContact> contacts;
    List<String> matchedFields = new ArrayList<>();

    switch (queryType) {
      case EMAIL:
        // Exact email search
        contacts =
            de.freshplan.modules.leads.domain.LeadContact.find(
                    "lower(email) = ?1 AND isActive = true", query.toLowerCase())
                .page(0, limit)
                .list();
        matchedFields.add("email");
        break;

      case PHONE:
        // Search in phone AND mobile fields
        contacts =
            de.freshplan.modules.leads.domain.LeadContact.find(
                    "isActive = true AND (phone LIKE ?1 OR mobile LIKE ?1)",
                    "%" + query + "%")
                .page(0, limit)
                .list();
        matchedFields.addAll(List.of("phone", "mobile"));
        break;

      case TEXT:
      case CUSTOMER_NUMBER:
      default:
        // Full text search in ALL contact fields
        String searchPattern = "%" + query.toLowerCase() + "%";
        contacts =
            de.freshplan.modules.leads.domain.LeadContact.find(
                    """
                    isActive = true AND (
                        lower(firstName) LIKE ?1 OR
                        lower(lastName) LIKE ?1 OR
                        lower(email) LIKE ?1 OR
                        lower(phone) LIKE ?1 OR
                        lower(mobile) LIKE ?1 OR
                        lower(position) LIKE ?1
                    )
                    """,
                    searchPattern)
                .page(0, limit)
                .list();
        matchedFields.addAll(
            List.of("firstName", "lastName", "email", "phone", "mobile", "position"));
        break;
    }

    // Convert to search results
    return contacts.stream()
        .map(
            contact -> {
              int score = calculateLeadContactRelevanceScore(contact, query, queryType);
              // Create DTO to avoid lazy loading issues
              de.freshplan.domain.search.service.dto.LeadContactSearchDto dto =
                  new de.freshplan.domain.search.service.dto.LeadContactSearchDto();
              dto.setId(contact.getId().toString());
              dto.setFirstName(contact.getFirstName());
              dto.setLastName(contact.getLastName());
              dto.setEmail(contact.getEmail());
              dto.setPhone(contact.getPhone());
              dto.setPosition(contact.getPosition());
              dto.setLeadId(contact.getLead().id.toString());
              dto.setCustomerId(contact.getLead().id.toString()); // Alias for frontend compatibility
              dto.setLeadName(contact.getLead().companyName);
              dto.setIsPrimary(contact.isPrimary());

              return SearchResult.builder()
                  .type("lead_contact")
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

  /** Calculates relevance score for lead results. */
  private int calculateLeadRelevanceScore(
      de.freshplan.modules.leads.domain.Lead lead, String query, QueryType queryType) {
    int score = 0;
    String lowerQuery = query.toLowerCase();

    // Email exact match (Lead has direct email field)
    if (lead.email != null && lead.email.equalsIgnoreCase(query)) {
      score += 100;
    }

    // Company name match
    if (lead.companyName != null) {
      String lowerName = lead.companyName.toLowerCase();
      if (lowerName.equals(lowerQuery)) {
        score += 90;
      } else if (lowerName.startsWith(lowerQuery)) {
        score += 70;
      } else if (lowerName.contains(lowerQuery)) {
        score += 50;
      }
    }

    // Status bonus (active/qualified leads are more relevant)
    if (lead.status == de.freshplan.modules.leads.domain.LeadStatus.QUALIFIED) {
      score += 25;
    } else if (lead.status == de.freshplan.modules.leads.domain.LeadStatus.ACTIVE) {
      score += 20;
    }

    // Stage bonus (higher stages are more qualified)
    if (lead.stage == de.freshplan.modules.leads.domain.LeadStage.QUALIFIZIERT) {
      score += 15;
    } else if (lead.stage == de.freshplan.modules.leads.domain.LeadStage.REGISTRIERUNG) {
      score += 10;
    }

    // Recent activity bonus
    if (lead.lastActivityAt != null) {
      long daysSinceActivity =
          java.time.Duration.between(lead.lastActivityAt, LocalDateTime.now()).toDays();
      if (daysSinceActivity < 30) {
        score += 10;
      }
    }

    return score;
  }

  /** Calculates relevance score for lead contact results. */
  private int calculateLeadContactRelevanceScore(
      de.freshplan.modules.leads.domain.LeadContact contact, String query, QueryType queryType) {
    int score = 0;
    String lowerQuery = query.toLowerCase();

    // Email exact match
    if (queryType == QueryType.EMAIL
        && contact.getEmail() != null
        && contact.getEmail().equalsIgnoreCase(query)) {
      score += 100;
    }

    // Name matching
    String fullName =
        (contact.getFirstName() + " " + contact.getLastName()).toLowerCase();
    if (fullName.equals(lowerQuery)) {
      score += 90;
    } else if (fullName.startsWith(lowerQuery)) {
      score += 70;
    } else if (fullName.contains(lowerQuery)) {
      score += 50;
    }

    // Primary contact bonus
    if (contact.isPrimary()) {
      score += 30;
    }

    return score;
  }
}
