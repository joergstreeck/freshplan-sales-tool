# Arbeitsrichtlinien für Claude im FreshPlan Sales Tool Projekt

## 0. Grundlegende Arbeitsphilosophie

**🎯 UNSERE DEVISE: GRÜNDLICHKEIT GEHT VOR SCHNELLIGKEIT**

- Jede Implementierung muss gründlich getestet werden
- Keine Quick-Fixes oder Workarounds ohne Dokumentation
- Denke immer an die zukünftigen Integrationen und Erweiterungen
- Was wir jetzt richtig machen, erspart uns später Arbeit
- Siehe `VISION_AND_ROADMAP.md` für die langfristige Ausrichtung des Projekts

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

## 1. Projektübersicht und Ziele

**Projektname:** FreshPlan Sales Tool 2.0
**Hauptziel:** Migration zu einer cloud-nativen Enterprise-Lösung mit React + Quarkus + Keycloak + PostgreSQL auf AWS.
**Aktuelle Phase:** Sprint 0 - Walking Skeleton (Monorepo Setup, Auth-Integration, erste API)
**Stack-Entscheidung:** 
- Frontend: React + TypeScript + Vite
- Backend: Quarkus (Java)
- Auth: Keycloak
- DB: PostgreSQL
- Cloud: AWS (ECS, RDS, S3, CloudFront)

## 2. Kommunikation und Vorgehensweise

1.  **Sprache:** Deutsch.
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

## 4. Architektur und Code-Struktur (Monorepo)

### Neue Struktur ab Sprint 0:
```
freshplan-sales-tool/
├── /legacy              # Alter Code (eingefroren als legacy-1.0.0)
├── /frontend            # React SPA
│   ├── src/
│   │   ├── components/
│   │   ├── contexts/    # AuthContext, etc.
│   │   ├── hooks/
│   │   ├── pages/
│   │   └── services/
│   └── package.json
├── /backend             # Quarkus API
│   ├── src/main/java/
│   ├── src/main/resources/
│   └── pom.xml
├── /infrastructure      # Docker, K8s, AWS CDK
├── /docs               # ADRs, API-Docs
└── /.github/workflows  # CI/CD Pipelines
```

### Tech-Stack:
* **Frontend:** React 18 + TypeScript + Vite + MUI + React Query
* **Backend:** Quarkus + RESTEasy Reactive + Hibernate ORM + Flyway
* **Auth:** Keycloak mit OIDC
* **Database:** PostgreSQL mit Row-Level Security
* **Testing:** Vitest (Unit), Playwright (E2E), RestAssured (API)
* **CI/CD:** GitHub Actions + SonarCloud + AWS

## 5. Bekannte Probleme (Known Issues) und Workarounds

* Siehe `KNOWN_ISSUES.md` für eine aktuelle Liste.
* **Übersetzung dynamischer Tabs:** Ein bekanntes Problem. Workaround wird in Phase 2 gesucht.
* **Performance bei großen Datenmengen:** Bei der Verarbeitung sehr vieler Positionen im Calculator kann es zu Verzögerungen kommen. Optimierungen sind für spätere Phasen geplant.

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

## 7. Aktueller Sprint: Sprint 0 - Walking Skeleton

### Heutige Ziele (Tag 1):
1. **09:00-09:05:** Legacy einfrieren (Tag `legacy-1.0.0`)
2. **09:05-09:30:** Monorepo-Struktur anlegen
3. **09:30-12:30:** Skeleton-PRs (Frontend + Backend)
4. **12:30-15:00:** CI/CD-Pipeline
5. **15:00-Ende:** Walking Skeleton verbinden (React → /api/ping → DB)

### Definition of Done für Sprint 0:
- [ ] User kann sich via Keycloak einloggen
- [ ] Geschützte Route `/calculator` nur mit Auth erreichbar
- [ ] API-Call `/api/ping` liefert DB-Timestamp
- [ ] E2E-Test läuft grün in GitHub Actions
- [ ] Alle Teammitglieder können lokal entwickeln

### Nächste Sprints (Preview):
- **Sprint 1:** Erste Features migrieren (Calculator, Customer-Liste)
- **Sprint 2:** API-Integration, Repository-Pattern
- **Sprint 3:** Vollständige Feature-Parität mit Legacy

## 8. Zukunftsorientierung

**Denke bei jeder Implementierung an:**
- Skalierbarkeit für große Datenmengen
- Erweiterbarkeit für neue Features
- Integration mit externen Systemen (Monday.com, Klenty, etc.)
- Wartbarkeit des Codes
- Performance-Optimierung

Siehe `VISION_AND_ROADMAP.md` für Details zu geplanten Integrationen und Features.