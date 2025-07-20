# 🗣️ FC-029 VOICE-FIRST INTERFACE (KOMPAKT)

**Erstellt:** 18.07.2025  
**Status:** 📋 READY TO START  
**Feature-Typ:** 🎨 FRONTEND  
**Priorität:** HIGH - Außendienst Essential  
**Geschätzt:** 2 Tage (Basic: 1 Tag in Mobile Light)  

---

## 🧠 WAS WIR BAUEN

**Problem:** Tippen während Fahrt unmöglich  
**Lösung:** Sprechen statt Tippen  
**Value:** Sicher und 3x schneller  

> **Business Case:** 50% weniger Tipparbeit = 2h/Tag gespart

### 🎯 Voice Commands:
- **Create:** "Neuer Kontakt bei Bosch"
- **Update:** "Meeting war erfolgreich"
- **Navigate:** "Zeige Kunden in Hamburg"
- **Quick Notes:** "Notiz: Kunde will Referenzen"

---

## 🚀 SOFORT LOSLEGEN (15 MIN)

### 1. **Web Speech API:**
```typescript
// Browser native - no libs needed!
const recognition = new webkitSpeechRecognition();
recognition.lang = 'de-DE';
recognition.continuous = true;

recognition.onresult = (event) => {
  const transcript = event.results[0][0].transcript;
  processVoiceCommand(transcript);
};
```

### 2. **Voice Button:**
```typescript
<VoiceButton
  onTranscript={(text) => {
    const command = parseCommand(text);
    executeCommand(command);
  }}
/>
```

### 3. **Command Parser:**
```typescript
const commands = {
  'neuer kontakt': CREATE_CONTACT,
  'neue notiz': CREATE_NOTE,
  'zeige kunden': SHOW_CUSTOMERS,
  'meeting erfolgreich': UPDATE_SUCCESS
};
```

---

## 🎙️ VOICE UI PATTERNS

### Push-to-Talk:
```typescript
const VoiceButton = () => {
  const [listening, setListening] = useState(false);
  
  return (
    <Fab
      color={listening ? "secondary" : "primary"}
      onMouseDown={() => startListening()}
      onMouseUp={() => stopListening()}
      onTouchStart={() => startListening()}
      onTouchEnd={() => stopListening()}
    >
      {listening ? <MicIcon /> : <MicOffIcon />}
    </Fab>
  );
};
```

### Voice Feedback:
```typescript
// Visual feedback während Sprechen
<VoiceIndicator active={listening}>
  <WaveForm amplitude={voiceLevel} />
  <Typography variant="caption">
    {transcript || "Sprechen Sie jetzt..."}
  </Typography>
</VoiceIndicator>
```

---

## 🧠 NATURAL LANGUAGE PROCESSING

### Smart Command Parser:
```typescript
interface VoiceCommand {
  action: 'CREATE' | 'UPDATE' | 'SEARCH' | 'NAVIGATE';
  entity: 'customer' | 'contact' | 'note' | 'appointment';
  data: Record<string, any>;
}

function parseVoiceCommand(text: string): VoiceCommand {
  // "Neuer Kontakt bei Bosch, Herr Müller, Einkaufsleiter"
  
  const patterns = {
    createContact: /neuer? kontakt bei (.*?), (.*?), (.*)/i,
    createNote: /notiz:? (.*)/i,
    updateStatus: /meeting (erfolgreich|gescheitert)/i,
    search: /zeige( alle)? kunden (in|von) (.*)/i
  };
  
  // Match patterns and extract data
  for (const [key, pattern] of Object.entries(patterns)) {
    const match = text.match(pattern);
    if (match) {
      return buildCommand(key, match);
    }
  }
}
```

### Context-Aware Commands:
```typescript
// Voice versteht aktuellen Kontext
const VoiceContext = {
  currentCustomer: customer,
  currentView: 'opportunity_detail',
  
  interpret(text: string) {
    // "Wiedervorlage nächste Woche"
    // → Bezieht sich auf current customer
    
    if (text.includes('wiedervorlage')) {
      return {
        action: 'CREATE_FOLLOWUP',
        customerId: this.currentCustomer.id,
        date: parseRelativeDate('nächste woche')
      };
    }
  }
};
```

---

## 📱 MOBILE INTEGRATION

### Hands-Free Mode:
```typescript
// Aktiviert bei Bewegung
const HandsFreeMode = () => {
  const [autoListen, setAutoListen] = useState(false);
  
  // Detect driving via accelerometer
  useMotionDetection((motion) => {
    if (motion.speed > 20) { // km/h
      setAutoListen(true);
    }
  });
  
  return (
    <HandsFreeBanner active={autoListen}>
      🚗 Freisprechmodus aktiv
      Sage "Hey FreshPlan" zum Starten
    </HandsFreeBanner>
  );
};
```

### Voice Navigation:
```typescript
// Komplett ohne Touch bedienbar
const voiceNavigation = {
  'zurück': () => history.back(),
  'home': () => navigate('/'),
  'meine kunden': () => navigate('/customers'),
  'hilfe': () => showVoiceHelp()
};
```

---

## 🎯 PRAKTISCHE VOICE FLOWS

