# 🚀 Bulk Operations - Massen-Bearbeitung ohne Chaos

**Phase:** 1 - Core Functionality  
**Priorität:** 🟡 WICHTIG - Zeitsparend für Power-User  
**Status:** 📋 GEPLANT  
**Letzte Aktualisierung:** 08.08.2025  
**Claude-Ready:** ✅ Vollständig navigierbar

## 🧭 NAVIGATION FÜR CLAUDE

**← Zurück:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/FEATURE_ADOPTION_TRACKING.md`  
**→ Nächster:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/IMPORT_EXPORT.md`  
**↑ Parent:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/README.md`  
**⚠️ Wichtig für:**
- Vertrieb (Zeit sparen)
- Daten-Migration
- Admin-Tasks

## ⚡ Quick Implementation Guide für Claude

```bash
# SOFORT STARTEN - Bulk Operations implementieren:
cd /Users/joergstreeck/freshplan-sales-tool

# 1. Backend Bulk Services
mkdir -p backend/src/main/java/de/freshplan/bulk
touch backend/src/main/java/de/freshplan/bulk/BulkOperationService.java
touch backend/src/main/java/de/freshplan/bulk/BulkValidationService.java
touch backend/src/main/java/de/freshplan/bulk/BulkOperationResult.java

# 2. Frontend Bulk Components
mkdir -p frontend/src/features/customers/components/bulk
touch frontend/src/features/customers/components/bulk/BulkActionBar.tsx
touch frontend/src/features/customers/components/bulk/BulkEditDialog.tsx
touch frontend/src/features/customers/components/bulk/BulkProgressMonitor.tsx
touch frontend/src/features/customers/components/bulk/BulkSelectionManager.tsx

# 3. Bulk Store
mkdir -p frontend/src/features/customers/stores
touch frontend/src/features/customers/stores/bulkOperationStore.ts

# 4. Tests
mkdir -p backend/src/test/java/de/freshplan/bulk
touch backend/src/test/java/de/freshplan/bulk/BulkOperationServiceTest.java
```

## 🎯 Das Problem: Einzelbearbeitung dauert ewig

**User-Frustration:**
- 📝 50 Kontakte einzeln taggen = 30 Minuten
- 🏢 Location für 100 Kontakte = 1 Stunde
- 📧 Email-Kampagne starten = Copy & Paste Hölle
- 🗑️ Alte Kontakte löschen = Klick-Marathon

## 💡 DIE LÖSUNG: Intelligente Bulk-Operations

### 1. Bulk Selection Manager

**Datei:** `frontend/src/features/customers/components/bulk/BulkSelectionManager.tsx`

```typescript
// CLAUDE: Intelligente Mehrfach-Auswahl mit Shortcuts
// Pfad: frontend/src/features/customers/components/bulk/BulkSelectionManager.tsx

import React, { useState, useEffect, useCallback } from 'react';
import {
  Box,
  Checkbox,
  Chip,
  Typography,
  Button,
  IconButton,
  Menu,
  MenuItem,
  Divider,
  Alert,
  Tooltip
} from '@mui/material';
import {
  SelectAll as SelectAllIcon,
  FilterList as FilterIcon,
  Clear as ClearIcon,
  Visibility as VisibilityIcon,
  VisibilityOff as VisibilityOffIcon
} from '@mui/icons-material';
import { useContactStore } from '../../stores/contactStore';
import { Contact } from '../../types/contact.types';

interface BulkSelectionManagerProps {
  contacts: Contact[];
  onSelectionChange: (selected: Contact[]) => void;
  maxSelection?: number;
}

