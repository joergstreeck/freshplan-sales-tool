# FreshPlan Dashboard & Rechteverwaltung - Architektur

## 📋 Übersicht

Dieses Dokument beschreibt die geplante Architektur für das integrierte Dashboard-System mit Rechteverwaltung im FreshPlan Sales Tool.

## 🎯 Zielsetzung

- **Einheitliches System** für Vertrieb und Geschäftsleitung
- **Rollenbasierte Ansichten** mit unterschiedlichen Berechtigungen
- **Integrierter Freigabe-Workflow** für Bonitätsprüfungen
- **Echtzeit-Kommunikation** zwischen Vertrieb und Geschäftsleitung
- **E-Mail-Benachrichtigungen** bei wichtigen Ereignissen

## 👥 Benutzerrollen

### 1. Verkäufer/Vertrieb
- Zugriff auf eigene Kunden
- Kann Bonitätsprüfungen anfragen
- Sieht nur eigene Statistiken
- Kann mit Geschäftsleitung kommunizieren

### 2. Geschäftsleitung
- Vollzugriff auf alle Daten
- Kann zwischen Verkäufer-Ansichten wechseln
- Freigabe/Ablehnung von Anfragen
- Gesamt-Statistiken und Reports
- Direkte Kommunikation mit allen Verkäufern

### 3. Administrator
- Benutzerverwaltung
- Systemkonfiguration
- Rechteverwaltung

## 🏗️ Technische Architektur

### Frontend-Erweiterungen

```javascript
// Neue Module struktur
src/
├── modules/
│   ├── auth/
│   │   ├── LoginModule.ts
│   │   ├── AuthService.ts
│   │   └── SessionManager.ts
│   ├── dashboard/
│   │   ├── DashboardModule.ts
│   │   ├── SalesOverview.ts
│   │   ├── ApprovalQueue.ts
│   │   └── Statistics.ts
│   └── communication/
│       ├── MessageModule.ts
│       ├── NotificationService.ts
│       └── EmailService.ts
```

### Backend-Anforderungen

```javascript
// API Endpoints
POST   /api/auth/login
POST   /api/auth/logout
GET    /api/auth/session

GET    /api/dashboard/overview
GET    /api/dashboard/sales/:userId?
GET    /api/dashboard/approvals

GET    /api/customers
POST   /api/customers
PUT    /api/customers/:id

POST   /api/credit-check/request
PUT    /api/credit-check/:id/approve
PUT    /api/credit-check/:id/reject

POST   /api/messages
GET    /api/messages/:threadId
```

### Datenbank-Schema

```sql
-- Benutzer & Rollen
CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('sales', 'management', 'admin'),
    name VARCHAR(255),
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Kunden (erweitert)
CREATE TABLE customers (
    id UUID PRIMARY KEY,
    company_name VARCHAR(255) NOT NULL,
    sales_user_id UUID REFERENCES users(id),
    status ENUM('active', 'inactive', 'pending'),
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    -- weitere Felder wie bisher
);

-- Bonitätsprüfungen
CREATE TABLE credit_checks (
    id UUID PRIMARY KEY,
    customer_id UUID REFERENCES customers(id),
    requested_by UUID REFERENCES users(id),
    status ENUM('pending', 'approved', 'rejected', 'in_review'),
    credit_limit DECIMAL(10,2),
    approved_by UUID REFERENCES users(id),
    approved_at TIMESTAMP,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Nachrichten/Kommunikation
CREATE TABLE messages (
    id UUID PRIMARY KEY,
    thread_id UUID NOT NULL,
    sender_id UUID REFERENCES users(id),
    recipient_id UUID REFERENCES users(id),
    subject VARCHAR(255),
    message TEXT,
    read BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Aktivitäts-Log
CREATE TABLE activity_log (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES users(id),
    action VARCHAR(100),
    entity_type VARCHAR(50),
    entity_id UUID,
    details JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 📊 Dashboard-Komponenten

### Verkäufer-Dashboard

```typescript
interface SalesDashboard {
    // Übersicht
    myCustomers: Customer[];
    pendingApprovals: CreditCheck[];
    recentActivities: Activity[];
    
    // Statistiken
    monthlyStats: {
        newCustomers: number;
        totalVolume: number;
        approvalRate: number;
    };
    
    // Aufgaben
    openTasks: Task[];
    messages: Message[];
}
```

### Geschäftsleitung-Dashboard

```typescript
interface ManagementDashboard {
    // Team-Übersicht
    salesTeam: {
        userId: string;
        name: string;
        performance: PerformanceMetrics;
    }[];
    
    // Freigaben
    approvalQueue: {
        pending: CreditCheck[];
        recent: CreditCheck[];
    };
    
