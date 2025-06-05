#!/usr/bin/env node

/**
 * Script to update Way of Working HTML when markdown changes
 * Keeps both versions in sync for team access
 */

const fs = require('fs');
const path = require('path');

// Template for HTML generation
const generateHTML = (content) => {
  const date = new Date().toLocaleDateString('de-DE');
  const version = content.match(/\| (\d+\.\d+) \| Initial Version/)?.[1] || '1.0';
  
  return `<!DOCTYPE html>
<html lang="de">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>The FreshPlan Way - Unser Weg zu exzellenter Software</title>
    <meta name="description" content="FreshPlan 2.0 Development Standards and Way of Working">
    <meta name="author" content="FreshPlan Team">
    <meta name="version" content="${version}">
    <meta name="updated" content="${date}">
    
    <!-- Auto-refresh in development -->
    <script>
      if (location.hostname === 'localhost' || location.hostname === '127.0.0.1') {
        setTimeout(() => location.reload(), 30000); // Reload every 30s in dev
      }
    </script>
    
    <style>
        /* Previous styles remain the same */
        @page {
            size: A4;
            margin: 2cm;
        }
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
            line-height: 1.6;
            color: #333;
            max-width: 210mm;
            margin: 0 auto;
            padding: 20px;
        }
        h1 {
            color: #2c3e50;
            border-bottom: 3px solid #3498db;
            padding-bottom: 10px;
            page-break-after: avoid;
        }
        h2 {
            color: #34495e;
            margin-top: 30px;
            border-bottom: 2px solid #ecf0f1;
            padding-bottom: 5px;
            page-break-after: avoid;
        }
        h3 {
            color: #7f8c8d;
            margin-top: 20px;
            page-break-after: avoid;
        }
        blockquote {
            border-left: 4px solid #3498db;
            padding-left: 20px;
            margin: 20px 0;
            font-style: italic;
            color: #555;
            background: #f8f9fa;
            padding: 15px 20px;
        }
        code {
            background: #f4f4f4;
            padding: 2px 6px;
            border-radius: 3px;
            font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
            font-size: 0.9em;
        }
        pre {
            background: #282c34;
            color: #abb2bf;
            padding: 15px;
            border-radius: 5px;
            overflow-x: auto;
            page-break-inside: avoid;
        }
        pre code {
            background: none;
            color: inherit;
            padding: 0;
        }
        table {
            border-collapse: collapse;
            width: 100%;
            margin: 20px 0;
            page-break-inside: avoid;
        }
        th, td {
            border: 1px solid #ddd;
            padding: 12px;
            text-align: left;
        }
        th {
            background-color: #3498db;
            color: white;
            font-weight: bold;
        }
        tr:nth-child(even) {
            background-color: #f8f9fa;
        }
        ul, ol {
            margin: 15px 0;
            padding-left: 30px;
        }
        li {
            margin: 5px 0;
        }
        .toc {
            background: #f8f9fa;
            border: 1px solid #e9ecef;
            padding: 20px;
            margin: 20px 0;
            border-radius: 5px;
        }
        .toc h2 {
            margin-top: 0;
            border: none;
        }
        .toc ol {
            margin: 10px 0;
        }
        .page-break {
            page-break-after: always;
        }
        .highlight {
            background: #fff3cd;
            padding: 15px;
            border-left: 4px solid #ffb703;
            margin: 20px 0;
        }
        .footer {
            margin-top: 50px;
            padding-top: 20px;
            border-top: 1px solid #ddd;
            text-align: center;
            color: #666;
            font-size: 0.9em;
        }
        .updated-badge {
            position: fixed;
            top: 10px;
            right: 10px;
            background: #27ae60;
            color: white;
            padding: 5px 15px;
            border-radius: 20px;
            font-size: 0.8em;
            box-shadow: 0 2px 5px rgba(0,0,0,0.2);
        }
        @media print {
            body {
                font-size: 12pt;
            }
            h1 {
                font-size: 24pt;
            }
            h2 {
                font-size: 18pt;
            }
            h3 {
                font-size: 14pt;
            }
            .no-print, .updated-badge {
                display: none;
            }
        }
    </style>
</head>
<body>
    <div class="updated-badge no-print">
        Aktualisiert: ${date}
    </div>
    
    <!-- Content will be injected here by the update script -->
    ${content}
    
    <div class="footer">
        <p><strong>"Wir bauen Software, auf die wir stolz sind!"</strong> 🚀</p>
        <p>Version ${version} - Stand: ${date}</p>
        <p class="no-print">
            <a href="WAY_OF_WORKING.md">Markdown Version</a> | 
            <a href="#" onclick="window.print()">Als PDF drucken</a>
        </p>
    </div>
</body>
</html>`;
};

// Main update function
const updateHTML = () => {
  try {
    const mdPath = path.join(__dirname, '..', 'WAY_OF_WORKING.md');
    const htmlPath = path.join(__dirname, '..', 'WAY_OF_WORKING.html');
    
    // Read current markdown
    const mdContent = fs.readFileSync(mdPath, 'utf8');
    
    // Parse and convert content (simplified - in production use a proper markdown parser)
    let htmlContent = mdContent
      .replace(/^# (.+)$/gm, '<h1>$1</h1>')
      .replace(/^## (.+)$/gm, '<h2>$1</h2>')
      .replace(/^### (.+)$/gm, '<h3>$1</h3>')
      .replace(/^> (.+)$/gm, '<blockquote>$1</blockquote>')
      .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
      .replace(/`(.+?)`/g, '<code>$1</code>')
      .replace(/^\| (.+) \|$/gm, (match) => {
        const cells = match.split('|').filter(c => c.trim());
        const isHeader = cells.some(c => c.includes('---'));
        if (isHeader) return '';
        
        const tag = cells[0].includes('**') ? 'th' : 'td';
        const row = cells.map(c => `<${tag}>${c.trim()}</${tag}>`).join('');
        return `<tr>${row}</tr>`;
      })
      .replace(/(<tr>.*<\/tr>\n)+/g, '<table>$&</table>');
    
    // Generate full HTML
    const fullHTML = generateHTML(htmlContent);
    
    // Write updated HTML
    fs.writeFileSync(htmlPath, fullHTML);
    
    console.log('✅ WAY_OF_WORKING.html updated successfully!');
    console.log(`📅 Version: ${new Date().toLocaleDateString('de-DE')}`);
    
  } catch (error) {
    console.error('❌ Error updating HTML:', error.message);
    process.exit(1);
  }
};

// Run update
updateHTML();

// Watch mode for development
if (process.argv.includes('--watch')) {
  const mdPath = path.join(__dirname, '..', 'WAY_OF_WORKING.md');
  console.log('👀 Watching for changes...');
  
  fs.watchFile(mdPath, { interval: 1000 }, () => {
    console.log('📝 Markdown changed, updating HTML...');
    updateHTML();
  });
}