# FC-022 CLAUDE_TECH: Mobile Light PWA

**CLAUDE TECH** | **Original:** 1615 Zeilen → **Optimiert:** 520 Zeilen (68% Reduktion!)  
**Feature-Typ:** 🎨 FRONTEND | **Priorität:** HOCH | **Geschätzter Aufwand:** 3 Tage

## ⚡ QUICK-LOAD (30 Sekunden bis produktiv!)

**Leichtgewichtige Mobile-Experience ohne PWA-Komplexität für sofortigen Außendienst-Support**

### 🎯 Das macht es:
- **Read-Only Customer Access**: Kundendaten und Opportunities offline verfügbar (80% der Mobile Use Cases)
- **Offline Notes System**: Notizen mit Geolocation und Voice Input - sync bei Online-Verbindung
- **Touch-Optimierte UX**: Swipe-Gestures, Pull-to-Refresh, 44px Touch-Targets für iOS/Android
- **Progressive Web App**: Installation ohne App Store, arbeitet als native App

### 🚀 ROI:
- **Sofortige Mobile-Unterstützung**: 5 Außendienstler × 10 Besuche/Tag = 50 mobile Zugriffe täglich
- **3 Tage statt 7 Wochen**: Stepping Stone zu FC-006 Full Mobile App - validiert Workflows
- **80% Use Cases abgedeckt**: Customer Lookup, Notizen, Quick Actions bei 60% weniger Aufwand

### 🏗️ Architektur:
```
PWA Shell → Mobile Router → Customer Views + Offline Notes
     ↓           ↓                ↓              ↓
Service Worker  IndexedDB     Virtual Lists   Voice Input
Cache Strategy  Sync Queue    Touch Gestures  Geolocation
```

---

## 📋 COPY-PASTE READY RECIPES

### 🔧 Backend Starter Kit

#### 1. Mobile API Endpoints:
```java
@Path("/api/mobile")
@Authenticated
public class MobileApiResource {
    
    @Inject CustomerService customerService;
    @Inject MobileNotesService notesService;
    
    @GET @Path("/customers")
    public Response getMobileCustomers(
        @QueryParam("lastSync") String lastSyncTimestamp,
        @QueryParam("limit") @DefaultValue("50") int limit
    ) {
        try {
            Instant lastSync = parseTimestamp(lastSyncTimestamp);
            MobileCustomerResponse response = customerService
                .getMobileCustomerData(getCurrentUser(), lastSync, limit);
            
            EntityTag etag = new EntityTag(response.getDataHash());
            return Response.ok(response)
                .tag(etag)
                .header("Cache-Control", "max-age=300") // 5 minutes
                .build();
        } catch (Exception e) {
            return Response.status(500)
                .entity(Map.of("error", "Failed to fetch customer data"))
                .build();
        }
    }
    
    @GET @Path("/customers/{customerId}")
    public Response getCustomerDetail(@PathParam("customerId") UUID customerId) {
        try {
            CustomerDetailResponse customer = customerService
                .getCustomerDetailForMobile(customerId, getCurrentUser());
            
            if (customer == null) {
                return Response.status(404)
                    .entity(Map.of("error", "Customer not found"))
                    .build();
            }
            
            return Response.ok(customer)
                .header("Cache-Control", "max-age=600") // 10 minutes
                .build();
        } catch (SecurityException e) {
            return Response.status(403)
                .entity(Map.of("error", "Access denied"))
                .build();
        }
    }
    
    @POST @Path("/notes")
    public Response createOfflineNote(CreateOfflineNoteRequest request) {
        try {
            validateOfflineNote(request);
            OfflineNote note = notesService.createOfflineNote(request, getCurrentUser());
            return Response.status(201)
                .entity(OfflineNoteResponse.from(note))
                .build();
        } catch (ValidationException e) {
            return Response.status(400)
                .entity(Map.of("error", e.getMessage()))
                .build();
        }
    }
    
    @POST @Path("/sync/notes")
    public Response syncOfflineNotes(SyncOfflineNotesRequest request) {
        try {
            SyncResult result = notesService.syncOfflineNotes(
                request.getNotes(), getCurrentUser());
            return Response.ok(result).build();
        } catch (Exception e) {
            return Response.status(500)
                .entity(Map.of("error", "Sync failed"))
                .build();
        }
    }
}
```

