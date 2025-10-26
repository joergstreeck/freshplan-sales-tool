/**
 * Customer Detail View Component (Modal/Drawer)
 *
 * Sprint 2.1.7.2 D11: Server-Driven Customer Cards - Phase 2
 *
 * Fullscreen Drawer mit Tab-Navigation für Deep Dive (20% Use Case).
 *
 * Architektur-Entscheidung: Option B (Modal/Drawer)
 * - Konsistent mit SPA-Navigation Pattern
 * - Eigener ← Zurück Button (kein Browser-Zurück)
 * - Fullscreen für maximale Informationsdichte
 *
 * Features:
 * - 3 Tabs: Firma, Geschäft, Verlauf
 * - Server-Driven Cards in 2-Spalten Grid Layout
 * - MUI v7 Grid API compliant
 * - Responsive (Mobile: 1 Spalte, Desktop: 2 Spalten)
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */

import React, { useState } from 'react';
import {
  Drawer,
  Box,
  Typography,
  IconButton,
  Tabs,
  Tab,
  AppBar,
  Toolbar,
  Alert,
} from '@mui/material';
import {
  Close as CloseIcon,
  Business as BusinessIcon,
  Handshake as HandshakeIcon,
  Timeline as TimelineIcon,
} from '@mui/icons-material';
import { CustomerDetailTabFirma } from './CustomerDetailTabFirma';
import { CustomerDetailTabGeschaeft } from './CustomerDetailTabGeschaeft';

interface CustomerDetailViewProps {
  open: boolean;
  onClose: () => void;
  customerId: string;
  customerName?: string;
}

/**
 * Tab Panel Component
 */
interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

function TabPanel(props: TabPanelProps) {
  const { children, value, index, ...other } = props;

  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`customer-detail-tabpanel-${index}`}
      aria-labelledby={`customer-detail-tab-${index}`}
      {...other}
    >
      {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
    </div>
  );
}

function a11yProps(index: number) {
  return {
    id: `customer-detail-tab-${index}`,
    'aria-controls': `customer-detail-tabpanel-${index}`,
  };
}

/**
 * Customer Detail View (Drawer)
 *
 * Deep Dive mit Tabs für vollständige Kundeninformationen.
 * 20% Use Case - für detaillierte Analysen und Multi-Location Details.
 */
export const CustomerDetailView: React.FC<CustomerDetailViewProps> = ({
  open,
  onClose,
  customerId,
  customerName = 'Kunde',
}) => {
  const [activeTab, setActiveTab] = useState(0);

  const handleTabChange = (_event: React.SyntheticEvent, newValue: number) => {
    setActiveTab(newValue);
  };

  return (
    <Drawer
      anchor="right"
      open={open}
      onClose={onClose}
      PaperProps={{
        sx: {
          width: '100%',
          maxWidth: { xs: '100%', lg: '90%' },
        },
      }}
    >
      {/* AppBar with Close Button */}
      <AppBar position="static" color="default" elevation={1}>
        <Toolbar>
          <IconButton edge="start" color="inherit" onClick={onClose} aria-label="schließen">
            <CloseIcon />
          </IconButton>
          <Typography variant="h6" sx={{ ml: 2, flex: 1 }}>
            {customerName} - Alle Details
          </Typography>
        </Toolbar>

        {/* Tabs */}
        <Tabs
          value={activeTab}
          onChange={handleTabChange}
          aria-label="Kunden-Detail Tabs"
          sx={{ px: 2 }}
        >
          <Tab label="Firma" icon={<BusinessIcon />} iconPosition="start" {...a11yProps(0)} />
          <Tab
            label="Geschäft"
            icon={<HandshakeIcon />}
            iconPosition="start"
            {...a11yProps(1)}
          />
          <Tab
            label="Verlauf"
            icon={<TimelineIcon />}
            iconPosition="start"
            {...a11yProps(2)}
            disabled
          />
        </Tabs>
      </AppBar>

      {/* Tab Panels */}
      <Box sx={{ flexGrow: 1, overflow: 'auto', bgcolor: 'background.default' }}>
        {/* Tab 0: Firma */}
        <TabPanel value={activeTab} index={0}>
          <CustomerDetailTabFirma customerId={customerId} />
        </TabPanel>

        {/* Tab 1: Geschäft */}
        <TabPanel value={activeTab} index={1}>
          <CustomerDetailTabGeschaeft customerId={customerId} />
        </TabPanel>

        {/* Tab 2: Verlauf (DISABLED - Phase 4) */}
        <TabPanel value={activeTab} index={2}>
          <Alert severity="info">
            <Typography variant="body2">
              <strong>Verlauf-Tab:</strong> Wird in Phase 4 implementiert (Kontakte & Timeline).
            </Typography>
          </Alert>
        </TabPanel>
      </Box>
    </Drawer>
  );
};
