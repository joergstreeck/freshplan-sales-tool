# Number Assignment Workflow (Template)
# 1) Im Planning bleiben Dateien als VXXX__*.sql unter /artefakte/migration-templates/sql/
# 2) Vor Production:
#    ./scripts/get-next-migration.sh -> liefert nächste freie Nummer (z. B. 226)
#    ./migration-template-generator.sh sql/VXXX__user_lead_protection_foundation.sql ../backend/src/main/resources/db/migration
# 3) CI:
#    ./scripts/validate-migration-template.sh ../backend/src/main/resources/db/migration
# 4) Nach Rollout: Contract-Cleanup-PR (alte Logik entfernen)
