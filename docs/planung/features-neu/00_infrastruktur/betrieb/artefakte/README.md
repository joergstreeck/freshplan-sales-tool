# FreshFoodz Operations Pack (CQRS Light Optimized)

**Last Updated:** 2025-09-21
**Architecture:** CQRS Light für 5-50 interne Benutzer

Dieses Paket liefert **copy‑paste‑fertige** Artefakte für:
1) **User‑Lead‑Protection** (State Machine mit PostgreSQL LISTEN/NOTIFY)
2) **Simple Monitoring** (Basic Monitoring für interne Tools, keine War-Rooms)
3) **CQRS Light Operations** (One-Database-Architecture, <200ms P95)

Sie sind **optimiert für CQRS Light** und berücksichtigen:
- **One-Database-Architecture** mit PostgreSQL LISTEN/NOTIFY
- **5-50 interne Benutzer** statt 1000+ Enterprise-Scale
- **Cost-Efficient Monitoring** ohne Over-Engineering
- **<200ms P95 Performance** durch CQRS Light Query-Services
- **Kein Event-Bus** - LISTEN/NOTIFY ist ausreichend

Siehe Verzeichnisse: `docs/runbooks`, `docs/playbooks`, `sql`, `monitoring`, `testing`.
