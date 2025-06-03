# GitHub Secrets Setup für E-Mail-Benachrichtigungen

## Erforderliche Secrets

Die folgenden Secrets müssen in GitHub unter **Settings → Secrets and variables → Actions** angelegt werden:

| Secret Name | Beschreibung | Beispielwert |
|-------------|--------------|--------------|
| `MAIL_SERVER` | SMTP Server Adresse | `smtp.gmail.com` |
| `MAIL_PORT` | SMTP Port | `465` |
| `MAIL_SECURE` | SSL/TLS aktiviert | `true` |
| `MAIL_USERNAME` | E-Mail-Adresse für Login | `noreply@freshplan.de` |
| `MAIL_PASSWORD` | App-Passwort (NICHT das normale Passwort!) | `xxxx xxxx xxxx xxxx` |

## Setup-Anleitung für Gmail

1. **2-Faktor-Authentifizierung aktivieren** (falls noch nicht geschehen)
   - Google-Konto → Sicherheit → 2FA einrichten

2. **App-Passwort generieren**
   - Google-Konto → Sicherheit → App-Passwörter
   - App auswählen: "Mail"
   - Gerät auswählen: "Andere" → Name: "FreshPlan CI"
   - Generiertes Passwort notieren (16 Zeichen ohne Leerzeichen)

3. **Secrets in GitHub anlegen**
   - Repository → Settings → Secrets and variables → Actions
   - "New repository secret" für jeden Wert
   - Namen EXAKT wie oben angegeben verwenden

## Alternative SMTP-Anbieter

### SendGrid
```
MAIL_SERVER: smtp.sendgrid.net
MAIL_PORT: 587
MAIL_SECURE: true
MAIL_USERNAME: apikey
MAIL_PASSWORD: SG.xxxxx (API Key)
```

### Mailgun
```
MAIL_SERVER: smtp.mailgun.org
MAIL_PORT: 587
MAIL_SECURE: true
MAIL_USERNAME: postmaster@mg.freshplan.de
MAIL_PASSWORD: xxxxx
```

## Test der E-Mail-Benachrichtigung

Nach dem Setup kann die Benachrichtigung getestet werden:

1. Workflow manuell triggern (Actions → Backup Smoke Tests → Run workflow)
2. Oder: Einen Test absichtlich fehlschlagen lassen
3. E-Mail sollte innerhalb von 2-3 Minuten ankommen

## Troubleshooting

- **"Authentication failed"**: App-Passwort prüfen, keine Leerzeichen
- **"Connection timeout"**: Firewall/Port prüfen
- **Keine Mail erhalten**: Spam-Ordner checken, SPF/DKIM einrichten