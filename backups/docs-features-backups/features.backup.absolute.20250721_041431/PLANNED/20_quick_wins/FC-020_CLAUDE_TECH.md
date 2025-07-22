# FC-020 CLAUDE_TECH: Quick Wins Collection

**CLAUDE TECH** | **Original:** 1427 Zeilen → **Optimiert:** 480 Zeilen (66% Reduktion!)  
**Feature-Typ:** 🎨 FRONTEND | **Priorität:** HOCH | **Geschätzter Aufwand:** 2 Tage

## ⚡ QUICK-LOAD (30 Sekunden bis produktiv!)

**5 Power-User Features für 437 Stunden Zeitersparnis pro User/Jahr**

### 🎯 Das macht es:
- **Command Palette** (Cmd+K): Alles finden/machen ohne Maus-Navigation
- **Keyboard Shortcuts**: Maus-freie Workflows für Expert Users  
- **Smart Search**: Intelligente Suche mit Natural Language ("k kunde", "neue opp")
- **Bulk Actions**: Multi-Select + Batch-Operations (bis 100 Entities)
- **Excel Export**: One-Click XLSX mit Templates und Filterung

### 🚀 ROI:
- **105 Min/Tag** Zeitersparnis pro User
- **437 Stunden/Jahr** Produktivitätssteigerung
- **218:1 ROI** ratio (2 Tage Development für 437h Ersparnis)

### 🏗️ Architektur:
```
Global Keyboard Manager → Command Palette → Command Registry → Action Dispatcher
         ↓                       ↓                ↓              ↓
   Shortcut Registry      Smart Search     Command Providers   Bulk Actions
         ↓                       ↓                ↓              ↓
   User Preferences       Search Providers   Permission Check   Excel Export
```

---

## 📋 COPY-PASTE READY RECIPES

### 🔧 Backend Starter Kit

#### 1. Bulk Operations API:
```java
@Path("/api/bulk")
@RolesAllowed({"admin", "manager", "sales"})
public class BulkOperationsResource {
    
    @Inject BulkProcessor bulkProcessor;
    
    @POST @Path("/opportunities")
    public Response bulkUpdateOpportunities(BulkUpdateRequest request) {
        try {
            validateBulkRequest(request);
            
            BulkUpdateResult result = bulkProcessor.processOpportunityUpdates(
                request.getEntityIds(),
                request.getUpdates(),
                getCurrentUser()
            );
            
            return Response.ok(result).build();
            
        } catch (BulkValidationException e) {
            return Response.status(400)
                .entity(Map.of("error", e.getMessage(), "violations", e.getViolations()))
                .build();
        }
    }
    
    @POST @Path("/assign")
    public Response bulkAssignToUser(BulkAssignRequest request) {
        return bulkProcessor.processBulkAssignment(
            request.getEntityType(),
            request.getEntityIds(),
            request.getTargetUserId(),
            getCurrentUser()
        );
    }
}
```

#### 2. Excel Export API:
```java
@Path("/api/export")
@RolesAllowed({"admin", "manager", "sales"})
public class ExportDataResource {
    
    @Inject ExportDataService exportService;
    
    @POST @Path("/opportunities")
    @Produces("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public Response exportOpportunities(ExportRequest request) {
        try {
            // Security: Filter based on user permissions
            FilterContext filterContext = FilterContext.builder()
                .userId(getCurrentUser().getId())
                .userRoles(getCurrentUser().getRoles())
                .filters(request.getFilters())
                .build();
            
            ExportData exportData = exportService.prepareOpportunityExport(filterContext);
            
            return Response.ok(exportData.getData())
                .header("Content-Disposition", 
                    "attachment; filename=\"opportunities_" + LocalDate.now() + ".xlsx\"")
                .build();
                
        } catch (SecurityException e) {
            return Response.status(403)
                .entity(Map.of("error", "Insufficient permissions for export"))
                .build();
        }
    }
    
    @GET @Path("/templates")
    public List<ExportTemplate> getExportTemplates() {
        return exportService.getAvailableTemplates(getCurrentUser());
    }
}
```

