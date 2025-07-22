# M7 Settings Tech Concept - Advanced Settings Management

**Feature-Code:** M7  
**Feature-Typ:** 🎨 FRONTEND Enhancement  
**Priorität:** HIGH  
**Aufwand:** 2-3 Tage  
**Status:** 📋 TECH CONCEPT  

---

## 🎯 ÜBERBLICK

### Geschäftlicher Kontext
Das Settings-Modul bildet das zentrale Kontrollzentrum für alle Benutzer- und Systemeinstellungen. Es baut auf der bereits existierenden `SettingsPage.tsx` (50% fertig) auf und erweitert diese um erweiterte Verwaltungsfunktionen, granulare Berechtigungen und eine intuitive Benutzeroberfläche.

### Technische Vision
Entwicklung eines modularen, tab-basierten Settings-Systems mit Material-UI, das sowohl Endbenutzer-Präferenzen als auch administrative Funktionen elegant vereint. Das System integriert nahtlos mit FC-009 (Advanced Permissions) und bietet eine erweiterbare Architektur für zukünftige Einstellungskategorien.

### Business Value
- **Zentrale Verwaltung:** Ein-Klick-Zugriff auf alle wichtigen Einstellungen
- **Benutzerfreundlichkeit:** Intuitive Tab-Navigation mit sofortiger Speicherung
- **Skalierbarkeit:** Modulare Struktur für einfache Erweiterungen
- **Compliance:** DSGVO-konforme Datenschutzeinstellungen

---

## 🏗️ ARCHITEKTUR

### Frontend-Architektur (React + Material-UI)
```typescript
// Hauptkomponente - Enhancement der bestehenden SettingsPage.tsx
export const SettingsPageV2: React.FC = () => {
    const [activeTab, setActiveTab] = useState(0);
    const { user } = useAuth();
    const { permissions } = usePermissions();
    
    return (
        <Container maxWidth="xl" sx={{ py: 3 }}>
            <Typography variant="h4" gutterBottom>
                Einstellungen
            </Typography>
            
            <Paper sx={{ width: '100%' }}>
                <Tabs value={activeTab} onChange={(e, v) => setActiveTab(v)}>
                    <Tab label="Benutzer" />
                    <Tab label="Benachrichtigungen" />
                    <Tab label="Datenschutz" />
                    {permissions.hasSystemAdmin && <Tab label="System" />}
                    {permissions.hasUserAdmin && <Tab label="Benutzerverwaltung" />}
                </Tabs>
                
                <TabPanel value={activeTab} index={0}>
                    <UserPreferencesTab />
                </TabPanel>
                <TabPanel value={activeTab} index={1}>
                    <NotificationSettingsTab />
                </TabPanel>
                <TabPanel value={activeTab} index={2}>
                    <PrivacySettingsTab />
                </TabPanel>
                {permissions.hasSystemAdmin && (
                    <TabPanel value={activeTab} index={3}>
                        <SystemSettingsTab />
                    </TabPanel>
                )}
                {permissions.hasUserAdmin && (
                    <TabPanel value={activeTab} index={4}>
                        <UserManagementTab />
                    </TabPanel>
                )}
            </Paper>
        </Container>
    );
};
```

