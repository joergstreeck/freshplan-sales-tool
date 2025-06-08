import { Link } from 'react-router-dom';
import '../styles/legacy/components.css';
import '../styles/legacy/typography.css';

export function HomePage() {
  return (
    <div style={{ padding: '2rem', maxWidth: '800px', margin: '0 auto' }}>
      <h1 className="heading" style={{ marginBottom: '2rem' }}>FreshPlan Test-Seiten</h1>
      
      <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
        <h2 style={{ color: '#004F7B', marginTop: '2rem' }}>Komponenten-Vergleiche</h2>
        
        <Link to="/test-button-comparison" className="btn btn-secondary" style={{ textAlign: 'center' }}>
          Button Vergleich (shadcn vs FreshPlan)
        </Link>
        
        <Link to="/test-card-comparison" className="btn btn-secondary" style={{ textAlign: 'center' }}>
          Card Vergleich (shadcn vs FreshPlan)
        </Link>
        
        <h2 style={{ color: '#004F7B', marginTop: '2rem' }}>Legacy Komponenten</h2>
        
        <Link to="/test-header" className="btn btn-secondary" style={{ textAlign: 'center' }}>
          Test Header
        </Link>
        
        <Link to="/test-header-nav" className="btn btn-secondary" style={{ textAlign: 'center' }}>
          Test Header mit Navigation
        </Link>
        
        <Link to="/test-calculator-layout" className="btn btn-secondary" style={{ textAlign: 'center' }}>
          Test Calculator Layout
        </Link>
        
        <Link to="/original-calculator" className="btn btn-secondary" style={{ textAlign: 'center' }}>
          Original Calculator
        </Link>
        
        <Link to="/legacy-tool" className="btn btn-secondary" style={{ textAlign: 'center' }}>
          Legacy Tool (Komplett)
        </Link>
        
        <h2 style={{ color: '#004F7B', marginTop: '2rem' }}>Neue Features</h2>
        
        <Link to="/calculator" className="btn btn-primary" style={{ textAlign: 'center' }}>
          Calculator (Neue Version)
        </Link>
        
        <Link to="/users" className="btn btn-primary" style={{ textAlign: 'center' }}>
          User Management
        </Link>
      </div>
    </div>
  );
}