import { Button, Menu, MenuItem } from '@mui/material';
import { Language as LanguageIcon } from '@mui/icons-material';
import { useState, useEffect } from 'react';
import { useLanguage } from '../i18n/hooks';
import { useTranslation } from 'react-i18next';

interface LanguageSwitchProps {
  onLanguageChange?: (lang: string) => void;
}

export function LanguageSwitch({ onLanguageChange }: LanguageSwitchProps = {}) {
  const { currentLanguage, setLanguage } = useLanguage();
  const { t } = useTranslation('navigation');
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);

  const handleClick = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  const handleLanguageChange = (lang: 'de' | 'en') => {
    setLanguage(lang);
    onLanguageChange?.(lang);
    handleClose();
  };

  // Sync initial language
  useEffect(() => {
    onLanguageChange?.(currentLanguage);
  }, [currentLanguage, onLanguageChange]);

  return (
    <>
      <Button
        color="inherit"
        onClick={handleClick}
        startIcon={<LanguageIcon />}
        sx={{ textTransform: 'none' }}
      >
        {currentLanguage === 'de' ? '🇩🇪 DE' : '🇬🇧 EN'}
      </Button>
      <Menu anchorEl={anchorEl} open={Boolean(anchorEl)} onClose={handleClose}>
        <MenuItem onClick={() => handleLanguageChange('de')} selected={currentLanguage === 'de'}>
          🇩🇪 {t('menu.german')}
        </MenuItem>
        <MenuItem onClick={() => handleLanguageChange('en')} selected={currentLanguage === 'en'}>
          🇬🇧 {t('menu.english')}
        </MenuItem>
      </Menu>
    </>
  );
}
