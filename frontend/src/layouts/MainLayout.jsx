import { Box, Container } from '@mui/material';
import Navbar from '../components/Navbar';

/**
 * The primary layout shell shared by all main application screens.
 *
 * Renders the persistent Navbar and a centred content container. Page content
 * is injected through the `children` prop, so the frame is written once here
 * and every page reuses it — the UI equivalent of a shared template.
 */
const MainLayout = ({ children }) => {
  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        flexDirection: 'column',
        bgcolor: 'background.default',
      }}
    >
      <Navbar />

      <Container
        component="main"
        maxWidth="lg"
        sx={{ flexGrow: 1, py: 4 }}
      >
        {children}
      </Container>
    </Box>
  );
};

export default MainLayout;