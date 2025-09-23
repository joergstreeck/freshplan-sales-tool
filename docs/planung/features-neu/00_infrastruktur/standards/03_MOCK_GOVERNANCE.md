# Mock-Governance (Business-Logic mock-frei)

**📊 Status:** ✅ Ready for Implementation
**🎯 Owner:** Development Team
**⏱️ Timeline:** Sprint 1.1 (Setup) → Sprint 1.2 (Cockpit Ent-Mocking)
**🔗 ADR:** [ADR-0006](../../../adr/ADR-0006-mock-governance.md)

## 🎯 Ziel

**Keine Mock-Daten in Business-Logic; Mocks nur in Tests/Stories. Dev-Daten via Seeds. API-Layer minimal robust.**

## 📏 Gültigkeit

**Verbotene Pfade (Business-Logic):**
- `src/app/**/*.{ts,tsx}`
- `src/features/**/*.{ts,tsx}`
- `src/lib/**/*.{ts,tsx}`
- `src/hooks/**/*.{ts,tsx}`
- `src/store/**/*.{ts,tsx}`

**Erlaubte Pfade (Ausnahmen):**
- `__tests__/`, `tests/`, `*.test.*`, `*.spec.*`
- `__mocks__/`, `fixtures/`
- `*.stories.*`, `.storybook/`, `storybook/`

## ✅ Regeln (DoD)

- [ ] **Business-Logic ohne `mockData`, `*Mock*`** (ESLint/CI enforced)
- [ ] **Tests/Stories dürfen Mocks nutzen** – strikt getrennt
- [ ] **Dev-Daten über Flyway `db/dev-migration`** + optionalen Seed-Endpoint (nur dev)
- [ ] **API-Layer:** React Query + Zod (wo sinnvoll), einheitlicher `ApiError`

## 🔧 ESLint (Business-Logic ohne Mocks)

**Frontend/.eslintrc.cjs:**
```js
// frontend/.eslintrc.cjs (Ausschnitt)
module.exports = {
  overrides: [{
    files: [
      'src/app/**/*.{ts,tsx}','src/features/**/*.{ts,tsx}',
      'src/lib/**/*.{ts,tsx}','src/hooks/**/*.{ts,tsx}','src/store/**/*.{ts,tsx}'
    ],
    excludedFiles: [
      '**/{__tests__,tests,__mocks__,fixtures,.storybook,storybook}/**',
      '**/*.{spec,test,stories}.*'
    ],
    rules: {
      'no-restricted-imports': ['error', { patterns: [{
        group: ['**/mock*','**/*Mock*','**/mocks/**','**/data/mock*'],
        message: 'Mock-Daten in Business-Logic verboten. Bitte echte APIs/Seeds/Test-Factories nutzen.'
      }]}]
    }
  }]
};
```

## 🤖 CI-Guard (PR-Scan)

**.github/workflows/mock-guard.yml:**
```yaml
name: mock-guard
on:
  pull_request:
    branches: [ main ]
jobs:
  scan:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Install ripgrep
        run: sudo apt-get update -y >/dev/null && sudo apt-get install -y ripgrep >/dev/null

      - name: Frontend mock scan (business logic only)
        run: |
          set -euo pipefail
          PATHS=$(printf "%s\n" "frontend/src/app" "frontend/src/features" "frontend/src/lib" "frontend/src/hooks" "frontend/src/store")
          PATTERN='(import|from).*mock|mockData|__mocks__/|/mocks?/'
          FOUND=""
          for P in $PATHS; do
            HITS=$(rg -n "$PATTERN" "$P" \
              --glob '!**/{__tests__,tests,__mocks__,fixtures,.storybook,storybook}/**' \
              --glob '!**/*.{spec,test,stories}.*' || true)
            if [ -n "$HITS" ]; then FOUND="$FOUND\\n$HITS"; fi
          done
          if [ -n "$FOUND" ]; then
            echo "❌ Disallowed mock references in business logic:"
            echo -e "$FOUND"
            exit 1
          fi
          echo "✅ No disallowed mocks in business logic."
```

## 🪝 Pre-commit (einfach)

**.husky/pre-commit:**
```bash
#!/bin/sh
. "$(dirname "$0")/_/husky.sh"
FILES=$(git diff --cached --name-only --diff-filter=ACMR | grep -E '\\.tsx?' || true)
[ -z "$FILES" ] && exit 0
echo "🔎 Running ESLint on staged TypeScript files..."
npx eslint --max-warnings 0 $FILES
```

## 🌱 Dev-Seeds & Profile-Schutz

**application-dev.properties:**
```properties
quarkus.flyway.locations=classpath:db/migration,classpath:db/dev-migration
freshplan.seed.token=change-me
```

