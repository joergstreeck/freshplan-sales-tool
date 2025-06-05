/**
 * Locations Module - Manages chain customer location functionality
 */

import Module from '../core/Module';
import { useStore } from '../store';

export default class LocationsModule extends Module {
  private locationCounter = 0;

  constructor() {
    super('locations');
  }

  /**
   * Setup module
   */
  setup(): void {
    this.setupChainCustomerListener();
    this.updateLocationsVisibility();
  }

  /**
   * Bind module events
   */
  bindEvents(): void {
    // Chain customer toggle
    this.on('#chainCustomer', 'change', this.handleChainCustomerChange.bind(this));
    
    // Capture details toggle
    this.on('#captureDetailsToggle', 'change', this.handleCaptureDetailsToggle.bind(this));
    
    // Add location button
    this.on('[data-action="add-location"]', 'click', this.addLocation.bind(this));
    
    // Location actions (using event delegation)
    this.on('[data-action="toggle-location"]', 'click', this.toggleLocation.bind(this));
    this.on('[data-action="remove-location"]', 'click', this.removeLocation.bind(this));
    
    // Industry field changes
    this.on('#industry', 'change', this.updateIndustryFields.bind(this));
  }

  /**
   * Setup chain customer listener
   */
  private setupChainCustomerListener(): void {
    this.subscribe('customer.data.chainCustomer', () => {
      this.updateLocationsVisibility();
    });
  }

  /**
   * Handle chain customer change
   */
  private handleChainCustomerChange(event: Event): void {
    const select = event.target as HTMLSelectElement;
    const isChain = select.value === 'ja';
    
    useStore.getState().updateCustomer({
      chainCustomer: isChain ? 'ja' : 'nein'
    });
    
    this.updateLocationsVisibility();
  }

  /**
   * Handle capture details toggle
   */
  private handleCaptureDetailsToggle(event: Event): void {
    const checkbox = event.target as HTMLInputElement;
    const locationDetailsTab = document.getElementById('locationDetailsTab');
    
    if (locationDetailsTab) {
      locationDetailsTab.classList.toggle('hidden', !checkbox.checked);
    }
    
    if (checkbox.checked) {
      this.checkLocationSynchronization();
    }
  }

  /**
   * Update locations visibility
   */
  private updateLocationsVisibility(): void {
    const state = useStore.getState();
    const isChain = state.customer.data?.chainCustomer === 'ja';
    const locationsTab = document.getElementById('locationsTab');
    
    if (locationsTab) {
      locationsTab.classList.toggle('hidden', !isChain);
    }
  }

  /**
   * Update industry-specific fields
   */
  private updateIndustryFields(_event?: Event): void {
    const industrySelect = document.getElementById('industry') as HTMLSelectElement;
    const selectedIndustryInput = document.getElementById('selectedIndustry') as HTMLInputElement;
    const container = document.getElementById('industrySpecificFields');
    
    if (!industrySelect || !container) return;
    
    const industry = industrySelect.value;
    if (selectedIndustryInput) {
      selectedIndustryInput.value = this.getIndustryLabel(industry);
    }
    
    container.innerHTML = this.generateIndustryFields(industry);
    this.updateTotalLocations();
  }

  /**
   * Generate industry-specific fields HTML
   */
  private generateIndustryFields(industry: string): string {
    let html = '<h3 class="form-section-title">Branchenspezifische Angaben</h3>';
    
    switch (industry) {
      case 'hotel':
        html += this.generateHotelFields();
        break;
      case 'klinik':
        html += this.generateKlinikFields();
        break;
      case 'seniorenresidenz':
        html += this.generateSeniorenresidenzFields();
        break;
      case 'betriebsrestaurant':
        html += this.generateBetriebsrestaurantFields();
        break;
      case 'restaurant':
        html += this.generateRestaurantFields();
        break;
      default:
        html += '<p>Keine branchenspezifischen Felder verfügbar.</p>';
    }
    
    return html;
  }

