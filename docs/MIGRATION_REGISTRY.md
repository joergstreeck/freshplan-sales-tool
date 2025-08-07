# 📋 MIGRATION REGISTRY - KRITISCHES DOKUMENT

**⚠️ WARNUNG: Dieses Dokument ist VERBINDLICH für alle Entwickler (Menschen & KI)**

**Letzte Aktualisierung:** 02.08.2025, 18:36
**Höchste vergebene Nummer:** V120

## 🚫 TABU-LISTE: Diese Migrationsnummern sind VERGEBEN und UNVERÄNDERLICH

| Version | Dateiname | Status | Datum | Beschreibung |
|---------|-----------|---------|--------|--------------|
| V1 | V1__initial_schema.sql | ✅ Applied | 2025-06-xx | Initial Schema |
| V2 | V2__create_user_table.sql | ✅ Applied | 2025-06-xx | User Table |
| V3 | V3__add_user_roles.sql | ✅ Applied | 2025-06-xx | User Roles |
| V4 | V4__Create_profile_tables.sql | ✅ Applied | 2025-06-xx | Profile Tables |
| V5 | V5__Create_customer_tables.sql | ✅ Applied | 2025-06-xx | Customer Tables |
| V6 | V6__add_expansion_planned_field.sql | ✅ Applied | 2025-07-xx | Expansion Field |
| V7 | V7__create_opportunities_table.sql | ✅ Applied | 2025-07-xx | Opportunities |
| V8 | V8__add_data_quality_fields.sql | ✅ Applied | 2025-07-xx | Data Quality |
| V9 | V9__create_audit_trail.sql | ✅ Applied | 2025-07-xx | Audit Trail |
| V10 | V10__Complete_schema_alignment.sql | ✅ Applied | 2025-07-xx | Schema Alignment |
| V11 | V11__Make_updated_fields_nullable.sql | ✅ Applied | 2025-07-xx | Nullable Fields |
| V12 | V12__Add_last_modified_audit_fields.sql | ✅ Applied | 2025-07-xx | Last Modified |
| V13 | V13__Add_missing_timeline_event_columns.sql | ✅ Applied | 2025-07-xx | Timeline Columns |
| V14 | V14__Fix_contact_roles_columns.sql | ✅ Applied | 2025-07-xx | Contact Roles |
| V15 | V15__Add_last_contact_date_to_contacts.sql | ✅ Applied | 2025-07-xx | Last Contact |
| V16 | V16__Rename_preferred_contact_to_communication_method.sql | ✅ Applied | 2025-07-xx | Rename Column |
| V17 | V17__Add_missing_columns_for_entities.sql | ✅ Applied | 2025-07-xx | Missing Columns |
| V18 | V18__Add_missing_columns_for_entities.sql | ✅ Applied | 2025-07-xx | Missing Columns |
| V19 | V19__add_test_data_flag.sql | ✅ Applied | 2025-07-xx | Test Data Flag |
| V20 | V20__add_customer_search_performance_indices.sql | ✅ Applied | 2025-07-xx | Search Indices |
| V21 | V21__fix_audit_trail_ip_address_type.sql | ✅ Applied | 2025-07-xx | Fix IP Type |
| V22 | V22__fix_audit_trail_value_columns_type.sql | ✅ Applied | 2025-07-xx | Fix Value Type |
| V23 | V23__add_renewal_stage_to_opportunity_stage_enum.sql | ✅ Applied | 2025-07-xx | Renewal Stage |
| V24 | V24__add_data_quality_fields.sql | ✅ Applied | 2025-07-xx | Data Quality |
| V25 | V25__Make_updated_fields_nullable.sql | ✅ Applied | 2025-07-xx | Nullable Fields |
| V26 | V26__fix_flyway_history.sql | ✅ Applied | 2025-07-xx | Fix History |
| V27 | V27__create_cost_management_tables.sql | ✅ Applied | 2025-07-xx | Cost Management |
| V28 | V28__create_help_system_tables.sql | ✅ Applied | 2025-07-xx | Help System |
| V29 | V29__fix_help_system_column_types.sql | ✅ Applied | 2025-07-xx | Fix Help Types |
| V30 | V30__remove_redundant_contact_trigger.sql | ✅ Applied | 2025-07-xx | Remove Trigger |
| V31 | V31__fix_postgresql_specific_helpfulness_index.sql | ✅ Applied | 2025-07-xx | Fix Index |
| V32 | V32__create_contact_interaction_table.sql | ✅ Applied | 2025-07-xx | Contact Interactions |
| V33 | ~~V33__add_location_details~~ | 🗑️ Merged into V120 | - | JSONB location_details |
| V34 | ~~V34__add_service_offerings~~ | 🗑️ Merged into V120 | - | JSONB service_offerings |
| V35 | V35__add_sprint2_location_fields.sql | ✅ Applied | 2025-08-xx | Sprint 2 Location |
| V36 | V36__add_sprint2_pain_points_field.sql | ✅ Applied | 2025-08-xx | Sprint 2 Pain Points |
| V37 | V37__add_remaining_sprint2_fields.sql | ✅ Applied | 2025-08-xx | Sprint 2 Fields |
| **V38-V119** | - | ❌ RESERVIERT | - | Für zukünftige Nutzung |
| V120 | V120__fix_migration_duplicates.sql | ✅ Applied | 2025-08-02 | Consolidation Fix + JSONB |

## 🆕 NÄCHSTE VERFÜGBARE NUMMER: V121

## ⚠️ KRITISCHE REGELN:

1. **NIEMALS** eine Nummer aus der Tabu-Liste verwenden
2. **NIEMALS** eine bestehende Migration ändern oder löschen
3. **IMMER** dieses Dokument ZUERST lesen vor neuer Migration
4. **IMMER** dieses Dokument nach neuer Migration aktualisieren

## 📝 UPDATE-PROZESS:

Wenn du eine neue Migration erstellst:
```bash
# 1. Prüfe diese Liste
# 2. Nimm die nächste freie Nummer (aktuell: V121)
# 3. Erstelle deine Migration
# 4. Aktualisiere SOFORT diese Datei
```

## 🚨 HÄUFIGE FEHLER:

1. **Duplikate durch parallele Entwicklung**
   - Problem: Zwei Entwickler nutzen gleiche Nummer
   - Lösung: IMMER diese Liste prüfen

2. **Falsche Annahmen über freie Nummern**
   - Problem: "V38 sieht frei aus"
   - Lösung: NUR die explizit als NÄCHSTE markierte Nummer nutzen

## 🔄 AUTOMATISCHE PRÜFUNG:

```bash
# Script zum Prüfen auf Duplikate
find backend/src/main/resources/db/migration -name "*.sql" | \
  xargs -I {} basename {} | \
  sed 's/V\([0-9]*\)__.*/\1/' | \
  sort -n | uniq -d
```

## 📋 CHECKLISTE FÜR NEUE MIGRATIONEN:

- [ ] MIGRATION_REGISTRY.md gelesen
- [ ] Nächste freie Nummer notiert (V121)
- [ ] Migration erstellt mit korrekter Nummer
- [ ] Migration ist idempotent (IF NOT EXISTS, etc.)
- [ ] MIGRATION_REGISTRY.md aktualisiert
- [ ] Commit-Message enthält Migrationsnummer

---

**Hinweis für Claude:** Dieses Dokument MUSS bei jeder Übergabe erwähnt werden!