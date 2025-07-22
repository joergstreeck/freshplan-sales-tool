# FC-006: Mobile Companion App (Speak & Snap) 🎙️📸 CLAUDE_TECH

**Feature Code:** FC-006  
**Optimiert für:** Claude's 30-Sekunden-Produktivität  
**Original:** 987 Zeilen → **Optimiert:** ~500 Zeilen (49% Reduktion)

## 🎯 QUICK-LOAD: Sofort produktiv in 30 Sekunden!

### Was macht FC-006?
**Offline-First Mobile App mit Voice-to-CRM und Foto-Dokumentation für Außendienst**

### Die 3 Kern-Features:
1. **Speak & Snap** → Spracheingabe wird zu strukturierten CRM-Daten (OpenAI Whisper)
2. **Offline-First** → SQLite + Sync Queue für 100% Verfügbarkeit
3. **Smart Capture** → Fotos mit Auto-Tagging und OCR (AWS Textract)

### Sofort starten:
```bash
# React Native Setup
npx react-native@latest init FreshPlanMobile --template react-native-template-typescript
cd FreshPlanMobile

# Core Dependencies
npm install @react-native-voice/voice react-native-camera react-native-sqlite-storage
npm install @apollo/client graphql react-native-mmkv react-native-background-fetch
```

---

## 📱 1. MOBILE APP: Copy-paste Recipes

### 1.1 Voice Input Component (5 Minuten)
```typescript
import Voice from '@react-native-voice/voice';
import { useState, useEffect } from 'react';

export const VoiceCapture: React.FC = () => {
  const [isRecording, setIsRecording] = useState(false);
  const [transcript, setTranscript] = useState('');
  const [entities, setEntities] = useState<ExtractedEntities | null>(null);
  
  useEffect(() => {
    Voice.onSpeechResults = (e) => {
      const text = e.value?.[0] || '';
      setTranscript(text);
      extractEntities(text);
    };
    
    return () => Voice.destroy();
  }, []);
  
  const startRecording = async () => {
    try {
      await Voice.start('de-DE');
      setIsRecording(true);
    } catch (error) {
      console.error('Voice start error:', error);
    }
  };
  
  const stopRecording = async () => {
    try {
      await Voice.stop();
      setIsRecording(false);
      await processTranscript();
    } catch (error) {
      console.error('Voice stop error:', error);
    }
  };
  
  const extractEntities = (text: string) => {
    // Entity extraction patterns
    const patterns = {
      customer: /(?:kunde|firma|bei)\s+(\w+[\w\s]*)/i,
      quantity: /(\d+)\s*(?:stück|kg|liter|einheiten|paletten)/i,
      date: /(?:am|bis|zum)\s+(\d{1,2}\.\d{1,2}\.?\d{0,4})/i,
      product: /(?:produkt|artikel|ware)\s+([\w\s]+?)(?:\s|$)/i,
      price: /(\d+(?:,\d+)?)\s*€/i
    };
    
    const extracted: ExtractedEntities = {};
    
    Object.entries(patterns).forEach(([key, pattern]) => {
      const match = text.match(pattern);
      if (match) {
        extracted[key] = match[1].trim();
      }
    });
    
    setEntities(extracted);
    return extracted;
  };
  
  const processTranscript = async () => {
    if (!transcript || !entities) return;
    
    // Create opportunity from voice input
    const opportunity: CreateOpportunityInput = {
      title: `Voice Note - ${entities.customer || 'Unknown'}`,
      customer: entities.customer,
      amount: parseFloat(entities.price?.replace(',', '.') || '0'),
      expectedCloseDate: parseDate(entities.date),
      notes: transcript,
      source: 'VOICE_INPUT',
      entities: entities
    };
    
    // Save to local DB first
    await saveToLocalDB(opportunity);
    
    // Queue for sync
    await queueForSync({
      type: 'CREATE_OPPORTUNITY',
      data: opportunity,
      timestamp: Date.now()
    });
  };
  
  return (
    <View style={styles.container}>
      <TouchableOpacity
        onPress={isRecording ? stopRecording : startRecording}
        style={[
          styles.recordButton,
          isRecording && styles.recording
        ]}
      >
        <Icon 
          name={isRecording ? 'stop' : 'mic'} 
          size={48} 
          color="white" 
        />
      </TouchableOpacity>
      
      {transcript && (
        <View style={styles.transcriptContainer}>
          <Text style={styles.transcriptText}>{transcript}</Text>
          
          {entities && (
            <View style={styles.entitiesContainer}>
              {Object.entries(entities).map(([key, value]) => (
                <Chip key={key} label={`${key}: ${value}`} />
              ))}
            </View>
          )}
        </View>
      )}
    </View>
  );
};

// Voice Commands Handler
export const useVoiceCommands = () => {
  const commands = {
    'neue opportunity': () => navigation.navigate('NewOpportunity'),
    'zeige kunden': () => navigation.navigate('CustomerList'),
    'meine termine': () => navigation.navigate('Calendar'),
    'foto machen': () => navigation.navigate('PhotoCapture')
  };
  
  useEffect(() => {
    Voice.onSpeechPartialResults = (e) => {
      const text = e.value?.[0]?.toLowerCase() || '';
      
      Object.entries(commands).forEach(([command, action]) => {
        if (text.includes(command)) {
          action();
          Voice.stop();
        }
      });
    };
  }, []);
};
```

