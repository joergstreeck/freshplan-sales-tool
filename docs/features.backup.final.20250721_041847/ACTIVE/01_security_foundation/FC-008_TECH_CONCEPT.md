# FC-008 Security Foundation - Tech Concept

**Feature-Code:** FC-008  
**Feature-Name:** Security Foundation  
**Feature-Typ:** 🔧 BACKEND FOUNDATION  
**Erstellt:** 2025-07-20  
**Status:** Tech Concept  
**Priorität:** KRITISCH - Foundation Feature  
**Geschätzter Aufwand:** 5-7 Tage  

---

## 🧠 CLAUDE WORKING SECTION (15-Min Context Chunk)

### 📍 Kontext laden für produktive Arbeit

**Was ist FC-008?** JWT-basierte Security Foundation mit Keycloak Integration für vollständige Authentifizierung und Autorisierung

**Warum kritisch?** Foundation Feature - ALLE anderen Features benötigen User-Context und Zugriffsschutz

**Aktueller Stand:** 85% fertig - Tests deaktiviert wegen fehlender Test-Endpoints (TODO-024/028)

**Abhängigkeiten verstehen:**
- **Benötigt:** KEINE - Foundation Feature
- **Blockiert:** FC-009 Permissions, M4 Pipeline, FC-004 Verkäuferschutz, alle UI-Features
- **Keycloak Setup:** Läuft auf localhost:8180, Realm: freshplan

**Technischer Kern:**
```java
@ApplicationScoped 
public class SecurityContextProvider {
    public Optional<User> getCurrentUser() {
        // JWT Token → User Context
        return extractUserFromToken(getCurrentToken());
    }
}
```

**Frontend-Integration:**
```typescript
export const useAuth = () => {
    const { data: user } = useQuery(['auth-user'], authService.getCurrentUser);
    return { user, isAuthenticated: !!user };
};
```

---

## 🎯 ÜBERSICHT

### Business Value
**Problem:** Neue Sales-Tool ohne Authentifizierung und Autorisierung - Sicherheitsrisiko und Compliance-Problem

**Lösung:** Enterprise-Grade Security Foundation mit JWT-basierter Authentifizierung und rolle-basierter Autorisierung

**ROI:** 
- **Kosten:** 5-7 Entwicklertage (~€7.000)
- **Compliance Value:** Unverzichtbar für Production (€50.000+ Risiko ohne Security)
- **Developer Productivity:** User-Context für alle Features (+30% Entwicklungsgeschwindigkeit)

### Kernfunktionen
1. **JWT Token Validation** - Sichere Token-basierte Authentifizierung
2. **User Context Provider** - Zentraler User-Service für alle Features
3. **Role-based Authorization** - 3 Rollen: admin, manager, sales
4. **Keycloak Integration** - OIDC-konforme Authentication
5. **Security Headers** - CORS, CSP, Security Headers für Frontend
6. **Audit Logging** - Nachverfolgbare Security-Events

---

## 🏗️ ARCHITEKTUR

### Security Flow
```
Frontend Request → Authentication Filter → JWT Validation → User Context → Resource Access
        ↓                ↓                     ↓               ↓              ↓
    Bearer Token    Token Extraction    Keycloak Verify   SecurityContext   @RolesAllowed
    Authorization   Header Check        Public Key         User Details      Permission Check
    HTTP Header     Extract JWT         Signature Valid    Role Mapping      Allow/Deny
```

