# 🔄 Offline Conflict Resolution UX

**Erstellt:** 01.08.2025  
**Status:** 🆕 Konzept  
**Priorität:** HIGH - Kritisch für Datensicherheit bei Offline-Support  

## 🧭 Navigation

**← Zurück:** [Data Strategy Intelligence](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/DATA_STRATEGY_INTELLIGENCE.md)  
**→ Nächstes:** [Cost Management External Services](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/COST_MANAGEMENT_EXTERNAL_SERVICES.md)  
**↗ Verbunden:** [Offline Mobile Support](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/OFFLINE_MOBILE_SUPPORT.md)  

## 🎯 Problem: Datenverlust durch schlechte Konflikt-UI

**Die Herausforderung:**
- Technische Fehlermeldungen verwirren User
- "Ihre Version" vs "Server Version" ist unklar
- Falscher Klick = Datenverlust
- Angst vor Konflikten führt zu Offline-Vermeidung

## 💡 Lösung: Visueller Merge-Assistent

### 1. Conflict Detection & Notification

```typescript
// Sanfte Benachrichtigung statt Alarm
export const ConflictNotification: React.FC = () => {
  return (
    <Snackbar open={hasConflicts} anchorOrigin={{vertical: 'top', horizontal: 'center'}}>
      <Alert severity="info" icon={<MergeIcon />}>
        <AlertTitle>Änderungen synchronisieren</AlertTitle>
        Ein Kollege hat ebenfalls Änderungen vorgenommen. 
        <Button size="small" onClick={openMergeAssistant}>
          Zusammenführen
        </Button>
      </Alert>
    </Snackbar>
  );
};
```

### 2. Visueller Diff mit klaren Optionen

```typescript
export const MergeAssistant: React.FC<{conflict: ConflictData}> = ({conflict}) => {
  return (
    <Dialog maxWidth="lg" fullWidth>
      <DialogTitle>
        <Grid container alignItems="center" spacing={2}>
          <Grid item>
            <MergeTypeIcon />
          </Grid>
          <Grid item xs>
            <Typography variant="h6">
              Änderungen bei {conflict.contactName} zusammenführen
            </Typography>
            <Typography variant="caption" color="textSecondary">
              Ihre Änderungen: {formatTime(conflict.localTime)} | 
              {conflict.otherUser}: {formatTime(conflict.serverTime)}
            </Typography>
          </Grid>
        </Grid>
      </DialogTitle>
      
      <DialogContent>
        {/* Visuelle Diff-Ansicht */}
        <ConflictFieldList conflicts={conflict.fields} />
        
        {/* Smart Merge Suggestions */}
        <SmartMergeSuggestion conflict={conflict} />
      </DialogContent>
      
      <DialogActions>
        <Button onClick={handleKeepLocal} color="primary">
          Meine Version behalten
        </Button>
        <Button onClick={handleKeepServer}>
          {conflict.otherUser}'s Version nehmen
        </Button>
        <Button onClick={handleSmartMerge} variant="contained" color="primary">
          Intelligent zusammenführen ✨
        </Button>
      </DialogActions>
    </Dialog>
  );
};
```

### 3. Field-Level Conflict Resolution

