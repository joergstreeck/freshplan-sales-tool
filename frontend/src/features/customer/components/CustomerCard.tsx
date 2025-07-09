import React from 'react';
import {
  Card,
  CardContent,
  CardActions,
  Typography,
  Chip,
  Box,
  IconButton,
  Tooltip,
} from '@mui/material';
import {
  CalendarToday as CalendarIcon,
  Euro as EuroIcon,
  Email as EmailIcon,
  Phone as PhoneIcon,
  Warning as WarningIcon,
} from '@mui/icons-material';
import { formatCurrency, daysSince } from '../../../utils/formatters';
import type { Customer } from '../hooks/useCustomerSearch';

interface CustomerCardProps {
  customer: Customer;
  onClick: (customer: Customer) => void;
}

const statusColors: Record<string, string> = {
  LEAD: '#004F7B', // Freshfoodz Blau
  AKTIV: '#94C456', // Freshfoodz Grün
  INAKTIV: '#999',
  GESPERRT: '#F44336',
};

const statusLabels: Record<string, string> = {
  LEAD: 'Lead',
  AKTIV: 'Aktiv',
  INAKTIV: 'Inaktiv',
  GESPERRT: 'Gesperrt',
};

const getRiskColor = (score: number): string => {
  if (score >= 70) return '#F44336'; // Rot
  if (score >= 40) return '#FF9800'; // Orange
  return '#4CAF50'; // Grün
};

export const CustomerCard: React.FC<CustomerCardProps> = ({ customer, onClick }) => {
  const daysSinceContact = customer.lastContactDate
    ? daysSince(new Date(customer.lastContactDate))
    : null;
  
  const needsAttention = daysSinceContact && daysSinceContact > 30;

  return (
    <Card
      sx={{
        cursor: 'pointer',
        transition: 'all 0.2s',
        '&:hover': {
          transform: 'translateY(-2px)',
          boxShadow: 3,
        },
        height: '100%',
        display: 'flex',
        flexDirection: 'column',
      }}
      onClick={() => onClick(customer)}
    >
      <CardContent sx={{ flex: 1 }}>
        {/* Header */}
        <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
          <Box>
            <Typography variant="h6" component="h3" gutterBottom>
              {customer.companyName}
            </Typography>
            {customer.tradingName && (
              <Typography variant="body2" color="text.secondary">
                {customer.tradingName}
              </Typography>
            )}
            <Typography variant="caption" color="text.secondary">
              Kd.-Nr. {customer.customerNumber}
            </Typography>
          </Box>
          <Box sx={{ textAlign: 'right' }}>
            <Chip
              label={statusLabels[customer.status]}
              size="small"
              sx={{
                backgroundColor: statusColors[customer.status],
                color: '#fff',
              }}
            />
            {needsAttention && (
              <Tooltip title="Lange kein Kontakt">
                <WarningIcon color="warning" sx={{ ml: 1 }} />
              </Tooltip>
            )}
          </Box>
        </Box>

        {/* Risk Score */}
        <Box sx={{ mb: 2 }}>
          <Typography variant="body2" color="text.secondary" gutterBottom>
            Risiko-Score
          </Typography>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Box
              sx={{
                width: '100%',
                height: 8,
                backgroundColor: '#e0e0e0',
                borderRadius: 4,
                overflow: 'hidden',
              }}
            >
              <Box
                sx={{
                  width: `${customer.riskScore}%`,
                  height: '100%',
                  backgroundColor: getRiskColor(customer.riskScore),
                  transition: 'width 0.3s',
                }}
              />
            </Box>
            <Typography
              variant="body2"
              sx={{ 
                minWidth: 40, 
                color: getRiskColor(customer.riskScore),
                fontWeight: 'bold',
              }}
            >
              {customer.riskScore}%
            </Typography>
          </Box>
        </Box>

        {/* Meta Information */}
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
          {customer.industry && (
            <Typography variant="body2" color="text.secondary">
              Branche: {customer.industry}
            </Typography>
          )}
          
          {daysSinceContact !== null && (
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
              <CalendarIcon fontSize="small" color="action" />
              <Typography 
                variant="body2" 
                color={needsAttention ? 'error' : 'text.secondary'}
              >
                Letzter Kontakt: vor {daysSinceContact} Tagen
              </Typography>
            </Box>
          )}
          
          {customer.expectedAnnualVolume && (
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
              <EuroIcon fontSize="small" color="action" />
              <Typography variant="body2" color="text.secondary">
                Jahresumsatz: {formatCurrency(customer.expectedAnnualVolume)}
              </Typography>
            </Box>
          )}
        </Box>

        {/* Tags */}
        {customer.tags && customer.tags.length > 0 && (
          <Box sx={{ mt: 2, display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
            {customer.tags.map((tag) => (
              <Chip
                key={tag}
                label={tag}
                size="small"
                variant="outlined"
                sx={{ fontSize: '0.75rem' }}
              />
            ))}
          </Box>
        )}
      </CardContent>

      {/* Quick Actions */}
      <CardActions sx={{ justifyContent: 'space-between', px: 2, pb: 2 }}>
        <Box>
          {customer.assignedTo && (
            <Typography variant="caption" color="text.secondary">
              Betreuer: {customer.assignedTo}
            </Typography>
          )}
        </Box>
        <Box>
          <Tooltip title="E-Mail senden">
            <IconButton 
              size="small" 
              onClick={(e) => {
                e.stopPropagation();
                // TODO: E-Mail-Funktion implementieren
              }}
            >
              <EmailIcon fontSize="small" />
            </IconButton>
          </Tooltip>
          <Tooltip title="Anrufen">
            <IconButton 
              size="small"
              onClick={(e) => {
                e.stopPropagation();
                // TODO: Anruf-Funktion implementieren
              }}
            >
              <PhoneIcon fontSize="small" />
            </IconButton>
          </Tooltip>
        </Box>
      </CardActions>
    </Card>
  );
};