import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import {
  CreateUserSchema,
  UpdateUserSchema,
  createEmptyUser,
  getAllRoles,
  type CreateUserData,
  type UpdateUserData,
  type User,
} from '../api/userSchemas';
import { useCreateUser, useUpdateUser } from '../api/userQueries';
import { Button } from '../../../components/ui/button-transition';
import { Input } from '../../../components/ui/input';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '../../../components/ui/card-transition';

interface UserFormProps {
  user?: User; // If provided, form is in edit mode
  onSuccess?: () => void;
  onCancel?: () => void;
}

export const UserForm = ({ user, onSuccess, onCancel }: UserFormProps) => {
  const isEditMode = !!user;
  const createUser = useCreateUser();
  const updateUser = useUpdateUser();

  const schema = isEditMode ? UpdateUserSchema : CreateUserSchema;
  const defaultValues = isEditMode ? user : createEmptyUser();

  const {
    register,
    handleSubmit,
    formState: { errors, isValid },
    watch,
    setValue,
  } = useForm({
    resolver: zodResolver(schema),
    defaultValues,
    mode: 'onChange', // Validate on change for better UX
  });

  const watchedRoles = watch('roles') || [];

  const onSubmit = async (data: CreateUserData | UpdateUserData) => {
    try {
      if (isEditMode) {
        await updateUser.mutateAsync(data as UpdateUserData);
      } else {
        await createUser.mutateAsync(data as CreateUserData);
      }
      onSuccess?.();
    } catch (_error) {
      void _error;
      // Error handling is done in the mutation hooks
    }
  };

  const handleRoleToggle = (role: string) => {
    const currentRoles = watchedRoles;
    const updatedRoles = currentRoles.includes(role)
      ? currentRoles.filter(r => r !== role)
      : [...currentRoles, role];

    // Ensure at least one role is selected
    if (updatedRoles.length > 0) {
      setValue('roles', updatedRoles, { shouldValidate: true });
    }
  };

  const isPending = createUser.isPending || updateUser.isPending;

  return (
    <Card className="w-full max-w-2xl">
      <CardHeader>
        <CardTitle>{isEditMode ? 'Benutzer bearbeiten' : 'Neuen Benutzer erstellen'}</CardTitle>
        <CardDescription>
          {isEditMode
            ? 'Bearbeiten Sie die Benutzerdaten und Rollen.'
            : 'Geben Sie die Daten für den neuen Benutzer ein.'}
        </CardDescription>
      </CardHeader>

      <CardContent>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
          {/* Username */}
          <div className="space-y-2">
            <label htmlFor="username" className="text-sm font-medium">
              Benutzername *
            </label>
            <Input
              id="username"
              {...register('username')}
              placeholder="z.B. john.doe"
              className={errors.username ? 'border-destructive' : ''}
            />
            {errors.username && (
              <p className="text-sm text-destructive">{errors.username.message}</p>
            )}
          </div>

          {/* Email */}
          <div className="space-y-2">
            <label htmlFor="email" className="text-sm font-medium">
              E-Mail *
            </label>
            <Input
              id="email"
              type="email"
              {...register('email')}
              placeholder="john.doe@example.com"
              className={errors.email ? 'border-destructive' : ''}
            />
            {errors.email && <p className="text-sm text-destructive">{errors.email.message}</p>}
          </div>

          {/* First Name */}
          <div className="space-y-2">
            <label htmlFor="firstName" className="text-sm font-medium">
              Vorname *
            </label>
            <Input
              id="firstName"
              {...register('firstName')}
              placeholder="John"
              className={errors.firstName ? 'border-destructive' : ''}
            />
            {errors.firstName && (
              <p className="text-sm text-destructive">{errors.firstName.message}</p>
            )}
          </div>

          {/* Last Name */}
          <div className="space-y-2">
            <label htmlFor="lastName" className="text-sm font-medium">
              Nachname *
            </label>
            <Input
              id="lastName"
              {...register('lastName')}
              placeholder="Doe"
              className={errors.lastName ? 'border-destructive' : ''}
            />
            {errors.lastName && (
              <p className="text-sm text-destructive">{errors.lastName.message}</p>
            )}
          </div>

          {/* Roles */}
          <div className="space-y-2">
            <label className="text-sm font-medium">Rollen *</label>
            <div className="grid grid-cols-2 gap-2">
              {getAllRoles().map(role => (
                <label key={role} className="flex items-center space-x-2 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={watchedRoles.includes(role)}
                    onChange={() => handleRoleToggle(role)}
                    className="rounded border-gray-300"
                  />
                  <span className="text-sm capitalize">{role}</span>
                </label>
              ))}
            </div>
            {errors.roles && <p className="text-sm text-destructive">{errors.roles.message}</p>}
          </div>

          {/* Enabled Status */}
          <div className="space-y-2">
            <label className="flex items-center space-x-2 cursor-pointer">
              <input type="checkbox" {...register('enabled')} className="rounded border-gray-300" />
              <span className="text-sm font-medium">Benutzer aktiviert</span>
            </label>
            {errors.enabled && <p className="text-sm text-destructive">{errors.enabled.message}</p>}
          </div>

          {/* Form Actions */}
          <div className="flex justify-end space-x-2 pt-4">
            {onCancel && (
              <Button type="button" variant="outline" onClick={onCancel} disabled={isPending}>
                Abbrechen
              </Button>
            )}
            <Button type="submit" disabled={!isValid || isPending}>
              {isPending && <span className="mr-2">⏳</span>}
              {isEditMode ? 'Speichern' : 'Erstellen'}
            </Button>
          </div>
        </form>
      </CardContent>
    </Card>
  );
};
