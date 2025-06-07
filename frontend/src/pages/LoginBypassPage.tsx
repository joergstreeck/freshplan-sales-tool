import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

/**
 * Test-only login bypass page.
 * This should ONLY be included in test builds!
 */
export function LoginBypassPage() {
  const { login } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    // Simulate login for E2E tests
    // SECURITY: Never use hardcoded credentials!
    const testEmail = import.meta.env.VITE_TEST_USER_EMAIL;
    const testPassword = import.meta.env.VITE_TEST_USER_PASSWORD;

    if (!testEmail || !testPassword) {
      if (import.meta.env.DEV) {
        console.error('Test credentials not configured');
      }
      return;
    }

    login(testEmail, testPassword);

    // Navigate to main app after "login"
    setTimeout(() => {
      navigate('/');
    }, 100);
  }, [login, navigate]);

  return (
    <div style={{ padding: '20px', textAlign: 'center' }}>
      <h2>Test Login...</h2>
      <p>Logging in as e2e@test.de</p>
    </div>
  );
}
