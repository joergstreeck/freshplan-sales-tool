# 🔍 Finding: CLAUDE_TECH Dokumente brauchen Dateipfade

## Problem
Der Reality Check hat aufgedeckt, dass die CLAUDE_TECH Dokumente zwar Code-Beispiele enthalten, aber keine konkreten Dateipfade wo dieser Code implementiert werden soll.

## Beispiel
FC-008_CLAUDE_TECH.md zeigt:
```java
@Path("/api/auth")
public class AuthResource {
    // Code...
}
```

Aber nicht WO: `backend/src/main/java/de/freshplan/api/resources/AuthResource.java`

## Empfehlung
Jedes CLAUDE_TECH Dokument sollte eine Section haben:

```markdown
## 📁 Datei-Struktur
- `backend/src/main/java/de/freshplan/api/resources/AuthResource.java` - REST Endpoint
- `backend/src/main/java/de/freshplan/domain/auth/service/AuthService.java` - Business Logic
- `frontend/src/contexts/AuthContext.tsx` - Frontend Auth State
- `frontend/src/components/auth/LoginForm.tsx` - Login UI
```

## Impact
Ohne diese Information:
- Claude muss raten wo Code hin soll
- Reality Check kann nicht richtig prüfen
- Hohe Fehlerwahrscheinlichkeit

## Nächste Schritte
1. Template für CLAUDE_TECH erweitern
2. Bestehende Dokumente nachbessern
3. Reality Check wird dann viel effektiver