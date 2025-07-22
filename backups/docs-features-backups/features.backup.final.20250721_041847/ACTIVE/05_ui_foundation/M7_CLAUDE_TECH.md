# M7 Settings Enhancement - CLAUDE TECH 🤖

## 🧠 QUICK-LOAD (30 Sekunden bis zur Produktivität)

**Feature:** Tab-basiertes Settings Management mit Admin Features  
**Stack:** React + MUI Tabs + Permission-aware UI + Auto-Save  
**Status:** 🟡 50% FERTIG - SettingsPage.tsx vorhanden, Enhancement nötig  
**Dependencies:** FC-008 Security (✅) | FC-009 Permissions (✅) | User Management  

**Jump to:** [📚 Recipes](#-implementation-recipes) | [🧪 Tests](#-test-patterns) | [🔌 Integration](#-integration-cookbook) | [🎯 Admin Features](#-admin-features)

**Core Purpose in 1 Line:** `User Preferences → System Settings → Admin Controls → DSGVO Compliance`

---

## 🍳 IMPLEMENTATION RECIPES

### Recipe 1: Enhanced Settings Page in 5 Minuten
```typescript
// 1. Enhanced Settings Page with Tabs (copy-paste ready)
export const SettingsPageV2: React.FC = () => {
    const [activeTab, setActiveTab] = useState(0);
    const { user } = useAuth();
    const { hasPermission } = usePermissions();
    const [unsavedChanges, setUnsavedChanges] = useState(false);
    
    // Permission-aware tabs
    const tabs = [
        { label: 'Benutzer', component: <UserPreferencesTab /> },
        { label: 'Benachrichtigungen', component: <NotificationSettingsTab /> },
        { label: 'Datenschutz', component: <PrivacySettingsTab /> },
        ...(hasPermission('system:admin') ? [
            { label: 'System', component: <SystemSettingsTab /> }
        ] : []),
        ...(hasPermission('users:admin') ? [
            { label: 'Benutzerverwaltung', component: <UserManagementTab /> }
        ] : [])
    ];
    
    // Warn on unsaved changes
    useEffect(() => {
        const handleBeforeUnload = (e: BeforeUnloadEvent) => {
            if (unsavedChanges) {
                e.preventDefault();
                e.returnValue = '';
            }
        };
        
        window.addEventListener('beforeunload', handleBeforeUnload);
        return () => window.removeEventListener('beforeunload', handleBeforeUnload);
    }, [unsavedChanges]);
    
    return (
        <Container maxWidth="xl" sx={{ py: 3 }}>
            <Box sx={{ mb: 3 }}>
                <Typography variant="h4" component="h1">
                    Einstellungen
                </Typography>
                <Typography variant="body2" color="text.secondary">
                    Verwalten Sie Ihre persönlichen Einstellungen und Systemkonfigurationen
                </Typography>
            </Box>
            
            <Paper sx={{ width: '100%' }}>
                <Tabs 
                    value={activeTab} 
                    onChange={(e, v) => setActiveTab(v)}
                    variant="scrollable"
                    scrollButtons="auto"
                    sx={{ borderBottom: 1, borderColor: 'divider' }}
                >
                    {tabs.map((tab, index) => (
                        <Tab key={index} label={tab.label} />
                    ))}
                </Tabs>
                
                <Box sx={{ p: 3 }}>
                    {tabs[activeTab]?.component}
                </Box>
            </Paper>
        </Container>
    );
};
```

### Recipe 2: User Preferences Tab with Auto-Save
```typescript
// 2. User Preferences Tab (copy-paste ready)
export const UserPreferencesTab: React.FC = () => {
    const { user, updatePreferences } = useAuth();
    const [preferences, setPreferences] = useState(user.preferences);
    const [saving, setSaving] = useState(false);
    
    // Auto-save with debouncing
    const debouncedSave = useMemo(
        () => debounce(async (newPreferences: UserPreferences) => {
            setSaving(true);
            try {
                await apiClient.patch(`/api/users/${user.id}/preferences`, newPreferences);
                toast.success('Einstellungen gespeichert');
            } catch (error) {
                toast.error('Speichern fehlgeschlagen');
            } finally {
                setSaving(false);
            }
        }, 1000),
        [user.id]
    );
    
    const handleChange = (key: string, value: any) => {
        const newPreferences = { ...preferences, [key]: value };
        setPreferences(newPreferences);
        debouncedSave(newPreferences);
    };
    
    return (
        <Grid container spacing={3}>
            <Grid item xs={12} md={6}>
                <Card>
                    <CardHeader 
                        title="Persönliche Daten"
                        action={saving && <CircularProgress size={20} />}
                    />
                    <CardContent>
                        <Stack spacing={2}>
                            <TextField
                                label="Anzeigename"
                                value={preferences.displayName}
                                onChange={(e) => handleChange('displayName', e.target.value)}
                                fullWidth
                            />
                            <TextField
                                label="E-Mail"
                                value={preferences.email}
                                onChange={(e) => handleChange('email', e.target.value)}
                                fullWidth
                                type="email"
                            />
                            <TextField
                                label="Telefon"
                                value={preferences.phone}
                                onChange={(e) => handleChange('phone', e.target.value)}
                                fullWidth
                            />
                            <FormControl fullWidth>
                                <InputLabel>Sprache</InputLabel>
                                <Select
                                    value={preferences.language}
                                    onChange={(e) => handleChange('language', e.target.value)}
                                    label="Sprache"
                                >
                                    <MenuItem value="de">Deutsch</MenuItem>
                                    <MenuItem value="en">English</MenuItem>
                                </Select>
                            </FormControl>
                        </Stack>
                    </CardContent>
                </Card>
            </Grid>
            
            <Grid item xs={12} md={6}>
                <Card>
                    <CardHeader title="Erscheinungsbild" />
                    <CardContent>
                        <Stack spacing={2}>
                            <FormControl>
                                <FormLabel>Theme</FormLabel>
                                <RadioGroup
                                    value={preferences.theme}
                                    onChange={(e) => handleChange('theme', e.target.value)}
                                >
                                    <FormControlLabel value="light" control={<Radio />} label="Hell" />
                                    <FormControlLabel value="dark" control={<Radio />} label="Dunkel" />
                                    <FormControlLabel value="auto" control={<Radio />} label="System" />
                                </RadioGroup>
                            </FormControl>
                            
                            <FormControlLabel
                                control={
                                    <Switch
                                        checked={preferences.compactMode}
                                        onChange={(e) => handleChange('compactMode', e.target.checked)}
                                    />
                                }
                                label="Kompakte Ansicht"
                            />
                        </Stack>
                    </CardContent>
                </Card>
            </Grid>
        </Grid>
    );
};
```

### Recipe 3: Notification Settings Tab
```typescript
// 3. Notification Settings (copy-paste ready)
export const NotificationSettingsTab: React.FC = () => {
    const { user } = useAuth();
    const [settings, setSettings] = useState(user.notificationSettings);
    
    const categories = [
        { key: 'opportunities', label: 'Opportunities', icon: <TrendingUpIcon /> },
        { key: 'tasks', label: 'Aufgaben', icon: <TaskIcon /> },
        { key: 'messages', label: 'Nachrichten', icon: <MessageIcon /> },
        { key: 'system', label: 'System', icon: <SettingsIcon /> }
    ];
    
    return (
        <Grid container spacing={3}>
            {categories.map(category => (
                <Grid item xs={12} sm={6} key={category.key}>
                    <Card>
                        <CardHeader
                            avatar={category.icon}
                            title={category.label}
                        />
                        <CardContent>
                            <FormGroup>
                                <FormControlLabel
                                    control={
                                        <Switch
                                            checked={settings[category.key]?.email}
                                            onChange={(e) => updateSetting(category.key, 'email', e.target.checked)}
                                        />
                                    }
                                    label="E-Mail Benachrichtigungen"
                                />
                                <FormControlLabel
                                    control={
                                        <Switch
                                            checked={settings[category.key]?.push}
                                            onChange={(e) => updateSetting(category.key, 'push', e.target.checked)}
                                        />
                                    }
                                    label="Push Benachrichtigungen"
                                />
                                <FormControlLabel
                                    control={
                                        <Switch
                                            checked={settings[category.key]?.inApp}
                                            onChange={(e) => updateSetting(category.key, 'inApp', e.target.checked)}
                                        />
                                    }
                                    label="In-App Benachrichtigungen"
                                />
                            </FormGroup>
                        </CardContent>
                    </Card>
                </Grid>
            ))}
        </Grid>
    );
};
```

---

## 🧪 TEST PATTERNS

### Pattern 1: Tab Navigation Tests
```typescript
describe('SettingsPageV2', () => {
    it('should show permission-aware tabs', () => {
        mockUsePermissions({
            hasPermission: (perm: string) => perm === 'users:admin'
        });
        
        render(<SettingsPageV2 />);
        
        expect(screen.getByText('Benutzer')).toBeInTheDocument();
        expect(screen.getByText('Benutzerverwaltung')).toBeInTheDocument();
        expect(screen.queryByText('System')).not.toBeInTheDocument();
    });
    
    it('should warn on unsaved changes', () => {
        render(<SettingsPageV2 />);
        
        const input = screen.getByLabelText('Anzeigename');
        fireEvent.change(input, { target: { value: 'New Name' } });
        
        const mockEvent = new Event('beforeunload');
        fireEvent(window, mockEvent);
        
        expect(mockEvent.defaultPrevented).toBe(true);
    });
});
```

### Pattern 2: Auto-Save Tests
```typescript
describe('UserPreferencesTab', () => {
    it('should auto-save after debounce', async () => {
        const mockSave = jest.fn();
        jest.mocked(apiClient.patch).mockImplementation(mockSave);
        
        render(<UserPreferencesTab />);
        
        const input = screen.getByLabelText('Anzeigename');
        fireEvent.change(input, { target: { value: 'New Name' } });
        
        expect(mockSave).not.toHaveBeenCalled();
        
        await waitFor(() => {
            expect(mockSave).toHaveBeenCalledWith(
                expect.stringContaining('/preferences'),
                expect.objectContaining({ displayName: 'New Name' })
            );
        }, { timeout: 1500 });
    });
});
```

---

## 🔌 INTEGRATION COOKBOOK

### Mit Navigation (M1)
```typescript
// Add settings link to navigation
export const navigationItems = [
    // ... other items
    {
        path: '/einstellungen',
        label: 'Einstellungen',
        icon: <SettingsIcon />,
        position: 'bottom' // Show at bottom of nav
    }
];
```

### Mit Permissions (FC-009)
```typescript
// Permission-aware tab content
const SystemSettingsTab: React.FC = () => {
    const { hasPermission } = usePermissions();
    
    if (!hasPermission('system:admin')) {
        return <AccessDenied />;
    }
    
    return (
        <Grid container spacing={3}>
            <Grid item xs={12}>
                <SystemConfiguration />
            </Grid>
        </Grid>
    );
};
```

### Mit User Management
```typescript
// Integrated user management tab
export const UserManagementTab: React.FC = () => {
    const { data: users } = useUsers();
    const { openUserDialog } = useUserManagement();
    
    return (
        <Box>
            <Box sx={{ mb: 2, display: 'flex', justifyContent: 'space-between' }}>
                <Typography variant="h6">Benutzerverwaltung</Typography>
                <Button
                    variant="contained"
                    startIcon={<AddIcon />}
                    onClick={() => openUserDialog('create')}
                >
                    Benutzer hinzufügen
                </Button>
            </Box>
            
            <DataGrid
                rows={users || []}
                columns={userColumns}
                pageSize={25}
                autoHeight
            />
        </Box>
    );
};
```

---

## 🎯 ADMIN FEATURES

### Recipe 4: System Settings Tab
```typescript
// System configuration for admins (copy-paste ready)
export const SystemSettingsTab: React.FC = () => {
    const [config, setConfig] = useState<SystemConfig>();
    const { mutate: saveConfig } = useSaveSystemConfig();
    
    return (
        <Grid container spacing={3}>
            <Grid item xs={12} md={6}>
                <Card>
                    <CardHeader title="E-Mail Konfiguration" />
                    <CardContent>
                        <Stack spacing={2}>
                            <TextField
                                label="SMTP Server"
                                value={config?.smtp.host}
                                onChange={(e) => updateConfig('smtp.host', e.target.value)}
                                fullWidth
                            />
                            <TextField
                                label="SMTP Port"
                                value={config?.smtp.port}
                                onChange={(e) => updateConfig('smtp.port', e.target.value)}
                                type="number"
                                fullWidth
                            />
                            <FormControlLabel
                                control={
                                    <Switch
                                        checked={config?.smtp.secure}
                                        onChange={(e) => updateConfig('smtp.secure', e.target.checked)}
                                    />
                                }
                                label="SSL/TLS verwenden"
                            />
                        </Stack>
                    </CardContent>
                </Card>
            </Grid>
            
            <Grid item xs={12} md={6}>
                <Card>
                    <CardHeader title="Sicherheit" />
                    <CardContent>
                        <Stack spacing={2}>
                            <TextField
                                label="Session Timeout (Minuten)"
                                value={config?.security.sessionTimeout}
                                onChange={(e) => updateConfig('security.sessionTimeout', e.target.value)}
                                type="number"
                                fullWidth
                            />
                            <FormControlLabel
                                control={
                                    <Switch
                                        checked={config?.security.twoFactorRequired}
                                        onChange={(e) => updateConfig('security.twoFactorRequired', e.target.checked)}
                                    />
                                }
                                label="2FA verpflichtend"
                            />
                        </Stack>
                    </CardContent>
                </Card>
            </Grid>
        </Grid>
    );
};
```

### Recipe 5: DSGVO Compliance Tab
```typescript
// Privacy settings with GDPR compliance (copy-paste ready)
export const PrivacySettingsTab: React.FC = () => {
    const { user } = useAuth();
    const [consents, setConsents] = useState(user.privacyConsents);
    
    const handleDataExport = async () => {
        const response = await apiClient.get(`/api/users/${user.id}/export`, {
            responseType: 'blob'
        });
        
        const url = window.URL.createObjectURL(new Blob([response.data]));
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', `user-data-${user.id}.json`);
        document.body.appendChild(link);
        link.click();
        link.remove();
    };
    
    const handleAccountDeletion = async () => {
        const confirmed = await showConfirmDialog({
            title: 'Account löschen?',
            message: 'Diese Aktion kann nicht rückgängig gemacht werden!',
            confirmText: 'Endgültig löschen',
            severity: 'error'
        });
        
        if (confirmed) {
            await apiClient.delete(`/api/users/${user.id}`);
            logout();
        }
    };
    
    return (
        <Grid container spacing={3}>
            <Grid item xs={12}>
                <Card>
                    <CardHeader title="Datenschutz-Einstellungen" />
                    <CardContent>
                        <Stack spacing={3}>
                            <Alert severity="info">
                                Ihre Daten werden gemäß DSGVO verarbeitet. 
                                Sie haben jederzeit das Recht auf Auskunft, Berichtigung und Löschung.
                            </Alert>
                            
                            <Divider />
                            
                            <Typography variant="h6">Einwilligungen</Typography>
                            
                            <FormGroup>
                                <FormControlLabel
                                    control={
                                        <Checkbox
                                            checked={consents.marketing}
                                            onChange={(e) => updateConsent('marketing', e.target.checked)}
                                        />
                                    }
                                    label="Marketing-Kommunikation"
                                />
                                <FormControlLabel
                                    control={
                                        <Checkbox
                                            checked={consents.analytics}
                                            onChange={(e) => updateConsent('analytics', e.target.checked)}
                                        />
                                    }
                                    label="Anonyme Nutzungsstatistiken"
                                />
                            </FormGroup>
                            
                            <Divider />
                            
                            <Box>
                                <Button
                                    variant="outlined"
                                    startIcon={<DownloadIcon />}
                                    onClick={handleDataExport}
                                    sx={{ mr: 2 }}
                                >
                                    Daten exportieren
                                </Button>
                                
                                <Button
                                    variant="outlined"
                                    color="error"
                                    startIcon={<DeleteIcon />}
                                    onClick={handleAccountDeletion}
                                >
                                    Account löschen
                                </Button>
                            </Box>
                        </Stack>
                    </CardContent>
                </Card>
            </Grid>
        </Grid>
    );
};
```

---

## 📚 DEEP KNOWLEDGE (Bei Bedarf expandieren)

<details>
<summary>🔐 Advanced Security Settings</summary>

### Two-Factor Authentication Setup
```typescript
export const TwoFactorSetup: React.FC = () => {
    const [qrCode, setQrCode] = useState<string>();
    const [verificationCode, setVerificationCode] = useState('');
    
    const initiate2FA = async () => {
        const response = await apiClient.post('/api/auth/2fa/initiate');
        setQrCode(response.data.qrCode);
    };
    
    const verify2FA = async () => {
        try {
            await apiClient.post('/api/auth/2fa/verify', { code: verificationCode });
            toast.success('2FA aktiviert');
        } catch (error) {
            toast.error('Ungültiger Code');
        }
    };
    
    return (
        <Box>
            {!qrCode ? (
                <Button onClick={initiate2FA}>2FA aktivieren</Button>
            ) : (
                <Box>
                    <img src={qrCode} alt="2FA QR Code" />
                    <TextField
                        label="Verification Code"
                        value={verificationCode}
                        onChange={(e) => setVerificationCode(e.target.value)}
                    />
                    <Button onClick={verify2FA}>Verifizieren</Button>
                </Box>
            )}
        </Box>
    );
};
```

</details>

<details>
<summary>📊 Usage Analytics</summary>

### Track Settings Usage
```typescript
const trackSettingsEvent = (event: string, data?: any) => {
    if (window.gtag) {
        window.gtag('event', event, {
            event_category: 'Settings',
            event_label: data?.tab || 'unknown',
            value: data?.value
        });
    }
};

// Usage
trackSettingsEvent('settings_tab_viewed', { tab: 'privacy' });
trackSettingsEvent('preference_changed', { preference: 'theme', value: 'dark' });
```

</details>

---

**🎯 Nächster Schritt:** SettingsPageV2 mit Tab-Navigation implementieren und Auto-Save hinzufügen