# FreshPlan Sales Tool - Feature Checklist

This checklist covers all features found in the original `freshplan-complete.html` file to ensure feature parity in the TypeScript version.

## 1. Navigation & Tabs

### Main Tabs
- [ ] **Demonstrator Tab** - Active by default
- [ ] **Customer Tab** - Customer data capture
- [ ] **Locations Tab** - Hidden by default, shown when chain customer selected
- [ ] **Location Details Tab** - Hidden by default, shown when capture details enabled
- [ ] **Profile Tab** - Company profile
- [ ] **Offer Tab** - Offer generation
- [ ] **Settings Tab** - Application settings

### Tab Behavior
- [ ] Tab switching functionality
- [ ] Active tab highlighting
- [ ] Hidden tabs show/hide based on conditions
- [ ] Tab persistence on page reload

## 2. Demonstrator Tab Features

### Calculator Section
- [ ] **Order Value Slider**
  - [ ] Range: €1,000 - €100,000
  - [ ] Default: €15,000
  - [ ] Step: €1,000
  - [ ] Live value display
  - [ ] Gradient background based on value

- [ ] **Lead Time Slider**
  - [ ] Range: 1-30 days
  - [ ] Default: 14 days
  - [ ] Step: 1 day
  - [ ] Live value display

- [ ] **Pickup Option Checkbox**
  - [ ] Toggle on/off
  - [ ] Affects discount calculation

### Discount Calculations
- [ ] **Base Discount** - Based on order value
- [ ] **Early Booking Discount** - Based on lead time
- [ ] **Pickup Discount** - 3% when enabled
- [ ] **Total Discount** - Sum of all discounts
- [ ] **Discount Amount** - Euro value of discount
- [ ] **Final Price** - Order value minus discount

### Scenarios Section
- [ ] **Spontaneous Order** scenario card
- [ ] **Planned Order** scenario card
- [ ] **Optimal Order** scenario card
- [ ] Scenario selection/highlighting
- [ ] Scenario-specific discount displays

### Information Cards
- [ ] **Discount Scale Card** - Shows discount tiers
- [ ] **Chain Customer Info Card**
- [ ] **Combination Discount Card**
- [ ] **Terms & Conditions Card**

## 3. Customer Tab Features

### Alert System
- [ ] **New Customer Alert** - Hidden by default
- [ ] Credit check request button
- [ ] Management notification button
- [ ] Alert show/hide functionality

### Customer Form Sections

#### Basic Data
- [ ] **Company Name** - Required text input
- [ ] **Legal Form** dropdown
  - [ ] GmbH
  - [ ] AG
  - [ ] GmbH & Co. KG
  - [ ] OHG
  - [ ] KG
  - [ ] GbR
  - [ ] e.K.
  - [ ] Einzelunternehmen
  - [ ] Other options

- [ ] **Customer Type** dropdown
  - [ ] Neukunde
  - [ ] Bestandskunde

- [ ] **Industry** dropdown
  - [ ] Hotel
  - [ ] Klinik
  - [ ] Seniorenresidenz
  - [ ] Betriebsrestaurant
  - [ ] Restaurant
  - [ ] Dynamic industry-specific fields

#### Registration Data
- [ ] **Trade Register Number** - Optional
- [ ] **VAT ID** - Required, pattern validation
- [ ] **Customer Number** - Auto-generated placeholder
- [ ] **Company Size** dropdown
- [ ] **Chain Customer** dropdown (Yes/No)

#### Address
- [ ] **Street** - Required
- [ ] **Postal Code** - Required, 5-digit pattern
- [ ] **City** - Required

#### Contact Person
- [ ] **Contact Name** - Required
- [ ] **Position** - Optional
- [ ] **Email** - Required, email validation
- [ ] **Phone** - Required, pattern validation

#### Additional Information
- [ ] **Notes** - Textarea
- [ ] **Special Requirements** - Textarea

### Industry-Specific Fields
When industry is selected, show specific fields:

#### Hotel
- [ ] Small hotels count (up to 50 rooms)
- [ ] Medium hotels count (51-150 rooms)
- [ ] Large hotels count (over 150 rooms)
- [ ] Total rooms calculation
- [ ] Services checkboxes

#### Klinik
- [ ] Small clinics count (up to 150 beds)
- [ ] Medium clinics count (151-400 beds)
- [ ] Large clinics count (over 400 beds)
- [ ] Total beds calculation
- [ ] Private patient percentage

#### Seniorenresidenz
- [ ] Small residences count (up to 50 residents)
- [ ] Medium residences count (51-100 residents)
- [ ] Large residences count (over 100 residents)
- [ ] Total residents calculation
- [ ] Care services options

#### Betriebsrestaurant
- [ ] Small restaurants count (up to 200 employees)
- [ ] Medium restaurants count (201-500 employees)
- [ ] Large restaurants count (over 500 employees)
- [ ] Total employees calculation
- [ ] Meal services options

