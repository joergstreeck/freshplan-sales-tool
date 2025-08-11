import React, { useState } from 'react';
import {
  Paper,
  Box,
  Typography,
  Grid,
  TextField,
  FormGroup,
  FormControlLabel,
  Checkbox,
  Button,
  Divider,
  Alert,
  Chip,
} from '@mui/material';
import { formatEUR } from '../../utils/currencyFormatter';
import { useCustomerOnboardingStore } from '../../stores/customerOnboardingStore';

interface HotelSegment {
  size: 'small' | 'medium' | 'large';
  count: number;
  services: {
    breakfast: boolean;
    alacarte: boolean;
    banquet: boolean;
    roomservice: boolean;
  };
}

// Pauschale Jahreswerte pro Segment und Hotelgröße (vereinbart mit Jörg)
const SEGMENT_VALUES = {
  small: {
    breakfast: 15000,
    alacarte: 25000,
    banquet: 5000,
    roomservice: 2000,
  },
  medium: {
    breakfast: 40000,
    alacarte: 80000,
    banquet: 10000,
    roomservice: 5000,
  },
  large: {
    breakfast: 80000,
    alacarte: 180000,
    banquet: 25000,
    roomservice: 10000,
  },
};

interface SegmentedRevenueCalculatorProps {
  onApplyCalculation: (value: number) => void;
  currentValue: number;
}

