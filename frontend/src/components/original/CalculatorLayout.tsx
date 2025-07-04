import { ReactNode, useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { useLanguage } from '../../i18n/hooks';
import { formatCurrency } from '../../i18n/formatters';
import '../../styles/legacy/calculator.css';
import '../../styles/legacy/slider.css';
import '../../styles/legacy/calculator-components.css';
import '../../styles/legacy/calculator-layout.css';
import { CustomSlider } from './CustomSlider';
import { useCalculateDiscount } from '../../features/calculator/api/calculatorQueries';

interface CalculatorLayoutProps {
  leftContent?: ReactNode;
  rightContent?: ReactNode;
}

export function CalculatorLayout({ leftContent, rightContent }: CalculatorLayoutProps) {
  const { t } = useTranslation('calculator');
  const { currentLanguage } = useLanguage();
  const [orderValue, setOrderValue] = useState(15000);
  const [leadTime, setLeadTime] = useState(14);
  const [pickup, setPickup] = useState(false);
  const calculateDiscount = useCalculateDiscount();

  // Berechnung auslösen bei Änderungen
  useEffect(() => {
    calculateDiscount.mutate({
      orderValue,
      leadTime,
      pickup,
      chain: false, // TODO: Kettenkundenrabatt implementieren
    });
  }, [orderValue, leadTime, pickup]); // calculateDiscount.mutate ist stabil

  // Handler für Beispielszenarien
  const applyScenario = (
    scenarioOrderValue: number,
    scenarioLeadTime: number,
    scenarioPickup: boolean
  ) => {
    setOrderValue(scenarioOrderValue);
    setLeadTime(scenarioLeadTime);
    setPickup(scenarioPickup);
  };

  // Ergebnisse aus der API
  const result = calculateDiscount.data;
  const baseDiscount = result?.baseDiscount || 0;
  const earlyBooking = result?.earlyDiscount || 0;
  const pickupDiscount = result?.pickupDiscount || 0;
  const totalDiscount = result?.totalDiscount || 0;
  const savings = result?.savingsAmount || 0;
  const finalPrice = result?.finalPrice || orderValue;

  return (
    <div className="customer-container">
      <h2 className="section-title">{t('title')}</h2>

      <div className="demonstrator-container">
        {/* Linke Seite - Kalkulator */}
        <div className="calculator-section">
          {leftContent || (
            <div>
              {/* Bestellwert Slider */}
              <div className="form-group">
                <div className="slider-label-container">
                  <span id="orderValue-label">{t('sliders.orderValue')}</span>
                  <span className="slider-value-display">
                    {formatCurrency(orderValue, currentLanguage)}
                  </span>
                </div>
                {/* Barrierefreier Custom Slider mit Radix UI */}
                <CustomSlider
                  value={orderValue}
                  onValueChange={setOrderValue}
                  min={1000}
                  max={100000}
                  step={1000}
                  aria-label={t('sliders.orderValue')}
                  aria-labelledby="orderValue-label"
                />
              </div>

              {/* Vorlaufzeit Slider */}
              <div className="form-group">
                <div className="slider-label-container">
                  <span id="leadTime-label">{t('sliders.leadTime')}</span>
                  <span className="slider-value-display">
                    {t('sliders.leadTimeDays', { count: leadTime })}
                  </span>
                </div>
                {/* Barrierefreier Custom Slider mit Radix UI */}
                <CustomSlider
                  value={leadTime}
                  onValueChange={setLeadTime}
                  min={1}
                  max={50}
                  step={1}
                  aria-label={t('sliders.leadTime')}
                  aria-labelledby="leadTime-label"
                />
              </div>

              {/* Abholung Checkbox */}
              <div className="form-group checkbox-group">
                <label className="checkbox-label">
                  <input
                    type="checkbox"
                    className="checkbox"
                    checked={pickup}
                    onChange={e => setPickup(e.target.checked)}
                  />
                  <span>
                    {t('checkboxes.pickup', { minValue: formatCurrency(5000, currentLanguage) })}
                  </span>
                </label>
              </div>

              {/* Results Container - Grauer Bereich */}
              <div className="calculator-results-container">
                {/* Result Grid */}
                <div className="calculator-result-grid">
                  <div className="calculator-result-item">
                    <span className="calculator-result-label">{t('results.baseDiscount')}</span>
                    <span className="calculator-result-value">{baseDiscount}%</span>
                  </div>
                  <div className="calculator-result-item">
                    <span className="calculator-result-label">{t('results.earlyBooking')}</span>
                    <span className="calculator-result-value">{earlyBooking}%</span>
                  </div>
                  <div className="calculator-result-item">
                    <span className="calculator-result-label">{t('results.pickupDiscount')}</span>
                    <span className="calculator-result-value">{pickupDiscount}%</span>
                  </div>
                </div>

                {/* Total Discount */}
                <div className="total-discount">
                  <span>{t('results.totalDiscount')}</span>
                  <span className="total-value">{totalDiscount}%</span>
                </div>

                {/* Savings Display */}
                <div className="savings-display">
                  <div className="savings-item">
                    <span>{t('results.savings')}</span>
                    <span className="savings-value">
                      {formatCurrency(savings, currentLanguage)}
                    </span>
                  </div>
                  <div className="savings-item highlight">
                    <span>{t('results.finalPrice')}</span>
                    <span className="savings-value">
                      {formatCurrency(finalPrice, currentLanguage)}
                    </span>
                  </div>
                </div>
              </div>

              {/* Maximaler Gesamtrabatt */}
              <div className="max-discount-info">{t('info.maxDiscount', { max: 15 })}</div>
            </div>
          )}
        </div>

        {/* Rechte Seite - Info */}
        <div className="info-section">
          {rightContent || (
            <>
              {/* 1. Rabattsystem Details */}
              <div className="info-card">
                <div className="rabattsystem-grid">
                  {/* Basisrabatt - 3 Spalten Tabelle */}
                  <div>
                    <h3>{t('results.baseDiscount')}</h3>
                    <table className="rabatt-table">
                      <tbody>
                        <tr>
                          <td>{currentLanguage === 'de' ? 'ab' : 'from'}</td>
                          <td className="value-column">{formatCurrency(5000, currentLanguage)}</td>
                          <td className="percent-column">3%</td>
                        </tr>
                        <tr>
                          <td>{currentLanguage === 'de' ? 'ab' : 'from'}</td>
                          <td className="value-column">{formatCurrency(15000, currentLanguage)}</td>
                          <td className="percent-column">6%</td>
                        </tr>
                        <tr>
                          <td>{currentLanguage === 'de' ? 'ab' : 'from'}</td>
                          <td className="value-column">{formatCurrency(30000, currentLanguage)}</td>
                          <td className="percent-column">8%</td>
                        </tr>
                        <tr>
                          <td>{currentLanguage === 'de' ? 'ab' : 'from'}</td>
                          <td className="value-column">{formatCurrency(50000, currentLanguage)}</td>
                          <td className="percent-column">9%</td>
                        </tr>
                        <tr>
                          <td>{currentLanguage === 'de' ? 'ab' : 'from'}</td>
                          <td className="value-column">{formatCurrency(75000, currentLanguage)}</td>
                          <td className="percent-column">10%</td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                  {/* Frühbucherrabatt - 2 Spalten Tabelle */}
                  <div>
                    <h3>{t('results.earlyBooking')}</h3>
                    <table className="rabatt-table">
                      <tbody>
                        <tr>
                          <td>{currentLanguage === 'de' ? 'ab 10 Tage' : 'from 10 days'}</td>
                          <td className="percent-column">1%</td>
                        </tr>
                        <tr>
                          <td>{currentLanguage === 'de' ? 'ab 15 Tage' : 'from 15 days'}</td>
                          <td className="percent-column">2%</td>
                        </tr>
                        <tr>
                          <td>{currentLanguage === 'de' ? 'ab 30 Tage' : 'from 30 days'}</td>
                          <td className="percent-column">3%</td>
                        </tr>
                        <tr>
                          <td>&nbsp;</td>
                          <td>&nbsp;</td>
                        </tr>
                        <tr>
                          <td>{t('results.pickupDiscount')}</td>
                          <td className="percent-column">2%</td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>

              {/* 2. Kettenkundenregelung */}
              <div className="info-card">
                <h3>
                  {currentLanguage === 'de' ? 'Kettenkundenregelung' : 'Chain Customer Policy'}
                </h3>
                <p>
                  {currentLanguage === 'de'
                    ? 'Für Unternehmen mit mehreren Standorten (z.B. Hotel- oder Klinikgruppen):'
                    : 'For companies with multiple locations (e.g. hotel or clinic groups):'}
                </p>
                <div className="option-container">
                  <div className="option-box">
                    <strong className="option-label">
                      {currentLanguage === 'de' ? 'Option A:' : 'Option A:'}
                    </strong>{' '}
                    <span className="option-text">
                      {currentLanguage === 'de'
                        ? 'Bestellungen verschiedener Standorte innerhalb einer'
                        : 'Orders from different locations within one'}
                    </span>
                    <div className="option-indent">
                      {currentLanguage === 'de'
                        ? 'Woche werden zusammengerechnet'
                        : 'week are combined'}
                    </div>
                  </div>
                  <div className="option-box">
                    <strong className="option-label">
                      {currentLanguage === 'de' ? 'Option B:' : 'Option B:'}
                    </strong>{' '}
                    <span className="option-text">
                      {currentLanguage === 'de'
                        ? 'Zentrale Bestellung mit Mehrfachauslieferung'
                        : 'Central order with multiple deliveries'}
                    </span>
                  </div>
                </div>
              </div>

              {/* 3. Beispielszenarien */}
              <div className="info-card gradient">
                <h3>{t('scenarios.title')}</h3>
                <div className="scenario-grid">
                  {/* Hotelkette */}
                  <div className="scenario-card" onClick={() => applyScenario(35000, 21, true)}>
                    <div className="scenario-header">
                      <span className="scenario-icon">🏨</span>
                      <strong className="scenario-title">{t('scenarios.hotel')}</strong>
                    </div>
                    <div className="scenario-content">
                      <div className="scenario-details">
                        {formatCurrency(35000, currentLanguage)}
                        <span className="separator">•</span>
                        21 {currentLanguage === 'de' ? 'Tage' : 'days'}
                        <span className="separator">•</span>
                        {t('calculator.deliveryOptions.pickup')}
                      </div>
                      <div className="scenario-discount">
                        12% {currentLanguage === 'de' ? 'Rabatt' : 'discount'}
                      </div>
                    </div>
                  </div>

                  {/* Klinikgruppe */}
                  <div className="scenario-card" onClick={() => applyScenario(65000, 30, false)}>
                    <div className="scenario-header">
                      <span className="scenario-icon">🏥</span>
                      <strong className="scenario-title">{t('scenarios.clinic')}</strong>
                    </div>
                    <div className="scenario-content">
                      <div className="scenario-details">
                        {formatCurrency(65000, currentLanguage)}
                        <span className="separator">•</span>
                        30 {currentLanguage === 'de' ? 'Tage' : 'days'}
                        <span className="separator">•</span>
                        {t('calculator.deliveryOptions.delivery')}
                      </div>
                      <div className="scenario-discount">
                        12% {currentLanguage === 'de' ? 'Rabatt' : 'discount'}
                      </div>
                    </div>
                  </div>

                  {/* Restaurant */}
                  <div className="scenario-card" onClick={() => applyScenario(8500, 14, true)}>
                    <div className="scenario-header">
                      <span className="scenario-icon">🍽️</span>
                      <strong className="scenario-title">{t('scenarios.restaurant')}</strong>
                    </div>
                    <div className="scenario-content">
                      <div className="scenario-details">
                        {formatCurrency(8500, currentLanguage)}
                        <span className="separator">•</span>
                        14 {currentLanguage === 'de' ? 'Tage' : 'days'}
                        <span className="separator">•</span>
                        {t('calculator.deliveryOptions.pickup')}
                      </div>
                      <div className="scenario-discount">
                        6% {currentLanguage === 'de' ? 'Rabatt' : 'discount'}
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
