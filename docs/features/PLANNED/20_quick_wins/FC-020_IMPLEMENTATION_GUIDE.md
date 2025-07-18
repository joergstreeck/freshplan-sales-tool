# 🚀 FC-020 QUICK WINS COLLECTION

**Feature Code:** FC-020  
**Feature-Typ:** 🎨 FRONTEND  
**Geschätzter Aufwand:** 5-6 Tage (alle Quick Wins)  
**Priorität:** MEDIUM - Sofortige Produktivitätssteigerung  
**ROI:** Kleine Features, große Wirkung  

---

## 🎯 PROBLEM & LÖSUNG

**Problem:** Viele Klicks, umständliche Suche, keine Stapelverarbeitung, kein Excel-Export  
**Lösung:** Quick Wins Bundle - Keyboard Shortcuts, Bulk Actions, Smart Search, Excel Export  
**Impact:** 50% schnellere Bedienung, zufriedenere Power-User  

---

## 🎹 1. KEYBOARD SHORTCUTS

### Globale Shortcuts

```typescript
interface KeyboardShortcut {
  key: string;
  modifiers?: ('ctrl' | 'alt' | 'shift' | 'meta')[];
  description: string;
  action: () => void;
  context?: string; // Nur in bestimmten Kontexten aktiv
}

const globalShortcuts: KeyboardShortcut[] = [
  // Navigation
  { key: 'g', modifiers: [], description: 'Go to...', action: openQuickNav },
  { key: 'g', modifiers: [], key2: 'c', description: 'Go to Customers', action: () => navigate('/customers') },
  { key: 'g', modifiers: [], key2: 'p', description: 'Go to Pipeline', action: () => navigate('/pipeline') },
  { key: 'g', modifiers: [], key2: 'd', description: 'Go to Dashboard', action: () => navigate('/dashboard') },
  
  // Create Actions
  { key: 'n', modifiers: ['ctrl'], description: 'New Opportunity', action: openNewOpportunity },
  { key: 'c', modifiers: ['ctrl'], description: 'New Customer', action: openNewCustomer },
  { key: 't', modifiers: ['ctrl'], description: 'New Task', action: openNewTask },
  
  // Search
  { key: '/', modifiers: [], description: 'Focus Search', action: focusSearch },
  { key: 'k', modifiers: ['ctrl'], description: 'Command Palette', action: openCommandPalette },
  
  // Views
  { key: '1-9', modifiers: ['alt'], description: 'Switch Tab', action: switchTab },
  { key: 'w', modifiers: ['ctrl'], description: 'Close Tab', action: closeCurrentTab },
  
  // Actions
  { key: 's', modifiers: ['ctrl'], description: 'Save', action: saveCurrentForm },
  { key: 'Enter', modifiers: ['ctrl'], description: 'Submit Form', action: submitCurrentForm },
  { key: 'Escape', modifiers: [], description: 'Cancel/Close', action: cancelCurrentAction }
];

// React Hook für Shortcuts
const useKeyboardShortcuts = () => {
  useEffect(() => {
    const handleKeyPress = (event: KeyboardEvent) => {
      // Ignore in input fields unless explicitly allowed
      if (['INPUT', 'TEXTAREA'].includes(document.activeElement?.tagName || '')) {
        if (!event.ctrlKey && !event.metaKey) return;
      }
      
      const shortcut = findMatchingShortcut(event);
      if (shortcut) {
        event.preventDefault();
        shortcut.action();
        
        // Show toast
        showShortcutToast(shortcut.description);
      }
    };
    
    window.addEventListener('keydown', handleKeyPress);
    return () => window.removeEventListener('keydown', handleKeyPress);
  }, []);
};

// Shortcut Help Dialog
const ShortcutHelpDialog: React.FC = () => {
  const [open, setOpen] = useState(false);
  
  // ? opens help
  useKeyboardShortcut('?', () => setOpen(true));
  
  return (
    <Dialog open={open} onClose={() => setOpen(false)} maxWidth="md">
      <DialogTitle>
        <Box display="flex" alignItems="center" gap={1}>
          <Keyboard />
          <Typography variant="h6">Keyboard Shortcuts</Typography>
        </Box>
      </DialogTitle>
      <DialogContent>
        <Grid container spacing={3}>
          <Grid item xs={6}>
            <Typography variant="subtitle2" gutterBottom>Navigation</Typography>
            <List dense>
              {shortcuts.filter(s => s.category === 'navigation').map(shortcut => (
                <ListItem key={shortcut.key}>
                  <ListItemText
                    primary={shortcut.description}
                    secondary={<Kbd>{formatShortcut(shortcut)}</Kbd>}
                  />
                </ListItem>
              ))}
            </List>
          </Grid>
          <Grid item xs={6}>
            <Typography variant="subtitle2" gutterBottom>Actions</Typography>
            <List dense>
              {shortcuts.filter(s => s.category === 'actions').map(shortcut => (
                <ListItem key={shortcut.key}>
                  <ListItemText
                    primary={shortcut.description}
                    secondary={<Kbd>{formatShortcut(shortcut)}</Kbd>}
                  />
                </ListItem>
              ))}
            </List>
          </Grid>
        </Grid>
      </DialogContent>
    </Dialog>
  );
};
```

