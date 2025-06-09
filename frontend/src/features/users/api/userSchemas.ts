// Zod schemas for User domain - Single Source of Truth
import { z } from 'zod';

// User roles enum
export const UserRole = z.enum(['admin', 'manager', 'sales']);

// Base User schema matching backend API
export const UserSchema = z.object({
  id: z.string().uuid(),
  username: z.string().min(3).max(50),
  email: z.string().email(),
  firstName: z.string().min(1).max(100),
  lastName: z.string().min(1).max(100),
  roles: z.array(UserRole).min(1),
  enabled: z.boolean(),
  createdAt: z.string().datetime().optional(),
  updatedAt: z.string().datetime().optional(),
});

// Schema for creating a new user (no id, timestamps)
export const CreateUserSchema = UserSchema.omit({
  id: true,
  createdAt: true,
  updatedAt: true,
});

// Schema for updating a user (all fields optional except id)
export const UpdateUserSchema = UserSchema.partial().extend({
  id: z.string().uuid(),
});

// Schema for user search/filter params
export const UserFilterSchema = z.object({
  email: z.string().optional(),
  role: UserRole.optional(),
  enabled: z.boolean().optional(),
  search: z.string().optional(), // General search term
});

// TypeScript types derived from schemas
export type User = z.infer<typeof UserSchema>;
export type CreateUserData = z.infer<typeof CreateUserSchema>;
export type UpdateUserData = z.infer<typeof UpdateUserSchema>;
export type UserFilter = z.infer<typeof UserFilterSchema>;
export type UserRoleType = z.infer<typeof UserRole>;

// Helper functions for role validation
export const isValidRole = (role: string): role is UserRoleType => {
  return UserRole.safeParse(role).success;
};

export const getAllRoles = (): UserRoleType[] => {
  return UserRole.options;
};

// Default values
export const DEFAULT_USER_ROLE: UserRoleType = 'sales';

export const EMPTY_USER_FILTER: UserFilter = {};

export const createEmptyUser = (): Omit<CreateUserData, 'id'> => ({
  username: '',
  email: '',
  firstName: '',
  lastName: '',
  roles: [DEFAULT_USER_ROLE],
  enabled: true,
});
