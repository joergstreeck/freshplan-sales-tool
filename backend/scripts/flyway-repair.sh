#!/bin/bash

# Flyway Repair Script - Basierend auf Best Practices
# ===================================================
# Nutzt Flyway direkt für Repair-Operationen

# Farben
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}🔧 Flyway Repair & Validation Tool${NC}"
echo "===================================="

# Prüfe ob wir im Backend-Verzeichnis sind
if [ ! -f "pom.xml" ]; then
    echo -e "${RED}❌ Fehler: Bitte im backend/ Verzeichnis ausführen!${NC}"
    exit 1
fi

# Menu
echo -e "\n${YELLOW}Was möchtest du tun?${NC}"
echo "1) Info - Zeige aktuellen Migration-Status"
echo "2) Validate - Prüfe Migrations auf Probleme"
echo "3) Repair - Repariere Flyway Schema History"
echo "4) Clean - WARNUNG: Löscht ALLE Daten!"
echo "5) Baseline - Setze Baseline für existierende DB"
echo "6) Migrate - Führe Migrationen aus"
echo ""
read -p "Auswahl (1-6): " CHOICE

case $CHOICE in
    1)
        echo -e "\n${BLUE}📊 Flyway Info...${NC}"
        MAVEN_OPTS="-Dmaven.multiModuleProjectDirectory=$PWD" ./mvnw flyway:info -Dflyway.configFiles=src/main/resources/application.properties
        ;;
        
    2)
        echo -e "\n${BLUE}🔍 Flyway Validate...${NC}"
        MAVEN_OPTS="-Dmaven.multiModuleProjectDirectory=$PWD" ./mvnw flyway:validate -Dflyway.configFiles=src/main/resources/application.properties
        
        if [ $? -eq 0 ]; then
            echo -e "\n${GREEN}✅ Keine Validierungsfehler gefunden!${NC}"
        else
            echo -e "\n${RED}❌ Validierungsfehler gefunden!${NC}"
            echo -e "${YELLOW}Tipp: Nutze Option 3 (Repair) um zu beheben.${NC}"
        fi
        ;;
        
    3)
        echo -e "\n${YELLOW}⚠️  WARNUNG: Repair ändert die Flyway Schema History!${NC}"
        echo -e "${YELLOW}   Dies sollte nur bei Problemen verwendet werden.${NC}"
        read -p "Fortfahren? (y/n): " CONFIRM
        
        if [ "$CONFIRM" = "y" ] || [ "$CONFIRM" = "Y" ]; then
            echo -e "\n${BLUE}🔧 Flyway Repair...${NC}"
            MAVEN_OPTS="-Dmaven.multiModuleProjectDirectory=$PWD" ./mvnw flyway:repair -Dflyway.configFiles=src/main/resources/application.properties
            echo -e "\n${GREEN}✅ Repair abgeschlossen!${NC}"
            
            # Zeige Info nach Repair
            echo -e "\n${BLUE}📊 Neuer Status:${NC}"
            MAVEN_OPTS="-Dmaven.multiModuleProjectDirectory=$PWD" ./mvnw flyway:info -Dflyway.configFiles=src/main/resources/application.properties
        else
            echo "Abgebrochen."
        fi
        ;;
        
    4)
        echo -e "\n${RED}⚠️  EXTREME WARNUNG: Clean löscht ALLE Daten!${NC}"
        echo -e "${RED}   Die gesamte Datenbank wird geleert!${NC}"
        echo -e "${RED}   Nutze dies NUR in Entwicklungsumgebungen!${NC}"
        read -p "Wirklich fortfahren? Tippe 'DELETE ALL' zur Bestätigung: " CONFIRM
        
        if [ "$CONFIRM" = "DELETE ALL" ]; then
            echo -e "\n${RED}🗑️  Flyway Clean...${NC}"
            MAVEN_OPTS="-Dmaven.multiModuleProjectDirectory=$PWD" ./mvnw flyway:clean -Dflyway.configFiles=src/main/resources/application.properties
            echo -e "\n${YELLOW}Datenbank wurde geleert. Führe Migrate aus...${NC}"
            MAVEN_OPTS="-Dmaven.multiModuleProjectDirectory=$PWD" ./mvnw flyway:migrate -Dflyway.configFiles=src/main/resources/application.properties
        else
            echo "Abgebrochen."
        fi
        ;;
        
    5)
        echo -e "\n${BLUE}📍 Flyway Baseline...${NC}"
        echo "Baseline markiert die aktuelle DB als Version 1."
        echo "Nutze dies für existierende Datenbanken ohne Flyway-Historie."
        read -p "Baseline-Version (default: 1): " VERSION
        VERSION=${VERSION:-1}
        
        MAVEN_OPTS="-Dmaven.multiModuleProjectDirectory=$PWD" ./mvnw flyway:baseline -Dflyway.baselineVersion=$VERSION -Dflyway.configFiles=src/main/resources/application.properties
        ;;
        
    6)
        echo -e "\n${BLUE}🚀 Flyway Migrate...${NC}"
        MAVEN_OPTS="-Dmaven.multiModuleProjectDirectory=$PWD" ./mvnw flyway:migrate -Dflyway.configFiles=src/main/resources/application.properties
        ;;
        
    *)
        echo -e "${RED}Ungültige Auswahl!${NC}"
        exit 1
        ;;
esac

echo -e "\n${GREEN}✨ Fertig!${NC}"