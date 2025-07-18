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