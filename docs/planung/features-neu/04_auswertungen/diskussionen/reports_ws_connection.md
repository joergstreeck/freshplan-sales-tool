# WebSocket Connection Management (Reports V1)

Ziel: stabile Live-Badges bei minimalem Aufwand. KPIs per Polling (30 s), Events nur als Inkremente/Stale-Marker.

## Client-Verhalten
- **Auth**: JWT im Query (`/ws?topics=reports.*&token=...`) oder per `Authorization` Header (wenn Gateway unterstützt).
- **Heartbeat**: Ping/Pong alle 30 s (Server schließt nach 90 s Inaktivität).
- **Reconnect**: Exponential Backoff (1s, 2s, 4s, 8s, max 30s) mit Jitter ±30%.
- **Buffering**: Client hält max 100 Events im RAM; bei Overflow → `full-refresh` Trigger (Poll).
- **Stale-Indicator**: Wenn letzte `reports.badge.changed` > 5 min her → Badge „Stand vor …“ anzeigen.

## Server-Verhalten
- **Topic-Autorisierung**: Vor dem Subscribe Scopes prüfen (territory/chain).
- **QoS**: At-most-once fürs UI; State kommt via Polling aus DB.
- **Backpressure**: bei > 50 msgs/s / Client → Drosselung und Sammel-Event `reports.compact` senden.

## Fehlerbilder & Fallbacks
- **Auth-Error** → Close mit Code 4401
- **Topic not allowed** → 4403
- **Rate-Limit** → 4429 (Client wechselt auf Polling)
- **Broker down** → keine Events; UI fährt Polling hoch (10 s) bis Recovery
