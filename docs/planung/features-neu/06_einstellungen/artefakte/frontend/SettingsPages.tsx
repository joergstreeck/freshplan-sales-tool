import React, { useState, useEffect } from 'react';
import {
  Paper,
  Typography,
  ToggleButtonGroup,
  ToggleButton,
  FormControlLabel,
  Switch,
  Button,
  Chip,
  Stack,
  Alert,
  Snackbar,
  Skeleton,
  Box,
  CircularProgress,
} from '@mui/material';
import { Save as SaveIcon, Refresh as RefreshIcon } from '@mui/icons-material';
import { useUISettings, useNotificationSettings, useSLASettings, useUpdateSettings } from './useSettings_OPTIMIZED';

/**
 * OPTIMIZED: Enhanced UI components with proper UX patterns
 * - Clean Material-UI integration (from NEW)
 * - Comprehensive error handling and loading states (ENHANCED)
 * - Dirty state tracking and unsaved changes warnings (ENHANCED)
 * - Optimistic updates with visual feedback (ENHANCED)
 * - Accessibility and responsive design (ENHANCED)
 */

// Enhanced appearance page with dirty state tracking
export function AppearancePage() {
  const { settings: uiSettings, loading, error, refetch } = useUISettings();
  const { updateSetting, isLoading: isSaving, error: saveError } = useUpdateSettings();

  const [theme, setTheme] = useState<'light' | 'dark'>('light');
  const [isDirty, setIsDirty] = useState(false);
  const [saveSuccess, setSaveSuccess] = useState(false);

  // Sync with server state
  useEffect(() => {
    if (uiSettings?.theme) {
      setTheme(uiSettings.theme);
      setIsDirty(false);
    }
  }, [uiSettings]);

  // Track dirty state
  useEffect(() => {
    setIsDirty(theme !== uiSettings?.theme);
  }, [theme, uiSettings?.theme]);

  const handleSave = async () => {
    try {
      await updateSetting('ui.theme', theme);
      setIsDirty(false);
      setSaveSuccess(true);
    } catch (err) {
      // Error handling is managed by the hook
      console.error('Failed to save theme:', err);
    }
  };

  const handleReset = () => {
    if (uiSettings?.theme) {
      setTheme(uiSettings.theme);
      setIsDirty(false);
    }
  };

  if (loading) {
    return (
      <Paper sx={{ p: 3 }}>
        <Skeleton variant="text" width={200} height={32} />
        <Skeleton variant="rectangular" width={300} height={56} sx={{ mt: 2 }} />
        <Skeleton variant="rectangular" width={120} height={36} sx={{ mt: 2 }} />
      </Paper>
    );
  }

  return (
    <Paper sx={{ p: 3 }}>
      <Typography variant="h6" gutterBottom>
        Darstellung
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} action={
          <Button color="inherit" size="small" onClick={refetch} startIcon={<RefreshIcon />}>
            Wiederholen
          </Button>
        }>
          Fehler beim Laden der Einstellungen: {error.message}
        </Alert>
      )}

      {saveError && (
        <Alert severity="error" sx={{ mb: 2 }}>
          Fehler beim Speichern: {saveError.detail}
        </Alert>
      )}

      <Box sx={{ mb: 3 }}>
        <Typography variant="body2" color="text.secondary" gutterBottom>
          Wählen Sie Ihr bevorzugtes Design-Theme
        </Typography>

        <ToggleButtonGroup
          value={theme}
          exclusive
          onChange={(_, value) => value && setTheme(value)}
          sx={{ mt: 1 }}
          disabled={isSaving}
        >
          <ToggleButton value="light" aria-label="Hell">
            ☀️ Hell
          </ToggleButton>
          <ToggleButton value="dark" aria-label="Dunkel">
            🌙 Dunkel
          </ToggleButton>
        </ToggleButtonGroup>
      </Box>

      <Stack direction="row" spacing={1}>
        <Button
          variant="contained"
          onClick={handleSave}
          disabled={!isDirty || isSaving}
          startIcon={isSaving ? <CircularProgress size={16} /> : <SaveIcon />}
        >
          {isSaving ? 'Speichert...' : 'Speichern'}
        </Button>

        {isDirty && (
          <Button variant="outlined" onClick={handleReset} disabled={isSaving}>
            Zurücksetzen
          </Button>
        )}
      </Stack>

      {isDirty && (
        <Typography variant="caption" color="warning.main" sx={{ mt: 1, display: 'block' }}>
          ⚠️ Sie haben ungespeicherte Änderungen
        </Typography>
      )}

      <Snackbar
        open={saveSuccess}
        autoHideDuration={3000}
        onClose={() => setSaveSuccess(false)}
        message="Theme erfolgreich gespeichert"
      />
    </Paper>
  );
}