### Tab-Module Architektur
```typescript
// 1. Benutzer-Präferenzen Tab
export const UserPreferencesTab: React.FC = () => {
    const { user, updateUserPreferences } = useAuth();
    const [preferences, setPreferences] = useState(user.preferences);
    
    return (
        <Grid container spacing={3}>
            <Grid item xs={12} md={6}>
                <Card>
                    <CardHeader title="Persönliche Daten" />
                    <CardContent>
                        <TextField
                            label="Anzeigename"
                            value={preferences.displayName}
                            onChange={(e) => updatePreference('displayName', e.target.value)}
                            fullWidth
                        />
                        <TextField
                            label="E-Mail"
                            value={preferences.email}
                            onChange={(e) => updatePreference('email', e.target.value)}
                            fullWidth
                            sx={{ mt: 2 }}
                        />
                        <FormControl fullWidth sx={{ mt: 2 }}>
                            <InputLabel>Sprache</InputLabel>
                            <Select
                                value={preferences.language}
                                onChange={(e) => updatePreference('language', e.target.value)}
                            >
                                <MenuItem value="de">Deutsch</MenuItem>
                                <MenuItem value="en">English</MenuItem>
                            </Select>
                        </FormControl>
                    </CardContent>
                </Card>
            </Grid>
            
            <Grid item xs={12} md={6}>
                <Card>
                    <CardHeader title="Interface-Einstellungen" />
                    <CardContent>
                        <FormControlLabel
                            control={
                                <Switch
                                    checked={preferences.darkMode}
                                    onChange={(e) => updatePreference('darkMode', e.target.checked)}
                                />
                            }
                            label="Dark Mode"
                        />
                        <FormControlLabel
                            control={
                                <Switch
                                    checked={preferences.compactMode}
                                    onChange={(e) => updatePreference('compactMode', e.target.checked)}
                                />
                            }
                            label="Kompakte Ansicht"
                        />
                        <FormControl fullWidth sx={{ mt: 2 }}>
                            <InputLabel>Dashboard-Layout</InputLabel>
                            <Select
                                value={preferences.dashboardLayout}
                                onChange={(e) => updatePreference('dashboardLayout', e.target.value)}
                            >
                                <MenuItem value="classic">Klassisch</MenuItem>
                                <MenuItem value="modern">Modern</MenuItem>
                                <MenuItem value="compact">Kompakt</MenuItem>
                            </Select>
                        </FormControl>
                    </CardContent>
                </Card>
            </Grid>
        </Grid>
    );
};

// 2. Benachrichtigungen Tab
export const NotificationSettingsTab: React.FC = () => {
    const { user, updateNotificationSettings } = useAuth();
    const [settings, setSettings] = useState(user.notificationSettings);
    
    return (
        <Card>
            <CardHeader title="Benachrichtigungseinstellungen" />
            <CardContent>
                <Typography variant="h6" gutterBottom>
                    E-Mail-Benachrichtigungen
                </Typography>
                <FormControlLabel
                    control={
                        <Switch
                            checked={settings.emailNotifications}
                            onChange={(e) => updateSetting('emailNotifications', e.target.checked)}
                        />
                    }
                    label="E-Mail-Benachrichtigungen aktivieren"
                />
                
                <Typography variant="h6" gutterBottom sx={{ mt: 3 }}>
                    Push-Benachrichtigungen
                </Typography>
                <FormControlLabel
                    control={
                        <Switch
                            checked={settings.pushNotifications}
                            onChange={(e) => updateSetting('pushNotifications', e.target.checked)}
                        />
                    }
                    label="Browser-Benachrichtigungen"
                />
                
                <Typography variant="h6" gutterBottom sx={{ mt: 3 }}>
                    Benachrichtigungsfrequenz
                </Typography>
                <FormControl component="fieldset">
                    <RadioGroup
                        value={settings.frequency}
                        onChange={(e) => updateSetting('frequency', e.target.value)}
                    >
                        <FormControlLabel value="instant" control={<Radio />} label="Sofort" />
                        <FormControlLabel value="daily" control={<Radio />} label="Täglich" />
                        <FormControlLabel value="weekly" control={<Radio />} label="Wöchentlich" />
                    </RadioGroup>
                </FormControl>
            </CardContent>
        </Card>
    );
};

// 3. Datenschutz Tab
export const PrivacySettingsTab: React.FC = () => {
    const { user, updatePrivacySettings } = useAuth();
    const [settings, setSettings] = useState(user.privacySettings);
    
    return (
        <Card>
            <CardHeader title="Datenschutz & Sicherheit" />
            <CardContent>
                <Typography variant="h6" gutterBottom>
                    Datenverarbeitung
                </Typography>
                <FormControlLabel
                    control={
                        <Switch
                            checked={settings.analyticsConsent}
                            onChange={(e) => updateSetting('analyticsConsent', e.target.checked)}
                        />
                    }
                    label="Analytics & Nutzungsstatistiken"
                />
                
                <Typography variant="h6" gutterBottom sx={{ mt: 3 }}>
                    Datenexport
                </Typography>
                <Button
                    variant="outlined"
                    startIcon={<DownloadIcon />}
                    onClick={() => exportUserData()}
                >
                    Meine Daten exportieren
                </Button>
                
                <Typography variant="h6" gutterBottom sx={{ mt: 3 }}>
                    Konto-Aktionen
                </Typography>
                <Button
                    variant="outlined"
                    color="error"
                    startIcon={<DeleteIcon />}
                    onClick={() => setDeleteDialogOpen(true)}
                >
                    Konto löschen
                </Button>
            </CardContent>
        </Card>
    );
};
```

