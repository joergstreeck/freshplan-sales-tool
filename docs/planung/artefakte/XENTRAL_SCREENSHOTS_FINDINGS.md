# 📸 Xentral Screenshots - Erkenntnisse

**Datum:** 2025-10-20
**Quelle:** 3 Screenshots aus Xentral UI
**Genutzt von:** Sprint 2.1.7.4, Sprint 2.1.7.2
**Status:** ✅ KOMPLETT - Alle kritischen Felder identifiziert

---

## 🎯 ZUSAMMENFASSUNG

Aus 3 Xentral-Screenshots haben wir **ALLE** kritischen Informationen für beide Sprints:

| Bereich | Information | Quelle | Status |
|---------|-------------|--------|--------|
| **Kundennummer** | 5-stellig ohne Präfix (`56037`, `55509`) | Screenshot #1 (Adresse) | ✅ |
| **Sales-Rep** | Feld `"Vertrieb"` unter Zuordnung | Screenshot #1 (Adresse) | ✅ |
| **Account Manager** | Feld `"Innendienst"` (Fallback) | Screenshot #1 (Adresse) | ✅ |
| **Lieferstatus** | `Status = "VERSENDET"` (Trigger) | Screenshot #2 (Lieferschein) | ✅ |
| **Zahlungsstatus** | `Zahlungsstatus: "offen"` oder `"bezahlt"` | Screenshot #3 (Rechnung) | ✅ |
| **Bezahlt-Datum** | `Bezahlt am: DD.MM.YYYY` (leer wenn offen) | Screenshot #3 (Rechnung) | ✅ |
| **Mahndatum** | `Mahndatum: DD.MM.YYYY` | Screenshot #3 (Rechnung) | ✅ |
| **Lieferdatum** | `Lieferdatum: DD.MM.YYYY` | Screenshot #2+3 | ✅ |

---

## 📋 SCREENSHOT #1 - KUNDE/ADRESSE

**URL:** `644b6ff97320d.xentral.biz/app/x17?module=adresse&action=edit&id=3261`

### **Stammdaten**
- **Typ:** Firma (Dropdown)
- **Name:** Hotel NH Vienna Airport Conference Center
- **Kundennummer (Kunde):** `56037` ← **5-STELLIG OHNE PRÄFIX!**

### **Zuordnung** (kritisch für Sales-Rep Mapping!)
- **Vertrieb:** (Suchfeld mit Lupe) ← **PRIMARY Sales-Rep Feld**
- **Innendienst:** (Suchfeld mit Lupe) ← **FALLBACK Account Manager**
- **Hauptprojekt:** JBX-ORDER-TRANSFER

### **Kontaktdaten**
- Telefon, Telefax, Mobil, Anschreiben, E-Mail, Internetseite

### **Einstellungen**
- Liefersperre: (Datum 00.00.0000)
- Sprache für Belege: Deutsch (Dropdown)
- Kundenfreigabe: nein (Dropdown)

---

## 📋 SCREENSHOT #2 - LIEFERSCHEIN

**URL:** `644b6ff97320d.xentral.biz/app/x17?module=lieferschein&action=edit&id=12382`

**Titel:** Lieferschein (Bearbeiten) - NH Bingen Lieferschein XFJ720

### **Allgemein**
- **Kunde:** `55509` ← **5-STELLIG BESTÄTIGT!**
- **Projekt:** JBX-ORDER-TRANSFER
- **Status:** `VERSENDET` ← **TRIGGER-WERT für PROSPECT → AKTIV!**
- **Auftrag:** XFJ868
- **Ihre Bestellnummer:** 4507456531
- **Datum:** 20.10.2025
- **Lieferdatum:** 20.10.2025

### **Business-Entscheidung (User-Bestätigung):**
✅ **`Status = "VERSENDET"` reicht für Customer-Aktivierung!**

**Begründung:**
- Lieferschein mit Status VERSENDET = Commitment erfüllt
- Food-Logistik: Wenn versendet → wird auch geliefert
- Kein Paket-Tracking nötig (B2B-Direct-Delivery)

