import React, { useEffect, useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Button,
  Chip,
  Alert,
  IconButton,
  Tooltip,
  Collapse
} from '@mui/material';
import {
  Settings as SettingsIcon,
  ExpandMore as ExpandIcon,
  ExpandLess as CollapseIcon,
  Launch as LaunchIcon
} from '@mui/icons-material';
import { UserLeadOperationsPanel } from '../operations/UserLeadOperationsPanel';
import { useOperationsGuidance } from '../hooks/useOperationsGuidance';

interface OperationsHelpIntegrationProps {
  userQuery?: string;
  onOperationsLaunch?: () => void;
  showInline?: boolean;
}

/**
 * Integration Component für Operations-Guidance im CAR-Strategy Help-System
 * Erkennt Operations-relevante Queries und bietet structured guidance
 */
export const OperationsHelpIntegration: React.FC<OperationsHelpIntegrationProps> = ({
  userQuery,
  onOperationsLaunch,
  showInline = true
}) => {
  const {
    guidance,
    isLoading,
    error,
    getGuidance,
    checkConfidence,
    getTemplates,
    executeOperationsAction,
    copyToClipboard
  } = useOperationsGuidance();

  const [confidenceCheck, setConfidenceCheck] = useState<any>(null);
  const [showFullGuidance, setShowFullGuidance] = useState(false);
  const [templates, setTemplates] = useState<Record<string, any> | null>(null);

  // Check confidence when query changes
  useEffect(() => {
    if (userQuery) {
      checkConfidence(userQuery).then(setConfidenceCheck);
    }
  }, [userQuery, checkConfidence]);

  // Load templates on mount
  useEffect(() => {
    getTemplates().then(setTemplates);
  }, [getTemplates]);

  const handleGetFullGuidance = async () => {
    if (userQuery) {
      await getGuidance(userQuery);
      setShowFullGuidance(true);
    }
  };

  const handleTemplateSelect = async (templateQuery: string) => {
    await getGuidance(templateQuery);
    setShowFullGuidance(true);
  };

  // Don't render if no operations relevance
  if (!confidenceCheck?.has_guidance && !userQuery?.toLowerCase().includes('operation')) {
    return null;
  }

  return (
    <Box sx={{ mb: 2 }}>
      {/* Confidence-based Operations-Suggestion */}
      {confidenceCheck?.has_guidance && (
        <Card
          sx={{
            mb: 2,
            background: confidenceCheck.tier === 'high'
              ? 'linear-gradient(135deg, #1976d2 0%, #42a5f5 100%)'
              : 'linear-gradient(135deg, #ed6c02 0%, #ff9800 100%)',
            color: 'white'
          }}
        >
          <CardContent sx={{ pb: 2 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 1 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <SettingsIcon fontSize="small" />
                <Typography variant="subtitle2" sx={{ fontWeight: 'bold' }}>
                  Operations-Guidance verfügbar
                </Typography>
                <Chip
                  label={`${Math.round(confidenceCheck.confidence * 100)}% Relevanz`}
                  size="small"
                  sx={{ backgroundColor: 'rgba(255,255,255,0.2)', color: 'white' }}
                />
              </Box>
              <Tooltip title="Vollständige Operations-Guidance">
                <IconButton
                  size="small"
                  onClick={handleGetFullGuidance}
                  sx={{ color: 'white' }}
                  disabled={isLoading}
                >
                  <LaunchIcon fontSize="small" />
                </IconButton>
              </Tooltip>
            </Box>

            <Typography variant="body2" sx={{ opacity: 0.9, mb: 2 }}>
              Ihre Anfrage betrifft User-Lead-Protection Operations. Strukturierte Guidance verfügbar.
            </Typography>

            <Button
              variant="contained"
              onClick={handleGetFullGuidance}
              disabled={isLoading}
              sx={{
                backgroundColor: 'rgba(255,255,255,0.2)',
                color: 'white',
                '&:hover': {
                  backgroundColor: 'rgba(255,255,255,0.3)'
                }
              }}
            >
              {isLoading ? 'Lädt...' : 'Operations-Guide öffnen'}
            </Button>
          </CardContent>
        </Card>
      )}

      {/* Operations-Templates Quick-Access */}
      {templates && !userQuery && (
        <Card sx={{ mb: 2 }}>
          <CardContent>
            <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }}>
              <Typography variant="h6" sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <SettingsIcon color="primary" />
                Operations-Shortcuts
              </Typography>
              <IconButton
                size="small"
                onClick={() => setShowFullGuidance(!showFullGuidance)}
              >
                {showFullGuidance ? <CollapseIcon /> : <ExpandIcon />}
              </IconButton>
            </Box>

            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
              Häufige Operations-Aufgaben mit einem Klick
            </Typography>

            <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
              {Object.entries(templates).slice(0, 3).map(([key, template]) => (
                <Chip
                  key={key}
                  label={template.title}
                  onClick={() => handleTemplateSelect(template.query)}
                  clickable
                  variant="outlined"
                  color="primary"
                />
              ))}
            </Box>

            <Collapse in={showFullGuidance}>
              <Box sx={{ mt: 2, display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                {Object.entries(templates).slice(3).map(([key, template]) => (
                  <Chip
                    key={key}
                    label={template.title}
                    onClick={() => handleTemplateSelect(template.query)}
                    clickable
                    variant="outlined"
                    color="primary"
                  />
                ))}
              </Box>
            </Collapse>
          </CardContent>
        </Card>
      )}

      {/* Error Display */}
      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          Operations-Guidance Fehler: {error}
        </Alert>
      )}

      {/* Full Operations-Guidance Display */}
      {guidance && showInline && (
        <Box sx={{ mt: 2 }}>
          <UserLeadOperationsPanel
            response={guidance.guidance}
            onActionExecute={executeOperationsAction}
            onCopyToClipboard={copyToClipboard}
          />
        </Box>
      )}
    </Box>
  );
};

export default OperationsHelpIntegration;