# 📤 Audit Export Features

**Feature:** FC-005 Step3 - Contact Management UI  
**Status:** 📋 GEPLANT für PR 4  
**Priorität:** 🏅 Priorität 4  
**Geschätzter Aufwand:** 3-4 Stunden  

## 🧭 Navigation

**← Zurück:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/PERFORMANCE_OPTIMIZATIONS.md`  
**← Parent:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/README.md`  
**→ Nächstes:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/FAB_DRAWER_INTEGRATION.md`  
**← Smart Cards:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/SMART_CONTACT_CARD_AUDIT_INTEGRATION.md`  

## 🎯 Ziele

Ermögliche autorisierten Nutzern (Admin, Auditor) den Export von Audit-Daten in verschiedenen Formaten für Compliance, Reporting und Analyse.

## 📊 Export-Formate

### Unterstützte Formate
1. **CSV** - Für Excel/Spreadsheet-Analyse
2. **PDF** - Für offizielle Reports und Archivierung
3. **JSON** - Für technische Integration
4. **Excel** - Formatierte Berichte mit Styling

## 🏗️ Technische Implementierung

### 1. Export Button Component

**Datei:** `/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/audit/components/AuditExportButton.tsx`

```typescript
import { useState } from 'react';
import {
  Button,
  Menu,
  MenuItem,
  ListItemIcon,
  ListItemText,
  CircularProgress,
  Tooltip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  FormControl,
  FormLabel,
  RadioGroup,
  FormControlLabel,
  Radio,
  TextField,
  Box,
  Alert
} from '@mui/material';
import {
  Download as DownloadIcon,
  Description as PdfIcon,
  TableChart as CsvIcon,
  Code as JsonIcon,
  GridOn as ExcelIcon,
  DateRange as DateRangeIcon
} from '@mui/icons-material';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { useAuth } from '../../../contexts/AuthContext';
import { auditExportService } from '../services/auditExportService';
import { toast } from 'react-hot-toast';
import type { ExportOptions, ExportFormat } from '../types/export.types';

interface AuditExportButtonProps {
  entityType?: string;
  entityId?: string;
  variant?: 'text' | 'outlined' | 'contained';
  size?: 'small' | 'medium' | 'large';
  fullWidth?: boolean;
}

export function AuditExportButton({
  entityType,
  entityId,
  variant = 'outlined',
  size = 'medium',
  fullWidth = false
}: AuditExportButtonProps) {
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [exporting, setExporting] = useState(false);
  const [exportOptions, setExportOptions] = useState<ExportOptions>({
    format: 'csv',
    dateFrom: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000), // 30 Tage
    dateTo: new Date(),
    includeDetails: true,
    includeMetadata: false
  });
  
  const { user } = useAuth();
  
  // Prüfe Berechtigung
  const canExport = user?.roles?.some(role => 
    ['admin', 'auditor'].includes(role)
  );
  
  if (!canExport) {
    return null;
  }
  
  const handleMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };
  
  const handleMenuClose = () => {
    setAnchorEl(null);
  };
  
  const handleFormatSelect = (format: ExportFormat) => {
    setExportOptions(prev => ({ ...prev, format }));
    handleMenuClose();
    setDialogOpen(true);
  };
  
  const handleExport = async () => {
    setExporting(true);
    try {
      const blob = await auditExportService.exportAuditData({
        ...exportOptions,
        entityType,
        entityId
      });
      
      // Download Datei
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `audit_export_${entityType || 'all'}_${Date.now()}.${exportOptions.format}`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
      
      toast.success(`Audit-Daten erfolgreich als ${exportOptions.format.toUpperCase()} exportiert`);
      setDialogOpen(false);
      
      // Log Export für Audit Trail
      await auditExportService.logExportEvent({
        format: exportOptions.format,
        entityType,
        entityId,
        recordCount: 0 // Wird vom Service gefüllt
      });
      
    } catch (error) {
      console.error('Export failed:', error);
      toast.error('Export fehlgeschlagen. Bitte versuchen Sie es erneut.');
    } finally {
      setExporting(false);
    }
  };
  
  const getFormatIcon = (format: ExportFormat) => {
    switch(format) {
      case 'pdf': return <PdfIcon />;
      case 'csv': return <CsvIcon />;
      case 'json': return <JsonIcon />;
      case 'excel': return <ExcelIcon />;
      default: return <DownloadIcon />;
    }
  };
  
  return (
    <>
      <Tooltip title="Audit-Daten exportieren">
        <Button
          variant={variant}
          size={size}
          fullWidth={fullWidth}
          startIcon={<DownloadIcon />}
          onClick={handleMenuOpen}
          disabled={exporting}
        >
          {exporting ? <CircularProgress size={20} /> : 'Export'}
        </Button>
      </Tooltip>
      
      {/* Format-Auswahl Menu */}
      <Menu
        anchorEl={anchorEl}
        open={Boolean(anchorEl)}
        onClose={handleMenuClose}
      >
        <MenuItem onClick={() => handleFormatSelect('csv')}>
          <ListItemIcon><CsvIcon /></ListItemIcon>
          <ListItemText primary="CSV Export" secondary="Für Excel/Tabellen" />
        </MenuItem>
        <MenuItem onClick={() => handleFormatSelect('pdf')}>
          <ListItemIcon><PdfIcon /></ListItemIcon>
          <ListItemText primary="PDF Report" secondary="Für Dokumentation" />
        </MenuItem>
        <MenuItem onClick={() => handleFormatSelect('excel')}>
          <ListItemIcon><ExcelIcon /></ListItemIcon>
          <ListItemText primary="Excel Export" secondary="Formatierte Tabelle" />
        </MenuItem>
        <MenuItem onClick={() => handleFormatSelect('json')}>
          <ListItemIcon><JsonIcon /></ListItemIcon>
          <ListItemText primary="JSON Export" secondary="Für Integration" />
        </MenuItem>
      </Menu>
      
      {/* Export Options Dialog */}
      <Dialog 
        open={dialogOpen} 
        onClose={() => setDialogOpen(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>
          Export-Optionen für {exportOptions.format.toUpperCase()}
        </DialogTitle>
        
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3, mt: 2 }}>
            {/* Zeitraum */}
            <Box>
              <FormLabel component="legend">
                <DateRangeIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                Zeitraum
              </FormLabel>
              <Box sx={{ display: 'flex', gap: 2, mt: 1 }}>
                <DatePicker
                  label="Von"
                  value={exportOptions.dateFrom}
                  onChange={(date) => date && setExportOptions(prev => ({ 
                    ...prev, 
                    dateFrom: date 
                  }))}
                  slotProps={{ textField: { fullWidth: true } }}
                />
                <DatePicker
                  label="Bis"
                  value={exportOptions.dateTo}
                  onChange={(date) => date && setExportOptions(prev => ({ 
                    ...prev, 
                    dateTo: date 
                  }))}
                  slotProps={{ textField: { fullWidth: true } }}
                />
              </Box>
            </Box>
            
            {/* Format-spezifische Optionen */}
            {exportOptions.format === 'pdf' && (
              <FormControl>
                <FormLabel>PDF Optionen</FormLabel>
                <RadioGroup
                  value={exportOptions.pdfStyle || 'detailed'}
                  onChange={(e) => setExportOptions(prev => ({ 
                    ...prev, 
                    pdfStyle: e.target.value as 'summary' | 'detailed' 
                  }))}
                >
                  <FormControlLabel 
                    value="summary" 
                    control={<Radio />} 
                    label="Zusammenfassung" 
                  />
                  <FormControlLabel 
                    value="detailed" 
                    control={<Radio />} 
                    label="Detaillierter Report" 
                  />
                </RadioGroup>
              </FormControl>
            )}
            
            {exportOptions.format === 'excel' && (
              <FormControl>
                <FormLabel>Excel Optionen</FormLabel>
                <FormControlLabel
                  control={
                    <Checkbox
                      checked={exportOptions.includeCharts || false}
                      onChange={(e) => setExportOptions(prev => ({ 
                        ...prev, 
                        includeCharts: e.target.checked 
                      }))}
                    />
                  }
                  label="Mit Diagrammen"
                />
                <FormControlLabel
                  control={
                    <Checkbox
                      checked={exportOptions.includeSummary || false}
                      onChange={(e) => setExportOptions(prev => ({ 
                        ...prev, 
                        includeSummary: e.target.checked 
                      }))}
                    />
                  }
                  label="Mit Zusammenfassung"
                />
              </FormControl>
            )}
            
            {/* Allgemeine Optionen */}
            <FormControl>
              <FormLabel>Daten-Umfang</FormLabel>
              <FormControlLabel
                control={
                  <Checkbox
                    checked={exportOptions.includeDetails}
                    onChange={(e) => setExportOptions(prev => ({ 
                      ...prev, 
                      includeDetails: e.target.checked 
                    }))}
                  />
                }
                label="Detaillierte Änderungen einschließen"
              />
              <FormControlLabel
                control={
                  <Checkbox
                    checked={exportOptions.includeMetadata}
                    onChange={(e) => setExportOptions(prev => ({ 
                      ...prev, 
                      includeMetadata: e.target.checked 
                    }))}
                  />
                }
                label="Metadaten einschließen"
              />
            </FormControl>
            
            {/* Info Alert */}
            <Alert severity="info">
              Der Export wird alle Audit-Einträge 
              {entityType && ` für ${entityType}`}
              {entityId && ` (ID: ${entityId})`}
              {' '}im gewählten Zeitraum enthalten.
            </Alert>
          </Box>
        </DialogContent>
        
        <DialogActions>
          <Button onClick={() => setDialogOpen(false)}>
            Abbrechen
          </Button>
          <Button 
            onClick={handleExport} 
            variant="contained"
            disabled={exporting}
            startIcon={exporting ? <CircularProgress size={16} /> : getFormatIcon(exportOptions.format)}
          >
            {exporting ? 'Exportiere...' : 'Export starten'}
          </Button>
        </DialogActions>
      </Dialog>
    </>
  );
}
```

