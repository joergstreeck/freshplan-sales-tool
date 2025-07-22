# 📱 FC-018 MOBILE FIELD SALES APP

**Feature Code:** FC-018  
**Feature-Typ:** 🎨 FRONTEND (PWA)  
**Geschätzter Aufwand:** 8-10 Tage  
**Priorität:** HIGH - Außendienst braucht mobile Lösung  
**ROI:** 100% Produktivität auch unterwegs  

---

## 🎯 PROBLEM & LÖSUNG

**Problem:** Außendienst ohne Laptop = blind, keine Kundeninfos, keine Erfassung vor Ort  
**Lösung:** PWA mit Offline-Support, Visitenkarten-Scanner, Voice-to-Text  
**Impact:** Vollständige CRM-Nutzung mobil, auch ohne Internet  

---

## 📱 MOBILE-FIRST KONZEPT

```
MOBILE PWA INTERFACE
┌─────────────────────┐
│ 📍 Bei: FreshFoodz  │
│ ⚡ Quick Actions    │
├─────────────────────┤
│ [📸 Visitenkarte]   │
│ [🎤 Sprachnotiz]    │
│ [📝 Quick Note]     │
│ [📅 Folgetermin]    │
├─────────────────────┤
│ KUNDE               │
│ FreshFoodz GmbH     │
│ ⭐⭐⭐⭐⭐ Premium    │
│                     │
│ LETZTE AKTIVITÄT    │
│ Vor 5 Tagen:        │
│ "Interesse an Bio"  │
│                     │
│ OPPORTUNITIES (2)   │
│ • Q1 Liefervertrag  │
│   50.000€ (80%)     │
│ • Bio-Sortiment     │
│   25.000€ (40%)     │
│                     │
│ [💬 Chat] [📞 Call] │
└─────────────────────┘
```

---

## 📋 FEATURES IM DETAIL

### 1. Progressive Web App Setup

```typescript
// manifest.json
{
  "name": "FreshPlan Field Sales",
  "short_name": "FreshPlan",
  "description": "CRM für unterwegs - auch offline",
  "start_url": "/mobile",
  "display": "standalone",
  "theme_color": "#94C456",
  "background_color": "#ffffff",
  "icons": [
    {
      "src": "/icons/icon-192.png",
      "sizes": "192x192",
      "type": "image/png"
    },
    {
      "src": "/icons/icon-512.png",
      "sizes": "512x512",
      "type": "image/png",
      "purpose": "any maskable"
    }
  ],
  "screenshots": [
    {
      "src": "/screenshots/mobile-home.png",
      "sizes": "375x812",
      "type": "image/png"
    }
  ]
}

// Service Worker für Offline
self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open('freshplan-v1').then((cache) => {
      return cache.addAll([
        '/',
        '/mobile',
        '/offline.html',
        '/api/customers/sync', // Offline-Daten
        '/icons/icon-192.png',
        '/css/mobile.css',
        '/js/mobile.bundle.js'
      ]);
    })
  );
});
```

### 2. Visitenkarten-Scanner

