import { AppBar, Box, Button, Toolbar, Typography } from '@mui/material';
import BusinessCenterIcon from '@mui/icons-material/BusinessCenter';
import LogoutIcon from '@mui/icons-material/Logout';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useHealth } from '../context/HealthContext';
import StatusIndicator from './StatusIndicator';

/**
 * Top navigation bar. Shows app identity, backend status, and — when a user is
 * logged in — their name, role, and a logout action. Reads both auth and
 * health state from context (no props drilled in).
 */
const Navbar = () => {
  const { status } = useHealth();
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login', { replace: true });
  };

  return (
    <AppBar position="static" elevation={1}>
      <Toolbar>
        <BusinessCenterIcon sx={{ mr: 1.5 }} />

        <Typography variant="h6" component="div" sx={{ flexGrow: 1, fontWeight: 600 }}>
          Institute Workforce Tracking
        </Typography>

        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <StatusIndicator status={status} />

          {user && (
            <>
              <Box sx={{ textAlign: 'right', lineHeight: 1.15 }}>
                <Typography variant="body2" fontWeight={600}>
                  {user.fullName}
                </Typography>
                <Typography variant="caption" sx={{ opacity: 0.85 }}>
                  {user.role}
                </Typography>
              </Box>
              <Button color="inherit" startIcon={<LogoutIcon />} onClick={handleLogout}>
                Logout
              </Button>
            </>
          )}
        </Box>
      </Toolbar>
    </AppBar>
  );
};

export default Navbar;