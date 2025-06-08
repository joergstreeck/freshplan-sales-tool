#!/usr/bin/env node

/**
 * Automatisches Datum-Update für Markdown-Dokumente
 * 
 * Dieses Script aktualisiert alle Markdown-Dateien mit dem aktuellen Datum.
 * Es sucht nach verschiedenen Datum-Mustern und ersetzt sie automatisch.
 */

const fs = require('fs');
const path = require('path');
const glob = require('glob');

// Datum-Formate
const getCurrentDate = () => {
    const now = new Date();
    return {
        german: now.toLocaleDateString('de-DE', { 
            day: '2-digit', 
            month: '2-digit', 
            year: 'numeric' 
        }),
        iso: now.toISOString().split('T')[0],
        full: now.toLocaleDateString('de-DE', { 
            weekday: 'long',
            day: 'numeric', 
            month: 'long', 
            year: 'numeric' 
        })
    };
};

// Muster für Datum-Ersetzung
const DATE_PATTERNS = [
    // **Datum:** DD.MM.YYYY
    {
        pattern: /\*\*Datum:\*\* \d{2}\.\d{2}\.\d{4}/g,
        replacement: () => `**Datum:** ${getCurrentDate().german}`
    },
    // **Letzte Aktualisierung:** DD.MM.YYYY
    {
        pattern: /\*\*Letzte Aktualisierung:\*\* \d{2}\.\d{2}\.\d{4}/g,
        replacement: () => `**Letzte Aktualisierung:** ${getCurrentDate().german}`
    },
    // **Stand:** DD.MM.YYYY
    {
        pattern: /\*\*Stand:\*\* \d{2}\.\d{2}\.\d{4}/g,
        replacement: () => `**Stand:** ${getCurrentDate().german}`
    },
    // **Erstellt am:** DD.MM.YYYY
    {
        pattern: /\*\*Erstellt am:\*\* \d{2}\.\d{2}\.\d{4}/g,
        replacement: () => `**Erstellt am:** ${getCurrentDate().german}`
    },
    // *Letzte Änderung:* YYYY-MM-DD
    {
        pattern: /\*Letzte Änderung:\* \d{4}-\d{2}-\d{2}/g,
        replacement: () => `*Letzte Änderung:* ${getCurrentDate().iso}`
    },
    // ---\n*Letzte Aktualisierung: DD.MM.YYYY*
    {
        pattern: /---\n\*Letzte Aktualisierung: \d{2}\.\d{2}\.\d{4}\*/g,
        replacement: () => `---\n*Letzte Aktualisierung: ${getCurrentDate().german}*`
    }
];

// Spezielle Marker für automatische Datumsfelder
const AUTO_DATE_MARKERS = {
    '<!-- AUTO_DATE -->': getCurrentDate().german,
    '<!-- AUTO_DATE_ISO -->': getCurrentDate().iso,
    '<!-- AUTO_DATE_FULL -->': getCurrentDate().full,
    '<!-- AUTO_UPDATE_DATE -->': `Automatisch aktualisiert am ${getCurrentDate().german}`
};

// Dateien verarbeiten
const processFile = (filePath) => {
    try {
        let content = fs.readFileSync(filePath, 'utf8');
        let updated = false;

        // Datum-Muster ersetzen
        DATE_PATTERNS.forEach(({ pattern, replacement }) => {
            const newContent = content.replace(pattern, replacement());
            if (newContent !== content) {
                content = newContent;
                updated = true;
            }
        });

        // Auto-Date Marker ersetzen
        Object.entries(AUTO_DATE_MARKERS).forEach(([marker, date]) => {
            if (content.includes(marker)) {
                content = content.replace(new RegExp(marker, 'g'), date);
                updated = true;
            }
        });

        // Datei nur schreiben wenn Änderungen vorgenommen wurden
        if (updated) {
            fs.writeFileSync(filePath, content, 'utf8');
            console.log(`✅ Aktualisiert: ${path.relative(process.cwd(), filePath)}`);
        }
    } catch (error) {
        console.error(`❌ Fehler bei ${filePath}: ${error.message}`);
    }
};

// Hauptfunktion
const main = () => {
    const args = process.argv.slice(2);
    
    // Hilfe anzeigen
    if (args.includes('--help') || args.includes('-h')) {
        console.log(`
Automatisches Datum-Update für Markdown-Dokumente

Verwendung:
  node update-doc-dates.js [optionen] [pfad]

Optionen:
  --dry-run    Zeigt Änderungen ohne Dateien zu modifizieren
  --help, -h   Zeigt diese Hilfe

Beispiele:
  node update-doc-dates.js                    # Alle .md Dateien
  node update-doc-dates.js docs/              # Nur docs/ Ordner
  node update-doc-dates.js --dry-run         # Testlauf

Unterstützte Datum-Muster:
  **Datum:** DD.MM.YYYY
  **Letzte Aktualisierung:** DD.MM.YYYY
  **Stand:** DD.MM.YYYY
  <!-- AUTO_DATE -->
  <!-- AUTO_DATE_ISO -->
`);
        return;
    }

    const isDryRun = args.includes('--dry-run');
    const searchPath = args.find(arg => !arg.startsWith('--')) || '**/*.md';

    console.log(`\n🔍 Suche Markdown-Dateien: ${searchPath}\n`);

    // Finde alle Markdown-Dateien
    const files = glob.sync(searchPath, {
        ignore: ['**/node_modules/**', '**/dist/**', '**/build/**', '**/legacy/**']
    });

    console.log(`📝 Gefunden: ${files.length} Dateien\n`);

    // Verarbeite jede Datei
    files.forEach(file => {
        if (isDryRun) {
            console.log(`🔍 Würde prüfen: ${file}`);
        } else {
            processFile(file);
        }
    });

    console.log('\n✨ Fertig!');
};

// Script ausführen
if (require.main === module) {
    main();
}

module.exports = { getCurrentDate, processFile };