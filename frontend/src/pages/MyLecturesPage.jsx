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
  DialogTitle,
  Grid,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from '@mui/material';
import EventIcon from '@mui/icons-material/Event';
import CancelIcon from '@mui/icons-material/Cancel';
import StopIcon from '@mui/icons-material/Stop';
import MoreTimeIcon from '@mui/icons-material/MoreTime';
import MainLayout from '../layouts/MainLayout';
import LectureStatusChip from '../components/LectureStatusChip';
import { lectureService } from '../services/lectureService';
import { formatTimeOfDay } from '../utils/formatters';

const PAGE_SIZE = 10;

const INITIAL_FORM = {
  subject: '',
  className: '',
  batch: '',
  lectureDate: '',
  startTime: '',
  endTime: '',
};

/**
 * The teacher's scheduling screen: a form to plan lectures, the upcoming
 * schedule with cancellation, and live-lecture controls (end / extend).
 */
const MyLecturesPage = () => {
  const [lectures, setLectures] = useState(null);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [fieldErrors, setFieldErrors] = useState([]);
  const [form, setForm] = useState(INITIAL_FORM);
  const [submitting, setSubmitting] = useState(false);
  const [cancellingId, setCancellingId] = useState(null);
  const [actionId, setActionId] = useState(null);
  const [extendTarget, setExtendTarget] = useState(null); // lecture being extended
  const [extendMinutes, setExtendMinutes] = useState('15');
  const [extending, setExtending] = useState(false);

  const loadLectures = useCallback(async (pageNumber) => {
    setLoading(true);
    setError(null);
    try {
      const response = await lectureService.getMyUpcomingLectures(pageNumber, PAGE_SIZE);
      setLectures(response.data);
    } catch (err) {
      setError(err?.message ?? 'Failed to load lectures.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadLectures(page);
  }, [page, loadLectures]);

  const handleChange = (field) => (event) => {
    setForm((prev) => ({ ...prev, [field]: event.target.value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError(null);
    setFieldErrors([]);
    setSubmitting(true);
    try {
      await lectureService.scheduleLecture(form);
      setForm(INITIAL_FORM);
      await loadLectures(page);
    } catch (err) {
      setFieldErrors(err?.details ?? []);
      setError(err?.message ?? 'Failed to schedule the lecture.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleCancel = async (id) => {
    setCancellingId(id);
    setError(null);
    try {
      await lectureService.cancelLecture(id);
      await loadLectures(page);
    } catch (err) {
      setError(err?.message ?? 'Failed to cancel the lecture.');
    } finally {
      setCancellingId(null);
    }
  };

  const handleEnd = async (id) => {
    setActionId(id);
    setError(null);
    try {
      await lectureService.endLecture(id);
      await loadLectures(page);
    } catch (err) {
      setError(err?.message ?? 'Failed to end the lecture.');
    } finally {
      setActionId(null);
    }
  };

  const handleExtend = async () => {
    setExtending(true);
    setError(null);
    try {
      await lectureService.extendLecture(extendTarget.id, Number(extendMinutes));
      setExtendTarget(null);
      await loadLectures(page);
    } catch (err) {
      setExtendTarget(null);
      setError(err?.message ?? 'Failed to extend the lecture.');
    } finally {
      setExtending(false);
    }
  };

  return (
    <MainLayout>
      <Stack spacing={3}>
        <Box>
          <Typography variant="h4" component="h1" fontWeight={600}>
            My Lectures
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Plan your schedule and manage upcoming lectures.
          </Typography>
        </Box>

        {error && (
          <Alert severity="error" onClose={() => setError(null)}>
            {error}
          </Alert>
        )}
        {fieldErrors.length > 0 && (
          <Alert severity="error">
            <ul style={{ margin: 0, paddingLeft: '1.2rem' }}>
              {fieldErrors.map((detail) => (
                <li key={detail}>{detail}</li>
              ))}
            </ul>
          </Alert>
        )}

        {/* ---- Schedule form ---- */}
        <Card elevation={2}>
          <CardContent>
            <Typography variant="h6" fontWeight={600} sx={{ mb: 2 }}>
              Schedule a Lecture
            </Typography>
            <form onSubmit={handleSubmit} noValidate>
              <Grid container spacing={2}>
                <Grid size={{ xs: 12, sm: 4 }}>
                  <TextField label="Subject" value={form.subject}
                    onChange={handleChange('subject')} required fullWidth
                    disabled={submitting} />
                </Grid>
                <Grid size={{ xs: 12, sm: 4 }}>
                  <TextField label="Class" value={form.className}
                    onChange={handleChange('className')} required fullWidth
                    disabled={submitting} />
                </Grid>
                <Grid size={{ xs: 12, sm: 4 }}>
                  <TextField label="Batch (optional)" value={form.batch}
                    onChange={handleChange('batch')} fullWidth
                    disabled={submitting} />
                </Grid>
                <Grid size={{ xs: 12, sm: 3 }}>
                  <TextField label="Date" type="date" value={form.lectureDate}
                    onChange={handleChange('lectureDate')} required fullWidth
                    InputLabelProps={{ shrink: true }} disabled={submitting} />
                </Grid>
                <Grid size={{ xs: 12, sm: 3 }}>
                  <TextField label="Start" type="time" value={form.startTime}
                    onChange={handleChange('startTime')} required fullWidth
                    InputLabelProps={{ shrink: true }} disabled={submitting} />
                </Grid>
                <Grid size={{ xs: 12, sm: 3 }}>
                  <TextField label="End" type="time" value={form.endTime}
                    onChange={handleChange('endTime')} required fullWidth
                    InputLabelProps={{ shrink: true }} disabled={submitting} />
                </Grid>
                <Grid size={{ xs: 12, sm: 3 }}>
                  <Button type="submit" variant="contained" fullWidth
                    startIcon={<EventIcon />} disabled={submitting}
                    sx={{ height: 56 }}>
                    {submitting ? <CircularProgress size={22} color="inherit" /> : 'Schedule'}
                  </Button>
                </Grid>
              </Grid>
            </form>
          </CardContent>
        </Card>

        {/* ---- Upcoming lectures ---- */}
        <Paper elevation={2}>
          {loading && !lectures ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', p: 6 }}>
              <CircularProgress />
            </Box>
          ) : !lectures || lectures.content.length === 0 ? (
            <Box sx={{ p: 6, textAlign: 'center' }}>
              <Typography color="text.secondary">No upcoming lectures.</Typography>
            </Box>
          ) : (
            <>
              <TableContainer>
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>Date</TableCell>
                      <TableCell>Time</TableCell>
                      <TableCell>Subject</TableCell>
                      <TableCell>Class</TableCell>
                      <TableCell>Batch</TableCell>
                      <TableCell>Status</TableCell>
                      <TableCell align="right">Actions</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {lectures.content.map((lecture) => (
                      <TableRow key={lecture.id} hover>
                        <TableCell>{lecture.lectureDate}</TableCell>
                        <TableCell>
                          {formatTimeOfDay(lecture.startTime)} –{' '}
                          {formatTimeOfDay(lecture.effectiveEndTime)}
                        </TableCell>
                        <TableCell>{lecture.subject}</TableCell>
                        <TableCell>{lecture.className}</TableCell>
                        <TableCell>{lecture.batch ?? '—'}</TableCell>
                        <TableCell>
                          <LectureStatusChip status={lecture.status} />
                        </TableCell>
                        <TableCell align="right">
                          {lecture.status === 'SCHEDULED' && (
                            <Button size="small" color="error" startIcon={<CancelIcon />}
                              onClick={() => handleCancel(lecture.id)}
                              disabled={cancellingId === lecture.id}>
                              Cancel
                            </Button>
                          )}
                          {lecture.status === 'LIVE' && (
                            <Stack direction="row" spacing={1} justifyContent="flex-end">
                              <Button size="small" startIcon={<MoreTimeIcon />}
                                onClick={() => { setExtendTarget(lecture); setExtendMinutes('15'); }}
                                disabled={actionId === lecture.id}>
                                Extend
                              </Button>
                              <Button size="small" color="error" variant="outlined"
                                startIcon={<StopIcon />}
                                onClick={() => handleEnd(lecture.id)}
                                disabled={actionId === lecture.id}>
                                End
                              </Button>
                            </Stack>
                          )}
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
              <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ p: 2 }}>
                <Typography variant="body2" color="text.secondary">
                  Page {lectures.page + 1} of {lectures.totalPages} · {lectures.totalElements} total
                </Typography>
                <Stack direction="row" spacing={1}>
                  <Button size="small" disabled={lectures.first} onClick={() => setPage((p) => p - 1)}>
                    Previous
                  </Button>
                  <Button size="small" disabled={lectures.last} onClick={() => setPage((p) => p + 1)}>
                    Next
                  </Button>
                </Stack>
              </Stack>
            </>
          )}
        </Paper>
      </Stack>

      {/* ---- Extend dialog ---- */}
      <Dialog open={extendTarget !== null} onClose={() => !extending && setExtendTarget(null)}>
        <DialogTitle>Extend lecture</DialogTitle>
        <DialogContent>
          <TextField
            label="Minutes"
            type="number"
            value={extendMinutes}
            onChange={(e) => setExtendMinutes(e.target.value)}
            inputProps={{ min: 1, max: 30 }}
            helperText={`Currently extended by ${extendTarget?.extendedMinutes ?? 0} of 30 minutes`}
            fullWidth
            sx={{ mt: 1 }}
            disabled={extending}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setExtendTarget(null)} disabled={extending}>
            Cancel
          </Button>
          <Button variant="contained" onClick={handleExtend} disabled={extending}>
            {extending ? <CircularProgress size={22} color="inherit" /> : 'Extend'}
          </Button>
        </DialogActions>
      </Dialog>
    </MainLayout>
  );
};

export default MyLecturesPage;
