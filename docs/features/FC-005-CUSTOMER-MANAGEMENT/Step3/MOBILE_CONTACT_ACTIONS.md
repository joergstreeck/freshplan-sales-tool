# 📱 Mobile Contact Actions - Quick Actions für Vertrieb unterwegs

**Phase:** 1 - Foundation  
**Tag:** 4 der Woche 1  
**Status:** 📋 GEPLANT  
**Letzte Aktualisierung:** 08.08.2025  
**Claude-Ready:** ✅ Vollständig navigierbar

## 🧭 NAVIGATION FÜR CLAUDE

**← Zurück:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/SMART_CONTACT_CARDS.md`  
**→ Nächster:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/RELATIONSHIP_INTELLIGENCE.md`  
**↑ Parent:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/README.md`  
**⚠️ Voraussetzungen:**
- `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/FRONTEND_FOUNDATION.md` ✅
- `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/SMART_CONTACT_CARDS.md` ✅

## ⚡ Quick Implementation Guide für Claude

```bash
# SOFORT STARTEN - Pfade für Copy & Paste:
cd /Users/joergstreeck/freshplan-sales-tool

# 1. Voraussetzungen prüfen
cat frontend/src/features/customers/components/contacts/SmartContactCard.tsx
# → Muss existieren, sonst zuerst SMART_CONTACT_CARDS.md implementieren!

# 2. Mobile Components erstellen
mkdir -p frontend/src/features/customers/components/mobile
touch frontend/src/features/customers/components/mobile/SwipeableContactCard.tsx
touch frontend/src/features/customers/components/mobile/MobileActionBar.tsx
touch frontend/src/features/customers/components/mobile/MobileContactFAB.tsx
touch frontend/src/features/customers/components/mobile/ActionSuggestionService.ts

# 3. Action Services erstellen
mkdir -p frontend/src/features/customers/services/actions
touch frontend/src/features/customers/services/actions/ActionExecutionService.ts
touch frontend/src/features/customers/services/actions/OfflineQueueService.ts

# 4. Tests vorbereiten
mkdir -p frontend/src/features/customers/components/mobile/__tests__
touch frontend/src/features/customers/components/mobile/__tests__/SwipeableContactCard.test.tsx
```

## 📦 IMPLEMENTATION REQUIREMENTS

### Abhängigkeiten (MÜSSEN existieren!):

| Komponente | Pfad | Status | Implementiert in |
|------------|------|--------|------------------|
| SmartContactCard | `frontend/src/features/customers/components/contacts/SmartContactCard.tsx` | ⚠️ Prüfen | [SMART_CONTACT_CARDS.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/SMART_CONTACT_CARDS.md) |
| ContactStore | `frontend/src/features/customers/stores/contactStore.ts` | ⚠️ Prüfen | [FRONTEND_FOUNDATION.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/FRONTEND_FOUNDATION.md) |
| react-swipeable | `package.json` | ❌ Installieren | `npm install react-swipeable` |
| Service Worker | `public/sw.js` | ❌ Erstellen | PWA Setup |

### Neue Dateien (werden erstellt):

```
frontend/src/features/customers/
├── components/mobile/
│   ├── SwipeableContactCard.tsx     # Swipe-fähige Card
│   ├── MobileActionBar.tsx          # Mobile Action Buttons
│   ├── MobileContactFAB.tsx         # Floating Action Button
│   └── ActionSuggestionService.ts   # Smart Suggestions
├── services/actions/
│   ├── ActionExecutionService.ts    # Action Handler
│   └── OfflineQueueService.ts       # Offline Support
└── hooks/
    └── useUnifiedActions.ts          # Cross-Platform Hook
