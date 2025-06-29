import { useTranslation } from 'react-i18next';
import { LanguageSwitchLegacy } from './LanguageSwitchLegacy';
import '../../styles/legacy/header-logo.css';

interface HeaderProps {
  onLanguageChange: (lang: string) => void;
  onClearForm: () => void;
  onSave: () => void;
}

export function Header({ onLanguageChange, onClearForm, onSave }: HeaderProps) {
  const { t } = useTranslation(['navigation', 'common']);

  return (
    <header className="header">
      <div className="header-container">
        <div className="header-content">
          <div className="logo">
            <img src="/freshfoodzlogo.png" alt="FreshFoodz Logo" className="logo-img" />
            <div className="logo-text-container">
              <h1 className="logo-text">FreshPlan</h1>
              <span className="logo-tagline">{t('navigation:header.tagline')}</span>
            </div>
          </div>
          <div className="header-actions">
            <LanguageSwitchLegacy onLanguageChange={onLanguageChange} />
            <button className="header-btn header-btn-clear" onClick={onClearForm}>
              <span>{t('common:buttons.clear')}</span>
            </button>
            <button className="header-btn header-btn-save" onClick={onSave}>
              <span>{t('common:buttons.save')}</span>
            </button>
          </div>
        </div>
      </div>
    </header>
  );
}
