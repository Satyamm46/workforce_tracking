import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  CircularProgress,
  MenuItem,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import MainLayout from '../layouts/MainLayout';
import { userService } from '../services/userService';

/** Roles an admin can assign, matching the backend Role enum. */
const ROLE_OPTIONS = [
  { value: 'EMPLOYEE', label: 'Employee' },
  { value: 'TEACHER', label: 'Teacher' },
  { value: 'ADMIN', label: 'Admin' },
  { value: 'SUPER_ADMIN', label: 'Super Admin' },
];

const INITIAL_FORM = {
  fullName: '',
  email: '',
  password: '',
  role: 'EMPLOYEE',
};

/**
 * Admin form for creating a new user. Uses controlled inputs, submits via the
 * user service, shows field-level and general errors, and returns to the users
 * list on success.
 */
const CreateUserPage = () => {
  const navigate = useNavigate();

  const [form, setForm] = useState(INITIAL_FORM);
  const [error, setError] = useState(null);
  const [fieldErrors, setFieldErrors] = useState([]);
  const [submitting, setSubmitting] = useState(false);

  const handleChange = (field) => (event) => {
    setForm((prev) => ({ ...prev, [field]: event.target.value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError(null);
    setFieldErrors([]);
    setSubmitting(true);

    try {
      await userService.createUser(form);
      navigate('/users');
    } catch (err) {
      // Field-level validation details, if the backend returned any.
      setFieldErrors(err?.details ?? []);
      setError(err?.message ?? 'Failed to create user.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <MainLayout>
      <Stack spacing={3} sx={{ maxWidth: 560 }}>
        <Button
          startIcon={<ArrowBackIcon />}
          onClick={() => navigate('/users')}
          sx={{ alignSelf: 'flex-start' }}
        >
          Back to Users
        </Button>

        <Box>
          <Typography variant="h4" component="h1" fontWeight={600}>
            Add User
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Create an account for an employee or teacher.
          </Typography>
        </Box>

        <Card elevation={2}>
          <CardContent sx={{ p: 4 }}>
            <form onSubmit={handleSubmit} noValidate>
              <Stack spacing={2.5}>
                {error && <Alert severity="error">{error}</Alert>}

                {fieldErrors.length > 0 && (
                  <Alert severity="error">
                    <ul style={{ margin: 0, paddingLeft: '1.2rem' }}>
                      {fieldErrors.map((detail) => (
                        <li key={detail}>{detail}</li>
                      ))}
                    </ul>
                  </Alert>
                )}

                <TextField
                  label="Full Name"
                  value={form.fullName}
                  onChange={handleChange('fullName')}
                  required
                  fullWidth
                  autoFocus
                  disabled={submitting}
                />

                <TextField
                  label="Email"
                  type="email"
                  value={form.email}
                  onChange={handleChange('email')}
                  required
                  fullWidth
                  disabled={submitting}
                />

                <TextField
                  label="Temporary Password"
                  type="password"
                  value={form.password}
                  onChange={handleChange('password')}
                  required
                  fullWidth
                  disabled={submitting}
                  helperText="At least 8 characters. The user can change it later."
                />

                <TextField
                  label="Role"
                  select
                  value={form.role}
                  onChange={handleChange('role')}
                  required
                  fullWidth
                  disabled={submitting}
                >
                  {ROLE_OPTIONS.map((option) => (
                    <MenuItem key={option.value} value={option.value}>
                      {option.label}
                    </MenuItem>
                  ))}
                </TextField>

                <Box sx={{ display: 'flex', gap: 2, justifyContent: 'flex-end' }}>
                  <Button onClick={() => navigate('/users')} disabled={submitting}>
                    Cancel
                  </Button>
                  <Button type="submit" variant="contained" disabled={submitting}>
                    {submitting ? <CircularProgress size={24} color="inherit" /> : 'Create User'}
                  </Button>
                </Box>
              </Stack>
            </form>
          </CardContent>
        </Card>
      </Stack>
    </MainLayout>
  );
};

export default CreateUserPage;