# FC-015: Permission Management UI

**Zurück:** [FC-015 Hauptkonzept](/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-24_TECH_CONCEPT_FC-015-rights-roles-concept.md)  
**Vorheriges:** [Approval Workflows](./approval-workflows.md)

## 📋 Übersicht

Administratives UI für die Verwaltung von Rollen, Permissions, Delegationen und Workflows.

## 🎨 UI-Komponenten

### Permission Management Dashboard
```typescript
export const PermissionManagementDashboard: React.FC = () => {
  const [activeTab, setActiveTab] = useState(0);
  const { hasPermission } = usePermissions();
  
  if (!hasPermission('system.manage_permissions')) {
    return <AccessDenied />;
  }

  return (
    <Box sx={{ width: '100%' }}>
      <Typography variant="h4" gutterBottom>
        Berechtigungsverwaltung
      </Typography>
      
      <Tabs value={activeTab} onChange={(_, tab) => setActiveTab(tab)}>
        <Tab label="Rollen" />
        <Tab label="Benutzer" />
        <Tab label="Delegationen" />
        <Tab label="Workflows" />
        <Tab label="Audit Log" />
      </Tabs>
      
      <Box sx={{ mt: 3 }}>
        {activeTab === 0 && <RoleManagement />}
        {activeTab === 1 && <UserPermissions />}
        {activeTab === 2 && <DelegationOverview />}
        {activeTab === 3 && <WorkflowConfiguration />}
        {activeTab === 4 && <PermissionAuditLog />}
      </Box>
    </Box>
  );
};
```

### Role Management Component
```typescript
interface Role {
  id: string;
  name: string;
  description: string;
  permissions: Permission[];
  isSystem: boolean;
  userCount: number;
}

export const RoleManagement: React.FC = () => {
  const { data: roles } = useQuery(['roles'], fetchRoles);
  const [selectedRole, setSelectedRole] = useState<Role | null>(null);
  const [showCreateDialog, setShowCreateDialog] = useState(false);

  return (
    <Grid container spacing={3}>
      <Grid item xs={12} md={4}>
        <Paper sx={{ p: 2 }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
            <Typography variant="h6">Rollen</Typography>
            <Button
              startIcon={<AddIcon />}
              onClick={() => setShowCreateDialog(true)}
              size="small"
            >
              Neue Rolle
            </Button>
          </Box>
          
          <List>
            {roles?.map(role => (
              <ListItem
                key={role.id}
                button
                selected={selectedRole?.id === role.id}
                onClick={() => setSelectedRole(role)}
              >
                <ListItemText
                  primary={role.name}
                  secondary={`${role.userCount} Benutzer`}
                />
                {role.isSystem && (
                  <Chip label="System" size="small" />
                )}
              </ListItem>
            ))}
          </List>
        </Paper>
      </Grid>
      
      <Grid item xs={12} md={8}>
        {selectedRole ? (
          <RoleEditor role={selectedRole} onUpdate={handleRoleUpdate} />
        ) : (
          <EmptyState message="Wählen Sie eine Rolle aus" />
        )}
      </Grid>
      
      <CreateRoleDialog
        open={showCreateDialog}
        onClose={() => setShowCreateDialog(false)}
        onCreate={handleCreateRole}
      />
    </Grid>
  );
};
```

### Role Editor with Permission Tree
```typescript
export const RoleEditor: React.FC<RoleEditorProps> = ({ role, onUpdate }) => {
  const { data: allPermissions } = useQuery(['permissions'], fetchAllPermissions);
  const [selectedPermissions, setSelectedPermissions] = useState(
    new Set(role.permissions.map(p => p.key))
  );
  const [expandedCategories, setExpandedCategories] = useState<Set<string>>(new Set());

  const permissionTree = useMemo(() => {
    if (!allPermissions) return {};
    
    return allPermissions.reduce((acc, perm) => {
      if (!acc[perm.category]) acc[perm.category] = [];
      acc[perm.category].push(perm);
      return acc;
    }, {} as Record<string, Permission[]>);
  }, [allPermissions]);

  const handlePermissionToggle = (permKey: string) => {
    const newSet = new Set(selectedPermissions);
    if (newSet.has(permKey)) {
      newSet.delete(permKey);
    } else {
      newSet.add(permKey);
    }
    setSelectedPermissions(newSet);
  };

  return (
    <Paper sx={{ p: 3 }}>
      <Box sx={{ mb: 3 }}>
        <Typography variant="h5" gutterBottom>
          {role.name}
        </Typography>
        <Typography variant="body2" color="text.secondary">
          {role.description}
        </Typography>
      </Box>

      <Divider sx={{ my: 2 }} />

      <Typography variant="h6" gutterBottom>
        Berechtigungen
      </Typography>

      <TreeView
        defaultCollapseIcon={<ExpandMoreIcon />}
        defaultExpandIcon={<ChevronRightIcon />}
        expanded={Array.from(expandedCategories)}
        onNodeToggle={(_, nodeIds) => setExpandedCategories(new Set(nodeIds))}
      >
        {Object.entries(permissionTree).map(([category, permissions]) => (
          <TreeItem key={category} nodeId={category} label={
            <Box sx={{ display: 'flex', alignItems: 'center', py: 1 }}>
              <Typography variant="subtitle1">{category}</Typography>
              <Chip
                label={`${permissions.filter(p => selectedPermissions.has(p.key)).length}/${permissions.length}`}
                size="small"
                sx={{ ml: 2 }}
              />
            </Box>
          }>
            {permissions.map(permission => (
              <TreeItem
                key={permission.key}
                nodeId={permission.key}
                label={
                  <FormControlLabel
                    control={
                      <Checkbox
                        checked={selectedPermissions.has(permission.key)}
                        onChange={() => handlePermissionToggle(permission.key)}
                        onClick={(e) => e.stopPropagation()}
                        disabled={role.isSystem}
                      />
                    }
                    label={
                      <Box>
                        <Typography variant="body2">
                          {permission.action}
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                          {permission.description}
                        </Typography>
                      </Box>
                    }
                  />
                }
              />
            ))}
          </TreeItem>
        ))}
      </TreeView>

      <Box sx={{ mt: 3, display: 'flex', justifyContent: 'flex-end' }}>
        <Button
          variant="contained"
          onClick={() => onUpdate(Array.from(selectedPermissions))}
          disabled={role.isSystem}
        >
          Speichern
        </Button>
      </Box>
    </Paper>
  );
};
```