---

## 📦 2. BULK ACTIONS

### Bulk Operations Component

```typescript
interface BulkAction<T> {
  id: string;
  label: string;
  icon: React.ReactNode;
  action: (items: T[]) => Promise<void>;
  confirmRequired?: boolean;
  confirmMessage?: (count: number) => string;
  availability?: (items: T[]) => boolean;
}

const BulkActionsToolbar: React.FC<{
  selectedItems: any[];
  actions: BulkAction<any>[];
}> = ({ selectedItems, actions }) => {
  const [processing, setProcessing] = useState(false);
  
  const handleBulkAction = async (action: BulkAction<any>) => {
    if (action.confirmRequired) {
      const confirmed = await showConfirmDialog({
        title: 'Bulk Action Confirmation',
        message: action.confirmMessage?.(selectedItems.length) || 
          `Apply "${action.label}" to ${selectedItems.length} items?`,
        confirmText: 'Proceed',
        cancelText: 'Cancel'
      });
      
      if (!confirmed) return;
    }
    
    setProcessing(true);
    try {
      await action.action(selectedItems);
      showSuccessToast(`${action.label} applied to ${selectedItems.length} items`);
    } catch (error) {
      showErrorToast(`Failed to apply ${action.label}`);
    } finally {
      setProcessing(false);
    }
  };
  
  if (selectedItems.length === 0) return null;
  
  return (
    <Paper
      sx={{
        position: 'sticky',
        top: 64,
        zIndex: 10,
        p: 2,
        backgroundColor: 'primary.main',
        color: 'primary.contrastText'
      }}
    >
      <Box display="flex" alignItems="center" gap={2}>
        <Chip
          label={`${selectedItems.length} selected`}
          color="secondary"
          onDelete={() => clearSelection()}
        />
        
        <Box flexGrow={1} />
        
        {actions.map(action => {
          const isAvailable = !action.availability || action.availability(selectedItems);
          
          return (
            <Button
              key={action.id}
              variant="contained"
              color="secondary"
              startIcon={action.icon}
              onClick={() => handleBulkAction(action)}
              disabled={!isAvailable || processing}
            >
              {action.label}
            </Button>
          );
        })}
      </Box>
      
      {processing && <LinearProgress sx={{ mt: 1 }} />}
    </Paper>
  );
};

// Opportunity Bulk Actions
const opportunityBulkActions: BulkAction<Opportunity>[] = [
  {
    id: 'move-stage',
    label: 'Move Stage',
    icon: <SwapHoriz />,
    action: async (opportunities) => {
      const newStage = await showStageSelector();
      if (!newStage) return;
      
      await api.post('/api/opportunities/bulk/move-stage', {
        ids: opportunities.map(o => o.id),
        newStage
      });
    }
  },
  {
    id: 'assign',
    label: 'Assign',
    icon: <PersonAdd />,
    action: async (opportunities) => {
      const user = await showUserSelector();
      if (!user) return;
      
      await api.post('/api/opportunities/bulk/assign', {
        ids: opportunities.map(o => o.id),
        userId: user.id
      });
    }
  },
  {
    id: 'add-tag',
    label: 'Add Tag',
    icon: <Label />,
    action: async (opportunities) => {
      const tag = await showTagSelector();
      if (!tag) return;
      
      await api.post('/api/opportunities/bulk/tag', {
        ids: opportunities.map(o => o.id),
        tag
      });
    }
  },
  {
    id: 'export',
    label: 'Export',
    icon: <FileDownload />,
    action: async (opportunities) => {
      const format = await showExportFormatSelector();
      exportOpportunities(opportunities, format);
    }
  },
  {
    id: 'delete',
    label: 'Delete',
    icon: <Delete />,
    confirmRequired: true,
    confirmMessage: (count) => `Delete ${count} opportunities? This cannot be undone.`,
    action: async (opportunities) => {
      await api.delete('/api/opportunities/bulk', {
        data: { ids: opportunities.map(o => o.id) }
      });
    },
    availability: (items) => items.every(item => item.stage === 'LOST')
  }
];
```