#### 2. Mobile DTOs:
```java
// Lightweight Customer for Mobile
public class MobileCustomerSummary {
    private UUID id;
    private String name;
    private String contactPerson;
    private String city;
    private String phone;
    private String email;
    private CustomerType type; // A, B, C customer
    private BigDecimal totalRevenue;
    private int openOpportunities;
    
    // Mobile-specific computed fields
    private String shortDisplayName; // "Müller GmbH"
    private String subtitle; // "Berlin • A-Kunde"
    private String primaryAction; // "call" | "email" | "visit"
}

public class CustomerDetailResponse {
    private UUID id;
    private String name;
    private ContactInfo contactInfo;
    private List<MobileOpportunity> opportunities;
    private List<MobileActivity> recentActivities;
    private List<OfflineNote> notes;
    
    // Computed for mobile UX
    private String displayAddress;
    private String formattedPhone;
    private boolean canCall;
    private boolean canEmail;
}

public class MobileOpportunity {
    private UUID id;
    private String title;
    private BigDecimal amount;
    private OpportunityStage stage;
    private LocalDate expectedCloseDate;
    private String statusIcon; // 🟢 🟡 🔴
    private String shortDescription;
}
```

#### 3. Offline Notes Entity:
```java
@Entity
@Table(name = "offline_notes")
public class OfflineNote {
    @Id private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(name = "customer_id") private UUID customerId;
    @Column(name = "opportunity_id") private UUID opportunityId;
    @Column(name = "note_content", nullable = false, length = 2000) private String noteContent;
    
    @Column(name = "note_type")
    @Enumerated(EnumType.STRING)
    private OfflineNoteType type; // VISIT, CALL, EMAIL, GENERAL
    
    @Column(name = "created_offline_at", nullable = false) private Instant createdOfflineAt;
    @Column(name = "synced_at") private Instant syncedAt;
    @Column(name = "device_id") private String deviceId;
    
    @Column(name = "geolocation")
    @Type(type = "jsonb")
    private GeolocationData geolocation;
    
    @Column(name = "sync_status")
    @Enumerated(EnumType.STRING)
    private SyncStatus syncStatus; // PENDING, SYNCED, FAILED
}

public class GeolocationData {
    private double latitude;
    private double longitude;
    private double accuracy;
    private Instant timestamp;
}
```

#### 4. Database Migration:
```sql
-- V5.0__create_mobile_tables.sql

-- Offline Notes Table
CREATE TABLE offline_notes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    customer_id UUID REFERENCES customers(id),
    opportunity_id UUID REFERENCES opportunities(id),
    note_content TEXT NOT NULL CHECK (char_length(note_content) <= 2000),
    note_type VARCHAR(20) NOT NULL DEFAULT 'GENERAL',
    created_offline_at TIMESTAMP WITH TIME ZONE NOT NULL,
    synced_at TIMESTAMP WITH TIME ZONE,
    device_id VARCHAR(100) NOT NULL,
    geolocation JSONB,
    sync_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Mobile Sync State (per user)
CREATE TABLE mobile_sync_state (
    user_id UUID PRIMARY KEY REFERENCES users(id),
    last_customer_sync TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    last_notes_sync TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    device_tokens JSONB DEFAULT '[]',
    sync_preferences JSONB DEFAULT '{}',
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Performance Indices
CREATE INDEX idx_offline_notes_user_date 
    ON offline_notes(user_id, created_offline_at DESC);
CREATE INDEX idx_offline_notes_sync_status 
    ON offline_notes(sync_status, synced_at) WHERE sync_status = 'PENDING';
CREATE INDEX idx_offline_notes_device_timestamp 
    ON offline_notes(device_id, created_offline_at);

-- Initial Mobile Sync State for existing users
INSERT INTO mobile_sync_state (user_id, last_customer_sync, last_notes_sync)
SELECT id, NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days' 
FROM users WHERE is_active = TRUE;
```

