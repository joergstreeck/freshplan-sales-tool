#!/usr/bin/env python3
import re
import os

# Lese V5 Datei
with open('docs/CRM_COMPLETE_MASTER_PLAN_V5.md', 'r') as f:
    content = f.read()

# Finde alle CLAUDE_TECH Links
links = re.findall(r'\[CLAUDE_TECH\]\((/docs/features/[^)]+\.md)\)', content)

# Prüfe jeden Link
missing = []
found = []

for link in links:
    # Entferne führenden Slash für Dateiprüfung
    file_path = link[1:] if link.startswith('/') else link
    
    if os.path.exists(file_path):
        found.append(link)
    else:
        missing.append(link)

print(f"📊 V5 CLAUDE_TECH Link-Analyse:")
print(f"✅ Gefunden: {len(found)} Links")
print(f"❌ Fehlend: {len(missing)} Links")
print()

if missing:
    print("❌ FEHLENDE DATEIEN:")
    for link in missing:
        print(f"   {link}")
else:
    print("✅ ALLE LINKS FUNKTIONIEREN!")

# Zähle Features
active_count = len([l for l in links if '/ACTIVE/' in l])
planned_count = len([l for l in links if '/PLANNED/' in l])

print()
print(f"📈 Feature-Verteilung:")
print(f"   ACTIVE: {active_count}")
print(f"   PLANNED: {planned_count}")
print(f"   GESAMT: {len(links)}")

# Zeige alle gefundenen Features mit Code
print()
print("📋 Alle Features in V5:")
features = {}
for link in links:
    # Extrahiere Feature-Code aus Dateiname
    filename = link.split('/')[-1]
    code = filename.replace('_CLAUDE_TECH.md', '')
    features[code] = link

for code in sorted(features.keys()):
    status = "ACTIVE" if '/ACTIVE/' in features[code] else "PLANNED"
    print(f"   {code}: {status}")