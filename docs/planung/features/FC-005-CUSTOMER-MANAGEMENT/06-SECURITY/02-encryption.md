# 🔐 FC-005 SECURITY - ENCRYPTION & TECHNICAL MEASURES

**Navigation:**
- **Parent:** [06-SECURITY](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/06-SECURITY/README.md)
- **Prev:** [01-dsgvo-compliance.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/06-SECURITY/01-dsgvo-compliance.md)
- **Next:** [03-permissions.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/06-SECURITY/03-permissions.md)

---

## Field-Level Verschlüsselung

### Encryption Service

```java
@Component
public class FieldEncryptionService {
    
    @Value("${encryption.key}")
    private String encryptionKey;
    
    private SecretKeySpec secretKey;
    private final String ALGORITHM = "AES/GCM/NoPadding";
    private final int GCM_IV_LENGTH = 12;
    private final int GCM_TAG_LENGTH = 128;
    
    @PostConstruct
    public void init() throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(encryptionKey);
        secretKey = new SecretKeySpec(decodedKey, "AES");
    }
    
    public String encryptFieldValue(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        
        try {
            // Generate random IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            SecureRandom.getInstanceStrong().nextBytes(iv);
            
            // Initialize cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);
            
            // Encrypt
            byte[] encrypted = cipher.doFinal(
                plainText.getBytes(StandardCharsets.UTF_8)
            );
            
            // Combine IV and encrypted data
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
            
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new EncryptionException("Failed to encrypt field value", e);
        }
    }
    
    public String decryptFieldValue(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }
        
        try {
            byte[] combined = Base64.getDecoder().decode(encryptedText);
            
            // Extract IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, iv.length);
            
            // Extract encrypted data
            byte[] encrypted = new byte[combined.length - iv.length];
            System.arraycopy(combined, iv.length, encrypted, 0, encrypted.length);
            
            // Initialize cipher for decryption
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
            
            // Decrypt
            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new EncryptionException("Failed to decrypt field value", e);
        }
    }
}

// Automatic encryption for sensitive fields
@Component
public class FieldValueEncryptionInterceptor {
    
    @Inject
    FieldEncryptionService encryptionService;
    
    @Inject
    FieldDefinitionService fieldDefinitionService;
    
    public FieldValue encryptIfRequired(FieldValue fieldValue) {
        FieldDefinition definition = fieldDefinitionService
            .getDefinition(fieldValue.getFieldKey());
            
        if (definition.isEncryptionRequired()) {
            String encrypted = encryptionService.encryptFieldValue(
                fieldValue.getValue().toString()
            );
            fieldValue.setValue(encrypted);
            fieldValue.setEncrypted(true);
        }
        
        return fieldValue;
    }
    
    public FieldValue decryptIfRequired(FieldValue fieldValue) {
        if (fieldValue.isEncrypted()) {
            String decrypted = encryptionService.decryptFieldValue(
                fieldValue.getValue().toString()
            );
            fieldValue.setValue(decrypted);
        }
        
        return fieldValue;
    }
}
```

---

## Database-Level Encryption

### PostgreSQL Transparent Data Encryption

```sql
-- Enable pgcrypto extension
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Create encryption key table
CREATE TABLE IF NOT EXISTS encryption_keys (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    key_name VARCHAR(255) UNIQUE NOT NULL,
    key_value TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    rotated_at TIMESTAMP,
    is_active BOOLEAN DEFAULT true
);

-- Encrypt sensitive columns
CREATE OR REPLACE FUNCTION encrypt_sensitive_data()
RETURNS TRIGGER AS $$
DECLARE
    encryption_key TEXT;
BEGIN
    -- Get active encryption key
    SELECT key_value INTO encryption_key
    FROM encryption_keys
    WHERE key_name = 'field_values_key' AND is_active = true
    LIMIT 1;
    
    -- Encrypt sensitive fields
    IF NEW.field_definition_id IN (
        'contactEmail', 'contactPhone', 'taxId', 
        'bankAccount', 'creditLimit'
    ) THEN
        NEW.value = pgp_sym_encrypt(
            NEW.value::text, 
            encryption_key,
            'cipher-algo=aes256'
        )::jsonb;
        NEW.is_encrypted = true;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger for automatic encryption
CREATE TRIGGER encrypt_field_values_trigger
    BEFORE INSERT OR UPDATE ON field_values
    FOR EACH ROW
    EXECUTE FUNCTION encrypt_sensitive_data();

-- Decrypt function for reading
CREATE OR REPLACE FUNCTION decrypt_field_value(
    encrypted_value TEXT,
    field_key VARCHAR
) RETURNS TEXT AS $$
DECLARE
    encryption_key TEXT;
    decrypted TEXT;
BEGIN
    -- Only decrypt if it's a sensitive field
    IF field_key NOT IN (
        'contactEmail', 'contactPhone', 'taxId', 
        'bankAccount', 'creditLimit'
    ) THEN
        RETURN encrypted_value;
    END IF;
    
    -- Get encryption key
    SELECT key_value INTO encryption_key
    FROM encryption_keys
    WHERE key_name = 'field_values_key' AND is_active = true
    LIMIT 1;
    
    -- Decrypt
    decrypted = pgp_sym_decrypt(
        encrypted_value::bytea,
        encryption_key
    );
    
    RETURN decrypted;
EXCEPTION
    WHEN OTHERS THEN
        -- Log decryption failure
        RAISE WARNING 'Decryption failed for field %: %', field_key, SQLERRM;
        RETURN NULL;
END;
$$ LANGUAGE plpgsql;
```

