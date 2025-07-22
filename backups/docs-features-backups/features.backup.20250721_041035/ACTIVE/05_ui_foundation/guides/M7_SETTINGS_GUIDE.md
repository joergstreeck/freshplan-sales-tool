# ⚙️ M7 SETTINGS IMPLEMENTATION GUIDE

**Fokus:** Nur Settings Tab-Inhalte  
**Zeilen:** <200 für optimale Claude-Arbeitsweise  
**Prerequisite:** [M7_SETTINGS_KOMPAKT.md](../M7_SETTINGS_KOMPAKT.md) gelesen  
**Zurück zur Übersicht:** [IMPLEMENTATION_GUIDE.md](../IMPLEMENTATION_GUIDE.md)  

---

## 🚀 PHASE 1: SYSTEM SETTINGS TAB (Tag 1)

### 1.1: System Settings Component

```typescript
// components/settings/SystemSettingsTab.tsx (NEU)
import { useState } from 'react';
import { Box, Typography, Switch, FormControlLabel, TextField, Button, Paper } from '@mui/material';

interface SystemSettings {
  autoSaveInterval: number;
  defaultCurrency: string;
  dateFormat: string;
  language: string;
  darkMode: boolean;
  notifications: boolean;
}

export const SystemSettingsTab: React.FC = () => {
  const [settings, setSettings] = useState<SystemSettings>({
    autoSaveInterval: 5,
    defaultCurrency: 'EUR',
    dateFormat: 'DD.MM.YYYY',
    language: 'de',
    darkMode: false,
    notifications: true
  });

  const handleSave = async () => {
    try {
      await systemSettingsService.updateSettings(settings);
      showSuccessNotification('Einstellungen gespeichert');
    } catch (error) {
      showErrorNotification('Fehler beim Speichern');
    }
  };

  return (
    <Box sx={{ maxWidth: 600 }}>
      <Typography variant="h6" sx={{ mb: 3 }}>
        System-Einstellungen
      </Typography>
      
      <Paper sx={{ p: 3, mb: 2 }}>
        <Typography variant="subtitle1" sx={{ mb: 2 }}>
          Allgemeine Einstellungen
        </Typography>
        
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          <TextField
            label="Auto-Speichern (Minuten)"
            type="number"
            value={settings.autoSaveInterval}
            onChange={(e) => setSettings({...settings, autoSaveInterval: Number(e.target.value)})}
          />
          
          <TextField
            label="Standard-Währung"
            value={settings.defaultCurrency}
            onChange={(e) => setSettings({...settings, defaultCurrency: e.target.value})}
          />
          
          <FormControlLabel
            control={
              <Switch
                checked={settings.darkMode}
                onChange={(e) => setSettings({...settings, darkMode: e.target.checked})}
              />
            }
            label="Dark Mode"
          />
          
          <FormControlLabel
            control={
              <Switch
                checked={settings.notifications}
                onChange={(e) => setSettings({...settings, notifications: e.target.checked})}
              />
            }
            label="Benachrichtigungen"
          />
        </Box>
      </Paper>
      
      <Button variant="contained" onClick={handleSave}>
        Speichern
      </Button>
    </Box>
  );
};
```

### 1.2: SettingsPage.tsx erweitern

```typescript
// pages/SettingsPage.tsx (BESTEHEND ERWEITERN!)
import { SystemSettingsTab } from '../components/settings/SystemSettingsTab';

// In der TabPanel für value={1}:
{value === 1 && (
  <Box sx={{ py: 3 }}>
    <SystemSettingsTab />
  </Box>
)}
```

---

## 🔐 PHASE 2: SECURITY SETTINGS TAB (Tag 2)

### 2.1: Security Settings Component

```typescript
// components/settings/SecuritySettingsTab.tsx (NEU)
import { usePermissions } from '../../permissions/permissionQueries';
import { Paper, List, ListItem, ListItemText, Typography, Switch, TextField, FormControlLabel } from '@mui/material';

export const SecuritySettingsTab: React.FC = () => {
  const { data: permissions } = usePermissions();
  const [twoFactorEnabled, setTwoFactorEnabled] = useState(false);
  const [sessionTimeout, setSessionTimeout] = useState(30);
  
  return (
    <Box sx={{ maxWidth: 600 }}>
      <Typography variant="h6" sx={{ mb: 3 }}>
        Sicherheits-Einstellungen
      </Typography>
      
      {/* 2FA Settings */}
      <Paper sx={{ p: 3, mb: 2 }}>
        <Typography variant="subtitle1" sx={{ mb: 2 }}>
          Zwei-Faktor-Authentifizierung
        </Typography>
        <FormControlLabel
          control={
            <Switch
              checked={twoFactorEnabled}
              onChange={(e) => setTwoFactorEnabled(e.target.checked)}
            />
          }
          label="2FA aktivieren"
        />
      </Paper>
      
      {/* Session Settings */}
      <Paper sx={{ p: 3, mb: 2 }}>
        <Typography variant="subtitle1" sx={{ mb: 2 }}>
          Session-Einstellungen
        </Typography>
        <TextField
          label="Session-Timeout (Minuten)"
          type="number"
          value={sessionTimeout}
          onChange={(e) => setSessionTimeout(Number(e.target.value))}
          fullWidth
        />
      </Paper>
      
      {/* Permissions Overview */}
      <Paper sx={{ p: 3 }}>
        <Typography variant="subtitle1" sx={{ mb: 2 }}>
          Meine Berechtigungen
        </Typography>
        <List>
          {permissions?.map(permission => (
            <ListItem key={permission.id}>
              <ListItemText 
                primary={permission.name}
                secondary={permission.description}
              />
            </ListItem>
          ))}
        </List>
      </Paper>
    </Box>
  );
};
```

