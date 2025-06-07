# 6. Calculator API Design

Date: 2025-01-07

## Status

Accepted

## Context

For Sprint 2, we need to implement the FreshPlan calculator as a REST API. The calculator is a core feature that calculates B2B wholesale food discounts based on order value, lead time, and pickup options.

We need to design an API that:
- Serves the immediate MVP needs
- Is future-proof for planned enhancements
- Follows REST best practices
- Supports future integration with ERP systems

## Decision

We have decided to implement a versioned REST API with the following design:

### Endpoint Structure
- Base path: `/api/v1/calculator`
- Main endpoint: `POST /api/v1/calculator/discount`

### API Versioning
- Version in URL path (`/v1/`) for clear versioning
- Allows parallel operation of multiple API versions
- Enables gradual migration of clients

### Request/Response Design
```json
// Request
{
  "orderValueNet": 8000.00,
  "leadTimeDays": 15,
  "selfPickup": true
}

// Response
{
  "orderValueNet": 8000.00,
  "leadTimeDays": 15,
  "selfPickup": true,
  "volumeDiscountTier": "TIER_1",
  "volumeDiscountRate": 0.03,
  "volumeDiscountAmount": 240.00,
  "leadTimeDiscountCategory": "MEDIUM_LEAD",
  "leadTimeDiscountRate": 0.02,
  "leadTimeDiscountAmount": 160.00,
  "selfPickupDiscountRate": 0.02,
  "selfPickupDiscountAmount": 160.00,
  "totalDiscountRate": 0.07,
  "totalDiscountAmount": 560.00,
  "finalNetValue": 7440.00,
  "calculatedAt": "2025-01-07T10:30:00Z"
}
```

### Implementation Choices
1. **Minimal MVP Approach**: Only implement fields needed now
2. **Immutable DTOs**: Using Jackson `@JsonCreator` for proper deserialization
3. **Comprehensive Response**: Include all calculation details for transparency
4. **Metadata Fields**: Add `calculatedAt` for audit/debugging

### Future Extensibility
The design allows for future additions without breaking changes:
- Optional ERP fields (e.g., `erpCustomerId`, `erpOrderId`)
- Event emission hooks for domain events
- Additional discount types
- Multi-tenant support via headers

## Consequences

### Positive
- Clean, RESTful API design
- Clear versioning strategy
- Easy to test and document
- Ready for future enhancements
- Follows team standards (immutable DTOs, proper validation)

### Negative
- More verbose than a simple calculation endpoint
- Requires proper DTO mapping layer
- Version in URL means client updates for major changes

### Neutral
- Standard REST approach (familiar to developers)
- POST for calculations (follows REST semantics for non-idempotent operations)

## References
- VISION_AND_ROADMAP.md - Future architecture plans
- ChatGPT recommendation for minimal MVP implementation
- REST API best practices
- FreshPlan domain documentation (AGBs, business rules)