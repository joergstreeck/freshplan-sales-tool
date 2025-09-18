# 🛠️ Sprint 2 - Tag 3: Mobile Optimization & Audit UI

**Sprint:** Sprint 2 - Customer UI Integration & Task Preview  
**Tag:** Tag 3 von 3.5  
**Dauer:** 8 Stunden  
**Fokus:** Mobile Swipe Cards, Cockpit Teaser, FC-012 Audit UI, Performance  

---

## 📍 Navigation

### Sprint 2 Dokumente:
- **← Zurück:** [Tag 2 Implementation](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/DAY2_IMPLEMENTATION.md)
- **→ Weiter:** [Tag 3.5 Final Polish](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/DAY3_5_FINAL.md)
- **↑ Übersicht:** [Sprint 2 Overview](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/README.md)
- **📚 Quick Ref:** [Quick Reference](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/QUICK_REFERENCE.md)

### FC-005 Struktur:
- **Hauptübersicht:** [FC-005 README](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/README.md)
- **Frontend Docs:** [03-FRONTEND](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/README.md)
- **Performance:** [07-PERFORMANCE](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/07-PERFORMANCE/README.md)

### Verwandte Features:
- **FC-012 Audit Trail:** [FC-012 Tech Concept](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-012-audit-trail.md)
- **FC-011 Cockpit:** [Cockpit Integration](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-011-cockpit-integration.md)

---

## ⚠️ KRITISCH: TypeScript Import Type Requirements

**ALLE Type-Imports müssen `import type` verwenden bei `verbatimModuleSyntax: true`!**

```typescript
// ✅ RICHTIG - So müssen alle Type-Imports aussehen:
import type { Task } from '../../types/task.types';
import type { AuditLogEntry } from '../../types/audit.types';

// ❌ FALSCH - Führt zu Build-Fehlern:
import type { Task } from '../../types/task.types';
```

**Bei Problemen:** [TypeScript Import Type Guide](/Users/joergstreeck/freshplan-sales-tool/docs/guides/TYPESCRIPT_IMPORT_TYPE_GUIDE.md)

---

## 🎯 Tag 3 Ziele

1. **Swipeable Task Cards** (3h)
2. **Cockpit Teaser Widget** (2h)
3. **FC-012 Audit Admin UI** (2h)
4. **Performance Optimierung** (1h)

---

## 3.1 Swipeable Task Cards (3h)