#### 3. Bulk Processing Service:
```java
@ApplicationScoped
@Transactional
public class BulkProcessor {
    
    @Inject OpportunityRepository opportunityRepository;
    @Inject SecurityService securityService;
    @Inject Event<BulkOperationEvent> bulkEvents;
    
    public BulkUpdateResult processOpportunityUpdates(
        List<UUID> entityIds,
        Map<String, Object> updates,
        User currentUser
    ) {
        BulkUpdateResult.Builder resultBuilder = BulkUpdateResult.builder()
            .totalRequested(entityIds.size());
        
        List<BulkError> errors = new ArrayList<>();
        int successCount = 0;
        
        // Process in chunks to avoid memory issues
        List<List<UUID>> chunks = Lists.partition(entityIds, 50);
        
        for (List<UUID> chunk : chunks) {
            List<Opportunity> opportunities = opportunityRepository
                .find("id in ?1", chunk).list();
            
            for (Opportunity opp : opportunities) {
                try {
                    // Security check per entity
                    if (!securityService.canUpdate(opp, currentUser)) {
                        errors.add(BulkError.builder()
                            .entityId(opp.getId())
                            .errorCode("ACCESS_DENIED")
                            .build());
                        continue;
                    }
                    
                    applyUpdates(opp, updates);
                    validateEntity(opp);
                    opportunityRepository.persist(opp);
                    successCount++;
                    
                } catch (ValidationException e) {
                    errors.add(BulkError.builder()
                        .entityId(opp.getId())
                        .errorCode("VALIDATION_ERROR")
                        .errorMessage(e.getMessage())
                        .build());
                }
            }
        }
        
        BulkUpdateResult result = resultBuilder
            .successfulUpdates(successCount)
            .failedUpdates(errors.size())
            .errors(errors)
            .build();
        
        // Fire event for audit log
        bulkEvents.fireAsync(BulkOperationEvent.builder()
            .operationType("OPPORTUNITY_BULK_UPDATE")
            .userId(currentUser.getId())
            .result(result)
            .build());
        
        return result;
    }
}
```

#### 4. Database Schema:
```sql
-- V8.0__create_quick_wins_tables.sql

-- Export Templates
CREATE TABLE export_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    template_name VARCHAR(255) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    columns_config JSONB NOT NULL,
    default_filters JSONB,
    user_id UUID REFERENCES users(id),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Bulk Operation Log
CREATE TABLE bulk_operation_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    operation_id UUID NOT NULL,
    operation_type VARCHAR(100) NOT NULL,
    user_id UUID NOT NULL REFERENCES users(id),
    entity_type VARCHAR(50) NOT NULL,
    total_requested INTEGER NOT NULL,
    successful_updates INTEGER NOT NULL,
    failed_updates INTEGER NOT NULL,
    processing_time_ms INTEGER,
    created_at TIMESTAMP DEFAULT NOW()
);

-- User Shortcuts Preferences
CREATE TABLE user_shortcuts (
    user_id UUID PRIMARY KEY REFERENCES users(id),
    shortcuts_config JSONB NOT NULL DEFAULT '{}',
    command_history JSONB DEFAULT '[]',
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Default Export Templates
INSERT INTO export_templates (template_name, entity_type, columns_config, user_id) VALUES
(
    'Standard Opportunities Export',
    'OPPORTUNITIES',
    '[
        {"fieldPath": "title", "headerName": "Opportunity Title", "order": 1},
        {"fieldPath": "customer.name", "headerName": "Customer", "order": 2},
        {"fieldPath": "amount", "headerName": "Amount", "dataType": "CURRENCY", "order": 3},
        {"fieldPath": "status", "headerName": "Status", "order": 4},
        {"fieldPath": "owner.name", "headerName": "Owner", "order": 5}
    ]'::jsonb,
    null
);
```

### 🎨 Frontend Starter Kit

