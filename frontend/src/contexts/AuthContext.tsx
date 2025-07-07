/* eslint-disable react-refresh/only-export-components */
import { createContext, useContext, useState } from 'react';
import type { ReactNode } from 'react';

interface User {
  id: string;
  name: string;
  email: string;
}

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
  token: string | null;
}

const AuthContext = createContext<AuthContextType | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  // Auto-Login in Development mit richtigem Mock-Token
  const mockToken =
    'eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6Im1vY2sta2V5LWlkIn0.eyJleHAiOjE5OTk5OTk5OTksImlhdCI6MTYwOTQ1OTIwMCwianRpIjoibW9jay1qdGktYWRtaW4iLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgxODAvcmVhbG1zL2ZyZXNocGxhbiIsImF1ZCI6ImZyZXNocGxhbi1iYWNrZW5kIiwic3ViIjoibW9jay1hZG1pbi11c2VyIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoiZnJlc2hwbGFuLWJhY2tlbmQiLCJzZXNzaW9uX3N0YXRlIjoibW9jay1zZXNzaW9uIiwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbImFkbWluIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsiZnJlc2hwbGFuLWJhY2tlbmQiOnsicm9sZXMiOlsiYWRtaW4iXX19LCJzY29wZSI6Im9wZW5pZCBlbWFpbCBwcm9maWxlIiwic2lkIjoibW9jay1zaWQiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwibmFtZSI6IkFkbWluIFVzZXIiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJhZG1pbiIsImdpdmVuX25hbWUiOiJBZG1pbiIsImZhbWlseV9uYW1lIjoiVXNlciIsImVtYWlsIjoiYWRtaW5AZnJlc2hwbGFuLmRlIn0.mock-signature';

  // Initialisierung mit Development Auto-Login
  const [user, setUser] = useState<User | null>(() => {
    if (import.meta.env.DEV) {
      const mockUser = {
        id: 'mock-admin-user',
        name: 'Admin User',
        email: 'admin@freshplan.de',
      };
      // Speichere auch in localStorage für API-Client
      localStorage.setItem('auth-token', mockToken);
      localStorage.setItem('auth-user', JSON.stringify(mockUser));
      return mockUser;
    }
    return null;
  });

  const [token, setToken] = useState<string | null>(import.meta.env.DEV ? mockToken : null);

  const login = async (email: string, password: string) => {
    // TODO: Implement Keycloak login
    if (import.meta.env.DEV && password.length === 0) {
      throw new Error('Password should not be empty');
    }

    // Mock login for now
    const mockUser = {
      id: '1',
      name: 'Demo User',
      email: email,
    };
    setUser(mockUser);
    setToken('mock-jwt-token');

    // Sync with localStorage for API client
    localStorage.setItem('auth-token', 'mock-jwt-token');
    localStorage.setItem('auth-user', JSON.stringify(mockUser));
  };

  const logout = () => {
    // TODO: Implement Keycloak logout
    setUser(null);
    setToken(null);

    // Clear localStorage
    localStorage.removeItem('auth-token');
    localStorage.removeItem('auth-user');
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: !!user,
        login,
        logout,
        token,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
}
