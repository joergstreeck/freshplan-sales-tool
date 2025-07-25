import React, { useState } from 'react';
import {
  DndContext,
  KeyboardSensor,
  PointerSensor,
  useSensor,
  useSensors,
  DragOverlay,
  useDroppable,
  rectIntersection,
} from '@dnd-kit/core';
import type { DragEndEvent, DragStartEvent } from '@dnd-kit/core';
import {
  SortableContext,
  sortableKeyboardCoordinates,
  verticalListSortingStrategy,
} from '@dnd-kit/sortable';
import {
  Box,
  Paper,
  Typography,
  Badge,
  Card,
  CardContent,
  Stack,
  Avatar,
  LinearProgress,
  IconButton,
  Tooltip,
} from '@mui/material';
import { useTheme } from '@mui/material/styles';
import PersonIcon from '@mui/icons-material/Person';
import BusinessIcon from '@mui/icons-material/Business';
import EuroIcon from '@mui/icons-material/Euro';
import CalendarTodayIcon from '@mui/icons-material/CalendarToday';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import CancelIcon from '@mui/icons-material/Cancel';
import RestoreIcon from '@mui/icons-material/Restore';
import { SortableOpportunityCard } from './SortableOpportunityCard';

import { OpportunityStage, STAGE_CONFIGS } from '../types/stages';

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
  contactName?: string;
  assignedToName?: string;
  expectedCloseDate?: string;
  createdAt: string;
  updatedAt: string;
}

// Convert STAGE_CONFIGS array to Record for easier lookup
const STAGE_CONFIGS_RECORD: Record<string, typeof STAGE_CONFIGS[0]> = {};
STAGE_CONFIGS.forEach(config => {
  STAGE_CONFIGS_RECORD[config.stage] = config;
});

// Mock-Daten
const initialOpportunities: Opportunity[] = [
  {
    id: '1',
    name: 'Großauftrag Wocheneinkauf',
    stage: OpportunityStage.LEAD,
    value: 15000,
    probability: 20,
    customerName: 'Restaurant Schmidt GmbH',
    contactName: 'Hans Schmidt',
    assignedToName: 'Max Mustermann',
    expectedCloseDate: '2025-08-15',
    createdAt: '2025-07-01T10:00:00Z',
    updatedAt: '2025-07-20T15:30:00Z',
  },
  {
    id: '2', 
    name: 'Wocheneinkauf Hotelküche',
    stage: OpportunityStage.QUALIFIED,
    value: 8500,
    probability: 60,
    customerName: 'Hotel Adler',
    contactName: 'Maria Adler',
    assignedToName: 'Anna Weber',
    expectedCloseDate: '2025-07-30',
    createdAt: '2025-07-05T09:15:00Z',
    updatedAt: '2025-07-22T11:45:00Z',
  },
  {
    id: '3',
    name: 'Event-Paket Sommerfest',
    stage: OpportunityStage.PROPOSAL,
    value: 5200,
    probability: 80,
    customerName: 'Catering Müller',
    contactName: 'Peter Müller',
    assignedToName: 'Tom Fischer',
    expectedCloseDate: '2025-08-01',
    createdAt: '2025-07-10T14:20:00Z',
    updatedAt: '2025-07-23T09:10:00Z',
  },
  {
    id: '4',
    name: 'Großbestellung Jubiläum',
    stage: OpportunityStage.CLOSED_WON,
    value: 12000,
    probability: 100,
    customerName: 'Bistro Sonne',
    contactName: 'Julia Sonne',
    assignedToName: 'Max Mustermann',
    expectedCloseDate: '2025-07-15',
    createdAt: '2025-06-20T10:00:00Z',
    updatedAt: '2025-07-15T16:00:00Z',
  },
  {
    id: '5',
    name: 'Testbestellung Kantine',
    stage: OpportunityStage.CLOSED_LOST,
    value: 3000,
    probability: 0,
    customerName: 'Kantine Nord GmbH',
    contactName: 'Klaus Nord',
    assignedToName: 'Anna Weber',
    expectedCloseDate: '2025-07-10',
    createdAt: '2025-06-25T11:00:00Z',
    updatedAt: '2025-07-10T14:30:00Z',
  }
];

