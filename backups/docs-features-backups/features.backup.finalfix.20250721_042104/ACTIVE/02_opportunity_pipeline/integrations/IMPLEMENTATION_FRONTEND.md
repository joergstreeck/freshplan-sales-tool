# FC-011: Bonitätsprüfung - Frontend Implementation

**Fokus:** React Components, UI/UX, Real-time Updates

## 🎨 Credit Check UI Components

### Credit Score Display

```typescript
// components/CreditScoreDisplay.tsx
interface CreditScoreDisplayProps {
  score: number;
  riskLevel: 'LOW' | 'MEDIUM' | 'HIGH';
  validUntil: Date;
  onRefresh?: () => void;
}

export const CreditScoreDisplay: React.FC<CreditScoreDisplayProps> = ({
  score,
  riskLevel,
  validUntil,
  onRefresh
}) => {
  const getScoreColor = (score: number) => {
    if (score >= 700) return '#4CAF50'; // Green
    if (score >= 500) return '#FF9800'; // Orange
    return '#F44336'; // Red
  };

  const getRiskIcon = (level: string) => {
    switch (level) {
      case 'LOW': return <CheckCircleIcon color="success" />;
      case 'MEDIUM': return <WarningIcon color="warning" />;
      case 'HIGH': return <ErrorIcon color="error" />;
    }
  };

  const isExpiringSoon = () => {
    const daysUntilExpiry = differenceInDays(validUntil, new Date());
    return daysUntilExpiry <= 7;
  };

  return (
    <Card sx={{ position: 'relative', overflow: 'visible' }}>
      {isExpiringSoon() && (
        <Chip
          label="Läuft bald ab"
          color="warning"
          size="small"
          sx={{
            position: 'absolute',
            top: -10,
            right: 10,
            zIndex: 1
          }}
        />
      )}
      
      <CardContent>
        <Box display="flex" alignItems="center" gap={2}>
          <CircularProgress
            variant="determinate"
            value={(score / 850) * 100}
            size={80}
            thickness={8}
            sx={{
              color: getScoreColor(score),
              '& .MuiCircularProgress-circle': {
                strokeLinecap: 'round',
              }
            }}
          />
          
          <Box flex={1}>
            <Typography variant="h4" component="div">
              {score}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Kredit-Score
            </Typography>
          </Box>
          
          <Box textAlign="center">
            {getRiskIcon(riskLevel)}
            <Typography variant="body2" mt={1}>
              {riskLevel} Risk
            </Typography>
          </Box>
        </Box>
        
        <Divider sx={{ my: 2 }} />
        
        <Box display="flex" justifyContent="space-between" alignItems="center">
          <Typography variant="caption" color="text.secondary">
            Gültig bis: {format(validUntil, 'dd.MM.yyyy')}
          </Typography>
          
          {onRefresh && (
            <Tooltip title="Bonitätsprüfung aktualisieren">
              <IconButton size="small" onClick={onRefresh}>
                <RefreshIcon />
              </IconButton>
            </Tooltip>
          )}
        </Box>
      </CardContent>
    </Card>
  );
};
```

### Credit Check Button

