# 🔐 Xentral API Token - Erstellungs-Checkliste

**Erstellt:** 2025-10-20
**Quelle:** https://help.xentral.com/hc/de/articles/22627542067740-Personal-Access-Tokens-PAT-in-Xentral
**Für:** Sprint 2.1.7.2 - Xentral Integration
**Status:** ⚠️ Token muss neu erstellt werden (alter Token: keine Scopes)

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

### **3. Scopes (Zugriffsbereiche)** ⚠️ KRITISCH!

**Minimalprinzip:** Nur aktivieren was wir wirklich brauchen!

#### **Für Sprint 2.1.7.2 + 2.1.7.4 (Read-Only):**

| Scope | Benötigt | Begründung |
|-------|----------|------------|
| **CRM - Customers** | ✅ JA (Read) | Customer-Daten lesen (Sales-Rep, Kundennummer) |
| **Verkauf - Delivery-Notes** | ✅ JA (Read) | Lieferschein-Status prüfen (`VERSENDET`) |
| **Buchhaltung - Invoices** | ✅ JA (Read) | Rechnungen + Zahlungsverhalten lesen |
| **Admin read** | ❌ NEIN | Zu viele Rechte (Security) |
| **Admin** | ❌ NEIN | Zu viele Rechte (Security) |

**⚠️ WICHTIG:** Keine Create/Update/Delete Rechte für erste Phase!

---

### **4. CRUD-Rechte (feingranular)** ⚠️ KRITISCH!

**Für jede aktivierte Ressource:**

| Ressource | Create | Read | Update | Delete | Begründung |
|-----------|--------|------|--------|--------|------------|
| **Customers** | ❌ | ✅ | ❌ | ❌ | Nur Lesen (kein Schreiben!) |
| **Delivery-Notes** | ❌ | ✅ | ❌ | ❌ | Nur Status prüfen |
| **Invoices** | ❌ | ✅ | ❌ | ❌ | Nur Zahlungsdaten lesen |

**Minimalprinzip:**
> "Ein Reporting-Tool benötigt oft nur Leserechte"

→ **Wir brauchen für Phase 1 NUR Read-Rechte!**

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
   - **Notiz:** Scopes + CRUD + Ablaufdatum
4. ✅ Token Claude geben für Sprint 2.1.7.2

**⚠️ Sicherheitsmaßnahmen:**
- ❌ NIEMALS per E-Mail/Chat unverschlüsselt versenden
- ❌ NIEMALS in Git-Repository committen
- ❌ NIEMALS in Frontend-Code einbetten
- ✅ Nur in Backend Environment-Variable (`XENTRAL_API_TOKEN`)

---

### **6. Dokumentation**

**Interne Doku pflegen:**
```markdown
## Xentral API Token

**Name:** FreshPlan-CRM-Sync-Production
**Erstellt:** 2025-10-20
**Ersteller:** Jörg Streeck
**Ablaufdatum:** 2026-10-20 (365 Tage)
**Zweck:** FreshPlan CRM Integration (Sprint 2.1.7.2 + 2.1.7.4)

**Scopes:**
- CRM - Customers (Read)
- Verkauf - Delivery-Notes (Read)
- Buchhaltung - Invoices (Read)

**CRUD-Rechte:**
- Alle Ressourcen: Read-Only (keine Create/Update/Delete)

**Sicherheit:**
- Token in 1Password gespeichert
- Nur Backend hat Zugriff (Environment-Variable)
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
**Ursache:** Keine Scopes vergeben oder falsche Scopes
**Lösung:** Token neu erstellen mit korrekten Scopes (siehe Checkliste)

### **Problem 2: 401 Unauthorized**
**Ursache:** Token abgelaufen (nach 180/365 Tagen)
**Lösung:** Laufzeit verlängern oder neuen Token erstellen

### **Problem 3: 403 Forbidden**
**Ursache:** CRUD-Rechte fehlen (z.B. Read-Recht nicht aktiviert)
**Lösung:** Token neu erstellen mit Read-Rechten für alle benötigten Ressourcen

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
- [ ] Benötigte Scopes identifiziert (siehe Tabelle)
- [ ] CRUD-Rechte definiert (Read-Only für Phase 1)

**Bei Token-Erstellung:**
- [ ] Scopes aktiviert (CRM-Customers, Delivery-Notes, Invoices)
- [ ] CRUD auf Read-Only gesetzt
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

**Letzte Aktualisierung:** 2025-10-20 (nach Xentral Help-Center Review)
**Erstellt von:** Claude + Jörg (Xentral Help-Artikel)
**Status:** ✅ Ready für Token-Erstellung vor Sprint 2.1.7.2

**⚠️ AKTION:** Token vor Sprint 2.1.7.2 Start (in ~3-5 Tagen) neu erstellen!
