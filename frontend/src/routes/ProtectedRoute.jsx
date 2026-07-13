import { Navigate, Outlet } from 'react-router-dom';
import { Box, CircularProgress } from '@mui/material';
import { useAuth } from '../context/AuthContext';

/**
 * Guards routes that require authentication.
 *
 * While the session is being restored (loading), it shows a spinner rather
 * than making a premature decision. Once resolved: authenticated users see the
 * nested routes; unauthenticated users are redirected to the login page.
 */
const ProtectedRoute = () => {
  const { isAuthenticated, loading } = useAuth();

  // Session restoration is still in progress — decide nothing yet.
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

  // Not logged in → send to login, replacing history so Back doesn't loop.
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  // Authenticated → render whatever nested route matched.
  return <Outlet />;
};

export default ProtectedRoute;