// Transition version - uses FreshPlan styles but keeps the same API as shadcn
import * as React from 'react';
import { Slot } from '@radix-ui/react-slot';
import '../../styles/legacy/forms.css';

// Keep the original ButtonProps interface
export interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  asChild?: boolean;
  variant?: 'default' | 'destructive' | 'outline' | 'secondary' | 'ghost' | 'link';
  size?: 'default' | 'sm' | 'lg' | 'icon';
}

const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant, size, asChild = false, ...props }, ref) => {
    const Comp = asChild ? Slot : 'button';

    // Map shadcn variants to our CSS classes
    const variantClasses: Record<string, string> = {
      default: 'btn btn-primary',
      destructive: 'btn btn-primary',
      outline: 'btn btn-secondary',
      secondary: 'btn btn-secondary',
      ghost: 'btn btn-ghost',
      link: 'btn btn-ghost',
    };

    const sizeClasses: Record<string, string> = {
      default: '',
      sm: 'btn-sm',
      lg: 'btn-lg',
      icon: 'btn-sm',
    };

    const variantClass = variantClasses[variant || 'default'] || 'btn btn-primary';
    const sizeClass = sizeClasses[size || 'default'] || '';

    const classes = [variantClass, sizeClass, className].filter(Boolean).join(' ');

    return <Comp className={classes} ref={ref} {...props} />;
  }
);
Button.displayName = 'Button';

// Export buttonVariants as empty function to maintain compatibility
export const buttonVariants = () => '';

export { Button, ButtonProps };
