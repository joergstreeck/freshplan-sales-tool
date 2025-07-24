# FC-009: Frontend-Komponenten für Contract Renewal

**Parent:** [/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-24_TECH_CONCEPT_FC-009-contract-renewal-management.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-24_TECH_CONCEPT_FC-009-contract-renewal-management.md)  
**Fokus:** UI/UX Komponenten

## Kanban Board Erweiterung

### Neue RENEWAL Stage

```typescript
// Erweiterung in types/opportunity.types.ts
export enum OpportunityStage {
  LEAD = "lead",
  QUALIFIED = "qualified",
  PROPOSAL = "proposal",
  NEGOTIATION = "negotiation",
  CLOSED_WON = "closed_won",
  CLOSED_LOST = "closed_lost",
  RENEWAL = "renewal" // NEU
}

// Stage-Konfiguration
export const RENEWAL_STAGE_CONFIG: StageConfig = {
  stage: OpportunityStage.RENEWAL,
  label: "Vertragsverlängerung",
  color: "#FF9800", // Orange
  icon: <RenewalIcon />,
  allowedActions: ["complete_renewal", "send_reminder", "update_contract"],
  dropDisabled: false
};
```

## Contract Status Badge

```typescript
interface ContractBadgeProps {
  opportunity: Opportunity;
  contractMonitoring?: ContractMonitoring;
}

export const ContractBadge: React.FC<ContractBadgeProps> = ({ 
  opportunity, 
  contractMonitoring 
}) => {
  if (!contractMonitoring) return null;
  
  const daysUntilExpiry = calculateDaysUntil(contractMonitoring.contractEnd);
  
  const getConfig = () => {
    if (daysUntilExpiry < 0) {
      return {
        color: "error" as const,
        icon: <ErrorIcon />,
        label: "Vertrag abgelaufen"
      };
    }
    if (daysUntilExpiry <= 30) {
      return {
        color: "error" as const,
        icon: <TimerIcon />,
        label: `${daysUntilExpiry} Tage`
      };
    }
    if (daysUntilExpiry <= 90) {
      return {
        color: "warning" as const,
        icon: <TimerIcon />,
        label: `${daysUntilExpiry} Tage`
      };
    }
    return {
      color: "success" as const,
      icon: <CheckCircleIcon />,
      label: "Vertrag aktiv"
    };
  };
  
  const config = getConfig();
  
  return (
    <Tooltip title={`Vertrag läuft am ${formatDate(contractMonitoring.contractEnd)} ab`}>
      <Chip
        size="small"
        color={config.color}
        icon={config.icon}
        label={config.label}
        sx={{
          fontWeight: 'bold',
          '& .MuiChip-icon': {
            fontSize: '1rem'
          }
        }}
      />
    </Tooltip>
  );
};
```

## Quick Action Buttons

```typescript
interface RenewalActionsProps {
  opportunity: Opportunity;
  onAction: (action: string) => void;
}

export const RenewalActions: React.FC<RenewalActionsProps> = ({ 
  opportunity, 
  onAction 
}) => {
  const { contractMonitoring } = useContractMonitoring(opportunity.customerId);
  
  if (!contractMonitoring || opportunity.stage !== OpportunityStage.RENEWAL) {
    return null;
  }
  
  return (
    <Box sx={{ mt: 1, display: 'flex', gap: 1 }}>
      <Button
        size="small"
        variant="contained"
        color="primary"
        startIcon={<CheckIcon />}
        onClick={() => onAction('complete_renewal')}
        sx={{
          backgroundColor: FRESHFOODZ_GREEN,
          '&:hover': {
            backgroundColor: darken(FRESHFOODZ_GREEN, 0.1)
          }
        }}
      >
        Verlängerung abschließen
      </Button>
      
      <Button
        size="small"
        variant="outlined"
        startIcon={<EmailIcon />}
        onClick={() => onAction('send_reminder')}
      >
        Erinnerung senden
      </Button>
      
      {contractMonitoring.status === 'EXPIRED' && (
        <Button
          size="small"
          variant="outlined"
          color="warning"
          startIcon={<RestoreIcon />}
          onClick={() => onAction('lapsed_renewal')}
        >
          Rückwirkend verlängern
        </Button>
      )}
    </Box>
  );
};
```

## Monitoring Dashboard

