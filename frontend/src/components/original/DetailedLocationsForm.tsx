import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import '../../styles/legacy/forms.css';
import '../../styles/legacy/locations.css';

interface LocationDetail {
  id: number;
  name: string;
  category: string;
  street: string;
  postalCode: string;
  city: string;
  contactName: string;
  contactPhone: string;
  contactEmail: string;
}

interface DetailedLocationsFormProps {
  totalLocations?: number;
}

export function DetailedLocationsForm({ totalLocations = 0 }: DetailedLocationsFormProps) {
  const { t } = useTranslation('locationDetails');
  const [locations, setLocations] = useState<LocationDetail[]>([]);
  const [nextId, setNextId] = useState(1);

  // Check for sync warning
  const showSyncWarning = totalLocations > 0 && locations.length !== totalLocations;

  const addLocation = () => {
    const newLocation: LocationDetail = {
      id: nextId,
      name: '',
      category: '',
      street: '',
      postalCode: '',
      city: '',
      contactName: '',
      contactPhone: '',
      contactEmail: '',
    };
    setLocations([...locations, newLocation]);
    setNextId(nextId + 1);
  };

  const removeLocation = (id: number) => {
    if (window.confirm(t('confirmRemove'))) {
      setLocations(locations.filter(loc => loc.id !== id));
    }
  };

  const updateLocation = (id: number, field: keyof LocationDetail, value: string) => {
    setLocations(locations.map(loc => (loc.id === id ? { ...loc, [field]: value } : loc)));
  };

  return (
    <div className="customer-container">
      <h2 className="section-title">{t('title')}</h2>

      <div className="customer-form">
        {/* Summary */}
        <div className="form-section">
          <div className="form-row">
            <div className="form-group">
              <p>
                {t('capturedLocations')} <strong>{locations.length}</strong>
              </p>
              <p>
                {t('totalLocations')} <strong>{totalLocations}</strong>
              </p>
            </div>
            <div className="form-group">
              <button type="button" className="btn btn-primary" onClick={addLocation}>
                {t('addLocation')}
              </button>
            </div>
          </div>
        </div>

        {/* Sync Warning */}
        {showSyncWarning && (
          <div className="alert-box show">
            <div className="alert-content">
              <h3>{t('syncWarning.title')}</h3>
              <p>{t('syncWarning.text')}</p>
              <div>
                {t('syncWarning.details', {
                  captured: locations.length,
                  total: totalLocations,
                })}
              </div>
            </div>
          </div>
        )}

        {/* No Locations Message */}
        {locations.length === 0 && (
          <div className="form-section" style={{ textAlign: 'center', padding: '2rem' }}>
            <p>{t('noLocationsYet')}</p>
            <p>{t('clickAddLocation')}</p>
          </div>
        )}

        {/* Location Details List */}
        <div id="locationDetailsList">
          {locations.map((location, index) => (
            <div key={location.id} className="location-detail-card" data-location-id={location.id}>
              {/* Header with title and remove button */}
              <div
                style={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  marginBottom: '1rem',
                }}
              >
                <h4 style={{ margin: 0 }}>
                  {t('location')} {index + 1}
                </h4>
                <button
                  type="button"
                  className="btn btn-danger btn-sm"
                  onClick={() => removeLocation(location.id)}
                >
                  {t('remove')}
                </button>
              </div>

              {/* Location name and category */}
              <div className="form-row">
                <div className="form-group">
                  <label>{t('locationName')}*</label>
                  <input
                    type="text"
                    className="location-name"
                    value={location.name}
                    onChange={e => updateLocation(location.id, 'name', e.target.value)}
                  />
                </div>
                <div className="form-group">
                  <label>{t('category')}</label>
                  <select
                    className="location-category"
                    value={location.category}
                    onChange={e => updateLocation(location.id, 'category', e.target.value)}
                  >
                    <option value="">{t('pleaseSelect')}</option>
                    <option value="hauptstandort">{t('categories.mainLocation')}</option>
                    <option value="filiale">{t('categories.branch')}</option>
                    <option value="aussenstelle">{t('categories.externalOffice')}</option>
                  </select>
                </div>
              </div>

              {/* Address */}
              <div className="form-row">
                <div className="form-group" style={{ gridColumn: 'span 2' }}>
                  <label>{t('streetAndNumber')}*</label>
                  <input
                    type="text"
                    className="location-street"
                    value={location.street}
                    onChange={e => updateLocation(location.id, 'street', e.target.value)}
                  />
                </div>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>{t('postalCode')}*</label>
                  <input
                    type="text"
                    className="location-postalcode"
                    value={location.postalCode}
                    onChange={e => updateLocation(location.id, 'postalCode', e.target.value)}
                    maxLength={5}
                  />
                </div>
                <div className="form-group">
                  <label>{t('city')}*</label>
                  <input
                    type="text"
                    className="location-city"
                    value={location.city}
                    onChange={e => updateLocation(location.id, 'city', e.target.value)}
                  />
                </div>
              </div>

              {/* Contact Person */}
              <div className="form-row">
                <div className="form-group">
                  <label>{t('contactName')}</label>
                  <input
                    type="text"
                    className="location-contact-name"
                    value={location.contactName}
                    onChange={e => updateLocation(location.id, 'contactName', e.target.value)}
                  />
                </div>
                <div className="form-group">
                  <label>{t('contactPhone')}</label>
                  <input
                    type="tel"
                    className="location-contact-phone"
                    value={location.contactPhone}
                    onChange={e => updateLocation(location.id, 'contactPhone', e.target.value)}
                  />
                </div>
              </div>
              <div className="form-row">
                <div className="form-group" style={{ gridColumn: 'span 2' }}>
                  <label>{t('contactEmail')}</label>
                  <input
                    type="email"
                    className="location-contact-email"
                    value={location.contactEmail}
                    onChange={e => updateLocation(location.id, 'contactEmail', e.target.value)}
                  />
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
