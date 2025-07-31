# 📆 Tag 1: FC-012 Audit Trail Integration

**Datum:** Montag, 26. August 2025  
**Fokus:** Audit Trail Modul Integration  
**Ziel:** Lückenlose Audit-Fähigkeit für Compliance  

## 🧭 Navigation

**↑ Woche 4 Übersicht:** [README.md](./README.md)  
**→ Nächster Tag:** [Tag 2: DSGVO](./DAY2_DSGVO.md)  
**📘 Spec:** [Audit Specification](./specs/AUDIT_SPEC.md)  
**🔗 FC-012:** [Audit Trail Module](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-012-audit-trail.md)  

## 🎯 Tagesziel

- Backend: Audit Integration Service
- Events: Alle Contact Events loggen
- Frontend: Audit Trail Viewer
- Security: Sensible Daten maskieren

## 📄 Audit Architecture

```
Contact Event → Audit Mapper → Audit Service (FC-012)
      │                                   │
      │                            ┌──────┴──────┐
      │                            │ Audit Store │
      │                            └──────┬──────┘
      │                                   │
      └─────────→ Event Store ←────────┘
```

## 💻 Backend Implementation

### 1. Audit Integration Service

```java
// AuditIntegrationService.java
@ApplicationScoped
public class AuditIntegrationService {
    
    @Inject
    @Channel("audit-events")
    Emitter<AuditEvent> auditEmitter;
    
    @Inject
    SecurityContext securityContext;
    
    // Listen to all contact events and create audit entries
    @Incoming("contact-events")
    @Acknowledgment(Acknowledgment.Strategy.POST_PROCESSING)
    public CompletionStage<Void> processContactEvent(BaseEvent event) {
        // Map to audit event
        AuditEvent auditEvent = mapToAuditEvent(event);
        
        // Send to audit module
        return auditEmitter.send(auditEvent)
            .thenApply(v -> null);
    }
    
    private AuditEvent mapToAuditEvent(BaseEvent event) {
        return AuditEvent.builder()
            .eventId(UUID.randomUUID())
            .timestamp(event.getCreatedAt())
            .userId(event.getMetadata().getUserId())
            .userName(getUserName(event.getMetadata().getUserId()))
            .entityType("Contact")
            .entityId(event.getAggregateId().toString())
            .action(mapEventTypeToAction(event))
            .details(buildAuditDetails(event))
            .ipAddress(event.getMetadata().getIpAddress())
            .userAgent(event.getMetadata().getUserAgent())
            .result("SUCCESS")
            .build();
    }
}
```

**Vollständiger Code:** [backend/AuditIntegrationService.java](./code/backend/AuditIntegrationService.java)

### 2. Audit Action Mapping

```java
private AuditAction mapEventTypeToAction(BaseEvent event) {
    return switch (event.getClass().getSimpleName()) {
        case "ContactCreatedEvent" -> AuditAction.CREATE;
        case "ContactUpdatedEvent" -> AuditAction.UPDATE;
        case "ContactDeletedEvent" -> AuditAction.DELETE;
        case "ConsentGrantedEvent" -> AuditAction.CONSENT_GRANTED;
        case "ConsentRevokedEvent" -> AuditAction.CONSENT_REVOKED;
        case "DataExportedEvent" -> AuditAction.DATA_EXPORTED;
        default -> AuditAction.OTHER;
    };
}

private Map<String, Object> buildAuditDetails(BaseEvent event) {
    Map<String, Object> details = new HashMap<>();
    
    // Add event-specific details
    if (event instanceof ContactUpdatedEvent updated) {
        details.put("changedFields", updated.getChangedFields());
        details.put("oldValues", sanitizePersonalData(updated.getOldValues()));
        details.put("newValues", sanitizePersonalData(updated.getNewValues()));
    } else if (event instanceof ConsentGrantedEvent consent) {
        details.put("consentType", consent.getConsentType());
        details.put("basis", consent.getBasis());
        details.put("expiresAt", consent.getExpiresAt());
    }
    
    return details;
}
```

