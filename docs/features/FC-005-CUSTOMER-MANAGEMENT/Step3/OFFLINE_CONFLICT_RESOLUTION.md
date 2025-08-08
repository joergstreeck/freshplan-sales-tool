# 🔄 Offline Conflict Resolution - Intelligente Konfliktlösung

**Phase:** 0 - Critical Foundation  
**Priorität:** 🔴 KRITISCH - Datenverlust verhindern  
**Status:** 📋 GEPLANT  
**Letzte Aktualisierung:** 08.08.2025  
**Claude-Ready:** ✅ Vollständig navigierbar

## 🧭 NAVIGATION FÜR CLAUDE

**← Zurück:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/DATA_STRATEGY_INTELLIGENCE.md`  
**→ Nächster:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/COST_MANAGEMENT_EXTERNAL_SERVICES.md`  
**↑ Parent:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/README.md`  
**⚠️ Blockiert:**
- `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/MOBILE_CONTACT_ACTIONS.md`

## ⚡ Quick Implementation Guide für Claude

```bash
# SOFORT STARTEN - Conflict Resolution implementieren:
cd /Users/joergstreeck/freshplan-sales-tool

# 1. Conflict Types & Resolution Strategy
mkdir -p frontend/src/features/customers/services/sync
touch frontend/src/features/customers/services/sync/ConflictResolutionService.ts
touch frontend/src/features/customers/services/sync/MergeStrategies.ts
touch frontend/src/features/customers/types/conflict.types.ts

# 2. UI Components für Konflikt-Dialog
mkdir -p frontend/src/features/customers/components/sync
touch frontend/src/features/customers/components/sync/ConflictDialog.tsx
touch frontend/src/features/customers/components/sync/ConflictPreview.tsx
touch frontend/src/features/customers/components/sync/BulkConflictResolver.tsx

# 3. Backend Conflict Detection
mkdir -p backend/src/main/java/de/freshplan/sync
touch backend/src/main/java/de/freshplan/sync/ConflictDetectionService.java
touch backend/src/main/java/de/freshplan/sync/MergeStrategy.java

# 4. Tests
mkdir -p frontend/src/features/customers/services/sync/__tests__
touch frontend/src/features/customers/services/sync/__tests__/ConflictResolution.test.ts
```

## 🎯 Das Problem: Offline-Änderungen kollidieren

**Typische Szenarien:**
- 📱 Außendienst arbeitet offline im Zug
- 👥 Mehrere User bearbeiten gleichen Kontakt
- 🔄 Sync nach längerer Offline-Zeit
- 💥 Technische Fehlermeldungen frustrieren

## 💡 DIE LÖSUNG: User-freundliche Konfliktlösung

### 1. Conflict Types Definition

**Datei:** `frontend/src/features/customers/types/conflict.types.ts`

```typescript
// CLAUDE: Types für Konflikt-Management
// Pfad: frontend/src/features/customers/types/conflict.types.ts

export enum ConflictType {
  FIELD_UPDATE = 'field_update',      // Gleiches Feld geändert
  DELETION = 'deletion',              // Einer hat gelöscht
  RELATIONSHIP = 'relationship',      // Primary-Status konflikt
  HIERARCHY = 'hierarchy',            // Parent/Child konflikt
  BUSINESS_LOGIC = 'business_logic'   // Geschäftslogik verletzt
}

export enum MergeStrategy {
  USE_MINE = 'use_mine',              // Lokale Version behalten
  USE_THEIRS = 'use_theirs',          // Server-Version nehmen
  MERGE_BOTH = 'merge_both',          // Beide zusammenführen
  USE_LATEST = 'use_latest',          // Neueste nach Timestamp
  USE_HIGHER = 'use_higher',          // Höherer Wert (optimistisch)
  MANUAL = 'manual'                   // User entscheidet
}

export interface ConflictField {
  fieldName: string;
  displayName: string;
  localValue: any;
  serverValue: any;
  mergedValue?: any;
  strategy: MergeStrategy;
  importance: 'critical' | 'high' | 'medium' | 'low';
}

export interface ContactConflict {
  id: string;
  contactId: string;
  contactName: string;
  type: ConflictType;
  fields: ConflictField[];
  localVersion: {
    data: any;
    timestamp: Date;
    user: string;
  };
  serverVersion: {
    data: any;
    timestamp: Date;
    user: string;
  };
  suggestedResolution: MergeStrategy;
  autoResolvable: boolean;
}

