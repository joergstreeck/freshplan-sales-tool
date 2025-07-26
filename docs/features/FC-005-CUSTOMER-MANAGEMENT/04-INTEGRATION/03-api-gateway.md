# 🌐 FC-005 INTEGRATION - API GATEWAY PATTERN

**Navigation:**
- **Parent:** [04-INTEGRATION](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/04-INTEGRATION/README.md)
- **Prev:** [02-event-system.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/04-INTEGRATION/02-event-system.md)
- **Next:** [05-TESTING](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/05-TESTING/README.md)

---

## Customer Aggregate API

### Aggregate Resource

```java
@Path("/api/customer-aggregate")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"admin", "manager", "sales"})
public class CustomerAggregateResource {
    
    @Inject
    CustomerService customerService;
    
    @Inject
    OpportunityService opportunityService;
    
    @Inject
    ContractService contractService;
    
    @Inject
    ActivityService activityService;
    
    @Inject
    CustomerSecurityService securityService;
    
    @GET
    @Path("/{customerId}/complete")
    public CustomerAggregateResponse getCompleteCustomer(
        @PathParam("customerId") UUID customerId
    ) {
        // Security check
        if (!securityService.canViewCustomer(customerId)) {
            throw new ForbiddenException("Access denied to customer");
        }
        
        // Parallel data fetching for performance
        CompletableFuture<Customer> customerFuture = 
            CompletableFuture.supplyAsync(() -> 
                customerService.getCustomer(customerId)
            );
            
        CompletableFuture<List<Opportunity>> opportunitiesFuture = 
            CompletableFuture.supplyAsync(() -> 
                opportunityService.findByCustomer(customerId)
            );
            
        CompletableFuture<List<Contract>> contractsFuture = 
            CompletableFuture.supplyAsync(() -> 
                contractService.findByCustomer(customerId)
            );
            
        CompletableFuture<List<Activity>> activitiesFuture = 
            CompletableFuture.supplyAsync(() -> 
                activityService.findRecentByCustomer(customerId, 10)
            );
        
        // Wait for all futures to complete
        CompletableFuture.allOf(
            customerFuture, 
            opportunitiesFuture, 
            contractsFuture, 
            activitiesFuture
        ).join();
        
        // Build aggregate response
        return CustomerAggregateResponse.builder()
            .customer(customerFuture.join())
            .opportunities(opportunitiesFuture.join())
            .contracts(contractsFuture.join())
            .recentActivities(activitiesFuture.join())
            .statistics(calculateStatistics(
                customerFuture.join(),
                opportunitiesFuture.join(),
                contractsFuture.join()
            ))
            .permissions(securityService.getCustomerPermissions(customerId))
            .build();
    }
    
    @GET
    @Path("/{customerId}/summary")
    public CustomerSummaryResponse getCustomerSummary(
        @PathParam("customerId") UUID customerId
    ) {
        if (!securityService.canViewCustomer(customerId)) {
            throw new ForbiddenException("Access denied to customer");
        }
        
        Customer customer = customerService.getCustomer(customerId);
        
        // Lightweight summary for list views
        return CustomerSummaryResponse.builder()
            .id(customer.getId())
            .companyName(customer.getFieldValue("companyName"))
            .industry(customer.getFieldValue("industry"))
            .status(customer.getStatus())
            .activeOpportunities(opportunityService.countActiveByCustomer(customerId))
            .activeContracts(contractService.countActiveByCustomer(customerId))
            .lastActivity(activityService.getLastActivityDate(customerId))
            .build();
    }
    
    private CustomerStatistics calculateStatistics(
        Customer customer,
        List<Opportunity> opportunities,
        List<Contract> contracts
    ) {
        return CustomerStatistics.builder()
            .totalOpportunityValue(
                opportunities.stream()
                    .mapToDouble(Opportunity::getValue)
                    .sum()
            )
            .activeOpportunities(
                opportunities.stream()
                    .filter(o -> o.getStatus() == OpportunityStatus.ACTIVE)
                    .count()
            )
            .activeContracts(
                contracts.stream()
                    .filter(c -> c.getStatus() == ContractStatus.ACTIVE)
                    .count()
            )
            .totalContractValue(
                contracts.stream()
                    .filter(c -> c.getStatus() == ContractStatus.ACTIVE)
                    .mapToDouble(Contract::getMonthlyValue)
                    .sum() * 12 // Annual value
            )
            .customerSince(customer.getCreatedAt())
            .build();
    }
}
```

