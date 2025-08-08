# 🌡️ Warmth Indicator - Beziehungstemperatur-Anzeige

**Status:** 📋 GEPLANT  
**Komponente von:** Smart Contact Cards  
**Letzte Aktualisierung:** 08.08.2025  
**Claude-Ready:** ✅ Vollständig navigierbar

## 🧭 NAVIGATION FÜR CLAUDE

**← Parent:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/SMART_CONTACT_CARDS.md`  
**↑ Übergeordnet:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/README.md`

## ⚡ Quick Implementation

```bash
# CLAUDE: Erstelle diese Komponente als Teil von Smart Contact Cards
cd /Users/joergstreeck/freshplan-sales-tool
touch frontend/src/features/customers/components/contacts/WarmthIndicator.tsx
```

## 💻 VOLLSTÄNDIGE IMPLEMENTIERUNG

**Datei:** `frontend/src/features/customers/components/contacts/WarmthIndicator.tsx`

```typescript
// WarmthIndicator.tsx - Visualisiert Beziehungstemperatur
import React from 'react';
import { Box, LinearProgress, Typography, Tooltip, Chip } from '@mui/material';
import {
  LocalFireDepartment as FireIcon,
  AcUnit as ColdIcon,
  WbSunny as WarmIcon,
  Warning as WarningIcon
} from '@mui/icons-material';

export interface WarmthIndicatorProps {
  warmthScore: number; // 0-100
  freshnessLevel: 'fresh' | 'aging' | 'stale' | 'critical' | 'no_contact';
  size?: 'small' | 'medium' | 'large';
  showDetails?: boolean;
  showTrend?: boolean;
  trendDirection?: 'up' | 'down' | 'stable';
  lastContactDays?: number;
}

export const WarmthIndicator: React.FC<WarmthIndicatorProps> = ({
  warmthScore,
  freshnessLevel,
  size = 'medium',
  showDetails = false,
  showTrend = false,
  trendDirection = 'stable',
  lastContactDays
}) => {
  // Temperatur-Kategorisierung
  const getTemperatureCategory = (score: number) => {
    if (score >= 75) return { label: 'Heiß', color: '#FF4444', icon: FireIcon };
    if (score >= 50) return { label: 'Warm', color: '#FF8800', icon: WarmIcon };
    if (score >= 25) return { label: 'Kühl', color: '#FFBB00', icon: WarningIcon };
    return { label: 'Kalt', color: '#666666', icon: ColdIcon };
  };

  // Freshness-Farben
  const getFreshnessColor = (level: string) => {
    const colors = {
      fresh: '#4CAF50',      // Grün
      aging: '#FFC107',      // Gelb
      stale: '#FF9800',      // Orange
      critical: '#F44336',   // Rot
      no_contact: '#9E9E9E'  // Grau
    };
    return colors[level] || colors.no_contact;
  };

  const temp = getTemperatureCategory(warmthScore);
  const Icon = temp.icon;
  
  // Größen-Konfiguration
  const sizeConfig = {
    small: { height: 4, fontSize: '0.75rem', iconSize: 16 },
    medium: { height: 8, fontSize: '0.875rem', iconSize: 20 },
    large: { height: 12, fontSize: '1rem', iconSize: 24 }
  };
  
  const config = sizeConfig[size];

  return (
    <Box>
      {/* Hauptanzeige */}
      <Box display="flex" alignItems="center" gap={1}>
        <Tooltip title={`Beziehungstemperatur: ${temp.label} (${warmthScore}%)`}>
          <Icon 
            sx={{ 
              fontSize: config.iconSize,
              color: temp.color
            }} 
          />
        </Tooltip>
        
        <Box flex={1}>
          <LinearProgress
            variant="determinate"
            value={warmthScore}
            sx={{
              height: config.height,
              borderRadius: 1,
              backgroundColor: 'grey.200',
              '& .MuiLinearProgress-bar': {
                backgroundColor: temp.color,
                borderRadius: 1
              }
            }}
          />
        </Box>
        
        {showDetails && (
          <Typography 
            variant="caption" 
            sx={{ 
              fontWeight: 'bold',
              color: temp.color,
              fontSize: config.fontSize,
              minWidth: '35px'
            }}
          >
            {warmthScore}%
          </Typography>
        )}
      </Box>
      
      {/* Erweiterte Details */}
      {showDetails && (
        <Box mt={1} display="flex" gap={0.5} flexWrap="wrap">
          {/* Freshness Chip */}
          <Chip
            label={getFreshnessLabel(freshnessLevel)}
            size="small"
            sx={{
              backgroundColor: getFreshnessColor(freshnessLevel),
              color: 'white',
              fontWeight: 'medium',
              fontSize: '0.7rem',
              height: '20px'
            }}
          />
          
          {/* Trend Indicator */}
          {showTrend && trendDirection !== 'stable' && (
            <Chip
              label={trendDirection === 'up' ? '↑ Steigend' : '↓ Fallend'}
              size="small"
              color={trendDirection === 'up' ? 'success' : 'error'}
              variant="outlined"
              sx={{ 
                fontSize: '0.7rem',
                height: '20px'
              }}
            />
          )}
          
          {/* Letzter Kontakt */}
          {lastContactDays !== undefined && (
            <Chip
              label={`${lastContactDays} Tage`}
              size="small"
              variant="outlined"
              sx={{ 
                fontSize: '0.7rem',
                height: '20px'
              }}
            />
          )}
        </Box>
      )}
    </Box>
  );
};

// Helper Funktionen
function getFreshnessLabel(level: string): string {
  const labels = {
    fresh: 'Frisch',
    aging: 'Alternd',
    stale: 'Veraltet',
    critical: 'Kritisch',
    no_contact: 'Kein Kontakt'
  };
  return labels[level] || 'Unbekannt';
}

// Export für Tests
export const WarmthIndicatorTestIds = {
  container: 'warmth-indicator',
  progressBar: 'warmth-progress',
  temperatureIcon: 'warmth-temp-icon',
  freshnessChip: 'warmth-freshness',
  trendChip: 'warmth-trend'
};
```