  /**
   * Generate hotel-specific fields
   */
  private generateHotelFields(): string {
    return `
      <div class="form-row">
        <div class="form-group">
          <label>Kleine Hotels (bis 50 Zimmer)</label>
          <input type="number" id="smallHotels" min="0" value="0" onchange="updateTotalLocations()">
        </div>
        <div class="form-group">
          <label>Mittlere Hotels (51-150 Zimmer)</label>
          <input type="number" id="mediumHotels" min="0" value="0" onchange="updateTotalLocations()">
        </div>
        <div class="form-group">
          <label>Große Hotels (über 150 Zimmer)</label>
          <input type="number" id="largeHotels" min="0" value="0" onchange="updateTotalLocations()">
        </div>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label>Hotels mit Frühstücksservice</label>
          <input type="number" id="hotelBreakfast" min="0" value="0">
        </div>
        <div class="form-group">
          <label>Hotels mit Restaurant à la carte</label>
          <input type="number" id="hotelRestaurant" min="0" value="0">
        </div>
        <div class="form-group">
          <label>Hotels mit Room Service</label>
          <input type="number" id="hotelRoomService" min="0" value="0">
        </div>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label>Hotels mit Bankett/Veranstaltungen</label>
          <input type="number" id="hotelBanquet" min="0" value="0">
        </div>
      </div>
    `;
  }

  /**
   * Generate klinik-specific fields
   */
  private generateKlinikFields(): string {
    return `
      <div class="form-row">
        <div class="form-group">
          <label>Kleine Kliniken (bis 150 Betten)</label>
          <input type="number" id="smallClinics" min="0" value="0" onchange="updateTotalLocations()">
        </div>
        <div class="form-group">
          <label>Mittlere Kliniken (151-400 Betten)</label>
          <input type="number" id="mediumClinics" min="0" value="0" onchange="updateTotalLocations()">
        </div>
        <div class="form-group">
          <label>Große Kliniken (über 400 Betten)</label>
          <input type="number" id="largeClinics" min="0" value="0" onchange="updateTotalLocations()">
        </div>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label>Durchschnittlicher Privatpatientenanteil (%)</label>
          <input type="number" id="privatePatientPercentage" min="0" max="100" value="0">
        </div>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label>Kliniken mit Patientenverpflegung</label>
          <input type="number" id="clinicPatientMeals" min="0" value="0">
        </div>
        <div class="form-group">
          <label>Kliniken mit Mitarbeiterverpflegung</label>
          <input type="number" id="clinicStaffMeals" min="0" value="0">
        </div>
      </div>
    `;
  }

  /**
   * Generate seniorenresidenz-specific fields
   */
  private generateSeniorenresidenzFields(): string {
    return `
      <div class="form-row">
        <div class="form-group">
          <label>Kleine Einrichtungen (bis 50 Bewohner)</label>
          <input type="number" id="smallSenior" min="0" value="0" onchange="updateTotalLocations()">
        </div>
        <div class="form-group">
          <label>Mittlere Einrichtungen (51-100 Bewohner)</label>
          <input type="number" id="mediumSenior" min="0" value="0" onchange="updateTotalLocations()">
        </div>
        <div class="form-group">
          <label>Große Einrichtungen (über 100 Bewohner)</label>
          <input type="number" id="largeSenior" min="0" value="0" onchange="updateTotalLocations()">
        </div>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label>Einrichtungen mit Vollverpflegung</label>
          <input type="number" id="seniorFullCatering" min="0" value="0">
        </div>
        <div class="form-group">
          <label>Einrichtungen mit Teilverpflegung</label>
          <input type="number" id="seniorPartialCatering" min="0" value="0">
        </div>
        <div class="form-group">
          <label>Einrichtungen mit Sonderkostform</label>
          <input type="number" id="seniorSpecialDiet" min="0" value="0">
        </div>
      </div>
    `;
  }