---

## 📋 SCREENSHOT #3 - RECHNUNG

**URL:** `644b6ff97320d.xentral.biz/app/x17?module=rechnung&action=edit&id=12171`

**Titel:** Rechnung (Bearbeiten) - NH Hoteles Deutsch. GmbH Rechnung XFJ1583

### **Allgemein**
- **Kunde:** `55509` ← **5-STELLIG KONSISTENT!**
- **Projekt:** JBX-ORDER-TRANSFER
- **Status:** `versendet` (Rechnung versendet)
- **Auftrag:** XFJ867
- **Ihre Bestellnummer:** 4507456478
- **Lieferdatum:** 20.10.2025
- **Lieferschein:** XFJ719
- **Datum:** 20.10.2025

### **Mahnwesen** (kritisch für Zahlungsverhalten!)
- **Zahlungsstatus:** `offen` (Dropdown: `offen` | `bezahlt`) ← **PAYMENT-STATUS**
- **Bezahlt am:** (leer) ← **NULL wenn nicht bezahlt, Datum wenn bezahlt**
- **SOLL:** 912.92
- **IST:** 0.00
- **Skonto gegeben:** 0 (als Geldbetrag)
- **Mahndatum:** 20.10.2025
- **Mahnstufe:** (Dropdown leer)

### **Zahlungsziel-Info** (aus User-Message)
- **Feld existiert:** `"Zahlungsziel (in Tagen):"` (nicht im Screenshot sichtbar)
- **Format:** Anzahl Tage (z.B. 14, 30, etc.)

---

## 🔍 ERKENNTNISSE FÜR SPRINT 2.1.7.4

### **XentralOrderEventHandler Interface**

```java
public interface XentralOrderEventHandler {
    /**
     * Handle Xentral Order Delivery Event
     *
     * @param xentralCustomerId 5-stellige Kundennummer (z.B. "55509")
     * @param deliveryNoteNumber Lieferschein-Nummer (z.B. "XFJ868")
     * @param shippedDate Datum als Status VERSENDET gesetzt wurde
     */
    void handleOrderDelivered(
        String xentralCustomerId,  // "55509" (5-stellig, kein Präfix!)
        String deliveryNoteNumber, // "XFJ868"
        LocalDate shippedDate      // Lieferdatum
    );
}
```

### **PROSPECT → AKTIV Logik**

```java
// Sprint 2.1.7.4 - Automatic Activation Service
public class XentralSyncService {

    public void checkDeliveredOrders() {
        // GET /api/v1/lieferschein?filter[0][key]=status&filter[0][op]=equals&filter[0][value]=VERSENDET

        List<DeliveryNote> versendetNotes = xentralApi.getDeliveryNotes(status = "VERSENDET");

        for (DeliveryNote note : versendetNotes) {
            String customerId = note.kunde; // "55509" (5-stellig)

            // Find FreshPlan Customer by xentral_customer_id
            Customer customer = customerRepo.findByXentralCustomerId(customerId);

            if (customer != null && customer.status == CustomerStatus.PROSPECT) {
                // ACTIVATE!
                customerService.activateCustomer(customer.id,
                    "Erste Lieferung: " + note.auftrag + " am " + note.datum);

                log.info("Customer {} activated via delivery note {}", customerId, note.id);
            }
        }
    }
}
```

---

## 🔍 ERKENNTNISSE FÜR SPRINT 2.1.7.2

### **Sales-Rep Mapping**

**Xentral Felder:**
- Primary: `Vertrieb` (Customer → Zuordnung)
- Fallback: `Innendienst` (Customer → Zuordnung)

