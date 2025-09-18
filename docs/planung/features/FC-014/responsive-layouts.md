# FC-014: Responsive Layouts für alle Bildschirmgrößen

**Parent:** [/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-24_TECH_CONCEPT_FC-014-mobile-tablet-optimization.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-24_TECH_CONCEPT_FC-014-mobile-tablet-optimization.md)  
**Fokus:** Adaptive UI Layouts

## 📐 Layout-Strategien nach Device

### Device-Kategorien
```typescript
// Breakpoint-System
export const DEVICES = {
  mobile: {
    small: 320,   // iPhone SE
    medium: 375,  // iPhone 12/13
    large: 428    // iPhone Pro Max
  },
  tablet: {
    small: 768,   // iPad Mini
    medium: 834,  // iPad Air
    large: 1024   // iPad Pro 11"
  },
  desktop: {
    small: 1280,  // Laptop
    medium: 1440, // Desktop
    large: 1920   // Wide
  }
};
```

## 🎨 Feature-spezifische Layouts

### M4 Opportunity Pipeline

#### Mobile Layout (< 768px)
```tsx
// Vertikale Stage-Liste mit Accordion
<Box sx={{ width: '100%' }}>
  {stages.map(stage => (
    <Accordion key={stage.id} defaultExpanded={stage.isActive}>
      <AccordionSummary>
        <Typography>{stage.name}</Typography>
        <Chip label={stage.count} size="small" />
      </AccordionSummary>
      <AccordionDetails>
        <Stack spacing={1}>
          {stage.opportunities.map(opp => (
            <OpportunityCardMobile key={opp.id} {...opp} />
          ))}
        </Stack>
      </AccordionDetails>
    </Accordion>
  ))}
</Box>
```

#### Tablet Layout (768-1023px)
```tsx
// 2-3 Spalten Grid
<Grid container spacing={2}>
  {stages.slice(0, 3).map(stage => (
    <Grid item xs={12} sm={6} md={4} key={stage.id}>
      <StageColumn stage={stage} />
    </Grid>
  ))}
  {/* Weitere Stages horizontal scrollbar */}
</Grid>
```

#### Desktop Layout (≥ 1024px)
```tsx
// Klassisches Kanban Board
<Box sx={{ display: 'flex', overflowX: 'auto' }}>
  {stages.map(stage => (
    <Box key={stage.id} sx={{ minWidth: 300, flex: '1 1 300px' }}>
      <StageColumn stage={stage} />
    </Box>
  ))}
</Box>
```

### M8 Calculator Mobile

```tsx
// Mobile-optimierter Calculator
const CalculatorMobile: React.FC = () => {
  return (
    <Box sx={{ 
      display: 'flex', 
      flexDirection: 'column',
      height: '100vh'
    }}>
      {/* Fixed Header */}
      <AppBar position="sticky">
        <Toolbar>
          <IconButton onClick={onClose}>
            <CloseIcon />
          </IconButton>
          <Typography variant="h6">Kalkulation</Typography>
          <Button onClick={onSave}>Speichern</Button>
        </Toolbar>
      </AppBar>
      
      {/* Scrollable Content */}
      <Box sx={{ flex: 1, overflow: 'auto', p: 2 }}>
        {/* Produkt-Eingabe */}
        <ProductInputMobile />
        
        {/* Positionen-Liste */}
        <PositionListMobile />
      </Box>
      
      {/* Fixed Footer mit Summe */}
      <Paper 
        elevation={3} 
        sx={{ 
          position: 'sticky', 
          bottom: 0,
          p: 2,
          borderTop: 1,
          borderColor: 'divider'
        }}
      >
        <TotalSummaryMobile />
      </Paper>
    </Box>
  );
};
```

### FC-013 Activity Timeline Responsive

```tsx
// Adaptive Activity Display
const ActivityTimelineResponsive: React.FC = () => {
  const isMobile = useMediaQuery('(max-width:767px)');
  const isTablet = useMediaQuery('(min-width:768px) and (max-width:1023px)');
  
  if (isMobile) {
    return (
      <List>
        {activities.map(activity => (
          <ActivityListItemMobile key={activity.id} {...activity} />
        ))}
      </List>
    );
  }
  
  if (isTablet) {
    return (
      <Grid container spacing={2}>
        {activities.map(activity => (
          <Grid item xs={12} sm={6} key={activity.id}>
            <ActivityCardTablet {...activity} />
          </Grid>
        ))}
      </Grid>
    );
  }
  
  // Desktop: Timeline View
  return <ActivityTimeline activities={activities} />;
};
```

## 📱 Navigation Patterns

