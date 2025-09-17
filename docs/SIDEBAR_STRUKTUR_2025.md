# Sidebar-Struktur Stand 17.09.2025

## Implementierungs-Status

✅ = Funktionalität vorhanden
⏳ = Placeholder-Seite
❌ = Route fehlt

## Navigation-Struktur

### 🏠 Mein Cockpit ✅
- **Route:** `/cockpit`
- **Status:** Funktional implementiert

### 📧 Neukundengewinnung
- **E-Mail Posteingang** ⏳
  - Route: `/neukundengewinnung/posteingang`
- **Lead-Erfassung** ⏳
  - Route: `/neukundengewinnung/leads`
- **Kampagnen** ⏳
  - Route: `/neukundengewinnung/kampagnen`

### 👥 Kundenmanagement
- **Alle Kunden** ✅
  - Route: `/customers`
- **Neuer Kunde** ✅
  - Action: Modal-Dialog (kein Route)
- **Verkaufschancen** ✅
  - Route: `/kundenmanagement/opportunities`
- **Aktivitäten** ⏳
  - Route: `/kundenmanagement/aktivitaeten`

### 📊 Auswertungen
- **Umsatzübersicht** ⏳
  - Route: `/berichte/umsatz`
- **Kundenanalyse** ⏳
  - Route: `/berichte/kunden`
- **Aktivitätsbericht** ⏳
  - Route: `/berichte/aktivitaeten`

### 💬 Kommunikation
- **Team-Chat** ⏳
  - Route: `/kommunikation/chat`
- **Ankündigungen** ⏳
  - Route: `/kommunikation/ankuendigungen`
- **Notizen** ⏳
  - Route: `/kommunikation/notizen`
- **Interne Nachrichten** ⏳
  - Route: `/kommunikation/nachrichten`

### ⚙️ Einstellungen
- **Mein Profil** ⏳
  - Route: `/einstellungen/profil`
- **Benachrichtigungen** ⏳
  - Route: `/einstellungen/benachrichtigungen`
- **Darstellung** ⏳
  - Route: `/einstellungen/darstellung`
- **Sicherheit** ⏳
  - Route: `/einstellungen/sicherheit`

### ❓ Hilfe & Support
- **Erste Schritte** ⏳
  - Route: `/hilfe/erste-schritte`
- **Handbücher** ⏳
  - Route: `/hilfe/handbuecher`
- **Video-Tutorials** ⏳
  - Route: `/hilfe/videos`
- **Häufige Fragen** ⏳
  - Route: `/hilfe/faq`
- **Support kontaktieren** ⏳
  - Route: `/hilfe/support`

### 🔐 Administrator
- **Audit Dashboard** ✅
  - Route: `/admin/audit`
- **Benutzerverwaltung** ✅
  - Route: `/admin/users`

#### System (Untermenü)
- **API Status** ✅
  - Route: `/admin/system/api-test`
- **System-Logs** ⏳
  - Route: `/admin/system/logs`
- **Performance** ⏳
  - Route: `/admin/system/performance`
- **Backup & Recovery** ⏳
  - Route: `/admin/system/backup`

#### Integration (Untermenü)
- **KI-Anbindungen** ⏳
  - Route: `/admin/integrationen/ki`
- **Xentral** ⏳
  - Route: `/admin/integrationen/xentral`
- **E-Mail Service** ⏳
  - Route: `/admin/integrationen/email`
- **Payment Provider** ⏳
  - Route: `/admin/integrationen/payment`
- **Webhooks** ⏳
  - Route: `/admin/integrationen/webhooks`
- **+ Neue Integration** ⏳
  - Route: `/admin/integrationen/neu`

#### Hilfe-Konfiguration (Untermenü)
- **Hilfe-System Demo** ✅
  - Route: `/admin/help/demo`
- **Tooltips verwalten** ⏳
  - Route: `/admin/help/tooltips`
- **Touren erstellen** ⏳
  - Route: `/admin/help/tours`
- **Analytics** ⏳
  - Route: `/admin/help/analytics`

- **Compliance Reports** ⏳
  - Route: `/admin/reports`

---

## Zusammenfassung

### Funktionale Bereiche (8 von 42)
✅ **Vollständig implementiert:**
1. Mein Cockpit
2. Alle Kunden
3. Neuer Kunde (Modal)
4. Verkaufschancen
5. Audit Dashboard
6. Benutzerverwaltung
7. API Status
8. Hilfe-System Demo

### Placeholder-Bereiche (34 von 42)
⏳ Alle anderen Menüpunkte zeigen aktuell Placeholder-Seiten mit:
- Beschreibung der geplanten Funktionalität
- Erwartetes Release-Datum
- Liste der geplanten Features
- Freshfoodz CI-konforme Darstellung

### Nächste Implementierungs-Prioritäten
Basierend auf Nutzen und Aufwand:
1. **Aktivitäten** - Basis für viele andere Features
2. **E-Mail Posteingang** - Wichtig für Neukundengewinnung
3. **Mein Profil** - Grundlegende Benutzerverwaltung
4. **Umsatzübersicht** - Wichtige Metrik für Vertrieb