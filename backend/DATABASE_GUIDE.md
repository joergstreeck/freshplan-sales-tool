# Database Guide - Backend

**Datenbank:** PostgreSQL 15+  
**Migration Tool:** Flyway

## 📊 Aktuelles Schema

### Users Table
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index für Performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
```

### User Roles Table
```sql
CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role)
);

-- Erlaubte Rollen: admin, manager, sales
```

## 🔄 Flyway Migrations

### Neue Migration erstellen
```bash
# Dateiname Format: V{version}__{description}.sql
# Beispiel: V4__add_customer_table.sql

# Speicherort:
src/main/resources/db/migration/
```

### Migration Beispiel
```sql
-- V4__add_customer_table.sql
CREATE TABLE customers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_name VARCHAR(255) NOT NULL,
    contact_person VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(50),
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Migrations ausführen
```bash
# Automatisch beim Start:
./mvnw quarkus:dev

# Manuell:
./mvnw flyway:migrate

# Status prüfen:
./mvnw flyway:info
```

## 🛠️ Lokale Entwicklung

### PostgreSQL starten
```bash
# Mit Docker Compose:
cd ../infrastructure
./start-local-env.sh

# Oder direkt:
docker run -d \
  --name freshplan-postgres \
  -e POSTGRES_USER=freshplan \
  -e POSTGRES_PASSWORD=freshplan123 \
  -e POSTGRES_DB=freshplan \
  -p 5432:5432 \
  postgres:15-alpine
```

### Datenbank-Verbindung
```properties
# application.properties
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/freshplan
quarkus.datasource.username=freshplan
quarkus.datasource.password=freshplan123
```

### pgAdmin oder DBeaver
```
Host: localhost
Port: 5432
Database: freshplan
User: freshplan
Password: freshplan123
```

## 📝 JPA/Panache Tipps

### Entity mit Panache
```java
@Entity
@Table(name = "users")
public class User extends PanacheEntityBase {
    @Id
    @GeneratedValue
    public UUID id;
    
    @Column(unique = true, nullable = false)
    public String username;
    
    // Panache Active Record Pattern
    public static User findByUsername(String username) {
        return find("username", username).firstResult();
    }
}
```

### Repository Pattern
```java
@ApplicationScoped
public class UserRepository implements PanacheRepositoryBase<User, UUID> {
    
    public Optional<User> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }
    
    public List<User> findActiveUsers() {
        return list("enabled", true);
    }
}
```

### Transaktionen
```java
@Transactional
public User createUser(CreateUserRequest request) {
    // Alles in dieser Methode ist eine Transaktion
    User user = new User();
    user.username = request.username;
    user.persist(); // oder repository.persist(user)
    return user;
}
```

## 🐛 Häufige Probleme

### 1. "Table already exists"
Migration wurde schon ausgeführt. Check:
```sql
SELECT * FROM flyway_schema_history;
```

### 2. "Unique constraint violation"
Doppelte Daten. Test-Daten löschen:
```sql
TRUNCATE TABLE users CASCADE;
```

### 3. Connection refused
PostgreSQL läuft nicht:
```bash
docker ps # Check ob Container läuft
cd ../infrastructure && ./start-local-env.sh
```

### 4. Lazy Loading Exception
```java
// Schlecht:
return user; // roles werden später geladen → Exception

// Gut:
return User.findById(id)
    .fetch("roles") // Eager fetch
    .firstResult();
```

## 🧪 Test-Datenbank

### H2 für Tests
```properties
# application-test.properties
quarkus.datasource.db-kind=h2
quarkus.datasource.jdbc.url=jdbc:h2:mem:test
```

### Test-Daten
```java
@QuarkusTest
class UserServiceTest {
    @BeforeEach
    @Transactional
    void setupTestData() {
        User.deleteAll(); // Clean slate
        
        User admin = new User();
        admin.username = "test.admin";
        admin.persist();
    }
}
```

## 📚 Nützliche Queries

### User mit Rollen
```sql
SELECT u.*, array_agg(ur.role) as roles
FROM users u
LEFT JOIN user_roles ur ON u.id = ur.user_id
GROUP BY u.id;
```

### Aktivitäts-Report
```sql
SELECT 
    DATE(created_at) as day,
    COUNT(*) as new_users
FROM users
WHERE created_at > NOW() - INTERVAL '30 days'
GROUP BY DATE(created_at)
ORDER BY day DESC;
```