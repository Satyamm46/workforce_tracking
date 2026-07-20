import { useCallback, useEffect, useState } from 'react';
import {
  Alert, Box, Button, CircularProgress, Paper, Stack, Table, TableBody,
  TableCell, TableContainer, TableHead, TableRow, TextField, Typography,
} from '@mui/material';
import MainLayout from '../layouts/MainLayout';
import LectureStatusChip from '../components/LectureStatusChip';
import { lectureService } from '../services/lectureService';
import { formatTimeOfDay } from '../utils/formatters';

const PAGE_SIZE = 20;

/** Admin view: every lecture on a chosen day (empty date = today). */
const LectureAdminPage = () => {
  const [date, setDate] = useState('');
  const [pageData, setPageData] = useState(null);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const loadLectures = useCallback(async (selectedDate, pageNumber) => {
    setLoading(true);
    setError(null);
    try {
      const response = await lectureService.getLecturesByDate(
        selectedDate || null, pageNumber, PAGE_SIZE);
      setPageData(response.data);
    } catch (err) {
      setError(err?.message ?? 'Failed to load lectures.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadLectures(date, page);
  }, [date, page, loadLectures]);

  return (
    <MainLayout>
      <Stack spacing={3}>
        <Stack direction="row" justifyContent="space-between" alignItems="flex-end">
          <Box>
            <Typography variant="h4" component="h1" fontWeight={600}>Lectures</Typography>
            <Typography variant="body1" color="text.secondary">
              Daily lecture schedule across the institute.
            </Typography>
          </Box>
          <TextField label="Date" type="date" size="small" value={date}
            onChange={(e) => { setDate(e.target.value); setPage(0); }}
            slotProps={{ inputLabel: { shrink: true } }} helperText="Leave empty for today" />
        </Stack>

        {error && <Alert severity="error" onClose={() => setError(null)}>{error}</Alert>}

        <Paper elevation={2}>
          {loading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', p: 6 }}>
              <CircularProgress />
            </Box>
          ) : !pageData || pageData.content.length === 0 ? (
            <Box sx={{ p: 6, textAlign: 'center' }}>
              <Typography color="text.secondary">No lectures for this day.</Typography>
            </Box>
          ) : (
            <>
              <TableContainer>
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>Teacher</TableCell>
                      <TableCell>Time</TableCell>
                      <TableCell>Subject</TableCell>
                      <TableCell>Class</TableCell>
                      <TableCell>Batch</TableCell>
                      <TableCell>Status</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {pageData.content.map((lecture) => (
                      <TableRow key={lecture.id} hover>
                        <TableCell>{lecture.teacherFullName}</TableCell>
                        <TableCell>
                          {formatTimeOfDay(lecture.startTime)} –{' '}
                          {formatTimeOfDay(lecture.effectiveEndTime)}
                        </TableCell>
                        <TableCell>{lecture.subject}</TableCell>
                        <TableCell>{lecture.className}</TableCell>
                        <TableCell>{lecture.batch ?? '—'}</TableCell>
                        <TableCell><LectureStatusChip status={lecture.status} /></TableCell>
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

export default LectureAdminPage;
