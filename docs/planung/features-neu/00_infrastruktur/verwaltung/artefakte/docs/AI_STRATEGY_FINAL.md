---
Title: AI Cost & Routing Strategy (Final)
Purpose: Kostenkontrollierte, zuverlässige AI-Nutzung mit maximalem Business-Nutzen.
Audience: Engineering, Product, Finance
Last Updated: 2025-09-20
Status: Final
---

# 🤖 AI Cost & Routing Strategy (Final)

## 30-Second Summary
- Budget Core: €600–€1.200/Monat (Plan: €1.000).
- Confidence Routing: Small-First (0.7), Large bei komplexem Reasoning.
- Guardrails: Budget-Gates, Cache, Capped Prompts, Provider-Fallbacks.

## Use-Case Routing

### Small (schnell/günstig)
- Lead ABC-Klassifikation, E-Mail-Kategorisierung, Help-Drafts, Contact-Role-Detection.

### Large (High Reasoning)
- ROI-Empfehlungen, Multi-Contact-Strategie, Sample-Produkt-Matching, Seasonal-Optimierung.

## Guardrails
- **Budget Caps:** je Org (ai.budget.monthly.cap)
- **Confidence Threshold:** pro User/Org
- **Cache TTL:** (ai.cache.ttl)
- **Demotion Cooldown:** (ai.routing.demotion.cooldown)
- **Provider-Fallback:** OpenAI ↔ Anthropic bei Störung

## Decision Tree (vereinfacht)
```yaml
if useCase.requiresLarge → Large
else:
  call Small →
    if confidence ≥ threshold → accept
    else → fallback Large (Budget prüfen)
```

## KPIs
- Cost/Lead, Cost/Order, ai_calls_small/large, ai_cost_eur_day, cache_hit_rate.

## Integration Points
- **Settings:** [SETTINGS_REGISTRY_COMPLETE.md](./SETTINGS_REGISTRY_COMPLETE.md) (ai.*)
- **Ops:** [OPERATIONS_RUNBOOK.md](../../betrieb/artefakte/OPERATIONS_RUNBOOK.md)
- **Events:** [EVENT_CATALOG.md](../../integration/artefakte/EVENT_CATALOG.md) (ai.usage.* optional)

## Troubleshooting

### Kosten steigen
Threshold anheben, Large-Usecases prüfen, Caching aggressiver, Batching.