### 2. Export Service

**Datei:** `/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/audit/services/auditExportService.ts`

```typescript
import { format } from 'date-fns';
import { de } from 'date-fns/locale';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import * as XLSX from 'xlsx';
import { auditApi } from '../api/auditApi';
import type { AuditEntry } from '../types/audit.types';
import type { ExportOptions } from '../types/export.types';

class AuditExportService {
  /**
   * Haupt-Export-Funktion
   */
  async exportAuditData(options: ExportOptions): Promise<Blob> {
    // Lade Daten von API
    const data = await this.fetchAuditData(options);
    
    // Export basierend auf Format
    switch(options.format) {
      case 'csv':
        return this.exportToCsv(data, options);
      case 'pdf':
        return this.exportToPdf(data, options);
      case 'excel':
        return this.exportToExcel(data, options);
      case 'json':
        return this.exportToJson(data, options);
      default:
        throw new Error(`Unsupported format: ${options.format}`);
    }
  }
  
  /**
   * Daten von API laden
   */
  private async fetchAuditData(options: ExportOptions): Promise<AuditEntry[]> {
    const response = await auditApi.exportAudit({
      entityType: options.entityType,
      entityId: options.entityId,
      from: options.dateFrom.toISOString(),
      to: options.dateTo.toISOString(),
      format: 'json' // Immer JSON für Verarbeitung
    });
    
    return response.data;
  }
  
  /**
   * CSV Export
   */
  private exportToCsv(data: AuditEntry[], options: ExportOptions): Blob {
    const headers = [
      'Zeitstempel',
      'Aktion',
      'Entity-Typ',
      'Entity-ID',
      'Benutzer',
      'Rolle',
      'Änderungsgrund',
      'IP-Adresse',
      'Quelle'
    ];
    
    if (options.includeDetails) {
      headers.push('Alte Werte', 'Neue Werte');
    }
    
    if (options.includeMetadata) {
      headers.push('Session-ID', 'Request-ID', 'Hash');
    }
    
    // CSV Zeilen erstellen
    const rows = data.map(entry => {
      const row = [
        format(new Date(entry.timestamp), 'dd.MM.yyyy HH:mm:ss', { locale: de }),
        this.translateEventType(entry.eventType),
        entry.entityType,
        entry.entityId,
        entry.userName,
        entry.userRole,
        entry.changeReason || '',
        entry.ipAddress || '',
        entry.source || ''
      ];
      
      if (options.includeDetails) {
        row.push(
          JSON.stringify(entry.oldValue || {}),
          JSON.stringify(entry.newValue || {})
        );
      }
      
      if (options.includeMetadata) {
        row.push(
          entry.sessionId || '',
          entry.requestId || '',
          entry.hash || ''
        );
      }
      
      return row;
    });
    
    // CSV String erstellen
    const csvContent = [
      headers.join(','),
      ...rows.map(row => row.map(cell => `"${cell}"`).join(','))
    ].join('\n');
    
    return new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' });
  }
  
  /**
   * PDF Export
   */
  private exportToPdf(data: AuditEntry[], options: ExportOptions): Blob {
    const doc = new jsPDF();
    
    // Header
    doc.setFontSize(20);
    doc.text('Audit Trail Report', 14, 22);
    
    // Meta-Informationen
    doc.setFontSize(10);
    doc.text(`Erstellt am: ${format(new Date(), 'dd.MM.yyyy HH:mm', { locale: de })}`, 14, 32);
    doc.text(`Zeitraum: ${format(options.dateFrom, 'dd.MM.yyyy', { locale: de })} - ${format(options.dateTo, 'dd.MM.yyyy', { locale: de })}`, 14, 38);
    doc.text(`Anzahl Einträge: ${data.length}`, 14, 44);
    
    if (options.entityType) {
      doc.text(`Entity-Typ: ${options.entityType}`, 14, 50);
    }
    
    // Tabelle
    const tableData = data.map(entry => [
      format(new Date(entry.timestamp), 'dd.MM.yyyy HH:mm', { locale: de }),
      this.translateEventType(entry.eventType),
      entry.entityType,
      entry.userName,
      entry.changeReason || '-'
    ]);
    
    autoTable(doc, {
      head: [['Zeitstempel', 'Aktion', 'Entity', 'Benutzer', 'Grund']],
      body: tableData,
      startY: 60,
      styles: { fontSize: 8 },
      headStyles: { fillColor: [66, 139, 202] }
    });
    
    // Zusammenfassung (wenn gewünscht)
    if (options.pdfStyle === 'detailed') {
      const finalY = (doc as any).lastAutoTable.finalY || 60;
      
      doc.setFontSize(12);
      doc.text('Zusammenfassung', 14, finalY + 10);
      
      doc.setFontSize(10);
      const summary = this.generateSummary(data);
      let yPos = finalY + 20;
      
      Object.entries(summary).forEach(([key, value]) => {
        doc.text(`${key}: ${value}`, 14, yPos);
        yPos += 6;
      });
    }
    
    return doc.output('blob');
  }
  
  /**
   * Excel Export
   */
  private exportToExcel(data: AuditEntry[], options: ExportOptions): Blob {
    const workbook = XLSX.utils.book_new();
    
    // Hauptdaten Sheet
    const mainData = data.map(entry => ({
      'Zeitstempel': format(new Date(entry.timestamp), 'dd.MM.yyyy HH:mm:ss', { locale: de }),
      'Aktion': this.translateEventType(entry.eventType),
      'Entity-Typ': entry.entityType,
      'Entity-ID': entry.entityId,
      'Benutzer': entry.userName,
      'Rolle': entry.userRole,
      'Änderungsgrund': entry.changeReason || '',
      'IP-Adresse': entry.ipAddress || '',
      'Quelle': entry.source || ''
    }));
    
    const mainSheet = XLSX.utils.json_to_sheet(mainData);
    
    // Spaltenbreiten anpassen
    mainSheet['!cols'] = [
      { wch: 20 }, // Zeitstempel
      { wch: 15 }, // Aktion
      { wch: 15 }, // Entity-Typ
      { wch: 36 }, // Entity-ID (UUID)
      { wch: 20 }, // Benutzer
      { wch: 10 }, // Rolle
      { wch: 30 }, // Änderungsgrund
      { wch: 15 }, // IP
      { wch: 10 }  // Quelle
    ];
    
    XLSX.utils.book_append_sheet(workbook, mainSheet, 'Audit Trail');
    
    // Zusammenfassung Sheet (wenn gewünscht)
    if (options.includeSummary) {
      const summary = this.generateSummary(data);
      const summaryData = Object.entries(summary).map(([key, value]) => ({
        'Metrik': key,
        'Wert': value
      }));
      
      const summarySheet = XLSX.utils.json_to_sheet(summaryData);
      XLSX.utils.book_append_sheet(workbook, summarySheet, 'Zusammenfassung');
    }
    
    // Workbook zu Blob
    const wbout = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
    return new Blob([wbout], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
  }
  
  /**
   * JSON Export
   */
  private exportToJson(data: AuditEntry[], options: ExportOptions): Blob {
    const exportData = {
      metadata: {
        exportDate: new Date().toISOString(),
        dateFrom: options.dateFrom.toISOString(),
        dateTo: options.dateTo.toISOString(),
        entityType: options.entityType,
        entityId: options.entityId,
        recordCount: data.length
      },
      data: options.includeDetails ? data : data.map(({ oldValue, newValue, ...rest }) => rest)
    };
    
    return new Blob(
      [JSON.stringify(exportData, null, 2)], 
      { type: 'application/json' }
    );
  }
  
  /**
   * Event-Typ übersetzen
   */
  private translateEventType(eventType: string): string {
    const translations: Record<string, string> = {
      'CREATED': 'Erstellt',
      'UPDATED': 'Aktualisiert',
      'DELETED': 'Gelöscht',
      'VIEWED': 'Angesehen',
      'EXPORTED': 'Exportiert',
      'LOGIN': 'Anmeldung',
      'LOGOUT': 'Abmeldung',
      'PERMISSION_CHANGE': 'Berechtigung geändert',
      'SECURITY_EVENT': 'Sicherheitsereignis'
    };
    
    return translations[eventType] || eventType;
  }
  
  /**
   * Zusammenfassung generieren
   */
  private generateSummary(data: AuditEntry[]): Record<string, any> {
    const eventCounts = data.reduce((acc, entry) => {
      acc[entry.eventType] = (acc[entry.eventType] || 0) + 1;
      return acc;
    }, {} as Record<string, number>);
    
    const userCounts = data.reduce((acc, entry) => {
      acc[entry.userName] = (acc[entry.userName] || 0) + 1;
      return acc;
    }, {} as Record<string, number>);
    
    return {
      'Gesamt-Einträge': data.length,
      'Häufigste Aktion': Object.entries(eventCounts)
        .sort(([,a], [,b]) => b - a)[0]?.[0] || '-',
      'Aktivste Benutzer': Object.entries(userCounts)
        .sort(([,a], [,b]) => b - a)[0]?.[0] || '-',
      'Zeitraum': `${data.length > 0 ? format(new Date(data[0].timestamp), 'dd.MM.yyyy', { locale: de }) : '-'} - ${data.length > 0 ? format(new Date(data[data.length - 1].timestamp), 'dd.MM.yyyy', { locale: de }) : '-'}`
    };
  }
  
  /**
   * Export-Event loggen
   */
  async logExportEvent(details: {
    format: string;
    entityType?: string;
    entityId?: string;
    recordCount: number;
  }): Promise<void> {
    try {
      await auditApi.logEvent({
        eventType: 'DATA_EXPORT',
        entityType: details.entityType || 'audit_trail',
        entityId: details.entityId || 'all',
        details: {
          format: details.format,
          recordCount: details.recordCount
        }
      });
    } catch (error) {
      console.error('Failed to log export event:', error);
    }
  }
}

export const auditExportService = new AuditExportService();
```

