# 🚨 OPEN QUESTIONS TRACKER - Zentrale Sammlung

**Zweck:** Alle offenen Fragen an einer Stelle, damit nichts verloren geht  
**Status:** Wird bei jedem Feature-Start aktualisiert  
**Verantwortlich:** Product Owner / Tech Lead

## 🔴 KRITISCHE FRAGEN (Blockieren Features)

### 1. Keycloak Configuration (Tag 1)
- [ ] **Keycloak URL:** `auth.z-catering.de` - Ist das korrekt?
- [ ] **Realm Name:** Production Realm Name?
- [ ] **Client ID:** Frontend Client ID?
- [ ] **Redirect URIs:** Erlaubte Redirect URLs?
- [ ] **Admin Access:** Wer kann Keycloak konfigurieren?

### 2. Opportunity Stage Transitions (Tag 2)
- [ ] **Erlaubte Übergänge:** Vollständige Matrix fehlt!
  ```
  NEW_LEAD → ? (Nur QUALIFICATION oder auch direkt CLOSED_LOST?)
  QUALIFICATION → ? (Kann man zurück zu NEW_LEAD?)
  ```
- [ ] **Pflicht-Validierungen:** Welche Felder bei welcher Stage?
- [ ] **Auto-Aktionen:** Was passiert automatisch?

### 3. Verkäuferschutz Rules (Tag 7)
- [ ] **Sichtbarkeit:** Wer sieht welche Opportunities?
- [ ] **Edit-Rechte:** Wer darf Stages ändern?
- [ ] **Provisions-Split:** Wie bei Team-Arbeit?
- [ ] **Konflikt-Eskalation:** An wen? Wie?

## 🟡 TECHNISCHE FRAGEN (Wichtig, aber nicht blockierend)

### 4. Performance Requirements
- [ ] **Max Opportunities:** Pro Stage? (100, 500, 1000?)
- [ ] **Response Times:** SLAs definieren
- [ ] **Concurrent Users:** Wie viele gleichzeitig?
- [ ] **Data Retention:** Wie lange Opportunities behalten?

### 5. Integration Details
- [ ] **Calculator Data:** Wo speichern? (In Opportunity?)
- [ ] **E-Mail Zuordnung:** Automatisch nach welchen Regeln?
- [ ] **Xentral Sync:** Real-time oder Batch?
- [ ] **Document Storage:** S3 Bucket Structure?

### 6. Mobile Strategy
- [ ] **PWA vs Native:** Endgültige Entscheidung?
- [ ] **Offline Capability:** Welche Features offline?
- [ ] **App Stores:** iOS/Android Deployment?
- [ ] **Push Notifications:** Provider?

## 🟢 STRATEGISCHE FRAGEN (Langfristige Planung)

### 7. AI/ML Features
- [ ] **Datenschutz:** Dürfen wir Kundendaten für ML nutzen?
- [ ] **Hosting:** Cloud oder On-Premise für AI?
- [ ] **Kosten:** Budget für AI-Services?
- [ ] **Ethik:** Guidelines für AI-Nutzung?

### 8. Skalierung
- [ ] **Multi-Tenant:** Geplant? Wann?
- [ ] **White-Label:** Option für Partner?
- [ ] **Internationalisierung:** Weitere Sprachen?
- [ ] **Compliance:** Weitere Märkte (US, UK)?

### 9. Business Model
- [ ] **Lizenzierung:** Pro User? Pro Feature?
- [ ] **API Access:** Für Partner? Kosten?
- [ ] **Support Level:** SLAs definieren
- [ ] **Customization:** Wie viel erlauben?

## 📊 FRAGEN NACH PRIORITÄT

### Muss VOR Tag 1 geklärt werden:
1. Keycloak Configuration (alle Punkte)
2. Basic User Roles

### Muss VOR Tag 2 geklärt werden:
1. Stage Transition Rules
2. Opportunity Ownership Basics

### Muss VOR Woche 2 geklärt werden:
1. Verkäuferschutz Details
2. Calculator Integration Flow
3. Performance Requirements

### Kann später geklärt werden:
1. AI/ML Details
2. Internationalisierung
3. White-Label Options

## 🔄 PROZESS

1. **Bei Feature-Start:** Relevante Fragen hier nachschlagen
2. **In Meetings:** Diese Liste als Agenda nutzen
3. **Nach Klärung:** ✅ markieren und Antwort dokumentieren
4. **Neue Fragen:** Sofort hier eintragen mit Priorität

## 📝 KLÄRUNGSTERMINE

| Datum | Thema | Teilnehmer | Status |
|-------|-------|------------|--------|
| 13.07. 09:00 | Keycloak Setup | DevOps + Security | 📅 Geplant |
| 13.07. 14:00 | Business Rules Workshop | PO + Sales Lead | 📅 Geplant |
| 15.07. 10:00 | Performance Requirements | Tech Lead + Infra | 📅 Geplant |

## 💡 ENTSCHEIDUNGS-LOG

### Bereits entschieden:
1. **Frontend Framework:** React + TypeScript ✅
2. **Backend:** Quarkus + Java 17 ✅
3. **Database:** PostgreSQL ✅
4. **Auth:** Keycloak ✅
5. **Cloud:** AWS ✅

### Noch offen:
- State Management (React Query vs Redux)
- Testing Framework für E2E
- Monitoring Solution
- Error Tracking Tool

---

**Hinweis:** Dieses Dokument ist die SINGLE SOURCE OF TRUTH für alle offenen Fragen. Bitte IMMER hier prüfen, bevor neue Features gestartet werden!