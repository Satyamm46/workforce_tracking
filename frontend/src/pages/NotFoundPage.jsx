import { Box, Button, Stack, Typography } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import MainLayout from '../layouts/MainLayout';

/**
 * Screen shown when the user navigates to a URL that matches no route.
 *
 * A dedicated 404 page is part of a complete routing setup: it turns an
 * unknown URL into a clear, on-brand message with a way back, instead of a
 * blank screen. Rendered inside MainLayout so it keeps the app's navigation.
 */
const NotFoundPage = () => {
  const navigate = useNavigate();

  return (
    <MainLayout>
      <Stack spacing={2} alignItems="center" sx={{ mt: 8, textAlign: 'center' }}>
        <Typography variant="h2" component="h1" fontWeight={700} color="primary">
          404
        </Typography>
        <Typography variant="h5" component="p">
          Page not found
        </Typography>
        <Typography variant="body1" color="text.secondary" sx={{ maxWidth: 420 }}>
          The page you are looking for does not exist or may have been moved.
        </Typography>
        <Box>
          <Button variant="contained" onClick={() => navigate('/')}>
            Back to Home
          </Button>
        </Box>
      </Stack>
    </MainLayout>
  );
};

export default NotFoundPage;