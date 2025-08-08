# 📍 Location Intelligence - Standort-basierte Insights

**Phase:** 2 - Intelligence Features  
**Tag:** 4 der Woche 2  
**Status:** 🎯 Ready for Implementation  

## 🧭 Navigation

**← Zurück:** [Smart Suggestions](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/SMART_SUGGESTIONS.md)  
**→ Nächster:** [Performance Optimization](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/PERFORMANCE_OPTIMIZATION.md)  
**↑ Übergeordnet:** [Step 3 Main Guide](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/README.md)  

## 🎯 Vision: Geografische Beziehungsintelligenz

**Location Intelligence** bringt **räumliche Dimension** in die Kontaktverwaltung:

> "Wer ist wo zuständig? Wer kennt sich vor Ort aus?"

## 🗺️ Location Data Architecture

### Enhanced Data Model

```typescript
// types/location.types.ts
export interface LocationIntelligence {
  locationId: string;
  locationName: string;
  address: Address;
  coordinates?: {
    latitude: number;
    longitude: number;
  };
  contacts: LocationContact[];
  statistics: LocationStats;
  nearbyLocations?: NearbyLocation[];
  travelTime?: {
    fromHQ: number; // minutes
    fromSalesRep?: number;
  };
}

export interface LocationContact {
  contact: Contact;
  role: LocationRole;
  coverage: {
    primary: boolean;
    since: Date;
    expertise: string[];
  };
  performance?: {
    lastVisit?: Date;
    visitFrequency: number; // per month
    orderValue: number;
    satisfactionScore?: number;
  };
}

export enum LocationRole {
  PRIMARY_CONTACT = 'primary_contact',
  BACKUP_CONTACT = 'backup_contact',
  TECHNICAL_SUPPORT = 'technical_support',
  DECISION_MAKER = 'decision_maker',
  LOCAL_EXPERT = 'local_expert'
}

export interface LocationStats {
  totalContacts: number;
  coverageScore: number; // 0-100
  lastInteraction?: Date;
  monthlyRevenue: number;
  growthTrend: 'up' | 'down' | 'stable';
}
```

## 🎨 Location Dashboard Components

### Location Overview Map

