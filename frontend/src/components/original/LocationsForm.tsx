import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
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
  onTotalLocationsChange?: (total: number) => void;
}

export function LocationsForm({
  customerIndustry = '',
  onDetailedLocationsChange,
  onTotalLocationsChange,
}: LocationsFormProps) {
  const { t } = useTranslation('locations');
  const [formData, setFormData] = useState<LocationsFormData>({
    totalLocations: 0,
    locationsManagementType: 'zentral',
    detailedLocations: false,
    vendingInterest: false,
    vendingLocations: 0,
    vendingType: '',
    privatePatientShare: 15,
    careLevel: 'mixed',
  });

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value, type } = e.target;
    const newValue =
      type === 'checkbox'
        ? (e.target as HTMLInputElement).checked
        : type === 'number'
          ? parseInt(value) || 0
          : value;

    setFormData(prev => ({
      ...prev,
      [name]: newValue,
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
        total =
          (formData.smallHotels || 0) + (formData.mediumHotels || 0) + (formData.largeHotels || 0);
        break;
      case 'krankenhaus':
        total =
          (formData.smallClinics || 0) +
          (formData.mediumClinics || 0) +
          (formData.largeClinics || 0);
        break;
      case 'seniorenresidenz':
        total =
          (formData.smallSeniorResidences || 0) +
          (formData.mediumSeniorResidences || 0) +
          (formData.largeSeniorResidences || 0);
        break;
      case 'restaurant':
        total =
          (formData.smallRestaurants || 0) +
          (formData.mediumRestaurants || 0) +
          (formData.largeRestaurants || 0);
        break;
      case 'betriebsrestaurant':
        total =
          (formData.smallCafeterias || 0) +
          (formData.mediumCafeterias || 0) +
          (formData.largeCafeterias || 0);
        break;
    }

    return total;
  };

  useEffect(() => {
    const total = calculateTotalLocations();
    if (total !== formData.totalLocations) {
      setFormData(prev => ({ ...prev, totalLocations: total }));
      if (onTotalLocationsChange) {
        onTotalLocationsChange(total);
      }
    }
  }, [formData, onTotalLocationsChange, calculateTotalLocations]);

  const renderIndustryFields = () => {
    switch (customerIndustry) {
      case 'hotel':
        return (
          <>
            <h3 className="form-section-title">{t('hotel.title')}</h3>
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="smallHotels">{t('hotel.small')}</label>
                <input
                  type="number"
                  id="smallHotels"
                  name="smallHotels"
                  min="0"
                  value={formData.smallHotels || 0}
                  onChange={handleInputChange}
                  placeholder={t('hotel.placeholder')}
                />
              </div>
              <div className="form-group">
                <label htmlFor="mediumHotels">{t('hotel.medium')}</label>
                <input
                  type="number"
                  id="mediumHotels"
                  name="mediumHotels"
                  min="0"
                  value={formData.mediumHotels || 0}
                  onChange={handleInputChange}
                  placeholder={t('hotel.placeholder')}
                />
              </div>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="largeHotels">{t('hotel.large')}</label>
                <input
                  type="number"
                  id="largeHotels"
                  name="largeHotels"
                  min="0"
                  value={formData.largeHotels || 0}
                  onChange={handleInputChange}
                  placeholder={t('hotel.placeholder')}
                />
              </div>
              <div className="form-group"></div>
            </div>

            <h4>{t('hotel.services')}</h4>
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="hotelBreakfast">{t('hotel.breakfast')}</label>
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
                  <span>
                    {t('hotel.of')} {calculateTotalLocations()} {t('hotel.hotels')}
                  </span>
                </div>
              </div>
              <div className="form-group">
                <label htmlFor="hotelRestaurant">{t('hotel.restaurant')}</label>
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
                  <span>
                    {t('hotel.of')} {calculateTotalLocations()} {t('hotel.hotels')}
                  </span>
                </div>
              </div>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="hotelRoomService">{t('hotel.roomService')}</label>
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
                  <span>
                    {t('hotel.of')} {calculateTotalLocations()} {t('hotel.hotels')}
                  </span>
                </div>
              </div>
              <div className="form-group">
                <label htmlFor="hotelBanquet">{t('hotel.banquet')}</label>
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
                  <span>
                    {t('hotel.of')} {calculateTotalLocations()} {t('hotel.hotels')}
                  </span>
                </div>
              </div>
            </div>
          </>
        );

      case 'krankenhaus':
        return (
          <>
            <h3 className="form-section-title">{t('clinic.title')}</h3>
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="smallClinics">{t('clinic.small')}</label>
                <input
                  type="number"
                  id="smallClinics"
                  name="smallClinics"
                  min="0"
                  value={formData.smallClinics || 0}
                  onChange={handleInputChange}
                  placeholder={t('clinic.placeholder')}
                />
              </div>
              <div className="form-group">
                <label htmlFor="mediumClinics">{t('clinic.medium')}</label>
                <input
                  type="number"
                  id="mediumClinics"
                  name="mediumClinics"
                  min="0"
                  value={formData.mediumClinics || 0}
                  onChange={handleInputChange}
                  placeholder={t('clinic.placeholder')}
                />
              </div>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="largeClinics">{t('clinic.large')}</label>
                <input
                  type="number"
                  id="largeClinics"
                  name="largeClinics"
                  min="0"
                  value={formData.largeClinics || 0}
                  onChange={handleInputChange}
                  placeholder={t('clinic.placeholder')}
                />
              </div>
              <div className="form-group">
                <label htmlFor="privatePatientShare">{t('clinic.privatePatients')}</label>
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
            <h4>{t('clinic.services')}</h4>
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="clinicPremiumMeals">{t('clinic.patientCatering')}</label>
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
                  <span>
                    {t('clinic.of')} {calculateTotalLocations()} {t('clinic.clinics')}
                  </span>
                </div>
              </div>
              <div className="form-group">
                <label htmlFor="clinicStaffCatering">{t('clinic.staffCatering')}</label>
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
                  <span>
                    {t('clinic.of')} {calculateTotalLocations()} {t('clinic.clinics')}
                  </span>
                </div>
              </div>
            </div>
          </>
        );

      case 'seniorenresidenz':
        return (
          <>
            <h3 className="form-section-title">{t('seniorResidence.title')}</h3>
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="smallSeniorResidences">{t('seniorResidence.small')}</label>
                <input
                  type="number"
                  id="smallSeniorResidences"
                  name="smallSeniorResidences"
                  min="0"
                  value={formData.smallSeniorResidences || 0}
                  onChange={handleInputChange}
                  placeholder={t('seniorResidence.placeholder')}
                />
              </div>
              <div className="form-group">
                <label htmlFor="mediumSeniorResidences">{t('seniorResidence.medium')}</label>
                <input
                  type="number"
                  id="mediumSeniorResidences"
                  name="mediumSeniorResidences"
                  min="0"
                  value={formData.mediumSeniorResidences || 0}
                  onChange={handleInputChange}
                  placeholder={t('seniorResidence.placeholder')}
                />
              </div>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="largeSeniorResidences">{t('seniorResidence.large')}</label>
                <input
                  type="number"
                  id="largeSeniorResidences"
                  name="largeSeniorResidences"
                  min="0"
                  value={formData.largeSeniorResidences || 0}
                  onChange={handleInputChange}
                  placeholder={t('seniorResidence.placeholder')}
                />
              </div>
              <div className="form-group">
                <label htmlFor="careLevel">{t('seniorResidence.careLevel')}</label>
                <select
                  id="careLevel"
                  name="careLevel"
                  value={formData.careLevel}
                  onChange={handleInputChange}
                >
                  <option value="mixed">{t('seniorResidence.careLevels.mixed')}</option>
                  <option value="assisted">{t('seniorResidence.careLevels.assisted')}</option>
                  <option value="nursing">{t('seniorResidence.careLevels.nursing')}</option>
                </select>
              </div>
            </div>
            <h4>{t('seniorResidence.catering')}</h4>
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="seniorFullCatering">{t('seniorResidence.fullCatering')}</label>
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
                  <span>
                    {t('seniorResidence.of')} {calculateTotalLocations()}{' '}
                    {t('seniorResidence.residences')}
                  </span>
                </div>
              </div>
              <div className="form-group">
                <label htmlFor="seniorPartialCatering">
                  {t('seniorResidence.partialCatering')}
                </label>
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
                  <span>
                    {t('seniorResidence.of')} {calculateTotalLocations()}{' '}
                    {t('seniorResidence.residences')}
                  </span>
                </div>
              </div>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="seniorSpecialDiet">{t('seniorResidence.specialDiet')}</label>
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
                  <span>
                    {t('seniorResidence.of')} {calculateTotalLocations()}{' '}
                    {t('seniorResidence.residences')}
                  </span>
                </div>
              </div>
              <div className="form-group"></div>
            </div>
          </>
        );

      case 'restaurant':
        return (
          <>
            <h3 className="form-section-title">{t('restaurant.title')}</h3>
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="smallRestaurants">{t('restaurant.small')}</label>
                <input
                  type="number"
                  id="smallRestaurants"
                  name="smallRestaurants"
                  min="0"
                  value={formData.smallRestaurants || 0}
                  onChange={handleInputChange}
                  placeholder={t('restaurant.placeholder')}
                />
              </div>
              <div className="form-group">
                <label htmlFor="mediumRestaurants">{t('restaurant.medium')}</label>
                <input
                  type="number"
                  id="mediumRestaurants"
                  name="mediumRestaurants"
                  min="0"
                  value={formData.mediumRestaurants || 0}
                  onChange={handleInputChange}
                  placeholder={t('restaurant.placeholder')}
                />
              </div>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="largeRestaurants">{t('restaurant.large')}</label>
                <input
                  type="number"
                  id="largeRestaurants"
                  name="largeRestaurants"
                  min="0"
                  value={formData.largeRestaurants || 0}
                  onChange={handleInputChange}
                  placeholder={t('restaurant.placeholder')}
                />
              </div>
              <div className="form-group"></div>
            </div>
            <h4>{t('restaurant.services')}</h4>
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="restaurantAlaCarte">{t('restaurant.alaCarte')}</label>
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
                  <span>
                    {t('restaurant.of')} {calculateTotalLocations()} {t('restaurant.restaurants')}
                  </span>
                </div>
              </div>
              <div className="form-group">
                <label htmlFor="restaurantBanquet">{t('restaurant.banquet')}</label>
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
                  <span>
                    {t('restaurant.of')} {calculateTotalLocations()} {t('restaurant.restaurants')}
                  </span>
                </div>
              </div>
            </div>
          </>
        );

      case 'betriebsrestaurant':
        return (
          <>
            <h3 className="form-section-title">{t('cafeteria.title')}</h3>
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="smallCafeterias">{t('cafeteria.small')}</label>
                <input
                  type="number"
                  id="smallCafeterias"
                  name="smallCafeterias"
                  min="0"
                  value={formData.smallCafeterias || 0}
                  onChange={handleInputChange}
                  placeholder={t('cafeteria.placeholder')}
                />
              </div>
              <div className="form-group">
                <label htmlFor="mediumCafeterias">{t('cafeteria.medium')}</label>
                <input
                  type="number"
                  id="mediumCafeterias"
                  name="mediumCafeterias"
                  min="0"
                  value={formData.mediumCafeterias || 0}
                  onChange={handleInputChange}
                  placeholder={t('cafeteria.placeholder')}
                />
              </div>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="largeCafeterias">{t('cafeteria.large')}</label>
                <input
                  type="number"
                  id="largeCafeterias"
                  name="largeCafeterias"
                  min="0"
                  value={formData.largeCafeterias || 0}
                  onChange={handleInputChange}
                  placeholder={t('cafeteria.placeholder')}
                />
              </div>
              <div className="form-group"></div>
            </div>
            <h4>{t('cafeteria.serviceScope')}</h4>
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="cafeteriaBreakfast">{t('cafeteria.breakfast')}</label>
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
                  <span>
                    {t('cafeteria.of')} {calculateTotalLocations()} {t('cafeteria.locations')}
                  </span>
                </div>
              </div>
              <div className="form-group">
                <label htmlFor="cafeteriaLunch">{t('cafeteria.lunch')}</label>
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
                  <span>
                    {t('cafeteria.of')} {calculateTotalLocations()} {t('cafeteria.locations')}
                  </span>
                </div>
              </div>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="cafeteriaDinner">{t('cafeteria.dinner')}</label>
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
                  <span>
                    {t('cafeteria.of')} {calculateTotalLocations()} {t('cafeteria.locations')}
                  </span>
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
      <h2 className="section-title">{t('title')}</h2>

      <div className="customer-form">
        {/* Kettenübersicht */}
        <div className="form-section">
          <h3 className="form-section-title">{t('sections.overview')}</h3>
          <div className="form-row">
            <div className="form-group">
              <label htmlFor="totalLocations">{t('fields.totalLocations')}</label>
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
              <label htmlFor="locationsManagementType">{t('fields.managementType')}</label>
              <select
                id="locationsManagementType"
                name="locationsManagementType"
                value={formData.locationsManagementType}
                onChange={handleInputChange}
              >
                <option value="zentral">{t('managementTypes.central')}</option>
                <option value="dezentral">{t('managementTypes.decentral')}</option>
              </select>
            </div>
          </div>
        </div>

        {/* Industry Specific Section */}
        {customerIndustry && <div className="form-section">{renderIndustryFields()}</div>}

        {/* Detaillierte Erfassung */}
        <div className="form-section">
          <h3 className="form-section-title">{t('sections.detailed')}</h3>
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
                <span className="checkbox-text">{t('fields.detailedCapture')}</span>
              </label>
              <p className="help-text">{t('fields.detailedHelpText')}</p>
            </div>
          </div>
        </div>

        {/* Vending-Konzept */}
        <div className="form-section">
          <h3 className="form-section-title">{t('sections.vending')}</h3>
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
                <span className="checkbox-text">{t('fields.vendingInterest')}</span>
              </label>
            </div>
          </div>
          <div
            id="vendingDetails"
            className={
              formData.vendingInterest ? 'vending-details-visible' : 'vending-details-hidden'
            }
          >
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="vendingLocations">{t('fields.vendingLocations')}</label>
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
                <label htmlFor="vendingType">{t('fields.vendingType')}</label>
                <select
                  id="vendingType"
                  name="vendingType"
                  value={formData.vendingType}
                  onChange={handleInputChange}
                >
                  <option value="">{t('vendingTypes.placeholder')}</option>
                  <option value="snack">{t('vendingTypes.snack')}</option>
                  <option value="fresh">{t('vendingTypes.fresh')}</option>
                  <option value="combi">{t('vendingTypes.combi')}</option>
                </select>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
