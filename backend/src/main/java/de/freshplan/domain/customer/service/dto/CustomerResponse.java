package de.freshplan.domain.customer.service.dto;

import de.freshplan.domain.customer.entity.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for customer data.
 * Contains all customer information in a structured format.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
public record CustomerResponse(
        
        // Identifier
        String id,
        String customerNumber,
        
        // Basic Information
        String companyName,
        String tradingName,
        String legalForm,
        
        // Classification
        CustomerType customerType,
        Industry industry,
        Classification classification,
        
        // Hierarchy
        String parentCustomerId,
        CustomerHierarchyType hierarchyType,
        List<String> childCustomerIds,
        boolean hasChildren,
        
        // Status & Lifecycle
        CustomerStatus status,
        CustomerLifecycleStage lifecycleStage,
        PartnerStatus partnerStatus,
        
        // Financial Information
        BigDecimal expectedAnnualVolume,
        BigDecimal actualAnnualVolume,
        PaymentTerms paymentTerms,
        BigDecimal creditLimit,
        DeliveryCondition deliveryCondition,
        
        // Risk Management
        Integer riskScore,
        boolean atRisk,
        LocalDateTime lastContactDate,
        LocalDateTime nextFollowUpDate,
        
        // Audit Information
        LocalDateTime createdAt,
        String createdBy,
        LocalDateTime updatedAt,
        String updatedBy,
        
        // Soft Delete (for admin/audit views)
        Boolean isDeleted,
        LocalDateTime deletedAt,
        String deletedBy
) {
    
    /**
     * Constructor for minimal customer response (for lists).
     */
    public CustomerResponse(
            String id,
            String customerNumber,
            String companyName,
            CustomerStatus status,
            CustomerType customerType,
            Industry industry,
            Integer riskScore,
            LocalDateTime createdAt) {
        
        this(
                id, customerNumber, companyName, null, null,
                customerType, industry, null,
                null, null, List.of(), false,
                status, null, null,
                null, null, null, null, null,
                riskScore, false, null, null,
                createdAt, null, null, null,
                false, null, null
        );
    }
}