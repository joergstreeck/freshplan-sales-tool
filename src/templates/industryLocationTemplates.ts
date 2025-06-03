/**
 * Industry-specific Location Templates
 * Provides template functions for rendering industry-specific location fields with i18n support
 */

/**
 * Creates hotel-specific location fields
 */
export function createHotelLocationFields(): string {
  return `
    <h3 class="form-section-title" data-i18n="locations.hotel.categorization">Hotel-Kategorisierung</h3>
    <div class="form-row">
      <div class="form-group">
        <label data-i18n="locations.hotel.small">Klein (bis 50 Zimmer)</label>
        <input type="number" id="smallHotels" name="smallHotels" min="0" value="0" oninput="updateTotalLocations()"> 
        <span data-i18n="locations.hotel.unit">Hotels</span>
      </div>
      <div class="form-group">
        <label data-i18n="locations.hotel.medium">Mittel (51-150 Zimmer)</label>
        <input type="number" id="mediumHotels" name="mediumHotels" min="0" value="0" oninput="updateTotalLocations()"> 
        <span data-i18n="locations.hotel.unit">Hotels</span>
      </div>
    </div>
    <div class="form-row">
      <div class="form-group">
        <label data-i18n="locations.hotel.large">Groß (über 150 Zimmer)</label>
        <input type="number" id="largeHotels" name="largeHotels" min="0" value="0" oninput="updateTotalLocations()"> 
        <span data-i18n="locations.hotel.unit">Hotels</span>
      </div>
      <div class="form-group">
        <!-- Platzhalter -->
      </div>
    </div>
    <h4 style="margin-top: 1.5rem; margin-bottom: 1rem; color: var(--primary-blue);" data-i18n="locations.services">Services</h4>
    <div class="form-row">
      <div class="form-group">
        <label data-i18n="locations.hotel.breakfast">Frühstücksservice</label>
        <input type="number" id="hotelBreakfast" name="hotelBreakfast" min="0" max="999"> 
        <span><span data-i18n="locations.hotel.ofPrefix">von</span> <span id="totalHotelsCount">0</span> <span data-i18n="locations.hotel.unit">Hotels</span></span>
      </div>
      <div class="form-group">
        <label data-i18n="locations.hotel.restaurant">Restaurant à la carte</label>
        <input type="number" id="hotelRestaurant" name="hotelRestaurant" min="0" max="999"> 
        <span><span data-i18n="locations.hotel.ofPrefix">von</span> <span id="totalHotelsCount2">0</span> <span data-i18n="locations.hotel.unit">Hotels</span></span>
      </div>
    </div>
    <div class="form-row">
      <div class="form-group">
        <label data-i18n="locations.hotel.roomService">Room Service</label>
        <input type="number" id="hotelRoomService" name="hotelRoomService" min="0" max="999"> 
        <span><span data-i18n="locations.hotel.ofPrefix">von</span> <span id="totalHotelsCount3">0</span> <span data-i18n="locations.hotel.unit">Hotels</span></span>
      </div>
      <div class="form-group">
        <label data-i18n="locations.hotel.banquet">Bankett/Veranstaltungen</label>
        <input type="number" id="hotelBanquet" name="hotelBanquet" min="0" max="999"> 
        <span><span data-i18n="locations.hotel.ofPrefix">von</span> <span id="totalHotelsCount4">0</span> <span data-i18n="locations.hotel.unit">Hotels</span></span>
      </div>
    </div>
  `;
}

/**
 * Creates clinic-specific location fields
 */
