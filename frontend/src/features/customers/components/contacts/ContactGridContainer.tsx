/**
 * ContactGridContainer Component
 *
 * Manages the responsive grid layout for contact cards with sorting and filtering.
 * Part of FC-005 Contact Management UI - Smart Contact Cards.
 *
 * @see /docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/SMART_CONTACT_CARDS.md
 */

import React, { useState, useMemo } from 'react';
import {
  Box,
  Grid,
  ToggleButtonGroup,
  ToggleButton,
  Typography,
  Stack,
  TextField,
  MenuItem,
  InputAdornment,
  Skeleton,
} from '@mui/material';
import {
  GridView as GridViewIcon,
  ViewList as ListIcon,
  Search as SearchIcon,
  FilterList as _FilterIcon,
} from '@mui/icons-material';

import type { Contact } from '../../types/contact.types';
import { SmartContactCard } from './SmartContactCard';
import { ContactCard } from './ContactCard';
import { EmptyContactState } from './EmptyContactState';
import type { RelationshipWarmth } from './WarmthIndicator';
import { useContactGrid } from '../../hooks/useContactGrid';
import { LazyComponent } from '../../../../components/common/LazyComponent';

export interface ContactAction {
  type:
    | 'add'
    | 'edit'
    | 'delete'
    | 'setPrimary'
    | 'assignLocation'
    | 'viewTimeline'
    | 'quickAction';
  contact?: Contact;
  contactId?: string;
  action?: string;
}

interface ContactGridContainerProps {
  contacts: Contact[];
  warmthData?: Map<string, RelationshipWarmth>;
  onContactAction: (action: ContactAction) => void;
  loading?: boolean;
  viewMode?: 'grid' | 'list';
  showFilters?: boolean;
  useSmartCards?: boolean;
  highlightContactId?: string | null;
  customerId?: string;
}

/**
 * Responsive grid container for contact cards with sorting and view modes
 */