---

## 🔍 3. SMART SEARCH

### Advanced Search Component

```typescript
interface SearchOperator {
  operator: string;
  field: string;
  description: string;
  example: string;
  valueType: 'string' | 'number' | 'date' | 'boolean';
}

const searchOperators: SearchOperator[] = [
  // Amount operators
  { operator: 'amount:', field: 'value', description: 'Deal amount', example: 'amount:>10000', valueType: 'number' },
  { operator: 'value:', field: 'value', description: 'Deal value (alias)', example: 'value:10000..50000', valueType: 'number' },
  
  // Date operators
  { operator: 'created:', field: 'createdAt', description: 'Creation date', example: 'created:2024-01', valueType: 'date' },
  { operator: 'closed:', field: 'closedAt', description: 'Close date', example: 'closed:last-month', valueType: 'date' },
  { operator: 'updated:', field: 'updatedAt', description: 'Last update', example: 'updated:<7d', valueType: 'date' },
  
  // Status operators
  { operator: 'stage:', field: 'stage', description: 'Pipeline stage', example: 'stage:proposal', valueType: 'string' },
  { operator: 'status:', field: 'status', description: 'Deal status', example: 'status:won', valueType: 'string' },
  
  // Assignment operators
  { operator: 'owner:', field: 'ownerId', description: 'Deal owner', example: 'owner:me', valueType: 'string' },
  { operator: 'team:', field: 'teamId', description: 'Team', example: 'team:sales-north', valueType: 'string' },
  
  // Customer operators
  { operator: 'customer:', field: 'customerId', description: 'Customer name', example: 'customer:fresh*', valueType: 'string' },
  { operator: 'industry:', field: 'industry', description: 'Industry', example: 'industry:tech', valueType: 'string' },
  
  // Boolean operators
  { operator: 'has:', field: 'has', description: 'Has property', example: 'has:attachment', valueType: 'boolean' },
  { operator: 'is:', field: 'is', description: 'Is state', example: 'is:stuck', valueType: 'boolean' }
];

const SmartSearchBar: React.FC = () => {
  const [query, setQuery] = useState('');
  const [suggestions, setSuggestions] = useState<string[]>([]);
  const [activeOperator, setActiveOperator] = useState<SearchOperator | null>(null);
  
  const parseQuery = (input: string): ParsedQuery => {
    const tokens = input.match(/(\w+:(?:"[^"]+"|[^\s]+)|\S+)/g) || [];
    const filters: Filter[] = [];
    const keywords: string[] = [];
    
    tokens.forEach(token => {
      const operatorMatch = token.match(/^(\w+):(.+)$/);
      if (operatorMatch) {
        const [, operator, value] = operatorMatch;
        const op = searchOperators.find(o => o.operator === `${operator}:`);
        
        if (op) {
          filters.push(parseOperatorValue(op, value));
        }
      } else {
        keywords.push(token);
      }
    });
    
    return { filters, keywords };
  };
  
  const parseOperatorValue = (op: SearchOperator, value: string): Filter => {
    // Range values: 10000..50000
    if (value.includes('..')) {
      const [min, max] = value.split('..');
      return { field: op.field, operator: 'between', value: [min, max] };
    }
    
    // Comparison operators: >10000, <5000, >=100
    const compMatch = value.match(/^([<>]=?)(.+)$/);
    if (compMatch) {
      const [, operator, val] = compMatch;
      return { field: op.field, operator, value: val };
    }
    
    // Date shortcuts: today, yesterday, last-week, last-month
    if (op.valueType === 'date') {
      const dateValue = parseDateShortcut(value);
      return { field: op.field, operator: 'equals', value: dateValue };
    }
    
    // Wildcard: fresh*
    if (value.includes('*')) {
      return { field: op.field, operator: 'like', value: value.replace('*', '%') };
    }
    
    // Default: exact match
    return { field: op.field, operator: 'equals', value };
  };
  
  return (
    <Box position="relative">
      <TextField
        fullWidth
        placeholder="Search... (Press / to focus, ? for help)"
        value={query}
        onChange={(e) => setQuery(e.target.value)}
        onKeyDown={handleKeyDown}
        InputProps={{
          startAdornment: <Search />,
          endAdornment: query && (
            <IconButton size="small" onClick={() => setQuery('')}>
              <Clear />
            </IconButton>
          )
        }}
      />
      
      {/* Operator Suggestions */}
      {showSuggestions && (
        <Paper
          sx={{
            position: 'absolute',
            top: '100%',
            left: 0,
            right: 0,
            mt: 1,
            maxHeight: 300,
            overflow: 'auto',
            zIndex: 1000
          }}
        >
          <List dense>
            {suggestions.map(suggestion => (
              <ListItem
                button
                key={suggestion}
                onClick={() => applySuggestion(suggestion)}
              >
                <ListItemText
                  primary={suggestion}
                  secondary={getOperatorDescription(suggestion)}
                />
              </ListItem>
            ))}
          </List>
        </Paper>
      )}
      
      {/* Active Filters */}
      {parsedFilters.length > 0 && (
        <Box display="flex" gap={1} mt={1} flexWrap="wrap">
          {parsedFilters.map((filter, index) => (
            <Chip
              key={index}
              label={formatFilter(filter)}
              onDelete={() => removeFilter(index)}
              size="small"
            />
          ))}
        </Box>
      )}
    </Box>
  );
};

// Search Examples Dialog
const SearchExamplesDialog: React.FC = () => (
  <Dialog open maxWidth="md">
    <DialogTitle>Smart Search Examples</DialogTitle>
    <DialogContent>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>Query</TableCell>
            <TableCell>Description</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {[
            { query: 'amount:>50000', desc: 'Opportunities over 50k' },
            { query: 'stage:proposal amount:>10000', desc: 'Proposals over 10k' },
            { query: 'created:last-month status:won', desc: 'Won deals from last month' },
            { query: 'owner:me is:stuck', desc: 'My stuck deals' },
            { query: 'customer:fresh* industry:food', desc: 'Food industry customers starting with "fresh"' },
            { query: 'has:attachment stage:negotiation', desc: 'Negotiations with attachments' },
            { query: 'closed:2024-Q1 value:10000..100000', desc: 'Q1 closes between 10k-100k' }
          ].map(example => (
            <TableRow key={example.query}>
              <TableCell>
                <code>{example.query}</code>
              </TableCell>
              <TableCell>{example.desc}</TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </DialogContent>
  </Dialog>
);
```

