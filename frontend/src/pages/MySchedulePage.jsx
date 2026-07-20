import { useCallback, useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  CircularProgress,
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
import EventNoteIcon from '@mui/icons-material/EventNote';
import MainLayout from '../layouts/MainLayout';
import { workPlanService } from '../services/workPlanService';
import { formatTimeOfDay } from '../utils/formatters';

const PAGE_SIZE = 10;

const EMPTY_FORM = { plannedStartTime: '09:00', plannedEndTime: '17:00', workDescription: '' };

/**
 * The user's schedule screen: submit/update tomorrow's plan (and today's,
 * late, if it was forgotten — check-in is blocked without it), plus history.
 */
const MySchedulePage = () => {
  // 'tomorrow' (default) or 'today' — which day the form submits for.
  const [mode, setMode] = useState('tomorrow');
  const [form, setForm] = useState(EMPTY_FORM);
  const [submitting, setSubmitting] = useState(false);
  const [success, setSuccess] = useState(null);
  const [error, setError] = useState(null);

  const [todayPlan, setTodayPlan] = useState(null);
  const [history, setHistory] = useState(null);
  const [historyLoading, setHistoryLoading] = useState(true);
  const [page, setPage] = useState(0);

  const loadTodayPlan = useCallback(async () => {
    try {
      const response = await workPlanService.getMyPlanForDay();
      setTodayPlan(response.data);
    } catch (err) {
      if (err?.status === 404) {
        setTodayPlan(null); // normal: nothing submitted for today
      }
    }
  }, []);

  const loadHistory = useCallback(async (pageNumber) => {
    setHistoryLoading(true);
    try {
      const response = await workPlanService.getMyPlans(pageNumber, PAGE_SIZE);
      setHistory(response.data);

      // Pre-fill the form with tomorrow's existing plan when editing.
      const tomorrow = new Date(Date.now() + 24 * 60 * 60 * 1000)
        .toISOString()
        .slice(0, 10);
      const existing = response.data.content.find((plan) => plan.planDate === tomorrow);
      if (existing) {
        setForm({
          plannedStartTime: existing.plannedStartTime.slice(0, 5),
          plannedEndTime: existing.plannedEndTime.slice(0, 5),
          workDescription: existing.workDescription,
        });
      }
    } catch (err) {
      setError(err?.message ?? 'Failed to load your plans.');
    } finally {
      setHistoryLoading(false);
    }
  }, []);

  useEffect(() => {
    loadTodayPlan();
  }, [loadTodayPlan]);

  useEffect(() => {
    loadHistory(page);
  }, [page, loadHistory]);

  const handleChange = (field) => (event) => {
    setForm((prev) => ({ ...prev, [field]: event.target.value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSubmitting(true);
    setError(null);
    setSuccess(null);
    try {
      const response =
        mode === 'today'
          ? await workPlanService.submitTodayLate(form)
          : await workPlanService.submitTomorrow(form);
      setSuccess(response.message);
      await Promise.all([loadTodayPlan(), loadHistory(page)]);
      if (mode === 'today') {
        setMode('tomorrow');
      }
    } catch (err) {
      setError(err?.message ?? 'Failed to save the plan.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <MainLayout>
      <Stack spacing={3}>
        <Box>
          <Typography variant="h4" component="h1" fontWeight={600}>
            My Schedule
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Declare tomorrow's working hours and tasks — required before you can check in.
          </Typography>
        </Box>

        {error && (
          <Alert severity="error" onClose={() => setError(null)}>
            {error}
          </Alert>
        )}
        {success && (
          <Alert severity="success" onClose={() => setSuccess(null)}>
            {success}
          </Alert>
        )}

        {/* Missing today's plan → offer the late submission path. */}
        {todayPlan === null && mode !== 'today' && (
          <Alert
            severity="warning"
            action={
              <Button color="inherit" size="small" onClick={() => setMode('today')}>
                Submit now
              </Button>
            }
          >
            You have no plan for today — check-in is blocked until you submit one (it will be
            marked late).
          </Alert>
        )}

        {/* ---- Plan form ---- */}
        <Card elevation={2}>
          <CardContent>
            <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 2 }}>
              <EventNoteIcon color="primary" />
              <Typography variant="h6">
                {mode === 'today' ? "Today's plan (late)" : "Tomorrow's plan"}
              </Typography>
              {mode === 'today' && (
                <Button size="small" onClick={() => setMode('tomorrow')}>
                  Switch to tomorrow
                </Button>
              )}
            </Stack>

            <form onSubmit={handleSubmit}>
              <Stack spacing={2.5}>
                <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2.5}>
                  <TextField
                    label="Planned login time"
                    type="time"
                    value={form.plannedStartTime}
                    onChange={handleChange('plannedStartTime')}
                    required
                    fullWidth
                    disabled={submitting}
                    slotProps={{ inputLabel: { shrink: true } }}
                  />
                  <TextField
                    label="Planned logout time"
                    type="time"
                    value={form.plannedEndTime}
                    onChange={handleChange('plannedEndTime')}
                    required
                    fullWidth
                    disabled={submitting}
                    slotProps={{ inputLabel: { shrink: true } }}
                  />
                </Stack>

                <TextField
                  label="What will you work on?"
                  value={form.workDescription}
                  onChange={handleChange('workDescription')}
                  required
                  fullWidth
                  multiline
                  minRows={3}
                  inputProps={{ maxLength: 1000 }}
                  helperText={`${form.workDescription.length}/1000`}
                  disabled={submitting}
                />

                <Box>
                  <Button type="submit" variant="contained" disabled={submitting}>
                    {submitting ? (
                      <CircularProgress size={22} color="inherit" />
                    ) : mode === 'today' ? (
                      "Save Today's Plan"
                    ) : (
                      "Save Tomorrow's Plan"
                    )}
                  </Button>
                </Box>
              </Stack>
            </form>
          </CardContent>
        </Card>

        {/* ---- History ---- */}
        <Paper elevation={2}>
          {historyLoading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', p: 6 }}>
              <CircularProgress />
            </Box>
          ) : !history || history.content.length === 0 ? (
            <Box sx={{ p: 6, textAlign: 'center' }}>
              <Typography color="text.secondary">No plans submitted yet.</Typography>
            </Box>
          ) : (
            <>
              <TableContainer>
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>Date</TableCell>
                      <TableCell>Planned Login</TableCell>
                      <TableCell>Planned Logout</TableCell>
                      <TableCell>Planned Work</TableCell>
                      <TableCell>Submitted</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {history.content.map((plan) => (
                      <TableRow key={plan.id} hover>
                        <TableCell>{plan.planDate}</TableCell>
                        <TableCell>{formatTimeOfDay(plan.plannedStartTime)}</TableCell>
                        <TableCell>{formatTimeOfDay(plan.plannedEndTime)}</TableCell>
                        <TableCell sx={{ maxWidth: 360 }}>
                          <Typography variant="body2" noWrap title={plan.workDescription}>
                            {plan.workDescription}
                          </Typography>
                        </TableCell>
                        <TableCell>
                          <Chip
                            label={plan.submittedLate ? 'Late' : 'On time'}
                            color={plan.submittedLate ? 'warning' : 'success'}
                            size="small"
                          />
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
    </MainLayout>
  );
};

export default MySchedulePage;
