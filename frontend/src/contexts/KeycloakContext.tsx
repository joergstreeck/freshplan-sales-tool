/**
 * Keycloak Authentication Context
 */
import React, { createContext, useContext, useEffect, useState, ReactNode } from 'react';
import { keycloak, initKeycloak, authUtils } from '../lib/keycloak';

interface KeycloakContextType {
  isAuthenticated: boolean;
  isLoading: boolean;
  login: () => void;
  logout: (redirectUri?: string) => void;
  token: string | undefined;
  userId: string | undefined;
  username: string | undefined;
  email: string | undefined;
  hasRole: (role: string) => boolean;
  userRoles: string[];
}

const KeycloakContext = createContext<KeycloakContextType | undefined>(undefined);

interface KeycloakProviderProps {
  children: ReactNode;
}

export const KeycloakProvider: React.FC<KeycloakProviderProps> = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [token, setToken] = useState<string | undefined>(undefined);
  const [userId, setUserId] = useState<string | undefined>(undefined);
  const [username, setUsername] = useState<string | undefined>(undefined);
  const [email, setEmail] = useState<string | undefined>(undefined);
  const [userRoles, setUserRoles] = useState<string[]>([]);

  useEffect(() => {
    const initAuth = async () => {
      try {
        const authenticated = await initKeycloak();

        setIsAuthenticated(authenticated);

        if (authenticated) {
          updateUserInfo();

          // Event-Listener für Token-Updates
          keycloak.onTokenExpired = () => {
            keycloak
              .updateToken(30)
              .then(() => {
                // Token refreshed successfully
              })
              .catch(() => {
                // User-Benachrichtigung bei Token-Fehler
                const event = new CustomEvent('auth-error', {
                  detail: {
                    type: 'token-refresh-failed',
                    message:
                      'Ihre Sitzung ist abgelaufen. Bitte melden Sie sich erneut an.',
                  },
                });
                window.dispatchEvent(event);

                // Nach 3 Sekunden zur Login-Seite
                setTimeout(() => {
                  keycloak.login();
                }, 3000);
              });
          };

          keycloak.onAuthSuccess = () => {
            updateUserInfo();
          };

          keycloak.onAuthError = () => {
            const event = new CustomEvent('auth-error', {
              detail: {
                type: 'auth-failed',
                message:
                  'Authentifizierung fehlgeschlagen. Bitte versuchen Sie es erneut.',
              },
            });
            window.dispatchEvent(event);
          };

          keycloak.onAuthLogout = () => {
            setIsAuthenticated(false);
            clearUserInfo();
          };
        }
      } catch (error) {
        // Auth initialization failed
      } finally {
        setIsLoading(false);
      }
    };

    initAuth();
  }, []);

  const updateUserInfo = () => {
    setToken(authUtils.getToken());
    setUserId(authUtils.getUserId());
    setUsername(authUtils.getUsername());
    setEmail(authUtils.getEmail());
    setUserRoles(authUtils.getUserRoles());
  };

  const clearUserInfo = () => {
    setToken(undefined);
    setUserId(undefined);
    setUsername(undefined);
    setEmail(undefined);
    setUserRoles([]);
  };

  const contextValue: KeycloakContextType = {
    isAuthenticated,
    isLoading,
    login: authUtils.login,
    logout: authUtils.logout,
    token,
    userId,
    username,
    email,
    hasRole: authUtils.hasRole,
    userRoles,
  };

  return <KeycloakContext.Provider value={contextValue}>{children}</KeycloakContext.Provider>;
};

export const useKeycloak = (): KeycloakContextType => {
  const context = useContext(KeycloakContext);
  if (context === undefined) {
    throw new Error('useKeycloak must be used within a KeycloakProvider');
  }
  return context;
};