```typescript
// components/CreditCheckButton.tsx
export const CreditCheckButton: React.FC<{
  customerId: string;
  opportunityId?: string;
  onComplete: (result: CreditCheckResult) => void;
}> = ({ customerId, opportunityId, onComplete }) => {
  const [checking, setChecking] = useState(false);
  const [progress, setProgress] = useState(0);
  const { enqueueSnackbar } = useSnackbar();

  const performCheck = async () => {
    setChecking(true);
    setProgress(10);
    
    try {
      // Simulate progress
      const progressInterval = setInterval(() => {
        setProgress(prev => Math.min(prev + 20, 90));
      }, 500);
      
      const result = await apiClient.post('/api/credit-check', {
        customerId,
        opportunityId,
        forceRefresh: true
      });
      
      clearInterval(progressInterval);
      setProgress(100);
      
      enqueueSnackbar('Bonitätsprüfung abgeschlossen', { 
        variant: 'success' 
      });
      
      onComplete(result.data);
    } catch (error) {
      enqueueSnackbar('Fehler bei der Bonitätsprüfung', { 
        variant: 'error' 
      });
    } finally {
      setChecking(false);
      setTimeout(() => setProgress(0), 500);
    }
  };

  return (
    <Box position="relative" display="inline-block">
      <Button
        variant="contained"
        onClick={performCheck}
        disabled={checking}
        startIcon={checking ? <CircularProgress size={20} /> : <SecurityIcon />}
        sx={{
          backgroundColor: '#004F7B',
          '&:hover': {
            backgroundColor: '#003A5C'
          }
        }}
      >
        {checking ? 'Prüfung läuft...' : 'Bonität prüfen'}
      </Button>
      
      {checking && (
        <LinearProgress
          variant="determinate"
          value={progress}
          sx={{
            position: 'absolute',
            bottom: 0,
            left: 0,
            right: 0,
            height: 3,
            borderBottomLeftRadius: 4,
            borderBottomRightRadius: 4
          }}
        />
      )}
    </Box>
  );
};
```

### Credit Details Modal

```typescript
// components/CreditDetailsModal.tsx
export const CreditDetailsModal: React.FC<{
  open: boolean;
  onClose: () => void;
  creditCheck: CreditCheckResult;
}> = ({ open, onClose, creditCheck }) => {
  const theme = useTheme();
  const fullScreen = useMediaQuery(theme.breakpoints.down('md'));

  return (
    <Dialog
      open={open}
      onClose={onClose}
      fullScreen={fullScreen}
      maxWidth="md"
      fullWidth
    >
      <DialogTitle>
        <Box display="flex" alignItems="center" gap={2}>
          <SecurityIcon />
          <Typography variant="h6">
            Bonitätsprüfung Details
          </Typography>
          <Box flex={1} />
          <IconButton edge="end" onClick={onClose}>
            <CloseIcon />
          </IconButton>
        </Box>
      </DialogTitle>
      
      <DialogContent dividers>
        <Grid container spacing={3}>
          {/* Score Overview */}
          <Grid item xs={12} md={4}>
            <CreditScoreDisplay
              score={creditCheck.score}
              riskLevel={creditCheck.riskLevel}
              validUntil={creditCheck.validUntil}
            />
          </Grid>
          
          {/* Key Metrics */}
          <Grid item xs={12} md={8}>
            <Grid container spacing={2}>
              <Grid item xs={6}>
                <MetricCard
                  title="Kreditlimit"
                  value={formatCurrency(creditCheck.creditLimit)}
                  icon={<AccountBalanceIcon />}
                  color="primary"
                />
              </Grid>
              <Grid item xs={6}>
                <MetricCard
                  title="Zahlungsziel"
                  value={creditCheck.paymentTerms}
                  icon={<EventIcon />}
                  color="secondary"
                />
              </Grid>
              <Grid item xs={6}>
                <MetricCard
                  title="Offene Forderungen"
                  value={formatCurrency(creditCheck.openClaims || 0)}
                  icon={<ReceiptIcon />}
                  color={creditCheck.openClaims > 0 ? 'warning' : 'success'}
                />
              </Grid>
              <Grid item xs={6}>
                <MetricCard
                  title="Letzte Prüfung"
                  value={format(creditCheck.checkDate, 'dd.MM.yyyy HH:mm')}
                  icon={<UpdateIcon />}
                  color="info"
                />
              </Grid>
            </Grid>
          </Grid>
          
          {/* Risk Factors */}
          <Grid item xs={12}>
            <Typography variant="h6" gutterBottom>
              Risikofaktoren
            </Typography>
            <List>
              {creditCheck.riskFactors.map((factor, index) => (
                <ListItem key={index}>
                  <ListItemIcon>
                    {factor.severity === 'HIGH' ? (
                      <ErrorIcon color="error" />
                    ) : factor.severity === 'MEDIUM' ? (
                      <WarningIcon color="warning" />
                    ) : (
                      <InfoIcon color="info" />
                    )}
                  </ListItemIcon>
                  <ListItemText
                    primary={factor.description}
                    secondary={factor.recommendation}
                  />
                </ListItem>
              ))}
            </List>
          </Grid>
          
          {/* History Chart */}
          <Grid item xs={12}>
            <Typography variant="h6" gutterBottom>
              Score-Verlauf
            </Typography>
            <CreditScoreChart customerId={creditCheck.customerId} />
          </Grid>
        </Grid>
      </DialogContent>
      
      <DialogActions>
        <Button onClick={onClose}>
          Schließen
        </Button>
        <Button
          variant="contained"
          onClick={() => {
            window.print();
          }}
          startIcon={<PrintIcon />}
        >
          Drucken
        </Button>
      </DialogActions>
    </Dialog>
  );
};
```

