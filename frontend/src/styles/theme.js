import { createTheme } from '@mui/material/styles';

/**
 * The application's Material UI theme — a single, central design system.
 *
 * Defining colours, typography, and component defaults here (rather than
 * styling each component ad hoc) means the entire UI stays visually
 * consistent, and a rebrand is a change in ONE file. Every MUI component and
 * every `sx` reference to `primary`, `background.default`, etc. resolves
 * against these values.
 */
const theme = createTheme({
  palette: {
    mode: 'light',
    primary: {
      main: '#1565c0', // institute blue
    },
    secondary: {
      main: '#00897b', // teal accent
    },
    background: {
      default: '#f4f6f8', // app background (used by MainLayout)
      paper: '#ffffff', // Card / surface background
    },
    success: {
      main: '#2e7d32', // health "UP"
    },
    error: {
      main: '#c62828', // health "DOWN" / error alerts
    },
  },

  typography: {
    fontFamily: ['Roboto', 'Helvetica', 'Arial', 'sans-serif'].join(','),
    h4: { fontWeight: 600 },
    h6: { fontWeight: 600 },
  },

  shape: {
    borderRadius: 8,
  },

  components: {
    // Application-wide default props / styles for MUI components.
    MuiButton: {
      defaultProps: {
        disableElevation: true,
      },
      styleOverrides: {
        root: {
          textTransform: 'none', // keep button labels as written, not UPPERCASE
        },
      },
    },
  },
});

export default theme;
