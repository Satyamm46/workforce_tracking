import { useCallback, useEffect, useState } from 'react';
import {
  Alert, Box, Button, CircularProgress, Paper, Stack, Table, TableBody,
  TableCell, TableContainer, TableHead, TableRow, TextField, Typography,
} from '@mui/material';
import MainLayout from '../layouts/MainLayout';
import AttendanceStatusChip from '../components/AttendanceStatusChip';
import { attendanceService } from '../services/attendanceService';
import { formatMinutes, formatTime } from '../utils/formatters';

const PAGE_SIZE = 20;

/**
 * Admin view: every user's attendance for a chosen day. An empty date means
 * "today" — the backend applies its zone-correct default.
 */
const AttendanceAdminPage = () => {
  const [date, setDate] = useState('');
  const [pageData, setPageData] = useState(null);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const loadAttendance = useCallback(async (selectedDate, pageNumber) => {
    setLoading(true);
    setError(null);
    try {
      const response = await attendanceService.getByDate(
        selectedDate || null, pageNumber, PAGE_SIZE);
      setPageData(response.data);
    } catch (err) {
      setError(err?.message ?? 'Failed to load attendance.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadAttendance(date, page);
  }, [date, page, loadAttendance]);

  const handleDateChange = (event) => {
    setDate(event.target.value);
    setPage(0); // new filter → back to the first page
  };

  return (
    <MainLayout>
      <Stack spacing={3}>
        <Stack direction="row" justifyContent="space-between" alignItems="flex-end">
          <Box>
            <Typography variant="h4" component="h1" fontWeight={600}>Attendance</Typography>
            <Typography variant="body1" color="text.secondary">
              Daily attendance across the institute.
            </Typography>
          </Box>
          <TextField
            label="Date"
            type="date"
            value={date}
            onChange={handleDateChange}
            size="small"
            InputLabelProps={{ shrink: true }}
            helperText="Leave empty for today"
          />
        </Stack>

        {error && <Alert severity="error" onClose={() => setError(null)}>{error}</Alert>}

        <Paper elevation={2}>
          {loading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', p: 6 }}>
              <CircularProgress />
            </Box>
          ) : !pageData || pageData.content.length === 0 ? (
            <Box sx={{ p: 6, textAlign: 'center' }}>
              <Typography color="text.secondary">No attendance records for this day.</Typography>
            </Box>
          ) : (
            <>
              <TableContainer>
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>Employee</TableCell>
                      <TableCell>Login</TableCell>
                      <TableCell>Logout</TableCell>
                      <TableCell>Breaks</TableCell>
                      <TableCell>Worked</TableCell>
                      <TableCell>Status</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {pageData.content.map((record) => (
                      <TableRow key={record.id} hover>
                        <TableCell>{record.userFullName}</TableCell>
                        <TableCell>{formatTime(record.loginTime)}</TableCell>
                        <TableCell>{formatTime(record.logoutTime)}</TableCell>
                        <TableCell>{formatMinutes(record.totalBreakMinutes)}</TableCell>
                        <TableCell>{formatMinutes(record.workingMinutes)}</TableCell>
                        <TableCell><AttendanceStatusChip status={record.status} /></TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
              <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ p: 2 }}>
                <Typography variant="body2" color="text.secondary">
                  Page {pageData.page + 1} of {pageData.totalPages} · {pageData.totalElements} total
                </Typography>
                <Stack direction="row" spacing={1}>
                  <Button size="small" disabled={pageData.first} onClick={() => setPage((p) => p - 1)}>
                    Previous
                  </Button>
                  <Button size="small" disabled={pageData.last} onClick={() => setPage((p) => p + 1)}>
                    Next
                  </Button>
                </Stack>
              </Stack>
            </>
          )}
        </Paper>
      </Stack>
    </MainLayout>
  );
};

export default AttendanceAdminPage;