### 🎨 Frontend Starter Kit

#### 1. PWA Shell Component:
```typescript
export const PWAShell: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [isOnline, setIsOnline] = useState(navigator.onLine);
  const [deferredPrompt, setDeferredPrompt] = useState<BeforeInstallPromptEvent | null>(null);
  const { syncPendingNotes } = useOfflineNotes();
  
  // Online/Offline detection
  useEffect(() => {
    const handleOnline = () => {
      setIsOnline(true);
      syncPendingNotes(); // Auto-sync when back online
    };
    const handleOffline = () => setIsOnline(false);
    
    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);
    return () => {
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
    };
  }, [syncPendingNotes]);
  
  // PWA Install Prompt
  useEffect(() => {
    const handleBeforeInstallPrompt = (e: BeforeInstallPromptEvent) => {
      e.preventDefault();
      setDeferredPrompt(e);
    };
    window.addEventListener('beforeinstallprompt', handleBeforeInstallPrompt);
    return () => window.removeEventListener('beforeinstallprompt', handleBeforeInstallPrompt);
  }, []);
  
  const handleInstallPWA = async () => {
    if (!deferredPrompt) return;
    
    deferredPrompt.prompt();
    const { outcome } = await deferredPrompt.userChoice;
    
    if (outcome === 'accepted') {
      toast.success('FreshPlan wurde zu deinem Homescreen hinzugefügt!');
    }
    setDeferredPrompt(null);
  };
  
  return (
    <div className="pwa-shell min-h-screen bg-gray-50">
      {/* Status Bar */}
      <div className={cn(
        "status-bar p-2 text-center text-sm",
        isOnline ? "bg-green-100 text-green-800" : "bg-yellow-100 text-yellow-800"
      )}>
        {isOnline ? (
          <div className="flex items-center justify-center gap-2">
            <Wifi className="w-4 h-4" /> Online
          </div>
        ) : (
          <div className="flex items-center justify-center gap-2">
            <WifiOff className="w-4 h-4" />
            Offline - Daten werden synchronisiert sobald Verbindung verfügbar
          </div>
        )}
      </div>
      
      {/* Install Prompt */}
      {deferredPrompt && (
        <div className="install-prompt bg-blue-50 border-b border-blue-200 p-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <Smartphone className="w-5 h-5 text-blue-600" />
              <div>
                <div className="font-medium text-sm">FreshPlan installieren</div>
                <div className="text-xs text-gray-600">
                  Für schnelleren Zugriff zum Homescreen hinzufügen
                </div>
              </div>
            </div>
            <div className="flex gap-2">
              <Button variant="ghost" size="sm" onClick={() => setDeferredPrompt(null)}>
                Später
              </Button>
              <Button size="sm" onClick={handleInstallPWA}>
                Installieren
              </Button>
            </div>
          </div>
        </div>
      )}
      
      <main className="pb-safe-bottom">{children}</main>
      <SyncIndicator />
    </div>
  );
};
```