---

## 🔔 PHASE 3: NOTIFICATION SETTINGS TAB (Tag 3)

### 3.1: Notification Settings Component

```typescript
// components/settings/NotificationSettingsTab.tsx (NEU)
import { useState } from 'react';
import { Box, Typography, Paper, FormControlLabel, Switch, TextField, Select, MenuItem, FormControl, InputLabel } from '@mui/material';

interface NotificationSettings {
  emailNotifications: boolean;
  pushNotifications: boolean;
  inAppNotifications: boolean;
  dailyDigest: boolean;
  weeklyReport: boolean;
  newCustomerAlert: boolean;
  dealUpdates: boolean;
  systemMaintenance: boolean;
}

export const NotificationSettingsTab: React.FC = () => {
  const [settings, setSettings] = useState<NotificationSettings>({
    emailNotifications: true,
    pushNotifications: true,
    inAppNotifications: true,
    dailyDigest: true,
    weeklyReport: false,
    newCustomerAlert: true,
    dealUpdates: true,
    systemMaintenance: true
  });

  const handleToggle = (key: keyof NotificationSettings) => {
    setSettings(prev => ({
      ...prev,
      [key]: !prev[key]
    }));
  };

  return (
    <Box sx={{ maxWidth: 600 }}>
      <Typography variant="h6" sx={{ mb: 3 }}>
        Benachrichtigungs-Einstellungen
      </Typography>
      
      {/* General Notifications */}
      <Paper sx={{ p: 3, mb: 2 }}>
        <Typography variant="subtitle1" sx={{ mb: 2 }}>
          Allgemeine Benachrichtigungen
        </Typography>
        
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
          <FormControlLabel
            control={
              <Switch
                checked={settings.emailNotifications}
                onChange={() => handleToggle('emailNotifications')}
              />
            }
            label="E-Mail Benachrichtigungen"
          />
          
          <FormControlLabel
            control={
              <Switch
                checked={settings.pushNotifications}
                onChange={() => handleToggle('pushNotifications')}
              />
            }
            label="Push Benachrichtigungen"
          />
          
          <FormControlLabel
            control={
              <Switch
                checked={settings.inAppNotifications}
                onChange={() => handleToggle('inAppNotifications')}
              />
            }
            label="In-App Benachrichtigungen"
          />
        </Box>
      </Paper>
      
      {/* Sales Notifications */}
      <Paper sx={{ p: 3 }}>
        <Typography variant="subtitle1" sx={{ mb: 2 }}>
          Sales-spezifische Benachrichtigungen
        </Typography>
        
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
          <FormControlLabel
            control={
              <Switch
                checked={settings.newCustomerAlert}
                onChange={() => handleToggle('newCustomerAlert')}
              />
            }
            label="Neue Kunden-Benachrichtigung"
          />
          
          <FormControlLabel
            control={
              <Switch
                checked={settings.dealUpdates}
                onChange={() => handleToggle('dealUpdates')}
              />
            }
            label="Deal-Updates"
          />
          
          <FormControlLabel
            control={
              <Switch
                checked={settings.dailyDigest}
                onChange={() => handleToggle('dailyDigest')}
              />
            }
            label="Tägliche Zusammenfassung"
          />
        </Box>
      </Paper>
    </Box>
  );
};
```

---

## 🎯 SUCCESS CRITERIA

**Nach M7 Enhancement:**
1. ✅ System Settings Tab gefüllt und funktioniert
2. ✅ Security Settings Tab mit Permissions
3. ✅ Notification Settings Tab vollständig
4. ✅ Alle Settings persistent gespeichert
5. ✅ User kann alle Einstellungen anpassen

**Geschätzter Aufwand:** 3 Tage
**Voraussetzung:** Entscheidung D5 (Settings Storage) geklärt

---

## 📞 NÄCHSTE SCHRITTE

1. **Entscheidung D5 klären** - Settings Storage Strategy
2. **SystemSettingsTab implementieren** - System-Einstellungen
3. **SecuritySettingsTab implementieren** - Sicherheits-Einstellungen
4. **NotificationSettingsTab implementieren** - Benachrichtigungen
5. **SettingsPage erweitern** - Alle Tabs integrieren

**WICHTIG:** Tab-Struktur ist perfekt - nur Inhalte hinzufügen!