### Database Schema - User Management
```sql
-- Users Table (synced from Keycloak)
CREATE TABLE users (
    id UUID PRIMARY KEY,
    keycloak_id VARCHAR(100) UNIQUE NOT NULL,
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    role VARCHAR(50) NOT NULL DEFAULT 'sales', -- 'admin', 'manager', 'sales'
    active BOOLEAN DEFAULT true,
    last_login TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Security Audit Log
CREATE TABLE security_audit_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id),
    event_type VARCHAR(50) NOT NULL, -- 'login', 'logout', 'access_denied', 'permission_check'
    resource_accessed VARCHAR(200),
    ip_address INET,
    user_agent TEXT,
    success BOOLEAN NOT NULL,
    error_message TEXT,
    additional_data JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Session Management (optional - for hybrid auth)
CREATE TABLE user_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    session_token VARCHAR(500) UNIQUE NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    ip_address INET,
    user_agent TEXT,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_users_keycloak_id ON users(keycloak_id);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_security_audit_user_event ON security_audit_log(user_id, event_type);
CREATE INDEX idx_security_audit_created ON security_audit_log(created_at);
CREATE INDEX idx_user_sessions_token ON user_sessions(session_token);
CREATE INDEX idx_user_sessions_user_active ON user_sessions(user_id, active);
```

