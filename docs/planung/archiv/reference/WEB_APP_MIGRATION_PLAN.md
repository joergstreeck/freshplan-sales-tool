# FreshPlan Web App Migration Plan: TypeScript → React

## 🎯 Migrationsstrategie

**Prinzip:** Schrittweise Migration des bestehenden TypeScript-Codes zu React mit maximalem Code-Reuse bei laufendem Betrieb.

## 📦 Technology Stack (IT-Entscheidung vom 05.06.2025)

- **Frontend**: React 18 + TypeScript + Vite
- **Backend**: Quarkus (Java)
- **Database**: PostgreSQL (AWS RDS/Aurora)
- **Authentication**: Keycloak
- **Infrastructure**: AWS (ECS Fargate, S3, CloudFront)
- **CI/CD**: GitHub Actions

## 🏗️ Architektur-Transformation

```
AKTUELL (Standalone)              ZIEL (Enterprise Web App)
┌─────────────────┐               ┌─────────────────┐     ┌─────────────────┐
│  Browser App    │               │   React SPA     │────▶│  Quarkus API    │
│  TypeScript     │     ──→       │   (CloudFront)  │     │  (ECS Fargate)  │
│  LocalStorage   │               └─────────────────┘     └────────┬────────┘
└─────────────────┘                                                 │
                                                           ┌────────▼────────┐
                                                           │   PostgreSQL    │
                                                           │   (RDS/Aurora)  │
                                                           └─────────────────┘
                                                                    │
                                                           ┌────────▼────────┐
                                                           │    Keycloak     │
                                                           │     (Auth)      │
                                                           └─────────────────┘
```

## 📋 Migration Checkliste

### Phase 1: Vorbereitung & Setup (1-2 Wochen)
- [ ] React-Projekt mit Vite aufsetzen
- [ ] TypeScript-Konfiguration vom bestehenden Projekt übernehmen
- [ ] Bestehende CSS-Styles integrieren
- [ ] Build-Pipeline für Parallel-Betrieb konfigurieren
- [ ] Feature-Flag-System implementieren

### Phase 2: Core-Migration (2-3 Wochen)
- [ ] Repository-Pattern abstrakt machen (LocalStorage + REST API)
- [ ] Event-Bus als React Context migrieren
- [ ] State Management mit Redux Toolkit/Zustand
- [ ] Routing mit React Router
- [ ] i18n-System (bestehende Übersetzungen beibehalten)

### Phase 3: Module-für-Module Migration (4-6 Wochen)
- [ ] CustomerModule → CustomerComponents
- [ ] CalculatorModule → CalculatorComponents  
- [ ] PDFModule → PDFService
- [ ] SettingsModule → SettingsPage
- [ ] IntegrationModule → IntegrationService

## 🔄 Code-Migration-Strategie

### Was bleibt erhalten (100% Re-Use):
- ✅ Kalkulationslogik (Business Rules)
- ✅ Validierungsregeln
- ✅ PDF-Generierung (jspdf)
- ✅ Übersetzungen (de.json)
- ✅ Styles/CSS (mit minimalen Anpassungen)

### Konkrete Migration eines Moduls

#### Beispiel: CustomerModule Migration

**Alt (TypeScript Module):**
```typescript
// src/modules/CustomerModule.ts
export class CustomerModule extends Module {
  private form: HTMLFormElement | null = null;
  private customerService: CustomerService;
  
  async initialize(): Promise<void> {
    this.form = this.domHelper.getElement('#customerForm');
    this.setupEventListeners();
    this.loadCustomers();
  }
  
  private setupEventListeners(): void {
    this.form?.addEventListener('submit', this.handleSubmit.bind(this));
    this.eventBus.on('customer:update', this.handleUpdate.bind(this));
  }
  
  private async handleSubmit(e: Event): Promise<void> {
    e.preventDefault();
    const formData = new FormData(this.form!);
    const customer = this.validateAndTransform(formData);
    await this.customerService.save(customer);
  }
}
```

**Neu (React Component):**
```typescript
// src/components/features/Customer/CustomerForm.tsx
import { useForm } from 'react-hook-form';
import { useCustomerRepository } from '@/hooks/useCustomerRepository';
import { useEventBus } from '@/hooks/useEventBus';
import { customerValidation } from '@/utils/validation'; // WIEDERVERWENDET!

export function CustomerForm() {
  const { register, handleSubmit, formState } = useForm<CustomerData>();
  const { saveCustomer, loading } = useCustomerRepository();
  const eventBus = useEventBus();
  
  useEffect(() => {
    const unsubscribe = eventBus.on('customer:update', handleUpdate);
    return unsubscribe;
  }, []);
  
  const onSubmit = async (data: CustomerData) => {
    const validated = customerValidation.validate(data); // ALTE VALIDIERUNG!
    await saveCustomer(validated);
    eventBus.emit('customer:saved', validated);
  };
  
  return (
    <form onSubmit={handleSubmit(onSubmit)} className="customer-form">
      {/* Formular-Felder mit bestehenden CSS-Klassen */}
    </form>
  );
}
```

