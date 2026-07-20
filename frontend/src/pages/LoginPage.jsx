import { useState } from 'react';
import { Link as RouterLink, useNavigate } from 'react-router-dom';
import {
  Alert,
  Avatar,
  Box,
  Button,
  Card,
  CardContent,
  CircularProgress,
  Link,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import BusinessCenterIcon from '@mui/icons-material/BusinessCenter';
import { useAuth } from '../context/AuthContext';

/**
 * Login screen.
 *
 * A controlled form that submits credentials via the auth context. It manages
 * three interaction states — idle, submitting, and error — and redirects to the
 * home route on success. Contains no HTTP logic itself; all of that lives in
 * the auth context and service.
 */
const LoginPage = () => {
  const { login } = useAuth();
  const navigate = useNavigate();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError(null);
    setSubmitting(true);

    try {
      await login({ email, password });
      navigate('/', { replace: true });
    } catch (err) {
      // err is the normalized error from apiClient's interceptor.
      setError(err?.message ?? 'Login failed. Please try again.');
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
        background: (theme) =>
          `linear-gradient(135deg, ${theme.palette.primary.dark} 0%, ${theme.palette.primary.main} 55%, ${theme.palette.secondary.main} 100%)`,
        p: 2,
      }}
    >
      <Card sx={{ width: '100%', maxWidth: 420, borderRadius: 3 }} elevation={8}>
        <CardContent sx={{ p: { xs: 3, sm: 4 } }}>
          <Stack spacing={1.5} alignItems="center" sx={{ mb: 3 }}>
            <Avatar sx={{ bgcolor: 'primary.main', width: 56, height: 56 }}>
              <BusinessCenterIcon fontSize="large" />
            </Avatar>
            <Typography variant="h5" component="h1" fontWeight={700} textAlign="center">
              Institute Workforce Tracking
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Sign in to continue
            </Typography>
          </Stack>

          <form onSubmit={handleSubmit} noValidate>
            <Stack spacing={2.5}>
              {error && <Alert severity="error">{error}</Alert>}

              <TextField
                label="Email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                autoFocus
                fullWidth
                autoComplete="email"
                disabled={submitting}
              />

              <TextField
                label="Password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                fullWidth
                autoComplete="current-password"
                disabled={submitting}
              />

              <Button
                type="submit"
                variant="contained"
                size="large"
                fullWidth
                disabled={submitting}
              >
                {submitting ? <CircularProgress size={24} color="inherit" /> : 'Sign In'}
              </Button>

              <Typography variant="body2" color="text.secondary" textAlign="center">
                Don&apos;t have an account?{' '}
                <Link component={RouterLink} to="/register">
                  Request one
                </Link>
              </Typography>
            </Stack>
          </form>
        </CardContent>
      </Card>
    </Box>
  );
};

export default LoginPage;