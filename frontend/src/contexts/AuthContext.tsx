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
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);

  const login = async (email: string, password: string) => {
    // TODO: Implement Keycloak login
    if (import.meta.env.DEV) {
      console.log('Login attempt for email:', email);
      // SECURITY: Never log passwords! But we need to use the parameter
      console.assert(password.length > 0, 'Password should not be empty');
    }

    // Mock login for now
    setUser({
      id: '1',
      name: 'Demo User',
      email: email,
    });
    setToken('mock-jwt-token');
  };

  const logout = () => {
    // TODO: Implement Keycloak logout
    setUser(null);
    setToken(null);
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