export function createClinicLocationFields(): string {
  return `
    <h3 class="form-section-title" data-i18n="locations.clinic.categorization">Klinik-Kategorisierung</h3>
    <div class="form-row">
      <div class="form-group">
        <label data-i18n="locations.clinic.small">Klein (bis 150 Betten)</label>
        <input type="number" id="smallClinics" name="smallClinics" min="0" value="0" oninput="updateTotalLocations()"> 
        <span data-i18n="locations.clinic.unit">Kliniken</span>
      </div>
      <div class="form-group">
        <label data-i18n="locations.clinic.medium">Mittel (151-400 Betten)</label>
        <input type="number" id="mediumClinics" name="mediumClinics" min="0" value="0" oninput="updateTotalLocations()"> 
        <span data-i18n="locations.clinic.unit">Kliniken</span>
      </div>
    </div>
    <div class="form-row">
      <div class="form-group">
        <label data-i18n="locations.clinic.large">Groß (über 400 Betten)</label>
        <input type="number" id="largeClinics" name="largeClinics" min="0" value="0" oninput="updateTotalLocations()"> 
        <span data-i18n="locations.clinic.unit">Kliniken</span>
      </div>
      <div class="form-group">
        <label data-i18n="locations.clinic.privatePatientShare">Anteil Privatpatienten</label>
        <input type="range" id="privatePatientShare" name="privatePatientShare" min="0" max="100" value="15" class="slider" 
               oninput="document.getElementById('privatePatientDisplay').textContent = this.value + '%'">
        <span id="privatePatientDisplay">15%</span>
      </div>
    </div>
    <h4 style="margin-top: 1.5rem; margin-bottom: 1rem; color: var(--primary-blue);" data-i18n="locations.services">Services</h4>
    <div class="form-row">
      <div class="form-group">
        <label data-i18n="locations.clinic.patientCatering">Patientenverpflegung</label>
        <input type="number" id="clinicPremiumMeals" name="clinicPremiumMeals" min="0" max="999"> 
        <span><span data-i18n="locations.clinic.ofPrefix">von</span> <span id="totalClinicsCount">0</span> <span data-i18n="locations.clinic.unit">Kliniken</span></span>
      </div>
      <div class="form-group">
        <label data-i18n="locations.clinic.staffCatering">Mitarbeiterverpflegung</label>
        <input type="number" id="clinicStaffCatering" name="clinicStaffCatering" min="0" max="999"> 
        <span><span data-i18n="locations.clinic.ofPrefix">von</span> <span id="totalClinicsCount2">0</span> <span data-i18n="locations.clinic.unit">Kliniken</span></span>
      </div>
    </div>
  `;
}

/**
 * Creates senior residence-specific location fields
 */
