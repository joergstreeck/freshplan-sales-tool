# FC-012 Team Communication & Notifications - CLAUDE TECH 🤖

## 🧠 QUICK-LOAD (30 Sekunden bis zur Produktivität)

**Feature:** Real-time Team Chat + Activity Timeline + Smart Notifications mit WebSocket  
**Stack:** Quarkus WebSocket + React Hooks + Zustand + MUI Components  
**Status:** 📋 Geplant - Team Collaboration Core  
**Dependencies:** FC-008 Security, FC-009 Permissions | Erweitert: FC-024 Email Integration, FC-020 Voice Integration  

**Jump to:** [📚 Recipes](#-implementation-recipes) | [🧪 Tests](#-test-patterns) | [🔌 Integration](#-integration-cookbook) | [⚡ WebSocket Patterns](#-websocket-patterns)

**Core Purpose in 1 Line:** `Customer Event → WebSocket Broadcast → Real-time UI Update → Team Awareness`

---

## 🍳 IMPLEMENTATION RECIPES

### Recipe 1: WebSocket Backend in 5 Minuten
```java
// 1. WebSocket Endpoint (copy-paste ready)
@ServerEndpoint("/ws/team/{userId}")
@ApplicationScoped
public class TeamCommunicationWebSocket {
    private static final Map<String, Session> userSessions = new ConcurrentHashMap<>();
    
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        userSessions.put(userId, session);
        broadcastUserStatus(userId, "online");
        log.info("User {} connected", userId);
    }
    
    @OnMessage
    public void onMessage(String message, Session session, @PathParam("userId") String userId) {
        try {
            ChatMessage chatMessage = parseMessage(message);
            chatMessage.setUserId(UUID.fromString(userId));
            
            // Persist message
            chatMessageService.save(chatMessage);
            
            // Broadcast to team members
            broadcastToTeamMembers(chatMessage, userId);
            
            // Handle @mentions
            notifyMentionedUsers(chatMessage);
        } catch (Exception e) {
            sendError(session, "Message processing failed: " + e.getMessage());
        }
    }
    
    @OnClose
    public void onClose(Session session, @PathParam("userId") String userId) {
        userSessions.remove(userId);
        broadcastUserStatus(userId, "offline");
    }
    
    private void broadcastToTeamMembers(ChatMessage message, String senderId) {
        List<String> teamMemberIds = teamService.getTeamMemberIds(message.getCustomerId());
        
        WebSocketMessage wsMessage = WebSocketMessage.builder()
            .type("chat_message")
            .payload(message)
            .timestamp(Instant.now())
            .build();
        
        String jsonMessage = JsonbBuilder.create().toJson(wsMessage);
        
        teamMemberIds.stream()
            .filter(id -> !id.equals(senderId))
            .map(userSessions::get)
            .filter(Objects::nonNull)
            .forEach(session -> sendAsync(session, jsonMessage));
    }
}
```

### Recipe 2: Frontend Real-time Hook
```typescript
// 2. WebSocket Connection Hook (copy-paste ready)
export const useTeamWebSocket = (customerId: string) => {
    const [isConnected, setIsConnected] = useState(false);
    const [messages, setMessages] = useState<ChatMessage[]>([]);
    const [activities, setActivities] = useState<Activity[]>([]);
    const [onlineUsers, setOnlineUsers] = useState<string[]>([]);
    
    const wsRef = useRef<WebSocket | null>(null);
    const reconnectTimeoutRef = useRef<NodeJS.Timeout>();
    
    const connect = useCallback(() => {
        const userId = getCurrentUserId();
        const ws = new WebSocket(`${WS_URL}/team/${userId}`);
        wsRef.current = ws;
        
        ws.onopen = () => {
            setIsConnected(true);
            console.log('WebSocket connected');
            
            // Subscribe to customer updates
            ws.send(JSON.stringify({
                type: 'subscribe',
                customerId: customerId
            }));
        };
        
        ws.onmessage = (event) => {
            const data = JSON.parse(event.data) as WebSocketMessage;
            
            switch (data.type) {
                case 'chat_message':
                    setMessages(prev => [...prev, data.payload as ChatMessage]);
                    break;
                    
                case 'activity':
                    setActivities(prev => [data.payload as Activity, ...prev]);
                    break;
                    
                case 'user_status':
                    const { userId, status } = data.payload;
                    setOnlineUsers(prev => 
                        status === 'online' 
                            ? [...prev, userId]
                            : prev.filter(id => id !== userId)
                    );
                    break;
                    
                case 'notification':
                    showNotification(data.payload as Notification);
                    break;
            }
        };
        
        ws.onerror = (error) => {
            console.error('WebSocket error:', error);
        };
        
        ws.onclose = () => {
            setIsConnected(false);
            
            // Auto-reconnect after 3 seconds
            reconnectTimeoutRef.current = setTimeout(() => {
                console.log('Attempting to reconnect...');
                connect();
            }, 3000);
        };
    }, [customerId]);
    
    useEffect(() => {
        connect();
        
        return () => {
            clearTimeout(reconnectTimeoutRef.current);
            wsRef.current?.close();
        };
    }, [connect]);
    
    const sendMessage = useCallback((message: string, mentions: string[] = []) => {
        if (wsRef.current?.readyState === WebSocket.OPEN) {
            wsRef.current.send(JSON.stringify({
                type: 'chat_message',
                customerId,
                message,
                mentions
            }));
        }
    }, [customerId]);
    
    return {
        isConnected,
        messages,
        activities,
        onlineUsers,
        sendMessage
    };
};
```

### Recipe 3: Team Chat Component
```typescript
// 3. Team Chat UI Component (copy-paste ready)
export const TeamChat: React.FC<{ customerId: string }> = ({ customerId }) => {
    const { messages, onlineUsers, sendMessage, isConnected } = useTeamWebSocket(customerId);
    const [inputValue, setInputValue] = useState('');
    const [showMentions, setShowMentions] = useState(false);
    const messagesEndRef = useRef<HTMLDivElement>(null);
    
    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    };
    
    useEffect(scrollToBottom, [messages]);
    
    const handleSend = () => {
        if (inputValue.trim()) {
            const mentions = extractMentions(inputValue);
            sendMessage(inputValue, mentions);
            setInputValue('');
        }
    };
    
    return (
        <Card className="h-full flex flex-col">
            <CardHeader className="border-b">
                <div className="flex items-center justify-between">
                    <CardTitle className="text-lg">Team Chat</CardTitle>
                    <div className="flex items-center gap-2">
                        <div className={cn(
                            "w-2 h-2 rounded-full",
                            isConnected ? "bg-green-500" : "bg-red-500"
                        )} />
                        <span className="text-sm text-muted-foreground">
                            {onlineUsers.length} online
                        </span>
                    </div>
                </div>
            </CardHeader>
            
            <ScrollArea className="flex-1 p-4">
                <div className="space-y-4">
                    {messages.map((msg) => (
                        <ChatMessage key={msg.id} message={msg} />
                    ))}
                    <div ref={messagesEndRef} />
                </div>
            </ScrollArea>
            
            <div className="p-4 border-t">
                <div className="flex gap-2">
                    <Textarea
                        value={inputValue}
                        onChange={(e) => setInputValue(e.target.value)}
                        onKeyPress={(e) => {
                            if (e.key === 'Enter' && !e.shiftKey) {
                                e.preventDefault();
                                handleSend();
                            }
                        }}
                        placeholder="Nachricht schreiben... @ für Erwähnung"
                        className="resize-none"
                        rows={2}
                    />
                    <Button 
                        onClick={handleSend}
                        disabled={!inputValue.trim() || !isConnected}
                    >
                        <Send className="h-4 w-4" />
                    </Button>
                </div>
            </div>
        </Card>
    );
};
```

---

## 🧪 TEST PATTERNS

### Pattern 1: WebSocket Connection Test
```java
// Backend WebSocket Test
@QuarkusTest
class TeamCommunicationWebSocketTest {
    @Test
    void testWebSocketConnection() throws Exception {
        // Create WebSocket client
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        Session session = container.connectToServer(
            TestWebSocketClient.class,
            URI.create("ws://localhost:8080/ws/team/test-user-id")
        );
        
        // Test connection
        assertTrue(session.isOpen());
        
        // Send test message
        ChatMessage testMessage = ChatMessage.builder()
            .customerId(UUID.randomUUID())
            .message("Test message")
            .build();
        
        session.getBasicRemote().sendText(JsonbBuilder.create().toJson(testMessage));
        
        // Wait for response
        TestWebSocketClient client = (TestWebSocketClient) session.getUserProperties().get("client");
        await().atMost(5, SECONDS).until(() -> !client.getReceivedMessages().isEmpty());
        
        // Verify broadcast
        assertEquals(1, client.getReceivedMessages().size());
    }
}
```

### Pattern 2: Real-time Updates Test
```typescript
// Frontend Hook Test
describe('useTeamWebSocket', () => {
    it('should handle incoming messages', async () => {
        const { result } = renderHook(() => useTeamWebSocket('customer-123'));
        
        // Wait for connection
        await waitFor(() => {
            expect(result.current.isConnected).toBe(true);
        });
        
        // Simulate incoming message
        const mockMessage = {
            type: 'chat_message',
            payload: {
                id: '123',
                message: 'Test message',
                userId: 'user-456',
                timestamp: new Date().toISOString()
            }
        };
        
        act(() => {
            // Trigger WebSocket message
            mockWebSocket.triggerMessage(mockMessage);
        });
        
        expect(result.current.messages).toHaveLength(1);
        expect(result.current.messages[0].message).toBe('Test message');
    });
});
```

---

## 🔌 INTEGRATION COOKBOOK

### Mit Customer Detail Page
```typescript
// Integration in Customer View
export const CustomerDetailWithTeam: React.FC<{ customerId: string }> = ({ customerId }) => {
    const [activeTab, setActiveTab] = useState<'info' | 'communication'>('info');
    
    return (
        <div className="grid grid-cols-3 gap-6 h-full">
            <div className="col-span-2">
                <CustomerInfoCard customerId={customerId} />
                <OpportunityPipeline customerId={customerId} />
            </div>
            
            <div className="h-full">
                <Tabs value={activeTab} onValueChange={setActiveTab}>
                    <TabsList className="grid w-full grid-cols-2">
                        <TabsTrigger value="info">Info</TabsTrigger>
                        <TabsTrigger value="communication">Team</TabsTrigger>
                    </TabsList>
                    
                    <TabsContent value="info" className="h-[calc(100%-40px)]">
                        <CustomerQuickInfo customerId={customerId} />
                    </TabsContent>
                    
                    <TabsContent value="communication" className="h-[calc(100%-40px)]">
                        <TeamChat customerId={customerId} />
                    </TabsContent>
                </Tabs>
            </div>
        </div>
    );
};
```

### Mit Activity Events
```java
// Auto-post activities to timeline
@ApplicationScoped
public class ActivityEventHandler {
    @Inject TeamCommunicationWebSocket webSocket;
    @Inject ActivityService activityService;
    
    void onOpportunityUpdate(@Observes OpportunityUpdatedEvent event) {
        // Create activity
        Activity activity = Activity.builder()
            .customerId(event.getCustomerId())
            .userId(event.getUserId())
            .type(ActivityType.OPPORTUNITY)
            .summary("Opportunity aktualisiert: " + event.getOpportunityName())
            .details(Map.of(
                "stage", event.getNewStage(),
                "value", event.getValue()
            ))
            .build();
        
        // Save to database
        activityService.save(activity);
        
        // Broadcast via WebSocket
        webSocket.broadcastActivity(activity);
    }
}
```

---

## ⚡ WEBSOCKET PATTERNS

### Connection Management
```typescript
// Auto-reconnect with exponential backoff
const useReconnectingWebSocket = (url: string) => {
    const [retryCount, setRetryCount] = useState(0);
    const maxRetries = 5;
    
    const getRetryDelay = (attempt: number) => {
        return Math.min(1000 * Math.pow(2, attempt), 30000);
    };
    
    const connect = useCallback(() => {
        if (retryCount >= maxRetries) {
            console.error('Max reconnection attempts reached');
            return;
        }
        
        const ws = new WebSocket(url);
        
        ws.onclose = () => {
            const delay = getRetryDelay(retryCount);
            console.log(`Reconnecting in ${delay}ms...`);
            
            setTimeout(() => {
                setRetryCount(prev => prev + 1);
                connect();
            }, delay);
        };
        
        ws.onopen = () => {
            console.log('WebSocket connected');
            setRetryCount(0);
        };
        
        return ws;
    }, [url, retryCount]);
    
    return connect;
};
```

### Message Batching
```java
// Batch messages for performance
@ApplicationScoped
public class MessageBatcher {
    private final List<WebSocketMessage> messageQueue = new ArrayList<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    @PostConstruct
    void init() {
        // Send batched messages every 100ms
        scheduler.scheduleAtFixedRate(this::flushMessages, 100, 100, TimeUnit.MILLISECONDS);
    }
    
    public void queueMessage(WebSocketMessage message) {
        synchronized (messageQueue) {
            messageQueue.add(message);
        }
    }
    
    private void flushMessages() {
        List<WebSocketMessage> toSend;
        synchronized (messageQueue) {
            if (messageQueue.isEmpty()) return;
            toSend = new ArrayList<>(messageQueue);
            messageQueue.clear();
        }
        
        BatchedMessage batch = new BatchedMessage(toSend);
        broadcastBatch(batch);
    }
}
```

---

## 📚 DEEP KNOWLEDGE (Bei Bedarf expandieren)

<details>
<summary>🔒 Security Considerations</summary>

### WebSocket Authentication
```java
@ServerEndpoint(value = "/ws/team/{userId}", 
    configurator = JWTWebSocketConfigurator.class)
public class SecureWebSocket {
    
    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        // JWT is validated by configurator
        String userId = (String) config.getUserProperties().get("userId");
        String role = (String) config.getUserProperties().get("role");
        
        if (!hasAccess(userId, role)) {
            closeWithError(session, "Unauthorized");
            return;
        }
        
        // Continue with normal flow
    }
}

public class JWTWebSocketConfigurator extends ServerEndpointConfig.Configurator {
    @Override
    public void modifyHandshake(ServerEndpointConfig config, 
                               HandshakeRequest request, 
                               HandshakeResponse response) {
        String token = extractToken(request);
        if (token != null && validateJWT(token)) {
            config.getUserProperties().put("userId", getUserIdFromToken(token));
            config.getUserProperties().put("role", getRoleFromToken(token));
        }
    }
}
```

### Rate Limiting
```typescript
// Client-side rate limiting
const useRateLimitedWebSocket = () => {
    const messageQueue = useRef<QueuedMessage[]>([]);
    const lastSentTime = useRef<number>(0);
    const minInterval = 100; // ms between messages
    
    const sendMessage = (message: any) => {
        const now = Date.now();
        const timeSinceLastSent = now - lastSentTime.current;
        
        if (timeSinceLastSent >= minInterval) {
            ws.send(JSON.stringify(message));
            lastSentTime.current = now;
        } else {
            // Queue message
            messageQueue.current.push({
                message,
                timestamp: now + minInterval - timeSinceLastSent
            });
        }
    };
    
    return { sendMessage };
};
```

</details>

<details>
<summary>⚡ Performance Optimization</summary>

### Message Compression
```java
// Enable WebSocket compression
@ServerEndpoint(value = "/ws/team/{userId}",
    encoders = { MessageEncoder.class },
    decoders = { MessageDecoder.class },
    configurator = CompressionConfigurator.class)
```

### Connection Pooling
```typescript
// Share WebSocket connections
class WebSocketPool {
    private connections = new Map<string, WebSocket>();
    
    getConnection(key: string, url: string): WebSocket {
        if (!this.connections.has(key)) {
            const ws = new WebSocket(url);
            this.connections.set(key, ws);
        }
        return this.connections.get(key)!;
    }
}

export const wsPool = new WebSocketPool();
```

</details>

---

**🎯 Nächster Schritt:** WebSocket Endpoint implementieren und mit Frontend verbinden