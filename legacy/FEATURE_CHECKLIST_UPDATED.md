# FreshPlan Sales Tool - Feature Checklist (Aktualisiert nach Phase 1)

## ✅ = Implementiert in Phase 1
## ⚠️ = Teilweise implementiert
## ❌ = Noch nicht implementiert

## 1. Navigation & Tabs

### Main Tabs
- [✅] **Demonstrator Tab** - Active by default
- [✅] **Customer Tab** - Customer data capture
- [✅] **Locations Tab** - Hidden by default, shown when chain customer selected
- [⚠️] **Location Details Tab** - Tab vorhanden, Funktionalität in Phase 3
- [⚠️] **Profile Tab** - Tab vorhanden, Basis-Funktionalität
- [⚠️] **Offer Tab** - Tab vorhanden, Basis-Funktionalität
- [⚠️] **Settings Tab** - Tab vorhanden, Basis-Funktionalität

### Tab Behavior
- [✅] Tab switching functionality
- [✅] Active tab highlighting
- [✅] Hidden tabs show/hide based on conditions
- [✅] Tab persistence on page reload

## 2. Demonstrator Tab Features

### Calculator Section
- [✅] **Order Value Slider**
  - [✅] Range: €0 - €100,000
  - [✅] Default: €15,000
  - [✅] Step: €1,000
  - [✅] Live value display
  - [✅] Gradient background based on value

- [✅] **Lead Time Slider**
  - [✅] Range: 0-45 days
  - [✅] Default: 10 days
  - [✅] Step: 1 day
  - [✅] Live value display

- [✅] **Pickup Option Checkbox**
  - [✅] Toggle on/off
  - [✅] Affects discount calculation
  - [✅] Min. 5000€ requirement

### Discount Calculations
- [✅] **Base Discount** - Based on order value
- [✅] **Early Booking Discount** - Based on lead time
- [✅] **Pickup Discount** - 2% when enabled (min 5000€)
- [✅] **Total Discount** - Sum of all discounts (max 15%)
- [✅] **Discount Amount** - Euro value of discount
- [✅] **Final Price** - Order value minus discount

### Scenarios Section
- [✅] **Example Scenarios** - Hotelkette, Klinikgruppe, Restaurant
- [✅] Scenario selection/loading
- [✅] Scenario-specific discount displays

## 3. Customer Tab Features ⭐ PHASE 1 FOKUS

### Alert System
- [✅] **New Customer Alert** - Hidden by default
- [✅] Alert show/hide functionality based on Neukunde + Rechnung
- [❌] Credit check request button (Phase 2)
- [❌] Management notification button (Phase 2)

### Customer Form Sections

#### Basic Data - ALLE NEU IN PHASE 1 ✅
- [✅] **Company Name** - Required text input
- [✅] **Legal Form** dropdown
  - [✅] GmbH
  - [✅] AG
  - [✅] GbR
  - [✅] Einzelunternehmen
  - [✅] Sonstige

- [✅] **Customer Type** dropdown
  - [✅] Neukunde
  - [✅] Bestandskunde

- [✅] **Industry** dropdown
  - [✅] Hotel
  - [✅] Klinik (vorher Krankenhaus)
  - [✅] Alten-/Pflegeheim
  - [✅] Betriebsrestaurant
  - [✅] Restaurant

- [✅] **Customer Number** - Optional internal field

#### Address
- [✅] **Street** - Required
- [✅] **Postal Code** - Required, 5-digit pattern
- [✅] **City** - Required

#### Contact Person
- [✅] **Contact Name** - Required
- [✅] **Position** - Optional (NEU in Phase 1)
- [✅] **Email** - Required, email validation
- [✅] **Phone** - Required, pattern validation

#### Business Data - NEU IN PHASE 1 ✅
- [✅] **Annual Volume** - Required number field
- [✅] **Payment Method** dropdown
  - [✅] Vorkasse
  - [✅] Bar bei Lieferung
  - [✅] Rechnung

#### Additional Information - NEU IN PHASE 1 ✅
- [✅] **Notes** - Textarea
- [✅] **Custom Field 1** - Text input
- [✅] **Custom Field 2** - Text input

#### Chain Customer Fields
- [✅] **Chain Customer Selection** (Single/Chain radio buttons)
- [✅] **Number of Locations** - When chain selected
- [✅] **Avg Order per Location** - When chain selected

#### Vending Fields
- [✅] **Vending Interest** - Checkbox
- [✅] **Vending Machine Locations** - Conditional field
- [✅] **Expected Daily Transactions** - Conditional field

#### Contract Details
- [✅] **Contract Start Date** - Date picker
- [✅] **Contract Duration** - Dropdown (12/24/36 months)

### Form Actions
- [✅] **Save Customer** button
- [✅] **Clear Form** button
- [✅] Form validation
- [✅] Required field indicators (*)
- [✅] Error message display
- [✅] Autosave functionality

## 4. Bonitätsprüfung Tab (PHASE 2 - Ausstehend)
- [❌] Tab nur sichtbar bei Neukunde + Rechnung
- [❌] Automatische Datenübernahme
- [❌] Handelsregister-Nummer
- [❌] USt-IdNr. mit Validierung
- [❌] Unternehmensgröße
- [❌] Kreditlimit-Berechnung
- [❌] Prüfungsstatus
- [❌] Anfrage an Geschäftsleitung

## 5. Global Features

### Data Management
- [✅] Local storage persistence
- [✅] Autosave functionality
- [✅] Data validation

### UI/UX Features
- [✅] Loading screen
- [✅] Responsive design
- [✅] Translations (DE/EN)

### Validation
- [✅] Email format validation
- [✅] Phone number format validation
- [✅] Postal code validation (5 digits)
- [✅] Required field validation

## 📊 Feature-Vollständigkeit nach Phase 1: ~60%

### ✅ Vollständig implementiert:
- Rabattrechner (100%)
- Kundendaten-Grundformular (100%)
- Neue Felder aus Phase 1 (100%)
- Validierung und Speicherung (100%)

### ⚠️ Teilweise implementiert:
- Standorte-Verwaltung (Tab vorhanden, Details fehlen)
- Profil/Angebot/Einstellungen (Basis vorhanden)

### ❌ Noch ausstehend:
- Bonitätsprüfung (Phase 2)
- Standort-Details (Phase 3)
- Vollständige Profil-Funktionen
- PDF-Generierung
- Erweiterte Einstellungen