#### 1. Command Palette Component:
```typescript
// CommandPalette.tsx
export const CommandPalette: React.FC<CommandPaletteProps> = ({
  open, onOpenChange
}) => {
  const [search, setSearch] = useState('');
  const { commands, isLoading } = useCommands();
  const { executeCommand } = useCommandExecution();
  const { recentCommands, addToRecent } = useCommandHistory();
  
  // Filter commands based on search
  const filteredCommands = useMemo(() => {
    if (!search) return [...recentCommands, ...commands].slice(0, 10);
    return fuse.search(search).map(result => result.item);
  }, [search, commands, recentCommands]);
  
  const handleCommandSelect = useCallback(async (command: Command) => {
    try {
      await executeCommand(command.id, { source: 'palette' });
      addToRecent(command);
      onOpenChange(false);
      setSearch('');
    } catch (error) {
      toast.error(`Command failed: ${error.message}`);
    }
  }, [executeCommand, addToRecent, onOpenChange]);
  
  return (
    <CommandDialog open={open} onOpenChange={onOpenChange}>
      <CommandInput
        placeholder="Was möchtest du tun? (Tipp: 'k kunde' für Kundensuche)"
        value={search}
        onValueChange={setSearch}
      />
      
      <CommandList>
        {isLoading && <CommandLoading>Lade Commands...</CommandLoading>}
        
        {!isLoading && filteredCommands.length === 0 && (
          <CommandEmpty>
            Keine Commands gefunden für "{search}"
            <div className="text-xs text-muted-foreground mt-2">
              Versuche: 'neuer kunde', 'export', 'einstellungen'
            </div>
          </CommandEmpty>
        )}
        
        {!isLoading && (
          <CommandGroup>
            {filteredCommands.map((command) => (
              <CommandItem
                key={command.id}
                onSelect={() => handleCommandSelect(command)}
                className="flex items-center gap-2"
              >
                <span className="text-lg">{command.icon}</span>
                <div className="flex-1">
                  <div className="font-medium">{command.label}</div>
                  {command.shortcuts && (
                    <div className="text-xs text-muted-foreground">
                      {command.shortcuts.join(' + ')}
                    </div>
                  )}
                </div>
                <Badge variant="outline">{command.category}</Badge>
              </CommandItem>
            ))}
          </CommandGroup>
        )}
      </CommandList>
    </CommandDialog>
  );
};
```

#### 2. Smart Search Component:
```typescript
// SmartSearch.tsx
export const SmartSearch: React.FC<SmartSearchProps> = ({
  placeholder = "Suche oder gib einen Befehl ein...",
  onResultSelect
}) => {
  const [query, setQuery] = useState('');
  const [isOpen, setIsOpen] = useState(false);
  const { results, isLoading } = useSmartSearch(query);
  const inputRef = useRef<HTMLInputElement>(null);
  
  // Smart query parsing
  const parsedQuery = useMemo(() => parseSmartQuery(query), [query]);
  
  // Global shortcut to focus search
  useHotkeys('/', () => inputRef.current?.focus(), { preventDefault: true });
  
  const handleResultSelect = (result: SearchResult) => {
    onResultSelect?.(result);
    setQuery('');
    setIsOpen(false);
    
    // Navigate based on result type
    switch (result.type) {
      case 'customer':
        router.push(`/customers/${result.id}`);
        break;
      case 'opportunity':
        router.push(`/opportunities/${result.id}`);
        break;
      case 'action':
        executeAction(result.action);
        break;
    }
  };
  
  return (
    <div className="relative">
      <div className="relative">
        <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4" />
        <Input
          ref={inputRef}
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          onFocus={() => setIsOpen(true)}
          placeholder={placeholder}
          className="pl-10 pr-20"
        />
        
        {parsedQuery.type && (
          <Badge variant="outline" className="absolute right-3 top-1/2 transform -translate-y-1/2">
            {getTypeLabel(parsedQuery.type)}
          </Badge>
        )}
      </div>
      
      {isOpen && query && (
        <div className="absolute top-full left-0 right-0 mt-1 bg-white border rounded-lg shadow-lg z-50">
          {parsedQuery.type && (
            <div className="p-3 border-b bg-muted/50">
              <div className="text-sm font-medium">Interpretiert als:</div>
              <div className="text-xs text-muted-foreground">
                {formatQueryInterpretation(parsedQuery)}
              </div>
            </div>
          )}
          
          {isLoading && (
            <div className="p-4 text-center">
              <Loader2 className="w-4 h-4 animate-spin mx-auto" />
            </div>
          )}
          
          {!isLoading && results.map((result, index) => (
            <button
              key={result.id}
              onClick={() => handleResultSelect(result)}
              className="w-full text-left p-3 hover:bg-muted/50 flex items-center gap-3"
            >
              <div className="text-lg">{getResultIcon(result.type)}</div>
              <div className="flex-1">
                <div className="font-medium">{result.title}</div>
                <div className="text-sm text-muted-foreground">{result.subtitle}</div>
              </div>
            </button>
          ))}
        </div>
      )}
    </div>
  );
};
```

