import { useEffect, useState } from 'react';
import {
  Badge,
  Box,
  Button,
  Divider,
  IconButton,
  List,
  ListItem,
  ListItemText,
  Popover,
  Stack,
  Typography,
} from '@mui/material';
import NotificationsIcon from '@mui/icons-material/Notifications';
import NotificationsActiveIcon from '@mui/icons-material/NotificationsActive';
import DoneAllIcon from '@mui/icons-material/DoneAll';
import { useNotifications } from '../context/NotificationContext';
import { pushService } from '../services/pushService';
import { formatTime } from '../utils/formatters';

/**
 * The navbar bell: unread badge plus a popover panel of recent notifications
 * with a mark-all-read action. All state comes from NotificationContext.
 */
const NotificationBell = () => {
  const { notifications, unreadCount, markAllRead } = useNotifications();
  const [anchorEl, setAnchorEl] = useState(null);
  // null = checking, false = can offer enabling, true = already enabled
  const [pushEnabled, setPushEnabled] = useState(null);
  const [pushError, setPushError] = useState(null);

  const open = Boolean(anchorEl);

  useEffect(() => {
    if (!pushService.isPushSupported()) {
      setPushEnabled(true); // unsupported → simply don't offer the button
      return;
    }
    pushService
      .isSubscribed()
      .then((subscribed) => setPushEnabled(subscribed))
      .catch(() => setPushEnabled(true));
  }, []);

  const handleEnablePush = async () => {
    setPushError(null);
    try {
      await pushService.enablePush();
      setPushEnabled(true);
    } catch (err) {
      setPushError(err?.message ?? 'Could not enable push notifications.');
    }
  };

  const handleMarkAllRead = async () => {
    try {
      await markAllRead();
    } catch {
      // Non-fatal: the badge simply stays until the next successful attempt.
    }
  };

  return (
    <>
      <IconButton color="inherit" onClick={(e) => setAnchorEl(e.currentTarget)}>
        <Badge badgeContent={unreadCount} color="error" max={99}>
          <NotificationsIcon />
        </Badge>
      </IconButton>

      <Popover
        open={open}
        anchorEl={anchorEl}
        onClose={() => setAnchorEl(null)}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
        transformOrigin={{ vertical: 'top', horizontal: 'right' }}
      >
        <Box sx={{ width: 380 }}>
          <Stack
            direction="row"
            justifyContent="space-between"
            alignItems="center"
            sx={{ px: 2, py: 1.5 }}
          >
            <Typography variant="subtitle1" fontWeight={600}>
              Notifications
            </Typography>
            {unreadCount > 0 && (
              <Button size="small" startIcon={<DoneAllIcon />} onClick={handleMarkAllRead}>
                Mark all read
              </Button>
            )}
          </Stack>
          <Divider />

          {pushEnabled === false && (
            <>
              <Box sx={{ px: 2, py: 1 }}>
                <Button
                  size="small"
                  fullWidth
                  variant="outlined"
                  startIcon={<NotificationsActiveIcon />}
                  onClick={handleEnablePush}
                >
                  Enable notifications on this device
                </Button>
                {pushError && (
                  <Typography variant="caption" color="error">
                    {pushError}
                  </Typography>
                )}
              </Box>
              <Divider />
            </>
          )}

          {notifications.length === 0 ? (
            <Box sx={{ p: 4, textAlign: 'center' }}>
              <Typography variant="body2" color="text.secondary">
                No notifications yet.
              </Typography>
            </Box>
          ) : (
            <List dense sx={{ maxHeight: 420, overflowY: 'auto', py: 0 }}>
              {notifications.map((notification) => (
                <ListItem
                  key={notification.id}
                  divider
                  sx={{ bgcolor: notification.read ? 'transparent' : 'action.hover' }}
                >
                  <ListItemText
                    primary={notification.message}
                    secondary={formatTime(notification.createdAt)}
                    primaryTypographyProps={{
                      variant: 'body2',
                      fontWeight: notification.read ? 400 : 600,
                    }}
                  />
                </ListItem>
              ))}
            </List>
          )}
        </Box>
      </Popover>
    </>
  );
};

export default NotificationBell;
