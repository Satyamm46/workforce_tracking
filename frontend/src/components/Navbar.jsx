import { AppBar, Box, Toolbar, Typography } from '@mui/material';
import BusinessCenterIcon from '@mui/icons-material/BusinessCenter';
import { useHealth } from '../context/HealthContext';
import StatusIndicator from './StatusIndicator';

/**
 * The application's top navigation bar.
 *
 * Displays the app identity and a live backend-status indicator. It reads the
 * health status from HealthContext via the `useHealth` hook — demonstrating a
 * component consuming app-wide state without any props being drilled into it.
 */
const Navbar = () => {
  const { status } = useHealth();

  return (
    <AppBar position="static" elevation={1}>
      <Toolbar>
        <BusinessCenterIcon sx={{ mr: 1.5 }} />

        <Typography variant="h6" component="div" sx={{ flexGrow: 1, fontWeight: 600 }}>
          Institute Workforce Tracking
        </Typography>

        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <Typography variant="body2" sx={{ opacity: 0.85 }}>
            Backend
          </Typography>
          <StatusIndicator status={status} />
        </Box>
      </Toolbar>
    </AppBar>
  );
};

export default Navbar;