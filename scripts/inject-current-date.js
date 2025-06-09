#!/usr/bin/env node

/**
 * Injiziert das aktuelle Datum in MASTER_BRIEFING.md und PROJECT_STATE.json
 * Sollte bei jedem Claude-Start ausgeführt werden
 */

const fs = require('fs');
const path = require('path');

// Aktuelles Datum
const now = new Date();
const dateDE = now.toLocaleDateString('de-DE', { 
    day: '2-digit', 
    month: '2-digit', 
    year: 'numeric' 
});

const fullDateDE = now.toLocaleDateString('de-DE', {
    weekday: 'long',
    year: 'numeric',
    month: 'long', 
    day: 'numeric'
});

console.log(`📅 Injiziere aktuelles Datum: ${fullDateDE}`);

// Update MASTER_BRIEFING.md
try {
    let briefing = fs.readFileSync('MASTER_BRIEFING.md', 'utf8');
    
    // Ersetze INJECT_DATE Marker
    briefing = briefing.replace(/<!-- INJECT_DATE -->/g, fullDateDE);
    
    // Ersetze auch alte Datumsmuster
    briefing = briefing.replace(/\*\*Heute ist: .+?\*\*/g, `**Heute ist: ${fullDateDE}**`);
    
    fs.writeFileSync('MASTER_BRIEFING.md', briefing);
    console.log('✅ MASTER_BRIEFING.md aktualisiert');
} catch (error) {
    console.error('❌ Fehler beim Update von MASTER_BRIEFING.md:', error.message);
}

// Update PROJECT_STATE.json
try {
    let stateContent = fs.readFileSync('PROJECT_STATE.json', 'utf8');
    const state = JSON.parse(stateContent.replace(/<!-- AUTO_UPDATE_DATE -->/g, dateDE));
    
    // Update lastUpdated
    state.currentState.lastUpdated = dateDE;
    
    // Schreibe formatiert zurück
    fs.writeFileSync('PROJECT_STATE.json', JSON.stringify(state, null, 2));
    console.log('✅ PROJECT_STATE.json aktualisiert');
} catch (error) {
    console.error('❌ Fehler beim Update von PROJECT_STATE.json:', error.message);
}

// Erstelle auch eine .current-date Datei für andere Scripts
fs.writeFileSync('.current-date', fullDateDE);
console.log('✅ .current-date erstellt');

console.log(`\n💡 Tipp: Sage Claude er soll MASTER_BRIEFING.md lesen!`);
console.log(`   Das Datum "${fullDateDE}" ist dort jetzt eingetragen.`);