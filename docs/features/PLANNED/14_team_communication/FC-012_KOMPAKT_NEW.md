# FC-012 Team Communication - KOMPAKT ⚡

**Feature-Typ:** 🔀 FULLSTACK  
**Priorität:** MEDIUM  
**Aufwand:** 4 Tage  
**Status:** 📋 Geplant (Team Collaboration)  

---

## 🎯 ÜBERBLICK

### Business Context
Kunden-zentriertes Team-Kommunikationssystem mit Real-time Chat, Aktivitäts-Stream und Smart Notifications. Eliminiert Info-Silos zwischen Verkäufern für koordinierte Kundenbetreuung.

### Technical Vision
Event-driven Architecture mit WebSocket-basierter Echtzeitübertragung. Customer-Context Chat, Activity Timeline und Smart Notifications für bessere Team-Koordination.

---

## 🏗️ CORE ARCHITEKTUR

### Real-time Communication Flow
```
Customer Event → Event Bus → Notification Engine → WebSocket → Team Updates
      ↓              ↓              ↓               ↓           ↓
Opportunity    Activity Stream   Smart Rules    Real-time    UI Updates
Call/Email     Database Log      User Prefs     Push         Notifications
Status Change  Timeline Update   Priority Logic Chat Message  Team Awareness
```

### Backend Core Components
```java
// 1. Customer Event Publisher - Event-driven Core
@ApplicationScoped
public class CustomerEventPublisher {
    @Inject Event<CustomerEvent> eventBus;
    @Inject NotificationEngine notificationEngine;
    
    public void publishCustomerEvent(UUID customerId, CustomerEventType type, Object data) {
        CustomerEvent event = CustomerEvent.builder()
            .customerId(customerId)
            .eventType(type)
            .data(data)
            .timestamp(LocalDateTime.now())
            .build();
            
        eventBus.fire(event);
        notificationEngine.processEvent(event);
    }
}

// 2. WebSocket Handler - Real-time Communication
@ServerEndpoint("/ws/team/{userId}")
@ApplicationScoped
public class TeamCommunicationWebSocket {
    private static final Map<String, Session> userSessions = new ConcurrentHashMap<>();
    
    @OnMessage
    public void onMessage(String message, Session session, @PathParam("userId") String userId) {
        ChatMessage chatMessage = parseMessage(message);
        
        // Save to database
        chatMessageService.save(chatMessage);
        
        // Broadcast to team members
        broadcastToTeamMembers(chatMessage, userId);
    }
    
    private void broadcastToTeamMembers(ChatMessage message, String senderId) {
        List<String> teamMemberIds = getTeamMembersForCustomer(message.getCustomerId());
        
        teamMemberIds.stream()
            .filter(id -> !id.equals(senderId))
            .forEach(id -> sendToUser(id, message));
    }
}

// 3. Notification Engine - Smart Rules
@ApplicationScoped
public class NotificationEngine {
    
    public void processEvent(CustomerEvent event) {
        List<NotificationRule> rules = getRulesForEventType(event.getEventType());
        
        for (NotificationRule rule : rules) {
            if (rule.matches(event)) {
                createNotification(event, rule);
            }
        }
    }
    
    private void createNotification(CustomerEvent event, NotificationRule rule) {
        Notification notification = Notification.builder()
            .customerId(event.getCustomerId())
            .message(rule.generateMessage(event))
            .priority(rule.getPriority())
            .targetUsers(rule.getTargetUsers(event))
            .build();
            
        notificationService.send(notification);
    }
}
```

### Frontend Integration
```typescript
// 1. WebSocket Manager - Real-time Connection
export class TeamWebSocketManager {
    private ws: WebSocket | null = null;
    private reconnectAttempts = 0;
    private maxReconnectAttempts = 5;
    
    connect(userId: string): void {
        const wsUrl = `ws://localhost:8080/ws/team/${userId}`;
        this.ws = new WebSocket(wsUrl);
        
        this.ws.onmessage = (event) => {
            const message = JSON.parse(event.data);
            this.handleMessage(message);
        };
        
        this.ws.onclose = () => {
            this.handleReconnect();
        };
    }
    
    sendMessage(customerId: string, content: string): void {
        if (this.ws?.readyState === WebSocket.OPEN) {
            this.ws.send(JSON.stringify({
                type: 'chat_message',
                customerId,
                content,
                timestamp: new Date().toISOString()
            }));
        }
    }
    
    private handleReconnect(): void {
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            setTimeout(() => {
                this.reconnectAttempts++;
                this.connect(this.userId);
            }, Math.pow(2, this.reconnectAttempts) * 1000);
        }
    }
}

