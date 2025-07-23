import createCache from '@emotion/cache';

// Create emotion cache with specific insertion point
// This ensures @import rules are placed before other rules
export const emotionCache = createCache({
  key: 'mui-style',
  prepend: true, // Insert MUI styles at the beginning of <head>
});

// Optional: Create a specific insertion point
export const createEmotionCacheWithInsertionPoint = () => {
  // Create a meta tag as insertion point for MUI styles
  let insertionPoint: HTMLElement;
  
  if (typeof document !== 'undefined') {
    const existingPoint = document.querySelector('meta[name="emotion-insertion-point"]');
    if (existingPoint) {
      insertionPoint = existingPoint as HTMLElement;
    } else {
      insertionPoint = document.createElement('meta');
      insertionPoint.setAttribute('name', 'emotion-insertion-point');
      insertionPoint.setAttribute('content', '');
      document.head.insertBefore(insertionPoint, document.head.firstChild);
    }
  }

  return createCache({
    key: 'mui-style',
    insertionPoint: insertionPoint!,
  });
};