### Backend-Architecture - Security Foundation
```java
// Security Context Provider - Core Service
@ApplicationScoped
public class SecurityContextProvider {
    
    @Inject JsonWebToken jwt;
    @Inject UserRepository userRepository;
    @Inject SecurityAuditService auditService;
    
    public Optional<User> getCurrentUser() {
        try {
            if (jwt == null || !isValidToken()) {
                return Optional.empty();
            }
            
            String keycloakId = jwt.getSubject();
            Optional<User> user = userRepository.findByKeycloakId(keycloakId);
            
            if (user.isPresent()) {
                // Update last login
                updateLastLogin(user.get());
                auditService.logAccess(user.get(), "context_access", true);
                return user;
            } else {
                auditService.logAccess(null, "user_not_found", false, keycloakId);
                return Optional.empty();
            }
            
        } catch (Exception e) {
            auditService.logAccess(null, "context_error", false, e.getMessage());
            return Optional.empty();
        }
    }
    
    public boolean hasRole(String requiredRole) {
        return getCurrentUser()
            .map(user -> user.getRole().equals(requiredRole) || isHigherRole(user.getRole(), requiredRole))
            .orElse(false);
    }
    
    public boolean isHigherRole(String userRole, String requiredRole) {
        // Role hierarchy: admin > manager > sales
        Map<String, Integer> roleHierarchy = Map.of(
            "admin", 3,
            "manager", 2, 
            "sales", 1
        );
        
        return roleHierarchy.getOrDefault(userRole, 0) >= 
               roleHierarchy.getOrDefault(requiredRole, 0);
    }
    
    private boolean isValidToken() {
        return jwt != null && 
               jwt.getExpirationTime() > Instant.now().getEpochSecond() &&
               jwt.getSubject() != null;
    }
    
    private void updateLastLogin(User user) {
        user.setLastLogin(LocalDateTime.now());
        userRepository.persist(user);
    }
}

// Authentication Filter - Request Interception
@WebFilter(urlPatterns = "/api/*")
@ApplicationScoped
public class AuthenticationFilter implements ContainerRequestFilter {
    
    @Inject SecurityContextProvider securityProvider;
    @Inject SecurityAuditService auditService;
    
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();
        
        // Skip authentication for public endpoints
        if (isPublicEndpoint(path)) {
            return;
        }
        
        // Extract and validate JWT token
        String authHeader = requestContext.getHeaderString("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            auditService.logAccess(null, "missing_token", false, path);
            requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse("Missing or invalid authorization header"))
                    .build()
            );
            return;
        }
        
        // Validate user context
        Optional<User> currentUser = securityProvider.getCurrentUser();
        if (currentUser.isEmpty()) {
            auditService.logAccess(null, "invalid_user", false, path);
            requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse("Invalid user context"))
                    .build()
            );
            return;
        }
        
        // Log successful authentication
        auditService.logAccess(currentUser.get(), "authenticated_access", true, path);
    }
    
    private boolean isPublicEndpoint(String path) {
        return path.startsWith("health") || 
               path.startsWith("metrics") ||
               path.startsWith("openapi") ||
               path.startsWith("swagger");
    }
}

// User Repository
@ApplicationScoped
public class UserRepository implements PanacheRepositoryBase<User, UUID> {
    
    public Optional<User> findByKeycloakId(String keycloakId) {
        return find("keycloak_id", keycloakId).firstResultOptional();
    }
    
    public Optional<User> findByUsername(String username) {
        return find("username", username).firstResultOptional();
    }
    
    public List<User> findByRole(String role) {
        return find("role = ?1 AND active = true", role).list();
    }
    
    public User createFromKeycloakToken(JsonWebToken jwt) {
        User user = new User();
        user.setKeycloakId(jwt.getSubject());
        user.setUsername(jwt.getName());
        user.setEmail(jwt.getClaim("email"));
        user.setFirstName(jwt.getClaim("given_name"));
        user.setLastName(jwt.getClaim("family_name"));
        user.setRole(extractRoleFromToken(jwt));
        user.setActive(true);
        
        persist(user);
        return user;
    }
    
    private String extractRoleFromToken(JsonWebToken jwt) {
        // Extract role from Keycloak token
        Set<String> roles = jwt.getGroups();
        
        if (roles.contains("admin")) return "admin";
        if (roles.contains("manager")) return "manager";
        return "sales"; // Default role
    }
}

// Security Audit Service
@ApplicationScoped
@Transactional
public class SecurityAuditService {
    
    @Inject SecurityAuditLogRepository auditRepository;
    
    public void logAccess(User user, String eventType, boolean success, String... details) {
        SecurityAuditLog log = SecurityAuditLog.builder()
            .userId(user != null ? user.getId() : null)
            .eventType(eventType)
            .resourceAccessed(details.length > 0 ? details[0] : null)
            .success(success)
            .errorMessage(details.length > 1 ? details[1] : null)
            .ipAddress(getCurrentIpAddress())
            .userAgent(getCurrentUserAgent())
            .build();
            
        auditRepository.persist(log);
    }
    
    public void logLogin(User user, boolean success) {
        logAccess(user, "login", success);
    }
    
    public void logLogout(User user) {
        logAccess(user, "logout", true);
    }
    
    public void logPermissionDenied(User user, String resource) {
        logAccess(user, "access_denied", false, resource);
    }
    
    private String getCurrentIpAddress() {
        // Extract from current request context
        return "127.0.0.1"; // Simplified
    }
    
    private String getCurrentUserAgent() {
        // Extract from current request context
        return "Unknown"; // Simplified
    }
}

// Test Endpoints (für TODO-024/028)
@Path("/api/test")
@ApplicationScoped
public class TestResource {
    
    @Inject SecurityContextProvider securityProvider;
    
    @GET
    @Path("/public")
    @Produces(MediaType.APPLICATION_JSON)
    public Response publicEndpoint() {
        return Response.ok(Map.of("message", "Public endpoint accessible")).build();
    }
    
    @GET
    @Path("/authenticated")
    @Produces(MediaType.APPLICATION_JSON)
    public Response authenticatedEndpoint() {
        User currentUser = securityProvider.getCurrentUser()
            .orElseThrow(() -> new UnauthorizedException("No authenticated user"));
            
        return Response.ok(Map.of(
            "message", "Authenticated endpoint",
            "user", currentUser.getUsername(),
            "role", currentUser.getRole()
        )).build();
    }
    
    @GET
    @Path("/admin")
    @RolesAllowed("admin")
    @Produces(MediaType.APPLICATION_JSON)
    public Response adminEndpoint() {
        User currentUser = securityProvider.getCurrentUser()
            .orElseThrow(() -> new UnauthorizedException("No authenticated user"));
            
        return Response.ok(Map.of(
            "message", "Admin endpoint",
            "user", currentUser.getUsername(),
            "adminAccess", true
        )).build();
    }
    
    @GET
    @Path("/manager")
    @RolesAllowed({"admin", "manager"})
    @Produces(MediaType.APPLICATION_JSON)
    public Response managerEndpoint() {
        User currentUser = securityProvider.getCurrentUser()
            .orElseThrow(() -> new UnauthorizedException("No authenticated user"));
            
        return Response.ok(Map.of(
            "message", "Manager endpoint",
            "user", currentUser.getUsername(),
            "managerAccess", true
        )).build();
    }
    
    @GET
    @Path("/sales")
    @RolesAllowed({"admin", "manager", "sales"})
    @Produces(MediaType.APPLICATION_JSON)
    public Response salesEndpoint() {
        User currentUser = securityProvider.getCurrentUser()
            .orElseThrow(() -> new UnauthorizedException("No authenticated user"));
            
        return Response.ok(Map.of(
            "message", "Sales endpoint",
            "user", currentUser.getUsername(),
            "salesAccess", true
        )).build();
    }
}
```

