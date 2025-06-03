/**
 * Location Details Templates
 * Provides template functions for rendering location detail cards with i18n support
 */

// Define LocationDetail interface based on legacy-script.ts structure
export interface LocationDetail {
  id: number;
  name: string;
  street: string;
  postalCode: string;
  city: string;
  contactName: string;
  contactPhone: string;
  contactEmail: string;
  category: string;
}

/**
 * Creates a complete location detail card
 */
export function createLocationDetailCard(location: LocationDetail, index: number): string {
  return `
    <div class="location-detail-card" data-location-id="${location.id}">
      ${createLocationHeader(index, location.id)}
      ${createLocationNameFields(location)}
      ${createLocationAddressFields(location)}
      ${createLocationContactFields(location)}
    </div>
  `;
}

/**
 * Creates the header section with title and remove button
 */
export function createLocationHeader(index: number, locationId: number): string {
  return `
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem;">
      <h4 style="margin: 0;">
        <span data-i18n="locationDetails.locationTitle">Standort</span> ${index}
      </h4>
      <button type="button" class="btn btn-danger btn-sm" onclick="removeLocationDetail(${locationId})">
        <span data-i18n="common.remove">Entfernen</span>
      </button>
    </div>
  `;
}

/**
 * Creates the name and category fields
 */
export function createLocationNameFields(location: LocationDetail): string {
  return `
    <div class="form-row">
      <div class="form-group">
        <label data-i18n="locationDetails.locationName">Standortname*</label>
        <input type="text" class="location-name" value="${escapeHtml(location.name)}" data-field="name">
      </div>
      <div class="form-group">
        <label data-i18n="locationDetails.category">Kategorie</label>
        <select class="location-category" data-field="category">
          <option value="" data-i18n="common.pleaseSelect">Bitte wählen</option>
          <option value="hauptstandort" ${location.category === 'hauptstandort' ? 'selected' : ''} 
                  data-i18n="locationDetails.mainLocation">Hauptstandort</option>
          <option value="filiale" ${location.category === 'filiale' ? 'selected' : ''} 
                  data-i18n="locationDetails.branch">Filiale</option>
          <option value="aussenstelle" ${location.category === 'aussenstelle' ? 'selected' : ''} 
                  data-i18n="locationDetails.externalOffice">Außenstelle</option>
        </select>
      </div>
    </div>
  `;
}

/**
 * Creates the address fields
 */
export function createLocationAddressFields(location: LocationDetail): string {
  return `
    <div class="form-row">
      <div class="form-group" style="grid-column: span 2;">
        <label data-i18n="locationDetails.streetAndNumber">Straße und Hausnummer*</label>
        <input type="text" class="location-street" value="${escapeHtml(location.street)}" data-field="street">
      </div>
    </div>
    <div class="form-row">
      <div class="form-group">
        <label data-i18n="locationDetails.postalCode">PLZ*</label>
        <input type="text" class="location-postalcode" value="${escapeHtml(location.postalCode)}" 
               data-field="postalCode" maxlength="5">
      </div>
      <div class="form-group">
        <label data-i18n="locationDetails.city">Ort*</label>
        <input type="text" class="location-city" value="${escapeHtml(location.city)}" data-field="city">
      </div>
    </div>
  `;
}

/**
 * Creates the contact fields
 */
export function createLocationContactFields(location: LocationDetail): string {
  return `
    <div class="form-row">
      <div class="form-group">
        <label data-i18n="locationDetails.contactName">Ansprechpartner Name</label>
        <input type="text" class="location-contact-name" value="${escapeHtml(location.contactName)}" 
               data-field="contactName">
      </div>
      <div class="form-group">
        <label data-i18n="locationDetails.contactPhone">Telefon</label>
        <input type="tel" class="location-contact-phone" value="${escapeHtml(location.contactPhone)}" 
               data-field="contactPhone">
      </div>
    </div>
    <div class="form-row">
      <div class="form-group" style="grid-column: span 2;">
        <label data-i18n="locationDetails.contactEmail">E-Mail</label>
        <input type="email" class="location-contact-email" value="${escapeHtml(location.contactEmail)}" 
               data-field="contactEmail">
      </div>
    </div>
  `;
}

/**
 * Escapes HTML to prevent XSS attacks
 */
function escapeHtml(str: string): string {
  const entityMap: Record<string, string> = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#39;',
    '/': '&#x2F;',
    '`': '&#x60;',
    '=': '&#x3D;'
  };
  
  return String(str).replace(/[&<>"'`=\/]/g, (s) => entityMap[s]);
}