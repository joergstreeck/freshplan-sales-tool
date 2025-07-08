# FC-002-M3: Cockpit-Integration

**Modul:** M3  
**Feature:** FC-002  
**Status:** 📋 In Planung (0%)  
**Geschätzter Aufwand:** 3-4 Tage  
**Abhängigkeit:** FC-001 (Dynamic Focus List)

## 📋 Implementierungs-Checkliste

- [ ] CockpitView.tsx Hauptcontainer
- [ ] MeinTag.tsx (Spalte 1)
- [ ] FocusListColumn Integration (Spalte 2)
- [ ] AktionsCenter.tsx (Spalte 3)
- [ ] cockpitStore.ts erweitern
- [ ] Aggregierten API-Endpunkt nutzen
- [ ] Responsive Layout implementieren
- [ ] Drag & Drop für Spaltenbreiten
- [ ] Local Storage für Layout-Präferenzen
- [ ] Performance-Optimierung
- [ ] Unit & Integration Tests

## 🏗️ Komponenten-Struktur

```
frontend/src/
├── pages/
│   └── cockpit/
│       └── CockpitView.tsx             # Hauptseite
├── features/
│   └── cockpit/
│       ├── components/
│       │   ├── MeinTag/
│       │   │   ├── MeinTag.tsx         # Spalte 1 Container
│       │   │   ├── AlertsList.tsx      # Tagesalarme
│       │   │   ├── AppointmentsList.tsx # Termine
│       │   │   ├── TasksList.tsx       # Aufgaben
│       │   │   └── TriageInbox.tsx     # E-Mail Posteingang
│       │   ├── AktionsCenter/
│       │   │   ├── AktionsCenter.tsx   # Spalte 3 Container
│       │   │   ├── CustomerDetail.tsx  # Kundendetails
│       │   │   ├── ActivityTimeline.tsx # Aktivitäten
│       │   │   └── QuickActions.tsx    # Schnellaktionen
│       │   └── layout/
│       │       └── ResizablePanels.tsx # Spalten-Layout
│       ├── hooks/
│       │   └── useCockpitData.ts       # Aggregierte Daten
│       └── store/
│           └── cockpitStore.ts         # Cockpit-Zustand
```

## 📝 Detaillierte Spezifikation

### 1. Cockpit Hauptansicht

```typescript
// frontend/src/pages/cockpit/CockpitView.tsx
import React, { useEffect } from 'react';
import { Box, Container } from '@mui/material';
import { ResizablePanels } from '@/features/cockpit/components/layout/ResizablePanels';
import { MeinTag } from '@/features/cockpit/components/MeinTag/MeinTag';
import { FocusListColumn } from '@/features/customers/components/FocusListColumn';
import { AktionsCenter } from '@/features/cockpit/components/AktionsCenter/AktionsCenter';
import { useCockpitData } from '@/features/cockpit/hooks/useCockpitData';
import { useCockpitStore } from '@/features/cockpit/store/cockpitStore';
import { LoadingState, ErrorState } from '@/components/common';

export const CockpitView: React.FC = () => {
  const { selectedCustomerId, setSelectedCustomer } = useCockpitStore();
  const { data, isLoading, isError, error } = useCockpitData();

  // Track page visit
  useEffect(() => {
    document.title = 'Mein Cockpit - FreshPlan';
  }, []);

  if (isLoading) return <LoadingState message="Cockpit wird geladen..." />;
  if (isError) return <ErrorState error={error} />;

  return (
    <Container maxWidth={false} sx={{ height: '100%', py: 2 }}>
      <ResizablePanels
        panels={[
          {
            id: 'mein-tag',
            minSize: 300,
            defaultSize: 350,
            content: <MeinTag data={data} />,
          },
          {
            id: 'focus-list',
            minSize: 400,
            defaultSize: 500,
            content: (
              <FocusListColumn
                onCustomerSelect={setSelectedCustomer}
                selectedCustomerId={selectedCustomerId}
              />
            ),
          },
          {
            id: 'aktions-center',
            minSize: 400,
            content: (
              <AktionsCenter
                customerId={selectedCustomerId}
                onClose={() => setSelectedCustomer(null)}
              />
            ),
          },
        ]}
      />
    </Container>
  );
};
```

### 2. Mein Tag Komponente (Spalte 1)

