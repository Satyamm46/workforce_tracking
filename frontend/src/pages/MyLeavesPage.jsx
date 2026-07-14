import { useCallback, useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  CircularProgress,
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
import SendIcon from '@mui/icons-material/Send';
import CancelIcon from '@mui/icons-material/Cancel';
import MainLayout from '../layouts/MainLayout';
import LeaveStatusChip from '../components/LeaveStatusChip';
import { leaveService } from '../services/leaveService';

const PAGE_SIZE = 10;

const INITIAL_FORM = { startDate: '', endDate: '', reason: '' };

/**
 * The employee's leave screen: current balance, an application form, and the
 * request history with cancellation of pending requests.
 */
const MyLeavesPage = () => {
  const [balance, setBalance] = useState(null);
  const [history, setHistory] = useState(null);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [fieldErrors, setFieldErrors] = useState([]);
  const [form, setForm] = useState(INITIAL_FORM);
  const [submitting, setSubmitting] = useState(false);
  const [cancellingId, setCancellingId] = useState(null);

  /** Loads balance and the current history page together. */
  const loadAll = useCallback(async (pageNumber) => {
    setLoading(true);
    setError(null);
    try {
      const [balanceRes, historyRes] = await Promise.all([
        leaveService.getMyBalance(),
        leaveService.getMyLeaves(pageNumber, PAGE_SIZE),
      ]);
      setBalance(balanceRes.data);
      setHistory(historyRes.data);
    } catch (err) {
      setError(err?.message ?? 'Failed to load leave data.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadAll(page);
  }, [page, loadAll]);

  const handleChange = (field) => (event) => {
    setForm((prev) => ({ ...prev, [field]: event.target.value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError(null);
    setFieldErrors([]);
    setSubmitting(true);
    try {
      await leaveService.applyForLeave(form);
      setForm(INITIAL_FORM);
      await loadAll(page);
    } catch (err) {
      setFieldErrors(err?.details ?? []);
      setError(err?.message ?? 'Failed to submit leave request.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleCancel = async (id) => {
    setCancellingId(id);
    setError(null);
    try {
      await leaveService.cancelLeave(id);
      await loadAll(page);
    } catch (err) {
      setError(err?.message ?? 'Failed to cancel the request.');
    } finally {
      setCancellingId(null);
    }
  };

  return (
    <MainLayout>
      <Stack spacing={3}>
        <Box>
          <Typography variant="h4" component="h1" fontWeight={600}>
            My Leaves
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Your balance, applications, and history.
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

        {loading && !history ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', p: 6 }}>
            <CircularProgress />
          </Box>
        ) : (
          <>
            {/* ---- Balance card ---- */}
            {balance && (
              <Card elevation={2}>
                <CardContent>
                  <Stack direction="row" spacing={6}>
                    <Box>
                      <Typography variant="caption" color="text.secondary">
                        Allowance ({balance.year})
                      </Typography>
                      <Typography variant="h5" fontWeight={700}>
                        {balance.allowedDays} days
                      </Typography>
                    </Box>
                    <Box>
                      <Typography variant="caption" color="text.secondary">
                        Used
                      </Typography>
                      <Typography variant="h5" fontWeight={700}>
                        {balance.usedDays} days
                      </Typography>
                    </Box>
                    <Box>
                      <Typography variant="caption" color="text.secondary">
                        Remaining
                      </Typography>
                      <Typography
                        variant="h5"
                        fontWeight={700}
                        color={balance.remainingDays > 0 ? 'success.main' : 'error.main'}
                      >
                        {balance.remainingDays} days
                      </Typography>
                    </Box>
                  </Stack>
                </CardContent>
              </Card>
            )}

            {/* ---- Apply form ---- */}
            <Card elevation={2}>
              <CardContent>
                <Typography variant="h6" fontWeight={600} sx={{ mb: 2 }}>
                  Apply for Leave
                </Typography>
                <form onSubmit={handleSubmit} noValidate>
                  <Grid container spacing={2} alignItems="flex-start">
                    <Grid size={{ xs: 12, sm: 3 }}>
                      <TextField
                        label="From"
                        type="date"
                        value={form.startDate}
                        onChange={handleChange('startDate')}
                        required
                        fullWidth
                        InputLabelProps={{ shrink: true }}
                        disabled={submitting}
                      />
                    </Grid>
                    <Grid size={{ xs: 12, sm: 3 }}>
                      <TextField
                        label="To"
                        type="date"
                        value={form.endDate}
                        onChange={handleChange('endDate')}
                        required
                        fullWidth
                        InputLabelProps={{ shrink: true }}
                        disabled={submitting}
                      />
                    </Grid>
                    <Grid size={{ xs: 12, sm: 4 }}>
                      <TextField
                        label="Reason"
                        value={form.reason}
                        onChange={handleChange('reason')}
                        required
                        fullWidth
                        disabled={submitting}
                      />
                    </Grid>
                    <Grid size={{ xs: 12, sm: 2 }}>
                      <Button
                        type="submit"
                        variant="contained"
                        fullWidth
                        startIcon={<SendIcon />}
                        disabled={submitting}
                        sx={{ height: 56 }}
                      >
                        {submitting ? <CircularProgress size={22} color="inherit" /> : 'Apply'}
                      </Button>
                    </Grid>
                  </Grid>
                </form>
              </CardContent>
            </Card>

            {/* ---- History table ---- */}
            <Paper elevation={2}>
              {!history || history.content.length === 0 ? (
                <Box sx={{ p: 6, textAlign: 'center' }}>
                  <Typography color="text.secondary">No leave requests yet.</Typography>
                </Box>
              ) : (
                <>
                  <TableContainer>
                    <Table>
                      <TableHead>
                        <TableRow>
                          <TableCell>From</TableCell>
                          <TableCell>To</TableCell>
                          <TableCell>Days</TableCell>
                          <TableCell>Reason</TableCell>
                          <TableCell>Status</TableCell>
                          <TableCell>Decision Note</TableCell>
                          <TableCell align="right">Actions</TableCell>
                        </TableRow>
                      </TableHead>
                      <TableBody>
                        {history.content.map((leave) => (
                          <TableRow key={leave.id} hover>
                            <TableCell>{leave.startDate}</TableCell>
                            <TableCell>{leave.endDate}</TableCell>
                            <TableCell>{leave.totalDays}</TableCell>
                            <TableCell>{leave.reason}</TableCell>
                            <TableCell>
                              <LeaveStatusChip status={leave.status} />
                            </TableCell>
                            <TableCell>{leave.decisionComment ?? '—'}</TableCell>
                            <TableCell align="right">
                              {leave.status === 'PENDING' && (
                                <Button
                                  size="small"
                                  color="error"
                                  startIcon={<CancelIcon />}
                                  onClick={() => handleCancel(leave.id)}
                                  disabled={cancellingId === leave.id}
                                >
                                  Cancel
                                </Button>
                              )}
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
                      Page {history.page + 1} of {history.totalPages} · {history.totalElements}{' '}
                      total
                    </Typography>
                    <Stack direction="row" spacing={1}>
                      <Button
                        size="small"
                        disabled={history.first}
                        onClick={() => setPage((p) => p - 1)}
                      >
                        Previous
                      </Button>
                      <Button
                        size="small"
                        disabled={history.last}
                        onClick={() => setPage((p) => p + 1)}
                      >
                        Next
                      </Button>
                    </Stack>
                  </Stack>
                </>
              )}
            </Paper>
          </>
        )}
      </Stack>
    </MainLayout>
  );
};

export default MyLeavesPage;
