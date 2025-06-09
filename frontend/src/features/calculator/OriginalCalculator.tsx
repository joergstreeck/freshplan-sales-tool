import { useState, useEffect } from 'react';
import { useCalculateDiscount } from '../api/calculatorQueries';
import '../../../styles/legacy/calculator.css';
import '../../../styles/legacy/variables.css';
import '../../../styles/legacy/components.css';

// Original Calculator Component - 1:1 Migration
export function OriginalCalculator() {
  const [orderValue, setOrderValue] = useState(15000);
  const [leadTime, setLeadTime] = useState(14);
  const [pickup, setPickup] = useState(false);
  const [chain, setChain] = useState(false);

  const calculateMutation = useCalculateDiscount();

  // Calculate on every change (like original)
  useEffect(() => {
    calculateMutation.mutate({
      orderValue,
      leadTime,
      pickup,
      chain,
    });
  }, [orderValue, leadTime, pickup, chain]);

  const result = calculateMutation.data;

  // Format currency like original
  const formatCurrency = (value: number) => {
    return new Intl.NumberFormat('de-DE', {
      style: 'currency',
      currency: 'EUR',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(value);
  };

  // Update slider progress (visual effect from original)
  const updateSliderProgress = (value: number, min: number, max: number) => {
    const percentage = ((value - min) / (max - min)) * 100;
    return {
      background: `linear-gradient(to right, #94c456 0%, #94c456 ${percentage}%, #e0e0e0 ${percentage}%, #e0e0e0 100%)`,
    };
  };

  return (
    <div className="demonstrator-container">
      {/* Calculator Section */}
      <div className="calculator-section">
        <h2 className="section-title">FreshPlan Rabattrechner</h2>

        {/* Order Value Slider */}
        <div className="slider-group">
          <div className="slider-label">
            <span>Bestellwert</span>
            <span className="slider-value">{formatCurrency(orderValue)}</span>
          </div>
          <input
            type="range"
            className="slider"
            min="1000"
            max="100000"
            step="1000"
            value={orderValue}
            onChange={e => setOrderValue(Number(e.target.value))}
            style={updateSliderProgress(orderValue, 1000, 100000)}
          />
        </div>

        {/* Lead Time Slider */}
        <div className="slider-group">
          <div className="slider-label">
            <span>Vorlaufzeit</span>
            <span className="slider-value">{leadTime} Tage</span>
          </div>
          <input
            type="range"
            className="slider"
            min="0"
            max="60"
            value={leadTime}
            onChange={e => setLeadTime(Number(e.target.value))}
            style={updateSliderProgress(leadTime, 0, 60)}
          />
        </div>

        {/* Checkboxes */}
        <div className="checkbox-group">
          <label className="checkbox-label">
            <input type="checkbox" checked={pickup} onChange={e => setPickup(e.target.checked)} />
            <span>Abholung (Mindestbestellwert: 5.000€ netto)</span>
          </label>
          <label className="checkbox-label">
            <input type="checkbox" checked={chain} onChange={e => setChain(e.target.checked)} />
            <span>Kettenkunde</span>
          </label>
        </div>

        {/* Results - only show when we have data */}
        {result && (
          <div className="results-section">
            <h3>Ihre Rabatte:</h3>
            <div className="discount-grid">
              <div className="discount-item">
                <span className="discount-label">Basisrabatt:</span>
                <span className="discount-value">{result.baseDiscount}%</span>
              </div>
              <div className="discount-item">
                <span className="discount-label">Frühbucher:</span>
                <span className="discount-value">+{result.earlyDiscount}%</span>
              </div>
              {result.pickupDiscount > 0 && (
                <div className="discount-item">
                  <span className="discount-label">Abholung:</span>
                  <span className="discount-value">+{result.pickupDiscount}%</span>
                </div>
              )}
              <div className="discount-item total">
                <span className="discount-label">Gesamtrabatt:</span>
                <span className="discount-value accent">{result.totalDiscount}%</span>
              </div>
            </div>

            <div className="price-summary">
              <div className="price-row">
                <span>Ersparnis:</span>
                <span className="savings">{formatCurrency(result.savingsAmount)}</span>
              </div>
              <div className="price-row final">
                <span>Endpreis:</span>
                <span className="final-price">{formatCurrency(result.finalPrice)}</span>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Right Column - Information */}
      <div className="info-section">
        {/* Scenario Cards */}
        <div className="info-card modern">
          <div className="info-header">
            <h3>Beispielszenarien</h3>
          </div>
          <div className="scenario-grid">
            <div
              className="scenario-card"
              onClick={() => {
                setOrderValue(8000);
                setLeadTime(5);
                setPickup(false);
                setChain(false);
              }}
            >
              <h4>Spontanbestellung</h4>
              <p>Kurzfristige Bestellung mit geringem Volumen</p>
              <div className="scenario-values">
                <span>8.000€ • 5 Tage</span>
              </div>
            </div>

            <div
              className="scenario-card"
              onClick={() => {
                setOrderValue(32000);
                setLeadTime(16);
                setPickup(false);
                setChain(false);
              }}
            >
              <h4>Geplante Bestellung</h4>
              <p>Mittelfristig geplante Standardbestellung</p>
              <div className="scenario-values">
                <span>32.000€ • 16 Tage</span>
              </div>
            </div>

            <div
              className="scenario-card"
              onClick={() => {
                setOrderValue(50000);
                setLeadTime(30);
                setPickup(true);
                setChain(false);
              }}
            >
              <h4>Optimale Konditionen</h4>
              <p>Maximaler Rabatt durch Planung und Abholung</p>
              <div className="scenario-values">
                <span>50.000€ • 30 Tage • Abholung</span>
              </div>
            </div>
          </div>
        </div>

        {/* Discount System Info */}
        <div className="info-card modern">
          <div className="info-header">
            <h3>FreshPlan-Rabattsystem</h3>
          </div>
          <div className="discount-info">
            <div className="discount-tier">
              <h4>Mengenrabatt</h4>
              <ul>
                <li>ab 5.000€: 3%</li>
                <li>ab 10.000€: 4%</li>
                <li>ab 15.000€: 6%</li>
                <li>ab 30.000€: 8%</li>
                <li>ab 50.000€: 10%</li>
              </ul>
            </div>
            <div className="discount-tier">
              <h4>Frühbucherrabatt</h4>
              <ul>
                <li>ab 14 Tage: +1%</li>
                <li>ab 21 Tage: +2%</li>
                <li>ab 30 Tage: +3%</li>
              </ul>
            </div>
            <div className="discount-tier">
              <h4>Zusätzliche Rabatte</h4>
              <ul>
                <li>Abholung: +2%</li>
                <li className="max-discount">Maximaler Gesamtrabatt: 15%</li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
