# 🏗️ Step 3 Architektur-Entscheidung: Pragmatischer Ansatz

**Datum:** 31.07.2025  
**Status:** ✅ Entschieden  
**Entscheidungsträger:** Team & Management  

## 🧭 Navigation

**→ Weiter zu:** [Step 3 Implementation Guide](./step3/README.md)  
**↑ Übergeordnet:** [Sprint 2 Master Plan](./SPRINT2_MASTER_PLAN.md)  
**📘 Context:** [Contact Management Vision](./CONTACT_MANAGEMENT_VISION.md)  

## 🎯 Executive Summary

Für Step 3 (Multi-Contact Support) verwenden wir einen **pragmatischen CRUD-Ansatz** statt Event Sourcing:
- Standard JPA Entities mit Audit-Trail
- PostgreSQL mit Hibernate
- Zustand Store im Frontend
- Focus auf Business Value statt Architektur-Komplexität

## 📊 Entscheidungsgrundlage

### Nutzer-Zahlen:
- **10-15 Verkäufer**
- **5 Verwaltung/Management**
- **65-110 Events/Tag** (realistisch)

### Vergleich: Event Sourcing wäre bei:
- **> 1.000 Events/Tag** sinnvoll
- **> 10.000 Events/Tag** notwendig

**Unsere Last:** < 1% der Event Sourcing Schwelle!

## ✅ Gewählte Architektur

### Backend:
```java
@Entity
@Audited // Hibernate Envers
public class Contact {
    @Id
    @GeneratedValue
    private UUID id;
    
    @ManyToOne
    private Customer customer;
    
    // Audit automatisch via Envers
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
```

### Frontend:
```typescript
// Zustand Store (bereits vorhanden)
interface ContactState {
  contacts: Contact[];
  addContact: (contact: Contact) => void;
  updateContact: (id: string, data: Partial<Contact>) => void;
}
```

## 🚀 Vorteile dieser Entscheidung

1. **Zeit & Budget**
   - 3-4 Wochen schnellere Lieferung
   - Keine Lernkurve für Team

2. **Wartbarkeit**
   - Standard-Patterns
   - Jeder Java-Entwickler versteht es

3. **Performance**
   - PostgreSQL bei < 1% Auslastung
   - Keine komplexen Event-Projektionen

4. **Flexibilität**
   - Migration zu Event Sourcing später möglich
   - Kein Lock-in

## 📋 Konkrete Umsetzung für Step 3

**MUSS:**
- Contact als JPA Entity ✅
- Multi-Contact CRUD ✅
- Location-Zuordnung mit History ✅
- Basis Audit-Trail ✅

**SPÄTER:**
- Event Sourcing ❌
- Complex Conflict Resolution ❌
- AI-Features ❌

## 🔗 Nächste Schritte

1. [Step 3 Implementation Guide](./step3/README.md)
2. [Backend Contact Entity](./step3/BACKEND_CONTACT.md)
3. [Frontend Multi-Contact UI](./step3/FRONTEND_MULTICONTACT.md)

---

**Entscheidung getroffen:** 31.07.2025  
**Nächste Review:** Bei > 500 Events/Tag