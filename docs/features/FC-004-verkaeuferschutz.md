# FC-004: Verkäuferschutz-System (Provisions-Sicherung)

**Feature Code:** FC-004  
**Status:** 📋 Planungsphase  
**Geschätzter Aufwand:** 3-4 Tage (komplex wegen Provisions-Logik)  
**Priorität:** KRITISCH - Kernmotivation der Verkäufer  
**Phase:** 4.1 (Team-Funktionen)  

## 🎯 Zusammenfassung

**Verkäufer arbeiten auf Provision!** Ihre Arbeit muss geschützt werden, sonst verlieren Sie Motivation und Geld. Das Verkäuferschutz-System sichert faire Provisionszuteilung basierend auf nachvollziehbaren Regeln.

## 🛡️ Schutz-Stufen-Konzept

```typescript
enum ProtectionLevel {
  OPEN = "Offen",                    // Jeder kann kontaktieren
  FIRST_CONTACT = "Erstkontakt",     // 7 Tage Schutz
  IN_NEGOTIATION = "In Verhandlung", // 14 Tage Schutz  
  OFFER_CREATED = "Angebot erstellt",// 30 Tage Schutz
  DEAL_WON = "Auftrag gewonnen"      // Dauerhaft zugeordnet
}
```

## 🏗️ Technische Architektur

### 1. Domain Model (Backend)

```java
@Entity
public class CustomerProtection {
    @Id UUID id;
    @ManyToOne Customer customer;
    @ManyToOne User salesRep;
    @Enumerated ProtectionLevel level;
    LocalDateTime validUntil;
    LocalDateTime createdAt;
    String reason;
    
    // Business Logic
    public boolean isValid() {
        return validUntil.isAfter(LocalDateTime.now());
    }
    
    public void escalate(ProtectionLevel newLevel) {
        // Validierung und neue Gültigkeit berechnen
    }
}
```

### 2. Protection Service

```java
@ApplicationScoped
public class CustomerProtectionService {
    
    public ProtectionStatus checkProtection(UUID customerId, UUID userId) {
        var protection = repository.findActiveProtection(customerId);
        
        if (protection.isEmpty()) {
            return ProtectionStatus.OPEN;
        }
        
        if (protection.get().getSalesRep().equals(userId)) {
            return ProtectionStatus.OWNED;
        }
        
        return ProtectionStatus.BLOCKED_BY_OTHER;
    }
    
    @Transactional
    @RequiresPermission("customer.claim") // FC-015 Integration
    public void claimCustomer(UUID customerId, UUID userId, String reason) {
        // FC-015: Permission Check für Customer-Claim
        if (!permissionService.hasPermission("customer.claim")) {
            throw new ForbiddenException("Keine Berechtigung zum Claim von Kunden");
        }
        
        // Prüfungen und Event publizieren
        eventBus.publish(new CustomerClaimedEvent(customerId, userId));
    }
}
```

### 3. Provisions-Berechnung

```java
@Entity
public class CommissionRule {
    @Id UUID id;
    @ManyToOne User salesRep;
    BigDecimal percentageRate;      // z.B. 3.0%
    BigDecimal newCustomerBonus;    // z.B. 500€
    BigDecimal monthlyMax;          // z.B. 5000€
    LocalDate validFrom;
}

@ApplicationScoped
public class CommissionCalculator {
    
    public Commission calculateCommission(Payment payment, User salesRep) {
        var rule = getActiveRule(salesRep);
        var baseCommission = payment.getNetAmount()
            .multiply(rule.getPercentageRate());
            
        // Sonderregeln anwenden
        if (payment.isNewCustomer() && rule.hasNewCustomerBonus()) {
            baseCommission = baseCommission.add(rule.getNewCustomerBonus());
        }
        
        // Monatliches Maximum prüfen
        var monthlyTotal = getMonthlyTotal(salesRep);
        if (monthlyTotal.add(baseCommission).compareTo(rule.getMonthlyMax()) > 0) {
            baseCommission = rule.getMonthlyMax().subtract(monthlyTotal);
        }
        
        return new Commission(salesRep, payment, baseCommission);
    }
}
```

### 4. Frontend-Integration

**Betroffene Module:**
- **M5 Kundenmanagement**: Schutz-Status-Anzeige
- **M3 Cockpit**: Warnungen bei geschützten Kunden
- **M2 FocusList**: Filterung nach Schutz-Status