#### 3. Bulk Actions Manager:
```typescript
// BulkActionsManager.tsx
export const BulkActionsManager: React.FC<BulkActionsProps> = ({
  selectedItems, entityType, onSelectionChange, onActionComplete
}) => {
  const [isActionDialogOpen, setIsActionDialogOpen] = useState(false);
  const [currentAction, setCurrentAction] = useState<BulkAction | null>(null);
  const { executeBulkAction } = useBulkActions();
  
  const availableActions = useMemo(() => {
    return getBulkActionsForEntity(entityType);
  }, [entityType]);
  
  const handleActionExecute = async (actionParams: any) => {
    if (!currentAction) return;
    
    try {
      const result = await executeBulkAction({
        action: currentAction.id,
        entityType,
        entityIds: selectedItems,
        parameters: actionParams
      });
      
      toast.success(
        `${currentAction.label} erfolgreich: ${result.successCount}/${result.totalCount}`
      );
      
      if (result.errors.length > 0) {
        toast.warning(`${result.errors.length} Fehler aufgetreten`);
      }
      
      onActionComplete?.();
      setIsActionDialogOpen(false);
      onSelectionChange([]);
      
    } catch (error) {
      toast.error(`Fehler bei ${currentAction.label}: ${error.message}`);
    }
  };
  
  if (selectedItems.length === 0) return null;
  
  return (
    <>
      <div className="fixed bottom-4 left-1/2 transform -translate-x-1/2 bg-white border rounded-lg shadow-lg p-4 z-50">
        <div className="flex items-center gap-4">
          <div className="text-sm font-medium">
            {selectedItems.length} {entityType} ausgewählt
          </div>
          
          <div className="flex gap-2">
            {availableActions.map((action) => (
              <Button
                key={action.id}
                variant="outline"
                size="sm"
                onClick={() => {
                  setCurrentAction(action);
                  setIsActionDialogOpen(true);
                }}
              >
                <span>{action.icon}</span>
                {action.label}
              </Button>
            ))}
          </div>
          
          <Button variant="ghost" size="sm" onClick={() => onSelectionChange([])}>
            Abbrechen
          </Button>
        </div>
      </div>
      
      <BulkActionDialog
        open={isActionDialogOpen}
        onOpenChange={setIsActionDialogOpen}
        action={currentAction}
        selectedCount={selectedItems.length}
        onExecute={handleActionExecute}
      />
    </>
  );
};
```

#### 4. Excel Export Component:
```typescript
// ExcelExport.tsx
export const ExcelExport: React.FC<ExcelExportProps> = ({
  entityType, filters = {}, selectedIds
}) => {
  const [isExporting, setIsExporting] = useState(false);
  const [exportOptions, setExportOptions] = useState<ExportOptions>({
    template: 'default',
    includeHeaders: true,
    dateFormat: 'DD.MM.YYYY'
  });
  const { exportTemplates } = useExportTemplates(entityType);
  const { exportToExcel } = useExcelExport();
  
  const handleExport = async () => {
    setIsExporting(true);
    
    try {
      const exportRequest: ExportRequest = {
        entityType,
        template: exportOptions.template,
        filters: selectedIds ? { ids: selectedIds } : filters,
        options: exportOptions
      };
      
      const blob = await exportToExcel(exportRequest);
      
      const fileName = `${entityType}_export_${format(new Date(), 'yyyy-MM-dd_HH-mm')}.xlsx`;
      downloadBlob(blob, fileName);
      
      toast.success(`Export erfolgreich: ${fileName}`);
      
    } catch (error) {
      toast.error(`Export fehlgeschlagen: ${error.message}`);
    } finally {
      setIsExporting(false);
    }
  };
  
  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="outline" size="sm" disabled={isExporting}>
          {isExporting ? (
            <Loader2 className="w-4 h-4 animate-spin" />
          ) : (
            <Download className="w-4 h-4" />
          )}
          Excel Export
        </Button>
      </DropdownMenuTrigger>
      
      <DropdownMenuContent align="end" className="w-56">
        <DropdownMenuLabel>Export-Optionen</DropdownMenuLabel>
        
        {/* Template Selection */}
        <DropdownMenuSub>
          <DropdownMenuSubTrigger>Template</DropdownMenuSubTrigger>
          <DropdownMenuSubContent>
            {exportTemplates.map((template) => (
              <DropdownMenuCheckboxItem
                key={template.id}
                checked={exportOptions.template === template.id}
                onCheckedChange={() => 
                  setExportOptions(prev => ({ ...prev, template: template.id }))
                }
              >
                {template.name}
              </DropdownMenuCheckboxItem>
            ))}
          </DropdownMenuSubContent>
        </DropdownMenuSub>
        
        <DropdownMenuSeparator />
        
        <DropdownMenuItem onClick={handleExport}>
          <Download className="w-4 h-4 mr-2" />
          Jetzt exportieren
          {selectedIds && (
            <span className="ml-2 text-xs text-muted-foreground">
              ({selectedIds.length} ausgewählt)
            </span>
          )}
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  );
};
```