// Opportunity Card Komponente (für DragOverlay)
interface OpportunityCardProps {
  opportunity: Opportunity;
  isDragging?: boolean;
  onQuickAction?: (opportunityId: string, action: 'won' | 'lost' | 'reactivate') => void;
  showActions?: boolean;
}

export const OpportunityCard: React.FC<OpportunityCardProps> = ({ 
  opportunity, 
  isDragging = false,
  onQuickAction,
  showActions = false
}) => {
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
    <Card
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
      sx={{
        opacity: isDragging ? 0.5 : 1,
        boxShadow: isDragging ? theme.shadows[8] : theme.shadows[2],
        border: '1px solid rgba(148, 196, 86, 0.2)',
        cursor: isDragging ? 'grabbing' : 'grab',
        transform: isDragging ? 'rotate(5deg)' : 'none',
        '&:hover': {
          boxShadow: theme.shadows[4],
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

        {/* Customer & Contact */}
        <Stack spacing={0.5} sx={{ mb: 1 }}>
          {opportunity.customerName && (
            <Box sx={{ display: 'flex', alignItems: 'center' }}>
              <BusinessIcon 
                fontSize="small" 
                sx={{ color: theme.palette.grey[600], mr: 0.5 }} 
              />
              <Typography variant="body2" sx={{ color: theme.palette.text.secondary }}>
                {opportunity.customerName}
              </Typography>
            </Box>
          )}
          {opportunity.contactName && (
            <Box sx={{ display: 'flex', alignItems: 'center' }}>
              <PersonIcon 
                fontSize="small" 
                sx={{ color: theme.palette.grey[600], mr: 0.5 }} 
              />
              <Typography variant="body2" sx={{ color: theme.palette.text.secondary }}>
                {opportunity.contactName}
              </Typography>
            </Box>
          )}
        </Stack>

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
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
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
          </Box>

          <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
            {/* Action Buttons - je nach Stage */}
            {showActions && onQuickAction && (
              <>
                {/* Buttons für aktive Stages */}
                {ACTIVE_STAGES.includes(opportunity.stage) && (
                  <>
                    <Tooltip title="Als gewonnen markieren">
                      <IconButton
                        size="small"
                        onClick={(e) => {
                          e.stopPropagation();
                          onQuickAction(opportunity.id, 'won');
                        }}
                        sx={{
                          width: 24,
                          height: 24,
                          color: '#66BB6A',
                          opacity: 0.7,
                          transition: 'all 0.2s ease',
                          '&:hover': {
                            bgcolor: 'rgba(102, 187, 106, 0.1)',
                            opacity: 1,
                            transform: 'scale(1.1)',
                          },
                        }}
                      >
                        <CheckCircleIcon fontSize="small" />
                      </IconButton>
                    </Tooltip>
                    <Tooltip title="Als verloren markieren">
                      <IconButton
                        size="small"
                        onClick={(e) => {
                          e.stopPropagation();
                          onQuickAction(opportunity.id, 'lost');
                        }}
                        sx={{
                          width: 24,
                          height: 24,
                          color: '#EF5350',
                          opacity: 0.7,
                          transition: 'all 0.2s ease',
                          '&:hover': {
                            bgcolor: 'rgba(239, 83, 80, 0.1)',
                            opacity: 1,
                            transform: 'scale(1.1)',
                          },
                        }}
                      >
                        <CancelIcon fontSize="small" />
                      </IconButton>
                    </Tooltip>
                  </>
                )}
                
                {/* Reaktivieren-Button für verlorene Opportunities */}
                {opportunity.stage === OpportunityStage.CLOSED_LOST && (
                  <Tooltip title="Opportunity reaktivieren">
                    <IconButton
                      size="small"
                      onClick={(e) => {
                        e.stopPropagation();
                        onQuickAction(opportunity.id, 'reactivate');
                      }}
                      sx={{
                        width: 24,
                        height: 24,
                        color: '#2196F3',
                        opacity: 0.7,
                        transition: 'all 0.2s ease',
                        '&:hover': {
                          bgcolor: 'rgba(33, 150, 243, 0.1)',
                          opacity: 1,
                          transform: 'scale(1.1)',
                        },
                      }}
                    >
                      <RestoreIcon fontSize="small" />
                    </IconButton>
                  </Tooltip>
                )}
              </>
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
        </Box>
      </CardContent>
    </Card>
  );
};


// Kanban Column Komponente
interface KanbanColumnProps {
  stage: OpportunityStage;
  opportunities: Opportunity[];
  onQuickAction?: (opportunityId: string, action: 'won' | 'lost' | 'reactivate') => void;
  animatingIds: Set<string>;
}

const KanbanColumn: React.FC<KanbanColumnProps> = ({ stage, opportunities, onQuickAction, animatingIds }) => {
  const theme = useTheme();
  const config = STAGE_CONFIGS_RECORD[stage];
  const totalValue = opportunities.reduce((sum, opp) => sum + (opp.value || 0), 0);
  
  // Make the column a drop target
  const { setNodeRef, isOver } = useDroppable({
    id: stage,
  });

  return (
    <Paper
      ref={setNodeRef}
      sx={{
        minHeight: 400,
        flex: '1 1 0',
        minWidth: 280,
        maxWidth: 450,
        p: 2,
        bgcolor: isOver ? 'rgba(148, 196, 86, 0.1)' : config.bgColor,
        border: isOver ? '2px solid #94C456' : '1px solid rgba(0, 0, 0, 0.12)',
        borderRadius: 2,
        transition: 'all 0.2s ease',
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

      {/* Sortable Area */}
      <SortableContext
        items={opportunities.map(o => o.id)}
        strategy={verticalListSortingStrategy}
      >
        <Box sx={{ minHeight: 300 }}>
          {opportunities.map((opportunity) => (
            <SortableOpportunityCard
              key={opportunity.id}
              opportunity={opportunity}
              onQuickAction={onQuickAction}
              isAnimating={animatingIds.has(opportunity.id)}
            />
          ))}
        </Box>
      </SortableContext>
    </Paper>
  );
};

// Main Kanban Board
export const KanbanBoardDndKit: React.FC = () => {
  const theme = useTheme();
  const [opportunities, setOpportunities] = useState<Opportunity[]>(initialOpportunities);
  const [activeId, setActiveId] = useState<string | null>(null);
  const [animatingIds, setAnimatingIds] = useState<Set<string>>(new Set());

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

  const handleDragStart = (event: DragStartEvent) => {
    console.log('🚀 DRAG START - dnd-kit', event.active.id);
    setActiveId(event.active.id as string);
  };

  const handleDragEnd = (event: DragEndEvent) => {
    console.log('🏁 DRAG END - dnd-kit', {
      activeId: event.active.id,
      overId: event.over?.id,
      overData: event.over?.data,
    });
    const { active, over } = event;
    setActiveId(null);

    if (!over) return;

    const activeOpp = opportunities.find(o => o.id === active.id);
    if (!activeOpp) return;

    // Find which stage the item was dropped in
    let targetStage: OpportunityStage | null = null;
    
    // Check if dropped on a stage column or quick drop zone
    if (Object.values(OpportunityStage).includes(over.id as OpportunityStage)) {
      targetStage = over.id as OpportunityStage;
      console.log('Dropped on stage/quick-zone:', targetStage);
    } else {
      // Dropped on another opportunity - find its stage
      const overOpp = opportunities.find(o => o.id === over.id);
      if (overOpp) {
        targetStage = overOpp.stage;
        console.log('Dropped on opportunity in stage:', targetStage);
      }
    }

    if (!targetStage || targetStage === activeOpp.stage) {
      console.log('Same stage or no target stage');
      return;
    }

    // Verhindere Drag von abgeschlossenen zu aktiven Stages
    if (CLOSED_STAGES.includes(activeOpp.stage) && ACTIVE_STAGES.includes(targetStage)) {
      console.warn('❌ Abgeschlossene Opportunities können nicht reaktiviert werden');
      return;
    }

    // Update opportunity stage
    setOpportunities(prevOpportunities => 
      prevOpportunities.map(opp => 
        opp.id === active.id 
          ? { ...opp, stage: targetStage, updatedAt: new Date().toISOString() }
          : opp
      )
    );

    console.log(`✅ Moved opportunity ${active.id} to ${targetStage}`);
  };

  const handleQuickAction = (opportunityId: string, action: 'won' | 'lost' | 'reactivate') => {
    let targetStage: OpportunityStage;
    
    if (action === 'reactivate') {
      // Bei Reaktivierung zurück in Lead-Phase
      targetStage = OpportunityStage.LEAD;
    } else {
      targetStage = action === 'won' ? OpportunityStage.CLOSED_WON : OpportunityStage.CLOSED_LOST;
    }
    
    // Animation starten
    setAnimatingIds(prev => new Set(prev).add(opportunityId));
    
    // Nach kurzer Verzögerung Stage wechseln
    setTimeout(() => {
      setOpportunities(prevOpportunities => 
        prevOpportunities.map(opp => 
          opp.id === opportunityId 
            ? { ...opp, stage: targetStage, updatedAt: new Date().toISOString() }
            : opp
        )
      );
      
      // Animation nach weiterer Verzögerung beenden
      setTimeout(() => {
        setAnimatingIds(prev => {
          const newSet = new Set(prev);
          newSet.delete(opportunityId);
          return newSet;
        });
      }, 500);
    }, 300);

    console.log(`✅ Quick action: ${action === 'reactivate' ? 'Reactivated' : 'Marked'} opportunity ${opportunityId} as ${action}`);
  };

  const activeOpportunity = activeId ? opportunities.find(o => o.id === activeId) : null;

  // Custom collision detection that prioritizes quick drop zones
  const customCollisionDetection = (args: Parameters<typeof rectIntersection>[0]) => {
    // Use rect intersection for better drop zone detection
    return rectIntersection(args);
  };

  // Initialize scroll indicator on mount
  React.useEffect(() => {
    const container = document.getElementById('kanbanScrollContainer');
    const indicator = document.getElementById('scrollIndicator');
    if (container && indicator) {
      const indicatorWidth = (container.clientWidth / container.scrollWidth) * 100;
      indicator.style.width = `${indicatorWidth}%`;
      indicator.style.left = '0%';
    }
  }, []);

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


      {/* Scroll-Indikator oben */}
      <Box sx={{ 
        position: 'relative',
        width: '100%',
        height: 8,
        bgcolor: 'rgba(0, 0, 0, 0.05)',
        borderRadius: 1,
        mb: 2,
        overflow: 'hidden'
      }}>
        <Box
          id="scrollIndicator"
          sx={{
            position: 'absolute',
            height: '100%',
            bgcolor: '#94C456',
            borderRadius: 1,
            transition: 'all 0.1s ease',
            '&:hover': {
              bgcolor: '#7BA646',
            },
          }}
        />
      </Box>

      {/* Kanban Board */}
      <DndContext
        sensors={sensors}
        collisionDetection={customCollisionDetection}
        onDragStart={handleDragStart}
        onDragEnd={handleDragEnd}
      >
        <Box 
          id="kanbanScrollContainer"
          sx={{ 
            display: 'flex', 
            gap: 2, 
            pb: 2,
            minHeight: 500,
            overflowX: 'auto',
            overflowY: 'hidden',
            '&::-webkit-scrollbar': {
              height: 0, // Verstecke nativen Scrollbar
            },
          }}
          onScroll={(e) => {
            const container = e.target as HTMLElement;
            const indicator = document.getElementById('scrollIndicator');
            if (indicator) {
              const scrollPercentage = container.scrollLeft / (container.scrollWidth - container.clientWidth);
              const indicatorWidth = (container.clientWidth / container.scrollWidth) * 100;
              indicator.style.width = `${indicatorWidth}%`;
              indicator.style.left = `${scrollPercentage * (100 - indicatorWidth)}%`;
            }
          }}
        >
          {/* Zeige immer alle Stages */}
          {Object.values(OpportunityStage).map((stage) => (
            <KanbanColumn
              key={stage}
              stage={stage}
              opportunities={opportunitiesByStage[stage]}
              onQuickAction={handleQuickAction}
              animatingIds={animatingIds}
            />
          ))}
        </Box>

        <DragOverlay
          style={{
            cursor: 'grabbing',
          }}
          modifiers={[
            // Custom modifier für Cursor-Zentrierung
            ({ transform }) => ({
              ...transform,
              x: transform.x - 280, // Volle Kartenbreite
            })
          ]}
        >
          {activeOpportunity ? (
            <OpportunityCard opportunity={activeOpportunity} isDragging />
          ) : null}
        </DragOverlay>
      </DndContext>
    </Box>
  );
};