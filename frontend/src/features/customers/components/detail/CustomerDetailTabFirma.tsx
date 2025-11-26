/**
 * Customer Detail Tab: Firma
 *
 * Sprint 2.1.7.2 D11: Server-Driven Customer Cards - Phase 2
 *
 * Rendert die 3 Cards für die Firma-Informationen:
 * 1. company_profile - Unternehmensprofil
 * 2. locations - Standorte (wird in Phase 3 erweitert)
 * 3. classification - Klassifikation & Segmentierung
 *
 * Layout: 2-Spalten Grid (Desktop), 1-Spalte (Mobile)
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */

import React from 'react';
import { Box, CircularProgress, Alert, Typography, Grid } from '@mui/material';
// Alert wird für Error/Warning States benötigt
import { DynamicCustomerCard } from '../../../../components/DynamicCustomerCard';
import { useCustomerSchema } from '../../../../hooks/useCustomerSchema';

interface CustomerDetailTabFirmaProps {
  customerId: string;
}

/**
 * Tab "Firma"
 *
 * Zeigt Firma-bezogene Cards in einem 2-Spalten Grid Layout.
 * Server-Driven: Backend definiert welche Felder angezeigt werden.
 */
export const CustomerDetailTabFirma: React.FC<CustomerDetailTabFirmaProps> = ({ customerId }) => {
  const { data: cardSchemas, isLoading, error } = useCustomerSchema();

  // Filter cards for Tab "Firma"
  const firmaCardIds = ['company_profile', 'locations', 'classification'];
  const firmaCards = cardSchemas?.filter(schema => firmaCardIds.includes(schema.cardId)) || [];

  if (isLoading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: 400 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    return (
      <Alert severity="error" sx={{ mb: 2 }}>
        <Typography variant="h6" gutterBottom>
          Fehler beim Laden des Schemas
        </Typography>
        <Typography variant="body2">{error.message}</Typography>
      </Alert>
    );
  }

  if (firmaCards.length === 0) {
    return (
      <Alert severity="warning" sx={{ mb: 2 }}>
        Keine Firma-Karten verfügbar. Erwartete Card-IDs: {firmaCardIds.join(', ')}
      </Alert>
    );
  }

  // Sort cards by order
  const sortedCards = [...firmaCards].sort((a, b) => a.order - b.order);

  return (
    <Box>
      {/* 2-Column Grid Layout with equal height cards */}
      <Grid container spacing={2} sx={{ alignItems: 'stretch' }}>
        {sortedCards.map(schema => (
          <Grid key={schema.cardId} size={{ xs: 12, md: 6 }} sx={{ display: 'flex' }}>
            <DynamicCustomerCard schema={schema} customerId={customerId} />
          </Grid>
        ))}
      </Grid>
    </Box>
  );
};
