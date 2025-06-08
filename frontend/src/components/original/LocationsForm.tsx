import { useState, useEffect } from 'react';
import '../../styles/legacy/forms.css';
import '../../styles/legacy/locations.css';

interface LocationsFormData {
  totalLocations: number;
  locationsManagementType: string;
  detailedLocations: boolean;
  vendingInterest: boolean;
  vendingLocations: number;
  vendingType: string;
  
  // Industry specific counts
  smallHotels?: number;
  mediumHotels?: number;
  largeHotels?: number;
  hotelBreakfast?: number;
  hotelRestaurant?: number;
  hotelRoomService?: number;
  hotelBanquet?: number;
  
  smallClinics?: number;
  mediumClinics?: number;
  largeClinics?: number;
  privatePatientShare?: number;
  clinicPremiumMeals?: number;
  clinicStaffCatering?: number;
  
  smallSeniorResidences?: number;
  mediumSeniorResidences?: number;
  largeSeniorResidences?: number;
  careLevel?: string;
  seniorFullCatering?: number;
  seniorPartialCatering?: number;
  seniorSpecialDiet?: number;
  
  smallRestaurants?: number;
  mediumRestaurants?: number;
  largeRestaurants?: number;
  restaurantAlaCarte?: number;
  restaurantBanquet?: number;
  
  smallCafeterias?: number;
  mediumCafeterias?: number;
  largeCafeterias?: number;
  cafeteriaBreakfast?: number;
  cafeteriaLunch?: number;
  cafeteriaDinner?: number;
}

interface LocationsFormProps {
  customerIndustry?: string;
  onDetailedLocationsChange?: (enabled: boolean) => void;
}

