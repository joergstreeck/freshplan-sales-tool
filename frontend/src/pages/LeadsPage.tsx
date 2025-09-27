import LeadList from '../features/leads/LeadList';
import { MainLayoutV2 } from '../components/layout/MainLayoutV2';

export default function LeadsPage() {
  return (
    <MainLayoutV2>
      <LeadList />
    </MainLayoutV2>
  );
}