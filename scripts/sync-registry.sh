#!/bin/bash

# Feature Registry Synchronisierung
# Gleicht die FEATURE_REGISTRY.md mit der tatsächlichen Ordnerstruktur ab

PROJECT_ROOT="/Users/joergstreeck/freshplan-sales-tool"
REGISTRY="$PROJECT_ROOT/docs/FEATURE_REGISTRY.md"
FEATURES_DIR="$PROJECT_ROOT/docs/features"

# Farben für Output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo "🔄 Feature Registry Synchronisierung"
echo "===================================="
echo ""

# Backup der Registry
cp "$REGISTRY" "$REGISTRY.backup.$(date +%Y%m%d_%H%M%S)"
echo "✅ Backup erstellt"

# Sammle alle Features aus dem Dateisystem
echo ""
echo "📊 Analysiere Dateisystem..."
echo ""

declare -A active_features
declare -A planned_features

# ACTIVE Features sammeln
echo "📁 ACTIVE Features:"
for dir in "$FEATURES_DIR/ACTIVE"/*; do
    if [ -d "$dir" ]; then
        dirname=$(basename "$dir")
        # Extrahiere Feature-Code aus Dateien im Ordner
        feature_code=""
        
        # Suche nach FC-XXX oder M[0-9] Pattern in README oder TECH_CONCEPT Dateien
        if [ -f "$dir/README.md" ]; then
            feature_code=$(grep -E "^#.*\b(FC-[0-9]{3}|M[0-9])\b" "$dir/README.md" | head -1 | grep -oE "\b(FC-[0-9]{3}|M[0-9])\b" | head -1)
        fi
        
        if [ -z "$feature_code" ] && ls "$dir"/*TECH_CONCEPT*.md 2>/dev/null | head -1 > /dev/null; then
            tech_file=$(ls "$dir"/*TECH_CONCEPT*.md 2>/dev/null | head -1)
            feature_code=$(basename "$tech_file" | grep -oE "\b(FC-[0-9]{3}|M[0-9])\b" | head -1)
        fi
        
        if [ -n "$feature_code" ]; then
            active_features["$feature_code"]="$dirname"
            echo "  ✅ $feature_code → $dirname"
        else
            echo "  ⚠️  $dirname (kein Feature-Code gefunden)"
        fi
    fi
done

echo ""
echo "📁 PLANNED Features:"
for dir in "$FEATURES_DIR/PLANNED"/*; do
    if [ -d "$dir" ]; then
        dirname=$(basename "$dir")
        # Extrahiere Feature-Code
        feature_code=""
        
        if [ -f "$dir/README.md" ]; then
            feature_code=$(grep -E "^#.*\b(FC-[0-9]{3}|M[0-9])\b" "$dir/README.md" | head -1 | grep -oE "\b(FC-[0-9]{3}|M[0-9])\b" | head -1)
        fi
        
        if [ -z "$feature_code" ] && ls "$dir"/*TECH_CONCEPT*.md 2>/dev/null | head -1 > /dev/null; then
            tech_file=$(ls "$dir"/*TECH_CONCEPT*.md 2>/dev/null | head -1)
            feature_code=$(basename "$tech_file" | grep -oE "\b(FC-[0-9]{3}|M[0-9])\b" | head -1)
        fi
        
        if [ -n "$feature_code" ]; then
            planned_features["$feature_code"]="$dirname"
            echo "  ✅ $feature_code → $dirname"
        else
            echo "  ⚠️  $dirname (kein Feature-Code gefunden)"
        fi
    fi
done

echo ""
echo "🔧 Generiere neue Registry..."
echo ""

# Generiere neue Registry
cat > "$REGISTRY" << 'EOF'
# 📋 FEATURE REGISTRY - Zentrale Wahrheitsquelle

**Zweck:** Eindeutige Registrierung aller Feature-Codes  
**Regel:** KEINE neue Feature-Nummer ohne Eintrag hier!  
**Update:** Bei JEDER Feature-Änderung
**Zuletzt synchronisiert:** $(date +%Y-%m-%d\ %H:%M)

## ✅ AKTIVE FEATURES

| Code | Feature Name | Status | Ordner | Erstellt | Zuletzt aktualisiert |
|------|-------------|--------|--------|----------|---------------------|
EOF

# Füge ACTIVE Features zur Registry hinzu
for code in $(echo "${!active_features[@]}" | tr ' ' '\n' | sort); do
    dir="${active_features[$code]}"
    # Versuche Feature-Namen aus README zu extrahieren
    name="$code Feature"
    if [ -f "$FEATURES_DIR/ACTIVE/$dir/README.md" ]; then
        extracted_name=$(grep -E "^#\s+" "$FEATURES_DIR/ACTIVE/$dir/README.md" | head -1 | sed 's/^#\s*//;s/\s*$//' | cut -d'-' -f2- | sed 's/^\s*//')
        if [ -n "$extracted_name" ]; then
            name="$extracted_name"
        fi
    fi
    
    echo "| $code | $name | ACTIVE | ACTIVE/$dir | $(date +%Y-%m-%d) | $(date +%Y-%m-%d) |" >> "$REGISTRY"
