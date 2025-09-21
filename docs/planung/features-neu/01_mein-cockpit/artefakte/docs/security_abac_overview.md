# ABAC Security – Territory + Channel
- Claims: `territories`: string[], `channels`: ['DIRECT','PARTNER'], optional `chain_id`
- Request-Filter parst JWT (SmallRye) und befüllt ScopeContext
- **Prod:** Header-Fallback deaktivieren (`app.security.allowHeaderScopesInDev=false`)
- Services schneiden Daten per Scope (Intersect requested vs allowed)
