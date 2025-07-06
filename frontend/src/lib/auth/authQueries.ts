/**
 * React Query Hooks für Auth-Daten
 */
import { useQuery } from '@tanstack/react-query';
import { authUtils } from '../keycloak';

/**
 * Hook für gecachte User-Rollen
 */
export const useUserRoles = () => {
  return useQuery({
    queryKey: ['auth', 'roles'],
    queryFn: () => authUtils.getUserRoles(),
    staleTime: 30 * 60 * 1000, // 30 Minuten
    cacheTime: 60 * 60 * 1000, // 60 Minuten
    enabled: authUtils.isAuthenticated(),
  });
};

/**
 * Hook für gecachte User-Informationen
 */
export const useUserInfo = () => {
  return useQuery({
    queryKey: ['auth', 'userInfo'],
    queryFn: () => ({
      userId: authUtils.getUserId(),
      username: authUtils.getUsername(),
      email: authUtils.getEmail(),
      roles: authUtils.getUserRoles(),
    }),
    staleTime: 30 * 60 * 1000, // 30 Minuten
    cacheTime: 60 * 60 * 1000, // 60 Minuten
    enabled: authUtils.isAuthenticated(),
  });
};

/**
 * Hook für Role-Check mit Cache
 */
export const useHasRole = (role: string) => {
  const { data: roles = [] } = useUserRoles();
  return roles.includes(role);
};
