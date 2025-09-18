# FC-005: Xentral-Integration für Provisions-Management

**Feature Code:** FC-005  
**Status:** 📋 Planungsphase  
**Geschätzter Aufwand:** 8-10 Tage  
**Priorität:** HOCH - Kern für Provisions-System  
**Phase:** 4.3 (Xentral-Integration)  

## 🎯 Zusammenfassung

Xentral ist die "Source of Truth" für Rechnungen und Zahlungen. Ohne diese Daten keine korrekte Provisionsberechnung. Diese Integration macht das CRM zum vollwertigen Provisions-Management-System.

## 💰 Kern-Datenfluss

```
Xentral → CRM (Pull + Push):
1. Auftrag abgeschlossen → Status-Update im CRM
2. Rechnung erstellt → Provisionsbasis bekannt
3. Zahlung eingegangen → Provision wird fällig (Webhook!)
4. Teilzahlung → Anteilige Provision

CRM → Xentral (Push):
1. Neuer Auftrag → Auftrag in Xentral anlegen
2. Kundendaten → Stammdaten synchronisieren
```

## 🏗️ Technische Architektur

### 1. Xentral API Client (Backend)

```java
@ApplicationScoped
public class XentralApiClient {
    @ConfigProperty(name = "xentral.api.base-url")
    String baseUrl;
    
    @Inject
    XentralAuthService authService;
    
    public Mono<List<Invoice>> getInvoices(LocalDate from, LocalDate to) {
        return webClient
            .get()
            .uri("/api/v1/invoices?from={from}&to={to}", from, to)
            .header("Authorization", authService.getToken())
            .retrieve()
            .bodyToFlux(Invoice.class)
            .collectList();
    }
    
    public Mono<Payment> getPaymentDetails(Long paymentId) {
        // Implementation mit Retry und Error Handling
    }
}
```

### 2. Webhook Handler

```java
@Path("/webhooks/xentral")
@Consumes(MediaType.APPLICATION_JSON)
public class XentralWebhookResource {
    
    @POST
    @Transactional
    public Response handleWebhook(
        @HeaderParam("X-Xentral-Signature") String signature,
        XentralWebhookPayload payload
    ) {
        // 1. Signatur validieren
        if (!webhookValidator.isValid(signature, payload)) {
            return Response.status(401).build();
        }
        
        // 2. Event verarbeiten
        switch (payload.getEvent()) {
            case "payment.received":
                handlePaymentReceived(payload.getData());
                break;
            case "invoice.created":
                handleInvoiceCreated(payload.getData());
                break;
            // weitere Events...
        }
        
        return Response.ok().build();
    }
    
    private void handlePaymentReceived(PaymentData data) {
        // Event publizieren für Provisions-Berechnung
        eventBus.publish(new PaymentReceivedEvent(
            data.getInvoiceId(),
            data.getAmount(),
            data.getCustomerId()
        ));
    }
}
```

### 3. Sync Service

```java
@ApplicationScoped
public class XentralSyncService {
    
    @Scheduled(every = "6h")
    @RequiresPermission("sync.trigger") // FC-015 Integration
    void syncInvoices() {
        var lastSync = syncRepository.getLastSync("invoices");
        var invoices = xentralClient.getInvoicesSince(lastSync);
        
        invoices.forEach(invoice -> {
            // Update oder Create
            var existing = invoiceRepo.findByXentralId(invoice.getId());
            if (existing.isPresent()) {
                updateInvoice(existing.get(), invoice);
            } else {
                createInvoice(invoice);
            }
        });
        
        syncRepository.updateLastSync("invoices", LocalDateTime.now());
    }
    
    @Scheduled(every = "24h")
    void reconcilePayments() {
        // Täglicher Abgleich als Fallback für Webhooks
    }
}
```

### 4. Provisions-Engine

```java
@ApplicationScoped
public class CommissionEngine {
    
    void onPaymentReceived(@Observes PaymentReceivedEvent event) {
        // 1. Rechnung und Auftrag laden
        var invoice = invoiceRepo.findByXentralId(event.getInvoiceId());
        var order = orderRepo.findByInvoice(invoice);
        
        // 2. Verkäufer ermitteln
        var salesRep = customerProtection.getResponsibleRep(order.getCustomerId());
        
        // 3. Provision berechnen
        var commission = commissionCalculator.calculate(
            event.getAmount(),
            salesRep,
            order.isNewCustomer()
        );
        
        // 4. Provision speichern
        commissionRepo.persist(commission);
        
        // 5. Notification
        notificationService.notifySalesRep(salesRep, commission);
    }
}
```

