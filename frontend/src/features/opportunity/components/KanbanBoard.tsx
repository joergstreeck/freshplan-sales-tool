import React, { useState, useCallback, useMemo } from 'react';
import { DragDropContext, Droppable, Draggable } from '@hello-pangea/dnd';
import type { DropResult } from '@hello-pangea/dnd';
import {
  Box,
  Paper,
  Typography,
  Badge,
  Card,
  CardContent,
  Avatar,
  LinearProgress,
  ToggleButton,
  ToggleButtonGroup,
  Alert,
  Skeleton,
  CircularProgress,
} from '@mui/material';
import { useTheme } from '@mui/material/styles';
import PersonIcon from '@mui/icons-material/Person';
import EuroIcon from '@mui/icons-material/Euro';
import CalendarTodayIcon from '@mui/icons-material/CalendarToday';
import FilterListIcon from '@mui/icons-material/FilterList';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import CancelIcon from '@mui/icons-material/Cancel';

import { OpportunityStage, STAGE_CONFIGS, type Opportunity } from '../types';
import { logger } from '../../../lib/logger';
import { useErrorHandler } from '../../../components/ErrorBoundary';
import { useOpportunities, useChangeOpportunityStage } from '../hooks/useOpportunities';

// Aktive Pipeline Stages (immer sichtbar)
const ACTIVE_STAGES = [
  OpportunityStage.NEW_LEAD,
  OpportunityStage.QUALIFICATION,
  OpportunityStage.NEEDS_ANALYSIS,
  OpportunityStage.PROPOSAL,
  OpportunityStage.NEGOTIATION,
];

// Abgeschlossene Stages (über Filter einblendbar)
const CLOSED_STAGES = [OpportunityStage.CLOSED_WON, OpportunityStage.CLOSED_LOST];

// Type imported from '../types'

// Convert STAGE_CONFIGS array to Record for easier lookup
const STAGE_CONFIGS_RECORD: Record<string, (typeof STAGE_CONFIGS)[0]> = {};
STAGE_CONFIGS.forEach(config => {
  STAGE_CONFIGS_RECORD[config.stage] = config;
});

// Mock data removed - now using real API data via useOpportunities hook

/**
 * Opportunity Card Component
 * @description Renders a draggable opportunity card with details
 * @component
 */
interface OpportunityCardProps {
  /** The opportunity data to display */
  opportunity: Opportunity;
  /** Index for drag and drop ordering */
  index: number;
}

