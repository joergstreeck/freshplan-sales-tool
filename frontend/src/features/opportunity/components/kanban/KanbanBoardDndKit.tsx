import React, { useState, useCallback, useMemo, useEffect } from 'react';
import {
  DndContext,
  KeyboardSensor,
  PointerSensor,
  useSensor,
  useSensors,
  DragOverlay,
  closestCenter,
} from '@dnd-kit/core';
import type { DragEndEvent, DragStartEvent } from '@dnd-kit/core';
import { sortableKeyboardCoordinates } from '@dnd-kit/sortable';
import { snapCenterToCursor } from '@dnd-kit/modifiers';
import {
  Box,
  Paper,
  Typography,
  Stack,
  ToggleButtonGroup,
  ToggleButton,
  TextField,
  InputAdornment,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Divider,
} from '@mui/material';
import { useTheme } from '@mui/material/styles';
import SearchIcon from '@mui/icons-material/Search';
import toast from 'react-hot-toast';

import { OpportunityStage, type Opportunity } from '../../types';
import { logger } from '../../../../lib/logger';
import { useErrorHandler } from '../../../../components/ErrorBoundary';
import { httpClient } from '../../../../lib/apiClient';

import { OpportunityCard } from './OpportunityCard';
import { KanbanColumn } from './KanbanColumn';

// Stage constants - should be fetched from backend config in production
const ACTIVE_STAGES = [
  OpportunityStage.NEW_LEAD,
  OpportunityStage.QUALIFICATION,
  OpportunityStage.NEEDS_ANALYSIS,
  OpportunityStage.PROPOSAL,
  OpportunityStage.NEGOTIATION,
];

const CLOSED_STAGES = [OpportunityStage.CLOSED_WON, OpportunityStage.CLOSED_LOST];

const componentLogger = logger.child('KanbanBoardDndKit');

interface PipelineStats {
  totalActive: number;
  totalWon: number;
  totalLost: number;
  activeValue: number;
  wonValue: number;
  lostValue: number;
  conversionRate: number;
}