### Frontend-Architecture - Auth Integration
```typescript
// Auth Service
class AuthService {
    private keycloak: Keycloak;
    
    constructor() {
        this.keycloak = new Keycloak({
            url: import.meta.env.VITE_KEYCLOAK_URL || 'http://localhost:8180',
            realm: 'freshplan',
            clientId: 'freshplan-frontend'
        });
    }
    
    async initialize(): Promise<boolean> {
        try {
            const authenticated = await this.keycloak.init({
                onLoad: 'check-sso',
                silentCheckSsoRedirectUri: window.location.origin + '/silent-check-sso.html',
                checkLoginIframe: false
            });
            
            if (authenticated) {
                this.setupTokenRefresh();
            }
            
            return authenticated;
        } catch (error) {
            console.error('Keycloak initialization failed:', error);
            return false;
        }
    }
    
    async login(): Promise<void> {
        await this.keycloak.login({
            redirectUri: window.location.origin
        });
    }
    
    async logout(): Promise<void> {
        await this.keycloak.logout({
            redirectUri: window.location.origin
        });
    }
    
    getToken(): string | undefined {
        return this.keycloak.token;
    }
    
    async getCurrentUser(): Promise<User | null> {
        if (!this.isAuthenticated()) {
            return null;
        }
        
        try {
            const response = await apiClient.get('/api/users/me', {
                headers: { Authorization: `Bearer ${this.getToken()}` }
            });
            return response.data;
        } catch (error) {
            console.error('Failed to get current user:', error);
            return null;
        }
    }
    
    isAuthenticated(): boolean {
        return !!this.keycloak.authenticated;
    }
    
    hasRole(role: string): boolean {
        return this.keycloak.hasRealmRole(role);
    }
    
    private setupTokenRefresh(): void {
        // Auto-refresh token when it expires
        setInterval(async () => {
            try {
                const refreshed = await this.keycloak.updateToken(70);
                if (refreshed) {
                    console.log('Token refreshed');
                }
            } catch (error) {
                console.error('Token refresh failed:', error);
                await this.logout();
            }
        }, 60000); // Check every minute
    }
}

// Auth Context Provider
interface AuthContextType {
    user: User | null;
    isAuthenticated: boolean;
    isLoading: boolean;
    login: () => Promise<void>;
    logout: () => Promise<void>;
    hasRole: (role: string) => boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [user, setUser] = useState<User | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const authService = useRef(new AuthService()).current;
    
    useEffect(() => {
        initializeAuth();
    }, []);
    
    const initializeAuth = async () => {
        setIsLoading(true);
        try {
            const authenticated = await authService.initialize();
            
            if (authenticated) {
                const currentUser = await authService.getCurrentUser();
                setUser(currentUser);
            }
        } catch (error) {
            console.error('Auth initialization failed:', error);
        } finally {
            setIsLoading(false);
        }
    };
    
    const login = async () => {
        await authService.login();
    };
    
    const logout = async () => {
        await authService.logout();
        setUser(null);
    };
    
    const hasRole = (role: string): boolean => {
        return authService.hasRole(role);
    };
    
    const contextValue: AuthContextType = {
        user,
        isAuthenticated: authService.isAuthenticated(),
        isLoading,
        login,
        logout,
        hasRole
    };
    
    return (
        <AuthContext.Provider value={contextValue}>
            {children}
        </AuthContext.Provider>
    );
};

// Auth Hook
export const useAuth = (): AuthContextType => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within AuthProvider');
    }
    return context;
};

// Protected Route Component
interface ProtectedRouteProps {
    children: React.ReactNode;
    requiredRole?: string;
    fallback?: React.ReactNode;
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ 
    children, 
    requiredRole, 
    fallback 
}) => {
    const { isAuthenticated, hasRole, isLoading } = useAuth();
    
    if (isLoading) {
        return <CircularProgress />;
    }
    
    if (!isAuthenticated) {
        return fallback || <Navigate to="/login" />;
    }
    
    if (requiredRole && !hasRole(requiredRole)) {
        return fallback || <Navigate to="/unauthorized" />;
    }
    
    return <>{children}</>;
};

// API Client with Auto-Authentication
class ApiClient {
    private authService: AuthService;
    
    constructor(authService: AuthService) {
        this.authService = authService;
        this.setupInterceptors();
    }
    
    private setupInterceptors(): void {
        // Request interceptor to add auth header
        apiClient.interceptors.request.use((config) => {
            const token = this.authService.getToken();
            if (token) {
                config.headers.Authorization = `Bearer ${token}`;
            }
            return config;
        });
        
        // Response interceptor for auth errors
        apiClient.interceptors.response.use(
            (response) => response,
            async (error) => {
                if (error.response?.status === 401) {
                    await this.authService.logout();
                    window.location.href = '/login';
                }
                return Promise.reject(error);
            }
        );
    }
}
```

