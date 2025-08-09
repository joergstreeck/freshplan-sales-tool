import React, { useEffect } from 'react';
import {
  Paper,
  Typography,
  Box,
  LinearProgress,
  Chip,
  Skeleton
} from '@mui/material';
import Grid from '@mui/material/Unstable_Grid2';
import {
  TrendingUp as TrendingUpIcon,
  TrendingDown as TrendingDownIcon,
  Security as SecurityIcon,
  Assignment as AssignmentIcon,
  People as PeopleIcon,
  Warning as WarningIcon
} from '@mui/icons-material';
import { useAuditAdminStore } from '@/store/admin/auditAdminStore';
import { AuditStatisticsCards } from './AuditStatisticsCards';
import { AuditActivityHeatmap } from './AuditActivityHeatmap';
import { AuditStreamMonitor } from './AuditStreamMonitor';
import type { AuditDashboardMetrics } from '../types';

interface StatCardProps {
  title: string;
  value: string | number;
  icon: React.ReactNode;
  trend?: {
    value: number;
    isUp: boolean;
  };
  color?: string;
  subtitle?: string;
}

const StatCard: React.FC<StatCardProps> = ({ 
  title, 
  value, 
  icon, 
  trend, 
  color = '#004F7B',
  subtitle 
}) => {
  return (
    <Paper sx={{ p: 2, height: '100%' }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
        <Box sx={{ 
          width: 48, 
          height: 48, 
          borderRadius: 2,
          bgcolor: `${color}15`,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center'
        }}>
          <Box sx={{ color }}>{icon}</Box>
        </Box>
        {trend && (
          <Chip
            size="small"
            icon={trend.isUp ? <TrendingUpIcon /> : <TrendingDownIcon />}
            label={`${trend.isUp ? '+' : ''}${trend.value}%`}
            color={trend.isUp ? 'success' : 'error'}
            variant="outlined"
          />
        )}
      </Box>
      
      <Typography 
        variant="h4" 
        sx={{ 
          fontFamily: 'Antonio, sans-serif',
          fontWeight: 'bold',
          color: '#333',
          mb: 0.5
        }}
      >
        {value}
      </Typography>
      
      <Typography 
        variant="subtitle2" 
        color="text.secondary"
        sx={{ fontFamily: 'Poppins, sans-serif' }}
      >
        {title}
      </Typography>
      
      {subtitle && (
        <Typography 
          variant="caption" 
          color="text.secondary"
          sx={{ display: 'block', mt: 0.5 }}
        >
          {subtitle}
        </Typography>
      )}
    </Paper>
  );
};

interface AuditDashboardProps {
  metrics?: AuditDashboardMetrics;
  dateRange: {
    from: Date;
    to: Date;
  };
  onDateRangeChange?: (range: { from: Date; to: Date }) => void;
}

export const AuditDashboard: React.FC<AuditDashboardProps> = ({ 
  metrics,
  dateRange 
}) => {
  if (!metrics) {
    return <Box>Keine Daten verfügbar</Box>;
  }
  
  return (
    <Grid container spacing={3}>
      {/* Statistics Cards */}
      <Grid size={{ xs: 12, sm: 6, md: 3 }}>
        <StatCard
          title="Ereignisse heute"
          value={metrics.totalEventsToday.toLocaleString('de-DE')}
          icon={<AssignmentIcon />}
          color="#004F7B"
          trend={{ value: 12, isUp: true }}
        />
      </Grid>
      
      <Grid size={{ xs: 12, sm: 6, md: 3 }}>
        <StatCard
          title="Aktive Benutzer"
          value={metrics.activeUsers}
          icon={<PeopleIcon />}
          color="#94C456"
          subtitle={`Heute aktiv`}
        />
      </Grid>
      
      <Grid size={{ xs: 12, sm: 6, md: 3 }}>
        <StatCard
          title="Kritische Ereignisse"
          value={metrics.criticalEventsToday}
          icon={<WarningIcon />}
          color={metrics.criticalEventsToday > 0 ? '#f44336' : '#4caf50'}
          trend={metrics.criticalEventsToday > 0 ? { value: 5, isUp: true } : undefined}
        />
      </Grid>
      
      <Grid size={{ xs: 12, sm: 6, md: 3 }}>
        <StatCard
          title="Audit Coverage"
          value={`${metrics.coverage}%`}
          icon={<SecurityIcon />}
          color="#ff9800"
        />
      </Grid>
      
      {/* Compliance Overview */}
      <Grid size={{ xs: 12, md: 8 }}>
        <Paper sx={{ p: 3 }}>
          <Typography 
            variant="h6" 
            gutterBottom
            sx={{ fontFamily: 'Antonio, sans-serif' }}
          >
            Compliance Status
          </Typography>
          
          <Grid container spacing={2}>
            <Grid size={{ xs: 12, sm: 6 }}>
              <Box sx={{ mb: 2 }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                  <Typography variant="body2">DSGVO Compliance</Typography>
                  <Typography variant="body2" fontWeight="bold">
                    {metrics.retentionCompliance}%
                  </Typography>
                </Box>
                <LinearProgress 
                  variant="determinate" 
                  value={metrics.retentionCompliance}
                  sx={{
                    height: 8,
                    borderRadius: 4,
                    bgcolor: '#e0e0e0',
                    '& .MuiLinearProgress-bar': {
                      bgcolor: metrics.retentionCompliance >= 80 ? '#94C456' : '#ff9800'
                    }
                  }}
                />
              </Box>
            </Grid>
            
            <Grid size={{ xs: 12, sm: 6 }}>
              <Box sx={{ mb: 2 }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                  <Typography variant="body2">Integrity Status</Typography>
                  <Typography variant="body2" fontWeight="bold">
                    {metrics.integrityStatus}
                  </Typography>
                </Box>
                <Chip
                  label={metrics.integrityStatus === 'valid' ? 'Gültig' : 'Prüfung erforderlich'}
                  color={metrics.integrityStatus === 'valid' ? 'success' : 'warning'}
                  size="small"
                />
              </Box>
            </Grid>
          </Grid>
        </Paper>
      </Grid>
      
      {/* Top Event Types */}
      <Grid size={{ xs: 12, md: 4 }}>
        <Paper sx={{ p: 3 }}>
          <Typography 
            variant="h6" 
            gutterBottom
            sx={{ fontFamily: 'Antonio, sans-serif' }}
          >
            Top Ereignistypen
          </Typography>
          
          <Box sx={{ mt: 2 }}>
            {metrics.topEventTypes?.slice(0, 5).map((event, index) => (
              <Box 
                key={event.type || event.eventType || `event-${index}`}
                sx={{ 
                  display: 'flex', 
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  py: 1,
                  borderBottom: index < 4 ? '1px solid #e0e0e0' : 'none'
                }}
              >
                <Typography variant="body2">
                  {event.type || event.eventType}
                </Typography>
                <Chip
                  label={event.count.toLocaleString('de-DE')}
                  size="small"
                  sx={{ 
                    bgcolor: '#004F7B15',
                    color: '#004F7B'
                  }}
                />
              </Box>
            ))}
          </Box>
        </Paper>
      </Grid>
      
      {/* Activity Timeline */}
      <Grid size={12}>
        <Paper sx={{ p: 3 }}>
          <Typography 
            variant="h6" 
            gutterBottom
            sx={{ fontFamily: 'Antonio, sans-serif' }}
          >
            Aktivitätsverlauf (7 Tage)
          </Typography>
          
          {/* Hier würde normalerweise ein Chart kommen */}
          <Box 
            sx={{ 
              height: 200, 
              display: 'flex', 
              alignItems: 'center', 
              justifyContent: 'center',
              bgcolor: '#f5f5f5',
              borderRadius: 1
            }}
          >
            <Typography color="text.secondary">
              Activity Chart Placeholder - Integration mit recharts/MUI Charts
            </Typography>
          </Box>
        </Paper>
      </Grid>
    </Grid>
  );
};