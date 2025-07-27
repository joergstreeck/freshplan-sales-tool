/**
 * Field Wrapper Component
 * 
 * Provides consistent wrapper for all field types with label, error handling,
 * and DSGVO-compliant sensitive data indicators.
 * 
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/06-SECURITY/01-dsgvo-compliance.md
 */

import React from 'react';
import { Box, Typography, Tooltip, Chip } from '@mui/material';
import { Info as InfoIcon, Lock as LockIcon } from '@mui/icons-material';
import type { FieldDefinition } from '../../types/field.types';

interface FieldWrapperProps {
  /** Field definition */
  field: FieldDefinition;
  /** Child field component */
  children: React.ReactNode;
  /** Additional error message */
  error?: string;
}

/**
 * Field Wrapper
 * 
 * Wraps field components with consistent styling and metadata display.
 * Shows required indicators, sensitive data warnings, and help text.
 */
export const FieldWrapper: React.FC<FieldWrapperProps> = ({
  field,
  children,
  error
}) => {
  return (
    <Box sx={{ width: '100%' }}>
      {/* Field Label with Indicators */}
      <Box sx={{ display: 'flex', alignItems: 'center', mb: 0.5 }}>
        <Typography
          component="label"
          htmlFor={field.key}
          sx={{
            fontSize: '0.875rem',
            fontWeight: 500,
            color: error ? 'error.main' : 'text.primary'
          }}
        >
          {field.label}
          {field.required && (
            <Typography
              component="span"
              sx={{ color: 'error.main', ml: 0.5 }}
              aria-label="erforderlich"
            >
              *
            </Typography>
          )}
        </Typography>
        
        {/* Sensitive Data Indicator */}
        {field.sensitive && (
          <Tooltip title="Dieses Feld enthält sensible Daten (DSGVO-relevant)">
            <Chip
              icon={<LockIcon />}
              label="Sensibel"
              size="small"
              sx={{ ml: 1, height: 20 }}
              color="warning"
            />
          </Tooltip>
        )}
        
        {/* Help Icon */}
        {field.helpText && !error && (
          <Tooltip title={field.helpText}>
            <InfoIcon
              sx={{
                ml: 1,
                fontSize: 16,
                color: 'action.active',
                cursor: 'help'
              }}
            />
          </Tooltip>
        )}
      </Box>
      
      {/* Field Component */}
      {children}
      
      {/* Error or Help Text */}
      {(error || field.helpText) && (
        <Typography
          variant="caption"
          sx={{
            mt: 0.5,
            display: 'block',
            color: error ? 'error.main' : 'text.secondary'
          }}
        >
          {error || field.helpText}
        </Typography>
      )}
    </Box>
  );
};