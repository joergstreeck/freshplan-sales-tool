import { useState } from 'react';
import { Header } from './Header';
import { Navigation } from './Navigation';
import { CalculatorLayout } from './CalculatorLayout';
import { CustomerForm } from './CustomerForm';
import { LocationsForm } from './LocationsForm';
import '../../styles/legacy/variables.css';
import '../../styles/legacy/design-system.css';
import '../../styles/legacy/typography.css';
import '../../styles/legacy/layout.css';
import '../../styles/legacy/responsive.css';

export function LegacyApp() {
  const [activeTab, setActiveTab] = useState('calculator');
  const [customerIndustry, setCustomerIndustry] = useState('');

  const handleTabChange = (tabId: string) => {
    setActiveTab(tabId);
  };

  const renderTabContent = () => {
    switch (activeTab) {
      case 'calculator':
        return <CalculatorLayout />;
      case 'customer':
        return <CustomerForm onIndustryChange={setCustomerIndustry} />;
      case 'locations':
        return (
          <LocationsForm customerIndustry={customerIndustry} onDetailedLocationsChange={() => {}} />
        );
      case 'creditcheck':
        return (
          <div className="customer-container">
            <h2 className="section-title">Bonitätsprüfung</h2>
            <p>Bonitätsprüfung - Kommt bald...</p>
          </div>
        );
      case 'profile':
        return (
          <div className="customer-container">
            <h2 className="section-title">Profil</h2>
            <p>Profilverwaltung - Kommt bald...</p>
          </div>
        );
      case 'offer':
        return (
          <div className="customer-container">
            <h2 className="section-title">Angebot</h2>
            <p>Angebotserstellung - Kommt bald...</p>
          </div>
        );
      case 'settings':
        return (
          <div className="customer-container">
            <h2 className="section-title">Einstellungen</h2>
            <p>Einstellungen - Kommt bald...</p>
          </div>
        );
      default:
        return null;
    }
  };

  return (
    <div>
      <Header />
      <Navigation activeTab={activeTab} onTabChange={handleTabChange} />
      {renderTabContent()}
    </div>
  );
}