### Berechtigungsbasierte Tabs
```typescript
// 4. System-Settings Tab (nur für System-Admins)
export const SystemSettingsTab: React.FC = () => {
    const { hasPermission } = usePermissions();
    
    if (!hasPermission('system:admin')) {
        return <AccessDenied />;
    }
    
    return (
        <Grid container spacing={3}>
            <Grid item xs={12} md={6}>
                <Card>
                    <CardHeader title="Systemkonfiguration" />
                    <CardContent>
                        <TextField
                            label="System-Name"
                            fullWidth
                            margin="normal"
                        />
                        <TextField
                            label="Support-E-Mail"
                            fullWidth
                            margin="normal"
                        />
                        <FormControlLabel
                            control={<Switch />}
                            label="Wartungsmodus"
                        />
                    </CardContent>
                </Card>
            </Grid>
            
            <Grid item xs={12} md={6}>
                <Card>
                    <CardHeader title="Sicherheitseinstellungen" />
                    <CardContent>
                        <TextField
                            label="Session-Timeout (Minuten)"
                            type="number"
                            fullWidth
                            margin="normal"
                        />
                        <FormControlLabel
                            control={<Switch />}
                            label="Zwei-Faktor-Authentifizierung erzwingen"
                        />
                        <FormControlLabel
                            control={<Switch />}
                            label="Passwort-Komplexität"
                        />
                    </CardContent>
                </Card>
            </Grid>
        </Grid>
    );
};

// 5. Benutzerverwaltung Tab (nur für User-Admins)
export const UserManagementTab: React.FC = () => {
    const { hasPermission } = usePermissions();
    const [users, setUsers] = useState([]);
    
    if (!hasPermission('users:admin')) {
        return <AccessDenied />;
    }
    
    return (
        <Card>
            <CardHeader 
                title="Benutzerverwaltung" 
                action={
                    <Button variant="contained" startIcon={<AddIcon />}>
                        Neuer Benutzer
                    </Button>
                }
            />
            <CardContent>
                <DataGrid
                    rows={users}
                    columns={[
                        { field: 'username', headerName: 'Benutzername', width: 200 },
                        { field: 'email', headerName: 'E-Mail', width: 250 },
                        { field: 'role', headerName: 'Rolle', width: 150 },
                        { field: 'lastLogin', headerName: 'Letzter Login', width: 200 },
                        {
                            field: 'actions',
                            headerName: 'Aktionen',
                            width: 200,
                            renderCell: (params) => (
                                <Box>
                                    <IconButton onClick={() => editUser(params.row.id)}>
                                        <EditIcon />
                                    </IconButton>
                                    <IconButton onClick={() => deleteUser(params.row.id)}>
                                        <DeleteIcon />
                                    </IconButton>
                                </Box>
                            )
                        }
                    ]}
                    pageSize={10}
                    autoHeight
                />
            </CardContent>
        </Card>
    );
};
```

---

## 🔗 ABHÄNGIGKEITEN

### Direkte Dependencies
- **FC-009 Advanced Permissions:** Permission-aware UI und Berechtigungsprüfungen
- **FC-008 Security Foundation:** JWT-basierte Authentifizierung für Settings-API
- **M1 Navigation System:** Integration in Haupt-Navigation

### API Dependencies
```typescript
// Settings API Service
export class SettingsApiService {
    async getUserPreferences(userId: string): Promise<UserPreferences> {
        return await apiClient.get(`/api/users/${userId}/preferences`);
    }
    
    async updateUserPreferences(userId: string, preferences: UserPreferences): Promise<void> {
        await apiClient.put(`/api/users/${userId}/preferences`, preferences);
    }
    
    async getNotificationSettings(userId: string): Promise<NotificationSettings> {
        return await apiClient.get(`/api/users/${userId}/notifications`);
    }
    
    async updateNotificationSettings(userId: string, settings: NotificationSettings): Promise<void> {
        await apiClient.put(`/api/users/${userId}/notifications`, settings);
    }
    
    async exportUserData(userId: string): Promise<Blob> {
        return await apiClient.get(`/api/users/${userId}/export`, { responseType: 'blob' });
    }
}
```

