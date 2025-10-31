/**
 * Customer Detail Tab: Geschäft
 *
 * Sprint 2.1.7.2 D11: Server-Driven Customer Cards - Phase 2
 *
 * Rendert die 4 Cards für die Geschäfts-Informationen:
 * 1. business_data - Geschäftsdaten (Umsatz, Rabatte, etc.)
 * 2. contracts - Verträge & Konditionen
 * 3. pain_points - Pain Points & Bedürfnisse
 * 4. products - Produktinteressen
 *
 * Layout: 2-Spalten Grid (Desktop), 1-Spalte (Mobile)
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */

import React from 'react';
import { Box, CircularProgress, Alert, Typography, Grid } from '@mui/material';
import { DynamicCustomerCard } from '../../../../components/DynamicCustomerCard';
import { useCustomerSchema } from '../../../../hooks/useCustomerSchema';

interface CustomerDetailTabGeschaeftProps {
  customerId: string;
}

/**
 * Tab "Geschäft"
 *
 * Zeigt Geschäfts-bezogene Cards in einem 2-Spalten Grid Layout.
 * Server-Driven: Backend definiert welche Felder angezeigt werden.
 */
export const CustomerDetailTabGeschaeft: React.FC<CustomerDetailTabGeschaeftProps> = ({
  customerId,
}) => {
  const { data: cardSchemas, isLoading, error } = useCustomerSchema();

  // Filter cards for Tab "Geschäft"
  const geschaeftCardIds = ['business_data', 'contracts', 'pain_points', 'products'];
  const geschaeftCards =
    cardSchemas?.filter(schema => geschaeftCardIds.includes(schema.cardId)) || [];

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

  if (geschaeftCards.length === 0) {
    return (
      <Alert severity="warning" sx={{ mb: 2 }}>
        Keine Geschäfts-Karten verfügbar. Erwartete Card-IDs: {geschaeftCardIds.join(', ')}
      </Alert>
    );
  }

  // Sort cards by order
  const sortedCards = [...geschaeftCards].sort((a, b) => a.order - b.order);

  return (
    <Box>
      {/* Info Banner */}
      <Alert severity="info" sx={{ mb: 3 }}>
        <Typography variant="body2">
          <strong>Tab "Geschäft":</strong> Umsatz, Verträge, Pain Points und Produktinteressen.
        </Typography>
        <Typography variant="caption" color="text.secondary">
          {sortedCards.length} Karten · Server-Driven Schema
        </Typography>
      </Alert>

      {/* 2-Column Grid Layout */}
      <Grid container spacing={2}>
        {sortedCards.map(schema => (
          <Grid key={schema.cardId} size={{ xs: 12, md: 6 }}>
            <DynamicCustomerCard schema={schema} customerId={customerId} />
          </Grid>
        ))}
      </Grid>
    </Box>
  );
};
