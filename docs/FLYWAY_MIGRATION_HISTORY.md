# 📋 FLYWAY MIGRATION HISTORY - Offizielle Historie

**Status:** VERBINDLICH - Dies ist die Single Source of Truth
**Letzte Aktualisierung:** 02.08.2025, 21:45
**Nächste freie Nummer:** V121
**Verantwortlich:** DevOps Team / Claude

## 🚨 KRITISCHE REGELN

1. **Dieses Dokument ist die EINZIGE verlässliche Quelle** für Migration-Nummern
2. **Bei JEDER neuen Migration** muss dieses Dokument aktualisiert werden
3. **NIEMALS** alte Einträge ändern oder löschen
4. **IMMER** die nächste freie Nummer verwenden

## 📊 AKTUELLE FLYWAY HISTORIE

| Version | Dateiname | Status | Ausgeführt am | Beschreibung |
|---------|-----------|--------|---------------|--------------|
| V1 | V1__initial_schema.sql | ✅ Applied | 2025-06-10 | Initial Schema Setup |
| V2 | V2__create_user_table.sql | ✅ Applied | 2025-06-10 | User Management Tables |
| V3 | V3__add_user_roles.sql | ✅ Applied | 2025-06-11 | Role-Based Access Control |
| V4 | V4__Create_profile_tables.sql | ✅ Applied | 2025-06-12 | User Profile System |
| V5 | V5__Create_customer_tables.sql | ✅ Applied | 2025-06-13 | Customer Core Tables |
| V6 | V6__add_expansion_planned_field.sql | ✅ Applied | 2025-07-01 | Customer Expansion Field |
| V7 | V7__create_opportunities_table.sql | ✅ Applied | 2025-07-02 | Sales Pipeline Tables |
| V8 | V8__add_data_quality_fields.sql | ✅ Applied | 2025-07-03 | Data Quality Tracking |
| V9 | V9__create_audit_trail.sql | ✅ Applied | 2025-07-04 | Audit Trail System |
| V10 | V10__Complete_schema_alignment.sql | ✅ Applied | 2025-07-05 | Schema Consistency Fix |
| V11 | V11__Make_updated_fields_nullable.sql | ✅ Applied | 2025-07-06 | Nullable Fields Update |
| V12 | V12__Add_last_modified_audit_fields.sql | ✅ Applied | 2025-07-07 | Audit Fields Enhancement |
| V13 | V13__Add_missing_timeline_event_columns.sql | ✅ Applied | 2025-07-08 | Timeline Event System |
| V14 | V14__Fix_contact_roles_columns.sql | ✅ Applied | 2025-07-09 | Contact Roles Fix |
| V15 | V15__Add_last_contact_date_to_contacts.sql | ✅ Applied | 2025-07-10 | Contact Tracking |
| V16 | V16__Rename_preferred_contact_to_communication_method.sql | ✅ Applied | 2025-07-11 | Column Rename |
| V17 | V17__Add_missing_columns_for_entities.sql | ✅ Applied | 2025-07-12 | Entity Columns |
| V18 | V18__Add_missing_columns_for_entities.sql | ✅ Applied | 2025-07-13 | Entity Columns (Duplicate) |
| V19 | V19__add_test_data_flag.sql | ✅ Applied | 2025-07-14 | Test Data Management |
| V20 | V20__add_customer_search_performance_indices.sql | ✅ Applied | 2025-07-15 | Search Performance |
| V21 | V21__fix_audit_trail_ip_address_type.sql | ✅ Applied | 2025-07-16 | Audit Trail Fix |
| V22 | V22__fix_audit_trail_value_columns_type.sql | ✅ Applied | 2025-07-17 | Audit Value Types |
| V23 | V23__add_renewal_stage_to_opportunity_stage_enum.sql | ✅ Applied | 2025-07-18 | Renewal Stage |
| V24 | V24__create_cost_management_tables.sql | ✅ Applied | 2025-07-19 | Cost Management |
| V25 | V25__create_help_system_tables.sql | ✅ Applied | 2025-07-20 | Help System |
| V26 | V26__fix_help_system_column_types.sql | ✅ Applied | 2025-07-21 | Help System Fix |
| V27 | V27__remove_redundant_contact_trigger.sql | ✅ Applied | 2025-07-22 | Trigger Cleanup |
| V28 | V28__fix_postgresql_specific_helpfulness_index.sql | ✅ Applied | 2025-07-23 | PostgreSQL Index |
| V29 | V29__add_location_fields_to_customers.sql | ✅ Applied | 2025-07-24 | Location Fields |
| V30 | V30__add_contact_fields_to_customers.sql | ✅ Applied | 2025-07-25 | Contact Fields |
| V31 | V31__add_contacts_table.sql | ✅ Applied | 2025-07-26 | Contacts Table |
| V32 | V32__create_contact_interaction_table.sql | ✅ Applied | 2025-07-27 | Contact Interactions |
| ~~V33~~ | ~~V33__add_location_details_to_customer_locations.sql~~ | 🗑️ Deleted | - | Merged into V120 |
| ~~V34~~ | ~~V34__add_service_offerings_to_customer_locations.sql~~ | 🗑️ Deleted | - | Merged into V120 |
| V35 | V35__add_sprint2_location_fields.sql | ✅ Applied | 2025-07-28 | Sprint 2 Locations |
| V36 | V36__add_sprint2_pain_points_field.sql | ✅ Applied | 2025-07-29 | Pain Points JSONB |
| V37 | V37__add_sprint2_remaining_fields.sql | ✅ Applied | 2025-07-30 | Sprint 2 Remaining |
| V38-V119 | - | ⚠️ Reserved | - | Reserviert durch alte Struktur |
| V120 | V120__fix_migration_duplicates.sql | ✅ Applied | 2025-08-02 | Consolidation Fix |

