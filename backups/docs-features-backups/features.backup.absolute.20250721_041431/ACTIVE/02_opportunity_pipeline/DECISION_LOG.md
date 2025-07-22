# 📋 DECISION LOG: M4 Opportunity Pipeline

**Status:** 🟡 Mehrere Entscheidungen offen  
**Owner:** @joergstreeck  

---

## ✅ ENTSCHIEDENE FRAGEN

### 1. Anzahl der Sales Stages
**Frage:** Wie viele Stages brauchen wir?  
**Entscheidung:** 5 Stages + Closed (Won/Lost)  
**Begründung:** Standard Sales Process, nicht zu komplex  
**Datum:** 17.07.2025  

### 2. Guided Mode Default
**Frage:** Ist der geführte Modus standardmäßig an?  
**Entscheidung:** JA - aber User kann ausschalten  
**Begründung:** Neue User profitieren, Profis können abschalten  
**Datum:** 17.07.2025  

---

## ❓ OFFENE FRAGEN (Antwort benötigt!)

### 1. Soft Delete vs. Hard Delete
**Frage:** Opportunities löschen oder archivieren?  
**Optionen:**
- Soft Delete (is_deleted flag)
- Archiv-Status nach 90 Tagen
- Hard Delete mit Audit Log
**Empfehlung:** Soft Delete + Auto-Archiv  
**Status:** ⏳ Warte auf Jörg  

### 2. Stage History
**Frage:** Sollen wir jeden Stage-Wechsel tracken?  
**Use Case:** "Wann war Deal in Negotiation?"  
**Storage Impact:** ~50 Entries pro Opportunity  
**Empfehlung:** JA - für Analytics wichtig  
**Status:** ⏳ Warte auf Business  

### 3. Attachment Handling
**Frage:** Wo speichern wir Dokumente?  
**Optionen:**
- S3 mit Presigned URLs
- Database BLOBs (nicht empfohlen)
- Filesystem mit Backup
**Empfehlung:** S3 mit Lifecycle Rules  
**Status:** ⏳ Warte auf DevOps Input  

### 4. Calculator Integration
**Frage:** Wann/wie öffnet sich der Calculator?  
**Optionen:**
- Button auf Opportunity Card
- Automatisch bei Stage "Proposal"
- Menüpunkt in Detail-View
**Empfehlung:** Button + Auto-Suggest  
**Status:** ⏳ Warte auf UX Decision  

### 5. Duplicate Detection
**Frage:** Wie erkennen wir doppelte Opportunities?  
**Kriterien:**
- Gleicher Kunde + ähnlicher Titel?
- Gleicher Wert + Zeitraum?
- Manuell durch User?
**Empfehlung:** Warnung bei Kunde+Titel  
**Status:** ⏳ Warte auf Sales Team  

---

## 🔮 ZUKÜNFTIGE ÜBERLEGUNGEN

### 1. KI-Features
- **Lead Scoring** basierend auf historischen Daten
- **Next Best Action** Empfehlungen
- **Win Probability** jenseits der Stage %

### 2. Mobile Optimierung
- **Swipe Actions** für Stage-Wechsel
- **Offline Mode** für Außendienst
- **Voice Notes** für schnelle Updates

### 3. Integrationen
- **Email** - Automatische Zuordnung
- **Kalender** - Meeting-Verknüpfung
- **Telefon** - Call Logs

---

## 📊 DEFAULT-ANNAHMEN

Falls keine Antwort bis Implementation:

1. **Soft Delete** - Opportunities werden nie gelöscht
2. **Stage History** - Wird getrackt
3. **S3 Storage** - Für Attachments
4. **Calculator Button** - Auf Card + Suggestion
5. **Duplicate Warning** - Bei gleichem Kunde+Titel