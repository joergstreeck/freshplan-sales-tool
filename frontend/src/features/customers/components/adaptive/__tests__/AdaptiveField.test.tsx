/**
 * Tests für AdaptiveField Component
 */

import { render, screen } from '@testing-library/react';
import { vi } from 'vitest';
import React from 'react';
import { CustomerFieldThemeProvider } from '../../../theme/CustomerFieldThemeProvider';
import { AdaptiveField } from '../AdaptiveField';
import type { FieldDefinition } from '../../../../../types/field.types';

// Helper to render with theme provider
const renderWithTheme = (component: React.ReactElement) => {
  return render(<CustomerFieldThemeProvider>{component}</CustomerFieldThemeProvider>);
};

describe.skip('AdaptiveField', () => {
  const mockField: FieldDefinition = {
    key: 'email',
    label: 'E-Mail',
    fieldType: 'email',
    required: true,
    placeholder: 'test@example.com',
    maxLength: 100,
  };

  const defaultProps = {
    field: mockField,
    value: '',
    onChange: vi.fn(),
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe.skip('Rendering', () => {
    it('should render field with label', () => {
      renderWithTheme(<AdaptiveField {...defaultProps} />);

      expect(screen.getByLabelText('E-Mail')).toBeInTheDocument();
    });

    it('should show placeholder', () => {
      renderWithTheme(<AdaptiveField {...defaultProps} />);

      const input = screen.getByLabelText('E-Mail');
      expect(input).toHaveAttribute('placeholder', 'test@example.com');
    });

    it('should mark required fields', () => {
      renderWithTheme(<AdaptiveField {...defaultProps} />);

      const input = screen.getByLabelText('E-Mail');
      expect(input).toHaveAttribute('aria-required', 'true');
    });
  });

  describe.skip('Dynamic Width', () => {
    it('should grow with content', () => {
      const { rerender } = render(<AdaptiveField {...defaultProps} />);

      // Initial render with empty value
      let input = screen.getByLabelText('E-Mail');
      const initialWidth = window.getComputedStyle(input.parentElement!).width;

      // Rerender with longer value
      rerender(<AdaptiveField {...defaultProps} value="very.long.email.address@example.com" />);

      input = screen.getByLabelText('E-Mail');
      const newWidth = window.getComputedStyle(input.parentElement!).width;

      // Width should have changed (exact values depend on CSS)
      expect(initialWidth).not.toBe(newWidth);
    });

    it('should respect maximum width', () => {
      render(
        <AdaptiveField
          {...defaultProps}
          value="extremely.super.long.email.address.that.exceeds.maximum@example.com"
        />
      );

      const input = screen.getByLabelText('E-Mail');
      const container = input.closest('.field-large');

      expect(container).toHaveStyle({ maxWidth: '500px' });
    });
  });

  describe.skip('Error Handling', () => {
    it('should show error message', () => {
      render(<AdaptiveField {...defaultProps} error="E-Mail ist erforderlich" />);

      expect(screen.getByText('E-Mail ist erforderlich')).toBeInTheDocument();
    });

    it('should maintain layout stability with error', () => {
      const { rerender } = render(<AdaptiveField {...defaultProps} />);

      // Get initial height
      const container = screen.getByLabelText('E-Mail').closest('.MuiBox-root');
      const initialHeight = container!.offsetHeight;

      // Add error
      rerender(<AdaptiveField {...defaultProps} error="E-Mail ist erforderlich" />);

      // Height should remain stable (error space was pre-allocated)
      const newHeight = container!.offsetHeight;
      expect(Math.abs(newHeight - initialHeight)).toBeLessThan(5); // Small tolerance
    });

    it('should set aria-invalid when error exists', () => {
      render(<AdaptiveField {...defaultProps} error="E-Mail ist erforderlich" />);

      const input = screen.getByLabelText('E-Mail');
      expect(input).toHaveAttribute('aria-invalid', 'true');
    });
  });

  describe.skip('User Interaction', () => {
    it('should call onChange when typing', () => {
      render(<AdaptiveField {...defaultProps} />);

      const input = screen.getByLabelText('E-Mail');
      fireEvent.change(input, { target: { value: 'test@test.de' } });

      expect(defaultProps.onChange).toHaveBeenCalledWith('test@test.de');
    });

    it('should call onBlur when provided', () => {
      const onBlur = vi.fn();
      render(<AdaptiveField {...defaultProps} onBlur={onBlur} />);

      const input = screen.getByLabelText('E-Mail');
      fireEvent.blur(input);

      expect(onBlur).toHaveBeenCalled();
    });

    it('should respect maxLength', () => {
      render(<AdaptiveField {...defaultProps} />);

      const input = screen.getByLabelText('E-Mail');
      expect(input).toHaveAttribute('maxLength', '100');
    });
  });

  describe.skip('States', () => {
    it('should handle disabled state', () => {
      render(<AdaptiveField {...defaultProps} disabled />);

      const input = screen.getByLabelText('E-Mail');
      expect(input).toBeDisabled();
    });

    it('should handle readOnly state', () => {
      render(<AdaptiveField {...defaultProps} readOnly />);

      const input = screen.getByLabelText('E-Mail');
      expect(input).toHaveAttribute('readonly');
    });
  });

  describe.skip('Field Types', () => {
    it('should render number field correctly', () => {
      const numberField: FieldDefinition = {
        key: 'age',
        label: 'Alter',
        fieldType: 'number',
        placeholder: '0',
      };

      render(<AdaptiveField field={numberField} value="42" onChange={vi.fn()} />);

      const container = screen.getByLabelText('Alter').closest('.field-medium');
      expect(container).toHaveClass('field-medium'); // Default size
    });

    it('should render select field correctly', () => {
      const selectField: FieldDefinition = {
        key: 'salutation',
        label: 'Anrede',
        fieldType: 'select',
        options: [
          { value: 'herr', label: 'Herr' },
          { value: 'frau', label: 'Frau' },
        ],
      };

      render(<AdaptiveField field={selectField} value="herr" onChange={vi.fn()} />);

      expect(screen.getByLabelText('Anrede')).toBeInTheDocument();
    });
  });
});
