import React from 'react';
import { Box } from '@mui/material';
import { MainLayoutV2 } from '../components/layout/MainLayoutV2';
import { KanbanBoardDndKit } from '../features/opportunity/components/KanbanBoardDndKit';

/**
 * M4 Opportunity Pipeline Page
 *
 * Hauptseite für das Verkaufschancen-Management mit Kanban Board.
 * Zeigt alle Opportunities in verschiedenen Verkaufsphasen mit
 * Drag & Drop Funktionalität.
 */
export const OpportunityPipelinePage: React.FC = () => {
  return (
    <MainLayoutV2>
      <Box sx={{ height: '100vh', overflow: 'hidden' }}>
        <KanbanBoardDndKit />
      </Box>
    </MainLayoutV2>
  );
};

export default OpportunityPipelinePage;
