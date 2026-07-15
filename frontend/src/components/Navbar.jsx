import { AppBar, Box, Button, Toolbar, Typography } from '@mui/material';
import BusinessCenterIcon from '@mui/icons-material/BusinessCenter';
import LogoutIcon from '@mui/icons-material/Logout';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useHealth } from '../context/HealthContext';
import { attendanceService } from '../services/attendanceService';
import NotificationBell from './NotificationBell';
import StatusIndicator from './StatusIndicator';

/** Roles permitted to use management screens — mirrors the backend rules. */
const MANAGER_ROLES = ['SUPER_ADMIN', 'ADMIN'];

/**
 * The nav map: one entry per link, visibility as a predicate. Adding a screen
 * is one array entry; the whole role-visibility policy is readable here.
 * Gating is UX only — the backend @PreAuthorize rules are the enforcement.
 */
const NAV_LINKS = [
  { label: 'Home', path: '/', show: (user) => !!user },
  { label: 'My Attendance', path: '/attendance', show: (user) => !!user },
  { label: 'My Leaves', path: '/leaves', show: (user) => !!user },
  { label: 'My Lectures', path: '/lectures', show: (user) => user?.role === 'TEACHER' },
  { label: 'Dashboard', path: '/admin/dashboard', show: (user) => MANAGER_ROLES.includes(user?.role) },
  { label: 'Attendance', path: '/admin/attendance', show: (user) => MANAGER_ROLES.includes(user?.role) },
  { label: 'Leaves', path: '/admin/leaves', show: (user) => MANAGER_ROLES.includes(user?.role) },
  { label: 'Lectures', path: '/admin/lectures', show: (user) => MANAGER_ROLES.includes(user?.role) },
  { label: 'Users', path: '/users', show: (user) => MANAGER_ROLES.includes(user?.role) },
];

/**
 * Top navigation bar: config-driven, role-gated links; notification bell;
 * backend health; the logged-in user; and logout with best-effort clock-out.
 */
const Navbar = () => {
  const { status } = useHealth();
  const { user, logout } = useAuth();
  const navigate = useNavigate();

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

        {/* Navigation links — driven by the NAV_LINKS map */}
        <Box sx={{ display: 'flex', gap: 1, ml: 4, flexGrow: 1 }}>
          {NAV_LINKS.filter((link) => link.show(user)).map((link) => (
            <Button key={link.path} color="inherit" onClick={() => navigate(link.path)}>
              {link.label}
            </Button>
          ))}
        </Box>

        {/* Right side: bell + status + user + logout */}
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          {user && <NotificationBell />}
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