    // Statistiken
    companyStats: {
        totalVolume: number;
        customerCount: number;
        avgDealSize: number;
        topPerformers: User[];
    };
    
    // Kommunikation
    announcements: Announcement[];
    directMessages: MessageThread[];
}
```

## 🔄 Freigabe-Workflow

### 1. Anfrage stellen (Verkäufer)
```typescript
async function requestCreditApproval(customerId: string, amount: number) {
    const request = await api.post('/credit-check/request', {
        customerId,
        requestedLimit: amount,
        urgency: 'normal',
        notes: 'Neukunde, Erstbestellung'
    });
    
    // Automatische E-Mail an Geschäftsleitung
    await notificationService.send({
        type: 'APPROVAL_REQUEST',
        to: 'management',
        data: request
    });
}
```

### 2. Freigabe-Queue (Geschäftsleitung)
```typescript
interface ApprovalQueue {
    requests: CreditCheckRequest[];
    filters: {
        status: 'all' | 'pending' | 'urgent';
        salesperson?: string;
        dateRange?: DateRange;
    };
    
    actions: {
        approve: (id: string, limit?: number) => Promise<void>;
        reject: (id: string, reason: string) => Promise<void>;
        requestInfo: (id: string, message: string) => Promise<void>;
    };
}
```

### 3. Benachrichtigungen
```typescript
interface NotificationConfig {
    email: {
        newApprovalRequest: boolean;
        approvalDecision: boolean;
        messageReceived: boolean;
    };
    
    inApp: {
        all: boolean;
    };
    
    triggers: {
        urgentRequests: 'immediate';
        normalRequests: 'digest'; // Täglich
    };
}
```

## 🔐 Sicherheit & Datenschutz

### Authentifizierung
- JWT-basierte Sessions
- 2FA für Geschäftsleitung (optional)
- Session-Timeout nach Inaktivität

### Berechtigungen
```typescript
const permissions = {
    sales: {
        customers: ['read:own', 'create', 'update:own'],
        creditChecks: ['create', 'read:own'],
        dashboard: ['read:own'],
        messages: ['create', 'read:own']
    },
    
    management: {
        customers: ['read:all', 'update:all'],
        creditChecks: ['read:all', 'approve', 'reject'],
        dashboard: ['read:all'],
        messages: ['create', 'read:all'],
        users: ['read:all']
    },
    
    admin: ['*:all']
};
```

## 📱 Responsive Design

- **Desktop**: Vollständiges Dashboard mit allen Features
- **Tablet**: Optimierte Ansicht, wichtigste Funktionen
- **Mobile**: Fokus auf Freigaben und Nachrichten

## 🚀 Implementierungs-Roadmap

### Phase 1: Basis-Authentifizierung
- [ ] Login/Logout
- [ ] Session-Management
- [ ] Basis-Rechteverwaltung

### Phase 2: Dashboard-Grundlagen
- [ ] Verkäufer-Dashboard
- [ ] Geschäftsleitung-Übersicht
- [ ] Basis-Statistiken

### Phase 3: Freigabe-Workflow
- [ ] Anfrage-System
- [ ] Freigabe-Queue
- [ ] E-Mail-Benachrichtigungen

### Phase 4: Kommunikation
- [ ] Internes Messaging
- [ ] Aktivitäts-Feed
- [ ] Erweiterte Notifications

### Phase 5: Integration
- [ ] Warenkreditversicherer-API
- [ ] Export-Funktionen
- [ ] Erweiterte Reports

## 📝 Entwickler-Notizen

### Aktuelle Bonitätsprüfung-Implementation
**WICHTIG**: Die aktuelle Bonitätsprüfung ist nur eine Demo-Implementation!

- Simulierte Logik in `freshplan-complete.html` (Zeilen 1150-1170)
- KEINE echte Versicherer-Anbindung
- Muss vor Produktivbetrieb durch echte API ersetzt werden

### Integration mit bestehendem Code
- Nutzt vorhandene Module-Struktur
- Erweitert bestehende Store-Pattern
- Kompatibel mit aktuellem Build-System

### Migrations-Strategie
1. Bestehende localStorage-Daten in DB migrieren
2. Schrittweise Features aktivieren
3. Parallelbetrieb während Übergangsphase

## 🔗 Externe Abhängigkeiten

- **E-Mail-Service**: SendGrid/AWS SES empfohlen
- **Echtzeit-Updates**: Socket.io oder WebSockets
- **Datenbank**: PostgreSQL empfohlen
- **Hosting**: Cloud-Lösung für Skalierbarkeit

---

*Letzte Aktualisierung: 30.05.2025*