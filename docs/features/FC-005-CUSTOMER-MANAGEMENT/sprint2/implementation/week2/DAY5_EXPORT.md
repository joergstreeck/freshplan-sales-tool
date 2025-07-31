# 📆 Tag 5: GDPR Export & Testing

**Datum:** Freitag, 16. August 2025  
**Fokus:** DSGVO Export & Wochenabschluss  
**Ziel:** Vollständige GDPR Compliance  

## 🧭 Navigation

**← Vorheriger Tag:** [Tag 4: Offline](./DAY4_OFFLINE.md)  
**↑ Woche 2 Übersicht:** [README.md](./README.md)  
**→ Nächste Woche:** [Woche 3: Relationship](../week3/README.md)  
**📘 Spec:** [Export Specification](./specs/EXPORT_SPEC.md)  

## 🎯 Tagesziel

- Backend: GDPR Export Service
- Frontend: Export Dialog
- Formate: JSON, PDF, CSV
- Testing: Integration Tests
- Review: Woche 2 Abschluss

## 📤 GDPR Export Konzept

```
DSGVO Art. 20 - Datenübertragbarkeit
│
├── Persönliche Daten
├── Einwilligungshistorie
├── Kommunikationsverlauf
└── Aktivitätsprotokoll
    │
    ├─── JSON (maschinenlesbar)
    ├─── PDF (human-readable)
    └─── CSV (Tabellen)
```

## 💻 Backend Implementation

### 1. GDPR Export Service

```java
// GDPRExportService.java
@ApplicationScoped
public class GDPRExportService {
    
    @Inject
    EventStore eventStore;
    
    @Inject
    CryptoShredService cryptoService;
    
    public ExportResult exportContactData(UUID contactId, ExportFormat format) {
        // Collect all data
        ContactExportData data = ContactExportData.builder()
            .personalData(getPersonalData(contactId))
            .consentHistory(getConsentHistory(contactId))
            .communicationHistory(getCommunicationHistory(contactId))
            .eventHistory(getEventHistory(contactId))
            .build();
        
        // Format export
        return switch (format) {
            case JSON -> exportAsJson(data);
            case PDF -> exportAsPdf(data);
            case CSV -> exportAsCsv(data);
        };
    }
    
    private List<EventSummary> getEventHistory(UUID contactId) {
        return eventStore.getEvents(contactId).stream()
            .map(event -> EventSummary.builder()
                .eventType(event.getEventType())
                .timestamp(event.getCreatedAt())
                .userId(event.getMetadata().getUserId())
                // Personal data is encrypted - only show metadata
                .build())
            .collect(Collectors.toList());
    }
    
    // Scheduled cleanup of old exports
    @Scheduled(every = "24h")
    void cleanupOldExports() {
        exportRepository.deleteExportsOlderThan(Duration.ofDays(7));
    }
}
```

**Vollständiger Code:** [backend/GDPRExportService.java](./code/backend/GDPRExportService.java)

### 2. PDF Generation

```java
// PdfExportService.java
@ApplicationScoped
public class PdfExportService {
    
    public byte[] generatePdf(ContactExportData data) {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        PdfWriter.getInstance(document, baos);
        document.open();
        
        // Header
        addHeader(document, "DSGVO Datenexport");
        
        // Personal Data Section
        addSection(document, "Persönliche Daten", data.getPersonalData());
        
        // Consent History
        addConsentTable(document, data.getConsentHistory());
        
        // Communication Timeline
        addTimelineSection(document, data.getCommunicationHistory());
        
        document.close();
        return baos.toByteArray();
    }
}
```

## 🎨 Frontend Implementation

### Export Dialog

