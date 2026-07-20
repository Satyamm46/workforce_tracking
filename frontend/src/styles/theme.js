import { createTheme } from '@mui/material/styles';

/**
 * The application's Material UI theme — a single, central design system.
 *
 * Defining colours, typography, and component defaults here (rather than
 * styling each component ad hoc) means the entire UI stays visually
 * consistent, and a rebrand is a change in ONE file. Every MUI component and
 * every `sx` reference to `primary`, `background.default`, etc. resolves
 * against these values.
 *
 * Type and palette matched to nalintutorials.com: Playfair Display (an
 * elegant serif) for headings, and Cabinet Grotesk (a clean geometric sans,
 * with Inter as fallback) for body text and data tables. Loaded in
 * index.html. Colours mirror the site — deep navy primary, gold accent, over
 * a light blue-tinted background.
 */

const SANS = ['"Cabinet Grotesk"', 'Inter', 'Roboto', 'Helvetica', 'Arial', 'sans-serif'].join(',');
const SERIF = ['"Playfair Display"', 'Georgia', '"Times New Roman"', 'serif'].join(',');

// A soft, layered shadow scale — subtler and cooler than MUI's default black
// shadows, so cards read as gently lifted rather than heavy. Tuned to the
// navy base so shadows read as a cool tint, not flat grey.
const softShadow = '0 1px 2px rgba(15, 35, 71, 0.06), 0 1px 3px rgba(15, 35, 71, 0.10)';
const softShadowMd = '0 4px 10px -3px rgba(15, 35, 71, 0.12), 0 2px 5px -2px rgba(15, 35, 71, 0.07)';
const softShadowLg = '0 14px 24px -6px rgba(15, 35, 71, 0.16), 0 6px 10px -4px rgba(15, 35, 71, 0.06)';

const theme = createTheme({
  palette: {
    mode: 'light',
    primary: {
      main: '#0f2347', // Nalin deep navy
      light: '#1a3a6b',
      dark: '#020a16',
      contrastText: '#ffffff',
    },
    secondary: {
      main: '#8B6200', // Nalin gold/bronze accent
      light: '#BA7517',
      dark: '#5c4100',
      contrastText: '#ffffff',
    },
    background: {
      default: '#E8F4F8', // Nalin light blue-tinted app background
      paper: '#ffffff', // Card / surface background
    },
    success: { main: '#1E8449', light: '#28a745', dark: '#14532d' },
    warning: { main: '#B87415', light: '#d68a1e', dark: '#8a5610' },
    info: { main: '#1A5276', light: '#3D7A9A', dark: '#123c56' },
    error: { main: '#8B0000', light: '#C0392B', dark: '#5c0000' },
    text: {
      primary: '#0f2347',
      secondary: '#4a5a6a',
    },
    divider: 'rgba(15, 35, 71, 0.12)',
  },

  typography: {
    fontFamily: SANS,
    // Headings use the serif for a classic feel.
    h1: { fontFamily: SERIF, fontWeight: 700, letterSpacing: '-0.5px' },
    h2: { fontFamily: SERIF, fontWeight: 700, letterSpacing: '-0.5px' },
    h3: { fontFamily: SERIF, fontWeight: 700, letterSpacing: '-0.3px' },
    h4: { fontFamily: SERIF, fontWeight: 700, letterSpacing: '-0.3px' },
    h5: { fontFamily: SERIF, fontWeight: 600, letterSpacing: '-0.2px' },
    h6: { fontFamily: SERIF, fontWeight: 600 },
    subtitle1: { fontWeight: 600 },
    subtitle2: { fontWeight: 600 },
    body1: { letterSpacing: '0.1px' },
    body2: { letterSpacing: '0.1px' },
    button: { fontWeight: 600, letterSpacing: '0.2px' },
    caption: { letterSpacing: '0.2px' },
  },

  shape: {
    borderRadius: 10,
  },

  components: {
    // Global baseline: font smoothing for crisper text, and a subtle
    // scrollbar that matches the palette rather than the OS default.
    MuiCssBaseline: {
      styleOverrides: {
        html: {
          WebkitFontSmoothing: 'antialiased',
          MozOsxFontSmoothing: 'grayscale',
          textRendering: 'optimizeLegibility',
        },
        body: {
          backgroundColor: '#eaf4f8',
        },
        '*::-webkit-scrollbar': { width: 10, height: 10 },
        '*::-webkit-scrollbar-thumb': {
          backgroundColor: 'rgba(15, 35, 71, 0.22)',
          borderRadius: 8,
          border: '2px solid transparent',
          backgroundClip: 'content-box',
        },
        '*::-webkit-scrollbar-thumb:hover': {
          backgroundColor: 'rgba(15, 35, 71, 0.40)',
        },
      },
    },

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
          transition: 'background-color 120ms ease, box-shadow 120ms ease, border-color 120ms ease',
        },
        sizeLarge: {
          paddingTop: 10,
          paddingBottom: 10,
        },
        containedPrimary: {
          '&:hover': { boxShadow: softShadowMd },
        },
      },
    },

    MuiCard: {
      defaultProps: { elevation: 0 },
      styleOverrides: {
        root: {
          border: '1px solid rgba(23, 33, 55, 0.08)',
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
            backgroundColor: '#e8f4f8',
            color: '#1a5276',
            fontFamily: SANS,
            fontWeight: 700,
            fontSize: '0.75rem',
            textTransform: 'uppercase',
            letterSpacing: '0.5px',
            borderBottom: '1px solid rgba(23, 33, 55, 0.12)',
          },
        },
      },
    },

    MuiTableCell: {
      styleOverrides: {
        root: {
          borderBottom: '1px solid rgba(23, 33, 55, 0.06)',
        },
      },
    },

    MuiTableRow: {
      styleOverrides: {
        root: {
          transition: 'background-color 100ms ease',
          '&:last-child .MuiTableCell-root': { borderBottom: 0 },
          '&.MuiTableRow-hover:hover': {
            backgroundColor: 'rgba(31, 58, 95, 0.04)',
          },
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

    MuiOutlinedInput: {
      styleOverrides: {
        root: {
          borderRadius: 8,
        },
      },
    },

    MuiAlert: {
      styleOverrides: {
        root: { borderRadius: 10, alignItems: 'center' },
      },
    },

    MuiTooltip: {
      styleOverrides: {
        tooltip: {
          backgroundColor: '#0f2347',
          fontSize: '0.75rem',
          borderRadius: 6,
          padding: '6px 10px',
        },
      },
    },

    MuiListItemButton: {
      styleOverrides: {
        root: {
          borderRadius: 8,
        },
      },
    },
  },
});

export default theme;
