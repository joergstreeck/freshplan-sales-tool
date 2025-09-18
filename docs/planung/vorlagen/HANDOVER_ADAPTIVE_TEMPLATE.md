# Adaptive Übergabe Template

## Standard-Information (IMMER ausfüllen)
- **Datum/Zeit:** {{DATE_TIME}}
- **Session-Dauer:** {{DURATION}}
- **TODOs erledigt:** {{COMPLETED_TODOS}}
- **TODOs offen:** {{OPEN_TODOS}}

## Was wurde gemacht?
{{WORK_DONE}}

## Was funktioniert?
{{VERIFIED_FEATURES}}

## Nächste Schritte
{{NEXT_STEPS}}

---

## 🚨 UNTERBRECHUNGS-BLOCK (nur bei ungeplanter Beendigung)

**Status:** {{IF_INTERRUPTED}}
- [ ] Geplante Beendigung (Standard-Übergabe)
- [ ] Ungeplante Unterbrechung (zusätzliche Info nötig)
- [ ] Fehler-Unterbrechung (kritische Info nötig)

### Bei Unterbrechung zusätzlich ausfüllen:

**Exakte Position:**
- **TODO:** {{CURRENT_TODO}}
- **Datei:** {{CURRENT_FILE}}
- **Zeile/Bereich:** {{CURRENT_POSITION}}

**Gedankengang:**
{{MENTAL_CONTEXT}}

**Nächster geplanter Schritt:**
{{NEXT_PLANNED_ACTION}}

### Bei Fehler-Unterbrechung zusätzlich:

**Fehlermeldung:**
```
{{ERROR_MESSAGE}}
```

**Bereits versucht:**
{{ATTEMPTED_SOLUTIONS}}

**Verdächtige Ursache:**
{{SUSPECTED_CAUSE}}

---

## Für die nächste Session
{{HANDOVER_NOTES}}