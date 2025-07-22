# FreshPlan 2.0 - Frontend (React)

**📅 Aktuelles Datum: <!-- AUTO_DATE --> (System: 08.06.2025)**

## 🚀 React + TypeScript + Vite

Frontend-Anwendung für die FreshPlan 2.0 Enterprise Sales Platform.

### Tech Stack

- **React 18** - UI Framework
- **TypeScript** - Type Safety
- **Vite** - Build Tool with HMR
- **ESLint & Prettier** - Code Quality
- **Vitest** - Unit Testing
- **React Testing Library** - Component Testing
- **Keycloak JS** - Authentication (coming soon)

### Getting Started

```bash
# Install dependencies
npm install

# Start development server
npm run dev

# Run tests
npm run test

# Type check
npm run type-check

# Lint code
npm run lint

# Format code
npm run format
```

### Project Structure

```
frontend/
├── src/
│   ├── components/     # Reusable UI components
│   ├── contexts/       # React contexts (Auth, etc.)
│   ├── hooks/          # Custom React hooks
│   ├── pages/          # Page components
│   ├── services/       # API services
│   └── utils/          # Helper functions
├── public/             # Static assets
└── tests/              # Test files
```

### Available Scripts

- `npm run dev` - Start development server on http://localhost:5173
- `npm run build` - Build for production
- `npm run preview` - Preview production build
- `npm run lint` - Run ESLint
- `npm run format` - Format code with Prettier
- `npm run type-check` - Run TypeScript compiler check
- `npm run test` - Run unit tests with Vitest
- `npm run test:ui` - Run tests with UI

### Code Quality

This project uses:

- **ESLint** for code linting with TypeScript and React rules
- **Prettier** for consistent code formatting
- **Husky** for pre-commit hooks
- **lint-staged** to run linters on staged files

### Environment Variables

Create a `.env` file based on `.env.example`:

```bash
VITE_KEYCLOAK_URL=http://localhost:8180
VITE_API_URL=http://localhost:8080
```

### Testing

Tests are written using Vitest and React Testing Library. Run tests with:

```bash
npm run test        # Run once
npm run test:ui     # Interactive UI
```

### Team

- Frontend Team + ChatGPT AI Assistant

### Sprint 0 Status

- ✅ React app initialized with Vite
- ✅ TypeScript configured
- ✅ ESLint & Prettier setup
- ✅ Basic test infrastructure
- ✅ AuthContext placeholder
- 🚧 Keycloak integration (pending)
- 🚧 Material-UI setup (pending)
- 🚧 API client setup (pending)

## 📚 Wichtige Dokumentation für Frontend-Entwickler

### Essenzielle Docs (auch ohne Root-Zugriff)

#### 🏗️ Architektur & Standards

- **Frontend Design System**: Siehe UI Foundation Module
- **Component Architecture**: Dokumentation in Arbeit
- **Code Standards**: TypeScript Strict Mode, ESLint Rules in `.eslintrc`

#### 🔧 Setup & Development

- **Local Development Setup**:
  ```bash
  npm install
  npm run dev
  # Backend läuft auf http://localhost:8080
  # Frontend läuft auf http://localhost:5173
  ```
- **Environment Variables**: Siehe `.env.example`
- **Mock Service Worker**: Für Backend-unabhängige Entwicklung in `src/mocks/`

#### 🌐 API Integration

- **API Endpoints**:
  - Users: `GET/POST/PUT/DELETE /api/users`
  - Calculator: `POST /api/calculator/calculate`
  - Auth: Keycloak auf Port 8180
- **API Client**: `src/shared/lib/apiClient.ts`
- **React Query**: Für Server State Management

#### 🧪 Testing

- **Unit Tests**: `npm run test`
- **Component Tests**: `*.test.tsx` Dateien
- **E2E Tests**: Playwright in `/tests` _(wenn Zugriff)_

#### ⚠️ Known Issues

- Users-Page: Rolle 'viewer' muss in Zod Schema sein
- Auto-Login in Dev: Token muss in localStorage
- Hot Reload: Manchmal Neustart nötig

#### 🚀 Deployment

- Build: `npm run build`
- Preview: `npm run preview`
- Docker: Siehe `Dockerfile`

### Wo finde ich was? (Frontend-Perspektive)

```
frontend/
├── src/
│   ├── features/      # Feature-Module (users, calculator)
│   ├── components/    # Wiederverwendbare UI-Komponenten
│   ├── pages/         # Route-Pages
│   ├── contexts/      # React Contexts (Auth)
│   ├── shared/        # Geteilte Utils (API Client)
│   └── styles/        # CSS und Design System
├── public/            # Statische Assets
└── tests/             # E2E Tests
```

### Support & Hilfe

- **Team Chat**: Slack #frontend-dev
- **Code Reviews**: PR Template beachten
- **Probleme**: Issue in GitHub erstellen

## 📖 Quick Links (Frontend-spezifisch)

- Troubleshooting Guide - In Arbeit
- API Cheatsheet - Siehe Backend README
- Design System - Siehe UI Foundation Module
