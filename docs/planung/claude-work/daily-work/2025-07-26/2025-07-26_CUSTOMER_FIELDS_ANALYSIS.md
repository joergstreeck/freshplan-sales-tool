# 📋 CUSTOMER MANAGEMENT - FELDANALYSE

**Datum:** 26.07.2025  
**Autor:** Claude  
**Zweck:** Vollständige Dokumentation aller Felder aus der Legacy-Kundenverwaltung  

## 🏢 1. KUNDENDATEN (CustomerForm.tsx)

### Grunddaten
- **companyName** (string) - Firmenname *
- **legalForm** (select) - Rechtsform *
  - gmbh - GmbH
  - ag - AG 
  - gbr - GbR
  - einzelunternehmen - Einzelunternehmen
  - kg - KG
  - ohg - OHG
  - ug - UG
- **customerType** (select) - Kundentyp *
  - neukunde - Neukunde
  - bestandskunde - Bestandskunde
- **industry** (select) - Branche *
  - hotel - Hotel
  - krankenhaus - Krankenhaus
  - seniorenresidenz - Seniorenresidenz
  - betriebsrestaurant - Betriebsrestaurant
  - restaurant - Restaurant
- **chainCustomer** (select) - Kettenkunde
  - ja - Ja
  - nein - Nein (Standard)
- **customerNumber** (string) - Kundennummer (intern)

### Adressdaten
- **street** (string) - Straße und Hausnummer *
- **postalCode** (string) - PLZ * (max. 5 Zeichen, deutsche PLZ-Validierung)
- **city** (string) - Ort *

### Ansprechpartner
- **contactName** (string) - Name des Ansprechpartners *
- **contactPosition** (string) - Position/Funktion
- **contactPhone** (string) - Telefonnummer * (Telefon-Validierung)
- **contactEmail** (string) - E-Mail * (E-Mail-Validierung)

### Geschäftsdaten
- **expectedVolume** (string) - Erwartetes Jahresvolumen * (formatiert mit Tausender-Trennung)
- **paymentMethod** (select) - Zahlungsart *
  - vorkasse - Vorkasse
  - barzahlung - Barzahlung
  - rechnung - Rechnung

### Zusatzinformationen
- **notes** (textarea) - Notizen
- **customField1** (string) - Zusatzfeld 1
- **customField2** (string) - Zusatzfeld 2

## 🏪 2. STANDORTE (LocationsForm.tsx)

### Allgemeine Standort-Felder
- **totalLocations** (number) - Gesamtzahl Standorte (berechnet, readonly)
- **locationsManagementType** (select) - Verwaltungsart
  - zentral - Zentrale Verwaltung (Standard)
  - dezentral - Dezentrale Verwaltung
- **detailedLocations** (boolean) - Detaillierte Standorterfassung aktivieren
- **vendingInterest** (boolean) - Interesse an Vending-Konzept
- **vendingLocations** (number) - Anzahl Vending-Standorte
- **vendingType** (select) - Vending-Typ
  - snack - Snack-Automaten
  - fresh - Frische-Automaten
  - combi - Kombi-Automaten

### 🏨 BRANCHE: HOTEL

#### Größenkategorien
- **smallHotels** (number) - Kleine Hotels (< 50 Zimmer)
- **mediumHotels** (number) - Mittlere Hotels (50-150 Zimmer)
- **largeHotels** (number) - Große Hotels (> 150 Zimmer)

#### Service-Angebote (jeweils Anzahl von Gesamt-Hotels)
- **hotelBreakfast** (number) - Hotels mit Frühstücksservice
- **hotelRestaurant** (number) - Hotels mit Restaurant
- **hotelRoomService** (number) - Hotels mit Zimmerservice
- **hotelBanquet** (number) - Hotels mit Bankett/Veranstaltungen

### 🏥 BRANCHE: KRANKENHAUS

#### Größenkategorien  
- **smallClinics** (number) - Kleine Kliniken (< 200 Betten)
- **mediumClinics** (number) - Mittlere Kliniken (200-500 Betten)
- **largeClinics** (number) - Große Kliniken (> 500 Betten)
- **privatePatientShare** (range 0-100) - Privatpatientenanteil in % (Standard: 15%)

#### Service-Bereiche (jeweils Anzahl von Gesamt-Kliniken)
- **clinicPremiumMeals** (number) - Kliniken mit Patientenverpflegung
- **clinicStaffCatering** (number) - Kliniken mit Personalgastronomie

### 🏛️ BRANCHE: SENIORENRESIDENZ

