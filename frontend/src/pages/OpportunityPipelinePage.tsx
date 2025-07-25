import React from 'react';
import { Box, Typography } from '@mui/material';
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
      <Box sx={{ p: 3, height: '100vh', overflow: 'hidden' }}>
        <Typography 
          variant="h4" 
          component="h1" 
          gutterBottom
          sx={{ 
            fontFamily: 'Antonio, sans-serif',
            fontWeight: 'bold',
            color: '#004F7B',
            mb: 3
          }}
        >
          Verkaufschancen Pipeline
        </Typography>
        
        <KanbanBoardDndKit />
      </Box>
    </MainLayoutV2>
  );
};

export default OpportunityPipelinePage;