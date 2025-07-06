/**
 * Login Page Komponente
 */
import React from 'react';
import { useKeycloak } from '../../contexts/KeycloakContext';
import { Button } from '../ui/button';
import { IS_DEV_MODE } from '../../lib/constants';

export const LoginPage: React.FC = () => {
  const { login } = useKeycloak();

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center">
      <div className="max-w-md w-full space-y-8 p-8">
        <div className="text-center">
          <div className="mx-auto h-12 w-12 text-indigo-600 text-4xl">🚀</div>
          <h2 className="mt-6 text-3xl font-bold text-gray-900">FreshPlan Sales Cockpit</h2>
          <p className="mt-2 text-sm text-gray-600">
            Melden Sie sich an, um auf Ihr Sales Command Center zuzugreifen
          </p>
        </div>

        <div className="mt-8 space-y-6">
          <div className="bg-white shadow-md rounded-lg p-6">
            <div className="space-y-4">
              <div className="text-center">
                <h3 className="text-lg font-medium text-gray-900">Sichere Anmeldung</h3>
                <p className="mt-2 text-sm text-gray-500">
                  Verwenden Sie Ihre FreshPlan-Anmeldedaten
                </p>
              </div>

              <Button
                onClick={login}
                className="w-full bg-indigo-600 hover:bg-indigo-700 text-white font-medium py-2 px-4 rounded-md transition-colors"
              >
                Mit Keycloak anmelden
              </Button>

              {IS_DEV_MODE && (
                <div className="mt-4 pt-4 border-t border-gray-200">
                  <div className="text-xs text-gray-500 space-y-1">
                    <p>
                      <strong>Test-Benutzer (nur in Entwicklung):</strong>
                    </p>
                    <p>• sales@freshplan.de / sales123</p>
                    <p>• manager@freshplan.de / manager123</p>
                    <p>• admin@freshplan.de / admin123</p>
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
