import * as React from 'react';
import '../../styles/legacy/forms.css';

export interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'ghost';
  size?: 'default' | 'sm' | 'lg';
  asChild?: boolean;
}

const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className = '', variant = 'primary', size = 'default', children, ...props }, ref) => {
    // Map variants to our CSS classes
    const variantClasses = {
      primary: 'btn btn-primary',
      secondary: 'btn btn-secondary',
      ghost: 'btn btn-ghost'
    };

    // Size modifiers (we'll add these to our CSS if needed)
    const sizeClasses = {
      default: '',
      sm: 'btn-sm',
      lg: 'btn-lg'
    };

    const classes = [
      variantClasses[variant],
      sizeClasses[size],
      className
    ].filter(Boolean).join(' ');

    return (
      <button
        className={classes}
        ref={ref}
        {...props}
      >
        {children}
      </button>
    );
  }
);

Button.displayName = 'Button';

export { Button };