**UI-Komponenten:**
```typescript
// ProtectionBadge.tsx
interface ProtectionBadgeProps {
  customerId: string;
  currentUserId: string;
}

export const ProtectionBadge: React.FC<ProtectionBadgeProps> = ({
  customerId,
  currentUserId
}) => {
  const { data: protection } = useCustomerProtection(customerId);
  
  if (!protection) return null;
  
  if (protection.salesRepId === currentUserId) {
    return (
      <Chip 
        label={`${protection.level} - 100% Provision`}
        color="success"
        icon={<ShieldIcon />}
      />
    );
  }
  
  return (
    <Chip 
      label={`Geschützt durch ${protection.salesRepName}`}
      color="warning"
      icon={<WarningIcon />}
    />
  );
};
```

## 🔗 Abhängigkeiten

### Zu bestehenden Modulen:
- **Customer (M5)**: Protection als Customer-Aggregate
- **Activities (M4)**: Aktivitäten beeinflussen Schutz-Level
- **Timeline**: Schutz-Historie dokumentieren

### Zu neuen Features:
- **FC-003 (E-Mail)**: E-Mails dokumentieren Kundenkontakt
- **FC-005 (Xentral)**: Provisions-Berechnung bei Zahlung
- **FC-007 (Chef-Dashboard)**: Provisions-Übersicht

### Kritische Integration:
- **Xentral-API**: Zahlungsdaten für Provisionsberechnung
- **Event-System**: Echtzeit-Updates bei Schutz-Änderungen

## 🏛️ Architektur-Entscheidungen

### ADR-004-001: Provisions-Splitting
**Entscheidung:** Bei Teamarbeit wird Provision aufgeteilt:
- Erstkontakt: 30%
- Qualifizierung: 20%
- Angebotserstellung: 30%
- Abschluss: 20%

### ADR-004-002: Konfliktlösung
**Entscheidung:** Chef-Override nur mit Begründung
- Alle Overrides werden dokumentiert
- Provisions-Ausgleich bei ungerechtfertigtem Override

## 🚀 Implementierungsphasen

### Phase 1: Domain Model (1 Tag)
1. CustomerProtection Entity
2. CommissionRule Entity
3. Protection Service Basis
4. Event Definitionen

### Phase 2: Business Logic (1 Tag)
1. Schutz-Level-Verwaltung
2. Gültigkeits-Berechnung
3. Provisions-Kalkulation
4. Konflikt-Auflösung

### Phase 3: API & Integration (1 Tag)
1. REST-Endpoints
2. Event-Handler
3. Xentral-Integration vorbereiten
4. Timeline-Integration

### Phase 4: Frontend (1 Tag)
1. Protection Badge Component
2. Warn-Dialoge
3. Provisions-Anzeige
4. Chef-Override UI

## 🧪 Test-Szenarien

```java
@Test
void shouldEscalateProtectionLevel() {
    // Given: Kunde mit Erstkontakt-Schutz
    var protection = createProtection(FIRST_CONTACT, salesRep1);
    
    // When: Angebot erstellt
    protectionService.escalateProtection(customerId, OFFER_CREATED);
    
    // Then: 30 Tage Schutz
    assertThat(protection.getValidUntil())
        .isAfter(LocalDateTime.now().plusDays(29));
}

@Test
void shouldCalculateSplitCommission() {
    // Given: Mehrere Verkäufer beteiligt
    var contributors = List.of(
        new Contributor(salesRep1, FIRST_CONTACT),
        new Contributor(salesRep2, OFFER_CREATED)
    );
    
    // When: Zahlung eingeht
    var commissions = calculator.calculateSplit(payment, contributors);
    
    // Then: Korrekte Aufteilung
    assertThat(commissions.get(salesRep1)).isEqualTo(payment.getNet() * 0.3);
    assertThat(commissions.get(salesRep2)).isEqualTo(payment.getNet() * 0.3);
}
```

## 📊 Erfolgsmetriken

- Konfliktrate: <5% der Kunden-Zuordnungen
- Fairness-Index: 95% Zustimmung der Verkäufer
- Provisions-Transparenz: 100% nachvollziehbar

## 🔍 Offene Fragen

1. **Rechtliche Prüfung**: Provisions-Regeln arbeitsrechtlich OK?
2. **Sonderfälle**: Großkunden-Regelungen?
3. **Historie**: Wie lange Schutz-Historie aufbewahren?
4. **Eskalation**: Konflikt-Eskalations-Prozess definieren