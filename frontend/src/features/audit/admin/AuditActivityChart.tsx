import React, { useEffect, useState } from 'react';
import {
  Area,
  AreaChart,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Legend,
} from 'recharts';
import { Box, CircularProgress, Typography, ToggleButton, ToggleButtonGroup } from '@mui/material';
import { auditApi } from '../services/auditApi';
import type { ActivityChartData } from '../types';

interface AuditActivityChartProps {
  days?: number;
  height?: number;
  showLegend?: boolean;
}

export const AuditActivityChart: React.FC<AuditActivityChartProps> = ({
  days = 7,
  height = 250,
  showLegend = false,
}) => {
  const [data, setData] = useState<ActivityChartData[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [viewMode, setViewMode] = useState<'hour' | 'day'>('hour');

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      setError(null);
      try {
        const chartData = await auditApi.getActivityChartData(days, viewMode);
        setData(chartData);
      } catch (_err) {
        void _err;
        setError('Fehler beim Laden der Aktivitätsdaten');
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [days, viewMode]);

  const handleViewModeChange = (
    _: React.MouseEvent<HTMLElement>,
    newMode: 'hour' | 'day' | null
  ) => {
    if (newMode !== null) {
      setViewMode(newMode);
    }
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height }}>
        <CircularProgress sx={{ color: '#004F7B' }} />
      </Box>
    );
  }

  if (error) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height }}>
        <Typography color="error">{error}</Typography>
      </Box>
    );
  }

  if (!data || data.length === 0) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height }}>
        <Typography color="text.secondary">Keine Aktivitätsdaten verfügbar</Typography>
      </Box>
    );
  }

  // Format data for chart
  const chartData = data.map(item => ({
    name: item.time || item.period || 'Unknown',
    events: item.value || item.count || 0,
    // Add additional metrics if available
    critical: item.critical || 0,
    users: item.users || 0,
  }));

  return (
    <Box>
      {/* View Mode Toggle */}
      <Box sx={{ display: 'flex', justifyContent: 'flex-end', mb: 2 }}>
        <ToggleButtonGroup value={viewMode} exclusive onChange={handleViewModeChange} size="small">
          <ToggleButton value="hour" sx={{ textTransform: 'none' }}>
            Stündlich
          </ToggleButton>
          <ToggleButton value="day" sx={{ textTransform: 'none' }}>
            Täglich
          </ToggleButton>
        </ToggleButtonGroup>
      </Box>

      {/* Chart */}
      <ResponsiveContainer width="100%" height={height}>
        <AreaChart data={chartData} margin={{ top: 5, right: 5, left: 5, bottom: 5 }}>
          <defs>
            <linearGradient id="colorEvents" x1="0" y1="0" x2="0" y2="1">
              <stop offset="5%" stopColor="#004F7B" stopOpacity={0.8} />
              <stop offset="95%" stopColor="#004F7B" stopOpacity={0.1} />
            </linearGradient>
            <linearGradient id="colorCritical" x1="0" y1="0" x2="0" y2="1">
              <stop offset="5%" stopColor="#f44336" stopOpacity={0.8} />
              <stop offset="95%" stopColor="#f44336" stopOpacity={0.1} />
            </linearGradient>
          </defs>
          <CartesianGrid strokeDasharray="3 3" stroke="#e0e0e0" />
          <XAxis
            dataKey="name"
            tick={{ fontSize: 12 }}
            stroke="#666"
            interval={viewMode === 'hour' ? 3 : 0}
          />
          <YAxis tick={{ fontSize: 12 }} stroke="#666" />
          <Tooltip
            contentStyle={{
              backgroundColor: 'rgba(255, 255, 255, 0.95)',
              border: '1px solid #e0e0e0',
              borderRadius: 8,
            }}
            labelStyle={{ color: '#333', fontWeight: 'bold' }}
          />
          {showLegend && <Legend wrapperStyle={{ paddingTop: '20px' }} iconType="rect" />}
          <Area
            type="monotone"
            dataKey="events"
            stroke="#004F7B"
            strokeWidth={2}
            fillOpacity={1}
            fill="url(#colorEvents)"
            name="Ereignisse"
          />
          {data[0]?.critical !== undefined && (
            <Area
              type="monotone"
              dataKey="critical"
              stroke="#f44336"
              strokeWidth={2}
              fillOpacity={1}
              fill="url(#colorCritical)"
              name="Kritisch"
            />
          )}
        </AreaChart>
      </ResponsiveContainer>
    </Box>
  );
};