const OpportunityCard: React.FC<OpportunityCardProps> = React.memo(({ opportunity, index }) => {
  const theme = useTheme();

  const getProbabilityColor = useCallback(
    (probability?: number) => {
      if (!probability) return theme.palette.grey[400];
      if (probability >= 80) return '#66BB6A';
      if (probability >= 60) return '#94C456';
      if (probability >= 40) return '#FFA726';
      if (probability >= 20) return '#FF7043';
      return '#EF5350';
    },
    [theme]
  );

  const formatDate = useCallback((dateString?: string) => {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleDateString('de-DE', {
      day: '2-digit',
      month: '2-digit',
      year: '2-digit',
    });
  }, []);

  const formatValue = useCallback((value?: number) => {
    if (!value) return 'Kein Wert';
    return new Intl.NumberFormat('de-DE', {
      style: 'currency',
      currency: 'EUR',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(value);
  }, []);

  return (
    <Draggable draggableId={opportunity.id} index={index}>
      {(provided, snapshot) => (
        <div
          ref={provided.innerRef}
          {...provided.draggableProps}
          {...provided.dragHandleProps}
          style={{
            ...provided.draggableProps.style,
            marginBottom: snapshot.isDragging ? 0 : '12px',
          }}
        >
          <Card
            sx={{
              opacity: snapshot.isDragging ? 0.9 : 1,
              boxShadow: snapshot.isDragging ? theme.shadows[8] : theme.shadows[2],
              transition: snapshot.isDragging ? 'none' : 'box-shadow 0.2s ease',
              border: '1px solid rgba(148, 196, 86, 0.2)',
              cursor: snapshot.isDragging ? 'grabbing' : 'grab',
              '&:hover': {
                boxShadow: theme.shadows[4],
                cursor: 'grab',
              },
              '&:active': {
                cursor: 'grabbing',
              },
            }}
          >
            <CardContent sx={{ pb: '16px !important' }}>
              {/* Opportunity Name */}
              <Typography
                variant="h6"
                sx={{
                  color: '#004F7B',
                  fontFamily: 'Antonio, sans-serif',
                  fontWeight: 600,
                  fontSize: '1rem',
                  mb: 1,
                  lineHeight: 1.3,
                }}
              >
                {opportunity.name}
              </Typography>

              {/* Customer */}
              {opportunity.customerName && (
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                  <PersonIcon fontSize="small" sx={{ color: theme.palette.grey[600], mr: 0.5 }} />
                  <Typography variant="body2" sx={{ color: theme.palette.text.secondary }}>
                    {opportunity.customerName}
                  </Typography>
                </Box>
              )}

              {/* Value */}
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                <EuroIcon fontSize="small" sx={{ color: '#94C456', mr: 0.5 }} />
                <Typography
                  variant="body2"
                  sx={{ color: theme.palette.text.primary, fontWeight: 600 }}
                >
                  {formatValue(opportunity.value)}
                </Typography>
              </Box>

              {/* Probability Progress */}
              {opportunity.probability !== undefined && (
                <Box sx={{ mb: 1.5 }}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.5 }}>
                    <Typography variant="caption" sx={{ color: theme.palette.text.secondary }}>
                      Wahrscheinlichkeit
                    </Typography>
                    <Typography
                      variant="caption"
                      sx={{
                        color: getProbabilityColor(opportunity.probability),
                        fontWeight: 600,
                      }}
                    >
                      {opportunity.probability}%
                    </Typography>
                  </Box>
                  <LinearProgress
                    variant="determinate"
                    value={opportunity.probability}
                    sx={{
                      height: 6,
                      borderRadius: 3,
                      bgcolor: 'rgba(0, 0, 0, 0.08)',
                      '& .MuiLinearProgress-bar': {
                        bgcolor: getProbabilityColor(opportunity.probability),
                        borderRadius: 3,
                      },
                    }}
                  />
                </Box>
              )}

              {/* Footer */}
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                {opportunity.expectedCloseDate && (
                  <Box sx={{ display: 'flex', alignItems: 'center' }}>
                    <CalendarTodayIcon
                      fontSize="small"
                      sx={{ color: theme.palette.grey[500], mr: 0.5 }}
                    />
                    <Typography variant="caption" sx={{ color: theme.palette.text.secondary }}>
                      {formatDate(opportunity.expectedCloseDate)}
                    </Typography>
                  </Box>
                )}

                {opportunity.assignedToName && (
                  <Avatar
                    sx={{
                      width: 24,
                      height: 24,
                      bgcolor: '#94C456',
                      fontSize: '0.75rem',
                      fontWeight: 600,
                    }}
                  >
                    {opportunity.assignedToName.charAt(0).toUpperCase()}
                  </Avatar>
                )}
              </Box>
            </CardContent>
          </Card>
        </div>
      )}
    </Draggable>
  );
});

OpportunityCard.displayName = 'OpportunityCard';

/**
 * Kanban Column Component
 * @description Renders a single pipeline stage column with droppable area
 * @component
 */
interface KanbanColumnProps {
  /** The stage this column represents */
  stage: OpportunityStage;
  /** Opportunities in this stage */
  opportunities: Opportunity[];
}