  /**
   * Generate betriebsrestaurant-specific fields
   */
  private generateBetriebsrestaurantFields(): string {
    return `
      <div class="form-row">
        <div class="form-group">
          <label>Kleine Betriebe (bis 200 MA)</label>
          <input type="number" id="smallCompany" min="0" value="0" onchange="updateTotalLocations()">
        </div>
        <div class="form-group">
          <label>Mittlere Betriebe (201-500 MA)</label>
          <input type="number" id="mediumCompany" min="0" value="0" onchange="updateTotalLocations()">
        </div>
        <div class="form-group">
          <label>Große Betriebe (über 500 MA)</label>
          <input type="number" id="largeCompany" min="0" value="0" onchange="updateTotalLocations()">
        </div>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label>Betriebe mit Frühstück</label>
          <input type="number" id="companyBreakfast" min="0" value="0">
        </div>
        <div class="form-group">
          <label>Betriebe mit Mittagessen</label>
          <input type="number" id="companyLunch" min="0" value="0">
        </div>
        <div class="form-group">
          <label>Betriebe mit Abendessen</label>
          <input type="number" id="companyDinner" min="0" value="0">
        </div>
      </div>
    `;
  }

  /**
   * Generate restaurant-specific fields
   */
  private generateRestaurantFields(): string {
    return `
      <div class="form-row">
        <div class="form-group">
          <label>Kleine Restaurants (bis 50 Plätze)</label>
          <input type="number" id="smallRestaurant" min="0" value="0" onchange="updateTotalLocations()">
        </div>
        <div class="form-group">
          <label>Mittlere Restaurants (51-150 Plätze)</label>
          <input type="number" id="mediumRestaurant" min="0" value="0" onchange="updateTotalLocations()">
        </div>
        <div class="form-group">
          <label>Große Restaurants (über 150 Plätze)</label>
          <input type="number" id="largeRestaurant" min="0" value="0" onchange="updateTotalLocations()">
        </div>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label>Restaurants mit À la carte</label>
          <input type="number" id="restaurantAlaCarte" min="0" value="0">
        </div>
        <div class="form-group">
          <label>Restaurants mit Bankett/Veranstaltungen</label>
          <input type="number" id="restaurantBanquet" min="0" value="0">
        </div>
      </div>
    `;
  }

  /**
   * Add a new location
   */
  private addLocation(): void {
    this.locationCounter++;
    const locationId = `location_${this.locationCounter}`;
    const industry = (document.getElementById('industry') as HTMLSelectElement)?.value || 'default';
    const container = document.getElementById('locationDetailsList');
    
    if (!container) return;
    
    const locationHtml = this.generateLocationHTML(locationId, industry);
    container.insertAdjacentHTML('beforeend', locationHtml);
    
    // Update no locations message
    const noLocationsMsg = document.getElementById('noLocationsMessage');
    if (noLocationsMsg) {
      noLocationsMsg.style.display = 'none';
    }
    
    this.checkLocationSynchronization();
    
    // Store location in state
    const locations = useStore.getState().locations.locations || [];
    locations.push({
      id: locationId,
      name: '',
      industry,
      address: {},
      contact: {},
      industrySpecific: {}
    });
    useStore.getState().updateLocations(locations);
  }

  /**
   * Generate location HTML
   */
  private generateLocationHTML(locationId: string, industry: string): string {
    return `
      <div class="location-detail" id="${locationId}">
        <div class="location-header" data-action="toggle-location" data-location-id="${locationId}">
          <h4>Standort ${this.locationCounter}</h4>
          <div class="location-actions">
            <button type="button" class="btn-icon" data-action="toggle-location" data-location-id="${locationId}">
              <svg class="chevron-icon" width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
                <path d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z"/>
              </svg>
            </button>
            <button type="button" class="btn-icon danger" data-action="remove-location" data-location-id="${locationId}">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
                <path d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z"/>
              </svg>
            </button>
          </div>
        </div>
        <div class="location-content">
          ${this.generateLocationFieldsHTML(locationId, industry)}
        </div>
      </div>
    `;
  }