```typescript
interface BusinessCardScanner {
  scan: () => Promise<BusinessCardData>;
  extractText: (image: Blob) => Promise<string>;
  parseContactInfo: (text: string) => ContactInfo;
}

const BusinessCardScannerComponent: React.FC = () => {
  const [scanning, setScanning] = useState(false);
  const [extractedData, setExtractedData] = useState<ContactInfo | null>(null);
  
  const handleScan = async () => {
    try {
      // Camera API
      const stream = await navigator.mediaDevices.getUserMedia({ 
        video: { facingMode: 'environment' } 
      });
      
      // Capture image
      const image = await captureImage(stream);
      
      // OCR mit Tesseract.js (offline)
      const text = await Tesseract.recognize(image, 'deu', {
        logger: m => console.log(m)
      });
      
      // Smart Parsing
      const contact = parseBusinessCard(text.data.text);
      setExtractedData(contact);
      
    } catch (error) {
      // Fallback: Manuelles Foto
      const input = document.createElement('input');
      input.type = 'file';
      input.accept = 'image/*';
      input.capture = 'environment';
      input.click();
    }
  };
  
  return (
    <Card sx={{ p: 2 }}>
      <CardContent>
        {!scanning && !extractedData && (
          <Button
            variant="contained"
            fullWidth
            size="large"
            startIcon={<CameraAlt />}
            onClick={handleScan}
          >
            Visitenkarte scannen
          </Button>
        )}
        
        {scanning && (
          <Box textAlign="center">
            <CircularProgress />
            <Typography>Karte wird gescannt...</Typography>
          </Box>
        )}
        
        {extractedData && (
          <Box>
            <Typography variant="h6" gutterBottom>
              Erkannte Daten:
            </Typography>
            
            <TextField
              fullWidth
              label="Name"
              value={extractedData.name}
              onChange={(e) => setExtractedData({
                ...extractedData,
                name: e.target.value
              })}
              margin="normal"
            />
            
            <TextField
              fullWidth
              label="Firma"
              value={extractedData.company}
              margin="normal"
            />
            
            <TextField
              fullWidth
              label="Position"
              value={extractedData.position}
              margin="normal"
            />
            
            <TextField
              fullWidth
              label="E-Mail"
              value={extractedData.email}
              margin="normal"
            />
            
            <TextField
              fullWidth
              label="Telefon"
              value={extractedData.phone}
              margin="normal"
            />
            
            <Box mt={2} display="flex" gap={1}>
              <Button
                variant="outlined"
                onClick={() => setExtractedData(null)}
              >
                Neu scannen
              </Button>
              <Button
                variant="contained"
                onClick={() => saveContact(extractedData)}
              >
                Kontakt speichern
              </Button>
            </Box>
          </Box>
        )}
      </CardContent>
    </Card>
  );
};

// Smart Business Card Parser
function parseBusinessCard(text: string): ContactInfo {
  const lines = text.split('\n').filter(line => line.trim());
  
  const contact: ContactInfo = {
    name: '',
    company: '',
    position: '',
    email: '',
    phone: '',
    address: ''
  };
  
  // Email erkennen
  const emailMatch = text.match(/[\w._%+-]+@[\w.-]+\.[A-Z|a-z]{2,}/);
  if (emailMatch) {
    contact.email = emailMatch[0];
  }
  
  // Telefon erkennen
  const phoneMatch = text.match(/(\+49|0)[\d\s\-\/]+/);
  if (phoneMatch) {
    contact.phone = phoneMatch[0].replace(/[\s\-\/]/g, '');
  }
  
  // Firma (oft größte Schrift = erste Zeile)
  if (lines.length > 0) {
    contact.company = lines[0];
  }
  
  // Name (oft zweite Zeile oder vor Email)
  const namePatterns = [
    /^(Herr|Frau|Dr\.|Prof\.)?\s*([A-ZÄÖÜ][a-zäöüß]+\s+[A-ZÄÖÜ][a-zäöüß]+)/,
    /([A-ZÄÖÜ][a-zäöüß]+\s+[A-ZÄÖÜ][a-zäöüß]+)(?=\s|$)/
  ];
  
  for (const pattern of namePatterns) {
    const match = text.match(pattern);
    if (match) {
      contact.name = match[0].trim();
      break;
    }
  }
  
  // Position (Keywords)
  const positionKeywords = [
    'Geschäftsführer', 'CEO', 'Manager', 'Direktor', 
    'Leiter', 'Head of', 'Inhaber', 'Partner'
  ];
  
  for (const line of lines) {
    for (const keyword of positionKeywords) {
      if (line.includes(keyword)) {
        contact.position = line;
        break;
      }
    }
  }
  
  return contact;
}
```

### 3. Voice-to-Text für Notizen