export function createSeniorResidenceLocationFields(): string {
  return `
    <h3 class="form-section-title" data-i18n="locations.senior.categorization">Seniorenresidenz-Kategorisierung</h3>
    <div class="form-row">
      <div class="form-group">
        <label data-i18n="locations.senior.small">Klein (bis 50 Bewohner)</label>
        <input type="number" id="smallSeniorResidences" name="smallSeniorResidences" min="0" value="0" oninput="updateTotalLocations()"> 
        <span data-i18n="locations.senior.unit">Residenzen</span>
      </div>
      <div class="form-group">
        <label data-i18n="locations.senior.medium">Mittel (51-150 Bewohner)</label>
        <input type="number" id="mediumSeniorResidences" name="mediumSeniorResidences" min="0" value="0" oninput="updateTotalLocations()"> 
        <span data-i18n="locations.senior.unit">Residenzen</span>
      </div>
    </div>
    <div class="form-row">
      <div class="form-group">
        <label data-i18n="locations.senior.large">Groß (über 150 Bewohner)</label>
        <input type="number" id="largeSeniorResidences" name="largeSeniorResidences" min="0" value="0" oninput="updateTotalLocations()"> 
        <span data-i18n="locations.senior.unit">Residenzen</span>
      </div>
      <div class="form-group">
        <label data-i18n="locations.senior.careFocus">Pflegeschwerpunkt</label>
        <select id="careLevel" name="careLevel">
          <option value="mixed" data-i18n="locations.senior.careMixed">Gemischt</option>
          <option value="assisted" data-i18n="locations.senior.careAssisted">Betreutes Wohnen</option>
          <option value="nursing" data-i18n="locations.senior.careNursing">Vollpflege</option>
        </select>
      </div>
    </div>
    <h4 style="margin-top: 1.5rem; margin-bottom: 1rem; color: var(--primary-blue);" data-i18n="locations.senior.catering">Verpflegung</h4>
    <div class="form-row">
      <div class="form-group">
        <label data-i18n="locations.senior.fullCatering">Vollverpflegung</label>
        <input type="number" id="seniorFullCatering" name="seniorFullCatering" min="0" max="999"> 
        <span><span data-i18n="locations.senior.ofPrefix">von</span> <span id="totalSeniorResidencesCount">0</span> <span data-i18n="locations.senior.unit">Residenzen</span></span>
      </div>
      <div class="form-group">
        <label data-i18n="locations.senior.partialCatering">Teilverpflegung</label>
        <input type="number" id="seniorPartialCatering" name="seniorPartialCatering" min="0" max="999"> 
        <span><span data-i18n="locations.senior.ofPrefix">von</span> <span id="totalSeniorResidencesCount2">0</span> <span data-i18n="locations.senior.unit">Residenzen</span></span>
      </div>
    </div>
    <div class="form-row">
      <div class="form-group">
        <label data-i18n="locations.senior.specialDiet">Sonderkostform</label>
        <input type="number" id="seniorSpecialDiet" name="seniorSpecialDiet" min="0" max="999"> 
        <span><span data-i18n="locations.senior.ofPrefix">von</span> <span id="totalSeniorResidencesCount3">0</span> <span data-i18n="locations.senior.unit">Residenzen</span></span>
      </div>
      <div class="form-group">
        <!-- Platzhalter -->
      </div>
    </div>
  `;
}

/**
 * Creates restaurant-specific location fields
 */
export function createRestaurantLocationFields(): string {
  return `
    <h3 class="form-section-title" data-i18n="locations.restaurant.categorization">Restaurant-Kategorisierung</h3>
    <div class="form-row">
      <div class="form-group">
        <label data-i18n="locations.restaurant.small">Klein (bis 50 Sitzplätze)</label>
        <input type="number" id="smallRestaurants" name="smallRestaurants" min="0" value="0" oninput="updateTotalLocations()"> 
        <span data-i18n="locations.restaurant.unit">Restaurants</span>
      </div>
      <div class="form-group">
        <label data-i18n="locations.restaurant.medium">Mittel (51-150 Sitzplätze)</label>
        <input type="number" id="mediumRestaurants" name="mediumRestaurants" min="0" value="0" oninput="updateTotalLocations()"> 
        <span data-i18n="locations.restaurant.unit">Restaurants</span>
      </div>
    </div>
    <div class="form-row">
      <div class="form-group">
        <label data-i18n="locations.restaurant.large">Groß (über 150 Sitzplätze)</label>
        <input type="number" id="largeRestaurants" name="largeRestaurants" min="0" value="0" oninput="updateTotalLocations()"> 
        <span data-i18n="locations.restaurant.unit">Restaurants</span>
      </div>
      <div class="form-group">
        <!-- Platzhalter -->
      </div>
    </div>
    <h4 style="margin-top: 1.5rem; margin-bottom: 1rem; color: var(--primary-blue);" data-i18n="locations.services">Services</h4>
    <div class="form-row">
      <div class="form-group">
        <label data-i18n="locations.restaurant.alaCarte">À la carte</label>
        <input type="number" id="restaurantAlaCarte" name="restaurantAlaCarte" min="0" max="999"> 
        <span><span data-i18n="locations.restaurant.ofPrefix">von</span> <span id="totalRestaurantsCount">0</span> <span data-i18n="locations.restaurant.unit">Restaurants</span></span>
      </div>
      <div class="form-group">
        <label data-i18n="locations.restaurant.banquet">Bankett/Veranstaltungen</label>
        <input type="number" id="restaurantBanquet" name="restaurantBanquet" min="0" max="999"> 
        <span><span data-i18n="locations.restaurant.ofPrefix">von</span> <span id="totalRestaurantsCount2">0</span> <span data-i18n="locations.restaurant.unit">Restaurants</span></span>
      </div>
    </div>
  `;
}

