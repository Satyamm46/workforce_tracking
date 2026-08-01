import { useState } from 'react';
import { Link as RouterLink } from 'react-router-dom';
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  CircularProgress,
  IconButton,
  InputAdornment,
  Link,
  Stack,
  Step,
  StepLabel,
  Stepper,
  TextField,
  Typography,
} from '@mui/material';
import Visibility from '@mui/icons-material/Visibility';
import VisibilityOff from '@mui/icons-material/VisibilityOff';
import { authService } from '../services/authService';

const STEPS = ['Your email', 'Reset password'];

/**
 * Password recovery screen — a two-step flow for users who can still reach
 * their inbox but no longer remember their password.
 *
 * Step 1 emails a one-time code to the address on the account. Step 2 takes
 * that code together with the new password; the backend only accepts the change
 * if the code matches, so controlling the inbox stands in for the forgotten
 * password.
 *
 * An address with no account fails step 1 outright and the error is shown in
 * place — the flow never advances to the code screen for an email that could
 * not have been sent one.
 */
const ForgotPasswordPage = () => {
  const [email, setEmail] = useState('');
  const [otp, setOtp] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);

  const [step, setStep] = useState(0); // 0 = email, 1 = code + new password
  const [error, setError] = useState(null);
  const [info, setInfo] = useState(null);
  const [busy, setBusy] = useState(false);
  const [done, setDone] = useState(false);

  const passwordsMatch = newPassword === confirmPassword;
  const canSubmitReset =
    otp.length === 6 && newPassword.length >= 8 && passwordsMatch;

  /** Step 1 → 2: request the code, then advance on success. */
  const handleSendCode = async (event) => {
    event.preventDefault();
    setError(null);
    setInfo(null);
    setBusy(true);
    try {
      await authService.forgotPassword({ email });
      setStep(1);
      setInfo(`We sent a 6-digit code to ${email}. Enter it below.`);
    } catch (err) {
      // Covers the mistyped-email case: the backend 404s with "No account
      // found with this email address", which is what surfaces here. The step
      // deliberately does not advance.
      setError(err?.message ?? 'Could not send the reset code.');
    } finally {
      setBusy(false);
    }
  };

  /** Step 2: submit the code with the chosen password. */
  const handleReset = async (event) => {
    event.preventDefault();
    setError(null);
    if (!passwordsMatch) {
      setError('The two passwords do not match.');
      return;
    }
    setBusy(true);
    try {
      await authService.resetPassword({ email, otp, newPassword });
      setDone(true);
    } catch (err) {
      setError(err?.message ?? 'Could not reset the password. Please try again.');
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
      await authService.forgotPassword({ email });
      setInfo(`A new code was sent to ${email}.`);
      setOtp('');
    } catch (err) {
      setError(err?.message ?? 'Could not resend the reset code.');
    } finally {
      setBusy(false);
    }
  };

  /** Eye button shared by both password fields. */
  const passwordToggle = {
    input: {
      endAdornment: (
        <InputAdornment position="end">
          <IconButton
            aria-label={showPassword ? 'Hide password' : 'Show password'}
            onClick={() => setShowPassword((visible) => !visible)}
            onMouseDown={(e) => e.preventDefault()}
            edge="end"
          >
            {showPassword ? <VisibilityOff /> : <Visibility />}
          </IconButton>
        </InputAdornment>
      ),
    },
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
      <Card sx={{ width: '100%', maxWidth: 440, borderRadius: 3 }} elevation={8}>
        <CardContent sx={{ p: { xs: 3, sm: 4 } }}>
          <Stack spacing={1} sx={{ mb: 3 }}>
            <Typography variant="h5" component="h1" fontWeight={700}>
              Reset your password
            </Typography>
            <Typography variant="body2" color="text.secondary">
              We&apos;ll email you a one-time code to confirm it&apos;s you.
            </Typography>
          </Stack>

          {done ? (
            <Stack spacing={2.5}>
              <Alert severity="success">
                Your password has been updated. Sign in with your new password.
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
                      label="Email"
                      type="email"
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
                      required
                      autoFocus
                      fullWidth
                      autoComplete="email"
                      helperText="The address you sign in with"
                      disabled={busy}
                    />

                    <Button
                      type="submit"
                      variant="contained"
                      size="large"
                      fullWidth
                      disabled={busy || !email}
                    >
                      {busy ? <CircularProgress size={24} color="inherit" /> : 'Send Code'}
                    </Button>

                    <Typography variant="body2" color="text.secondary" textAlign="center">
                      Remembered it?{' '}
                      <Link component={RouterLink} to="/login">
                        Back to sign in
                      </Link>
                    </Typography>
                  </Stack>
                </form>
              ) : (
                <form onSubmit={handleReset} noValidate>
                  <Stack spacing={2.5}>
                    <TextField
                      label="Reset Code"
                      value={otp}
                      onChange={(e) => setOtp(e.target.value.replace(/\D/g, '').slice(0, 6))}
                      required
                      autoFocus
                      fullWidth
                      slotProps={{ htmlInput: { inputMode: 'numeric', maxLength: 6 } }}
                      helperText="Enter the 6-digit code sent to your email"
                      disabled={busy}
                    />

                    <TextField
                      label="New Password"
                      type={showPassword ? 'text' : 'password'}
                      value={newPassword}
                      onChange={(e) => setNewPassword(e.target.value)}
                      required
                      fullWidth
                      autoComplete="new-password"
                      helperText="At least 8 characters, including a letter and a number"
                      disabled={busy}
                      slotProps={passwordToggle}
                    />

                    <TextField
                      label="Confirm New Password"
                      type={showPassword ? 'text' : 'password'}
                      value={confirmPassword}
                      onChange={(e) => setConfirmPassword(e.target.value)}
                      required
                      fullWidth
                      autoComplete="new-password"
                      error={confirmPassword.length > 0 && !passwordsMatch}
                      helperText={
                        confirmPassword.length > 0 && !passwordsMatch
                          ? 'The two passwords do not match'
                          : ' '
                      }
                      disabled={busy}
                      slotProps={passwordToggle}
                    />

                    <Button
                      type="submit"
                      variant="contained"
                      size="large"
                      fullWidth
                      disabled={busy || !canSubmitReset}
                    >
                      {busy ? <CircularProgress size={24} color="inherit" /> : 'Set New Password'}
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

export default ForgotPasswordPage;
