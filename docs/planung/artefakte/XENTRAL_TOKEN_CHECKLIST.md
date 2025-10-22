# 🔐 Xentral API Token - Erstellungs-Checkliste

**Erstellt:** 2025-10-20
**Quelle:** https://help.xentral.com/hc/de/articles/22627542067740-Personal-Access-Tokens-PAT-in-Xentral
**Für:** Sprint 2.1.7.2 - Xentral Integration
**Status:** ⚠️ Token muss neu erstellt werden

**⚠️ WICHTIGER HINWEIS (2025-10-20):**
Die im Xentral Help-Artikel beschriebenen **Scopes/CRUD-Rechte sind noch NICHT verfügbar** (Early Access/Beta).
Aktuell: **Token = volle User-Rechte** (alle API-Bereiche des Benutzers).
Rechte-Steuerung erfolgt über **Benutzerprofil**, nicht über Token selbst.

---

## 📋 SCHRITT-FÜR-SCHRITT ANLEITUNG

### **1. Token-Erstellung öffnen**

**Navigation:**
```
Xentral → Oben rechts Icon (JS) → Kontoeinstellungen
→ Developer Einstellungen
→ Personal Access Tokens
→ Button "Token erzeugen" (oben rechts)
```

**Berechtigungen:**
- ✅ Nur Admins dürfen Tokens erstellen
- ✅ Jörg hat Admin-Rechte

---

### **2. Allgemeine Angaben**

**Name:** (standardisiertes Schema verwenden)
```
FreshPlan-CRM-Sync-Production
```

**Beschreibung:**
```
API-Token für FreshPlan Sales Tool Integration
- Customer-Sync (Sales-Rep Mapping)
- Delivery-Note Status Polling (PROSPECT → AKTIV)
- Invoice Payment Tracking (Zahlungsverhalten)
Sprint 2.1.7.2 + 2.1.7.4
Erstellt: 2025-10-20
Verantwortlich: Jörg Streeck
```

**Laufzeit:**
- **Empfehlung:** 365 Tage (1 Jahr)
- **Standard:** 180 Tage
- **Automatische Erinnerung:** 180 Tage Inaktivität → Mail mit Verlängerungsoption

---

### **3. Scopes/CRUD-Rechte** ⚠️ AKTUELL NICHT VERFÜGBAR!

**⚠️ WICHTIGE KORREKTUR (2025-10-20):**

Die im Xentral Help-Artikel beschriebenen **granularen Scopes und CRUD-Rechte sind bei den meisten Xentral-Installationen noch NICHT verfügbar**.

**Aktueller Stand:**
- ❌ Scope-Auswahl (CRM, Verkauf, Buchhaltung) = **Early Access/Beta**
- ❌ CRUD-Rechte (Read, Create, Update, Delete) = **Early Access/Beta**
- ✅ **Token = volle User-Rechte** (alle API-Bereiche)

**Was das bedeutet:**
```
Token-Rechte = Alle Rechte des Benutzers (Jörg Streeck)
→ Zugriff auf: Customers, Delivery-Notes, Invoices, alle anderen Module
→ Operationen: Read, Create, Update, Delete (je nach User-Berechtigung)
```

**Rechte-Steuerung erfolgt über:**
- ✅ **Benutzerprofil** (Jörgs Account-Rechte in Xentral)
- ❌ NICHT über Token selbst (kein granulares Scoping möglich)

**Sicherheits-Implikation:**
⚠️ **Token hat ALLE Rechte des Users!**
→ Besonders wichtig: Token sicher aufbewahren (Passwort-Manager)
→ Keine Read-Only-Beschränkung möglich (würde User-Rechte erfordern)

**Zukünftig (Early Access/Beta):**
- Xentral rollt granulare Token-Scopes aus
- Dann: Pro Token Scopes + CRUD-Rechte konfigurierbar
- Bis dahin: Token = volle User-Rechte

---

### **5. Token erstellen und sichern** 🔒

**KRITISCH:** Token wird **NUR EINMAL** angezeigt!

**Schritte:**
1. ✅ Button "Erstellen" klicken
2. ✅ Token sofort kopieren (wird nicht mehr angezeigt!)
3. ✅ Token in Passwort-Manager speichern:
   - **Name:** Xentral-API-FreshPlan-Production
   - **URL:** https://644b6ff97320d.xentral.biz/api/v1
   - **Token:** (generierter Wert)
   - **Notiz:** User-Rechte (Jörg Streeck), Ablaufdatum, volle API-Rechte
4. ✅ Token Claude geben für Sprint 2.1.7.2

