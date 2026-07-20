import { Navigate, Outlet } from 'react-router-dom';
import { Box, CircularProgress } from '@mui/material';
import { useAuth } from '../context/AuthContext';

/**
 * Guards routes that require authentication, and optionally a role.
 *
 * While the session is being restored, shows a spinner. Unauthenticated users
 * are sent to login. When `allowedRoles` is given, a signed-in user whose role
 * is not in the list is redirected home — this stops, for example, a teacher
 * reaching /attendance by typing the URL. Backend @PreAuthorize rules remain
 * the real enforcement; this is UX-level gating that mirrors the nav.
 */
const ProtectedRoute = ({ allowedRoles }) => {
  const { isAuthenticated, loading, user } = useAuth();

  if (loading) {
    return (
      <Box
        sx={{
          minHeight: '100vh',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
        }}
      >
        <CircularProgress />
      </Box>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles && !allowedRoles.includes(user?.role)) {
    return <Navigate to="/" replace />;
  }

  return <Outlet />;
};

export default ProtectedRoute;
