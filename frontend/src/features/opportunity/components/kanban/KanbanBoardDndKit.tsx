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
import { Box, Paper, Typography, Stack } from '@mui/material';
import { useTheme } from '@mui/material/styles';

import { OpportunityStage, type Opportunity } from '../../types';
import { logger } from '../../../../lib/logger';
import { useErrorHandler } from '../../../../components/ErrorBoundary';
import { httpClient } from '../../../../lib/apiClient';

import { OpportunityCard } from './OpportunityCard';
import { KanbanColumn } from './KanbanColumn';
import { initialOpportunities, ACTIVE_STAGES, CLOSED_STAGES } from './mockData';

const componentLogger = logger.child('KanbanBoardDndKit');

// Drag & Drop Konfiguration
// Diese Werte funktionieren für die meisten Browser und Bildschirmgrößen
// Bei Bedarf können sie über CSS Custom Properties überschrieben werden
const CARD_WIDTH = 280; // Standard-Breite der Opportunity Cards
const DRAG_OFFSET_X = CARD_WIDTH; // Horizontale Korrektur für Cursor-Position

// Vertikaler Offset: Kompensiert Browser-Unterschiede beim Drag-Start
// Chrome/Edge: 30px, Firefox: 25px, Safari: 35px - wir nehmen den Mittelwert
const DRAG_OFFSET_Y = 30; 

// Browser-Detection für feinere Anpassungen (falls nötig)
const isSafari = /^((?!chrome|android).)*safari/i.test(navigator.userAgent);
const isFirefox = navigator.userAgent.toLowerCase().includes('firefox');
const BROWSER_OFFSET_Y = isSafari ? 35 : isFirefox ? 25 : 30;

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
  const [opportunities, setOpportunities] = useState<Opportunity[]>(initialOpportunities);
  const [activeId, setActiveId] = useState<string | null>(null);
  const [animatingIds, setAnimatingIds] = useState<Set<string>>(new Set());

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
        // Keep using initialOpportunities as fallback
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

  // Opportunities nach Stage gruppieren (memoized)
  const opportunitiesByStage = useMemo(() => {
    return Object.values(OpportunityStage).reduce(
      (acc, stage) => {
        acc[stage] = opportunities.filter(opp => opp.stage === stage);
        return acc;
      },
      {} as Record<OpportunityStage, Opportunity[]>
    );
  }, [opportunities]);

  const handleDragStart = useCallback(
    (event: DragStartEvent) => {
      try {
        componentLogger.debug('Drag operation started', { activeId: event.active.id });
        setActiveId(event.active.id as string);
      } catch (error) {
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
        <Typography
          variant="h4"
          sx={{ mb: 3 }}
        >
          Pipeline Übersicht
        </Typography>
        <Stack direction="row" spacing={4} flexWrap="wrap">
          <Box>
            <Typography 
              variant="body2" 
              color="textSecondary"
              sx={{ mb: 0.5, fontWeight: 'medium' }}
            >
              Aktive Opportunities
            </Typography>
            <Typography 
              variant="h3" 
              color="primary"
            >
              {pipelineStats.totalActive}
            </Typography>
            <Typography 
              variant="h6" 
              color="textSecondary"
              sx={{ mt: 0.5 }}
            >
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
            <Typography 
              variant="h3" 
              sx={{ color: theme.palette.status.won }}
            >
              {pipelineStats.totalWon}
            </Typography>
            <Typography 
              variant="h6" 
              color="textSecondary"
              sx={{ mt: 0.5 }}
            >
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
            <Typography 
              variant="h3" 
              sx={{ color: theme.palette.status.lost }}
            >
              {pipelineStats.totalLost}
            </Typography>
            <Typography 
              variant="h6" 
              color="textSecondary"
              sx={{ mt: 0.5 }}
            >
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
            <Typography 
              variant="h3" 
              sx={{ color: theme.palette.primary.main }}
            >
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
          {/* Active Stages */}
          {ACTIVE_STAGES.map(stage => (
            <KanbanColumn
              key={stage}
              stage={stage}
              opportunities={opportunitiesByStage[stage] || []}
              onQuickAction={handleQuickAction}
              animatingIds={animatingIds}
            />
          ))}

          {/* Closed Stages */}
          {CLOSED_STAGES.map(stage => (
            <KanbanColumn
              key={stage}
              stage={stage}
              opportunities={opportunitiesByStage[stage] || []}
              onQuickAction={handleQuickAction}
              animatingIds={animatingIds}
            />
          ))}
        </Box>

        {/* Drag Overlay with proper offset */}
        <DragOverlay
          dropAnimation={{
            duration: 200,
            easing: 'cubic-bezier(0.18, 0.67, 0.6, 1.22)',
          }}
          style={{
            cursor: 'grabbing',
          }}
          modifiers={[
            // Custom modifier für Cursor-Zentrierung
            // WICHTIG: Dieser Offset korrigiert die Position der Drag-Preview
            // sodass die Karte unter dem Cursor zentriert wird.
            // Bei Änderungen der Kartenbreite muss CARD_WIDTH angepasst werden!
            ({ transform }) => ({
              ...transform,
              x: transform.x - DRAG_OFFSET_X, // Horizontaler Offset
              y: transform.y + BROWSER_OFFSET_Y, // Browser-spezifischer vertikaler Offset
            }),
          ]}
        >
          {activeOpportunity ? (
            <OpportunityCard opportunity={activeOpportunity} isDragging />
          ) : null}
        </DragOverlay>
      </DndContext>
    </Box>
  );
});

KanbanBoardDndKit.displayName = 'KanbanBoardDndKit';