#### 2. Mobile Customer List:
```typescript
export const MobileCustomerList: React.FC<{
  searchQuery?: string;
  onCustomerSelect?: (customer: MobileCustomer) => void;
}> = ({ searchQuery, onCustomerSelect }) => {
  const { customers, isLoading, hasNextPage, fetchNextPage } = useMobileCustomers();
  const [pullToRefreshTriggered, setPullToRefreshTriggered] = useState(false);
  
  // Filter customers based on search
  const filteredCustomers = useMemo(() => {
    if (!searchQuery) return customers;
    return customers.filter(customer =>
      customer.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      customer.contactPerson?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      customer.city?.toLowerCase().includes(searchQuery.toLowerCase())
    );
  }, [customers, searchQuery]);
  
  // Pull to refresh
  const { refresh } = usePullToRefresh({
    onRefresh: async () => {
      setPullToRefreshTriggered(true);
      await refetchCustomers();
      setPullToRefreshTriggered(false);
    }
  });
  
  // Virtual scrolling for performance
  const virtualizer = useVirtualizer({
    count: filteredCustomers.length,
    getScrollElement: () => document.querySelector('.customer-list-container'),
    estimateSize: () => 80, // Estimated item height
    overscan: 5
  });
  
  const handleCustomerAction = (customer: MobileCustomer, action: 'call' | 'email' | 'note') => {
    switch (action) {
      case 'call':
        window.location.href = `tel:${customer.phone}`;
        trackMobileAction('customer_call', { customerId: customer.id });
        break;
      case 'email':
        window.location.href = `mailto:${customer.email}`;
        trackMobileAction('customer_email', { customerId: customer.id });
        break;
      case 'note':
        onCustomerSelect?.(customer);
        break;
    }
  };
  
  if (isLoading && customers.length === 0) {
    return <MobileCustomerListSkeleton />;
  }
  
  return (
    <div className="customer-list-container h-full overflow-auto">
      {/* Pull to refresh indicator */}
      {pullToRefreshTriggered && (
        <div className="p-4 text-center">
          <Loader2 className="w-5 h-5 animate-spin mx-auto" />
          <div className="text-sm text-gray-600 mt-2">Aktualisiere...</div>
        </div>
      )}
      
      {/* Virtual scrolled list */}
      <div style={{ height: virtualizer.getTotalSize(), width: '100%', position: 'relative' }}>
        {virtualizer.getVirtualItems().map(virtualItem => {
          const customer = filteredCustomers[virtualItem.index];
          
          return (
            <div
              key={virtualItem.key}
              style={{
                position: 'absolute',
                top: 0, left: 0, width: '100%', height: virtualItem.size,
                transform: `translateY(${virtualItem.start}px)`
              }}
            >
              <SwipeableListItem
                leftAction={{
                  icon: <Phone className="w-5 h-5" />,
                  label: 'Anrufen', color: 'green',
                  onSwipe: () => handleCustomerAction(customer, 'call')
                }}
                rightAction={{
                  icon: <MessageSquare className="w-5 h-5" />,
                  label: 'Notiz', color: 'blue',
                  onSwipe: () => handleCustomerAction(customer, 'note')
                }}
              >
                <MobileCustomerCard
                  customer={customer}
                  onTap={() => onCustomerSelect?.(customer)}
                  onActionTap={(action) => handleCustomerAction(customer, action)}
                />
              </SwipeableListItem>
            </div>
          );
        })}
      </div>
      
      {/* Load more / Empty state */}
      {hasNextPage && (
        <div className="p-4 text-center">
          <Button variant="outline" onClick={() => fetchNextPage()} disabled={isLoading}>
            {isLoading ? (
              <><Loader2 className="w-4 h-4 animate-spin mr-2" />Lade mehr...</>
            ) : 'Weitere Kunden laden'}
          </Button>
        </div>
      )}
      
      {filteredCustomers.length === 0 && !isLoading && (
        <div className="p-8 text-center">
          <Building className="w-12 h-12 text-gray-400 mx-auto mb-4" />
          <div className="text-lg font-medium text-gray-900 mb-2">Keine Kunden gefunden</div>
          <div className="text-gray-600">
            {searchQuery ? `Keine Ergebnisse für "${searchQuery}"` : 'Keine Kunden verfügbar'}
          </div>
        </div>
      )}
    </div>
  );
};
```