### Pipeline Integration

```typescript
// components/OpportunityPipeline/StageGate.tsx
export const CreditCheckGate: React.FC<{
  opportunity: Opportunity;
  onPass: () => void;
  onFail: (reason: string) => void;
}> = ({ opportunity, onPass, onFail }) => {
  const { data: creditCheck, mutate } = useSWR(
    `/api/credit-check/customer/${opportunity.customerId}`,
    fetcher
  );

  const handleCheckComplete = (result: CreditCheckResult) => {
    mutate(result);
    
    if (result.score >= 500 && result.riskLevel !== 'HIGH') {
      onPass();
    } else {
      onFail('Bonitätsprüfung nicht bestanden');
    }
  };

  if (!creditCheck) {
    return (
      <Alert severity="warning" sx={{ mb: 2 }}>
        <AlertTitle>Bonitätsprüfung erforderlich</AlertTitle>
        Bevor Sie zur Angebotsphase wechseln können, muss eine 
        Bonitätsprüfung durchgeführt werden.
        <Box mt={2}>
          <CreditCheckButton
            customerId={opportunity.customerId}
            opportunityId={opportunity.id}
            onComplete={handleCheckComplete}
          />
        </Box>
      </Alert>
    );
  }

  const canProceed = creditCheck.score >= 500 && 
                     creditCheck.riskLevel !== 'HIGH' &&
                     !creditCheck.isExpired;

  return (
    <Paper sx={{ p: 2, mb: 2 }}>
      <Box display="flex" alignItems="center" gap={2}>
        <Box flex={1}>
          <Typography variant="subtitle1">
            Bonitätsprüfung
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Score: {creditCheck.score} | Risiko: {creditCheck.riskLevel}
          </Typography>
        </Box>
        
        {canProceed ? (
          <Chip
            label="Bestanden"
            color="success"
            icon={<CheckCircleIcon />}
          />
        ) : (
          <Chip
            label="Manuelle Prüfung"
            color="warning"
            icon={<WarningIcon />}
          />
        )}
      </Box>
      
      {!canProceed && (
        <Alert severity="warning" sx={{ mt: 2 }}>
          Diese Opportunity benötigt eine manuelle Freigabe durch 
          das Credit Management.
          <Button
            size="small"
            sx={{ mt: 1 }}
            onClick={() => requestManualReview(opportunity.id)}
          >
            Freigabe anfordern
          </Button>
        </Alert>
      )}
    </Paper>
  );
};
```

## 📊 Real-time Updates

```typescript
// hooks/useCreditCheckSubscription.ts
export const useCreditCheckSubscription = (customerId: string) => {
  const { mutate } = useSWRConfig();
  
  useEffect(() => {
    const eventSource = new EventSource(
      `/api/credit-check/subscribe/${customerId}`
    );
    
    eventSource.onmessage = (event) => {
      const update = JSON.parse(event.data);
      
      // Update cache
      mutate(
        `/api/credit-check/customer/${customerId}`,
        update,
        false
      );
      
      // Show notification if score changed significantly
      if (Math.abs(update.scoreDelta) > 50) {
        showNotification({
          type: update.scoreDelta > 0 ? 'success' : 'warning',
          message: `Kredit-Score geändert: ${update.score} (${
            update.scoreDelta > 0 ? '+' : ''
          }${update.scoreDelta})`
        });
      }
    };
    
    return () => eventSource.close();
  }, [customerId, mutate]);
};
```