  /**
   * Generate location fields HTML
   */
  private generateLocationFieldsHTML(locationId: string, industry: string): string {
    let html = `
      <div class="form-section">
        <h3 class="form-section-title">Adresse</h3>
        <div class="form-row">
          <div class="form-group">
            <label>Standortname*</label>
            <input type="text" id="${locationId}_name" required>
          </div>
          <div class="form-group">
            <label>Straße und Hausnummer*</label>
            <input type="text" id="${locationId}_street" required>
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>PLZ*</label>
            <input type="text" id="${locationId}_plz" pattern="[0-9]{5}" maxlength="5" required>
          </div>
          <div class="form-group">
            <label>Ort*</label>
            <input type="text" id="${locationId}_city" required>
          </div>
        </div>
      </div>
      
      <div class="form-section">
        <h3 class="form-section-title">Ansprechpartner</h3>
        <div class="form-row">
          <div class="form-group">
            <label>Name</label>
            <input type="text" id="${locationId}_contact">
          </div>
          <div class="form-group">
            <label>Telefon</label>
            <input type="tel" id="${locationId}_phone">
          </div>
        </div>
      </div>
      
      <div class="form-section">
        <h3 class="form-section-title">Branchenspezifische Angaben</h3>
        ${this.getLocationIndustryFields(industry, locationId)}
      </div>
    `;
    
    return html;
  }

  /**
   * Get location industry fields
   */
  private getLocationIndustryFields(industry: string, locationId: string): string {
    switch (industry) {
      case 'hotel':
        return this.getHotelLocationFields(locationId);
      case 'klinik':
        return this.getKlinikLocationFields(locationId);
      case 'seniorenresidenz':
        return this.getSeniorenresidenzLocationFields(locationId);
      case 'betriebsrestaurant':
        return this.getBetriebsrestaurantLocationFields(locationId);
      case 'restaurant':
        return this.getRestaurantLocationFields(locationId);
      default:
        return '<p>Keine branchenspezifischen Felder verfügbar.</p>';
    }
  }

  /**
   * Get hotel location fields
   */
  private getHotelLocationFields(locationId: string): string {
    return `
      <div class="form-row">
        <div class="form-group">
          <label>Größenkategorie</label>
          <select id="${locationId}_size">
            <option value="">Bitte wählen</option>
            <option value="small">Klein (bis 50 Zimmer)</option>
            <option value="medium">Mittel (51-150 Zimmer)</option>
            <option value="large">Groß (über 150 Zimmer)</option>
          </select>
        </div>
        <div class="form-group">
          <label>Anzahl Zimmer</label>
          <input type="number" id="${locationId}_rooms" min="1">
        </div>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label>Services</label>
          <div>
            <label class="checkbox-label">
              <input type="checkbox" id="${locationId}_breakfast">
              <span>Frühstücksservice</span>
            </label>
            <label class="checkbox-label">
              <input type="checkbox" id="${locationId}_restaurant">
              <span>Restaurant à la carte</span>
            </label>
            <label class="checkbox-label">
              <input type="checkbox" id="${locationId}_roomservice">
              <span>Room Service</span>
            </label>
            <label class="checkbox-label">
              <input type="checkbox" id="${locationId}_banquet">
              <span>Bankett/Veranstaltungen</span>
            </label>
          </div>
        </div>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label>Bemerkungen</label>
          <textarea id="${locationId}_bemerkungen" rows="3" placeholder="Wünsche und Besonderheiten"></textarea>
        </div>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label>Interesse an Freshfoodz-Vending?</label>
          <input type="text" id="${locationId}_vending" placeholder="Standortangabe">
        </div>
      </div>
    `;
  }

  /**
   * Get klinik location fields
   */
  private getKlinikLocationFields(locationId: string): string {
    return `
      <div class="form-row">
        <div class="form-group">
          <label>Größenkategorie</label>
          <select id="${locationId}_size">
            <option value="">Bitte wählen</option>
            <option value="small">Klein (bis 150 Betten)</option>
            <option value="medium">Mittel (151-400 Betten)</option>
            <option value="large">Groß (über 400 Betten)</option>
          </select>
        </div>
        <div class="form-group">
          <label>Anzahl Betten</label>
          <input type="number" id="${locationId}_beds" min="1">
        </div>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label>Privatpatientenanteil (%)</label>
          <input type="number" id="${locationId}_private" min="0" max="100">
        </div>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label>Services</label>
          <div>
            <label class="checkbox-label">
              <input type="checkbox" id="${locationId}_patientMeals">
              <span>Patientenverpflegung</span>
            </label>
            <label class="checkbox-label">
              <input type="checkbox" id="${locationId}_staffMeals">
              <span>Mitarbeiterverpflegung</span>
            </label>
          </div>
        </div>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label>Bemerkungen</label>
          <textarea id="${locationId}_bemerkungen" rows="3" placeholder="Wünsche und Besonderheiten"></textarea>
        </div>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label>Interesse an Freshfoodz-Vending?</label>
          <input type="text" id="${locationId}_vending" placeholder="Standortangabe">
        </div>
      </div>
    `;
  }