### Mobile Task Card Component:
```typescript
// frontend/src/components/mobile/SwipeableTaskCard.tsx
import { useRef, useState } from 'react';
import { motion, useMotionValue, useTransform, PanInfo } from 'framer-motion';
import { Card, CardContent, Typography, Chip, Box } from '@mui/material';
import PhoneIcon from '@mui/icons-material/Phone';
import EmailIcon from '@mui/icons-material/Email';
import EventIcon from '@mui/icons-material/Event';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import SnoozeIcon from '@mui/icons-material/Snooze';
import type { Task } from '../../types/task.types';
import { formatDistanceToNow } from 'date-fns';
import { de } from 'date-fns/locale';

interface SwipeableTaskCardProps {
  task: Task;
  onComplete: (taskId: string) => void;
  onSnooze: (taskId: string) => void;
  onTap: (task: Task) => void;
}

export function SwipeableTaskCard({ 
  task, 
  onComplete, 
  onSnooze,
  onTap 
}: SwipeableTaskCardProps) {
  const [exitX, setExitX] = useState(0);
  const x = useMotionValue(0);
  const rotate = useTransform(x, [-200, 200], [-25, 25]);
  const opacity = useTransform(x, [-200, -100, 0, 100, 200], [0, 1, 1, 1, 0]);
  
  // Background color based on swipe direction
  const backgroundColor = useTransform(
    x,
    [-200, 0, 200],
    ['#ef5350', '#ffffff', '#66bb6a']
  );
  
  const handleDragEnd = (event: any, info: PanInfo) => {
    if (info.offset.x > 100) {
      // Swipe right - Complete
      setExitX(300);
      onComplete(task.id);
    } else if (info.offset.x < -100) {
      // Swipe left - Snooze
      setExitX(-300);
      onSnooze(task.id);
    }
  };
  
  const getTaskIcon = () => {
    switch (task.type) {
      case 'call': return <PhoneIcon />;
      case 'email': return <EmailIcon />;
      case 'meeting': return <EventIcon />;
      default: return null;
    }
  };
  
  const getPriorityColor = () => {
    switch (task.priority) {
      case 'high': return 'error';
      case 'medium': return 'warning';
      case 'low': return 'info';
      default: return 'default';
    }
  };
  
  return (
    <motion.div
      style={{ x, rotate, opacity }}
      drag="x"
      dragConstraints={{ left: -200, right: 200 }}
      onDragEnd={handleDragEnd}
      animate={{ x: exitX }}
      transition={{ type: "spring", stiffness: 300, damping: 20 }}
      whileTap={{ scale: 0.95 }}
      onClick={() => onTap(task)}
    >
      <Card
        sx={{
          position: 'relative',
          cursor: 'grab',
          '&:active': { cursor: 'grabbing' },
          overflow: 'visible',
          boxShadow: 3
        }}
      >
        {/* Swipe Indicators */}
        <Box
          sx={{
            position: 'absolute',
            top: '50%',
            left: 16,
            transform: 'translateY(-50%)',
            opacity: useTransform(x, [-100, 0], [1, 0])
          }}
        >
          <SnoozeIcon sx={{ color: 'error.main', fontSize: 32 }} />
        </Box>
        
        <Box
          sx={{
            position: 'absolute',
            top: '50%',
            right: 16,
            transform: 'translateY(-50%)',
            opacity: useTransform(x, [0, 100], [0, 1])
          }}
        >
          <CheckCircleIcon sx={{ color: 'success.main', fontSize: 32 }} />
        </Box>
        
        <CardContent sx={{ bgcolor: backgroundColor }}>
          <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 2 }}>
            <Box sx={{ color: 'action.active' }}>
              {getTaskIcon()}
            </Box>
            
            <Box sx={{ flex: 1 }}>
              <Typography variant="subtitle1" fontWeight="medium">
                {task.title}
              </Typography>
              
              <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
                {task.customerName}
              </Typography>
              
              <Box sx={{ display: 'flex', gap: 1, mt: 1, flexWrap: 'wrap' }}>
                <Chip
                  label={task.type}
                  size="small"
                  variant="outlined"
                />
                
                <Chip
                  label={task.priority}
                  size="small"
                  color={getPriorityColor()}
                />
                
                {task.isOverdue && (
                  <Chip
                    label="ÜBERFÄLLIG"
                    size="small"
                    color="error"
                  />
                )}
                
                {task.isNew && (
                  <Chip
                    label="NEU"
                    size="small"
                    sx={{ 
                      bgcolor: '#94C456',
                      color: 'white'
                    }}
                  />
                )}
              </Box>
              
              <Typography 
                variant="caption" 
                color="text.secondary"
                sx={{ mt: 1, display: 'block' }}
              >
                Fällig {formatDistanceToNow(new Date(task.dueDate), { 
                  addSuffix: true,
                  locale: de 
                })}
              </Typography>
            </Box>
          </Box>
        </CardContent>
      </Card>
    </motion.div>
  );
}
```

