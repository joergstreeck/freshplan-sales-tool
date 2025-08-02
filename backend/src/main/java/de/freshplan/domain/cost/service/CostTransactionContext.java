package de.freshplan.domain.cost.service;

import de.freshplan.domain.cost.entity.CostTransaction;

import java.math.BigDecimal;

/**
 * Context für Cost Transactions - Implementiert Auto-Closeable für Resource Management
 */
public class CostTransactionContext implements AutoCloseable {
    
    private final CostTransaction transaction;
    private final CostTrackingService service;
    private boolean completed = false;

    public CostTransactionContext(CostTransaction transaction, CostTrackingService service) {
        this.transaction = transaction;
        this.service = service;
    }

    /**
     * Markiert die Transaction als erfolgreich abgeschlossen
     */
    public void complete(BigDecimal actualCost, Integer tokensUsed) {
        if (!completed) {
            service.completeTransaction(transaction.id, actualCost, tokensUsed);
            completed = true;
        }
    }

    /**
     * Markiert die Transaction als fehlgeschlagen
     */
    public void fail(String errorMessage) {
        if (!completed) {
            service.failTransaction(transaction.id, errorMessage);
            completed = true;
        }
    }

    /**
     * Holt die Transaction ID
     */
    public String getTransactionId() {
        return transaction.id.toString();
    }

    /**
     * Holt das Service-Name
     */
    public String getService() {
        return transaction.service;
    }

    /**
     * Holt das Feature-Name
     */
    public String getFeature() {
        return transaction.feature;
    }

    /**
     * Holt geschätzte Kosten
     */
    public BigDecimal getEstimatedCost() {
        return transaction.estimatedCost;
    }

    /**
     * Auto-Close: Falls Transaction nicht explizit completed wurde, als fehlgeschlagen markieren
     */
    @Override
    public void close() {
        if (!completed) {
            fail("Transaction wurde nicht explizit abgeschlossen");
        }
    }
}