```

## 🎯 Vision: Vertrieb unterwegs optimieren

**Mobile Contact Actions** verwandelt das Smartphone in ein **mächtiges Vertriebstool**:

> "Ein Swipe nach rechts = sofortiger Anruf beim wichtigsten Kontakt"

### 💬 Team-Feedback:
> "Gamechanger für Adoption und Akzeptanz. Top-UX für Außendienst/Messe. CRM-Light im Alltag, volle Kraft im Detail."

## 📱 Mobile-First Action System

### 1. Quick Action Types (CORE)

**Datei:** `frontend/src/features/customers/types/mobileActions.types.ts`

```typescript
// CLAUDE: Erstelle diese Types ZUERST!
// Pfad: frontend/src/features/customers/types/mobileActions.types.ts

export interface QuickAction {
  id: string;
  type: 'call' | 'email' | 'whatsapp' | 'sms' | 'calendar' | 'note';
  label: string;
  icon: React.ReactNode;
  color: string;
  primary?: boolean;      // User-definierte Primäraktion
  contextual?: boolean;   // Basierend auf Kontaktdaten angezeigt
  urgency?: 'high' | 'medium' | 'low';
}

export interface SwipeActions {
  left: QuickAction;   // Swipe left action
  right: QuickAction;  // Swipe right action
}

export interface ContactActionConfig {
  contactId: string;
  primaryAction: QuickAction;
  secondaryActions: QuickAction[];
  swipeActions: SwipeActions;
  lastUsedAction?: string;
  actionHistory: ActionHistory[];
}

export interface ActionHistory {
  actionId: string;
  timestamp: Date;
  outcome: 'successful' | 'failed' | 'pending';
  responseTime?: number;
}

// ColdStart Defaults für neue Kontakte
export const DEFAULT_SWIPE_ACTIONS: SwipeActions = {
  left: {
    id: 'email-default',
    type: 'email',
    label: 'E-Mail',
    icon: '<EmailIcon />',
    color: '#2196F3'
  },
  right: {
    id: 'call-default',
    type: 'call',
    label: 'Anrufen',
    icon: '<PhoneIcon />',
    color: '#4CAF50'
  }
};
```

### 2. Action Suggestion Service (INTELLIGENCE)

**Datei:** `frontend/src/features/customers/components/mobile/ActionSuggestionService.ts`  
**Größe:** ~200 Zeilen  
**Wichtig:** Nutzt Warmth & Freshness Daten für intelligente Vorschläge

```typescript
// CLAUDE: Smart Suggestions basierend auf Kontext
// Pfad: frontend/src/features/customers/components/mobile/ActionSuggestionService.ts

import { CustomerContact, ContactIntelligence, DataFreshness } from '../../types/contact.types';
import { QuickAction } from '../../types/mobileActions.types';
import { 
  Phone as PhoneIcon,
  Email as EmailIcon,
  WhatsApp as WhatsAppIcon,
  Cake as CakeIcon,
  Warning as WarningIcon,
  Event as EventIcon,
  TrendingUp as TrendingUpIcon
} from '@mui/icons-material';

export class ActionSuggestionService {
  
