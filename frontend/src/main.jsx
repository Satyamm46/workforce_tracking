import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { ThemeProvider, CssBaseline } from '@mui/material';
import { BrowserRouter } from 'react-router-dom';

import theme from './styles/theme';
import { HealthProvider } from './context/HealthContext';
import AppRoutes from './routes/AppRoutes';

/**
 * Application entry point.
 *
 * This is where the provider tree is assembled. The nesting ORDER matters:
 * each provider makes its capability available to everything rendered inside
 * it, so cross-cutting providers wrap the more specific ones.
 *
 *   ThemeProvider   -> MUI theme + styling available everywhere
 *     CssBaseline   -> normalizes browser default styles
 *       BrowserRouter -> enables URL-based routing for the whole tree
 *         HealthProvider -> app-wide backend-health state
 *           AppRoutes    -> the actual screens
 */
createRoot(document.getElementById('root')).render(
  <StrictMode>
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <BrowserRouter>
        <HealthProvider>
          <AppRoutes />
        </HealthProvider>
      </BrowserRouter>
    </ThemeProvider>
  </StrictMode>
);