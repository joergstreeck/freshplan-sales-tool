/**
 * Detailed Locations Step Component
 *
 * Third step of the wizard - manages detailed locations within each location.
 * Allows batch creation and management of sub-locations like departments, stations, etc.
 *
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/01-components.md
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/01-TECH-CONCEPT/03-data-model.md
 */

import React, { useState, useMemo } from 'react';
import {
  Box,
  Typography,
  Button,
  IconButton,
  Alert,
  AlertTitle,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  Chip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Select,
  MenuItem,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Tooltip,
  Divider,
  List,
  ListItem,
  ListItemText,
  ListItemSecondaryAction,
} from '@mui/material';
import {
  ExpandMore as ExpandMoreIcon,
  Add as AddIcon,
  Delete as DeleteIcon,
  Edit as EditIcon,
  ContentCopy as CopyIcon,
  LocalHospital as HospitalIcon,
  Hotel as HotelIcon,
  Restaurant as RestaurantIcon,
  Business as BusinessIcon,
  Groups as GroupsIcon,
  FileDownload as DownloadIcon,
} from '@mui/icons-material';
import { useCustomerOnboardingStore } from '../../stores/customerOnboardingStore';
import { DetailedLocation, DetailedLocationCategory } from '../../types/location.types';
import { toast } from 'react-toastify';
import {
  categoryLabels,
  getCategoryIcon,
  getIndustryTemplates,
} from '../../config';

// Configuration data has been externalized to separate files for better maintainability
// See: /Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/config/

interface BatchAddDialogProps {
  open: boolean;
  onClose: () => void;
  onAdd: (locations: Omit<DetailedLocation, 'id' | 'locationId'>[]) => void;
  locationName: string;
  industry: string;
}

/**
 * Dialog for batch adding detailed locations
 */
