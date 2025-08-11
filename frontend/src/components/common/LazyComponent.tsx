/**
 * LazyComponent
 * 
 * Generic lazy loading wrapper for heavy components.
 * Uses intersection observer to defer rendering until visible.
 * 
 * @module LazyComponent
 * @since FC-005 PR4
 */

import React, { useState, useEffect } from 'react';
import { useInView } from 'react-intersection-observer';
import { Box, CircularProgress } from '@mui/material';

interface LazyComponentProps {
  children: React.ReactNode;
  threshold?: number;
  rootMargin?: string;
  minHeight?: number | string;
  placeholder?: React.ReactNode;
  fallback?: React.ReactNode;
  onVisible?: () => void;
  forceRender?: boolean;
}

/**
 * Wrapper component for lazy loading heavy components
 */
export const LazyComponent: React.FC<LazyComponentProps> = ({
  children,
  threshold = 0.1,
  rootMargin = '100px',
  minHeight = 200,
  placeholder,
  fallback,
  onVisible,
  forceRender = false,
}) => {
  const [shouldRender, setShouldRender] = useState(forceRender);
  
  const { ref, inView } = useInView({
    threshold,
    rootMargin,
    triggerOnce: true,
    skip: forceRender,
  });

  useEffect(() => {
    if (inView && !shouldRender) {
      setShouldRender(true);
      onVisible?.();
    }
  }, [inView, shouldRender, onVisible]);

  // Force render if prop changes
  useEffect(() => {
    if (forceRender && !shouldRender) {
      setShouldRender(true);
    }
  }, [forceRender, shouldRender]);

  if (!shouldRender) {
    return (
      <Box
        ref={ref}
        sx={{
          minHeight,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
        }}
      >
        {placeholder || (
          <CircularProgress size={24} sx={{ color: 'text.disabled' }} />
        )}
      </Box>
    );
  }

  return (
    <Box ref={ref}>
      {fallback ? (
        <React.Suspense fallback={fallback}>
          {children}
        </React.Suspense>
      ) : (
        children
      )}
    </Box>
  );
};