```typescript
interface ConflictField {
  fieldName: string;
  displayName: string;
  localValue: any;
  serverValue: any;
  resolution: 'local' | 'server' | 'merged';
  mergedValue?: any;
}

export const ConflictFieldView: React.FC<{field: ConflictField}> = ({field}) => {
  return (
    <Card sx={{mb: 2}}>
      <CardContent>
        <Typography variant="subtitle2" gutterBottom>
          {field.displayName}
        </Typography>
        
        <Grid container spacing={2}>
          {/* Lokale Version */}
          <Grid item xs={5}>
            <Paper 
              elevation={field.resolution === 'local' ? 3 : 0}
              sx={{
                p: 2, 
                border: field.resolution === 'local' ? '2px solid primary.main' : '1px solid grey.300',
                cursor: 'pointer'
              }}
              onClick={() => selectResolution('local')}
            >
              <Typography variant="caption" color="textSecondary">
                Ihre Version
              </Typography>
              <Typography>{formatFieldValue(field.localValue)}</Typography>
            </Paper>
          </Grid>
          
          {/* Merge Indicator */}
          <Grid item xs={2} display="flex" alignItems="center" justifyContent="center">
            <CompareArrowsIcon color="action" />
          </Grid>
          
          {/* Server Version */}
          <Grid item xs={5}>
            <Paper 
              elevation={field.resolution === 'server' ? 3 : 0}
              sx={{
                p: 2,
                border: field.resolution === 'server' ? '2px solid secondary.main' : '1px solid grey.300',
                cursor: 'pointer'
              }}
              onClick={() => selectResolution('server')}
            >
              <Typography variant="caption" color="textSecondary">
                {conflict.otherUser}'s Version
              </Typography>
              <Typography>{formatFieldValue(field.serverValue)}</Typography>
            </Paper>
          </Grid>
        </Grid>
        
        {/* Smart Merge Option für bestimmte Felder */}
        {canSmartMerge(field) && (
          <Box mt={2}>
            <Alert severity="success" icon={<AutoFixHighIcon />}>
              <AlertTitle>Intelligenter Vorschlag</AlertTitle>
              {getSmartMergeDescription(field)}
              <Button size="small" onClick={() => applySmartMerge(field)}>
                Anwenden
              </Button>
            </Alert>
          </Box>
        )}
      </CardContent>
    </Card>
  );
};
```

### 4. Smart Merge Strategien

```typescript
// Intelligente Merge-Regeln je nach Feldtyp
const smartMergeStrategies = {
  // Listen zusammenführen (z.B. Hobbies)
  arrayFields: (local: string[], server: string[]) => {
    return [...new Set([...local, ...server])];
  },
  
  // Neueste Notiz behalten, alte anhängen
  notes: (local: string, server: string, localTime: Date, serverTime: Date) => {
    const newer = localTime > serverTime ? local : server;
    const older = localTime > serverTime ? server : local;
    return `${newer}\n\n--- Frühere Notiz ---\n${older}`;
  },
  
  // Höchste Warmth gewinnt (optimistisch)
  warmthScore: (local: number, server: number) => {
    return Math.max(local, server);
  },
  
  // Termine kombinieren
  appointments: (local: Date[], server: Date[]) => {
    return [...local, ...server].sort((a, b) => a.getTime() - b.getTime());
  }
};
```

### 5. Conflict Prevention

```typescript
// Echtzeit-Kollaborations-Hinweise
export const CollaborationIndicator: React.FC<{contactId: string}> = ({contactId}) => {
  const activeUsers = useActiveUsers(contactId);
  
  if (activeUsers.length === 0) return null;
  
  return (
    <Chip
      icon={<GroupIcon />}
      label={`${activeUsers.join(', ')} bearbeitet gerade`}
      color="warning"
      size="small"
      sx={{animation: 'pulse 2s infinite'}}
    />
  );
};

// Field-Level Locking
export const FieldLockIndicator: React.FC<{field: string, user: string}> = ({field, user}) => {
  return (
    <Tooltip title={`${user} bearbeitet dieses Feld gerade`}>
      <LockIcon fontSize="small" color="warning" />
    </Tooltip>
  );
};
```

## 📱 Mobile-optimierte Konfliktlösung

### Swipe-Gesten für schnelle Entscheidungen

```typescript
export const MobileConflictResolver: React.FC = () => {
  return (
    <SwipeableViews onChangeIndex={handleSwipe}>
      {conflicts.map(conflict => (
        <ConflictCard key={conflict.id}>
          <SwipeHint>
            ← Ihre Version | {conflict.otherUser}'s Version →
          </SwipeHint>
          
          <ConflictPreview conflict={conflict} />
          
          <SwipeActions>
            <IconButton color="primary">
              <CheckIcon /> Meine
            </IconButton>
            <IconButton>
              <MergeIcon /> Merge
            </IconButton>
            <IconButton color="secondary">
              <CheckIcon /> Andere
            </IconButton>
          </SwipeActions>
        </ConflictCard>
      ))}
    </SwipeableViews>
  );
};
```

## 🚀 Bulk-Konfliktlösung (NEU)