**Dev-Migration Beispiel:**
```sql
-- backend/src/main/resources/db/dev-migration/V8001__dev_seed_minimal.sql
-- ⚠️ Läuft nur im DEV-Profil (separater Flyway-Pfad)
INSERT INTO tags (id, name) VALUES ('dev-quickstart','Dev Quickstart') ON CONFLICT DO NOTHING;
INSERT INTO customers (id, name, status) VALUES ('dev-customer-1','[DEV] Müller GmbH','ACTIVE') ON CONFLICT DO NOTHING;
```

**Optional: Seed-Endpoint (nur dev):**
```java
// backend/src/main/java/de/freshplan/dev/SeedResource.java
@Path("/dev/seed")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class SeedResource {
    @POST
    @Transactional
    public Response seed(@HeaderParam("X-Seed-Token") String token) {
        if (!"dev".equals(ProfileManager.getActiveProfile())) {
            return Response.status(Response.Status.FORBIDDEN)
                .entity(Map.of("error","available only in dev profile")).build();
        }
        if (!Objects.equals(token, System.getProperty("freshplan.seed.token","dev-only"))) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        // TODO: TestDataBuilder-Integration hier
        return Response.ok(Map.of("status","seeded")).build();
    }
}
```

## 🔌 API-Layer Standards

**ApiError (robust ohne Over-Engineering):**
```typescript
// frontend/src/lib/api/error.ts
export class ApiError extends Error {
  status: number;
  url: string;
  body?: unknown;
  isRetryable: boolean;
  userMessage: string;
  context?: Record<string, unknown>;

  constructor(msg: string, status: number, url: string, body?: unknown, ctx?: Record<string, unknown>) {
    super(msg);
    this.status = status;
    this.url = url;
    this.body = body;
    this.context = ctx;
    this.isRetryable = status >= 500 || status === 429;
    this.userMessage = status === 401 ? 'Bitte anmelden.' :
                       status === 403 ? 'Zugriff verweigert.' :
                       status >= 500   ? 'Serverfehler. Bitte später erneut.' :
                                         'Anfrage fehlgeschlagen.';
  }
}
```

**API Client + Zod:**
```typescript
// frontend/src/lib/api/client.ts
import { z } from 'zod';
import { ApiError } from './error';

export async function api<T>(
  path: string,
  schema?: z.ZodType<T>,
  init?: RequestInit
): Promise<T> {
  const res = await fetch(`/api${path}`, { credentials: 'include', ...init });
  const text = await res.text();
  const json = text ? JSON.parse(text) : null;

  if (!res.ok) {
    throw new ApiError(`API ${path} failed`, res.status, path, json ?? text);
  }
  if (schema) return schema.parse(json);
  return json as T;
}
```

**React Query Integration:**
```typescript
// frontend/src/features/cockpit/api/useMyDay.ts
import { useQuery } from '@tanstack/react-query';
import { api } from '../../../lib/api/client';
import { MyDaySchema, type MyDay } from './myDay.schemas';

export function useMyDay() {
  return useQuery<MyDay>({
    queryKey: ['my-day'],
    queryFn: () => api('/cockpit/my-day', MyDaySchema),
    staleTime: 30_000,
    retry: 1
  });
}
```

## 🎯 Implementation Roadmap

### **Sprint 1.1 - Governance Setup**
- [ ] ADR-0006 dokumentiert
- [ ] ESLint-Regel aktiviert
- [ ] CI-Guard implementiert
- [ ] Dev-Flyway-Pfad eingerichtet
- [ ] Optional: Husky pre-commit

### **Sprint 1.2 - API Foundation & Cockpit**
- [ ] Backend: `GET /cockpit/my-day`
- [ ] Frontend: `useMyDay()` via React Query + Zod
- [ ] Entferne `frontend/src/features/cockpit/data/mockData.ts`
- [ ] Error/Loading/Empty-Zustände

### **Further Sprints**
- Schrittweise Mock-Elimination weitere Module
- Nach CQRS-Foundation & Query-APIs verfügbar

## ⚠️ Risiken & Mitigationen

**Risiko: False Positives im CI-Guard**
- **Mitigation:** Ausnahme-Globs synchron mit Ordnerstruktur halten

**Risiko: Dev-Seeds versehentlich in Prod**
- **Mitigation:** Profile-Check + CI-Guard gegen dev-migration in application.properties

**Risiko: API-Layer zu komplex**
- **Mitigation:** Zod optional, ApiError minimal, React Query standard

## 🔗 Related

- **Strategy:** [Mock Infrastructure Analysis](../betrieb/analyse/02_MOCK_INFRASTRUCTURE_ANALYSIS.md)
- **Decision:** [ADR-0006](../../adr/ADR-0006-mock-governance.md)
- **Implementation:** [TRIGGER_SPRINT_1_1.md](../../TRIGGER_SPRINT_1_1.md)