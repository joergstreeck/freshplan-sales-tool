# 📋 DECISION LOG: M8 Calculator Modal

**Status:** 🟡 Einige Entscheidungen offen  
**Owner:** @joergstreeck  

---

## ✅ ENTSCHIEDENE FRAGEN

### 1. Modal vs. Full Page
**Frage:** Calculator als Modal oder eigene Seite?  
**Entscheidung:** Modal Dialog  
**Begründung:** Kontext bleibt erhalten, schnellerer Workflow  
**Datum:** 17.07.2025  

### 2. Legacy Calculator Handling
**Frage:** Refactoren oder Wrappen?  
**Entscheidung:** NUR Wrappen, kein Refactoring  
**Begründung:** Risiko minimieren, funktioniert bereits  
**Datum:** 17.07.2025  

---

## ❓ OFFENE FRAGEN (Antwort benötigt!)

### 1. Autosave Frequenz
**Frage:** Wie oft soll automatisch gespeichert werden?  
**Optionen:**
- Bei jeder Änderung (aggressiv)
- Alle 30 Sekunden
- Alle 2 Minuten
- Nur manuell
**Empfehlung:** Alle 30 Sekunden + vor Schließen  
**Status:** ⏳ Warte auf UX Team  

### 2. Template Verwaltung
**Frage:** Wer kann Templates erstellen/teilen?  
**Optionen:**
- Jeder für sich selbst
- Team-Templates (Manager erstellt)
- Globale Templates (Admin)
- Alle Ebenen
**Empfehlung:** Alle Ebenen mit Berechtigungen  
**Status:** ⏳ Warte auf Business  

### 3. PDF Generierung
**Frage:** Wo wird das PDF generiert?  
**Optionen:**
- Frontend (jsPDF)
- Backend (Puppeteer/wkhtmltopdf)
- Externer Service
**Empfehlung:** Backend mit Puppeteer  
**Status:** ⏳ Warte auf DevOps  

### 4. Offline Capability
**Frage:** Soll Calculator offline funktionieren?  
**Use Case:** Außendienst ohne Internet  
**Komplexität:** LocalStorage + Sync  
**Empfehlung:** Phase 2 (nicht MVP)  
**Status:** ⏳ Warte auf Priorisierung  

### 5. Calculation Versioning
**Frage:** Alte Versionen einer Kalkulation behalten?  
**Use Case:** "Zeig mir Version vom letzten Montag"  
**Storage:** +30% Database Size  
**Empfehlung:** Letzte 5 Versionen  
**Status:** ⏳ Warte auf Product Owner  

---

## 🔮 ZUKÜNFTIGE ÜBERLEGUNGEN

### 1. KI-Features
- **Produkt-Vorschläge** basierend auf Kundenhistorie
- **Preis-Optimierung** für höhere Abschlussrate
- **Cross-Selling** Empfehlungen

### 2. Erweiterte Integration
- **Direkt-Versand** aus Calculator
- **Unterschrift** elektronisch einholen
- **Payment Link** für Sofort-Zahlung

### 3. Analytics
- **Angebots-Erfolgsrate** nach Produkten
- **Durchschnittliche Bearbeitungszeit**
- **Häufigste Änderungen** nach Versand

---

## 📊 DEFAULT-ANNAHMEN

Falls keine Antwort bis Implementation:

1. **Autosave** - Alle 30 Sekunden
2. **Templates** - 3-Ebenen-System
3. **PDF** - Backend-Generierung
4. **Offline** - Nicht in MVP
5. **Versioning** - Letzte 5 Versionen behalten