```typescript
// components/LocationOverviewMap.tsx
import React, { useState, useMemo } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Chip,
  Avatar,
  AvatarGroup,
  IconButton,
  Tooltip,
  Badge,
  Paper,
  List,
  ListItem,
  ListItemAvatar,
  ListItemText,
  ListItemSecondaryAction
} from '@mui/material';
import {
  LocationOn as LocationIcon,
  Navigation as NavigationIcon,
  Warning as WarningIcon,
  TrendingUp as TrendingUpIcon,
  Group as GroupIcon,
  DirectionsCar as CarIcon
} from '@mui/icons-material';
import { MapContainer, TileLayer, Marker, Popup, Circle } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';

export const LocationOverviewMap: React.FC<{
  customerId: string;
  locations: LocationIntelligence[];
  onLocationSelect: (locationId: string) => void;
}> = ({ customerId, locations, onLocationSelect }) => {
  const [selectedLocation, setSelectedLocation] = useState<string | null>(null);
  const [mapView, setMapView] = useState<'coverage' | 'revenue' | 'contacts'>('coverage');
  
  // Calculate map bounds
  const mapBounds = useMemo(() => {
    if (!locations.length) return null;
    
    const lats = locations.map(l => l.coordinates?.latitude || 0).filter(Boolean);
    const lngs = locations.map(l => l.coordinates?.longitude || 0).filter(Boolean);
    
    return {
      center: {
        lat: lats.reduce((a, b) => a + b, 0) / lats.length,
        lng: lngs.reduce((a, b) => a + b, 0) / lngs.length
      },
      bounds: [
        [Math.min(...lats), Math.min(...lngs)],
        [Math.max(...lats), Math.max(...lngs)]
      ]
    };
  }, [locations]);
  
  const getMarkerColor = (location: LocationIntelligence): string => {
    if (mapView === 'coverage') {
      if (location.statistics.coverageScore < 50) return '#f44336';
      if (location.statistics.coverageScore < 80) return '#ff9800';
      return '#4caf50';
    }
    
    if (mapView === 'revenue') {
      if (location.statistics.monthlyRevenue > 100000) return '#4caf50';
      if (location.statistics.monthlyRevenue > 50000) return '#2196f3';
      return '#9e9e9e';
    }
    
    return '#2196f3';
  };
  
  const getLocationIcon = (location: LocationIntelligence) => {
    const hasWarning = location.statistics.coverageScore < 50 || 
                      !location.contacts.some(c => c.role === LocationRole.PRIMARY_CONTACT);
    
    return (
      <Box position="relative">
        <LocationIcon 
          sx={{ 
            fontSize: 40,
            color: getMarkerColor(location)
          }}
        />
        {hasWarning && (
          <Badge
            badgeContent={<WarningIcon sx={{ fontSize: 12 }} />}
            color="error"
            sx={{ position: 'absolute', top: -5, right: -5 }}
          />
        )}
      </Box>
    );
  };
  
  return (
    <Box>
      {/* Map Controls */}
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
        <Typography variant="h6">Standort-Übersicht</Typography>
        
        <ToggleButtonGroup
          value={mapView}
          exclusive
          onChange={(_, value) => value && setMapView(value)}
          size="small"
        >
          <ToggleButton value="coverage">
            <Tooltip title="Abdeckung">
              <GroupIcon />
            </Tooltip>
          </ToggleButton>
          <ToggleButton value="revenue">
            <Tooltip title="Umsatz">
              <TrendingUpIcon />
            </Tooltip>
          </ToggleButton>
          <ToggleButton value="contacts">
            <Tooltip title="Kontakte">
              <PersonIcon />
            </Tooltip>
          </ToggleButton>
        </ToggleButtonGroup>
      </Box>
      
      {/* Map Container */}
      <Card sx={{ height: 500, position: 'relative' }}>
        {mapBounds && (
          <MapContainer
            center={[mapBounds.center.lat, mapBounds.center.lng]}
            zoom={10}
            style={{ height: '100%', width: '100%' }}
          >
            <TileLayer
              url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
              attribution='&copy; OpenStreetMap contributors'
            />
            
            {locations.map(location => (
              location.coordinates && (
                <Marker
                  key={location.locationId}
                  position={[location.coordinates.latitude, location.coordinates.longitude]}
                  eventHandlers={{
                    click: () => {
                      setSelectedLocation(location.locationId);
                      onLocationSelect(location.locationId);
                    }
                  }}
                >
                  <Popup>
                    <LocationPopup location={location} />
                  </Popup>
                  
                  {/* Coverage Circle */}
                  {mapView === 'coverage' && (
                    <Circle
                      center={[location.coordinates.latitude, location.coordinates.longitude]}
                      radius={5000} // 5km
                      pathOptions={{
                        color: getMarkerColor(location),
                        fillOpacity: 0.2
                      }}
                    />
                  )}
                </Marker>
              )
            ))}
          </MapContainer>
        )}
        
        {/* Location Cards Overlay */}
        <Paper
          sx={{
            position: 'absolute',
            top: 16,
            left: 16,
            width: 300,
            maxHeight: 'calc(100% - 32px)',
            overflow: 'auto',
            zIndex: 1000
          }}
        >
          <List>
            {locations
              .sort((a, b) => b.statistics.coverageScore - a.statistics.coverageScore)
              .map(location => (
                <ListItem
                  key={location.locationId}
                  button
                  selected={selectedLocation === location.locationId}
                  onClick={() => {
                    setSelectedLocation(location.locationId);
                    onLocationSelect(location.locationId);
                  }}
                >
                  <ListItemAvatar>
                    {getLocationIcon(location)}
                  </ListItemAvatar>
                  
                  <ListItemText
                    primary={location.locationName}
                    secondary={
                      <Box>
                        <Typography variant="caption" display="block">
                          {location.contacts.length} Kontakte
                        </Typography>
                        <Box display="flex" gap={0.5} mt={0.5}>
                          <Chip
                            label={`${location.statistics.coverageScore}%`}
                            size="small"
                            color={location.statistics.coverageScore >= 80 ? 'success' : 'warning'}
                          />
                          {location.statistics.growthTrend === 'up' && (
                            <Chip
                              icon={<TrendingUpIcon />}
                              label="Wachsend"
                              size="small"
                              color="primary"
                            />
                          )}
                        </Box>
                      </Box>
                    }
                  />
                  
                  <ListItemSecondaryAction>
                    <AvatarGroup max={3} sx={{ '& .MuiAvatar-root': { width: 24, height: 24 } }}>
                      {location.contacts.slice(0, 3).map(lc => (
                        <Tooltip key={lc.contact.id} title={`${lc.contact.firstName} ${lc.contact.lastName}`}>
                          <Avatar sx={{ fontSize: '0.75rem' }}>
                            {lc.contact.firstName[0]}{lc.contact.lastName[0]}
                          </Avatar>
                        </Tooltip>
                      ))}
                    </AvatarGroup>
                  </ListItemSecondaryAction>
                </ListItem>
              ))}
          </List>
        </Paper>
      </Card>
    </Box>
  );
};

// Location Popup Component
const LocationPopup: React.FC<{ location: LocationIntelligence }> = ({ location }) => {
  return (
    <Box sx={{ minWidth: 250 }}>
      <Typography variant="h6" gutterBottom>
        {location.locationName}
      </Typography>
      
      <Typography variant="body2" color="text.secondary" paragraph>
        {location.address.street}, {location.address.city}
      </Typography>
      
      <Box display="flex" gap={1} flexDirection="column">
        <Box display="flex" justifyContent="space-between">
          <Typography variant="caption">Abdeckung:</Typography>
          <Chip
            label={`${location.statistics.coverageScore}%`}
            size="small"
            color={location.statistics.coverageScore >= 80 ? 'success' : 'warning'}
          />
        </Box>
        
        <Box display="flex" justifyContent="space-between">
          <Typography variant="caption">Monatsumsatz:</Typography>
          <Typography variant="caption" fontWeight="bold">
            {location.statistics.monthlyRevenue.toLocaleString('de-DE')} €
          </Typography>
        </Box>
        
        {location.travelTime && (
          <Box display="flex" justifyContent="space-between">
            <Typography variant="caption">Fahrzeit:</Typography>
            <Chip
              icon={<CarIcon sx={{ fontSize: 16 }} />}
              label={`${location.travelTime.fromHQ} Min`}
              size="small"
              variant="outlined"
            />
          </Box>
        )}
      </Box>
      
      <Box mt={2}>
        <Typography variant="subtitle2" gutterBottom>
          Ansprechpartner:
        </Typography>
        {location.contacts.slice(0, 3).map(lc => (
          <Box key={lc.contact.id} display="flex" alignItems="center" gap={1} mb={0.5}>
            <Avatar sx={{ width: 20, height: 20, fontSize: '0.7rem' }}>
              {lc.contact.firstName[0]}
            </Avatar>
            <Typography variant="caption">
              {lc.contact.firstName} {lc.contact.lastName}
              {lc.role === LocationRole.PRIMARY_CONTACT && ' (Primary)'}
            </Typography>
          </Box>
        ))}
      </Box>
    </Box>
  );
};
```

