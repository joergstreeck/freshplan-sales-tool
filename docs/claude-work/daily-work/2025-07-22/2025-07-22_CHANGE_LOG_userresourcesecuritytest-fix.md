# CHANGE LOG: UserResourceSecurityTest Fixes
**Datum:** 22.07.2025  
**Feature:** FC-008 Security Foundation  
**TODO:** TODO-77 - UserResourceSecurityTest Failures beheben  
**Author:** Claude  

## 🎯 Ziel
Die fehlschlagenden UserResourceSecurityTest Tests reparieren, die versuchen als non-Admin User zu erstellen.

## 📋 Problem
5 Tests in UserResourceSecurityTest schlugen fehl:
- managerShouldNotBeAbleToUpdateUsers
- managerShouldNotBeAbleToDeleteUsers  
- managerShouldNotBeAbleToUpdateUserRoles
- salesShouldNotBeAbleToUpdateUserRoles
- unauthenticatedShouldNotBeAbleToUpdateUserRoles

Alle Tests riefen `createTestUser()` auf, was einen POST auf `/api/users` macht. Da die Tests aber mit non-Admin Rollen laufen (@TestSecurity mit "manager" oder "sales"), bekamen sie 403 Forbidden statt der erwarteten 201 Created.

## 🔍 Analyse

### Root Cause
Die `createTestUser()` Methode versuchte einen echten User zu erstellen, aber nur Admins dürfen das. Die Tests liefen im Security-Kontext von "manager" oder "sales", die keine Berechtigung zum User-Erstellen haben.

### Lösungsansätze geprüft
1. ❌ @BeforeAll mit @TestSecurity - funktioniert nicht zusammen
2. ❌ Helper-Methode mit @TestSecurity - funktioniert nur für Test-Methoden
3. ✅ Separate Test-Klasse mit statischer UUID für non-Admin Tests

## 🛠️ Implementierung

### Schritt 1: Neue Test-Klasse erstellt
`UserResourceSecurityNonAdminTest.java` mit:
- Statischer UUID die nicht existieren muss
- Tests die nur Security prüfen (403/401 Responses)
- Kein Setup von echten Test-Daten nötig

### Schritt 2: Problematische Tests verschoben
Die 5 fehlschlagenden Tests aus `UserResourceSecurityTest.java` entfernt und in die neue Klasse verschoben.

### Schritt 3: Tests angepasst
Statt `createTestUser()` zu rufen, verwenden die Tests jetzt eine statische UUID:
```java
private static final UUID TEST_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
```

## 📊 Ergebnisse

### Vorher
- UserResourceSecurityTest: 18/23 Tests grün (5 Failures)

### Nachher  
- UserResourceSecurityTest: 18/18 Tests grün ✅
- UserResourceSecurityNonAdminTest: 5/5 Tests grün ✅
- Gesamt: 23/23 Tests grün ✅

## 🔧 Dateien geändert

1. **UserResourceSecurityTest.java**
   - 5 problematische Tests entfernt
   - Kommentare hinzugefügt dass Tests verschoben wurden

2. **UserResourceSecurityNonAdminTest.java** (NEU)
   - Neue Test-Klasse für non-Admin Security Tests
   - Verwendet statische UUID statt echte User zu erstellen
   - Alle 5 Tests erfolgreich implementiert

## ✅ FC-008 Status Update
- FC-008 Security Foundation ist jetzt 100% fertig!
- Alle Security Tests laufen grün
- TODO-77 erfolgreich abgeschlossen