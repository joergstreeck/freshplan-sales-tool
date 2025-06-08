import { useState } from 'react';
import { Header } from '../components/original/Header';
import { Navigation } from '../components/original/Navigation';
import '../styles/legacy/variables.css';
import '../styles/legacy/header-logo.css';

export function TestHeaderNavPage() {
  const [activeTab, setActiveTab] = useState('calculator');

  const handleLanguageChange = (lang: string) => {
    console.log('Language changed to:', lang);
  };

  const handleClearForm = () => {
    console.log('Clear form clicked');
  };

  const handleSave = () => {
    console.log('Save clicked');
  };

  const handleTabChange = (tab: string) => {
    console.log('Tab changed to:', tab);
    setActiveTab(tab);
  };

  return (
    <>
      <Header 
        onLanguageChange={handleLanguageChange}
        onClearForm={handleClearForm}
        onSave={handleSave}
      />
      <Navigation 
        activeTab={activeTab}
        onTabChange={handleTabChange}
      />
      <div style={{ padding: '2rem', maxWidth: '1400px', margin: '0 auto' }}>
        <h2>Header & Navigation Test</h2>
        <p>Aktiver Tab: <strong>{activeTab}</strong></p>
        <p>Klicken Sie auf die Tabs um zwischen ihnen zu wechseln.</p>
      </div>
    </>
  );
}