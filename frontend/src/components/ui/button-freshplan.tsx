import * as React from 'react';
import { Slot } from '@radix-ui/react-slot';
import '../../styles/legacy/forms.css';

export interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'default' | 'secondary' | 'ghost' | 'destructive' | 'outline' | 'link';
  size?: 'default' | 'sm' | 'lg' | 'icon';
  asChild?: boolean;
}

const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className = '', variant = 'default', size = 'default', asChild = false, children, ...props }, ref) => {
    const Comp = asChild ? Slot : 'button';
    
    // Map shadcn variants to our CSS classes
    const variantClasses: Record<string, string> = {
      default: 'btn btn-primary',
      secondary: 'btn btn-secondary', 
      ghost: 'btn btn-ghost',
      destructive: 'btn btn-primary', // Use primary for destructive
      outline: 'btn btn-secondary',   // Use secondary for outline
      link: 'btn btn-ghost'           // Use ghost for link
    };

    // Size modifiers
    const sizeClasses: Record<string, string> = {
      default: '',
      sm: 'btn-sm',
      lg: 'btn-lg',
      icon: 'btn-sm'  // Use small for icon
    };

    const classes = [
      variantClasses[variant],
      sizeClasses[size],
      className
    ].filter(Boolean).join(' ');

    return (
      <Comp
        className={classes}
        ref={ref}
        {...props}
      >
        {children}
      </Comp>
    );
  }
);

Button.displayName = 'Button';

export { Button };