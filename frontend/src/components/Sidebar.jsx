import {
  Box,
  Divider,
  Drawer,
  List,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Toolbar,
  Typography,
} from '@mui/material';
import BusinessCenterIcon from '@mui/icons-material/BusinessCenter';
import { useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { NAV_SECTIONS } from '../constants/navConfig';

/** Width of the permanent sidebar, in pixels. Shared with the layout. */
export const SIDEBAR_WIDTH = 260;

/**
 * The application sidebar: a branded header, then role-gated navigation grouped
 * into sections. Rendered as a permanent drawer on desktop and a temporary
 * (overlay) drawer on mobile — both share this inner content.
 */
const SidebarContent = ({ onNavigate }) => {
  const { user } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();

  const go = (path) => {
    navigate(path);
    if (onNavigate) onNavigate();
  };

  return (
    <>
      <Toolbar
        sx={{
          gap: 1.25,
          px: 2.5,
          color: 'primary.main',
        }}
      >
        <BusinessCenterIcon />
        <Box sx={{ lineHeight: 1.1 }}>
          <Typography variant="subtitle1" fontWeight={700} sx={{ lineHeight: 1.2 }}>
            Workforce
          </Typography>
          <Typography variant="caption" color="text.secondary">
            Institute Tracking
          </Typography>
        </Box>
      </Toolbar>
      <Divider />

      <Box sx={{ overflowY: 'auto', flexGrow: 1, py: 1 }}>
        {NAV_SECTIONS.map((section, index) => {
          const visibleLinks = section.links.filter((link) => link.show(user));
          if (visibleLinks.length === 0) return null;

          return (
            <Box key={section.heading ?? `section-${index}`} sx={{ mb: 0.5 }}>
              {section.heading && (
                <Typography
                  variant="overline"
                  sx={{ px: 3, color: 'text.secondary', fontWeight: 700, letterSpacing: 0.8 }}
                >
                  {section.heading}
                </Typography>
              )}
              <List dense sx={{ px: 1.5, py: 0.5 }}>
                {visibleLinks.map((link) => {
                  const Icon = link.icon;
                  const active =
                    link.path === '/'
                      ? location.pathname === '/'
                      : location.pathname.startsWith(link.path);
                  return (
                    <ListItemButton
                      key={link.path}
                      selected={active}
                      onClick={() => go(link.path)}
                      sx={{
                        borderRadius: 2,
                        mb: 0.25,
                        '&.Mui-selected': {
                          bgcolor: 'primary.main',
                          color: 'primary.contrastText',
                          '&:hover': { bgcolor: 'primary.dark' },
                          '& .MuiListItemIcon-root': { color: 'primary.contrastText' },
                        },
                      }}
                    >
                      <ListItemIcon sx={{ minWidth: 38, color: 'text.secondary' }}>
                        <Icon fontSize="small" />
                      </ListItemIcon>
                      <ListItemText
                        primary={link.label}
                        primaryTypographyProps={{ variant: 'body2', fontWeight: active ? 600 : 500 }}
                      />
                    </ListItemButton>
                  );
                })}
              </List>
            </Box>
          );
        })}
      </Box>
    </>
  );
};

/**
 * Responsive sidebar wrapper. Renders both a permanent drawer (desktop, always
 * visible) and a temporary drawer (mobile, toggled via `mobileOpen`).
 */
const Sidebar = ({ mobileOpen, onClose }) => {
  const drawerSx = {
    width: SIDEBAR_WIDTH,
    boxSizing: 'border-box',
    display: 'flex',
    flexDirection: 'column',
  };

  return (
    <Box component="nav" sx={{ width: { md: SIDEBAR_WIDTH }, flexShrink: { md: 0 } }}>
      {/* Mobile: temporary overlay drawer */}
      <Drawer
        variant="temporary"
        open={mobileOpen}
        onClose={onClose}
        ModalProps={{ keepMounted: true }}
        sx={{
          display: { xs: 'block', md: 'none' },
          '& .MuiDrawer-paper': drawerSx,
        }}
      >
        <SidebarContent onNavigate={onClose} />
      </Drawer>

      {/* Desktop: permanent drawer */}
      <Drawer
        variant="permanent"
        open
        sx={{
          display: { xs: 'none', md: 'block' },
          '& .MuiDrawer-paper': { ...drawerSx, borderRight: '1px solid', borderColor: 'divider' },
        }}
      >
        <SidebarContent />
      </Drawer>
    </Box>
  );
};

export { SidebarContent };
export default Sidebar;
