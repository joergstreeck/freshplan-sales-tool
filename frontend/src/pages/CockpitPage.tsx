/**
 * Cockpit Page with MainLayoutV2
 * Updated to use the new standard layout for consistency
 */
import { MainLayoutV2 } from '../components/layout/MainLayoutV2';
import { SalesCockpitV2 } from '../features/cockpit/components/SalesCockpitV2';

export function CockpitPage() {
  return (
    <MainLayoutV2 maxWidth="full">
      <SalesCockpitV2 />
    </MainLayoutV2>
  );
}
