# 🔒 Security Guidelines - FreshPlan Enterprise Security Framework

**Erstellt:** 2025-09-17
**Status:** ✅ Basiert auf implementierter Security-Architektur
**Basis:** Keycloak OIDC + Quarkus Security + React Auth + Security Tests
**Scope:** Authentication, Authorization, Data Protection, Security Testing

## 📋 Executive Summary: Enterprise Security Posture

### **SECURITY ARCHITECTURE OVERVIEW:**
```yaml
Authentication Layer:
  - Provider: Keycloak OIDC (Production: auth.z-catering.de)
  - Protocol: OpenID Connect + OAuth 2.0
  - Token Type: JWT (JSON Web Tokens)
  - Token Refresh: Automatic (5-minute buffer)
  - Development Mode: Security disabled für rapid iteration

Authorization Model:
  - Role-Based Access Control (RBAC)
  - Roles: admin, manager, sales
  - Method-Level Security: @RolesAllowed annotations
  - Security Audit: @SecurityAudit interceptor

Data Protection:
  - Transport: HTTPS enforced (Production)
  - CORS: Configured für specific origins
  - Input Validation: DTO-level + service-level
  - SQL Injection: Prevention via JPA/Panache

Security Testing:
  - MSW Security Tests (Frontend)
  - Security Audit Tests (Backend)
  - Token Security Validation
  - CORS Configuration Testing
```

## 🔐 **AUTHENTICATION ARCHITECTURE**

### **Keycloak OIDC Integration:**

#### **Production Configuration:**
```properties
# application.properties - Production Security
quarkus.oidc.auth-server-url=https://auth.z-catering.de/realms/freshplan-realm
quarkus.oidc.client-id=freshplan-backend
quarkus.oidc.credentials.secret=${KEYCLOAK_CLIENT_SECRET}
quarkus.oidc.application-type=service
quarkus.oidc.tls.verification=certificate-validation

# Enhanced JWT Configuration
quarkus.oidc.token.issuer=https://auth.z-catering.de/realms/freshplan-realm
quarkus.oidc.token.audience=freshplan-backend
quarkus.oidc.jwks-path=https://auth.z-catering.de/realms/freshplan-realm/protocol/openid-connect/certs

# Token validation settings
quarkus.oidc.token.verify-access-token-with-user-info=false
quarkus.oidc.token.allow-jwt-introspection=true
quarkus.oidc.token.allow-opaque-token-introspection=true

# Role mapping
quarkus.oidc.roles.role-claim-path=realm_access/roles
```

#### **Development Configuration:**
```properties
# Development: Security disabled for rapid iteration
%dev.quarkus.oidc.enabled=false
%dev.quarkus.security.auth.enabled-in-dev-mode=false

# Test: Security disabled for testing
%test.quarkus.oidc.enabled=false
%test.quarkus.http.auth.basic=false
%test.quarkus.http.auth.form.enabled=false

# E2E: Security enabled for integration tests
%e2e.quarkus.oidc.enabled=true
%e2e.quarkus.oidc.auth-server-url=http://localhost:8180/realms/freshplan
```

### **Frontend Authentication (React + Keycloak):**

#### **Automatic Token Management:**
```typescript
// authenticatedApiClient.ts - Token Lifecycle Management
export const apiClient: AxiosInstance = axios.create({
  baseURL: API_URL,
  headers: { 'Content-Type': 'application/json' },
});

// Request interceptor with automatic token refresh
apiClient.interceptors.request.use(async config => {
  const token = authUtils.getToken();

  if (token) {
    // Check if token needs refresh (5-minute buffer)
    const needsRefresh = authUtils.isTokenExpired(300);
    if (needsRefresh) {
      try {
        await authUtils.updateToken(300);
        const newToken = authUtils.getToken();
        config.headers.Authorization = `Bearer ${newToken}`;
      } catch (error) {
        console.error('Token refresh failed:', error);
        // Redirect to login or handle gracefully
      }
    } else {
      config.headers.Authorization = `Bearer ${token}`;
    }
  }

  return config;
});

// Response interceptor for auth errors
apiClient.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      // Handle unauthorized access
      authUtils.logout();
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);
```

