# 🚀 Claude Start-Checkliste

## Bei jedem neuen Chat mit Claude:

### 1. Datum injizieren
```bash
node scripts/inject-current-date.js
```

### 2. Claude anweisen
```
"Lies bitte zuerst MASTER_BRIEFING.md sehr gründlich durch. 
Das ist dein zentrales Briefing-Dokument. 
Bestätige mir danach das heutige Datum."
```

### 3. Bei Bedarf weitere Docs
```
"Lies jetzt auch CLAUDE.md für deine Arbeitsregeln."
```

## Bei komprimierten Chats:

Wenn Claude Kontext verliert:
```
"Du scheinst den Kontext verloren zu haben. 
Lies bitte nochmal MASTER_BRIEFING.md zur Orientierung."
```

## Wichtige Hinweise:

- **IMMER** das Datum von Claude bestätigen lassen
- **NIE** davon ausgehen, dass Claude das richtige Datum kennt
- Bei Unsicherheiten → MASTER_BRIEFING.md neu lesen lassen