### User Permission Overview
```typescript
export const UserPermissions: React.FC = () => {
  const [searchTerm, setSearchTerm] = useState('');
  const { data: users } = useQuery(
    ['users', searchTerm],
    () => searchUsers(searchTerm),
    { debounce: 300 }
  );

  return (
    <Box>
      <TextField
        fullWidth
        label="Benutzer suchen"
        value={searchTerm}
        onChange={(e) => setSearchTerm(e.target.value)}
        InputProps={{
          startAdornment: <SearchIcon />
        }}
        sx={{ mb: 3 }}
      />

      <DataGrid
        rows={users || []}
        columns={[
          { field: 'name', headerName: 'Name', flex: 1 },
          { field: 'email', headerName: 'E-Mail', flex: 1 },
          { 
            field: 'roles',
            headerName: 'Rollen',
            flex: 1,
            renderCell: (params) => (
              <Box>
                {params.value.map((role: string) => (
                  <Chip key={role} label={role} size="small" sx={{ mr: 0.5 }} />
                ))}
              </Box>
            )
          },
          {
            field: 'lastActive',
            headerName: 'Letzte Aktivität',
            width: 150,
            renderCell: (params) => (
              <Typography variant="caption">
                {dayjs(params.value).fromNow()}
              </Typography>
            )
          },
          {
            field: 'actions',
            headerName: 'Aktionen',
            width: 120,
            renderCell: (params) => (
              <IconButton
                size="small"
                onClick={() => handleEditUser(params.row)}
              >
                <EditIcon />
              </IconButton>
            )
          }
        ]}
        autoHeight
        pageSize={10}
      />
    </Box>
  );
};
```

### Delegation Overview
```typescript
export const DelegationOverview: React.FC = () => {
  const { data: delegations } = useQuery(['delegations', 'all'], fetchAllDelegations);
  const [filter, setFilter] = useState<'all' | 'active' | 'planned' | 'expired'>('all');

  const filteredDelegations = useMemo(() => {
    if (!delegations) return [];
    if (filter === 'all') return delegations;
    return delegations.filter(d => d.status === filter.toUpperCase());
  }, [delegations, filter]);

  return (
    <Box>
      <Box sx={{ mb: 3, display: 'flex', gap: 2 }}>
        <ToggleButtonGroup
          value={filter}
          exclusive
          onChange={(_, value) => value && setFilter(value)}
        >
          <ToggleButton value="all">Alle</ToggleButton>
          <ToggleButton value="active">Aktiv</ToggleButton>
          <ToggleButton value="planned">Geplant</ToggleButton>
          <ToggleButton value="expired">Abgelaufen</ToggleButton>
        </ToggleButtonGroup>
      </Box>

      <Grid container spacing={2}>
        {filteredDelegations.map(delegation => (
          <Grid item xs={12} md={6} key={delegation.id}>
            <DelegationCard
              delegation={delegation}
              onEdit={handleEditDelegation}
              onRevoke={handleRevokeDelegation}
            />
          </Grid>
        ))}
      </Grid>
    </Box>
  );
};
```

