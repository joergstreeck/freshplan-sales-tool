# 📋 STRATEGIE FÜR TECHNISCHE DOKUMENTATIONS-LINKS

**Datum:** 20.07.2025  
**Problem:** 102 broken links zu noch nicht existierenden technischen Dokumenten  
**Lösung:** Einheitliche Kennzeichnung als "geplant"  

---

## 🎯 EMPFOHLENE STRATEGIE

### Links als "(geplant)" markieren

**Vorteile:**
- ✅ Zeigt transparent den geplanten Umfang
- ✅ Keine broken links mehr
- ✅ Einfach zu implementieren
- ✅ Kann später aktualisiert werden

**Nachteile:**
- ⚠️ Könnte Erwartungen wecken
- ⚠️ Muss später nachgepflegt werden

---

## 🔧 IMPLEMENTATION

### Standard-Pattern für technische Details:

```markdown
### 🔧 Technische Details:
- **[FEATURE]_IMPLEMENTATION_GUIDE.md** *(geplant)* - Technische Umsetzung
- **[FEATURE]_DECISION_LOG.md** *(geplant)* - Architektur-Entscheidungen
- **[SPECIFIC]_CATALOG.md** *(geplant)* - Feature-spezifischer Katalog
```

### Beispiel konkret:

```markdown
### 🔧 Technische Details:
- **FC-008_IMPLEMENTATION_GUIDE.md** *(geplant)* - Security Setup Guide
- **FC-008_DECISION_LOG.md** *(geplant)* - Keycloak vs. Auth0
- **SECURITY_PATTERNS.md** *(geplant)* - Best Practices
```

---

## 📊 AUFWAND

### Geschätzter Aufwand für Umsetzung:
- **40 Features** × 3 Links = 120 Links
- **Bereits korrekt:** FC-033 bis FC-036 (12 Links)
- **Zu ändern:** 108 Links in 36 Features
- **Zeit pro Feature:** ~1 Minute
- **Gesamtzeit:** ~40 Minuten

### Automatisierung möglich:
```bash
# Script könnte Links automatisch mit *(geplant)* ergänzen
sed -i 's/\(\[.*\](\.\/.*.md)\)/\1 *(geplant)*/g' *.md
```

---

## 🎨 ALTERNATIVEN

### Alternative 1: Placeholder-Dateien
- **Pro:** Keine broken links
- **Contra:** 120 leere Dateien, Wartungsaufwand

### Alternative 2: Links entfernen
- **Pro:** Sauber, keine falschen Erwartungen
- **Contra:** Verlust der Planungsinformation

### Alternative 3: Zentrale Tech-Docs
- **Pro:** Weniger Dateien
- **Contra:** Weniger Feature-spezifisch

---

## 🚀 EMPFEHLUNG

1. **Sofort:** Links als *(geplant)* markieren
2. **Mittelfristig:** Bei Implementation die Docs erstellen
3. **Langfristig:** Template für neue Features anpassen

**Warum diese Lösung?**
- Minimal invasiv
- Behält Planungsinformationen
- Klare Kommunikation des Status
- Einfach zu aktualisieren

---

## 📝 NÄCHSTE SCHRITTE

1. **Entscheidung treffen** - Welche Strategie?
2. **Update-Script erstellen** - Für Automatisierung
3. **Features updaten** - Alle 36 betroffenen
4. **Re-Validierung** - Links erneut prüfen
5. **Template anpassen** - Für zukünftige Features