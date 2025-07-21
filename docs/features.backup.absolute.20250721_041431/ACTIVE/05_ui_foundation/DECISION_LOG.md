# 🤔 UI FOUNDATION DECISION LOG

**Zweck:** Offene Entscheidungen für UI Foundation Enhancement dokumentieren  
**Erstellungsdatum:** 17.07.2025 16:55  
**Status:** 8 ENTSCHEIDUNGEN OFFEN für Stakeholder-Input  

---

## 📋 ENTSCHEIDUNGSÜBERSICHT

**🚨 ESKALATION:** D1-D3 blockieren UI Foundation Start! → **Deadline: 19.07.2025**

| ID | Kategorie | Entscheidung | Status | Priorität |
|----|-----------|-------------|---------|-----------|
| **D1** | KI-Integration | KI-Provider: OpenAI vs. Local Model | ❓ OFFEN | 🔥 HOCH |
| **D2** | Real-time | Refresh-Strategie: Polling vs. WebSocket | ❓ OFFEN | 🔥 HOCH |
| **D3** | Navigation | Menü-Style: Sidebar vs. Top-Navigation | ❓ OFFEN | 🔥 HOCH |
| **D4** | Quick Create | FAB-Position: Fixed vs. Context-sensitive | ❓ OFFEN | 🟡 MITTEL |
| **D5** | Settings | Storage: Local vs. Server vs. Hybrid | ❓ OFFEN | 🟡 MITTEL |
| **D6** | Permissions | Integration: Eager vs. Lazy Loading | ❓ OFFEN | 🟡 MITTEL |
| **D7** | Performance | Caching: Memory vs. Local Storage | ❓ OFFEN | 🟢 NIEDRIG |
| **D8** | Design | Theme: System vs. Custom vs. User-Choice | ❓ OFFEN | 🟢 NIEDRIG |

---

## 🔥 HOCHPRIORITÄT ENTSCHEIDUNGEN

### **D1: KI-Integration Provider** 🤖

**Kontext:** Sales Cockpit braucht KI-Priorisierung für MyDay Column

**Optionen:**
- **A) OpenAI API** 
  - ✅ Beste Qualität, sofort verfügbar
  - ❌ Kosten: ~$0.02 pro Request, externe Abhängigkeit
  - ❌ Datenschutz: Daten gehen an OpenAI
  
- **B) Local Model (Ollama)**
  - ✅ Datenschutz, keine laufenden Kosten
  - ❌ Setup-Komplexität, Hardware-Anforderungen
  - ❌ Qualität schlechter als GPT-4

- **C) Hybrid Approach**
  - ✅ OpenAI für Produktion, Local für Development
  - ❌ Doppelte Implementierung, Komplexität

**Auswirkungen:**
- Entwicklungszeit: A=2 Tage, B=5 Tage, C=7 Tage
- Laufende Kosten: A=~€50/Monat, B=€0, C=€50/Monat
- Datenqualität: A=Exzellent, B=Gut, C=Exzellent

**Empfehlung:** Option A (OpenAI) für schnellen Start, später Option B evaluieren

**Benötigt Input von:** Jörg (Kosten-Nutzen), Compliance-Team (Datenschutz)

**Betrifft Module:** [M3_TECH_CONCEPT.md](./M3_TECH_CONCEPT.md)

---

### **D2: Real-time Refresh Strategy** ⚡

**Kontext:** Sales Cockpit braucht aktuelle Daten für Team-Kollaboration

**Optionen:**
- **A) Polling (5 Min Intervall)**
  - ✅ Einfache Implementierung, bewährt
  - ❌ Latenz, potentiell veraltete Daten
  - ❌ Höhere Server-Last

- **B) WebSocket Real-time**
  - ✅ Sofortige Updates, bessere UX
  - ❌ Komplexere Implementierung
  - ❌ Connection-Management, Reconnect-Logic

- **C) Hybrid: Polling + WebSocket für kritische Events**
  - ✅ Bestes aus beiden Welten
  - ❌ Doppelte Implementierung
  - ❌ Komplexe Orchestrierung

**Auswirkungen:**
- Entwicklungszeit: A=1 Tag, B=3 Tage, C=4 Tage
- User Experience: A=Gut, B=Exzellent, C=Exzellent
- Server-Komplexität: A=Niedrig, B=Hoch, C=Sehr hoch

**Empfehlung:** Option A für MVP, Option B für V2

**Benötigt Input von:** Jörg (Priorität), Backend-Team (Machbarkeit)

**Betrifft Module:** [M3_TECH_CONCEPT.md](./M3_TECH_CONCEPT.md)

---

### **D3: Navigation Menü-Style** 🧭

**Kontext:** MainLayoutV2 braucht optimale Navigation für Sales-Workflows

**Optionen:**
- **A) Sidebar (aktuell)**
  - ✅ Etabliert, viel Platz für Menüpunkte
  - ❌ Weniger Platz für Content
  - ❌ Mobile-Experience suboptimal

