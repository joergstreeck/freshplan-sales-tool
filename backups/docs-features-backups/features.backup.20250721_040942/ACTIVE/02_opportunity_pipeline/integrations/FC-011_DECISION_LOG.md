# FC-011 Decision Log - Bonitätsprüfung & Vertragsmanagement

**Feature Code:** FC-011  
**Dokument:** Entscheidungsprotokoll  
**Stand:** 18.07.2025  

---

## 📋 OFFENE ENTSCHEIDUNGEN

### D1: Bonitätsprüfungs-Provider ⭐

**Frage:** Welchen externen Anbieter für Bonitätsprüfungen nutzen?

**Optionen:**
1. **Creditreform** - Marktführer Deutschland
   - ✅ Umfassende Daten
   - ✅ API verfügbar
   - ❌ Teurer (~2-5€ pro Abfrage)

2. **Schufa B2B** - Bekannter Anbieter
   - ✅ Hohe Datenqualität
   - ✅ Schnelle Integration
   - ❌ Sehr teuer (~5-10€ pro Abfrage)

3. **Mock/Internes System** - Erstmal simulieren
   - ✅ Kostenlos für MVP
   - ✅ Volle Kontrolle
   - ❌ Keine echten Daten

**Empfehlung:** Mock für MVP, Creditreform für Production

**Impact:** Kostenstruktur, Datenqualität, Implementierungszeit

---

### D2: Vertragslaufzeiten

**Frage:** Welche Standard-Laufzeiten bieten wir an?

**Optionen:**
1. **Fix: 12 Monate** - Einfach und klar
2. **Flexibel: 6/12/24 Monate** - Kundenfreundlich
3. **Custom: Frei wählbar** - Maximal flexibel

**Empfehlung:** Option 2 (6/12/24 Monate)

**Begründung:** Balance zwischen Flexibilität und Komplexität

---

### D3: Vertrags-Automatisierung

**Frage:** Wie viel soll automatisch passieren?

**Optionen:**
1. **Voll-Automatisch** - Vertrag wird bei Bonität automatisch erstellt
2. **Semi-Automatisch** - Verkäufer muss bestätigen
3. **Manuell** - Verkäufer erstellt bewusst

**Empfehlung:** Semi-Automatisch mit Opt-in für Voll-Auto

**Impact:** User Experience, Fehlerquote, Akzeptanz

---

## ✅ GETROFFENE ENTSCHEIDUNGEN

### D4: PDF-Generierung ✅

**Entscheidung:** Server-seitige Generierung mit Templates

**Technologie:** 
- Apache FOP für PDF-Generierung
- Thymeleaf für HTML-Templates
- Flying Saucer als Alternative

**Begründung:**
- Konsistente Formatierung
- Offline-fähig
- Versionierbar

---

### D5: Vertrags-Speicherung ✅

**Entscheidung:** S3-kompatible Object Storage

**Details:**
- Verschlüsselt at-rest
- Versionierung aktiviert
- 7 Jahre Aufbewahrung (gesetzlich)

**Alternative für Dev:** Lokales Filesystem

---

### D6: Erinnerungs-Zeitpunkte ✅

**Entscheidung:** Gestaffelte Erinnerungen

**Timeline:**
- 90 Tage vorher: Erste Info
- 60 Tage vorher: Erinnerung
- 30 Tage vorher: Dringende Erinnerung + Task
- 14 Tage vorher: Eskalation an Manager
- 7 Tage vorher: Tägliche Erinnerung

**Konfigurierbar:** Per Customer-Settings überschreibbar

---

## 🤔 ÜBERLEGUNGEN

### Datenschutz bei Bonitätsprüfung

**Bedenken:**
- DSGVO-Konformität erforderlich
- Einwilligung des Kunden nötig?
- Speicherdauer der Daten

**Lösung:**
- In AGBs/Vertrag aufnehmen
- Opt-out Möglichkeit
- Automatische Löschung nach 180 Tagen

---

### Integration mit Buchhaltung

**Zukünftig bedenken:**
- Export zu DATEV?
- Mahnwesen-Integration?
- Zahlungseingänge tracken?

**Erstmal:** Basis-Features, Schnittstellen vorbereiten

---

## 📊 TECHNISCHE DETAILS

### Performance-Überlegungen

**Bonitätsprüfung:**
- Cache-Dauer: 90 Tage default
- Async wenn möglich
- Batch-Abfragen für Import

**PDF-Generierung:**
- Queue-basiert bei vielen Verträgen
- Pre-Generation der Templates
- CDN für fertige PDFs

---

### Security-Aspekte

**Kritische Punkte:**
1. Verträge = sensible Daten
2. Bonitätsdaten = hochsensibel
3. Manipulationsschutz nötig

**Maßnahmen:**
- Verschlüsselung überall
- Audit-Log für alle Zugriffe
- Digitale Signaturen (Phase 2)

---

## 🎯 NÄCHSTE SCHRITTE

Nach Klärung der offenen Punkte:
1. Mock Credit Service implementieren
2. PDF-Templates erstellen
3. Contract Entity + API
4. Frontend-Integration
5. Reminder-System

---

**Bei Fragen:** Team-Meeting oder Slack #freshplan-decisions