### Permission Matrix View
```typescript
export const PermissionMatrix: React.FC = () => {
  const { data: roles } = useQuery(['roles'], fetchRoles);
  const { data: permissions } = useQuery(['permissions'], fetchAllPermissions);
  
  const matrix = useMemo(() => {
    if (!roles || !permissions) return [];
    
    return permissions.map(permission => ({
      permission,
      roles: roles.reduce((acc, role) => {
        acc[role.id] = role.permissions.some(p => p.key === permission.key);
        return acc;
      }, {} as Record<string, boolean>)
    }));
  }, [roles, permissions]);

  return (
    <TableContainer component={Paper}>
      <Table stickyHeader>
        <TableHead>
          <TableRow>
            <TableCell>Berechtigung</TableCell>
            {roles?.map(role => (
              <TableCell key={role.id} align="center">
                <Typography variant="caption">{role.name}</Typography>
              </TableCell>
            ))}
          </TableRow>
        </TableHead>
        <TableBody>
          {matrix.map(({ permission, roles: roleMap }) => (
            <TableRow key={permission.key}>
              <TableCell>
                <Typography variant="body2">{permission.key}</Typography>
                <Typography variant="caption" color="text.secondary">
                  {permission.description}
                </Typography>
              </TableCell>
              {roles?.map(role => (
                <TableCell key={role.id} align="center">
                  {roleMap[role.id] && <CheckIcon color="success" />}
                </TableCell>
              ))}
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
};
```

## 📊 Analytics & Reporting

### Permission Usage Dashboard
```typescript
export const PermissionAnalytics: React.FC = () => {
  const { data: analytics } = useQuery(
    ['permission-analytics'],
    fetchPermissionAnalytics,
    { refetchInterval: 300000 } // 5 minutes
  );

  return (
    <Grid container spacing={3}>
      <Grid item xs={12} md={6}>
        <Paper sx={{ p: 2 }}>
          <Typography variant="h6" gutterBottom>
            Meistgenutzte Berechtigungen
          </Typography>
          <BarChart
            data={analytics?.topPermissions || []}
            xAxis="permission"
            yAxis="usageCount"
            height={300}
          />
        </Paper>
      </Grid>
      
      <Grid item xs={12} md={6}>
        <Paper sx={{ p: 2 }}>
          <Typography variant="h6" gutterBottom>
            Fehlgeschlagene Zugriffe
          </Typography>
          <LineChart
            data={analytics?.deniedAccess || []}
            xAxis="date"
            yAxis="count"
            height={300}
          />
        </Paper>
      </Grid>
      
      <Grid item xs={12}>
        <Paper sx={{ p: 2 }}>
          <Typography variant="h6" gutterBottom>
            Aktive Delegationen nach Abteilung
          </Typography>
          <PieChart
            data={analytics?.delegationsByDepartment || []}
            height={300}
          />
        </Paper>
      </Grid>
    </Grid>
  );
};
```

## 🔐 Security Considerations

### Admin UI Security
```typescript
// Zusätzliche Sicherheitsprüfungen für Admin UI
export const useAdminSecurity = () => {
  const { hasPermission, user } = useAuth();
  
  // Zwei-Faktor-Authentifizierung für kritische Aktionen
  const require2FA = async (action: string) => {
    if (!user.has2FAEnabled) {
      throw new Error('2FA required for this action');
    }
    
    const token = await prompt2FAToken();
    return verify2FA(token);
  };
  
  // IP-Whitelist für Admin-Zugriff
  const checkIPWhitelist = () => {
    const allowedIPs = process.env.REACT_APP_ADMIN_IP_WHITELIST?.split(',') || [];
    const currentIP = getCurrentIP();
    
    if (allowedIPs.length > 0 && !allowedIPs.includes(currentIP)) {
      throw new Error('Access denied from this IP');
    }
  };
  
  return { require2FA, checkIPWhitelist };
};
```

## 🔗 API Integration

### Permission Management API Client
```typescript
export const permissionApi = {
  // Roles
  fetchRoles: () => apiClient.get<Role[]>('/api/admin/roles'),
  createRole: (role: CreateRoleRequest) => apiClient.post('/api/admin/roles', role),
  updateRole: (id: string, updates: UpdateRoleRequest) => 
    apiClient.put(`/api/admin/roles/${id}`, updates),
  deleteRole: (id: string) => apiClient.delete(`/api/admin/roles/${id}`),
  
  // Permissions
  fetchAllPermissions: () => apiClient.get<Permission[]>('/api/admin/permissions'),
  
  // User Permissions
  getUserPermissions: (userId: string) => 
    apiClient.get<UserPermissions>(`/api/admin/users/${userId}/permissions`),
  updateUserPermissions: (userId: string, permissions: string[]) =>
    apiClient.put(`/api/admin/users/${userId}/permissions`, { permissions }),
    
  // Analytics
  fetchPermissionAnalytics: (timeframe: string = '7d') =>
    apiClient.get('/api/admin/permissions/analytics', { params: { timeframe } })
};
```

## 🎯 Best Practices

1. **Bulk Operations**
   - Batch-Updates für bessere Performance
   - Undo/Redo für kritische Änderungen

2. **Audit Trail**
   - Jede Änderung wird protokolliert
   - Export-Funktion für Compliance

3. **Import/Export**
   - Role Templates als JSON
   - Backup vor großen Änderungen

## 🔗 Verknüpfungen

- **Hauptkonzept:** [FC-015 Übersicht](/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-24_TECH_CONCEPT_FC-015-rights-roles-concept.md)
- **Backend API:** [Permission API Documentation](./api/permission-api.md)
- **Integration:** [FC-011 Cockpit Integration](/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-24_TECH_CONCEPT_FC-011-pipeline-cockpit-integration.md)