### Backend Requirements (Quarkus)
```java
@Path("/api/users/{userId}/preferences")
@ApplicationScoped
public class UserPreferencesResource {
    
    @Inject
    UserPreferencesService preferencesService;
    
    @GET
    @RolesAllowed({"admin", "manager", "sales"})
    public UserPreferencesResponse getPreferences(@PathParam("userId") UUID userId) {
        return preferencesService.getUserPreferences(userId);
    }
    
    @PUT
    @RolesAllowed({"admin", "manager", "sales"})
    public Response updatePreferences(@PathParam("userId") UUID userId, 
                                    UpdatePreferencesRequest request) {
        preferencesService.updateUserPreferences(userId, request);
        return Response.ok().build();
    }
}
```

---

## 🧪 TESTING-STRATEGIE

### Unit Tests (Jest + React Testing Library)
```typescript
describe('SettingsPageV2', () => {
    it('should render all tabs for admin user', () => {
        const mockUser = { role: 'admin' };
        render(<SettingsPageV2 />, { 
            wrapper: createAuthWrapper(mockUser) 
        });
        
        expect(screen.getByText('Benutzer')).toBeInTheDocument();
        expect(screen.getByText('Benachrichtigungen')).toBeInTheDocument();
        expect(screen.getByText('Datenschutz')).toBeInTheDocument();
        expect(screen.getByText('System')).toBeInTheDocument();
        expect(screen.getByText('Benutzerverwaltung')).toBeInTheDocument();
    });
    
    it('should hide admin tabs for regular user', () => {
        const mockUser = { role: 'sales' };
        render(<SettingsPageV2 />, { 
            wrapper: createAuthWrapper(mockUser) 
        });
        
        expect(screen.queryByText('System')).not.toBeInTheDocument();
        expect(screen.queryByText('Benutzerverwaltung')).not.toBeInTheDocument();
    });
});

describe('UserPreferencesTab', () => {
    it('should save preferences on change', async () => {
        const mockUpdatePreferences = jest.fn();
        render(<UserPreferencesTab />, {
            wrapper: createWrapper({ updateUserPreferences: mockUpdatePreferences })
        });
        
        const darkModeSwitch = screen.getByLabelText('Dark Mode');
        fireEvent.click(darkModeSwitch);
        
        await waitFor(() => {
            expect(mockUpdatePreferences).toHaveBeenCalledWith(
                expect.objectContaining({ darkMode: true })
            );
        });
    });
});
```

### Integration Tests
```typescript
describe('Settings Integration', () => {
    it('should persist settings across page refresh', async () => {
        // 1. Login and navigate to settings
        await login('testuser', 'password');
        await navigate('/einstellungen');
        
        // 2. Change dark mode setting
        const darkModeSwitch = screen.getByLabelText('Dark Mode');
        fireEvent.click(darkModeSwitch);
        
        // 3. Refresh page
        window.location.reload();
        
        // 4. Verify setting persisted
        await waitFor(() => {
            expect(screen.getByLabelText('Dark Mode')).toBeChecked();
        });
    });
});
```

### E2E Tests (Playwright)
```typescript
test('Settings workflow for admin user', async ({ page }) => {
    await page.goto('/login');
    await page.fill('[data-testid="username"]', 'admin@freshplan.de');
    await page.fill('[data-testid="password"]', 'admin123');
    await page.click('[data-testid="login-button"]');
    
    await page.goto('/einstellungen');
    
    // Test tab navigation
    await page.click('text=Benutzerverwaltung');
    await expect(page.locator('[data-testid="user-grid"]')).toBeVisible();
    
    // Test user creation
    await page.click('[data-testid="add-user-button"]');
    await page.fill('[data-testid="username-input"]', 'newuser');
    await page.fill('[data-testid="email-input"]', 'newuser@freshplan.de');
    await page.click('[data-testid="save-user-button"]');
    
    await expect(page.locator('text=Benutzer erfolgreich erstellt')).toBeVisible();
});
```

---

## 📋 IMPLEMENTIERUNGSPLAN