### 1.2 Offline Storage & Sync (10 Minuten)
```typescript
import SQLite from 'react-native-sqlite-storage';
import NetInfo from '@react-native-community/netinfo';
import BackgroundFetch from 'react-native-background-fetch';

// Offline Manager
export class OfflineManager {
  private db: SQLite.Database;
  private syncQueue: SyncQueue;
  
  async initialize() {
    this.db = await SQLite.openDatabase({
      name: 'freshplan.db',
      location: 'default'
    });
    
    await this.createTables();
    await this.setupBackgroundSync();
  }
  
  private async createTables() {
    await this.db.executeSql(`
      CREATE TABLE IF NOT EXISTS opportunities (
        id TEXT PRIMARY KEY,
        data TEXT NOT NULL,
        synced INTEGER DEFAULT 0,
        created_at INTEGER NOT NULL,
        updated_at INTEGER NOT NULL
      )
    `);
    
    await this.db.executeSql(`
      CREATE TABLE IF NOT EXISTS sync_queue (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        type TEXT NOT NULL,
        data TEXT NOT NULL,
        status TEXT DEFAULT 'pending',
        attempts INTEGER DEFAULT 0,
        created_at INTEGER NOT NULL
      )
    `);
  }
  
  async saveOpportunity(opportunity: Opportunity) {
    const id = opportunity.id || generateLocalId();
    const data = JSON.stringify(opportunity);
    
    await this.db.executeSql(
      'INSERT OR REPLACE INTO opportunities (id, data, created_at, updated_at) VALUES (?, ?, ?, ?)',
      [id, data, Date.now(), Date.now()]
    );
    
    return id;
  }
  
  async queueSync(type: string, data: any) {
    await this.db.executeSql(
      'INSERT INTO sync_queue (type, data, created_at) VALUES (?, ?, ?)',
      [type, JSON.stringify(data), Date.now()]
    );
    
    // Try immediate sync if online
    const netInfo = await NetInfo.fetch();
    if (netInfo.isConnected) {
      this.processQueue();
    }
  }
  
  private async processQueue() {
    const [results] = await this.db.executeSql(
      'SELECT * FROM sync_queue WHERE status = ? ORDER BY created_at',
      ['pending']
    );
    
    for (let i = 0; i < results.rows.length; i++) {
      const item = results.rows.item(i);
      
      try {
        await this.syncItem(item);
        
        // Mark as synced
        await this.db.executeSql(
          'UPDATE sync_queue SET status = ? WHERE id = ?',
          ['synced', item.id]
        );
      } catch (error) {
        // Increment attempts
        await this.db.executeSql(
          'UPDATE sync_queue SET attempts = attempts + 1 WHERE id = ?',
          [item.id]
        );
        
        // Mark as failed after 3 attempts
        if (item.attempts >= 3) {
          await this.db.executeSql(
            'UPDATE sync_queue SET status = ? WHERE id = ?',
            ['failed', item.id]
          );
        }
      }
    }
  }
  
  private async syncItem(item: SyncQueueItem) {
    const data = JSON.parse(item.data);
    
    switch (item.type) {
      case 'CREATE_OPPORTUNITY':
        return await api.createOpportunity(data);
      case 'UPDATE_CUSTOMER':
        return await api.updateCustomer(data.id, data);
      case 'UPLOAD_PHOTO':
        return await this.uploadPhoto(data);
    }
  }
  
  private setupBackgroundSync() {
    BackgroundFetch.configure({
      minimumFetchInterval: 15, // 15 minutes
      stopOnTerminate: false,
      startOnBoot: true,
      enableHeadless: true
    }, async () => {
      await this.processQueue();
      BackgroundFetch.finish(BackgroundFetch.FETCH_RESULT_NEW_DATA);
    });
  }
}

// Conflict Resolution
export class ConflictResolver {
  async resolve(local: any, remote: any): Promise<any> {
    // Last-write-wins with field-level merge
    const merged = { ...remote };
    
    // Preserve local changes that are newer
    Object.keys(local).forEach(key => {
      if (local[`${key}_updated_at`] > remote[`${key}_updated_at`]) {
        merged[key] = local[key];
      }
    });
    
    // Special handling for arrays (e.g., notes, activities)
    if (local.notes && remote.notes) {
      merged.notes = this.mergeArrays(local.notes, remote.notes);
    }
    
    return merged;
  }
  
  private mergeArrays(local: any[], remote: any[]) {
    const merged = [...remote];
    
    local.forEach(item => {
      if (!merged.find(r => r.id === item.id)) {
        merged.push(item);
      }
    });
    
    return merged.sort((a, b) => b.created_at - a.created_at);
  }
}
```

