# 📆 Tag 2: Crypto-Shredding Implementation

**Datum:** Dienstag, 13. August 2025  
**Fokus:** Verschlüsselung & DSGVO Löschung  
**Ziel:** Crypto-Shredding für "Recht auf Vergessenwerden"  

## 🧭 Navigation

**← Vorheriger Tag:** [Tag 1: Consent](./DAY1_CONSENT.md)  
**↑ Woche 2 Übersicht:** [README.md](./README.md)  
**→ Nächster Tag:** [Tag 3: Mobile](./DAY3_MOBILE.md)  
**📘 Spec:** [Crypto Specification](./specs/CRYPTO_SPEC.md)  

## 🎯 Tagesziel

- Backend: Encryption Service mit Key Management
- Schema: Key Storage Tables
- Integration: Event Encryption
- Testing: Crypto-Shredding verifizieren

## 🔐 Crypto-Shredding Konzept

```
Personal Data → Encrypt (AES-256) → Encrypted Data + Key ID
                        ↓
                    Key Store
                        ↓
DSGVO Delete Request → Delete Key → Data unlesbar!
```

## 💻 Backend Implementation

### 1. Encryption Service

```java
// CryptoShredService.java
@ApplicationScoped
public class CryptoShredService {
    
    @Inject
    KeyManagementService keyService;
    
    // Encrypt personal data
    public EncryptedData encrypt(PersonalData data) {
        String keyId = keyService.generateKey();
        SecretKey key = keyService.getKey(keyId);
        
        String encrypted = AESUtil.encrypt(JsonUtil.toJson(data), key);
        
        return EncryptedData.builder()
            .keyId(keyId)
            .encryptedPayload(encrypted)
            .algorithm("AES-256-GCM")
            .build();
    }
    
    // Decrypt personal data
    public PersonalData decrypt(EncryptedData encrypted) {
        SecretKey key = keyService.getKey(encrypted.getKeyId());
        if (key == null) {
            // Key was shredded - data is permanently gone
            throw new DataShreddedException("Personal data has been deleted");
        }
        
        String json = AESUtil.decrypt(encrypted.getEncryptedPayload(), key);
        return JsonUtil.fromJson(json, PersonalData.class);
    }
    
    // GDPR deletion - just delete the key
    public void shredData(String keyId) {
        keyService.deleteKey(keyId);
        
        // Audit log the deletion
        auditService.log(DataShreddedEvent.builder()
            .keyId(keyId)
            .timestamp(Instant.now())
            .reason("GDPR deletion request")
            .build());
    }
}
```

**Vollständiger Code:** [backend/CryptoShredService.java](./code/backend/CryptoShredService.java)

### 2. Key Storage Schema

```sql
-- Encryption keys table
CREATE TABLE encryption_keys (
  key_id UUID PRIMARY KEY,
  encrypted_key TEXT NOT NULL, -- Key encrypted with master key
  algorithm VARCHAR(50) NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  expires_at TIMESTAMP WITH TIME ZONE,
  status VARCHAR(20) DEFAULT 'active'
);

-- Audit table for key operations
CREATE TABLE key_audit_log (
  audit_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  key_id UUID NOT NULL,
  operation VARCHAR(50) NOT NULL, -- created, accessed, deleted
  user_id UUID NOT NULL,
  timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  reason TEXT
);
```

**Migration:** [V8__crypto_shredding.sql](./code/migrations/V8__crypto_shredding.sql)

## 🔒 Key Management

### Master Key Rotation

```java
// KeyRotationService.java
@Scheduled(every = "90d")
void rotateMasterKey() {
    // 1. Generate new master key
    SecretKey newMasterKey = generateMasterKey();
    
    // 2. Re-encrypt all active keys
    List<EncryptionKey> activeKeys = keyRepository.findActive();
    for (EncryptionKey key : activeKeys) {
        // Decrypt with old master
        SecretKey decrypted = decryptWithOldMaster(key);
        
        // Re-encrypt with new master
        String reencrypted = encryptWithNewMaster(decrypted, newMasterKey);
        key.setEncryptedKey(reencrypted);
        
        keyRepository.update(key);
    }
    
    // 3. Activate new master key
    activateNewMasterKey(newMasterKey);
}
```

## 🧪 Tests

### Crypto-Shredding Test

```java
@Test
void shouldMakeDataUnreadableAfterKeyDeletion() {
    // Given - Encrypt personal data
    PersonalData original = createTestPersonalData();
    EncryptedData encrypted = cryptoService.encrypt(original);
    
    // When - Delete the key (GDPR request)
    cryptoService.shredData(encrypted.getKeyId());
    
    // Then - Data should be unreadable
    assertThrows(DataShreddedException.class, () -> {
        cryptoService.decrypt(encrypted);
    });
}
```

**Alle Tests:** [tests/crypto/](./code/tests/crypto/)

## 🔐 Security Best Practices

1. **Key Storage**
   - Keys niemals im Klartext speichern
   - Master Key in HSM oder Key Vault
   - Separate Key-Datenbank möglich

2. **Audit Trail**
   - Jede Key-Operation loggen
   - Wer hat wann welchen Key gelöscht
   - Compliance-Reports generieren

3. **Performance**
   - Keys cachen (mit TTL)
   - Batch-Verschlüsselung
   - Async Key-Deletion

## 📝 Checkliste

- [ ] CryptoShredService implementiert
- [ ] Key Management Service erstellt
- [ ] Database Schema migriert
- [ ] Encryption/Decryption getestet
- [ ] Key Deletion verifiziert
- [ ] Audit Logging funktioniert
- [ ] Performance optimiert

## 🔗 Weiterführende Links

- **Crypto Patterns:** [Crypto-Shredding Best Practices](./guides/CRYPTO_SHREDDING_GUIDE.md)
- **DSGVO Details:** [GDPR Article 17 Implementation](./guides/GDPR_ARTICLE_17.md)
- **Nächster Schritt:** [→ Tag 3: Mobile Actions](./DAY3_MOBILE.md)

---

**Status:** 📋 Bereit zur Implementierung