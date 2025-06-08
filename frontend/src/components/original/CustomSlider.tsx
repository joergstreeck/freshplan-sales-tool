import * as Slider from '@radix-ui/react-slider';
import { CSSProperties } from 'react';

interface CustomSliderProps {
  value: number;
  onValueChange: (value: number) => void;
  min: number;
  max: number;
  step?: number;
  id?: string;
  'aria-label'?: string;
}

export function CustomSlider({ 
  value, 
  onValueChange, 
  min, 
  max, 
  step = 1,
  id,
  'aria-label': ariaLabel 
}: CustomSliderProps) {
  const percentage = ((value - min) / (max - min)) * 100;

  return (
    <Slider.Root
      className="relative flex items-center select-none touch-none w-full h-5"
      value={[value]}
      onValueChange={(values) => onValueChange(values[0])}
      min={min}
      max={max}
      step={step}
      id={id}
      aria-label={ariaLabel}
      style={{ position: 'relative', height: '20px', cursor: 'pointer' }}
    >
      <Slider.Track 
        className="relative grow rounded-full h-1"
        style={{
          position: 'absolute',
          top: '50%',
          transform: 'translateY(-50%)',
          width: '100%',
          height: '4px',
          backgroundColor: '#e0e0e0',
          borderRadius: '2px'
        }}
      >
        <Slider.Range 
          className="absolute rounded-full h-full"
          style={{
            position: 'absolute',
            top: 0,
            left: 0,
            height: '100%',
            width: `${percentage}%`,
            backgroundColor: '#94c456',
            borderRadius: '2px'
          }}
        />
      </Slider.Track>
      <Slider.Thumb 
        className="block rounded-full"
        style={{
          position: 'absolute',
          top: '50%',
          left: `${percentage}%`,
          transform: 'translate(-50%, -50%)',
          width: '20px',
          height: '20px',
          backgroundColor: '#94c456',
          borderRadius: '50%',
          boxShadow: '0 2px 8px rgba(0, 0, 0, 0.2)',
          transition: 'box-shadow 0.3s ease',
          outline: 'none'
        } as CSSProperties}
        aria-label={ariaLabel}
      />
    </Slider.Root>
  );
}