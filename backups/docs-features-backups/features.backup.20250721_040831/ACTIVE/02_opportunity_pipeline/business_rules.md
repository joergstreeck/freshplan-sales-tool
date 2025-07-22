# 📋 Business Rules - Opportunity Pipeline

**Status:** 🚨 DRAFT - Muss mit Product Owner validiert werden  
**Letzte Aktualisierung:** 12.07.2025

## 🔄 Stage Transition Matrix

### Erlaubte Übergänge

| Von Stage | Nach Stage | Bedingungen | Auto-Aktionen |
|-----------|------------|-------------|---------------|
| NEW_LEAD | QUALIFICATION | - Keine | - E-Mail an Verkäufer |
| NEW_LEAD | CLOSED_LOST | - Grund erforderlich | - In Archiv |
| QUALIFICATION | NEEDS_ANALYSIS | - Kunde zugeordnet<br>- Budget geklärt | - Kalender-Eintrag |
| QUALIFICATION | CLOSED_LOST | - Grund erforderlich | - Follow-up in 6 Monaten |
| NEEDS_ANALYSIS | PROPOSAL | - Min. 1 Kontaktperson<br>- Anforderungen dokumentiert | - Calculator öffnet automatisch |
| NEEDS_ANALYSIS | QUALIFICATION | - Wenn Info fehlt | - Notification an Verkäufer |
| PROPOSAL | NEGOTIATION | - Angebot erstellt<br>- Angebot versendet | - Wiedervorlage in 7 Tagen |
| PROPOSAL | CLOSED_WON | - Bei Direkt-Zusage | - Kunde erstellen<br>- Provision berechnen |
| PROPOSAL | CLOSED_LOST | - Grund erforderlich | - Analyse-Dashboard |
| NEGOTIATION | CLOSED_WON | - Finale Konditionen | - Auftrag an Xentral<br>- Team-Notification |
| NEGOTIATION | CLOSED_LOST | - Grund erforderlich | - Win/Loss Analyse |
| NEGOTIATION | PROPOSAL | - Neues Angebot nötig | - Calculator öffnet |

### 🚫 Verbotene Übergänge
- CLOSED_WON → Jede andere Stage (Finale Stage!)
- CLOSED_LOST → Jede andere Stage (Finale Stage!)
- Direkte Sprünge über 2+ Stages (außer zu CLOSED_*)

## 👥 Berechtigungen

### Wer darf Opportunities sehen?

| Rolle | Eigene | Team | Alle | Bedingungen |
|-------|--------|------|------|-------------|
| Sales Rep | ✅ | ✅ | ❌ | Nur eigenes Team |
| Team Lead | ✅ | ✅ | ❌ | Alle im Team |
| Sales Manager | ✅ | ✅ | ✅ | Alle Opportunities |
| Admin | ✅ | ✅ | ✅ | Vollzugriff |
| Viewer | ❌ | ❌ | ✅ | Nur Lesen, keine PII |

### Wer darf Stage ändern?

| Rolle | Eigene | Team | Alle |
|-------|--------|------|------|
| Sales Rep | ✅ | ❌ | ❌ |
| Team Lead | ✅ | ✅* | ❌ |
| Sales Manager | ✅ | ✅ | ✅ |

*Mit Begründung und Notification an Original-Verkäufer

## 💰 Provisions-Regeln

### Provisions-Zuteilung bei Stage-Wechsel

| Stage | Provisions-Anteil | Bedingung |
|-------|------------------|-----------|
| NEW_LEAD → QUALIFICATION | 10% | Erster Kontakt |
| QUALIFICATION → NEEDS_ANALYSIS | 20% | Qualifizierung |
| NEEDS_ANALYSIS → PROPOSAL | 30% | Anforderungsanalyse |
| PROPOSAL → CLOSED_WON | 40% | Abschluss |

### Sonderfälle:
- **Übernahme:** Bei Verkäuferwechsel behält Original-Verkäufer seine bis dahin erarbeiteten Prozente
- **Team-Arbeit:** Explizite Aufteilung durch Team Lead
- **Automatische Leads:** Keine Provision für NEW_LEAD Stage

## 📅 Zeitliche Regeln

### Maximale Verweildauer pro Stage

| Stage | Max. Tage | Aktion bei Überschreitung |
|-------|-----------|---------------------------|
| NEW_LEAD | 3 | Alert an Verkäufer |
| QUALIFICATION | 7 | Alert an Team Lead |
| NEEDS_ANALYSIS | 14 | Review Meeting |
| PROPOSAL | 21 | Eskalation an Manager |
| NEGOTIATION | 30 | Strategic Review |

### Automatische Aktionen

| Trigger | Aktion |
|---------|--------|
| 7 Tage keine Aktivität | Erinnerung an Verkäufer |
| 14 Tage keine Aktivität | Alert an Team Lead |
| 30 Tage keine Aktivität | Opportunity wird "dormant" |
| Stage = PROPOSAL | Follow-up Task in 3 Tagen |

## 🔒 Verkäuferschutz-Integration

### Schutz-Level nach Stage

| Stage | Schutz-Level | Bedeutung |
|-------|--------------|-----------|
| NEW_LEAD | Niedrig | 3 Tage Exklusivität |
| QUALIFICATION | Mittel | 7 Tage, dann Team-Pool |
| NEEDS_ANALYSIS+ | Hoch | Fest zugeordnet |
| CLOSED_* | Permanent | Unveränderlich |

### Konflikte

**Bei Doppel-Anlage:**
1. System prüft: Existiert Kunde bereits?
2. Wenn ja: Opportunity dem Erst-Bearbeiter zuordnen
3. Notification an beide Verkäufer
4. Team Lead entscheidet bei Konflikt

## 📊 Pflichtfelder pro Stage

### NEW_LEAD
- Name* (Firma oder Kontakt)
- Quelle* (Woher kommt der Lead?)
- Kontaktdaten (E-Mail oder Telefon)

### QUALIFICATION
- Kunde oder Firma*
- Budget-Range*
- Entscheider identifiziert*
- Bedarf grob erfasst*

### NEEDS_ANALYSIS
- Detaillierte Anforderungen*
- Projekt-Timeline*
- Entscheidungskriterien*
- Competition bekannt

### PROPOSAL
- Angebotssumme*
- Gültigkeit*
- Konditionen*
- Nächste Schritte*

## ❓ NOCH ZU KLÄREN

1. **Stornierung:** Kann eine CLOSED_WON Opportunity storniert werden?
2. **Splitting:** Kann eine Opportunity in mehrere geteilt werden?
3. **Merging:** Können mehrere Opportunities zusammengeführt werden?
4. **Währungen:** Multi-Currency Support nötig?
5. **Approval-Workflow:** Brauchen große Deals Freigabe?
6. **Forecasting:** Wie berechnet sich die Probability?
7. **Custom Fields:** Welche kundenspezifischen Felder?

---

**WICHTIG:** Diese Rules sind ein ENTWURF basierend auf Best Practices. Ein Workshop mit dem Sales-Team ist zwingend erforderlich!