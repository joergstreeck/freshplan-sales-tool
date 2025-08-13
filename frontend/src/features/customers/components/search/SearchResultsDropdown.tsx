/**
 * Search Results Dropdown Component
 *
 * Displays universal search results for both customers and contacts
 * in a dropdown overlay beneath the search field.
 *
 * Features:
 * - Highlighting of search terms in results
 * - Visual separation of customer and contact sections
 * - Relevance scoring indicators
 * - Deep-linking support for contacts
 *
 * @module SearchResultsDropdown
 * @since FC-005 PR4
 */

import React, { useMemo } from 'react';
import {
  Paper,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  ListItemButton,
  Typography,
  Divider,
  Box,
  Chip,
  CircularProgress,
  Alert,
  alpha,
  useTheme,
} from '@mui/material';
import {
  Business as CustomerIcon,
  Person as ContactIcon,
  Email as EmailIcon,
  Phone as PhoneIcon,
  Tag as NumberIcon,
  Search as SearchIcon,
  WorkOutline,
  Star as StarIcon,
  LocationOn as LocationIcon,
  BusinessCenter as DepartmentIcon,
} from '@mui/icons-material';

// Field translations for German localization
const FIELD_TRANSLATIONS: Record<string, string> = {
  'companyName': 'Firmenname',
  'tradingName': 'Handelsname',
  'customerNumber': 'Kundennummer',
  'email': 'E-Mail',
  'phone': 'Telefon',
  'mobile': 'Mobil',
  'position': 'Position',
  'department': 'Abteilung',
  'firstName': 'Vorname',
  'lastName': 'Nachname',
  'status': 'Status',
  'location': 'Standort',
  'tags': 'Schlagwörter',
  'notes': 'Notizen',
  'address': 'Adresse',
  'city': 'Stadt',
  'postalCode': 'PLZ',
  'country': 'Land',
};

// Search Result Types
interface ContactSearchResult {
  id: string;
  firstName: string;
  lastName: string;
  email?: string;
  phone?: string;
  position?: string;
  customerId: string;
  customerName: string;
  isPrimary?: boolean;
}

interface CustomerSearchResult {
  id: string;
  companyName: string;
  customerNumber: string;
  status: string;
  contactEmail?: string;
  contactPhone?: string;
  contactCount?: number;
}

interface SearchResults {
  customers: Array<{
    type: 'customer';
    id: string;
    data: CustomerSearchResult;
    relevanceScore: number;
    matchedFields: string[];
  }>;
  contacts: Array<{
    type: 'contact';
    id: string;
    data: ContactSearchResult;
    relevanceScore: number;
    matchedFields: string[];
  }>;
  totalCount: number;
  executionTime: number;
}

interface SearchResultsDropdownProps {
  searchQuery: string;
  searchResults: SearchResults | null;
  isLoading: boolean;
  error: string | null;
  onCustomerClick: (customerId: string) => void;
  onContactClick: (customerId: string, contactId: string) => void;
  onClose: () => void;
  anchorEl?: HTMLElement | null;
}

