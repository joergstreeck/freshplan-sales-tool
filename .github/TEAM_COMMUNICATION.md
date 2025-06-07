# Team Communication Guide

## 🔄 Sync-Protokoll

### Regelmäßige Checks
- **Intervall:** Alle 30 Minuten
- **Dateien:** 
  - `TEAM_SYNC_LOG.md` - Allgemeine Updates
  - `API_CONTRACT.md` - API Änderungen
  - `INTEGRATION_STATUS.json` - Build/Test Status

### Magic Commands
Wenn Jörg sagt:
- **"An alle"** → Eintrag in TEAM_SYNC_LOG.md für beide Teams
- **"API Update"** → Änderung in API_CONTRACT.md dokumentieren  
- **"Blocker"** → In TEAM_SYNC_LOG unter "Blockers & Help Needed"
- **"Breaking Change"** → SOFORT in beide Logs + API Contract

### Workflow Beispiel

```bash
# Team FRONT - Vor der Arbeit
cat ../freshplan-sales-tool/TEAM_SYNC_LOG.md | tail -20

# Team FRONT - Nach wichtiger Änderung  
echo "### $(date '+%H:%M') - Team FRONT" >> ../freshplan-sales-tool/TEAM_SYNC_LOG.md
echo "- ✅ Auth Context implementiert" >> ../freshplan-sales-tool/TEAM_SYNC_LOG.md
echo "- 🚧 User-Liste braucht Pagination-Header" >> ../freshplan-sales-tool/TEAM_SYNC_LOG.md
```

## 📝 Dokumentations-Pflichten

### Team BACK muss dokumentieren:
- Neue API Endpoints
- Breaking Changes
- Keycloak Config Änderungen
- Security Requirements

### Team FRONT muss dokumentieren:
- Benötigte API Features
- UI/UX Entscheidungen  
- Performance Issues
- Browser-Kompatibilität

## 🚨 Eskalation

Bei kritischen Problemen:
1. **"URGENT: An alle"** in TEAM_SYNC_LOG
2. Jörg wird benachrichtigt
3. Beide Teams pausieren bis geklärt

## 💡 Pro-Tipps

- Git Commits referenzieren: "Siehe Commit abc123"
- Screenshots möglich: Als Base64 in Markdown
- Code-Snippets für Klarheit nutzen
- Emojis für schnelle Status-Erkennung:
  - ✅ Fertig
  - 🚧 In Arbeit
  - ❌ Blockiert
  - ⚠️ Achtung
  - 💡 Idee
  - 🐛 Bug