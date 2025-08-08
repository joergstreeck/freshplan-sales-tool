# FC-012: Frontend Audit Viewer UI

**Parent:** [/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-24_TECH_CONCEPT_FC-012-audit-trail-system.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-24_TECH_CONCEPT_FC-012-audit-trail-system.md)  
**Fokus:** React UI für Audit Trail Anzeige und Export

## 🎨 UI Components

### 1. Audit Trail Table

```typescript
// frontend/src/features/audit/components/AuditTrailTable.tsx
interface AuditTrailTableProps {
  filters?: AuditFilters;
  onExport?: () => void;
}

export const AuditTrailTable: React.FC<AuditTrailTableProps> = ({ filters, onExport }) => {
  const { data, isLoading, error, fetchNextPage, hasNextPage } = useInfiniteQuery({
    queryKey: ['auditTrail', filters],
    queryFn: ({ pageParam = 0 }) => auditApi.getAuditTrail({
      ...filters,
      page: pageParam,
      pageSize: 50
    }),
    getNextPageParam: (lastPage) => 
      lastPage.hasMore ? lastPage.page + 1 : undefined,
  });

  const columns: ColumnDef<AuditEntry>[] = [
    {
      header: 'Zeitstempel',
      accessorKey: 'timestamp',
      cell: ({ getValue }) => formatDateTime(getValue()),
      size: 180,
    },
    {
      header: 'Benutzer',
      accessorKey: 'userName',
      cell: ({ row }) => (
        <UserCell 
          name={row.original.userName} 
          role={row.original.userRole}
        />
      ),
    },
    {
      header: 'Aktion',
      accessorKey: 'eventType',
      cell: ({ getValue }) => (
        <EventTypeBadge type={getValue()} />
      ),
    },
    {
      header: 'Objekt',
      accessorKey: 'entityType',
      cell: ({ row }) => (
        <EntityLink 
          type={row.original.entityType}
          id={row.original.entityId}
        />
      ),
    },
    {
      header: 'Änderungen',
      id: 'changes',
      cell: ({ row }) => (
        <ChangeDetails 
          oldValue={row.original.changeData.oldValue}
          newValue={row.original.changeData.newValue}
        />
      ),
    },
    {
      header: 'Grund',
      accessorKey: 'changeData.changeReason',
      size: 200,
    },
  ];

  return (
    <Box>
      <TableContainer>
        <DataTable
          columns={columns}
          data={data?.pages.flatMap(page => page.items) ?? []}
          onRowClick={(row) => openAuditDetail(row.id)}
          isLoading={isLoading}
          onEndReached={() => hasNextPage && fetchNextPage()}
        />
      </TableContainer>
      
      {onExport && (
        <Box sx={{ mt: 2, display: 'flex', justifyContent: 'flex-end' }}>
          <Button
            variant="outlined"
            startIcon={<DownloadIcon />}
            onClick={onExport}
          >
            Export als CSV
          </Button>
        </Box>
      )}
    </Box>
  );
};
```

### 2. Audit Filter Component

```typescript
// frontend/src/features/audit/components/AuditFilters.tsx
export const AuditFilters: React.FC<{
  onFiltersChange: (filters: AuditFilters) => void;
}> = ({ onFiltersChange }) => {
  const [filters, setFilters] = useState<AuditFilters>({
    dateRange: { from: subDays(new Date(), 7), to: new Date() },
    eventTypes: [],
    entityTypes: [],
    users: [],
  });

  return (
    <Paper sx={{ p: 2, mb: 2 }}>
      <Grid container spacing={2}>
        <Grid item xs={12} md={4}>
          <DateRangePicker
            label="Zeitraum"
            value={filters.dateRange}
            onChange={(range) => updateFilter('dateRange', range)}
          />
        </Grid>
        
        <Grid item xs={12} md={4}>
          <Autocomplete
            multiple
            options={EVENT_TYPES}
            value={filters.eventTypes}
            onChange={(_, value) => updateFilter('eventTypes', value)}
            renderInput={(params) => (
              <TextField {...params} label="Aktionstypen" />
            )}
            renderTags={(value, getTagProps) =>
              value.map((option, index) => (
                <Chip
                  label={getEventTypeLabel(option)}
                  {...getTagProps({ index })}
                  size="small"
                />
              ))
            }
          />
        </Grid>
        
        <Grid item xs={12} md={4}>
          <UserSelect
            multiple
            label="Benutzer"
            value={filters.users}
            onChange={(users) => updateFilter('users', users)}
          />
        </Grid>
        
        <Grid item xs={12}>
          <Box sx={{ display: 'flex', gap: 1 }}>
            <Button
              variant="contained"
              onClick={() => onFiltersChange(filters)}
            >
              Filter anwenden
            </Button>
            <Button
              variant="outlined"
              onClick={resetFilters}
            >
              Zurücksetzen
            </Button>
          </Box>
        </Grid>
      </Grid>
    </Paper>
  );
};
```

