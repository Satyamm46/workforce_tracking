import { useCallback, useEffect, useState } from 'react';
import {
  Alert, Box, Button, CircularProgress, Paper, Stack, Table, TableBody,
  TableCell, TableContainer, TableHead, TableRow, TextField, Typography,
} from '@mui/material';
import DownloadIcon from '@mui/icons-material/Download';
import MainLayout from '../layouts/MainLayout';
import { reportService } from '../services/reportService';
import { formatMinutes } from '../utils/formatters';
import { downloadCsv } from '../utils/csv';

/** Current month as the input's "YYYY-MM" value. */
const currentMonthValue = () => new Date().toISOString().slice(0, 7);

/**
 * Admin monthly reports: attendance summary per employee and teaching
 * activity per teacher, with CSV export of the loaded data.
 */
const ReportsPage = () => {
  const [month, setMonth] = useState(currentMonthValue());
  const [attendance, setAttendance] = useState(null);
  const [teaching, setTeaching] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const loadReports = useCallback(async (monthValue) => {
    setLoading(true);
    setError(null);
    const [year, monthNumber] = monthValue.split('-').map(Number);
    try {
      const [attendanceRes, teachingRes] = await Promise.all([
        reportService.getAttendanceReport(year, monthNumber),
        reportService.getTeachingReport(year, monthNumber),
      ]);
      setAttendance(attendanceRes.data);
      setTeaching(teachingRes.data);
    } catch (err) {
      setError(err?.message ?? 'Failed to load reports.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadReports(month);
  }, [month, loadReports]);

  const exportAttendance = () => {
    downloadCsv(
      `attendance-report-${month}.csv`,
      ['Employee', 'Present Days', 'Leave Days', 'Working Hours', 'Break Hours'],
      attendance.map((row) => [
        row.fullName, row.presentDays, row.leaveDays,
        formatMinutes(row.workingMinutes), formatMinutes(row.breakMinutes),
      ])
    );
  };

  const exportTeaching = () => {
    downloadCsv(
      `teaching-report-${month}.csv`,
      ['Teacher', 'Lectures', 'Teaching Hours', 'Extension Minutes'],
      teaching.map((row) => [
        row.fullName, row.lecturesCompleted,
        formatMinutes(row.teachingMinutes), row.extensionMinutes,
      ])
    );
  };

  return (
    <MainLayout>
      <Stack spacing={3}>
        <Stack direction="row" justifyContent="space-between" alignItems="flex-end">
          <Box>
            <Typography variant="h4" component="h1" fontWeight={600}>Reports</Typography>
            <Typography variant="body1" color="text.secondary">
              Monthly attendance and teaching summaries.
            </Typography>
          </Box>
          <TextField
            label="Month" type="month" size="small" value={month}
            onChange={(e) => setMonth(e.target.value)}
            InputLabelProps={{ shrink: true }}
          />
        </Stack>

        {error && <Alert severity="error" onClose={() => setError(null)}>{error}</Alert>}

        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', p: 6 }}>
            <CircularProgress />
          </Box>
        ) : (
          <>
            {/* ---- Attendance report ---- */}
            <Paper elevation={2}>
              <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ p: 2 }}>
                <Typography variant="h6" fontWeight={600}>Employee Attendance</Typography>
                <Button size="small" startIcon={<DownloadIcon />} onClick={exportAttendance}
                  disabled={!attendance || attendance.length === 0}>
                  Export CSV
                </Button>
              </Stack>
              {!attendance || attendance.length === 0 ? (
                <Box sx={{ p: 4, textAlign: 'center' }}>
                  <Typography color="text.secondary">No attendance data for this month.</Typography>
                </Box>
              ) : (
                <TableContainer>
                  <Table>
                    <TableHead>
                      <TableRow>
                        <TableCell>Employee</TableCell>
                        <TableCell align="right">Present Days</TableCell>
                        <TableCell align="right">Leave Days</TableCell>
                        <TableCell align="right">Working Hours</TableCell>
                        <TableCell align="right">Break Hours</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {attendance.map((row) => (
                        <TableRow key={row.userId} hover>
                          <TableCell>{row.fullName}</TableCell>
                          <TableCell align="right">{row.presentDays}</TableCell>
                          <TableCell align="right">{row.leaveDays}</TableCell>
                          <TableCell align="right">{formatMinutes(row.workingMinutes)}</TableCell>
                          <TableCell align="right">{formatMinutes(row.breakMinutes)}</TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              )}
            </Paper>

            {/* ---- Teaching report ---- */}
            <Paper elevation={2}>
              <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ p: 2 }}>
                <Typography variant="h6" fontWeight={600}>Teaching Activity</Typography>
                <Button size="small" startIcon={<DownloadIcon />} onClick={exportTeaching}
                  disabled={!teaching || teaching.length === 0}>
                  Export CSV
                </Button>
              </Stack>
              {!teaching || teaching.length === 0 ? (
                <Box sx={{ p: 4, textAlign: 'center' }}>
                  <Typography color="text.secondary">No completed lectures this month.</Typography>
                </Box>
              ) : (
                <TableContainer>
                  <Table>
                    <TableHead>
                      <TableRow>
                        <TableCell>Teacher</TableCell>
                        <TableCell align="right">Lectures</TableCell>
                        <TableCell align="right">Teaching Hours</TableCell>
                        <TableCell align="right">Extensions</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {teaching.map((row) => (
                        <TableRow key={row.teacherId} hover>
                          <TableCell>{row.fullName}</TableCell>
                          <TableCell align="right">{row.lecturesCompleted}</TableCell>
                          <TableCell align="right">{formatMinutes(row.teachingMinutes)}</TableCell>
                          <TableCell align="right">{row.extensionMinutes} min</TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              )}
            </Paper>
          </>
        )}
      </Stack>
    </MainLayout>
  );
};

export default ReportsPage;
