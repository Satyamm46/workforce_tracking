import { AppBar, Box, Button, Toolbar, Typography } from '@mui/material';
import BusinessCenterIcon from '@mui/icons-material/BusinessCenter';
import LogoutIcon from '@mui/icons-material/Logout';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useHealth } from '../context/HealthContext';
import { attendanceService } from '../services/attendanceService';
import StatusIndicator from './StatusIndicator';

const MANAGER_ROLES = ['SUPER_ADMIN', 'ADMIN'];

const Navbar = () => {
  const { status } = useHealth();
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const isManager = user && MANAGER_ROLES.includes(user.role);

  /**
   * Best-effort clock-out before ending the session. Runs while the token is
   * still present; any failure (already clocked out, no record, offline) is
   * ignored — logging out must never be blocked by attendance.
   */
  const handleLogout = async () => {
    try {
      await attendanceService.clockOut();
    } catch {
      // Intentionally ignored — logout proceeds regardless.
    }
    logout();
    navigate('/login', { replace: true });
  };

  return (
    <AppBar position="static" elevation={1}>
      <Toolbar>
        <BusinessCenterIcon sx={{ mr: 1.5 }} />
        <Typography variant="h6" component="div" sx={{ fontWeight: 600 }}>
          Institute Workforce Tracking
        </Typography>

        <Box sx={{ display: 'flex', gap: 1, ml: 4, flexGrow: 1 }}>
          {user && (
            <>
              <Button color="inherit" onClick={() => navigate('/')}>Home</Button>
              <Button color="inherit" onClick={() => navigate('/attendance')}>My Attendance</Button>
            </>
          )}
          {isManager && (
            <>
              <Button color="inherit" onClick={() => navigate('/admin/attendance')}>Attendance</Button>
              <Button color="inherit" onClick={() => navigate('/users')}>Users</Button>
            </>
          )}
        </Box>

        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <StatusIndicator status={status} />
          {user && (
            <>
              <Box sx={{ textAlign: 'right', lineHeight: 1.15 }}>
                <Typography variant="body2" fontWeight={600}>{user.fullName}</Typography>
                <Typography variant="caption" sx={{ opacity: 0.85 }}>{user.role}</Typography>
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