### Location Assignment Manager

```typescript
// components/LocationAssignmentManager.tsx
export const LocationAssignmentManager: React.FC<{
  contact: Contact;
  locations: CustomerLocation[];
  assignments: LocationAssignment[];
  onAssign: (locationId: string, role: LocationRole) => void;
  onUnassign: (locationId: string) => void;
}> = ({ contact, locations, assignments, onAssign, onUnassign }) => {
  const [selectedRole, setSelectedRole] = useState<LocationRole>(LocationRole.PRIMARY_CONTACT);
  
  const assignedLocationIds = new Set(assignments.map(a => a.locationId));
  
  const getRoleColor = (role: LocationRole): string => {
    const colors = {
      [LocationRole.PRIMARY_CONTACT]: 'primary',
      [LocationRole.BACKUP_CONTACT]: 'secondary',
      [LocationRole.TECHNICAL_SUPPORT]: 'info',
      [LocationRole.DECISION_MAKER]: 'warning',
      [LocationRole.LOCAL_EXPERT]: 'success'
    };
    return colors[role] || 'default';
  };
  
  return (
    <Card>
      <CardHeader
        title="Standort-Zuordnungen"
        subheader={`${contact.firstName} ${contact.lastName}`}
        action={
          <FormControl size="small">
            <InputLabel>Rolle</InputLabel>
            <Select
              value={selectedRole}
              onChange={(e) => setSelectedRole(e.target.value as LocationRole)}
              label="Rolle"
            >
              <MenuItem value={LocationRole.PRIMARY_CONTACT}>
                Hauptansprechpartner
              </MenuItem>
              <MenuItem value={LocationRole.BACKUP_CONTACT}>
                Vertretung
              </MenuItem>
              <MenuItem value={LocationRole.TECHNICAL_SUPPORT}>
                Technischer Support
              </MenuItem>
              <MenuItem value={LocationRole.DECISION_MAKER}>
                Entscheider
              </MenuItem>
              <MenuItem value={LocationRole.LOCAL_EXPERT}>
                Lokaler Experte
              </MenuItem>
            </Select>
          </FormControl>
        }
      />
      
      <CardContent>
        <Grid container spacing={2}>
          {/* Available Locations */}
          <Grid item xs={12} md={6}>
            <Typography variant="subtitle2" gutterBottom>
              Verfügbare Standorte
            </Typography>
            
            <List dense sx={{ bgcolor: 'background.paper', borderRadius: 1 }}>
              {locations
                .filter(loc => !assignedLocationIds.has(loc.id))
                .map(location => (
                  <ListItem
                    key={location.id}
                    secondaryAction={
                      <IconButton
                        edge="end"
                        onClick={() => onAssign(location.id, selectedRole)}
                      >
                        <AddIcon />
                      </IconButton>
                    }
                  >
                    <ListItemIcon>
                      <LocationIcon />
                    </ListItemIcon>
                    <ListItemText
                      primary={location.name}
                      secondary={location.address}
                    />
                  </ListItem>
                ))}
            </List>
          </Grid>
          
          {/* Assigned Locations */}
          <Grid item xs={12} md={6}>
            <Typography variant="subtitle2" gutterBottom>
              Zugeordnete Standorte
            </Typography>
            
            <List dense sx={{ bgcolor: 'background.paper', borderRadius: 1 }}>
              {assignments.map(assignment => {
                const location = locations.find(l => l.id === assignment.locationId);
                if (!location) return null;
                
                return (
                  <ListItem
                    key={assignment.locationId}
                    secondaryAction={
                      <IconButton
                        edge="end"
                        onClick={() => onUnassign(assignment.locationId)}
                      >
                        <RemoveIcon />
                      </IconButton>
                    }
                  >
                    <ListItemIcon>
                      <LocationIcon />
                    </ListItemIcon>
                    <ListItemText
                      primary={location.name}
                      secondary={
                        <Chip
                          label={getRoleLabel(assignment.role)}
                          size="small"
                          color={getRoleColor(assignment.role) as any}
                        />
                      }
                    />
                  </ListItem>
                );
              })}
            </List>
          </Grid>
        </Grid>
        
        {/* Coverage Analysis */}
        <Box mt={3}>
          <Alert 
            severity={assignments.length === 0 ? 'warning' : 'info'}
            icon={<LocationIcon />}
          >
            {assignments.length === 0 ? (
              'Dieser Kontakt ist noch keinem Standort zugeordnet.'
            ) : (
              `Zuständig für ${assignments.length} Standort${assignments.length > 1 ? 'e' : ''}`
            )}
          </Alert>
        </Box>
      </CardContent>
    </Card>
  );
};
```

