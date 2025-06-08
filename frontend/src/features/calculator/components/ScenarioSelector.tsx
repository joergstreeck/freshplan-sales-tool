import { Button } from '@/components/ui/button-transition';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card-transition';
import { useCalculatorStore } from '../store/calculatorStore';
import { PREDEFINED_SCENARIOS, SCENARIO_DESCRIPTIONS } from '../api/calculatorSchemas';
import { formatCurrency, formatDays } from '@/utils/formatting';

export function ScenarioSelector() {
  const { selectedScenario, loadScenario, resetCalculator } = useCalculatorStore();

  const handleScenarioSelect = (scenarioName: string) => {
    const scenarioInput = PREDEFINED_SCENARIOS[scenarioName];
    if (scenarioInput) {
      loadScenario(scenarioInput, scenarioName);
    }
  };

  const scenarios = Object.entries(PREDEFINED_SCENARIOS);

  return (
    <Card>
      <CardHeader>
        <CardTitle>Vordefinierte Szenarien</CardTitle>
        <CardDescription>
          Wählen Sie ein typisches Bestellszenario oder passen Sie die Werte manuell an
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
          {scenarios.map(([name, scenario]) => (
            <ScenarioCard
              key={name}
              name={name}
              scenario={scenario}
              description={SCENARIO_DESCRIPTIONS[name]}
              isSelected={selectedScenario === name}
              onSelect={() => handleScenarioSelect(name)}
            />
          ))}
        </div>

        {/* Reset Button */}
        {selectedScenario && (
          <div className="pt-4 border-t">
            <Button variant="outline" onClick={resetCalculator} className="w-full">
              Eigene Werte eingeben
            </Button>
          </div>
        )}
      </CardContent>
    </Card>
  );
}

interface ScenarioCardProps {
  name: string;
  scenario: (typeof PREDEFINED_SCENARIOS)[keyof typeof PREDEFINED_SCENARIOS];
  description: string;
  isSelected: boolean;
  onSelect: () => void;
}

function ScenarioCard({ name, scenario, description, isSelected, onSelect }: ScenarioCardProps) {
  const displayName = name.charAt(0).toUpperCase() + name.slice(1);

  return (
    <div
      className={`p-4 rounded-lg border-2 cursor-pointer transition-all hover:border-primary/50 ${
        isSelected ? 'border-primary bg-primary/5' : 'border-border bg-card hover:bg-muted/50'
      }`}
      onClick={onSelect}
    >
      <div className="space-y-3">
        <div className="flex items-center justify-between">
          <h4 className={`font-semibold ${isSelected ? 'text-primary' : ''}`}>{displayName}</h4>
          {isSelected && (
            <span className="text-xs px-2 py-1 bg-primary text-primary-foreground rounded-full">
              Aktiv
            </span>
          )}
        </div>

        <p className="text-sm text-muted-foreground">{description}</p>

        <div className="space-y-1 text-xs">
          <div className="flex justify-between">
            <span className="text-muted-foreground">Bestellwert:</span>
            <span className="font-medium">{formatCurrency(scenario.orderValue)}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-muted-foreground">Vorlaufzeit:</span>
            <span className="font-medium">{formatDays(scenario.leadTime)}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-muted-foreground">Abholung:</span>
            <span className="font-medium">{scenario.pickup ? 'Ja' : 'Nein'}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-muted-foreground">Kettenkunde:</span>
            <span className="font-medium">{scenario.chain ? 'Ja' : 'Nein'}</span>
          </div>
        </div>
      </div>
    </div>
  );
}
