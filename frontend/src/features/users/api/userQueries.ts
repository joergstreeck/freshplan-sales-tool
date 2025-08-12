// React Query hooks for User API
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { httpClient } from '../../../shared/lib/apiClient';
import type { User, CreateUserData, UpdateUserData, UserFilter } from './userSchemas';
import { UserSchema, CreateUserSchema, UpdateUserSchema } from './userSchemas';

// Query keys for consistent cache management
export const userKeys = {
  all: ['users'] as const,
  lists: () => [...userKeys.all, 'list'] as const,
  list: (filters: UserFilter) => [...userKeys.lists(), { filters }] as const,
  details: () => [...userKeys.all, 'detail'] as const,
  detail: (id: string) => [...userKeys.details(), id] as const,
};

// Get all users with optional filtering
export const useUsers = (filters: UserFilter = {}) => {
  return useQuery({
    queryKey: userKeys.list(filters),
    queryFn: async () => {
      const params = new URLSearchParams();

      // Add filter params if provided
      if (filters.email) params.append('email', filters.email);
      if (filters.role) params.append('role', filters.role);
      if (filters.enabled !== undefined) params.append('enabled', filters.enabled.toString());
      if (filters.search) params.append('search', filters.search);

      const queryString = params.toString();
      const endpoint = queryString ? `/api/users?${queryString}` : '/api/users';

      const response = await httpClient.get<User[]>(endpoint);

      // Validate response with Zod
      return UserSchema.array().parse(response.data);
    },
    staleTime: 30000, // 30 seconds
  });
};

// Get single user by ID
export const useUser = (id: string) => {
  return useQuery({
    queryKey: userKeys.detail(id),
    queryFn: async () => {
      const response = await httpClient.get<User>(`/api/users/${id}`);
      return UserSchema.parse(response.data);
    },
    enabled: !!id, // Only run query if ID is provided
  });
};

// Create new user
export const useCreateUser = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (userData: CreateUserData) => {
      // Validate input data
      const validatedData = CreateUserSchema.parse(userData);

      const response = await httpClient.post<User>('/api/users', validatedData);
      return UserSchema.parse(response.data);
    },
    onSuccess: newUser => {
      // Invalidate users list to refetch with new user
      queryClient.invalidateQueries({ queryKey: userKeys.lists() });

      // Optionally add the new user to the cache
      queryClient.setQueryData(userKeys.detail(newUser.id), newUser);
    },
    onError: _error => {
      // Error handled by mutation
    },
  });
};

// Update existing user
export const useUpdateUser = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (userData: UpdateUserData) => {
      // Validate input data
      const validatedData = UpdateUserSchema.parse(userData);

      const response = await httpClient.put<User>(`/api/users/${validatedData.id}`, validatedData);
      return UserSchema.parse(response.data);
    },
    onSuccess: updatedUser => {
      // Update the specific user in cache
      queryClient.setQueryData(userKeys.detail(updatedUser.id), updatedUser);

      // Invalidate users list to show updated data
      queryClient.invalidateQueries({ queryKey: userKeys.lists() });
    },
    onError: _error => {
      // Error handled by mutation
    },
  });
};

// Delete user
export const useDeleteUser = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (id: string) => {
      await httpClient.delete(`/api/users/${id}`);
      return id;
    },
    onSuccess: deletedUserId => {
      // Remove user from cache
      queryClient.removeQueries({ queryKey: userKeys.detail(deletedUserId) });

      // Invalidate users list to refetch without deleted user
      queryClient.invalidateQueries({ queryKey: userKeys.lists() });
    },
    onError: _error => {
      // Error handled by mutation
    },
  });
};

// Enable/Disable user
export const useToggleUserStatus = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({ id, enabled }: { id: string; enabled: boolean }) => {
      const endpoint = enabled ? `/api/users/${id}/enable` : `/api/users/${id}/disable`;
      await httpClient.put(endpoint);
      // The endpoints return 204 No Content, so we return the id and new status
      return { id, enabled };
    },
    onSuccess: ({ id }) => {
      // Invalidate caches to refetch updated data
      queryClient.invalidateQueries({ queryKey: userKeys.detail(id) });
      queryClient.invalidateQueries({ queryKey: userKeys.lists() });
    },
    onError: _error => {
      // Error handled by mutation
    },
  });
};
