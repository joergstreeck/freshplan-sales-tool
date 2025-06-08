import { useState } from 'react';
import { Link } from 'react-router-dom';
import { ApiService } from './services/api';
import { useAuth } from './contexts/AuthContext';
import { Button } from './components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from './components/ui/card';
import './styles/legacy/typography.css';
import './styles/legacy/layout.css';
import './styles/app.css';

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
    <div className="app-container">
      <div className="content-wrapper">
        <div style={{ textAlign: 'center', marginBottom: '3rem' }}>
          <h1 className="main-title">FreshPlan 2.0</h1>
          <p className="subtitle">Ihr digitales Verkaufstool</p>
        </div>

        <div className="card-grid">
          <Card>
            <CardHeader>
              <CardTitle>FreshPlan Verkaufstool</CardTitle>
              <CardDescription>Komplette Verwaltung für Kunden und Angebote</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="card-actions">
                <p className="card-text">
                  Kundendaten, Standorte, Kalkulator - Alles in einem Tool
                </p>
                <div className="card-button-wrapper">
                  <Button asChild>
                    <Link to="/legacy-tool">FreshPlan Tool öffnen</Link>
                  </Button>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Benutzerverwaltung</CardTitle>
              <CardDescription>Verwalten Sie Benutzer und Zugriffsrechte</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="card-actions">
                <p className="card-text">
                  Moderne Benutzerverwaltung mit Rollen und Rechten
                </p>
                <div className="card-button-wrapper">
                  <Button asChild>
                    <Link to="/users">Benutzerverwaltung öffnen</Link>
                  </Button>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>API Verbindungstest</CardTitle>
              <CardDescription>Prüfen Sie die Backend-Verbindung</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="card-actions">
                <p className="card-text">
                  Testen Sie die Verbindung zum Backend-Server
                </p>
                <div className="card-button-wrapper">
                  <Button onClick={handlePing}>
                    Verbindung testen
                  </Button>
                  {pingResult && (
                    <pre className="code-block" style={{ marginTop: 'var(--spacing-md)' }}>
                      {pingResult}
                    </pre>
                  )}
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Zähler Demo</CardTitle>
              <CardDescription>Einfache Demonstration der Interaktivität</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="card-actions">
                <div className="counter-display">Zählerstand: {count}</div>
                <div className="card-button-wrapper">
                  <Button onClick={() => setCount(count => count + 1)}>Zähler erhöhen</Button>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}

export default App;