**⚠️ KRITISCHE Sicherheitsmaßnahmen (Token hat VOLLE User-Rechte!):**
- ❌ NIEMALS per E-Mail/Chat unverschlüsselt versenden
- ❌ NIEMALS in Git-Repository committen
- ❌ NIEMALS in Frontend-Code einbetten
- ❌ NIEMALS mit anderen Personen teilen
- ✅ Nur in Backend Environment-Variable (`XENTRAL_API_TOKEN`)
- ✅ Besonders sicher aufbewahren (volle Schreibrechte!)

**⚠️ WICHTIG:** Da Token ALLE User-Rechte hat (inkl. Create/Update/Delete):
- Backend muss Read-Only-Logik selbst implementieren
- Nie direkt User-Input in Write-APIs durchreichen
- Validation + Sanitization auf Backend-Seite

---

### **6. Dokumentation**

**Interne Doku pflegen:**
```markdown
## Xentral API Token

**Name:** FreshPlan-CRM-Sync-Production
**Erstellt:** 2025-10-20
**Ersteller:** Jörg Streeck (User-Account)
**Ablaufdatum:** 2026-10-20 (365 Tage)
**Zweck:** FreshPlan CRM Integration (Sprint 2.1.7.2 + 2.1.7.4)

**Rechte:**
- ⚠️ **VOLLE User-Rechte** (Jörg Streeck Account)
- Zugriff: Alle API-Bereiche (CRM, Verkauf, Buchhaltung, etc.)
- Operationen: Read, Create, Update, Delete (je nach User-Berechtigung)
- Granulare Scopes: Nicht verfügbar (Early Access/Beta)

**Verwendung in FreshPlan:**
- Phase 1 (Sprint 2.1.7.2/2.1.7.4): Nur Read-Operationen
- Read-Only Logik: Im Backend implementiert (nicht Token-Ebene)
- Write-Operationen: Für später (Phase 2+)

**Sicherheit:**
- ⚠️ Token hat Schreibrechte → besonders kritisch!
- Token in 1Password gespeichert
- Nur Backend hat Zugriff (Environment-Variable)
- Keine Weitergabe an Dritte
- Review: alle 6 Monate
```

---

## 🧪 TOKEN-VALIDIERUNG (nach Erstellung)

**Test-Call durchführen:**

```bash
# 1. Customer-Daten testen (sollte funktionieren)
curl -s -H 'Authorization: Bearer {TOKEN}' \
'https://644b6ff97320d.xentral.biz/api/v1/customers?page%5Bsize%5D=1'

# Erwartete Response: JSON mit Customer-Daten
# Fehler-Response: 401/403 → Scopes prüfen!

# 2. Delivery-Notes testen
curl -s -H 'Authorization: Bearer {TOKEN}' \
'https://644b6ff97320d.xentral.biz/api/v1/delivery-notes?page%5Bsize%5D=1'

# 3. Invoices testen
curl -s -H 'Authorization: Bearer {TOKEN}' \
'https://644b6ff97320d.xentral.biz/api/v1/invoices?page%5Bsize%5D=1'
```

**Erfolg-Kriterien:**
- ✅ Alle 3 Endpoints geben JSON zurück (nicht Redirect auf /login)
- ✅ Kein 401 Unauthorized
- ✅ Kein 403 Forbidden

---

## 🔄 SPÄTER: ERWEITERTE RECHTE (Phase 2)

**Wenn wir Customer-Sync implementieren (später):**

**Zusätzliche CRUD-Rechte benötigt:**
- **Customers:** Update (für Sales-Rep Mapping Sync)

**Dann neuen Token erstellen:**
- Alter Token löschen (Security)
- Neuer Token mit erweiterten Rechten
- Dokumentation aktualisieren

---

## ⚠️ HÄUFIGE FEHLER

### **Problem 1: Redirect auf /login**
**Ursache:** User hat keine API-Berechtigung oder Token ist ungültig
**Lösung:**
- Prüfe ob User API-Zugriff hat (Admin-Rechte meist nötig)
- Token neu erstellen
- Richtige Base-URL verwenden (`/api/v1` nicht `/customerlistv2`)

### **Problem 2: 401 Unauthorized**
**Ursache:** Token abgelaufen (nach 180/365 Tagen)
**Lösung:** Laufzeit verlängern oder neuen Token erstellen

### **Problem 3: 403 Forbidden**
**Ursache:** User hat keine Berechtigung für diese Ressource
**Lösung:**
- Prüfe Benutzer-Rechte in Xentral (nicht Token-Rechte!)
- Jörg braucht Zugriff auf CRM, Verkauf, Buchhaltung Module
- Token übernimmt User-Rechte automatisch

