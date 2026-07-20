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

// A soft, layered shadow scale — subtler and cooler than MUI's default black
// shadows, so cards read as gently lifted rather than heavy.
const softShadow = '0 1px 2px rgba(16, 24, 40, 0.06), 0 1px 3px rgba(16, 24, 40, 0.10)';
const softShadowMd = '0 4px 8px -2px rgba(16, 24, 40, 0.08), 0 2px 4px -2px rgba(16, 24, 40, 0.06)';
const softShadowLg = '0 12px 16px -4px rgba(16, 24, 40, 0.08), 0 4px 6px -2px rgba(16, 24, 40, 0.04)';

const theme = createTheme({
  palette: {
    mode: 'light',
    primary: {
      main: '#1565c0', // institute blue
      light: '#5e92f3',
      dark: '#003c8f',
      contrastText: '#ffffff',
    },
    secondary: {
      main: '#00897b', // teal accent
      light: '#4ebaaa',
      dark: '#005b4f',
      contrastText: '#ffffff',
    },
    background: {
      default: '#f1f4f9', // app background (used by MainLayout)
      paper: '#ffffff', // Card / surface background
    },
    success: { main: '#2e7d32', light: '#4caf50', dark: '#1b5e20' },
    warning: { main: '#ed6c02', light: '#ff9800', dark: '#e65100' },
    info: { main: '#0277bd', light: '#03a9f4', dark: '#01579b' },
    error: { main: '#c62828', light: '#ef5350', dark: '#8e0000' },
    text: {
      primary: '#1a2027',
      secondary: '#5a6a7a',
    },
    divider: 'rgba(16, 24, 40, 0.10)',
  },

  typography: {
    fontFamily: ['Inter', 'Roboto', 'Helvetica', 'Arial', 'sans-serif'].join(','),
    h4: { fontWeight: 700, letterSpacing: '-0.5px' },
    h5: { fontWeight: 700, letterSpacing: '-0.3px' },
    h6: { fontWeight: 600 },
    subtitle1: { fontWeight: 600 },
    subtitle2: { fontWeight: 600 },
    button: { fontWeight: 600 },
    caption: { letterSpacing: '0.2px' },
  },

  shape: {
    borderRadius: 10,
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
          borderRadius: 8,
          paddingTop: 6,
          paddingBottom: 6,
        },
        sizeLarge: {
          paddingTop: 10,
          paddingBottom: 10,
        },
      },
    },

    MuiCard: {
      defaultProps: { elevation: 0 },
      styleOverrides: {
        root: {
          border: '1px solid rgba(16, 24, 40, 0.08)',
          boxShadow: softShadow,
        },
      },
    },

    MuiPaper: {
      styleOverrides: {
        // Only tone down the outlined/flat surfaces; keep elevation utilities.
        rounded: { borderRadius: 12 },
        elevation1: { boxShadow: softShadow },
        elevation2: { boxShadow: softShadowMd },
        elevation3: { boxShadow: softShadowLg },
      },
    },

    MuiAppBar: {
      styleOverrides: {
        root: {
          backgroundImage: 'none',
        },
      },
    },

    MuiTableHead: {
      styleOverrides: {
        root: {
          '& .MuiTableCell-head': {
            backgroundColor: '#f7f9fc',
            color: '#5a6a7a',
            fontWeight: 700,
            fontSize: '0.75rem',
            textTransform: 'uppercase',
            letterSpacing: '0.5px',
            borderBottom: '1px solid rgba(16, 24, 40, 0.12)',
          },
        },
      },
    },

    MuiTableCell: {
      styleOverrides: {
        root: {
          borderBottom: '1px solid rgba(16, 24, 40, 0.06)',
        },
      },
    },

    MuiTableRow: {
      styleOverrides: {
        root: {
          '&:last-child .MuiTableCell-root': { borderBottom: 0 },
        },
      },
    },

    MuiChip: {
      styleOverrides: {
        root: { fontWeight: 600 },
        sizeSmall: { fontSize: '0.72rem' },
      },
    },

    MuiTextField: {
      defaultProps: { size: 'small' },
    },

    MuiAlert: {
      styleOverrides: {
        root: { borderRadius: 10 },
      },
    },

    MuiTooltip: {
      styleOverrides: {
        tooltip: {
          backgroundColor: '#1a2027',
          fontSize: '0.75rem',
          borderRadius: 6,
        },
      },
    },
  },
});

export default theme;