export interface BulkConflictResolution {
  totalConflicts: number;
  autoResolved: number;
  needsManualReview: number;
  strategy: 'all_mine' | 'all_theirs' | 'smart' | 'manual';
}
```

### 2. Intelligente Merge-Strategien

**Datei:** `frontend/src/features/customers/services/sync/MergeStrategies.ts`

```typescript
// CLAUDE: Intelligente Field-basierte Merge-Strategien
// Pfad: frontend/src/features/customers/services/sync/MergeStrategies.ts

import { MergeStrategy, ConflictField } from '../../types/conflict.types';

export class MergeStrategies {
  
  /**
   * Field-Type basierte Default-Strategien
   */
  private static fieldStrategies: Record<string, MergeStrategy> = {
    // Timestamps - immer neuestes
    'lastContactDate': MergeStrategy.USE_LATEST,
    'updatedAt': MergeStrategy.USE_LATEST,
    
    // Notizen - beide behalten
    'notes': MergeStrategy.MERGE_BOTH,
    'personalNotes': MergeStrategy.MERGE_BOTH,
    'internalComments': MergeStrategy.MERGE_BOTH,
    
    // Kontaktdaten - manuell
    'email': MergeStrategy.MANUAL,
    'phone': MergeStrategy.MANUAL,
    'mobile': MergeStrategy.MANUAL,
    
    // Tags/Labels - vereinen
    'tags': MergeStrategy.MERGE_BOTH,
    'categories': MergeStrategy.MERGE_BOTH,
    
    // Numerische Werte - optimistisch
    'revenue': MergeStrategy.USE_HIGHER,
    'dealSize': MergeStrategy.USE_HIGHER,
    'employeeCount': MergeStrategy.USE_HIGHER,
    
    // Status - neuestes
    'status': MergeStrategy.USE_LATEST,
    'stage': MergeStrategy.USE_LATEST,
    
    // Boolean - OR-Verknüpfung
    'isActive': MergeStrategy.USE_HIGHER,
    'isPrimary': MergeStrategy.MANUAL,
    
    // Default
    '*': MergeStrategy.MANUAL
  };
  
  /**
   * Ermittelt optimale Merge-Strategie für ein Feld
   */
  static getStrategy(fieldName: string, context?: any): MergeStrategy {
    // Spezifische Strategie
    if (this.fieldStrategies[fieldName]) {
      return this.fieldStrategies[fieldName];
    }
    
    // Pattern-basierte Strategien
    if (fieldName.endsWith('Date') || fieldName.endsWith('At')) {
      return MergeStrategy.USE_LATEST;
    }
    
    if (fieldName.startsWith('is') || fieldName.startsWith('has')) {
      return MergeStrategy.USE_HIGHER;
    }
    
    if (fieldName.includes('notes') || fieldName.includes('comment')) {
      return MergeStrategy.MERGE_BOTH;
    }
    
    // Default
    return MergeStrategy.MANUAL;
  }
  
  /**
   * Führt zwei Werte basierend auf Strategie zusammen
   */
  static merge(
    localValue: any,
    serverValue: any,
    strategy: MergeStrategy,
    fieldType?: string
  ): any {
    switch (strategy) {
      case MergeStrategy.USE_MINE:
        return localValue;
        
      case MergeStrategy.USE_THEIRS:
        return serverValue;
        
      case MergeStrategy.USE_LATEST:
        // Benötigt Timestamps im Context
        return localValue; // Fallback
        
      case MergeStrategy.USE_HIGHER:
        if (typeof localValue === 'number' && typeof serverValue === 'number') {
          return Math.max(localValue, serverValue);
        }
        if (typeof localValue === 'boolean' || typeof serverValue === 'boolean') {
          return localValue || serverValue;
        }
        return localValue;
        
      case MergeStrategy.MERGE_BOTH:
        // Arrays vereinen
        if (Array.isArray(localValue) && Array.isArray(serverValue)) {
          return [...new Set([...localValue, ...serverValue])];
        }
        
        // Strings konkatenieren
        if (typeof localValue === 'string' && typeof serverValue === 'string') {
          if (localValue === serverValue) return localValue;
          return `${localValue}\n---\n${serverValue}`;
        }
        
        // Objects mergen
        if (typeof localValue === 'object' && typeof serverValue === 'object') {
          return { ...serverValue, ...localValue };
        }
        
        return localValue;
        
      case MergeStrategy.MANUAL:
      default:
        return undefined; // User muss entscheiden
    }
  }
  
