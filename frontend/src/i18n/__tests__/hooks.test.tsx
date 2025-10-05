import { describe, test, expect, beforeEach } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { I18nextProvider } from 'react-i18next';
import i18n from '../index';
import { useLanguage } from '../hooks';
import React from 'react';

// Test wrapper component
const wrapper = ({ children }: { children: React.ReactNode }) => (
  <I18nextProvider i18n={i18n}>{children}</I18nextProvider>
);

describe('useLanguage Hook', () => {
  beforeEach(() => {
    // Reset i18n to default state
    i18n.changeLanguage('de');
    localStorage.clear();
  });

  test('returns current language', () => {
    const { result } = renderHook(() => useLanguage(), { wrapper });

    expect(result.current.currentLanguage).toBe('de');
    expect(result.current.isGerman).toBe(true);
  });

  test('toggleLanguage switches between de and en', async () => {
    const { result } = renderHook(() => useLanguage(), { wrapper });

    expect(result.current.currentLanguage).toBe('de');

    await act(async () => {
      result.current.toggleLanguage();
    });

    expect(result.current.currentLanguage).toBe('en');
    expect(result.current.isGerman).toBe(false);

    await act(async () => {
      result.current.toggleLanguage();
    });

    expect(result.current.currentLanguage).toBe('de');
    expect(result.current.isGerman).toBe(true);
  });

  test('setLanguage sets specific language', async () => {
    const { result } = renderHook(() => useLanguage(), { wrapper });

    await act(async () => {
      result.current.setLanguage('en');
    });

    expect(result.current.currentLanguage).toBe('en');
    expect(result.current.isGerman).toBe(false);
  });

  test('language change persists in localStorage', async () => {
    const { result } = renderHook(() => useLanguage(), { wrapper });

    await act(async () => {
      result.current.setLanguage('en');
    });

    // Check localStorage
    const storedLanguage = localStorage.getItem('i18nextLng');
    expect(storedLanguage).toBe('en');
  });
});
