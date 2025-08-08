import React, { useMemo } from 'react';
import {
  Box,
  Paper,
  Typography,
  Tooltip,
  Skeleton,
  Chip,
  IconButton,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  useTheme
} from '@mui/material';
import {
  Info as InfoIcon,
  ZoomIn as ZoomInIcon,
  ZoomOut as ZoomOutIcon
} from '@mui/icons-material';
import { format, startOfWeek, addDays, addHours, isSameDay } from 'date-fns';
import { de } from 'date-fns/locale';

interface HeatmapDataPoint {
  timestamp: Date;
  value: number;
  label?: string;
  details?: {
    totalEvents: number;
    uniqueUsers: number;
    criticalEvents: number;
    dsgvoEvents?: number;
  };
}

interface AuditActivityHeatmapProps {
  data?: HeatmapDataPoint[];
  granularity?: 'hour' | 'day' | 'week';
  dateRange?: {
    from: Date;
    to: Date;
  };
  isLoading?: boolean;
  onCellClick?: (dataPoint: HeatmapDataPoint) => void;
  height?: number;
}

export const AuditActivityHeatmap: React.FC<AuditActivityHeatmapProps> = ({
  data = [],
  granularity = 'hour',
  dateRange,
  isLoading = false,
  onCellClick,
  height = 400
}) => {
  const theme = useTheme();

  // Calculate intensity color based on value
  const getIntensityColor = (value: number, maxValue: number): string => {
    if (value === 0) return theme.palette.grey[100];
    
    const intensity = value / maxValue;
    
    if (intensity < 0.2) return '#e8f5e9'; // Very light green
    if (intensity < 0.4) return '#a5d6a7'; // Light green
    if (intensity < 0.6) return '#66bb6a'; // Medium green
    if (intensity < 0.8) return '#43a047'; // Dark green
    return '#2e7d32'; // Very dark green
  };

  // Get color for critical events
  const getCriticalColor = (criticalEvents: number): string => {
    if (criticalEvents === 0) return 'transparent';
    if (criticalEvents < 5) return '#fff3e0'; // Light orange
    if (criticalEvents < 10) return '#ffcc80'; // Medium orange
    return '#ff6b6b'; // Red for high critical events
  };

  // Prepare heatmap grid data
  const heatmapGrid = useMemo(() => {
    if (!data || data.length === 0) return [];

    if (granularity === 'hour') {
      // Create 7 days x 24 hours grid
      const grid: HeatmapDataPoint[][] = [];
      const startDate = dateRange?.from || new Date(Date.now() - 7 * 24 * 60 * 60 * 1000);
      
      for (let day = 0; day < 7; day++) {
        const dayData: HeatmapDataPoint[] = [];
        const currentDay = addDays(startDate, day);
        
        for (let hour = 0; hour < 24; hour++) {
          const cellTime = addHours(currentDay, hour);
          const dataPoint = data.find(d => 
            isSameDay(new Date(d.timestamp), cellTime) &&
            new Date(d.timestamp).getHours() === hour
          );
          
          dayData.push(dataPoint || {
            timestamp: cellTime,
            value: 0,
            details: {
              totalEvents: 0,
              uniqueUsers: 0,
              criticalEvents: 0
            }
          });
        }
        grid.push(dayData);
      }
      return grid;
    }
    
    // For day/week granularity, create a simple grid
    return [data];
  }, [data, granularity, dateRange]);

  const maxValue = useMemo(() => {
    return Math.max(...data.map(d => d.value), 1);
  }, [data]);

  if (isLoading) {
    return (
      <Paper sx={{ p: 2, height }}>
        <Skeleton variant="rectangular" height={height - 32} />
      </Paper>
    );
  }

  if (!data || data.length === 0) {
    return (
      <Paper sx={{ p: 2, height, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <Typography color="text.secondary">
          Keine Aktivitätsdaten verfügbar
        </Typography>
      </Paper>
    );
  }

  return (
    <Paper sx={{ p: 2, height, overflow: 'auto' }}>
      {/* Header */}
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
        <Typography 
          variant="h6"
          sx={{ 
            fontFamily: 'Antonio, sans-serif',
            fontWeight: 'bold'
          }}
        >
          Aktivitäts-Heatmap
        </Typography>
        
        <Box display="flex" gap={1} alignItems="center">
          <FormControl size="small" sx={{ minWidth: 120 }}>
            <InputLabel>Granularität</InputLabel>
            <Select
              value={granularity}
              label="Granularität"
              disabled // For now, controlled by parent
            >
              <MenuItem value="hour">Stündlich</MenuItem>
              <MenuItem value="day">Täglich</MenuItem>
              <MenuItem value="week">Wöchentlich</MenuItem>
            </Select>
          </FormControl>
          
          <Tooltip title="Info">
            <IconButton size="small">
              <InfoIcon />
            </IconButton>
          </Tooltip>
        </Box>
      </Box>

      {/* Legend */}
      <Box display="flex" gap={2} mb={2} alignItems="center">
        <Typography variant="caption" color="text.secondary">
          Wenig
        </Typography>
        <Box display="flex" gap={0.5}>
          {[0.0, 0.2, 0.4, 0.6, 0.8, 1.0].map((intensity, index) => (
            <Box
              key={index}
              sx={{
                width: 20,
                height: 20,
                bgcolor: getIntensityColor(intensity * maxValue, maxValue),
                border: '1px solid',
                borderColor: 'divider',
                borderRadius: 0.5
              }}
            />
          ))}
        </Box>
        <Typography variant="caption" color="text.secondary">
          Viel
        </Typography>
        
        <Box sx={{ ml: 'auto' }}>
          <Chip 
            label={`Max: ${maxValue} Events`} 
            size="small" 
            variant="outlined"
            sx={{ fontFamily: 'Poppins, sans-serif' }}
          />
        </Box>
      </Box>

      {/* Heatmap Grid */}
      {granularity === 'hour' && (
        <Box sx={{ overflowX: 'auto' }}>
          <Box sx={{ minWidth: 800 }}>
            {/* Hour labels */}
            <Box display="flex" mb={1}>
              <Box sx={{ width: 100 }} />
              {Array.from({ length: 24 }, (_, i) => (
                <Box 
                  key={i} 
                  sx={{ 
                    flex: 1, 
                    textAlign: 'center',
                    fontSize: '0.75rem',
                    color: 'text.secondary'
                  }}
                >
                  {i}
                </Box>
              ))}
            </Box>

            {/* Grid rows */}
            {heatmapGrid.map((row, dayIndex) => (
              <Box key={dayIndex} display="flex" mb={0.5}>
                {/* Day label */}
                <Box 
                  sx={{ 
                    width: 100, 
                    display: 'flex', 
                    alignItems: 'center',
                    pr: 1
                  }}
                >
                  <Typography variant="caption" noWrap>
                    {row[0] && format(row[0].timestamp, 'EEE dd.MM', { locale: de })}
                  </Typography>
                </Box>
                
                {/* Hour cells */}
                {row.map((cell, hourIndex) => (
                  <Tooltip
                    key={hourIndex}
                    title={
                      <Box>
                        <Typography variant="caption">
                          {format(cell.timestamp, 'dd.MM.yyyy HH:mm', { locale: de })}
                        </Typography>
                        <br />
                        <Typography variant="caption">
                          Events: {cell.details?.totalEvents || 0}
                        </Typography>
                        <br />
                        <Typography variant="caption">
                          Benutzer: {cell.details?.uniqueUsers || 0}
                        </Typography>
                        {cell.details?.criticalEvents ? (
                          <>
                            <br />
                            <Typography variant="caption" color="error">
                              Kritisch: {cell.details.criticalEvents}
                            </Typography>
                          </>
                        ) : null}
                      </Box>
                    }
                  >
                    <Box
                      onClick={() => onCellClick?.(cell)}
                      sx={{
                        flex: 1,
                        height: 30,
                        bgcolor: getIntensityColor(cell.value, maxValue),
                        border: '1px solid',
                        borderColor: 'divider',
                        cursor: cell.value > 0 ? 'pointer' : 'default',
                        position: 'relative',
                        transition: 'all 0.2s',
                        '&:hover': cell.value > 0 ? {
                          transform: 'scale(1.1)',
                          zIndex: 1,
                          boxShadow: 2
                        } : {},
                        // Overlay for critical events
                        '&::after': cell.details?.criticalEvents ? {
                          content: '""',
                          position: 'absolute',
                          top: 0,
                          right: 0,
                          width: 8,
                          height: 8,
                          bgcolor: 'error.main',
                          borderRadius: '50%'
                        } : {}
                      }}
                    />
                  </Tooltip>
                ))}
              </Box>
            ))}
          </Box>
        </Box>
      )}

      {/* Simple view for day/week granularity */}
      {granularity !== 'hour' && (
        <Box display="flex" flexWrap="wrap" gap={0.5}>
          {data.map((point, index) => (
            <Tooltip
              key={index}
              title={
                <Box>
                  <Typography variant="caption">
                    {format(point.timestamp, 'PPpp', { locale: de })}
                  </Typography>
                  <br />
                  <Typography variant="caption">
                    Events: {point.value}
                  </Typography>
                </Box>
              }
            >
              <Box
                onClick={() => onCellClick?.(point)}
                sx={{
                  width: 40,
                  height: 40,
                  bgcolor: getIntensityColor(point.value, maxValue),
                  border: '1px solid',
                  borderColor: 'divider',
                  borderRadius: 1,
                  cursor: 'pointer',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  transition: 'all 0.2s',
                  '&:hover': {
                    transform: 'scale(1.1)',
                    zIndex: 1,
                    boxShadow: 2
                  }
                }}
              >
                <Typography variant="caption" sx={{ fontSize: '0.65rem' }}>
                  {point.value > 999 ? `${Math.floor(point.value / 1000)}k` : point.value}
                </Typography>
              </Box>
            </Tooltip>
          ))}
        </Box>
      )}

      {/* Summary Stats */}
      <Box 
        display="flex" 
        gap={2} 
        mt={2} 
        pt={2} 
        borderTop={1} 
        borderColor="divider"
      >
        <Box>
          <Typography variant="caption" color="text.secondary">
            Gesamt Events
          </Typography>
          <Typography 
            variant="h6"
            sx={{ 
              fontFamily: 'Antonio, sans-serif',
              fontWeight: 'bold'
            }}
          >
            {data.reduce((sum, d) => sum + d.value, 0).toLocaleString('de-DE')}
          </Typography>
        </Box>
        
        <Box>
          <Typography variant="caption" color="text.secondary">
            Peak Zeit
          </Typography>
          <Typography 
            variant="h6"
            sx={{ 
              fontFamily: 'Antonio, sans-serif',
              fontWeight: 'bold'
            }}
          >
            {data.length > 0 && 
              format(
                data.reduce((max, d) => d.value > max.value ? d : max).timestamp,
                'HH:mm',
                { locale: de }
              )
            }
          </Typography>
        </Box>
        
        <Box>
          <Typography variant="caption" color="text.secondary">
            Durchschnitt
          </Typography>
          <Typography 
            variant="h6"
            sx={{ 
              fontFamily: 'Antonio, sans-serif',
              fontWeight: 'bold'
            }}
          >
            {Math.round(data.reduce((sum, d) => sum + d.value, 0) / Math.max(data.length, 1))}
          </Typography>
        </Box>
      </Box>
    </Paper>
  );
};