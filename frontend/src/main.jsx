import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { ThemeProvider, CssBaseline } from '@mui/material';
import { BrowserRouter } from 'react-router-dom';

import theme from './styles/theme';
import { AuthProvider } from './context/AuthContext';
import { NotificationProvider } from './context/NotificationContext';
import { HealthProvider } from './context/HealthContext';
import AppRoutes from './routes/AppRoutes';

/**
 * Application entry point — the provider tree. Nesting order encodes
 * dependency: NotificationProvider consumes useAuth, so it must sit inside
 * AuthProvider. Exactly ONE BrowserRouter exists in the app.
 */
createRoot(document.getElementById('root')).render(
  <StrictMode>
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <BrowserRouter>
        <AuthProvider>
          <NotificationProvider>
            <HealthProvider>
              <AppRoutes />
            </HealthProvider>
          </NotificationProvider>
        </AuthProvider>
      </BrowserRouter>
    </ThemeProvider>
  </StrictMode>
);