#### Restaurant
- [ ] Small restaurants count (up to 50 seats)
- [ ] Medium restaurants count (51-150 seats)
- [ ] Large restaurants count (over 150 seats)
- [ ] Total seats calculation
- [ ] Service type options

### Form Actions
- [ ] **Save Customer** button
- [ ] **Clear Form** button
- [ ] Form validation
- [ ] Required field indicators (*)
- [ ] Error message display

## 4. Locations Tab Features

### Overview Section
- [ ] Total locations display
- [ ] Industry-specific location counts
- [ ] Capture details toggle
- [ ] Synchronization warning

### Quick Entry
- [ ] Add multiple locations by category
- [ ] Size category selection
- [ ] Service matrix checkboxes

## 5. Location Details Tab Features

### Location Management
- [ ] **Add Location** button
- [ ] Location list display
- [ ] No locations message

### Individual Location Fields
- [ ] Location name
- [ ] Address fields
- [ ] Size category dropdown
- [ ] Industry-specific metrics
- [ ] Services checkboxes
- [ ] Notes textarea
- [ ] Vending interest field

### Location Actions
- [ ] Expand/collapse location details
- [ ] Remove location with confirmation
- [ ] Location synchronization check

## 6. Profile Tab Features

### Company Information
- [ ] Company details display
- [ ] Contact information
- [ ] Registration details

### Business Metrics
- [ ] Potential analysis
- [ ] Volume calculations
- [ ] Savings projections

### Strategy Section
- [ ] Business arguments list
- [ ] Value propositions
- [ ] Implementation steps

## 7. Offer Tab Features

### Offer Generation
- [ ] Offer preview
- [ ] PDF generation
- [ ] Download functionality
- [ ] Email integration

### Offer Customization
- [ ] Template selection
- [ ] Content editing
- [ ] Pricing details

## 8. Settings Tab Features

### Application Settings
- [ ] Language selection (DE/EN)
- [ ] Theme settings
- [ ] Notification preferences

### User Preferences
- [ ] Autosave toggle
- [ ] Data persistence
- [ ] Export/Import settings

## 9. Global Features

### Header
- [ ] Logo display
- [ ] Tagline
- [ ] Language switcher
- [ ] Progress bar

### Data Management
- [ ] Local storage persistence
- [ ] Autosave functionality
- [ ] Autosave indicator
- [ ] Data validation

### UI/UX Features
- [ ] Loading screen
- [ ] Smooth animations
- [ ] Responsive design
- [ ] Mobile optimization
- [ ] Print styles

### Notifications
- [ ] Success notifications
- [ ] Error notifications
- [ ] Info notifications
- [ ] Warning notifications
- [ ] Auto-dismiss functionality

### Modals
- [ ] Modal display system
- [ ] PDF preview modal
- [ ] Close functionality
- [ ] Overlay backdrop

## 10. Calculations & Business Logic

### Discount Calculations
- [ ] Order value tiers
  - [ ] 0-5k: 0%
  - [ ] 5k-10k: 3%
  - [ ] 10k-20k: 6%
  - [ ] 20k-50k: 10%
  - [ ] 50k+: 14%
- [ ] Lead time discount (0.5% per day, max 15%)
- [ ] Pickup discount (3%)
- [ ] Chain customer special rates

### Data Validation
- [ ] Email format validation
- [ ] Phone number format validation
- [ ] Postal code validation (5 digits)
- [ ] Required field validation
- [ ] Industry-specific validations

## 11. Integration Features

### PDF Generation
- [ ] Customer data PDF
- [ ] Offer PDF
- [ ] Profile PDF
- [ ] PDF preview functionality
- [ ] Download functionality

### External Libraries
- [ ] jsPDF integration
- [ ] Font support
- [ ] Image embedding

## 12. Accessibility Features

- [ ] ARIA labels
- [ ] Role attributes
- [ ] Keyboard navigation
- [ ] Focus management
- [ ] Screen reader support

## 13. Performance Features

- [ ] Lazy loading
- [ ] Debounced inputs
- [ ] Optimized re-renders
- [ ] Efficient state management

## 14. Error Handling

- [ ] Form validation errors
- [ ] Network error handling
- [ ] Storage quota errors
- [ ] Graceful fallbacks

## 15. Development Features

- [ ] Debug mode (localhost only)
- [ ] State inspection
- [ ] Event logging
- [ ] Module reloading
- [ ] Clear state functionality

---

## Testing Checklist

### Functional Testing
- [ ] All form submissions work correctly
- [ ] All calculations are accurate
- [ ] Tab switching preserves data
- [ ] Local storage saves/loads correctly
- [ ] PDF generation works on all tabs

### Cross-browser Testing
- [ ] Chrome
- [ ] Firefox
- [ ] Safari
- [ ] Edge

### Responsive Testing
- [ ] Desktop (1400px+)
- [ ] Laptop (1024px-1400px)
- [ ] Tablet (768px-1024px)
- [ ] Mobile (320px-768px)

### Performance Testing
- [ ] Page load time < 3s
- [ ] Smooth animations (60fps)
- [ ] No memory leaks
- [ ] Efficient bundle size