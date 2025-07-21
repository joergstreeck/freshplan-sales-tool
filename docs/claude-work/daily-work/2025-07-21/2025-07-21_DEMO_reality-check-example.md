# 🎯 Reality Check - Beispiel-Durchlauf

## So funktioniert der neue 3-Stufen Reality Check:

### 🖥️ Terminal-Output:

```bash
$ ./scripts/reality-check.sh FC-008

🔍 Reality Check für FC-008
====================================

📌 Der Reality Check Prozess:
1️⃣  Plan lesen und verstehen
2️⃣  Code lesen (VERPFLICHTEND)
3️⃣  Abgleich und Bestätigung

====================================

SCHRITT 1: Plan lesen
📋 Plan gefunden: docs/features/ACTIVE/01_security_foundation/FC-008_CLAUDE_TECH.md
→ Bitte lies jetzt den Plan durch bevor du fortfährst!

Hast du den Plan gelesen? (j/n): j

SCHRITT 2: Code lesen (VERPFLICHTEND)
🔎 Extrahiere Code-Referenzen aus Plan...

📁 Zu lesende Code-Dateien:
frontend/src/contexts/AuthContext.tsx
backend/src/main/java/de/freshplan/security/SecurityConfig.java
backend/src/main/java/de/freshplan/api/filters/AuthenticationFilter.java

→ Bitte lies JETZT diese Code-Dateien durch!
   Achte besonders auf:
   - Stimmt die Struktur mit dem Plan überein?
   - Gibt es unerwartete Dependencies?
   - Sind die geplanten Schnittstellen vorhanden?

Hast du alle relevanten Code-Dateien gelesen? (j/n): j

SCHRITT 3: Abgleich und Bestätigung
✓ Prüfe Existenz...
  ✅ frontend/src/contexts/AuthContext.tsx
  ❌ backend/src/main/java/de/freshplan/security/SecurityConfig.java - NICHT GEFUNDEN
  ⚠️  backend/src/main/java/de/freshplan/api/filters/AuthenticationFilter.java -> gefunden als: backend/src/main/java/de/freshplan/config/SecurityConfig.java

====================================
Zusammenfassung: 2 OK, 1 FEHLEN

## Reality Check Checkliste:

Bitte bestätige jeden Punkt:

✓ Plan und Code-Struktur stimmen überein? (j/n): n
  → Nicht bestätigt
✓ Alle APIs/Endpoints sind wie geplant? (j/n): j
  → OK
✓ Datenstrukturen sind kompatibel? (j/n): j
  → OK
✓ Dependencies sind installiert? (j/n): j
  → OK
✓ Keine unerwarteten Abhängigkeiten gefunden? (j/n): n
  → Nicht bestätigt

⚠️  Nicht alle Punkte bestätigt
Bitte kläre die Diskrepanzen bevor du mit der Implementation beginnst!

====================================
📊 ZUSAMMENFASSUNG

✓ Plan gelesen und verstanden
✓ Code-Dateien durchgesehen
✓ Abgleich durchgeführt

❌ REALITY CHECK FAILED

   → 1 Dateien fehlen oder sind falsch benannt
   → Diskrepanzen zwischen Plan und Code gefunden

STOPP! Bitte NICHT mit der Implementation beginnen!

Nächste Schritte:
1. Kläre alle Diskrepanzen
2. Update den Plan ODER den Code
3. Führe Reality Check erneut aus

Warum ist das wichtig?
→ Mit veralteten Plänen verschwendest du 2+ Stunden Arbeit
→ Mit aktuellem Plan arbeitest du effizient und richtig
```

## 🎯 Die Vorteile des neuen Prozesses:

### 1. **Erzwungenes Code-Lesen**
- Entwickler MUSS den Code lesen, nicht nur den Plan
- Verhindert Annahmen basierend auf veralteten Plänen

### 2. **Interaktive Checkliste**
- Nicht nur "Dateien existieren?"
- Sondern: "Stimmt die Architektur?"
- "Gibt es unerwartete Dependencies?"

### 3. **Klare Stop-Signale**
- Bei JEDER Diskrepanz: STOPP!
- Keine Ausreden mehr ("wird schon passen")

### 4. **Automatisches Logging**
In `.reality-check-log`:
```
- [❌] FC-008: Reality Check failed - Plan/Code Diskrepanz (21.07. 16:45)
- [✅] FC-011: Reality Check passed - Plan/Code synchron (21.07. 17:30)
```

## 🔄 Integration in den Workflow:

### Für Claude:
```markdown
## Reality Check vor Implementation:
1. ./scripts/reality-check.sh FC-XXX ausführen
2. Plan lesen wenn gefragt
3. ALLE genannten Code-Dateien mit Read tool lesen
4. Checkliste ehrlich beantworten
5. Bei PASSED: Implementation starten
6. Bei FAILED: Erst Diskrepanzen klären
```

### Für Menschen:
1. Feature-Branch erstellen: `git checkout -b feature/FC-008-security`
2. Reality Check: `./scripts/reality-check.sh FC-008`
3. Folge den Anweisungen im Terminal
4. Erst bei grünem Check mit Coding beginnen

## 📈 Messbare Verbesserungen:

- **Vorher**: 2-3 Stunden verschwendet mit falschen Annahmen
- **Nachher**: 5 Minuten Reality Check = richtige Implementation beim ersten Versuch

Der Reality Check macht aus "hoffentlich stimmt der Plan" ein "ich weiß dass Plan und Code synchron sind"!