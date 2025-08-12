/**
 * Filialstruktur Layout Component
 *
 * Spezielle Layout-Komponente für die Filialstruktur-Felder:
 * - Labels in zwei Zeilen oberhalb
 * - Alle Input-Felder in einer Zeile darunter
 * - Mobile: Vertikal gestapelt
 */

import React from 'react';
import { Box, Typography, useTheme, Tooltip } from '@mui/material';
import { styled } from '@mui/material/styles';
import { Info as InfoIcon } from '@mui/icons-material';
import type { FieldDefinition } from '../../types/field.types';
import { isFieldDefinition } from '../../types/field.types';

interface FilialstrukturLayoutProps {
  /** Filialstruktur-Felder */
  fields: FieldDefinition[];
  /** Render-Funktion für einzelne Felder */
  renderField: (field: FieldDefinition) => React.ReactNode;
  /** Aktuelle Formular-Werte */
  values: Record<string, unknown>;
}

const LabelsContainer = styled(Box)(({ theme }) => ({
  display: 'flex',
  flexWrap: 'wrap',
  gap: theme.spacing(2),
  marginBottom: theme.spacing(1),

  // Labels für kompakte Nummer-Felder
  '& .label-number-compact': {
    flex: '0 0 auto',
    minWidth: '60px',
    maxWidth: '90px',
  },

  // Labels für Dropdown-Felder
  '& .label-dropdown-auto': {
    flex: '0 0 auto',
    minWidth: '200px',
  },

  // Mobile Breakpoint
  '@media (max-width: 600px)': {
    flexDirection: 'column',
    gap: theme.spacing(0.5),

    '& .label-dropdown-auto, & .label-number-compact': {
      width: '100%',
      maxWidth: '100%',
      minWidth: 'unset',
    },
  },
}));

const InputsContainer = styled(Box)(({ theme }) => ({
  display: 'flex',
  flexWrap: 'wrap',
  gap: theme.spacing(2),
  alignItems: 'flex-start',

  // Nummer-Felder kompakt
  '& .field-number-compact': {
    flex: '0 0 auto',
    minWidth: '60px',
    maxWidth: '90px',

    // Nummer rechtsbündig
    '& input[type="number"]': {
      textAlign: 'right',
      padding: '8px 12px',
    },
  },

  // Dropdown Auto-Width Felder wachsen mit Inhalt
  '& .field-dropdown-auto': {
    flex: '0 0 auto',
    minWidth: '200px',
    maxWidth: 'none',
    width: 'auto',
  },

  // Mobile Breakpoint
  '@media (max-width: 600px)': {
    flexDirection: 'column',
    gap: theme.spacing(2),

    // Mobile: Alle Felder volle Breite
    '& .field-dropdown-auto, & .field-number-compact': {
      width: '100%',
      maxWidth: '100%',
      minWidth: 'unset',
      flex: '1 1 100%',
    },
  },
}));

const FieldLabel = styled(Typography)(({ theme }) => ({
  fontSize: '0.875rem',
  fontWeight: 500,
  color: theme.palette.text.primary,
  lineHeight: 1.2,
  minHeight: '32px', // Platz für 2-zeilige Labels
  display: 'flex',
  alignItems: 'center',

  // Required Stern
  '&[data-required="true"]::after': {
    content: '"*"',
    color: theme.palette.error.main,
    marginLeft: '4px',
  },
}));

/**
 * Filialstruktur Layout
 *
 * Organisiert die 7 Filialstruktur-Felder in einem kompakten Layout:
 *
 * Zeile 1: [Standorte (EU)] [Deutschland] [Österreich] [Schweiz]
 * Zeile 2: [Rest-EU] [Expansion] [Entscheidung]
 * Zeile 3: [Input 1] [Input 2] [Input 3] [Input 4] [Input 5] [Input 6] [Input 7]
 */
export const FilialstrukturLayout: React.FC<FilialstrukturLayoutProps> = ({
  fields,
  renderField,
  values: _values,
}) => {
  const _theme = useTheme();

  // Sortiere Felder in der gewünschten Reihenfolge
  const fieldOrder = [
    'totalLocationsEU', // Standorte gesamt (EU)
    'locationsGermany', // davon Deutschland
    'locationsAustria', // davon Österreich
    'locationsSwitzerland', // davon Schweiz
    'locationsRestEU', // davon Rest-EU
    'expansionPlanned', // Expansion geplant
    'decisionStructure', // Entscheidungsstruktur
  ];

  const sortedFields = fieldOrder
    .map(key => fields.find(f => f.key === key))
    .filter(isFieldDefinition);

  return (
    <Box>
      {/* Labels-Bereich */}
      <LabelsContainer>
        {sortedFields.map(field => {
          // CSS-Klasse basierend auf Feldtyp
          const labelClass =
            field.fieldType === 'number'
              ? 'label-number-compact'
              : field.fieldType === 'select' || field.fieldType === 'dropdown'
                ? 'label-dropdown-auto'
                : '';

          return (
            <Box
              key={`label-${field.key}`}
              className={labelClass}
              sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}
            >
              <FieldLabel data-required={field.required}>{field.label}</FieldLabel>
              {/* Hilfe-Icon für Felder mit helpText */}
              {field.helpText && (
                <Tooltip title={field.helpText} placement="top">
                  <InfoIcon
                    sx={{
                      fontSize: 14,
                      color: 'action.active',
                      cursor: 'help',
                    }}
                  />
                </Tooltip>
              )}
            </Box>
          );
        })}
      </LabelsContainer>

      {/* Input-Felder-Bereich */}
      <InputsContainer>
        {sortedFields.map(field => {
          // CSS-Klasse basierend auf Feldtyp
          const fieldClass =
            field.fieldType === 'number'
              ? 'field-number-compact'
              : field.fieldType === 'select' || field.fieldType === 'dropdown'
                ? 'field-dropdown-auto'
                : '';

          return (
            <Box
              key={`input-${field.key}`}
              className={fieldClass}
              sx={{
                '& .MuiFormControl-root': {
                  marginBottom: 0, // Kein zusätzlicher Abstand
                  width: '100%', // Wichtig für number fields
                },
              }}
            >
              {renderField(field)}
            </Box>
          );
        })}
      </InputsContainer>
    </Box>
  );
};
