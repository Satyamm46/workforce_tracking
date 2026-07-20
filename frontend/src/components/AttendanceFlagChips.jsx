import { Chip, Stack } from '@mui/material';

/**
 * Renders the late-arrival / half-day / absent flags on an attendance record
 * as small coloured chips. Presentational only; renders nothing when the
 * record carries no flags.
 *
 * @param {object} record an AttendanceResponse (lateArrival, halfDay, absentNoReport)
 */
const AttendanceFlagChips = ({ record }) => {
  if (!record?.lateArrival && !record?.halfDay && !record?.absentNoReport) {
    return null;
  }
  return (
    <Stack direction="row" spacing={0.5} sx={{ mt: 0.5 }} flexWrap="wrap" useFlexGap>
      {record.lateArrival && <Chip color="warning" label="Late" size="small" variant="outlined" />}
      {record.halfDay && <Chip color="warning" label="Half Day" size="small" />}
      {record.absentNoReport && <Chip color="error" label="Absent (no report)" size="small" />}
    </Stack>
  );
};

export default AttendanceFlagChips;
