import { Header } from '../components/original/Header';
import { Navigation } from '../components/original/Navigation';
import { CalculatorLayout } from '../components/original/CalculatorLayout';
import '../styles/legacy/variables.css';
import '../styles/legacy/header-logo.css';
import '../styles/legacy/calculator.css';

export function TestCalculatorLayoutPage() {
  const handleLanguageChange = (lang: string) => {
    console.log('Language changed to:', lang);
  };

  const handleClearForm = () => {
    console.log('Clear form clicked');
  };

  const handleSave = () => {
    console.log('Save clicked');
  };

  const handleTabChange = (tab: string) => {
    console.log('Tab changed to:', tab);
  };

  return (
    <div style={{ minHeight: '100vh', background: '#f5f7fa' }}>
      <Header
        onLanguageChange={handleLanguageChange}
        onClearForm={handleClearForm}
        onSave={handleSave}
      />
      <Navigation activeTab="calculator" onTabChange={handleTabChange} />
      <CalculatorLayout />
    </div>
  );
}
