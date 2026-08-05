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
import AssignmentIcon from '@mui/icons-material/Assignment';
import MainLayout from '../layouts/MainLayout';
import ExpandableText from '../components/ExpandableText';
import { workReportService } from '../services/workReportService';
import { formatDateTime } from '../utils/formatters';

const PAGE_SIZE = 10;

/**
 * User's work report screen: submit a report for any checked-out day that is
 * still owed one (required within 24h of checkout to avoid absence), plus
 * history.
 *
 * The day is chosen explicitly rather than always assumed to be the latest
 * checkout. Otherwise a day whose deadline an admin extended could never be
 * filled in: once you check out again, the form would silently point at the
 * newer day and the extended one would stay unreported forever.
 */
const MyWorkReportsPage = () => {
  const [reportText, setReportText] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [success, setSuccess] = useState(null);
  const [error, setError] = useState(null);

  const [openDays, setOpenDays] = useState([]);
  const [openDaysLoading, setOpenDaysLoading] = useState(true);
  const [selectedDate, setSelectedDate] = useState('');

  const [history, setHistory] = useState(null);
  const [historyLoading, setHistoryLoading] = useState(true);
  const [page, setPage] = useState(0);

  const loadHistory = useCallback(async (pageNumber) => {
    setHistoryLoading(true);
    try {
      const response = await workReportService.getMyReports(pageNumber, PAGE_SIZE);
      setHistory(response.data);
    } catch (err) {
      setError(err?.message ?? 'Failed to load your reports.');
    } finally {
      setHistoryLoading(false);
    }
  }, []);

  const loadOpenDays = useCallback(async () => {
    setOpenDaysLoading(true);
    try {
      const response = await workReportService.getOpenReportDays();
      const days = response.data ?? [];
      setOpenDays(days);
      // The API returns them newest first; default to the most recent day owed.
      setSelectedDate(days.length > 0 ? days[0].workDate : '');
    } catch (err) {
      setError(err?.message ?? 'Failed to load the days awaiting a report.');
    } finally {
      setOpenDaysLoading(false);
    }
  }, []);

  useEffect(() => {
    loadHistory(page);
  }, [page, loadHistory]);

  useEffect(() => {
    loadOpenDays();
  }, [loadOpenDays]);

  const selectedDay = openDays.find((day) => day.workDate === selectedDate) ?? null;

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSubmitting(true);
    setError(null);
    setSuccess(null);
    try {
      const response = await workReportService.submitReport({
        reportText,
        workDate: selectedDate || null,
      });
      setSuccess(response.message);
      setReportText('');
      // The submitted day drops out of the owed list, so refresh both.
      await Promise.all([loadHistory(0), loadOpenDays()]);
      setPage(0);
    } catch (err) {
      setError(err?.message ?? 'Failed to submit the report.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <MainLayout>
      <Stack spacing={3}>
        <Box>
          <Typography variant="h4" component="h1" fontWeight={600}>
            My Work Reports
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Submit your end-of-day report within 24 hours of checkout to avoid being marked absent.
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

        {/* Submit Report Form */}
        <Card elevation={2}>
          <CardContent>
            <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 2 }}>
              <AssignmentIcon color="primary" />
              <Typography variant="h6">Submit Work Report</Typography>
            </Stack>

            {openDaysLoading ? (
              <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
                <CircularProgress />
              </Box>
            ) : openDays.length === 0 ? (
              <Alert severity="info">
                Nothing to submit right now — no checked-out day is waiting on a report. If you
                missed a day whose deadline has already passed, ask an admin to extend the work
                report deadline for that date and it will appear here.
              </Alert>
            ) : (
              <form onSubmit={handleSubmit}>
                <Stack spacing={2.5}>
                  {openDays.length > 1 ? (
                    <TextField
                      select
                      label="Report for"
                      value={selectedDate}
                      onChange={(e) => setSelectedDate(e.target.value)}
                      fullWidth
                      disabled={submitting}
                      helperText="You owe a report for more than one day — pick the day this report covers."
                    >
                      {openDays.map((day) => (
                        <MenuItem key={day.workDate} value={day.workDate}>
                          {day.workDate}
                          {day.extraHours > 0 ? ` · extended +${day.extraHours}h` : ''}
                          {day.overdue ? ' · overdue' : ''}
                        </MenuItem>
                      ))}
                    </TextField>
                  ) : (
                    <Typography variant="body2" color="text.secondary">
                      Reporting for <strong>{selectedDate}</strong>
                    </Typography>
                  )}

                  {selectedDay && (
                    <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
                      <Chip
                        size="small"
                        variant="outlined"
                        label={`Checked out ${formatDateTime(selectedDay.checkoutTime)}`}
                      />
                      <Chip
                        size="small"
                        variant="outlined"
                        color={selectedDay.overdue ? 'warning' : 'default'}
                        label={`Due ${formatDateTime(selectedDay.deadline)}`}
                      />
                      {selectedDay.extraHours > 0 && (
                        <Chip
                          size="small"
                          color="info"
                          label={`Deadline extended +${selectedDay.extraHours}h`}
                        />
                      )}
                    </Stack>
                  )}

                  {selectedDay?.markedAbsent && (
                    <Alert severity="warning">
                      This day is currently marked absent for the missing report. Submitting now
                      clears that.
                    </Alert>
                  )}

                  <TextField
                    label="What did you accomplish on this day?"
                    value={reportText}
                    onChange={(e) => setReportText(e.target.value)}
                    required
                    fullWidth
                    multiline
                    minRows={4}
                    slotProps={{ htmlInput: { maxLength: 2000 } }}
                    helperText={`${reportText.length}/2000 — Submit within 24h of checkout to avoid absence.`}
                    disabled={submitting}
                  />

                  <Box>
                    <Button
                      type="submit"
                      variant="contained"
                      disabled={submitting || !selectedDate}
                    >
                      {submitting ? <CircularProgress size={22} color="inherit" /> : 'Submit Report'}
                    </Button>
                  </Box>
                </Stack>
              </form>
            )}
          </CardContent>
        </Card>

        {/* History */}
        <Paper elevation={2}>
          {historyLoading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', p: 6 }}>
              <CircularProgress />
            </Box>
          ) : !history || history.content.length === 0 ? (
            <Box sx={{ p: 6, textAlign: 'center' }}>
              <Typography color="text.secondary">No reports submitted yet.</Typography>
            </Box>
          ) : (
            <>
              <TableContainer>
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>Work Date</TableCell>
                      <TableCell>Report</TableCell>
                      <TableCell>Submitted At</TableCell>
                      <TableCell>Status</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {history.content.map((report) => (
                      <TableRow key={report.id} hover>
                        <TableCell>{report.workDate}</TableCell>
                        <TableCell sx={{ maxWidth: 400 }}>
                          <ExpandableText text={report.reportText} />
                        </TableCell>
                        <TableCell>{formatDateTime(report.submittedAt)}</TableCell>
                        <TableCell>
                          <Chip
                            label={report.submittedLate ? 'Late' : 'On time'}
                            color={report.submittedLate ? 'warning' : 'success'}
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

export default MyWorkReportsPage;