export const ContactGridContainer: React.FC<ContactGridContainerProps> = ({
  contacts,
  warmthData,
  onContactAction,
  loading = false,
  viewMode: initialViewMode = 'grid',
  showFilters = true,
  useSmartCards = true,
  highlightContactId,
  customerId,
}) => {
  const { gridProps: _gridProps } = useContactGrid();
  const [viewMode, setViewMode] = useState<'grid' | 'list'>(initialViewMode);
  const [searchQuery, setSearchQuery] = useState('');
  const [filterDecisionLevel, setFilterDecisionLevel] = useState<string>('all');
  const [sortBy, setSortBy] = useState<'name' | 'warmth' | 'recent'>('name');

  // Filter contacts based on search and filters
  const filteredContacts = useMemo(() => {
    let filtered = [...contacts];

    // Search filter
    if (searchQuery) {
      const query = searchQuery.toLowerCase();
      filtered = filtered.filter(
        contact =>
          contact.firstName.toLowerCase().includes(query) ||
          contact.lastName.toLowerCase().includes(query) ||
          contact.email?.toLowerCase().includes(query) ||
          contact.position?.toLowerCase().includes(query)
      );
    }

    // Decision level filter
    if (filterDecisionLevel !== 'all') {
      filtered = filtered.filter(contact => contact.decisionLevel === filterDecisionLevel);
    }

    return filtered;
  }, [contacts, searchQuery, filterDecisionLevel]);

  // Sort contacts
  const sortedContacts = useMemo(() => {
    const sorted = [...filteredContacts];

    sorted.sort((a, b) => {
      // Primary contacts always first
      if (a.isPrimary && !b.isPrimary) return -1;
      if (!a.isPrimary && b.isPrimary) return 1;

      // Then apply selected sorting
      switch (sortBy) {
        case 'warmth':
          if (warmthData) {
            const warmthA = warmthData.get(a.id);
            const warmthB = warmthData.get(b.id);
            if (warmthA && warmthB) {
              return warmthB.score - warmthA.score;
            }
          }
          break;
        case 'recent': {
          const dateA = new Date(a.updatedAt || a.createdAt).getTime();
          const dateB = new Date(b.updatedAt || b.createdAt).getTime();
          return dateB - dateA;
        }
        case 'name':
        default:
          return `${a.lastName} ${a.firstName}`.localeCompare(`${b.lastName} ${b.firstName}`);
      }

      return 0;
    });

    return sorted;
  }, [filteredContacts, sortBy, warmthData]);

  // Show empty state if no contacts
  if (contacts.length === 0 && !loading) {
    return <EmptyContactState onAddContact={() => onContactAction({ type: 'add' })} />;
  }

  const renderContactCard = (contact: Contact, index: number) => {
    const warmth = warmthData?.get(contact.id);

    // Use lazy loading for cards beyond the first 6 (two rows on desktop)
    const shouldLazyLoad = index >= 6;

    const cardContent = useSmartCards ? (
      <SmartContactCard
        contact={contact}
        warmth={warmth}
        onEdit={c => onContactAction({ type: 'edit', contact: c })}
        onDelete={id => onContactAction({ type: 'delete', contactId: id })}
        onSetPrimary={id => onContactAction({ type: 'setPrimary', contactId: id })}
        onAssignLocation={id => onContactAction({ type: 'assignLocation', contactId: id })}
        onViewTimeline={id => onContactAction({ type: 'viewTimeline', contactId: id })}
        onQuickAction={(action, id) =>
          onContactAction({ type: 'quickAction', contactId: id, action })
        }
        showAuditTrail={true}
        customerId={customerId}
      />
    ) : (
      <ContactCard
        contact={contact}
        onEdit={c => onContactAction({ type: 'edit', contact: c })}
        onDelete={id => onContactAction({ type: 'delete', contactId: id })}
        onSetPrimary={id => onContactAction({ type: 'setPrimary', contactId: id })}
      />
    );

    if (shouldLazyLoad) {
      return (
        <LazyComponent
          minHeight={250}
          threshold={0.1}
          rootMargin="100px"
          placeholder={
            <Box sx={{ p: 2, bgcolor: 'background.paper', borderRadius: 1, height: 250 }}>
              <Skeleton variant="circular" width={56} height={56} />
              <Skeleton variant="text" sx={{ mt: 2 }} />
              <Skeleton variant="text" width="60%" />
            </Box>
          }
        >
          {cardContent}
        </LazyComponent>
      );
    }

    return cardContent;
  };

  return (
    <Box>
      {/* Filters and Controls */}
      {showFilters && (
        <Stack spacing={2} sx={{ mb: 3 }}>
          {/* Search and View Mode */}
          <Box display="flex" justifyContent="space-between" alignItems="center" gap={2}>
            <TextField
              placeholder="Kontakte suchen..."
              value={searchQuery}
              onChange={(e: React.ChangeEvent<HTMLInputElement>) => setSearchQuery(e.target.value)}
              size="small"
              sx={{ flex: 1, maxWidth: 400 }}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon />
                  </InputAdornment>
                ),
              }}
            />

            <ToggleButtonGroup
              value={viewMode}
              exclusive
              onChange={(_, value) => value && setViewMode(value)}
              size="small"
            >
              <ToggleButton value="grid" aria-label="Grid view">
                <GridViewIcon />
              </ToggleButton>
              <ToggleButton value="list" aria-label="List view">
                <ListIcon />
              </ToggleButton>
            </ToggleButtonGroup>
          </Box>

          {/* Filters Row */}
          <Box display="flex" gap={2}>
            <TextField
              select
              label="Entscheidungsebene"
              value={filterDecisionLevel}
              onChange={(e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) =>
                setFilterDecisionLevel(e.target.value)
              }
              size="small"
              sx={{ minWidth: 200 }}
            >
              <MenuItem value="all">Alle Ebenen</MenuItem>
              <MenuItem value="entscheider">Entscheider</MenuItem>
              <MenuItem value="mitentscheider">Mitentscheider</MenuItem>
              <MenuItem value="einflussnehmer">Einflussnehmer</MenuItem>
              <MenuItem value="nutzer">Nutzer</MenuItem>
              <MenuItem value="gatekeeper">Gatekeeper</MenuItem>
            </TextField>

            <TextField
              select
              label="Sortierung"
              value={sortBy}
              onChange={(e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) =>
                setSortBy(e.target.value as 'name' | 'warmth' | 'recent')
              }
              size="small"
              sx={{ minWidth: 150 }}
            >
              <MenuItem value="name">Name</MenuItem>
              {warmthData && <MenuItem value="warmth">Beziehungswärme</MenuItem>}
              <MenuItem value="recent">Zuletzt aktualisiert</MenuItem>
            </TextField>
          </Box>

          {/* Results Count */}
          <Typography variant="body2" color="text.secondary">
            {sortedContacts.length} {sortedContacts.length === 1 ? 'Kontakt' : 'Kontakte'} gefunden
            {searchQuery && ` für "${searchQuery}"`}
          </Typography>
        </Stack>
      )}

      {/* Contact Cards Grid/List */}
      {sortedContacts.length === 0 ? (
        <Box
          sx={{
            textAlign: 'center',
            py: 8,
            bgcolor: 'background.paper',
            borderRadius: 2,
            border: '1px dashed',
            borderColor: 'divider',
          }}
        >
          <Typography variant="h6" gutterBottom>
            Keine Kontakte gefunden
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Versuchen Sie es mit anderen Suchkriterien
          </Typography>
        </Box>
      ) : viewMode === 'grid' ? (
        <Grid container spacing={2}>
          {sortedContacts.map((contact, index) => (
            <Grid
              key={contact.id}
              size={{ xs: 12, sm: 6, md: 4, lg: 3 }}
              id={`contact-${contact.id}`}
              sx={{
                ...(highlightContactId === contact.id && {
                  animation: 'pulse 1s ease-in-out 3',
                  '& > *': {
                    border: '2px solid',
                    borderColor: 'primary.main',
                    boxShadow: theme => `0 0 20px ${theme.palette.primary.main}40`,
                  },
                }),
              }}
            >
              {renderContactCard(contact, index)}
            </Grid>
          ))}
        </Grid>
      ) : (
        <Stack spacing={2}>
          {sortedContacts.map((contact, index) => (
            <Box key={contact.id}>{renderContactCard(contact, index)}</Box>
          ))}
        </Stack>
      )}
    </Box>
  );
};
