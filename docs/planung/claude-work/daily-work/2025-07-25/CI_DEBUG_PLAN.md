# 📋 CI Pipeline Debug Plan - 25.07.2025

## 🎯 Ziel
CI Pipeline für PR #68 komplett grün bekommen mit strukturiertem, lokalem Testing

## 📊 Aktuelle Situation

### CI Status:
- ✅ Backend CI: Grün 
- ❌ Lint & Format Check: Rot (Backend Tests schlagen fehl)
- ❌ Smoke Tests: Rot (h1 Element nicht gefunden)
- ❌ Integration Tests: Rot (vermutlich Folgefehler)

### Identifizierte Probleme:

1. **Smoke Test Problem:**
   - Fehler: `h1.main-title` oder `h1:has-text("FreshPlan")` nicht gefunden
   - Timeout nach 15 Sekunden
   - App scheint nicht zu rendern oder andere Struktur zu haben

2. **Backend Test Problem:**
   - Tests laufen in CI mit speziellen Parametern
   - Möglicherweise Datenbank-Verbindungsproblem

## 🔧 Strukturierter Lösungsansatz

### Phase 1: Lokale Reproduktion & Analyse

#### 1.1 Frontend Smoke Test lokal reproduzieren
```bash
cd frontend
# Frontend starten
npm run dev &

# Warte bis Frontend bereit
sleep 5

# Smoke Test lokal ausführen
npx playwright test tests/auth.spec.ts --project=chromium --headed

# Bei Fehler: Debug Mode
npx playwright test tests/auth.spec.ts --project=chromium --debug
```

#### 1.2 HTML-Struktur analysieren
```bash
# Playwright Codegen nutzen um tatsächliche Struktur zu sehen
npx playwright codegen http://localhost:5173

# Screenshot der Seite machen
npx playwright screenshot http://localhost:5173 screenshot.png
```

#### 1.3 Backend Tests lokal mit CI-Parametern
```bash
cd backend
# Exakt wie in CI
mvn -B clean verify -Pgreen -Dquarkus.profile=test \
  -Dquarkus.datasource.devservices.enabled=false \
  -Dquarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/freshplan \
  -Dquarkus.datasource.username=freshplan \
  -Dquarkus.datasource.password=freshplan
```

### Phase 2: Fixes basierend auf Analyse

#### 2.1 Smoke Test Fix-Optionen

**Option A: Robusterer Selektor**
```typescript
// Mehrere Fallback-Optionen
const titleLocator = page.locator('h1').filter({ hasText: /FreshPlan/i });
// oder
const titleLocator = page.getByRole('heading', { name: /FreshPlan/i, level: 1 });
```

**Option B: Warte auf App-Initialisierung**
```typescript
// Warte auf spezifisches App-Element
await page.waitForSelector('#root', { state: 'attached' });
await page.waitForLoadState('networkidle');
```

**Option C: Vereinfachter Test**
```typescript
// Nur prüfen ob App überhaupt lädt
await expect(page).toHaveTitle(/FreshPlan/);
await expect(page.locator('body')).toBeVisible();
```

#### 2.2 Backend Test Fix
- Prüfen ob PostgreSQL in CI korrekt läuft
- Event. Test-Profile anpassen
- Flyway-Migrationen prüfen

### Phase 3: Inkrementelle Fixes

1. **Erst Smoke Test fixen** (schnellster Fix)
2. **Dann Backend Tests** (komplexer)
3. **Integration Tests** sollten dann automatisch grün werden

### Phase 4: Validierung

Nach jedem Fix:
1. Lokal testen
2. Commit mit klarer Message
3. Push und CI beobachten
4. Bei Erfolg: Nächster Fix

## 📝 Tracking

- [ ] Smoke Test lokal reproduziert
- [ ] HTML-Struktur analysiert
- [ ] Smoke Test Fix implementiert
- [ ] Smoke Test lokal grün
- [ ] Backend Tests lokal mit CI-Params getestet
- [ ] Backend Test Fix implementiert
- [ ] Alle Tests lokal grün
- [ ] CI komplett grün

## 🚀 Nächster Schritt
Start mit Phase 1.1 - Smoke Test lokal reproduzieren