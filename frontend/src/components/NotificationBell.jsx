import { useState } from 'react';
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
import DoneAllIcon from '@mui/icons-material/DoneAll';
import { useNotifications } from '../context/NotificationContext';
import { formatTime } from '../utils/formatters';

/**
 * The navbar bell: unread badge plus a popover panel of recent notifications
 * with a mark-all-read action. All state comes from NotificationContext.
 */
const NotificationBell = () => {
  const { notifications, unreadCount, markAllRead } = useNotifications();
  const [anchorEl, setAnchorEl] = useState(null);

  const open = Boolean(anchorEl);

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