#### 5. Global Keyboard Manager:
```typescript
// GlobalKeyboardManager.tsx
export const GlobalKeyboardManager: React.FC<{ children: React.ReactNode }> = ({
  children
}) => {
  const [commandPaletteOpen, setCommandPaletteOpen] = useState(false);
  const [helpOverlayOpen, setHelpOverlayOpen] = useState(false);
  const { userShortcuts } = useUserShortcuts();
  const { executeCommand } = useCommandExecution();
  
  // Global shortcuts
  const shortcuts = useMemo(() => ({
    'cmd+k, ctrl+k': () => setCommandPaletteOpen(true),
    '?': () => setHelpOverlayOpen(true),
    'esc': () => {
      setCommandPaletteOpen(false);
      setHelpOverlayOpen(false);
    },
    'ctrl+n': () => executeCommand('new-opportunity'),
    'ctrl+shift+n': () => executeCommand('new-customer'),
    'ctrl+e': () => executeCommand('excel-export'),
    'ctrl+,': () => executeCommand('open-settings'),
    ...userShortcuts
  }), [userShortcuts, executeCommand]);
  
  // Register all shortcuts
  Object.entries(shortcuts).forEach(([key, handler]) => {
    useHotkeys(key, handler, { 
      enableOnFormTags: false,
      preventDefault: true
    });
  });
  
  return (
    <>
      {children}
      
      <CommandPalette
        open={commandPaletteOpen}
        onOpenChange={setCommandPaletteOpen}
      />
      
      <KeyboardHelpOverlay
        open={helpOverlayOpen}
        onOpenChange={setHelpOverlayOpen}
        shortcuts={shortcuts}
      />
    </>
  );
};
```

#### 6. React Query Hooks:
```typescript
// useCommands.ts
export const useCommands = () => {
  const { user } = useAuth();
  
  return useQuery({
    queryKey: ['commands', user?.id],
    queryFn: async () => {
      const providers = [
        new NavigationCommandProvider(),
        new CreationCommandProvider(),
        new ExportCommandProvider(),
        new SettingsCommandProvider()
      ];
      
      const allCommands = await Promise.all(
        providers.map(provider => provider.getCommands())
      );
      
      return allCommands
        .flat()
        .filter(cmd => hasPermission(user, cmd.permissions))
        .sort((a, b) => a.label.localeCompare(b.label));
    },
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
};

// useSmartSearch.ts
export const useSmartSearch = (query: string) => {
  const debouncedQuery = useDebounce(query, 300);
  
  return useQuery({
    queryKey: ['smart-search', debouncedQuery],
    queryFn: async () => {
      if (!debouncedQuery || debouncedQuery.length < 2) return [];
      
      const parsedQuery = parseSmartQuery(debouncedQuery);
      const providers = getSearchProviders();
      
      const searchPromises = providers
        .filter(provider => provider.canHandle(debouncedQuery))
        .map(provider => provider.search(debouncedQuery, { parsedQuery }));
      
      const results = await Promise.all(searchPromises);
      
      return results
        .flat()
        .sort((a, b) => b.relevance - a.relevance)
        .slice(0, 20);
    },
    enabled: debouncedQuery.length >= 2,
    staleTime: 30 * 1000,
  });
};

// useBulkActions.ts
export const useBulkActions = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: async (request: BulkActionRequest) => {
      const response = await api.bulk.execute(request);
      return response.data;
    },
    onSuccess: (result, variables) => {
      queryClient.invalidateQueries({ 
        queryKey: [variables.entityType] 
      });
      
      if (result.successCount > 0) {
        updateCacheOptimistically(queryClient, variables, result);
      }
    }
  });
};
```

