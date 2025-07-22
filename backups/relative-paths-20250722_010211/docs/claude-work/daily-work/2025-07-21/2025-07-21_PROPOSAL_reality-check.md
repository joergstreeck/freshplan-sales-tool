# Reality Check Prozess - Proposal

## 🎯 Konzept
Vor jeder größeren Implementierung (neues Feature/Modul) wird ein verpflichtender Reality Check durchgeführt.

## 📋 Der 3-Schritte-Prozess

### 1️⃣ Plan lesen
```bash
cat docs/features/ACTIVE/[feature]/FC-XXX_CLAUDE_TECH.md
```

### 2️⃣ Code Reality Check (NEU!)
```bash
# Die im Plan genannten Dateien MÜSSEN gelesen werden:
cat frontend/src/contexts/AuthContext.tsx
cat backend/src/main/java/.../SecurityConfig.java
# etc.
```

### 3️⃣ Abgleich & Entscheidung

**Option A: ✅ Plan stimmt mit Reality überein**
```markdown
## Reality Check: FC-008 Security Foundation
- ✅ AuthContext.tsx existiert und nutzt Keycloak wie geplant
- ✅ SecurityConfig.java hat OIDC Integration
- ✅ Endpoints stimmen überein
→ PROCEED WITH IMPLEMENTATION
```

**Option B: ❌ Abweichungen gefunden**
```markdown
## Reality Check: FC-008 Security Foundation
- ❌ Plan sagt "Redux" aber Code nutzt "React Context"
- ❌ Plan erwähnt /api/auth aber Code hat /api/v1/auth
- ⚠️ UserRole enum hat andere Werte als Plan
→ STOP! UPDATE PLAN FIRST!
```

## 🚀 Integration in Workflow

### In CLAUDE.md ergänzen:
```markdown
### Reality Check (PFLICHT bei neuen Features!)
1. Feature-Plan lesen (CLAUDE_TECH)
2. Betroffene Code-Dateien lesen
3. Reality-Check dokumentieren:
   - Stimmt überein → Implementierung starten
   - Abweichungen → Plan updaten, dann neu starten
```

### Als Script:
```bash
#!/bin/bash
# scripts/reality-check.sh

FEATURE=$1
echo "🔍 Reality Check für $FEATURE"

# 1. Finde relevante Code-Dateien aus Plan
echo "📋 Extrahiere Code-Referenzen aus Plan..."
grep -E "(\.tsx|\.ts|\.java|\.py)" docs/features/ACTIVE/$FEATURE/*.md

# 2. Bestätige Existenz
echo "✓ Prüfe Datei-Existenz..."

# 3. Generiere Check-Liste
echo "
## Reality Check Checkliste:
- [ ] Alle im Plan genannten Dateien existieren
- [ ] APIs/Endpoints stimmen überein  
- [ ] Datenstrukturen passen
- [ ] Dependencies vorhanden
"
```

## 💡 Vorteile

1. **Verhindert Zeitverschwendung** durch veraltete Pläne
2. **Erzwingt Code-Verständnis** vor Implementierung
3. **Dokumentiert Abweichungen** für Team
4. **Einfach automatisierbar**
5. **Funktioniert für Claude UND Menschen**

## 🎯 Beispiel-Trigger

In der Übergabe:
```markdown
## 🔍 REALITY CHECK STATUS
- [ ] FC-008: Reality Check ausstehend
- [✓] M4: Reality Check passed 20.07.
- [❌] FC-011: Reality Check failed - Plan Update nötig
```

## Empfehlung
Diesen Prozess als TODO-49 aufnehmen und als erstes für FC-008 testen!