  /**
   * Generiert kontextbasierte Action-Vorschläge
   * Priorisiert nach: Urgency → Warmth → Availability → History
   */
  getSuggestedActions(
    contact: CustomerContact, 
    intelligence?: ContactIntelligence
  ): QuickAction[] {
    const actions: QuickAction[] = [];
    
    // 1. URGENT: Geburtstag
    if (this.isBirthdayUpcoming(contact.birthday)) {
      actions.push({
        id: 'birthday-call',
        type: 'call',
        label: '🎂 Geburtstag gratulieren',
        icon: <CakeIcon />,
        color: '#E91E63',
        primary: true,
        urgency: 'high'
      });
    }
    
    // 2. URGENT: Beziehung kühlt ab
    if (intelligence?.freshnessLevel === 'critical') {
      actions.push({
        id: 'urgent-reconnect',
        type: 'call',
        label: '🔥 Dringend kontaktieren',
        icon: <WarningIcon />,
        color: '#FF5722',
        primary: true,
        urgency: 'high'
      });
    }
    
    // 3. OPPORTUNITY: Warmth steigt
    if (intelligence?.trendDirection === 'improving') {
      actions.push({
        id: 'momentum-follow',
        type: 'meeting',
        label: '📈 Momentum nutzen',
        icon: <TrendingUpIcon />,
        color: '#4CAF50',
        primary: true,
        urgency: 'medium'
      });
    }
    
    // 4. STANDARD: Basierend auf verfügbaren Daten
    if (contact.phone || contact.mobile) {
      actions.push({
        id: 'call-standard',
        type: 'call',
        label: 'Anrufen',
        icon: <PhoneIcon />,
        color: '#4CAF50',
        contextual: true
      });
    }
    
    if (contact.mobile) {
      actions.push({
        id: 'whatsapp',
        type: 'whatsapp',
        label: 'WhatsApp',
        icon: <WhatsAppIcon />,
        color: '#25D366',
        contextual: true
      });
    }
    
    if (contact.email) {
      actions.push({
        id: 'email',
        type: 'email',
        label: 'E-Mail',
        icon: <EmailIcon />,
        color: '#2196F3',
        contextual: true
      });
    }
    
    // 5. ALWAYS AVAILABLE: Universelle Aktionen
    actions.push({
      id: 'schedule',
      type: 'calendar',
      label: 'Termin planen',
      icon: <EventIcon />,
      color: '#FF9800'
    });
    
    // Sortierung nach Priorität
    return this.prioritizeActions(actions, contact, intelligence);
  }
  
  private prioritizeActions(
    actions: QuickAction[], 
    contact: CustomerContact,
    intelligence?: ContactIntelligence
  ): QuickAction[] {
    return actions.sort((a, b) => {
      // Urgency first
      const urgencyOrder = { high: 0, medium: 1, low: 2, undefined: 3 };
      const urgencyDiff = (urgencyOrder[a.urgency || 'undefined']) - 
                          (urgencyOrder[b.urgency || 'undefined']);
      if (urgencyDiff !== 0) return urgencyDiff;
      
      // Primary actions second
      if (a.primary && !b.primary) return -1;
      if (!a.primary && b.primary) return 1;
      
      // Preferred communication method
      if (contact.preferredCommunicationMethod) {
        const preferred = contact.preferredCommunicationMethod.toLowerCase();
        if (a.type === preferred) return -1;
        if (b.type === preferred) return 1;
      }
      
      return 0;
    });
  }
  
  private isBirthdayUpcoming(birthday?: Date | string): boolean {
    if (!birthday) return false;
    
    const today = new Date();
    const birthdayDate = new Date(birthday);
    birthdayDate.setFullYear(today.getFullYear());
    
    const daysUntil = Math.ceil(
      (birthdayDate.getTime() - today.getTime()) / (1000 * 60 * 60 * 24)
    );
    
    return daysUntil >= 0 && daysUntil <= 7;
  }
  
  /**
   * Generiert optimale Swipe-Actions basierend auf Kontext
   */
  getSwipeActions(
    contact: CustomerContact,
    intelligence?: ContactIntelligence
  ): SwipeActions {
    const suggestions = this.getSuggestedActions(contact, intelligence);
    
    // Finde beste Actions für Links/Rechts
    const callAction = suggestions.find(a => a.type === 'call') || DEFAULT_SWIPE_ACTIONS.right;
    const emailAction = suggestions.find(a => a.type === 'email') || DEFAULT_SWIPE_ACTIONS.left;
    const whatsappAction = suggestions.find(a => a.type === 'whatsapp');
    
    return {
      // Rechts = Primäraktion (meist Call)
      right: callAction.urgency === 'high' ? callAction : 
             (whatsappAction && contact.preferredCommunicationMethod === 'WHATSAPP' ? 
              whatsappAction : callAction),
      
      // Links = Sekundäraktion (meist Email)
      left: emailAction
    };
  }
}