### Repository Pattern für sanfte Migration

```typescript
// src/repositories/ICustomerRepository.ts
export interface ICustomerRepository {
  findAll(): Promise<Customer[]>;
  findById(id: string): Promise<Customer | null>;
  save(customer: Customer): Promise<Customer>;
  delete(id: string): Promise<void>;
}

// src/repositories/LocalStorageCustomerRepository.ts
export class LocalStorageCustomerRepository implements ICustomerRepository {
  // BESTEHENDER CODE aus Phase 1 - wird 1:1 übernommen!
  async findAll(): Promise<Customer[]> {
    const data = localStorage.getItem('freshplan_customers');
    return data ? JSON.parse(data) : [];
  }
  // ...
}

// src/repositories/ApiCustomerRepository.ts
export class ApiCustomerRepository implements ICustomerRepository {
  constructor(private apiClient: ApiClient) {}
  
  async findAll(): Promise<Customer[]> {
    const response = await this.apiClient.get('/api/customers');
    return response.data;
  }
  // ...
}

// src/hooks/useCustomerRepository.ts
export function useCustomerRepository() {
  const { useApi } = useFeatureFlags();
  
  const repository = useMemo(() => {
    return useApi 
      ? new ApiCustomerRepository(apiClient)
      : new LocalStorageCustomerRepository();
  }, [useApi]);
  
  return {
    customers: useQuery(['customers'], () => repository.findAll()),
    saveCustomer: useMutation((data) => repository.save(data)),
    // ...
  };
}
```

### Event-Bus Migration zu React Context

```typescript
// src/contexts/EventBusContext.tsx
interface EventBusContextType {
  emit: (event: string, data?: any) => void;
  on: (event: string, handler: EventHandler) => () => void;
}

const EventBusContext = createContext<EventBusContextType>(null!);

export function EventBusProvider({ children }: { children: ReactNode }) {
  const handlers = useRef<Map<string, Set<EventHandler>>>(new Map());
  
  const emit = useCallback((event: string, data?: any) => {
    // Lokale Handler
    handlers.current.get(event)?.forEach(handler => handler(data));
    
    // Legacy-Kompatibilität
    if (window.legacyEventBus) {
      window.legacyEventBus.emit(event, data);
    }
  }, []);
  
  const on = useCallback((event: string, handler: EventHandler) => {
    if (!handlers.current.has(event)) {
      handlers.current.set(event, new Set());
    }
    handlers.current.get(event)!.add(handler);
    
    // Cleanup
    return () => {
      handlers.current.get(event)?.delete(handler);
    };
  }, []);
  
  return (
    <EventBusContext.Provider value={{ emit, on }}>
      {children}
    </EventBusContext.Provider>
  );
}
```

### Legacy-Wrapper für schrittweise Migration

```typescript
// src/components/legacy/LegacyModuleWrapper.tsx
import { useEffect, useRef } from 'react';
import { Module } from '@/legacy/core/Module';

interface Props {
  module: new () => Module;
  containerId: string;
  config?: any;
}

export function LegacyModuleWrapper({ module: ModuleClass, containerId, config }: Props) {
  const containerRef = useRef<HTMLDivElement>(null);
  const moduleRef = useRef<Module | null>(null);
  
  useEffect(() => {
    if (containerRef.current) {
      // Legacy-Modul initialisieren
      moduleRef.current = new ModuleClass();
      moduleRef.current.initialize(config);
      
      // DOM-Container für Legacy-Code bereitstellen
      const legacyContainer = document.createElement('div');
      legacyContainer.id = containerId;
      containerRef.current.appendChild(legacyContainer);
    }
    
    return () => {
      moduleRef.current?.destroy();
    };
  }, [ModuleClass, containerId, config]);
  
  return <div ref={containerRef} className="legacy-module-wrapper" />;
}

// Verwendung:
function App() {
  const { useReactCalculator } = useFeatureFlags();
  
  return (
    <div>
      {useReactCalculator ? (
        <CalculatorComponent /> // Neue React-Version
      ) : (
        <LegacyModuleWrapper 
          module={CalculatorModule} 
          containerId="calculator-section"
        /> // Alte TypeScript-Version
      )}
    </div>
  );
}
```

## 📁 Projekt-Setup

### 1. Neue Verzeichnisstruktur (neben bestehendem Code)

