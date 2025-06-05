/**
 * Calculator React Hook
 * For future React migration
 */

import { useStore, useCalculatorActions } from '../store';
import { useShallow } from 'zustand/react/shallow';

export const useCalculator = () => {
  // Get state with shallow comparison to prevent unnecessary re-renders
  const state = useStore(
    useShallow((state) => ({
      orderValue: state.calculator.orderValue,
      leadTime: state.calculator.leadTime,
      pickup: state.calculator.pickup,
      chain: state.calculator.chain,
      calculation: state.calculator.calculation
    }))
  );

  // Get actions
  const actions = useCalculatorActions();

  // Computed values
  const totalDiscount = state.calculation?.totalDiscount ?? 0;
  const finalPrice = state.calculation?.finalPrice ?? 0;
  const savings = state.calculation?.discountAmount ?? 0;
  const hasDiscount = totalDiscount > 0;

  return {
    // State
    ...state,
    
    // Computed
    totalDiscount,
    finalPrice,
    savings,
    hasDiscount,
    
    // Actions
    ...actions,
    
    // Scenarios
    loadScenario: (scenario: 'spontan' | 'geplant' | 'optimal') => {
      const scenarios = {
        spontan: { orderValue: 8000, leadTime: 3, pickup: false, chain: false },
        geplant: { orderValue: 25000, leadTime: 14, pickup: false, chain: false },
        optimal: { orderValue: 50000, leadTime: 30, pickup: true, chain: false }
      };
      
      const config = scenarios[scenario];
      if (config) {
        actions.updateOrderValue(config.orderValue);
        actions.updateLeadTime(config.leadTime);
        actions.updatePickup(config.pickup);
        actions.updateChain(config.chain);
      }
    },
    
    reset: () => {
      actions.updateOrderValue(15000);
      actions.updateLeadTime(14);
      actions.updatePickup(false);
      actions.updateChain(false);
    }
  };
};

// Example React component usage (for documentation)
/*
import React from 'react';
import { useCalculator } from './useCalculator';

export const CalculatorComponent: React.FC = () => {
  const {
    orderValue,
    leadTime,
    pickup,
    chain,
    totalDiscount,
    finalPrice,
    savings,
    updateOrderValue,
    updateLeadTime,
    updatePickup,
    updateChain,
    loadScenario,
    reset
  } = useCalculator();

  return (
    <div>
      <h2>Rabattrechner</h2>
      
      <div>
        <label>Bestellwert: €{orderValue.toLocaleString('de-DE')}</label>
        <input
          type="range"
          min="0"
          max="100000"
          value={orderValue}
          onChange={(e) => updateOrderValue(Number(e.target.value))}
        />
      </div>
      
      <div>
        <label>Vorlaufzeit: {leadTime} Tage</label>
        <input
          type="range"
          min="0"
          max="45"
          value={leadTime}
          onChange={(e) => updateLeadTime(Number(e.target.value))}
        />
      </div>
      
      <div>
        <label>
          <input
            type="checkbox"
            checked={pickup}
            onChange={(e) => updatePickup(e.target.checked)}
          />
          Selbstabholung
        </label>
      </div>
      
      <div>
        <label>
          <input
            type="checkbox"
            checked={chain}
            onChange={(e) => updateChain(e.target.checked)}
          />
          Kettenkunde
        </label>
      </div>
      
      <div>
        <h3>Ergebnis</h3>
        <p>Gesamtrabatt: {totalDiscount}%</p>
        <p>Ersparnis: €{savings.toLocaleString('de-DE')}</p>
        <p>Endpreis: €{finalPrice.toLocaleString('de-DE')}</p>
      </div>
      
      <div>
        <h3>Szenarien</h3>
        <button onClick={() => loadScenario('spontan')}>Spontan</button>
        <button onClick={() => loadScenario('geplant')}>Geplant</button>
        <button onClick={() => loadScenario('optimal')}>Optimal</button>
        <button onClick={reset}>Zurücksetzen</button>
      </div>
    </div>
  );
};
*/