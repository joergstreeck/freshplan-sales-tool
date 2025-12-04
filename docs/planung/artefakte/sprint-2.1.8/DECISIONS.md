# Sprint 2.1.8 - Entscheidungen & Festlegungen

**Stand:** 2025-12-04
**Session:** Sprint-Planung mit Product Owner

---

## Übersicht der Entscheidungen

| ID | Thema | Entscheidung | Begründung |
|----|-------|--------------|------------|
| D1 | Scope | Full Sprint | Claude 4-5x schneller als Menschen |
| D2 | PDF Library | Apache PDFBox | Apache 2.0 Lizenz, keine GPL-Einschränkungen |
| D3 | Import-Modell | Quota + Auto-Approval | Self-Service mit Eskalation bei Problemen |
| D4 | DSGVO-Löschung | Soft-Delete + PII-Anonymisierung | Legal compliant, Audit-Trail erhalten |

---

## D1: Sprint-Scope

**Frage:** Sollen wir den Sprint-Scope reduzieren?

**Entscheidung:** Nein, Full Sprint

**Begründung (Product Owner):**
> "Du unterschätzt systematisch deine Geschwindigkeit. Du bist 4-5 mal schneller als menschliche Programmierer."

**Scope:**
- Phase 1: DSGVO-Kern (Art. 15, 17, 7.3)
- Phase 2: Lead-Import mit Quota-System
- Phase 3: Admin-UI + Routing
- Phase 4: Advanced Search + BANT

---

## D2: PDF Library

**Frage:** Welche PDF-Library für DSGVO-Datenexport (Art. 15)?

**Optionen:**
1. **iText 7** - Umfangreich, aber AGPL-Lizenz (problematisch für kommerzielle Nutzung)
2. **Apache PDFBox** - Apache 2.0 Lizenz, gute Quarkus-Integration
3. **OpenPDF** - LGPL, Fork von iText 4

**Entscheidung:** Apache PDFBox

**Begründung:**
- Apache 2.0 Lizenz = keine Einschränkungen
- Bereits in Quarkus-Ökosystem bewährt
- Ausreichend für einfache Datenexport-PDFs
- Keine Abhängigkeit von proprietären Features

**Maven Dependency:**
```xml
<dependency>
    <groupId>org.apache.pdfbox</groupId>
    <artifactId>pdfbox</artifactId>
    <version>3.0.0</version>
</dependency>
```

---

## D3: Import-Modell

**Frage:** Wie soll der Lead-Import funktionieren? Brauchen Verkäufer eine Freigabe?

**Optionen:**
1. **Approval-Required:** Jeder Import muss von Manager/Admin freigegeben werden
2. **Self-Service:** Jeder User kann frei importieren (keine Limits)
3. **Quota + Auto-Approval:** Self-Service mit Limits, Eskalation bei Problemen

**Entscheidung:** Quota + Auto-Approval

**Begründung (Best Practice):**
- Self-Service ermöglicht schnelles Arbeiten (Messe → sofort importieren)
- Quota-System verhindert Missbrauch
- Auto-Approval bei <10% Duplikaten = schneller Workflow
- Eskalation bei ≥10% Duplikaten = Qualitätssicherung

### Quota-Limits

| Rolle | Max. Offene Leads | Imports/Tag | Leads/Import |
|-------|-------------------|-------------|--------------|
| SALES | 100 | 3 | 100 |
| MANAGER | 200 | 5 | 200 |
| ADMIN | ∞ | ∞ | 1000 |

### Auto-Approval Regel

```
IF duplicate_rate < 10%
   → Auto-Approve (Import sofort)
ELSE
   → Escalation an MANAGER/ADMIN
   → Notification: "Import wartet auf Freigabe"
```

**Rationale:**
- SALES: Fokus auf Qualität, nicht Quantität
- MANAGER: Mehr Kapazität für Team-Leads
- ADMIN: Keine Einschränkungen für System-Imports

---

## D4: DSGVO-Löschung (Art. 17)

**Frage:** Hard-Delete oder Soft-Delete bei DSGVO-Löschung?

**Optionen:**
1. **Hard-Delete:** Komplette Löschung aus Datenbank
2. **Soft-Delete:** Nur Flag setzen, Daten behalten
3. **Soft-Delete + PII-Anonymisierung:** Flag + personenbezogene Daten anonymisieren