#### **Security Context Management:**
```typescript
// Frontend Auth Context with security state
interface AuthContextType {
  isAuthenticated: boolean;
  user: User | null;
  token: string | null;
  roles: string[];
  hasRole: (role: string) => boolean;
  login: () => Promise<void>;
  logout: () => void;
}

const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [user, setUser] = useState<User | null>(null);

  // Security state management
  const hasRole = useCallback((role: string) => {
    return user?.roles?.includes(role) ?? false;
  }, [user]);

  return (
    <AuthContext.Provider value={{
      isAuthenticated,
      user,
      hasRole,
      // ... other auth methods
    }}>
      {children}
    </AuthContext.Provider>
  );
};
```

## 🛡️ **AUTHORIZATION MODEL**

### **Role-Based Access Control (RBAC):**

#### **Defined Roles & Permissions:**
```java
// SecurityConfig.java - Role Constants
public static class Roles {
    public static final String ADMIN = "admin";      // Full system access
    public static final String MANAGER = "manager";  // Management functions
    public static final String SALES = "sales";      // Sales operations
}

// Role Hierarchy (implicit):
// admin > manager > sales
// Each role includes permissions of lower roles
```

#### **Backend Authorization Patterns:**
```java
// Method-Level Security
@Path("/customers")
@SecurityAudit  // Audit all customer operations
public class CustomerResource {

    @GET
    @RolesAllowed({Roles.ADMIN, Roles.MANAGER, Roles.SALES})  // All roles can read
    public Response getAllCustomers() {
        // Customer list access
    }

    @POST
    @RolesAllowed({Roles.ADMIN, Roles.MANAGER})  // Only admin/manager can create
    @SecurityAudit
    public Response createCustomer(@Valid CreateCustomerRequest request) {
        // Audit logged + authorization enforced
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed(Roles.ADMIN)  // Only admin can delete
    @SecurityAudit
    public Response deleteCustomer(@PathParam("id") UUID id) {
        // Critical operation - admin only
    }
}
```

#### **Frontend Authorization Guards:**
```typescript
// Route-level protection
import { AuthGuard } from '@/components/auth/AuthGuard';

const ProtectedRoute: React.FC = () => {
  return (
    <AuthGuard roles={['admin', 'manager']}>
      <SensitiveComponent />
    </AuthGuard>
  );
};

// Component-level authorization
const ActionButton: React.FC = () => {
  const { hasRole } = useAuth();

  if (!hasRole('manager')) {
    return null; // Hide component for unauthorized users
  }

  return <Button onClick={sensitiveAction}>Delete Customer</Button>;
};
```

### **Security Context Integration:**

#### **Backend Security Context:**
```java
// SecurityContextProvider.java - Current User Access
@ApplicationScoped
public class SecurityContextProvider {

    @Inject
    SecurityIdentity securityIdentity;

    public UserPrincipal getCurrentUser() {
        if (securityIdentity.isAnonymous()) {
            throw new UnauthorizedException("No authenticated user");
        }

        return UserPrincipal.builder()
            .username(securityIdentity.getPrincipal().getName())
            .roles(securityIdentity.getRoles())
            .attributes(securityIdentity.getAttributes())
            .build();
    }

    public boolean hasRole(String role) {
        return securityIdentity.hasRole(role);
    }
}

// CurrentUser injection for services
@ApplicationScoped
public class CustomerService {

    @Inject
    @CurrentUser
    UserPrincipal currentUser;  // Injected current user context

    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        // Access current user for audit logging
        log.info("Customer created by: {}", currentUser.getUsername());
        // ... business logic
    }
}
```

