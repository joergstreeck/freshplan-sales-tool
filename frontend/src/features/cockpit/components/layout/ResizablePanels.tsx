import React, { ReactNode, useEffect, useState } from 'react';
import { Panel, PanelGroup, PanelResizeHandle } from 'react-resizable-panels';
import { Box } from '@mui/material';
import { styled } from '@mui/material/styles';

interface ResizablePanelsProps {
  children: ReactNode[];
  storageKey?: string;
  minSizes?: number[];
  defaultSizes?: number[];
}

const StyledPanelGroup = styled(PanelGroup)(() => ({
  height: '100%',
  width: '100%',
  display: 'flex',
  flexDirection: 'row',
  gap: 0,
}));

const StyledResizeHandle = styled(PanelResizeHandle)(({ theme }) => ({
  width: '8px',
  background: 'transparent',
  position: 'relative',
  cursor: 'col-resize',
  transition: 'background-color 0.2s',
  
  '&:hover': {
    backgroundColor: theme.palette.primary.light,
    opacity: 0.3,
  },
  
  '&:active': {
    backgroundColor: theme.palette.primary.main,
    opacity: 0.5,
  },
  
  '&::before': {
    content: '""',
    position: 'absolute',
    top: '50%',
    left: '50%',
    transform: 'translate(-50%, -50%)',
    width: '2px',
    height: '40px',
    backgroundColor: theme.palette.divider,
    borderRadius: '1px',
  },
}));

const PanelContainer = styled(Box)(() => ({
  height: '100%',
  overflow: 'auto',
  display: 'flex',
  flexDirection: 'column',
}));

export const ResizablePanels: React.FC<ResizablePanelsProps> = ({
  children,
  storageKey = 'cockpit-panels',
  minSizes = [20, 20, 20],
  defaultSizes = [33, 34, 33],
}) => {
  const [sizes, setSizes] = useState<number[]>(defaultSizes);

  // Lade gespeicherte Größen aus localStorage
  useEffect(() => {
    const savedSizes = localStorage.getItem(`${storageKey}-sizes`);
    if (savedSizes) {
      try {
        const parsed = JSON.parse(savedSizes);
        if (Array.isArray(parsed) && parsed.length === children.length) {
          setSizes(parsed);
        }
      } catch (error) {
        console.error('Fehler beim Laden der Spaltengrößen:', error);
      }
    }
  }, [storageKey, children.length]);

  // Speichere Größen bei Änderung
  const handleLayout = (newSizes: number[]) => {
    setSizes(newSizes);
    localStorage.setItem(`${storageKey}-sizes`, JSON.stringify(newSizes));
  };

  // Filter nur gültige Kinder (keine null/undefined)
  const validChildren = React.Children.toArray(children).filter(Boolean);

  if (validChildren.length === 0) {
    return null;
  }

  return (
    <StyledPanelGroup
      direction="horizontal"
      onLayout={handleLayout}
    >
      {validChildren.map((child, index) => (
        <React.Fragment key={index}>
          <Panel
            defaultSize={sizes[index]}
            minSize={minSizes[index] || 20}
            order={index}
          >
            <PanelContainer>
              {child}
            </PanelContainer>
          </Panel>
          {index < validChildren.length - 1 && (
            <StyledResizeHandle />
          )}
        </React.Fragment>
      ))}
    </StyledPanelGroup>
  );
};

export default ResizablePanels;