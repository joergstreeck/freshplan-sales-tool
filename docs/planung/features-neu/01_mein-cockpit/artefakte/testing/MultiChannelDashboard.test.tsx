// MultiChannelDashboard test – render sanity with typed hooks
import React from 'react';
import { render } from '@testing-library/react';
import MultiChannelDashboard from '../src/components/cockpit/MultiChannelDashboard';

test('renders dashboard component without crashing', () => {
  // NOTE: hooks perform fetch – in a real test, mock fetch; this is a smoke placeholder
  render(<MultiChannelDashboard />);
});
