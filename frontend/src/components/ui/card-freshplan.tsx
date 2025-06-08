import * as React from 'react';

// FreshPlan Card component using our CSS classes
const Card = React.forwardRef<HTMLDivElement, React.HTMLAttributes<HTMLDivElement>>(
  ({ className, ...props }, ref) => {
    const classes = ['card'];
    if (className) classes.push(className);
    
    return (
      <div
        ref={ref}
        className={classes.join(' ')}
        {...props}
      />
    );
  }
);
Card.displayName = 'Card';

const CardHeader = React.forwardRef<HTMLDivElement, React.HTMLAttributes<HTMLDivElement>>(
  ({ className, ...props }, ref) => {
    const classes = ['card-header'];
    if (className) classes.push(className);
    
    return (
      <div ref={ref} className={classes.join(' ')} {...props} />
    );
  }
);
CardHeader.displayName = 'CardHeader';

const CardTitle = React.forwardRef<HTMLParagraphElement, React.HTMLAttributes<HTMLHeadingElement>>(
  ({ className, ...props }, ref) => {
    const classes = ['card-title'];
    if (className) classes.push(className);
    
    return (
      <h3 ref={ref} className={classes.join(' ')} {...props} />
    );
  }
);
CardTitle.displayName = 'CardTitle';

const CardDescription = React.forwardRef<
  HTMLParagraphElement,
  React.HTMLAttributes<HTMLParagraphElement>
>(({ className, ...props }, ref) => {
  const classes = ['card-description'];
  if (className) classes.push(className);
  
  return (
    <p ref={ref} className={classes.join(' ')} {...props} />
  );
});
CardDescription.displayName = 'CardDescription';

const CardContent = React.forwardRef<HTMLDivElement, React.HTMLAttributes<HTMLDivElement>>(
  ({ className, ...props }, ref) => {
    const classes = ['card-content'];
    if (className) classes.push(className);
    
    return (
      <div ref={ref} className={classes.join(' ')} {...props} />
    );
  }
);
CardContent.displayName = 'CardContent';

const CardFooter = React.forwardRef<HTMLDivElement, React.HTMLAttributes<HTMLDivElement>>(
  ({ className, ...props }, ref) => {
    const classes = ['card-footer'];
    if (className) classes.push(className);
    
    return (
      <div ref={ref} className={classes.join(' ')} {...props} />
    );
  }
);
CardFooter.displayName = 'CardFooter';

export { Card, CardHeader, CardFooter, CardTitle, CardDescription, CardContent };