### 3. Data Sanitization

```java
// Remove sensitive data from audit logs
private Map<String, Object> sanitizePersonalData(Map<String, Object> data) {
    Map<String, Object> sanitized = new HashMap<>(data);
    
    // Hash sensitive fields
    List<String> sensitiveFields = List.of("email", "phone", "mobile", "birthday");
    for (String field : sensitiveFields) {
        if (sanitized.containsKey(field)) {
            sanitized.put(field, "[REDACTED-" + hashValue(sanitized.get(field)) + "]" );
        }
    }
    
    return sanitized;
}

private String hashValue(Object value) {
    if (value == null) return "NULL";
    return DigestUtils.sha256Hex(value.toString()).substring(0, 8);
}
```

### 4. Audit Query API

```java
// AuditQueryResource.java
@Path("/api/contacts/{contactId}/audit")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public class ContactAuditResource {
    
    @Inject
    AuditQueryService auditService;
    
    @GET
    @RolesAllowed({"admin", "manager"})
    public Response getAuditTrail(
            @PathParam("contactId") UUID contactId,
            @QueryParam("from") @DefaultValue("-30d") String from,
            @QueryParam("to") String to,
            @QueryParam("action") List<String> actions) {
        
        AuditQuery query = AuditQuery.builder()
            .entityType("Contact")
            .entityId(contactId.toString())
            .fromDate(parseTimeExpression(from))
            .toDate(to != null ? parseTimeExpression(to) : Instant.now())
            .actions(actions)
            .build();
        
        List<AuditEntry> entries = auditService.query(query);
        
        return Response.ok(AuditTrailResponse.builder()
            .contactId(contactId)
            .entries(entries)
            .total(entries.size())
            .build()).build();
    }
}
```

## 🎨 Frontend Implementation

### Audit Trail Viewer

```typescript
// components/audit/ContactAuditTrail.tsx
export const ContactAuditTrail: React.FC<AuditTrailProps> = ({
  contactId,
  embedded = false
}) => {
  const [filter, setFilter] = useState<AuditFilter>({
    from: '-7d',
    actions: []
  });
  
  const { data: auditTrail, loading } = useAuditTrail(contactId, filter);
  
  const getActionIcon = (action: AuditAction): ReactNode => {
    const icons: Record<AuditAction, ReactNode> = {
      CREATE: <AddCircleIcon color="success" />,
      UPDATE: <EditIcon color="info" />,
      DELETE: <DeleteIcon color="error" />,
      CONSENT_GRANTED: <CheckCircleIcon color="success" />,
      CONSENT_REVOKED: <CancelIcon color="warning" />,
      DATA_EXPORTED: <DownloadIcon color="primary" />
    };
    return icons[action] || <MoreHorizIcon />;
  };
  
  if (loading) return <CircularProgress />;
  
  return (
    <Box>
      {!embedded && (
        <Typography variant="h6" gutterBottom>
          Audit-Protokoll
        </Typography>
      )}
      
      {/* Filter */}
      <Box sx={{ mb: 2, display: 'flex', gap: 2 }}>
        <FormControl size="small" sx={{ minWidth: 120 }}>
          <InputLabel>Zeitraum</InputLabel>
          <Select
            value={filter.from}
            onChange={(e) => setFilter({...filter, from: e.target.value})}
          >
            <MenuItem value="-1d">Letzter Tag</MenuItem>
            <MenuItem value="-7d">Letzte Woche</MenuItem>
            <MenuItem value="-30d">Letzter Monat</MenuItem>
            <MenuItem value="-90d">Letzte 3 Monate</MenuItem>
          </Select>
        </FormControl>
      </Box>
      
      {/* Audit Entries */}
      <TableContainer component={Paper} variant="outlined">
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Zeitpunkt</TableCell>
              <TableCell>Aktion</TableCell>
              <TableCell>Benutzer</TableCell>
              <TableCell>Details</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {auditTrail?.entries.map((entry) => (
              <TableRow key={entry.eventId}>
                <TableCell>
                  {format(new Date(entry.timestamp), 'dd.MM.yyyy HH:mm:ss')}
                </TableCell>
                <TableCell>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    {getActionIcon(entry.action)}
                    <Typography variant="body2">
                      {entry.action}
                    </Typography>
                  </Box>
                </TableCell>
                <TableCell>{entry.userName}</TableCell>
                <TableCell>
                  <AuditDetailPopover details={entry.details} />
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  );
};
```

