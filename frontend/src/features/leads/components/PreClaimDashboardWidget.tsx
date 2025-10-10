/**
 * PreClaimDashboardWidget - Dashboard Widget für Pre-Claim Übersicht
 *
 * Variante B Pre-Claim Logic (Handelsvertretervertrag §2(8)(a)):
 * - Zeigt Metriken: Anzahl Pre-Claims, überfällige Pre-Claims, diese Woche ablaufend
 * - Liste der überfälligen Leads (max. 5, sortiert nach Deadline)
 * - Button "Alle Pre-Claims anzeigen" → filtert LeadList
 *
 * FreshFoodz CI: #94C456 (Grün), #FF9800 (Orange), #F44336 (Rot)
 */

import React, { useEffect, useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Grid,
  Chip,
  List,
  ListItem,
  ListItemText,
  Button,
  CircularProgress,
  Alert,
} from '@mui/material';
import { CheckCircle as CheckCircleIcon } from '@mui/icons-material';
import { listLeads } from '../api';
import type { Problem } from '../types';

interface PreClaimMetrics {
  totalPreClaims: number;
  overdue: number;
  expiringThisWeek: number;
}

interface OverdueLead {
  id: string;
  companyName: string;
  registeredAt: string;
  deadline: Date;
  daysOverdue: number;
}