## 🔍 **SECURITY AUDIT & LOGGING**

### **Comprehensive Audit Framework:**

#### **Audit Annotation & Interceptor:**
```java
// SecurityAudit.java - Annotation for audit logging
@InterceptorBinding
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SecurityAudit {}

// SecurityAuditInterceptor.java - Audit implementation
@SecurityAudit
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class SecurityAuditInterceptor {

    @AroundInvoke
    public Object auditSecurityOperation(InvocationContext context) throws Exception {
        UserPrincipal user = getCurrentUser();
        String operation = context.getMethod().getName();
        String resource = context.getTarget().getClass().getSimpleName();

        // Pre-execution audit
        log.info("SECURITY_AUDIT: User {} attempting {} on {}",
            user.getUsername(), operation, resource);

        try {
            Object result = context.proceed();

            // Success audit
            log.info("SECURITY_AUDIT: User {} successfully executed {} on {}",
                user.getUsername(), operation, resource);

            return result;
        } catch (Exception e) {
            // Failure audit
            log.warn("SECURITY_AUDIT: User {} failed to execute {} on {} - Error: {}",
                user.getUsername(), operation, resource, e.getMessage());
            throw e;
        }
    }
}
```

### **Audit Log Configuration:**
```properties
# application.properties - Security Logging
quarkus.log.category."de.freshplan.infrastructure.security".level=INFO
quarkus.log.category."SECURITY_AUDIT".level=INFO

# Structured logging for security events
quarkus.log.file.format=%d{yyyy-MM-dd HH:mm:ss} %-5p [%c{2.}] (%t) %s%e%n

# Separate security log file (optional)
quarkus.log.handler.file."SECURITY".enable=true
quarkus.log.handler.file."SECURITY".path=logs/security-audit.log
quarkus.log.handler.file."SECURITY".format=%d{yyyy-MM-dd HH:mm:ss} [SECURITY] %s%e%n
```

## 🌐 **NETWORK SECURITY & CORS**

### **CORS Configuration (Secure & Flexible):**
```properties
# application.properties - CORS Security
quarkus.http.cors=true
quarkus.http.cors.origins=http://localhost:5173,http://192.168.1.42:5173,http://127.0.0.1:5173
quarkus.http.cors.methods=GET,POST,PUT,DELETE,OPTIONS,HEAD,PATCH
quarkus.http.cors.headers=accept,authorization,content-type,x-requested-with,origin
quarkus.http.cors.exposed-headers=location,content-disposition

# SECURITY: Credentials disabled to prevent CSRF attacks
# Frontend uses Bearer tokens in Authorization header (not cookies)
quarkus.http.cors.access-control-allow-credentials=false
quarkus.http.cors.access-control-max-age=86400  # 24h cache for preflight

# Health endpoints without authentication
quarkus.http.auth.permission.health.paths=/q/health,/q/health/*
quarkus.http.auth.permission.health.policy=permit
quarkus.http.auth.permission.health.methods=GET

# All other endpoints require authentication
quarkus.http.auth.permission.authenticated.paths=/*
quarkus.http.auth.permission.authenticated.policy=authenticated
```

### **TLS/HTTPS Configuration:**
```yaml
Production Security:
  - TLS 1.2+ enforced
  - Certificate validation enabled
  - HSTS headers configured
  - Secure cookie settings

Development Security:
  - HTTP allowed für local development
  - Self-signed certificates acceptance
  - CORS relaxed für development origins
```

## 🛠️ **INPUT VALIDATION & DATA PROTECTION**

### **Multi-Layer Input Validation:**