// Enhanced preferences page with object merge handling
export function PreferencesPage() {
  const { settings: notificationSettings, loading, error, refetch } = useNotificationSettings();
  const { updateSetting, isLoading: isSaving, error: saveError } = useUpdateSettings();

  const [channels, setChannels] = useState({
    email: false,
    phone: false,
    sms: false,
  });
  const [isDirty, setIsDirty] = useState(false);
  const [saveSuccess, setSaveSuccess] = useState(false);

  // Sync with server state
  useEffect(() => {
    if (notificationSettings?.channels) {
      setChannels({
        email: notificationSettings.channels.email || false,
        phone: notificationSettings.channels.phone || false,
        sms: notificationSettings.channels.sms || false,
      });
      setIsDirty(false);
    }
  }, [notificationSettings]);

  // Track dirty state
  useEffect(() => {
    const serverChannels = notificationSettings?.channels || {};
    const hasChanges =
      channels.email !== (serverChannels.email || false) ||
      channels.phone !== (serverChannels.phone || false) ||
      channels.sms !== (serverChannels.sms || false);
    setIsDirty(hasChanges);
  }, [channels, notificationSettings?.channels]);

  const handleChannelChange = (channel: keyof typeof channels, enabled: boolean) => {
    setChannels(prev => ({ ...prev, [channel]: enabled }));
  };

  const handleSave = async () => {
    try {
      await updateSetting('notifications.channels', channels);
      setIsDirty(false);
      setSaveSuccess(true);
    } catch (err) {
      console.error('Failed to save notification preferences:', err);
    }
  };

  const handleReset = () => {
    if (notificationSettings?.channels) {
      setChannels({
        email: notificationSettings.channels.email || false,
        phone: notificationSettings.channels.phone || false,
        sms: notificationSettings.channels.sms || false,
      });
      setIsDirty(false);
    }
  };

  if (loading) {
    return (
      <Paper sx={{ p: 3 }}>
        <Skeleton variant="text" width={200} height={32} />
        {[1, 2, 3].map(i => (
          <Skeleton key={i} variant="rectangular" width="100%" height={48} sx={{ mt: 1 }} />
        ))}
      </Paper>
    );
  }

  return (
    <Paper sx={{ p: 3 }}>
      <Typography variant="h6" gutterBottom>
        Benachrichtigungs-Präferenzen
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} action={
          <Button color="inherit" size="small" onClick={refetch} startIcon={<RefreshIcon />}>
            Wiederholen
          </Button>
        }>
          Fehler beim Laden: {error.message}
        </Alert>
      )}

      {saveError && (
        <Alert severity="error" sx={{ mb: 2 }}>
          Fehler beim Speichern: {saveError.detail}
        </Alert>
      )}

      <Box sx={{ mb: 3 }}>
        <Typography variant="body2" color="text.secondary" gutterBottom>
          Wählen Sie aus, über welche Kanäle Sie Benachrichtigungen erhalten möchten
        </Typography>

        <Stack spacing={1} sx={{ mt: 2 }}>
          <FormControlLabel
            control={
              <Switch
                checked={channels.email}
                onChange={(_, checked) => handleChannelChange('email', checked)}
                disabled={isSaving}
              />
            }
            label="📧 E-Mail Benachrichtigungen"
          />

          <FormControlLabel
            control={
              <Switch
                checked={channels.phone}
                onChange={(_, checked) => handleChannelChange('phone', checked)}
                disabled={isSaving}
              />
            }
            label="📞 Telefon Benachrichtigungen"
          />

          <FormControlLabel
            control={
              <Switch
                checked={channels.sms}
                onChange={(_, checked) => handleChannelChange('sms', checked)}
                disabled={isSaving}
              />
            }
            label="📱 SMS Benachrichtigungen"
          />
        </Stack>
      </Box>

      <Stack direction="row" spacing={1}>
        <Button
          variant="contained"
          onClick={handleSave}
          disabled={!isDirty || isSaving}
          startIcon={isSaving ? <CircularProgress size={16} /> : <SaveIcon />}
        >
          {isSaving ? 'Speichert...' : 'Speichern'}
        </Button>

        {isDirty && (
          <Button variant="outlined" onClick={handleReset} disabled={isSaving}>
            Zurücksetzen
          </Button>
        )}
      </Stack>

      {isDirty && (
        <Typography variant="caption" color="warning.main" sx={{ mt: 1, display: 'block' }}>
          ⚠️ Sie haben ungespeicherte Änderungen
        </Typography>
      )}

      <Snackbar
        open={saveSuccess}
        autoHideDuration={3000}
        onClose={() => setSaveSuccess(false)}
        message="Präferenzen erfolgreich gespeichert"
      />
    </Paper>
  );
}