// Singleton Export
export const actionSuggestionService = new ActionSuggestionService();
```

### 3. Swipeable Contact Card (HAUPTKOMPONENTE)

**Datei:** `frontend/src/features/customers/components/mobile/SwipeableContactCard.tsx`  
**Größe:** ~300 Zeilen  
**Dependencies:** react-swipeable, SmartContactCard, ActionSuggestionService

```typescript
// CLAUDE: Swipe-fähige Contact Card für Mobile
// Pfad: frontend/src/features/customers/components/mobile/SwipeableContactCard.tsx

import React, { useState, useEffect } from 'react';
import { useSwipeable } from 'react-swipeable';
import { Box, Card, CardContent, Typography, Fade } from '@mui/material';
import { SmartContactCard } from '../contacts/SmartContactCard';
import { MobileActionBar } from './MobileActionBar';
import { actionSuggestionService } from './ActionSuggestionService';
import { CustomerContact, ContactIntelligence } from '../../types/contact.types';
import { QuickAction, SwipeActions } from '../../types/mobileActions.types';

interface SwipeableContactCardProps {
  contact: CustomerContact;
  intelligence?: ContactIntelligence;
  onAction: (action: QuickAction) => void;
  onEdit?: (contact: CustomerContact) => void;
  onDelete?: (contactId: string) => void;
}

export const SwipeableContactCard: React.FC<SwipeableContactCardProps> = ({
  contact,
  intelligence,
  onAction,
  onEdit,
  onDelete
}) => {
  const [swipeActions, setSwipeActions] = useState<SwipeActions>();
  const [swipeProgress, setSwipeProgress] = useState(0);
  const [swipeDirection, setSwipeDirection] = useState<'left' | 'right' | null>(null);
  
  useEffect(() => {
    const actions = actionSuggestionService.getSwipeActions(contact, intelligence);
    setSwipeActions(actions);
  }, [contact, intelligence]);
  
  const handlers = useSwipeable({
    onSwiping: (eventData) => {
      const progress = Math.min(Math.abs(eventData.deltaX) / 100, 1);
      setSwipeProgress(progress);
      setSwipeDirection(eventData.deltaX < 0 ? 'left' : 'right');
    },
    onSwipedLeft: () => {
      if (swipeActions?.left) {
        // Haptic Feedback
        if ('vibrate' in navigator) {
          navigator.vibrate(50);
        }
        onAction(swipeActions.left);
      }
      resetSwipe();
    },
    onSwipedRight: () => {
      if (swipeActions?.right) {
        // Haptic Feedback
        if ('vibrate' in navigator) {
          navigator.vibrate(50);
        }
        onAction(swipeActions.right);
      }
      resetSwipe();
    },
    onTouchEndOrOnMouseUp: () => {
      if (swipeProgress < 0.5) {
        resetSwipe();
      }
    },
    preventDefaultTouchmoveEvent: true,
    trackMouse: false, // Nur Touch, kein Mouse für Mobile
    delta: 10,
    swipeThreshold: 50
  });
  
  const resetSwipe = () => {
    setSwipeProgress(0);
    setSwipeDirection(null);
  };
  
  return (
    <Box 
      {...handlers} 
      sx={{ 
        position: 'relative',
        touchAction: 'pan-y',
        userSelect: 'none'
      }}
    >
      {/* Swipe Indicators */}
      <SwipeIndicator 
        direction="left"
        action={swipeActions?.left}
        progress={swipeDirection === 'left' ? swipeProgress : 0}
      />
      <SwipeIndicator 
        direction="right"
        action={swipeActions?.right}
        progress={swipeDirection === 'right' ? swipeProgress : 0}
      />
      
      {/* Main Card with Transform */}
      <Box
        sx={{
          transform: swipeDirection ? 
            `translateX(${swipeDirection === 'right' ? swipeProgress * 50 : -swipeProgress * 50}px)` : 
            'translateX(0)',
          transition: swipeProgress === 0 ? 'transform 0.3s ease' : 'none',
          opacity: 1 - (swipeProgress * 0.2)
        }}
      >
        <SmartContactCard
          contact={contact}
          warmth={intelligence}
          onEdit={onEdit}
          onDelete={onDelete}
          onSetPrimary={() => {}}
          onAssignLocation={() => {}}
          onQuickAction={onAction}
        />
        
        {/* Mobile Action Bar unterhalb der Card */}
        <MobileActionBar
          contact={contact}
          suggestions={actionSuggestionService.getSuggestedActions(contact, intelligence)}
          onAction={onAction}
        />
      </Box>
      
      {/* Swipe Instructions (nur beim ersten Mal) */}
      <SwipeInstructions 
        show={!localStorage.getItem('swipe-instructions-seen')}
        onDismiss={() => localStorage.setItem('swipe-instructions-seen', 'true')}
      />
    </Box>
  );
};