  /**
   * Intelligente Auto-Resolution für Bulk-Konflikte
   */
  static canAutoResolve(field: ConflictField): boolean {
    // Kritische Felder nie auto-resolven
    if (field.importance === 'critical') {
      return false;
    }
    
    // Identische Werte
    if (JSON.stringify(field.localValue) === JSON.stringify(field.serverValue)) {
      return true;
    }
    
    // Nur Whitespace-Unterschiede
    if (typeof field.localValue === 'string' && typeof field.serverValue === 'string') {
      if (field.localValue.trim() === field.serverValue.trim()) {
        return true;
      }
    }
    
    // Einer ist null/undefined
    if (!field.localValue || !field.serverValue) {
      return true;
    }
    
    // Timestamp-Felder können auto-resolved werden
    if (field.fieldName.endsWith('Date') || field.fieldName.endsWith('At')) {
      return true;
    }
    
    // Low-importance Felder
    if (field.importance === 'low') {
      return true;
    }
    
    return false;
  }
}
```

### 3. Conflict Resolution UI

**Datei:** `frontend/src/features/customers/components/sync/ConflictDialog.tsx`

```typescript
// CLAUDE: User-freundlicher Konflikt-Dialog
// Pfad: frontend/src/features/customers/components/sync/ConflictDialog.tsx

import React, { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Box,
  Typography,
  Chip,
  RadioGroup,
  FormControlLabel,
  Radio,
  Divider,
  Alert,
  IconButton,
  Tooltip,
  Grid,
  Card,
  CardContent,
  Stepper,
  Step,
  StepLabel
} from '@mui/material';
import {
  Phone as PhoneIcon,
  Email as EmailIcon,
  Person as PersonIcon,
  Business as BusinessIcon,
  Info as InfoIcon,
  CheckCircle as CheckIcon,
  Warning as WarningIcon,
  Merge as MergeIcon,
  CompareArrows as CompareIcon
} from '@mui/icons-material';
import { ContactConflict, MergeStrategy, ConflictField } from '../../types/conflict.types';

interface ConflictDialogProps {
  open: boolean;
  conflicts: ContactConflict[];
  onResolve: (resolutions: Map<string, MergeStrategy>) => void;
  onCancel: () => void;
}

