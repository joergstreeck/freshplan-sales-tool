# 📋 DECISION LOG: FC-009 Permissions

**Status:** 🟡 Einige Entscheidungen offen  
**Owner:** @joergstreeck  

## ✅ ENTSCHIEDENE FRAGEN

### 1. Cache-Strategie
**Frage:** Redis oder In-Memory Cache?  
**Entscheidung:** In-Memory für MVP, Redis später  
**Begründung:** Weniger Dependencies, reicht für < 1000 User  
**Datum:** 17.07.2025  

### 2. Permission Scopes
**Frage:** Welche Scopes brauchen wir?  
**Entscheidung:** ALL, TEAM, OWN  
**Begründung:** Deckt 95% der Use Cases ab  
**Datum:** 17.07.2025  

### 3. Migration Approach
**Frage:** Feature Flag oder Big Bang?  
**Entscheidung:** Feature Flag `permissions.use-new-system`  
**Begründung:** Sicherer Rollback möglich  
**Datum:** 17.07.2025  

## ❓ OFFENE FRAGEN (Antwort benötigt!)

### 1. Breaking Changes
**Frage:** Dürfen wir @RolesAllowed sofort deprecaten?  
**Impact:** 50+ Stellen im Code  
**Empfehlung:** 2-Wochen Deprecation Period  
**Status:** ⏳ Warte auf Jörg  

### 2. Multi-Team Membership
**Frage:** Kann ein User in mehreren Teams sein?  
**DB erlaubt es:** Ja (kein UNIQUE constraint)  
**Business Case:** Urlaubsvertretung?  
**Status:** ⏳ Warte auf Business  

### 3. Orphaned Customers
**Frage:** Was passiert mit Kunden ohne Owner?  
**Optionen:**
- Nur Admin sieht sie
- Automatisch Team Lead zuweisen
- Error State  
**Status:** ⏳ Warte auf Product Owner  

### 4. Permission Inheritance
**Frage:** Erben Sub-Teams Permissions vom Parent?  
**Beispiel:** Team "Vertrieb" → "Vertrieb Süd"  
**Empfehlung:** Nein, zu komplex für MVP  
**Status:** ⏳ Warte auf Architektur-Review  

### 5. Audit Requirements
**Frage:** Wie detailliert muss geloggt werden?  
**Optionen:**
- Jeder Permission Check (viel!)
- Nur Permission Changes
- Nur kritische Actions (delete, transfer)  
**Status:** ⏳ Warte auf Compliance  

## 🎯 DEFAULT-ANNAHMEN (wenn keine Antwort)

Falls Jörg nicht verfügbar:
1. **@RolesAllowed**: Bleibt erstmal, parallel System
2. **Multi-Team**: Nein, nur ein Team pro User
3. **Orphaned**: Nur Admin sieht sie
4. **Inheritance**: Keine Vererbung
5. **Audit**: Nur Permission Changes

## 📝 NOTES

- Bei Unsicherheit: Konservative Lösung wählen
- Lieber zu wenig Features als kaputtes System
- Performance > Features in Phase 1