### Mobile Task List View:
```typescript
// frontend/src/features/tasks/components/MobileTaskList.tsx
import { useState } from 'react';
import { Box, Typography, IconButton } from '@mui/material';
import { AnimatePresence } from 'framer-motion';
import { SwipeableTaskCard } from '../../../components/mobile/SwipeableTaskCard';
import type { Task } from '../../../types/task.types';
import { useCompleteTask, useSnoozeTask } from '../hooks/useTasks';
import { useNavigate } from 'react-router-dom';
import RefreshIcon from '@mui/icons-material/Refresh';

interface MobileTaskListProps {
  tasks: Task[];
  onRefresh: () => void;
}

export function MobileTaskList({ tasks, onRefresh }: MobileTaskListProps) {
  const [visibleTasks, setVisibleTasks] = useState(tasks);
  const completeTask = useCompleteTask();
  const snoozeTask = useSnoozeTask();
  const navigate = useNavigate();
  
  const handleComplete = async (taskId: string) => {
    // Optimistic update
    setVisibleTasks(prev => prev.filter(t => t.id !== taskId));
    
    try {
      await completeTask.mutateAsync(taskId);
      // Show success feedback
      window.dispatchEvent(new CustomEvent('freshplan:show-toast', {
        detail: { message: 'Aufgabe erledigt! 🎉', type: 'success' }
      }));
    } catch (error) {
      // Revert on error
      setVisibleTasks(tasks);
    }
  };
  
  const handleSnooze = async (taskId: string) => {
    // Optimistic update
    setVisibleTasks(prev => prev.filter(t => t.id !== taskId));
    
    try {
      await snoozeTask.mutateAsync({ taskId, days: 1 });
      window.dispatchEvent(new CustomEvent('freshplan:show-toast', {
        detail: { message: 'Auf morgen verschoben', type: 'info' }
      }));
    } catch (error) {
      setVisibleTasks(tasks);
    }
  };
  
  return (
    <Box sx={{ pb: 2 }}>
      <Box sx={{ 
        display: 'flex', 
        justifyContent: 'space-between',
        alignItems: 'center',
        p: 2
      }}>
        <Typography variant="h6">
          Meine Aufgaben ({visibleTasks.length})
        </Typography>
        
        <IconButton onClick={onRefresh} size="small">
          <RefreshIcon />
        </IconButton>
      </Box>
      
      <Box sx={{ px: 2 }}>
        <AnimatePresence>
          {visibleTasks.map((task, index) => (
            <Box key={task.id} sx={{ mb: 2 }}>
              <SwipeableTaskCard
                task={task}
                onComplete={handleComplete}
                onSnooze={handleSnooze}
                onTap={(task) => navigate(`/tasks/${task.id}`)}
              />
            </Box>
          ))}
        </AnimatePresence>
        
        {visibleTasks.length === 0 && (
          <Box sx={{ 
            textAlign: 'center', 
            py: 8,
            color: 'text.secondary' 
          }}>
            <Typography variant="h6" gutterBottom>
              Keine offenen Aufgaben! 🎉
            </Typography>
            <Typography variant="body2">
              Genießen Sie die freie Zeit oder legen Sie neue Kunden an.
            </Typography>
          </Box>
        )}
      </Box>
    </Box>
  );
}
```

---

## 3.2 Cockpit Teaser Widget (2h)

```typescript
// frontend/src/components/dashboard/CockpitTeaser.tsx
import { Card, CardContent, Typography, Box, Button, Skeleton } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import AssignmentIcon from '@mui/icons-material/Assignment';
import PeopleIcon from '@mui/icons-material/People';
import { useTaskStats } from '../../features/tasks/hooks/useTasks';
import { motion } from 'framer-motion';

export function CockpitTeaser() {
  const navigate = useNavigate();
  const { data: stats, isLoading } = useTaskStats();
  
  const metrics = [
    {
      label: 'Offene Aufgaben',
      value: stats?.pending || 0,
      color: '#2196f3',
      icon: <AssignmentIcon />
    },
    {
      label: 'Heute erledigt',
      value: stats?.completedToday || 0,
      color: '#66bb6a',
      icon: <TrendingUpIcon />
    },
    {
      label: 'Überfällig',
      value: stats?.overdue || 0,
      color: stats?.overdue ? '#ef5350' : '#bdbdbd',
      icon: <AssignmentIcon />
    }
  ];
  
  return (
    <Card
      component={motion.div}
      whileHover={{ scale: 1.02 }}
      sx={{ 
        cursor: 'pointer',
        transition: 'box-shadow 0.2s',
        '&:hover': {
          boxShadow: 6
        }
      }}
      onClick={() => navigate('/cockpit')}
    >
      <CardContent>
        <Box sx={{ 
          display: 'flex', 
          justifyContent: 'space-between',
          alignItems: 'center',
          mb: 3
        }}>
          <Typography variant="h6" component="h2">
            Mein Cockpit
          </Typography>
          
          <Typography 
            variant="caption" 
            sx={{ 
              color: 'primary.main',
              fontWeight: 'medium'
            }}
          >
            Zum vollständigen Cockpit →
          </Typography>
        </Box>
        
        <Box sx={{ 
          display: 'grid', 
          gridTemplateColumns: 'repeat(3, 1fr)',
          gap: 2
        }}>
          {metrics.map((metric, index) => (
            <Box
              key={metric.label}
              sx={{
                textAlign: 'center',
                p: 2,
                borderRadius: 2,
                bgcolor: 'grey.50'
              }}
            >
              {isLoading ? (
                <>
                  <Skeleton variant="text" width="60%" sx={{ mx: 'auto' }} />
                  <Skeleton variant="text" width="40%" sx={{ mx: 'auto' }} />
                </>
              ) : (
                <>
                  <Typography
                    variant="h4"
                    sx={{ 
                      color: metric.color,
                      fontWeight: 'bold'
                    }}
                  >
                    {metric.value}
                  </Typography>
                  <Typography 
                    variant="caption" 
                    color="text.secondary"
                  >
                    {metric.label}
                  </Typography>
                </>
              )}
            </Box>
          ))}
        </Box>
        
        <Box sx={{ mt: 3, textAlign: 'center' }}>
          <Typography 
            variant="body2" 
            color="text.secondary"
            sx={{ fontStyle: 'italic' }}
          >
            "Vollständiges Dashboard mit KPIs, Charts und Insights"
          </Typography>
        </Box>
      </CardContent>
    </Card>
  );
}
```

