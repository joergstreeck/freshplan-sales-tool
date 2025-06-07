import React from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { useCalculatorStore } from '../store/calculatorStore';
import { formatCurrency, formatPercentage } from '@/utils/formatting';
import { CalculatorResultsSkeleton } from './CalculatorResultsSkeleton';

export function CalculatorResults() {
  const { lastResult, isCalculating } = useCalculatorStore();

  if (isCalculating) {
    return <CalculatorResultsSkeleton />;
  }

  if (!lastResult) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Rabatt-Ergebnis</CardTitle>
          <CardDescription>
            Führen Sie eine Berechnung durch, um Ihre Ersparnis zu sehen
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="text-center py-8 text-muted-foreground">
            <div className="w-16 h-16 mx-auto mb-4 rounded-full bg-muted flex items-center justify-center">
              <svg
                className="w-8 h-8"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
                aria-label="Taschenrechner-Icon"
                role="img"
              >
                <title>Kein Ergebnis verfügbar</title>
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M9 7h6m0 10v-3m-3 3h.01M9 17h.01M9 14h.01M12 14h.01M15 11h.01M12 11h.01M9 11h.01M7 21h10a2 2 0 002-2V5a2 2 0 00-2-2H7a2 2 0 00-2 2v14a2 2 0 002 2z"
                />
              </svg>
            </div>
            <p>Kein Ergebnis verfügbar</p>
            <p className="text-sm mt-1">
              Geben Sie Ihre Bestelldaten ein und klicken Sie auf "Rabatt berechnen"
            </p>
          </div>
        </CardContent>
      </Card>
    );
  }

  const hasSignificantDiscount = lastResult.totalDiscount > 0;

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center justify-between">
          Ihr Rabatt-Ergebnis
          <span className="text-2xl font-bold text-primary">
            {formatPercentage(lastResult.totalDiscount)}
          </span>
        </CardTitle>
        <CardDescription>
          Berechnung für {formatCurrency(lastResult.orderValue)} Bestellwert
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-6">
        {/* Hauptergebnis */}
        <div className="text-center p-6 bg-primary/5 rounded-lg border-2 border-primary/20">
          <div className="space-y-2">
            <p className="text-sm text-muted-foreground">Ihre Gesamtersparnis</p>
            <p className="text-4xl font-bold text-primary">
              {formatCurrency(lastResult.discountAmount)}
            </p>
            <p className="text-lg text-muted-foreground">
              Endpreis:{' '}
              <span className="font-semibold">{formatCurrency(lastResult.finalPrice)}</span>
            </p>
          </div>
        </div>

        {/* Rabatt-Aufschlüsselung */}
        <div className="space-y-3">
          <h4 className="font-semibold text-sm text-muted-foreground uppercase tracking-wide">
            Rabatt-Aufschlüsselung
          </h4>

          <div className="space-y-2">
            <DiscountRow
              label="Mengenrabatt"
              percentage={lastResult.baseDiscount}
              description={`Basierend auf ${formatCurrency(lastResult.orderValue)} Bestellwert`}
            />

            <DiscountRow
              label="Frühbucher-Rabatt"
              percentage={lastResult.earlyDiscount}
              description={`Bei ${lastResult.leadTime} Tagen Vorlaufzeit`}
            />

            <DiscountRow
              label="Abhol-Rabatt"
              percentage={lastResult.pickupDiscount}
              description={lastResult.pickup ? 'Selbstabholung gewählt' : 'Keine Selbstabholung'}
            />

            <DiscountRow
              label="Ketten-Rabatt"
              percentage={lastResult.chainDiscount}
              description={
                lastResult.chain ? 'Kettenkunde (andere Vorteile verfügbar)' : 'Kein Kettenkunde'
              }
            />
          </div>

          {/* Gesamtsumme */}
          <div className="pt-3 border-t">
            <DiscountRow
              label="Gesamtrabatt"
              percentage={lastResult.totalDiscount}
              description="Maximum 15% möglich"
              isTotal
            />
          </div>
        </div>

        {/* Hinweise */}
        {!hasSignificantDiscount && (
          <div className="p-4 bg-amber-50 border border-amber-200 rounded-lg">
            <h4 className="font-semibold text-amber-800 mb-2">Rabatt-Optimierung möglich</h4>
            <ul className="text-sm text-amber-700 space-y-1">
              {lastResult.orderValue < 5000 && (
                <li>• Erhöhen Sie den Bestellwert auf mindestens 5.000€ für ersten Rabatt</li>
              )}
              {lastResult.leadTime < 10 && (
                <li>• Planen Sie mindestens 10 Tage Vorlaufzeit für Frühbucher-Rabatt</li>
              )}
              {!lastResult.pickup && lastResult.orderValue >= 5000 && (
                <li>• Wählen Sie Selbstabholung für zusätzliche 2% Rabatt</li>
              )}
            </ul>
          </div>
        )}

        {hasSignificantDiscount && lastResult.totalDiscount >= 10 && (
          <div className="p-4 bg-green-50 border border-green-200 rounded-lg">
            <h4 className="font-semibold text-green-800 mb-2">Ausgezeichneter Rabatt!</h4>
            <p className="text-sm text-green-700">
              Sie haben bereits einen sehr guten Rabatt von{' '}
              {formatPercentage(lastResult.totalDiscount)} erreicht.
              {lastResult.totalDiscount < 15 &&
                ' Prüfen Sie weitere Optimierungsmöglichkeiten für bis zu 15% Gesamtrabatt.'}
            </p>
          </div>
        )}
      </CardContent>
    </Card>
  );
}

interface DiscountRowProps {
  label: string;
  percentage: number;
  description: string;
  isTotal?: boolean;
}

const DiscountRow = React.memo(
  ({ label, percentage, description, isTotal = false }: DiscountRowProps) => {
    const hasDiscount = percentage > 0;

    return (
      <div
        className={`flex items-center justify-between p-3 rounded-md ${
          isTotal ? 'bg-primary/10 border border-primary/20' : 'bg-muted/50'
        }`}
      >
        <div className="flex-1">
          <div className="flex items-center gap-2">
            <span
              className={`${isTotal ? 'font-semibold' : ''} ${hasDiscount ? '' : 'text-muted-foreground'}`}
            >
              {label}
            </span>
            {hasDiscount && (
              <span className="text-xs px-2 py-1 bg-green-100 text-green-800 rounded-full">✓</span>
            )}
          </div>
          <p className="text-xs text-muted-foreground mt-1">{description}</p>
        </div>
        <div className={`text-right ${isTotal ? 'font-bold' : ''}`}>
          <span className={hasDiscount ? 'text-primary font-semibold' : 'text-muted-foreground'}>
            {formatPercentage(percentage)}
          </span>
        </div>
      </div>
    );
  }
);

DiscountRow.displayName = 'DiscountRow';
