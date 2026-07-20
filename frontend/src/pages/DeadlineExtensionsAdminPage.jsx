import { useCallback, useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  CircularProgress,
  MenuItem,
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
import MoreTimeIcon from '@mui/icons-material/MoreTime';
import MainLayout from '../layouts/MainLayout';
import { deadlineExtensionService } from '../services/deadlineExtensionService';
import { userService } from '../services/userService';

const PAGE_SIZE = 20;

const DEADLINE_TYPES = [
  { value: 'WORK_PLAN', label: 'Work Schedule' },
  { value: 'WORK_REPORT', label: 'Work Report' },
  { value: 'LECTURE_SUMMARY', label: 'Lecture Summary' },
];

const typeLabel = (value) =>
  DEADLINE_TYPES.find((t) => t.value === value)?.label ?? value;

const EMPTY_FORM = { userId: '', type: 'WORK_REPORT', targetDate: '', extraHours: '48', reason: '' };

/**
 * Admin screen for granting deadline extensions (up to 48 hours) on work
 * schedules, work reports, and lecture summaries. Granting one also reverses
 * an already-applied penalty for that user and day.
 */
const DeadlineExtensionsAdminPage = () => {
  const [form, setForm] = useState(EMPTY_FORM);
  const [users, setUsers] = useState([]);
  const [submitting, setSubmitting] = useState(false);
  const [success, setSuccess] = useState(null);
  const [error, setError] = useState(null);

  const [pageData, setPageData] = useState(null);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [revokingId, setRevokingId] = useState(null);

  const loadUsers = useCallback(async () => {
    try {
      // Large page: the user picker needs everyone in one list.
      const response = await userService.getUsers(0, 200);
      setUsers(response.data.content);
    } catch {
      // Non-fatal: the picker stays empty and the admin sees the list error.
    }
  }, []);

  const loadExtensions = useCallback(async (pageNumber) => {
    setLoading(true);
    setError(null);
    try {
      const response = await deadlineExtensionService.getExtensions(pageNumber, PAGE_SIZE);
      setPageData(response.data);
    } catch (err) {
      setError(err?.message ?? 'Failed to load extensions.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadUsers();
  }, [loadUsers]);

  useEffect(() => {
    loadExtensions(page);
  }, [page, loadExtensions]);

  const handleChange = (field) => (event) => {
    setForm((prev) => ({ ...prev, [field]: event.target.value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSubmitting(true);
    setError(null);
    setSuccess(null);
    try {
      const response = await deadlineExtensionService.grantExtension({
        userId: Number(form.userId),
        type: form.type,
        targetDate: form.targetDate,
        extraHours: Number(form.extraHours),
        reason: form.reason.trim() || null,
      });
      setSuccess(response.message);
      setForm(EMPTY_FORM);
      await loadExtensions(0);
      setPage(0);
    } catch (err) {
      setError(err?.message ?? 'Failed to grant the extension.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleRevoke = async (id) => {
    setRevokingId(id);
    setError(null);
    try {
      await deadlineExtensionService.revokeExtension(id);
      await loadExtensions(page);
    } catch (err) {
      setError(err?.message ?? 'Failed to revoke the extension.');
    } finally {
      setRevokingId(null);
    }
  };

  return (
    <MainLayout>
      <Stack spacing={3}>
        <Box>
          <Typography variant="h4" component="h1" fontWeight={600}>
            Deadline Extensions
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Grant a grace period (up to 48 hours) on a work schedule, work report, or lecture
            summary deadline. Granting also reverses any penalty already applied for that day.
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

        {/* ---- Grant form ---- */}
        <Card elevation={2}>
          <CardContent>
            <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 2 }}>
              <MoreTimeIcon color="primary" />
              <Typography variant="h6">Grant an Extension</Typography>
            </Stack>

            <form onSubmit={handleSubmit}>
              <Stack spacing={2.5}>
                <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2.5}>
                  <TextField
                    select
                    label="User"
                    value={form.userId}
                    onChange={handleChange('userId')}
                    required
                    fullWidth
                    disabled={submitting}
                  >
                    {users.map((user) => (
                      <MenuItem key={user.id} value={user.id}>
                        {user.fullName} — {user.role}
                      </MenuItem>
                    ))}
                  </TextField>

                  <TextField
                    select
                    label="Deadline type"
                    value={form.type}
                    onChange={handleChange('type')}
                    required
                    fullWidth
                    disabled={submitting}
                  >
                    {DEADLINE_TYPES.map((t) => (
                      <MenuItem key={t.value} value={t.value}>
                        {t.label}
                      </MenuItem>
                    ))}
                  </TextField>
                </Stack>

                <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2.5}>
                  <TextField
                    label="Target date (the day the deadline belongs to)"
                    type="date"
                    value={form.targetDate}
                    onChange={handleChange('targetDate')}
                    required
                    fullWidth
                    disabled={submitting}
                    slotProps={{ inputLabel: { shrink: true } }}
                  />
                  <TextField
                    label="Extra hours"
                    type="number"
                    value={form.extraHours}
                    onChange={handleChange('extraHours')}
                    required
                    fullWidth
                    disabled={submitting}
                    inputProps={{ min: 1, max: 48 }}
                    helperText="1–48 hours added to the normal deadline"
                  />
                </Stack>

                <TextField
                  label="Reason (optional, shown to the user)"
                  value={form.reason}
                  onChange={handleChange('reason')}
                  fullWidth
                  disabled={submitting}
                  inputProps={{ maxLength: 500 }}
                />

                <Box>
                  <Button type="submit" variant="contained" disabled={submitting || !form.userId}>
                    {submitting ? <CircularProgress size={22} color="inherit" /> : 'Grant Extension'}
                  </Button>
                </Box>
              </Stack>
            </form>
          </CardContent>
        </Card>

        {/* ---- Granted extensions ---- */}
        <Paper elevation={2}>
          {loading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', p: 6 }}>
              <CircularProgress />
            </Box>
          ) : !pageData || pageData.content.length === 0 ? (
            <Box sx={{ p: 6, textAlign: 'center' }}>
              <Typography color="text.secondary">No extensions granted yet.</Typography>
            </Box>
          ) : (
            <>
              <TableContainer>
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>User</TableCell>
                      <TableCell>Type</TableCell>
                      <TableCell>Target Date</TableCell>
                      <TableCell>Extra Hours</TableCell>
                      <TableCell>Granted By</TableCell>
                      <TableCell>Reason</TableCell>
                      <TableCell align="right">Actions</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {pageData.content.map((ext) => (
                      <TableRow key={ext.id} hover>
                        <TableCell>{ext.userFullName}</TableCell>
                        <TableCell>
                          <Chip label={typeLabel(ext.type)} size="small" color="info" />
                        </TableCell>
                        <TableCell>{ext.targetDate}</TableCell>
                        <TableCell>+{ext.extraHours}h</TableCell>
                        <TableCell>{ext.grantedBy}</TableCell>
                        <TableCell sx={{ maxWidth: 260 }}>
                          <Typography variant="body2" noWrap title={ext.reason ?? ''}>
                            {ext.reason ?? '—'}
                          </Typography>
                        </TableCell>
                        <TableCell align="right">
                          <Button
                            size="small"
                            color="error"
                            onClick={() => handleRevoke(ext.id)}
                            disabled={revokingId === ext.id}
                          >
                            Revoke
                          </Button>
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

export default DeadlineExtensionsAdminPage;