#### **DTO-Level Validation:**
```java
// CreateCustomerRequest.java - Bean Validation
public record CreateCustomerRequest(
    @NotBlank(message = "Company name is required")
    @Size(max = 255, message = "Company name too long")
    String companyName,

    @Email(message = "Invalid email format")
    @Size(max = 320, message = "Email too long")
    String email,

    @Pattern(regexp = "^[A-Z]{2}$", message = "Invalid country code")
    String countryCode,

    @DecimalMin(value = "0.0", message = "Annual volume must be positive")
    @DecimalMax(value = "999999999.99", message = "Annual volume too large")
    BigDecimal expectedAnnualVolume,

    @Valid
    List<@Valid ContactRequest> contacts  // Nested validation
) {}
```

#### **Service-Level Validation:**
```java
// CustomerService.java - Business Rule Validation
@Transactional
public CustomerResponse createCustomer(CreateCustomerRequest request) {
    // 1. DTO validation (automatic via @Valid)
    // 2. Business rule validation
    validateBusinessRules(request);

    // 3. Security context validation
    if (!securityContext.hasRole(Roles.MANAGER) &&
        request.expectedAnnualVolume().compareTo(LARGE_CUSTOMER_THRESHOLD) > 0) {
        throw new UnauthorizedException("Large customers require manager approval");
    }

    // 4. Data sanitization
    String sanitizedCompanyName = sanitizeInput(request.companyName());

    // 5. Proceed with business logic
    return createCustomerInternal(request.withCompanyName(sanitizedCompanyName));
}

private void validateBusinessRules(CreateCustomerRequest request) {
    // Custom business validation
    if (customerRepository.existsByCompanyName(request.companyName())) {
        throw new BusinessException("Customer with this company name already exists");
    }
}

private String sanitizeInput(String input) {
    return input.trim()
        .replaceAll("[<>\"'&]", "")  // Basic XSS prevention
        .substring(0, Math.min(input.length(), 255));  // Length enforcement
}
```

### **SQL Injection Prevention:**
```java
// Automatic prevention via JPA/Panache
@Repository
public class CustomerRepository implements PanacheRepositoryBase<Customer, UUID> {

    // ✅ SAFE: Parameterized queries via Panache
    public List<Customer> findByCompanyNameContaining(String name) {
        return find("companyName LIKE ?1", "%" + name + "%").list();
    }

    // ✅ SAFE: Named parameters
    public List<Customer> findByStatusAndRisk(CustomerStatus status, int minRisk) {
        return find("status = :status AND riskScore >= :minRisk",
                   Parameters.with("status", status)
                            .and("minRisk", minRisk))
               .list();
    }

    // ❌ DANGEROUS: Would be unsafe if using raw SQL
    // Never do: entityManager.createNativeQuery("SELECT * FROM customers WHERE name = '" + name + "'")
}
```

## 🧪 **SECURITY TESTING FRAMEWORK**

### **Frontend Security Tests:**

#### **MSW Security Testing:**
```typescript
// msw-security.test.ts - Mock Service Worker Security
describe('MSW Security Configuration', () => {

  it('should NOT set auth token when MSW is disabled', () => {
    const USE_MSW = 'false';

    if (USE_MSW === 'true') {
      localStorage.setItem('auth-token', 'mock_token');
    } else {
      localStorage.removeItem('auth-token');
    }

    // Verify no mock tokens leak into real tests
    expect(localStorage.getItem('auth-token')).toBeNull();
  });

  it('should handle token expiration gracefully', () => {
    // Test token refresh mechanism
    // Test unauthorized response handling
    // Test logout on auth failure
  });
});

// msw-token-security.test.ts - Token Security
describe('Token Security', () => {

  it('should validate token format', () => {
    const validJWT = 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...';
    const invalidToken = 'invalid-token';

    expect(isValidJWTFormat(validJWT)).toBe(true);
    expect(isValidJWTFormat(invalidToken)).toBe(false);
  });

  it('should detect token expiration', () => {
    const expiredToken = createExpiredToken();
    const validToken = createValidToken();

    expect(isTokenExpired(expiredToken)).toBe(true);
    expect(isTokenExpired(validToken)).toBe(false);
  });
});
```

