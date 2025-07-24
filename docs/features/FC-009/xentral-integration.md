# FC-009: Xentral Integration für Contract Renewal

**Parent:** [/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-24_TECH_CONCEPT_FC-009-contract-renewal-management.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-24_TECH_CONCEPT_FC-009-contract-renewal-management.md)  
**Basis:** [/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-xentral-integration.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-xentral-integration.md)

## Event Payloads

### Contract Status Changed Event

```json
{
  "eventType": "contract.status.changed",
  "eventId": "evt_2025_07_24_12345",
  "timestamp": "2025-07-24T15:30:00Z",
  "data": {
    "customerId": "C-47236",
    "xentralCustomerId": "xentral_12345",
    "contractId": "FP-DE-2025-001122",
    "oldStatus": "ACTIVE",
    "newStatus": "EXPIRED",
    "effectiveDate": "2025-07-31",
    "triggeredBy": "system_scheduler",
    "metadata": {
      "daysOverdue": 0,
      "lastOrderDate": "2025-07-15",
      "totalContractValue": 125000.00
    }
  }
}
```

### Price Index Threshold Exceeded

```json
{
  "eventType": "price.index.threshold.exceeded",
  "eventId": "evt_2025_07_24_12346",
  "timestamp": "2025-07-24T08:00:00Z",
  "data": {
    "indexType": "VPI_Nahrungsmittel_alkoholfrei",
    "source": "destatis.de",
    "baseline": {
      "value": 100.0,
      "date": "2024-08-01"
    },
    "current": {
      "value": 106.2,
      "date": "2025-07-24"
    },
    "change": {
      "absolute": 6.2,
      "percentage": 6.2,
      "thresholdExceeded": true,
      "allowedPriceIncrease": 1.2
    },
    "affectedCustomers": [
      {
        "xentralId": "xentral_12345",
        "contractId": "FP-DE-2025-001122",
        "currentDiscount": 8.0
      }
    ]
  }
}
```

### Contract Renewal Completed

```json
{
  "eventType": "contract.renewal.completed",
  "eventId": "evt_2025_07_24_12347",
  "timestamp": "2025-07-24T16:45:00Z",
  "data": {
    "customerId": "C-47236",
    "xentralCustomerId": "xentral_12345",
    "oldContract": {
      "contractId": "FP-DE-2025-001122",
      "validUntil": "2025-07-31"
    },
    "newContract": {
      "contractId": "FP-DE-2025-001123",
      "validFrom": "2025-08-01",
      "validUntil": "2026-07-31",
      "renewalType": "standard"
    },
    "salesRep": "vertrieb.mueller",
    "documentUrl": "s3://contracts/FP-DE-2025-001123.pdf"
  }
}
```

## API Endpoints

### Contract Status Update

```typescript
// PUT /api/v1/customers/{xentralId}/contract
interface UpdateContractRequest {
  contractStatus: 'active' | 'expired' | 'renewed' | 'lapsed_renewed';
  freshplanPartner: boolean;
  validFrom: string; // ISO date
  validUntil: string; // ISO date
  discountEligible: boolean;
  renewalType?: 'standard' | 'lapsed';
  gapPeriod?: {
    from: string;
    to: string;
  };
}

interface UpdateContractResponse {
  success: boolean;
  xentralCustomerId: string;
  updatedFields: string[];
  syncTimestamp: string;
}
```

### Discount Management

```typescript
// PATCH /api/v1/customers/{xentralId}/discount
interface UpdateDiscountRequest {
  freshplanActive: boolean;
  reason: 'contract_expired' | 'contract_renewed' | 'manual_override';
  deactivatedAt?: string; // ISO datetime
  reactivatedAt?: string; // ISO datetime
  fallbackPricing?: 'list_prices' | 'custom';
}

interface UpdateDiscountResponse {
  success: boolean;
  previousStatus: boolean;
  currentStatus: boolean;
  affectedOrders: number;
}
```

### Price Index Update

```typescript
// POST /api/v1/pricing/index-update
interface PriceIndexUpdateRequest {
  indexType: string;
  currentIndex: number;
  baselineIndex: number;
  changePercent: number;
  allowedPriceIncrease?: number;
  effectiveDate: string;
  notificationsSent: string[];
}

interface PriceIndexUpdateResponse {
  acknowledged: boolean;
  customersAffected: number;
  priceListsUpdated: string[];
  scheduledDate: string;
}
```

## API Client Implementation

