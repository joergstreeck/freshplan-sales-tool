# 🔧 FC-005 BACKEND - REST API

**Navigation:** [← Services](02-services.md) | [Database →](04-database.md)

---

**Datum:** 26.07.2025  
**Version:** 1.0  
**Status:** 🔄 In Entwicklung  

## 📋 Inhaltsverzeichnis

1. [Customer Resource](#customer-resource)
2. [Field Definition Resource](#field-definition-resource)
3. [Location Resource](#location-resource)
4. [API Models](#api-models)

## Customer Resource

### Endpoints für Kundenverwaltung

```java
@Path("/api/customers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class CustomerResource {
    
    @Inject
    CustomerService customerService;
    
    @POST
    @Path("/draft")
    @RolesAllowed({"admin", "manager", "sales"})
    public Response createDraft(
        @Valid CreateCustomerDraftRequest request
    ) {
        CustomerDraftResponse response = 
            customerService.createDraft(request);
        return Response.ok(response).build();
    }
    
    @PUT
    @Path("/draft/{id}")
    @RolesAllowed({"admin", "manager", "sales"})
    public Response updateDraft(
        @PathParam("id") UUID id,
        @Valid UpdateCustomerDraftRequest request
    ) {
        CustomerDraftResponse response = 
            customerService.updateDraft(id, request);
        return Response.ok(response).build();
    }
    
    @POST
    @Path("/draft/{id}/finalize")
    @RolesAllowed({"admin", "manager"})
    public Response finalizeDraft(@PathParam("id") UUID id) {
        CustomerResponse response = 
            customerService.finalizeDraft(id);
        return Response.ok(response).build();
    }
    
    @GET
    @Path("/{id}/field-values")
    @RolesAllowed({"admin", "manager", "sales"})
    public Response getFieldValues(@PathParam("id") UUID id) {
        Map<String, Object> values = 
            fieldValueService.getFieldValuesAsMap(
                id, 
                EntityType.CUSTOMER
            );
        return Response.ok(values).build();
    }
    
    @PUT
    @Path("/{id}/field-values")
    @RolesAllowed({"admin", "manager", "sales"})
    public Response updateFieldValues(
        @PathParam("id") UUID id,
        @Valid List<FieldValueRequest> updates
    ) {
        customerService.updateFieldValues(id, updates);
        return Response.noContent().build();
    }
    
    @GET
    @RolesAllowed({"admin", "manager", "sales"})
    public Response listCustomers(
        @QueryParam("status") CustomerStatus status,
        @QueryParam("page") @DefaultValue("0") int page,
        @QueryParam("size") @DefaultValue("20") int size
    ) {
        Page<CustomerListResponse> customers = 
            customerService.listCustomers(status, page, size);
        return Response.ok(customers).build();
    }
}
```

## Field Definition Resource

### Endpoints für Field Definitions

```java
@Path("/api/field-definitions")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public class FieldDefinitionResource {
    
    @Inject
    FieldDefinitionService service;
    
    @GET
    @Path("/{entityType}")
    @RolesAllowed({"admin", "manager", "sales"})
    public List<FieldDefinitionResponse> getFieldDefinitions(
        @PathParam("entityType") EntityType entityType,
        @QueryParam("industry") String industry,
        @QueryParam("includeCustom") @DefaultValue("false") boolean includeCustom
    ) {
        return service.getFieldDefinitions(
            entityType, 
            industry, 
            includeCustom
        );
    }
    
    @POST
    @RolesAllowed({"admin"})
    public Response createCustomField(
        @Valid CreateFieldDefinitionRequest request
    ) {
        FieldDefinition definition = 
            service.createCustomField(request);
        return Response.ok(definition).build();
    }
    
    @PUT
    @Path("/{key}")
    @RolesAllowed({"admin"})
    public Response updateFieldDefinition(
        @PathParam("key") String key,
        @Valid UpdateFieldDefinitionRequest request
    ) {
        FieldDefinition definition = 
            service.updateFieldDefinition(key, request);
        return Response.ok(definition).build();
    }
    
    @DELETE
    @Path("/{key}")
    @RolesAllowed({"admin"})
    public Response deactivateFieldDefinition(
        @PathParam("key") String key
    ) {
        service.deactivateFieldDefinition(key);
        return Response.noContent().build();
    }
}
```

## Location Resource

### Endpoints für Standortverwaltung

```java
@Path("/api/customers/{customerId}/locations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class LocationResource {
    
    @Inject
    LocationService locationService;
    
    @POST
    @RolesAllowed({"admin", "manager", "sales"})
    public Response createLocation(
        @PathParam("customerId") UUID customerId,
        @Valid CreateLocationRequest request
    ) {
        LocationResponse response = 
            locationService.createLocation(customerId, request);
        return Response.ok(response).build();
    }
    
    @GET
    @RolesAllowed({"admin", "manager", "sales"})
    public Response getLocations(
        @PathParam("customerId") UUID customerId
    ) {
        List<LocationResponse> locations = 
            locationService.getCustomerLocations(customerId);
        return Response.ok(locations).build();
    }
    
    @PUT
    @Path("/{locationId}")
    @RolesAllowed({"admin", "manager", "sales"})
    public Response updateLocation(
        @PathParam("customerId") UUID customerId,
        @PathParam("locationId") UUID locationId,
        @Valid UpdateLocationRequest request
    ) {
        LocationResponse response = 
            locationService.updateLocation(locationId, request);
        return Response.ok(response).build();
    }
}
```

## API Models

### Request DTOs

```java
// Customer Draft Request
public class CreateCustomerDraftRequest {
    @NotNull
    private List<FieldValueRequest> fieldValues;
    
    // Constructor, getters, setters
}

// Field Value Request
public class FieldValueRequest {
    @NotBlank
    private String fieldKey;
    
    private Object value;
    
    // Constructor, getters, setters
}

// Field Definition Request
public class CreateFieldDefinitionRequest {
    @NotBlank
    private String key;
    
    @NotBlank
    private String label;
    
    @NotNull
    private EntityType entityType;
    
    @NotBlank
    private String fieldType;
    
    private JsonNode validation;
    
    private Set<String> industries;
    
    // Constructor, getters, setters
}
```

### Response DTOs

```java
// Customer Response
public class CustomerResponse {
    private UUID id;
    private CustomerStatus status;
    private Map<String, Object> fieldValues;
    private List<LocationSummary> locations;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructor, getters, setters
}

// Field Definition Response
public class FieldDefinitionResponse {
    private String key;
    private String label;
    private String fieldType;
    private JsonNode validation;
    private Object defaultValue;
    private boolean required;
    private boolean custom;
    private Integer sortOrder;
    
    // Constructor, getters, setters
}
```

### Error Responses

```java
// Standard Error Response
public class ErrorResponse {
    private String error;
    private String message;
    private Map<String, Object> details;
    private LocalDateTime timestamp;
    
    // Constructor, getters, setters
}

// Validation Error Response
public class ValidationErrorResponse extends ErrorResponse {
    private Map<String, List<String>> fieldErrors;
    
    // Constructor, getters, setters
}
```

---

**Parent:** [Backend Architecture](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/02-BACKEND/README.md)  
**Related:** [Services](02-services.md) | [Database](04-database.md) | [Testing](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/05-TESTING/README.md)