#### **Authentication Flow Testing:**
```typescript
// auth.spec.ts - E2E Authentication Testing
describe('Authentication Flow', () => {

  test('should redirect unauthenticated users to login', async ({ page }) => {
    await page.goto('/customers');
    await expect(page).toHaveURL(/.*login/);
  });

  test('should allow access after successful login', async ({ page }) => {
    // Login with test credentials
    await authenticateUser(page, { role: 'manager' });

    // Verify access to protected route
    await page.goto('/customers');
    await expect(page.locator('[data-testid="customer-list"]')).toBeVisible();
  });

  test('should enforce role-based access', async ({ page }) => {
    await authenticateUser(page, { role: 'sales' });

    // Verify sales user cannot access admin features
    await page.goto('/admin');
    await expect(page.locator('[data-testid="access-denied"]')).toBeVisible();
  });
});
```

### **Backend Security Tests:**

#### **Authorization Testing:**
```java
// CustomerResourceSecurityTest.java
@QuarkusTest
@TestSecurity(user = "testuser", roles = {"sales"})
class CustomerResourceSecurityTest {

    @Test
    void salesUser_canReadCustomers() {
        given()
            .when().get("/api/customers")
            .then()
            .statusCode(200);
    }

    @Test
    void salesUser_cannotDeleteCustomers() {
        given()
            .when().delete("/api/customers/123")
            .then()
            .statusCode(403);  // Forbidden
    }
}

@QuarkusTest
@TestSecurity(user = "admin", roles = {"admin"})
class AdminSecurityTest {

    @Test
    void adminUser_canDeleteCustomers() {
        // Admin has full access
        given()
            .when().delete("/api/customers/123")
            .then()
            .statusCode(204);  // No Content (success)
    }
}
```

#### **Security Audit Testing:**
```java
// SecurityAuditTest.java
@QuarkusTest
class SecurityAuditTest {

    @Inject
    SecurityAuditLogger auditLogger;

    @Test
    @TestSecurity(user = "testuser", roles = {"manager"})
    void securityAudit_shouldLogCustomerCreation() {
        CreateCustomerRequest request = validCustomerRequest();

        given()
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .when().post("/api/customers")
            .then()
            .statusCode(201);

        // Verify audit log entry
        verify(auditLogger).info(contains("User testuser successfully executed createCustomer"));
    }
}
```

## 🚨 **SECURITY INCIDENT RESPONSE**

### **Security Monitoring & Alerting:**
```yaml
Security Events to Monitor:
  - Failed authentication attempts (>5 in 5 minutes)
  - Unauthorized access attempts
  - Token manipulation attempts
  - Unusual data access patterns
  - Privilege escalation attempts

Alerting Thresholds:
  - Failed logins: >10 per user per hour
  - 401/403 responses: >100 per hour
  - Large data exports: >1000 records
  - Off-hours admin access
  - SQL injection attempts

Response Procedures:
  1. Automatic: Rate limiting, account lockout
  2. Alert: Security team notification
  3. Investigation: Log analysis, user verification
  4. Mitigation: Access revocation, system updates
```

### **Security Configuration Validation:**
```bash
# Security validation checklist
./scripts/security-check.sh

# Validate HTTPS configuration
curl -I https://api.freshplan.de/q/health

# Check CORS configuration
curl -H "Origin: https://malicious-site.com" \
     -H "Access-Control-Request-Method: POST" \
     -X OPTIONS https://api.freshplan.de/api/customers

# Verify authentication requirement
curl https://api.freshplan.de/api/customers  # Should return 401

# Test token validation
curl -H "Authorization: Bearer invalid-token" \
     https://api.freshplan.de/api/customers  # Should return 401
```

## 📋 **SECURITY BEST PRACTICES & GUIDELINES**

### **Development Security Guidelines:**