const KanbanColumn: React.FC<KanbanColumnProps> = React.memo(({ stage, opportunities = [] }) => {
  const theme = useTheme();
  const config = STAGE_CONFIGS_RECORD[stage];
  const totalValue = opportunities?.reduce((sum, opp) => sum + (opp.value || 0), 0) || 0;

  return (
    <Paper
      sx={{
        minHeight: 400,
        flex: '1 1 0',
        minWidth: 280,
        maxWidth: 450,
        p: 2,
        bgcolor: config.bgColor,
        border: '1px solid rgba(0, 0, 0, 0.12)',
        borderRadius: 2,
      }}
    >
      {/* Column Header */}
      <Box sx={{ mb: 2 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 1 }}>
          <Typography
            variant="h6"
            sx={{
              color: config.color,
              fontFamily: 'Antonio, sans-serif',
              fontWeight: 700,
              fontSize: '1.1rem',
            }}
          >
            {config.label}
          </Typography>

          <Badge
            badgeContent={opportunities.length}
            color="primary"
            sx={{
              '& .MuiBadge-badge': {
                bgcolor: config.color,
                color: 'white',
                fontWeight: 600,
              },
            }}
          />
        </Box>

        {totalValue > 0 && (
          <Typography variant="body2" sx={{ color: theme.palette.text.secondary, fontWeight: 500 }}>
            Gesamt:{' '}
            {new Intl.NumberFormat('de-DE', {
              style: 'currency',
              currency: 'EUR',
              minimumFractionDigits: 0,
            }).format(totalValue)}
          </Typography>
        )}
      </Box>

      {/* Droppable Area */}
      <Droppable droppableId={stage}>
        {(provided, snapshot) => (
          <Box
            ref={provided.innerRef}
            {...provided.droppableProps}
            data-testid={`droppable-${stage.toLowerCase()}`}
            sx={{
              minHeight: 300,
              bgcolor: snapshot.isDraggingOver ? 'rgba(148, 196, 86, 0.08)' : 'transparent',
              borderRadius: 1,
              transition: 'background-color 0.2s ease',
            }}
          >
            {opportunities.map((opportunity, index) => (
              <OpportunityCard key={opportunity.id} opportunity={opportunity} index={index} />
            ))}
            {provided.placeholder}
          </Box>
        )}
      </Droppable>
    </Paper>
  );
});

KanbanColumn.displayName = 'KanbanColumn';

// Component logger instance
const componentLogger = logger.child('KanbanBoard');

/**
 * KanbanBoard Component
 * @description Main opportunity pipeline visualization with drag & drop
 * @component
 * @since 2.0.0
 */
