#!/bin/bash

# FreshPlan Feature Briefing Generator
# Erstellt standardisierte Feature-Briefings für konsistente Entwicklung

if [ -z "$1" ]; then
    echo "Usage: $0 <feature-name>"
    echo "Example: $0 user-export"
    exit 1
fi

FEATURE=$1
DATE=$(date +%Y-%m-%d)
TIME=$(date +%H:%M)
FEATURE_DIR="docs/features/$FEATURE"

echo "🚀 FreshPlan Feature Briefing Generator"
echo "======================================"
echo ""
echo "Feature: $FEATURE"
echo "Datum: $DATE $TIME"
echo ""

# Erstelle Feature-Verzeichnis
mkdir -p "$FEATURE_DIR"

# Generiere Feature-Briefing
cat > "$FEATURE_DIR/FEATURE_BRIEFING.md" << 'EOF'
# Feature Briefing: ${FEATURE}

**Erstellt:** ${DATE} ${TIME}
**Status:** 🔵 Planning
**Verantwortlich:** [Name eintragen]

## 📋 Feature-Übersicht

### Beschreibung
[Was soll das Feature tun? Warum wird es benötigt?]

### Betroffene Komponenten
- [ ] Frontend
- [ ] Backend  
- [ ] Database
- [ ] API
- [ ] Infrastructure

### Geschätzter Aufwand
- Planning: [ ] Stunden
- Implementation: [ ] Stunden
- Testing: [ ] Stunden
- Documentation: [ ] Stunden

## 🔒 Security Checklist

### Authentifizierung & Autorisierung
- [ ] Welche Rollen haben Zugriff?
- [ ] Sind alle Endpoints geschützt?
- [ ] Token-Validierung implementiert?

### Daten-Sicherheit
- [ ] Input-Validierung geplant?
- [ ] Output-Encoding vorgesehen?
- [ ] SQL-Injection verhindert?
- [ ] XSS-Protection implementiert?

### Sensitive Daten
- [ ] Werden personenbezogene Daten verarbeitet?
- [ ] Verschlüsselung notwendig?
- [ ] Logging ohne sensitive Daten?
- [ ] DSGVO-Konformität geprüft?

## 🧪 Test-Strategie

### Unit Tests
- [ ] Service-Layer Tests
- [ ] Utility-Funktionen Tests
- [ ] Edge-Cases definiert
- **Ziel-Coverage:** ≥ 80%

### Integration Tests
- [ ] API-Endpoints
- [ ] Database-Operationen
- [ ] External Services

### E2E Tests
- [ ] Happy Path
- [ ] Error Cases
- [ ] Performance unter Last

### Security Tests
- [ ] Penetration Testing nötig?
- [ ] OWASP Top 10 geprüft?

## 🏗️ Implementierungs-Plan

### Phase 1: Foundation
- [ ] API-Design dokumentieren
- [ ] Datenmodell entwerfen
- [ ] Security-Konzept erstellen

### Phase 2: Backend
- [ ] Entities/Models erstellen
- [ ] Service-Layer implementieren
- [ ] API-Endpoints erstellen
- [ ] Tests schreiben

### Phase 3: Frontend
- [ ] UI-Design abstimmen
- [ ] Komponenten erstellen
- [ ] State Management
- [ ] API-Integration

### Phase 4: Integration
- [ ] End-to-End Tests
- [ ] Performance-Optimierung
- [ ] Security-Review
- [ ] Dokumentation

## 📊 Akzeptanzkriterien

### Funktional
- [ ] [Kriterium 1]
- [ ] [Kriterium 2]
- [ ] [Kriterium 3]

### Nicht-Funktional
- [ ] Performance: Response < 200ms
- [ ] Security: Keine High/Critical Vulnerabilities
- [ ] Usability: Intuitive Bedienung
- [ ] Code-Quality: Linter-konform

## ⚠️ Risiken & Abhängigkeiten

### Risiken
1. [Risiko identifizieren]
   - Eintrittswahrscheinlichkeit: Hoch/Mittel/Niedrig
   - Impact: Hoch/Mittel/Niedrig
   - Mitigation: [Strategie]

### Abhängigkeiten
- [ ] Externe APIs verfügbar?
- [ ] Team-Ressourcen?
- [ ] Andere Features?

## 📝 Dokumentations-Anforderungen

### Technische Dokumentation
- [ ] API-Dokumentation (OpenAPI/Swagger)
- [ ] Code-Kommentare (JSDoc/JavaDoc)
- [ ] Architecture Decision Record (ADR)

