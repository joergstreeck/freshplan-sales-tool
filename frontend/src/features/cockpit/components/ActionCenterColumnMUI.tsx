/**
 * Aktions-Center - Spalte 3 des Sales Cockpit (MUI Version)
 *
 * Zeigt kontextbezogene Aktionen und Details zum ausgewählten Kunden
 * Integriert verschiedene Module wie Calculator, Customer Details etc.
 */

import { useState } from 'react';
import {
  Box,
  Typography,
  Card,
  CardContent,
  Button,
  ButtonGroup,
  Divider,
  Stack,
  IconButton,
  Chip,
  Avatar,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  ListItemAvatar,
  Tabs,
  Tab,
  CircularProgress,
  Alert,
  Dialog,
  DialogTitle,
  DialogContent,
  TextField,
  DialogActions,
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import EmailIcon from '@mui/icons-material/Email';
import PhoneIcon from '@mui/icons-material/Phone';
import CalculateIcon from '@mui/icons-material/Calculate';
import DescriptionIcon from '@mui/icons-material/Description';
import HistoryIcon from '@mui/icons-material/History';
import BusinessIcon from '@mui/icons-material/Business';
import NoteAddIcon from '@mui/icons-material/NoteAdd';
import { useCustomerDetails } from '../../customer/hooks/useCustomerDetails';
import { customerStatusLabels, industryLabels } from '../../customer/types/customer.types';
import { format } from 'date-fns';
import { de } from 'date-fns/locale';

interface ActionCenterColumnMUIProps {
  selectedCustomerId?: string;
  onClose?: () => void;
}

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
      id={`action-center-tabpanel-${index}`}
      aria-labelledby={`action-center-tab-${index}`}
      {...other}
    >
      {value === index && <Box sx={{ pt: 2 }}>{children}</Box>}
    </div>
  );
}

