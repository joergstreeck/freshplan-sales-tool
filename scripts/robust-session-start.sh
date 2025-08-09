#!/bin/bash

# =====================================================
# ROBUSTES SESSION-START-SCRIPT
# =====================================================
# Funktioniert aus JEDEM Verzeichnis
# Startet alle Services intelligent
# Prüft Voraussetzungen automatisch
# =====================================================

# Farben für Output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Finde Projekt-Root (suche nach .git und CLAUDE.md)
find_project_root() {
    local dir="$PWD"
    while [[ "$dir" != "/" ]]; do
        if [[ -d "$dir/.git" ]] && ([[ -f "$dir/CLAUDE.md" ]] || [[ -f "$dir/README.md" ]]); then
            echo "$dir"
            return 0
        fi
        dir="$(dirname "$dir")"
    done
    
    # Fallback: Bekannte Pfade prüfen
    for path in \
        "/Users/joergstreeck/freshplan-sales-tool" \
        "$HOME/freshplan-sales-tool" \
        "$HOME/projects/freshplan-sales-tool"; do
        if [[ -d "$path" ]]; then
            echo "$path"
            return 0
        fi
    done
    
    echo ""
    return 1
}

# Service-Check Funktion
check_service() {
    local name=$1
    local port=$2
    
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        echo -e "${GREEN}✅ $name läuft auf Port $port${NC}"
        return 0
    else
        echo -e "${YELLOW}⚠️  $name nicht aktiv auf Port $port${NC}"
        return 1
    fi
}

# PostgreSQL starten
start_postgres() {
    echo -e "${BLUE}🐘 Prüfe PostgreSQL...${NC}"
    
    if check_service "PostgreSQL" 5432; then
        return 0
    fi
    
    echo -e "${YELLOW}Starting PostgreSQL...${NC}"
    
    # macOS mit Homebrew
    if command -v brew &> /dev/null; then
        brew services start postgresql@14 2>/dev/null || \
        brew services start postgresql@15 2>/dev/null || \
        brew services start postgresql 2>/dev/null
    fi
    
    # Linux mit systemctl
    if command -v systemctl &> /dev/null; then
        sudo systemctl start postgresql 2>/dev/null
    fi
    
    # Docker Fallback
    if command -v docker &> /dev/null && ! check_service "PostgreSQL" 5432; then
        echo -e "${CYAN}Starte PostgreSQL in Docker...${NC}"
        docker run -d --name freshplan-postgres \
            -e POSTGRES_PASSWORD=freshplan \
            -e POSTGRES_USER=freshplan \
            -e POSTGRES_DB=freshplan \
            -p 5432:5432 \
            postgres:15 2>/dev/null || \
        docker start freshplan-postgres 2>/dev/null
    fi
    
    sleep 2
    check_service "PostgreSQL" 5432
}

# Backend starten
start_backend() {
    echo -e "${BLUE}☕ Prüfe Backend...${NC}"
    
    if check_service "Quarkus Backend" 8080; then
        return 0
    fi
    
    local backend_dir="$PROJECT_ROOT/backend"
    if [[ ! -d "$backend_dir" ]]; then
        # Vielleicht sind wir schon im backend Ordner?
        if [[ -f "pom.xml" ]] && [[ -f "mvnw" ]]; then
            backend_dir="$PWD"
        else
            echo -e "${RED}❌ Backend-Verzeichnis nicht gefunden!${NC}"
            return 1
        fi
    fi
    
    cd "$backend_dir"
    
    echo -e "${CYAN}Starte Quarkus Backend...${NC}"
    
    # Maven Wrapper verwenden wenn vorhanden
    if [[ -f "./mvnw" ]]; then
        echo "Verwende Maven Wrapper..."
        nohup ./mvnw quarkus:dev > /tmp/quarkus.log 2>&1 &
    elif command -v mvn &> /dev/null; then
        echo "Verwende System Maven..."
        nohup mvn quarkus:dev > /tmp/quarkus.log 2>&1 &
    else
        echo -e "${RED}❌ Maven nicht gefunden! Installiere Maven oder verwende ./mvnw${NC}"
        return 1
    fi
    
    echo -e "${YELLOW}⏳ Warte auf Backend-Start (max 30 Sekunden)...${NC}"
    local count=0
    while [[ $count -lt 30 ]]; do
        if curl -s http://localhost:8080/q/health >/dev/null 2>&1; then
            echo -e "${GREEN}✅ Backend erfolgreich gestartet!${NC}"
            return 0
        fi
        sleep 1
        count=$((count + 1))
        echo -n "."
    done
    
    echo -e "${RED}❌ Backend Start fehlgeschlagen. Log: /tmp/quarkus.log${NC}"
    tail -20 /tmp/quarkus.log
    return 1
}

# Frontend starten
start_frontend() {
    echo -e "${BLUE}⚛️  Prüfe Frontend...${NC}"
    
    if check_service "Vite Frontend" 5173; then
        return 0
    fi
    
    local frontend_dir="$PROJECT_ROOT/frontend"
    if [[ ! -d "$frontend_dir" ]]; then
        echo -e "${YELLOW}⚠️  Frontend-Verzeichnis nicht gefunden${NC}"
        return 1
    fi
    
    cd "$frontend_dir"
    
    # Dependencies installieren falls nötig
    if [[ ! -d "node_modules" ]]; then
        echo -e "${CYAN}Installiere Frontend Dependencies...${NC}"
        npm install
    fi
    
    echo -e "${CYAN}Starte Frontend...${NC}"
    nohup npm run dev > /tmp/vite.log 2>&1 &
    
    sleep 3
    check_service "Vite Frontend" 5173
}

