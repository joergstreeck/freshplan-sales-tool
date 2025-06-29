import React from 'react';

interface LoadingSpinnerProps {
  size?: 'small' | 'medium' | 'large';
  message?: string;
}

export function LoadingSpinner({ size = 'medium', message }: LoadingSpinnerProps) {
  const sizeClasses = {
    small: 'w-4 h-4',
    medium: 'w-8 h-8',
    large: 'w-12 h-12',
  };

  return (
    <div className="flex flex-col items-center justify-center p-4">
      <div
        className={`${sizeClasses[size]} border-4 border-gray-200 
          border-t-blue-600 rounded-full animate-spin`}
        role="status"
        aria-label="Loading"
      />
      {message && <p className="mt-2 text-sm text-gray-600">{message}</p>}
    </div>
  );
}
