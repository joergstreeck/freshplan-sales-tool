#!/usr/bin/env node

/**
 * Einfaches Script zum Aktualisieren von AUTO_DATE Markern
 * Ohne externe Dependencies
 */

const fs = require('fs');
const path = require('path');

// Aktuelles Datum
const today = new Date();
const dateDE = today.toLocaleDateString('de-DE', { 
    day: '2-digit', 
    month: '2-digit', 
    year: 'numeric' 
});

console.log(`📅 Aktualisiere Dokumentations-Datum auf: ${dateDE}`);
console.log('');

// Funktion zum rekursiven Durchsuchen von Verzeichnissen
function findMarkdownFiles(dir, fileList = []) {
    const files = fs.readdirSync(dir);
    
    files.forEach(file => {
        const filePath = path.join(dir, file);
        const stat = fs.statSync(filePath);
        
        // Ignoriere bestimmte Verzeichnisse
        if (stat.isDirectory()) {
            if (!file.includes('node_modules') && 
                !file.includes('.git') && 
                !file.includes('dist') &&
                !file.includes('target') &&
                !file.includes('build')) {
                findMarkdownFiles(filePath, fileList);
            }
        } else if (file.endsWith('.md')) {
            fileList.push(filePath);
        }
    });
    
    return fileList;
}

// Hauptfunktion
function updateDates() {
    const rootDir = process.cwd();
    const mdFiles = findMarkdownFiles(rootDir);
    
    console.log(`Gefunden: ${mdFiles.length} Markdown-Dateien\n`);
    
    let updatedCount = 0;
    
    mdFiles.forEach(file => {
        try {
            let content = fs.readFileSync(file, 'utf8');
            const originalContent = content;
            
            // Ersetze AUTO_DATE Marker
            content = content.replace(/<!-- AUTO_DATE -->/g, dateDE);
            
            // Ersetze auch das Pattern aus CLAUDE.md und anderen Dateien
            // "**📅 Aktuelles Datum: <!-- AUTO_DATE --> (System: DD.MM.YYYY)**"
            const systemDatePattern = /\(System: \d{2}\.\d{2}\.\d{4}\)/g;
            content = content.replace(systemDatePattern, `(System: ${dateDE})`);
            
            // Wenn sich was geändert hat, speichern
            if (content !== originalContent) {
                fs.writeFileSync(file, content, 'utf8');
                console.log(`✅ ${path.relative(rootDir, file)}`);
                updatedCount++;
            }
        } catch (error) {
            console.error(`❌ Fehler bei ${file}: ${error.message}`);
        }
    });
    
    console.log(`\n✨ Fertig! ${updatedCount} Dateien aktualisiert.`);
}

// Ausführen
updateDates();