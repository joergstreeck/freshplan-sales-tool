/**
 * Dynamic Customer Card Component
 *
 * Sprint 2.1.7.2 D11: Server-Driven Customer Cards
 *
 * Framework for rendering customer cards based on backend schema.
 * This is the core of the Server-Driven UI architecture.
 *
 * Features:
 * - ✅ Schema-driven rendering (no hardcoded fields)
 * - ✅ Collapsible sections
 * - ✅ Grid-based layout (responsive)
 * - ✅ MUI Theme compliance
 * - ✅ Deutsche UI-Texte
 * - ✅ Icon support
 * - ✅ Read-only display (Edit mode: Future enhancement)
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */

import React, { useState } from 'react';
import {
  Card,
  CardHeader,
  CardContent,
  Typography,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  Grid,
  Chip,
  Box,
  Skeleton,
  Alert,
} from '@mui/material';
import { ExpandMore as ExpandMoreIcon } from '@mui/icons-material';
import type { CustomerCardSchema } from '../types/customer-schema';
import { DynamicField } from './DynamicField';
import { useCustomerDetails } from '../features/customer/hooks/useCustomerDetails';

interface DynamicCustomerCardProps {
  schema: CustomerCardSchema;
  customerId: string;
}

/**
 * Dynamic Customer Card
 *
 * Renders a complete customer card based on backend schema definition.
 *
 * Backend defines:
 * - Card structure (title, icon, order)
 * - Sections (title, collapsible)
 * - Fields (type, label, validation, grid layout)
 *
 * Frontend just renders what backend defines → True Server-Driven UI!
 */
export const DynamicCustomerCard: React.FC<DynamicCustomerCardProps> = ({ schema, customerId }) => {
  const { title, subtitle, icon, sections, defaultCollapsed } = schema;

  // Load customer data
  const { data: customer, isLoading, error } = useCustomerDetails(customerId);

  // Track card collapsed state
  const [isCardCollapsed, setIsCardCollapsed] = useState(defaultCollapsed);

  // Handle field value changes (for future edit mode)
  const handleFieldChange = (_fieldKey: string, _value: unknown) => {
    // TODO: Implement edit mode in Sprint 2.1.7.3
    // For now, fields are read-only
  };

  // Get field value from customer data
  const getFieldValue = (fieldKey: string): unknown => {
    if (!customer) return null;

    // Handle nested properties with dot notation
    // e.g., "address.city" → customer.address?.city
    const keys = fieldKey.split('.');
    let value: unknown = customer;

    for (const key of keys) {
      if (value == null) return null;
      value = value[key];
    }

    return value;
  };

  // Loading state
  if (isLoading) {
    return (
      <Card sx={{ mb: 2 }}>
        <CardHeader
          avatar={<Skeleton variant="circular" width={40} height={40} />}
          title={<Skeleton width="40%" />}
          subheader={<Skeleton width="60%" />}
        />
        <CardContent>
          <Skeleton variant="rectangular" height={200} />
        </CardContent>
      </Card>
    );
  }

  // Error state
  if (error) {
    return (
      <Card sx={{ mb: 2 }}>
        <CardHeader title={`${icon} ${title}`} />
        <CardContent>
          <Alert severity="error">Fehler beim Laden der Kundendaten: {error.message}</Alert>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card
      sx={{
        mb: 2,
        border: '1px solid',
        borderColor: 'divider',
      }}
    >
      <CardHeader
        title={
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <span>{icon}</span>
            <Typography variant="h6" component="span">
              {title}
            </Typography>
          </Box>
        }
        subheader={subtitle}
        action={
          defaultCollapsed ? (
            <Chip
              label={isCardCollapsed ? 'Erweitern' : 'Einklappen'}
              size="small"
              onClick={() => setIsCardCollapsed(!isCardCollapsed)}
              sx={{ cursor: 'pointer' }}
            />
          ) : null
        }
      />

      {!isCardCollapsed && (
        <CardContent>
          {sections.map(section => {
            const {
              sectionId,
              title: sectionTitle,
              subtitle: sectionSubtitle,
              fields,
              collapsible,
              defaultCollapsed: sectionDefaultCollapsed,
            } = section;

            // Collapsible section
            if (collapsible) {
              return (
                <Accordion
                  key={sectionId}
                  defaultExpanded={!sectionDefaultCollapsed}
                  sx={{ mb: 2, boxShadow: 'none', '&:before': { display: 'none' } }}
                >
                  <AccordionSummary
                    expandIcon={<ExpandMoreIcon />}
                    sx={{
                      backgroundColor: 'action.hover',
                      '&:hover': { backgroundColor: 'action.selected' },
                    }}
                  >
                    <Box>
                      <Typography variant="subtitle1" fontWeight="bold">
                        {sectionTitle}
                      </Typography>
                      {sectionSubtitle && (
                        <Typography variant="caption" color="text.secondary">
                          {sectionSubtitle}
                        </Typography>
                      )}
                    </Box>
                  </AccordionSummary>
                  <AccordionDetails>
                    <Grid container spacing={2}>
                      {fields.map(field => (
                        <DynamicField
                          key={field.fieldKey}
                          field={field}
                          value={getFieldValue(field.fieldKey)}
                          onChange={handleFieldChange}
                          customerId={customerId}
                        />
                      ))}
                    </Grid>
                  </AccordionDetails>
                </Accordion>
              );
            }

            // Non-collapsible section
            return (
              <Box key={sectionId} sx={{ mb: 3 }}>
                <Typography variant="subtitle1" fontWeight="bold" gutterBottom>
                  {sectionTitle}
                </Typography>
                {sectionSubtitle && (
                  <Typography
                    variant="caption"
                    color="text.secondary"
                    display="block"
                    sx={{ mb: 2 }}
                  >
                    {sectionSubtitle}
                  </Typography>
                )}
                <Grid container spacing={2}>
                  {fields.map(field => (
                    <DynamicField
                      key={field.fieldKey}
                      field={field}
                      value={getFieldValue(field.fieldKey)}
                      onChange={handleFieldChange}
                      customerId={customerId}
                    />
                  ))}
                </Grid>
              </Box>
            );
          })}
        </CardContent>
      )}
    </Card>
  );
};
