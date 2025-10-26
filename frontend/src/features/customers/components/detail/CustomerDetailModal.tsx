/**
 * Customer Detail Modal Component
 *
 * Sprint 2.1.7.2 D11: Server-Driven Customer Cards - Phase 3
 *
 * Fullscreen Drawer für vollständige Kundendetails.
 * Progressive Disclosure: Nur wenn Benutzer "Alle Details anzeigen" klickt.
 *
 * Features:
 * - Fullscreen Drawer (anchor="right")
 * - 3 Tabs: Firma, Geschäft, Verlauf
 * - Zurück-Button oben links
 * - Server-Driven Cards in Tabs
 *
 * Navigation Flow:
 * CustomersPageV2 → CustomerCompactView → [Alle Details] → CustomerDetailModal
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */

import React, { useState } from 'react';
import {
  Drawer,
  Box,
  IconButton,
  Typography,
  Tabs,
  Tab,
  useTheme,
  useMediaQuery,
} from '@mui/material';
import {
  ArrowBack as ArrowBackIcon,
} from '@mui/icons-material';
import { CustomerDetailTabFirma } from './CustomerDetailTabFirma';
import { CustomerDetailTabGeschaeft } from './CustomerDetailTabGeschaeft';
import { CustomerDetailTabVerlauf } from './CustomerDetailTabVerlauf';

interface CustomerDetailModalProps {
  customerId: string;
  open: boolean;
  onClose: () => void;
}

/**
 * TabPanel Component
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

/**
 * Customer Detail Modal
 *
 * Zeigt alle Kundeninformationen in einem Fullscreen Drawer.
 * 20% Use Case: Wenn Benutzer vollständige Details bearbeiten muss.
 */
export const CustomerDetailModal: React.FC<CustomerDetailModalProps> = ({
  customerId,
  open,
  onClose,
}) => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const [activeTab, setActiveTab] = useState(0);

  const handleTabChange = (_event: React.SyntheticEvent, newValue: number) => {
    setActiveTab(newValue);
  };

  return (
    <Drawer
      open={open}
      onClose={onClose}
      anchor="right"
      PaperProps={{
        sx: {
          width: '100%',
          maxWidth: isMobile ? '100%' : '100vw',
        },
      }}
    >
      {/* Header mit Zurück-Button */}
      <Box
        sx={{
          p: 2,
          display: 'flex',
          alignItems: 'center',
          borderBottom: 1,
          borderColor: 'divider',
          bgcolor: 'background.paper',
          position: 'sticky',
          top: 0,
          zIndex: 1100,
        }}
      >
        <IconButton onClick={onClose} edge="start" aria-label="Zurück">
          <ArrowBackIcon />
        </IconButton>
        <Typography variant="h5" sx={{ ml: 2 }}>
          Kundendetails
        </Typography>
      </Box>

      {/* Tabs */}
      <Tabs
        value={activeTab}
        onChange={handleTabChange}
        variant={isMobile ? 'fullWidth' : 'standard'}
        sx={{
          borderBottom: 1,
          borderColor: 'divider',
          bgcolor: 'background.paper',
          position: 'sticky',
          top: 64,
          zIndex: 1100,
        }}
      >
        <Tab
          label="Firma"
          id="customer-detail-tab-0"
          aria-controls="customer-detail-tabpanel-0"
        />
        <Tab
          label="Geschäft"
          id="customer-detail-tab-1"
          aria-controls="customer-detail-tabpanel-1"
        />
        <Tab
          label="Verlauf"
          id="customer-detail-tab-2"
          aria-controls="customer-detail-tabpanel-2"
        />
      </Tabs>

      {/* Tab Content */}
      <TabPanel value={activeTab} index={0}>
        <CustomerDetailTabFirma customerId={customerId} />
      </TabPanel>

      <TabPanel value={activeTab} index={1}>
        <CustomerDetailTabGeschaeft customerId={customerId} />
      </TabPanel>

      <TabPanel value={activeTab} index={2}>
        <CustomerDetailTabVerlauf customerId={customerId} />
      </TabPanel>
    </Drawer>
  );
};
