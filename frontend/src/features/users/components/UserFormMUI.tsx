import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import {
  Box,
  Paper,
  TextField,
  Button,
  Stack,
  Typography,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  FormHelperText,
  Chip,
  Alert,
  CircularProgress,
} from '@mui/material';
import { useCreateUser, useUpdateUser } from '../userQueries';
import type { User, CreateUserData, UpdateUserData } from '../userSchemas';
import { CreateUserSchema, UpdateUserSchema } from '../userSchemas';

interface UserFormProps {
  user?: User;
  onSuccess?: () => void;
  onCancel?: () => void;
}

type FormData = CreateUserData | UpdateUserData;

const availableRoles = [
  { value: 'admin', label: 'Administrator' },
  { value: 'manager', label: 'Manager' },
  { value: 'sales', label: 'Vertrieb' },
];

export const UserFormMUI = ({ user, onSuccess, onCancel }: UserFormProps) => {
  const isEditing = !!user;
  const createUser = useCreateUser();
  const updateUser = useUpdateUser();

  const {
    control,
    handleSubmit,
    formState: { errors, isSubmitting },
    setError,
  } = useForm<FormData>({
    resolver: zodResolver(isEditing ? UpdateUserSchema : CreateUserSchema),
    defaultValues: isEditing
      ? {
          firstName: user.firstName,
          lastName: user.lastName,
          email: user.email,
          roles: user.roles,
        }
      : {
          username: '',
          firstName: '',
          lastName: '',
          email: '',
          password: '',
          roles: ['sales'],
        },
  });

  const onSubmit = async (data: FormData) => {
    try {
      if (isEditing) {
        await updateUser.mutateAsync({ id: user.id, data: data as UpdateUserRequest });
      } else {
        await createUser.mutateAsync(data as CreateUserRequest);
      }
      onSuccess?.();
    } catch (error: unknown) {
      // Handle server validation errors
      if (error && typeof error === 'object' && 'response' in error) {
        const apiError = error as {
          response?: { data?: { errors?: Record<string, string> } };
          message?: string;
        };
        if (apiError.response?.data?.errors) {
          Object.entries(apiError.response.data.errors).forEach(([field, message]) => {
            setError(field as keyof FormData, { message: message as string });
          });
        } else {
          setError('root', {
            message: apiError.message || 'Ein unerwarteter Fehler ist aufgetreten',
          });
        }
      } else {
        setError('root', {
          message: 'Ein unerwarteter Fehler ist aufgetreten',
        });
      }
    }
  };

  return (
    <Paper sx={{ maxWidth: 600, mx: 'auto', p: 4 }}>
      <Typography variant="h5" gutterBottom>
        {isEditing ? 'Benutzer bearbeiten' : 'Neuen Benutzer anlegen'}
      </Typography>

      <Box component="form" onSubmit={handleSubmit(onSubmit)} sx={{ mt: 3 }}>
        <Stack spacing={3}>
          {/* Username - only for creation */}
          {!isEditing && (
            <Controller
              name="username"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  label="Benutzername"
                  fullWidth
                  required
                  error={!!errors.username}
                  helperText={errors.username?.message}
                />
              )}
            />
          )}

          {/* First Name */}
          <Controller
            name="firstName"
            control={control}
            render={({ field }) => (
              <TextField
                {...field}
                label="Vorname"
                fullWidth
                required
                error={!!errors.firstName}
                helperText={errors.firstName?.message}
              />
            )}
          />

          {/* Last Name */}
          <Controller
            name="lastName"
            control={control}
            render={({ field }) => (
              <TextField
                {...field}
                label="Nachname"
                fullWidth
                required
                error={!!errors.lastName}
                helperText={errors.lastName?.message}
              />
            )}
          />

          {/* Email */}
          <Controller
            name="email"
            control={control}
            render={({ field }) => (
              <TextField
                {...field}
                label="E-Mail"
                type="email"
                fullWidth
                required
                error={!!errors.email}
                helperText={errors.email?.message}
              />
            )}
          />

          {/* Password - only for creation */}
          {!isEditing && (
            <Controller
              name="password"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  label="Passwort"
                  type="password"
                  fullWidth
                  required
                  error={!!errors.password}
                  helperText={errors.password?.message}
                />
              )}
            />
          )}

          {/* Roles */}
          <Controller
            name="roles"
            control={control}
            render={({ field }) => (
              <FormControl fullWidth error={!!errors.roles}>
                <InputLabel>Rollen</InputLabel>
                <Select
                  {...field}
                  multiple
                  label="Rollen"
                  renderValue={selected => (
                    <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                      {(selected as string[]).map(value => (
                        <Chip
                          key={value}
                          label={availableRoles.find(r => r.value === value)?.label || value}
                          size="small"
                        />
                      ))}
                    </Box>
                  )}
                >
                  {availableRoles.map(role => (
                    <MenuItem key={role.value} value={role.value}>
                      {role.label}
                    </MenuItem>
                  ))}
                </Select>
                {errors.roles && <FormHelperText>{errors.roles.message}</FormHelperText>}
              </FormControl>
            )}
          />

          {/* Error display */}
          {errors.root && <Alert severity="error">{errors.root.message}</Alert>}

          {/* Actions */}
          <Stack direction="row" spacing={2} justifyContent="flex-end">
            <Button variant="outlined" onClick={onCancel} disabled={isSubmitting}>
              Abbrechen
            </Button>
            <Button
              type="submit"
              variant="contained"
              disabled={isSubmitting}
              sx={{
                bgcolor: '#94C456',
                '&:hover': { bgcolor: '#7aa845' },
              }}
            >
              {isSubmitting ? (
                <CircularProgress size={24} color="inherit" />
              ) : isEditing ? (
                'Speichern'
              ) : (
                'Anlegen'
              )}
            </Button>
          </Stack>
        </Stack>
      </Box>
    </Paper>
  );
};
