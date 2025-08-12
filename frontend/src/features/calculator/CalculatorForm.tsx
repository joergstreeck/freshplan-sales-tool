import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Button } from '@/components/ui/button-transition';
import { Input } from '@/components/ui/input';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card-transition';
import { Label } from '@/components/ui/label';
import { Switch } from '@/components/ui/switch';
import { useCalculatorStore } from './calculatorStore';
import { useCalculateDiscount } from './calculatorQueries';
import { CalculatorInputSchema, type CalculatorInput } from './calculatorSchemas';

export function CalculatorForm() {
  const { currentInput, selectedScenario, setInput, setResult, setIsCalculating, addToHistory } =
    useCalculatorStore();

  const calculateMutation = useCalculateDiscount();

  const form = useForm<CalculatorInput>({
    resolver: zodResolver(CalculatorInputSchema),
    defaultValues: currentInput,
    values: currentInput, // Keep form in sync with store
  });

  const onSubmit = async (data: CalculatorInput) => {
    try {
      setIsCalculating(true);
      const result = await calculateMutation.mutateAsync(data);

      setResult(result);
      addToHistory(data, result, selectedScenario || undefined);
    } catch (_error) { void _error;
      if (import.meta.env.DEV) {
      }
    } finally {
      setIsCalculating(false);
    }
  };

  // Update store when form values change
  const handleInputChange = (field: keyof CalculatorInput, value: number | boolean) => {
    setInput({ [field]: value });
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle>Rabatt-Kalkulator</CardTitle>
        <CardDescription>
          Berechnen Sie Ihren individuellen FreshPlan-Rabatt
          {selectedScenario && (
            <span className="block mt-1 text-primary font-medium">
              Szenario: {selectedScenario}
            </span>
          )}
        </CardDescription>
      </CardHeader>
      <CardContent>
        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
          {/* Bestellwert */}
          <div className="space-y-2">
            <Label htmlFor="orderValue">Bestellwert (€)</Label>
            <Input
              id="orderValue"
              type="number"
              min="0"
              max="1000000"
              step="100"
              placeholder="z.B. 25000"
              {...form.register('orderValue', {
                valueAsNumber: true,
                onChange: e => handleInputChange('orderValue', Number(e.target.value)),
              })}
            />
            {form.formState.errors.orderValue && (
              <p className="text-sm text-destructive">{form.formState.errors.orderValue.message}</p>
            )}
            <p className="text-sm text-muted-foreground">
              Netto-Bestellwert ohne Rabatt (0 - 1.000.000 €)
            </p>
          </div>

          {/* Vorlaufzeit */}
          <div className="space-y-2">
            <Label htmlFor="leadTime">Vorlaufzeit (Tage)</Label>
            <Input
              id="leadTime"
              type="number"
              min="0"
              max="365"
              step="1"
              placeholder="z.B. 14"
              {...form.register('leadTime', {
                valueAsNumber: true,
                onChange: e => handleInputChange('leadTime', Number(e.target.value)),
              })}
            />
            {form.formState.errors.leadTime && (
              <p className="text-sm text-destructive">{form.formState.errors.leadTime.message}</p>
            )}
            <p className="text-sm text-muted-foreground">
              Tage zwischen Bestellung und gewünschter Lieferung (0 - 365)
            </p>
          </div>

          {/* Abholung */}
          <div className="flex items-center justify-between space-x-2">
            <div className="space-y-1">
              <Label htmlFor="pickup">Selbstabholung</Label>
              <p className="text-sm text-muted-foreground">
                Abholung am FreshPlan-Standort (+2% Rabatt ab 5.000€)
              </p>
            </div>
            <Switch
              id="pickup"
              checked={form.watch('pickup')}
              onCheckedChange={checked => {
                form.setValue('pickup', checked);
                handleInputChange('pickup', checked);
              }}
            />
          </div>

          {/* Kettenkunde */}
          <div className="flex items-center justify-between space-x-2">
            <div className="space-y-1">
              <Label htmlFor="chain">Kettenkunde</Label>
              <p className="text-sm text-muted-foreground">
                Mehrere Standorte oder Filialen (andere Vorteile verfügbar)
              </p>
            </div>
            <Switch
              id="chain"
              checked={form.watch('chain')}
              onCheckedChange={checked => {
                form.setValue('chain', checked);
                handleInputChange('chain', checked);
              }}
            />
          </div>

          {/* Calculate Button */}
          <Button type="submit" className="w-full" disabled={calculateMutation.isPending} size="lg">
            {calculateMutation.isPending ? (
              <>
                <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2" />
                Berechnung läuft...
              </>
            ) : (
              'Rabatt berechnen'
            )}
          </Button>

          {/* Error Display */}
          {calculateMutation.isError && (
            <div className="text-sm text-destructive bg-destructive/10 p-3 rounded-md">
              Fehler bei der Berechnung: {calculateMutation.error?.message}
            </div>
          )}
        </form>
      </CardContent>
    </Card>
  );
}
