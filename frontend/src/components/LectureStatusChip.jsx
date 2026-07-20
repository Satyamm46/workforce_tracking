import { Chip } from '@mui/material';

/** Colour/label per lecture lifecycle status. LIVE/COMPLETED activate in the
 *  tracking milestone but are wired now — the M3 chip lesson applied. */
const STATUS_CONFIG = {
  SCHEDULED: { color: 'info', label: 'Scheduled' },
  LIVE: { color: 'success', label: 'Live' },
  COMPLETED: { color: 'default', label: 'Completed' },
  CANCELLED: { color: 'error', label: 'Cancelled' },
  MISSED: { color: 'warning', label: 'Missed' },
  SUMMARY_MISSED: { color: 'error', label: 'No Summary' },
};

/** Renders a lecture status as a coloured chip. Presentational only. */
const LectureStatusChip = ({ status }) => {
  const config = STATUS_CONFIG[status] ?? { color: 'default', label: status };
  return <Chip color={config.color} label={config.label} size="small" />;
};

export default LectureStatusChip;
