/**
 * Tests für LoginPage Komponente
 */

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { LoginPage } from './LoginPage';

// Mock für useKeycloak Hook
const mockLogin = vi.fn();
vi.mock('../../contexts/KeycloakContext', () => ({
  useKeycloak: () => ({
    login: mockLogin,
  }),
}));

// Mock für Button-Komponente
vi.mock('../ui/button', () => ({
  Button: ({ children, onClick, className }: any) => (
    <button onClick={onClick} className={className}>
      {children}
    </button>
  ),
}));

// Mock für Konstanten
vi.mock('../../lib/constants', () => ({
  IS_DEV_MODE: true, // Default auf true für Tests
}));

describe('LoginPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('sollte die Login-Seite korrekt rendern', () => {
    render(<LoginPage />);

    // Titel und Beschreibung prüfen
    expect(screen.getByText('FreshPlan Sales Cockpit')).toBeInTheDocument();
    expect(
      screen.getByText('Melden Sie sich an, um auf Ihr Sales Command Center zuzugreifen')
    ).toBeInTheDocument();

    // Sichere Anmeldung Bereich
    expect(screen.getByText('Sichere Anmeldung')).toBeInTheDocument();
    expect(screen.getByText('Verwenden Sie Ihre FreshPlan-Anmeldedaten')).toBeInTheDocument();

    // Login-Button
    expect(screen.getByText('Mit Keycloak anmelden')).toBeInTheDocument();
  });

  it('sollte Test-Benutzer-Informationen in Dev-Mode anzeigen', () => {
    render(<LoginPage />);

    // Test-Benutzer Überschrift (mit angepasstem Text)
    expect(screen.getByText('Test-Benutzer (nur in Entwicklung):')).toBeInTheDocument();

    // Test-Benutzer Details
    expect(screen.getByText('• sales@freshplan.de / sales123')).toBeInTheDocument();
    expect(screen.getByText('• manager@freshplan.de / manager123')).toBeInTheDocument();
    expect(screen.getByText('• admin@freshplan.de / admin123')).toBeInTheDocument();
  });

  it('sollte login Funktion beim Button-Klick aufrufen', () => {
    render(<LoginPage />);

    const loginButton = screen.getByText('Mit Keycloak anmelden');
    fireEvent.click(loginButton);

    expect(mockLogin).toHaveBeenCalledTimes(1);
  });

  it('sollte richtige CSS-Klassen für Styling haben', () => {
    render(<LoginPage />);

    // Container sollte min-height und gradient haben
    const container = screen.getByText('FreshPlan Sales Cockpit').closest('.min-h-screen');
    expect(container).toHaveClass(
      'min-h-screen',
      'bg-gradient-to-br',
      'from-blue-50',
      'to-indigo-100'
    );

    // Login-Button sollte die richtigen Styles haben
    const loginButton = screen.getByText('Mit Keycloak anmelden');
    expect(loginButton).toHaveClass('w-full', 'bg-indigo-600', 'hover:bg-indigo-700');
  });

  it('sollte das Rocket-Icon anzeigen', () => {
    render(<LoginPage />);

    // Das Rocket-Emoji sollte vorhanden sein
    expect(screen.getByText('🚀')).toBeInTheDocument();
  });

  it('sollte eine strukturierte Layout-Hierarchie haben', () => {
    const { container } = render(<LoginPage />);

    // Prüfe die Struktur
    const mainContainer = container.querySelector('.min-h-screen');
    expect(mainContainer).toBeInTheDocument();

    const contentBox = container.querySelector('.bg-white.shadow-md');
    expect(contentBox).toBeInTheDocument();

    const testUserSection = container.querySelector('.border-t.border-gray-200');
    expect(testUserSection).toBeInTheDocument();
  });

  it('sollte responsive max-width haben', () => {
    const { container } = render(<LoginPage />);

    const contentWrapper = container.querySelector('.max-w-md');
    expect(contentWrapper).toBeInTheDocument();
    expect(contentWrapper).toHaveClass('w-full');
  });

  it('sollte Test-Benutzer-Informationen in Production-Mode NICHT anzeigen', async () => {
    // Temporarily override the mock to simulate production mode
    vi.doUnmock('../../lib/constants');
    vi.doMock('../../lib/constants', () => ({
      IS_DEV_MODE: false,
    }));
    
    // Clean module cache and re-import
    vi.resetModules();
    const { LoginPage: LoginPageProd } = await import('./LoginPage');
    
    render(<LoginPageProd />);

    // Test-Benutzer sollten NICHT angezeigt werden
    expect(screen.queryByText('Test-Benutzer (nur in Entwicklung):')).not.toBeInTheDocument();
    expect(screen.queryByText('• sales@freshplan.de / sales123')).not.toBeInTheDocument();
    
    // Login-Button sollte aber noch da sein
    expect(screen.getByText('Mit Keycloak anmelden')).toBeInTheDocument();
    
    // Reset mock for other tests
    vi.doUnmock('../../lib/constants');
    vi.doMock('../../lib/constants', () => ({
      IS_DEV_MODE: true,
    }));
  });
});