export const BulkSelectionManager: React.FC<BulkSelectionManagerProps> = ({
  contacts,
  onSelectionChange,
  maxSelection = 500
}) => {
  const [selectedIds, setSelectedIds] = useState<Set<string>>(new Set());
  const [selectionMode, setSelectionMode] = useState<'manual' | 'smart'>('manual');
  const [filterAnchor, setFilterAnchor] = useState<null | HTMLElement>(null);
  
  // Keyboard shortcuts
  useEffect(() => {
    const handleKeyPress = (e: KeyboardEvent) => {
      // Ctrl/Cmd + A: Select all visible
      if ((e.ctrlKey || e.metaKey) && e.key === 'a') {
        e.preventDefault();
        selectAllVisible();
      }
      
      // Ctrl/Cmd + Shift + A: Deselect all
      if ((e.ctrlKey || e.metaKey) && e.shiftKey && e.key === 'a') {
        e.preventDefault();
        clearSelection();
      }
      
      // Ctrl/Cmd + I: Invert selection
      if ((e.ctrlKey || e.metaKey) && e.key === 'i') {
        e.preventDefault();
        invertSelection();
      }
    };
    
    window.addEventListener('keydown', handleKeyPress);
    return () => window.removeEventListener('keydown', handleKeyPress);
  }, [contacts]);
  
  const toggleSelection = (contactId: string) => {
    const newSelection = new Set(selectedIds);
    if (newSelection.has(contactId)) {
      newSelection.delete(contactId);
    } else {
      if (newSelection.size >= maxSelection) {
        // Show warning
        showMaxSelectionWarning();
        return;
      }
      newSelection.add(contactId);
    }
    updateSelection(newSelection);
  };
  
  const selectAllVisible = () => {
    const newSelection = new Set(selectedIds);
    contacts.forEach(contact => {
      if (newSelection.size < maxSelection) {
        newSelection.add(contact.id);
      }
    });
    updateSelection(newSelection);
  };
  
  const selectByFilter = (filter: string) => {
    const newSelection = new Set<string>();
    
    switch (filter) {
      case 'no-email':
        contacts.forEach(c => {
          if (!c.email && newSelection.size < maxSelection) {
            newSelection.add(c.id);
          }
        });
        break;
        
      case 'no-phone':
        contacts.forEach(c => {
          if (!c.phone && !c.mobile && newSelection.size < maxSelection) {
            newSelection.add(c.id);
          }
        });
        break;
        
      case 'decision-makers':
        contacts.forEach(c => {
          if (c.decisionLevel === 'EXECUTIVE' && newSelection.size < maxSelection) {
            newSelection.add(c.id);
          }
        });
        break;
        
      case 'primary':
        contacts.forEach(c => {
          if (c.isPrimary && newSelection.size < maxSelection) {
            newSelection.add(c.id);
          }
        });
        break;
        
      case 'inactive':
        const thirtyDaysAgo = new Date();
        thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30);
        
        contacts.forEach(c => {
          const lastContact = c.lastContactDate ? new Date(c.lastContactDate) : null;
          if ((!lastContact || lastContact < thirtyDaysAgo) && newSelection.size < maxSelection) {
            newSelection.add(c.id);
          }
        });
        break;
        
      case 'duplicates':
        // Find potential duplicates by name/email
        const seen = new Map<string, Contact[]>();
        contacts.forEach(c => {
          const key = `${c.firstName?.toLowerCase()}_${c.lastName?.toLowerCase()}`;
          if (!seen.has(key)) {
            seen.set(key, []);
          }
          seen.get(key)!.push(c);
        });
        
        seen.forEach(duplicates => {
          if (duplicates.length > 1) {
            // Select all but the first (keep oldest)
            duplicates.slice(1).forEach(c => {
              if (newSelection.size < maxSelection) {
                newSelection.add(c.id);
              }
            });
          }
        });
        break;
    }
    
    updateSelection(newSelection);
    setFilterAnchor(null);
  };
  
  const invertSelection = () => {
    const newSelection = new Set<string>();
    contacts.forEach(contact => {
      if (!selectedIds.has(contact.id) && newSelection.size < maxSelection) {
        newSelection.add(contact.id);
      }
    });
    updateSelection(newSelection);
  };
  
  const clearSelection = () => {
    updateSelection(new Set());
  };
  
  const updateSelection = (newSelection: Set<string>) => {
    setSelectedIds(newSelection);
    const selectedContacts = contacts.filter(c => newSelection.has(c.id));
    onSelectionChange(selectedContacts);
  };
  
  const showMaxSelectionWarning = () => {
    // Toast or inline alert
    console.warn(`Maximum selection of ${maxSelection} reached`);
  };
  
  return (
    <Box
      sx={{
        p: 2,
        bgcolor: 'background.paper',
        borderRadius: 1,
        boxShadow: 1,
        position: 'sticky',
        top: 0,
        zIndex: 100
      }}
    >
      <Box display="flex" alignItems="center" gap={2}>
        {/* Selection Info */}
        <Typography variant="subtitle1" sx={{ minWidth: 150 }}>
          {selectedIds.size === 0 ? (
            'Keine Auswahl'
          ) : (
            <Chip
              label={`${selectedIds.size} ausgewählt`}
              color="primary"
              onDelete={clearSelection}
            />
          )}
        </Typography>
        
        {/* Quick Actions */}
        <Box display="flex" gap={1}>
          <Tooltip title="Alle sichtbaren auswählen (Ctrl+A)">
            <Button
              size="small"
              startIcon={<SelectAllIcon />}
              onClick={selectAllVisible}
              disabled={selectedIds.size >= maxSelection}
            >
              Alle
            </Button>
          </Tooltip>
          
          <Tooltip title="Smart Filter Selection">
            <Button
              size="small"
              startIcon={<FilterIcon />}
              onClick={(e) => setFilterAnchor(e.currentTarget)}
            >
              Smart Select
            </Button>
          </Tooltip>
          
          <Tooltip title="Auswahl invertieren (Ctrl+I)">
            <IconButton size="small" onClick={invertSelection}>
              <VisibilityOffIcon />
            </IconButton>
          </Tooltip>
          
          <Tooltip title="Auswahl löschen (Ctrl+Shift+A)">
            <IconButton size="small" onClick={clearSelection}>
              <ClearIcon />
            </IconButton>
          </Tooltip>
        </Box>
        
        {/* Smart Filter Menu */}
        <Menu
          anchorEl={filterAnchor}
          open={Boolean(filterAnchor)}
          onClose={() => setFilterAnchor(null)}
        >
          <MenuItem onClick={() => selectByFilter('no-email')}>
            📧 Ohne Email-Adresse
          </MenuItem>
          <MenuItem onClick={() => selectByFilter('no-phone')}>
            📞 Ohne Telefonnummer
          </MenuItem>
          <Divider />
          <MenuItem onClick={() => selectByFilter('decision-makers')}>
            👔 Nur Entscheider
          </MenuItem>
          <MenuItem onClick={() => selectByFilter('primary')}>
            ⭐ Nur Hauptkontakte
          </MenuItem>
          <Divider />
          <MenuItem onClick={() => selectByFilter('inactive')}>
            💤 Inaktiv (>30 Tage)
          </MenuItem>
          <MenuItem onClick={() => selectByFilter('duplicates')}>
            👥 Mögliche Duplikate
          </MenuItem>
        </Menu>
        
        {/* Selection Limit Warning */}
        {selectedIds.size > maxSelection * 0.8 && (
          <Alert severity="warning" sx={{ py: 0 }}>
            Max. {maxSelection} Kontakte
          </Alert>
        )}
      </Box>
      
      {/* Keyboard Shortcuts Help */}
      {selectedIds.size === 0 && (
        <Typography variant="caption" color="text.secondary" sx={{ mt: 1, display: 'block' }}>
          Tipp: Nutze Ctrl+A (alle), Ctrl+I (invertieren), Ctrl+Shift+A (löschen)
        </Typography>
      )}
    </Box>
  );
};
```

### 2. Bulk Edit Dialog

**Datei:** `frontend/src/features/customers/components/bulk/BulkEditDialog.tsx`

```typescript
// CLAUDE: Multi-Edit Dialog mit Undo
// Pfad: frontend/src/features/customers/components/bulk/BulkEditDialog.tsx