### 1.3 Smart Photo Capture (5 Minuten)
```typescript
import { RNCamera } from 'react-native-camera';
import RNFS from 'react-native-fs';

export const PhotoCapture: React.FC = () => {
  const [photos, setPhotos] = useState<Photo[]>([]);
  const cameraRef = useRef<RNCamera>(null);
  
  const takePicture = async () => {
    if (!cameraRef.current) return;
    
    const options = {
      quality: 0.8,
      base64: true,
      exif: true
    };
    
    const data = await cameraRef.current.takePictureAsync(options);
    
    // Extract location from EXIF
    const location = {
      latitude: data.exif?.GPSLatitude,
      longitude: data.exif?.GPSLongitude
    };
    
    // Save locally first
    const localPath = `${RNFS.DocumentDirectoryPath}/${Date.now()}.jpg`;
    await RNFS.writeFile(localPath, data.base64!, 'base64');
    
    const photo: Photo = {
      id: generateLocalId(),
      localPath,
      location,
      timestamp: Date.now(),
      synced: false
    };
    
    setPhotos([...photos, photo]);
    
    // Queue for OCR and upload
    await queueForSync({
      type: 'PROCESS_PHOTO',
      data: {
        photoId: photo.id,
        base64: data.base64,
        location
      }
    });
  };
  
  return (
    <View style={styles.container}>
      <RNCamera
        ref={cameraRef}
        style={styles.preview}
        type={RNCamera.Constants.Type.back}
        captureAudio={false}
      >
        {/* AR Overlay for document framing */}
        <View style={styles.overlay}>
          <View style={styles.frameGuide} />
          <Text style={styles.hint}>
            Dokument im Rahmen positionieren
          </Text>
        </View>
      </RNCamera>
      
      <TouchableOpacity
        onPress={takePicture}
        style={styles.captureButton}
      >
        <Icon name="camera" size={32} color="white" />
      </TouchableOpacity>
      
      {/* Recent photos strip */}
      <ScrollView 
        horizontal 
        style={styles.photoStrip}
        showsHorizontalScrollIndicator={false}
      >
        {photos.map(photo => (
          <TouchableOpacity
            key={photo.id}
            onPress={() => viewPhoto(photo)}
          >
            <Image 
              source={{ uri: `file://${photo.localPath}` }}
              style={styles.thumbnail}
            />
            {!photo.synced && (
              <View style={styles.syncBadge}>
                <Icon name="cloud-off" size={12} />
              </View>
            )}
          </TouchableOpacity>
        ))}
      </ScrollView>
    </View>
  );
};