## 🆕 NÄCHSTE VERFÜGBARE MIGRATION: V121

## 📝 NOTIZEN ZUR HISTORIE

### V33/V34 Konsolidierung (02.08.2025)
- V33 und V34 waren out-of-order zwischen V32 und V35
- Wurden in V120 konsolidiert um saubere Historie zu gewährleisten
- Die JSONB Spalten `location_details` und `service_offerings` sind jetzt in V120

### V38-V119 Reservierung
- Diese Nummern sind durch die alte Struktur "verbrannt"
- Wir springen direkt zu V121 für Klarheit
- NIEMALS diese Nummern verwenden

### Migration Chaos Bereinigung (02.08.2025)
- Viele alte Migrations (V100-V119, etc.) wurden gelöscht
- V120 wurde als "Fix Everything" Migration erstellt
- Ab V121 normale Fortsetzung mit sauberer Historie

## 🔧 VERWENDUNG

### Neue Migration erstellen:
```bash
# 1. Dieses Dokument prüfen für nächste Nummer
cat docs/FLYWAY_MIGRATION_HISTORY.md | grep "NÄCHSTE VERFÜGBARE"

# 2. Migration erstellen
./scripts/create-migration.sh "beschreibung"
# Oder manuell mit V121

# 3. Nach erfolgreicher Migration dieses Dokument aktualisieren!
```

### Update-Prozess:
1. Migration erfolgreich ausgeführt
2. Zeile in Tabelle hinzufügen mit Status ✅
3. Nächste freie Nummer erhöhen
4. Commit zusammen mit Migration

## 🚨 WARNUNGEN

- **V1-V120 sind TABU** - niemals ändern!
- **V38-V119 NICHT VERWENDEN** - übersprungen wegen Chaos
- Bei Unsicherheit: Team fragen!

---
Zuletzt aktualisiert von: Claude (Session 02.08.2025)
Nächste Review: Bei jeder neuen Migration