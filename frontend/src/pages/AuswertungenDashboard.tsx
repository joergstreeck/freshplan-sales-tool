import {
  Box,
  Typography,
  Card,
  CardContent,
  CardActions,
  Button,
  Paper,
  Chip,
  LinearProgress,
  useTheme,
} from '@mui/material';
import Grid from '@mui/material/Grid';
import { useNavigate } from 'react-router-dom';
import { MainLayoutV2 } from '../components/layout/MainLayoutV2';
import PieChartIcon from '@mui/icons-material/PieChart';
import TimelineIcon from '@mui/icons-material/Timeline';
import BarChartIcon from '@mui/icons-material/BarChart';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import TrendingDownIcon from '@mui/icons-material/TrendingDown';

export function AuswertungenDashboard() {
  const navigate = useNavigate();
  const theme = useTheme();

  const reports = [
    {
      title: 'Umsatzübersicht',
      description: 'Detaillierte Umsatzanalysen und Trends',
      icon: <BarChartIcon sx={{ fontSize: 56, color: theme.palette.primary.main }} />,
      path: '/berichte/umsatz',
      stats: '€1.8M',
      label: 'YTD',
      trend: '+12%',
      trendUp: true,
      progress: 75,
    },
    {
      title: 'Kundenanalyse',
      description: 'Kundenverhalten und Segmentierung',
      icon: <PieChartIcon sx={{ fontSize: 56, color: theme.palette.secondary.main }} />,
      path: '/berichte/kunden',
      stats: '1.247',
      label: 'Aktive Kunden',
      trend: '+8%',
      trendUp: true,
      progress: 60,
    },
    {
      title: 'Aktivitätsberichte',
      description: 'Team-Performance und Aktivitäten',
      icon: <TimelineIcon sx={{ fontSize: 56, color: theme.palette.warning.main }} />,
      path: '/berichte/aktivitaeten',
      stats: '3.4k',
      label: 'Diese Woche',
      trend: '-3%',
      trendUp: false,
      progress: 85,
    },
  ];

  return (
    <MainLayoutV2 maxWidth="full">
      <Box sx={{ py: 4 }}>
        <Box sx={{ mb: 4 }}>
          <Typography
            variant="h3"
            sx={{ mb: 1, fontFamily: 'Antonio, sans-serif', fontWeight: 'bold', color: theme.palette.secondary.main }}
          >
            Auswertungen & Berichte
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Umfassende Analysen und Berichte für datengetriebene Entscheidungen
          </Typography>
        </Box>

        <Grid container spacing={3}>
          {reports.map(report => (
            <Grid key={report.title} size={{ xs: 12, md: 4 }}>
              <Card
                sx={{
                  height: '100%',
                  display: 'flex',
                  flexDirection: 'column',
                  transition: 'all 0.2s',
                  cursor: 'pointer',
                  '&:hover': { boxShadow: 4, transform: 'translateY(-2px)' },
                }}
                onClick={() => navigate(report.path)}
              >
                <CardContent sx={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
                  <Box sx={{ textAlign: 'center', mb: 3 }}>{report.icon}</Box>
                  <Typography
                    variant="h5"
                    sx={{
                      mb: 1,
                      fontFamily: 'Antonio, sans-serif',
                      color: theme.palette.secondary.main,
                      textAlign: 'center',
                    }}
                  >
                    {report.title}
                  </Typography>
                  <Typography
                    variant="body2"
                    color="text.secondary"
                    sx={{ mb: 3, textAlign: 'center', minHeight: 40 }}
                  >
                    {report.description}
                  </Typography>
                  <Box sx={{ mt: 'auto' }}>
                    <Box
                      sx={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'baseline',
                        mb: 1,
                      }}
                    >
                      <Box>
                        <Typography
                          variant="h4"
                          sx={{ color: theme.palette.secondary.main, fontWeight: 'bold', display: 'inline' }}
                        >
                          {report.stats}
                        </Typography>
                        <Typography
                          variant="body2"
                          color="text.secondary"
                          sx={{ ml: 1, display: 'inline' }}
                        >
                          {report.label}
                        </Typography>
                      </Box>
                      <Chip
                        icon={report.trendUp ? <TrendingUpIcon /> : <TrendingDownIcon />}
                        label={report.trend}
                        size="small"
                        sx={{
                          backgroundColor: report.trendUp ? theme.palette.success.light + '40' : theme.palette.error.light + '40',
                          color: report.trendUp ? theme.palette.success.dark : theme.palette.error.dark,
                        }}
                      />
                    </Box>
                    <LinearProgress
                      variant="determinate"
                      value={report.progress}
                      sx={{
                        height: 8,
                        borderRadius: 4,
                        backgroundColor: theme.palette.grey[300],
                        '& .MuiLinearProgress-bar': {
                          backgroundColor: theme.palette.primary.main,
                          borderRadius: 4,
                        },
                      }}
                    />
                    <Typography
                      variant="caption"
                      color="text.secondary"
                      sx={{ display: 'block', mt: 0.5, textAlign: 'right' }}
                    >
                      {report.progress}% zum Ziel
                    </Typography>
                  </Box>
                </CardContent>
                <CardActions sx={{ px: 2, pb: 2 }}>
                  <Button
                    fullWidth
                    variant="contained"
                    sx={{ backgroundColor: theme.palette.primary.main, '&:hover': { backgroundColor: theme.palette.primary.dark } }}
                  >
                    Bericht öffnen
                  </Button>
                </CardActions>
              </Card>
            </Grid>
          ))}
        </Grid>

        <Paper sx={{ p: 3, mt: 4, backgroundColor: theme.palette.grey[100] }}>
          <Typography variant="h6" sx={{ mb: 2, color: theme.palette.secondary.main }}>
            Quick Stats
          </Typography>
          <Grid container spacing={2}>
            <Grid size={{ xs: 12, sm: 6, md: 3 }}>
              <Box sx={{ textAlign: 'center' }}>
                <Typography variant="h4" sx={{ color: theme.palette.primary.main, fontWeight: 'bold' }}>
                  +23%
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Umsatzwachstum
                </Typography>
              </Box>
            </Grid>
            <Grid size={{ xs: 12, sm: 6, md: 3 }}>
              <Box sx={{ textAlign: 'center' }}>
                <Typography variant="h4" sx={{ color: theme.palette.secondary.main, fontWeight: 'bold' }}>
                  89%
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Kundenzufriedenheit
                </Typography>
              </Box>
            </Grid>
            <Grid size={{ xs: 12, sm: 6, md: 3 }}>
              <Box sx={{ textAlign: 'center' }}>
                <Typography variant="h4" sx={{ color: theme.palette.warning.main, fontWeight: 'bold' }}>
                  4.2
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Avg. Aktivitäten/Tag
                </Typography>
              </Box>
            </Grid>
            <Grid size={{ xs: 12, sm: 6, md: 3 }}>
              <Box sx={{ textAlign: 'center' }}>
                <Typography variant="h4" sx={{ color: theme.palette.info.main, fontWeight: 'bold' }}>
                  15min
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Avg. Response Zeit
                </Typography>
              </Box>
            </Grid>
          </Grid>
        </Paper>
      </Box>
    </MainLayoutV2>
  );
}