// OCR Integration
export const processPhotoWithOCR = async (photo: Photo) => {
  const formData = new FormData();
  formData.append('image', {
    uri: `file://${photo.localPath}`,
    type: 'image/jpeg',
    name: 'photo.jpg'
  });
  
  try {
    const response = await fetch('/api/ocr/process', {
      method: 'POST',
      body: formData
    });
    
    const result = await response.json();
    
    // Extract structured data
    const extracted = {
      customerName: result.blocks.find(b => b.type === 'COMPANY')?.text,
      invoiceNumber: result.blocks.find(b => b.type === 'INVOICE_NO')?.text,
      amount: result.blocks.find(b => b.type === 'TOTAL')?.text,
      date: result.blocks.find(b => b.type === 'DATE')?.text
    };
    
    return extracted;
  } catch (error) {
    // Fallback to local processing
    return null;
  }
};
```

---

## 🚀 2. BACKEND API: GraphQL Extensions

### 2.1 Mobile-Optimized GraphQL Schema
```graphql
extend type Query {
  # Offline-friendly queries with minimal data
  mobileSync(lastSync: DateTime!): MobileSyncPayload!
  nearbyCustomers(location: LocationInput!, radius: Float): [Customer!]!
}

extend type Mutation {
  # Batch operations for efficient sync
  batchCreateOpportunities(input: [CreateOpportunityInput!]!): BatchResult!
  processVoiceNote(audio: Upload!, metadata: VoiceMetadata): VoiceProcessingResult!
  uploadPhoto(photo: Upload!, metadata: PhotoMetadata): PhotoUploadResult!
}

type MobileSyncPayload {
  opportunities: [Opportunity!]!
  customers: [Customer!]!
  deletedIds: [ID!]!
  serverTime: DateTime!
  syncToken: String!
}

type VoiceProcessingResult {
  transcript: String!
  entities: ExtractedEntities!
  suggestedActions: [SuggestedAction!]!
  confidence: Float!
}

input VoiceMetadata {
  duration: Int!
  location: LocationInput
  customerId: ID
  language: String
}
```

### 2.2 Sync API Implementation
```java
@GraphQLApi
public class MobileSyncResource {
    
    @Query
    public MobileSyncPayload mobileSync(LocalDateTime lastSync) {
        // Efficient delta sync
        List<Opportunity> opportunities = opportunityRepository
            .find("updatedAt > ?1 AND (assignedTo = ?2 OR createdBy = ?2)", 
                  lastSync, getCurrentUser())
            .list();
            
        List<Customer> customers = customerRepository
            .find("updatedAt > ?1 AND id IN ?2", 
                  lastSync, getCustomerIdsFromOpportunities(opportunities))
            .list();
            
        List<UUID> deletedIds = auditLogRepository
            .find("action = 'DELETE' AND timestamp > ?1", lastSync)
            .project(AuditLog::entityId)
            .list();
            
        return MobileSyncPayload.builder()
            .opportunities(opportunities)
            .customers(customers)
            .deletedIds(deletedIds)
            .serverTime(LocalDateTime.now())
            .syncToken(generateSyncToken())
            .build();
    }
    
