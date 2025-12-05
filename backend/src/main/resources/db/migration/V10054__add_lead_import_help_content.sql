-- Lead-Import Hilfe-Inhalte für das zentrale Help-System
-- Sprint 2.1.8 - User-Dokumentation

-- Haupt-Tooltip für Lead-Import Wizard
INSERT INTO help_contents (
    id,
    feature,
    help_type,
    title,
    short_content,
    medium_content,
    detailed_content,
    target_user_level,
    priority,
    is_active,
    view_count,
    helpful_count,
    not_helpful_count,
    created_at,
    updated_at
) VALUES (
    gen_random_uuid(),
    'lead-import',
    'TOOLTIP',
    'Lead-Import Wizard',
    'Importieren Sie Leads aus CSV- oder Excel-Dateien in das System.',
    'Der Import-Wizard führt Sie in 4 Schritten durch den Import-Prozess:
1. Datei hochladen (CSV oder Excel, max. 5 MB)
2. Spalten zuordnen (automatisches Mapping wird vorgeschlagen)
3. Vorschau prüfen (Duplikate und Fehler werden angezeigt)
4. Import ausführen

Bei einer Duplikat-Rate über 10% wird der Import zur Genehmigung an einen Administrator weitergeleitet.',
    'Der Lead-Import Wizard ermöglicht den einfachen Import von Kontaktdaten aus externen Quellen.

**Unterstützte Formate:**
- CSV (Komma- oder Semikolon-getrennt, UTF-8)
- Excel (.xlsx)
- Maximale Dateigröße: 5 MB
- Maximale Zeilen: 1.000 pro Import

**Pflichtfelder:**
- Firmenname (company)

**Empfohlene Felder:**
- E-Mail (email)
- Ansprechpartner (contactPerson)
- Telefon (phone)

**Duplikat-Erkennung:**
Das System erkennt Duplikate anhand von:
- Exakte E-Mail-Übereinstimmung
- Ähnlicher Firmenname (Fuzzy-Match)
- Gleiche Telefonnummer

**Genehmigungspflicht:**
Bei mehr als 10% Duplikaten wird der Import pausiert und ein Administrator muss den Import genehmigen oder ablehnen.',
    'BEGINNER',
    10,
    true,
    0,
    0,
    0,
    NOW(),
    NOW()
);

-- Schritt 1: Datei-Upload
INSERT INTO help_contents (
    id,
    feature,
    help_type,
    title,
    short_content,
    medium_content,
    detailed_content,
    target_user_level,
    priority,
    is_active,
    view_count,
    helpful_count,
    not_helpful_count,
    created_at,
    updated_at
) VALUES (
    gen_random_uuid(),
    'lead-import-upload',
    'TOOLTIP',
    'Datei hochladen',
    'Wählen Sie eine CSV- oder Excel-Datei mit Ihren Lead-Daten.',
    'Ziehen Sie eine Datei in den Upload-Bereich oder klicken Sie zum Auswählen.

**Anforderungen:**
- Format: CSV oder Excel (.xlsx)
- Max. Größe: 5 MB
- Max. Zeilen: 1.000
- Encoding: UTF-8 empfohlen',
    'Die erste Zeile Ihrer Datei sollte die Spaltenüberschriften enthalten.

**CSV-Format:**
- Trennzeichen: Komma (,) oder Semikolon (;)
- Text mit Sonderzeichen in Anführungszeichen
- Encoding: UTF-8 für Umlaute

**Excel-Format:**
- .xlsx Format (Excel 2007+)
- Erste Zeile = Überschriften
- Nur das erste Arbeitsblatt wird verwendet

**Beispiel-Spalten:**
Firma;Email;Ansprechpartner;Telefon;Branche
Müller GmbH;info@mueller.de;Hans Müller;0123-456789;Produktion',
    'BEGINNER',
    20,
    true,
    0,
    0,
    0,
    NOW(),
    NOW()
);

