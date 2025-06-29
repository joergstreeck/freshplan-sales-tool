import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useLanguage } from '../../i18n/hooks';
import {
  isValidEmail,
  isValidGermanPostalCode,
  isValidPhoneNumber,
  validationMessages,
} from '../../utils/validation';
import '../../styles/legacy/forms.css';

export interface CustomerFormData {
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
  formData: CustomerFormData;
  onFormDataChange: (data: CustomerFormData) => void;
}

export function CustomerForm({ formData, onFormDataChange }: CustomerFormProps) {
  const { t } = useTranslation('customers');
  const { currentLanguage } = useLanguage();
  const [errors, setErrors] = useState<Record<string, string>>({});

  const validateField = (name: string, value: string) => {
    const newErrors = { ...errors };

    // Email validation
    if (name === 'contactEmail' && value) {
      if (!isValidEmail(value)) {
        newErrors[name] = validationMessages.email[currentLanguage];
      } else {
        delete newErrors[name];
      }
    }

    // Postal code validation
    if (name === 'postalCode' && value) {
      if (!isValidGermanPostalCode(value)) {
        newErrors[name] = validationMessages.postalCode[currentLanguage];
      } else {
        delete newErrors[name];
      }
    }

    // Phone validation
    if (name === 'contactPhone' && value) {
      if (!isValidPhoneNumber(value)) {
        newErrors[name] = validationMessages.phone[currentLanguage];
      } else {
        delete newErrors[name];
      }
    }

    setErrors(newErrors);
  };

  const handleInputChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>
  ) => {
    const { name, value } = e.target;
    onFormDataChange({
      ...formData,
      [name]: value,
    });
    validateField(name, value);
  };

  const formatCurrency = (value: string) => {
    // Remove all non-digits
    const digits = value.replace(/\D/g, '');
    // Format with thousand separators
    return digits.replace(/\B(?=(\d{3})+(?!\d))/g, '.');
  };

  const handleVolumeChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const formatted = formatCurrency(e.target.value);
    onFormDataChange({
      ...formData,
      expectedVolume: formatted,
    });
  };

  return (
    <div className="customer-container">
      <h2 className="section-title">{t('formTitle')}</h2>

      <form id="customerForm" className="customer-form">
        {/* Grunddaten */}
        <div className="form-section">
          <h3 className="form-section-title">{t('sections.basicData')}</h3>
          <div className="form-row">
            <div className="form-group">
              <label htmlFor="companyName">{t('fields.companyName')}</label>
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
              <label htmlFor="legalForm">{t('fields.legalForm')}</label>
              <select
                id="legalForm"
                name="legalForm"
                value={formData.legalForm}
                onChange={handleInputChange}
                required
              >
                <option value="">{t('options.pleaseSelect')}</option>
                <option value="gmbh">{t('options.legalForms.gmbh')}</option>
                <option value="ag">{t('options.legalForms.ag')}</option>
                <option value="gbr">{t('options.legalForms.gbr')}</option>
                <option value="einzelunternehmen">
                  {t('options.legalForms.einzelunternehmen')}
                </option>
                <option value="kg">{t('options.legalForms.kg')}</option>
                <option value="ohg">{t('options.legalForms.ohg')}</option>
                <option value="ug">{t('options.legalForms.ug')}</option>
              </select>
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="customerType">{t('fields.customerType')}*</label>
              <select
                id="customerType"
                name="customerType"
                value={formData.customerType}
                onChange={handleInputChange}
                required
              >
                <option value="">{t('options.pleaseSelect')}</option>
                <option value="neukunde">{t('options.customerTypes.neukunde')}</option>
                <option value="bestandskunde">{t('options.customerTypes.bestandskunde')}</option>
              </select>
            </div>
            <div className="form-group">
              <label htmlFor="industry">{t('fields.industry')}*</label>
              <select
                id="industry"
                name="industry"
                value={formData.industry}
                onChange={handleInputChange}
                required
              >
                <option value="">{t('options.pleaseSelect')}</option>
                <option value="hotel">{t('options.industries.hotel')}</option>
                <option value="krankenhaus">{t('options.industries.krankenhaus')}</option>
                <option value="seniorenresidenz">{t('options.industries.seniorenresidenz')}</option>
                <option value="betriebsrestaurant">
                  {t('options.industries.betriebsrestaurant')}
                </option>
                <option value="restaurant">{t('options.industries.restaurant')}</option>
              </select>
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="chainCustomer">{t('fields.chainCustomer')}</label>
              <select
                id="chainCustomer"
                name="chainCustomer"
                value={formData.chainCustomer}
                onChange={handleInputChange}
              >
                <option value="nein">{t('options.no')}</option>
                <option value="ja">{t('options.yes')}</option>
              </select>
            </div>
            <div className="form-group">
              <label htmlFor="customerNumber">{t('fields.customerNumberInternal')}</label>
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
          <h3 className="form-section-title">{t('sections.addressData')}</h3>
          <div className="form-row">
            <div className="form-group form-group-full">
              <label htmlFor="street">{t('fields.streetAndNumber')}*</label>
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
              <label htmlFor="postalCode">{t('fields.postalCode')}*</label>
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
              <label htmlFor="city">{t('fields.city')}*</label>
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
          <h3 className="form-section-title">{t('sections.contactData')}</h3>
          <div className="form-row">
            <div className="form-group">
              <label htmlFor="contactName">{t('fields.contactName')}*</label>
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
              <label htmlFor="contactPosition">{t('fields.contactPosition')}</label>
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
              <label htmlFor="contactPhone">{t('fields.contactPhone')}*</label>
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
              <label htmlFor="contactEmail">{t('fields.contactEmail')}*</label>
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
          <h3 className="form-section-title">{t('sections.businessData')}</h3>
          <div className="form-row">
            <div className="form-group">
              <label htmlFor="expectedVolume">{t('fields.expectedVolume')}*</label>
              <input
                type="text"
                id="expectedVolume"
                name="expectedVolume"
                value={formData.expectedVolume}
                onChange={handleVolumeChange}
                placeholder={t('placeholders.expectedVolume')}
                required
              />
            </div>
            <div className="form-group">
              <label htmlFor="paymentMethod">{t('fields.paymentMethod')}*</label>
              <select
                id="paymentMethod"
                name="paymentMethod"
                value={formData.paymentMethod}
                onChange={handleInputChange}
                required
              >
                <option value="">{t('options.pleaseSelect')}</option>
                <option value="vorkasse">{t('options.paymentMethods.prepayment')}</option>
                <option value="barzahlung">{t('options.paymentMethods.cash')}</option>
                <option value="rechnung">{t('options.paymentMethods.invoice')}</option>
              </select>
            </div>
          </div>
        </div>

        {/* Zusatzinformationen */}
        <div className="form-section">
          <h3 className="form-section-title">{t('sections.additionalData')}</h3>
          <div className="form-row">
            <div className="form-group form-group-full">
              <label htmlFor="notes">{t('fields.notes')}</label>
              <textarea
                id="notes"
                name="notes"
                rows={3}
                value={formData.notes}
                onChange={handleInputChange}
                placeholder={t('placeholders.notes')}
              />
            </div>
          </div>
          <div className="form-row">
            <div className="form-group">
              <label htmlFor="customField1">{t('fields.customField1')}</label>
              <input
                type="text"
                id="customField1"
                name="customField1"
                value={formData.customField1}
                onChange={handleInputChange}
              />
            </div>
            <div className="form-group">
              <label htmlFor="customField2">{t('fields.customField2')}</label>
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
