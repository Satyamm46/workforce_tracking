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
import AssignmentIcon from '@mui/icons-material/Assignment';
import MainLayout from '../layouts/MainLayout';
import { workReportService } from '../services/workReportService';
import { formatDateTime } from '../utils/formatters';

const PAGE_SIZE = 10;

/**
 * User's work report screen: submit an end-of-day report for the most recent
 * checkout (required within 24h to avoid absence), plus history.
 */
const MyWorkReportsPage = () => {
  const [reportText, setReportText] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [success, setSuccess] = useState(null);
  const [error, setError] = useState(null);

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

  useEffect(() => {
    loadHistory(page);
  }, [page, loadHistory]);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSubmitting(true);
    setError(null);
    setSuccess(null);
    try {
      const response = await workReportService.submitReport({ reportText });
      setSuccess(response.message);
      setReportText('');
      await loadHistory(0);
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
              <Typography variant="h6">Submit Today's Report</Typography>
            </Stack>

            <form onSubmit={handleSubmit}>
              <Stack spacing={2.5}>
                <TextField
                  label="What did you accomplish today?"
                  value={reportText}
                  onChange={(e) => setReportText(e.target.value)}
                  required
                  fullWidth
                  multiline
                  minRows={4}
                  inputProps={{ maxLength: 2000 }}
                  helperText={`${reportText.length}/2000 — Submit within 24h of checkout to avoid absence.`}
                  disabled={submitting}
                />

                <Box>
                  <Button type="submit" variant="contained" disabled={submitting}>
                    {submitting ? <CircularProgress size={22} color="inherit" /> : 'Submit Report'}
                  </Button>
                </Box>
              </Stack>
            </form>
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
                          <Typography variant="body2" noWrap title={report.reportText}>
                            {report.reportText}
                          </Typography>
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
