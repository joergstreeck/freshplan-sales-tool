import { themeV2 } from '../../design-system/theme-v2.mui';
test('Theme V2 primary/secondary mapping', () => {
  expect(themeV2.palette.primary.main).toBe('var(--color-primary-500)');
  expect(themeV2.palette.secondary.main).toBe('var(--color-secondary-500)');
});