### Response DTOs

```java
// CustomerAggregateResponse.java
@Data
@Builder
public class CustomerAggregateResponse {
    private Customer customer;
    private List<Opportunity> opportunities;
    private List<Contract> contracts;
    private List<Activity> recentActivities;
    private CustomerStatistics statistics;
    private CustomerPermissions permissions;
}

// CustomerStatistics.java
@Data
@Builder
public class CustomerStatistics {
    private double totalOpportunityValue;
    private long activeOpportunities;
    private long activeContracts;
    private double totalContractValue;
    private LocalDateTime customerSince;
}

// CustomerSummaryResponse.java
@Data
@Builder
public class CustomerSummaryResponse {
    private UUID id;
    private String companyName;
    private String industry;
    private CustomerStatus status;
    private long activeOpportunities;
    private long activeContracts;
    private LocalDateTime lastActivity;
}
```

---

## Frontend Integration

### Aggregate Hook

```typescript
// hooks/useCustomerAggregate.ts
export const useCustomerAggregate = (customerId: string) => {
  return useQuery({
    queryKey: ['customerAggregate', customerId],
    queryFn: () => customerApi.getCustomerAggregate(customerId),
    staleTime: 30 * 1000, // 30 seconds
    cacheTime: 5 * 60 * 1000, // 5 minutes
    select: (data) => ({
      ...data,
      // Computed values
      totalOpportunityValue: data.opportunities.reduce(
        (sum, opp) => sum + opp.value, 
        0
      ),
      activeContracts: data.contracts.filter(
        c => c.status === 'ACTIVE'
      ).length,
      lastActivity: data.recentActivities[0]?.timestamp,
      // Grouping for UI
      opportunitiesByStage: groupBy(data.opportunities, 'stage'),
      contractsByStatus: groupBy(data.contracts, 'status')
    })
  });
};

// hooks/useCustomerSummaries.ts
export const useCustomerSummaries = (customerIds: string[]) => {
  return useQueries({
    queries: customerIds.map(id => ({
      queryKey: ['customerSummary', id],
      queryFn: () => customerApi.getCustomerSummary(id),
      staleTime: 60 * 1000, // 1 minute
    }))
  });
};
```

### Aggregate Components

```typescript
// CustomerDashboard.tsx
export const CustomerDashboard: React.FC<{ customerId: string }> = ({ 
  customerId 
}) => {
  const { data, isLoading, error } = useCustomerAggregate(customerId);
  
  if (isLoading) return <CustomerDashboardSkeleton />;
  if (error) return <ErrorMessage error={error} />;
  if (!data) return null;
  
  return (
    <Grid container spacing={3}>
      {/* Customer Header */}
      <Grid item xs={12}>
        <CustomerHeader 
          customer={data.customer} 
          statistics={data.statistics}
        />
      </Grid>
      
      {/* Key Metrics */}
      <Grid item xs={12} md={3}>
        <MetricCard
          title="Opportunities"
          value={data.statistics.activeOpportunities}
          total={data.opportunities.length}
          icon={<TrendingUpIcon />}
        />
      </Grid>
      
      <Grid item xs={12} md={3}>
        <MetricCard
          title="Contracts"
          value={data.statistics.activeContracts}
          total={data.contracts.length}
          icon={<DescriptionIcon />}
        />
      </Grid>
      
      <Grid item xs={12} md={3}>
        <MetricCard
          title="Annual Value"
          value={formatCurrency(data.statistics.totalContractValue)}
          icon={<EuroIcon />}
        />
      </Grid>
      
      <Grid item xs={12} md={3}>
        <MetricCard
          title="Customer Since"
          value={formatDate(data.statistics.customerSince)}
          icon={<DateRangeIcon />}
        />
      </Grid>
      
      {/* Opportunities Section */}
      <Grid item xs={12} md={6}>
        <Card>
          <CardHeader title="Active Opportunities" />
          <CardContent>
            <OpportunityList 
              opportunities={data.opportunities}
              customerId={customerId}
            />
          </CardContent>
        </Card>
      </Grid>
      
      {/* Recent Activities */}
      <Grid item xs={12} md={6}>
        <Card>
          <CardHeader title="Recent Activities" />
          <CardContent>
            <ActivityTimeline 
              activities={data.recentActivities}
            />
          </CardContent>
        </Card>
      </Grid>
    </Grid>
  );
};
```