```
freshplan-sales-tool/
├── src/                    # Bestehender TypeScript-Code
│   ├── modules/           # Alte Module (bleiben während Migration)
│   ├── services/          # Services (werden schrittweise migriert)
│   └── utils/             # Utils (werden wiederverwendet!)
├── src-react/             # Neuer React-Code
│   ├── components/
│   │   ├── legacy/        # Wrapper für alte Module
│   │   ├── features/      # Neue React-Features
│   │   └── common/        # Shared Components
│   ├── hooks/             # Custom React Hooks
│   ├── repositories/      # Repository-Implementierungen
│   ├── contexts/          # React Contexts (EventBus, etc.)
│   └── App.tsx
├── vite.config.ts         # Für React-Build
└── vite.config.legacy.ts  # Für Legacy-Build
```

### 2. Build-Setup für Parallel-Betrieb

```typescript
// vite.config.ts
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src-react'),
      '@legacy': path.resolve(__dirname, './src'),
      '@shared': path.resolve(__dirname, './src/utils'), // Wiederverwendung!
    }
  },
  build: {
    outDir: 'dist-react',
    // Separater Build, um Legacy nicht zu stören
  }
});
```

### 3. Feature-Flag-System

```typescript
// src-react/config/featureFlags.ts
export const FEATURE_FLAGS = {
  USE_REACT_CUSTOMER: false,
  USE_REACT_CALCULATOR: false,
  USE_API_BACKEND: false,
  USE_KEYCLOAK_AUTH: false,
  ENABLE_OFFLINE_SYNC: true,
} as const;

// src-react/hooks/useFeatureFlags.ts
export function useFeatureFlags() {
  // Später: Flags aus Backend/LocalStorage laden
  const [flags, setFlags] = useState(FEATURE_FLAGS);
  
  useEffect(() => {
    // Check for override in localStorage (für Testing)
    const overrides = localStorage.getItem('freshplan_feature_flags');
    if (overrides) {
      setFlags(prev => ({ ...prev, ...JSON.parse(overrides) }));
    }
  }, []);
  
  return flags;
}

// Admin-UI zum Togglen
export function FeatureFlagAdmin() {
  const flags = useFeatureFlags();
  
  const toggleFlag = (flag: keyof typeof FEATURE_FLAGS) => {
    const newFlags = { ...flags, [flag]: !flags[flag] };
    localStorage.setItem('freshplan_feature_flags', JSON.stringify(newFlags));
    window.location.reload(); // Für sauberen Switch
  };
  
  return (
    <div className="feature-flags-admin">
      {Object.entries(flags).map(([key, value]) => (
        <label key={key}>
          <input
            type="checkbox"
            checked={value}
            onChange={() => toggleFlag(key as any)}
          />
          {key}
        </label>
      ))}
    </div>
  );
}
```

## 📊 Migrations-Tracking

| Modul | Status | Migration-Strategie | Notizen |
|-------|--------|-------------------|----------|
| CustomerModule | 🟡 Geplant | React Hooks + Form | Validierung wiederverwenden |
| CalculatorModule | 🟡 Geplant | React Component | Business-Logik 1:1 |
| PDFModule | ⬜ Später | Service-Layer | Keine UI-Migration nötig |
| SettingsModule | ⬜ Später | React + Keycloak | Nach Auth-Integration |
| TabNavigation | ⬜ Später | React Router | Einfache Migration |
| i18nModule | 🟢 Ready | React Context | Übersetzungen beibehalten |

### Migrations-Reihenfolge (empfohlen):

1. **Calculator** (einfachste Migration, hoher Nutzen)
2. **Customer** (Forms + Validierung testen)
3. **i18n** (wird überall gebraucht)
4. **TabNavigation** (React Router einführen)
5. **Settings** (mit Keycloak-Integration)
6. **PDF** (nur Service-Layer)

## 🚀 Rollout-Strategie

### Woche 1-2: Foundation
- React-Projekt Setup
- Build-Pipeline parallel zum Legacy
- Feature-Flag-System
- Erste Komponente (Calculator) als PoC

### Woche 3-4: Core Features
- Calculator vollständig migriert
- API-Anbindung vorbereitet
- Customer-Modul gestartet
- E2E-Tests für beide Versionen

### Woche 5-6: Integration
- Keycloak-Integration
- API-Backend aktiviert
- Offline-Sync implementiert
- Performance-Tests

### Woche 7-8: Finalisierung
- Alle Module migriert
- Legacy-Code deaktiviert (aber noch vorhanden)
- Production Release mit Feature-Flags
- Monitoring & Rollback-Plan

## ✅ Qualitätssicherung

### Testing-Strategie