### User-Dokumentation
- [ ] Benutzerhandbuch-Update
- [ ] Release Notes
- [ ] Migration Guide (falls nötig)

## 🚀 Definition of Done

- [ ] Code implemented und reviewed
- [ ] Alle Tests grün (Unit, Integration, E2E)
- [ ] Security-Check bestanden
- [ ] Performance-Ziele erreicht
- [ ] Dokumentation vollständig
- [ ] Deployed auf Staging
- [ ] Product Owner Abnahme

## 📅 Timeline

| Phase | Start | Ende | Status |
|-------|-------|------|--------|
| Planning | ${DATE} | | 🔵 In Progress |
| Implementation | | | ⚪ Not Started |
| Testing | | | ⚪ Not Started |
| Deployment | | | ⚪ Not Started |

## 🔗 Referenzen

- Issue/Ticket: #[Nummer]
- Design Mockups: [Link]
- API Spec: [Link]
- Related Features: [Links]

---

## 📌 Notizen & Updates

### ${DATE} - Initial Briefing
- Feature-Briefing erstellt
- Warte auf Team-Input

[Weitere Updates hier hinzufügen]

EOF

# Ersetze Variablen
sed -i.bak "s/\${FEATURE}/$FEATURE/g" "$FEATURE_DIR/FEATURE_BRIEFING.md"
sed -i.bak "s/\${DATE}/$DATE/g" "$FEATURE_DIR/FEATURE_BRIEFING.md"
sed -i.bak "s/\${TIME}/$TIME/g" "$FEATURE_DIR/FEATURE_BRIEFING.md"
rm "$FEATURE_DIR/FEATURE_BRIEFING.md.bak"

# Erstelle Test-Template
cat > "$FEATURE_DIR/TEST_PLAN.md" << EOF
# Test Plan: $FEATURE

## Test-Szenarien

### 1. Happy Path
\`\`\`gherkin
Given: [Vorbedingung]
When: [Aktion]
Then: [Erwartetes Ergebnis]
\`\`\`

### 2. Edge Cases
- [ ] Leere Eingaben
- [ ] Maximale Werte
- [ ] Minimale Werte
- [ ] Sonderzeichen

### 3. Error Cases
- [ ] Netzwerkfehler
- [ ] Timeout
- [ ] Ungültige Daten
- [ ] Fehlende Berechtigung

## Test-Daten
[Definiere Test-Datensätze]

## Test-Umgebung
- Frontend: [URL]
- Backend: [URL]
- Database: [Connection]
EOF

# Erstelle Security-Checklist
cat > "$FEATURE_DIR/SECURITY_CHECKLIST.md" << EOF
# Security Checklist: $FEATURE

## 🔐 Authentication & Authorization
- [ ] Alle Endpoints erfordern Authentifizierung
- [ ] Rollen-basierte Zugriffskontrolle implementiert
- [ ] Token-Expiration geprüft
- [ ] Session-Management sicher

## 🛡️ Input Validation
- [ ] Alle Eingaben werden validiert
- [ ] Whitelist-Ansatz verwendet
- [ ] Längen-Limits definiert
- [ ] Datentypen geprüft

## 💉 Injection Prevention
- [ ] SQL-Injection verhindert (Prepared Statements)
- [ ] NoSQL-Injection verhindert
- [ ] Command-Injection verhindert
- [ ] XSS-Prevention implementiert

## 🔒 Data Protection
- [ ] Sensitive Daten verschlüsselt
- [ ] HTTPS enforced
- [ ] Keine Secrets im Code
- [ ] Logging ohne sensitive Daten

## 🚨 Error Handling
- [ ] Keine Stack-Traces in Production
- [ ] Generische Fehlermeldungen
- [ ] Rate-Limiting implementiert
- [ ] Monitoring aktiviert

Geprüft von: ________________
Datum: $DATE
EOF

echo "✅ Feature-Briefing erstellt!"
echo ""
echo "📁 Erstellte Dateien:"
echo "   - $FEATURE_DIR/FEATURE_BRIEFING.md"
echo "   - $FEATURE_DIR/TEST_PLAN.md"
echo "   - $FEATURE_DIR/SECURITY_CHECKLIST.md"
echo ""
echo "📋 Nächste Schritte:"
echo "   1. Öffne FEATURE_BRIEFING.md und fülle die Details aus"
echo "   2. Bespreche das Briefing mit dem Team"
echo "   3. Beginne erst NACH Freigabe mit der Implementierung"
echo ""
echo "💡 Denke daran: Security First, Test First, Document First!"