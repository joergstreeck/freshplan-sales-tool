# 📐 Standards & Governance

**📊 Status:** 🔄 In Progress - Mock-Governance Implementation
**🎯 Owner:** Development Team
**⏱️ Timeline:** Sprint 1.1 (Mock-Governance) → Sprint 1.2 (API Foundation)

## 🎯 Überblick

Standards und Governance-Regeln für einheitliche, wartbare Codebase:

### **03_MOCK_GOVERNANCE.md**
- **Ziel:** Business-Logic mock-frei, Mocks nur in Tests/Stories
- **Status:** ✅ Ready for Implementation
- **Sprint:** S1.1 (ESLint/CI Setup) + S1.2 (Frontend Mock-Elimination)

## 🗂️ Implementierungs-Artefakte

### **Production-Ready:**
- `03_MOCK_GOVERNANCE.md` - Zentrale Mock-Governance Dokumentation
- `snippets/` - Code-Snippets für ESLint/CI/Husky Setup

### **Related:**
- [ADR-0006](../../adr/ADR-0006-mock-governance.md) - Entscheidungsgrundlage
- [Mock Infrastructure Analysis](../betrieb/analyse/02_MOCK_INFRASTRUCTURE_ANALYSIS.md) - Detailanalyse

## 🚀 Quick Start

1. **Lese:** `03_MOCK_GOVERNANCE.md` für vollständige Umsetzung
2. **Implementiere:** ESLint-Regel + CI-Guard aus Snippets
3. **Setup:** Dev-Seeds für mock-freie Development-Experience
4. **Validate:** PR-Checks brechen bei Mock-Imports in Business-Logic

## 🔗 Navigation

- **⬆️ Zurück:** [Infrastruktur Overview](../README.md)
- **➡️ Implementation:** [Mock Governance](03_MOCK_GOVERNANCE.md)
- **📋 Planung:** [TRIGGER_SPRINT_1_1.md](../../TRIGGER_SPRINT_1_1.md)