/**
 * Creates cafeteria-specific location fields
 */
export function createCafeteriaLocationFields(): string {
  return `
    <h3 class="form-section-title" data-i18n="locations.cafeteria.categorization">Betriebsrestaurant-Kategorisierung</h3>
    <div class="form-row">
      <div class="form-group">
        <label data-i18n="locations.cafeteria.small">Klein (bis 200 MA)</label>
        <input type="number" id="smallCafeterias" name="smallCafeterias" min="0" value="0" oninput="updateTotalLocations()"> 
        <span data-i18n="locations.cafeteria.unit">Standorte</span>
      </div>
      <div class="form-group">
        <label data-i18n="locations.cafeteria.medium">Mittel (201-500 MA)</label>
        <input type="number" id="mediumCafeterias" name="mediumCafeterias" min="0" value="0" oninput="updateTotalLocations()"> 
        <span data-i18n="locations.cafeteria.unit">Standorte</span>
      </div>
    </div>
    <div class="form-row">
      <div class="form-group">
        <label data-i18n="locations.cafeteria.large">Groß (über 500 MA)</label>
        <input type="number" id="largeCafeterias" name="largeCafeterias" min="0" value="0" oninput="updateTotalLocations()"> 
        <span data-i18n="locations.cafeteria.unit">Standorte</span>
      </div>
      <div class="form-group">
        <!-- Platzhalter -->
      </div>
    </div>
    <h4 style="margin-top: 1.5rem; margin-bottom: 1rem; color: var(--primary-blue);" data-i18n="locations.cafeteria.serviceScope">Serviceumfang</h4>
    <div class="form-row">
      <div class="form-group">
        <label data-i18n="locations.cafeteria.breakfast">Frühstück</label>
        <input type="number" id="cafeteriaBreakfast" name="cafeteriaBreakfast" min="0" max="999"> 
        <span><span data-i18n="locations.cafeteria.ofPrefix">von</span> <span id="totalCafeteriasCount">0</span> <span data-i18n="locations.cafeteria.unit">Standorten</span></span>
      </div>
      <div class="form-group">
        <label data-i18n="locations.cafeteria.lunch">Mittagessen</label>
        <input type="number" id="cafeteriaLunch" name="cafeteriaLunch" min="0" max="999"> 
        <span><span data-i18n="locations.cafeteria.ofPrefix">von</span> <span id="totalCafeteriasCount2">0</span> <span data-i18n="locations.cafeteria.unit">Standorten</span></span>
      </div>
    </div>
    <div class="form-row">
      <div class="form-group">
        <label data-i18n="locations.cafeteria.dinner">Abendessen</label>
        <input type="number" id="cafeteriaDinner" name="cafeteriaDinner" min="0" max="999"> 
        <span><span data-i18n="locations.cafeteria.ofPrefix">von</span> <span id="totalCafeteriasCount3">0</span> <span data-i18n="locations.cafeteria.unit">Standorten</span></span>
      </div>
      <div class="form-group">
        <!-- Platzhalter -->
      </div>
    </div>
  `;
}

/**
 * Gets the appropriate template function for the given industry
 */
export function getIndustryLocationFields(industry: string): string {
  switch(industry) {
    case 'hotel':
      return createHotelLocationFields();
    case 'krankenhaus':
      return createClinicLocationFields();
    case 'seniorenresidenz':
      return createSeniorResidenceLocationFields();
    case 'restaurant':
      return createRestaurantLocationFields();
    case 'betriebsrestaurant':
      return createCafeteriaLocationFields();
    default:
      return '';
  }
}