### 3. Audit Detail Modal

```typescript
// frontend/src/features/audit/components/AuditDetailModal.tsx
export const AuditDetailModal: React.FC<{
  auditId: string;
  open: boolean;
  onClose: () => void;
}> = ({ auditId, open, onClose }) => {
  const { data: audit, isLoading } = useQuery({
    queryKey: ['auditDetail', auditId],
    queryFn: () => auditApi.getAuditDetail(auditId),
    enabled: open && !!auditId,
  });

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>
        Audit-Details
        <IconButton
          onClick={onClose}
          sx={{ position: 'absolute', right: 8, top: 8 }}
        >
          <CloseIcon />
        </IconButton>
      </DialogTitle>
      
      <DialogContent dividers>
        {isLoading ? (
          <CircularProgress />
        ) : audit ? (
          <Box>
            <AuditSummary audit={audit} />
            <Divider sx={{ my: 2 }} />
            <ChangeComparison 
              before={audit.changeData.oldValue}
              after={audit.changeData.newValue}
            />
            <Divider sx={{ my: 2 }} />
            <AuditMetadata audit={audit} />
            <Divider sx={{ my: 2 }} />
            <IntegrityInfo 
              hash={audit.dataHash}
              previousHash={audit.previousHash}
            />
          </Box>
        ) : null}
      </DialogContent>
    </Dialog>
  );
};
```

### 4. Change Visualization

```typescript
// frontend/src/features/audit/components/ChangeVisualization.tsx
export const ChangeComparison: React.FC<{
  before: any;
  after: any;
}> = ({ before, after }) => {
  const diff = useMemo(() => 
    calculateDiff(before, after), [before, after]
  );

  return (
    <Box>
      <Typography variant="h6" gutterBottom>
        Änderungen
      </Typography>
      
      {diff.map((change, index) => (
        <Box key={index} sx={{ mb: 2 }}>
          <Typography variant="subtitle2" color="text.secondary">
            {change.path}
          </Typography>
          
          <Grid container spacing={2}>
            <Grid item xs={6}>
              <Paper 
                sx={{ 
                  p: 1, 
                  bgcolor: 'error.light',
                  opacity: 0.1 
                }}
              >
                <Typography variant="body2" sx={{ textDecoration: 'line-through' }}>
                  {formatValue(change.oldValue)}
                </Typography>
              </Paper>
            </Grid>
            
            <Grid item xs={6}>
              <Paper 
                sx={{ 
                  p: 1, 
                  bgcolor: 'success.light',
                  opacity: 0.1 
                }}
              >
                <Typography variant="body2">
                  {formatValue(change.newValue)}
                </Typography>
              </Paper>
            </Grid>
          </Grid>
        </Box>
      ))}
    </Box>
  );
};
```

### 5. Compliance Dashboard

