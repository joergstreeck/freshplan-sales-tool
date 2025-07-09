/**
 * Cockpit Page with Authenticated Layout - DEBUG VERSION
 */
import { AuthenticatedLayout } from '../components/layout/AuthenticatedLayout';

export function CockpitPage() {
  console.log('CockpitPage: Rendering...');
  
  return (
    <AuthenticatedLayout>
      <div style={{ padding: '20px', backgroundColor: 'white', minHeight: '100vh' }}>
        <h1>🎯 Sales Cockpit - DEBUG</h1>
        <p>Wenn du diese Nachricht siehst, funktioniert das Grundgerüst!</p>
        <p>Sidebar sollte links sichtbar sein.</p>
        <p>Theme: Freshfoodz CI sollte aktiv sein.</p>
      </div>
    </AuthenticatedLayout>
  );
}