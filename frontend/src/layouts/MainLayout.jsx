import { useState } from 'react';
import {
  AppBar,
  Box,
  Button,
  Container,
  IconButton,
  Stack,
  Toolbar,
  Typography,
} from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu';
import LogoutIcon from '@mui/icons-material/Logout';
import { useLocation, useNavigate } from 'react-router-dom';
import Sidebar, { SIDEBAR_WIDTH } from '../components/Sidebar';
import NotificationBell from '../components/NotificationBell';
import StatusIndicator from '../components/StatusIndicator';
import { useAuth } from '../context/AuthContext';
import { useHealth } from '../context/HealthContext';
import { attendanceService } from '../services/attendanceService';
import { ALL_NAV_LINKS } from '../constants/navConfig';

/** Resolves the current route to its nav label, for the top-bar title. */
const usePageTitle = () => {
  const { pathname } = useLocation();
  if (pathname === '/') return 'Home';
  // Longest matching path wins (so /admin/attendance beats /admin).
  const match = ALL_NAV_LINKS
    .filter((link) => link.path !== '/' && pathname.startsWith(link.path))
    .sort((a, b) => b.path.length - a.path.length)[0];
  return match?.label ?? '';
};

/**
 * The primary layout shell shared by all main application screens.
 *
 * A permanent sidebar (collapsible on mobile) plus a slim top bar carrying the
 * page title and the session cluster (notifications, health, user, logout).
 * Page content is injected through `children`, so the frame is written once
 * here and every page reuses it.
 */
const MainLayout = ({ children }) => {
  const [mobileOpen, setMobileOpen] = useState(false);
  const { user, logout } = useAuth();
  const { status } = useHealth();
  const navigate = useNavigate();
  const title = usePageTitle();

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
    <Box sx={{ display: 'flex', minHeight: '100vh', bgcolor: 'background.default' }}>
      <Sidebar mobileOpen={mobileOpen} onClose={() => setMobileOpen(false)} />

      <Box
        sx={{
          flexGrow: 1,
          display: 'flex',
          flexDirection: 'column',
          width: { md: `calc(100% - ${SIDEBAR_WIDTH}px)` },
        }}
      >
        <AppBar
          position="sticky"
          elevation={0}
          color="inherit"
          sx={{
            bgcolor: 'background.paper',
            borderBottom: '1px solid',
            borderColor: 'divider',
          }}
        >
          <Toolbar sx={{ gap: 1 }}>
            <IconButton
              edge="start"
              onClick={() => setMobileOpen(true)}
              sx={{ display: { md: 'none' } }}
              aria-label="Open navigation"
            >
              <MenuIcon />
            </IconButton>

            <Typography variant="h6" component="h1" sx={{ flexGrow: 1, fontWeight: 700 }}>
              {title}
            </Typography>

            <Stack direction="row" alignItems="center" spacing={1.5}>
              {user && <NotificationBell />}
              <StatusIndicator status={status} />
              {user && (
                <>
                  <Box sx={{ textAlign: 'right', lineHeight: 1.15, display: { xs: 'none', sm: 'block' } }}>
                    <Typography variant="body2" fontWeight={600}>
                      {user.fullName}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      {user.role}
                    </Typography>
                  </Box>
                  <Button
                    color="inherit"
                    startIcon={<LogoutIcon />}
                    onClick={handleLogout}
                    sx={{ color: 'text.secondary' }}
                  >
                    Logout
                  </Button>
                </>
              )}
            </Stack>
          </Toolbar>
        </AppBar>

        <Container component="main" maxWidth="xl" sx={{ flexGrow: 1, py: 4 }}>
          {children}
        </Container>
      </Box>
    </Box>
  );
};

export default MainLayout;