**Vollständiger Code:** [frontend/ContactAuditTrail.tsx](./code/frontend/audit/ContactAuditTrail.tsx)

### Audit Detail Popover

```typescript
// components/audit/AuditDetailPopover.tsx
export const AuditDetailPopover: React.FC<{details: any}> = ({ details }) => {
  const [anchorEl, setAnchorEl] = useState<HTMLElement | null>(null);
  
  const formatDetails = (details: any): string => {
    if (!details) return 'Keine Details';
    
    if (details.changedFields) {
      return `Geänderte Felder: ${details.changedFields.join(', ')}`;
    }
    if (details.consentType) {
      return `Einwilligung: ${details.consentType}`;
    }
    
    return JSON.stringify(details, null, 2);
  };
  
  return (
    <>
      <IconButton size="small" onClick={(e) => setAnchorEl(e.currentTarget)}>
        <InfoIcon />
      </IconButton>
      <Popover
        open={Boolean(anchorEl)}
        anchorEl={anchorEl}
        onClose={() => setAnchorEl(null)}
      >
        <Box sx={{ p: 2, maxWidth: 400 }}>
          <Typography variant="subtitle2" gutterBottom>
            Audit Details
          </Typography>
          <pre style={{ fontSize: '12px', overflow: 'auto' }}>
            {formatDetails(details)}
          </pre>
        </Box>
      </Popover>
    </>
  );
};
```

## 🧪 Tests

### Audit Integration Test

```java
@Test
void shouldCreateAuditEntryForContactCreation() {
    // Given
    ContactCreatedEvent event = createTestContactEvent();
    
    // When
    auditIntegrationService.processContactEvent(event).toCompletableFuture().join();
    
    // Then
    List<AuditEvent> auditEvents = auditRepository.findByEntityId(event.getAggregateId());
    assertThat(auditEvents).hasSize(1);
    assertThat(auditEvents.get(0).getAction()).isEqualTo(AuditAction.CREATE);
}

@Test
void shouldSanitizeSensitiveData() {
    // Given
    Map<String, Object> data = Map.of(
        "name", "Max Mustermann",
        "email", "max@example.com",
        "phone", "+49123456789"
    );
    
    // When
    Map<String, Object> sanitized = auditService.sanitizePersonalData(data);
    
    // Then
    assertThat(sanitized.get("name")).isEqualTo("Max Mustermann");
    assertThat(sanitized.get("email")).startsWith("[REDACTED-");
    assertThat(sanitized.get("phone")).startsWith("[REDACTED-");
}
```

## 📝 Checkliste

- [ ] Audit Integration Service implementiert
- [ ] Event Mapping komplett
- [ ] Data Sanitization funktioniert
- [ ] Audit Query API erstellt
- [ ] Frontend Viewer implementiert
- [ ] Detail Popover funktioniert
- [ ] Integration Tests grün

## 🔗 Weiterführende Links

- **Audit Standards:** [Audit Trail Best Practices](./guides/AUDIT_BEST_PRACTICES.md)
- **FC-012 Details:** [Audit Trail Module](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-012-audit-trail.md)
- **Nächster Schritt:** [→ Tag 2: DSGVO Integration](./DAY2_DSGVO.md)

---

**Status:** 📋 Bereit zur Implementierung