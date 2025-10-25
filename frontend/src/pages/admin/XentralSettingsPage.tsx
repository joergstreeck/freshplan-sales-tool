/**
 * XentralSettingsPage - Admin-UI für Xentral-Einstellungen
 *
 * Sprint 2.1.7.2 - D5: Admin-UI für Xentral-Einstellungen
 *
 * Allows admins to configure Xentral API connection settings.
 *
 * Features:
 * - API URL configuration
 * - API Token management
 * - Mock-Mode toggle (development vs production)
 * - Connection test button
 *
 * @see /docs/planung/artefakte/SPEC_SPRINT_2_1_7_2_TECHNICAL.md Section 5.2
 */

import React, { useState, useEffect } from 'react';
import {
  Box,
  Paper,
  Typography,
  TextField,
  Button,
  Switch,
  FormControlLabel,
  Alert,
  CircularProgress,
  Divider,
} from '@mui/material';
import {
  Save as SaveIcon,
  CloudDone as CloudDoneIcon,
  Warning as WarningIcon,
  Settings as SettingsIcon,
} from '@mui/icons-material';
import { MainLayoutV2 } from '@/components/layout/MainLayoutV2';
import { httpClient } from '@/lib/apiClient';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';

interface XentralSettings {
  apiUrl: string;
  apiToken: string;
  mockMode: boolean;
}

interface ConnectionTestResponse {
  status: 'success' | 'error';
  message: string;
}