### Batch-Aktionen für viele Konflikte

```typescript
export const BulkConflictResolver: React.FC<{conflicts: Conflict[]}> = ({conflicts}) => {
  const [selectedConflicts, setSelectedConflicts] = useState<string[]>([]);
  const [bulkStrategy, setBulkStrategy] = useState<BulkStrategy | null>(null);
  
  // Zeige Bulk-Modus nur bei vielen Konflikten
  if (conflicts.length < 5) {
    return <StandardConflictResolver conflicts={conflicts} />;
  }
  
  return (
    <Box>
      {/* Bulk Action Bar */}
      <Paper sx={{p: 2, mb: 2, bgcolor: 'warning.light'}}>
        <Grid container alignItems="center" spacing={2}>
          <Grid item>
            <WarningIcon color="warning" />
          </Grid>
          <Grid item xs>
            <Typography variant="h6">
              {conflicts.length} Konflikte gefunden
            </Typography>
            <Typography variant="body2" color="textSecondary">
              Nach {getDaysOffline()} Tagen offline
            </Typography>
          </Grid>
          <Grid item>
            <Button 
              variant="outlined" 
              startIcon={<FlashOnIcon />}
              onClick={() => setShowBulkOptions(true)}
            >
              Schnell-Lösung
            </Button>
          </Grid>
        </Grid>
      </Paper>
      
      {/* Bulk Strategy Selection */}
      <Dialog open={showBulkOptions} maxWidth="md" fullWidth>
        <DialogTitle>
          Strategie für mehrere Konflikte wählen
        </DialogTitle>
        <DialogContent>
          <List>
            {/* Intelligente Vorschläge basierend auf Konflikt-Typen */}
            <ListItem button onClick={() => applyBulkStrategy('smart')}>
              <ListItemIcon>
                <AutoFixHighIcon color="primary" />
              </ListItemIcon>
              <ListItemText
                primary="Intelligente Lösung (Empfohlen)"
                secondary={
                  <Box>
                    <Typography variant="body2">
                      • Text-Felder: Ihre neueren Änderungen behalten<br/>
                      • Zahlen: Höhere Werte übernehmen<br/>
                      • Listen: Zusammenführen ohne Duplikate<br/>
                      • Termine: Alle behalten
                    </Typography>
                  </Box>
                }
              />
            </ListItem>
            
            <Divider />
            
            {/* Typ-spezifische Bulk-Aktionen */}
            <ListItem button onClick={() => applyBulkStrategy('mine-text')}>
              <ListItemIcon>
                <TextFieldsIcon />
              </ListItemIcon>
              <ListItemText
                primary="Meine Text-Änderungen behalten"
                secondary={`Betrifft ${countByType(conflicts, 'text')} Text-Konflikte`}
              />
            </ListItem>
            
            <ListItem button onClick={() => applyBulkStrategy('theirs-numbers')}>
              <ListItemIcon>
                <NumbersIcon />
              </ListItemIcon>
              <ListItemText
                primary="Server-Zahlen übernehmen"
                secondary={`Betrifft ${countByType(conflicts, 'number')} Zahlen-Konflikte`}
              />
            </ListItem>
            
            <ListItem button onClick={() => applyBulkStrategy('merge-lists')}>
              <ListItemIcon>
                <ListIcon />
              </ListItemIcon>
              <ListItemText
                primary="Listen zusammenführen"
                secondary={`Betrifft ${countByType(conflicts, 'array')} Listen-Konflikte`}
              />
            </ListItem>
            
            <Divider />
            
            {/* Globale Aktionen */}
            <ListItem button onClick={() => applyBulkStrategy('all-mine')}>
              <ListItemIcon>
                <PersonIcon color="primary" />
              </ListItemIcon>
              <ListItemText
                primary="Alle meine Änderungen behalten"
                secondary="Vorsicht: Überschreibt alle Server-Änderungen"
              />
            </ListItem>
            
            <ListItem button onClick={() => applyBulkStrategy('all-theirs')}>
              <ListItemIcon>
                <CloudIcon color="secondary" />
              </ListItemIcon>
              <ListItemText
                primary="Alle Server-Änderungen übernehmen"
                secondary="Vorsicht: Verwirft alle Ihre Änderungen"
              />
            </ListItem>
          </List>
          
          {/* Preview der Auswirkungen */}
          {bulkStrategy && (
            <Alert severity="info" sx={{mt: 2}}>
              <AlertTitle>Vorschau</AlertTitle>
              Diese Aktion würde {getAffectedCount(bulkStrategy)} Konflikte lösen.
              {getRemainingCount(bulkStrategy) > 0 && (
                <Typography variant="body2" sx={{mt: 1}}>
                  {getRemainingCount(bulkStrategy)} Konflikte müssen noch manuell gelöst werden.
                </Typography>
              )}
            </Alert>
          )}
        </DialogContent>
        
        <DialogActions>
          <Button onClick={() => setShowBulkOptions(false)}>
            Abbrechen
          </Button>
          <Button 
            variant="contained" 
            onClick={executeBulkStrategy}
            disabled={!bulkStrategy}
          >
            Strategie anwenden
          </Button>
        </DialogActions>
      </Dialog>
      
      {/* Konflikt-Liste mit Checkboxes */}
      <List>
        {conflicts.map((conflict, index) => (
          <ListItem key={conflict.id}>
            <ListItemIcon>
              <Checkbox
                checked={selectedConflicts.includes(conflict.id)}
                onChange={(e) => handleSelectConflict(conflict.id, e.target.checked)}
              />
            </ListItemIcon>
            <ListItemText
              primary={`${index + 1}. ${conflict.fieldName}`}
              secondary={
                <ConflictSummary 
                  local={conflict.localValue}
                  server={conflict.serverValue}
                  resolution={conflict.resolution}
                />
              }
            />
            <ListItemSecondaryAction>
              <IconButton onClick={() => openDetailedView(conflict)}>
                <EditIcon />
              </IconButton>
            </ListItemSecondaryAction>
          </ListItem>
        ))}
      </List>
      
      {/* Bulk Action Footer */}
      {selectedConflicts.length > 0 && (
        <Paper 
          sx={{
            position: 'sticky',
            bottom: 0,
            p: 2,
            borderTop: 1,
            borderColor: 'divider'
          }}
        >
          <Grid container spacing={2} alignItems="center">
            <Grid item xs>
              <Typography>
                {selectedConflicts.length} von {conflicts.length} ausgewählt
              </Typography>
            </Grid>
            <Grid item>
              <Button
                variant="outlined"
                onClick={() => applyToSelected('mine')}
              >
                Meine für Auswahl
              </Button>
            </Grid>
            <Grid item>
              <Button
                variant="outlined"
                onClick={() => applyToSelected('theirs')}
              >
                Server für Auswahl
              </Button>
            </Grid>
          </Grid>
        </Paper>
      )}
    </Box>
  );
};

// Bulk Strategy Engine
class BulkConflictEngine {
  applyStrategy(
    conflicts: Conflict[], 
    strategy: BulkStrategy
  ): ConflictResolution[] {
    switch(strategy) {
      case 'smart':
        return this.applySmartStrategy(conflicts);
      case 'mine-text':
        return this.applyTypeStrategy(conflicts, 'text', 'local');
      case 'theirs-numbers':
        return this.applyTypeStrategy(conflicts, 'number', 'server');
      case 'merge-lists':
        return this.applyMergeStrategy(conflicts, 'array');
      default:
        return this.applyGlobalStrategy(conflicts, strategy);
    }
  }
  
  private applySmartStrategy(conflicts: Conflict[]): ConflictResolution[] {
    return conflicts.map(conflict => {
      // Intelligente Regeln pro Feld-Typ
      if (conflict.fieldType === 'text') {
        // Neuere Version gewinnt
        return conflict.localTimestamp > conflict.serverTimestamp 
          ? {id: conflict.id, resolution: 'local'}
          : {id: conflict.id, resolution: 'server'};
      }
      
      if (conflict.fieldType === 'number') {
        // Höherer Wert gewinnt (z.B. bei Umsatz)
        return conflict.localValue > conflict.serverValue
          ? {id: conflict.id, resolution: 'local'}
          : {id: conflict.id, resolution: 'server'};
      }
      
      if (conflict.fieldType === 'array') {
        // Arrays zusammenführen
        return {
          id: conflict.id, 
          resolution: 'merged',
          mergedValue: [...new Set([...conflict.localValue, ...conflict.serverValue])]
        };
      }
      
      // Default: Manuell
      return {id: conflict.id, resolution: 'manual'};
    });
  }
}
```