export const KanbanBoard: React.FC = React.memo(() => {
  const theme = useTheme();
  const errorHandler = useErrorHandler('KanbanBoard');

  // API Integration
  const { data: opportunities = [], isLoading, error, refetch } = useOpportunities();
  const changeStagemutation = useChangeOpportunityStage();

  const [showClosed, setShowClosed] = useState<boolean>(false);
  const [isDragging, setIsDragging] = useState<boolean>(false);
  const [dragKey, setDragKey] = useState<number>(0); // Force re-render nach Drag

  // Component lifecycle logging
  React.useEffect(() => {
    componentLogger.debug('Component mounted');
    return () => {
      componentLogger.debug('Component unmounted');
    };
  }, []);

  // Drag state monitoring
  React.useEffect(() => {
    componentLogger.debug('Drag state changed', { isDragging });
  }, [isDragging]);

  // Pipeline-Statistiken berechnen (memoized)
  const pipelineStats = useMemo(() => {
    // Alle Opportunities sind bereits geladen, keine Filter nötig
    // Da aktuell alle Opportunities in aktiven Stages sind
    const activeOpps = opportunities.filter(opp => ACTIVE_STAGES.includes(opp.stage));
    const wonOpps = opportunities.filter(opp => opp.stage === OpportunityStage.CLOSED_WON);
    const lostOpps = opportunities.filter(opp => opp.stage === OpportunityStage.CLOSED_LOST);

    const totalClosed = wonOpps.length + lostOpps.length;
    const conversionRate = totalClosed > 0 ? wonOpps.length / totalClosed : 0;
    
    const activeValue = activeOpps.reduce((sum, opp) => sum + (opp.value || 0), 0);

    return {
      totalActive: activeOpps.length,
      totalWon: wonOpps.length,
      totalLost: lostOpps.length,
      activeValue,
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

  const handleDragStart = useCallback(() => {
    try {
      componentLogger.debug('Drag operation started');
      setIsDragging(true);
      // Vibrieren für haptisches Feedback (falls verfügbar)
      if (navigator.vibrate) {
        navigator.vibrate(50);
      }
    } catch (error) {
      componentLogger.error('Error in handleDragStart', { error });
      errorHandler(error as Error);
    }
  }, [errorHandler]);

  const handleDragEnd = useCallback(
    (result: DropResult) => {
      const timer = componentLogger.time('handleDragEnd');

      try {
        componentLogger.debug('Drag operation ended', { result });
        setIsDragging(false);
        const { destination, source, draggableId } = result;

        // Kein gültiges Drop-Target
        if (!destination) {
          componentLogger.debug('No valid drop target');
          return;
        }

        // Gleiche Position
        if (destination.droppableId === source.droppableId && destination.index === source.index) {
          componentLogger.debug('Same position - no change needed');
          return;
        }

        const sourceStage = source.droppableId as OpportunityStage;
        const destStage = destination.droppableId as OpportunityStage;

        // Verhindere Drag von abgeschlossenen zu aktiven Stages
        if (CLOSED_STAGES.includes(sourceStage) && ACTIVE_STAGES.includes(destStage)) {
          componentLogger.warn('Cannot reactivate closed opportunities', {
            sourceStage,
            destStage,
          });
          return;
        }

        // API Call für Stage-Änderung
        changeStagemutation.mutate(
          {
            id: draggableId,
            request: {
              newStage: destStage,
              reason: `Stage changed via drag & drop from ${sourceStage} to ${destStage}`,
              stageChangedAt: new Date().toISOString(),
            },
          },
          {
            onSuccess: updatedOpportunity => {
              componentLogger.info('Opportunity stage updated successfully', {
                opportunityId: draggableId,
                fromStage: sourceStage,
                toStage: destStage,
                opportunityName: updatedOpportunity.name,
              });

              // Force re-render des DragDropContext
              setDragKey(prev => prev + 1);
            },
            onError: error => {
              componentLogger.error('Failed to update opportunity stage', {
                opportunityId: draggableId,
                fromStage: sourceStage,
                toStage: destStage,
                error,
              });

              // Zeige User-friendly Error Message
              errorHandler(
                new Error(
                  `Konnte Opportunity-Stage nicht ändern: ${error.message || 'Unbekannter Fehler'}`
                )
              );

              // Revert optimistic update durch refetch
              refetch();
            },
          }
        );
      } catch (error) {
        componentLogger.error('Error in handleDragEnd', { error });
        errorHandler(error as Error);
      } finally {
        timer();
      }
    },
    [changeStagemutation, refetch, errorHandler]
  );

  // Loading State
  if (isLoading) {
    return (
      <Box sx={{ p: 3 }}>
        <Paper sx={{ p: 2, mb: 2 }}>
          <Skeleton variant="text" width="40%" height={40} />
          <Box sx={{ display: 'flex', gap: 2, mt: 1 }}>
            <Skeleton variant="text" width="15%" />
            <Skeleton variant="text" width="20%" />
          </Box>
        </Paper>
        <Box sx={{ display: 'flex', gap: 2 }}>
          {[1, 2, 3, 4].map(index => (
            <Paper key={index} sx={{ flex: 1, p: 2 }}>
              <Skeleton variant="text" width="60%" height={30} />
              <Skeleton variant="rectangular" width="100%" height={120} sx={{ mt: 1 }} />
              <Skeleton variant="rectangular" width="100%" height={80} sx={{ mt: 1 }} />
            </Paper>
          ))}
        </Box>
      </Box>
    );
  }

  // Error State
  if (error) {
    return (
      <Box sx={{ p: 3 }}>
        <Alert severity="error" action={<button onClick={() => refetch()}>Wiederholen</button>}>
          <Typography variant="h6">Fehler beim Laden der Opportunities</Typography>
          <Typography variant="body2">
            {error instanceof Error
              ? error.message
              : 'Unbekannter Fehler beim Laden der Pipeline-Daten'}
          </Typography>
        </Alert>
      </Box>
    );
  }

  return (
    <Box sx={{ p: 3 }}>
      {/* Status Indikator für laufende Stage-Änderungen */}
      {changeStagemutation.isPending && (
        <Box
          sx={{
            display: 'flex',
            alignItems: 'center',
            mb: 2,
            p: 1,
            bgcolor: 'info.light',
            borderRadius: 1,
          }}
        >
          <CircularProgress size={16} sx={{ mr: 1 }} />
          <Typography variant="body2">Stage-Änderung wird gespeichert...</Typography>
        </Box>
      )}

      {/* Kompakter Header */}
      <Paper
        sx={{
          p: 2,
          mb: 2,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          bgcolor: 'background.paper',
          borderBottom: '2px solid #94C456',
        }}
      >
        {/* Titel */}
        <Typography
          variant="h5"
          sx={{
            color: '#004F7B',
            fontFamily: 'Antonio, sans-serif',
            fontWeight: 700,
          }}
        >
          Verkaufschancen Pipeline
        </Typography>

        {/* Zentrale Metriken */}
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <Typography variant="body1" sx={{ color: '#94C456', fontWeight: 600 }}>
            {pipelineStats.totalActive} Aktive
          </Typography>
          <Typography variant="body1" sx={{ color: theme.palette.text.secondary }}>
            •
          </Typography>
          <Typography variant="body1" sx={{ color: '#004F7B', fontWeight: 600 }}>
            {new Intl.NumberFormat('de-DE', {
              style: 'currency',
              currency: 'EUR',
              minimumFractionDigits: 0,
            }).format(pipelineStats.activeValue)}
          </Typography>
        </Box>

        {/* Filter-Dropdown */}
        <ToggleButtonGroup
          value={showClosed ? 'all' : 'active'}
          exclusive
          onChange={(_, value) => setShowClosed(value === 'all')}
          size="small"
        >
          <ToggleButton value="active" sx={{ px: 2 }}>
            <FilterListIcon sx={{ mr: 0.5, fontSize: 16 }} />
            Aktive
          </ToggleButton>
          <ToggleButton value="all" sx={{ px: 2 }}>
            Alle
          </ToggleButton>
        </ToggleButtonGroup>
      </Paper>

      {/* Minimale Erfolgs-Leiste */}
      {(pipelineStats.totalWon > 0 || pipelineStats.totalLost > 0) && (
        <Box
          sx={{
            display: 'flex',
            alignItems: 'center',
            gap: 2,
            mb: 2,
            p: 1,
            bgcolor: 'rgba(0, 0, 0, 0.02)',
            borderRadius: 1,
            fontSize: '0.875rem',
          }}
        >
          <CheckCircleIcon sx={{ fontSize: 16, color: '#66BB6A' }} />
          <Typography variant="body2" sx={{ color: '#66BB6A' }}>
            {pipelineStats.totalWon} Gewonnen (
            {new Intl.NumberFormat('de-DE', {
              style: 'currency',
              currency: 'EUR',
              minimumFractionDigits: 0,
            }).format(pipelineStats.wonValue)}
            )
          </Typography>

          <Typography variant="body2" sx={{ color: theme.palette.text.secondary }}>
            •
          </Typography>

          <CancelIcon sx={{ fontSize: 16, color: '#EF5350' }} />
          <Typography variant="body2" sx={{ color: '#EF5350' }}>
            {pipelineStats.totalLost} Verloren (
            {new Intl.NumberFormat('de-DE', {
              style: 'currency',
              currency: 'EUR',
              minimumFractionDigits: 0,
            }).format(pipelineStats.lostValue)}
            )
          </Typography>

          <Typography variant="body2" sx={{ color: theme.palette.text.secondary }}>
            •
          </Typography>

          <Typography variant="body2" sx={{ color: '#FFA726', fontWeight: 600 }}>
            {Math.round(pipelineStats.conversionRate * 100)}% Erfolgsquote
          </Typography>
        </Box>
      )}

      {/* Info Alert wenn abgeschlossene angezeigt werden */}
      {showClosed && (
        <Alert severity="info" sx={{ mb: 2 }} onClose={() => setShowClosed(false)}>
          Sie sehen jetzt auch abgeschlossene Verkaufschancen (Gewonnen/Verloren). Diese können
          nicht mehr in aktive Stages verschoben werden.
        </Alert>
      )}

      {/* Kanban Board */}
      <DragDropContext key={dragKey} onDragStart={handleDragStart} onDragEnd={handleDragEnd}>
        <Box sx={{ position: 'relative' }}>
          <Box
            sx={{
              display: 'flex',
              gap: 2,
              pb: 2,
              minHeight: 500,
              // Kein horizontales Scrollen für 4 Spalten
              overflowX: showClosed ? 'auto' : 'visible',
              // Scrollbar-Styling nur wenn benötigt
              ...(showClosed && {
                '&::-webkit-scrollbar': {
                  height: 8,
                },
                '&::-webkit-scrollbar-track': {
                  backgroundColor: 'rgba(0, 0, 0, 0.05)',
                  borderRadius: 4,
                },
                '&::-webkit-scrollbar-thumb': {
                  backgroundColor: '#94C456',
                  borderRadius: 4,
                  '&:hover': {
                    backgroundColor: '#7BA646',
                  },
                },
              }),
            }}
          >
            {/* Zeige nur aktive Stages oder alle je nach Filter */}
            {(showClosed ? Object.values(OpportunityStage) : ACTIVE_STAGES).map(stage => (
              <KanbanColumn key={stage} stage={stage} opportunities={opportunitiesByStage[stage]} />
            ))}
          </Box>

          {/* Quick-Action Drop Zones - nur beim Dragging und wenn nicht alle Stages sichtbar sind */}
          {!showClosed && isDragging && (
            <>
              {/* Gewonnen Drop Zone */}
              <Droppable droppableId={OpportunityStage.CLOSED_WON}>
                {(provided, snapshot) => (
                  <Paper
                    ref={provided.innerRef}
                    {...provided.droppableProps}
                    sx={{
                      position: 'absolute',
                      right: -120,
                      top: '50%',
                      transform: 'translateY(-50%)',
                      width: 100,
                      height: 200,
                      bgcolor: snapshot.isDraggingOver ? '#66BB6A' : 'rgba(102, 187, 106, 0.1)',
                      border: '2px dashed #66BB6A',
                      display: 'flex',
                      flexDirection: 'column',
                      alignItems: 'center',
                      justifyContent: 'center',
                      opacity: snapshot.isDraggingOver ? 1 : 0.5,
                      transition: 'all 0.3s ease',
                      '&:hover': {
                        opacity: 0.8,
                      },
                    }}
                  >
                    <CheckCircleIcon sx={{ fontSize: 40, color: '#66BB6A', mb: 1 }} />
                    <Typography variant="caption" sx={{ color: '#66BB6A', fontWeight: 600 }}>
                      Gewonnen
                    </Typography>
                    {provided.placeholder}
                  </Paper>
                )}
              </Droppable>

              {/* Verloren Drop Zone */}
              <Droppable droppableId={OpportunityStage.CLOSED_LOST}>
                {(provided, snapshot) => (
                  <Paper
                    ref={provided.innerRef}
                    {...provided.droppableProps}
                    sx={{
                      position: 'absolute',
                      right: -120,
                      bottom: 20,
                      width: 100,
                      height: 120,
                      bgcolor: snapshot.isDraggingOver ? '#EF5350' : 'rgba(239, 83, 80, 0.1)',
                      border: '2px dashed #EF5350',
                      display: 'flex',
                      flexDirection: 'column',
                      alignItems: 'center',
                      justifyContent: 'center',
                      opacity: snapshot.isDraggingOver ? 1 : 0.5,
                      transition: 'all 0.3s ease',
                      '&:hover': {
                        opacity: 0.8,
                      },
                    }}
                  >
                    <CancelIcon sx={{ fontSize: 40, color: '#EF5350', mb: 1 }} />
                    <Typography variant="caption" sx={{ color: '#EF5350', fontWeight: 600 }}>
                      Verloren
                    </Typography>
                    {provided.placeholder}
                  </Paper>
                )}
              </Droppable>
            </>
          )}
        </Box>
      </DragDropContext>
    </Box>
  );
});

KanbanBoard.displayName = 'KanbanBoard';