import React, { useState, useMemo } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Box,
  Typography,
  TextField,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Chip,
  Alert,
  Tabs,
  Tab,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  Checkbox,
  Divider,
  LinearProgress
} from '@mui/material';
import {
  Edit as EditIcon,
  Add as AddIcon,
  Remove as RemoveIcon,
  Undo as UndoIcon,
  Warning as WarningIcon
} from '@mui/icons-material';
import { Contact } from '../../types/contact.types';
import { BulkOperation, BulkChange } from '../../types/bulk.types';

interface BulkEditDialogProps {
  open: boolean;
  contacts: Contact[];
  onClose: () => void;
  onApply: (operations: BulkOperation[]) => Promise<void>;
}

export const BulkEditDialog: React.FC<BulkEditDialogProps> = ({
  open,
  contacts,
  onClose,
  onApply
}) => {
  const [activeTab, setActiveTab] = useState(0);
  const [operations, setOperations] = useState<BulkOperation[]>([]);
  const [preview, setPreview] = useState<Map<string, BulkChange>>(new Map());
  const [applying, setApplying] = useState(false);
  
  // Common values analysis
  const commonValues = useMemo(() => {
    if (contacts.length === 0) return {};
    
    const analysis: any = {};
    
    // Find common tags
    const tagCounts = new Map<string, number>();
    contacts.forEach(c => {
      c.tags?.forEach(tag => {
        tagCounts.set(tag, (tagCounts.get(tag) || 0) + 1);
      });
    });
    
    analysis.commonTags = Array.from(tagCounts.entries())
      .filter(([_, count]) => count > contacts.length * 0.5)
      .map(([tag]) => tag);
    
    // Find common location
    const locationCounts = new Map<string, number>();
    contacts.forEach(c => {
      if (c.location) {
        locationCounts.set(c.location, (locationCounts.get(c.location) || 0) + 1);
      }
    });
    
    analysis.mostCommonLocation = Array.from(locationCounts.entries())
      .sort((a, b) => b[1] - a[1])[0]?.[0];
    
    return analysis;
  }, [contacts]);
  
  const addOperation = (op: BulkOperation) => {
    setOperations([...operations, op]);
    updatePreview([...operations, op]);
  };
  
  const removeOperation = (index: number) => {
    const newOps = operations.filter((_, i) => i !== index);
    setOperations(newOps);
    updatePreview(newOps);
  };
  
  const updatePreview = (ops: BulkOperation[]) => {
    const changes = new Map<string, BulkChange>();
    
    contacts.forEach(contact => {
      const change: BulkChange = {
        contactId: contact.id,
        original: { ...contact },
        modified: { ...contact },
        operations: []
      };
      
      // Apply each operation
      ops.forEach(op => {
        switch (op.type) {
          case 'SET_FIELD':
            change.modified[op.field] = op.value;
            change.operations.push(`Set ${op.field} to ${op.value}`);
            break;
            
          case 'ADD_TAG':
            change.modified.tags = [...(change.modified.tags || []), op.value];
            change.operations.push(`Add tag: ${op.value}`);
            break;
            
          case 'REMOVE_TAG':
            change.modified.tags = (change.modified.tags || []).filter(t => t !== op.value);
            change.operations.push(`Remove tag: ${op.value}`);
            break;
            
          case 'SET_LOCATION':
            change.modified.location = op.value;
            change.operations.push(`Set location: ${op.value}`);
            break;
            
          case 'SET_DECISION_LEVEL':
            change.modified.decisionLevel = op.value;
            change.operations.push(`Set decision level: ${op.value}`);
            break;
        }
      });
      
      changes.set(contact.id, change);
    });
    
    setPreview(changes);
  };
  
  const handleApply = async () => {
    setApplying(true);
    try {
      await onApply(operations);
      onClose();
    } catch (error) {
      console.error('Bulk operation failed:', error);
    } finally {
      setApplying(false);
    }
  };
  
  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>
        <Box display="flex" justifyContent="space-between" alignItems="center">
          <Typography variant="h6">
            {contacts.length} Kontakte bearbeiten
          </Typography>
          <Chip
            label={`${operations.length} Änderungen`}
            color={operations.length > 0 ? 'primary' : 'default'}
          />
        </Box>
      </DialogTitle>
      
      <DialogContent>
        <Tabs value={activeTab} onChange={(_, v) => setActiveTab(v)}>
          <Tab label="Basis-Felder" />
          <Tab label="Tags & Labels" />
          <Tab label="Erweitert" />
          <Tab label="Vorschau" />
        </Tabs>
        
        <Box sx={{ mt: 3, minHeight: 400 }}>
          {/* Basic Fields Tab */}
          {activeTab === 0 && (
            <Box>
              <Typography variant="subtitle2" gutterBottom>
                Felder für alle Kontakte setzen:
              </Typography>
              
              <List>
                <ListItem>
                  <FormControl fullWidth>
                    <InputLabel>Decision Level</InputLabel>
                    <Select
                      value=""
                      onChange={(e) => addOperation({
                        type: 'SET_DECISION_LEVEL',
                        field: 'decisionLevel',
                        value: e.target.value
                      })}
                    >
                      <MenuItem value="">-- Nicht ändern --</MenuItem>
                      <MenuItem value="EXECUTIVE">Executive</MenuItem>
                      <MenuItem value="MANAGER">Manager</MenuItem>
                      <MenuItem value="OPERATIONAL">Operational</MenuItem>
                      <MenuItem value="INFLUENCER">Influencer</MenuItem>
                    </Select>
                  </FormControl>
                </ListItem>
                
                <ListItem>
                  <FormControl fullWidth>
                    <InputLabel>Department</InputLabel>
                    <Select
                      value=""
                      onChange={(e) => addOperation({
                        type: 'SET_FIELD',
                        field: 'department',
                        value: e.target.value
                      })}
                    >
                      <MenuItem value="">-- Nicht ändern --</MenuItem>
                      <MenuItem value="IT">IT</MenuItem>
                      <MenuItem value="Sales">Vertrieb</MenuItem>
                      <MenuItem value="Marketing">Marketing</MenuItem>
                      <MenuItem value="Operations">Operations</MenuItem>
                      <MenuItem value="Finance">Finance</MenuItem>
                    </Select>
                  </FormControl>
                </ListItem>
                
                <ListItem>
                  <TextField
                    fullWidth
                    label="Company (für alle setzen)"
                    onBlur={(e) => {
                      if (e.target.value) {
                        addOperation({
                          type: 'SET_FIELD',
                          field: 'company',
                          value: e.target.value
                        });
                      }
                    }}
                  />
                </ListItem>
              </List>
            </Box>
          )}
          
          {/* Tags Tab */}
          {activeTab === 1 && (
            <Box>
              <Typography variant="subtitle2" gutterBottom>
                Tags verwalten:
              </Typography>
              
              <Box sx={{ mb: 3 }}>
                <Typography variant="body2" color="text.secondary" gutterBottom>
                  Gemeinsame Tags:
                </Typography>
                <Box display="flex" gap={1} flexWrap="wrap">
                  {commonValues.commonTags?.map((tag: string) => (
                    <Chip
                      key={tag}
                      label={tag}
                      onDelete={() => addOperation({
                        type: 'REMOVE_TAG',
                        field: 'tags',
                        value: tag
                      })}
                    />
                  ))}
                </Box>
              </Box>
              
              <TextField
                fullWidth
                label="Neue Tags hinzufügen (komma-getrennt)"
                onKeyPress={(e) => {
                  if (e.key === 'Enter') {
                    const input = e.target as HTMLInputElement;
                    const tags = input.value.split(',').map(t => t.trim()).filter(Boolean);
                    tags.forEach(tag => {
                      addOperation({
                        type: 'ADD_TAG',
                        field: 'tags',
                        value: tag
                      });
                    });
                    input.value = '';
                  }
                }}
              />
            </Box>
          )}
          
          {/* Advanced Tab */}
          {activeTab === 2 && (
            <Box>
              <Alert severity="warning" sx={{ mb: 2 }}>
                Erweiterte Operationen können nicht rückgängig gemacht werden!
              </Alert>
              
              <List>
                <ListItem>
                  <ListItemIcon>
                    <Checkbox
                      onChange={(e) => {
                        if (e.target.checked) {
                          addOperation({
                            type: 'SET_FIELD',
                            field: 'isPrimary',
                            value: false
                          });
                        }
                      }}
                    />
                  </ListItemIcon>
                  <ListItemText
                    primary="Primary-Status entfernen"
                    secondary="Alle ausgewählten als Nicht-Hauptkontakte markieren"
                  />
                </ListItem>
                
                <ListItem>
                  <ListItemIcon>
                    <Checkbox
                      onChange={(e) => {
                        if (e.target.checked) {
                          addOperation({
                            type: 'SET_FIELD',
                            field: 'consentGiven',
                            value: true
                          });
                        }
                      }}
                    />
                  </ListItemIcon>
                  <ListItemText
                    primary="Marketing-Einwilligung setzen"
                    secondary="DSGVO-Einwilligung für alle markieren"
                  />
                </ListItem>
              </List>
            </Box>
          )}
          
          {/* Preview Tab */}
          {activeTab === 3 && (
            <Box>
              <Typography variant="subtitle2" gutterBottom>
                Änderungen die angewendet werden:
              </Typography>
              
              {operations.length === 0 ? (
                <Alert severity="info">
                  Keine Änderungen definiert
                </Alert>
              ) : (
                <Box>
                  <List dense>
                    {operations.map((op, index) => (
                      <ListItem key={index}>
                        <ListItemIcon>
                          <EditIcon />
                        </ListItemIcon>
                        <ListItemText
                          primary={`${op.type}: ${op.field}`}
                          secondary={`Neuer Wert: ${op.value}`}
                        />
                        <IconButton onClick={() => removeOperation(index)}>
                          <RemoveIcon />
                        </IconButton>
                      </ListItem>
                    ))}
                  </List>
                  
                  <Divider sx={{ my: 2 }} />
                  
                  <Alert severity="info">
                    {preview.size} Kontakte werden geändert
                  </Alert>
                </Box>
              )}
            </Box>
          )}
        </Box>
        
        {applying && (
          <LinearProgress sx={{ mt: 2 }} />
        )}
      </DialogContent>
      
      <DialogActions>
        <Button onClick={onClose}>
          Abbrechen
        </Button>
        <Button
          startIcon={<UndoIcon />}
          onClick={() => {
            setOperations([]);
            updatePreview([]);
          }}
          disabled={operations.length === 0}
        >
          Zurücksetzen
        </Button>
        <Button
          variant="contained"
          onClick={handleApply}
          disabled={operations.length === 0 || applying}
        >
          {applying ? 'Wird angewendet...' : `${operations.length} Änderungen anwenden`}
        </Button>
      </DialogActions>
    </Dialog>
  );
};
```

### 3. Backend Bulk Service

**Datei:** `backend/src/main/java/de/freshplan/bulk/BulkOperationService.java`

```java
// CLAUDE: Performante Bulk-Operations mit Validation
// Pfad: backend/src/main/java/de/freshplan/bulk/BulkOperationService.java