### Mobile Navigation
```tsx
// Bottom Navigation für Hauptbereiche
<BottomNavigation
  sx={{ 
    position: 'fixed', 
    bottom: 0, 
    left: 0, 
    right: 0,
    pb: 'env(safe-area-inset-bottom)' // iOS Safe Area
  }}
>
  <BottomNavigationAction label="Pipeline" icon={<ViewKanbanIcon />} />
  <BottomNavigationAction label="Kunden" icon={<PeopleIcon />} />
  <BottomNavigationAction label="Cockpit" icon={<DashboardIcon />} />
  <BottomNavigationAction label="Mehr" icon={<MoreHorizIcon />} />
</BottomNavigation>
```

### Tablet Sidebar
```tsx
// Collapsible Sidebar für Tablets
const TabletLayout: React.FC = () => {
  const [sidebarOpen, setSidebarOpen] = useState(true);
  
  return (
    <Box sx={{ display: 'flex' }}>
      <Drawer
        variant="persistent"
        open={sidebarOpen}
        sx={{
          width: sidebarOpen ? 240 : 60,
          transition: 'width 0.3s'
        }}
      >
        <SidebarNavigation collapsed={!sidebarOpen} />
      </Drawer>
      
      <Box sx={{ flex: 1, ml: sidebarOpen ? '240px' : '60px' }}>
        {children}
      </Box>
    </Box>
  );
};
```

## 🎯 Responsive Components

### Adaptive Card Component
```tsx
const OpportunityCard: React.FC<OpportunityProps> = (props) => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));
  const isTablet = useMediaQuery(theme.breakpoints.between('sm', 'md'));
  
  return (
    <Card
      sx={{
        p: isMobile ? 1 : 2,
        mb: 1,
        cursor: 'pointer',
        touchAction: 'manipulation'
      }}
    >
      {/* Mobile: Kompakt */}
      {isMobile && (
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <Box sx={{ flex: 1 }}>
            <Typography variant="subtitle2">{props.name}</Typography>
            <Typography variant="caption" color="text.secondary">
              {props.customerName} • {formatCurrency(props.value)}
            </Typography>
          </Box>
          <IconButton size="small">
            <MoreVertIcon />
          </IconButton>
        </Box>
      )}
      
      {/* Tablet: Mittel */}
      {isTablet && (
        <>
          <Typography variant="subtitle1">{props.name}</Typography>
          <Typography variant="body2">{props.customerName}</Typography>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 1 }}>
            <Chip label={props.stage} size="small" />
            <Typography variant="h6">{formatCurrency(props.value)}</Typography>
          </Box>
        </>
      )}
      
      {/* Desktop: Voll */}
      {!isMobile && !isTablet && (
        <OpportunityCardDesktop {...props} />
      )}
    </Card>
  );
};
```

## 📊 Responsive Tables

### Mobile Table Alternative
```tsx
// Statt Tabellen: Card-basierte Listen
const CustomerListMobile: React.FC = () => {
  return (
    <List>
      {customers.map(customer => (
        <ListItem
          key={customer.id}
          button
          onClick={() => navigateToCustomer(customer.id)}
        >
          <ListItemAvatar>
            <Avatar>{customer.name[0]}</Avatar>
          </ListItemAvatar>
          <ListItemText
            primary={customer.name}
            secondary={
              <>
                <Typography variant="caption" display="block">
                  {customer.city} • {customer.branch}
                </Typography>
                <Typography variant="caption" color="primary">
                  {customer.opportunityCount} Opportunities
                </Typography>
              </>
            }
          />
          <ChevronRightIcon />
        </ListItem>
      ))}
    </List>
  );
};
```

## 🔧 Responsive Utilities

### useResponsive Hook
```typescript
export const useResponsive = () => {
  const theme = useTheme();
  
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));
  const isTablet = useMediaQuery(theme.breakpoints.between('sm', 'md'));
  const isDesktop = useMediaQuery(theme.breakpoints.up('md'));
  const isWide = useMediaQuery(theme.breakpoints.up('lg'));
  
  const deviceType = isMobile ? 'mobile' : 
                    isTablet ? 'tablet' : 
                    isDesktop ? 'desktop' : 'wide';
  
  return {
    isMobile,
    isTablet,
    isDesktop,
    isWide,
    deviceType,
    // Helpers
    getValue: <T,>(mobile: T, tablet: T, desktop: T): T => {
      if (isMobile) return mobile;
      if (isTablet) return tablet;
      return desktop;
    }
  };
};
```

### Responsive Typography
```typescript
// Responsive Schriftgrößen
const responsiveTypography = {
  h1: {
    fontSize: { xs: '1.5rem', sm: '2rem', md: '2.5rem' }
  },
  h2: {
    fontSize: { xs: '1.25rem', sm: '1.5rem', md: '2rem' }
  },
  body1: {
    fontSize: { xs: '0.875rem', sm: '1rem' }
  }
};
```

## ⚡ Performance Tipps

1. **CSS Grid/Flexbox** statt JavaScript Layout
2. **Container Queries** für komponenten-basiertes Responsive
3. **Lazy Loading** für Off-Screen Content
4. **Virtual Scrolling** bei langen Listen
5. **Responsive Images** mit srcset