export const ConflictDialog: React.FC<ConflictDialogProps> = ({
  open,
  conflicts,
  onResolve,
  onCancel
}) => {
  const [currentIndex, setCurrentIndex] = useState(0);
  const [resolutions, setResolutions] = useState<Map<string, MergeStrategy>>(new Map());
  const [bulkStrategy, setBulkStrategy] = useState<'smart' | 'mine' | 'theirs' | 'manual'>('smart');
  
  const currentConflict = conflicts[currentIndex];
  const hasMultiple = conflicts.length > 1;
  
  // Bulk-Resolution bei 5+ Konflikten
  if (conflicts.length >= 5 && bulkStrategy !== 'manual') {
    return (
      <Dialog open={open} maxWidth="md" fullWidth>
        <DialogTitle>
          <Box display="flex" alignItems="center" gap={2}>
            <WarningIcon color="warning" />
            <Typography variant="h6">
              {conflicts.length} Konflikte gefunden
            </Typography>
          </Box>
        </DialogTitle>
        
        <DialogContent>
          <Alert severity="info" sx={{ mb: 3 }}>
            Mehrere Änderungen wurden offline gemacht und kollidieren mit Team-Updates.
            Wählen Sie eine Strategie für alle Konflikte:
          </Alert>
          
          <RadioGroup
            value={bulkStrategy}
            onChange={(e) => setBulkStrategy(e.target.value as any)}
          >
            <Card sx={{ mb: 2 }}>
              <CardContent>
                <FormControlLabel
                  value="smart"
                  control={<Radio />}
                  label={
                    <Box>
                      <Typography variant="subtitle1">
                        🤖 Intelligente Auflösung (Empfohlen)
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        Automatisch basierend auf Feld-Typ und Aktualität
                      </Typography>
                    </Box>
                  }
                />
              </CardContent>
            </Card>
            
            <Card sx={{ mb: 2 }}>
              <CardContent>
                <FormControlLabel
                  value="mine"
                  control={<Radio />}
                  label={
                    <Box>
                      <Typography variant="subtitle1">
                        📱 Meine Änderungen behalten
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        Alle Ihre Offline-Änderungen werden übernommen
                      </Typography>
                    </Box>
                  }
                />
              </CardContent>
            </Card>
            
            <Card sx={{ mb: 2 }}>
              <CardContent>
                <FormControlLabel
                  value="theirs"
                  control={<Radio />}
                  label={
                    <Box>
                      <Typography variant="subtitle1">
                        ☁️ Server-Version übernehmen
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        Team-Änderungen haben Vorrang
                      </Typography>
                    </Box>
                  }
                />
              </CardContent>
            </Card>
            
            <Card>
              <CardContent>
                <FormControlLabel
                  value="manual"
                  control={<Radio />}
                  label={
                    <Box>
                      <Typography variant="subtitle1">
                        ✋ Einzeln durchgehen
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        Jeden Konflikt manuell prüfen ({conflicts.length} Schritte)
                      </Typography>
                    </Box>
                  }
                />
              </CardContent>
            </Card>
          </RadioGroup>
        </DialogContent>
        
        <DialogActions>
          <Button onClick={onCancel}>Abbrechen</Button>
          <Button
            variant="contained"
            onClick={() => {
              if (bulkStrategy === 'manual') {
                setBulkStrategy('manual');
              } else {
                applyBulkStrategy(bulkStrategy);
              }
            }}
          >
            {bulkStrategy === 'manual' ? 'Einzeln prüfen' : 'Anwenden'}
          </Button>
        </DialogActions>
      </Dialog>
    );
  }
  
  // Einzelner Konflikt oder manueller Modus
  if (!currentConflict) return null;
  
  return (
    <Dialog open={open} maxWidth="lg" fullWidth>
      <DialogTitle>
        <Box display="flex" justifyContent="space-between" alignItems="center">
          <Box display="flex" alignItems="center" gap={2}>
            <CompareIcon color="warning" />
            <Typography variant="h6">
              Konflikt bei: {currentConflict.contactName}
            </Typography>
          </Box>
          {hasMultiple && (
            <Chip
              label={`${currentIndex + 1} von ${conflicts.length}`}
              color="primary"
              variant="outlined"
            />
          )}
        </Box>
      </DialogTitle>
      
      <DialogContent>
        {hasMultiple && (
          <Stepper activeStep={currentIndex} sx={{ mb: 3 }}>
            {conflicts.map((c, idx) => (
              <Step key={c.id}>
                <StepLabel>{c.contactName}</StepLabel>
              </Step>
            ))}
          </Stepper>
        )}
        
        <Grid container spacing={3}>
          {currentConflict.fields.map((field) => (
            <Grid item xs={12} key={field.fieldName}>
              <ConflictFieldCompare
                field={field}
                onResolve={(strategy) => {
                  resolutions.set(field.fieldName, strategy);
                  setResolutions(new Map(resolutions));
                }}
              />
            </Grid>
          ))}
        </Grid>
      </DialogContent>
      
      <DialogActions>
        <Button onClick={onCancel}>Abbrechen</Button>
        {hasMultiple && currentIndex > 0 && (
          <Button onClick={() => setCurrentIndex(currentIndex - 1)}>
            Zurück
          </Button>
        )}
        {hasMultiple && currentIndex < conflicts.length - 1 ? (
          <Button
            variant="contained"
            onClick={() => setCurrentIndex(currentIndex + 1)}
            disabled={!allFieldsResolved(currentConflict, resolutions)}
          >
            Weiter
          </Button>
        ) : (
          <Button
            variant="contained"
            onClick={() => onResolve(resolutions)}
            disabled={!allFieldsResolved(currentConflict, resolutions)}
          >
            Konflikte lösen
          </Button>
        )}
      </DialogActions>
    </Dialog>
  );
};