package de.freshplan.bulk;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ApplicationScoped
public class BulkOperationService {
    
    @Inject
    ContactRepository contactRepository;
    
    @Inject
    BulkValidationService validationService;
    
    @Inject
    AuditService auditService;
    
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    
    /**
     * Execute bulk operations with validation and rollback support
     */
    @Transactional
    public BulkOperationResult executeBulkOperations(
        List<UUID> contactIds,
        List<BulkOperation> operations,
        UUID userId
    ) {
        BulkOperationResult result = new BulkOperationResult();
        result.setStartTime(LocalDateTime.now());
        result.setTotalContacts(contactIds.size());
        
        // Validate operations
        ValidationResult validation = validationService.validateOperations(operations);
        if (!validation.isValid()) {
            result.setStatus(OperationStatus.VALIDATION_FAILED);
            result.setErrors(validation.getErrors());
            return result;
        }
        
        // Load contacts in batches
        List<Contact> contacts = loadContactsInBatches(contactIds);
        
        // Create backup for rollback
        Map<UUID, Contact> backup = createBackup(contacts);
        
        try {
            // Apply operations
            int successCount = 0;
            List<String> errors = new ArrayList<>();
            
            for (Contact contact : contacts) {
                try {
                    applyOperationsToContact(contact, operations);
                    successCount++;
                } catch (Exception e) {
                    errors.add(String.format("Contact %s: %s", contact.getId(), e.getMessage()));
                    
                    // Rollback if too many errors
                    if (errors.size() > contactIds.size() * 0.1) { // >10% failure rate
                        rollback(backup);
                        result.setStatus(OperationStatus.ROLLED_BACK);
                        result.setErrors(errors);
                        return result;
                    }
                }
            }
            
            // Batch update
            contactRepository.batchUpdate(contacts);
            
            // Audit log
            auditService.logBulkOperation(userId, operations, contactIds);
            
            result.setStatus(OperationStatus.SUCCESS);
            result.setSuccessCount(successCount);
            result.setFailureCount(errors.size());
            result.setErrors(errors);
            
        } catch (Exception e) {
            rollback(backup);
            result.setStatus(OperationStatus.FAILED);
            result.setErrors(List.of(e.getMessage()));
        }
        
        result.setEndTime(LocalDateTime.now());
        result.setDurationMs(
            Duration.between(result.getStartTime(), result.getEndTime()).toMillis()
        );
        
        return result;
    }
    