  /**
   * Get seniorenresidenz location fields
   */
  private getSeniorenresidenzLocationFields(locationId: string): string {
    return `
      <div class="form-row">
        <div class="form-group">
          <label>Größenkategorie</label>
          <select id="${locationId}_size">
            <option value="">Bitte wählen</option>
            <option value="small">Klein (bis 50 Bewohner)</option>
            <option value="medium">Mittel (51-100 Bewohner)</option>
            <option value="large">Groß (über 100 Bewohner)</option>
          </select>
        </div>
        <div class="form-group">
          <label>Anzahl Bewohner</label>
          <input type="number" id="${locationId}_residents" min="1">
        </div>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label>Verpflegung</label>
          <div>
            <label class="checkbox-label">
              <input type="checkbox" id="${locationId}_fullCatering">
              <span>Vollverpflegung</span>
            </label>
            <label class="checkbox-label">
              <input type="checkbox" id="${locationId}_partialCatering">
              <span>Teilverpflegung</span>
            </label>
            <label class="checkbox-label">
              <input type="checkbox" id="${locationId}_specialDiet">
              <span>Sonderkostform</span>
            </label>
          </div>
        </div>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label>Bemerkungen</label>
          <textarea id="${locationId}_bemerkungen" rows="3" placeholder="Wünsche und Besonderheiten"></textarea>
        </div>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label>Interesse an Freshfoodz-Vending?</label>
          <input type="text" id="${locationId}_vending" placeholder="Standortangabe">
        </div>
      </div>
    `;
  }

  /**
   * Get betriebsrestaurant location fields
   */
  private getBetriebsrestaurantLocationFields(locationId: string): string {
    return `
      <div class="form-row">
        <div class="form-group">
          <label>Größenkategorie</label>
          <select id="${locationId}_size">
            <option value="">Bitte wählen</option>
            <option value="small">Klein (bis 200 MA)</option>
            <option value="medium">Mittel (201-500 MA)</option>
            <option value="large">Groß (über 500 MA)</option>
          </select>
        </div>
        <div class="form-group">
          <label>Anzahl Mitarbeiter</label>
          <input type="number" id="${locationId}_employees" min="1">
        </div>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label>Serviceumfang</label>
          <div>
            <label class="checkbox-label">
              <input type="checkbox" id="${locationId}_breakfast">
              <span>Frühstück</span>
            </label>
            <label class="checkbox-label">
              <input type="checkbox" id="${locationId}_lunch">
              <span>Mittagessen</span>
            </label>
            <label class="checkbox-label">
              <input type="checkbox" id="${locationId}_dinner">
              <span>Abendessen</span>
            </label>
          </div>
        </div>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label>Bemerkungen</label>
          <textarea id="${locationId}_bemerkungen" rows="3" placeholder="Wünsche und Besonderheiten"></textarea>
        </div>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label>Interesse an Freshfoodz-Vending?</label>
          <input type="text" id="${locationId}_vending" placeholder="Standortangabe">
        </div>
      </div>
    `;
  }