```typescript
// frontend/src/features/cockpit/components/MeinTag/MeinTag.tsx
import React from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Stack,
  Chip,
  IconButton,
  Tooltip,
} from '@mui/material';
import RefreshIcon from '@mui/icons-material/Refresh';
import { AlertsList } from './AlertsList';
import { AppointmentsList } from './AppointmentsList';
import { TasksList } from './TasksList';
import { TriageInbox } from './TriageInbox';
import { CockpitOverviewData } from '@/types/cockpit.types';

interface MeinTagProps {
  data: CockpitOverviewData;
}

export const MeinTag: React.FC<MeinTagProps> = ({ data }) => {
  const { alerts, appointments, tasks, unassignedEmails, stats } = data;

  return (
    <Stack spacing={2} sx={{ height: '100%', overflow: 'auto' }}>
      {/* Header */}
      <Card>
        <CardContent>
          <Box display="flex" justifyContent="space-between" alignItems="center">
            <Typography variant="h5" component="h1">
              Mein Tag
            </Typography>
            <Box display="flex" gap={1} alignItems="center">
              <Chip
                label={new Date().toLocaleDateString('de-DE', {
                  weekday: 'long',
                  day: 'numeric',
                  month: 'long',
                })}
                color="primary"
                variant="outlined"
              />
              <Tooltip title="Aktualisieren">
                <IconButton size="small" onClick={() => window.location.reload()}>
                  <RefreshIcon />
                </IconButton>
              </Tooltip>
            </Box>
          </Box>
          
          {/* Quick Stats */}
          <Box display="flex" gap={2} mt={2}>
            <Chip
              label={`${stats.todayRevenue.toLocaleString('de-DE')} € Umsatz`}
              color="success"
              size="small"
            />
            <Chip
              label={`${stats.activeOpportunities} Verkaufschancen`}
              color="info"
              size="small"
            />
          </Box>
        </CardContent>
      </Card>

      {/* Alerts Section */}
      {alerts.length > 0 && (
        <AlertsList alerts={alerts} />
      )}

      {/* Appointments Section */}
      <AppointmentsList appointments={appointments} />

      {/* Tasks Section */}
      <TasksList tasks={tasks} />

      {/* Triage Inbox */}
      {unassignedEmails.length > 0 && (
        <TriageInbox emails={unassignedEmails} />
      )}
    </Stack>
  );
};
```

### 3. Aktions-Center (Spalte 3)

```typescript
// frontend/src/features/cockpit/components/AktionsCenter/AktionsCenter.tsx
import React from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  IconButton,
  Divider,
  Stack,
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import { CustomerDetail } from './CustomerDetail';
import { ActivityTimeline } from './ActivityTimeline';
import { QuickActions } from './QuickActions';
import { EmptyState } from '@/components/common';
import PeopleIcon from '@mui/icons-material/People';

interface AktionsCenterProps {
  customerId: string | null;
  onClose: () => void;
}

export const AktionsCenter: React.FC<AktionsCenterProps> = ({
  customerId,
  onClose,
}) => {
  if (!customerId) {
    return (
      <Card sx={{ height: '100%', display: 'flex', alignItems: 'center' }}>
        <CardContent sx={{ width: '100%' }}>
          <EmptyState
            icon={PeopleIcon}
            message="Wählen Sie einen Kunden aus der Liste"
            subMessage="Hier erscheinen dann alle Details und möglichen Aktionen"
          />
        </CardContent>
      </Card>
    );
  }

  return (
    <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      {/* Header */}
      <Box
        sx={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          p: 2,
          borderBottom: 1,
          borderColor: 'divider',
        }}
      >
        <Typography variant="h6">Aktions-Center</Typography>
        <IconButton size="small" onClick={onClose}>
          <CloseIcon />
        </IconButton>
      </Box>

      {/* Content */}
      <Box sx={{ flex: 1, overflow: 'auto' }}>
        <Stack spacing={2} sx={{ p: 2 }}>
          {/* Customer Details */}
          <CustomerDetail customerId={customerId} />
          
          <Divider />
          
          {/* Quick Actions */}
          <QuickActions customerId={customerId} />
          
          <Divider />
          
          {/* Activity Timeline */}
          <ActivityTimeline customerId={customerId} />
        </Stack>
      </Box>
    </Card>
  );
};
```