// Komponente für einzelnes Konflikt-Feld
const ConflictFieldCompare: React.FC<{
  field: ConflictField;
  onResolve: (strategy: MergeStrategy) => void;
}> = ({ field, onResolve }) => {
  const [selected, setSelected] = useState<'mine' | 'theirs' | 'merged'>();
  
  const getFieldIcon = (fieldName: string) => {
    if (fieldName.includes('phone')) return <PhoneIcon />;
    if (fieldName.includes('email')) return <EmailIcon />;
    if (fieldName.includes('name')) return <PersonIcon />;
    return <BusinessIcon />;
  };
  
  return (
    <Card variant="outlined">
      <CardContent>
        <Box display="flex" alignItems="center" gap={1} mb={2}>
          {getFieldIcon(field.fieldName)}
          <Typography variant="subtitle1" fontWeight="bold">
            {field.displayName}
          </Typography>
          {field.importance === 'critical' && (
            <Chip label="Kritisch" color="error" size="small" />
          )}
        </Box>
        
        <Grid container spacing={2}>
          <Grid item xs={12} md={5}>
            <Card
              sx={{
                border: selected === 'mine' ? 2 : 1,
                borderColor: selected === 'mine' ? 'primary.main' : 'divider',
                cursor: 'pointer'
              }}
              onClick={() => {
                setSelected('mine');
                onResolve(MergeStrategy.USE_MINE);
              }}
            >
              <CardContent>
                <Typography variant="caption" color="text.secondary">
                  📱 Ihre Version
                </Typography>
                <Typography variant="body1" sx={{ mt: 1 }}>
                  {formatValue(field.localValue)}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  {field.localVersion?.timestamp}
                </Typography>
              </CardContent>
            </Card>
          </Grid>
          
          <Grid item xs={12} md={2}>
            <Box
              display="flex"
              alignItems="center"
              justifyContent="center"
              height="100%"
            >
              <CompareIcon color="action" />
            </Box>
          </Grid>
          
          <Grid item xs={12} md={5}>
            <Card
              sx={{
                border: selected === 'theirs' ? 2 : 1,
                borderColor: selected === 'theirs' ? 'primary.main' : 'divider',
                cursor: 'pointer'
              }}
              onClick={() => {
                setSelected('theirs');
                onResolve(MergeStrategy.USE_THEIRS);
              }}
            >
              <CardContent>
                <Typography variant="caption" color="text.secondary">
                  ☁️ Server Version
                </Typography>
                <Typography variant="body1" sx={{ mt: 1 }}>
                  {formatValue(field.serverValue)}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  Von: {field.serverVersion?.user}
                </Typography>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
        
        {field.mergedValue && (
          <Card
            sx={{
              mt: 2,
              border: selected === 'merged' ? 2 : 1,
              borderColor: selected === 'merged' ? 'success.main' : 'divider',
              backgroundColor: 'action.hover',
              cursor: 'pointer'
            }}
            onClick={() => {
              setSelected('merged');
              onResolve(MergeStrategy.MERGE_BOTH);
            }}
          >
            <CardContent>
              <Box display="flex" alignItems="center" gap={1}>
                <MergeIcon color="success" />
                <Typography variant="caption" color="text.secondary">
                  Intelligenter Merge-Vorschlag
                </Typography>
              </Box>
              <Typography variant="body1" sx={{ mt: 1 }}>
                {formatValue(field.mergedValue)}
              </Typography>
            </CardContent>
          </Card>
        )}
      </CardContent>
    </Card>
  );
};

// Helper Functions
function formatValue(value: any): string {
  if (value === null || value === undefined) return '(leer)';
  if (typeof value === 'boolean') return value ? '✓ Ja' : '✗ Nein';
  if (Array.isArray(value)) return value.join(', ');
  if (typeof value === 'object') return JSON.stringify(value, null, 2);
  return String(value);
}

function allFieldsResolved(
  conflict: ContactConflict,
  resolutions: Map<string, MergeStrategy>
): boolean {
  return conflict.fields.every(field => resolutions.has(field.fieldName));
}
```

## 📋 IMPLEMENTIERUNGS-CHECKLISTE

### Phase 1: Types & Services (30 Min)
- [ ] conflict.types.ts definieren
- [ ] MergeStrategies.ts implementieren
- [ ] ConflictResolutionService erstellen

### Phase 2: UI Components (45 Min)
- [ ] ConflictDialog.tsx implementieren
- [ ] ConflictPreview komponente
- [ ] BulkConflictResolver

### Phase 3: Backend (30 Min)
- [ ] ConflictDetectionService.java
- [ ] Versioning für Optimistic Locking
- [ ] Merge-Endpoint

### Phase 4: Testing (30 Min)
- [ ] Unit Tests für Merge-Strategien
- [ ] UI Tests für Konflikt-Dialog
- [ ] E2E Test für Sync-Szenario

## 🔗 INTEGRATION POINTS

1. **OfflineQueueService** - Konflikte beim Sync erkennen
2. **ContactStore** - Resolutions anwenden
3. **SyncIndicator** - Status anzeigen
4. **ErrorBoundary** - Graceful degradation

## ⚠️ HÄUFIGE FEHLER VERMEIDEN

1. **Daten-Verlust bei Auto-Merge**
   → Lösung: Immer Backup vor Merge

2. **Überforderung bei vielen Konflikten**
   → Lösung: Bulk-Resolution anbieten

3. **Technische Error-Messages**
   → Lösung: User-freundliche Beschreibungen

---

**Status:** BEREIT FÜR IMPLEMENTIERUNG  
**Geschätzte Zeit:** 135 Minuten  
**Nächstes Dokument:** [→ Cost Management](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/COST_MANAGEMENT_EXTERNAL_SERVICES.md)  
**Parent:** [↑ Critical Success Factors](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/CRITICAL_SUCCESS_FACTORS.md)

**Konfliktlösung = Vertrauen statt Frustration! 🤝**