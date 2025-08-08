#!/bin/bash

# DEFINITIVER Migration Reset - Stellt die korrekte Reihenfolge her
# =================================================================

MIGRATION_DIR="src/main/resources/db/migration"

# Farben
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${RED}🔧 COMPLETE Migration Order Reset${NC}"
echo "===================================="
echo -e "${YELLOW}Dieses Script stellt die KORREKTE Reihenfolge aller Migrationen her.${NC}"
echo ""

# Backup
BACKUP_DIR="migration_backup_reset_$(date +%Y%m%d_%H%M%S)"
echo -e "${BLUE}📦 Erstelle Backup in $BACKUP_DIR...${NC}"
mkdir -p "$BACKUP_DIR"
cp -r "$MIGRATION_DIR" "$BACKUP_DIR/"
echo -e "${GREEN}✅ Backup erstellt${NC}"

# SCHRITT 1: Definiere die korrekte Reihenfolge basierend auf Abhängigkeiten
echo -e "\n${BLUE}📋 Definiere korrekte Reihenfolge...${NC}"

# Basis-Tabellen (keine Abhängigkeiten)
PHASE1=(
    "V1__initial_schema.sql"
    "V2__create_user_table.sql"
    "V3__add_user_roles.sql"
    "V4__Create_profile_tables.sql"
)

# Customer-Tabellen (müssen vor allen customer-bezogenen Migrationen kommen)
PHASE2=(
    "V121__Create_customer_tables.sql"  # Erstellt customers, customer_addresses, etc.
)

# Weitere Basis-Tabellen
PHASE3=(
    "V103__create_permission_system_core.sql"
    "V104__create_opportunities_table.sql"
    "V105__create_opportunity_activities_table.sql"
    "V106__create_audit_trail.sql"
    "V113__create_contacts_table.sql"  # Erstellt contacts
)

# Tabellen-Modifikationen (benötigen existierende Tabellen)
PHASE4=(
    "V125__add_expansion_planned_field.sql"  # ALTER customers
    "V10__Complete_schema_alignment.sql"     # ALTER verschiedene Tabellen
    "V12__Add_last_modified_audit_fields.sql" # ALTER verschiedene Tabellen
    "V13__Add_missing_timeline_event_columns.sql"
    "V14__Fix_contact_roles_columns.sql"
    "V15__Add_last_contact_date_to_contacts.sql"  # ALTER contacts
    "V16__Rename_preferred_contact_to_communication_method.sql"
    "V17__Add_missing_columns_for_entities.sql"
    "V100__add_test_data_flag.sql"
    "V102__add_customer_search_performance_indices.sql"
    "V107__fix_audit_trail_ip_address_type.sql"
    "V108__fix_audit_trail_value_columns_type.sql"
    "V109__add_renewal_stage_to_opportunity_stage_enum.sql"
    "V122__add_data_quality_fields.sql"  # ALTER contacts
    "V123__Make_updated_fields_nullable.sql"
)

# Weitere Tabellen die von anderen abhängen
PHASE5=(
    "V114__fix_flyway_history.sql"
    "V115__create_cost_management_tables.sql"
    "V116__create_help_system_tables.sql"
    "V117__fix_help_system_column_types.sql"
    "V118__remove_redundant_contact_trigger.sql"
    "V119__fix_postgresql_specific_helpfulness_index.sql"
    "V120__create_contact_interaction_table.sql"  # REFERENCES contacts
    "V124__Add_last_modified_audit_fields.sql"
)

# SCHRITT 2: Benenne alle Dateien temporär um
echo -e "\n${YELLOW}📝 Phase 1: Temporäre Umbenennung...${NC}"
cd "$MIGRATION_DIR"
for f in V*.sql; do
    if [ -f "$f" ]; then
        mv "$f" "TEMP_$f"
    fi
done
cd - > /dev/null

# SCHRITT 3: Benenne in korrekter Reihenfolge um
echo -e "\n${YELLOW}📝 Phase 2: Setze neue Reihenfolge...${NC}"
VERSION=1

# Funktion zum Umbenennen
rename_migration() {
    local OLD_NAME=$1
    local TEMP_NAME="TEMP_$OLD_NAME"
    
    if [ -f "$MIGRATION_DIR/$TEMP_NAME" ]; then
        # Extrahiere den beschreibenden Teil
        local SUFFIX=$(echo "$OLD_NAME" | sed 's/V[0-9]*__//')
        local NEW_NAME="V${VERSION}__${SUFFIX}"
        
        echo "  V$VERSION: $SUFFIX"
        
        # Update Version im Datei-Inhalt
        sed -i.bak "s/-- V[0-9]*:/-- V${VERSION}:/" "$MIGRATION_DIR/$TEMP_NAME"
        sed -i.bak "s/Migration V[0-9]*:/Migration V${VERSION}:/" "$MIGRATION_DIR/$TEMP_NAME"
        rm "$MIGRATION_DIR/${TEMP_NAME}.bak"
        
        # Benenne Datei um
        mv "$MIGRATION_DIR/$TEMP_NAME" "$MIGRATION_DIR/$NEW_NAME"
        
        VERSION=$((VERSION + 1))
    fi
}

# Benenne alle Phasen in Reihenfolge
echo -e "\n${BLUE}Basis-Tabellen:${NC}"
for f in "${PHASE1[@]}"; do rename_migration "$f"; done

echo -e "\n${BLUE}Customer-Tabellen:${NC}"
for f in "${PHASE2[@]}"; do rename_migration "$f"; done

echo -e "\n${BLUE}Weitere Basis-Tabellen:${NC}"
for f in "${PHASE3[@]}"; do rename_migration "$f"; done

echo -e "\n${BLUE}Tabellen-Modifikationen:${NC}"
for f in "${PHASE4[@]}"; do rename_migration "$f"; done

echo -e "\n${BLUE}Abhängige Tabellen:${NC}"
for f in "${PHASE5[@]}"; do rename_migration "$f"; done

# SCHRITT 4: Verarbeite alle übrigen Dateien
echo -e "\n${BLUE}Übrige Migrationen:${NC}"
for f in $MIGRATION_DIR/TEMP_V*.sql; do
    if [ -f "$f" ]; then
        BASENAME=$(basename "$f" | sed 's/TEMP_//')
        rename_migration "$BASENAME"
    fi
done

# SCHRITT 5: Finale Überprüfung
echo -e "\n${GREEN}✅ Neue Reihenfolge gesetzt!${NC}"
echo -e "\n${BLUE}📋 Neue Migration-Übersicht:${NC}"
ls $MIGRATION_DIR/V*.sql | head -20 | while read f; do
    VERSION=$(basename "$f" | sed 's/V\([0-9]*\)__.*/\1/')
    NAME=$(basename "$f" | sed 's/V[0-9]*__//' | sed 's/.sql//')
    printf "V%-3s: %s\n" "$VERSION" "$NAME"
done

echo -e "\n${YELLOW}💡 Nächste Schritte:${NC}"
echo "1. mvn clean compile"
echo "2. mvn test"
echo "3. Bei Erfolg: git add und commit"

echo -e "\n${GREEN}✨ Fertig!${NC}"