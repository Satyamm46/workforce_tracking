import { Chip } from '@mui/material';

/** Colour/label per attendance status. ON_BREAK activates in the break milestone. */
const STATUS_CONFIG = {
  WORKING: { color: 'success', label: 'Working' },
  ON_BREAK: { color: 'warning', label: 'On Break' },
  CHECKED_OUT: { color: 'default', label: 'Checked Out' },
};

/** Renders an attendance status as a coloured chip. Presentational only. */
const AttendanceStatusChip = ({ status }) => {
  const config = STATUS_CONFIG[status] ?? { color: 'default', label: status };
  return <Chip color={config.color} label={config.label} size="small" />;
};

export default AttendanceStatusChip;