```typescript
export const ContractMonitoringDashboard: React.FC = () => {
  const { data: stats } = useContractStats();
  const { data: expiring } = useExpiringContracts(90);
  
  return (
    <Grid container spacing={3}>
      {/* Statistik-Karten */}
      <Grid item xs={12} md={3}>
        <StatCard
          title="Aktive Verträge"
          value={stats?.activeContracts || 0}
          color="success"
          icon={<VerifiedIcon />}
        />
      </Grid>
      
      <Grid item xs={12} md={3}>
        <StatCard
          title="Auslaufend (90 Tage)"
          value={stats?.expiringIn90Days || 0}
          color="warning"
          icon={<WarningIcon />}
          onClick={() => navigate('/contracts/expiring')}
        />
      </Grid>
      
      <Grid item xs={12} md={3}>
        <StatCard
          title="Kritisch (30 Tage)"
          value={stats?.expiringIn30Days || 0}
          color="error"
          icon={<ErrorIcon />}
        />
      </Grid>
      
      <Grid item xs={12} md={3}>
        <StatCard
          title="Renewal Rate"
          value={`${stats?.renewalRate || 0}%`}
          color="info"
          icon={<TrendingUpIcon />}
        />
      </Grid>
      
      {/* Preisindex-Widget */}
      <Grid item xs={12} md={6}>
        <PriceIndexWidget />
      </Grid>
      
      {/* Xentral Sync Status */}
      <Grid item xs={12} md={6}>
        <XentralSyncStatus />
      </Grid>
      
      {/* Auslaufende Verträge Liste */}
      <Grid item xs={12}>
        <Paper sx={{ p: 2 }}>
          <Typography variant="h6" gutterBottom>
            Demnächst auslaufende Verträge
          </Typography>
          <ExpiringContractsList contracts={expiring} />
        </Paper>
      </Grid>
    </Grid>
  );
};
```

## Price Index Widget

```typescript
const PriceIndexWidget: React.FC = () => {
  const { data: indexData } = usePriceIndex();
  
  const changePercent = indexData 
    ? ((indexData.current - indexData.baseline) / indexData.baseline * 100)
    : 0;
    
  const thresholdExceeded = changePercent > 5;
  
  return (
    <Card>
      <CardContent>
        <Typography variant="h6" gutterBottom>
          Verbraucherpreisindex
        </Typography>
        
        <Box sx={{ display: 'flex', alignItems: 'baseline', gap: 1, mb: 2 }}>
          <Typography variant="h3">
            {indexData?.current.toFixed(1) || '—'}
          </Typography>
          <Chip
            size="small"
            label={`${changePercent > 0 ? '+' : ''}${changePercent.toFixed(1)}%`}
            color={thresholdExceeded ? 'warning' : 'default'}
          />
        </Box>
        
        {thresholdExceeded && (
          <Alert severity="warning" sx={{ mb: 2 }}>
            <AlertTitle>Preisanpassung möglich</AlertTitle>
            Der Index hat die 5%-Schwelle überschritten. 
            Preiserhöhung um {(changePercent - 5).toFixed(1)}% möglich.
          </Alert>
        )}
        
        <Typography variant="body2" color="textSecondary">
          Baseline: {indexData?.baseline || '—'} | 
          Letzte Prüfung: {formatDate(indexData?.lastCheck)}
        </Typography>
      </CardContent>
    </Card>
  );
};
```

## Integration in Opportunity Card

```typescript
// Erweiterung der bestehenden OpportunityCard
export const OpportunityCard: React.FC<OpportunityCardProps> = ({ opportunity }) => {
  const { contractMonitoring } = useContractMonitoring(opportunity.customerId);
  
  return (
    <Card>
      {/* Existing card content */}
      
      {/* Contract Badge Integration */}
      {contractMonitoring && (
        <Box sx={{ px: 2, pb: 1 }}>
          <ContractBadge 
            opportunity={opportunity}
            contractMonitoring={contractMonitoring}
          />
        </Box>
      )}
      
      {/* Renewal Actions für RENEWAL Stage */}
      {opportunity.stage === OpportunityStage.RENEWAL && (
        <CardActions>
          <RenewalActions 
            opportunity={opportunity}
            onAction={handleAction}
          />
        </CardActions>
      )}
    </Card>
  );
};
```

## 🔗 Verwandte Dokumente

- **Backend API:** [./backend-architecture.md](./backend-architecture.md)
- **Styles & Theme:** [/Users/joergstreeck/freshplan-sales-tool/docs/FRESH-FOODZ_CI.md](/Users/joergstreeck/freshplan-sales-tool/docs/FRESH-FOODZ_CI.md)
- **Dashboard Layout:** [./monitoring-dashboard.md](./monitoring-dashboard.md)