## 🎨 USAGE BEISPIELE

### Basic Usage
```typescript
<WarmthIndicator 
  warmthScore={75} 
  freshnessLevel="fresh"
/>
```

### Mit Details
```typescript
<WarmthIndicator 
  warmthScore={45}
  freshnessLevel="stale"
  size="large"
  showDetails={true}
  showTrend={true}
  trendDirection="down"
  lastContactDays={45}
/>
```

### In Contact Card
```typescript
{contact.warmthScore && (
  <WarmthIndicator
    warmthScore={contact.warmthScore}
    freshnessLevel={contact.freshnessLevel}
    size="small"
    showDetails={isHovered}
  />
)}
```

## 🧪 TEST CASES

```typescript
// __tests__/WarmthIndicator.test.tsx
describe('WarmthIndicator', () => {
  it('should display correct temperature category', () => {
    const { rerender } = render(<WarmthIndicator warmthScore={80} freshnessLevel="fresh" />);
    expect(screen.getByTestId('warmth-temp-icon')).toHaveClass('FireIcon');
    
    rerender(<WarmthIndicator warmthScore={20} freshnessLevel="critical" />);
    expect(screen.getByTestId('warmth-temp-icon')).toHaveClass('ColdIcon');
  });
  
  it('should show freshness chip when details enabled', () => {
    render(<WarmthIndicator warmthScore={50} freshnessLevel="aging" showDetails />);
    expect(screen.getByText('Alternd')).toBeInTheDocument();
  });
});
```

## 📊 PERFORMANCE CONSIDERATIONS

- **Memoization:** Component ist bereits optimiert mit React.memo (in Parent)
- **Re-renders:** Nur bei Score/Level-Änderung
- **Animation:** CSS-basiert, keine JS-Animationen

---

**Status:** BEREIT FÜR IMPLEMENTIERUNG  
**Geschätzte Zeit:** 15 Minuten  
**Integration in:** SmartContactCard.tsx