```typescript
// frontend/src/features/audit/components/ComplianceDashboard.tsx
export const ComplianceDashboard: React.FC = () => {
  const { data: stats } = useQuery({
    queryKey: ['complianceStats'],
    queryFn: auditApi.getComplianceStats,
  });

  return (
    <Grid container spacing={3}>
      <Grid item xs={12}>
        <Typography variant="h4" gutterBottom>
          Compliance Dashboard
        </Typography>
      </Grid>
      
      <Grid item xs={12} md={3}>
        <MetricCard
          title="Audit Coverage"
          value={`${stats?.coverage ?? 0}%`}
          subtitle="Kritische Operationen"
          icon={<SecurityIcon />}
          color="success"
        />
      </Grid>
      
      <Grid item xs={12} md={3}>
        <MetricCard
          title="Integritätsstatus"
          value={stats?.integrityStatus ?? 'Prüfung läuft'}
          subtitle="Hash-Chain Validierung"
          icon={<VerifiedIcon />}
          color={stats?.integrityValid ? 'success' : 'error'}
        />
      </Grid>
      
      <Grid item xs={12} md={3}>
        <MetricCard
          title="Retention Compliance"
          value={`${stats?.retentionCompliance ?? 0}%`}
          subtitle="Aufbewahrungsfristen"
          icon={<StorageIcon />}
          color="info"
        />
      </Grid>
      
      <Grid item xs={12} md={3}>
        <MetricCard
          title="Letzte Prüfung"
          value={formatDate(stats?.lastAudit)}
          subtitle="Nächste in 7 Tagen"
          icon={<EventIcon />}
          color="default"
        />
      </Grid>
      
      <Grid item xs={12}>
        <Paper sx={{ p: 2 }}>
          <AuditActivityChart data={stats?.activityData} />
        </Paper>
      </Grid>
      
      <Grid item xs={12} md={6}>
        <Paper sx={{ p: 2 }}>
          <CriticalEventsTable events={stats?.criticalEvents} />
        </Paper>
      </Grid>
      
      <Grid item xs={12} md={6}>
        <Paper sx={{ p: 2 }}>
          <ComplianceAlerts alerts={stats?.alerts} />
        </Paper>
      </Grid>
    </Grid>
  );
};
```

## 🎯 UX Features

### 1. Smart Filtering
```typescript
// Vordefinierte Filter-Presets
const FILTER_PRESETS = {
  'today': {
    name: 'Heute',
    filters: {
      dateRange: { from: startOfDay(new Date()), to: new Date() }
    }
  },
  'critical': {
    name: 'Kritische Änderungen',
    filters: {
      eventTypes: ['CONTRACT_MODIFIED', 'PRICE_CHANGED', 'PERMISSION_CHANGED']
    }
  },
  'myActions': {
    name: 'Meine Aktionen',
    filters: {
      users: [currentUser.id]
    }
  }
};
```

### 2. Real-time Updates
```typescript
// WebSocket Integration für Live-Updates
const useAuditWebSocket = () => {
  useEffect(() => {
    const ws = new WebSocket('wss://api.freshplan.de/audit-stream');
    
    ws.on('audit:new', (entry: AuditEntry) => {
      // Update table if entry matches current filters
      if (matchesFilters(entry, currentFilters)) {
        queryClient.setQueryData(['auditTrail'], (old) => {
          return {
            ...old,
            pages: [[entry, ...old.pages[0]], ...old.pages.slice(1)]
          };
        });
        
        // Show notification for critical events
        if (isCriticalEvent(entry)) {
          toast.warning(`Kritische Aktion: ${entry.eventType}`);
        }
      }
    });
    
    return () => ws.close();
  }, [currentFilters]);
};
```

### 3. Export Options
```typescript
interface ExportOptions {
  format: 'csv' | 'pdf' | 'excel';
  includeDetails: boolean;
  anonymize: boolean;
  dateRange: DateRange;
  eventTypes?: AuditEventType[];
}

const exportAuditTrail = async (options: ExportOptions) => {
  const response = await auditApi.export(options);
  
  // Track the export in audit trail
  await auditApi.logExport({
    format: options.format,
    recordCount: response.count,
    filters: options
  });
  
  downloadFile(response.data, `audit_trail_${format(new Date(), 'yyyy-MM-dd')}.${options.format}`);
};
```

## 🔐 Access Control UI

```typescript
// Role-based UI elements
const AuditMenuItem: React.FC = () => {
  const { hasRole } = useAuth();
  
  if (!hasRole(['admin', 'auditor', 'compliance_officer'])) {
    return null;
  }
  
  return (
    <MenuItem onClick={() => navigate('/audit')}>
      <ListItemIcon>
        <SecurityIcon />
      </ListItemIcon>
      <ListItemText primary="Audit Trail" />
      {hasRole('admin') && (
        <Chip label="Admin" size="small" />
      )}
    </MenuItem>
  );
};
```