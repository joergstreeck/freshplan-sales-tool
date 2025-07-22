# 🔄 Der neue Two-Pass-Review-Prozess - Dokumentation

**Datum:** 06.07.2025
**Status:** VERBINDLICH - Ab sofort gültig
**Autor:** Claude & Jörg

## 🎯 Die Vision

Unser Two-Pass-Review-Prozess kombiniert das Beste aus beiden Welten: Automatisierte Code-Hygiene durch Maschinen und strategische Code-Qualität durch menschliche Intelligenz.

## 📋 Der neue Prozess im Detail

### Pass 1: Die "Pflicht" – Automatische Code-Hygiene

**Was wird geprüft?**
- Reine Code-Formatierung
- Einrückungen und Leerzeichen
- Kommasetzung
- Import-Reihenfolge
- Konsistente Klammersetzung

**Wer macht das?**
- Claude führt automatisch `./mvnw spotless:apply` aus
- Das ist eine rein maschinelle, objektive Aufgabe
- Keine Diskussionen mehr über Formatierung

**Aufwand für das Team:** 
- **NULL** - Sie müssen sich nie wieder Gedanken darüber machen!

**Implementierung:**
```bash
# Claude führt automatisch aus:
cd backend
./mvnw spotless:apply

# Bei Änderungen: Separater Commit
git add -u
git commit -m "chore: apply Spotless formatting"
```

### Pass 2: Die "Kür" – Strategische Code-Qualität

**Was wird geprüft?**
Alles, was eine Maschine nicht kann und was wirklich über die Qualität der Software entscheidet:

#### 1. **Architektur** 🏛️
- Hält sich der Code an unsere Vision des "Sales Cockpit"?
- Ist er sauber entkoppelt (Domain/API/Infrastructure)?
- Folgt er unseren Backend- und Frontend-Architektur-Standards?

#### 2. **Logik** 🧠
- Tut das Feature genau das, was es laut `CRM_COMPLETE_MASTER_PLAN_V5.md` tun soll?
- Gibt es unentdeckte Denkfehler?
- Sind Edge-Cases berücksichtigt?
- Ist die Business-Logik korrekt implementiert?

#### 3. **Lesbarkeit & Wartbarkeit** 📖
- Ist der Code auch in sechs Monaten noch für einen neuen Entwickler verständlich?
- Sind die Namen von Variablen und Funktionen selbsterklärend?
- Gibt es ausreichend (aber nicht zu viel) Dokumentation?
- Sind komplexe Stellen kommentiert?

#### 4. **Philosophie** 💡
- Lebt der Code unsere Prinzipien?
  - "Geführte Freiheit" (Guided Freedom)
  - "Alles ist miteinander verbunden"
  - "Skalierbare Exzellenz"
- Ist der Code proaktiv statt reaktiv?
- Macht er das Leben der Vertriebsmitarbeiter einfacher?

**Wer macht das?**
- Claude und Jörg gemeinsam
- Claude dokumentiert Findings
- Jörg entscheidet über strategische Fragen

## 🚀 Praktische Umsetzung

### Vor jedem Commit:

1. **Claude führt Pass 1 aus:**
   ```bash
   cd backend && ./mvnw spotless:apply
   # Wenn Änderungen: Separater Commit
   ```

2. **Claude führt Pass 2 aus:**
   - Erstellt Review-Report nach Template
   - Markiert strategische Fragen für Jörg
   - Schlägt Verbesserungen vor

3. **Gemeinsame Entscheidung:**
   - Bei kritischen Findings: Sofort beheben
   - Bei strategischen Fragen: Jörg entscheidet
   - Bei Verbesserungen: In Backlog aufnehmen

### Review-Report Template für Pass 2:

```markdown
# Strategic Code Review - [Feature Name]
**Datum:** [YYYY-MM-DD]
**Feature:** [Name]

## 🏛️ Architektur-Check
- [ ] Folgt der Code unserer Schichtenarchitektur?
- [ ] Ist die Entkopplung sauber?
- [ ] Findings: [Liste]

## 🧠 Logik-Check
- [ ] Entspricht die Implementierung dem Master Plan?
- [ ] Sind alle Edge-Cases berücksichtigt?
- [ ] Findings: [Liste]

## 📖 Lesbarkeit & Wartbarkeit
- [ ] Sind Namen selbsterklärend?
- [ ] Ist der Code für neue Entwickler verständlich?
- [ ] Findings: [Liste]

## 💡 Philosophie-Check
- [ ] Geführte Freiheit umgesetzt?
- [ ] Alles verbunden?
- [ ] Skalierbare Exzellenz?
- [ ] Findings: [Liste]

## 🎯 Strategische Fragen für Jörg
1. [Frage mit Kontext]
2. [Frage mit Kontext]

## ✅ Empfehlung
[Claude's Einschätzung und Empfehlung]
```

## 🎉 Die Vorteile

1. **Keine Zeitverschwendung** mit Formatierungs-Diskussionen
2. **Fokus auf das Wesentliche** - Architektur und Geschäftslogik
3. **Konsistenter Code** durch automatische Formatierung
4. **Bessere Qualität** durch strategischen Fokus
5. **Schnellere Reviews** durch klare Trennung

## 📝 Zusammenfassung

- **Pass 1 (Maschine):** Formatierung - automatisch, objektiv, diskussionslos
- **Pass 2 (Mensch):** Strategie - durchdacht, wertvoll, zielgerichtet

Dieser Prozess stellt sicher, dass wir unsere Zeit mit den wichtigen Dingen verbringen: Software zu bauen, die unsere Vertriebsmitarbeiter lieben werden!