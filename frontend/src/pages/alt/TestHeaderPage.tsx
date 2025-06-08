import { Header } from '../components/original/Header';
import '../styles/legacy/variables.css';
import '../styles/legacy/header-logo.css';

export function TestHeaderPage() {
  const handleLanguageChange = (lang: string) => {
    console.log('Language changed to:', lang);
  };

  const handleClearForm = () => {
    console.log('Clear form clicked');
  };

  const handleSave = () => {
    console.log('Save clicked');
  };

  return (
    <div style={{ minHeight: '100vh', background: '#f5f5f5' }}>
      <Header
        onLanguageChange={handleLanguageChange}
        onClearForm={handleClearForm}
        onSave={handleSave}
      />
      <div style={{ padding: '2rem', textAlign: 'center' }}>
        <h2>Header Test</h2>
        <p>Dies ist eine Test-Seite um den Header zu überprüfen.</p>
      </div>
    </div>
  );
}