// Swipe Indicator Component
const SwipeIndicator: React.FC<{
  direction: 'left' | 'right';
  action?: QuickAction;
  progress: number;
}> = ({ direction, action, progress }) => {
  if (!action) return null;
  
  return (
    <Fade in={progress > 0.1}>
      <Box
        sx={{
          position: 'absolute',
          top: '50%',
          [direction]: 16,
          transform: 'translateY(-50%)',
          backgroundColor: action.color,
          color: 'white',
          borderRadius: '50%',
          width: 56 + (progress * 20),
          height: 56 + (progress * 20),
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          opacity: progress,
          zIndex: 10,
          transition: 'all 0.2s ease'
        }}
      >
        {action.icon}
      </Box>
    </Fade>
  );
};

// Swipe Instructions Overlay
const SwipeInstructions: React.FC<{
  show: boolean;
  onDismiss: () => void;
}> = ({ show, onDismiss }) => {
  if (!show) return null;
  
  return (
    <Fade in={show}>
      <Box
        onClick={onDismiss}
        sx={{
          position: 'absolute',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(0,0,0,0.7)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 100,
          padding: 3
        }}
      >
        <Card sx={{ maxWidth: 300, textAlign: 'center' }}>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              Swipe für Schnellaktionen
            </Typography>
            <Typography variant="body2" paragraph>
              👈 Nach links für E-Mail
            </Typography>
            <Typography variant="body2" paragraph>
              👉 Nach rechts für Anruf
            </Typography>
            <Typography variant="caption" color="text.secondary">
              Tippen zum Schließen
            </Typography>
          </CardContent>
        </Card>
      </Box>
    </Fade>
  );
};
```

### 4. Action Execution Service (HANDLER)

**Datei:** `frontend/src/features/customers/services/actions/ActionExecutionService.ts`  
**Größe:** ~250 Zeilen  
**Wichtig:** Handled alle Action-Types und Offline-Queue

```typescript
// CLAUDE: Service für Action-Ausführung mit Offline-Support
// Pfad: frontend/src/features/customers/services/actions/ActionExecutionService.ts

import { QuickAction } from '../../types/mobileActions.types';
import { CustomerContact } from '../../types/contact.types';
import { contactApi } from '../contactApi';
import { offlineQueueService } from './OfflineQueueService';

export class ActionExecutionService {
  
  async executeAction(
    action: QuickAction, 
    contact: CustomerContact
  ): Promise<void> {
    // Track für Analytics
    this.trackAction(action, contact);
    
    try {
      switch (action.type) {
        case 'call':
          await this.initiateCall(contact);
          break;
        case 'email':
          await this.composeEmail(contact);
          break;
        case 'whatsapp':
          await this.openWhatsApp(contact);
          break;
        case 'sms':
          await this.composeSMS(contact);
          break;
        case 'calendar':
          await this.scheduleAppointment(contact);
          break;
        case 'note':
          await this.addQuickNote(contact);
          break;
      }
      
      // Log successful interaction
      await this.recordInteraction(contact.id, action.type, 'successful');
      
    } catch (error) {
      // Queue for retry if offline
      if (!navigator.onLine) {
        await offlineQueueService.queueAction(action, contact);
      }
      throw error;
    }
  }
  