### 4. Resizable Panels Layout

```typescript
// frontend/src/features/cockpit/components/layout/ResizablePanels.tsx
import React, { useState, useRef, useEffect } from 'react';
import { Box, Divider } from '@mui/material';
import { useCockpitLayoutStore } from '@/features/cockpit/store/cockpitLayoutStore';

interface Panel {
  id: string;
  minSize: number;
  defaultSize?: number;
  content: React.ReactNode;
}

interface ResizablePanelsProps {
  panels: Panel[];
}

export const ResizablePanels: React.FC<ResizablePanelsProps> = ({ panels }) => {
  const { columnWidths, setColumnWidths } = useCockpitLayoutStore();
  const containerRef = useRef<HTMLDivElement>(null);
  const [sizes, setSizes] = useState<number[]>(() => 
    panels.map((panel, index) => 
      columnWidths[panel.id] || panel.defaultSize || 400
    )
  );
  const [isDragging, setIsDragging] = useState<number | null>(null);

  const handleMouseDown = (index: number) => (e: React.MouseEvent) => {
    e.preventDefault();
    setIsDragging(index);
  };

  useEffect(() => {
    const handleMouseMove = (e: MouseEvent) => {
      if (isDragging === null || !containerRef.current) return;

      const containerRect = containerRef.current.getBoundingClientRect();
      const containerWidth = containerRect.width;
      const mouseX = e.clientX - containerRect.left;

      setSizes((prevSizes) => {
        const newSizes = [...prevSizes];
        const totalSize = newSizes.reduce((sum, size) => sum + size, 0);
        
        // Calculate new sizes
        let leftSize = mouseX;
        let rightSize = containerWidth - mouseX;

        // Apply min size constraints
        const leftPanel = panels[isDragging];
        const rightPanel = panels[isDragging + 1];
        
        leftSize = Math.max(leftSize, leftPanel.minSize);
        rightSize = Math.max(rightSize, rightPanel.minSize);

        // Update sizes
        newSizes[isDragging] = leftSize;
        newSizes[isDragging + 1] = rightSize;

        return newSizes;
      });
    };

    const handleMouseUp = () => {
      if (isDragging !== null) {
        // Save to store
        const newWidths: Record<string, number> = {};
        panels.forEach((panel, index) => {
          newWidths[panel.id] = sizes[index];
        });
        setColumnWidths(newWidths);
      }
      setIsDragging(null);
    };

    if (isDragging !== null) {
      document.addEventListener('mousemove', handleMouseMove);
      document.addEventListener('mouseup', handleMouseUp);
      document.body.style.cursor = 'col-resize';
    }

    return () => {
      document.removeEventListener('mousemove', handleMouseMove);
      document.removeEventListener('mouseup', handleMouseUp);
      document.body.style.cursor = '';
    };
  }, [isDragging, panels, sizes, setColumnWidths]);

  return (
    <Box
      ref={containerRef}
      sx={{
        display: 'flex',
        height: '100%',
        width: '100%',
        position: 'relative',
      }}
    >
      {panels.map((panel, index) => (
        <React.Fragment key={panel.id}>
          <Box
            sx={{
              width: sizes[index],
              height: '100%',
              overflow: 'hidden',
              position: 'relative',
            }}
          >
            {panel.content}
          </Box>
          
          {index < panels.length - 1 && (
            <Divider
              orientation="vertical"
              flexItem
              sx={{
                cursor: 'col-resize',
                position: 'relative',
                '&:hover': {
                  backgroundColor: 'primary.main',
                  opacity: 0.5,
                },
                '&::before': {
                  content: '""',
                  position: 'absolute',
                  top: 0,
                  bottom: 0,
                  left: -4,
                  right: -4,
                },
              }}
              onMouseDown={handleMouseDown(index)}
            />
          )}
        </React.Fragment>
      ))}
    </Box>
  );
};
```

### 5. Cockpit Data Hook (Aggregierte API)

