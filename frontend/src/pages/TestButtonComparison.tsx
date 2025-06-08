import { Button as ShadcnButton } from '../components/ui/button';
import { Button as FreshPlanButton } from '../components/ui/button-freshplan';
import '../styles/legacy/forms.css';

export function TestButtonComparison() {
  return (
    <div style={{ padding: '2rem', background: '#f5f5f5' }}>
      <h2 className="section-title">Button Vergleich: shadcn vs FreshPlan</h2>
      
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem', marginTop: '2rem' }}>
        {/* Shadcn Buttons */}
        <div style={{ background: 'white', padding: '2rem', borderRadius: '12px' }}>
          <h3 style={{ marginBottom: '1rem', color: '#004F7B' }}>Shadcn (Tailwind)</h3>
          
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            <ShadcnButton>Primary Default</ShadcnButton>
            <ShadcnButton variant="secondary">Secondary</ShadcnButton>
            <ShadcnButton variant="outline">Outline</ShadcnButton>
            <ShadcnButton variant="ghost">Ghost</ShadcnButton>
            <ShadcnButton variant="destructive">Destructive</ShadcnButton>
            <ShadcnButton size="sm">Small</ShadcnButton>
            <ShadcnButton size="lg">Large</ShadcnButton>
            <ShadcnButton disabled>Disabled</ShadcnButton>
          </div>
        </div>

        {/* FreshPlan Buttons */}
        <div style={{ background: 'white', padding: '2rem', borderRadius: '12px' }}>
          <h3 style={{ marginBottom: '1rem', color: '#004F7B' }}>FreshPlan (Unser CSS)</h3>
          
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            <FreshPlanButton>Primary Default</FreshPlanButton>
            <FreshPlanButton variant="secondary">Secondary</FreshPlanButton>
            <FreshPlanButton variant="ghost">Ghost</FreshPlanButton>
            <FreshPlanButton size="sm">Small</FreshPlanButton>
            <FreshPlanButton size="lg">Large</FreshPlanButton>
            <FreshPlanButton disabled>Disabled</FreshPlanButton>
          </div>
        </div>
      </div>

      <div style={{ marginTop: '2rem', padding: '1rem', background: '#e8f5e9', borderRadius: '8px' }}>
        <p><strong>Hinweis:</strong> Die FreshPlan-Buttons nutzen unsere CI-Farben (#94C456 für Grün, #004F7B für Blau)</p>
      </div>
    </div>
  );
}