```typescript
// src-react/components/features/Calculator/__tests__/Calculator.test.tsx
import { render, fireEvent } from '@testing-library/react';
import { Calculator } from '../Calculator';
import { calculateDiscount } from '@legacy/utils/calculator'; // ALTE LOGIK!

describe('Calculator React Component', () => {
  it('should calculate same results as legacy version', () => {
    const testCases = [
      { orderValue: 15000, leadTime: 5, isPickup: true },
      { orderValue: 50000, leadTime: 10, isPickup: false },
    ];
    
    testCases.forEach(testCase => {
      // Legacy-Berechnung
      const legacyResult = calculateDiscount(testCase);
      
      // React-Component Test
      const { getByTestId } = render(<Calculator />);
      // ... set inputs ...
      
      const reactResult = getByTestId('discount-result').textContent;
      
      // Muss identisch sein!
      expect(reactResult).toBe(legacyResult.totalDiscount.toString());
    });
  });
});
```

### Migration Validation Checklist
- [ ] Alle Features funktionieren identisch in beiden Versionen
- [ ] Performance mindestens gleich gut (Lighthouse Score)
- [ ] Keine Datenverluste beim Switch
- [ ] Browser-Kompatibilität erhalten
- [ ] Offline-Funktionalität weiterhin gegeben
- [ ] Alle Tests grün (Legacy + React)

## 🔄 Rollback-Plan

Falls Probleme auftreten:

```typescript
// 1. Feature Flags sofort deaktivieren
localStorage.setItem('freshplan_feature_flags', JSON.stringify({
  USE_REACT_CUSTOMER: false,
  USE_REACT_CALCULATOR: false,
  USE_API_BACKEND: false,
  USE_KEYCLOAK_AUTH: false
}));

// 2. Legacy-Version aktivieren
window.location.href = '/index-legacy.html';

// 3. Monitoring
console.error('[ROLLBACK] Switching to legacy version due to:', error);

// 4. Notification
if (window.Sentry) {
  window.Sentry.captureException(error, {
    tags: { rollback: true, version: 'react' }
  });
}
```

### Rollback-Trigger:
- Performance-Degradation > 20%
- Fehlerrate > 1%
- Kritische Features nicht funktionsfähig
- Datenverlust erkannt

## 📈 Erfolgsmetriken

### Performance
- **Bundle-Size:** < 500KB (mit Code-Splitting)
- **Initial Load:** < 2s (Lighthouse Score > 90)
- **Runtime Performance:** Keine spürbaren Unterschiede

### Code-Qualität
- **TypeScript Coverage:** 100% (strict mode)
- **Test Coverage:** > 80%
- **0 kritische Linting-Fehler**

### Business Metrics
- **Keine Funktionsverluste**
- **Alle Kunden-Workflows identisch**
- **Datenintegrität 100%**

## 🎯 Quick Wins

1. **Tag 1-2:** Calculator als React-PoC
2. **Tag 3-4:** Feature-Flag-System live
3. **Tag 5:** Erster A/B-Test mit internen Usern
4. **Woche 2:** Performance-Vergleich dokumentiert

## 📅 Zeitplan

| Phase | Dauer | Fokus |
|-------|-------|-------|
| Setup & Foundation | 2 Wochen | React-Setup, Build-Pipeline, Feature-Flags |
| Core Migration | 3 Wochen | Calculator, Customer, Repository-Pattern |
| Module Migration | 4 Wochen | Alle Module, Testing, Performance |
| Integration & Go-Live | 3 Wochen | API-Anbindung, Keycloak, Production |

**Gesamt: 12 Wochen** (3 Monate)

## 🛠️ Benötigte Ressourcen

### Team
- 2 Frontend-Entwickler (React-Erfahrung)
- 1 Backend-Entwickler (Quarkus/Java)
- 1 DevOps (AWS, CI/CD)
- 1 QA/Tester

### Tools & Services
- GitHub (Repository, Actions)
- AWS Account (Dev + Prod)
- Keycloak Instance
- Monitoring (Sentry, DataDog)

## 📌 Wichtige Entscheidungen

### Bereits entschieden:
- ✅ React + TypeScript + Vite
- ✅ Schrittweise Migration mit Feature-Flags
- ✅ Code-Reuse maximieren
- ✅ Repository-Pattern für API-Migration

### Noch zu klären:
- ⏳ State Management: Redux Toolkit vs. Zustand?
- ⏳ UI-Library: Material-UI vs. Tailwind?
- ⏳ Testing: Playwright vs. Cypress?
- ⏳ Monitoring: Sentry vs. DataDog?

---

**Dokument Version:** 2.0  
**Aktualisiert:** 05.06.2025  
**Status:** Ready to Start 🚀

_Nächster Review: Nach Setup-Phase (Woche 2)_