  /**
   * Get restaurant location fields
   */
  private getRestaurantLocationFields(locationId: string): string {
    return `
      <div class="form-row">
        <div class="form-group">
          <label>Größenkategorie</label>
          <select id="${locationId}_size">
            <option value="">Bitte wählen</option>
            <option value="small">Klein (bis 50 Plätze)</option>
            <option value="medium">Mittel (51-150 Plätze)</option>
            <option value="large">Groß (über 150 Plätze)</option>
          </select>
        </div>
        <div class="form-group">
          <label>Anzahl Sitzplätze</label>
          <input type="number" id="${locationId}_seats" min="1">
        </div>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label>Services</label>
          <div>
            <label class="checkbox-label">
              <input type="checkbox" id="${locationId}_alacarte">
              <span>À la carte</span>
            </label>
            <label class="checkbox-label">
              <input type="checkbox" id="${locationId}_banquet">
              <span>Bankett/Veranstaltungen</span>
            </label>
          </div>
        </div>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label>Bemerkungen</label>
          <textarea id="${locationId}_bemerkungen" rows="3" placeholder="Wünsche und Besonderheiten"></textarea>
        </div>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label>Interesse an Freshfoodz-Vending?</label>
          <input type="text" id="${locationId}_vending" placeholder="Standortangabe">
        </div>
      </div>
    `;
  }

  /**
   * Toggle location visibility
   */
  private toggleLocation(event: Event): void {
    event.stopPropagation();
    const button = event.currentTarget as HTMLElement;
    const locationId = button.dataset.locationId;
    
    if (!locationId) return;
    
    const locationElement = document.getElementById(locationId);
    if (locationElement) {
      locationElement.classList.toggle('expanded');
    }
  }

  /**
   * Remove location
   */
  private removeLocation(event: Event): void {
    event.stopPropagation();
    const button = event.currentTarget as HTMLElement;
    const locationId = button.dataset.locationId;
    
    if (!locationId || !confirm('Möchten Sie diesen Standort wirklich löschen?')) return;
    
    const locationElement = document.getElementById(locationId);
    if (locationElement) {
      locationElement.remove();
    }
    
    // Update state
    const locations = useStore.getState().locations.locations || [];
    const filteredLocations = locations.filter(loc => loc.id !== locationId);
    useStore.getState().updateLocations(filteredLocations);
    
    // Show no locations message if empty
    const container = document.getElementById('locationDetailsList');
    if (container && container.children.length === 0) {
      const noLocationsMsg = document.getElementById('noLocationsMessage');
      if (noLocationsMsg) {
        noLocationsMsg.style.display = 'block';
      }
    }
    
    this.checkLocationSynchronization();
  }

  /**
   * Update total locations count
   */
  private updateTotalLocations(): void {
    let total = 0;
    
    // Get all number inputs in the industry specific fields
    const inputs = document.querySelectorAll('#industrySpecificFields input[type="number"]');
    inputs.forEach((input: Element) => {
      const htmlInput = input as HTMLInputElement;
      if (htmlInput.id.includes('small') || htmlInput.id.includes('medium') || htmlInput.id.includes('large')) {
        total += parseInt(htmlInput.value) || 0;
      }
    });
    
    const totalLocationsInput = document.getElementById('totalLocations') as HTMLInputElement;
    if (totalLocationsInput) {
      totalLocationsInput.value = total.toString();
    }
    
    // Check synchronization if detail capture is enabled
    const captureDetailsToggle = document.getElementById('captureDetailsToggle') as HTMLInputElement;
    if (captureDetailsToggle?.checked) {
      this.checkLocationSynchronization();
    }
  }

  /**
   * Check location synchronization
   */
  private checkLocationSynchronization(): void {
    const totalLocationsInput = document.getElementById('totalLocations') as HTMLInputElement;
    const mainTotal = parseInt(totalLocationsInput?.value) || 0;
    const detailCount = document.querySelectorAll('.location-detail').length;
    const syncWarning = document.getElementById('syncWarning');
    const syncMessage = document.getElementById('syncMessage');
    
    if (!syncWarning || !syncMessage) return;
    
    if (mainTotal === 0 && detailCount === 0) {
      syncWarning.classList.add('hidden');
      return;
    }
    
    if (detailCount > mainTotal) {
      syncMessage.textContent = `Sie haben ${detailCount} Standorte erfasst, aber nur ${mainTotal} Standorte in der Übersicht angegeben.`;
      syncWarning.classList.remove('hidden');
    } else if (detailCount < mainTotal) {
      syncMessage.textContent = `Sie haben ${mainTotal} Standorte in der Übersicht angegeben, aber erst ${detailCount} detailliert erfasst.`;
      syncWarning.classList.remove('hidden');
    } else {
      // Check category distribution
      const categoryCounts = this.getDetailedCategoryCounts();
      const mainCounts = this.getMainCategoryCounts();
      
      let mismatch = false;
      for (const size in mainCounts) {
        if (categoryCounts[size] !== mainCounts[size]) {
          mismatch = true;
          break;
        }
      }
      
      if (mismatch) {
        syncMessage.textContent = 'Die Größenkategorien der erfassten Standorte stimmen nicht mit der Übersicht überein.';
        syncWarning.classList.remove('hidden');
      } else {
        syncWarning.classList.add('hidden');
      }
    }
  }