const XentralSettingsPage: React.FC = () => {
  const queryClient = useQueryClient();

  // Local form state
  const [formData, setFormData] = useState<XentralSettings>({
    apiUrl: 'https://644b6ff97320d.xentral.biz',
    apiToken: '',
    mockMode: true,
  });

  const [testResult, setTestResult] = useState<ConnectionTestResponse | null>(null);
  const [showPassword, setShowPassword] = useState(false);

  // Fetch current settings
  const {
    data: settings,
    isLoading: settingsLoading,
    error: settingsError,
  } = useQuery<XentralSettings>({
    queryKey: ['xentralSettings'],
    queryFn: async () => {
      try {
        const response = await httpClient.get('/api/admin/xentral/settings');
        return response.data;
      } catch (error: unknown) {
        if (error && typeof error === 'object' && 'response' in error) {
          const axiosError = error as { response?: { status?: number } };
          if (axiosError.response?.status === 404) {
            // No settings in database, use defaults from application.properties
            return null;
          }
        }
        throw error;
      }
    },
    retry: false,
  });

  // Update form when settings are loaded
  useEffect(() => {
    if (settings) {
      setFormData(settings);
    }
  }, [settings]);

  // Save settings mutation
  const saveMutation = useMutation({
    mutationFn: async (data: XentralSettings) => {
      const response = await httpClient.put('/api/admin/xentral/settings', data);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['xentralSettings'] });
      setTestResult({
        status: 'success',
        message: 'Einstellungen erfolgreich gespeichert.',
      });
    },
    onError: (error: unknown) => {
      const message =
        error && typeof error === 'object' && 'message' in error
          ? String(error.message)
          : 'Fehler beim Speichern der Einstellungen.';
      setTestResult({
        status: 'error',
        message,
      });
    },
  });

  // Test connection mutation
  const testMutation = useMutation({
    mutationFn: async () => {
      const response = await httpClient.get<ConnectionTestResponse>(
        '/api/admin/xentral/test-connection'
      );
      return response.data;
    },
    onSuccess: data => {
      setTestResult(data);
    },
    onError: (error: unknown) => {
      const message =
        error && typeof error === 'object' && 'message' in error
          ? String(error.message)
          : 'Verbindungstest fehlgeschlagen.';
      setTestResult({
        status: 'error',
        message,
      });
    },
  });

  const handleSave = () => {
    setTestResult(null);
    saveMutation.mutate(formData);
  };

  const handleTestConnection = () => {
    setTestResult(null);
    testMutation.mutate();
  };

  const handleFieldChange =
    (field: keyof XentralSettings) => (event: React.ChangeEvent<HTMLInputElement>) => {
      setFormData(prev => ({
        ...prev,
        [field]: field === 'mockMode' ? event.target.checked : event.target.value,
      }));
    };

  if (settingsLoading) {
    return (
      <MainLayoutV2 maxWidth="lg">
        <Box
          sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: 400 }}
        >
          <CircularProgress />
        </Box>
      </MainLayoutV2>
    );
  }

  return (
    <MainLayoutV2 maxWidth="lg">
      <Box>
        {/* Header */}
        <Box sx={{ mb: 4 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
            <SettingsIcon sx={{ fontSize: 40, color: 'secondary.main', mr: 2 }} />
            <Typography variant="h4" component="h1">
              Xentral-Einstellungen
            </Typography>
          </Box>
          <Typography variant="body1" color="text.secondary">
            Konfigurieren Sie die Verbindung zu Ihrem Xentral ERP-System.
          </Typography>
        </Box>

        {/* Info Alert - No settings in database */}
        {!settings && !settingsError && (
          <Alert severity="info" sx={{ mb: 3 }}>
            <Typography variant="body2">
              Keine Einstellungen in der Datenbank gefunden. Es werden die Standard-Einstellungen
              aus
              <code>application.properties</code> verwendet.
            </Typography>
          </Alert>
        )}

        {/* Error Alert */}
        {settingsError && (
          <Alert severity="error" sx={{ mb: 3 }}>
            <Typography variant="body2">
              Fehler beim Laden der Einstellungen. Bitte versuchen Sie es später erneut.
            </Typography>
          </Alert>
        )}

        {/* Settings Form */}
        <Paper sx={{ p: 4 }}>
          <Typography variant="h6" gutterBottom>
            API-Konfiguration
          </Typography>

          <Divider sx={{ my: 3 }} />

          {/* API URL */}
          <TextField
            fullWidth
            label="API URL"
            value={formData.apiUrl}
            onChange={handleFieldChange('apiUrl')}
            placeholder="https://644b6ff97320d.xentral.biz"
            helperText="Basis-URL Ihres Xentral ERP-Systems (z.B. https://ihr-unternehmen.xentral.biz)"
            sx={{ mb: 3 }}
            required
            inputProps={{ 'data-testid': 'xentral-api-url-input' }}
          />

          {/* API Token */}
          <TextField
            fullWidth
            label="API Token"
            type={showPassword ? 'text' : 'password'}
            value={formData.apiToken}
            onChange={handleFieldChange('apiToken')}
            placeholder="Ihr Xentral API Token"
            helperText="Authentifizierungs-Token für die Xentral API (wird verschlüsselt gespeichert)"
            sx={{ mb: 3 }}
            required
            inputProps={{ 'data-testid': 'xentral-api-token-input' }}
          />

          <Box sx={{ mb: 3 }}>
            <FormControlLabel
              control={
                <Switch checked={showPassword} onChange={e => setShowPassword(e.target.checked)} />
              }
              label="Token anzeigen"
            />
          </Box>

          <Divider sx={{ my: 3 }} />

          {/* Mock Mode */}
          <Box sx={{ mb: 3 }}>
            <FormControlLabel
              control={
                <Switch checked={formData.mockMode} onChange={handleFieldChange('mockMode')} />
              }
              label="Mock-Mode (Entwicklung)"
            />
            <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mt: 1 }}>
              Aktiviert: Verwendet Mock-Daten für Tests (keine echten API-Aufrufe)
              <br />
              Deaktiviert: Verbindet mit dem echten Xentral ERP-System
            </Typography>
          </Box>

          {/* Test Result */}
          {testResult && (
            <Alert
              severity={testResult.status === 'success' ? 'success' : 'error'}
              sx={{ mb: 3 }}
              icon={testResult.status === 'success' ? <CloudDoneIcon /> : <WarningIcon />}
            >
              {testResult.message}
            </Alert>
          )}

          {/* Action Buttons */}
          <Box sx={{ display: 'flex', gap: 2, mt: 4 }}>
            <Button
              variant="outlined"
              onClick={handleTestConnection}
              disabled={testMutation.isPending || !formData.apiUrl || !formData.apiToken}
              startIcon={
                testMutation.isPending ? <CircularProgress size={20} /> : <CloudDoneIcon />
              }
              data-testid="test-connection-button"
            >
              Verbindung testen
            </Button>

            <Button
              variant="contained"
              onClick={handleSave}
              disabled={saveMutation.isPending || !formData.apiUrl || !formData.apiToken}
              startIcon={saveMutation.isPending ? <CircularProgress size={20} /> : <SaveIcon />}
              data-testid="save-button"
            >
              Speichern
            </Button>
          </Box>
        </Paper>

        {/* Info Box */}
        <Alert severity="info" sx={{ mt: 3 }}>
          <Typography variant="body2" fontWeight="medium" gutterBottom>
            Hinweis zur API-Sicherheit
          </Typography>
          <Typography variant="caption">
            Der API Token wird verschlüsselt in der Datenbank gespeichert. Stellen Sie sicher, dass
            Sie einen Token mit minimalen Berechtigungen verwenden (nur Lesen von Kunden, Rechnungen
            und Mitarbeitern).
          </Typography>
        </Alert>
      </Box>
    </MainLayoutV2>
  );
};

// Default export for lazy loading
export default XentralSettingsPage;
