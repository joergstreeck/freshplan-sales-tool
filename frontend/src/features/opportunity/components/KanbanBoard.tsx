import React, { useState } from 'react';
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
} from '@mui/material';
import { useTheme } from '@mui/material/styles';
import PersonIcon from '@mui/icons-material/Person';
import EuroIcon from '@mui/icons-material/Euro';
import CalendarTodayIcon from '@mui/icons-material/CalendarToday';
import FilterListIcon from '@mui/icons-material/FilterList';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import CancelIcon from '@mui/icons-material/Cancel';

import { OpportunityStage } from '../types/stages';


// Aktive Pipeline Stages (immer sichtbar)
const ACTIVE_STAGES = [
  OpportunityStage.LEAD,
  OpportunityStage.QUALIFIED,
  OpportunityStage.PROPOSAL,
  OpportunityStage.NEGOTIATION,
];

// Abgeschlossene Stages (über Filter einblendbar)
const CLOSED_STAGES = [
  OpportunityStage.CLOSED_WON,
  OpportunityStage.CLOSED_LOST,
];

interface Opportunity {
  id: string;
  name: string;
  stage: OpportunityStage;
  value?: number;
  probability?: number;
  customerName?: string;
  assignedToName?: string;
  expectedCloseDate?: string;
  createdAt: string;
  updatedAt: string;
}

interface StageConfig {
  stage: OpportunityStage;
  label: string;
  color: string;
  bgColor: string;
}

// Freshfoodz Corporate Identity Stage-Farben
const STAGE_CONFIGS: Record<OpportunityStage, StageConfig> = {
  [OpportunityStage.LEAD]: {
    stage: OpportunityStage.LEAD,
    label: 'Lead',
    color: '#004F7B',
    bgColor: 'rgba(0, 79, 123, 0.05)'
  },
  [OpportunityStage.QUALIFIED]: {
    stage: OpportunityStage.QUALIFIED,
    label: 'Qualifiziert',
    color: '#94C456',
    bgColor: 'rgba(148, 196, 86, 0.05)'
  },
  [OpportunityStage.PROPOSAL]: {
    stage: OpportunityStage.PROPOSAL,
    label: 'Angebot',
    color: '#FFA726',
    bgColor: 'rgba(255, 167, 38, 0.05)'
  },
  [OpportunityStage.NEGOTIATION]: {
    stage: OpportunityStage.NEGOTIATION,
    label: 'Verhandlung',
    color: '#FF7043',
    bgColor: 'rgba(255, 112, 67, 0.05)'
  },
  [OpportunityStage.CLOSED_WON]: {
    stage: OpportunityStage.CLOSED_WON,
    label: 'Gewonnen',
    color: '#66BB6A',
    bgColor: 'rgba(102, 187, 106, 0.05)'
  },
  [OpportunityStage.CLOSED_LOST]: {
    stage: OpportunityStage.CLOSED_LOST,
    label: 'Verloren',
    color: '#EF5350',
    bgColor: 'rgba(239, 83, 80, 0.05)'
  }
};

// Mock-Daten
const initialOpportunities: Opportunity[] = [
  {
    id: '1',
    name: 'Großauftrag Restaurant Schmidt',
    stage: OpportunityStage.LEAD,
    value: 15000,
    probability: 20,
    customerName: 'Restaurant Schmidt GmbH',
    assignedToName: 'Max Mustermann',
    expectedCloseDate: '2025-08-15',
    createdAt: '2025-07-01T10:00:00Z',
    updatedAt: '2025-07-20T15:30:00Z',
  },
  {
    id: '2', 
    name: 'Hotel Adler - Wocheneinkauf',
    stage: OpportunityStage.QUALIFIED,
    value: 8500,
    probability: 60,
    customerName: 'Hotel Adler',
    assignedToName: 'Anna Weber',
    expectedCloseDate: '2025-07-30',
    createdAt: '2025-07-05T09:15:00Z',
    updatedAt: '2025-07-22T11:45:00Z',
  },
  {
    id: '3',
    name: 'Catering Müller - Event-Paket',
    stage: OpportunityStage.PROPOSAL,
    value: 5200,
    probability: 80,
    customerName: 'Catering Müller',
    assignedToName: 'Tom Fischer',
    expectedCloseDate: '2025-08-01',
    createdAt: '2025-07-10T14:20:00Z',
    updatedAt: '2025-07-23T09:10:00Z',
  },
  {
    id: '4',
    name: 'Bistro Sonne - Großbestellung',
    stage: OpportunityStage.CLOSED_WON,
    value: 12000,
    probability: 100,
    customerName: 'Bistro Sonne',
    assignedToName: 'Max Mustermann',
    expectedCloseDate: '2025-07-15',
    createdAt: '2025-06-20T10:00:00Z',
    updatedAt: '2025-07-15T16:00:00Z',
  },
  {
    id: '5',
    name: 'Kantine Nord - Testbestellung',
    stage: OpportunityStage.CLOSED_LOST,
    value: 3000,
    probability: 0,
    customerName: 'Kantine Nord GmbH',
    assignedToName: 'Anna Weber',
    expectedCloseDate: '2025-07-10',
    createdAt: '2025-06-25T11:00:00Z',
    updatedAt: '2025-07-10T14:30:00Z',
  }
];

