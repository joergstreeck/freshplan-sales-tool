import { useTranslation as useTranslationBase } from 'react-i18next';

// Wrapper für useTranslation mit Default-Namespace
export function useTranslation(namespace?: string) {
  return useTranslationBase(namespace);
}

// Hook für Sprachumschaltung
export function useLanguage() {
  const { i18n } = useTranslationBase();

  const currentLanguage = i18n.language;
  const isGerman = currentLanguage === 'de';

  const toggleLanguage = () => {
    const newLang = isGerman ? 'en' : 'de';
    i18n.changeLanguage(newLang);
  };

  const setLanguage = (lang: 'de' | 'en') => {
    i18n.changeLanguage(lang);
  };

  return {
    currentLanguage,
    isGerman,
    toggleLanguage,
    setLanguage,
  };
}
