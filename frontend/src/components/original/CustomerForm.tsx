import { useState } from 'react';
import '../../styles/legacy/forms.css';

interface CustomerFormData {
  // Grunddaten
  companyName: string;
  legalForm: string;
  customerType: string;
  industry: string;
  chainCustomer: string;
  customerNumber: string;
  
  // Adressdaten
  street: string;
  postalCode: string;
  city: string;
  
  // Ansprechpartner
  contactName: string;
  contactPosition: string;
  contactPhone: string;
  contactEmail: string;
  
  // Geschäftsdaten
  expectedVolume: string;
  paymentMethod: string;
  
  // Zusatzinformationen
  notes: string;
  customField1: string;
  customField2: string;
}

interface CustomerFormProps {
  onIndustryChange?: (industry: string) => void;
}

export function CustomerForm({ onIndustryChange }: CustomerFormProps) {
  const [formData, setFormData] = useState<CustomerFormData>({
    companyName: '',
    legalForm: '',
    customerType: '',
    industry: '',
    chainCustomer: 'nein',
    customerNumber: '',
    street: '',
    postalCode: '',
    city: '',
    contactName: '',
    contactPosition: '',
    contactPhone: '',
    contactEmail: '',
    expectedVolume: '',
    paymentMethod: '',
    notes: '',
    customField1: '',
    customField2: ''
  });

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    
    // Notify parent component when industry changes
    if (name === 'industry' && onIndustryChange) {
      onIndustryChange(value);
    }
  };

  const formatCurrency = (value: string) => {
    // Remove all non-digits
    const digits = value.replace(/\D/g, '');
    // Format with thousand separators
    return digits.replace(/\B(?=(\d{3})+(?!\d))/g, '.');
  };

  const handleVolumeChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const formatted = formatCurrency(e.target.value);
    setFormData(prev => ({
      ...prev,
      expectedVolume: formatted
    }));
  };

  return (
    <div className="customer-container">
      <h2 className="section-title">Kundendaten erfassen</h2>
      
      <form id="customerForm" className="customer-form">
        {/* Grunddaten */}
        <div className="form-section">
          <h3 className="form-section-title">Grunddaten</h3>
          <div className="form-row">
            <div className="form-group">
              <label htmlFor="companyName">Firmenname*</label>
              <input 
                type="text" 
                id="companyName" 
                name="companyName" 
                value={formData.companyName}
                onChange={handleInputChange}
                required 
              />
            </div>
            <div className="form-group">
              <label htmlFor="legalForm">Rechtsform*</label>
              <select 
                id="legalForm" 
                name="legalForm" 
                value={formData.legalForm}
                onChange={handleInputChange}
                required
              >
                <option value="">Bitte wählen</option>
                <option value="gmbh">GmbH</option>
                <option value="ag">AG</option>
                <option value="gbr">GbR</option>
                <option value="einzelunternehmen">Einzelunternehmen</option>
                <option value="kg">KG</option>
                <option value="ohg">OHG</option>
                <option value="ug">UG</option>
              </select>
            </div>
          </div>
          
          <div className="form-row">
            <div className="form-group">
              <label htmlFor="customerType">Kundentyp*</label>
              <select 
                id="customerType" 
                name="customerType" 
                value={formData.customerType}
                onChange={handleInputChange}
                required
              >
                <option value="">Bitte wählen</option>
                <option value="neukunde">Neukunde</option>
                <option value="bestandskunde">Bestandskunde</option>
              </select>
            </div>
            <div className="form-group">
              <label htmlFor="industry">Branche*</label>
              <select 
                id="industry" 
                name="industry" 
                value={formData.industry}
                onChange={handleInputChange}
                required
              >
                <option value="">Bitte wählen</option>
                <option value="hotel">Hotel</option>
                <option value="krankenhaus">Klinik</option>
                <option value="seniorenresidenz">Seniorenresidenz</option>
                <option value="betriebsrestaurant">Betriebsrestaurant</option>
                <option value="restaurant">Restaurant</option>
              </select>
            </div>
          </div>
          
          <div className="form-row">
            <div className="form-group">
              <label htmlFor="chainCustomer">Kettenkunde</label>
              <select 
                id="chainCustomer" 
                name="chainCustomer"
                value={formData.chainCustomer}
                onChange={handleInputChange}
              >
                <option value="nein">Nein</option>
                <option value="ja">Ja</option>
              </select>
            </div>
            <div className="form-group">
              <label htmlFor="customerNumber">Kundennummer (intern)</label>
              <input 
                type="text" 
                id="customerNumber" 
                name="customerNumber"
                value={formData.customerNumber}
                onChange={handleInputChange}
              />
            </div>
          </div>
        </div>

        {/* Adressdaten */}
        <div className="form-section">
          <h3 className="form-section-title">Adressdaten</h3>
          <div className="form-row">
            <div className="form-group form-group-full">
              <label htmlFor="street">Straße und Hausnummer*</label>
              <input 
                type="text" 
                id="street" 
                name="street" 
                value={formData.street}
                onChange={handleInputChange}
                required 
              />
            </div>
          </div>
          <div className="form-row">
            <div className="form-group">
              <label htmlFor="postalCode">PLZ*</label>
              <input 
                type="text" 
                id="postalCode" 
                name="postalCode" 
                value={formData.postalCode}
                onChange={handleInputChange}
                required 
                maxLength={5}
              />
            </div>
            <div className="form-group">
              <label htmlFor="city">Ort*</label>
              <input 
                type="text" 
                id="city" 
                name="city" 
                value={formData.city}
                onChange={handleInputChange}
                required 
              />
            </div>
          </div>
        </div>

        {/* Ansprechpartner */}
        <div className="form-section">
          <h3 className="form-section-title">Ansprechpartner</h3>
          <div className="form-row">
            <div className="form-group">
              <label htmlFor="contactName">Name*</label>
              <input 
                type="text" 
                id="contactName" 
                name="contactName" 
                value={formData.contactName}
                onChange={handleInputChange}
                required 
              />
            </div>
            <div className="form-group">
              <label htmlFor="contactPosition">Position</label>
              <input 
                type="text" 
                id="contactPosition" 
                name="contactPosition"
                value={formData.contactPosition}
                onChange={handleInputChange}
              />
            </div>
          </div>
          <div className="form-row">
            <div className="form-group">
              <label htmlFor="contactPhone">Telefon*</label>
              <input 
                type="tel" 
                id="contactPhone" 
                name="contactPhone" 
                value={formData.contactPhone}
                onChange={handleInputChange}
                required 
              />
            </div>
            <div className="form-group">
              <label htmlFor="contactEmail">E-Mail*</label>
              <input 
                type="email" 
                id="contactEmail" 
                name="contactEmail" 
                value={formData.contactEmail}
                onChange={handleInputChange}
                required 
              />
            </div>
          </div>
        </div>

        {/* Geschäftsdaten */}
        <div className="form-section">
          <h3 className="form-section-title">Geschäftsdaten</h3>
          <div className="form-row">
            <div className="form-group">
              <label htmlFor="expectedVolume">Erwartetes Jahresvolumen (€)*</label>
              <input 
                type="text" 
                id="expectedVolume" 
                name="expectedVolume" 
                value={formData.expectedVolume}
                onChange={handleVolumeChange}
                placeholder="z.B. 500.000"
                required 
              />
            </div>
            <div className="form-group">
              <label htmlFor="paymentMethod">Zahlungsart*</label>
              <select 
                id="paymentMethod" 
                name="paymentMethod" 
                value={formData.paymentMethod}
                onChange={handleInputChange}
                required
              >
                <option value="">Bitte wählen</option>
                <option value="vorkasse">Vorkasse</option>
                <option value="barzahlung">Barzahlung</option>
                <option value="rechnung">Rechnung (30 Tage netto)</option>
              </select>
            </div>
          </div>
        </div>

        {/* Zusatzinformationen */}
        <div className="form-section">
          <h3 className="form-section-title">Zusatzinformationen</h3>
          <div className="form-row">
            <div className="form-group form-group-full">
              <label htmlFor="notes">Notizen</label>
              <textarea 
                id="notes" 
                name="notes" 
                rows={3}
                value={formData.notes}
                onChange={handleInputChange}
                placeholder="Interne Notizen zum Kunden..."
              />
            </div>
          </div>
          <div className="form-row">
            <div className="form-group">
              <label htmlFor="customField1">Freifeld 1</label>
              <input 
                type="text" 
                id="customField1" 
                name="customField1"
                value={formData.customField1}
                onChange={handleInputChange}
              />
            </div>
            <div className="form-group">
              <label htmlFor="customField2">Freifeld 2</label>
              <input 
                type="text" 
                id="customField2" 
                name="customField2"
                value={formData.customField2}
                onChange={handleInputChange}
              />
            </div>
          </div>
        </div>
      </form>
    </div>
  );
}