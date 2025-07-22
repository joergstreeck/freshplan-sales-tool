# 🔌 FC-036 API & DATA MODEL

**Companion zu:** FC-036_OVERVIEW.md  
**Zweck:** Datenmodell, API & Privacy-Konzept  

---

## 📊 DATENMODELL

### Personal Details Schema
```typescript
interface PersonalDetails {
  id: string;
  contactId: string;
  userId: string; // Owner - nur dieser User kann sehen
  
  // Familie
  familyStatus?: 'single' | 'married' | 'divorced' | 'widowed';
  spouse?: {
    name: string;
    birthday?: Date;
  };
  children?: Array<{
    name: string;
    age?: number;
    interests?: string[];
  }>;
  pets?: Array<{
    type: string;
    name: string;
  }>;
  
  // Persönliches
  birthday?: Date;
  hobbies?: string[];
  favoriteFood?: string[];
  allergies?: string[];
  
  // Präferenzen
  communicationPreference?: 'email' | 'phone' | 'whatsapp' | 'personal';
  bestTimeToContact?: string;
  avoidTopics?: string[];
  
  // Metadaten
  createdAt: Date;
  updatedAt: Date;
  isPrivate: boolean; // Explizit privat markiert
}
```

### Gift History
```typescript
interface GiftRecord {
  id: string;
  contactId: string;
  date: Date;
  occasion: string; // 'birthday', 'christmas', 'anniversary'
  item: string;
  value?: number;
  reaction?: 'loved' | 'liked' | 'neutral' | 'disliked';
  notes?: string;
}
```

### Conversation Notes
```typescript
interface ConversationNote {
  id: string;
  contactId: string;
  userId: string;
  date: Date;
  content: string;
  tags?: string[]; // ['family', 'business', 'personal']
  sentiment?: 'positive' | 'neutral' | 'negative';
  followUpRequired?: boolean;
  isPrivate: boolean;
}
```

---

## 🔐 PRIVACY & SECURITY

### Datenschutz-Konzept
```typescript
// Persönliche Daten werden LOKAL gespeichert
// Keine Synchronisation über Server

// IndexedDB für lokale Speicherung
const db = new Dexie('RelationshipData');
db.version(1).stores({
  personalDetails: 'id, contactId, userId',
  giftHistory: 'id, contactId, date',
  conversationNotes: 'id, contactId, date'
});

// Verschlüsselung sensibler Daten
const encryptPersonalData = async (data: PersonalDetails) => {
  const key = await getUserEncryptionKey();
  return CryptoJS.AES.encrypt(
    JSON.stringify(data), 
    key
  ).toString();
};
```

### Backup & Export
```typescript
// Lokales Backup erstellen
const exportPersonalData = async (userId: string) => {
  const data = {
    personalDetails: await db.personalDetails
      .where('userId').equals(userId).toArray(),
    giftHistory: await db.giftHistory.toArray(),
    notes: await db.conversationNotes
      .where('userId').equals(userId).toArray()
  };
  
  // Als verschlüsselte Datei exportieren
  const blob = new Blob([
    await encryptPersonalData(data)
  ], { type: 'application/json' });
  
  saveAs(blob, `relationship_backup_${Date.now()}.enc`);
};
```

---

## 🔌 API ENDPOINTS (Optional Server Sync)

### Personal Reminders API
```typescript
// GET /api/relationship/reminders
interface RemindersResponse {
  reminders: Array<{
    contactId: string;
    type: 'birthday' | 'anniversary' | 'followup';
    date: Date;
    message: string;
    priority: 'high' | 'medium' | 'low';
  }>;
}

// POST /api/relationship/reminders
interface CreateReminderRequest {
  contactId: string;
  type: string;
  date: Date;
  recurring?: boolean;
}
```

### Relationship Analytics
```typescript
// GET /api/relationship/analytics
interface RelationshipAnalytics {
  totalContacts: number;
  withPersonalDetails: number;
  upcomingBirthdays: number;
  overdueFollowUps: number;
  strongRelationships: number;
  weakRelationships: number;
}
```

---

## 🤖 SMART FEATURES

### Relationship Scoring Algorithm
```typescript
const calculateRelationshipScore = (contact: Contact): RelationshipScore => {
  let score = 0;
  const factors = {
    // Kommunikation
    lastContactDays: getDaysSince(contact.lastContact),
    totalInteractions: contact.interactions?.length || 0,
    
    // Persönliche Details
    hasPersonalDetails: !!contact.personalDetails,
    detailsCompleteness: calculateCompleteness(contact.personalDetails),
    
    // Engagement
    meetingsCount: contact.meetings?.length || 0,
    giftsGiven: contact.giftHistory?.length || 0,
    
    // Business
    dealsClosed: contact.opportunities?.filter(o => o.won).length || 0,
    totalRevenue: contact.totalRevenue || 0
  };
  
  // Scoring Logic
  if (factors.lastContactDays < 30) score += 20;
  if (factors.lastContactDays < 60) score += 10;
  if (factors.hasPersonalDetails) score += 15;
  if (factors.detailsCompleteness > 0.7) score += 10;
  if (factors.meetingsCount > 5) score += 15;
  if (factors.giftsGiven > 0) score += 10;
  
  return {
    score: Math.min(score, 100),
    percentage: score,
    level: score > 70 ? 'Stark' : score > 40 ? 'Mittel' : 'Schwach',
    factors
  };
};
```

### Smart Reminder Generation
```typescript
const generateSmartReminders = async (contacts: Contact[]) => {
  const reminders = [];
  
  for (const contact of contacts) {
    // Birthday reminders
    if (contact.birthday) {
      const days = getDaysUntilBirthday(contact.birthday);
      if (days === 14) {
        reminders.push({
          type: 'birthday_advance',
          contact,
          message: 'Geburtstag in 2 Wochen - Karte besorgen?',
          priority: 'medium'
        });
      }
    }
    
    // Relationship maintenance
    const score = calculateRelationshipScore(contact);
    if (score.percentage < 40 && contact.isKeyAccount) {
      reminders.push({
        type: 'relationship_weak',
        contact,
        message: 'Wichtiger Kunde - Beziehung stärken!',
        priority: 'high',
        suggestions: [
          'Persönliches Treffen vereinbaren',
          'Zum Essen einladen',
          'Event-Einladung senden'
        ]
      });
    }
  }
  
  return reminders;
};
```

---

## 🔄 SYNC & CONFLICT RESOLUTION

### Multi-Device Sync (Optional)
```typescript
// Wenn Server-Sync gewünscht
const syncPersonalData = async () => {
  const localData = await db.personalDetails.toArray();
  const serverData = await api.getPersonalDetails();
  
  // Conflict resolution - neueste Version gewinnt
  const merged = mergeByTimestamp(localData, serverData);
  
  // Verschlüsselt zum Server
  await api.updatePersonalDetails(
    await encryptForServer(merged)
  );
};
```

### Data Retention
```typescript
// DSGVO-konform: Automatisches Löschen
const cleanupOldData = async () => {
  const twoYearsAgo = new Date();
  twoYearsAgo.setFullYear(twoYearsAgo.getFullYear() - 2);
  
  // Lösche alte Notizen
  await db.conversationNotes
    .where('date')
    .below(twoYearsAgo)
    .delete();
    
  // Archiviere Geschenk-Historie
  const oldGifts = await db.giftHistory
    .where('date')
    .below(twoYearsAgo)
    .toArray();
    
  await archiveData(oldGifts);
};
```