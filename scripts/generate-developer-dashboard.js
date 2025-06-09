#!/usr/bin/env node

/**
 * Generiert ein umfassendes HTML-Dashboard für menschliche Entwickler
 * Kombiniert Daten aus verschiedenen Quellen
 */

const fs = require('fs');
const path = require('path');
const { exec } = require('child_process');
const util = require('util');
const execPromise = util.promisify(exec);

async function generateDashboard() {
    console.log('🔨 Generiere Developer Dashboard...');
    
    // Lade PROJECT_STATE.json
    const projectState = JSON.parse(fs.readFileSync('PROJECT_STATE.json', 'utf8'));
    
    // Aktuelles Datum
    const now = new Date();
    const dateTime = now.toLocaleString('de-DE');
    
    // Sammle Git-Statistiken
    let gitStats = {
        branch: 'main',
        lastCommit: 'unbekannt',
        uncommittedChanges: 0
    };
    
    try {
        const { stdout: branch } = await execPromise('git branch --show-current');
        gitStats.branch = branch.trim();
        
        const { stdout: lastCommit } = await execPromise('git log -1 --pretty=format:"%h - %s (%cr)"');
        gitStats.lastCommit = lastCommit;
        
        const { stdout: changes } = await execPromise('git status --porcelain | wc -l');
        gitStats.uncommittedChanges = parseInt(changes.trim());
    } catch (e) {
        console.warn('Git-Statistiken konnten nicht geladen werden');
    }
    
    // Generiere HTML
    const html = `<!DOCTYPE html>
<html lang="de">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>FreshPlan Developer Dashboard</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <style>
        :root {
            --primary: #004F7B;
            --secondary: #94C456;
            --success: #4CAF50;
            --warning: #FF9800;
            --danger: #F44336;
            --bg: #f5f7fa;
            --card-bg: #ffffff;
            --text: #2c3e50;
            --text-light: #718096;
            --border: #e2e8f0;
        }
        
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
            background: var(--bg);
            color: var(--text);
            line-height: 1.6;
        }
        
        .header {
            background: var(--card-bg);
            border-bottom: 1px solid var(--border);
            padding: 1.5rem 0;
            position: sticky;
            top: 0;
            z-index: 100;
            box-shadow: 0 1px 3px rgba(0,0,0,0.1);
        }
        
        .container {
            max-width: 1400px;
            margin: 0 auto;
            padding: 0 2rem;
        }
        
        .header-content {
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        
        h1 {
            font-size: 2rem;
            font-weight: 700;
            color: var(--primary);
        }
        
        .subtitle {
            color: var(--text-light);
            font-size: 0.875rem;
            margin-top: 0.25rem;
        }
        
        .grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
            gap: 1.5rem;
            margin: 2rem 0;
        }
        
        .card {
            background: var(--card-bg);
            border-radius: 8px;
            padding: 1.5rem;
            box-shadow: 0 1px 3px rgba(0,0,0,0.1);
            border: 1px solid var(--border);
        }
        
        .card h2 {
            font-size: 1.125rem;
            font-weight: 600;
            margin-bottom: 1rem;
            color: var(--primary);
        }
        
        .metric {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 0.5rem 0;
            border-bottom: 1px solid var(--border);
        }
        
        .metric:last-child {
            border-bottom: none;
        }
        
        .metric-label {
            font-size: 0.875rem;
            color: var(--text-light);
        }
        
        .metric-value {
            font-weight: 600;
            font-size: 1.125rem;
        }
        
        .status {
            display: inline-block;
            padding: 0.25rem 0.75rem;
            border-radius: 9999px;
            font-size: 0.75rem;
            font-weight: 500;
        }
        
        .status-success {
            background: #d4edda;
            color: #155724;
        }
        
        .status-warning {
            background: #fff3cd;
            color: #856404;
        }
        
        .status-danger {
            background: #f8d7da;
            color: #721c24;
        }
        
        .progress-bar {
            width: 100%;
            height: 8px;
            background: var(--border);
            border-radius: 4px;
            overflow: hidden;
            margin-top: 0.5rem;
        }
        
        .progress-fill {
            height: 100%;
            background: var(--secondary);
            transition: width 0.3s ease;
        }
        
        .tech-stack {
            display: flex;
            flex-wrap: wrap;
            gap: 0.5rem;
            margin-top: 1rem;
        }
        
        .tech-badge {
            background: var(--bg);
            padding: 0.25rem 0.75rem;
            border-radius: 4px;
            font-size: 0.875rem;
            border: 1px solid var(--border);
        }
        
        .quick-links {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 1rem;
            margin-top: 1rem;
        }
        
        .link-card {
            background: var(--bg);
            padding: 1rem;
            border-radius: 4px;
            text-align: center;
            text-decoration: none;
            color: var(--text);
            border: 1px solid var(--border);
            transition: all 0.2s ease;
        }
        
        .link-card:hover {
            background: var(--card-bg);
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            transform: translateY(-2px);
        }
        
        .link-card h3 {
            font-size: 0.875rem;
            font-weight: 600;
            margin-bottom: 0.25rem;
        }
        
        .link-card p {
            font-size: 0.75rem;
            color: var(--text-light);
        }
        
        .alert {
            background: #fef3c7;
            border: 1px solid #f59e0b;
            border-radius: 4px;
            padding: 1rem;
            margin: 1rem 0;
        }
        
        .alert-title {
            font-weight: 600;
            color: #92400e;
            margin-bottom: 0.5rem;
        }
        
        .command {
            background: #1e293b;
            color: #e2e8f0;
            padding: 0.75rem 1rem;
            border-radius: 4px;
            font-family: 'Monaco', 'Consolas', monospace;
            font-size: 0.875rem;
            margin: 0.5rem 0;
            overflow-x: auto;
        }
        
        @media (max-width: 768px) {
            .grid {
                grid-template-columns: 1fr;
            }
        }
    </style>
</head>
<body>
    <header class="header">
        <div class="container">
            <div class="header-content">
                <div>
                    <h1>FreshPlan Developer Dashboard</h1>
                    <p class="subtitle">Enterprise Sales Tool für die Lebensmittelbranche</p>
                </div>
                <div style="text-align: right;">
                    <p class="subtitle">Zuletzt aktualisiert</p>
                    <p style="font-weight: 600;">${dateTime}</p>
                </div>
            </div>
        </div>
    </header>

    <main class="container">
        <!-- Wichtige Warnung -->
        <div class="alert">
            <div class="alert-title">⚠️ Wichtiger Hinweis für neue Entwickler</div>
            <p>Das Backend wurde am 08.06.2025 komplett reorganisiert! Die alte verschachtelte Struktur wurde durch eine flache Struktur ersetzt.</p>
            <p>Alte Struktur im <code>/archive</code> Ordner.</p>
        </div>

        <div class="grid">
            <!-- Projekt Status -->
            <div class="card">
                <h2>📊 Projekt Status</h2>
                <div class="metric">
                    <span class="metric-label">Sprint</span>
                    <span class="metric-value">${projectState.currentState.sprint}</span>
                </div>
                <div class="metric">
                    <span class="metric-label">Sprint Name</span>
                    <span class="metric-value" style="font-size: 0.875rem;">${projectState.currentState.sprintName}</span>
                </div>
                <div class="metric">
                    <span class="metric-label">Fortschritt</span>
                    <span class="metric-value">${projectState.currentState.sprintProgress}%</span>
                </div>
                <div class="progress-bar">
                    <div class="progress-fill" style="width: ${projectState.currentState.sprintProgress}%"></div>
                </div>
            </div>

            <!-- Git Status -->
            <div class="card">
                <h2>🔀 Git Status</h2>
                <div class="metric">
                    <span class="metric-label">Branch</span>
                    <span class="metric-value">${gitStats.branch}</span>
                </div>
                <div class="metric">
                    <span class="metric-label">Ungespeicherte Änderungen</span>
                    <span class="metric-value">${gitStats.uncommittedChanges}</span>
                </div>
                <div class="metric">
                    <span class="metric-label">Letzter Commit</span>
                    <span class="metric-value" style="font-size: 0.75rem;">${gitStats.lastCommit}</span>
                </div>
            </div>

            <!-- Code Metriken -->
            <div class="card">
                <h2>📈 Code Metriken</h2>
                <div class="metric">
                    <span class="metric-label">Frontend Coverage</span>
                    <span class="metric-value">${projectState.metrics.codeCoverage.frontend}%</span>
                </div>
                <div class="metric">
                    <span class="metric-label">Backend Coverage</span>
                    <span class="metric-value">${projectState.metrics.codeCoverage.backend}%</span>
                </div>
                <div class="metric">
                    <span class="metric-label">Tech Debt</span>
                    <span class="metric-value">${projectState.metrics.techDebt.hours}h</span>
                </div>
                <div class="metric">
                    <span class="metric-label">Offene Issues</span>
                    <span class="metric-value">
                        ${projectState.metrics.openIssues.total}
                        <span class="status status-danger" style="margin-left: 0.5rem;">
                            ${projectState.metrics.openIssues.critical} kritisch
                        </span>
                    </span>
                </div>
            </div>

            <!-- Tech Stack -->
            <div class="card">
                <h2>🛠️ Tech Stack</h2>
                <div class="tech-stack">
                    <span class="tech-badge">React ${projectState.techStack.frontend.framework}</span>
                    <span class="tech-badge">TypeScript</span>
                    <span class="tech-badge">Vite</span>
                    <span class="tech-badge">Quarkus ${projectState.techStack.backend.version}</span>
                    <span class="tech-badge">Java</span>
                    <span class="tech-badge">PostgreSQL</span>
                    <span class="tech-badge">Keycloak</span>
                </div>
            </div>
        </div>

        <!-- Quick Commands -->
        <div class="card">
            <h2>🚀 Quick Commands</h2>
            <div class="grid" style="margin-top: 1rem;">
                <div>
                    <h3 style="font-size: 1rem; margin-bottom: 0.5rem;">Frontend</h3>
                    <div class="command">cd frontend && npm run dev</div>
                    <div class="command">cd frontend && npm test</div>
                </div>
                <div>
                    <h3 style="font-size: 1rem; margin-bottom: 0.5rem;">Backend</h3>
                    <div class="command">cd backend && ./mvnw quarkus:dev</div>
                    <div class="command">cd backend && ./mvnw test</div>
                </div>
                <div>
                    <h3 style="font-size: 1rem; margin-bottom: 0.5rem;">Wartung</h3>
                    <div class="command">./scripts/quick-cleanup.sh</div>
                    <div class="command">node update-docs-simple.js</div>
                </div>
            </div>
        </div>

        <!-- Wichtige Links -->
        <div class="card">
            <h2>📚 Wichtige Dokumentation</h2>
            <div class="quick-links">
                <a href="MASTER_BRIEFING.md" class="link-card">
                    <h3>Master Briefing</h3>
                    <p>Zentrale Übersicht für alle</p>
                </a>
                <a href="CLAUDE.md" class="link-card">
                    <h3>Claude Regeln</h3>
                    <p>AI-Arbeitsrichtlinien</p>
                </a>
                <a href="API_CONTRACT.md" class="link-card">
                    <h3>API Contract</h3>
                    <p>Frontend-Backend Schnittstelle</p>
                </a>
                <a href="docs/development/KNOWN_ISSUES.md" class="link-card">
                    <h3>Known Issues</h3>
                    <p>Bekannte Probleme</p>
                </a>
                <a href="WAY_OF_WORKING.md" class="link-card">
                    <h3>Way of Working</h3>
                    <p>Team-Prozesse</p>
                </a>
                <a href="backend/DATABASE_GUIDE.md" class="link-card">
                    <h3>Database Guide</h3>
                    <p>PostgreSQL & Flyway</p>
                </a>
            </div>
        </div>

        <!-- Rollen -->
        <div class="card">
            <h2>👥 System-Rollen</h2>
            <p style="margin-bottom: 1rem; color: var(--text-light);">Nur 3 Rollen im System (viewer wurde entfernt)</p>
            <div class="metric">
                <span class="metric-label">admin</span>
                <span class="metric-value" style="font-size: 0.875rem;">Software-Administration</span>
            </div>
            <div class="metric">
                <span class="metric-label">manager</span>
                <span class="metric-value" style="font-size: 0.875rem;">Geschäftsleitung, Berichte</span>
            </div>
            <div class="metric">
                <span class="metric-label">sales</span>
                <span class="metric-value" style="font-size: 0.875rem;">Verkäufer, Kalkulationen</span>
            </div>
        </div>
    </main>

    <script>
        // Auto-Refresh alle 60 Sekunden
        setTimeout(() => {
            location.reload();
        }, 60000);
        
        // Zeige Refresh-Countdown
        let seconds = 60;
        setInterval(() => {
            seconds--;
            if (seconds <= 0) seconds = 60;
        }, 1000);
    </script>
</body>
</html>`;

    // Schreibe HTML
    fs.writeFileSync('DEVELOPER_DASHBOARD.html', html);
    console.log('✅ DEVELOPER_DASHBOARD.html generiert');
    
    // Öffne im Browser (optional)
    if (process.argv.includes('--open')) {
        require('child_process').exec('open DEVELOPER_DASHBOARD.html');
    }
}

// Ausführen
generateDashboard().catch(console.error);