- **B) Top-Navigation**
  - ✅ Mehr Content-Platz, modern
  - ❌ Weniger Platz für Menüpunkte
  - ❌ Bei vielen Menüpunkten problematisch

- **C) Hybrid: Collapsible Sidebar**
  - ✅ Flexibel, nutzergesteuert
  - ❌ Komplexere State-Management
  - ❌ Inkonsistente UX

**Auswirkungen:**
- User Experience: A=Gut, B=Sehr gut, C=Exzellent
- Entwicklungsaufwand: A=0 Tage, B=2 Tage, C=3 Tage
- Mobile-Tauglichkeit: A=Schlecht, B=Gut, C=Exzellent

**Empfehlung:** Option C (Hybrid) für beste Flexibilität

**Benötigt Input von:** Jörg (UX-Priorität), Sales-Team (Workflow-Präferenz)

**Betrifft Module:** [M1_TECH_CONCEPT.md](./M1_TECH_CONCEPT.md)

---

## 🟡 MITTLERE PRIORITÄT ENTSCHEIDUNGEN

### **D4: Quick Create FAB-Position** ⚡

**Kontext:** Floating Action Button für schnelle Erstellung

**Optionen:**
- **A) Fixed Bottom-Right**
  - ✅ Standard-Position, immer sichtbar
  - ❌ Kann Content überdecken
  
- **B) Context-sensitive**
  - ✅ Intelligente Positionierung
  - ❌ Komplexere Logik, weniger vorhersagbar

**Empfehlung:** Option A für Konsistenz

**Benötigt Input von:** UX-Team

**Betrifft Module:** [M2_TECH_CONCEPT.md](./M2_TECH_CONCEPT.md)

---

### **D5: Settings Storage Strategy** ⚙️

**Kontext:** Wo sollen User-Settings gespeichert werden?

**Optionen:**
- **A) Local Storage**
  - ✅ Schnell, keine Server-Calls
  - ❌ Verloren bei Browser-Wechsel
  
- **B) Server-side**
  - ✅ Überall verfügbar, backup
  - ❌ Latenz, Server-Abhängigkeit

- **C) Hybrid: Local + Server Sync**
  - ✅ Beste Performance + Persistenz
  - ❌ Komplexere Implementierung

**Empfehlung:** Option C für beste User Experience

**Benötigt Input von:** Backend-Team (API-Aufwand)

**Betrifft Module:** [M7_SETTINGS_TECH_CONCEPT.md](./M7_SETTINGS_TECH_CONCEPT.md)

---

### **D6: Permissions Integration** 🔐

**Kontext:** Wie sollen Permissions in UI-Komponenten geladen werden?

**Optionen:**
- **A) Eager Loading beim Login**
  - ✅ Sofortige Verfügbarkeit
  - ❌ Längere Login-Zeit
  
- **B) Lazy Loading per Component**
  - ✅ Schnellerer Login
  - ❌ Loading-States, Komplexität

**Empfehlung:** Option A für bessere UX

**Benötigt Input von:** Performance-Team

---

## 🟢 NIEDRIGE PRIORITÄT ENTSCHEIDUNGEN

### **D7: Caching Strategy** 🚀

**Kontext:** Wo sollen häufig genutzte Daten gecacht werden?

**Optionen:**
- **A) Memory-only (React Query)**
- **B) Local Storage + Memory**
- **C) IndexedDB für große Datasets**

**Empfehlung:** Option B für Balance

---

### **D8: Theme System** 🎨

**Kontext:** Welche Theme-Optionen anbieten?

**Optionen:**
- **A) System-Theme (Auto Dark/Light)**
- **B) Feste Freshfoodz CI**
- **C) User-Choice mit Freshfoodz Base**

**Empfehlung:** Option C für Flexibilität

---

## 📞 NÄCHSTE SCHRITTE

1. **Stakeholder-Meeting** → D1, D2, D3 klären (kritisch für Architektur)
2. **Technical Spike** → KI-Provider testen (wenn D1 = A oder B)
3. **UX-Review** → Navigation-Präferenzen ermitteln (D3)
4. **Backend-Abstimmung** → WebSocket-Machbarkeit prüfen (D2)

**WICHTIG:** D1-D3 MÜSSEN vor Implementation geklärt werden!

**DEADLINES:**
- D1, D2, D3: Vor M3 Enhancement (kritisch)
- D4, D5, D6: Vor M2 Implementation (wichtig)
- D7, D8: Flexibel, kann später entschieden werden

---

## 🎯 DECISION TRACKING

**Entscheidungsinhaber:** Jörg Streeck  
**Technische Beratung:** Backend-Team, UX-Team  
**Review-Datum:** Nächstes Entwickler-Meeting  
**Dokumentations-Update:** Nach jeder Entscheidung  

**Template für Entscheidungsdokumentation:**
```
ENTSCHEIDUNG D1: [Datum]
- Gewählte Option: [A/B/C]
- Begründung: [Warum]
- Auswirkungen: [Aufwand, Kosten, Risiken]
- Nächste Schritte: [Konkrete Todos]
```