## 🎓 User Education

### Interaktives Tutorial

```typescript
export const ConflictResolutionTutorial: React.FC = () => {
  const steps = [
    {
      target: '.merge-assistant',
      content: 'Hier sehen Sie beide Versionen nebeneinander',
      placement: 'top'
    },
    {
      target: '.smart-merge-button',
      content: 'Unser System schlägt die beste Kombination vor',
      placement: 'bottom'
    },
    {
      target: '.conflict-preview',
      content: 'Farbige Markierungen zeigen Unterschiede',
      placement: 'right'
    }
  ];
  
  return <Joyride steps={steps} run={showTutorial} />;
};
```

## 🔒 Sicherheits-Features

### Automatische Backups vor Merge

```java
@Service
public class ConflictBackupService {
    
    @Transactional
    public ConflictBackup createBackup(UUID contactId, ConflictData conflict) {
        ConflictBackup backup = ConflictBackup.builder()
            .contactId(contactId)
            .localVersion(conflict.getLocalData())
            .serverVersion(conflict.getServerData())
            .timestamp(LocalDateTime.now())
            .userId(getCurrentUserId())
            .build();
            
        backupRepository.save(backup);
        
        // Auto-cleanup nach 30 Tagen
        scheduleCleanup(backup.getId(), 30, TimeUnit.DAYS);
        
        return backup;
    }
    
    public void restoreFromBackup(UUID backupId) {
        // One-Click Restore bei Merge-Fehlern
    }
}
```