    /**
     * Apply operations to a single contact
     */
    private void applyOperationsToContact(Contact contact, List<BulkOperation> operations) {
        for (BulkOperation op : operations) {
            switch (op.getType()) {
                case SET_FIELD:
                    applySetField(contact, op.getField(), op.getValue());
                    break;
                    
                case ADD_TAG:
                    contact.getTags().add(op.getValue().toString());
                    break;
                    
                case REMOVE_TAG:
                    contact.getTags().remove(op.getValue().toString());
                    break;
                    
                case SET_LOCATION:
                    contact.setLocation(op.getValue().toString());
                    break;
                    
                case SET_DECISION_LEVEL:
                    contact.setDecisionLevel(DecisionLevel.valueOf(op.getValue().toString()));
                    break;
                    
                case APPEND_NOTE:
                    String existingNotes = contact.getNotes() != null ? contact.getNotes() : "";
                    contact.setNotes(existingNotes + "\n" + op.getValue());
                    break;
            }
        }
        
        // Update metadata
        contact.setUpdatedAt(LocalDateTime.now());
        contact.setUpdatedBy(getCurrentUserId());
    }
    
    /**
     * Bulk delete with safety checks
     */
    @Transactional
    public BulkDeleteResult bulkDelete(List<UUID> contactIds, boolean softDelete, UUID userId) {
        BulkDeleteResult result = new BulkDeleteResult();
        
        // Safety check
        if (contactIds.size() > 100) {
            result.setRequiresConfirmation(true);
            result.setMessage("Deleting more than 100 contacts requires additional confirmation");
            return result;
        }
        
        // Check for primary contacts
        List<Contact> contacts = contactRepository.findByIds(contactIds);
        List<Contact> primaryContacts = contacts.stream()
            .filter(Contact::isPrimary)
            .collect(Collectors.toList());
        
        if (!primaryContacts.isEmpty()) {
            result.setWarnings(List.of(
                String.format("%d primary contacts will be deleted", primaryContacts.size())
            ));
        }
        
        // Perform deletion
        if (softDelete) {
            contacts.forEach(c -> {
                c.setDeletedAt(LocalDateTime.now());
                c.setDeletedBy(userId);
            });
            contactRepository.batchUpdate(contacts);
        } else {
            contactRepository.deleteByIds(contactIds);
        }
        
        // Audit
        auditService.logBulkDelete(userId, contactIds, softDelete);
        
        result.setDeletedCount(contacts.size());
        result.setStatus(OperationStatus.SUCCESS);
        
        return result;
    }
    