// Opportunity Card Komponente
interface OpportunityCardProps {
  opportunity: Opportunity;
  index: number;
}

const OpportunityCard: React.FC<OpportunityCardProps> = ({ opportunity, index }) => {
  const theme = useTheme();

  const getProbabilityColor = (probability?: number) => {
    if (!probability) return theme.palette.grey[400];
    if (probability >= 80) return '#66BB6A';
    if (probability >= 60) return '#94C456';
    if (probability >= 40) return '#FFA726';
    if (probability >= 20) return '#FF7043';
    return '#EF5350';
  };

  const formatDate = (dateString?: string) => {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleDateString('de-DE', {
      day: '2-digit',
      month: '2-digit',
      year: '2-digit',
    });
  };

  const formatValue = (value?: number) => {
    if (!value) return 'Kein Wert';
    return new Intl.NumberFormat('de-DE', {
      style: 'currency',
      currency: 'EUR',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(value);
  };

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
                <PersonIcon 
                  fontSize="small" 
                  sx={{ color: theme.palette.grey[600], mr: 0.5 }} 
                />
                <Typography variant="body2" sx={{ color: theme.palette.text.secondary }}>
                  {opportunity.customerName}
                </Typography>
              </Box>
            )}

            {/* Value */}
            <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
              <EuroIcon 
                fontSize="small" 
                sx={{ color: '#94C456', mr: 0.5 }} 
              />
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
};

// Kanban Column Komponente
interface KanbanColumnProps {
  stage: OpportunityStage;
  opportunities: Opportunity[];
}