---

## 3.3 FC-012 Audit Trail Admin UI (2h)

```typescript
// frontend/src/features/admin/pages/AuditLogPage.tsx
import { useState } from 'react';
import {
  Box,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TablePagination,
  TextField,
  InputAdornment,
  Chip,
  IconButton,
  Tooltip,
  Typography
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import FilterListIcon from '@mui/icons-material/FilterList';
import RefreshIcon from '@mui/icons-material/Refresh';
import { useAuditLogs } from '../hooks/useAuditLogs';
import { format } from 'date-fns';
import { de } from 'date-fns/locale';

export function AuditLogPage() {
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(25);
  const [searchTerm, setSearchTerm] = useState('');
  const [filters, setFilters] = useState({
    entityType: '',
    action: '',
    userId: ''
  });
  
  const { data, isLoading, refetch } = useAuditLogs({
    page,
    size: rowsPerPage,
    search: searchTerm,
    ...filters
  });
  
  const getActionColor = (action: string) => {
    switch (action) {
      case 'CREATE': return 'success';
      case 'UPDATE': return 'info';
      case 'DELETE': return 'error';
      default: return 'default';
    }
  };
  
  const getEntityIcon = (entityType: string) => {
    switch (entityType) {
      case 'Customer': return '👤';
      case 'Opportunity': return '💰';
      case 'Task': return '📋';
      case 'User': return '👥';
      default: return '📄';
    }
  };
  
  return (
    <Box sx={{ p: 3 }}>
      <Box sx={{ mb: 3, display: 'flex', justifyContent: 'space-between' }}>
        <Typography variant="h4" component="h1">
          Audit Trail
        </Typography>
        
        <Box sx={{ display: 'flex', gap: 1 }}>
          <Tooltip title="Filter">
            <IconButton>
              <FilterListIcon />
            </IconButton>
          </Tooltip>
          
          <Tooltip title="Aktualisieren">
            <IconButton onClick={() => refetch()}>
              <RefreshIcon />
            </IconButton>
          </Tooltip>
        </Box>
      </Box>
      
      <Paper sx={{ mb: 2 }}>
        <Box sx={{ p: 2 }}>
          <TextField
            fullWidth
            variant="outlined"
            placeholder="Suche nach Benutzer, Entität, Aktion..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon />
                </InputAdornment>
              )
            }}
          />
        </Box>
      </Paper>
      
      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Zeitpunkt</TableCell>
              <TableCell>Benutzer</TableCell>
              <TableCell>Aktion</TableCell>
              <TableCell>Entität</TableCell>
              <TableCell>Details</TableCell>
              <TableCell>IP-Adresse</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {data?.content.map((log) => (
              <TableRow key={log.id} hover>
                <TableCell>
                  {format(new Date(log.timestamp), 'dd.MM.yyyy HH:mm:ss', { 
                    locale: de 
                  })}
                </TableCell>
                
                <TableCell>{log.userName}</TableCell>
                
                <TableCell>
                  <Chip
                    label={log.action}
                    size="small"
                    color={getActionColor(log.action)}
                  />
                </TableCell>
                
                <TableCell>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <span>{getEntityIcon(log.entityType)}</span>
                    <span>{log.entityType}</span>
                    <Typography variant="caption" color="text.secondary">
                      #{log.entityId}
                    </Typography>
                  </Box>
                </TableCell>
                
                <TableCell>
                  <Tooltip title={log.changes || 'Keine Details'}>
                    <Typography
                      variant="body2"
                      sx={{
                        maxWidth: 300,
                        overflow: 'hidden',
                        textOverflow: 'ellipsis',
                        whiteSpace: 'nowrap'
                      }}
                    >
                      {log.description}
                    </Typography>
                  </Tooltip>
                </TableCell>
                
                <TableCell>
                  <Typography variant="caption" sx={{ fontFamily: 'monospace' }}>
                    {log.ipAddress}
                  </Typography>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
        
        <TablePagination
          component="div"
          count={data?.totalElements || 0}
          page={page}
          onPageChange={(e, newPage) => setPage(newPage)}
          rowsPerPage={rowsPerPage}
          onRowsPerPageChange={(e) => {
            setRowsPerPage(parseInt(e.target.value, 10));
            setPage(0);
          }}
          labelRowsPerPage="Einträge pro Seite:"
          labelDisplayedRows={({ from, to, count }) => 
            `${from}–${to} von ${count !== -1 ? count : `mehr als ${to}`}`
          }
        />
      </TableContainer>
    </Box>
  );
}
```

