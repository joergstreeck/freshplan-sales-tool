/**
 * Server-Driven Customer Cards Container
 *
 * Sprint 2.1.7.2 D11: Server-Driven Customer Cards
 *
 * Container component that loads schema and renders all 7 customer cards.
 * This replaces the old Customer Wizard approach with a server-driven architecture.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */

import React from 'react';
import { Box, CircularProgress, Alert, Typography, Chip, Stack } from '@mui/material';
import { DynamicCustomerCard } from './DynamicCustomerCard';
import { useCustomerSchema } from '../hooks/useCustomerSchema';

interface ServerDrivenCustomerCardsProps {
  customerId: string;
}

/**
 * Server-Driven Customer Cards Container
 *
 * Loads card schema from backend and renders all customer cards dynamically.
 *
 * Key features:
 * - Backend defines structure (no hardcoded fields)
 * - Automatic Lead→Customer parity
 * - All 7 cards from single schema endpoint
 * - Ordered by backend-defined `order` property
 */
export const ServerDrivenCustomerCards: React.FC<ServerDrivenCustomerCardsProps> = ({
  customerId,
}) => {
  const { data: cardSchemas, isLoading, error } = useCustomerSchema();

  if (isLoading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: 400 }}>
        <Stack spacing={2} alignItems="center">
          <CircularProgress />
          <Typography variant="body2" color="text.secondary">
            Lade Kundenprofil-Schema...
          </Typography>
        </Stack>
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

  if (!cardSchemas || cardSchemas.length === 0) {
    return (
      <Alert severity="warning" sx={{ mb: 2 }}>
        Keine Kundenkarten-Schemas verfügbar.
      </Alert>
    );
  }

  // Sort cards by order (just to be sure, backend should already sort them)
  const sortedCards = [...cardSchemas].sort((a, b) => a.order - b.order);

  return (
    <Box>
      {/* Info Banner */}
      <Alert severity="info" sx={{ mb: 3 }}>
        <Stack direction="row" spacing={1} alignItems="center" flexWrap="wrap">
          <Typography variant="body2" component="span">
            <strong>Server-Driven UI:</strong> Alle Felder werden vom Backend definiert.
          </Typography>
          <Chip label={`${sortedCards.length} Karten`} size="small" color="primary" />
          <Chip label="Sprint 2.1.7.2 D11" size="small" variant="outlined" />
        </Stack>
      </Alert>

      {/* Render all customer cards */}
      {sortedCards.map(schema => (
        <DynamicCustomerCard key={schema.cardId} schema={schema} customerId={customerId} />
      ))}
    </Box>
  );
};
