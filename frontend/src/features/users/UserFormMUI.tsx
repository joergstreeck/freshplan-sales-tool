import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useState, useRef, useEffect } from 'react';
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
  styled,
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

// Styled Components für adaptive Felder
const AdaptiveTextField = styled(TextField)(() => ({
  '& .MuiInputBase-root': {
    transition: 'width 0.2s ease-out',
    minHeight: '44px',
  },
  '& .MuiInputBase-input': {
    padding: '12px 16px',
  },
}));

// Unsichtbarer Mess-Span für Textbreite
const MeasureSpan = styled('span')({
  position: 'absolute',
  visibility: 'hidden',
  whiteSpace: 'pre',
  fontSize: '14px',
  fontFamily: 'Roboto, Helvetica, Arial, sans-serif',
  padding: '0 32px', // Extra padding für Komfort
  pointerEvents: 'none',
  zIndex: -1,
});

// Custom Hook für adaptive Feldbreite
const useAdaptiveWidth = (value: string, minWidth = 200, maxWidth = 400) => {
  const [width, setWidth] = useState(minWidth);
  const measureRef = useRef<HTMLSpanElement>(null);

  useEffect(() => {
    if (measureRef.current) {
      const textWidth = measureRef.current.offsetWidth;
      const calculatedWidth = Math.min(Math.max(textWidth + 50, minWidth), maxWidth);
      setWidth(calculatedWidth);
    }
  }, [value, minWidth, maxWidth]);

  return { width, measureRef };
};

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

  // Adaptive width hooks für alle Felder
  const firstNameWidth = useAdaptiveWidth(watch('firstName') || '', 150, 300);
  const lastNameWidth = useAdaptiveWidth(watch('lastName') || '', 150, 300);
  const usernameWidth = useAdaptiveWidth(watch('username') || '', 180, 350);
  const emailWidth = useAdaptiveWidth(watch('email') || '', 250, 450);

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

        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 2, mb: 3, position: 'relative' }}>
          {/* Measure spans für adaptive width */}
          <MeasureSpan ref={firstNameWidth.measureRef}>
            {watch('firstName') || 'Vorname'}
          </MeasureSpan>
          <MeasureSpan ref={lastNameWidth.measureRef}>
            {watch('lastName') || 'Nachname'}
          </MeasureSpan>

          <Controller
            name="firstName"
            control={control}
            render={({ field }) => (
              <AdaptiveTextField
                {...field}
                label="Vorname *"
                error={!!errors.firstName}
                helperText={errors.firstName?.message}
                variant="outlined"
                sx={{ width: `${firstNameWidth.width}px` }}
              />
            )}
          />
          <Controller
            name="lastName"
            control={control}
            render={({ field }) => (
              <AdaptiveTextField
                {...field}
                label="Nachname *"
                error={!!errors.lastName}
                helperText={errors.lastName?.message}
                variant="outlined"
                sx={{ width: `${lastNameWidth.width}px` }}
              />
            )}
          />
        </Box>

        {/* Account Information Section */}
        <Typography variant="h6" sx={{ mb: 2, fontFamily: 'Antonio, sans-serif' }}>
          Kontodaten
        </Typography>

        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 2, mb: 3, position: 'relative' }}>
          {/* Measure spans für adaptive width */}
          <MeasureSpan ref={usernameWidth.measureRef}>
            {watch('username') || 'Benutzername'}
          </MeasureSpan>
          <MeasureSpan ref={emailWidth.measureRef}>
            {watch('email') || 'email@example.com'}
          </MeasureSpan>

          <Controller
            name="username"
            control={control}
            render={({ field }) => (
              <AdaptiveTextField
                {...field}
                label="Benutzername *"
                error={!!errors.username}
                helperText={errors.username?.message || 'z.B. john.doe'}
                variant="outlined"
                sx={{ width: `${usernameWidth.width}px` }}
              />
            )}
          />
          <Controller
            name="email"
            control={control}
            render={({ field }) => (
              <AdaptiveTextField
                {...field}
                type="email"
                label="E-Mail *"
                error={!!errors.email}
                helperText={errors.email?.message || 'john.doe@example.com'}
                variant="outlined"
                sx={{ width: `${emailWidth.width}px` }}
              />
            )}
          />
        </Box>

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