  private async initiateCall(contact: CustomerContact): Promise<void> {
    const phoneNumber = contact.mobile || contact.phone;
    if (!phoneNumber) {
      throw new Error('Keine Telefonnummer verfügbar');
    }
    
    // Mobile/Desktop Detection
    if (this.isMobileDevice()) {
      window.location.href = `tel:${phoneNumber}`;
    } else {
      // Desktop: Show Click-to-Call Dialog
      this.showCallDialog(contact, phoneNumber);
    }
  }
  
  private async composeEmail(contact: CustomerContact): Promise<void> {
    if (!contact.email) {
      throw new Error('Keine E-Mail-Adresse verfügbar');
    }
    
    const subject = this.generateEmailSubject(contact);
    const body = this.generateEmailTemplate(contact);
    
    const mailtoLink = `mailto:${contact.email}?subject=${encodeURIComponent(subject)}&body=${encodeURIComponent(body)}`;
    window.location.href = mailtoLink;
  }
  
  private async openWhatsApp(contact: CustomerContact): Promise<void> {
    const phoneNumber = contact.mobile?.replace(/\D/g, '');
    if (!phoneNumber) {
      throw new Error('Keine Mobilnummer für WhatsApp');
    }
    
    const message = this.generateWhatsAppTemplate(contact);
    const whatsappUrl = `https://wa.me/${phoneNumber}?text=${encodeURIComponent(message)}`;
    
    window.open(whatsappUrl, '_blank');
  }
  
  private generateEmailSubject(contact: CustomerContact): string {
    // Intelligente Betreffzeile basierend auf Kontext
    const now = new Date();
    const hour = now.getHours();
    
    if (this.isBirthdayToday(contact.birthday)) {
      return `Herzlichen Glückwunsch zum Geburtstag! 🎂`;
    }
    
    if (contact.freshnessLevel === 'critical') {
      return `Lange nicht gesprochen - Zeit für ein Update?`;
    }
    
    return `Kontakt bezüglich Ihrer Anfrage`;
  }
  
  private generateEmailTemplate(contact: CustomerContact): string {
    const greeting = this.getTimeBasedGreeting();
    
    return `${greeting} ${contact.salutation || ''} ${contact.lastName},

ich hoffe, es geht Ihnen gut!

[Ihr Anliegen hier]

Mit freundlichen Grüßen
${this.getCurrentUserName()}
FreshPlan Team

--
📱 Gesendet von FreshPlan Mobile`;
  }
  
  private generateWhatsAppTemplate(contact: CustomerContact): string {
    const greeting = this.getTimeBasedGreeting();
    return `${greeting} ${contact.firstName}, kurze Frage: Haben Sie gerade 5 Minuten Zeit? 😊`;
  }
  
  private getTimeBasedGreeting(): string {
    const hour = new Date().getHours();
    if (hour < 12) return 'Guten Morgen';
    if (hour < 17) return 'Guten Tag';
    return 'Guten Abend';
  }
  
  private isMobileDevice(): boolean {
    return /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(
      navigator.userAgent
    );
  }
  
  private async recordInteraction(
    contactId: string, 
    type: string, 
    outcome: string
  ): Promise<void> {
    await contactApi.logInteraction({
      contactId,
      interactionType: type.toUpperCase(),
      direction: 'outgoing',
      notes: `Mobile Action: ${type}`,
      sentiment: outcome === 'successful' ? 'positive' : 'neutral'
    });
  }
  
