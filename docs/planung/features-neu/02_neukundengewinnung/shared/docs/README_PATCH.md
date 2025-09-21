# Modul 02 – Foundation Standards Compliance Update
Stand: 2025-09-19

## Inhalt (Projekt-angepasst)
- **Package-Namen**: Korrigiert zu `de.freshplan` (statt `com.freshplan`)
- **Quarkus OIDC**: Native JsonWebToken Integration (statt SmallRye JWT direkt)
- **Database Migration**: V225 Lead-Tabelle mit Performance-Indices
- **FreshFoodz Theme**: Integration mit existierendem `/src/theme/freshfoodz.ts`
- **Security (ABAC)**: Territory-Scoping via Keycloak Claims
- **Repository Pattern**: Complete JPA Entity/DTO/Service Layer

## Integration (Schritte)
1. **Database:** Migration-Script kopieren (Nummer wird in Übergabe dokumentiert)
2. **Backend:** Alle Java-Klassen nach korrekten Packages kopieren
3. **Frontend:** FreshFoodz Theme bereits vorhanden → Test validiert korrekte Integration
4. **CI/CD:** Tests ausführen und Coverage-Gates beibehalten

## Hinweise
- JWT-basierte Tests benötigen Test-Key/Issuer-Konfiguration (SmallRye JWT). Für schnelle Pipeline-Tests ist der Header-Fallback aktiv.
- Export/Realtime sind in eurem Core bereits vorhanden und bleiben unverändert.
