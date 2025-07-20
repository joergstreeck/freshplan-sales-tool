# FC-005: Xentral Integration - Provisions-Engine ⚡

**Feature Code:** FC-005  
**Feature-Typ:** 🔧 BACKEND  
**Geschätzter Aufwand:** 8-10 Tage  
**Priorität:** HOCH - Kern für Provisions-System  
**ROI:** Automatische Provisionsberechnung = 0 manuelle Fehler  

---

## 🎯 PROBLEM & LÖSUNG IN 30 SEKUNDEN

**Problem:** Provisionen manuell aus Xentral-Daten berechnen = Fehler + Zeitverlust  
**Lösung:** Webhook + API Integration für Echtzeit-Provisionsberechnung  
**Impact:** Zahlung in Xentral → Provision in CRM in <2 Sekunden  

---

## 💰 KERN-DATENFLUSS

```
Xentral → CRM:
1. Zahlung eingetroffen (Webhook) → Provision berechnet
2. Rechnung erstellt → Provisionsbasis bekannt
3. Teilzahlung → Anteilige Provision

CRM → Xentral:
1. Neuer Auftrag → Xentral-Auftrag
2. Kundendaten → Stammdaten-Sync
```

---

## 🏃 QUICK-START IMPLEMENTIERUNG

### Webhook Handler
```java
@Path("/webhooks/xentral")
public class XentralWebhookResource {
    @POST
    public Response handleWebhook(
        @HeaderParam("X-Xentral-Signature") String signature,
        XentralWebhookPayload payload
    ) {
        if (!validateSignature(signature, payload)) {
            return Response.status(401).build();
        }
        
        if (payload.getEvent().equals("payment.received")) {
            eventBus.publish(new PaymentReceivedEvent(
                payload.getInvoiceId(),
                payload.getAmount()
            ));
        }
        return Response.ok().build();
    }
}
```

### Provisions-Engine
```java
@ApplicationScoped
public class CommissionEngine {
    void onPaymentReceived(@Observes PaymentReceivedEvent event) {
        // 1. Verkäufer ermitteln
        var salesRep = getResponsibleRep(event.getCustomerId());
        
        // 2. Provision berechnen (3% Standard)
        var commission = event.getAmount() * 0.03;
        
        // 3. Speichern & Benachrichtigen
        saveCommission(salesRep, commission);
        notifySalesRep(salesRep, commission);
    }
}
```

### Sync Service (Fallback)
```java
@Scheduled(every = "6h")
void syncInvoices() {
    var invoices = xentralClient.getInvoicesSince(lastSync);
    invoices.forEach(this::processInvoice);
    updateLastSync();
}
```

---

## 🔗 DEPENDENCIES & INTEGRATION

**Abhängig von:**
- FC-004 Verkäuferschutz (wer bekommt Provision?)
- M5 Customer (Xentral-ID Mapping)
- OAuth Service (API Auth)

**Kritisch:**
- Xentral API Verfügbarkeit
- Webhook-Endpunkt öffentlich erreichbar

**Ermöglicht:**
- FC-007 Chef-Dashboard (Provisions-KPIs)
- FC-006 Mobile (Push bei Zahlung)

---

## ⚡ KRITISCHE ENTSCHEIDUNGEN

1. **Sync-Strategie:** Webhook + 6h Polling Hybrid?
2. **Daten-Storage:** Lokale Kopie oder nur Cache?
3. **Fehler-Handling:** Retry-Policy bei API-Ausfall?

---

## 📊 SUCCESS METRICS

- **Latenz:** <2s Webhook → Provision
- **Vollständigkeit:** 100% Zahlungen erfasst
- **Verfügbarkeit:** 99.9% (mit Fallback)
- **Genauigkeit:** 0 Provisionsfehler

---

## 🚀 NÄCHSTER SCHRITT

1. Xentral API Credentials beschaffen
2. Webhook-Endpunkt registrieren
3. OAuth Client implementieren

**Detaillierte Implementierung:** [IMPLEMENTATION_GUIDE.md](./IMPLEMENTATION_GUIDE.md)  
**API-Analyse:** [XENTRAL_API_ANALYSIS.md](/docs/technical/XENTRAL_API_ANALYSIS.md)

---

## 🧭 NAVIGATION & VERWEISE

### 📋 Zurück zum Überblick:
- **[📊 Master Plan V5](/docs/CRM_COMPLETE_MASTER_PLAN_V5.md)** - Vollständige Feature-Roadmap
- **[🗺️ Feature Overview](/docs/features/MASTER/FEATURE_OVERVIEW.md)** - Alle 40 Features im Überblick

### 🔗 Dependencies (Required):
- **[🔒 FC-008 Security Foundation](/docs/features/ACTIVE/01_security_foundation/FC-008_KOMPAKT.md)** - OAuth Service für API Auth
- **[🛡️ FC-004 Verkäuferschutz](/docs/features/PLANNED/07_verkaeuferschutz/FC-004_KOMPAKT.md)** - Wer bekommt Provision
- **[👥 M5 Customer Refactor](/docs/features/PLANNED/12_customer_refactor_m5/M5_KOMPAKT.md)** - Xentral-ID Mapping

### ⚡ Integriert mit:
- **[📊 M4 Opportunity Pipeline](/docs/features/ACTIVE/02_opportunity_pipeline/M4_KOMPAKT.md)** - Deal → Xentral-Auftrag
- **[🧮 M8 Calculator Modal](/docs/features/ACTIVE/03_calculator_modal/M8_KOMPAKT.md)** - Angebot → Xentral

### 🚀 Ermöglicht folgende Features:
- **[👨‍💼 FC-007 Chef-Dashboard](/docs/features/PLANNED/10_chef_dashboard/FC-007_KOMPAKT.md)** - Provisions-KPIs
- **[📱 FC-006 Mobile App](/docs/features/PLANNED/09_mobile_app/FC-006_KOMPAKT.md)** - Push bei Zahlung
- **[📈 FC-019 Advanced Sales Metrics](/docs/features/PLANNED/19_advanced_metrics/FC-019_KOMPAKT.md)** - Umsatz-Analytics
- **[🎯 FC-027 Magic Moments](/docs/features/PLANNED/27_magic_moments/FC-027_KOMPAKT.md)** - Zahlungseingang-Trigger

### 🎨 UI Integration:
- **[📊 M3 Sales Cockpit](/docs/features/ACTIVE/05_ui_foundation/M3_SALES_COCKPIT_KOMPAKT.md)** - Provisions-Anzeige
- **[⚙️ M7 Settings](/docs/features/ACTIVE/05_ui_foundation/M7_SETTINGS_KOMPAKT.md)** - Xentral-Konfiguration

### 🔧 Technische Details:
- **[IMPLEMENTATION_GUIDE.md](./IMPLEMENTATION_GUIDE.md)** - Vollständige technische Umsetzung
- **[XENTRAL_API_ANALYSIS.md](/docs/technical/XENTRAL_API_ANALYSIS.md)** - API-Dokumentation