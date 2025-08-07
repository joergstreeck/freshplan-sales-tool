/**
 * AdaptiveFormContainer - Theme-integriertes CSS Grid Layout
 *
 * Container für adaptive Formulare mit automatischem Feldwachstum
 * und intelligentem Umbruch basierend auf Inhalt.
 * Unterstützt sowohl Grid- als auch Flexbox-Layout.
 *
 * @see /docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/prototypes/ADAPTIVE_LAYOUT_IMPLEMENTATION_GUIDE.md
 */

import React from 'react';
import type { ReactNode } from 'react';
import { styled } from '@mui/material/styles';
import { useCustomerFieldTheme } from '../../theme';

interface AdaptiveFormContainerProps {
  children: ReactNode;
  variant?: 'grid' | 'flexbox';
  className?: string;
}

const GridContainer = styled('div')(({ theme }) => ({
  display: 'grid',
  gridTemplateColumns: 'repeat(auto-fit, minmax(var(--kunde-mindest-spalten-breite, 280px), 1fr))',
  gap: 'var(--kunde-spalten-abstand, 16px) var(--kunde-zeilen-abstand, 24px)',
  width: '100%',

  // Mobile First
  [theme.breakpoints.down('md')]: {
    gridTemplateColumns: '1fr',
    gap: theme.spacing(2),
  },

  // Theme-basierte Feldgrößen
  '& .field-kompakt': {
    gridColumn: 'span 1',
    maxWidth: 'var(--kunde-feld-kompakt-max, 120px)',
  },

  '& .field-klein': {
    gridColumn: 'span 2',
    maxWidth: 'var(--kunde-feld-klein-max, 200px)',
  },

  '& .field-mittel': {
    gridColumn: 'span 3',
    maxWidth: 'var(--kunde-feld-mittel-max, 300px)',
  },

  '& .field-groß': {
    gridColumn: 'span 4',
    maxWidth: 'var(--kunde-feld-groß-max, 400px)',
  },

  '& .field-voll': {
    gridColumn: '1 / -1',
    maxWidth: '100%',
  },

  // Spezielle Behandlung für Dropdown-Felder - Text muss immer lesbar sein
  '& .field-dropdown-auto': {
    gridColumn: 'span 1',
    minWidth: '200px',
    maxWidth: 'none', // Keine Begrenzung - wächst mit Inhalt
    width: 'auto',
  },

  // Mobile Overrides
  [theme.breakpoints.down('sm')]: {
    '& .field-kompakt, & .field-klein, & .field-mittel, & .field-groß': {
      gridColumn: '1 / -1',
      maxWidth: '100%',
    },
  },
}));

const FlexContainer = styled('div')(({ theme }) => ({
  display: 'flex',
  flexWrap: 'wrap',
  gap: 'var(--kunde-spalten-abstand, 16px) var(--kunde-zeilen-abstand, 24px)',
  width: '100%',
  alignItems: 'flex-start',

  // Flexbox Feldgrößen mit automatischem Umbruch - Best Practice Werte
  '& .field-kompakt': {
    flex: '0 1 auto',
    minWidth: '60px',
    maxWidth: '100px',
  },
  '& .field-klein': {
    flex: '0 1 auto',
    minWidth: '100px',
    maxWidth: '160px',
  },
  '& .field-mittel': {
    flex: '0 1 auto',
    minWidth: '140px',
    maxWidth: '240px',
  },
  '& .field-groß': {
    flex: '1 1 auto',
    minWidth: '220px',
    maxWidth: '400px',
  },
  '& .field-voll': {
    flex: '1 1 100%',
    minWidth: '350px',
    maxWidth: '100%',
  },

  // Spezielle Behandlung für Dropdown-Felder - Text muss immer lesbar sein
  '& .field-dropdown-auto': {
    flex: '0 1 auto',
    minWidth: '200px',
    maxWidth: 'none', // Keine Begrenzung - wächst mit Inhalt
  },

  // Mobile Breakpoint
  [theme.breakpoints.down('sm')]: {
    '& > *': {
      flex: '1 1 100%',
      maxWidth: '100%',
      minWidth: 'unset',
    },
  },
}));

/**
 * Adaptive Form Container
 *
 * Nutzt CSS Grid oder Flexbox für intelligente Feldverteilung.
 * Felder passen sich automatisch an und brechen bei Bedarf um.
 * Theme-Werte werden aus dem CustomerFieldTheme bezogen.
 */
export const AdaptiveFormContainer: React.FC<AdaptiveFormContainerProps> = ({
  children,
  variant = 'flexbox', // Flexbox als Standard für besseres Umbruchverhalten
  className = '',
}) => {
  const { theme, cssVariables } = useCustomerFieldTheme();
  const Container = variant === 'grid' ? GridContainer : FlexContainer;

  return (
    <Container className={`adaptive-form-container ${className}`} style={cssVariables}>
      {children}
    </Container>
  );
};