## 🔒 Security & Compliance

### Berechtigungs-Checks
- Nur Admin und Auditor Rollen
- Server-seitige Validierung
- Export wird selbst im Audit Trail geloggt

### DSGVO-Konformität
- Personenbezogene Daten anonymisierbar
- Export-Log für Nachweispflicht
- Zeitliche Begrenzung der Exports

### Rate Limiting
```typescript
// Backend: Max 10 Exports pro Stunde
@RateLimited(max = 10, window = "1h")
public Response exportAuditTrail(...) { }
```

## 🧪 Test-Strategie

### Unit Tests
```typescript
describe('AuditExportService', () => {
  it('should export to CSV format', async () => {
    const blob = await service.exportToCsv(mockData, options);
    expect(blob.type).toBe('text/csv');
  });
  
  it('should include only authorized data', () => {
    // Test Filterung sensibler Daten
  });
});
```

### E2E Tests
```typescript
it('should download export file', async () => {
  // Klick Export Button
  // Wähle Format
  // Prüfe Download
});
```

## ✅ Definition of Done

- [ ] Export Button mit Format-Auswahl
- [ ] CSV Export implementiert
- [ ] PDF Export implementiert
- [ ] Excel Export implementiert
- [ ] JSON Export implementiert
- [ ] Options Dialog funktioniert
- [ ] Security Checks implementiert
- [ ] Export wird geloggt
- [ ] Tests geschrieben
- [ ] Dokumentation aktualisiert

---

**Status:** Bereit für Implementierung in PR 4  
**Abhängigkeiten:** Backend Export-Endpoints müssen verfügbar sein