---

## 🔄 ABHÄNGIGKEITEN

### Benötigt diese Features:
- **KEINE** - Foundation Feature, wird von allen anderen benötigt

### Ermöglicht diese Features:
- **FC-009 Advanced Permissions** - Baut auf JWT-Auth und User-Context auf
- **M4 Opportunity Pipeline** - Benötigt User-Context für Opportunity-Zuordnung
- **FC-004 Verkäuferschutz** - User-basierte Provisionsberechnung
- **ALLE UI-Features** - Login/Logout, User-spezifische Daten

### Keycloak Setup benötigt:
- **Keycloak Server** - localhost:8180 (läuft bereits)
- **Realm Configuration** - freshplan realm
- **Client Configuration** - freshplan-frontend client

---

## 🧪 TESTING-STRATEGIE

### Unit Tests - Security Services
```java
@QuarkusTest
class SecurityContextProviderTest {
    
    @Test
    void testGetCurrentUser_withValidToken_shouldReturnUser() {
        // Given
        JsonWebToken mockJwt = createMockJwt("test-user", "admin");
        User expectedUser = createTestUser("test-user", "admin");
        
        // When
        Optional<User> result = securityProvider.getCurrentUser();
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("test-user");
        assertThat(result.get().getRole()).isEqualTo("admin");
    }
    
    @Test
    void testHasRole_withValidRole_shouldReturnTrue() {
        // Test role checking logic...
    }
    
    @Test
    void testRoleHierarchy_adminCanAccessManager_shouldReturnTrue() {
        // Test role hierarchy...
    }
}

@QuarkusTest
class AuthenticationFilterTest {
    
    @Test
    void testFilter_withoutToken_shouldReturn401() {
        // Test authentication filter...
    }
    
    @Test
    void testFilter_withValidToken_shouldAllowAccess() {
        // Test successful authentication...
    }
}
```

