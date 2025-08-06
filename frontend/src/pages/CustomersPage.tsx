import React from 'react';
import { CustomerList } from '@/features/customer/components';
import { MainLayoutV2 } from '@/components/layout/MainLayoutV2';

const CustomersPage: React.FC = () => {
  return (
    <MainLayoutV2>
      <CustomerList />
    </MainLayoutV2>
  );
};

export default CustomersPage;
