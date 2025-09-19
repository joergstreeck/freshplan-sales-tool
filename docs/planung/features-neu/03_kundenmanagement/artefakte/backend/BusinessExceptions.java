package de.freshplan.customer.exceptions;

import jakarta.ws.rs.BadRequestException;

/**
 * Business-spezifische Exceptions für Modul 03 Kundenmanagement
 * Foundation Reference: /docs/planung/grundlagen/API_STANDARDS.md
 */

public class SampleNotFoundException extends BadRequestException {
    public SampleNotFoundException(String sampleId) {
        super("Sample not found: " + sampleId);
    }
}

public class SampleAlreadyCanceledException extends BadRequestException {
    public SampleAlreadyCanceledException(String sampleId) {
        super("Sample already canceled: " + sampleId);
    }
}

public class InvalidSampleStatusTransitionException extends BadRequestException {
    public InvalidSampleStatusTransitionException(String from, String to) {
        super("Invalid status transition from " + from + " to " + to);
    }
}

public class CustomerNotFoundException extends BadRequestException {
    public CustomerNotFoundException(String customerId) {
        super("Customer not found: " + customerId);
    }
}

public class InvalidDeliveryDateException extends BadRequestException {
    public InvalidDeliveryDateException(String reason) {
        super("Invalid delivery date: " + reason);
    }
}

public class InsufficientStockException extends BadRequestException {
    public InsufficientStockException(String sku, int requested, int available) {
        super("Insufficient stock for SKU " + sku + ": requested " + requested + ", available " + available);
    }
}