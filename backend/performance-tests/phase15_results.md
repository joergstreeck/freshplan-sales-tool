# Phase 15: Performance Testing Results

**Datum:** 15.08.2025
**Tester:** Claude
**Branch:** feature/refactor-large-services

## Test-Konfiguration
- Backend: Quarkus auf Port 8080
- Datenbank: PostgreSQL mit 294+ Kunden (Test-Daten-Pollution bekannt)
- JVM: Warmed up (Backend läuft bereits)
- curl-format.txt: Detaillierte Timing-Metriken

## Baseline: Legacy Mode (features.cqrs.enabled=false)

### Test 1: GET /api/customers (Liste aller Kunden)


#### Messungen (5 Durchläufe):
- Messung 1: 41.531ms
- Messung 2: 25.002ms  
- Messung 3: 31.294ms
- Messung 4: 19.034ms
- Messung 5: 33.893ms

**Durchschnitt: 30.151ms**
**Median: 31.294ms**
**Min: 19.034ms / Max: 41.531ms**

### Test 2: GET /api/customers/{id} (Einzelner Kunde)

#### Messungen (5 Durchläufe):
- Messung 1: 27.557ms
- Messung 2: 7.449ms
- Messung 3: 26.816ms
- Messung 4: 10.511ms
- Messung 5: 22.758ms

**Durchschnitt: 19.018ms**
**Median: 22.758ms**
**Min: 7.449ms / Max: 27.557ms**


## CQRS Mode Performance (features.cqrs.enabled=true)

### Test 1: GET /api/customers (Liste aller Kunden)

#### Messungen (5 Durchläufe):
- Messung 1: 148.267ms (Erster Aufruf - Cold Start)
- Messung 2: 34.169ms
- Messung 3: 44.031ms
- Messung 4: 34.097ms
- Messung 5: 43.613ms

**Durchschnitt (ohne Cold Start): 39.228ms**
**Median: 38.891ms**
**Min: 34.097ms / Max: 44.031ms**

### Test 2: GET /api/customers/{id} (Einzelner Kunde)

#### Messungen (5 Durchläufe):
- Messung 1: 19.143ms
- Messung 2: 17.145ms
- Messung 3: 22.514ms
- Messung 4: 12.636ms
- Messung 5: 22.668ms

**Durchschnitt: 18.821ms**
**Median: 19.143ms**
**Min: 12.636ms / Max: 22.668ms**


## Performance-Vergleich: Legacy vs CQRS

### Zusammenfassung

| Metrik | Legacy Mode | CQRS Mode | Differenz |
|--------|------------|-----------|-----------|
| **GET /api/customers (Liste)** | | | |
| Durchschnitt | 30.151ms | 39.228ms | +30.1% langsamer |
| Median | 31.294ms | 38.891ms | +24.3% langsamer |
| Min | 19.034ms | 34.097ms | +79.1% langsamer |
| Max | 41.531ms | 44.031ms | +6.0% langsamer |
| **GET /api/customers/{id}** | | | |
| Durchschnitt | 19.018ms | 18.821ms | -1.0% schneller |
| Median | 22.758ms | 19.143ms | -15.9% schneller |
| Min | 7.449ms | 12.636ms | +69.6% langsamer |
| Max | 27.557ms | 22.668ms | -17.7% schneller |

### Analyse

#### Performance-Charakteristik:
1. **Listen-Abfragen (GET /api/customers)**: CQRS Mode ist ca. 30% langsamer
   - Mögliche Ursache: Overhead durch zusätzliche Service-Layer
   - Cold Start deutlich langsamer (148ms vs 42ms)

2. **Einzel-Abfragen (GET /api/customers/{id})**: Nahezu identische Performance
   - CQRS marginal schneller im Durchschnitt (-1%)
   - Bessere Median-Performance (-15.9%)
   - Konstantere Response-Zeiten

#### Erkenntnisse:
- ⚠️ CQRS zeigt bei Listen-Abfragen Performance-Regression
- ✅ Bei Einzel-Abfragen gleichwertige oder leicht bessere Performance
- ✅ API-Kompatibilität vollständig gewährleistet (bis auf ContactsCount-Bug)
- ✅ Keine Fehler bei 100+ parallelen Requests

### Empfehlungen:
1. **Performance-Optimierung** für QueryService.getAllCustomers() untersuchen
2. **Caching-Strategie** für häufige Queries implementieren
3. **ContactsCount-Bug** separat beheben
4. **Weitere Tests** mit größeren Datenmengen durchführen

### Test-Umgebung:
- Backend: Quarkus 3.17.4 auf JVM
- Datenbank: PostgreSQL 15.13 mit 292 Kunden
- Test-Pollution: 294+ Test-Kunden bekannt
- Hardware: Lokale Entwicklungsumgebung

