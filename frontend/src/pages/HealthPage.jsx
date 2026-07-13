import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  CircularProgress,
  Stack,
  Typography,
} from '@mui/material';
import RefreshIcon from '@mui/icons-material/Refresh';
import MainLayout from '../layouts/MainLayout';
import StatusIndicator from '../components/StatusIndicator';
import { useHealth } from '../context/HealthContext';

/**
 * Screen that reports the live connectivity between this frontend and the
 * backend by displaying the result of the /api/health check.
 *
 * It demonstrates the three states every data-driven view must handle:
 * loading, error, and success. State and data come entirely from
 * HealthContext via `useHealth`; this page contains no fetching logic itself.
 */
const HealthPage = () => {
  const { status, loading, error, checkHealth } = useHealth();

  return (
    <MainLayout>
      <Stack spacing={3} alignItems="flex-start">
        <Box>
          <Typography variant="h4" component="h1" gutterBottom fontWeight={600}>
            System Health
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Live connectivity check between the web client and the backend API.
          </Typography>
        </Box>

        <Card sx={{ width: '100%', maxWidth: 480 }} elevation={2}>
          <CardContent>
            {loading ? (
              <Stack direction="row" spacing={2} alignItems="center">
                <CircularProgress size={24} />
                <Typography variant="body1">Checking backend status…</Typography>
              </Stack>
            ) : error ? (
              <Alert severity="error">{error}</Alert>
            ) : (
              <Stack direction="row" spacing={2} alignItems="center">
                <Typography variant="h6" component="span">
                  Backend status:
                </Typography>
                <StatusIndicator status={status} />
              </Stack>
            )}
          </CardContent>
        </Card>

        <Button
          variant="contained"
          startIcon={<RefreshIcon />}
          onClick={checkHealth}
          disabled={loading}
        >
          Re-check
        </Button>
      </Stack>
    </MainLayout>
  );
};

export default HealthPage;