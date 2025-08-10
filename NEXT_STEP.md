# 🧭 NEXT STEP NAVIGATION

**Letzte Aktualisierung:** 2025-08-10, 12:15 Uhr  
**Aktiver Branch:** `feature/fc-005-enhanced-features`
**Nächste Migration:** V217 (letzte war V216__add_extended_search_indexes.sql)

## 🚨 KRITISCHES PROBLEM:

### Backend kompiliert nicht! 
**31 Kompilierungsfehler** durch Inkonsistenz zwischen Contact und CustomerContact Entities
- ContactRepository nutzt `CustomerContact`
- Services (ContactService, DataHygieneService, etc.) nutzen noch `Contact`
- **Backend kann nicht gestartet werden!**

## 🎯 JETZT GERADE:

**FC-005 Step3 PR4: BACKEND-FIX ERFORDERLICH! 🚨**

**Stand 10.08.2025 12:15:**
- ✅ Frontend Universal Search mit Deep-Linking komplett implementiert
- ✅ SearchResultsDropdown mit Highlighting und Icons
- ✅ CustomerDetailPage mit highlightContact Parameter
- ✅ ContactGridContainer mit Highlight-Animation
- ✅ Migration V216 für erweiterte Search-Indizes
- ❌ **Backend kompiliert nicht (Contact vs CustomerContact Problem)**

## 🚀 NÄCHSTER SCHRITT:

### PRIORITÄT 1: Backend-Kompilierung fixen

```bash
# 1. Problem analysieren
cd /Users/joergstreeck/freshplan-sales-tool/backend
grep -r "import.*Contact;" src/ | grep -v CustomerContact

# 2. Entscheidung treffen:
# Option A: Alle Services auf CustomerContact migrieren
# Option B: Contact als Interface/Wrapper definieren

# 3. Nach Fix testen
./mvnw quarkus:dev
```

### Nach Backend-Fix:

```bash
# Universal Search testen
curl -X GET "http://localhost:8080/api/search/universal?query=Müller&includeContacts=true" | jq

# Frontend testen
cd ../frontend
npm run dev
# Browser: Suche testen, auf Kontakt klicken → Deep-Link sollte funktionieren
```

## ✅ WAS WURDE HEUTE FERTIGGESTELLT:

### Frontend (100% fertig):
1. **SearchResultsDropdown:**
   - Highlighting von Suchbegriffen
   - Kategorisierte Sektionen (Kunden/Kontakte)
   - Icons für gefundene Felder
   - "Top-Treffer" Badge
   
2. **Deep-Linking:**
   - Navigate zu `/customers/{id}?highlightContact={contactId}`
   - Auto-Switch zu Kontakte-Tab
   - Highlight-Animation (3x Pulse)
   - CSS-Animationen in `animations.css`

3. **Backend-Teile:**
   - ContactRepository.searchContactsFullText()
   - SearchService mit Query-Analyse
   - Migration V216 erstellt

## 📋 TODO-STATUS:
- **Completed:** 6 Tasks ✅
- **Pending:** 9 Tasks
  - **PRIORITÄT:** Backend Kompilierungsfehler beheben
  - Backend Tests für erweiterte Kontakt-Suche
  - E2E Test: Suche → Click → Highlight
  - ESLint-Fehler (308 verbleibend)
  - Weitere Tasks...

## 📁 WICHTIGE DATEIEN:
- **Problem-Services:** 
  - `/backend/src/main/java/de/freshplan/domain/customer/service/ContactService.java`
  - `/backend/src/main/java/de/freshplan/domain/customer/service/DataHygieneService.java`
  - `/backend/src/main/java/de/freshplan/domain/customer/service/ContactInteractionService.java`
- **Funktionierende Teile:**
  - `/backend/src/main/java/de/freshplan/domain/customer/repository/ContactRepository.java`
  - `/backend/src/main/java/de/freshplan/domain/search/service/SearchService.java`

## ⚡ Quick Commands

```bash
# Backend kompilieren (wird fehlschlagen)
cd backend
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home
./mvnw compile

# Fehler analysieren
./mvnw compile 2>&1 | grep "error:" | head -20

# Nach Fix: Backend starten
./mvnw quarkus:dev

# Frontend läuft bereits
cd ../frontend
npm run dev
```