### Integration Tests - Security Endpoints
```java
@QuarkusTest
@TestSecurity(user = "testuser", roles = {"admin"})
class SecurityIntegrationTest {
    
    @Test
    void testAdminEndpoint_withAdminRole_shouldReturn200() {
        given()
            .when().get("/api/test/admin")
            .then()
                .statusCode(200)
                .body("message", equalTo("Admin endpoint"));
    }
    
    @Test
    @TestSecurity(user = "salesuser", roles = {"sales"})
    void testAdminEndpoint_withSalesRole_shouldReturn403() {
        given()
            .when().get("/api/test/admin")
            .then()
                .statusCode(403);
    }
    
    @Test
    void testPublicEndpoint_withoutAuth_shouldReturn200() {
        given()
            .when().get("/api/test/public")
            .then()
                .statusCode(200);
    }
}
```

### Frontend Tests
```typescript
describe('AuthProvider', () => {
    it('should initialize Keycloak on mount', async () => {
        const mockKeycloak = {
            init: jest.fn().mockResolvedValue(true),
            authenticated: true
        };
        
        render(
            <AuthProvider>
                <div>Test</div>
            </AuthProvider>
        );
        
        await waitFor(() => {
            expect(mockKeycloak.init).toHaveBeenCalled();
        });
    });
    
    it('should provide auth context to children', async () => {
        const TestComponent = () => {
            const { isAuthenticated } = useAuth();
            return <div>{isAuthenticated ? 'Authenticated' : 'Not authenticated'}</div>;
        };
        
        render(
            <AuthProvider>
                <TestComponent />
            </AuthProvider>
        );
        
        expect(screen.getByText('Authenticated')).toBeInTheDocument();
    });
});

describe('ProtectedRoute', () => {
    it('should redirect to login when not authenticated', () => {
        const mockUseAuth = {
            isAuthenticated: false,
            isLoading: false
        };
        
        jest.mocked(useAuth).mockReturnValue(mockUseAuth);
        
        render(
            <MemoryRouter>
                <ProtectedRoute>
                    <div>Protected Content</div>
                </ProtectedRoute>
            </MemoryRouter>
        );
        
        expect(screen.queryByText('Protected Content')).not.toBeInTheDocument();
    });
});
```

---

## 🚀 IMPLEMENTATION GUIDE

### Phase 1: Backend Security Foundation (3 Tage)
1. **User Entity & Repository** - User-Management mit Keycloak-Sync
2. **SecurityContextProvider** - JWT-Validation und User-Context
3. **AuthenticationFilter** - Request-Interception und Token-Validation
4. **Test Endpoints** - LÖSUNG für TODO-024/028

### Phase 2: Frontend Auth Integration (2 Tage)
1. **Keycloak Client Setup** - Frontend-Integration
2. **AuthProvider & Context** - React Auth State Management
3. **ProtectedRoute Component** - Route-based Authorization
4. **API Client Integration** - Automatic Token Handling

### Phase 3: Security Enhancements (2 Tage)
1. **Audit Logging** - Security Event Tracking
2. **Role Management** - Admin Interface für User-Verwaltung
3. **Session Management** - Optional Session-basierte Ergänzung
4. **Security Headers** - CORS, CSP, XSS Protection

---

## 🔧 TODO-024/028 LÖSUNG

### Problem: Tests deaktiviert wegen fehlender Test-Endpoints

### Lösung: TestResource implementieren
```bash
# 1. Test-Endpoints erstellen
cd backend/src/main/java/de/freshplan/api/resources
# → Code oben in Backend-Architecture verwenden

# 2. Tests wieder aktivieren
cd backend/src/test/java/de/freshplan/api
# → @Disabled entfernen aus SecurityTest.java

# 3. Tests ausführen
./mvnw test -Dtest=SecurityContextProviderIntegrationTest
```

**Nach TODO-024/028 Fix: FC-008 → 100% fertig!**