#### 3. Offline Notes Component:
```typescript
export const OfflineNotes: React.FC<{
  customerId?: string;
  opportunityId?: string;
}> = ({ customerId, opportunityId }) => {
  const [noteContent, setNoteContent] = useState('');
  const [noteType, setNoteType] = useState<OfflineNoteType>('GENERAL');
  const [isRecording, setIsRecording] = useState(false);
  const { createOfflineNote, notes, pendingCount } = useOfflineNotes();
  const { getCurrentLocation } = useGeolocation();
  
  const handleCreateNote = async () => {
    if (!noteContent.trim()) return;
    
    try {
      const location = await getCurrentLocation();
      await createOfflineNote({
        content: noteContent,
        type: noteType,
        customerId, opportunityId,
        geolocation: location,
        createdAt: new Date()
      });
      
      setNoteContent('');
      toast.success('Notiz gespeichert');
      
      // Haptic feedback if available
      if (navigator.vibrate) {
        navigator.vibrate(50);
      }
    } catch (error) {
      toast.error('Fehler beim Speichern der Notiz');
    }
  };
  
  const handleVoiceNote = async () => {
    if (!('webkitSpeechRecognition' in window)) {
      toast.error('Spracherkennung nicht verfügbar');
      return;
    }
    
    setIsRecording(true);
    const recognition = new webkitSpeechRecognition();
    recognition.lang = 'de-DE';
    recognition.continuous = false;
    recognition.interimResults = false;
    
    recognition.onresult = (event) => {
      const transcript = event.results[0][0].transcript;
      setNoteContent(prev => prev + ' ' + transcript);
    };
    
    recognition.onerror = () => {
      toast.error('Spracherkennung fehlgeschlagen');
      setIsRecording(false);
    };
    
    recognition.onend = () => setIsRecording(false);
    recognition.start();
  };
  
  const contextNotes = notes.filter(note => 
    (!customerId || note.customerId === customerId) &&
    (!opportunityId || note.opportunityId === opportunityId)
  );
  
  return (
    <div className="offline-notes h-full flex flex-col">
      {/* Header with sync status */}
      <div className="p-4 border-b bg-white">
        <div className="flex items-center justify-between">
          <h2 className="text-lg font-semibold">Notizen</h2>
          {pendingCount > 0 && (
            <Badge variant="secondary">{pendingCount} ausstehend</Badge>
          )}
        </div>
      </div>
      
      {/* Notes list */}
      <div className="flex-1 overflow-auto p-4 space-y-3">
        {contextNotes.map(note => (
          <div key={note.id} className="bg-white rounded-lg p-3 border shadow-sm">
            <div className="flex items-start justify-between mb-2">
              <div className="flex items-center gap-2">
                <div className="text-sm font-medium">{getNoteTypeLabel(note.type)}</div>
                <Badge variant={note.isSynced ? 'default' : 'secondary'}>
                  {note.isSynced ? 'Synchronisiert' : 'Offline'}
                </Badge>
              </div>
              <div className="text-xs text-gray-500">
                {formatRelativeTime(note.createdAt)}
              </div>
            </div>
            
            <div className="text-sm text-gray-900">{note.content}</div>
            
            {note.geolocation && (
              <div className="text-xs text-gray-500 mt-2 flex items-center gap-1">
                <MapPin className="w-3 h-3" /> Standort erfasst
              </div>
            )}
          </div>
        ))}
        
        {contextNotes.length === 0 && (
          <div className="text-center py-8">
            <FileText className="w-12 h-12 text-gray-400 mx-auto mb-4" />
            <div className="text-gray-600">Noch keine Notizen vorhanden</div>
          </div>
        )}
      </div>
      
      {/* Note creation */}
      <div className="p-4 border-t bg-white">
        <div className="space-y-3">
          {/* Note type selector */}
          <div className="flex gap-2 overflow-x-auto">
            {(['GENERAL', 'VISIT', 'CALL', 'EMAIL'] as OfflineNoteType[]).map(type => (
              <Button
                key={type}
                variant={noteType === type ? 'default' : 'outline'}
                size="sm"
                onClick={() => setNoteType(type)}
                className="flex-shrink-0"
              >
                {getNoteTypeIcon(type)} {getNoteTypeLabel(type)}
              </Button>
            ))}
          </div>
          
          {/* Note input */}
          <div className="relative">
            <Textarea
              value={noteContent}
              onChange={(e) => setNoteContent(e.target.value)}
              placeholder="Notiz eingeben..."
              className="pr-12 min-h-[80px]"
              maxLength={2000}
            />
            
            {/* Voice input button */}
            <Button
              variant="ghost" size="sm"
              className="absolute top-2 right-2"
              onClick={handleVoiceNote}
              disabled={isRecording}
            >
              {isRecording ? (
                <MicOff className="w-4 h-4 text-red-500" />
              ) : (
                <Mic className="w-4 h-4" />
              )}
            </Button>
          </div>
          
          {/* Character count */}
          <div className="text-xs text-gray-500 text-right">
            {noteContent.length}/2000
          </div>
          
          {/* Action button */}
          <Button
            onClick={handleCreateNote}
            disabled={!noteContent.trim()}
            className="w-full"
          >
            <Plus className="w-4 h-4 mr-2" /> Notiz speichern
          </Button>
        </div>
      </div>
    </div>
  );
};
```