    /**
     * Bulk assign to locations
     */
    @Transactional
    public BulkOperationResult bulkAssignLocations(
        List<UUID> contactIds,
        List<String> locations,
        AssignmentStrategy strategy,
        UUID userId
    ) {
        List<Contact> contacts = contactRepository.findByIds(contactIds);
        
        switch (strategy) {
            case ROUND_ROBIN:
                // Distribute evenly across locations
                for (int i = 0; i < contacts.size(); i++) {
                    contacts.get(i).setLocation(locations.get(i % locations.size()));
                }
                break;
                
            case RANDOM:
                Random random = new Random();
                contacts.forEach(c -> {
                    c.setLocation(locations.get(random.nextInt(locations.size())));
                });
                break;
                
            case ALL_TO_FIRST:
                String location = locations.get(0);
                contacts.forEach(c -> c.setLocation(location));
                break;
        }
        
        contactRepository.batchUpdate(contacts);
        
        return BulkOperationResult.success(contacts.size());
    }
    
    /**
     * Load contacts in batches for memory efficiency
     */
    private List<Contact> loadContactsInBatches(List<UUID> contactIds) {
        List<Contact> allContacts = new ArrayList<>();
        int batchSize = 100;
        
        for (int i = 0; i < contactIds.size(); i += batchSize) {
            int end = Math.min(i + batchSize, contactIds.size());
            List<UUID> batch = contactIds.subList(i, end);
            allContacts.addAll(contactRepository.findByIds(batch));
        }
        
        return allContacts;
    }
    