### 5. Frontend-Integration

**Dashboard-Komponente für Provisions-Übersicht:**

```typescript
export const CommissionDashboard: React.FC = () => {
  const { data: commissions } = useCommissions();
  const { data: pending } = usePendingCommissions();
  
  return (
    <Grid container spacing={3}>
      <Grid item xs={12} md={4}>
        <StatCard
          title="Provisionen diesen Monat"
          value={commissions?.thisMonth || 0}
          format="currency"
          icon={<EuroIcon />}
        />
      </Grid>
      <Grid item xs={12} md={4}>
        <StatCard
          title="Offene Provisionen"
          value={pending?.total || 0}
          format="currency"
          subtitle="Warte auf Zahlungseingang"
        />
      </Grid>
      <Grid item xs={12} md={4}>
        <StatCard
          title="Provisions-Rate"
          value={commissions?.averageRate || 0}
          format="percentage"
        />
      </Grid>
      
      <Grid item xs={12}>
        <CommissionTimeline commissions={commissions?.recent} />
      </Grid>
    </Grid>
  );
};
```

## 🔗 Abhängigkeiten

### Zu bestehenden Modulen:
- **Customer (M5)**: Kunden-Mapping Xentral ↔ CRM
- **Protection (FC-004)**: Verkäuferzuordnung für Provision
- **Timeline**: Zahlungs-Events dokumentieren
- **FC-009 Renewal**: Contract Status Sync, Discount Management

### Zu neuen Features:
- **FC-007 (Chef-Dashboard)**: Provisions-KPIs
- **FC-006 (Mobile)**: Push bei Zahlungseingang

### Externe Abhängigkeiten:
- **Xentral API**: Verfügbarkeit und Performance
- **OAuth Service**: Token-Management
- **Webhook-Endpunkt**: Muss öffentlich erreichbar sein

## 🏛️ Architektur-Entscheidungen

### ADR-005-001: Sync-Strategie
**Entscheidung:** Hybrid-Ansatz mit Webhook + Polling
- Webhooks für Echtzeit-Events
- 6h-Sync für neue Rechnungen
- 24h-Reconciliation als Fallback

### ADR-005-002: Daten-Speicherung
**Entscheidung:** Lokale Kopie relevanter Xentral-Daten
- Bessere Performance
- Offline-Fähigkeit
- Historische Auswertungen

## 🚀 Implementierungsphasen

### Phase 1: API Client & Auth (2 Tage)
1. OAuth 2.0 Implementation
2. API Client mit Retry-Logic
3. Rate Limiting beachten
4. Error Handling

### Phase 2: Datenmodell (1 Tag)
1. Invoice, Payment Entities
2. Mapping zu Xentral-IDs
3. Sync-Status tracking

### Phase 3: Webhook-Integration (2 Tage)
1. Webhook-Endpunkt
2. Signatur-Validierung
3. Event-Processing
4. Queue für Reliability

### Phase 4: Sync-Services (2 Tage)
1. Invoice-Sync
2. Payment-Reconciliation
3. Customer-Mapping
4. Performance-Optimierung

### Phase 5: Provisions-Engine (2 Tage)
1. Event-Handler
2. Berechnungs-Logic
3. Notifications
4. Reporting

### Phase 6: Frontend (1 Tag)
1. Provisions-Dashboard
2. Timeline-Integration
3. Detail-Ansichten

## 🧪 Test-Strategie

```java
@Test
void shouldCalculateCommissionOnPaymentWebhook() {
    // Given
    var webhook = createPaymentWebhook(1500.00, customerId);
    mockXentralApi();
    
    // When
    webhookResource.handleWebhook(validSignature, webhook);
    
    // Then
    await().atMost(5, SECONDS).untilAsserted(() -> {
        var commission = commissionRepo.findByPaymentId(paymentId);
        assertThat(commission.getAmount()).isEqualTo(45.00); // 3%
    });
}
```

## 📊 Erfolgsmetriken

- Webhook-Latenz: <2s bis Provision berechnet
- Sync-Vollständigkeit: 100% aller Zahlungen erfasst
- Verfügbarkeit: 99.9% (Fallback via Polling)

## 🔍 Technische Details

Siehe: [Xentral API Analyse](/docs/technical/XENTRAL_API_ANALYSIS.md)

## 🚨 Risiken & Mitigationen

1. **API-Ausfall**: Polling als Fallback
2. **Webhook-Verlust**: Tägliche Reconciliation
3. **Performance**: Caching & Pagination
4. **Datenkonsistenz**: Event Sourcing für Audit Trail