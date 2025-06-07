# FreshPlan 2.0 - Frontend (React)

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