#### Größenkategorien
- **smallSeniorResidences** (number) - Kleine Residenzen (< 50 Bewohner)
- **mediumSeniorResidences** (number) - Mittlere Residenzen (50-150 Bewohner)
- **largeSeniorResidences** (number) - Große Residenzen (> 150 Bewohner)
- **careLevel** (select) - Pflegestufe
  - mixed - Gemischt (Standard)
  - assisted - Betreutes Wohnen
  - nursing - Vollpflege

#### Verpflegungsarten (jeweils Anzahl von Gesamt-Residenzen)
- **seniorFullCatering** (number) - Vollverpflegung
- **seniorPartialCatering** (number) - Teilverpflegung
- **seniorSpecialDiet** (number) - Spezielle Diätformen

### 🍽️ BRANCHE: RESTAURANT

#### Größenkategorien
- **smallRestaurants** (number) - Kleine Restaurants (< 50 Plätze)
- **mediumRestaurants** (number) - Mittlere Restaurants (50-150 Plätze)
- **largeRestaurants** (number) - Große Restaurants (> 150 Plätze)

#### Service-Arten (jeweils Anzahl von Gesamt-Restaurants)
- **restaurantAlaCarte** (number) - À la carte Service
- **restaurantBanquet** (number) - Bankett/Veranstaltungen

### 🍴 BRANCHE: BETRIEBSRESTAURANT

#### Größenkategorien
- **smallCafeterias** (number) - Kleine Kantinen (< 100 Essen/Tag)
- **mediumCafeterias** (number) - Mittlere Kantinen (100-500 Essen/Tag)
- **largeCafeterias** (number) - Große Kantinen (> 500 Essen/Tag)

#### Service-Zeiten (jeweils Anzahl von Gesamt-Standorten)
- **cafeteriaBreakfast** (number) - Frühstücksangebot
- **cafeteriaLunch** (number) - Mittagessen
- **cafeteriaDinner** (number) - Abendessen

## 📍 3. DETAILLIERTE STANDORTE (DetailedLocationsForm.tsx)

Für jeden einzelnen Standort erfassbar:

### Standort-Identifikation
- **id** (number) - Interne ID (automatisch)
- **name** (string) - Standortname *
- **category** (select) - Standortkategorie
  - hauptstandort - Hauptstandort
  - filiale - Filiale
  - aussenstelle - Außenstelle

### Standort-Adresse
- **street** (string) - Straße und Hausnummer *
- **postalCode** (string) - PLZ * (max. 5 Zeichen)
- **city** (string) - Ort *

### Standort-Ansprechpartner
- **contactName** (string) - Name des Ansprechpartners
- **contactPhone** (string) - Telefonnummer
- **contactEmail** (string) - E-Mail

## 📊 ZUSAMMENFASSUNG

### Feldstatistik
- **Kundendaten:** 18 Felder (11 Pflichtfelder)
- **Standorte Allgemein:** 6 Felder
- **Branchenspezifisch:** 
  - Hotel: 8 Felder
  - Krankenhaus: 6 Felder
  - Seniorenresidenz: 8 Felder
  - Restaurant: 5 Felder
  - Betriebsrestaurant: 6 Felder
- **Detaillierte Standorte:** 8 Felder pro Standort

### Validierungen
1. **E-Mail:** Standard E-Mail-Format Validierung
2. **Telefon:** Deutsche Telefonnummer-Validierung
3. **PLZ:** Deutsche Postleitzahl (5 Zeichen)
4. **Zahlen:** Min 0, bei Service-Angeboten max = Gesamtanzahl Standorte

### Besonderheiten
1. **Dynamische Berechnung:** totalLocations wird automatisch aus den Größenkategorien berechnet
2. **Bedingte Anzeige:** vendingDetails nur bei vendingInterest = true
3. **Branchenabhängig:** Standort-Felder ändern sich je nach gewählter Branche
4. **Synchronisierungs-Warnung:** Bei Abweichung zwischen totalLocations und erfassten Details

### Pflichtfeld-Hierarchie
1. **Immer Pflicht:** Firmenname, Rechtsform, Kundentyp, Branche, Adresse, Ansprechpartner
2. **Bedingt Pflicht:** Bei detailedLocations = true werden Standortname und Adresse Pflicht
3. **Optional:** Alle branchenspezifischen Service-Felder, Vending-Konzept, Zusatzfelder

---

**NÄCHSTER SCHRITT:** Diese Feldstruktur als Basis für neue Wizard/Workflow-basierte UI verwenden