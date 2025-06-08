/**
 * ⚠️ WICHTIG: DIES IST EINE TEST-SEITE! ⚠️
 *
 * Diese Seite verwendet BEWUSST die alten shadcn Komponenten zum Vergleich.
 * Sie ist NUR für Entwicklung/Testing gedacht.
 *
 * NICHT für Produktion verwenden!
 * Alle produktiven Seiten verwenden die Transition-Komponenten.
 */

import {
  Card as ShadcnCard,
  CardHeader as ShadcnCardHeader,
  CardTitle as ShadcnCardTitle,
  CardDescription as ShadcnCardDescription,
  CardContent as ShadcnCardContent,
} from '../components/ui/card-shadcn'; // ⚠️ ALTER SHADCN CARD - NUR FÜR TEST!
import {
  Card as FreshPlanCard,
  CardHeader as FreshPlanCardHeader,
  CardTitle as FreshPlanCardTitle,
  CardDescription as FreshPlanCardDescription,
  CardContent as FreshPlanCardContent,
} from '../components/ui/card-freshplan';
import '../styles/legacy/components.css';

export function TestCardComparison() {
  return (
    <div style={{ padding: '2rem', background: '#f5f5f5' }}>
      <h2 className="section-title">Card Vergleich: shadcn vs FreshPlan</h2>

      <div
        style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem', marginTop: '2rem' }}
      >
        {/* Shadcn Card */}
        <div>
          <h3 style={{ marginBottom: '1rem', color: '#004F7B' }}>Shadcn (Tailwind)</h3>

          <ShadcnCard>
            <ShadcnCardHeader>
              <ShadcnCardTitle>Card Title</ShadcnCardTitle>
              <ShadcnCardDescription>
                This is a card description with some sample text.
              </ShadcnCardDescription>
            </ShadcnCardHeader>
            <ShadcnCardContent>
              <p>Card content goes here. This is the main body of the card.</p>
            </ShadcnCardContent>
          </ShadcnCard>
        </div>

        {/* FreshPlan Card */}
        <div>
          <h3 style={{ marginBottom: '1rem', color: '#004F7B' }}>FreshPlan (Unser CSS)</h3>

          <FreshPlanCard>
            <FreshPlanCardHeader>
              <FreshPlanCardTitle>Card Title</FreshPlanCardTitle>
              <FreshPlanCardDescription>
                This is a card description with some sample text.
              </FreshPlanCardDescription>
            </FreshPlanCardHeader>
            <FreshPlanCardContent>
              <p>Card content goes here. This is the main body of the card.</p>
            </FreshPlanCardContent>
          </FreshPlanCard>
        </div>
      </div>

      <div
        style={{ marginTop: '2rem', padding: '1rem', background: '#e8f5e9', borderRadius: '8px' }}
      >
        <p>
          <strong>Hinweis:</strong> Die FreshPlan-Card nutzt unsere CI-Farben und hat einen grünen
          Unterstrich beim Titel.
        </p>
      </div>
    </div>
  );
}