  private trackAction(action: QuickAction, contact: CustomerContact): void {
    // Analytics Tracking
    if (typeof gtag !== 'undefined') {
      gtag('event', 'contact_action', {
        action_type: action.type,
        contact_id: contact.id,
        urgency: action.urgency,
        device: this.isMobileDevice() ? 'mobile' : 'desktop'
      });
    }
  }
  
  private getCurrentUserName(): string {
    // TODO: Get from Auth Context
    return 'Ihr Vertriebsteam';
  }
  
  private isBirthdayToday(birthday?: Date | string): boolean {
    if (!birthday) return false;
    const today = new Date();
    const bday = new Date(birthday);
    return today.getDate() === bday.getDate() && 
           today.getMonth() === bday.getMonth();
  }
  
  private showCallDialog(contact: CustomerContact, phoneNumber: string): void {
    // Desktop Call Dialog Implementation
    // TODO: Implement with MUI Dialog
    console.log(`Call Dialog for ${contact.firstName} ${contact.lastName}: ${phoneNumber}`);
  }
}

// Singleton Export
export const actionExecutionService = new ActionExecutionService();
```

## 📱 Offline Queue Service

**Datei:** `frontend/src/features/customers/services/actions/OfflineQueueService.ts`

```typescript
// CLAUDE: Offline-Queue für Actions wenn keine Verbindung
// Pfad: frontend/src/features/customers/services/actions/OfflineQueueService.ts

import { QuickAction } from '../../types/mobileActions.types';
import { CustomerContact } from '../../types/contact.types';

interface QueuedAction {
  id: string;
  action: QuickAction;
  contact: CustomerContact;
  timestamp: Date;
  retryCount: number;
}

class OfflineQueueService {
  private readonly STORAGE_KEY = 'freshplan_offline_actions';
  private readonly MAX_RETRIES = 3;
  
  async queueAction(action: QuickAction, contact: CustomerContact): Promise<void> {
    const queue = this.getQueue();
    
    const queuedAction: QueuedAction = {
      id: `${Date.now()}-${Math.random()}`,
      action,
      contact,
      timestamp: new Date(),
      retryCount: 0
    };
    
    queue.push(queuedAction);
    this.saveQueue(queue);
    
    // Register for background sync if available
    if ('serviceWorker' in navigator && 'sync' in self.registration) {
      await self.registration.sync.register('sync-contact-actions');
    }
  }
  
  async processQueue(): Promise<void> {
    if (!navigator.onLine) return;
    
    const queue = this.getQueue();
    const pending = [...queue];
    
    for (const item of pending) {
      try {
        // Attempt to execute action
        await this.executeQueuedAction(item);
        
        // Remove from queue on success
        this.removeFromQueue(item.id);
        
      } catch (error) {
        item.retryCount++;
        
        if (item.retryCount >= this.MAX_RETRIES) {
          // Max retries reached, remove and notify
          this.removeFromQueue(item.id);
          this.notifyFailedAction(item);
        } else {
          // Update retry count
          this.updateQueueItem(item);
        }
      }
    }
  }
  
  private getQueue(): QueuedAction[] {
    const stored = localStorage.getItem(this.STORAGE_KEY);
    return stored ? JSON.parse(stored) : [];
  }
  
  private saveQueue(queue: QueuedAction[]): void {
    localStorage.setItem(this.STORAGE_KEY, JSON.stringify(queue));
  }
  
  private removeFromQueue(id: string): void {
    const queue = this.getQueue();
    const filtered = queue.filter(item => item.id !== id);
    this.saveQueue(filtered);
  }
  
  private updateQueueItem(item: QueuedAction): void {
    const queue = this.getQueue();
    const index = queue.findIndex(q => q.id === item.id);
    if (index !== -1) {
      queue[index] = item;
      this.saveQueue(queue);
    }
  }
  
  private async executeQueuedAction(item: QueuedAction): Promise<void> {
    // Re-execute the action
    const { actionExecutionService } = await import('./ActionExecutionService');
    await actionExecutionService.executeAction(item.action, item.contact);
  }
  
