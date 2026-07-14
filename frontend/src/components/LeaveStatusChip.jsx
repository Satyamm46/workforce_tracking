import { Chip } from '@mui/material';

/** Colour/label per leave workflow status. */
const STATUS_CONFIG = {
  PENDING: { color: 'warning', label: 'Pending' },
  APPROVED: { color: 'success', label: 'Approved' },
  REJECTED: { color: 'error', label: 'Rejected' },
  CANCELLED: { color: 'default', label: 'Cancelled' },
  ON_LEAVE: { color: 'info', label: 'On Leave' }
};

/** Renders a leave status as a coloured chip. Presentational only. */
const LeaveStatusChip = ({ status }) => {
  const config = STATUS_CONFIG[status] ?? { color: 'default', label: status };
  return <Chip color={config.color} label={config.label} size="small" />;
};

export default LeaveStatusChip;
