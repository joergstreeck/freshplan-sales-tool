import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import {
  Box,
  TextField,
  Button,
  Typography,
  FormControl,
  FormLabel,
  FormGroup,
  FormControlLabel,
  Checkbox,
  Switch,
  Alert,
  Grid,
  Divider,
  CircularProgress,
} from '@mui/material';
import {
  CreateUserSchema,
  UpdateUserSchema,
  createEmptyUser,
  getAllRoles,
  type CreateUserData,
  type UpdateUserData,
  type User,
} from './userSchemas';
import { useCreateUser, useUpdateUser } from './userQueries';

interface UserFormProps {
  user?: User;
  onSuccess?: () => void;
  onCancel?: () => void;
}

export const UserFormMUI = ({ user, onSuccess, onCancel }: UserFormProps) => {
  const isEditMode = !!user;
  const createUser = useCreateUser();
  const updateUser = useUpdateUser();

  const schema = isEditMode ? UpdateUserSchema : CreateUserSchema;
  const defaultValues = isEditMode ? user : createEmptyUser();

  const {
    control,
    handleSubmit,
    formState: { errors, isValid },
    watch,
    setValue,
  } = useForm({
    resolver: zodResolver(schema),
    defaultValues,
    mode: 'onChange',
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
    }
  };

  const handleRoleToggle = (role: string) => {
    const currentRoles = watchedRoles;
    const updatedRoles = currentRoles.includes(role)
      ? currentRoles.filter((r) => r !== role)
      : [...currentRoles, role];

    if (updatedRoles.length > 0) {
      setValue('roles', updatedRoles, { shouldValidate: true });
    }
  };

  const isPending = createUser.isPending || updateUser.isPending;

  const roleLabels: Record<string, string> = {
    admin: 'Administrator',
    manager: 'Manager',
    sales: 'Vertrieb',
    user: 'Benutzer',
  };

  return (
    <Box sx={{ width: '100%' }}>
      {/* Title */}
      <Typography variant="h5" sx={{ mb: 2, fontFamily: 'Antonio, sans-serif' }}>
        {isEditMode ? 'Benutzer bearbeiten' : 'Neuen Benutzer erstellen'}
      </Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
        {isEditMode
          ? 'Bearbeiten Sie die Benutzerdaten und Rollen.'
          : 'Geben Sie die Daten für den neuen Benutzer ein.'}
      </Typography>

      <Divider sx={{ mb: 3 }} />

      <form onSubmit={handleSubmit(onSubmit)}>
        {/* Personal Information Section */}
        <Typography variant="h6" sx={{ mb: 2, fontFamily: 'Antonio, sans-serif' }}>
          Persönliche Daten
        </Typography>

        <Grid container spacing={2} sx={{ mb: 3 }}>
          <Grid item xs={12} md={6}>
            <Controller
              name="firstName"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  fullWidth
                  label="Vorname *"
                  error={!!errors.firstName}
                  helperText={errors.firstName?.message}
                  variant="outlined"
                />
              )}
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <Controller
              name="lastName"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  fullWidth
                  label="Nachname *"
                  error={!!errors.lastName}
                  helperText={errors.lastName?.message}
                  variant="outlined"
                />
              )}
            />
          </Grid>
        </Grid>

        {/* Account Information Section */}
        <Typography variant="h6" sx={{ mb: 2, fontFamily: 'Antonio, sans-serif' }}>
          Kontodaten
        </Typography>

        <Grid container spacing={2} sx={{ mb: 3 }}>
          <Grid item xs={12} md={6}>
            <Controller
              name="username"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  fullWidth
                  label="Benutzername *"
                  error={!!errors.username}
                  helperText={errors.username?.message || 'z.B. john.doe'}
                  variant="outlined"
                />
              )}
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <Controller
              name="email"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  fullWidth
                  type="email"
                  label="E-Mail *"
                  error={!!errors.email}
                  helperText={errors.email?.message || 'john.doe@example.com'}
                  variant="outlined"
                />
              )}
            />
          </Grid>
        </Grid>

        {/* Roles Section */}
        <Typography variant="h6" sx={{ mb: 2, fontFamily: 'Antonio, sans-serif' }}>
          Berechtigungen
        </Typography>

        <FormControl component="fieldset" error={!!errors.roles} sx={{ mb: 3, width: '100%' }}>
          <FormLabel component="legend" sx={{ mb: 1 }}>
            Rollen zuweisen *
          </FormLabel>
          <Grid container spacing={1}>
            {getAllRoles().map((role) => (
              <Grid item xs={12} sm={6} key={role}>
                <FormControlLabel
                  control={
                    <Checkbox
                      checked={watchedRoles.includes(role)}
                      onChange={() => handleRoleToggle(role)}
                      sx={{
                        color: '#94C456',
                        '&.Mui-checked': {
                          color: '#94C456',
                        },
                      }}
                    />
                  }
                  label={
                    <Box>
                      <Typography variant="body1">{roleLabels[role] || role}</Typography>
                      <Typography variant="caption" color="text.secondary">
                        {role === 'admin' && 'Vollzugriff auf alle Funktionen'}
                        {role === 'manager' && 'Verwaltung von Teams und Berichten'}
                        {role === 'sales' && 'Zugriff auf Vertriebsfunktionen'}
                        {role === 'user' && 'Basiszugriff auf das System'}
                      </Typography>
                    </Box>
                  }
                />
              </Grid>
            ))}
          </Grid>
          {errors.roles && (
            <Typography variant="caption" color="error" sx={{ mt: 1 }}>
              {errors.roles.message}
            </Typography>
          )}
        </FormControl>

        {/* Status Section */}
        <Typography variant="h6" sx={{ mb: 2, fontFamily: 'Antonio, sans-serif' }}>
          Status
        </Typography>

        <FormControl sx={{ mb: 3 }}>
          <Controller
            name="enabled"
            control={control}
            render={({ field }) => (
              <FormControlLabel
                control={
                  <Switch
                    {...field}
                    checked={field.value}
                    sx={{
                      '& .MuiSwitch-switchBase.Mui-checked': {
                        color: '#94C456',
                      },
                      '& .MuiSwitch-switchBase.Mui-checked + .MuiSwitch-track': {
                        backgroundColor: '#94C456',
                      },
                    }}
                  />
                }
                label={
                  <Box>
                    <Typography variant="body1">Benutzer aktiviert</Typography>
                    <Typography variant="caption" color="text.secondary">
                      {field.value
                        ? 'Der Benutzer kann sich anmelden'
                        : 'Der Benutzer ist gesperrt'}
                    </Typography>
                  </Box>
                }
              />
            )}
          />
        </FormControl>

        <Divider sx={{ mb: 3 }} />

        {/* Error Message */}
        {(createUser.isError || updateUser.isError) && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {createUser.error?.message || updateUser.error?.message || 'Ein Fehler ist aufgetreten'}
          </Alert>
        )}

        {/* Form Actions */}
        <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 2 }}>
          {onCancel && (
            <Button
              variant="outlined"
              onClick={onCancel}
              disabled={isPending}
              sx={{
                borderColor: '#004F7B',
                color: '#004F7B',
                '&:hover': {
                  borderColor: '#003A5A',
                },
              }}
            >
              Abbrechen
            </Button>
          )}
          <Button
            type="submit"
            variant="contained"
            disabled={!isValid || isPending}
            startIcon={isPending && <CircularProgress size={20} />}
            sx={{
              backgroundColor: '#94C456',
              color: 'white',
              '&:hover': {
                backgroundColor: '#7BA347',
              },
              '&:disabled': {
                backgroundColor: '#cccccc',
              },
            }}
          >
            {isEditMode ? 'Speichern' : 'Erstellen'}
          </Button>
        </Box>
      </form>
    </Box>
  );
};