export const KanbanBoardDndKit: React.FC = React.memo(() => {
  const theme = useTheme();
  const errorHandler = useErrorHandler('KanbanBoardDndKit');
  const [opportunities, setOpportunities] = useState<Opportunity[]>([]);
  const [activeId, setActiveId] = useState<string | null>(null);
  const [animatingIds, setAnimatingIds] = useState<Set<string>>(new Set());

  // Feature 1: Status Filter (Sprint 2.1.7.1)
  const [statusFilter, setStatusFilter] = useState<'active' | 'closed' | 'all'>('active');

  // Feature 2: Benutzer-Filter (Sprint 2.1.7.1 - Manager View)
  const [selectedUserId, setSelectedUserId] = useState<string>('all');

  // Feature 3: Quick-Search (Sprint 2.1.7.1)
  const [searchQuery, setSearchQuery] = useState('');

  // TODO: Get from Auth Context in Sprint 2.1.7.2
  // Dummy current user for demo
  const currentUser = {
    id: 'current-user-123',
    name: 'Max Manager',
    role: 'MANAGER', // or 'USER' for sales rep
  };

  // TODO: Get from API in Sprint 2.1.7.2
  // Dummy team members for demo
  const teamMembers = [
    { id: 'current-user-123', name: 'Max Manager' },
    { id: 'user-1', name: 'Anna Schmidt' },
    { id: 'user-2', name: 'Peter Meier' },
    { id: 'user-3', name: 'Sarah Wagner' },
  ];

  // Load opportunities from API on component mount
  useEffect(() => {
    const loadOpportunities = async () => {
      try {
        const response = await httpClient.get<Opportunity[]>('/api/opportunities?size=100');

        if (response.data && response.data.length > 0) {
          // Transform API data to match our Opportunity interface
          const apiOpportunities = response.data.map((opp: Record<string, unknown>) => ({
            ...opp,
            // Map expectedValue to value if value is null
            value: opp.value || opp.expectedValue || 0,
            // Ensure all required fields are present
            contactName: opp.contactName || 'Unbekannt',
            assignedToName: opp.assignedToName || 'Nicht zugewiesen',
            probability: opp.probability || 0,
            createdAt: opp.createdAt || new Date().toISOString(),
            updatedAt: opp.updatedAt || new Date().toISOString(),
          }));

          setOpportunities(apiOpportunities);
        }
      } catch {
        // Fallback to empty array if API fails
        // Error is silently handled - opportunities will remain as initial mock data
      }
    };

    loadOpportunities();
  }, []);

  // Configure sensors with cursor offset fix
  const sensors = useSensors(
    useSensor(PointerSensor, {
      activationConstraint: {
        distance: 3,
        delay: 0,
        tolerance: 5,
      },
    }),
    useSensor(KeyboardSensor, {
      coordinateGetter: sortableKeyboardCoordinates,
    })
  );

  // Pipeline-Statistiken berechnen (memoized)
  const pipelineStats = useMemo<PipelineStats>(() => {
    const activeOpps = opportunities.filter(opp => ACTIVE_STAGES.includes(opp.stage));
    const wonOpps = opportunities.filter(opp => opp.stage === OpportunityStage.CLOSED_WON);
    const lostOpps = opportunities.filter(opp => opp.stage === OpportunityStage.CLOSED_LOST);

    const totalClosed = wonOpps.length + lostOpps.length;
    const conversionRate = totalClosed > 0 ? wonOpps.length / totalClosed : 0;

    return {
      totalActive: activeOpps.length,
      totalWon: wonOpps.length,
      totalLost: lostOpps.length,
      activeValue: activeOpps.reduce((sum, opp) => sum + (opp.value || 0), 0),
      wonValue: wonOpps.reduce((sum, opp) => sum + (opp.value || 0), 0),
      lostValue: lostOpps.reduce((sum, opp) => sum + (opp.value || 0), 0),
      conversionRate,
    };
  }, [opportunities]);

  // Filter opportunities (Sprint 2.1.7.1)
  const filteredOpportunities = useMemo(() => {
    let filtered = opportunities;

    // Feature 2: Benutzer-Filter (Manager View)
    // Only filter by user if not "all" selected
    if (selectedUserId !== 'all') {
      // TODO: Replace with real assignedToUserId field when backend is ready
      // For now, match by assignedToName (placeholder logic)
      const selectedUser = teamMembers.find(u => u.id === selectedUserId);
      if (selectedUser) {
        filtered = filtered.filter(opp =>
          opp.assignedToName && opp.assignedToName.includes(selectedUser.name)
        );
      }
    }

    // Feature 3: Quick-Search
    if (searchQuery.trim()) {
      const query = searchQuery.toLowerCase();
      filtered = filtered.filter(
        opp =>
          opp.name.toLowerCase().includes(query) ||
          (opp.customerName && opp.customerName.toLowerCase().includes(query)) ||
          (opp.leadCompanyName && opp.leadCompanyName.toLowerCase().includes(query))
      );
    }

    return filtered;
  }, [opportunities, selectedUserId, searchQuery, teamMembers]);

  // Determine visible stages based on status filter (Sprint 2.1.7.1)
  const visibleStages = useMemo(() => {
    switch (statusFilter) {
      case 'active':
        return ACTIVE_STAGES;
      case 'closed':
        return CLOSED_STAGES;
      case 'all':
        return [...ACTIVE_STAGES, ...CLOSED_STAGES];
      default:
        return ACTIVE_STAGES;
    }
  }, [statusFilter]);

  // Opportunities nach Stage gruppieren (memoized) - mit Filter
  const opportunitiesByStage = useMemo(() => {
    return Object.values(OpportunityStage).reduce(
      (acc, stage) => {
        acc[stage] = filteredOpportunities.filter(opp => opp.stage === stage);
        return acc;
      },
      {} as Record<OpportunityStage, Opportunity[]>
    );
  }, [filteredOpportunities]);

  const handleDragStart = useCallback(
    (event: DragStartEvent) => {
      try {
        console.log('[KANBAN DRAG START] 🚀', { activeId: event.active.id });
        componentLogger.debug('Drag operation started', { activeId: event.active.id });
        setActiveId(event.active.id as string);
      } catch (error) {
        console.error('[KANBAN DRAG START ERROR]', error);
        componentLogger.error('Error in handleDragStart', { error });
        errorHandler(error as Error);
      }
    },
    [errorHandler]
  );

  const handleDragEnd = useCallback(
    (event: DragEndEvent) => {
      componentLogger.debug('handleDragEnd called');

      try {
        const { active, over } = event;

        if (!over || active.id === over.id) {
          setActiveId(null);
          return;
        }

        const sourceStage = opportunities.find(o => o.id === active.id)?.stage;
        const targetStage = over.id as OpportunityStage;

        if (!sourceStage || !targetStage || sourceStage === targetStage) {
          setActiveId(null);
          return;
        }

        // VALIDATION: CLOSED_* können nicht verschoben werden
        // → Reaktivierung nur über "Reaktivieren"-Button in Card
        if (
          sourceStage === OpportunityStage.CLOSED_WON ||
          sourceStage === OpportunityStage.CLOSED_LOST
        ) {
          toast.error('Geschlossene Opportunities können nicht verschoben werden', {
            duration: 3000,
            icon: '🔒',
          });
          setActiveId(null);
          componentLogger.warn('Blocked drag from closed stage', {
            opportunityId: active.id,
            fromStage: sourceStage,
            toStage: targetStage,
          });
          return;
        }

        // TODO: Future validation (Sprint 2.1.7.2+):
        // - Max 1 Stage rückwärts
        // - Confirmation Dialog bei großen Sprüngen
        // - Reason-Field bei Rückwärts
        // → Erst nach User-Feedback implementieren

        // Add animation effect
        setAnimatingIds(prev => new Set([...prev, active.id as string]));

        // Update opportunity stage
        setOpportunities(prev =>
          prev.map(opp => {
            if (opp.id === active.id) {
              const updatedOpp = {
                ...opp,
                stage: targetStage,
                updatedAt: new Date().toISOString(),
              };

              // Update probability based on stage
              switch (targetStage) {
                case OpportunityStage.NEW_LEAD:
                  updatedOpp.probability = 10;
                  break;
                case OpportunityStage.QUALIFICATION:
                  updatedOpp.probability = 25;
                  break;
                case OpportunityStage.NEEDS_ANALYSIS:
                  updatedOpp.probability = 40;
                  break;
                case OpportunityStage.PROPOSAL:
                  updatedOpp.probability = 60;
                  break;
                case OpportunityStage.NEGOTIATION:
                  updatedOpp.probability = 80;
                  break;
                case OpportunityStage.CLOSED_WON:
                  updatedOpp.probability = 100;
                  break;
                case OpportunityStage.CLOSED_LOST:
                  updatedOpp.probability = 0;
                  break;
              }

              componentLogger.info('Opportunity stage updated', {
                opportunityId: opp.id,
                fromStage: sourceStage,
                toStage: targetStage,
                newProbability: updatedOpp.probability,
              });

              return updatedOpp;
            }
            return opp;
          })
        );

        // Remove animation after delay
        setTimeout(() => {
          setAnimatingIds(prev => {
            const newSet = new Set(prev);
            newSet.delete(active.id as string);
            return newSet;
          });
        }, 300);

        setActiveId(null);
      } catch (error) {
        componentLogger.error('Error in handleDragEnd', { error });
        errorHandler(error as Error);
        setActiveId(null);
      }
    },
    [opportunities, errorHandler]
  );

  const handleQuickAction = useCallback(
    (opportunityId: string, action: 'won' | 'lost' | 'reactivate') => {
      try {
        setAnimatingIds(prev => new Set([...prev, opportunityId]));

        setOpportunities(prev =>
          prev.map(opp => {
            if (opp.id === opportunityId) {
              let newStage = opp.stage;
              let newProbability = opp.probability;

              switch (action) {
                case 'won':
                  newStage = OpportunityStage.CLOSED_WON;
                  newProbability = 100;
                  break;
                case 'lost':
                  newStage = OpportunityStage.CLOSED_LOST;
                  newProbability = 0;
                  break;
                case 'reactivate':
                  newStage = OpportunityStage.QUALIFICATION;
                  newProbability = 25;
                  break;
              }

              componentLogger.info('Quick action executed', {
                opportunityId,
                action,
                newStage,
                newProbability,
              });

              return {
                ...opp,
                stage: newStage,
                probability: newProbability,
                updatedAt: new Date().toISOString(),
              };
            }
            return opp;
          })
        );

        setTimeout(() => {
          setAnimatingIds(prev => {
            const newSet = new Set(prev);
            newSet.delete(opportunityId);
            return newSet;
          });
        }, 300);
      } catch (error) {
        componentLogger.error('Error in handleQuickAction', { error });
        errorHandler(error as Error);
      }
    },
    [errorHandler]
  );

  const activeOpportunity = useMemo(
    () => opportunities.find(o => o.id === activeId),
    [activeId, opportunities]
  );

  const formatCurrency = useCallback((value: number) => {
    return new Intl.NumberFormat('de-DE', {
      style: 'currency',
      currency: 'EUR',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(value);
  }, []);

  return (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column', p: 2 }}>
      {/* Pipeline Statistics Header */}
      <Paper sx={{ p: 3, mb: 2, bgcolor: theme.palette.background.default, borderRadius: 2 }}>
        {/* Header Row mit Title + Filter Controls */}
        <Stack direction="row" spacing={2} alignItems="center" sx={{ mb: 3 }}>
          <Typography variant="h4">
            Pipeline Übersicht
          </Typography>

          <Box sx={{ flexGrow: 1 }} />

          {/* Feature 3: Quick-Search */}
          <TextField
            placeholder="Suche nach Name oder Kunde..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            size="small"
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon fontSize="small" />
                </InputAdornment>
              ),
            }}
            sx={{ width: 300 }}
          />

          {/* Feature 2: Benutzer-Filter (Manager View) */}
          {currentUser.role === 'MANAGER' && (
            <FormControl size="small" sx={{ minWidth: 200 }}>
              <InputLabel>Verkäufer</InputLabel>
              <Select
                value={selectedUserId}
                onChange={(e) => setSelectedUserId(e.target.value)}
                label="Verkäufer"
              >
                <MenuItem value="all">
                  <strong>Alle Verkäufer</strong>
                </MenuItem>
                <Divider />
                {teamMembers.map((member) => (
                  <MenuItem key={member.id} value={member.id}>
                    {member.name}
                    {member.id === currentUser.id && ' (ich)'}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          )}

          {/* Feature 1: Status Filter */}
          <ToggleButtonGroup
            value={statusFilter}
            exclusive
            onChange={(e, value) => value && setStatusFilter(value)}
            size="small"
          >
            <ToggleButton value="active">
              🔥 Aktive ({pipelineStats.totalActive})
            </ToggleButton>
            <ToggleButton value="closed">
              📦 Geschlossene ({pipelineStats.totalWon + pipelineStats.totalLost})
            </ToggleButton>
            <ToggleButton value="all">
              📊 Alle ({opportunities.length})
            </ToggleButton>
          </ToggleButtonGroup>
        </Stack>
        <Stack direction="row" spacing={4} flexWrap="wrap">
          <Box>
            <Typography
              variant="body2"
              color="textSecondary"
              sx={{ mb: 0.5, fontWeight: 'medium' }}
            >
              Aktive Opportunities
            </Typography>
            <Typography variant="h3" color="primary">
              {pipelineStats.totalActive}
            </Typography>
            <Typography variant="h6" color="textSecondary" sx={{ mt: 0.5 }}>
              {formatCurrency(pipelineStats.activeValue)}
            </Typography>
          </Box>
          <Box>
            <Typography
              variant="body2"
              color="textSecondary"
              sx={{ mb: 0.5, fontWeight: 'medium' }}
            >
              Gewonnen
            </Typography>
            <Typography variant="h3" sx={{ color: theme.palette.status.won }}>
              {pipelineStats.totalWon}
            </Typography>
            <Typography variant="h6" color="textSecondary" sx={{ mt: 0.5 }}>
              {formatCurrency(pipelineStats.wonValue)}
            </Typography>
          </Box>
          <Box>
            <Typography
              variant="body2"
              color="textSecondary"
              sx={{ mb: 0.5, fontWeight: 'medium' }}
            >
              Verloren
            </Typography>
            <Typography variant="h3" sx={{ color: theme.palette.status.lost }}>
              {pipelineStats.totalLost}
            </Typography>
            <Typography variant="h6" color="textSecondary" sx={{ mt: 0.5 }}>
              {formatCurrency(pipelineStats.lostValue)}
            </Typography>
          </Box>
          <Box>
            <Typography
              variant="body2"
              color="textSecondary"
              sx={{ mb: 0.5, fontWeight: 'medium' }}
            >
              Conversion Rate
            </Typography>
            <Typography variant="h3" sx={{ color: theme.palette.primary.main }}>
              {(pipelineStats.conversionRate * 100).toFixed(0)}%
            </Typography>
          </Box>
        </Stack>
      </Paper>

      {/* Kanban Board */}
      <DndContext
        sensors={sensors}
        collisionDetection={closestCenter}
        onDragStart={handleDragStart}
        onDragEnd={handleDragEnd}
      >
        <Box
          sx={{
            display: 'flex',
            gap: 2,
            overflowX: 'auto',
            flex: 1,
            pb: 2,
            minHeight: 0,
            '&::-webkit-scrollbar': { height: 8 },
            '&::-webkit-scrollbar-track': { bgcolor: 'grey.200', borderRadius: 4 },
            '&::-webkit-scrollbar-thumb': { bgcolor: 'grey.400', borderRadius: 4 },
          }}
        >
          {/* Stages based on Status Filter (Sprint 2.1.7.1) */}
          {visibleStages.map(stage => (
            <KanbanColumn
              key={stage}
              stage={stage}
              opportunities={opportunitiesByStage[stage] || []}
              onQuickAction={handleQuickAction}
              animatingIds={animatingIds}
            />
          ))}
        </Box>

        {/* Drag Overlay - Sprint 2.1.7.1: snapCenterToCursor = NO OFFSET! */}
        <DragOverlay
          dropAnimation={null}
          modifiers={[snapCenterToCursor]}
        >
          {activeOpportunity && (
            <Box
              sx={{
                cursor: 'grabbing',
                boxShadow: 8,
              }}
            >
              <OpportunityCard opportunity={activeOpportunity} isDragging />
            </Box>
          )}
        </DragOverlay>
      </DndContext>
    </Box>
  );
});

KanbanBoardDndKit.displayName = 'KanbanBoardDndKit';
