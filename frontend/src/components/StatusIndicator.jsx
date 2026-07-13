import { Box, Chip } from '@mui/material';
import { HEALTH_STATUS } from '../context/HealthContext';

/**
 * Small, reusable presentational component that renders a coloured status
 * chip for a backend health value.
 *
 * "Presentational" means it holds NO state and performs NO data fetching — it
 * simply renders whatever `status` it is given. All data comes in through
 * props, which makes it trivially reusable and testable.
 */

/**
 * Maps a health status to a Material UI Chip colour and label.
 * Defined outside the component so it is not recreated on every render.
 */
const STATUS_CONFIG = {
  [HEALTH_STATUS.UP]: { color: 'success', label: 'UP' },
  [HEALTH_STATUS.DOWN]: { color: 'error', label: 'DOWN' },
  [HEALTH_STATUS.UNKNOWN]: { color: 'default', label: 'UNKNOWN' },
};

const StatusIndicator = ({ status }) => {
  const config = STATUS_CONFIG[status] ?? STATUS_CONFIG[HEALTH_STATUS.UNKNOWN];

  return (
    <Box component="span" sx={{ display: 'inline-flex', alignItems: 'center' }}>
      <Chip color={config.color} label={config.label} size="small" variant="filled" />
    </Box>
  );
};

export default StatusIndicator;