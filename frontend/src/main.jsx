import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { ThemeProvider, CssBaseline } from '@mui/material';
import { BrowserRouter } from 'react-router-dom';

import theme from './styles/theme';
import { AuthProvider } from './context/AuthContext';
import { HealthProvider } from './context/HealthContext';
import AppRoutes from './routes/AppRoutes';

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <BrowserRouter>
        <AuthProvider>
          <HealthProvider>
            <AppRoutes />
          </HealthProvider>
        </AuthProvider>
      </BrowserRouter>
    </ThemeProvider>
  </StrictMode>
);