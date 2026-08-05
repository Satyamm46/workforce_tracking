import { useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Card,
  CardActionArea,
  CardContent,
  Chip,
  CircularProgress,
  Dialog,
  DialogContent,
  DialogTitle,
  Divider,
  Grid,
  IconButton,
  List,
  ListItem,
  ListItemText,
  Stack,
  Typography,
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import { Client } from '@stomp/stompjs';
import MainLayout from '../layouts/MainLayout';
import { API_BASE_URL } from '../constants/appConfig';
import { dashboardService } from '../services/dashboardService';
import { formatTime } from '../utils/formatters';

const WS_URL = API_BASE_URL.replace(/^http/, 'ws') + '/ws';

const ROLE_LABELS = {
  SUPER_ADMIN: 'Super Admin',
  ADMIN: 'Admin',
  EMPLOYEE: 'Employee',
};

/**
 * The tiles, in display order. `group` is the backend DashboardGroup this tile
 * expands to, so the number and the list it opens always come from one source.
 */
const TILES = [
  { label: 'Total Employees', field: 'totalEmployees', group: 'TOTAL' },
  { label: 'Online Now', field: 'onlineCount', group: 'ONLINE', color: 'success.main' },
  { label: 'Working', field: 'workingCount', group: 'WORKING', color: 'success.main' },
  { label: 'On Break', field: 'onBreakCount', group: 'ON_BREAK', color: 'warning.main' },
  { label: 'In Lectures', field: 'liveLectureCount', group: 'IN_LECTURE', color: 'info.main' },
  { label: 'On Leave', field: 'onLeaveCount', group: 'ON_LEAVE', color: 'info.main' },
  { label: 'Checked Out', field: 'checkedOutCount', group: 'CHECKED_OUT' },
  { label: 'Absent Today', field: 'absentCount', group: 'ABSENT', color: 'error.main' },
];

/** One statistic tile. Presentational only. `color` is a theme path used for
 *  the value text and a thin accent bar down the card's left edge. Clicking it
 *  opens the people behind the number. */
const StatCard = ({ label, value, color = 'text.primary', onClick }) => (
  <Card
    sx={{
      height: '100%',
      position: 'relative',
      overflow: 'hidden',
      transition: 'box-shadow 150ms ease, transform 150ms ease',
      '&:hover': { boxShadow: 6, transform: 'translateY(-2px)' },
      '&::before': {
        content: '""',
        position: 'absolute',
        left: 0,
        top: 0,
        bottom: 0,
        width: 4,
        bgcolor: color === 'text.primary' ? 'primary.main' : color,
        zIndex: 1,
      },
    }}
  >
    <CardActionArea onClick={onClick} sx={{ height: '100%' }}>
      <CardContent sx={{ pl: 2.5 }}>
        <Typography
          variant="caption"
          color="text.secondary"
          sx={{ textTransform: 'uppercase', fontWeight: 700, letterSpacing: 0.5 }}
        >
          {label}
        </Typography>
        <Typography variant="h3" fontWeight={700} color={color} sx={{ mt: 0.5 }}>
          {value}
        </Typography>
      </CardContent>
    </CardActionArea>
  </Card>
);

/**
 * The live workforce dashboard. Loads a snapshot over REST, then keeps it
 * fresh through the /topic/dashboard broadcast for as long as the page is
 * open — the subscription's lifecycle is the page's lifecycle.
 *
 * Every tile drills down: the counts answer "how many", and opening one
 * answers "who", which is the question an admin actually acts on.
 */
const DashboardPage = () => {
  const [stats, setStats] = useState(null);
  const [live, setLive] = useState(false);
  const [error, setError] = useState(null);

  // The open tile, or null when the drill-down dialog is closed. Its members
  // are fetched on demand — the broadcast carries counts only.
  const [openTile, setOpenTile] = useState(null);
  const [members, setMembers] = useState([]);
  const [membersLoading, setMembersLoading] = useState(false);
  const [membersError, setMembersError] = useState(null);

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

  const openDrillDown = (tile) => {
    setOpenTile(tile);
    setMembers([]);
    setMembersError(null);
    setMembersLoading(true);
    dashboardService
      .getMembers(tile.group)
      .then((response) => setMembers(response.data ?? []))
      .catch((err) => setMembersError(err?.message ?? 'Failed to load the list.'))
      .finally(() => setMembersLoading(false));
  };

  return (
    <MainLayout>
      <Stack spacing={3}>
        <Stack direction="row" justifyContent="space-between" alignItems="center">
          <Box>
            <Typography variant="h4" component="h1" fontWeight={600}>
              Dashboard
            </Typography>
            <Typography variant="body1" color="text.secondary">
              Live workforce overview. Tap any tile to see who it counts.
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
            {TILES.map((tile) => (
              <Grid key={tile.group} size={{ xs: 6, sm: 3 }}>
                <StatCard
                  label={tile.label}
                  value={stats[tile.field]}
                  color={tile.color}
                  onClick={() => openDrillDown(tile)}
                />
              </Grid>
            ))}
          </Grid>
        )}
      </Stack>

      <Dialog
        open={openTile !== null}
        onClose={() => setOpenTile(null)}
        fullWidth
        maxWidth="xs"
      >
        <DialogTitle sx={{ pr: 6 }}>
          {openTile?.label}
          <Typography variant="body2" color="text.secondary">
            {membersLoading ? 'Loading…' : `${members.length} ${members.length === 1 ? 'person' : 'people'}`}
          </Typography>
          <IconButton
            aria-label="Close"
            onClick={() => setOpenTile(null)}
            sx={{ position: 'absolute', right: 8, top: 8 }}
          >
            <CloseIcon />
          </IconButton>
        </DialogTitle>
        <DialogContent dividers sx={{ p: 0 }}>
          {membersLoading && (
            <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
              <CircularProgress size={28} />
            </Box>
          )}

          {!membersLoading && membersError && (
            <Alert severity="error" sx={{ m: 2 }}>
              {membersError}
            </Alert>
          )}

          {!membersLoading && !membersError && members.length === 0 && (
            <Typography variant="body2" color="text.secondary" sx={{ p: 3, textAlign: 'center' }}>
              Nobody right now.
            </Typography>
          )}

          {!membersLoading && !membersError && members.length > 0 && (
            <List disablePadding>
              {members.map((member, index) => (
                <Box key={`${member.userId}-${index}`}>
                  {index > 0 && <Divider component="li" />}
                  <ListItem sx={{ alignItems: 'flex-start' }}>
                    <ListItemText
                      primary={
                        <Stack direction="row" spacing={1} alignItems="center" flexWrap="wrap">
                          <Typography variant="body1" fontWeight={600}>
                            {member.fullName}
                          </Typography>
                          <Chip
                            label={ROLE_LABELS[member.role] ?? member.role}
                            size="small"
                            variant="outlined"
                          />
                        </Stack>
                      }
                      secondary={
                        member.since
                          ? `${member.status} · since ${formatTime(member.since)}`
                          : member.status
                      }
                    />
                  </ListItem>
                </Box>
              ))}
            </List>
          )}
        </DialogContent>
      </Dialog>
    </MainLayout>
  );
};

export default DashboardPage;
