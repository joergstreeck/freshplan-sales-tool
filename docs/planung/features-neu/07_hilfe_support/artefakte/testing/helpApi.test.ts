import { getMenu } from '../../src/lib/helpApi';

test('helpApi getMenu builds query', () => {
  const u = new URL('/api/help/menu?module=03&limit=10', 'http://x');
  expect(u.searchParams.get('module')).toBe('03');
});
