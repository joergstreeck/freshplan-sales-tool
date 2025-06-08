import { useState } from 'react';
import '../../styles/legacy/header-logo.css';

interface HeaderProps {
  onLanguageChange: (lang: string) => void;
  onClearForm: () => void;
  onSave: () => void;
}

export function Header({ onLanguageChange, onClearForm, onSave }: HeaderProps) {
  const [language, setLanguage] = useState('de');

  const handleLanguageChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const newLang = e.target.value;
    setLanguage(newLang);
    onLanguageChange(newLang);
  };

  return (
    <header className="header">
      <div className="header-container">
        <div className="header-content">
          <div className="logo">
            <img 
              src="/freshfoodzlogo.png" 
              alt="FreshFoodz Logo" 
              className="logo-img"
            />
            <div className="logo-text-container">
              <h1 className="logo-text">FreshPlan</h1>
              <span className="logo-tagline" data-i18n="tagline">
                So einfach, schnell und lecker!
              </span>
            </div>
          </div>
          <div className="header-actions">
            <select 
              id="languageSelect" 
              className="header-select" 
              value={language}
              onChange={handleLanguageChange}
            >
              <option value="de" data-i18n="languageDE">Deutsch</option>
              <option value="en" data-i18n="languageEN">English</option>
            </select>
            <button 
              className="header-btn header-btn-clear" 
              onClick={onClearForm}
            >
              <span data-i18n="common.clearForm">Formular leeren</span>
            </button>
            <button 
              className="header-btn header-btn-save" 
              onClick={onSave}
            >
              <span data-i18n="common.save">Speichern</span>
            </button>
          </div>
        </div>
      </div>
    </header>
  );
}