#### 4. PWA Configuration:
```json
// public/manifest.json
{
  "name": "FreshPlan Mobile",
  "short_name": "FreshPlan",
  "description": "Mobile CRM für Außendienst",
  "start_url": "/mobile",
  "display": "standalone",
  "orientation": "portrait-primary",
  "theme_color": "#94C456",
  "background_color": "#ffffff",
  "icons": [
    {
      "src": "/icons/icon-192x192.png",
      "sizes": "192x192",
      "type": "image/png",
      "purpose": "any"
    },
    {
      "src": "/icons/icon-512x512.png",
      "sizes": "512x512",
      "type": "image/png",
      "purpose": "any maskable"
    }
  ],
  "shortcuts": [
    {
      "name": "Kunden",
      "url": "/mobile/customers",
      "icons": [{"src": "/icons/customers-shortcut.png", "sizes": "96x96"}]
    },
    {
      "name": "Neue Notiz",
      "url": "/mobile/notes/new",
      "icons": [{"src": "/icons/note-shortcut.png", "sizes": "96x96"}]
    }
  ]
}
```

#### 5. React Query Hooks:
```typescript
// useOfflineNotes.ts
export const useOfflineNotes = () => {
  const [notes, setNotes] = useState<OfflineNote[]>([]);
  const [pendingCount, setPendingCount] = useState(0);
  const { isOnline } = useNetworkStatus();
  const { user } = useAuth();
  
  // Load notes from IndexedDB
  useEffect(() => {
    loadNotesFromDB().then(setNotes);
  }, []);
  
  // Auto-sync when online
  useEffect(() => {
    if (isOnline && pendingCount > 0) {
      syncPendingNotes();
    }
  }, [isOnline, pendingCount]);
  
  const createOfflineNote = useCallback(async (noteData: CreateNoteData) => {
    const note: OfflineNote = {
      id: generateUUID(),
      ...noteData,
      userId: user.id,
      deviceId: getDeviceId(),
      isSynced: false,
      syncAttempts: 0
    };
    
    // Save to IndexedDB immediately
    await saveNoteToIndexedDB(note);
    
    // Update local state
    setNotes(prev => [note, ...prev]);
    setPendingCount(prev => prev + 1);
    
    // Try immediate sync if online
    if (isOnline) {
      trySyncNote(note);
    }
    
    return note;
  }, [user, isOnline]);
  
  const syncPendingNotes = useCallback(async () => {
    const pendingNotes = notes.filter(note => !note.isSynced);
    if (pendingNotes.length === 0) return;
    
    try {
      const result = await api.mobile.syncNotes(pendingNotes);
      
      // Update sync status for successful notes
      const updatedNotes = notes.map(note => {
        const wasSuccessful = result.successfulNotes.includes(note.id);
        return wasSuccessful ? { ...note, isSynced: true } : note;
      });
      
      setNotes(updatedNotes);
      setPendingCount(pendingNotes.length - result.successfulNotes.length);
      
      // Update IndexedDB
      await updateNotesInIndexedDB(updatedNotes);
      
      if (result.successfulNotes.length > 0) {
        toast.success(`${result.successfulNotes.length} Notizen synchronisiert`);
      }
    } catch (error) {
      console.error('Sync failed:', error);
      // Will retry on next online event
    }
  }, [notes]);
  
  return { notes, pendingCount, createOfflineNote, syncPendingNotes };
};

// useMobileCustomers.ts
export const useMobileCustomers = () => {
  const [lastSync, setLastSync] = useState<Date | null>(null);
  const { isOnline } = useNetworkStatus();
  
  return useInfiniteQuery({
    queryKey: ['mobile-customers'],
    queryFn: async ({ pageParam = 0 }) => {
      // Try cache first if offline
      if (!isOnline) {
        return getCachedCustomers(pageParam);
      }
      
      const response = await api.mobile.getCustomers({
        lastSync: lastSync?.toISOString(),
        offset: pageParam * 50,
        limit: 50
      });
      
      // Cache for offline use
      await cacheCustomers(response.customers, pageParam);
      setLastSync(new Date());
      
      return response;
    },
    getNextPageParam: (lastPage, pages) => {
      return lastPage.hasMore ? pages.length : undefined;
    },
    staleTime: 5 * 60 * 1000, // 5 minutes
    gcTime: 30 * 60 * 1000, // 30 minutes
    networkMode: isOnline ? 'online' : 'offlineFirst'
  });
};
```