### 🕒 15-Minuten Claude Working Section

**Aufgabe:** Enhancement der bestehenden SettingsPage.tsx um erweiterte Tab-Funktionalität

**Sofort loslegen:**
1. Bestehende SettingsPage.tsx analysieren (aktuell 50% fertig)
2. Material-UI Tabs-Struktur implementieren
3. UserPreferencesTab als erste Tab umsetzen
4. Speicher-Logik mit useAuth Hook verbinden

**Quick-Win Schritte:**
```typescript
// 1. Basis-Struktur erweitern
// frontend/src/features/settings/SettingsPage.tsx

// 2. Tab-Navigation hinzufügen
const [activeTab, setActiveTab] = useState(0);

// 3. Erste Tab implementieren
<TabPanel value={activeTab} index={0}>
    <UserPreferencesTab />
</TabPanel>

// 4. Auto-Save implementieren
const debouncedSave = useCallback(
    debounce((preferences) => {
        updateUserPreferences(user.id, preferences);
    }, 500),
    [user.id]
);
```

### Phase 1: Basis-Enhancement (Tag 1 - 4h)
- ✅ Bestehende SettingsPage.tsx erweitern
- ✅ Material-UI Tabs implementieren  
- ✅ UserPreferencesTab vollständig umsetzen
- ✅ Auto-Save-Funktionalität
- ✅ Responsive Design

### Phase 2: Erweiterte Tabs (Tag 2 - 4h)
- ✅ NotificationSettingsTab implementieren
- ✅ PrivacySettingsTab mit DSGVO-Compliance
- ✅ Permission-aware Tab-Anzeige
- ✅ Validierung und Error Handling

### Phase 3: Admin-Funktionen (Tag 3 - 3h)
- ✅ SystemSettingsTab für System-Admins
- ✅ UserManagementTab mit DataGrid
- ✅ Berechtigungsprüfungen
- ✅ Admin-spezifische Aktionen

### Erfolgs-Kriterien
- ✅ Alle 5 Tabs funktional und benutzerfreundlich
- ✅ Permission-aware UI mit korrekter Berechtigung-Anzeige
- ✅ Auto-Save ohne Benutzerinteraktion
- ✅ DSGVO-konforme Datenschutzeinstellungen
- ✅ Responsive Design für alle Bildschirmgrößen
- ✅ Unit-Test-Coverage ≥ 80%

---

## 🎯 QUALITÄTSSTANDARDS

### Code-Qualität
- **TypeScript:** Strikte Typisierung für alle Props und State
- **Performance:** Memoization für schwere Komponenten
- **Accessibility:** WCAG 2.1 AA Compliance
- **Error Handling:** Graceful Degradation bei API-Fehlern

### UX-Standards
- **Auto-Save:** Sofortige Speicherung bei Änderungen
- **Loading States:** Skeleton-Loading für alle Datenoperationen
- **Feedback:** Toast-Nachrichten für alle Aktionen
- **Validation:** Echtzeit-Validierung mit hilfreichen Fehlermeldungen

### Security
- **Permission Checks:** Alle admin-spezifischen Funktionen abgesichert
- **Input Validation:** Frontend und Backend Validierung
- **Data Export:** Sichere Datenexport-Funktionalität
- **Account Deletion:** Sicherheitsabfragen vor kritischen Aktionen

---

## 🔗 ABSOLUTE NAVIGATION ZU ALLEN 40 FEATURES

### 🟢 ACTIVE Features (In Entwicklung - 9 Features)
| Code | Feature | Dokument |
|------|---------|----------|
| FC-008 | Security Foundation | [TECH_CONCEPT](/docs/features/ACTIVE/01_security_foundation/FC-008_TECH_CONCEPT.md) |
| M4 | Opportunity Pipeline | [KOMPAKT](/docs/features/ACTIVE/02_opportunity_pipeline/M4_TECH_CONCEPT.md) |
| M8 | Calculator Modal | [KOMPAKT](/docs/features/ACTIVE/03_calculator_modal/M8_TECH_CONCEPT.md) |
| FC-009 | Advanced Permissions | [TECH_CONCEPT](/docs/features/ACTIVE/04_permissions_system/FC-009_TECH_CONCEPT.md) |
| M1 | Navigation System | [TECH_CONCEPT](/docs/features/ACTIVE/05_ui_foundation/M1_TECH_CONCEPT.md) |
| M2 | Quick Create Actions | [TECH_CONCEPT](/docs/features/ACTIVE/05_ui_foundation/M2_TECH_CONCEPT.md) |
| M3 | Sales Cockpit Enhancement | [TECH_CONCEPT](/docs/features/ACTIVE/05_ui_foundation/M3_TECH_CONCEPT.md) |
| **M7** | **Settings Enhancement** | **[TECH_CONCEPT](/docs/features/ACTIVE/05_ui_foundation/M7_TECH_CONCEPT.md)** ⭐ |

