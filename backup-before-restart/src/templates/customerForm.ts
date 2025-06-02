/**
 * Customer Form Template
 * Generates the HTML structure for the customer data form
 */

export function getCustomerFormTemplate(): string {
  return `
    <div class="customer-container">
      <h2 data-i18n="customer.title">Kundendaten erfassen</h2>
      
      <!-- Customer Type Selection -->
      <div class="customer-type-selection">
        <label class="customer-type-card">
          <input type="radio" name="customerTypeSelection" value="single" checked>
          <div class="card-content">
            <h3 data-i18n="customer.single">Einzelstandort</h3>
            <p data-i18n="customer.singleDesc">Ein Standort</p>
          </div>
        </label>
        <label class="customer-type-card">
          <input type="radio" name="customerTypeSelection" value="chain">
          <div class="card-content">
            <h3 data-i18n="customer.chain">Kette/Gruppe</h3>
            <p data-i18n="customer.chainDesc">Mehrere Standorte</p>
          </div>
        </label>
      </div>

      <!-- Alert for new customers -->
      <div id="newCustomerAlert" class="alert alert-warning hidden">
        <strong>Hinweis:</strong> Für Neukunden mit Zahlung auf Rechnung ist eine Bonitätsprüfung erforderlich.
      </div>

      <!-- Customer Form -->
      <form id="customerForm" class="customer-form">
        <!-- Basic Information -->
        <section class="form-section">
          <h3 data-i18n="customer.basicInfo">Grunddaten</h3>
          
          <div class="form-row">
            <div class="form-group">
              <label for="companyName">
                <span data-i18n="customer.company">Firma/Organisation</span>
                <span class="required">*</span>
              </label>
              <input type="text" id="companyName" name="companyName" required>
            </div>
            
            <div class="form-group">
              <label for="legalForm">
                <span data-i18n="customer.legalForm">Rechtsform</span>
                <span class="required">*</span>
              </label>
              <select id="legalForm" name="legalForm" required>
                <option value="" data-i18n="customer.selectLegalForm">Bitte wählen</option>
                <option value="gmbh" data-i18n="customer.gmbh">GmbH</option>
                <option value="ag" data-i18n="customer.ag">AG</option>
                <option value="gbr" data-i18n="customer.gbr">GbR</option>
                <option value="einzelunternehmen" data-i18n="customer.einzelunternehmen">Einzelunternehmen</option>
                <option value="other" data-i18n="customer.other">Sonstige</option>
              </select>
            </div>
          </div>
          
          <div class="form-row">
            <div class="form-group">
              <label for="customerStatus">
                <span data-i18n="customer.customerStatus">Kundentyp</span>
                <span class="required">*</span>
              </label>
              <select id="customerStatus" name="customerStatus" required>
                <option value="" selected>Bitte wählen</option>
                <option value="neukunde" data-i18n="customer.newCustomer">Neukunde</option>
                <option value="bestandskunde" data-i18n="customer.existingCustomer">Bestandskunde</option>
              </select>
            </div>
            
            <div class="form-group">
              <label for="industry">
                <span data-i18n="customer.industry">Branche</span>
                <span class="required">*</span>
              </label>
              <select id="industry" name="industry" required>
                <option value="" data-i18n="customer.selectIndustry">Bitte wählen</option>
                <option value="hotel" data-i18n="customer.hotel">Hotel</option>
                <option value="krankenhaus" data-i18n="customer.krankenhaus">Klinik</option>
                <option value="altenheim" data-i18n="customer.altenheim">Alten-/Pflegeheim</option>
                <option value="betriebsrestaurant" data-i18n="customer.betriebsrestaurant">Betriebsrestaurant</option>
                <option value="restaurant" data-i18n="customer.restaurant">Restaurant</option>
              </select>
            </div>
          </div>
          
          <div class="form-row">
            <div class="form-group">
              <label for="customerNumber">
                <span data-i18n="customer.customerNumber">Kundennummer (intern)</span>
              </label>
              <input type="text" id="customerNumber" name="customerNumber">
            </div>
            
            <div class="form-group chain-field hidden">
              <label for="chainCustomer">
                <span data-i18n="customer.chainCustomer">Kettenkunde</span>
              </label>
              <select id="chainCustomer" name="chainCustomer">
                <option value="nein">Nein</option>
                <option value="ja">Ja</option>
              </select>
            </div>
          </div>
        </section>

        <!-- Address -->
        <section class="form-section">
          <h3 data-i18n="customer.address">Adresse</h3>
          
          <div class="form-group">
            <label for="street">
              <span data-i18n="customer.street">Straße & Hausnummer</span>
              <span class="required">*</span>
            </label>
            <input type="text" id="street" name="street" required>
          </div>
          
          <div class="form-row">
            <div class="form-group">
              <label for="postalCode">
                <span data-i18n="customer.postalCode">PLZ</span>
                <span class="required">*</span>
              </label>
              <input type="text" id="postalCode" name="postalCode" pattern="[0-9]{5}" required>
            </div>
            
            <div class="form-group">
              <label for="city">
                <span data-i18n="customer.city">Ort</span>
                <span class="required">*</span>
              </label>
              <input type="text" id="city" name="city" required>
            </div>
          </div>
        </section>

        <!-- Contact Person -->
        <section class="form-section">
          <h3 data-i18n="customer.contact">Ansprechpartner</h3>
          
          <div class="form-row">
            <div class="form-group">
              <label for="contactName">
                <span data-i18n="customer.contact">Ansprechpartner</span>
                <span class="required">*</span>
              </label>
              <input type="text" id="contactName" name="contactName" required>
            </div>
            
            <div class="form-group">
              <label for="contactPosition">
                <span data-i18n="customer.position">Position</span>
              </label>
              <input type="text" id="contactPosition" name="contactPosition">
            </div>
          </div>
          
          <div class="form-row">
            <div class="form-group">
              <label for="contactPhone">
                <span data-i18n="customer.phone">Telefon</span>
                <span class="required">*</span>
              </label>
              <input type="tel" id="contactPhone" name="contactPhone" required>
            </div>
            
            <div class="form-group">
              <label for="contactEmail">
                <span data-i18n="customer.email">E-Mail</span>
                <span class="required">*</span>
              </label>
              <input type="email" id="contactEmail" name="contactEmail" required>
            </div>
          </div>
        </section>

        <!-- Business Data -->
        <section class="form-section">
          <h3 data-i18n="customer.businessData">Geschäftsdaten</h3>
          
          <div class="form-row">
            <div class="form-group">
              <label for="annualVolume">
                <span data-i18n="customer.annualVolume">Erwartetes Jahresvolumen</span>
                <span class="required">*</span>
              </label>
              <input type="number" id="annualVolume" name="annualVolume" min="0" step="1000" required>
            </div>
            
            <div class="form-group">
              <label for="paymentMethod">
                <span data-i18n="customer.paymentMethod">Zahlungsart</span>
                <span class="required">*</span>
              </label>
              <select id="paymentMethod" name="paymentMethod" required>
                <option value="" selected>Bitte wählen</option>
                <option value="vorkasse" data-i18n="customer.prepayment">Vorkasse</option>
                <option value="bar" data-i18n="customer.cash">Bar bei Lieferung</option>
                <option value="rechnung" data-i18n="customer.invoice">Rechnung</option>
              </select>
            </div>
          </div>
        </section>

        <!-- Additional Information -->
        <section class="form-section">
          <h3 data-i18n="customer.additionalInfo">Zusatzinformationen</h3>
          
          <div class="form-group">
            <label for="notes">
              <span data-i18n="customer.notes">Notizen</span>
            </label>
            <textarea id="notes" name="notes" rows="4"></textarea>
          </div>
          
          <div class="form-row">
            <div class="form-group">
              <label for="customField1">
                <span data-i18n="customer.customField1">Freifeld 1</span>
              </label>
              <input type="text" id="customField1" name="customField1">
            </div>
            
            <div class="form-group">
              <label for="customField2">
                <span data-i18n="customer.customField2">Freifeld 2</span>
              </label>
              <input type="text" id="customField2" name="customField2">
            </div>
          </div>
        </section>

        <!-- Industry-specific fields container -->
        <section id="industrySpecificFields" class="form-section hidden">
          <!-- Dynamic industry fields will be inserted here -->
        </section>

        <!-- Chain-specific fields -->
        <section id="chainFields" class="form-section hidden">
          <h3 data-i18n="customer.chainDetails">Ketten-Details</h3>
          
          <div class="form-row">
            <div class="form-group">
              <label for="totalLocations">
                <span data-i18n="customer.numberOfLocations">Anzahl Standorte</span>
              </label>
              <input type="number" id="totalLocations" name="totalLocations" min="2" value="2">
            </div>
            
            <div class="form-group">
              <label for="avgOrderPerLocation">
                <span data-i18n="customer.avgOrderPerLocation">Ø Bestellwert pro Standort</span>
              </label>
              <input type="number" id="avgOrderPerLocation" name="avgOrderPerLocation" min="0" step="100">
            </div>
          </div>
        </section>

        <!-- Vending fields -->
        <section class="form-section">
          <h3 data-i18n="customer.vendingConcept">Vending-Konzept</h3>
          
          <div class="form-group">
            <label>
              <input type="checkbox" id="vendingInterest" name="vendingInterest">
              <span data-i18n="customer.vendingInterest">Interesse an Vending-Automaten</span>
            </label>
          </div>
          
          <div id="vendingFields" class="hidden">
            <div class="form-row">
              <div class="form-group">
                <label for="vendingLocations">
                  <span data-i18n="customer.vendingMachineLocations">Anzahl Automaten-Standorte</span>
                </label>
                <input type="number" id="vendingLocations" name="vendingLocations" min="0">
              </div>
              
              <div class="form-group">
                <label for="vendingDaily">
                  <span data-i18n="customer.expectedTransactions">Erwartete Transaktionen/Tag</span>
                </label>
                <input type="number" id="vendingDaily" name="vendingDaily" min="0">
              </div>
            </div>
          </div>
        </section>

        <!-- Contract Details -->
        <section class="form-section">
          <h3 data-i18n="customer.contractDetails">Vertragsdetails</h3>
          
          <div class="form-row">
            <div class="form-group">
              <label for="contractStart">
                <span data-i18n="customer.desiredContractStart">Gewünschter Vertragsbeginn</span>
              </label>
              <input type="date" id="contractStart" name="contractStart">
            </div>
            
            <div class="form-group">
              <label for="contractDuration">
                <span data-i18n="customer.contractDuration">Vertragslaufzeit</span>
              </label>
              <select id="contractDuration" name="contractDuration">
                <option value="12" data-i18n="customer.months12">12 Monate</option>
                <option value="24" data-i18n="customer.months24">24 Monate</option>
                <option value="36" data-i18n="customer.months36">36 Monate</option>
              </select>
            </div>
          </div>
        </section>

        <!-- Form Actions -->
        <div class="form-actions">
          <button type="button" class="btn btn-secondary" id="clearForm">
            <span data-i18n="common.reset">Zurücksetzen</span>
          </button>
          <button type="submit" class="btn btn-primary">
            <span data-i18n="customer.save">Speichern</span>
          </button>
        </div>
      </form>
    </div>
  `;
}