### Performance Optimizations

```typescript
// Prefetching strategy
export const usePrefetchCustomerAggregate = () => {
  const queryClient = useQueryClient();
  
  return useCallback((customerId: string) => {
    queryClient.prefetchQuery({
      queryKey: ['customerAggregate', customerId],
      queryFn: () => customerApi.getCustomerAggregate(customerId),
      staleTime: 30 * 1000
    });
  }, [queryClient]);
};

// Usage in list view
export const CustomerList: React.FC = () => {
  const prefetchAggregate = usePrefetchCustomerAggregate();
  
  return (
    <List>
      {customers.map(customer => (
        <ListItem
          key={customer.id}
          onMouseEnter={() => prefetchAggregate(customer.id)}
          onClick={() => navigate(`/customers/${customer.id}`)}
        >
          <CustomerSummaryCard customer={customer} />
        </ListItem>
      ))}
    </List>
  );
};
```

---

## Caching Strategy

### Backend Caching

```java
@ApplicationScoped
public class CustomerAggregateCacheService {
    
    @Inject
    @CacheResult(cacheName = "customer-aggregate")
    Cache cache;
    
    @CacheResult(cacheName = "customer-aggregate")
    public CustomerAggregateResponse getFromCache(UUID customerId) {
        // Will be cached automatically by Quarkus
        return buildAggregateResponse(customerId);
    }
    
    @CacheInvalidate(cacheName = "customer-aggregate")
    public void invalidate(UUID customerId) {
        // Called when customer data changes
    }
    
    // Invalidate on events
    public void onCustomerUpdated(@Observes CustomerFieldUpdatedEvent event) {
        invalidate(event.getCustomerId());
    }
    
    public void onOpportunityUpdated(@Observes OpportunityUpdatedEvent event) {
        invalidate(event.getCustomerId());
    }
}
```

### Frontend Caching

```typescript
// Cache configuration
export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30 * 1000, // 30 seconds
      cacheTime: 5 * 60 * 1000, // 5 minutes
      refetchOnWindowFocus: false,
      retry: 2
    }
  }
});

// Optimistic updates
export const useUpdateCustomerField = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: customerApi.updateField,
    onMutate: async ({ customerId, fieldKey, value }) => {
      // Cancel queries
      await queryClient.cancelQueries(['customerAggregate', customerId]);
      
      // Snapshot previous value
      const previous = queryClient.getQueryData(['customerAggregate', customerId]);
      
      // Optimistically update
      queryClient.setQueryData(['customerAggregate', customerId], old => ({
        ...old,
        customer: {
          ...old.customer,
          fieldValues: {
            ...old.customer.fieldValues,
            [fieldKey]: value
          }
        }
      }));
      
      return { previous };
    },
    onError: (err, variables, context) => {
      // Rollback on error
      queryClient.setQueryData(
        ['customerAggregate', variables.customerId], 
        context.previous
      );
    },
    onSettled: (data, error, variables) => {
      // Refetch to ensure consistency
      queryClient.invalidateQueries(['customerAggregate', variables.customerId]);
    }
  });
};
```

---

## Best Practices

1. **Parallel Fetching**: CompletableFuture für parallele Backend-Calls
2. **Selective Loading**: Nur benötigte Daten laden (summary vs. complete)
3. **Cache Invalidation**: Event-basierte Cache-Invalidierung
4. **Optimistic Updates**: Für bessere UX bei Änderungen
5. **Prefetching**: Bei hover/focus für schnellere Navigation