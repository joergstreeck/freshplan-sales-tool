# Way of Working - FreshPlan Sales Tool

## Sprache und Benennung

**Grundregel: Klarheit vor Komplexität**

Alle Begriffe im UI und in der Dokumentation müssen einfach und verständlich sein. Englische Fachbegriffe und Berater-Deutsch sind zu vermeiden. Wir sprechen die Sprache unserer Nutzer - klar, direkt und ohne unnötige Fremdwörter.

### Unsere Sprachprinzipien:

1. **Deutsche Begriffe bevorzugen**: Verwende verständliche deutsche Bezeichnungen statt englischer Fachbegriffe
2. **Einfachheit**: Wähle den einfachsten Begriff, der die Funktion klar beschreibt
3. **Nutzerorientierung**: Benenne Funktionen aus Sicht des Nutzers, nicht aus technischer Sicht
4. **Konsistenz**: Verwende einmal gewählte Begriffe durchgängig im gesamten System

### Konkrete Beispiele:

| ❌ Vermeiden | ✅ Verwenden | Begründung |
|--------------|--------------|------------|
| Triage-Inbox | Posteingang für neue E-Mails | Direkt verständlich |
| Customer 360° View | Kundenakte | Klarer deutscher Begriff |
| Hardening & Optimization | Stabilisierung & Verbesserung | Keine Anglizismen |
| Lead Scoring | Kundenbewertung | Selbsterklärend |
| Dashboard | Übersicht / Cockpit | Etablierter deutscher Begriff |
| Activity Timeline | Verlauf / Kundenhistorie | Verständlicher |
| Pipeline | Verkaufsprozess | Beschreibt die Funktion |
| Opportunity | Verkaufschance | Klarer Bezug zum Geschäft |
| Feature Flag | Funktionsschalter | Technisch korrekt, aber verständlich |
| Backend/Frontend | Server/Benutzeroberfläche (in Nutzer-Docs) | Für technische Docs OK, für Nutzer übersetzen |

### Ausnahmen:

Technische Dokumentation für Entwickler darf etablierte Fachbegriffe verwenden (z.B. API, REST, Docker), aber auch hier gilt: Wenn ein guter deutscher Begriff existiert, diesen bevorzugen.

### Umsetzung:

1. **Bei neuen Features**: Begriff-Checkliste vor Implementierung
2. **Code-Reviews**: Prüfung auf verständliche Benennung
3. **UI-Texte**: Immer aus Nutzersicht formulieren
4. **Fehlermeldungen**: Klar und handlungsorientiert
5. **Dokumentation**: Zielgruppengerecht (Nutzer vs. Entwickler)

Diese Regel gilt ab sofort für alle neuen Entwicklungen und wird schrittweise auch in bestehenden Bereichen umgesetzt.