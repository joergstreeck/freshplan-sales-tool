package de.freshplan.domain.audit.entity;

/**
 * Comprehensive audit event types for enterprise compliance
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
public enum AuditEventType {
    
    // ===== Opportunity Events =====
    OPPORTUNITY_CREATED("Opportunity created"),
    OPPORTUNITY_UPDATED("Opportunity updated"),
    OPPORTUNITY_DELETED("Opportunity deleted"),
    OPPORTUNITY_STAGE_CHANGED("Opportunity stage changed"),
    OPPORTUNITY_ASSIGNED("Opportunity assigned to user"),
    OPPORTUNITY_WON("Opportunity marked as won"),
    OPPORTUNITY_LOST("Opportunity marked as lost"),
    OPPORTUNITY_REACTIVATED("Opportunity reactivated"),
    OPPORTUNITY_VALUE_CHANGED("Opportunity value changed"),
    OPPORTUNITY_BULK_UPDATE("Bulk opportunity update"),
    
    // ===== Customer Events =====
    CUSTOMER_CREATED("Customer created"),
    CUSTOMER_UPDATED("Customer updated"),
    CUSTOMER_DELETED("Customer deleted"),
    CUSTOMER_MERGED("Customer records merged"),
    CUSTOMER_STATUS_CHANGED("Customer status changed"),
    CUSTOMER_GDPR_REQUEST("GDPR data request for customer"),
    CUSTOMER_DATA_EXPORT("Customer data exported"),
    
    // ===== Contract Events =====
    CONTRACT_CREATED("Contract created"),
    CONTRACT_RENEWED("Contract renewed"),
    CONTRACT_EXPIRED("Contract expired"),
    CONTRACT_TERMINATED("Contract terminated"),
    CONTRACT_PRICE_ADJUSTED("Contract price adjusted"),
    CONTRACT_TERMS_CHANGED("Contract terms changed"),
    
    // ===== Pricing Events =====
    PRICE_ADJUSTMENT("Price adjustment applied"),
    DISCOUNT_APPLIED("Discount applied"),
    DISCOUNT_REMOVED("Discount removed"),
    PRICE_INDEX_UPDATE("Price index updated"),
    PRICE_OVERRIDE("Manual price override"),
    
    // ===== Calculator Events =====
    CALCULATION_PERFORMED("Price calculation performed"),
    CALCULATION_SAVED("Calculation saved"),
    CALCULATION_CONVERTED("Calculation converted to opportunity"),
    
    // ===== API & Integration Events =====
    API_REQUEST("API request processed"),
    API_ERROR("API error occurred"),
    WEBHOOK_RECEIVED("Webhook received"),
    WEBHOOK_PROCESSED("Webhook processed"),
    WEBHOOK_FAILED("Webhook processing failed"),
    XENTRAL_SYNC_SUCCESS("Xentral sync successful"),
    XENTRAL_SYNC_FAILURE("Xentral sync failed"),
    XENTRAL_DATA_PUSHED("Data pushed to Xentral"),
    XENTRAL_DATA_PULLED("Data pulled from Xentral"),
    
    // ===== Security Events =====
    LOGIN_SUCCESS("User login successful"),
    LOGIN_FAILURE("User login failed"),
    LOGOUT("User logged out"),
    SESSION_EXPIRED("User session expired"),
    PERMISSION_CHECK("Permission check performed"),
    PERMISSION_GRANTED("Permission granted"),
    PERMISSION_DENIED("Permission denied"),
    ROLE_ASSIGNED("Role assigned to user"),
    ROLE_REMOVED("Role removed from user"),
    DELEGATION_CREATED("Delegation created"),
    DELEGATION_ACTIVATED("Delegation activated"),
    DELEGATION_EXPIRED("Delegation expired"),
    PASSWORD_CHANGED("Password changed"),
    TWO_FACTOR_ENABLED("Two-factor authentication enabled"),
    TWO_FACTOR_DISABLED("Two-factor authentication disabled"),
    
    // ===== Workflow Events =====
    APPROVAL_REQUESTED("Approval requested"),
    APPROVAL_GRANTED("Approval granted"),
    APPROVAL_DENIED("Approval denied"),
    APPROVAL_TIMEOUT("Approval timed out"),
    WORKFLOW_STARTED("Workflow started"),
    WORKFLOW_COMPLETED("Workflow completed"),
    WORKFLOW_CANCELLED("Workflow cancelled"),
    
    // ===== Communication Events =====
    EMAIL_SENT("Email sent"),
    EMAIL_RECEIVED("Email received"),
    EMAIL_BOUNCED("Email bounced"),
    EMAIL_ATTACHED_TO_ENTITY("Email attached to entity"),
    NOTIFICATION_SENT("Notification sent"),
    NOTIFICATION_READ("Notification read"),
    
    // ===== Activity Events =====
    ACTIVITY_CREATED("Activity created"),
    ACTIVITY_UPDATED("Activity updated"),
    ACTIVITY_COMPLETED("Activity completed"),
    ACTIVITY_CANCELLED("Activity cancelled"),
    TASK_ASSIGNED("Task assigned"),
    TASK_COMPLETED("Task completed"),
    NOTE_ADDED("Note added"),
    NOTE_UPDATED("Note updated"),
    REMINDER_SET("Reminder set"),
    REMINDER_SENT("Reminder sent"),
    
    // ===== System Events =====
    SYSTEM_STARTUP("System started"),
    SYSTEM_SHUTDOWN("System shutdown"),
    CONFIGURATION_CHANGED("System configuration changed"),
    MAINTENANCE_MODE_ENABLED("Maintenance mode enabled"),
    MAINTENANCE_MODE_DISABLED("Maintenance mode disabled"),
    BACKUP_STARTED("System backup started"),
    BACKUP_COMPLETED("System backup completed"),
    BACKUP_FAILED("System backup failed"),
    
    // ===== Data Management Events =====
    DATA_IMPORT_STARTED("Data import started"),
    DATA_IMPORT_COMPLETED("Data import completed"),
    DATA_IMPORT_FAILED("Data import failed"),
    DATA_EXPORT_STARTED("Data export started"),
    DATA_EXPORT_COMPLETED("Data export completed"),
    DATA_EXPORT_FAILED("Data export failed"),
    BULK_DELETE("Bulk delete operation"),
    DATA_ANONYMIZED("Data anonymized for GDPR"),
    DATA_RETENTION_APPLIED("Data retention policy applied"),
    
    // ===== Error & Exception Events =====
    ERROR_OCCURRED("System error occurred"),
    EXCEPTION_HANDLED("Exception handled"),
    CRITICAL_ERROR("Critical error occurred"),
    SECURITY_VIOLATION("Security violation detected");
    
    private final String description;
    
    AuditEventType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Check if this event type requires immediate notification
     */
    public boolean requiresNotification() {
        return this == SECURITY_VIOLATION ||
               this == CRITICAL_ERROR ||
               this == LOGIN_FAILURE ||
               this == PERMISSION_DENIED ||
               this == DATA_EXPORT_STARTED ||
               this == GDPR_REQUEST;
    }
    
    /**
     * Check if this event type is security-relevant
     */
    public boolean isSecurityRelevant() {
        return name().contains("PERMISSION") ||
               name().contains("ROLE") ||
               name().contains("LOGIN") ||
               name().contains("SECURITY") ||
               name().contains("DELEGATION") ||
               name().contains("PASSWORD");
    }
    
    /**
     * Get severity level for monitoring/alerting
     */
    public AuditSeverity getSeverity() {
        if (name().contains("FAILURE") || name().contains("ERROR") || name().contains("DENIED")) {
            return AuditSeverity.ERROR;
        }
        if (name().contains("SECURITY") || name().contains("GDPR") || name().contains("CRITICAL")) {
            return AuditSeverity.CRITICAL;
        }
        if (name().contains("LOGIN") || name().contains("PERMISSION") || name().contains("ROLE")) {
            return AuditSeverity.WARNING;
        }
        return AuditSeverity.INFO;
    }
    
    public enum AuditSeverity {
        INFO, WARNING, ERROR, CRITICAL
    }
}