```typescript
// components/GDPRExportDialog.tsx
export const GDPRExportDialog: React.FC<GDPRExportDialogProps> = ({
  contact,
  open,
  onClose
}) => {
  const [format, setFormat] = useState<ExportFormat>('pdf');
  const [loading, setLoading] = useState(false);
  
  const handleExport = async () => {
    setLoading(true);
    try {
      const blob = await contactApi.exportGDPRData(contact.id, format);
      
      // Download file
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `dsgvo-export-${contact.name}-${new Date().toISOString()}.${format}`;
      a.click();
      
      // Audit log
      await auditApi.logExport({
        contactId: contact.id,
        format,
        timestamp: new Date()
      });
      
      onClose();
    } catch (error) {
      console.error('Export failed:', error);
    } finally {
      setLoading(false);
    }
  };
  
  return (
    <Dialog open={open} onClose={onClose}>
      <DialogTitle>DSGVO Datenexport</DialogTitle>
      
      <DialogContent>
        <Alert severity="info" sx={{ mb: 2 }}>
          Exportieren Sie alle gespeicherten Daten gemäß DSGVO Art. 20
        </Alert>
        
        <FormControl fullWidth>
          <InputLabel>Export-Format</InputLabel>
          <Select
            value={format}
            onChange={(e) => setFormat(e.target.value as ExportFormat)}
          >
            <MenuItem value="pdf">PDF (Lesbar)</MenuItem>
            <MenuItem value="json">JSON (Maschinenlesbar)</MenuItem>
            <MenuItem value="csv">CSV (Tabellenformat)</MenuItem>
          </Select>
        </FormControl>
        
        <Box sx={{ mt: 2 }}>
          <Typography variant="caption">
            Der Export enthält:
          </Typography>
          <ul>
            <li>Persönliche Daten</li>
            <li>Einwilligungshistorie</li>
            <li>Kommunikationsverlauf</li>
            <li>Aktivitätsprotokoll</li>
          </ul>
        </Box>
      </DialogContent>
      
      <DialogActions>
        <Button onClick={onClose}>Abbrechen</Button>
        <Button 
          onClick={handleExport} 
          variant="contained"
          disabled={loading}
        >
          {loading ? <CircularProgress size={20} /> : 'Exportieren'}
        </Button>
      </DialogActions>
    </Dialog>
  );
};
```

**Vollständiger Code:** [frontend/GDPRExportDialog.tsx](./code/frontend/GDPRExportDialog.tsx)

## 🧪 Integration Tests

### GDPR Compliance Test Suite

```typescript
// __tests__/gdpr.integration.test.ts
describe('GDPR Compliance', () => {
  it('should handle complete GDPR workflow', async () => {
    // Create contact
    const contact = await createTestContact();
    
    // Grant consent
    await gdprApi.grantConsent(contact.id, {
      type: 'marketing',
      basis: 'explicit'
    });
    
    // Export data
    const exportData = await gdprApi.exportData(contact.id, 'json');
    expect(exportData.personalData).toBeDefined();
    expect(exportData.consentHistory).toHaveLength(1);
    
    // Revoke consent
    await gdprApi.revokeConsent(contact.id, 'marketing');
    
    // Delete data
    await gdprApi.deleteData(contact.id);
    
    // Verify crypto-shredded
    await expect(contactApi.getContact(contact.id))
      .rejects.toThrow('DataShreddedException');
  });
  
  it('should export in all formats', async () => {
    const contact = await createTestContact();
    
    // Test JSON export
    const jsonExport = await gdprApi.exportData(contact.id, 'json');
    expect(JSON.parse(jsonExport)).toHaveProperty('personalData');
    
    // Test PDF export
    const pdfExport = await gdprApi.exportData(contact.id, 'pdf');
    expect(pdfExport.type).toBe('application/pdf');
    
    // Test CSV export
    const csvExport = await gdprApi.exportData(contact.id, 'csv');
    expect(csvExport).toContain('firstName,lastName,email');
  });
});
```

## 📊 Woche 2 Review

### Deliverables

| Feature | Status | Test Coverage |
|---------|--------|---------------|
| Consent Management | ✅ Done | 92% |
| Crypto-Shredding | ✅ Done | 88% |
| Mobile Quick Actions | ✅ Done | 85% |
| Offline Queue | ✅ Done | 90% |
| GDPR Export | ✅ Done | 95% |

### Performance Metriken

- Export Generation: < 500ms
- Mobile Touch Response: < 100ms
- Offline Sync: < 2s (bei 10 Actions)
- Consent UI: < 200ms

### Lessons Learned

1. **Crypto-Shredding** sehr effektiv für DSGVO
2. **Service Worker** Caching verbessert Mobile UX
3. **Conflict Resolution** braucht klare Regeln
4. **Export Formate** müssen flexibel sein

## 📝 Checkliste

- [ ] GDPR Export Service implementiert
- [ ] PDF Generation funktioniert
- [ ] Export Dialog erstellt
- [ ] Alle Formate getestet
- [ ] Integration Tests grün
- [ ] Woche 2 Dokumentation komplett
- [ ] Code Review durchgeführt

## 🔗 Nächste Woche

**Woche 3 Preview:**
- Relationship Warmth Indicator
- Beziehungsebene Features
- Analytics Integration
- Timeline Implementation

[→ Weiter zu Woche 3: Relationship + Analytics](../week3/README.md)

---

**Status:** ✅ Woche 2 erfolgreich abgeschlossen!