# 📋 Task-Generierung basierend auf Zeitfeldern

**Sprint:** 2  
**Feature:** Automatische Aufgabenerstellung  
**Status:** 🆕 Konzept  

---

## 🎯 Ziel

Automatische Generierung von Vertriebsaufgaben basierend auf:
- Vertragsende
- Entscheidungszeitpunkt
- Wechselbereitschaft

---

## 📊 Task-Generierungs-Matrix

### 1. Vertragsende-basierte Tasks

| Vertragsende | Generierte Aufgabe | Fälligkeit |
|--------------|-------------------|------------|
| In den nächsten 3 Monaten | "Vertrag läuft aus - Angebot vorbereiten" | 1 Monat vorher |
| In 3-6 Monaten | "Kündigungsfrist prüfen" | 2 Monate vorher |
| In 6-12 Monaten | "Quartalskontakt pflegen" | Alle 3 Monate |
| Über 12 Monate | "Jahresgespräch vereinbaren" | 1x jährlich |
| Unbefristet | "Zufriedenheit prüfen" | Alle 6 Monate |

### 2. Entscheidungszeitpunkt-basierte Tasks

| Entscheidung | Generierte Aufgabe | Fälligkeit |
|--------------|-------------------|------------|
| Sofort möglich | "🔥 Heißer Lead - Sofort kontaktieren!" | Heute |
| Innerhalb 1 Monat | "Angebot nachfassen" | In 1 Woche |
| In 1-3 Monaten | "Entscheidungstermin vereinbaren" | In 2 Wochen |
| In 3-6 Monaten | "Status-Update einholen" | Monatlich |
| In 6-12 Monaten | "Im Radar behalten" | Quartalsweise |
| Noch unklar | "Zeitplan klären" | In 2 Wochen |

### 3. Kombinierte Logik

```typescript
const generateTasks = (customer: Customer): Task[] => {
  const tasks: Task[] = [];
  
  // Hot Lead Detection
  if (customer.switchWillingness === 'immediate' || 
      customer.decisionTimeline === 'immediate') {
    tasks.push({
      title: `🔥 Heißer Lead: ${customer.companyName} sofort kontaktieren!`,
      priority: 'high',
      dueDate: 'today',
      type: 'follow_up'
    });
  }
  
  // Contract End Warning
  if (customer.contractEndDate === 'next_3_months') {
    tasks.push({
      title: `⏰ Vertrag läuft aus bei ${customer.companyName}`,
      priority: 'high',
      dueDate: addMonths(new Date(), 2),
      type: 'contract_renewal'
    });
  }
  
  // Decision Timeline Follow-up
  if (customer.decisionTimeline === '3_months') {
    tasks.push({
      title: `Entscheidung steht an: ${customer.companyName}`,
      priority: 'medium',
      dueDate: addWeeks(new Date(), 2),
      type: 'decision_support'
    });
  }
  
  return tasks;
};
```

---

## 🎨 UI-Verbesserungen

### Dropdown-Design Best Practices:

1. **Relative Zeiträume statt feste Daten**
   - ✅ "In 3-6 Monaten"  
   - ❌ "Q1 2025"

2. **Klare Handlungsimpulse**
   - Labels die Aktion implizieren
   - Helptext erklärt Task-Generierung

3. **Visuelle Priorisierung**
   ```css
   .high-priority { background: #fee; }  /* Rot für dringend */
   .medium-priority { background: #ffe; }  /* Gelb für bald */
   .low-priority { background: #eff; }  /* Blau für später */
   ```

4. **Smart Defaults**
   - Vertragsende: "Unbefristet" (häufigster Fall)
   - Entscheidung: "Noch unklar" (realistisch)

---

## 💡 Vorteile des Ansatzes

1. **Zukunftssicher**: Keine festen Jahreszahlen
2. **Aufgabenorientiert**: Jede Auswahl triggert sinnvolle Tasks
3. **Flexibel**: Zeiträume passen immer
4. **Intuitiv**: Verkäufer denken in Zeiträumen, nicht Daten

---

## 🔗 Integration

- Backend speichert Enum-Werte
- Task-Service berechnet konkrete Daten
- Cron-Job generiert Tasks täglich
- Notifications bei neuen Tasks

---

**Nächste Schritte:**
1. Backend Task-Service implementieren
2. Task-Generation Rules Engine
3. UI für Task-Übersicht