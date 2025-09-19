import React from 'react';
import { render, screen } from '@testing-library/react';
import ThreadList from '../../src/components/communication/ThreadList';

test('renders loading state', () => {
  // with no fetch mock, component should render 'Lade Threads…' before data
  render(<ThreadList />);
  expect(screen.getByText(/Lade Threads/)).toBeInTheDocument();
});