## 📊 Location Analytics

### Coverage Analysis Service

```typescript
// services/locationAnalytics.ts
export class LocationAnalyticsService {
  async analyzeLocationCoverage(
    customerId: string
  ): Promise<LocationCoverageAnalysis> {
    const locations = await locationApi.getCustomerLocations(customerId);
    const contacts = await contactApi.getContacts(customerId);
    
    const analysis: LocationCoverageAnalysis = {
      totalLocations: locations.length,
      coveredLocations: 0,
      uncoveredLocations: [],
      weakCoverage: [],
      optimalAssignments: [],
      travelOptimization: null
    };
    
    // Analyze each location
    for (const location of locations) {
      const assignedContacts = contacts.filter(c => 
        c.assignedLocationId === location.id
      );
      
      if (assignedContacts.length === 0) {
        analysis.uncoveredLocations.push({
          location,
          reason: 'Kein zugeordneter Kontakt',
          priority: 'high',
          suggestedContacts: this.findNearestContacts(location, contacts)
        });
      } else if (assignedContacts.length === 1) {
        analysis.weakCoverage.push({
          location,
          reason: 'Nur ein Kontakt zugeordnet - keine Vertretung',
          priority: 'medium',
          suggestedBackup: this.findBackupContact(location, assignedContacts[0], contacts)
        });
      } else {
        analysis.coveredLocations++;
      }
    }
    
    // Travel optimization
    if (locations.length > 5) {
      analysis.travelOptimization = await this.calculateOptimalRoutes(locations, contacts);
    }
    
    return analysis;
  }
  
  private findNearestContacts(
    location: CustomerLocation,
    contacts: Contact[]
  ): Contact[] {
    // Simple distance calculation (would use real geocoding in production)
    return contacts
      .filter(c => !c.assignedLocationId) // Unassigned contacts
      .slice(0, 3); // Top 3 suggestions
  }
  
  private async calculateOptimalRoutes(
    locations: CustomerLocation[],
    contacts: Contact[]
  ): Promise<TravelOptimization> {
    // Group locations by assigned contact
    const contactRoutes = new Map<string, CustomerLocation[]>();
    
    locations.forEach(location => {
      const primaryContact = contacts.find(c => 
        c.assignedLocationId === location.id && c.isPrimary
      );
      
      if (primaryContact) {
        if (!contactRoutes.has(primaryContact.id)) {
          contactRoutes.set(primaryContact.id, []);
        }
        contactRoutes.get(primaryContact.id)!.push(location);
      }
    });
    
    // Calculate optimal routes for each contact
    const optimizations = [];
    for (const [contactId, locations] of contactRoutes.entries()) {
      if (locations.length > 1) {
        const route = await this.calculateTSPRoute(locations);
        optimizations.push({
          contactId,
          currentDistance: route.currentDistance,
          optimizedDistance: route.optimizedDistance,
          timeSaved: route.timeSaved,
          suggestedOrder: route.order
        });
      }
    }
    
    return {
      totalTimeSaved: optimizations.reduce((sum, opt) => sum + opt.timeSaved, 0),
      routes: optimizations
    };
  }
}
```

## 🧪 Testing

```typescript
describe('LocationIntelligence', () => {
  it('should identify uncovered locations', async () => {
    const service = new LocationAnalyticsService();
    const analysis = await service.analyzeLocationCoverage('customer-1');
    
    expect(analysis.uncoveredLocations).toHaveLength(2);
    expect(analysis.uncoveredLocations[0].suggestedContacts).toHaveLength(3);
  });
  
  it('should calculate travel optimization', async () => {
    const locations = createMockLocations(10);
    const optimization = await service.calculateOptimalRoutes(locations, contacts);
    
    expect(optimization.totalTimeSaved).toBeGreaterThan(0);
  });
});
```

## 🎯 Success Metrics

### Coverage:
- **Location Coverage:** > 95% with primary contact
- **Backup Coverage:** > 80% with secondary contact
- **Response Time:** < 2h for local issues

### Efficiency:
- **Travel Time Reduction:** -20% through optimization
- **Contact Utilization:** Balanced workload
- **Local Expertise:** Right person at right place

---

**Nächster Schritt:** [→ Performance Optimization](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/PERFORMANCE_OPTIMIZATION.md)

**Location Intelligence = Räumliche Beziehungsstrategie! 📍✨**