### Backup Encryption

```bash
#!/bin/bash
# backup-encrypted.sh

# Backup with encryption
pg_dump freshplan_db | \
    openssl enc -aes-256-cbc \
    -salt \
    -pass file:/etc/freshplan/backup.key \
    -out /backups/freshplan_$(date +%Y%m%d_%H%M%S).sql.enc

# Restore encrypted backup
openssl enc -d -aes-256-cbc \
    -pass file:/etc/freshplan/backup.key \
    -in /backups/freshplan_backup.sql.enc | \
    psql freshplan_db
```

---

## Transport Security

### HTTPS Configuration

```yaml
# application.yml
quarkus:
  http:
    ssl:
      certificate:
        file: ${SSL_CERT_FILE:/etc/ssl/certs/freshplan.crt}
        key-file: ${SSL_KEY_FILE:/etc/ssl/private/freshplan.key}
      
      # TLS Configuration
      protocols: TLSv1.3,TLSv1.2
      cipher-suites:
        - TLS_AES_256_GCM_SHA384          # TLS 1.3
        - TLS_AES_128_GCM_SHA256          # TLS 1.3
        - TLS_CHACHA20_POLY1305_SHA256    # TLS 1.3
        - TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384  # TLS 1.2
        - TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256  # TLS 1.2
  
    # Redirect HTTP to HTTPS
    insecure-requests: REDIRECT
    redirect-to-https: true
    
    # HSTS
    headers:
      Strict-Transport-Security: "max-age=31536000; includeSubDomains; preload"
```

### Security Headers

```java
@Provider
public class SecurityHeadersFilter implements ContainerResponseFilter {
    
    @Override
    public void filter(
        ContainerRequestContext requestContext,
        ContainerResponseContext responseContext
    ) {
        MultivaluedMap<String, Object> headers = responseContext.getHeaders();
        
        // Prevent Clickjacking
        headers.add("X-Frame-Options", "DENY");
        headers.add("Content-Security-Policy", "frame-ancestors 'none'");
        
        // Prevent MIME type sniffing
        headers.add("X-Content-Type-Options", "nosniff");
        
        // XSS Protection (for older browsers)
        headers.add("X-XSS-Protection", "1; mode=block");
        
        // Referrer Policy
        headers.add("Referrer-Policy", "strict-origin-when-cross-origin");
        
        // Feature Policy
        headers.add("Permissions-Policy", 
            "geolocation=(), microphone=(), camera=()");
        
        // Content Security Policy
        headers.add("Content-Security-Policy", buildCSP());
    }
    
    private String buildCSP() {
        return String.join("; ",
            "default-src 'self'",
            "script-src 'self' 'unsafe-inline' 'unsafe-eval'", // For React
            "style-src 'self' 'unsafe-inline'",
            "img-src 'self' data: https:",
            "font-src 'self'",
            "connect-src 'self' wss://localhost:* https://api.freshplan.de",
            "frame-src 'none'",
            "object-src 'none'",
            "base-uri 'self'",
            "form-action 'self'",
            "upgrade-insecure-requests"
        );
    }
}
```

---

## Key Management

### Key Rotation Strategy

