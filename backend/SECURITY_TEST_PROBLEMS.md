# FC-008 Security Foundation - Probleme bei der Implementierung

## Zusammenfassung
Ich habe versucht, die FC-008 Security Foundation basierend auf der CLAUDE_TECH Dokumentation zu implementieren und bin auf zahlreiche Probleme gestoßen.

## Gefundene Probleme

### 1. pom.xml: Fehlende Dependency
- **Problem:** Die Dokumentation sagt, ich soll `quarkus-smallrye-jwt` hinzufügen
- **Status:** Behoben - Dependency hinzugefügt

### 2. Keycloak Realm Name
- **Problem:** Dokumentation verwendet `freshplan`, tatsächlich heißt es `freshplan-realm`
- **Tatsächlich:** `quarkus.oidc.auth-server-url=${KEYCLOAK_URL:http://localhost:8180}/realms/${KEYCLOAK_REALM:freshplan-realm}`

### 3. SecurityContextProvider Implementierung
- **Problem:** Die existierende Implementierung unterscheidet sich stark von der Dokumentation
- **Dokumentation:** Erwartet UserRepository und findByKeycloakId
- **Tatsächlich:** Arbeitet nur mit JWT Claims, keine User-Erstellung

### 4. UserRepository ohne findByKeycloakId
- **Problem:** Die Methode `findByKeycloakId` existiert nicht im UserRepository
- **Auswirkung:** Kann Keycloak Users nicht mit lokalen Users verknüpfen

### 5. User Entity ohne keycloakId
- **Problem:** Die User Entity hat kein `keycloakId` Feld
- **Auswirkung:** Keine Verknüpfung zwischen Keycloak und lokalen Users möglich

### 6. RealityCheckResource mit alten Imports
- **Problem:** Verwendet `javax.ws.rs` statt `jakarta.ws.rs`
- **Status:** Behoben - Imports aktualisiert

### 7. Path Namenskonflikt
- **Problem:** Konflikt zwischen `java.nio.file.Path` und `jakarta.ws.rs.Path`
- **Status:** Behoben - Explizite Imports verwendet

### 8. apiClient TODO
- **Problem:** apiClient hat Token-Handling, aber es ist nicht mit AuthContext verbunden
- **Kommentar im Code:** `// TODO: Integrate with AuthContext once Keycloak is ready`

### 9. Test Failures
- **Problem:** Alle Security Tests schlagen fehl
- **Ursache:** SecurityContextProvider erwartet SecurityIdentity, die im Test nicht verfügbar ist

### 10. Test Configuration deaktiviert Security
- **Problem:** application.properties für Tests deaktiviert ALLE Security
- **Konflikt:** Security Tests erwarten aktive Security
- **Zeilen:** 18-35 in src/test/resources/application.properties

## Weitere Beobachtungen

### Bereits vorhandene Implementierungen
- Es gibt bereits einen `authenticatedApiClient` mit Keycloak Integration
- Es gibt bereits einen `useAuth` Hook im Frontend
- Es gibt bereits Auth-bezogene Components (AuthGuard, LoginPage, etc.)

### Diskrepanzen zur Dokumentation
Die CLAUDE_TECH Dokumentation scheint nicht die aktuelle Implementierung widerzuspiegeln:
- Andere Security-Architektur als dokumentiert
- Fehlende Felder und Methoden
- Andere Konfiguration als erwartet

## Fazit
Die Dokumentation und die tatsächliche Implementierung stimmen nicht überein. Es scheint, als ob die Security Foundation bereits teilweise implementiert ist, aber anders als in der Dokumentation beschrieben.

## Empfehlung
1. Die bestehende Implementierung analysieren und verstehen
2. Die Dokumentation an die tatsächliche Implementierung anpassen
3. Fehlende Teile identifizieren und gezielt ergänzen
4. Tests in einem separaten Security-Test-Profil laufen lassen, wo Security aktiviert ist