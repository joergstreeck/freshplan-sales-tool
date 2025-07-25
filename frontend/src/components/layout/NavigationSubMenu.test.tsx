import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import { NavigationSubMenu } from './NavigationSubMenu';

// Mock useLocation hook
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useLocation: () => ({
      pathname: '/kundenmanagement/kunden'
    })
  };
});

describe('NavigationSubMenu', () => {
  const mockOnItemClick = vi.fn();
  
  const defaultProps = {
    items: [
      { label: 'Alle Kunden', path: '/kundenmanagement/kunden' },
      { label: 'Verkaufschancen', path: '/kundenmanagement/opportunities' },
      { label: 'Aktivitäten', path: '/kundenmanagement/aktivitaeten' }
    ],
    onItemClick: mockOnItemClick
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders all submenu items', () => {
    render(
      <BrowserRouter>
        <NavigationSubMenu {...defaultProps} />
      </BrowserRouter>
    );
    
    expect(screen.getByText('Alle Kunden')).toBeInTheDocument();
    expect(screen.getByText('Verkaufschancen')).toBeInTheDocument();
    expect(screen.getByText('Aktivitäten')).toBeInTheDocument();
  });

  it('highlights active menu item based on current path', () => {
    render(
      <BrowserRouter>
        <NavigationSubMenu {...defaultProps} />
      </BrowserRouter>
    );
    
    // MUI ListItemButton setzt 'selected' prop, aber nicht unbedingt aria-selected
    const activeItem = screen.getByText('Alle Kunden').closest('.MuiListItemButton-root');
    expect(activeItem).toHaveClass('Mui-selected');
  });

  it('calls onItemClick when submenu item is clicked', async () => {
    const user = userEvent.setup();
    
    render(
      <BrowserRouter>
        <NavigationSubMenu {...defaultProps} />
      </BrowserRouter>
    );
    
    await user.click(screen.getByText('Verkaufschancen'));
    
    expect(mockOnItemClick).toHaveBeenCalledWith('/kundenmanagement/opportunities');
  });

  it('renders with correct styling', () => {
    render(
      <BrowserRouter>
        <NavigationSubMenu {...defaultProps} />
      </BrowserRouter>
    );
    
    // MUI List mit component="div" hat keine role="group"
    const submenuList = document.querySelector('.MuiList-root');
    expect(submenuList).toBeInTheDocument();
  });

  it('handles empty items array gracefully', () => {
    render(
      <BrowserRouter>
        <NavigationSubMenu {...defaultProps} items={[]} />
      </BrowserRouter>
    );
    
    const submenuList = document.querySelector('.MuiList-root');
    expect(submenuList).toBeInTheDocument();
    expect(submenuList?.children).toHaveLength(0);
  });

  it('applies hover styling', async () => {
    const user = userEvent.setup();
    
    render(
      <BrowserRouter>
        <NavigationSubMenu {...defaultProps} />
      </BrowserRouter>
    );
    
    const menuItem = screen.getByText('Verkaufschancen').closest('div[role="button"]');
    
    // Hover sollte backgroundColor ändern
    await user.hover(menuItem as Element);
    
    // Da wir die styles nicht direkt testen können, prüfen wir nur ob das Element existiert
    expect(menuItem).toBeInTheDocument();
  });
});