  private notifyFailedAction(item: QueuedAction): void {
    // Notify user about failed action
    if ('Notification' in window && Notification.permission === 'granted') {
      new Notification('Aktion fehlgeschlagen', {
        body: `${item.action.label} für ${item.contact.firstName} konnte nicht ausgeführt werden`,
        icon: '/icon-192.png'
      });
    }
  }
  
  // Auto-process queue when coming online
  constructor() {
    window.addEventListener('online', () => {
      this.processQueue();
    });
  }
}

export const offlineQueueService = new OfflineQueueService();
```

## 📋 IMPLEMENTIERUNGS-CHECKLISTE FÜR CLAUDE

### Phase 1: Setup (15 Min)
- [ ] react-swipeable installieren (`npm install react-swipeable`)
- [ ] Mobile Types erstellen (mobileActions.types.ts)
- [ ] Ordnerstruktur anlegen

### Phase 2: Services (30 Min)
- [ ] ActionSuggestionService implementieren (200 Zeilen)
- [ ] ActionExecutionService implementieren (250 Zeilen)
- [ ] OfflineQueueService implementieren (150 Zeilen)

### Phase 3: Components (30 Min)
- [ ] SwipeableContactCard implementieren (300 Zeilen)
- [ ] MobileActionBar implementieren (150 Zeilen)
- [ ] MobileContactFAB implementieren (100 Zeilen)

### Phase 4: Integration (15 Min)
- [ ] useUnifiedActions Hook erstellen
- [ ] In CustomersPage einbinden
- [ ] Service Worker Setup (optional)

### Phase 5: Testing (20 Min)
- [ ] Swipe-Tests schreiben
- [ ] Action-Execution Tests
- [ ] Offline-Queue Tests

## 🔗 INTEGRATION POINTS

### Wo wird Mobile Actions verwendet?

1. **CustomersPage.tsx** (Mobile View)
   ```typescript
   // Pfad: frontend/src/pages/CustomersPage.tsx
   import { SwipeableContactCard } from '@/features/customers/components/mobile/SwipeableContactCard';
   ```

2. **ContactDetailModal.tsx** (Mobile Actions)
   ```typescript
   // Pfad: frontend/src/features/customers/components/ContactDetailModal.tsx
   import { MobileActionBar } from '@/features/customers/components/mobile/MobileActionBar';
   ```

3. **App.tsx** (FAB Integration)
   ```typescript
   // Pfad: frontend/src/App.tsx
   import { MobileContactFAB } from '@/features/customers/components/mobile/MobileContactFAB';
   ```

## ⚠️ HÄUFIGE FEHLER VERMEIDEN

1. **Swipe funktioniert nicht**
   → Lösung: `touch-action: pan-y` auf Container setzen

2. **Actions werden nicht ausgeführt**
   → Lösung: ActionExecutionService korrekt importieren

3. **Offline-Queue verliert Daten**
   → Lösung: LocalStorage korrekt serialisieren

4. **Performance bei vielen Cards**
   → Lösung: Virtuelles Scrolling implementieren

## 🚀 NÄCHSTE SCHRITTE NACH IMPLEMENTIERUNG

1. **Relationship Intelligence** vervollständigen
   → [Dokument öffnen](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/RELATIONSHIP_INTELLIGENCE.md)

2. **Contact Timeline** implementieren
   → [Dokument öffnen](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/CONTACT_TIMELINE.md)

3. **Testing Integration** durchführen
   → [Dokument öffnen](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/TESTING_INTEGRATION.md)

---

**Status:** BEREIT FÜR IMPLEMENTIERUNG  
**Geschätzte Zeit:** 110 Minuten  
**Nächstes Dokument:** [→ Relationship Intelligence](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/RELATIONSHIP_INTELLIGENCE.md)  
**Parent:** [↑ Step3 Übersicht](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/README.md)

**Mobile Actions = Vertriebspower überall! 📱💼🚀**