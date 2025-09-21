# 🤖 Claude Meta-Prompt für FreshPlan Sales Tool

**📅 Aktuelles Datum: <!-- AUTO_DATE --> (System: 08.06.2025)**

## 🎯 CLAUDE'S ARBEITSWEISE

### ⚡ EINZIGE WAHRHEIT:
**Master Plan V5:** `/docs/planung/CRM_COMPLETE_MASTER_PLAN_V5.md`

**Dort findest du ALLES:**
- Aktueller Projektstand
- Nächste Schritte
- Technical Concepts
- Troubleshooting-Links
- Übergabe-Workflows

## 🚨 KRITISCHE REGELN (IMMER LESEN!)

### Die 17 wichtigsten Regeln für Claude:

1. **REPOSITORY SAUBER HALTEN:** VOR JEDEM GIT PUSH/COMMIT muss `./scripts/quick-cleanup.sh` ausgeführt werden!
2. **SPRACHE:** IMMER Deutsch - auch bei kurzen Antworten, Status-Updates, Zusammenfassungen!
3. **GRÜNDLICHKEIT:** Gründlichkeit geht VOR Schnelligkeit - keine Quick-Fixes ohne Dokumentation
4. **TESTS:** KEINE Implementierung ohne Tests (Unit ≥80%, Integration, Browser-Tests)
5. **DOKUMENTATION:** Strukturiert ablegen in `docs/planung/claude-work/` mit Datum + Kategorie
6. **CODE-REVIEW:** Nach JEDEM bedeutenden Abschnitt + Two-Pass Review durchführen
7. **ARCHITEKTUR:** Strikte Trennung - Backend: domain/api/infrastructure, Frontend: features/components
8. **ZEILENLÄNGE:** Max. 80-100 Zeichen - nutze Umbrüche und Hilfsvariablen
9. **KEINE NEUEN DATEIEN:** IMMER bestehende Dateien editieren statt neue erstellen
10. **CLEAN CODE:** SOLID, DRY, KISS - Code muss selbsterklärend sein
11. **SECURITY:** Keine Hardcoded Secrets, Input Validation überall, CORS korrekt
12. **PROAKTIVITÄT:** Fasse Verständnis zusammen und frage BEVOR du codest
13. **INKREMENTELL:** Kleine, nachvollziehbare Schritte - teste häufig
14. **CI GRÜN HALTEN:** Bei roter CI selbstständig debuggen mit GitHub CLI - siehe Master Plan V5
15. **NAMING:** PascalCase für Klassen, camelCase für Methoden, UPPER_SNAKE für Konstanten
16. **PERFORMANCE:** Bundle ≤200KB, API <200ms P95, keine N+1 Queries
17. **FRESHFOODZ CI COMPLIANCE:** ALLE sichtbaren Frontend-Elemente MÜSSEN Freshfoodz CI verwenden (#94C456, #004F7B, Antonio Bold, Poppins)

## 🆘 BEI PROBLEMEN

**Alle Lösungen findest du im Master Plan V5!**
Dort sind alle aktuellen Troubleshooting-Links, Debug-Guides und Workarounds verlinkt.

---

<!-- Alle Links sind im Master Plan V5 - keine doppelte Pflege! -->

## 0. Grundlegende Arbeitsphilosophie

**🎯 UNSERE DEVISE: GRÜNDLICHKEIT GEHT VOR SCHNELLIGKEIT**

- Jede Implementierung muss gründlich getestet werden
- Keine Quick-Fixes oder Workarounds ohne Dokumentation
- Denke immer an die zukünftigen Integrationen und Erweiterungen
- Was wir jetzt richtig machen, erspart uns später Arbeit
- Siehe Master Plan V5 für die langfristige Ausrichtung des Projekts

### Unser Code-Qualitäts-Versprechen

**Wir verpflichten uns, bei jedem Commit auf:**

**Sauberkeit**
- Klare Modul-Grenzen, sprechende Namen, keine toten Pfade

**Robustheit**
- Unit-, Integrations- und E2E-Tests, defensives Error-Handling

**Wartbarkeit**
- SOLID, DRY, CI-Checks, lückenlose Docs & ADRs

**Transparenz**
- Unklarheiten sofort kanalisieren → Issue / Stand-up

## 0.1 Code-Standards und Architektur

**📋 Detaillierte Standards:** Siehe Master Plan V5

### 🎯 Core Prinzipien:
- **Clean Code** - Lesbarkeit vor Cleverness
- **SOLID** - Eine Verantwortung pro Klasse
- **DRY/KISS/YAGNI** - Einfach und wartbar

### 🏗️ Architektur-Überblick:

**Backend (Quarkus/Java):**
```
backend/modules/customer/{core,contacts,financials,timeline}
backend/legacy/ (Migration laufend)
```

**Frontend (React/TypeScript):**
```
frontend/{components,features,pages,services}
```

**⚠️ TypeScript KRITISCH:** `import type` für alle Types bei Vite!

### 📏 Qualitäts-Gates:
- **Zeilenlänge:** 80-100 Zeichen
- **Test Coverage:** ≥80%
- **Naming:** PascalCase/camelCase/UPPER_SNAKE
- **Git:** `./scripts/quick-cleanup.sh` vor jedem Push

## 0.2 DevOps & Release-Management

### Branch- & Release-Strategie:
| Thema | Empfehlung | Nutzen |
|-------|------------|--------|
| Branching | Trunk-based mit short-lived feature branches (max. 24h offen) | Weniger Merge-Konflikte, kontinuierliche Integration |
| Commit-Konvention | Conventional Commits + Commitlint | Automatische CHANGELOGs & Releases |
| Releases | SemVer + GitHub Actions Release-Workflow | Klare Versionierung, Hot-fix-Pfad |

### Release-Workflow:
```bash
# Feature fertig? Merge to main
git checkout main && git pull
git merge --no-ff feature/user-management

# Release vorbereiten
npm version minor  # oder major/patch
git push && git push --tags

# GitHub Action erstellt automatisch:
# - Release Notes aus Commits
# - Docker Images mit Tags
# - Deployment zu Stage
```

### Dokumentation & Wissenstransfer:
- **ADRs** (Architecture Decision Records) für alle wichtigen Entscheidungen
  - Template: [ADR Template][adr-template]
  - Automatisierung: `adr-tools` → PR-Kommentar mit Diff
- **Onboarding-Playbook**: 90-Minuten "Tour de Codebase"
  - README mit Links zu Key-Files
  - Architektur-Diagramme (C4 Model)
  - Video-Walkthrough für neue Teammitglieder
- **Tech Radar**: Bewertung neuer Libraries/Tools
  - Adopt / Trial / Assess / Hold
  - Quartalsweise Review

## 0.3 Security & Compliance

### Security-Standards:
| Ebene | Regel | Automatisierung |
|-------|-------|-----------------|
| Dependencies | Snyk + Dependabot, auto-merge wenn CVSS < 4 | CI-Gate |
| Secrets | GitHub Secrets → env-subst in Docker | Keine Secrets im Code |
| API Security | OWASP Top 10 Check | ZAP-Docker nightly |
| Code Quality | SonarCloud Security Hotspots | PR-Block bei kritisch |

### Security-Checkliste:
```yaml
# .github/workflows/security.yml
- Dependency Check (Snyk)
- SAST (SonarCloud)
- Container Scan (Trivy)
- API Security Test (OWASP ZAP)
- Secret Scanning (GitGuardian)
```

### Compliance:
- **DSGVO**: Personenbezogene Daten verschlüsselt
- **Audit-Log**: Alle kritischen Operationen
- **Data Retention**: Automatisches Löschen nach X Tagen

## 0.4 Observability & Performance

### Golden Signals:
- **Latency**: < 200ms P95 für API Calls
- **Traffic**: Requests per Second
- **Errors**: < 0.1% Error Rate
- **Saturation**: CPU/Memory < 80%

### Monitoring Stack:
```yaml
# OpenTelemetry → CloudWatch/X-Ray Pipeline
- Distributed Tracing (Jaeger-kompatibel)
- Metrics (Prometheus-Format)
- Logs (strukturiert, JSON)
- Real User Monitoring (RUM)
```

### Performance Budgets:

#### Frontend:
- **Bundle Size**: ≤ 200 KB initial (gzipped)
- **LCP**: ≤ 2.5s (mobile 3G)
- **FID**: ≤ 100ms
- **CLS**: ≤ 0.1
- **Lighthouse Score**: ≥ 90

#### Backend:
- **API Response**: P95 < 200ms
- **Database Queries**: < 50ms
- **Memory per Request**: < 50MB
- **Startup Time**: < 10s

### Performance-Gates:
```bash
# Lighthouse CI als GitHub Check
lighthouse:
  assertions:
    categories:performance: ["error", {"minScore": 0.9}]
    first-contentful-paint: ["error", {"maxNumericValue": 2000}]
    interactive: ["error", {"maxNumericValue": 5000}]
```

## 0.5 Testing-Pyramide

### Test-Strategie:
| Stufe | Coverage-Ziel | Technologie | Scope |
|-------|---------------|-------------|-------|
| Unit | 80% Lines/Functions | JUnit 5 + Mockito / Vitest | Business Logic |
| Integration | 100% API Endpoints | RestAssured / MSW | API Contracts |
| E2E | Critical User Journeys | Playwright | Happy Paths |
| Performance | Key Transactions | k6 / Artillery | Load Testing |
| Security | OWASP Top 10 | ZAP / Burp | Penetration |

### Test-Patterns:
```java
// Given-When-Then für BDD
@Test
void createUser_withValidData_shouldReturnCreatedUser() {
    // Given
    var request = validUserRequest();
    
    // When
    var response = userService.createUser(request);
    
    // Then
    assertThat(response).satisfies(user -> {
        assertThat(user.getId()).isNotNull();
        assertThat(user.getUsername()).isEqualTo("john.doe");
    });
}
```

### CI-Monitoring und Automatisches Debugging

**NEU: Proaktive CI-Überwachung durch Claude** 🤖

Wenn die CI rot ist:
1. **Claude holt sich selbstständig die Logs** via GitHub CLI
2. **Analysiert den Fehler** und versucht eigenständig zu fixen
3. **Pusht die Lösung** und überwacht erneut
4. **Eskaliert nur bei:** 
   - Komplexen Problemen die mehrere Versuche erfordern
   - Architektur-Entscheidungen
   - Unklarheiten über Business-Logik

```bash
# Claude's CI-Workflow
gh run list --branch <branch> --status failure --limit 1
gh run view <RUN_ID> --log-failed
# Analyse → Fix → Push → Repeat
```

**Vorteile:**
- ✅ Schnellere Fixes (keine Wartezeit)
- ✅ Jörg wird nur bei echten Problemen involviert
- ✅ CI bleibt häufiger grün
- ✅ Teams können sich auf Features konzentrieren

### CI-Debugging-Strategie: "Strategie der kleinen Schritte"

**Wenn die CI nach mehreren automatischen Versuchen immer noch fehlschlägt:**

#### 1. Minimierung des Fehlerbereichs
```bash
# Beispiel: Test isoliert ausführen
./mvnw test -Dtest=UserServiceTest#testGetAllUsers

# Mit Debug-Output
./mvnw test -Dtest=UserServiceTest#testGetAllUsers -X
```

#### 2. Schrittweise Vereinfachung
```java
@Test
void debugTest() {
    // Schritt 1: Minimaler Test
    System.out.println("=== DEBUG: Test startet ===");
    assertThat(1).isEqualTo(1);
    
    // Schritt 2: Eine Komponente hinzufügen
    System.out.println("=== DEBUG: Repository-Mock ===");
    when(repository.findAll()).thenReturn(List.of());
    
    // Schritt 3: Schrittweise erweitern
    System.out.println("=== DEBUG: Service-Aufruf ===");
    var result = service.getAllUsers();
    System.out.println("=== DEBUG: Result size: " + result.size());
}
```

#### 3. Debug-Output Best Practices
```java
// Strukturierter Debug-Output
private void debugLog(String phase, Object data) {
    System.out.printf("=== DEBUG [%s]: %s ===%n", 
        phase, 
        data != null ? data.toString() : "null"
    );
}

// In Tests verwenden
debugLog("BEFORE", testUser);
var result = service.updateUser(testUser);
debugLog("AFTER", result);
```

#### 4. CI-spezifisches Debugging
```yaml
# .github/workflows/ci.yml
- name: Run failing test with debug
  if: failure()
  run: |
    echo "=== Rerunning failed test with debug ==="
    ./mvnw test -Dtest=FailingTest -X
    echo "=== Environment Info ==="
    java -version
    ./mvnw --version
```

#### 5. Systematisches Vorgehen
1. **Isolieren**: Einzelnen Test ausführen
2. **Minimieren**: Test auf Kern-Assertion reduzieren
3. **Debug-Output**: An kritischen Stellen einfügen
4. **Schrittweise erweitern**: Eine Zeile nach der anderen
5. **Vergleichen**: Lokal vs. CI Environment
6. **Dokumentieren**: Findings für Team festhalten

#### Anti-Patterns vermeiden:
- ❌ Blind try-catch ohne Logging
- ❌ System.out.println ohne Kontext
- ❌ Große Code-Blöcke auf einmal ändern
- ❌ Debug-Output in Production-Code vergessen

### Feature Flag Governance:

**Trunk-based Development erfordert strikte Feature Flag Disziplin!**

#### Namenskonvention:
```
ff_<ticket-nr>_<kurzer-name>
Beispiel: ff_FRESH-123_user_export
```

#### Feature Flag Manifest:
```java
@FeatureFlag(
    name = "ff_FRESH-123_user_export",
    description = "Enable user data export functionality",
    ticket = "FRESH-123",
    owner = "user-team",
    createdDate = "2025-01-06",
    sunsetDate = "2025-02-06", // PFLICHT: Max 30 Tage!
    defaultValue = false
)
```

#### CI-Gates für Feature Flags:
1. **Naming Convention Check**: Regex-Validation im Build
2. **Age Check**: Flags > 30 Tage → Build Warning
3. **Sunset Enforcement**: Flags > 60 Tage → Build Failure
4. **Usage Analysis**: Unused Flags → Automatic Removal PR

#### Feature Flag Lifecycle:
```yaml
1. Create: ff_TICKET_feature + Sunset Date
2. Test: Gradual Rollout (1% → 10% → 50% → 100%)
3. Monitor: Metrics & Error Rates per Flag State
4. Remove: Automated PR when 100% + 7 days stable
```

#### Anti-Patterns vermeiden:
- ❌ Permanente Feature Flags (werden zu Tech Debt)
- ❌ Verschachtelte Flags (if flag1 && flag2)
- ❌ Business Logic in Flags (nur Ein/Aus)
- ❌ Flags ohne Metriken

## 0.6 Frontend Excellence

### Design System:
- **Storybook** als Living Style Guide
  - Alle Components isoliert entwickeln
  - Visual Regression Tests
  - Auto-Deploy zu Chromatic
- **Accessibility (A11Y)**:
  - `eslint-plugin-jsx-a11y`
  - `axe-core` in CI
  - WCAG 2.1 AA Compliance
- **Component Structure**:
  ```
  components/
  └── Button/
      ├── Button.tsx         # Component
      ├── Button.test.tsx    # Tests
      ├── Button.stories.tsx # Storybook
      ├── Button.module.css  # Styles
      └── index.ts          # Export
  ```

### State Management:
- **React Query** für Server State
- **Zustand** für Client State (wenn nötig)
- **Context** nur für Cross-Cutting Concerns

## 0.7 Infrastructure as Code

### AWS CDK Setup:
```typescript
// infrastructure/cdk/lib/freshplan-stack.ts
export class FreshPlanStack extends Stack {
  constructor(scope: Construct, id: string, props?: StackProps) {
    super(scope, id, props);
    
    // ECS Fargate für Backend
    const backend = new ApplicationLoadBalancedFargateService(...);
    
    // CloudFront für Frontend
    const frontend = new CloudFrontWebDistribution(...);
    
    // RDS PostgreSQL
    const database = new DatabaseInstance(...);
  }
}
```

### Policy as Code:
- **Open Policy Agent** für Security Rules
- **AWS Config Rules** für Compliance
- **Drift Detection** täglich

### Disaster Recovery:
- **RTO**: 4 Stunden
- **RPO**: 1 Stunde
- **Backups**: Automated Snapshots
- **Runbooks**: Dokumentierte Prozesse

## 0.8 Team-Rituale & Workflows

### Development Workflow:
1. **Monday**: Sprint Planning & Backlog Grooming
2. **Daily**: 15-min Standup (blocker-focused)
3. **Wednesday**: Tech Debt Review (1h)
4. **Friday**: Refactoring Slot (2h) + Demos
5. **Retrospective**: Alle 2 Wochen

### Code Review Process:
```yaml
PR-Checklist:
  - [ ] Tests grün + Coverage ≥ 80%
  - [ ] Keine Security Warnings
  - [ ] Performance Budget eingehalten
  - [ ] Dokumentation aktualisiert
  - [ ] Screenshots bei UI-Änderungen
  - [ ] Changelog Entry (wenn public API)
```

### Knowledge Management:
- **ADR Reviews**: Quartalsweise
- **Tech Talks**: Jeden 2. Freitag
- **Pair Programming**: Min. 4h/Woche
- **Documentation Days**: 1x/Monat

### Incident Response:
1. **Detect**: Monitoring Alert
2. **Triage**: Severity 1-4 (siehe Matrix)
3. **Respond**: Runbook befolgen
4. **Resolve**: Fix + Deploy
5. **Review**: Blameless Postmortem

### Incident Severity Matrix:

| Severity | Impact | Beispiele | Response Time | Eskalation |
|----------|---------|-----------|---------------|------------|
| **SEV-1** | Komplettausfall Produktion | - Keine User können sich einloggen<br>- Datenverlust droht<br>- Sicherheitslücke aktiv ausgenutzt | < 15 Min | CTO + On-Call sofort |
| **SEV-2** | Teilausfall / Major Feature | - Zahlungsprozess defekt<br>- Performance < 50%<br>- Keine neuen Aufträge möglich | < 1 Std | Team Lead + On-Call |
| **SEV-3** | Minor Feature / Degradation | - PDF-Export fehlt<br>- Einzelne API langsam<br>- UI-Glitch in Firefox | < 4 Std | Team in Slack |
| **SEV-4** | Cosmetic / Low Impact | - Typo in UI<br>- Log-Spam<br>- Nicht-kritische Warnings | Next Sprint | Ticket in Backlog |

### Eskalations-Pfade:
```
SEV-1: @on-call → Team Lead → CTO → CEO
SEV-2: @on-call → Team Lead → Engineering Manager
SEV-3: Team Channel → Team Lead
SEV-4: Jira Ticket → Sprint Planning
```

### On-Call Rotation:
- **Primär**: 1 Woche Rotation (Mo-So)
- **Backup**: Immer verfügbar
- **Erreichbarkeit**: Handy + Laptop in 30 Min
- **Kompensation**: 1 Tag Ausgleich pro Woche

## 0.9 Tooling & Automation

### Development Tools:
- **IDE**: IntelliJ IDEA / VS Code mit Extensions
- **API Testing**: Insomnia / Postman
- **DB Client**: DBeaver / TablePlus
- **Git GUI**: GitKraken / SourceTree (optional)

### CI/CD Pipeline:
```yaml
# Stages
1. Lint & Format Check
2. Unit Tests + Coverage
3. Build & Package
4. Integration Tests
5. Security Scans
6. Deploy to Stage
7. E2E Tests
8. Performance Tests
9. Deploy to Production (manual approval)
```

### Automation:
- **Dependabot**: Weekly Updates
- **Renovate**: Grouped Updates
- **Release Please**: Automated Releases
- **Mergify**: Auto-merge bei grünen Checks

Diese Standards stellen sicher, dass FreshPlan 2.0 auf Enterprise-Niveau entwickelt wird - mit der Qualität, die erfahrene Entwickler erwarten und sofort verstehen.

## 0.10 Code-Review-Regel: Gründliche Überprüfung bei jedem bedeutenden Abschnitt

### 🔍 **GOLDENE REGEL: Nach jedem bedeutenden Entwicklungsschritt**

**Bei jedem bedeutenden Abschnitt gilt:**
> "Prüfe noch einmal sehr gründlich den Code auf Einhaltung unserer Programmierregeln und Logik"

**Definition "bedeutender Abschnitt":**
- Abschluss eines Features
- Ende eines Sprints
- Vor jedem Merge in main
- Nach größeren Refactorings
- Bei Architektur-Änderungen
- Nach Integration externer Services

### Prüfkriterien für Code Reviews:

#### 1. **Programmierregeln-Compliance** ✓
- [ ] Zeilenlänge eingehalten (80-100 Zeichen)
- [ ] Naming Conventions befolgt
- [ ] Proper Error Handling implementiert
- [ ] JavaDoc/JSDoc vorhanden
- [ ] DRY-Prinzip beachtet
- [ ] SOLID-Prinzipien eingehalten

#### 2. **Security-Check** 🔒
- [ ] Keine hardcoded Credentials
- [ ] Input Validation vorhanden
- [ ] Keine SQL-Injection-Anfälligkeit
- [ ] XSS-Protection implementiert
- [ ] CORS korrekt konfiguriert

#### 3. **Test-Coverage** 🧪
- [ ] Unit Tests ≥ 80%
- [ ] Integration Tests vorhanden
- [ ] Edge Cases abgedeckt
- [ ] Error Cases getestet
- [ ] Performance Tests (wenn relevant)

#### 4. **Logik-Überprüfung** 🧠
- [ ] Business Logic korrekt implementiert
- [ ] Keine Race Conditions
- [ ] Transaktionsgrenzen richtig gesetzt
- [ ] State Management konsistent
- [ ] Keine Memory Leaks

#### 5. **Performance** ⚡
- [ ] Keine N+1 Queries
- [ ] Lazy Loading wo sinnvoll
- [ ] Caching-Strategie implementiert
- [ ] Bundle Size im Budget
- [ ] Keine blockierenden Operationen

### Review-Prozess:

```bash
# 1. Automatisierte Checks
npm run lint
npm run test:coverage
npm run security:audit

# 2. Manuelle Code-Inspektion
# Verwende die Checkliste oben

# 3. Dokumentiere Findings
# Erstelle REVIEW_REPORT_<datum>.md

# 4. Behebe kritische Issues sofort
# Plane mittelfristige Verbesserungen
```

### Review-Report Template:

```markdown
# Code Review Report - [Feature/Sprint Name]
**Datum:** [YYYY-MM-DD]
**Reviewer:** Claude
**Scope:** [Beschreibung]

## Zusammenfassung
- Kritische Issues: X
- Wichtige Issues: Y
- Verbesserungsvorschläge: Z

## Kritische Findings
1. [Issue mit Code-Beispiel und Fix]

## Compliance-Status
- [ ] Programmierregeln: X%
- [ ] Security: ✓/✗
- [ ] Test Coverage: X%
- [ ] Performance: ✓/✗

## Nächste Schritte
1. ...
```

**Diese Regel ist VERPFLICHTEND und wird bei jedem Sprint-Ende automatisch ausgeführt!**

### 🔒 Der neue Two-Pass Review: Pflicht & Kür

**VERBESSERTE REGEL (ab 06.07.2025):** Unser Two-Pass-Review trennt maschinelle Hygiene von strategischer Qualität.

#### Two-Pass Review Prozess:

**Pass 1: Die "Pflicht" – Automatische Code-Hygiene**
- **Was:** Reine Code-Formatierung (Einrückungen, Leerzeichen, Imports)
- **Wer:** Claude führt automatisch `./mvnw spotless:apply` aus
- **Aufwand:** NULL für das Team - vollautomatisch!
- **Ergebnis:** Separater Commit für Formatierung

**Pass 2: Die "Kür" – Strategische Code-Qualität**
- **Was:** Alles, was wirklich zählt:
  - 🏛️ **Architektur:** Folgt der Code unserer Vision?
  - 🧠 **Logik:** Tut es was es soll laut Master Plan?
  - 📖 **Wartbarkeit:** Versteht es ein neuer Entwickler?
  - 💡 **Philosophie:** Lebt es unsere Prinzipien?
- **Wer:** Claude analysiert, Jörg entscheidet bei strategischen Fragen
- **Fokus:** Das, was Software wirklich gut macht

#### Der neue Ablauf:

```bash
# 1. Claude führt Pass 1 aus (automatisch)
cd backend && ./mvnw spotless:apply

# 2. Bei Änderungen: Separater Commit
git add -u && git commit -m "chore: apply Spotless formatting"

# 3. Pass 2: Strategische Review (siehe Template)
```

#### Review-Report Template (nur für Pass 2):

```markdown
# Strategic Code Review - [Feature]

## 🏛️ Architektur-Check
- [ ] Schichtenarchitektur eingehalten?
- [ ] Findings: ...

## 🧠 Logik-Check  
- [ ] Master Plan umgesetzt?
- [ ] Findings: ...

## 📖 Wartbarkeit
- [ ] Selbsterklärende Namen?
- [ ] Findings: ...

## 💡 Philosophie
- [ ] Unsere Prinzipien gelebt?
- [ ] Findings: ...

## 🎯 Strategische Fragen für Jörg
1. [Frage mit Kontext]
```

**Die Vorteile:**
- ✅ Keine Formatierungs-Diskussionen mehr
- ✅ Fokus auf wichtige Dinge
- ✅ Konsistenter Code automatisch
- ✅ Bessere Software durch strategischen Fokus

**Details:** Siehe Master Plan V5

## 1. Projekt-Kontext

**Zweck:** Diese Datei definiert nur ARBEITSREGELN für Claude
**Alles andere:** Siehe Master Plan V5

## 2. Kommunikation und Vorgehensweise

1.  **Sprache:** Deutsch (IMMER - auch bei komprimierten Antworten oder Status-Updates).
2.  **Proaktivität:** Fasse dein Verständnis zusammen und frage nach, bevor du codest. Bei Unklarheiten oder Alternativen, stelle diese zur Diskussion.
3.  **Inkrementell Arbeiten:** Implementiere in kleinen, nachvollziehbaren Schritten. Teste häufig.
4.  **Fokus:** Konzentriere dich auf die aktuelle Aufgabe. Vermeide Scope Creep.
5.  **Claude-Protokoll:** Führe ein Markdown-Protokoll über deine Schritte, Entscheidungen und Testergebnisse für die aktuelle Aufgabe.
6.  **Gründlichkeit:** Führe IMMER umfassende Tests durch:
    - Unit-Tests für alle neuen Funktionen
    - Integration-Tests für Modul-Interaktionen
    - Manuelle Tests in verschiedenen Browsern
    - Performance-Tests bei größeren Änderungen
    - Dokumentiere alle Testergebnisse
7.  **Zusammenfassungen:** Auch bei Status-Updates, Zusammenfassungen oder kurzen Antworten IMMER auf Deutsch antworten. Die Tendenz bei komprimierten Inhalten ins Englische zu verfallen ist ein bekanntes Problem und muss aktiv vermieden werden.

## 3. Wichtige Befehle und Werkzeuge

### Legacy (im `/legacy` Ordner):
* `npm install`: Dependencies installieren
* `npm run dev`: Vite-Dev-Server für Legacy-App
* `npm run build:standalone`: Production-Build der Standalone-Version
* `npm run test`: Vitest Unit- und Integrationstests

### Frontend (React - im `/frontend` Ordner):
* `npm install`: Dependencies installieren
* `npm run dev`: Vite-Dev-Server für React-App
* `npm run build`: Production-Build
* `npm run test`: Vitest + React Testing Library
* `npm run lint`: ESLint mit React-Rules

### Backend (Quarkus - im `/backend` Ordner):
* `./mvnw quarkus:dev`: Development Mode mit Hot-Reload
* `./mvnw test`: Unit-Tests
* `./mvnw package`: Build JAR
* `./mvnw package -Pnative`: Native Build (GraalVM)

### Git-Konventionen:
* Conventional Commits: `feat:`, `fix:`, `chore:`, `docs:`
* Branch-Naming: `feature/`, `bugfix/`, `hotfix/`
* PR vor Merge, mindestens 1 Review

## 4. Repository-Struktur (Kurzübersicht)

```
/frontend    # React SPA
/backend     # Quarkus API
/docs        # Dokumentation → /docs/planung/ für neue Struktur
```

**Details:** Siehe Master Plan V5

## 5. Problemlösung

**Alle Lösungen:** Siehe Master Plan V5

## 6. Test-Standards und Qualitätssicherung

**WICHTIG: Keine Implementierung ohne ausreichende Tests!**

### Minimale Test-Anforderungen:
1. **Unit-Tests**: Mindestens 80% Coverage für neue Module
2. **Integration-Tests**: Alle Modul-Interaktionen müssen getestet werden
3. **Browser-Tests**: Chrome, Firefox, Safari (mindestens)
4. **Performance-Tests**: Bei kritischen Komponenten
5. **Manuelle Tests**: Vollständige User-Flows durchspielen

### Test-Dokumentation:
- Erstelle immer einen Test-Report
- Dokumentiere gefundene Probleme
- Notiere Edge-Cases und Limitierungen

## 7. Arbeitsprozess

### 🔄 Bei jeder Session:
1. **NEXT_STEP.md lesen** - Was ist zu tun?
2. **Master Plan V5 checken** - Wo stehen wir?
3. **TodoWrite nutzen** - Tasks tracken
4. **Bei Problemen:** Debug Cookbook konsultieren
5. **Bei Übergabe:** Trigger Texts verwenden

## 9. Implementierungs-Prinzipien

**Denke bei jeder Entwicklung an:**
- Wartbarkeit vor Cleverness
- Tests vor Implementation
- Dokumentation parallel zum Code
- Kleine, inkrementelle Schritte

## 8. Tech-Standards (Kurzreferenz)

**Frontend:** React + TypeScript + Vite
**Backend:** Quarkus + PostgreSQL
**Auth:** Keycloak OIDC
**Testing:** ≥80% Coverage

**⚠️ Besonderheiten:**
- TypeScript: `import type` für alle Types bei Vite
- Git: `./scripts/quick-cleanup.sh` vor jedem Push
- Zeilenlänge: 80-100 Zeichen max

## 10. Dokumentations-Workflow

### 🗂️ Neue Struktur (ab 18.09.2025):
```
docs/planung/
├── features-neu/           # Technical Concepts nach Sidebar
├── claude-work/daily-work/ # Tägliche Dokumentation
├── grundlagen/            # Foundation-Dokumente (z.B. CODING_STANDARDS.md)
└── archiv/               # Historische Dokumente
```

### 📋 Übergabe-Workflow:
1. **Trigger Texts nutzen:** `/docs/TRIGGER_TEXTS.md` für vollständige Übergaben
2. **Master Plan V5 Update:** Automatisch in Übergabe-Prozess integriert
3. **Template-Validierung:** Alle Sections müssen ausgefüllt werden
4. **Smart Commits:** Explizite JA/NEIN/ÄNDERN Bestätigung

### 🔒 Kritische Dokumente (NIEMALS löschen):
- `/docs/TRIGGER_TEXTS.md` - Offizielle Übergabe-Templates
- `/docs/CLAUDE.md` - Diese Meta-Prompt-Anweisungen
- `/docs/planung/CRM_COMPLETE_MASTER_PLAN_V5.md` - Aktueller Projektstand
- `/docs/NEXT_STEP.md` - Nächste konkrete Schritte

### 📝 Namenskonvention:
`YYYY-MM-DD_<KATEGORIE>_<beschreibung>.md`

**Kategorien:** `IMPL_`, `FIX_`, `REVIEW_`, `CLEANUP_`, `PROPOSAL_`

---

## 🚨 PROBLEMLÖSUNGEN FÜR CLAUDE

### Detaillierte Lösungen {#problemloesungen}

Die häufigsten Probleme und ihre Lösungen findest du im:
**Alle Problem-Lösungen sind im Master Plan V5 verlinkt!**

<!-- Keine Links mehr - alles im Master Plan V5! -->