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

  const handleSliderChange = (value: number, setter: (val: number) => void) => {
    setter(value);
  };

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
                  <span className="slider-value-display">€{orderValue.toLocaleString('de-DE')}</span>
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
              <div className="max-discount-info">
                Maximaler Gesamtrabatt: 15%
              </div>
            </div>
          )}
        </div>
        
        {/* Rechte Seite - Info */}
        <div className="info-section" style={{
          display: 'flex',
          flexDirection: 'column',
          gap: '0.875rem',
          height: '100%',
          justifyContent: 'space-between'
        }}>
          {rightContent || (
            <>
              {/* 1. Rabattsystem Details */}
              <div className="info-card" style={{
                background: 'white',
                padding: '1.75rem',
                borderRadius: '12px',
                boxShadow: '0 2px 12px rgba(0, 0, 0, 0.06)'
              }}>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem' }}>
                  {/* Basisrabatt - 3 Spalten Tabelle */}
                  <div>
                    <h4 style={{ color: '#004F7B', fontSize: '0.875rem', fontFamily: 'Antonio, sans-serif', fontWeight: 700, marginBottom: '0.5rem' }}>Basisrabatt</h4>
                    <table style={{ width: '100%', fontSize: '0.875rem', fontFamily: 'Poppins, sans-serif', borderCollapse: 'collapse' }}>
                      <tbody>
                        <tr>
                          <td style={{ padding: '0.25rem 0' }}>ab</td>
                          <td style={{ padding: '0.25rem 0.5rem', textAlign: 'right', width: '80px' }}>5.000€</td>
                          <td style={{ padding: '0.25rem 0', textAlign: 'right', color: '#94c456', fontWeight: '600' }}>3%</td>
                        </tr>
                        <tr>
                          <td style={{ padding: '0.25rem 0' }}>ab</td>
                          <td style={{ padding: '0.25rem 0.5rem', textAlign: 'right', width: '80px' }}>15.000€</td>
                          <td style={{ padding: '0.25rem 0', textAlign: 'right', color: '#94c456', fontWeight: '600' }}>6%</td>
                        </tr>
                        <tr>
                          <td style={{ padding: '0.25rem 0' }}>ab</td>
                          <td style={{ padding: '0.25rem 0.5rem', textAlign: 'right', width: '80px' }}>30.000€</td>
                          <td style={{ padding: '0.25rem 0', textAlign: 'right', color: '#94c456', fontWeight: '600' }}>8%</td>
                        </tr>
                        <tr>
                          <td style={{ padding: '0.25rem 0' }}>ab</td>
                          <td style={{ padding: '0.25rem 0.5rem', textAlign: 'right', width: '80px' }}>50.000€</td>
                          <td style={{ padding: '0.25rem 0', textAlign: 'right', color: '#94c456', fontWeight: '600' }}>9%</td>
                        </tr>
                        <tr>
                          <td style={{ padding: '0.25rem 0' }}>ab</td>
                          <td style={{ padding: '0.25rem 0.5rem', textAlign: 'right', width: '80px' }}>75.000€</td>
                          <td style={{ padding: '0.25rem 0', textAlign: 'right', color: '#94c456', fontWeight: '600' }}>10%</td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                  {/* Frühbucherrabatt - 2 Spalten Tabelle */}
                  <div>
                    <h4 style={{ color: '#004F7B', fontSize: '0.875rem', fontFamily: 'Antonio, sans-serif', fontWeight: 700, marginBottom: '0.5rem' }}>Frühbucherrabatt</h4>
                    <table style={{ width: '100%', fontSize: '0.875rem', fontFamily: 'Poppins, sans-serif', borderCollapse: 'collapse' }}>
                      <tbody>
                        <tr>
                          <td style={{ padding: '0.25rem 0' }}>ab 10 Tage</td>
                          <td style={{ padding: '0.25rem 0', textAlign: 'right', color: '#94c456', fontWeight: '600' }}>1%</td>
                        </tr>
                        <tr>
                          <td style={{ padding: '0.25rem 0' }}>ab 15 Tage</td>
                          <td style={{ padding: '0.25rem 0', textAlign: 'right', color: '#94c456', fontWeight: '600' }}>2%</td>
                        </tr>
                        <tr>
                          <td style={{ padding: '0.25rem 0' }}>ab 30 Tage</td>
                          <td style={{ padding: '0.25rem 0', textAlign: 'right', color: '#94c456', fontWeight: '600' }}>3%</td>
                        </tr>
                        <tr>
                          <td style={{ padding: '0.25rem 0' }}>&nbsp;</td>
                          <td style={{ padding: '0.25rem 0' }}>&nbsp;</td>
                        </tr>
                        <tr>
                          <td style={{ padding: '0.25rem 0' }}>Abholung</td>
                          <td style={{ padding: '0.25rem 0', textAlign: 'right', color: '#94c456', fontWeight: '600' }}>2%</td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>

              {/* 2. Kettenkundenregelung */}
              <div className="info-card" style={{
                background: 'white',
                padding: '1.75rem',
                borderRadius: '12px',
                boxShadow: '0 2px 12px rgba(0, 0, 0, 0.06)'
              }}>
                <h3 style={{ color: '#004F7B', fontSize: '1.125rem', fontFamily: 'Antonio, sans-serif', fontWeight: 700, marginBottom: '1rem' }}>Kettenkundenregelung</h3>
                <p style={{ color: '#666', marginBottom: '1rem', fontSize: '0.875rem', fontFamily: 'Poppins, sans-serif' }}>
                  Für Unternehmen mit mehreren Standorten (z.B. Hotel- oder Klinikgruppen):
                </p>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                  <div style={{ 
                    padding: '0.75rem',
                    background: '#f8f9fa',
                    borderRadius: '6px',
                    fontSize: '0.875rem'
                  }}>
                    <div>
                      <strong style={{ color: '#004F7B', fontFamily: 'Poppins, sans-serif' }}>Option A:</strong> <span style={{ fontFamily: 'Poppins, sans-serif' }}>Bestellungen verschiedener Standorte innerhalb einer</span>
                      <div style={{ paddingLeft: '4.5rem', fontFamily: 'Poppins, sans-serif' }}>Woche werden zusammengerechnet</div>
                    </div>
                  </div>
                  <div style={{ 
                    padding: '0.75rem',
                    background: '#f8f9fa',
                    borderRadius: '6px',
                    fontSize: '0.875rem'
                  }}>
                    <strong style={{ color: '#004F7B', fontFamily: 'Poppins, sans-serif' }}>Option B:</strong> <span style={{ fontFamily: 'Poppins, sans-serif' }}>Zentrale Bestellung mit Mehrfachauslieferung</span>
                  </div>
                </div>
              </div>

              {/* 3. Beispielszenarien */}
              <div className="info-card" style={{
                background: 'linear-gradient(135deg, rgba(148, 196, 86, 0.1), rgba(0, 79, 123, 0.05))',
                padding: '1.75rem',
                borderRadius: '12px',
                boxShadow: '0 2px 12px rgba(0, 0, 0, 0.06)'
              }}>
                <h3 style={{ color: '#004F7B', fontSize: '1.125rem', fontFamily: 'Antonio, sans-serif', fontWeight: 700, marginBottom: '1rem' }}>Beispielszenarien</h3>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '0.75rem' }}>
                  {/* Hotelkette */}
                  <div style={{
                    background: 'white',
                    padding: '0.75rem',
                    borderRadius: '8px',
                    cursor: 'pointer',
                    transition: 'all 0.3s',
                    border: '1px solid transparent',
                    fontFamily: 'Poppins, sans-serif',
                    textAlign: 'center'
                  }}>
                    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.5rem', marginBottom: '0.25rem' }}>
                      <span style={{ fontSize: '1.25rem' }}>🏨</span>
                      <strong style={{ color: '#333' }}>Hotelkette</strong>
                    </div>
                    <div style={{ fontSize: '0.75rem', color: '#666' }}>
                      35.000 € • 21 Tage • Abholung
                    </div>
                    <div style={{ fontSize: '0.875rem', color: '#94c456', fontWeight: '600', marginTop: '0.25rem' }}>
                      12% Rabatt
                    </div>
                  </div>
                  
                  {/* Klinikgruppe */}
                  <div style={{
                    background: 'white',
                    padding: '0.75rem',
                    borderRadius: '8px',
                    cursor: 'pointer',
                    transition: 'all 0.3s',
                    border: '1px solid transparent',
                    fontFamily: 'Poppins, sans-serif',
                    textAlign: 'center'
                  }}>
                    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.5rem', marginBottom: '0.25rem' }}>
                      <span style={{ fontSize: '1.25rem' }}>🏥</span>
                      <strong style={{ color: '#333' }}>Klinikgruppe</strong>
                    </div>
                    <div style={{ fontSize: '0.75rem', color: '#666' }}>
                      65.000 € • 30 Tage • Lieferung
                    </div>
                    <div style={{ fontSize: '0.875rem', color: '#94c456', fontWeight: '600', marginTop: '0.25rem' }}>
                      12% Rabatt
                    </div>
                  </div>
                  
                  {/* Restaurant */}
                  <div style={{
                    background: 'white',
                    padding: '0.75rem',
                    borderRadius: '8px',
                    cursor: 'pointer',
                    transition: 'all 0.3s',
                    border: '1px solid transparent',
                    fontFamily: 'Poppins, sans-serif',
                    textAlign: 'center'
                  }}>
                    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.5rem', marginBottom: '0.25rem' }}>
                      <span style={{ fontSize: '1.25rem' }}>🍽️</span>
                      <strong style={{ color: '#333' }}>Restaurant</strong>
                    </div>
                    <div style={{ fontSize: '0.75rem', color: '#666' }}>
                      8.500 € • 14 Tage • Abholung
                    </div>
                    <div style={{ fontSize: '0.875rem', color: '#94c456', fontWeight: '600', marginTop: '0.25rem' }}>
                      6% Rabatt
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