export function ActionCenterColumnMUI({ selectedCustomerId, onClose }: ActionCenterColumnMUIProps) {
  const [activeTab, setActiveTab] = useState(0);
  const [noteDialogOpen, setNoteDialogOpen] = useState(false);
  const [newNote, setNewNote] = useState('');

  // Debug logging
  console.log('ActionCenterColumnMUI - selectedCustomerId:', selectedCustomerId);

  // Lade Kundendaten
  const { data: customer, isLoading, isError } = useCustomerDetails(selectedCustomerId);

  // Empty State wenn kein Kunde ausgewählt
  if (!selectedCustomerId) {
    return (
      <Card
        sx={{
          height: '100%',
          display: 'flex',
          flexDirection: 'column',
          bgcolor: 'background.paper',
        }}
      >
        {/* Header */}
        <Box
          sx={{
            p: 2,
            borderBottom: 1,
            borderColor: 'divider',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
          }}
        >
          <Typography variant="h6" sx={{ color: 'primary.main' }}>
            Arbeitsbereich
          </Typography>
        </Box>

        {/* Empty State Content */}
        <Box
          sx={{
            flex: 1,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            p: 3,
          }}
        >
          <Box sx={{ textAlign: 'center' }}>
            <BusinessIcon sx={{ fontSize: 64, color: 'text.disabled', mb: 2 }} />
            <Typography variant="h6" color="text.secondary" gutterBottom>
              Kein Kunde ausgewählt
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Wählen Sie einen Kunden aus der Arbeitsliste aus,
              <br />
              um mit der Bearbeitung zu beginnen.
            </Typography>
          </Box>
        </Box>
      </Card>
    );
  }

  // Loading State
  if (isLoading) {
    return (
      <Card
        sx={{ height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center' }}
      >
        <CircularProgress />
      </Card>
    );
  }

  // Error State
  if (isError || !customer) {
    return (
      <Card sx={{ height: '100%', p: 2 }}>
        <Alert severity="error">Fehler beim Laden der Kundendaten</Alert>
      </Card>
    );
  }

  // Quick Actions Handler
  const handleEmailClick = () => {
    // TODO: Email-Adresse aus Kontakten laden
    alert('E-Mail-Funktion wird implementiert, sobald Kontaktdaten verfügbar sind');
  };

  const handlePhoneClick = () => {
    // TODO: Telefonnummer aus Kontakten laden
    alert('Anruf-Funktion wird implementiert, sobald Kontaktdaten verfügbar sind');
  };

  const handleCalculatorClick = () => {
    // TODO: Calculator-Modul öffnen
    console.log('Calculator für Kunde:', customer.id);
  };

  const handleQuoteClick = () => {
    // TODO: Angebots-Modul öffnen
    console.log('Angebot erstellen für:', customer.id);
  };

  const handleAddNote = () => {
    // TODO: API-Call zum Speichern der Notiz
    console.log('Neue Notiz:', newNote);
    setNoteDialogOpen(false);
    setNewNote('');
  };

  return (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      {/* Header */}
      <Box
        sx={{
          p: 2,
          borderBottom: 1,
          borderColor: 'divider',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
        }}
      >
        <Typography variant="h6" sx={{ color: 'primary.main' }}>
          Arbeitsbereich
        </Typography>
        {onClose && (
          <IconButton size="small" onClick={onClose}>
            <CloseIcon />
          </IconButton>
        )}
      </Box>

      {/* Customer Header */}
      <Box sx={{ p: 2, bgcolor: 'grey.50' }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
          <Avatar sx={{ width: 56, height: 56, bgcolor: 'primary.main' }}>
            <BusinessIcon />
          </Avatar>
          <Box sx={{ flex: 1 }}>
            <Typography variant="h6">{customer.companyName}</Typography>
            <Box sx={{ display: 'flex', gap: 1, mt: 0.5 }}>
              <Chip
                label={customerStatusLabels[customer.status]}
                color={customer.status === 'AKTIV' ? 'success' : 'default'}
                size="small"
              />
              {customer.expectedAnnualVolume && (
                <Chip
                  label={`Umsatz: € ${customer.expectedAnnualVolume.toLocaleString('de-DE')}`}
                  size="small"
                  variant="outlined"
                />
              )}
              {customer.riskScore > 70 && (
                <Chip label={`Risiko: ${customer.riskScore}%`} size="small" color="error" />
              )}
            </Box>
          </Box>
        </Box>

        {/* Quick Info */}
        <Stack spacing={1}>
          {customer.tradingName && (
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <BusinessIcon fontSize="small" color="action" />
              <Typography variant="body2">Handelsname: {customer.tradingName}</Typography>
            </Box>
          )}
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Typography variant="body2" color="text.secondary">
              Kundennummer: #{customer.customerNumber}
            </Typography>
          </Box>
          {customer.industry && (
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <Typography variant="body2">Branche: {industryLabels[customer.industry]}</Typography>
            </Box>
          )}
        </Stack>
      </Box>

      {/* Quick Actions */}
      <Box sx={{ p: 2, borderBottom: 1, borderColor: 'divider' }}>
        <Typography variant="subtitle2" gutterBottom>
          Schnellaktionen
        </Typography>
        <Box
          sx={{
            display: 'grid',
            gridTemplateColumns: 'repeat(2, 1fr)',
            gap: 1,
          }}
        >
          <Button
            variant="outlined"
            size="small"
            startIcon={<EmailIcon />}
            onClick={handleEmailClick}
            sx={{ justifyContent: 'flex-start' }}
          >
            E-Mail
          </Button>
          <Button
            variant="outlined"
            size="small"
            startIcon={<PhoneIcon />}
            onClick={handlePhoneClick}
            sx={{ justifyContent: 'flex-start' }}
          >
            Anrufen
          </Button>
          <Button
            variant="outlined"
            size="small"
            startIcon={<CalculateIcon />}
            onClick={handleCalculatorClick}
            sx={{ justifyContent: 'flex-start' }}
          >
            Kalkulation
          </Button>
          <Button
            variant="outlined"
            size="small"
            startIcon={<DescriptionIcon />}
            onClick={handleQuoteClick}
            sx={{ justifyContent: 'flex-start' }}
          >
            Angebot
          </Button>
        </Box>
      </Box>

      {/* Tabs */}
      <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
        <Tabs value={activeTab} onChange={(e, v) => setActiveTab(v)}>
          <Tab label="Aktivitäten" />
          <Tab label="Details" />
          <Tab label="Dokumente" />
        </Tabs>
      </Box>

      {/* Tab Content */}
      <Box sx={{ flex: 1, overflow: 'auto', p: 2 }}>
        <TabPanel value={activeTab} index={0}>
          {/* Add Note Button */}
          <Box sx={{ mb: 2 }}>
            <Button
              variant="outlined"
              startIcon={<NoteAddIcon />}
              onClick={() => setNoteDialogOpen(true)}
              fullWidth
            >
              Notiz hinzufügen
            </Button>
          </Box>

          {/* Activity Timeline */}
          <List>
            <ListItem alignItems="flex-start">
              <ListItemAvatar>
                <Avatar sx={{ bgcolor: 'primary.light' }}>
                  <EmailIcon />
                </Avatar>
              </ListItemAvatar>
              <ListItemText
                primary="E-Mail gesendet"
                secondary={
                  <>
                    <Typography component="span" variant="body2" color="text.primary">
                      Angebot für Q3/2025
                    </Typography>
                    {' — Vor 2 Tagen'}
                  </>
                }
              />
            </ListItem>
            <Divider variant="inset" component="li" />
            <ListItem alignItems="flex-start">
              <ListItemAvatar>
                <Avatar sx={{ bgcolor: 'success.light' }}>
                  <PhoneIcon />
                </Avatar>
              </ListItemAvatar>
              <ListItemText
                primary="Telefonat"
                secondary={
                  <>
                    <Typography component="span" variant="body2" color="text.primary">
                      Bedarfsanalyse durchgeführt
                    </Typography>
                    {' — Vor 1 Woche'}
                  </>
                }
              />
            </ListItem>
            <Divider variant="inset" component="li" />
            <ListItem alignItems="flex-start">
              <ListItemAvatar>
                <Avatar sx={{ bgcolor: 'info.light' }}>
                  <HistoryIcon />
                </Avatar>
              </ListItemAvatar>
              <ListItemText
                primary="Status geändert"
                secondary={
                  <>
                    <Typography component="span" variant="body2" color="text.primary">
                      Von "Interessent" zu "Aktiv"
                    </Typography>
                    {' — Vor 2 Wochen'}
                  </>
                }
              />
            </ListItem>
          </List>
        </TabPanel>

        <TabPanel value={activeTab} index={1}>
          {/* Customer Details */}
          <Stack spacing={2}>
            <Card>
              <CardContent>
                <Typography variant="subtitle2" gutterBottom>
                  Unternehmensinformationen
                </Typography>
                <Stack spacing={1} sx={{ mt: 2 }}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                    <Typography variant="body2" color="text.secondary">
                      Kundentyp:
                    </Typography>
                    <Typography variant="body2">{customer.customerType}</Typography>
                  </Box>
                  {customer.classification && (
                    <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                      <Typography variant="body2" color="text.secondary">
                        Klassifizierung:
                      </Typography>
                      <Typography variant="body2">{customer.classification}</Typography>
                    </Box>
                  )}
                  <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                    <Typography variant="body2" color="text.secondary">
                      Kunde seit:
                    </Typography>
                    <Typography variant="body2">
                      {format(new Date(customer.createdAt), 'dd.MM.yyyy', { locale: de })}
                    </Typography>
                  </Box>
                  {customer.creditLimit && (
                    <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                      <Typography variant="body2" color="text.secondary">
                        Kreditlimit:
                      </Typography>
                      <Typography variant="body2">
                        € {customer.creditLimit.toLocaleString('de-DE')}
                      </Typography>
                    </Box>
                  )}
                </Stack>
              </CardContent>
            </Card>

            <Card>
              <CardContent>
                <Typography variant="subtitle2" gutterBottom>
                  Vertriebsinformationen
                </Typography>
                <Stack spacing={1} sx={{ mt: 2 }}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                    <Typography variant="body2" color="text.secondary">
                      Erstellt von:
                    </Typography>
                    <Typography variant="body2">{customer.createdBy}</Typography>
                  </Box>
                  {customer.lastContactDate && (
                    <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                      <Typography variant="body2" color="text.secondary">
                        Letzter Kontakt:
                      </Typography>
                      <Typography variant="body2">
                        {format(new Date(customer.lastContactDate), 'dd.MM.yyyy', { locale: de })}
                      </Typography>
                    </Box>
                  )}
                  {customer.nextFollowUpDate && (
                    <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                      <Typography variant="body2" color="text.secondary">
                        Nächste Aktion:
                      </Typography>
                      <Typography variant="body2" color="warning.main">
                        {format(new Date(customer.nextFollowUpDate), 'dd.MM.yyyy', { locale: de })}
                      </Typography>
                    </Box>
                  )}
                  {customer.paymentTerms && (
                    <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                      <Typography variant="body2" color="text.secondary">
                        Zahlungsziel:
                      </Typography>
                      <Typography variant="body2">{customer.paymentTerms}</Typography>
                    </Box>
                  )}
                </Stack>
              </CardContent>
            </Card>
          </Stack>
        </TabPanel>

        <TabPanel value={activeTab} index={2}>
          {/* Documents */}
          <List>
            <ListItem>
              <ListItemIcon>
                <DescriptionIcon />
              </ListItemIcon>
              <ListItemText primary="Angebot_Q3_2025.pdf" secondary="Hochgeladen vor 2 Tagen" />
            </ListItem>
            <ListItem>
              <ListItemIcon>
                <DescriptionIcon />
              </ListItemIcon>
              <ListItemText primary="Rahmenvertrag_2025.pdf" secondary="Hochgeladen vor 1 Monat" />
            </ListItem>
            <ListItem>
              <ListItemIcon>
                <DescriptionIcon />
              </ListItemIcon>
              <ListItemText
                primary="Produktkatalog_2025.pdf"
                secondary="Hochgeladen vor 2 Monaten"
              />
            </ListItem>
          </List>
        </TabPanel>
      </Box>

      {/* Note Dialog */}
      <Dialog
        open={noteDialogOpen}
        onClose={() => setNoteDialogOpen(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>Notiz hinzufügen</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            multiline
            rows={4}
            fullWidth
            variant="outlined"
            placeholder="Notiz eingeben..."
            value={newNote}
            onChange={e => setNewNote(e.target.value)}
            sx={{ mt: 1 }}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setNoteDialogOpen(false)}>Abbrechen</Button>
          <Button onClick={handleAddNote} variant="contained" disabled={!newNote.trim()}>
            Speichern
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