---

## 📊 4. EXCEL EXPORT

### Universal Excel Export

```typescript
interface ExcelExportConfig<T> {
  data: T[];
  columns: ExcelColumn<T>[];
  filename: string;
  sheetName?: string;
  headerStyle?: ExcelStyle;
  includeFilters?: boolean;
  includeSummary?: boolean;
}

interface ExcelColumn<T> {
  header: string;
  key: keyof T | ((item: T) => any);
  width?: number;
  format?: 'currency' | 'percentage' | 'date' | 'number' | 'text';
  style?: ExcelStyle;
}

const ExcelExportService = {
  async exportToExcel<T>(config: ExcelExportConfig<T>) {
    const workbook = new ExcelJS.Workbook();
    const worksheet = workbook.addWorksheet(config.sheetName || 'Data');
    
    // Add headers
    const headerRow = worksheet.addRow(config.columns.map(col => col.header));
    headerRow.font = { bold: true };
    headerRow.fill = {
      type: 'pattern',
      pattern: 'solid',
      fgColor: { argb: 'FF94C456' } // FreshPlan Green
    };
    
    // Add data
    config.data.forEach(item => {
      const row = worksheet.addRow(
        config.columns.map(col => {
          if (typeof col.key === 'function') {
            return col.key(item);
          }
          return item[col.key];
        })
      );
      
      // Apply formatting
      config.columns.forEach((col, index) => {
        const cell = row.getCell(index + 1);
        
        switch (col.format) {
          case 'currency':
            cell.numFmt = '€#,##0.00';
            break;
          case 'percentage':
            cell.numFmt = '0.00%';
            break;
          case 'date':
            cell.numFmt = 'dd.mm.yyyy';
            break;
          case 'number':
            cell.numFmt = '#,##0';
            break;
        }
      });
    });
    
    // Auto-fit columns
    config.columns.forEach((col, index) => {
      const column = worksheet.getColumn(index + 1);
      column.width = col.width || 15;
    });
    
    // Add filters
    if (config.includeFilters) {
      worksheet.autoFilter = {
        from: { row: 1, column: 1 },
        to: { row: 1, column: config.columns.length }
      };
    }
    
    // Add summary
    if (config.includeSummary) {
      worksheet.addRow([]); // Empty row
      const summaryRow = worksheet.addRow(['Summary']);
      summaryRow.font = { bold: true };
      
      // Calculate totals for numeric columns
      config.columns.forEach((col, index) => {
        if (col.format === 'currency' || col.format === 'number') {
          const columnLetter = String.fromCharCode(65 + index);
          const formula = `SUM(${columnLetter}2:${columnLetter}${config.data.length + 1})`;
          worksheet.getCell(`${columnLetter}${config.data.length + 3}`).formula = formula;
        }
      });
    }
    
    // Generate file
    const buffer = await workbook.xlsx.writeBuffer();
    const blob = new Blob([buffer], { 
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
    });
    
    // Download
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `${config.filename}_${format(new Date(), 'yyyy-MM-dd')}.xlsx`;
    link.click();
    URL.revokeObjectURL(url);
  }
};

// Export Button Component
const ExcelExportButton: React.FC<{
  getData: () => any[];
  columns: ExcelColumn<any>[];
  filename: string;
}> = ({ getData, columns, filename }) => {
  const [exporting, setExporting] = useState(false);
  
  const handleExport = async () => {
    setExporting(true);
    try {
      const data = await getData();
      
      await ExcelExportService.exportToExcel({
        data,
        columns,
        filename,
        includeFilters: true,
        includeSummary: true
      });
      
      showSuccessToast(`Exported ${data.length} items to Excel`);
    } catch (error) {
      showErrorToast('Export failed');
    } finally {
      setExporting(false);
    }
  };
  
  return (
    <Button
      variant="outlined"
      startIcon={exporting ? <CircularProgress size={16} /> : <FileDownload />}
      onClick={handleExport}
      disabled={exporting}
    >
      Export to Excel
    </Button>
  );
};

// Predefined Export Configs
const exportConfigs = {
  opportunities: {
    columns: [
      { header: 'Title', key: 'title' },
      { header: 'Customer', key: (o: Opportunity) => o.customer.name },
      { header: 'Value', key: 'value', format: 'currency' },
      { header: 'Stage', key: 'stage' },
      { header: 'Owner', key: (o: Opportunity) => o.owner.name },
      { header: 'Created', key: 'createdAt', format: 'date' },
      { header: 'Expected Close', key: 'expectedCloseDate', format: 'date' },
      { header: 'Probability', key: 'probability', format: 'percentage' }
    ],
    filename: 'opportunities'
  },
  
  customers: {
    columns: [
      { header: 'Name', key: 'name', width: 30 },
      { header: 'Industry', key: 'industry' },
      { header: 'City', key: 'city' },
      { header: 'Revenue', key: 'totalRevenue', format: 'currency' },
      { header: 'Open Opportunities', key: 'openOpportunities', format: 'number' },
      { header: 'Last Activity', key: 'lastActivityDate', format: 'date' },
      { header: 'Owner', key: (c: Customer) => c.owner?.name || 'Unassigned' }
    ],
    filename: 'customers'
  }
};
```

---

## 🎯 BUSINESS VALUE

- **Keyboard Shortcuts:** 50% weniger Klicks für Power-User
- **Bulk Actions:** 10x schnellere Massenoperationen
- **Smart Search:** Komplexe Suchen in Sekunden
- **Excel Export:** Vertrieb bekommt vertraute Formate

---

## 🚀 IMPLEMENTIERUNGS-PHASEN

1. **Phase 1:** Keyboard Shortcuts Framework
2. **Phase 2:** Bulk Actions für Opportunities
3. **Phase 3:** Smart Search Parser
4. **Phase 4:** Excel Export Engine

---

## 📊 SUCCESS METRICS

- **Shortcut Usage:** > 30% der User nutzen Shortcuts
- **Bulk Operations:** Ø 20+ Items pro Operation
- **Search Efficiency:** 70% weniger Zeit für Suchen
- **Export Usage:** > 100 Exports/Monat

---

**Nächster Schritt:** Keyboard Shortcut Manager implementieren