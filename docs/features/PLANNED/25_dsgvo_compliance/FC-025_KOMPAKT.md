# 🔒 FC-025 DSGVO COMPLIANCE TOOLKIT (KOMPAKT)

**Erstellt:** 18.07.2025  
**Status:** 📋 READY TO START  
**Feature-Typ:** 🔀 FULLSTACK  
**Priorität:** HIGH - Rechtssicherheit  
**Geschätzt:** 2 Tage  

---

## 🧠 WAS WIR BAUEN

**Problem:** DSGVO-Anforderungen nicht erfüllt  
**Lösung:** Compliance-Tools von Anfang an  
**Value:** Rechtssicherheit & Vertrauen  

> **Business Case:** Bußgelder vermeiden, Kundenvertrauen gewinnen

### 🎯 DSGVO Features:
- **Right to be Forgotten:** Lösch-Mechanismen
- **Data Portability:** Export-Funktionen
- **Access Logs:** Wer hat was gesehen
- **Consent Management:** Einwilligungen

---

## 🚀 SOFORT LOSLEGEN (15 MIN)

### 1. **Audit Tables:**
```sql
-- V6.0__create_audit_tables.sql
CREATE TABLE data_access_log (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    entity_type VARCHAR(50),
    entity_id UUID,
    action VARCHAR(20), -- VIEW, EDIT, DELETE
    ip_address VARCHAR(45),
    user_agent TEXT,
    accessed_at TIMESTAMP
);

CREATE TABLE deletion_requests (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    requested_at TIMESTAMP,
    processed_at TIMESTAMP,
    status VARCHAR(20) -- PENDING, COMPLETED
);
```

### 2. **Audit Interceptor:**
```java
@Interceptor
@Audited
public class AuditInterceptor {
    @AroundInvoke
    public Object audit(InvocationContext ctx) {
        // Log data access
    }
}
```

### 3. **DSGVO Service:**
```java
@ApplicationScoped
public class DsgvoService {
    public void deleteCustomerData(UUID customerId);
    public CustomerDataExport exportData(UUID customerId);
    public AccessReport getAccessReport(UUID customerId);
}
```

---

## 🗑️ RIGHT TO BE FORGOTTEN

### Deletion Strategy:
```java
@Transactional
public void deleteCustomerData(UUID customerId) {
    // 1. Soft delete customer
    customer.setDeleted(true);
    customer.setDeletionDate(now());
    
    // 2. Anonymize personal data
    customer.setName("DELETED");
    customer.setEmail(null);
    customer.setPhone(null);
    
    // 3. Delete related data
    fileService.deleteCustomerFiles(customerId);
    activityService.anonymizeActivities(customerId);
    
    // 4. Keep for legal retention (Buchhaltung)
    retentionService.scheduleHardDelete(
        customerId, 
        plusYears(10) // GoBD
    );
}
```

### UI Deletion Request:
```typescript
<Dialog open={deleteDialogOpen}>
  <DialogTitle>
    ⚠️ Kunde löschen (DSGVO)
  </DialogTitle>
  <DialogContent>
    <Alert severity="warning">
      Diese Aktion kann nicht rückgängig 
      gemacht werden. Alle personenbezogenen 
      Daten werden gelöscht.
    </Alert>
    
    <FormControlLabel
      control={<Checkbox />}
      label="Buchhaltungsdaten behalten (GoBD)"
    />
  </DialogContent>
  <DialogActions>
    <Button onClick={confirmDelete} color="error">
      Endgültig löschen
    </Button>
  </DialogActions>
</Dialog>
```

---

## 📤 DATA EXPORT

### Export Format:
```json
{
  "exportDate": "2025-07-18",
  "customer": {
    "id": "...",
    "personalData": {...},
    "activities": [...],
    "opportunities": [...],
    "files": [...]
  },
  "accessLog": [
    {
      "date": "2025-07-17",
      "user": "Max Mustermann",
      "action": "VIEW"
    }
  ]
}
```

### Export API:
```java
@GET
@Path("/customers/{id}/export")
@Produces("application/zip")
public Response exportCustomerData(
    @PathParam("id") UUID customerId
) {
    // 1. Collect all data
    // 2. Create JSON + PDFs
    // 3. ZIP everything
    // 4. Audit log export
}
```

---

## 👁️ ACCESS TRACKING

### Automatic Logging:
```java
@Entity
@EntityListeners(AccessLogListener.class)
public class Customer {
    // Automatic access logging
}

public class AccessLogListener {
    @PostLoad
    void onLoad(Object entity) {
        auditService.logAccess(
            entity.getClass(),
            getId(entity),
            "VIEW"
        );
    }
}
```

### Access Report UI:
```
┌─────────────────────────────────────┐
│ Zugriffs-Historie: Müller GmbH     │
├─────────────────────────────────────┤
│ Datum    | Nutzer      | Aktion    │
│ 18.07.   | M. Schmidt  | Angesehen │
│ 17.07.   | S. Meyer    | Bearbeitet│
│ 15.07.   | M. Schmidt  | Exportiert│
└─────────────────────────────────────┘
```

---

## 🎯 COMPLIANCE DASHBOARD

```typescript
<ComplianceDashboard>
  <MetricCard
    title="Löschanfragen"
    value={pendingDeletions}
    icon={<DeleteIcon />}
    action="Bearbeiten"
  />
  
  <MetricCard
    title="Datenexporte"
    value={monthlyExports}
    icon={<DownloadIcon />}
    trend="+15%"
  />
  
  <MetricCard
    title="Zugriffsberichte"
    value={accessReports}
    icon={<VisibilityIcon />}
  />
  
  <ConsentOverview>
    {/* Cookie Consent Status */}
    {/* Email Marketing Consent */}
    {/* Data Processing Agreements */}
  </ConsentOverview>
</ComplianceDashboard>
```

---

## 📞 NÄCHSTE SCHRITTE

1. **Audit Tables** erstellen
2. **Access Logging** implementieren
3. **Deletion Service** + UI
4. **Export Function** + ZIP
5. **Compliance Dashboard**
6. **Retention Policies**

**WICHTIG:** Auch Event Store berücksichtigen!