export function LocationsForm({ customerIndustry = '', onDetailedLocationsChange }: LocationsFormProps) {
  const [formData, setFormData] = useState<LocationsFormData>({
    totalLocations: 0,
    locationsManagementType: 'zentral',
    detailedLocations: false,
    vendingInterest: false,
    vendingLocations: 0,
    vendingType: '',
    privatePatientShare: 15,
    careLevel: 'mixed'
  });

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value, type } = e.target;
    const newValue = type === 'checkbox' ? (e.target as HTMLInputElement).checked : 
                     type === 'number' ? parseInt(value) || 0 : value;
    
    setFormData(prev => ({
      ...prev,
      [name]: newValue
    }));

    // Handle special cases
    if (name === 'detailedLocations' && onDetailedLocationsChange) {
      onDetailedLocationsChange(newValue as boolean);
    }
    
    if (name === 'vendingInterest') {
      const vendingDetails = document.getElementById('vendingDetails');
      if (vendingDetails) {
        vendingDetails.style.display = newValue ? 'block' : 'none';
      }
    }
  };

  // Calculate total locations based on industry
  const calculateTotalLocations = () => {
    let total = 0;
    
    switch (customerIndustry) {
      case 'hotel':
        total = (formData.smallHotels || 0) + (formData.mediumHotels || 0) + (formData.largeHotels || 0);
        break;
      case 'krankenhaus':
        total = (formData.smallClinics || 0) + (formData.mediumClinics || 0) + (formData.largeClinics || 0);
        break;
      case 'altenheim':
        total = (formData.smallSeniorResidences || 0) + (formData.mediumSeniorResidences || 0) + (formData.largeSeniorResidences || 0);
        break;
      case 'restaurant':
        total = (formData.smallRestaurants || 0) + (formData.mediumRestaurants || 0) + (formData.largeRestaurants || 0);
        break;
      case 'betriebsrestaurant':
        total = (formData.smallCafeterias || 0) + (formData.mediumCafeterias || 0) + (formData.largeCafeterias || 0);
        break;
    }
    
    return total;
  };

  useEffect(() => {
    const total = calculateTotalLocations();
    if (total !== formData.totalLocations) {
      setFormData(prev => ({ ...prev, totalLocations: total }));
    }
  }, [formData]);

  const renderIndustryFields = () => {
    switch (customerIndustry) {
      case 'hotel':
        return (
          <>
            <h3 className="form-section-title">Hotel-Kategorisierung</h3>
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="smallHotels">Klein (bis 50 Zimmer)</label>
                <input 
                  type="number" 
                  id="smallHotels"
                  name="smallHotels" 
                  min="0" 
                  value={formData.smallHotels || 0} 
                  onChange={handleInputChange} 
                  placeholder="Anzahl Hotels"
                />
              </div>
              <div className="form-group">
                <label htmlFor="mediumHotels">Mittel (51-150 Zimmer)</label>
                <input 
                  type="number" 
                  id="mediumHotels"
                  name="mediumHotels" 
                  min="0" 
                  value={formData.mediumHotels || 0} 
                  onChange={handleInputChange}
                  placeholder="Anzahl Hotels"
                />
              </div>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="largeHotels">Groß (über 150 Zimmer)</label>
                <input 
                  type="number" 
                  id="largeHotels"
                  name="largeHotels" 
                  min="0" 
                  value={formData.largeHotels || 0} 
                  onChange={handleInputChange}
                  placeholder="Anzahl Hotels"
                />
              </div>
              <div className="form-group"></div>
            </div>
            
            <h4>Services</h4>
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="hotelBreakfast">Frühstücksservice</label>
                <div className="location-service-group">
                  <input 
                    type="number" 
                    id="hotelBreakfast"
                    name="hotelBreakfast" 
                    min="0" 
                    max={calculateTotalLocations()} 
                    value={formData.hotelBreakfast || 0} 
                    onChange={handleInputChange} 
                  />
                  <span>von {calculateTotalLocations()} Hotels</span>
                </div>
              </div>
              <div className="form-group">
                <label htmlFor="hotelRestaurant">Restaurant à la carte</label>
                <div className="location-service-group">
                  <input 
                    type="number" 
                    id="hotelRestaurant"
                    name="hotelRestaurant" 
                    min="0" 
                    max={calculateTotalLocations()} 
                    value={formData.hotelRestaurant || 0} 
                    onChange={handleInputChange} 
                  />
                  <span>von {calculateTotalLocations()} Hotels</span>
                </div>
              </div>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="hotelRoomService">Room Service</label>
                <div className="location-service-group">
                  <input 
                    type="number" 
                    id="hotelRoomService"
                    name="hotelRoomService" 
                    min="0" 
                    max={calculateTotalLocations()} 
                    value={formData.hotelRoomService || 0} 
                    onChange={handleInputChange} 
                  />
                  <span>von {calculateTotalLocations()} Hotels</span>
                </div>
              </div>
              <div className="form-group">
                <label htmlFor="hotelBanquet">Bankett/Veranstaltungen</label>
                <div className="location-service-group">
                  <input 
                    type="number" 
                    id="hotelBanquet"
                    name="hotelBanquet" 
                    min="0" 
                    max={calculateTotalLocations()} 
                    value={formData.hotelBanquet || 0} 
                    onChange={handleInputChange} 
                  />
                  <span>von {calculateTotalLocations()} Hotels</span>
                </div>
              </div>
            </div>
          </>
        );
        
      case 'krankenhaus':
        return (
          <>
            <h3 className="form-section-title">Klinik-Kategorisierung</h3>
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="smallClinics">Klein (bis 150 Betten)</label>
                <input 
                  type="number" 
                  id="smallClinics"
                  name="smallClinics" 
                  min="0" 
                  value={formData.smallClinics || 0} 
                  onChange={handleInputChange}
                  placeholder="Anzahl Kliniken"
                />
              </div>
              <div className="form-group">
                <label htmlFor="mediumClinics">Mittel (151-400 Betten)</label>
                <input 
                  type="number" 
                  id="mediumClinics"
                  name="mediumClinics" 
                  min="0" 
                  value={formData.mediumClinics || 0} 
                  onChange={handleInputChange}
                  placeholder="Anzahl Kliniken"
                />
              </div>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="largeClinics">Groß (über 400 Betten)</label>
                <input 
                  type="number" 
                  id="largeClinics"
                  name="largeClinics" 
                  min="0" 
                  value={formData.largeClinics || 0} 
                  onChange={handleInputChange}
                  placeholder="Anzahl Kliniken"
                />
              </div>
              <div className="form-group">
                <label htmlFor="privatePatientShare">Anteil Privatpatienten</label>
                <div className="slider-container">
                  <input 
                    type="range" 
                    id="privatePatientShare"
                    name="privatePatientShare" 
                    min="0" 
                    max="100" 
                    value={formData.privatePatientShare || 15}
                    onChange={handleInputChange}
                    className="slider-input"
                  />
                  <span className="slider-value">{formData.privatePatientShare || 15}%</span>
                </div>
              </div>
            </div>
            <h4>Services</h4>
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="clinicPremiumMeals">Patientenverpflegung</label>
                <div className="location-service-group">
                  <input 
                    type="number" 
                    id="clinicPremiumMeals"
                    name="clinicPremiumMeals" 
                    min="0" 
                    max={calculateTotalLocations()} 
                    value={formData.clinicPremiumMeals || 0} 
                    onChange={handleInputChange} 
                  />
                  <span>von {calculateTotalLocations()} Kliniken</span>
                </div>
              </div>
              <div className="form-group">
                <label htmlFor="clinicStaffCatering">Mitarbeiterverpflegung</label>
                <div className="location-service-group">
                  <input 
                    type="number" 
                    id="clinicStaffCatering"
                    name="clinicStaffCatering" 
                    min="0" 
                    max={calculateTotalLocations()} 
                    value={formData.clinicStaffCatering || 0} 
                    onChange={handleInputChange} 
                  />
                  <span>von {calculateTotalLocations()} Kliniken</span>
                </div>
              </div>
            </div>
          </>
        );
        
      case 'altenheim':
        return (
          <>
            <h3 className="form-section-title">Seniorenresidenz-Kategorisierung</h3>
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="smallSeniorResidences">Klein (bis 50 Bewohner)</label>
                <input 
                  type="number" 
                  id="smallSeniorResidences"
                  name="smallSeniorResidences" 
                  min="0" 
                  value={formData.smallSeniorResidences || 0} 
                  onChange={handleInputChange}
                  placeholder="Anzahl Residenzen"
                />
              </div>
              <div className="form-group">
                <label htmlFor="mediumSeniorResidences">Mittel (51-150 Bewohner)</label>
                <input 
                  type="number" 
                  id="mediumSeniorResidences"
                  name="mediumSeniorResidences" 
                  min="0" 
                  value={formData.mediumSeniorResidences || 0} 
                  onChange={handleInputChange}
                  placeholder="Anzahl Residenzen"
                />
              </div>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="largeSeniorResidences">Groß (über 150 Bewohner)</label>
                <input 
                  type="number" 
                  id="largeSeniorResidences"
                  name="largeSeniorResidences" 
                  min="0" 
                  value={formData.largeSeniorResidences || 0} 
                  onChange={handleInputChange}
                  placeholder="Anzahl Residenzen"
                />
              </div>
              <div className="form-group">
                <label htmlFor="careLevel">Pflegeschwerpunkt</label>
                <select 
                  id="careLevel"
                  name="careLevel" 
                  value={formData.careLevel} 
                  onChange={handleInputChange}
                >
                  <option value="mixed">Gemischt</option>
                  <option value="assisted">Betreutes Wohnen</option>
                  <option value="nursing">Vollpflege</option>
                </select>
              </div>
            </div>
            <h4>Verpflegung</h4>
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="seniorFullCatering">Vollverpflegung</label>
                <div className="location-service-group">
                  <input 
                    type="number" 
                    id="seniorFullCatering"
                    name="seniorFullCatering" 
                    min="0" 
                    max={calculateTotalLocations()} 
                    value={formData.seniorFullCatering || 0} 
                    onChange={handleInputChange} 
                  />
                  <span>von {calculateTotalLocations()} Residenzen</span>
                </div>
              </div>
              <div className="form-group">
                <label htmlFor="seniorPartialCatering">Teilverpflegung</label>
                <div className="location-service-group">
                  <input 
                    type="number" 
                    id="seniorPartialCatering"
                    name="seniorPartialCatering" 
                    min="0" 
                    max={calculateTotalLocations()} 
                    value={formData.seniorPartialCatering || 0} 
                    onChange={handleInputChange} 
                  />
                  <span>von {calculateTotalLocations()} Residenzen</span>
                </div>
              </div>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="seniorSpecialDiet">Sonderkostform</label>
                <div className="location-service-group">
                  <input 
                    type="number" 
                    id="seniorSpecialDiet"
                    name="seniorSpecialDiet" 
                    min="0" 
                    max={calculateTotalLocations()} 
                    value={formData.seniorSpecialDiet || 0} 
                    onChange={handleInputChange} 
                  />
                  <span>von {calculateTotalLocations()} Residenzen</span>
                </div>
              </div>
              <div className="form-group"></div>
            </div>
          </>
        );
        
      case 'restaurant':
        return (
          <>
            <h3 className="form-section-title">Restaurant-Kategorisierung</h3>
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="smallRestaurants">Klein (bis 50 Sitzplätze)</label>
                <input 
                  type="number" 
                  id="smallRestaurants"
                  name="smallRestaurants" 
                  min="0" 
                  value={formData.smallRestaurants || 0} 
                  onChange={handleInputChange}
                  placeholder="Anzahl Restaurants"
                />
              </div>
              <div className="form-group">
                <label htmlFor="mediumRestaurants">Mittel (51-150 Sitzplätze)</label>
                <input 
                  type="number" 
                  id="mediumRestaurants"
                  name="mediumRestaurants" 
                  min="0" 
                  value={formData.mediumRestaurants || 0} 
                  onChange={handleInputChange}
                  placeholder="Anzahl Restaurants"
                />
              </div>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="largeRestaurants">Groß (über 150 Sitzplätze)</label>
                <input 
                  type="number" 
                  id="largeRestaurants"
                  name="largeRestaurants" 
                  min="0" 
                  value={formData.largeRestaurants || 0} 
                  onChange={handleInputChange}
                  placeholder="Anzahl Restaurants"
                />
              </div>
              <div className="form-group"></div>
            </div>
            <h4>Services</h4>
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="restaurantAlaCarte">À la carte</label>
                <div className="location-service-group">
                  <input 
                    type="number" 
                    id="restaurantAlaCarte"
                    name="restaurantAlaCarte" 
                    min="0" 
                    max={calculateTotalLocations()} 
                    value={formData.restaurantAlaCarte || 0} 
                    onChange={handleInputChange} 
                  />
                  <span>von {calculateTotalLocations()} Restaurants</span>
                </div>
              </div>
              <div className="form-group">
                <label htmlFor="restaurantBanquet">Bankett/Veranstaltungen</label>
                <div className="location-service-group">
                  <input 
                    type="number" 
                    id="restaurantBanquet"
                    name="restaurantBanquet" 
                    min="0" 
                    max={calculateTotalLocations()} 
                    value={formData.restaurantBanquet || 0} 
                    onChange={handleInputChange} 
                  />
                  <span>von {calculateTotalLocations()} Restaurants</span>
                </div>
              </div>
            </div>
          </>
        );
        
      case 'betriebsrestaurant':
        return (
          <>
            <h3 className="form-section-title">Betriebsrestaurant-Kategorisierung</h3>
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="smallCafeterias">Klein (bis 200 MA)</label>
                <input 
                  type="number" 
                  id="smallCafeterias"
                  name="smallCafeterias" 
                  min="0" 
                  value={formData.smallCafeterias || 0} 
                  onChange={handleInputChange}
                  placeholder="Anzahl Standorte"
                />
              </div>
              <div className="form-group">
                <label htmlFor="mediumCafeterias">Mittel (201-500 MA)</label>
                <input 
                  type="number" 
                  id="mediumCafeterias"
                  name="mediumCafeterias" 
                  min="0" 
                  value={formData.mediumCafeterias || 0} 
                  onChange={handleInputChange}
                  placeholder="Anzahl Standorte"
                />
              </div>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="largeCafeterias">Groß (über 500 MA)</label>
                <input 
                  type="number" 
                  id="largeCafeterias"
                  name="largeCafeterias" 
                  min="0" 
                  value={formData.largeCafeterias || 0} 
                  onChange={handleInputChange}
                  placeholder="Anzahl Standorte"
                />
              </div>
              <div className="form-group"></div>
            </div>
            <h4>Serviceumfang</h4>
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="cafeteriaBreakfast">Frühstück</label>
                <div className="location-service-group">
                  <input 
                    type="number" 
                    id="cafeteriaBreakfast"
                    name="cafeteriaBreakfast" 
                    min="0" 
                    max={calculateTotalLocations()} 
                    value={formData.cafeteriaBreakfast || 0} 
                    onChange={handleInputChange} 
                  />
                  <span>von {calculateTotalLocations()} Standorten</span>
                </div>
              </div>
              <div className="form-group">
                <label htmlFor="cafeteriaLunch">Mittagessen</label>
                <div className="location-service-group">
                  <input 
                    type="number" 
                    id="cafeteriaLunch"
                    name="cafeteriaLunch" 
                    min="0" 
                    max={calculateTotalLocations()} 
                    value={formData.cafeteriaLunch || 0} 
                    onChange={handleInputChange} 
                  />
                  <span>von {calculateTotalLocations()} Standorten</span>
                </div>
              </div>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="cafeteriaDinner">Abendessen</label>
                <div className="location-service-group">
                  <input 
                    type="number" 
                    id="cafeteriaDinner"
                    name="cafeteriaDinner" 
                    min="0" 
                    max={calculateTotalLocations()} 
                    value={formData.cafeteriaDinner || 0} 
                    onChange={handleInputChange} 
                  />
                  <span>von {calculateTotalLocations()} Standorten</span>
                </div>
              </div>
              <div className="form-group"></div>
            </div>
          </>
        );
        
      default:
        return null;
    }
  };

  return (
    <div className="customer-container">
      <h2 className="section-title">Standortverwaltung</h2>
      
      <div className="customer-form">
        {/* Kettenübersicht */}
        <div className="form-section">
          <h3 className="form-section-title">Kettenübersicht</h3>
          <div className="form-row">
            <div className="form-group">
              <label htmlFor="totalLocations">Gesamtanzahl Standorte</label>
              <input 
                type="number" 
                id="totalLocations" 
                name="totalLocations" 
                min="0" 
                value={formData.totalLocations}
                readOnly
                className="readonly-input"
              />
            </div>
            <div className="form-group">
              <label htmlFor="locationsManagementType">Verwaltungstyp</label>
              <select 
                id="locationsManagementType" 
                name="locationsManagementType"
                value={formData.locationsManagementType}
                onChange={handleInputChange}
              >
                <option value="zentral">Zentrale Verwaltung</option>
                <option value="dezentral">Dezentrale Verwaltung</option>
              </select>
            </div>
          </div>
        </div>

        {/* Industry Specific Section */}
        {customerIndustry && (
          <div className="form-section">
            {renderIndustryFields()}
          </div>
        )}

        {/* Detaillierte Erfassung */}
        <div className="form-section">
          <h3 className="form-section-title">Detaillierte Erfassung</h3>
          <div className="form-row">
            <div className="form-group form-group-full">
              <label className="checkbox-label">
                <input 
                  type="checkbox" 
                  id="detailedLocations" 
                  name="detailedLocations"
                  checked={formData.detailedLocations}
                  onChange={handleInputChange}
                  className="checkbox-input"
                />
                <span className="checkbox-text">Standorte detailliert erfassen</span>
              </label>
              <p className="help-text">
                Erfassen Sie jeden Standort einzeln mit Adresse, Ansprechpartner und spezifischen Eigenschaften.
              </p>
            </div>
          </div>
        </div>

        {/* Vending-Konzept */}
        <div className="form-section">
          <h3 className="form-section-title">Vending-Konzept</h3>
          <div className="form-row">
            <div className="form-group">
              <label className="checkbox-label">
                <input 
                  type="checkbox" 
                  id="vendingInterest" 
                  name="vendingInterest"
                  checked={formData.vendingInterest}
                  onChange={handleInputChange}
                  className="checkbox-input"
                />
                <span className="checkbox-text">Interesse an Freshfoodz-Vending?</span>
              </label>
            </div>
          </div>
          <div id="vendingDetails" className={formData.vendingInterest ? 'vending-details-visible' : 'vending-details-hidden'}>
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="vendingLocations">Anzahl Standorte mit Vending</label>
                <input 
                  type="number" 
                  id="vendingLocations" 
                  name="vendingLocations" 
                  min="0" 
                  value={formData.vendingLocations}
                  onChange={handleInputChange}
                />
              </div>
              <div className="form-group">
                <label htmlFor="vendingType">Automatentyp</label>
                <select 
                  id="vendingType" 
                  name="vendingType"
                  value={formData.vendingType}
                  onChange={handleInputChange}
                >
                  <option value="">Bitte wählen</option>
                  <option value="snack">Snack-Automat</option>
                  <option value="fresh">Frische-Automat</option>
                  <option value="combi">Kombi-Automat</option>
                </select>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}