### 🔵 PLANNED Features (Geplant - 31 Features)

#### Customer & Sales Features (FC-001 bis FC-007)
| Code | Feature | Dokument |
|------|---------|----------|
| FC-001 | Customer Acquisition Engine | [TECH_CONCEPT](/docs/features/PLANNED/01_customer_acquisition/FC-001_TECH_CONCEPT.md) |
| FC-002 | Smart Customer Insights | [TECH_CONCEPT](/docs/features/PLANNED/02_smart_insights/FC-002_TECH_CONCEPT.md) |
| FC-003 | E-Mail Integration | [KOMPAKT](/docs/features/PLANNED/06_email_integration/FC-003_TECH_CONCEPT.md) |
| FC-004 | Verkäuferschutz | [KOMPAKT](/docs/features/PLANNED/07_verkaeuferschutz/FC-004_TECH_CONCEPT.md) |
| FC-005 | Xentral Integration | [KOMPAKT](/docs/features/PLANNED/08_xentral_integration/FC-005_TECH_CONCEPT.md) |
| FC-006 | Mobile App | [KOMPAKT](/docs/features/PLANNED/09_mobile_app/FC-006_TECH_CONCEPT.md) |
| FC-007 | Chef-Dashboard | [KOMPAKT](/docs/features/PLANNED/10_chef_dashboard/FC-007_TECH_CONCEPT.md) |

#### Core Infrastructure Features (FC-008 bis FC-021)
| Code | Feature | Dokument |
|------|---------|----------|
| FC-010 | Customer Import | [KOMPAKT](/docs/features/PLANNED/11_customer_import/FC-010_TECH_CONCEPT.md) |
| FC-011 | Bonitätsprüfung | [KOMPAKT](/docs/features/ACTIVE/02_opportunity_pipeline/integrations/FC-011_TECH_CONCEPT.md) |
| FC-012 | Team Communication | [KOMPAKT](/docs/features/PLANNED/14_team_communication/FC-012_TECH_CONCEPT.md) |
| FC-013 | Duplicate Detection | [KOMPAKT](/docs/features/PLANNED/15_duplicate_detection/FC-013_TECH_CONCEPT.md) |
| FC-014 | Activity Timeline | [KOMPAKT](/docs/features/PLANNED/16_activity_timeline/FC-014_TECH_CONCEPT.md) |
| FC-015 | Deal Loss Analysis | [KOMPAKT](/docs/features/PLANNED/17_deal_loss_analysis/FC-015_TECH_CONCEPT.md) |
| FC-016 | Opportunity Cloning | [KOMPAKT](/docs/features/PLANNED/18_opportunity_cloning/FC-016_TECH_CONCEPT.md) |
| FC-017 | Sales Gamification | [KOMPAKT](/docs/features/PLANNED/99_sales_gamification/FC-017_TECH_CONCEPT.md) |
| FC-018 | Mobile PWA | [KOMPAKT](/docs/features/PLANNED/09_mobile_app/FC-018_MOBILE_FIELD_SALES.md) |
| FC-019 | Advanced Sales Metrics | [KOMPAKT](/docs/features/PLANNED/19_advanced_metrics/FC-019_TECH_CONCEPT.md) |
| FC-020 | Quick Wins | [KOMPAKT](/docs/features/PLANNED/20_quick_wins/FC-020_TECH_CONCEPT.md) |
| FC-021 | Integration Hub | [KOMPAKT](/docs/features/PLANNED/21_integration_hub/FC-021_TECH_CONCEPT.md) |

