# Frontend Troubleshooting Guide

**Letzte Aktualisierung:** 08.06.2025

## 🔥 Häufige Probleme & Lösungen

### 1. "Fehler beim Laden der Benutzer"

**Problem:** Users-Page zeigt Fehler, obwohl Backend läuft

**Ursache:** Zod Schema kennt nicht alle Rollen vom Backend

**Lösung:**

```typescript
// In src/features/users/api/userSchemas.ts
export const UserRole = z.enum(['admin', 'manager', 'sales']);
// Nur diese drei Rollen sind verfügbar!
```

### 2. Auto-Login funktioniert nicht

**Problem:** In Development kein automatischer Login

**Ursache:** Token wird nicht in localStorage gespeichert

**Lösung:** AuthContext muss Token synchronisieren:

```typescript
// Bei Auto-Login:
localStorage.setItem('auth-token', mockToken);
localStorage.setItem('auth-user', JSON.stringify(mockUser));
```

### 3. Hot Reload hängt

**Problem:** Änderungen werden nicht angezeigt

**Lösungen:**

1. Browser Hard-Refresh: `Cmd+Shift+R` (Mac) / `Ctrl+Shift+R` (Win)
2. Vite neustarten: `Ctrl+C` dann `npm run dev`
3. Cache löschen: `rm -rf node_modules/.vite`

### 4. CORS Fehler bei API-Calls

**Problem:** "Access-Control-Allow-Origin" Fehler

**Checks:**

- Backend läuft auf http://localhost:8080?
- Frontend läuft auf http://localhost:5173?
- Nicht http**s** verwenden in Development!

### 5. TypeScript Fehler nach Package-Update

**Problem:** Rote Unterstreichungen überall

**Lösung:**

```bash
# VS Code neustarten
# Oder TypeScript Server neustarten:
Cmd+Shift+P → "TypeScript: Restart TS Server"
```

### 6. Tests schlagen fehl

**Problem:** Tests die vorher liefen, sind jetzt rot

**Mögliche Ursachen:**

- Mock Service Worker nicht gestartet
- Timeouts zu kurz
- React Query Cache nicht geleert

**Lösung:**

```typescript
// In Setup-Datei:
beforeEach(() => {
  queryClient.clear();
});
```

### 7. Build schlägt fehl

**Problem:** `npm run build` gibt Fehler

**Häufige Ursachen:**

- Ungenutzte Imports → ESLint Fehler
- TypeScript strict mode → Any-Types
- Fehlende Environment Variables

**Debug:**

```bash
# Erst TypeScript prüfen:
npx tsc --noEmit

# Dann ESLint:
npm run lint
```

## 🛠️ Allgemeine Debug-Tipps

### Browser DevTools

- **Network Tab**: Sehen ob API-Calls rausgehen
- **Console**: JavaScript Fehler checken
- **React DevTools**: Component State inspizieren

### Logs checken

```bash
# Backend Logs:
cd ../backend && ./mvnw quarkus:dev

# Frontend Build Logs:
npm run build -- --debug
```

### Clean Install

```bash
# Nuclear Option - alles neu:
rm -rf node_modules package-lock.json
npm install
npm run dev
```

## 📞 Hilfe holen

1. **Erst hier schauen** → Diese Datei
2. **Dann README** → Setup-Schritte prüfen
3. **Dann Team** → Slack #frontend-dev
4. **Dann Issue** → GitHub Issue erstellen
