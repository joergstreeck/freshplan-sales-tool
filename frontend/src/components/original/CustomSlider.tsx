import * as Slider from '@radix-ui/react-slider';

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
  'aria-label': ariaLabel,
}: CustomSliderProps) {
  const percentage = ((value - min) / (max - min)) * 100;

  return (
    <Slider.Root
      className="custom-slider-root"
      value={[value]}
      onValueChange={values => onValueChange(values[0])}
      min={min}
      max={max}
      step={step}
      id={id}
      aria-label={ariaLabel}
    >
      <Slider.Track className="custom-slider-track">
        <Slider.Range className="custom-slider-range" style={{ width: `${percentage}%` }} />
      </Slider.Track>
      <Slider.Thumb
        className="custom-slider-thumb"
        style={{ left: `${percentage}%` }}
        aria-label={ariaLabel}
      />
    </Slider.Root>
  );
}