```typescript
// frontend/src/features/cockpit/hooks/useCockpitData.ts
import { useQuery } from '@tanstack/react-query';
import { cockpitService } from '@/services/cockpit.service';

export const useCockpitData = (date?: string) => {
  const today = date || new Date().toISOString().split('T')[0];
  
  return useQuery({
    queryKey: ['cockpit-overview', today],
    queryFn: () => cockpitService.getOverview(today),
    staleTime: 60 * 1000, // 1 Minute
    gcTime: 5 * 60 * 1000, // 5 Minuten
    refetchInterval: 30 * 1000, // Auto-refresh alle 30 Sekunden
    refetchIntervalInBackground: false,
  });
};
```

### 6. Cockpit Store

```typescript
// frontend/src/features/cockpit/store/cockpitStore.ts
import { create } from 'zustand';

interface CockpitState {
  // Selected customer
  selectedCustomerId: string | null;
  
  // View preferences
  showAlerts: boolean;
  showTriage: boolean;
  dateFilter: string; // ISO date
  
  // Actions
  setSelectedCustomer: (customerId: string | null) => void;
  toggleAlerts: () => void;
  toggleTriage: () => void;
  setDateFilter: (date: string) => void;
  resetFilters: () => void;
}

export const useCockpitStore = create<CockpitState>((set) => ({
  selectedCustomerId: null,
  showAlerts: true,
  showTriage: true,
  dateFilter: new Date().toISOString().split('T')[0],
  
  setSelectedCustomer: (customerId) => set({ selectedCustomerId: customerId }),
  
  toggleAlerts: () => set((state) => ({ showAlerts: !state.showAlerts })),
  
  toggleTriage: () => set((state) => ({ showTriage: !state.showTriage })),
  
  setDateFilter: (date) => set({ dateFilter: date }),
  
  resetFilters: () => set({
    showAlerts: true,
    showTriage: true,
    dateFilter: new Date().toISOString().split('T')[0],
  }),
}));

// Separate store for layout preferences
interface CockpitLayoutState {
  columnWidths: Record<string, number>;
  isCompactMode: boolean;
  
  setColumnWidths: (widths: Record<string, number>) => void;
  toggleCompactMode: () => void;
}

export const useCockpitLayoutStore = create<CockpitLayoutState>()(
  persist(
    (set) => ({
      columnWidths: {},
      isCompactMode: false,
      
      setColumnWidths: (widths) => set({ columnWidths: widths }),
      
      toggleCompactMode: () => set((state) => ({ 
        isCompactMode: !state.isCompactMode 
      })),
    }),
    {
      name: 'cockpit-layout-storage',
    }
  )
);
```

## 🎨 UI/UX Guidelines

### Layout-Prinzipien
- 3-Spalten-Layout als Standard
- Spalten sind resize-fähig mit Mindestbreiten
- Responsive: Auf Tablets 2 Spalten, Mobile 1 Spalte
- Smooth Animations für alle Übergänge

### Interaktions-Patterns
- Klick auf Kunde in Spalte 2 → Details in Spalte 3
- Doppelklick → Öffnet Kunden-Detailseite
- Drag & Drop für Aufgaben zwischen Status
- Keyboard Navigation zwischen Spalten (Tab)

### Performance
- Virtual Scrolling für lange Listen
- Lazy Loading für Timeline-Einträge
- Optimistic Updates für alle Aktionen
- Background Refresh alle 30 Sekunden

## 🧪 Test-Szenarien

```typescript
describe('CockpitView', () => {
  it('sollte alle 3 Spalten rendern');
  it('sollte aggregierte Daten laden');
  it('sollte Spaltenbreiten anpassen lassen');
  it('sollte Layout-Präferenzen speichern');
  it('sollte Kundendetails bei Auswahl anzeigen');
  it('sollte Auto-Refresh durchführen');
  it('sollte responsive auf verschiedenen Geräten sein');
});
```

## 📱 Responsive Breakpoints

```typescript
// Desktop (>1280px): 3 Spalten
// Tablet (768-1280px): 2 Spalten (Mein Tag + Focus List)
// Mobile (<768px): 1 Spalte mit Tab-Navigation

const breakpoints = {
  mobile: '@media (max-width: 767px)',
  tablet: '@media (min-width: 768px) and (max-width: 1279px)',
  desktop: '@media (min-width: 1280px)',
};
```

---

**Nächste Schritte:**
1. CockpitView Page implementieren
2. API-Service für aggregierte Daten
3. Spalten-Komponenten entwickeln
4. Responsive Layout testen