const KanbanColumn: React.FC<KanbanColumnProps> = ({ stage, opportunities }) => {
  const theme = useTheme();
  const config = STAGE_CONFIGS[stage];
  const totalValue = opportunities.reduce((sum, opp) => sum + (opp.value || 0), 0);

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
              }
            }}
          />
        </Box>
        
        {totalValue > 0 && (
          <Typography variant="body2" sx={{ color: theme.palette.text.secondary, fontWeight: 500 }}>
            Gesamt: {new Intl.NumberFormat('de-DE', {
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
            sx={{
              minHeight: 300,
              bgcolor: snapshot.isDraggingOver ? 'rgba(148, 196, 86, 0.08)' : 'transparent',
              borderRadius: 1,
              transition: 'background-color 0.2s ease',
            }}
          >
            {opportunities.map((opportunity, index) => (
              <OpportunityCard
                key={opportunity.id}
                opportunity={opportunity}
                index={index}
              />
            ))}
            {provided.placeholder}
          </Box>
        )}
      </Droppable>
    </Paper>
  );
};

// Main Kanban Board
export const KanbanBoard: React.FC = () => {
  const theme = useTheme();
  const [opportunities, setOpportunities] = useState<Opportunity[]>(initialOpportunities);
  const [showClosed, setShowClosed] = useState<boolean>(false);
  const [isDragging, setIsDragging] = useState<boolean>(false);
  const [dragKey, setDragKey] = useState<number>(0); // Force re-render nach Drag

  // Debug: Component Mount/Unmount
  React.useEffect(() => {
    console.log('🔵 KanbanBoard MOUNTED');
    return () => {
      console.log('🔴 KanbanBoard UNMOUNTED');
    };
  }, []);

  // Debug: isDragging changes
  React.useEffect(() => {
    console.log('🎯 isDragging changed to:', isDragging);
  }, [isDragging]);

  // Pipeline-Statistiken berechnen
  const pipelineStats = React.useMemo(() => {
    const activeOpps = opportunities.filter(opp => 
      ACTIVE_STAGES.includes(opp.stage)
    );
    const wonOpps = opportunities.filter(opp => 
      opp.stage === OpportunityStage.CLOSED_WON
    );
    const lostOpps = opportunities.filter(opp => 
      opp.stage === OpportunityStage.CLOSED_LOST
    );
    
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

  // Opportunities nach Stage gruppieren
  const opportunitiesByStage = React.useMemo(() => {
    return Object.values(OpportunityStage).reduce((acc, stage) => {
      acc[stage] = opportunities.filter(opp => opp.stage === stage);
      return acc;
    }, {} as Record<OpportunityStage, Opportunity[]>);
  }, [opportunities]);

  const handleDragStart = () => {
    console.log('🚀 DRAG START - Setting isDragging to true');
    setIsDragging(true);
    // Vibrieren für haptisches Feedback (falls verfügbar)
    if (navigator.vibrate) {
      navigator.vibrate(50);
    }
  };

  const handleDragEnd = (result: DropResult) => {
    console.log('🏁 DRAG END - Result:', result);
    console.log('🏁 DRAG END - Setting isDragging to false');
    setIsDragging(false);
    const { destination, source, draggableId } = result;

    // Kein gültiges Drop-Target
    if (!destination) return;

    // Gleiche Position
    if (destination.droppableId === source.droppableId && destination.index === source.index) {
      return;
    }

    const sourceStage = source.droppableId as OpportunityStage;
    const destStage = destination.droppableId as OpportunityStage;
    
    // Verhindere Drag von abgeschlossenen zu aktiven Stages
    if (CLOSED_STAGES.includes(sourceStage) && ACTIVE_STAGES.includes(destStage)) {
      console.warn('❌ Abgeschlossene Opportunities können nicht reaktiviert werden');
      return;
    }
    
    // Opportunity Stage aktualisieren
    setOpportunities(prevOpportunities => 
      prevOpportunities.map(opp => 
        opp.id === draggableId 
          ? { ...opp, stage: destStage, updatedAt: new Date().toISOString() }
          : opp
      )
    );

    console.log(`✅ Moved opportunity ${draggableId} to ${destStage}`);
    
    // Force re-render des DragDropContext
    setDragKey(prev => prev + 1);
  };

  return (
    <Box sx={{ p: 3 }}>
      {/* Kompakter Header */}
      <Paper sx={{ 
        p: 2, 
        mb: 2, 
        display: 'flex', 
        alignItems: 'center', 
        justifyContent: 'space-between',
        bgcolor: 'background.paper',
        borderBottom: '2px solid #94C456'
      }}>
        {/* Titel */}
        <Typography variant="h5" sx={{ 
          color: '#004F7B',
          fontFamily: 'Antonio, sans-serif',
          fontWeight: 700,
        }}>
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
        <Box sx={{ 
          display: 'flex', 
          alignItems: 'center', 
          gap: 2,
          mb: 2,
          p: 1,
          bgcolor: 'rgba(0, 0, 0, 0.02)',
          borderRadius: 1,
          fontSize: '0.875rem'
        }}>
          <CheckCircleIcon sx={{ fontSize: 16, color: '#66BB6A' }} />
          <Typography variant="body2" sx={{ color: '#66BB6A' }}>
            {pipelineStats.totalWon} Gewonnen ({new Intl.NumberFormat('de-DE', {
              style: 'currency',
              currency: 'EUR',
              minimumFractionDigits: 0,
            }).format(pipelineStats.wonValue)})
          </Typography>
          
          <Typography variant="body2" sx={{ color: theme.palette.text.secondary }}>•</Typography>
          
          <CancelIcon sx={{ fontSize: 16, color: '#EF5350' }} />
          <Typography variant="body2" sx={{ color: '#EF5350' }}>
            {pipelineStats.totalLost} Verloren ({new Intl.NumberFormat('de-DE', {
              style: 'currency',
              currency: 'EUR',
              minimumFractionDigits: 0,
            }).format(pipelineStats.lostValue)})
          </Typography>
          
          <Typography variant="body2" sx={{ color: theme.palette.text.secondary }}>•</Typography>
          
          <Typography variant="body2" sx={{ color: '#FFA726', fontWeight: 600 }}>
            {Math.round(pipelineStats.conversionRate * 100)}% Erfolgsquote
          </Typography>
        </Box>
      )}

      {/* Info Alert wenn abgeschlossene angezeigt werden */}
      {showClosed && (
        <Alert 
          severity="info" 
          sx={{ mb: 2 }}
          onClose={() => setShowClosed(false)}
        >
          Sie sehen jetzt auch abgeschlossene Verkaufschancen (Gewonnen/Verloren). 
          Diese können nicht mehr in aktive Stages verschoben werden.
        </Alert>
      )}

      {/* Kanban Board */}
      <DragDropContext key={dragKey} onDragStart={handleDragStart} onDragEnd={handleDragEnd}>
        <Box sx={{ position: 'relative' }}>
          <Box sx={{ 
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
          }}>
          {/* Zeige nur aktive Stages oder alle je nach Filter */}
          {(showClosed ? Object.values(OpportunityStage) : ACTIVE_STAGES).map((stage) => (
            <KanbanColumn
              key={stage}
              stage={stage}
              opportunities={opportunitiesByStage[stage]}
            />
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
};