# Quick Info anzeigen
show_quick_info() {
    echo ""
    echo -e "${CYAN}╔══════════════════════════════════════════════════════╗${NC}"
    echo -e "${CYAN}║            🚀 FreshPlan Session Status               ║${NC}"
    echo -e "${CYAN}╚══════════════════════════════════════════════════════╝${NC}"
    echo ""
    
    # Git Status
    cd "$PROJECT_ROOT"
    local branch=$(git branch --show-current 2>/dev/null || echo "unbekannt")
    local changes=$(git status --porcelain 2>/dev/null | wc -l | tr -d ' ')
    
    echo -e "${BLUE}📍 Projekt:${NC} $PROJECT_ROOT"
    echo -e "${BLUE}🌿 Branch:${NC} $branch"
    echo -e "${BLUE}📝 Änderungen:${NC} $changes Dateien"
    
    # Migration Info
    local migration_dir="$PROJECT_ROOT/backend/src/main/resources/db/migration"
    if [[ ! -d "$migration_dir" ]]; then
        migration_dir="$PROJECT_ROOT/src/main/resources/db/migration"
    fi
    
    if [[ -d "$migration_dir" ]]; then
        local last_migration=$(ls -1 "$migration_dir" 2>/dev/null | tail -1 | grep -oE 'V[0-9]+' | sed 's/V//')
        local next_migration=$((last_migration + 1))
        echo -e "${BLUE}🔢 Nächste Migration:${NC} V${next_migration}"
    fi
    
    echo ""
    echo -e "${CYAN}═══════════════════════════════════════════════════════${NC}"
    echo ""
    
    # Service Status
    check_service "PostgreSQL" 5432
    check_service "Backend" 8080
    check_service "Frontend" 5173
    
    echo ""
    echo -e "${CYAN}═══════════════════════════════════════════════════════${NC}"
    echo ""
    
    # URLs
    echo -e "${GREEN}📌 URLs:${NC}"
    echo "   Backend:  http://localhost:8080"
    echo "   Frontend: http://localhost:5173"
    echo "   API Docs: http://localhost:8080/q/swagger-ui"
    echo ""
    
    # Logs
    echo -e "${YELLOW}📜 Logs:${NC}"
    echo "   Backend:  tail -f /tmp/quarkus.log"
    echo "   Frontend: tail -f /tmp/vite.log"
    echo ""
}

# Hauptfunktion
main() {
    echo -e "${CYAN}╔══════════════════════════════════════════════════════╗${NC}"
    echo -e "${CYAN}║         🚀 FreshPlan Robust Session Start            ║${NC}"
    echo -e "${CYAN}╚══════════════════════════════════════════════════════╝${NC}"
    echo ""
    
    # Projekt-Root finden
    PROJECT_ROOT=$(find_project_root)
    if [[ -z "$PROJECT_ROOT" ]]; then
        echo -e "${RED}❌ Fehler: FreshPlan Projekt nicht gefunden!${NC}"
        echo -e "${YELLOW}Bitte im Projekt-Verzeichnis ausführen.${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✅ Projekt gefunden: $PROJECT_ROOT${NC}"
    echo ""
    
    # Services starten
    start_postgres
    
    # Optional: Backend und Frontend starten
    echo ""
    read -p "Backend starten? (j/n) " -n 1 -r
    echo ""
    if [[ $REPLY =~ ^[Jj]$ ]]; then
        start_backend
    fi
    
    echo ""
    read -p "Frontend starten? (j/n) " -n 1 -r
    echo ""
    if [[ $REPLY =~ ^[Jj]$ ]]; then
        start_frontend
    fi
    
    # Status anzeigen
    show_quick_info
    
    # NEXT_STEP anzeigen wenn vorhanden
    if [[ -f "$PROJECT_ROOT/docs/NEXT_STEP.md" ]]; then
        echo -e "${CYAN}═══════════════════════════════════════════════════════${NC}"
        echo -e "${BLUE}📋 NEXT_STEP.md (erste 20 Zeilen):${NC}"
        echo ""
        head -20 "$PROJECT_ROOT/docs/NEXT_STEP.md"
    elif [[ -f "$PROJECT_ROOT/NEXT_STEP.md" ]]; then
        echo -e "${CYAN}═══════════════════════════════════════════════════════${NC}"
        echo -e "${BLUE}📋 NEXT_STEP.md (erste 20 Zeilen):${NC}"
        echo ""
        head -20 "$PROJECT_ROOT/NEXT_STEP.md"
    fi
    
    echo ""
    echo -e "${GREEN}✨ Session bereit!${NC}"
    echo ""
    
    # Working directory Info
    echo -e "${YELLOW}💡 Tipp: Du kannst jetzt in folgende Verzeichnisse wechseln:${NC}"
    echo "   cd $PROJECT_ROOT          # Projekt-Root"
    echo "   cd $PROJECT_ROOT/backend  # Backend"
    echo "   cd $PROJECT_ROOT/frontend # Frontend"
    echo ""
}

# Script ausführen
main "$@"