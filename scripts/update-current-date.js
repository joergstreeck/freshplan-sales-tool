#!/usr/bin/env node

/**
 * Aktualisiert NUR die AUTO_DATE Marker in Markdown-Dokumenten
 * 
 * Dieses Script ersetzt ausschließlich spezielle Marker mit dem aktuellen Datum.
 * Historische Daten wie "Letzte Aktualisierung" bleiben unverändert!
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
        }),
        time: now.toLocaleTimeString('de-DE', {
            hour: '2-digit',
            minute: '2-digit'
        })
    };
};

// NUR diese Marker werden ersetzt (keine historischen Daten!)
const AUTO_DATE_MARKERS = {
    '<!-- AUTO_DATE -->': () => getCurrentDate().german,
    '<!-- AUTO_DATE_ISO -->': () => getCurrentDate().iso,
    '<!-- AUTO_DATE_FULL -->': () => getCurrentDate().full,
    '<!-- CURRENT_DATE -->': () => getCurrentDate().german,
    '<!-- TODAY -->': () => getCurrentDate().german,
    '<!-- NOW -->': () => `${getCurrentDate().german}, ${getCurrentDate().time} Uhr`,
    '<!-- AUTO_UPDATE_DATE -->': () => `Dokument angezeigt am ${getCurrentDate().german}`
};

// Dateien verarbeiten
const processFile = (filePath, options = {}) => {
    try {
        let content = fs.readFileSync(filePath, 'utf8');
        let updated = false;
        const replacements = [];

        // NUR Auto-Date Marker ersetzen
        Object.entries(AUTO_DATE_MARKERS).forEach(([marker, dateFunc]) => {
            const regex = new RegExp(marker.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'g');
            const matches = content.match(regex);
            
            if (matches && matches.length > 0) {
                const replacement = dateFunc();
                content = content.replace(regex, replacement);
                updated = true;
                replacements.push({
                    marker,
                    count: matches.length,
                    replacement
                });
            }
        });

        // Datei nur schreiben wenn Änderungen vorgenommen wurden
        if (updated && !options.dryRun) {
            fs.writeFileSync(filePath, content, 'utf8');
            
            if (options.verbose) {
                console.log(`✅ ${path.relative(process.cwd(), filePath)}`);
                replacements.forEach(({ marker, count, replacement }) => {
                    console.log(`   ${marker} → ${replacement} (${count}x)`);
                });
            } else {
                console.log(`✅ ${path.relative(process.cwd(), filePath)}`);
            }
        } else if (updated && options.dryRun) {
            console.log(`🔍 ${path.relative(process.cwd(), filePath)}`);
            replacements.forEach(({ marker, count }) => {
                console.log(`   Würde ersetzen: ${marker} (${count}x)`);
            });
        }

        return { updated, replacements };
    } catch (error) {
        console.error(`❌ Fehler bei ${filePath}: ${error.message}`);
        return { updated: false, error: error.message };
    }
};

// Hauptfunktion
const main = () => {
    const args = process.argv.slice(2);
    
    // Hilfe anzeigen
    if (args.includes('--help') || args.includes('-h')) {
        console.log(`
Aktualisiert AUTO_DATE Marker in Markdown-Dokumenten

WICHTIG: Dieses Script ersetzt NUR spezielle Marker wie <!-- AUTO_DATE -->
         Historische Daten bleiben unverändert!

Verwendung:
  node update-current-date.js [optionen] [pfad]

Optionen:
  --dry-run    Zeigt was geändert würde, ohne Dateien zu ändern
  --verbose    Zeigt detaillierte Informationen
  --help, -h   Zeigt diese Hilfe

Unterstützte Marker:
  <!-- AUTO_DATE -->         ${getCurrentDate().german}
  <!-- AUTO_DATE_ISO -->     ${getCurrentDate().iso}
  <!-- AUTO_DATE_FULL -->    ${getCurrentDate().full}
  <!-- CURRENT_DATE -->      ${getCurrentDate().german}
  <!-- TODAY -->             ${getCurrentDate().german}
  <!-- NOW -->               ${getCurrentDate().german}, ${getCurrentDate().time} Uhr

Beispiele:
  node update-current-date.js                    # Alle .md Dateien
  node update-current-date.js docs/              # Nur docs/ Ordner
  node update-current-date.js --dry-run         # Testlauf
  node update-current-date.js --verbose         # Mit Details

Diese Daten werden NICHT geändert:
  - "Letzte Aktualisierung: 01.01.2025"
  - "Erstellt am: 01.01.2025"
  - "Stand: 01.01.2025"
  - Andere historische Datumsangaben
`);
        return;
    }

    const isDryRun = args.includes('--dry-run');
    const isVerbose = args.includes('--verbose');
    const searchPath = args.find(arg => !arg.startsWith('--')) || '**/*.md';

    console.log(`\n📅 Update Current Date Markers\n`);
    console.log(`🔍 Suche Markdown-Dateien: ${searchPath}`);
    
    if (isDryRun) {
        console.log(`🧪 Dry-Run Modus - keine Änderungen\n`);
    }

    // Finde alle Markdown-Dateien
    const files = glob.sync(searchPath, {
        ignore: ['**/node_modules/**', '**/dist/**', '**/build/**', '**/legacy/**']
    });

    console.log(`📝 ${files.length} Dateien gefunden\n`);

    let totalUpdated = 0;

    // Verarbeite jede Datei
    files.forEach(file => {
        const result = processFile(file, { dryRun: isDryRun, verbose: isVerbose });
        if (result.updated) {
            totalUpdated++;
        }
    });

    console.log(`\n✨ Fertig! ${totalUpdated} Dateien ${isDryRun ? 'würden aktualisiert' : 'aktualisiert'}.`);
};

// Script ausführen
if (require.main === module) {
    main();
}

module.exports = { getCurrentDate, processFile };