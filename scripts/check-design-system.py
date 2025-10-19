#!/usr/bin/env python3
"""
Design System Compliance Guard
===============================
Checkt: Kein Hard-Coding, MUI Theme Nutzung, Sprachregeln
Usage: ./scripts/check-design-system.py
Exit: 0 = OK, 1 = Violations found
"""

import re
import sys
from pathlib import Path
from typing import List, Tuple

SCRIPT_DIR = Path(__file__).parent
PROJECT_ROOT = SCRIPT_DIR.parent
FRONTEND_SRC = PROJECT_ROOT / "frontend/src"

# KRITISCHE VIOLATIONS - Design System
CRITICAL_PATTERNS = [
    # 1. Hardcoded Hex Colors (außer Whitelist + Recharts fills)
    (r'(?<!fill=)["\']#(?!94C456|004F7B|FFFFFF|000000|F5F5F5|fff|000|8884d8)[0-9A-Fa-f]{3,6}["\']',
     "HARDCODED COLOR: Use theme.palette instead"),
    
    # 2. Inline RGB colors (not rgba - rgba is OK for opacity/gradients)
    (r'(?<!a)\(rgb\s*\(\s*\d+\s*,\s*\d+\s*,\s*\d+\s*\)',
     "HARDCODED RGB: Use theme.palette instead"),
    
    # 3. Direct fontFamily (außer 'inherit' und CSS vars)
    (r'fontFamily:\s*["\'](?!inherit|var\()[A-Za-z]',
     "HARDCODED FONT: Use theme.typography instead"),
    
    # 4. Englische UI-Texte (Common violations)
    (r'[>"]Save[<"]',
     "ENGLISH TEXT: Use 'Speichern' (German)"),
    (r'[>"]Delete[<"]',
     "ENGLISH TEXT: Use 'Löschen' (German)"),
    (r'[>"]Cancel[<"]',
     "ENGLISH TEXT: Use 'Abbrechen' (German)"),
    (r'[>"]Edit[<"]',
     "ENGLISH TEXT: Use 'Bearbeiten' (German)"),
    (r'[>"]Create[<"]',
     "ENGLISH TEXT: Use 'Erstellen' (German)"),
    (r'[>"]Dashboard[<"]',
     "ENGLISH TEXT: Use 'Übersicht' (German)"),
    (r'[>"]Settings[<"]',
     "ENGLISH TEXT: Use 'Einstellungen' (German)"),
    (r'[>"]Search[<"]',
     "ENGLISH TEXT: Use 'Suchen' (German)"),
    (r'[>"]Filter[<"]',
     "ENGLISH TEXT: Use 'Filtern' (German)"),
    (r'[>"]Export[<"]',
     "ENGLISH TEXT: Use 'Exportieren' (German)"),
]

# Ignore patterns (false positives)
IGNORE_PATTERNS = [
    r'//.*',  # Comments
    r'/\*.*?\*/',  # Block comments
    r'test\.ts',  # Test files
    r'\.test\.',  # Test files
    r'\.spec\.',  # Spec files
]

def should_ignore_line(line: str, file_path: Path) -> bool:
    """Check if line should be ignored"""
    # Ignore test files
    if any(pattern in str(file_path) for pattern in ['/test/', '/__tests__/', '.test.', '.spec.']):
        return True
    
    # Ignore comments
    stripped = line.strip()
    if stripped.startswith('//') or stripped.startswith('/*'):
        return True
    
    return False

def check_file(file_path: Path) -> List[Tuple[int, str, str]]:
    """Check file for design system violations"""
    violations = []
    
    try:
        content = file_path.read_text()
        lines = content.split('\n')
        
        for line_num, line in enumerate(lines, 1):
            if should_ignore_line(line, file_path):
                continue
            
            for pattern, message in CRITICAL_PATTERNS:
                if re.search(pattern, line):
                    violations.append((line_num, line.strip()[:100], message))
        
    except Exception:
        pass  # Skip files that can't be read
    
    return violations

def main():
    print("🎨 Design System Compliance Check")
    print("━" * 50)
    print("📖 Reference: docs/planung/grundlagen/DESIGN_SYSTEM.md")
    print()
    print("🔍 Checking:")
    print("  ❌ No hardcoded colors (use theme.palette)")
    print("  ❌ No hardcoded fonts (use theme.typography)")
    print("  ❌ No English UI text (use German)")
    print()
    
    # Find TypeScript/TSX files only
    print("📋 Scanning frontend files...")
    
    tsx_files = list(FRONTEND_SRC.glob("**/*.tsx"))
    ts_files = list(FRONTEND_SRC.glob("**/*.ts"))
    
    # WHITELIST: Theme definition files MUST have hardcoded colors/fonts (SoT!)
    # Chart/Data-Viz files MAY have hardcoded colors for gradients/heatmaps
    THEME_FILES = [
        'theme/freshfoodz.ts',
        'theme/ThemeProvider.tsx',
        'types/theme-v2.mui.ts',
        'theme/customerFieldTheme.ts',
        # Root Provider (OUTSIDE ThemeProvider context - no useTheme available)
        'providers.tsx',
        # Charts & Data Visualization (Recharts gradients, heatmaps)
        'AuditActivityChart.tsx',
        'AuditActivityHeatmap.tsx',
        'AuditDashboard.tsx',  # Chart components inside
        'DataHygieneDashboard.tsx',  # Heatmap colors
        'LeadQualityDashboard.tsx',  # Chart gradients
    ]

    all_files = [
        f for f in (tsx_files + ts_files)
        if 'node_modules' not in str(f)
        and 'dist' not in str(f)
        and '.test.' not in str(f)
        and '.spec.' not in str(f)
        and not any(theme_file in str(f) for theme_file in THEME_FILES)
    ]
    
    print(f"✅ Found {len(all_files)} files")
    print()
    
    # Check files
    all_violations = {}
    total_violations = 0
    
    for file_path in all_files:
        violations = check_file(file_path)
        if violations:
            relative_path = file_path.relative_to(PROJECT_ROOT)
            all_violations[relative_path] = violations
            total_violations += len(violations)
    
    # Report
    if total_violations == 0:
        print("✅ SUCCESS: Design System compliant!")
        print(f"   Files checked: {len(all_files)}")
        print(f"   Violations: 0")
        print()
        print("   ✓ No hardcoded colors")
        print("   ✓ No hardcoded fonts")
        print("   ✓ German UI text")
        sys.exit(0)
    else:
        print(f"❌ FAILURE: {total_violations} violations found:")
        print()
        
        for file_path, violations in sorted(all_violations.items()):
            print(f"  📄 {file_path}")
            for line_num, line, message in violations[:5]:  # Max 5 per file
                print(f"     Line {line_num}: {message}")
                if len(line) > 80:
                    print(f"       {line[:77]}...")
                else:
                    print(f"       {line}")
            if len(violations) > 5:
                print(f"     ... and {len(violations) - 5} more")
            print()
        
        print("━" * 50)
        print("🚫 Design System Rules Violated")
        print()
        print("🔧 How to fix:")
        print("   • Colors: sx={{ color: 'primary.main' }}")
        print("   • Fonts: sx={{ fontFamily: 'heading' }}")
        print("   • Text: Use German (Save → Speichern)")
        print()
        print("📖 See: docs/planung/grundlagen/DESIGN_SYSTEM.md")
        sys.exit(1)

if __name__ == '__main__':
    main()