#### **Frontend Security Practices:**
```typescript
// ✅ DO: Secure token storage
const getToken = () => {
  return keycloak.token;  // In-memory only, no localStorage
};

// ✅ DO: Validate user input
const sanitizeInput = (input: string) => {
  return input.trim().replace(/[<>]/g, '');
};

// ✅ DO: Use environment variables for config
const API_URL = import.meta.env.VITE_API_URL;

// ❌ DON'T: Store sensitive data in localStorage
localStorage.setItem('password', password);  // NEVER DO THIS

// ❌ DON'T: Hardcode secrets
const API_KEY = 'secret-key-123';  // NEVER DO THIS

// ❌ DON'T: Trust client-side validation only
if (userRole === 'admin') {  // Backend must also validate
  showAdminFeatures();
}
```

#### **Backend Security Practices:**
```java
// ✅ DO: Use security annotations
@RolesAllowed({"admin", "manager"})
@SecurityAudit
public Response sensitiveOperation() { }

// ✅ DO: Validate all inputs
public Response createCustomer(@Valid CreateCustomerRequest request) {
    validateBusinessRules(request);  // Additional validation
}

// ✅ DO: Use parameterized queries
find("companyName = ?1", name);  // Safe

// ❌ DON'T: Concatenate SQL strings
entityManager.createNativeQuery("SELECT * FROM customers WHERE name = '" + name + "'");

// ❌ DON'T: Return sensitive data in errors
throw new BusinessException("Database connection failed: " + dbPassword);

// ❌ DON'T: Log sensitive information
log.info("Processing payment for card: {}", creditCardNumber);
```

### **Configuration Security:**
```properties
# ✅ DO: Use environment variables for secrets
quarkus.oidc.credentials.secret=${KEYCLOAK_CLIENT_SECRET}

# ✅ DO: Disable features in production
%prod.quarkus.hibernate-orm.log.sql=false
%prod.quarkus.log.category."org.hibernate.SQL".level=WARN

# ✅ DO: Configure secure headers
quarkus.http.header."X-Frame-Options".value=DENY
quarkus.http.header."X-Content-Type-Options".value=nosniff
quarkus.http.header."X-XSS-Protection".value=1; mode=block

# ❌ DON'T: Hardcode secrets
quarkus.oidc.credentials.secret=hardcoded-secret-123

# ❌ DON'T: Enable debug features in production
%prod.quarkus.http.auth.enabled=false
```

## 🔄 **SECURITY ROADMAP & IMPROVEMENTS**

### **Current Security Posture Assessment:**
```yaml
✅ IMPLEMENTED:
  - Keycloak OIDC authentication
  - Role-based authorization (3 roles)
  - Automatic token refresh
  - Security audit logging
  - Input validation framework
  - CORS configuration
  - SQL injection prevention
  - Security testing framework

🔄 IN PROGRESS:
  - Security monitoring dashboard
  - Advanced audit analytics
  - Penetration testing
  - Security documentation

📋 PLANNED:
  - Multi-factor authentication
  - Rate limiting implementation
  - Security scanning automation
  - Compliance certification (ISO 27001)
```

### **Future Security Enhancements:**
```yaml
Q4 2025:
  - Enhanced audit dashboard
  - Automated security testing in CI/CD
  - Rate limiting middleware
  - Security headers enforcement

Q1 2026:
  - Multi-factor authentication
  - Advanced threat detection
  - Security compliance automation
  - Penetration testing integration

Q2 2026:
  - Zero-trust architecture
  - Advanced encryption (field-level)
  - Security analytics AI
  - Compliance certification
```

---

**📋 Security Guidelines basiert auf:** Enterprise OIDC Implementation + Security Tests + Audit Framework
**📅 Letzte Aktualisierung:** 2025-09-17
**👨‍💻 Security Owner:** Development Team + Security Lead

**🎯 Diese Security-Architektur bietet Enterprise-Grade Sicherheit mit umfassendem Audit-Trail und Multi-Layer-Validation!**