```typescript
const VoiceNoteComponent: React.FC = () => {
  const [isRecording, setIsRecording] = useState(false);
  const [transcript, setTranscript] = useState('');
  const [audioBlob, setAudioBlob] = useState<Blob | null>(null);
  
  const recognition = useRef<SpeechRecognition | null>(null);
  
  useEffect(() => {
    // Web Speech API Setup
    if ('webkitSpeechRecognition' in window) {
      recognition.current = new webkitSpeechRecognition();
      recognition.current.continuous = true;
      recognition.current.interimResults = true;
      recognition.current.lang = 'de-DE';
      
      recognition.current.onresult = (event) => {
        let finalTranscript = '';
        let interimTranscript = '';
        
        for (let i = event.resultIndex; i < event.results.length; i++) {
          const transcript = event.results[i][0].transcript;
          if (event.results[i].isFinal) {
            finalTranscript += transcript + ' ';
          } else {
            interimTranscript += transcript;
          }
        }
        
        setTranscript(prev => prev + finalTranscript);
      };
    }
  }, []);
  
  const startRecording = async () => {
    setIsRecording(true);
    
    // Audio Recording für Backup
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
    const mediaRecorder = new MediaRecorder(stream);
    const chunks: Blob[] = [];
    
    mediaRecorder.ondataavailable = (e) => chunks.push(e.data);
    mediaRecorder.onstop = () => {
      const blob = new Blob(chunks, { type: 'audio/webm' });
      setAudioBlob(blob);
    };
    
    mediaRecorder.start();
    recognition.current?.start();
    
    // Auto-Stop nach 5 Minuten
    setTimeout(() => {
      if (isRecording) {
        stopRecording();
      }
    }, 5 * 60 * 1000);
  };
  
  const stopRecording = () => {
    setIsRecording(false);
    recognition.current?.stop();
  };
  
  return (
    <Card>
      <CardContent>
        <Box display="flex" alignItems="center" gap={2} mb={2}>
          <IconButton
            color={isRecording ? 'error' : 'primary'}
            onClick={isRecording ? stopRecording : startRecording}
            size="large"
          >
            {isRecording ? <Stop /> : <Mic />}
          </IconButton>
          
          <Typography>
            {isRecording ? 'Aufnahme läuft...' : 'Sprachnotiz aufnehmen'}
          </Typography>
          
          {isRecording && (
            <Box sx={{ flexGrow: 1 }}>
              <LinearProgress />
            </Box>
          )}
        </Box>
        
        {transcript && (
          <Box>
            <TextField
              fullWidth
              multiline
              rows={4}
              value={transcript}
              onChange={(e) => setTranscript(e.target.value)}
              label="Transkript"
              helperText="Text kann bearbeitet werden"
            />
            
            <Box mt={2} display="flex" gap={1}>
              <Button
                variant="outlined"
                startIcon={<VolumeUp />}
                onClick={() => playAudio(audioBlob)}
                disabled={!audioBlob}
              >
                Anhören
              </Button>
              
              <Button
                variant="contained"
                onClick={() => saveNote({
                  text: transcript,
                  audio: audioBlob,
                  customerId: currentCustomer.id
                })}
              >
                Notiz speichern
              </Button>
            </Box>
          </Box>
        )}
        
        {/* Quick Templates */}
        <Box mt={2}>
          <Typography variant="caption">Quick Templates:</Typography>
          <Box display="flex" gap={0.5} flexWrap="wrap" mt={0.5}>
            {[
              'Interesse an Produkt X',
              'Folgetermin vereinbart',
              'Budget verfügbar',
              'Entscheider getroffen',
              'Konkurrenz: Y'
            ].map(template => (
              <Chip
                key={template}
                label={template}
                size="small"
                onClick={() => setTranscript(prev => prev + ' ' + template)}
              />
            ))}
          </Box>
        </Box>
      </CardContent>
    </Card>
  );
};
```

### 4. Offline Sync Manager

```typescript
class OfflineSyncManager {
  private db: IDBDatabase;
  private syncQueue: SyncItem[] = [];
  
  async init() {
    // IndexedDB für Offline Storage
    this.db = await openDB('freshplan-offline', 1, {
      upgrade(db) {
        // Customers Store
        if (!db.objectStoreNames.contains('customers')) {
          const customerStore = db.createObjectStore('customers', { 
            keyPath: 'id' 
          });
          customerStore.createIndex('lastModified', 'lastModified');
        }
        
        // Sync Queue Store
        if (!db.objectStoreNames.contains('syncQueue')) {
          db.createObjectStore('syncQueue', { 
            keyPath: 'id',
            autoIncrement: true 
          });
        }
        
        // Activities Store
        if (!db.objectStoreNames.contains('activities')) {
          db.createObjectStore('activities', { keyPath: 'id' });
        }
      }
    });
    
    // Background Sync Registration
    if ('serviceWorker' in navigator && 'SyncManager' in window) {
      const registration = await navigator.serviceWorker.ready;
      await registration.sync.register('freshplan-sync');
    }
  }
  
  async saveOffline(type: string, data: any) {
    const tx = this.db.transaction(['syncQueue'], 'readwrite');
    await tx.objectStore('syncQueue').add({
      type,
      data,
      timestamp: new Date(),
      status: 'pending'
    });
    
    // Try immediate sync if online
    if (navigator.onLine) {
      this.syncNow();
    }
  }
  
  async syncNow() {
    const tx = this.db.transaction(['syncQueue'], 'readonly');
    const queue = await tx.objectStore('syncQueue').getAll();
    
    for (const item of queue) {
      try {
        await this.syncItem(item);
        await this.markSynced(item.id);
      } catch (error) {
        console.error('Sync failed for item:', item, error);
      }
    }
  }
  
  private async syncItem(item: SyncItem) {
    switch (item.type) {
      case 'customer':
        return await api.post('/api/customers', item.data);
      case 'activity':
        return await api.post('/api/activities', item.data);
      case 'note':
        return await api.post('/api/notes', item.data);
      case 'opportunity':
        return await api.post('/api/opportunities', item.data);
    }
  }
}

// React Hook für Offline Status
const useOfflineSync = () => {
  const [isOnline, setIsOnline] = useState(navigator.onLine);
  const [pendingSync, setPendingSync] = useState(0);
  
  useEffect(() => {
    const handleOnline = () => {
      setIsOnline(true);
      showNotification('Online! Daten werden synchronisiert...');
      syncManager.syncNow();
    };
    
    const handleOffline = () => {
      setIsOnline(false);
      showNotification('Offline-Modus aktiviert');
    };
    
    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);
    
    return () => {
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
    };
  }, []);
  
  return { isOnline, pendingSync };
};
```