### **Problem 4: Token versehentlich gelöscht**
**Ursache:** Token wurde nach 180 Tagen Inaktivität auto-gelöscht
**Lösung:** Neuen Token erstellen, System regelmäßig nutzen (Polling 1x täglich → kein Auto-Delete)

---

## 📊 TOKEN-VERWALTUNG

### **Admin-Übersicht (nur für Admins):**
```
Smart Search → "Personal Access Token" suchen
→ Zeigt alle Tokens im System
```

**Was sichtbar ist:**
- Token-Name
- Scopes
- Ablaufdatum
- Status (aktiv/gesperrt)
- Zuletzt verwendet
- Ersteller (Admin)

### **Lock-Funktion:**
Admins können Tokens sperren (z.B. bei Sicherheitsvorfällen)

### **Automatisches Entfernen:**
> "Tokens, die länger als 180 Tage nicht genutzt wurden, werden automatisch gelöscht. Vorher erhält der Besitzer Erinnerungsmails mit der Möglichkeit zur Verlängerung."

**Unsere Situation:**
✅ Polling 1x täglich (02:00 Uhr) → Token wird regelmäßig genutzt → kein Auto-Delete!

---

## 📅 REVIEW-ZYKLUS

**Empfohlene Reviews:**
- **Monatlich:** Token-Nutzung prüfen (Admin-Übersicht)
- **Quartalsweise:** CRUD-Rechte evaluieren (Minimalprinzip)
- **Jährlich:** Token-Rotation (neuer Token, alter löschen)

**Vor Ablaufdatum:**
- 30 Tage vorher: Erinnerung erstellen
- Laufzeit verlängern oder neuen Token erstellen

---

## ✅ CHECKLISTE - ZUSAMMENFASSUNG

**Vor Token-Erstellung:**
- [ ] Namensschema definiert
- [ ] Beschreibung geschrieben (Zweck, Verantwortlicher, Sprint)
- [ ] Laufzeit festgelegt (365 Tage empfohlen)
- [ ] User-Rechte geprüft (Jörg hat Admin/API-Zugriff?)
- [ ] Verstanden: Token = volle User-Rechte (keine Scopes)

**Bei Token-Erstellung:**
- [ ] Name + Beschreibung eingegeben
- [ ] Laufzeit gesetzt (365 Tage)
- [ ] ~~Scopes aktiviert~~ (nicht verfügbar - Token hat volle User-Rechte)
- [ ] ~~CRUD gesetzt~~ (nicht verfügbar - Token hat volle User-Rechte)
- [ ] Token sofort kopiert (wird nicht mehr angezeigt!)
- [ ] Token in Passwort-Manager gespeichert

**Nach Token-Erstellung:**
- [ ] Test-Calls durchgeführt (3 Endpoints)
- [ ] Dokumentation aktualisiert
- [ ] Token Claude gegeben (für Sprint 2.1.7.2)
- [ ] Environment-Variable im Backend gesetzt

---

## 🔗 REFERENZEN

**Xentral Dokumentation:**
- Token-Erstellung: https://help.xentral.com/hc/de/articles/22627542067740-Personal-Access-Tokens-PAT-in-Xentral
- API-Doku: https://developer.xentral.com/reference/intro

**FreshPlan Dokumentation:**
- Xentral API Info: [`XENTRAL_API_INFO.md`](./XENTRAL_API_INFO.md)
- Screenshot-Findings: [`XENTRAL_SCREENSHOTS_FINDINGS.md`](./XENTRAL_SCREENSHOTS_FINDINGS.md)
- Sprint 2.1.7.2 Trigger: [`../TRIGGER_SPRINT_2_1_7_2.md`](../TRIGGER_SPRINT_2_1_7_2.md)
- Sprint 2.1.7.4 Trigger: [`../TRIGGER_SPRINT_2_1_7_4.md`](../TRIGGER_SPRINT_2_1_7_4.md)

---

**Letzte Aktualisierung:** 2025-10-20 23:45 (KORREKTUR: Scopes nicht verfügbar)
**Erstellt von:** Claude + Jörg (Xentral Help-Artikel + Community-Feedback)
**Status:** ✅ Ready für Token-Erstellung vor Sprint 2.1.7.2

**⚠️ KORREKTUR (2025-10-20):**
Granulare Scopes/CRUD-Rechte sind noch **NICHT verfügbar** (Early Access/Beta).
Token = volle User-Rechte. Dokumentation entsprechend angepasst.

**⚠️ AKTION:** Token vor Sprint 2.1.7.2 Start (in ~3-5 Tagen) neu erstellen!
