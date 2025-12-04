/**
 * Cockpit Page with MainLayoutV2
 * Updated to use the new standard layout for consistency
 *
 * Note: SalesCockpitV2 contains the h1 heading for WCAG compliance
 */
import { MainLayoutV2 } from '../components/layout/MainLayoutV2';
import { SalesCockpitV2 } from '../features/cockpit/components/SalesCockpitV2';

export function CockpitPage() {
  // SalesCockpitV2 provides the h1 heading (component="h1")
  return (
    <MainLayoutV2 maxWidth="full">
      <SalesCockpitV2 />
    </MainLayoutV2>
  );
}
