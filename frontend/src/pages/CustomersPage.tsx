import React from 'react';
import { CustomerList } from '@/features/customer/components';
import { AuthenticatedLayout } from '@/components/layout/AuthenticatedLayout';

const CustomersPage: React.FC = () => {
  return (
    <AuthenticatedLayout>
      <div className="page-container">
        <CustomerList />
      </div>
    </AuthenticatedLayout>
  );
};

export default CustomersPage;