const PreClaimDashboardWidget: React.FC = () => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Problem | null>(null);
  const [metrics, setMetrics] = useState<PreClaimMetrics>({
    totalPreClaims: 0,
    overdue: 0,
    expiringThisWeek: 0,
  });
  const [overdueLeads, setOverdueLeads] = useState<OverdueLead[]>([]);

  useEffect(() => {
    const fetchPreClaimData = async () => {
      try {
        setLoading(true);
        setError(null);

        const leads = await listLeads();

        // Variante B: Pre-Claim Detection (firstContactDocumentedAt === null)
        const preClaimLeads = leads.filter(lead => !lead.firstContactDocumentedAt);

        const now = new Date();
        const oneWeekFromNow = new Date();
        oneWeekFromNow.setDate(now.getDate() + 7);

        const overdueList: OverdueLead[] = [];
        let overdueCount = 0;
        let expiringThisWeekCount = 0;

        preClaimLeads.forEach(lead => {
          const registeredDate = new Date(lead.registeredAt);
          const deadline = new Date(registeredDate);
          deadline.setDate(deadline.getDate() + 10); // 10 Tage Frist

          const daysRemaining = Math.ceil((deadline.getTime() - now.getTime()) / (1000 * 60 * 60 * 24));

          if (daysRemaining < 0) {
            // Überfällig
            overdueCount++;
            overdueList.push({
              id: lead.id,
              companyName: lead.companyName,
              registeredAt: lead.registeredAt,
              deadline,
              daysOverdue: Math.abs(daysRemaining),
            });
          } else if (deadline <= oneWeekFromNow) {
            // Diese Woche ablaufend
            expiringThisWeekCount++;
          }
        });

        // Sortiere überfällige Leads nach Deadline (älteste zuerst)
        overdueList.sort((a, b) => a.deadline.getTime() - b.deadline.getTime());

        setMetrics({
          totalPreClaims: preClaimLeads.length,
          overdue: overdueCount,
          expiringThisWeek: expiringThisWeekCount,
        });
        setOverdueLeads(overdueList.slice(0, 5)); // Max 5 Leads
      } catch (err) {
        setError(err as Problem);
      } finally {
        setLoading(false);
      }
    };

    fetchPreClaimData();
  }, []);

  if (loading) {
    return (
      <Card>
        <CardContent>
          <Box display="flex" justifyContent="center" alignItems="center" minHeight="200px">
            <CircularProgress />
          </Box>
        </CardContent>
      </Card>
    );
  }

  if (error) {
    return (
      <Card>
        <CardContent>
          <Alert severity="error">
            {error.title ?? 'Fehler'}
            {error.detail ? ` – ${error.detail}` : ''}
          </Alert>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardContent>
        <Typography variant="h6" gutterBottom sx={{ fontWeight: 600, mb: 2 }}>
          Pre-Claim Übersicht
        </Typography>

        {/* Metriken */}
        <Grid container spacing={2} sx={{ mb: 3 }}>
          <Grid size={{ xs: 12, md: 4 }}>
            <Box
              sx={{
                p: 2,
                bgcolor: '#E3F2FD',
                borderRadius: 1,
                border: '1px solid #2196F3',
                textAlign: 'center',
              }}
            >
              <Typography variant="h4" sx={{ color: '#2196F3', fontWeight: 700 }}>
                {metrics.totalPreClaims}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Aktive Pre-Claims
              </Typography>
            </Box>
          </Grid>

          <Grid size={{ xs: 12, md: 4 }}>
            <Box
              sx={{
                p: 2,
                bgcolor: '#FFF3E0',
                borderRadius: 1,
                border: '1px solid #FF9800',
                textAlign: 'center',
              }}
            >
              <Typography variant="h4" sx={{ color: '#FF9800', fontWeight: 700 }}>
                {metrics.expiringThisWeek}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Ablauf diese Woche
              </Typography>
            </Box>
          </Grid>

          <Grid size={{ xs: 12, md: 4 }}>
            <Box
              sx={{
                p: 2,
                bgcolor: '#FFEBEE',
                borderRadius: 1,
                border: '1px solid #F44336',
                textAlign: 'center',
              }}
            >
              <Typography variant="h4" sx={{ color: '#F44336', fontWeight: 700 }}>
                {metrics.overdue}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Überfällig
              </Typography>
            </Box>
          </Grid>
        </Grid>

        {/* Überfällige Leads */}
        {overdueLeads.length > 0 ? (
          <>
            <Typography variant="subtitle2" gutterBottom sx={{ fontWeight: 600, mb: 1 }}>
              ⚠️ Überfällige Pre-Claims (Erstkontakt dokumentieren!)
            </Typography>
            <List dense sx={{ bgcolor: '#FFEBEE', borderRadius: 1, mb: 2 }}>
              {overdueLeads.map(lead => (
                <ListItem key={lead.id}>
                  <ListItemText
                    primary={
                      <Box display="flex" alignItems="center" gap={1}>
                        <Typography variant="body2" sx={{ fontWeight: 600 }}>
                          {lead.companyName}
                        </Typography>
                        <Chip
                          label={`${lead.daysOverdue}d überfällig`}
                          size="small"
                          sx={{
                            bgcolor: '#F44336',
                            color: '#fff',
                            fontWeight: 600,
                            fontSize: '0.7rem',
                          }}
                        />
                      </Box>
                    }
                    secondary={
                      <Typography variant="caption" color="text.secondary">
                        Frist war: {lead.deadline.toLocaleDateString('de-DE')} (Registriert:{' '}
                        {new Date(lead.registeredAt).toLocaleDateString('de-DE')})
                      </Typography>
                    }
                  />
                </ListItem>
              ))}
            </List>
          </>
        ) : (
          <Box
            sx={{
              p: 2,
              bgcolor: '#E8F5E9',
              borderRadius: 1,
              border: '1px solid #94C456',
              textAlign: 'center',
              mb: 2,
            }}
          >
            <CheckCircleIcon sx={{ color: '#94C456', fontSize: 40, mb: 1 }} />
            <Typography variant="body2" color="text.secondary">
              Keine überfälligen Pre-Claims. Gut gemacht!
            </Typography>
          </Box>
        )}

        {/* Button "Alle Pre-Claims anzeigen" */}
        {metrics.totalPreClaims > 0 && (
          <Button
            variant="outlined"
            fullWidth
            onClick={() => {
              // TODO: Navigate to LeadList with Pre-Claim filter
              // This will require adding a filter state to LeadList
              window.location.href = '/leads?filter=pre-claim';
            }}
            sx={{
              borderColor: '#2196F3',
              color: '#2196F3',
              '&:hover': {
                borderColor: '#1976D2',
                bgcolor: '#E3F2FD',
              },
            }}
          >
            Alle Pre-Claims anzeigen ({metrics.totalPreClaims})
          </Button>
        )}
      </CardContent>
    </Card>
  );
};

export default PreClaimDashboardWidget;