    /**
     * Create backup for potential rollback
     */
    private Map<UUID, Contact> createBackup(List<Contact> contacts) {
        return contacts.stream()
            .collect(Collectors.toMap(
                Contact::getId,
                contact -> contact.clone() // Deep copy
            ));
    }
    
    /**
     * Rollback changes using backup
     */
    private void rollback(Map<UUID, Contact> backup) {
        List<Contact> restoredContacts = new ArrayList<>(backup.values());
        contactRepository.batchUpdate(restoredContacts);
    }
}
```

## 📋 IMPLEMENTIERUNGS-CHECKLISTE

### Phase 1: Selection (30 Min)
- [ ] BulkSelectionManager Component
- [ ] Keyboard Shortcuts
- [ ] Smart Filters
- [ ] Selection Store

### Phase 2: Operations (45 Min)
- [ ] BulkEditDialog
- [ ] Operation Types definieren
- [ ] Preview System
- [ ] Undo/Redo Support

### Phase 3: Backend (45 Min)
- [ ] BulkOperationService
- [ ] Batch Processing
- [ ] Validation Service
- [ ] Rollback Mechanism

### Phase 4: Special Operations (30 Min)
- [ ] Bulk Delete (soft/hard)
- [ ] Bulk Export
- [ ] Bulk Assignment
- [ ] Bulk Tagging

### Phase 5: Testing (30 Min)
- [ ] Unit Tests
- [ ] Performance Tests (500+ items)
- [ ] Rollback Tests
- [ ] UI Tests

## 🔗 INTEGRATION POINTS

1. **ContactList** - Selection UI integrieren
2. **ActionBar** - Bulk Actions anzeigen
3. **AuditLog** - Alle Bulk-Ops tracken
4. **Permissions** - Role-based limits

## ⚠️ HÄUFIGE FEHLER VERMEIDEN

1. **Performance bei vielen Items**
   → Lösung: Batch Processing, Virtual Scrolling

2. **Versehentliche Massen-Löschung**
   → Lösung: Confirmation Dialog, Soft Delete

3. **Timeout bei großen Operations**
   → Lösung: Background Jobs, Progress Updates

---

**Status:** BEREIT FÜR IMPLEMENTIERUNG  
**Geschätzte Zeit:** 180 Minuten  
**Nächstes Dokument:** [→ Import/Export](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/IMPORT_EXPORT.md)  
**Parent:** [↑ Critical Success Factors](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/CRITICAL_SUCCESS_FACTORS.md)

**Bulk = Zeitersparnis = Happy Users! 🚀✨**