```java
@ApplicationScoped
public class XentralContractApiClient extends XentralApiClient {
    
    @ConfigProperty(name = "xentral.api.contract.timeout", defaultValue = "10s")
    Duration contractTimeout;
    
    public Uni<UpdateContractResponse> updateContractStatus(
        String xentralCustomerId,
        ContractUpdateRequest request
    ) {
        return webClient
            .put()
            .uri("/api/v1/customers/{id}/contract", xentralCustomerId)
            .header("Authorization", getAuthToken())
            .body(request)
            .send()
            .timeout(contractTimeout)
            .onFailure().retry().atMost(3)
            .onItem().transform(response -> {
                if (response.statusCode() != 200) {
                    throw new XentralApiException(
                        "Contract update failed: " + response.statusCode()
                    );
                }
                return response.bodyAsJson(UpdateContractResponse.class);
            });
    }
    
    public Uni<Void> deactivateCustomerDiscount(
        String xentralCustomerId,
        String reason
    ) {
        var request = new UpdateDiscountRequest();
        request.setFreshplanActive(false);
        request.setReason(reason);
        request.setDeactivatedAt(Instant.now().toString());
        
        return webClient
            .patch()
            .uri("/api/v1/customers/{id}/discount", xentralCustomerId)
            .header("Authorization", getAuthToken())
            .body(request)
            .send()
            .onItem().transformToUni(response -> {
                if (response.statusCode() == 200) {
                    return Uni.createFrom().voidItem();
                }
                return Uni.createFrom().failure(
                    new XentralApiException("Discount deactivation failed")
                );
            });
    }
}
```

## Event Handler

```java
@ApplicationScoped
public class ContractXentralEventHandler {
    
    @Inject
    XentralContractApiClient xentralClient;
    
    @Inject
    ContractMonitoringRepository contractRepo;
    
    void onContractExpired(@Observes ContractExpiredEvent event) {
        var contract = event.getContract();
        
        xentralClient.updateContractStatus(
            contract.getXentralCustomerId(),
            ContractUpdateRequest.builder()
                .contractStatus("expired")
                .freshplanPartner(false)
                .discountEligible(false)
                .build()
        )
        .subscribe().with(
            response -> {
                contract.setXentralSyncStatus(SyncStatus.SYNCED);
                contract.setLastSyncAttempt(LocalDateTime.now());
                contractRepo.persist(contract);
                Log.infof("Contract %s synced to Xentral", contract.getId());
            },
            failure -> {
                contract.setXentralSyncStatus(SyncStatus.FAILED);
                contract.setLastSyncAttempt(LocalDateTime.now());
                contractRepo.persist(contract);
                Log.errorf(failure, "Failed to sync contract %s", contract.getId());
            }
        );
    }
    
    void onPriceIndexExceeded(@Observes PriceIndexExceededEvent event) {
        // Notification only - actual price changes manual
        var affectedContracts = contractRepo.findActiveContracts();
        
        xentralClient.notifyPriceIndexChange(
            PriceIndexUpdateRequest.builder()
                .indexType(event.getIndexType())
                .currentIndex(event.getCurrentValue())
                .baselineIndex(event.getBaselineValue())
                .changePercent(event.getChangePercent())
                .effectiveDate(LocalDate.now().plusMonths(1).toString())
                .build()
        )
        .subscribe().with(
            response -> Log.info("Price index notification sent to Xentral"),
            failure -> Log.error("Failed to notify Xentral about price index", failure)
        );
    }
}
```

## Fehlerbehandlung & Retry

```java
@ApplicationScoped
public class XentralSyncService {
    
    @Inject
    @Channel("xentral-retry")
    Emitter<FailedSyncEvent> retryEmitter;
    
    @Scheduled(every = "30m")
    void retryFailedSyncs() {
        var failedSyncs = contractRepo.findByXentralSyncStatus(SyncStatus.FAILED);
        
        failedSyncs.forEach(contract -> {
            retryEmitter.send(new FailedSyncEvent(contract));
        });
    }
    
    @Incoming("xentral-retry")
    @Retry(maxRetries = 5, delay = 1000, exponentialBackoff = @ExponentialBackoff(factor = 2))
    void processRetry(FailedSyncEvent event) {
        // Retry logic with exponential backoff
    }
}
```

## 🔗 Verwandte Dokumente

- **Event Bus Setup:** [/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-xentral-integration.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-xentral-integration.md)
- **Error Handling:** [./error-handling.md](./error-handling.md)
- **Monitoring:** [./monitoring-dashboard.md](./monitoring-dashboard.md)