#### Modern Platform Features (FC-022 bis FC-036)
| Code | Feature | Dokument |
|------|---------|----------|
| FC-022 | Mobile Light | [KOMPAKT](/docs/features/PLANNED/22_mobile_light/FC-022_TECH_CONCEPT.md) |
| FC-023 | Event Sourcing | [KOMPAKT](/docs/features/PLANNED/23_event_sourcing/FC-023_TECH_CONCEPT.md) |
| FC-024 | File Management | [KOMPAKT](/docs/features/PLANNED/24_file_management/FC-024_TECH_CONCEPT.md) |
| FC-025 | DSGVO Compliance | [KOMPAKT](/docs/features/PLANNED/25_dsgvo_compliance/FC-025_TECH_CONCEPT.md) |
| FC-026 | Analytics Platform | [KOMPAKT](/docs/features/PLANNED/26_analytics_platform/FC-026_TECH_CONCEPT.md) |
| FC-027 | Magic Moments | [KOMPAKT](/docs/features/PLANNED/27_magic_moments/FC-027_TECH_CONCEPT.md) |
| FC-028 | WhatsApp Business | [KOMPAKT](/docs/features/PLANNED/28_whatsapp_integration/FC-028_TECH_CONCEPT.md) |
| FC-029 | Voice-First Interface | [KOMPAKT](/docs/features/PLANNED/29_voice_first/FC-029_TECH_CONCEPT.md) |
| FC-030 | One-Tap Actions | [KOMPAKT](/docs/features/PLANNED/30_one_tap_actions/FC-030_TECH_CONCEPT.md) |
| FC-031 | Smart Templates | [KOMPAKT](/docs/features/PLANNED/31_smart_templates/FC-031_TECH_CONCEPT.md) |
| FC-032 | Offline-First | [KOMPAKT](/docs/features/PLANNED/32_offline_first/FC-032_TECH_CONCEPT.md) |
| FC-033 | Visual Customer Cards | [TECH_CONCEPT](/docs/features/PLANNED/33_visual_cards/FC-033_TECH_CONCEPT.md) |
| FC-034 | Instant Insights | [TECH_CONCEPT](/docs/features/PLANNED/34_instant_insights/FC-034_TECH_CONCEPT.md) |
| FC-035 | Social Selling Helper | [TECH_CONCEPT](/docs/features/PLANNED/35_social_selling/FC-035_TECH_CONCEPT.md) |
| FC-036 | Beziehungsmanagement | [TECH_CONCEPT](/docs/features/PLANNED/36_relationship_mgmt/FC-036_TECH_CONCEPT.md) |

#### Enterprise Platform Features (FC-037 bis FC-040)
| Code | Feature | Dokument |
|------|---------|----------|
| FC-037 | Advanced Reporting | [TECH_CONCEPT](/docs/features/PLANNED/37_advanced_reporting/FC-037_TECH_CONCEPT.md) |
| FC-038 | Multi-Tenant Architecture | [TECH_CONCEPT](/docs/features/PLANNED/38_multitenant/FC-038_TECH_CONCEPT.md) |
| FC-039 | API Gateway | [TECH_CONCEPT](/docs/features/PLANNED/39_api_gateway/FC-039_TECH_CONCEPT.md) |
| FC-040 | Performance Monitoring | [TECH_CONCEPT](/docs/features/PLANNED/40_performance_monitoring/FC-040_TECH_CONCEPT.md) |

#### Module Features (M1 bis M6)
| Code | Feature | Dokument |
|------|---------|----------|
| M5 | Customer Refactor | [TECH_CONCEPT](/docs/features/PLANNED/12_customer_refactor_m5/M5_TECH_CONCEPT.md) |
| M6 | Analytics Module | [TECH_CONCEPT](/docs/features/PLANNED/13_analytics_m6/M6_TECH_CONCEPT.md) |

#### Future Features
| Code | Feature | Status |
|------|---------|--------|
| FC-041 | Future Feature Slot | Noch zu definieren |

### 📊 Tech Concept Coverage
- **Abgeschlossen:** 39 von 42 Features (92.9%)
- **Verbleibend:** M8, FC-041 + 1 Future Feature  
- **Session 16 Status:** 3 von 12 Features verbleibend für 100% Coverage

---

**🎯 Nächster Schritt:** M8 Calculator Modal Tech Concept erstellen