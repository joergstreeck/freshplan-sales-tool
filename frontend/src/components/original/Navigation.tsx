import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import '../../styles/legacy/header-logo.css';

interface NavigationProps {
  activeTab?: string;
  onTabChange: (tab: string) => void;
  showLocations?: boolean;
  showDetailedLocations?: boolean;
}

interface Tab {
  id: string;
}

// Tabs in der richtigen Reihenfolge wie im Original
const tabs: Tab[] = [
  { id: 'calculator' },
  { id: 'customer' },
  { id: 'locations' },
  { id: 'detailedLocations' },
  { id: 'creditcheck' },
  { id: 'profile' },
  { id: 'offer' },
  { id: 'settings' },
];

export function Navigation({
  activeTab = 'calculator',
  onTabChange,
  showLocations = false,
  showDetailedLocations = false,
}: NavigationProps) {
  const { t } = useTranslation('navigation');
  const [currentTab, setCurrentTab] = useState(activeTab);

  useEffect(() => {
    setCurrentTab(activeTab);
  }, [activeTab]);

  const handleTabClick = (tabId: string) => {
    setCurrentTab(tabId);
    onTabChange(tabId);
  };

  // Filter tabs based on showLocations and showDetailedLocations
  const visibleTabs = tabs.filter(tab => {
    if (tab.id === 'locations') {
      return showLocations;
    }
    if (tab.id === 'detailedLocations') {
      return showDetailedLocations && showLocations;
    }
    return true;
  });

  return (
    <nav className="nav" aria-label="Main navigation">
      <div className="nav-tabs" role="tablist">
        {visibleTabs.map(tab => (
          <button
            key={tab.id}
            className={`nav-tab ${currentTab === tab.id ? 'active' : ''}`}
            data-tab={tab.id}
            role="tab"
            aria-selected={currentTab === tab.id}
            onClick={() => handleTabClick(tab.id)}
          >
            <span>{t(`tabs.${tab.id}`)}</span>
          </button>
        ))}
      </div>
    </nav>
  );
}
