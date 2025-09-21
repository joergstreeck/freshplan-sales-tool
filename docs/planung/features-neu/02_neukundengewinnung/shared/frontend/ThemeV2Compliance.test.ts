// ThemeV2Compliance.test.ts – validates existing FreshFoodz theme
import { freshfoodzTheme } from '../../../src/theme/freshfoodz';

test('FreshFoodz theme has correct brand colors', () => {
  expect(freshfoodzTheme.palette.primary.main).toBe('#94C456'); // FreshFoodz Primärgrün
  expect(freshfoodzTheme.palette.secondary.main).toBe('#004F7B'); // FreshFoodz Dunkelblau
});
