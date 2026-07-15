import { useEffect, useState } from 'react';
import { Alert, Box, Card, CardContent, Chip, Grid, Stack, Typography } from '@mui/material';
import { Client } from '@stomp/stompjs';
import MainLayout from '../layouts/MainLayout';
import { API_BASE_URL } from '../constants/appConfig';
import { dashboardService } from '../services/dashboardService';

const WS_URL = API_BASE_URL.replace(/^http/, 'ws') + '/ws';

/** One statistic tile. Presentational only. */
const StatCard = ({ label, value, color = 'text.primary' }) => (
  <Card elevation={2}>
    <CardContent>
      <Typography variant="caption" color="text.secondary">
        {label}
      </Typography>
      <Typography variant="h3" fontWeight={700} color={color}>
        {value}
      </Typography>
    </CardContent>
  </Card>
);

/**
 * The live workforce dashboard. Loads a snapshot over REST, then keeps it
 * fresh through the /topic/dashboard broadcast for as long as the page is
 * open — the subscription's lifecycle is the page's lifecycle.
 */
const DashboardPage = () => {
  const [stats, setStats] = useState(null);
  const [live, setLive] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    dashboardService
      .getStats()
      .then((response) => setStats(response.data))
      .catch((err) => setError(err?.message ?? 'Failed to load dashboard.'));

    const client = new Client({
      brokerURL: WS_URL,
      connectHeaders: {
        Authorization: `Bearer ${localStorage.getItem('accessToken')}`,
      },
      reconnectDelay: 5000,
      onConnect: () => {
        setLive(true);
        client.subscribe('/topic/dashboard', (message) => {
          setStats(JSON.parse(message.body));
        });
      },
      onWebSocketClose: () => setLive(false),
    });
    client.activate();

    return () => {
      client.deactivate();
    };
  }, []);

  return (
    <MainLayout>
      <Stack spacing={3}>
        <Stack direction="row" justifyContent="space-between" alignItems="center">
          <Box>
            <Typography variant="h4" component="h1" fontWeight={600}>
              Dashboard
            </Typography>
            <Typography variant="body1" color="text.secondary">
              Live workforce overview.
            </Typography>
          </Box>
          <Chip
            label={live ? 'Live' : 'Connecting…'}
            color={live ? 'success' : 'default'}
            size="small"
          />
        </Stack>

        {error && (
          <Alert severity="error" onClose={() => setError(null)}>
            {error}
          </Alert>
        )}

        {stats && (
          <Grid container spacing={2}>
            <Grid size={{ xs: 6, sm: 3 }}>
              <StatCard label="Total Employees" value={stats.totalEmployees} />
            </Grid>
            <Grid size={{ xs: 6, sm: 3 }}>
              <StatCard label="Online Now" value={stats.onlineCount} color="success.main" />
            </Grid>
            <Grid size={{ xs: 6, sm: 3 }}>
              <StatCard label="Working" value={stats.workingCount} color="success.main" />
            </Grid>
            <Grid size={{ xs: 6, sm: 3 }}>
              <StatCard label="On Break" value={stats.onBreakCount} color="warning.main" />
            </Grid>
            <Grid size={{ xs: 6, sm: 3 }}>
              <StatCard label="In Lectures" value={stats.liveLectureCount} color="info.main" />
            </Grid>
            <Grid size={{ xs: 6, sm: 3 }}>
              <StatCard label="On Leave" value={stats.onLeaveCount} color="info.main" />
            </Grid>
            <Grid size={{ xs: 6, sm: 3 }}>
              <StatCard label="Checked Out" value={stats.checkedOutCount} />
            </Grid>
            <Grid size={{ xs: 6, sm: 3 }}>
              <StatCard label="Absent Today" value={stats.absentCount} color="error.main" />
            </Grid>
          </Grid>
        )}
      </Stack>
    </MainLayout>
  );
};

export default DashboardPage;
