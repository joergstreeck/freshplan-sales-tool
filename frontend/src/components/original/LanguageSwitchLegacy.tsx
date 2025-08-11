import { useLanguage } from '../../i18n/hooks';
import { useTranslation } from 'react-i18next';
import { useEffect } from 'react';

interface LanguageSwitchLegacyProps {
  onLanguageChange?: (lang: string) => void;
}

export function LanguageSwitchLegacy({ onLanguageChange }: LanguageSwitchLegacyProps) {
  const { currentLanguage, setLanguage } = useLanguage();
  const { t } = useTranslation('navigation');

  const handleLanguageChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const newLang = e.target.value as 'de' | 'en';
    setLanguage(newLang);
    onLanguageChange?.(newLang);
  };

  // Sync initial language
  useEffect(() => {
    onLanguageChange?.(currentLanguage);
  }, [currentLanguage, onLanguageChange]);

  return (
    <select
      id="languageSelect"
      className="header-select"
      value={currentLanguage}
      onChange={handleLanguageChange}
    >
      <option value="de">🇩🇪 {t('menu.german')}</option>
      <option value="en">🇬🇧 {t('menu.english')}</option>
    </select>
  );
}