### 5. Mobile-Optimized UI Components

```typescript
// Swipeable Customer Cards
const SwipeableCustomerList: React.FC = () => {
  const handlers = useSwipeable({
    onSwipedLeft: (eventData) => handleArchive(eventData),
    onSwipedRight: (eventData) => handleFavorite(eventData),
    preventDefaultTouchmoveEvent: true,
    trackMouse: true
  });
  
  return (
    <List>
      {customers.map(customer => (
        <SwipeableListItem key={customer.id} {...handlers}>
          <ListItemAvatar>
            <Avatar>{customer.name[0]}</Avatar>
          </ListItemAvatar>
          <ListItemText
            primary={customer.name}
            secondary={
              <React.Fragment>
                <Typography component="span" variant="body2">
                  {customer.city} • {customer.industry}
                </Typography>
                {customer.lastActivity && (
                  <Typography component="span" variant="caption" display="block">
                    {formatRelativeTime(customer.lastActivity)}
                  </Typography>
                )}
              </React.Fragment>
            }
          />
          <ListItemSecondaryAction>
            <IconButton edge="end" onClick={() => callCustomer(customer)}>
              <Phone />
            </IconButton>
          </ListItemSecondaryAction>
        </SwipeableListItem>
      ))}
    </List>
  );
};

// Bottom Navigation
const MobileNavigation: React.FC = () => {
  const [value, setValue] = useState(0);
  
  return (
    <BottomNavigation
      value={value}
      onChange={(event, newValue) => setValue(newValue)}
      sx={{ 
        position: 'fixed', 
        bottom: 0, 
        left: 0, 
        right: 0,
        borderTop: 1,
        borderColor: 'divider'
      }}
    >
      <BottomNavigationAction
        label="Kunden"
        icon={<People />}
      />
      <BottomNavigationAction
        label="Pipeline"
        icon={<TrendingUp />}
      />
      <BottomNavigationAction
        label="Quick"
        icon={
          <Fab size="small" color="primary" sx={{ boxShadow: 3 }}>
            <Add />
          </Fab>
        }
      />
      <BottomNavigationAction
        label="Kalender"
        icon={<CalendarToday />}
      />
      <BottomNavigationAction
        label="Mehr"
        icon={<MoreHoriz />}
      />
    </BottomNavigation>
  );
};
```

### 6. Location-Based Features

```typescript
// Nearby Customers
const NearbyCustomers: React.FC = () => {
  const [location, setLocation] = useState<GeolocationPosition | null>(null);
  const [nearbyCustomers, setNearbyCustomers] = useState<Customer[]>([]);
  
  useEffect(() => {
    if ('geolocation' in navigator) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          setLocation(position);
          fetchNearbyCustomers(position.coords);
        },
        (error) => console.error('Location error:', error),
        { enableHighAccuracy: true }
      );
    }
  }, []);
  
  return (
    <Card>
      <CardHeader
        title="Kunden in der Nähe"
        subheader={location ? `${nearbyCustomers.length} im Umkreis von 5km` : 'Standort wird ermittelt...'}
        avatar={<LocationOn color="primary" />}
      />
      <CardContent>
        <List>
          {nearbyCustomers.map(customer => (
            <ListItem key={customer.id}>
              <ListItemText
                primary={customer.name}
                secondary={`${customer.distance}km • ${customer.address}`}
              />
              <ListItemSecondaryAction>
                <IconButton onClick={() => openInMaps(customer)}>
                  <Navigation />
                </IconButton>
              </ListItemSecondaryAction>
            </ListItem>
          ))}
        </List>
      </CardContent>
    </Card>
  );
};
```

---

## 🎯 BUSINESS VALUE

- **Außendienst-Produktivität:** +40% durch mobile Erfassung
- **Datenqualität:** Sofortige Eingabe = bessere Daten
- **Reaktionszeit:** Instant Follow-up noch vor Ort
- **Offline-Fähigkeit:** Auch ohne Netz voll arbeitsfähig

---

## 🚀 IMPLEMENTIERUNGS-PHASEN

1. **Phase 1:** PWA Grundgerüst + Offline Basics
2. **Phase 2:** Visitenkarten-Scanner
3. **Phase 3:** Voice-to-Text Integration
4. **Phase 4:** Location Features + Sync

---

## 📊 SUCCESS METRICS

- **Mobile Usage:** > 80% Außendienst nutzt App täglich
- **Data Entry Speed:** 3x schneller als Desktop
- **Offline Reliability:** 99.9% Sync-Erfolg
- **User Satisfaction:** > 90% NPS Score

---

**Nächster Schritt:** PWA Setup + Service Worker implementieren