**Entscheidung:** Soft-Delete + PII-Anonymisierung

**Rechtsgrundlage:**

DSGVO Art. 17 ("Recht auf Vergessenwerden"):
- Verpflichtet zur Löschung **personenbezogener Daten**
- Erlaubt **explizit** Aufbewahrung für:
  - Rechtliche Verpflichtungen (Art. 17 Abs. 3b)
  - Geltendmachung von Rechtsansprüchen (Art. 17 Abs. 3e)

**Umsetzung:**

```java
// PII wird anonymisiert:
lead.setCompanyName("DSGVO-GELÖSCHT-" + lead.getId().substring(0,8));
lead.setEmail(null);
lead.setPhone(null);
lead.setContactPerson(null);
lead.setStreet(null);
lead.setCity(null);
lead.setPostalCode(null);
lead.setNotes(null);

// Audit-Trail bleibt:
lead.setGdprDeleted(true);
lead.setGdprDeletedAt(Instant.now());
lead.setGdprDeletedBy(currentUserId);
lead.setGdprDeletionReason(reason);
```

**Vorteile:**
- ✅ DSGVO-konform (PII gelöscht)
- ✅ Audit-Trail erhalten (für Compliance-Audits)
- ✅ Referentielle Integrität (keine broken Foreign Keys)
- ✅ Statistiken bleiben intakt (Lead-Counts)

---

## D5: Admin-Routen

**Frage:** Welche Admin-Routen brauchen wir?

**Bestandsaufnahme (existierend):**
- `/admin` - Admin Dashboard
- `/admin/audit` - Audit-Logs mit Compliance-Tab
- `/admin/users` - Benutzerverwaltung
- `/admin/system` - Systemeinstellungen
- `/admin/integrationen` - Xentral etc.

**Neue Routen:**
- `/admin/dsgvo` - DSGVO-Verwaltung
- `/admin/imports` - Import-Verwaltung

### /admin/dsgvo

**Tabs:**
1. **Datenauskunfts-Anfragen:** Offene Art. 15 Anfragen, PDF-Downloads
2. **Löschprotokoll:** Art. 17 Löschungen mit Audit-Trail
3. **Gesperrte Kontakte:** Art. 7.3 Sperrliste

### /admin/imports

**Tabs:**
1. **Import-Historie:** Alle Imports mit Status
2. **Wartende Freigaben:** Imports mit >10% Duplikaten
3. **Quota-Übersicht:** User-Quotas und Auslastung

---

## D6: Technische Architektur

### Services (Backend)

```
GdprService
├── generateDataExport()      # Art. 15
├── gdprDelete()              # Art. 17
└── revokeConsent()           # Art. 7.3

LeadImportService
├── uploadFile()
├── previewImport()
├── executeImport()
└── downloadErrorReport()

ImportQuotaService
├── checkQuota()
└── getQuotaLimits()

PdfGeneratorService
└── generateGdprExport()      # Apache PDFBox
```

### Migration-Reihenfolge

1. `V{N}_add_gdpr_fields.sql` - DSGVO-Felder in leads
2. `V{N+1}_add_gdpr_tables.sql` - gdpr_data_requests, gdpr_deletion_logs
3. `V{N+2}_add_import_tracking.sql` - import_logs

---

## Nicht-Entscheidungen (Deferred)

### Advanced Search & BANT

**Status:** Verschoben in Phase 4

**Grund:** DSGVO und Lead-Import haben höhere Priorität (gesetzliche Pflicht vs. Nice-to-have)

**Geplanter Umfang:**
- PostgreSQL Full-Text-Search mit pg_trgm
- BANT-Felder (Budget, Authority, Need, Timeline)
- BANT-Score-Berechnung (0-4)

---

## Referenzen

- DSGVO Art. 15: https://dsgvo-gesetz.de/art-15-dsgvo/
- DSGVO Art. 17: https://dsgvo-gesetz.de/art-17-dsgvo/
- DSGVO Art. 7: https://dsgvo-gesetz.de/art-7-dsgvo/
- Apache PDFBox: https://pdfbox.apache.org/
