import { useState } from 'react';
import { Link as RouterLink } from 'react-router-dom';
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  CircularProgress,
  Link,
  MenuItem,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import { registrationService } from '../services/registrationService';

/** Roles a visitor may request. SUPER_ADMIN is intentionally absent. */
const REQUESTABLE_ROLES = [
  { value: 'EMPLOYEE', label: 'Employee' },
  { value: 'TEACHER', label: 'Teacher' },
  { value: 'ADMIN', label: 'Admin' },
];

/**
 * Public self-registration screen.
 *
 * Submits a registration request that awaits Super Admin approval — no
 * account exists (and login fails) until it is approved. On success the form
 * is replaced by a confirmation message.
 */
const RegisterPage = () => {
  const [form, setForm] = useState({
    fullName: '',
    email: '',
    password: '',
    phone: '',
    requestedRole: 'EMPLOYEE',
  });
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [submitted, setSubmitted] = useState(false);

  const handleChange = (field) => (event) => {
    setForm((prev) => ({ ...prev, [field]: event.target.value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError(null);
    setSubmitting(true);

    try {
      await registrationService.register(form);
      setSubmitted(true);
    } catch (err) {
      setError(err?.message ?? 'Registration failed. Please try again.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        bgcolor: 'background.default',
        p: 2,
      }}
    >
      <Card sx={{ width: '100%', maxWidth: 420 }} elevation={3}>
        <CardContent sx={{ p: 4 }}>
          <Stack spacing={1} sx={{ mb: 3 }}>
            <Typography variant="h5" component="h1" fontWeight={700}>
              Institute Workforce Tracking
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Request an account — the administrator will review your request.
            </Typography>
          </Stack>

          {submitted ? (
            <Stack spacing={2.5}>
              <Alert severity="success">
                Registration submitted. You will be able to sign in once the
                administrator approves your request.
              </Alert>
              <Button
                component={RouterLink}
                to="/login"
                variant="contained"
                size="large"
                fullWidth
              >
                Back to Sign In
              </Button>
            </Stack>
          ) : (
            <form onSubmit={handleSubmit} noValidate>
              <Stack spacing={2.5}>
                {error && <Alert severity="error">{error}</Alert>}

                <TextField
                  label="Full Name"
                  value={form.fullName}
                  onChange={handleChange('fullName')}
                  required
                  autoFocus
                  fullWidth
                  autoComplete="name"
                  disabled={submitting}
                />

                <TextField
                  label="Email"
                  type="email"
                  value={form.email}
                  onChange={handleChange('email')}
                  required
                  fullWidth
                  autoComplete="email"
                  disabled={submitting}
                />

                <TextField
                  label="Password"
                  type="password"
                  value={form.password}
                  onChange={handleChange('password')}
                  required
                  fullWidth
                  autoComplete="new-password"
                  helperText="At least 8 characters"
                  disabled={submitting}
                />

                <TextField
                  label="Phone Number (optional)"
                  value={form.phone}
                  onChange={handleChange('phone')}
                  fullWidth
                  autoComplete="tel"
                  helperText="Digits only with country code, e.g. 91XXXXXXXXXX"
                  disabled={submitting}
                />

                <TextField
                  label="Requested Role"
                  select
                  value={form.requestedRole}
                  onChange={handleChange('requestedRole')}
                  required
                  fullWidth
                  disabled={submitting}
                >
                  {REQUESTABLE_ROLES.map((role) => (
                    <MenuItem key={role.value} value={role.value}>
                      {role.label}
                    </MenuItem>
                  ))}
                </TextField>

                <Button
                  type="submit"
                  variant="contained"
                  size="large"
                  fullWidth
                  disabled={submitting}
                >
                  {submitting ? (
                    <CircularProgress size={24} color="inherit" />
                  ) : (
                    'Request Account'
                  )}
                </Button>

                <Typography variant="body2" color="text.secondary" textAlign="center">
                  Already have an account?{' '}
                  <Link component={RouterLink} to="/login">
                    Sign in
                  </Link>
                </Typography>
              </Stack>
            </form>
          )}
        </CardContent>
      </Card>
    </Box>
  );
};

export default RegisterPage;