### Merge-History

```typescript
interface MergeHistoryEntry {
  timestamp: Date;
  user: string;
  conflictType: 'field' | 'record' | 'delete';
  resolution: 'local' | 'server' | 'merged' | 'manual';
  affectedFields: string[];
  backupId: string;
}

// UI Component
export const MergeHistory: React.FC<{contactId: string}> = ({contactId}) => {
  const history = useMergeHistory(contactId);
  
  return (
    <Timeline>
      {history.map(entry => (
        <TimelineItem key={entry.backupId}>
          <TimelineContent>
            <Typography variant="caption">
              {entry.user} hat {entry.conflictType} Konflikt gelöst
            </Typography>
            <Button size="small" onClick={() => showDetails(entry)}>
              Details
            </Button>
            <Button size="small" onClick={() => restore(entry.backupId)}>
              Rückgängig
            </Button>
          </TimelineContent>
        </TimelineItem>
      ))}
    </Timeline>
  );
};
```

## 📊 Metriken

### Conflict Resolution Analytics

```typescript
interface ConflictMetrics {
  totalConflicts: number;
  resolutionMethods: {
    keepLocal: number;
    keepServer: number;
    smartMerge: number;
    manual: number;
  };
  averageResolutionTime: number; // Sekunden
  dataLossIncidents: number;
  userSatisfaction: number; // 1-5 Sterne
}
```

## 🚀 Implementierungs-Checkliste

- [ ] Konflikt-Detection Engine
- [ ] Visual Diff Component
- [ ] Smart Merge Algorithmen
- [ ] Mobile Swipe UI
- [ ] Backup Service
- [ ] Tutorial Content
- [ ] Analytics Integration

## 🔗 Verwandte Dokumente

- [Offline Mobile Support](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/OFFLINE_MOBILE_SUPPORT.md) - Basis für Offline-Konflikte
- [Mobile Contact Actions](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/MOBILE_CONTACT_ACTIONS.md) - Mobile UI Patterns
- [Data Strategy Intelligence](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/DATA_STRATEGY_INTELLIGENCE.md) - Datenqualität

---

**Nächster Schritt:** [→ Cost Management für External Services](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/COST_MANAGEMENT_EXTERNAL_SERVICES.md)