import React, { useState, useMemo } from 'react';
import { 
  Box, 
  Typography, 
  Alert,
  CircularProgress,
  Stack
} from '@mui/material';
import { useTheme } from '@mui/material/styles';
import { DndContext, DragOverlay, closestCorners } from '@dnd-kit/core';
import type { DragEndEvent, DragStartEvent } from '@dnd-kit/core';

// Import unserer neuen Komponenten
import { PipelineStage, OpportunityStage } from './PipelineStage';
import { OpportunityCard } from './OpportunityCard';
import type { Opportunity } from './OpportunityCard';

// Temporär: Mock-Daten für Development (bis API vollständig implementiert)
const mockOpportunities: Opportunity[] = [
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
    stage: OpportunityStage.NEEDS_ANALYSIS,
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
  }
];


/**
 * M4 Opportunity Pipeline - Kanban Board für Verkaufschancen
 * 
 * Diese Komponente stellt das Herzstück des prozessorientierten Vertriebs dar.
 * Zeigt alle Opportunities in einem Kanban-Board mit Drag & Drop zwischen Stages.
 */
export const OpportunityPipeline: React.FC = () => {
  const theme = useTheme();
  const [activeOpportunity, setActiveOpportunity] = useState<Opportunity | null>(null);
  
  // State für Mock-Daten (wird bei Drag & Drop aktualisiert)
  const [opportunities, setOpportunities] = useState<Opportunity[]>(mockOpportunities);
  
  // Pipeline-Daten basierend auf aktuellem State berechnen
  const pipelineData = useMemo(() => ({
    totalOpportunities: opportunities.length,
    totalValue: opportunities.reduce((sum, opp) => sum + (opp.value || 0), 0),
    conversionRate: 0.35,
    stageDistribution: Object.values(OpportunityStage).reduce((acc, stage) => {
      const stageOpps = opportunities.filter(opp => opp.stage === stage);
      acc[stage] = {
        count: stageOpps.length,
        value: stageOpps.reduce((sum, opp) => sum + (opp.value || 0), 0),
        opportunities: stageOpps,
      };
      return acc;
    }, {} as Record<OpportunityStage, { count: number, value: number, opportunities: Opportunity[] }>)
  }), [opportunities]);
  
  const isLoading = false;
  const error = null;

  // Drag & Drop Handlers
  const handleDragStart = (event: DragStartEvent) => {
    console.log('🚀 Drag Started:', event.active.id);
    const opportunity = event.active.data.current?.opportunity as Opportunity;
    setActiveOpportunity(opportunity);
  };

  const handleDragEnd = (event: DragEndEvent) => {
    const { active, over } = event;
    console.log('🏁 Drag Ended:', { activeId: active.id, overId: over?.id });
    setActiveOpportunity(null);

    if (!over) {
      console.log('❌ No drop target');
      return;
    }

    const opportunityId = active.id as string;
    const newStage = over.id as OpportunityStage;
    const opportunity = active.data.current?.opportunity as Opportunity;

    // Nur ändern wenn Stage wirklich unterschiedlich
    if (opportunity && opportunity.stage !== newStage) {
      console.log(`✅ Moving opportunity ${opportunity.name} from ${opportunity.stage} to ${newStage}`);
      
      // State aktualisieren - Opportunity Stage ändern
      setOpportunities(prevOpportunities => 
        prevOpportunities.map(opp => 
          opp.id === opportunityId 
            ? { ...opp, stage: newStage, updatedAt: new Date().toISOString() }
            : opp
        )
      );
      
      // TODO: In echter App würde hier API-Call stehen
      // changeStage.mutate({ id: opportunityId, newStage, reason: '...' });
    } else {
      console.log('⚠️ Same stage or no opportunity data');
    }
  };

  // Loading State
  if (isLoading) {
    return (
      <Box sx={{ 
        display: 'flex', 
        justifyContent: 'center', 
        alignItems: 'center', 
        minHeight: 400,
        flexDirection: 'column',
        gap: 2
      }}>
        <CircularProgress size={48} sx={{ color: '#94C456' }} />
        <Typography variant="body1" sx={{ color: theme.palette.text.secondary }}>
          Lade Pipeline-Daten...
        </Typography>
      </Box>
    );
  }

  // Error State
  if (error) {
    return (
      <Box sx={{ p: 3 }}>
        <Alert severity="error" sx={{ mb: 2 }}>
          <Typography variant="h6" sx={{ mb: 1 }}>
            Fehler beim Laden der Pipeline
          </Typography>
          <Typography variant="body2">
            {error instanceof Error ? error.message : 'Unbekannter Fehler aufgetreten'}
          </Typography>
        </Alert>
      </Box>
    );
  }

  // Empty State
  if (!pipelineData || pipelineData.totalOpportunities === 0) {
    return (
      <Box sx={{ p: 3 }}>
        <Typography variant="h4" sx={{ 
          color: '#004F7B',
          fontFamily: 'Antonio, sans-serif',
          fontWeight: 700,
          mb: 3
        }}>
          Verkaufschancen Pipeline
        </Typography>
        
        <Alert severity="info" sx={{ textAlign: 'center' }}>
          <Typography variant="h6" sx={{ mb: 1 }}>
            Keine Verkaufschancen vorhanden
          </Typography>
          <Typography variant="body2">
            Erstellen Sie Ihre erste Verkaufschance, um mit der Pipeline zu beginnen.
          </Typography>
        </Alert>
      </Box>
    );
  }

  return (
    <Box sx={{ p: 3 }}>
      {/* Header */}
      <Box sx={{ mb: 3 }}>
        <Typography variant="h4" sx={{ 
          color: '#004F7B',
          fontFamily: 'Antonio, sans-serif',
          fontWeight: 700,
          mb: 1
        }}>
          Verkaufschancen Pipeline
        </Typography>
        <Typography variant="body1" sx={{ color: theme.palette.text.secondary }}>
          Verwalten Sie Ihre Verkaufschancen in einem übersichtlichen Kanban-Board
        </Typography>
      </Box>

      {/* Pipeline Statistics */}
      <Box sx={{ 
        display: 'flex', 
        gap: 3, 
        mb: 3,
        flexWrap: 'wrap'
      }}>
        <Box sx={{ textAlign: 'center' }}>
          <Typography variant="h5" sx={{ color: '#94C456', fontWeight: 700 }}>
            {pipelineData.totalOpportunities}
          </Typography>
          <Typography variant="body2" sx={{ color: theme.palette.text.secondary }}>
            Gesamt
          </Typography>
        </Box>
        <Box sx={{ textAlign: 'center' }}>
          <Typography variant="h5" sx={{ color: '#004F7B', fontWeight: 700 }}>
            {new Intl.NumberFormat('de-DE', {
              style: 'currency',
              currency: 'EUR',
              minimumFractionDigits: 0,
            }).format(pipelineData.totalValue)}
          </Typography>
          <Typography variant="body2" sx={{ color: theme.palette.text.secondary }}>
            Gesamtwert
          </Typography>
        </Box>
        <Box sx={{ textAlign: 'center' }}>
          <Typography variant="h5" sx={{ color: '#FFA726', fontWeight: 700 }}>
            {Math.round(pipelineData.conversionRate * 100)}%
          </Typography>
          <Typography variant="body2" sx={{ color: theme.palette.text.secondary }}>
            Conversion Rate
          </Typography>
        </Box>
      </Box>

      {/* Kanban Board */}
      <DndContext
        collisionDetection={closestCorners}
        onDragStart={handleDragStart}
        onDragEnd={handleDragEnd}
      >
        <Box sx={{ 
          display: 'flex', 
          gap: 2, 
          overflowX: 'auto',
          pb: 2,
          minHeight: 500
        }}>
          {Object.values(OpportunityStage).map((stage) => {
            const stageData = pipelineData.stageDistribution[stage];
            const opportunities: Opportunity[] = stageData?.opportunities || [];

            return (
              <PipelineStage
                key={stage}
                stage={stage}
                opportunityCount={stageData?.count || 0}
                totalValue={stageData?.value}
              >
                <Stack spacing={1.5}>
                  {opportunities.map((opportunity) => (
                    <OpportunityCard
                      key={opportunity.id}
                      opportunity={opportunity}
                      onClick={(opp) => {
                        // TODO: Open opportunity detail modal
                        console.log('Open opportunity:', opp.id);
                      }}
                    />
                  ))}
                </Stack>
              </PipelineStage>
            );
          })}
        </Box>

        {/* Drag Overlay - Hand-Auge-Koordination optimiert */}
        <DragOverlay 
          adjustScale={false}
          wrapperElement="div"
          style={{
            // Card wird an der ursprünglichen Grab-Position gehalten
            transformOrigin: '0 0',
          }}
          dropAnimation={{
            duration: 250,
            easing: 'cubic-bezier(0.18, 0.67, 0.6, 1.22)',
          }}
        >
          {activeOpportunity && (
            <Box sx={{ 
              transform: 'rotate(5deg)', 
              opacity: 0.9,
              boxShadow: 4,
              pointerEvents: 'none', // Drag-Performance optimieren
            }}>
              <OpportunityCard 
                opportunity={activeOpportunity}
              />
            </Box>
          )}
        </DragOverlay>
      </DndContext>

      {/* TODO: Mutation Loading/Error für API Integration */}
    </Box>
  );
};

export default OpportunityPipeline;