-- Schritt 2: Spalten-Mapping
INSERT INTO help_contents (
    id,
    feature,
    help_type,
    title,
    short_content,
    medium_content,
    detailed_content,
    target_user_level,
    priority,
    is_active,
    view_count,
    helpful_count,
    not_helpful_count,
    created_at,
    updated_at
) VALUES (
    gen_random_uuid(),
    'lead-import-mapping',
    'TOOLTIP',
    'Spalten zuordnen',
    'Ordnen Sie Ihre Dateispalten den Lead-Feldern zu.',
    'Das System erkennt automatisch passende Spalten. Prüfen und korrigieren Sie die Zuordnung bei Bedarf.

**Pflicht:** Firmenname (company)
**Empfohlen:** E-Mail, Ansprechpartner, Telefon

Nicht zugeordnete Spalten werden beim Import ignoriert.',
    'Für jede Spalte Ihrer Datei können Sie ein Zielfeld im Lead-System auswählen.

**Automatisches Mapping:**
Das System erkennt gängige Spaltenbezeichnungen wie:
- "Firma", "Company", "Unternehmen" → company
- "Email", "E-Mail", "Mail" → email
- "Telefon", "Phone", "Tel" → phone

**Verfügbare Felder:**
- company (Pflicht): Firmenname
- email: E-Mail-Adresse
- contactPerson: Hauptansprechpartner
- phone: Telefonnummer
- website: Webseite
- industry: Branche
- source: Quelle des Leads
- notes: Notizen/Bemerkungen

**Tipp:** Nutzen Sie die Vorschau, um das Mapping zu prüfen.',
    'BEGINNER',
    30,
    true,
    0,
    0,
    0,
    NOW(),
    NOW()
);

-- Schritt 3: Vorschau
INSERT INTO help_contents (
    id,
    feature,
    help_type,
    title,
    short_content,
    medium_content,
    detailed_content,
    target_user_level,
    priority,
    is_active,
    view_count,
    helpful_count,
    not_helpful_count,
    created_at,
    updated_at
) VALUES (
    gen_random_uuid(),
    'lead-import-preview',
    'TOOLTIP',
    'Vorschau prüfen',
    'Überprüfen Sie die erkannten Daten, Duplikate und eventuelle Fehler.',
    'Die Vorschau zeigt:
- **Gültige Zeilen:** Können importiert werden
- **Duplikate:** Bereits im System vorhanden
- **Fehler:** Ungültige oder fehlende Daten

Bei mehr als 10% Duplikaten ist eine Admin-Genehmigung erforderlich.',
    'Die Vorschau analysiert alle Zeilen Ihrer Datei.

**Duplikat-Erkennung:**
Duplikate werden erkannt anhand von:
1. Exakter E-Mail-Übereinstimmung
2. Ähnlichem Firmennamen (>80% Übereinstimmung)
3. Gleicher Telefonnummer

**Duplikat-Behandlung:**
- "Überspringen": Duplikate werden nicht importiert
- "Trotzdem erstellen": Erstellt neue Leads (bei Admin-Genehmigung)

**Fehler-Typen:**
- Pflichtfeld leer (company)
- Ungültiges E-Mail-Format
- Zu lange Feldwerte

**10%-Regel:**
Überschreitet die Duplikat-Rate 10%, wird der Import pausiert und ein Administrator muss entscheiden.',
    'BEGINNER',
    40,
    true,
    0,
    0,
    0,
    NOW(),
    NOW()
);

-- Schritt 4: Import ausführen
INSERT INTO help_contents (
    id,
    feature,
    help_type,
    title,
    short_content,
    medium_content,
    detailed_content,
    target_user_level,
    priority,
    is_active,
    view_count,
    helpful_count,
    not_helpful_count,
    created_at,
    updated_at
) VALUES (
    gen_random_uuid(),
    'lead-import-execute',
    'TOOLTIP',
    'Import ausführen',
    'Starten Sie den Import und verfolgen Sie den Fortschritt.',
    'Wählen Sie vor dem Start:
- **Quelle:** Herkunft der Leads (z.B. "Messe 2024")
- **Duplikat-Behandlung:** Überspringen oder erstellen

Nach dem Import sehen Sie eine Zusammenfassung mit:
- Anzahl importierter Leads
- Übersprungene Duplikate
- Eventuelle Fehler',
    'Der letzte Schritt führt den eigentlichen Import durch.

**Einstellungen:**
- **Quelle:** Optional. Beschreibt die Herkunft der Leads (z.B. "Messe Hamburg 2024", "Website-Formular")
- **Bei Duplikaten:**
  - Überspringen (Standard): Duplikate werden nicht importiert
  - Trotzdem erstellen: Erstellt neue Leads

**Import-Ablauf:**
1. Validierung aller Zeilen
2. Duplikat-Prüfung
3. Lead-Erstellung in der Datenbank
4. Ergebnis-Anzeige

**Nach dem Import:**
- Importierte Leads erscheinen sofort in der Lead-Liste
- Bei hoher Duplikat-Rate: Warten auf Admin-Genehmigung
- Bei Fehlern: Error-Report als CSV herunterladen

**Quota:**
Pro User maximal 1.000 Leads pro Tag importierbar.',
    'BEGINNER',
    50,
    true,
    0,
    0,
    0,
    NOW(),
    NOW()
);