**FreshPlan Mapping:**
```java
public class XentralCustomerSyncService {

    public void syncSalesReps() {
        // Schritt 1: Hole alle Xentral Customers
        List<XentralCustomer> xentralCustomers = xentralApi.getCustomers();

        for (XentralCustomer xc : xentralCustomers) {
            // Schritt 2: Sales-Rep ID holen (Primary: Vertrieb, Fallback: Innendienst)
            String salesRepId = xc.vertrieb != null ? xc.vertrieb : xc.innendienst;

            if (salesRepId == null) {
                log.warn("Customer {} has no Sales-Rep assigned", xc.id);
                continue;
            }

            // Schritt 3: Find FreshPlan User by xentral_user_id
            User freshplanUser = userRepo.findByXentralUserId(salesRepId);

            if (freshplanUser != null) {
                // Schritt 4: Update Customer
                Customer customer = customerRepo.findByXentralCustomerId(xc.id);
                if (customer != null) {
                    customer.assignedTo = freshplanUser.id;
                    customerRepo.save(customer);
                }
            }
        }
    }
}
```

### **Zahlungsverhalten Tracking**

**Xentral Felder:**
- `zahlungsstatus`: "offen" | "bezahlt"
- `bezahlt_am`: Date | null
- `mahndatum`: Date
- `zahlungsziel_tage`: Integer (Anzahl Tage)

**Berechnung:**
```java
public PaymentBehavior calculatePaymentBehavior(Invoice invoice) {
    if (invoice.zahlungsstatus.equals("offen")) {
        return PaymentBehavior.PENDING;
    }

    LocalDate invoiceDate = invoice.datum;
    LocalDate paymentDate = invoice.bezahlt_am;
    LocalDate dueDate = invoiceDate.plusDays(invoice.zahlungsziel_tage);

    long daysToPay = ChronoUnit.DAYS.between(invoiceDate, paymentDate);
    long daysOverdue = ChronoUnit.DAYS.between(dueDate, paymentDate);

    // Ampel-Logik
    if (daysOverdue <= 0) return PaymentBehavior.GREEN;  // Pünktlich
    if (daysOverdue <= 7) return PaymentBehavior.YELLOW; // 1-7 Tage zu spät
    if (daysOverdue <= 30) return PaymentBehavior.ORANGE; // 8-30 Tage
    return PaymentBehavior.RED; // >30 Tage zu spät
}
```

---

## 🚀 NÄCHSTE SCHRITTE

### **Für Sprint 2.1.7.4 (morgen):**
✅ Alle Informationen vorhanden - kann starten!
- XentralOrderEventHandler Interface definieren
- Mock-Implementierung
- PROSPECT → AKTIV Logik

### **Für Sprint 2.1.7.2 (in ~3-5 Tagen):**
✅ Alle Informationen vorhanden - kann starten!
- API Token validieren/neu erstellen
- Echte API-Calls implementieren
- Sales-Rep Mapping
- Zahlungsverhalten Tracking

### **API-Token Status:**
⚠️ Token validierung fehlgeschlagen (Redirect auf /login)
- Möglicherweise: Token abgelaufen
- Möglicherweise: Falsche Permissions
- Aktion: Neuen Token vor Sprint 2.1.7.2 erstellen

---

## 📊 VALIDIERUNG STATUS

| Information | Screenshot-Quelle | Verifiziert | Ready für Sprint |
|-------------|-------------------|-------------|------------------|
| Kundennummer 5-stellig | #1, #2, #3 | ✅ | 2.1.7.4 + 2.1.7.2 |
| Vertrieb-Feld | #1 | ✅ | 2.1.7.2 |
| Innendienst-Feld | #1 | ✅ | 2.1.7.2 |
| Status VERSENDET | #2 | ✅ | 2.1.7.4 |
| Zahlungsstatus | #3 | ✅ | 2.1.7.2 |
| Bezahlt-am | #3 | ✅ | 2.1.7.2 |
| Mahndatum | #3 | ✅ | 2.1.7.2 |

**✅ STATUS: 100% KOMPLETT - Alle kritischen Felder identifiziert und dokumentiert!**

---

**Letzte Aktualisierung:** 2025-10-20 23:15 CEST
**Erstellt von:** Claude + Jörg (Screenshots)
**Nächster Review:** Vor Sprint 2.1.7.2 Start (API-Token neu validieren)
