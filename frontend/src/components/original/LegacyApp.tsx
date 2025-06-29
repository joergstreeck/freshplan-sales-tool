import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Header } from './Header';
import { Navigation } from './Navigation';
import { CalculatorLayout } from './CalculatorLayout';
import { CustomerForm, type CustomerFormData } from './CustomerForm';
import { LocationsForm } from './LocationsForm';
import { DetailedLocationsForm } from './DetailedLocationsForm';
import '../../styles/legacy/variables.css';
import '../../styles/legacy/design-system.css';
import '../../styles/legacy/typography.css';
import '../../styles/legacy/layout.css';
import '../../styles/legacy/responsive.css';

export function LegacyApp() {
  const { t } = useTranslation(['navigation', 'common']);
  const [activeTab, setActiveTab] = useState('calculator');
  const [showDetailedLocations, setShowDetailedLocations] = useState(false);
  const [totalLocations, setTotalLocations] = useState(0);

  // Customer form state
  const [customerFormData, setCustomerFormData] = useState<CustomerFormData>({
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
    customField2: '',
  });

  const handleTabChange = (tabId: string) => {
    setActiveTab(tabId);
  };

  const handleLanguageChange = () => {
    // Language change is handled by i18n internally
    // This is just for legacy compatibility
  };

  const handleClearForm = () => {
    setCustomerFormData({
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
      customField2: '',
    });
    setShowDetailedLocations(false);
  };

  const handleSave = () => {
    // TODO: Implement save functionality
  };

  const renderTabContent = () => {
    switch (activeTab) {
      case 'calculator':
        return <CalculatorLayout />;
      case 'customer':
        return <CustomerForm formData={customerFormData} onFormDataChange={setCustomerFormData} />;
      case 'locations':
        return (
          <LocationsForm
            customerIndustry={customerFormData.industry}
            onDetailedLocationsChange={setShowDetailedLocations}
            onTotalLocationsChange={setTotalLocations}
          />
        );
      case 'creditcheck':
        return (
          <div className="customer-container">
            <h2 className="section-title">{t('navigation:tabs.creditcheck')}</h2>
            <p>{t('common:messages.comingSoon')}</p>
          </div>
        );
      case 'profile':
        return (
          <div className="customer-container">
            <h2 className="section-title">{t('navigation:tabs.profile')}</h2>
            <p>
              {t('common:messages.featureComingSoon', { feature: t('navigation:tabs.profile') })}
            </p>
          </div>
        );
      case 'offer':
        return (
          <div className="customer-container">
            <h2 className="section-title">{t('navigation:tabs.offer')}</h2>
            <p>{t('common:messages.featureComingSoon', { feature: t('navigation:tabs.offer') })}</p>
          </div>
        );
      case 'settings':
        return (
          <div className="customer-container">
            <h2 className="section-title">{t('navigation:tabs.settings')}</h2>
            <p>{t('common:messages.comingSoon')}</p>
          </div>
        );
      case 'detailedLocations':
        return <DetailedLocationsForm totalLocations={totalLocations} />;
      default:
        return null;
    }
  };

  return (
    <div>
      <Header
        onLanguageChange={handleLanguageChange}
        onClearForm={handleClearForm}
        onSave={handleSave}
      />
      <Navigation
        activeTab={activeTab}
        onTabChange={handleTabChange}
        showLocations={customerFormData.chainCustomer === 'ja'}
        showDetailedLocations={showDetailedLocations}
      />
      {renderTabContent()}
    </div>
  );
}
