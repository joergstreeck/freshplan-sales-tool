import React from 'react';
import { Container, Typography, Tabs, Tab, Box, Paper } from '@mui/material';
import { MainLayoutV2 } from '../components/layout/MainLayoutV2';
import { UserTable } from '../features/users/UserTable';
import { UserForm } from '../features/users/UserForm';
import { useUser } from '../features/users/userQueries';
import { useUserStore } from '../features/users/userStore';
import PersonIcon from '@mui/icons-material/Person';
import SettingsIcon from '@mui/icons-material/Settings';
import SecurityIcon from '@mui/icons-material/Security';

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
      id={`settings-tabpanel-${index}`}
      aria-labelledby={`settings-tab-${index}`}
      {...other}
    >
      {value === index && <Box sx={{ py: 3 }}>{children}</Box>}
    </div>
  );
}

function a11yProps(index: number) {
  return {
    id: `settings-tab-${index}`,
    'aria-controls': `settings-tabpanel-${index}`,
  };
}

export function SettingsPage() {
  const [tabValue, setTabValue] = React.useState(0);
  
  const { 
    isCreateModalOpen, 
    isEditModalOpen, 
    selectedUserId, 
    closeCreateModal, 
    closeEditModal 
  } = useUserStore();

  // Fetch user data when editing
  const { data: selectedUser } = useUser(selectedUserId || '');

  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
  };

  const handleFormSuccess = () => {
    // Close modals on successful form submission
    closeCreateModal();
    closeEditModal();
  };

  return (
    <MainLayoutV2>
      <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
        <Typography variant="h4" gutterBottom sx={{ mb: 4 }}>
          Einstellungen
        </Typography>
        
        <Paper sx={{ width: '100%' }}>
          <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
            <Tabs 
              value={tabValue} 
              onChange={handleTabChange} 
              aria-label="Einstellungen Tabs"
              sx={{
                '& .MuiTab-root': {
                  textTransform: 'none',
                  minHeight: 64,
                  fontSize: '1rem',
                },
              }}
            >
              <Tab 
                icon={<PersonIcon />} 
                iconPosition="start" 
                label="Benutzerverwaltung" 
                {...a11yProps(0)} 
              />
              <Tab 
                icon={<SettingsIcon />} 
                iconPosition="start" 
                label="Systemeinstellungen" 
                {...a11yProps(1)} 
              />
              <Tab 
                icon={<SecurityIcon />} 
                iconPosition="start" 
                label="Sicherheit" 
                {...a11yProps(2)} 
              />
            </Tabs>
          </Box>

          <Box sx={{ p: 3 }}>
            <TabPanel value={tabValue} index={0}>
              {/* Benutzerverwaltung */}
              {!isCreateModalOpen && !isEditModalOpen && <UserTable />}

              {/* Create user form */}
              {isCreateModalOpen && (
                <Box sx={{ display: 'flex', justifyContent: 'center' }}>
                  <UserForm onSuccess={handleFormSuccess} onCancel={closeCreateModal} />
                </Box>
              )}

              {/* Edit user form */}
              {isEditModalOpen && selectedUser && (
                <Box sx={{ display: 'flex', justifyContent: 'center' }}>
                  <UserForm 
                    user={selectedUser} 
                    onSuccess={handleFormSuccess} 
                    onCancel={closeEditModal} 
                  />
                </Box>
              )}

              {/* Loading state for edit mode */}
              {isEditModalOpen && !selectedUser && selectedUserId && (
                <Box sx={{ display: 'flex', justifyContent: 'center', p: 8 }}>
                  <Typography>Lade Benutzerdaten...</Typography>
                </Box>
              )}
            </TabPanel>

            <TabPanel value={tabValue} index={1}>
              {/* Systemeinstellungen - Placeholder */}
              <Box sx={{ textAlign: 'center', py: 8 }}>
                <SettingsIcon sx={{ fontSize: 64, color: 'text.secondary', mb: 2 }} />
                <Typography variant="h6" color="text.secondary">
                  Systemeinstellungen
                </Typography>
                <Typography color="text.secondary" sx={{ mt: 1 }}>
                  Hier können später allgemeine Systemeinstellungen konfiguriert werden.
                </Typography>
              </Box>
            </TabPanel>

            <TabPanel value={tabValue} index={2}>
              {/* Sicherheit - Placeholder */}
              <Box sx={{ textAlign: 'center', py: 8 }}>
                <SecurityIcon sx={{ fontSize: 64, color: 'text.secondary', mb: 2 }} />
                <Typography variant="h6" color="text.secondary">
                  Sicherheitseinstellungen
                </Typography>
                <Typography color="text.secondary" sx={{ mt: 1 }}>
                  Hier können später Sicherheitseinstellungen wie 2FA konfiguriert werden.
                </Typography>
              </Box>
            </TabPanel>
          </Box>
        </Paper>
      </Container>
    </MainLayoutV2>
  );
}