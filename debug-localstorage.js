// Debug-Script für die Browser-Konsole
// Kopieren Sie diesen Code und fügen Sie ihn in die Browser-Konsole ein

// 1. Prüfe localStorage
const stored = localStorage.getItem('focus-list-store');
if (!stored) {
    console.log('❌ Kein focus-list-store im localStorage gefunden!');
} else {
    const data = JSON.parse(stored);
    console.log('✅ localStorage Daten gefunden:', data);
    
    if (data.state && data.state.tableColumns) {
        console.log('📊 Spalten im localStorage:');
        data.state.tableColumns.forEach(col => {
            console.log(`  - ${col.label}: visible=${col.visible}, order=${col.order}`);
        });
    } else {
        console.log('❌ Keine tableColumns im localStorage!');
    }
}

// 2. Test: Manuell speichern und reload
console.log('\n🧪 TEST: Ändere erste Spalte manuell...');
if (stored) {
    const data = JSON.parse(stored);
    if (data.state && data.state.tableColumns && data.state.tableColumns[0]) {
        const firstCol = data.state.tableColumns[0];
        firstCol.visible = !firstCol.visible;
        localStorage.setItem('focus-list-store', JSON.stringify(data));
        console.log(`✅ Spalte "${firstCol.label}" auf visible=${firstCol.visible} gesetzt`);
        console.log('🔄 Lade die Seite neu (F5) und führe dieses Script erneut aus');
    }
}