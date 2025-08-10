package de.freshplan.domain.search.service;

import de.freshplan.domain.customer.entity.CustomerContact;
import de.freshplan.domain.customer.repository.ContactRepository;
import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.search.service.dto.QueryType;
import de.freshplan.domain.search.service.dto.SearchResult;
import de.freshplan.domain.search.service.dto.SearchResults;
import de.freshplan.domain.search.service.dto.CustomerSearchDto;
import de.freshplan.domain.search.service.dto.ContactSearchDto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;

/**
 * Service for universal search functionality across multiple entities.
 * Provides intelligent query analysis, parallel search, and relevance scoring.
 * 
 * @since FC-005 PR4
 */
@ApplicationScoped
@Transactional
public class SearchService {

    private static final Logger LOG = Logger.getLogger(SearchService.class);

    // Regex patterns for query type detection
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^[\\d\\s+\\-()]+$"
    );
    private static final Pattern CUSTOMER_NUMBER_PATTERN = Pattern.compile(
        "^[A-Z0-9\\-]+$"
    );

    @Inject
    CustomerRepository customerRepository;

    @Inject
    ContactRepository contactRepository;

    /**
     * Performs universal search across customers and contacts.
     * 
     * @param query The search query
     * @param includeContacts Whether to include contacts
     * @param includeInactive Whether to include inactive customers
     * @param limit Maximum results per entity type
     * @return Combined search results
     */
    public SearchResults universalSearch(
            String query, 
            boolean includeContacts, 
            boolean includeInactive,
            int limit) {
        
        long startTime = System.currentTimeMillis();
        
        // Analyze query type
        QueryType queryType = detectQueryType(query);
        LOG.debugf("Query type detected: %s for query: %s", queryType, query);
        
        // Prepare search results
        List<SearchResult> customerResults = new ArrayList<>();
        List<SearchResult> contactResults = new ArrayList<>();
        
        // Search customers
        customerResults = searchCustomers(query, queryType, includeInactive, limit);
        
        // Search contacts if requested
        if (includeContacts) {
            contactResults = searchContacts(query, queryType, limit);
        }
        
        // Calculate execution time
        long executionTime = System.currentTimeMillis() - startTime;
        
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
     * Returns minimal data for performance.
     * 
     * @param query The search query
     * @param limit Maximum results
     * @return Lightweight search results
     */
    public SearchResults quickSearch(String query, int limit) {
        long startTime = System.currentTimeMillis();
        
        // Quick search only in customer names and numbers
        List<Customer> customers = customerRepository.quickSearch(query, limit);
        
        List<SearchResult> customerResults = customers.stream()
                .map(customer -> {
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

    /**
     * Searches for customers based on query and type.
     */
    private List<SearchResult> searchCustomers(
            String query, 
            QueryType queryType, 
            boolean includeInactive,
            int limit) {
        
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
            customers = customers.stream()
                    .filter(c -> c.getStatus() != CustomerStatus.INAKTIV)
                    .collect(Collectors.toList());
        }
        
        // Convert to search results with relevance scoring
        return customers.stream()
                .map(customer -> {
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

    /**
     * Searches for contacts based on query and type.
     * Uses extended search methods for hybrid solution.
     */
    private List<SearchResult> searchContacts(
            String query, 
            QueryType queryType, 
            int limit) {
        
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
                matchedFields.addAll(List.of("firstName", "lastName", "email", "phone", 
                    "mobile", "position", "department", "notes"));
                break;
        }
        
        // Convert to search results
        return contacts.stream()
                .map(contact -> {
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
     */
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

    /**
     * Calculates relevance score for customer results.
     * Higher score = more relevant.
     */
    private int calculateRelevanceScore(Customer customer, String query, QueryType queryType) {
        int score = 0;
        String lowerQuery = query.toLowerCase();
        
        // Exact match bonus
        if (customer.getCustomerNumber() != null && 
            customer.getCustomerNumber().equalsIgnoreCase(query)) {
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
            long daysSinceContact = java.time.Duration.between(
                    customer.getLastContactDate(), 
                    LocalDateTime.now()).toDays();
            if (daysSinceContact < 30) {
                score += 10;
            }
        }
        
        return score;
    }

    /**
     * Calculates relevance score for contact results.
     */
    private int calculateContactRelevanceScore(CustomerContact contact, String query, QueryType queryType) {
        int score = 0;
        String lowerQuery = query.toLowerCase();
        
        // Email exact match
        if (queryType == QueryType.EMAIL && 
            contact.getEmail() != null && 
            contact.getEmail().equalsIgnoreCase(query)) {
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
}