import { ReactNode, useState } from 'react';
import '../../styles/legacy/calculator.css';
import '../../styles/legacy/slider.css';
import '../../styles/legacy/calculator-components.css';
import '../../styles/legacy/calculator-layout.css';
import { CustomSlider } from './CustomSlider';

interface CalculatorLayoutProps {
  leftContent?: ReactNode;
  rightContent?: ReactNode;
}

export function CalculatorLayout({ leftContent, rightContent }: CalculatorLayoutProps) {
  const [orderValue, setOrderValue] = useState(15000);
  const [leadTime, setLeadTime] = useState(14);

  return (
    <div className="customer-container">
      <h2 className="section-title">FreshPlan Rabattrechner</h2>

      <div className="demonstrator-container">
        {/* Linke Seite - Kalkulator */}
        <div className="calculator-section">
          {leftContent || (
            <div>
              {/* Bestellwert Slider */}
              <div className="form-group">
                <div className="slider-label-container">
                  <label htmlFor="orderValue">Bestellwert</label>
                  <span className="slider-value-display">
                    €{orderValue.toLocaleString('de-DE')}
                  </span>
                </div>
                {/* Barrierefreier Custom Slider mit Radix UI */}
                <CustomSlider
                  id="orderValue"
                  value={orderValue}
                  onValueChange={setOrderValue}
                  min={1000}
                  max={100000}
                  step={1000}
                  aria-label="Bestellwert"
                />
              </div>

              {/* Vorlaufzeit Slider */}
              <div className="form-group">
                <div className="slider-label-container">
                  <label htmlFor="leadTime">Vorlaufzeit</label>
                  <span className="slider-value-display">{leadTime} Tage</span>
                </div>
                {/* Barrierefreier Custom Slider mit Radix UI */}
                <CustomSlider
                  id="leadTime"
                  value={leadTime}
                  onValueChange={setLeadTime}
                  min={1}
                  max={30}
                  step={1}
                  aria-label="Vorlaufzeit in Tagen"
                />
              </div>

              {/* Abholung Checkbox */}
              <div className="form-group checkbox-group">
                <label className="checkbox-label">
                  <input type="checkbox" className="checkbox" />
                  <span>Abholung (Mindestbestellwert: 5.000€ netto)</span>
                </label>
              </div>

              {/* Results Container - Grauer Bereich */}
              <div className="calculator-results-container">
                {/* Result Grid */}
                <div className="calculator-result-grid">
                  <div className="calculator-result-item">
                    <span className="calculator-result-label">Basisrabatt</span>
                    <span className="calculator-result-value">6%</span>
                  </div>
                  <div className="calculator-result-item">
                    <span className="calculator-result-label">Frühbucher</span>
                    <span className="calculator-result-value">1%</span>
                  </div>
                  <div className="calculator-result-item">
                    <span className="calculator-result-label">Abholung</span>
                    <span className="calculator-result-value">0%</span>
                  </div>
                </div>

                {/* Total Discount */}
                <div className="total-discount">
                  <span>Gesamtrabatt</span>
                  <span className="total-value">7%</span>
                </div>

                {/* Savings Display */}
                <div className="savings-display">
                  <div className="savings-item">
                    <span>Ersparnis</span>
                    <span className="savings-value">€1.050</span>
                  </div>
                  <div className="savings-item highlight">
                    <span>Endpreis</span>
                    <span className="savings-value">€13.950</span>
                  </div>
                </div>
              </div>

              {/* Maximaler Gesamtrabatt */}
              <div className="max-discount-info">Maximaler Gesamtrabatt: 15%</div>
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
                    <h3>Basisrabatt</h3>
                    <table className="rabatt-table">
                      <tbody>
                        <tr>
                          <td>ab</td>
                          <td className="value-column">5.000€</td>
                          <td className="percent-column">3%</td>
                        </tr>
                        <tr>
                          <td>ab</td>
                          <td className="value-column">15.000€</td>
                          <td className="percent-column">6%</td>
                        </tr>
                        <tr>
                          <td>ab</td>
                          <td className="value-column">30.000€</td>
                          <td className="percent-column">8%</td>
                        </tr>
                        <tr>
                          <td>ab</td>
                          <td className="value-column">50.000€</td>
                          <td className="percent-column">9%</td>
                        </tr>
                        <tr>
                          <td>ab</td>
                          <td className="value-column">75.000€</td>
                          <td className="percent-column">10%</td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                  {/* Frühbucherrabatt - 2 Spalten Tabelle */}
                  <div>
                    <h3>Frühbucherrabatt</h3>
                    <table className="rabatt-table">
                      <tbody>
                        <tr>
                          <td>ab 10 Tage</td>
                          <td className="percent-column">1%</td>
                        </tr>
                        <tr>
                          <td>ab 15 Tage</td>
                          <td className="percent-column">2%</td>
                        </tr>
                        <tr>
                          <td>ab 30 Tage</td>
                          <td className="percent-column">3%</td>
                        </tr>
                        <tr>
                          <td>&nbsp;</td>
                          <td>&nbsp;</td>
                        </tr>
                        <tr>
                          <td>Abholung</td>
                          <td className="percent-column">2%</td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>

              {/* 2. Kettenkundenregelung */}
              <div className="info-card">
                <h3>Kettenkundenregelung</h3>
                <p>Für Unternehmen mit mehreren Standorten (z.B. Hotel- oder Klinikgruppen):</p>
                <div className="option-container">
                  <div className="option-box">
                    <strong className="option-label">Option A:</strong>{' '}
                    <span className="option-text">
                      Bestellungen verschiedener Standorte innerhalb einer
                    </span>
                    <div className="option-indent">Woche werden zusammengerechnet</div>
                  </div>
                  <div className="option-box">
                    <strong className="option-label">Option B:</strong>{' '}
                    <span className="option-text">
                      Zentrale Bestellung mit Mehrfachauslieferung
                    </span>
                  </div>
                </div>
              </div>

              {/* 3. Beispielszenarien */}
              <div className="info-card gradient">
                <h3>Beispielszenarien</h3>
                <div className="scenario-grid">
                  {/* Hotelkette */}
                  <div className="scenario-card">
                    <div className="scenario-header">
                      <span className="scenario-icon">🏨</span>
                      <strong className="scenario-title">Hotelkette</strong>
                    </div>
                    <div className="scenario-details">35.000 € • 21 Tage • Abholung</div>
                    <div className="scenario-discount">12% Rabatt</div>
                  </div>

                  {/* Klinikgruppe */}
                  <div className="scenario-card">
                    <div className="scenario-header">
                      <span className="scenario-icon">🏥</span>
                      <strong className="scenario-title">Klinikgruppe</strong>
                    </div>
                    <div className="scenario-details">65.000 € • 30 Tage • Lieferung</div>
                    <div className="scenario-discount">12% Rabatt</div>
                  </div>

                  {/* Restaurant */}
                  <div className="scenario-card">
                    <div className="scenario-header">
                      <span className="scenario-icon">🍽️</span>
                      <strong className="scenario-title">Restaurant</strong>
                    </div>
                    <div className="scenario-details">8.500 € • 14 Tage • Abholung</div>
                    <div className="scenario-discount">6% Rabatt</div>
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