```java
@ApplicationScoped
@Scheduled(every = "90d") // Every 90 days
public class EncryptionKeyRotationService {
    
    @Inject
    EncryptionKeyRepository keyRepository;
    
    @Inject
    FieldValueRepository fieldValueRepository;
    
    @Transactional
    public void rotateEncryptionKeys() {
        Log.info("Starting encryption key rotation");
        
        // Generate new key
        String newKey = generateSecureKey();
        EncryptionKey newKeyEntity = new EncryptionKey();
        newKeyEntity.setKeyName("field_values_key");
        newKeyEntity.setKeyValue(newKey);
        newKeyEntity.setActive(false);
        keyRepository.persist(newKeyEntity);
        
        // Get current active key
        EncryptionKey currentKey = keyRepository
            .findActiveByName("field_values_key")
            .orElseThrow();
        
        // Re-encrypt all sensitive data
        int batchSize = 1000;
        int offset = 0;
        
        while (true) {
            List<FieldValue> batch = fieldValueRepository
                .findEncryptedValues(offset, batchSize);
                
            if (batch.isEmpty()) break;
            
            for (FieldValue fv : batch) {
                // Decrypt with old key
                String decrypted = decrypt(fv.getValue(), currentKey.getKeyValue());
                
                // Encrypt with new key
                String encrypted = encrypt(decrypted, newKey);
                
                fv.setValue(encrypted);
            }
            
            offset += batchSize;
        }
        
        // Activate new key
        currentKey.setActive(false);
        currentKey.setRotatedAt(LocalDateTime.now());
        newKeyEntity.setActive(true);
        
        Log.info("Encryption key rotation completed");
    }
    
    private String generateSecureKey() {
        byte[] key = new byte[32]; // 256 bits
        SecureRandom.getInstanceStrong().nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }
}
```

### Key Storage Security

```yaml
# Kubernetes Secret for encryption keys
apiVersion: v1
kind: Secret
metadata:
  name: freshplan-encryption-keys
  namespace: production
type: Opaque
data:
  master-key: <base64-encoded-key>
  field-encryption-key: <base64-encoded-key>
  backup-encryption-key: <base64-encoded-key>
---
# Use AWS KMS for key encryption
apiVersion: v1
kind: ConfigMap
metadata:
  name: encryption-config
data:
  kms-key-id: "arn:aws:kms:eu-central-1:123456789:key/abc-def"
  enable-envelope-encryption: "true"
```

---

## Frontend Security

### Secure Data Handling

```typescript
// utils/secureStorage.ts
export class SecureStorage {
  private static readonly ENCRYPTION_KEY = process.env.REACT_APP_CLIENT_KEY;
  
  static setItem(key: string, value: any, encrypt = true): void {
    const stringValue = JSON.stringify(value);
    
    if (encrypt && this.shouldEncrypt(key)) {
      const encrypted = CryptoJS.AES.encrypt(
        stringValue, 
        this.ENCRYPTION_KEY
      ).toString();
      sessionStorage.setItem(key, encrypted);
    } else {
      sessionStorage.setItem(key, stringValue);
    }
  }
  
  static getItem(key: string, decrypt = true): any {
    const item = sessionStorage.getItem(key);
    if (!item) return null;
    
    try {
      if (decrypt && this.shouldEncrypt(key)) {
        const decrypted = CryptoJS.AES.decrypt(
          item, 
          this.ENCRYPTION_KEY
        ).toString(CryptoJS.enc.Utf8);
        return JSON.parse(decrypted);
      }
      return JSON.parse(item);
    } catch (e) {
      console.error('Failed to decrypt/parse item', e);
      return null;
    }
  }
  
  private static shouldEncrypt(key: string): boolean {
    const sensitiveKeys = [
      'customerData',
      'personalInfo',
      'sessionToken'
    ];
    return sensitiveKeys.some(k => key.includes(k));
  }
}
```

### XSS Prevention

```typescript
// utils/sanitizer.ts
import DOMPurify from 'dompurify';

export const sanitizeInput = (input: string): string => {
  return DOMPurify.sanitize(input, {
    ALLOWED_TAGS: [],
    ALLOWED_ATTR: []
  });
};

export const sanitizeHTML = (html: string): string => {
  return DOMPurify.sanitize(html, {
    ALLOWED_TAGS: ['b', 'i', 'em', 'strong', 'p', 'br'],
    ALLOWED_ATTR: []
  });
};

// Use in components
const CustomerField: React.FC<{ value: string }> = ({ value }) => {
  const sanitized = sanitizeInput(value);
  return <TextField value={sanitized} />;
};
```