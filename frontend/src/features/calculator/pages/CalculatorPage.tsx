import { CalculatorForm, CalculatorResults, ScenarioSelector } from '../components';

export function CalculatorPage() {
  return (
    <main className="min-h-screen bg-background p-8">
      <div className="mx-auto max-w-7xl space-y-8">
        {/* Header */}
        <div className="text-center">
          <h1 className="text-4xl font-bold text-foreground mb-4">FreshPlan Rabatt-Kalkulator</h1>
          <p className="text-xl text-muted-foreground max-w-3xl mx-auto">
            Berechnen Sie Ihren individuellen Rabatt für Lebensmittel-Großhandelsbestellungen. Bis
            zu <span className="font-semibold text-primary">15% Gesamtrabatt</span> möglich!
          </p>
        </div>

        {/* Main Content Grid */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          {/* Left Column - Input */}
          <div className="space-y-6">
            {/* Scenario Selector */}
            <ScenarioSelector />

            {/* Calculator Form */}
            <CalculatorForm />
          </div>

          {/* Right Column - Results */}
          <div className="space-y-6">
            <CalculatorResults />
          </div>
        </div>

        {/* Info Section */}
        <div className="mt-12 p-6 bg-muted/50 rounded-lg">
          <h3 className="text-lg font-semibold mb-4">So funktioniert die Rabatt-Berechnung</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 text-sm">
            <div className="space-y-2">
              <h4 className="font-semibold text-primary">Mengenrabatt</h4>
              <ul className="space-y-1 text-muted-foreground">
                <li>• Ab 5.000€: 3%</li>
                <li>• Ab 15.000€: 6%</li>
                <li>• Ab 30.000€: 8%</li>
                <li>• Ab 50.000€: 9%</li>
                <li>• Ab 75.000€: 10%</li>
              </ul>
            </div>
            <div className="space-y-2">
              <h4 className="font-semibold text-primary">Frühbucher-Rabatt</h4>
              <ul className="space-y-1 text-muted-foreground">
                <li>• Ab 10 Tage: +1%</li>
                <li>• Ab 15 Tage: +2%</li>
                <li>• Ab 30 Tage: +3%</li>
              </ul>
            </div>
            <div className="space-y-2">
              <h4 className="font-semibold text-primary">Abhol-Rabatt</h4>
              <ul className="space-y-1 text-muted-foreground">
                <li>• +2% bei Selbstabholung</li>
                <li>• Nur ab 5.000€ Bestellwert</li>
                <li>• Abholung am FreshPlan-Standort</li>
              </ul>
            </div>
            <div className="space-y-2">
              <h4 className="font-semibold text-primary">Besondere Konditionen</h4>
              <ul className="space-y-1 text-muted-foreground">
                <li>• Kettenkunden: Individuelle Angebote</li>
                <li>• Maximum: 15% Gesamtrabatt</li>
                <li>• Alle Preise netto</li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </main>
  );
}