export const SegmentedRevenueCalculator: React.FC<SegmentedRevenueCalculatorProps> = ({
  onApplyCalculation,
  currentValue: _currentValue,
}) => {
  // Hole die Filialanzahlen aus den Customer-Daten
  const customerData = useCustomerOnboardingStore(state => state.customerData);
  const locationsGermany = parseInt(customerData.locationsGermany) || 0;
  const locationsAustria = parseInt(customerData.locationsAustria) || 0;
  const locationsSwitzerland = parseInt(customerData.locationsSwitzerland) || 0;
  const locationsRestEU = parseInt(customerData.locationsRestEU) || 0;

  // Berechne Gesamtzahl der Filialen
  const totalLocations =
    locationsGermany + locationsAustria + locationsSwitzerland + locationsRestEU;

  // Debug-Ausgabe entfernt

  const [segments, setSegments] = useState<HotelSegment[]>([
    {
      size: 'small',
      count: 0,
      services: { breakfast: false, alacarte: false, banquet: false, roomservice: false },
    },
    {
      size: 'medium',
      count: 0,
      services: { breakfast: false, alacarte: false, banquet: false, roomservice: false },
    },
    {
      size: 'large',
      count: 0,
      services: { breakfast: false, alacarte: false, banquet: false, roomservice: false },
    },
  ]);

  const calculateSegmentTotal = (segment: HotelSegment): number => {
    if (segment.count === 0) return 0;

    const values = SEGMENT_VALUES[segment.size];
    let total = 0;

    if (segment.services.breakfast) total += values.breakfast;
    if (segment.services.alacarte) total += values.alacarte;
    if (segment.services.banquet) total += values.banquet;
    if (segment.services.roomservice) total += values.roomservice;

    return segment.count * total;
  };

  const calculateTotal = (): number => {
    return segments.reduce((sum, segment) => sum + calculateSegmentTotal(segment), 0);
  };

  const updateSegment = (index: number, field: string, value: string | number) => {
    const newSegments = [...segments];
    if (field === 'count') {
      newSegments[index].count = parseInt(value) || 0;
    } else {
      newSegments[index].services[field as keyof (typeof newSegments)[0]['services']] = value;
    }
    setSegments(newSegments);
  };

  const getSizeLabel = (size: string): string => {
    switch (size) {
      case 'small':
        return 'Klein (<50 Zimmer)';
      case 'medium':
        return 'Mittel (50-120 Zimmer)';
      case 'large':
        return 'Groß (>120 Zimmer)';
      default:
        return size;
    }
  };

  const total = calculateTotal();
  const roundedTotal = Math.round(total / 10000) * 10000; // Auf 10.000 runden

  // Berechne die Gesamtanzahl der eingegebenen Hotels
  const totalEnteredHotels = segments.reduce((sum, segment) => sum + segment.count, 0);

  return (
    <Paper
      variant="outlined"
      sx={{
        p: 3,
        mt: 2,
        backgroundColor: 'background.default',
        border: '2px solid',
        borderColor: 'primary.light',
      }}
    >
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
        <Typography variant="h6" color="primary">
          💡 Intelligente Potenzialberechnung
        </Typography>
        {totalLocations > 0 && (
          <Chip
            label={`${totalLocations} ${totalLocations === 1 ? 'Filiale' : 'Filialen'} erfasst`}
            color="primary"
            variant="outlined"
            size="small"
          />
        )}
      </Box>

      <Alert severity="info" sx={{ mb: 3 }}>
        Erfassen Sie grob Ihre Hotelstruktur. Die genaue Aufschlüsselung erfolgt später pro
        Standort.
      </Alert>

      {segments.map((segment, index) => (
        <Box key={segment.size} sx={{ mb: 3 }}>
          <Typography variant="subtitle1" fontWeight="bold" gutterBottom>
            {getSizeLabel(segment.size)}
          </Typography>

          <Grid container spacing={2} alignItems="center">
            <Grid size={{ xs: 12, sm: 3 }}>
              <TextField
                label="Anzahl Hotels"
                type="number"
                size="small"
                fullWidth
                value={segment.count}
                onChange={e => updateSegment(index, 'count', e.target.value)}
                inputProps={{ min: 0 }}
              />
            </Grid>

            <Grid size={{ xs: 12, sm: 9 }}>
              <FormGroup row>
                <FormControlLabel
                  control={
                    <Checkbox
                      checked={segment.services.breakfast}
                      onChange={e => updateSegment(index, 'breakfast', e.target.checked)}
                      disabled={segment.count === 0}
                    />
                  }
                  label="Warmes Frühstück"
                />
                <FormControlLabel
                  control={
                    <Checkbox
                      checked={segment.services.alacarte}
                      onChange={e => updateSegment(index, 'alacarte', e.target.checked)}
                      disabled={segment.count === 0}
                    />
                  }
                  label="À la carte"
                />
                <FormControlLabel
                  control={
                    <Checkbox
                      checked={segment.services.banquet}
                      onChange={e => updateSegment(index, 'banquet', e.target.checked)}
                      disabled={segment.count === 0}
                    />
                  }
                  label="Bankett"
                />
                <FormControlLabel
                  control={
                    <Checkbox
                      checked={segment.services.roomservice}
                      onChange={e => updateSegment(index, 'roomservice', e.target.checked)}
                      disabled={segment.count === 0}
                    />
                  }
                  label="Roomservice"
                />
              </FormGroup>
            </Grid>
          </Grid>

          {segment.count > 0 && calculateSegmentTotal(segment) > 0 && (
            <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
              Potenzial: {formatEUR(calculateSegmentTotal(segment))}
            </Typography>
          )}
        </Box>
      ))}

      <Divider sx={{ my: 2 }} />

      {/* Validierungs-Hinweis wenn Anzahlen nicht übereinstimmen */}
      {totalLocations > 0 && totalEnteredHotels > 0 && totalEnteredHotels !== totalLocations && (
        <Alert severity="warning" sx={{ mb: 2 }}>
          Sie haben {totalEnteredHotels} Hotels in der Kalkulation erfasst, aber {totalLocations}{' '}
          Filialen in Schritt 1 angegeben. Bitte prüfen Sie Ihre Eingabe.
        </Alert>
      )}

      {/* Info wenn alle Hotels erfasst wurden */}
      {totalLocations > 0 && totalEnteredHotels === totalLocations && totalEnteredHotels > 0 && (
        <Alert severity="success" sx={{ mb: 2 }}>
          ✓ Alle {totalLocations} Filialen in der Kalkulation berücksichtigt
        </Alert>
      )}

      {/* Transparente Berechnung */}
      {total > 0 && (
        <Box sx={{ mb: 2 }}>
          <Typography variant="body2" color="text.secondary" gutterBottom>
            Berechnung basiert auf Branchenerfahrungswerten:
          </Typography>
          {segments
            .filter(s => s.count > 0)
            .map(segment => {
              const segmentTotal = calculateSegmentTotal(segment);
              if (segmentTotal === 0) return null;

              return (
                <Typography key={segment.size} variant="caption" display="block" sx={{ ml: 2 }}>
                  {segment.count} {getSizeLabel(segment.size)}: {formatEUR(segmentTotal)}
                </Typography>
              );
            })}
        </Box>
      )}

      {/* Gesamt */}
      <Box
        sx={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          mt: 2,
        }}
      >
        <Box>
          <Typography variant="caption" color="text.secondary">
            Geschätztes Jahrespotenzial:
          </Typography>
          <Typography variant="h5" color="primary" fontWeight="bold">
            {formatEUR(total)}
          </Typography>
          {total > 0 && (
            <Typography variant="caption" color="text.secondary" display="block">
              → für Partnerschaft: {formatEUR(roundedTotal)}
            </Typography>
          )}
        </Box>
        {total > 0 && (
          <Button
            size="medium"
            variant="contained"
            onClick={() => onApplyCalculation(roundedTotal)}
          >
            Übernehmen
          </Button>
        )}
      </Box>

      <Typography
        variant="caption"
        color="text.secondary"
        sx={{ display: 'block', mt: 2, fontStyle: 'italic' }}
      >
        💡 Diese Schätzung können Sie jederzeit anpassen. In Schritt 4 erfassen Sie die Details pro
        Standort.
      </Typography>
    </Paper>
  );
};