### Audit Hook:
```typescript
// frontend/src/features/admin/hooks/useAuditLogs.ts
import { useQuery } from '@tanstack/react-query';
import { auditApi } from '../../../services/api/auditApi';

interface AuditFilter {
  page?: number;
  size?: number;
  search?: string;
  entityType?: string;
  action?: string;
  userId?: string;
}

export function useAuditLogs(filter: AuditFilter = {}) {
  return useQuery({
    queryKey: ['audit-logs', filter],
    queryFn: () => auditApi.getLogs(filter),
    keepPreviousData: true
  });
}
```

---

## 3.4 Performance Optimierung (1h)

### Lazy Loading für Routes:
```typescript
// frontend/src/App.tsx
import { lazy, Suspense } from 'react';
import { Routes, Route } from 'react-router-dom';
import { LoadingScreen } from './components/common/LoadingScreen';

// Lazy load heavy pages
const CustomersPage = lazy(() => import('./pages/CustomersPage'));
const CustomerDetailPage = lazy(() => import('./pages/CustomerDetailPage'));
const AuditLogPage = lazy(() => import('./features/admin/pages/AuditLogPage'));

export function App() {
  return (
    <Suspense fallback={<LoadingScreen />}>
      <Routes>
        <Route path="/customers" element={<CustomersPage />} />
        <Route path="/customers/:id" element={<CustomerDetailPage />} />
        <Route path="/admin/audit" element={<AuditLogPage />} />
      </Routes>
    </Suspense>
  );
}
```

### Virtual Scrolling für große Listen:
```typescript
// frontend/src/features/customers/components/VirtualCustomerTable.tsx
import { useVirtual } from '@tanstack/react-virtual';
import { useRef } from 'react';

export function VirtualCustomerTable({ customers }: { customers: Customer[] }) {
  const parentRef = useRef<HTMLDivElement>(null);
  
  const rowVirtualizer = useVirtual({
    size: customers.length,
    parentRef,
    estimateSize: useCallback(() => 64, []),
    overscan: 5
  });
  
  return (
    <div ref={parentRef} style={{ height: '600px', overflow: 'auto' }}>
      <div style={{ height: `${rowVirtualizer.totalSize}px` }}>
        {rowVirtualizer.virtualItems.map(virtualRow => (
          <div
            key={virtualRow.index}
            style={{
              position: 'absolute',
              top: 0,
              left: 0,
              width: '100%',
              height: `${virtualRow.size}px`,
              transform: `translateY(${virtualRow.start}px)`
            }}
          >
            <CustomerRow customer={customers[virtualRow.index]} />
          </div>
        ))}
      </div>
    </div>
  );
}
```

---

## ✅ Tag 3 Checklist

- [ ] Swipeable Task Cards mit Animation
- [ ] Mobile Task List mit Swipe Actions
- [ ] Cockpit Teaser Widget
- [ ] FC-012 Audit Trail Admin UI
- [ ] Performance: Lazy Loading implementiert
- [ ] Virtual Scrolling für große Listen
- [ ] Mobile Tests auf echten Geräten
- [ ] Performance Metrics gemessen

---

## 📊 Performance Metriken

| Metrik | Ziel | Gemessen |
|--------|------|----------|
| Task Card Swipe | 60 FPS | ___ FPS |
| Audit Log Load | < 500ms | ___ ms |
| Bundle Size | < 250KB | ___ KB |
| Mobile LCP | < 2.5s | ___ s |

---

## 🎯 Nächste Schritte

Nach erfolgreichem Abschluss von Tag 3:
1. Performance Tests: `npm run lighthouse`
2. Mobile Tests: Auf echten Geräten testen
3. Commit: `git commit -m "feat(sprint2): implement Day 3 - Mobile & Audit UI"`
4. Weiter mit [Tag 3.5: Final Polish & Demo Prep](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/DAY3_5_FINAL.md)

---

**Remember:** Mobile First bedeutet Touch-optimiert und performant! 📱