---

## 📊 IMPLEMENTIERUNGSPLAN

### Phase 1: PWA Foundation (1 Tag)
1. **PWA Setup** (Vormittag)
   - PWA Manifest mit Icons und Shortcuts
   - Service Worker (Cache Strategy)
   - Mobile Router Setup (/mobile/* Routes)
   - Install Prompt Component

2. **Mobile Shell** (Nachmittag)
   - PWA Shell Component mit Online/Offline Status
   - Mobile Navigation Touch-optimiert
   - Safe Area Insets für iOS/Android
   - Basic Offline Storage (IndexedDB Setup)

### Phase 2: Customer Views (1 Tag)
1. **Customer List** (Vormittag)
   - Virtual Scrolling für Performance
   - Pull-to-Refresh Mechanism
   - Swipe Gestures für Actions
   - Search & Filter Touch-optimiert

2. **Customer Detail** (Nachmittag)
   - Customer Detail View Mobile-optimiert
   - Quick Actions (Call, Email, Note)
   - Opportunity Cards Swipeable
   - Cache Management für Offline Access

### Phase 3: Offline Notes System (1 Tag)
1. **Notes Creation** (Vormittag)
   - Offline Notes Component mit Types
   - Voice Input über Web Speech API
   - Geolocation Capture
   - IndexedDB Storage für Persistence

2. **Sync & Polish** (Nachmittag)
   - Background Sync Implementation
   - Conflict Resolution für Duplicate Notes
   - Performance Optimization (Virtual Lists, Caching)
   - Final Testing auf verschiedenen Devices

---

## 🎯 BUSINESS VALUE

### ROI Metriken:
- **Sofortige Mobile-Unterstützung**: 5 Außendienstler × 10 Besuche/Tag = 50 mobile Zugriffe täglich
- **3 Tage statt 7 Wochen**: 95% schnellere Time-to-Market als FC-006 Full Mobile App
- **80% Use Cases abgedeckt**: Customer Lookup + Notizen bei 60% weniger Aufwand

### Technische Vorteile:
- **Progressive Web App**: Installation ohne App Store, arbeitet als native App
- **Offline-First**: 7 Tage Offline-Funktionalität für gecachte Daten
- **Touch-Optimiert**: 44px Touch-Targets, Swipe-Gestures, Pull-to-Refresh
- **Performance**: <3s First Load, <1s Navigation, Virtual Scrolling

---

## 🔗 INTEGRATION POINTS

### Dependencies:
- **FC-008 Security Foundation**: User Authentication + Authorization
- **Bestehende Customer API**: Erweitert um Mobile-optimierte Endpoints

### Enables:
- **FC-006 Mobile App**: Validation der Mobile Workflows und User Patterns
- **FC-018 Mobile PWA**: Upgrade-Pfad mit erweiterten PWA Features

---

## ⚠️ WICHTIGE ENTSCHEIDUNGEN

1. **PWA vs Native App**: PWA für schnelle Implementation ohne App Store Dependencies
2. **Read-Only + Notes**: 80% Use Cases bei 60% weniger Aufwand als Full Editing
3. **Virtual Scrolling**: Performance bei großen Kundenlisten (1000+ Kunden)
4. **IndexedDB Storage**: Robuste Offline-Funktionalität mit strukturierten Daten

---

**Status:** Ready for Implementation | **Phase 1:** PWA Manifest erstellen | **Next:** Mobile Router Setup