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
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import MainLayout from '../layouts/MainLayout';
import LectureCalendar from '../components/LectureCalendar';
import { lectureService } from '../services/lectureService';
import { monthRange } from '../utils/formatters';

/** The current month as a "YYYY-MM" string. */
const currentMonth = () => {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
};

const INITIAL_SCHEDULE_FORM = {
  subject: '',
  className: '',
  batch: '',
  lectureDate: '',
  startTime: '',
  endTime: '',
};

/**
 * The teacher's calendar: a month grid of their own classes, one-off and
 * series-generated alike. Zoom-style: any day can be clicked, and a class can
 * be scheduled straight from the day dialog with the date pre-filled.
 * Repeating classes only exist ~8 weeks ahead, so months beyond that horizon
 * are legitimately empty.
 */
const MyCalendarPage = () => {
  const [month, setMonth] = useState(currentMonth);
  const [lectures, setLectures] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [scheduleForm, setScheduleForm] = useState(null); // null = dialog closed
  const [fieldErrors, setFieldErrors] = useState([]);
  const [submitting, setSubmitting] = useState(false);

  const loadMonth = useCallback(async (value) => {
    const range = monthRange(value);
    if (!range) {
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const response = await lectureService.getMyCalendar(range.from, range.to);
      setLectures(response.data ?? []);
    } catch (err) {
      setError(err?.message ?? 'Failed to load the calendar.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadMonth(month);
  }, [month, loadMonth]);

  const openSchedule = (date) => {
    setFieldErrors([]);
    setScheduleForm({ ...INITIAL_SCHEDULE_FORM, lectureDate: date });
  };

  const handleFormChange = (field) => (event) => {
    setScheduleForm((prev) => ({ ...prev, [field]: event.target.value }));
  };

  const handleSchedule = async () => {
    setSubmitting(true);
    setError(null);
    setFieldErrors([]);
    try {
      await lectureService.scheduleLecture(scheduleForm);
      setScheduleForm(null);
      await loadMonth(month);
    } catch (err) {
      setFieldErrors(err?.details ?? []);
      setError(err?.message ?? 'Failed to schedule the class.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <MainLayout>
      <Stack spacing={3}>
        <Box>
          <Typography variant="h4" component="h1" fontWeight={600}>
            My Calendar
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Your classes at a glance — click any day to see details or schedule a class.
          </Typography>
        </Box>

        {error && (
          <Alert severity="error" onClose={() => setError(null)}>
            {error}
          </Alert>
        )}

        <Card elevation={2}>
          <CardContent>
            {loading ? (
              <Box sx={{ display: 'flex', justifyContent: 'center', p: 6 }}>
                <CircularProgress />
              </Box>
            ) : (
              <LectureCalendar
                month={month}
                lectures={lectures}
                onMonthChange={setMonth}
                onSchedule={openSchedule}
              />
            )}
          </CardContent>
        </Card>
      </Stack>

      {/* ---- Schedule-from-day dialog ---- */}
      <Dialog open={scheduleForm !== null} onClose={() => !submitting && setScheduleForm(null)}
        maxWidth="sm" fullWidth>
        <DialogTitle>Schedule a class — {scheduleForm?.lectureDate}</DialogTitle>
        <DialogContent>
          <Stack spacing={2.5} sx={{ mt: 1 }}>
            {fieldErrors.length > 0 && (
              <Alert severity="error">
                <ul style={{ margin: 0, paddingLeft: '1.2rem' }}>
                  {fieldErrors.map((detail) => (
                    <li key={detail}>{detail}</li>
                  ))}
                </ul>
              </Alert>
            )}
            <TextField label="Subject" value={scheduleForm?.subject ?? ''}
              onChange={handleFormChange('subject')} required fullWidth
              disabled={submitting} />
            <TextField label="Class" value={scheduleForm?.className ?? ''}
              onChange={handleFormChange('className')} required fullWidth
              disabled={submitting} />
            <TextField label="Batch / Student Name (optional)" value={scheduleForm?.batch ?? ''}
              onChange={handleFormChange('batch')} fullWidth
              disabled={submitting} />
            <TextField label="Date" type="date" value={scheduleForm?.lectureDate ?? ''}
              onChange={handleFormChange('lectureDate')} required fullWidth
              slotProps={{ inputLabel: { shrink: true } }} disabled={submitting} />
            <Stack direction="row" spacing={2}>
              <TextField label="Start" type="time" value={scheduleForm?.startTime ?? ''}
                onChange={handleFormChange('startTime')} required fullWidth
                slotProps={{ inputLabel: { shrink: true } }} disabled={submitting} />
              <TextField label="End" type="time" value={scheduleForm?.endTime ?? ''}
                onChange={handleFormChange('endTime')} required fullWidth
                slotProps={{ inputLabel: { shrink: true } }} disabled={submitting} />
            </Stack>
            <Typography variant="caption" color="text.secondary">
              Need it to repeat weekly or monthly? Use the Repeat option on My Lectures.
            </Typography>
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setScheduleForm(null)} disabled={submitting}>
            Cancel
          </Button>
          <Button variant="contained" onClick={handleSchedule}
            disabled={submitting
              || !scheduleForm?.subject.trim()
              || !scheduleForm?.className.trim()
              || !scheduleForm?.lectureDate
              || !scheduleForm?.startTime
              || !scheduleForm?.endTime}>
            {submitting ? <CircularProgress size={22} color="inherit" /> : 'Schedule'}
          </Button>
        </DialogActions>
      </Dialog>
    </MainLayout>
  );
};

export default MyCalendarPage;