---

## 📊 SUCCESS CRITERIA

### Funktionale Anforderungen
- ✅ JWT-basierte Authentifizierung mit Keycloak
- ✅ Role-based Authorization (admin, manager, sales)
- ✅ User Context für alle Backend-Services
- ✅ Frontend Auth Integration mit Auto-Refresh
- ✅ Security Audit Logging für Compliance

### Performance-Anforderungen
- ✅ Token-Validation < 50ms P95
- ✅ User-Context Lookup < 20ms P95
- ✅ Keycloak Integration < 100ms P95
- ✅ Frontend Auth Check < 10ms
- ✅ 99.9% Uptime für Auth Services

### Security-Anforderungen
- ✅ Secure JWT Token Handling
- ✅ HTTPS-only for Production
- ✅ CORS Configuration für Frontend
- ✅ XSS und CSRF Protection
- ✅ Audit Trail für alle Security Events

---

## 🔗 NAVIGATION ZU ALLEN 40 FEATURES

### Core Sales Features
- [FC-001 Customer Acquisition](/docs/features/PLANNED/01_customer_acquisition/FC-001_TECH_CONCEPT.md) | [FC-002 Smart Customer Insights](/docs/features/PLANNED/02_smart_insights/FC-002_TECH_CONCEPT.md) | [M3 Sales Cockpit](/docs/features/ACTIVE/05_ui_foundation/M3_TECH_CONCEPT.md) | [M4 Opportunity Pipeline](/docs/features/ACTIVE/02_opportunity_pipeline/M4_TECH_CONCEPT.md)
- [FC-004 Verkäuferschutz](/docs/features/PLANNED/07_verkaeuferschutz/FC-004_TECH_CONCEPT.md) | [FC-013 Duplicate Detection](/docs/features/PLANNED/15_duplicate_detection/FC-013_TECH_CONCEPT.md) | [FC-014 Activity Timeline](/docs/features/PLANNED/16_activity_timeline/FC-014_TECH_CONCEPT.md)
- [FC-015 Deal Loss Analysis](/docs/features/PLANNED/17_deal_loss_analysis/FC-015_TECH_CONCEPT.md) | [FC-016 Opportunity Cloning](/docs/features/PLANNED/18_opportunity_cloning/FC-016_TECH_CONCEPT.md) | [FC-019 Advanced Sales Metrics](/docs/features/PLANNED/19_advanced_metrics/FC-019_TECH_CONCEPT.md)

### Security & Foundation Features  
- [FC-009 Permissions System](/docs/features/ACTIVE/04_permissions_system/FC-009_TECH_CONCEPT.md) | [FC-025 DSGVO Compliance](/docs/features/PLANNED/25_dsgvo_compliance/FC-025_TECH_CONCEPT.md) | [FC-023 Event Sourcing](/docs/features/PLANNED/23_event_sourcing/FC-023_TECH_CONCEPT.md)
- [FC-038 Multi-Tenant Architecture](/docs/features/PLANNED/38_multi_tenant/FC-038_TECH_CONCEPT.md) | [FC-039 API Gateway](/docs/features/PLANNED/39_api_gateway/FC-039_TECH_CONCEPT.md) | [FC-040 Performance Monitoring](/docs/features/PLANNED/40_performance_monitoring/FC-040_TECH_CONCEPT.md)

### Mobile & Field Service Features
- [FC-006 Mobile App](/docs/features/PLANNED/09_mobile_app/FC-006_TECH_CONCEPT.md) | [FC-018 Mobile PWA](/docs/features/PLANNED/09_mobile_app/FC-018_MOBILE_FIELD_SALES.md) | [FC-022 Mobile Light](/docs/features/PLANNED/22_mobile_light/FC-022_TECH_CONCEPT.md)
- [FC-029 Voice-First Interface](/docs/features/PLANNED/29_voice_first/FC-029_TECH_CONCEPT.md) | [FC-030 One-Tap Actions](/docs/features/PLANNED/30_one_tap_actions/FC-030_TECH_CONCEPT.md) | [FC-032 Offline-First](/docs/features/PLANNED/32_offline_first/FC-032_TECH_CONCEPT.md)

