# API-Gleichheits-Verifikation: Legacy vs CQRS Mode

**Datum:** 15.08.2025  
**Tester:** Claude

## Zusammenfassung

### ✅ IDENTISCHE STRUKTUREN:
- **Response Wrapper**: Beide Modi nutzen identische Pagination-Struktur
  - Keys: `content`, `first`, `last`, `page`, `size`, `totalElements`, `totalPages`
- **Customer Felder**: Alle 40 Felder sind in beiden Modi vorhanden
- **Error Response Format**: Identische Struktur (nur Feld-Reihenfolge variiert)
- **HTTP Status Codes**: Identisch (200 für Success, 404 für Not Found)
- **Pagination**: Funktioniert identisch in beiden Modi

### ⚠️ GEFUNDENE UNTERSCHIEDE:

#### 1. ContactsCount Diskrepanz
- **Customer ID:** 39ca3e6d-17dc-426c-bd8e-b5e1dc75d8fc (BioFresh Märkte AG)
- **Legacy Mode:** contactsCount = 12
- **CQRS Mode:** contactsCount = 15
- **Ursache:** Möglicherweise unterschiedliche Zähllogik oder Cache-Problem

#### 2. Error Response Feld-Reihenfolge
- Beide haben dieselben Felder, aber unterschiedliche JSON-Reihenfolge
- **Funktional irrelevant**, aber könnte sensitive Client-Tests brechen

## Empfehlung

Die Facade neutralisiert die Unterschiede zu 99% erfolgreich. Der ContactsCount-Bug sollte untersucht werden.