done

echo "" >> "$REGISTRY"
echo "## 📅 GEPLANTE FEATURES" >> "$REGISTRY"
echo "" >> "$REGISTRY"
echo "| Code | Feature Name | Status | Ordner | Reserviert | Geplant für |" >> "$REGISTRY"
echo "|------|-------------|--------|--------|------------|-------------|" >> "$REGISTRY"

# Füge PLANNED Features zur Registry hinzu
for code in $(echo "${!planned_features[@]}" | tr ' ' '\n' | sort); do
    dir="${planned_features[$code]}"
    # Versuche Feature-Namen aus README zu extrahieren
    name="$code Feature"
    if [ -f "$FEATURES_DIR/PLANNED/$dir/README.md" ]; then
        extracted_name=$(grep -E "^#\s+" "$FEATURES_DIR/PLANNED/$dir/README.md" | head -1 | sed 's/^#\s*//;s/\s*$//' | cut -d'-' -f2- | sed 's/^\s*//')
        if [ -n "$extracted_name" ]; then
            name="$extracted_name"
        fi
    fi
    
    echo "| $code | $name | PLANNED | PLANNED/$dir | $(date +%Y-%m-%d) | TBD |" >> "$REGISTRY"
done

echo "" >> "$REGISTRY"
echo "## 🗂️ ARCHIVIERTE FEATURES" >> "$REGISTRY"
echo "" >> "$REGISTRY"
echo "| Code | Feature Name | Archiviert am | Grund |" >> "$REGISTRY"
echo "|------|--------------|---------------|-------|" >> "$REGISTRY"
echo "| - | - | - | - |" >> "$REGISTRY"

echo "" >> "$REGISTRY"
echo "## 📝 REGISTRY REGELN" >> "$REGISTRY"
echo "" >> "$REGISTRY"
echo "1. **Eindeutigkeit:** Jeder Feature-Code darf nur EINMAL vergeben werden" >> "$REGISTRY"
echo "2. **Sequenz:** Feature-Codes werden fortlaufend vergeben (FC-001, FC-002, ...)" >> "$REGISTRY"
echo "3. **Keine Lücken:** Auch verworfene Features behalten ihre Nummer" >> "$REGISTRY"
echo "4. **Status-Pflege:** Bei jeder Änderung muss der Status aktualisiert werden" >> "$REGISTRY"
echo "5. **Ordner-Sync:** Der Ordnername muss mit der Registry übereinstimmen" >> "$REGISTRY"

echo -e "${GREEN}✅ Feature Registry wurde neu generiert!${NC}"
echo ""
echo "📊 Zusammenfassung:"
echo "  - ${#active_features[@]} ACTIVE Features"
echo "  - ${#planned_features[@]} PLANNED Features"
echo ""
echo "⚠️  Bitte prüfe die neue Registry und committiere sie wenn alles korrekt ist!"