### Communication Features
- [FC-003 E-Mail Integration](/docs/features/PLANNED/06_email_integration/FC-003_TECH_CONCEPT.md) | [FC-012 Team Communication](/docs/features/PLANNED/14_team_communication/FC-012_TECH_CONCEPT.md) | [FC-028 WhatsApp Business](/docs/features/PLANNED/28_whatsapp_integration/FC-028_TECH_CONCEPT.md)
- [FC-035 Social Selling Helper](/docs/features/PLANNED/35_social_selling/FC-035_TECH_CONCEPT.md)

### Analytics & Intelligence Features
- [M6 Analytics Module](/docs/features/PLANNED/13_analytics_m6/M6_TECH_CONCEPT.md) | [FC-007 Chef-Dashboard](/docs/features/PLANNED/10_chef_dashboard/FC-007_TECH_CONCEPT.md) | [FC-026 Analytics Platform](/docs/features/PLANNED/26_analytics_platform/FC-026_TECH_CONCEPT.md)
- [FC-034 Instant Insights](/docs/features/PLANNED/34_instant_insights/FC-034_TECH_CONCEPT.md) | [FC-037 Advanced Reporting](/docs/features/PLANNED/37_advanced_reporting/FC-037_TECH_CONCEPT.md)

### Integration & Infrastructure Features
- [FC-005 Xentral Integration](/docs/features/PLANNED/08_xentral_integration/FC-005_TECH_CONCEPT.md) | [FC-010 Customer Import](/docs/features/PLANNED/11_customer_import/FC-010_TECH_CONCEPT.md) | [FC-021 Integration Hub](/docs/features/PLANNED/21_integration_hub/FC-021_TECH_CONCEPT.md)
- [FC-024 File Management](/docs/features/PLANNED/24_file_management/FC-024_TECH_CONCEPT.md) | [M5 Customer Refactor](/docs/features/PLANNED/12_customer_refactor_m5/M5_TECH_CONCEPT.md)

### UI & Productivity Features
- [M1 Navigation](/docs/features/ACTIVE/05_ui_foundation/M1_TECH_CONCEPT.md) | [M2 Quick Create](/docs/features/ACTIVE/05_ui_foundation/M2_TECH_CONCEPT.md) | [M7 Settings](/docs/features/ACTIVE/05_ui_foundation/M7_TECH_CONCEPT.md)
- [FC-020 Quick Wins](/docs/features/PLANNED/20_quick_wins/FC-020_TECH_CONCEPT.md) | [FC-031 Smart Templates](/docs/features/PLANNED/31_smart_templates/FC-031_TECH_CONCEPT.md) | [FC-033 Visual Customer Cards](/docs/features/PLANNED/33_visual_cards/FC-033_TECH_CONCEPT.md)

### Advanced Features
- [FC-011 Bonitätsprüfung](/docs/features/ACTIVE/02_opportunity_pipeline/integrations/FC-011_TECH_CONCEPT.md) | [FC-017 Sales Gamification](/docs/features/PLANNED/99_sales_gamification/FC-017_TECH_CONCEPT.md) | [FC-027 Magic Moments](/docs/features/PLANNED/27_magic_moments/FC-027_TECH_CONCEPT.md)
- [FC-036 Beziehungsmanagement](/docs/features/PLANNED/36_relationship_mgmt/FC-036_TECH_CONCEPT.md)

---

**🔒 FC-008 Security Foundation - Ready for Implementation!**  
**⏱️ Geschätzter Aufwand:** 5-7 Tage | **Priorität:** KRITISCH | **Blockiert:** Alle anderen Features