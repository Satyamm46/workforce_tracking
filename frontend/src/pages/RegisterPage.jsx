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
  Step,
  StepLabel,
  Stepper,
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

const STEPS = ['Your details', 'Verify email'];

/**
 * Public self-registration screen — a two-step flow.
 *
 * Step 1 collects the applicant's details and sends a one-time code to the
 * email address. Step 2 takes that code and submits the registration, which
 * only succeeds if the code matches — proving the applicant controls the
 * inbox, so fake or borrowed addresses cannot register. The account still
 * awaits Super Admin approval before sign-in works.
 */
const RegisterPage = () => {
  const [form, setForm] = useState({
    fullName: '',
    email: '',
    password: '',
    phone: '',
    requestedRole: 'EMPLOYEE',
  });
  const [otp, setOtp] = useState('');
  const [step, setStep] = useState(0); // 0 = details, 1 = verify
  const [error, setError] = useState(null);
  const [info, setInfo] = useState(null);
  const [busy, setBusy] = useState(false);
  const [submitted, setSubmitted] = useState(false);

  const handleChange = (field) => (event) => {
    setForm((prev) => ({ ...prev, [field]: event.target.value }));
  };

  /** Step 1 → 2: request the email code, then advance on success. */
  const handleSendCode = async (event) => {
    event.preventDefault();
    setError(null);
    setInfo(null);
    setBusy(true);
    try {
      await registrationService.sendOtp({ email: form.email });
      setStep(1);
      setInfo(`We sent a 6-digit code to ${form.email}. Enter it below to finish.`);
    } catch (err) {
      setError(err?.message ?? 'Could not send the verification code.');
    } finally {
      setBusy(false);
    }
  };

  /** Step 2: submit the registration with the code. */
  const handleRegister = async (event) => {
    event.preventDefault();
    setError(null);
    setBusy(true);
    try {
      await registrationService.register({ ...form, otp });
      setSubmitted(true);
    } catch (err) {
      setError(err?.message ?? 'Registration failed. Please try again.');
    } finally {
      setBusy(false);
    }
  };

  /** Re-send a fresh code (also used if the first didn't arrive). */
  const handleResend = async () => {
    setError(null);
    setInfo(null);
    setBusy(true);
    try {
      await registrationService.sendOtp({ email: form.email });
      setInfo(`A new code was sent to ${form.email}.`);
    } catch (err) {
      setError(err?.message ?? 'Could not resend the verification code.');
    } finally {
      setBusy(false);
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
      <Card sx={{ width: '100%', maxWidth: 440 }} elevation={3}>
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
            <>
              <Stepper activeStep={step} sx={{ mb: 3 }}>
                {STEPS.map((label) => (
                  <Step key={label}>
                    <StepLabel>{label}</StepLabel>
                  </Step>
                ))}
              </Stepper>

              {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
              {info && <Alert severity="info" sx={{ mb: 2 }}>{info}</Alert>}

              {step === 0 ? (
                <form onSubmit={handleSendCode} noValidate>
                  <Stack spacing={2.5}>
                    <TextField
                      label="Full Name"
                      value={form.fullName}
                      onChange={handleChange('fullName')}
                      required
                      autoFocus
                      fullWidth
                      autoComplete="name"
                      disabled={busy}
                    />

                    <TextField
                      label="Email"
                      type="email"
                      value={form.email}
                      onChange={handleChange('email')}
                      required
                      fullWidth
                      autoComplete="email"
                      helperText="A verification code will be sent here"
                      disabled={busy}
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
                      disabled={busy}
                    />

                    <TextField
                      label="Phone Number"
                      value={form.phone}
                      onChange={handleChange('phone')}
                      required
                      fullWidth
                      autoComplete="tel"
                      helperText="Digits only with country code, e.g. 91XXXXXXXXXX"
                      disabled={busy}
                    />

                    <TextField
                      label="Requested Role"
                      select
                      value={form.requestedRole}
                      onChange={handleChange('requestedRole')}
                      required
                      fullWidth
                      disabled={busy}
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
                      disabled={busy}
                    >
                      {busy ? <CircularProgress size={24} color="inherit" /> : 'Send Code'}
                    </Button>

                    <Typography variant="body2" color="text.secondary" textAlign="center">
                      Already have an account?{' '}
                      <Link component={RouterLink} to="/login">
                        Sign in
                      </Link>
                    </Typography>
                  </Stack>
                </form>
              ) : (
                <form onSubmit={handleRegister} noValidate>
                  <Stack spacing={2.5}>
                    <TextField
                      label="Verification Code"
                      value={otp}
                      onChange={(e) => setOtp(e.target.value.replace(/\D/g, '').slice(0, 6))}
                      required
                      autoFocus
                      fullWidth
                      inputProps={{ inputMode: 'numeric', maxLength: 6 }}
                      helperText="Enter the 6-digit code sent to your email"
                      disabled={busy}
                    />

                    <Button
                      type="submit"
                      variant="contained"
                      size="large"
                      fullWidth
                      disabled={busy || otp.length !== 6}
                    >
                      {busy ? <CircularProgress size={24} color="inherit" /> : 'Create Account'}
                    </Button>

                    <Stack direction="row" justifyContent="space-between">
                      <Button
                        size="small"
                        onClick={() => { setStep(0); setError(null); setInfo(null); }}
                        disabled={busy}
                      >
                        Back
                      </Button>
                      <Button size="small" onClick={handleResend} disabled={busy}>
                        Resend code
                      </Button>
                    </Stack>
                  </Stack>
                </form>
              )}
            </>
          )}
        </CardContent>
      </Card>
    </Box>
  );
};

export default RegisterPage;
