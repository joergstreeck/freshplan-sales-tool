import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { Input } from './input';

describe('Input', () => {
  it('renders with default classes', () => {
    render(<Input data-testid="input" />);

    const input = screen.getByTestId('input');
    expect(input).toBeInTheDocument();
    expect(input).toHaveClass(
      'flex',
      'h-9',
      'w-full',
      'rounded-md',
      'border',
      'border-input',
      'bg-transparent',
      'px-3',
      'py-1',
      'text-sm',
      'shadow-sm'
    );
  });

  it('forwards all HTML input props', () => {
    render(<Input type="email" placeholder="Enter email" name="email" />);

    const input = screen.getByRole('textbox');
    expect(input).toHaveAttribute('type', 'email');
    expect(input).toHaveAttribute('placeholder', 'Enter email');
    expect(input).toHaveAttribute('name', 'email');
  });

  it('handles value changes', () => {
    const handleChange = vi.fn();
    render(<Input onChange={handleChange} />);

    const input = screen.getByRole('textbox');
    fireEvent.change(input, { target: { value: 'test value' } });

    expect(handleChange).toHaveBeenCalled();
  });

  it('applies custom className', () => {
    render(<Input className="custom-class" data-testid="input" />);

    const input = screen.getByTestId('input');
    expect(input).toHaveClass('custom-class');
  });

  it('is disabled when disabled prop is true', () => {
    render(<Input disabled />);

    const input = screen.getByRole('textbox');
    expect(input).toBeDisabled();
  });

  it('forwards ref properly', () => {
    const ref = vi.fn();
    render(<Input ref={ref} />);

    expect(ref).toHaveBeenCalled();
  });

  it('handles different input types', () => {
    const { rerender } = render(<Input type="password" data-testid="input" />);
    expect(screen.getByTestId('input')).toHaveAttribute('type', 'password');

    rerender(<Input type="number" data-testid="input" />);
    expect(screen.getByTestId('input')).toHaveAttribute('type', 'number');

    rerender(<Input type="email" data-testid="input" />);
    expect(screen.getByTestId('input')).toHaveAttribute('type', 'email');
  });
});