### Nach Kundenbesuch:
```
👤: "Meeting bei Müller beendet"
🤖: "Wie war das Meeting?"
👤: "Erfolgreich, 100k Potential"
🤖: "Super! Soll ich eine Wiedervorlage anlegen?"
👤: "Ja, nächste Woche Mittwoch"
🤖: "Erledigt. Notizen hinzufügen?"
👤: "Ja - Kunde will Referenzen von Hotels"
🤖: "Gespeichert. Nächster Termin in 30 Min bei Schmidt."
```

### Schnelle Suche:
```
👤: "Zeige alle Kunden in Hamburg"
🤖: "15 Kunden in Hamburg gefunden"
👤: "Nur A-Kunden"
🤖: "3 A-Kunden in Hamburg"
👤: "Route planen"
🤖: "Route mit 3 Stopps wird berechnet..."
```

---

## 🔧 TECHNICAL IMPLEMENTATION

### Voice Service:
```typescript
class VoiceService {
  recognition: SpeechRecognition;
  synthesis: SpeechSynthesis;
  
  constructor() {
    this.setupRecognition();
    this.setupCommands();
  }
  
  speak(text: string) {
    const utterance = new SpeechSynthesisUtterance(text);
    utterance.lang = 'de-DE';
    utterance.rate = 1.1; // Slightly faster
    this.synthesis.speak(utterance);
  }
  
  async processCommand(transcript: string) {
    const command = this.parseCommand(transcript);
    const result = await this.executeCommand(command);
    this.speak(result.feedback);
  }
}
```

### Offline Voice:
```typescript
// Basis-Commands offline verfügbar
const offlineCommands = new Map([
  ['neue notiz', () => createOfflineNote()],
  ['zeige notizen', () => showOfflineNotes()],
  ['diktat', () => startDictation()]
]);
```

---

## 📞 IMPLEMENTATION PHASES

### Phase 1 (Tag 10 - Mobile Light):
1. **Basic Voice Input** - Notizen diktieren
2. **Simple Commands** - Create, Search
3. **Voice Button** - Push-to-talk

### Phase 2 (Tag 23-24 - Full):
4. **Natural Language** - Komplexe Commands
5. **Voice Navigation** - Hands-free
6. **Conversations** - Dialoge
7. **Multi-Language** - DE/EN

**TIPP:** Mit Chrome Mobile testen - beste Speech API!

---

## 🧭 NAVIGATION & VERWEISE

### 📋 Zurück zum Überblick:
- **[📊 Master Plan V5](/docs/CRM_COMPLETE_MASTER_PLAN_V5.md)** - Vollständige Feature-Roadmap
- **[🗺️ Feature Overview](/docs/features/MASTER/FEATURE_OVERVIEW.md)** - Alle 40 Features im Überblick

### 🔗 Dependencies (Required):
- **[📱 FC-022 Mobile Light](/docs/features/PLANNED/22_mobile_light/FC-022_KOMPAKT.md)** - Mobile UI Foundation
- **[🔒 FC-008 Security Foundation](/docs/features/ACTIVE/01_security_foundation/FC-008_KOMPAKT.md)** - User Context
- **[🔒 FC-009 Permissions](/docs/features/ACTIVE/04_permissions_system/FC-009_KOMPAKT.md)** - Voice Permissions

### ⚡ Audio & Datenquellen:
- **[📈 FC-014 Activity Timeline](/docs/features/PLANNED/16_activity_timeline/FC-014_KOMPAKT.md)** - Voice Activities
- **[📧 FC-028 WhatsApp Integration](/docs/features/PLANNED/28_whatsapp_integration/FC-028_KOMPAKT.md)** - Voice-to-Text Messages
- **[📱 FC-018 Mobile PWA](/docs/features/PLANNED/18_mobile_pwa/FC-018_KOMPAKT.md)** - Offline Voice

### 🚀 Ermöglicht folgende Features:
- **[👆 FC-030 One-Tap Actions](/docs/features/PLANNED/30_one_tap_actions/FC-030_KOMPAKT.md)** - Voice Shortcuts
- **[📱 FC-031 Smart Templates](/docs/features/PLANNED/31_smart_templates/FC-031_KOMPAKT.md)** - Voice Template Fill
- **[🏆 FC-017 Sales Gamification](/docs/features/PLANNED/99_sales_gamification/FC-017_KOMPAKT.md)** - Voice Achievements

### 🎨 UI Integration:
- **[📊 M3 Sales Cockpit](/docs/features/ACTIVE/05_ui_foundation/M3_SALES_COCKPIT_KOMPAKT.md)** - Voice Button Integration
- **[⚡ M2 Quick Create](/docs/features/ACTIVE/05_ui_foundation/M2_QUICK_CREATE_KOMPAKT.md)** - Voice Quick Create
- **[🧭 M1 Navigation](/docs/features/ACTIVE/05_ui_foundation/M1_NAVIGATION_KOMPAKT.md)** - Voice Navigation

### 🔧 Technische Details:
- [FC-029_IMPLEMENTATION_GUIDE.md](./FC-029_IMPLEMENTATION_GUIDE.md) *(geplant)* - Speech API Setup
- [FC-029_DECISION_LOG.md](./FC-029_DECISION_LOG.md) *(geplant)* - Web Speech vs. Cloud APIs
- [VOICE_COMMANDS.md](./VOICE_COMMANDS.md) *(geplant)* - Vollständige Command Liste