  /**
   * Get detailed category counts
   */
  private getDetailedCategoryCounts(): Record<string, number> {
    const counts: Record<string, number> = { small: 0, medium: 0, large: 0 };
    
    document.querySelectorAll('.location-detail').forEach(location => {
      const sizeSelect = location.querySelector('select[id$="_size"]') as HTMLSelectElement;
      if (sizeSelect?.value) {
        counts[sizeSelect.value] = (counts[sizeSelect.value] || 0) + 1;
      }
    });
    
    return counts;
  }

  /**
   * Get main category counts
   */
  private getMainCategoryCounts(): Record<string, number> {
    const industry = (document.getElementById('industry') as HTMLSelectElement)?.value || '';
    const counts: Record<string, number> = { small: 0, medium: 0, large: 0 };
    
    switch (industry) {
      case 'hotel':
        counts.small = parseInt((document.getElementById('smallHotels') as HTMLInputElement)?.value) || 0;
        counts.medium = parseInt((document.getElementById('mediumHotels') as HTMLInputElement)?.value) || 0;
        counts.large = parseInt((document.getElementById('largeHotels') as HTMLInputElement)?.value) || 0;
        break;
      case 'klinik':
        counts.small = parseInt((document.getElementById('smallClinics') as HTMLInputElement)?.value) || 0;
        counts.medium = parseInt((document.getElementById('mediumClinics') as HTMLInputElement)?.value) || 0;
        counts.large = parseInt((document.getElementById('largeClinics') as HTMLInputElement)?.value) || 0;
        break;
      case 'seniorenresidenz':
        counts.small = parseInt((document.getElementById('smallSenior') as HTMLInputElement)?.value) || 0;
        counts.medium = parseInt((document.getElementById('mediumSenior') as HTMLInputElement)?.value) || 0;
        counts.large = parseInt((document.getElementById('largeSenior') as HTMLInputElement)?.value) || 0;
        break;
      case 'betriebsrestaurant':
        counts.small = parseInt((document.getElementById('smallCompany') as HTMLInputElement)?.value) || 0;
        counts.medium = parseInt((document.getElementById('mediumCompany') as HTMLInputElement)?.value) || 0;
        counts.large = parseInt((document.getElementById('largeCompany') as HTMLInputElement)?.value) || 0;
        break;
      case 'restaurant':
        counts.small = parseInt((document.getElementById('smallRestaurant') as HTMLInputElement)?.value) || 0;
        counts.medium = parseInt((document.getElementById('mediumRestaurant') as HTMLInputElement)?.value) || 0;
        counts.large = parseInt((document.getElementById('largeRestaurant') as HTMLInputElement)?.value) || 0;
        break;
    }
    
    return counts;
  }

  /**
   * Get industry label
   */
  private getIndustryLabel(value: string): string {
    const labels: Record<string, string> = {
      'hotel': 'Hotel',
      'klinik': 'Klinik',
      'seniorenresidenz': 'Seniorenresidenz',
      'betriebsrestaurant': 'Betriebsrestaurant',
      'restaurant': 'Restaurant'
    };
    return labels[value] || '';
  }

  /**
   * Subscribe to state changes
   */
  subscribeToState(): void {
    // State subscriptions are handled in setupChainCustomerListener
  }

  /**
   * Cleanup module
   */
  cleanup(): void {
    // Cleanup is handled by base class
  }
}