export const SearchResultsDropdown: React.FC<SearchResultsDropdownProps> = ({
  searchQuery,
  searchResults,
  isLoading,
  error,
  onCustomerClick,
  onContactClick,
  onClose,
}) => {
  const theme = useTheme();

  // Highlight matching text in search results
  const highlightText = useMemo(() => {
    return (text: string, query: string): JSX.Element => {
      if (!text || !query) {
        return <>{text}</>;
      }

      // Escape special regex characters in query
      const escapedQuery = query.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
      const parts = text.split(new RegExp(`(${escapedQuery})`, 'gi'));

      return (
        <>
          {parts.map((part, index) =>
            part.toLowerCase() === query.toLowerCase() ? (
              <Box
                key={index}
                component="mark"
                sx={{
                  backgroundColor: alpha(theme.palette.warning.main, 0.3),
                  color: 'inherit',
                  fontWeight: 600,
                  px: 0.25,
                  borderRadius: 0.5,
                }}
              >
                {part}
              </Box>
            ) : (
              <React.Fragment key={index}>{part}</React.Fragment>
            )
          )}
        </>
      );
    };
  }, [theme]);

  // Get icon based on matched field
  const getMatchIcon = (matchedFields: string[]) => {
    if (matchedFields.includes('email')) return <EmailIcon sx={{ fontSize: 16 }} />;
    if (matchedFields.includes('phone') || matchedFields.includes('mobile'))
      return <PhoneIcon sx={{ fontSize: 16 }} />;
    if (matchedFields.includes('customerNumber')) return <NumberIcon sx={{ fontSize: 16 }} />;
    if (matchedFields.includes('position')) return <WorkOutline sx={{ fontSize: 16 }} />;
    if (matchedFields.includes('department')) return <DepartmentIcon sx={{ fontSize: 16 }} />;
    return null;
  };

  // Don't show dropdown if no search query
  if (!searchQuery || searchQuery.length < 2) {
    return null;
  }

  const hasResults =
    searchResults &&
    searchResults.customers &&
    searchResults.contacts &&
    (searchResults.customers.length > 0 || searchResults.contacts.length > 0);

  return (
    <Paper
      elevation={8}
      sx={{
        position: 'absolute',
        top: '100%',
        left: 0,
        right: 0,
        mt: 1,
        maxHeight: 480,
        overflow: 'auto',
        zIndex: theme.zIndex.modal,
        border: `1px solid ${alpha(theme.palette.primary.main, 0.2)}`,
        '&::-webkit-scrollbar': {
          width: 8,
        },
        '&::-webkit-scrollbar-track': {
          backgroundColor: alpha(theme.palette.primary.main, 0.05),
        },
        '&::-webkit-scrollbar-thumb': {
          backgroundColor: alpha(theme.palette.primary.main, 0.2),
          borderRadius: 4,
        },
      }}
    >
      {/* Loading State */}
      {isLoading && (
        <Box sx={{ p: 3, textAlign: 'center' }}>
          <CircularProgress size={24} />
          <Typography variant="body2" sx={{ mt: 1, color: 'text.secondary' }}>
            Suche läuft...
          </Typography>
        </Box>
      )}

      {/* Error State */}
      {error && !isLoading && (
        <Alert severity="error" sx={{ m: 2 }}>
          {error}
        </Alert>
      )}

      {/* No Results */}
      {!isLoading && !error && !hasResults && (
        <Box sx={{ p: 3, textAlign: 'center' }}>
          <SearchIcon sx={{ fontSize: 48, color: 'text.disabled', mb: 1 }} />
          <Typography variant="body1" gutterBottom>
            Keine Ergebnisse gefunden
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Für "{searchQuery}" wurden keine Kunden oder Kontakte gefunden
          </Typography>
        </Box>
      )}

      {/* Results */}
      {!isLoading && !error && hasResults && (
        <List sx={{ py: 0 }}>
          {/* Customer Results */}
          {searchResults?.customers?.length > 0 && (
            <>
              <ListItem
                sx={{
                  py: 0.5,
                  px: 2,
                  backgroundColor: alpha(theme.palette.primary.main, 0.05),
                  position: 'sticky',
                  top: 0,
                  zIndex: 1,
                  borderBottom: `2px solid ${theme.palette.primary.main}`,
                }}
              >
                <CustomerIcon sx={{ mr: 1, color: 'primary.main' }} />
                <Typography variant="overline" sx={{ fontWeight: 600, color: 'primary.main' }}>
                  Kunden ({searchResults?.customers?.length || 0})
                </Typography>
              </ListItem>

              {searchResults?.customers?.map(result => (
                <ListItemButton
                  key={result.id}
                  onClick={() => {
                    onCustomerClick(result.id);
                    onClose();
                  }}
                  sx={{
                    '&:hover': {
                      backgroundColor: alpha(theme.palette.primary.main, 0.08),
                    },
                  }}
                >
                  <ListItemIcon>
                    <CustomerIcon color="primary" />
                  </ListItemIcon>
                  <ListItemText
                    primary={
                      <span style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                        <span style={{ fontWeight: 500 }}>
                          {highlightText(result.data.companyName, searchQuery)}
                        </span>
                        <Chip
                          icon={<NumberIcon />}
                          label={highlightText(result.data.customerNumber, searchQuery)}
                          size="small"
                          variant="outlined"
                          sx={{ height: 24 }}
                        />
                        {result.relevanceScore >= 90 && (
                          <Chip
                            icon={<StarIcon />}
                            label="Top-Treffer"
                            size="small"
                            color="success"
                            sx={{ height: 24 }}
                          />
                        )}
                        {getMatchIcon(result.matchedFields) && (
                          <span
                            style={{
                              marginLeft: 'auto',
                              display: 'flex',
                              alignItems: 'center',
                              gap: '4px',
                            }}
                          >
                            {getMatchIcon(result.matchedFields)}
                            <span style={{ fontSize: '0.75rem', color: 'rgba(0, 0, 0, 0.6)' }}>
                              Gefunden in: {result.matchedFields
                                .map(field => FIELD_TRANSLATIONS[field] || field)
                                .join(', ')}
                            </span>
                          </span>
                        )}
                      </span>
                    }
                    secondary={
                      <span
                        style={{
                          display: 'flex',
                          alignItems: 'center',
                          gap: '16px',
                          marginTop: '4px',
                        }}
                      >
                        {result.data.contactEmail && (
                          <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                            <EmailIcon sx={{ fontSize: 14, color: 'text.secondary' }} />
                            <span style={{ fontSize: '0.75rem', color: 'rgba(0, 0, 0, 0.6)' }}>
                              {result.data.contactEmail}
                            </span>
                          </span>
                        )}
                        {result.data.contactPhone && (
                          <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                            <PhoneIcon sx={{ fontSize: 14, color: 'text.secondary' }} />
                            <span style={{ fontSize: '0.75rem', color: 'rgba(0, 0, 0, 0.6)' }}>
                              {result.data.contactPhone}
                            </span>
                          </span>
                        )}
                        {result.data.contactCount && result.data.contactCount > 0 && (
                          <span style={{ fontSize: '0.75rem', color: 'rgba(0, 0, 0, 0.6)' }}>
                            {result.data.contactCount} Kontakt
                            {result.data.contactCount > 1 ? 'e' : ''}
                          </span>
                        )}
                      </span>
                    }
                  />
                </ListItemButton>
              ))}
            </>
          )}

          {/* Divider between sections */}
          {searchResults?.customers?.length > 0 && searchResults?.contacts?.length > 0 && (
            <Divider />
          )}

          {/* Contact Results */}
          {searchResults?.contacts?.length > 0 && (
            <>
              <ListItem
                sx={{
                  py: 0.5,
                  px: 2,
                  backgroundColor: alpha(theme.palette.secondary.main, 0.05),
                  position: 'sticky',
                  top: 0,
                  zIndex: 1,
                  borderBottom: `2px solid ${theme.palette.secondary.main}`,
                }}
              >
                <ContactIcon sx={{ mr: 1, color: 'secondary.main' }} />
                <Typography variant="overline" sx={{ fontWeight: 600, color: 'secondary.main' }}>
                  Ansprechpartner ({searchResults?.contacts?.length || 0})
                </Typography>
              </ListItem>

              {searchResults?.contacts?.map(result => (
                <ListItemButton
                  key={result.id}
                  onClick={() => {
                    onContactClick(result.data.customerId, result.id);
                    onClose();
                  }}
                  sx={{
                    '&:hover': {
                      backgroundColor: alpha(theme.palette.primary.main, 0.08),
                    },
                  }}
                >
                  <ListItemIcon>
                    <ContactIcon color="secondary" />
                  </ListItemIcon>
                  <ListItemText
                    primary={
                      <span style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                        <span style={{ fontWeight: 500 }}>
                          {highlightText(
                            `${result.data.firstName} ${result.data.lastName}`,
                            searchQuery
                          )}
                        </span>
                        {result.data.isPrimary && (
                          <Chip
                            icon={<StarIcon />}
                            label="Hauptkontakt"
                            size="small"
                            color="primary"
                            variant="filled"
                            sx={{ height: 24 }}
                          />
                        )}
                        {result.data.position && (
                          <Chip
                            icon={<WorkOutline />}
                            label={highlightText(result.data.position, searchQuery)}
                            size="small"
                            variant="outlined"
                            sx={{ height: 24 }}
                          />
                        )}
                        {result.relevanceScore >= 90 && (
                          <Chip
                            icon={<StarIcon />}
                            label="Top-Treffer"
                            size="small"
                            color="success"
                            sx={{ height: 24 }}
                          />
                        )}
                      </span>
                    }
                    secondary={
                      <span>
                        <span
                          style={{
                            display: 'flex',
                            alignItems: 'center',
                            gap: '8px',
                            marginBottom: '4px',
                          }}
                        >
                          <LocationIcon sx={{ fontSize: 14, color: 'primary.main' }} />
                          <span style={{ fontSize: '0.875rem', fontWeight: 600, color: '#1976d2' }}>
                            bei: {highlightText(result.data.customerName, searchQuery)}
                          </span>
                        </span>
                        <span style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
                          {result.data.email && (
                            <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                              <EmailIcon sx={{ fontSize: 14, color: 'text.secondary' }} />
                              <span style={{ fontSize: '0.75rem', color: 'rgba(0, 0, 0, 0.6)' }}>
                                {highlightText(result.data.email, searchQuery)}
                              </span>
                            </span>
                          )}
                          {result.data.phone && (
                            <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                              <PhoneIcon sx={{ fontSize: 14, color: 'text.secondary' }} />
                              <span style={{ fontSize: '0.75rem', color: 'rgba(0, 0, 0, 0.6)' }}>
                                {highlightText(result.data.phone, searchQuery)}
                              </span>
                            </span>
                          )}
                        </span>
                      </span>
                    }
                  />
                </ListItemButton>
              ))}
            </>
          )}

          {/* Search Metadata Footer */}
          {searchResults && (
            <ListItem
              sx={{
                py: 1,
                px: 2,
                backgroundColor: alpha(theme.palette.action.hover, 0.05),
                borderTop: `1px solid ${theme.palette.divider}`,
              }}
            >
              <Typography variant="caption" color="text.secondary">
                {searchResults.totalCount} Ergebnis{searchResults.totalCount !== 1 ? 'se' : ''} in{' '}
                {searchResults.executionTime}ms
              </Typography>
            </ListItem>
          )}
        </List>
      )}
    </Paper>
  );
};
