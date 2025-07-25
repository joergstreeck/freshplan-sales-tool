import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { NavigationSubMenu } from './NavigationSubMenu';
import '@testing-library/jest-dom';

describe('NavigationSubMenu', () => {
  const mockItems = [
    {
      label: 'Alle Kunden',
      path: '/kundenmanagement/kunden',
    },
    {
      label: 'Verkaufschancen',
      path: '/kundenmanagement/opportunities',
    },
    {
      label: 'Aktivitäten',
      path: '/kundenmanagement/aktivitaeten',
    },
  ];

  const mockOnItemClick = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders all sub-items', () => {
    render(
      <MemoryRouter>
        <NavigationSubMenu items={mockItems} onItemClick={mockOnItemClick} />
      </MemoryRouter>
    );
    
    expect(screen.getByText('Alle Kunden')).toBeInTheDocument();
    expect(screen.getByText('Verkaufschancen')).toBeInTheDocument();
    expect(screen.getByText('Aktivitäten')).toBeInTheDocument();
  });

  it('calls onItemClick with correct path when item is clicked', () => {
    render(
      <MemoryRouter>
        <NavigationSubMenu items={mockItems} onItemClick={mockOnItemClick} />
      </MemoryRouter>
    );
    
    const verkaufschancen = screen.getByText('Verkaufschancen');
    fireEvent.click(verkaufschancen);
    
    expect(mockOnItemClick).toHaveBeenCalledWith('/kundenmanagement/opportunities');
  });

  it('highlights active sub-item based on current path', () => {
    render(
      <MemoryRouter initialEntries={['/kundenmanagement/opportunities']}>
        <NavigationSubMenu items={mockItems} onItemClick={mockOnItemClick} />
      </MemoryRouter>
    );
    
    const verkaufschancenButton = screen.getByText('Verkaufschancen').closest('div[role="button"]');
    expect(verkaufschancenButton).toHaveClass('Mui-selected');
    
    // Others should not be selected
    const alleKundenButton = screen.getByText('Alle Kunden').closest('div[role="button"]');
    expect(alleKundenButton).not.toHaveClass('Mui-selected');
  });

  it('applies correct styling to active item', () => {
    render(
      <MemoryRouter initialEntries={['/kundenmanagement/opportunities']}>
        <NavigationSubMenu items={mockItems} onItemClick={mockOnItemClick} />
      </MemoryRouter>
    );
    
    const verkaufschancenText = screen.getByText('Verkaufschancen');
    const computedStyle = window.getComputedStyle(verkaufschancenText);
    
    // Active item should have different styling
    expect(verkaufschancenText).toHaveStyle({
      fontWeight: '600',
      color: '#94C456',
    });
  });

  it('renders with correct indentation', () => {
    const { container } = render(
      <MemoryRouter>
        <NavigationSubMenu items={mockItems} onItemClick={mockOnItemClick} />
      </MemoryRouter>
    );
    
    const buttons = container.querySelectorAll('div[role="button"]');
    buttons.forEach(button => {
      // Check for left padding (pl: 7 = 28px in MUI)
      expect(button).toHaveStyle({
        paddingLeft: '56px', // 7 * 8px
      });
    });
  });

  it('shows bullet points for all items', () => {
    const { container } = render(
      <MemoryRouter>
        <NavigationSubMenu items={mockItems} onItemClick={mockOnItemClick} />
      </MemoryRouter>
    );
    
    // Check for pseudo-element bullets
    const buttons = container.querySelectorAll('div[role="button"]');
    expect(buttons.length).toBe(3);
    
    // Each button should have the bullet point styling
    buttons.forEach(button => {
      expect(button).toHaveStyle({
        position: 'relative',
      });
    });
  });

  it('handles empty items array', () => {
    const { container } = render(
      <MemoryRouter>
        <NavigationSubMenu items={[]} onItemClick={mockOnItemClick} />
      </MemoryRouter>
    );
    
    // Should render empty list without errors
    const list = container.querySelector('.MuiList-root');
    expect(list).toBeInTheDocument();
    expect(list.children.length).toBe(0);
  });

  it('handles items with permissions', () => {
    const itemsWithPermissions = [
      ...mockItems,
      {
        label: 'Admin Only',
        path: '/admin',
        permissions: ['admin'],
      },
    ];
    
    render(
      <MemoryRouter>
        <NavigationSubMenu items={itemsWithPermissions} onItemClick={mockOnItemClick} />
      </MemoryRouter>
    );
    
    // All items should be rendered (permission filtering happens in parent)
    expect(screen.getByText('Admin Only')).toBeInTheDocument();
  });
});