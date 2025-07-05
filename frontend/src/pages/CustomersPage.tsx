import React from 'react';
import { CustomerList } from '@/features/customer/components';

const CustomersPage: React.FC = () => {
  return (
    <div className="page-container">
      <CustomerList />
    </div>
  );
};

export default CustomersPage;