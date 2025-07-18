# FC-004: Verkäuferschutz - Provisions-Sicherung ⚡

**Feature Code:** FC-004  
**Feature-Typ:** 🔧 BACKEND  
**Geschätzter Aufwand:** 3-4 Tage  
**Priorität:** KRITISCH - Kernmotivation Verkäufer  
**ROI:** Konfliktreduktion 95%, Motivation ++  

---

## 🎯 PROBLEM & LÖSUNG IN 30 SEKUNDEN

**Problem:** Verkäufer-Konflikte um Kunden = verlorene Provision  
**Lösung:** Klare Schutz-Stufen mit automatischer Eskalation  
**Impact:** Faire Provisions-Verteilung, dokumentierte Historie  

---

## 🛡️ SCHUTZ-STUFEN SYSTEM

```
OPEN → FIRST_CONTACT (7d) → IN_NEGOTIATION (14d) → OFFER_CREATED (30d) → DEAL_WON (∞)
```

**Provisions-Split bei Teamarbeit:**
- Erstkontakt: 30%
- Qualifizierung: 20%
- Angebot: 30%
- Abschluss: 20%

---

## 🏃 QUICK-START IMPLEMENTIERUNG

### Domain Model
```java
@Entity
public class CustomerProtection {
    @Id UUID id;
    @ManyToOne Customer customer;
    @ManyToOne User salesRep;
    @Enumerated ProtectionLevel level;
    LocalDateTime validUntil;
    
    public boolean isValid() {
        return validUntil.isAfter(LocalDateTime.now());
    }
}
```

### Protection Service
```java
@ApplicationScoped
public class CustomerProtectionService {
    public ProtectionStatus checkProtection(UUID customerId, UUID userId) {
        // OPEN | OWNED | BLOCKED_BY_OTHER
    }
    
    @Transactional
    public void escalateProtection(UUID customerId, ProtectionLevel newLevel) {
        // Validierung → Update → Event
    }
}
```

### Commission Calculator
```java
public Commission calculateCommission(Payment payment, User salesRep) {
    var rule = getActiveRule(salesRep);
    var baseCommission = payment.getNetAmount() * rule.getPercentageRate();
    
    // + Neukunden-Bonus
    // - Monatliches Maximum
    // = Final Commission
}
```

---

## 🔗 DEPENDENCIES & INTEGRATION

**Abhängig von:**
- M5 Customer (Protection als Aggregate)
- M4 Activities (triggern Schutz-Eskalation)
- Event System (Echtzeit-Updates)

**Kritische Integration:**
- Xentral API (Zahlungsdaten)
- Timeline (Schutz-Historie)

**Ermöglicht:**
- FC-003 E-Mail (dokumentiert Kontakt)
- FC-007 Chef-Dashboard (Provisions-Übersicht)

---

## ⚡ KRITISCHE ENTSCHEIDUNGEN

1. **Override-Rechte:** Nur Chef oder auch Team-Lead?
2. **Provisions-Split:** Fix oder konfigurierbar?
3. **Schutz-Dauer:** Anpassbar pro Kunde-Typ?

---

## 📊 SUCCESS METRICS

- **Konfliktrate:** <5% der Zuordnungen
- **Fairness:** 95% Verkäufer-Zustimmung
- **Transparenz:** 100% nachvollziehbar
- **Performance:** Schutz-Check <50ms

---

## 🚀 NÄCHSTER SCHRITT

1. Rechtliche Prüfung der Provisions-Regeln
2. CustomerProtection Entity implementieren
3. Event-System für Schutz-Updates

**Detaillierte Implementierung:** [IMPLEMENTATION_GUIDE.md](./IMPLEMENTATION_GUIDE.md)  
**Original-Planung:** [FC-004-verkaeuferschutz.md](../../FC-004-verkaeuferschutz.md)