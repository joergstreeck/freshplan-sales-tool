# ADR-004: Frontend Design System Choice

**Status:** ⚠️ VERALTET/ÜBERHOLT
**Date:** 2025-01-07
**Archiviert:** 2025-07-11
**Authors:** Team FRONT (Claude)
**Reviewers:** @ChatGPT (Team ADVISOR)

## Context

FreshPlan 2.0 benötigt eine Design System Entscheidung für das React Frontend. Wir haben:

- Enterprise Sales Tool mit ~10 geplanten Screens
- Kein dedicated UX/UI Designer (vorerst)
- Business Requirements sind klar definiert
- Team BACK hat robustes Quarkus Backend etabliert
- Sprint 1 startet mit User Management + Calculator Migration
- Zielgruppe: Interne Sales Teams + Kunden

**Technischer Kontext:**
- React 18 + TypeScript + Vite
- Test Coverage Ziel: 80%
- Responsive Design erforderlich
- Accessibility (WCAG 2.1 AA) gewünscht

## ⚠️ ARCHIVIERUNG HINWEIS

**Diese Entscheidung wurde REVIDIERT und durch Material-UI (MUI) ersetzt.**

**Neuer Standard ab Juli 2025:** Material-UI (MUI) ist unser verbindliches Design-System
- Siehe: `/docs/design/AKTUELLER_DESIGN_STANDARD.md`
- Grund: Bessere Enterprise-Tauglichkeit und TeamScalability

---

## Decision (VERALTET - NUR ZUR REFERENZ)

**ACCEPTED:** Tailwind CSS 3.x + shadcn/ui + Radix UI primitives

**Advisor Feedback (ChatGPT):** Tailwind gewinnt aufgrund von Speed of Implementation, kleinem Team-Scope, Design-Token-Integration und Future-Proof-Architektur.

## Rationale

### Considered Options

#### 1. **Material-UI (MUI)**
- **Pros:** 
  - Enterprise-ready Components
  - Excellent TypeScript Support
  - Built-in Accessibility
  - Robustes Theming System
  - Large Community
- **Cons:**
  - Bundle Size (größer)
  - Vendor Lock-in
  - Design kann "generic" wirken
  - Learning Curve für Customization

#### 2. **Tailwind CSS**
- **Pros:**
  - Utility-first, sehr flexibel
  - Kleine Bundle Size (purged)
  - Rapid Prototyping
  - Design Tokens integriert
  - Keine Vendor Lock-in
- **Cons:**
  - Accessibility manuell
  - Komponentenbibliothek selbst bauen
  - Mehr Setup-Aufwand
  - Team muss Tailwind lernen

#### 3. **Vanilla CSS + CSS Modules**
- **Pros:**
  - Volle Kontrolle
  - Minimale Dependencies
  - Performance optimal
- **Cons:**
  - Hoher Entwicklungsaufwand
  - Accessibility komplett manuell
  - Konsistenz schwerer zu halten
  - Langsamere Entwicklung

## Consequences

### Für Team FRONT:
- Component Library Entwicklung
- Styling Standards etablieren
- Accessibility Compliance sicherstellen

### Für Team BACK:
- Minimal impact (API bleibt gleich)
- ggf. Response-Struktur für UI-Optimierung

### Für Projekt:
- Time-to-Market Impact
- Maintenance Overhead
- Future Design Flexibility

## Implementation Notes

**Sofortige Umsetzung:**
1. `npm install tailwindcss postcss autoprefixer @radix-ui/react-slot class-variance-authority lucide-react`
2. `npx tailwindcss init -p` + Vite-Konfiguration
3. `npx shadcn-ui@latest init` für Component Setup
4. Base Components: `npx shadcn-ui add button input dialog`
5. Design Tokens in `tailwind.config.js`
6. Accessibility Testing mit `jest-axe`

## References

- [Material-UI Documentation](https://mui.com/)
- [Tailwind CSS Documentation](https://tailwindcss.com/)
- [WCAG 2.1 Guidelines](https://www.w3.org/WAI/WCAG21/quickref/)
- FreshPlan 2.0 Technical Roadmap

---

**@ChatGPT - ADVISOR REQUEST:**

Basierend auf dem Kontext eines Enterprise Sales Tools mit den genannten Constraints:

1. **Welche Option empfiehlst du?**
2. **Begründung mit Focus auf Time-to-Market vs Long-term Maintainability**
3. **Specific Implementation Strategy für die gewählte Option**
4. **Risks & Mitigation Strategy**

**Review Checklist für @advisor:**
- [ ] Cross-cutting concerns berücksichtigt (Theming, Responsive, i18n)
- [ ] Security implications geprüft (CSP, Bundle-Größe)
- [ ] Performance impact bewertet
- [ ] Migration strategy definiert  
- [ ] Team alignment sichergestellt