const BatchAddDialog: React.FC<BatchAddDialogProps> = ({
  open,
  onClose,
  onAdd,
  locationName,
  industry,
}) => {
  const [locations, setLocations] = useState<
    Array<{
      name: string;
      category: DetailedLocationCategory;
      floor?: string;
      capacity?: number;
    }>
  >([{ name: '', category: 'restaurant' }]);

  const templates = getIndustryTemplates(industry) || [];

  const handleAddRow = () => {
    setLocations([...locations, { name: '', category: 'restaurant' }]);
  };

  const handleRemoveRow = (index: number) => {
    setLocations(locations.filter((_, i) => i !== index));
  };

  const handleChange = (index: number, field: string, value: any) => {
    const updated = [...locations];
    updated[index] = { ...updated[index], [field]: value };
    setLocations(updated);
  };

  const handleLoadTemplate = () => {
    setLocations(
      templates.map(t => ({
        name: t.name,
        category: t.category,
        floor: '',
        capacity: undefined,
      }))
    );
    toast.info(`Template für ${industry} geladen`);
  };

  const handleSave = () => {
    const validLocations = locations.filter(l => l.name.trim());
    if (validLocations.length === 0) {
      toast.error('Bitte geben Sie mindestens einen Namen ein');
      return;
    }
    onAdd(validLocations);
    onClose();
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>Detail-Standorte für {locationName} hinzufügen</DialogTitle>
      <DialogContent>
        {templates.length > 0 && (
          <Box sx={{ mb: 2 }}>
            <Button
              variant="outlined"
              size="small"
              onClick={handleLoadTemplate}
              startIcon={<DownloadIcon />}
            >
              {industry}-Template laden
            </Button>
          </Box>
        )}

        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Name</TableCell>
              <TableCell>Kategorie</TableCell>
              <TableCell>Etage</TableCell>
              <TableCell>Kapazität</TableCell>
              <TableCell width={50}></TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {locations.map((location, index) => (
              <TableRow key={index}>
                <TableCell>
                  <TextField
                    size="small"
                    value={location.name}
                    onChange={e => handleChange(index, 'name', e.target.value)}
                    placeholder="z.B. Station 2A"
                    fullWidth
                  />
                </TableCell>
                <TableCell>
                  <Select
                    size="small"
                    value={location.category}
                    onChange={e => handleChange(index, 'category', e.target.value)}
                    fullWidth
                  >
                    {Object.entries(categoryLabels).map(([value, label]) => (
                      <MenuItem key={value} value={value}>
                        {label}
                      </MenuItem>
                    ))}
                  </Select>
                </TableCell>
                <TableCell>
                  <TextField
                    size="small"
                    value={location.floor || ''}
                    onChange={e => handleChange(index, 'floor', e.target.value)}
                    placeholder="z.B. 1. OG"
                  />
                </TableCell>
                <TableCell>
                  <TextField
                    size="small"
                    type="number"
                    value={location.capacity || ''}
                    onChange={e =>
                      handleChange(
                        index,
                        'capacity',
                        e.target.value ? parseInt(e.target.value) : undefined
                      )
                    }
                    placeholder="50"
                  />
                </TableCell>
                <TableCell>
                  <IconButton
                    size="small"
                    onClick={() => handleRemoveRow(index)}
                    disabled={locations.length === 1}
                  >
                    <DeleteIcon fontSize="small" />
                  </IconButton>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>

        <Box sx={{ mt: 2 }}>
          <Button startIcon={<AddIcon />} onClick={handleAddRow} size="small">
            Zeile hinzufügen
          </Button>
        </Box>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Abbrechen</Button>
        <Button
          onClick={handleSave}
          variant="contained"
          sx={{
            bgcolor: '#94C456',
            '&:hover': { bgcolor: '#7BA345' },
          }}
        >
          Hinzufügen
        </Button>
      </DialogActions>
    </Dialog>
  );
};

/**
 * Detailed Locations Step
 *
 * Manages detailed locations within each main location.
 * Supports batch operations and industry-specific templates.
 */
export const DetailedLocationsStep: React.FC = () => {
  const {
    locations,
    locationFieldValues,
    detailedLocations,
    customerData,
    addDetailedLocation,
    updateDetailedLocation,
    removeDetailedLocation,
    addBatchDetailedLocations,
  } = useCustomerOnboardingStore();

  const [expandedLocations, setExpandedLocations] = useState<string[]>([]);
  const [batchAddDialog, setBatchAddDialog] = useState<{
    open: boolean;
    locationId: string;
    locationName: string;
  }>({ open: false, locationId: '', locationName: '' });
  const [editingLocation, setEditingLocation] = useState<string | null>(null);

  const industry = customerData.industry || '';

  /**
   * Get industry icon
   */
  const getIndustryIcon = () => {
    switch (industry) {
      case 'hotel':
        return <HotelIcon />;
      case 'krankenhaus':
        return <HospitalIcon />;
      case 'restaurant':
        return <RestaurantIcon />;
      default:
        return <BusinessIcon />;
    }
  };

  /**
   * Toggle location expansion
   */
  const toggleExpanded = (locationId: string) => {
    setExpandedLocations(prev =>
      prev.includes(locationId) ? prev.filter(id => id !== locationId) : [...prev, locationId]
    );
  };

  /**
   * Get detailed locations for a specific location
   */
  const getDetailedLocationsForLocation = (locationId: string) => {
    return detailedLocations.filter(dl => dl.locationId === locationId);
  };

  /**
   * Handle batch add
   */
  const handleBatchAdd = (
    locationId: string,
    newLocations: Omit<DetailedLocation, 'id' | 'locationId'>[]
  ) => {
    addBatchDetailedLocations(locationId, newLocations);
    toast.success(`${newLocations.length} Detail-Standorte hinzugefügt`);
  };

  /**
   * Calculate statistics
   */
  const stats = useMemo(() => {
    const totalDetailedLocations = detailedLocations.length;
    const byCategory = detailedLocations.reduce(
      (acc, dl) => {
        acc[dl.category] = (acc[dl.category] || 0) + 1;
        return acc;
      },
      {} as Record<string, number>
    );

    return { total: totalDetailedLocations, byCategory };
  }, [detailedLocations]);

  return (
    <Box>
      <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
        {getIndustryIcon()}
        <Typography variant="h5" component="h2" sx={{ ml: 1 }}>
          Detail-Standorte verwalten
        </Typography>
      </Box>

      <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
        Erfassen Sie hier die detaillierten Bereiche innerhalb Ihrer Standorte, wie z.B. Stationen,
        Restaurants, Abteilungen oder Verkaufspunkte.
      </Typography>

      {locations.length === 0 ? (
        <Alert severity="warning">
          <AlertTitle>Keine Standorte vorhanden</AlertTitle>
          Bitte erfassen Sie zuerst Ihre Standorte im vorherigen Schritt.
        </Alert>
      ) : (
        <>
          {/* Statistics */}
          {stats.total > 0 && (
            <Box sx={{ mb: 3, p: 2, bgcolor: 'grey.50', borderRadius: 1 }}>
              <Typography variant="body2" gutterBottom>
                <strong>Gesamt: {stats.total} Detail-Standorte</strong>
              </Typography>
              <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', mt: 1 }}>
                {Object.entries(stats.byCategory).map(([category, count]) => (
                  <Chip
                    key={category}
                    icon={getCategoryIcon(category as DetailedLocationCategory)}
                    label={`${categoryLabels[category as DetailedLocationCategory]}: ${count}`}
                    size="small"
                    variant="outlined"
                  />
                ))}
              </Box>
            </Box>
          )}

          {/* Location Accordions */}
          {locations.map((location, index) => {
            const locationName = locationFieldValues[location.id]?.name || `Standort ${index + 1}`;
            const locationDetailedLocations = getDetailedLocationsForLocation(location.id);

            return (
              <Accordion
                key={location.id}
                expanded={expandedLocations.includes(location.id)}
                onChange={() => toggleExpanded(location.id)}
                sx={{ mb: 1 }}
              >
                <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                  <Box sx={{ display: 'flex', alignItems: 'center', width: '100%', pr: 2 }}>
                    <BusinessIcon sx={{ mr: 1, color: 'primary.main' }} />
                    <Typography sx={{ flexGrow: 1 }}>{locationName}</Typography>
                    <Chip
                      label={`${locationDetailedLocations.length} Bereiche`}
                      size="small"
                      color={locationDetailedLocations.length > 0 ? 'primary' : 'default'}
                    />
                  </Box>
                </AccordionSummary>

                <AccordionDetails>
                  <Box>
                    {/* Action Buttons */}
                    <Box sx={{ mb: 2, display: 'flex', gap: 1 }}>
                      <Button
                        size="small"
                        variant="outlined"
                        startIcon={<AddIcon />}
                        onClick={() =>
                          setBatchAddDialog({
                            open: true,
                            locationId: location.id,
                            locationName,
                          })
                        }
                      >
                        Bereiche hinzufügen
                      </Button>

                      {locationDetailedLocations.length > 0 && (
                        <Button
                          size="small"
                          variant="outlined"
                          startIcon={<CopyIcon />}
                          onClick={() => {
                            // Copy to other locations logic
                            toast.info('Kopieren zu anderen Standorten - Funktion folgt');
                          }}
                        >
                          Zu anderen kopieren
                        </Button>
                      )}
                    </Box>

                    {/* Detailed Locations List */}
                    {locationDetailedLocations.length === 0 ? (
                      <Alert severity="info" variant="outlined">
                        Noch keine Detail-Standorte erfasst. Klicken Sie auf "Bereiche hinzufügen"
                        um zu beginnen.
                      </Alert>
                    ) : (
                      <List disablePadding>
                        {locationDetailedLocations.map((dl, dlIndex) => (
                          <React.Fragment key={dl.id}>
                            {dlIndex > 0 && <Divider />}
                            <ListItem
                              sx={{
                                bgcolor:
                                  editingLocation === dl.id ? 'action.selected' : 'transparent',
                                '&:hover': { bgcolor: 'action.hover' },
                              }}
                            >
                              <Box sx={{ display: 'flex', alignItems: 'center', mr: 2 }}>
                                {getCategoryIcon(dl.category)}
                              </Box>
                              <ListItemText
                                primary={dl.name}
                                secondary={
                                  <Box component="span" sx={{ display: 'flex', gap: 1 }}>
                                    <Typography variant="caption" component="span">
                                      {categoryLabels[dl.category]}
                                    </Typography>
                                    {dl.floor && (
                                      <>
                                        <Typography variant="caption" component="span">
                                          •
                                        </Typography>
                                        <Typography variant="caption" component="span">
                                          {dl.floor}
                                        </Typography>
                                      </>
                                    )}
                                    {dl.capacity && (
                                      <>
                                        <Typography variant="caption" component="span">
                                          •
                                        </Typography>
                                        <Typography variant="caption" component="span">
                                          Kapazität: {dl.capacity}
                                        </Typography>
                                      </>
                                    )}
                                  </Box>
                                }
                              />
                              <ListItemSecondaryAction>
                                <Tooltip title="Bearbeiten">
                                  <IconButton
                                    edge="end"
                                    size="small"
                                    onClick={() => {
                                      setEditingLocation(dl.id);
                                      toast.info('Bearbeiten-Funktion folgt');
                                    }}
                                  >
                                    <EditIcon fontSize="small" />
                                  </IconButton>
                                </Tooltip>
                                <Tooltip title="Löschen">
                                  <IconButton
                                    edge="end"
                                    size="small"
                                    onClick={() => {
                                      removeDetailedLocation(dl.id);
                                      toast.success('Detail-Standort entfernt');
                                    }}
                                  >
                                    <DeleteIcon fontSize="small" />
                                  </IconButton>
                                </Tooltip>
                              </ListItemSecondaryAction>
                            </ListItem>
                          </React.Fragment>
                        ))}
                      </List>
                    )}
                  </Box>
                </AccordionDetails>
              </Accordion>
            );
          })}

          {/* Info Box */}
          <Alert severity="info" sx={{ mt: 3 }} icon={<GroupsIcon />}>
            <AlertTitle>Tipp für {industry}</AlertTitle>
            {industry === 'hotel' &&
              'Erfassen Sie alle gastronomischen Outlets, Bars und Veranstaltungsräume als separate Bereiche.'}
            {industry === 'krankenhaus' &&
              'Denken Sie an alle Stationen, Cafeterien und Kioske. Jede Station kann individuelle Anforderungen haben.'}
            {industry === 'seniorenresidenz' &&
              'Unterscheiden Sie zwischen Wohnbereichen, Pflegestationen und allgemeinen Bereichen.'}
            {industry === 'restaurant' &&
              'Bei mehreren Räumen oder Außenbereichen erfassen Sie diese als separate Bereiche.'}
            {industry === 'betriebsrestaurant' &&
              'Unterschiedliche Ausgabestellen oder Themenbereiche können als eigene Bereiche erfasst werden.'}
          </Alert>
        </>
      )}

      {/* Batch Add Dialog */}
      <BatchAddDialog
        open={batchAddDialog.open}
        onClose={() => setBatchAddDialog({ open: false, locationId: '', locationName: '' })}
        onAdd={locations => handleBatchAdd(batchAddDialog.locationId, locations)}
        locationName={batchAddDialog.locationName}
        industry={industry}
      />
    </Box>
  );
};
