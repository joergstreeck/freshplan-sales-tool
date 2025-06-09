package de.freshplan.user;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * User roles for FreshPlan application.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
public enum UserRole {
    ADMIN("admin"),
    MANAGER("manager"),
    SALES("sales");
    
    private final String value;
    
    UserRole(String value) {
        this.value = value;
    }
    
    @JsonValue
    public String getValue() {
        return value;
    }
    
    @JsonCreator
    public static UserRole fromValue(String value) {
        for (UserRole role : UserRole.values()) {
            if (role.value.equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + value);
    }
}