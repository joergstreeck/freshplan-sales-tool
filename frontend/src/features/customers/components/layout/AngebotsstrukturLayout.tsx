/**
 * Angebotsstruktur Layout Component
 *
 * Strukturiertes Layout für Step 2 Angebotsfelder mit klarer Anordnung
 * und adaptive Theme-Integration.
 */

import React from 'react';
import { Box, Typography, Grid, Paper } from '@mui/material';
import { styled } from '@mui/material/styles';
import { DynamicFieldRenderer } from '../fields/DynamicFieldRenderer';
import type { FieldDefinition } from '../../types/field.types';

interface AngebotsstrukturLayoutProps {
  /** Felder gruppiert nach Kategorien */
  fieldGroups: {
    title: string;
    icon?: string;
    fields: FieldDefinition[];
  }[];
  /** Aktuelle Formular-Werte */
  values: Record<string, any>;
  /** Validierungsfehler */
  errors: Record<string, string>;
  /** Change Handler */
  onChange: (fieldKey: string, value: any) => void;
  /** Blur Handler */
  onBlur: (fieldKey: string) => void;
}

const SectionContainer = styled(Paper)(({ theme }) => ({
  padding: theme.spacing(3),
  marginBottom: theme.spacing(3),
  backgroundColor: theme.palette.background.default,
  border: `1px solid ${theme.palette.divider}`,
  boxShadow: 'none',

  '& .section-title': {
    display: 'flex',
    alignItems: 'center',
    gap: theme.spacing(1),
    marginBottom: theme.spacing(2),

    '& .icon': {
      fontSize: '1.5rem',
    },
  },
}));

/**
 * Angebotsstruktur Layout
 *
 * Organisiert Angebotsfelder in übersichtlichen Gruppen mit
 * adaptive Theme-Integration für optimale Darstellung.
 */
export const AngebotsstrukturLayout: React.FC<AngebotsstrukturLayoutProps> = ({
  fieldGroups,
  values,
  errors,
  onChange,
  onBlur,
}) => {
  return (
    <Box>
      {fieldGroups.map((group, index) => (
        <SectionContainer key={index}>
          <div className="section-title">
            {group.icon && <span className="icon">{group.icon}</span>}
            <Typography variant="h6" component="h3">
              {group.title}
            </Typography>
          </div>

          <DynamicFieldRenderer
            fields={group.fields}
            values={values}
            errors={errors}
            onChange={onChange}
            onBlur={onBlur}
            useAdaptiveLayout={true}
          />
        </SectionContainer>
      ))}
    </Box>
  );
};
