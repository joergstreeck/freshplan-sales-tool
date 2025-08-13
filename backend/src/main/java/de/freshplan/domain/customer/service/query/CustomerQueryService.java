package de.freshplan.domain.customer.service.query;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerLifecycleStage;
import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.domain.customer.entity.Industry;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.customer.service.dto.CustomerDashboardResponse;
import de.freshplan.domain.customer.service.dto.CustomerListResponse;
import de.freshplan.domain.customer.service.dto.CustomerResponse;
import de.freshplan.domain.customer.service.dto.CustomerResponseBuilder;
import de.freshplan.domain.customer.service.exception.CustomerNotFoundException;
import de.freshplan.domain.customer.service.mapper.CustomerMapper;
import io.quarkus.panache.common.Page;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Query Service for Customer domain following CQRS pattern.
 * Handles all read operations (queries).
 * 
 * This service works in parallel with the existing CustomerService
 * and can be activated via feature flag: features.cqrs.enabled
 * 
 * IMPORTANT: This service maintains EXACT compatibility with CustomerService
 * to ensure seamless switching via feature flag.
 * 
 * NO WRITE OPERATIONS - This is a READ-ONLY service!
 */
@ApplicationScoped
public class CustomerQueryService {
    
    private static final Logger log = LoggerFactory.getLogger(CustomerQueryService.class);
    
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    
    @Inject
    public CustomerQueryService(
        CustomerRepository customerRepository,
        CustomerMapper customerMapper
    ) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
    }
    
    /**
     * Retrieves a customer by ID.
     * EXACT COPY from CustomerService.getCustomer() - line 129
     *
     * @param id The customer ID
     * @return The customer response
     * @throws CustomerNotFoundException if customer not found
     */
    public CustomerResponse getCustomer(UUID id) {
        log.debug("Fetching customer with ID: {}", id);
        
        // Null validation - exact copy from CustomerService
        if (id == null) {
            throw new IllegalArgumentException("Customer ID cannot be null");
        }
        
        Customer customer = customerRepository
            .findByIdActive(id)
            .orElseThrow(() -> {
                log.error("Customer not found with ID: {}", id);
                return new CustomerNotFoundException(id);
            });
        
        log.debug("Customer found: {} ({})", customer.getCompanyName(), customer.getCustomerNumber());
        return customerMapper.toResponse(customer);
    }
    
    /**
     * Gets all customers with pagination.
     * EXACT COPY from CustomerService.getAllCustomers() - line 309
     */
    public CustomerListResponse getAllCustomers(int page, int size) {
        log.debug("Fetching all customers - page: {}, size: {}", page, size);
        
        Page pageRequest = Page.of(page, size);
        List<Customer> customers = customerRepository.findAllActive(pageRequest);
        long totalElements = customerRepository.countActive();
        
        log.debug(
            "Found {} customers (page {} of {})",
            customers.size(),
            page,
            (totalElements + size - 1) / size);
        
        return customerMapper.toMinimalListResponse(customers, page, size, totalElements);
    }
    
    /**
     * Filters customers by status.
     * EXACT COPY from CustomerService.getCustomersByStatus() - line 329
     */
    public CustomerListResponse getCustomersByStatus(CustomerStatus status, int page, int size) {
        log.debug("Fetching customers by status - status: {}, page: {}, size: {}", status, page, size);
        
        Page pageRequest = Page.of(page, size);
        List<Customer> customers = customerRepository.findByStatus(status, pageRequest);
        long totalElements = customerRepository.countByStatus(status);
        
        List<CustomerResponse> customerResponses = 
            customers.stream().map(this::mapToResponse).collect(Collectors.toList());
        
        log.debug("Found {} customers with status {}", totalElements, status);
        
        return CustomerListResponse.of(customerResponses, page, size, totalElements);
    }
    
    /**
     * Filters customers by industry.
     * EXACT COPY from CustomerService.getCustomersByIndustry() - line 345
     */
    public CustomerListResponse getCustomersByIndustry(Industry industry, int page, int size) {
        Page pageRequest = Page.of(page, size);
        List<Customer> customers = customerRepository.findByIndustry(industry, pageRequest);
        
        // Count manually since we don't have countByIndustry method
        long totalElements = customers.size();
        
        List<CustomerResponse> customerResponses = 
            customers.stream().map(this::mapToResponse).collect(Collectors.toList());
        
        return CustomerListResponse.of(customerResponses, page, size, totalElements);
    }
    
    /**
     * Gets the hierarchy tree for a customer.
     * EXACT COPY from CustomerService.getCustomerHierarchy() - line 361
     */
    public CustomerResponse getCustomerHierarchy(UUID customerId) {
        Customer customer = customerRepository
            .findByIdActive(customerId)
            .orElseThrow(() -> new CustomerNotFoundException(customerId));
        
        return mapToResponseWithHierarchy(customer);
    }
    
    /**
     * Gets customers at risk.
     * EXACT COPY from CustomerService.getCustomersAtRisk() - line 417
     */
    public CustomerListResponse getCustomersAtRisk(int minRiskScore, int page, int size) {
        Page pageRequest = Page.of(page, size);
        List<Customer> customers = customerRepository.findAtRisk(minRiskScore, pageRequest);
        long totalElements = customerRepository.countAtRisk(minRiskScore);
        
        List<CustomerResponse> customerResponses = 
            customers.stream().map(this::mapToResponse).collect(Collectors.toList());
        
        return CustomerListResponse.of(customerResponses, page, size, totalElements);
    }
    
    /**
     * Gets customers with overdue follow-ups.
     * EXACT COPY from CustomerService.getOverdueFollowUps() - line 429
     */
    public CustomerListResponse getOverdueFollowUps(int page, int size) {
        Page pageRequest = Page.of(page, size);
        List<Customer> customers = customerRepository.findOverdueFollowUps(pageRequest);
        long totalElements = customerRepository.countOverdueFollowUps();
        
        List<CustomerResponse> customerResponses = 
            customers.stream().map(this::mapToResponse).collect(Collectors.toList());
        
        return CustomerListResponse.of(customerResponses, page, size, totalElements);
    }
    
    /**
     * Checks for potential duplicates.
     * EXACT COPY from CustomerService.checkDuplicates() - line 476
     */
    public List<CustomerResponse> checkDuplicates(String companyName) {
        List<Customer> duplicates = customerRepository.findPotentialDuplicates(companyName);
        
        return duplicates.stream().map(this::mapToResponse).collect(Collectors.toList());
    }
    
    /**
     * Gets dashboard statistics.
     * EXACT COPY from CustomerService.getDashboardData() - line 580
     */
    public CustomerDashboardResponse getDashboardData() {
        // Basic counts
        long totalCustomers = customerRepository.countActive();
        long activeCustomers = customerRepository.countByStatus(CustomerStatus.AKTIV);
        long newThisMonth = customerRepository.countNewThisMonth();
        long atRiskCount = customerRepository.countAtRisk(70); // Risk score >= 70
        long upcomingFollowUps = customerRepository.countOverdueFollowUps();
        
        // Status distribution
        var customersByStatus = new java.util.HashMap<CustomerStatus, Long>();
        for (CustomerStatus status : CustomerStatus.values()) {
            customersByStatus.put(status, customerRepository.countByStatus(status));
        }
        
        // Lifecycle distribution
        var customersByLifecycle = new java.util.HashMap<CustomerLifecycleStage, Long>();
        for (CustomerLifecycleStage stage : CustomerLifecycleStage.values()) {
            customersByLifecycle.put(stage, customerRepository.countByLifecycleStage(stage));
        }
        
        return new CustomerDashboardResponse(
            totalCustomers,
            activeCustomers,
            newThisMonth,
            atRiskCount,
            upcomingFollowUps,
            customersByStatus,
            customersByLifecycle
        );
    }
    
    // ========== PRIVATE HELPER METHODS ==========
    // EXACT COPIES from CustomerService
    
    private CustomerResponse mapToResponse(Customer customer) {
        return CustomerResponseBuilder.builder().fromEntity(customer).build();
    }
    
    private CustomerResponse mapToResponseWithHierarchy(Customer customer) {
        // For hierarchy view, load children recursively
        // This is simplified - in production you'd want to optimize this
        return mapToResponse(customer);
    }
}