---

## 📊 IMPLEMENTIERUNGSPLAN

### Phase 1: Command Palette Foundation (0.5 Tage)
1. **Command Infrastructure** (Vormittag)
   - Command Registry System + Command Providers
   - Basic Command Palette Component
   - Keyboard Navigation (Cmd+K, Arrow Keys)

2. **Command Actions** (Nachmittag)
   - Navigation + Creation Commands
   - Recent Commands History + Categories

### Phase 2: Shortcuts & Smart Search (0.5 Tage)
1. **Global Shortcuts** (Vormittag)
   - Global Keyboard Manager + User Preferences
   - Help Overlay (? key) + Conflict Detection

2. **Smart Search Engine** (Nachmittag)
   - Query Parser + Search Provider Registry
   - Fuzzy Search + Result Navigation

### Phase 3: Bulk Actions (0.5 Tage)
1. **Frontend Bulk Selection** (Vormittag)
   - Multi-Select State + Bulk Action Bar
   - Action Dialogs + Progress Indicators

2. **Backend Bulk API** (Nachmittag)
   - Bulk Operations Endpoints + Batch Processing
   - Security Validation + Error Handling

### Phase 4: Excel Export & Integration (0.5 Tage)
1. **Excel Export Engine** (Vormittag)
   - Export Templates + XLSX Generation
   - Column Configuration + Filtering

2. **Final Integration** (Nachmittag)
   - Integration in alle Module (M1, M3, M4)
   - Performance Optimization + Testing

---

## 🎯 POWER-USER FEATURES

### Command Categories:
```typescript
const COMMAND_CATEGORIES = {
  NAVIGATION: 'navigation',    // zu Seiten wechseln
  CREATION: 'creation',       // neue Entities erstellen  
  ACTIONS: 'actions',         // Bulk Operations
  EXPORT: 'export',           // Daten exportieren
  SETTINGS: 'settings'        // App konfigurieren
};
```

### Smart Search Patterns:
- **"k kunde"** → Kundensuche
- **"o opp"** → Opportunity-Suche
- **"neue opp"** → Neue Opportunity erstellen
- **"export all"** → Excel Export starten
- **"settings"** → Einstellungen öffnen

### Keyboard Shortcuts:
- **Cmd+K / Ctrl+K** → Command Palette
- **/** → Search fokussieren
- **?** → Help Overlay
- **Ctrl+N** → Neue Opportunity
- **Ctrl+Shift+N** → Neuer Kunde
- **Ctrl+E** → Excel Export
- **Ctrl+,** → Einstellungen

---

## 🔗 INTEGRATION POINTS

### Dependencies:
- **M1 Navigation**: Smart Search in Header
- **M3 Sales Cockpit**: Bulk Actions für Opportunities
- **M4 Opportunity Pipeline**: Export + Bulk Updates

### Libraries:
- **cmdk**: Command Menu Library (accessibility + fuzzy search)
- **react-hotkeys-hook**: Keyboard Shortcuts
- **xlsx**: Excel Export (SheetJS)
- **fuse.js**: Fuzzy Search

---

## ⚠️ WICHTIGE ENTSCHEIDUNGEN

1. **Command Palette**: cmdk Library (bewährt, accessibility, fuzzy search out-of-the-box)
2. **Smart Search**: Regex-basiertes Parsing mit Command Shortcuts (einfach, erweiterbar)
3. **Bulk Operations**: 50 Entities per Batch, max 100 total (Performance-Balance)
4. **Excel Export**: Client-side mit SheetJS (bessere UX, +600KB Bundle Size)

---

**Status:** Ready for Implementation | **ROI:** 218:1 | **Next:** Command Palette starten