import React, { useState } from 'react';
import { useAuth } from '../hooks/useAuth';
import apiClient from '../lib/authenticatedApiClient';
import { Card } from '../components/ui/card';
import { Button } from '../components/ui/button';

/**
 * Security Test Page
 *
 * This page tests the FC-008 Security Foundation implementation.
 * It shows current authentication status and allows testing different endpoints.
 */
export const SecurityTestPage: React.FC = () => {
  const { isAuthenticated, username, email, userRoles, hasRole } = useAuth();
  const [testResults, setTestResults] = useState<
    Record<string, { success: boolean; data?: unknown; error?: string }>
  >({});
  const [loading, setLoading] = useState<Record<string, boolean>>({});

  const testEndpoint = async (endpoint: string, name: string) => {
    setLoading(prev => ({ ...prev, [name]: true }));
    try {
      const response = await apiClient.get(endpoint);
      setTestResults(prev => ({
        ...prev,
        [name]: { success: true, data: response.data },
      }));
    } catch (error: unknown) {
      setTestResults(prev => ({
        ...prev,
        [name]: {
          success: false,
          error: error.response?.status || error.message,
          details: error.response?.data || error,
        },
      }));
    } finally {
      setLoading(prev => ({ ...prev, [name]: false }));
    }
  };

  const endpoints = [
    {
      name: 'Public',
      path: '/api/security-test/public',
      description: 'Sollte ohne Auth funktionieren',
    },
    {
      name: 'Authenticated',
      path: '/api/security-test/authenticated',
      description: 'Benötigt Login',
    },
    { name: 'Admin', path: '/api/security-test/admin', description: 'Benötigt Admin-Rolle' },
    { name: 'Manager', path: '/api/security-test/manager', description: 'Benötigt Manager-Rolle' },
    { name: 'Sales', path: '/api/security-test/sales', description: 'Benötigt Sales-Rolle' },
  ];

  return (
    <main className="container mx-auto p-6">
      <h1 className="text-3xl font-bold mb-8">Security Foundation Test (FC-008)</h1>

      {/* Current User Info */}
      <Card className="mb-8 p-6">
        <h2 className="text-xl font-semibold mb-4">Aktuelle Authentifizierung</h2>
        <div className="space-y-2">
          <p>
            <strong>Authentifiziert:</strong> {isAuthenticated ? 'Ja' : 'Nein'}
          </p>
          <p>
            <strong>Benutzername:</strong> {username || 'N/A'}
          </p>
          <p>
            <strong>Email:</strong> {email || 'N/A'}
          </p>
          <p>
            <strong>Rollen:</strong> {userRoles.length > 0 ? userRoles.join(', ') : 'Keine'}
          </p>
          <div className="mt-4">
            <h3 className="font-semibold mb-2">Rollen-Check:</h3>
            <p>Admin: {hasRole('admin') ? '✅' : '❌'}</p>
            <p>Manager: {hasRole('manager') ? '✅' : '❌'}</p>
            <p>Sales: {hasRole('sales') ? '✅' : '❌'}</p>
          </div>
        </div>
      </Card>

      {/* Endpoint Tests */}
      <Card className="p-6">
        <h2 className="text-xl font-semibold mb-4">Endpoint Tests</h2>
        <div className="space-y-4">
          {endpoints.map(endpoint => (
            <div key={endpoint.name} className="border rounded p-4">
              <div className="flex items-center justify-between mb-2">
                <div>
                  <h3 className="font-semibold">{endpoint.name}</h3>
                  <p className="text-sm text-gray-600">{endpoint.description}</p>
                  <p className="text-xs text-gray-500 font-mono">{endpoint.path}</p>
                </div>
                <Button
                  onClick={() => testEndpoint(endpoint.path, endpoint.name)}
                  disabled={loading[endpoint.name]}
                  size="sm"
                >
                  {loading[endpoint.name] ? 'Teste...' : 'Test'}
                </Button>
              </div>

              {testResults[endpoint.name] && (
                <div
                  className={`mt-2 p-2 rounded text-sm ${
                    testResults[endpoint.name].success ? 'bg-green-50' : 'bg-red-50'
                  }`}
                >
                  {testResults[endpoint.name].success ? (
                    <div>
                      <p className="text-green-700 font-semibold">✅ Erfolgreich</p>
                      <pre className="mt-1 text-xs overflow-auto">
                        {JSON.stringify(testResults[endpoint.name].data, null, 2)}
                      </pre>
                    </div>
                  ) : (
                    <div>
                      <p className="text-red-700 font-semibold">
                        ❌ Fehler: {testResults[endpoint.name].error}
                      </p>
                      <pre className="mt-1 text-xs overflow-auto">
                        {JSON.stringify(testResults[endpoint.name].details, null, 2)}
                      </pre>
                    </div>
                  )}
                </div>
              )}
            </div>
          ))}
        </div>
      </Card>

      {/* Problems Found */}
      <Card className="mt-8 p-6 bg-yellow-50">
        <h2 className="text-xl font-semibold mb-4">Gefundene Probleme</h2>
        <ol className="list-decimal list-inside space-y-2 text-sm">
          <li>pom.xml: quarkus-smallrye-jwt Dependency fehlte</li>
          <li>application.properties: Realm heißt "freshplan-realm" nicht "freshplan"</li>
          <li>SecurityContextProvider: Unterschiedliche Implementierung als in Doku</li>
          <li>UserRepository: Keine findByKeycloakId Methode vorhanden</li>
          <li>User Entity: Kein keycloakId Feld für Keycloak-Verknüpfung</li>
          <li>RealityCheckResource: Alte javax.ws.rs imports statt jakarta.ws.rs</li>
          <li>RealityCheckResource: Path Namenskonflikt mit java.nio.file.Path</li>
          <li>apiClient: TODO für AuthContext Integration noch offen</li>
        </ol>
      </Card>
    </main>
  );
};
