import { useState } from 'react';
import { Button } from '@/components/ui/button-transition';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card-transition';
import { calculatorApi } from '@/features/calculator/calculatorApi';
import type { CalculatorInput } from '@/features/calculator/calculatorSchemas';

export function IntegrationTestPage() {
  const [results, setResults] = useState<
    Record<
      string,
      {
        success: boolean;
        data?: unknown;
        error?: string;
        duration?: number;
      }
    >
  >({});
  const [loading, setLoading] = useState<string | null>(null);

  const testInput: CalculatorInput = {
    orderValue: 25000,
    leadTime: 14,
    pickup: false,
    chain: false,
  };

  const runTest = async (testName: string, testFn: () => Promise<unknown>) => {
    setLoading(testName);
    try {
      const startTime = Date.now();
      const result = await testFn();
      const duration = Date.now() - startTime;

      setResults(prev => ({
        ...prev,
        [testName]: {
          success: true,
          data: result,
          duration,
        },
      }));
    } catch (_error) {
      void _error;
      setResults(prev => ({
        ...prev,
        [testName]: {
          success: false,
          error: error instanceof Error ? error.message : 'Unknown error',
        },
      }));
    } finally {
      setLoading(null);
    }
  };

  const tests = [
    {
      name: 'Calculate Discount',
      fn: () => calculatorApi.calculate(testInput),
    },
    {
      name: 'Get Scenarios',
      fn: () => calculatorApi.getScenarios(),
    },
    {
      name: 'Get Optimal Scenario',
      fn: () => calculatorApi.getScenario('optimal'),
    },
    {
      name: 'Get Discount Rules',
      fn: () => calculatorApi.getDiscountRules(),
    },
  ];

  const runAllTests = async () => {
    setResults({});
    for (const test of tests) {
      await runTest(test.name, test.fn);
    }
  };

  return (
    <main className="min-h-screen bg-background p-8">
      <div className="mx-auto max-w-4xl space-y-8">
        <div className="text-center">
          <h1 className="text-4xl font-bold text-foreground mb-4">
            Calculator API Integration Test
          </h1>
          <p className="text-xl text-muted-foreground">
            Testing connection to backend calculator endpoints
          </p>
        </div>

        <Card>
          <CardHeader>
            <CardTitle>Test Controls</CardTitle>
            <CardDescription>Run integration tests against the Calculator API</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex gap-4">
              <Button onClick={runAllTests} disabled={loading !== null}>
                Run All Tests
              </Button>
              {tests.map(test => (
                <Button
                  key={test.name}
                  variant="outline"
                  onClick={() => runTest(test.name, test.fn)}
                  disabled={loading !== null}
                >
                  {test.name}
                </Button>
              ))}
            </div>

            {/* Test Input Display */}
            <div className="p-4 bg-muted rounded-lg">
              <h4 className="font-semibold mb-2">Test Input:</h4>
              <pre className="text-sm">{JSON.stringify(testInput, null, 2)}</pre>
            </div>
          </CardContent>
        </Card>

        {/* Results */}
        <div className="space-y-4">
          {Object.entries(results).map(([testName, result]) => (
            <Card key={testName}>
              <CardHeader>
                <CardTitle className="flex items-center justify-between">
                  <span>{testName}</span>
                  <span className={`text-sm ${result.success ? 'text-green-600' : 'text-red-600'}`}>
                    {result.success ? '✅ Success' : '❌ Failed'}
                    {result.duration && ` (${result.duration}ms)`}
                  </span>
                </CardTitle>
              </CardHeader>
              <CardContent>
                <pre className="text-sm bg-muted p-4 rounded-lg overflow-auto max-h-96">
                  {result.success ? JSON.stringify(result.data, null, 2) : `Error: ${result.error}`}
                </pre>
              </CardContent>
            </Card>
          ))}
        </div>

        {loading && (
          <div className="fixed bottom-4 right-4 bg-primary text-primary-foreground px-4 py-2 rounded-lg shadow-lg">
            Testing: {loading}...
          </div>
        )}
      </div>
    </main>
  );
}
