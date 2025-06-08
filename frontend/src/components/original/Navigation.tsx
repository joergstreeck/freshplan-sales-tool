import { useState, useEffect } from 'react';
import '../../styles/legacy/header-logo.css';

interface NavigationProps {
  activeTab?: string;
  onTabChange: (tab: string) => void;
}

interface Tab {
  id: string;
  label: string;
  dataI18n: string;
}

// Tabs in der richtigen Reihenfolge wie im Original
const tabs: Tab[] = [
  { id: 'calculator', label: 'Rabattrechner', dataI18n: 'calculator' },
  { id: 'customer', label: 'Kundendaten', dataI18n: 'customer' },
  { id: 'locations', label: 'Standorte', dataI18n: 'locations' },
  { id: 'creditcheck', label: 'Bonitätsprüfung', dataI18n: 'creditcheck' },
  { id: 'profile', label: 'Profil', dataI18n: 'profile' },
  { id: 'offer', label: 'Angebot', dataI18n: 'offer' },
  { id: 'settings', label: 'Einstellungen', dataI18n: 'settings' }
];

export function Navigation({ activeTab = 'calculator', onTabChange }: NavigationProps) {
  const [currentTab, setCurrentTab] = useState(activeTab);

  useEffect(() => {
    setCurrentTab(activeTab);
  }, [activeTab]);

  const handleTabClick = (tabId: string) => {
    setCurrentTab(tabId);
    onTabChange(tabId);
  };

  return (
    <nav className="nav">
      <div className="nav-tabs">
        {tabs.map((tab) => (
          <button
            key={tab.id}
            className={`nav-tab ${currentTab === tab.id ? 'active' : ''}`}
            data-tab={tab.id}
            role="tab"
            aria-selected={currentTab === tab.id}
            onClick={() => handleTabClick(tab.id)}
          >
            <span data-i18n={tab.dataI18n}>{tab.label}</span>
          </button>
        ))}
      </div>
    </nav>
  );
}