    @Mutation
    @Transactional
    public BatchResult batchCreateOpportunities(List<CreateOpportunityInput> inputs) {
        List<String> successIds = new ArrayList<>();
        List<BatchError> errors = new ArrayList<>();
        
        for (CreateOpportunityInput input : inputs) {
            try {
                Opportunity opp = opportunityService.create(input);
                successIds.add(opp.getId().toString());
            } catch (Exception e) {
                errors.add(new BatchError(input.getClientId(), e.getMessage()));
            }
        }
        
        return new BatchResult(successIds, errors);
    }
}
```

---

## 🎨 3. SMART FEATURES

### 3.1 Location-Based Customer Suggestions
```typescript
export const useNearbyCustomers = (radius = 5000) => {
  const [location] = useLocation();
  
  const { data: customers } = useQuery({
    queryKey: ['nearby-customers', location, radius],
    queryFn: () => api.getNearbyCustomers(location, radius),
    enabled: !!location
  });
  
  // Smart sorting by relevance
  const sortedCustomers = useMemo(() => {
    if (!customers) return [];
    
    return customers.sort((a, b) => {
      // Prioritize by: distance, last visit, opportunity value
      const scoreA = calculateRelevanceScore(a, location);
      const scoreB = calculateRelevanceScore(b, location);
      return scoreB - scoreA;
    });
  }, [customers, location]);
  
  return sortedCustomers;
};
```

### 3.2 Smart Notifications
```typescript
// Push notification handler
export const setupPushNotifications = () => {
  PushNotification.configure({
    onNotification: (notification) => {
      if (notification.data.type === 'CUSTOMER_NEARBY') {
        // Show in-app alert
        Alert.alert(
          'Kunde in der Nähe!',
          `${notification.data.customerName} ist nur ${notification.data.distance}m entfernt`,
          [
            { text: 'Ignorieren' },
            { 
              text: 'Route planen',
              onPress: () => openMaps(notification.data.location)
            }
          ]
        );
      }
    }
  });
};
```

---

## 🚀 4. DEPLOYMENT

### 4.1 Build Configuration
```json
// app.json
{
  "expo": {
    "name": "FreshPlan Mobile",
    "slug": "freshplan-mobile",
    "version": "1.0.0",
    "orientation": "portrait",
    "plugins": [
      "@react-native-voice/voice",
      "react-native-camera",
      "react-native-background-fetch"
    ],
    "ios": {
      "bundleIdentifier": "de.freshplan.mobile",
      "infoPlist": {
        "NSMicrophoneUsageDescription": "Für Sprachnotizen",
        "NSCameraUsageDescription": "Für Dokumentenfotos",
        "NSLocationWhenInUseUsageDescription": "Für Kundenvorschläge"
      }
    },
    "android": {
      "package": "de.freshplan.mobile",
      "permissions": [
        "RECORD_AUDIO",
        "CAMERA",
        "ACCESS_FINE_LOCATION"
      ]
    }
  }
}
```

---

## ✅ 5. TESTING & PERFORMANCE

```typescript
// Offline Test
describe('Offline Functionality', () => {
  beforeEach(() => {
    NetInfo.fetch.mockResolvedValue({ isConnected: false });
  });
  
  it('should save opportunity locally when offline', async () => {
    const opportunity = await createOpportunity(testData);
    
    expect(opportunity.id).toMatch(/^local-/);
    expect(await getFromLocalDB(opportunity.id)).toBeDefined();
    expect(await getSyncQueueLength()).toBe(1);
  });
});
```

---

## 🎯 IMPLEMENTATION PRIORITIES

1. **Phase 1 (5 Tage)**: Voice Input + Offline Storage
2. **Phase 2 (5 Tage)**: Sync Engine + Conflict Resolution  
3. **Phase 3 (3 Tage)**: Photo Capture + OCR
4. **Phase 4 (2 Tage)**: Push Notifications + Location Features

**Geschätzter Aufwand:** 15 Entwicklungstage