// 2. Team Chat Component
export const TeamChatPanel: React.FC<{ customerId: string }> = ({ customerId }) => {
    const [messages, setMessages] = useState<ChatMessage[]>([]);
    const [newMessage, setNewMessage] = useState('');
    const { user } = useAuth();
    const wsManager = useRef(new TeamWebSocketManager()).current;
    
    useEffect(() => {
        wsManager.connect(user.id);
        wsManager.onMessage = (message) => {
            if (message.customerId === customerId) {
                setMessages(prev => [...prev, message]);
            }
        };
        
        // Load chat history
        loadChatHistory(customerId).then(setMessages);
        
        return () => wsManager.disconnect();
    }, [customerId]);
    
    const sendMessage = () => {
        if (newMessage.trim()) {
            wsManager.sendMessage(customerId, newMessage);
            setNewMessage('');
        }
    };
    
    return (
        <Card>
            <CardHeader title="Team Chat" />
            <CardContent sx={{ height: 400, overflow: 'auto' }}>
                {messages.map(message => (
                    <ChatMessageItem key={message.id} message={message} />
                ))}
            </CardContent>
            <CardActions>
                <TextField
                    value={newMessage}
                    onChange={(e) => setNewMessage(e.target.value)}
                    placeholder="Nachricht an Team..."
                    fullWidth
                    onKeyPress={(e) => e.key === 'Enter' && sendMessage()}
                />
                <Button onClick={sendMessage}>Senden</Button>
            </CardActions>
        </Card>
    );
};
```

---

## 🔗 DEPENDENCIES

- **Benötigt:** FC-014 Activity Timeline (activity logging), FC-003 E-Mail Integration (email events)
- **Erweitert:** FC-008 Security (user context), M3 Sales Cockpit (team panel integration)
- **Integration:** WebSocket für Real-time, CDI Events für Backend communication

---

## 🧪 TESTING

### WebSocket Integration Tests
```java
@QuarkusTest
class TeamWebSocketTest {
    @Test
    void sendMessage_shouldBroadcastToTeamMembers() {
        // Connect multiple users
        Session user1 = connectUser("user1");
        Session user2 = connectUser("user2");
        
        // Send message from user1
        String message = "{\"type\":\"chat_message\",\"customerId\":\"123\",\"content\":\"Test\"}";
        user1.getBasicRemote().sendText(message);
        
        // Verify user2 receives message
        verify(user2).getBasicRemote().sendText(contains("Test"));
    }
}
```

### Event-driven Tests
```java
@Test
void customerEvent_shouldTriggerNotifications() {
    UUID customerId = UUID.randomUUID();
    
    eventPublisher.publishCustomerEvent(customerId, OPPORTUNITY_CREATED, opportunityData);
    
    verify(notificationEngine).processEvent(any(CustomerEvent.class));
}
```

---

## 📋 QUICK IMPLEMENTATION

### 🕒 15-Min Claude Working Section

**Aufgabe:** Team Communication mit WebSocket und Real-time Chat implementieren

**Sofort loslegen:**
1. WebSocket Endpoint für Team Communication
2. Customer Event Publisher für Activity Stream
3. Team Chat Component mit Real-time Updates
4. Notification Engine mit Smart Rules

**Quick-Win Code:**
```java
// 1. Simple WebSocket Chat
@ServerEndpoint("/ws/team/{userId}")
public class SimpleTeamChat {
    private static Map<String, Session> sessions = new ConcurrentHashMap<>();
    
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        sessions.put(userId, session);
    }
    
    @OnMessage
    public void onMessage(String message, @PathParam("userId") String userId) {
        // Broadcast to all connected users
        sessions.values().forEach(session -> {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                // Handle error
            }
        });
    }
}

// 2. Event Publishing
@ApplicationScoped
public class CustomerEventService {
    public void logActivity(UUID customerId, String activity, String userId) {
        CustomerEvent event = new CustomerEvent(customerId, activity, userId);
        
        // Save to database
        activityRepository.persist(event);
        
        // Notify team via WebSocket
        webSocketHandler.broadcast(event);
    }
}
```

**Nächste Schritte:**
- Phase 1: WebSocket Setup und Basic Chat (1h)
- Phase 2: Event Publishing für Activities (1h)
- Phase 3: Smart Notifications Engine (1h)
- Phase 4: Frontend Integration und UI (1h)

**Erfolgs-Kriterien:**
- ✅ Real-time Chat zwischen Team Members
- ✅ Activity Timeline mit Live Updates
- ✅ Smart Notifications basierend auf Events
- ✅ Customer-Context Team Communication

---

**🔗 DETAIL-DOCS:**
- [TECH_CONCEPT](/docs/features/PLANNED/14_team_communication/FC-012_TECH_CONCEPT.md) - Vollständige technische Spezifikation
- [IMPLEMENTATION_GUIDE](/docs/features/PLANNED/14_team_communication/FC-012_IMPLEMENTATION_GUIDE.md) - Detaillierte Umsetzungsanleitung

**🎯 Nächster Schritt:** WebSocket Endpoint für Team Communication implementieren