// Enhanced notifications page with list merge handling
export function NotificationsPage() {
  const { settings: slaSettings, loading, error, refetch } = useSLASettings();
  const { updateSetting, isLoading: isSaving, error: saveError } = useUpdateSettings();

  const [followups, setFollowups] = useState<string[]>([]);
  const [isDirty, setIsDirty] = useState(false);
  const [saveSuccess, setSaveSuccess] = useState(false);

  const availableFollowups = ['P3D', 'P7D', 'P14D', 'P21D', 'P30D'];
  const followupLabels: Record<string, string> = {
    'P3D': '3 Tage',
    'P7D': '7 Tage',
    'P14D': '14 Tage',
    'P21D': '21 Tage',
    'P30D': '30 Tage',
  };

  // Sync with server state
  useEffect(() => {
    if (slaSettings?.sample?.followups) {
      setFollowups([...slaSettings.sample.followups]);
      setIsDirty(false);
    }
  }, [slaSettings]);

  // Track dirty state
  useEffect(() => {
    const serverFollowups = slaSettings?.sample?.followups || [];
    const hasChanges =
      followups.length !== serverFollowups.length ||
      followups.some(f => !serverFollowups.includes(f)) ||
      serverFollowups.some(f => !followups.includes(f));
    setIsDirty(hasChanges);
  }, [followups, slaSettings?.sample?.followups]);

  const toggleFollowup = (value: string) => {
    setFollowups(prev =>
      prev.includes(value)
        ? prev.filter(f => f !== value)
        : [...prev, value].sort()
    );
  };

  const handleSave = async () => {
    try {
      await updateSetting('sla.sample.followups', followups);
      setIsDirty(false);
      setSaveSuccess(true);
    } catch (err) {
      console.error('Failed to save SLA settings:', err);
    }
  };

  const handleReset = () => {
    if (slaSettings?.sample?.followups) {
      setFollowups([...slaSettings.sample.followups]);
      setIsDirty(false);
    }
  };

  if (loading) {
    return (
      <Paper sx={{ p: 3 }}>
        <Skeleton variant="text" width={300} height={32} />
        <Skeleton variant="rectangular" width="100%" height={80} sx={{ mt: 2 }} />
      </Paper>
    );
  }

  return (
    <Paper sx={{ p: 3 }}>
      <Typography variant="h6" gutterBottom>
        SLA & Benachrichtigungen
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} action={
          <Button color="inherit" size="small" onClick={refetch} startIcon={<RefreshIcon />}>
            Wiederholen
          </Button>
        }>
          Fehler beim Laden: {error.message}
        </Alert>
      )}

      {saveError && (
        <Alert severity="error" sx={{ mb: 2 }}>
          Fehler beim Speichern: {saveError.detail}
        </Alert>
      )}

      <Box sx={{ mb: 3 }}>
        <Typography variant="body2" color="text.secondary" gutterBottom>
          Follow-up Erinnerungen nach Sample-Lieferung
        </Typography>

        <Stack direction="row" spacing={1} sx={{ mt: 2 }} flexWrap="wrap" useFlexGap>
          {availableFollowups.map(period => (
            <Chip
              key={period}
              label={followupLabels[period]}
              color={followups.includes(period) ? 'primary' : 'default'}
              onClick={() => toggleFollowup(period)}
              disabled={isSaving}
              variant={followups.includes(period) ? 'filled' : 'outlined'}
            />
          ))}
        </Stack>

        {followups.length === 0 && (
          <Typography variant="caption" color="warning.main" sx={{ mt: 1, display: 'block' }}>
            ⚠️ Keine Follow-ups ausgewählt - Sie erhalten keine automatischen Erinnerungen
          </Typography>
        )}
      </Box>

      <Stack direction="row" spacing={1}>
        <Button
          variant="contained"
          onClick={handleSave}
          disabled={!isDirty || isSaving}
          startIcon={isSaving ? <CircularProgress size={16} /> : <SaveIcon />}
        >
          {isSaving ? 'Speichert...' : 'Speichern'}
        </Button>

        {isDirty && (
          <Button variant="outlined" onClick={handleReset} disabled={isSaving}>
            Zurücksetzen
          </Button>
        )}
      </Stack>

      {isDirty && (
        <Typography variant="caption" color="warning.main" sx={{ mt: 1, display: 'block' }}>
          ⚠️ Sie haben ungespeicherte Änderungen
        </Typography>
      )}

      <Snackbar
        open={saveSuccess}
        autoHideDuration={3000}
        onClose={() => setSaveSuccess(false)}
        message="SLA-Einstellungen erfolgreich gespeichert"
      />
    </Paper>
  );
}