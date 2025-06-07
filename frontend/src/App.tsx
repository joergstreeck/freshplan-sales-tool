import { useState } from 'react';
import { Link } from 'react-router-dom';
import { ApiService } from './services/api';
import { useAuth } from './contexts/AuthContext';
import { Button } from './components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from './components/ui/card';

function App() {
  const [count, setCount] = useState(0);
  const [pingResult, setPingResult] = useState<string>('');
  const { token } = useAuth();

  const handlePing = async () => {
    if (!token) {
      setPingResult('Error: Not authenticated. Please login first.');
      return;
    }

    try {
      const result = await ApiService.ping(token);
      setPingResult(JSON.stringify(result, null, 2));
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'An unknown error occurred';
      setPingResult(`Error: ${errorMessage}`);
    }
  };

  return (
    <div className="min-h-screen bg-background p-8">
      <div className="mx-auto max-w-4xl space-y-8">
        <div className="text-center">
          <h1 className="text-4xl font-bold text-foreground mb-2">FreshPlan 2.0</h1>
          <p className="text-muted-foreground">Sprint 0 - Walking Skeleton</p>
        </div>

        <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
          <Card>
            <CardHeader>
              <CardTitle>Calculator</CardTitle>
              <CardDescription>Rabatt-Kalkulator für Großhandelsbestellungen</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                <p className="text-sm text-muted-foreground">
                  Legacy Migration: 4 Rabatttypen, bis zu 15% Gesamtrabatt
                </p>
                <Button asChild>
                  <Link to="/calculator">Rabatt berechnen</Link>
                </Button>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>User Management</CardTitle>
              <CardDescription>Sprint 1 Feature Implementation</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                <p className="text-sm text-muted-foreground">
                  React Query + React Hook Form + Zod + Zustand
                </p>
                <Button asChild>
                  <Link to="/users">Benutzerverwaltung öffnen</Link>
                </Button>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>API Test</CardTitle>
              <CardDescription>Test Backend Connection</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                <Button onClick={handlePing} variant="secondary">
                  Ping API
                </Button>
                {pingResult && (
                  <pre className="bg-muted p-4 rounded-md text-sm overflow-auto max-h-32">
                    {pingResult}
                  </pre>
                )}
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Counter Test</CardTitle>
              <CardDescription>Test React State Management</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                <p className="text-2xl font-mono">{count}</p>
                <Button onClick={() => setCount(count => count + 1)}>Count is {count}</Button>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}

export default App;
