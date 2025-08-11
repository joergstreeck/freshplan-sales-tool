/**
 * LazyImage Component
 *
 * Optimized image loading with intersection observer for performance.
 * Shows skeleton while loading and handles errors gracefully.
 *
 * @module LazyImage
 * @since FC-005 PR4
 */

import React, { useState, useEffect } from 'react';
import { useInView } from 'react-intersection-observer';
import { Box, Skeleton } from '@mui/material';
import { BrokenImage as BrokenImageIcon } from '@mui/icons-material';

interface LazyImageProps {
  src: string;
  alt: string;
  width?: number | string;
  height?: number | string;
  className?: string;
  threshold?: number;
  rootMargin?: string;
  placeholder?: React.ReactNode;
  onLoad?: () => void;
  onError?: () => void;
  objectFit?: 'cover' | 'contain' | 'fill' | 'none' | 'scale-down';
}

/**
 * Lazy loading image component with intersection observer
 */
export const LazyImage: React.FC<LazyImageProps> = ({
  src,
  alt,
  width = '100%',
  height = 'auto',
  className,
  threshold = 0.1,
  rootMargin = '50px',
  placeholder,
  onLoad,
  onError,
  objectFit = 'cover',
}) => {
  const [imageSrc, setImageSrc] = useState<string | null>(null);
  const [imageError, setImageError] = useState(false);
  const [isLoaded, setIsLoaded] = useState(false);

  const { ref, inView } = useInView({
    threshold,
    rootMargin,
    triggerOnce: true,
  });

  useEffect(() => {
    if (inView && src) {
      // Start loading the image
      const img = new Image();

      img.onload = () => {
        setImageSrc(src);
        setIsLoaded(true);
        onLoad?.();
      };

      img.onerror = () => {
        setImageError(true);
        onError?.();
      };

      img.src = src;
    }
  }, [inView, src, onLoad, onError]);

  // Error state
  if (imageError) {
    return (
      <Box
        ref={ref}
        sx={{
          width,
          height,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          bgcolor: 'action.disabledBackground',
          borderRadius: 1,
        }}
        className={className}
      >
        <BrokenImageIcon sx={{ color: 'text.disabled', fontSize: 40 }} />
      </Box>
    );
  }

  // Loading state
  if (!isLoaded) {
    return (
      <Box ref={ref} sx={{ width, height }} className={className}>
        {placeholder || (
          <Skeleton variant="rectangular" width={width} height={height} animation="wave" />
        )}
      </Box>
    );
  }

  // Loaded state
  return (
    <Box
      ref={ref}
      component="img"
      src={imageSrc!}
      alt={alt}
      sx={{
        width,
        height,
        objectFit,
        display: 'block',
      }}
      className={className}
    />
  );
};
