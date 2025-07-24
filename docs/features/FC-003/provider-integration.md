# FC-003: Provider-Integration - Detailspezifikation

## OAuth2 Provider-Implementierungen

### 1. Gmail Integration

```java
@ApplicationScoped
public class GmailProvider implements EmailProvider {
    
    private static final String GMAIL_AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String[] GMAIL_SCOPES = {
        "https://www.googleapis.com/auth/gmail.readonly",
        "https://www.googleapis.com/auth/gmail.send",
        "https://www.googleapis.com/auth/gmail.modify"
    };
    
    @Override
    public AuthorizationUrl initiateOAuth(String redirectUri) {
        return AuthorizationUrl.builder()
            .authUrl(GMAIL_AUTH_URL)
            .clientId(gmailClientId)
            .redirectUri(redirectUri)
            .scopes(GMAIL_SCOPES)
            .responseType("code")
            .accessType("offline")
            .prompt("consent")
            .build();
    }
    
    @Override
    public EmailConnection connect(OAuthTokens tokens) {
        Gmail service = new Gmail.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            GsonFactory.getDefaultInstance(),
            createCredentials(tokens)
        ).setApplicationName("FreshPlan").build();
        
        return new GmailConnection(service);
    }
}
```

### 2. Exchange/Outlook Integration

```java
@ApplicationScoped
public class ExchangeProvider implements EmailProvider {
    
    private static final String OUTLOOK_AUTH_URL = 
        "https://login.microsoftonline.com/common/oauth2/v2.0/authorize";
    private static final String[] OUTLOOK_SCOPES = {
        "https://graph.microsoft.com/Mail.Read",
        "https://graph.microsoft.com/Mail.Send",
        "https://graph.microsoft.com/Mail.ReadWrite"
    };
    
    @Override
    public EmailConnection connect(OAuthTokens tokens) {
        GraphServiceClient graphClient = GraphServiceClient
            .builder()
            .authenticationProvider(createAuthProvider(tokens))
            .buildClient();
            
        return new ExchangeConnection(graphClient);
    }
}
```

### 3. IMAP/SMTP Generic Provider

```java
@ApplicationScoped
public class ImapSmtpProvider implements EmailProvider {
    
    @Override
    public EmailConnection connect(ImapSmtpCredentials credentials) {
        Properties props = new Properties();
        
        // IMAP Configuration
        props.put("mail.imap.host", credentials.getImapHost());
        props.put("mail.imap.port", credentials.getImapPort());
        props.put("mail.imap.ssl.enable", credentials.isImapSsl());
        
        // SMTP Configuration
        props.put("mail.smtp.host", credentials.getSmtpHost());
        props.put("mail.smtp.port", credentials.getSmtpPort());
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", credentials.isSmtpTls());
        
        Session session = Session.getInstance(props, 
            new PasswordAuthenticator(credentials));
            
        return new ImapSmtpConnection(session);
    }
}
```

## Provider Factory Pattern

```java
@ApplicationScoped
public class EmailProviderFactory {
    
    @Inject GmailProvider gmailProvider;
    @Inject ExchangeProvider exchangeProvider;
    @Inject ImapSmtpProvider imapSmtpProvider;
    
    public EmailProvider getProvider(EmailProviderType type) {
        return switch (type) {
            case GMAIL -> gmailProvider;
            case EXCHANGE -> exchangeProvider;
            case IMAP_SMTP -> imapSmtpProvider;
            default -> throw new UnsupportedProviderException(type);
        };
    }
}
```

## Token Management & Security

```java
@Entity
public class EmailAccountTokens {
    @Id UUID id;
    UUID accountId;
    
    @Convert(converter = EncryptedStringConverter.class)
    String accessToken;
    
    @Convert(converter = EncryptedStringConverter.class)
    String refreshToken;
    
    Instant expiresAt;
    
    public boolean needsRefresh() {
        return Instant.now().isAfter(expiresAt.minus(5, ChronoUnit.MINUTES));
    }
}

@ApplicationScoped
public class TokenRefreshService {
    
    @Scheduled(every = "5m")
    void refreshExpiredTokens() {
        List<EmailAccountTokens> expiring = tokenRepository
            .findExpiringTokens(Instant.now().plus(10, ChronoUnit.MINUTES));
            
        for (EmailAccountTokens tokens : expiring) {
            try {
                refreshToken(tokens);
            } catch (Exception e) {
                notifyTokenRefreshFailure(tokens.accountId, e);
            }
        }
    }
}
```

## Connection Pooling & Performance

```java
@ApplicationScoped
public class EmailConnectionPool {
    
    private final Map<UUID, EmailConnection> connectionCache = 
        new ConcurrentHashMap<>();
    
    private final LoadingCache<UUID, EmailConnection> connections = 
        CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .removalListener(this::closeConnection)
            .build(new CacheLoader<>() {
                @Override
                public EmailConnection load(UUID accountId) {
                    return createConnection(accountId);
                }
            });
    
    public EmailConnection getConnection(UUID accountId) {
        return connections.getUnchecked(accountId);
    }
}
```

## Error Handling & Retry Logic

```java
@ApplicationScoped
public class EmailProviderErrorHandler {
    
    @Retry(maxRetries = 3, delay = 1000, jitter = 500)
    @Fallback(fallbackMethod = "handleProviderError")
    public EmailMessage sendEmail(EmailConnection connection, 
                                  EmailMessage message) {
        return connection.send(message);
    }
    
    private EmailMessage handleProviderError(EmailConnection connection,
                                             EmailMessage message,
                                             Exception e) {
        if (e instanceof RateLimitException) {
            queueForLaterRetry(message);
        } else if (e instanceof AuthenticationException) {
            triggerReauthentication(connection.getAccountId());
        }
        
        throw new EmailSendException("Failed to send email", e);
    }
}
```

## Provider-Specific Features

### Gmail Labels Support
```java
public interface GmailSpecificFeatures {
    void applyLabel(String messageId, String label);
    void removeLabel(String messageId, String label);
    List<String> getLabels();
}
```

### Exchange Categories
```java
public interface ExchangeSpecificFeatures {
    void categorize(String messageId, String category);
    void flag(String messageId, FollowUpFlag flag);
}
```

## Migration Path für bestehende E-Mail-Daten

```sql
-- Migration für Import existierender E-Mails
ALTER TABLE email_messages ADD COLUMN legacy_id VARCHAR(255);
ALTER TABLE email_messages ADD COLUMN import_source VARCHAR(50);

-- Index für Performance
CREATE INDEX idx_email_customer_date ON email_messages(customer_id, sent_at DESC);
CREATE INDEX idx_email_opportunity ON email_messages(opportunity_id);
CREATE INDEX idx_email_thread ON email_messages(thread_id);
```