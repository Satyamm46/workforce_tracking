import { useCallback, useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
} from '@mui/material';
import LogoutIcon from '@mui/icons-material/Logout';
import CoffeeIcon from '@mui/icons-material/Coffee';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import MainLayout from '../layouts/MainLayout';
import AttendanceStatusChip from '../components/AttendanceStatusChip';
import { attendanceService } from '../services/attendanceService';
import { formatMinutes, formatTime } from '../utils/formatters';

const PAGE_SIZE = 10;

/**
 * The employee's attendance screen: today's live status with break and
 * clock-out actions, plus paginated history. "No record today" is a normal
 * state (e.g. a session restored without a fresh login), not an error.
 */
const MyAttendancePage = () => {
  const [today, setToday] = useState(null);
  const [todayLoading, setTodayLoading] = useState(true);
  const [history, setHistory] = useState(null);
  const [historyLoading, setHistoryLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [error, setError] = useState(null);
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [clockingOut, setClockingOut] = useState(false);
  const [breakBusy, setBreakBusy] = useState(false);

  const loadToday = useCallback(async () => {
    setTodayLoading(true);
    try {
      const response = await attendanceService.getMyToday();
      setToday(response.data);
    } catch (err) {
      if (err?.status === 404) {
        setToday(null); // valid state: no record for today
      } else {
        setError(err?.message ?? "Failed to load today's attendance.");
      }
    } finally {
      setTodayLoading(false);
    }
  }, []);

  const loadHistory = useCallback(async (pageNumber) => {
    setHistoryLoading(true);
    try {
      const response = await attendanceService.getMyHistory(pageNumber, PAGE_SIZE);
      setHistory(response.data);
    } catch (err) {
      setError(err?.message ?? 'Failed to load attendance history.');
    } finally {
      setHistoryLoading(false);
    }
  }, []);

  useEffect(() => {
    loadToday();
  }, [loadToday]);

  useEffect(() => {
    loadHistory(page);
  }, [page, loadHistory]);

  const handleClockOut = async () => {
    setClockingOut(true);
    setError(null);
    try {
      await attendanceService.clockOut();
      setConfirmOpen(false);
      await Promise.all([loadToday(), loadHistory(page)]);
    } catch (err) {
      setConfirmOpen(false);
      setError(err?.message ?? 'Clock-out failed.');
    } finally {
      setClockingOut(false);
    }
  };

  const handleBreakToggle = async () => {
    setBreakBusy(true);
    setError(null);
    try {
      if (today.status === 'ON_BREAK') {
        const response = await attendanceService.endBreak();
        setToday(response.data);
      } else {
        const response = await attendanceService.startBreak();
        setToday(response.data);
      }
    } catch (err) {
      setError(err?.message ?? 'Break action failed.');
    } finally {
      setBreakBusy(false);
    }
  };

  return (
    <MainLayout>
      <Stack spacing={3}>
        <Box>
          <Typography variant="h4" component="h1" fontWeight={600}>
            My Attendance
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Your working day and history.
          </Typography>
        </Box>

        {error && (
          <Alert severity="error" onClose={() => setError(null)}>
            {error}
          </Alert>
        )}

        {/* ---- Today card ---- */}
        <Card elevation={2}>
          <CardContent>
            {todayLoading ? (
              <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
                <CircularProgress size={28} />
              </Box>
            ) : !today ? (
              <Alert severity="info">
                No attendance record for today. It is created automatically when you log in.
              </Alert>
            ) : (
              <Stack
                direction={{ xs: 'column', sm: 'row' }}
                spacing={4}
                alignItems={{ sm: 'center' }}
                justifyContent="space-between"
              >
                <Stack direction="row" spacing={4}>
                  <Box>
                    <Typography variant="caption" color="text.secondary">
                      Status
                    </Typography>
                    <Box sx={{ mt: 0.5 }}>
                      <AttendanceStatusChip status={today.status} />
                    </Box>
                  </Box>
                  <Box>
                    <Typography variant="caption" color="text.secondary">
                      Login
                    </Typography>
                    <Typography fontWeight={600}>{formatTime(today.loginTime)}</Typography>
                  </Box>
                  <Box>
                    <Typography variant="caption" color="text.secondary">
                      Logout
                    </Typography>
                    <Typography fontWeight={600}>{formatTime(today.logoutTime)}</Typography>
                  </Box>
                  <Box>
                    <Typography variant="caption" color="text.secondary">
                      Worked
                    </Typography>
                    <Typography fontWeight={600}>{formatMinutes(today.workingMinutes)}</Typography>
                  </Box>
                  <Box>
                    <Typography variant="caption" color="text.secondary">
                      Breaks
                    </Typography>
                    <Typography fontWeight={600}>
                      {formatMinutes(today.totalBreakMinutes)}
                    </Typography>
                  </Box>
                </Stack>

                <Stack direction="row" spacing={1.5}>
                  <Button
                    variant="outlined"
                    startIcon={today.status === 'ON_BREAK' ? <PlayArrowIcon /> : <CoffeeIcon />}
                    onClick={handleBreakToggle}
                    disabled={breakBusy || today.status === 'CHECKED_OUT'}
                  >
                    {breakBusy ? (
                      <CircularProgress size={20} color="inherit" />
                    ) : today.status === 'ON_BREAK' ? (
                      'Resume Work'
                    ) : (
                      'Start Break'
                    )}
                  </Button>

                  <Button
                    variant="contained"
                    color="error"
                    startIcon={<LogoutIcon />}
                    onClick={() => setConfirmOpen(true)}
                    disabled={today.status === 'CHECKED_OUT'}
                  >
                    {today.status === 'CHECKED_OUT' ? 'Day Ended' : 'Clock Out'}
                  </Button>
                </Stack>
              </Stack>
            )}
          </CardContent>
        </Card>

        {/* ---- History table ---- */}
        <Paper elevation={2}>
          {historyLoading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', p: 6 }}>
              <CircularProgress />
            </Box>
          ) : !history || history.content.length === 0 ? (
            <Box sx={{ p: 6, textAlign: 'center' }}>
              <Typography color="text.secondary">No attendance records yet.</Typography>
            </Box>
          ) : (
            <>
              <TableContainer>
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>Date</TableCell>
                      <TableCell>Login</TableCell>
                      <TableCell>Logout</TableCell>
                      <TableCell>Breaks</TableCell>
                      <TableCell>Worked</TableCell>
                      <TableCell>Status</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {history.content.map((record) => (
                      <TableRow key={record.id} hover>
                        <TableCell>{record.workDate}</TableCell>
                        <TableCell>{formatTime(record.loginTime)}</TableCell>
                        <TableCell>{formatTime(record.logoutTime)}</TableCell>
                        <TableCell>{formatMinutes(record.totalBreakMinutes)}</TableCell>
                        <TableCell>{formatMinutes(record.workingMinutes)}</TableCell>
                        <TableCell>
                          <AttendanceStatusChip status={record.status} />
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>

              <Stack
                direction="row"
                justifyContent="space-between"
                alignItems="center"
                sx={{ p: 2 }}
              >
                <Typography variant="body2" color="text.secondary">
                  Page {history.page + 1} of {history.totalPages} · {history.totalElements} total
                </Typography>
                <Stack direction="row" spacing={1}>
                  <Button size="small" disabled={history.first} onClick={() => setPage((p) => p - 1)}>
                    Previous
                  </Button>
                  <Button size="small" disabled={history.last} onClick={() => setPage((p) => p + 1)}>
                    Next
                  </Button>
                </Stack>
              </Stack>
            </>
          )}
        </Paper>
      </Stack>

      {/* ---- Clock-out confirmation ---- */}
      <Dialog open={confirmOpen} onClose={() => !clockingOut && setConfirmOpen(false)}>
        <DialogTitle>End your working day?</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Clocking out records your logout time and finalizes today's working hours. If you are
            on a break, it will be ended automatically. This cannot be undone.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmOpen(false)} disabled={clockingOut}>
            Cancel
          </Button>
          <Button color="error" variant="contained" onClick={handleClockOut} disabled={clockingOut}>
            {clockingOut ? <CircularProgress size={22} color="inherit" /> : 'Clock Out'}
          </Button>
        </DialogActions>
      </Dialog>
    </MainLayout>
  );
};

export default MyAttendancePage;
