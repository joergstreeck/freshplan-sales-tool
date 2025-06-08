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
    <div style={{ minHeight: '100vh', background: '#f5f5f5', padding: '2rem' }}>
      <div style={{ maxWidth: '1024px', margin: '0 auto' }}>
        <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
          <h1 style={{ fontSize: '2.5rem', fontWeight: 'bold', marginBottom: '0.5rem' }}>FreshPlan 2.0</h1>
          <p style={{ color: '#666' }}>Sprint 0 - Walking Skeleton</p>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '1.5rem' }}>
          <Card>
            <CardHeader>
              <CardTitle>FreshPlan Sales Tool</CardTitle>
              <CardDescription>Das komplette Verkaufstool mit allen Features</CardDescription>
            </CardHeader>
            <CardContent>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                <p style={{ fontSize: '0.875rem', color: '#666' }}>
                  Kundendaten, Standorte, Kalkulator - Alles in einem Tool
                </p>
                <Button asChild>
                  <Link to="/legacy-tool">FreshPlan Tool öffnen</Link>
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
              <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                <p style={{ fontSize: '0.875rem', color: '#666' }}>
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
              <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                <Button onClick={handlePing} variant="secondary">
                  Ping API
                </Button>
                {pingResult && (
                  <pre style={{ background: '#e0e0e0', padding: '1rem', borderRadius: '0.5rem', fontSize: '0.875rem', overflow: 'auto', maxHeight: '8rem' }}>
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
              <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                <p style={{ fontSize: '1.5rem', fontFamily: 'monospace' }}>{count}</p>
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
